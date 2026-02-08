/**
 * Lox.java - Central error reporting and static state for the Lox interpreter.
 * 
 * This class holds:
 * - hadError: tracks if any error occurred (prevents execution after parse error)
 * - error(): report an error at a given line
 * - runtimeError(): report a runtime error (e.g., undefined variable)
 * 
 * We use static state so Scanner, Parser, and Interpreter can all report errors
 * to a central location.
 */
class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    /**
     * Report a compile-time (scan/parse) error.
     * Doesn't throw - we want to report as many errors as possible.
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Report error at a specific token (for parser errors with context).
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    /**
     * Report a runtime error (during interpretation).
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
