package com.dabomstew.pkromio.romhandlers.romentries;

/**
 * An entry for a file in the 3DS file system, with a path and expected CRC32s.
 */
public class ThreeDSFileEntry {
    private final String path;
    private final long[] expectedCRC32s;

    public ThreeDSFileEntry() {
        this("", new long[2]);
    }
    public ThreeDSFileEntry(String path, long[] expectedCRC32s) {
        this.path = path;
        this.expectedCRC32s = expectedCRC32s;
    }

    public String getPath() {
        return path;
    }

    public long[] getExpectedCRC32s() {
        return expectedCRC32s;
    }
}
