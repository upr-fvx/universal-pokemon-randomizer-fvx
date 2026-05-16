package com.uprfvx.random.gui;

import com.uprfvx.romio.gamedata.Species;

import java.util.List;
import java.util.Objects;

final class DropdownSpeciesHelper {

    private DropdownSpeciesHelper() {
    }

    static List<Species> getDropdownSpecies(List<Species> speciesList, boolean skipCosmeticReplacements) {
        return speciesList.stream()
                .filter(Objects::nonNull)
                .filter(pk -> !skipCosmeticReplacements || !pk.isCosmeticReplacement())
                .toList();
    }

    static String[] getDropdownSpeciesNames(List<Species> speciesList) {
        String[] pokeNames = new String[speciesList.size() + 1];
        pokeNames[0] = "Random";
        for (int i = 0; i < speciesList.size(); i++) {
            pokeNames[i + 1] = speciesList.get(i).getFullName();
        }
        return pokeNames;
    }
}
