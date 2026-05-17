package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3OakLabRivalScriptTest {

    @Test
    public void oakLabRivalTrainerIdsAreExtractedByPlayerStarterSlot() {
        byte[] rom = new byte[512];
        writeOakTutorialTrainerBattle(rom, 100, 901);
        writeOakTutorialTrainerBattle(rom, 140, 902);
        writeOakTutorialTrainerBattle(rom, 180, 903);

        List<Integer> trainerIds = Gen3RomHandler.findFrlgOakLabRivalTrainerIdsByPlayerStarterSlot(rom, 64);

        assertEquals(List.of(902, 903, 901), trainerIds);
    }

    @Test
    public void oakLabRivalTrainerIdsSkipWhenTutorialCommandsAreMissing() {
        byte[] rom = new byte[512];
        writeTrainerBattle(rom, 100, 0x09, 901, 0);
        writeOakTutorialTrainerBattle(rom, 140, 902);

        List<Integer> trainerIds = Gen3RomHandler.findFrlgOakLabRivalTrainerIdsByPlayerStarterSlot(rom, 64);

        assertTrue(trainerIds.isEmpty());
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
}
