package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen2Constants;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.dabomstew.pkromio.FileFunctions.openConfig;

/**
 * A description of a specific rom, or version of a rom. The {@code RomEntry} differentiates the rom from
 * similar roms, and contains data needed to read from, parse, and write to it. E.g. offsets or file names,
 * or string templates needed to modify TM event text.
 * <p>
 * The data in a RomEntry is generally different from that in constant files like {@link Gen2Constants},
 * because it relates to a specific rom, and not a game or generation as a whole. An offset may not be the same in
 * "Crystal (U)" and "Crystal (S)", despite both being roms of Pokémon crystal.
 */
public abstract class RomEntry extends IniEntry {

    protected abstract static class RomEntryReader<T extends RomEntry> extends IniEntryReader<T> {

        protected enum CopyFromMode { NAME, ROMCODE }

        private final CopyFromMode copyFromMode;

        public RomEntryReader(DefaultReadMode defaultReadMode, CopyFromMode copyFromMode) {
            super(defaultReadMode);
            this.copyFromMode = copyFromMode;
            putSpecialKeyMethod("Game", RomEntry::setRomCode);
            putSpecialKeyMethod("Version", RomEntry::setVersion);
            putSpecialKeyMethod("Type", RomEntry::setRomType);
            putKeySuffixMethod("Tweak", RomEntry::putTweakFile);
        }

        @Override
        public List<T> readEntriesFromFile(String fileName) throws FileNotFoundException {
            Scanner scanner = new Scanner(openConfig(fileName), "UTF-8");
            setFileName(fileName);
            return readEntriesFromScanner(scanner);
        }

        @Override
        protected boolean matchesCopyFromValue(T other, String value) {
            switch (copyFromMode) {
                case NAME : return value.equalsIgnoreCase(other.getName());
                case ROMCODE : return value.equals(other.getRomCode());
            }
            throw new RuntimeException("Should not get here");
        }

    }

    protected String romCode;
    protected int version;
    protected int romType;
    protected Map<String, String> tweakFiles = new HashMap<>(); // is this a good name?

    public RomEntry(String name) {
        super(name);
    }

    public RomEntry(RomEntry original) {
        super(original.name);
        this.romCode = original.romCode;
        this.version = original.version;
        this.romType = original.romType;
        intValues.putAll(original.intValues);
        stringValues.putAll(original.stringValues);
        arrayValues.putAll(original.arrayValues);
        tweakFiles.putAll(original.tweakFiles);
    }

    public String getRomCode() {
        return romCode;
    }

    public void setRomCode(String romCode) {
        this.romCode = romCode;
    }

    public int getVersion() {
        return version;
    }

    private void setVersion(String s) {
        this.version = IniEntryReader.parseInt(s);
    }

    public int getRomType() {
        return romType;
    }

    public void setRomType(int romType) {
        this.romType = romType;
    }

    protected abstract void setRomType(String s);

    public String getTweakFile(String key) {
        if (!tweakFiles.containsKey(key)) {
            tweakFiles.put(key, null);
        }
        return tweakFiles.get(key);
    }

    public boolean hasTweakFile(String key) {
        return tweakFiles.containsKey(key);
    }

    public void putTweakFile(String key, String value) {
        tweakFiles.put(key, value);
    }

    private void putTweakFile(String[] valuePair) {
        putTweakFile(valuePair[0], valuePair[1]);
    }

    public abstract boolean hasStaticPokemonSupport();

}
