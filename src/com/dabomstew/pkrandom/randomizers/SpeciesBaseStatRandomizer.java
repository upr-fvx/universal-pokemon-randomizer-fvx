package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.gamedata.ExpCurve;
import com.dabomstew.pkromio.gamedata.MegaEvolution;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.SpeciesSet;
import com.dabomstew.pkromio.gamedata.cueh.BasicSpeciesAction;
import com.dabomstew.pkromio.gamedata.cueh.EvolvedSpeciesAction;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Random;

public class SpeciesBaseStatRandomizer extends Randomizer {

    public SpeciesBaseStatRandomizer(RomHandler romHandler, Settings settings, Random random) {
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
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() > 1)
                    continue;
                megaEvo.getTo().copyShuffledStatsUpEvolution(megaEvo.getFrom());
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
