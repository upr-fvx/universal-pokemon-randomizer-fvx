package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.Item;
import com.dabomstew.pkrandom.gamedata.Shop;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import com.dabomstew.pkrandom.romhandlers.Gen2RomHandler;
import com.dabomstew.pkrandom.romhandlers.Gen6RomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerShopTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopItemsAreNotEmpty(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        assertFalse(romHandler.getShops().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopItemsDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());

        List<Shop> before = new ArrayList<>();
        for (Shop original : romHandler.getShops()) {
            before.add(new Shop(original));
        }
        System.out.println("Before: " + toMultilineString(before));
        romHandler.setShops(romHandler.getShops());

        System.out.println("After: " + toMultilineString(romHandler.getShops()));
        assertEquals(before, romHandler.getShops());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopItemsCanBeRandomizedAndGetAndSet(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        new ItemRandomizer(romHandler, new Settings(), RND).randomizeShopItems();

        List<Shop> before = new ArrayList<>();
        for (Shop original : romHandler.getShops()) {
            before.add(new Shop(original));
        }
        System.out.println("Before: " + toMultilineString(before));
        romHandler.setShops(romHandler.getShops());

        System.out.println("After: " + toMultilineString(romHandler.getShops()));
        assertEquals(before, romHandler.getShops());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopsHaveNames(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        for (Shop shop : romHandler.getShops()) {
            assertNotNull(shop.getName());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void mainGameShopsExist(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        boolean hasMainGameShops = false;
        List<Shop> shops = romHandler.getShops();
        for (Shop shop : shops) {
            if (shop.isMainGame()) {
                hasMainGameShops = true;
                break;
            }
        }
        System.out.println(toMultilineString(shops));
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

    private String toMultilineString(List<Shop> shops) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (int i = 0; i < shops.size(); i++) {
            sb.append(i);
            sb.append(":\t");
            sb.append(shops.get(i));
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
