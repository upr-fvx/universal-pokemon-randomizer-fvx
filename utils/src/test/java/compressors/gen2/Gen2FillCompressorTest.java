package compressors.gen2;

import compressors.Gen2Cmp;
import compressors.Gen2Decmp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Gen2FillCompressor.
 *
 * Gen2FillCompressor encodes using BYTE_FILL (001), WORD_FILL (010), and
 * ZERO_FILL (011) commands only.
 *
 * BEHAVIOR QUIRK / BUG (inherited from Gen2Compressor.chunksToBytes):
 * the board is allocated as {@code new byte[uncompressed.length * 2]}.
 * For a 1-byte input this is 2 bytes, but writing header(1) + value(1) +
 * terminator(1) = 3 bytes causes an ArrayIndexOutOfBoundsException.
 * This is the same bug as in Gen2NullCompressor and Gen2SinglePassCompressor.
 * We characterize this by asserting that the exception IS thrown.
 */
public class Gen2FillCompressorTest {

    private final Gen2FillCompressor cmp = new Gen2FillCompressor();

    private byte[] compress(byte[] data) {
        return cmp.compress(data, Gen2Cmp.flipBits(data));
    }

    /**
     * BUG: single-byte input throws ArrayIndexOutOfBoundsException.
     * board = new byte[2], but header(1) + value(1) + terminator(1) = 3 bytes needed.
     */
    @Test
    public void compressSingleByte_throwsArrayIndexOutOfBounds() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> compress(new byte[]{0x55}),
                "KNOWN BUG: single-byte input causes AIOOBE in Gen2FillCompressor");
    }

    @Test
    public void roundTrip_allZeros_16bytes_usesZeroFill() {
        byte[] original = new byte[16];
        byte[] compressed = compress(original);
        // Should compress tightly due to zero-fill command
        assertTrue(compressed.length < original.length,
                "All-zeros data should compress smaller than raw with ZERO_FILL");
        assertArrayEquals(original, Gen2Decmp.decompress(compressed, 0));
    }

    @Test
    public void roundTrip_repeatedByte_16bytes_usesByteFill() {
        byte[] original = new byte[24];
        for (int i = 0; i < 24; i++) original[i] = (byte)0xAA;
        byte[] compressed = compress(original);
        // Expect significant compression (header=1, value=1, terminator=1 = 3 bytes)
        assertTrue(compressed.length < original.length);
        assertArrayEquals(original, Gen2Decmp.decompress(compressed, 0));
    }

    @Test
    public void roundTrip_alternatingBytes_usesWordFill() {
        byte[] original = new byte[20];
        for (int i = 0; i < 20; i++) original[i] = (byte)(i % 2 == 0 ? 0xAB : 0xCD);
        byte[] compressed = compress(original);
        assertArrayEquals(original, Gen2Decmp.decompress(compressed, 0));
    }

    @Test
    public void roundTrip_mixedData() {
        byte[] original = {0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x02, 0x03, 0x02, 0x03};
        assertArrayEquals(original, Gen2Decmp.decompress(compress(original), 0));
    }

    @Test
    public void compressTerminatesWithFF_twoByteInput() {
        // 2-byte input: board = 4 bytes, header(1) + 2 bytes value + terminator(1) = 4 OK
        byte[] compressed = compress(new byte[]{0x01, 0x01});
        assertEquals((byte)0xFF, compressed[compressed.length - 1]);
    }

    @Test
    public void roundTrip_exactly32bytes() {
        byte[] original = new byte[32];
        for (int i = 0; i < 32; i++) original[i] = (byte)(i % 4);
        assertArrayEquals(original, Gen2Decmp.decompress(compress(original), 0));
    }
}
