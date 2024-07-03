package com.dabomstew.pkrandom.pokemon;

import com.dabomstew.pkrandom.exceptions.RandomizationException;

import javax.print.attribute.UnmodifiableSetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An extension of {@link PokemonSet} instantiated to Pokemon.
 * Adds various evolution-related functions.
 */
public class PokemonSet extends HashSet<Pokemon> {

    /**
     * Creates an empty PokemonSet.
     */
    public PokemonSet() {
        super();
    }

    /**
     * Creates a new PokemonSet containing every Pokemon in the given Collection.
     * @param cloneFrom the Collection to copy from.
     */
    public PokemonSet(Collection<? extends Pokemon> cloneFrom) {
        super(cloneFrom);
    }

    /**
     * Creates a PokemonSet containing only the given Pokemon.
     * @param pokemon The Pokemon to include in the set.
     */
    public PokemonSet(Pokemon pokemon) {
        super();
        this.add(pokemon);
    }


    //getRandomPokemon related variables
    private ArrayList<Pokemon> randomCache = null;
    private static final double CACHE_RESET_FACTOR = 0.5;
    //Similar Strength will keep expanding until it reaches the smaller of
    //MINIMUM_POOL or total_pool / MINIMUM_POOL_FACTOR
    final int SS_MINIMUM_POOL = 5;
    final int SS_MINIMUM_POOL_FACTOR = 4;

    //How much of the cache must consist of removed Pokemon before resetting

    //Basic functions

    @Override
    public boolean add(Pokemon pokemon) {
        if(this.contains(pokemon) || pokemon == null) {
            return false;
        }
        randomCache = null;
        super.add(pokemon);

        return true;
    }

