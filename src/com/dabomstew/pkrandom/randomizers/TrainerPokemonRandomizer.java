package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.constants.AbilityIDs;
import com.dabomstew.pkromio.constants.Gen7Constants;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class TrainerPokemonRandomizer extends Randomizer {

    private Map<Type, SpeciesSet> cachedByType;
    private SpeciesSet cachedAll;
    private final SpeciesSet usedAsUnique = new SpeciesSet();

    private Map<Type, Integer> typeWeightings;
    private int totalTypeWeighting;

    private final Map<Species, Integer> placementHistory = new HashMap<>();

    private Set<Type> usedUberTypes = EnumSet.noneOf(Type.class);
    private Map<Trainer, Type> trainerTypes = new HashMap<>();

    public TrainerPokemonRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void applyTrainerLevelModifier() {
        int levelModifier = settings.getTrainersLevelModifier();

        List<Trainer> currentTrainers = romHandler.getTrainers();
        for (Trainer t : currentTrainers) {
            applyLevelModifierToTrainerPokemon(t, levelModifier);
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    public void randomizeTrainerPokes() {
        //TODO: this method direly needs a refactor to despaghettify
        boolean usePowerLevels = settings.isTrainersUsePokemonOfSimilarStrength();
        boolean weightByFrequency = settings.isTrainersMatchTypingDistribution();
        boolean useLocalPokemon = settings.isTrainersUseLocalPokemon();
        boolean noLegendaries = settings.isTrainersBlockLegendaries();
        boolean noEarlyWonderGuard = settings.isTrainersBlockEarlyWonderGuard();
        boolean isUnchanged = settings.getTrainersMod() == Settings.TrainersMod.UNCHANGED;
        boolean skipOriginalTeamMembers = false;
        boolean isTypeThemed = settings.getTrainersMod() == Settings.TrainersMod.TYPE_THEMED;
        boolean isTypeThemedEliteFourGymOnly = settings.getTrainersMod() == Settings.TrainersMod.TYPE_THEMED_ELITE4_GYMS;
        boolean keepTypeThemes = settings.getTrainersMod() == Settings.TrainersMod.KEEP_THEMED;
        boolean keepThemeOrPrimaryTypes = settings.getTrainersMod() == Settings.TrainersMod.KEEP_THEME_OR_PRIMARY;
        boolean distributionSetting = settings.getTrainersMod() == Settings.TrainersMod.DISTRIBUTED;
        boolean mainPlaythroughSetting = settings.getTrainersMod() == Settings.TrainersMod.MAINPLAYTHROUGH;
        boolean includeFormes = settings.isAllowTrainerAlternateFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean swapMegaEvos = settings.isSwapTrainerMegaEvos();
        boolean shinyChance = settings.isShinyChance();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;
        int eliteFourUniquePokemonNumber = settings.getEliteFourUniquePokemonNumber();
        boolean forceMiddleStage = settings.isTrainersForceMiddleStage();
        int forceMiddleStageLevel = settings.getTrainersForceMiddleStageLevel();
        boolean forceFullyEvolved = settings.isTrainersForceFullyEvolved();
        int forceFullyEvolvedLevel = settings.getTrainersForceFullyEvolvedLevel();
        boolean forceChallengeMode = (settings.getCurrentMiscTweaks() & MiscTweak.FORCE_CHALLENGE_MODE.getValue()) > 0;
        boolean rivalCarriesStarter = settings.isRivalCarriesStarterThroughout();
        boolean bossDiversity = settings.isDiverseTypesForBossTrainers();
        boolean importantDiversity = settings.isDiverseTypesForImportantTrainers();
        boolean regularDiversity = settings.isDiverseTypesForRegularTrainers();

        // If we get here with TrainersMod UNCHANGED, that means additional Pokemon were
        // added that are supposed to be randomized according to the following settings
        if (isUnchanged) {
            keepTypeThemes = true;
            banIrregularAltFormes = true;
            skipOriginalTeamMembers = true;
        }

        boolean hasAnyTypeTheme = isTypeThemed || isTypeThemedEliteFourGymOnly || keepTypeThemes
                || keepThemeOrPrimaryTypes;

        // Set up Pokemon pool
        cachedByType = new TreeMap<>();
        cachedAll = new SpeciesSet(rSpecService.getSpecies(noLegendaries, includeFormes, false));

        if (useLocalPokemon) {
            SpeciesSet localWithRelatives =
                    romHandler.getMainGameWildPokemonSpecies(settings.isUseTimeBasedEncounters())
                    .buildFullFamilies(false);

            cachedAll.retainAll(localWithRelatives);
        }

        SpeciesSet banned = new SpeciesSet(romHandler.getBannedFormesForTrainerPokemon());
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rSpecService.getAbilityDependentFormes();
            banned.addAll(abilityDependentFormes);
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }
        cachedAll.removeAll(banned);

        SpeciesSet wonderGuardPokemon = null;
        if(noEarlyWonderGuard) {
            wonderGuardPokemon = cachedAll.filter(pk -> pk.getAbility1() == AbilityIDs.wonderGuard
                    || pk.getAbility2() == AbilityIDs.wonderGuard
                    || pk.getAbility3() == AbilityIDs.wonderGuard);
        }

        List<Trainer> currentTrainers = romHandler.getTrainers();

        if (hasAnyTypeTheme) {
            cachedByType = cachedAll.sortByType(false);
            typeWeightings = new TreeMap<>();
            totalTypeWeighting = 0;

            Map<String, List<Trainer>> groups = getTrainerGroups(currentTrainers, isTypeThemedEliteFourGymOnly);
            Map<String, Type> themes = pickGroupTypeThemes(keepTypeThemes || keepThemeOrPrimaryTypes, groups.keySet());
            assignTypesToGroups(groups, themes);
        }

        // Randomize the order trainers are randomized in.
        // Leads to less predictable results for various modifiers.
        // Need to keep the original ordering around for saving though.
        List<Trainer> scrambledTrainers = new ArrayList<>(currentTrainers);
        Collections.shuffle(scrambledTrainers, random);

        // Elite Four Unique Pokemon related
        boolean eliteFourUniquePokemon = eliteFourUniquePokemonNumber > 0;
        SpeciesSet bannedFromUnique = new SpeciesSet();
        List<Integer> eliteFourIndices = romHandler.getEliteFourTrainers(forceChallengeMode);
        SpeciesSet eliteFourExceptions = null;
        if (eliteFourUniquePokemon) {
            // Sort Elite Four Trainers to the start of the list
            scrambledTrainers.sort((t1, t2) ->
                    Boolean.compare(eliteFourIndices.contains(currentTrainers.indexOf(t2) + 1), eliteFourIndices.contains(currentTrainers.indexOf(t1) + 1)));
            if (rivalCarriesStarter) {
                List<Species> starterList = romHandler.getStarters().subList(0, 3);
                for (Species starter : starterList) {
                    // If rival/friend carries starter, the starters cannot be set as unique
                    // (Excepting the final evolution, which is done later.)
                    bannedFromUnique.addFamily(starter, false);
                }
            }
            if (useLocalPokemon) {
                //elite four unique Pokemon are excepted from local requirement
                //and in fact, non-local species should be chosen first
                eliteFourExceptions = new SpeciesSet(rSpecService.getSpecies(noLegendaries, includeFormes, false));
                eliteFourExceptions.removeAll(banned);
                eliteFourExceptions.removeAll(cachedAll); // i.e. retains only non-local pokes
            }
        }
        //TODO: figure out what's causing that bug with Elite 4 Unique in RBY

        List<Integer> mainPlaythroughTrainers = romHandler.getMainPlaythroughTrainers();

        // Randomize Trainer Pokemon
        // The result after this is done will not be final if "Force Fully Evolved" or "Rival Carries Starter"
        // are used, as they are applied later
        for (Trainer t : scrambledTrainers) {

            //Get what type this trainer's theme should be, or null for no theme.
            Type typeForTrainer = getTypeForTrainer(t, isTypeThemed, weightByFrequency, noLegendaries,
                    includeFormes, keepTypeThemes, keepThemeOrPrimaryTypes);

            //Copy the list of trainer Pokemon so we can arrange it as we like
            //without changing the order in game
            List<TrainerPokemon> trainerPokemonList = new ArrayList<>(t.pokemon);

            //Rival/Friend starters have already been set, we don't want to change them
            boolean skipStarter = rivalCarriesStarter && t.tag != null &&
                    (t.tag.contains("RIVAL") || t.tag.contains("FRIEND"));
            // Preprocessing for the Elite Four (for Unique Pokemon only)
            boolean eliteFourTrackPokemon = eliteFourUniquePokemon && eliteFourIndices.contains(t.index);

            if (t.forceStarterPosition != -1 && skipStarter) {
                //Remove the starter
                TrainerPokemon starter = trainerPokemonList.remove(t.forceStarterPosition);
                if(eliteFourTrackPokemon) {
                    //Reverse & sort the other Pokemon
                    Collections.reverse(trainerPokemonList);
                    trainerPokemonList.sort((tp1, tp2) -> Integer.compare(tp2.getLevel(), tp1.getLevel()));
                }
                //Put starter back, in front
                trainerPokemonList.add(0, starter);

            } else if (eliteFourTrackPokemon || skipStarter) {
                // Sort Pokemon list back to front, and then put highest level Pokemon first
                // (Only while randomizing, does not affect order in game)
                Collections.reverse(trainerPokemonList);
                trainerPokemonList.sort((tp1, tp2) -> Integer.compare(tp2.getLevel(), tp1.getLevel()));
            }

            final boolean forceTypeDiverse = (t.isBoss() && bossDiversity) ||
                    (t.isImportant() && importantDiversity) ||
                    (!t.isBoss() && !t.isImportant() && regularDiversity);
            Set<Type> usedTypes = EnumSet.noneOf(Type.class);

            for (TrainerPokemon tp : trainerPokemonList) {

                boolean eliteFourSetUniquePokemon =
                        eliteFourTrackPokemon && eliteFourUniquePokemonNumber > trainerPokemonList.indexOf(tp);
                boolean swapThisMegaEvo = swapMegaEvos && tp.canMegaEvolve();

                Species oldSp = tp.getSpecies();
                if (tp.getForme() > 0) {
                    oldSp = romHandler.getAltFormeOfSpecies(oldSp, tp.getForme());
                }

                Species newSp;
                boolean forceFinalEvolution = forceFullyEvolved && tp.getLevel() >= forceFullyEvolvedLevel;
                boolean forceMiddleEvolution = !forceFinalEvolution // no need to force middle stage if Pokemon already has to fully evolve
                        && forceMiddleStage && tp.getLevel() >= forceMiddleStageLevel;
                if(skipStarter) {
                    newSp = oldSp; //We've already set this to what we want it to be
                    skipStarter = false; //We don't want to skip the rival's other Pokemon
                } else if (skipOriginalTeamMembers && !tp.isAddedTeamMember()){
                    // We do not want to randomize Pkmn that were not added to the team
                    if (forceFinalEvolution) {
                        createFullyEvolvedPokemon(tp);
                        newSp = tp.getSpecies();
                    }
                    else if (forceMiddleEvolution) {
                        createMiddleStagePokemon(tp);
                        newSp = tp.getSpecies();
                    }
                    else {
                        newSp = oldSp;
                    }
                } else {
                    SpeciesSet cacheReplacement = null;
                    boolean wgAllowed = (!noEarlyWonderGuard) || tp.getLevel() >= 20;

                    SpeciesSet bannedForReplacement = new SpeciesSet(usedAsUnique);
                    if (eliteFourSetUniquePokemon) {
                        bannedForReplacement.addAll(bannedFromUnique);
                        cacheReplacement = eliteFourExceptions;
                    }
                    if(!wgAllowed) {
                        bannedForReplacement.addAll(wonderGuardPokemon);
                    }

                    newSp = pickTrainerPokeReplacement(
                            oldSp,
                            usePowerLevels,
                            (keepThemeOrPrimaryTypes && typeForTrainer == null ? oldSp.getPrimaryType(true) : typeForTrainer),
                            distributionSetting || (mainPlaythroughSetting && mainPlaythroughTrainers.contains(t.index)),
                            swapThisMegaEvo,
                            cacheReplacement,
                            forceMiddleEvolution,
                            forceFinalEvolution,
                            usedTypes,
                            bannedForReplacement);

                    //We've chosen! Now to set it.
                    tp.setSpecies(newSp);
                    setFormeForTrainerPokemon(tp, newSp);
                    tp.setAbilitySlot(getRandomAbilitySlot(newSp));
                    tp.setResetMoves(true);
                }

                // Now, do all the bookkeeping we need for later choices

                if (distributionSetting || (mainPlaythroughSetting && mainPlaythroughTrainers.contains(t.index))) {
                    setPlacementHistory(newSp);
                }

                if (eliteFourSetUniquePokemon) {
                    usedAsUnique.add(newSp);
                }
                if (eliteFourTrackPokemon) {
                    bannedFromUnique.add(newSp);
                }

                if(forceTypeDiverse && typeForTrainer == null) {
                    usedTypes.add(newSp.getPrimaryType(false));
                    if(newSp.hasSecondaryType(false)) {
                        usedTypes.add(newSp.getSecondaryType(false));
                    }
                }

                if (swapThisMegaEvo) {
                    tp.setHeldItem(newSp
                            .getMegaEvolutionsFrom()
                            .get(random.nextInt(newSp.getMegaEvolutionsFrom().size()))
                            .getItem());
                }

                if (shinyChance) {
                    if (random.nextInt(256) == 0) {
                        tp.setIVs(tp.getIVs() | (1 << 30));
                    }
                }
            }
        }

        // Save it all up
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    private Type getTypeForTrainer(Trainer t, boolean isTypeThemed, boolean weightByFrequency, boolean noLegendaries, boolean includeFormes, boolean keepTypeThemes, boolean keepThemeOrPrimaryTypes) {
        Type typeForTrainer = trainerTypes.get(t);
        if (typeForTrainer == null && isTypeThemed) {
            typeForTrainer = pickType(weightByFrequency, noLegendaries, includeFormes);

            // Ubers: can't have the same type as each other
            if (t.tag != null && t.tag.equals("UBER")) {
                while (usedUberTypes.contains(typeForTrainer)) {
                    typeForTrainer = pickType(weightByFrequency, noLegendaries, includeFormes);
                }
                usedUberTypes.add(typeForTrainer);
            }
        }

        if ((keepTypeThemes || keepThemeOrPrimaryTypes) && typeForTrainer == null ) {
            SpeciesSet trainerPokemonSpecies = t.pokemon.stream().map(TrainerPokemon::getSpecies)
                    .collect(Collectors.toCollection(SpeciesSet::new));
            typeForTrainer = trainerPokemonSpecies.getSharedType(true);
        }
        return typeForTrainer;
    }

    /**
     * Given a list of group names, chooses types for all those groups.
     * @param useOriginalThemes Whether to use the types originally present in the game.
     * @param groupNames The list of group names.
     * @return A Map containing Types for the given groups.
     */
    private Map<String, Type> pickGroupTypeThemes(boolean useOriginalThemes, Collection<String> groupNames) {

        if(useOriginalThemes) {
            return romHandler.getGymAndEliteTypeThemes();
        } else {
            // Give a random type to each group
            // Gym & elite/champion types have to be unique; preferably also don't have types that are both, but
            // that's not always possible.
            // Also, the type we choose for the champion cannot be used by any other "uber" trainers.
            Set<Type> usedGymTypes = EnumSet.noneOf(Type.class);

            List<Type> remainingTypes = new ArrayList<>(typeService.getTypes());
            Collections.shuffle(remainingTypes, random);

            List<String> shuffledGroups = new ArrayList<>(groupNames);
            Collections.shuffle(shuffledGroups, random);
            //shuffle groups so it's not always the same few gyms that get duplicates

            List<String> post8Gyms = new ArrayList<>();

            Map<String, Type> typesForGroups = new HashMap<>();

            for (String group : groupNames) {
                if((group.startsWith("GYM1") && !group.equals("GYM1")) || group.equals("GYM9")) {
                    //a gym beyond the 8th. This might put us past the number of types in the game.
                    //So we'll delay all these, so that if there *are* duplicate types, they're in
                    //the post-game gyms. (Since only Johto has enough gyms to require duplicate types.)
                    post8Gyms.add(group);
                    continue;
                }
                if(group.startsWith("THEMED")) {
                    //this is a non-league group, and so doesn't need to have any type restrictions on it
                    typesForGroups.put(group, typeService.randomType(random));
                    continue;
                }
                if(remainingTypes.isEmpty()) {
                    throw new RandomizationException(
                            "Unexpected amount of Elite/Champions; could not assign types to all!");
                }
                Type typeForGroup = remainingTypes.remove(0);
                typesForGroups.put(group, typeForGroup);

                if (group.startsWith("GYM")) {
                    usedGymTypes.add(typeForGroup);
                }
                if (group.equals("CHAMPION")) {
                    usedUberTypes.add(typeForGroup);
                }
            }

            for (String group : post8Gyms) {
                Type typeForGroup;
                if(!remainingTypes.isEmpty()) {
                    //use the remaining types first
                    typeForGroup = remainingTypes.remove(0);
                } else {
                    do {
                        typeForGroup = typeService.randomType(random);
                    } while (usedGymTypes.contains(typeForGroup));
                }

                typesForGroups.put(group, typeForGroup);
                usedGymTypes.add(typeForGroup);
            }
            return typesForGroups;
        }
    }

    /**
     * Given a set of grouped trainers, and a set of types for those groups, assigns the corresponding type
     * to each trainer.
     * @param groups The trainers to assign types to.
     * @param groupTypes The types to assign.
     */
    private void assignTypesToGroups(Map<String, List<Trainer>> groups, Map<String, Type> groupTypes) {

        for (String group : groups.keySet()) {
            if (!groupTypes.containsKey(group)) {
                continue;
            }
            Type groupType = groupTypes.get(group);
            List<Trainer> trainersInGroup = groups.get(group);
            for (Trainer t : trainersInGroup) {
                trainerTypes.put(t, groupType);
            }
        }
    }

    /**
     * Given a list of trainers, sorts any that are tagged with a group-related tag into their respective groups.
     * @param currentTrainers The list of trainers to group.
     * @param ignoreNonLeagueGroups Whether to ignore groups that are not Gyms, Elite 4, or Champions.
     * @return A new Map with trainers sorted into groups.
     */
    private static Map<String, List<Trainer>> getTrainerGroups(List<Trainer> currentTrainers,
                                                               boolean ignoreNonLeagueGroups) {
        Map<String, List<Trainer>> groups = new TreeMap<>();

        // Construct groupings for types
        // Anything starting with GYM or ELITE or CHAMPION is a group

        for (Trainer t : currentTrainers) {
            String group = t.tag == null ? "" : t.tag;
            if (group.contains("-")) {
                group = group.substring(0, group.indexOf('-'));
            }
            if (group.startsWith("GYM") || group.startsWith("ELITE") ||
                    ((group.startsWith("CHAMPION") || group.startsWith("THEMED")) && !ignoreNonLeagueGroups)) {
                // Yep this is a group
                if (!groups.containsKey(group)) {
                    groups.put(group, new ArrayList<>());
                }
                groups.get(group).add(t);
            } else if (group.startsWith("GIO")) {
                // Giovanni has same grouping as his gym, gym 8
                if (!groups.containsKey("GYM8")) {
                    groups.put("GYM8", new ArrayList<>());
                }
                groups.get("GYM8").add(t);
            }
        }
        return groups;
    }


    private Species pickTrainerPokeReplacement(Species current, boolean usePowerLevels, Type type,
                                               boolean usePlacementHistory, boolean swapMegaEvos,
                                               SpeciesSet useInsteadOfCached,
                                               boolean noBasicPokemonWithTwoEvos, boolean finalFormOnly,
                                               Set<Type> bannedTypes, SpeciesSet bannedPokemon) {
        SpeciesSet cacheOrReplacement;
        if(useInsteadOfCached == null) {
            cacheOrReplacement = cachedAll;
        } else {
            cacheOrReplacement = useInsteadOfCached;
        }

        SpeciesSet pickFrom;
        SpeciesSet withoutBannedPokemon;

        if (swapMegaEvos) {
            pickFrom = rSpecService.getMegaEvolutions()
                    .stream()
                    .filter(MegaEvolution::isNeedsItem)
                    .map(MegaEvolution::getFrom)
                    .collect(Collectors.toCollection(SpeciesSet::new));
        } else {
            pickFrom = cacheOrReplacement;
        }

        if (type != null && cachedByType != null) {
            // "Type Themed" settings
            SpeciesSet pokemonOfType;

            if(useInsteadOfCached == null) {
                if (!cachedByType.containsKey(type)) {
                    throw new RandomizationException("No Pokemon of type " + type + " available for trainers!");
                } else {
                    pokemonOfType = cachedByType.get(type);
                }
            } else {
                //not using the cache, so don't use the cached-by-type set
                pokemonOfType = useInsteadOfCached.filterByType(type, false);
            }

            if (swapMegaEvos) {
                pickFrom = pokemonOfType.filter(pickFrom::contains);
                if (pickFrom.isEmpty()) {
                    pickFrom = pokemonOfType;
                }
            } else {
                pickFrom = pokemonOfType;
            }
        }

        if(!bannedTypes.isEmpty()) {
            pickFrom = pickFrom.filter(sp -> !bannedTypes.contains(sp.getPrimaryType(false)) &&
                    (!sp.hasSecondaryType(false) ||
                            !bannedTypes.contains(sp.getSecondaryType(false))));
        }

        if(finalFormOnly) {
            pickFrom = pickFrom.filterFinalEvos(false);
        } else if (noBasicPokemonWithTwoEvos) {
            pickFrom = pickFrom.filter(p -> !p.isBasicPokemonWithMoreThanTwoEvoStages(false));
        }

        if (usePlacementHistory) {
            // "Distributed" settings
            double placementAverage = getPlacementAverage();
            SpeciesSet belowAverage = pickFrom.filter(pk -> getPlacementHistory(pk) < placementAverage * 2);
            if (!belowAverage.isEmpty()) {
                pickFrom = belowAverage;
            }
        }

        if(pickFrom.isEmpty() && useInsteadOfCached != null) {
            //the cache replacement has no valid Pokemon
            //recurse using the cache
            return pickTrainerPokeReplacement(current, usePowerLevels, type, usePlacementHistory,
                    swapMegaEvos, null, noBasicPokemonWithTwoEvos, finalFormOnly, bannedTypes, bannedPokemon);
        }

        withoutBannedPokemon = pickFrom.filter(pk -> !bannedPokemon.contains(pk));
        if (!withoutBannedPokemon.isEmpty()) {
            pickFrom = withoutBannedPokemon;
        } else if(useInsteadOfCached != null) {
            //rather than using banned pokemon from the provided set,
            //see if we can get a non-banned pokemon from the cache
            Species cachePick = pickTrainerPokeReplacement(current, usePowerLevels, type, usePlacementHistory,
                    swapMegaEvos, null, noBasicPokemonWithTwoEvos, finalFormOnly, bannedTypes, bannedPokemon);
            if(withoutBannedPokemon.contains(cachePick)) {
                return cachePick;
            }
            //if we didn't... well, if it's banned anyway, it might as well be from the substitution set
        }

        return usePowerLevels ?
                pickFrom.getRandomSimilarStrengthSpecies(current, random) :
                pickFrom.getRandomSpecies(random);
    }

    /**
     * Picks a type, sometimes based on frequency of non-banned Pokémon of that type. Compare with randomType().
     * Never picks a type with no non-banned Pokémon, even when weightByFrequency == false.
     */
    private Type pickType(boolean weightByFrequency, boolean noLegendaries, boolean allowAltFormes) {
        if (totalTypeWeighting == 0) {
            initTypeWeightings(noLegendaries, allowAltFormes);
        }

        if (weightByFrequency) {
            int typePick = random.nextInt(totalTypeWeighting);
            int typePos = 0;
            for (Type t : typeWeightings.keySet()) {
                int weight = typeWeightings.get(t);
                if (typePos + weight > typePick) {
                    return t;
                }
                typePos += weight;
            }
            return null;
        } else {
            // assumes some type has non-banned Pokémon
            Type picked;
            do {
                picked = typeService.randomType(random);
            } while (typeWeightings.get(picked) == 0);
            return picked;
        }
    }

    /**
     * Determines the ratio of types of all usable Pokemon.
     * @param noLegendaries Whether to include legendaries.
     * @param allowAltFormes Whether to allow alt formes.
     */
    private void initTypeWeightings(boolean noLegendaries, boolean allowAltFormes) {
        // Determine weightings
        Map<Type, SpeciesSet> pokemonByType = rSpecService
                .getSpecies(noLegendaries, allowAltFormes, true).sortByType(false);
        for (Type t : typeService.getTypes()) {
            SpeciesSet pokemonOfType = pokemonByType.get(t);
            int pkWithTyping = pokemonOfType.size();
            typeWeightings.put(t, pkWithTyping);
            totalTypeWeighting += pkWithTyping;
        }
    }

    private int getRandomAbilitySlot(Species species) {
        if (romHandler.abilitiesPerSpecies() == 0) {
            return 0;
        }
        List<Integer> abilitiesList = Arrays.asList(species.getAbility1(), species.getAbility2(), species.getAbility3());
        int slot = random.nextInt(romHandler.abilitiesPerSpecies());
        while (abilitiesList.get(slot) == 0) {
            slot = random.nextInt(romHandler.abilitiesPerSpecies());
        }
        return slot + 1;
    }

    private int getValidAbilitySlotFromOriginal(Species species, int originalAbilitySlot) {
        // This is used in cases where one Trainer Pokemon evolves into another. If the unevolved Pokemon
        // is using slot 2, but the evolved Pokemon doesn't actually have a second ability, then we
        // want the evolved Pokemon to use slot 1 for safety's sake.
        if (originalAbilitySlot == 2 && species.getAbility2() == 0) {
            return 1;
        }
        return originalAbilitySlot;
    }

    private int getLevelOfStarter(List<Trainer> currentTrainers, String tag) {
        for (Trainer t : currentTrainers) {
            if (t.tag != null && t.tag.equals(tag)) {
                // Bingo, get highest level
                // last pokemon is given priority +2 but equal priority
                // = first pokemon wins, so its effectively +1
                // If it's tagged the same we can assume it's the same team
                // just the opposite gender or something like that...
                // So no need to check other trainers with same tag.
                int highestLevel = t.pokemon.get(0).getLevel();
                int trainerPkmnCount = t.pokemon.size();
                for (int i = 1; i < trainerPkmnCount; i++) {
                    int levelBonus = (i == trainerPkmnCount - 1) ? 2 : 0;
                    if (t.pokemon.get(i).getLevel() + levelBonus > highestLevel) {
                        highestLevel = t.pokemon.get(i).getLevel();
                    }
                }
                return highestLevel;
            }
        }
        return 0;
    }
    /**
     * Searches through the list of trainers given until it finds one with the given tag,
     * then assigns that trainer's strongest Pokemon the starter indicated.
     * @param currentTrainers The List of Trainers to search through.
     * @param tag The tag to find.
     * @param startersByLevel A map of levels to evolutions of the starter (including the base).
     * @param abilitySlot Which ability slot should be used for the starter.
     */
    private void changeStarterWithTag(List<Trainer> currentTrainers, String tag,
                                      NavigableMap<Integer, Species> startersByLevel, int abilitySlot) {
        for (Trainer t : currentTrainers) {
            if (t.tag != null && t.tag.equals(tag)) {

                TrainerPokemon bestPoke = t.pokemon.get(0);

                if (t.forceStarterPosition >= 0) {
                    bestPoke = t.pokemon.get(t.forceStarterPosition);
                } else {
                    // Change the highest level pokemon, not the last.
                    // BUT: last gets +2 lvl priority (effectively +1)
                    // same as above, equal priority = earlier wins
                    int trainerPkmnCount = t.pokemon.size();
                    for (int i = 1; i < trainerPkmnCount; i++) {
                        int levelBonus = (i == trainerPkmnCount - 1) ? 2 : 0;
                        if (t.pokemon.get(i).getLevel() + levelBonus > bestPoke.getLevel()) {
                            bestPoke = t.pokemon.get(i);
                        }
                    }
                }
                Species starter = startersByLevel.floorEntry(bestPoke.getLevel()).getValue();

                bestPoke.setSpecies(starter);
                setFormeForTrainerPokemon(bestPoke, starter);
                bestPoke.setResetMoves(true);
                bestPoke.setAbilitySlot(abilitySlot);
            }
        }

    }

    private Species evolveOnce(Species species) {
        if (!species.getEvolutionsFrom().isEmpty()) {
            // not already fully evolved
            List<Evolution> evolutions = species.getEvolutionsFrom();
            int evolutionIndex = random.nextInt(species.getEvolutionsFrom().size());
            species = species.getEvolutionsFrom().get(evolutionIndex).getTo();
        }
        return species;
    }

    private Species fullyEvolve(Species species) {
        Set<Species> seenMons = new HashSet<>();
        seenMons.add(species);

        while (true) {
            if (species.getEvolutionsFrom().isEmpty()) {
                // fully evolved
                break;
            }

            // check for cyclic evolutions from what we've already seen
            boolean cyclic = false;
            for (Evolution ev : species.getEvolutionsFrom()) {
                if (seenMons.contains(ev.getTo())) {
                    // cyclic evolution detected - bail now
                    cyclic = true;
                    break;
                }
            }

            if (cyclic) {
                break;
            }

            // No longer needs trainerIndex to be deterministic,
            // as this method is no longer run multiple times on the same TrainerPokemon!
            int evolutionIndex = random.nextInt(species.getEvolutionsFrom().size());
            species = species.getEvolutionsFrom().get(evolutionIndex).getTo();
            seenMons.add(species);
        }

        return species;
    }

    private void setFormeForTrainerPokemon(TrainerPokemon tp, Species sp) {
        tp.setForme(sp.getRandomCosmeticFormeNumber(random));
        tp.setSpecies(sp);
        while (!tp.getSpecies().isBaseForme()) {
            tp.setSpecies(tp.getSpecies().getBaseForme());
        }
        tp.setFormeSuffix(romHandler.getAltFormeOfSpecies(tp.getSpecies(), tp.getForme()).getFormeSuffix());
    }

    private void applyLevelModifierToTrainerPokemon(Trainer trainer, int levelModifier) {
        if (levelModifier != 0) {
            for (TrainerPokemon tp : trainer.pokemon) {
                tp.setLevel(Math.min(100, (int) Math.round(tp.getLevel() * (1 + levelModifier / 100.0))));
            }
        }
    }

    private void setPlacementHistory(Species newPK) {
        int history = getPlacementHistory(newPK);
        placementHistory.put(newPK, history + 1);
    }

    private int getPlacementHistory(Species newPK) {
        return placementHistory.getOrDefault(newPK, 0);
    }

    private double getPlacementAverage() {
        return placementHistory.values().stream().mapToInt(e -> e).average().orElse(0);
    }

    public void makeRivalCarryStarter() {
        List<Trainer> currentTrainers = romHandler.getTrainers();
        rivalCarriesStarterUpdate(currentTrainers, "RIVAL", 1);
        rivalCarriesStarterUpdate(currentTrainers, "FRIEND", 2);
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    private void rivalCarriesStarterUpdate(List<Trainer> currentTrainers, String prefix, int pokemonOffset) {
        // Find the highest rival battle #
        int highestRivalNum = 0;
        for (Trainer t : currentTrainers) {
            if (t.tag != null && t.tag.startsWith(prefix)) {
                highestRivalNum = Math.max(highestRivalNum,
                        Integer.parseInt(t.tag.substring(prefix.length(), t.tag.indexOf('-'))));
            }
        }

        if (highestRivalNum == 0) {
            // This rival type not used in this game
            return;
        }

        // Get the starters
        // us 0 1 2 => them 0+n 1+n 2+n
        List<Species> starters = romHandler.getStarters();

        // Yellow needs its own case, unfortunately.
        if (romHandler.isYellow()) {
            // The rival's starter is index 1
            Species rivalStarter = starters.get(1);

            int highestPreBranchLevel = getLevelOfStarter(currentTrainers, prefix + "3-0");

            NavigableMap<Integer, Species> startersByLevel =
                    getEvolutionsByLevel(rivalStarter, 1, highestPreBranchLevel);
            // Yellow does not have abilities
            int abilitySlot = 0;

            for (int encounter = 1; encounter <= 4; encounter++) {
                changeStarterWithTag(currentTrainers, prefix + encounter + "-0", startersByLevel, abilitySlot);
            }
            Map.Entry<Integer, Species> lastPreBranchEvolution = startersByLevel.floorEntry(highestPreBranchLevel);
            int lastEvoLevel = lastPreBranchEvolution.getKey();
            Species lastEvoSpecies = lastPreBranchEvolution.getValue();

            for (int variant = 0; variant < 3; variant++) {
                //determine further evolutions per-variant
                //so that if he's using Eevee, it has a chance to branch
                startersByLevel = getEvolutionsByLevel(lastEvoSpecies, lastEvoLevel, 100);

                for (int encounter = 5; encounter <= 8; encounter++) {
                    changeStarterWithTag(currentTrainers, prefix + encounter + "-" + variant,
                            startersByLevel, abilitySlot);
                }
            }

        } else {
            // Replace each starter as appropriate
            for (int variant = 0; variant < 3; variant++) {
                // Rival's starters are pokemonOffset over from each of ours
                int starterToUse = (variant + pokemonOffset) % 3;
                Species thisStarter = starters.get(starterToUse);

                NavigableMap<Integer, Species> startersByLevel =
                        getEvolutionsByLevel(thisStarter, 1, 100);
                //This could (rarely) result in a starter evolving before the first battle,
                //but that's better than the alternative option of crashing if the level is lowered.

                int abilitySlot = getRandomAbilitySlot(thisStarter);
                while (abilitySlot == 3) {
                    // Since starters never have hidden abilities, the rival's starter shouldn't either
                    abilitySlot = getRandomAbilitySlot(thisStarter);
                }

                for (int encounter = 0; encounter <= highestRivalNum; encounter++) {
                    changeStarterWithTag(currentTrainers, prefix + encounter + "-" + variant,
                            startersByLevel, abilitySlot);
                }
            }
        }

    }

    /**
     * Given a base Species, returns a NavigableMap containing it and its evolutions,
     * with each evolution's key being the lowest level it should appear at.
     * Used for rematches with the same trainer.
     * When reading, floorEntry(level) should be used.
     *
     * @param base         The base Species to start from.
     * @param initialLevel The level of the Pokemon in the first battle with this trainer.
     * @param maxLevel     The highest level interested in (typically the level of the Pokemon in the last
     *                     battle with the trainer, or 100 if not known).
     * @return A NavigableMap containing the Pokemon's evolutions by level.
     */
    private NavigableMap<Integer, Species> getEvolutionsByLevel(Species base, int initialLevel, int maxLevel) {
        boolean forceFullyEvolved = settings.isTrainersForceFullyEvolved();
        int fullyEvolvedLevel = settings.getTrainersForceFullyEvolvedLevel();

        NavigableMap<Integer, Species> evolutions = new TreeMap<>();
        evolutions.put(initialLevel, base);
        int currentLevel = initialLevel;
        Species currentSpecies = base;

        if(forceFullyEvolved && maxLevel < fullyEvolvedLevel) {
            maxLevel = fullyEvolvedLevel;
        }

        while(currentLevel < maxLevel && !currentSpecies.getEvolutionsFrom().isEmpty()) {
            List<Evolution> possibleEvolutions = currentSpecies.getEvolutionsFrom();
            int chosenEvoIndex = random.nextInt(possibleEvolutions.size());
            Evolution chosenEvo = possibleEvolutions.get(chosenEvoIndex);

            int level;
            if(chosenEvo.getType().usesLevel()) {
                level = chosenEvo.getExtraInfo();
                if (level <= currentLevel) {
                    level = currentLevel + 1;
                }
            } else {
                //arbitrary amount of levels later
                level = currentLevel + 20;
            }

            if(level >= maxLevel) {
                break;
            }
            currentLevel = level;
            currentSpecies = chosenEvo.getTo();
            evolutions.put(currentLevel, currentSpecies);
        }

        if(forceFullyEvolved) {
            evolutions.put(fullyEvolvedLevel, fullyEvolve(currentSpecies));
        }

        return evolutions;
    }

    public void forceMiddleStageTrainerPokes() {
        int minLevel = settings.getTrainersForceMiddleStageLevel();

        List<Trainer> currentTrainers = romHandler.getTrainers();
        for (Trainer t : currentTrainers) {
            for (TrainerPokemon tp : t.pokemon) {
                if (tp.getLevel() >= minLevel) {
                    createMiddleStagePokemon(tp);
                }
            }
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    public void createMiddleStagePokemon(TrainerPokemon tp) {
        if (tp.getSpecies().isBasicPokemonWithMoreThanTwoEvoStages(false)) {
            Species newSpecies = evolveOnce(tp.getSpecies());
            tp.setSpecies(newSpecies);
            setFormeForTrainerPokemon(tp, newSpecies);
            tp.setAbilitySlot(getValidAbilitySlotFromOriginal(newSpecies, tp.getAbilitySlot()));
        }
    }

    public void forceFullyEvolvedTrainerPokes() {
        int minLevel = settings.getTrainersForceFullyEvolvedLevel();

        List<Trainer> currentTrainers = romHandler.getTrainers();
        for (Trainer t : currentTrainers) {
            for (TrainerPokemon tp : t.pokemon) {
                if (tp.getLevel() >= minLevel) {
                    createFullyEvolvedPokemon(tp);
                }
            }
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    public void createFullyEvolvedPokemon(TrainerPokemon tp) {
        Species newSpecies = fullyEvolve(tp.getSpecies());
        if (newSpecies != tp.getSpecies()) {
            tp.setSpecies(newSpecies);
            setFormeForTrainerPokemon(tp, newSpecies);
            tp.setAbilitySlot(getValidAbilitySlotFromOriginal(newSpecies, tp.getAbilitySlot()));
        }
    }

    public void addTrainerPokemon() {
        int additionalNormal = settings.getAdditionalRegularTrainerPokemon();
        int additionalImportant = settings.getAdditionalImportantTrainerPokemon();
        int additionalBoss = settings.getAdditionalBossTrainerPokemon();

        List<Trainer> currentTrainers = romHandler.getTrainers();
        for (Trainer t : currentTrainers) {
            int additional;
            if (t.isBoss()) {
                additional = additionalBoss;
            } else if (t.isImportant()) {
                if (t.shouldNotGetBuffs()) continue;
                additional = additionalImportant;
            } else {
                additional = additionalNormal;
            }

            if (additional == 0) {
                continue;
            }

            List<TrainerPokemon> originalPokes = new ArrayList<>();
            int lowest = 100;
            int highest = 0;
            boolean duplicateHighest = false;

            // First pass: find lowest and highest level while copying the original Pokemon
            // and checking if more than one Pokemon has the highest level in the team
            for (TrainerPokemon tpk : t.pokemon) {
                int curLevel= tpk.getLevel();
                if (curLevel == highest) {
                    duplicateHighest = true; // Seen this highest level more than once
                }
                if (curLevel < lowest) {
                    lowest = curLevel;
                }
                if (curLevel > highest) {
                    highest = curLevel;
                    duplicateHighest = false; // Seen this highest level for the first time
                }
                originalPokes.add(tpk);
            }

            // If a trainer can appear in a Multi Battle (i.e., a Double Battle where the enemy consists
            // of two independent trainers), we want to be aware of that so we don't give them a team of
            // six Pokemon and have a 6v12 battle
            int maxPokemon = t.multiBattleStatus != Trainer.MultiBattleStatus.NEVER ? 3 : 6;
            int originalSize = originalPokes.size();
            // Determine max level of additional Pokemon, either
            // 1. the highest level in the original team if there is more than one Pokemon with that level
            // 2. the highest level in the original team - 1 if there is only one Pokemon of that level (keep the Ace of the trainer)
            int upperLevelBound = duplicateHighest ? highest : highest - 1;
            for (int i = 0; i < additional; i++) {
                if (t.pokemon.size() >= maxPokemon) break;

                // We want to preserve the original last Pokemon because the order is sometimes used to
                // determine the rival's starter
                int secondToLastIndex = t.pokemon.size() - 1;
                // Insert a random original Pokemon as placeholder and give it a random level
                // between the lowest and upperLevelBound
                TrainerPokemon newPokemon = originalPokes.get(random.nextInt(originalSize)).copy();
                newPokemon.setLevel(random.nextInt(Math.max(upperLevelBound, lowest) - lowest + 1) + lowest);

                // Clear out the held item because we only want one Pokemon with a mega stone if we're
                // swapping mega evolvables
                newPokemon.setHeldItem(null);
                newPokemon.setIsAddedTeamMember(true);
                t.pokemon.add(secondToLastIndex, newPokemon);
            }
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    private BattleStyle createTrainerStyle(BattleStyle style) {
        // Unchanged: passes style through
        // Randomize: Select a random Style to use
        // Single Style: passes style through, as the selected style is already picked.
        BattleStyle trainerStyle = new BattleStyle(style.getModification(), style.getStyle());
        if (trainerStyle.getModification() == BattleStyle.Modification.RANDOM) {
            int styleCount = BattleStyle.Style.values().length;
            if (romHandler.generationOfPokemon() < 5 || romHandler.generationOfPokemon() > 6)
                styleCount = 2; // Remove triple & rotation as options
            trainerStyle.setStyle(BattleStyle.Style.values()[random.nextInt(styleCount)]);
        }
        return trainerStyle;
    }

    public void modifyBattleStyle() {
        if (settings.getBattleStyle().getModification() == BattleStyle.Modification.UNCHANGED)
            return;
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            if (!(tr.multiBattleStatus == Trainer.MultiBattleStatus.ALWAYS || tr.shouldNotGetBuffs())) {
                tr.currBattleStyle = createTrainerStyle(settings.getBattleStyle());
                while (tr.pokemon.size() < tr.currBattleStyle.getRequiredPokemonCount()) {
                    tr.pokemon.add(tr.pokemon.get(0).copy());
                }
                tr.forcedDoubleBattle = true;
            }
        }
        romHandler.setTrainers(trainers);
        romHandler.makeDoubleBattleModePossible();
        changesMade = true;
    }

    public void randomizeTrainerHeldItems() {
        boolean giveToBossPokemon = settings.isRandomizeHeldItemsForBossTrainerPokemon();
        boolean giveToImportantPokemon = settings.isRandomizeHeldItemsForImportantTrainerPokemon();
        boolean giveToRegularPokemon = settings.isRandomizeHeldItemsForRegularTrainerPokemon();
        boolean highestLevelOnly = settings.isHighestLevelGetsItemsForTrainers();

        List<Move> moves = romHandler.getMoves();
        Map<Integer, List<MoveLearnt>> movesets = romHandler.getMovesLearnt();
        List<Trainer> currentTrainers = romHandler.getTrainers();
        for (Trainer t : currentTrainers) {
            if (t.shouldNotGetBuffs()) {
                continue;
            }
            if (!giveToRegularPokemon && (!t.isImportant() && !t.isBoss())) {
                continue;
            }
            if (!giveToImportantPokemon && t.isImportant()) {
                continue;
            }
            if (!giveToBossPokemon && t.isBoss()) {
                continue;
            }
            t.setPokemonHaveItems(true);
            if (highestLevelOnly) {
                int maxLevel = -1;
                TrainerPokemon highestLevelPoke = null;
                for (TrainerPokemon tp : t.pokemon) {
                    if (tp.getLevel() > maxLevel) {
                        highestLevelPoke = tp;
                        maxLevel = tp.getLevel();
                    }
                }
                if (highestLevelPoke == null) {
                    continue; // should never happen - trainer had zero pokes
                }
                int[] moveset = highestLevelPoke.isResetMoves() ?
                        RomFunctions.getMovesAtLevel(romHandler.getAltFormeOfSpecies(
                                        highestLevelPoke.getSpecies(), highestLevelPoke.getForme()).getNumber(),
                                movesets,
                                highestLevelPoke.getLevel()) :
                        highestLevelPoke.getMoves();
                randomizeHeldItem(highestLevelPoke, settings, moves, moveset);
            } else {
                for (TrainerPokemon tp : t.pokemon) {
                    int[] moveset = tp.isResetMoves() ?
                            RomFunctions.getMovesAtLevel(romHandler.getAltFormeOfSpecies(
                                            tp.getSpecies(), tp.getForme()).getNumber(),
                                    movesets,
                                    tp.getLevel()) :
                            tp.getMoves();
                    randomizeHeldItem(tp, settings, moves, moveset);
                    if (t.requiresUniqueHeldItems) {
                        while (!t.pokemonHaveUniqueHeldItems()) {
                            randomizeHeldItem(tp, settings, moves, moveset);
                        }
                    }
                }
            }
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    private void randomizeHeldItem(TrainerPokemon tp, Settings settings, List<Move> moves, int[] moveset) {
        boolean sensibleItemsOnly = settings.isSensibleItemsOnlyForTrainers();
        boolean consumableItemsOnly = settings.isConsumableItemsOnlyForTrainers();
        boolean swapMegaEvolutions = settings.isSwapTrainerMegaEvos();
        if (tp.hasZCrystal()) {
            return; // Don't overwrite existing Z Crystals.
        }
        if (tp.hasMegaStone() && swapMegaEvolutions) {
            return; // Don't overwrite mega stones if another setting handled that.
        }

        List<Item> toChooseFrom;
        if (sensibleItemsOnly) {
            toChooseFrom = romHandler.getSensibleHeldItemsFor(tp, consumableItemsOnly, moves, moveset);
        } else if (consumableItemsOnly) {
            toChooseFrom = new ArrayList<>(romHandler.getAllConsumableHeldItems());
        } else {
            toChooseFrom = new ArrayList<>(romHandler.getAllHeldItems());
        }
        tp.setHeldItem(toChooseFrom.get(random.nextInt(toChooseFrom.size())));
    }

    /**
     * Gives each Trainer Pokemon with a held (non-species-specific) Z-crystal a new random one,
     * based on the types of its moves.
     */
    public void randomUsableZCrystals() {
        List<Item> items = romHandler.getItems();
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            for (TrainerPokemon tp : tr.pokemon) {
                if (tp.getHeldItem() != null) {
                    if (Gen7Constants.heldZCrystalsByType.containsValue(tp.getHeldItem().getId())) { // TODO: better check for z crystals
                        int[] pokeMoves = tp.isResetMoves() ?
                                RomFunctions.getMovesAtLevel(
                                        romHandler.getAltFormeOfSpecies(tp.getSpecies(), tp.getForme()).getNumber(),
                                        romHandler.getMovesLearnt(), tp.getLevel()) :
                                tp.getMoves();
                        pokeMoves = Arrays.stream(pokeMoves).filter(mv -> mv != 0).toArray();
                        int chosenMove = pokeMoves[random.nextInt(pokeMoves.length)];
                        Type chosenMoveType = romHandler.getMoves().get(chosenMove).type;
                        tp.setHeldItem(items.get(Gen7Constants.heldZCrystalsByType.get(chosenMoveType)));
                    }
                }
            }
        }
        romHandler.setTrainers(trainers);
        // TODO: should this could as "changes made"?
    }
}
