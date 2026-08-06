package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.function.Predicate;

public class TypeOrRandomSettingDefinition extends SettingDefinition<Integer> {
    // If Species id were an enum, this could share code with that class
    // Am not suggesting it should become that, just an idle thought
    public static final int RANDOM_TYPE = -1;

    public TypeOrRandomSettingDefinition(String name, String category, 
                                         SettingRestriction prerequisite, 
                                         Predicate<RomHandler> supported) {
        super(name, category, RANDOM_TYPE, prerequisite, supported, true, null);
    }

    @Override
    public boolean isValueValid(Integer value) {
        if (value == null)
            return false;

        return value == RANDOM_TYPE || (value >= 0 && value < Type.values().length);
    }

    @Override
    public boolean isValueEnabled(Integer value, SettingsManager manager) {
        return true;
    }

    @Override
    public boolean isValueSupported(Integer value, RomHandler game) {
        if (value == null) {
            return false;
        }
        if (value == RANDOM_TYPE) {
            return true;
        }
        if (value < 0 || value >= Type.values().length) {
            return false;
        }
        return game.getTypeService().typeInGame(Type.values()[value]);
    }
}
