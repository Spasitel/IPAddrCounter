package org.spasitel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtomicBitArrayTest {

    @Test
    void invalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new AtomicBitArray(-10));
        AtomicBitArray atomicBitArray = new AtomicBitArray(10);
        assertThrows(IllegalArgumentException.class, () -> atomicBitArray.getBit(-10));
        assertThrows(IllegalArgumentException.class, () -> atomicBitArray.getBit(10));
        assertThrows(IllegalArgumentException.class, () -> atomicBitArray.setBitTrue(-10));
        assertThrows(IllegalArgumentException.class, () -> atomicBitArray.setBitTrue(10));
    }

    @Test
    void setBitTrue() {
        AtomicBitArray atomicBitArray = new AtomicBitArray(10);
        assertFalse(atomicBitArray.getBit(4));
        assertFalse(atomicBitArray.getBit(5));
        assertFalse(atomicBitArray.getBit(6));
        assertTrue(atomicBitArray.setBitTrue(5));
        assertFalse(atomicBitArray.getBit(4));
        assertTrue(atomicBitArray.getBit(5));
        assertFalse(atomicBitArray.getBit(6));
    }
}