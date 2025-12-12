package com.dabomstew.pkromio.gamedata;

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

import com.dabomstew.pkromio.constants.SpeciesIDs;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import java.util.*;

/**
 * Represents a Pokémon species or forme.
 */
public class Species implements Comparable<Species> {
    //TODO: make this backed by an unmodifiable original (set when saveOriginalData() is called, I suppose)
    //TODO: add a reset method that reverts this to original (for testing)

    private String name;
    private final int number;

    private String formeSuffix = "";
    private Species baseForme = null;
    private int formeNumber = 0;
    private int cosmeticForms = 0;
    private boolean actuallyCosmetic = false;
    private List<Integer> realCosmeticFormNumbers = new ArrayList<>();
    //TODO: condense this cosmetic bs into a single denotation

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

    private Item guaranteedHeldItem;
    private Item commonHeldItem;
    private Item rareHeldItem;
    private Item darkGrassHeldItem;

    private int genderRatio;

    private int frontImageDimensions;

    private int callRate;

    private ExpCurve growthCurve;
    
    private Palette normalPalette;
    private Palette shinyPalette;

    private List<Evolution> evolutionsFrom = new ArrayList<>();
    private List<Evolution> evolutionsTo = new ArrayList<>();

    private List<MegaEvolution> megaEvolutionsFrom = new ArrayList<>();
    private List<MegaEvolution> megaEvolutionsTo = new ArrayList<>();

    public Species(int number) {
        this.number = number;
    }

    /**
     * Gets the raw Base Stat Total. In most cases, {@link #getBSTForPowerLevels()}
     * should be used instead.
     */
    public int getBST() {
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
        // One might think this could just be turned into
        // return getBaseForme().getNumber()
        // but note the "while"; this works with formes-of-formes.
        // Formes-of-formes admittedly only exist in Gen 7,
        // but until something is done about them, don't touch this code.
        Species base = this;
        while (base.baseForme != null) {
            base = base.baseForme;
        }
        return base.number;
    }

    //Evolutionary Relatives functions

