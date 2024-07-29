package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkrandom.game_data.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;

public class EncounterRandomizer extends Randomizer {

    public EncounterRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeEncounters() {
        Settings.WildPokemonMod mode = settings.getWildPokemonMod();
        boolean useTimeOfDay = settings.isUseTimeBasedEncounters();
        boolean randomTypeThemes = settings.getWildPokemonTypeMod() == Settings.WildPokemonTypeMod.THEMED_AREAS;
        boolean keepTypeThemes = settings.isKeepWildTypeThemes();
        boolean keepPrimaryType = settings.getWildPokemonTypeMod() == Settings.WildPokemonTypeMod.KEEP_PRIMARY;
        boolean catchEmAll = settings.isCatchEmAllEncounters();
        boolean similarStrength = settings.isSimilarStrengthEncounters();
        boolean noLegendaries = settings.isBlockWildLegendaries();
        boolean balanceShakingGrass = settings.isBalanceShakingGrass();
        int levelModifier = settings.isWildLevelsModified() ? settings.getWildLevelModifier() : 0;
        boolean allowAltFormes = settings.isAllowWildAltFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;

        randomizeEncounters(mode, useTimeOfDay,
                randomTypeThemes, keepTypeThemes, keepPrimaryType, catchEmAll, similarStrength, noLegendaries,
                balanceShakingGrass, levelModifier, allowAltFormes, banIrregularAltFormes, abilitiesAreRandomized);
        changesMade = true;
    }

    // only exists for some old test cases, please don't use
    public void randomizeEncounters(Settings.WildPokemonMod mode, Settings.WildPokemonTypeMod typeMode,
                                    boolean useTimeOfDay,
                                    boolean catchEmAll, boolean similarStrength,
                                    boolean noLegendaries, boolean balanceShakingGrass, int levelModifier,
                                    boolean allowAltFormes, boolean banIrregularAltFormes,
                                    boolean abilitiesAreRandomized) {
        randomizeEncounters(mode,
                useTimeOfDay,
                typeMode == Settings.WildPokemonTypeMod.THEMED_AREAS,
                false,
                typeMode == Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                catchEmAll, similarStrength,
                noLegendaries, balanceShakingGrass, levelModifier,
                allowAltFormes, banIrregularAltFormes,
                abilitiesAreRandomized);
    }

    // only public for some old test cases, please don't use
    public void randomizeEncounters(Settings.WildPokemonMod mode,
                                     boolean useTimeOfDay,
                                     boolean randomTypeThemes, boolean keepTypeThemes, boolean keepPrimaryType,
                                     boolean catchEmAll, boolean similarStrength,
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

        SpeciesSet banned = getBannedForWildEncounters(banIrregularAltFormes, abilitiesAreRandomized);
        SpeciesSet allowed = new SpeciesSet(rPokeService.getPokemon(noLegendaries, allowAltFormes, false));
        allowed.removeAll(banned);



        InnerRandomizer ir = new InnerRandomizer(allowed, banned,
                randomTypeThemes, keepTypeThemes, keepPrimaryType, catchEmAll, similarStrength, balanceShakingGrass);
        switch (mode) {
            case RANDOM:
                if(romHandler.isORAS()) {
                    //this mode crashes ORAS and needs special handling to approximate
                    ir.randomEncountersORAS(encounterAreas);
                } else {
                    ir.randomEncounters(encounterAreas);
                }
                break;
            case AREA_MAPPING:
                ir.area1to1Encounters(encounterAreas);
                break;
            case LOCATION_MAPPING:
                ir.location1to1Encounters(encounterAreas);
                break;
            case GLOBAL_MAPPING:
                ir.game1to1Encounters(encounterAreas);
                break;
            case FAMILY_MAPPING:
                ir.gameFamilyToFamilyEncounters(encounterAreas);
                break;
        }

        applyLevelModifier(levelModifier, encounterAreas);
        romHandler.setEncounters(useTimeOfDay, encounterAreas);
    }

