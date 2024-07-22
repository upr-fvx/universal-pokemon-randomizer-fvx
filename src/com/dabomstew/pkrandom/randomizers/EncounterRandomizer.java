package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkrandom.pokemon.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;

public class EncounterRandomizer extends Randomizer {

    public EncounterRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeEncounters() {
        boolean useTimeOfDay = settings.isUseTimeBasedEncounters();
        int levelModifier = settings.isWildLevelsModified() ? settings.getWildLevelModifier() : 0;

        if(!settings.isRandomizeWildPokemon()) {
            modifyLevelsOnly(useTimeOfDay, levelModifier);
            return;
        }

        Settings.WildPokemonRegionMod mode = settings.getWildPokemonRegionMod();
        boolean splitByEncounterType = settings.isSplitWildRegionByEncounterTypes();
        boolean randomTypeThemes = settings.getWildPokemonTypeMod() == Settings.WildPokemonTypeMod.THEMED_AREAS;
        boolean keepTypeThemes = settings.isKeepWildTypeThemes();
        boolean keepPrimaryType = settings.getWildPokemonTypeMod() == Settings.WildPokemonTypeMod.KEEP_PRIMARY;
        boolean keepEvolutions = settings.isKeepWildEvolutionFamilies();
        boolean catchEmAll = settings.isCatchEmAllEncounters();
        boolean similarStrength = settings.isSimilarStrengthEncounters();
        boolean noLegendaries = settings.isBlockWildLegendaries();
        boolean balanceShakingGrass = settings.isBalanceShakingGrass();
        boolean allowAltFormes = settings.isAllowWildAltFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;

        randomizeEncounters(mode, splitByEncounterType, useTimeOfDay,
                randomTypeThemes, keepTypeThemes, keepPrimaryType, keepEvolutions, catchEmAll, similarStrength, noLegendaries,
                balanceShakingGrass, levelModifier, allowAltFormes, banIrregularAltFormes, abilitiesAreRandomized);
        changesMade = true;
    }

    // only exists for some old test cases, please don't use
    public void randomizeEncounters(Settings.WildPokemonRegionMod mode, Settings.WildPokemonTypeMod typeMode,
                                    boolean useTimeOfDay,
                                    boolean catchEmAll, boolean similarStrength,
                                    boolean noLegendaries, boolean balanceShakingGrass, int levelModifier,
                                    boolean allowAltFormes, boolean banIrregularAltFormes,
                                    boolean abilitiesAreRandomized) {
        randomizeEncounters(mode,
                false,
                useTimeOfDay,
                typeMode == Settings.WildPokemonTypeMod.THEMED_AREAS,
                false,
                typeMode == Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                false,
                catchEmAll, similarStrength,
                noLegendaries, balanceShakingGrass, levelModifier,
                allowAltFormes, banIrregularAltFormes,
                abilitiesAreRandomized);
    }

    // only public for some old test cases, please don't use
    public void randomizeEncounters(Settings.WildPokemonRegionMod mode, boolean splitByEncounterType,
                                    boolean useTimeOfDay,
                                    boolean randomTypeThemes, boolean keepTypeThemes, boolean keepPrimaryType,
                                    boolean keepEvolutions, boolean catchEmAll, boolean similarStrength,
                                    boolean noLegendaries, boolean balanceShakingGrass, int levelModifier,
                                    boolean allowAltFormes, boolean banIrregularAltFormes,
                                    boolean abilitiesAreRandomized) {
        // - prep settings
        // - get encounters
        // - setup banned + allowed
        // - randomize inner
        // - apply level modifier
        // - set encounters

        rPokeService.setRestrictions(settings);

        List<EncounterArea> encounterAreas = romHandler.getEncounters(useTimeOfDay);
        List<EncounterArea> preppedAreas = prepEncounterAreas(encounterAreas);

        PokemonSet banned = getBannedForWildEncounters(banIrregularAltFormes, abilitiesAreRandomized);
        PokemonSet allowed = new PokemonSet(rPokeService.getPokemon(noLegendaries, allowAltFormes, false));
        allowed.removeAll(banned);

        InnerRandomizer ir = new InnerRandomizer(allowed, banned,
                randomTypeThemes, keepTypeThemes, keepPrimaryType, catchEmAll, similarStrength, balanceShakingGrass,
                keepEvolutions);
        switch (mode) {
            case NONE:
                if(romHandler.isORAS()) {
                    //this mode crashes ORAS and needs special handling to approximate
                    ir.randomEncountersORAS(preppedAreas);
                } else {
                    ir.randomEncounters(preppedAreas);
                }
                break;
            case ENCOUNTER_SET:
                ir.area1to1Encounters(preppedAreas);
                break;
            case MAP:
                ir.map1to1Encounters(preppedAreas, splitByEncounterType);
                break;
            case NAMED_LOCATION:
                ir.location1to1Encounters(preppedAreas, splitByEncounterType);
                break;
            case GAME:
                ir.game1to1Encounters(preppedAreas, splitByEncounterType);
                break;
        }

        applyLevelModifier(levelModifier, encounterAreas);
        romHandler.setEncounters(useTimeOfDay, encounterAreas);
    }

    /**
     * Changes the levels of wild Pokemon encounters without randomizing them.
     */
    private void modifyLevelsOnly(boolean useTimeOfDay, int levelModifier) {
        List<EncounterArea> encounterAreas = romHandler.getEncounters(useTimeOfDay);
        applyLevelModifier(levelModifier, encounterAreas);
        romHandler.setEncounters(useTimeOfDay, encounterAreas);
    }

    private PokemonSet getBannedForWildEncounters(boolean banIrregularAltFormes,
                                                           boolean abilitiesAreRandomized) {
        PokemonSet banned = new PokemonSet();
        banned.addAll(romHandler.getBannedForWildEncounters());
        banned.addAll(rPokeService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            PokemonSet abilityDependentFormes = rPokeService.getAbilityDependentFormes();
            banned.addAll(abilityDependentFormes);
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }
        return banned;
    }

