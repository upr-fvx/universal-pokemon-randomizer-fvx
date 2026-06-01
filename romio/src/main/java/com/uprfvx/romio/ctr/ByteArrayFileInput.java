package com.uprfvx.romio.ctr;

import java.util.Arrays;

/**
 * Implements {@link RomfsFileInput}, by backing it on a byte array.
 */
public class ByteArrayFileInput implements RomfsFileInput {

    private final byte[] data;
    private int pos;

    public ByteArrayFileInput(byte[] data) {
        this.data = data;
    }

    public long size() {
        return data.length;
    }

    public void setPosition(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must be >= 0");
        }
        if (pos >= data.length) {
            throw new IllegalArgumentException("pos must be < size");
        }
        this.pos = pos;
    }

    public byte[] readFully() {
        return Arrays.copyOf(data, data.length);
    }

    public void read(byte[] b) {
        System.arraycopy(data, pos, b, 0, b.length);
        pos += b.length;
    }

    public byte readByte() {
        return data[pos++];
    }

    public byte readByteInPlace() {
        return data[pos];
    }

    public int readShort() {
        int value = ((data[pos + 1] & 0xFF) << 8) | (data[pos] & 0xFF);
        pos += 2;
        return value;
    }

    public int readInt() {
        int value = ((data[pos + 3] & 0xFF) << 24) | ((data[pos + 2] & 0xFF) << 16)
                | ((data[pos + 1] & 0xFF) << 8) | (data[pos] & 0xFF);
        pos += 4;
        return value;
    }

    public String readMagic() {
        byte[] chars = new byte[MAGIC_LEN];
        for (int i = 0; i < MAGIC_LEN; i++) {
            chars[MAGIC_LEN - 1 - i] = readByte();
        }
        return new String(chars);
    }
}
