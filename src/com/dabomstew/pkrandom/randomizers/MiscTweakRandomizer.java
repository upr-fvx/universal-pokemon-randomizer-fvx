package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MiscTweakRandomizer extends Randomizer {

    private static final int MAX_CATCHING_TUTORIAL_TRIES = 1000;

    public MiscTweakRandomizer(RomHandler romHandler, Settings settings, Random random) {
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
                randomizePCPotion();
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

    private void randomizePCPotion() {
        List<Item> possible = new ArrayList<>(romHandler.getNonBadItems());
        Item item;
        do {
            item = possible.get(random.nextInt(possible.size()));
        } while (item.isTM()); // assumes there will always be >0 non-TMs, otherwise this will loop infinitely
        romHandler.setPCPotionItem(item);
    }

}
