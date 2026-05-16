package com.uprfvx.random.gui;

import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GUIDropdownTest {

    @Test
    public void dropdownSpeciesSkipNullEntries() {
        Species bulbasaur = species(1, "Bulbasaur");
        Species charmander = species(4, "Charmander");

        List<Species> dropdownSpecies = DropdownSpeciesHelper.getDropdownSpecies(
                Arrays.asList(bulbasaur, null, charmander), false);

        assertEquals(List.of(bulbasaur, charmander), dropdownSpecies);
        assertArrayEquals(new String[] {"Random", "Bulbasaur", "Charmander"},
                DropdownSpeciesHelper.getDropdownSpeciesNames(dropdownSpecies));
    }

    @Test
    public void dropdownSpeciesSkipCosmeticReplacementsWhenRequested() {
        Species base = species(25, "Pikachu");
        base.setCosmeticForms(1);
        Species cosmetic = species(25, "Pikachu");
        cosmetic.setBaseForme(base);
        cosmetic.setFormeNumber(1);
        cosmetic.setFormeSuffix("-Cosmetic");
        Species regular = species(26, "Raichu");

        List<Species> dropdownSpecies = DropdownSpeciesHelper.getDropdownSpecies(
                Arrays.asList(base, null, cosmetic, regular), true);

        assertEquals(List.of(base, regular), dropdownSpecies);
        assertFalse(dropdownSpecies.stream().anyMatch(species -> species == cosmetic));
        assertArrayEquals(new String[] {"Random", "Pikachu", "Raichu"},
                DropdownSpeciesHelper.getDropdownSpeciesNames(dropdownSpecies));
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }
}
