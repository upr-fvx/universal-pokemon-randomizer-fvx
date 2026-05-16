package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM coverage for FVX-FOE-011 Trainer Battle Style decisions.
 */
public class TrainerBattleStyleTest {

    @Test
    public void unchangedBattleStyleDoesNotTouchTrainers() {
        Trainer trainer = trainer(1);
        BattleStyle before = trainer.getCurrBattleStyle();
        TrainerBattleStyleRomHandler handler = TrainerBattleStyleRomHandler.create(3, List.of(trainer));

        new TrainerPokemonRandomizer(handler.proxy, new Settings(), new Random(1)).modifyBattleStyle();

        assertEquals(before.getModification(), trainer.getCurrBattleStyle().getModification());
        assertEquals(before.getStyle(), trainer.getCurrBattleStyle().getStyle());
        assertFalse(trainer.isForcedDoubleBattle());
        assertFalse(handler.makeDoubleBattleModePossibleCalled);
    }

    @Test
    public void singleStyleSetsChosenStyleWhenTrainerHasEnoughPokemon() {
        Trainer trainer = trainer(1, 2);
        TrainerBattleStyleRomHandler handler = TrainerBattleStyleRomHandler.create(3, List.of(trainer));
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.SINGLE_STYLE, BattleStyle.Style.DOUBLE_BATTLE));

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, settings, new Random(2));
        randomizer.modifyBattleStyle();

        assertEquals(BattleStyle.Modification.SINGLE_STYLE, trainer.getCurrBattleStyle().getModification());
        assertEquals(BattleStyle.Style.DOUBLE_BATTLE, trainer.getCurrBattleStyle().getStyle());
        assertEquals(2, trainer.getPokemon().size());
        assertTrue(trainer.isForcedDoubleBattle());
        assertTrue(handler.makeDoubleBattleModePossibleCalled);
        assertTrue(randomizer.isChangesMade());
    }

    @Test
    public void singleStyleSkipsTrainerWithTooFewPokemon() {
        Trainer trainer = trainer(1);
        TrainerBattleStyleRomHandler handler = TrainerBattleStyleRomHandler.create(3, List.of(trainer));
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.SINGLE_STYLE, BattleStyle.Style.DOUBLE_BATTLE));

        TrainerPokemonRandomizer randomizer = new TrainerPokemonRandomizer(handler.proxy, settings, new Random(3));
        randomizer.modifyBattleStyle();

        assertEquals(BattleStyle.Modification.UNCHANGED, trainer.getCurrBattleStyle().getModification());
        assertEquals(1, trainer.getPokemon().size());
        assertFalse(trainer.isForcedDoubleBattle());
        assertFalse(handler.makeDoubleBattleModePossibleCalled);
        assertFalse(randomizer.isChangesMade());
    }

    @Test
    public void randomStyleSetsDeterministicValidStylesForSeed() {
        List<Trainer> firstRunTrainers = List.of(trainer(1), trainer(2, 2), trainer(3, 3));
        List<Trainer> secondRunTrainers = List.of(trainer(1), trainer(2, 2), trainer(3, 3));
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.RANDOM, BattleStyle.Style.SINGLE_BATTLE));

        new TrainerPokemonRandomizer(
                TrainerBattleStyleRomHandler.create(5, firstRunTrainers).proxy, settings, new Random(42))
                .modifyBattleStyle();
        new TrainerPokemonRandomizer(
                TrainerBattleStyleRomHandler.create(5, secondRunTrainers).proxy, settings, new Random(42))
                .modifyBattleStyle();

        for (int i = 0; i < firstRunTrainers.size(); i++) {
            Trainer first = firstRunTrainers.get(i);
            Trainer second = secondRunTrainers.get(i);
            assertEquals(first.getCurrBattleStyle().getStyle(), second.getCurrBattleStyle().getStyle());
            assertTrue(first.getPokemon().size() >= first.getCurrBattleStyle().getRequiredPokemonCount());
            assertEquals(BattleStyle.Modification.RANDOM, first.getCurrBattleStyle().getModification());
        }
        assertEquals(BattleStyle.Style.SINGLE_BATTLE, firstRunTrainers.get(0).getCurrBattleStyle().getStyle());
    }

    private static Trainer trainer(int index, int pokemonCount) {
        Trainer trainer = new Trainer();
        trainer.setIndex(index);
        List<TrainerPokemon> pokemon = new ArrayList<>();
        for (int i = 0; i < pokemonCount; i++) {
            pokemon.add(new TrainerPokemon());
        }
        trainer.setPokemon(pokemon);
        return trainer;
    }

    private static Trainer trainer(int index) {
        return trainer(index, 1);
    }

    private static class TrainerBattleStyleRomHandler implements InvocationHandler {
        private final int generation;
        private final List<Trainer> trainers;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private boolean makeDoubleBattleModePossibleCalled;

        private TrainerBattleStyleRomHandler(int generation, List<Trainer> trainers) {
            this.generation = generation;
            this.trainers = trainers;
        }

        private static TrainerBattleStyleRomHandler create(int generation, List<Trainer> trainers) {
            TrainerBattleStyleRomHandler handler = new TrainerBattleStyleRomHandler(generation, trainers);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] { RomHandler.class }, handler);
            handler.restrictedSpeciesService = new RestrictedSpeciesService(handler.proxy);
            handler.typeService = new TypeService(handler.proxy);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService" -> restrictedSpeciesService;
                case "getTypeService" -> typeService;
                case "getSpeciesSetInclFormes" -> new SpeciesSet();
                case "generationOfPokemon" -> generation;
                case "getTrainers" -> trainers;
                case "makeDoubleBattleModePossible" -> {
                    makeDoubleBattleModePossibleCalled = true;
                    yield null;
                }
                case "toString" -> "TrainerBattleStyleTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
