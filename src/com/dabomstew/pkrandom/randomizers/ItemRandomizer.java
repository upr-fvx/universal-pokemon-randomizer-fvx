package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.game_data.ItemList;
import com.dabomstew.pkrandom.game_data.PickupItem;
import com.dabomstew.pkrandom.game_data.Shop;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;

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
        List<Integer> currentItems = romHandler.getRegularFieldItems();
        List<Integer> currentTMs = romHandler.getCurrentFieldTMs();

        Collections.shuffle(currentItems, random);
        Collections.shuffle(currentTMs, random);

        romHandler.setRegularFieldItems(currentItems);
        romHandler.setFieldTMs(currentTMs);
        fieldChangesMade = true;
    }

    public void randomizeFieldItems() {
        boolean banBadItems = settings.isBanBadRandomFieldItems();
        boolean distributeItemsControl = settings.getFieldItemsMod() == Settings.FieldItemsMod.RANDOM_EVEN;
        boolean uniqueItems = !settings.isBalanceShopPrices();

        ItemList possibleItems = banBadItems ? romHandler.getNonBadItems().copy() : romHandler.getAllowedItems().copy();
        List<Integer> currentItems = romHandler.getRegularFieldItems();
        List<Integer> currentTMs = romHandler.getCurrentFieldTMs();
        List<Integer> requiredTMs = romHandler.getRequiredFieldTMs();
        List<Integer> uniqueNoSellItems = romHandler.getUniqueNoSellItems();
        // System.out.println("distributeItemsControl: "+ distributeItemsControl);

        int fieldItemCount = currentItems.size();
        int fieldTMCount = currentTMs.size();
        int reqTMCount = requiredTMs.size();
        int totalTMCount = romHandler.getTMCount();

        List<Integer> newItems = new ArrayList<>();
        List<Integer> newTMs = new ArrayList<>(requiredTMs);

        // List<Integer> chosenItems = new ArrayList<Integer>(); // collecting chosenItems for later process

        if (distributeItemsControl) {
            for (int i = 0; i < fieldItemCount; i++) {
                int chosenItem = possibleItems.randomNonTM(random);
                int iterNum = 0;
                while ((getItemPlacementHistory(chosenItem) > getItemPlacementAverage()) && iterNum < 100) {
                    chosenItem = possibleItems.randomNonTM(random);
                    iterNum += 1;
                }
                newItems.add(chosenItem);
                if (uniqueItems && uniqueNoSellItems.contains(chosenItem)) {
                    possibleItems.banSingles(chosenItem);
                } else {
                    setItemPlacementHistory(chosenItem);
                }
            }
        } else {
            for (int i = 0; i < fieldItemCount; i++) {
                int chosenItem = possibleItems.randomNonTM(random);
                newItems.add(chosenItem);
                if (uniqueItems && uniqueNoSellItems.contains(chosenItem)) {
                    possibleItems.banSingles(chosenItem);
                }
            }
        }

        for (int i = reqTMCount; i < fieldTMCount; i++) {
            while (true) {
                int tm = random.nextInt(totalTMCount) + 1;
                if (!newTMs.contains(tm)) {
                    newTMs.add(tm);
                    break;
                }
            }
        }


        Collections.shuffle(newItems, random);
        Collections.shuffle(newTMs, random);

        romHandler.setRegularFieldItems(newItems);
        romHandler.setFieldTMs(newTMs);
        fieldChangesMade = true;
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
        Map<Integer, Shop> currentItems = romHandler.getShopItems();
        if (currentItems == null) return;
        List<Integer> itemList = new ArrayList<>();
        for (Shop shop : currentItems.values()) {
            itemList.addAll(shop.items);
        }
        Collections.shuffle(itemList, random);

        Iterator<Integer> itemListIter = itemList.iterator();

        for (Shop shop : currentItems.values()) {
            for (int i = 0; i < shop.items.size(); i++) {
                shop.items.remove(i);
                shop.items.add(i, itemListIter.next());
            }
        }

        romHandler.setShopItems(currentItems);
        shopChangesMade = true;
    }

    public void randomizeShopItems() {
        boolean banBadItems = settings.isBanBadRandomShopItems();
        boolean banRegularShopItems = settings.isBanRegularShopItems();
        boolean banOPShopItems = settings.isBanOPShopItems();
        boolean balancePrices = settings.isBalanceShopPrices();
        boolean placeEvolutionItems = settings.isGuaranteeEvolutionItems();
        boolean placeXItems = settings.isGuaranteeXItems();

        if (romHandler.getShopItems() == null) return;
        Set<Integer> possibleItems = banBadItems ? romHandler.getNonBadItems().getNonTMSet() : romHandler.getAllowedItems().getNonTMSet();
        if (banRegularShopItems) {
            possibleItems.removeAll(romHandler.getRegularShopItems());
        }
        if (banOPShopItems) {
            possibleItems.removeAll(romHandler.getOPShopItems());
        }
        Map<Integer, Shop> currentItems = romHandler.getShopItems();

        int shopItemCount = currentItems.values().stream().mapToInt(s -> s.items.size()).sum();

        List<Integer> newItems = new ArrayList<>();
        Map<Integer, Shop> newItemsMap = new TreeMap<>();
        List<Integer> guaranteedItems = new ArrayList<>();
        if (placeEvolutionItems) {
            guaranteedItems.addAll(romHandler.getEvolutionItems());
        }
        if (placeXItems) {
            guaranteedItems.addAll(romHandler.getXItems());
        }
        shopItemCount = shopItemCount - guaranteedItems.size();
        newItems.addAll(guaranteedItems);
        possibleItems.removeAll(guaranteedItems);

        Stack<Integer> remaining = new Stack<>();
        Collections.shuffle(remaining, random);
        for (int i = 0; i < shopItemCount; i++) {
            if (remaining.isEmpty()) {
                remaining.addAll(possibleItems);
                Collections.shuffle(remaining, random);
            }
            newItems.add(remaining.pop());
        }

        if (placeEvolutionItems || placeXItems) {

            // Guarantee main-game
            List<Integer> mainGameShops = new ArrayList<>();
            List<Integer> nonMainGameShops = new ArrayList<>();
            for (int i : currentItems.keySet()) {
                if (currentItems.get(i).isMainGame) {
                    mainGameShops.add(i);
                } else {
                    nonMainGameShops.add(i);
                }
            }

            // Place items in non-main-game shops; skip over guaranteed items
            Collections.shuffle(newItems, random);
            for (int i : nonMainGameShops) {
                int j = 0;
                List<Integer> newShopItems = new ArrayList<>();
                Shop oldShop = currentItems.get(i);
                for (Integer ignored : oldShop.items) {
                    Integer item = newItems.get(j);
                    while (guaranteedItems.contains(item)) {
                        j++;
                        item = newItems.get(j);
                    }
                    newShopItems.add(item);
                    newItems.remove(item);
                }
                Shop shop = new Shop(oldShop);
                shop.items = newShopItems;
                newItemsMap.put(i, shop);
            }

            // Place items in main-game shops
            Collections.shuffle(newItems, random);
            for (int i : mainGameShops) {
                List<Integer> newShopItems = new ArrayList<>();
                Shop oldShop = currentItems.get(i);
                for (Integer ignored : oldShop.items) {
                    Integer item = newItems.get(0);
                    newShopItems.add(item);
                    newItems.remove(0);
                }
                Shop shop = new Shop(oldShop);
                shop.items = newShopItems;
                newItemsMap.put(i, shop);
            }
        } else {

            Iterator<Integer> newItemsIter = newItems.iterator();

            for (int i : currentItems.keySet()) {
                List<Integer> newShopItems = new ArrayList<>();
                Shop oldShop = currentItems.get(i);
                for (Integer ignored : oldShop.items) {
                    newShopItems.add(newItemsIter.next());
                }
                Shop shop = new Shop(oldShop);
                shop.items = newShopItems;
                newItemsMap.put(i, shop);
            }
        }

        romHandler.setShopItems(newItemsMap);
        if (balancePrices) {
            romHandler.setBalancedShopPrices();
        }
        shopChangesMade = true;
    }

    public void randomizePickupItems() {
        boolean banBadItems = settings.isBanBadRandomPickupItems();

        ItemList possibleItems = banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems();
        List<PickupItem> currentItems = romHandler.getPickupItems();
        List<PickupItem> newItems = new ArrayList<>();
        for (int i = 0; i < currentItems.size(); i++) {
            int item;
            if (romHandler.generationOfPokemon() == 3 || romHandler.generationOfPokemon() == 4) {
                // Allow TMs in Gen 3/4 since they aren't infinite (and you get TMs from Pickup in the vanilla game)
                item = possibleItems.randomItem(random);
            } else {
                item = possibleItems.randomNonTM(random);
            }
            PickupItem pickupItem = new PickupItem(item);
            pickupItem.probabilities = Arrays.copyOf(currentItems.get(i).probabilities, currentItems.size());
            newItems.add(pickupItem);
        }

        romHandler.setPickupItems(newItems);
        pickupChangesMade = true;
    }

}
