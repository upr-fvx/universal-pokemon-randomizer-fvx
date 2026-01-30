package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.*;

public class WildEncounterRandomizer extends Randomizer {

    public WildEncounterRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeEncounters() {
        boolean useTimeOfDay = settings.isUseTimeBasedEncounters();
        int levelModifier = settings.isWildLevelsModified() ? settings.getWildLevelModifier() : 0;

        if(!settings.isRandomizeWildPokemon()) {
            modifyLevelsOnly(useTimeOfDay, levelModifier);
            return;
        }

        Settings.WildPokemonZoneMod mode = settings.getWildPokemonZoneMod();
        boolean splitByEncounterType = settings.isSplitWildZoneByEncounterTypes();
        boolean randomTypeThemes = settings.getWildPokemonTypeMod() == Settings.WildPokemonTypeMod.RANDOM_THEMES;
        boolean keepTypeThemes = settings.isKeepWildTypeThemes();
        boolean keepPrimaryType = settings.getWildPokemonTypeMod() == Settings.WildPokemonTypeMod.KEEP_PRIMARY;
        boolean basicPokemonOnly = settings.getWildPokemonEvolutionMod() == Settings.WildPokemonEvolutionMod.BASIC_ONLY;
        boolean sameEvoStage = settings.getWildPokemonEvolutionMod() == Settings.WildPokemonEvolutionMod.KEEP_STAGE;
        boolean keepEvolutions = settings.isKeepWildEvolutionFamilies();
        boolean catchEmAll = settings.isCatchEmAllEncounters();
        boolean similarStrength = settings.isSimilarStrengthEncounters();
        boolean noLegendaries = settings.isBlockWildLegendaries();
        boolean balanceShakingGrass = settings.isBalanceShakingGrass();
        boolean allowAltFormes = settings.isAllowWildAltFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;

        randomizeEncounters(mode, splitByEncounterType, useTimeOfDay,
                randomTypeThemes, keepTypeThemes, keepPrimaryType,
                basicPokemonOnly, sameEvoStage, keepEvolutions,
                catchEmAll, similarStrength, balanceShakingGrass,
                noLegendaries, allowAltFormes, banIrregularAltFormes,
                levelModifier, abilitiesAreRandomized);
        changesMade = true;
    }

