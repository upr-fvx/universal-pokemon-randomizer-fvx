package test.romhandlers;

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.randomizers.StarterRandomizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

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
        SettingsManager s = new SettingsManager();
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
    public void customStartersCanBeSet(String romName) {
        loadROM(romName);
        SettingsManager s = new SettingsManager();
        s.setStartersMod(false, true, false);
        int customCount = romHandler.starterCount();
        int[] custom = new int[customCount];
        for (int i = 0; i < custom.length; i++) {
            custom[i] = i + 1;
        }
        s.setCustomStarters(custom);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();

        List<Species> starters = romHandler.getStarters();
        List<Species> allPokes = romHandler.getSpecies();

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
}
