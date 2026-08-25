package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.graphics.palettes.Palette;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class PaletteRandomizerTest extends RandomizerTest {
    // Most of the outcome of the PaletteRomHandler would be difficult
    // to automatically test, "does a palette look bad" is not measurable
    // without training a custom AI. So we mostly just check for throwing here.

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePalettes_NoExtraOptions_DoesNotThrow(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasPokemonPaletteSupport());

        SettingsManager s = new SettingsManager();
        s.setSetting(Settings.Name.RANDOMIZE_SPECIES_PALETTES, Settings.SpeciesPalettesMod.RANDOM);

        createPaletteRandomizer(romHandler, s).randomizePokemonPalettes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePalettes_PalettesFollowTypes_DoesNotThrow(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasPokemonPaletteSupport());

        SettingsManager s = new SettingsManager();
        s.setSetting(Settings.Name.RANDOMIZE_SPECIES_PALETTES, Settings.SpeciesPalettesMod.RANDOM);
        s.setSetting(Settings.Name.PALETTES_FOLLOW_TYPES, true);

        createPaletteRandomizer(romHandler, s).randomizePokemonPalettes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePalettes_PalettesFollowEvolutions_DoesNotThrow(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasPokemonPaletteSupport());

        SettingsManager s = new SettingsManager();
        s.setSetting(Settings.Name.RANDOMIZE_SPECIES_PALETTES, Settings.SpeciesPalettesMod.RANDOM);
        s.setSetting(Settings.Name.PALETTES_FOLLOW_EVOLUTIONS, true);

        createPaletteRandomizer(romHandler, s).randomizePokemonPalettes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePalettes_ShinyFromNormal_NewShinyIsOldNormalPalette(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasPokemonPaletteSupport());
        assumeTrue(getGenerationNumberOf(romName) >= 2);

        Map<Species, Palette> normalPalsBefore = new HashMap<>();
        // no palette form support expected yet
        romHandler.getSpeciesSet().forEach(
                pk -> normalPalsBefore.put(pk, new Palette(pk.getNormalPalette()))
        );

        SettingsManager s = new SettingsManager();
        s.setSetting(Settings.Name.RANDOMIZE_SPECIES_PALETTES, Settings.SpeciesPalettesMod.RANDOM);
        s.setSetting(Settings.Name.PALETTES_SHINY_FROM_NORMAL, true);

        createPaletteRandomizer(romHandler, s).randomizePokemonPalettes();

        for (Species pk : romHandler.getSpeciesSet()) {
            assertEquals(normalPalsBefore.get(pk), pk.getShinyPalette());
        }
    }


}
