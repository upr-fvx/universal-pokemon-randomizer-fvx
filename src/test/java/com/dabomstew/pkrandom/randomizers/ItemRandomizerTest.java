package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.gamedata.PickupItem;
import com.dabomstew.pkromio.gamedata.Shop;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ItemRandomizerTest extends RandomizerTest{

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shuffleFieldItemsRetainsSameItems(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setFieldItemsMod(Settings.FieldItemsMod.SHUFFLE);

        Map<Item, Integer> itemCountsBefore = countFieldItems(romHandler.getFieldItems());
        System.out.println(itemCountsBefore);
        new ItemRandomizer(romHandler, s, RND).randomizeFieldItems();
        Map<Item, Integer> itemCountsAfter = countFieldItems(romHandler.getFieldItems());
        System.out.println(itemCountsAfter);

        assertEquals(itemCountsBefore, itemCountsAfter);
    }

    private Map<Item, Integer> countFieldItems(List<Item> fieldItems) {
        Map<Item, Integer> counts = new HashMap<>();
        for (Item item : fieldItems) {
            if (!counts.containsKey(item)) {
                counts.put(item, 0);
            }
            counts.put(item, counts.get(item) + 1);
        }
        return counts;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomFieldItemsSetsOnlyAllowedItems(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM);
        new ItemRandomizer(romHandler, s, RND).randomizeFieldItems();

        for (Item item : romHandler.getFieldItems()) {
            System.out.println(item);
            assertTrue(item.isAllowed());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEvenFieldItemsSetsOnlyAllowedItems(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM_EVEN);
        new ItemRandomizer(romHandler, s, RND).randomizeFieldItems();

        for (Item item : romHandler.getFieldItems()) {
            System.out.println(item);
            assertTrue(item.isAllowed());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomFieldItemsCanBanBadItems(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM);
        s.setBanBadRandomFieldItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeFieldItems();

        for (Item item : romHandler.getFieldItems()) {
            System.out.println(item);
            assertFalse(item.isBad());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEvenFieldItemsCanBanBadItems(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM_EVEN);
        s.setBanBadRandomFieldItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeFieldItems();

        for (Item item : romHandler.getFieldItems()) {
            System.out.println(item);
            assertFalse(item.isBad());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomEvenWorks(String romName) {
        // no item appears more than one more time than any other
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM_EVEN);
        new ItemRandomizer(romHandler, s, RND).randomizeFieldItems();

        Map<Item, Integer> counts = countFieldItems(romHandler.getFieldItems());
        System.out.println(counts);
        Set<Item> uniqueItems = romHandler.getMegaStones();
        Set<Integer> filteredValues = counts.entrySet().stream()
                .filter(et -> !et.getKey().isTM())
                .filter(et -> !uniqueItems.contains(et.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        int min = filteredValues.stream().min(Integer::compareTo).orElseThrow(RuntimeException::new);
        int max = filteredValues.stream().max(Integer::compareTo).get();
        System.out.println("min: " + min + ", max: " + max);
        assertTrue(max - min <= 1);
    }

    // TODO: test uniqueNoSellItems (i.e. Mega Stones)

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shuffleShopItemsRetainsSameItems(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        Map<Item, Integer> itemCountsBefore = countItems(romHandler.getShops()
                .stream().filter(Shop::isSpecialShop).collect(Collectors.toList()));
        System.out.println(itemCountsBefore);
        new ItemRandomizer(romHandler, new Settings(), RND).shuffleShopItems();
        Map<Item, Integer> itemCountsAfter = countItems(romHandler.getShops()
                .stream().filter(Shop::isSpecialShop).collect(Collectors.toList()));
        System.out.println(itemCountsAfter);

        assertEquals(itemCountsBefore, itemCountsAfter);
    }

    private Map<Item, Integer> countItems(List<Shop> shops) {
        Map<Item, Integer> counts = new HashMap<>();
        for (Shop shop : shops) {
            for (Item item : shop.getItems()) {
                if (!counts.containsKey(item)) {
                    counts.put(item, 0);
                }
                counts.put(item, counts.get(item) + 1);
            }
        }
        return counts;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shuffleShopItemsCausesDifferentOrder(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        List<Item> before = new ArrayList<>();
        romHandler.getShops().stream().filter(Shop::isSpecialShop).forEach(shop -> before.addAll(shop.getItems()));
        System.out.println(before);

        new ItemRandomizer(romHandler, new Settings(), RND).shuffleShopItems();

        List<Item> after = new ArrayList<>();
        romHandler.getShops().stream().filter(Shop::isSpecialShop).forEach(shop -> after.addAll(shop.getItems()));
        System.out.println(after);
        assertNotEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanBanBadItems(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        Settings s = new Settings();
        s.setBanBadRandomShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        for (Shop shop : romHandler.getShops()) {
            if (!shop.isSpecialShop()) {
                continue;
            }
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertFalse(item.isBad());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanBadRegularShopItems(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        Settings s = new Settings();
        s.setBanRegularShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> regularShop = romHandler.getRegularShopItems();
        for (Shop shop : romHandler.getShops()) {
            if (!shop.isSpecialShop()) {
                continue;
            }
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertFalse(regularShop.contains(item));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanBanOverpoweredShopItems(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        Settings s = new Settings();
        s.setBanOPShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> opShop = romHandler.getOPShopItems();
        for (Shop shop : romHandler.getShops()) {
            if (!shop.isSpecialShop()) {
                continue;
            }
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertFalse(opShop.contains(item));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanGuaranteeEvolutionAndXItems(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        Settings s = new Settings();
        s.setGuaranteeEvolutionItems(true);
        s.setGuaranteeXItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> evoItems = romHandler.getEvolutionItems();
        Map<Item, Boolean> placedEvo = new HashMap<>();
        for (Item evoItem : evoItems) {
            placedEvo.put(evoItem, false);
        }
        Set<Item> xItems = romHandler.getXItems();
        Map<Item, Boolean> placedX = new HashMap<>();
        for (Item xItem : xItems) {
            placedX.put(xItem, false);
        }

        for (Shop shop : romHandler.getShops()) {
            if (!shop.isSpecialShop()) {
                continue;
            }
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                if (evoItems.contains(item)) {
                    placedEvo.put(item, true);
                }
                if (xItems.contains(item)) {
                    placedX.put(item, true);
                }
            }
        }

        System.out.println("Evo: " + placedEvo);
        System.out.println("X: " + placedX);
        int placedEvoCount = (int) placedEvo.values().stream().filter(b -> b).count();
        assertEquals(evoItems.size(), placedEvoCount);
        int placedXCount = (int) placedX.values().stream().filter(b -> b).count();
        assertEquals(xItems.size(), placedXCount);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePickupItemsCanBanBadItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBanBadRandomPickupItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizePickupItems();

        for (PickupItem pi : romHandler.getPickupItems()) {
            System.out.println(pi);
            assertFalse(pi.getItem().isBad());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePickupItemsMayGiveTMsIfTMsAreHoldableAndNotReusable(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);
        assumeTrue(romHandler.canTMsBeHeld());
        assumeTrue(!romHandler.isTMsReusable());

        Settings s = new Settings();
        new ItemRandomizer(romHandler, s, RND).randomizePickupItems();

        boolean tmUsed = false;
        for (PickupItem pi : romHandler.getPickupItems()) {
            System.out.println(pi);
            if (pi.getItem().isTM()) {
                tmUsed = true;
                break;
            }
        }

        assertTrue(tmUsed);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePickupItemsBanTMsIfTMsAreReusable(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);

        if (!romHandler.isTMsReusable()) {
            romHandler.applyMiscTweak(MiscTweak.REUSABLE_TMS);
        }

        new ItemRandomizer(romHandler, new Settings(), RND).randomizePickupItems();

        for (PickupItem pi : romHandler.getPickupItems()) {
            System.out.println(pi);
            assertFalse(pi.getItem().isTM());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizePickupItemsBanTMsIfTMsAreNotHoldable(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);
        assumeTrue(!romHandler.canTMsBeHeld());

        new ItemRandomizer(romHandler, new Settings(), RND).randomizePickupItems();

        for (PickupItem pi : romHandler.getPickupItems()) {
            System.out.println(pi);
            assertFalse(pi.getItem().isTM());
        }
    }
}
