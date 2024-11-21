package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MiscTweakRandomizer extends Randomizer {

    private static final int MAX_CATCHING_TUTORIAL_TRIES = 1000;

    public MiscTweakRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void applyMiscTweaks() {
        int selectedMiscTweaks = settings.getCurrentMiscTweaks();

        int codeTweaksAvailable = romHandler.miscTweaksAvailable();
        List<MiscTweak> tweaksToApply = new ArrayList<>();

        for (MiscTweak mt : MiscTweak.allTweaks) {
            if ((codeTweaksAvailable & mt.getValue()) > 0 && (selectedMiscTweaks & mt.getValue()) > 0) {
                tweaksToApply.add(mt);
            }
        }

        // Sort so priority is respected in tweak ordering.
        Collections.sort(tweaksToApply);

        // Now apply in order.
        for (MiscTweak mt : tweaksToApply) {
            if (mt == MiscTweak.RANDOMIZE_CATCHING_TUTORIAL) {
                randomizeCatchingTutorial();
            } else if (mt == MiscTweak.RANDOMIZE_PC_POTION) {
                romHandler.setPCPotionItem(romHandler.getNonBadItems().randomNonTM(random));
            } else {
                romHandler.applyMiscTweak(mt);
            }
        }

        changesMade = true;
    }

    private void randomizeCatchingTutorial() {
        boolean success = false;
        int tries = 0;
        while (!success && tries < MAX_CATCHING_TUTORIAL_TRIES) {
            success = romHandler.setCatchingTutorial(rSpecService.randomSpecies(random), rSpecService.randomSpecies(random));
            tries++;
        }
        if (tries == MAX_CATCHING_TUTORIAL_TRIES) {
            throw new RandomizationException("Could not randomize catching tutorial in " + tries + " tries.");
        }
    }

}
