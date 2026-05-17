package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Gen3TrainerTextDisplayNameSyncTest {

    @Test
    void setTrainerNamesRefreshesLoadedFullDisplayNamesForLogger() {
        TestableGen3RomHandler romHandler = new TestableGen3RomHandler();
        Trainer trainer = trainer(1, "Quinn", "BURGLAR Quinn");
        romHandler.addTrainer(trainer);

        romHandler.setTrainerNames(List.of("Alex"));

        assertEquals("Alex", trainer.getName());
        assertEquals("BURGLAR Alex", trainer.getFullDisplayName());
    }

    @Test
    void classNameRefreshUpdatesLoadedFullDisplayNamesForLogger() {
        TestableGen3RomHandler romHandler = new TestableGen3RomHandler();
        Trainer trainer = trainer(1, "Quinn", "BURGLAR Quinn");
        romHandler.addTrainer(trainer);

        romHandler.refreshTrainerFullDisplayNames(List.of("UNUSED", "ENGINEER"));

        assertEquals("Quinn", trainer.getName());
        assertEquals("ENGINEER Quinn", trainer.getFullDisplayName());
    }

    @Test
    void gen3TrainerClassNamesUseMaxLengthNotFixedLength() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("TrainerClassNameLength", 13);
        setField(romHandler, "romEntry", romEntry);

        assertFalse(romHandler.fixedTrainerClassNamesLength());
        assertEquals(12, romHandler.maxTrainerClassNameLength());
    }

    private static Trainer trainer(int trainerClass, String name, String fullDisplayName) {
        Trainer trainer = new Trainer();
        trainer.setTrainerclass(trainerClass);
        trainer.setName(name);
        trainer.setFullDisplayName(fullDisplayName);
        return trainer;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static class TestableGen3RomHandler extends Gen3RomHandler {
        @Override
        public List<String> getTrainerClassNames() {
            return List.of("UNUSED", "BURGLAR");
        }

        void addTrainer(Trainer trainer) {
            trainers.add(trainer);
        }
    }
}
