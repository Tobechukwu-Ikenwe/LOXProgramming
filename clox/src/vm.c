/**
 * vm.c - Stack-based bytecode VM execution.
 */
#include "vm.h"
#include "compiler.h"
#include "object.h"
#include "debug.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>

static VM vm;

static void resetStack(void) {
    vm.stackTop = 0;
}

static void push(Value value) {
    if (vm.stackCapacity < vm.stackTop + 1) {
        int oldCap = vm.stackCapacity;
        vm.stackCapacity = oldCap < 8 ? 8 : oldCap * 2;
        vm.stack = realloc(vm.stack, sizeof(Value) * vm.stackCapacity);
    }
    vm.stack[vm.stackTop++] = value;
}

static Value pop(void) {
    return vm.stack[--vm.stackTop];
}

static Value peek(int distance) {
    return vm.stack[vm.stackTop - 1 - distance];
}

/* Global variables: simple array of (name, value) pairs */
#define MAX_GLOBALS 256
static struct {
    ObjString* name;
    Value value;
} globals[MAX_GLOBALS];
static int globalCount = 0;

static ObjString* getConstantName(Chunk* chunk, uint8_t index) {
    Value v = chunk->constants.values[index];
    if (IS_OBJ(v)) return AS_OBJ(v);
    return NULL;
}

static bool stringsEqual(ObjString* a, ObjString* b) {
    if (a == b) return true;
    if (!a || !b || a->length != b->length) return false;
    return memcmp(a->chars, b->chars, a->length) == 0;
}

static bool getGlobal(ObjString* name, Value* out) {
    for (int i = 0; i < globalCount; i++) {
        if (stringsEqual(globals[i].name, name)) {
            *out = globals[i].value;
            return true;
        }
    }
    return false;
}

static void setGlobal(ObjString* name, Value value) {
    for (int i = 0; i < globalCount; i++) {
        if (stringsEqual(globals[i].name, name)) {
            globals[i].value = value;
            return;
        }
    }
    if (globalCount < MAX_GLOBALS) {
        globals[globalCount].name = name;
        globals[globalCount].value = value;
        globalCount++;
    }
}

static bool isTruthy(Value value) {
    if (IS_NIL(value)) return false;
    if (IS_BOOL(value)) return AS_BOOL(value);
    return true;
}

static void runtimeError(const char* format, ...) {
    fprintf(stderr, "Runtime error: ");
    va_list args;
    va_start(args, format);
    vfprintf(stderr, format, args);
    va_end(args);
    fprintf(stderr, "\n");
}

static InterpretResult run(void) {
#define READ_BYTE() (*vm.ip++)
#define READ_SHORT() (vm.ip += 2, (uint16_t)((vm.ip[-2] << 8) | vm.ip[-1]))
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define BINARY_OP(op) \
    do { \
        if (!IS_NUMBER(peek(0)) || !IS_NUMBER(peek(1))) { \
            runtimeError("Operands must be numbers."); \
            return INTERPRET_RUNTIME_ERROR; \
        } \
        double b = AS_NUMBER(pop()); \
        double a = AS_NUMBER(pop()); \
        push(NUMBER_VAL(a op b)); \
    } while (0)

    for (;;) {
#if DEBUG_TRACE_EXECUTION
        printf("          ");
        for (int i = 0; i < vm.stackTop; i++) {
            printf("[ ");
            printValue(vm.stack[i]);
            printf(" ]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif
        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_NIL: push(NIL_VAL); break;
            case OP_TRUE: push(BOOL_VAL(true)); break;
            case OP_FALSE: push(BOOL_VAL(false)); break;
            case OP_POP: pop(); break;
            case OP_GET_GLOBAL: {
                ObjString* name = getConstantName(vm.chunk, READ_BYTE());
                Value value;
                if (!getGlobal(name, &value)) {
                    runtimeError("Undefined variable '%.*s'.", name->length, name->chars);
                    return INTERPRET_RUNTIME_ERROR;
                }
                push(value);
                break;
            }
            case OP_DEFINE_GLOBAL: {
                ObjString* name = getConstantName(vm.chunk, READ_BYTE());
                setGlobal(name, peek(0));
                pop();
                break;
            }
            case OP_SET_GLOBAL: {
                ObjString* name = getConstantName(vm.chunk, READ_BYTE());
                Value v = pop();
                /* Check if exists - we need to add to globals if defining */
                Value old;
                if (!getGlobal(name, &old)) {
                    runtimeError("Undefined variable '%.*s'.", name->length, name->chars);
                    return INTERPRET_RUNTIME_ERROR;
                }
                setGlobal(name, v);
                push(v);  /* assignment yields the value */
                break;
            }
            case OP_EQUAL: {
                Value b = pop();
                Value a = pop();
                push(BOOL_VAL(valuesEqual(a, b)));
                break;
            }
            case OP_GREATER: BINARY_OP(>); break;
            case OP_LESS: BINARY_OP(<); break;
            case OP_ADD: {
                if (IS_NUMBER(peek(0)) && IS_NUMBER(peek(1))) {
                    double b = AS_NUMBER(pop());
                    double a = AS_NUMBER(pop());
                    push(NUMBER_VAL(a + b));
                } else {
                    runtimeError("Operands must be numbers.");
                    return INTERPRET_RUNTIME_ERROR;
                }
                break;
            }
            case OP_SUBTRACT: BINARY_OP(-); break;
            case OP_MULTIPLY: BINARY_OP(*); break;
            case OP_DIVIDE: {
                if (AS_NUMBER(peek(0)) == 0) {
                    runtimeError("Division by zero.");
                    return INTERPRET_RUNTIME_ERROR;
                }
                BINARY_OP(/);
                break;
            }
            case OP_NOT:
                push(BOOL_VAL(!isTruthy(pop())));
                break;
            case OP_NEGATE:
                if (!IS_NUMBER(peek(0))) {
                    runtimeError("Operand must be a number.");
                    return INTERPRET_RUNTIME_ERROR;
                }
                push(NUMBER_VAL(-AS_NUMBER(pop())));
                break;
            case OP_PRINT:
                printValue(pop());
                printf("\n");
                break;
            case OP_JUMP: {
                uint16_t offset = READ_SHORT();
                vm.ip += offset;
                break;
            }
            case OP_JUMP_IF_FALSE: {
                uint16_t offset = READ_SHORT();
                if (!isTruthy(peek(0))) {
                    pop();  /* pop the condition */
                    vm.ip += offset;
                }
                break;
            }
            case OP_LOOP: {
                uint16_t offset = READ_SHORT();
                vm.ip -= offset;
                break;
            }
            case OP_RETURN:
                return INTERPRET_OK;
        }
    }

#undef READ_BYTE
#undef READ_SHORT
#undef READ_CONSTANT
#undef BINARY_OP
}

void initVM(void) {
    resetStack();
    globalCount = 0;
}

void freeVM(void) {
    free(vm.stack);
}

InterpretResult interpret(const char* source) {
    Chunk chunk;
    initChunk(&chunk);
    if (!compile(source, &chunk)) {
        freeChunk(&chunk);
        return INTERPRET_COMPILE_ERROR;
    }
    vm.chunk = &chunk;
    vm.ip = chunk.code;
    InterpretResult result = run();
    freeChunk(&chunk);
    return result;
}
