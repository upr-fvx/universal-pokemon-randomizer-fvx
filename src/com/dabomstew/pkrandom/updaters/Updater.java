package com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.List;
import java.util.TreeMap;

/**
 * An abstract superclass for objects that "update" some aspect of a game (via a {@link RomHandler})
 * to be like in a later Generation. For the sake of logging, Updaters should record all updates they
 * make, to be retrieved using {@link #getUpdates()}.
 * <br><br>
 * This of course has a Vanilla perspective, so these classes might not work great with ROM hacks,
 * once those are otherwise supported.
 */
public abstract class Updater {

    public static class Update {
        private final Object descriptor;
        private final Object before;
        private final Object after;

        public Update(Object descriptor, Object before, Object after) {
            this.descriptor = descriptor;
            this.before = before;
            this.after = after;
        }

        @Override
        public String toString() {
            return descriptor + ": " + before + " -> " + after;
        }
    }

    protected final RomHandler romHandler;

    public Updater(RomHandler romHandler) {
        this.romHandler = romHandler;
    }

    public boolean isUpdated() {
        return getUpdates().isEmpty();
    }

    public abstract TreeMap<?, List<Update>> getUpdates();
}
