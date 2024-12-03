package test.romhandlers;

import com.dabomstew.pkrandom.gamedata.Evolution;
import com.dabomstew.pkrandom.gamedata.EvolutionType;
import com.dabomstew.pkrandom.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            for (Evolution evo : pk.getEvolutionsFrom()) {
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
