package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.gamedata.Shop;
import com.dabomstew.pkromio.romhandlers.Gen2RomHandler;
import com.dabomstew.pkromio.romhandlers.Gen6RomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        assumeTrue(getGenerationNumberOf(romName) == 2 || getGenerationNumberOf(romName) == 6);
        loadROM(romName);
        List<Integer> prices;
        if (romHandler instanceof Gen2RomHandler) {
            prices = ((Gen2RomHandler) romHandler).getShopPrices();
        } else if (romHandler instanceof Gen6RomHandler) {
            prices = ((Gen6RomHandler) romHandler).getShopPrices();
        } else {
            throw new IllegalStateException("can't get shop prices, unexpected ROM handler");
        }
        List<Item> items = romHandler.getItems();
        if (prices.size() != items.size()) {
            throw new IllegalStateException();
        }
        for (int i = 1; i < prices.size(); i++) {
            System.out.println(items.get(i).getName() + ": " + prices.get(i) + "¥");
        }
    }

}
