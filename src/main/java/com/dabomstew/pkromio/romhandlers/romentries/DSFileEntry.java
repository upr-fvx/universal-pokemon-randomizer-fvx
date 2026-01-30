package com.dabomstew.pkromio.romhandlers.romentries;

/**
 * An entry for a file in the DS (NARC) file system, with a path and expected CRC32.
 */
public class DSFileEntry {

    private final String path;
    private final long expectedCRC32;

    public DSFileEntry() {
        this("", 0);
    }

    public DSFileEntry(String path, long expectedCRC32) {
        this.path = path;
        this.expectedCRC32 = expectedCRC32;
    }

    public String getPath() {
        return path;
    }

    public long getExpectedCRC32() {
        return expectedCRC32;
    }
}
