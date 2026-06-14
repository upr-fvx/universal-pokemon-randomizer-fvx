package com.uprfvx.romio.ctr;

import java.io.IOException;

/**
 * Represents the read-only contents of a {@link RomfsFile}, with appropriate methods for reading it.
 * The reading methods must advance the pos by the appropriate amount of bytes, facilitating sequential reading.
 * Also, reading must be done little-endian.
 * <br><br>
 * Since implementations may be reading from an actual file on disk, each method throws IOException.
 */
public interface RomfsFileInput {

    int MAGIC_LEN = 4;

    long size() throws IOException;

    void setPosition(int pos) throws IOException;

    byte[] readFully() throws IOException;

    // read normally, not in little endian
    void read(byte[] b) throws IOException;

    byte readByte() throws IOException;

    /**
     * Reads a byte without advancing the position.
     */
    byte readByteInPlace() throws IOException;

    int readShort() throws IOException;

    int readInt() throws IOException;

    String readMagic() throws IOException;

}
