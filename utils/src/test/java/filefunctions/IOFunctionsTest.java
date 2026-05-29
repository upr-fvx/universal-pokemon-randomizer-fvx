package filefunctions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for IOFunctions.
 *
 * All methods are pure byte-array operations (no I/O), making them easy to
 * test without file system access.
 */
public class IOFunctionsTest {

    // --- readFullInt (little-endian) ---

    @Test
    public void readFullInt_littleEndian_zeroValue() {
        byte[] data = {0x00, 0x00, 0x00, 0x00};
        assertEquals(0, IOFunctions.readFullInt(data, 0));
    }

    @Test
    public void readFullInt_littleEndian_knownValue() {
        // 0x01 0x02 0x03 0x04 little-endian = 0x04030201
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        assertEquals(0x04030201, IOFunctions.readFullInt(data, 0));
    }

    @Test
    public void readFullInt_withOffset() {
        byte[] data = {(byte)0xFF, 0x00, 0x00, 0x00, 0x00, (byte)0xFF};
        assertEquals(0, IOFunctions.readFullInt(data, 1));
    }

    // --- readFullIntBigEndian ---

    @Test
    public void readFullIntBigEndian_zeroValue() {
        byte[] data = {0x00, 0x00, 0x00, 0x00};
        assertEquals(0, IOFunctions.readFullIntBigEndian(data, 0));
    }

    @Test
    public void readFullIntBigEndian_knownValue() {
        // 0x01 0x02 0x03 0x04 big-endian = 0x01020304
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        assertEquals(0x01020304, IOFunctions.readFullIntBigEndian(data, 0));
    }

    @Test
    public void readFullIntBigEndian_withOffset() {
        byte[] data = {0x00, 0x01, 0x02, 0x03, 0x04};
        assertEquals(0x01020304, IOFunctions.readFullIntBigEndian(data, 1));
    }

    // --- read2ByteInt (little-endian) ---

    @Test
    public void read2ByteInt_littleEndian() {
        byte[] data = {0x34, 0x12};
        assertEquals(0x1234, IOFunctions.read2ByteInt(data, 0));
    }

    @Test
    public void read2ByteInt_zero() {
        byte[] data = {0x00, 0x00};
        assertEquals(0, IOFunctions.read2ByteInt(data, 0));
    }

    // --- read2ByteIntBigEndian ---

    @Test
    public void read2ByteIntBigEndian_knownValue() {
        byte[] data = {0x12, 0x34};
        assertEquals(0x1234, IOFunctions.read2ByteIntBigEndian(data, 0));
    }

    @Test
    public void read2ByteIntBigEndian_zero() {
        byte[] data = {0x00, 0x00};
        assertEquals(0, IOFunctions.read2ByteIntBigEndian(data, 0));
    }

    // --- write2ByteInt ---

    @Test
    public void write2ByteInt_writesLittleEndian() {
        byte[] data = new byte[4];
        IOFunctions.write2ByteInt(data, 1, 0x1234);
        assertEquals((byte)0x34, data[1]);
        assertEquals((byte)0x12, data[2]);
    }

    @Test
    public void write2ByteInt_roundTrip() {
        byte[] data = new byte[2];
        IOFunctions.write2ByteInt(data, 0, 0xABCD);
        assertEquals(0xABCD, IOFunctions.read2ByteInt(data, 0));
    }

    // --- writeFullInt ---

    @Test
    public void writeFullInt_roundTrip() {
        byte[] data = new byte[4];
        IOFunctions.writeFullInt(data, 0, 0x12345678);
        assertEquals(0x12345678, IOFunctions.readFullInt(data, 0));
    }

    @Test
    public void writeFullInt_zero() {
        byte[] data = new byte[4];
        IOFunctions.writeFullInt(data, 0, 0);
        assertArrayEquals(new byte[4], data);
    }

    // --- writeFullIntBigEndian ---

    @Test
    public void writeFullIntBigEndian_roundTrip() {
        byte[] data = new byte[4];
        IOFunctions.writeFullIntBigEndian(data, 0, 0xDEADBEEF);
        assertEquals(0xDEADBEEF, IOFunctions.readFullIntBigEndian(data, 0));
    }

    // --- readFullLong / writeFullLong ---

    @Test
    public void readFullLong_zero() {
        byte[] data = new byte[8];
        assertEquals(0L, IOFunctions.readFullLong(data, 0));
    }

    @Test
    public void writeFullLong_roundTrip() {
        byte[] data = new byte[8];
        long value = 0x0102030405060708L;
        IOFunctions.writeFullLong(data, 0, value);
        assertEquals(value, IOFunctions.readFullLong(data, 0));
    }

    @Test
    public void writeFullLong_negativeLong() {
        byte[] data = new byte[8];
        long value = Long.MIN_VALUE;
        IOFunctions.writeFullLong(data, 0, value);
        assertEquals(value, IOFunctions.readFullLong(data, 0));
    }

    // --- getCRC32 ---

    @Test
    public void getCRC32_emptyArray() {
        long crc = IOFunctions.getCRC32(new byte[0]);
        assertEquals(0L, crc);
    }

    @Test
    public void getCRC32_knownValue() {
        // CRC32 of {0x00} is 0xD202EF8D
        long crc = IOFunctions.getCRC32(new byte[]{0x00});
        assertEquals(0xD202EF8DL, crc);
    }

    @Test
    public void getCRC32_isDeterministic() {
        byte[] data = {0x01, 0x02, 0x03};
        assertEquals(IOFunctions.getCRC32(data), IOFunctions.getCRC32(data));
    }

    @Test
    public void getCRC32_differentDataDifferentCRC() {
        long crc1 = IOFunctions.getCRC32(new byte[]{0x01});
        long crc2 = IOFunctions.getCRC32(new byte[]{0x02});
        assertNotEquals(crc1, crc2);
    }
}
