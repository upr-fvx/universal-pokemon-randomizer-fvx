package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkrandom.gamedata.Item;
import com.dabomstew.pkrandom.gamedata.PickupItem;
import com.dabomstew.pkrandom.gamedata.Shop;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class ItemRandomizer extends Randomizer {

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

    public void randomizeFieldItems() {
        // TMs and non-TMs must end up at the same indices. Complicates the algorithm somewhat.
        List<Item> current = romHandler.getFieldItems();

        Stack<Item> tms = new Stack<>();
        Stack<Item> nonTMs = new Stack<>();
        for (Item item : current) {
            (item.isTM() ? tms : nonTMs).push(item);
        }

        switch (settings.getFieldItemsMod()) {
            case SHUFFLE:
                Collections.shuffle(tms, random);
                Collections.shuffle(nonTMs, random);
                break;
            case RANDOM:
            case RANDOM_EVEN:
                randomizeTMFieldItems(tms);
                randomizeNonTMFieldItems(nonTMs);
                break;
        }

        List<Item> combined = new ArrayList<>(current.size());
        for (Item item : current) {
            combined.add((item.isTM() ? tms : nonTMs).pop());
        }

        romHandler.setFieldItems(combined);
        fieldChangesMade = true;
    }

    /**
     * Randomizes TM field items, by modifying the input list.
     */
    private void randomizeTMFieldItems(List<Item> tms) {
        List<Item> allTMs = romHandler.getItems().stream().filter(Item::isTM).collect(Collectors.toList());
        Set<Item> requiredTMs = romHandler.getRequiredFieldTMs();

        int neededTMAmount = tms.size();

        Set<Item> newTMs = new HashSet<>(requiredTMs);
        while (newTMs.size() < neededTMAmount && newTMs.size() < allTMs.size()) {
            newTMs.add(allTMs.get(random.nextInt(allTMs.size()))); // duplicates get automatically ignored by the Set
        }
        if (newTMs.size() != neededTMAmount) {
            throw new RandomizationException("Could not randomize TM field items, too many TMs requested.");
        }

        tms.clear();
        tms.addAll(newTMs);
    }

    /**
     * Randomizes non-TM field items, by modifying the input list.
     */
    private void randomizeNonTMFieldItems(List<Item> nonTMs) {

        boolean banBadItems = settings.isBanBadRandomFieldItems();
        boolean uniqueItems = !settings.isBalanceShopPrices(); // TODO: why is uniqueItems related to shop prices???
        boolean evenItems = settings.getFieldItemsMod() == Settings.FieldItemsMod.RANDOM_EVEN;

        List<Item> possible = new ArrayList<>(banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems());
        possible.removeIf(Item::isTM);
        Set<Item> uniqueNoSellItems = uniqueItems ? romHandler.getUniqueNoSellItems() : new HashSet<>();

        int neededNonTMCount = nonTMs.size();
        nonTMs.clear();

        // Completely different algorithms whether items are "evenly distributed", or entirely random
        // Though both of them are simple enough to be best left uncommented. Just read them.
        if (evenItems) {
            Stack<Item> remaining = new Stack<>();
            Collections.shuffle(remaining, random);
            for (int i = 0; i < neededNonTMCount; i++) {
                if (remaining.isEmpty()) {
                    remaining.addAll(possible);
                    Collections.shuffle(remaining, random);
                }
                Item chosen = remaining.pop();
                nonTMs.add(chosen);
                if (uniqueNoSellItems.contains(chosen)) {
                    possible.remove(chosen);
                }
            }

        } else {
            for (int i = 0; i < neededNonTMCount; i++) {
                Item chosen = possible.get(random.nextInt(possible.size()));
                nonTMs.add(chosen);
                if (uniqueNoSellItems.contains(chosen)) {
                    possible.remove(chosen);
                }
            }
        }

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
        if (settings.isGuaranteeXItems()) {
            guaranteed.addAll(romHandler.getXItems());
        }
        return guaranteed;
    }

    private Map<Integer, Shop> copyShops(Map<Integer, Shop> original) {
        Map<Integer, Shop> copy = new HashMap<>(original.size());
        for (Map.Entry<Integer, Shop> entry : original.entrySet()) {
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
        if (!romHandler.canTMsBeHeld() || romHandler.isTMsReusable()) {
            // Normally these conditions overlap, but if TMs are made reusable we can get the latter but not the former,
            // and it's still no fun getting the same reusable TM over and over again.
            possibleItems.removeIf(Item::isTM);
        }
        List<PickupItem> currentItems = romHandler.getPickupItems();
        List<PickupItem> newItems = new ArrayList<>();
        for (PickupItem currentItem : currentItems) {
            Item picked = possibleItems.get(random.nextInt(possibleItems.size()));

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
