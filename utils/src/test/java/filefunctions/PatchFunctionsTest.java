package filefunctions;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for PatchFunctions.
 *
 * applyPatch(byte[], String) loads an IPS patch from the classpath, then
 * applies it to the supplied byte array. Since test resources on the classpath
 * are not easily injectable, we test:
 *
 *  1. Missing resource → throws IOException with a descriptive message.
 *  2. The private helper methods are validated indirectly through the public
 *     applyPatch behaviour (the method throws on non-existent resource paths).
 *
 * BEHAVIOR QUIRK: getPatchFile uses FileFunctions.class.getResourceAsStream,
 * so the resource path must be absolute from the classpath root (starting with
 * '/'). If the resource is absent, an IOException is thrown with the message
 * "Could not read patch resource at: <path>".
 */
public class PatchFunctionsTest {

    @Test
    public void applyPatch_throwsIOExceptionOnMissingResource() {
        byte[] rom = new byte[100];
        IOException ex = assertThrows(IOException.class,
                () -> PatchFunctions.applyPatch(rom, "/nonexistent/patch.ips"),
                "Should throw IOException when the patch resource does not exist");
        assertTrue(ex.getMessage().contains("Could not read patch resource"),
                "Exception message should mention the missing resource");
    }

    @Test
    public void applyPatch_throwsIOExceptionForAnotherMissingPath() {
        // Characterize with a different missing path to confirm the path is included
        byte[] rom = new byte[50];
        IOException ex = assertThrows(IOException.class,
                () -> PatchFunctions.applyPatch(rom, "/patches/gen1_fix.ips"));
        assertTrue(ex.getMessage().contains("/patches/gen1_fix.ips"),
                "Exception message should include the requested path");
    }
}
