package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.romio.gamedata.*;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TradeRandomizer extends Randomizer {

    public TradeRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeIngameTrades() {
        boolean randomizeRequest = settings.getInGameTradesMod() == Settings.InGameTradesMod.RANDOMIZE_GIVEN_AND_REQUESTED;
        boolean randomNickname = settings.isRandomizeInGameTradesNicknames();
        boolean randomOT = settings.isRandomizeInGameTradesOTs();
        boolean randomStats = settings.isRandomizeInGameTradesIVs();
        boolean randomItem = settings.isRandomizeInGameTradesItems();
        CustomNamesSet customNames = getCustomNames();

        // Process trainer names
        List<String> trainerNames = new ArrayList<>();
        // Check for the file
        if (randomOT) {
            int maxOT = romHandler.maxTradeOTNameLength();
            for (String trainername : customNames.trainerNames()) {
                int len = romHandler.internalStringLength(trainername);
                if (len <= maxOT && !trainerNames.contains(trainername)) {
                    trainerNames.add(trainername);
                }
            }
        }

        // Process nicknames
        List<String> nicknames = new ArrayList<>();
        // Check for the file
        if (randomNickname) {
            int maxNN = romHandler.maxTradeNicknameLength();
            for (String nickname : customNames.pokemonNicknames()) {
                int len = romHandler.internalStringLength(nickname);
                if (len <= maxNN && !nicknames.contains(nickname)) {
                    nicknames.add(nickname);
                }
            }
        }

        // get old trades
        List<InGameTrade> trades = romHandler.getInGameTrades();
        List<Species> usedRequests = new ArrayList<>();
        List<Species> usedGivens = new ArrayList<>();
        List<String> usedOTs = new ArrayList<>();
        List<String> usedNicknames = new ArrayList<>();
        List<Item> possibleItems = new ArrayList<>(romHandler.getAllowedItems());

        int nickCount = nicknames.size();
        int trnameCount = trainerNames.size();

        for (InGameTrade trade : trades) {
            // pick new given pokemon
            Species oldgiven = trade.getGivenSpecies();
            Species given = rSpecService.randomSpecies(random);
            while (usedGivens.contains(given)) {
                given = rSpecService.randomSpecies(random);
            }
            usedGivens.add(given);
            trade.getGivenSpeciesHolder().setSpecies(given);
            randomizeGivenCosmeticForme(trade);

            // requested pokemon?
            if (oldgiven == trade.getRequestedSpecies()) {
                // preserve trades for the same pokemon
                trade.setRequestedSpecies(given);
            } else if (randomizeRequest) {
                if (trade.getRequestedSpecies() != null) {
                    Species request = rSpecService.randomSpecies(random);
                    while (usedRequests.contains(request) || request == given) {
                        request = rSpecService.randomSpecies(random);
                    }
                    usedRequests.add(request);
                    trade.setRequestedSpecies(request);
                }
            }

            // nickname?
            if (randomNickname && nickCount > usedNicknames.size()) {
                String nickname = nicknames.get(random.nextInt(nickCount));
                while (usedNicknames.contains(nickname)) {
                    nickname = nicknames.get(random.nextInt(nickCount));
                }
                usedNicknames.add(nickname);
                trade.setNickname(nickname);
            } else if (trade.getNickname().equalsIgnoreCase(oldgiven.getName())) {
                // change the name for sanity
                trade.setNickname(trade.getGivenSpecies().getName());
            }

            if (randomOT && trnameCount > usedOTs.size()) {
                String ot = trainerNames.get(random.nextInt(trnameCount));
                while (usedOTs.contains(ot)) {
                    ot = trainerNames.get(random.nextInt(trnameCount));
                }
                usedOTs.add(ot);
                trade.setOtName(ot);
                trade.setOtId(random.nextInt(65536));
            }

            if (randomStats) {
                int maxIV = romHandler.hasDVs() ? 16 : 32;
                for (int i = 0; i < trade.getIVs().length; i++) {
                    trade.getIVs()[i] = random.nextInt(maxIV);
                }
            }

            if (randomItem) {
                trade.setHeldItem(possibleItems.get(random.nextInt(possibleItems.size())));
            }
        }

        // things that the game doesn't support should just be ignored
        romHandler.setInGameTrades(trades);
        changesMade = true;
    }

    /**
     * If possible, sets the "given" Species of the given InGameTrade to a random cosmetic forme.<br>
     * Does nothing if InGameTrade doesn't allow alt formes, or if the Species doesn't have any cosmetic alt formes.
     */
    private void randomizeGivenCosmeticForme(InGameTrade trade) {
        SpeciesHolder sh = trade.getGivenSpeciesHolder();
        if (sh.isAltFormeAllowed() && sh.getSpecies().isBaseForme()) {
            Species base = sh.getSpecies().getBaseForme();
            sh.setFormeNumber(base.getRandomCosmeticFormeNumber(random));
        }
    }

}
