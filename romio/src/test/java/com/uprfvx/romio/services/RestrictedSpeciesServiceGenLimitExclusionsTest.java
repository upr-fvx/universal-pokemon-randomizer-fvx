package com.uprfvx.romio.services;

import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.MegaEvolution;
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
    public void genRestrictionsExcludeUnknownGenerationSpecies() {
        Species known = species(1, "Known", 1);
        Species unknown = species(99, "Unknown", -1);
        RestrictedSpeciesService service = serviceFor(List.of(known, unknown), List.of(), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());

        assertTrue(service.getAll(true).contains(known));
        assertFalse(service.getAll(true).contains(unknown));
    }

    @Test
    public void formesUseBaseFormeGenerationAndAltFormToggleStillFiltersThem() {
        Species base = species(1, "Base", 1);
        Species forme = species(1001, "Base-Forme", 9);
        forme.setBaseForme(base);
        forme.setFormeNumber(1);
        RestrictedSpeciesService service = serviceFor(List.of(base, forme), List.of(forme), List.of());

        service.setRestrictions(new GenRestrictionsBuilder().allowOnlyGen(1).withoutEvolutionaryRelatives().build());

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