    private void randomizeEncounters(Settings.WildPokemonZoneMod mode, boolean splitByEncounterType,
                                    boolean useTimeOfDay,
                                    boolean randomTypeThemes, boolean keepTypeThemes, boolean keepPrimaryType,
                                    boolean basicPokemonOnly, boolean sameEvoStage, boolean keepEvolutions,
                                    boolean catchEmAll, boolean similarStrength, boolean balanceShakingGrass,
                                    boolean noLegendaries, boolean allowAltFormes, boolean banIrregularAltFormes,
                                    int levelModifier, boolean abilitiesAreRandomized) {

        // get encounters
        List<EncounterArea> encounterAreas = romHandler.getEncounters(useTimeOfDay);
        List<EncounterArea> preppedAreas = prepEncounterAreas(encounterAreas);

        // setup banned + allowed
        SpeciesSet banned = getBannedForWildEncounters(banIrregularAltFormes, abilitiesAreRandomized);
        SpeciesSet allowed = new SpeciesSet(rSpecService.getSpecies(noLegendaries, allowAltFormes, false));
        allowed.removeAll(banned);

        // randomize inner
        InnerRandomizer ir = new InnerRandomizer(allowed, banned,
                randomTypeThemes, keepTypeThemes, keepPrimaryType, catchEmAll, similarStrength, balanceShakingGrass,
                basicPokemonOnly, sameEvoStage, keepEvolutions);
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

        // apply level modifier
        applyLevelModifier(levelModifier, encounterAreas);
        // set encounters
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

    private SpeciesSet getBannedForWildEncounters(boolean banIrregularAltFormes,
                                                           boolean abilitiesAreRandomized) {
        SpeciesSet banned = new SpeciesSet();
        banned.addAll(romHandler.getBannedForWildEncounters());
        banned.addAll(rSpecService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rSpecService.getAbilityDependentFormes();
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
        private final boolean balanceLowLevelEncounters;
        private final boolean basicPokemonOnly;
        private final boolean sameEvoStage;
        private final boolean keepEvolutions;

        private boolean useMapping;

        private Map<Type, SpeciesSet> allowedByType;
        private final SpeciesSet allowed;
        private final SpeciesSet banned;
        private Map<Type, SpeciesSet> remainingByType;
        private SpeciesSet remaining;

        private Map<Species, Species> zoneMap;
        private Map<Species, SpeciesAreaInformation> areaInformationMap = null;

        //ORAS's DexNav will crash if the load is higher than this value.
        final int ORAS_CRASH_THRESHOLD = 18;

        public InnerRandomizer(SpeciesSet allowed, SpeciesSet banned,
                               boolean randomTypeThemes, boolean keepTypeThemes, boolean keepPrimaryType,
                               boolean catchEmAll, boolean similarStrength, boolean balanceLowLevelEncounters,
                               boolean basicPokemonOnly, boolean sameEvoStage, boolean keepEvolutions) {

            if (randomTypeThemes && keepPrimaryType) {
                throw new IllegalArgumentException("Can't use keepPrimaryType with randomTypeThemes.");
            }
            this.randomTypeThemes = randomTypeThemes;
            this.keepTypeThemes = keepTypeThemes;
            this.keepPrimaryType = keepPrimaryType;

            if(basicPokemonOnly && sameEvoStage) {
                throw new IllegalArgumentException("Can't use basicPokemonOnly with sameEvoStage.");
            }
            this.basicPokemonOnly = basicPokemonOnly;
            this.sameEvoStage = sameEvoStage;
            this.keepEvolutions = keepEvolutions;

            this.needsTypes = keepPrimaryType || keepTypeThemes || randomTypeThemes;
            this.catchEmAll = catchEmAll;
            this.similarStrength = similarStrength;
            this.balanceLowLevelEncounters = balanceLowLevelEncounters;

            if(basicPokemonOnly && !keepEvolutions) {
                this.allowed = allowed.filterBasic(false);
            } else {
                this.allowed = allowed;
            }

            this.banned = banned;
            if (needsTypes) {
                this.allowedByType = allowed.sortByType(false, typeService.getTypes());
            }
            //any algorithm that uses mapping should use remaining, not just catch-em-all
            //easiest to just always use it
            refillRemainingSpecies();
        }

        private void refillRemainingSpecies() {
            remaining = new SpeciesSet(allowed);
            if (needsTypes) {
                remainingByType = new EnumMap<>(Type.class);
                for (Type t : typeService.getTypes()) {
                    remainingByType.put(t, new SpeciesSet(allowedByType.get(t)));
                }
            }
        }

        //This is now the one most different, algorithm-wise
        //but it has enough overlap to make sense here, anyway.
        public void randomEncounters(List<EncounterArea> encounterAreas) {
            useMapping = false;

            //ok. this is dumb, but it makes it integrate well.
            List<List<EncounterArea>> zones = new ArrayList<>();
            for(EncounterArea area : encounterAreas) {
                List<EncounterArea> zone = new ArrayList<>();
                zone.add(area);
                zones.add(zone);
            }

            randomizeZones(zones);
        }

        /**
         * Special case to approximate random encounters in ORAS, since they crash if the
         * normal algorithm is used.
         * @param encounterAreas The list of EncounterAreas to randomize.
         */
        private void randomEncountersORAS(List<EncounterArea> encounterAreas) {

            //ideally, this would treat the encounter types as zones rather than flattening them
            //however that would require ANOTHER big rewrite of this algorithm
            //TODO: convert this from flatten to group
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

            randomizeZonesORAS(maps);
        }

        public void area1to1Encounters(List<EncounterArea> encounterAreas) {
            useMapping = true;

            //ok. this is dumb, but it makes it integrate well.
            List<List<EncounterArea>> zones = new ArrayList<>();
            for(EncounterArea area : encounterAreas) {
                List<EncounterArea> zone = new ArrayList<>();
                zone.add(area);
                zones.add(zone);
            }

            randomizeZones(zones);
        }

        public void map1to1Encounters(List<EncounterArea> encounterAreas, boolean splitByEncounterType) {
            useMapping = true;
            Collection<List<EncounterArea>> zones = EncounterArea.groupAreasByMapIndex(encounterAreas).values();

            if(splitByEncounterType) {
                Collection<List<EncounterArea>> maps = zones;
                zones = new ArrayList<>();
                for(List<EncounterArea> map : maps) {
                    zones.addAll(EncounterArea.groupAreasByEncounterType(map).values());
                }
            }

            randomizeZones(zones);
        }

        public void location1to1Encounters(List<EncounterArea> encounterAreas, boolean splitByEncounterType) {
            useMapping = true;
            Collection<List<EncounterArea>> zones = EncounterArea.groupAreasByLocation(encounterAreas).values();

            if(splitByEncounterType) {
                Collection<List<EncounterArea>> maps = zones;
                zones = new ArrayList<>();
                for(List<EncounterArea> map : maps) {
                    zones.addAll(EncounterArea.groupAreasByEncounterType(map).values());
                }
            }

            randomizeZones(zones);
        }

        public void game1to1Encounters(List<EncounterArea> encounterAreas, boolean splitByEncounterType) {
            useMapping = true;

            Collection<List<EncounterArea>> zones;
            if (splitByEncounterType) {
                zones = EncounterArea.groupAreasByEncounterType(encounterAreas).values();
            } else {
                zones = new ArrayList<>();
                zones.add(encounterAreas);
            }

            randomizeZones(zones);
        }

        /**
         * Given a Collection of zones (represented by a List of EncounterAreas),
         * randomizes each zone such that type theming, 1-to-1 map, etc., are carried
         * throughout the zone.
         * @param zones The zones to randomize.
         */
        private void randomizeZones(Collection<List<EncounterArea>> zones) {

            //Shuffle zones; otherwise, large zones would tend to be randomized first.
            List<List<EncounterArea>> shuffledZones = new ArrayList<>(zones);
            Collections.shuffle(shuffledZones, random);

            for(List<EncounterArea> zone : shuffledZones) {
                Type zoneType = pickZoneType(zone);

                if(useMapping) {
                    zoneMap = new HashMap<>();
                    setupAreaInfoMap(zone);

                    if(keepEvolutions) {
                        spreadThemesThroughFamilies();
                    }
                }

                for(EncounterArea area : zone) {
                    randomizeArea(area, zoneType);
                }

                if(useMapping && !catchEmAll) {
                    //if not using mapping or catch em all, remaining will not empty in the first place.
                    refillRemainingSpecies();
                }
            }
        }

        private void randomizeArea(EncounterArea area, Type zoneType) {
            //no area-level type theme, because that could foul up other type restrictions.
            //Either it's zone-level, or determined per-Species (based on all areas in the zone).

            //removing allowedForArea: it added a lot of complexity to the algorithm, and only saved
            //a small amount of processing time in what are already the fastest-processing cases.
            for (Encounter enc : area) {
                Species current = enc.getSpecies();

                Species replacement;
                if(useMapping && zoneMap.containsKey(current)) {
                    //checking the map first lets us avoid creating a pointless allowedForReplacement set
                    replacement = zoneMap.get(current);

                } else {
                    //choose new Species
                    if(keepEvolutions && mapHasFamilyMember(current)) {
                        replacement = pickFamilyMemberReplacement(current);
                    } else {
                        SpeciesSet allowedForReplacement = setupAllowedForReplacement(current, area, zoneType);
                        replacement = pickReplacement(current, allowedForReplacement);
                    }

                    //add to map if applicable
                    if (useMapping) {
                        zoneMap.put(current, replacement);
                    }

                    //remove from remaining if applicable
                    if (useMapping || catchEmAll) {
                        removeFromRemaining(replacement);
                        //removeFromRemaining() already checks if remaining is empty, so we don't need to do that here.
                    }
                }

                enc.setSpecies(replacement);
                setFormeForEncounter(enc, replacement);
            }

            if (area.isForceMultipleSpecies()) {
                enforceMultipleSpecies(area);
            }
        }

        /**
         * Given a {@link Species} which has at least one evolutionary relative contained within the zoneMap,
         * chooses a replacement for it that is a corresponding relative of its relative's replacement.
         * @param toReplace The {@link Species} to find a replacement for.
         * @return An appropriate replacement {@link Species}.
         */
        private Species pickFamilyMemberReplacement(Species toReplace) {
            SpeciesAreaInformation info = areaInformationMap.get(toReplace);
            SpeciesSet family = info.getFamily();
            for(Species relative : family) {
                if(zoneMap.containsKey(relative)) {
                    return pickFamilyMemberReplacementInner(toReplace, relative);
                }
            }

            throw new IllegalArgumentException("Tried to pick family member replacement for non-mapped Species!");
        }

        /**
         * Given a {@link Species} and a relative of that {@link Species} which is contained in the zoneMap,
         * chooses a replacement for it that is a corresponding relative of its relative's replacement.
         * @param toReplace The {@link Species} to replace.
         * @param relative A relative of that {@link Species}, which is contained as a key in the zoneMap.
         * @return An appropriate replacement {@link Species}.
         */
        private Species pickFamilyMemberReplacementInner(Species toReplace, Species relative) {
            SpeciesAreaInformation info = areaInformationMap.get(toReplace);
            int relation = relative.getRelation(toReplace, true);
            Species relativeReplacement = zoneMap.get(relative);
            if(relativeReplacement == null) {
                throw new IllegalArgumentException("Relative had a null replacement!");
            }

            SpeciesSet possibleReplacements = relativeReplacement.getRelativesAtPosition(relation, false);
            possibleReplacements.retainAll(remaining);
            possibleReplacements.removeAll(info.getBannedForReplacement());
            if(!possibleReplacements.isEmpty()) {
                return pickReplacement(toReplace, possibleReplacements);
            }
            //else - remaining didn't have any valid, but allowed should.
            possibleReplacements = relativeReplacement.getRelativesAtPosition(relation, false);
            possibleReplacements.retainAll(allowed);
            possibleReplacements.removeAll(info.getBannedForReplacement());
            if(!possibleReplacements.isEmpty()) {
                return pickReplacement(toReplace, possibleReplacements);
            }
            //else - we messed up earlier, this Species has no replacement

            throw new IllegalStateException("Chose a family that is invalid!");
        }

        /**
         * Checks if any family member (as listed in areaInformationMap) of the given {@link Species}
         * is contained in the zoneMap.
         * @param current The {@link Species} to check.
         * @return True if any family member is present, false otherwise.
         */
        private boolean mapHasFamilyMember(Species current) {
           SpeciesAreaInformation info = areaInformationMap.get(current);
           SpeciesSet family = info.getFamily();
           for(Species relative : family) {
               if(zoneMap.containsKey(relative)) {
                   return true;
               }
           }

           return false;
        }

        /**
         * Given a {@link List} of maps (each represented by a {@link List} of {@link EncounterArea}s) randomizes them
         * with as many distinct Pokemon as possible without crashing ORAS's DexNav.
         * @param maps The list of maps to randomize.
         */
        private void randomizeZonesORAS(List<List<EncounterArea>> maps) {
            //Shuffle maps; otherwise, large maps would tend to be randomized first.
            List<List<EncounterArea>> shuffledMaps = new ArrayList<>(maps);
            Collections.shuffle(shuffledMaps, random);

            for(List<EncounterArea> map : shuffledMaps) {
                randomizeMapORAS(map);

                if(!catchEmAll) {
                    refillRemainingSpecies();
                }
            }
        }

        /**
         * Given a list of EncounterAreas, all on the same map, randomizes them with as many
         * different {@link Species} as it can without crashing.
         * @param map The map to randomize.
         */
        private void randomizeMapORAS(List<EncounterArea> map) {
            //a messy method, but less so than the previous versions

            class AreaWithData {
                EncounterArea area;
                Type areaType;
                Map<Species, Species> areaMap;
            }

            Map<Encounter, AreaWithData> encountersToAreas = new IdentityHashMap<>();
            //IdentityHashMap makes each key distinct if it has a different reference to the same value
            //This means that identical Encounters will still map to the correct areas

            for(EncounterArea area : map) {
                AreaWithData awd = new AreaWithData();
                awd.area = area;

                List<EncounterArea> dummyZone = new ArrayList<>();
                dummyZone.add(area);
                awd.areaType = pickZoneType(dummyZone);

                awd.areaMap = new HashMap<>();

                for(Encounter enc : area) {
                    encountersToAreas.put(enc, awd);
                }
            }

            List<Encounter> shuffledEncounters = new ArrayList<>(encountersToAreas.keySet());
            Collections.shuffle(shuffledEncounters, random);

            int dexNavLoad = getORASDexNavLoad(map);

            //now we're prepared to start actually randomizing
            for(Encounter enc : shuffledEncounters) {
                AreaWithData awd = encountersToAreas.get(enc);

                Species current = enc.getSpecies();
                Species replacement;

                if(!awd.areaMap.containsKey(current) || dexNavLoad < ORAS_CRASH_THRESHOLD) {
                    //get new species
                    SpeciesSet allowedForReplacement = setupAllowedForReplacement(current, awd.area, awd.areaType);

                    replacement = pickReplacement(current, allowedForReplacement);
                    removeFromRemaining(replacement);

                    //either put it in the map, or increase DexNav load
                    if(!awd.areaMap.containsKey(current)) {
                        awd.areaMap.put(current, replacement);
                    } else {
                        dexNavLoad++;
                    }
                } else {
                    replacement = awd.areaMap.get(enc.getSpecies());
                }

                enc.setSpecies(replacement);
                setFormeForEncounter(enc, replacement);
            }

        }

        /**
         * Chooses an appropriate type theme for the given zone based on the current settings:
         * If keepTypeThemes is true, chooses an existing theme if there is one.
         * If no theme was chosen, and randomTypeThemes is true, chooses a theme at random.
         * (Exception: If using catch-em-all and a banned {@link Species} was present in the zone, the
         * chosen "random" type will be one of the banned {@link Species}'s types.)
         * @param zone A List of EncounterAreas representing an appropriately-sized zone for randomization.
         * @return A Type chosen by one of the above-listed methods, or null if none was chosen.
         */
        private Type pickZoneType(List<EncounterArea> zone) {
            Type picked = null;
            if(keepTypeThemes) {
                //see if any types are shared among all areas in the zone
                Set<Type> possibleThemes = EnumSet.allOf(Type.class);
                for(EncounterArea area : zone) {
                    possibleThemes.retainAll(area.getSpeciesInArea().getSharedTypes(true));
                    if(possibleThemes.isEmpty()) {
                        break;
                    }
                }

                //if so, pick one
                if(!possibleThemes.isEmpty()) {
                    Iterator<Type> itor = possibleThemes.iterator();
                    picked = itor.next();
                    if(itor.hasNext()) {
                        if(picked == Type.NORMAL) {
                            //prefer not normal
                            picked = itor.next();
                        } else {
                            //prefer primary of first species
                            Type preferredTheme = zone.get(0).get(0).getSpecies().getPrimaryType(true);
                            if(picked != preferredTheme) {
                                picked = itor.next();
                            }
                        }
                        //both assume maximum two themes, which should be a safe assumption
                    }
                }
            }

            if(picked == null && randomTypeThemes) {
                picked = pickRandomTypeWithSpeciesRemaining();

                // Unown clause - since Unown (and other banned Species) aren't randomized with catchEmAll active,
                // the "random" type theme must be one of the banned Species's types.
                // The implementation below supports multiple banned Species of the same type in the same area,
                // because why not?
                if (catchEmAll) {
                    SpeciesSet bannedInArea = new SpeciesSet(banned);
                    SpeciesSet speciesInZone = new SpeciesSet();
                    zone.forEach(area -> speciesInZone.addAll(area.getSpeciesInArea()));
                    bannedInArea.retainAll(speciesInZone);

                    Type themeOfBanned = bannedInArea.getSharedType(false);
                    if (themeOfBanned != null) {
                        picked = themeOfBanned;
                    }
                }
            }

            return picked;
        }

        /**
         * Given an {@link EncounterArea}, returns a shared type of that area (before randomization) iff keepTypeThemes
         * is true.
         * @param area The area to examine.
         * @return A shared type if keepTypeThemes is true and such a type exists, null otherwise.
         */
        private Type findAreaType(EncounterArea area) {
            Type picked = null;
            if (keepTypeThemes) {
                picked = area.getSpeciesInArea().getSharedType(true);
            }
            return picked;
        }

        private Type pickRandomTypeWithSpeciesRemaining() {
            List<Type> types = new ArrayList<>(typeService.getTypes());
            Collections.shuffle(types, random);
            Type areaType;
            do {
                areaType = types.remove(0);
            } while (remainingByType.get(areaType).isEmpty() && !types.isEmpty());
            if(types.isEmpty() && remainingByType.get(areaType).isEmpty()) {
                throw new IllegalStateException("RemainingByType contained no Species of any valid type!");
            }
            return areaType;
        }

        /**
         * Removes all {@link Species} contained in the banned set from the given pool.
         * Safe to pass referenced {@link SpeciesSet}s to.
         * @param startingPool The pool of {@link Species} to start from.
         * @param banned The set of {@link Species} to remove.
         * @return startingPool if banned had no {@link Species}; a new {@link SpeciesSet} with the banned
         * {@link Species} removed otherwise.
         */
        private SpeciesSet removeBannedSpecies(SpeciesSet startingPool, SpeciesSet banned) {
            SpeciesSet output = startingPool;
            if(!banned.isEmpty()) {
                output = new SpeciesSet(startingPool);
                output.removeAll(banned);
            }

            return output;
        }

        /**
         * Given a {@link Species}, and some related information, finds a set of
         * valid replacements for that {@link Species}.
         * @param current The {@link Species} to find replacements for.
         * @param area The area the encounter is in. Used to determine banned {@link Species} if
         *             areaInformationMap is not populated.
         * @param zoneType A Type that all {@link Species} in the current zone should be.
         * @return A {@link SpeciesSet} containing all valid replacements for the encounter. This may be a
         * reference to another set; do not modify!
         */
        private SpeciesSet setupAllowedForReplacement(Species current, EncounterArea area, Type zoneType) {
            SpeciesSet allowedForReplacement;
            if(areaInformationMap == null) {
                allowedForReplacement = setupAllowedForReplacementNoInfoMap(current, area, zoneType);
            } else {
                allowedForReplacement = setupAllowedForReplacementUsingInfoMap(current, zoneType);
            }

            if (allowedForReplacement.isEmpty()) {
                throw new RandomizationException("Could not find a wild Species replacement for "
                        + current.getFullName() + " in area " + area.getDisplayName() + "!");
            }

            return allowedForReplacement;
        }

        /**
         * Given a {@link Species} and (optionally) a {@link Type}, finds allowed replacements for that species,
         * of the given type if there was one. <br>
         * Assumes that the info map is populated and contains an entry for the given species; throws an exception
         * otherwise. To find replacements for an unmapped Species, use setupAllowedForReplacementNoInfoMap().
         * @param current The {@link Species} to replace.
         * @param theme A {@link Type} that the allowed replacements should all be. Overrides any other type themes.
         * @return A {@link SpeciesSet} of valid replacements for the given {@link Species}. Warning: May be a reference
         * to a local variable; do not modify!
         * @throws NullPointerException if the info map was not set up.
         * @throws IllegalStateException if the info map did not contain a non-null value for the given {@link Species}.
         */
        private SpeciesSet setupAllowedForReplacementUsingInfoMap(Species current, Type theme) {
            SpeciesAreaInformation info = areaInformationMap.get(current);
            if(info == null) {
                throw new IllegalStateException("Info was null for encounter's species!");
            }

            Type typeForReplacement = (theme != null) ? theme : info.getTheme(keepPrimaryType);
            boolean needsInner = !info.bannedForReplacement.isEmpty() || keepEvolutions || sameEvoStage;
            //if none of these is true, the only restriction is the type

            SpeciesSet possiblyAllowed;
            possiblyAllowed = (typeForReplacement == null) ? remaining : remainingByType.get(typeForReplacement);
            if(needsInner) {
                possiblyAllowed = setupAllowedForReplacementInnerInfoMap(info, possiblyAllowed);
            }
            if(!possiblyAllowed.isEmpty()) {
                return possiblyAllowed;
            }
            //else - it didn't work looking at remaining. Let's try allowed.

            possiblyAllowed = (typeForReplacement == null) ? allowed : allowedByType.get(typeForReplacement);
            if(needsInner) {
                possiblyAllowed = setupAllowedForReplacementInnerInfoMap(info, possiblyAllowed);
            }
            return possiblyAllowed;
            //If it didn't work for allowed, we have no recourse; let the calling function deal with it.
        }

        /**
         * Given a {@link Species} and (optionally) a {@link Type}, finds allowed replacements for that species,
         * of the given type if there was one. <br>
         * Ignores any information present in the info map.
         * @param current The {@link Species} to replace.
         * @param area The area that this encounter was found in. Used to determine banned Species.
         * @param theme A {@link Type} that the allowed replacements should all be. If null,
         *              returns Species of all Types.
         * @return A {@link SpeciesSet} of valid replacements for the given {@link Species}. Warning: May be a reference
         * to a local variable; do not modify!
         */
        private SpeciesSet setupAllowedForReplacementNoInfoMap(Species current, EncounterArea area, Type theme) {
            Type typeForReplacement = (theme != null) ? theme :
                    (keepPrimaryType ? current.getPrimaryType(true) : null);

            SpeciesSet banned = area.getBannedSpecies();
            boolean needsInner = !banned.isEmpty() || keepEvolutions || sameEvoStage;
            //if none of these is true, the only restriction is the type

            SpeciesSet possiblyAllowed;
            possiblyAllowed = (typeForReplacement == null) ? remaining : remainingByType.get(typeForReplacement);
            if(needsInner) {
                possiblyAllowed = setupAllowedForReplacementInnerNoMap(possiblyAllowed, current, banned);
            }
            if(!possiblyAllowed.isEmpty()) {
                return possiblyAllowed;
            }
            //else - it didn't work looking at remaining. Let's try allowed.

            possiblyAllowed = (typeForReplacement == null) ? allowed : allowedByType.get(typeForReplacement);
            if(needsInner) {
                possiblyAllowed = setupAllowedForReplacementInnerNoMap(possiblyAllowed, current, banned);
            }
            return possiblyAllowed;
            //If it didn't work for allowed, we have no recourse; let the calling function deal with it.
        }

        /**
         * Given a {@link SpeciesAreaInformation} and a pool of {@link Species}, narrows the pool down to
         * {@link Species} valid as determined by the {@link SpeciesAreaInformation}.
         * Assumes all type restrictions have already been applied.
         * @param info The restrictions for the current encounter.
         * @param startingPool The pool to start from.
         * @return startingPool if no additional restrictions were applied, a new {@link SpeciesSet} with the narrowed
         * set otherwise.
         */
        private SpeciesSet setupAllowedForReplacementInnerInfoMap(SpeciesAreaInformation info, SpeciesSet startingPool) {
            SpeciesSet allowedForReplacement;
            if(!info.getBannedForReplacement().isEmpty()) {
                allowedForReplacement = new SpeciesSet(startingPool);
                allowedForReplacement.removeAll(info.getBannedForReplacement());
            } else {
                allowedForReplacement = startingPool;
            }

            if(sameEvoStage) {
                int stage = info.species.getStagesBefore(true);
                allowedForReplacement = allowedForReplacement.filter(sp ->
                        sp.getStagesBefore(false) == stage);
            }

            if(keepEvolutions) {
                allowedForReplacement = setupAllowedForFamily(allowedForReplacement, info);
            }
            return allowedForReplacement;
        }

        /**
         * Given a pool of {@link Species} and a {@link SpeciesSet} of banned Species, narrows the pool down to
         * {@link Species} valid as determined by the current settings.
         * Assumes all type restrictions have already been applied.
         * @param startingPool The pool to start from.
         * @param current The Species to replace. (Only matters when sameEvoStage is true)
         * @param banned The set of banned Species.
         * @return startingPool if no additional restrictions were applied, a new {@link SpeciesSet} with the narrowed
         * set otherwise.
         */
        private SpeciesSet setupAllowedForReplacementInnerNoMap(SpeciesSet startingPool, Species current, SpeciesSet banned) {
            SpeciesSet allowedForReplacement;
            if(!banned.isEmpty()) {
                allowedForReplacement = new SpeciesSet(startingPool);
                allowedForReplacement.removeAll(banned);
            } else {
                allowedForReplacement = startingPool;
            }

            if(sameEvoStage) {
                int stage = current.getStagesBefore(true);
                allowedForReplacement = allowedForReplacement.filter(sp ->
                        sp.getStagesBefore(false) == stage);
            }
            return allowedForReplacement;
        }

        private Species pickReplacement(Species current, SpeciesSet allowedForReplacement) {
            if (allowedForReplacement == null || allowedForReplacement.isEmpty()) {
                throw new IllegalArgumentException("No allowed Species to pick as replacement.");
            }

            Species replacement;
            // In Catch 'Em All mode, don't randomize encounters for Species that are banned for
            // wild encounters. Otherwise, it may be impossible to obtain this Species unless it
            // randomly appears as a static or unless it becomes a random evolution.
            if (catchEmAll && banned.contains(current)) {
                replacement = current;
            } else if (similarStrength) {
                if(balanceLowLevelEncounters && areaInformationMap != null) {
                    SpeciesAreaInformation info = areaInformationMap.get(current);
                    int bstToUse = Math.min(current.getBSTForPowerLevels(), info.getLowestLevel() * 10 + 250);

                    replacement = allowedForReplacement.getRandomSimilarStrengthSpecies(bstToUse, random);
                } else {
                    replacement = allowedForReplacement.getRandomSimilarStrengthSpecies(current, random);
                }
            } else {
                replacement = allowedForReplacement.getRandomSpecies(random);
            }
            return replacement;
        }

        /**
         * Removes the given {@link Species} from "remaining" and all variants that are in use.
         * If remaining is empty after removing, refills it.
         * @param replacement The {@link Species} to remove.
         */
        private void removeFromRemaining(Species replacement) {
            remaining.remove(replacement);
            if (needsTypes) {
                remainingByType.get(replacement.getPrimaryType(false)).remove(replacement);
                if (replacement.hasSecondaryType(false)) {
                    remainingByType.get(replacement.getSecondaryType(false)).remove(replacement);
                }
            }

            if(remaining.isEmpty()) {
                refillRemainingSpecies();
            }
        }

        private void enforceMultipleSpecies(EncounterArea area) {
            // If an area with forceMultipleSpecies yet has a single species,
            // just randomly pick a different species for one of the Encounters.
            // This is very unlikely to happen in practice, even with very
            // restrictive settings, so it should be okay to break logic here.
            while (area.stream().distinct().count() == 1) {
                area.get(0).setSpecies(rSpecService.randomSpecies(random));
            }
        }

        /**
         * For each {@link Species} in the areaInfoMap, for each that has a type theme, adds that theme
         * to each listed member of its family. <br>
         * setupAreaInfoMap() must be called before this method!
         * @throws IllegalStateException if areaInformationMap is null.
         * @throws IllegalArgumentException if families contains a {@link Species} which has no
         * information in areaInformationMap.
         */
        private void spreadThemesThroughFamilies() {
            SpeciesSet completedFamilies = new SpeciesSet();
            if(areaInformationMap == null) {
                throw new IllegalStateException("Cannot spread themes before determining themes!");
            }
            for(SpeciesAreaInformation info : areaInformationMap.values()) {
                if(info == null) {
                    throw new IllegalStateException("AreaInfoMap contained a null value!");
                }
                Species poke = info.getSpecies();
                if(completedFamilies.contains(poke)) {
                    continue;
                }

                SpeciesSet family = info.getFamily();
                completedFamilies.addAll(family);

                //this algorithm weights any area which contains (for example) two Species in the family twice as strongly
                //this is probably acceptable
                Map<Type, Integer> familyThemeInfo = new EnumMap<>(Type.class);
                for(Species relative : family) {

                    //get this Species's possible themes
                    SpeciesAreaInformation relativeInfo = areaInformationMap.get(relative);
                    if(relativeInfo == null) {
                        throw new IllegalArgumentException("Cannot spread themes among Species without theme information!");
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
                for(Species relative : family) {
                    SpeciesAreaInformation relativeInfo = areaInformationMap.get(relative);
                    if(relativeInfo == null) {
                        //shouldn't be possible
                        throw new RuntimeException("Species's info became null between checking and setting themes??");
                    }
                    relativeInfo.setPossibleThemes(familyThemeInfo);
                }

            }
        }

        /**
         * Narrows the given pool of {@link Species} down to one that is compatible with the family contained in the
         * given area information. Uses the full allowed pool for relatives.
         * Ignores all type restrictions.
         * @param potentiallyAllowed The set of {@link Species} to work from.
         * @param info The information of the {@link Species} to match.
         * @return A new {@link SpeciesSet} narrowed down as specified.
         * @throws RandomizationException if no match for the given family can be found in the allowed pool.
         */
        private SpeciesSet setupAllowedForFamily(SpeciesSet potentiallyAllowed, SpeciesAreaInformation info) {
            SpeciesSet family = info.getFamily();
            Species match = info.getSpecies();

            int before = family.getNumberEvoStagesBefore(match, true);
            int after = family.getNumberEvoStagesAfter(match, true);
            potentiallyAllowed = potentiallyAllowed.filter(sp -> sp.getStagesBefore(false) >= before
                    && sp.getStagesAfter(false) >= after);
            if(basicPokemonOnly) {
                potentiallyAllowed.removeIf(sp -> sp.getStagesBefore(false) > before);
            }

            for(Species relative : family) {
                int relation = match.getRelation(relative, true);

                //Remove all Species for which "relative" cannot be replaced by any corresponding relative
                //either because it's not in the allowed pool, or it's banned
                potentiallyAllowed = potentiallyAllowed.filter(p -> {
                       SpeciesSet sameRelations = p.getRelativesAtPositionSameBranch(relation, false);
                       sameRelations.retainAll(allowed);
                       sameRelations.removeAll(areaInformationMap.get(relative).getBannedForReplacement());
                       return !sameRelations.isEmpty();
                });
            }

            //Try to remove any Species which have a relative that has already been used
            SpeciesSet withoutUsedFamilies = potentiallyAllowed.filter(p ->
                    !p.getFamily(false).containsAny(zoneMap.values()));

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
            //the load is equal to the number of distinct Species in each area summed.
            //(Not the total number of unique Species).
            //I am not going to attempt to verify this (yet).
            int load = 0;
            for(EncounterArea area : areasInMap) {
                if(area.getEncounterType() == EncounterType.INTERACT) {
                    //Rock Smash doesn't contribute to DexNav load.
                    continue;
                }

                SpeciesSet speciesInArea = new SpeciesSet();
                for (Species poke : area.getSpeciesInArea()) {
                    //Different formes of the same species do not contribute to load
                    speciesInArea.add(poke.getBaseForme());
                }

                load += speciesInArea.size();
            }
            return load;
        }

        /**
         * Given a set of EncounterAreas, creates a map of every {@link Species} in the areas to
         * information about the areas that {@link Species} is contained in.
         * @param areas The list of EncounterAreas to explore.
         */
        private void setupAreaInfoMap(List<EncounterArea> areas) {

            SpeciesSet existingSpecies = new SpeciesSet();

            areaInformationMap = new HashMap<>();
            for(EncounterArea area : areas) {
                Type areaTheme = findAreaType(area);
                int areaSize = area.getSpeciesInArea().size();

                for(Species species : area.getSpeciesInArea()) {
                    SpeciesAreaInformation info = areaInformationMap.get(species);

                    if(info == null) {
                        info = new SpeciesAreaInformation(species);
                        areaInformationMap.put(species, info);

                        if(keepEvolutions) {
                            existingSpecies.add(species);
                            SpeciesSet family = existingSpecies.filterFamily(species, true);
                            if(family.size() > 1) {
                                family.forEach(relative -> areaInformationMap.get(relative).addFamily(family));
                            }
                        }
                    }

                    info.addTypeTheme(areaTheme, areaSize);
                    info.banAll(area.getBannedSpecies());
                }
                if(balanceLowLevelEncounters) {
                    //TODO: either verify that this IS a shaking grass encounter,
                    // or rename the setting.
                    // (Leaning towards the latter.)
                    for (Encounter enc : area) {
                        SpeciesAreaInformation info = areaInformationMap.get(enc.getSpecies());
                        info.setLevelIfLower((enc.getLevel() + enc.getMaxLevel()) / 2);
                        //TODO: *Should* this be average level? Or should it be lowest?
                    }
                }
            }
        }

        /**
         * A class which stores some information about the areas and encounters a {@link Species} was found in,
         * in order to allow us to use this information later.
         */
        private class SpeciesAreaInformation {
            private Map<Type, Integer> possibleThemes = new EnumMap<>(Type.class);
            private final SpeciesSet bannedForReplacement = new SpeciesSet();
            private final SpeciesSet family = new SpeciesSet();
            private final Species species;
            private int lowestLevel = 100;

            /**
             * Creates a new RandomizationInformation with the given data.
             * @param sp The {@link Species} this RandomizationInformation is about.
             */
            SpeciesAreaInformation(Species sp) {
                species = sp;
            }

            /**
             * Adds all {@link Species} in the given collection to the set of {@link Species} banned for replacement.
             * @param banned The Collection of {@link Species} to add.
             */
            public void banAll(Collection<Species> banned) {
                bannedForReplacement.addAll(banned);
            }

            /**
             * Get the list of all {@link Species} banned as replacements for this {@link Species}.
             * @return A new unmodifiable {@link SpeciesSet} containing the banned {@link Species}.
             */
            public SpeciesSet getBannedForReplacement() {
                return SpeciesSet.unmodifiable(bannedForReplacement);
            }

            /**
             * Adds the given type and count of {@link Species} to the list of existing themes for
             * this {@link Species}. <br>
             * If theme is null, has no effect.
             * @param theme The type to add.
             * @throws IllegalArgumentException if count is less than 1.
             */
            public void addTypeTheme(Type theme, int count) {
                if (count < 1) {
                    throw new IllegalArgumentException("Number of Species in theme cannot be less than 1!");
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
             * Gets the type of this {@link Species}'s area theming. <br>
             * If there are two or more themes, returns the one with the highest count of {@link Species}. If tied,
             * will choose the {@link Species}'s original primary type. If neither theme is the original primary,
             * chooses one arbitrarily.<br>
             * If there are no themes, it will default to the original primary only if defaultToPrimary is true;
             * otherwise, it will default to null.
             * @param defaultToPrimary Whether the type should default to the {@link Species}'s primary type
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
             * Returns the set of all desired themes for this {@link Species}.
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
             * Adds the {@link Species} in the given set to this {@link Species}'s family.
             * @param family The {@link Species} to add.
             */
            void addFamily(SpeciesSet family) {
                this.family.addAll(family);
            }

            /**
             * Gets any members of the {@link Species}'s family that have been added to the information.
             * @return A new unmodifiable {@link SpeciesSet} containing the {@link Species}'s family.
             */
            SpeciesSet getFamily() {
                return SpeciesSet.unmodifiable(family);
            }

            /**
             * Gets the {@link Species} that this SpeciesAreaInformation is about.
             * @return The {@link Species}.
             */
            public Species getSpecies() {
                return species;
            }

            /**
             * Sets the lowest level to the level given, if it is lower than the current lowest level.
             * @param level The level to lower to.
             */
            void setLevelIfLower(int level) {
                lowestLevel = Math.min(level, lowestLevel);
            }

            /**
             * Gets the lowest level encounter with this Species in the zone.
             * @return The lowest level.
             */
            public int getLowestLevel() {
                return lowestLevel;
            }
        }
    }

    /**
     * Prepares the EncounterAreas for randomization by copying them, removing unused areas, and shuffling the order.
     * @param originalAreas The List of EncounterAreas to prepare.
     * @return A new List of all the same Encounters, with the areas shuffled and possibly merged as appropriate.
     */
    private List<EncounterArea> prepEncounterAreas(List<EncounterArea> originalAreas) {
        // Clone the original set, so that we don't mess up saving
        List<EncounterArea> prepped = new ArrayList<>(originalAreas);

        prepped.removeIf(area -> area.getEncounterType() == EncounterType.UNUSED
                || "UNUSED".equals(area.getLocationTag()));
        //don't randomize unused areas
        //mostly important for catch 'em all

        // Shuffling the EncounterAreas leads to less predictable results for various modifiers.
        Collections.shuffle(prepped, random);
        return prepped;
    }

    private void setFormeForEncounter(Encounter enc, Species sp) {
        enc.setFormeNumber(enc.getSpecies().getRandomCosmeticFormeNumber(random));
        while(!enc.getSpecies().isBaseForme()) {
            enc.setSpecies(enc.getSpecies().getBaseForme());
        }
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
        for (Species sp : romHandler.getSpeciesSetInclFormes()) {
            int minCatchRate = sp.isLegendary() ? rateLegendary : rateNonLegendary;
            sp.setCatchRate(Math.max(sp.getCatchRate(), minCatchRate));
        }

    }

}
