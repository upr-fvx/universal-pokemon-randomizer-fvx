package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3CatchingTutorialSpeciesMappingTest {

    private static final int OPPONENT_OFFSET = 0x20;
    private static final int PLAYER_OFFSET = 0x40;

    @Test
    public void cfruDpeCatchingTutorialUsesInternalSpeciesIdentity() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler();
        Species opponent = species(25, 200);
        Species player = species(26, 444);

        assertTrue(romHandler.setCatchingTutorial(opponent, player));

        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        assertArrayEquals(new byte[] {(byte) 200, 0x21}, bytes(rom, OPPONENT_OFFSET, 2));
        assertArrayEquals(new byte[] {(byte) 0xFF, 0x21, (byte) (444 - 0xFF), 0x31},
                bytes(rom, PLAYER_OFFSET, 4));
    }

    @Test
    public void cfruDpeCatchingTutorialRejectsSpeciesZeroWithoutWriting() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler();
        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        byte[] originalOpponentBytes = bytes(rom, OPPONENT_OFFSET, 4);
        byte[] originalPlayerBytes = bytes(rom, PLAYER_OFFSET, 4);

        assertFalse(romHandler.setCatchingTutorial(species(25, 0), species(26, 444)));

        assertArrayEquals(originalOpponentBytes, bytes(rom, OPPONENT_OFFSET, 4));
        assertArrayEquals(originalPlayerBytes, bytes(rom, PLAYER_OFFSET, 4));
    }

    @Test
    public void cfruDpeCatchingTutorialRejectsInvalidPlayerWithoutPartialWrite() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler();
        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        byte[] originalOpponentBytes = bytes(rom, OPPONENT_OFFSET, 4);

        assertFalse(romHandler.setCatchingTutorial(species(25, 200), species(26, 0)));

        assertArrayEquals(originalOpponentBytes, bytes(rom, OPPONENT_OFFSET, 4));
    }

    @Test
    public void vanillaCatchingTutorialStillUsesPokedexToInternalMapping() throws Exception {
        Gen3RomHandler romHandler = vanillaRomHandler();
        Species opponent = species(25, 200);
        Species player = species(26, 444);

        assertTrue(romHandler.setCatchingTutorial(opponent, player));

        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        assertArrayEquals(new byte[] {19, 0x21}, bytes(rom, OPPONENT_OFFSET, 2));
        assertArrayEquals(new byte[] {29, 0x21, (byte) 0xC0, 0x46}, bytes(rom, PLAYER_OFFSET, 4));
    }

    private static Gen3RomHandler cfruDpeRomHandler() throws Exception {
        Gen3RomHandler romHandler = baseRomHandler();
        Gen3RomEntry romEntry = fieldValue(romHandler, "romEntry", Gen3RomEntry.class);
        romEntry.setRomCode("BPRE");
        romEntry.putIntValue("PokemonCount", Gen3Constants.unhackedMaxPokedex + 1);
        setField(romHandler, "isRomHack", true);
        return romHandler;
    }

    private static Gen3RomHandler vanillaRomHandler() throws Exception {
        return baseRomHandler();
    }

    private static Gen3RomHandler baseRomHandler() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = fireRedRomEntry();
        romEntry.putIntValue("CatchingTutorialOpponentMonOffset", OPPONENT_OFFSET);
        romEntry.putIntValue("CatchingTutorialPlayerMonOffset", PLAYER_OFFSET);

        byte[] rom = new byte[0x80];
        for (int i = 0; i < rom.length; i++) {
            rom[i] = (byte) 0x7F;
        }
        int[] pokedexToInternal = new int[64];
        pokedexToInternal[25] = 19;
        pokedexToInternal[26] = 29;

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "pokedexToInternal", pokedexToInternal);
        return romHandler;
    }

    private static Species species(int number, int speciesSetIdentityNumber) {
        Species species = new Species(number);
        species.setName("Species" + number);
        species.setSpeciesSetIdentityNumber(speciesSetIdentityNumber);
        return species;
    }

    private static byte[] bytes(byte[] rom, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(rom, offset, result, 0, length);
        return result;
    }

    private static Gen3RomEntry fireRedRomEntry() throws Exception {
        for (Gen3RomEntry entry : Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini")) {
            if ("Fire Red (U) 1.0".equals(entry.getName())) {
                return new Gen3RomEntry(entry);
            }
        }
        throw new IllegalStateException("Fire Red (U) 1.0 ROM entry not found");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> T fieldValue(Object target, String name, Class<T> fieldType) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return fieldType.cast(field.get(target));
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
