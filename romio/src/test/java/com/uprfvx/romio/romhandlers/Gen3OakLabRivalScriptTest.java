package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Gen3OakLabRivalScriptTest {

    @Test
    public void oakLabRivalTrainerIdsAreExtractedByPlayerStarterSlot() {
        byte[] rom = new byte[512];
        writeOakTutorialTrainerBattle(rom, 100, 901);
        writeOakTutorialTrainerBattle(rom, 140, 902);
        writeOakTutorialTrainerBattle(rom, 180, 903);

        List<Integer> trainerIds = Gen3RomHandler.findFrlgOakLabRivalTrainerIdsByPlayerStarterSlot(rom, 64);

        assertEquals(List.of(902, 901, 903), trainerIds);
    }

    @Test
    public void oakLabRivalTrainerIdsSkipWhenTutorialCommandsAreMissing() {
        byte[] rom = new byte[512];
        writeTrainerBattle(rom, 100, 0x09, 901, 0);
        writeOakTutorialTrainerBattle(rom, 140, 902);

        List<Integer> trainerIds = Gen3RomHandler.findFrlgOakLabRivalTrainerIdsByPlayerStarterSlot(rom, 64);

        assertTrue(trainerIds.isEmpty());
    }

    @Test
    public void oakLabStarterScriptContainsSeparatePlayerAndRivalStarterSpecies() {
        byte[] rom = new byte[1024];
        writeWord(rom, 64, 1001);
        writeWord(rom, 69, 1002);
        writeWord(rom, 64 + 515, 1003);
        writeWord(rom, 64 + 520, 1004);
        writeWord(rom, 64 + 461, 1005);
        writeWord(rom, 64 + 466, 1006);

        List<Gen3RomHandler.FrlgOakLabStarterScriptSlot> slots =
                Gen3RomHandler.readFrlgOakLabStarterScriptSlots(rom, 64);

        assertEquals(3, slots.size());
        assertEquals(new Gen3RomHandler.FrlgOakLabStarterScriptSlot(0, 64, 1001, 69, 1002), slots.get(0));
        assertEquals(new Gen3RomHandler.FrlgOakLabStarterScriptSlot(1, 579, 1003, 584, 1004), slots.get(1));
        assertEquals(new Gen3RomHandler.FrlgOakLabStarterScriptSlot(2, 525, 1005, 530, 1006), slots.get(2));
    }

    @Test
    public void rawTrainerPartyOffsetCanReadTrainerRowsOutsideLoadedTrainerCount() {
        byte[] rom = new byte[2048];
        int trainerDataOffset = 128;
        int trainerEntrySize = 40;
        int trainerId = 26;
        int trainerOffset = trainerDataOffset + trainerId * trainerEntrySize;
        int partyOffset = 1600;
        rom[trainerOffset + (trainerEntrySize - 8)] = 1;
        writePointer(rom, trainerOffset + (trainerEntrySize - 4), partyOffset);

        int firstPokemonOffset = Gen3RomHandler.getFirstRawTrainerPokemonOffset(
                rom, trainerDataOffset, trainerEntrySize, trainerId);

        assertEquals(partyOffset, firstPokemonOffset);
    }

    @Test
    public void rawTrainerPartyOffsetSkipsEmptyOrInvalidRawRows() {
        byte[] rom = new byte[2048];
        int trainerDataOffset = 128;
        int trainerEntrySize = 40;
        int emptyTrainerId = 26;
        int invalidPointerTrainerId = 27;
        int invalidTrainerOffset = trainerDataOffset + invalidPointerTrainerId * trainerEntrySize;
        rom[invalidTrainerOffset + (trainerEntrySize - 8)] = 1;

        assertEquals(-1, Gen3RomHandler.getFirstRawTrainerPokemonOffset(
                rom, trainerDataOffset, trainerEntrySize, emptyTrainerId));
        assertEquals(-1, Gen3RomHandler.getFirstRawTrainerPokemonOffset(
                rom, trainerDataOffset, trainerEntrySize, invalidPointerTrainerId));
    }

    @Test
    public void trainerBattleRuntimeSourceMapsScriptTrainerIdToRawTrainerParty() {
        byte[] rom = new byte[2048];
        int trainerDataOffset = 128;
        int trainerEntrySize = 40;
        int trainerId = 17;
        int trainerOffset = trainerDataOffset + trainerId * trainerEntrySize;
        int partyOffset = 1400;
        rom[trainerOffset] = 0;
        rom[trainerOffset + (trainerEntrySize - 8)] = 1;
        writePointer(rom, trainerOffset + (trainerEntrySize - 4), partyOffset);
        writeWord(rom, partyOffset + 2, 12);
        writeWord(rom, partyOffset + 4, 321);
        writeTrainerBattle(rom, 64, 0, trainerId, 0);

        List<Gen3RomHandler.FrlgTrainerBattleRuntimeSource> sources =
                Gen3RomHandler.findFrlgTrainerBattleRuntimeSources(rom, trainerDataOffset, trainerEntrySize, 0,
                        rom.length);

        assertEquals(1, sources.size());
        Gen3RomHandler.FrlgTrainerBattleRuntimeSource source = sources.get(0);
        assertEquals(64, source.scriptOffset());
        assertEquals(trainerId, source.trainerId());
        assertEquals(trainerOffset, source.trainerOffset());
        assertEquals(partyOffset, source.partyPointer());
        assertTrue(source.trainerEntryValid());
        assertTrue(source.partyPointerValid());
        assertEquals(partyOffset, source.firstPokemonOffset());
        assertEquals(321, source.firstRawSpeciesId());
    }

    @Test
    public void trainerBattleRuntimeSourceKeepsInvalidTrainerRowsVisible() {
        byte[] rom = new byte[512];
        int trainerDataOffset = 128;
        int trainerEntrySize = 40;
        int trainerId = 50;
        writeTrainerBattle(rom, 64, 0, trainerId, 0);

        List<Gen3RomHandler.FrlgTrainerBattleRuntimeSource> sources =
                Gen3RomHandler.findFrlgTrainerBattleRuntimeSources(rom, trainerDataOffset, trainerEntrySize, 0,
                        rom.length);

        assertEquals(1, sources.size());
        Gen3RomHandler.FrlgTrainerBattleRuntimeSource source = sources.get(0);
        assertEquals(trainerId, source.trainerId());
        assertFalse(source.trainerEntryValid());
        assertFalse(source.partyPointerValid());
        assertEquals(-1, source.firstPokemonOffset());
        assertEquals(-1, source.firstRawSpeciesId());
    }

    @Test
    public void referenceOakLabSourceUsesRivalStarterScriptVariableBeforeBattle() throws IOException {
        Path oakLabScriptPath = referenceSourcePath("data/maps/PalletTown_ProfessorOaksLab/scripts.inc");
        assumeTrue(Files.isRegularFile(oakLabScriptPath), "pret FireRed reference source is not available");

        String source = Files.readString(oakLabScriptPath);
        assertTrue(source.contains(".equ PLAYER_STARTER_SPECIES,  VAR_TEMP_2"));
        assertTrue(source.contains(".equ RIVAL_STARTER_SPECIES,   VAR_TEMP_3"));
        assertTrue(source.contains("setvar PLAYER_STARTER_SPECIES, SPECIES_BULBASAUR"));
        assertTrue(source.contains("setvar RIVAL_STARTER_SPECIES, SPECIES_CHARMANDER"));
        assertTrue(source.contains("setvar PLAYER_STARTER_SPECIES, SPECIES_SQUIRTLE"));
        assertTrue(source.contains("setvar RIVAL_STARTER_SPECIES, SPECIES_BULBASAUR"));
        assertTrue(source.contains("setvar PLAYER_STARTER_SPECIES, SPECIES_CHARMANDER"));
        assertTrue(source.contains("setvar RIVAL_STARTER_SPECIES, SPECIES_SQUIRTLE"));
        assertTrue(source.contains("trainerbattle_earlyrival TRAINER_RIVAL_OAKS_LAB_CHARMANDER"));
        assertTrue(source.contains("trainerbattle_earlyrival TRAINER_RIVAL_OAKS_LAB_BULBASAUR"));
        assertTrue(source.contains("trainerbattle_earlyrival TRAINER_RIVAL_OAKS_LAB_SQUIRTLE"));
    }

    @Test
    public void cfruSourceKeepsOakTutorialBattleButDoesNotOwnOakLabScript() throws IOException {
        Path overworldSourcePath = cfruSourcePath("src/overworld.c");
        Path configSourcePath = cfruSourcePath("src/config.h");
        Path cfruOakLabScriptPath = cfruSourcePath("assembly/overworld_scripts/PalletTown_ProfessorOaksLab.s");
        assumeTrue(Files.isRegularFile(overworldSourcePath), "CFRU source tree is not available");
        assumeTrue(Files.isRegularFile(configSourcePath), "CFRU source tree is not available");

        String overworldSource = Files.readString(overworldSourcePath);
        String configSource = Files.readString(configSourcePath);
        assertTrue(configSource.contains("#define TUTORIAL_BATTLES"));
        assertTrue(overworldSource.contains("case TRAINER_BATTLE_OAK_TUTORIAL"));
        assertTrue(overworldSource.contains("TrainerBattleLoadArgs(sOakTutorialParams, data)"));
        assertTrue(overworldSource.contains("gBattleTypeFlags |= BATTLE_TYPE_OAK_TUTORIAL"));
        assertFalse(Files.isRegularFile(cfruOakLabScriptPath),
                "If CFRU adds an owned Oak Lab script, the runtime-source diagnosis must inspect it directly.");
    }

    private static void writeOakTutorialTrainerBattle(byte[] rom, int offset, int trainerId) {
        writeTrainerBattle(rom, offset, 0x09, trainerId, 3);
    }

    private static void writeTrainerBattle(byte[] rom, int offset, int battleType, int trainerId, int helperFlags) {
        rom[offset] = 0x5C;
        rom[offset + 1] = (byte) battleType;
        writeWord(rom, offset + 2, trainerId);
        writeWord(rom, offset + 4, helperFlags);
    }

    private static void writeWord(byte[] rom, int offset, int value) {
        rom[offset] = (byte) (value & 0xFF);
        rom[offset + 1] = (byte) ((value >>> 8) & 0xFF);
    }

    private static void writePointer(byte[] rom, int offset, int value) {
        int pointer = value + 0x8000000;
        rom[offset] = (byte) (pointer & 0xFF);
        rom[offset + 1] = (byte) ((pointer >>> 8) & 0xFF);
        rom[offset + 2] = (byte) ((pointer >>> 16) & 0xFF);
        rom[offset + 3] = (byte) ((pointer >>> 24) & 0xFF);
    }

    private static Path referenceSourcePath(String relativePath) {
        List<Path> candidates = List.of(
                Path.of("../references/pret-pokefirered", relativePath),
                Path.of("../../references/pret-pokefirered", relativePath),
                Path.of("02_external/references/pret-pokefirered", relativePath));
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    private static Path cfruSourcePath(String relativePath) {
        List<Path> candidates = List.of(
                Path.of("../CFRU-expansion", relativePath),
                Path.of("../../CFRU-expansion", relativePath),
                Path.of("02_external/CFRU-expansion", relativePath));
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return candidates.get(0);
    }
}
