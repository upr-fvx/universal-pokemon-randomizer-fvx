package test.romhandlers;

import com.dabomstew.pkrandom.RomFunctions;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.Gen7Constants;
import com.dabomstew.pkrandom.game_data.*;
import com.dabomstew.pkrandom.randomizers.PokemonTypeRandomizer;
import com.dabomstew.pkrandom.randomizers.TrainerPokemonRandomizer;
import com.dabomstew.pkrandom.romhandlers.AbstractGBRomHandler;
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
    public void trainersDoNotChangeWithGetAndSet(String romName) {
        // TODO: this comparison needs to be deeper
        loadROM(romName);
        List<Trainer> trainers = romHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        romHandler.setTrainers(trainers);
        assertEquals(before, romHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersDoNotChangeWithLoadAndSave(String romName) {
        // TODO: this comparison needs to be deeper
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersHaveAtLeastTwoPokemonAfterSettingDoubleBattleMode(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        loadROM(romName);
        new TrainerPokemonRandomizer(romHandler, new Settings(), RND).setDoubleBattleMode();
        for (Trainer trainer : romHandler.getTrainers()) {
            System.out.println(trainer);
            if (trainer.forcedDoubleBattle) {
                assertTrue(trainer.pokemon.size() >= 2);
            } else {
                System.out.println("Not a forced double battle.");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersSaveAndLoadCorrectlyAfterSettingDoubleBattleMode(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 3);
        loadROM(romName);
        new TrainerPokemonRandomizer(romHandler, new Settings(), RND).setDoubleBattleMode();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToBossTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(6);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToImportantTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToImportantTrainers());
        Settings s = new Settings();
        s.setAdditionalImportantTrainerPokemon(6);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToBossAndImportantTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(6);
        s.setAdditionalImportantTrainerPokemon(6);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddPokemonToAllTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers()
                && romHandler.canAddPokemonToRegularTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(5);
        s.setAdditionalImportantTrainerPokemon(5);
        s.setAdditionalRegularTrainerPokemon(5);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addPokemonToBossAndImportantTrainersAndSaveAndLoadGivesThemFullParties(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(5);
        s.setAdditionalImportantTrainerPokemon(5);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        for (Trainer tr : gbRomHandler.getTrainers()) {
            if (tr.multiBattleStatus == Trainer.MultiBattleStatus.NEVER && !tr.shouldNotGetBuffs()
                    && (tr.isBoss() || tr.isImportant())) {
                System.out.println(tr);
                assertEquals(6, tr.pokemon.size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addPokemonToAllTrainersAndSaveAndLoadGivesThemFullParties(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers() && romHandler.canAddPokemonToImportantTrainers()
                && romHandler.canAddPokemonToRegularTrainers());
        Settings s = new Settings();
        s.setAdditionalBossTrainerPokemon(5);
        s.setAdditionalImportantTrainerPokemon(5);
        s.setAdditionalRegularTrainerPokemon(5);
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        for (Trainer tr : gbRomHandler.getTrainers()) {
            if (tr.multiBattleStatus == Trainer.MultiBattleStatus.NEVER && !tr.shouldNotGetBuffs()) {
                System.out.println(tr);
                assertEquals(6, tr.pokemon.size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToBossTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToImportantTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToBossAndImportantTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canAddHeldItemsToAllTrainersAndSaveAndLoad(String romName) {
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        assumeTrue(romHandler.canAddHeldItemsToRegularTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        s.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        List<Trainer> before = new ArrayList<>(trainers);
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        assertEquals(before, gbRomHandler.getTrainers());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addHeldItemsToAllTrainersAndSaveAndLoadGivesThemHeldItems(String romName) {
        // Fails in Emerald because it can't give held items to Mossdeep Steven
        assumeTrue(isGBGame(romName));
        loadROM(romName);
        assumeTrue(romHandler.canAddHeldItemsToBossTrainers());
        assumeTrue(romHandler.canAddHeldItemsToImportantTrainers());
        assumeTrue(romHandler.canAddHeldItemsToRegularTrainers());
        Settings s = new Settings();
        s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
        s.setRandomizeHeldItemsForBossTrainerPokemon(true);
        s.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerHeldItems();
        AbstractGBRomHandler gbRomHandler = (AbstractGBRomHandler) romHandler;
        List<Trainer> trainers = gbRomHandler.getTrainers();
        gbRomHandler.setTrainers(trainers);
        gbRomHandler.saveTrainers();
        gbRomHandler.loadTrainers();
        for (Trainer tr : gbRomHandler.getTrainers()) {
            // TODO: add clause to ignore mossdeep steven
            System.out.println(tr.fullDisplayName);
            if (tr.shouldNotGetBuffs()) {
                System.out.println("skip");
            } else {
                for (TrainerPokemon tp : tr.pokemon) {
                    System.out.println(tp);
                    assertNotEquals(0, tp.getHeldItem().getId());
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedWorks(String romName) {
        loadROM(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);

        Settings s = new Settings();
        s.setTrainersMod(false, false, false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemedCheck(beforeTrainerStrings, typeThemedTrainers);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedOrPrimaryWorks(String romName) {
        loadROM(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        Map<Trainer, List<Species>> nonTypeThemedTrainers;
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);
        nonTypeThemedTrainers = recordTrainerPokemon();
        typeThemedTrainers.keySet().forEach(nonTypeThemedTrainers::remove);

        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.KEEP_THEME_OR_PRIMARY);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemeOrPrimaryCheck(beforeTrainerStrings, typeThemedTrainers, nonTypeThemedTrainers);
    }

    /**
     * Checks that trainers which had themes before randomizing still have them.
     * If nonTypeThemedTrainers is not null, also checks if trainers that did not have themes
     * have preserved the primary type of their Pokemon.
     */
    private void keepTypeThemeOrPrimaryCheck(Map<Trainer, List<String>> beforeTrainerStrings,
                                             Map<Trainer, Type> typeThemedTrainers,
                                             Map<Trainer, List<Species>> nonTypeThemedTrainers) {

        for (Trainer tr : romHandler.getTrainers()) {
            List<String> beforeStrings = beforeTrainerStrings.get(tr);
            System.out.println("Before: " + beforeStrings.get(0));
            for (int i = 1; i < beforeStrings.size(); i++) {
                System.out.println("\t" + beforeStrings.get(i));
            }

            if (typeThemedTrainers.containsKey(tr)) {
                Type theme = typeThemedTrainers.get(tr);
                System.out.println("Type Theme: " + theme);
                System.out.println("After: " + tr);
                for (TrainerPokemon tp : tr.pokemon) {
                    Species pk = tp.getSpecies();
                    System.out.println("\t" + pk);
                    assertTrue(pk.getPrimaryType(false) == theme || pk.getSecondaryType(false) == theme);
                }
            } else {
                System.out.println("Not Type Themed");
                if(nonTypeThemedTrainers != null) {
                    System.out.println("After: " + tr);

                    List<Species> before = nonTypeThemedTrainers.get(tr);
                    List<TrainerPokemon> after = tr.pokemon;

                    if (before.size() < after.size()) {
                        throw new IllegalStateException("Trainer removed Pokemon!");
                    }

                    for (int i = 0; i < before.size(); i++) {
                        Species beforePoke = before.get(i);
                        Species afterPoke = after.get(i).getSpecies();

                        System.out.println("\t\tBefore: " + beforePoke.getName() + "; Primary type: "
                                + beforePoke.getPrimaryType(true).name());
                        System.out.println("\t\tAfter: " + afterPoke.getName() + "; Types: "
                                + afterPoke.getPrimaryType(false).name()
                                + (afterPoke.hasSecondaryType(false) ?
                                ", " + afterPoke.getSecondaryType(false) : ""));

                        assertTrue(afterPoke.hasType(beforePoke.getPrimaryType(true), false));
                    }
                }
            }
            System.out.println();
        }
    }

    /**
     * Records the species (in order) of the Pokemon for every trainer in the romHandler.
     * @return A Map of every trainer to a list of their Pokemon's species.
     */
    private Map<Trainer, List<Species>> recordTrainerPokemon() {
        Map<Trainer, List<Species>> trainersWithPokemonTypes = new HashMap<>();
        for(Trainer trainer : romHandler.getTrainers()) {
            List<Species> speciesPrimaryTypes = new ArrayList<>();
            for (TrainerPokemon pokemon : trainer.pokemon) {
                speciesPrimaryTypes.add(pokemon.getSpecies());
            }
            trainersWithPokemonTypes.put(trainer, speciesPrimaryTypes);
        }

        return trainersWithPokemonTypes;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedWorksWithRandomPokemonTypes(String romName) {
        loadROM(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);

        Settings s = new Settings();
        s.setTypesMod(false, false, true);
        new PokemonTypeRandomizer(romHandler, s, RND).randomizePokemonTypes();
        s.setTrainersMod(false, false, false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemedCheck(beforeTrainerStrings, typeThemedTrainers);
    }

    private void recordTypeThemeBefore(Map<Trainer, List<String>> beforeTrainerStrings, Map<Trainer, Type> typeThemedTrainers) {
        Map <String, Type> gymTypeMap = romHandler.getGymAndEliteTypeThemes();

        for (Trainer tr : romHandler.getTrainers()) {
            List<String> beforeStrings = new ArrayList<>();
            beforeTrainerStrings.put(tr, beforeStrings);
            beforeStrings.add(tr.toString());
            for (TrainerPokemon tp : tr.pokemon) {
                beforeStrings.add(tp.getSpecies().toString());
            }

            // the rival in yellow is forced to always have eevee, which causes a mess if eevee's type is randomized
            if (tr.tag != null && tr.tag.contains("RIVAL") && romHandler.isYellow()) continue;

            String gymTag = tr.tag;
            if(gymTag != null) {
                gymTag = gymTag.split("-")[0];

                //special Giovanni case
                if(gymTag.contains("GIO")) {
                    gymTag = "GYM8";
                }
            }

            Type theme = getThemedTrainerType(tr);
            if(gymTypeMap.containsKey(gymTag)) {
                beforeStrings.add("Forced theme from " + gymTag);
                theme = gymTypeMap.get(gymTag);
            }
            if (theme != null) {
                typeThemedTrainers.put(tr, theme);
            }
        }
    }

    private Type getThemedTrainerType(Trainer tr) {
        Species first = tr.pokemon.get(0).getSpecies();
        Type primary = first.getPrimaryType(true);
        Type secondary = first.getSecondaryType(true);
        for (int i = 1; i < tr.pokemon.size(); i++) {
            Species pk = tr.pokemon.get(i).getSpecies();
            if (secondary != null) {
                if (secondary != pk.getPrimaryType(true) && secondary != pk.getSecondaryType(true)) {
                    secondary = null;
                }
            }
            if (primary != pk.getPrimaryType(true) && primary != pk.getSecondaryType(true)) {
                primary = secondary;
                secondary = null;
            }
            if (primary == null) {
                return null; //no type is shared, no need to look at the remaining pokemon
            }
        }

        //we have a type theme!
        if (primary == Type.NORMAL && secondary != null) {
            //Bird override
            //(Normal is less significant than other types, for example, Flying)
            return secondary;
        } else {
            return primary;
        }

    }

    private void keepTypeThemedCheck(Map<Trainer, List<String>> beforeTrainerStrings, Map<Trainer, Type> typeThemedTrainers) {
        keepTypeThemeOrPrimaryCheck(beforeTrainerStrings, typeThemedTrainers, null);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void useLocalPokemonGuaranteesLocalPokemonOnly(String romName) {
        loadROM(romName);
        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setTrainersUseLocalPokemon(true);
        s.setUseTimeBasedEncounters(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        SpeciesSet localWithRelatives = romHandler.getMainGameWildPokemon(s.isUseTimeBasedEncounters())
                        .buildFullFamilies(false);
        SpeciesSet all = romHandler.getPokemonSet();
        SpeciesSet nonLocal = new SpeciesSet(all);
        nonLocal.removeAll(localWithRelatives);

        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);

            // ignore the yellow rival and his forced eevee
            if (tr.tag != null && tr.tag.contains("RIVAL") && romHandler.isYellow()) {
                continue;
            }

            for (TrainerPokemon tp : tr.pokemon) {
                System.out.println(tp.getSpecies());
                assertTrue(localWithRelatives.contains(tp.getSpecies()));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void useLocalPokemonAndE4UniquePokesGivesE4NonLocalPokemon(String romName) {
        int wantedNonLocal = 1; // you can play around with this value between 1-6 but what's important is it works for 1

        loadROM(romName);
        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setTrainersUseLocalPokemon(true);
        s.setEliteFourUniquePokemonNumber(wantedNonLocal);
        s.setUseTimeBasedEncounters(true); // should be at least 4 non-local Pokemon in each game
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        SpeciesSet localWithRelatives = romHandler.getMainGameWildPokemon(s.isUseTimeBasedEncounters())
                .buildFullFamilies(false);
        SpeciesSet all = romHandler.getPokemonSet();
        SpeciesSet nonLocal = new SpeciesSet(all);
        nonLocal.removeAll(localWithRelatives);

        List<Integer> eliteFourIndices = romHandler.getEliteFourTrainers(false);
        assumeTrue(!eliteFourIndices.isEmpty());
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println("\n" + tr);

            if (eliteFourIndices.contains(tr.index)) {
                System.out.println("-E4 Member-");
                System.out.println("Non-local: " + nonLocal.stream().map(Species::getName).collect(Collectors.toList()));
                System.out.println("Local: " + localWithRelatives.stream().map(Species::getName).collect(Collectors.toList()));
                int nonLocalCount = 0;
                for (TrainerPokemon tp : tr.pokemon) {
                    if (nonLocal.contains(tp.getSpecies())) {
                        nonLocalCount++;
                        System.out.println(tp.getSpecies().getName() + " is nonlocal");
                    }
                }
                assertTrue(nonLocalCount == wantedNonLocal || nonLocalCount == tr.pokemon.size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueElite4PokemonGivesUniquePokemonToSaidTrainers(String romName) {
        loadROM(romName);

        Settings s = new Settings();
        s.setEliteFourUniquePokemonNumber(1);
        s.setTrainersMod(false, true, false, false, false);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        elite4UniquePokemonCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueElite4PokemonGivesUniquePokemonToSaidTrainersWithUseLocal(String romName) {
        loadROM(romName);

        Settings s = new Settings();
        s.setTrainersUseLocalPokemon(true);
        s.setEliteFourUniquePokemonNumber(1);
        s.setTrainersMod(false, true, false, false, false);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        elite4UniquePokemonCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueElite4PokemonGivesUniquePokemonToSaidTrainersWithTypeThemes(String romName) {
        loadROM(romName);

        Settings s = new Settings();
        //s.setTrainersUseLocalPokemon(true);
        s.setEliteFourUniquePokemonNumber(1);
        s.setTrainersMod(false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        elite4UniquePokemonCheck();
    }

    private void elite4UniquePokemonCheck() {
        List<Trainer> trainers = romHandler.getTrainers();
        int[] pokeCount = new int[romHandler.getPokemon().size()];
        for (Trainer tr : trainers) {
            System.out.println(tr);
            for (TrainerPokemon tp : tr.pokemon) {
                Species pk = tp.getSpecies();
                pokeCount[pk.getNumber()]++;
            }
        }

        List<Species> allPokes = romHandler.getPokemon();
        for (int i = 1; i < allPokes.size(); i++) {
            System.out.println(allPokes.get(i).getName() + " : " + pokeCount[i]);
        }

        List<Integer> eliteFourIndices = romHandler.getEliteFourTrainers(false);
        assumeTrue(!eliteFourIndices.isEmpty());
        for (Trainer tr : romHandler.getTrainers()) {
            if (eliteFourIndices.contains(tr.index)) {
                System.out.println(tr);
                int minCount = Integer.MAX_VALUE;
                for (TrainerPokemon tp : tr.pokemon) {
                    Species pk = tp.getSpecies();
                    System.out.println(pk.getName() + ":" + pokeCount[pk.getNumber()]);
                    minCount = Math.min(minCount, pokeCount[pk.getNumber()]);
                }
                assertEquals(1, minCount);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void elite4MembersWithMultipleBattlesGetSameTypeThemeForAll(String romName) {
        //with random type themes
        loadROM(romName);

        Settings s = new Settings();
        s.setTrainersMod(false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        Map<String, List<Type>> e4Types = new HashMap<>();
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.tag != null && tr.tag.contains("ELITE")) {
                String memberTag = tr.tag.split("-")[0];
                if (!e4Types.containsKey(memberTag)) {
                    e4Types.put(memberTag, new ArrayList<>());
                }
                e4Types.get(memberTag).add(getThemedTrainerType(tr));
                //TODO: add handling for double-theme battles (e.g. all pokemon are Fire-Fighting)
            }
        }

        System.out.println(e4Types);
        for (Map.Entry<String, List<Type>> entry : e4Types.entrySet()) {
            Set<Type> uniques = new HashSet<>(entry.getValue());
            System.out.println(entry.getKey() + " has " + uniques.size() + " type theme(s)");
            assertEquals(1, uniques.size());
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

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomUsableZCrystalsDoesNotChangeWhichPokemonHaveZCrystals(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 7);
        loadROM(romName);

        System.out.println("==== BEFORE: ====");
        Map<Integer, boolean[]> before = findZCrystals();
        new TrainerPokemonRandomizer(romHandler, new Settings(), RND).randomUsableZCrystals();
        System.out.println("==== AFTER: ====");
        Map<Integer, boolean[]> after = findZCrystals();

        assertEquals(before.keySet(), after.keySet());
        for (Integer key : before.keySet()) {
            assertArrayEquals(before.get(key), after.get(key));
        }
    }

    private Map<Integer, boolean[]> findZCrystals() {
        List<Item> items = romHandler.getItems();

        Map<Integer, boolean[]> zCrystalsByTrainer = new HashMap<>();
        List<Trainer> trainersBefore = romHandler.getTrainers();
        for (int i = 0; i < trainersBefore.size(); i++) {
            Trainer tr = trainersBefore.get(i);
            System.out.println(tr);
            boolean[] zCrystals = new boolean[tr.pokemon.size()];
            boolean anyHasZCrystal = false;
            for (int pkNum = 0; pkNum < tr.pokemon.size(); pkNum++) {
                TrainerPokemon tp = tr.pokemon.get(pkNum);
                System.out.println(items.get(tp.getHeldItem().getId()));
                if (Gen7Constants.heldZCrystalsByType.containsValue(tp.getHeldItem().getId())) {
                    zCrystals[pkNum] = true;
                    anyHasZCrystal = true;
                }
            }
            if (anyHasZCrystal) {
                zCrystalsByTrainer.put(i, zCrystals);
            }
        }
        return zCrystalsByTrainer;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomUsableZCrystalsGivesZCrystalsOfUsableType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 7);
        loadROM(romName);

        List<Item> items = romHandler.getItems();

        new TrainerPokemonRandomizer(romHandler, new Settings(), RND).randomUsableZCrystals();
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);
            for (TrainerPokemon tp : tr.pokemon) {
                if (Gen7Constants.heldZCrystalsByType.containsValue(tp.getHeldItem().getId())) {
                    System.out.println(tp.getSpecies().getName() + " holds " + items.get(tp.getHeldItem().getId()).getName());

                    int[] pkMoves = tp.isResetMoves() ?
                            RomFunctions.getMovesAtLevel(tp.getSpecies().getNumber(), romHandler.getMovesLearnt(), tp.getLevel())
                            : Arrays.stream(tp.getMoves()).distinct().filter(mv -> mv != 0).toArray();
                    Set<Type> moveTypes = new HashSet<>();
                    for (int moveID : pkMoves) {
                        Move mv = romHandler.getMoves().get(moveID);
                        System.out.println(mv.name + " | " + mv.type);
                        moveTypes.add(mv.type);
                    }

                    boolean anyMoveTypeCorrect = false;
                    for (Type t : moveTypes) {
                        if (Gen7Constants.heldZCrystalsByType.get(t) == tp.getHeldItem().getId()) {
                            anyMoveTypeCorrect = true;
                        }
                    }
                    assertTrue(anyMoveTypeCorrect);
                }
            }
        }
    }

}
