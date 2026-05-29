package compressors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Gen2Cmp and Gen2Decmp.
 *
 * On Windows, Gen2Cmp.compress() delegates to lunarCompress() which requires a
 * native DLL. To avoid that dependency we call the in-Java path directly via
 * Gen2NullCompressor / Gen2FillCompressor / Gen2SinglePassCompressor and then
 * verify round-trips through Gen2Decmp.decompress().
 *
 * BEHAVIOR QUIRK (Gen2Cmp.compress on Windows): on Windows the method always
 * delegates to LunarCompressLibrary. Attempting to call it without the DLL
 * will throw an UnsatisfiedLinkError at runtime. We characterize this.
 *
 * BEHAVIOR QUIRK (Gen2Cmp.flipBits): the flip table maps each byte to a
 * bit-reversed version. flipBits(flipBits(x)) == x.
 */
public class Gen2CmpDecmpTest {

    // --- Gen2Cmp.flipBits ---

    @Test
    public void flipBitsEmptyArray() {
        byte[] result = Gen2Cmp.flipBits(new byte[0]);
        assertArrayEquals(new byte[0], result);
    }

    @Test
    public void flipBitsKnownValues() {
        // 0x00 flipped => 0x00; 0xFF flipped => 0xFF; 0x80 => 0x01; 0x01 => 0x80
        byte[] input = {0x00, (byte)0xFF, (byte)0x80, 0x01};
        byte[] expected = {0x00, (byte)0xFF, 0x01, (byte)0x80};
        assertArrayEquals(expected, Gen2Cmp.flipBits(input));
    }

    @Test
    public void flipBitsIsItsOwnInverse() {
        byte[] input = {0x12, 0x34, 0x56, 0x78, (byte)0xAB, (byte)0xCD, (byte)0xEF};
        assertArrayEquals(input, Gen2Cmp.flipBits(Gen2Cmp.flipBits(input)));
    }

    // --- Gen2Decmp.decompress round-trip via Gen2NullCompressor ---

    /**
     * BUG: single-byte input causes AIOOBE in Gen2NullCompressor
     * (board = new byte[2], needs 3 bytes).
     */
    @Test
    public void nullCompressorRoundTrip_singleByte_throwsDueToGen2Bug() {
        byte[] original = {0x42};
        compressors.gen2.Gen2NullCompressor nullCmp = new compressors.gen2.Gen2NullCompressor();
        byte[] bitFlipped = Gen2Cmp.flipBits(original);
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> nullCmp.compress(original, bitFlipped),
                "KNOWN BUG: 1-byte input causes AIOOBE in Gen2NullCompressor");
    }

    @Test
    public void nullCompressorRoundTrip_shortSequence() {
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
        compressDecompressRoundTrip(original);
    }

    @Test
    public void nullCompressorRoundTrip_32bytes() {
        byte[] original = new byte[32];
        for (int i = 0; i < 32; i++) original[i] = (byte)(i * 5);
        compressDecompressRoundTrip(original);
    }

    @Test
    public void nullCompressorRoundTrip_allZeros() {
        byte[] original = new byte[16];
        compressDecompressRoundTrip(original);
    }

    @Test
    public void nullCompressorRoundTrip_allSameByte() {
        byte[] original = new byte[24];
        for (int i = 0; i < 24; i++) original[i] = (byte)0xAA;
        compressDecompressRoundTrip(original);
    }

    // --- Gen2Decmp.lengthOfCompressed ---

    @Test
    public void lengthOfCompressedIsPositive() {
        byte[] original = {0x10, 0x20, 0x30};
        compressors.gen2.Gen2NullCompressor nullCmp = new compressors.gen2.Gen2NullCompressor();
        byte[] bitFlipped = Gen2Cmp.flipBits(original);
        byte[] compressed = nullCmp.compress(original, bitFlipped);

        int len = Gen2Decmp.lengthOfCompressed(compressed, 0);
        assertTrue(len > 0, "lengthOfCompressed should be positive");
        assertTrue(len <= compressed.length, "lengthOfCompressed should not exceed compressed array length");
    }

    // --- helper ---

    private void compressDecompressRoundTrip(byte[] original) {
        compressors.gen2.Gen2NullCompressor nullCmp = new compressors.gen2.Gen2NullCompressor();
        byte[] bitFlipped = Gen2Cmp.flipBits(original);
        byte[] compressed = nullCmp.compress(original, bitFlipped);

        byte[] decompressed = Gen2Decmp.decompress(compressed, 0);
        assertArrayEquals(original, decompressed,
                "Gen2NullCompressor round-trip should be lossless");
    }
}
