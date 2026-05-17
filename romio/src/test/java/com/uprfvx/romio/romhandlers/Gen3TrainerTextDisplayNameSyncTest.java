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
        Trainer trainer = trainer(3, "Quinn", "FISHERMAN Quinn");
        romHandler.addTrainer(trainer);

        romHandler.setTrainerNames(List.of("Alex"));

        assertEquals("Alex", trainer.getName());
        assertEquals("FISHERMAN Alex", trainer.getFullDisplayName());
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
    void loadTrainersStoresTrainerClassIdNotGenderBitForDisplayRefresh() throws Exception {
        TestableGen3RomHandler romHandler = new TestableGen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("TrainerData", 0);
        romEntry.putIntValue("TrainerCount", 2);
        romEntry.putIntValue("TrainerEntrySize", 40);
        setField(romHandler, "romEntry", romEntry);

        byte[] rom = new byte[128];
        int trainerOffset = 40;
        rom[trainerOffset + 1] = 3;
        rom[trainerOffset + 2] = (byte) 0x80;
        rom[trainerOffset + 4] = (byte) 0xFF;
        rom[trainerOffset + 36] = 100;
        rom[trainerOffset + 39] = 8;
        setField(romHandler, "rom", rom);
        setField(romHandler, "tb", new String[256]);

        romHandler.loadTrainers();

        Trainer trainer = romHandler.loadedTrainers().get(0);
        assertEquals(3, trainer.getTrainerclass());
        assertEquals("FISHERMAN ", trainer.getFullDisplayName());

        romHandler.refreshTrainerFullDisplayNames(List.of("UNUSED", "[PK][MN] BREEDER", "ENGINEER", "BURGLAR"));

        assertEquals("BURGLAR ", trainer.getFullDisplayName());
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
            return List.of("UNUSED", "[PK][MN] BREEDER", "ENGINEER", "FISHERMAN");
        }

        void addTrainer(Trainer trainer) {
            trainers.add(trainer);
        }

        List<Trainer> loadedTrainers() {
            return trainers;
        }
    }
}
