package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.constants.SpeciesIDs;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class StarterRandomizerTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void completelyRandomChangesStarterLineup(String romName) {
        activateRomHandler(romName);

        List<Species> before = new ArrayList<>(romHandler.getStarters());

        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        System.out.println("Before: " + before);
        System.out.println("After: " + romHandler.getStarters());
        assertNotEquals(before, romHandler.getStarters());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void completelyRandomDoesNotAlwaysGiveBasics(String romName) {
        activateRomHandler(romName);

        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        StarterRandomizer sr = new StarterRandomizer(romHandler, s, RND);

        boolean allBasic = true;
        for (int i = 0; i < 100; i++) {
            sr.randomizeStarters();
            System.out.println("Set #" + (i + 1) + ": " 
                    + romHandler.getStarters().stream().map(Species::getName).toList());
            if (romHandler.getStarters().stream().anyMatch(sp -> !sp.getEvolutionsTo().isEmpty())) {
                allBasic = false;
                break;
            }
        }

        assertFalse(allBasic);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomWithTwoEvosWorks(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersHaveTwoEvos();
        checkStartersAreBasic();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomBasicWorks(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_BASIC);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreBasic();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fwgTriangleWorksWithCompletelyRandom(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreFireWaterAndGrass(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fwgTriangleWorksWithRandomWithTwoEvos(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreFireWaterAndGrass(getGenerationNumberOf(romName));
        checkStartersHaveTwoEvos();
        checkStartersAreBasic();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fwgTriangleWorksWithRandomBasic(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_BASIC);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreFireWaterAndGrass(getGenerationNumberOf(romName));
        checkStartersAreBasic();
    }

    private void checkStartersAreFireWaterAndGrass(int generation) {
        List<Species> starters = romHandler.getStarters();
        Species fireStarter;
        Species waterStarter;
        Species grassStarter;
        if(generation <= 2) {
            //early games start with Fire
            fireStarter = starters.get(0);
            waterStarter = starters.get(1);
            grassStarter = starters.get(2);
        } else {
            //later games start with Grass
            grassStarter = starters.get(0);
            fireStarter = starters.get(1);
            waterStarter = starters.get(2);
        }
        System.out.println("Fire Starter: " + fireStarter);
        assertTrue(fireStarter.getPrimaryType(false) == Type.FIRE || fireStarter.getSecondaryType(false) == Type.FIRE);
        System.out.println("Water Starter: " + waterStarter);
        assertTrue(waterStarter.getPrimaryType(false) == Type.WATER || waterStarter.getSecondaryType(false) == Type.WATER);
        System.out.println("Grass Starter: " + grassStarter);
        assertTrue(grassStarter.getPrimaryType(false) == Type.GRASS || grassStarter.getSecondaryType(false) == Type.GRASS);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleChecksReturnTrueForVanilla(String romName) {
        activateRomHandler(romName);
        assumeFalse(romHandler.isYellow());
        checkStartersAreTypeTriangle();
        checkStartersAreFireWaterAndGrass(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleWorksWithCompletelyRandom(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.TRIANGLE);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreTypeTriangle();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleWorksWithRandomWithTwoEvos(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.TRIANGLE);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreTypeTriangle();
        checkStartersHaveTwoEvos();
        checkStartersAreBasic();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleWorksWithRandomBasic(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_BASIC);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.TRIANGLE);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreTypeTriangle();
        checkStartersAreBasic();
    }

    private void checkStartersAreTypeTriangle() {
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters + "\n");

        typeTriangleCheckInner(starters);
    }

    private void typeTriangleCheckInner(List<Species> starters) {
        // Checks only for type triangles going in the same direction as the vanilla starter's triangle
        // (or technically, how the vanilla starters are read by the randomizer),
        // since triangles in the other direction might mess up rival effectiveness
        assertTrue(isSuperEffectiveAgainst(starters.get(1), starters.get(0)));
        assertTrue(isSuperEffectiveAgainst(starters.get(2), starters.get(1)));
        assertTrue(isSuperEffectiveAgainst(starters.get(0), starters.get(2)));

        if(starters.size() > 3) {
            //recurse to check the later triangles too
            List<Species> nextSet = starters.subList(3, starters.size());
            typeTriangleCheckInner(nextSet);
        }
    }

    private boolean isSuperEffectiveAgainst(Species attacker, Species defender) {
        // just checks whether any of the attacker's types is super effective against any of the defender's,
        // nothing more sophisticated
        System.out.println("Is " + attacker.getName() + " super-effective against " + defender.getName() + " ?");
        boolean isSuperEffective =
                isSuperEffectiveAgainst(attacker.getPrimaryType(false), defender.getPrimaryType(false)) ||
                isSuperEffectiveAgainst(attacker.getPrimaryType(false), defender.getSecondaryType(false)) ||
                isSuperEffectiveAgainst(attacker.getSecondaryType(false), defender.getPrimaryType(false)) ||
                isSuperEffectiveAgainst(attacker.getSecondaryType(false), defender.getSecondaryType(false));
        System.out.println(isSuperEffective + "\n");
        return isSuperEffective;
    }

    private boolean isSuperEffectiveAgainst(Type attacker, Type defender) {
        if (attacker == null || defender == null) {
            return false;
        }
        System.out.println("Is " + attacker + " super-effective against " + defender + " ?");
        System.out.println(romHandler.getTypeTable().superEffectiveWhenAttacking(attacker));
        boolean isSuperEffective = romHandler.getTypeTable().superEffectiveWhenAttacking(attacker).contains(defender);
        System.out.println(isSuperEffective);
        return isSuperEffective;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueTypesWorksWithCompletelyRandom(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.UNIQUE);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersInSameTriosAreAllDifferentTypes();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueTypesWorksWithRandomWithTwoEvos(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.UNIQUE);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersInSameTriosAreAllDifferentTypes();
        checkStartersHaveTwoEvos();
        checkStartersAreBasic();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueTypesWorksWithRandomBasic(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_BASIC);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.UNIQUE);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersInSameTriosAreAllDifferentTypes();
        checkStartersAreBasic();
    }

    private void checkStartersInSameTriosAreAllDifferentTypes() {
        List<Species> starters = romHandler.getStarters();
        int startOfTrio = 0;
        for (int i = 0; i < starters.size(); i++) {
            Species starter = starters.get(i);
            System.out.println(starter.getFullName() + ": " + starter.getPrimaryType(false) +
                    (!starter.hasSecondaryType(false) ? "" : "/" + starter.getSecondaryType(false)));

            for (int j = startOfTrio; j < i; j++) {
                Species compare = starters.get(j);
                assertFalse(sharesTypes(starter, compare),
                        starter.getFullName() + " and " + compare.getFullName() + " share a type!");
            }

            if(i == startOfTrio + 2) {
                startOfTrio = i + 1;
                System.out.println("Trio does not share types.");
                System.out.println();
            }
        }
    }

    private boolean sharesTypes(Species a, Species b) {
        if (a.getPrimaryType(false) == b.getPrimaryType(false)) {
            return true;
        }
        if (b.getSecondaryType(false) != null) {
            if (a.getPrimaryType(false) == b.getSecondaryType(false)) {
                return true;
            }
        }
        if (a.getSecondaryType(false) != null) {
            return a.getSecondaryType(false) == b.getPrimaryType(false) || a.getSecondaryType(false) == b.getSecondaryType(false);
        }
        return false;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void singleTypeWorksWithCompletelyRandom(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.SINGLE_TYPE);

        runStarterSingleTypeOnEveryTypeWithCheck(s);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void singleTypeWorksWithRandomWithTwoEvosForGrassType(String romName) {
        //Can't do every type since Fighting only has two three-stage lines - even in the games we don't support
        //However, Grass type works since Gen 1
        //So it makes a reasonable proxy to check this is working
        activateRomHandler(romName);
        assumeFalse(romHandler.isORAS());
        //Because ORAS demands 12 starters, there aren't enough 3-stage Grass types to go around
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.SINGLE_TYPE);
        s.set(Settings.Name.STARTERS_SINGLE_TYPE_SELECTION, Type.GRASS.ordinal());

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreAllOfType(Type.GRASS);
        checkStartersHaveTwoEvos();
        checkStartersAreBasic();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void singleTypeWorksWithRandomBasicForEveryType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Gen 1 & 2 have too few basic Dragon/Ghost types
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM_BASIC);
        s.set(Settings.Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.SINGLE_TYPE);

        runStarterSingleTypeOnEveryTypeWithCheck(s);
        checkStartersAreBasic();
    }

    private void runStarterSingleTypeOnEveryTypeWithCheck(SettingsManager s) {
        for (int i = 0; i < Type.values().length; i++) {
            Type t = Type.values()[i];
            if (romHandler.getTypeService().typeInGame(t)) {
                s.set(Settings.Name.STARTERS_SINGLE_TYPE_SELECTION, i);
                System.out.println(t);
                new StarterRandomizer(romHandler, s, RND).randomizeStarters();

                checkStartersAreAllOfType(t);
            }
        }
    }

    private void checkStartersAreAllOfType(Type type) {
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters.stream().map(pk -> pk.getName() +
                " " + pk.getPrimaryType(false) +
                (pk.getSecondaryType(false) == null ? ""
                        : " / " + pk.getSecondaryType(false))).collect(Collectors.toList()));

        for (Species starter : starters) {
            assertTrue(starter.getPrimaryType(false) == type
                    || starter.getSecondaryType(false) == type);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noDualTypesWorks(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_NO_DUAL_TYPES, true);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreEachSingleType();
    }

    private void checkStartersAreEachSingleType() {
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters);
        for(Species starter : starters) {
            assertNull(starter.getSecondaryType(false));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noDualTypesWorksWithRandomTypes(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();
        s.set(Settings.Name.RANDOMIZE_SPECIES_TYPES, Settings.SpeciesTypesMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_NO_DUAL_TYPES, true);

        new SpeciesTypeRandomizer(romHandler, s, RND).randomizeSpeciesTypes();
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        checkStartersAreEachSingleType();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void starterBSTLimitsWorks(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();

        final int MINIMUM_BST = 245;
        final int MAXIMUM_BST = 255;
        //arbitrary narrow range that has at least 3 Pokemon in all gens
        //(and 12 in gen 6)

        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);
        s.set(Settings.Name.STARTERS_BST_MINIMUM, MINIMUM_BST);
        s.set(Settings.Name.STARTERS_BST_MAXIMUM, MAXIMUM_BST);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        startersInBSTRangeCheck(MINIMUM_BST, MAXIMUM_BST);
    }

    private void startersInBSTRangeCheck(int minimumBST, int maximumBST) {
        for(Species starter : romHandler.getStarters()) {
            System.out.println(starter);
            assertTrue(starter.getBST(false) >= minimumBST && starter.getBST(false) <= maximumBST);
        }
    }

    private void checkStartersHaveTwoEvos() {


        for(Species starter : romHandler.getStarters()) {
            boolean hasTwoEvos = false;

            System.out.println(starter.getFullName());
            for(Species firstEvo : starter.getEvolvedSpecies(false)) {
                System.out.println("\t" + firstEvo.getFullName());
                for(Species secondEvo : firstEvo.getEvolvedSpecies(false)) {
                    System.out.println("\t\t" + secondEvo.getFullName());
                    hasTwoEvos = true;
                }
            }

            assertTrue(hasTwoEvos);
        }
    }

    private void checkStartersAreBasic() {
        for(Species starter : romHandler.getStarters()) {
            System.out.println(starter.getFullName());
            assertTrue(starter.getPreEvolvedSpecies(false).isEmpty());
        }
    }


    @ParameterizedTest
    @MethodSource("getRomNames")
    public void customStartersCanBeSet(String romName) {
        activateRomHandler(romName);
        SettingsManager s = createSettingsManager();

        List<Integer> customStarters = List.of(SpeciesIDs.venusaur, SpeciesIDs.charizard, SpeciesIDs.blastoise);
        s.set(Settings.Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        s.set(Settings.Name.STARTER_CUSTOM_1, customStarters.get(0));
        s.set(Settings.Name.STARTER_CUSTOM_2, customStarters.get(1));
        if (s.isEnabled(Settings.Name.STARTER_CUSTOM_3)) {
            s.set(Settings.Name.STARTER_CUSTOM_3, customStarters.get(2));
        }

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        List<Species> starters = romHandler.getStarters();
        List<Species> allPokes = romHandler.getSpecies();

        System.out.print("Expected: ");
        System.out.println(customStarters.stream().map(allPokes::get).map(Species::getNumberAndFullName).toList());
        System.out.print("Actual:   ");
        System.out.println(starters.stream().map(Species::getNumberAndFullName).toList());

        for (int i = 0; i < 3; i++) {
            assertEquals(starters.get(i), allPokes.get(customStarters.get(i)));
        }
    }
}
