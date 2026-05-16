package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Gen3EvolutionLoadDecisionTest {

    @Test
    public void loadEvolutionsSkipsNullSpeciesAndUnknownEvolutionTargets() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = new Gen3RomEntry(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini").get(0));
        romEntry.putIntValue("PokemonCount", 3);
        romEntry.putIntValue("PokemonEvolutions", 0);

        Species source = new Species(2);
        source.setName("Source");
        Species target = new Species(3);
        target.setName("Target");

        byte[] rom = new byte[0x100];
        int sourceEvolutionOffset = 2 * 0x28;
        writeWord(rom, sourceEvolutionOffset, Gen3Constants.evolutionTypeToIndex(EvolutionType.LEVEL));
        writeWord(rom, sourceEvolutionOffset + 2, 16);
        writeWord(rom, sourceEvolutionOffset + 4, 1);
        writeWord(rom, sourceEvolutionOffset + 8, Gen3Constants.evolutionTypeToIndex(EvolutionType.LEVEL));
        writeWord(rom, sourceEvolutionOffset + 10, 32);
        writeWord(rom, sourceEvolutionOffset + 12, 3);

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "speciesList", Arrays.asList(null, null, source));
        setField(romHandler, "numRealPokemon", 2);
        setField(romHandler, "pokes", new Species[] {null, null, source, target});
        setField(romHandler, "pokesInternal", new Species[] {null, null, source, target});
        setField(romHandler, "pokedexToInternal", new int[] {0, 0, 2, 3});

        romHandler.loadEvolutions();

        List<Evolution> sourceEvolutions = source.getEvolutionsFrom();
        assertEquals(1, sourceEvolutions.size());
        assertSame(target, sourceEvolutions.get(0).getTo());
        assertEquals(1, target.getEvolutionsTo().size());
        assertSame(sourceEvolutions.get(0), target.getEvolutionsTo().get(0));
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
}
