package compressors.gen2;

import compressors.Gen2Cmp;
import compressors.Gen2Decmp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Gen2SinglePassCompressor.
 *
 * Tests a representative subset of ALL_OPTIONS combinations plus explicit
 * constructor-parameter validation.
 *
 * BEHAVIOR QUIRK / BUG (shared with all Gen2 compressors): the internal board
 * is allocated as {@code new byte[uncompressed.length * 2]} in
 * Gen2Compressor.chunksToBytes. For a 1-byte input this is 2 bytes, but
 * writing header(1) + data(1) + terminator(1) = 3 bytes causes
 * ArrayIndexOutOfBoundsException. Tests with 1-byte input characterize this bug.
 */
public class Gen2SinglePassCompressorTest {

    // A few representative option combinations for parameterized round-trips
    static Stream<Gen2SinglePassCompressor> sampleOptions() {
        // First, last, and a few in the middle of ALL_OPTIONS
        int size = Gen2SinglePassCompressor.ALL_OPTIONS.size();
        return Stream.of(
                Gen2SinglePassCompressor.ALL_OPTIONS.get(0),
                Gen2SinglePassCompressor.ALL_OPTIONS.get(1),
                Gen2SinglePassCompressor.ALL_OPTIONS.get(size / 4),
                Gen2SinglePassCompressor.ALL_OPTIONS.get(size / 2),
                Gen2SinglePassCompressor.ALL_OPTIONS.get(size * 3 / 4),
                Gen2SinglePassCompressor.ALL_OPTIONS.get(size - 1)
        );
    }

    @Test
    public void allOptionsHasExpectedCount() {
        // 2 * 2 * 2 * 3 * 3 = 72 combinations
        assertEquals(72, Gen2SinglePassCompressor.ALL_OPTIONS.size());
    }

    @Test
    public void constructorThrowsOnNegativeMaxScanDelay() {
        assertThrows(IllegalArgumentException.class,
                () -> new Gen2SinglePassCompressor(false, false, false, -1,
                        Gen2SinglePassCompressor.CopyCommandPref.NRF));
    }

    @Test
    public void constructorThrowsOnMaxScanDelayTooLarge() {
        assertThrows(IllegalArgumentException.class,
                () -> new Gen2SinglePassCompressor(false, false, false, 3,
                        Gen2SinglePassCompressor.CopyCommandPref.NRF));
    }

    @Test
    public void constructorAcceptsMaxScanDelay0() {
        assertDoesNotThrow(() -> new Gen2SinglePassCompressor(false, false, false, 0,
                Gen2SinglePassCompressor.CopyCommandPref.NRF));
    }

    @Test
    public void constructorAcceptsMaxScanDelay2() {
        assertDoesNotThrow(() -> new Gen2SinglePassCompressor(false, false, false, 2,
                Gen2SinglePassCompressor.CopyCommandPref.FRN));
    }

    /**
     * BUG: single-byte input causes ArrayIndexOutOfBoundsException in all
     * Gen2 compressors via chunksToBytes.
     */
    @ParameterizedTest
    @MethodSource("sampleOptions")
    public void roundTrip_singleByte_throwsDueToGen2Bug(Gen2SinglePassCompressor compressor) {
        byte[] original = {0x7E};
        byte[] bitFlipped = Gen2Cmp.flipBits(original);
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> compressor.compress(original, bitFlipped),
                "KNOWN BUG: single-byte input causes AIOOBE in Gen2 compressors");
    }

    @ParameterizedTest
    @MethodSource("sampleOptions")
    public void roundTrip_allZeros_16bytes(Gen2SinglePassCompressor compressor) {
        byte[] original = new byte[16];
        assertRoundTrip(original, compressor);
    }

    @ParameterizedTest
    @MethodSource("sampleOptions")
    public void roundTrip_repeatingPattern_32bytes(Gen2SinglePassCompressor compressor) {
        byte[] original = new byte[32];
        for (int i = 0; i < 32; i++) original[i] = (byte)(i % 4);
        assertRoundTrip(original, compressor);
    }

    @ParameterizedTest
    @MethodSource("sampleOptions")
    public void roundTrip_randomLookingData_50bytes(Gen2SinglePassCompressor compressor) {
        byte[] original = new byte[50];
        for (int i = 0; i < 50; i++) original[i] = (byte)(((i * 31) + 17) & 0xFF);
        assertRoundTrip(original, compressor);
    }

    @Test
    public void toStringIncludesParameters() {
        Gen2SinglePassCompressor spc = new Gen2SinglePassCompressor(
                true, false, true, 1, Gen2SinglePassCompressor.CopyCommandPref.RFN);
        String s = spc.toString();
        assertTrue(s.contains("SPC{"), "toString should start with SPC{");
        assertTrue(s.contains("RFN"), "toString should contain the CopyCommandPref name");
    }

    private void assertRoundTrip(byte[] original, Gen2SinglePassCompressor compressor) {
        byte[] bitFlipped = Gen2Cmp.flipBits(original);
        byte[] compressed = compressor.compress(original, bitFlipped);
        byte[] decompressed = Gen2Decmp.decompress(compressed, 0);
        assertArrayEquals(original, decompressed,
                "Round-trip failed for compressor: " + compressor);
    }
}
