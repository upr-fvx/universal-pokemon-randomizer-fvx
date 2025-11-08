package com.dabomstew.pkromio.gamedata;

import javax.print.attribute.UnmodifiableSetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An extension of {@link HashSet} instantiated to {@link Species}.
 * Adds various helper functions useful for this class.
 * Will not add null values to the set.
 */
public class SpeciesSet extends HashSet<Species> {

    /**
     * Creates an empty {@link SpeciesSet}.
     */
    public SpeciesSet() {
        super();
    }

    /**
     * Creates a new {@link SpeciesSet} containing every {@link Species} in the given Collection.
     * @param cloneFrom the Collection to copy from.
     */
    public SpeciesSet(Collection<? extends Species> cloneFrom) {
        super(cloneFrom);
    }

    /**
     * Creates a {@link SpeciesSet} containing only the given {@link Species}.
     * @param species The {@link Species} to include in the set.
     */
    public SpeciesSet(Species species) {
        super();
        this.add(species);
    }


    //getRandomSpecies related variables
    private ArrayList<Species> randomCache = null;
    private static final double CACHE_RESET_FACTOR = 0.5;
    //Similar Strength will keep expanding until it reaches the smaller of
    //MINIMUM_POOL or total_pool / MINIMUM_POOL_FACTOR
    final int SS_MINIMUM_POOL = 5;
    final int SS_MINIMUM_POOL_FACTOR = 4;

    //How much of the cache must consist of removed Species before resetting

    //Basic functions

    @Override
    public boolean add(Species species) {
        if(this.contains(species) || species == null) {
            return false;
        }
        randomCache = null;
        super.add(species);

        return true;
    }

