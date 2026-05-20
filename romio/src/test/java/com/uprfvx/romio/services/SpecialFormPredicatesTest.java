package com.uprfvx.romio.services;

import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.SpecialFormCategory;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpecialFormPredicatesTest {

    @Test
    public void defaultOptionsExcludeMegaAndGigantamaxForms() {
        Species venusaur = species(3, "Venusaur", 1);
        Species megaVenusaur = species(1003, "Mega Venusaur", 6);
        megaVenusaur.addSpecialFormCategory(SpecialFormCategory.MEGA);
        Species gigantamaxVenusaur = species(2003, "Gigantamax Venusaur", 8);
        gigantamaxVenusaur.addSpecialFormCategory(SpecialFormCategory.GIGANTAMAX);

        assertTrue(SpecialFormPredicates.isSpeciesAllowed(venusaur, null, SpecialFormExclusionOptions.defaults()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(megaVenusaur, null, SpecialFormExclusionOptions.defaults()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(gigantamaxVenusaur, null,
                SpecialFormExclusionOptions.defaults()));

        assertTrue(SpecialFormPredicates.isSpeciesAllowed(megaVenusaur, null,
                new SpecialFormExclusionOptions(true, false, false)));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(gigantamaxVenusaur, null,
                new SpecialFormExclusionOptions(false, true, false)));
    }

    @Test
    public void cfruDpeGigantamaxIdentitiesAreRecognizedWithoutDisplayNameDependence() {
        Species pikachu = species(25, "Pikachu", 1);
        Species gigantamaxPikachu = species(25, "Pikachu", 1);
        gigantamaxPikachu.setSpeciesSetIdentityNumber(0x4F0);
        Species gigantamaxCharizard = species(6, "Charizard", 1);
        gigantamaxCharizard.setSpeciesSetIdentityNumber(0x4ED);
        Species gigantamaxVenusaur = species(3, "Venusaur", 1);
        gigantamaxVenusaur.setSpeciesSetIdentityNumber(0x4EC);
        Species gigantamaxBlastoise = species(9, "Blastoise", 1);
        gigantamaxBlastoise.setSpeciesSetIdentityNumber(0x4EE);
        Species gigantamaxMeowth = species(52, "Meowth", 1);
        gigantamaxMeowth.setSpeciesSetIdentityNumber(0x4F1);
        Species gigantamaxEevee = species(133, "Eevee", 1);
        gigantamaxEevee.setSpeciesSetIdentityNumber(0x4F6);

        assertFalse(pikachu.isGigantamaxForm());
        assertTrue(gigantamaxPikachu.isGigantamaxForm());
        assertTrue(gigantamaxCharizard.isGigantamaxForm());
        assertTrue(gigantamaxVenusaur.isGigantamaxForm());
        assertTrue(gigantamaxBlastoise.isGigantamaxForm());
        assertTrue(gigantamaxMeowth.isGigantamaxForm());
        assertTrue(gigantamaxEevee.isGigantamaxForm());

        assertFalse(SpecialFormPredicates.isSpeciesAllowed(gigantamaxPikachu, null,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(gigantamaxPikachu, null,
                new SpecialFormExclusionOptions(false, true, false)));
    }

    @Test
    public void regionalFormsUseOwnGenerationUnlessRegionalOverrideIsEnabled() {
        GenRestrictions gen1Only = allowOnlyGen(1);
        GenRestrictions gen7Only = allowOnlyGen(7);
        Species vulpix = species(37, "Vulpix", 1);
        Species alolanVulpix = regionalSpecies(10037, "Alolan Vulpix", 7, vulpix);

        assertFalse(SpecialFormPredicates.isSpeciesAllowed(alolanVulpix, gen1Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(alolanVulpix, gen7Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(alolanVulpix, gen1Only,
                new SpecialFormExclusionOptions(false, false, true)));
    }

    @Test
    public void regionalOverrideUsesBaseFamilyGenerationForKnownRegionalExamples() {
        GenRestrictions gen1Only = allowOnlyGen(1);
        GenRestrictions gen2Only = allowOnlyGen(2);
        SpecialFormExclusionOptions regionalOverride = new SpecialFormExclusionOptions(false, false, true);

        Species meowth = species(52, "Meowth", 1);
        Species galarianMeowth = regionalSpecies(10052, "Galarian Meowth", 8, meowth);
        Species growlithe = species(58, "Growlithe", 1);
        Species hisuianGrowlithe = regionalSpecies(10058, "Hisuian Growlithe", 8, growlithe);
        Species wooper = species(194, "Wooper", 2);
        Species paldeanWooper = regionalSpecies(10194, "Paldean Wooper", 9, wooper);

        assertTrue(SpecialFormPredicates.isSpeciesAllowed(galarianMeowth, gen1Only, regionalOverride));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(hisuianGrowlithe, gen1Only, regionalOverride));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(paldeanWooper, gen1Only, regionalOverride));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(paldeanWooper, gen2Only, regionalOverride));
    }

    @Test
    public void invalidSpeciesIdentityIsRejected() {
        Species invalid = species(0, "Invalid", 1);
        invalid.setSpeciesSetIdentityNumber(0);

        assertFalse(SpecialFormPredicates.isSpeciesAllowed(null, null, SpecialFormExclusionOptions.allowAllSpecialForms()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(invalid, null,
                SpecialFormExclusionOptions.allowAllSpecialForms()));
    }

    private static Species species(int number, String name, int generation) {
        Species species = new Species(number);
        species.setName(name);
        species.setGeneration(generation);
        species.setSpeciesSetIdentityNumber(number);
        return species;
    }

    private static Species regionalSpecies(int number, String name, int generation, Species baseFamily) {
        Species species = species(number, name, generation);
        species.setBaseForme(baseFamily);
        species.addSpecialFormCategory(SpecialFormCategory.REGIONAL);
        return species;
    }

    private static GenRestrictions allowOnlyGen(int generation) {
        GenRestrictions restrictions = new GenRestrictions(0);
        restrictions.setGenAllowed(generation, true);
        restrictions.setAllowEvolutionaryRelatives(false);
        return restrictions;
    }
}
