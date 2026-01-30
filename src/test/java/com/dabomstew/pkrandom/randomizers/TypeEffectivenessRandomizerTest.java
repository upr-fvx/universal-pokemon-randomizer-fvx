package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.TypeEffectivenessRandomizer;
import com.dabomstew.pkromio.gamedata.Effectiveness;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.gamedata.TypeTable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TypeEffectivenessRandomizerTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeTypeEffectivenessPreservesEffectivenessCounts(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasTypeEffectivenessSupport(), "Does not have Type Effectiveness support.");

        TypeTable before = romHandler.getTypeTable();
        int[] effCountsBefore = getEffCounts(before);
        System.out.println(before.toBigString());
        System.out.println(Arrays.toString(effCountsBefore));

        new TypeEffectivenessRandomizer(romHandler, new Settings(), RND).randomizeTypeEffectiveness(false);

        TypeTable after = romHandler.getTypeTable();
        int[] effCountsAfter = getEffCounts(after);
        System.out.println(after.toBigString());
        System.out.println(Arrays.toString(effCountsAfter));

        assertArrayEquals(effCountsBefore, effCountsAfter);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeTypeEffectivenessBalancedWorks(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasTypeEffectivenessSupport(), "Does not have Type Effectiveness support.");

        TypeTable before = romHandler.getTypeTable();
        int maxImmWhenAttacking = 0;
        int maxNVEWhenAttacking = 0;
        int maxSEWhenAttacking = 0;
        int maxImmWhenDefending = 0;
        int maxNVEWhenDefending = 0;
        int maxSEWhenDefending = 0;
        for (Type t : before.getTypes()) {
            maxImmWhenAttacking = Math.max(maxImmWhenAttacking, before.immuneWhenAttacking(t).size());
            maxNVEWhenAttacking = Math.max(maxNVEWhenAttacking, before.notVeryEffectiveWhenAttacking(t).size());
            maxSEWhenAttacking = Math.max(maxSEWhenAttacking, before.superEffectiveWhenAttacking(t).size());
            maxImmWhenDefending = Math.max(maxImmWhenDefending, before.immuneWhenDefending(t).size());
            maxNVEWhenDefending = Math.max(maxNVEWhenDefending, before.notVeryEffectiveWhenDefending(t).size());
            maxSEWhenDefending = Math.max(maxSEWhenDefending, before.superEffectiveWhenDefending(t).size());
        }

        new TypeEffectivenessRandomizer(romHandler, new Settings(), RND).randomizeTypeEffectiveness(true);
        TypeTable after = romHandler.getTypeTable();
        System.out.println(after.toBigString());

        System.out.println("MAX");
        System.out.println("imm att: " + maxImmWhenAttacking);
        System.out.println("NVE att: " + maxNVEWhenAttacking);
        System.out.println("SE att:  " + maxSEWhenAttacking);
        System.out.println("imm def: " + maxImmWhenDefending);
        System.out.println("NVE def: " + maxNVEWhenDefending);
        System.out.println("SE def:  " + maxSEWhenDefending);

        int faults = 0;
        for (Type t : after.getTypes()) {
            System.out.print("\n" + t);
            System.out.print("\nimm att: " + after.immuneWhenAttacking(t).size());
            if (after.immuneWhenAttacking(t).size() > maxImmWhenAttacking) {
                faults++;
                System.out.print(" - fault");
            }
            System.out.print("\nNVE att: " + after.notVeryEffectiveWhenAttacking(t).size());
            if (after.notVeryEffectiveWhenAttacking(t).size() > maxNVEWhenAttacking) {
                faults++;
                System.out.print(" - fault");
            }
            System.out.print("\nSE att:  " + after.superEffectiveWhenAttacking(t).size());
            if (after.superEffectiveWhenAttacking(t).size() > maxSEWhenAttacking) {
                faults++;
                System.out.print(" - fault");
            }
            System.out.print("\nimm def: " + after.immuneWhenDefending(t).size());
            if (after.immuneWhenDefending(t).size() > maxImmWhenDefending) {
                faults++;
                System.out.print(" - fault");
            }
            System.out.print("\nNVE def: " + after.notVeryEffectiveWhenDefending(t).size());
            if (after.notVeryEffectiveWhenDefending(t).size() > maxNVEWhenDefending) {
                faults++;
                System.out.print(" - fault");
            }
            System.out.print("\nSE def:  " + after.superEffectiveWhenDefending(t).size());
            if (after.superEffectiveWhenDefending(t).size() > maxSEWhenDefending) {
                faults++;
                System.out.print(" - fault");
            }
            System.out.println("");
        }
        System.out.println("\nFaults: " + faults);
        assertEquals(0, faults);
    }

    private int[] getEffCounts(TypeTable typeTable) {
        int[] effCounts = new int[Effectiveness.values().length];
        for (Type attacker : typeTable.getTypes()) {
            for (Type defender : typeTable.getTypes()) {
                Effectiveness eff = typeTable.getEffectiveness(attacker, defender);
                effCounts[eff.ordinal()]++;
            }
        }
        return effCounts;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeTypeEffectivenessKeepIdentitiesWorks(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasTypeEffectivenessSupport(), "Does not have Type Effectiveness support.");

        TypeTable before = new TypeTable(romHandler.getTypeTable());
        new TypeEffectivenessRandomizer(romHandler, new Settings(), RND).randomizeTypeEffectivenessKeepIdentities();
        TypeTable after = romHandler.getTypeTable();

        System.out.println("Before:");
        System.out.println(before.toBigString());
        System.out.println(before.nonNeutralEffectivenessCount());
        System.out.println(Arrays.toString(getEffCounts(before)));
        System.out.println("After:");
        System.out.println(after.toBigString());
        System.out.println(after.nonNeutralEffectivenessCount());
        System.out.println(Arrays.toString(getEffCounts(after)));
        for (Type t : before.getTypes()) {
            System.out.println(t);
            assertEquals(before.immuneWhenAttacking(t).size(), after.immuneWhenAttacking(t).size());
            assertEquals(before.notVeryEffectiveWhenAttacking(t).size(), after.notVeryEffectiveWhenAttacking(t).size());
            assertEquals(before.superEffectiveWhenAttacking(t).size(), after.superEffectiveWhenAttacking(t).size());
            assertEquals(before.immuneWhenDefending(t).size(), after.immuneWhenDefending(t).size());
            assertEquals(before.notVeryEffectiveWhenDefending(t).size(), after.notVeryEffectiveWhenDefending(t).size());
            assertEquals(before.superEffectiveWhenDefending(t).size(), after.superEffectiveWhenDefending(t).size());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void invertTypeEffectivenessWorks(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasTypeEffectivenessSupport(), "Does not have Type Effectiveness support.");

        TypeTable before = new TypeTable(romHandler.getTypeTable());
        new TypeEffectivenessRandomizer(romHandler, new Settings(), RND).invertTypeEffectiveness(false);
        TypeTable after = romHandler.getTypeTable();

        for (Type attacker : after.getTypes()) {
            for (Type defender : after.getTypes()) {

                System.out.println(attacker + " vs. " + defender);
                Effectiveness beforeEff = before.getEffectiveness(attacker, defender);
                System.out.println("before: " + beforeEff);
                Effectiveness afterEff = after.getEffectiveness(attacker, defender);
                System.out.println("after: " + afterEff);

                if (beforeEff == Effectiveness.HALF || beforeEff == Effectiveness.ZERO) {
                    assertEquals(Effectiveness.DOUBLE, afterEff);
                } else if (beforeEff == Effectiveness.DOUBLE) {
                    assertEquals(Effectiveness.HALF, afterEff);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void invertTypeEffectivenessWithRandomImmsDoesNotChangeImmCount(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasTypeEffectivenessSupport(), "Does not have Type Effectiveness support.");

        TypeTable before = new TypeTable(romHandler.getTypeTable());
        new TypeEffectivenessRandomizer(romHandler, new Settings(), RND).invertTypeEffectiveness(true);
        TypeTable after = romHandler.getTypeTable();
        int immCountBefore = 0;
        int immCountAfter = 0;
        for (Type attacker : before.getTypes()) {
            for (Type defender : before.getTypes()) {
                if (before.getEffectiveness(attacker, defender) == Effectiveness.ZERO)
                    immCountBefore++;
                if (after.getEffectiveness(attacker, defender) == Effectiveness.ZERO)
                    immCountAfter++;
            }
        }
        System.out.println(after.toBigString());
        assertEquals(immCountBefore, immCountAfter);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void invertTypeEffectivenessWithRandomImmsChangesSEToImms(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasTypeEffectivenessSupport(), "Does not have Type Effectiveness support.");

        TypeTable before = new TypeTable(romHandler.getTypeTable());
        new TypeEffectivenessRandomizer(romHandler, new Settings(), RND).invertTypeEffectiveness(true);
        TypeTable after = romHandler.getTypeTable();
        for (Type attacker : before.getTypes()) {
            for (Type defender : before.getTypes()) {
                if (after.getEffectiveness(attacker, defender) == Effectiveness.ZERO) {
                    assertEquals(Effectiveness.DOUBLE, before.getEffectiveness(attacker, defender));
                }
            }
        }
    }

}
