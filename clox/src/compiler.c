/**
 * compiler.c - Recursive descent compiler for Lox.
 * Emits bytecode directly (no AST).
 */
#include "compiler.h"
#include "scanner.h"
#include "object.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

typedef struct {
    Token current;
    Token previous;
    bool hadError;
    bool panicMode;
} Parser;

static Parser parser;
static Chunk* compilingChunk;

static void errorAt(Token* token, const char* message) {
    if (parser.panicMode) return;
    parser.panicMode = true;
    parser.hadError = true;
    fprintf(stderr, "[line %d] Error", token->line);
    if (token->type == TOKEN_EOF) {
        fprintf(stderr, " at end");
    } else if (token->type != TOKEN_ERROR) {
        fprintf(stderr, " at '%.*s'", token->length, token->start);
    }
    fprintf(stderr, ": %s\n", message);
}

static void error(const char* message) { errorAt(&parser.previous, message); }
static void errorAtCurrent(const char* message) { errorAt(&parser.current, message); }

static void advance(void) {
    parser.previous = parser.current;
    for (;;) {
        parser.current = scanToken();
        if (parser.current.type != TOKEN_ERROR) break;
        errorAtCurrent(parser.current.start);
    }
}

static bool match(TokenType type) {
    if (parser.current.type == type) {
        advance();
        return true;
    }
    return false;
}

static void consume(TokenType type, const char* message) {
    if (parser.current.type == type) {
        advance();
        return;
    }
    errorAtCurrent(message);
}

static void emitByte(uint8_t byte, int line) {
    writeChunk(compilingChunk, byte, line);
}

static void emitBytes(uint8_t b1, uint8_t b2, int line) {
    emitByte(b1, line);
    emitByte(b2, line);
}

static void emitConstant(Value value, int line) {
    emitBytes(OP_CONSTANT, (uint8_t)addConstant(compilingChunk, value), line);
}

static int emitJump(uint8_t op, int line) {
    emitByte(op, line);
    emitByte(0xff, line);
    emitByte(0xff, line);
    return compilingChunk->count - 2;
}

static void patchJump(int offset) {
    int jump = compilingChunk->count - offset - 2;
    compilingChunk->code[offset] = (jump >> 8) & 0xff;
    compilingChunk->code[offset + 1] = jump & 0xff;
}

static void patchJumpBack(int offset) {
    int jump = offset - compilingChunk->count - 2;
    compilingChunk->code[offset] = (jump >> 8) & 0xff;
    compilingChunk->code[offset + 1] = jump & 0xff;
}

static uint8_t identifierConstant(Token* name) {
    ObjString* str = copyString(name->start, name->length);
    return (uint8_t)addConstant(compilingChunk, OBJ_VAL(str));
}

static void expression(void);
static void declaration(void);

/* parsePrecedence1: unary, primary, and identifiers.
   The above only handles unary and primary. We need to parse:
   primary (op primary)* with correct precedence.
   Simplified: parse a chain of (primary op primary op primary...)
   for each precedence level. */
static void parsePrecedence1(void) {
    /* Parse unary/primary first */
    if (match(TOKEN_BANG) || match(TOKEN_MINUS)) {
        TokenType op = parser.previous.type;
        parsePrecedence1();
        if (op == TOKEN_BANG) emitByte(OP_NOT, parser.previous.line);
        else emitByte(OP_NEGATE, parser.previous.line);
        return;
    }
    if (match(TOKEN_FALSE)) { emitByte(OP_FALSE, parser.previous.line); return; }
    if (match(TOKEN_TRUE)) { emitByte(OP_TRUE, parser.previous.line); return; }
    if (match(TOKEN_NIL)) { emitByte(OP_NIL, parser.previous.line); return; }
    if (match(TOKEN_NUMBER)) {
        emitConstant(NUMBER_VAL(strtod(parser.previous.start, NULL)), parser.previous.line);
        return;
    }
    if (match(TOKEN_LEFT_PAREN)) {
        expression();
        consume(TOKEN_RIGHT_PAREN, "Expect ')' after expression.");
        return;
    }
    if (match(TOKEN_IDENTIFIER)) {
        Token name = parser.previous;
        uint8_t arg = identifierConstant(&name);
        if (match(TOKEN_EQUAL)) {
            expression();
            emitBytes(OP_SET_GLOBAL, arg, parser.previous.line);
        } else {
            emitBytes(OP_GET_GLOBAL, arg, parser.previous.line);
        }
        return;
    }
    errorAtCurrent("Expect expression.");
}

