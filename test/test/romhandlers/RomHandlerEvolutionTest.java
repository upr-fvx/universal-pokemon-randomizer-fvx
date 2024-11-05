package test.romhandlers;

import com.dabomstew.pkrandom.gamedata.Evolution;
import com.dabomstew.pkrandom.gamedata.EvolutionType;
import com.dabomstew.pkrandom.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerEvolutionTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void pokemonHaveEvolutions(String romName) {
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
    public void printAllEvoTypesByUsage(String romName) {
        // not really a test since it makes no assertions, but still useful when debugging
        loadROM(romName);

        Map<EvolutionType, List<Evolution>> allEvos = new EnumMap<>(EvolutionType.class);
        for (EvolutionType et : EvolutionType.values()) {
            allEvos.put(et, new ArrayList<>());
        }

        for (Species pk : romHandler.getSpeciesSet()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                allEvos.get(evo.getType()).add(evo);
            }
        }

        Comparator<Map.Entry<EvolutionType, List<Evolution>>> comparator =
                Comparator.comparingInt(entry -> entry.getValue().size());
        comparator = comparator.reversed();
        List<Map.Entry<EvolutionType, List<Evolution>>> sorted =
                allEvos.entrySet().stream()
                        .filter(entry -> entry.getValue().size() != 0)
                        .sorted(comparator)
                        .collect(Collectors.toList());
        for (Map.Entry<EvolutionType, List<Evolution>> entry : sorted) {
            System.out.println(entry.getValue().size() + "\t" + entry.getKey());
            for (Evolution evo : entry.getValue()) {
                System.out.println("\t" + evo);
            }
        }
    }
}
