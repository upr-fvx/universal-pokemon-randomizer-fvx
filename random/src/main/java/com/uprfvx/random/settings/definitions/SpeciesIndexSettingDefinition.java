package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.constants.Gen7Constants;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A SettingDefinition for Species, through their species IDs.<br>
 * The purpose of this class is to allow the custom starter selection
 * that the Randomizer currently has, so it is designed with that in mind.
 * ID=0 is taken to mean "Random".
 */
public class SpeciesIndexSettingDefinition extends NumericSettingDefinition<Integer> {

    public static final int RANDOM_SPECIES = 0;
    private static final int HIGHEST_SPECIES_INDEX = Gen7Constants.getPokemonCount(Gen7Constants.Type_USUM) +
            Gen7Constants.getFormeCount(Gen7Constants.Type_USUM); //This is clunky and possibly inaccurate. TODO: fix.

    public static class Builder<B extends Builder<B>> extends NumericSettingDefinition.Builder<B, Integer> {

        public Builder(Settings.Name name, Settings.Category category) {
            super(name, category, RANDOM_SPECIES, RANDOM_SPECIES, HIGHEST_SPECIES_INDEX);
        }

        @Override
        public SpeciesIndexSettingDefinition build() {
            return new SpeciesIndexSettingDefinition(
                    name, category,
                    defaultValue,
                    prerequisite, supported,
                    variableDefaultValue,
                    minimum, maximum,
                    restrictedMinimums, restrictedMaximums,
                    supportedMinimums, supportedMaximums
            );
        }
    }

    protected SpeciesIndexSettingDefinition(Settings.Name name, Settings.Category category,
                                            Integer defaultValue,
                                            SettingRestriction prerequisite, Predicate<RomHandler> supported,
                                            Function<RomHandler, Integer> variableDefaultValue,
                                            Integer minimum, Integer maximum,
                                            List<Pair<Integer, SettingRestriction>> restrictedMinimums,
                                            List<Pair<Integer, SettingRestriction>> restrictedMaximums,
                                            Function<RomHandler, Integer> supportedMinimums,
                                            Function<RomHandler, Integer> supportedMaximums) {
        super(name, category, defaultValue, prerequisite, supported, variableDefaultValue, minimum, maximum,
                restrictedMinimums, restrictedMaximums, supportedMinimums, supportedMaximums);
    }

    /*
     * Unless we can check for cosmetic formes here, this does nothing not handled by the super method
    @Override
    public boolean isValueValid(Integer value) {
        if (value == null)
            return false;

        //If possible, checking here for cosmetic forms would be nice. Unnecessary though.

        return super.isValueValid(value);
    }
    //*/

    @Override
    public boolean isValueSupported(Integer value, RomHandler game) {
        if (!isValueValid(value)) {
            return false;
        }
        if (value == RANDOM_SPECIES) {
            return true;
        }
        if (game == null) {
            return true;
            //Standard is for all (valid) values to be supported when no game is loaded.
            //(Does mean we'll need special handling for the unloaded case in the GUI.)
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
