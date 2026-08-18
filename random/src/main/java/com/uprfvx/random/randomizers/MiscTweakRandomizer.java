package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.romio.RomFunctions;
import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.constants.SpeciesIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MiscTweakRandomizer extends Randomizer {

    // TODO: write new test cases for these methods

    private static final int MAX_CATCHING_TUTORIAL_TRIES = 1000;
    private static final int DEFAULT_CALL_RATE = 5;

    public MiscTweakRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void applyMiscTweaks() {

        // TODO: reorder these according to misc tweak priority

        if (settings.getSetting(Settings.Name.TWEAK_USE_SCALED_EXPERIENCE)) {
            romHandler.makeExperienceScaled();
        }
        if (settings.getSetting(Settings.Name.TWEAK_NERF_X_ACCURACY)) {
            romHandler.nerfXAccuracy();
        }
        if (settings.getSetting(Settings.Name.TWEAK_UPDATE_CRIT_RATE)) {
            romHandler.updateCritRate();
        }
        if (settings.getSetting(Settings.Name.TWEAK_FASTEST_TEXT)) {
            romHandler.forceFastestText();
        }
        if (settings.getSetting(Settings.Name.TWEAK_RUN_INDOORS)) {
            romHandler.allowRunningIndoors();
        }
        if (settings.getSetting(Settings.Name.TWEAK_RUN_WITHOUT_RUNNING_SHOES)) {
            romHandler.allowRunningWithoutRunningShoes();
        }
        if (settings.getSetting(Settings.Name.TWEAK_RANDOMIZE_PC_POTION)) {
            randomizePCPotion();
        }
        if (settings.getSetting(Settings.Name.TWEAK_ALLOW_PIKACHU_EVOLUTION)) {
            romHandler.allowPikachuEvolution();
        }
        if (settings.getSetting(Settings.Name.TWEAK_NATIONAL_DEX_AT_START)) {
            romHandler.giveNationalDexAtStart();
        }
        if (settings.getSetting(Settings.Name.TWEAK_FAST_EGG_HATCHING)) {
            romHandler.makeEggsHatchFast();
        }
        if (settings.getSetting(Settings.Name.TWEAK_FORCE_CHALLENGE_MODE)) {
            romHandler.forceChallengeMode();
        }
        if (settings.getSetting(Settings.Name.TWEAK_CAPITAL_CASE_SPECIES_NAMES)) {
            makeSpeciesNamesCapitalCase();
        }
        if (settings.getSetting(Settings.Name.TWEAK_RANDOMIZE_CATCHING_TUTORIAL)) {
            randomizeCatchingTutorial();
        }
        if (settings.getSetting(Settings.Name.TWEAK_BAN_LUCKY_EGG)) {
            banLuckyEgg();
        }
        if (settings.getSetting(Settings.Name.TWEAK_NO_FREE_LUCKY_EGG)) {
            replaceFreeLuckyEgg();
        }
        // TODO BAN_BIG_MONEY_ITEMS
        if (settings.getSetting(Settings.Name.TWEAK_ALL_WILD_POKEMON_CALL_ALLIES)) {
            makeAllSpeciesCallAllies();
        }
        if (settings.getSetting(Settings.Name.TWEAK_BALANCE_FOSSIL_LEVELS)) {
            balanceFossilPokemonLevels();
        }
        if (settings.getSetting(Settings.Name.TWEAK_RETAIN_TEMPORARY_FORMES)) {
            romHandler.forceRetainTemporaryFormes();
        }
        if (settings.getSetting(Settings.Name.TWEAK_FASTER_HP_AND_EXP_BARS)) {
            romHandler.makeHPAndEXPBarsFaster();
        }
        if (settings.getSetting(Settings.Name.TWEAK_FAST_DISTORTION_WORLD)) {
            romHandler.makeDistortionWorldShorter();
        }
        if (settings.getSetting(Settings.Name.TWEAK_UPDATE_ROTOM_TYPING)) {
            updateRotomTyping();
        }
        if (settings.getSetting(Settings.Name.TWEAK_DISABLE_LOW_HP_MUSIC)) {
            romHandler.disableLowHPMusic();
        }
        if (settings.getSetting(Settings.Name.TWEAK_REUSABLE_TMS)) {
            romHandler.makeTMsReusable();
        }
        if (settings.getSetting(Settings.Name.TWEAK_FORGETTABLE_HMS)) {
            romHandler.makeHMsForgettable();
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

    private void makeSpeciesNamesCapitalCase() {
        romHandler.getSpeciesSetInclFormes().forEach(pk -> pk.setName(RomFunctions.capitalCase(pk.getName())));
    }

    private void banLuckyEgg() {
        romHandler.getItems().get(ItemIDs.luckyEgg).setAllowed(false);
    }

    private void replaceFreeLuckyEgg() {
        int mulchID = random.nextInt(ItemIDs.growthMulch, ItemIDs.gooeyMulch + 1);
        Item mulch = romHandler.getItems().get(mulchID);
        romHandler.setFreeLuckyEggItem(mulch);
    }

    private void makeAllSpeciesCallAllies() {
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.getCallRate() <= 0) {
                pk.setCallRate(DEFAULT_CALL_RATE);
            }
        }
    }

    private void balanceFossilPokemonLevels() {
        // TODO: reconsider; should the RomHandler not be responsible for knowing what a balanced level is?
        int balancedLevel;
        if (romHandler.generationOfPokemon() == 3) {
            balancedLevel = 30;
        } else if (romHandler.generationOfPokemon() == 5) {
            balancedLevel = 50;
        } else {
            throw new RuntimeException("unexpected generation: " + romHandler.generationOfPokemon());
        }
        romHandler.setFossilPokemonLevel(balancedLevel);
    }

    private void updateRotomTyping() {
        Species rotom = romHandler.getSpecies().get(SpeciesIDs.rotom);
        Species heat = rotom.getForme(1);
        heat.setSecondaryType(Type.FIRE);
        Species wash = rotom.getForme(2);
        wash.setSecondaryType(Type.WATER);
        Species frost = rotom.getForme(3);
        frost.setSecondaryType(Type.ICE);
        Species fan = rotom.getForme(4);
        fan.setSecondaryType(Type.FLYING);
        Species mow = rotom.getForme(5);
        mow.setSecondaryType(Type.GRASS);
    }

}
