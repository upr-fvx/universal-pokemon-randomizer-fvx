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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradeDecisionTest {

    @Test
    public void tradeRandomizationKeepsGivenSpeciesDecodedAndInPool() {
        Species requested = species(78, "Rapidash");
        Species oldGiven = species(31, "Nidorina");
        Species candidate = species(283, "Surskit");
        SpeciesSet speciesSet = speciesSet(requested, oldGiven, candidate);
        InGameTrade trade = trade(oldGiven, requested);
        TradeTestRomHandler handler = TradeTestRomHandler.create(List.of(trade), speciesSet);

        TradeRandomizer randomizer = new TradeRandomizer(handler.proxy, inGameTradeSettings(), new Random(3));
        randomizer.randomizeIngameTrades();

        assertTrue(randomizer.isChangesMade());
        assertTrue(handler.setInGameTradesCalled);
        assertNotNull(trade.getGivenSpecies());
        assertTrue(speciesSet.contains(trade.getGivenSpecies()));
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
        trade.setNickname(givenSpecies.getName());
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
        private boolean setInGameTradesCalled;

        private TradeTestRomHandler(List<InGameTrade> trades, SpeciesSet speciesSet) {
            this.trades = trades;
            this.speciesSet = speciesSet;
        }

        private static TradeTestRomHandler create(List<InGameTrade> trades, SpeciesSet speciesSet) {
            TradeTestRomHandler handler = new TradeTestRomHandler(trades, speciesSet);
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] {RomHandler.class}, handler);
            handler.restrictedSpeciesService = new RestrictedSpeciesService(handler.proxy);
            handler.restrictedSpeciesService.setRestrictions(null);
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
                    setInGameTradesCalled = true;
                    yield null;
                }
                case "getSpeciesSetInclFormes", "getSpeciesSet" -> speciesSet;
                case "getSpeciesInclFormes", "getSpecies" -> speciesSet.stream().sorted().toList();
                case "getAltFormes" -> new SpeciesSet();
                case "getMegaEvolutions" -> Collections.emptyList();
                case "getAllowedItems" -> Collections.emptySet();
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
