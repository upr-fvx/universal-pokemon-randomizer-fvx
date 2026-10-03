package com.uprfvx.romio.gamedata;

public class TrainerClass {

    private final int id;
    private String name;

    public TrainerClass(int id, String name) {
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.id = id;
        this.name = name;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return "#" + id + " - " + name;
    }
}