    protected void applyLevelModifier(int levelModifier, List<EncounterArea> currentEncounterAreas) {
        if (levelModifier != 0) {
            for (EncounterArea area : currentEncounterAreas) {
                for (Encounter enc : area) {
                    enc.setLevel(Math.min(100, (int) Math.round(enc.getLevel() * (1 + levelModifier / 100.0))));
                    enc.setMaxLevel(Math.min(100, (int) Math.round(enc.getMaxLevel() * (1 + levelModifier / 100.0))));
                }
            }
        }
    }

    private class InnerRandomizer {
        private final boolean randomTypeThemes;
        private final boolean keepTypeThemes;
        private final boolean keepPrimaryType;
        private final boolean needsTypes;
        private final boolean catchEmAll;
        private final boolean similarStrength;
        private final boolean balanceShakingGrass;
        private final boolean keepEvolutions;

        private boolean useMapping;

        private Map<Type, PokemonSet> allowedByType;
        private final PokemonSet allowed;
        private final PokemonSet banned;

        private Map<Type, PokemonSet> remainingByType;
        private PokemonSet remaining;

        private Type regionType;
        //private PokemonSet allowedForArea;
        private Map<Pokemon, Pokemon> regionMap;
        //private PokemonSet allowedForReplacement;

        private Map<Pokemon, PokemonAreaInformation> areaInformationMap = null;

        //ORAS's DexNav will crash if the load is higher than this value.
        final int ORAS_CRASH_THRESHOLD = 18;

        public InnerRandomizer(PokemonSet allowed, PokemonSet banned,
                               boolean randomTypeThemes, boolean keepTypeThemes, boolean keepPrimaryType,
                               boolean catchEmAll, boolean similarStrength, boolean balanceShakingGrass,
                               boolean keepEvolutions) {
            if (randomTypeThemes && keepPrimaryType) {
                throw new IllegalArgumentException("Can't use keepPrimaryType with randomTypeThemes.");
            }
            this.randomTypeThemes = randomTypeThemes;
            this.keepTypeThemes = keepTypeThemes;
            this.keepPrimaryType = keepPrimaryType;
            this.keepEvolutions = keepEvolutions;
            this.needsTypes = keepPrimaryType || keepTypeThemes || randomTypeThemes;
            this.catchEmAll = catchEmAll;
            this.similarStrength = similarStrength;
            this.balanceShakingGrass = balanceShakingGrass;
            this.allowed = allowed;
            this.banned = banned;
            if (needsTypes) {
                this.allowedByType = allowed.sortByType(false, typeService.getTypes());
            }
            //any algorithm that uses mapping should use remaining, not just catch-em-all
            //easiest to just always use it
            refillRemainingPokemon();
        }

        private void refillRemainingPokemon() {
            remaining = new PokemonSet(allowed);
            if (needsTypes) {
                remainingByType = new EnumMap<>(Type.class);
                for (Type t : typeService.getTypes()) {
                    remainingByType.put(t, new PokemonSet(allowedByType.get(t)));
                }
            }
        }

        //This is now the one most different, algorithm-wise
        //but it has enough overlap to make sense here, anyway.
        public void randomEncounters(List<EncounterArea> encounterAreas) {
            useMapping = false;

            //ok. this is dumb, but it makes it integrate well.
            List<List<EncounterArea>> regions = new ArrayList<>();
            for(EncounterArea area : encounterAreas) {
                List<EncounterArea> region = new ArrayList<>();
                region.add(area);
                regions.add(region);
            }

            randomizeRegions(regions);
        }

        /**
         * Special case to approximate random encounters in ORAS, since they crash if the
         * normal algorithm is used.
         * @param encounterAreas The list of EncounterAreas to randomize.
         */
        private void randomEncountersORAS(List<EncounterArea> encounterAreas) {

            List<EncounterArea> collapsedEncounters = EncounterArea.flattenEncounterTypesInMaps(encounterAreas);
            List<List<EncounterArea>> maps = new ArrayList<>(
                    EncounterArea.groupAreasByMapIndex(collapsedEncounters).values());
            Collections.shuffle(maps, random);
            //Awkwardly, the grouping is run twice...

            //sort out Rock Smash areas
            List<EncounterArea> rockSmashAreas = new ArrayList<>();
            for(List<EncounterArea> map : maps) {
                Iterator<EncounterArea> mapIterator = map.iterator();
                while(mapIterator.hasNext()) {
                    EncounterArea area = mapIterator.next();
                    if(area.getEncounterType() == EncounterType.INTERACT) {
                        //rock smash is the only INTERACT type in ORAS
                        rockSmashAreas.add(area);
                        mapIterator.remove();
                    }
                }
            }

            //Rock smash is not affected by the crashing, so we can run the standard RandomEncounters on it.
            this.randomEncounters(rockSmashAreas);

            //For other areas, run it by map
            //(They're already shuffled)
            for(List<EncounterArea> map : maps) {
                randomizeMapORAS(map);
            }
        }

        public void area1to1Encounters(List<EncounterArea> encounterAreas) {
            useMapping = true;

            //ok. this is dumb, but it makes it integrate well.
            List<List<EncounterArea>> regions = new ArrayList<>();
            for(EncounterArea area : encounterAreas) {
                List<EncounterArea> region = new ArrayList<>();
                region.add(area);
                regions.add(region);
            }

            randomizeRegions(regions);
        }

        public void map1to1Encounters(List<EncounterArea> encounterAreas, boolean splitByEncounterType) {
            useMapping = true;
            Collection<List<EncounterArea>> regions = EncounterArea.groupAreasByMapIndex(encounterAreas).values();

            if(splitByEncounterType) {
                Collection<List<EncounterArea>> maps = regions;
                regions = new ArrayList<>();
                for(List<EncounterArea> map : maps) {
                    regions.addAll(EncounterArea.groupAreasByEncounterType(map).values());
                }
            }

            randomizeRegions(regions);
        }

        public void location1to1Encounters(List<EncounterArea> encounterAreas, boolean splitByEncounterType) {
            useMapping = true;
            Collection<List<EncounterArea>> regions = EncounterArea.groupAreasByLocation(encounterAreas).values();

            if(splitByEncounterType) {
                Collection<List<EncounterArea>> maps = regions;
                regions = new ArrayList<>();
                for(List<EncounterArea> map : maps) {
                    regions.addAll(EncounterArea.groupAreasByEncounterType(map).values());
                }
            }

            randomizeRegions(regions);
        }

