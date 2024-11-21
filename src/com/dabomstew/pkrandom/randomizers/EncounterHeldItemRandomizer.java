package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.ItemList;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Random;

/**
 * A {@link Randomizer} for the held items of wild Pokemon.
 * In some games, these items may be shared between Pokemon
 * in normal and static encounters, thus the separation from
 * {@link WildEncounterRandomizer} which only does normal encounters.
 */
public class EncounterHeldItemRandomizer extends Randomizer {

    public EncounterHeldItemRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeWildHeldItems() {
        boolean banBadItems = settings.isBanBadRandomWildPokemonHeldItems();

        ItemList possibleItems = banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems();
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.getGuaranteedHeldItem() == -1 && pk.getCommonHeldItem() == -1 && pk.getRareHeldItem() == -1
                    && pk.getDarkGrassHeldItem() == -1) {
                // No held items at all, abort
                return;
            }
            boolean canHaveDarkGrass = pk.getDarkGrassHeldItem() != -1;
            if (pk.getGuaranteedHeldItem() != -1) {
                // Guaranteed held items are supported.
                if (pk.getGuaranteedHeldItem() > 0) {
                    // Currently have a guaranteed item
                    double decision = this.random.nextDouble();
                    if (decision < 0.9) {
                        // Stay as guaranteed
                        canHaveDarkGrass = false;
                        pk.setGuaranteedHeldItem(possibleItems.randomItem(this.random));
                    } else {
                        // Change to 25% or 55% chance
                        pk.setGuaranteedHeldItem(0);
                        pk.setCommonHeldItem(possibleItems.randomItem(this.random));
                        pk.setRareHeldItem(possibleItems.randomItem(this.random));
                        while (pk.getRareHeldItem() == pk.getCommonHeldItem()) {
                            pk.setRareHeldItem(possibleItems.randomItem(this.random));
                        }
                    }
                } else {
                    // No guaranteed item atm
                    double decision = this.random.nextDouble();
                    if (decision < 0.5) {
                        // No held item at all
                        pk.setCommonHeldItem(0);
                        pk.setRareHeldItem(0);
                    } else if (decision < 0.65) {
                        // Just a rare item
                        pk.setCommonHeldItem(0);
                        pk.setRareHeldItem(possibleItems.randomItem(this.random));
                    } else if (decision < 0.8) {
                        // Just a common item
                        pk.setCommonHeldItem(possibleItems.randomItem(this.random));
                        pk.setRareHeldItem(0);
                    } else if (decision < 0.95) {
                        // Both a common and rare item
                        pk.setCommonHeldItem(possibleItems.randomItem(this.random));
                        pk.setRareHeldItem(possibleItems.randomItem(this.random));
                        while (pk.getRareHeldItem() == pk.getCommonHeldItem()) {
                            pk.setRareHeldItem(possibleItems.randomItem(this.random));
                        }
                    } else {
                        // Guaranteed item
                        canHaveDarkGrass = false;
                        pk.setGuaranteedHeldItem(possibleItems.randomItem(this.random));
                        pk.setCommonHeldItem(0);
                        pk.setRareHeldItem(0);
                    }
                }
            } else {
                // Code for no guaranteed items
                double decision = this.random.nextDouble();
                if (decision < 0.5) {
                    // No held item at all
                    pk.setCommonHeldItem(0);
                    pk.setRareHeldItem(0);
                } else if (decision < 0.65) {
                    // Just a rare item
                    pk.setCommonHeldItem(0);
                    pk.setRareHeldItem(possibleItems.randomItem(this.random));
                } else if (decision < 0.8) {
                    // Just a common item
                    pk.setCommonHeldItem(possibleItems.randomItem(this.random));
                    pk.setRareHeldItem(0);
                } else {
                    // Both a common and rare item
                    pk.setCommonHeldItem(possibleItems.randomItem(this.random));
                    pk.setRareHeldItem(possibleItems.randomItem(this.random));
                    while (pk.getRareHeldItem() == pk.getCommonHeldItem()) {
                        pk.setRareHeldItem(possibleItems.randomItem(this.random));
                    }
                }
            }

            if (canHaveDarkGrass) {
                double dgDecision = this.random.nextDouble();
                if (dgDecision < 0.5) {
                    // Yes, dark grass item
                    pk.setDarkGrassHeldItem(possibleItems.randomItem(this.random));
                } else {
                    pk.setDarkGrassHeldItem(0);
                }
            } else if (pk.getDarkGrassHeldItem() != -1) {
                pk.setDarkGrassHeldItem(0);
            }
        }

        changesMade = true;
    }
}
