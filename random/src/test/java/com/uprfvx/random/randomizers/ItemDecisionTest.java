package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.ItemMechanicCategory;
import com.uprfvx.romio.gamedata.PickupItem;
import com.uprfvx.romio.gamedata.Shop;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.CfruDpeItemCategories;
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

    private static final int CFRU_DPE_ULTRANECROZIUM_Z = 0x214;
    private static final int CFRU_DPE_SNORLIUM_Z = 0x263;
    private static final int CFRU_DPE_STANDARD_ULTRANECROZIUM_Z =
            CfruDpeItemCategories.standardIdForSourceId(CFRU_DPE_ULTRANECROZIUM_Z);
    private static final int NONCANONICAL_PIDGEOTITE = 9000;
    private static final int NONCANONICAL_CAMERUPTITE = 9001;

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

    @Test
    public void mechanicItemsAreExcludedFromFieldShopPickupAndHeldPoolsByDefault() {
        Item normal = item(10, "Normal", true, false);
        Item mega = mechanicItem(20, "Mega", ItemMechanicCategory.MEGA_STONE);
        Item zCrystal = mechanicItem(30, "ZCrystal", ItemMechanicCategory.Z_CRYSTAL);
        Item necroziumZ = item(CFRU_DPE_ULTRANECROZIUM_Z, "Necrozium Z", true, false);
        Item cfruDpeStandardNecroziumZ = item(CFRU_DPE_STANDARD_ULTRANECROZIUM_Z, "Necrozium Z", true, false);
        Item snorliumZ = item(CFRU_DPE_SNORLIUM_Z, "Snorlium Z", true, false);
        Item pidgeotite = item(NONCANONICAL_PIDGEOTITE, "Pidgeotite", true, false);
        Item cameruptite = item(NONCANONICAL_CAMERUPTITE, "Cameruptite", true, false);
        Item dynamaxBand = item(ItemIDs.dynamaxBand, "Dynamax Band", true, false);
        Item wishingPiece = item(ItemIDs.wishingPiece, "Wishing Piece", true, false);
        Item dynamax = mechanicItem(40, "Dynamax", ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
        Set<Item> allItems = Set.of(normal, mega, zCrystal, necroziumZ, cfruDpeStandardNecroziumZ, snorliumZ,
                pidgeotite, cameruptite, dynamaxBand, wishingPiece, dynamax);
        ItemTestRomHandler romHandler = ItemTestRomHandler.create(
                List.of(mega, zCrystal, necroziumZ, cfruDpeStandardNecroziumZ, snorliumZ, pidgeotite, cameruptite,
                        dynamaxBand, wishingPiece, dynamax),
                allItems,
                allItems);
        romHandler.shops = List.of(specialShop(List.of(mega, zCrystal, necroziumZ, cfruDpeStandardNecroziumZ,
                snorliumZ, pidgeotite, cameruptite, dynamaxBand, wishingPiece, dynamax)));
        romHandler.pickupItems = List.of(new PickupItem(mega), new PickupItem(zCrystal), new PickupItem(necroziumZ),
                new PickupItem(cfruDpeStandardNecroziumZ), new PickupItem(snorliumZ), new PickupItem(pidgeotite),
                new PickupItem(cameruptite), new PickupItem(dynamaxBand), new PickupItem(wishingPiece),
                new PickupItem(dynamax));
        romHandler.starterHeldItems = List.of(mega, zCrystal, necroziumZ, cfruDpeStandardNecroziumZ, snorliumZ,
                pidgeotite, cameruptite, dynamaxBand, wishingPiece, dynamax);
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        Trainer trainer = new Trainer();
        trainer.getPokemon().add(trainerPokemon);
        romHandler.trainers = List.of(trainer);
        romHandler.allHeldItems = allItems;
        Settings settings = new Settings();
        settings.setFieldItemsMod(Settings.FieldItemsMod.RANDOM);
        settings.setRandomizeHeldItemsForRegularTrainerPokemon(true);

        new ItemRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizeFieldItems();
        new ItemRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizeShopItems();
        new ItemRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizePickupItems();
        new StarterRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizeStarterHeldItems();
        new TrainerPokemonRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizeTrainerHeldItems();

        assertEquals(List.of(normal, normal, normal, normal, normal, normal, normal, normal, normal, normal),
                romHandler.writtenFieldItems);
        assertEquals(List.of(normal, normal, normal, normal, normal, normal, normal, normal, normal, normal),
                romHandler.writtenShops.get(0).getItems());
        assertEquals(List.of(normal, normal, normal, normal, normal, normal, normal, normal, normal, normal),
                romHandler.writtenPickupItems.stream().map(PickupItem::getItem).toList());
        assertEquals(List.of(normal, normal, normal, normal, normal, normal, normal, normal, normal, normal),
                romHandler.writtenStarterHeldItems);
        assertEquals(normal, trainerPokemon.getHeldItem());
    }

    @Test
    public void trainerSensibleHeldItemsFallbackKeepsMechanicExclusions() {
        Item normal = item(10, "Normal", true, false);
        Item mega = mechanicItem(20, "Mega", ItemMechanicCategory.MEGA_STONE);
        Item zCrystal = mechanicItem(30, "ZCrystal", ItemMechanicCategory.Z_CRYSTAL);
        Item dynamax = mechanicItem(40, "Dynamax", ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        Trainer trainer = new Trainer();
        trainer.getPokemon().add(trainerPokemon);
        ItemTestRomHandler romHandler = ItemTestRomHandler.create(
                List.of(), Set.of(normal, mega, zCrystal, dynamax), Set.of(normal, mega, zCrystal, dynamax));
        romHandler.trainers = List.of(trainer);
        romHandler.sensibleHeldItems = List.of(mega, zCrystal, dynamax);
        romHandler.allHeldItems = Set.of(normal, mega, zCrystal, dynamax);
        Settings settings = new Settings();
        settings.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        settings.setSensibleItemsOnlyForTrainers(true);

        new TrainerPokemonRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizeTrainerHeldItems();

        assertEquals(normal, trainerPokemon.getHeldItem());
    }

    @Test
    public void trainerSensibleHeldItemsUseSensibleCandidatesWhenAvailable() {
        Item normal = item(10, "Normal", true, false);
        Item sensible = item(11, "Sensible", true, false);
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        Trainer trainer = new Trainer();
        trainer.getPokemon().add(trainerPokemon);
        ItemTestRomHandler romHandler = ItemTestRomHandler.create(List.of(), Set.of(normal, sensible),
                Set.of(normal, sensible));
        romHandler.trainers = List.of(trainer);
        romHandler.sensibleHeldItems = List.of(sensible);
        romHandler.allHeldItems = Set.of(normal);
        Settings settings = new Settings();
        settings.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        settings.setSensibleItemsOnlyForTrainers(true);

        new TrainerPokemonRandomizer(romHandler.proxy, settings, new ZeroRandom()).randomizeTrainerHeldItems();

        assertEquals(sensible, trainerPokemon.getHeldItem());
    }

    @Test
    public void mechanicItemsAreIncludedWhenTheirSettingsAreEnabled() {
        Item mega = mechanicItem(20, "Mega", ItemMechanicCategory.MEGA_STONE);
        Item pidgeotite = item(NONCANONICAL_PIDGEOTITE, "Pidgeotite", true, false);
        Item snorliumZ = item(CFRU_DPE_SNORLIUM_Z, "Snorlium Z", true, false);
        Item dynamax = mechanicItem(40, "Dynamax", ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
        Settings settings = new Settings();
        settings.setFieldItemsMod(Settings.FieldItemsMod.RANDOM);
        settings.setIncludeMegaItems(true);
        settings.setIncludeZCrystalItems(true);
        settings.setIncludeDynamaxGmaxItems(true);

        ItemTestRomHandler fieldHandler = ItemTestRomHandler.create(List.of(mega), Set.of(mega), Set.of(mega));
        new ItemRandomizer(fieldHandler.proxy, settings, new ZeroRandom()).randomizeFieldItems();

        ItemTestRomHandler shopHandler = ItemTestRomHandler.create(List.of(), Set.of(pidgeotite), Set.of(pidgeotite));
        shopHandler.shops = List.of(specialShop(List.of(pidgeotite)));
        new ItemRandomizer(shopHandler.proxy, settings, new ZeroRandom()).randomizeShopItems();

        ItemTestRomHandler pickupHandler = ItemTestRomHandler.create(List.of(), Set.of(snorliumZ), Set.of(snorliumZ));
        pickupHandler.pickupItems = List.of(new PickupItem(snorliumZ));
        new ItemRandomizer(pickupHandler.proxy, settings, new ZeroRandom()).randomizePickupItems();

        ItemTestRomHandler starterHandler = ItemTestRomHandler.create(List.of(), Set.of(dynamax), Set.of(dynamax));
        starterHandler.starterHeldItems = List.of(dynamax);
        new StarterRandomizer(starterHandler.proxy, settings, new ZeroRandom()).randomizeStarterHeldItems();

        assertEquals(List.of(mega), fieldHandler.writtenFieldItems);
        assertEquals(List.of(pidgeotite), shopHandler.writtenShops.get(0).getItems());
        assertEquals(snorliumZ, pickupHandler.writtenPickupItems.get(0).getItem());
        assertEquals(List.of(dynamax), starterHandler.writtenStarterHeldItems);
    }

    private static Item item(int id, String name, boolean allowed, boolean bad) {
        Item item = new Item(id, name);
        item.setAllowed(allowed);
        item.setBad(bad);
        return item;
    }

    private static Item mechanicItem(int id, String name, ItemMechanicCategory category) {
        Item item = item(id, name, true, false);
        item.addMechanicCategory(category);
        return item;
    }

    private static Shop specialShop(List<Item> items) {
        Shop shop = new Shop();
        shop.setItems(new ArrayList<>(items));
        shop.setMainGame(true);
        shop.setSpecialShop(true);
        return shop;
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

    private static class ZeroRandom extends java.util.Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }

    private static class ItemTestRomHandler implements InvocationHandler {
        private final List<Item> originalFieldItems;
        private final Set<Item> allowedItems;
        private final Set<Item> nonBadItems;
        private RomHandler proxy;
        private List<Shop> shops = Collections.emptyList();
        private List<PickupItem> pickupItems = Collections.emptyList();
        private List<Item> starterHeldItems = Collections.emptyList();
        private List<Trainer> trainers = Collections.emptyList();
        private List<Item> sensibleHeldItems = Collections.emptyList();
        private Set<Item> allHeldItems = Collections.emptySet();
        private List<Item> writtenFieldItems;
        private List<Shop> writtenShops;
        private List<PickupItem> writtenPickupItems;
        private List<Item> writtenStarterHeldItems;
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
                case "getShops" -> shops.stream().map(Shop::new).toList();
                case "getPickupItems" -> pickupItems.stream().map(PickupItem::new).toList();
                case "getStarterHeldItems" -> new ArrayList<>(starterHeldItems);
                case "getTrainers" -> trainers;
                case "getSensibleHeldItemsFor" -> sensibleHeldItems;
                case "getAllHeldItems" -> allHeldItems;
                case "getAllConsumableHeldItems" -> allHeldItems;
                case "getMoves" -> Collections.emptyList();
                case "getItems" -> new ArrayList<>(allowedItems);
                case "getAllowedItems" -> allowedItems;
                case "getNonBadItems" -> nonBadItems;
                case "getMegaStones", "getRequiredFieldTMs", "getEvolutionItems", "getXItems",
                     "getRegularShopItems", "getOPShopItems" -> Collections.<Item>emptySet();
                case "isBalanceShopPrices" -> false;
                case "canTMsBeHeld" -> true;
                case "isTMsReusable" -> false;
                case "setFieldItems" -> {
                    writtenFieldItems = typedItemList(args[0]);
                    setFieldItemsCalls++;
                    yield null;
                }
                case "setShops" -> {
                    writtenShops = typedShopList(args[0]);
                    yield null;
                }
                case "setPickupItems" -> {
                    writtenPickupItems = typedPickupList(args[0]);
                    yield null;
                }
                case "setStarterHeldItems" -> {
                    writtenStarterHeldItems = typedItemList(args[0]);
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

        @SuppressWarnings("unchecked")
        private static List<Shop> typedShopList(Object shops) {
            return (List<Shop>) shops;
        }

        @SuppressWarnings("unchecked")
        private static List<PickupItem> typedPickupList(Object pickupItems) {
            return (List<PickupItem>) pickupItems;
        }
    }
}
