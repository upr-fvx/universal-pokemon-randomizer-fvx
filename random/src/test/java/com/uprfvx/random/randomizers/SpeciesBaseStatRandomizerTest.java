package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

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

        List<Integer> before = getAllBSTsSorted();

        Settings s = new Settings();
        s.setBSTMod(Settings.BSTMod.SHUFFLE);
        new SpeciesBaseStatRandomizer(romHandler, s, RND).randomizeBSTs();

        List<Integer> after = getAllBSTsSorted();

        assertEquals(before, after);
    }

    private List<Integer> getAllBSTsSorted() {
        return romHandler.getSpeciesSetInclFormes().stream()
                .filter(pk -> !pk.isEssentiallyCosmetic())
                .filter(pk -> pk.getMegaEvolutionsTo().isEmpty())
                .map(pk -> pk.getBST(false))
                .sorted()
                .toList();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_FollowEvolutions_Works(String romName) {
        activateRomHandler(romName);
        // TODO
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_SwapLegendaries_Works(String romName) {
        activateRomHandler(romName);
        // TODO
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeBSTs_Shuffle_FollowEvolutionsAndSwapLegendaries_Works(String romName) {
        activateRomHandler(romName);
        // TODO
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
