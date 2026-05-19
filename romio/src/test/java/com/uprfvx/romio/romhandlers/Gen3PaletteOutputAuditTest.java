package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3PaletteOutputAuditTest {

    @Test
    public void paletteOutputAuditDigestUsesPaletteBytes() {
        String digestA = Gen3RomHandler.digestPaletteBytesForDiagnostics(new byte[]{1, 2, 3, 4});
        String digestB = Gen3RomHandler.digestPaletteBytesForDiagnostics(new byte[]{1, 2, 3, 5});

        assertEquals(digestA, Gen3RomHandler.digestPaletteBytesForDiagnostics(new byte[]{1, 2, 3, 4}));
        assertNotEquals(digestA, digestB);
        assertEquals("<missing>", Gen3RomHandler.digestPaletteBytesForDiagnostics(null));
    }

    @Test
    public void paletteOutputAuditSummarizesChangedAndUnchangedPalettes() {
        Gen3RomHandler.Gen3PaletteAuditSnapshot baseCharmander =
                snapshot(4, 4, "Charmander", 0x100, 0x200, "normal-a", "shiny-a");
        Gen3RomHandler.Gen3PaletteAuditSnapshot baseSquirtle =
                snapshot(7, 7, "Squirtle", 0x300, 0x400, "normal-b", "shiny-b");
        Gen3RomHandler.Gen3PaletteAuditSnapshot baseCaterpie =
                snapshot(10, 10, "Caterpie", 0x500, 0x600, "normal-c", "shiny-c");

        Gen3RomHandler.Gen3PaletteAuditSnapshot outputCharmander =
                snapshot(4, 4, "Charmander", 0x110, 0x210, "normal-a", "shiny-z");
        Gen3RomHandler.Gen3PaletteAuditSnapshot outputSquirtle =
                snapshot(7, 7, "Squirtle", 0x310, 0x410, "normal-z", "shiny-b");
        Gen3RomHandler.Gen3PaletteAuditSnapshot outputCaterpie =
                snapshot(10, 10, "Caterpie", 0x510, 0x610, "normal-c", "shiny-c");

        Gen3RomHandler.Gen3PaletteOutputAuditReport report = Gen3RomHandler.buildGen3PaletteOutputAudit(
                List.of(baseCharmander, baseSquirtle, baseCaterpie),
                List.of(outputCharmander, outputSquirtle, outputCaterpie));

        assertEquals(3, report.summary().sampledCount());
        assertEquals(1, report.summary().normalChangedCount());
        assertEquals(1, report.summary().shinyChangedCount());
        assertEquals(1, report.summary().unchangedCount());

        Gen3RomHandler.Gen3PaletteOutputAuditRow charmander = report.rows().get(0);
        assertEquals(4, charmander.speciesId());
        assertEquals("Charmander", charmander.decodedSpecies());
        assertEquals(0x100, charmander.baseNormalPalettePointer());
        assertEquals(0x110, charmander.outputNormalPalettePointer());
        assertFalse(charmander.normalChangedFromBase());
        assertTrue(charmander.shinyChangedFromBase());

        Gen3RomHandler.Gen3PaletteOutputAuditRow squirtle = report.rows().get(1);
        assertTrue(squirtle.normalChangedFromBase());
        assertFalse(squirtle.shinyChangedFromBase());

        Gen3RomHandler.Gen3PaletteOutputAuditRow caterpie = report.rows().get(2);
        assertFalse(caterpie.normalChangedFromBase());
        assertFalse(caterpie.shinyChangedFromBase());
    }

    @Test
    public void paletteOutputAuditTreatsMissingOutputSpeciesAsChanged() {
        Gen3RomHandler.Gen3PaletteOutputAuditReport report = Gen3RomHandler.buildGen3PaletteOutputAudit(
                List.of(snapshot(25, 25, "Pikachu", 0x700, 0x800, "normal", "shiny")),
                List.of());

        assertEquals(1, report.summary().sampledCount());
        assertEquals(1, report.summary().normalChangedCount());
        assertEquals(1, report.summary().shinyChangedCount());
        assertEquals(0, report.summary().unchangedCount());

        Gen3RomHandler.Gen3PaletteOutputAuditRow row = report.rows().getFirst();
        assertEquals("<missing>", row.outputNormalPaletteDigest());
        assertEquals("<missing>", row.outputShinyPaletteDigest());
        assertTrue(row.normalChangedFromBase());
        assertTrue(row.shinyChangedFromBase());
    }

    private static Gen3RomHandler.Gen3PaletteAuditSnapshot snapshot(int speciesId, int identityNumber,
                                                                    String decodedSpecies,
                                                                    int normalPointer, int shinyPointer,
                                                                    String normalDigest, String shinyDigest) {
        return new Gen3RomHandler.Gen3PaletteAuditSnapshot(speciesId, identityNumber, decodedSpecies,
                normalPointer, shinyPointer, normalDigest, shinyDigest);
    }
}
