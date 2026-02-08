/**
 * Expr.java - Abstract Syntax Tree (AST) nodes for expressions.
 * 
 * An expression produces a VALUE (e.g., 2 + 3 produces 5).
 * We use the Visitor pattern: each expression type has an accept() method
 * that lets the interpreter "visit" and evaluate it.
 * 
 * Expression types:
 * - Literal: 42, "hello", true, nil
 * - Unary: -x, !x
 * - Binary: x + y, x * y, x == y
 * - Grouping: (x + y) - for precedence
 * - Variable: x (reference to a variable)
 * - Assign: x = 5 (assignment)
 */
abstract class Expr {
    // Visitor pattern: each subclass implements accept() to allow traversal
    abstract <R> R accept(Expr.Visitor<R> visitor);

    /** Visitor interface - the interpreter implements this to evaluate each expression type */
    interface Visitor<R> {
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitVariableExpr(Variable expr);
        R visitAssignExpr(Assign expr);
    }

    // ========== Concrete Expression Classes ==========

    /** Literal value: number, string, boolean, or nil */
    static class Literal extends Expr {
        final Object value;  // Double, String, Boolean, or null (nil)

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    /** Unary operator: -expr or !expr */
    static class Unary extends Expr {
        final Token operator;  // MINUS or BANG
        final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    /** Binary operator: left OP right (e.g., 1 + 2, 3 * 4) */
    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    /** Grouping: (expr) - used for explicit precedence */
    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    /** Variable reference: just the name (e.g., x) */
    static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    /** Assignment: name = value (e.g., x = 5) */
    static class Assign extends Expr {
        final Token name;
        final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }
}