/* expression with binary ops - loop for * / + - == != < <= > >= */
static void expression(void) {
    parsePrecedence1();
    while (1) {
        if (match(TOKEN_STAR)) {
            parsePrecedence1();
            emitByte(OP_MULTIPLY, parser.previous.line);
        } else if (match(TOKEN_SLASH)) {
            parsePrecedence1();
            emitByte(OP_DIVIDE, parser.previous.line);
        } else if (match(TOKEN_PLUS)) {
            parsePrecedence1();
            emitByte(OP_ADD, parser.previous.line);
        } else if (match(TOKEN_MINUS)) {
            parsePrecedence1();
            emitByte(OP_SUBTRACT, parser.previous.line);
        } else if (match(TOKEN_EQUAL_EQUAL)) {
            parsePrecedence1();
            emitByte(OP_EQUAL, parser.previous.line);
        } else if (match(TOKEN_BANG_EQUAL)) {
            parsePrecedence1();
            emitByte(OP_EQUAL, parser.previous.line);
            emitByte(OP_NOT, parser.previous.line);
        } else if (match(TOKEN_LESS)) {
            parsePrecedence1();
            emitByte(OP_LESS, parser.previous.line);
        } else if (match(TOKEN_LESS_EQUAL)) {
            parsePrecedence1();
            emitByte(OP_GREATER, parser.previous.line);
            emitByte(OP_NOT, parser.previous.line);
        } else if (match(TOKEN_GREATER)) {
            parsePrecedence1();
            emitByte(OP_GREATER, parser.previous.line);
        } else if (match(TOKEN_GREATER_EQUAL)) {
            parsePrecedence1();
            emitByte(OP_LESS, parser.previous.line);
            emitByte(OP_NOT, parser.previous.line);
        } else {
            break;
        }
    }
}

static void declaration(void) {
    if (match(TOKEN_VAR)) {
        consume(TOKEN_IDENTIFIER, "Expect variable name.");
        uint8_t arg = identifierConstant(&parser.previous);
        if (match(TOKEN_EQUAL)) {
            expression();
        } else {
            emitByte(OP_NIL, parser.previous.line);
        }
        consume(TOKEN_SEMICOLON, "Expect ';' after variable declaration.");
        emitBytes(OP_DEFINE_GLOBAL, arg, parser.previous.line);
        return;
    }
    /* statement */
    if (match(TOKEN_PRINT)) {
        expression();
        consume(TOKEN_SEMICOLON, "Expect ';' after value.");
        emitByte(OP_PRINT, parser.previous.line);
        return;
    }
    if (match(TOKEN_IF)) {
        consume(TOKEN_LEFT_PAREN, "Expect '(' after 'if'.");
        expression();
        consume(TOKEN_RIGHT_PAREN, "Expect ')' after if condition.");
        int thenJump = emitJump(OP_JUMP_IF_FALSE, parser.previous.line);
        emitByte(OP_POP, parser.previous.line);
        declaration();
        int elseJump = emitJump(OP_JUMP, parser.previous.line);
        patchJump(thenJump);
        emitByte(OP_POP, parser.previous.line);
        if (match(TOKEN_ELSE)) {
            declaration();
        }
        patchJump(elseJump);
        return;
    }
    if (match(TOKEN_WHILE)) {
        int loopStart = compilingChunk->count;
        consume(TOKEN_LEFT_PAREN, "Expect '(' after 'while'.");
        expression();
        consume(TOKEN_RIGHT_PAREN, "Expect ')' after condition.");
        int exitJump = emitJump(OP_JUMP_IF_FALSE, parser.previous.line);
        emitByte(OP_POP, parser.previous.line);
        declaration();
        {
            int offset = loopStart - compilingChunk->count - 2;
            emitByte(OP_LOOP, parser.previous.line);
            emitByte((offset >> 8) & 0xff, parser.previous.line);
            emitByte(offset & 0xff, parser.previous.line);
        }
        patchJump(exitJump);
        emitByte(OP_POP, parser.previous.line);
        return;
    }
    if (match(TOKEN_LEFT_BRACE)) {
        while (parser.current.type != TOKEN_RIGHT_BRACE && !parser.hadError) {
            declaration();
        }
        consume(TOKEN_RIGHT_BRACE, "Expect '}' after block.");
        return;
    }
    /* Expression statement */
    expression();
    consume(TOKEN_SEMICOLON, "Expect ';' after expression.");
    emitByte(OP_POP, parser.previous.line);
}

bool compile(const char* source, Chunk* chunk) {
    initScanner(source);
    compilingChunk = chunk;
    parser.hadError = false;
    parser.panicMode = false;
    advance();
    while (!match(TOKEN_EOF)) {
        declaration();
    }
    emitByte(OP_RETURN, parser.previous.line);
    return !parser.hadError;
}
