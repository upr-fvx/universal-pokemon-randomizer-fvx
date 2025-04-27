package com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Map;

/**
 * An abstract superclass for objects that "update" some aspect of a game (via a {@link RomHandler})
 * to be like in a later Generation. For the sake of logging, Updaters should record all updates they
 * make, to be retrieved using {@link #getUpdates()}.
 * <br><br>
 * This of course has a Vanilla perspective, so these classes might not work great with ROM hacks,
 * once those are otherwise supported.
 */
public abstract class Updater<Target, Desc, Attr> {

    protected final RomHandler romHandler;

    public Updater(RomHandler romHandler) {
        this.romHandler = romHandler;
    }

    public boolean isUpdated() {
        return !getUpdates().isEmpty();
    }

    public abstract Map<Target, Map<Desc, Update<Attr>>> getUpdates();
}
