package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.InGameTrade;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void cfruDpeTradeSpeciesWriteUsesSpeciesSetIdentity() {
        Species rapidash = species(78, "Rapidash");
        rapidash.setSpeciesSetIdentityNumber(79);
        Species highInternalGiven = species(77, "Highmon");
        highInternalGiven.setSpeciesSetIdentityNumber(1200);
        int[] pokedexToInternal = new int[100];
        pokedexToInternal[rapidash.getNumber()] = 79;
        pokedexToInternal[highInternalGiven.getNumber()] = 77;

        assertEquals(77, Gen3RomHandler.getInGameTradeInternalSpeciesId(highInternalGiven, pokedexToInternal, false));
        assertEquals(1200, Gen3RomHandler.getInGameTradeInternalSpeciesId(highInternalGiven, pokedexToInternal, true));
    }

    @Test
    public void cfruDpeTradeWriteGuardAcceptsSpeciesByInternalIdentity() {
        Species requested = species(78, "Rapidash");
        requested.setSpeciesSetIdentityNumber(79);
        Species given = species(77, "Highmon");
        given.setSpeciesSetIdentityNumber(1200);
        InGameTrade trade = trade(given, requested);

        int[] pokedexToInternal = new int[100];
        pokedexToInternal[requested.getNumber()] = 79;
        pokedexToInternal[given.getNumber()] = 77;
        Species[] pokesInternal = new Species[1201];
        pokesInternal[79] = requested;
        pokesInternal[1200] = given;

        assertFalse(Gen3RomHandler.canWriteInGameTrade(trade, pokedexToInternal, pokesInternal));
        assertTrue(Gen3RomHandler.canWriteInGameTrade(trade, pokedexToInternal, pokesInternal, true));
    }

    @Test
    public void cfruDpeTradeReadDecodesRawInternalGivenSpecies() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("TradeTableOffset", 0x100);
        romEntry.putIntValue("TradeTableSize", 1);
        romEntry.putArrayValue("TradesUnused", new int[0]);

        Species requested = species(78, "Rapidash");
        requested.setSpeciesSetIdentityNumber(79);
        Species given = species(77, "Highmon");
        given.setSpeciesSetIdentityNumber(1200);
        Species[] pokesInternal = new Species[1201];
        pokesInternal[79] = requested;
        pokesInternal[1200] = given;

        byte[] rom = new byte[0x160];
        Arrays.fill(rom, (byte) 0xFF);
        writeWord(rom, 0x100 + 12, 1200);
        writeWord(rom, 0x100 + 40, 0);
        writeWord(rom, 0x100 + 56, 79);

        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "rom", rom);
        setField(romHandler, "pokesInternal", pokesInternal);
        setField(romHandler, "items", Collections.singletonList((Item) null));
        romHandler.initTextTables();

        InGameTrade decoded = romHandler.getInGameTrades().get(0);

        assertSame(given, decoded.getGivenSpecies());
        assertSame(requested, decoded.getRequestedSpecies());
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

    private static void writeWord(byte[] rom, int offset, int value) {
        rom[offset] = (byte) value;
        rom[offset + 1] = (byte) (value >> 8);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
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
