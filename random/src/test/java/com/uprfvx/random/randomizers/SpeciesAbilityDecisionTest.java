package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.AbilityIDs;
import com.uprfvx.romio.gamedata.MegaEvolution;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.gamedata.TypeTable;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ROM-free coverage for a first Items/Moves/Abilities slice.
 * This does not prove ROM-facing ability writer/reload behavior.
 */
public class SpeciesAbilityDecisionTest {

    @Test
    public void randomizeAbilitiesKeepsSyntheticSpeciesInAllowedAbilityPool() {
        Species lowSpecies = species(25, "LowSpecies");
        Species highSpecies = species(1025, "HighSpecies");
        AbilityTestRomHandler romHandler = AbilityTestRomHandler.create(List.of(lowSpecies, highSpecies), 1005);
        Settings settings = new Settings();
        settings.setAllowWonderGuard(false);
        settings.setEnsureTwoAbilities(true);

        SpeciesAbilityRandomizer randomizer = new SpeciesAbilityRandomizer(romHandler.proxy, settings,
                new FixedIntRandom(
                        2, 999, // ability 3 is useless/banned, then ability 1000 is accepted
                        24, 1000, // Wonder Guard is banned, then ability 1001 is accepted
                        1001, 1002,
                        1003, 1004));
        randomizer.randomizeAbilities();

        assertTrue(randomizer.isChangesMade());
        assertSpeciesAbilitiesWithinPool(lowSpecies, 1005);
        assertSpeciesAbilitiesWithinPool(highSpecies, 1005);
        assertFalse(hasAbility(lowSpecies, 3));
        assertFalse(hasAbility(highSpecies, 3));
        assertFalse(hasAbility(lowSpecies, AbilityIDs.wonderGuard));
        assertFalse(hasAbility(highSpecies, AbilityIDs.wonderGuard));
        assertTrue(hasHighAbility(lowSpecies) || hasHighAbility(highSpecies));
        assertEquals(1025, highSpecies.getNumber());
    }

    private static void assertSpeciesAbilitiesWithinPool(Species species, int highestAbility) {
        assertAbilityWithinPool(species.getAbility1(), highestAbility);
        assertAbilityWithinPool(species.getAbility2(), highestAbility);
        assertNotEquals(species.getAbility1(), species.getAbility2());
    }

    private static void assertAbilityWithinPool(int ability, int highestAbility) {
        assertTrue(ability >= 1 && ability <= highestAbility, "Ability outside allowed pool: " + ability);
    }

    private static boolean hasAbility(Species species, int ability) {
        return species.getAbility1() == ability || species.getAbility2() == ability || species.getAbility3() == ability;
    }

    private static boolean hasHighAbility(Species species) {
        return species.getAbility1() > 1000 || species.getAbility2() > 1000 || species.getAbility3() > 1000;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        species.setPrimaryType(Type.NORMAL);
        species.setHp(50);
        species.setAttack(50);
        species.setDefense(50);
        species.setSpatk(50);
        species.setSpdef(50);
        species.setSpeed(50);
        species.setAbility1(1);
        species.setAbility2(2);
        return species;
    }

    private static SpeciesSet speciesSet(List<Species> species) {
        SpeciesSet speciesSet = new SpeciesSet();
        speciesSet.addAll(species);
        return speciesSet;
    }

    private static class FixedIntRandom extends java.util.Random {
        private final Queue<Integer> ints;

        private FixedIntRandom(int... ints) {
            this.ints = new ArrayDeque<>();
            for (int value : ints) {
                this.ints.add(value);
            }
        }

        @Override
        public int nextInt(int bound) {
            int value = ints.remove();
            if (value < 0 || value >= bound) {
                throw new IllegalArgumentException("Fixed value " + value + " outside bound " + bound);
            }
            return value;
        }
    }

    private static class AbilityTestRomHandler implements InvocationHandler {
        private final SpeciesSet speciesSet;
        private final int highestAbility;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;

        private AbilityTestRomHandler(List<Species> species, int highestAbility) {
            this.speciesSet = speciesSet(species);
            this.highestAbility = highestAbility;
        }

        private static AbilityTestRomHandler create(List<Species> species, int highestAbility) {
            AbilityTestRomHandler handler = new AbilityTestRomHandler(species, highestAbility);
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
                case "abilitiesPerSpecies" -> 2;
                case "highestAbilityIndex" -> highestAbility;
                case "getUselessAbilities" -> new java.util.ArrayList<>(List.of(3));
                case "getSpeciesSetInclFormes", "getSpeciesSet" -> speciesSet;
                case "getAltFormes" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "getTypeTable" -> new TypeTable(List.of(Type.NORMAL));
                case "generationOfPokemon" -> 9;
                case "getAbilityVariations" -> Collections.<Integer, List<Integer>>emptyMap();
                case "toString" -> "AbilityTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