    private SpeciesSet getBannedForWildEncounters(boolean banIrregularAltFormes,
                                                  boolean abilitiesAreRandomized) {
        SpeciesSet banned = new SpeciesSet();
        banned.addAll(romHandler.getBannedForWildEncounters());
        banned.addAll(rPokeService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rPokeService.getAbilityDependentFormes();
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

        private boolean map1to1;
        private boolean useLocations;

        private Map<Type, SpeciesSet> allowedByType;
        private final SpeciesSet allowed;
        private final SpeciesSet banned;

        private Map<Type, SpeciesSet> remainingByType;
        private SpeciesSet remaining;

        private Type areaType;
        private SpeciesSet allowedForArea;
        private Map<Species, Species> areaMap;
        private SpeciesSet allowedForReplacement;

        private Map<Species, PokemonAreaInformation> areaInformationMap;
        private SpeciesSet remainingFamilyRestricted;
        private Map<Type, SpeciesSet> remainingFamilyRestrictedByType;

        //ORAS's DexNav will crash if the load is higher than this value.
        final int ORAS_CRASH_THRESHOLD = 18;

        public InnerRandomizer(SpeciesSet allowed, SpeciesSet banned,
                               boolean randomTypeThemes, boolean keepTypeThemes, boolean keepPrimaryType,
                               boolean catchEmAll, boolean similarStrength, boolean balanceShakingGrass) {
            if (randomTypeThemes && keepPrimaryType) {
                throw new IllegalArgumentException("Can't use keepPrimaryType with randomTypeThemes.");
            }
            this.randomTypeThemes = randomTypeThemes;
            this.keepTypeThemes = keepTypeThemes;
            this.keepPrimaryType = keepPrimaryType;
            this.needsTypes = keepPrimaryType || keepTypeThemes || randomTypeThemes;
            this.catchEmAll = catchEmAll;
            this.similarStrength = similarStrength;
            this.balanceShakingGrass = balanceShakingGrass;
            this.allowed = allowed;
            this.banned = banned;
            if (needsTypes) {
                this.allowedByType = allowed.sortByType(false, typeService.getTypes());
            }
            if (catchEmAll) {
                refillRemainingPokemon();
            }
        }

        private void refillRemainingPokemon() {
            remaining = new SpeciesSet(allowed);
            if (needsTypes) {
                remainingByType = new EnumMap<>(Type.class);
                for (Type t : typeService.getTypes()) {
                    remainingByType.put(t, new SpeciesSet(allowedByType.get(t)));
                }
            }
        }

        public void randomEncounters(List<EncounterArea> encounterAreas) {
            map1to1 = false;
            useLocations = false;
            randomEncountersInner(encounterAreas);
        }

        public void area1to1Encounters(List<EncounterArea> encounterAreas) {
            map1to1 = true;
            useLocations = false;
            randomEncountersInner(encounterAreas);
        }

        public void location1to1Encounters(List<EncounterArea> encounterAreas) {
            map1to1 = true;
            useLocations = true;
            randomEncountersInner(encounterAreas);
        }

        private void randomEncountersInner(List<EncounterArea> encounterAreas) {
            List<EncounterArea> preppedEncounterAreas = prepEncounterAreas(encounterAreas);
            for (EncounterArea area : preppedEncounterAreas) {
                areaType = pickAreaType(area);
                allowedForArea = setupAllowedForArea();

                areaMap = new TreeMap<>();

                for (Encounter enc : area) {
                    Species replacement = pickReplacement(enc);
                    if (map1to1) {
                        areaMap.put(enc.getSpecies(), replacement);
                    }

                    enc.setSpecies(replacement);
                    setFormeForEncounter(enc, replacement);

                    if (catchEmAll) {
                        removeFromRemaining(replacement);
                        if (allowedForArea.isEmpty()) {
                            allowedForArea = setupAllowedForArea();
                        }
                    }
                }
                if (area.isForceMultipleSpecies()) {
                    enforceMultipleSpecies(area);
                }
            }
        }

        /**
         * Special case to approximate random encounters in ORAS, since they crash if the
         * normal algorithm is used.
         * @param encounterAreas The list of EncounterAreas to randomize.
         */
        private void randomEncountersORAS(List<EncounterArea> encounterAreas) {

            List<EncounterArea> collapsedEncounters = flattenEncounterTypesInMaps(encounterAreas);
            List<List<EncounterArea>> maps = new ArrayList<>(groupAreasByMapIndex(collapsedEncounters).values());
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

        /**
         * Given a list of EncounterAreas, all on the same map, randomizes them with as many
         * different Pokemon as it can without crashing.
         * @param map The map to randomize.
         */
        private void randomizeMapORAS(List<EncounterArea> map) {

            class EncounterWithData {
                //fully functional and anatomically correct *is shot*
                int areaIndex;
                Species originalPokemon;
                Encounter encounter;
            }

            //log original Pokemon
            List<EncounterWithData> encounters = new ArrayList<>();
            for(int i = 0; i < map.size(); i++){
                EncounterArea area = map.get(i);
                for(Encounter enc : area) {
                    EncounterWithData data = new EncounterWithData();
                    data.encounter = enc;
                    data.originalPokemon = enc.getSpecies();
                    data.areaIndex = i;
                    encounters.add(data);
                }
            }

            //do area 1-to-1 to make sure everything gets SOME randomization
            this.area1to1Encounters(map);

            //set to the proper settings, in case it matters
            map1to1 = false;
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
                    if(enc.getSpecies() == otherEncData.encounter.getSpecies()) {
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
                enc.setSpecies(encData.originalPokemon);

                Species replacement = pickReplacement(enc);
                enc.setSpecies(replacement);
                setFormeForEncounter(enc, replacement);

                if (catchEmAll) {
                    removeFromRemaining(replacement);
                }
            }

        }

        /**
         * Prepares the EncounterAreas for randomization by shuffling the order and flattening them if appropriate.
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

            if (useLocations) {
                prepped = flattenLocations(prepped);
            } else if (romHandler.isORAS()) {
                //some modes crash in ORAS if the maps aren't flattened
                prepped = flattenEncounterTypesInMaps(prepped);
            }
            // Shuffling the EncounterAreas leads to less predictable results for various modifiers.
            Collections.shuffle(prepped, random);
            return prepped;
        }

        /**
         * Given a List of EncounterAreas, merges those that have the same Location tag.
         * @param unflattened The set of EncounterAreas to merge.
         * @return A List of EncounterAreas with the specified areas merged.
         */
        private List<EncounterArea> flattenLocations(List<EncounterArea> unflattened) {
            Map<String, List<EncounterArea>> grouped = groupAreasByLocation(unflattened);
            List<EncounterArea> flattenedLocations = new ArrayList<>();
            for (Map.Entry<String, List<EncounterArea>> locEntry : grouped.entrySet()) {
                EncounterArea flattened = new EncounterArea();
                flattened.setDisplayName("All of location " + locEntry.getKey());
                for(EncounterArea area : locEntry.getValue()) {
                    flattened.addAll(area);
                    flattened.banAllPokemon(area.getBannedPokemon());
                }
                flattenedLocations.add(flattened);
            }
            return flattenedLocations;
        }

        /**
         * Given a List of EncounterAreas, merges those that have the same map index and encounter type.
         * @param unflattened The set of EncounterAreas to merge.
         * @return A List of EncounterAreas with the specified areas merged.
         */
        private List<EncounterArea> flattenEncounterTypesInMaps(List<EncounterArea> unflattened) {
            Map<Integer, List<EncounterArea>> grouped = groupAreasByMapIndex(unflattened);
            List<EncounterArea> flattenedEncounters = new ArrayList<>();
            int unnamed = 1;
            for (Map.Entry<Integer, List<EncounterArea>> mapEntry : grouped.entrySet()) {
                Map<EncounterType, List<EncounterArea>> mapGrouped =
                        groupAreasByEncounterType(mapEntry.getValue());
                String mapName = mapEntry.getValue().get(0).getLocationTag();
                if (mapName == null) {
                    mapName = "Unknown Map " + unnamed;
                    unnamed++;
                }
                for (Map.Entry<EncounterType, List<EncounterArea>> typeEntry : mapGrouped.entrySet()) {
                    EncounterArea flattened = new EncounterArea();
                    flattened.setDisplayName(mapName + "-" + typeEntry.getKey().name());
                    flattened.setEncounterType(typeEntry.getKey());
                    flattened.setMapIndex(mapEntry.getKey());
                    for (EncounterArea area : typeEntry.getValue()) {
                        flattened.addAll(area);
                        flattened.banAllPokemon(area.getBannedPokemon());
                    }
                    flattenedEncounters.add(flattened);
                }
            }
            return flattenedEncounters;
        }

        /**
         * Given a List of EncounterAreas, groups those that have the same map index.
         * @param ungrouped The set of EncounterAreas to group.
         * @return A Map of mapIndexes to EncounterAreas.
         */
        private Map<Integer, List<EncounterArea>> groupAreasByMapIndex(List<EncounterArea> ungrouped) {
            Map<Integer, List<EncounterArea>> grouped = new HashMap<>();
            for (EncounterArea area : ungrouped) {
                int index = area.getMapIndex();
                if (!grouped.containsKey(index)) {
                    grouped.put(index, new ArrayList<>());
                }
                grouped.get(index).add(area);
            }
            return grouped;
        }

        /**
         * Given a List of EncounterAreas, groups those that have the same location tag.
         * @param ungrouped The set of EncounterAreas to group.
         * @return A Map of locationTags to EncounterAreas.
         */
        private Map<String, List<EncounterArea>> groupAreasByLocation(List<EncounterArea> ungrouped) {
            Map<String, List<EncounterArea>> grouped = new HashMap<>();
            int untagged = 1;
            for (EncounterArea area : ungrouped) {
                String tag = area.getLocationTag();
                if (tag == null) {
                    tag = "UNTAGGED-" + untagged;
                    untagged++;
                }
                if (!grouped.containsKey(tag)) {
                    grouped.put(tag, new ArrayList<>());
                }
                grouped.get(tag).add(area);
            }
            return grouped;
        }

        /**
         * Given a List of EncounterAreas, groups those that have the same encounter type.
         * @param ungrouped The set of EncounterAreas to group.
         * @return A Map of encounterTypes to EncounterAreas.
         */
        private Map<EncounterType, List<EncounterArea>>
                        groupAreasByEncounterType(List<EncounterArea> ungrouped) {
            Map<EncounterType, List<EncounterArea>> grouped = new HashMap<>();
            for (EncounterArea area : ungrouped) {
                EncounterType encType = area.getEncounterType();
                if (!grouped.containsKey(encType)) {
                    grouped.put(encType, new ArrayList<>());
                }
                grouped.get(encType).add(area);
            }
            return grouped;
        }

        //should the above functions instead be in EncounterArea?

        private Type pickAreaType(EncounterArea area) {
            Type picked = null;
            if (keepTypeThemes) {
                picked = area.getSpeciesInArea().getSharedType(true);
            }
            if (randomTypeThemes && picked == null) {
                picked = pickRandomAreaType();

                // Unown clause - since Unown (and other banned Pokemon) aren't randomized with catchEmAll active,
                // the "random" type theme must be one of the banned's types.
                // The implementation below supports multiple banned Pokemon of the same type in the same area,
                // because why not?
                if (catchEmAll) {
                    SpeciesSet bannedInArea = new SpeciesSet(banned);
                    bannedInArea.retainAll(area.getSpeciesInArea());
                    Type themeOfBanned = bannedInArea.getSharedType(false);
                    if (themeOfBanned != null) {
                        picked = themeOfBanned;
                    }
                }
            }
            return picked;
        }

        private Type pickRandomAreaType() {
            Map<Type, SpeciesSet> byType = catchEmAll ? remainingByType : allowedByType;
            Type areaType;
            do {
                areaType = typeService.randomType(random);
            } while (byType.get(areaType).isEmpty());
            //TODO: ensure loop terminates
            return areaType;
        }

        private Type findExistingAreaType(EncounterArea area) {
            Type areaType = null;
            if(keepTypeThemes || randomTypeThemes) {
                SpeciesSet inArea = area.getSpeciesInArea();
                areaType = inArea.getSharedType(false);
            }
            return areaType;
        }

        private SpeciesSet setupAllowedForArea() {
            if (areaType != null) {
                return catchEmAll && !remainingByType.get(areaType).isEmpty()
                        ? remainingByType.get(areaType) : allowedByType.get(areaType);
            } else {
                return catchEmAll ? remaining : allowed;
            }

            //TODO: remove locally banned Pokemon
        }

        private Species pickReplacement(Encounter enc) {
            allowedForReplacement = allowedForArea;
            if (keepPrimaryType && areaType == null) {
                allowedForReplacement = getAllowedReplacementPreservePrimaryType(enc);
            }

            if (map1to1) {
                return pickReplacement1to1(enc);
            } else {
                return pickReplacementInner(enc);
            }
        }


        private SpeciesSet getAllowedReplacementPreservePrimaryType(Encounter enc) {
            //TODO: ensure this works correctly with area bans
            Species current = enc.getSpecies();
            Type primaryType = current.getPrimaryType(true);
            return catchEmAll && !remainingByType.get(primaryType).isEmpty()
                    ? remainingByType.get(primaryType) : allowedByType.get(primaryType);
        }

        private Species pickReplacementInner(Encounter enc) {
            if (allowedForReplacement.isEmpty()) {
                throw new IllegalStateException("No allowed Pokemon to pick as replacement.");
            }
            Species current = enc.getSpecies();

            Species replacement;
            // In Catch 'Em All mode, don't randomize encounters for Pokemon that are banned for
            // wild encounters. Otherwise, it may be impossible to obtain this Pokemon unless it
            // randomly appears as a static or unless it becomes a random evolution.
            if (catchEmAll && banned.contains(current)) {
                replacement = current;
            } else if (similarStrength) {
                if(balanceShakingGrass) {
                    int bstToUse = current.getBSTForPowerLevels();
                    int avgLevel = (enc.getLevel() + enc.getMaxLevel()) / 2;
                    bstToUse = Math.min(bstToUse, avgLevel * 10 + 250);
                    replacement = allowedForReplacement.getRandomSimilarStrengthSpecies(bstToUse, random);
                } else {
                    replacement = allowedForReplacement.getRandomSimilarStrengthSpecies(current, random);
                }
            } else {
                replacement = allowedForReplacement.getRandomSpecies(random);
            }
            return replacement;
        }

        private Species pickReplacement1to1(Encounter enc) {
            Species current = enc.getSpecies();
            if (areaMap.containsKey(current)) {
                return areaMap.get(current);
            } else {
                // the below loop ensures no two Pokemon are given the same replacement,
                // unless that's impossible due to the (small) size of allowedForArea
                Species replacement;
                do {
                    replacement = pickReplacementInner(enc);
                } while (areaMap.containsValue(replacement) && areaMap.size() < allowedForArea.size());
                return replacement;
            }
        }

        /**
         * Removes the given Pokemon from "remaining" and all variants that are in use.
         * If remaining is empty after removing, refills it.
         * @param replacement The Pokemon to remove.
         */
        private void removeFromRemaining(Species replacement) {
            remaining.remove(replacement);
            if (needsTypes) {
                remainingByType.get(replacement.getPrimaryType(false)).remove(replacement);
                if (replacement.hasSecondaryType(false)) {
                    remainingByType.get(replacement.getSecondaryType(false)).remove(replacement);
                }
            }
            if(remainingFamilyRestricted != null) {
                remainingFamilyRestricted.remove(replacement);
                if (needsTypes) {
                    remainingFamilyRestrictedByType.get(replacement.getPrimaryType(false)).remove(replacement);
                    if (replacement.hasSecondaryType(false)) {
                        remainingFamilyRestrictedByType.get(replacement.getSecondaryType(false)).remove(replacement);
                    }
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
                area.get(0).setSpecies(rPokeService.randomPokemon(random));
            }
        }

        // quite different functionally from the other random encounter methods,
        // but still grouped in this inner class due to conceptual cohesion
        public void game1to1Encounters(List<EncounterArea> encounterAreas) {
            refillRemainingPokemon();
            Map<Species, Species> translateMap = new HashMap<>();
            List<EncounterArea> prepped = prepEncounterAreas(encounterAreas);
            //mostly to skip unused areas, since the order doesn't matter

            setupAreaInfoMap(prepped, null);

            // shuffle to not give certain Pokémon priority when picking replacements
            // matters for similar strength
            List<PokemonAreaInformation> shuffled = new ArrayList<>(areaInformationMap.values());
            Collections.shuffle(shuffled, random);

            for (PokemonAreaInformation current : shuffled) {
                Species replacement = pickGame1to1Replacement(current);
                translateMap.put(current.getPokemon(), replacement);
            }

            applyGlobalMap(prepped, translateMap);
        }

        /**
         * Randomizes the set of EncounterAreas given such that for each Pokemon in the areas, they
         * will be replaced by exactly one new Pokemon, and its evolutionary family will replace the
         * original Pokemon's family.
         * @param encounterAreas The EncounterAreas to randomize.
         */
        public void gameFamilyToFamilyEncounters(List<EncounterArea> encounterAreas) {
            refillRemainingPokemon();
            Map<Species, Species> translateMap = new HashMap<>();
            List<EncounterArea> prepped = prepEncounterAreas(encounterAreas);

            SpeciesSet pokemonToRandomize = new SpeciesSet();
            setupAreaInfoMap(prepped, pokemonToRandomize);

            spreadThemesThroughFamilies(pokemonToRandomize);

            //assumes that the longest evo line to randomize is 3 (or shorter)
            //this seems a safe assumption given no 4-length evo line exists in Pokemon
            //(Anyway, it would still include it; just wouldn't isolate it.)

            //starts with rarest evo lines, moving to more common.
            //(If this wasn't done, could end up with a 3-length with no replacement.)
            //3-long
            translateMap.putAll(pickReplacementFamiliesOfLength(3, false, pokemonToRandomize));
            //3-long with gap (I'm not sure this ever actually comes up)
            translateMap.putAll(pickReplacementFamiliesOfLength(3, true, pokemonToRandomize));
            //2-long
            translateMap.putAll(pickReplacementFamiliesOfLength(2, false, pokemonToRandomize));
            //standalone
            translateMap.putAll(pickReplacementFamiliesOfLength(1, false, pokemonToRandomize));


            applyGlobalMap(prepped, translateMap);
        }

        /**
         * Given a set of Pokemon, for each that has a type theme, adds that theme to each evolutionary relative
         * (before randomization) in the set. <br>
         * setupAreaInfoMap() must be called before this method!
         * @param families The Pokemon to spread type themes through.
         * @throws IllegalStateException if areaInformationMap is null.
         * @throws IllegalArgumentException if families contains a Pokemon which has no
         * information in areaInformationMap.
         */
        private void spreadThemesThroughFamilies(SpeciesSet families) {
            SpeciesSet remainingFamilies = new SpeciesSet(families);
            if(areaInformationMap == null) {
                throw new IllegalStateException("Cannot spread themes before determining themes!");
            }
            for(Species poke : families) {
                if(!remainingFamilies.contains(poke)) {
                    continue;
                }
                SpeciesSet family = remainingFamilies.filterFamily(poke, true);
                remainingFamilies.removeAll(family);

                //this algorithm weights any area which contains (for example) two Pokemon in the family twice as strongly
                //this is probably acceptable
                Map<Type, Integer> familyThemeInfo = new EnumMap<>(Type.class);
                for(Species relative : family) {

                    //get this Pokemon's possible themes
                    PokemonAreaInformation info = areaInformationMap.get(relative);
                    if(info == null) {
                        throw new IllegalArgumentException("Cannot spread themes among Pokemon without theme information!");
                    }
                    Map<Type, Integer> themeInfo = info.getAllPossibleThemes();

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
                for(Species relative : family) {
                    PokemonAreaInformation info = areaInformationMap.get(relative);
                    if(info == null) {
                        //shouldn't be possible
                        throw new RuntimeException("Pokemon's info became null between checking and setting themes??");
                    }
                    info.setPossibleThemes(familyThemeInfo);
                }

            }
        }

        /**
         * Given a set of areas and a map, applies the map's replacements to each area.
         * @param encounterAreas The areas to replace Pokemon in.
         * @param translateMap The map of which Pokemon to replace each Pokemon with.
         * @throws IllegalArgumentException if the map is missing a replacement for a Pokemon in one or more
         * of the areas, or if the replacement for a Pokemon is banned in one of that Pokemon's areas.
         */
        private void applyGlobalMap(List<EncounterArea> encounterAreas, Map<Species, Species> translateMap) {
            for (EncounterArea area : encounterAreas) {
                for (Encounter enc : area) {
                    Species replacement = translateMap.get(enc.getSpecies());
                    if (replacement == null) {
                        throw new IllegalArgumentException("Map did not contain replacement for "
                                + enc.getSpecies() + "!");
                    }
                    if (area.getBannedPokemon().contains(replacement)) {
                        // This should never happen, since we already checked all the areas' banned Pokemon.
                        throw new IllegalArgumentException("Map contained a banned Pokemon " + replacement +
                                " as replacement for " + enc.getSpecies() + "!");
                    }
                    enc.setSpecies(replacement);
                    setFormeForEncounter(enc, replacement);
                }
            }
        }

        /**
         * Helper function for Global Family-To-Family.
         * Given a set of Pokemon, randomizes each family within that set of the given length.
         * @param length The length of the evolutionary families.
         * @param allowGaps Whether the evolutionary families should allow gaps.
         * @param pokemonToRandomize The set of Pokemon to randomize. Will remove each Pokemon randomized.
         */
        private Map<Species, Species> pickReplacementFamiliesOfLength(int length, boolean allowGaps,
                                                                      SpeciesSet pokemonToRandomize) {
            SpeciesSet familiesToRandomize = pokemonToRandomize.filterEvoLinesAtLeastLength(length, allowGaps, true);
            pokemonToRandomize.removeAll(familiesToRandomize);

            remainingFamilyRestricted = remaining.filterEvoLinesAtLeastLength(length, allowGaps, false);
            if (needsTypes) {
                remainingFamilyRestrictedByType = remainingFamilyRestricted.sortByType(false);
            }

            Map<Species, Species> result = new HashMap<>();

            while(!familiesToRandomize.isEmpty()) {
                //Because we're randomizing a family at a time, we can't use a shuffled list.
                //(Well, I supppose a List of SpeciesSets would work, but acquiring that is harder than just
                //choosing random Species and then pulling their families.)
                Species toRandomize = familiesToRandomize.getRandomSpecies(random);
                SpeciesSet family = familiesToRandomize.filterFamily(toRandomize, true);

                result.putAll(pickFamilyReplacement(toRandomize, family));

                familiesToRandomize.removeAll(family);
                SpeciesSet replacementFamily = result.get(toRandomize).getFamily(false);
                replacementFamily.forEach(this::removeFromRemaining);
            }

            return result;
        }

        /**
         * Given a family and a primary Pokemon, maps each member of the family to a member of a random family.
         * @param match The primary Pokemon to randomize.
         * @param family The family to randomize.
         * @return A Map of each Pokemon in the family to a replacement in a new family.
         */
        private Map<Species, Species> pickFamilyReplacement(Species match, SpeciesSet family) {

            allowedForReplacement = setupAllowedForFamily(match, family);

            Map<Species, Species> familyMap = new HashMap<>();

            Species replacement = pickGame1to1ReplacementInner(match);
            familyMap.put(match, replacement);

            //now we add the rest of the family
            for (Species relative : family) {
                if(relative == match) {
                    //we don't need to do it again
                    continue;
                }

                int relation = match.getRelation(relative, true);
                SpeciesSet replaceCandidates = replacement.getRelativesAtPositionSameBranch(relation, false);
                replaceCandidates.retainAll(allowed);
                //We could try remaining first, but there shouldn't be any individual family members
                //in allowed but not remaining. Only full families.

                replaceCandidates.removeAll(areaInformationMap.get(relative).getBannedForReplacement());
                if(replaceCandidates.isEmpty()) {
                    throw new RuntimeException("Chosen Pokemon does not have correct family. This shouldn't happen.");
                }

                familyMap.put(relative, replaceCandidates.getRandomSpecies(random));
            }
            return familyMap;
        }

        /**
         * Narrows the family-restricted pool of Species down to one that is compatible with the given Species
         * and the portion of its evolutionary family given. Uses the post-randomization evolutions
         * of the pool, and pre-randomization evolutions of the family.
         * @param match The Species to make a pool for.
         * @param family The portion of the given Species's family to consider.
         * @return A Species narrowed down as specified.
         * @throws RandomizationException if no match for the given family can be found in the allowed pool.
         */
        private SpeciesSet setupAllowedForFamily(Species match, SpeciesSet family) {
            Type theme = areaInformationMap.get(match).getTheme(keepPrimaryType);
            SpeciesSet availablePool;
            if(theme != null) {
                availablePool = remainingFamilyRestrictedByType.get(theme);
            } else {
                availablePool = remainingFamilyRestricted;
            }

            int before = family.getNumberEvoStagesBefore(match, true);
            int after = family.getNumberEvoStagesAfter(match, true);
            availablePool = availablePool.filterHasEvoStages(before, after, false);

            for(Species relative : family) {
                int relation = match.getRelation(relative, true);

                //Remove all Pokemon for which "relative" cannot be replaced by any corresponding relative
                //either because it's not in the remaining pool, or it's banned
                availablePool = availablePool.filter(p -> {
                   SpeciesSet sameRelations = p.getRelativesAtPositionSameBranch(relation, false);
                   sameRelations.retainAll(remainingFamilyRestricted);
                   sameRelations.removeAll(areaInformationMap.get(relative).getBannedForReplacement());
                   return !sameRelations.isEmpty();
                });
            }

            if(availablePool.isEmpty()) {
                //nothing's in the remaining pool, how about the full pool?
                if(theme != null) {
                    availablePool = allowedByType.get(theme);
                } else {
                    availablePool = allowed;
                }

                availablePool = availablePool.filterHasEvoStages(before, after, false);

                for(Species relative : family) {
                    int relation = match.getRelation(relative, true);

                    availablePool = availablePool.filter(p -> {
                        SpeciesSet sameRelations = p.getRelativesAtPositionSameBranch(relation, false);
                        sameRelations.retainAll(allowed);
                        sameRelations.removeAll(areaInformationMap.get(relative).getBannedForReplacement());
                        return !sameRelations.isEmpty();
                    });
                }
                //do everything the same, but with allowed instead of remaining
                //(should we extract that to a method?)

                if(availablePool.isEmpty()) {
                    //there's STILL no matches
                    //guess i'll die
                    throw new RandomizationException("No replacement found for family of " + match +"!");
                }
            }

            return availablePool;
        }

        private Species pickGame1to1Replacement(PokemonAreaInformation current) {
            Type theme = current.getTheme(keepPrimaryType);
            if(theme != null) {
                allowedForReplacement = remainingByType.get(theme);
            } else {
                allowedForReplacement = remaining;
            }
            allowedForReplacement.removeAll(current.getBannedForReplacement());

            Species replacement = pickGame1to1ReplacementInner(current.species);
            // In case it runs out of unique Pokémon, picks something already mapped to.
            // Shouldn't happen unless restrictions are really harsh, normally [#allowed Pokémon] > [#Pokémon which appear in the wild]
            if (replacement == null) {
                allowedForReplacement = theme != null ? allowedByType.get(theme) : allowed;
                allowedForReplacement.removeAll(current.getBannedForReplacement());
                replacement = pickGame1to1ReplacementInner(current.species);
            } else {
                remaining.remove(replacement);
            }
            return replacement;
        }

        private Species pickGame1to1ReplacementInner(Species species) {
            return similarStrength ?
                    allowedForReplacement.getRandomSimilarStrengthSpecies(species, true, random) :
                    allowedForReplacement.getRandomSpecies(random);
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

                SpeciesSet pokemonInArea = new SpeciesSet();
                for (Species poke : area.getSpeciesInArea()) {
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
         * Given a set of EncounterAreas, creates a map of every Species in the areas to
         * information about the areas that Species is contained in.
         * If addAllPokemonTo is not null, also adds every Species to it.
         * @param areas The list of EncounterAreas to explore.
         * @param addAllPokemonTo A SpeciesSet to add every Species found to. Can be null.
         */
        private void setupAreaInfoMap(List<EncounterArea> areas, SpeciesSet addAllPokemonTo) {

            areaInformationMap = new HashMap<>();
            for(EncounterArea area : areas) {
                Type areaTheme = pickAreaType(area);
                int areaSize = area.getSpeciesInArea().size();

                for(Species species : area.getSpeciesInArea()) {
                    PokemonAreaInformation info = areaInformationMap.get(species);
                    if(info == null) {
                        info = new PokemonAreaInformation(species);
                        areaInformationMap.put(species, info);

                        if(addAllPokemonTo != null) {
                            addAllPokemonTo.add(species);
                        }
                    }

                    info.addTypeTheme(areaTheme, areaSize);
                    info.banAll(area.getBannedPokemon());
                }
            }
        }

        /**
         * A class which stores some information about the areas a Pokemon was found in,
         * in order to allow us to use this information later.
         */
        private class PokemonAreaInformation {
            private Map<Type, Integer> possibleThemes;
            private SpeciesSet bannedForReplacement;
            private Species species;

            /**
             * Creates a new RandomizationInformation with the given data.
             * @param pk The Pokemon this RandomizationInformation is about.
             */
            PokemonAreaInformation(Species pk) {
                possibleThemes = new EnumMap<>(Type.class);
                bannedForReplacement = new SpeciesSet();
                species = pk;
            }

            /**
             * Adds all Pokemon in the given collection to the set of Pokemon banned for replacement.
             * @param banned The Collection of Pokemon to add.
             */
            public void banAll(Collection<Species> banned) {
                bannedForReplacement.addAll(banned);
            }

            /**
             * Get the list of all Pokemon banned as replacements for this Pokemon.
             * @return A new unmodifiable {@link SpeciesSet} containing the banned Pokemon.
             */
            public SpeciesSet getBannedForReplacement() {
                return SpeciesSet.unmodifiable(bannedForReplacement);
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
                        return species.getPrimaryType(true);
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
                            Type primary = species.getPrimaryType(true);
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
             * Gets the Pokemon that this PokemonAreaInformation is about.
             * @return The Pokemon.
             */
            public Species getPokemon() {
                return species;
            }
        }
    }

    private void setFormeForEncounter(Encounter enc, Species pk) {
        boolean checkCosmetics = true;
        enc.setFormeNumber(0);
        if (enc.getSpecies().getFormeNumber() > 0) {
            enc.setFormeNumber(enc.getSpecies().getFormeNumber());
            enc.setSpecies(enc.getSpecies().getBaseForme());
            checkCosmetics = false;
        }
        if (checkCosmetics && enc.getSpecies().getCosmeticForms() > 0) {
            enc.setFormeNumber(enc.getSpecies().getCosmeticFormNumber(this.random.nextInt(enc.getSpecies().getCosmeticForms())));
        } else if (!checkCosmetics && pk.getCosmeticForms() > 0) {
            enc.setFormeNumber(enc.getFormeNumber() + pk.getCosmeticFormNumber(this.random.nextInt(pk.getCosmeticForms())));
        }
        //TODO: instead of (most of) this function, have encounter store the actual forme used and call basePokemon when needed
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
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            int minCatchRate = pk.isLegendary() ? rateLegendary : rateNonLegendary;
            pk.setCatchRate(Math.max(pk.getCatchRate(), minCatchRate));
        }

    }

}
