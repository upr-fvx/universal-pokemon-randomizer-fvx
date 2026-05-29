package text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case characterization tests for UnicodeParser (extending existing coverage).
 *
 * UnicodeParser has 83% line / 62.5% branch coverage in UPR-4. The uncovered
 * branches are around the CRLF stripping logic and null/empty table entries.
 *
 * The class is entirely static with a static initializer that loads the table.
 * tb[] is a 65536-element array; most entries are null. d is the reverse map.
 *
 * BEHAVIOR QUIRK (tb vs d size mismatch): tb has 1148 non-null entries but d
 * has only 1124 entries. Multiple table rows map different codepoints to the
 * same Unicode text string, and since d is a HashMap keyed on the string,
 * later entries overwrite earlier ones. The d map is therefore smaller.
 *
 * BEHAVIOR QUIRK (tb[0] is not null): the Generation4 table file contains an
 * entry for codepoint 0x0000 mapping it to the string literal "\x0000". So
 * tb[0] == "\x0000" rather than null.
 *
 * BEHAVIOR QUIRK (reverse map partial mismatch): some entries in d contain
 * encoded values that differ from tb due to file-encoding collisions when
 * the table is loaded via Scanner(UTF-8). In particular, certain Unicode
 * right-quote characters (e.g., code 0x01B3) appear differently in tb vs d
 * because the reverse map was last-write-wins and the last writer stored a
 * different byte sequence for the same visual character. We characterize this
 * by not asserting exact equality for the whole reverse map.
 */
public class UnicodeParserEdgeCasesTest {

    @Test
    public void tbArrayIsNonNull() {
        assertNotNull(UnicodeParser.tb, "tb array should be initialized");
    }

    @Test
    public void tbArrayHasExpectedSize() {
        assertEquals(65536, UnicodeParser.tb.length,
                "tb array should have 65536 entries");
    }

    @Test
    public void dMapIsNonNull() {
        assertNotNull(UnicodeParser.d, "d (reverse) map should be initialized");
    }

    @Test
    public void dMapIsNotEmpty() {
        assertFalse(UnicodeParser.d.isEmpty(),
                "d map should have entries from the table file");
    }

    /**
     * BEHAVIOR QUIRK: tb has more non-null entries than d entries because some
     * text strings appear multiple times in the table file and d is a HashMap
     * (last-write-wins for duplicate text keys). Characterize the observed counts.
     */
    @Test
    public void tbNonNullCountIsGreaterThanDSize() {
        int tbCount = 0;
        for (String s : UnicodeParser.tb) {
            if (s != null) tbCount++;
        }
        // tb has 1148 non-null entries, d has 1124 entries
        // (1148 and 1124 are the characterization values from the actual run)
        assertTrue(tbCount > UnicodeParser.d.size(),
                "tb non-null count (" + tbCount + ") should be > d map size (" + UnicodeParser.d.size() +
                ") due to duplicate text strings in the table");
        assertEquals(1148, tbCount, "tb should have 1148 non-null entries");
        assertEquals(1124, UnicodeParser.d.size(), "d should have 1124 entries");
    }

    /**
     * BEHAVIOR QUIRK: tb[0] is NOT null — the table maps codepoint 0x0000
     * to the string "\\x0000" (literal backslash-x-0000), not null.
     */
    @Test
    public void tbCodepoint0IsNotNull() {
        assertNotNull(UnicodeParser.tb[0],
                "tb[0] should NOT be null — codepoint 0x0000 maps to '\\x0000' in Gen4 table");
        assertEquals("\\x0000", UnicodeParser.tb[0],
                "tb[0] should be the literal string '\\x0000'");
    }

    @Test
    public void noEntryHasTrailingCRLF() {
        // Characterize the CRLF-stripping: no stored value should end with \r\n
        for (String s : UnicodeParser.tb) {
            if (s != null) {
                assertFalse(s.endsWith("\r\n"),
                        "No table entry should end with \\r\\n after loading: " + s);
            }
        }
    }

    @Test
    public void tbHasAtLeastOneEntry() {
        boolean found = false;
        for (String s : UnicodeParser.tb) {
            if (s != null) { found = true; break; }
        }
        assertTrue(found, "tb should have at least one non-null entry");
    }

    @Test
    public void dMapContainsExpectedEntries() {
        // d should contain some well-known ASCII-range characters
        // Check that we can look up at least some entries from d
        assertFalse(UnicodeParser.d.isEmpty());
        // The table should map SOME printable text
        boolean hasPrintable = UnicodeParser.d.keySet().stream()
                .anyMatch(k -> k.length() >= 1 && !k.startsWith("\\x"));
        assertTrue(hasPrintable, "d map should have some non-escaped text entries");
    }
}
