package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.MegaEvolution;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;
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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for FVX-FOE-010, FVX-FOE-012 and FVX-FOE-014 Trainer special rules.
 */
public class TrainerSpecialRulesTest {

    @Test
    public void eliteFourUniquePokemonAvoidsDuplicateLeagueAces() {
        List<Species> species = speciesRange(12);
        Trainer eliteOne = trainer(1, "ELITE1", pokemon(species.get(0), 50));
        Trainer eliteTwo = trainer(2, "ELITE2", pokemon(species.get(0), 51));
        Trainer champion = trainer(3, "CHAMPION", pokemon(species.get(0), 52));
        TrainerTestRomHandler handler = TrainerTestRomHandler.create(
                species,
                List.of(eliteOne, eliteTwo, champion),
                species.subList(0, 3),
                List.of(1, 2, 3));
        Settings settings = new Settings();
        settings.setTrainersMod(Settings.TrainersMod.RANDOM);
        settings.setEliteFourUniquePokemonNumber(1);

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, settings, new Random(17));
        randomizer.randomizeTrainerPokes();

        Set<Species> leagueAces = new HashSet<>();
        leagueAces.add(eliteOne.getPokemon().get(0).getSpecies());
        leagueAces.add(eliteTwo.getPokemon().get(0).getSpecies());
        leagueAces.add(champion.getPokemon().get(0).getSpecies());
        assertEquals(3, leagueAces.size());
        assertTrue(randomizer.isChangesMade());
    }

    @Test
    public void rivalCarriesStarterThroughSyntheticRivalTeams() {
        Species playerStarter = species(101, "PlayerStarter");
        Species rivalStarter = species(102, "RivalStarter");
        Species friendStarter = species(103, "FriendStarter");
        Species rivalStageTwo = species(202, "RivalStageTwo");
        linkEvolution(rivalStarter, rivalStageTwo, 16);
        Species filler = species(301, "Filler");
        Trainer earlyRival = trainer(1, "RIVAL1-0", pokemon(filler, 5));
        Trainer laterRival = trainer(2, "RIVAL2-0", pokemon(filler, 20));
        TrainerTestRomHandler handler = TrainerTestRomHandler.create(
                List.of(playerStarter, rivalStarter, friendStarter, rivalStageTwo, filler),
                List.of(earlyRival, laterRival),
                List.of(playerStarter, rivalStarter, friendStarter),
                Collections.emptyList());

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, new Settings(), new Random(5));
        randomizer.makeRivalCarryStarter();

        assertSame(rivalStarter, earlyRival.getPokemon().get(0).getSpecies());
        assertSame(rivalStageTwo, laterRival.getPokemon().get(0).getSpecies());
        assertTrue(randomizer.isChangesMade());
    }

    @Test
    public void firstRivalCarryStarterSyncsOnlyOpeningRivalAndFriendBattles() {
        Species playerStarter = species(101, "PlayerStarter");
        Species rivalStarter = species(102, "RivalStarter");
        Species friendStarter = species(103, "FriendStarter");
        Species filler = species(301, "Filler");
        Trainer firstRival = trainer(1, "RIVAL1-0", pokemon(filler, 5));
        Trainer laterRival = trainer(2, "RIVAL2-0", pokemon(filler, 20));
        Trainer firstFriend = trainer(3, "FRIEND1-0", pokemon(filler, 5));
        Trainer laterFriend = trainer(4, "FRIEND2-0", pokemon(filler, 20));
        TrainerTestRomHandler handler = TrainerTestRomHandler.create(
                List.of(playerStarter, rivalStarter, friendStarter, filler),
                List.of(firstRival, laterRival, firstFriend, laterFriend),
                List.of(playerStarter, rivalStarter, friendStarter),
                Collections.emptyList());

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, new Settings(), new Random(5));
        randomizer.makeFirstRivalCarryStarter();

        assertSame(rivalStarter, firstRival.getPokemon().get(0).getSpecies());
        assertSame(friendStarter, firstFriend.getPokemon().get(0).getSpecies());
        assertSame(filler, laterRival.getPokemon().get(0).getSpecies());
        assertSame(filler, laterFriend.getPokemon().get(0).getSpecies());
        assertTrue(randomizer.isChangesMade());
    }

    @Test
    public void firstRivalCarryStarterUsesRandomizedCounterStarterSlot() {
        Species beedrillSlot = species(15, "Beedrill");
        Species counterSlot = species(102, "RandomizedCounterSlot");
        Species thirdSlot = species(103, "RandomizedThirdSlot");
        Species vanillaSquirtle = species(7, "Squirtle");
        Trainer firstRivalForBeedrillSlot = trainer(1, "RIVAL1-0", pokemon(vanillaSquirtle, 5));
        TrainerTestRomHandler handler = TrainerTestRomHandler.create(
                List.of(beedrillSlot, counterSlot, thirdSlot, vanillaSquirtle),
                List.of(firstRivalForBeedrillSlot),
                List.of(beedrillSlot, counterSlot, thirdSlot),
                Collections.emptyList());

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, new Settings(), new Random(5));
        randomizer.makeFirstRivalCarryStarter();

        Species rivalSpecies = firstRivalForBeedrillSlot.getPokemon().get(0).getSpecies();
        assertSame(counterSlot, rivalSpecies);
        assertNotSame(vanillaSquirtle, rivalSpecies);
        assertTrue(randomizer.isChangesMade());
    }

    @Test
    public void frlgOpeningRivalTrainerIdsUseRandomizedCounterStarterSlots() {
        Species hattrem = species(857, "Hattrem");
        Species clefairy = species(35, "Clefairy");
        Species golisopod = species(768, "Golisopod");
        Species squirtle = species(7, "Squirtle");
        Species bulbasaur = species(1, "Bulbasaur");
        Species charmander = species(4, "Charmander");
        Trainer rivalSquirtle = trainer(326, null, pokemon(squirtle, 5));
        Trainer rivalBulbasaur = trainer(327, null, pokemon(bulbasaur, 5));
        Trainer rivalCharmander = trainer(328, null, pokemon(charmander, 5));
        List<Species> starters = List.of(hattrem, clefairy, golisopod);
        TrainerTestRomHandler handler = TrainerTestRomHandler.create(
                List.of(hattrem, clefairy, golisopod, squirtle, bulbasaur, charmander),
                List.of(rivalSquirtle, rivalBulbasaur, rivalCharmander),
                starters,
                Collections.emptyList());

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, new Settings(), new Random(5));
        randomizer.syncFrlgOpeningRivalTrainerIds(handler.trainers, starters);

        assertSame(golisopod, rivalSquirtle.getPokemon().get(0).getSpecies());
        assertSame(hattrem, rivalBulbasaur.getPokemon().get(0).getSpecies());
        assertSame(clefairy, rivalCharmander.getPokemon().get(0).getSpecies());
        assertNotSame(squirtle, rivalSquirtle.getPokemon().get(0).getSpecies());
        assertNotSame(bulbasaur, rivalBulbasaur.getPokemon().get(0).getSpecies());
        assertNotSame(golisopod, rivalBulbasaur.getPokemon().get(0).getSpecies());
    }

    @Test
    public void trainerEvolutionAndLevelModifierMutateSyntheticTrainerPokemon() {
        Species basic = species(401, "Basic");
        Species stageTwo = species(402, "StageTwo");
        Species finalStage = species(403, "FinalStage");
        linkEvolution(basic, stageTwo, 16);
        linkEvolution(stageTwo, finalStage, 36);
        Trainer trainer = trainer(1, null, pokemon(basic, 24), pokemon(stageTwo, 40));
        TrainerTestRomHandler handler = TrainerTestRomHandler.create(
                List.of(basic, stageTwo, finalStage),
                List.of(trainer),
                List.of(basic, stageTwo, finalStage),
                Collections.emptyList());
        Settings settings = new Settings();
        settings.setTrainersLevelModifier(25);
        settings.setTrainersEvolutionLevelModifier(-50);

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, settings, new Random(9));
        randomizer.applyTrainerLevelModifier();
        randomizer.evolveTrainerPokemonAsFarAsLegal();

        assertEquals(30, trainer.getPokemon().get(0).getLevel());
        assertEquals(50, trainer.getPokemon().get(1).getLevel());
        assertSame(finalStage, trainer.getPokemon().get(0).getSpecies());
        assertSame(finalStage, trainer.getPokemon().get(1).getSpecies());
        assertTrue(randomizer.isChangesMade());
    }

    private static Trainer trainer(int index, String tag, TrainerPokemon... pokemon) {
        Trainer trainer = new Trainer();
        trainer.setIndex(index);
        trainer.setTag(tag);
        trainer.setPokemon(new ArrayList<>(List.of(pokemon)));
        return trainer;
    }

    private static TrainerPokemon pokemon(Species species, int level) {
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setSpecies(species);
        trainerPokemon.setLevel(level);
        trainerPokemon.setAbilitySlot(1);
        return trainerPokemon;
    }

    private static List<Species> speciesRange(int count) {
        List<Species> species = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            species.add(species(i, "Species" + i));
        }
        return species;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        species.setPrimaryType(Type.NORMAL);
        species.setAbility1(1);
        species.setAbility2(2);
        species.setHp(50 + number % 20);
        species.setAttack(50);
        species.setDefense(50);
        species.setSpatk(50);
        species.setSpdef(50);
        species.setSpeed(50);
        return species;
    }

    private static void linkEvolution(Species from, Species to, int level) {
        Evolution evolution = new Evolution(from, to, EvolutionType.LEVEL, level);
        from.getEvolutionsFrom().add(evolution);
        to.getEvolutionsTo().add(evolution);
    }

    private static SpeciesSet speciesSet(List<Species> species) {
        SpeciesSet speciesSet = new SpeciesSet();
        speciesSet.addAll(species);
        return speciesSet;
    }

    private static class TrainerTestRomHandler implements InvocationHandler {
        private final List<Species> species;
        private final SpeciesSet speciesSet;
        private final List<Trainer> trainers;
        private final List<Species> starters;
        private final List<Integer> eliteFourTrainers;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;

        private TrainerTestRomHandler(List<Species> species, List<Trainer> trainers,
                                      List<Species> starters, List<Integer> eliteFourTrainers) {
            this.species = species;
            this.speciesSet = speciesSet(species);
            this.trainers = trainers;
            this.starters = starters;
            this.eliteFourTrainers = eliteFourTrainers;
        }

        private static TrainerTestRomHandler create(List<Species> species, List<Trainer> trainers,
                                                    List<Species> starters, List<Integer> eliteFourTrainers) {
            TrainerTestRomHandler handler = new TrainerTestRomHandler(species, trainers, starters, eliteFourTrainers);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] { RomHandler.class }, handler);
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
                case "getAltFormes", "getIrregularFormes", "getBannedFormesForTrainerPokemon" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "getTypeTable" -> new TypeTable(List.of(Type.NORMAL, Type.FIRE, Type.WATER, Type.GRASS));
                case "getTrainers" -> trainers;
                case "getStarters" -> starters;
                case "getMainPlaythroughTrainers" -> Collections.<Integer>emptyList();
                case "getEliteFourTrainers" -> eliteFourTrainers;
                case "getGymAndEliteTypeThemes" -> Collections.<String, Type>emptyMap();
                case "getAltFormeOfSpecies" -> args[0];
                case "abilitiesPerSpecies" -> 2;
                case "isYellow", "isORAS" -> false;
                case "generationOfPokemon" -> 3;
                case "getHighestEvoLvl" -> 40;
                case "toString" -> "TrainerSpecialRulesTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
