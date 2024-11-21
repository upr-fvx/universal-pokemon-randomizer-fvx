package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.gamedata.cueh.BasicSpeciesAction;
import com.dabomstew.pkrandom.gamedata.cueh.EvolvedSpeciesAction;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Random;

public class SpeciesBaseStatRandomizer extends Randomizer {

    public SpeciesBaseStatRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void shuffleSpeciesStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();

        copyUpEvolutionsHelper.apply(evolutionSanity, false,
                pk -> pk.shuffleStats(random),
                (evFrom, evTo, toMonIsFinalEvo) -> evTo.copyShuffledStatsUpEvolution(evFrom));

        romHandler.getSpeciesSetInclFormes().filter(Species::isActuallyCosmetic)
                .forEach(pk -> pk.copyBaseFormeBaseStats(pk.getBaseForme()));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.from.getMegaEvolutionsFrom().size() > 1)
                    continue;
                megaEvo.to.copyShuffledStatsUpEvolution(megaEvo.from);
            }
        }
        changesMade = true;
    }

    public void randomizeSpeciesStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();
        boolean assignEvoStatsRandomly = settings.isAssignEvoStatsRandomly();

        BasicSpeciesAction<Species> bpAction = pk -> pk.randomizeStatsWithinBST(random);
        EvolvedSpeciesAction<Species> randomEpAction = (evFrom, evTo, toMonIsFinalEvo) -> evTo
                .assignNewStatsForEvolution(evFrom, random);
        EvolvedSpeciesAction<Species> copyEpAction = (evFrom, evTo, toMonIsFinalEvo) -> evTo
                .copyRandomizedStatsUpEvolution(evFrom);

        copyUpEvolutionsHelper.apply(evolutionSanity, true, bpAction,
                assignEvoStatsRandomly ? randomEpAction : copyEpAction, randomEpAction, bpAction);

        romHandler.getSpeciesSetInclFormes().filter(Species::isActuallyCosmetic)
                .forEach(pk -> pk.copyBaseFormeBaseStats(pk.getBaseForme()));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.from.getMegaEvolutionsFrom().size() > 1 || assignEvoStatsRandomly) {
                    megaEvo.to.assignNewStatsForEvolution(megaEvo.from, random);
                } else {
                    megaEvo.to.copyRandomizedStatsUpEvolution(megaEvo.from);
                }
            }
        }
        changesMade = true;
    }

    public void standardizeEXPCurves() {
        SettingsManager.ExpCurveMod mod = settings.getExpCurveMod();
        ExpCurve expCurve = settings.getSelectedEXPCurve();

        SpeciesSet pokes = romHandler.getSpeciesSetInclFormes();
        switch (mod) {
            case LEGENDARIES:
                for (Species pk : pokes) {
                    pk.setGrowthCurve(pk.isLegendary() ? ExpCurve.SLOW : expCurve);
                }
                break;
            case STRONG_LEGENDARIES:
                for (Species pk : pokes) {
                    pk.setGrowthCurve(pk.isStrongLegendary() ? ExpCurve.SLOW : expCurve);
                }
                break;
            case ALL:
                for (Species pk : pokes) {
                    pk.setGrowthCurve(expCurve);
                }
                break;
        }
    }

}
