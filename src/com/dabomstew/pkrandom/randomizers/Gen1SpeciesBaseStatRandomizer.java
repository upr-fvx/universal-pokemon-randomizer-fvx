package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.romhandlers.RomHandler;

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
            List<Integer> stats = Arrays.asList(
                    pk.getHp(), pk.getAttack(), pk.getDefense(), pk.getSpeed(), pk.getSpecial()
            );
            pk.setHp(stats.get(order.get(0)));
            pk.setAttack(stats.get(order.get(1)));
            pk.setDefense(stats.get(order.get(2)));
            pk.setSpeed(stats.get(order.get(3)));
            pk.setSpecial(stats.get(order.get(4)));
        }
    }

    @Override
    protected void randomizeStatsWithinBST(Species pk) {
        do {
            int bst = pk.getBST() - (MIN_HP + MIN_NON_HP_STAT * 4);

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
            double specW = random.nextDouble(), speW = random.nextDouble();

            double totW = hpW + atkW + defW + specW + speW;

            pk.setHp((int) Math.max(1, Math.round(hpW / totW * bst)) + MIN_HP);
            pk.setAttack((int) Math.max(1, Math.round(atkW / totW * bst)) + MIN_NON_HP_STAT);
            pk.setDefense((int) Math.max(1, Math.round(defW / totW * bst)) + MIN_NON_HP_STAT);
            pk.setSpecial((int) Math.max(1, Math.round(specW / totW * bst)) + MIN_NON_HP_STAT);
            pk.setSpeed((int) Math.max(1, Math.round(speW / totW * bst)) + MIN_NON_HP_STAT);

            // Re-roll if the stats become something we can't store
        } while (pk.getHp() > 255 || pk.getAttack() > 255 || pk.getDefense() > 255 || pk.getSpecial() > 255
                || pk.getSpeed() > 255);
    }

    @Override
    protected void assignNewStatsForEvolution(Species from, Species to) {
        double bstDiff = to.getBST() - from.getBST();

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double specW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + specW + speW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double specDiff = Math.round((specW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        to.setHp((int) Math.min(255, Math.max(1, from.getHp() + hpDiff)));
        to.setAttack((int) Math.min(255, Math.max(1, from.getAttack() + atkDiff)));
        to.setDefense((int) Math.min(255, Math.max(1, from.getDefense() + defDiff)));
        to.setSpeed((int) Math.min(255, Math.max(1, from.getSpeed() + speDiff)));
        to.setSpecial((int) Math.min(255, Math.max(1, from.getSpecial() + specDiff)));
    }

    @Override
    protected void copyRandomizedStatsUpEvolution(Species from, Species to) {
        double bstRatio = (double) to.getBST() / (double) from.getBST();

        to.setHp((int) Math.min(255, Math.max(1, Math.round(from.getHp() * bstRatio))));
        to.setAttack((int) Math.min(255, Math.max(1, Math.round(from.getAttack() * bstRatio))));
        to.setDefense((int) Math.min(255, Math.max(1, Math.round(from.getDefense() * bstRatio))));
        to.setSpeed((int) Math.min(255, Math.max(1, Math.round(from.getSpeed() * bstRatio))));
        to.setSpecial((int) Math.min(255, Math.max(1, Math.round(from.getSpecial() * bstRatio))));
    }
}
