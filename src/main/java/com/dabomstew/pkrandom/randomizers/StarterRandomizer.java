package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class StarterRandomizer extends Randomizer {

    public StarterRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeStarters() {
        boolean useCustomStarters = settings.getStartersMod() == Settings.StartersMod.CUSTOM;
        boolean typeFwg = settings.getStartersTypeMod() == Settings.StartersTypeMod.FIRE_WATER_GRASS;
        boolean typeUnique = settings.getStartersTypeMod() == Settings.StartersTypeMod.UNIQUE;
        boolean typeTriangle = settings.getStartersTypeMod() == Settings.StartersTypeMod.TRIANGLE;
        boolean typeSingle = settings.getStartersTypeMod() == Settings.StartersTypeMod.SINGLE_TYPE;
        boolean hasTypeRestriction = typeFwg || typeUnique || typeTriangle || typeSingle;
        Type singleType = settings.getStartersSingleType();
        int[] customStarters = settings.getCustomStarters();
        int starterCount = romHandler.starterCount();

        List<Species> pickedStarters = new ArrayList<>();

        if (useCustomStarters) {
            pickedStarters.addAll(getCustomStarters(customStarters));

            if (pickedStarters.size() == starterCount) {
                //The customs were all we needed
                romHandler.setStarters(pickedStarters);
                return;
            } else if (pickedStarters.size() > starterCount) {
                //what.
                throw new IllegalStateException("Custom starter list exceeded starter count?!");
            }
        }

        SpeciesSet choosable = getAvailableSet(pickedStarters);

        //sanity check
        if (choosable.size() < starterCount - pickedStarters.size()) {
            throw new RandomizationException("Not enough valid starters");
        }

        //set complete. start picking.
        if (!hasTypeRestriction) {
            pickedStarters.addAll(chooseStartersBasic(starterCount - pickedStarters.size(), choosable));
        } else if (typeUnique) {
            //we don't actually need a type map for this one

            //run on a trio at a time so that we don't run out of types in games with many starters
            while (pickedStarters.size() < starterCount) {
                int startOfTrio = (pickedStarters.size() / 3) * 3; //round down to the nearest 3
                int endOfTrio = Math.min(startOfTrio + 3, starterCount);
                int sizeOfTrio = endOfTrio - startOfTrio;
                int pickedInTrio = pickedStarters.size() - startOfTrio; //modulus 3 would also work
                SpeciesSet pokemonInTrio = new SpeciesSet();
                for(int i = 0; i < pickedInTrio; i++) {
                    pokemonInTrio.add(pickedStarters.get(startOfTrio + i));
                }

                pickedStarters.addAll(chooseUniqueTypeStarters(pokemonInTrio, sizeOfTrio, choosable));
                choosable.removeAll(pickedStarters);
            }
        } else {

            //build type map
            Map<Type, SpeciesSet> choosableByType = choosable.sortByType(false);

            //assuming only one type restriction at a time (not counting noDualTypes)
            //also assuming that the triangle restrictions (typeTriangle, fireWaterGrass)
            //are not used with custom starters
            if (typeTriangle) {
                pickedStarters = chooseTypeTriangleStarters(choosableByType, true);
                while (pickedStarters.size() < starterCount) {
                    //remove the ones we already picked
                    choosable.removeAll(pickedStarters);
                    choosableByType = choosable.sortByType(false);

                    //and do it again
                    pickedStarters.addAll(chooseTypeTriangleStarters(choosableByType, false));
                }
            } else if (typeFwg) {
                pickedStarters = chooseStartersFireWaterGrass(choosableByType);
                while (pickedStarters.size() < starterCount) {
                    //remove the ones we already picked
                    choosable.removeAll(pickedStarters);
                    choosableByType = choosable.sortByType(false);

                    //and do it again
                    pickedStarters.addAll(chooseStartersFireWaterGrass(choosableByType));
                }
            } else if (typeSingle) {
                if(singleType == null) {
                    singleType = chooseTypeForStarters(starterCount - pickedStarters.size(), choosableByType);
                }
                pickedStarters.addAll(chooseStartersBasic(starterCount - pickedStarters.size(),
                                choosableByType.get(singleType)));
            } //no other case
        }

        romHandler.setStarters(pickedStarters);
        changesMade = true;
    }

    /**
     * Chooses a random type that has at least the given number of Pokemon.
     * @param numStartersNeeded The number of Pokemon that must be in the type.
     * @param availableByType The set of Pokemon available.
     * @return A Type which has the number of Pokemon needed.
     * @throws RandomizationException if no type has the needed number of Pokemon.
     */
    private Type chooseTypeForStarters(int numStartersNeeded, Map<Type, SpeciesSet> availableByType) {
        List<Type> types = new ArrayList<>(typeService.getTypes());
        Collections.shuffle(types, random);

        for (Type type : types) {
            if(availableByType.get(type).size() > numStartersNeeded) {
                return type;
            }
        }

        throw new RandomizationException("No type has " + numStartersNeeded + " starters available!");
    }

    /**
     * Chooses a trio of starters, one each of Fire, Water, and Grass type,
     * in the appropriate order for the current game.
     * @param choosableByType The set of Pokemon to choose from, sorted by type.
     * @return A new List containing a trio of starters of Fire, Water, and Grass types in the appropriate order.
     */
    private List<Species> chooseStartersFireWaterGrass(Map<Type, SpeciesSet> choosableByType) {
        return chooseStartersOfTypes(choosableByType, romHandler.getStandardTypeTriangle());
    }

    /**
     * Finds all type triangles possible with the current type advantages,
     * then chooses one at random to select a trio of starters with that type triangle.
     * @param availablePokemonByType The set of Pokemon to choose from, sorted by type.
     * @return A new List containing a trio of Pokemon such that each is super-effective
     * against the previous (wrapping around).
     */
    private List<Species> chooseTypeTriangleStarters(Map<Type, SpeciesSet> availablePokemonByType, boolean firstTrio) {
        Set<List<Type>> typeTriangles = findTypeTriangles();
        if (typeTriangles.isEmpty()) {
            throw new RandomizationException("Could not find any type triangles");
        }
        // to pick randomly from
        List<List<Type>> typeTriangleList = new ArrayList<>(typeTriangles);
        Collections.shuffle(typeTriangleList, random);

        List<Species> picks = null;

        // okay, we found our triangles! now pick one and pick starters from it.
        // loop because we might find that there isn't a pokemon set of the appropriate types
        while (picks == null && !typeTriangleList.isEmpty()) {
            List<Type> triangle = typeTriangleList.get(random.nextInt(typeTriangleList.size()));
            try {
                picks = chooseStartersOfTypes(availablePokemonByType, triangle);
            } catch (RandomizationException e) {
                //If it failed, it's because this triangle isn't valid.
                typeTriangles.remove(triangle);
                picks = null; //this is theoretically unnecessary, but here for clarity
            }

            //we've succeeded. If this is the first trio of starters, set starter type triangle.
            if(firstTrio) {
                romHandler.setStarterTypeTriangle(triangle);
            }
        }

        if (picks == null) {
            throw new RandomizationException("No valid starter set with a type triangle could be found!");
        }
        return picks;
    }

    /**
     * Given a list of types, chooses one Pokemon of each type. No Pokemon will have more than
     * one of the given types.
     * @param availablePokemonByType The set of Pokemon to choose from, sorted by type.
     * @param types The list of types to choose.
     * @return A new List of Pokemon, each of one of the given types, in the same order given.
     * @throws RandomizationException If one of the types has no valid Pokemon (i.e., Pokemon which do not
     *              have any of the other types.)
     */
    private List<Species> chooseStartersOfTypes(Map<Type, SpeciesSet> availablePokemonByType, List<Type> types) {
        List<Species> chosenStarters = new ArrayList<>();

        for (Type type : types) {
            SpeciesSet pokemonOfType;
            if(availablePokemonByType.get(type) != null) {
                pokemonOfType = new SpeciesSet(availablePokemonByType.get(type));
                //clone so we can safely drain it
            } else {
                throw new RandomizationException("No valid starters of type " + type + "found!");
            }

            boolean noPick = true;
            while (noPick && !pokemonOfType.isEmpty()) {
                Species picked = pokemonOfType.getRandomSpecies(random, true);
                Type otherType;
                if (picked.getPrimaryType(false) == type) {
                    otherType = picked.getSecondaryType(false);
                } else {
                    otherType = picked.getPrimaryType(false);
                }
                if (!types.contains(otherType)) {
                    //this pokemon works
                    noPick = false;
                    chosenStarters.add(picked);
                }
            }
            if (noPick) {
                throw new RandomizationException("No valid starter of type " + type + " found!");
            }
        }

        return chosenStarters;
    }

    /**
     * Given a set of available Species, chooses a set of unique starters with no additional constraints.
     * @param numberPicks The number of Species to choose.
     * @param available The set of Species to choose from.
     * @return A new {@link List} containing each starter chosen. (Not a SpeciesSet so that the order remains random.)
     */
    private List<Species> chooseStartersBasic(int numberPicks, SpeciesSet available) {
        if(available.size() < numberPicks) {
            throw new RandomizationException("Not enough starters to choose from!");
        }

        List<Species> picks = new ArrayList<>();
        //List rather than set so the order isn't deterministic
        while (picks.size() < numberPicks) {
            Species picked = available.getRandomSpecies(random);
            picks.add(picked);
            available.remove(picked);
        }

        return picks;
    }

    /**
     * Given a set of available Species, chooses a set of unique starters such that none of their types are
     * shared with any Species already picked.
     * @param alreadyPicked The set of Species already picked.
     * @param numberPicks The number of Species to choose (not including those already picked).
     * @param available The set of Species available to choose from.
     * @return A new {@link List} containing each starter chosen. (Not a SpeciesSet so that the order remains random.)
     */
    private List<Species> chooseUniqueTypeStarters(SpeciesSet alreadyPicked, int numberPicks, SpeciesSet available) {
        available = new SpeciesSet(available); //so as to not modify by reference
        for (Species picked : alreadyPicked) {
            available.removeIf(poke -> poke.hasType(picked.getPrimaryType(false), false)
                    || poke.hasType(picked.getSecondaryType(false), false));
        }

        List<Species> picks = new ArrayList<>();

        while (picks.size() < numberPicks) {
            Species picked = available.getRandomSpecies(random);
            picks.add(picked);
            available.remove(picked);
            available.removeIf(poke -> poke.hasType(picked.getPrimaryType(false), false)
                    || poke.hasType(picked.getSecondaryType(false), false));
        }

        Collections.shuffle(picks, random);
        //Because without this, types with more Pokemon are likely to appear sooner in the list.

        return picks;
    }

    /**
     * Finds all Species that could be used as starters given the current settings.
     * @param alreadyChosen The list of already-chosen (i.e. custom) starters.
     * @return A new SpeciesSet containing all Species which are valid starters, except the already chosen ones.
     */
    private SpeciesSet getAvailableSet(List<Species> alreadyChosen) {
        boolean abilitiesUnchanged = settings.getAbilitiesMod() == Settings.AbilitiesMod.UNCHANGED;
        boolean allowAltFormes = settings.isAllowStarterAltFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean noLegendaries = settings.isStartersNoLegendaries();
        boolean noDualTypes = settings.isStartersNoDualTypes();
        boolean triStageOnly = settings.getStartersMod() == Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS;
        boolean basicOnly = triStageOnly || settings.getStartersMod() == Settings.StartersMod.RANDOM_BASIC;
        int bstMin = settings.getStartersBSTMinimum();
        int bstMax = settings.getStartersBSTMaximum() == 0 ? 1530 : settings.getStartersBSTMaximum();

        SpeciesSet available;

        if (allowAltFormes) {
            available = new SpeciesSet(noLegendaries ? rSpecService.getNonLegendaries(true)
                    : rSpecService.getAll(true));
            if (abilitiesUnchanged) {
                available.removeAll(rSpecService.getAbilityDependentFormes());
            }
            if (banIrregularAltFormes) {
                available.removeAll(romHandler.getIrregularFormes());
            }
            available.removeIf(Species::isCosmeticReplacement);
            available.removeIf(Species::isActuallyCosmetic);
        } else {
            available = new SpeciesSet(noLegendaries ? rSpecService.getNonLegendaries(false) : rSpecService.getAll(false));
        }

        available.removeAll(alreadyChosen);

        if (noDualTypes) {
            available.removeIf(p -> p.hasSecondaryType(false));
        }
        if (basicOnly) {
            available = available.filterBasic(false);
        }
        if (triStageOnly) {
            available.removeIf(p -> p.getStagesAfter(false) < 2);
        }
        if(bstMin != 0 || bstMax != 1530) {
            available.removeIf(p -> p.getBSTForPowerLevels() < bstMin || p.getBSTForPowerLevels() > bstMax);
        }

        //all constraints except type done!
        return available;
    }

    /**
     * Given a list of indices, finds the corresponding Pokemon.
     * @param starterIndices A list of indices for the Pokemon in the rom's full list.
     * @return A new List of Pokemon in the same order, with any 0s skipped.
     */
    //TODO: enhance the ordering (i.e. if the first and third are given, make sure they stay in those positions)
    private List<Species> getCustomStarters(int[] starterIndices) {
        List<Species> customStarters = new ArrayList<>();
        List<Species> romSpecies = romHandler.getSpeciesInclFormes()
                .stream()
                .filter(pk -> pk == null || !pk.isCosmeticReplacement())
                .collect(Collectors.toList());

        for (int customStarter : starterIndices) {
            if (!(customStarter == 0)) {
                Species starter = romSpecies.get(customStarter);
                customStarters.add(starter);
            }
        }

        return customStarters;
    }

    /**
     * Finds all potential type triangles given the current type advantages.
     * Each set of types is listed three times, once for each starting type.
     * @return A set of all distinct triangles.
     */
    private Set<List<Type>> findTypeTriangles() {
        TypeTable typeTable = romHandler.getTypeTable();
        Set<List<Type>> typeTriangles;
        typeTriangles = new HashSet<>();
        for (Type typeOne : typeTable.getTypes()) {
            List<Type> superEffectiveOne = typeTable.superEffectiveWhenAttacking(typeOne);
            superEffectiveOne.remove(typeOne);
            //don't want a Ghost-Ghost-Ghost "triangle"
            //(although it would be funny)
            for (Type typeTwo : superEffectiveOne) {
                List<Type> superEffectiveTwo = typeTable.superEffectiveWhenAttacking(typeTwo);
                superEffectiveTwo.remove(typeOne);
                superEffectiveTwo.remove(typeTwo);
                for (Type typeThree : superEffectiveTwo) {
                    List<Type> superEffectiveThree = typeTable.superEffectiveWhenAttacking(typeThree);
                    if (superEffectiveThree.contains(typeOne)) {
                        // The below is an ArrayList because the immutable list created by List.of throws a
                        // NullPointerException when you check whether it contains null.

                        // It is "reverse" direction because it's used for starter generation,
                        // and the starter list expects type triangles to be this way
                        // (it's [Fire, Water, Grass] in vanilla)
                        List<Type> triangle = Arrays.asList(typeThree, typeTwo, typeOne);
                        typeTriangles.add(triangle);
                    }
                }
            }
        }
        return typeTriangles;
    }

    public void randomizeStarterHeldItems() {
        boolean banBadItems = settings.isBanBadRandomStarterHeldItems();

        List<Item> oldHeldItems = romHandler.getStarterHeldItems();
        List<Item> newHeldItems = new ArrayList<>();
        List<Item> possibleItems = new ArrayList<>(banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems());
        for (int i = 0; i < oldHeldItems.size(); i++) {
            newHeldItems.add(possibleItems.get(random.nextInt(possibleItems.size())));
        }
        romHandler.setStarterHeldItems(newHeldItems);
        changesMade = true;
    }
}