        public void game1to1Encounters(List<EncounterArea> encounterAreas, boolean splitByEncounterType) {
            useMapping = true;

            Collection<List<EncounterArea>> regions;
            if (splitByEncounterType) {
                regions = EncounterArea.groupAreasByEncounterType(encounterAreas).values();
            } else {
                regions = new ArrayList<>();
                regions.add(encounterAreas);
            }

            randomizeRegions(regions);
        }

        /**
         * Given a Collection of regions (represented by a List of EncounterAreas),
         * randomizes each region such that type theming, 1-to-1 map, etc., are carried
         * throughout the region.
         * @param regions The regions to randomize.
         */
        private void randomizeRegions(Collection<List<EncounterArea>> regions) {

            //Shuffle regions; otherwise, large regions would tend to be randomized first.
            List<List<EncounterArea>> shuffledRegions = new ArrayList<>(regions);
            Collections.shuffle(shuffledRegions, random);

            for(List<EncounterArea> region : shuffledRegions) {
                regionType = pickRegionType(region);


                if(useMapping) {
                    regionMap = new HashMap<>();
                    setupAreaInfoMap(region);

                    if(keepEvolutions) {
                        spreadThemesThroughFamilies();
                    }
                }

                for(EncounterArea area : region) {
                    randomizeArea(area);
                }

                if(useMapping && !catchEmAll) {
                    //if not using mapping or catch em all, remaining will not empty in the first place.
                    refillRemainingPokemon();
                }
            }
        }

        private void randomizeArea(EncounterArea area) {
            //no area-level type theme, because that could foul up other type restrictions.

            boolean needsIndividualTypeRestrictions = (needsTypes && regionType == null);
            //efficiency-related: if each Pokemon has its own type restrictions, faster to
            //run area filters on remainingByType than to run type filters on AllowedForArea

            PokemonSet allowedForArea = null;
            if(!needsIndividualTypeRestrictions) {
                allowedForArea = setupAllowedForArea(regionType, area);
                if (useMapping || catchEmAll) {
                    allowedForArea = new PokemonSet(allowedForArea);
                }
            }

            for (Encounter enc : area) {
                Pokemon current = enc.getPokemon();

                if(useMapping && regionMap.containsKey(current)) {
                    //checking the map first lets us avoid creating a pointless allowedForReplacement set
                    Pokemon replacement = regionMap.get(current);
                    enc.setPokemon(replacement);
                    setFormeForEncounter(enc, replacement);
                } else {
                    Pokemon replacement;
                    if(keepEvolutions && mapHasFamilyMember(current)) {
                        replacement = pickFamilyMemberReplacement(current);
                    } else {

                        //we actually need to pick a new one
                        PokemonSet allowedForReplacement;
                        if (needsIndividualTypeRestrictions) {
                            allowedForReplacement = setupAllowedForReplacement(enc, area);
                        } else {
                            allowedForReplacement = setupAllowedForReplacement(enc, allowedForArea);
                            if (allowedForReplacement.isEmpty()) {
                                allowedForReplacement = retrySetupAllowedForAreaAndReplacement(enc, area, regionType);
                            }
                        }
                        if (allowedForReplacement.isEmpty()) {
                            throw new RandomizationException("Could not find a wild Pokemon replacement for " + enc);
                        }


                        //ok, we have a valid set. Time to actually choose a Pokemon!
                        replacement = pickReplacement(current, allowedForReplacement);
                    }

                    enc.setPokemon(replacement);
                    setFormeForEncounter(enc, replacement);

                    //add to map if applicable
                    if (useMapping) {
                        regionMap.put(current, replacement);
                    }

                    //remove from possible picks if applicable
                    if (allowedForArea != null && (useMapping || catchEmAll)) {
                        removeFromRemaining(replacement);
                        allowedForArea.remove(replacement);

                        if (allowedForArea.isEmpty()) {
                            allowedForArea = new PokemonSet(setupAllowedForArea(regionType, area));
                        }
                        //removeFromRemaining() already checks if remaining is empty, so we don't need to do that here.
                    }
                }

            }

            if (area.isForceMultipleSpecies()) {
                enforceMultipleSpecies(area);
            }
        }

        /**
         * Given a Pokemon which has at least one evolutionary relative contained within the regionMap,
         * chooses a replacement for it that is a corresponding relative of its relative's replacement.
         * @param toReplace The Pokemon to find a replacement for.
         * @return An appropriate replacement Pokemon.
         */
        private Pokemon pickFamilyMemberReplacement(Pokemon toReplace) {
            PokemonAreaInformation info = areaInformationMap.get(toReplace);
            PokemonSet family = info.getFamily();
            for(Pokemon relative : family) {
                if(regionMap.containsKey(relative)) {
                    return pickFamilyMemberReplacementInner(toReplace, relative);
                }
            }

            throw new IllegalArgumentException("Tried to pick family member replacement for non-mapped Pokemon!");
        }

        /**
         * Given a Pokemon and a relative of that Pokemon which is contained in the regionMap,
         * chooses a replacement for it that is a corresponding relative of its relative's replacement.
         * @param toReplace The Pokemon to replace.
         * @param relative A relative of that Pokemon, which is contained as a key in the regionMap.
         * @return An appropriate replacement Pokemon.
         */
        private Pokemon pickFamilyMemberReplacementInner(Pokemon toReplace, Pokemon relative) {
            PokemonAreaInformation info = areaInformationMap.get(toReplace);
            int relation = relative.getRelation(info.getPokemon(), true);
            Pokemon relativeReplacement = regionMap.get(relative);
            if(relativeReplacement == null) {
                throw new IllegalArgumentException("Relative had a null replacement!");
            }

            PokemonSet possibleReplacements = relativeReplacement.getRelativesAtPosition(relation, false);
            possibleReplacements.retainAll(remaining);
            possibleReplacements.removeAll(info.getBannedForReplacement());
            if(!possibleReplacements.isEmpty()) {
                pickReplacement(toReplace, possibleReplacements);
            }
            //else - remaining didn't have any valid, but allowed should.
            possibleReplacements = relativeReplacement.getRelativesAtPosition(relation, false);
            possibleReplacements.retainAll(allowed);
            possibleReplacements.removeAll(info.getBannedForReplacement());
            if(!possibleReplacements.isEmpty()) {
                pickReplacement(toReplace, possibleReplacements);
            }
            //else - we messed up earlier, this Pokemon has no replacement
            throw new IllegalStateException("Chose a family that is invalid!");
        }

