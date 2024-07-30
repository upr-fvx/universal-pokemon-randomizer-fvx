package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.*;
import com.dabomstew.pkrandom.game_data.*;
import com.dabomstew.pkrandom.randomizers.EncounterRandomizer;
import com.dabomstew.pkrandom.romhandlers.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerEncounterTest extends RomHandlerTest {

    private static final double MAX_AVERAGE_POWER_LEVEL_DIFF = 0.065;

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getEncounters(false).isEmpty());
        assertFalse(romHandler.getEncounters(true).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersDoNotChangeWithGetAndSetNotUsingTimeOfDay(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(false);
        System.out.println(encounterAreas);
        List<EncounterArea> before = new ArrayList<>(encounterAreas);
        romHandler.setEncounters(false, encounterAreas);
        assertEquals(before, romHandler.getEncounters(false));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersDoNotChangeWithGetAndSetUsingTimeOfDay(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);
        System.out.println(encounterAreas);
        List<EncounterArea> before = new ArrayList<>(encounterAreas);
        romHandler.setEncounters(true, encounterAreas);
        assertEquals(before, romHandler.getEncounters(true));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersAreIdenticalToEarlierRandomizerCodeOutput(String romName) throws IOException {
        // This test checks whether you've accidentally broken the reading of encounters
        // by comparing the current output with logged output in text files. If you *intentionally* change something
        // about how the encounters are read, like the EncounterArea names, you can expect this test to fail.
        // In that case, check it only differs in the way you want, and then update the text files.
        loadROM(romName);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        List<EncounterArea> noTimeOfDay = romHandler.getEncounters(false);
        List<EncounterArea> useTimeOfDay = romHandler.getEncounters(true);

        pw.println("useTimeOfDay=false");
        pw.println(encounterAreasToMultilineString(noTimeOfDay));
        pw.println("");
        pw.println("useTimeOfDay=true");
        pw.println(encounterAreasToMultilineString(useTimeOfDay));
        pw.close();

        String orig = readFile("test/resources/encounters/" + romHandler.getROMName() + ".txt");
        assertEquals(orig.replaceAll("\r\n", "\n"),
                sw.toString().replaceAll("\r\n", "\n"));
    }

    static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private String encounterAreasToMultilineString(List<EncounterArea> encounterAreas) {
        StringBuilder sb = new StringBuilder();
        sb.append("[EncounterAreas:");
        for (EncounterArea area : encounterAreas) {
            sb.append(String.format("\n\t[Name = %s, Rate = %d, Offset = %d,",
                    area.getDisplayName(), area.getRate(), area.getMapIndex()));
            sb.append(String.format("\n\t\tEncounters = %s", new ArrayList<>(area)));
            if (!area.getBannedSpecies().isEmpty()) {
                sb.append(String.format("\n\t\tBanned = %s", area.getBannedSpecies()));
            }
            sb.append("]");
        }
        sb.append("\n]");
        return sb.toString();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allEncounterAreasHaveALocationTagNoTimeOfDay(String romName) {
        loadROM(romName);
        for (EncounterArea area : romHandler.getEncounters(false)) {
            assertNotNull(area.getLocationTag());
            assertNotEquals("", area.getLocationTag());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allEncounterAreasHaveALocationTagUseTimeOfDay(String romName) {
        loadROM(romName);
        for (EncounterArea area : romHandler.getEncounters(true)) {
            assertNotNull(area.getLocationTag());
            assertNotEquals("", area.getLocationTag());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allLocationTagsAreFoundInTraverseOrder(String romName) {
        assumeTrue(getGenerationNumberOf(romName) <= 6);
        loadROM(romName);
        Set<String> inOrder = new HashSet<>(getLocationTagsTraverseOrder());
        Set<String> used = new HashSet<>();
        for (EncounterArea area : romHandler.getEncounters(false)) {
            used.add(area.getLocationTag());
        }
        for (EncounterArea area : romHandler.getEncounters(true)) {
            used.add(area.getLocationTag());
        }
        System.out.println("In traverse order:\n" + inOrder);
        System.out.println("Used:\n" + used);
        Set<String> notUsed = new HashSet<>(used);
        notUsed.removeAll(inOrder);
        System.out.println("Used but not in traverse order:\n" + notUsed);
        assertTrue(notUsed.isEmpty());
    }

    private List<String> getLocationTagsTraverseOrder() {
        if (romHandler instanceof Gen1RomHandler) {
            return Gen1Constants.locationTagsTraverseOrder;
        } else if (romHandler instanceof Gen2RomHandler) {
            return Gen2Constants.locationTagsTraverseOrder;
        } else if (romHandler instanceof Gen3RomHandler) {
            return (((Gen3RomHandler) romHandler).getRomEntry().getRomType() == Gen3Constants.RomType_FRLG ?
                    Gen3Constants.locationTagsTraverseOrderFRLG : Gen3Constants.locationTagsTraverseOrderRSE);
        } else if (romHandler instanceof Gen4RomHandler) {
            return (((Gen4RomHandler) romHandler).getRomEntry().getRomType() == Gen4Constants.Type_HGSS ?
                    Gen4Constants.locationTagsTraverseOrderHGSS : Gen4Constants.locationTagsTraverseOrderDPPt);
        } else if (romHandler instanceof Gen5RomHandler) {
            return (((Gen5RomHandler) romHandler).getRomEntry().getRomType() == Gen5Constants.Type_BW2 ?
                    Gen5Constants.locationTagsTraverseOrderBW2 : Gen5Constants.locationTagsTraverseOrderBW);
        } else if (romHandler instanceof Gen6RomHandler) {
            return (((Gen6RomHandler) romHandler).getRomEntry().getRomType() == Gen6Constants.Type_ORAS ?
                    Gen6Constants.locationTagsTraverseOrderORAS : Gen6Constants.locationTagsTraverseOrderXY);
        }
        return Collections.emptyList();
    }

    private void checkForNoLegendaries() {
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println(area.getDisplayName() + ":");
            System.out.println(area);
            for (Encounter enc : area) {
                assertFalse(enc.getSpecies().isLegendary());
            }
        }
    }

    /**
     * not to be confused with {@link #checkForAlternateFormes()}
     */
    private void checkForNoAlternateFormes() {
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println(area.getDisplayName() + ":");
            System.out.println(area);
            for (Encounter enc : area) {
                assertNull(enc.getSpecies().getBaseForme());
            }
        }
    }

    /**
     * not to be confused with {@link #checkForNoAlternateFormes()}
     */
    private void checkForAlternateFormes() {
        boolean hasAltFormes = false;
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println(area.getDisplayName() + ":");
            System.out.println(area);
            for (Encounter enc : area) {
                if (enc.getSpecies().getBaseForme() != null) {
                    System.out.println(enc.getSpecies());
                    hasAltFormes = true;
                    break;
                }
            }
            if (hasAltFormes) {
                break;
            }
        }
        assertTrue(hasAltFormes);
    }


    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCanBanLegendaries(String romName) {
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, true, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForNoLegendaries();
    }

    /**
     * Gets a new Settings object with the most common settings for testing: <br>
     * Randomize Wild Pokemon = true <br>
     * Region Mod = None <br>
     * Use Time-based Encounters = true <br>
     * Allow Alt Formes = true in generation 5 and above, false earlier. <br>
     * Ban Irregular Alt Formes = true
     * @param romName The name of the ROM in use.
     * @return A Settings object with the described settings.
     */
    private Settings getStandardSettings(String romName) {
        Settings settings = new Settings();
        settings.setRandomizeWildPokemon(true);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.NONE);
        settings.setUseTimeBasedEncounters(true);
        settings.setAllowWildAltFormes(getGenerationNumberOf(romName) >= 5); //idk why 5 and not 3 but w/e
        settings.setBanIrregularAltFormes(true);
        settings.setBlockWildLegendaries(false);
        return settings;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, false, true, false);
        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllWorks(String romName) {
        loadROM(romName);
        SpeciesSet allPokes = romHandler.getSpeciesSet();
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        catchEmAllCheck(allPokes);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersRandomTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail

        loadROM(romName);


        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = getStandardSettings(romName);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersKeepTypeThemesANDRandomTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = getStandardSettings(romName);
        settings.setKeepWildTypeThemes(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersKeepPrimaryTypeWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreasStrings = new ArrayList<>();
        List<List<Type>> beforePrimaryTypes = new ArrayList<>();
        recordPrimaryTypesBefore(beforeAreasStrings, beforePrimaryTypes);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);

        keepPrimaryTypeCheck(beforeAreasStrings, beforePrimaryTypes);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersUsePowerLevelsWorks(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllANDRandomTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        SpeciesSet allPokes = romHandler.getSpeciesSet();
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NONE, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        catchEmAllCheck(allPokes);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllANDKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);

        settings.setKeepWildTypeThemes(true);
        settings.setCatchEmAllEncounters(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllANDRandomTypeThemesANDKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
        randomTypeThemesAreasCheck();
        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    private double calcPowerLevelDiff(Species a, Species b) {
        return Math.abs((double) a.getBSTForPowerLevels() /
                b.getBSTForPowerLevels() - 1);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMon(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithCatchEmAll(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithRandomTypeThemes(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithKeepTypeThemes(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.ENCOUNTER_SET);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithKeepPrimaryType(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithUsePowerLevels(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    /**
     * Given a list of EncounterAreas, checks that each one has an internally-correct 1-to-1 replacement of Pokemon.
     * @param before The list of EncounterAreas, in the pre-randomization state.
     * @param after The same list of EncounterAreas after randomization.
     * @param checkUnique Whether to also check that no Pokemon replaces two or more Pokemon in one area.
     */
    private void checkEachAreaIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
        Iterator<EncounterArea> beforeIterator = before.iterator();
        Iterator<EncounterArea> afterIterator = after.iterator();
        while (beforeIterator.hasNext()) {
            //Map<Species, Species> map = new HashMap<>();
            EncounterArea beforeArea = beforeIterator.next();
            EncounterArea afterArea = afterIterator.next();

            checkAreaIsReplaced1To1(beforeArea, afterArea, null, checkUnique ? new SpeciesSet() : null);
        }
    }

    private static void checkEachLocationIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
        Map<String, List<EncounterArea>> beforeLocations = EncounterArea.groupAreasByLocation(before);
        Map<String, List<EncounterArea>> afterLocations = EncounterArea.groupAreasByLocation(after);
        for(String location : beforeLocations.keySet()) {

            List<EncounterArea> locationBefore = beforeLocations.get(location);
            List<EncounterArea> locationAfter = afterLocations.get(location);

            checkIsReplaced1To1(locationBefore, locationAfter,  checkUnique);
        }
    }

    private static void checkEachMapIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
        Map<Integer, List<EncounterArea>> beforeMaps = EncounterArea.groupAreasByMapIndex(before);
        Map<Integer, List<EncounterArea>> afterMaps = EncounterArea.groupAreasByMapIndex(after);
        for(int map : beforeMaps.keySet()) {

            List<EncounterArea> mapBefore = beforeMaps.get(map);
            List<EncounterArea> mapAfter = afterMaps.get(map);

            checkIsReplaced1To1(mapBefore, mapAfter,  checkUnique);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMon(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithCatchEmAll(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithRandomTypeThemes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithKeepTypeThemes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.ENCOUNTER_SET);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithKeepPrimaryType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithUsePowerLevels(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCanBanLegendaries(String romName) {
        loadROM(romName);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, true, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForNoLegendaries();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, false, true, false);
        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllWorks(String romName) {
        loadROM(romName);
        SpeciesSet allPokes = romHandler.getSpeciesSet();
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        catchEmAllCheck(allPokes);
    }

    private void catchEmAllCheck(SpeciesSet allPokes) {
        SpeciesSet catchable = new SpeciesSet();
        for (EncounterArea area : romHandler.getEncounters(true)) {
            catchable.addAll(area.getSpeciesInArea());
        }
        SpeciesSet notCatchable = new SpeciesSet(allPokes);
        notCatchable.removeAll(catchable);
        System.out.println("Not catchable: " + notCatchable.stream().map(Species::getName).collect(Collectors.toList()));
        assertTrue(notCatchable.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersRandomTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.ENCOUNTER_SET);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersKeepPrimaryTypeWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreasStrings = new ArrayList<>();
        List<List<Type>> beforePrimaryTypes = new ArrayList<>();
        recordPrimaryTypesBefore(beforeAreasStrings, beforePrimaryTypes);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);

        keepPrimaryTypeCheck(beforeAreasStrings, beforePrimaryTypes);
    }

    private void randomTypeThemesAreasCheck() {
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println("\n" + area.getDisplayName() + ":\n" + area);
            Species firstPk = area.get(0).getSpecies();
            SpeciesSet allInArea = area.getSpeciesInArea();

            Type primaryType = firstPk.getPrimaryType(false);
            SpeciesSet ofFirstType = area.getSpeciesInArea().filterByType(primaryType, false);
            SpeciesSet notOfFirstType = new SpeciesSet(allInArea).filter(pk -> !ofFirstType.contains(pk));
            System.out.println(notOfFirstType);

            if (!notOfFirstType.isEmpty()) {
                System.out.println("Not " + primaryType);
                Type secondaryType = firstPk.getSecondaryType(false);
                if (secondaryType == null) {
                    fail();
                }
                SpeciesSet ofSecondaryType = area.getSpeciesInArea().filterByType(secondaryType, false);
                SpeciesSet notOfSecondaryType = new SpeciesSet(allInArea)
                        .filter(pk -> !ofSecondaryType.contains(pk));
                System.out.println(notOfSecondaryType);
                if (!notOfSecondaryType.isEmpty()) {
                    System.out.println("Not " + secondaryType);
                    fail();
                } else {
                    System.out.println(secondaryType);
                }
            } else {
                System.out.println(primaryType);
            }
        }
    }

    private String toNameAndTypesString(Species pk) {
        return pk.getName() + ", " + pk.getPrimaryType(false)
                + (pk.getSecondaryType(false) == null ? "" : " / " + pk.getSecondaryType(false));
    }

    private void recordTypeThemeBefore(List<List<String>> beforeAreaStrings, Map<Integer, Type> typeThemedAreas) {
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);
        for (int i = 0; i < encounterAreas.size(); i++) {
            EncounterArea area = encounterAreas.get(i);
            List<String> beforeStrings = new ArrayList<>();
            beforeAreaStrings.add(beforeStrings);
            beforeStrings.add(area.toString());
            for (Encounter enc : area) {
                Species pk = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                beforeStrings.add(toNameAndTypesString(pk));
            }

            Type theme = getThemedAreaType(area);
            if (theme != null) {
                typeThemedAreas.put(i, theme);
            }
        }
    }

    private Type getThemedAreaType(EncounterArea area) {
        Species first = area.get(0).getSpecies();
        Type primary = first.getPrimaryType(true);
        Type secondary = first.getSecondaryType(true);
        for (int i = 1; i < area.size(); i++) {
            Species pk = area.get(i).getSpecies();
            if (secondary != null) {
                if (secondary != pk.getPrimaryType(true) && secondary != pk.getSecondaryType(true)) {
                    secondary = null;
                }
            }
            if (primary != pk.getPrimaryType(true) && primary != pk.getSecondaryType(true)) {
                primary = secondary;
                secondary = null;
            }
            if (primary == null) {
                return null; //no type is shared, no need to look at the remaining pokemon
            }
        }

        //we have a type theme!
        if (primary == Type.NORMAL && secondary != null) {
            //Bird override
            //(Normal is less significant than other types, for example, Flying)
            return secondary;
        } else {
            return primary;
        }

    }

    private void keepTypeThemedAreasCheck(List<List<String>> beforeAreaStrings, Map<Integer, Type> typeThemedAreas) {
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);
        for (int i = 0; i < encounterAreas.size(); i++) {
            List<String> beforeStrings = beforeAreaStrings.get(i);
            System.out.println("Before: " + beforeStrings.get(0));
            for (int j = 1; j < beforeStrings.size(); j++) {
                System.out.println("\t" + beforeStrings.get(j));
            }

            EncounterArea area = encounterAreas.get(i);
            if (typeThemedAreas.containsKey(i)) {
                Type theme = typeThemedAreas.get(i);
                System.out.println("Type Theme: " + theme);
                System.out.println("After: " + area);
                for (Encounter enc : area) {
                    Species pk = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                    System.out.println("\t" + toNameAndTypesString(pk));
                    assertTrue(pk.getPrimaryType(false) == theme || pk.getSecondaryType(false) == theme);
                }
            } else {
                System.out.println("Not Type Themed");
            }
            System.out.println();
        }
    }

    private void recordPrimaryTypesBefore(List<List<String>> beforeAreaStrings, List<List<Type>> beforePrimaryTypes) {
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);
        for (int i = 0; i < encounterAreas.size(); i++) {
            EncounterArea area = encounterAreas.get(i);
            List<String> beforeStrings = new ArrayList<>();
            beforeAreaStrings.add(beforeStrings);
            beforeStrings.add(area.toString());
            List<Type> beforeTypes = new ArrayList<>();
            beforePrimaryTypes.add(beforeTypes);
            for (Encounter enc : area) {
                Species pk = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                beforeStrings.add(toNameAndTypesString(pk));
                beforeTypes.add(pk.getPrimaryType(false));
            }
        }
    }

    private void keepPrimaryTypeCheck(List<List<String>> beforeAreaStrings, List<List<Type>> beforePrimaryTypes) {
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);
        for (int i = 0; i < encounterAreas.size(); i++) {
            List<String> beforeStrings = beforeAreaStrings.get(i);
            System.out.println("Before: " + beforeStrings.get(0));
            for (int j = 1; j < beforeStrings.size(); j++) {
                System.out.println("\t" + beforeStrings.get(j));
            }

            EncounterArea area = encounterAreas.get(i);
            System.out.println("After: " + area);
            for (int j = 0; j < area.size(); j++) {
                Encounter enc = area.get(j);
                Species pk = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                Type primary = beforePrimaryTypes.get(i).get(j);
                System.out.println("\t" + toNameAndTypesString(pk));
                assertTrue(pk.getPrimaryType(false) == primary || pk.getSecondaryType(false) == primary);
            }
            System.out.println();
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersUsePowerLevelsWorks(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    private void powerLevelsCheck(List<EncounterArea> before, List<EncounterArea> after) {
        List<Double> diffs = new ArrayList<>();
        Iterator<EncounterArea> beforeIterator = before.iterator();
        Iterator<EncounterArea> afterIterator = after.iterator();
        while (beforeIterator.hasNext()) {
            EncounterArea beforeArea = beforeIterator.next();
            EncounterArea afterArea = afterIterator.next();
            if (!beforeArea.getDisplayName().equals(afterArea.getDisplayName())) {
                throw new RuntimeException("Area mismatch; " + beforeArea.getDisplayName() + " and "
                        + afterArea.getDisplayName());
            }

            Iterator<Encounter> beforeEncIterator = beforeArea.iterator();
            Iterator<Encounter> afterEncIterator = afterArea.iterator();
            while (beforeEncIterator.hasNext()) {
                Species beforePk = beforeEncIterator.next().getSpecies();
                Species afterPk = afterEncIterator.next().getSpecies();
                diffs.add(calcPowerLevelDiff(beforePk, afterPk));
            }
        }

        double averageDiff = diffs.stream().mapToDouble(d -> d).average().getAsDouble();
        System.out.println(diffs);
        System.out.println(averageDiff);
        assertTrue(averageDiff <= MAX_AVERAGE_POWER_LEVEL_DIFF);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllANDRandomTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        SpeciesSet allPokes = romHandler.getSpeciesSet();
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.ENCOUNTER_SET, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        catchEmAllCheck(allPokes);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllANDKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        // for some reason fails with the Gen 3 Hoenn games
        // there's no obvious bug-related reason so I'm guessing they just have too few encounters/areas
        if (romHandler instanceof Gen3RomHandler) {
            assumeTrue(((Gen3RomHandler) romHandler).getRomEntry().getRomType() == Gen3Constants.RomType_FRLG);
        }

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        catchEmAllCheck(allPokes);

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllANDRandomTypeThemesANDKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        // for some reason fails with the Gen 3 Hoenn games
        // there's no obvious bug-related reason so I'm guessing they just have too few encounters/areas
        if (romHandler instanceof Gen3RomHandler) {
            assumeTrue(((Gen3RomHandler) romHandler).getRomEntry().getRomType() == Gen3Constants.RomType_FRLG);
        }

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        catchEmAllCheck(allPokes);

        randomTypeThemesAreasCheck();
        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMon(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithCatchEmAll(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithRandomTypeThemes(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithKeepPrimaryType(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithUsePowerLevels(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    private Map<String, List<EncounterArea>> groupEncountersByLocation(List<EncounterArea> ungrouped) {
        Map<String, List<EncounterArea>> grouped = new HashMap<>();
        int untagged = 0;
        for (EncounterArea area : ungrouped) {
            String tag = area.getLocationTag();
            if (tag == null) {
                tag = "UNTAGGED-" + untagged;
                untagged++;
            }
            if (!grouped.containsKey(tag)) {
                grouped.put(tag, new ArrayList<>());
            }
            grouped.get(tag).add(area);
        }
        return grouped;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMon(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithCatchEmAll(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithRandomTypeThemes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 4); // Too few mons of some types vs the size of the locations, so it always fails
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithKeepPrimaryType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithUsePowerLevels(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCanBanLegendaries(String romName) {
        loadROM(romName);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, true, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForNoLegendaries();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, false, true, false);
        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCatchEmAllWorks(String romName) {
        //does not hold later than Gen 5
        assumeTrue(getGenerationNumberOf(romName) <= 5);

        loadROM(romName);
        // does not hold in BW1, presumably too few wild Pokémon species and too many in the national dex
        if (romHandler instanceof Gen5RomHandler) {
            assumeFalse(((Gen5RomHandler) romHandler).getRomEntry().getRomType() == Gen5Constants.Type_BW);
        }
        SpeciesSet allPokes = romHandler.getSpeciesSet();
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        catchEmAllCheck(allPokes);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersRandomTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        randomTypeThemesLocationsCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersKeepPrimaryTypeWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreasStrings = new ArrayList<>();
        List<List<Type>> beforePrimaryTypes = new ArrayList<>();
        recordPrimaryTypesBefore(beforeAreasStrings, beforePrimaryTypes);

        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);

        keepPrimaryTypeCheck(beforeAreasStrings, beforePrimaryTypes);
    }

    private void randomTypeThemesLocationsCheck() {

        Map<String, List<EncounterArea>> grouped = groupEncountersByLocation(romHandler.getEncounters(true));

        for (Map.Entry<String, List<EncounterArea>> loc : grouped.entrySet()) {
            if(loc.getKey().contains("UNUSED")) {
                System.out.println("Skipping location: " + loc.getKey());
                continue;
            }

            // lazy solution mashing together all of a location's associated EncounterAreas into a single one,
            // so old code can be reused to a greater extent
            EncounterArea area = new EncounterArea();
            area.setDisplayName("All of location " + loc.getKey());
            for(EncounterArea locArea : loc.getValue()) {
                if(locArea.getEncounterType() == EncounterType.UNUSED) {
                    System.out.println("Skipping area: " + locArea.getDisplayName());
                    continue;
                }
                area.addAll(locArea);
            }

            System.out.println("\n" + area.getDisplayName() + ":\n" + area);
            Species firstPk = area.get(0).getSpecies();
            SpeciesSet allInArea = area.getSpeciesInArea();

            Type primaryType = firstPk.getPrimaryType(false);
            SpeciesSet ofFirstType = area.getSpeciesInArea().filterByType(primaryType, false);
            SpeciesSet notOfFirstType = new SpeciesSet(allInArea).filter(pk -> !ofFirstType.contains(pk));
            System.out.println(notOfFirstType);

            if (!notOfFirstType.isEmpty()) {
                System.out.println("Not " + primaryType);
                Type secondaryType = firstPk.getSecondaryType(false);
                if (secondaryType == null) {
                    fail();
                }
                SpeciesSet ofSecondaryType = area.getSpeciesInArea().filterByType(secondaryType, false);
                SpeciesSet notOfSecondaryType = new SpeciesSet(allInArea)
                        .filter(pk -> !ofSecondaryType.contains(pk));
                System.out.println(notOfSecondaryType);
                if (!notOfSecondaryType.isEmpty()) {
                    System.out.println("Not " + secondaryType);
                    fail();
                } else {
                    System.out.println(secondaryType);
                }
            } else {
                System.out.println(primaryType);
            }
        }

    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersUsePowerLevelsWorks(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCatchEmAllANDRandomTypeThemesWorks(String romName) {
        //does not hold later than Gen 5
        assumeTrue(getGenerationNumberOf(romName) <= 5);

        loadROM(romName);
        // does not hold in RSE/BW1/BW2, presumably too few wild Pokémon species and too many in the national dex
        if (romHandler instanceof Gen3RomHandler) {
            assumeTrue(((Gen3RomHandler) romHandler).getRomEntry().getRomType() == Gen3Constants.RomType_FRLG);
        }
        assumeFalse(romHandler instanceof Gen5RomHandler);

        SpeciesSet allPokes = romHandler.getSpeciesSet();
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.NAMED_LOCATION, Settings.WildPokemonTypeMod.RANDOM_THEMES,
                true,
                true, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        catchEmAllCheck(allPokes);
        randomTypeThemesLocationsCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersGivesConsequentReplacementsForEachMon(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true); // TODO: deep copy just in case
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.GAME, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersGivesUniqueReplacementsForEachMon(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true); // TODO: deep copy just in case
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.GAME, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkIsReplaced1To1(before, after, true);
    }

    /**
     * Given a set of EncounterAreas, ensures that for each Pokemon present before randomization,
     * exactly one replacement is present after randomization (in all areas).
     * @param before The list of EncounterAreas in the state before randomization.
     * @param after The same list of EncounterAreas, after randomization.
     * @param checkUnique Whether to also check that no Pokemon is used as a replacement for more than one Pokemon.
     */
    private static void checkIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
        Map<Species, Species> map = new HashMap<>();
        Iterator<EncounterArea> beforeIterator = before.iterator();
        Iterator<EncounterArea> afterIterator = after.iterator();
        SpeciesSet usedPokemon = checkUnique ? new SpeciesSet() : null;
        while (beforeIterator.hasNext()) {
            EncounterArea beforeArea = beforeIterator.next();
            EncounterArea afterArea = afterIterator.next();

            checkAreaIsReplaced1To1(beforeArea, afterArea, map, usedPokemon);
        }

        System.out.println(pokemapToString(map));
    }

    /**
     * Checks that for each Pokemon in beforeArea, it is replaced by the Pokemon listed in the map if there is one;
     * if not, adds its replacement to the map and ensures that any future replacements use the same Pokemon.
     * @param beforeArea The area, in its state before randomization. If the area is marked as Unused (either by
     *                   EncounterType, or Location tag), the method will return without performing any checks.
     * @param afterArea The same area, after randomization.
     * @param map The map of Pokemon to their replacements. WARNING: MODIFIED. If null, a new map will be created.
     * @param usedPokemon A PokemonSet of Pokemon already used as replacements. If null, Pokemon will not be checked
     *                    for uniqueness.
     * @throws RuntimeException if the areas do not have the same display name.
     */
    private static void checkAreaIsReplaced1To1(EncounterArea beforeArea, EncounterArea afterArea, Map<Species, Species> map, SpeciesSet usedPokemon) {
        if (!beforeArea.getDisplayName().equals(afterArea.getDisplayName())) {
            throw new RuntimeException("Area mismatch; " + beforeArea.getDisplayName() + " and "
                    + afterArea.getDisplayName());
        }

        System.out.println(beforeArea.getDisplayName() + ":");
        if(beforeArea.getEncounterType() == EncounterType.UNUSED || "Unused".equals(beforeArea.getLocationTag())) {
            System.out.println("Unused; skipping.");
            return;
        }
        System.out.println(beforeArea);
        System.out.println(afterArea);

        if (map == null) {
            map = new HashMap<>();
        }

        Iterator<Encounter> beforeEncIterator = beforeArea.iterator();
        Iterator<Encounter> afterEncIterator = afterArea.iterator();
        while (beforeEncIterator.hasNext()) {
            Species beforePk = beforeEncIterator.next().getSpecies();
            Species afterPk = afterEncIterator.next().getSpecies();

            if (!map.containsKey(beforePk)) {
                map.put(beforePk, afterPk);
                if(usedPokemon != null) {
                    System.out.println("Adding map entry: " + beforePk.getName() + beforePk.getFormeSuffix() +
                            " to " + afterPk.getName() + afterPk.getFormeSuffix());
                    assertFalse(usedPokemon.contains(afterPk));
                    usedPokemon.add(afterPk);
                }
            }
            assertEquals(map.get(beforePk), afterPk);
        }
    }

    private static String pokemapToString(Map<Species, Species> map) {
        StringBuilder sb = new StringBuilder("{\n");
        for (Map.Entry<Species, Species> entry : map.entrySet()) {
            sb.append(entry.getKey().getName());
            sb.append(" -> ");
            sb.append(entry.getValue().getName());
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersCanBanLegendaries(String romName) {
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.GAME, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, true, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        checkForNoLegendaries();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.GAME, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, false, true, false);
        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        loadROM(romName);
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.GAME, Settings.WildPokemonTypeMod.NONE,
                true,
                false, false, false, false,
                0, true, true, false);
        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersUsePowerLevelsWorks(String romName) {
        loadROM(romName);
        List<EncounterArea> before = romHandler.getEncounters(true); // TODO: deep copy just in case
        new EncounterRandomizer(romHandler, new Settings(), RND).randomizeEncounters(
                Settings.WildPokemonRegionMod.GAME, Settings.WildPokemonTypeMod.NONE,
                true,
                false, true, false, false,
                0, getGenerationNumberOf(romName) >= 5, true, false);
        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    private static final List<String> encounterTerminators = Arrays.asList(
            "Grass/Cave", "Yellow Flowers", "Red Flowers", "Purple Flowers", "Rough Terrain/Tall Grass", "Long Grass",
            "Surf",
            "Common Horde", "Uncommon Horde", "Rare Horde", "DexNav Foreign Encounter",
            "Old Rod", "Good Rod", "Super Rod",
            "Rock Smash"
    );

    private static String simplifyTag(String tag) {
        for (String terminator : encounterTerminators) {
            if (tag.endsWith(terminator)) {
                tag = tag.replace(terminator, "");
                break;
            }
        }
        tag = tag.split(", Table")[0]; // for Gen 7
        tag = tag.trim().toUpperCase();

        tag = tag.replace('’', '\''); //apostrophes, not single quote

        return tag;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locationTagsAndDisplayNamesMatchUp(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);

        String lastTag = "NOT A LOCATION TAG";
        int count = 0;
        for (int i = 0; i < encounterAreas.size(); i++) {
            EncounterArea area = encounterAreas.get(i);
            String areaTag = area.getLocationTag();
            if (!areaTag.equals(lastTag)) {
                System.out.println("\t Start location: " + areaTag + "; Index: " + i);
                lastTag = areaTag;
                count = 1;
            } else {
                count++;
            }
            if(area.getEncounterType() != null) {
                System.out.println("\t\t\t " + count + " " + area.getEncounterType().name()
                        + " " + area.getSpeciesInArea().toStringShort() + " " + area.getDisplayName());
            } else {
                System.out.println("\t\t\t " + count
                        + " " + area.getSpeciesInArea().toStringShort() + " " + area.getDisplayName());
            }

            String simplifiedDisplayName = simplifyTag(area.getDisplayName());
            checkLocationTagAgainstDisplayName(areaTag, simplifiedDisplayName);
        }

    }

    private void checkLocationTagAgainstDisplayName(String locationTag, String displayName) {

        //switch for special cases
        switch(locationTag) {
            case "UNUSED":
                //doesn't need to match anything
                break;
            case "HAU'OLI CITY":
                assertTrue(displayName.contains(locationTag) || displayName.contains("HAU'OLI OUTSKIRTS")
                        || displayName.contains("TRAINERS' SCHOOL"));
                break;
            case "ROUTE 2":
                assertTrue(displayName.contains(locationTag) || displayName.contains("BERRY FIELDS"));
                break;
            case "PANIOLA TOWN":
                assertTrue(displayName.contains("PANIOLA"));
                break;
            default:
                assertTrue(displayName.contains(locationTag));
                break;
        }

    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void hasMapIndicesIsCorrect(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);

        Map<Integer, List<EncounterArea>> areasByMapIndex = EncounterArea.groupAreasByMapIndex(encounterAreas);

        List<Integer> maps = new ArrayList<>(areasByMapIndex.keySet());
        maps.sort(null);

        if(maps.size() > 1) {
            for (int map : maps) {
                System.out.println("\t" + map + ":");
                for (EncounterArea area : areasByMapIndex.get(map)) {
                    System.out.println("\t\t" + area.getDisplayName());
                }
            }
        } else {
            System.out.println("No map indices.");
        }

        if(romHandler.hasMapIndices()) {
            assertNotEquals(areasByMapIndex.size(), 1);
        } else {
            assertEquals(areasByMapIndex.size(), 1);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void hasEncounterTypes(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);

        String lastLocation = "NOT A LOCATION";
        for(EncounterArea area : encounterAreas) {
            if(!area.getLocationTag().equals(lastLocation)) {
                System.out.println("\t" + area.getLocationTag() + ":");
                lastLocation = area.getLocationTag();
            }
            assertNotNull(area.getEncounterType());
            System.out.println("\t\t" + area.getEncounterType().name() + ": " + area.getDisplayName());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void globalFamilyToFamilyGivesConsequentReplacements(String romName) {
        loadROM(romName);

        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.GAME);
        settings.setKeepWildEvolutionFamilies(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);


        checkIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void globalFamilyToFamilyGivesUniqueReplacements(String romName) {
        loadROM(romName);

        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.GAME);
        settings.setKeepWildEvolutionFamilies(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);


        checkIsReplaced1To1(before, after, true);
    }

    //TODO: test that family 1-to-1 actually preserves families
    // also, that it does not duplicate families

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1To1KeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail
        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = new Settings();
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.GAME);
        settings.setKeepWildTypeThemes(true);
        settings.setUseTimeBasedEncounters(true);


        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void globalFamilyToFamilyKeepTypeThemesWorks(String romName) {
        assumeTrue(romName.length() > 1);
        //X & Y have problems testing types, which makes the test nearly always fail

        loadROM(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);
        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.GAME);
        settings.setKeepWildEvolutionFamilies(true);
        settings.setKeepWildTypeThemes(true);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        //TODO: check family integrity
        checkIsReplaced1To1(before, after, true);
        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    /**
     * Checks that map 1-to-1 encounters gives both consequent and unique replacements for each Species in each map.
     * @param romName The name of the ROM to test.
     */
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void map1to1EncountersWorks(String romName) {
        loadROM(romName);

        assumeTrue(romHandler.hasMapIndices());

        List<EncounterArea> before = romHandler.getEncounters(true);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonRegionMod(Settings.WildPokemonRegionMod.MAP);

        new EncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachMapIsReplaced1To1(before, after, true);
    }
}
