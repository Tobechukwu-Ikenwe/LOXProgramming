/**
 * Token.java - Represents a single lexical unit (token) in the source code.
 * 
 * The scanner produces a stream of tokens from raw source text. Each token has:
 * - type: What kind of token it is (number, identifier, keyword, operator, etc.)
 * - lexeme: The exact text from the source (e.g., "42", "myVar", "+")
 * - literal: The actual value if applicable (e.g., 42 for a number)
 * - line: Line number in source for error reporting
 * 
 * This is the foundation of our lexer's output.
 */
public class Token {
    // What kind of token this is (from TokenType enum)
    public final TokenType type;
    
    // The raw text as it appeared in source code (e.g., "print", "123")
    public final String lexeme;
    
    // The actual value for literals (numbers, strings) or null for keywords/operators
    public final Object literal;
    
    // Line number for helpful error messages
    public final int line;

    /**
     * Creates a new token.
     * @param type    The token type (NUMBER, PLUS, IDENTIFIER, etc.)
     * @param lexeme  The raw source text
     * @param literal The value (e.g., Double for numbers, null for operators)
     * @param line    Line number in source file
     */
    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + (literal != null ? " " + literal : "");
    }
}
