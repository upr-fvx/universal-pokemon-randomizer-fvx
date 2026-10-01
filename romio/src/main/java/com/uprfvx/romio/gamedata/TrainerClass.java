package com.uprfvx.romio.gamedata;

public class TrainerClass {

    private final int id;
    private String name;

    public TrainerClass(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
