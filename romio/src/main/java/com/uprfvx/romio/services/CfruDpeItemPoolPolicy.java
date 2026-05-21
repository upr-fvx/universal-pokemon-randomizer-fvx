package com.uprfvx.romio.services;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;

import java.util.Locale;
import java.util.Set;

public final class CfruDpeItemPoolPolicy {

    private static final Set<Integer> FOSSIL_IDS = Set.of(
            ItemIDs.rootFossil,
            ItemIDs.clawFossil,
            ItemIDs.helixFossil,
            ItemIDs.domeFossil,
            ItemIDs.oldAmber,
            ItemIDs.armorFossil,
            ItemIDs.skullFossil,
            ItemIDs.coverFossil,
            ItemIDs.plumeFossil,
            ItemIDs.jawFossil,
            ItemIDs.sailFossil,
            ItemIDs.fossilizedBird,
            ItemIDs.fossilizedFish,
            ItemIDs.fossilizedDrake,
            ItemIDs.fossilizedDino
    );
    private static final Set<String> FOSSIL_NAMES = Set.of(
            "rootfossil",
            "clawfossil",
            "helixfossil",
            "domefossil",
            "oldamber",
            "armorfossil",
            "skullfossil",
            "coverfossil",
            "plumefossil",
            "jawfossil",
            "sailfossil",
            "fossilizedbird",
            "fossilizedfish",
            "fossilizeddrake",
            "fossilizeddino"
    );
    private static final Set<String> POKE_BALL_NAMES = Set.of(
            "masterball",
            "ultraball",
            "greatball",
            "pokeball",
            "safariball",
            "netball",
            "diveball",
            "nestball",
            "repeatball",
            "timerball",
            "luxuryball",
            "premierball"
    );
    private static final Set<Integer> USEFUL_BERRY_IDS = Set.of(
            ItemIDs.cheriBerry,
            ItemIDs.chestoBerry,
            ItemIDs.pechaBerry,
            ItemIDs.rawstBerry,
            ItemIDs.aspearBerry,
            ItemIDs.leppaBerry,
            ItemIDs.oranBerry,
            ItemIDs.persimBerry,
            ItemIDs.lumBerry,
            ItemIDs.sitrusBerry
    );
    private static final Set<Integer> SHARD_EXCHANGE_IDS = Set.of(
            ItemIDs.redShard,
            ItemIDs.blueShard,
            ItemIDs.yellowShard,
            ItemIDs.greenShard
    );
    private static final Set<Integer> HELD_BATTLE_ITEM_IDS_ALLOWED_BY_POLICY = Set.of(
            ItemIDs.lightBall,
            ItemIDs.soulDew,
            ItemIDs.luckyPunch,
            ItemIDs.metalPowder,
            ItemIDs.thickClub,
            ItemIDs.leek
    );

    private CfruDpeItemPoolPolicy() {
    }

    public static boolean isBannedFromNormalItemPools(Item item) {
        return isFossilItem(item);
    }

    public static boolean isBadWhenBanBadItems(Item item) {
        return isFormChangeItem(item);
    }

    public static boolean isAllowedWhenBanBadItems(Item item) {
        if (item == null) {
            return false;
        }
        return isPokeBallItem(item)
                || USEFUL_BERRY_IDS.contains(item.getId())
                || SHARD_EXCHANGE_IDS.contains(item.getId())
                || HELD_BATTLE_ITEM_IDS_ALLOWED_BY_POLICY.contains(item.getId());
    }

    public static boolean isFormChangeItem(Item item) {
        return CfruDpeItemCategories.isArceusPlate(item)
                || CfruDpeItemCategories.isGenesectDrive(item)
                || CfruDpeItemCategories.isSilvallyMemory(item)
                || CfruDpeItemCategories.isNectarOrFormChangeItem(item);
    }

    public static boolean isFossilItem(Item item) {
        if (item == null) {
            return false;
        }
        return FOSSIL_IDS.contains(item.getId()) || FOSSIL_NAMES.contains(normalizedName(item));
    }

    public static boolean isPokeBallItem(Item item) {
        if (item == null) {
            return false;
        }
        return (item.getId() >= ItemIDs.masterBall && item.getId() <= ItemIDs.premierBall)
                || POKE_BALL_NAMES.contains(normalizedName(item));
    }

    private static String normalizedName(Item item) {
        String name = item.getName();
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
