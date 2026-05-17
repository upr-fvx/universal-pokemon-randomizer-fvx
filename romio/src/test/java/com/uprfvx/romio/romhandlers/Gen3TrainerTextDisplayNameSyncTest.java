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
        assertEquals(1, trainer.getTrainerclass());
        assertEquals("ENGINEER Quinn", trainer.getFullDisplayName());
    }

    @Test
    void trainerRecordLayoutKeepsClassIdSeparateFromMusicGenderAndSprite() {
        assertEquals(1, Gen3RomHandler.GEN3_TRAINER_CLASS_OFFSET);
        assertEquals(2, Gen3RomHandler.GEN3_TRAINER_ENCOUNTER_MUSIC_GENDER_OFFSET);
        assertEquals(3, Gen3RomHandler.GEN3_TRAINER_PIC_OFFSET);
        assertEquals(4, Gen3RomHandler.GEN3_TRAINER_NAME_OFFSET);
    }

    @Test
    void trainerClassNameTextRemapDoesNotChangeTrainerClassIdOrSpriteRecordBytes() throws Exception {
        RomBackedTestableGen3RomHandler romHandler = new RomBackedTestableGen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("TrainerClassNames", 0x20);
        romEntry.putIntValue("TrainerClassCount", 3);
        romEntry.putIntValue("TrainerClassNameLength", 13);

        int trainerOffset = 0x100;
        byte[] rom = new byte[0x200];
        rom[trainerOffset + Gen3RomHandler.GEN3_TRAINER_CLASS_OFFSET] = 1;
        rom[trainerOffset + Gen3RomHandler.GEN3_TRAINER_ENCOUNTER_MUSIC_GENDER_OFFSET] = (byte) 0x82;
        rom[trainerOffset + Gen3RomHandler.GEN3_TRAINER_PIC_OFFSET] = 7;

        Trainer trainer = trainer(1, "Pi", "BUG CATCHER Pi");
        trainer.setOffset(trainerOffset);
        romHandler.addTrainer(trainer);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "rom", rom);
        romHandler.initTextTables();

        romHandler.setTrainerClassNames(List.of("UNUSED", "PKMN RANGER", "TUBER"));

        assertEquals(1, trainer.getTrainerclass());
        assertEquals("PKMN RANGER Pi", trainer.getFullDisplayName());
        assertEquals(1, rom[trainerOffset + Gen3RomHandler.GEN3_TRAINER_CLASS_OFFSET]);
        assertEquals((byte) 0x82, rom[trainerOffset + Gen3RomHandler.GEN3_TRAINER_ENCOUNTER_MUSIC_GENDER_OFFSET]);
        assertEquals(7, rom[trainerOffset + Gen3RomHandler.GEN3_TRAINER_PIC_OFFSET]);
    }

    @Test
    void proposedTrainerClassAssignmentWouldNeedToChangeTrainerClassId() {
        TestableGen3RomHandler romHandler = new TestableGen3RomHandler();
        Trainer trainer = trainer(1, "Pi", "BUG CATCHER Pi");
        romHandler.addTrainer(trainer);

        trainer.setTrainerclass(2);
        romHandler.refreshTrainerFullDisplayNames(List.of("UNUSED", "BUG CATCHER", "PKMN RANGER"));

        assertEquals(2, trainer.getTrainerclass());
        assertEquals("PKMN RANGER Pi", trainer.getFullDisplayName());
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

    @Test
    void vanillaTrainerClassNamesUseStaticTableOffset() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("TrainerClassNames", 0x80);
        romEntry.putIntValue("TrainerClassCount", 2);
        romEntry.putIntValue("TrainerClassNameLength", 13);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "rom", new byte[0x400]);

        assertEquals(0x80, romHandler.getTrainerClassNamesOffsetForDiagnostics());
    }

    @Test
    void cfruDpeTrainerClassNamesUseRuntimePointerWhenAvailable() {
        byte[] rom = new byte[Gen3RomHandler.CFRU_RUNTIME_TRAINER_CLASS_NAMES_POINTER_LOCATION + 4];
        writePointer(rom, Gen3RomHandler.CFRU_RUNTIME_TRAINER_CLASS_NAMES_POINTER_LOCATION, 0x200);

        int offset = Gen3RomHandler.chooseCfruDpeTrainerClassNamesOffset(rom, 0x80, 26);

        assertEquals(0x200, offset);
    }

    @Test
    void cfruDpeTrainerClassNamesFallBackToStaticOffsetWhenRuntimePointerInvalid() {
        byte[] rom = new byte[Gen3RomHandler.CFRU_RUNTIME_TRAINER_CLASS_NAMES_POINTER_LOCATION + 4];

        int offset = Gen3RomHandler.chooseCfruDpeTrainerClassNamesOffset(rom, 0x80, 26);

        assertEquals(0x80, offset);
    }

    @Test
    void cfruDpeSetTrainerClassNamesWritesRuntimeTableThatGetReads() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("TrainerClassNames", 0x80);
        romEntry.putIntValue("TrainerClassCount", 2);
        romEntry.putIntValue("TrainerClassNameLength", 13);

        byte[] rom = new byte[Gen3RomHandler.CFRU_RUNTIME_TRAINER_CLASS_NAMES_POINTER_LOCATION + 4];
        writePointer(rom, Gen3RomHandler.CFRU_RUNTIME_TRAINER_CLASS_NAMES_POINTER_LOCATION, 0x200);

        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "rom", rom);
        setField(romHandler, "useCfruDpeGen9SpeciesCount", true);
        romHandler.initTextTables();

        romHandler.setTrainerClassNames(List.of("TUBER", "BUG CATCHER"));

        assertEquals(0x200, romHandler.getTrainerClassNamesOffsetForDiagnostics());
        assertEquals(List.of("TUBER", "BUG CATCHER"), romHandler.getTrainerClassNames());
        assertEquals(0, rom[0x80]);
    }

    private static Trainer trainer(int trainerClass, String name, String fullDisplayName) {
        Trainer trainer = new Trainer();
        trainer.setTrainerclass(trainerClass);
        trainer.setName(name);
        trainer.setFullDisplayName(fullDisplayName);
        return trainer;
    }

    private static void writePointer(byte[] rom, int offset, int tableOffset) {
        int pointer = tableOffset + 0x8000000;
        rom[offset] = (byte) pointer;
        rom[offset + 1] = (byte) (pointer >> 8);
        rom[offset + 2] = (byte) (pointer >> 16);
        rom[offset + 3] = (byte) (pointer >> 24);
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

    private static class RomBackedTestableGen3RomHandler extends Gen3RomHandler {
        void addTrainer(Trainer trainer) {
            trainers.add(trainer);
        }
    }
}
