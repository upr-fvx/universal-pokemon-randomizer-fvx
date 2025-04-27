package com.dabomstew.pkromio.romhandlers.romentries;

/**
 * An entry describing an offset in a file, both as integers.
 */
public class InFileEntry {

    private final int file;
    private final int offset;

    public InFileEntry(int file, int offset) {
        this.file = file;
        this.offset = offset;
    }

    public int getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "file=" + file +"|offset=" + offset;
    }
}
