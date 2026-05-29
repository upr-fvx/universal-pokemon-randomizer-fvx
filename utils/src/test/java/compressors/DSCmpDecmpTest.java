package compressors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for DSCmp and DSDecmp.
 *
 * We use DSCmp.compressLZ10/compressLZ11 to produce valid compressed bytes,
 * then verify DSDecmp.Decompress round-trips correctly where the decompressor
 * works correctly.
 *
 * BEHAVIOR QUIRK / BUG (DSDecmp LZ10): decompress10LZ does NOT bounds-check
 * the compressed-data offset before reading each byte within the 8-block loop.
 * This means that when the last flags byte has fewer than 8 valid blocks after
 * it (e.g., the data ends mid-way through the 8-block processing), the method
 * reads past the end of the array and throws ArrayIndexOutOfBoundsException.
 * This bug affects many input sizes — specifically those where the decompressed
 * length is NOT a multiple of 8 blocks (roughly multiples of 8 bytes).
 * The LZ11 path has the guard `curr_size < outData.length` in its inner loop
 * and does NOT have this bug.
 *
 * BEHAVIOR QUIRK / BUG (DSDecmp LZ10 empty input): for empty input, the
 * compressed header has a 3-byte length field of 0, which triggers the
 * "read 4-byte extended length" path in decompress10LZ. Since the compressed
 * data is only 4 bytes (magic + 3 zero length bytes), this immediately throws
 * IndexOutOfBoundsException while trying to read the 4-byte extended length.
 *
 * The LZ11 path (DSCmp.compressLZ11 → DSDecmp.Decompress) is used by the
 * actual randomizer for round-trips. LZ10 data in the real DS ROMs appears
 * to always be padded to 8-block boundaries, so the bug doesn't manifest in
 * practice.
 */
public class DSCmpDecmpTest {

    // --- LZ10 KNOWN BUG characterization tests ---

    /**
     * BUG: LZ10 decompressor throws IndexOutOfBoundsException on empty input.
     * DSCmp.compressLZ10(empty) produces a 4-byte header with length=0,
     * which triggers the extended-length read path (IOFunctions.readFullIntBigEndian)
     * but the compressed data is only 4 bytes → IOOBE.
     */
    @Test
    public void lz10RoundTrip_emptyInput_throwsDueToLZ10Bug() {
        byte[] original = new byte[0];
        byte[] compressed = DSCmp.compressLZ10(original);
        assertThrows(Exception.class,
                () -> DSDecmp.Decompress(compressed),
                "KNOWN BUG: LZ10 decompressor throws on empty input due to extended-length read path");
    }

