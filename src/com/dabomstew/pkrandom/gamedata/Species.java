package com.dabomstew.pkrandom.gamedata;

/*----------------------------------------------------------------------------*/
/*--  Species.java                                                          --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.constants.SpeciesIDs;
import javafx.util.Pair;
import com.dabomstew.pkrandom.graphics.palettes.Palette;

import java.util.*;

/**
 * Represents a Pokémon species or forme.
 */
public class Species implements Comparable<Species> {

    private String name;
    private final int number;

    private String formeSuffix = "";
    private Species baseForme = null;
    private int formeNumber = 0;
    private int cosmeticForms = 0;
    private int formeSpriteIndex = 0;
    private boolean actuallyCosmetic = false;
    private List<Integer> realCosmeticFormNumbers = new ArrayList<>();

    private int generation = -1;

    private SpeciesSet originalEvolvedForms, originalPreEvolvedForms;

    private Type primaryType;
    private Type secondaryType;

    private Type originalPrimaryType;
    private Type originalSecondaryType;
    private boolean hasSetPrimaryType;
    private boolean hasSetSecondaryType;

    private int hp;
    private int attack;
    private int defense;
    private int spatk;
    private int spdef;
    private int speed;
    private int special;

    private int ability1;
    private int ability2;
    private int ability3;

    private int catchRate;
    private int expYield;

    private int guaranteedHeldItem;
    private int commonHeldItem;
    private int rareHeldItem;
    private int darkGrassHeldItem;

    private int genderRatio;

    private int frontImageDimensions;

    private int callRate;

    private ExpCurve growthCurve;
    
    private List<Palette> normalPalettes = new ArrayList<>(1);
    private List<Palette> shinyPalettes = new ArrayList<>(1);

    private List<Evolution> evolutionsFrom = new ArrayList<>();
    private List<Evolution> evolutionsTo = new ArrayList<>();

    private List<MegaEvolution> megaEvolutionsFrom = new ArrayList<>();
    private List<MegaEvolution> megaEvolutionsTo = new ArrayList<>();

    protected List<Integer> shuffledStatsOrder;

    public Species(int number) {
        this.number = number;
        shuffledStatsOrder = Arrays.asList(0, 1, 2, 3, 4, 5);
    }

    public void shuffleStats(Random random) {
        Collections.shuffle(shuffledStatsOrder, random);
        applyShuffledOrderToStats();
    }
    
    public void copyShuffledStatsUpEvolution(Species evolvesFrom) {
        // If stats were already shuffled once, un-shuffle them
        shuffledStatsOrder = Arrays.asList(
                shuffledStatsOrder.indexOf(0),
                shuffledStatsOrder.indexOf(1),
                shuffledStatsOrder.indexOf(2),
                shuffledStatsOrder.indexOf(3),
                shuffledStatsOrder.indexOf(4),
                shuffledStatsOrder.indexOf(5));
        applyShuffledOrderToStats();
        shuffledStatsOrder = evolvesFrom.shuffledStatsOrder;
        applyShuffledOrderToStats();
    }

    protected void applyShuffledOrderToStats() {
        List<Integer> stats = Arrays.asList(hp, attack, defense, spatk, spdef, speed);

        // Copy in new stats
        hp = stats.get(shuffledStatsOrder.get(0));
        attack = stats.get(shuffledStatsOrder.get(1));
        defense = stats.get(shuffledStatsOrder.get(2));
        spatk = stats.get(shuffledStatsOrder.get(3));
        spdef = stats.get(shuffledStatsOrder.get(4));
        speed = stats.get(shuffledStatsOrder.get(5));
    }

    public void randomizeStatsWithinBST(Random random) {
        if (number == SpeciesIDs.shedinja) {
            // Shedinja is horribly broken unless we restrict him to 1HP.
            int bst = getBST() - 51;

            // Make weightings
            double atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = atkW + defW + spaW + spdW + speW;

            hp = 1;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        } else {
            // Minimum 20 HP, 10 everything else
            int bst = getBST() - 70;

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = hpW + atkW + defW + spaW + spdW + speW;

            hp = (int) Math.max(1, Math.round(hpW / totW * bst)) + 20;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        }

        // Check for something we can't store
        if (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255 || speed > 255) {
            // re roll
            randomizeStatsWithinBST(random);
        }

    }

