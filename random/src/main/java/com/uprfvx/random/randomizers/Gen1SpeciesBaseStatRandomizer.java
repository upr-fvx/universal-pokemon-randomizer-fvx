package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.gamedata.basestats.Gen1BaseStats;
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

    public Gen1SpeciesBaseStatRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
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
        int bst = pk.getBaseStats().getBST() - (MIN_HP + MIN_NON_HP_STAT * 4);

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double speW = random.nextDouble(), specW = random.nextDouble();

        double totW = hpW + atkW + defW + speW + specW;

        double hp = hpW / totW * bst + MIN_HP;
        double atk = atkW / totW * bst + MIN_NON_HP_STAT;
        double def = defW / totW * bst + MIN_NON_HP_STAT;
        double spe = speW / totW * bst + MIN_NON_HP_STAT;
        double spec = specW / totW * bst + MIN_NON_HP_STAT;

        ((Gen1BaseStats) pk.getBaseStats()).setStatRatios(hp, atk, def, spe, spec);
    }

    @Override
    protected void assignNewStatsForEvolution(Species from, Species to) {
        double bstDiff = to.getBaseStats().getBST() - from.getBaseStats().getBST();

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double speW = random.nextDouble(), specW = random.nextDouble();

        double totW = hpW + atkW + defW + speW + specW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);
        double specDiff = Math.round((specW / totW) * bstDiff);

        Gen1BaseStats fromBS = (Gen1BaseStats) from.getBaseStats();

        double hp = fromBS.getHp() + hpDiff;
        double atk = fromBS.getAttack() + atkDiff;
        double def = fromBS.getDefense() + defDiff;
        double spe = fromBS.getSpeed() + speDiff;
        double spec = fromBS.getSpecial() + specDiff;

        ((Gen1BaseStats) to.getBaseStats()).setStatRatios(hp, atk, def, spe, spec);
    }

    @Override
    protected void copyRandomizedStatsUpEvolution(Species from, Species to) {
        Gen1BaseStats fromBS = (Gen1BaseStats) from.getBaseStats();
        ((Gen1BaseStats) to.getBaseStats()).setStatRatios(
                fromBS.getHp(), fromBS.getAttack(), fromBS.getDefense(), fromBS.getSpeed(), fromBS.getSpecial()
        );
    }
}
