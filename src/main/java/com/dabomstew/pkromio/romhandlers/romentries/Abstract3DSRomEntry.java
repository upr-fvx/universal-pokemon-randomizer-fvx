package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.romhandlers.Abstract3DSRomHandler;

import java.util.*;

/**
 * An abstract {@link RomEntry} to be used by 3DS games. Thus supports descriptions of the 3DS file system.
 * Corresponds to {@link Abstract3DSRomHandler}.
 */
public abstract class Abstract3DSRomEntry extends RomEntry {

    protected abstract static class ThreeDSRomEntryReader<T extends Abstract3DSRomEntry> extends RomEntryReader<T> {

        protected ThreeDSRomEntryReader() {
            super(DefaultReadMode.STRING, CopyFromMode.ROMCODE);
            putSpecialKeyMethod("TitleId", Abstract3DSRomEntry::setTitleID);
            putSpecialKeyMethod("Acronym", Abstract3DSRomEntry::setAcronym);
            putSpecialKeyMethod("CodeCRC32", Abstract3DSRomEntry::setExpectedCodeCRC32s);
            putSpecialKeyMethod("LinkedStaticEncounterOffsets", Abstract3DSRomEntry::addLinkedEncounter);
            putKeyPrefixMethod("File<", Abstract3DSRomEntry::addFile);
            putKeySuffixMethod("Offset", this::addIntValue);
            putKeySuffixMethod("Count", this::addIntValue);
            putKeySuffixMethod("Number", this::addIntValue);
        }
    }

    private String titleID;
    private String acronym;
    private final long[] expectedCodeCRC32s = new long[2];
    private final List<ThreeDSLinkedEncounter> linkedEncounters = new ArrayList<>();
    private final Map<String, InFileEntry[]> offsetArrayEntries = new HashMap<>();
    private final Map<String, ThreeDSFileEntry> files = new HashMap<>();

    public Abstract3DSRomEntry(String name) {
        super(name);
    }

    @Override
    public boolean hasStaticPokemonSupport() {
        return true;
    }

    public String getTitleID(){
        return titleID;
    }

    private void setTitleID(String titleID) {
        this.titleID = titleID;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public long[] getExpectedCodeCRC32s() {
        return expectedCodeCRC32s;
    }

    private void setExpectedCodeCRC32s(String s) {
        String[] values = s.substring(1, s.length() - 1).split(",");
        expectedCodeCRC32s[0] = IniEntryReader.parseLong("0x" + values[0].trim());
        expectedCodeCRC32s[1] = IniEntryReader.parseLong("0x" + values[1].trim());
    }

    public Set<String> getFileKeys() {
        return Collections.unmodifiableSet(files.keySet());
    }

    public String getFile(String key) {
        if (!files.containsKey(key)) {
            files.put(key, new ThreeDSFileEntry());
        }
        return files.get(key).getPath();
    }

    public long[] getFileExpectedCRC32s(String key) {
        if (!files.containsKey(key)) {
            files.put(key, new ThreeDSFileEntry());
        }
        return files.get(key).getExpectedCRC32s();
    }

    private void addFile(String[] valuePair) {
        String key = valuePair[0].split("<")[1].split(">")[0];
        String[] values = valuePair[1].substring(1, valuePair[1].length() - 1).split(",");
        String path = values[0].trim();
        String crcString = values[1].trim() + ", " + values[2].trim();
        String[] crcs = crcString.substring(1, crcString.length() - 1).split(",");
        long[] expectedCRC32s = new long[2];
        expectedCRC32s[0] = IniEntryReader.parseLong("0x" + crcs[0].trim());
        expectedCRC32s[1] = IniEntryReader.parseLong("0x" + crcs[1].trim());
        files.put(key, new ThreeDSFileEntry(path, expectedCRC32s));
    }

    public List<ThreeDSLinkedEncounter> getLinkedEncounters() {
        return Collections.unmodifiableList(linkedEncounters);
    }

    private void addLinkedEncounter(String s) {
        String[] offsets = s.substring(1, s.length() - 1).split(",");
        for (String offset : offsets) {
            String[] parts = offset.split(":");
            linkedEncounters.add(new ThreeDSLinkedEncounter(Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim())));
        }
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof Abstract3DSRomEntry) {
            Abstract3DSRomEntry threeDSOther = (Abstract3DSRomEntry) other;
            linkedEncounters.addAll(threeDSOther.linkedEncounters);
            offsetArrayEntries.putAll(threeDSOther.offsetArrayEntries);
            files.putAll(threeDSOther.files);
        }
    }
}
