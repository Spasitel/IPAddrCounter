package ru.spasitel;

public class BitArray {

    private static final int ALL_ONES = 0xFFFFFFFF;
    private static final int WORD_SIZE = 32;
    private int[] bits;

    public BitArray(long size) {
        bits = new int[(int)(size / WORD_SIZE + (size % WORD_SIZE == 0 ? 0 : 1))];
    }

    public boolean getBit(long pos) {
        return (bits[(int)(pos / WORD_SIZE)] & (1 << (pos % WORD_SIZE))) != 0;
    }

    public void setBit(long pos, boolean b) {
        int word = bits[(int)(pos / WORD_SIZE)];
        int posBit = 1 << (pos % WORD_SIZE);
        if (b) {
            word |= posBit;
        } else {
            word &= (ALL_ONES - posBit);
        }
        bits[(int)(pos / WORD_SIZE)] = word;
    }

}