package com.dabomstew.pkrandom.game_data;

import java.util.Objects;

public class Item {

    public static final Item NOTHING = new Item(0, "nothing");

    private final int id;
    private final String name;

    public Item(int id, String name) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be positive");
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
