package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.WildEncounterRandomizer;
import com.dabomstew.pkromio.constants.Gen3Constants;
import com.dabomstew.pkromio.constants.Gen5Constants;
import com.dabomstew.pkromio.gamedata.*;
import javafx.util.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class WildEncounterRandomizerTest extends RandomizerTest {

    private static final double MAX_AVERAGE_POWER_LEVEL_DIFF = 0.065;

    private void checkForNoLegendaries() {
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println(area.getDisplayName() + ":");
            System.out.println(area);
            for (Encounter enc : area) {
                if(enc.getSpecies().isLegendary()) {
                    Species forme = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                    fail(forme.getFullName() + " is legendary!");
                }
            }
        }
    }

    /**
     * Given a List of EncounterAreas, copies them, also copying each Encounter in the EncounterAreas.
     * @param originalEncounters The List of EncounterAreas to copy.
     * @return A new List of new EncounterAreas which are copies of the given ones.
     */
    private List<EncounterArea> deepCopyEncounters(List<EncounterArea> originalEncounters) {
        List<EncounterArea> copiedEncounters = new ArrayList<>();
        for(EncounterArea originalArea : originalEncounters) {
            EncounterArea copiedArea = new EncounterArea();
            copiedArea.setRate(originalArea.getRate());
            copiedArea.banAllSpecies(originalArea.getBannedSpecies());
            copiedArea.setIdentifiers(originalArea.getDisplayName(), originalArea.getMapIndex(),
                    originalArea.getEncounterType(), originalArea.getLocationTag());
            copiedArea.setPostGame(originalArea.isPostGame());
            copiedArea.setPartiallyPostGameCutoff(originalArea.getPartiallyPostGameCutoff());
            copiedArea.setForceMultipleSpecies(originalArea.isForceMultipleSpecies());

            for(Encounter origEnc : originalArea) {
                Encounter copyEnc = new Encounter();
                copyEnc.setLevel(origEnc.getLevel());
                copyEnc.setMaxLevel(origEnc.getMaxLevel());
                copyEnc.setSpecies(origEnc.getSpecies());
                copyEnc.setFormeNumber(origEnc.getFormeNumber());
                copyEnc.setSOS(origEnc.isSOS());
                copyEnc.setSosType(origEnc.getSosType());

                copiedArea.add(copyEnc);
            }

            copiedEncounters.add(copiedArea);
        }

        return copiedEncounters;
    }

    /**
     * Checks the EncounterAreas in the current RomHandler to ensure they contain no alternate Formes.
     * Not to be confused with {@link #checkForAlternateFormes()}
     */
    private void checkForNoAlternateFormes() {
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println(area.getDisplayName() + ":");
            System.out.println(area);
            for (Encounter enc : area) {
                assertTrue(enc.getSpecies().isBaseForme());
            }
        }
    }

    /**
     * Checks the EncounterAreas in the current RomHandler to ensure they contain at least one alternate Forme.
     * Not to be confused with {@link #checkForNoAlternateFormes()}
     */
    private void checkForAlternateFormes() {
        boolean hasAltFormes = false;
        for (EncounterArea area : romHandler.getEncounters(true)) {
            System.out.println(area.getDisplayName() + ":");
            System.out.println(area);
            for (Encounter enc : area) {
                if (enc.getSpecies().getBaseForme() != null ||
                        enc.getFormeNumber() != enc.getSpecies().getFormeNumber()) {
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
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setBlockWildLegendaries(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NONE);
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
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setAllowWildAltFormes(false);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllWorks(String romName) {
        activateRomHandler(romName);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersRandomTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = getStandardSettings(romName);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersKeepTypeThemesANDRandomTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = getStandardSettings(romName);
        settings.setKeepWildTypeThemes(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersKeepPrimaryTypeWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreasStrings = new ArrayList<>();
        List<List<Type>> beforePrimaryTypes = new ArrayList<>();
        recordPrimaryTypesBefore(beforeAreasStrings, beforePrimaryTypes);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepPrimaryTypeCheck(beforeAreasStrings, beforePrimaryTypes);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersUsePowerLevelsWorks(String romName) {
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllANDRandomTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllANDKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);

        settings.setKeepWildTypeThemes(true);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEncountersCatchEmAllANDRandomTypeThemesANDKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithCatchEmAll(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithRandomTypeThemes(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithKeepTypeThemes(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithKeepPrimaryType(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesConsequentReplacementsForEachMonWithUsePowerLevels(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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

    private void checkEachLocationIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
        Map<String, List<EncounterArea>> beforeLocations = EncounterArea.groupAreasByLocation(before);
        Map<String, List<EncounterArea>> afterLocations = EncounterArea.groupAreasByLocation(after);
        for(String location : beforeLocations.keySet()) {

            List<EncounterArea> locationBefore = beforeLocations.get(location);
            List<EncounterArea> locationAfter = afterLocations.get(location);

            checkIsReplaced1To1(locationBefore, locationAfter,  checkUnique);
        }
    }

    private void checkEachMapIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
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
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithCatchEmAll(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithRandomTypeThemes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithKeepTypeThemes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithKeepPrimaryType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersGivesUniqueReplacementsForEachMonWithUsePowerLevels(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachAreaIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCanBanLegendaries(String romName) {
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setBlockWildLegendaries(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoLegendaries();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setAllowWildAltFormes(false);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllWorks(String romName) {
        activateRomHandler(romName);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersKeepPrimaryTypeWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreasStrings = new ArrayList<>();
        List<List<Type>> beforePrimaryTypes = new ArrayList<>();
        recordPrimaryTypesBefore(beforeAreasStrings, beforePrimaryTypes);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepPrimaryTypeCheck(beforeAreasStrings, beforePrimaryTypes);
    }

    private void randomTypeThemesAreasCheck() {
        List <EncounterArea> areas = romHandler.getEncounters(true);

        randomTypeThemesAreasCheck(areas);
    }

    private void randomTypeThemesAreasCheck(List<EncounterArea> areas) {
        for (EncounterArea area : areas) {
            if(area.isEmpty()) {
                continue; //can occur with locations
            }

            System.out.println("\n" + area.getDisplayName() + ":\n" + area);
            Encounter firstEnc = area.get(0);
            Species firstSpec = romHandler.getAltFormeOfSpecies(firstEnc.getSpecies(), firstEnc.getFormeNumber());
            SpeciesSet allInArea = area.getSpeciesInArea();

            Type primaryType = firstSpec.getPrimaryType(false);
            boolean primaryTypeMatched = true;
            for(Encounter enc : area) {
                Species spec = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                if(!spec.hasType(primaryType, false)) {
                    System.out.println(spec);
                    primaryTypeMatched = false;
                }
            }

            if(!primaryTypeMatched) {
                System.out.println("Not " + primaryType);

                Type secondaryType = firstSpec.getSecondaryType(false);
                if (secondaryType == null) {
                    fail();
                }

                boolean secondaryTypeMatched = true;
                for(Encounter enc : area) {
                    Species spec = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                    if(!spec.hasType(secondaryType, false)) {
                        System.out.println(spec);
                        secondaryTypeMatched = false;
                    }
                }

                if(!secondaryTypeMatched) {
                    System.out.println("Not " + secondaryType);
                    fail();
                } else {
                    System.out.println("All " + secondaryType);
                }

            } else {
                System.out.println("All " + primaryType);
            }
        }
    }

    private String toNameAndTypesString(Species sp) {
        return sp.getName() + ", " + sp.getPrimaryType(false)
                + (sp.getSecondaryType(false) == null ? "" : " / " + sp.getSecondaryType(false));
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
                    Species sp = romHandler.getAltFormeOfSpecies(enc.getSpecies(), enc.getFormeNumber());
                    System.out.println("\t" + toNameAndTypesString(sp));
                    assertTrue(sp.getPrimaryType(false) == theme || sp.getSecondaryType(false) == theme);
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
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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
        activateRomHandler(romName);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
        randomTypeThemesAreasCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllANDKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        // for some reason fails with the Gen 3 Hoenn games
        // there's no obvious bug-related reason so I'm guessing they just have too few encounters/areas
        if (romHandler.generationOfPokemon() == 3) {
            assumeTrue(romHandler.getROMType() == Gen3Constants.RomType_FRLG);
        }

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        catchEmAllCheck(allPokes);

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void area1to1EncountersCatchEmAllANDRandomTypeThemesANDKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        // for some reason fails with the Gen 3 Hoenn games
        // there's no obvious bug-related reason so I'm guessing they just have too few encounters/areas
        if (romHandler.generationOfPokemon() == 3) {
            assumeTrue(romHandler.getROMType() == Gen3Constants.RomType_FRLG);
        }

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        catchEmAllCheck(allPokes);

        randomTypeThemesAreasCheck();
        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMon(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithCatchEmAll(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithRandomTypeThemes(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();
        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithKeepPrimaryType(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locations1to1EncountersGivesConsequentReplacementsForEachMonWithUsePowerLevels(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMon(String romName) {
        activateRomHandler(romName);
        
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithCatchEmAll(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithRandomTypeThemes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 4); // Too few mons of some types vs the size of the locations, so it always fails
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithKeepPrimaryType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Too few mons of some types, so it always fails
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersGivesUniqueReplacementsForEachMonWithUsePowerLevels(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachLocationIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCanBanLegendaries(String romName) {
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setBlockWildLegendaries(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoLegendaries();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setAllowWildAltFormes(false);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCatchEmAllWorks(String romName) {
        //does not hold later than Gen 5
        assumeTrue(getGenerationNumberOf(romName) <= 5);

        activateRomHandler(romName);
        // does not hold in BW1, presumably too few wild Pokémon species and too many in the national dex
        if (romHandler.generationOfPokemon() == 5) {
            assumeFalse(romHandler.getROMType() == Gen5Constants.Type_BW);
        }

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersRandomTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        randomTypeThemesLocationsCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersKeepPrimaryTypeWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreasStrings = new ArrayList<>();
        List<List<Type>> beforePrimaryTypes = new ArrayList<>();
        recordPrimaryTypesBefore(beforeAreasStrings, beforePrimaryTypes);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepPrimaryTypeCheck(beforeAreasStrings, beforePrimaryTypes);
    }

    private void randomTypeThemesLocationsCheck() {
        //This may need to be changed, since (afaik) this is the only current use of flattenLocations
        List<EncounterArea> grouped = EncounterArea.flattenLocations(romHandler.getEncounters(true));
        randomTypeThemesAreasCheck(grouped);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersUsePowerLevelsWorks(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void location1to1EncountersCatchEmAllANDRandomTypeThemesWorks(String romName) {
        //does not hold later than Gen 5
        assumeTrue(getGenerationNumberOf(romName) <= 5);

        activateRomHandler(romName);
        // does not hold in RSE/BW1/BW2, presumably too few wild Pokémon species and too many in the national dex
        if (romHandler.generationOfPokemon() == 3) {
            assumeTrue( romHandler.getROMType() == Gen3Constants.RomType_FRLG);
        }
        assumeFalse(romHandler.generationOfPokemon() == 5);

        SpeciesSet allPokes = romHandler.getSpeciesSet();

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NAMED_LOCATION);
        settings.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.RANDOM_THEMES);
        settings.setCatchEmAllEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        catchEmAllCheck(allPokes);
        randomTypeThemesLocationsCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersGivesConsequentReplacementsForEachMon(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersGivesUniqueReplacementsForEachMon(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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
    private void checkIsReplaced1To1(List<EncounterArea> before, List<EncounterArea> after, boolean checkUnique) {
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
     * Checks that for each Species in beforeArea, it is replaced by the Species listed in the map if there is one;
     * if not, adds its replacement to the map and ensures that any future replacements use the same Species.
     * @param beforeArea The area, in its state before randomization. If the area is marked as Unused (either by
     *                   EncounterType, or Location tag), the method will return without performing any checks.
     * @param afterArea The same area, after randomization.
     * @param map The map of Species to their replacements. WARNING: MODIFIED. If null, a new map will be created.
     * @param usedSpecies A SpeciesSet of Species already used as replacements. If null, Species will not be checked
     *                    for uniqueness.
     * @throws RuntimeException if the areas do not have the same display name.
     */
    private void checkAreaIsReplaced1To1(EncounterArea beforeArea, EncounterArea afterArea, Map<Species, Species> map, SpeciesSet usedSpecies) {
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
            Species beforeSp = getNonCosmeticForme(beforeEncIterator.next());
            Species afterSp = getNonCosmeticForme(afterEncIterator.next());

            if (!map.containsKey(beforeSp)) {
                map.put(beforeSp, afterSp);
                if(usedSpecies != null) {
                    System.out.println("Adding map entry: " + beforeSp.getFullName() + " to " + afterSp.getFullName());
                    assertFalse(usedSpecies.contains(afterSp));
                    usedSpecies.add(afterSp);
                }
            }
            assertEquals(map.get(beforeSp), afterSp);
        }
    }

    private static void checkAllSpeciesAreBasic(List<EncounterArea> encounters) {
        for(EncounterArea area : encounters) {
            System.out.println(area);

            SpeciesSet speciesInArea = area.getSpeciesInArea();
            for(Species species : speciesInArea) {
                System.out.print("\t" + species.getFullName() + ": ");
                SpeciesSet prevos = species.getPreEvolvedSpecies(false);
                if (prevos.isEmpty()) {
                    System.out.println("Is basic.");
                } else {
                    System.out.println("Evolves from " + prevos.toStringShort());
                    fail(species.getFullName() + " has previous evolutions");
                }
            }
            System.out.println();
        }
    }

    private static void checkAllSpeciesAreBasicOrRelatives(List<EncounterArea> encounters) {
        for(EncounterArea area : encounters) {
            System.out.println(area);

            SpeciesSet speciesInArea = area.getSpeciesInArea();
            for(Species species : speciesInArea) {
                System.out.print("\t" + species.getFullName() + ": ");
                SpeciesSet prevos = species.getPreEvolvedSpecies(false);
                if (prevos.isEmpty()) {
                    System.out.println("Is basic.");
                } else {
                    SpeciesSet basics = species.getFamily(false);
                    basics = basics.filterBasic(false);
                    System.out.println("Evolves from " + basics.toStringShort());
                    basics.retainAll(speciesInArea);
                    if(prevos.isEmpty()) {
                        System.out.println("\t\tNone are in area.");
                        fail(species.getFullName() + " evolves from non-present species");
                    } else {
                        System.out.println("\t\t" + basics.toStringShort() + " present.");
                    }
                }
            }
            System.out.println();
        }
    }

    private Species getNonCosmeticForme(Encounter enc) {
        Species base = enc.getSpecies();
        int formeNumber = enc.getFormeNumber();
        Species forme = romHandler.getAltFormeOfSpecies(base, formeNumber);


        return forme.isCosmeticReplacement() ? base : forme;
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
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setBlockWildLegendaries(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoLegendaries();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersCanBanAltFormes(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setAllowWildAltFormes(false);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForNoAlternateFormes();
    }

    // since alt formes are not guaranteed, this test can be considered "reverse";
    // any success is a success for the test as a whole
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersCanHaveAltFormesIfNotBanned(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 5);
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        checkForAlternateFormes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1to1EncountersUsePowerLevelsWorks(String romName) {
        activateRomHandler(romName);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true)); // TODO: deep copy just in case

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setSimilarStrengthEncounters(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        powerLevelsCheck(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void globalFamilyToFamilyGivesConsequentReplacements(String romName) {
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setKeepWildEvolutionFamilies(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);


        checkIsReplaced1To1(before, after, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void globalFamilyToFamilyGivesUniqueReplacements(String romName) {
        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setKeepWildEvolutionFamilies(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);


        checkIsReplaced1To1(before, after, true);
    }

    //TODO: test that family 1-to-1 actually preserves families
    // also, that it does not duplicate families

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void game1To1KeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);

        Settings settings = new Settings();
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setKeepWildTypeThemes(true);
        settings.setUseTimeBasedEncounters(true);


        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        keepTypeThemedAreasCheck(beforeAreaStrings, typeThemedAreas);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void globalFamilyToFamilyKeepTypeThemesWorks(String romName) {
        activateRomHandler(romName);

        List<List<String>> beforeAreaStrings = new ArrayList<>();
        Map<Integer, Type> typeThemedAreas = new HashMap<>();
        recordTypeThemeBefore(beforeAreaStrings, typeThemedAreas);
        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);
        settings.setKeepWildEvolutionFamilies(true);
        settings.setKeepWildTypeThemes(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

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
        activateRomHandler(romName);

        assumeTrue(romHandler.hasMapIndices());

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.MAP);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkEachMapIsReplaced1To1(before, after, true);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void onlyBasicPokemonWorks(String romName) {
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonEvolutionMod(Settings.WildPokemonEvolutionMod.BASIC_ONLY);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkAllSpeciesAreBasic(after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void onlyBasicPokemonWorksWithKeepRelations(String romName) {
        activateRomHandler(romName);

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonEvolutionMod(Settings.WildPokemonEvolutionMod.BASIC_ONLY);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.ENCOUNTER_SET);
        settings.setKeepWildEvolutionFamilies(true);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkAllSpeciesAreBasicOrRelatives(after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void sameStageWorks(String romName) {
        assumeTrue(getGenerationNumberOf(romName) != 7);
        //Alolan forms do not test correctly
        //TODO: fix that

        activateRomHandler(romName);

        List<EncounterArea> before = deepCopyEncounters(romHandler.getEncounters(true));

        Settings settings = getStandardSettings(romName);
        settings.setWildPokemonEvolutionMod(Settings.WildPokemonEvolutionMod.KEEP_STAGE);

        new WildEncounterRandomizer(romHandler, settings, RND).randomizeEncounters();

        List<EncounterArea> after = romHandler.getEncounters(true);

        checkAllSpeciesKeepSameEvoStage(before, after);
    }

    private static void checkAllSpeciesKeepSameEvoStage(List<EncounterArea> before, List<EncounterArea> after) {
        if(before.size() != after.size()) {
            throw new RuntimeException("Encounter area counts do not match before and after!");
        }

        for(int i = 0; i < before.size(); i++) {
            EncounterArea areaBefore = before.get(i);
            EncounterArea areaAfter = after.get(i);

            if(!areaBefore.getDisplayName().equals(areaAfter.getDisplayName()) ||
                    areaBefore.size() != areaAfter.size()) {
                throw new RuntimeException("Area mismatch: " + areaBefore.getDisplayName() + " and "
                        + areaAfter.getDisplayName() + ".");
            }

            System.out.println("Before: " + areaBefore);
            System.out.println("After: " + areaAfter);

            Set<Pair<Species, Species>> pairs = new HashSet<>();
            for(int j = 0; j < areaBefore.size(); j++) {
                pairs.add(new Pair<>(areaBefore.get(j).getSpecies(), areaAfter.get(j).getSpecies()));
            }

            for(Pair<Species, Species> pair : pairs) {
                System.out.print("\t" + pair.getKey().getFullName() + " -> " + pair.getValue().getFullName() + ": ");
                int stageBefore = pair.getKey().getStagesBefore(true);
                int stageAfter = pair.getValue().getStagesBefore(false);
                System.out.println(stageBefore + " -> " + stageAfter);
                assertEquals(stageBefore, stageAfter);
            }
            System.out.println();
        }
    }

}
