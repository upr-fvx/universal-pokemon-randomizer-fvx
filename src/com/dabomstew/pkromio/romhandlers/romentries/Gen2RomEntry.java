package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen2Constants;
import com.dabomstew.pkromio.romhandlers.Gen2RomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link RomEntry} for Gen 2.
 */
public class Gen2RomEntry extends AbstractGBCRomEntry {

    public static class Gen2RomEntryReader<T extends Gen2RomEntry> extends GBCRomEntryReader<T> {

        protected Gen2RomEntryReader() {
            super();
            putSpecialKeyMethod("StaticPokemon{}", Gen2RomEntry::addStaticPokemon);
            putSpecialKeyMethod("StaticPokemonGameCorner{}", Gen2RomEntry::addStaticPokemonGameCorner);
            putSpecialKeyMethod("ShopName[]", Gen2RomEntry::addShopName);
            // aliases for backwards compatibility with old .ini files
            putIntAlias("PicPointers", "PokemonImages");
            putIntAlias("UnownPicPointers", "UnownImages");
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
            return (T) new Gen2RomEntry(name);
        }

        protected static Gen2RomHandler.StaticPokemon parseStaticPokemon(String s, boolean gameCorner) {
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
            // the only part which differs from Gen1RomEntryReader.parseStaticPokemon()
            Gen2RomHandler.StaticPokemon sp;
            if (gameCorner) {
                sp = new Gen2RomHandler.StaticPokemonGameCorner(speciesOffsets, levelOffsets);
            } else {
                sp = new Gen2RomHandler.StaticPokemon(speciesOffsets, levelOffsets);
            }
            return sp;
        }
    }

    public static final Gen2RomEntryReader<Gen2RomEntry> READER = new Gen2RomEntryReader<>();

    private final List<Gen2RomHandler.StaticPokemon> staticPokemon = new ArrayList<>();
    private List<String> shopNames = new ArrayList<>();

    private Gen2RomEntry(String name) {
        super(name);
    }

    public Gen2RomEntry(Gen2RomEntry original) {
        super(original);
        this.staticPokemon.addAll(original.staticPokemon);
        this.shopNames.addAll(original.shopNames);
    }

    public boolean isCrystal() {
        return romType == Gen2Constants.Type_Crystal;
    }

    @Override
    protected void setRomType(String s) {
        if (s.equalsIgnoreCase("GS")) {
            setRomType(Gen2Constants.Type_GS);
        } else if (s.equalsIgnoreCase("Crystal")) {
            setRomType(Gen2Constants.Type_Crystal);
        } else {
            System.err.println("unrecognised rom type: " + s);
        }
    }

    public List<Gen2RomHandler.StaticPokemon> getStaticPokemon() {
        return Collections.unmodifiableList(staticPokemon);
    }

    private void addStaticPokemon(String s) {
        staticPokemon.add(Gen2RomEntryReader.parseStaticPokemon(s, false));
    }

    private void addStaticPokemonGameCorner(String s) {
        staticPokemon.add(Gen2RomEntryReader.parseStaticPokemon(s, true));
    }

    public List<String> getShopNames() {
        return Collections.unmodifiableList(shopNames);
    }

    private void addShopName(String s)  {
        if (s.startsWith("[") && s.endsWith("]")) {
            String[] parts = s.substring(1, s.length() - 1).split(",", 2);
            int index = IniEntryReader.parseInt(parts[0]);
            String name = parts[1].trim();
            while (shopNames.size() < index) {
                shopNames.add("MISSING NAME");
            }
            shopNames.add(name);
        } else {
            System.out.println("Could not parse shop name: " + s);
        }
    }

    public void setShopNames(List<String> shopNames) {
        this.shopNames = shopNames;
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof Gen2RomEntry) {
            Gen2RomEntry gen2Other = (Gen2RomEntry) other;
            if (getIntValue("CopyStaticPokemon") == 1) {
                staticPokemon.addAll(gen2Other.staticPokemon);
                intValues.put("StaticPokemonSupport", 1);
            } else {
                intValues.put("StaticPokemonSupport", 0);
                intValues.remove("StaticPokemonOddEggOffset");
                intValues.remove("StaticPokemonOddEggDataSize");
            }
        }
    }
}
