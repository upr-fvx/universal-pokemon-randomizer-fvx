package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.romhandlers.AbstractDSRomHandler;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract {@link RomEntry} to be used by DS games. Thus supports descriptions of the DS (NARC) file system.
 * Corresponds to {@link AbstractDSRomHandler}.
 */
public abstract class AbstractDSRomEntry extends RomEntry {

    protected abstract static class DSRomEntryReader<T extends AbstractDSRomEntry> extends RomEntryReader<T> {

        protected DSRomEntryReader(CopyFromMode copyFromMode) {
            super(DefaultReadMode.STRING, copyFromMode);
            putSpecialKeyMethod("Arm9CRC32", AbstractDSRomEntry::setArm9ExpectedCRC32);
            putSpecialKeyMethod("StaticPokemonSupport", AbstractDSRomEntry::setStaticPokemonSupport);
            putSpecialKeyMethod("CopyStaticPokemon", AbstractDSRomEntry::setCopyStaticPokemon);
            putSpecialKeyMethod("CopyRoamingPokemon", AbstractDSRomEntry::setCopyRoamingPokemon);
            putKeyPrefixMethod("File<", AbstractDSRomEntry::addFile);
            putKeyPrefixMethod("OverlayCRC32<", AbstractDSRomEntry::addOverlayExpectedCRC32);
            putSpecialKeyMethod("StaticPokemon{}", AbstractDSRomEntry::addStaticPokemon);
            putKeySuffixMethod("Offset", this::addIntValue);
            putKeySuffixMethod("Count", this::addIntValue);
            putKeySuffixMethod("Number", this::addIntValue);
            putKeySuffixMethod("Size", this::addIntValue);
            putKeySuffixMethod("Index", this::addIntValue);
        }

        protected static DSStaticPokemon parseStaticPokemon(String s) {
            InFileEntry[] speciesEntries = new InFileEntry[0];
            InFileEntry[] levelEntries = new InFileEntry[0];
            InFileEntry[] formeEntries = new InFileEntry[0];
            String pattern = "[A-z]+=\\[([0-9]+:0x[0-9a-fA-F]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(s);
            while (m.find()) {
                String[] segments = m.group().split("=");
                String[] offsets = segments[1].substring(1, segments[1].length() - 1).split(",");
                InFileEntry[] entries = new InFileEntry[offsets.length];
                for (int i = 0; i < entries.length; i++) {
                    String[] parts = offsets[i].split(":");
                    entries[i] = new InFileEntry(IniEntryReader.parseInt(parts[0]), IniEntryReader.parseInt(parts[1]));
                }
                switch (segments[0]) {
                    case "Species":
                        speciesEntries = entries;
                        break;
                    case "Level":
                        levelEntries = entries;
                        break;
                    case "Forme":
                        formeEntries = entries;
                        break;
                }
            }

            return new DSStaticPokemon(speciesEntries, formeEntries, levelEntries);
        }
    }

    private long arm9ExpectedCRC32;
    private boolean staticPokemonSupport = false;
    private boolean copyStaticPokemon = false;
    private boolean copyRoamingPokemon = false;
    private final Map<String, DSFileEntry> files = new HashMap<>();
    private final Map<Integer, Long> overlayExpectedCRC32s = new HashMap<>();
    private final List<DSStaticPokemon> staticPokemon = new ArrayList<>();

    public AbstractDSRomEntry(String name) {
        super(name);
    }

    public long getArm9ExpectedCRC32() {
        return arm9ExpectedCRC32;
    }

    private void setArm9ExpectedCRC32(String s) {
        this.arm9ExpectedCRC32 = IniEntryReader.parseLong("0x" + s);
    }

    public boolean hasStaticPokemonSupport() {
        return staticPokemonSupport;
    }

    protected void setStaticPokemonSupport(Boolean staticPokemonSupport) {
        this.staticPokemonSupport = staticPokemonSupport;
    }

    private void setStaticPokemonSupport(String s) {
        this.staticPokemonSupport = IniEntryReader.parseBoolean(s);
    }

    public boolean isCopyStaticPokemon() {
        return copyStaticPokemon;
    }

    private void setCopyStaticPokemon(String s) {
        this.copyStaticPokemon = IniEntryReader.parseBoolean(s);
    }

    public boolean isCopyRoamingPokemon() {
        return copyRoamingPokemon;
    }

    private void setCopyRoamingPokemon(String s) {
        this.copyRoamingPokemon = IniEntryReader.parseBoolean(s);
    }

    public Set<String> getFileKeys() {
        return Collections.unmodifiableSet(files.keySet());
    }

    public String getFile(String key) {
        if (!files.containsKey(key)) {
            throw new IllegalArgumentException("File \"" + key + "\" does not exist.");
        }
        return files.get(key).getPath();
    }

    public long getFileExpectedCRC32(String key) {
        if (!files.containsKey(key)) {
            files.put(key, new DSFileEntry());
        }
        return files.get(key).getExpectedCRC32();
    }

    private void addFile(String[] valuePair) {
        String key = valuePair[0].split("<")[1].split(">")[0];
        String[] values = valuePair[1].substring(1, valuePair[1].length() - 1).split(",");
        String path = values[0].trim();
        long expectedCRC32 = IniEntryReader.parseLong("0x" + values[1].trim());
        files.put(key, new DSFileEntry(path, expectedCRC32));
    }

    public Set<Integer> getOverlayExpectedCRC32Keys() {
        return Collections.unmodifiableSet(overlayExpectedCRC32s.keySet());
    }

    public long getOverlayExpectedCRC32(int key) {
        return overlayExpectedCRC32s.get(key);
    }

    private void addOverlayExpectedCRC32(String[] valuePair) {
        String keyString = valuePair[0].split("<")[1].split(">")[0];
        int key = IniEntryReader.parseInt(keyString);
        long value = IniEntryReader.parseLong("0x" + valuePair[1]);
        overlayExpectedCRC32s.put(key, value);
    }

    public List<DSStaticPokemon> getStaticPokemon() {
        return Collections.unmodifiableList(staticPokemon);
    }

    public void addStaticPokemon(DSStaticPokemon sp) {
        staticPokemon.add(sp);
    }

    private void addStaticPokemon(String s) {
        staticPokemon.add(DSRomEntryReader.parseStaticPokemon(s));
    }

    // Sub-optimal use of protection levels, this exists just to support removing game corner pokes in Gen4RomEntry.
    // Should consider whether private fields like staticPokemon should just be protected to allow subclasses
    // to use them directly.
    protected void removeStaticPokemonIf(Predicate<DSStaticPokemon> filter) {
        staticPokemon.removeIf(filter);
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof AbstractDSRomEntry) {
            AbstractDSRomEntry dsOther = (AbstractDSRomEntry) other;
            files.putAll(dsOther.files);
            if (isCopyStaticPokemon()) {
                staticPokemon.addAll(dsOther.staticPokemon);
                setStaticPokemonSupport(true);
            } else {
                setStaticPokemonSupport(false);
            }
        }
    }

}
