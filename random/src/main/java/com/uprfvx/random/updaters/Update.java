package com.uprfvx.random.updaters;

public class Update<T> {
    private final T before;
    private final T after;

    public Update(T before, T after) {
        this.before = before;
        this.after = after;
    }

    public T getBefore() {
        return before;
    }

    public T getAfter() {
        return after;
    }
}
