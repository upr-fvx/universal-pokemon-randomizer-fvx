package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for FVX-FOE-009 Force Diverse Types / Type Themes guard behavior.
 */
public class TrainerTypeDiversityGuardTest {

    @Test
    public void usedTypeBookkeepingIgnoresNullTypesAndKeepsKnownTypes() {
        Set<Type> usedTypes = EnumSet.noneOf(Type.class);

        assertDoesNotThrow(() -> updateUsedTypes(true, null, usedTypes, species(1, "MissingPrimary", null, Type.WATER)));
        assertTrue(usedTypes.isEmpty());

        assertDoesNotThrow(() -> updateUsedTypes(true, null, usedTypes, species(2, "FireOnly", Type.FIRE, null)));
        assertEquals(EnumSet.of(Type.FIRE), usedTypes);

        assertDoesNotThrow(() -> updateUsedTypes(true, null, usedTypes, species(3, "WaterGrass", Type.WATER, Type.GRASS)));
        assertEquals(EnumSet.of(Type.FIRE, Type.WATER, Type.GRASS), usedTypes);
    }

    @Test
    public void regularTrainerTypeDiversityRunsWithSyntheticNullTypeData() {
        Species missingPrimary = species(1, "MissingPrimary", null, Type.WATER);
        Species missingSecondary = species(2, "MissingSecondary", Type.NORMAL, null);
        Species fire = species(3, "Fire", Type.FIRE, null);
        Species water = species(4, "Water", Type.WATER, null);
        Species grass = species(5, "Grass", Type.GRASS, null);
        Trainer trainer = trainer(
                trainerPokemon(missingPrimary, 5),
                trainerPokemon(missingSecondary, 6));
        TrainerTestRomHandler romHandler = TrainerTestRomHandler.create(
                List.of(missingPrimary, missingSecondary, fire, water, grass),
                List.of(trainer));
        Settings settings = new Settings();
        settings.setTrainersMod(Settings.TrainersMod.RANDOM);
        settings.setDiverseTypesForRegularTrainers(true);
        settings.setTrainersBlockEarlyWonderGuard(false);

        TrainerPokemonRandomizer randomizer =
                new TrainerPokemonRandomizer(romHandler.proxy, settings, new Random(1));

        assertDoesNotThrow(randomizer::randomizeTrainerPokes);

        assertTrue(randomizer.isChangesMade());
        List<TrainerPokemon> randomizedPokemon = trainer.getPokemon();
        Type firstPrimaryType = randomizedPokemon.get(0).getSpecies().getPrimaryType(false);
        Type secondPrimaryType = randomizedPokemon.get(1).getSpecies().getPrimaryType(false);
        assertNotEquals(null, firstPrimaryType);
        assertNotEquals(null, secondPrimaryType);
        assertNotEquals(firstPrimaryType, secondPrimaryType);
    }

    private static void updateUsedTypes(boolean forceTypeDiverse, Type typeForTrainer, Set<Type> usedTypes,
                                        Species species) throws ReflectiveOperationException {
        Method updateUsedTypes = TrainerPokemonRandomizer.class.getDeclaredMethod(
                "updateUsedTypes", boolean.class, Type.class, Set.class, Species.class);
        updateUsedTypes.setAccessible(true);
        updateUsedTypes.invoke(null, forceTypeDiverse, typeForTrainer, usedTypes, species);
    }

    private static Trainer trainer(TrainerPokemon... pokemon) {
        Trainer trainer = new Trainer();
        trainer.setIndex(1);
        trainer.setPokemon(List.of(pokemon));
        return trainer;
    }

    private static TrainerPokemon trainerPokemon(Species species, int level) {
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setSpecies(species);
        trainerPokemon.setLevel(level);
        return trainerPokemon;
    }

    private static Species species(int number, String name, Type primaryType, Type secondaryType) {
        Species species = new Species(number);
        species.setName(name);
        species.setPrimaryType(primaryType);
        species.setSecondaryType(secondaryType);
        species.setAbility1(1);
        setBst(species, 300 + number);
        return species;
    }

    private static void setBst(Species species, int bst) {
        int stat = bst / 6;
        species.setHp(stat);
        species.setAttack(stat);
        species.setDefense(stat);
        species.setSpatk(stat);
        species.setSpdef(stat);
        species.setSpeed(bst - stat * 5);
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
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;

        private TrainerTestRomHandler(List<Species> species, List<Trainer> trainers) {
            this.species = species;
            this.speciesSet = speciesSet(species);
            this.trainers = trainers;
        }

        private static TrainerTestRomHandler create(List<Species> species, List<Trainer> trainers) {
            TrainerTestRomHandler handler = new TrainerTestRomHandler(species, trainers);
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
                case "getAltFormes", "getIrregularFormes", "getBannedFormesForTrainerPokemon" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "getTypeTable" -> new TypeTable(List.of(Type.NORMAL, Type.FIRE, Type.WATER, Type.GRASS));
                case "getTrainers" -> trainers;
                case "getMainPlaythroughTrainers", "getEliteFourTrainers" -> Collections.<Integer>emptyList();
                case "getGymAndEliteTypeThemes" -> Collections.<String, Type>emptyMap();
                case "getAltFormeOfSpecies" -> args[0];
                case "abilitiesPerSpecies" -> 0;
                case "isYellow", "isORAS" -> false;
                case "generationOfPokemon" -> 3;
                case "toString" -> "TrainerTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
