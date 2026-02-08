@echo off
REM Build and run the Lox interpreter
REM Usage: run.bat              -- start REPL
REM        run.bat script.lox   -- run script (use ..\examples\01_arithmetic.lox from jlox dir)

cd /d "%~dp0"
if not exist build mkdir build

echo Compiling...
javac -d build src/*.java
if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

cd build
if "%~1"=="" (
    java Main
) else (
    java Main "%~1"
)