    /**
     * Determines whether this {@link Species} is a basic Pokemon (has no pre-evolution) with an evolution
     * that has an evolution of its own (without counting Mega Evolution).
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A boolean, true if this {@link Species} is a basic Pokemon with an evolution that has
     *         an evolution of its own
     */
    public boolean isBasicPokemonWithMoreThanTwoEvoStages(boolean useOriginal) {
        if (this.getPreEvolvedSpecies(useOriginal).isEmpty()) {
            // We have a basic Pokemon (no pre-evolutions)
            for (Species evolvedSpecies : this.getEvolvedSpecies(useOriginal)) {
                if (!evolvedSpecies.getEvolvedSpecies(useOriginal).isEmpty()) {
                    // Pokemon has one evolution that has an evolution of its own, i.e., has at least two evolution stages
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets all {@link Species} that this {@link Species} can evolve directly into.
     * Does not include Mega Evolution.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all possible evolved forms of this {@link Species}.
     */
    public SpeciesSet getEvolvedSpecies(boolean useOriginal) {
        SpeciesSet evolvedSpecies;
        if(useOriginal) {
            if(this.originalEvolvedForms == null) {
                evolvedSpecies = new SpeciesSet();
            } else {
                evolvedSpecies = new SpeciesSet(this.originalEvolvedForms);
            }
        } else {
            evolvedSpecies = new SpeciesSet();
            for (Evolution evo : evolutionsFrom) {
                evolvedSpecies.add(evo.getTo());
            }
        }

        if(!isBaseForme()) {
            evolvedSpecies.addAll(baseForme.getEvolvedSpecies(useOriginal));
        }
        return evolvedSpecies;
    }

    /**
     * Gets all {@link Species} that can evolve directly into this {@link Species}.
     * Does not include Mega Evolution.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A new {@link SpeciesSet} containing all pre-evolved forms of this {@link Species}.
     */
    public SpeciesSet getPreEvolvedSpecies(boolean useOriginal) {
        SpeciesSet evolvedSpecies;

        if(useOriginal) {
            if(this.originalPreEvolvedForms == null) {
                evolvedSpecies = new SpeciesSet();
            } else {
                evolvedSpecies = new SpeciesSet(this.originalPreEvolvedForms);
            }
        } else {
            evolvedSpecies = new SpeciesSet();
            for (Evolution evo : evolutionsTo) {
                evolvedSpecies.add(evo.getFrom());
            }
        }

        if(!isBaseForme()) {
            evolvedSpecies.addAll(baseForme.getPreEvolvedSpecies(useOriginal));
            //TODO: improve handling for non-evolving forms of evolving species,
            // e.g. Battle Bond Greninja, Eternal Flower Floette
        }
        return evolvedSpecies;
    }

    /**
     * Gets all {@link Species} that this {@link Species} is related to by evolution.
     * Includes itself. Does not include Mega Evolution.
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
     * A tiny class intended to remove reliance on javafx.util.Pair.
     * Holds a given species and its relation to another species.
     */
    private static class RelationRecord {
        Species relative;
        int relation;

        RelationRecord(Species relative, int relation) {
            this.relative = relative;
            this.relation = relation;
        }
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
        Queue<RelationRecord> toCheck = new ArrayDeque<>();
        SpeciesSet checked = new SpeciesSet();
        toCheck.add(new RelationRecord(this, 0));

        while(!toCheck.isEmpty()) {
            RelationRecord current = toCheck.remove();
            Species currentSpecies = current.relative;
            int currentPosition = current.relation;
            if(checked.contains(currentSpecies)) {
                continue;
            }
            checked.add(currentSpecies);

            if(currentSpecies == relative) {
                return currentPosition;
            }

            for(Species evo : currentSpecies.getEvolvedSpecies(useOriginal)) {
                toCheck.add(new RelationRecord(evo, currentPosition + 1));
            }
            for(Species evo : currentSpecies.getPreEvolvedSpecies(useOriginal)) {
                toCheck.add(new RelationRecord(evo, currentPosition - 1));
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
        Queue<RelationRecord> toCheck = new ArrayDeque<>();
        SpeciesSet checked = new SpeciesSet();
        SpeciesSet relatives = new SpeciesSet();
        toCheck.add(new RelationRecord(this, 0));

        while(!toCheck.isEmpty()) {
            RelationRecord current = toCheck.remove();
            Species currentSpecies = current.relative;
            int currentPosition = current.relation;
            if(checked.contains(currentSpecies)) {
                continue;
            }
            checked.add(currentSpecies);

            if(currentPosition == position) {
                relatives.add(currentSpecies);
            }

            for(Species evo : currentSpecies.getEvolvedSpecies(useOriginal)) {
                toCheck.add(new RelationRecord(evo, currentPosition + 1));
            }
            for(Species evo : currentSpecies.getPreEvolvedSpecies(useOriginal)) {
                toCheck.add(new RelationRecord(evo, currentPosition - 1));
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
    //...also likely to have odd behavior with merged evolutions of different lengths
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
                nextStage.addAll(spec.getEvolvedSpecies(useOriginal));
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
                previousStage.addAll(spec.getPreEvolvedSpecies(useOriginal));
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
        //Doesn't copy evolutions to as that would result in poorly-defined behavior
    }

    public String getFullName() {
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
        // Don't change this hash!! Things *will* break.
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
        return legendaries.contains(getBaseForme().number);
    }

    public boolean isStrongLegendary() {
        return strongLegendaries.contains(getBaseForme().number);
    }

    // This method can only be used in contexts where alt formes are NOT involved; otherwise, some alt formes
    // will be considered as Ultra Beasts in SM.
    // In contexts where formes are involved, use "if (ultraBeastList.contains(...))" instead,
    // assuming "checkSpeciesRestrictions" has been used at some point beforehand.
    public boolean isUltraBeast() {
        return ultraBeasts.contains(this.number);
    }

    /**
     * Gets a random cosmetic forme of this Species, including itself.
     * @param random A seeded random number generator.
     * @return A forme number for a random cosmetic forme of this Species, including itself.
     */
    public int getRandomCosmeticFormeNumber(Random random) {
        if(cosmeticForms == 0) {
            return formeNumber;
        }

        int num = random.nextInt(cosmeticForms);
        if (num == cosmeticForms) {
            return formeNumber;
        }

        if(!realCosmeticFormNumbers.isEmpty()) {
            if(num > realCosmeticFormNumbers.size()) {
                throw new IllegalStateException("Not all cosmetic formes listed in cosmeticFormeNumbers!");
            }
            return realCosmeticFormNumbers.get(num);
        } else {
            return formeNumber + num;
        }
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

    /**
     * Returns the base forme of this Species, or itself if it is the base forme.<br>
     * E.g. Deoxys and Deoxys-Attack would both return Deoxys, and Gloom would return Gloom.
     */
    public Species getBaseForme() {
        return isBaseForme() ? this : baseForme;
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

    /**
     * Checks whether the form is a purely cosmetic variant on its base form.
     * Has some false positives and negatives at the current time.<br>
     * See also {@link #isCosmeticReplacement()}
     * @return Whether the form is cosmetic.
     */
    public boolean isActuallyCosmetic() {
        return actuallyCosmetic;
    }

    /**
     * Checks if this forme can be chosen as a "cosmetic" replacement.<br>
     * To check if the forme is a cosmetic forme, use {@link #isActuallyCosmetic()}. <br>
     * Despite the name, not all "cosmetic" replacements are purely cosmetic (e.g. Pumpkaboo's sizing).
     * @return True if the forme is a cosmetic variant, false otherwise.
     */
    public boolean isCosmeticReplacement() {
        if(baseForme == null) {
            return false;
        }

        Species base = baseForme;
        if(base.getRealCosmeticFormNumbers().isEmpty()) {
            return formeNumber <= base.formeNumber + base.getCosmeticForms();
        } else {
            return base.getRealCosmeticFormNumbers().contains(formeNumber);
        }
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
     * <br><br>
     * If the secondary given is the same as the current primary, it will instead be set to null.
     * Therefore, if changing both types, it is important to change the primary type first.
     */
    public void setSecondaryType(Type secondaryType) {
        if(hasSetPrimaryType && secondaryType == primaryType) {
            secondaryType = null; //So that original types aren't full of NORMAL/NORMAL and the like
        }

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

    public Item getGuaranteedHeldItem() {
        return guaranteedHeldItem;
    }

    public void setGuaranteedHeldItem(Item guaranteedHeldItem) {
        this.guaranteedHeldItem = guaranteedHeldItem;
    }

    public Item getCommonHeldItem() {
        return commonHeldItem;
    }

    public void setCommonHeldItem(Item commonHeldItem) {
        this.commonHeldItem = commonHeldItem;
    }

    public Item getRareHeldItem() {
        return rareHeldItem;
    }

    public void setRareHeldItem(Item rareHeldItem) {
        this.rareHeldItem = rareHeldItem;
    }

    public Item getDarkGrassHeldItem() {
        return darkGrassHeldItem;
    }

    public void setDarkGrassHeldItem(Item darkGrassHeldItem) {
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

    public Palette getNormalPalette() {
        return normalPalette;
    }

    public void setNormalPalette(Palette normalPalette) {
        this.normalPalette = normalPalette;
    }

    public Palette getShinyPalette() {
        return shinyPalette;
    }

    public void setShinyPalette(Palette shinyPalette) {
        this.shinyPalette = shinyPalette;
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
