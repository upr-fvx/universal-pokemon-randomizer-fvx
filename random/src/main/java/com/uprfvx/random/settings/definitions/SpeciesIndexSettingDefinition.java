package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.constants.Gen7Constants;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.List;
import java.util.function.Predicate;

/**
 * A SettingDefinition for Species, through their species IDs.<br>
 * The purpose of this class is to allow the custom starter selection
 * that the Randomizer currently has, so it is designed with that in mind.
 * ID=0 is taken to mean "Random".
 */
public class SpeciesIndexSettingDefinition extends SettingDefinition<Integer> {

    public static final int RANDOM_SPECIES = 0;
    private static final int HIGHEST_SPECIES_INDEX = Gen7Constants.getPokemonCount(Gen7Constants.Type_USUM) +
            Gen7Constants.getFormeCount(Gen7Constants.Type_USUM); //This is clunky and possibly inaccurate. TODO: fix.

    public SpeciesIndexSettingDefinition(String name, String category,
                                         SettingRestriction prerequisite,
                                         Predicate<RomHandler> supported) {
        super(name, category, RANDOM_SPECIES, prerequisite, supported, true, null);
    }

    public boolean isValueValid(Integer value) {
        if (value == null)
            return false;

        return value >= 0 && value <= HIGHEST_SPECIES_INDEX;
    }

    @Override
    public boolean isValueEnabled(Integer value, SettingsManager manager) {
        return true;
    }

    @Override
    public boolean isValueSupported(Integer value, RomHandler game) {
        if (value < 0) {
            return false;
        }
        if (value == RANDOM_SPECIES) {
            return true;
        }
        List<Species> allSpecies = game.getSpecies();
        if (value >= allSpecies.size()) {
            return false;
        }
        Species spec = allSpecies.get(value);
        if (spec == null) {
            return false;
        }
        return !spec.isEssentiallyCosmetic();
    }
}
