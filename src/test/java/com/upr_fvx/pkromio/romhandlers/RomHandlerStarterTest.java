package com.upr_fvx.pkromio.romhandlers;

import com.upr_fvx.pkromio.constants.ItemIDs;
import com.upr_fvx.pkromio.constants.SpeciesIDs;
import com.upr_fvx.pkromio.gamedata.Item;
import com.upr_fvx.pkromio.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RomHandlerStarterTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void startersAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getStarters().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void startersDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Species> starters = romHandler.getStarters();
        System.out.println(starters);
        List<Species> before = new ArrayList<>(starters);
        romHandler.setStarters(starters);
        assertEquals(before, romHandler.getStarters());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void startersCanBeChangedWithSet(String romName) {
        loadROM(romName);
        // mewtwo because we don't expect it as a starter in the unmodified ROM
        Species mewtwo = romHandler.getSpecies().get(SpeciesIDs.mewtwo);

        List<Species> before = new ArrayList<>(romHandler.getStarters());
        Collections.fill(before, mewtwo);
        System.out.println(before);
        romHandler.setStarters(before);

        List<Species> after = new ArrayList<>(romHandler.getStarters());
        System.out.println(after);
        assertEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void starterHeldItemsDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Item> before = new ArrayList<>(romHandler.getStarterHeldItems());
        System.out.println(before);
        romHandler.setStarterHeldItems(before);
        assertEquals(before, romHandler.getStarterHeldItems());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void starterHeldItemsCanBeChangedWithSet(String romName) {
        loadROM(romName);
        // master ball because we don't expect it as a held item in the unmodified ROM
        Item masterBall = romHandler.getItems().get(ItemIDs.masterBall);

        List<Item> before = new ArrayList<>(romHandler.getStarterHeldItems());
        Collections.fill(before, masterBall);
        System.out.println(before);
        romHandler.setStarterHeldItems(before);

        List<Item> after = new ArrayList<>(romHandler.getStarterHeldItems());
        System.out.println(after);
        assertEquals(before, after);
    }
}
