package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerClassSpriteSyncRandomizerTest {

    @Test
    void trainerNameRandomizationAloneDoesNotChangeClassIdOrPic() {
        Trainer trainer = trainer(1, 7, "BOB", null);
        TestHandler handler = new TestHandler(List.of(trainer), true, List.of("YOUNGSTER", "LASS"));
        TrainerNameRandomizer nameRandomizer = new TrainerNameRandomizer(
                handler.proxy, settingsWithNames(), new Random(1));

        nameRandomizer.randomizeTrainerNames();
        new TrainerClassSpriteSyncRandomizer(handler.proxy, new Settings(), new Random(1), nameRandomizer)
                .randomizeTrainerClassSprites();

        assertEquals(1, trainer.getTrainerclass());
        assertEquals(7, trainer.getTrainerPic());
        assertFalse(handler.syncEnabled);
    }

    @Test
    void classNameRandomizationWithSpriteSyncChangesClassIdAndPicConsistently() {
        Trainer youngster = trainer(0, 7, "A", null);
        Trainer lass = trainer(1, 8, "B", null);
        TestHandler handler = new TestHandler(List.of(youngster, lass), true, List.of("YOUNGSTER", "LASS"));
        TrainerNameRandomizer nameRandomizer = new TrainerNameRandomizer(
                handler.proxy, settingsWithNames(), new Random(1));

        nameRandomizer.randomizeTrainerClassNames();
        new TrainerClassSpriteSyncRandomizer(handler.proxy, new Settings(), new Random(1), nameRandomizer)
                .randomizeTrainerClassSprites();

        assertTrue(handler.syncEnabled);
        assertEquals(List.of("YOUNGSTER", "LASS"), handler.writtenTrainerClassNames);
        assertEquals(1, youngster.getTrainerclass());
        assertEquals(8, youngster.getTrainerPic());
        assertEquals("LASS A", youngster.getFullDisplayName());
        assertEquals(0, lass.getTrainerclass());
        assertEquals(7, lass.getTrainerPic());
        assertEquals("YOUNGSTER B", lass.getFullDisplayName());
    }

    @Test
    void classNameRandomizationWithSpriteSyncCanUseChaoticEliteFourTargetClassAndPic() {
        Trainer youngster = trainer(0, 7, "A", null);
        Trainer elite = trainer(1, 9, "B", "ELITE1");
        TestHandler handler = new TestHandler(List.of(youngster, elite), true, List.of("YOUNGSTER", "ELITE 4"));
        TrainerNameRandomizer nameRandomizer = new TrainerNameRandomizer(
                handler.proxy, settingsWithNames(), new Random(1));

        nameRandomizer.randomizeTrainerClassNames();
        new TrainerClassSpriteSyncRandomizer(handler.proxy, new Settings(), new Random(1), nameRandomizer)
                .randomizeTrainerClassSprites();

        assertTrue(handler.syncEnabled);
        // Special-looking targets are valid here: Sprite Sync preserves visual
        // consistency, not lore plausibility.
        assertEquals(1, youngster.getTrainerclass());
        assertEquals(9, youngster.getTrainerPic());
        assertEquals("ELITE 4 A", youngster.getFullDisplayName());
    }

    @Test
    void classNameRandomizationWithoutSpriteSyncRemainsTextLabelOnly() {
        Trainer youngster = trainer(0, 7, "A", null);
        Trainer lass = trainer(1, 8, "B", null);
        TestHandler handler = new TestHandler(List.of(youngster, lass), true, List.of("YOUNGSTER", "LASS"));

        new TrainerNameRandomizer(handler.proxy, settingsWithNames(), new Random(1)).randomizeTrainerClassNames();

        assertTrue(handler.setTrainerClassNamesCalled);
        assertEquals(List.of("LASS", "YOUNGSTER"), handler.writtenTrainerClassNames);
        assertEquals(0, youngster.getTrainerclass());
        assertEquals(7, youngster.getTrainerPic());
        assertEquals(1, lass.getTrainerclass());
        assertEquals(8, lass.getTrainerPic());
        assertFalse(handler.syncEnabled);
    }

    @Test
    void spriteSyncSkipsTargetClassWithoutObservedPic() {
        Trainer youngster = trainer(0, 7, "A", null);
        TestHandler handler = new TestHandler(List.of(youngster), true, List.of("YOUNGSTER", "ELITE 4"));
        TrainerNameRandomizer nameRandomizer = new TrainerNameRandomizer(
                handler.proxy, settingsWithNames(), new Random(1));

        nameRandomizer.randomizeTrainerClassNames();
        new TrainerClassSpriteSyncRandomizer(handler.proxy, new Settings(), new Random(1), nameRandomizer)
                .randomizeTrainerClassSprites();

        assertEquals(0, youngster.getTrainerclass());
        assertEquals(7, youngster.getTrainerPic());
        assertFalse(handler.syncEnabled);
    }

    @Test
    void unsupportedHandlerDoesNothing() {
        Trainer first = trainer(0, 7, "A", null);
        Trainer second = trainer(1, 8, "B", null);
        TestHandler handler = new TestHandler(List.of(first, second), false, List.of("YOUNGSTER", "LASS"));
        TrainerNameRandomizer nameRandomizer = new TrainerNameRandomizer(
                handler.proxy, settingsWithNames(), new Random(1));

        nameRandomizer.randomizeTrainerClassNames();
        TrainerClassSpriteSyncRandomizer randomizer = new TrainerClassSpriteSyncRandomizer(
                handler.proxy, new Settings(), new Random(1), nameRandomizer);
        randomizer.randomizeTrainerClassSprites();

        assertFalse(randomizer.isChangesMade());
        assertFalse(handler.syncEnabled);
        assertEquals(0, first.getTrainerclass());
        assertEquals(7, first.getTrainerPic());
        assertEquals(1, second.getTrainerclass());
        assertEquals(8, second.getTrainerPic());
    }

    private static Settings settingsWithNames() {
        Settings settings = new Settings();
        settings.setCustomNames(new CustomNamesSet(
                List.of("ANA"),
                List.of("YOUNGSTER", "LASS", "ELITE 4"),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()));
        return settings;
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
        private final List<String> trainerClassNames;
        private boolean syncEnabled;
        private boolean setTrainerClassNamesCalled;
        private List<String> writtenTrainerClassNames = new ArrayList<>();
        private final RomHandler proxy = (RomHandler) Proxy.newProxyInstance(
                RomHandler.class.getClassLoader(), new Class<?>[]{RomHandler.class}, this);

        TestHandler(List<Trainer> trainers, boolean supportsSync, List<String> trainerClassNames) {
            this.trainers = trainers;
            this.supportsSync = supportsSync;
            this.trainerClassNames = trainerClassNames;
            this.writtenTrainerClassNames = new ArrayList<>(trainerClassNames);
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            return switch (method.getName()) {
                case "supportsTrainerClassSpriteSync" -> supportsSync;
                case "setTrainerClassSpriteSyncEnabled" -> {
                    syncEnabled = (boolean) args[0];
                    yield null;
                }
                case "canChangeTrainerText" -> true;
                case "getTrainers" -> trainers;
                case "getTrainerNames" -> trainers.stream().map(Trainer::getName).toList();
                case "setTrainerNames" -> null;
                case "trainerNameMode" -> RomHandler.TrainerNameMode.MAX_LENGTH;
                case "maxTrainerNameLength", "maxSumOfTrainerNameLengths", "maxTrainerClassNameLength" -> 10;
                case "getTCNameLengthsByTrainer" -> Collections.nCopies(trainers.size(), 0);
                case "getTrainerClassNames" -> trainerClassNames;
                case "setTrainerClassNames" -> {
                    setTrainerClassNamesCalled = true;
                    writtenTrainerClassNames = new ArrayList<>(asStringList(args[0]));
                    yield null;
                }
                case "fixedTrainerClassNamesLength" -> false;
                case "getDoublesTrainerClasses" -> Collections.emptyList();
                case "internalStringLength" -> ((String) args[0]).length();
                case "getRestrictedSpeciesService" -> new RestrictedSpeciesService(this.proxy);
                case "getTypeService" -> new TypeService(this.proxy);
                case "getSpeciesSetInclFormes" -> new SpeciesSet();
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private static List<String> asStringList(Object value) {
            List<?> raw = (List<?>) value;
            List<String> result = new ArrayList<>();
            for (Object entry : raw) {
                result.add((String) entry);
            }
            return result;
        }
    }
}
