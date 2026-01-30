package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen7Constants;

/**
 * A {@link RomEntry} for Gen 7.
 */
public class Gen7RomEntry extends Abstract3DSRomEntry {

    public static class Gen7RomEntryReader<T extends Gen7RomEntry> extends ThreeDSRomEntryReader<T> {

        protected Gen7RomEntryReader() {
            putSpecialKeyMethod("Type", Gen7RomEntry::setRomType);
            putSpecialKeyMethod("IsSunny", Gen7RomEntry::setSunny);
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
            return (T) new Gen7RomEntry(name);
        }
    }

    public static final Gen7RomEntryReader<Gen7RomEntry> READER = new Gen7RomEntryReader<>();

    private boolean sunny;

    public Gen7RomEntry(String name) {
        super(name);
    }

    @Override
    protected void setRomType(String s) {
        if (s.equalsIgnoreCase("USUM")) {
            setRomType(Gen7Constants.Type_USUM);
        } else {
            setRomType(Gen7Constants.Type_SM);
        }
    }

    /**
     * Returns true if the ROM is either Sun or Ultra Sun.
     */
    public boolean isSunny() {
        return sunny;
    }

    private void setSunny(String s) {
        sunny = IniEntryReader.parseBoolean(s);
    }

}
