package filefunctions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * Functions for reading/writing various date types, from/to byte arrays.
 */
public class IOFunctions {
    public static long readFullLong(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(data, offset, 8);
        buf.rewind();
        return buf.getLong();
    }

    public static int readFullInt(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(data, offset, 4);
        buf.rewind();
        return buf.getInt();
    }

    public static int readFullIntBigEndian(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.allocate(4).put(data, offset, 4);
        buf.rewind();
        return buf.getInt();
    }

    public static int read2ByteIntBigEndian(byte[] data, int index) {
        return (data[index + 1] & 0xFF) | ((data[index] & 0xFF) << 8);
    }

    public static int read2ByteInt(byte[] data, int index) {
        return (data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8);
    }

    public static void write2ByteInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    public static void writeFullInt(byte[] data, int offset, int value) {
        byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
        System.arraycopy(valueBytes, 0, data, offset, 4);
    }

    public static void writeFullIntBigEndian(byte[] data, int offset, int value) {
        byte[] valueBytes = ByteBuffer.allocate(4).putInt(value).array();
        System.arraycopy(valueBytes, 0, data, offset, 4);
    }

    public static void writeFullLong(byte[] data, int offset, long value) {
        byte[] valueBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
        System.arraycopy(valueBytes, 0, data, offset, 8);
    }

    public static long getCRC32(byte[] data) {
        CRC32 checksum = new CRC32();
        checksum.update(data);
        return checksum.getValue();
    }
}
