package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen6Constants;

/**
 * A {@link RomEntry} for Gen 6.
 */
public class Gen6RomEntry extends Abstract3DSRomEntry {

    public static class Gen6RomEntryReader<T extends Gen6RomEntry> extends ThreeDSRomEntryReader<T> {

        protected Gen6RomEntryReader() {
            super();
            putSpecialKeyMethod("Type", Gen6RomEntry::setRomType);
        }

        /**
         * Initiates a RomEntry of this class, since RomEntryReader can't do it on its own.<br>
         * MUST be overridden by any subclass.
         *
         * @param name The name of the RomEntry
         */
        @Override
        @SuppressWarnings("unchecked")
        protected T initiateEntry(String name) {
            return (T) new Gen6RomEntry(name);
        }
    }

    public static final Gen6RomEntryReader<Gen6RomEntry> READER = new Gen6RomEntryReader<>();

    public Gen6RomEntry(String name) {
        super(name);
    }

    @Override
    protected void setRomType(String s) {
        if (s.equalsIgnoreCase("XY")) {
            setRomType(Gen6Constants.Type_XY);
        } else if (s.equalsIgnoreCase("ORAS")){
            setRomType(Gen6Constants.Type_ORAS);
        } else {
            System.err.println("unrecognised rom type: " + s);
        }
    }

}
