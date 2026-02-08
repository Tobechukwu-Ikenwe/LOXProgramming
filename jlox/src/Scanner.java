/**
 * Scanner.java - Lexer/Tokenizer: converts source code into a stream of Tokens.
 * 
 * The scanner reads character by character and groups them into meaningful
 * lexical units (tokens). It handles:
 * - Whitespace (ignored, except newlines for line counting)
 * - Comments (// line comments)
 * - Numbers (integers and decimals like 42, 3.14)
 * - Strings (double-quoted "hello")
 * - Keywords (var, print, if, while, etc.)
 * - Identifiers (variable names)
 * - Operators and punctuation (+, -, *, /, ==, !=, etc.)
 * 
 * Flow: source string -> characters -> tokens
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;       // The raw source code
    private final List<Token> tokens = new ArrayList<>();
    
    // Scanner state - our position in the source
    private int start = 0;   // Start of current lexeme
    private int current = 0; // Current character being looked at
    private int line = 1;    // Current line number (for error reporting)

    // Reserved keywords - map keyword string to TokenType
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    /**
     * Main entry point: scan entire source and return list of tokens.
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;  // Beginning of next lexeme
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * Scan a single token. Dispatches based on current character.
     */
    private void scanToken() {
        char c = advance();

        switch (c) {
            // Single-character tokens
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            // Slash: could be / (division) or // (comment)
            case '/':
                if (match('/')) {
                    // Line comment - consume until end of line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // One-or-two character tokens
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;

            // Whitespace
            case ' ':
            case '\r':
            case '\t':
                break;  // Ignore

            case '\n':
                line++;
                break;

            // String literals
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character: '" + c + "'");
                }
                break;
        }
    }

    /** Handle string literals: "hello world" */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        advance();  // Consume closing "
        String value = source.substring(start + 1, current - 1);  // Strip quotes
        addToken(TokenType.STRING, value);
    }

    /** Handle number literals: 42, 3.14 */
    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();  // Consume .
            while (isDigit(peek())) advance();
        }
        double value = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, value);
    }

    /** Handle identifiers and keywords: myVar, print, if */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    /** Advance and consume current character, return it */
    private char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(current++);
    }

    /** Consume 'expected' only if it matches current char (for ==, !=, etc.) */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /** Look at current char without consuming (peek ahead) */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /** Look one character ahead (for decimal numbers like 3.14) */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    /** Add token without literal value */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /** Add token with literal value (for numbers, strings) */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
