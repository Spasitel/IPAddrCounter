package ru.spasitel;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicBitArray {
    private static final int WORD_SIZE = 32;
    private final AtomicIntegerArray bits;

    public AtomicBitArray(long size) {
        int arraySize = (int) (size / WORD_SIZE + (size % WORD_SIZE == 0 ? 0 : 1));
        bits = new AtomicIntegerArray(arraySize);
    }

    public boolean getBit(long pos) {
        int arrayPos = (int) (pos / WORD_SIZE);
        return (bits.get(arrayPos) & (1 << (pos % WORD_SIZE))) != 0;
    }

    /**
     * Atomically sets the bit at the specified position from false to true.
     *
     * @param pos the position of the bit to set
     * @return {@code true} if the bit was successfully set; {@code false} if the bit was already true or if a concurrent modification occurred
     */
    public boolean setBitTrue(long pos) {
        int arrayPos = (int) (pos / WORD_SIZE);
        int word = bits.get(arrayPos);
        int old = word;
        int posBit = 1 << (pos % WORD_SIZE);
        word |= posBit;
        if (word != old)
            return bits.compareAndSet(arrayPos, old, word);
        else
            return false;
    }
}