    public void copyRandomizedStatsUpEvolution(Species evolvesFrom) {
        double ourBST = getBST();
        double theirBST = evolvesFrom.getBST();

        double bstRatio = ourBST / theirBST;

        hp = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.hp * bstRatio)));
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack * bstRatio)));
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense * bstRatio)));
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed * bstRatio)));
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk * bstRatio)));
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef * bstRatio)));
    }

    public void assignNewStatsForEvolution(Species evolvesFrom, Random random) {

        double ourBST = getBST();
        double theirBST = evolvesFrom.getBST();

        double bstDiff = ourBST - theirBST;

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

        hp = (int) Math.min(255, Math.max(1, evolvesFrom.hp + hpDiff));
        attack = (int) Math.min(255, Math.max(1, evolvesFrom.attack + atkDiff));
        defense = (int) Math.min(255, Math.max(1, evolvesFrom.defense + defDiff));
        speed = (int) Math.min(255, Math.max(1, evolvesFrom.speed + speDiff));
        spatk = (int) Math.min(255, Math.max(1, evolvesFrom.spatk + spaDiff));
        spdef = (int) Math.min(255, Math.max(1, evolvesFrom.spdef + spdDiff));
    }

    protected int getBST() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    public int getBSTForPowerLevels() {
        // Take into account Shedinja's purposefully nerfed HP
        if (number == SpeciesIDs.shedinja) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return hp + attack + defense + spatk + spdef + speed;
        }
    }

    public double getAttackSpecialAttackRatio() {
        return (double)attack / ((double)attack + (double)spatk);
    }

    public int getBaseNumber() {
        Species base = this;
        while (base.baseForme != null) {
            base = base.baseForme;
        }
        return base.number;
    }

    //Evolutionary Relatives functions

    /**
     * Gets all {@link Species} that this {@link Species} can evolve directly into.
     * Does not include Mega Evolution.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all possible evolved forms of this {@link Species}.
     */
    public SpeciesSet getEvolvedSpecies(boolean useOriginal) {
        if(useOriginal) {
            if(this.originalEvolvedForms == null) {
                return new SpeciesSet();
            } else {
                return new SpeciesSet(this.originalEvolvedForms);
            }
        } else {
            SpeciesSet evolvedSpecies = new SpeciesSet();
            for (Evolution evo : evolutionsFrom) {
                evolvedSpecies.add(evo.getTo());
            }
            return evolvedSpecies;
        }
    }

    /**
     * Gets all {@link Species} that can evolve directly into this {@link Species}.
     * Does not include Mega Evolution.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A new {@link SpeciesSet} containing all pre-evolved forms of this {@link Species}.
     */
    public SpeciesSet getPreEvolvedSpecies(boolean useOriginal) {
        if(useOriginal) {
            if(this.originalPreEvolvedForms == null) {
                return new SpeciesSet();
            } else {
                return new SpeciesSet(this.originalPreEvolvedForms);
            }
        } else {
            SpeciesSet evolvedSpecies = new SpeciesSet();
            for (Evolution evo : evolutionsTo) {
                evolvedSpecies.add(evo.getFrom());
            }
            return evolvedSpecies;
        }
    }

    /**
     * Gets all {@link Species} that this {@link Species} is related to by evolution.
     * Does not include Mega Evolution.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a {@link SpeciesSet} containing all {@link Species} this {@link Species} is related to (including itself)
     */
    public SpeciesSet getFamily(boolean useOriginal) {
        SpeciesSet family = new SpeciesSet();

        Queue<Species> toAdd = new ArrayDeque<>();
        toAdd.add(this);
        while(!toAdd.isEmpty()) {
            Species adding = toAdd.remove();
            if(family.contains(adding)) {
                continue;
            }
            family.add(adding);

            toAdd.addAll(adding.getEvolvedSpecies(useOriginal));
            toAdd.addAll(adding.getPreEvolvedSpecies(useOriginal));
        }

        return family;
    }

    /**
     * Gets the relative position of the given {@link Species} in the evolutionary family.
     * If the family is a cycle, will return the closest path. This is usually, but
     * not always, the lowest absolute value.
     * @return A number indicating the relative position of the given {@link Species}.
     *         For example, if the given {@link Species} evolves directly into this {@link Species},
     *         the number will be -1.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @throws IllegalArgumentException if the {@link Species} are not related.
     */
    public int getRelation(Species relative, boolean useOriginal) {
        Queue<Pair<Species, Integer>> toCheck = new ArrayDeque<>();
        SpeciesSet checked = new SpeciesSet();
        toCheck.add(new Pair<>(this, 0));

        while(!toCheck.isEmpty()) {
            Pair<Species, Integer> current = toCheck.remove();
            Species currentSpecies = current.getKey();
            int currentPosition = current.getValue();
            if(checked.contains(currentSpecies)) {
                continue;
            }
            checked.add(currentSpecies);

            if(currentSpecies == relative) {
                return currentPosition;
            }

            for(Species evo : currentSpecies.getEvolvedSpecies(useOriginal)) {
                toCheck.add(new Pair<>(evo, currentPosition + 1));
            }
            for(Species evo : currentSpecies.getPreEvolvedSpecies(useOriginal)) {
                toCheck.add(new Pair<>(evo, currentPosition - 1));
            }
        }

        throw new IllegalArgumentException("Cannot find relation of a non-related {@link Species}!");
    }

    /**
     * Gets all {@link Species} related to this one that are at the given relative position, evolution-wise,
     * from this {@link Species}, including those on different branches.
     * For example, a value of +1 on a Cascoon would give both Dustox and Beautifly.
     * @param position The relative position to find.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} including all {@link Species} at this relative position, or an empty set if
     *         there are none.
     */
    public SpeciesSet getRelativesAtPosition(int position, boolean useOriginal) {
        Queue<Pair<Species, Integer>> toCheck = new ArrayDeque<>();
        SpeciesSet checked = new SpeciesSet();
        SpeciesSet relatives = new SpeciesSet();
        toCheck.add(new Pair<>(this, 0));

        while(!toCheck.isEmpty()) {
            Pair<Species, Integer> current = toCheck.remove();
            Species currentSpecies = current.getKey();
            int currentPosition = current.getValue();
            if(checked.contains(currentSpecies)) {
                continue;
            }
            checked.add(currentSpecies);

            if(currentPosition == position) {
                relatives.add(currentSpecies);
            }

            for(Species evo : currentSpecies.getEvolvedSpecies(useOriginal)) {
                toCheck.add(new Pair<>(evo, currentPosition + 1));
            }
            for(Species evo : currentSpecies.getPreEvolvedSpecies(useOriginal)) {
                toCheck.add(new Pair<>(evo, currentPosition - 1));
            }
        }

        return relatives;
    }

    /**
     * Gets all {@link Species} related to this one by the given number of evolutions (or pre-evolutions,
     * if position is negative).
     * Does not include those on different branches.
     * @param position The relative position to find.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} including all {@link Species} at this relative position, or an empty set if
     *         there are none.
     */
    public SpeciesSet getRelativesAtPositionSameBranch(int position, boolean useOriginal) {
        SpeciesSet currentStage = new SpeciesSet(this);

        if(position == 0) {
            return currentStage;
        }

        int step = position > 0 ? 1 : -1;

        for(int i = 0; i != position; i += step) {
            SpeciesSet nextStage = new SpeciesSet();
            for(Species spec : currentStage) {
                nextStage.addAll(position > 0 ? spec.getEvolvedSpecies(useOriginal)
                        : spec.getPreEvolvedSpecies(useOriginal));
            }

            currentStage = nextStage;
        }

        return currentStage;
    }

    //TODO: improve behaviour around cycles
    //(For these and the SpeciesSet versions)
    /**
     * Gets the maximum number of times this {@link Species} can evolve into distinct {@link Species}.
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial {@link Species}.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The highest count of evolution stages after this {@link Species}.
     */
    public int getStagesAfter(boolean useOriginal) {
        int stages = 0;
        SpeciesSet currentStage = new SpeciesSet(this);
        SpeciesSet checked = new SpeciesSet();

        while(!currentStage.isEmpty()) {
            SpeciesSet nextStage = new SpeciesSet();
            for(Species spec : currentStage) {
                if(checked.contains(spec)) {
                    continue;
                }
                nextStage.addAll(this.getEvolvedSpecies(useOriginal));
                checked.add(spec);
            }
            if(!nextStage.isEmpty()) {
                stages++;
            }
            currentStage = nextStage;
        }

        return stages;
    }

    /**
     * Gets the maximum number of times a {@link Species} can evolve into distinct {@link Species} before this one.
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial {@link Species}.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The highest count of evolutionary steps before this {@link Species}.
     */
    public int getStagesBefore(boolean useOriginal) {
        int stages = 0;
        SpeciesSet currentStage = new SpeciesSet(this);
        SpeciesSet checked = new SpeciesSet();

        while(!currentStage.isEmpty()) {
            SpeciesSet previousStage = new SpeciesSet();
            for(Species spec : currentStage) {
                if(checked.contains(spec)) {
                    continue;
                }
                previousStage.addAll(this.getPreEvolvedSpecies(useOriginal));
                checked.add(spec);
            }
            if(!previousStage.isEmpty()) {
                stages++;
            }
            currentStage = previousStage;
        }

        return stages;
    }


    /**
     * Saves certain pieces of data that can be randomized, but that
     * we want to know the original version of for later randomization.
     * Currently: Evolutions. (Types are done elsewhere.)
     * Must be called before randomizing any of this data.
     */
    public void saveOriginalData() {
        //originalPrimaryType = primaryType;
        //originalSecondaryType = secondaryType;
        originalEvolvedForms = SpeciesSet.unmodifiable(getEvolvedSpecies(false));
        originalPreEvolvedForms = SpeciesSet.unmodifiable(getPreEvolvedSpecies(false));
    }

    public void copyBaseFormeBaseStats(Species baseForme) {
        hp = baseForme.hp;
        attack = baseForme.attack;
        defense = baseForme.defense;
        speed = baseForme.speed;
        spatk = baseForme.spatk;
        spdef = baseForme.spdef;
    }

    public void copyBaseFormeAbilities(Species baseForme) {
        ability1 = baseForme.ability1;
        ability2 = baseForme.ability2;
        ability3 = baseForme.ability3;
    }

    public void copyBaseFormeEvolutions(Species baseForme) {
        evolutionsFrom = baseForme.evolutionsFrom;
    }

    public int getSpriteIndex() {
        return formeNumber == 0 ? number : formeSpriteIndex + formeNumber - 1;
    }

    public String fullName() {
        return name + formeSuffix;
    }

    @Override
    public String toString() {
        return "Species [name=" + name + formeSuffix + ", number=" + number + ", primaryType=" + primaryType
                + ", secondaryType=" + secondaryType + ", hp=" + hp + ", attack=" + attack + ", defense=" + defense
                + ", spatk=" + spatk + ", spdef=" + spdef + ", speed=" + speed + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Species other = (Species) obj;
        return number == other.number;
    }

    @Override
    public int compareTo(Species o) {
        return number - o.number;
    }

    private static final List<Integer> legendaries = Arrays.asList(SpeciesIDs.articuno, SpeciesIDs.zapdos, SpeciesIDs.moltres,
            SpeciesIDs.mewtwo, SpeciesIDs.mew, SpeciesIDs.raikou, SpeciesIDs.entei, SpeciesIDs.suicune, SpeciesIDs.lugia, SpeciesIDs.hoOh,
            SpeciesIDs.celebi, SpeciesIDs.regirock, SpeciesIDs.regice, SpeciesIDs.registeel, SpeciesIDs.latias, SpeciesIDs.latios,
            SpeciesIDs.kyogre, SpeciesIDs.groudon, SpeciesIDs.rayquaza, SpeciesIDs.jirachi, SpeciesIDs.deoxys, SpeciesIDs.uxie,
            SpeciesIDs.mesprit, SpeciesIDs.azelf, SpeciesIDs.dialga, SpeciesIDs.palkia, SpeciesIDs.heatran, SpeciesIDs.regigigas,
            SpeciesIDs.giratina, SpeciesIDs.cresselia, SpeciesIDs.phione, SpeciesIDs.manaphy, SpeciesIDs.darkrai, SpeciesIDs.shaymin,
            SpeciesIDs.arceus, SpeciesIDs.victini, SpeciesIDs.cobalion, SpeciesIDs.terrakion, SpeciesIDs.virizion, SpeciesIDs.tornadus,
            SpeciesIDs.thundurus, SpeciesIDs.reshiram, SpeciesIDs.zekrom, SpeciesIDs.landorus, SpeciesIDs.kyurem, SpeciesIDs.keldeo,
            SpeciesIDs.meloetta, SpeciesIDs.genesect, SpeciesIDs.xerneas, SpeciesIDs.yveltal, SpeciesIDs.zygarde, SpeciesIDs.diancie,
            SpeciesIDs.hoopa, SpeciesIDs.volcanion, SpeciesIDs.typeNull, SpeciesIDs.silvally, SpeciesIDs.tapuKoko, SpeciesIDs.tapuLele,
            SpeciesIDs.tapuBulu, SpeciesIDs.tapuFini, SpeciesIDs.cosmog, SpeciesIDs.cosmoem, SpeciesIDs.solgaleo, SpeciesIDs.lunala,
            SpeciesIDs.necrozma, SpeciesIDs.magearna, SpeciesIDs.marshadow, SpeciesIDs.zeraora);

    private static final List<Integer> strongLegendaries = Arrays.asList(SpeciesIDs.mewtwo, SpeciesIDs.lugia, SpeciesIDs.hoOh,
            SpeciesIDs.kyogre, SpeciesIDs.groudon, SpeciesIDs.rayquaza, SpeciesIDs.dialga, SpeciesIDs.palkia, SpeciesIDs.regigigas,
            SpeciesIDs.giratina, SpeciesIDs.arceus, SpeciesIDs.reshiram, SpeciesIDs.zekrom, SpeciesIDs.kyurem, SpeciesIDs.xerneas,
            SpeciesIDs.yveltal, SpeciesIDs.cosmog, SpeciesIDs.cosmoem, SpeciesIDs.solgaleo, SpeciesIDs.lunala);

    private static final List<Integer> ultraBeasts = Arrays.asList(SpeciesIDs.nihilego, SpeciesIDs.buzzwole, SpeciesIDs.pheromosa,
            SpeciesIDs.xurkitree, SpeciesIDs.celesteela, SpeciesIDs.kartana, SpeciesIDs.guzzlord, SpeciesIDs.poipole, SpeciesIDs.naganadel,
            SpeciesIDs.stakataka, SpeciesIDs.blacephalon);

    public boolean isLegendary() {
        return formeNumber == 0 ? legendaries.contains(this.number) : legendaries.contains(this.baseForme.number);
    }

    public boolean isStrongLegendary() {
        return formeNumber == 0 ? strongLegendaries.contains(this.number) : strongLegendaries.contains(this.baseForme.number);
    }

    // This method can only be used in contexts where alt formes are NOT involved; otherwise, some alt formes
    // will be considered as Ultra Beasts in SM.
    // In contexts where formes are involved, use "if (ultraBeastList.contains(...))" instead,
    // assuming "checkSpeciesRestrictions" has been used at some point beforehand.
    public boolean isUltraBeast() {
        return ultraBeasts.contains(this.number);
    }

    public int getCosmeticFormNumber(int num) {
        return realCosmeticFormNumbers.isEmpty() ? num : realCosmeticFormNumbers.get(num);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public String getFormeSuffix() {
        return formeSuffix;
    }

    public void setFormeSuffix(String formeSuffix) {
        this.formeSuffix = formeSuffix;
    }

    public Species getBaseForme() {
        return baseForme;
    }

    public void setBaseForme(Species baseForme) {
        this.baseForme = baseForme;
    }

    /**
     * Returns whether this {@link Species} is a base forme.
     * @return False if the {@link Species} has a different base forme, true otherwise.
     */
    public boolean isBaseForme() {
        return baseForme == null;
    }

    public int getFormeNumber() {
        return formeNumber;
    }

    public void setFormeNumber(int formeNumber) {
        this.formeNumber = formeNumber;
    }

    public int getCosmeticForms() {
        return cosmeticForms;
    }

    public void setCosmeticForms(int cosmeticForms) {
        this.cosmeticForms = cosmeticForms;
    }

    public int getFormeSpriteIndex() {
        return formeSpriteIndex;
    }

    public void setFormeSpriteIndex(int formeSpriteIndex) {
        this.formeSpriteIndex = formeSpriteIndex;
    }

    public boolean isActuallyCosmetic() {
        return actuallyCosmetic;
    }

    public void setActuallyCosmetic(boolean actuallyCosmetic) {
        this.actuallyCosmetic = actuallyCosmetic;
    }

    public List<Integer> getRealCosmeticFormNumbers() {
        return realCosmeticFormNumbers;
    }

    public void setRealCosmeticFormNumbers(List<Integer> realCosmeticFormNumbers) {
        this.realCosmeticFormNumbers = realCosmeticFormNumbers;
    }

    /**
     * Returns the Generation this {@link Species} (or forme) first appeared in.
     */
    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    /**
     * Returns the {@link Species}'s secondary type. If it has no secondary type, it will return null.
     * @param useOriginal Whether to use type data from before randomization.
     * @return The {@link Species}'s secondary type.
     */
    public Type getPrimaryType(boolean useOriginal) {
        if(useOriginal) {
            return originalPrimaryType;
        } else {
            return primaryType;
        }
    }

    /**
     * Sets the primary type.<br>
     * The first time this method is called, it also sets the "original" primary type,
     * which can be retrieved with {@link #getPrimaryType(boolean)}.
     */
    public void setPrimaryType(Type primaryType) {
        this.primaryType = primaryType;
        if (!hasSetPrimaryType) {
            this.originalPrimaryType = primaryType;
            hasSetPrimaryType = true;
        }
    }

    /**
     * Returns the {@link Species}'s secondary type. If it has no secondary type, it will return null.
     * @param useOriginal Whether to use type data from before randomization.
     * @return The {@link Species}'s secondary type.
     */
    public Type getSecondaryType(boolean useOriginal) {
        if(useOriginal) {
            return originalSecondaryType;
        } else {
            return secondaryType;
        }
    }

    /**
     * Sets the secondary type.<br>
     * The first time this method is called, it also sets the "original" secondary type,
     * which can be retrieved with {@link #getSecondaryType(boolean)}.
     * For this reason, it is important to use this method when initializing a {@link Species}'s types,
     * even if the "null" value used to represent no secondary type is technically the internal state of the
     * secondaryType attribute before being set.
     */
    public void setSecondaryType(Type secondaryType) {
        this.secondaryType = secondaryType;
        if (!hasSetSecondaryType) {
            this.originalSecondaryType = secondaryType;
            hasSetSecondaryType = true;
        }
    }

    /**
     * Checks whether either of the {@link Species}'s types are the given type.
     * If the given type is null, always returns false.
     * @param type The type to check against.
     * @param useOriginal Whether to use type data from before randomization.
     * @return True if the {@link Species} has the given type, false otherwise.
     */
    public boolean hasType(Type type, boolean useOriginal) {
        if(type == null) {
            return false;
        }
        return getPrimaryType(useOriginal) == type || getSecondaryType(useOriginal) == type;
    }

    /**
     * Checks whether the {@link Species} has a secondary type.
     * @param useOriginal Whether to use type data from before randomization.
     * @return True if the {@link Species} has a secondary type, false otherwise.
     */
    public boolean hasSecondaryType(boolean useOriginal) {
        return this.getSecondaryType(useOriginal) != null;
    }

    /**
     * Returns true if this shares any {@link Type} with the given {@link Species}.
     */
    public boolean hasSharedType(Species other) {
        return getPrimaryType(false).equals(other.getPrimaryType(false)) || getPrimaryType(false).equals(other.getSecondaryType(false))
                || (getSecondaryType(false) != null &&
                (getSecondaryType(false).equals(other.getPrimaryType(false)) || getSecondaryType(false).equals(other.getSecondaryType(false))));
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpatk() {
        return spatk;
    }

    public void setSpatk(int spatk) {
        this.spatk = spatk;
    }

    public int getSpdef() {
        return spdef;
    }

    public void setSpdef(int spdef) {
        this.spdef = spdef;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpecial() {
        return special;
    }

    public void setSpecial(int special) {
        this.special = special;
    }

    public int getAbility1() {
        return ability1;
    }

    public void setAbility1(int ability1) {
        this.ability1 = ability1;
    }

    public int getAbility2() {
        return ability2;
    }

    public void setAbility2(int ability2) {
        this.ability2 = ability2;
    }

    public int getAbility3() {
        return ability3;
    }

    public void setAbility3(int ability3) {
        this.ability3 = ability3;
    }

    public int getCatchRate() {
        return catchRate;
    }

    public void setCatchRate(int catchRate) {
        this.catchRate = catchRate;
    }

    public int getExpYield() {
        return expYield;
    }

    public void setExpYield(int expYield) {
        this.expYield = expYield;
    }

    public int getGuaranteedHeldItem() {
        return guaranteedHeldItem;
    }

    public void setGuaranteedHeldItem(int guaranteedHeldItem) {
        this.guaranteedHeldItem = guaranteedHeldItem;
    }

    public int getCommonHeldItem() {
        return commonHeldItem;
    }

    public void setCommonHeldItem(int commonHeldItem) {
        this.commonHeldItem = commonHeldItem;
    }

    public int getRareHeldItem() {
        return rareHeldItem;
    }

    public void setRareHeldItem(int rareHeldItem) {
        this.rareHeldItem = rareHeldItem;
    }

    public int getDarkGrassHeldItem() {
        return darkGrassHeldItem;
    }

    public void setDarkGrassHeldItem(int darkGrassHeldItem) {
        this.darkGrassHeldItem = darkGrassHeldItem;
    }

    public int getGenderRatio() {
        return genderRatio;
    }

    public void setGenderRatio(int genderRatio) {
        this.genderRatio = genderRatio;
    }

    public int getFrontImageDimensions() {
        return frontImageDimensions;
    }

    public void setFrontImageDimensions(int frontImageDimensions) {
        this.frontImageDimensions = frontImageDimensions;
    }

    public int getCallRate() {
        return callRate;
    }

    public void setCallRate(int callRate) {
        this.callRate = callRate;
    }

    public ExpCurve getGrowthCurve() {
        return growthCurve;
    }

    public void setGrowthCurve(ExpCurve growthCurve) {
        this.growthCurve = growthCurve;
    }

    public List<Palette> getNormalPalettes() {
        return normalPalettes;
    }

    public void setNormalPalettes(List<Palette> normalPalettes) {
        this.normalPalettes = normalPalettes;
    }

    public List<Palette> getShinyPalettes() {
        return shinyPalettes;
    }

    public void setShinyPalettes(List<Palette> shinyPalettes) {
        this.shinyPalettes = shinyPalettes;
    }

    public Palette getNormalPalette() {
        return getNormalPalette(0);
    }

    public Palette getNormalPalette(int index) {
        return normalPalettes.size() <= index ? null : normalPalettes.get(index);
    }

    public void setNormalPalette(Palette normalPalette) {
        setNormalPalette(0, normalPalette);
    }

    public void setNormalPalette(int index, Palette normalPalette) {
        while (normalPalettes.size() <= index) {
            normalPalettes.add(index, null);
        }
        normalPalettes.set(index, normalPalette);
    }

    public Palette getShinyPalette() {
        return getShinyPalette(0);
    }

    public Palette getShinyPalette(int index) {
        return shinyPalettes.size() <= index ? null : shinyPalettes.get(index);
    }

    public void setShinyPalette(Palette shinyPalette) {
        setShinyPalette(0, shinyPalette);
    }

    public void setShinyPalette(int index, Palette shinyPalette) {
        while (shinyPalettes.size() <= index) {
            shinyPalettes.add(index, null);
        }
        shinyPalettes.set(index, shinyPalette);
    }

    /**
     * Returns a (modifiable!) {@link List} of {@link Evolution}s where this Pokémon species is what the evolution is
     * "from".<br>
     * E.g. if the Pokémon is Gloom, this would return a List with two elements, one being the Evolution from
     * Gloom to Vileplume, and the other being the Evolution from Gloom to Bellossom.
     */
    //TODO: Make this (and getEvolutionsTo) return unmodifiable lists and use setEvolutions methods instead.
    // (Or, alternatively, addEvolution and clearEvolutions methods, wherein the first call to clear
    // sets the original evolutions.)
    //(And then make setEvolutions also set original evolutions when first called.)
    public List<Evolution> getEvolutionsFrom() {
        return evolutionsFrom;
    }

    /**
     * Returns a (modifiable!) {@link List} of {@link Evolution}s where this Pokémon species is what the evolution is
     * "to".<br>
     * E.g. if the Pokémon is Gloom, this would return a List with one element, being the Evolution from
     * Oddish to Gloom.<br>
     * Normally this List has only one or zero elements, because no two vanilla Pokémon evolve into
     * the same third Pokémon.
     */
    public List<Evolution> getEvolutionsTo() {
        return evolutionsTo;
    }

    public List<MegaEvolution> getMegaEvolutionsFrom() {
        return megaEvolutionsFrom;
    }

    public void setMegaEvolutionsFrom(List<MegaEvolution> megaEvolutionsFrom) {
        this.megaEvolutionsFrom = megaEvolutionsFrom;
    }

    public List<MegaEvolution> getMegaEvolutionsTo() {
        return megaEvolutionsTo;
    }

    public void setMegaEvolutionsTo(List<MegaEvolution> megaEvolutionsTo) {
        this.megaEvolutionsTo = megaEvolutionsTo;
    }

}