package com.dabomstew.pkromio.romhandlers;

/*----------------------------------------------------------------------------*/
/*--  AbstractRomHandler.java - a base class for all rom handlers which     --*/
/*--                            implements the majority of the actual       --*/
/*--                            randomizer logic by building on the base    --*/
/*--                            getters & setters provided by each concrete --*/
/*--                            handler.                                    --*/
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

import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.constants.AbilityIDs;
import com.dabomstew.pkromio.constants.GlobalConstants;
import com.dabomstew.pkromio.constants.ItemIDs;
import com.dabomstew.pkromio.exceptions.RomIOException;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.graphics.packs.CustomPlayerGraphics;
import com.dabomstew.pkromio.romhandlers.romentries.RomEntry;
import com.dabomstew.pkromio.services.RestrictedSpeciesService;
import com.dabomstew.pkromio.services.TypeService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract base class for {@link RomHandler}s, with default implementations for many of the interface's methods.
 */
public abstract class AbstractRomHandler implements RomHandler {

    protected final RestrictedSpeciesService rPokeService = new RestrictedSpeciesService(this);
    protected final TypeService typeService = new TypeService(this);

    protected int perfectAccuracy = 100; // default

    private int highestOriginalEvoLvl = 0;

    private List<Type> starterTypeTriangle = null;

    protected final List<Trainer> trainers = new ArrayList<>();

    /*
     * Public Methods, implemented here for all gens. Unlikely to be overridden.
     */

    public int getHighestOriginalEvoLvl() {
        return highestOriginalEvoLvl;
    }

    public RestrictedSpeciesService getRestrictedSpeciesService() {
        return rPokeService;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    @Override
    public SpeciesSet getSpeciesSet() {
        return SpeciesSet.unmodifiable(getSpecies());
    }

    @Override
    public SpeciesSet getSpeciesSetInclFormes() {
        return SpeciesSet.unmodifiable(getSpeciesInclFormes());
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        return getEncounters(useTimeOfDay);
    }

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        return new SpeciesSet();
    }

    @Override
    public boolean canAddPokemonToBossTrainers() {
        return true;
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        return true;
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        return true;
    }

    @Override
    public List<Trainer> getTrainers() {
        return Collections.unmodifiableList(trainers);
    }

    public SpeciesSet getMainGameWildPokemonSpecies(boolean useTimeOfDay) {
        SpeciesSet wildPokemon = new SpeciesSet();
        List<EncounterArea> areas = this.getEncounters(useTimeOfDay);

        for (EncounterArea area : areas) {
            if (area.isPartiallyPostGame()) {
                for (int i = 0; i < area.getPartiallyPostGameCutoff(); i++) {
                    wildPokemon.add(area.get(i).getSpecies());
                }
            } else if (!area.isPostGame()) {
                for (Encounter enc : area) {
                    wildPokemon.add(enc.getSpecies());
                }
            }
        }
        return wildPokemon;
    }

    @Override
    public boolean canAddHeldItemsToBossTrainers() {
        return true;
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        return true;
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        return true;
    }

    @Override
    public boolean hasRivalFinalBattle() {
        return false;
    }

    @Override
    public boolean hasStarterTypeTriangleSupport() {
        return (starterCount() % 3 == 0);
    }

    public void setStarterTypeTriangle(List<Type> triangle) {
        if(triangle.size() != 3) {
            throw new IllegalArgumentException("Type triangle must contain three types!");
        }
        starterTypeTriangle = Collections.unmodifiableList(triangle);
    }

    public List<Type> getStarterTypeTriangle() {
        if(isTypeTriangleChanged()) {
            return starterTypeTriangle;
        } else {
            return getStandardTypeTriangle();
        }
    }

    public boolean isTypeTriangleChanged() {
        return starterTypeTriangle != null;
    }

