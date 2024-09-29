package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.Item;
import com.dabomstew.pkrandom.gamedata.PickupItem;
import com.dabomstew.pkrandom.gamedata.Shop;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class ItemRandomizer extends Randomizer {

    private final Map<Integer, Integer> itemPlacementHistory = new HashMap<>();

    private boolean fieldChangesMade;
    private boolean shopChangesMade;
    private boolean pickupChangesMade;

    public ItemRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    /**
     * Returns whether any changes have been made to Field Items.
     */
    public boolean isFieldChangesMade() {
        return fieldChangesMade;
    }

    /**
     * Returns whether any changes have been made to Shop Items.
     */
    public boolean isShopChangesMade() {
        return shopChangesMade;
    }

    /**
     * Returns whether any changes have been made to Pickup Items.
     */
    public boolean isPickupChangesMade() {
        return pickupChangesMade;
    }

    public void shuffleFieldItems() {
        // TMs and non-TMs must end up at the same indices. Complicates the algorithm somewhat.
        List<Item> current = romHandler.getFieldItems();
        
        Stack<Item> tms = new Stack<>();
        Stack<Item> nonTMs = new Stack<>();
        for (Item item : current) {
            (item.isTM() ? tms : nonTMs).push(item);
        }

        Collections.shuffle(tms, random);
        Collections.shuffle(nonTMs, random);
        
        List<Item> combined = new ArrayList<>(current.size());
        for (Item item : current) {
            combined.add((item.isTM() ? tms : nonTMs).pop());
        }

        romHandler.setFieldItems(combined);
        fieldChangesMade = true;
    }

    public void randomizeFieldItems() {
//        boolean banBadItems = settings.isBanBadRandomFieldItems();
//        boolean distributeItemsControl = settings.getFieldItemsMod() == Settings.FieldItemsMod.RANDOM_EVEN;
//        boolean uniqueItems = !settings.isBalanceShopPrices(); // why is uniqueItems related to shop prices???
//
//        List<Item> allItems = romHandler.getItems();
//
//        ItemList possibleItems = banBadItems ? romHandler.getNonBadItems().copy() : romHandler.getAllowedItems().copy();
//        List<Item> currentItems = romHandler.getRegularFieldItems();
//        Set<Item> uniqueNoSellItems = romHandler.getUniqueNoSellItems();
//
//        List<Integer> currentTMs = romHandler.getCurrentFieldTMs();
//        List<Integer> requiredTMs = romHandler.getRequiredFieldTMs();
//
//        // System.out.println("distributeItemsControl: "+ distributeItemsControl);
//
//        int fieldItemCount = currentItems.size();
//        int fieldTMCount = currentTMs.size();
//        int reqTMCount = requiredTMs.size();
//        int totalTMCount = romHandler.getTMCount();
//
//        List<Item> newItems = new ArrayList<>();
//        List<Integer> newTMs = new ArrayList<>(requiredTMs);
//
//        // List<Integer> chosenItems = new ArrayList<Integer>(); // collecting chosenItems for later process
//
//        if (distributeItemsControl) {
//            for (int i = 0; i < fieldItemCount; i++) {
//                int chosenItem = possibleItems.randomNonTM(random);
//                int iterNum = 0;
//                while ((getItemPlacementHistory(chosenItem) > getItemPlacementAverage()) && iterNum < 100) {
//                    chosenItem = possibleItems.randomNonTM(random);
//                    iterNum += 1;
//                }
//                newItems.add(allItems.get(chosenItem));
//                if (uniqueItems && uniqueNoSellItems.contains(allItems.get(chosenItem))) {
//                    possibleItems.banSingles(chosenItem);
//                } else {
//                    setItemPlacementHistory(chosenItem);
//                }
//            }
//        } else {
//            for (int i = 0; i < fieldItemCount; i++) {
//                int chosenItem = possibleItems.randomNonTM(random);
//                newItems.add(allItems.get(chosenItem));
//                if (uniqueItems && uniqueNoSellItems.contains(allItems.get(chosenItem))) {
//                    possibleItems.banSingles(chosenItem);
//                }
//            }
//        }
//
//        for (int i = reqTMCount; i < fieldTMCount; i++) {
//            while (true) {
//                int tm = random.nextInt(totalTMCount) + 1;
//                if (!newTMs.contains(tm)) {
//                    newTMs.add(tm);
//                    break;
//                }
//            }
//        }
//
//
//        Collections.shuffle(newItems, random);
//        Collections.shuffle(newTMs, random);
//
//        romHandler.setRegularFieldItems(newItems);
//        romHandler.setFieldTMs(newTMs);
//        fieldChangesMade = true;
    }

    private void setItemPlacementHistory(int newItem) {
        int history = getItemPlacementHistory(newItem);
        // System.out.println("Current history: " + newPK.name + " : " + history);
        itemPlacementHistory.put(newItem, history + 1);
    }

    private int getItemPlacementHistory(int newItem) {
        List<Integer> placedItem = new ArrayList<>(itemPlacementHistory.keySet());
        if (placedItem.contains(newItem)) {
            return itemPlacementHistory.get(newItem);
        } else {
            return 0;
        }
    }

    private float getItemPlacementAverage() {
        // This method will return an integer of average for itemPlacementHistory
        // placed is less than average of all placed pokemon's appearances
        // E.g., Charmander's been placed once, but the average for all pokemon is 2.2
        // So add to list and return

        List<Integer> placedPK = new ArrayList<>(itemPlacementHistory.keySet());
        int placedPKNum = 0;
        for (Integer p : placedPK) {
            placedPKNum += itemPlacementHistory.get(p);
        }
        return (float) placedPKNum / (float) placedPK.size();
    }

    public void shuffleShopItems() {
        Map<Integer, Shop> shops = romHandler.getShopItems();
        if (shops == null) return;

        List<Item> allItems = new ArrayList<>();
        for (Shop shop : shops.values()) {
            allItems.addAll(shop.getItems());
        }
        Collections.shuffle(allItems, random);

        Iterator<Item> allItemsIter = allItems.iterator();

        for (Shop shop : shops.values()) {
            ListIterator<Item> shopIter = shop.getItems().listIterator();
            while (shopIter.hasNext()) {
                shopIter.next();
                shopIter.set(allItemsIter.next()); // assumes allItemsIter will always have a next
            }
        }

        romHandler.setShopItems(shops);
        shopChangesMade = true;
    }

    public void randomizeShopItems() {
        // TODO: how to deal with mega stones?
        Set<Item> possible = setupPossible();
        Set<Item> guaranteed = setupGuaranteed();

        Map<Integer, Shop> shops = copyShops(romHandler.getShopItems());

        List<Item> newItems = setupNewItems(possible, guaranteed, shops);

        placeNewItems(newItems, shops, guaranteed);

        romHandler.setShopItems(shops);
        shopChangesMade = true;
    }

    private Set<Item> setupPossible() {
        Set<Item> possible = new HashSet<>(settings.isBanBadRandomShopItems() ?
                romHandler.getNonBadItems() : romHandler.getAllowedItems());
        possible.removeIf(Item::isTM);
        if (settings.isBanRegularShopItems()) {
            possible.removeAll(romHandler.getRegularShopItems());
        }
        if (settings.isBanOPShopItems()) {
            possible.removeAll(romHandler.getOPShopItems());
        }

        return possible;
    }

    private Set<Item> setupGuaranteed() {
        Set<Item> guaranteed = new HashSet<>();
        if (settings.isGuaranteeEvolutionItems()) {
            guaranteed.addAll(romHandler.getEvolutionItems());
        }
        if ( settings.isGuaranteeXItems()) {
            guaranteed.addAll(romHandler.getXItems());
        }
        return guaranteed;
    }

    private Map<Integer, Shop> copyShops(Map<Integer, Shop> original) {
        Map<Integer, Shop> copy = new HashMap<>(original.size());
        for (Map.Entry<Integer, Shop> entry: original.entrySet()) {
            copy.put(entry.getKey(), new Shop(entry.getValue()));
        }
        return copy;
    }

    private List<Item> setupNewItems(Set<Item> possible, Set<Item> guaranteed, Map<Integer, Shop> shops) {
        List<Item> newItems = new ArrayList<>(guaranteed);

        int shopItemCount = shops.values().stream().mapToInt(s -> s.getItems().size()).sum();
        shopItemCount -= guaranteed.size();

        possible.removeAll(guaranteed);

        Stack<Item> remaining = new Stack<>();
        Collections.shuffle(remaining, random);
        for (int i = 0; i < shopItemCount; i++) {
            if (remaining.isEmpty()) {
                remaining.addAll(possible);
                Collections.shuffle(remaining, random);
            }
            newItems.add(remaining.pop());
        }
        return newItems;
    }

    private void placeNewItems(List<Item> newItems, Map<Integer, Shop> shops, Set<Item> guaranteed) {
        // split shops into main-game and non-main-game
        List<Shop> mainGameShops = new ArrayList<>();
        List<Shop> nonMainGameShops = new ArrayList<>();
        for (Shop shop : shops.values()) {
            (shop.isMainGame() ? mainGameShops : nonMainGameShops).add(shop);
        }

        // Place items in non-main-game shops; skip over guaranteed items
        Collections.shuffle(newItems, random);
        for (Shop shop : nonMainGameShops) {
            Iterator<Item> newItemsIter = newItems.iterator();
            for (int i = 0; i < shop.getItems().size(); i++) {
                Item replacement;
                do {
                    replacement = newItemsIter.next();
                } while (guaranteed.contains(replacement));
                newItemsIter.remove();
                shop.getItems().set(i, replacement);
            }
        }

        // And place the rest (including all guaranteed) in the main-game shops
        Collections.shuffle(newItems, random);
        for (Shop shop : mainGameShops) {
            Iterator<Item> newItemsIter = newItems.iterator();
            for (int i = 0; i < shop.getItems().size(); i++) {
                Item replacement = newItemsIter.next();
                newItemsIter.remove();
                shop.getItems().set(i, replacement);
            }
        }

        if (!newItems.isEmpty()) {
            throw new IllegalStateException("newItems has not been emptied");
        }
    }

    public void randomizePickupItems() {
        boolean banBadItems = settings.isBanBadRandomPickupItems();

        List<Item> possibleItems = new ArrayList<>(banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems());
        List<PickupItem> currentItems = romHandler.getPickupItems();
        List<PickupItem> newItems = new ArrayList<>();
        for (PickupItem currentItem : currentItems) {
            Item picked;
            do {
                picked = possibleItems.get(random.nextInt(possibleItems.size()));
                // No point getting TMs if they are reusable
            } while (picked.isTM() && romHandler.isTMsReusable());

            PickupItem pickupItem = new PickupItem(picked);
            for (int j = 0; j < PickupItem.PROBABILITY_SLOTS; j++) {
                pickupItem.getProbabilities()[j] = currentItem.getProbabilities()[j];
            }
            newItems.add(pickupItem);
        }

        romHandler.setPickupItems(newItems);
        pickupChangesMade = true;
    }

}
