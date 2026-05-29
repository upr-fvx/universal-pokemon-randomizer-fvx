package text;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for N3DSTextHandler.
 *
 * The binary format for readTexts is:
 *   - Short (LE) numSections
 *   - Short (LE) numEntries
 *   - Int (LE) totalLength
 *   - Int (LE) initialKey (must be 0)
 *   - Int (LE) sectionDataOffset
 *   - [sectionDataOffset points to:]
 *     - Int (LE) sectionLength (must equal totalLength)
 *     - [numEntries * 8 bytes]: (Int offset from sectionDataOffset) + (Short entryLength) + 2 pad bytes
 *     - [entry data]: each entry = entryLength * 2 bytes of XOR-encrypted text
 *
 * Entry 0 key = KEY_BASE = 0x7C89.
 * Each character is XOR'd with a rotating key.
 *
 * BEHAVIOR QUIRK: readTexts validates numSections==1, initialKey==0,
 * sectionLength==totalLength. If any check fails, it returns an empty list
 * with a System.err message.
 */
public class N3DSTextHandlerTest {

    private static final int KEY_BASE = 0x7C89;

    /**
     * Build a minimal valid text binary with one section and given plaintext strings.
     * Each string is encoded as its char values (Unicode) terminated by 0x0000.
     */
    private static byte[] buildTextBinary(List<String> strings) {
        int numEntries = strings.size();
        int sectionDataOffset = 0x10; // fixed header size

        // Encode each string as UTF-16 LE chars + null terminator, then XOR with key
        byte[][] encodedEntries = new byte[numEntries][];
        int key = KEY_BASE;
        for (int i = 0; i < numEntries; i++) {
            String s = strings.get(i);
            int charCount = s.length() + 1; // +1 for null terminator
            short[] plainChars = new short[charCount];
            for (int j = 0; j < s.length(); j++) plainChars[j] = (short) s.charAt(j);
            plainChars[charCount - 1] = 0x0000; // terminator

            // XOR with rolling key
            byte[] encoded = new byte[charCount * 2];
            int rotKey = key;
            for (int j = 0; j < charCount; j++) {
                int plain = plainChars[j] & 0xFFFF;
                int xored = plain ^ (rotKey & 0xFFFF);
                encoded[j * 2] = (byte)(xored & 0xFF);
                encoded[j * 2 + 1] = (byte)((xored >> 8) & 0xFF);
                rotKey = (rotKey << 3 | rotKey >>> 13) & 0xFFFF;
            }
            encodedEntries[i] = encoded;
            key = (key + 0x2983) & 0xFFFF;
        }

        // Compute offsets within the section (after the section-length int + entry table)
        int entryTableSize = numEntries * 8; // 4 bytes offset + 2 bytes length + 2 pad
        int dataStartOffset = 4 + entryTableSize; // relative to sectionDataOffset
        int[] entryOffsets = new int[numEntries];
        int runningOffset = dataStartOffset;
        for (int i = 0; i < numEntries; i++) {
            entryOffsets[i] = runningOffset;
            int len = encodedEntries[i].length;
            runningOffset += len;
            // Align to 4 bytes
            if (len % 4 == 2) runningOffset += 2;
        }
        int sectionLength = runningOffset;
        int totalLength = sectionLength;

        // Build the full binary
        int totalSize = sectionDataOffset + sectionLength;
        ByteBuffer buf = ByteBuffer.allocate(totalSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // Header
        buf.putShort((short) 1);             // numSections
        buf.putShort((short) numEntries);     // numEntries
        buf.putInt(totalLength);             // totalLength
        buf.putInt(0);                       // initialKey = 0
        buf.putInt(sectionDataOffset);       // sectionDataOffset

        // Section header (at sectionDataOffset = 0x10)
        buf.putInt(sectionLength);           // sectionLength

        // Entry table
        for (int i = 0; i < numEntries; i++) {
            buf.putInt(entryOffsets[i]);                          // offset from sectionDataOffset
            buf.putShort((short)(encodedEntries[i].length / 2)); // char count
            buf.putShort((short) 0);                             // padding / unknown
        }

        // Entry data
        for (int i = 0; i < numEntries; i++) {
            buf.put(encodedEntries[i]);
            // Padding to 4-byte boundary
            if (encodedEntries[i].length % 4 == 2) {
                buf.put((byte)0);
                buf.put((byte)0);
            }
        }

        return buf.array();
    }

    @Test
    public void readTexts_singleSimpleString() {
        byte[] data = buildTextBinary(Collections.singletonList("A"));
        List<String> result = N3DSTextHandler.readTexts(data, false, 0);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0));
    }

    @Test
    public void readTexts_multipleStrings() {
        List<String> input = Arrays.asList("Hello", "World");
        byte[] data = buildTextBinary(input);
        List<String> result = N3DSTextHandler.readTexts(data, false, 0);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0));
        assertEquals("World", result.get(1));
    }

    @Test
    public void readTexts_emptyString() {
        byte[] data = buildTextBinary(Collections.singletonList(""));
        List<String> result = N3DSTextHandler.readTexts(data, false, 0);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test
    public void readTexts_invalidInitialKey_returnsEmptyList() {
        byte[] data = buildTextBinary(Collections.singletonList("test"));
        // Corrupt the initialKey field (at offset 8) to non-zero
        data[8] = 0x01;
        List<String> result = N3DSTextHandler.readTexts(data, false, 0);
        assertTrue(result.isEmpty(),
                "Non-zero initialKey should cause readTexts to return empty list");
    }

    @Test
    public void readTexts_invalidNumSections_returnsEmptyList() {
        byte[] data = buildTextBinary(Collections.singletonList("test"));
        // Set numSections to 2 (at offset 0, LE short)
        data[0] = 0x02;
        data[1] = 0x00;
        List<String> result = N3DSTextHandler.readTexts(data, false, 0);
        assertTrue(result.isEmpty(),
                "numSections != 1 should cause readTexts to return empty list");
    }

    @Test
    public void saveEntry_roundTrip_singleString() throws IOException {
        List<String> original = Collections.singletonList("B");
        byte[] data = buildTextBinary(original);

        List<String> read = N3DSTextHandler.readTexts(data, false, 0);
        byte[] resaved = N3DSTextHandler.saveEntry(data, read, 0);
        List<String> reread = N3DSTextHandler.readTexts(resaved, false, 0);

        assertEquals(read, reread, "Save/re-read round-trip should produce same strings");
    }

    @Test
    public void saveEntry_roundTrip_multipleStrings() throws IOException {
        List<String> original = Arrays.asList("Pikachu", "Bulbasaur", "Charmander");
        byte[] data = buildTextBinary(original);

        List<String> read = N3DSTextHandler.readTexts(data, false, 0);
        byte[] resaved = N3DSTextHandler.saveEntry(data, read, 0);
        List<String> reread = N3DSTextHandler.readTexts(resaved, false, 0);

        assertEquals(read, reread);
    }
}
