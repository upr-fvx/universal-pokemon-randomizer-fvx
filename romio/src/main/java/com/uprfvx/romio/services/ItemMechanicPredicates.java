package com.uprfvx.romio.services;

import com.uprfvx.romio.constants.Gen6Constants;
import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.ItemMechanicCategory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class ItemMechanicPredicates {

    private static final Set<Integer> MEGA_ACCESSORY_IDS = Set.of(
            ItemIDs.megaRing,
            ItemIDs.megaCharm,
            ItemIDs.megaGlove,
            ItemIDs.megaBracelet,
            ItemIDs.megaPendant,
            ItemIDs.megaGlasses,
            ItemIDs.megaAnchor,
            ItemIDs.megaStickpin,
            ItemIDs.megaTiara,
            ItemIDs.megaAnklet,
            ItemIDs.megaCuff,
            ItemIDs.keyStone
    );

    private static final Set<Integer> Z_CRYSTAL_IDS = setupZCrystalIds();
    private static final Set<Integer> Z_ACCESSORY_IDS = Set.of(ItemIDs.zRing, ItemIDs.zPowerRing);

    private static final Set<Integer> DYNAMAX_GIGANTAMAX_IDS = Set.of(
            ItemIDs.wishingStar,
            ItemIDs.dynamaxBand,
            ItemIDs.dynamaxCandy,
            ItemIDs.wishingPiece,
            ItemIDs.wishingChip,
            ItemIDs.maxHoney,
            ItemIDs.maxMushrooms,
            ItemIDs.dyniteOre
    );

    private ItemMechanicPredicates() {
    }

    public static boolean isItemAllowed(Item item, ItemMechanicExclusionOptions options) {
        if (item == null) {
            return false;
        }
        ItemMechanicExclusionOptions effectiveOptions = options == null ? ItemMechanicExclusionOptions.defaults() : options;
        Set<ItemMechanicCategory> categories = categoriesFor(item);
        if (!effectiveOptions.isIncludeMegaMechanicItems()
                && (categories.contains(ItemMechanicCategory.MEGA_STONE)
                || categories.contains(ItemMechanicCategory.MEGA_ACCESSORY))) {
            return false;
        }
        if (!effectiveOptions.isIncludeZCrystalItems()
                && (categories.contains(ItemMechanicCategory.Z_CRYSTAL)
                || categories.contains(ItemMechanicCategory.Z_ACCESSORY))) {
            return false;
        }
        return effectiveOptions.isIncludeDynamaxGigantamaxItems()
                || !categories.contains(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
    }

    public static Set<ItemMechanicCategory> categoriesFor(Item item) {
        if (item == null) {
            return Collections.emptySet();
        }
        EnumSet<ItemMechanicCategory> categories = EnumSet.noneOf(ItemMechanicCategory.class);
        categories.addAll(item.getMechanicCategories());
        int id = item.getId();
        if (Gen6Constants.megaStones.contains(id)) {
            categories.add(ItemMechanicCategory.MEGA_STONE);
        }
        if (MEGA_ACCESSORY_IDS.contains(id)) {
            categories.add(ItemMechanicCategory.MEGA_ACCESSORY);
        }
        if (Z_CRYSTAL_IDS.contains(id)) {
            categories.add(ItemMechanicCategory.Z_CRYSTAL);
        }
        if (Z_ACCESSORY_IDS.contains(id)) {
            categories.add(ItemMechanicCategory.Z_ACCESSORY);
        }
        if (DYNAMAX_GIGANTAMAX_IDS.contains(id)) {
            categories.add(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
        }
        return Collections.unmodifiableSet(categories);
    }

    public static boolean isMegaMechanicItem(Item item) {
        Set<ItemMechanicCategory> categories = categoriesFor(item);
        return categories.contains(ItemMechanicCategory.MEGA_STONE)
                || categories.contains(ItemMechanicCategory.MEGA_ACCESSORY);
    }

    public static boolean isZMechanicItem(Item item) {
        Set<ItemMechanicCategory> categories = categoriesFor(item);
        return categories.contains(ItemMechanicCategory.Z_CRYSTAL)
                || categories.contains(ItemMechanicCategory.Z_ACCESSORY);
    }

    public static boolean isDynamaxGigantamaxItem(Item item) {
        return categoriesFor(item).contains(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
    }

    private static Set<Integer> setupZCrystalIds() {
        Set<Integer> ids = new HashSet<>();
        addBetween(ids, ItemIDs.normaliumZHeld, ItemIDs.pikaniumZHeld);
        addBetween(ids, ItemIDs.decidiumZHeld, ItemIDs.mewniumZHeld);
        addBetween(ids, ItemIDs.normaliumZBag, ItemIDs.pikashuniumZBag);
        addBetween(ids, ItemIDs.solganiumZBag, ItemIDs.kommoniumZBag);
        addBetween(ids, ItemIDs.mimikiumZHeld, ItemIDs.ultranecroziumZHeld);
        ids.add(ItemIDs.pikashuniumZHeld);
        ids.add(ItemIDs.ilimaNormaliumZ);
        return Collections.unmodifiableSet(ids);
    }

    private static void addBetween(Set<Integer> set, int start, int end) {
        for (int i = start; i <= end; i++) {
            set.add(i);
        }
    }
}
