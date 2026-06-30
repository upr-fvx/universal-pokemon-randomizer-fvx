package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.uprfvx.random.randomizers.SpeciesBaseStatRandomizer.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpeciesBaseStatRandomizerTest extends RandomizerTest {

    private static final double ACCEPTABLE_BUFFNERF_DEVIANCE = 0.01;

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_RandomBuffNerf_AllBuffsNerfsWithinMaxPercentage(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.RANDOM_BUFF_NERF_PERC);
        s.setBSTBuffNerfMaxPercentage(50);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            // mega evos are exempt
            if (!pk.getMegaEvolutionsTo().isEmpty()) continue;

            assertTrue(pk.getBST(false) >= (int) (pk.getBST(true) * 0.5));
            assertTrue(pk.getBST(false) <= (int) (pk.getBST(true) * 1.5));
        }
    }
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_RandomBuffNerf_FollowFamilyWorks(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.RANDOM_BUFF_NERF_PERC);
        s.setBSTBuffNerfMaxPercentage(50);
        s.setBSTFollowEvolutions(true);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        for (Species evFrom : romHandler.getSpeciesSetInclFormes()) {
            for (Species evTo : evFrom.getEvolvedSpecies(true)) {
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
                .filter(pk -> pk.getMegaEvolutionsTo().isEmpty())
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
                .filter(pk -> pk.getMegaEvolutionsTo().isEmpty());

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

        assertEquals(beforeLegs, afterLegs);
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
                assertEquals(pk.getBaseForme().getBST(false), pk.getBST(false));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_MegaEvolutionsHaveBSTOfBaseFormePlus100(String romName) {
        activateRomHandler(romName);

        new SpeciesBaseStatRandomizer(romHandler, new Settings(), RND).randomizeBSTs();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (!pk.getMegaEvolutionsTo().isEmpty()) {
                assertEquals(pk.getBaseForme().getBST(false) + MEGA_BST_BOOST, pk.getBST(false));
            }
        }
    }

    // TODO: tests for base stat distribution randomization
}
