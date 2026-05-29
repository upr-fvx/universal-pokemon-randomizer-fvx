package compressors.gen2;

import compressors.Gen2Cmp;
import compressors.Gen2Decmp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Gen2NullCompressor.
 *
 * Gen2NullCompressor uses only the DIRECT_COPY (000) command, so the
 * compressed output is always larger than the input for sufficiently-sized
 * inputs.
 *
 * BEHAVIOR QUIRK (BUG): the internal board is allocated as
 * {@code new byte[uncompressed.length * 2]}, which is:
 *  - 0 bytes for empty input → ArrayIndexOutOfBoundsException when writing
 *    the terminator 0xFF byte.
 *  - 2 bytes for a 1-byte input → too small to hold header(1) + data(1) + terminator(1),
 *    causing an ArrayIndexOutOfBoundsException when writing the terminator.
 *
 * These bugs are characterized as-is (tests assert that the exception is thrown).
 * Do not fix these bugs; these tests document current behavior.
 */
public class Gen2NullCompressorTest {

    private final Gen2NullCompressor cmp = new Gen2NullCompressor();

    private byte[] compress(byte[] data) {
        return cmp.compress(data, Gen2Cmp.flipBits(data));
    }

    /**
     * BUG: compress(empty) throws ArrayIndexOutOfBoundsException.
     * board = new byte[0], then board[0] = TERMINATOR → out of bounds.
     */
    @Test
    public void compressEmpty_throwsArrayIndexOutOfBounds() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> compress(new byte[0]),
                "KNOWN BUG: empty input causes ArrayIndexOutOfBoundsException because " +
                "board is allocated as length*2=0 but the terminator write accesses board[0]");
    }

    /**
     * BUG: compress(1-byte input) throws ArrayIndexOutOfBoundsException.
     * board = new byte[2], needs 3 bytes (header+data+terminator) → out of bounds.
     */
    @Test
    public void compressOneByte_throwsArrayIndexOutOfBounds() {
        byte[] original = {0x42};
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> compress(original),
                "KNOWN BUG: single-byte input causes ArrayIndexOutOfBoundsException because " +
                "board is allocated as 2 bytes but 3 are needed (header+data+terminator)");
    }

    @Test
    public void roundTrip_smallData_threeBytes() {
        byte[] original = {0x01, 0x02, 0x03};
        assertArrayEquals(original, Gen2Decmp.decompress(compress(original), 0));
    }

    @Test
    public void roundTrip_32bytes() {
        byte[] original = new byte[32];
        for (int i = 0; i < 32; i++) original[i] = (byte)(i);
        assertArrayEquals(original, Gen2Decmp.decompress(compress(original), 0));
    }

    @Test
    public void roundTrip_33bytes_splitsIntoTwoChunks() {
        // 33 bytes should split into one chunk of 32 and one chunk of 1
        byte[] original = new byte[33];
        for (int i = 0; i < 33; i++) original[i] = (byte)(i * 2);
        assertArrayEquals(original, Gen2Decmp.decompress(compress(original), 0));
    }

    @Test
    public void roundTrip_allZeros_16bytes() {
        byte[] original = new byte[16];
        assertArrayEquals(original, Gen2Decmp.decompress(compress(original), 0));
    }

    @Test
    public void compressedLengthIsLargerThanOriginal() {
        byte[] original = new byte[20];
        byte[] compressed = compress(original);
        // At least 1 header + 20 data + 1 terminator = 22
        assertTrue(compressed.length >= original.length + 2);
    }
}
