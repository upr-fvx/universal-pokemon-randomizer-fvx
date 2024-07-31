package test.romhandlers;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.*;
import com.dabomstew.pkrandom.gamedata.GenRestrictions;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.gamedata.SpeciesSet;
import com.dabomstew.pkrandom.romhandlers.romentries.RomEntry;
import com.dabomstew.pkrandom.services.RestrictedSpeciesService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerMiscTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void loadingDoesNotGiveNullRomHandler(String romName) {
        loadROM(romName);
        assertNotNull(romHandler);
    }

    /**
     * Checks all ROMs found as {@link RomEntry}s in the .ini files, to see if they are is testable.
     * I.e. does the actual ROM exist within the test/roms folder.
     * <br>
     * Since running this always checks for Gen 6+ ROMs, which are very slow to handle, it is disabled by default.
     */
    @Disabled
    @ParameterizedTest
    @MethodSource("getAllRomNames")
    public void romIsTestable(String romName) {
        try {
            loadROM(romName);
            if (!Objects.equals(romHandler.getROMName(), "Pokemon " + romName)) {
                throw new RuntimeException("Rom mismatch. Wanted to load Pokemon " + romName + ", found "
                        + romHandler.getROMName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Checks all ROMs in the test/roms folder, to see if they correspond to {@link RomEntry}s in the .ini files.
     * <br>
     * Since running this may open Gen 6+ ROMs (if you have any), which are very slow to handle,
     * it is disabled by default.
     */
    @Disabled
    @ParameterizedTest
    @MethodSource("getRomNamesInFolder")
    public void romNameOfRomInFolderIsCorrect(String romName) {
        loadROM(romName);
        assertEquals("Pokemon " + romName, romHandler.getROMName());
    }

    /**
     * Checks all ROMs in the test/roms folder, to see if they pass isRomValid().
     * <br>
     * Since running this may open Gen 6+ ROMs (if you have any), which are very slow to handle,
     * it is disabled by default.
     */
    @Disabled
    @ParameterizedTest
    @MethodSource("getRomNamesInFolder")
    public void romInFolderIsValid(String romName) {
        loadROM(romName);
        assertTrue(romHandler.isRomValid());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void pokemonListIsNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getSpecies().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void firstPokemonInPokemonListIsNull(String romName) {
        loadROM(romName);
        assertNull(romHandler.getSpecies().get(0));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void numberOfPokemonInPokemonListEqualsPokemonCountConstant(String romName) {
        loadROM(romName);
        // Because Gen 7 doesn't have a pokemonCount constant really
        // Also, I personally won't be working much on those games...
        assumeFalse(romHandler.generationOfPokemon() == 7);

        int pokemonCount = getPokemonCount();
        assertEquals(pokemonCount + 1, romHandler.getSpecies().size());
    }

    private int getPokemonCount() {
        switch (romHandler.generationOfPokemon()) {
            case 1:
                return Gen1Constants.pokemonCount;
            case 2:
                return Gen2Constants.pokemonCount;
            case 3:
                return Gen3Constants.pokemonCount;
            case 4:
                return Gen4Constants.pokemonCount;
            case 5:
                return Gen5Constants.pokemonCount;
            case 6:
                return Gen6Constants.pokemonCount;
            default:
                return 0;
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void speciesSetIncludesAllNonNullSpeciesInSpeciesList(String romName) {
        loadROM(romName);
        List<Species> speciesList = romHandler.getSpecies();
        SpeciesSet speciesSet = romHandler.getSpeciesSet();
        for (Species pk : speciesList) {
            if (pk != null && !speciesSet.contains(pk)) {
                fail(pk + " in Species List (getSpecies()) but not in Species Set (getSpeciesSet())");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void speciesSetOnlyHasSpeciesAlsoInSpeciesList(String romName) {
        loadROM(romName);
        List<Species> speciesList = romHandler.getSpecies();
        for (Species pk : romHandler.getSpeciesSet()) {
            if (!speciesList.contains(pk)) {
                fail(pk + " in Species Set (getSpeciesSet()) but not in Species List (getSpecies())");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void restrictedPokemonAreSameAsSpeciesSetWithNoRestrictionsSet(String romName) {
        loadROM(romName);
        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(null);
        assertEquals(romHandler.getSpeciesSet(), rPokeService.getAll(false));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void restrictedPokemonWithNoRelativesDoesNotContainUnrelatedPokemonFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        Settings settings = new Settings();
        settings.setLimitPokemon(true);
        settings.setCurrentRestrictions(genRestrictionsFromBools(false, new int[]{1}));

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(settings);
        for (Species pk : rPokeService.getAll(false)) {
            SpeciesSet related = pk.getFamily(false);
            boolean anyFromRightGen = false;
            for (Species relative : related) {
                if (relative.getNumber() <= Gen1Constants.pokemonCount) {
                    anyFromRightGen = true;
                    break;
                }
            }
            assertTrue(anyFromRightGen, pk.getName() + " is from the wrong Gen, and is unrelated to " +
                    "Pokémon from the right (Gen I).");
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void restrictedPokemonWithNoRelativesDoesNotContainRelatedPokemonFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        Settings settings = new Settings();
        settings.setLimitPokemon(true);
        settings.setCurrentRestrictions(genRestrictionsFromBools(false, new int[]{1}));

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(settings);
        SpeciesSet restrictedPokemon = rPokeService.getAll(false);
        for (Species pk : restrictedPokemon) {
            SpeciesSet related = pk.getFamily(false);
            String fromRightGen = null;
            String fromWrongGen = null;
            for (Species relative : related) {
                if (relative.getNumber() <= Gen1Constants.pokemonCount) {
                    fromRightGen = relative.getName();
                } else if (restrictedPokemon.contains(relative)) {
                    fromWrongGen = relative.getName();
                }
            }
            if (fromRightGen != null) {
                assertNull(fromWrongGen, fromWrongGen + " is from the wrong Gen, and though it is related to " +
                        fromRightGen + " from the right (Gen I), this is not allowed.");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void restrictedPokemonWithRelativesDoesNotContainUnrelatedPokemonFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        Settings settings = new Settings();
        settings.setLimitPokemon(true);
        settings.setCurrentRestrictions(genRestrictionsFromBools(true, new int[]{1}));
        // except for the above line's "relativesAllowed: true", identical to the "WithNoRelatives" method...

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(settings);
        for (Species pk : rPokeService.getAll(false)) {
            SpeciesSet related = pk.getFamily(false);
            boolean anyFromRightGen = false;
            for (Species relative : related) {
                if (relative.getNumber() <= Gen1Constants.pokemonCount) {
                    anyFromRightGen = true;
                    break;
                }
            }
            assertTrue(anyFromRightGen, pk.getName() + " is from the wrong Gen, and is unrelated to " +
                    "Pokémon from the right (Gen I).");
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void restrictedPokemonWithRelativesAlwaysContainsRelatedPokemonFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        Settings settings = new Settings();
        settings.setLimitPokemon(true);
        settings.setCurrentRestrictions(genRestrictionsFromBools(true, new int[]{1}));

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(settings);
        SpeciesSet restrictedPokemon = rPokeService.getAll(false);
        for (Species pk : restrictedPokemon) {
            SpeciesSet related = pk.getFamily(false);
            Species fromRightGen = null;
            Species fromWrongGen = null;
            for (Species relative : related) {
                if (relative.getNumber() <= Gen1Constants.pokemonCount) {
                    fromRightGen = relative;
                } else {
                    fromWrongGen = relative;
                }
            }
            if (fromRightGen != null && fromWrongGen != null) {
                assertTrue(restrictedPokemon.contains(fromWrongGen), fromWrongGen.getName() + " is missing from " +
                        "restrictedPokemon. It is from the wrong Gen, though as it is related to " + fromRightGen.getName() +
                        " from the right (Gen I), it is allowed.");
            }
        }
    }

    private GenRestrictions genRestrictionsFromBools(boolean relativesAllowed, int[] gensAllowed) {
        int state = 0;
        for (int gen : gensAllowed) {
            state += 1 << (gen - 1);
        }
        state += relativesAllowed ? 1 << HIGHEST_GENERATION : 0;
        return new GenRestrictions(state);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canApplyAllLegalTweaksWithoutThrowing(String romName){
        loadROM(romName);

        int codeTweaksAvailable = romHandler.miscTweaksAvailable();
        List<MiscTweak> tweaksToApply = new ArrayList<>();
        for (MiscTweak mt : MiscTweak.allTweaks) {
            if ((codeTweaksAvailable & mt.getValue()) > 0) {
                tweaksToApply.add(mt);
            }
        }

        // Sort so priority is respected in tweak ordering.
        Collections.sort(tweaksToApply);

        // Now apply in order.
        for (MiscTweak mt : tweaksToApply) {
            romHandler.applyMiscTweak(mt);
        }
    }


    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allPokemonHaveAGeneration(String romName){
        loadROM(romName);

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk);
            System.out.println(pk.fullName());
            System.out.println(pk.getGeneration());
            assertNotEquals(-1, pk.getGeneration());
        }
    }

}