        /**
         * Checks if any family member (as listed in areaInformationMap) of the given Pokemon
         * is contained in the regionMap.
         * @param current The Pokemon to check.
         * @return True if any family member is present, false otherwise.
         */
        private boolean mapHasFamilyMember(Pokemon current) {
           PokemonAreaInformation info = areaInformationMap.get(current);
           PokemonSet family = info.getFamily();
           for(Pokemon relative : family) {
               if(regionMap.containsKey(relative)) {
                   return true;
               }
           }

           return false;
        }

        /**
         * Given an encounter and area, and optionally a type, runs (the equivalent of) setupAllowedForArea and
         * setupAllowedForReplacement on allowed, rather than remaining.
         * @param enc The encounter to choose replacements for.
         * @param areaType The type theme for the area, or null if none.
         * @return A PokemonSet containing the allowed replacements for this encounter and area.
         */
        private PokemonSet retrySetupAllowedForAreaAndReplacement(Encounter enc, EncounterArea area, Type areaType) {
            PokemonSet allowedForArea = removeBannedFromArea(
                    (areaType == null) ? allowed : allowedByType.get(areaType),
                    area);

            return setupAllowedForReplacement(enc, allowedForArea);
        }



        /**
         * Given a list of EncounterAreas, all on the same map, randomizes them with as many
         * different Pokemon as it can without crashing.
         * @param map The map to randomize.
         */
        private void randomizeMapORAS(List<EncounterArea> map) {

            class EncounterWithData {
                //fully functional and anatomically correct *is shot*
                int areaIndex;
                Pokemon originalPokemon;
                Encounter encounter;
            }

            //log original Pokemon
            List<EncounterWithData> encounters = new ArrayList<>();
            for(int i = 0; i < map.size(); i++){
                EncounterArea area = map.get(i);
                for(Encounter enc : area) {
                    EncounterWithData data = new EncounterWithData();
                    data.encounter = enc;
                    data.originalPokemon = enc.getPokemon();
                    data.areaIndex = i;
                    encounters.add(data);
                }
            }

            //do area 1-to-1 to make sure everything gets SOME randomization
            this.area1to1Encounters(map);

            //set to the proper settings, in case it matters
            useMapping = false;
            useLocations = false;

            //then do more randomizing!
            Collections.shuffle(encounters, random);
            while(getORASDexNavLoad(map) < ORAS_CRASH_THRESHOLD && !encounters.isEmpty()) {

                EncounterWithData encData = encounters.remove(0);

                Encounter enc = encData.encounter;
                //check if there's another encounter with the same Pokemon - otherwise, this is a waste of time
                //(And, if we're using catchEmAll, a removal of a used Pokemon, which is bad.)
                boolean anotherExists = false;
                for(EncounterWithData otherEncData : encounters) {
                    if(enc.getPokemon() == otherEncData.encounter.getPokemon()) {
                        anotherExists = true;
                        break;
                    }
                }
                if(!anotherExists) {
                    continue;
                }

                //now the standard replacement logic
                areaType = findExistingAreaType(map.get(encData.areaIndex));
                allowedForArea = setupAllowedForArea();

                //reset the Pokemon
                //(Matters for keep primary, similar strength)
                enc.setPokemon(encData.originalPokemon);

                Pokemon replacement = pickReplacement(enc);
                enc.setPokemon(replacement);
                setFormeForEncounter(enc, replacement);

                if (catchEmAll) {
                    removeFromRemaining(replacement);
                }
            }

        }

        /**
         * Chooses an appropriate type theme for the given region based on the current settings:
         * If keepTypeThemes is true, chooses an existing theme if there is one.
         * If no theme was chosen, and randomTypeThemes is true, chooses a theme at random.
         * (Exception: If using catch-em-all and a banned Pokemon was present in the regions, the
         * chosen "random" type will be one of the banned Pokemon's types.)
         * @param region A List of EncounterAreas representing an appropriately-sized region for randomization.
         * @return A Type chosen by one of the above-listed methods, or null if none was chosen.
         */
        private Type pickRegionType(List<EncounterArea> region) {
            Type picked = null;
            if(keepTypeThemes) {
                //see if any types are shared among all areas in the region
                Set<Type> possibleThemes = EnumSet.allOf(Type.class);
                for(EncounterArea area : region) {
                    possibleThemes.retainAll(area.getPokemonInArea().getSharedTypes(true));
                    if(possibleThemes.isEmpty()) {
                        break;
                    }
                }

                //if so, pick one, preferring it to not be Normal
                if(!possibleThemes.isEmpty()) {
                    Iterator<Type> itor = possibleThemes.iterator();
                    picked = itor.next();
                    if(picked == Type.NORMAL && itor.hasNext()) {
                        picked = itor.next();
                    }
                }
            }

            if(picked == null && randomTypeThemes) {
                picked = pickRandomTypeWithPokemonRemaining();

                // Unown clause - since Unown (and other banned Pokemon) aren't randomized with catchEmAll active,
                // the "random" type theme must be one of the banned Pokemon's types.
                // The implementation below supports multiple banned Pokemon of the same type in the same area,
                // because why not?
                if (catchEmAll) {
                    PokemonSet bannedInArea = new PokemonSet(banned);
                    PokemonSet pokemonInRegion = new PokemonSet();
                    region.forEach(area -> pokemonInRegion.addAll(area.getPokemonInArea()));
                    bannedInArea.retainAll(pokemonInRegion);

                    Type themeOfBanned = bannedInArea.getSharedType(false);
                    if (themeOfBanned != null) {
                        picked = themeOfBanned;
                    }
                }
            }

            return picked;
        }

