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
    public void cfruDpePikachuVariantIdentitiesAreIrregularSpecialForms() {
        Species pikachu = species(25, "Pikachu", 1);
        Species surfingPikachu = species(25, "Pikachu", 1);
        surfingPikachu.setSpeciesSetIdentityNumber(0x43D);
        Species cosplayPikachu = species(25, "Pikachu", 1);
        cosplayPikachu.setSpeciesSetIdentityNumber(0x43F);
        Species librePikachu = species(25, "Pikachu", 1);
        librePikachu.setSpeciesSetIdentityNumber(0x440);
        Species originalCapPikachu = species(25, "Pikachu", 1);
        originalCapPikachu.setSpeciesSetIdentityNumber(0x445);
        Species partnerCapPikachu = species(25, "Pikachu", 1);
        partnerCapPikachu.setSpeciesSetIdentityNumber(0x44B);
        Species spikyEarPichu = species(172, "Pichu", 2);
        spikyEarPichu.setSpeciesSetIdentityNumber(0x44C);

        assertFalse(pikachu.isIrregularSpecialForm());
        assertTrue(surfingPikachu.isIrregularSpecialForm());
        assertTrue(cosplayPikachu.isIrregularSpecialForm());
        assertTrue(librePikachu.isIrregularSpecialForm());
        assertTrue(originalCapPikachu.isIrregularSpecialForm());
        assertTrue(partnerCapPikachu.isIrregularSpecialForm());
        assertFalse(spikyEarPichu.isIrregularSpecialForm());

        assertTrue(SpecialFormPredicates.isSpeciesAllowed(pikachu, null,
                SpecialFormExclusionOptions.defaults()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(cosplayPikachu, null,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(cosplayPikachu, null,
                SpecialFormExclusionOptions.allowAllSpecialForms()));
    }

    @Test
    public void explicitIrregularMetadataIsExcludedByDefault() {
        Species regular = species(25, "Pikachu", 1);
        Species irregular = species(7000, "Synthetic Costume Form", 1);
        irregular.addSpecialFormCategory(SpecialFormCategory.IRREGULAR);

        assertTrue(SpecialFormPredicates.isSpeciesAllowed(regular, null,
                SpecialFormExclusionOptions.defaults()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(irregular, null,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(irregular, null,
                SpecialFormExclusionOptions.allowAllSpecialForms()));
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
    public void cfruDpeRegionalIdentitiesUseRegionalGenerationWithoutDisplayNameDependence() {
        GenRestrictions gen1Only = allowOnlyGen(1);
        GenRestrictions gen2Only = allowOnlyGen(2);
        GenRestrictions gen7Only = allowOnlyGen(7);
        GenRestrictions gen8Only = allowOnlyGen(8);
        SpecialFormExclusionOptions regionalOverride = new SpecialFormExclusionOptions(false, false, true);
        Species arcanine = species(59, "Arcanine", 1);
        Species hisuianArcanine = species(59, "Arcanine", 1);
        hisuianArcanine.setSpeciesSetIdentityNumber(0x4D3);
        Species alolanRaticate = species(20, "Raticate", 1);
        alolanRaticate.setSpeciesSetIdentityNumber(0x3FD);
        Species alolanVulpix = species(37, "Vulpix", 1);
        alolanVulpix.setSpeciesSetIdentityNumber(0x401);
        Species galarianWeezing = species(110, "Weezing", 1);
        galarianWeezing.setSpeciesSetIdentityNumber(0x4C3);
        Species galarianMrMime = species(122, "Mr. Mime", 1);
        galarianMrMime.setSpeciesSetIdentityNumber(0x4C4);
        Species paldeanWooper = species(194, "Wooper", 2);
        paldeanWooper.setSpeciesSetIdentityNumber(0x584);

        assertFalse(arcanine.isRegionalForm());
        assertTrue(hisuianArcanine.isRegionalForm());
        assertTrue(alolanRaticate.isRegionalForm());
        assertTrue(alolanVulpix.isRegionalForm());
        assertTrue(galarianWeezing.isRegionalForm());
        assertTrue(galarianMrMime.isRegionalForm());
        assertTrue(paldeanWooper.isRegionalForm());

        assertTrue(SpecialFormPredicates.isSpeciesAllowed(arcanine, gen1Only,
                SpecialFormExclusionOptions.defaults()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(hisuianArcanine, gen1Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(hisuianArcanine, gen8Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(hisuianArcanine, gen1Only, regionalOverride));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(alolanRaticate, gen1Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(alolanRaticate, gen7Only,
                SpecialFormExclusionOptions.defaults()));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(alolanVulpix, gen1Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(alolanVulpix, gen7Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(alolanVulpix, gen1Only, regionalOverride));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(galarianWeezing, gen1Only, regionalOverride));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(paldeanWooper, gen1Only, regionalOverride));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(paldeanWooper, gen2Only, regionalOverride));
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
    public void cfruDpeRegionalBranchEvolutionsUseRegionalOverrideSemantics() {
        GenRestrictions gen1Only = allowOnlyGen(1);
        GenRestrictions gen8Only = allowOnlyGen(8);
        SpecialFormExclusionOptions regionalOverride = new SpecialFormExclusionOptions(false, false, true);
        Species mrRime = species(866, "Mr. Rime", 8);
        mrRime.setSpeciesSetIdentityNumber(0x486);
        Species perrserker = species(863, "Perrserker", 8);
        perrserker.setSpeciesSetIdentityNumber(0x483);
        Species clodsire = species(980, "Clodsire", 9);
        clodsire.setSpeciesSetIdentityNumber(0x560);

        assertFalse(mrRime.isRegionalForm());
        assertTrue(mrRime.isRegionalBranchEvolution());
        assertTrue(perrserker.isRegionalBranchEvolution());
        assertTrue(clodsire.isRegionalBranchEvolution());
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(mrRime, gen1Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(mrRime, gen8Only,
                SpecialFormExclusionOptions.defaults()));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(mrRime, gen1Only, regionalOverride));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(perrserker, gen1Only, regionalOverride));
        assertFalse(SpecialFormPredicates.isSpeciesAllowed(clodsire, gen1Only, regionalOverride));
        assertTrue(SpecialFormPredicates.isSpeciesAllowed(clodsire, allowOnlyGen(2), regionalOverride));
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
