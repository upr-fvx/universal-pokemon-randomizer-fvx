package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    public void randomizeMovesLearntSkipsEmptyOrNullMovesetsAndKeepsValidSpeciesRandomized() {
        Species emptySpecies = species(0, "Bad Egg");
        Species nullSpecies = species(1, "DummySpecies");
        Species validSpecies = species(25, "ValidSpecies");
        Map<Integer, List<MoveLearnt>> movesets = new LinkedHashMap<>();
        movesets.put(emptySpecies.getNumber(), new ArrayList<>());
        movesets.put(nullSpecies.getNumber(), null);
        movesets.put(validSpecies.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        Set<Integer> allowedMoves = Set.of(40, 41, 42, 43, 44, 45, 46, 47);
        LearnsetTestRomHandler romHandler = LearnsetTestRomHandler.create(List.of(emptySpecies, nullSpecies,
                validSpecies), movesets, moves(allowedMoves));
        romHandler.supportsFourStartingMoves = true;
        Settings settings = new Settings();
        settings.setMovesetsMod(Settings.MovesetsMod.COMPLETELY_RANDOM);
        settings.setStartWithGuaranteedMoves(true);
        settings.setGuaranteedMoveCount(4);
        settings.setEvolutionMovesForAll(true);

        SpeciesMovesetRandomizer randomizer = new SpeciesMovesetRandomizer(romHandler.proxy, settings, new Random(2));
        randomizer.randomizeMovesLearnt();

        assertTrue(randomizer.isChangesMade());
        assertEquals(1, romHandler.setMovesLearntCalls);
        assertTrue(romHandler.writtenMovesets.get(emptySpecies.getNumber()).isEmpty());
        assertNull(romHandler.writtenMovesets.get(nullSpecies.getNumber()));
        List<MoveLearnt> validMoveset = romHandler.writtenMovesets.get(validSpecies.getNumber());
        assertFalse(validMoveset.isEmpty());
        assertTrue(validMoveset.stream().allMatch(moveLearnt -> allowedMoves.contains(moveLearnt.move)));
        assertEquals(List.of(0, 1, 1, 1, 1, 7, 14), validMoveset.stream()
                .map(moveLearnt -> moveLearnt.level)
                .collect(Collectors.toList()));
    }

    @Test
    public void orderDamagingMovesByDamageOnlyReordersDamagingMovesWithinExistingSlots() {
        Species emptySpecies = species(0, "Bad Egg");
        Species highSpecies = species(1025, "HighSpecies");
        Map<Integer, List<MoveLearnt>> movesets = new LinkedHashMap<>();
        movesets.put(emptySpecies.getNumber(), new ArrayList<>());
        movesets.put(highSpecies.getNumber(), new ArrayList<>(List.of(
                new MoveLearnt(30, 0),
                new MoveLearnt(31, 1),
                new MoveLearnt(32, 8),
                new MoveLearnt(33, 20),
                new MoveLearnt(34, 50))));
        Map<Integer, List<Integer>> levelsBefore = levelsBySpecies(movesets);
        Map<Integer, Integer> sizesBefore = sizesBySpecies(movesets);
        Set<Integer> originalMovePool = movesets.get(highSpecies.getNumber()).stream()
                .map(moveLearnt -> moveLearnt.move)
                .collect(Collectors.toSet());
        LearnsetTestRomHandler romHandler = LearnsetTestRomHandler.create(List.of(highSpecies),
                movesets, indexedMoves(
                        move(30, 10),
                        move(31, 90),
                        move(32, 40),
                        move(33, 120),
                        move(34, 0)));

        SpeciesMovesetRandomizer randomizer = new SpeciesMovesetRandomizer(romHandler.proxy, new Settings(),
                new Random(2));
        randomizer.orderDamagingMovesByDamage();

        List<MoveLearnt> ordered = romHandler.writtenMovesets.get(highSpecies.getNumber());
        assertTrue(randomizer.isChangesMade());
        assertEquals(1, romHandler.setMovesLearntCalls);
        assertTrue(romHandler.writtenMovesets.get(emptySpecies.getNumber()).isEmpty());
        assertFalse(ordered.isEmpty());
        assertEquals(sizesBefore, sizesBySpecies(romHandler.writtenMovesets));
        assertEquals(levelsBefore, levelsBySpecies(romHandler.writtenMovesets));
        assertEquals(List.of(30, 32, 31, 33, 34), ordered.stream()
                .map(moveLearnt -> moveLearnt.move)
                .collect(Collectors.toList()));
        assertEquals(originalMovePool, ordered.stream()
                .map(moveLearnt -> moveLearnt.move)
                .collect(Collectors.toSet()));
        assertEquals(1025, highSpecies.getNumber());
    }

    @Test
    public void guaranteedStartingMovesAddsOnlyExpectedLevelOneSlots() {
        Species highSpecies = species(1025, "HighSpecies");
        Map<Integer, List<MoveLearnt>> movesets = new LinkedHashMap<>();
        movesets.put(highSpecies.getNumber(), new ArrayList<>(List.of(
                new MoveLearnt(10, 1),
                new MoveLearnt(11, 7))));
        Set<Integer> allowedMoves = Set.of(40, 41, 42, 43, 44, 45);
        LearnsetTestRomHandler romHandler = LearnsetTestRomHandler.create(List.of(highSpecies),
                movesets, moves(allowedMoves));
        romHandler.supportsFourStartingMoves = true;
        Settings settings = new Settings();
        settings.setMovesetsMod(Settings.MovesetsMod.COMPLETELY_RANDOM);
        settings.setStartWithGuaranteedMoves(true);
        settings.setGuaranteedMoveCount(4);

        SpeciesMovesetRandomizer randomizer = new SpeciesMovesetRandomizer(romHandler.proxy, settings, new Random(3));
        randomizer.randomizeMovesLearnt();

        List<MoveLearnt> written = romHandler.writtenMovesets.get(highSpecies.getNumber());
        assertTrue(randomizer.isChangesMade());
        assertEquals(1, romHandler.setMovesLearntCalls);
        assertFalse(written.isEmpty());
        assertEquals(5, written.size());
        assertEquals(List.of(1, 1, 1, 1, 7), written.stream()
                .map(moveLearnt -> moveLearnt.level)
                .collect(Collectors.toList()));
        assertEquals(4, written.stream().filter(moveLearnt -> moveLearnt.level == 1).count());
        assertTrue(written.stream().allMatch(moveLearnt -> allowedMoves.contains(moveLearnt.move)));
        assertEquals(1025, highSpecies.getNumber());
    }

    @Test
    public void evolutionMovesForAllAddsOnlyExpectedEvolutionMoveSlot() {
        Species highSpecies = species(1025, "HighSpecies");
        Map<Integer, List<MoveLearnt>> movesets = new LinkedHashMap<>();
        movesets.put(highSpecies.getNumber(), new ArrayList<>(List.of(
                new MoveLearnt(10, 1),
                new MoveLearnt(11, 16))));
        Set<Integer> allowedMoves = Set.of(50, 51, 52, 53, 54, 55);
        LearnsetTestRomHandler romHandler = LearnsetTestRomHandler.create(List.of(highSpecies),
                movesets, moves(allowedMoves));
        Settings settings = new Settings();
        settings.setMovesetsMod(Settings.MovesetsMod.COMPLETELY_RANDOM);
        settings.setEvolutionMovesForAll(true);

        SpeciesMovesetRandomizer randomizer = new SpeciesMovesetRandomizer(romHandler.proxy, settings, new Random(4));
        randomizer.randomizeMovesLearnt();

        List<MoveLearnt> written = romHandler.writtenMovesets.get(highSpecies.getNumber());
        assertTrue(randomizer.isChangesMade());
        assertEquals(1, romHandler.setMovesLearntCalls);
        assertFalse(written.isEmpty());
        assertEquals(3, written.size());
        assertEquals(List.of(0, 1, 16), written.stream()
                .map(moveLearnt -> moveLearnt.level)
                .collect(Collectors.toList()));
        assertTrue(written.stream().allMatch(moveLearnt -> allowedMoves.contains(moveLearnt.move)));
        assertEquals(1025, highSpecies.getNumber());
    }

    @Test
    public void randomizeMovesLearntDoesNotAlterStarterEvolutionChain() {
        Species bulbasaur = species(1, "Bulbasaur");
        Species ivysaur = species(2, "Ivysaur");
        Species venusaur = species(3, "Venusaur");
        Species squirtle = species(7, "Squirtle");
        Species wartortle = species(8, "Wartortle");
        addEvolution(bulbasaur, ivysaur, 16);
        addEvolution(ivysaur, venusaur, 32);
        addEvolution(squirtle, wartortle, 16);
        Map<Integer, List<String>> evolutionsBefore = evolutionTargetsBySpecies(List.of(bulbasaur, ivysaur,
                venusaur, squirtle, wartortle));
        Map<Integer, List<MoveLearnt>> movesets = new LinkedHashMap<>();
        movesets.put(bulbasaur.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        movesets.put(ivysaur.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        movesets.put(venusaur.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        movesets.put(squirtle.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        movesets.put(wartortle.getNumber(), movesLearnt(10, 1, 11, 7, 12, 14));
        LearnsetTestRomHandler romHandler = LearnsetTestRomHandler.create(List.of(bulbasaur, ivysaur, venusaur,
                squirtle, wartortle), movesets, moves(Set.of(40, 41, 42, 43, 44, 45, 46, 47)));
        Settings settings = new Settings();
        settings.setMovesetsMod(Settings.MovesetsMod.COMPLETELY_RANDOM);

        new SpeciesMovesetRandomizer(romHandler.proxy, settings, new Random(5)).randomizeMovesLearnt();

        assertEquals(evolutionsBefore, evolutionTargetsBySpecies(List.of(bulbasaur, ivysaur, venusaur,
                squirtle, wartortle)));
        assertEquals(List.of("Ivysaur:LEVEL:16"), evolutionTargetsBySpecies(List.of(bulbasaur)).get(1));
        assertEquals(List.of("Venusaur:LEVEL:32"), evolutionTargetsBySpecies(List.of(ivysaur)).get(2));
        assertEquals(List.of("Wartortle:LEVEL:16"), evolutionTargetsBySpecies(List.of(squirtle)).get(7));
        assertTrue(venusaur.getEvolutionsFrom().isEmpty());
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        species.setPrimaryType(Type.NORMAL);
        species.setAttack(60);
        species.setSpatk(60);
        return species;
    }

    private static void addEvolution(Species from, Species to, int level) {
        Evolution evolution = new Evolution(from, to, EvolutionType.LEVEL, level);
        from.getEvolutionsFrom().add(evolution);
        to.getEvolutionsTo().add(evolution);
    }

    private static Map<Integer, List<String>> evolutionTargetsBySpecies(List<Species> species) {
        Map<Integer, List<String>> evolutions = new LinkedHashMap<>();
        for (Species current : species) {
            evolutions.put(current.getNumber(), current.getEvolutionsFrom().stream()
                    .map(evolution -> evolution.getTo().getName() + ":" + evolution.getType() + ":"
                            + evolution.getExtraInfo())
                    .collect(Collectors.toList()));
        }
        return evolutions;
    }

    private static List<MoveLearnt> movesLearnt(int moveOne, int levelOne, int moveTwo, int levelTwo,
                                                int moveThree, int levelThree) {
        return new ArrayList<>(List.of(new MoveLearnt(moveOne, levelOne), new MoveLearnt(moveTwo, levelTwo),
                new MoveLearnt(moveThree, levelThree)));
    }

    private static List<Move> moves(Set<Integer> moveNumbers) {
        List<Move> moves = new ArrayList<>();
        for (int moveNumber : moveNumbers) {
            moves.add(move(moveNumber, 80));
        }
        return moves;
    }

    private static List<Move> indexedMoves(Move... moves) {
        int maxMoveNumber = 0;
        for (Move move : moves) {
            maxMoveNumber = Math.max(maxMoveNumber, move.number);
        }
        List<Move> indexedMoves = new ArrayList<>(Collections.nCopies(maxMoveNumber + 1, null));
        for (Move move : moves) {
            indexedMoves.set(move.number, move);
        }
        return indexedMoves;
    }

    private static Move move(int moveNumber, int power) {
        Move move = new Move();
        move.number = moveNumber;
        move.name = "Move" + moveNumber;
        move.power = power;
        move.pp = 15;
        move.hitratio = 100;
        move.type = Type.NORMAL;
        move.category = moveNumber % 2 == 0 ? MoveCategory.PHYSICAL : MoveCategory.SPECIAL;
        return move;
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
        private boolean supportsFourStartingMoves;

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
                case "supportsFourStartingMoves" -> supportsFourStartingMoves;
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
