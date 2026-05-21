package com.uprfvx.romio.services;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CfruDpeItemPoolPolicyTest {

    @Test
    public void formChangeItemsAreBadOnlyWhenBanBadItemsIsEnabled() {
        assertBadOnly(item(ItemIDs.flamePlate, "Flame Plate"));
        assertBadOnly(item(ItemIDs.burnDrive, "Burn Drive"));
        assertBadOnly(item(ItemIDs.bugMemory, "Bug Memory"));
        assertBadOnly(item(ItemIDs.redNectar, "Red Nectar"));
        assertBadOnly(item(ItemIDs.revealGlass, "Reveal Glass"));
        assertBadOnly(item(ItemIDs.dNASplicersFuse, "DNA Splicers"));
    }

    @Test
    public void fossilsAreBannedFromNormalItemPools() {
        assertBanned(item(ItemIDs.helixFossil, "Helix Fossil"));
        assertBanned(item(ItemIDs.rootFossil, "Root Fossil"));
        assertBanned(item(ItemIDs.fossilizedBird, "Fossilized Bird"));
        assertBanned(item(9000, "Plume Fossil"));
        assertBanned(item(9001, "Old Amber"));
    }

    @Test
    public void clearlyAllowedPolicyItemsAreNotNewlyBanned() {
        assertAllowed(item(ItemIDs.potion, "Potion"));
        assertAllowed(item(ItemIDs.antidote, "Antidote"));
        assertAllowed(item(ItemIDs.pokeBall, "Poke Ball"));
        assertAllowed(item(ItemIDs.masterBall, "Master Ball"));
        assertAllowed(item(ItemIDs.escapeRope, "Escape Rope"));
        assertAllowed(item(ItemIDs.rareCandy, "Rare Candy"));
        assertAllowed(item(ItemIDs.hpUp, "HP Up"));
        assertAllowed(item(ItemIDs.nugget, "Nugget"));
        assertAllowed(item(ItemIDs.blueShard, "Blue Shard"));
        assertAllowed(item(ItemIDs.leftovers, "Leftovers"));
    }

    @Test
    public void usefulBerriesShardsAndSpecificHeldItemsOverrideLegacyBadFlags() {
        assertAllowedWhenBanBad(item(ItemIDs.oranBerry, "Oran Berry"));
        assertAllowedWhenBanBad(item(ItemIDs.lumBerry, "Lum Berry"));
        assertAllowedWhenBanBad(item(ItemIDs.blueShard, "Blue Shard"));
        assertAllowedWhenBanBad(item(ItemIDs.lightBall, "Light Ball"));
        assertAllowedWhenBanBad(item(ItemIDs.thickClub, "Thick Club"));
    }

    @Test
    public void pokeBallsAreRecognizedAsAllowedRewardItems() {
        assertTrue(CfruDpeItemPoolPolicy.isPokeBallItem(item(ItemIDs.pokeBall, "Poke Ball")));
        assertTrue(CfruDpeItemPoolPolicy.isPokeBallItem(item(ItemIDs.masterBall, "Master Ball")));
        assertTrue(CfruDpeItemPoolPolicy.isPokeBallItem(item(9000, "Premier Ball")));
        assertFalse(CfruDpeItemPoolPolicy.isPokeBallItem(item(ItemIDs.potion, "Potion")));
    }

    @Test
    public void unknownItemsAreNotSilentlyAddedToNewBanOrAllowCategories() {
        Item unknown = item(9999, "Future Custom Item");

        assertFalse(CfruDpeItemPoolPolicy.isBannedFromNormalItemPools(unknown));
        assertFalse(CfruDpeItemPoolPolicy.isBadWhenBanBadItems(unknown));
    }

    private static void assertBadOnly(Item item) {
        assertFalse(CfruDpeItemPoolPolicy.isBannedFromNormalItemPools(item), item + " should stay allowed");
        assertTrue(CfruDpeItemPoolPolicy.isBadWhenBanBadItems(item), item + " should be Ban-Bad filtered");
    }

    private static void assertBanned(Item item) {
        assertTrue(CfruDpeItemPoolPolicy.isBannedFromNormalItemPools(item), item + " should be banned");
        assertFalse(CfruDpeItemPoolPolicy.isBadWhenBanBadItems(item), item + " should not need Ban-Bad filtering");
    }

    private static void assertAllowed(Item item) {
        assertFalse(CfruDpeItemPoolPolicy.isBannedFromNormalItemPools(item), item + " should stay allowed");
        assertFalse(CfruDpeItemPoolPolicy.isBadWhenBanBadItems(item), item + " should not be Ban-Bad filtered");
    }

    private static void assertAllowedWhenBanBad(Item item) {
        assertAllowed(item);
        assertTrue(CfruDpeItemPoolPolicy.isAllowedWhenBanBadItems(item), item + " should override legacy bad flags");
    }

    private static Item item(int id, String name) {
        return new Item(id, name);
    }
}
