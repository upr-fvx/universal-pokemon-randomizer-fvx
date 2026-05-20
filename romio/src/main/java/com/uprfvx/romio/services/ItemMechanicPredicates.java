package com.uprfvx.romio.services;

import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.ItemMechanicCategory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ItemMechanicPredicates {

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
        categories.addAll(CfruDpeItemCategories.categoriesFor(item));
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
}
