package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.SpeciesIDs;
import com.uprfvx.romio.gamedata.*;
import com.uprfvx.romio.gamedata.cueh.BasicSpeciesAction;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.gamedata.cueh.EvolvedSpeciesAction;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;

public class SpeciesBaseStatRandomizer extends Randomizer {

    private static final int SHEDINJA_HP = 1;
    protected static final int MIN_HP = 20;
    protected static final int MIN_NON_HP_STAT = 10;

    public SpeciesBaseStatRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void shuffleBSTs(boolean evolutionSanity, boolean swapLegendaries) {
        // TODO: deal with mega evos somehow

        List<SpeciesSet> shuffleGroups = new ArrayList<>();
        shuffleGroups.add(romHandler.getSpeciesSet());

        if (evolutionSanity) {
            shuffleGroups = splitByLineLength(shuffleGroups);
        }
        if (swapLegendaries) {
            shuffleGroups = splitByLegendaryStatus(shuffleGroups);
        }

        for (SpeciesSet group : shuffleGroups) {
            Map<Species, Species> donators = new HashMap<>(group.size());
            List<Species> keys = new ArrayList<>(group);
            List<Species> values = new ArrayList<>(keys);
            Collections.shuffle(values);
            for (int i = 0; i < keys.size(); i++) {
                donators.put(keys.get(i), values.get(i));
            }

            CopyUpEvolutionsHelper<Species> cueh = new CopyUpEvolutionsHelper<>(group);
            BasicSpeciesAction<Species> bpAction = (bp -> {
                Species donator = donators.get(bp);
                // TODO: for this to work, we need some way to access a Species' original BST (for power levels)
                bp.getBaseStats().setBSTForPowerLevels(donator.getBaseStats().getBSTForPowerLevels());
            });
            EvolvedSpeciesAction<Species> epAction = ((evFrom, evTo, _) -> {
                Species fromDonator = donators.get(evFrom);
                // assumes lines are even; Applin could break this
                // so could split evos where the BST differs
                Species toDonator = fromDonator.getEvolutionsFrom().getFirst().getTo();
                donators.put(evTo, toDonator);
                evTo.getBaseStats().setBSTForPowerLevels(toDonator.getBaseStats().getBSTForPowerLevels());
            });
            cueh.apply(evolutionSanity, true, bpAction, epAction);
        }

    }

    private List<SpeciesSet> splitByLineLength(List<SpeciesSet> shuffleGroups) {
        List<SpeciesSet> newShuffleGroups = new ArrayList<>();
        for (SpeciesSet group : shuffleGroups) {
            Map<Integer, SpeciesSet> groupsByLineLength = new HashMap<>();
            for (Species pk : group.filterBasic(true)) {
                int lineLength = pk.getStagesAfter(true);
                groupsByLineLength.putIfAbsent(lineLength, new SpeciesSet());
                groupsByLineLength.get(lineLength).add(pk);
            }
            newShuffleGroups.addAll(groupsByLineLength.values());
        }
        return newShuffleGroups;
    }

    private List<SpeciesSet> splitByLegendaryStatus(List<SpeciesSet> shuffleGroups) {
        List<SpeciesSet> newShuffleGroups = new ArrayList<>();
        for (SpeciesSet group : shuffleGroups) {
            newShuffleGroups.add(group.filter(Species::isLegendary));
            newShuffleGroups.add(group.filter(species -> !species.isLegendary()));
        }
        return newShuffleGroups;
    }


    Map<Species, List<Integer>> shuffledStatsOrders;

    public void shuffleSpeciesStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();

        shuffledStatsOrders = new HashMap<>();

        copyUpEvolutionsHelper.apply(evolutionSanity, false,
                this::putShuffledStatsOrder, this::copyUpShuffledStatsOrder);

