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
    }
}
