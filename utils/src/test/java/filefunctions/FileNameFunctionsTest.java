package filefunctions;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for FileNameFunctions.fixFilename.
 *
 * Documented behavior:
 *  - If file has no extension → add defaultExtension
 *  - If file has a banned extension → replace with defaultExtension
 *  - Otherwise → leave as is (but if it doesn't end with defaultExtension, still adds it)
 *
 * BEHAVIOR QUIRK: the "leave as is" branch only applies when the existing
 * extension IS the defaultExtension (since the final check is
 * `!absolutePath.endsWith("." + defaultExtension)`). So if a file ends with
 * ".zip" and the default is "rom" and "zip" is not banned, the method still
 * appends ".rom" — resulting in a name like "file.zip.rom". We characterize this.
 */
public class FileNameFunctionsTest {

    // --- No extension: default extension is added ---

    @Test
    public void noExtension_addsDefaultExtension() {
        File f = new File("/tmp/myrom");
        File result = FileNameFunctions.fixFilename(f, "rom");
        assertTrue(result.getAbsolutePath().endsWith(".rom"));
    }

    // --- File already has default extension: no change ---

    @Test
    public void alreadyHasDefaultExtension_noChange() {
        File f = new File("/tmp/myrom.rom");
        File result = FileNameFunctions.fixFilename(f, "rom");
        assertEquals(f.getAbsolutePath(), result.getAbsolutePath());
    }

    // --- Banned extension: replaced with default ---

    @Test
    public void bannedExtension_replacedWithDefault() {
        File f = new File("/tmp/myrom.nds");
        List<String> banned = Arrays.asList("nds", "gba");
        File result = FileNameFunctions.fixFilename(f, "rom", banned);
        assertTrue(result.getAbsolutePath().endsWith(".rom"));
        assertFalse(result.getAbsolutePath().endsWith(".nds"));
    }

    @Test
    public void bannedExtensionSecondInList_replacedWithDefault() {
        File f = new File("/tmp/myrom.gba");
        List<String> banned = Arrays.asList("nds", "gba");
        File result = FileNameFunctions.fixFilename(f, "rom", banned);
        assertTrue(result.getAbsolutePath().endsWith(".rom"));
    }

    // --- Non-banned, non-default extension: default is APPENDED (behavior quirk) ---

    @Test
    public void nonBannedNonDefaultExtension_defaultIsAppended() {
        // BEHAVIOR QUIRK: "zip" is not banned and not "rom",
        // so the file gets ".rom" appended → "file.zip.rom"
        File f = new File("/tmp/archive.zip");
        File result = FileNameFunctions.fixFilename(f, "rom");
        assertTrue(result.getAbsolutePath().endsWith(".rom"),
                "default extension should be appended even if a different extension is present");
    }

    // --- Empty banned list: same as no-banned-list overload ---

    @Test
    public void emptyBannedList_sameAsNoBannedOverload() {
        File f = new File("/tmp/myfile");
        File r1 = FileNameFunctions.fixFilename(f, "gba");
        File r2 = FileNameFunctions.fixFilename(f, "gba", Arrays.asList());
        assertEquals(r1.getAbsolutePath(), r2.getAbsolutePath());
    }

    // --- Multiple banned extensions but file has none of them ---

    @Test
    public void noBannedExtensionMatch_defaultAppendedIfMissing() {
        File f = new File("/tmp/myfile.txt");
        List<String> banned = Arrays.asList("nds", "gba");
        File result = FileNameFunctions.fixFilename(f, "rom", banned);
        // "txt" not banned, but doesn't end with ".rom" → ".rom" is appended
        assertTrue(result.getAbsolutePath().endsWith(".rom"));
    }

    // --- File with dot in directory path but no extension in filename ---

    @Test
    public void dotInDirectory_noExtensionInFilename_addsDefault() {
        File f = new File("/tmp/my.dir/myfile");
        File result = FileNameFunctions.fixFilename(f, "rom");
        assertTrue(result.getAbsolutePath().endsWith(".rom"));
    }
}
