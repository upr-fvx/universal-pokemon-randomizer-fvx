package com.dabomstew.pkrandom.romhandlers.romentries;

import com.dabomstew.pkrandom.constants.Gen4Constants;
import com.dabomstew.pkrandom.romhandlers.Gen4RomHandler;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gen4RomEntry extends AbstractDSRomEntry {

    protected static class Gen4RomEntryReader<T extends Gen4RomEntry> extends DSRomEntryReader<T> {

        public Gen4RomEntryReader(String fileName) throws IOException {
            super(fileName);
            putSpecialKeyMethod("Type", Gen4RomEntry::setRomType);
            putSpecialKeyMethod("CopyText", Gen4RomEntry::setCopyText);
            putSpecialKeyMethod("IgnoreGameCornerStatics", Gen4RomEntry::setIgnoreGameCornerStatics);
            putSpecialKeyMethod("RoamingPokemon{}", Gen4RomEntry::addRoamingPokemon);
            putSpecialKeyMethod("StaticPokemonGameCorner{}", Gen4RomEntry::addStaticPokemonGameCorner);
            putSpecialKeyMethod("TMText{}", Gen4RomEntry::addTMText);
            putSpecialKeyMethod("TMTextGameCorner{}", Gen4RomEntry::addTMTextGameCorner);
            putSpecialKeyMethod("FrontierScriptTMOffsets{}", Gen4RomEntry::addFrontierScriptTMOffset);
            putSpecialKeyMethod("FrontierTMText{}", Gen4RomEntry::addFrontierTMText);
            putKeySuffixMethod("MarillCryScripts", Gen4RomEntry::setMarillCryScriptEntries);
        }

        /**
         * Initiates a RomEntry of this class, since RomEntryReader can't do it on its own.<br>
         * MUST be overridden by any subclass.
         *
         * @param name The name of the RomEntry
         */
        @Override
        @SuppressWarnings("unchecked")
        protected T initiateRomEntry(String name) {
            return (T) new Gen4RomEntry(name);
        }

        private static Gen4RomHandler.StaticPokemonGameCorner parseStaticPokemonGameCorner(String staticPokemonString) {
            ScriptInFileEntry[] speciesEntries = new ScriptInFileEntry[0];
            ScriptInFileEntry[] levelEntries = new ScriptInFileEntry[0];
            Gen4RomHandler.TextEntry[] textEntries = new Gen4RomHandler.TextEntry[0];
            String pattern = "[A-z]+=\\[([0-9]+:0x[0-9a-fA-F]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(staticPokemonString);
            while (m.find()) {
                String[] segments = m.group().split("=");
                String[] offsets = segments[1].substring(1, segments[1].length() - 1).split(",");
                switch (segments[0]) {
                    case "Species" -> {
                        speciesEntries = new ScriptInFileEntry[offsets.length];
                        for (int i = 0; i < speciesEntries.length; i++) {
                            String[] parts = offsets[i].split(":");
                            speciesEntries[i] = new ScriptInFileEntry(BaseRomEntryReader.parseInt(parts[0]),
                                    BaseRomEntryReader.parseInt(parts[1]));
                        }
                    }
                    case "Level" -> {
                        levelEntries = new ScriptInFileEntry[offsets.length];
                        for (int i = 0; i < levelEntries.length; i++) {
                            String[] parts = offsets[i].split(":");
                            levelEntries[i] = new ScriptInFileEntry(BaseRomEntryReader.parseInt(parts[0]),
                                    BaseRomEntryReader.parseInt(parts[1]));
                        }
                    }
                    case "Text" -> {
                        textEntries = new Gen4RomHandler.TextEntry[offsets.length];
                        for (int i = 0; i < textEntries.length; i++) {
                            String[] parts = offsets[i].split(":");
                            textEntries[i] = new Gen4RomHandler.TextEntry(BaseRomEntryReader.parseInt(parts[0]),
                                    BaseRomEntryReader.parseInt(parts[1]));
                        }
                    }
                }
            }
            return new Gen4RomHandler.StaticPokemonGameCorner(speciesEntries, levelEntries, textEntries);
        }

        private static Gen4RomHandler.RoamingPokemon parseRoamingPokemon(String roamingPokemonString) {
            int[] speciesCodeOffsets = new int[0];
            int[] levelCodeOffsets = new int[0];
            ScriptInFileEntry[] speciesScriptOffsets = new ScriptInFileEntry[0];
            ScriptInFileEntry[] genderOffsets = new ScriptInFileEntry[0];
            String pattern = "[A-z]+=\\[(0x[0-9a-fA-F]+,?\\s?)+]|[A-z]+=\\[([0-9]+:0x[0-9a-fA-F]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(roamingPokemonString);
            while (m.find()) {
                String[] segments = m.group().split("=");
                String[] offsets = segments[1].substring(1, segments[1].length() - 1).split(",");
                switch (segments[0]) {
                    case "Species" -> {
                        speciesCodeOffsets = new int[offsets.length];
                        for (int i = 0; i < speciesCodeOffsets.length; i++) {
                            speciesCodeOffsets[i] = BaseRomEntryReader.parseInt(offsets[i]);
                        }
                    }
                    case "Level" -> {
                        levelCodeOffsets = new int[offsets.length];
                        for (int i = 0; i < levelCodeOffsets.length; i++) {
                            levelCodeOffsets[i] = BaseRomEntryReader.parseInt(offsets[i]);
                        }
                    }
                    case "Script" -> {
                        speciesScriptOffsets = new ScriptInFileEntry[offsets.length];
                        for (int i = 0; i < speciesScriptOffsets.length; i++) {
                            String[] parts = offsets[i].split(":");
                            speciesScriptOffsets[i] = new ScriptInFileEntry(BaseRomEntryReader.parseInt(parts[0]),
                                    BaseRomEntryReader.parseInt(parts[1]));
                        }
                    }
                    case "Gender" -> {
                        genderOffsets = new ScriptInFileEntry[offsets.length];
                        for (int i = 0; i < genderOffsets.length; i++) {
                            String[] parts = offsets[i].split(":");
                            genderOffsets[i] = new ScriptInFileEntry(BaseRomEntryReader.parseInt(parts[0]),
                                    BaseRomEntryReader.parseInt(parts[1]));
                        }
                    }
                }
            }
            return new Gen4RomHandler.RoamingPokemon(speciesCodeOffsets, levelCodeOffsets, speciesScriptOffsets, genderOffsets);
        }

        private static void parseTMText(String tmTextString, Map<Integer, List<Gen4RomHandler.TextEntry>> tmTexts) {
            String pattern = "[0-9]+=\\[([0-9]+:[0-9]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(tmTextString);
            while (m.find()) {
                String[] segments = m.group().split("=");
                int tmNum = BaseRomEntryReader.parseInt(segments[0]);
                String[] entries = segments[1].substring(1, segments[1].length() - 1).split(",");
                List<Gen4RomHandler.TextEntry> textEntries = new ArrayList<>();
                for (String entry : entries) {
                    String[] textSegments = entry.split(":");
                    Gen4RomHandler.TextEntry textEntry = new Gen4RomHandler.TextEntry(BaseRomEntryReader.parseInt(textSegments[0]), BaseRomEntryReader.parseInt(textSegments[1]));
                    textEntries.add(textEntry);
                }
                tmTexts.put(tmNum, textEntries);
            }
        }

        private static void parseTMTextGameCorner(String tmTextGameCornerString, Map<Integer, Gen4RomHandler.TextEntry> tmTextGameCorner) {
            String[] tmTextGameCornerEntries = tmTextGameCornerString.substring(1, tmTextGameCornerString.length() - 1)
                    .split(",");
            for (String tmTextGameCornerEntry : tmTextGameCornerEntries) {
                String[] segments = tmTextGameCornerEntry.trim().split("=");
                int tmNum = BaseRomEntryReader.parseInt(segments[0]);
                String textEntry = segments[1].substring(1, segments[1].length() - 1);
                String[] textSegments = textEntry.split(":");
                Gen4RomHandler.TextEntry entry = new Gen4RomHandler.TextEntry(BaseRomEntryReader.parseInt(textSegments[0]), BaseRomEntryReader.parseInt(textSegments[1]));
                tmTextGameCorner.put(tmNum, entry);
            }
        }
    }

    public static void readEntriesFromInfoFile(String fileName, Collection<Gen4RomEntry> romEntries) throws IOException {
        BaseRomEntryReader<Gen4RomEntry> rer = new Gen4RomEntry.Gen4RomEntryReader<>(fileName);
        rer.readAllRomEntries(romEntries);
    }

    private boolean ignoreGameCornerStatics = false;
    private boolean copyText = false;
    private final List<Gen4RomHandler.RoamingPokemon> roamingPokemon = new ArrayList<>();
    private final Map<Integer, List<Gen4RomHandler.TextEntry>> tmTexts = new HashMap<>();
    private final Map<Integer, Gen4RomHandler.TextEntry> tmTextsGameCorner = new HashMap<>();
    private final Map<Integer, Integer> tmScriptOffsetsFrontier = new HashMap<>();
    private final Map<Integer, Integer> tmTextsFrontier = new HashMap<>();
    private final List<ScriptInFileEntry> marillCryScriptEntries = new ArrayList<>();

    public Gen4RomEntry(String name) {
        super(name);
    }

    private void setRomType(String s) {
        if (s.equalsIgnoreCase("DP")) {
            setRomType(Gen4Constants.Type_DP);
        } else if (s.equalsIgnoreCase("Plat")) {
            setRomType(Gen4Constants.Type_Plat);
        } else if (s.equalsIgnoreCase("HGSS")) {
            setRomType(Gen4Constants.Type_HGSS);
        } else {
            System.err.println("unrecognised rom type: " + s);
        }
    }

    private void setIgnoreGameCornerStatics(String s) {
        this.ignoreGameCornerStatics = BaseRomEntryReader.parseBoolean(s);
    }

    private void setCopyText(String s) {
        this.copyText = BaseRomEntryReader.parseBoolean(s);
    }

    private void addStaticPokemonGameCorner(String s) {
        addStaticPokemon(Gen4RomEntryReader.parseStaticPokemonGameCorner(s));
    }

    public List<Gen4RomHandler.RoamingPokemon> getRoamingPokemon() {
        return Collections.unmodifiableList(roamingPokemon);
    }

    private void addRoamingPokemon(String s) {
        roamingPokemon.add(Gen4RomEntryReader.parseRoamingPokemon(s));
    }

    public Map<Integer, List<Gen4RomHandler.TextEntry>> getTMTexts() {
        return Collections.unmodifiableMap(tmTexts);
    }

    private void addTMText(String s) {
        Gen4RomEntryReader.parseTMText(s, tmTexts);
    }

    public Map<Integer, Gen4RomHandler.TextEntry> getTmTextsGameCorner() {
        return Collections.unmodifiableMap(tmTextsGameCorner);
    }

    private void addTMTextGameCorner(String s) {
        Gen4RomEntryReader.parseTMTextGameCorner(s, tmTextsGameCorner);
    }

    public Map<Integer, Integer> getTMScriptOffsetsFrontier() {
        return Collections.unmodifiableMap(tmScriptOffsetsFrontier);
    }

    private void addFrontierScriptTMOffset(String s) {
        String[] offsets = s.substring(1, s.length() - 1).split(",");
        for (String off : offsets) {
            String[] parts = off.split("=");
            int tmNum = BaseRomEntryReader.parseInt(parts[0]);
            int offset = BaseRomEntryReader.parseInt(parts[1]);
            tmScriptOffsetsFrontier.put(tmNum, offset);
        }
    }

    public Map<Integer, Integer> getTMTextsFrontier() {
        return Collections.unmodifiableMap(tmTextsFrontier);
    }

    private void addFrontierTMText(String s) {
        String[] offsets = s.substring(1, s.length() - 1).split(",");
        for (String off : offsets) {
            String[] parts = off.split("=");
            int tmNum = BaseRomEntryReader.parseInt(parts[0]);
            int stringNumber = BaseRomEntryReader.parseInt(parts[1]);
            tmTextsFrontier.put(tmNum, stringNumber);
        }
    }

    public List<ScriptInFileEntry> getMarillCryScriptEntries() {
        return Collections.unmodifiableList(getMarillCryScriptEntries());
    }

    private void setMarillCryScriptEntries(String[] valuePair) {
        marillCryScriptEntries.clear();
        String[] offsets = valuePair[1].substring(1, valuePair[1].length() - 1).split(",");
        for (String off : offsets) {
            String[] parts = off.split(":");
            int file = BaseRomEntryReader.parseInt(parts[0]);
            int offset = BaseRomEntryReader.parseInt(parts[1]);
            ScriptInFileEntry entry = new ScriptInFileEntry(file, offset);
            marillCryScriptEntries.add(entry);
        }
    }

    @Override
    public void copyFrom(RomEntry other) {
        super.copyFrom(other);
        if (other instanceof Gen4RomEntry gen4Other) {
            if (isCopyRoamingPokemon()) {
                roamingPokemon.addAll(gen4Other.roamingPokemon);
            }
            if (copyText) {
                tmTexts.putAll(gen4Other.tmTexts);
                tmTextsGameCorner.putAll(gen4Other.tmTextsGameCorner);
                tmScriptOffsetsFrontier.putAll(gen4Other.tmScriptOffsetsFrontier);
                tmTextsFrontier.putAll(gen4Other.tmTextsFrontier);
            }
            marillCryScriptEntries.addAll(gen4Other.marillCryScriptEntries);
        }
    }


}
