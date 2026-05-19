package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.graphics.palettes.PaletteDescription;
import com.uprfvx.romio.graphics.palettes.PalettePartDescription;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3to5PaletteBoundsTest {

    @Test
    public void Gen3to5PaletteRandomizer_usesAvailableDescriptionWhenSpeciesIsInRange() {
        Gen3to5PaletteRandomizer randomizer = randomizer();
        Species species = new Species(1);
        List<PaletteDescription> descriptions = List.of(new PaletteDescription("Bulbasaur [2,3,4]"));

        PalettePartDescription[] parts = randomizer.getPalettePartDescriptions(species, descriptions);

        assertFalse(parts[0].isBlank());
    }

    @Test
    public void Gen3to5PaletteRandomizer_defaultsMissingExpandedSpeciesDescriptionToBlank() {
        Gen3to5PaletteRandomizer randomizer = randomizer();
        Species expandedSpecies = new Species(388);
        List<PaletteDescription> descriptions = descriptions(387);

        PalettePartDescription[] parts = randomizer.getPalettePartDescriptions(expandedSpecies, descriptions);

        assertTrue(parts[0].isBlank());
    }

    @Test
    public void Gen3to5PaletteRandomizer_defaultsInvalidSpeciesNumberToBlank() {
        Gen3to5PaletteRandomizer randomizer = randomizer();
        Species invalidSpecies = new Species(0);
        List<PaletteDescription> descriptions = List.of(new PaletteDescription("Bulbasaur [2,3,4]"));

        PalettePartDescription[] parts = randomizer.getPalettePartDescriptions(invalidSpecies, descriptions);

        assertTrue(parts[0].isBlank());
    }

    private static Gen3to5PaletteRandomizer randomizer() {
        RomHandler romHandler = (RomHandler) Proxy.newProxyInstance(
                RomHandler.class.getClassLoader(),
                new Class[]{RomHandler.class},
                (proxy, method, args) -> {
                    if ("getPaletteFilesID".equals(method.getName())) {
                        return "FRLG";
                    }
                    if ("getRestrictedSpeciesService".equals(method.getName()) ||
                            "getTypeService".equals(method.getName())) {
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        return new Gen3to5PaletteRandomizer(romHandler, new Settings(), new Random(1));
    }

    private static List<PaletteDescription> descriptions(int count) {
        List<PaletteDescription> descriptions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            descriptions.add(new PaletteDescription("Species" + (i + 1) + " [2,3,4]"));
        }
        return descriptions;
    }
}
