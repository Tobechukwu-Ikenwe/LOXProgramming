/**
 * object.h - Runtime objects (strings for variable names).
 * Minimal implementation for storing variable names in bytecode.
 */
#ifndef clox_object_h
#define clox_object_h

#include "common.h"

typedef struct ObjString {
    int length;
    char* chars;
} ObjString;

ObjString* copyString(const char* chars, int length);
void freeObject(ObjString* obj);

#endif
