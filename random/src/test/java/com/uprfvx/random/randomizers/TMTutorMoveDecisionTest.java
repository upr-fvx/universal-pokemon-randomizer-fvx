package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.MoveCategory;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ROM-free coverage for a first Moves slice.
 * This does not prove ROM-facing TM/Tutor writer/reload behavior.
 */
public class TMTutorMoveDecisionTest {

    @Test
    public void randomizeTMMovesKeepsSelectionInAllowedPoolAndPreservesFieldSlots() {
        List<Integer> originalTms = List.of(10, 11, 12, 13);
        MoveTestRomHandler romHandler = MoveTestRomHandler.create(
                originalTms,
                moves(
                        10,
                        20,
                        21,
                        22,
                        23,
                        1001,
                        1002,
                        1003));
        Settings settings = new Settings();
        settings.setKeepFieldMoveTMs(true);
        settings.setBlockBrokenTMMoves(true);

        TMTutorMoveRandomizer randomizer = new TMTutorMoveRandomizer(romHandler.proxy, settings, new Random(1));
        randomizer.randomizeTMMoves();

        assertTrue(randomizer.isTMChangesMade());
        assertEquals(1, romHandler.setTMMovesCalls);
        assertEquals(originalTms.size(), romHandler.writtenTms.size());
        assertEquals(10, romHandler.writtenTms.get(0));

        Set<Integer> allowedHighMoves = Set.of(1001, 1002, 1003);
        Set<Integer> excludedMoves = Set.of(10, 20, 21, 22, 23);
        for (int i = 1; i < romHandler.writtenTms.size(); i++) {
            int selectedMove = romHandler.writtenTms.get(i);
            assertTrue(allowedHighMoves.contains(selectedMove), "Unexpected TM move " + selectedMove);
            assertFalse(excludedMoves.contains(selectedMove), "Excluded move was selected: " + selectedMove);
        }
        assertEquals(allowedHighMoves, Set.copyOf(romHandler.writtenTms.subList(1, romHandler.writtenTms.size())));
    }

    private static List<Move> moves(int... moveNumbers) {
        List<Move> moves = new ArrayList<>();
        moves.add(null);
        for (int moveNumber : moveNumbers) {
            moves.add(move(moveNumber));
        }
        return moves;
    }

    private static Move move(int moveNumber) {
        Move move = new Move();
        move.number = moveNumber;
        move.internalId = moveNumber;
        move.name = "Move " + moveNumber;
        move.power = 80;
        move.pp = 15;
        move.hitratio = 100;
        move.type = Type.NORMAL;
        move.category = MoveCategory.PHYSICAL;
        return move;
    }

    private static class MoveTestRomHandler implements InvocationHandler {
        private final List<Integer> originalTms;
        private final List<Move> moves;
        private RomHandler proxy;
        private List<Integer> writtenTms;
        private int setTMMovesCalls;

        private MoveTestRomHandler(List<Integer> originalTms, List<Move> moves) {
            this.originalTms = originalTms;
            this.moves = moves;
        }

        private static MoveTestRomHandler create(List<Integer> originalTms, List<Move> moves) {
            MoveTestRomHandler handler = new MoveTestRomHandler(originalTms, moves);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] {RomHandler.class}, handler);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService", "getTypeService" -> null;
                case "getTMCount" -> originalTms.size();
                case "getMoves" -> moves;
                case "getTMMoves" -> new ArrayList<>(originalTms);
                case "getHMMoves" -> List.of(20);
                case "getGameBreakingMoves" -> List.of(21);
                case "getMovesBannedFromLevelup" -> List.of(22);
                case "getIllegalMoves" -> List.of(23);
                case "getFieldMoves" -> List.of(10);
                case "getPerfectAccuracy" -> 100;
                case "setTMMoves" -> {
                    writtenTms = typedMoveList(args[0]);
                    setTMMovesCalls++;
                    yield null;
                }
                case "toString" -> "MoveTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        @SuppressWarnings("unchecked")
        private static List<Integer> typedMoveList(Object moves) {
            return (List<Integer>) moves;
        }
    }
}