    //I'm not certain that I need to override addAll—but I'm not
    //certain that I don't, either.
    @Override
    public boolean addAll(Collection<? extends Species> c) {
        boolean changed = false;
        for (Species species : c) {
            boolean added = this.add(species);
            if(added) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        super.clear();
        randomCache = null;
    }

    /**
     * Returns the subset of this set for which the predicate function returns true.
     * @param predicate The function to test {@link Species} against.
     * @return A new {@link SpeciesSet} containing every {@link Species} in this set for which predicate returns true.
     */
    public SpeciesSet filter(Predicate<? super Species> predicate) {
        SpeciesSet filtered = new SpeciesSet();
        for (Species sp : this) {
            if (predicate.test(sp)) {
                filtered.add(sp);
            }
        }
        return filtered;
    }

    /**
     * Given a {@link Collection} of {@link Species}, finds whether this set shares any members with it.
     * @param other The {@link Collection} to compare.
     * @return True if any {@link Species} is in both Collections, false otherwise.
     */
    public boolean containsAny(Collection<Species> other) {
        if(this.size() < other.size()) {
            for(Species species : this) {
                if(other.contains(species)) {
                    return true;
                }
            }
        } else {
            for(Species species : other) {
                if(this.contains(species)) {
                    return true;
                }
            }
        }

        return false;
    }

    //BuildSet variants

    /**
     * Builds a new set by running the given function on each {@link Species} in this set,
     * and adding its return value to the new set.
     * @param function The function to run on each {@link Species} in this set.
     * @return The {@link SpeciesSet} containing all {@link Species} returned after running the given function.
     */
    public SpeciesSet buildSetSingle(Function<? super Species, Species> function) {
        SpeciesSet newSet = new SpeciesSet();

        for(Species species : this) {
            newSet.add(function.apply(species));
        }

        return newSet;
    }

    /**
     * Builds a new set by running the given function on each {@link Species} in this set,
     * and adding its return value to the new set.
     * @param function The function to run on each {@link Species} in this set.
     * @return The {@link SpeciesSet} containing all {@link Species} returned after running the given function.
     */
    public SpeciesSet buildSet(Function<? super Species, Collection<Species>> function) {
        SpeciesSet newSet = new SpeciesSet();

        for(Species species : this) {
            newSet.addAll(function.apply(species));
        }

        return newSet;
    }

    /**
     * Builds a new set by running the given function on each {@link Species} in this set,
     * and adding its return value to the new set only if that {@link Species} is contained in this set.
     * @param function The function to run on each {@link Species} in this set.
     * @return The {@link SpeciesSet} containing all {@link Species} in this set returned after running the given function.
     */
    public SpeciesSet filterBuiltSetSingle(Function<? super Species, Species> function) {
        SpeciesSet builtSet = this.buildSetSingle(function);
        builtSet.retainAll(this);
        return builtSet;
    }

    /**
     * Builds a new set by running the given function on each {@link Species} in this set,
     * and adding its return value to the new set only if that {@link Species} is contained in this set.
     * @param function The function to run on each {@link Species} in this set.
     * @return The {@link SpeciesSet} containing all {@link Species} in this set returned after running the given function.
     */
    public SpeciesSet filterBuiltSet(Function<? super Species, Collection<Species>> function) {
        SpeciesSet builtSet = this.buildSet(function);
        builtSet.retainAll(this);
        return builtSet;
    }

    /**
     * Adds to this set all {@link Species} that were returned by running the given function
     * on at least one {@link Species} in the set. <br>
     * @param function The function to run on all {@link Species} in the set.
     * @return Whether any {@link Species} were added to the set.
     */
    public boolean addBuiltSetSingle(Function<? super Species, Species> function) {
        SpeciesSet builtSet = this.buildSetSingle(function);
        return this.addAll(builtSet);
    }

    /**
     * Adds to this set all {@link Species} that were returned by running the given function
     * on at least one {@link Species} in the set. <br>
     * @param function The function to run on all {@link Species} in the set.
     * @return Whether any {@link Species} were added to the set.
     */
    public boolean addBuiltSet(Function<? super Species, Collection<Species>> function) {
        SpeciesSet builtSet = this.buildSet(function);
        return this.addAll(builtSet);
    }

    /**
     * Removes from this set all {@link Species} that were returned by running the given function
     * on at least one {@link Species} in the set. <br>
     * @param function The function to run on all {@link Species} in the set.
     * @return Whether any {@link Species} were removed from the set.
     */
    public boolean removeBuiltSetSingle(Function<? super Species, Species> function) {
        SpeciesSet builtSet = this.buildSetSingle(function);
        return this.removeAll(builtSet);
    }

    /**
     * Removes from this set all {@link Species} that were returned by running the given function
     * on at least one {@link Species} in the set. <br>
     * @param function The function to run on all {@link Species} in the set.
     * @return Whether any {@link Species} were removed from the set.
     */
    public boolean removeBuiltSet(Function<? super Species, Collection<Species>> function) {
        SpeciesSet builtSet = this.buildSet(function);
        return this.removeAll(builtSet);
    }

    //End basic functions

    //Type Zone
    /**
     * Returns every {@link Species} in this set which has the given type.
     * @param type The type to match.
     * @param useOriginal Whether to use type data from before randomization.
     * @return a new {@link SpeciesSet} containing every {@link Species} of the given type.
     */
    public SpeciesSet filterByType(Type type, boolean useOriginal) {
        return this.filter(p -> p.hasType(type, useOriginal));
    }

    /**
     * Sorts all {@link Species} in this set by type.
     * Significantly faster than calling filterByType for each type.
     * @param useOriginal Whether to use type data from before randomization.
     * @return A Map of {@link Species} sorted by type. <br>
     * WARNING: types with no {@link Species} will contain null rather than an empty set!
     */
    public Map<Type, SpeciesSet> sortByType(boolean useOriginal) {
        Map<Type, SpeciesSet> typeMap = new EnumMap<>(Type.class);

        for(Species spec : this) {
            addToTypeMap(typeMap, spec.getPrimaryType(useOriginal), spec);
            if(spec.hasSecondaryType(useOriginal)) {
                addToTypeMap(typeMap, spec.getSecondaryType(useOriginal), spec);
            }
        }

        return typeMap;
    }

    /**
     * Adds the given {@link Species} to the given map, creating a new {@link SpeciesSet} if needed.
     * @param type The type to add the {@link Species} to.
     * @param species The {@link Species} to add.
     */
    private void addToTypeMap(Map<Type, SpeciesSet> map, Type type, Species species) {
        SpeciesSet typeList = map.get(type);

        if(typeList == null) {
            typeList = new SpeciesSet();
            map.put(type, typeList);
        }

        typeList.add(species);
    }

    /**
     * Sorts all {@link Species} in this set by type.
     * Significantly faster than calling filterByType for each type.
     * @param useOriginal Whether to use type data from before randomization.
     * @param sortInto A Collection of the Types to sort by. Null values will be ignored.
     *                 Types not contained within this collection will not be sorted by.
     * @return A Map of {@link Species} sorted by type. Every Type given will contain a {@link SpeciesSet}, even if it is empty.
     */
    public Map<Type, SpeciesSet> sortByType(boolean useOriginal, Collection<Type> sortInto) {
        Set<Type> types = EnumSet.copyOf(sortInto);
        Map<Type, SpeciesSet> typeMap = new EnumMap<>(Type.class);
        for(Type type : types) {
            typeMap.put(type, new SpeciesSet());
        }

        for(Species spec : this) {
            if(types.contains(spec.getPrimaryType(useOriginal))) {
                typeMap.get(spec.getPrimaryType(useOriginal)).add(spec);
            }
            if(types.contains(spec.getSecondaryType(useOriginal))) {
                typeMap.get(spec.getSecondaryType(useOriginal)).add(spec);
            }
        }

        return typeMap;
    }

    /**
     * Finds if all {@link Species} in this set share a type.
     * If two types are shared, will return the primary type of an arbitrary {@link Species} in the set,
     * unless that type is Normal; in this case will return the secondary type.
     * @param useOriginal Whether to use type data from before randomization.
     * @return The Type shared by all the {@link Species}, or null if none was shared.
     */
    public Type getSharedType(boolean useOriginal) {
        if(this.isEmpty()) {
            return null;
        }
        Iterator<Species> itor = this.iterator();
        Species spec = itor.next();
        Type primary = spec.getPrimaryType(useOriginal);
        Type secondary = spec.getSecondaryType(useOriginal);

        while(itor.hasNext()) {
            spec = itor.next();
            if(secondary != null) {
                if (!spec.hasType(secondary, useOriginal)) {
                    secondary = null;
                }
            }
            if (!spec.hasType(primary, useOriginal)) {
                primary = secondary;
                secondary = null;
            }
            if (primary == null) {
                return null; //we've determined there's no type theme, no need to run through the rest of the set.
            }
        }

        if(primary == Type.NORMAL && secondary != null) {
            return secondary;
        } else {
            return primary;
        }
    }

    /**
     * Finds if all {@link Species} in this set share one or more types.
     * @param useOriginal Whether to use type data from before randomization.
     * @return The set of Types shared by all {@link Species} in the set, or an empty set if none were shared.
     */
    public Set<Type> getSharedTypes(boolean useOriginal) {
        if(this.isEmpty()) {
            return EnumSet.noneOf(Type.class);
        }
        Set<Type> sharedTypes = EnumSet.allOf(Type.class);

        for(Species spec : this) {
            sharedTypes.removeIf(t -> !spec.hasType(t, useOriginal));
            if(sharedTypes.isEmpty()) {
                break;
            }
        }

        return sharedTypes;
    }

    //End Type Zone

    //Evolution methods

    /**
     * Returns all {@link Species} in this set that the given {@link Species} can evolve directly into.
     * @param species The {@link Species} to get evolutions of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all direct evolutions of the given {@link Species} in this set.
     */
    public SpeciesSet filterEvolutions(Species species, boolean useOriginal) {
        SpeciesSet evolvedSpecies = species.getEvolvedSpecies(useOriginal);
        evolvedSpecies.retainAll(this);
        return evolvedSpecies;
    }

    /**
     * Returns all {@link Species} in this set that the given {@link Species} evolves from.
     * @param species The {@link Species} to get the pre-evolution of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all {@link Species} that the given {@link Species} evolves from in this set.
     */
    public SpeciesSet filterPreEvolutions(Species species, boolean useOriginal) {
        //I *think* there are no cases of merged evolution? But... better not to assume that.
        SpeciesSet preEvolvedSpecies = species.getPreEvolvedSpecies(useOriginal);
        preEvolvedSpecies.retainAll(this);
        return preEvolvedSpecies;
    }

    /**
     * Checks whether this set contains any directly evolved forms of the given {@link Species}.
     * @param species The {@link Species} to check for evolutions of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return true if this set contains at least one evolved form of the given {@link Species}, false otherwise.
     */
    public boolean hasEvolutions(Species species, boolean useOriginal) {
        SpeciesSet evolvedSpecies = species.getEvolvedSpecies(useOriginal);
        for (Species evo : evolvedSpecies) {
            if(this.contains(evo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this set contains any {@link Species} that can evolve directly into the given {@link Species}.
     * @param species The {@link Species} to check for pre-evolutions of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return true if this set contains at least one pre-evolved form of the given {@link Species}, false otherwise.
     */
    public boolean hasPreEvolutions(Species species, boolean useOriginal) {
        SpeciesSet preEvolvedSpecies = species.getPreEvolvedSpecies(useOriginal);
        for (Species prevo : preEvolvedSpecies) {
            if(this.contains(prevo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all {@link Species} in this set that no other {@link Species} in this set evolves into.
     * @param contiguous Whether to keep {@link Species} that other {@link Species} in the set can evolve indirectly into.
     *                   For example, if this set includes Weedle and Beedrill but not Kakuna,
     *                   the returned set will include Beedrill only if this parameter is true.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all {@link Species} that no other {@link Species} in this set evolves into.
     */
    public SpeciesSet filterFirstEvolutionAvailable(boolean contiguous, boolean useOriginal) {
        SpeciesSet firstInLines = new SpeciesSet();
        for(Species species : this) {
            if (contiguous) {
                if (!this.hasPreEvolutions(species, useOriginal)) {
                    firstInLines.add(species);
                }

            } else {

                SpeciesSet checked = new SpeciesSet();
                Queue<Species> toCheck = new ArrayDeque<>();
                toCheck.add(species);
                boolean hasPrevo = false;
                while(!toCheck.isEmpty()) {
                    Species checking = toCheck.remove();
                    if(checked.contains(checking)) {
                        continue; //continue inner loop only
                    }
                    if(this.hasPreEvolutions(checking, useOriginal)) {
                        hasPrevo = true;
                        break; //break inner loop only
                    }
                    checked.add(checking);

                    toCheck.addAll(species.getPreEvolvedSpecies(useOriginal));
                }

                if(!hasPrevo) {
                    firstInLines.add(species);
                }
            }
        }
        return firstInLines;
    }

    /**
     * Returns all {@link Species} in this set that no other {@link Species} (in this set or otherwise) evolves into.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all basic {@link Species} in this set.
     */
    public SpeciesSet filterBasic(boolean useOriginal) {
        return this.filter(p -> p.getPreEvolvedSpecies(useOriginal).isEmpty());
    }

    /**
     * Returns all {@link Species} in this set that evolve into no other {@link Species} (in this set or otherwise).
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all final-evo {@link Species} in this set.
     */
    public SpeciesSet filterFinalEvos(boolean useOriginal) {
        return this.filter(p -> p.getEvolvedSpecies(useOriginal).isEmpty());
    }

    /**
     * Returns all {@link Species} in this set that are related to no other {@link Species} (in this set or otherwise).
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all non-evolving {@link Species} in this set.
     */
    public SpeciesSet filterStandalone(boolean useOriginal) {
        return this.filter(p -> p.getPreEvolvedSpecies(useOriginal).isEmpty() &&
                p.getEvolvedSpecies(useOriginal).isEmpty());
    }

    /**
     * Returns all {@link Species} in this set that both evolve from and to at least one other {@link Species}.
     * The related {@link Species} are not required to be in the set.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all middle-evo {@link Species} in this set.
     */
    public SpeciesSet filterMiddleEvos(boolean useOriginal) {
        return this.filter(p -> !p.getPreEvolvedSpecies(useOriginal).isEmpty() &&
                !p.getEvolvedSpecies(useOriginal).isEmpty());
    }

    /**
     * Returns all {@link Species} in this set that evolve from a {@link Species} which can evolve into two or more different {@link Species}.
     * The related {@link Species} are not required to be in the set.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all split-evo {@link Species} in this set.
     */
    public SpeciesSet filterSplitEvolutions(boolean useOriginal) {
        // TODO: there was a notion in earlier code, of treating Ninjask only as a non-split evo
        //  (or technically Species which evolved through EvolutionType.LEVEL_CREATE_EXTRA).
        //  Is this something we want to recreate?
        return filter(p -> {
            for (Species pre : p.getPreEvolvedSpecies(useOriginal)) {
                if (pre.getEvolvedSpecies(useOriginal).size() > 1) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Returns all {@link Species} for which evolutionary lines of at least the given length are contained
     * within this set.
     * In the case of branching evolutions, only branches of the correct length will be included.
     * @param length the number of {@link Species} required in the evolutionary line. At least 1. Counts itself.
     * @param allowGaps Whether to allow lines with one or more of the middle {@link Species} missing.
     *                  For example, if allowGaps is true, and the set contains Weedle and Beedrill but not Kakuna,
     *                  they will be included in the returned set if length is 2 or 3.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a {@link SpeciesSet} containing all valid {@link Species}.
     */
    public SpeciesSet filterEvoLinesAtLeastLength(int length, boolean allowGaps, boolean useOriginal) {
        if(length < 1) {
            throw new IllegalArgumentException("Invalid evolutionary line length.");
        }
        if(length == 1) {
            return new SpeciesSet(this);
        }

        SpeciesSet validEvoLines = new SpeciesSet();
        SpeciesSet firstInLines = this.filterFirstEvolutionAvailable(!allowGaps, useOriginal);

        Map<Species, SpeciesSet> currentStage = new HashMap<>();
        for(Species species : firstInLines) {
            currentStage.put(species, new SpeciesSet());
        }

        for(int i = 2; !currentStage.isEmpty(); i++) {
            Map<Species, SpeciesSet> nextStage = new HashMap<>();
            for(Species spec : currentStage.keySet()) {
                if(!allowGaps && !this.contains(spec)) {
                    //kill this line
                    continue;
                }

                SpeciesSet lineSoFar = new SpeciesSet(currentStage.get(spec));
                if(this.contains(spec)){
                    lineSoFar.add(spec);

                    if(i >= length) {
                        validEvoLines.addAll(lineSoFar);
                    }
                }

                for(Species evo : spec.getEvolvedSpecies(useOriginal)) {
                    nextStage.put(evo, lineSoFar);
                }
            }

            currentStage = nextStage;
        }

        return validEvoLines;
    }

    /**
     * Gets all {@link Species} in the set which have at least the specified number of evolution stages
     * before and after them. <br>
     * If the {@link Species} is in a cycle, guarantees that the total length of the cycle is greater than
     * before + 1 + after.
     * @param before The number of stages before this {@link Species}. 0+.
     * @param after The number of stages after this {@link Species}. 0+.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A {@link SpeciesSet} containing all {@link Species} in the specified position.
     */
    public SpeciesSet filterHasEvoStages(int before, int after, boolean useOriginal) {
        if (before < 0 || after < 0) {
            throw new IllegalArgumentException("Cannot have a negative number of evolutions!");
        }
        if (before == 0 && after == 0) {
            //impossible not to have these
            return new SpeciesSet(this);
        }

        return filter(p -> {
            if (this.isInEvoCycle(p, useOriginal)) {
                return this.getNumberEvoStagesAfter(p, useOriginal) >= before + 1 + after;
            } else {
                return getNumberEvoStagesBefore(p, useOriginal) >= before
                        && getNumberEvoStagesAfter(p, useOriginal) >= after;
            }
        });
    }

    /**
     * Finds the largest number of evolutionary steps contained in this set that could evolve into
     * the given {@link Species}.
     * For example, a {@link Species} which has both a basic and a once-evolved {@link Species} that can evolve into
     * it would return 2 (for the once-evolved {@link Species}).
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial {@link Species}.
     * @param species The {@link Species} to find the evolutionary count for.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The number of {@link Species} in the longest line before this {@link Species}.
     */
    public int getNumberEvoStagesBefore(Species species, boolean useOriginal) {
        int numStages = 0;
        SpeciesSet currentStage = new SpeciesSet();
        currentStage.add(species);

        SpeciesSet checked = new SpeciesSet();

        while(!currentStage.isEmpty()) {
            SpeciesSet previousStage = new SpeciesSet();
            for(Species spec : currentStage) {
                if(checked.contains(spec)) {
                    continue;
                }
                previousStage.addAll(this.filterPreEvolutions(spec, useOriginal));
                checked.add(spec);
            }
            if(!previousStage.isEmpty()) {
                numStages++;
            }
            currentStage = previousStage;
        }

        return numStages;
    }

    /**
     * Finds the largest number of evolutionary steps contained in this set that the given {@link Species}
     * can evolve into.<br>
     * For example, a {@link Species} which evolves into two {@link Species}, one of which can evolve again,
     * would return 2 (for the {@link Species} which can evolve again.)<br>
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial {@link Species}.
     * @param species The {@link Species} to find the evolutionary count for.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The number of {@link Species} in the longest line after this {@link Species}.
     */
    public int getNumberEvoStagesAfter(Species species, boolean useOriginal) {
        int numStages = 0;
        SpeciesSet currentStage = new SpeciesSet();
        currentStage.add(species);
        SpeciesSet checked = new SpeciesSet();

        while(!currentStage.isEmpty()) {
            SpeciesSet nextStage = new SpeciesSet();
            for(Species spec : currentStage) {
                if(checked.contains(spec)) {
                    continue;
                }
                nextStage.addAll(this.filterEvolutions(spec, useOriginal));
                checked.add(spec);
            }
            if(!nextStage.isEmpty()) {
                numStages++;
            }
            currentStage = nextStage;
        }

        return numStages;
    }

    /**
     * Finds the longest evolutionary line in this set that the given {@link Species} belongs to. <br>
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial {@link Species}.
     * @param species The {@link Species} to find an evolutionary line for.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The number of {@link Species} in the longest evolutionary line, including itself.
     */
    public int getLongestEvoLine(Species species, boolean useOriginal) {
        if(isInEvoCycle(species, useOriginal)) {
            return getNumberEvoStagesAfter(species, useOriginal);
        } else {
            return getNumberEvoStagesBefore(species, useOriginal) + 1
                    + getNumberEvoStagesAfter(species, useOriginal);
        }
    }

    /**
     * Checks whether the given {@link Species} is in an evolutionary cycle for which all {@link Species}
     * are contained within this set.
     * @param species The {@link Species} to check for cycles.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return Whether the {@link Species} is in a cycle contained in this set.
     */
    public boolean isInEvoCycle(Species species, boolean useOriginal) {
        SpeciesSet currentStage = new SpeciesSet();
        currentStage.addAll(species.getEvolvedSpecies(useOriginal));
        SpeciesSet checked = new SpeciesSet();

        while(!currentStage.isEmpty()) {
            SpeciesSet nextStage = new SpeciesSet();
            for(Species spec : currentStage) {
                if(spec == species) {
                    return true;
                }
                if(checked.contains(spec)) {
                    continue;
                }
                nextStage.addAll(this.filterEvolutions(spec, useOriginal));
                checked.add(spec);
            }
            currentStage = nextStage;
        }

        return false;
    }

    //Evolution subset: Family methods

    /**
     * Adds the given {@link Species} and every evolutionary relative to this set, if they are
     * not already contained in the set.
     *
     * @param species     The {@link Species} to add the family of.
     * @param useOriginal Whether to use the pre-randomization evolution data.
     * @return True if any {@link Species} were added to the set, false otherwise.
     */
    public boolean addFamily(Species species, boolean useOriginal) {
        return addAll(species.getFamily(useOriginal));
    }

    /**
     * Removes the given {@link Species} and every evolutionary relative from this set, if they
     * are contained in the set.
     *
     * @param species The {@link Species} to remove the family of.
     * @return If any {@link Species} were removed from the set.
     */
    public boolean removeFamily(Species species, boolean useOriginal) {
        return removeAll(species.getFamily(useOriginal));
    }

    /**
     * Returns all members of the given {@link Species}'s evolutionary family that this set contains.
     * Returns an empty set if no members are in this set.
     *
     * @param species     The {@link Species} to get the family of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a {@link SpeciesSet} containing every member of the given {@link Species}'s family
     */
    public SpeciesSet filterFamily(Species species, boolean useOriginal) {
        SpeciesSet family = species.getFamily(useOriginal);
        family.retainAll(this);
        return family;
    }

    /**
     * Creates a new set containing the full evolutionary families of all {@link Species} in this set.
     *
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a {@link SpeciesSet} containing all {@link Species} in this set and all their evolutionary relatives.
     */
    public SpeciesSet buildFullFamilies(boolean useOriginal) {
        //Could just use buildSetMulti, but this is more efficient
        //(And it's already written)
        SpeciesSet allRelatedSpecies = new SpeciesSet();
        for (Species species : this) {
            if (!allRelatedSpecies.contains(species)) {
                allRelatedSpecies.addFamily(species, useOriginal);
            }
        }
        return allRelatedSpecies;
    }

    /**
     * Adds to this set the evolutionary families of every {@link Species} in this set.
     *
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return True if any {@link Species} were added, false otherwise.
     */
    public boolean addFullFamilies(boolean useOriginal) {
        SpeciesSet allWithFamilies = this.buildFullFamilies(useOriginal);
        return this.addAll(allWithFamilies);
    }

    /**
     * Adds to this set all {@link Species} in the given set and their full evolutionary families.
     *
     * @param source      The set to add {@link Species} and families from.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return True if any {@link Species} were added, false otherwise.
     */
    public boolean addAllFamilies(SpeciesSet source, boolean useOriginal) {
        boolean anyChanged = false;
        for (Species spec : source) {
            boolean changed = this.addFamily(spec, useOriginal);
            if (changed) {
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    /**
     * Removes from this set all {@link Species} that are neither contained within the given set nor
     * an evolutionary relative of a {@link Species} that is.
     *
     * @param source      The set to keep {@link Species} and families from.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return True if any {@link Species} were removes, false otherwise.
     */
    public boolean retainAllFamilies(SpeciesSet source, boolean useOriginal) {
        SpeciesSet families = source.buildFullFamilies(useOriginal);

        return this.retainAll(families);
    }

    /**
     * Returns all members of the given {@link Species}'s evolutionary family
     * that are contained uninterrupted within this set.
     * Returns an empty set if the given {@link Species} is not in this set.
     *
     * @param species     The {@link Species} to get the family of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a {@link SpeciesSet} containing every {@link Species} related to the given {@link Species} by members of this set.
     */
    public SpeciesSet filterContiguousFamily(Species species, boolean useOriginal) {
        SpeciesSet family = new SpeciesSet();
        if (!this.contains(species)) {
            return family;
        }

        Queue<Species> toCheck = new ArrayDeque<>();
        toCheck.add(species);
        while (!toCheck.isEmpty()) {
            Species checking = toCheck.remove();
            if (family.contains(checking)) {
                continue;
            }
            family.add(checking);

            toCheck.addAll(this.filterEvolutions(checking, useOriginal));
            toCheck.addAll(this.filterPreEvolutions(checking, useOriginal));
        }

        return family;
    }

    //end evolution and family methods

    //randoms

    /**
     * Chooses a random {@link Species} from the set.
     * @param random A seeded random number generator.
     * @return A random {@link Species} from the set.
     * @throws IllegalStateException if the set is empty.
     */
    public Species getRandomSpecies(Random random) {
        return this.getRandomSpecies(random, false);
    }

    /**
     * Chooses a random {@link Species} from the set.
     * @param random A seeded random number generator.
     * @param removePicked Whether to remove the {@link Species} chosen.
     * @return A random {@link Species} from the set.
     * @throws IllegalStateException if the set is empty.
     */
    public Species getRandomSpecies(Random random, boolean removePicked) {
        if(this.isEmpty()) {
            throw new IllegalStateException("Tried to choose a random member of an empty set!");
        }

        //make sure cache state is good
        if(randomCache == null) {
            randomCache = new ArrayList<>(this);
        }
        if((double) this.size() / (double) randomCache.size() > CACHE_RESET_FACTOR)
        {
            randomCache = new ArrayList<>(this);
        }

        //ok, we should be good to randomize
        while(true) {
            int choice = random.nextInt(randomCache.size());
            Species spec = randomCache.get(choice);
            if(!this.contains(spec)) {
                continue;
            }

            if(removePicked) {
                this.remove(spec);
            }
            return spec;
        }

    }

    /**
     * Gets a random {@link Species} from the set with BST "similar" to the given value.
     * @param bst The BST to find {@link Species} near. Cannot be negative.
     * @param random A seeded random number generator.
     * @return A random {@link Species} from the set with "similar" BST.
     */
    public Species getRandomSimilarStrengthSpecies(int bst, Random random) {
        if(bst < 0) {
            throw new IllegalArgumentException("Cannot find similar to a negative BST!");
        }
        return getRandomSimilarStrengthSpecies(null, false, bst, random);
    }

    /**
     * Gets a random {@link Species} from the set with BST "similar" to the given {@link Species}'s.
     * @param match The {@link Species} to find a "similar" BST to.
     * @param random A seeded random number generator.
     * @return A random {@link Species} from the set with "similar" BST.
     */
    public Species getRandomSimilarStrengthSpecies(Species match, Random random) {
        return getRandomSimilarStrengthSpecies(match, false, -1, random);
    }

    /**
     * Gets a random {@link Species} from the set with BST "similar" to the given {@link Species}'s, or to the
     * given integer if it is not negative.
     * @param match The {@link Species} to find a "similar" BST to.
     * @param overrideBST If not negative, use this value instead of the given {@link Species}'s actual BST.
     *                    Ignored if negative.
     * @param random A seeded random number generator.
     * @return A random {@link Species} from the set with "similar" BST.
     */
    public Species getRandomSimilarStrengthSpecies(Species match, int overrideBST, Random random) {
        return getRandomSimilarStrengthSpecies(match, false, overrideBST, random);
    }

    /**
     * Gets a random {@link Species} from the set with BST "similar" to the given {@link Species}'s.
     * @param match The {@link Species} to find a "similar" BST to.
     * @param notSameSpecies If true, will exclude the given {@link Species} from the possible returns.
     *                       Ignored if the given {@link Species} is the only {@link Species} in the set.
     * @param random A seeded random number generator.
     * @return A random {@link Species} from the set with "similar" BST.
     */
    public Species getRandomSimilarStrengthSpecies(Species match, boolean notSameSpecies, Random random) {
        return getRandomSimilarStrengthSpecies(match, notSameSpecies, -1, random);
    }

    /**
     * Gets a random {@link Species} from the set with BST "similar" to the given {@link Species}'s, or to the
     * given integer if it is not negative.
     * @param match The {@link Species} to find a "similar" BST to.
     * @param notSameSpecies If true, will exclude the given {@link Species} from the possible returns.
     *                       Ignored if the given {@link Species} is the only {@link Species} in the set.
     * @param overrideBST If not negative, use this value instead of the given {@link Species}'s actual BST.
     *                    Ignored if negative.
     * @param random A seeded random number generator.
     * @return A random {@link Species} from the set with "similar" BST.
     */
    public Species getRandomSimilarStrengthSpecies(Species match, boolean notSameSpecies,
                                                   int overrideBST, Random random) {
        SpeciesSet availablePool = new SpeciesSet(this); //clone for draining
        if(notSameSpecies) {
            availablePool.remove(match);
        }

        if(availablePool.isEmpty()) {
            if(this.isEmpty()) {
                throw new IllegalStateException("Attempted to choose a Pokemon Species from an empty set!");
            } else {
                //if availablePool is empty, but this set isn't, match must be the only Species in the set.
                return match;
            }
        }

        int minimumPool = Math.min(SS_MINIMUM_POOL, availablePool.size() / SS_MINIMUM_POOL_FACTOR);
        if(minimumPool < 1) {
            minimumPool = 1;
        }
        if (minimumPool >= availablePool.size()) {
            //must use the whole pool
            //(I think this only happens if there's exactly one Species to choose.)
            return availablePool.getRandomSpecies(random);
        }

        // start with within 10% and add 5% either direction until the pool is big enough
        int matchBST;
        if(overrideBST < 0) {
            matchBST = match.getBSTForPowerLevels();
        } else {
            matchBST = overrideBST;
        }

        int minTarget = matchBST - matchBST / 10;
        int maxTarget = matchBST + matchBST / 10;
        SpeciesSet canPick = new SpeciesSet();
        while (canPick.size() < minimumPool) {
            Iterator<Species> itor = availablePool.iterator();
            while (itor.hasNext()) {
                Species spec = itor.next();
                if(spec.getBSTForPowerLevels() >= minTarget && spec.getBSTForPowerLevels() <= maxTarget) {
                    canPick.add(spec);
                    itor.remove();
                }
            }
            minTarget -= matchBST / 20;
            maxTarget += matchBST / 20;
        }
        return canPick.getRandomSpecies(random);
    }

    //end randoms

    //Various Functions

    public String toStringShort() {
        StringBuilder string = new StringBuilder("[");
        Iterator<Species> itor = this.iterator();
        while(itor.hasNext()) {
            Species species = itor.next();
            string.append(species.getFullName());
            if(itor.hasNext()) {
                //friggin' loop-and-a-half
                string.append(", ");
            }
        }
        string.append("]");
        return string.toString();
    }

    //End Various Functions
    
    //Subclass
    
    public static SpeciesSet unmodifiable(Collection<Species> source) {
        return new UnmodifiableSpeciesSet(source);
    }

    /**
     * Just what it sounds like, a {@link SpeciesSet} which throws {@link UnmodifiableSetException}
     * whenever modifications are attempted.
     */
    private static class UnmodifiableSpeciesSet extends SpeciesSet {
        private final boolean unmodifiable;

        public UnmodifiableSpeciesSet(Collection<? extends Species> original) {
            super(original);
            unmodifiable = true; // since you can't use the super constructor if add() is always impossible
        }

        @Override
        public boolean add(Species sp) {
            if (unmodifiable) {
                throw new UnmodifiableSetException();
            } else {
                return super.add(sp);
            }
        }

        @Override
        public boolean remove(Object o) {
            throw new UnmodifiableSetException();
        }

        @Override
        public void clear() {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean removeIf(Predicate filter) {
            throw new UnmodifiableSetException();
        }

        // overriding the iterator is important to disable its remove()
        @Override
        public Iterator<Species> iterator() {
            return new Iterator<Species>() {
                private final Iterator<Species> inner = UnmodifiableSpeciesSet.super.iterator();

                @Override
                public boolean hasNext() {
                    return inner.hasNext();
                }

                @Override
                public Species next() {
                    return inner.next();
                }

                @Override
                public void remove() {
                    throw new UnmodifiableSetException();
                }
            };
        }
    }
    
    //End subclass
}