    //I'm not certain that I need to override addAll—but I'm not
    //certain that I don't, either.
    @Override
    public boolean addAll(Collection<? extends Pokemon> c) {
        boolean changed = false;
        for (Pokemon pokemon : c) {
            boolean added = this.add(pokemon);
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
     * @param predicate The function to test Pokemon against.
     * @return A PokemonSet containing every Pokemon in this set for which predicate returns true.
     */
    public PokemonSet filter(Predicate<? super Pokemon> predicate) {
        PokemonSet filtered = new PokemonSet();
        for (Pokemon pk : this) {
            if (predicate.test(pk)) {
                filtered.add(pk);
            }
        }
        return filtered;
    }

    //BuildSet variants

    /**
     * Builds a new set by running the given function on each Pokemon in this set,
     * and adding its return value to the new set.
     * @param function The function to run on each Pokemon in this set.
     * @return The PokemonSet containing all Pokemon returned after running the given function.
     */
    public PokemonSet buildSetSingle(Function<? super Pokemon, Pokemon> function) {
        PokemonSet newSet = new PokemonSet();

        for(Pokemon pokemon : this) {
            newSet.add(function.apply(pokemon));
        }

        return newSet;
    }

    /**
     * Builds a new set by running the given function on each Pokemon in this set,
     * and adding its return value to the new set.
     * @param function The function to run on each Pokemon in this set.
     * @return The PokemonSet containing all Pokemon returned after running the given function.
     */
    public PokemonSet buildSet(Function<? super Pokemon, Collection<Pokemon>> function) {
        PokemonSet newSet = new PokemonSet();

        for(Pokemon pokemon : this) {
            newSet.addAll(function.apply(pokemon));
        }

        return newSet;
    }

    /**
     * Builds a new set by running the given function on each Pokemon in this set,
     * and adding its return value to the new set only if that Pokemon is contained in this set.
     * @param function The function to run on each Pokemon in this set.
     * @return The PokemonSet containing all Pokemon in this set returned after running the given function.
     */
    public PokemonSet filterBuiltSetSingle(Function<? super Pokemon, Pokemon> function) {
        PokemonSet builtSet = this.buildSetSingle(function);
        builtSet.retainAll(this);
        return builtSet;
    }

    /**
     * Builds a new set by running the given function on each Pokemon in this set,
     * and adding its return value to the new set only if that Pokemon is contained in this set.
     * @param function The function to run on each Pokemon in this set.
     * @return The PokemonSet containing all Pokemon in this set returned after running the given function.
     */
    public PokemonSet filterBuiltSet(Function<? super Pokemon, Collection<Pokemon>> function) {
        PokemonSet builtSet = this.buildSet(function);
        builtSet.retainAll(this);
        return builtSet;
    }

    /**
     * Adds to this set all Pokemon that were returned by running the given function
     * on at least one Pokemon in the set. <br>
     * @param function The function to run on all Pokemon in the set.
     * @return Whether any Pokemon were added to the set.
     */
    public boolean addBuiltSetSingle(Function<? super Pokemon, Pokemon> function) {
        PokemonSet builtSet = this.buildSetSingle(function);
        return this.addAll(builtSet);
    }

    /**
     * Adds to this set all Pokemon that were returned by running the given function
     * on at least one Pokemon in the set. <br>
     * @param function The function to run on all Pokemon in the set.
     * @return Whether any Pokemon were added to the set.
     */
    public boolean addBuiltSet(Function<? super Pokemon, Collection<Pokemon>> function) {
        PokemonSet builtSet = this.buildSet(function);
        return this.addAll(builtSet);
    }

    /**
     * Removes from this set all Pokemon that were returned by running the given function
     * on at least one Pokemon in the set. <br>
     * @param function The function to run on all Pokemon in the set.
     * @return Whether any Pokemon were removed from the set.
     */
    public boolean removeBuiltSetSingle(Function<? super Pokemon, Pokemon> function) {
        PokemonSet builtSet = this.buildSetSingle(function);
        return this.removeAll(builtSet);
    }

    /**
     * Removes from this set all Pokemon that were returned by running the given function
     * on at least one Pokemon in the set. <br>
     * @param function The function to run on all Pokemon in the set.
     * @return Whether any Pokemon were removed from the set.
     */
    public boolean removeBuiltSet(Function<? super Pokemon, Collection<Pokemon>> function) {
        PokemonSet builtSet = this.buildSet(function);
        return this.removeAll(builtSet);
    }

    //End basic functions

    //Type Zone
    /**
     * Returns every Pokemon in this set which has the given type.
     * @param type The type to match.
     * @param useOriginal Whether to use type data from before randomization.
     * @return a new PokemonSet containing every Pokemon of the given type.
     */
    public PokemonSet filterByType(Type type, boolean useOriginal) {
        return this.filter(p -> p.hasType(type, useOriginal));
    }

    /**
     * Sorts all Pokemon in this set by type.
     * Significantly faster than calling filterByType for each type.
     * @param useOriginal Whether to use type data from before randomization.
     * @return A Map of Pokemon sorted by type. <br>
     * WARNING: types with no Pokemon will contain null rather than an empty set!
     */
    public Map<Type, PokemonSet> sortByType(boolean useOriginal) {
        Map<Type, PokemonSet> typeMap = new EnumMap<>(Type.class);

        for(Pokemon poke : this) {
            addToTypeMap(typeMap, poke.getPrimaryType(useOriginal), poke);
            if(poke.hasSecondaryType(useOriginal)) {
                addToTypeMap(typeMap, poke.getSecondaryType(useOriginal), poke);
            }
        }

        return typeMap;
    }

    /**
     * Adds the given pokemon to the given map, creating a new PokemonSet if needed.
     * @param type The type to add the Pokemon to.
     * @param pokemon The Pokemon to add.
     */
    private void addToTypeMap(Map<Type, PokemonSet> map, Type type, Pokemon pokemon) {
        PokemonSet typeList = map.get(type);

        if(typeList == null) {
            typeList = new PokemonSet();
            map.put(type, typeList);
        }

        typeList.add(pokemon);
    }

    /**
     * Sorts all Pokemon in this set by type.
     * Significantly faster than calling filterByType for each type.
     * @param useOriginal Whether to use type data from before randomization.
     * @param sortInto A Collection of the Types to sort by. Null values will be ignored.
     *                 Types not contained within this collection will not be sorted by.
     * @return A Map of Pokemon sorted by type. Every Type given will contain a PokemonSet, even if it is empty.
     */
    public Map<Type, PokemonSet> sortByType(boolean useOriginal, Collection<Type> sortInto) {
        Set<Type> types = EnumSet.copyOf(sortInto);
        Map<Type, PokemonSet> typeMap = new EnumMap<>(Type.class);
        for(Type type : types) {
            typeMap.put(type, new PokemonSet());
        }

        for(Pokemon poke : this) {
            if(types.contains(poke.getPrimaryType(useOriginal))) {
                typeMap.get(poke.getPrimaryType(useOriginal)).add(poke);
            }
            if(types.contains(poke.getSecondaryType(useOriginal))) {
                typeMap.get(poke.getSecondaryType(useOriginal)).add(poke);
            }
        }

        return typeMap;
    }

    /**
     * Finds if all Pokemon in this set share a type.
     * If two types are shared, will return the primary type of an arbitrary Pokemon in the set,
     * unless that type is Normal; in this case will return the secondary type.
     * @param useOriginal Whether to use type data from before randomization.
     * @return The Type shared by all the pokemon, or null if none was shared.
     */
    public Type getSharedType(boolean useOriginal) {
        if(this.isEmpty()) {
            return null;
        }
        Iterator<Pokemon> itor = this.iterator();
        Pokemon poke = itor.next();
        Type primary = poke.getPrimaryType(useOriginal);
        Type secondary = poke.getSecondaryType(useOriginal);

        while(itor.hasNext()) {
            poke = itor.next();
            if(secondary != null) {
                if (!poke.hasType(secondary, useOriginal)) {
                    secondary = null;
                }
            }
            if (!poke.hasType(primary, useOriginal)) {
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

    //End Type Zone

    //Evolution methods

    /**
     * Returns all Pokemon in this set that the given Pokemon can evolve directly into.
     * @param pokemon The Pokemon to get evolutions of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all direct evolutions of the given Pokemon in this set.
     */
    public PokemonSet filterEvolutions(Pokemon pokemon, boolean useOriginal) {
        PokemonSet evolvedPokemon = pokemon.getEvolvedPokemon(useOriginal);
        evolvedPokemon.retainAll(this);
        return evolvedPokemon;
    }

    /**
     * Returns all Pokemon in this set that the given Pokemon evolves from.
     * @param pokemon The Pokemon to get the pre-evolution of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all Pokemon that the given Pokemon evolves from in this set.
     */
    public PokemonSet filterPreEvolutions(Pokemon pokemon, boolean useOriginal) {
        //I *think* there are no cases of merged evolution? But... better not to assume that.
        PokemonSet preEvolvedPokemon = pokemon.getPreEvolvedPokemon(useOriginal);
        preEvolvedPokemon.retainAll(this);
        return preEvolvedPokemon;
    }

    /**
     * Checks whether this set contains any directly evolved forms of the given Pokemon.
     * @param pokemon The Pokemon to check for evolutions of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return true if this set contains at least one evolved form of the given Pokemon, false otherwise.
     */
    public boolean hasEvolutions(Pokemon pokemon, boolean useOriginal) {
        PokemonSet evolvedPokemon = pokemon.getEvolvedPokemon(useOriginal);
        for (Pokemon evo : evolvedPokemon) {
            if(this.contains(evo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this set contains any Pokemon that can evolve directly into the given Pokemon.
     * @param pokemon The Pokemon to check for pre-evolutions of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return true if this set contains at least one pre-evolved form of the given Pokemon, false otherwise.
     */
    public boolean hasPreEvolutions(Pokemon pokemon, boolean useOriginal) {
        PokemonSet preEvolvedPokemon = pokemon.getPreEvolvedPokemon(useOriginal);
        for (Pokemon prevo : preEvolvedPokemon) {
            if(this.contains(prevo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all Pokemon in this set that no other Pokemon in this set evolves into.
     * @param contiguous Whether to keep Pokemon that other Pokemon in the set can evolve indirectly into.
     *                   For example, if this set includes Weedle and Beedrill but not Kakuna,
     *                   the returned set will include Beedrill only if this parameter is true.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all Pokemon that no other Pokemon in this set evolves into.
     */
    public PokemonSet filterFirstEvolutionAvailable(boolean contiguous, boolean useOriginal) {
        PokemonSet firstInLines = new PokemonSet();
        for(Pokemon pokemon : this) {
            if (contiguous) {
                if (!this.hasPreEvolutions(pokemon, useOriginal)) {
                    firstInLines.add(pokemon);
                }

            } else {

                PokemonSet checked = new PokemonSet();
                Queue<Pokemon> toCheck = new ArrayDeque<>();
                toCheck.add(pokemon);
                boolean hasPrevo = false;
                while(!toCheck.isEmpty()) {
                    Pokemon checking = toCheck.remove();
                    if(checked.contains(checking)) {
                        continue; //continue inner loop only
                    }
                    if(this.hasPreEvolutions(checking, useOriginal)) {
                        hasPrevo = true;
                        break; //break inner loop only
                    }
                    checked.add(checking);

                    toCheck.addAll(pokemon.getPreEvolvedPokemon(useOriginal));
                }

                if(!hasPrevo) {
                    firstInLines.add(pokemon);
                }
            }
        }
        return firstInLines;
    }

    /**
     * Returns all Pokemon in this set that no other Pokemon (in this set or otherwise) evolves into.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all basic Pokemon in this set.
     */
    public PokemonSet filterBasic(boolean useOriginal) {
        return this.filter(p -> p.getPreEvolvedPokemon(useOriginal).isEmpty());
    }

    /**
     * Returns all Pokemon in this set that evolve into no other Pokemon (in this set or otherwise).
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all final-evo Pokemon in this set.
     */
    public PokemonSet filterFinalEvos(boolean useOriginal) {
        return this.filter(p -> p.getEvolvedPokemon(useOriginal).isEmpty());
    }

    /**
     * Returns all Pokemon in this set that are related to no other Pokemon (in this set or otherwise).
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all non-evolving Pokemon in this set.
     */
    public PokemonSet filterStandalone(boolean useOriginal) {
        return this.filter(p -> p.getPreEvolvedPokemon(useOriginal).isEmpty() &&
                p.getEvolvedPokemon(useOriginal).isEmpty());
    }

    /**
     * Returns all Pokemon in this set that both evolve from and to at least one other Pokemon.
     * The related Pokemon are not required to be in the set.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all middle-evo Pokemon in this set.
     */
    public PokemonSet filterMiddleEvos(boolean useOriginal) {
        return this.filter(p -> !p.getPreEvolvedPokemon(useOriginal).isEmpty() &&
                !p.getEvolvedPokemon(useOriginal).isEmpty());
    }

    /**
     * Returns all Pokemon in this set that evolve from a Pokemon which can evolve into two or more different Pokemon.
     * The related Pokemon are not required to be in the set.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all split-evo Pokemon in this set.
     */
    public PokemonSet filterSplitEvolutions(boolean useOriginal) {
        // TODO: there was a notion in earlier code, of treating Ninjask only as a non-split evo
        //  (or technically Pokemon which evolved through EvolutionType.LEVEL_CREATE_EXTRA).
        //  Is this something we want to recreate?
        return filter(p -> {
            for (Pokemon pre : p.getPreEvolvedPokemon(useOriginal)) {
                if (pre.getEvolvedPokemon(useOriginal).size() > 1) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Returns all Pokemon for which evolutionary lines of at least the given length are contained
     * within this set.
     * In the case of branching evolutions, only branches of the correct length will be included.
     * @param length the number of Pokemon required in the evolutionary line. At least 1. Counts itself.
     * @param allowGaps Whether to allow lines with one or more of the middle Pokemon missing.
     *                  For example, if allowGaps is true, and the set contains Weedle and Beedrill but not Kakuna,
     *                  they will be included in the returned set if length is 2 or 3.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a PokemonSet containing all valid Pokemon.
     */
    public PokemonSet filterEvoLinesAtLeastLength(int length, boolean allowGaps, boolean useOriginal) {
        if(length < 1) {
            throw new IllegalArgumentException("Invalid evolutionary line length.");
        }
        if(length == 1) {
            return new PokemonSet(this);
        }

        PokemonSet validEvoLines = new PokemonSet();
        PokemonSet firstInLines = this.filterFirstEvolutionAvailable(!allowGaps, useOriginal);

        Map<Pokemon, PokemonSet> currentStage = new HashMap<>();
        for(Pokemon pokemon : firstInLines) {
            currentStage.put(pokemon, new PokemonSet());
        }

        for(int i = 2; !currentStage.isEmpty(); i++) {
            Map<Pokemon, PokemonSet> nextStage = new HashMap<>();
            for(Pokemon poke : currentStage.keySet()) {
                if(!allowGaps && !this.contains(poke)) {
                    //kill this line
                    continue;
                }

                PokemonSet lineSoFar = new PokemonSet(currentStage.get(poke));
                if(this.contains(poke)){
                    lineSoFar.add(poke);

                    if(i >= length) {
                        validEvoLines.addAll(lineSoFar);
                    }
                }

                for(Pokemon evo : poke.getEvolvedPokemon(useOriginal)) {
                    nextStage.put(evo, lineSoFar);
                }
            }

            currentStage = nextStage;
        }

        return validEvoLines;
    }

    /**
     * Gets all Pokemon in the set which have at least the specified number of evolution stages
     * before and after them. <br>
     * If the Pokemon is in a cycle, guarantees that the total length of the cycle is greater than
     * before + 1 + after.
     * @param before The number of stages before this Pokemon. 0+.
     * @param after The number of stages after this Pokemon. 0+.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return A PokemonSet containing all Pokemon in the specified position.
     */
    public PokemonSet filterHasEvoStages(int before, int after, boolean useOriginal) {
        if (before < 0 || after < 0) {
            throw new IllegalArgumentException("Cannot have a negative number of evolutions!");
        }
        if (before == 0 && after == 0) {
            //impossible not to have these
            return new PokemonSet(this);
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
     * the given Pokemon.
     * For example, a Pokemon which has both a basic and a once-evolved Pokemon that can evolve into
     * it would return 2 (for the once-evolved Pokemon).
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial Pokemon.
     * @param pokemon The Pokemon to find the evolutionary count for.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The number of Pokemon in the longest line before this Pokemon.
     */
    public int getNumberEvoStagesBefore(Pokemon pokemon, boolean useOriginal) {
        int numStages = 0;
        PokemonSet currentStage = new PokemonSet();
        currentStage.add(pokemon);

        PokemonSet checked = new PokemonSet();

        while(!currentStage.isEmpty()) {
            PokemonSet previousStage = new PokemonSet();
            for(Pokemon poke : currentStage) {
                if(checked.contains(poke)) {
                    continue;
                }
                previousStage.addAll(this.filterPreEvolutions(poke, useOriginal));
                checked.add(poke);
            }
            if(!previousStage.isEmpty()) {
                numStages++;
            }
            currentStage = previousStage;
        }

        return numStages;
    }

    /**
     * Finds the largest number of evolutionary steps contained in this set that the given Pokemon
     * can evolve into.<br>
     * For example, a Pokemon which evolves into two Pokemon, one of which can evolve again,
     * would return 2 (for the Pokemon which can evolve again.)<br>
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial Pokemon.
     * @param pokemon The Pokemon to find the evolutionary count for.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The number of Pokemon in the longest line after this Pokemon.
     */
    public int getNumberEvoStagesAfter(Pokemon pokemon, boolean useOriginal) {
        int numStages = 0;
        PokemonSet currentStage = new PokemonSet();
        currentStage.add(pokemon);
        PokemonSet checked = new PokemonSet();

        while(!currentStage.isEmpty()) {
            PokemonSet nextStage = new PokemonSet();
            for(Pokemon poke : currentStage) {
                if(checked.contains(poke)) {
                    continue;
                }
                nextStage.addAll(this.filterEvolutions(poke, useOriginal));
                checked.add(poke);
            }
            if(!nextStage.isEmpty()) {
                numStages++;
            }
            currentStage = nextStage;
        }

        return numStages;
    }

    /**
     * Finds the longest evolutionary line in this set that the given Pokemon belongs to. <br>
     * If an evolutionary cycle is found, will count each evolution once,
     * including the one back to the initial Pokemon.
     * @param pokemon The Pokemon to find an evolutionary line for.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return The number of Pokemon in the longest evolutionary line, including itself.
     */
    public int getLongestEvoLine(Pokemon pokemon, boolean useOriginal) {
        if(isInEvoCycle(pokemon, useOriginal)) {
            return getNumberEvoStagesAfter(pokemon, useOriginal);
        } else {
            return getNumberEvoStagesBefore(pokemon, useOriginal) + 1
                    + getNumberEvoStagesAfter(pokemon, useOriginal);
        }
    }

    /**
     * Checks whether the given Pokemon is in an evolutionary cycle for which all Pokemon
     * are contained within this set.
     * @param pokemon The Pokemon to check for cycles.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return Whether the Pokemon is in a cycle contained in this set.
     */
    public boolean isInEvoCycle(Pokemon pokemon, boolean useOriginal) {
        PokemonSet currentStage = new PokemonSet();
        currentStage.addAll(pokemon.getEvolvedPokemon(useOriginal));
        PokemonSet checked = new PokemonSet();

        while(!currentStage.isEmpty()) {
            PokemonSet nextStage = new PokemonSet();
            for(Pokemon poke : currentStage) {
                if(poke == pokemon) {
                    return true;
                }
                if(checked.contains(poke)) {
                    continue;
                }
                nextStage.addAll(this.filterEvolutions(poke, useOriginal));
                checked.add(poke);
            }
            currentStage = nextStage;
        }

        return false;
    }

    //Evolution subset: Family methods

    /**
     * Adds the given Pokemon and every evolutionary relative to this set, if they are
     * not already contained in the set.
     *
     * @param pokemon     The Pokemon to add the family of.
     * @param useOriginal Whether to use the pre-randomization evolution data.
     * @return True if any Pokemon were added to the set, false otherwise.
     */
    public boolean addFamily(Pokemon pokemon, boolean useOriginal) {
        return addAll(pokemon.getFamily(useOriginal));
    }

    /**
     * Removes the given Pokemon and every evolutionary relative from this set, if they
     * are contained in the set.
     *
     * @param pokemon The Pokemon to remove the family of.
     * @return If any Pokemon were removed from the set.
     */
    public boolean removeFamily(Pokemon pokemon, boolean useOriginal) {
        return removeAll(pokemon.getFamily(useOriginal));
    }

    /**
     * Returns all members of the given Pokemon's evolutionary family that this set contains.
     * Returns an empty set if no members are in this set.
     *
     * @param pokemon     The Pokemon to get the family of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a PokemonSet containing every member of the given Pokemon's family
     */
    public PokemonSet filterFamily(Pokemon pokemon, boolean useOriginal) {
        PokemonSet family = pokemon.getFamily(useOriginal);
        family.retainAll(this);
        return family;
    }

    /**
     * Creates a new set containing the full evolutionary families of all Pokemon in this set.
     *
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a PokemonSet containing all Pokemon in this set and all their evolutionary relatives.
     */
    public PokemonSet buildFullFamilies(boolean useOriginal) {
        //Could just use buildSetMulti, but this is more efficient
        //(And it's already written)
        PokemonSet allRelatedPokemon = new PokemonSet();
        for (Pokemon pokemon : this) {
            if (!allRelatedPokemon.contains(pokemon)) {
                allRelatedPokemon.addFamily(pokemon, useOriginal);
            }
        }
        return allRelatedPokemon;
    }

    /**
     * Adds to this set the evolutionary families of every Pokemon in this set.
     *
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return True if any Pokemon were added, false otherwise.
     */
    public boolean addFullFamilies(boolean useOriginal) {
        PokemonSet allWithFamilies = this.buildFullFamilies(useOriginal);
        return this.addAll(allWithFamilies);
    }

    /**
     * Adds to this set all Pokemon in the given set and their full evolutionary families.
     *
     * @param source      The set to add Pokemon and families from.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return True if any Pokemon were added, false otherwise.
     */
    public boolean addAllFamilies(PokemonSet source, boolean useOriginal) {
        boolean anyChanged = false;
        for (Pokemon poke : source) {
            boolean changed = this.addFamily(poke, useOriginal);
            if (changed) {
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    /**
     * Removes from this set all Pokemon that are neither contained within the given set nor
     * an evolutionary relative of a Pokemon that is.
     *
     * @param source      The set to keep Pokemon and families from.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return True if any Pokemon were removes, false otherwise.
     */
    public boolean retainAllFamilies(PokemonSet source, boolean useOriginal) {
        PokemonSet families = source.buildFullFamilies(useOriginal);

        return this.retainAll(families);
    }

    /**
     * Returns all members of the given Pokemon's evolutionary family
     * that are contained uninterrupted within this set.
     * Returns an empty set if the given Pokemon is not in this set.
     *
     * @param pokemon     The Pokemon to get the family of.
     * @param useOriginal Whether to use the evolution data from before randomization.
     * @return a PokemonSet containing every Pokemon related to the given Pokemon by members of this set.
     */
    public PokemonSet filterContiguousFamily(Pokemon pokemon, boolean useOriginal) {
        PokemonSet family = new PokemonSet();
        if (!this.contains(pokemon)) {
            return family;
        }

        Queue<Pokemon> toCheck = new ArrayDeque<>();
        toCheck.add(pokemon);
        while (!toCheck.isEmpty()) {
            Pokemon checking = toCheck.remove();
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
     * Chooses a random Pokemon from the set.
     * @param random A seeded random number generator.
     * @return A random Pokemon from the set.
     * @throws IllegalStateException if the set is empty.
     */
    public Pokemon getRandomPokemon(Random random) {
        return this.getRandomPokemon(random, false);
    }

    /**
     * Chooses a random Pokemon from the set.
     * @param random A seeded random number generator.
     * @param removePicked Whether to remove the Pokemon chosen.
     * @return A random Pokemon from the set.
     * @throws IllegalStateException if the set is empty.
     */
    public Pokemon getRandomPokemon(Random random, boolean removePicked) {
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
            Pokemon poke = randomCache.get(choice);
            if(!this.contains(poke)) {
                continue;
            }

            if(removePicked) {
                this.remove(poke);
            }
            return poke;
        }

    }

    /**
     * Gets a random Pokemon from the set with BST "similar" to the given value.
     * @param bst The BST to find Pokemon near. Cannot be negative.
     * @param random A seeded random number generator.
     * @return A random Pokemon from the set with "similar" BST.
     */
    public Pokemon getRandomSimilarStrengthPokemon(int bst, Random random) {
        if(bst < 0) {
            throw new IllegalArgumentException("Cannot find similar to a negative BST!");
        }
        return getRandomSimilarStrengthPokemon(null, false, bst, random);
    }

    /**
     * Gets a random Pokemon from the set with BST "similar" to the given Pokemon's.
     * @param match The Pokemon to find a "similar" BST to.
     * @param random A seeded random number generator.
     * @return A random Pokemon from the set with "similar" BST.
     */
    public Pokemon getRandomSimilarStrengthPokemon(Pokemon match, Random random) {
        return getRandomSimilarStrengthPokemon(match, false, -1, random);
    }

    /**
     * Gets a random Pokemon from the set with BST "similar" to the given Pokemon's.
     * @param match The Pokemon to find a "similar" BST to.
     * @param notSamePokemon If true, will exclude the given Pokemon from the possible returns.
     *                       Ignored if the given Pokemon is the only Pokemon in the set.
     * @param random A seeded random number generator.
     * @return A random Pokemon from the set with "similar" BST.
     */
    public Pokemon getRandomSimilarStrengthPokemon(Pokemon match, boolean notSamePokemon, Random random) {
        return getRandomSimilarStrengthPokemon(match, notSamePokemon, -1, random);
    }

    /**
     * Gets a random Pokemon from the set with BST "similar" to the given Pokemon's, or to the
     * given integer if it is not negative.
     * @param match The Pokemon to find a "similar" BST to.
     * @param notSamePokemon If true, will exclude the given Pokemon from the possible returns.
     *                       Ignored if the given Pokemon is the only Pokemon in the set.
     * @param overrideBST If not negative, use this value instead of the given Pokemon's actual BST.
     *                    Ignored if negative.
     * @param random A seeded random number generator.
     * @return A random Pokemon from the set with "similar" BST.
     */
    public Pokemon getRandomSimilarStrengthPokemon(Pokemon match, boolean notSamePokemon,
                                                   int overrideBST, Random random) {
        PokemonSet availablePool = new PokemonSet(this); //clone for draining
        if(notSamePokemon) {
            availablePool.remove(match);
        }

        if(availablePool.isEmpty()) {
            if(this.isEmpty()) {
                throw new RandomizationException("Attempted to choose a wild Pokemon from an empty set!");
            } else {
                //if availablePool is empty, but this set isn't, match must be the only Pokemon in the set.
                return match;
            }
        }

        int minimumPool = Math.min(SS_MINIMUM_POOL, availablePool.size() / SS_MINIMUM_POOL_FACTOR);
        if(minimumPool < 1) {
            minimumPool = 1;
        }
        if (minimumPool >= availablePool.size()) {
            //must use the whole pool
            //(I think this only happens if there's exactly one Pokemon to choose.)
            return availablePool.getRandomPokemon(random);
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
        PokemonSet canPick = new PokemonSet();
        while (canPick.size() < minimumPool) {
            Iterator<Pokemon> itor = availablePool.iterator();
            while (itor.hasNext()) {
                Pokemon poke = itor.next();
                if(poke.getBSTForPowerLevels() >= minTarget && poke.getBSTForPowerLevels() <= maxTarget) {
                    canPick.add(poke);
                    itor.remove();
                }
            }
            minTarget -= matchBST / 20;
            maxTarget += matchBST / 20;
        }
        return canPick.getRandomPokemon(random);
    }

    //end randoms

    //Various Functions

    /**
     * Returns all Pokemon in the set which are cosmetic formes.
     * @return A PokemonSet containing all Pokemon in this set which are cosmetic.
     */
    public PokemonSet filterCosmetic() {
        return filter(Pokemon::isActuallyCosmetic);
    }

    /**
     * Returns all Pokemon in the set which are not cosmetic formes.
     * @return A PokemonSet containing all Pokemon in this set which are not cosmetic.
     */
    public PokemonSet filterNonCosmetic() {
        return filter(p -> !p.isActuallyCosmetic());
    }

    public String toStringShort() {
        StringBuilder string = new StringBuilder("[");
        Iterator<Pokemon> itor = this.iterator();
        while(itor.hasNext()) {
            Pokemon pokemon = itor.next();
            string.append(pokemon.getName()).append(pokemon.isBaseForme() ? "" : pokemon.getFormeSuffix());
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
    
    public static PokemonSet unmodifiable(Collection<Pokemon> source) {
        return new PokemonSet.UnmodifiablePokemonSet(source);
    }

    /**
     * Just what it sounds like, a {@link PokemonSet} which throws {@link UnmodifiableSetException}
     * whenever modifications are attempted.
     */
    private static class UnmodifiablePokemonSet extends PokemonSet {
        private final boolean unmodifiable;

        public UnmodifiablePokemonSet(Collection<? extends Pokemon> original) {
            super(original);
            unmodifiable = true; // since you can't use the super constructor if add() is always impossible
        }

        @Override
        public boolean add(Pokemon pk) {
            if (unmodifiable) {
                throw new UnmodifiableSetException();
            } else {
                return super.add(pk);
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
        public Iterator<Pokemon> iterator() {
            return new Iterator<Pokemon>() {
                private final Iterator<Pokemon> inner = UnmodifiablePokemonSet.super.iterator();

                @Override
                public boolean hasNext() {
                    return inner.hasNext();
                }

                @Override
                public Pokemon next() {
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