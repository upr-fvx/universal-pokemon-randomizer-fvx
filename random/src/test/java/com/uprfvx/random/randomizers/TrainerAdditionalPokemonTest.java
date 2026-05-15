package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.MegaEvolution;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for FVX-FOE-005/006/007 Additional Pokemon trainer party mutation.
 */
public class TrainerAdditionalPokemonTest {

    @Test
    public void additionalPokemonApplyToBossImportantAndRegularTrainers() {
        Species seed = species(1, "Seed");
        Trainer boss = trainer("GYM1-LEADER", pokemon(seed, 10), pokemon(seed, 12), pokemon(seed, 14),
                pokemon(seed, 16), pokemon(seed, 20));
        Trainer important = trainer("RIVAL2-1", pokemon(seed, 8), pokemon(seed, 10), pokemon(seed, 12),
                pokemon(seed, 16));
        Trainer regular = trainer(null, pokemon(seed, 6), pokemon(seed, 9));
        TrainerRomHandler handler = TrainerRomHandler.create(List.of(seed), List.of(boss, important, regular));
        Settings settings = new Settings();
        settings.setAdditionalBossTrainerPokemon(2);
        settings.setAdditionalImportantTrainerPokemon(2);
        settings.setAdditionalRegularTrainerPokemon(3);

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, settings, new Random(7));
        randomizer.addTrainerPokemon();

        assertTrue(randomizer.isChangesMade());
        assertTeamState(boss, 6, 1);
        assertTeamState(important, 6, 2);
        assertTeamState(regular, 5, 3);
    }

    @Test
    public void additionalPokemonRespectMultiBattlePartyLimit() {
        Species seed = species(1, "Seed");
        Trainer multiBattleTrainer = trainer(null, pokemon(seed, 12), pokemon(seed, 16));
        multiBattleTrainer.setMultiBattleStatus(Trainer.MultiBattleStatus.POTENTIAL);
        TrainerRomHandler handler = TrainerRomHandler.create(List.of(seed), List.of(multiBattleTrainer));
        Settings settings = new Settings();
        settings.setAdditionalRegularTrainerPokemon(4);

        new TrainerPokemonRandomizer(handler.proxy, settings, new Random(3)).addTrainerPokemon();

        assertTeamState(multiBattleTrainer, 3, 1);
    }

    @Test
    public void additionalPokemonDoNotCloneNullSpeciesSlots() {
        Species seed = species(1, "Seed");
        Trainer trainer = trainer(null, pokemon(null, 5), pokemon(seed, 10));
        Trainer allUnsafe = trainer(null, pokemon(null, 8));
        TrainerRomHandler handler = TrainerRomHandler.create(List.of(seed), List.of(trainer, allUnsafe));
        Settings settings = new Settings();
        settings.setAdditionalRegularTrainerPokemon(3);

        new TrainerPokemonRandomizer(handler.proxy, settings, new Random(11)).addTrainerPokemon();

        assertEquals(5, trainer.getPokemon().size());
        trainer.getPokemon().stream()
                .filter(TrainerPokemon::isAddedTeamMember)
                .forEach(tp -> assertNotNull(tp.getSpecies()));
        assertEquals(1, allUnsafe.getPokemon().size());
        assertFalse(allUnsafe.getPokemon().get(0).isAddedTeamMember());
    }

    private static void assertTeamState(Trainer trainer, int expectedSize, int expectedAdded) {
        assertEquals(expectedSize, trainer.getPokemon().size());
        assertEquals(expectedAdded, trainer.getPokemon().stream().filter(TrainerPokemon::isAddedTeamMember).count());
        trainer.getPokemon().stream()
                .filter(TrainerPokemon::isAddedTeamMember)
                .forEach(tp -> assertNotNull(tp.getSpecies()));
    }

    private static Trainer trainer(String tag, TrainerPokemon... pokemon) {
        Trainer trainer = new Trainer();
        trainer.setTag(tag);
        trainer.setPokemon(new ArrayList<>(Arrays.asList(pokemon)));
        return trainer;
    }

    private static TrainerPokemon pokemon(Species species, int level) {
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setSpecies(species);
        trainerPokemon.setLevel(level);
        return trainerPokemon;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        species.setPrimaryType(Type.NORMAL);
        species.setAbility1(1);
        species.setHp(50);
        species.setAttack(50);
        species.setDefense(50);
        species.setSpatk(50);
        species.setSpdef(50);
        species.setSpeed(50);
        return species;
    }

    private static SpeciesSet speciesSet(List<Species> species) {
        SpeciesSet speciesSet = new SpeciesSet();
        speciesSet.addAll(species);
        return speciesSet;
    }

    private static class TrainerRomHandler implements InvocationHandler {
        private final List<Species> species;
        private final SpeciesSet speciesSet;
        private final List<Trainer> trainers;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;

        private TrainerRomHandler(List<Species> species, List<Trainer> trainers) {
            this.species = species;
            this.speciesSet = speciesSet(species);
            this.trainers = trainers;
        }

        private static TrainerRomHandler create(List<Species> species, List<Trainer> trainers) {
            TrainerRomHandler handler = new TrainerRomHandler(species, trainers);
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
                case "getAltFormes", "getIrregularFormes" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "getTrainers" -> trainers;
                case "toString" -> "TrainerAdditionalPokemonTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
