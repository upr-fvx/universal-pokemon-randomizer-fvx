package com.uprfvx.random.settings;

import com.uprfvx.random.Version;
import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.romio.romhandlers.RomHandler;
import ini.IniEntry;
import ini.IniEntryReader;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.uprfvx.random.settings.Settings.ALL_SETTINGS;

// TODO: testing

/**
 * An {@link IniEntry} that holds the saved state of a {@link SettingsManager}.<br>
 * The functionality of this class comes in a pair: {@link #readFromString(String)} (and getters)
 * to retrieve the information held in a String, and {@link #createString(SettingsManager, RomHandler)}
 * to create said information to embed in a String.<br>
 * SettingsIniEntry stands out from other IniEntries in usage, since its string form is not expected
 * to live in a file by its own/with other IniEntries of the same class. Rather, the expectation is that
 * its string form will be embedded in bigger files also containing other information.
 */
class SettingsIniEntry extends IniEntry {
    private static final String SETTINGS_START = "[Settings]";
    private static final String SETTINGS_END = "[Settings_end]";
    private static final String VERSION_ID = "VersionID";
    private static final String ROM_NAME = "ROMName";
    private static final String SETTING = "Setting";

    private static final String ENTRY_REGEX = "\\" + SETTINGS_START + "[\\s\\S]*?\\" + SETTINGS_END;

    private static class SettingsIniEntryReader extends IniEntryReader<SettingsIniEntry> {

        protected SettingsIniEntryReader() {
            super(DefaultReadMode.INT); // does not matter
            putSpecialKeyMethod(VERSION_ID, SettingsIniEntry::setVersionID);
            putSpecialKeyMethod(ROM_NAME, SettingsIniEntry::setROMName);
            putKeyPrefixMethod(SETTING + "<", SettingsIniEntry::addSettingValue);
        }

        @Override
        protected SettingsIniEntry initiateEntry(String name) {
            return new SettingsIniEntry(name);
        }
    }

    /**
     * Reads any String, locates and parses the first string representation of a SettingsIniEntry.
     * @param s The input string.
     * @throws IllegalArgumentException if no Ini Entry could be found, or if it is misformatted.
     */
    public static SettingsIniEntry readFromString(String s) {
        Matcher matcher = Pattern.compile(ENTRY_REGEX).matcher(s);
        if (matcher.find()) {
            String entryBlock = matcher.group();
            SettingsIniEntry entry = new SettingsIniEntryReader().readFromString(entryBlock).getFirst();
            if (entry.getVersionID() == 0) {
                throw new IllegalArgumentException("Settings Ini Entry does not contain a VersionID");
            }
            if (Objects.equals(entry.getROMName(), "")) {
                throw new IllegalArgumentException("Settings Ini Entry does not contain a ROMName");
            }
            return entry;
        } else {
            throw new IllegalArgumentException("Could not find a readable Settings Ini Entry in the input String.");
        }
    }

    public static String createString(SettingsManager manager, RomHandler game) {
        StringBuilder sb = new StringBuilder(SETTINGS_START);
        sb.append("\n");
        sb.append(VERSION_ID).append("=").append(Version.LATEST.id).append("\n");
        sb.append(ROM_NAME).append("=").append(game == null ? "NONE" : game.getROMName()).append("\n");

        // assumes ALL_SETTINGS is being used to populate the states of the manager
        for (SettingDefinition<?> setting : ALL_SETTINGS) {
            Settings.Name name = setting.getName();
            if (!manager.isDefault(name)) {
                sb.append(SETTING);
                sb.append("<");
                sb.append(name);
                sb.append(">=");
                sb.append(manager.get(name).toString());
                sb.append("\n");
            }
        }
        sb.append(SETTINGS_END);
        sb.append("\n");
        return sb.toString();
    }

    private final Map<Settings.Name, Serializable> settingValues = new HashMap<>();

    private SettingsIniEntry(String name) {
        super(name);
    }

    // Having getters for version id/ROM names is kind of a redundancy.
    // With how few key/value pairs are in this IniEntry I wanted direct getters for each,
    // but also keep the ability to get them through getInt/StringValue().
    public int getVersionID() {
        return getIntValue(VERSION_ID);
    }

    private void setVersionID(String s) {
        putIntValue(VERSION_ID, Integer.parseInt(s));
    }

    public String getROMName() {
        return getStringValue(ROM_NAME);
    }

    private void setROMName(String s) {
        putStringValue(ROM_NAME, s);
    }

    /**
     * Gets a {@link Map} containing all Setting Name/Value pairs held by this entry.
     * By nature, these pairs are only those where the value is non-default.
     */
    public Map<Settings.Name, Serializable> getSettingValues() {
        return Collections.unmodifiableMap(settingValues);
    }

    private void addSettingValue(String[] valuePair) {
        Settings.Name name = Settings.Name.valueOf(valuePair[0].split("<")[1].split(">")[0]);
        Serializable value = parseSettingValue(name, valuePair[1]);
        settingValues.put(name, value);
    }

    /**
     * Parses a string as a setting value, contextualized by the name of the setting said value is meant for.
     * @param name The name of the relevant setting.
     * @param toParse The String to parse as a value.
     * @return A value that matches the type of the named setting.
     * @throws IllegalArgumentException if the name does not correspond to a setting with a parsable type
     * (Boolean, Integer, or Enum), or if an Integer or Enum value is invalid.
     */
    private Serializable parseSettingValue(Settings.Name name, String toParse) {
        Class<?> type = getSettingType(name);
        Serializable value;
        if (type.equals(Boolean.class)) {
            value = Boolean.parseBoolean(toParse);
        } else if (type.equals(Integer.class)) {
            try {
                value = Integer.parseInt(toParse);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        } else if (type.isEnum()) {
            value = Enum.valueOf(type.asSubclass(Enum.class), toParse);
        } else {
            throw new IllegalArgumentException("Parsing not defined for the type of setting: " + name);
        }
        return value;
    }

    private Class<? extends Serializable> getSettingType(Settings.Name name) {
        for (SettingDefinition<?> setting : Settings.ALL_SETTINGS) {
            if (setting.getName().equals(name)) {
                return setting.getType();
            }
        }
        for (SettingDefinition<?> removed : Settings.REMOVED_SETTINGS) {
            if (removed.getName().equals(name)) {
                return removed.getType();
            }
        }
        throw new IllegalArgumentException("No such setting: " + name);
    }
}
