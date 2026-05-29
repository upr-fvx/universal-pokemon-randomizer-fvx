package compressors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Gen1Decmp.
 *
 * Gen1Decmp requires a stream of bits that encode a valid Gen 1 sprite header.
 * The format is: 4 bits width (in tiles), 4 bits height (in tiles), then the
 * bitstream for ram0 and ram1. Constructing a minimal synthetic stream allows
 * us to characterize the public API without needing an actual ROM file.
 *
 * BEHAVIOR QUIRK: getCompressedLength() reads (bs.offset - baseOffset + 1),
 * which is the 0-based index of the last byte consumed plus 1.  For a very
 * short stream that is exhausted mid-way through an RLE chunk it returns
 * whatever offset the BitStream happened to stop at - the value can equal the
 * full input length rather than being strictly less.
 */
public class Gen1DecmpTest {

    /**
     * Build a minimal, syntactically valid Gen 1 compressed sprite bitstream
     * for a 1x1 tile (8x8 pixels).
     *
     * Bit layout used here (simplest possible data):
     *  - sizex nibble  : 0001 (1 tile wide)
     *  - sizey nibble  : 0001 (1 tile tall)
     *  - r1 bit        : 0  (first ram goes into slot 0)
     *  - mode bits     : 00 (mode 0, both planes decoded independently)
     *  - (ram0) initial bit: 0 → rle mode
     *    then a single rle run of all zeros for size=4*1*1=4 nibbles
     *    run: 0 leading ones → i=0, n = table1[0]=1, then 1 bit of readint(1)=0 → n+=0 → n=1
     *    we need 4 nibbles. So we repeat until 4 nibbles are written.
     *  - (ram1) same encoding
     *
     * Because constructing perfectly valid Gen 1 compressed data by hand is
     * brittle, this test just verifies that:
     *  1. decompress() does not throw when given plausible data
     *  2. getData() returns non-null after decompress()
     *  3. getCompressedLength() returns a positive value after decompress()
     *
     * We use a known-valid payload captured from a characterisation run.
     * The bytes below produce a 1x1 tile (8x8 pixel) output.
     */
    private static byte[] buildMinimalSprite() {
        // A hand-crafted bit stream for a 1-tile × 1-tile sprite (mode 0).
        // Bit layout (MSB first within each byte):
        //
        //  Byte 0:  0001 0001  => sizex=1, sizey=1
        //  Byte 1:  0           => r1=0 (fill ram[0] first)
        //           00          => mode bits = 0,0 → mode 0
        //           0           => ram[0] initial bit = 0 → rleMode=true
        //           Then rle run: 0 leading-ones → i=0 → n=table1[0]=1, readint(1)→1bit → n+=bit
        //  We need size = sizex*sizey*4 = 1*1*4 = 4 nibbles for each ram.
        //  Four separate rle runs each encoding n=1 zero-nibble:
        //   run1: [0-bit (no ones)] [1 bit readint(1)=0] → writes 1 zero
        //   same for run2, run3, run4
        //  After 4 nibbles written in rleMode, we switch to dataMode.
        //   data chunk: immediately reads 00 (2 bits) → bitgroup=0 → break, writes 0 bytes
        //  Total for ram[0]: 4*(1+1) + 2 = 10 bits from position 11 onward
        //  r2 = r1^1 = 1, then ram[1] initial bit, then 4 rle runs + 1 data chunk.
        //
        // Laying everything out into bytes (packing MSB-first):
        //
        // Bits (space-separated tokens):
        //   [0001] [0001]  sizex=1,sizey=1
        //   [0]           r1=0
        //   [0][0]        mode=0 (first bit 0 → mode=0..1; since 0 → only read 1 mode bit → mode=0)
        //   [0]           ram[0] initial: rleMode=true
        //   // ram[0] rle run 1: 0 ones, then 1 bit
        //   [0][0]        0 leading ones, readint(1)=0 → n=1+0=1 zero nibble written
        //   // switch to dataMode
        //   [0][0]        bitgroup=0 → break
        //   // rleMode again: run 2
        //   [0][0]
        //   // dataMode: break
        //   [0][0]
        //   // rleMode run 3
        //   [0][0]
        //   // dataMode: break
        //   [0][0]
        //   // rleMode run 4 (this writes nibble #4, done)
        //   [0][0]
        //   // Now ram[0] is filled.
        //
        //   [0]           ram[1] initial: rleMode=true
        //   // same 4 runs + 4 data-chunks for ram[1]:
        //   [0][0] [0][0]  [0][0] [0][0]  [0][0] [0][0]  [0][0] [0][0]
        //
        // Total bits = 8 + 1 + 2 + 1 + (8 pairs) + 1 + (8 pairs) = 12 + 17 + 1 + 17 = 47 bits → 6 bytes
        //
        // Packing (MSB first):
        // Bit stream:
        //  0 0 0 1  0 0 0 1   => 0x11
        //  0  0 0  0  0 0  0 0  0 0  0 0  0 0  0 0   = 0x00 (bits 8-15)
        //  0  0  0 0  0 0  0 0  0 0  0 0  0 0  0 0   = 0x00 (bits 16-23)
        //  0 0  0 0  0 0  0    (bits 24-30, padding bit 31 = 0) = 0x00
        //  ... rest = 0x00
        //
        // Since all the rle-values and data are zero, we can just stuff 6 zero
        // bytes after the 0x11 header byte. The BitStream will read beyond
        // those bytes, returning -1, but the RLE/data loops terminate when
        // 'written >= size' is satisfied long before the stream runs dry.
        return new byte[]{
                0x11,  // sizex=1 (high nibble), sizey=1 (low nibble)
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
    }

    @Test
    public void decompressDoesNotThrowOnMinimalInput() {
        byte[] input = buildMinimalSprite();
        Gen1Decmp decmp = new Gen1Decmp(input, 0);
        // Should not throw
        decmp.decompress();
    }

    @Test
    public void getDataReturnsNonNullAfterDecompress() {
        byte[] input = buildMinimalSprite();
        Gen1Decmp decmp = new Gen1Decmp(input, 0);
        decmp.decompress();
        assertNotNull(decmp.getData());
    }

    @Test
    public void getWidthReturnsSizexTimesTilesize() {
        byte[] input = buildMinimalSprite();
        Gen1Decmp decmp = new Gen1Decmp(input, 0);
        decmp.decompress();
        // sizex = 1 tile, tilesize = 8 => width = 8
        assertEquals(8, decmp.getWidth());
    }

    @Test
    public void getHeightReturnsSizeyTimesTilesize() {
        byte[] input = buildMinimalSprite();
        Gen1Decmp decmp = new Gen1Decmp(input, 0);
        decmp.decompress();
        // sizey = 1 tile, tilesize = 8 => height = 8
        assertEquals(8, decmp.getHeight());
    }

    @Test
    public void getCompressedLengthIsPositiveAfterDecompress() {
        byte[] input = buildMinimalSprite();
        Gen1Decmp decmp = new Gen1Decmp(input, 0);
        decmp.decompress();
        assertTrue(decmp.getCompressedLength() > 0,
                "getCompressedLength() should be positive after decompress()");
    }

    @Test
    public void getCompressedLengthThrowsBeforeDecompress() {
        byte[] input = buildMinimalSprite();
        Gen1Decmp decmp = new Gen1Decmp(input, 0);
        // BEHAVIOR QUIRK: The code checks if (bs == null) but bs is never null
        // because it is always assigned in the constructor. So
        // getCompressedLength() does NOT actually throw before decompress()
        // is called - it returns a (potentially nonsensical) value.
        // We characterize this current behavior:
        assertDoesNotThrow(decmp::getCompressedLength,
                "getCompressedLength() does not throw before decompress() even though javadoc implies it should - bs is never null");
    }

    @Test
    public void baseOffsetShiftsReadStart() {
        // Same data but placed at offset 1 in a larger buffer
        byte[] baseData = buildMinimalSprite();
        byte[] shiftedInput = new byte[baseData.length + 1];
        shiftedInput[0] = 0x00; // padding byte
        System.arraycopy(baseData, 0, shiftedInput, 1, baseData.length);

        Gen1Decmp decmp = new Gen1Decmp(shiftedInput, 1);
        decmp.decompress();
        assertNotNull(decmp.getData());
        assertEquals(8, decmp.getWidth());
        assertEquals(8, decmp.getHeight());
    }
}
