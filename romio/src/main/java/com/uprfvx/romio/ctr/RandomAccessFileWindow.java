package com.uprfvx.romio.ctr;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * A read-only view of a part of a {@link RandomAccessFile}. A window if you will.
 * This class is meant to be used when the underlying RandomAccessFile has an internal file system,
 * to access specific files without having to copy them over to a separate buffer or file.
 * <br><br>
 * In addition to limiting the available data, it wraps the reading methods to be more amenable
 * to the needs of reading 3DS ROMs. The reading methods advance the pos by the appropriate
 * amount of bytes, facilitating sequential reading. Also, reading is done little-endian.
 * <br><br>
 * <b>Warning</b>: this changes the pointer offset of the underlying RandomAccessFile,
 * so make sure to reset the pointer after using this class.
 */
public class RandomAccessFileWindow {
    private static final int MAGIC_LEN = 4;

    private final RandomAccessFile raf;
    private final long baseOffset;
    private final int size;
    private int pos;

    /**
     * Creates a RandomAccessFileWindow from a RandomAccessFile.
     * @param raf The underlying RandomAccessFile to read from.
     * @param baseOffset The offset in the underlying RandomAccessFile where this window starts.
     * @param size The size of this window.
     */
    public RandomAccessFileWindow(RandomAccessFile raf, long baseOffset, int size) {
        this.raf = raf;
        this.baseOffset = baseOffset;
        this.size = size;
    }

    public long size() {
        return size;
    }

    public void seek(int pos) throws IOException {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must be >= 0");
        }
        if (pos >= size) {
            throw new IllegalArgumentException("pos must be < size");
        }
        raf.seek(baseOffset + pos);
        this.pos = pos;
    }

    public byte[] readFully() throws IOException {
        byte[] b = new byte[size];
        raf.seek(baseOffset);
        raf.read(b);
        return b;
    }

    // read normally, not in little endian
    public void read(byte[] b) throws IOException {
        seek(pos);
        raf.read(b);
        pos += b.length;
    }

    public byte readByte() throws IOException {
        seek(pos);
        byte b = raf.readByte();
        pos += 1;
        return b;
    }

    /**
     * Reads a byte without advancing the position.
     */
    public byte readByteInPlace() throws IOException {
        seek(pos);
        return raf.readByte();
    }

    public int readShort() throws IOException {
        byte[] bytes = new byte[2];
        seek(pos);
        raf.read(bytes, 0, 2);
        pos += 2;
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        return buf.getShort() & 0xFFFF;
    }

    public int readInt() throws IOException {
        byte[] bytes = new byte[4];
        seek(pos);
        raf.read(bytes, 0, 4);
        pos += 4;
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        return buf.getInt();
    }

    public String readMagic() throws IOException {
        byte[] chars = new byte[MAGIC_LEN];
        for (int i = 0; i < MAGIC_LEN; i++) {
            chars[MAGIC_LEN - 1 - i] = readByte();
        }
        return new String(chars);
    }

}