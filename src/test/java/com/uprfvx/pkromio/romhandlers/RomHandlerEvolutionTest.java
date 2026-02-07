package com.uprfvx.pkromio.romhandlers;

import com.uprfvx.pkromio.constants.SpeciesIDs;
import com.uprfvx.pkromio.gamedata.Evolution;
import com.uprfvx.pkromio.gamedata.EvolutionType;
import com.uprfvx.pkromio.gamedata.Item;
import com.uprfvx.pkromio.gamedata.Species;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerEvolutionTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void speciesHaveEvolutions(String romName) {
        loadROM(romName);
        boolean hasEvolutions = false;
        for (Species pk : romHandler.getSpeciesSet()) {
            if (!pk.getEvolutionsFrom().isEmpty()) {
                hasEvolutions = true;
                break;
            }
        }
        assertTrue(hasEvolutions);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noEvolutionUsesEvoTypeNone(String romName) {
        loadROM(romName);
        for (Species pk : romHandler.getSpeciesSet()) {
            System.out.println(pk.getFullName());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println(evo);
                assertNotEquals(EvolutionType.NONE, evo.getType());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noSpeciesHasDuplicateEvolutions(String romName) {
        // The games actually allow this internally,
        // e.g. Feebas evolves into Milotic using both beauty and prism scale+trade.
        // For now the Randomizer doesn't play well with that though,
        // so we expect the RomHandlers to remove duplicate Evolutions.
        loadROM(romName);

        Set<Species> withDuplicateEvos = new HashSet<>();
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            Set<Species> evolved = new HashSet<>();
            System.out.println(pk.getEvolutionsFrom());
            for (Evolution evo : pk.getEvolutionsFrom()) {
                // LEVEL_FEMALE_ESPURR is an exception since it implies a forme difference
                if (evolved.contains(evo.getTo()) && evo.getType() != EvolutionType.LEVEL_FEMALE_ESPURR) {
                    withDuplicateEvos.add(pk);
                }
                evolved.add(evo.getTo());
            }
        }

        System.out.println("------");
        withDuplicateEvos.forEach(pk -> System.out.println(pk.getEvolutionsFrom()));
        assertTrue(withDuplicateEvos.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void evolutionsDoNotChangeWithSaveAndLoad(String romName) {
        loadROM(romName);

        Map<Species, List<Evolution>> evosToBefore = new HashMap<>();
        Map<Species, List<Evolution>> evosFromBefore = new HashMap<>();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            evosToBefore.put(pk, pk.getEvolutionsTo().stream().map(Evolution::new).collect(Collectors.toList()));
            evosFromBefore.put(pk, pk.getEvolutionsFrom().stream().map(Evolution::new).collect(Collectors.toList()));
        }

        romHandler.saveSpeciesStats();
        romHandler.loadSpeciesStats();
        if (romHandler instanceof AbstractGBRomHandler) {
            // TODO: not pleasant that GB romhandler load evolutions separately from stats,
            //  when they don't even save them separately.
            ((AbstractGBRomHandler) romHandler).loadEvolutions();
        }

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            List<Evolution> toBefore = evosToBefore.get(pk);
            List<Evolution> fromBefore = evosFromBefore.get(pk);
            List<Evolution> toAfter = pk.getEvolutionsTo();
            List<Evolution> fromAfter = pk.getEvolutionsFrom();

            System.out.println(pk.getFullName());
            System.out.println("Evos To");
            System.out.println("Before: " + toBefore);
            System.out.println("After: " + toAfter);
            assertEquals(toBefore, toAfter);
            System.out.println("Evos From");
            System.out.println("Before: " + fromBefore);
            System.out.println("After: " + fromAfter);
            assertEquals(fromBefore, fromAfter);
            System.out.println();
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void condenseEvolutionLevelsDoesNothingIfHighestEvoLvlIsUsed(String romName) {
        loadROM(romName);

        Map<Species, List<Evolution>> evosToBefore = new HashMap<>();
        Map<Species, List<Evolution>> evosFromBefore = new HashMap<>();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            evosToBefore.put(pk, pk.getEvolutionsTo().stream().map(Evolution::new).collect(Collectors.toList()));
            evosFromBefore.put(pk, pk.getEvolutionsFrom().stream().map(Evolution::new).collect(Collectors.toList()));
        }

        romHandler.condenseLevelEvolutions(romHandler.getHighestEvoLvl());

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            List<Evolution> toBefore = evosToBefore.get(pk);
            List<Evolution> fromBefore = evosFromBefore.get(pk);
            List<Evolution> toAfter = pk.getEvolutionsTo();
            List<Evolution> fromAfter = pk.getEvolutionsFrom();

            System.out.println(pk.getFullName());
            System.out.println("Evos To");
            System.out.println("Before: " + toBefore);
            System.out.println("After: " + toAfter);
            assertEquals(toBefore, toAfter);
            System.out.println("Evos From");
            System.out.println("Before: " + fromBefore);
            System.out.println("After: " + fromAfter);
            assertEquals(fromBefore, fromAfter);
            System.out.println();
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void condenseEvolutionLevelsWorks(String romName) {
        loadROM(romName);

        int maxLvl = 4;
        romHandler.condenseLevelEvolutions(maxLvl);

        // Expected: All final evolution levels should be 4 and all intermediate levels should be 0.75*4 = 3
        int expectedIntermediateLvl = (int) Math.ceil(0.75 * maxLvl);
        for (Species pk : romHandler.getSpeciesSet()) {
            if (pk != null) {
                for (Evolution checkEvo : pk.getEvolutionsFrom()) {
                    if (checkEvo.getType().usesLevelThreshold()) {
                        System.out.println(checkEvo);
                        if (!checkEvo.getTo().getEvolutionsFrom().isEmpty()) { // Evo is intermediate
                            assertEquals(expectedIntermediateLvl, checkEvo.getExtraInfo());
                            assertEquals(expectedIntermediateLvl, checkEvo.getEstimatedEvoLvl());
                        } else {
                            assertEquals(maxLvl, checkEvo.getExtraInfo());
                            assertEquals(maxLvl, checkEvo.getEstimatedEvoLvl());
                        }
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allSpeciesCanBeGivenExactlyOneEvolutionAndSaved(String romName) {
        // for testing whether "Random Every Level" evolutions would work
        loadROM(romName);
        assumeTrue(romHandler.canGiveEverySpeciesOneEvolutionEach());
        Species universalTo = romHandler.getSpecies().get(SpeciesIDs.krabby); // lol
        universalTo.getEvolutionsTo().clear();
        for (Species pk : romHandler.getSpecies()) {
            if (pk == null || pk == universalTo) {
                continue;
            }
            pk.getEvolutionsTo().clear();
            pk.getEvolutionsFrom().clear();
            // evolution type and extra should not matter
            Evolution evo = new Evolution(pk, universalTo, EvolutionType.LEVEL, 1);
            pk.getEvolutionsFrom().add(evo);
            universalTo.getEvolutionsTo().add(evo);
        }
        romHandler.saveSpeciesStats();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void evolutionsHaveSensibleItems(String romName) {
        loadROM(romName);

        List<Item> allItems = romHandler.getItems();
        Set<Item> evolutionItems = romHandler.getEvolutionItems();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                // In Gens 2+3, TRADE_ITEM items are not counted as evo items,
                // as the player is not expected to trade.
                // The player is not expected to trade in other games either,
                // but as Gen 4 introduces ITEM, TRADE_ITEM items
                // become relevant evo items within that context.
                if (evo.getType().usesItem() &&
                        !(romHandler.generationOfPokemon() < 4 && evo.getType() == EvolutionType.TRADE_ITEM)) {
                    System.out.println(evo);
                    Item evoItem = allItems.get(evo.getExtraInfo());
                    System.out.println(evoItem.getName());
                    assertTrue(evolutionItems.contains(evoItem));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void removeTimeEvosGivesSensibleEvoItems(String romName) {
        loadROM(romName);

        romHandler.removeTimeBasedEvolutions();

        List<Item> allItems = romHandler.getItems();
        Set<Item> evolutionItems = romHandler.getEvolutionItems();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                // In Gens 2+3, TRADE_ITEM items are not counted as evo items,
                // as the player is not expected to trade.
                // The player is not expected to trade in other games either,
                // but as Gen 4 introduces ITEM, TRADE_ITEM items
                // become relevant evo items within that context.
                if (evo.getType().usesItem() &&
                        !(romHandler.generationOfPokemon() < 4 && evo.getType() == EvolutionType.TRADE_ITEM)) {
                    System.out.println(evo);
                    Item evoItem = allItems.get(evo.getExtraInfo());
                    System.out.println(evoItem.getName());
                    assertTrue(evolutionItems.contains(evoItem));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void levelThresholdEvosHaveEqualExtraInfoAndEstimatedEvoLvl(String romName) {
        loadROM(romName);
        test_allEvolutionThatUseLevelThresholdHaveSameExtraInfoAndEstimatedEvoLvl();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void levelThresholdEvosHaveEqualExtraInfoAndEstimatedEvoLvlAfterEvolutionImprovements(String romName) {
        loadROM(romName);

        romHandler.removeImpossibleEvolutions(true, true);
        romHandler.condenseLevelEvolutions(40);
        romHandler.makeEvolutionsEasier(true, true);
        romHandler.removeTimeBasedEvolutions();

        test_allEvolutionThatUseLevelThresholdHaveSameExtraInfoAndEstimatedEvoLvl();
    }

    private void test_allEvolutionThatUseLevelThresholdHaveSameExtraInfoAndEstimatedEvoLvl() {
        for (Species pk : romHandler.getSpeciesSet()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                // Each level-up evolution should have the same evolution level as the estimated level
                if (evo.getType().usesLevelThreshold()) {
                    System.out.println(evo);
                    assertEquals(evo.getExtraInfo(), evo.getEstimatedEvoLvl());
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noEstimatedEvoLevelIsZero(String romName) {
        loadROM(romName);

        // We always check alt forms, because even if they shouldn't have different
        // evos from their base forms before Gen 7, in practice they do, or at least
        // have the data structures for it.
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                System.out.println(evo);
                assertNotEquals(0, evo.getEstimatedEvoLvl());
            }
        }
    }

    /**
     * Not really a test (except testing that every evolution got an estimated level) but will produce markdown output
     * copyable to the docs.
     * <br>
     * Since running this may open Gen 6+ ROMs (if you have any), which are very slow to handle,
     * it is disabled by default.
     */
    @Disabled
    @ParameterizedTest
    @MethodSource("getAllRomNames")
    public void printAllEstimatedLevelsPerGen(String romName) {
        try {
            loadROM(romName);

            String currentGen = "";
            switch (romHandler.getROMName()) {
                case "Pokemon Yellow (U)":
                    currentGen = "Generation 1 (Red/Blue/Yellow)";
                    break;
                case "Pokemon Crystal (U)":
                    currentGen = "Generation 2 (Gold/Silver/Crystal)";
                    break;
                case "Pokemon Emerald (U)":
                    currentGen = "Generation 3 (Ruby/Sapphire/Emerald/FireRed/LeafGreen)";
                    break;
                case "Pokemon Platinum (U)":
                    currentGen = "Generation 4 (Diamond/Pearl/Platinum/HeartGold/SoulSilver)";
                    break;
                case "Pokemon Black 2 (U)":
                    currentGen = "Generation 5 (Black/White/Black2/White2)";
                    break;
                case "Pokemon Omega Ruby":
                    currentGen = "Generation 6 (X/Y/Omega Ruby/Alpha Sapphire)";
                    break;
                case "Pokemon Ultra Sun":
                    currentGen = "Generation 7 (Sun/Moon/Ultra Sun/Ultra Moon)";
                    break;
            }
            if (!currentGen.isEmpty()) { // Only print once per generation
                System.out.println();
                System.out.println("### " + currentGen);
                System.out.println();
                System.out.println("| Pokemon | Evolution | Estimated Level |");
                System.out.println("|-----------|-----------|-----------------------|");
                for (Species pk : romHandler.getSpeciesSet()) {
                    for (Evolution evo : pk.getEvolutionsTo()) {
                        // Each level-up evolution should have the same evolution level as the estimated level
                        System.out.println("| " + evo.getFrom().getName() + " | " + pk.getName() + " | " +
                                evo.getEstimatedEvoLvl() + " | ");
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing if ROM not there
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void printAllEvoTypesByUsage(String romName) {
        // not really a test since it makes no assertions, but still useful when debugging
        loadROM(romName);

        Map<EvolutionType, List<Evolution>> allEvos = new EnumMap<>(EvolutionType.class);
        for (EvolutionType et : EvolutionType.values()) {
            allEvos.put(et, new ArrayList<>());
        }

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                allEvos.get(evo.getType()).add(evo);
            }
        }

        for (Map.Entry<EvolutionType, List<Evolution>> entry : allEvos.entrySet()) {
            System.out.println(entry.getValue().size() + "\t" + entry.getKey());
            for (Evolution evo : entry.getValue()) {
                System.out.println("\t" + evo);
            }
        }
    }
}
