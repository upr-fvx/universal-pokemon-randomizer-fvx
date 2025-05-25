package com.dabomstew.pkrandom.romhandlers.romentries;

import com.dabomstew.pkrandom.romhandlers.AbstractGBRomHandler;

/**
 * An abstract {@link RomEntry} to be used by GB games. Corresponds to {@link AbstractGBRomHandler}.
 */
public abstract class AbstractGBRomEntry extends RomEntry {

    protected abstract static class GBRomEntryReader<T extends AbstractGBRomEntry> extends RomEntryReader<T> {

        public GBRomEntryReader() {
            super(DefaultReadMode.INT, CopyFromMode.NAME);
            putSpecialKeyMethod("CRC32", AbstractGBRomEntry::setExpectedCRC32);
            putKeySuffixMethod("Locator", this::addStringValue);
            putKeySuffixMethod("Prefix", this::addStringValue);
            putKeySuffixMethod("String", this::addStringValue);
        }
    }

    private int version;
    private long expectedCRC32 = -1;

    public AbstractGBRomEntry(String name) {
        super(name);
    }

    public AbstractGBRomEntry(AbstractGBRomEntry original) {
        super(original);
        this.version = original.version;
        this.expectedCRC32 = original.expectedCRC32;
    }

    @Override
    public boolean hasStaticPokemonSupport() {
        return getIntValue("StaticPokemonSupport") > 0;
    }

    public long getExpectedCRC32() {
        return expectedCRC32;
    }

    private void setExpectedCRC32(String s) {
        this.expectedCRC32 = IniEntryReader.parseLong("0x" + s);
    }

}
