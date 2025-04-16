package ru.spasitel;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicBitArray {
    private static final int ALL_ONES = 0xFFFFFFFF;
    private static final int WORD_SIZE = 32;
    private AtomicIntegerArray bits;

    public AtomicBitArray(long size) {
        int arraySize = (int) (size / WORD_SIZE + (size % WORD_SIZE == 0 ? 0 : 1));
        bits = new AtomicIntegerArray(arraySize);
    }

    public boolean getBit(long pos) {
        int arrayPos = (int) (pos / WORD_SIZE);
        return (bits.get(arrayPos) & (1 << (pos % WORD_SIZE))) != 0;
    }

    public boolean setBit(long pos, boolean b) {
        int arrayPos = (int) (pos / WORD_SIZE);
        int word = bits.get(arrayPos);
        int old = word;
        int posBit = 1 << (pos % WORD_SIZE);
        if (b) {
            word |= posBit;
        } else {
            word &= (ALL_ONES - posBit);
        }
        return bits.compareAndSet(arrayPos, old, word);
    }
}
