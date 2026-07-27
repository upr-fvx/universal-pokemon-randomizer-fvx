package com.uprfvx.romio.gamedata.basestats;

import org.junit.jupiter.api.Test;

import static com.uprfvx.romio.gamedata.basestats.BaseStats.STAT_MAX;
import static org.junit.jupiter.api.Assertions.*;

public class Gen1BaseStatsTest {

    @Test
    public void init_WithNegativeSpecial_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Gen1BaseStats(10, 10, 10, 10, -1));
    }

    @Test
    public void init_WithSpecialAboveMax_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Gen1BaseStats(10, 10, 10, 10, STAT_MAX + 1));
    }

    @Test
    public void init_WithValidStats_GettersReturnThoseStats() {
        // This is sufficient to know calculateStats() is linked up correctly for the subclass
        Gen1BaseStats a = new Gen1BaseStats(10, 20, 30, 40, 50);

        assertEquals(10, a.getHp());
        assertEquals(20, a.getAttack());
        assertEquals(30, a.getDefense());
        assertEquals(40, a.getSpeed());
        assertEquals(50, a.getSpecial());
    }

    @Test
    public void init_WithValidStats_BSTReturnsSumOfThoseStats() {
        Gen1BaseStats a = new Gen1BaseStats(10, 20, 30, 40, 50);
        assertEquals(10 + 20 + 30 + 40 + 50, a.getBST());
    }

    @Test
    public void getSpatk_ThrowsUnsupportedOperationException() {
        Gen1BaseStats a = new Gen1BaseStats(10, 10, 10, 10, 10);
        assertThrows(UnsupportedOperationException.class, a::getSpatk);
    }

    @Test
    public void getSpdef_ThrowsUnsupportedOperationException() {
        Gen1BaseStats a = new Gen1BaseStats(10, 10, 10, 10, 10);
        assertThrows(UnsupportedOperationException.class, a::getSpdef);
    }

    @Test
    public void setStatRatios_UsingBaseMethod_ThrowsUnsupportedOperationException() {
        Gen1BaseStats a = new Gen1BaseStats(10, 10, 10, 10, 10);
        assertThrows(UnsupportedOperationException.class,
                () -> a.setStatRatios(1, 1, 1, 1, 1, 1));
    }

}
