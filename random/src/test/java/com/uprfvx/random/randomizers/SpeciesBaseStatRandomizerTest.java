package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.basestats.BaseStats;
import com.uprfvx.romio.gamedata.basestats.Gen1BaseStats;
import com.uprfvx.romio.gamedata.basestats.ShedinjaBaseStats;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.gamedata.cueh.EvolvedSpeciesAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static com.uprfvx.random.randomizers.SpeciesBaseStatRandomizer.*;
import static org.junit.jupiter.api.Assertions.*;

public class SpeciesBaseStatRandomizerTest extends RandomizerTest {

    private static final double ACCEPTABLE_BUFFNERF_DEVIANCE = 0.01;

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_RandomBuffNerf_AllBuffsNerfsWithinMaxPercentage(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.RANDOM_BUFF_NERF);
        s.setBSTBuffNerfMaxPercentage(50);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            // mega evos are exempt
            if (pk.isMegaEvolution()) continue;

            assertTrue(pk.getBST(false) >= (int) (pk.getBST(true) * 0.5));
            assertTrue(pk.getBST(false) <= (int) (pk.getBST(true) * 1.5));
        }
    }
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_RandomBuffNerf_FollowEvolutions_Works(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.RANDOM_BUFF_NERF);
        s.setBSTBuffNerfMaxPercentage(50);
        s.setBSTFollowEvolutions(true);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        for (Species evFrom : romHandler.getSpeciesSetInclFormes()) {
            for (Species evTo : evFrom.getEvolutionsFrom().stream().map(Evolution::getTo).toList()) {
                double fromModifier = (double) evFrom.getBST(false) / evFrom.getBST(true);
                double toModifier = (double) evTo.getBST(false) / evTo.getBST(true);

                System.out.println(evFrom.getNumberAndFullName() + ": "
                        + evFrom.getBST(false) + "/" + evFrom.getBST(true) + "="
                        + fromModifier);
                System.out.println(evTo.getNumberAndFullName() + ": "
                        + evTo.getBST(false) + "/" + evTo.getBST(true) + "="
                        + toModifier);

                System.out.println(Math.abs(fromModifier - toModifier));

                assertTrue(Math.abs(fromModifier - toModifier) <= ACCEPTABLE_BUFFNERF_DEVIANCE);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_NoRestriction_Works(String romName) {
        activateRomHandler(romName);

        List<Integer> before = getSortedBSTs(romHandler.getSpeciesSet());

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.SHUFFLE);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        List<Integer> after = getSortedBSTs(romHandler.getSpeciesSet());

        assertEquals(before, after);
    }

    private List<Integer> getSortedBSTs(SpeciesSet set) {
        return set.stream()
                .filter(pk -> !pk.isEssentiallyCosmetic())
                .filter(pk -> !pk.isMegaEvolution())
                .map(pk -> pk.getBST(false))
                .sorted()
                .toList();
    }

    private record FamilyBSTs(String name, List<Integer> bsts) {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof FamilyBSTs other && Objects.equals(bsts, other.bsts);
        }

        @Override
        public String toString() {
            return "[" + name + ", " + bsts + "]";
        }
    }

    private List<FamilyBSTs> getSortedFamilyBSTs(SpeciesSet set) {
        List<FamilyBSTs> allFamilyBSTs = new ArrayList<>();

        SpeciesSet basic = set.filterBasic(false)
                .filter(pk -> !pk.isEssentiallyCosmetic())
                .filter(pk -> !pk.isMegaEvolution());

        for (Species pk : basic) {
            FamilyBSTs familyBSTs = new FamilyBSTs(pk.getFullName(), new ArrayList<>());
            familyBSTs.bsts.add(pk.getBST(false));
            while (!pk.getEvolutionsFrom().isEmpty()) {
                pk = pk.getEvolutionsFrom().getFirst().getTo();
                familyBSTs.bsts.add(pk.getBST(false));
            }
            allFamilyBSTs.add(familyBSTs);
        }

        allFamilyBSTs.sort((o1, o2) -> {
            int lenCompare = Integer.compare(o1.bsts.size(), o2.bsts.size());
            if (lenCompare != 0) return lenCompare;
            for (int i = 0; i < o1.bsts.size(); i++) {
                int elemCompare = Integer.compare(o1.bsts.get(i), o2.bsts.get(i));
                if (elemCompare != 0) return elemCompare;
            }
            return 0;
        });
        return allFamilyBSTs;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_FollowEvolutions_Works(String romName) {
        activateRomHandler(romName);

        List<FamilyBSTs> before = getSortedFamilyBSTs(romHandler.getSpeciesSet());

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.SHUFFLE);
        s.setBSTFollowEvolutions(true);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        List<FamilyBSTs> after = getSortedFamilyBSTs(romHandler.getSpeciesSet());

        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++) {
            System.out.println(before.get(i).name);
            System.out.println(after.get(i).name);
            System.out.println(before.get(i).bsts);
            System.out.print(after.get(i).bsts);
            System.out.println(before.get(i).bsts.equals(after.get(i).bsts) ? "" : " MISMATCH!!");
            System.out.println();
        }
        assertEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_SwapLegendaries_Works(String romName) {
        activateRomHandler(romName);
        SpeciesSet legendaries = romHandler.getSpeciesSet().filter(Species::isLegendary);
        SpeciesSet nonLegendaries = romHandler.getSpeciesSet().filter(pk -> !pk.isLegendary());

        List<Integer> beforeLegs = getSortedBSTs(legendaries);
        List<Integer> beforeNonLegs = getSortedBSTs(nonLegendaries);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.SHUFFLE);
        s.setBSTShuffleSwapLegendaries(true);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        List<Integer> afterLegs = getSortedBSTs(legendaries);
        List<Integer> afterNonLegs = getSortedBSTs(nonLegendaries);

        assertEquals(beforeLegs, afterLegs);
        assertEquals(beforeNonLegs, afterNonLegs);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_FollowEvolutionsAndSwapLegendaries_Works(String romName) {
        activateRomHandler(romName);
        SpeciesSet legendaries = romHandler.getSpeciesSet().filter(Species::isLegendary);
        SpeciesSet nonLegendaries = romHandler.getSpeciesSet().filter(pk -> !pk.isLegendary());

        List<FamilyBSTs> beforeLegs = getSortedFamilyBSTs(legendaries);
        List<FamilyBSTs> beforeNonLegs = getSortedFamilyBSTs(nonLegendaries);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.SHUFFLE);
        s.setBSTFollowEvolutions(true);
        s.setBSTShuffleSwapLegendaries(true);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        List<FamilyBSTs> afterLegs = getSortedFamilyBSTs(legendaries);
        List<FamilyBSTs> afterNonLegs = getSortedFamilyBSTs(nonLegendaries);

        assertEquals(beforeLegs.size(), afterLegs.size());
        assertEquals(beforeLegs, afterLegs);
        assertEquals(beforeNonLegs.size(), afterNonLegs.size());
        assertEquals(beforeNonLegs, afterNonLegs);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Random_AllBSTsAreWithinBounds(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.RANDOM);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.isMegaEvolution()) continue;
            assertTrue(pk.getBST(false) >= SUNKERN_BST);
            assertTrue(pk.getBST(false) <= ARCEUS_BST);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_CosmeticFormesHaveSameBSTAsBaseForme(String romName) {
        activateRomHandler(romName);

        new SpeciesBaseStatRandomizer(romHandler, new Settings(), RND).randomizeBSTs();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.isEssentiallyCosmetic()) {
                Species base = pk.getConceptualBaseForme();
                System.out.println(pk.getNumberAndFullName() + ": " + pk.getBST(false));
                System.out.println(base.getNumberAndFullName() + ": " + base.getBST(false));
                assertEquals(pk.getConceptualBaseForme().getBST(false), pk.getBST(false));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_MegaEvolutionsHaveBSTOfBaseFormePlus100(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.RANDOM);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.isMegaEvolution()) {
                Species base = pk.getBaseForme();
                System.out.println(pk.getNumberAndFullName() + ": " + pk.getBST(false));
                System.out.println(base.getNumberAndFullName() + ": " + base.getBST(false));
                assertEquals(base.getBST(false) + MEGA_BST_BOOST, pk.getBST(false));
            }
        }
    }

    // TODO: just write coverage tests for the rest of base stat distribution, so we got *something*

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shuffleSpeciesStats_StatValuesAreTheSame(String romName) {
        // TODO: oh heck what's up with shedinja again? The bugger...
        activateRomHandler(romName);

        List<List<Integer>> statsBefore = romHandler.getSpeciesSet().stream().map(this::getSortedStats).toList();

        SpeciesBaseStatRandomizer sbsr = romHandler.generationOfPokemon() == 1
                ? new Gen1SpeciesBaseStatRandomizer(romHandler, new Settings(), RND)
                : new SpeciesBaseStatRandomizer(romHandler, new Settings(), RND);
        sbsr.shuffleSpeciesStats();

        List<List<Integer>> statsAfter = romHandler.getSpeciesSet().stream().map(this::getSortedStats).toList();

        assertEquals(statsBefore, statsAfter);
    }

    private List<Integer> getSortedStats(Species pk) {
        BaseStats bs = pk.getBaseStats();
        List<Integer> stats;
        if (bs instanceof Gen1BaseStats g1bs) {
            stats = new ArrayList<>(List.of(
                    bs.getHp(), bs.getAttack(), bs.getDefense(), bs.getSpeed(), g1bs.getSpecial()
            ));
        } else if (bs instanceof ShedinjaBaseStats) {
            stats = new ArrayList<>(List.of(
                    bs.getAttack(), bs.getDefense(), bs.getSpatk(), bs.getSpdef(), bs.getSpeed()
            ));
        } else {
            stats = new ArrayList<>(List.of(
                    bs.getHp(), bs.getAttack(), bs.getDefense(), bs.getSpatk(), bs.getSpdef(), bs.getSpeed()
            ));
        }
        Collections.sort(stats);
        return stats;
    }

    // If it's this rare, literally no one will notice. And getting it to never ever fail would be hard.
    private static final int ALLOWED_STAT_LOWER_THAN_PREVOS_FAILS = 3;

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeStats_FollowEvolutions_AssignStatsRandomly_EachStatIsAtLeastThatOfPrevo(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBaseStatsFollowEvolutions(true);
        s.setAssignEvoStatsRandomly(true);
        SpeciesBaseStatRandomizer sbsr = romHandler.generationOfPokemon() == 1
                ? new Gen1SpeciesBaseStatRandomizer(romHandler, s, RND)
                : new SpeciesBaseStatRandomizer(romHandler, s, RND);
        sbsr.randomizeSpeciesStats();

        List<Species> fails = new ArrayList<>();
        EvolvedSpeciesAction evolvedAction = (from, to, _) -> {
            if (to.getBST(false) < from.getBST(false)) {
                System.out.println("ignoring " + from.getFullName() + "->" + to.getFullName()
                        + " since BST is lower after evolution.");
            }
            BaseStats fromBS = from.getBaseStats();
            BaseStats toBS = to.getBaseStats();
            System.out.println(from.getNumberAndFullName() + ": " + fromBS);
            System.out.println(to.getNumberAndFullName() + ": " + toBS);
            if (!(toBS instanceof ShedinjaBaseStats)) {
                if (fromBS.getHp() > toBS.getHp()) fails.add(to);
            }
            if (fromBS.getAttack() > toBS.getAttack()) fails.add(to);
            if (fromBS.getDefense() > toBS.getDefense()) fails.add(to);
            if (fromBS.getSpeed() > toBS.getSpeed()) fails.add(to);
            if (toBS instanceof Gen1BaseStats) {
                if (((Gen1BaseStats) fromBS).getSpecial() > ((Gen1BaseStats) toBS).getSpecial()) fails.add(to);
            } else {
                if (fromBS.getSpatk() > toBS.getSpatk()) fails.add(to);
                if (fromBS.getSpdef() > toBS.getSpdef()) fails.add(to);
            }
        };

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(null, evolvedAction).build();
        new CopyUpEvolutionsHelper(romHandler.getSpeciesSet()).apply(options);

        System.out.println("Failed " + fails.size() + " times.");
        System.out.println(fails.stream().map(Species::getNumberAndFullName).toList());
        if (fails.size() <= ALLOWED_STAT_LOWER_THAN_PREVOS_FAILS) {
            System.out.println("This is within acceptable expectations.");
        } else {
            fail("This is too common. Do something about it.");
        }
    }
}
