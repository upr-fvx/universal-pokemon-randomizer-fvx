package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.MegaEvolution;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.MoveCategory;
import com.uprfvx.romio.gamedata.MoveLearnt;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for the first Learnsets slice.
 * This does not prove ROM-facing moveset writer/reload behavior.
 */
public class LearnsetDecisionTest {

    @Test
    public void randomizeMovesLearntPreservesSyntheticLearnsetStructureAndAllowedMovePool() {
        Species lowSpecies = species(25, "LowSpecies");
        Species highSpecies = species(1025, "HighSpecies");
        Map<Integer, List<MoveLearnt>> movesets = new LinkedHashMap<>();
        movesets.put(lowSpecies.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        movesets.put(highSpecies.getNumber(), movesLearnt(13, 1, 14, 20, 15, 40));
        Map<Integer, List<Integer>> levelsBefore = levelsBySpecies(movesets);
        Map<Integer, Integer> sizesBefore = sizesBySpecies(movesets);
        Set<Integer> allowedMoves = Set.of(20, 21, 22, 23, 24, 25, 26, 27);
        LearnsetTestRomHandler romHandler = LearnsetTestRomHandler.create(List.of(lowSpecies, highSpecies),
                movesets, moves(allowedMoves));
        Settings settings = new Settings();
        settings.setMovesetsMod(Settings.MovesetsMod.COMPLETELY_RANDOM);

        SpeciesMovesetRandomizer randomizer = new SpeciesMovesetRandomizer(romHandler.proxy, settings, new Random(1));
        randomizer.randomizeMovesLearnt();

        assertTrue(randomizer.isChangesMade());
        assertEquals(1, romHandler.setMovesLearntCalls);
        assertEquals(sizesBefore, sizesBySpecies(romHandler.writtenMovesets));
        assertEquals(levelsBefore, levelsBySpecies(romHandler.writtenMovesets));
        for (Map.Entry<Integer, List<MoveLearnt>> entry : romHandler.writtenMovesets.entrySet()) {
            assertFalse(entry.getValue().isEmpty(), "Moveset was emptied for species " + entry.getKey());
            for (MoveLearnt moveLearnt : entry.getValue()) {
                assertTrue(allowedMoves.contains(moveLearnt.move),
                        "Unexpected move " + moveLearnt.move + " for species " + entry.getKey());
            }
        }
        assertTrue(romHandler.writtenMovesets.containsKey(highSpecies.getNumber()));
        assertTrue(romHandler.writtenMovesets.get(highSpecies.getNumber()).stream()
                .allMatch(moveLearnt -> allowedMoves.contains(moveLearnt.move)));
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        species.setPrimaryType(Type.NORMAL);
        species.setAttack(60);
        species.setSpatk(60);
        return species;
    }

    private static List<MoveLearnt> movesLearnt(int moveOne, int levelOne, int moveTwo, int levelTwo,
                                                int moveThree, int levelThree) {
        return new ArrayList<>(List.of(new MoveLearnt(moveOne, levelOne), new MoveLearnt(moveTwo, levelTwo),
                new MoveLearnt(moveThree, levelThree)));
    }

    private static List<Move> moves(Set<Integer> moveNumbers) {
        List<Move> moves = new ArrayList<>();
        for (int moveNumber : moveNumbers) {
            Move move = new Move();
            move.number = moveNumber;
            move.name = "Move" + moveNumber;
            move.power = 80;
            move.pp = 15;
            move.hitratio = 100;
            move.type = Type.NORMAL;
            move.category = moveNumber % 2 == 0 ? MoveCategory.PHYSICAL : MoveCategory.SPECIAL;
            moves.add(move);
        }
        return moves;
    }

    private static Map<Integer, List<Integer>> levelsBySpecies(Map<Integer, List<MoveLearnt>> movesets) {
        Map<Integer, List<Integer>> levels = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<MoveLearnt>> entry : movesets.entrySet()) {
            levels.put(entry.getKey(), entry.getValue().stream()
                    .map(moveLearnt -> moveLearnt.level)
                    .collect(Collectors.toList()));
        }
        return levels;
    }

    private static Map<Integer, Integer> sizesBySpecies(Map<Integer, List<MoveLearnt>> movesets) {
        Map<Integer, Integer> sizes = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<MoveLearnt>> entry : movesets.entrySet()) {
            sizes.put(entry.getKey(), entry.getValue().size());
        }
        return sizes;
    }

    private static SpeciesSet speciesSet(List<Species> species) {
        SpeciesSet speciesSet = new SpeciesSet();
        speciesSet.addAll(species);
        return speciesSet;
    }

    private static class LearnsetTestRomHandler implements InvocationHandler {
        private final SpeciesSet speciesSet;
        private final List<Species> species;
        private final Map<Integer, List<MoveLearnt>> movesets;
        private final List<Move> moves;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private Map<Integer, List<MoveLearnt>> writtenMovesets;
        private int setMovesLearntCalls;

        private LearnsetTestRomHandler(List<Species> species, Map<Integer, List<MoveLearnt>> movesets,
                                       List<Move> moves) {
            this.species = species;
            this.speciesSet = speciesSet(species);
            this.movesets = movesets;
            this.moves = moves;
        }

        private static LearnsetTestRomHandler create(List<Species> species, Map<Integer, List<MoveLearnt>> movesets,
                                                     List<Move> moves) {
            LearnsetTestRomHandler handler = new LearnsetTestRomHandler(species, movesets, moves);
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
                case "getAltFormes" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "getTypeTable" -> new TypeTable(List.of(Type.NORMAL));
                case "getMovesLearnt" -> movesets;
                case "setMovesLearnt" -> {
                    writtenMovesets = typedMovesets(args[0]);
                    setMovesLearntCalls++;
                    yield null;
                }
                case "getMoves" -> moves;
                case "getHMMoves", "getGameBreakingMoves", "getMovesBannedFromLevelup", "getIllegalMoves" ->
                        Collections.<Integer>emptyList();
                case "getPerfectAccuracy" -> 100;
                case "supportsFourStartingMoves" -> false;
                case "toString" -> "LearnsetTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        @SuppressWarnings("unchecked")
        private static Map<Integer, List<MoveLearnt>> typedMovesets(Object movesets) {
            return (Map<Integer, List<MoveLearnt>>) movesets;
        }
    }
}
