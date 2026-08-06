package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.function.Predicate;

public class TypeOrRandomSettingDefinition extends NumericSettingDefinition<Integer> {
    // If Species id were an enum, this could share code with that class
    // Am not suggesting it should become that, just an idle thought
    public static final int RANDOM_TYPE = -1;

    //TODO: generalize to EnumPlusOptionsSettingDefinition?
    //(Options being "Random", "Unchanged", and anything else that ends up relevant.)
    //(Saw two settings that could be combined with such a class, but I'd sooner see the first extended TBH)

    public TypeOrRandomSettingDefinition(String name, String category, 
                                         SettingRestriction prerequisite, 
                                         Predicate<RomHandler> supported) {
        super(name, category, RANDOM_TYPE, prerequisite, supported, RANDOM_TYPE, Type.SIZE);
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
