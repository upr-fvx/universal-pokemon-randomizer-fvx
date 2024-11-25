package test.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.Item;
import com.dabomstew.pkrandom.gamedata.Shop;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ItemRandomizerTest extends RandomizerTest{

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shuffleShopItemsRetainsSameItems(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.hasShopSupport());

        Map<Item, Integer> itemCountsBefore = countItems(romHandler.getShopItems());
        System.out.println(itemCountsBefore);
        new ItemRandomizer(romHandler, new Settings(), RND).shuffleShopItems();
        Map<Item, Integer> itemCountsAfter = countItems(romHandler.getShopItems());
        System.out.println(itemCountsAfter);

        assertEquals(itemCountsBefore, itemCountsAfter);
    }

    private Map<Item, Integer> countItems(Map<Integer, Shop> shops) {
        Map<Item, Integer> counts = new HashMap<>();
        for (Shop shop : shops.values()) {
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
        romHandler.getShopItems().values().forEach(shop -> before.addAll(shop.getItems()));
        System.out.println(before);

        new ItemRandomizer(romHandler, new Settings(), RND).shuffleShopItems();

        List<Item> after = new ArrayList<>();
        romHandler.getShopItems().values().forEach(shop -> after.addAll(shop.getItems()));
        System.out.println(after);
        assertNotEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanBanBadItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBanBadRandomShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> nonBad = romHandler.getNonBadItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertTrue(nonBad.contains(item));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanBadRegularShopItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBanRegularShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> regularShop = romHandler.getRegularShopItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertFalse(regularShop.contains(item));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanBanOverpoweredShopItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBanOPShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> opShop = romHandler.getOPShopItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertFalse(opShop.contains(item));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeShopItemsCanGuaranteeEvolutionAndXItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        activateRomHandler(romName);

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

        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
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
}