        //This is mostly an optimization at this point, but it's somewhat useful in that capacity.
        private Type pickAreaType(EncounterArea area) {
            Type picked = null;
            if (keepTypeThemes) {
                picked = area.getPokemonInArea().getSharedType(true);
            }
            return picked;
        }

        private Type pickRandomTypeWithPokemonRemaining() {
            List<Type> types = new ArrayList<>(typeService.getTypes());
            Collections.shuffle(types, random);
            Type areaType;
            do {
                areaType = types.remove(0);
            } while (remainingByType.get(areaType).isEmpty() && !types.isEmpty());
            if(types.isEmpty()) {
                throw new IllegalStateException("RemainingByType contained no Pokemon of any valid type!");
            }
            return areaType;
        }

        private Type findExistingAreaType(EncounterArea area) {
            Type areaType = null;
            if(keepTypeThemes || randomTypeThemes) {
                PokemonSet inArea = area.getPokemonInArea();
                areaType = inArea.getSharedType(false);
            }
            return areaType;
        }

        /**
         * Given an area and (optionally) a Type, returns a set of Pokemon valid for placement in that area,
         * of the given type if there was one.
         * @param areaType The Type which all Pokemon returned should have, or null.
         * @param area The area to find allowed Pokemon for.
         * @return A PokemonSet (which may be a reference to an existing set) which contains all Pokemon allowed
         * for the area.
         */
        private PokemonSet setupAllowedForArea(Type areaType, EncounterArea area) {

            PokemonSet allowedForArea;
            if (areaType != null) {
                allowedForArea = removeBannedFromArea(remainingByType.get(areaType), area);
                if(allowedForArea.isEmpty()) {
                    allowedForArea = removeBannedFromArea(allowedByType.get(areaType), area);
                }
            } else {
                allowedForArea = removeBannedFromArea(remaining, area);
                if(allowedForArea.isEmpty()) {
                    allowedForArea = removeBannedFromArea(allowed, area);
                }
            }

            return allowedForArea;
        }

        /**
         * Removes all Pokemon banned from the given area from the given pool.
         * Safe to pass referenced PokemonSets to.
         * @param startingPool The pool of Pokemon to start from.
         * @param area The area to check for banned Pokemon.
         * @return startingPool if the area had no banned Pokemon; a new PokemonSet with the banned Pokemon removed
         * otherwise.
         */
        private PokemonSet removeBannedFromArea(PokemonSet startingPool, EncounterArea area) {
            PokemonSet banned = area.getBannedPokemon();
            if(!banned.isEmpty()) {
                startingPool = new PokemonSet(startingPool); //don't want to remove from the original!
                startingPool.removeAll(banned);
            }

            return startingPool;
        }

        /**
         * Given an encounter, chooses a set of potential replacements for that encounter.
         * @param enc The encounter to replace.
         * @param area The area the encounter is in. Used to determine banned Pokemon if areaInformationMap is not
         *             populated.
         * @return A PokemonSet containing all valid replacements for the encounter. This may be a reference to another
         *          set; do not modify!
         */
        private PokemonSet setupAllowedForReplacement(Encounter enc, EncounterArea area) {
            if(areaInformationMap == null) {
                //Since this method is only called when the Pokemon need individual type restrictions,
                //this should only happen if we're using keepPrimary.
                //However, I'm not going to make that an assumed condition.

                if(keepPrimaryType) {
                    Type primary = enc.getPokemon().getPrimaryType(true);
                    return setupAllowedForArea(primary, area);
                } else {
                    //this shouldn't be reached, but maybe that will change in future
                    return setupAllowedForArea(null, area);
                }
            }
            //else
            PokemonAreaInformation info = areaInformationMap.get(enc.getPokemon());
            if(info == null) {
                //technically, this is the same situation as the above. However, this should not happen with the current
                //flow, so we throw an exception.
                throw new IllegalStateException("Info was null for encounter's species!");
            }

            Type type = info.getTheme(keepPrimaryType);
            boolean needsInner = !info.bannedForReplacement.isEmpty() || keepEvolutions;
            //if neither of these is true, the only restriction is the type

            PokemonSet possiblyAllowed = (type == null) ? remaining : remainingByType.get(type);
            if(needsInner) {
                possiblyAllowed = setupAllowedForReplacementInner(info, possiblyAllowed);
            }
            if(!possiblyAllowed.isEmpty()) {
                return possiblyAllowed;
            }
            //else - it didn't work looking at remaining. Let's try allowed.

            possiblyAllowed = (type == null) ? allowed : allowedByType.get(type);
            if(needsInner) {
                possiblyAllowed = setupAllowedForReplacementInner(info, possiblyAllowed);
            }
            if(!possiblyAllowed.isEmpty()) {
                return possiblyAllowed;
            }

            //it didn't work for allowed, either
            throw new RandomizationException("Could not find any replacements for wild encounter " + enc + "!");
        }

        /**
         * Given an encounter and a set of potential replacements for that encounter, narrows it down to valid
         * replacements for the encounter.
         * Assumes that any type restrictions have already been applied.
         * @param enc The encounter to set up replacements for.
         * @param startingPool The pool to start from.
         * @return startingPool if all Pokemon in it were valid; a new PokemonSet containing all valid Pokemon from
         *      startingPool otherwise.
         */
        private PokemonSet setupAllowedForReplacement(Encounter enc, PokemonSet startingPool) {
            if(areaInformationMap == null) {
                //we have no information about this encounter, and assume that means it needs no individual treatment.
                return startingPool;
            }
            PokemonAreaInformation info = areaInformationMap.get(enc.getPokemon());
            if(info == null) {
                //technically, this is the same situation as the above. However, this should not happen with the current
                //flow, so we throw an exception.
                throw new IllegalStateException("Info was null for encounter's species!");
            }

            if(info.getBannedForReplacement().isEmpty() &&
                    !keepEvolutions) {
                //allowedForReplacement is exactly startingPool
                return startingPool;
            }

            //we actually need to run the inner

            return setupAllowedForReplacementInner(info, startingPool);
            //no check if it's empty, because we don't have the information needed to retry if it is.
        }

