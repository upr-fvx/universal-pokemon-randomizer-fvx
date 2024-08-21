package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.Item;
import com.dabomstew.pkrandom.gamedata.ItemList;
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
        List<Item> currentItems = romHandler.getRegularFieldItems();
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
        boolean uniqueItems = !settings.isBalanceShopPrices(); // why is uniqueItems related to shop prices???

        List<Item> allItems = romHandler.getItems();

        ItemList possibleItems = banBadItems ? romHandler.getNonBadItems().copy() : romHandler.getAllowedItems().copy();
        List<Item> currentItems = romHandler.getRegularFieldItems();
        Set<Item> uniqueNoSellItems = romHandler.getUniqueNoSellItems();

        List<Integer> currentTMs = romHandler.getCurrentFieldTMs();
        List<Integer> requiredTMs = romHandler.getRequiredFieldTMs();

        // System.out.println("distributeItemsControl: "+ distributeItemsControl);

        int fieldItemCount = currentItems.size();
        int fieldTMCount = currentTMs.size();
        int reqTMCount = requiredTMs.size();
        int totalTMCount = romHandler.getTMCount();

        List<Item> newItems = new ArrayList<>();
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
                newItems.add(allItems.get(chosenItem));
                if (uniqueItems && uniqueNoSellItems.contains(allItems.get(chosenItem))) {
                    possibleItems.banSingles(chosenItem);
                } else {
                    setItemPlacementHistory(chosenItem);
                }
            }
        } else {
            for (int i = 0; i < fieldItemCount; i++) {
                int chosenItem = possibleItems.randomNonTM(random);
                newItems.add(allItems.get(chosenItem));
                if (uniqueItems && uniqueNoSellItems.contains(allItems.get(chosenItem))) {
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
        List<Item> itemList = new ArrayList<>();
        for (Shop shop : currentItems.values()) {
            itemList.addAll(shop.getItems());
        }
        Collections.shuffle(itemList, random);

        Iterator<Item> itemListIter = itemList.iterator();

        for (Shop shop : currentItems.values()) {
            for (int i = 0; i < shop.getItems().size(); i++) {
                shop.getItems().remove(i);
                shop.getItems().add(i, itemListIter.next());
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
        List<Item> allItems = romHandler.getItems();
        Set<Item> possibleItems = (banBadItems ? romHandler.getNonBadItems().getNonTMSet() :
                romHandler.getAllowedItems().getNonTMSet()).stream()
                .map(allItems::get).collect(Collectors.toSet());

        if (banRegularShopItems) {
            possibleItems.removeAll(romHandler.getRegularShopItems());
        }
        if (banOPShopItems) {
            possibleItems.removeAll(romHandler.getOPShopItems());
        }
        Map<Integer, Shop> currentItems = romHandler.getShopItems();

        int shopItemCount = currentItems.values().stream().mapToInt(s -> s.getItems().size()).sum();

        Set<Item> guaranteedItems = new HashSet<>();
        if (placeEvolutionItems) {
            guaranteedItems.addAll(romHandler.getEvolutionItems());
        }
        if (placeXItems) {
            guaranteedItems.addAll(romHandler.getXItems());
        }
        shopItemCount = shopItemCount - guaranteedItems.size();

        Map<Integer, Shop> newItemsMap = new TreeMap<>();
        List<Item> newItems = new ArrayList<>(guaranteedItems);
        possibleItems.removeAll(guaranteedItems);

        Stack<Item> remaining = new Stack<>();
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
                if (currentItems.get(i).isMainGame()) {
                    mainGameShops.add(i);
                } else {
                    nonMainGameShops.add(i);
                }
            }

            // Place items in non-main-game shops; skip over guaranteed items
            Collections.shuffle(newItems, random);
            for (int i : nonMainGameShops) {
                int j = 0;
                List<Item> newShopItems = new ArrayList<>();
                Shop oldShop = currentItems.get(i);
                for (Item ignored : oldShop.getItems()) {
                    Item item = newItems.get(j);
                    while (guaranteedItems.contains(item)) {
                        j++;
                        item = newItems.get(j);
                    }
                    newShopItems.add(item);
                    newItems.remove(item);
                }
                Shop shop = new Shop(oldShop);
                shop.setItems(newShopItems);
                newItemsMap.put(i, shop);
            }

            // Place items in main-game shops
            Collections.shuffle(newItems, random);
            for (int i : mainGameShops) {
                List<Item> newShopItems = new ArrayList<>();
                Shop oldShop = currentItems.get(i);
                for (Item ignored : oldShop.getItems()) {
                    Item item = newItems.get(0);
                    newShopItems.add(item);
                    newItems.remove(0);
                }
                Shop shop = new Shop(oldShop);
                shop.setItems(newShopItems);
                newItemsMap.put(i, shop);
            }
        } else {

            Iterator<Item> newItemsIter = newItems.iterator();

            for (int i : currentItems.keySet()) {
                List<Item> newShopItems = new ArrayList<>();
                Shop oldShop = currentItems.get(i);
                for (Item ignored : oldShop.getItems()) {
                    newShopItems.add(newItemsIter.next());
                }
                Shop shop = new Shop(oldShop);
                shop.setItems(newShopItems);
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

        List<Item> possibleItems = new ArrayList<>(banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems());
        List<PickupItem> currentItems = romHandler.getPickupItems();
        List<PickupItem> newItems = new ArrayList<>();
        for (PickupItem currentItem : currentItems) {
            Item picked;
            do {
                picked = possibleItems.get(random.nextInt(possibleItems.size()));
                // No point getting TMs through if they are reusable
            } while (picked.isTM() || !romHandler.isTMsReusable());

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
