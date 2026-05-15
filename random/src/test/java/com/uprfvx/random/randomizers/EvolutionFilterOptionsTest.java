package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for Evolution filter slices:
 * FVX-TRAIT-017 Random Every Level, FVX-TRAIT-020 Limit to Three Stages,
 * FVX-TRAIT-021 No Convergence, FVX-TRAIT-022 Force Change, and FVX-TRAIT-023 Force Growth.
 */
public class EvolutionFilterOptionsTest {

    @Test
    public void randomEveryLevelCreatesLevelOneEvolutionsForEverySpecies() {
        List<Species> species = speciesRange(1, 8);
        EvolutionTestRomHandler romHandler = EvolutionTestRomHandler.create(species);
        Settings settings = new Settings();
        settings.setEvolutionsMod(Settings.EvolutionsMod.RANDOM_EVERY_LEVEL);

        EvolutionRandomizer randomizer = new EvolutionRandomizer(romHandler.proxy, settings, new Random(1));
        randomizer.randomizeEvolutions();

        for (Species source : species) {
            assertEquals(1, source.getEvolutionsFrom().size(), source.getName());
            Evolution evolution = source.getEvolutionsFrom().get(0);
            assertEquals(EvolutionType.LEVEL, evolution.getType());
            assertEquals(1, evolution.getExtraInfo());
            assertFalse(source.equals(evolution.getTo()));
        }
        assertTrue(randomizer.isChangesMade());
    }

    @Test
    public void limitToThreeStagesPreventsLongerSyntheticChains() {
        List<Species> species = speciesRange(1, 10);
        addEvolution(species.get(0), species.get(1));
        addEvolution(species.get(1), species.get(2));
        addEvolution(species.get(2), species.get(3));
        addEvolution(species.get(4), species.get(5));
        addEvolution(species.get(6), species.get(7));
        EvolutionTestRomHandler romHandler = EvolutionTestRomHandler.create(species);
        Settings settings = randomEvolutionSettings();
        settings.setEvosMaxThreeStages(true);

        new EvolutionRandomizer(romHandler.proxy, settings, new Random(2)).randomizeEvolutions();

        for (Species source : species) {
            assertTrue(maxStagesAfter(source, new HashSet<>()) <= 3, source.getName());
        }
    }

    @Test
    public void noConvergencePreventsMultipleSourcesFromSharingOneTarget() {
        List<Species> species = speciesRange(1, 8);
        addEvolution(species.get(0), species.get(4));
        addEvolution(species.get(1), species.get(5));
        addEvolution(species.get(2), species.get(6));
        addEvolution(species.get(3), species.get(7));
        EvolutionTestRomHandler romHandler = EvolutionTestRomHandler.create(species);
        Settings settings = randomEvolutionSettings();
        settings.setEvosNoConvergence(true);

        new EvolutionRandomizer(romHandler.proxy, settings, new Random(3)).randomizeEvolutions();

        for (Species target : species) {
            assertTrue(target.getEvolutionsTo().size() <= 1, target.getName());
        }
    }

    @Test
    public void forceChangeAvoidsOriginalEvolutionTargets() {
        List<Species> species = speciesRange(1, 9);
        addEvolution(species.get(0), species.get(3));
        addEvolution(species.get(1), species.get(4));
        addEvolution(species.get(2), species.get(5));
        Map<Species, Set<Species>> originalTargets = originalTargets(species);
        EvolutionTestRomHandler romHandler = EvolutionTestRomHandler.create(species);
        Settings settings = randomEvolutionSettings();
        settings.setEvosForceChange(true);

        new EvolutionRandomizer(romHandler.proxy, settings, new Random(4)).randomizeEvolutions();

        for (Species source : species) {
            for (Evolution evolution : source.getEvolutionsFrom()) {
                assertFalse(originalTargets.get(source).contains(evolution.getTo()), source.getName());
            }
        }
    }