        /**
         * Given a PokemonAreaInformation and a pool of Pokemon, narrows the pool down to Pokemon valid as determined
         * by the PokemonAreaInformation.
         * Assumes all type restrictions have already been applied.
         * @param info The restrictions for the current encounter.
         * @param startingPool The pool to start from.
         * @return startingPool if no additional restrictions were applied, a new PokemonSet with the narrowed
         * set otherwise.
         */
        private PokemonSet setupAllowedForReplacementInner(PokemonAreaInformation info, PokemonSet startingPool) {
            PokemonSet allowedForReplacement;
            if(!info.getBannedForReplacement().isEmpty()) {
                allowedForReplacement = new PokemonSet(startingPool);
                allowedForReplacement.removeAll(info.getBannedForReplacement());
            } else {
                allowedForReplacement = startingPool;
            }

            if(keepEvolutions) {
                allowedForReplacement = setupAllowedForFamily(allowedForReplacement, info);
            }
            return allowedForReplacement;
        }

        private Pokemon pickReplacement(Pokemon current, PokemonSet allowedForReplacement) {
            if (allowedForReplacement == null || allowedForReplacement.isEmpty()) {
                throw new IllegalArgumentException("No allowed Pokemon to pick as replacement.");
            }

            Pokemon replacement;
            // In Catch 'Em All mode, don't randomize encounters for Pokemon that are banned for
            // wild encounters. Otherwise, it may be impossible to obtain this Pokemon unless it
            // randomly appears as a static or unless it becomes a random evolution.
            if (catchEmAll && banned.contains(current)) {
                replacement = current;
            } else if (similarStrength) {
                if(balanceShakingGrass) {
                    PokemonAreaInformation info = areaInformationMap.get(current);
                    int bstToUse = Math.min(current.getBSTForPowerLevels(), info.getLowestLevel() * 10 + 250);

                    replacement = allowedForReplacement.getRandomSimilarStrengthPokemon(bstToUse, random);
                } else {
                    replacement = allowedForReplacement.getRandomSimilarStrengthPokemon(current, random);
                }
            } else {
                replacement = allowedForReplacement.getRandomPokemon(random);
            }
            return replacement;
        }

        /**
         * Removes the given Pokemon from "remaining" and all variants that are in use.
         * If remaining is empty after removing, refills it.
         * @param replacement The Pokemon to remove.
         */
        private void removeFromRemaining(Pokemon replacement) {
            remaining.remove(replacement);
            if (needsTypes) {
                remainingByType.get(replacement.getPrimaryType(false)).remove(replacement);
                if (replacement.hasSecondaryType(false)) {
                    remainingByType.get(replacement.getSecondaryType(false)).remove(replacement);
                }
            }

            if(remaining.isEmpty()) {
                refillRemainingPokemon();
            }
        }

        private void enforceMultipleSpecies(EncounterArea area) {
            // If an area with forceMultipleSpecies yet has a single species,
            // just randomly pick a different species for one of the Encounters.
            // This is very unlikely to happen in practice, even with very
            // restrictive settings, so it should be okay to break logic here.
            while (area.stream().distinct().count() == 1) {
                area.get(0).setPokemon(rPokeService.randomPokemon(random));
            }
        }

        /**
         * For each Pokemon in the areaInfoMap, for each that has a type theme, adds that theme
         * to each listed member of its family. <br>
         * setupAreaInfoMap() must be called before this method!
         * @throws IllegalStateException if areaInformationMap is null.
         * @throws IllegalArgumentException if families contains a Pokemon which has no
         * information in areaInformationMap.
         */
        private void spreadThemesThroughFamilies() {
            PokemonSet completedFamilies = new PokemonSet();
            if(areaInformationMap == null) {
                throw new IllegalStateException("Cannot spread themes before determining themes!");
            }
            for(PokemonAreaInformation info : areaInformationMap.values()) {
                if(info == null) {
                    throw new IllegalStateException("AreaInfoMap contained a null value!");
                }
                Pokemon poke = info.getPokemon();
                if(completedFamilies.contains(poke)) {
                    continue;
                }

                PokemonSet family = info.getFamily();
                completedFamilies.addAll(family);

                //this algorithm weights any area which contains (for example) two Pokemon in the family twice as strongly
                //this is probably acceptable
                Map<Type, Integer> familyThemeInfo = new EnumMap<>(Type.class);
                for(Pokemon relative : family) {

                    //get this Pokemon's possible themes
                    PokemonAreaInformation relativeInfo = areaInformationMap.get(relative);
                    if(relativeInfo == null) {
                        throw new IllegalArgumentException("Cannot spread themes among Pokemon without theme information!");
                    }
                    Map<Type, Integer> themeInfo = relativeInfo.getAllPossibleThemes();

                    //add them to the total theme info
                    for(Map.Entry<Type, Integer> possibleTheme : themeInfo.entrySet()) {
                        Type theme = possibleTheme.getKey();
                        int count = possibleTheme.getValue();

                        if(familyThemeInfo.containsKey(theme)) {
                            int existingCount = familyThemeInfo.get(theme);
                            count += existingCount;
                        }
                        familyThemeInfo.put(theme, count);
                    }
                }

                //set our determined theme info to the whole family
                for(Pokemon relative : family) {
                    PokemonAreaInformation relativeInfo = areaInformationMap.get(relative);
                    if(relativeInfo == null) {
                        //shouldn't be possible
                        throw new RuntimeException("Pokemon's info became null between checking and setting themes??");
                    }
                    relativeInfo.setPossibleThemes(familyThemeInfo);
                }

            }
        }

