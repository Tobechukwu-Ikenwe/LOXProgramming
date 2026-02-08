# Makefile for clox - Lox Bytecode VM
CC = gcc
CFLAGS = -Wall -Wextra -std=c99 -Isrc
SRC = src/clox.c src/chunk.c src/compiler.c src/debug.c src/object.c src/scanner.c src/value.c src/vm.c

clox: $(SRC)
	$(CC) $(CFLAGS) -o clox $(SRC)

clean:
	rm -f clox clox.exe

.PHONY: clean