    public List<Type> getStandardTypeTriangle(){
        List<Type> typesInOrder;
        if(generationOfPokemon() <= 2) {
            //the order is Fire, Water, Grass
            typesInOrder = Arrays.asList(Type.FIRE, Type.WATER, Type.GRASS);
        } else {
            //the order is Grass, Fire, Water
            typesInOrder = Arrays.asList(Type.GRASS, Type.FIRE, Type.WATER);
        }

        return Collections.unmodifiableList(typesInOrder);
    }

    @Override
    public boolean canChangeStaticPokemon() {
        return getRomEntry().hasStaticPokemonSupport();
    }

    @Override
    public void condenseLevelEvolutions(int maxLevel) {
        // Only condense level evolutions if a level smaller than the highest original evo level in the ROM is chosen
        if (maxLevel < getHighestOriginalEvoLvl()) {
            int maxIntermediateLevel = (int) Math.ceil(0.75 * maxLevel);
            // search for level evolutions
            for (Species pk : getSpeciesSet()) {
                if (pk != null) {
                    for (Evolution checkEvo : pk.getEvolutionsFrom()) {
                        if (checkEvo.getType().usesLevelThreshold()) {
                            // If evo is intermediate and too high, bring it down
                            // Else if it's just too high, bring it down
                            if (checkEvo.getExtraInfo() > maxIntermediateLevel && !checkEvo.getTo().getEvolutionsFrom().isEmpty()) {
                                markImprovedEvolutions(pk);
                                checkEvo.updateEvolutionMethod(checkEvo.getType(), maxIntermediateLevel);
                            } else if (checkEvo.getExtraInfo() > maxLevel) {
                                markImprovedEvolutions(pk);
                                checkEvo.updateEvolutionMethod(checkEvo.getType(), maxLevel);
                            }
                        } else {
                            // For all other evolutions, the estimated evolution levels have to be condensed if necessary
                            if (checkEvo.getEstimatedEvoLvl() > maxIntermediateLevel && !checkEvo.getTo().getEvolutionsFrom().isEmpty()) {
                                markImprovedEvolutions(pk);
                                checkEvo.setEstimatedEvoLvl(maxIntermediateLevel);
                            } else if (checkEvo.getEstimatedEvoLvl() > maxLevel) {
                                markImprovedEvolutions(pk);
                                checkEvo.setEstimatedEvoLvl(maxLevel);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void estimateEvolutionLevels() {
        // Get a list of all level-up evolutions and a list of all non-level-up evolutions
        List<Evolution> levelUpEvos = new ArrayList<>();
        List<Evolution> nonLevelUpEvos = new ArrayList<>();
        for (Species pk : getSpeciesSet()) {
            for (Evolution evoFrom : pk.getEvolutionsFrom()) {
                if (evoFrom.getType().usesLevelThreshold()) {
                    levelUpEvos.add(evoFrom);
                    // Set estimated evolution level and update highest evolution level in ROM
                    evoFrom.setEstimatedEvoLvl(evoFrom.getExtraInfo());
                    if (evoFrom.getExtraInfo() > highestOriginalEvoLvl) {
                        highestOriginalEvoLvl = evoFrom.getExtraInfo();
                    }
                } else {
                    nonLevelUpEvos.add(evoFrom);
                }
            }
        }

        // For all level-up evolutions, get triplets (BSTfrom, BSTto, evoLevel)
        List<int[]> levelUpTriplet = new ArrayList<>();
        for (Evolution evo : levelUpEvos) {
            int[] triplet = {evo.getFrom().getBSTForPowerLevels(), evo.getTo().getBSTForPowerLevels(), evo.getExtraInfo()};
            levelUpTriplet.add(triplet);
        }

        for (Evolution evo : nonLevelUpEvos) {
            evo.setEstimatedEvoLvl(findEvolutionLevel(levelUpTriplet, evo.getFrom().getBSTForPowerLevels(), evo.getTo().getBSTForPowerLevels()));
        }

        // Postprocess estimated level
        for (Evolution evo : nonLevelUpEvos) {
            if (!evo.getFrom().getEvolutionsTo().isEmpty()) { // getFrom Pkmn has a pre-evolution
                // Make sure the estimatedlevel is at least 25% higher than the evo level of the previous evolution
                Evolution previousEvo = evo.getFrom().getEvolutionsTo().get(0);
                evo.setEstimatedEvoLvl(
                        Math.max(evo.getEstimatedEvoLvl(), (int) Math.ceil(1.25 * previousEvo.getEstimatedEvoLvl())));
            }
            if (!evo.getTo().getEvolutionsFrom().isEmpty()) { // getTo Pkmn has an evolution
                // Make sure the evo level is at most 80% of the following evolutions evo level, i.e., the following evolution has a 25% higher level.
                for (Evolution nextEvo : evo.getTo().getEvolutionsFrom()) {
                    evo.setEstimatedEvoLvl(
                            Math.min(evo.getEstimatedEvoLvl(), (int) Math.ceil(0.8 * nextEvo.getEstimatedEvoLvl())));
                }
            }
            // Update highest evolution level in ROM if necessary
            if (evo.getEstimatedEvoLvl() > highestOriginalEvoLvl) {
                highestOriginalEvoLvl = evo.getEstimatedEvoLvl();
            }
        }
    }

    private static int findEvolutionLevel(List<int[]> samples, int targetPreBST, int targetPostBST) {

        // ==== CONFIGURATION PARAMETERS ====
        double p = 1;                // distance weighting exponent: 1/d^p
        double preFactor = 1.5;          // scaling factor for preBST
        double postFactor = 3;         // scaling factor for postBST
        double largeWeightForZero = 1; // weight to use if distance is zero
        // ==================================

        double weightedSum = 0.0;
        double weightSum = 0.0;

        for (int[] sample : samples) {
            int samplePre = sample[0];
            int samplePost = sample[1];
            int sampleLevel = sample[2];

            // Compute Euclidean distance
            double dx = targetPreBST - samplePre;
            double dy = targetPostBST - samplePost;
            double dist = Math.sqrt(dx * dx + dy * dy);

            // Weight = 1 / d^p, use largeWeightForZero if distance is zero
            double weight = dist == 0 ? largeWeightForZero : 1.0 / Math.pow(dist, p);

            // Pre/post scaling
            double scaledPre = Math.pow((double) targetPreBST / samplePre, preFactor);
            double scaledPost = Math.pow((double) targetPostBST / samplePost, postFactor);

            double adjustedLevel = sampleLevel * (scaledPre + scaledPost) / 2.0;

            weightedSum += adjustedLevel * weight;
            weightSum += weight;
        }

        // Return weighted average
        return (int) Math.round(weightedSum / weightSum);
    }

    @Override
    public void removeTimeBasedEvolutions() {
        for (Species pk : getSpecies()) {
            if (pk == null) {
                continue;
            }
            for (Evolution evo : pk.getEvolutionsFrom()) {
                EvolutionType et = evo.getType();

                if (et == EvolutionType.LEVEL_DUSK) {
                    markImprovedEvolutions(pk);
                    evo.updateEvolutionMethod(EvolutionType.STONE, ItemIDs.duskStone);
                } else if (et.usesTime()) {
                    markImprovedEvolutions(pk);
                    if (hadEvolutionOfType(pk, et.oppositeTime())) {
                        // Here we have just ascertained that this Species evolves by time,
                        // and that this evolution is paired; it has another similar evolution
                        // at the opposite time.
                        // E.g. Eevee -> Espeon/Umbreon, which is a HAPPINESS_DAY/HAPPINESS_NIGHT pair.
                        // In this case, we can't just remove the time-based-less,
                        // so instead we use Sun/Moon Stone.
                        int item = et.isDayType() ? ItemIDs.sunStone : ItemIDs.moonStone;
                        evo.updateEvolutionMethod(EvolutionType.STONE, item);
                    } else {
                        evo.updateEvolutionMethod(et.timeless(), evo.getExtraInfo());
                    }
                }
            }
        }
    }

    protected boolean hadEvolutionOfType(Species pk, EvolutionType et) {
        List<Evolution> evos = preImprovedEvolutions.get(pk);
        if (evos == null) {
            throw new IllegalStateException("Species should always have been added to preImprovedEvolutions.");
        }
        return evos.stream().map(Evolution::getType).anyMatch(et2 -> et2 == et);
    }

    @Override
    public Map<Species, List<Evolution>> getPreImprovedEvolutions() {
        return preImprovedEvolutions;
    }

    @Override
    public int[] getMovesAtLevel(int pkmn, Map<Integer, List<MoveLearnt>> movesets, int level) {
        int[] curMoves = new int[4];

        int moveCount = 0;
        List<MoveLearnt> movepool = movesets.get(pkmn);
        for (MoveLearnt ml : movepool) {
            if (ml.level > level) {
                // we're done
                break;
            }

            boolean alreadyKnownMove = false;
            for (int i = 0; i < moveCount; i++) {
                if (curMoves[i] == ml.move) {
                    alreadyKnownMove = true;
                    break;
                }
            }

            if (!alreadyKnownMove) {
                // add this move to the moveset
                if (moveCount == 4) {
                    // shift moves up and add to last slot
                    System.arraycopy(curMoves, 1, curMoves, 0, 3);
                    curMoves[3] = ml.move;
                } else {
                    // add to next available slot
                    curMoves[moveCount++] = ml.move;
                }
            }
        }

        return curMoves;
    }

    /* Private methods/structs used internally by the above methods */

    private final Map<Species, List<Evolution>> preImprovedEvolutions = new TreeMap<>();

    /**
     * Marks that a {@link Species} is getting its {@link Evolution}s improved,
     * (and saves its original Evolutions) for logging purposes.
     */
    protected void markImprovedEvolutions(Species pk) {
        // We can't overwrite these entries, because then species with
        // multiple evos to change (e.g. HGSS Eevee) will store their partially-changed evos,
        // instead of the original ones.
        if (!preImprovedEvolutions.containsKey(pk)) {
            List<Evolution> evosCopy = new ArrayList<>(pk.getEvolutionsFrom().size());
            for (Evolution original : pk.getEvolutionsFrom()) {
                evosCopy.add(new Evolution(original));
            }
            preImprovedEvolutions.put(pk, evosCopy);
        }
    }

    @Override
    public int getAbilityForTrainerPokemon(TrainerPokemon tp) {
        if (abilitiesPerSpecies() == 0) {
            throw new IllegalStateException("No abilities in this game.");
        }
        if (tp.getAbilitySlot() > abilitiesPerSpecies()) {
            throw new IllegalStateException("tp.abilitySlot too high for this game. Should be <="
                    + abilitiesPerSpecies() + ", is " + tp.getAbilitySlot());
        }

        // Before randomizing Trainer Pokemon, one possible value for abilitySlot is 0,
        // which represents "Either Ability 1 or 2". During randomization, we make sure
        // to set abilitySlot to some non-zero value, but if you call this method without
        // randomization, then you'll hit this case.
        if (tp.getAbilitySlot() == 0) {
            return AbilityIDs.undefined;
        }

        Species pk = !tp.getSpecies().isBaseForme() && isTrainerPokemonUseBaseFormeAbilities() ?
                tp.getSpecies().getBaseForme() : tp.getSpecies();
        int[] abilities = new int[] {pk.getAbility1(), pk.getAbility2(), pk.getAbility3()};

        int slot = isTrainerPokemonAlwaysUseAbility1() ? 1 : tp.getAbilitySlot();

        return abilities[slot - 1];
    }

    protected void checkFieldItemsTMsReplaceTMs(List<Item> replacement) {
        List<Item> current = getFieldItems();
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).isTM() != replacement.get(i).isTM()) {
                throw new IllegalArgumentException("TMs must replace TMs, non-TMs must replace non-TMs");
            }
        }
    }

    @Override
    public void setBalancedShopPrices() {
        List<Integer> prices = getShopPrices();
        for (Map.Entry<Integer, Integer> entry : getBalancedShopPrices().entrySet()) {
            if (entry.getKey() < prices.size()) {
                prices.set(entry.getKey(), entry.getValue());
            }
        }
        setShopPrices(prices);
    }

    protected abstract Map<Integer, Integer> getBalancedShopPrices();

    /* Helper methods used by subclasses and/or this class */

    /**
     * Splits occurrences of {@link EvolutionType#ITEM} into
     * a {@link EvolutionType#ITEM_DAY} and a {@link EvolutionType#ITEM_NIGHT} part.<br>
     * Since ITEM is not used internally in any ROM, this must be done before writing Evolutions.<br>
     * Assumes each Species has at most one ITEM Evolution.
     */
    protected void splitLevelItemEvolutions() {
        for (Species pk : getSpecies()) {
            if (pk == null) {
                continue;
            }
            List<Evolution> levelItemEvos = new ArrayList<>();
            for (Evolution evo : pk.getEvolutionsFrom()) {
                if (evo.getType() == EvolutionType.ITEM) {
                    levelItemEvos.add(evo);
                }
            }
            if (!levelItemEvos.isEmpty()) {
                for (Evolution levelItemEvo : levelItemEvos) {
                    levelItemEvo.updateEvolutionMethod(EvolutionType.ITEM_DAY, levelItemEvo.getExtraInfo());
                    Evolution nightEvo = new Evolution(levelItemEvo);
                    nightEvo.updateEvolutionMethod(EvolutionType.ITEM_NIGHT, levelItemEvo.getExtraInfo());
                    nightEvo.getFrom().getEvolutionsFrom().add(nightEvo);
                    nightEvo.getTo().getEvolutionsTo().add(nightEvo);
                }
            }
        }
    }

    /**
     * Merge occurrences of otherwise identical {@link EvolutionType#ITEM_DAY} and
     * {@link EvolutionType#ITEM_NIGHT} {@link Evolution}s into a single one
     * using {@link EvolutionType#ITEM}.<br>
     * Assumes each Species has at most one pair of ITEM_DAY/NIGHT Evolutions.
     */
    protected void mergeLevelItemEvolutions() {
        for (Species pk : getSpecies()) {
            if (pk == null) {
                continue;
            }
            List<Evolution> dayEvos = new ArrayList<>();
            List<Evolution> nightEvos = new ArrayList<>();
            for (Evolution evo : pk.getEvolutionsFrom()) {
                if (evo.getType() == EvolutionType.ITEM_DAY) {
                    dayEvos.add(evo);
                } else if (evo.getType() == EvolutionType.ITEM_NIGHT) {
                    nightEvos.add(evo);
                }
            }

            for (Evolution dayEvo : dayEvos) {
                boolean merged = false;

                dayEvo.updateEvolutionMethod(EvolutionType.ITEM_NIGHT, dayEvo.getExtraInfo());
                for (Evolution nightEvo : nightEvos) {
                    if (dayEvo.equals(nightEvo)) {
                        dayEvo.updateEvolutionMethod(EvolutionType.ITEM, dayEvo.getExtraInfo());
                        nightEvo.getFrom().getEvolutionsFrom().remove(nightEvo);
                        nightEvo.getTo().getEvolutionsTo().remove(nightEvo);
                        merged = true;
                    }
                }
                if (!merged) {
                    // The dayEvo didn't have an identical nightEvo, so turn it back
                    dayEvo.updateEvolutionMethod(EvolutionType.ITEM_DAY, dayEvo.getExtraInfo());
                }
            }
        }
    }

    protected void applyCamelCaseNames() {
        getSpeciesSet().forEach(pk -> pk.setName(RomFunctions.camelCase(pk.getName())));
    }

    /* Default Implementations */
    /* Used when a subclass doesn't override */
    /*
     * The implication here is that these WILL be overridden by at least one
     * subclass.
     */

    @Override
    public boolean canChangeShopSizes() {
        // DEFAULT: no
        return false;
    }

    @Override
    public ExpCurve[] getExpCurves() {
        return new ExpCurve[]{
                ExpCurve.MEDIUM_FAST, ExpCurve.MEDIUM_SLOW, ExpCurve.FAST, ExpCurve.SLOW,
                ExpCurve.ERRATIC, ExpCurve.FLUCTUATING
        };
    }

    @Override
    public boolean isTMsReusable() {
        return true;
    }

    @Override
    public boolean canTMsBeHeld() {
        return false;
    }

    @Override
    public boolean isTrainerPokemonAlwaysUseAbility1() {
        // DEFAULT: no
        return false;
    }

    @Override
    public boolean isTrainerPokemonUseBaseFormeAbilities() {
        // DEFAULT: no
        return false;
    }

    @Override
    public List<String> getLocationNamesForEvolution(EvolutionType et) {
        throw new UnsupportedOperationException("This game has no location-based evolutions.");
    }

    @Override
    public boolean canSetIntroPokemon() {
        // DEFAULT: yes
        return true;
    }

    @Override
    public boolean hasTimeBasedEvolutions() {
        // DEFAULT: yes
        return true;
    }

    @Override
    public boolean canGiveEverySpeciesOneEvolutionEach() {
        // DEFAULT: yes
        return true;
    }

    @Override
    public boolean hasTotemPokemon() {
        // DEFAULT: no
        return false;
    }

    @Override
    public void makeDoubleBattleModePossible() {
        // do nothing; this method is just needed by some ROMs
    }

    @Override
    public boolean hasTypeEffectivenessSupport() {
        // DEFAULT: no
        return false;
    }

    @Override
    public TypeTable getTypeTable() {
        // just returns some hard-coded tables if the subclass doesn't implement actually reading from ROM
        // obviously it is better if the type table can be actually read from ROM, so override this when possible
        if (generationOfPokemon() == 1) {
            return TypeTable.getVanillaGen1Table();
        } else if (generationOfPokemon() <= 5) {
            return TypeTable.getVanillaGen2To5Table();
        } else {
            return TypeTable.getVanillaGen6PlusTable();
        }
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        // do nothing
    }

    @Override
    public String abilityName(int number) {
        return "";
    }

    @Override
    public List<Integer> getUselessAbilities() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasTimeBasedEncounters() {
        // DEFAULT: no
        return false;
    }

    @Override
    public boolean hasGuaranteedWildHeldItems() {
        // DEFAULT: yes
        return true;
    }

    @Override
    public boolean hasDarkGrassHeldItems() {
        // DEFAULT: no
        return false;
    }

    @Override
    public int getPerfectAccuracy() {
        return perfectAccuracy;
    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        return new ArrayList<>();
    }

    @Override
    public SpeciesSet getBannedForStaticPokemon() {
        return new SpeciesSet();
    }

    @Override
    public boolean forceSwapStaticMegaEvos() {
        return false;
    }

    @Override
    public List<String> getTrainerNames() {
        return getTrainers().stream().map(tr -> tr.getName()).collect(Collectors.toList());
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {
        for (int i = 0; i < trainerNames.size(); i++) {
            getTrainers().get(i).setName(trainerNames.get(i));
        }
    }

    @Override
    public int maxTrainerNameLength() {
        // default: no real limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int maxSumOfTrainerNameLengths() {
        // default: no real limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int maxTrainerClassNameLength() {
        // default: no real limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int maxTradeNicknameLength() {
        return 10;
    }

    @Override
    public int maxTradeOTNameLength() {
        return 7;
    }

    @Override
    public boolean altFormesCanHaveDifferentEvolutions() {
        return false;
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        // Sonicboom & Dragon Rage
        return Arrays.asList(49, 82);
    }

    @Override
    public List<Integer> getIllegalMoves() {
        return new ArrayList<>();
    }

    @Override
    public boolean isYellow() {
        return false;
    }

    @Override
    public boolean isORAS() {
        return false;
    }

    @Override
    public boolean isUSUM() {
        return false;
    }

    @Override
    public boolean hasMultiplePlayerCharacters() {
        return true;
    }

    @Override
    public int miscTweaksAvailable() {
        // default: none
        return 0;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        // default: do nothing
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPCPotionItem(Item item) {
        throw new UnsupportedOperationException();
    }

    protected Set<Item> itemIdsToSet(Collection<Integer> ids) {
        List<Item> allItems = getItems();
        return ids.stream()
                .filter(id -> id < allItems.size())
                .map(allItems::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Item> getAllowedItems() {
        return getItems().stream().filter(item -> item != null && item.isAllowed()).collect(Collectors.toSet());
    }

    @Override
    public Set<Item> getNonBadItems() {
        return getAllowedItems().stream().filter(item -> !item.isBad()).collect(Collectors.toSet());
    }

    @Override
    public Set<Item> getXItems() {
        return itemIdsToSet(GlobalConstants.xItems);
    }

    @Override
    public Set<Item> getRegularShopItems() {
        return itemIdsToSet(GlobalConstants.regularShopItems);
    }

    @Override
    public Set<Item> getMegaStones() {
        return Collections.emptySet();
    }

    @Override
    public List<Item> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        return Collections.singletonList(null);
    }

    @Override
    public Set<Item> getAllConsumableHeldItems() {
        return Collections.singleton(null);
    }

    /**
     * Returns a {@link Set} of all {@link Item}s that may have an effect for enemy trainers in battle.<br>
     * So e.g. Everstone is excluded, but also Metal Powder or other items that only have effects for
     * certain Pokémon species, since when picked for any other Pokémon they will do nothing.
     */
    @Override
    public Set<Item> getAllHeldItems() {
        return Collections.singleton(null);
    }

    @Override
    public SpeciesSet getBannedFormesForTrainerPokemon() {
        return new SpeciesSet();
    }

    @Override
    public List<PickupItem> getPickupItems() {
        return new ArrayList<>();
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        // do nothing
    }

    @Override
    public boolean hasPokemonPaletteSupport() {
        return false;
    }

    @Override
    public boolean pokemonPaletteSupportIsPartial() {
        return false;
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        return false;
    }

    @Override
    public void setCustomPlayerGraphics(CustomPlayerGraphics customPlayerGraphics) {
        throw new UnsupportedOperationException("Custom player graphics not supported for this game.");
    }

    @Override
    public boolean hasPokemonImageGetter() {
        return false; // default: no
    }

    // just for testing
    public final void dumpAllPokemonImages() {
        List<BufferedImage> bims = getAllPokemonImages();

        for (int i = 0; i < bims.size(); i++) {
            String fileAdress = "Pokemon_image_dump/gen" + generationOfPokemon() + "/"
                    + String.format("%03d_d.png", i + 1);
            File outputfile = new File(fileAdress);
            try {
                ImageIO.write(bims.get(i), "png", outputfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getPaletteFilesID() {
        throw new UnsupportedOperationException(); // default: assumes no resource files are needed
    }

    public abstract List<BufferedImage> getAllPokemonImages();

    @Override
    public boolean saveRom(String filename, long seed, boolean saveAsDirectory) {
        try {
            prepareSaveRom();
            return saveAsDirectory ? saveRomDirectory(filename) : saveRomFile(filename, seed);
        } catch (RomIOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Writes the remaining things to the ROM, before it is written to file. When
     * overridden, this should be called as a superclass method.
     */
    protected void prepareSaveRom() {
        saveSpeciesStats();
        saveMoves();
        saveTrainers();
        savePokemonPalettes();
    }

    public abstract void saveMoves();

    protected abstract boolean saveRomFile(String filename, long seed);

    protected abstract boolean saveRomDirectory(String filename);

    protected abstract RomEntry getRomEntry();

    @Override
    public String getROMName() {
        return "Pokemon " + getRomEntry().getName();
    }

    @Override
    public String getROMCode() {
        return getRomEntry().getRomCode();
    }

    @Override
    public int getROMType() {
        return getRomEntry().getRomType();
    }

    @Override
    public String getSupportLevel() {
        return getRomEntry().hasStaticPokemonSupport() ? "Complete" : "No Static Pokemon";
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getROMName() + " | " + getROMCode() + "]";
    }
}