        romHandler.getSpeciesSetInclFormes().filter(Species::isEssentiallyCosmetic)
                .forEach(pk -> copyUpShuffledStatsOrder(pk.getConceptualBaseForme(), pk));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() == 1) {
                    copyUpShuffledStatsOrder(megaEvo.getFrom(), megaEvo.getTo());
                }
            }
        }

        romHandler.getSpeciesSetInclFormes().forEach(this::applyShuffledOrderToStats);

        changesMade = true;
    }

    protected void putShuffledStatsOrder(Species pk) {
        List<Integer> order = Arrays.asList(0, 1, 2, 3, 4, 5);
        Collections.shuffle(order, random);
        shuffledStatsOrders.put(pk, order);
    }

    private void copyUpShuffledStatsOrder(Species from, Species to) {
        copyUpShuffledStatsOrder(from, to, false);
    }

    private void copyUpShuffledStatsOrder(Species from, Species to, boolean unused) {
        // has a third boolean parameter just so it can fit the EvolvedSpeciesAction functional interface
        shuffledStatsOrders.put(to, shuffledStatsOrders.get(from));
    }

    protected void applyShuffledOrderToStats(Species pk) {
        if (shuffledStatsOrders.containsKey(pk)) {
            List<Integer> order = shuffledStatsOrders.get(pk);
            BaseStats bs = pk.getBaseStats();
            List<Integer> stats = Arrays.asList(
                    bs.getHp(), bs.getAttack(), bs.getDefense(), bs.getSpatk(), bs.getSpdef(), bs.getSpeed()
            );

            pk.getBaseStats().setStatRatios(
                    stats.get(order.get(0)),
                    stats.get(order.get(1)),
                    stats.get(order.get(2)),
                    stats.get(order.get(3)),
                    stats.get(order.get(4)),
                    stats.get(order.get(5))
            );
        }
    }

    public void randomizeSpeciesStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();
        boolean assignEvoStatsRandomly = settings.isAssignEvoStatsRandomly();

        BasicSpeciesAction<Species> bpAction = this::randomizeStatsWithinBST;
        EvolvedSpeciesAction<Species> randomEpAction = (evFrom, evTo, _) ->
                assignNewStatsForEvolution(evFrom, evTo);
        EvolvedSpeciesAction<Species> copyEpAction = (evFrom, evTo, _) ->
                copyRandomizedStatsUpEvolution(evFrom, evTo);

        copyUpEvolutionsHelper.apply(evolutionSanity, true, bpAction,
                assignEvoStatsRandomly ? randomEpAction : copyEpAction, randomEpAction, bpAction);

        romHandler.getSpeciesSetInclFormes().filter(Species::isEssentiallyCosmetic)
                .forEach(pk -> pk.setBaseStats(new BaseStats((pk.getConceptualBaseForme().getBaseStats()))));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() > 1 || assignEvoStatsRandomly) {
                    assignNewStatsForEvolution(megaEvo.getFrom(), megaEvo.getTo());
                } else {
                    copyRandomizedStatsUpEvolution(megaEvo.getFrom(), megaEvo.getTo());
                }
            }
        }
        changesMade = true;
    }

    protected void randomizeStatsWithinBST(Species pk) {
        do {
            if (pk.getNumber() == SpeciesIDs.shedinja) {
                randomizeShedinjaStatsWithinBST(pk);
            } else {
                randomizeRegularStatsWithinBST(pk);
            }
            // Re-roll if the stats become something we can't store
            // TODO: this check probably doesn't work; should throw before
        } while (pk.getBaseStats().getHp() > 255
                || pk.getBaseStats().getAttack() > 255 || pk.getBaseStats().getDefense() > 255
                || pk.getBaseStats().getSpatk() > 255 || pk.getBaseStats().getSpdef() > 255
                || pk.getBaseStats().getSpeed() > 255);
    }

    private void randomizeShedinjaStatsWithinBST(Species pk) {
        // TODO: refactor to use BaseStats.setStatRatios()
        // Shedinja is horribly broken unless we restrict it to 1HP.
        int bst = pk.getBaseStats().getBST() - (SHEDINJA_HP + MIN_NON_HP_STAT * 5);

        // Make weightings
        double atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = atkW + defW + spaW + spdW + speW;

        pk.setBaseStats(new BaseStats(
                SHEDINJA_HP,
                (int) Math.max(1, Math.round(atkW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(defW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(spaW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(spdW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(speW / totW * bst)) + MIN_NON_HP_STAT,
                true
        ));
    }

    private void randomizeRegularStatsWithinBST(Species pk) {
        // TODO: refactor to use BaseStats.setStatRatios()
        int bst = pk.getBaseStats().getBST() - (MIN_HP + MIN_NON_HP_STAT * 5);

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + spaW + spdW + speW;

        pk.setBaseStats(new BaseStats(
                (int) Math.max(1, Math.round(hpW / totW * bst)) + MIN_HP,
                (int) Math.max(1, Math.round(atkW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(defW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(spaW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(spdW / totW * bst)) + MIN_NON_HP_STAT,
                (int) Math.max(1, Math.round(speW / totW * bst)) + MIN_NON_HP_STAT,
                false
        ));
    }

    protected void assignNewStatsForEvolution(Species from, Species to) {
        double bstDiff = to.getBaseStats().getBST() - from.getBaseStats().getBST();

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + spaW + spdW + speW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double spaDiff = Math.round((spaW / totW) * bstDiff);
        double spdDiff = Math.round((spdW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        BaseStats fromBS = from.getBaseStats();

        // TODO: refactor away these clamps; reroll instead
        to.setBaseStats(new BaseStats(
                (int) Math.clamp(fromBS.getHp() + hpDiff, 1, 255),
                (int) Math.clamp(fromBS.getAttack() + atkDiff, 1, 255),
                (int) Math.clamp(fromBS.getDefense() + defDiff, 1, 255),
                (int) Math.clamp(fromBS.getSpatk() + spaDiff, 1, 255),
                (int) Math.clamp(fromBS.getSpdef() + spdDiff, 1, 255),
                (int) Math.clamp(fromBS.getSpeed() + speDiff, 1, 255),
                fromBS.isShedinja()
        ));
    }

    protected void copyRandomizedStatsUpEvolution(Species from, Species to) {
        BaseStats fromBS = from.getBaseStats();
        to.getBaseStats().setStatRatios(
                fromBS.getHp(),
                fromBS.getAttack(), fromBS.getDefense(),
                fromBS.getSpatk(), fromBS.getSpdef(),
                fromBS.getSpeed()
        );
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
