package com.uprfvx.romio.services;

public class ItemMechanicExclusionOptions {

    private final boolean includeMegaMechanicItems;
    private final boolean includeZCrystalItems;
    private final boolean includeDynamaxGigantamaxItems;

    public ItemMechanicExclusionOptions(boolean includeMegaMechanicItems, boolean includeZCrystalItems,
                                        boolean includeDynamaxGigantamaxItems) {
        this.includeMegaMechanicItems = includeMegaMechanicItems;
        this.includeZCrystalItems = includeZCrystalItems;
        this.includeDynamaxGigantamaxItems = includeDynamaxGigantamaxItems;
    }

    public static ItemMechanicExclusionOptions defaults() {
        return new ItemMechanicExclusionOptions(false, false, false);
    }

    public static ItemMechanicExclusionOptions allowAllMechanicItems() {
        return new ItemMechanicExclusionOptions(true, true, true);
    }

    public boolean isIncludeMegaMechanicItems() {
        return includeMegaMechanicItems;
    }

    public boolean isIncludeZCrystalItems() {
        return includeZCrystalItems;
    }

    public boolean isIncludeDynamaxGigantamaxItems() {
        return includeDynamaxGigantamaxItems;
    }
}
