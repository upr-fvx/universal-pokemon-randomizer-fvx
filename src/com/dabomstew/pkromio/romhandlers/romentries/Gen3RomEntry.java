package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.constants.Gen3Constants;
import com.dabomstew.pkromio.romhandlers.Gen3RomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link RomEntry} for Gen 3.
 */
public class Gen3RomEntry extends AbstractGBRomEntry {

    public static class Gen3RomEntryReader<T extends Gen3RomEntry> extends GBRomEntryReader<T> {

        protected Gen3RomEntryReader() {
            super();
            putSpecialKeyMethod("TableFile", Gen3RomEntry::setTableFile);
            putSpecialKeyMethod("CopyStaticPokemon", Gen3RomEntry::setCopyStaticPokemon);
            putSpecialKeyMethod("StaticPokemon{}", Gen3RomEntry::addStaticPokemon);
            putSpecialKeyMethod("RoamingPokemon{}", Gen3RomEntry::addRoamingPokemon);
            putSpecialKeyMethod("StarterText[]", Gen3RomEntry::addStarterText);
            putSpecialKeyMethod("TMText[]", Gen3RomEntry::addTMText);
            putSpecialKeyMethod("MoveTutorText[]", Gen3RomEntry::addMoveTutorText);
            putSpecialKeyMethod("TMMovesReusableFunctionOffset", Gen3RomEntry::setTMMovesReusableFunctionOffset);
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
            return (T) new Gen3RomEntry(name);
        }

        protected static Gen3RomHandler.StaticPokemon parseStaticPokemon(String staticPokemonString) {
            int[] speciesOffsets = new int[0];
            int[] levelOffsets = new int[0];
            String pattern = "[A-z]+=\\[(0x[0-9a-fA-F]+,?\\s?)+]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(staticPokemonString);
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
            return new Gen3RomHandler.StaticPokemon(speciesOffsets, levelOffsets);
        }

        protected static Gen3EventTextEntry parseEventTextEntry(String s) {
            if (s.startsWith("[") && s.endsWith("]")) {
                String[] parts = s.substring(1, s.length() - 1).split(",", 6);
                int id = IniEntryReader.parseInt(parts[0]);
                int mapBank = IniEntryReader.parseInt(parts[1]);
                int mapNumber = IniEntryReader.parseInt(parts[2]);
                int personNum = IniEntryReader.parseInt(parts[3]);
                int[] offsetInScript = parseOffsetInScript(parts[4]);
                String template = parts[5];
                return new Gen3EventTextEntry(id, mapBank, mapNumber, personNum, offsetInScript, template);
            }
            return null;
        }

        protected static int[] parseOffsetInScript(String s) {
            if (s.startsWith("[") && s.endsWith("]")) {
                String[] parts = s.substring(1, s.length() - 1).split(";");
                int[] offsetInScript = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    offsetInScript[i] = parseInt(parts[i]);
                }
                return offsetInScript;
            } else {
                return new int[]{parseInt(s)};
            }
        }
    }

    public static final Gen3RomEntryReader<Gen3RomEntry> READER = new Gen3RomEntryReader<>();

    private String tableFile; // really same as "extraTableFile" used by the Gen1/2 games...
    private boolean copyStaticPokemon;
    private final List<Gen3RomHandler.StaticPokemon> staticPokemon = new ArrayList<>();
    private final List<Gen3RomHandler.StaticPokemon> roamingPokemon = new ArrayList<>();
    private final List<Gen3EventTextEntry> starterTexts = new ArrayList<>();
    private final List<Gen3EventTextEntry> tmTexts = new ArrayList<>();
    private final List<Gen3EventTextEntry> moveTutorTexts = new ArrayList<>();

    private Gen3RomEntry(String name) {
        super(name);
    }

    public Gen3RomEntry(Gen3RomEntry original) {
        super(original);
        this.tableFile = original.tableFile;
        this.copyStaticPokemon = original.copyStaticPokemon;
        staticPokemon.addAll(original.staticPokemon);
        roamingPokemon.addAll(original.roamingPokemon);
        starterTexts.addAll(original.starterTexts);
        tmTexts.addAll(original.tmTexts);
        moveTutorTexts.addAll(original.moveTutorTexts);
    }

    @Override
    protected void setRomType(String s) {
        if (s.equalsIgnoreCase("Ruby")) {
            setRomType(Gen3Constants.RomType_Ruby);
        } else if (s.equalsIgnoreCase("Sapp")) {
            setRomType(Gen3Constants.RomType_Sapp);
        } else if (s.equalsIgnoreCase("Em")) {
            setRomType(Gen3Constants.RomType_Em);
        } else if (s.equalsIgnoreCase("FRLG")) {
            setRomType(Gen3Constants.RomType_FRLG);
        } else {
            System.err.println("unrecognised rom type: " + s);
        }
    }

    public String getTableFile() {
        return tableFile;
    }

    private void setTableFile(String tableFile) {
        this.tableFile = tableFile;
    }

    private void setCopyStaticPokemon(String s) {
        this.copyStaticPokemon = IniEntryReader.parseBoolean(s);
    }

    public List<Gen3RomHandler.StaticPokemon> getStaticPokemon() {
        return Collections.unmodifiableList(staticPokemon);
    }

    private void addStaticPokemon(String s) {
        staticPokemon.add(Gen3RomEntryReader.parseStaticPokemon(s));
    }

    public List<Gen3RomHandler.StaticPokemon> getRoamingPokemon() {
        return Collections.unmodifiableList(roamingPokemon);
    }

    private void addRoamingPokemon(String s) {
        roamingPokemon.add(Gen3RomEntryReader.parseStaticPokemon(s));
    }

    public List<Gen3EventTextEntry> getStarterTexts() {
        return starterTexts;
    }

    private void addStarterText(String s) {
        Gen3EventTextEntry ste = Gen3RomEntryReader.parseEventTextEntry(s);
        if (ste != null) {
            starterTexts.add(ste);
        }
    }

    public List<Gen3EventTextEntry> getTMTexts() {
        return Collections.unmodifiableList(tmTexts);
    }

    private void addTMText(String s) {
        Gen3EventTextEntry ete = Gen3RomEntryReader.parseEventTextEntry(s);
        if (ete != null) {
            tmTexts.add(ete);
        }
    }

    public List<Gen3EventTextEntry> getMoveTutorTexts() {
        return Collections.unmodifiableList(moveTutorTexts);
    }

    private void addMoveTutorText(String s) {
        Gen3EventTextEntry ete = Gen3RomEntryReader.parseEventTextEntry(s);
        if (ete != null) {
            moveTutorTexts.add(ete);
        }
    }

    private void setTMMovesReusableFunctionOffset(String s) {
        putArrayValue("TMMovesReusableFunctionOffsets", new int[]{RomEntryReader.parseInt(s)});
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof Gen3RomEntry) {
            Gen3RomEntry gen3Other = (Gen3RomEntry) other;
            if (copyStaticPokemon) {
                staticPokemon.addAll(gen3Other.staticPokemon);
                roamingPokemon.addAll(gen3Other.roamingPokemon);
                putIntValue("StaticPokemonSupport", 1);
            } else {
                putIntValue("StaticPokemonSupport", 0);
            }
            if (getIntValue("CopyStarterText") == 1) {
                starterTexts.addAll(gen3Other.starterTexts);
            }
            if (getIntValue("CopyTMText") == 1) {
                tmTexts.addAll(gen3Other.tmTexts);
                moveTutorTexts.addAll(gen3Other.moveTutorTexts);
            }
            tableFile = gen3Other.tableFile;
        }
    }

}
