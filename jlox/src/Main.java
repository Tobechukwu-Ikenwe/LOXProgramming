/**
 * Main.java - Entry point for the Lox interpreter.
 * 
 * Two modes:
 * 1. REPL (no args): Read lines from stdin, parse, interpret, print result. Loop.
 * 2. File (one arg): Read entire file, parse, interpret.
 * 
 * Usage:
 *   java Main              # Start REPL
 *   java Main script.lox   # Run script
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);  // Unix convention: 64 = command line usage error
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Run a Lox script from a file.
     * Reads entire file, runs it, exits with 65 if there was a parse error.
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (Lox.hadError) System.exit(65);   // Parse/scan error
        if (Lox.hadRuntimeError) System.exit(70);  // Runtime error
    }

    /**
     * REPL: Read-Eval-Print Loop.
     * Read a line, parse it, run it, print result. Repeat until Ctrl-D.
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        System.out.println("Lox Interpreter (Tree-Walking) - Type exit to quit");
        System.out.println("=================================================");

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;  // Ctrl-D
            if (line.trim().equalsIgnoreCase("exit")) break;

            run(line);
            Lox.hadError = false;  // Don't kill REPL on error - allow user to fix
        }
    }

    /**
     * Core execution: source -> tokens -> AST -> interpret.
     */
    private static void run(String source) {
        // 1. Scan: convert source to tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // 2. Parse: convert tokens to AST
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a parse error
        if (Lox.hadError) return;

        // 3. Interpret: execute the AST
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
