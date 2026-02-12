package com.uprfvx.romio.romhandlers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.randomizers.TrainerMovesetRandomizer;
import com.uprfvx.random.randomizers.TrainerPokemonRandomizer;
import com.uprfvx.romio.gamedata.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
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

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersSaveAndLoadCorrectlyAfterSettingDoubleBattleMode(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 2);
        loadROM(romName);
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.SINGLE_STYLE, BattleStyle.Style.DOUBLE_BATTLE));
        new TrainerPokemonRandomizer(romHandler, settings, RND).modifyBattleStyle();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGiveCustomMovesets_ToBossTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            if (!tr.isBoss()) continue;
            for (TrainerPokemon tp : tr.getPokemon()) {
                tp.setMoves(new int[] {1, 2, 3, 4}); // four real moves
            }
        }
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGiveCustomMovesets_ToBossAndImportantTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToImportantTrainers());
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            if (tr.isRegular()) continue;
            for (TrainerPokemon tp : tr.getPokemon()) {
                tp.setMoves(new int[] {1, 2, 3, 4}); // four real moves
            }
        }
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGiveCustomMovesets_ToAllTrainers_AndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToImportantTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToRegularTrainers());
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            for (TrainerPokemon tp : tr.getPokemon()) {
                tp.setMoves(new int[] {1, 2, 3, 4}); // four real moves
            }
        }
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void GiveCustomMovesets_ToAllTrainers_AndSaveAndLoad_GivesThemCustomMovesets(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canGiveCustomMovesetsToBossTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToImportantTrainers());
        assumeTrue(romHandler.canGiveCustomMovesetsToRegularTrainers());
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            for (TrainerPokemon tp : tr.getPokemon()) {
                tp.setMoves(new int[] {1, 2, 3, 4}); // four real moves
            }
        }
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);
            tr.getPokemon().forEach(tp -> System.out.println(Arrays.toString(tp.getMoves())));
            assertTrue(tr.pokemonHaveCustomMoves());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToBossTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(6);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToImportantTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToImportantTrainers());
        Settings s = new Settings();
        s.setAdditionalImportantTrainerPokemon(6);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToBossAndImportantTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(6);
        s.setAdditionalImportantTrainerPokemon(6);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToAllTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers()
                && romHandler.canAddPokemonToRegularTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(5);
        s.setAdditionalImportantTrainerPokemon(5);
        s.setAdditionalRegularTrainerPokemon(5);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addPokemonToBossAndImportantTrainersAndSaveAndLoadGivesThemFullParties(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(5);
        s.setAdditionalImportantTrainerPokemon(5);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        List<Trainer> trainers = romHandler.getTrainers();
        romHandler.saveTrainers();
        romHandler.loadTrainers();
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
    public void addPokemonToAllTrainersAndSaveAndLoadGivesThemFullParties(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers()
                && romHandler.canAddPokemonToRegularTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(5);
        s.setAdditionalImportantTrainerPokemon(5);
        s.setAdditionalRegularTrainerPokemon(5);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getMultiBattleStatus() == Trainer.MultiBattleStatus.NEVER && !tr.shouldNotGetBuffs()) {
                System.out.println(tr);
                assertEquals(6, tr.getPokemon().size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToBossTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToImportantTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToBossAndImportantTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToAllTrainersAndSaveAndLoad(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        assumeTrue(romHandler.canAddHeldItemsToRegularTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        s.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.saveTrainers();
        romHandler.loadTrainers();
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addHeldItemsToAllTrainersAndSaveAndLoadGivesThemHeldItems(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        assumeTrue(romHandler.canAddHeldItemsToRegularTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        s.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        romHandler.saveTrainers();
        romHandler.loadTrainers();
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

        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(romHandler.canAddPokemonToBossTrainers() ? 5 : 0);
        s.setAdditionalImportantTrainerPokemon(romHandler.canAddPokemonToImportantTrainers() ? 5 : 0);
        s.setAdditionalRegularTrainerPokemon(romHandler.canAddPokemonToRegularTrainers() ? 5 : 0);
        s.setRandomizeHeldItemsForBossTrainerPokemon(romHandler.canAddHeldItemsToBossTrainers());
        s.setRandomizeHeldItemsForImportantTrainerPokemon(romHandler.canAddHeldItemsToImportantTrainers());
        s.setRandomizeHeldItemsForRegularTrainerPokemon(romHandler.canAddHeldItemsToRegularTrainers());
        s.setBetterBossTrainerMovesets(romHandler.canGiveCustomMovesetsToBossTrainers());
        s.setBetterImportantTrainerMovesets(romHandler.canGiveCustomMovesetsToImportantTrainers());
        s.setBetterRegularTrainerMovesets(romHandler.canGiveCustomMovesetsToRegularTrainers());

        TrainerPokemonRandomizer tpr = new TrainerPokemonRandomizer(romHandler, s, RND);
        tpr.addTrainerPokemon();
        tpr.randomizeTrainerHeldItems();
        new TrainerMovesetRandomizer(romHandler, s, RND).randomizeTrainerMovesets();

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
