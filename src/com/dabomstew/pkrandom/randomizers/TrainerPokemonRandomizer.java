package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.RomFunctions;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.AbilityIDs;
import com.dabomstew.pkrandom.constants.Gen7Constants;
import com.dabomstew.pkrandom.constants.GlobalConstants;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.services.TypeService;

import java.util.*;
import java.util.stream.Collectors;

public class TrainerPokemonRandomizer extends Randomizer {

    private Map<Type, SpeciesSet> cachedByType;
    private SpeciesSet cachedAll;
    private SpeciesSet banned = new SpeciesSet();
    private final SpeciesSet usedAsUnique = new SpeciesSet();

    private Map<Type, Integer> typeWeightings;
    private int totalTypeWeighting;

    private final Map<Species, Integer> placementHistory = new HashMap<>();

    private int fullyEvolvedRandomSeed = -1;
    private Set<Type> usedUberTypes = EnumSet.noneOf(Type.class);
    private Map<Trainer, Type> trainerTypes = new HashMap<>();

    public TrainerPokemonRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void onlyChangeTrainerLevels() {
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
        int levelModifier = settings.isTrainersLevelModified() ? settings.getTrainersLevelModifier() : 0;
        boolean isTypeThemed = settings.getTrainersMod() == Settings.TrainersMod.TYPE_THEMED;
        boolean isTypeThemedEliteFourGymOnly = settings.getTrainersMod() == Settings.TrainersMod.TYPE_THEMED_ELITE4_GYMS;
        boolean keepTypeThemes = settings.getTrainersMod() == Settings.TrainersMod.KEEP_THEMED;
        boolean keepThemeOrPrimaryTypes = settings.getTrainersMod() == Settings.TrainersMod.KEEP_THEME_OR_PRIMARY;
        boolean hasAnyTypeTheme = isTypeThemed || isTypeThemedEliteFourGymOnly || keepTypeThemes
                || keepThemeOrPrimaryTypes;
        boolean distributionSetting = settings.getTrainersMod() == Settings.TrainersMod.DISTRIBUTED;
        boolean mainPlaythroughSetting = settings.getTrainersMod() == Settings.TrainersMod.MAINPLAYTHROUGH;
        boolean includeFormes = settings.isAllowTrainerAlternateFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean swapMegaEvos = settings.isSwapTrainerMegaEvos();
        boolean shinyChance = settings.isShinyChance();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;
        int eliteFourUniquePokemonNumber = settings.getEliteFourUniquePokemonNumber();
        boolean forceFullyEvolved = settings.isTrainersForceFullyEvolved();
        int forceFullyEvolvedLevel = settings.getTrainersForceFullyEvolvedLevel();
        boolean forceChallengeMode = (settings.getCurrentMiscTweaks() & MiscTweak.FORCE_CHALLENGE_MODE.getValue()) > 0;
        boolean rivalCarriesStarter = settings.isRivalCarriesStarterThroughout();

        // Set up Pokemon pool
        cachedByType = new TreeMap<>();
        cachedAll = new SpeciesSet(rSpecService.getSpecies(noLegendaries, includeFormes, false));

        if (useLocalPokemon) {
            SpeciesSet localWithRelatives =
                    romHandler.getMainGameWildPokemonSpecies(settings.isUseTimeBasedEncounters())
                    .buildFullFamilies(false);

            cachedAll.retainAll(localWithRelatives);
        }

        banned = romHandler.getBannedFormesForTrainerPokemon();
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
        SpeciesSet illegalIfEvolved = new SpeciesSet();
        SpeciesSet bannedFromUnique = new SpeciesSet();
        boolean illegalEvoChains = false;
        List<Integer> eliteFourIndices = romHandler.getEliteFourTrainers(forceChallengeMode);
        SpeciesSet eliteFourExceptions = null;
        if (eliteFourUniquePokemon) {
            // Sort Elite Four Trainers to the start of the list
            scrambledTrainers.sort((t1, t2) ->
                    Boolean.compare(eliteFourIndices.contains(currentTrainers.indexOf(t2) + 1), eliteFourIndices.contains(currentTrainers.indexOf(t1) + 1)));
            illegalEvoChains = forceFullyEvolved;
            if (rivalCarriesStarter) {
                List<Species> starterList = romHandler.getStarters().subList(0, 3);
                for (Species starter : starterList) {
                    // If rival/friend carries starter, the starters cannot be set as unique
                    bannedFromUnique.add(starter);
                    setEvoChainAsIllegal(starter, bannedFromUnique, true);

                    // If the final boss is a rival/friend, the fully evolved starters will be unique
                    if (romHandler.hasRivalFinalBattle()) {
                        usedAsUnique.addAll(getFinalEvos(starter));
                        if (illegalEvoChains) {
                            illegalIfEvolved.add(starter);
                            setEvoChainAsIllegal(starter, illegalIfEvolved, true);
                        }
                    }
                }
            }
            if (useLocalPokemon) {
                //elite four unique pokemon are excepted from local requirement
                //and in fact, non-local pokemon should be chosen first
                eliteFourExceptions = new SpeciesSet(rSpecService.getSpecies(noLegendaries, includeFormes, false));
                eliteFourExceptions.removeAll(banned);
                eliteFourExceptions.removeAll(cachedAll); // i.e. retains only non-local pokes
            }
        }
        //TODO: choose all Elite 4 Unique Pokemon before other Elite 4 Pokemon
        //Wait, hold on... that's already supposed to be covered? ...

        List<Integer> mainPlaythroughTrainers = romHandler.getMainPlaythroughTrainers();

        // Randomize Trainer Pokemon
        // The result after this is done will not be final if "Force Fully Evolved" or "Rival Carries Starter"
        // are used, as they are applied later
        for (Trainer t : scrambledTrainers) {
            applyLevelModifierToTrainerPokemon(t, levelModifier);
            if (t.tag != null && t.tag.equals("IRIVAL")) {
                // This is the first rival in Yellow. His Pokemon is used to determine the non-player
                // starter, so we can't change it here. Just skip it.
                continue;
            }

            // If type themed, give a type to each unassigned trainer
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
                SpeciesSet trainerPokemonSpecies = t.pokemon.stream().map(tp -> tp.species)
                        .collect(Collectors.toCollection(SpeciesSet::new));
                typeForTrainer = trainerPokemonSpecies.getSharedType(true);
            }

            SpeciesSet evolvesIntoTheWrongType = null;
            if (typeForTrainer != null) {
                SpeciesSet pokemonOfType = cachedAll.filterByType(typeForTrainer, false);
                evolvesIntoTheWrongType = pokemonOfType.filter(pk ->
                        !pokemonOfType.contains(fullyEvolve(pk, t.index)));
            }

            List<TrainerPokemon> trainerPokemonList = new ArrayList<>(t.pokemon);

            // Elite Four Unique Pokemon related
            boolean eliteFourTrackPokemon = false;
            boolean eliteFourRival = false;
            if (eliteFourUniquePokemon && eliteFourIndices.contains(t.index)) {
                eliteFourTrackPokemon = true;

                // Sort Pokemon list back to front, and then put highest level Pokemon first
                // (Only while randomizing, does not affect order in game)
                Collections.reverse(trainerPokemonList);
                trainerPokemonList.sort((tp1, tp2) -> Integer.compare(tp2.level, tp1.level));
                if (rivalCarriesStarter && (t.tag.contains("RIVAL") || t.tag.contains("FRIEND"))) {
                    eliteFourRival = true;
                }
            }

            for (TrainerPokemon tp : trainerPokemonList) {
                boolean swapThisMegaEvo = swapMegaEvos && tp.canMegaEvolve();
                boolean wgAllowed = (!noEarlyWonderGuard) || tp.level >= 20;
                boolean eliteFourSetUniquePokemon =
                        eliteFourTrackPokemon && eliteFourUniquePokemonNumber > trainerPokemonList.indexOf(tp);
                boolean willForceEvolve = forceFullyEvolved && tp.level >= forceFullyEvolvedLevel;
                SpeciesSet cacheReplacement = null;

                Species oldPK = tp.species;
                if (tp.forme > 0) {
                    oldPK = romHandler.getAltFormeOfSpecies(oldPK, tp.forme);
                }

                banned = new SpeciesSet(usedAsUnique);
                if (illegalEvoChains && willForceEvolve) {
                    banned.addAll(illegalIfEvolved);
                }
                if (eliteFourSetUniquePokemon) {
                    banned.addAll(bannedFromUnique);
                    cacheReplacement = eliteFourExceptions;
                }
                if (willForceEvolve) {
                    if (keepThemeOrPrimaryTypes && typeForTrainer == null) {
                        SpeciesSet pokemonOfType =
                                cachedByType.get(oldPK.getPrimaryType(true));
                        evolvesIntoTheWrongType = pokemonOfType.filter(pk ->
                                !pokemonOfType.contains(fullyEvolve(pk, t.index)));
                    }
                    if(evolvesIntoTheWrongType != null) {
                        banned.addAll(evolvesIntoTheWrongType);
                    }
                }
                if(!wgAllowed) {
                    banned.addAll(wonderGuardPokemon);
                }

                Species newPK = pickTrainerPokeReplacement(
                        oldPK,
                        usePowerLevels,
                        (keepThemeOrPrimaryTypes && typeForTrainer == null ? oldPK.getPrimaryType(true) : typeForTrainer),
                        distributionSetting || (mainPlaythroughSetting && mainPlaythroughTrainers.contains(t.index)),
                        swapThisMegaEvo,
                        cacheReplacement
                );

                // Chosen Pokemon is locked in past here
                if (distributionSetting || (mainPlaythroughSetting && mainPlaythroughTrainers.contains(t.index))) {
                    setPlacementHistory(newPK);
                }
                tp.species = newPK;
                setFormeForTrainerPokemon(tp, newPK);
                tp.abilitySlot = getRandomAbilitySlot(newPK);
                tp.resetMoves = true;

                if (!eliteFourRival) {
                    if (eliteFourSetUniquePokemon) {
                        SpeciesSet actualPKList;
                        if (willForceEvolve) {
                            actualPKList = getFinalEvos(newPK);
                        } else {
                            actualPKList = new SpeciesSet();
                            actualPKList.add(newPK);
                        }
                        // If the unique Pokemon will evolve, we have to set all its potential evolutions as unique
                        for (Species actualPK : actualPKList) {
                            usedAsUnique.add(actualPK);
                            if (illegalEvoChains) {
                                setEvoChainAsIllegal(actualPK, illegalIfEvolved, willForceEvolve);
                            }
                        }
                    }
                    if (eliteFourTrackPokemon) {
                        bannedFromUnique.add(newPK);
                        if (illegalEvoChains) {
                            setEvoChainAsIllegal(newPK, bannedFromUnique, willForceEvolve);
                        }
                    }
                } else {
                    // If the champion is a rival, the first Pokemon will be skipped - it's already
                    // set as unique since it's a starter
                    eliteFourRival = false;
                }

                if (swapThisMegaEvo) {
                    tp.heldItem = newPK
                            .getMegaEvolutionsFrom()
                            .get(random.nextInt(newPK.getMegaEvolutionsFrom().size()))
                            .argument;
                }

                if (shinyChance) {
                    if (random.nextInt(256) == 0) {
                        tp.IVs |= (1 << 30);
                    }
                }
            }
        }

