package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Encounter;
import com.uprfvx.romio.gamedata.EncounterArea;
import com.uprfvx.romio.gamedata.EncounterType;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for Wild decision/mutation slices:
 * FVX-WILD-007 Set Minimum Catch Rate, FVX-WILD-010 Catch Em All Mode,
 * and FVX-WILD-012 Balance Low Level Encounters + Level Modifier.
 */
public class WildCatchLevelDecisionTest {

    @Test
    public void minimumCatchRateRaisesLowRatesAndPreservesHigherRates() {
        Species lowNormal = species(1, "LowNormal", 45);
        Species highNormal = species(2, "HighNormal", 200);
        Species lowLegendary = species(150, "LowLegendary", 3);
        WildTestRomHandler romHandler = WildTestRomHandler.create(List.of(lowNormal, highNormal, lowLegendary));
        Settings settings = new Settings();
        settings.setMinimumCatchRateLevel(2);

        new WildEncounterRandomizer(romHandler.proxy, settings, new Random(1)).changeCatchRates();

        assertEquals(128, lowNormal.getCatchRate());
        assertEquals(200, highNormal.getCatchRate());
        assertEquals(64, lowLegendary.getCatchRate());
        assertFalse(romHandler.guaranteedCatchEnabled);
    }

    @Test
    public void catchEmAllUsesRemainingSpeciesAcrossSyntheticEncounters() {
        List<Species> species = speciesRange(1, 6);
        EncounterArea area = area("Synthetic Route", encounter(species.get(0), 3), encounter(species.get(0), 4),
                encounter(species.get(0), 5), encounter(species.get(0), 6), encounter(species.get(0), 7),
                encounter(species.get(0), 8));
        WildTestRomHandler romHandler = WildTestRomHandler.create(species, List.of(area));
        Settings settings = new Settings();
        settings.setRandomizeWildPokemon(true);
        settings.setCatchEmAllEncounters(true);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.NONE);

        WildEncounterRandomizer randomizer = new WildEncounterRandomizer(romHandler.proxy, settings, new Random(2));
        randomizer.randomizeEncounters();

