package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ROM-free coverage for a first Items slice.
 * This does not prove ROM-facing field item writer/reload behavior.
 */
public class ItemDecisionTest {

    @Test
    public void randomizeFieldItemsKeepsSelectionInNonBadAllowedPool() {
        Item lowAllowed = item(10, "LowAllowed", true, false);
        Item badItem = item(20, "BadItem", true, true);
        Item keyItem = item(30, "KeyItem", false, false);
        Item highOne = item(1001, "HighOne", true, false);
        Item highTwo = item(1002, "HighTwo", true, false);
        Item highThree = item(1003, "HighThree", true, false);
        List<Item> originalFieldItems = List.of(lowAllowed, badItem, keyItem);
        Set<Item> nonBadAllowedItems = Set.of(highOne, highTwo, highThree);
        ItemTestRomHandler romHandler = ItemTestRomHandler.create(originalFieldItems,
                Set.of(lowAllowed, badItem, keyItem, highOne, highTwo, highThree), nonBadAllowedItems);
        Settings settings = new Settings();
        settings.setFieldItemsMod(Settings.FieldItemsMod.RANDOM);
        settings.setBanBadRandomFieldItems(true);

        ItemRandomizer randomizer = new ItemRandomizer(romHandler.proxy, settings, new FixedIntRandom(0, 1, 2));
        randomizer.randomizeFieldItems();

        assertTrue(randomizer.isFieldChangesMade());
        assertEquals(1, romHandler.setFieldItemsCalls);
        assertEquals(originalFieldItems.size(), romHandler.writtenFieldItems.size());
        assertFalse(romHandler.writtenFieldItems.isEmpty());
        assertEquals(nonBadAllowedItems, Set.copyOf(romHandler.writtenFieldItems));
        for (Item selectedItem : romHandler.writtenFieldItems) {
            assertTrue(selectedItem.isAllowed(), "Item was not allowed: " + selectedItem);
            assertFalse(selectedItem.isBad(), "Bad item was selected: " + selectedItem);
            assertTrue(selectedItem.getId() >= 1001, "Expected high item ID, got " + selectedItem);
        }
        assertFalse(romHandler.writtenFieldItems.contains(badItem));
        assertFalse(romHandler.writtenFieldItems.contains(keyItem));
    }

    private static Item item(int id, String name, boolean allowed, boolean bad) {
        Item item = new Item(id, name);
        item.setAllowed(allowed);
        item.setBad(bad);
        return item;
    }

    private static class FixedIntRandom extends java.util.Random {
        private final Queue<Integer> ints;

        private FixedIntRandom(int... ints) {
            this.ints = new ArrayDeque<>();
            for (int value : ints) {
                this.ints.add(value);
            }
        }

        @Override
        public int nextInt(int bound) {
            int value = ints.remove();
            if (value < 0 || value >= bound) {
                throw new IllegalArgumentException("Fixed value " + value + " outside bound " + bound);
            }
            return value;
        }
    }

    private static class ItemTestRomHandler implements InvocationHandler {
        private final List<Item> originalFieldItems;
        private final Set<Item> allowedItems;
        private final Set<Item> nonBadItems;
        private RomHandler proxy;
        private List<Item> writtenFieldItems;
        private int setFieldItemsCalls;

        private ItemTestRomHandler(List<Item> originalFieldItems, Set<Item> allowedItems, Set<Item> nonBadItems) {
            this.originalFieldItems = originalFieldItems;
            this.allowedItems = allowedItems;
            this.nonBadItems = nonBadItems;
        }

        private static ItemTestRomHandler create(List<Item> originalFieldItems, Set<Item> allowedItems,
                                                 Set<Item> nonBadItems) {
            ItemTestRomHandler handler = new ItemTestRomHandler(originalFieldItems, allowedItems, nonBadItems);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] {RomHandler.class}, handler);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService", "getTypeService" -> null;
                case "getFieldItems" -> new ArrayList<>(originalFieldItems);
                case "getItems" -> new ArrayList<>(allowedItems);
                case "getAllowedItems" -> allowedItems;
                case "getNonBadItems" -> nonBadItems;
                case "getMegaStones", "getRequiredFieldTMs" -> Collections.<Item>emptySet();
                case "isBalanceShopPrices" -> false;
                case "setFieldItems" -> {
                    writtenFieldItems = typedItemList(args[0]);
                    setFieldItemsCalls++;
                    yield null;
                }
                case "toString" -> "ItemTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        @SuppressWarnings("unchecked")
        private static List<Item> typedItemList(Object items) {
            return (List<Item>) items;
        }
    }
}
