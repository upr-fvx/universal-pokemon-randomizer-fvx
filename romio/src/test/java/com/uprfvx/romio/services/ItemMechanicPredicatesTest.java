package com.uprfvx.romio.services;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.ItemMechanicCategory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemMechanicPredicatesTest {

    private static final int CFRU_DPE_ULTRANECROZIUM_Z = 0x214;
    private static final int CFRU_DPE_VENUSAURITE = 0x215;
    private static final int CFRU_DPE_NORMALIUM_Z = 0x244;
    private static final int CFRU_DPE_SNORLIUM_Z = 0x263;

    @Test
    public void megaStonesAndAccessoriesAreMegaMechanicItems() {
        Item venusaurite = item(ItemIDs.venusaurite, "Venusaurite");
        Item cfruDpeVenusaurite = item(CFRU_DPE_VENUSAURITE, "Venusaurite");
        Item pidgeotite = item(6000, "Pidgeotite");
        Item cameruptite = item(6001, "Cameruptite");
        Item megaRing = item(ItemIDs.megaRing, "Mega Ring");

        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(venusaurite));
        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(cfruDpeVenusaurite));
        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(pidgeotite));
        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(cameruptite));
        assertTrue(ItemMechanicPredicates.isMegaMechanicItem(megaRing));
        assertFalse(ItemMechanicPredicates.isItemAllowed(venusaurite, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cfruDpeVenusaurite,
                ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(pidgeotite, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cameruptite, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(megaRing, ItemMechanicExclusionOptions.defaults()));
        assertTrue(ItemMechanicPredicates.isItemAllowed(venusaurite,
                new ItemMechanicExclusionOptions(true, false, false)));
        assertTrue(ItemMechanicPredicates.isItemAllowed(cfruDpeVenusaurite,
                new ItemMechanicExclusionOptions(true, false, false)));
        assertTrue(ItemMechanicPredicates.isItemAllowed(pidgeotite,
                new ItemMechanicExclusionOptions(true, false, false)));
        assertTrue(ItemMechanicPredicates.isItemAllowed(cameruptite,
                new ItemMechanicExclusionOptions(true, false, false)));
    }

    @Test
    public void megaStoneNamesAreRecognizedWhenIdsAreNotCanonical() {
        List<Item> megaStones = List.of(
                item(6100, "Charizardite X"),
                item(6101, "Charizardite Y"),
                item(6102, "Mewtwonite X"),
                item(6103, "Mewtwonite Y"),
                item(6104, "Pidgeotite"),
                item(6105, "Cameruptite"),
                item(6106, "Diancite"),
                item(6107, "Beedrillite"),
                item(6108, "Sceptilite"),
                item(6109, "Sablenite")
        );
        Item eviolite = item(7000, "Eviolite");
        Item megaDrain = item(7001, "Mega Drain");

        for (Item megaStone : megaStones) {
            assertTrue(ItemMechanicPredicates.isMegaMechanicItem(megaStone),
                    megaStone + " should be Mega-related");
            assertFalse(ItemMechanicPredicates.isItemAllowed(megaStone, ItemMechanicExclusionOptions.defaults()));
            assertTrue(ItemMechanicPredicates.isItemAllowed(megaStone,
                    new ItemMechanicExclusionOptions(true, false, false)));
        }
        assertFalse(ItemMechanicPredicates.isMegaMechanicItem(eviolite));
        assertFalse(ItemMechanicPredicates.isMegaMechanicItem(megaDrain));
    }

    @Test
    public void zCrystalsAndAccessoriesAreZMechanicItems() {
        Item normaliumZ = item(ItemIDs.normaliumZHeld, "Normalium Z");
        Item ultranecroziumZ = item(ItemIDs.ultranecroziumZBag, "Ultranecrozium Z");
        Item cfruDpeNecroziumZ = item(CFRU_DPE_ULTRANECROZIUM_Z, "Necrozium Z");
        Item cfruDpeNormaliumZ = item(CFRU_DPE_NORMALIUM_Z, "Normalium Z");
        Item cfruDpeSnorliumZ = item(CFRU_DPE_SNORLIUM_Z, "Snorlium Z");
        Item zRing = item(ItemIDs.zRing, "Z-Ring");

        assertTrue(ItemMechanicPredicates.isZMechanicItem(normaliumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(ultranecroziumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(cfruDpeNecroziumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(cfruDpeNormaliumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(cfruDpeSnorliumZ));
        assertTrue(ItemMechanicPredicates.isZMechanicItem(zRing));
        assertFalse(ItemMechanicPredicates.isItemAllowed(normaliumZ, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(ultranecroziumZ, ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cfruDpeNecroziumZ,
                ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cfruDpeNormaliumZ,
                ItemMechanicExclusionOptions.defaults()));
        assertFalse(ItemMechanicPredicates.isItemAllowed(cfruDpeSnorliumZ,
                ItemMechanicExclusionOptions.defaults()));
        assertTrue(ItemMechanicPredicates.isItemAllowed(ultranecroziumZ,
                new ItemMechanicExclusionOptions(false, true, false)));
        assertTrue(ItemMechanicPredicates.isItemAllowed(cfruDpeNecroziumZ,
                new ItemMechanicExclusionOptions(false, true, false)));
    }

    @Test
    public void signatureZCrystalNamesAreRecognizedWhenIdsAreNotCanonical() {
        List<Item> signatureZCrystals = List.of(
                item(6000, "Pikanium Z"),
                item(6001, "Pikashunium Z"),
                item(6002, "Eevium Z"),
                item(6003, "Mewnium Z"),
                item(6004, "Decidium Z"),
                item(6005, "Incinium Z"),
                item(6006, "Primarium Z"),
                item(6007, "Tapunium Z"),
                item(6008, "Marshadium Z"),
                item(6009, "Kommonium Z"),
                item(6010, "Solganium Z"),
                item(6011, "Lunalium Z"),
                item(6012, "Lycanium Z"),
                item(6013, "Mimikium Z"),
                item(6014, "Snorlium Z"),
                item(6015, "Necrozium Z"),
                item(6016, "Ultranecrozium Z")
        );
        Item rareCandy = item(ItemIDs.rareCandy, "Rare Candy");
        Item zoomLens = item(ItemIDs.zoomLens, "Zoom Lens");
        Item megaDrain = item(7001, "Mega Drain");

        for (Item zCrystal : signatureZCrystals) {
            assertTrue(ItemMechanicPredicates.isZMechanicItem(zCrystal), zCrystal + " should be Z-related");
            assertFalse(ItemMechanicPredicates.isItemAllowed(zCrystal, ItemMechanicExclusionOptions.defaults()));
            assertTrue(ItemMechanicPredicates.isItemAllowed(zCrystal,
                    new ItemMechanicExclusionOptions(false, true, false)));
        }
        assertFalse(ItemMechanicPredicates.isZMechanicItem(rareCandy));
        assertFalse(ItemMechanicPredicates.isZMechanicItem(zoomLens));
        assertFalse(ItemMechanicPredicates.isMegaMechanicItem(megaDrain));
        assertFalse(ItemMechanicPredicates.isZMechanicItem(megaDrain));
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

    @Test
    public void nonRomMechanicItemAuditHasNoSuspiciousUnclassifiedModeledExamples() {
        List<Item> modeledItems = List.of(
                item(ItemIDs.venusaurite, "Venusaurite"),
                item(CFRU_DPE_VENUSAURITE, "Venusaurite"),
                item(7002, "Pidgeotite"),
                item(7003, "Cameruptite"),
                item(ItemIDs.megaRing, "Mega Ring"),
                item(CFRU_DPE_ULTRANECROZIUM_Z, "Necrozium Z"),
                item(CFRU_DPE_SNORLIUM_Z, "Snorlium Z"),
                item(7000, "Pikanium Z"),
                item(ItemIDs.dynamaxCandy, "Dynamax Candy"),
                item(ItemIDs.maxMushrooms, "Max Mushrooms"),
                item(ItemIDs.rareCandy, "Rare Candy"),
                item(ItemIDs.zoomLens, "Zoom Lens")
        );

        MechanicItemAudit audit = audit(modeledItems);

        assertEquals(modeledItems.size(), audit.totalItems());
        assertEquals(5, audit.count(ItemMechanicCategory.MEGA_STONE)
                + audit.count(ItemMechanicCategory.MEGA_ACCESSORY));
        assertEquals(3, audit.count(ItemMechanicCategory.Z_CRYSTAL));
        assertEquals(2, audit.count(ItemMechanicCategory.DYNAMAX_GIGANTAMAX));
        assertTrue(audit.suspiciousUnclassifiedItems().isEmpty(), audit.suspiciousUnclassifiedItems().toString());
    }

    private static Item item(int id, String name) {
        return new Item(id, name);
    }

    private static MechanicItemAudit audit(Collection<Item> items) {
        Map<ItemMechanicCategory, Integer> counts = new EnumMap<>(ItemMechanicCategory.class);
        List<Item> suspiciousUnclassifiedItems = new ArrayList<>();
        for (Item item : items) {
            var categories = ItemMechanicPredicates.categoriesFor(item);
            for (ItemMechanicCategory category : categories) {
                counts.merge(category, 1, Integer::sum);
            }
            if (categories.isEmpty() && looksMechanicRelated(item)) {
                suspiciousUnclassifiedItems.add(item);
            }
        }
        return new MechanicItemAudit(items.size(), counts, suspiciousUnclassifiedItems);
    }

    private static boolean looksMechanicRelated(Item item) {
        String normalized = item.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return normalized.endsWith("z")
                || normalized.contains("zpower")
                || normalized.contains("zmove")
                || normalized.contains("megaring")
                || normalized.contains("dynamax")
                || normalized.contains("gigantamax");
    }

    private record MechanicItemAudit(int totalItems, Map<ItemMechanicCategory, Integer> counts,
                                     List<Item> suspiciousUnclassifiedItems) {
        private int count(ItemMechanicCategory category) {
            return counts.getOrDefault(category, 0);
        }
    }
}
