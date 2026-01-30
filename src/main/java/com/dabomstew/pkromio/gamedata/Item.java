package com.dabomstew.pkromio.gamedata;

import java.util.Objects;

public class Item implements Comparable<Item> {

    // TODO: ideally, there should be an ItemType enum.
    //  This would eliminate disparate handling for questions like "is this a TM?", "is this a Mega Stone?",
    //  "is this a Z Crystal?". That will have to be done in a second Item-refactor though, not the same
    //  one that introduced the Item class.

    private final int id;
    private final String name;

    private boolean allowed = true;
    private boolean bad;
    private boolean tm;

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

    /**
     * Returns whether an Item is allowed to appear in most places, like {@link Shop}s and field items.
     * Generally key items and unused/glitch items NOT allowed, but depending on the game more Items may be as well.
     */
    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isBad() {
        return bad;
    }

    public void setBad(boolean bad) {
        this.bad = bad;
    }

    public boolean isTM() {
        return tm;
    }

    public void setTM(boolean tm) {
        this.tm = tm;
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

    @Override
    public int compareTo(Item o) {
        return id - o.id;
    }
}
