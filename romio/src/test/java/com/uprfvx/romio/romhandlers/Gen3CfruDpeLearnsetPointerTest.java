package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Gen3CfruDpeLearnsetPointerTest {

    @Test
    public void cfruDpeLevelUpLearnsetPointerOffsetUsesInternalSpeciesIdentity() {
        int tableOffset = 0x25D7B4;

        assertEquals(0x25D7B4 + 1427 * 4,
                Gen3RomHandler.cfruDpeLevelUpLearnsetPointerOffset(tableOffset, 1427));
        assertEquals(0x25D7B4 + 1439 * 4,
                Gen3RomHandler.cfruDpeLevelUpLearnsetPointerOffset(tableOffset, 1439));
        assertEquals((1439 + 1) * 4, Gen3RomHandler.cfruDpeLevelUpLearnsetsTableLength());
    }

    @Test
    public void cfruDpeGen9LearnsetsOverrideGenericJamboMovesetDetection() {
        assertEquals(true, Gen3RomHandler.shouldUseCfruDpeLevelUpLearnsets(true, true));
        assertEquals(true, Gen3RomHandler.shouldUseCfruDpeLevelUpLearnsets(true, false));
        assertEquals(false, Gen3RomHandler.shouldUseCfruDpeLevelUpLearnsets(false, true));
    }

    @Test
    public void cfruRuntimeLearnsetPointerOverridesFallbackWhenValid() {
        byte[] rom = new byte[0x600000];
        int runtimeTableOffset = 0x300000;
        writePointer(rom, 0x43E20, runtimeTableOffset);

        assertEquals(runtimeTableOffset, Gen3RomHandler.readCfruRuntimeLevelUpLearnsetsOffset(rom));
        assertEquals(true, Gen3RomHandler.hasValidCfruRuntimeLevelUpLearnsetsPointer(rom));
        assertEquals(runtimeTableOffset, Gen3RomHandler.chooseCfruDpeLevelUpLearnsetsOffset(rom, 0x25D7B4));
    }

    @Test
    public void cfruRuntimeLearnsetPointerFallsBackWhenZeroOrOutOfBounds() {
        byte[] zeroPointerRom = new byte[0x600000];
        assertEquals(false, Gen3RomHandler.hasValidCfruRuntimeLevelUpLearnsetsPointer(zeroPointerRom));
        assertEquals(0x25D7B4, Gen3RomHandler.chooseCfruDpeLevelUpLearnsetsOffset(zeroPointerRom, 0x25D7B4));

        byte[] outOfBoundsPointerRom = new byte[0x600000];
        writePointer(outOfBoundsPointerRom, 0x43E20, 0x5FF000);
        assertEquals(false, Gen3RomHandler.hasValidCfruRuntimeLevelUpLearnsetsPointer(outOfBoundsPointerRom));
        assertEquals(0x25D7B4, Gen3RomHandler.chooseCfruDpeLevelUpLearnsetsOffset(outOfBoundsPointerRom, 0x25D7B4));
    }

    private static void writePointer(byte[] rom, int offset, int pointer) {
        int gbaPointer = pointer + 0x8000000;
        rom[offset] = (byte) (gbaPointer & 0xFF);
        rom[offset + 1] = (byte) ((gbaPointer >> 8) & 0xFF);
        rom[offset + 2] = (byte) ((gbaPointer >> 16) & 0xFF);
        rom[offset + 3] = (byte) ((gbaPointer >> 24) & 0xFF);
    }
}
