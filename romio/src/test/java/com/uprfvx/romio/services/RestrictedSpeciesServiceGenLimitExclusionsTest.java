package com.uprfvx.romio.services;

import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.MegaEvolution;
import com.uprfvx.romio.gamedata.SpecialFormCategory;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestrictedSpeciesServiceGenLimitExclusionsTest {

    @Test
    public void genRestrictionsUseSpeciesGenerationAndCurrentRangeReachesGen9() {
        Species gen1 = species(1, "Gen1", 1);
        Species gen7 = species(7, "Gen7", 7);
        Species gen8 = species(8, "Gen8", 8);
        Species gen9 = species(9, "Gen9", 9);
        RestrictedSpeciesService service = serviceFor(List.of(gen1, gen7, gen8, gen9), List.of(), List.of());

        service.setRestrictions(null);
        assertTrue(service.getAll(true).contains(gen9));

        service.setRestrictions(new GenRestrictionsBuilder().allowAllCurrentGens().build());

        assertTrue(service.getAll(true).contains(gen1));
        assertTrue(service.getAll(true).contains(gen7));
        assertTrue(service.getAll(true).contains(gen8));
        assertTrue(service.getAll(true).contains(gen9));
    }

    @Test
    public void genRestrictionsExcludeGen8And9WhenOnlyEarlierGenerationsAreAllowed() {
        Species gen7 = species(7, "Gen7", 7);
        Species gen8 = species(8, "Gen8", 8);
        Species gen9 = species(9, "Gen9", 9);
        RestrictedSpeciesService service = serviceFor(List.of(gen7, gen8, gen9), List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(7).withoutEvolutionaryRelatives().build());

        assertTrue(service.getAll(true).contains(gen7));
        assertFalse(service.getAll(true).contains(gen8));
        assertFalse(service.getAll(true).contains(gen9));
    }

    @Test
    public void gen1OnlyExcludesLaterGenerationProblemSpeciesFromReplacementPools() {
        Species gen1 = species(1, "Shellder", 1);
        Species stonjorner = species(25, "Stonjorner", 8);
        Species squawkbily = species(26, "Squawkbily", 9);
        Species flabebe = species(27, "Flabebe", 6);
        RestrictedSpeciesService service = serviceFor(List.of(gen1, stonjorner, squawkbily, flabebe),
                List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());

        SpeciesSet replacementPool = service.getSpecies(false, false, false);
        assertTrue(replacementPool.contains(gen1));
        assertFalse(replacementPool.contains(stonjorner));
        assertFalse(replacementPool.contains(squawkbily));
        assertFalse(replacementPool.contains(flabebe));
    }

    @Test
    public void genRestrictionsExcludeUnknownGenerationSpecies() {
        Species known = species(1, "Known", 1);
        Species unknown = species(99, "Unknown", -1);
        RestrictedSpeciesService service = serviceFor(List.of(known, unknown), List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());

        assertTrue(service.getAll(true).contains(known));
        assertFalse(service.getAll(true).contains(unknown));
    }

    @Test
    public void formesUseOwnGenerationAndAltFormToggleStillFiltersThem() {
        Species base = species(1, "Base", 1);
        Species forme = species(1001, "Base-Forme", 9);
        forme.setBaseForme(base);
        forme.setFormeNumber(1);
        RestrictedSpeciesService service = serviceFor(List.of(base, forme), List.of(forme), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());

        assertFalse(service.getAll(true).contains(forme));
        assertFalse(service.getAll(false).contains(forme));

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(9).withoutEvolutionaryRelatives().build());

        assertTrue(service.getAll(true).contains(forme));
        assertFalse(service.getAll(false).contains(forme));
    }

    @Test
    public void evolutionaryRelativesCanExpandPastDirectGenerationRestriction() {
        Species gen1 = species(1, "Gen1", 1);
        Species gen9Evolution = species(901, "Gen9Evolution", 9);
        connectEvolution(gen1, gen9Evolution);
        RestrictedSpeciesService service = serviceFor(List.of(gen1, gen9Evolution), List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());
        assertFalse(service.getAll(true).contains(gen9Evolution));

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build());
        assertTrue(service.getAll(true).contains(gen9Evolution));
    }

    @Test
    public void megaEvolutionEntriesFollowAllowedTargetSpecies() {
        Species base = species(1, "Base", 1);
        Species allowedMega = species(1001, "Allowed-Mega", 1);
        Species excludedMega = species(1009, "Excluded-Mega", 9);
        MegaEvolution allowed = new MegaEvolution(base, allowedMega, true, null);
        MegaEvolution excluded = new MegaEvolution(base, excludedMega, true, null);
        RestrictedSpeciesService service = serviceFor(
                List.of(base, allowedMega, excludedMega), List.of(), List.of(allowed, excluded));

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());

        assertTrue(service.getMegaEvolutions().contains(allowed));
        assertFalse(service.getMegaEvolutions().contains(excluded));
    }

    @Test
    public void specialFormOptionsExcludeMegaAndGigantamaxFormsByDefault() {
        Species base = species(1, "Base", 1);
        Species mega = species(1001, "Base-Mega", 1);
        mega.addSpecialFormCategory(SpecialFormCategory.MEGA);
        Species gmax = species(2001, "Base-Gmax", 1);
        gmax.addSpecialFormCategory(SpecialFormCategory.GIGANTAMAX);
        MegaEvolution megaEvolution = new MegaEvolution(base, mega, true, null);
        RestrictedSpeciesService service = serviceFor(List.of(base, mega, gmax), List.of(), List.of(megaEvolution));

        service.setRestrictions(null, SpecialFormExclusionOptions.defaults());

        assertTrue(service.getAll(true).contains(base));
        assertFalse(service.getAll(true).contains(mega));
        assertFalse(service.getAll(true).contains(gmax));
        assertFalse(service.getMegaEvolutions().contains(megaEvolution));
    }

    @Test
    public void specialFormOptionsAllowMegaAndGigantamaxFormsWhenEnabled() {
        Species base = species(1, "Base", 1);
        Species mega = species(1001, "Base-Mega", 1);
        mega.addSpecialFormCategory(SpecialFormCategory.MEGA);
        Species gmax = species(2001, "Base-Gmax", 1);
        gmax.addSpecialFormCategory(SpecialFormCategory.GIGANTAMAX);
        MegaEvolution megaEvolution = new MegaEvolution(base, mega, true, null);
        RestrictedSpeciesService service = serviceFor(List.of(base, mega, gmax), List.of(), List.of(megaEvolution));

        service.setRestrictions(null, SpecialFormExclusionOptions.allowAllSpecialForms());

        assertTrue(service.getAll(true).contains(mega));
        assertTrue(service.getAll(true).contains(gmax));
        assertTrue(service.getMegaEvolutions().contains(megaEvolution));
    }

    @Test
    public void cfruDpeGigantamaxIdentitiesAreFilteredByGigantamaxOption() {
        Species pikachu = species(25, "Pikachu", 1);
        Species gigantamaxPikachu = species(25, "Pikachu", 1);
        gigantamaxPikachu.setSpeciesSetIdentityNumber(0x4F0);
        RestrictedSpeciesService service = serviceFor(List.of(pikachu, gigantamaxPikachu), List.of(), List.of());

        service.setRestrictions(null, SpecialFormExclusionOptions.defaults());

        assertTrue(service.getAll(true).contains(pikachu));
        assertFalse(service.getAll(true).contains(gigantamaxPikachu));

        service.setRestrictions(null, new SpecialFormExclusionOptions(false, true, false));

        assertTrue(service.getAll(true).contains(pikachu));
        assertTrue(service.getAll(true).contains(gigantamaxPikachu));
    }

    @Test
    public void cfruDpePikachuIrregularVariantsAreFilteredFromDefaultPools() {
        Species pikachu = species(25, "Pikachu", 1);
        Species capPikachu = species(25, "Pikachu", 1);
        capPikachu.setSpeciesSetIdentityNumber(0x445);
        Species cosplayPikachu = species(25, "Pikachu", 1);
        cosplayPikachu.setSpeciesSetIdentityNumber(0x43F);
        RestrictedSpeciesService service = serviceFor(List.of(pikachu, capPikachu, cosplayPikachu),
                List.of(), List.of());

        service.setRestrictions(null, SpecialFormExclusionOptions.defaults());

        assertTrue(service.getAll(true).contains(pikachu));
        assertFalse(service.getAll(true).contains(capPikachu));
        assertFalse(service.getAll(true).contains(cosplayPikachu));

        service.setRestrictions(null, SpecialFormExclusionOptions.allowAllSpecialForms());

        assertTrue(service.getAll(true).contains(pikachu));
        assertTrue(service.getAll(true).contains(capPikachu));
        assertTrue(service.getAll(true).contains(cosplayPikachu));
    }

    @Test
    public void regionalFormsUseOwnGenerationUnlessRegionalOverrideIsEnabled() {
        Species vulpix = species(37, "Vulpix", 1);
        Species alolanVulpix = species(10037, "Alolan Vulpix", 7);
        alolanVulpix.setBaseForme(vulpix);
        alolanVulpix.addSpecialFormCategory(SpecialFormCategory.REGIONAL);
        RestrictedSpeciesService service = serviceFor(List.of(vulpix, alolanVulpix), List.of(), List.of());
        GenRestrictions gen1Only = new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build();

        service.setRestrictions(gen1Only, SpecialFormExclusionOptions.defaults());

        assertTrue(service.getAll(true).contains(vulpix));
        assertFalse(service.getAll(true).contains(alolanVulpix));

        service.setRestrictions(gen1Only, new SpecialFormExclusionOptions(false, false, true));

        assertTrue(service.getAll(true).contains(vulpix));
        assertTrue(service.getAll(true).contains(alolanVulpix));
    }

    @Test
    public void cfruDpeRegionalIdentitiesAreFilteredFromDefaultPools() {
        Species arcanine = species(59, "Arcanine", 1);
        Species hisuianArcanine = species(59, "Arcanine", 1);
        hisuianArcanine.setSpeciesSetIdentityNumber(0x4D3);
        Species alolanRaticate = species(20, "Raticate", 1);
        alolanRaticate.setSpeciesSetIdentityNumber(0x3FD);
        Species galarianWeezing = species(110, "Weezing", 1);
        galarianWeezing.setSpeciesSetIdentityNumber(0x4C3);
        Species alolanVulpix = species(37, "Vulpix", 1);
        alolanVulpix.setSpeciesSetIdentityNumber(0x401);
        RestrictedSpeciesService service = serviceFor(
                List.of(arcanine, hisuianArcanine, alolanRaticate, galarianWeezing, alolanVulpix),
                List.of(), List.of());
        GenRestrictions gen1Only = new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build();

        service.setRestrictions(gen1Only, SpecialFormExclusionOptions.defaults());

        SpeciesSet allowedWithoutRegionalOverride = service.getAll(true);
        assertTrue(allowedWithoutRegionalOverride.contains(arcanine));
        assertFalse(allowedWithoutRegionalOverride.contains(hisuianArcanine));
        assertFalse(allowedWithoutRegionalOverride.contains(alolanRaticate));
        assertFalse(allowedWithoutRegionalOverride.contains(galarianWeezing));
        assertFalse(allowedWithoutRegionalOverride.contains(alolanVulpix));

        service.setRestrictions(gen1Only, new SpecialFormExclusionOptions(false, false, true));

        SpeciesSet allowedWithRegionalOverride = service.getAll(true);
        assertTrue(allowedWithRegionalOverride.contains(arcanine));
        assertTrue(allowedWithRegionalOverride.contains(hisuianArcanine));
        assertTrue(allowedWithRegionalOverride.contains(alolanRaticate));
        assertTrue(allowedWithRegionalOverride.contains(galarianWeezing));
        assertTrue(allowedWithRegionalOverride.contains(alolanVulpix));
    }

    @Test
    public void evolutionaryRelativesExpansionStillAllowsCrossGenerationFamilyMembers() {
        Species gen1 = species(1, "Gen1", 1);
        Species gen9Evolution = species(901, "Gen9Evolution", 9);
        connectEvolution(gen1, gen9Evolution);
        RestrictedSpeciesService service = serviceFor(List.of(gen1, gen9Evolution), List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build(),
                SpecialFormExclusionOptions.defaults());

        assertTrue(service.getAll(true).contains(gen1));
        assertTrue(service.getAll(true).contains(gen9Evolution));
    }

    @Test
    public void evolutionaryRelativesDoNotAllowRegionalFormsWithoutRegionalOverride() {
        Species electabuzz = species(125, "Electabuzz", 1);
        Species electivire = species(466, "Electivire", 4);
        connectEvolution(electabuzz, electivire);

        Species koffing = species(109, "Koffing", 1);
        Species weezing = species(110, "Weezing", 1);
        Species galarianWeezing = regionalSpecies(10110, "Galarian Weezing", 8, weezing);
        connectEvolution(koffing, weezing);
        connectEvolution(koffing, galarianWeezing);

        Species vulpix = species(37, "Vulpix", 1);
        Species alolanVulpix = regionalSpecies(10037, "Alolan Vulpix", 7, vulpix);
        connectEvolution(vulpix, alolanVulpix);

        RestrictedSpeciesService service = serviceFor(
                List.of(electabuzz, electivire, koffing, weezing, galarianWeezing, vulpix, alolanVulpix),
                List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build(),
                SpecialFormExclusionOptions.defaults());

        SpeciesSet allowed = service.getAll(true);
        assertTrue(allowed.contains(electivire));
        assertFalse(allowed.contains(galarianWeezing));
        assertFalse(allowed.contains(alolanVulpix));
    }

    @Test
    public void regionalOverrideAllowsRegionalFormsAfterEvolutionaryRelativeExpansion() {
        Species koffing = species(109, "Koffing", 1);
        Species weezing = species(110, "Weezing", 1);
        Species galarianWeezing = regionalSpecies(10110, "Galarian Weezing", 8, weezing);
        connectEvolution(koffing, weezing);
        connectEvolution(koffing, galarianWeezing);

        Species vulpix = species(37, "Vulpix", 1);
        Species alolanVulpix = regionalSpecies(10037, "Alolan Vulpix", 7, vulpix);
        connectEvolution(vulpix, alolanVulpix);

        RestrictedSpeciesService service = serviceFor(List.of(koffing, weezing, galarianWeezing, vulpix, alolanVulpix),
                List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build(),
                new SpecialFormExclusionOptions(false, false, true));

        SpeciesSet allowed = service.getAll(true);
        assertTrue(allowed.contains(galarianWeezing));
        assertTrue(allowed.contains(alolanVulpix));
    }

    @Test
    public void evolutionaryRelativesDoNotAllowRegionalBranchEvolutionsWithoutRegionalOverride() {
        Species electabuzz = species(125, "Electabuzz", 1);
        Species electivire = species(466, "Electivire", 4);
        connectEvolution(electabuzz, electivire);

        Species mrMime = species(122, "Mr. Mime", 1);
        Species galarianMrMime = regionalSpecies(10122, "Galarian Mr. Mime", 8, mrMime);
        Species mrRime = species(866, "Mr. Rime", 8);
        mrRime.setSpeciesSetIdentityNumber(0x486);
        connectEvolution(mrMime, galarianMrMime);
        connectEvolution(galarianMrMime, mrRime);

        RestrictedSpeciesService service = serviceFor(List.of(electabuzz, electivire, mrMime, galarianMrMime, mrRime),
                List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build(),
                SpecialFormExclusionOptions.defaults());

        SpeciesSet allowedWithoutRegionalOverride = service.getAll(true);
        assertTrue(allowedWithoutRegionalOverride.contains(electivire));
        assertFalse(allowedWithoutRegionalOverride.contains(galarianMrMime));
        assertFalse(allowedWithoutRegionalOverride.contains(mrRime));

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build(),
                new SpecialFormExclusionOptions(false, false, true));

        SpeciesSet allowedWithRegionalOverride = service.getAll(true);
        assertTrue(allowedWithRegionalOverride.contains(galarianMrMime));
        assertTrue(allowedWithRegionalOverride.contains(mrRime));
    }

    @Test
    public void evolutionaryRelativesDoNotBypassDisabledMegaOrGigantamaxFilters() {
        Species venusaur = species(3, "Venusaur", 1);
        Species megaVenusaur = species(10003, "Mega Venusaur", 6);
        megaVenusaur.addSpecialFormCategory(SpecialFormCategory.MEGA);
        Species gigantamaxVenusaur = species(20003, "Gigantamax Venusaur", 8);
        gigantamaxVenusaur.addSpecialFormCategory(SpecialFormCategory.GIGANTAMAX);
        Species cfruDpeGigantamaxPikachu = species(25, "Pikachu", 1);
        cfruDpeGigantamaxPikachu.setSpeciesSetIdentityNumber(0x4F0);
        connectEvolution(venusaur, megaVenusaur);
        connectEvolution(venusaur, gigantamaxVenusaur);
        connectEvolution(venusaur, cfruDpeGigantamaxPikachu);

        RestrictedSpeciesService service = serviceFor(
                List.of(venusaur, megaVenusaur, gigantamaxVenusaur, cfruDpeGigantamaxPikachu),
                List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withEvolutionaryRelatives().build(),
                SpecialFormExclusionOptions.defaults());

        SpeciesSet allowed = service.getAll(true);
        assertTrue(allowed.contains(venusaur));
        assertFalse(allowed.contains(megaVenusaur));
        assertFalse(allowed.contains(gigantamaxVenusaur));
        assertFalse(allowed.contains(cfruDpeGigantamaxPikachu));
    }

    private static RestrictedSpeciesService serviceFor(List<Species> species, List<Species> altFormes,
                                                       List<MegaEvolution> megaEvolutions) {
        TestRomHandler handler = new TestRomHandler(species, altFormes, megaEvolutions);
        return new RestrictedSpeciesService(handler.proxy());
    }

    private static Species species(int number, String name, int generation) {
        Species species = new Species(number);
        species.setName(name);
        species.setGeneration(generation);
        species.setSpeciesSetIdentityNumber(number);
        return species;
    }

    private static Species regionalSpecies(int number, String name, int generation, Species baseForme) {
        Species species = species(number, name, generation);
        species.setBaseForme(baseForme);
        species.addSpecialFormCategory(SpecialFormCategory.REGIONAL);
        return species;
    }

    private static void connectEvolution(Species from, Species to) {
        Evolution evolution = new Evolution(from, to, EvolutionType.LEVEL, 16);
        from.getEvolutionsFrom().add(evolution);
        to.getEvolutionsTo().add(evolution);
    }

    private static class GenRestrictionsBuilder {
        private final GenRestrictions restrictions = new GenRestrictions(0);

        private GenRestrictionsBuilder allowAllCurrentGens() {
            for (int gen = 1; gen <= GenRestrictions.MAX_GENERATION; gen++) {
                restrictions.setGenAllowed(gen, true);
            }
            return this;
        }

        private GenRestrictionsBuilder allowOnlyGen(int generation) {
            restrictions.setGenAllowed(generation, true);
            return this;
        }

        private GenRestrictionsBuilder withEvolutionaryRelatives() {
            restrictions.setAllowEvolutionaryRelatives(true);
            return this;
        }

        private GenRestrictionsBuilder withoutEvolutionaryRelatives() {
            restrictions.setAllowEvolutionaryRelatives(false);
            return this;
        }

        private GenRestrictions build() {
            return restrictions;
        }
    }

    private record TestRomHandler(List<Species> species, List<Species> altFormes,
                                  List<MegaEvolution> megaEvolutions) implements InvocationHandler {

        private RomHandler proxy() {
            return (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] { RomHandler.class }, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getSpeciesSetInclFormes" -> new SpeciesSet(species);
                case "getAltFormes" -> new SpeciesSet(altFormes);
                case "getMegaEvolutions" -> megaEvolutions;
                case "toString" -> "RestrictedSpeciesServiceGenLimitExclusionsTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
