package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerTrainerTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getTrainers().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersContainsNoDuplicates(String romName) {
        loadROM(romName);
        Map<Trainer, List<Integer>> trainerIndices = new HashMap<>();
        List<Trainer> trainers = romHandler.getTrainers();
        for (int i = 0; i < trainers.size(); i++) {
            Trainer tr = trainers.get(i);
            if (!trainerIndices.containsKey(tr)) {
                trainerIndices.put(tr, new ArrayList<>());
            }
            trainerIndices.get(tr).add(i);
        }

        int duplicatedTrainers = 0;
        for (Map.Entry<Trainer, List<Integer>> entry : trainerIndices.entrySet()) {
            if (entry.getValue().size() != 1) {
                System.out.println("duplicated trainer: " + entry);
                duplicatedTrainers++;
            }
        }
        assertEquals(0, duplicatedTrainers);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersDoNotChangeWithLoadAndSave(String romName) {
        // TODO: this comparison needs to be deeper
        loadROM(romName);
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    // TODO: write tests on forcing different battle styles (double, triple, etc.).
    //  There was one in the past, but since it depended on randomizer classes
    //  (which romio tests shouldn't/can't), it was removed.

    /**
     * Applies some enhancing function to a subset of all {@link Trainer}s,
     * and saves+loads them from ROM.<br>
     * Returns a {@link List} of all trainers before as they were before being saved/loaded.
     */
    private List<Trainer> enhanceTrainersAndSaveAndLoad(Consumer<List<Trainer>> enhancer, Predicate<Trainer> filter) {
        List<Trainer> before = new ArrayList<>(romHandler.getTrainers());
        List<Trainer> bosses = before.stream().filter(filter).collect(Collectors.toList());
        enhancer.accept(bosses);
        romHandler.saveTrainers();
        romHandler.loadTrainers();

        return before;
    }

    private void giveCustomMovesetsToTrainers(List<Trainer> trainers) {
        for (Trainer tr : trainers) {
            for (TrainerPokemon tp : tr.getPokemon()) {
                tp.setMoves(new int[] {1, 2, 3, 4}); // four real moves
            }
        }
    }

    private void addPokemonToTrainers(List<Trainer> trainers) {
        for (Trainer tr : trainers) {
            TrainerPokemon toCopy = tr.getPokemon().get(0);
            while (tr.getPokemon().size() < 6) {
                tr.getPokemon().add(new TrainerPokemon(toCopy));
            }
        }
    }

    private void addHeldItemsToTrainers(List<Trainer> trainers) {
        Item oranBerry = romHandler.getItems().get(ItemIDs.oranBerry);
        for (Trainer tr : trainers) {
            for (TrainerPokemon tp : tr.getPokemon()) {
                tp.setHeldItem(oranBerry);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGiveCustomMovesets_ToBossTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::giveCustomMovesetsToTrainers, Trainer::isBoss);

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGiveCustomMovesets_ToBossAndImportantTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToImportantTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::giveCustomMovesetsToTrainers,
                tr -> tr.isBoss() || tr.isImportant());

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGiveCustomMovesets_ToAllTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToImportantTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToRegularTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::giveCustomMovesetsToTrainers, tr -> true);

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void giveCustomMovesets_ToAllTrainers_AndSaveAndLoad_GivesThemCustomMovesets(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToImportantTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToRegularTrainers());

        enhanceTrainersAndSaveAndLoad(this::giveCustomMovesetsToTrainers, tr -> true);

        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);
            tr.getPokemon().forEach(tp -> System.out.println(Arrays.toString(tp.getMoves())));
            assertTrue(tr.pokemonHaveCustomMoves());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemon_ToBossTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::addPokemonToTrainers, Trainer::isBoss);

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemon_ToBossAndImportantTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers());
        assumeTrue(romHandler.canAddPokemonToImportantTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::addPokemonToTrainers,
                tr -> tr.isBoss() || tr.isImportant());

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemon_ToAllTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers()
                && romHandler.canAddPokemonToRegularTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::addPokemonToTrainers, tr -> true);

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addPokemon_ToBossAndImportantTrainers_AndSaveAndLoad_GivesThemFullParties(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers());

        enhanceTrainersAndSaveAndLoad(this::addPokemonToTrainers, tr -> tr.isBoss() || tr.isImportant());

        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getMultiBattleStatus() == Trainer.MultiBattleStatus.NEVER && !tr.shouldNotGetBuffs()
                    && (tr.isBoss() || tr.isImportant())) {
                System.out.println(tr);
                assertEquals(6, tr.getPokemon().size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addPokemon_ToAllTrainers_AndSaveAndLoad_GivesThemFullParties(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers()
                && romHandler.canAddPokemonToRegularTrainers());

        enhanceTrainersAndSaveAndLoad(this::addPokemonToTrainers, tr -> true);

        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getMultiBattleStatus() == Trainer.MultiBattleStatus.NEVER && !tr.shouldNotGetBuffs()) {
                System.out.println(tr);
                assertEquals(6, tr.getPokemon().size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItems_ToBossTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::addHeldItemsToTrainers, Trainer::isBoss);

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItems_ToBossAndImportantTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::addHeldItemsToTrainers,
                tr -> tr.isBoss() || tr.isImportant());

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItems_ToAllTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        assumeTrue(romHandler.canAddHeldItemsToRegularTrainers());

        List<Trainer> before = enhanceTrainersAndSaveAndLoad(this::addHeldItemsToTrainers, tr -> true);

        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addHeldItems_ToAllTrainers_AndSaveAndLoad_GivesThemHeldItems(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        assumeTrue(romHandler.canAddHeldItemsToRegularTrainers());

        enhanceTrainersAndSaveAndLoad(this::addHeldItemsToTrainers, tr -> true);

        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr.getFullDisplayName());
            if (tr.shouldNotGetBuffs()) {
                System.out.println("skip");
            } else {
                for (TrainerPokemon tp : tr.getPokemon()) {
                    System.out.println(tp);
                    assertNotEquals(null, tp.getHeldItem());
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addingAsMuchAsPossibleToTrainers_AndSaveAndLoad_DoesNotCrash(String romName) {
        loadROM(romName);

        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> bossTrainers = trainers.stream().filter(Trainer::isBoss).collect(Collectors.toList());
        List<Trainer> importantTrainers = trainers.stream().filter(Trainer::isImportant).collect(Collectors.toList());
        List<Trainer> regularTrainers = trainers.stream().filter(Trainer::isRegular).collect(Collectors.toList());

        if (romHandler.canGiveCustomMovesetsToBossTrainers()) giveCustomMovesetsToTrainers(bossTrainers);
        if (romHandler.canGiveCustomMovesetsToImportantTrainers()) giveCustomMovesetsToTrainers(importantTrainers);
        if (romHandler.canGiveCustomMovesetsToRegularTrainers()) giveCustomMovesetsToTrainers(regularTrainers);

        if (romHandler.canAddPokemonToBossTrainers()) addPokemonToTrainers(bossTrainers);
        if (romHandler.canAddPokemonToImportantTrainers()) addPokemonToTrainers(importantTrainers);
        if (romHandler.canAddPokemonToRegularTrainers()) addPokemonToTrainers(regularTrainers);

        if (romHandler.canAddHeldItemsToBossTrainers()) addHeldItemsToTrainers(bossTrainers);
        if (romHandler.canAddHeldItemsToImportantTrainers()) addHeldItemsToTrainers(importantTrainers);
        if (romHandler.canAddHeldItemsToRegularTrainers()) addHeldItemsToTrainers(regularTrainers);

        romHandler.saveTrainers();
        romHandler.loadTrainers();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersTaggedRival1OrFriend1OnlyHaveCorrectStarters(String romName) {
        loadROM(romName);

        List<Species> starters = romHandler.getStarters();
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getTag() != null && (tr.getTag().contains("RIVAL1-") || tr.getTag().contains("FRIEND1-"))) {
                System.out.println(tr);

                int variant = Integer.parseInt(tr.getTag().split("-")[1]);
                int offset = tr.getTag().contains("RIVAL") ? 1 : 2;
                Species expected = starters.get((variant + offset) % 3);
                Species actual = tr.getPokemon().get(0).getSpecies();

                assertEquals(expected, actual);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersTaggedRivalOrFriendCarryStarter(String romName) {
        loadROM(romName);

        List<SpeciesSet> starterFamilies = romHandler.getStarters()
                .stream().map(sp -> sp.getFamily(false))
                .collect(Collectors.toList());

        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getTag() != null && (tr.getTag().contains("RIVAL") || tr.getTag().contains("FRIEND"))) {
                System.out.println(tr);
                int variant = Integer.parseInt(tr.getTag().split("-")[1]);
                if (romHandler.isYellow()) {
                    // Yellow uses the variant syntax, to instead refer to alternate battles,
                    // with different evos of the original starter.
                    // Here it doesn't matter though, so clear out variant.
                    variant = 0;
                }

                int offset = tr.getTag().contains("RIVAL") ? 1 : 2;
                SpeciesSet expectedFamily = starterFamilies.get((variant + offset) % 3);

                boolean carriesStarter = false;
                for (TrainerPokemon tp : tr.getPokemon()) {
                    if (expectedFamily.contains(tp.getSpecies())) {
                        carriesStarter = true;
                        break;
                    }
                }

                assertTrue(carriesStarter);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerNamesAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getTrainerNames().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerNamesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<String> trainerNames = romHandler.getTrainerNames();
        System.out.println(trainerNames);
        List<String> before = new ArrayList<>(trainerNames);
        romHandler.setTrainerNames(trainerNames);
        assertEquals(before, romHandler.getTrainerNames());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerClassNamesAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getTrainerClassNames().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerClassNamesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<String> trainerClassNames = romHandler.getTrainerClassNames();
        System.out.println(trainerClassNames);
        List<String> before = new ArrayList<>(trainerClassNames);
        romHandler.setTrainerClassNames(trainerClassNames);
        assertEquals(before, romHandler.getTrainerClassNames());
    }
}