        /**
         * Narrows the given pool of Pokemon down to one that is compatible with the family contained in the
         * given area information. Uses the full allowed pool for relatives.
         * Ignores all type restrictions.
         * @param potentiallyAllowed The set of Pokemon to work from.
         * @param info The information of the Pokemon to match.
         * @return A new PokemonSet narrowed down as specified.
         * @throws RandomizationException if no match for the given family can be found in the allowed pool.
         */
        private PokemonSet setupAllowedForFamily(PokemonSet potentiallyAllowed, PokemonAreaInformation info) {
            PokemonSet family = info.getFamily();
            Pokemon match = info.getPokemon();

            int before = family.getNumberEvoStagesBefore(match, true);
            int after = family.getNumberEvoStagesAfter(match, true);
            potentiallyAllowed = potentiallyAllowed.filterHasEvoStages(before, after, false);

            for(Pokemon relative : family) {
                int relation = match.getRelation(relative, true);

                //Remove all Pokemon for which "relative" cannot be replaced by any corresponding relative
                //either because it's not in the allowed pool, or it's banned
                potentiallyAllowed = potentiallyAllowed.filter(p -> {
                       PokemonSet sameRelations = p.getRelativesAtPositionSameBranch(relation, false);
                       sameRelations.retainAll(allowed);
                       sameRelations.removeAll(areaInformationMap.get(relative).getBannedForReplacement());
                       return !sameRelations.isEmpty();
                });
            }

            //Try to remove any Pokemon which have a relative that has already been used
            PokemonSet withoutUsedFamilies = potentiallyAllowed.filter(p ->
                    !p.getFamily(false).containsAny(regionMap.keySet()));

            return withoutUsedFamilies.isEmpty() ? potentiallyAllowed : withoutUsedFamilies;
        }

        /**
         * Given the EncounterAreas for a single map, calculates the DexNav load for that map.
         * The DexNav crashes if this load is above ORAS_CRASH_THRESHOLD.
         * @param areasInMap A List of EncounterAreas, all of which are from the same map.
         * @return The DexNav load for that map.
         */
        private int getORASDexNavLoad(List<EncounterArea> areasInMap) {
            //If the previous implementation is to be believed,
            //the load is equal to the number of distinct Pokemon in each area summed.
            //(Not the total number of unique Pokemon).
            //I am not going to attempt to verify this (yet).
            int load = 0;
            for(EncounterArea area : areasInMap) {
                if(area.getEncounterType() == EncounterType.INTERACT) {
                    //Rock Smash doesn't contribute to DexNav load.
                    continue;
                }

                PokemonSet pokemonInArea = new PokemonSet();
                for (Pokemon poke : area.getPokemonInArea()) {
                    //Different formes of the same Pokemon do not contribute to load
                    if(poke.isBaseForme()) {
                        pokemonInArea.add(poke);
                    } else {
                        pokemonInArea.add(poke.getBaseForme());
                    }
                }

                load += pokemonInArea.size();
            }
            return load;
        }

        /**
         * Given a set of EncounterAreas, creates a map of every Pokemon in the areas to
         * information about the areas that Pokemon is contained in.
         * If addAllPokemonTo is not null, also adds every Pokemon to it.
         * @param areas The list of EncounterAreas to explore.
         */
        private void setupAreaInfoMap(List<EncounterArea> areas) {

            PokemonSet existingPokemon = new PokemonSet();

            areaInformationMap = new HashMap<>();
            for(EncounterArea area : areas) {
                Type areaTheme = pickAreaType(area);
                int areaSize = area.getPokemonInArea().size();

                for(Pokemon pokemon : area.getPokemonInArea()) {
                    PokemonAreaInformation info = areaInformationMap.get(pokemon);

                    if(info == null) {
                        info = new PokemonAreaInformation(pokemon);
                        areaInformationMap.put(pokemon, info);

                        if(keepEvolutions) {
                            existingPokemon.add(pokemon);
                            PokemonSet family = existingPokemon.filterFamily(pokemon, true);
                            if(family.size() > 1) {
                                family.forEach(relative -> areaInformationMap.get(relative).addFamily(family));
                            }
                        }
                    }

                    info.addTypeTheme(areaTheme, areaSize);
                    info.banAll(area.getBannedPokemon());
                }
                if(balanceShakingGrass) {
                    //TODO: either verify that this IS a shaking grass encounter,
                    // or rename the setting.
                    // (Leaning towards the latter.)
                    for (Encounter enc : area) {
                        PokemonAreaInformation info = areaInformationMap.get(enc.getPokemon());
                        info.setLevelIfLower((enc.getLevel() + enc.getMaxLevel()) / 2);
                        //TODO: *Should* this be average level? Or should it be lowest?
                    }
                }
            }
        }

        /**
         * A class which stores some information about the areas a Pokemon was found in,
         * in order to allow us to use this information later.
         */
        private class PokemonAreaInformation {
            private Map<Type, Integer> possibleThemes = new EnumMap<>(Type.class);
            private final PokemonSet bannedForReplacement = new PokemonSet();
            private final PokemonSet family = new PokemonSet();
            private final Pokemon pokemon;
            private int lowestLevel = 100;

            /**
             * Creates a new RandomizationInformation with the given data.
             * @param pk The Pokemon this RandomizationInformation is about.
             */
            PokemonAreaInformation(Pokemon pk) {
                pokemon = pk;
            }

            /**
             * Adds all Pokemon in the given collection to the set of Pokemon banned for replacement.
             * @param banned The Collection of Pokemon to add.
             */
            public void banAll(Collection<Pokemon> banned) {
                bannedForReplacement.addAll(banned);
            }

            /**
             * Get the list of all Pokemon banned as replacements for this Pokemon.
             * @return A new unmodifiable {@link PokemonSet} containing the banned Pokemon.
             */
            public PokemonSet getBannedForReplacement() {
                return PokemonSet.unmodifiable(bannedForReplacement);
            }

            /**
             * Adds the given type and count of Pokemon to the list of existing themes for this Pokemon.
             * If theme is null, has no effect.
             * @param theme The type to add.
             * @throws IllegalArgumentException if count is less than 1.
             */
            public void addTypeTheme(Type theme, int count) {
                if (count < 1) {
                    throw new IllegalArgumentException("Number of Pokemon in theme cannot be less than 1!");
                }
                if(theme != null) {
                    if(possibleThemes.containsKey(theme)) {
                        int existingCount = possibleThemes.get(theme);
                        count += existingCount;
                    }
                    possibleThemes.put(theme, count);
                }
            }

