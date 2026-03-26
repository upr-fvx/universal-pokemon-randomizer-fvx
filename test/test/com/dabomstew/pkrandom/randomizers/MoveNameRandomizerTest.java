package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.MoveNameRandomizer;
import com.dabomstew.pkromio.constants.MoveIDs;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveCategory;
import com.dabomstew.pkromio.gamedata.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MoveNameRandomizerTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeMoveNamesDoesNotCrash(String romName) {
        activateRomHandler(romName);
        new MoveNameRandomizer(romHandler, new Settings(), RND).randomizeMoveNames();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizedMoveNamesAreNotBlank(String romName) {
        activateRomHandler(romName);
        new MoveNameRandomizer(romHandler, new Settings(), RND).randomizeMoveNames();

        for (Move mv : romHandler.getMoves()) {
            if (mv != null) {
                assertFalse(mv.name.isBlank(),
                        "Move " + mv.number + " has a blank name after randomization");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizedMoveNamesRespectMaxLength(String romName) {
        activateRomHandler(romName);
        int maxLen = romHandler.getMaxMoveNameLength();

        new MoveNameRandomizer(romHandler, new Settings(), RND).randomizeMoveNames();

        for (Move mv : romHandler.getMoves()) {
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                assertTrue(mv.name.length() <= maxLen,
                        "Move '" + mv.name + "' (id=" + mv.number + ") exceeds max name length of " + maxLen);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizedMoveNamesAreUnique(String romName) {
        activateRomHandler(romName);
        new MoveNameRandomizer(romHandler, new Settings(), RND).randomizeMoveNames();

        Set<String> seen = new HashSet<>();
        for (Move mv : romHandler.getMoves()) {
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                String normalized = mv.name.toLowerCase();
                assertFalse(seen.contains(normalized),
                        "Duplicate move name found: '" + mv.name + "' (id=" + mv.number + ")");
                seen.add(normalized);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void struggleIsNotRenamed(String romName) {
        activateRomHandler(romName);
        List<Move> moves = romHandler.getMoves();

        // Find Struggle's original name before randomization
        String originalStruggleName = null;
        for (Move mv : moves) {
            if (mv != null && mv.internalId == MoveIDs.struggle) {
                originalStruggleName = mv.name;
                break;
            }
        }
        assertNotNull(originalStruggleName, "Struggle not found in move list");

        new MoveNameRandomizer(romHandler, new Settings(), RND).randomizeMoveNames();

        // Verify Struggle kept its original name
        for (Move mv : moves) {
            if (mv != null && mv.internalId == MoveIDs.struggle) {
                assertEquals(originalStruggleName, mv.name,
                        "Struggle should not be renamed");
                break;
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeMoveNamesSetsChangesMade(String romName) {
        activateRomHandler(romName);
        MoveNameRandomizer randomizer = new MoveNameRandomizer(romHandler, new Settings(), RND);
        randomizer.randomizeMoveNames();

        assertTrue(randomizer.isChangesMade(), "changesMade should be true after randomization");
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void sameSeedProducesSameMoveNames(String romName) {
        activateRomHandler(romName);

        long seed = 12345L;

        // First run
        new MoveNameRandomizer(romHandler, new Settings(), new Random(seed)).randomizeMoveNames();
        List<String> firstRunNames = new ArrayList<>();
        for (Move mv : romHandler.getMoves()) {
            firstRunNames.add(mv != null ? mv.name : null);
        }

        // Reset and do second run with same seed
        romHandler.reset();
        romHandler.prepare();
        new MoveNameRandomizer(romHandler, new Settings(), new Random(seed)).randomizeMoveNames();
        List<String> secondRunNames = new ArrayList<>();
        for (Move mv : romHandler.getMoves()) {
            secondRunNames.add(mv != null ? mv.name : null);
        }

        assertEquals(firstRunNames, secondRunNames,
                "Same seed should produce same move names");
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizedMoveNamesActuallyChange(String romName) {
        activateRomHandler(romName);

        // Record original names
        List<String> originalNames = new ArrayList<>();
        for (Move mv : romHandler.getMoves()) {
            originalNames.add(mv != null ? mv.name : null);
        }

        new MoveNameRandomizer(romHandler, new Settings(), RND).randomizeMoveNames();

        // At least some names should have changed (all non-null, non-Struggle moves)
        int changedCount = 0;
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            Move mv = moves.get(i);
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                if (!mv.name.equals(originalNames.get(i))) {
                    changedCount++;
                }
            }
        }

        assertTrue(changedCount > 0,
                "At least some move names should have changed after randomization");
    }
}