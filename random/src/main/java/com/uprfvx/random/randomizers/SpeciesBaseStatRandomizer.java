package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.basestats.BaseStats;
import com.uprfvx.romio.gamedata.basestats.ShedinjaBaseStats;
import com.uprfvx.romio.gamedata.cueh.AltFormeAction;
import com.uprfvx.romio.gamedata.cueh.BasicSpeciesAction;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.gamedata.cueh.EvolvedSpeciesAction;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;
import java.util.function.BiConsumer;

public class SpeciesBaseStatRandomizer extends Randomizer {

    protected static final int MEGA_BST_BOOST = 100;
    protected static final int SUNKERN_BST = 180;
    protected static final int ARCEUS_BST = 720;

    protected static final int MIN_HP = 20;
    protected static final int MIN_NON_HP_STAT = 10;

    public SpeciesBaseStatRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeBSTs() {
        switch (settings.getBSTMod()) {
            case RANDOM_BUFF_NERF -> randomlyModifyBSTsByPercentage();
            case SHUFFLE -> shuffleBSTs();
            case RANDOM -> fullyRandomizeBSTs();
        }
        changesMade = true;
    }

    private final AltFormeAction copyBSTAction = (baseForme, altForme) ->
            altForme.getBaseStats().setBST(baseForme.getBST(false));

    private final AltFormeAction giveMegaBoostAction = (baseForme, altForme) -> {
        if (altForme.isMegaEvolution()) {
            altForme.getBaseStats().setBST(baseForme.getBST(false) + MEGA_BST_BOOST);
        }
    };

    private void randomlyModifyBSTsByPercentage() {
        boolean evolutionSanity = settings.isBSTFollowEvolutions();
        double maxModifier = (double) settings.getBSTBuffNerfMaxPercentage() / 100;

        Map<Species, Double> modifiersBySpecies = new HashMap<>();

        BasicSpeciesAction basicSpeciesAction = pk -> {
            double modifier = random.nextDouble(1.0 - maxModifier, 1.0 + maxModifier);
            modifiersBySpecies.put(pk, modifier);
            applyBSTModifier(pk, modifier);
        };

        BiConsumer<Species, Species> copyModifierAction = (from, to) -> {
            double modifier = modifiersBySpecies.get(from);
            modifiersBySpecies.put(to, modifier);
            applyBSTModifier(to, modifier);
        };

        EvolvedSpeciesAction evolvedSpeciesAction =
                (evFrom, evTo, _) -> copyModifierAction.accept(evFrom, evTo);

        AltFormeAction altFormeAction = (baseForme, altForme) -> {
            copyModifierAction.accept(baseForme, altForme);
            giveMegaBoostAction.applyTo(baseForme, altForme);
        };

        AltFormeAction cosmeticAction = (baseForme, altForme) -> {
            // In case something evolves from a cosmetic forme
            copyBSTAction.applyTo(baseForme, altForme);
            modifiersBySpecies.put(altForme, modifiersBySpecies.get(baseForme));
        };

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(basicSpeciesAction, evolvedSpeciesAction)
                .evolutionSanity(evolutionSanity)
                .copySplitEvos(true)
                .altFormeAction(altFormeAction)
                .cosmeticAction(cosmeticAction)
                .build();
        copyUpEvolutionsHelper.apply(options);
    }

    private void applyBSTModifier(Species pk, double modifier) {
        int newBST = (int) (pk.getBST(false) * modifier);
        newBST = Math.min(newBST, pk.getBaseStats().getMaxBST());
        pk.getBaseStats().setBST(newBST);
    }

    private void shuffleBSTs() {
        // This method only shuffles the base formes, and then copies their new stats to alt formes.
        // The assumption here is that even if alt formes have evolutions of their own:
        // 1. their lines will be as long as those of the base formes, and
        // 2. if a Pokémon evolves from an alt forme, it is also an alt forme.
        // These assumptions make sense for Gens 1-7, which are supported at the time of writing...
        // but Gen 8+ will be annoying, sorry. Good luck adding support to e.g. Perrserker or Obstagoon ^^.
        // -- voliol 2026-07-24

        boolean evolutionSanity = settings.isBSTFollowEvolutions();
        boolean swapLegendaries = settings.isBSTShuffleSwapLegendaries();

        List<SpeciesSet> shuffleGroups = prepareBSTShuffleGroups(evolutionSanity, swapLegendaries);

        for (SpeciesSet group : shuffleGroups) {
            shuffleBSTsOfGroup(group, evolutionSanity);
        }

        copyShuffledBSTsToAltFormes();
    }

    private List<SpeciesSet> prepareBSTShuffleGroups(boolean evolutionSanity, boolean swapLegendaries) {
        List<SpeciesSet> shuffleGroups = new ArrayList<>();
        shuffleGroups.add(new SpeciesSet(romHandler.getSpeciesSet()));

        if (evolutionSanity) {
            shuffleGroups = splitByLineLength(shuffleGroups);
        }
        if (swapLegendaries) {
            shuffleGroups = splitByLegendaryStatus(shuffleGroups);
        }
        return shuffleGroups;
    }

