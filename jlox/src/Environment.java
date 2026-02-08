/**
 * Environment.java - Manages variable bindings and scopes.
 * 
 * An environment is a map from variable names (strings) to values.
 * It supports nested scopes: each block creates a new environment that
 * "encloses" the outer one. When we look up a variable, we search inner
 * scope first, then outer, then outer's outer, etc.
 * 
 * Example:
 *   var x = 1;
 *   {
 *     var y = 2;    // inner scope
 *     print x + y;  // x from outer, y from inner
 *   }
 *   print x;        // y is gone
 */
import java.util.HashMap;
import java.util.Map;

class Environment {
    // Parent scope - null for global scope
    final Environment enclosing;

    // Variable name -> value
    private final Map<String, Object> values = new HashMap<>();

    /** Create global scope (no parent) */
    Environment() {
        this.enclosing = null;
    }

    /** Create inner scope with given parent */
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Define a variable (var x = 5; or just var x;)
     * Allows redefining - "var x = 1; var x = 2;" is valid in Lox.
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Get variable value. Searches this scope, then parent, then grandparent...
     * @throws RuntimeError if variable is not found (undefined)
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Assign to variable. Updates existing binding in this or ancestor scope.
     * Does NOT create new variable - assignment to undefined is an error.
     */
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
