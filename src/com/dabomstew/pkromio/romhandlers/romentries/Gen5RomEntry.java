package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen5Constants;
import com.dabomstew.pkromio.romhandlers.Gen5RomHandler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link RomEntry} for Gen 5.
 */
public class Gen5RomEntry extends AbstractDSRomEntry {

    public static class Gen5RomEntryReader<T extends Gen5RomEntry> extends DSRomEntryReader<T> {

        protected Gen5RomEntryReader() {
            super(CopyFromMode.ROMCODE);
            putSpecialKeyMethod("Type", Gen5RomEntry::setRomType);
            putSpecialKeyMethod("IsBlack", Gen5RomEntry::setBlack);
            putSpecialKeyMethod("CopyTradeScripts", Gen5RomEntry::setCopyTradeScripts);
            putSpecialKeyMethod("StaticPokemonFakeBall{}", Gen5RomEntry::addStaticPokemonFakeBall);
            putSpecialKeyMethod("RoamingPokemon{}", Gen5RomEntry::addRoamingPokemon);
            putSpecialKeyMethod("TradeScript[]", Gen5RomEntry::addTradeScript);
            putKeyPrefixMethod("StarterOffsets", Gen5RomEntry::addStarterOffsets);
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
            return (T) new Gen5RomEntry(name);
        }

        protected static Gen5RomHandler.RoamingPokemon parseRoamingPokemon(String s) {
            int[] speciesOverlayOffsets = new int[0];
            int[] levelOverlayOffsets = new int[0];
            InFileEntry[] speciesScriptOffsets = new InFileEntry[0];
            String pattern = "[A-z]+=\\[(0x[0-9a-fA-F]+,?\\s?)+]|[A-z]+=\\[([0-9]+:0x[0-9a-fA-F]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(s);
            while (m.find()) {
                String[] segments = m.group().split("=");
                String[] offsets = segments[1].substring(1, segments[1].length() - 1).split(",");
                switch (segments[0]) {
                    case "Species": {
                        speciesOverlayOffsets = new int[offsets.length];
                        for (int i = 0; i < speciesOverlayOffsets.length; i++) {
                            speciesOverlayOffsets[i] = IniEntryReader.parseInt(offsets[i]);
                        }
                        break;
                    }
                    case "Level": {
                        levelOverlayOffsets = new int[offsets.length];
                        for (int i = 0; i < levelOverlayOffsets.length; i++) {
                            levelOverlayOffsets[i] = IniEntryReader.parseInt(offsets[i]);
                        }
                        break;
                    }
                    case "Script": {
                        speciesScriptOffsets = new InFileEntry[offsets.length];
                        for (int i = 0; i < speciesScriptOffsets.length; i++) {
                            String[] parts = offsets[i].split(":");
                            speciesScriptOffsets[i] = new InFileEntry(IniEntryReader.parseInt(parts[0]),
                                    IniEntryReader.parseInt(parts[1]));
                        }
                        break;
                    }
                }
            }
            return new Gen5RomHandler.RoamingPokemon(speciesOverlayOffsets, levelOverlayOffsets, speciesScriptOffsets);
        }
    }

    public static final Gen5RomEntryReader<Gen5RomEntry> READER = new Gen5RomEntryReader<>();

    private boolean copyTradeScripts = false;
    private boolean black = false;
    private final List<DSStaticPokemon> staticPokemonFakeBall = new ArrayList<>();
    private final List<Gen5RomHandler.RoamingPokemon> roamingPokemon = new ArrayList<>();
    private final List<Gen5RomHandler.TradeScript> tradeScripts = new ArrayList<>();
    private final Map<String, InFileEntry[]> offsetArrayEntries = new HashMap<>();

    public Gen5RomEntry(String name) {
        super(name);
    }

    @Override
    protected void setRomType(String s) {
        if (s.equalsIgnoreCase("BW1")) {
            setRomType(Gen5Constants.Type_BW);
        } else if (s.equalsIgnoreCase("BW2")) {
            setRomType(Gen5Constants.Type_BW2);
        } else {
            System.err.println("unrecognised rom type: " + s);
        }
    }

    private void setCopyTradeScripts(String s) {
        copyTradeScripts = IniEntryReader.parseBoolean(s);
    }

    public boolean isBlack() {
        return black;
    }

    private void setBlack(String s) {
        black = IniEntryReader.parseBoolean(s);
    }

    public List<DSStaticPokemon> getStaticPokemonFakeBall() {
        return Collections.unmodifiableList(staticPokemonFakeBall);
    }

    private void addStaticPokemonFakeBall(String s) {
        staticPokemonFakeBall.add(DSRomEntryReader.parseStaticPokemon(s));
    }

    public List<Gen5RomHandler.RoamingPokemon> getRoamingPokemon() {
        return Collections.unmodifiableList(roamingPokemon);
    }

    private void addRoamingPokemon(String s) {
        roamingPokemon.add(Gen5RomEntryReader.parseRoamingPokemon(s));
    }

    public List<Gen5RomHandler.TradeScript> getTradeScripts() {
        return Collections.unmodifiableList(tradeScripts);
    }

    private void addTradeScript(String s) {
        String[] offsets = s.substring(1, s.length() - 1).split(",");
        int[] requestedOffsets = new int[offsets.length];
        int[] givenOffsets = new int[offsets.length];
        int fileNum = 0;
        int c = 0;
        for (String off : offsets) {
            String[] parts = off.split(":");
            fileNum = IniEntryReader.parseInt(parts[0]);
            requestedOffsets[c] = IniEntryReader.parseInt(parts[1]);
            givenOffsets[c++] = IniEntryReader.parseInt(parts[2]);
        }
        tradeScripts.add(new Gen5RomHandler.TradeScript(fileNum, requestedOffsets, givenOffsets));
    }

    public InFileEntry[] getOffsetArrayEntry(String key) {
        return offsetArrayEntries.get(key);
    }

    private void addStarterOffsets(String[] valuePair) {
        String[] offsets = valuePair[1].substring(1, valuePair[1].length() - 1).split(",");
        InFileEntry[] offs = new InFileEntry[offsets.length];
        int c = 0;
        for (String off : offsets) {
            String[] parts = off.split(":");
            int entry = IniEntryReader.parseInt(parts[0]);
            int offset = IniEntryReader.parseInt(parts[1]);
            offs[c++] = new InFileEntry(entry, offset);
        }
        offsetArrayEntries.put(valuePair[0], offs);
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof Gen5RomEntry) {
            Gen5RomEntry gen5Other = (Gen5RomEntry) other;
            offsetArrayEntries.putAll(gen5Other.offsetArrayEntries);
            if (isCopyStaticPokemon()) {
                staticPokemonFakeBall.addAll(gen5Other.staticPokemonFakeBall);
                setStaticPokemonSupport(true);
            } else {
                setStaticPokemonSupport(false);
            }
            if (isCopyRoamingPokemon()) {
                roamingPokemon.addAll(gen5Other.roamingPokemon);
            }
            if (copyTradeScripts) {
                tradeScripts.addAll(gen5Other.tradeScripts);
            }
        }
    }

}
