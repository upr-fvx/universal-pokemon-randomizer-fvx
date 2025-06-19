package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.romhandlers.AbstractGBRomHandler;

import java.util.*;

/**
 * An abstract {@link RomEntry} to be used by GB games. Corresponds to {@link AbstractGBRomHandler}.
 * <br><br>
 * Provides the powerful, but risky, UnusedChunk functionality.
 * Each UnusedChunk corresponds to a known chunk of data, which can be safely deleted from the ROM
 * to make space for other data.<br>
 * <b>Be aware of the UnusedChunks when copying.</b> If offset and/or length of the data changes
 * between the copy-source and the copy-er, <b>copied UnusedChunks may cause corruption!!</b><br>
 * Since each UnusedChunk is tied to a name, their location and length can be overwritten.
 * And they can be removed by setting them to <code>null</code>. (e.g. <code>UnusedChunk&lt;Garb00&gt;=null</code>).
 * <br><br>
 * Since UnusedChunks are so risky, use other methods of space management when you can.
 */
public abstract class AbstractGBRomEntry extends RomEntry {

    protected abstract static class GBRomEntryReader<T extends AbstractGBRomEntry> extends RomEntryReader<T> {

        public GBRomEntryReader() {
            super(DefaultReadMode.INT, CopyFromMode.NAME);
            putSpecialKeyMethod("CRC32", AbstractGBRomEntry::setExpectedCRC32);
            putKeyPrefixMethod("UnusedChunk<", AbstractGBRomEntry::addUnusedChunk);
            putKeySuffixMethod("Locator", this::addStringValue);
            putKeySuffixMethod("Prefix", this::addStringValue);
            putKeySuffixMethod("String", this::addStringValue);
        }
    }

    private int version;
    private long expectedCRC32 = -1;
    private final Map<String, GBUnusedChunkEntry> unusedChunks = new HashMap<>();

    public AbstractGBRomEntry(String name) {
        super(name);
    }

    public AbstractGBRomEntry(AbstractGBRomEntry original) {
        super(original);
        this.version = original.version;
        this.expectedCRC32 = original.expectedCRC32;
        this.unusedChunks.putAll(original.unusedChunks);
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

    public Set<GBUnusedChunkEntry> getUnusedChunks() {
        return Collections.unmodifiableSet(new HashSet<>(unusedChunks.values()));
    }

    private void addUnusedChunk(String[] valuePair)  {
        String key = valuePair[0].split("<")[1].split(">")[0];
        String value = valuePair[1];
        if (value.equals("null")) {
            unusedChunks.remove(key);
        } else {
            if (!value.startsWith("[") || !value.endsWith("]")) {
                throw new IllegalArgumentException("Invalid format; brackets missing. " + Arrays.toString(valuePair));
            }
            String[] parts = value.substring(1, value.length() - 1).split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format, must have 2 args in brackets. " + Arrays.toString(valuePair));
            }
            int offset = IniEntryReader.parseInt(parts[0]);
            int length = IniEntryReader.parseInt(parts[1]);
            GBUnusedChunkEntry chunk = new GBUnusedChunkEntry(offset, length);
            unusedChunks.put(key, chunk);
        }
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof AbstractGBRomEntry) {
            AbstractGBRomEntry gbOther = (AbstractGBRomEntry) other;
            unusedChunks.putAll(gbOther.unusedChunks);
        }
    }

}
