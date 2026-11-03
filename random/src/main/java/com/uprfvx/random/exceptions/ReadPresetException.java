package com.uprfvx.random.exceptions;

public class ReadPresetException extends RuntimeException {

    public enum Type {
        IO_ERROR, NO_SEED, INVALID_SETTINGS, WRONG_VERSION
    }

    private final Type type;

    public ReadPresetException(Type type, Exception cause) {
        super(type.name(), cause);
        this.type = type;
    }

    public Type getType() {
      return type;
    }
}
