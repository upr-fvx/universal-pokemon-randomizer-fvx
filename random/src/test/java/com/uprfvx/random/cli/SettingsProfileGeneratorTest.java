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
    public void invoke_withFoeModeOverlays_setsTrainerModes() throws Exception {
        assertEquals(Settings.TrainersMod.RANDOM, settingsForOverlay("MODE-FOE-RANDOM").getTrainersMod());
        assertEquals(Settings.TrainersMod.DISTRIBUTED,
                settingsForOverlay("MODE-FOE-EVEN-DISTRIBUTION").getTrainersMod());
        assertEquals(Settings.TrainersMod.MAINPLAYTHROUGH,
                settingsForOverlay("MODE-FOE-MAIN-PLAYTHROUGH").getTrainersMod());
        assertEquals(Settings.TrainersMod.TYPE_THEMED, settingsForOverlay("MODE-FOE-TYPE-THEMED").getTrainersMod());
        assertEquals(Settings.TrainersMod.KEEP_THEMED, settingsForOverlay("MODE-FOE-KEEP-THEMED").getTrainersMod());
    }

    @Test
    public void invoke_withWildModeOverlays_setsLocationModes() throws Exception {
        Settings encounterSet = settingsForOverlay("MODE-WILD-ENCOUNTER-SET");
        assertTrue(encounterSet.isRandomizeWildPokemon());
        assertEquals(Settings.WildPokemonZoneMod.ENCOUNTER_SET, encounterSet.getWildPokemonZoneMod());

        Settings map = settingsForOverlay("MODE-WILD-MAP");
        assertTrue(map.isRandomizeWildPokemon());
        assertEquals(Settings.WildPokemonZoneMod.MAP, map.getWildPokemonZoneMod());

        Settings namedLocation = settingsForOverlay("MODE-WILD-NAMED-LOCATION");
        assertTrue(namedLocation.isRandomizeWildPokemon());
        assertEquals(Settings.WildPokemonZoneMod.NAMED_LOCATION, namedLocation.getWildPokemonZoneMod());

        Settings game = settingsForOverlay("MODE-WILD-GAME");
        assertTrue(game.isRandomizeWildPokemon());
        assertEquals(Settings.WildPokemonZoneMod.GAME, game.getWildPokemonZoneMod());

        Settings catchEmAll = settingsForOverlay("MODE-WILD-CATCH-EM-ALL");
        assertTrue(catchEmAll.isRandomizeWildPokemon());
        assertTrue(catchEmAll.isCatchEmAllEncounters());
    }

    @Test
    public void invoke_withTypeEffectivenessModeOverlays_setsTypeModes() throws Exception {
        assertEquals(Settings.TypeEffectivenessMod.RANDOM,
                settingsForOverlay("MODE-TYPE-RANDOM").getTypeEffectivenessMod());
        assertEquals(Settings.TypeEffectivenessMod.RANDOM_BALANCED,
                settingsForOverlay("MODE-TYPE-RANDOM-BALANCED").getTypeEffectivenessMod());
        assertEquals(Settings.TypeEffectivenessMod.KEEP_IDENTITIES,
                settingsForOverlay("MODE-TYPE-KEEP-IDENTITIES").getTypeEffectivenessMod());
        assertEquals(Settings.TypeEffectivenessMod.INVERSE,
                settingsForOverlay("MODE-TYPE-INVERSE").getTypeEffectivenessMod());
    }

    @Test
    public void invoke_withIntroModeOverlays_setsIntroFields() throws Exception {
        assertTrue(settingsForOverlay("MODE-INTRO-RANDOM").isRandomizeIntroMon());
        assertFalse(settingsForOverlay("MODE-NO-RANDOM-INTRO").isRandomizeIntroMon());
        assertFalse(settingsForOverlay("FVX-GEN-003").isRandomizeIntroMon());
    }

    @Test
    public void invoke_withTrainerClassSpriteSyncOverlay_setsOptInFlag() throws Exception {
        assertTrue(settingsForOverlay("MODE-TRAINER-CLASS-SPRITE-SYNC").isRandomizeTrainerClassSprites());
    }

    @Test
    public void invoke_withIntroModeOverlays_appliesLastOverlayInSettingsSerializationOrder() throws Exception {
        assertFalse(settingsForOverlays("MODE-INTRO-RANDOM", "MODE-NO-RANDOM-INTRO").isRandomizeIntroMon());
        assertTrue(settingsForOverlays("MODE-NO-RANDOM-INTRO", "MODE-INTRO-RANDOM").isRandomizeIntroMon());
    }

    @Test
    public void invoke_withVariantProfiles_appliesRepresentativeOverlay() throws Exception {
        assertEquals(Settings.TrainersMod.RANDOM, settingsForProfile("foe_mode_variants").getTrainersMod());
        assertEquals(Settings.WildPokemonZoneMod.ENCOUNTER_SET,
                settingsForProfile("wild_location_variants").getWildPokemonZoneMod());
        Settings generalRestrictions = settingsForProfile("general_restrictions_variants");
        assertTrue(generalRestrictions.isLimitPokemon());
        assertFalse(generalRestrictions.isRandomizeIntroMon());
        assertEquals(Settings.TypeEffectivenessMod.RANDOM,
                settingsForProfile("type_effectiveness_exact_variants").getTypeEffectivenessMod());
    }

    @Test
    public void invoke_withListFlags_succeedsWithoutSettingsFiles() {
        assertEquals(0, SettingsProfileGenerator.invoke(new String[]{"--list-features"}));
        assertEquals(0, SettingsProfileGenerator.invoke(new String[]{"--list-profiles"}));
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

    @Test
    public void invoke_withUnsupportedModeOverlay_failsAndDoesNotWriteOutput() {
        Path output = tempDir.resolve("unsupported-mode.rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--enable", "MODE-GEN-LIMIT-1-9-NO-GMAX"
        });

        assertEquals(1, exitCode);
        assertFalse(output.toFile().exists());
    }

    @Test
    public void invoke_withUnsupportedGenRestrictionOverlays_failAndDoNotWriteOutput() {
        assertUnsupportedOverlay("MODE-GEN-LIMIT-1-9");
        assertUnsupportedOverlay("MODE-GEN-LIMIT-1-9-NO-RELATIVES");
        assertUnsupportedOverlay("MODE-GEN-LIMIT-1-9-NO-MEGAS");
    }

    @Test
    public void invoke_withUnknownOverlay_failsAndDoesNotWriteOutput() {
        Path output = tempDir.resolve("unknown.rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--enable", "MODE-DOES-NOT-EXIST"
        });

        assertEquals(1, exitCode);
        assertFalse(output.toFile().exists());
    }

    private Settings settingsForOverlay(String overlayId) throws Exception {
        Path output = tempDir.resolve(overlayId + ".rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--enable", overlayId
        });

        assertEquals(0, exitCode);
        return readSettings(output);
    }

    private Settings settingsForOverlays(String... overlayIds) throws Exception {
        Path output = tempDir.resolve(String.join("-", overlayIds) + ".rnqs");
        String[] args = new String[4 + overlayIds.length * 2];
        args[0] = "--base-settings";
        args[1] = BASE_SETTINGS.toString();
        args[2] = "--output-settings";
        args[3] = output.toString();
        for (int i = 0; i < overlayIds.length; i++) {
            args[4 + i * 2] = "--enable";
            args[5 + i * 2] = overlayIds[i];
        }

        int exitCode = SettingsProfileGenerator.invoke(args);

        assertEquals(0, exitCode);
        return readSettings(output);
    }

    private Settings settingsForProfile(String profileId) throws Exception {
        Path output = tempDir.resolve(profileId + ".rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--profile", profileId
        });

        assertEquals(0, exitCode);
        return readSettings(output);
    }

    private void assertUnsupportedOverlay(String overlayId) {
        Path output = tempDir.resolve(overlayId + "-unsupported.rnqs");

        int exitCode = SettingsProfileGenerator.invoke(new String[]{
                "--base-settings", BASE_SETTINGS.toString(),
                "--output-settings", output.toString(),
                "--enable", overlayId
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
