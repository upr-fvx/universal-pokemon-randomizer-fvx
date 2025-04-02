package test.romhandlers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.Gen4Constants;
import com.dabomstew.pkrandom.gamedata.ItemList;
import com.dabomstew.pkrandom.gamedata.Shop;
import com.dabomstew.pkrandom.randomizers.ItemRandomizer;
import com.dabomstew.pkrandom.romhandlers.Gen2RomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        System.out.println("Before: " + toStringWithNames(shopItems));
        Map<Integer, Shop> before = new HashMap<>(shopItems);
        romHandler.setShopItems(shopItems);
        System.out.println("After: " + toStringWithNames(romHandler.getShopItems()));
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
            assertNotNull(shop.name);
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
            if (shop.isMainGame) {
                hasMainGameShops = true;
                break;
            }
        }
        System.out.println(toStringWithNames(shopItems));
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
    public void canBanBadItems(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 2);
        loadROM(romName);

        Settings s = new Settings();
        s.setBanBadRandomShopItems(true);
        new ItemRandomizer(romHandler, s, RND).randomizeShopItems();

        ItemList nonBad = romHandler.getNonBadItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(toStringWithNames(shop));
            for (int itemID : shop.items) {
                assertTrue(nonBad.isAllowed(itemID));
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

        List<Integer> regularShop = romHandler.getRegularShopItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(toStringWithNames(shop));
            for (int itemID : shop.items) {
                assertFalse(regularShop.contains(itemID));
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

        List<Integer> opShop = romHandler.getOPShopItems();
        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(toStringWithNames(shop));
            for (int itemID : shop.items) {
                assertFalse(opShop.contains(itemID));
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

        List<Integer> evoItems = romHandler.getEvolutionItems();
        Map<Integer, Boolean> placed = new HashMap<>();
        for (int evoItem : evoItems) {
            placed.put(evoItem, false);
        }

        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(toStringWithNames(shop));
            for (int itemID : shop.items) {
                if (evoItems.contains(itemID)) {
                    placed.put(itemID, true);
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

        List<Integer> xItems = romHandler.getXItems();
        Map<Integer, Boolean> placed = new HashMap<>();
        for (int xItem : xItems) {
            placed.put(xItem, false);
        }

        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(toStringWithNames(shop));
            for (int itemID : shop.items) {
                if (xItems.contains(itemID)) {
                    placed.put(itemID, true);
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

        List<Integer> evoItems = romHandler.getEvolutionItems();
        Map<Integer, Boolean> placedEvo = new HashMap<>();
        for (int evoItem : evoItems) {
            placedEvo.put(evoItem, false);
        }
        List<Integer> xItems = romHandler.getXItems();
        Map<Integer, Boolean> placedX = new HashMap<>();
        for (int xItem : xItems) {
            placedX.put(xItem, false);
        }

        Map<Integer, Shop> shopItems = romHandler.getShopItems();
        for (Shop shop : shopItems.values()) {
            System.out.println(toStringWithNames(shop));
            for (int itemID : shop.items) {
                if (evoItems.contains(itemID)) {
                    placedEvo.put(itemID, true);
                }
                if (xItems.contains(itemID)) {
                    placedX.put(itemID, true);
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

    private String toStringWithNames(Map<Integer, Shop> shops) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<Integer, Shop> entry : shops.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" -> ");
            sb.append(toStringWithNames(entry.getValue()));
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String toStringWithNames(Shop shop) {
        StringBuilder sb = new StringBuilder();
        sb.append("Shop [name=");
        sb.append(shop.name);
        sb.append(", isMainGame=");
        sb.append(shop.isMainGame);
        String[] itemNames = romHandler.getItemNames();
        sb.append(", items=[");
        for (int i = 0; i < shop.items.size(); i++) {
            int itemID = shop.items.get(i);
            sb.append(itemID);
            sb.append("-");
            sb.append(itemNames[itemID]);
            if (i != shop.items.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        sb.append("]");
        return sb.toString();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void shopPricesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Integer> prices = romHandler.getShopPrices();
        String[] names = romHandler.getItemNames();
        if (prices.size() != names.length) {
            throw new IllegalStateException("prices.size()=" + prices.size() + " names.length=" + names.length);
        }
        for (int i = 0; i < prices.size(); i++) {
            System.out.println(names[i] + ": " + prices.get(i) + "¥");
        }

        romHandler.setShopPrices(prices);
        List<Integer> after = romHandler.getShopPrices();

        assertEquals(prices, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void foo(String romName) {
        loadROM(romName);

        String[] names = romHandler.getItemNames();
        Map<Integer, Integer> balanced = Gen4Constants.balancedItemPrices;
        List<Integer> realPrices = romHandler.getShopPrices();

        System.out.println("\tpublic static final Map<Integer,Integer> balancedItemPrices = Stream.of(new Integer[][] {");
        for (int i = 1; i < 700; i++) {
            if (balanced.get(i) != null && !Objects.equals(realPrices.get(i), balanced.get(i))) {
                //System.out.println(i + "\t" + names[i] + "\tr=" + realPrices.get(i) + ", b=" + balanced.get(i));
                System.out.println("\t\t\t{ItemIDs." + names[i] + ", " + balanced.get(i) * 10 + "},");
            }
        }

        assertNotNull(romHandler);
    }

}
