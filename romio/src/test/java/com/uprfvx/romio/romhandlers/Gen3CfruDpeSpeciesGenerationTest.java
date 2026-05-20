package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.constants.SpeciesIDs;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Gen3CfruDpeSpeciesGenerationTest {

    @Test
    public void cfruDpeGenerationFallsBackToSpeciesSetIdentityForProblemNames() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler();

        Stream.of(
                problemSpecies("Stonjorner", SpeciesIDs.stonjourner, 8),
                problemSpecies("Squawkbily", SpeciesIDs.squawkabilly, 9),
                problemSpecies("Centskorch", SpeciesIDs.centiskorch, 8),
                problemSpecies("Polchgeist", SpeciesIDs.poltchageist, 9),
                problemSpecies("RoarinMoon", SpeciesIDs.roaringMoon, 9),
                problemSpecies("Enamorus", SpeciesIDs.enamorous, 8),
                problemSpecies("Flab\u00e9b\u00e9", SpeciesIDs.flabebe, 6),
                problemSpecies("Baculegion", SpeciesIDs.basculegion, 8),
                problemSpecies("BruteBonet", SpeciesIDs.bruteBonnet, 9)
        ).forEach(species -> assertEquals(species.expectedGeneration,
                generationOf(romHandler, species.species),
                species.species.getName()));
    }

    @Test
    public void vanillaGenerationStillUsesSpeciesNumber() throws Exception {
        Gen3RomHandler romHandler = baseRomHandler();
        Species species = species(25, SpeciesIDs.stonjourner, "Stonjorner");

        assertEquals(1, generationOf(romHandler, species));
    }

    private static ProblemSpecies problemSpecies(String name, int identityNumber, int expectedGeneration) {
        return new ProblemSpecies(species(25, identityNumber, name), expectedGeneration);
    }

    private static Species species(int number, int speciesSetIdentityNumber, String name) {
        Species species = new Species(number);
        species.setName(name);
        species.setSpeciesSetIdentityNumber(speciesSetIdentityNumber);
        return species;
    }

    private static int generationOf(Gen3RomHandler romHandler, Species species) {
        try {
            Method method = Gen3RomHandler.class.getDeclaredMethod("generationOf", Species.class);
            method.setAccessible(true);
            return (int) method.invoke(romHandler, species);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Gen3RomHandler cfruDpeRomHandler() throws Exception {
        Gen3RomHandler romHandler = baseRomHandler();
        Gen3RomEntry romEntry = fieldValue(romHandler, "romEntry", Gen3RomEntry.class);
        romEntry.setRomCode("BPRE");
        romEntry.putIntValue("PokemonCount", Gen3Constants.unhackedMaxPokedex + 1);
        setField(romHandler, "isRomHack", true);
        return romHandler;
    }

    private static Gen3RomHandler baseRomHandler() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        setField(romHandler, "romEntry", fireRedRomEntry());
        return romHandler;
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

    private record ProblemSpecies(Species species, int expectedGeneration) {
    }
}
