package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
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
public class SpeciesSettingDefinition extends SettingDefinition<Integer> {

    public static final int RANDOM_SPECIES = 0;

    public SpeciesSettingDefinition(String name, String category,
                                    SettingRestriction prerequisite,
                                    Predicate<RomHandler> supported) {
        super(name, category, RANDOM_SPECIES, prerequisite, supported, false, null);
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
