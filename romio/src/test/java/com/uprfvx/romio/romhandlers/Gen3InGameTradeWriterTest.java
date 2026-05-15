package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.InGameTrade;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Gen3InGameTradeWriterTest {

    @Test
    public void nullRequestedSpeciesRowIsPreservedBeforeByteWrite() {
        Species safeGiven = species(1, "Bulbasaur");
        InGameTrade trade = trade(safeGiven, null);
        byte[] rowBytes = sentinelRowBytes();
        byte[] before = rowBytes.clone();

        boolean writeAttempted = writeSyntheticMarkerIfAllowed(trade, rowBytes, mappingsFor(safeGiven), internalPokes(safeGiven));

        assertFalse(writeAttempted);
        assertArrayEquals(before, rowBytes);
    }

    @Test
    public void placeholderSpeciesRowIsPreservedBeforeByteWrite() {
        Species placeholderGiven = species(2, "Bad Egg");
        Species safeRequested = species(1, "Bulbasaur");
        InGameTrade trade = trade(placeholderGiven, safeRequested);
        byte[] rowBytes = sentinelRowBytes();
        byte[] before = rowBytes.clone();

        boolean writeAttempted = writeSyntheticMarkerIfAllowed(
                trade, rowBytes, mappingsFor(safeRequested, placeholderGiven), internalPokes(safeRequested, placeholderGiven));

        assertFalse(writeAttempted);
        assertArrayEquals(before, rowBytes);
    }

    private static boolean writeSyntheticMarkerIfAllowed(
            InGameTrade trade, byte[] rowBytes, int[] pokedexToInternal, Species[] pokesInternal) {
        if (!Gen3RomHandler.canWriteInGameTrade(trade, pokedexToInternal, pokesInternal)) {
            return false;
        }
        rowBytes[0] = 0x55;
        return true;
    }

    private static InGameTrade trade(Species givenSpecies, Species requestedSpecies) {
        InGameTrade trade = new InGameTrade();
        trade.setGivenSpecies(givenSpecies);
        trade.setRequestedSpecies(requestedSpecies);
        return trade;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }

    private static byte[] sentinelRowBytes() {
        byte[] rowBytes = new byte[60];
        Arrays.fill(rowBytes, (byte) 0x7E);
        return rowBytes;
    }

    private static int[] mappingsFor(Species... species) {
        int maxSpeciesNumber = 0;
        for (Species sp : species) {
            maxSpeciesNumber = Math.max(maxSpeciesNumber, sp.getNumber());
        }
        int[] pokedexToInternal = new int[maxSpeciesNumber + 1];
        for (Species sp : species) {
            pokedexToInternal[sp.getNumber()] = sp.getNumber();
        }
        return pokedexToInternal;
    }

    private static Species[] internalPokes(Species... species) {
        int maxSpeciesNumber = 0;
        for (Species sp : species) {
            maxSpeciesNumber = Math.max(maxSpeciesNumber, sp.getNumber());
        }
        Species[] pokesInternal = new Species[maxSpeciesNumber + 1];
        for (Species sp : species) {
            pokesInternal[sp.getNumber()] = sp;
        }
        return pokesInternal;
    }
}
