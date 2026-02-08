/**
 * TokenType.java - Enumeration of all token types our lexer can produce.
 * 
 * We need distinct types for:
 * - Single-character tokens: ( ) { } , . - + ; / *
 * - One-or-two character: ! != = == < <= > >=
 * - Literals: identifiers, numbers, strings
 * - Keywords: and, class, else, false, for, fun, if, nil, or, print, return, super, this, true, var, while
 * - End of file: EOF
 */
public enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN,  // ( )
    LEFT_BRACE, RIGHT_BRACE,  // { }
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens
    BANG, BANG_EQUAL,        // ! !=
    EQUAL, EQUAL_EQUAL,      // = ==
    GREATER, GREATER_EQUAL,  // > >=
    LESS, LESS_EQUAL,        // < <=

    // Literals - identifiers and values
    IDENTIFIER, STRING, NUMBER,

    // Keywords (reserved words)
    AND, CLASS, ELSE, FALSE, FOR, FUN, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    // End of file - scanner produces this when input is exhausted
    EOF
}
