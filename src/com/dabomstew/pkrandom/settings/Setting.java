package com.dabomstew.pkrandom.settings;

public class Setting<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
