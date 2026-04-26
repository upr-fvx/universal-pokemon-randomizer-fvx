package com.uprfvx.romio.ctr;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A read-only view of a part of a {@link RandomAccessFile}. A window if you will.
 * This class is meant to be used when the underlying RandomAccessFile has an internal file system,
 * to access specific files without having to copy them over to a separate buffer or file.
 * <br><br>
 * <b>Warning</b>: this changes the pointer offset of the underlying RandomAccessFile,
 * so make sure to reset the pointer after using this class.
 */
public class RandomAccessFileWindow implements DataInput {
    private final DataInput raf;
    private final long baseOffset;
    private final long size;
    private long pos = 0;

    /**
     * Creates a RandomAccessFileWindow from a RandomAccessFile.
     * @param raf The underlying RandomAccessFile to read from.
     * @param baseOffset The offset in the underlying RandomAccessFile where this window starts.
     * @param size The size of this window.
     */
    public RandomAccessFileWindow(RandomAccessFile raf, long baseOffset, long size) {
        this.raf = raf;
        this.baseOffset = baseOffset;
        this.size = size;
    }

    /**
     * Creates a RandomAccessFileWindow from a RandomAccessFileWindow.
     * @param raf The underlying RandomAccessFile to read from.
     * @param baseOffset The offset in the underlying RandomAccessFile where this window starts.
     * @param size The size of this window.
     */
    public RandomAccessFileWindow(RandomAccessFileWindow raf, long baseOffset, long size) {
        this.raf = raf;
        this.baseOffset = baseOffset;
        this.size = size;
    }

    public long size() {
        return size;
    }

    public void seek(long pos) throws IOException {
        if (pos < 0 || pos > size) {
            throw new IllegalArgumentException("position is out of bounds");
        }
        if (raf instanceof RandomAccessFile nonWindow) {
            this.pos = pos;
            nonWindow.seek(baseOffset + pos);
        } else if (raf instanceof RandomAccessFileWindow window) {
            this.pos = pos;
            window.seek(baseOffset + pos);
        } else {
            throw new IllegalStateException("underlying DataInput is not a RandomAccessFile or RandomAccessFileWindow");
        }
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        raf.readFully(b, (int) baseOffset, (int) size);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (off > size) {
            throw new IndexOutOfBoundsException("offset is out of bounds");
        }
        if (len > size - off) {
            throw new IndexOutOfBoundsException("length is more than the remaining size");
        }
        raf.readFully(b, (int) baseOffset + off, (int) size + len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int skipped = raf.skipBytes(n);
        pos += skipped;
        if (pos > size) {
            throw new IllegalArgumentException("skipped past the end of the window");
        }
        return skipped;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return raf.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return raf.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return raf.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return raf.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return raf.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return raf.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return raf.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return raf.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return raf.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return raf.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return raf.readUTF();
    }
}