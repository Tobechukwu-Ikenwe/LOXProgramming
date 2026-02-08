@echo off
cd /d "%~dp0"
gcc -Wall -std=c99 -Isrc -o clox src/clox.c src/chunk.c src/compiler.c src/debug.c src/object.c src/scanner.c src/value.c src/vm.c
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)
echo Build successful. Run: clox [script.lox]
