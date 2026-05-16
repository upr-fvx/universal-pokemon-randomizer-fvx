package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM decision coverage only for FVX-FOE-013 Trainer Names/Class Names.
 * These tests do not prove Gen3 writer, reload, or text-encoding safety.
 */
class TrainerNameRandomizerDecisions {

    @Test
    void trainerNameRandomizerNoOpWhenTrainerTextCannotChange() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.canChangeTrainerText = false;
        handler.trainerNames = List.of("ALICE");

        TrainerNameRandomizer randomizer = new TrainerNameRandomizer(handler.proxy, settings(), new Random(1));
        randomizer.randomizeTrainerNames();
        randomizer.randomizeTrainerClassNames();

        assertFalse(handler.setTrainerNamesCalled);
        assertFalse(handler.setTrainerClassNamesCalled);
        assertFalse(randomizer.isChangesMade());
    }

    @Test
    void trainerNameRandomizerUsesSinglePoolForNormalNames() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE", "BOB");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(2)).randomizeTrainerNames();

        assertTrue(handler.setTrainerNamesCalled);
        assertEquals(2, handler.writtenTrainerNames.size());
        assertTrue(handler.trainerNamePool.containsAll(handler.writtenTrainerNames));
    }

    @Test
    void trainerNameRandomizerUsesDoublesPoolForNamesWithAmpersand() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("AL & BOB", "CAL & DEE");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(3)).randomizeTrainerNames();

        assertTrue(handler.setTrainerNamesCalled);
        assertEquals(2, handler.writtenTrainerNames.size());
        assertTrue(handler.doublesTrainerNamePool.containsAll(handler.writtenTrainerNames));
    }

    @Test
    void trainerNameRandomizerRepeatedNonSpecialNamesKeepSameTranslation() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE", "ALICE", "ALICE");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(4)).randomizeTrainerNames();

        assertEquals(handler.writtenTrainerNames.get(0), handler.writtenTrainerNames.get(1));
        assertEquals(handler.writtenTrainerNames.get(1), handler.writtenTrainerNames.get(2));
    }

    @Test
    void trainerNameRandomizerRepeatedSpecialNamesMayBeTranslatedSeparately() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("GRUNT", "GRUNT");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(0)).randomizeTrainerNames();

        assertEquals(2, handler.writtenTrainerNames.size());
        assertNotEquals(handler.writtenTrainerNames.get(0), handler.writtenTrainerNames.get(1));
    }

    @Test
    void trainerNameRandomizerRespectsMaxLength() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE", "BOB", "CARL");
        handler.trainerNamePool = List.of("TOOLONG", "ANA", "BEX");
        handler.maxTrainerNameLength = 3;

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(5)).randomizeTrainerNames();

        assertTrue(handler.writtenTrainerNames.stream().allMatch(name -> name.length() <= 3));
    }

    @Test
    void trainerNameRandomizerRespectsMaxLengthWithClass() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNameMode = RomHandler.TrainerNameMode.MAX_LENGTH_WITH_CLASS;
        handler.trainerNames = List.of("ALICE", "BOB");
        handler.trainerNamePool = List.of("TOOLONG", "ANA", "BEX");
        handler.maxTrainerNameLength = 8;
        handler.tcNameLengthsByTrainer = List.of(5, 5);

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(6)).randomizeTrainerNames();

        assertTrue(handler.writtenTrainerNames.stream().allMatch(name -> name.length() + 5 <= 8));
    }

    @Test
    void trainerNameRandomizerClassNamesUseSingleAndDoublesPools() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("LAD", "DUO", "LASS");
        handler.doublesTrainerClasses = List.of(1);

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(7)).randomizeTrainerClassNames();

        assertTrue(handler.setTrainerClassNamesCalled);
        assertTrue(handler.trainerClassPool.contains(handler.writtenTrainerClassNames.get(0)));
        assertTrue(handler.doublesTrainerClassPool.contains(handler.writtenTrainerClassNames.get(1)));
        assertTrue(handler.trainerClassPool.contains(handler.writtenTrainerClassNames.get(2)));
    }

    @Test
    void trainerNameRandomizerFixedClassNamesLengthUsesSameInternalLength() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BOY", "GIRL");
        handler.trainerClassPool = List.of("AA", "DUO", "TEAM");
        handler.fixedTrainerClassNamesLength = true;

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(8)).randomizeTrainerClassNames();

        assertEquals(3, handler.writtenTrainerClassNames.get(0).length());
        assertEquals(4, handler.writtenTrainerClassNames.get(1).length());
    }

    private static Settings settings() {
        Settings settings = new Settings();
        settings.setCustomNames(new CustomNamesSet(
                List.of("ANA", "BEX", "CODY", "DORA", "ELI"),
                List.of("BOY", "GIRL", "DUO", "TEAM"),
                List.of("ANA & BEX", "CODY & DORA"),
                List.of("PAIR", "TEAM"),
                Collections.emptyList()));
        return settings;
    }

    private static class TrainerNameTestRomHandler implements InvocationHandler {
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private boolean canChangeTrainerText = true;
        private List<String> trainerNames = List.of("ALICE");
        private List<String> trainerNamePool = List.of("ANA", "BEX", "CODY", "DORA", "ELI");
        private List<String> doublesTrainerNamePool = List.of("ANA & BEX", "CODY & DORA");
        private List<String> trainerClassNames = List.of("LAD");
        private List<String> trainerClassPool = List.of("BOY", "GIRL", "DUO", "TEAM");
        private List<String> doublesTrainerClassPool = List.of("PAIR", "TEAM");
        private List<Integer> doublesTrainerClasses = Collections.emptyList();
        private List<Integer> tcNameLengthsByTrainer = Collections.emptyList();
        private RomHandler.TrainerNameMode trainerNameMode = RomHandler.TrainerNameMode.MAX_LENGTH;
        private int maxTrainerNameLength = 10;
        private int maxSumOfTrainerNameLengths = Integer.MAX_VALUE;
        private boolean fixedTrainerClassNamesLength;
        private int maxTrainerClassNameLength = 10;
        private boolean setTrainerNamesCalled;
        private boolean setTrainerClassNamesCalled;
        private List<String> writtenTrainerNames = new ArrayList<>();
        private List<String> writtenTrainerClassNames = new ArrayList<>();

        private static TrainerNameTestRomHandler create() {
            TrainerNameTestRomHandler handler = new TrainerNameTestRomHandler();
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
                case "canChangeTrainerText" -> canChangeTrainerText;
                case "getTrainerNames" -> trainerNames;
                case "setTrainerNames" -> {
                    setTrainerNamesCalled = true;
                    writtenTrainerNames = new ArrayList<>(asStringList(args[0]));
                    yield null;
                }
                case "trainerNameMode" -> trainerNameMode;
                case "maxTrainerNameLength" -> maxTrainerNameLength;
                case "maxSumOfTrainerNameLengths" -> maxSumOfTrainerNameLengths;
                case "getTCNameLengthsByTrainer" -> tcNameLengthsByTrainer;
                case "getTrainerClassNames" -> trainerClassNames;
                case "setTrainerClassNames" -> {
                    setTrainerClassNamesCalled = true;
                    writtenTrainerClassNames = new ArrayList<>(asStringList(args[0]));
                    yield null;
                }
                case "fixedTrainerClassNamesLength" -> fixedTrainerClassNamesLength;
                case "maxTrainerClassNameLength" -> maxTrainerClassNameLength;
                case "getDoublesTrainerClasses" -> doublesTrainerClasses;
                case "internalStringLength" -> ((String) args[0]).length();
                case "toString" -> "TrainerNameRandomizerTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        @SuppressWarnings("unchecked")
        private static List<String> asStringList(Object value) {
            return (List<String>) value;
        }
    }
}
