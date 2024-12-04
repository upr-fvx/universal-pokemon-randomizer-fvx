package com.dabomstew.pkrandom.settings;

import java.util.List;

public interface SettingRestriction {

    List<String> settingNames();

    boolean test(SettingsManager manager);
}
