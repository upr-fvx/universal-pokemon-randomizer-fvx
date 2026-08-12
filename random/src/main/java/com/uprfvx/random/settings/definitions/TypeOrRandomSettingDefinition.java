package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link SettingDefinition} for {@link Type}s, through their enum id.
 * It allows for choosing "Random" in addition to any Type that is present
 * in the given {@link RomHandler}.
 */
public class TypeOrRandomSettingDefinition extends NumericSettingDefinition<Integer> {
    // If Species id were an enum, this could share code with that class
    // Am not suggesting it should become that, just an idle thought
    public static final int RANDOM_TYPE = -1;

    //TODO: generalize to EnumPlusOptionsSettingDefinition?
    //(Options being "Random", "Unchanged", and anything else that ends up relevant.)
    //(Saw a pair of settings (Standardize EXP curves) that could be combined with such a class,
    // but in this case I'd honestly rather see the enabler extended.)

    public static class Builder<B extends Builder<B>> extends NumericSettingDefinition.Builder<B, Integer> {

        public Builder(Settings.Name name, Settings.Category category) {
            super(name, category, RANDOM_TYPE, RANDOM_TYPE, Type.SIZE);
        }

        @Override
        public B restrictedMinimums(List<Pair<Integer, SettingRestriction>> restrictedMinimums) {
            throw new UnsupportedOperationException();
        }

        @Override
        public B restrictedMaximums(List<Pair<Integer, SettingRestriction>> restrictedMaximums) {
            throw new UnsupportedOperationException();
        }

        @Override
        public B supportedMinimums(Function<RomHandler, Integer> supportedMinimums) {
            throw new UnsupportedOperationException();
        }

        @Override
        public B supportedMaximums(Function<RomHandler, Integer> supportedMaximums) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeOrRandomSettingDefinition build() {
            return new TypeOrRandomSettingDefinition(
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

    protected TypeOrRandomSettingDefinition(Settings.Name name, Settings.Category category,
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

    @Override
    public boolean isValueSupported(Integer value, RomHandler game) {
        if (!isValueValid(value)) {
            return false;
        }
        if (value == RANDOM_TYPE) {
            return true;
        }
        if (game == null) {
            return true;
        }
        return game.getTypeService().typeInGame(Type.values()[value]);
    }
}
