package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.pokemon.*;
import com.dabomstew.pkrandom.pokemon.cueh.BasicPokemonAction;
import com.dabomstew.pkrandom.pokemon.cueh.EvolvedPokemonAction;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Random;

public class PokemonBaseStatRandomizer extends Randomizer {

    public PokemonBaseStatRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void shufflePokemonStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();

        copyUpEvolutionsHelper.apply(evolutionSanity, false,
                pk -> pk.shuffleStats(random),
                (evFrom, evTo, toMonIsFinalEvo) -> evTo.copyShuffledStatsUpEvolution(evFrom));

        romHandler.getPokemonSetInclFormes().filterCosmetic()
                .forEach(pk -> pk.copyBaseFormeBaseStats(pk.getBaseForme()));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() > 1)
                    continue;
                megaEvo.getTo().copyShuffledStatsUpEvolution(megaEvo.getFrom());
            }
        }
        changesMade = true;
    }

    public void randomizePokemonStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();
        boolean assignEvoStatsRandomly = settings.isAssignEvoStatsRandomly();

        BasicPokemonAction<Pokemon> bpAction = pk -> pk.randomizeStatsWithinBST(random);
        EvolvedPokemonAction<Pokemon> randomEpAction = (evFrom, evTo, toMonIsFinalEvo) -> evTo
                .assignNewStatsForEvolution(evFrom, random);
        EvolvedPokemonAction<Pokemon> copyEpAction = (evFrom, evTo, toMonIsFinalEvo) -> evTo
                .copyRandomizedStatsUpEvolution(evFrom);

        copyUpEvolutionsHelper.apply(evolutionSanity, true, bpAction,
                assignEvoStatsRandomly ? randomEpAction : copyEpAction, randomEpAction, bpAction);

        romHandler.getPokemonSetInclFormes().filterCosmetic()
                .forEach(pk -> pk.copyBaseFormeBaseStats(pk.getBaseForme()));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() > 1 || assignEvoStatsRandomly) {
                    megaEvo.getTo().assignNewStatsForEvolution(megaEvo.getFrom(), random);
                } else {
                    megaEvo.getTo().copyRandomizedStatsUpEvolution(megaEvo.getFrom());
                }
            }
        }
        changesMade = true;
    }

    public void standardizeEXPCurves() {
        Settings.ExpCurveMod mod = settings.getExpCurveMod();
        ExpCurve expCurve = settings.getSelectedEXPCurve();

        PokemonSet pokes = romHandler.getPokemonSetInclFormes();
        switch (mod) {
            case LEGENDARIES:
                for (Pokemon pk : pokes) {
                    pk.setGrowthCurve(pk.isLegendary() ? ExpCurve.SLOW : expCurve);
                }
                break;
            case STRONG_LEGENDARIES:
                for (Pokemon pk : pokes) {
                    pk.setGrowthCurve(pk.isStrongLegendary() ? ExpCurve.SLOW : expCurve);
                }
                break;
            case ALL:
                for (Pokemon pk : pokes) {
                    pk.setGrowthCurve(expCurve);
                }
                break;
        }
    }

}
