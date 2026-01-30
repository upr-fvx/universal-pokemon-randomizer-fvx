package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen1Constants;
import com.dabomstew.pkromio.romhandlers.Gen1RomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link RomEntry} for Gen 1.
 */
public class Gen1RomEntry extends AbstractGBCRomEntry {

    public static class Gen1RomEntryReader<T extends Gen1RomEntry> extends GBCRomEntryReader<T> {

        protected Gen1RomEntryReader() {
            super();
            putSpecialKeyMethod("StaticPokemon{}", Gen1RomEntry::addStaticPokemon);
            putSpecialKeyMethod("StaticPokemonGhostMarowak{}", Gen1RomEntry::addStaticPokemonGhostMarowak);

            putIntAlias("SpecialMapList", "HiddenObjectMapList");
            putIntAlias("SpecialMapPointerTable", "HiddenObjectMapPointerTable");
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
            return (T) new Gen1RomEntry(name);
        }

        protected static Gen1RomHandler.StaticPokemon parseStaticPokemon(String s) {
            int[] speciesOffsets = new int[0];
            int[] levelOffsets = new int[0];
            String pattern = "[A-z]+=\\[(0x[0-9a-fA-F]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(s);
            while (m.find()) {
                String[] segments = m.group().split("=");
                String[] romOffsets = segments[1].substring(1, segments[1].length() - 1).split(",");
                int[] offsets = new int[romOffsets.length];
                for (int i = 0; i < offsets.length; i++) {
                    offsets[i] = IniEntryReader.parseInt(romOffsets[i]);
                }
                switch (segments[0]) {
                    case "Species":
                        speciesOffsets = offsets;
                        break;
                    case "Level":
                        levelOffsets = offsets;
                        break;
                }
            }
            return new Gen1RomHandler.StaticPokemon(speciesOffsets, levelOffsets);
        }
    }

    public static final Gen1RomEntryReader<Gen1RomEntry> READER = new Gen1RomEntryReader<>();

    private final List<Gen1RomHandler.StaticPokemon> staticPokemon = new ArrayList<>();
    private int[] ghostMarowakOffsets = new int[0];

    private Gen1RomEntry(String name) {
        super(name);
    }

    public Gen1RomEntry(Gen1RomEntry original) {
        super(original);
        staticPokemon.addAll(original.staticPokemon);
        ghostMarowakOffsets = original.ghostMarowakOffsets;
    }

    public boolean isYellow() {
        return romType == Gen1Constants.Type_Yellow;
    }

    @Override
    protected void setRomType(String s) {
        if (s.equalsIgnoreCase("RB")) {
            setRomType(Gen1Constants.Type_RB);
        } else if (s.equalsIgnoreCase("Yellow")) {
            setRomType(Gen1Constants.Type_Yellow);
        } else {
            System.err.println("unrecognised rom type: " + s);
        }
    }

    public List<Gen1RomHandler.StaticPokemon> getStaticPokemon() {
        return Collections.unmodifiableList(staticPokemon);
    }

    private void addStaticPokemon(String s) {
        staticPokemon.add(Gen1RomEntryReader.parseStaticPokemon(s));
    }

    public int[] getGhostMarowakOffsets() {
        return ghostMarowakOffsets;
    }

    private void addStaticPokemonGhostMarowak(String s) {
        Gen1RomHandler.StaticPokemon ghostMarowak = Gen1RomEntryReader.parseStaticPokemon(s);
        staticPokemon.add(ghostMarowak);
        ghostMarowakOffsets = ghostMarowak.getSpeciesOffsets();
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof Gen1RomEntry) {
            Gen1RomEntry gen1Other = (Gen1RomEntry) other;
            if (getIntValue("CopyStaticPokemon") == 1) {
                staticPokemon.addAll(gen1Other.staticPokemon);
                ghostMarowakOffsets = gen1Other.ghostMarowakOffsets;
                intValues.put("StaticPokemonSupport", 1);
            } else {
                intValues.put("StaticPokemonSupport", 0);
            }
        }

    }
}
