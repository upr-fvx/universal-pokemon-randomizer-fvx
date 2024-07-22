package com.dabomstew.pkrandom.game_data;

import java.util.Objects;

public class Item {

    private final int id;
    private final String name;

    public Item(int id, String name) {
        if (id < 1) {
            throw new IllegalArgumentException("id must be at least 1");
        }
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Item) {
            return ((Item) o).id == id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id + "-" + name;
    }
}
