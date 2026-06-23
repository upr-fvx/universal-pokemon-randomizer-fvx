package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Gen1BaseStats;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A {@link SpeciesBaseStatRandomizer} for Generation 1, taking the unified Special stat into account.
 */
public class Gen1SpeciesBaseStatRandomizer extends SpeciesBaseStatRandomizer {

    public Gen1SpeciesBaseStatRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    @Override
    protected void putShuffledStatsOrder(Species pk) {
        List<Integer> order = Arrays.asList(0, 1, 2, 3, 4);
        Collections.shuffle(order, random);
        shuffledStatsOrders.put(pk, order);
    }

    @Override
    protected void applyShuffledOrderToStats(Species pk) {
        if (shuffledStatsOrders.containsKey(pk)) {
            List<Integer> order = shuffledStatsOrders.get(pk);
            Gen1BaseStats bs = (Gen1BaseStats) pk.getBaseStats();
            List<Integer> stats = Arrays.asList(
                    bs.getHp(), bs.getAttack(), bs.getDefense(), bs.getSpeed(), bs.getSpecial()
            );

            pk.setBaseStats(new Gen1BaseStats(
                    stats.get(order.get(0)),
                    stats.get(order.get(1)),
                    stats.get(order.get(2)),
                    stats.get(order.get(3)),
                    stats.get(order.get(4))
            ));
        }
    }

    @Override
    protected void randomizeStatsWithinBST(Species pk) {
        Gen1BaseStats bs = (Gen1BaseStats) pk.getBaseStats();
        do {
            // TODO: refactor to use BaseStats.setStatRatios()
            int bst = pk.getBaseStats().getBST() - (MIN_HP + MIN_NON_HP_STAT * 4);

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
            double speW = random.nextDouble(), specW = random.nextDouble();

            double totW = hpW + atkW + defW + specW + speW;

            pk.setBaseStats(new Gen1BaseStats(
                    (int) Math.max(1, Math.round(hpW / totW * bst)) + MIN_HP,
                    (int) Math.max(1, Math.round(atkW / totW * bst)) + MIN_NON_HP_STAT,
                    (int) Math.max(1, Math.round(defW / totW * bst)) + MIN_NON_HP_STAT,
                    (int) Math.max(1, Math.round(speW / totW * bst)) + MIN_NON_HP_STAT,
                    (int) Math.max(1, Math.round(specW / totW * bst)) + MIN_NON_HP_STAT
            ));

            // Re-roll if the stats become something we can't store
            // TODO: this check probably doesn't work; should throw before
        } while (bs.getHp() > 255 || bs.getAttack() > 255 || bs.getDefense() > 255 || bs.getSpecial() > 255
                || bs.getSpeed() > 255);
    }

    @Override
    protected void assignNewStatsForEvolution(Species from, Species to) {
        double bstDiff = to.getBaseStats().getBST() - from.getBaseStats().getBST();

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double specW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + specW + speW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double specDiff = Math.round((specW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        Gen1BaseStats fromBS = (Gen1BaseStats) from.getBaseStats();

        // TODO: refactor away these clamps; reroll instead
        to.setBaseStats(new Gen1BaseStats(
                (int) Math.clamp(fromBS.getHp() + hpDiff, 1, 255),
                (int) Math.clamp(fromBS.getAttack() + atkDiff, 1, 255),
                (int) Math.clamp(fromBS.getDefense() + defDiff, 1, 255),
                (int) Math.clamp(fromBS.getSpeed() + speDiff, 1, 255),
                (int) Math.clamp(fromBS.getSpecial() + specDiff, 1, 255)
        ));
    }

    @Override
    protected void copyRandomizedStatsUpEvolution(Species from, Species to) {
        Gen1BaseStats fromBS = (Gen1BaseStats) from.getBaseStats();
        ((Gen1BaseStats) to.getBaseStats()).setStatRatios(
                fromBS.getHp(), fromBS.getAttack(), fromBS.getDefense(), fromBS.getSpeed(), fromBS.getSpecial()
        );
    }
}
