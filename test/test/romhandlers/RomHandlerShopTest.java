package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.Item;
import com.dabomstew.pkrandom.gamedata.ItemList;
import com.dabomstew.pkrandom.gamedata.Shop;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import com.dabomstew.pkrandom.romhandlers.Gen2RomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerShopTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopItemsAreNotEmpty(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        assertFalse(romHandler.getShopItems().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopItemsDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        System.out.println("Before: " + toMultilineString(shopItems));
        Map<Integer, Shop> before = new HashMap<>(shopItems);
        romHandler.setShopItems(shopItems);
        System.out.println("After: " + toMultilineString(romHandler.getShopItems()));
        assertEquals(before, romHandler.getShopItems());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopItemsCanBeRandomizedAndGetAndSet(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        new ItemRandomizer(romHandler, new Settings(), RND).randomizeShopItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        Map<Integer, Shop> before = new HashMap<>(shopItems);
        romHandler.setShopItems(shopItems);
        assertEquals(before, romHandler.getShopItems());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopsHaveNames(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            assertNotNull(shop.getName());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void mainGameShopsExist(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        boolean hasMainGameShops = false;
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            if (shop.isMainGame()) {
                hasMainGameShops = true;
                break;
            }
        }
        System.out.println(toMultilineString(shopItems));
        assertTrue(hasMainGameShops);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void regularShopItemsIsNotEmpty(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        assertFalse(romHandler.getRegularShopItems().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void opShopItemsIsNotEmpty(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        assertFalse(romHandler.getOPShopItems().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canBadBadItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

        Settings s = new Settings();
        s.setBanBadRandomShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        ItemList nonBad = romHandler.getNonBadItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertTrue(nonBad.isAllowed(item.getId()));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canBadRegularShopItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

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
    public void canBanOverpoweredShopItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

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
    public void canGuaranteeEvolutionItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

        Settings s = new Settings();
        s.setGuaranteeEvolutionItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> evoItems = romHandler.getEvolutionItems();
        Map<Item, Boolean> placed = new HashMap<>();
        for (Item evoItem : evoItems) {
            placed.put(evoItem, false);
        }

        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                if (evoItems.contains(item)) {
                    placed.put(item, true);
                }
            }
        }

        System.out.println(placed);
        int placedCount = (int) placed.values().stream().filter(b -> b).count();
        assertEquals(evoItems.size(), placedCount);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGuaranteeXItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

        Settings s = new Settings();
        s.setGuaranteeXItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        Set<Item> xItems = romHandler.getXItems();
        Map<Item, Boolean> placed = new HashMap<>();
        for (Item xItem : xItems) {
            placed.put(xItem, false);
        }

        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                if (xItems.contains(item)) {
                    placed.put(item, true);
                }
            }
        }

        System.out.println(placed);
        int placedCount = (int) placed.values().stream().filter(b -> b).count();
        assertEquals(xItems.size(), placedCount);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGuaranteeEvolutionAndXItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

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

    private String toMultilineString(Map<Integer, Shop> shops) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<Integer, Shop> entry : shops.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" -> ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canGetPricesWithoutThrowing(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 2);
        loadROM(romName);
        List<Integer> prices = ((Gen2RomHandler) romHandler).getShopPrices();
        List<Item> items = romHandler.getItems();
        if (prices.size() != items.size()) {
            throw new IllegalStateException();
        }
        for (int i = 0; i < prices.size(); i++) {
            System.out.println(items.get(i).getName() + ": " + prices.get(i) + "¥");
        }
    }

}
