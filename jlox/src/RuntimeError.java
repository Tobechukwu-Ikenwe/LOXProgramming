/**
 * RuntimeError.java - Exception thrown when a runtime error occurs.
 * 
 * Unlike parse errors (which we recover from), runtime errors happen
 * during execution: undefined variable, type mismatch, division by zero, etc.
 * We store the token that caused the error for helpful error messages.
 */
class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
