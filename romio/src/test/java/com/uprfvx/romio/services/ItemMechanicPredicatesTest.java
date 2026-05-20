package com.uprfvx.romio.services;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.ItemMechanicCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemMechanicPredicatesTest {

    private static final int CFRU_DPE_ULTRANECROZIUM_Z = 0x214;
    private static final int CFRU_DPE_NORMALIUM_Z = 0x244;

    @Test
    public void megaStonesAndAccessoriesAreMegaMechanicItems() {
        Item venusaurite = item(ItemIDs.venusaurite, "Venusaurite");
        Item megaRing = item(ItemIDs.megaRing, "Mega Ring");

        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(venusaurite));
        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(megaRing));
        assertFalse(ItemMechanicPredicates.isItemAllowed(venusaurite, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(megaRing, ItemMechanicExclusionOptions.defaults()));
        assertTrue(ItemMechanicPredicates.isItemAllowed(venusaurite,
                new ItemMechanicExclusionOptions(true, false, false)));
    }

    @Test
    public void zCrystalsAndAccessoriesAreZMechanicItems() {
        Item normaliumZ = item(ItemIDs.normaliumZHeld, "Normalium Z");
        Item ultranecroziumZ = item(ItemIDs.ultranecroziumZBag, "Ultranecrozium Z");
        Item cfruDpeNecroziumZ = item(CFRU_DPE_ULTRANECROZIUM_Z, "Necrozium Z");
        Item cfruDpeNormaliumZ = item(CFRU_DPE_NORMALIUM_Z, "Normalium Z");
        Item zRing = item(ItemIDs.zRing, "Z-Ring");

        assertTrue(ItemMechanicPredicates.isZMechanicItem(normaliumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(ultranecroziumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(cfruDpeNecroziumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(cfruDpeNormaliumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(zRing));
        assertFalse(ItemMechanicPredicates.isItemAllowed(normaliumZ, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(ultranecroziumZ, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cfruDpeNecroziumZ,
                ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cfruDpeNormaliumZ,
                ItemMechanicExclusionOptions.defaults()));
        assertTrue(ItemMechanicPredicates.isItemAllowed(ultranecroziumZ,
                new ItemMechanicExclusionOptions(false, true, false)));
        assertTrue(ItemMechanicPredicates.isItemAllowed(cfruDpeNecroziumZ,
                new ItemMechanicExclusionOptions(false, true, false)));
    }

    @Test
    public void dynamaxAndGigantamaxItemsUseTheirOwnMechanicCategory() {
        Item dynamaxCandy = item(ItemIDs.dynamaxCandy, "Dynamax Candy");
        Item maxMushrooms = item(ItemIDs.maxMushrooms, "Max Mushrooms");

        assertTrue(ItemMechanicPredicates.isDynamaxGigantamaxItem(dynamaxCandy));
        assertTrue(ItemMechanicPredicates.isDynamaxGigantamaxItem(maxMushrooms));
        assertFalse(ItemMechanicPredicates.isItemAllowed(dynamaxCandy, ItemMechanicExclusionOptions.defaults()));
        assertTrue(ItemMechanicPredicates.isItemAllowed(maxMushrooms,
                new ItemMechanicExclusionOptions(false, false, true)));
    }

    @Test
    public void explicitMetadataCategoryWorksForSyntheticItems() {
        Item syntheticMegaStone = item(5000, "Synthetic Mega Stone");
        syntheticMegaStone.addMechanicCategory(ItemMechanicCategory.MEGA_STONE);
        Item neutralItem = item(ItemIDs.rareCandy, "Rare Candy");

        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(syntheticMegaStone));
        assertFalse(ItemMechanicPredicates.isItemAllowed(syntheticMegaStone, ItemMechanicExclusionOptions.defaults()));
        assertTrue(ItemMechanicPredicates.isItemAllowed(neutralItem, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(null, ItemMechanicExclusionOptions.allowAllMechanicItems()));
    }

    private static Item item(int id, String name) {
        return new Item(id, name);
    }
}