    /**
     * BUG: LZ10 decompressor throws ArrayIndexOutOfBoundsException for various
     * small input sizes due to reading past the end of compressed data.
     * Characterize: inputs {1,5,15,16,24,32} bytes all fail with LZ10.
     * The AIOOBE index matches the length of the compressed data, confirming
     * the decompressor reads one byte beyond the end.
     */
    @Test
    public void lz10RoundTrip_singleByte_throwsDueToLZ10Bug() {
        byte[] compressed = DSCmp.compressLZ10(new byte[]{0x42});
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> DSDecmp.Decompress(compressed),
                "KNOWN BUG: LZ10 decompressor throws AIOOBE on 1-byte input");
    }

    @Test
    public void lz10RoundTrip_5bytes_throwsDueToLZ10Bug() {
        byte[] compressed = DSCmp.compressLZ10(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> DSDecmp.Decompress(compressed),
                "KNOWN BUG: LZ10 decompressor throws AIOOBE on 5-byte input");
    }

    @Test
    public void lz10RoundTrip_allZeros_64bytes_throwsDueToLZ10Bug() {
        byte[] compressed = DSCmp.compressLZ10(new byte[64]);
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> DSDecmp.Decompress(compressed),
                "KNOWN BUG: LZ10 decompressor throws AIOOBE on 64-byte all-zero input");
    }

    // --- LZ10 header byte assertion (still works even though decompression is buggy) ---

    @Test
    public void lz10CompressedStartsWith0x10() {
        byte[] original = {0x01, 0x02, 0x03};
        byte[] compressed = DSCmp.compressLZ10(original);
        assertEquals(DSCmp.LZ10, compressed[0] & 0xFF,
                "LZ10 compressed data should start with 0x10");
    }

    // --- LZ11 round-trips ---

    /**
     * BUG: LZ11 also throws IndexOutOfBoundsException on empty input.
     * Same extended-length read path bug as LZ10.
     * DSCmp.compressLZ11(empty) produces a 4-byte header with length=0,
     * which triggers IOFunctions.readFullIntBigEndian reading past end of array.
     */
    @Test
    public void lz11RoundTrip_emptyInput_throwsDueToExtendedLengthBug() {
        byte[] original = new byte[0];
        byte[] compressed = DSCmp.compressLZ11(original);
        assertThrows(Exception.class,
                () -> DSDecmp.Decompress(compressed),
                "KNOWN BUG: LZ11 decompressor also throws on empty input due to " +
                "extended-length read path trying to read 4 bytes past end of 4-byte header");
    }

    @Test
    public void lz11RoundTrip_singleByte() {
        byte[] original = {0x7F};
        byte[] compressed = DSCmp.compressLZ11(original);
        byte[] decompressed = DSDecmp.Decompress(compressed);
        assertArrayEquals(original, decompressed);
    }

    @Test
    public void lz11RoundTrip_shortSequence() {
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] compressed = DSCmp.compressLZ11(original);
        byte[] decompressed = DSDecmp.Decompress(compressed);
        assertArrayEquals(original, decompressed);
    }

    @Test
    public void lz11RoundTrip_allZeros() {
        byte[] original = new byte[64];
        byte[] compressed = DSCmp.compressLZ11(original);
        byte[] decompressed = DSDecmp.Decompress(compressed);
        assertArrayEquals(original, decompressed);
    }

    @Test
    public void lz11RoundTrip_allSameByte() {
        byte[] original = new byte[128];
        for (int i = 0; i < 128; i++) original[i] = 0x55;
        byte[] compressed = DSCmp.compressLZ11(original);
        byte[] decompressed = DSDecmp.Decompress(compressed);
        assertArrayEquals(original, decompressed);
    }

    @Test
    public void lz11RoundTrip_repeatingPattern() {
        byte[] original = new byte[80];
        for (int i = 0; i < 80; i++) original[i] = (byte)(i % 16);
        byte[] compressed = DSCmp.compressLZ11(original);
        byte[] decompressed = DSDecmp.Decompress(compressed);
        assertArrayEquals(original, decompressed);
    }

    @Test
    public void lz11RoundTrip_randomBytes() {
        byte[] original = new byte[200];
        for (int i = 0; i < 200; i++) original[i] = (byte)(((i * 37) + 13) & 0xFF);
        byte[] compressed = DSCmp.compressLZ11(original);
        byte[] decompressed = DSDecmp.Decompress(compressed);
        assertArrayEquals(original, decompressed);
    }

    @Test
    public void lz11CompressedStartsWith0x11() {
        byte[] original = {0x01, 0x02};
        byte[] compressed = DSCmp.compressLZ11(original);
        assertEquals(DSCmp.LZ11, compressed[0] & 0xFF,
                "LZ11 compressed data should start with 0x11");
    }

    // --- DSDecmp invalid magic ---

    @Test
    public void decompressThrowsOnUnknownMagicByte() {
        byte[] badData = new byte[8];
        badData[0] = 0x20; // not 0x10 or 0x11
        assertThrows(IllegalArgumentException.class,
                () -> DSDecmp.Decompress(badData),
                "Should throw on unknown magic byte");
    }

    // --- Decompress with offset (LZ11, which works correctly) ---

    @Test
    public void decompressLZ11WithOffset() {
        byte[] original = {0x10, 0x20, 0x30, 0x40};
        byte[] compressed = DSCmp.compressLZ11(original);

        // Prepend 5 junk bytes then concatenate compressed data
        byte[] shiftedData = new byte[5 + compressed.length];
        System.arraycopy(compressed, 0, shiftedData, 5, compressed.length);

        byte[] decompressed = DSDecmp.Decompress(shiftedData, 5);
        assertArrayEquals(original, decompressed);
    }
}
