/**
 * object.c - String allocation for variable names.
 */
#include "object.h"
#include <stdlib.h>
#include <string.h>

ObjString* copyString(const char* chars, int length) {
    ObjString* str = malloc(sizeof(ObjString));
    str->length = length;
    str->chars = malloc(length + 1);
    memcpy(str->chars, chars, length);
    str->chars[length] = '\0';
    return str;
}

void freeObject(ObjString* obj) {
    if (obj) {
        free(obj->chars);
        free(obj);
    }
}
