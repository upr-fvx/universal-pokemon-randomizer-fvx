package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.StarterRandomizer;
import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void starterHeldItemsDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Item> before = new ArrayList<>(romHandler.getStarterHeldItems());
        System.out.println(before);
        romHandler.setStarterHeldItems(before);
        assertEquals(before, romHandler.getStarterHeldItems());
    }
}
