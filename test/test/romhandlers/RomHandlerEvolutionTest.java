package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.SpeciesIDs;
import com.dabomstew.pkrandom.gamedata.Evolution;
import com.dabomstew.pkrandom.gamedata.EvolutionType;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.randomizers.EvolutionRandomizer;
import com.dabomstew.pkrandom.romhandlers.AbstractRomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                Species baseForme = evo.getTo().isBaseForme() ? evo.getTo() : evo.getTo().getBaseForme();
                // LEVEL_FEMALE_ESPURR is an exception since it implies a forme difference
                if (evolved.contains(baseForme) && evo.getType() != EvolutionType.LEVEL_FEMALE_ESPURR) {
                    withDuplicateEvos.add(pk);
                }
                evolved.add(baseForme);
            }
        }

        System.out.println("------");
        withDuplicateEvos.forEach(pk -> System.out.println(pk.getEvolutionsFrom()));
        assertTrue(withDuplicateEvos.isEmpty());
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
        ((AbstractRomHandler) romHandler).savePokemonStats();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void removeTimeEvosGivesSensibleEvoItems(String romName) {
        loadROM(romName);

        romHandler.removeTimeBasedEvolutions();

        String[] itemNames = romHandler.getItemNames();
        List<Integer> evolutionItems = romHandler.getEvolutionItems();
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                // In Gens 2+3, TRADE_ITEM items are not counted as evo items,
                // as the player is not expected to trade.
                // The player is not expected to trade in other games either,
                // but as Gen 4 introduces LEVEL_ITEM, TRADE_ITEM items
                // become relevant evo items within that context.
                if (evo.getType().usesItem() &&
                        !(romHandler.generationOfPokemon() < 4 && evo.getType() == EvolutionType.TRADE_ITEM)) {
                    System.out.println(evo);
                    System.out.println(itemNames[evo.getExtraInfo()]);
                    assertTrue(evolutionItems.contains(evo.getExtraInfo()));
                }
            }
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
