package cuecompressors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BLZCoder.BLZ_DecodePub.
 *
 * BLZCoder has two code paths triggered by the second argument:
 *  - "GARC" → calls LZSS_Decode (LZ11 variant)
 *  - anything else → calls BLZ_Decode (BLZ bottom-LZ format)
 *
 * The BLZ decode path reads a specific 8-byte footer structure. Constructing
 * synthetically valid BLZ data is complex. Instead we test the GARC/LZSS path
 * where the format (byte 0 = 0x11, then 3-byte little-endian length, then
 * compressed data) is easier to synthesize.
 *
 * For the BLZ path we verify the "not coded" path: when the inc_len field
 * (last 4 bytes read as little-endian with top bit masked) is < 1, the code
 * prints a warning and treats the whole buffer as raw (unencoded) data.
 *
 * BEHAVIOR QUIRK (BLZ "not coded"): when inc_len < 1, the method sets
 * enc_len=0 and dec_len=pak_len (the full buffer minus the 3 trailing padding
 * bytes), then pak_len=0 (no encoded region). The output is the full input
 * copy, which means BLZ_DecodePub returns a byte array equal to the original
 * data (padding excluded). We characterize this.
 *
 * BEHAVIOR QUIRK (LZSS_Decode / GARC path): the method returns null and
 * prints to System.err for various malformed inputs rather than throwing.
 */
public class BLZCoderTest {

    // -----------------------------------------------------------------------
    // GARC / LZSS path
    // -----------------------------------------------------------------------

    @Test
    public void blzDecodePub_GARC_nullOnBadMagicByte() {
        BLZCoder blz = new BLZCoder(null);
        byte[] badData = new byte[8];
        badData[0] = 0x10; // should be 0x11 for LZSS
        // Returns null and prints to stderr; does NOT throw
        assertNull(blz.BLZ_DecodePub(badData, "GARC"));
    }

    @Test
    public void blzDecodePub_GARC_emptyDecompressedOutput() {
        BLZCoder blz = new BLZCoder(null);
        // Build a minimal LZSS header with decSize=0
        // Byte 0: 0x11, bytes 1-3: decSize=0, then byte 4: extra int since decSize==0
        byte[] data = new byte[12];
        data[0] = 0x11;
        // decSize = buf.getInt() >>> 8 — little-endian int at offset 0 is 0x11000000 >>> 8 = 0x110000
        // That's non-zero, so we won't hit the decSize==0 branch. Let's set bytes 1-3 to 0 too.
        // Full little-endian int at offset 0: 0x11 | (0 << 8) | (0 << 16) | (0 << 24) = 0x11
        // >>> 8 = 0 → decSize=0 → reads next getInt()
        // next 4 bytes at offset 4: also 0 → decSize=0
        // Then output buffer of size 0, loop condition position(0) < 0 is false, done
        // Returns outBuf.array() which is byte[0]
        byte[] result = blz.BLZ_DecodePub(data, "GARC");
        assertNotNull(result, "Should return non-null for decSize=0 case (empty output)");
        assertEquals(0, result.length, "Should return empty array for decSize=0");
    }

    @Test
    public void blzDecodePub_GARC_simpleUncompressedByte() {
        // Build a LZSS file that decompresses to a single byte 0x42
        // Format: [0x11][3-byte LE decSize=1][...data...]
        // decSize calculation: getInt() = byte0 | (byte1<<8) | (byte2<<16) | (byte3<<24)
        //   = 0x11 | (0x01 << 8) | (0x00 << 16) | (0x00 << 24) = 0x0111
        //   >>> 8 = 0x01 → decSize = 1
        // Then we need 1 decompressed byte. With mask=1, flags bit = 0 → literal copy.
        // flags byte=0x00, then data byte 0x42.
        BLZCoder blz = new BLZCoder(null);
        byte[] data = {
                0x11, 0x01, 0x00, 0x00,  // header: magic=0x11, decSize LE bytes 1-3 = {1,0,0}
                0x00,                     // flags byte (bit 7 = 0 → literal)
                0x42                      // literal byte
        };
        byte[] result = blz.BLZ_DecodePub(data, "GARC");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals((byte)0x42, result[0]);
    }

    // -----------------------------------------------------------------------
    // BLZ non-GARC path: "not coded" case (inc_len < 1)
    // -----------------------------------------------------------------------

    @Test
    public void blzDecodePub_nonGARC_notCodedFile_returnsData() {
        // When inc_len < 1 (the 4-byte value at pak_len-4 with top bit masked),
        // BLZ_Decode treats the file as unencoded and copies all bytes.
        // inc_len = readUnsigned(pak_buffer, pak_len-4) where pak_len = data.length - 3
        // pak_buffer is data[i] & 0xFF with 3 trailing zeros appended.
        // readUnsigned at offset (data.length - 3 - 4) = (data.length - 7):
        //   = buf[off] | (buf[off+1]<<8) | (buf[off+2]<<16) | ((buf[off+3] & 0x7F) << 24)
        // If those 4 bytes are all 0, inc_len = 0 < 1 → "not coded" path.
        // In the "not coded" path: enc_len=0, dec_len=pak_len=data.length-3, raw_len=dec_len,
        // then all dec_len bytes are copied, BLZ_Invert(raw_buffer, dec_len, 0) is a no-op,
        // raw_len = raw = dec_len.
        // Result: a byte array of length dec_len = data.length - 3.

        byte[] data = new byte[10];
        // All zeros — last 7 bytes are 0, so inc_len = 0 < 1.
        BLZCoder blz = new BLZCoder(null);
        byte[] result = blz.BLZ_DecodePub(data, "notGARC");
        // Should return non-null (it prints a warning but continues)
        // BEHAVIOR QUIRK: pak_len = data.length - 3 = 7 (3 padding bytes appended by prepareData).
        // dec_len = pak_len = 7, raw_len = 7. The loop copies 7 bytes then BLZ_Invert(enc_len=0
        // region) is a no-op. raw_len is then set to raw = 7.
        // BUT: the result array is raw_buffer[0..raw_len-1] = 10 ints (raw_buffer.length is
        // data.length + 3 = 13), yet raw_len = 7... Actually the actual returned length is 10
        // because raw = dec_len = 7, raw_end = 7, raw != raw_end prints a warning,
        // and raw_len = raw = 7. retbuf[0..6] = data[0..6] = all zeros. But the actual
        // observed behavior (from test run) is result.length = 10. Characterize as observed:
        assertNotNull(result, "Should return non-null for not-coded path");
        assertEquals(10, result.length,
                "Not-coded path: observed result length is 10 (characterizing actual behavior)");
    }
}
