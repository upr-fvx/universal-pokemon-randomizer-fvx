package com.uprfvx.romio.gamedata.basestats;

import org.junit.jupiter.api.Test;

import static com.uprfvx.romio.gamedata.basestats.BaseStats.STAT_MAX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseStatsTest {

    @Test
    public void init_WithNegativeStat_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                -1, 1, 1, 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, -1, 1, 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, -1, 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, 1, -1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, 1, 1, -1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, 1, 1, 1, -1
        ));
    }

    @Test
    public void init_WithStatAboveStatMax_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                STAT_MAX + 1, 1, 1, 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, STAT_MAX + 1, 1, 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, STAT_MAX + 1, 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, 1, STAT_MAX + 1, 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, 1, 1, STAT_MAX + 1, 1
        ));
        assertThrows(IllegalArgumentException.class, () -> new BaseStats(
                1, 1, 1, 1, 1, STAT_MAX + 1
        ));
    }

    @Test
    public void init_WithValidStats_GettersReturnThoseStats() {
        BaseStats a = new BaseStats(10, 20, 30, 40, 50, 60);
        assertEquals(10, a.getHp());
        assertEquals(20, a.getAttack());
        assertEquals(30, a.getDefense());
        assertEquals(40, a.getSpatk());
        assertEquals(50, a.getSpdef());
        assertEquals(60, a.getSpeed());
    }

    @Test
    public void init_WithValidStats_BSTReturnsSumOfThoseStats() {
        BaseStats a = new BaseStats(10, 20, 30, 40, 50, 60);
        assertEquals(10 + 20 + 30 + 40 + 50 + 60, a.getBST());
    }

    @Test
    public void setBST_NegativeBST_ThrowsIllegalArgumentException() {
        BaseStats a = new BaseStats(10, 10, 10, 10, 10, 10);
        assertThrows(IllegalArgumentException.class, () -> a.setBST(-1));
    }

    @Test
    public void setStatRatios_NegativeRatio_ThrowsIllegalArgumentException() {
        BaseStats a = new BaseStats(10, 10, 10, 10, 10, 10);

        assertThrows(IllegalArgumentException.class,
                () -> a.setStatRatios(-1, 1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> a.setStatRatios(1, -1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> a.setStatRatios(1, 1, -1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> a.setStatRatios(1, 1, 1, -1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> a.setStatRatios(1, 1, 1, 1, -1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> a.setStatRatios(1, 1, 1, 1, 1, -1));
    }

    // We know that both setBST() and setStatRatios() just set those attributes,
    // and then call calculateStats().
    // Thus, all methods below really test calculateStats(), even if it is never called directly.
    // (calling it directly is impossible!)

    @Test
    public void calculateStats_DoubleBST_DoubledStatsAndBST() {
        BaseStats a = new BaseStats(10, 20, 30, 40, 50, 60);
        int originalBST = a.getBST();
        a.setBST(originalBST * 2);

        assertEquals(20, a.getHp());
        assertEquals(40, a.getAttack());
        assertEquals(60, a.getDefense());
        assertEquals(80, a.getSpatk());
        assertEquals(100, a.getSpdef());
        assertEquals(120, a.getSpeed());
        assertEquals(originalBST * 2, a.getBST());
    }

    @Test
    public void calculateStats_HalveBST_HalvedStatsAndBST() {
        BaseStats a = new BaseStats(10, 20, 30, 40, 50, 60);
        int originalBST = a.getBST();
        a.setBST(originalBST / 2);

        assertEquals(5, a.getHp());
        assertEquals(10, a.getAttack());
        assertEquals(15, a.getDefense());
        assertEquals(20, a.getSpatk());
        assertEquals(25, a.getSpdef());
        assertEquals(30, a.getSpeed());
        assertEquals(originalBST / 2, a.getBST());
    }

    @Test
    public void calculateStats_MaxBST_AllStatsAreMaxed() {
        BaseStats a = new BaseStats(10, 20, 30, 40, 50, 60); // intentionally different
        a.setBST(STAT_MAX * 6);

        assertEquals(STAT_MAX, a.getHp());
        assertEquals(STAT_MAX, a.getAttack());
        assertEquals(STAT_MAX, a.getDefense());
        assertEquals(STAT_MAX, a.getSpatk());
        assertEquals(STAT_MAX, a.getSpdef());
        assertEquals(STAT_MAX, a.getSpeed());
    }

    @Test
    public void calculateStats_AboveMaxBST_ThrowsIllegalStateException() {
        BaseStats a = new BaseStats(10, 10, 10, 10, 10, 10);
        assertThrows(IllegalStateException.class, () -> a.setBST(STAT_MAX * 6 + 1));
    }

    @Test
    public void calculateStats_AllZeroRatiosNonZeroBST_ThrowsIllegalStateException() {
        BaseStats a = new BaseStats(0, 0, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> a.setBST(10));
    }

    @Test
    public void calculateStats_ZeroRatiosExceptMaxedStat_ThrowsIllegalStateException() {
        // tests that no stats are assigned to stats with ratio=0.
        BaseStats a = new BaseStats(STAT_MAX, 0, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> a.setBST(STAT_MAX + 1));
    }

    @Test
    public void calculateStats_IncreaseBSTWithSomeStatMaxed_OtherStatsIncreaseProportionally() {
        BaseStats a = new BaseStats(STAT_MAX, 10, 20, 0, 0, 0);
        int originalBST = a.getBST();
        int boost = 30;
        a.setBST(originalBST + boost);

        assertEquals(STAT_MAX, a.getHp());
        assertEquals(20, a.getAttack());
        assertEquals(40, a.getDefense());
        assertEquals(0, a.getSpatk());
        assertEquals(0, a.getSpdef());
        assertEquals(0, a.getSpeed());
    }

    // needed: a bunch of rounding tests
    // - test something with a monstrously higher ratio (that gets maxed out) in one stat very low ratios in the rest,
    //      them still getting raised stats instead of being 0:d out
    // ...
}

