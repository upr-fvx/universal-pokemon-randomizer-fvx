package test.com.dabomstew.pkrandom.cli;

import com.dabomstew.pkrandom.cli.CliRandomizer;
import com.dabomstew.pkromio.RootPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CliRandomizerTest {

    static final String SOURCE_PATH = RootPath.path + "/test/roms/Blue (U).gb";
    static final String OUT_PATH = RootPath.path + "/out/cli_test_out.gb";
    static final String SETTINGS_PATH = RootPath.path + "/test/resources/settings/cli_clean.rnqs";
    static final String SETTINGS_PATH_INVALID = RootPath.path + "/test/resources/settings/cli_invalid.rnqs";

    static final String SETTINGS_STRING = "416ACOBAQQEAAcAAQAEAAEeCQARAQEUAAAUAEAEAAEA/////wAAAAAyBDIBAAgJMgkCADIAAgABAAEBAAAAAAAJAAABEFBva2Vtb24gQmx1ZSAoVSkobx8q48M4ig==";
    static final String SETTINGS_STRING_INVALID = "ö416ACOBAQQEAAcAAQAEAAEeCQARAQEUAAAUAEAEAAEA/////wAAAAAyBDIBAAgJMgkCADIAAgABAAEBAAAAAAAJAAABEFBva2Vtb24gQmx1ZSAoVSkobx8q48M4ig==";


    @Test
    public void invoke_noSourcePathGiven_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-o", OUT_PATH,
                "-s", SETTINGS_PATH
        }));
    }

    @Test
    public void invoke_noOutputPathGiven_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-s", SETTINGS_PATH
        }));
    }

    @Test
    public void invoke_cannotReadSourcePath_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", RootPath.path + "NON_EXISTENT.gb",
                "-o", OUT_PATH,
                "-s", SETTINGS_PATH
        }));
    }

    @Test
    public void invoke_cannotWriteOutputPathParentFolder_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", RootPath.path + "/NON_EXISTENT/cli_test_out.gb",
                "-s", SETTINGS_PATH
        }));
    }

    @Test
    public void invoke_noSettingsGiven_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
        }));
    }

    @Test
    public void invoke_cannotOpenSettingsPath_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-s", RootPath.path + "/NON_EXISTENT/cli_clean.rnqs"
        }));
    }

    @Test
    public void invoke_invalidSettingsFile_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-s", SETTINGS_PATH_INVALID
        }));
    }

    @Test
    public void invoke_invalidSettingsString_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-S", SETTINGS_STRING_INVALID
        }));
    }

    @Test
    public void invoke_seedIsNotLong_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-s", SETTINGS_PATH,
                "-z", "NOT-A-LONG"
        }));
    }

    @Test
    public void invoke_invalidCPGType_fails() {
        assertEquals(1, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-s", SETTINGS_PATH,
                "-c", "Red", "NOT-A-CPG-TYPE"
        }));
    }

    @Test
    public void invoke_withSettingsPath_succeeds() {
        assertEquals(0, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-s", SETTINGS_PATH,
        }));
    }

    @Test
    public void invoke_withSettingsString_succeeds() {
        assertEquals(0, CliRandomizer.invoke(new String[] {
                "-i", SOURCE_PATH,
                "-o", OUT_PATH,
                "-S", SETTINGS_STRING,
        }));
    }

}
