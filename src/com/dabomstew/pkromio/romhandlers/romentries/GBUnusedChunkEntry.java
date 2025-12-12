package com.dabomstew.pkromio.romhandlers.romentries;

public class GBUnusedChunkEntry {

    private final int offset;
    private final int length;

    public GBUnusedChunkEntry(int offset, int length) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive.");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length must be positive.");
        }
        this.offset = offset;
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
}