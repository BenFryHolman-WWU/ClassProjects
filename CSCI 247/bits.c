#include <assert.h>
#include <stdbool.h>
#include <stdlib.h>
#include "bits.h"

// Assert that the argument is a bit of either 0 or 1
#define assert_bit(a) if ((a) != 0 && (a) != 1) { assert(false); }

// Get the nth bit
uint16_t getbit(uint16_t number, int n) {
    return (number >> n) & 1;
}

// Get bits that are the given number of bits wide
uint16_t getbits(uint16_t number, int n, int wide) {
    return (number >> n) & ((1 << wide) - 1);
}

// Set the nth bit to the given bit value and return the result
uint16_t setbit(uint16_t number, int n) {
    return number | (1 << n);
}

// Clear the nth bit
uint16_t clearbit(uint16_t number, int n) {
    return number & ~(1 << n);
}

// Sign extend a number of the given bits to 16 bits
uint16_t sign_extend(uint16_t x, int bit_count) {
    if (x >> (bit_count - 1)) {
        x |= (0xFFFF << bit_count);
    }
    return x;
}

bool is_positive(uint16_t number) {
    return getbit(number, 15) == 0;
}

bool is_negative(uint16_t number) {
    return getbit(number, 15) == 1;
}
