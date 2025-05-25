package com.dabomstew.pkrandom.exceptions;

public class InvalidROMException extends Exception {
    public enum Type {
        LENGTH, ZIP_FILE, RAR_FILE, IPS_FILE, UNREADABLE
    }

    private final Type type;

    public InvalidROMException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
