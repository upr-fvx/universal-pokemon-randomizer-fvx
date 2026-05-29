package text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for PokeTextData.
 *
 * PokeTextData decrypts/encrypts Gen 4 game text. The binary format is:
 *   - Short (LE): num entries
 *   - Short (LE): key (used in pointer decryption)
 *   - [num * 8 bytes]: encrypted pointers (4 bytes offset, 4 bytes char count)
 *   - [text data at each pointer offset]
 *
 * Building a minimal valid dataset requires knowing the encryption:
 *   - Pointer encryption: key2 = (key * 0x2FD) * (i+1) & 0xFFFF, realkey = key2 | (key2 << 16)
 *     each pair of 4-byte words XOR'd with realkey
 *   - Text encryption: key = (0x91BD3 * id) & 0xFFFF, each 2-byte word XOR'd with key,
 *     key += 0x493D each step
 *
 * Rather than reproducing the full encryption here, we test the observable
 * API properties:
 *  1. get() returns a copy of the original data.
 *  2. GetKey/SetKey manipulate the key at offset 2.
 *  3. encrypt(decrypt(x)) results in data that, when decrypt() is called again,
 *     produces the same strings.
 *
 * We use a minimal known-good binary fixture constructed with the correct
 * encryption formulas.
 *
 * BEHAVIOR QUIRK: strlist and compressFlag are public fields, not accessed
 * via getters. compressFlag is set to true if the first char of any entry
 * is 0xF100 (compressed format indicator).
 */
public class PokeTextDataTest {

    /**
     * Build a minimal Gen 4 text binary with one entry containing the
     * character 'A' (0x0000 terminated) using the correct encryption.
     */
    private static byte[] buildGen4TextBinary(String[] strings) {
        int numEntries = strings.length;
        // The data offset for text starts after header (4 bytes) + entry table (8*n bytes)
        int textOffset = 4 + numEntries * 8;

        // Build decrypted text entries (each: chars then 0xFFFF terminator)
        short[][] decTextWords = new short[numEntries][];
        for (int i = 0; i < numEntries; i++) {
            String s = strings[i];
            decTextWords[i] = new short[s.length() + 1];
            for (int j = 0; j < s.length(); j++) {
                decTextWords[i][j] = (short) s.charAt(j);
            }
            decTextWords[i][s.length()] = (short) 0xFFFF; // end marker
        }

        // Compute text positions
        int[] charCounts = new int[numEntries];
        int[] textOffsets = new int[numEntries];
        int runOff = textOffset;
        for (int i = 0; i < numEntries; i++) {
            textOffsets[i] = runOff;
            charCounts[i] = decTextWords[i].length;
            runOff += charCounts[i] * 2;
        }

        int totalSize = runOff;
        byte[] data = new byte[totalSize];

        // Write header: num entries + key (use 0 for simplicity)
        data[0] = (byte)(numEntries & 0xFF);
        data[1] = (byte)((numEntries >> 8) & 0xFF);
        data[2] = 0x00; // key = 0
        data[3] = 0x00;

        // key = 0 → key2 = (0 * 0x2FD * (i+1)) & 0xFFFF = 0 → realkey = 0
        // So pointer data XOR 0 = pointer data (no actual encryption when key=0)
        for (int i = 0; i < numEntries; i++) {
            int ofs = 4 + i * 8;
            // Write offset (4 bytes LE)
            writeInt32(data, ofs, textOffsets[i]);
            // Write char count (4 bytes LE)
            writeInt32(data, ofs + 4, charCounts[i]);
        }

        // Write encrypted text entries
        for (int i = 0; i < numEntries; i++) {
            int id = i + 1; // text id = index + 1
            int key = (0x91BD3 * id) & 0xFFFF;
            int pos = textOffsets[i];
            for (short w : decTextWords[i]) {
                int plain = w & 0xFFFF;
                int enc = plain ^ key;
                data[pos] = (byte)(enc & 0xFF);
                data[pos + 1] = (byte)((enc >> 8) & 0xFF);
                pos += 2;
                key = (key + 0x493D) & 0xFFFF;
            }
        }

        return data;
    }

    private static void writeInt32(byte[] data, int offset, int value) {
        data[offset] = (byte)(value & 0xFF);
        data[offset + 1] = (byte)((value >> 8) & 0xFF);
        data[offset + 2] = (byte)((value >> 16) & 0xFF);
        data[offset + 3] = (byte)((value >> 24) & 0xFF);
    }

    @Test
    public void getReturnsACopyOfData() {
        byte[] original = buildGen4TextBinary(new String[]{"A"});
        PokeTextData ptd = new PokeTextData(original);
        byte[] got = ptd.get();
        assertNotSame(original, got, "get() should return a copy, not the original array");
        assertArrayEquals(original, got);
    }

    @Test
    public void decryptPopulatesStrlist() {
        byte[] data = buildGen4TextBinary(new String[]{"A"});
        PokeTextData ptd = new PokeTextData(data);
        ptd.decrypt();
        assertNotNull(ptd.strlist, "strlist should be populated after decrypt()");
        assertEquals(1, ptd.strlist.size());
    }

    @Test
    public void decryptSingleCharacterEntry() {
        byte[] data = buildGen4TextBinary(new String[]{"A"});
        PokeTextData ptd = new PokeTextData(data);
        ptd.decrypt();
        // 'A' (0x0041) in the Gen4 Pokemon text table does NOT map to ASCII 'A'.
        // It maps to the Japanese hiragana character "む" (U+3080) because the
        // Gen4 encoding uses a custom table (Generation4.tbl), not ASCII.
        // BEHAVIOR QUIRK: tb[0x0041] = "む", so the decrypted string is "む".
        String result = ptd.strlist.get(0);
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Decrypted string should not be empty");
        // Characterize: whatever tb[0x0041] is, that's what we get
        String expectedMapping = UnicodeParser.tb[0x0041];
        if (expectedMapping != null) {
            assertEquals(expectedMapping, result,
                    "Decrypted 0x0041 should map through UnicodeParser.tb[0x0041]");
        } else {
            assertTrue(result.contains("0041"),
                    "If tb[0x0041] is null, output should be \\x0041 escape");
        }
    }

    @Test
    public void getKeyAndSetKey() {
        byte[] data = buildGen4TextBinary(new String[]{"B"});
        PokeTextData ptd = new PokeTextData(data);
        assertEquals(0, ptd.GetKey(), "Initial key should be 0 as built");
        ptd.SetKey(0x1234);
        assertEquals(0x1234, ptd.GetKey(), "GetKey should return the value set by SetKey");
    }

    @Test
    public void encryptAfterDecryptPreservesStrings() {
        byte[] data = buildGen4TextBinary(new String[]{"C"});
        PokeTextData ptd = new PokeTextData(data);
        ptd.decrypt();
        List<String> firstDecrypted = ptd.strlist;

        // Re-encrypt
        ptd.encrypt();

        // Decrypt again on a fresh copy of the re-encrypted data
        PokeTextData ptd2 = new PokeTextData(ptd.get());
        ptd2.decrypt();

        assertEquals(firstDecrypted, ptd2.strlist,
                "Decrypt → Encrypt → Decrypt should produce the same strings");
    }
}
