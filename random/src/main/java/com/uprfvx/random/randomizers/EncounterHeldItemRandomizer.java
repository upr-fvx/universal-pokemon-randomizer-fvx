package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.CfruDpeItemPoolPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A {@link Randomizer} for the held items of wild Pokemon.
 * In some games, these items may be shared between Pokemon
 * in normal and static encounters, thus the separation from
 * {@link WildEncounterRandomizer} which only does normal encounters.
 */
public class EncounterHeldItemRandomizer extends Randomizer {

    public EncounterHeldItemRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeWildHeldItems() {
        boolean banBadItems = settings.isBanBadRandomWildPokemonHeldItems();

        List<Item> possible = filterAllowedMechanicItems(
                banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems());
        possible.removeIf(this::isUnsafeEncounterHeldItem);
        if (possible.isEmpty()) {
            throw new IllegalStateException("No safe encounter held items are available for randomization.");
        }
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (shouldSkipSpecies(pk)) {
                continue;
            }
            if (pk.getGuaranteedHeldItem() == null && pk.getCommonHeldItem() == null && pk.getRareHeldItem() == null
                    && pk.getDarkGrassHeldItem() == null) {
                // No held items at all, skip
                // TODO: does this make sense? do we want pokes to never get held items if they didn't already?
                continue;
            }
            
            double decision = random.nextDouble();
            if (pk.getGuaranteedHeldItem() != null) {
                // Currently have a guaranteed item
                if (decision < 0.9) {
                    setRandomGuaranteedItem(pk, possible);
                } else {
                    setRandomCommonAndRareItems(pk, possible);
                }
            } else {
                // No guaranteed item atm
                if (decision < 0.5) {
                    setNoItem(pk);
                } else if (decision < 0.65) {
                    setRandomOnlyRareItem(pk, possible);
                } else if (decision < 0.8) {
                    setRandomOnlyCommonItem(pk, possible);
                } else if (decision < 0.95 || !romHandler.hasGuaranteedWildHeldItems()) {
                    setRandomCommonAndRareItems(pk, possible);
                } else {
                    setRandomGuaranteedItem(pk, possible);
                }
            }
            
            if (romHandler.hasDarkGrassHeldItems() && pk.getGuaranteedHeldItem() == null) {
                double dgDecision = random.nextDouble();
                if (dgDecision < 0.5) {
                    // Yes, dark grass item
                    pk.setDarkGrassHeldItem(possible.get(random.nextInt(possible.size())));
                } else {
                    pk.setDarkGrassHeldItem(null);
                }
            } 
        }

        changesMade = true;
    }

    private void setNoItem(Species spec) {
        spec.setGuaranteedHeldItem(null);
        spec.setCommonHeldItem(null);
        spec.setRareHeldItem(null);
        spec.setDarkGrassHeldItem(null);
    }

    private void setRandomGuaranteedItem(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(possible.get(random.nextInt(possible.size())));
        pk.setCommonHeldItem(null);
        pk.setRareHeldItem(null);
        pk.setDarkGrassHeldItem(null);
    }

    private void setRandomOnlyCommonItem(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(null);
        pk.setCommonHeldItem(possible.get(random.nextInt(possible.size())));
        pk.setRareHeldItem(null);
    }

    private void setRandomOnlyRareItem(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(null);
        pk.setCommonHeldItem(null);
        pk.setRareHeldItem(possible.get(random.nextInt(possible.size())));
    }

    private void setRandomCommonAndRareItems(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(null);
        pk.setCommonHeldItem(possible.get(random.nextInt(possible.size())));
        pk.setRareHeldItem(possible.get(random.nextInt(possible.size())));
        while (pk.getRareHeldItem().equals(pk.getCommonHeldItem())) {
            pk.setRareHeldItem(possible.get(random.nextInt(possible.size())));
        }
    }

    private boolean shouldSkipSpecies(Species pk) {
        return pk == null || pk.getHp() == 0 || pk.getAttack() == 0
                || pk.getDefense() == 0 || pk.getSpeed() == 0 || pk.getSpatk() == 0 || pk.getSpdef() == 0;
    }

    private boolean isUnsafeEncounterHeldItem(Item item) {
        return item == null || item.getName() == null || item.getName().startsWith("item #")
                || item.isTM() || CfruDpeItemPoolPolicy.isPokeBallItem(item);
    }
}
