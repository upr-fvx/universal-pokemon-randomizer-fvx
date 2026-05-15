package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.InGameTrade;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradeRandomizerTest {

    @Test
    public void nullRequestedSpeciesRowsAreSkippedWithoutWriting() {
        Species safeGiven = species(1, "Bulbasaur");
        InGameTrade nullRequestedTrade = trade(safeGiven, null);
        TradeTestRomHandler romHandler = TradeTestRomHandler.create(List.of(nullRequestedTrade), speciesSet(safeGiven));
        TradeRandomizer randomizer = new TradeRandomizer(romHandler.proxy, inGameTradeSettings(), new Random(1));

        randomizer.randomizeIngameTrades();

        assertSame(safeGiven, nullRequestedTrade.getGivenSpecies());
        assertNull(nullRequestedTrade.getRequestedSpecies());
        assertEquals(1, randomizer.getSkippedNullRequestedSpeciesTrades());
        assertEquals(0, randomizer.getSkippedUnsafeSpeciesTrades());
        assertTrue(randomizer.hasSkippedTrades());
        assertFalse(randomizer.isChangesMade());
        assertEquals(0, romHandler.setInGameTradesCalls);
    }

    @Test
    public void unsafePlaceholderSpeciesRowsAreSkippedWithoutWriting() {
        Species safeRequested = species(1, "Bulbasaur");
        Species placeholderGiven = species(2, "Bad Egg");
        InGameTrade placeholderTrade = trade(placeholderGiven, safeRequested);
        TradeTestRomHandler romHandler = TradeTestRomHandler.create(List.of(placeholderTrade), speciesSet(safeRequested));
        TradeRandomizer randomizer = new TradeRandomizer(romHandler.proxy, inGameTradeSettings(), new Random(1));

        randomizer.randomizeIngameTrades();

        assertSame(placeholderGiven, placeholderTrade.getGivenSpecies());
        assertSame(safeRequested, placeholderTrade.getRequestedSpecies());
        assertEquals(0, randomizer.getSkippedNullRequestedSpeciesTrades());
        assertEquals(1, randomizer.getSkippedUnsafeSpeciesTrades());
        assertTrue(randomizer.hasSkippedTrades());
        assertFalse(randomizer.isChangesMade());
        assertEquals(0, romHandler.setInGameTradesCalls);
    }

    private static Settings inGameTradeSettings() {
        Settings settings = new Settings();
        settings.setInGameTradesMod(Settings.InGameTradesMod.RANDOMIZE_GIVEN);
        return settings;
    }

    private static InGameTrade trade(Species givenSpecies, Species requestedSpecies) {
        InGameTrade trade = new InGameTrade();
        trade.setGivenSpecies(givenSpecies);
        trade.setRequestedSpecies(requestedSpecies);
        trade.setNickname(givenSpecies == null ? "" : givenSpecies.getName());
        trade.setOtName("TEST");
        trade.setIVs(new int[] {1, 2, 3, 4, 5, 6});
        return trade;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }

    private static SpeciesSet speciesSet(Species... species) {
        SpeciesSet speciesSet = new SpeciesSet();
        for (Species sp : species) {
            speciesSet.add(sp);
        }
        return speciesSet;
    }

    private static class TradeTestRomHandler implements InvocationHandler {
        private final List<InGameTrade> trades;
        private final SpeciesSet speciesSet;
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private int setInGameTradesCalls;

        private TradeTestRomHandler(List<InGameTrade> trades, SpeciesSet speciesSet) {
            this.trades = trades;
            this.speciesSet = speciesSet;
        }

        private static TradeTestRomHandler create(List<InGameTrade> trades, SpeciesSet speciesSet) {
            TradeTestRomHandler handler = new TradeTestRomHandler(trades, speciesSet);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] {RomHandler.class}, handler);
            handler.restrictedSpeciesService = new RestrictedSpeciesService(handler.proxy);
            handler.typeService = new TypeService(handler.proxy);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService" -> restrictedSpeciesService;
                case "getTypeService" -> typeService;
                case "getInGameTrades" -> trades;
                case "setInGameTrades" -> {
                    setInGameTradesCalls++;
                    yield null;
                }
                case "getSpeciesSetInclFormes", "getSpeciesSet" -> speciesSet;
                case "getSpeciesInclFormes", "getSpecies" -> speciesList();
                case "getAltFormes" -> new SpeciesSet();
                case "getMegaEvolutions", "getAllowedItems" -> Collections.emptySet();
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private List<Species> speciesList() {
            return speciesSet.stream().sorted().toList();
        }
    }
}
