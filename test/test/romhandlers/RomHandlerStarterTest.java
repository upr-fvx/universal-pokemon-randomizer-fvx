package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.game_data.Species;
import com.dabomstew.pkrandom.game_data.Type;
import com.dabomstew.pkrandom.randomizers.PokemonTypeRandomizer;
import com.dabomstew.pkrandom.randomizers.StarterRandomizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerStarterTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void startersAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getStarters().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void startersDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters);
        List<Species> before = new ArrayList<>(starters);
        romHandler.setStarters(starters);
        assertEquals(before, romHandler.getStarters());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void startersCanBeRandomizedAndGetAndSet(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, true);
        System.out.println(s.getStartersMod());
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();
        List<Species> starters = romHandler.getStarters();
        List<Species> before = new ArrayList<>(starters);
        romHandler.setStarters(starters);
        assertEquals(before, romHandler.getStarters());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void completelyRandomChangesStarterLineup(String romName) {
        loadROM(romName);

        List<Species> before = new ArrayList<>(romHandler.getStarters());

        Settings s = new Settings();
        s.setStartersMod(false, false, true);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        System.out.println("Before: " + before);
        System.out.println("After: " + romHandler.getStarters());
        assertNotEquals(before, romHandler.getStarters());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void customStartersCanBeSet(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, true, false);
        int customCount = romHandler.starterCount();
        int[] custom = new int[customCount];
        for (int i = 0; i < custom.length; i++) {
            custom[i] = i + 1;
        }
        s.setCustomStarters(custom);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        List<Species> starters = romHandler.getStarters();
        List<Species> allPokes = romHandler.getPokemon();

        StringBuilder sb = new StringBuilder("Starters");
        sb.append(" (should be ");
        for (int i = 0; i < customCount; i++) {
            sb.append(allPokes.get(custom[i]).getName());
            if (i != customCount - 1) {
                sb.append(", ");
            }
        }
        sb.append("): ");
        sb.append(starters);
        System.out.println(sb);

        for (int i = 0; i < customCount; i++) {
            assertEquals(starters.get(i), allPokes.get(custom[i]));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void completelyRandomWithNoOtherOptionsDoesntThrow(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, true);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomWithTwoEvosWithNoOtherOptionsDoesntThrow(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, false, true);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomBasicWithNoOtherOptionsDoesntThrow(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, false, false, true);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fwgTriangleWorksWithCompletelyRandom(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        Settings s = new Settings();
        s.setStartersMod(false, false, true, false, false);
        s.setStartersTypeMod(false, true, false, false, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        fwgCheck(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fwgTriangleWorksWithRandomWithTwoEvos(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        Settings s = new Settings();
        s.setStartersMod(false, false, false, true, false);
        s.setStartersTypeMod(false, true, false, false, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        fwgCheck(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fwgTriangleWorksWithRandomBasic(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        Settings s = new Settings();
        s.setStartersMod(false, false, false, false, true);
        s.setStartersTypeMod(false, true, false, false, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        fwgCheck(getGenerationNumberOf(romName));
    }

    private void fwgCheck(int generation) {
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
    public void typeTriangleCheckReturnsTrueForVanilla(String romName) {
        loadROM(romName);
        assumeFalse(romHandler.isYellow());
        typeTriangleCheck(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleWorksWithCompletelyRandom(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        Settings s = new Settings();
        s.setStartersMod(false, false, true, false, false);
        s.setStartersTypeMod(false, false, true, false, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        typeTriangleCheck(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleWorksWithRandomWithTwoEvos(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        Settings s = new Settings();
        s.setStartersMod(false, false, false, true, false);
        s.setStartersTypeMod(false, false, true, false, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        typeTriangleCheck(getGenerationNumberOf(romName));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTriangleWorksWithRandomBasic(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasStarterTypeTriangleSupport());
        Settings s = new Settings();
        s.setStartersMod(false, false, false, false, true);
        s.setStartersTypeMod(false, false, true, false, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        typeTriangleCheck(getGenerationNumberOf(romName));
    }

    private void typeTriangleCheck(int generation) {
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters + "\n");

        typeTriangleCheckInner(starters, generation);
    }

    private void typeTriangleCheckInner(List<Species> starters, int generation) {
        // Checks only for type triangles going in the same direction as the vanilla starter's triangle
        // (or technically, how the vanilla starters are read by the randomizer),
        // since triangles in the other direction might mess up rival effectiveness
        assertTrue(isSuperEffectiveAgainst(starters.get(1), starters.get(0), generation));
        assertTrue(isSuperEffectiveAgainst(starters.get(2), starters.get(1), generation));
        assertTrue(isSuperEffectiveAgainst(starters.get(0), starters.get(2), generation));

        if(starters.size() > 3) {
            //recurse to check the later triangles too
            List<Species> nextSet = starters.subList(3, starters.size());
            typeTriangleCheckInner(nextSet, generation);
        }
    }

    private boolean isSuperEffectiveAgainst(Species attacker, Species defender, int generation) {
        // just checks whether any of the attacker's types is super effective against any of the defender's,
        // nothing more sophisticated
        System.out.println("Is " + attacker.getName() + " super-effective against " + defender.getName() + " ?");
        boolean isSuperEffective =
                isSuperEffectiveAgainst(attacker.getPrimaryType(false), defender.getPrimaryType(false), generation) ||
                isSuperEffectiveAgainst(attacker.getPrimaryType(false), defender.getSecondaryType(false), generation) ||
                isSuperEffectiveAgainst(attacker.getSecondaryType(false), defender.getPrimaryType(false), generation) ||
                isSuperEffectiveAgainst(attacker.getSecondaryType(false), defender.getSecondaryType(false), generation);
        System.out.println(isSuperEffective + "\n");
        return isSuperEffective;
    }

    private boolean isSuperEffectiveAgainst(Type attacker, Type defender, int generation) {
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
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, true, false, false);
        s.setStartersTypeMod(false, false, false, true, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        uniqueTypesCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueTypesWorksWithRandomWithTwoEvos(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, false, true, false);
        s.setStartersTypeMod(false, false, false, true, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        uniqueTypesCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueTypesWorksWithRandomBasic(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, false, false, true);
        s.setStartersTypeMod(false, false, false, true, false);
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        uniqueTypesCheck();
    }

    private void uniqueTypesCheck() {
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters);
        for (int i = 0; i < starters.size(); i++) {
            for (int j = i + 1; j < starters.size(); j++) {
                assertFalse(sharesTypes(starters.get(i), starters.get(j)));
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
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, true, false, false);
        s.setStartersTypeMod(false, false, false, false, true);

        singleTypeCheck(s);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void singleTypeWorksWithRandomWithTwoEvosForEveryType(String romName) {
        // fails on all vanilla games before gen 4, as until then there are only two for Dragon.
        assumeTrue(getGenerationNumberOf(romName) >= 4);
        loadROM(romName);
        assumeFalse(romHandler.isORAS());
        Settings s = new Settings();
        s.setStartersMod(false, false, false, true, false);
        s.setStartersTypeMod(false, false, false, false, true);

        singleTypeCheck(s);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void singleTypeWorksWithRandomBasic(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2); // Gen 1 & 2 have too few basic Dragon/Ghost types
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, false, false, true);
        s.setStartersTypeMod(false, false, false, false, true);

        singleTypeCheck(s);
    }

    private void singleTypeCheck(Settings s) {
        for (int i = 0; i < Type.values().length; i++) {
            Type t = Type.values()[i];
            if (romHandler.getTypeService().typeInGame(t)) {
                s.setStartersSingleType(i + 1);
                System.out.println(t);
                new StarterRandomizer(romHandler, s, RND).randomizeStarters();

                List<Species> starters = romHandler.getStarters();
                System.out.println(starters.stream().map(pk -> pk.getName() + " " + pk.getPrimaryType(false) +
                        (pk.getSecondaryType(false) == null ? "" : " / " + pk.getSecondaryType(false))).collect(Collectors.toList()));
                for (Species starter : starters) {
                    assertTrue(starter.getPrimaryType(false) == t || starter.getSecondaryType(false) == t);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noDualTypesWorks(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setStartersMod(false, false, false, false, false);
        s.setStartersTypeMod(false, false, false, false, false);
        s.setStartersNoDualTypes(true);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        noDualTypesCheck();
    }

    private void noDualTypesCheck() {
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters);
        for(Species starter : starters) {
            assertNull(starter.getSecondaryType(false));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noDualTypesWorksWithRandomTypes(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setTypesMod(Settings.TypesMod.COMPLETELY_RANDOM);
        s.setStartersMod(false, false, false, false, false);
        s.setStartersTypeMod(false, false, false, false, false);
        s.setStartersNoDualTypes(true);

        new PokemonTypeRandomizer(romHandler, s, RND).randomizePokemonTypes();
        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        noDualTypesCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void starterBSTLimitsWorks(String romName) {
        loadROM(romName);
        Settings s = new Settings();

        final int MINIMUM_BST = 245;
        final int MAXIMUM_BST = 255;
        //arbitrary narrow range that has at least 3 Pokemon in all gens
        //(and 12 in gen 6)

        s.setStartersMod(Settings.StartersMod.COMPLETELY_RANDOM);
        s.setStartersBSTMinimum(MINIMUM_BST);
        s.setStartersBSTMaximum(MAXIMUM_BST);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        startersInBSTRangeCheck(MINIMUM_BST, MAXIMUM_BST);
    }

    private void startersInBSTRangeCheck(int minimumBST, int maximumBST) {
        for(Species starter : romHandler.getStarters()) {
            System.out.println(starter);
            assertTrue(starter.getBSTForPowerLevels() >= minimumBST &&
                    starter.getBSTForPowerLevels() <= maximumBST);
        }
    }
}