        assertTrue(randomizer.isChangesMade());
        assertEquals(1, romHandler.setEncountersCalls);
        Set<Species> encountered = area.stream().map(Encounter::getSpecies).collect(Collectors.toSet());
        assertEquals(new HashSet<>(species), encountered);
    }

    @Test
    public void wildLevelModifierAdjustsSyntheticEncounterLevelsWithoutRandomizingSpecies() {
        Species rattata = species(19, "Rattata", 255);
        Species pidgey = species(16, "Pidgey", 255);
        Encounter first = encounter(rattata, 4, 8);
        Encounter second = encounter(pidgey, 50, 100);
        EncounterArea area = area("Synthetic Grass", first, second);
        WildTestRomHandler romHandler = WildTestRomHandler.create(List.of(rattata, pidgey), List.of(area));
        Settings settings = new Settings();
        settings.setWildLevelsModified(true);
        settings.setWildLevelModifier(50);

        new WildEncounterRandomizer(romHandler.proxy, settings, new Random(3)).randomizeEncounters();

        assertEquals(6, first.getLevel());
        assertEquals(12, first.getMaxLevel());
        assertEquals(75, second.getLevel());
        assertEquals(100, second.getMaxLevel());
        assertEquals(rattata, first.getSpecies());
        assertEquals(pidgey, second.getSpecies());
        assertEquals(1, romHandler.setEncountersCalls);
    }

    @Test
    public void balanceLowLevelEncountersCapsSimilarStrengthByEncounterLevel() {
        Species current = species(10, "HighBstCurrent", 120);
        Species lowLevelFit = species(11, "LowLevelFit", 120);
        setBst(current, 600);
        setBst(lowLevelFit, 300);
        Encounter encounter = encounter(current, 5);
        EncounterArea area = area("Synthetic Low Level Grass", encounter);
        WildTestRomHandler romHandler = WildTestRomHandler.create(List.of(current, lowLevelFit), List.of(area));
        Settings settings = new Settings();
        settings.setRandomizeWildPokemon(true);
        settings.setSimilarStrengthEncounters(true);
        settings.setBalanceShakingGrass(true);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);

        new WildEncounterRandomizer(romHandler.proxy, settings, new Random(4)).randomizeEncounters();

        assertEquals(lowLevelFit, encounter.getSpecies());
        assertEquals(1, romHandler.setEncountersCalls);
    }

    @Test
    public void randomizeEncountersKeepsSyntheticSlotsAndUsesAllowedHighNumberedSpecies() {
        Species highOne = species(1025, "HighOne", 120);
        Species highTwo = species(1100, "HighTwo", 120);
        Species highThree = species(1200, "HighThree", 120);
        Set<Species> allowed = Set.of(highOne, highTwo, highThree);
        EncounterArea grass = area("Synthetic High Grass", encounter(highOne, 5, 7), encounter(highTwo, 8));
        grass.setRate(25);
        EncounterArea surf = area("Synthetic High Surf", encounter(highTwo, 20), encounter(highThree, 25, 30));
        surf.setRate(15);
        WildTestRomHandler romHandler = WildTestRomHandler.create(List.of(highOne, highTwo, highThree),
                List.of(grass, surf));
        List<SlotShape> before = slotShapes(List.of(grass, surf));
        Settings settings = new Settings();
        settings.setRandomizeWildPokemon(true);
        settings.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME);

        new WildEncounterRandomizer(romHandler.proxy, settings, new Random(5)).randomizeEncounters();

        assertEquals(1, romHandler.setEncountersCalls);
        assertEquals(before, slotShapes(List.of(grass, surf)));
        for (EncounterArea area : List.of(grass, surf)) {
            assertFalse(area.isEmpty());
            for (Encounter encounter : area) {
                assertTrue(allowed.contains(encounter.getSpecies()));
                assertTrue(encounter.getSpecies().getNumber() > 1000);
            }
        }
    }

    private static List<Species> speciesRange(int firstNumber, int count) {
        List<Species> species = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            species.add(species(firstNumber + i, "Species" + (firstNumber + i), 120));
        }
        return species;
    }

    private static Species species(int number, String name, int catchRate) {
        Species species = new Species(number);
        species.setName(name);
        species.setCatchRate(catchRate);
        species.setPrimaryType(Type.NORMAL);
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

    private static Encounter encounter(Species species, int level) {
        return encounter(species, level, 0);
    }

    private static Encounter encounter(Species species, int level, int maxLevel) {
        Encounter encounter = new Encounter();
        encounter.setSpecies(species);
        encounter.setLevel(level);
        encounter.setMaxLevel(maxLevel);
        return encounter;
    }

    private static EncounterArea area(String name, Encounter... encounters) {
        EncounterArea area = new EncounterArea(List.of(encounters));
        area.setIdentifiers(name, 1, EncounterType.WALKING, name);
        return area;
    }

    private static List<SlotShape> slotShapes(List<EncounterArea> areas) {
        List<SlotShape> shapes = new ArrayList<>();
        for (EncounterArea area : areas) {
            for (Encounter encounter : area) {
                shapes.add(new SlotShape(area.getDisplayName(), area.getMapIndex(), area.getEncounterType(),
                        area.getLocationTag(), area.getRate(), encounter.getLevel(), encounter.getMaxLevel()));
            }
        }
        return shapes;
    }

    private record SlotShape(String areaName, int mapIndex, EncounterType encounterType, String locationTag,
                             int encounterRate, int level, int maxLevel) {
    }

    private static SpeciesSet speciesSet(List<Species> species) {
        SpeciesSet speciesSet = new SpeciesSet();
        speciesSet.addAll(species);
        return speciesSet;
    }

    private static class WildTestRomHandler implements InvocationHandler {
        private final SpeciesSet speciesSet;
        private final List<Species> species;
        private final List<EncounterArea> encounters;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private int setEncountersCalls;
        private boolean guaranteedCatchEnabled;

        private WildTestRomHandler(List<Species> species, List<EncounterArea> encounters) {
            this.species = species;
            this.speciesSet = speciesSet(species);
            this.encounters = encounters;
        }

        private static WildTestRomHandler create(List<Species> species) {
            return create(species, Collections.emptyList());
        }

        private static WildTestRomHandler create(List<Species> species, List<EncounterArea> encounters) {
            WildTestRomHandler handler = new WildTestRomHandler(species, encounters);
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
                case "getAltFormes", "getIrregularFormes", "getBannedForWildEncounters" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "getTypeTable" -> new TypeTable(List.of(Type.NORMAL));
                case "getEncounters" -> encounters;
                case "setEncounters" -> {
                    setEncountersCalls++;
                    yield null;
                }
                case "enableGuaranteedPokemonCatching" -> {
                    guaranteedCatchEnabled = true;
                    yield null;
                }
                case "isORAS", "hasWildAltFormes" -> false;
                case "toString" -> "WildTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
