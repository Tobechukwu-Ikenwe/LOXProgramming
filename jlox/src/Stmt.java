/**
 * Stmt.java - Abstract Syntax Tree (AST) nodes for statements.
 * 
 * A statement performs an ACTION (doesn't necessarily produce a value).
 * Examples: print 5; var x = 10; if (x) { ... }
 * 
 * We use the Visitor pattern again for clean traversal.
 * 
 * Statement types:
 * - ExpressionStmt: expr; (e.g., x + 1; or a function call)
 * - PrintStmt: print expr; (special syntax for output)
 * - VarStmt: var name = initializer; (variable declaration)
 * - BlockStmt: { stmt1; stmt2; } (block with its own scope)
 */
abstract class Stmt {
    // Visitor pattern for statement execution
    abstract <R> R accept(Stmt.Visitor<R> visitor);

    /** Visitor interface - interpreter implements this */
    interface Visitor<R> {
        R visitExpressionStmt(ExpressionStmt stmt);
        R visitPrintStmt(PrintStmt stmt);
        R visitVarStmt(VarStmt stmt);
        R visitBlockStmt(BlockStmt stmt);
        R visitIfStmt(IfStmt stmt);
        R visitWhileStmt(WhileStmt stmt);
    }

    // ========== Concrete Statement Classes ==========

    /** Expression as statement: expr; (e.g., x + 1;) */
    static class ExpressionStmt extends Stmt {
        final Expr expression;

        ExpressionStmt(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    /** Print statement: print expr; */
    static class PrintStmt extends Stmt {
        final Expr expression;

        PrintStmt(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    /** Variable declaration: var name = initializer; */
    static class VarStmt extends Stmt {
        final Token name;
        final Expr initializer;  // Can be null for "var x;"

        VarStmt(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    /** Block: { statement1; statement2; ... } - creates new scope */
    static class BlockStmt extends Stmt {
        final java.util.List<Stmt> statements;

        BlockStmt(java.util.List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    /** If statement: if (condition) thenBranch else elseBranch */
    static class IfStmt extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;  // Can be null

        IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    /** While loop: while (condition) body */
    static class WhileStmt extends Stmt {
        final Expr condition;
        final Stmt body;

        WhileStmt(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }
}