        // Save it all up
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
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
            if (t.tag != null && t.tag.equals("IRIVAL")) {
                // This is the first rival in Yellow. His Pokemon is used to determine the non-player
                // starter, so we can't change it here. Just skip it.
                continue;
            }
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
                                               SpeciesSet useInsteadOfCached) {
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
                    .filter(mega -> mega.method == 1)
                    .map(mega -> mega.from)
                    .collect(Collectors.toCollection(SpeciesSet::new));
        } else {
            pickFrom = cacheOrReplacement;
        }

        if (usePlacementHistory) {
            // "Distributed" settings
            double placementAverage = getPlacementAverage();
            SpeciesSet belowAverage = pickFrom.filter(pk -> getPlacementHistory(pk) < placementAverage * 2);
            if (!belowAverage.isEmpty()) {
                pickFrom = belowAverage;
            }
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

        if(pickFrom.isEmpty() && useInsteadOfCached != null) {
            //the cache replacement has no valid Pokemon
            //recurse using the cache
            return pickTrainerPokeReplacement(current, usePowerLevels, type,
                    usePlacementHistory, swapMegaEvos, null);
        }

        withoutBannedPokemon = pickFrom.filter(pk -> !banned.contains(pk));
        if (!withoutBannedPokemon.isEmpty()) {
            pickFrom = withoutBannedPokemon;
        } else if(useInsteadOfCached != null) {
            //rather than using banned pokemon from the provided set,
            //see if we can get a non-banned pokemon from the cache
            Species cachePick = pickTrainerPokeReplacement(current, usePowerLevels, type,
                    usePlacementHistory, swapMegaEvos, null);
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

    private Species pickRandomEvolutionOf(Species base, boolean mustEvolveItself) {
        // Used for "rival carries starter"
        // Pick a random evolution of base Pokemon, subject to
        // "must evolve itself" if appropriate.
        SpeciesSet candidates = new SpeciesSet();
        for (Evolution ev : base.getEvolutionsFrom()) {
            if (!mustEvolveItself || ev.getTo().getEvolutionsFrom().size() > 0) {
                candidates.add(ev.getTo());
            }
        }

        if (candidates.size() == 0) {
            throw new RandomizationException("Random evolution called on a Pokemon without any usable evolutions.");
        }

        return candidates.getRandomSpecies(random);
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
                int highestLevel = t.pokemon.get(0).level;
                int trainerPkmnCount = t.pokemon.size();
                for (int i = 1; i < trainerPkmnCount; i++) {
                    int levelBonus = (i == trainerPkmnCount - 1) ? 2 : 0;
                    if (t.pokemon.get(i).level + levelBonus > highestLevel) {
                        highestLevel = t.pokemon.get(i).level;
                    }
                }
                return highestLevel;
            }
        }
        return 0;
    }

    private void changeStarterWithTag(List<Trainer> currentTrainers, String tag, Species starter, int abilitySlot) {
        for (Trainer t : currentTrainers) {
            if (t.tag != null && t.tag.equals(tag)) {

                // Bingo
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
                        if (t.pokemon.get(i).level + levelBonus > bestPoke.level) {
                            bestPoke = t.pokemon.get(i);
                        }
                    }
                }
                bestPoke.species = starter;
                setFormeForTrainerPokemon(bestPoke, starter);
                bestPoke.resetMoves = true;
                bestPoke.abilitySlot = abilitySlot;
            }
        }

    }

    private int numEvolutions(Species pk, int maxInterested) {
        return numEvolutions(pk, 0, maxInterested);
    }

    private int numEvolutions(Species pk, int depth, int maxInterested) {
        if (pk.getEvolutionsFrom().size() == 0) {
            return 0;
        } else {
            if (depth == maxInterested - 1) {
                return 1;
            } else {
                int maxEvos = 0;
                for (Evolution ev : pk.getEvolutionsFrom()) {
                    maxEvos = Math.max(maxEvos, numEvolutions(ev.getTo(), depth + 1, maxInterested) + 1);
                }
                return maxEvos;
            }
        }
    }

    private Species fullyEvolve(Species species, int trainerIndex) {
        // If the fullyEvolvedRandomSeed hasn't been set yet, set it here.
        if (this.fullyEvolvedRandomSeed == -1) {
            this.fullyEvolvedRandomSeed = random.nextInt(GlobalConstants.LARGEST_NUMBER_OF_SPLIT_EVOS);
        }

        Set<Species> seenMons = new HashSet<>();
        seenMons.add(species);

        while (true) {
            if (species.getEvolutionsFrom().size() == 0) {
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

            // We want to make split evolutions deterministic, but still random on a seed-to-seed basis.
            // Therefore, we take a random value (which is generated once per seed) and add it to the trainer's
            // index to get a pseudorandom number that can be used to decide which split to take.
            int evolutionIndex = (this.fullyEvolvedRandomSeed + trainerIndex) % species.getEvolutionsFrom().size();
            species = species.getEvolutionsFrom().get(evolutionIndex).getTo();
            seenMons.add(species);
        }

        return species;
    }

    private void setEvoChainAsIllegal(Species newPK, SpeciesSet illegalList, boolean willForceEvolve) {
        // set pre-evos as illegal
        setIllegalPreEvos(newPK, illegalList);

        // if the placed Pokemon will be forced fully evolved, set its evolutions as illegal
        if (willForceEvolve) {
            setIllegalEvos(newPK, illegalList);
        }
    }

    private void setIllegalPreEvos(Species pk, SpeciesSet illegalList) {
        for (Evolution evo : pk.getEvolutionsTo()) {
            pk = evo.getFrom();
            illegalList.add(pk);
            setIllegalPreEvos(pk, illegalList);
        }
    }

    private void setIllegalEvos(Species pk, SpeciesSet illegalList) {
        for (Evolution evo : pk.getEvolutionsFrom()) {
            pk = evo.getTo();
            illegalList.add(pk);
            setIllegalEvos(pk, illegalList);
        }
    }

    private SpeciesSet getFinalEvos(Species pk) {
        SpeciesSet finalEvos = new SpeciesSet();
        traverseEvolutions(pk, finalEvos);
        return finalEvos;
    }

    private void traverseEvolutions(Species pk, SpeciesSet finalEvos) {
        if (!pk.getEvolutionsFrom().isEmpty()) {
            for (Evolution evo : pk.getEvolutionsFrom()) {
                pk = evo.getTo();
                traverseEvolutions(pk, finalEvos);
            }
        } else {
            finalEvos.add(pk);
        }
    }

    private void setFormeForTrainerPokemon(TrainerPokemon tp, Species sp) {
        tp.forme = sp.getRandomCosmeticFormeNumber(random);
        tp.species = sp;
        while(!tp.species.isBaseForme()) {
            tp.species = tp.species.getBaseForme();
        }
        tp.formeSuffix = romHandler.getAltFormeOfSpecies(tp.species, tp.forme).getFormeSuffix();
    }

    private void applyLevelModifierToTrainerPokemon(Trainer trainer, int levelModifier) {
        if (levelModifier != 0) {
            for (TrainerPokemon tp : trainer.pokemon) {
                tp.level = Math.min(100, (int) Math.round(tp.level * (1 + levelModifier / 100.0)));
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
        rivalCarriesStarterUpdate(currentTrainers, "RIVAL", romHandler.isORAS() ? 0 : 1);
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
            int timesEvolves = numEvolutions(rivalStarter, 2);
            // Yellow does not have abilities
            int abilitySlot = 0;
            // Apply evolutions as appropriate
            if (timesEvolves == 0) {
                for (int j = 1; j <= 3; j++) {
                    changeStarterWithTag(currentTrainers, prefix + j + "-0", rivalStarter, abilitySlot);
                }
                for (int j = 4; j <= 7; j++) {
                    for (int i = 0; i < 3; i++) {
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, rivalStarter, abilitySlot);
                    }
                }
            } else if (timesEvolves == 1) {
                for (int j = 1; j <= 3; j++) {
                    changeStarterWithTag(currentTrainers, prefix + j + "-0", rivalStarter, abilitySlot);
                }
                rivalStarter = pickRandomEvolutionOf(rivalStarter, false);
                for (int j = 4; j <= 7; j++) {
                    for (int i = 0; i < 3; i++) {
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, rivalStarter, abilitySlot);
                    }
                }
            } else if (timesEvolves == 2) {
                for (int j = 1; j <= 2; j++) {
                    changeStarterWithTag(currentTrainers, prefix + j + "-" + 0, rivalStarter, abilitySlot);
                }
                rivalStarter = pickRandomEvolutionOf(rivalStarter, true);
                changeStarterWithTag(currentTrainers, prefix + "3-0", rivalStarter, abilitySlot);
                for (int i = 0; i < 3; i++) {
                    changeStarterWithTag(currentTrainers, prefix + "4-" + i, rivalStarter, abilitySlot);
                }
                rivalStarter = pickRandomEvolutionOf(rivalStarter, false);
                for (int j = 5; j <= 7; j++) {
                    for (int i = 0; i < 3; i++) {
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, rivalStarter, abilitySlot);
                    }
                }
            }
        } else {
            // Replace each starter as appropriate
            // Use level to determine when to evolve, not number anymore
            for (int i = 0; i < 3; i++) {
                // Rival's starters are pokemonOffset over from each of ours
                int starterToUse = (i + pokemonOffset) % 3;
                Species thisStarter = starters.get(starterToUse);
                int timesEvolves = numEvolutions(thisStarter, 2);
                int abilitySlot = getRandomAbilitySlot(thisStarter);
                while (abilitySlot == 3) {
                    // Since starters never have hidden abilities, the rival's starter shouldn't either
                    abilitySlot = getRandomAbilitySlot(thisStarter);
                }
                // If a fully evolved pokemon, use throughout
                // Otherwise split by evolutions as appropriate
                if (timesEvolves == 0) {
                    for (int j = 1; j <= highestRivalNum; j++) {
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, thisStarter, abilitySlot);
                    }
                } else if (timesEvolves == 1) {
                    int j = 1;
                    for (; j <= highestRivalNum / 2; j++) {
                        if (getLevelOfStarter(currentTrainers, prefix + j + "-" + i) >= 30) {
                            break;
                        }
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, thisStarter, abilitySlot);
                    }
                    thisStarter = pickRandomEvolutionOf(thisStarter, false);
                    int evolvedAbilitySlot = getValidAbilitySlotFromOriginal(thisStarter, abilitySlot);
                    for (; j <= highestRivalNum; j++) {
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, thisStarter, evolvedAbilitySlot);
                    }
                } else if (timesEvolves == 2) {
                    int j = 1;
                    for (; j <= highestRivalNum; j++) {
                        if (getLevelOfStarter(currentTrainers, prefix + j + "-" + i) >= 16) {
                            break;
                        }
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, thisStarter, abilitySlot);
                    }
                    thisStarter = pickRandomEvolutionOf(thisStarter, true);
                    int evolvedAbilitySlot = getValidAbilitySlotFromOriginal(thisStarter, abilitySlot);
                    for (; j <= highestRivalNum; j++) {
                        if (getLevelOfStarter(currentTrainers, prefix + j + "-" + i) >= 36) {
                            break;
                        }
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, thisStarter, evolvedAbilitySlot);
                    }
                    thisStarter = pickRandomEvolutionOf(thisStarter, false);
                    evolvedAbilitySlot = getValidAbilitySlotFromOriginal(thisStarter, abilitySlot);
                    for (; j <= highestRivalNum; j++) {
                        changeStarterWithTag(currentTrainers, prefix + j + "-" + i, thisStarter, evolvedAbilitySlot);
                    }
                }
            }
        }

    }

    public void forceFullyEvolvedTrainerPokes() {
        int minLevel = settings.getTrainersForceFullyEvolvedLevel();

        List<Trainer> currentTrainers = romHandler.getTrainers();
        for (Trainer t : currentTrainers) {
            for (TrainerPokemon tp : t.pokemon) {
                if (tp.level >= minLevel) {
                    Species newSpecies = fullyEvolve(tp.species, t.index);
                    if (newSpecies != tp.species) {
                        tp.species = newSpecies;
                        setFormeForTrainerPokemon(tp, newSpecies);
                        tp.abilitySlot = getValidAbilitySlotFromOriginal(newSpecies, tp.abilitySlot);
                        tp.resetMoves = true;
                    }
                }
            }
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
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

            int lowest = 100;
            List<TrainerPokemon> potentialPokes = new ArrayList<>();

            // First pass: find lowest level
            for (TrainerPokemon tpk : t.pokemon) {
                if (tpk.level < lowest) {
                    lowest = tpk.level;
                }
            }

            // Second pass: find all Pokemon at lowest level
            for (TrainerPokemon tpk : t.pokemon) {
                if (tpk.level == lowest) {
                    potentialPokes.add(tpk);
                }
            }

            // If a trainer can appear in a Multi Battle (i.e., a Double Battle where the enemy consists
            // of two independent trainers), we want to be aware of that so we don't give them a team of
            // six Pokemon and have a 6v12 battle
            int maxPokemon = t.multiBattleStatus != Trainer.MultiBattleStatus.NEVER ? 3 : 6;
            for (int i = 0; i < additional; i++) {
                if (t.pokemon.size() >= maxPokemon) break;

                // We want to preserve the original last Pokemon because the order is sometimes used to
                // determine the rival's starter
                int secondToLastIndex = t.pokemon.size() - 1;
                TrainerPokemon newPokemon = potentialPokes.get(i % potentialPokes.size()).copy();

                // Clear out the held item because we only want one Pokemon with a mega stone if we're
                // swapping mega evolvables
                newPokemon.heldItem = 0;
                t.pokemon.add(secondToLastIndex, newPokemon);
            }
        }
        romHandler.setTrainers(currentTrainers);
        changesMade = true;
    }

    public void setDoubleBattleMode() {
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            if (!(tr.multiBattleStatus == Trainer.MultiBattleStatus.ALWAYS || tr.shouldNotGetBuffs())) {
                if (tr.pokemon.size() == 1) {
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
                    if (tp.level > maxLevel) {
                        highestLevelPoke = tp;
                        maxLevel = tp.level;
                    }
                }
                if (highestLevelPoke == null) {
                    continue; // should never happen - trainer had zero pokes
                }
                int[] moveset = highestLevelPoke.resetMoves ?
                        RomFunctions.getMovesAtLevel(romHandler.getAltFormeOfSpecies(
                                        highestLevelPoke.species, highestLevelPoke.forme).getNumber(),
                                movesets,
                                highestLevelPoke.level) :
                        highestLevelPoke.moves;
                randomizeHeldItem(highestLevelPoke, settings, moves, moveset);
            } else {
                for (TrainerPokemon tp : t.pokemon) {
                    int[] moveset = tp.resetMoves ?
                            RomFunctions.getMovesAtLevel(romHandler.getAltFormeOfSpecies(
                                            tp.species, tp.forme).getNumber(),
                                    movesets,
                                    tp.level) :
                            tp.moves;
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
        if (tp.hasZCrystal) {
            return; // Don't overwrite existing Z Crystals.
        }
        if (tp.hasMegaStone && swapMegaEvolutions) {
            return; // Don't overwrite mega stones if another setting handled that.
        }
        List<Integer> toChooseFrom;
        if (sensibleItemsOnly) {
            toChooseFrom = romHandler.getSensibleHeldItemsFor(tp, consumableItemsOnly, moves, moveset);
        } else if (consumableItemsOnly) {
            toChooseFrom = romHandler.getAllConsumableHeldItems();
        } else {
            toChooseFrom = romHandler.getAllHeldItems();
        }
        tp.heldItem = toChooseFrom.get(random.nextInt(toChooseFrom.size()));
    }

    /**
     * Gives each Trainer Pokemon with a held (non-species-specific) Z-crystal a new random one,
     * based on the types of its moves.
     */
    public void randomUsableZCrystals() {
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer tr : trainers) {
            for (TrainerPokemon tp : tr.pokemon) {
                if (Gen7Constants.heldZCrystalsByType.containsValue(tp.heldItem)) {
                    int[] pokeMoves = tp.resetMoves ?
                            RomFunctions.getMovesAtLevel(
                                    romHandler.getAltFormeOfSpecies(tp.species, tp.forme).getNumber(),
                                    romHandler.getMovesLearnt(), tp.level) :
                            tp.moves;
                    pokeMoves = Arrays.stream(pokeMoves).filter(mv -> mv != 0).toArray();
                    int chosenMove = pokeMoves[random.nextInt(pokeMoves.length)];
                    Type chosenMoveType = romHandler.getMoves().get(chosenMove).type;
                    tp.heldItem = Gen7Constants.heldZCrystalsByType.get(chosenMoveType);
                }
            }
        }
        romHandler.setTrainers(trainers);
        // TODO: should this could as "changes made"?
    }
}
