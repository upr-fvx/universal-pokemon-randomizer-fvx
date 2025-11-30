package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.constants.*;
import com.dabomstew.pkromio.gamedata.GenRestrictions;
import com.dabomstew.pkromio.gamedata.MegaEvolution;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.SpeciesSet;
import com.dabomstew.pkromio.romhandlers.romentries.RomEntry;
import com.dabomstew.pkromio.services.RestrictedSpeciesService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
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
        assertTrue(romHandler.isRomValid(System.out));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void romIsValid(String romName) {
        loadROM(romName);
        assertTrue(romHandler.isRomValid(System.out));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void speciesListIsNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getSpecies().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void firstSpeciesInSpeciesListIsNull(String romName) {
        loadROM(romName);
        assertNull(romHandler.getSpecies().get(0));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void numberOfSpeciesInSpeciesListEqualsPokemonCountConstant(String romName) {
        loadROM(romName);
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
                return Gen7Constants.getPokemonCount(romHandler.getROMType());
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
    public void restrictedSpeciesAreSameAsSpeciesSetWithNoRestrictionsSet(String romName) {
        loadROM(romName);
        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(new GenRestrictions());
        assertEquals(romHandler.getSpeciesSet(), rPokeService.getAll(false));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void restrictedSpeciesWithNoRelativesDoesNotContainUnrelatedSpeciesFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(genRestrictionsFromBools(false, new int[]{1}));
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
    public void restrictedSpeciesWithNoRelativesDoesNotContainRelatedSpeciesFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(genRestrictionsFromBools(false, new int[]{1}));
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
    public void restrictedSpeciesWithRelativesDoesNotContainUnrelatedSpeciesFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(genRestrictionsFromBools(true, new int[]{1}));
        // except for the above line's "relativesAllowed: true", identical to the "WithNoRelatives" method...
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
    public void restrictedSpeciesWithRelativesAlwaysContainsRelatedSpeciesFromWrongGeneration(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.generationOfPokemon() >= 2);

        RestrictedSpeciesService rPokeService = romHandler.getRestrictedSpeciesService();
        rPokeService.setRestrictions(genRestrictionsFromBools(true, new int[]{1}));
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
    public void canApplyAllLegalTweaksWithoutThrowing(String romName) {
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
    public void allSpeciesHaveAGeneration(String romName) {
        loadROM(romName);

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk);
            System.out.println(pk.getFullName());
            System.out.println(pk.getGeneration());
            assertNotEquals(-1, pk.getGeneration());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void megaEvosShareNameWithBaseFormes(String romName) {
        loadROM(romName);

        for (MegaEvolution mev : romHandler.getMegaEvolutions()) {
            System.out.println(mev);
            assertEquals(mev.getFrom().getName(), mev.getTo().getName());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void megaEvosNeedItemOfAppropriateName(String romName) {
        // i.e. Venusaur is linked to Venusaurite, Sableye to Sablenite
        loadROM(romName);

        for (MegaEvolution mev : romHandler.getMegaEvolutions()) {
            System.out.println(mev);
            if (mev.isNeedsItem()) {
                String prefix = mev.getFrom().getName().substring(0, 3);
                String itemName = mev.getItem().getName();
                assertTrue(itemName.startsWith(prefix));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void actuallyCosmeticAndIsCosmeticFormeMatch(String romName) {
        loadROM(romName);

        SpeciesSet mismatched = new SpeciesSet();

        System.out.println("Cosmetic replacements which are cosmetic formes: ");

        for (Species forme : romHandler.getSpeciesSetInclFormes()) {
            if (forme.isCosmeticReplacement() != forme.isActuallyCosmetic()) {
                mismatched.add(forme);
            }
            if (forme.isCosmeticReplacement() && forme.isActuallyCosmetic()) {
                System.out.print(forme.getFullName());
                if (forme.getName().equals(forme.getFullName())) {
                    System.out.print(" " + forme.getFormeNumber());
                }
                System.out.println();
            }
        }
        System.out.println();

        if (!mismatched.isEmpty()) {
            for (Species forme : mismatched) {
                System.out.println(forme.getFullName() +
                        (forme.getFormeSuffix().isEmpty() ? " " + forme.getFormeNumber() : "")
                        + ": isCosmeticReplacement = " + forme.isCosmeticReplacement()
                        + "; isActuallyCosmetic = " + forme.isActuallyCosmetic());
            }
            //Assumptions.abort();
            //This test isn't really meant to be passed, so much as it's meant to be informative.
        }
    }

}
