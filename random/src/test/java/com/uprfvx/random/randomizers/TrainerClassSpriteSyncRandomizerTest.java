package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerClassSpriteSyncRandomizerTest {

    @Test
    void regularTrainersAreRemappedToExistingClassSpritePairs() {
        Trainer first = trainer(1, 7, "A", null);
        Trainer second = trainer(2, 8, "B", null);
        Trainer third = trainer(3, 9, "C", null);
        TestHandler handler = new TestHandler(List.of(first, second, third), true);

        TrainerClassSpriteSyncRandomizer randomizer = new TrainerClassSpriteSyncRandomizer(
                handler.proxy, new Settings(), new Random(3));
        randomizer.randomizeTrainerClassSprites();

        assertTrue(randomizer.isChangesMade());
        assertTrue(handler.syncEnabled);
        for (Trainer trainer : handler.trainers) {
            assertTrue(List.of("CLASS1", "CLASS2", "CLASS3").stream()
                    .anyMatch(className -> trainer.getFullDisplayName().startsWith(className + " ")));
            assertTrue(isKnownPair(trainer.getTrainerclass(), trainer.getTrainerPic()));
        }
    }

    @Test
    void specialAndRuntimeTrainersAreExcluded() {
        Trainer regularA = trainer(1, 7, "A", null);
        Trainer regularB = trainer(2, 8, "B", null);
        Trainer rival = trainer(3, 9, "R", "RIVAL2-0");
        Trainer leader = trainer(4, 10, "L", "GYM1-LEADER");
        Trainer runtime = trainer(5, 11, "X", Trainer.RUNTIME_SOURCE_TAG);
        TestHandler handler = new TestHandler(List.of(regularA, regularB, rival, leader, runtime), true);

        new TrainerClassSpriteSyncRandomizer(handler.proxy, new Settings(), new Random(4))
                .randomizeTrainerClassSprites();

        assertEquals(3, rival.getTrainerclass());
        assertEquals(9, rival.getTrainerPic());
        assertEquals(4, leader.getTrainerclass());
        assertEquals(10, leader.getTrainerPic());
        assertEquals(5, runtime.getTrainerclass());
        assertEquals(11, runtime.getTrainerPic());
    }

    @Test
    void unsupportedHandlerDoesNothing() {
        Trainer first = trainer(1, 7, "A", null);
        Trainer second = trainer(2, 8, "B", null);
        TestHandler handler = new TestHandler(List.of(first, second), false);

        TrainerClassSpriteSyncRandomizer randomizer = new TrainerClassSpriteSyncRandomizer(
                handler.proxy, new Settings(), new Random(3));
        randomizer.randomizeTrainerClassSprites();

        assertFalse(randomizer.isChangesMade());
        assertFalse(handler.syncEnabled);
        assertEquals(1, first.getTrainerclass());
        assertEquals(7, first.getTrainerPic());
        assertEquals(2, second.getTrainerclass());
        assertEquals(8, second.getTrainerPic());
    }

    private static boolean isKnownPair(int trainerClass, int trainerPic) {
        return (trainerClass == 1 && trainerPic == 7)
                || (trainerClass == 2 && trainerPic == 8)
                || (trainerClass == 3 && trainerPic == 9);
    }

    private static Trainer trainer(int trainerClass, int trainerPic, String name, String tag) {
        Trainer trainer = new Trainer();
        trainer.setIndex(trainerClass);
        trainer.setTrainerclass(trainerClass);
        trainer.setTrainerPic(trainerPic);
        trainer.setName(name);
        trainer.setFullDisplayName("CLASS" + trainerClass + " " + name);
        trainer.setTag(tag);
        return trainer;
    }

    private static class TestHandler implements InvocationHandler {
        private final List<Trainer> trainers;
        private final boolean supportsSync;
        private boolean syncEnabled;
        private final RomHandler proxy = (RomHandler) Proxy.newProxyInstance(
                RomHandler.class.getClassLoader(), new Class<?>[]{RomHandler.class}, this);

        TestHandler(List<Trainer> trainers, boolean supportsSync) {
            this.trainers = trainers;
            this.supportsSync = supportsSync;
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            return switch (method.getName()) {
                case "supportsTrainerClassSpriteSync" -> supportsSync;
                case "setTrainerClassSpriteSyncEnabled" -> {
                    syncEnabled = (boolean) args[0];
                    yield null;
                }
                case "getTrainers" -> trainers;
                case "getTrainerClassNames" -> List.of("UNUSED", "CLASS1", "CLASS2", "CLASS3", "CLASS4", "CLASS5");
                case "getRestrictedSpeciesService" -> new RestrictedSpeciesService(this.proxy);
                case "getTypeService" -> new TypeService(this.proxy);
                case "getSpeciesSetInclFormes" -> new SpeciesSet();
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