    @Test
    public void forceGrowthOnlyChoosesHigherBstTargets() {
        List<Species> species = speciesRange(1, 10);
        setBst(species.get(0), 120);
        setBst(species.get(1), 140);
        setBst(species.get(2), 160);
        setBst(species.get(3), 260);
        setBst(species.get(4), 280);
        setBst(species.get(5), 300);
        setBst(species.get(6), 320);
        setBst(species.get(7), 340);
        setBst(species.get(8), 360);
        setBst(species.get(9), 380);
        addEvolution(species.get(0), species.get(3));
        addEvolution(species.get(1), species.get(4));
        addEvolution(species.get(2), species.get(5));
        EvolutionTestRomHandler romHandler = EvolutionTestRomHandler.create(species);
        Settings settings = randomEvolutionSettings();
        settings.setEvosForceGrowth(true);

        new EvolutionRandomizer(romHandler.proxy, settings, new Random(5)).randomizeEvolutions();

        for (Species source : species) {
            for (Evolution evolution : source.getEvolutionsFrom()) {
                assertTrue(evolution.getTo().getBSTForPowerLevels() > source.getBSTForPowerLevels(),
                        source.getName() + " -> " + evolution.getTo().getName());
            }
        }
    }

    private static Settings randomEvolutionSettings() {
        Settings settings = new Settings();
        settings.setEvolutionsMod(Settings.EvolutionsMod.RANDOM);
        return settings;
    }

    private static List<Species> speciesRange(int firstNumber, int count) {
        List<Species> species = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Species sp = new Species(firstNumber + i);
            sp.setName("Species" + (firstNumber + i));
            sp.setGrowthCurve(ExpCurve.MEDIUM_FAST);
            setBst(sp, 200 + i * 20);
            species.add(sp);
        }
        return species;
    }

    private static void setBst(Species species, int bst) {
        int stat = bst / 6;
        species.setHp(stat);
        species.setAttack(stat);
        species.setDefense(stat);
        species.setSpatk(stat);
        species.setSpdef(stat);
        species.setSpeed(bst - stat * 5);
    }

    private static void addEvolution(Species from, Species to) {
        Evolution evolution = new Evolution(from, to, EvolutionType.LEVEL, 16);
        from.getEvolutionsFrom().add(evolution);
        to.getEvolutionsTo().add(evolution);
    }

    private static Map<Species, Set<Species>> originalTargets(List<Species> species) {
        Map<Species, Set<Species>> targets = new HashMap<>();
        for (Species source : species) {
            targets.put(source, source.getEvolutionsFrom().stream().map(Evolution::getTo).collect(Collectors.toSet()));
        }
        return targets;
    }

    private static int maxStagesAfter(Species source, Set<Species> visited) {
        if (!visited.add(source)) {
            return 1;
        }
        int max = 1;
        for (Evolution evolution : source.getEvolutionsFrom()) {
            max = Math.max(max, 1 + maxStagesAfter(evolution.getTo(), visited));
        }
        visited.remove(source);
        return max;
    }

    private static SpeciesSet speciesSet(List<Species> species) {
        SpeciesSet speciesSet = new SpeciesSet();
        speciesSet.addAll(species);
        return speciesSet;
    }

    private static class EvolutionTestRomHandler implements InvocationHandler {
        private final SpeciesSet speciesSet;
        private final List<Species> species;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;

        private EvolutionTestRomHandler(List<Species> species) {
            this.species = species;
            this.speciesSet = speciesSet(species);
        }

        private static EvolutionTestRomHandler create(List<Species> species) {
            EvolutionTestRomHandler handler = new EvolutionTestRomHandler(species);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] {RomHandler.class}, handler);
            handler.restrictedSpeciesService = new RestrictedSpeciesService(handler.proxy);
            handler.typeService = new TypeService(handler.proxy);
            handler.restrictedSpeciesService.setRestrictions(null);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService" -> restrictedSpeciesService;
                case "getTypeService" -> typeService;
                case "getSpeciesSetInclFormes", "getSpeciesSet" -> speciesSet;
                case "getSpeciesInclFormes", "getSpecies" -> species;
                case "getAltFormes", "getIrregularFormes" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.emptyList();
                case "getAllowedItems" -> Collections.emptySet();
                case "altFormesCanHaveDifferentEvolutions" -> false;
                case "toString" -> "EvolutionTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
