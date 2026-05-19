package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.MegaEvolution;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntroPokemonDecisionTest {

    @Test
    public void randomizeIntroPokemonRetriesInvalidSpeciesAndNeverAcceptsSpeciesZero() {
        Species invalid = species(0, 0, "Invalid");
        Species valid = species(25, 25, "Pikachu");
        IntroTestRomHandler handler = IntroTestRomHandler.create(List.of(invalid, valid));
        IntroPokemonRandomizer randomizer = new IntroPokemonRandomizer(handler.proxy, new Settings(), new Random(0));

        randomizer.randomizeIntroPokemon();

        assertSame(valid, handler.acceptedIntroSpecies);
        assertSame(valid, randomizer.getIntroSpecies());
        assertEquals(List.of(invalid, valid), handler.introAttempts);
    }

    @Test
    public void randomizeIntroPokemonThrowsInsteadOfLoopingWhenNoValidSpeciesExists() {
        Species invalid = species(0, 0, "Invalid");
        IntroTestRomHandler handler = IntroTestRomHandler.create(List.of(invalid));
        IntroPokemonRandomizer randomizer = new IntroPokemonRandomizer(handler.proxy, new Settings(), new Random(0));

        assertThrows(IllegalStateException.class, randomizer::randomizeIntroPokemon);
        assertEquals(List.of(invalid), handler.introAttempts);
    }

    private static Species species(int number, int identityNumber, String name) {
        Species species = new Species(number);
        species.setSpeciesSetIdentityNumber(identityNumber);
        species.setName(name);
        return species;
    }

    private static class IntroTestRomHandler implements InvocationHandler {
        private final List<Species> species;
        private final SpeciesSet speciesSet;
        private final List<Species> introAttempts = new ArrayList<>();
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private Species acceptedIntroSpecies;

        private IntroTestRomHandler(List<Species> species) {
            this.species = species;
            this.speciesSet = new SpeciesSet(species);
        }

        private static IntroTestRomHandler create(List<Species> species) {
            IntroTestRomHandler handler = new IntroTestRomHandler(species);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] { RomHandler.class }, handler);
            handler.restrictedSpeciesService = new RestrictedSpeciesService(handler.proxy);
            handler.typeService = new TypeService(handler.proxy);
            handler.restrictedSpeciesService.setRestrictions(null);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService" -> restrictedSpeciesService;
                case "getTypeService" -> typeService;
                case "getSpeciesSetInclFormes", "getSpeciesSet" -> speciesSet;
                case "getSpeciesInclFormes", "getSpecies" -> species;
                case "getAltFormes", "getIrregularFormes" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.<MegaEvolution>emptyList();
                case "setIntroPokemon" -> setIntroPokemon((Species) args[0]);
                case "toString" -> "IntroTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private boolean setIntroPokemon(Species species) {
            introAttempts.add(species);
            if (species.getSpeciesSetIdentityNumber() <= 0) {
                return false;
            }
            acceptedIntroSpecies = species;
            return true;
        }
    }
}