            /**
             * Gets the type of this Pokemon's area theming. <br>
             * If there are two or more themes, returns the one with the highest count of Pokemon. If tied,
             * will choose the Pokemon's original primary type. If neither theme is the original primary,
             * chooses one arbitrarily.<br>
             * If there are no themes, it will default to the original primary only if defaultToPrimary is true;
             * otherwise, it will default to null.
             * @param defaultToPrimary Whether the type should default to the Pokemon's primary type
             *                         if there are no themes.
             * @return The type that should be used, or null for any type.
             */
            Type getTheme(boolean defaultToPrimary) {
                if(possibleThemes.isEmpty()) {
                    if(defaultToPrimary) {
                        return pokemon.getPrimaryType(true);
                    } else {
                        return null;
                    }
                } else {
                    Type bestTheme = null;
                    int bestThemeCount = 0;
                    for(Map.Entry<Type, Integer> possibleTheme : possibleThemes.entrySet()) {
                        int possibleThemeCount = possibleTheme.getValue();
                        if(possibleThemeCount > bestThemeCount) {
                            bestThemeCount = possibleThemeCount;
                            bestTheme = possibleTheme.getKey();
                        } else if(possibleThemeCount == bestThemeCount) {

                            //tie - default to primary if present
                            Type primary = pokemon.getPrimaryType(true);
                            if(primary == possibleTheme.getKey()) {
                                bestTheme = primary;
                            }
                            //if bestTheme is already primary, then no change is needed;
                            //if neither is primary, then we have no means of choosing & thus leave it as is.
                            //(The latter can possibly happen with family-to-family.)
                        }
                    }
                    return bestTheme;
                }
            }

            /**
             * Returns the set of all desired themes for this Pokemon.
             * @return A new Set containing all the possible themes.
             */
            Map<Type, Integer> getAllPossibleThemes() {
                return new EnumMap<>(possibleThemes);
            }

            /**
             * Sets the possible themes to match the data given.
             * @param replacementThemes A map of Types to weights of those types. (Normally, the highest weight will
             *                          be the type used.)
             */
            void setPossibleThemes(Map<Type, Integer> replacementThemes) {
                possibleThemes = new EnumMap<>(replacementThemes);
            }

            /**
             * Adds the Pokemon in the given set to this Pokemon's family.
             * @param family The Pokemon to add.
             */
            void addFamily(PokemonSet family) {
                this.family.addAll(family);
            }

            /**
             * Gets any members of the Pokemon's family that have been added to the information.
             * @return A new unmodifiable {@link PokemonSet} containing the Pokemon's family.
             */
            PokemonSet getFamily() {
                return PokemonSet.unmodifiable(family);
            }

            /**
             * Gets the Pokemon that this PokemonAreaInformation is about.
             * @return The Pokemon.
             */
            public Pokemon getPokemon() {
                return pokemon;
            }

            /**
             * Sets the lowest level to the level given, if it is lower than the current lowest level.
             * @param level The level to lower to.
             */
            void setLevelIfLower(int level) {
                lowestLevel = Math.min(level, lowestLevel);
            }

            /**
             * Gets the lowest level encounter with this Species in the region.
             * @return The lowest level.
             */
            public int getLowestLevel() {
                return lowestLevel;
            }
        }
    }

    /**
     * Prepares the EncounterAreas for randomization by copying them, removing unused areas, and shuffling the order.
     * @param unprepped The List of EncounterAreas to prepare.
     * @return A new List of all the same Encounters, with the areas shuffled and possibly merged as appropriate.
     */
    private List<EncounterArea> prepEncounterAreas(List<EncounterArea> unprepped) {
        // Clone the original set, so that we don't mess up saving
        List<EncounterArea> prepped = new ArrayList<>(unprepped);

        prepped.removeIf(area -> area.getEncounterType() == EncounterType.UNUSED
                || "UNUSED".equals(area.getLocationTag()));
        //don't randomize unused areas
        //mostly important for catch 'em all

        // Shuffling the EncounterAreas leads to less predictable results for various modifiers.
        Collections.shuffle(prepped, random);
        return prepped;
    }

    private void setFormeForEncounter(Encounter enc, Pokemon pk) {
        boolean checkCosmetics = true;
        enc.setFormeNumber(0);
        if (enc.getPokemon().getFormeNumber() > 0) {
            enc.setFormeNumber(enc.getPokemon().getFormeNumber());
            enc.setPokemon(enc.getPokemon().getBaseForme());
            checkCosmetics = false;
        }
        if (checkCosmetics && enc.getPokemon().getCosmeticForms() > 0) {
            enc.setFormeNumber(enc.getPokemon().getCosmeticFormNumber(this.random.nextInt(enc.getPokemon().getCosmeticForms())));
        } else if (!checkCosmetics && pk.getCosmeticForms() > 0) {
            enc.setFormeNumber(enc.getFormeNumber() + pk.getCosmeticFormNumber(this.random.nextInt(pk.getCosmeticForms())));
        }
        //TODO: instead of (most of) this function, have encounter store the actual forme used and call basePokemon when needed
        // Or.. some other solution to the problem of not recognizing formes in ORAS "enhance" logic
    }

    public void changeCatchRates() {
        int minimumCatchRateLevel = settings.getMinimumCatchRateLevel();

        if (minimumCatchRateLevel == 5) {
            romHandler.enableGuaranteedPokemonCatching();
        } else {
            int normalMin, legendaryMin;
            switch (minimumCatchRateLevel) {
                case 1:
                default:
                    normalMin = 75;
                    legendaryMin = 37;
                    break;
                case 2:
                    normalMin = 128;
                    legendaryMin = 64;
                    break;
                case 3:
                    normalMin = 200;
                    legendaryMin = 100;
                    break;
                case 4:
                    normalMin = legendaryMin = 255;
                    break;
            }
            minimumCatchRate(normalMin, legendaryMin);
        }
    }

    private void minimumCatchRate(int rateNonLegendary, int rateLegendary) {
        for (Pokemon pk : romHandler.getPokemonSetInclFormes()) {
            int minCatchRate = pk.isLegendary() ? rateLegendary : rateNonLegendary;
            pk.setCatchRate(Math.max(pk.getCatchRate(), minCatchRate));
        }

    }

}
