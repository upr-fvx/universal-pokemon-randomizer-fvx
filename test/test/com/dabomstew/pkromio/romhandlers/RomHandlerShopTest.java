package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.gamedata.Shop;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public void shopItemsContainNoNulls(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        for (Shop shop : romHandler.getShops()) {
            System.out.println(shop);
            for (Item item : shop.getItems()) {
                assertNotEquals(null, item);
            }
        }
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
    public void noSpecialShopContainsTMs(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        for (Shop shop : romHandler.getShops()) {
            System.out.println(shop);
            if (shop.isSpecialShop()) {
                for (Item item : shop.getItems()) {
                    assertFalse(item.isTM());
                }
            }
        }
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
    public void canAddOneItemToEveryShop(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasShopSupport());
        assumeTrue(romHandler.canChangeShopSizes());

        List<Item> allItems = romHandler.getItems();

        List<Shop> shops = romHandler.getShops();
        shops.forEach(s -> s.getItems().add(allItems.get(1)));
        romHandler.setShops(deepCopy(shops));

        assertEquals(shops, romHandler.getShops());
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

    private List<Shop> deepCopy(List<Shop> original) {
        List<Shop> copy = new ArrayList<>(original.size());
        for (Shop shop : original) {
            copy.add(new Shop(shop));
        }
        return copy;
    }


    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopPricesSizeEqualsItemsSize(String romName) {
        loadROM(romName);
        assertEquals(romHandler.getItems().size(), romHandler.getShopPrices().size());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopPricesAreZeroForNullItems(String romName) {
        loadROM(romName);
        List<Item> items = romHandler.getItems();
        List<Integer> prices = romHandler.getShopPrices();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == null) {
                System.out.println("null item #" + i);
                assertEquals(0, prices.get(i));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopPricesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Integer> before = romHandler.getShopPrices();
        List<Item> items = romHandler.getItems();
        for (int i = 0; i < before.size(); i++) {
            if (items.get(i) == null) {
                continue;
            }
            System.out.println(items.get(i) + ": " + before.get(i) + "¥");
        }

        romHandler.setShopPrices(before);
        List<Integer> after = romHandler.getShopPrices();

        System.out.println("\nFaulty prices: ");
        List<Integer> faulty = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == null) {
                continue;
            }
            if (!Objects.equals(before.get(i), after.get(i))) {
                faulty.add(i);
                System.out.println(items.get(i) + ": " + before.get(i) + "¥ -> " + after.get(i) + "¥");
            }
        }
        assertTrue(faulty.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void balanceShopPricesDoesNotCauseFailure(String romName) {
        loadROM(romName);
        romHandler.setBalancedShopPrices();
    }

}
