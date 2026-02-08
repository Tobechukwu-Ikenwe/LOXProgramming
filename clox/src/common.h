/**
 * common.h - Shared constants, macros, and types for clox.
 * 
 * This header is included by all clox source files. It defines:
 * - DEBUG_PRINT_CODE: when enabled, disassembles bytecode on compile
 * - DEBUG_TRACE_EXECUTION: when enabled, traces each VM instruction
 * - Common integer types and limits
 */
#ifndef clox_common_h
#define clox_common_h

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

// Set to 1 to print bytecode when compiling
#define DEBUG_PRINT_CODE 0

// Set to 1 to trace each instruction as VM executes
#define DEBUG_TRACE_EXECUTION 0

#endif
