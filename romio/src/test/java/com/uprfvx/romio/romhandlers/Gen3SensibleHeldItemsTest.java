package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.TrainerPokemon;
import com.uprfvx.romio.gamedata.Type;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3SensibleHeldItemsTest {

    @Test
    public void expandedTypeWithoutGen3BoostingItemsDoesNotCrash() throws Exception {
        Gen3RomHandler romHandler = romHandlerWithItems();
        TrainerPokemon pokemon = new TrainerPokemon();
        pokemon.setSpecies(new Species(9999));

        List<Item> sensibleItems = assertDoesNotThrow(() -> romHandler.getSensibleHeldItemsFor(
                pokemon, false, List.of(move(Type.FAIRY, 60)), new int[] {0}));

        assertFalse(sensibleItems.isEmpty());
        assertTrue(sensibleItems.stream().noneMatch(item -> item == null));
        assertTrue(sensibleItems.stream().anyMatch(item -> item.getId() == ItemIDs.petayaBerry));
    }

    @Test
    public void missingSpeciesAndInvalidMoveSlotsAreSkipped() throws Exception {
        Gen3RomHandler romHandler = romHandlerWithItems();

        List<Item> sensibleItems = assertDoesNotThrow(() -> romHandler.getSensibleHeldItemsFor(
                new TrainerPokemon(), false, List.of(move(Type.FIRE, 60)), new int[] {-1, 4, 0}));

        assertFalse(sensibleItems.isEmpty());
        assertTrue(sensibleItems.stream().anyMatch(item -> item.getId() == ItemIDs.charcoal));
    }

    @Test
    public void missingMovepoolReturnsEmptyMoveset() {
        Gen3RomHandler romHandler = new Gen3RomHandler();

        int[] moveset = assertDoesNotThrow(() -> romHandler.getMovesAtLevel(9999, Map.of(), 50));

        assertArrayEquals(new int[] {0, 0, 0, 0}, moveset);
    }

    @Test
    public void nullMovesetsReturnEmptyMoveset() {
        Gen3RomHandler romHandler = new Gen3RomHandler();

        int[] moveset = assertDoesNotThrow(() -> romHandler.getMovesAtLevel(9999, null, 50));

        assertArrayEquals(new int[] {0, 0, 0, 0}, moveset);
    }

    private static Gen3RomHandler romHandlerWithItems() throws ReflectiveOperationException {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Field itemsField = Gen3RomHandler.class.getDeclaredField("items");
        itemsField.setAccessible(true);
        itemsField.set(romHandler, itemsById(512));
        return romHandler;
    }

    private static List<Item> itemsById(int size) {
        List<Item> items = new ArrayList<>();
        items.add(null);
        for (int i = 1; i < size; i++) {
            items.add(new Item(i, "Item " + i));
        }
        return items;
    }

    private static Move move(Type type, int power) {
        Move move = new Move();
        move.type = type;
        move.power = power;
        return move;
    }
}
