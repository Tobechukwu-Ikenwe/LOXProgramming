/**
 * chunk.h - Bytecode chunk: a sequence of instructions and constants.
 * 
 * A chunk is the compiled form of a Lox program. It contains:
 * - code: array of opcodes (bytes)
 * - constants: array of values (numbers, strings) - instructions reference by index
 * - lines: source line number for each instruction (for error reporting)
 * 
 * Think of it like: code[0]=OP_CONSTANT, code[1]=0 means "load constant at index 0"
 */
#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

// Opcodes - each byte in the chunk's code array
typedef enum {
    OP_CONSTANT,   // Push constant onto stack (1 byte: constant index)
    OP_NIL,        // Push nil
    OP_TRUE,       // Push true
    OP_FALSE,      // Push false
    OP_POP,        // Pop and discard top of stack
    OP_GET_LOCAL,  // Get local variable (1 byte: slot index)
    OP_SET_LOCAL,  // Set local variable (1 byte: slot index)
    OP_GET_GLOBAL, // Get global variable (1 byte: constant index for name)
    OP_DEFINE_GLOBAL,
    OP_SET_GLOBAL,
    OP_EQUAL,      // Pop two, push a == b
    OP_GREATER,
    OP_LESS,
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
    OP_NOT,        // Logical not
    OP_NEGATE,     // Unary minus
    OP_PRINT,      // Pop and print
    OP_JUMP,       // Unconditional jump (2 bytes)
    OP_JUMP_IF_FALSE,  // Pop, jump if falsy (2 bytes)
    OP_LOOP,       // Jump backward (2 bytes)
    OP_RETURN,     // Return from script
} OpCode;

typedef struct {
    int count;      // Number of used elements
    int capacity;   // Allocated size
    uint8_t* code;
    int* lines;     // Line number for each instruction
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int addConstant(Chunk* chunk, Value value);

#endif
