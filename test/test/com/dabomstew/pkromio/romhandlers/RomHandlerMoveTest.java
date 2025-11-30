package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.TMTutorMoveRandomizer;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveLearnt;
import com.dabomstew.pkromio.romhandlers.AbstractGBRomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerMoveTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void movesAreNotEmpty(String romName) {
        loadROM(romName);
        System.out.println(romHandler.getMoves());
        assertFalse(romHandler.getMoves().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void firstMoveIsNull(String romName) {
        loadROM(romName);
        assertNull(romHandler.getMoves().get(0));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allMovesHaveNonBlankNames(String romName) {
        loadROM(romName);
        System.out.println(romHandler.getMoves());
        for (Move m : romHandler.getMoves()) {
            if (m != null) {
                assertFalse(m.name.replaceAll("\\s", "").isEmpty());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void movesLearntDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        Map<Integer, List<MoveLearnt>> movesLearnt = romHandler.getMovesLearnt();
        Map<Integer, List<MoveLearnt>> before = new HashMap<>(movesLearnt);
        romHandler.setMovesLearnt(movesLearnt);
        assertEquals(before, romHandler.getMovesLearnt());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void movesLearntDoNotChangeWithLoadAndSave(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        Map<Integer, List<MoveLearnt>> movesLearnt = romHandler.getMovesLearnt();
        Map<Integer, List<MoveLearnt>> before = new HashMap<>(movesLearnt);
        romHandler.setMovesLearnt(movesLearnt);
        gbRomHandler.savePokemonStats();
        gbRomHandler.loadPokemonStats();
        assertEquals(before, romHandler.getMovesLearnt());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void eggMovesDoNotChangeWithGetAndSet(String romName) {
        assumeTrue(getGenerationNumberOf(romName) != 1);
        loadROM(romName);
        Map<Integer, List<Integer>> eggMoves = romHandler.getEggMoves();
        Map<Integer, List<Integer>> before = new HashMap<>(eggMoves);
        romHandler.setEggMoves(eggMoves);
        assertEquals(before, romHandler.getEggMoves());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void tmMovesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Integer> tmMoves = romHandler.getTMMoves();
        List<Integer> before = new ArrayList<>(tmMoves);
        romHandler.setTMMoves(tmMoves);
        assertEquals(before, romHandler.getTMMoves());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void moveTutorMovesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasMoveTutors());
        List<Integer> moveTutorMoves = romHandler.getMoveTutorMoves();
        List<Integer> before = new ArrayList<>(moveTutorMoves);
        romHandler.setMoveTutorMoves(moveTutorMoves);
        assertEquals(before, romHandler.getMoveTutorMoves());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void moveTutorsCanBeRandomizedAndGetAndSet(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasMoveTutors());
        new TMTutorMoveRandomizer(romHandler, new Settings(), RND).randomizeMoveTutorMoves();
        List<Integer> moveTutorMoves = romHandler.getMoveTutorMoves();
        List<Integer> before = new ArrayList<>(moveTutorMoves);
        romHandler.setMoveTutorMoves(moveTutorMoves);
        assertEquals(before, romHandler.getMoveTutorMoves());
    }

}