    /**
     * Splits the input shuffleGroups by line length (Mewtwo->1, Paras->2, Bulbasaur->3).
     */
    private List<SpeciesSet> splitByLineLength(List<SpeciesSet> shuffleGroups) {
        List<SpeciesSet> newShuffleGroups = new ArrayList<>();
        for (SpeciesSet group : shuffleGroups) {
            Map<Integer, SpeciesSet> groupsByLineLength = new HashMap<>();
            for (Species pk : group.filterBasic(true)) {
                int lineLength = pk.getStagesAfter(true);
                groupsByLineLength.putIfAbsent(lineLength, new SpeciesSet());

                // can't use SpeciesSet.addFamily() because we only want to add species from the shuffleGroup
                SpeciesSet family = pk.getFamily(true).filter(group::contains);
                groupsByLineLength.get(lineLength).addAll(family);
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

    private void shuffleBSTsOfGroup(SpeciesSet group, boolean evolutionSanity) {
        // this definition of basic is slightly different from the CUEH's, but it should be fine
        SpeciesSet basics = evolutionSanity ? group.filterBasic(true) : group;
        Map<Species, Species> donators = new HashMap<>(basics.size());
        List<Species> keys = new ArrayList<>(basics);
        List<Species> values = new ArrayList<>(keys);
        Collections.shuffle(values, random);
        for (int i = 0; i < keys.size(); i++) {
            donators.put(keys.get(i), values.get(i));
        }

        CopyUpEvolutionsHelper cueh = new CopyUpEvolutionsHelper(group);

        BasicSpeciesAction basicSpeciesAction = pk -> {
            Species donator = donators.get(pk);
            pk.getBaseStats().setBST(donator.getBST(true));
        };

        EvolvedSpeciesAction evolvedSpeciesAction = (evFrom, evTo, _) -> {
            // Assumes lines are even; Applin could break this.
            // So could split evos where the BST differs...
            // though up until Gen 7, the only split evos where the BST differs are:
            // - Ninjask/Ninjada (which could use a special case; none implemented yet), and
            // - Poliwrath/Politoed (only 10 BST diff, can be ignored and no one will notice)
            Species fromDonator = donators.get(evFrom);
            Species toDonator = fromDonator.getEvolutionsFrom().getFirst().getTo();
            donators.put(evTo, toDonator);
            evTo.getBaseStats().setBST(toDonator.getBST(true));
        };

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(basicSpeciesAction, evolvedSpeciesAction)
                .evolutionSanity(evolutionSanity)
                .copySplitEvos(true)
                .build();
        cueh.apply(options);
    }

    private void copyShuffledBSTsToAltFormes() {
        AltFormeAction carryPercChangeAction = (baseForme, altForme) -> {
            float formeMultiplier = (float) altForme.getBST(true) / baseForme.getBST(true);
            int newBST = (int) (baseForme.getBST(false) * formeMultiplier);
            // TODO: what to do with Wishiwashi-school? It almost always maxing out is boring.
            newBST = Math.min(newBST, altForme.getBaseStats().getMaxBST());
            altForme.getBaseStats().setBST(newBST);

            giveMegaBoostAction.applyTo(baseForme, altForme);
        };

        EvolvedSpeciesAction carryPercChangeForEvolvedFormesAction = (_, to, _) -> {
            if (!to.isBaseForme()) {
                carryPercChangeAction.applyTo(to.getConceptualBaseForme(), to);
            }
        };

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(null, carryPercChangeForEvolvedFormesAction)
                .copySplitEvos(true)
                .altFormeAction(carryPercChangeAction)
                .cosmeticAction(copyBSTAction)
                .build();
        copyUpEvolutionsHelper.apply(options);
    }

    private void fullyRandomizeBSTs() {
        // This is very simple because it is a sort of chaos mode,
        // but it might be more interesting if it were to be weighted.
        // Could play around with normal, binormal distributions, etc...

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.isEssentiallyCosmetic()) continue;

            // between Sunkern and Arceus; almost all mons are between here to this day
            int newBST = random.nextInt(SUNKERN_BST, ARCEUS_BST);
            pk.getBaseStats().setBST(newBST);
        }

        // ensures cosmetic formes/megas get what they should
        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(null, null)
                .altFormeAction(giveMegaBoostAction)
                .cosmeticAction(copyBSTAction)
                .build();
        copyUpEvolutionsHelper.apply(options);
    }

    // TODO: write tests for these older randomization options

    Map<Species, List<Integer>> shuffledStatsOrders;

    public void shuffleSpeciesStats() {
        boolean evolutionSanity = settings.isBaseStatsFollowEvolutions();
        boolean megaEvolutionSanity = settings.isBaseStatsFollowMegaEvolutions();

        shuffledStatsOrders = new HashMap<>();

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(this::putShuffledStatsOrder, this::copyUpShuffledStatsOrder)
                .evolutionSanity(evolutionSanity)
                .copySplitEvos(false)
                .treatMegasAsEvos(megaEvolutionSanity)
                .cosmeticAction(this::copyUpShuffledStatsOrder)
                .build();
        copyUpEvolutionsHelper.apply(options);

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

        BasicSpeciesAction bsAction = this::randomizeStatsWithinBST;
        EvolvedSpeciesAction randomEsAction = (evFrom, evTo, _) ->
                assignNewStatsForEvolution(evFrom, evTo);
        EvolvedSpeciesAction copyEsAction = (evFrom, evTo, _) ->
                copyRandomizedStatsUpEvolution(evFrom, evTo);

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(bsAction, assignEvoStatsRandomly ? randomEsAction : copyEsAction)
                .evolutionSanity(evolutionSanity)
                .copySplitEvos(true)
                .treatMegasAsEvos(megaEvolutionSanity)
                .splitAction(randomEsAction)
                .cosmeticAction(this::copyRandomizedStatsUpEvolution)
                .build();
        copyUpEvolutionsHelper.apply(options);

        changesMade = true;
    }

    protected void randomizeStatsWithinBST(Species pk) {
        BaseStats bs = pk.getBaseStats();

        if (bs instanceof ShedinjaBaseStats shedBS) {
            randomizeStatsWithinBSTShedinja(shedBS);
        } else {
            randomizeStatsWithinBSTNormal(bs);
        }
    }

    private void randomizeStatsWithinBSTShedinja(ShedinjaBaseStats bs) {
        int bst = bs.getBST() - (MIN_NON_HP_STAT * 5);

        // Make weightings
        double atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = atkW + defW + spaW + spdW + speW;

        double atk = atkW / totW * bst + MIN_NON_HP_STAT;
        double def = defW / totW * bst + MIN_NON_HP_STAT;
        double spa = spaW / totW * bst + MIN_NON_HP_STAT;
        double spd = spdW / totW * bst + MIN_NON_HP_STAT;
        double spe = speW / totW * bst + MIN_NON_HP_STAT;

        bs.setStatRatios(atk, def, spa, spd, spe);
    }

    private void randomizeStatsWithinBSTNormal(BaseStats bs) {
        int bst = bs.getBST() - (MIN_HP + MIN_NON_HP_STAT * 5);

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + spaW + spdW + speW;

        double hp = hpW / totW * bst + MIN_HP;
        double atk = atkW / totW * bst + MIN_NON_HP_STAT;
        double def = defW / totW * bst + MIN_NON_HP_STAT;
        double spa = spaW / totW * bst + MIN_NON_HP_STAT;
        double spd = spdW / totW * bst + MIN_NON_HP_STAT;
        double spe = speW / totW * bst + MIN_NON_HP_STAT;

        bs.setStatRatios(hp, atk, def, spa, spd, spe);
    }

    protected void assignNewStatsForEvolution(Species from, Species to) {
        BaseStats fromBS = from.getBaseStats();
        BaseStats toBS = to.getBaseStats();

        if (toBS instanceof ShedinjaBaseStats shedBS) {
            assignNewStatsForEvolutionShedinja(fromBS, shedBS);
        } else {
            assignNewStatsForEvolutionNormal(fromBS, toBS);
        }
    }

    private void assignNewStatsForEvolutionShedinja(BaseStats fromBS, ShedinjaBaseStats toBS) {
        // This is conceptually so muddy, what does this mean, really?
        // And what if a mon evolves *from* Shedinja?
        // TODO: think about it more
        double bstDiff = toBS.getBST() - fromBS.getBST();

        // Make weightings
        double atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = atkW + defW + spaW + spdW + speW;

        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double spaDiff = Math.round((spaW / totW) * bstDiff);
        double spdDiff = Math.round((spdW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        double atk = fromBS.getAttack() + atkDiff;
        double def = fromBS.getDefense() + defDiff;
        double spa = fromBS.getSpatk() + spaDiff;
        double spd = fromBS.getSpdef() + spdDiff;
        double spe = fromBS.getSpeed() + speDiff;

        toBS.setStatRatios(atk, def, spa, spd, spe);
    }

    private void assignNewStatsForEvolutionNormal(BaseStats fromBS, BaseStats toBS) {
        double bstDiff = toBS.getBST() - fromBS.getBST();

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

        double hp = fromBS.getHp() + hpDiff;
        double atk = fromBS.getAttack() + atkDiff;
        double def = fromBS.getDefense() + defDiff;
        double spa = fromBS.getSpatk() + spaDiff;
        double spd = fromBS.getSpdef() + spdDiff;
        double spe = fromBS.getSpeed() + speDiff;

        toBS.setStatRatios(hp, atk, def, spa, spd, spe);
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
        for (Species pk : pokes) {
            pk.setGrowthCurve(
                    switch (mod) {
                        case LEGENDARIES -> pk.isLegendary() ? ExpCurve.SLOW : expCurve;
                        case STRONG_LEGENDARIES -> pk.isStrongLegendary() ? ExpCurve.SLOW : expCurve;
                        case ALL -> expCurve;
                    }
            );
        }
    }

}
