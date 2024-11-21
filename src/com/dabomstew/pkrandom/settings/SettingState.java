package com.dabomstew.pkrandom.settings;

import java.util.function.Predicate;

public class SettingState<U> {
    final String name;
    final Predicate<U> state;

    public SettingState(String name, Predicate<U> state) {
        this.name = name;
        this.state = state;
    }
}
