# Lox - Crafting Interpreters Implementation

A complete implementation of the **Lox** programming language from Robert Nystrom's [Crafting Interpreters](https://craftinginterpreters.com/) book.  Please read the book if you have the time 🙏.

## Project Structure

```
lox/
├── jlox/              # Tree-walking interpreter (Java)
│   └── src/
│       ├── Main.java       # Entry point, REPL, file execution
│       ├── Scanner.java    # Lexer/tokenizer
│       ├── Parser.java     # Parser → AST
│       ├── Expr.java       # Expression AST nodes
│       ├── Stmt.java       # Statement AST nodes
│       ├── Interpreter.java # Tree-walking execution
│       ├── Environment.java # Variable scopes
│       ├── Token.java      # Token class
│       ├── TokenType.java  # Token types enum
│       ├── Lox.java        # Error reporting
│       └── RuntimeError.java
├── clox/              # Bytecode VM (C)
└── examples/          # Example Lox programs
```

## How to Run

| Implementation | Build | Run REPL | Run script |
|----------------|-------|----------|------------|
| **jlox** (Java) | `cd jlox` then `javac -d build src/*.java` | `cd build` then `java Main` | `java Main ..\examples\01_arithmetic.lox` |
| **clox** (C) | `cd clox` then `build.bat` (Windows) or `make` (Linux/macOS), or use the clang command below if GCC is not available | `clox` or `./clox` | `clox ..\examples\01_arithmetic.lox` |

See **Build & Run** under each part below for full details (including Windows Clang build for clox).

## Part 1: Tree-Walking Interpreter (jlox)

### How It Works

1. **Scanner (Lexer)**: Converts source code `"var x = 5;"` into tokens `[VAR, IDENTIFIER(x), EQUAL, NUMBER(5), SEMICOLON]`
2. **Parser**: Builds an AST from tokens using recursive descent
3. **Interpreter**: Walks the AST recursively, evaluating expressions and executing statements
4. **Environment**: Tracks variable bindings with nested scopes

### Build & Run (Java)

```bash
cd lox/jlox
./run.bat                    # Start REPL
./run.bat ..\examples\01_arithmetic.lox   # Run a script
```

Or manually:
```bash
cd lox/jlox
javac -d build src/*.java
cd build
java Main                    # REPL
java Main ..\examples\01_arithmetic.lox   # Run script
```

### Lox Language Features

- **Literals**: numbers, strings, `true`, `false`, `nil`
- **Arithmetic**: `+`, `-`, `*`, `/`, unary `-`
- **Comparison**: `==`, `!=`, `<`, `<=`, `>`, `>=`
- **Variables**: `var x = 5;` `x = 10;`
- **Print**: `print expr;`
- **Blocks**: `{ var x = 1; print x; }`
- **Control flow**: `if (cond) stmt else stmt`, `while (cond) stmt`

### Example Programs

See `examples/` folder for:
- `01_arithmetic.lox` - Numbers and operators
- `02_variables.lox` - Variable declaration and assignment
- `03_blocks_scope.lox` - Blocks and scoping
- `04_control_flow.lox` - If and while
- `05_comprehensive.lox` - Fibonacci-like sequence

## Part 2: Bytecode VM (clox)

### How to Run (clox)

**Build** (from the `clox` folder). You need a C compiler: GCC, Clang, or MSVC.

- **Windows (GCC in PATH):**
  ```cmd
  cd clox
  build.bat
  ```
- **Windows (Clang, or GCC):**
  ```cmd
  cd clox
  clang -Wall -std=c99 -Isrc -o clox.exe src/clox.c src/chunk.c src/compiler.c src/debug.c src/object.c src/scanner.c src/value.c src/vm.c

  gcc -Wall -std=c99 -Isrc -o clox.exe \ src/clox.c src/chunk.c src/compiler.c src/debug.c \ src/object.c src/scanner.c src/value.c src/vm.c

  ```
- **Linux / macOS:**
  ```bash
  cd clox
  make
  # or: gcc -Wall -std=c99 -Isrc -o clox src/clox.c src/chunk.c src/compiler.c src/debug.c src/object.c src/scanner.c src/value.c src/vm.c
  ```

**Run:**

- **REPL** (interactive):
  ```cmd
  clox
  ```
  or `./clox` on Linux/macOS. Type `exit` to quit.

- **Run a script:**
  ```cmd
  clox ..\examples\01_arithmetic.lox
  ```
  or `./clox ../examples/01_arithmetic.lox` on Linux/macOS.

### How It Works

1. Scanner: Converts source text into a stream of tokens. 🔤

2. Compiler: Parses tokens and directly emits bytecode (no AST is built). 🧠

3. Chunk: Holds bytecode instructions and their constants. 📦

4. VM: Executes bytecode using a stack (push, pop, run ops). ⚙️

### Bytecode Example

For `print 2 + 3;`:
```
OP_CONSTANT 0    # push 2
OP_CONSTANT 1    # push 3
OP_ADD           # pop two, push 5
OP_PRINT         # pop and print
OP_RETURN
```

## Learning Concepts

- **Scanning**: Regex-like pattern matching, longest match, lookahead
- **Parsing**: Recursive descent, precedence climbing, expression grammar
- **AST**: Tree representation separating syntax from semantics (jlox)
- **Bytecode**: Dense instruction format, stack operations (clox)
- **Interpretation**: Visitor pattern (jlox) vs. instruction dispatch (clox)
- **Scopes**: Block-scoped variables, lexical scoping


## From the Author

- Read the code carefully.

- Learn the step-by-step process of building a programming language.

Understand how the languages you know and love are constructed.
