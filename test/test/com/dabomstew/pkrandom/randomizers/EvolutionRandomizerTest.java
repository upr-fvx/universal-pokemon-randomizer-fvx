package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.EvolutionRandomizer;
import com.dabomstew.pkrandom.randomizers.SpeciesBaseStatRandomizer;
import com.dabomstew.pkromio.constants.SpeciesIDs;
import com.dabomstew.pkromio.gamedata.Evolution;
import com.dabomstew.pkromio.gamedata.EvolutionType;
import com.dabomstew.pkromio.gamedata.ExpCurve;
import com.dabomstew.pkromio.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class EvolutionRandomizerTest extends RandomizerTest {

    private static final double MAX_AVERAGE_POWER_LEVEL_DIFF = 0.065;

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomNoPokemonEvolvesIntoItself(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println("\t" + evo.getTo().getName());
                assertNotEquals(pk, evo.getTo());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEvosShareEXPCurveWithPrevo(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName() + " " + pk.getGrowthCurve());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println("\t" + evo.getTo().getName() + " " + evo.getTo().getGrowthCurve());
                assertEquals(pk.getGrowthCurve(), evo.getTo().getGrowthCurve());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomSplitEvosDoNotChooseTheSamePokemon(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println("\t" + evo.getTo().getName());
            }
            for (int i = 0; i < pk.getEvolutionsFrom().size(); i++) {
                for (int j = i + 1; j < pk.getEvolutionsFrom().size(); j++) {
                    Species evoI = pk.getEvolutionsFrom().get(i).getTo();
                    Species evoJ = pk.getEvolutionsFrom().get(j).getTo();
                    assertNotEquals(evoI, evoJ);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomNoEvoCyclesExist(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName());
            checkForNoCycles(pk, pk, 1);
        }
    }

    private void checkForNoCycles(Species curr, Species start, int depth) {
        for (Evolution evo : curr.getEvolutionsFrom()) {
            System.out.print(new String(new char[depth]).replaceAll("\0", " "));
            System.out.println(evo.getTo().getName());
            assertNotEquals(start, evo.getTo());
            checkForNoCycles(evo.getTo(), start, depth + 1);
        }
    }


    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomSameTypingGivesEvosWithSomeSharedType(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosSameTyping(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        evosHaveSharedTypeCheck();
        System.out.println(evoGraph());
    }

    private void evosHaveSharedTypeCheck() {
        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(toStringWithTypes(pk) + " ->");
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.print("\t" + toStringWithTypes(evo.getTo()));
                if (pk.getEvolutionsFrom().size() == 1) {
                    assertTrue(evo.getTo().hasSharedType(pk));
                } else {
                    System.out.print("(split evo/no carry)");
                }
                System.out.println();
            }
        }
    }

    private String toStringWithTypes(Species pk) {
        return pk.getName() + "(" + pk.getPrimaryType(false) + (pk.getSecondaryType(false) == null ? "" : "/" + pk.getSecondaryType(false)) + ")";
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomSameTypingGivesNewEeveelutionsSharingSomeTypeWithTheOriginals(String romName) {
        activateRomHandler(romName);

        Species eeveeBefore = romHandler.getSpecies().get(SpeciesIDs.eevee);
        List<Species> beforeEvos = new ArrayList<>(eeveeBefore.getEvolutionsFrom().size());
        for (Evolution evo : eeveeBefore.getEvolutionsFrom()) {
            Species before = new Species(0);
            before.setName(evo.getTo().getName());
            before.setPrimaryType(evo.getTo().getPrimaryType(false));
            before.setSecondaryType(evo.getTo().getSecondaryType(false));
            beforeEvos.add(before);
        }

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosSameTyping(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        Species eevee = romHandler.getSpecies().get(SpeciesIDs.eevee);
        System.out.println(toStringWithTypes(eevee));
        for (int i = 0; i < eevee.getEvolutionsFrom().size(); i++) {
            Species before = beforeEvos.get(i);
            Species after = eevee.getEvolutionsFrom().get(i).getTo();
            System.out.println("before: " + toStringWithTypes(before));
            System.out.println("after: " + toStringWithTypes(after));
            assertTrue(before.hasSharedType(after));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomLimitEvosToThreeStagesWorks(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosMaxThreeStages(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        maxThreeEvoStagesCheck();
    }

    private void maxThreeEvoStagesCheck() {
        for (Species pk : romHandler.getSpeciesSet()) {
            int evostages = evoStagesAfter(pk, 1);
            System.out.println(evostages);
            assertTrue(evostages <= 3);
        }
    }

    private int evoStagesAfter(Species pk, int count) {
        System.out.print(new String(new char[count - 1]).replaceAll("\0", " "));
        System.out.println(pk.getName());
        int max = count++;
        for (Evolution evo : pk.getEvolutionsFrom()) {
            max = Math.max(max, evoStagesAfter(evo.getTo(), count));
        }
        return max;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomSameTypingANDLimitEvosToThreeStagesWorks(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosSameTyping(true);
        s.setEvosMaxThreeStages(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        evosHaveSharedTypeCheck();
        maxThreeEvoStagesCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomForceChangeWorks(String romName) {
        activateRomHandler(romName);

        Map<Species, List<Species>> allEvosBefore = new HashMap<>();
        for (Species pk : romHandler.getSpeciesSet()) {
            allEvosBefore.put(pk, pk.getEvolutionsFrom().stream().map(Evolution::getTo).collect(Collectors.toList()));
        }

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosForceChange(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            List<Species> evosBefore = allEvosBefore.get(pk);
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println(evo);
                assertFalse(evosBefore.contains(evo.getTo()));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomForceChangeWorksForCosmoem(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 7);
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosForceChange(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        Species cosmoem = romHandler.getSpecies().get(SpeciesIDs.cosmoem);
        System.out.println(cosmoem.getName());
        for (Evolution evo : cosmoem.getEvolutionsFrom()) {
            System.out.println(evo);
            assertNotEquals(SpeciesIDs.solgaleo, evo.getTo().getNumber());
            assertNotEquals(SpeciesIDs.lunala, evo.getTo().getNumber());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomSimilarStrengthWorks(String romName) {
        activateRomHandler(romName);

        Map<Species, List<Species>> allEvosBefore = new HashMap<>();
        for (Species pk : romHandler.getSpeciesSet()) {
            allEvosBefore.put(pk, pk.getEvolutionsFrom().stream().map(Evolution::getTo).collect(Collectors.toList()));
        }

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosSimilarStrength(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        similarStrengthCheck(allEvosBefore);
    }

    private void similarStrengthCheck(Map<Species, List<Species>> allEvosBefore) {
        List<Double> diffs = new ArrayList<>();
        for (Species pk : romHandler.getSpeciesSet()) {
            for (int i = 0; i < pk.getEvolutionsFrom().size(); i++) {
                Species before = allEvosBefore.get(pk).get(i);
                Species after = pk.getEvolutionsFrom().get(i).getTo();
                diffs.add(calcPowerLevelDiff(before, after));
            }
        }

        double averageDiff = diffs.stream().mapToDouble(d -> d).average().getAsDouble();
        System.out.println(diffs);
        System.out.println(averageDiff);
        assertTrue(averageDiff <= MAX_AVERAGE_POWER_LEVEL_DIFF);
    }

    private double calcPowerLevelDiff(Species a, Species b) {
        return Math.abs((double) a.getBSTForPowerLevels() /
                b.getBSTForPowerLevels() - 1);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomForceGrowthWorks(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosForceGrowth(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getFullName() + " BST=" + pk.getBSTForPowerLevels() + " ->");
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println("\t" + evo.getTo().getFullName() + " BST=" + evo.getTo().getBSTForPowerLevels());
                assertTrue(evo.getTo().getBSTForPowerLevels() > pk.getBSTForPowerLevels());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomNoConvergenceWorks(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        s.setEvosNoConvergence(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getFullName());
            System.out.println(pk.getEvolutionsTo());
            assertTrue(pk.getEvolutionsTo().size() <= 1);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomNoEvoHasLevelFemaleEspurrEvoType(String romName) {
        // Not entirely sure why this has to be the case, but older evolution randomization made sure to get rid of
        // and LEVEL_FEMALE_ESPURR, and so it's carried to newer code as well.
        // Probably there are some issues if LEVEL_FEMALE_ESPURR is used and the Pokemon it evolves to isn't Meowstic.
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, true, false);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println(evo);
                assertNotEquals(EvolutionType.LEVEL_FEMALE_ESPURR, evo.getType());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEveryLevelGivesEveryPokemonExactlyOneEvolutionAtLevelOne(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, false, true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName());
            System.out.println(pk.getEvolutionsFrom());
            assertEquals(1, pk.getEvolutionsFrom().size());
            Evolution evo = pk.getEvolutionsFrom().get(0);
            assertEquals(EvolutionType.LEVEL, evo.getType());
            assertEquals(1, evo.getExtraInfo());
        }

        System.out.println(evoGraph());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEveryLevelNoPokemonEvolvesIntoItself(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, false, true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println("\t" + evo.getTo().getName());
                assertNotEquals(pk, evo.getTo());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEveryLevelEvosShareEXPCurveWithPrevo(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(false, false, true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getName() + " " + pk.getGrowthCurve());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println("\t" + evo.getTo().getName() + " " + evo.getTo().getGrowthCurve());
                assertEquals(pk.getGrowthCurve(), evo.getTo().getGrowthCurve());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEveryLevelForceChangeWorks(String romName) {
        activateRomHandler(romName);

        Map<Species, List<Species>> allEvosBefore = new HashMap<>();
        for (Species pk : romHandler.getSpeciesSet()) {
            allEvosBefore.put(pk, pk.getEvolutionsFrom().stream().map(Evolution::getTo).collect(Collectors.toList()));
        }

        Settings s = new Settings();
        s.setEvolutionsMod(false, false, true);
        s.setEvosForceChange(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            List<Species> evosBefore = allEvosBefore.get(pk);
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println(evo);
                assertFalse(evosBefore.contains(evo.getTo()));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEveryLevelSameTypingGivesEvosWithSomeSharedType(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setExpCurveMod(false, false, true);
        s.setSelectedEXPCurve(ExpCurve.MEDIUM_FAST);
        s.setStandardizeEXPCurves(true);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).standardizeEXPCurves();
        s.setEvolutionsMod(false, false, true);
        s.setEvosSameTyping(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        evosHaveSharedTypeCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEveryLevelNoConvergenceWorks(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEvolutionsMod(Settings.EvolutionsMod.RANDOM_EVERY_LEVEL);
        s.setEvosNoConvergence(true);
        new EvolutionRandomizer(romHandler, s, RND).randomizeEvolutions();

        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getFullName());
            System.out.println(pk.getEvolutionsTo());
            assertTrue(pk.getEvolutionsTo().size() <= 1);
        }
    }

    /**
     * Returns a graph in the dot format, which can be seen using e.g.
     * <a href="https://dreampuf.github.io/GraphvizOnline">GraphvizOnline</a>
     */
    private String evoGraph() {
        StringBuilder sb = new StringBuilder("digraph G {\n");
        for (Species pk : romHandler.getSpeciesSet()) {
            sb.append(evoGraphFriendly(pk.getName()));
            sb.append(";\n");
            for (Evolution evo : pk.getEvolutionsFrom()) {
                sb.append(evoGraphFriendly(pk.getName()));
                sb.append("->");
                sb.append(evoGraphFriendly(evo.getTo().getName()));
                sb.append(";\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String evoGraphFriendly(String s) {
        return s.replace('♂', 'M')
                .replace('♀', 'F')
                .replaceAll("\\W", "");
    }

}
