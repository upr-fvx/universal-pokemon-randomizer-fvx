package com.dabomstew.pkrandom.romhandlers.romentries;

import com.dabomstew.pkrandom.romhandlers.AbstractGBRomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract {@link RomEntry} to be used by GB games. Corresponds to {@link AbstractGBRomHandler}.
 */
public abstract class AbstractGBRomEntry extends RomEntry {

    protected abstract static class GBRomEntryReader<T extends AbstractGBRomEntry> extends RomEntryReader<T> {

        public GBRomEntryReader() {
            super(DefaultReadMode.INT, CopyFromMode.NAME);
            putSpecialKeyMethod("CRC32", AbstractGBRomEntry::setExpectedCRC32);
            putSpecialKeyMethod("UnusedChunk[]", AbstractGBRomEntry::addUnusedChunk);
            putKeySuffixMethod("Locator", this::addStringValue);
            putKeySuffixMethod("Prefix", this::addStringValue);
            putKeySuffixMethod("String", this::addStringValue);
        }
    }

    private int version;
    private long expectedCRC32 = -1;
    private final List<GBUnusedChunkEntry> unusedChunks = new ArrayList<>();

    public AbstractGBRomEntry(String name) {
        super(name);
    }

    public AbstractGBRomEntry(AbstractGBRomEntry original) {
        super(original);
        this.version = original.version;
        this.expectedCRC32 = original.expectedCRC32;
        this.unusedChunks.addAll(original.unusedChunks);
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

    public List<GBUnusedChunkEntry> getUnusedChunks() {
        return Collections.unmodifiableList(unusedChunks);
    }

    private void addUnusedChunk(String s)  {
        if (s.startsWith("[") && s.endsWith("]")) {
            String[] parts = s.substring(1, s.length() - 1).split(",", 2);
            int offset = IniEntryReader.parseInt(parts[0]);
            int length = IniEntryReader.parseInt(parts[1]);
            GBUnusedChunkEntry chunk = new GBUnusedChunkEntry(offset, length);
            unusedChunks.add(chunk);
        }
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof AbstractGBRomEntry) {
            AbstractGBRomEntry gbOther = (AbstractGBRomEntry) other;
            // TODO: fix CopyUnusedChunks not working properly
            if (getIntValue("CopyUnusedChunks") == 1) {
                unusedChunks.addAll(gbOther.unusedChunks);
            }
        }
    }

}
