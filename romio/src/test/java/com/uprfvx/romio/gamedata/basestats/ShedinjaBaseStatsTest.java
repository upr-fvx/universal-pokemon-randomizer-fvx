package com.uprfvx.romio.gamedata.basestats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShedinjaBaseStatsTest {

    @Test
    public void init_WithValidStats_GettersReturnThoseStats() {
        // This is sufficient to know calculateStats() is linked up correctly for the subclass
        ShedinjaBaseStats a = new ShedinjaBaseStats(10, 20, 30, 40, 50);

        assertEquals(10, a.getAttack());
        assertEquals(20, a.getDefense());
        assertEquals(30, a.getSpatk());
        assertEquals(40, a.getSpdef());
        assertEquals(50, a.getSpeed());
    }

    @Test
    public void init_WithValidStats_BSTReturnsSumOfThoseStats_TimesSixFifths() {
        ShedinjaBaseStats a = new ShedinjaBaseStats(10, 20, 30, 40, 50);
        assertEquals((10 + 20 + 30 + 40 + 50) * 6/5, a.getBST());
    }

    @Test
    public void getHp_ReturnsOne() {
        ShedinjaBaseStats a = new ShedinjaBaseStats(10, 10, 10, 10, 10);
        assertEquals(1, a.getHp());
    }

    @Test
    public void setBST_WithShedinjasActualStats_ToCurrentBST_DoesNotChangeBST() {
        ShedinjaBaseStats a = new ShedinjaBaseStats(90, 45, 30, 30, 40);

        int before = a.getBST();
        a.setBST(a.getBST());
        assertEquals(before, a.getBST());
    }

    @Test
    public void setStatRatios_UsingBaseMethod_ThrowsUnsupportedOperationException() {
        ShedinjaBaseStats a = new ShedinjaBaseStats(10, 10, 10, 10, 10);
        assertThrows(UnsupportedOperationException.class,
                () -> a.setStatRatios(1, 1, 1, 1, 1, 1));
    }

    @Test
    public void setStatRatios_ToShuffledVanillaStats_SetsThemProperly() {
        ShedinjaBaseStats a = new ShedinjaBaseStats(90, 45, 30, 30, 40);
        a.setStatRatios(30, 30, 40, 45, 90);
        assertEquals(30, a.getAttack());
        assertEquals(30, a.getDefense());
        assertEquals(40, a.getSpatk());
        assertEquals(45, a.getSpdef());
        assertEquals(90, a.getSpeed());
    }

}
