package com.uprfvx.random.cli;

import com.uprfvx.random.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SettingsProfileGeneratorTest {

    private static final Path BASE_SETTINGS = Paths.get("src/test/resources/settings/cli_clean.rnqs");

    @TempDir
    Path tempDir;

    @Test
    public void invoke_withFeatureIds_writesSettingsWithoutRom() throws Exception {
        Path output = tempDir.resolve("generated.rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--enable", "FVX-TRAIT-016",
                "--enable", "FVX-TRAIT-018"
        });

        assertEquals(0, exitCode);
        Settings settings = readSettings(output);
        assertEquals(Settings.EvolutionsMod.RANDOM, settings.getEvolutionsMod());
        assertTrue(settings.isEvosSimilarStrength());
    }

    @Test
    public void invoke_withProfile_appliesProfileOverlay() throws Exception {
        Path output = tempDir.resolve("moves.rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--profile", "03_moves_movesets_full"
        });

        assertEquals(0, exitCode);
        Settings settings = readSettings(output);
        assertTrue(settings.isRandomizeMovePowers());
        assertTrue(settings.isRandomizeMoveNames());
        assertEquals(Settings.MovesetsMod.COMPLETELY_RANDOM, settings.getMovesetsMod());
        assertTrue(settings.isStartWithGuaranteedMoves());
        assertTrue(settings.isBlockBrokenMovesetMoves());
    }

    @Test
    public void invoke_withUnsupportedFeature_failsAndDoesNotWriteOutput() {
        Path output = tempDir.resolve("unsupported.rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--enable", "FVX-GFX-005"
        });

        assertEquals(1, exitCode);
        assertFalse(output.toFile().exists());
    }

    private static Settings readSettings(Path path) throws Exception {
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            return Settings.readFromFileFormat(in);
        }
    }
}
