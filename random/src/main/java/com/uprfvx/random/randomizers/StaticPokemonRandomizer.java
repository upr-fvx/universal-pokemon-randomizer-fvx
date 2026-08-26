package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.gamedata.*;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class StaticPokemonRandomizer extends Randomizer {

    // the totem randomization is here because the code is very similar,
    // but some notion of changes made to statics vs totems was still needed.
    private boolean totemChangesMade;

    public StaticPokemonRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    /**
     * Returns whether any changes to non-Totem static Pokemon have been made. Alias for {@link #isChangesMade()};
     */
    public boolean isStaticChangesMade() {
        return changesMade;
    }

    /**
     * Returns whether any changes to Totem Pokemon have been made.
     */
    public boolean isTotemChangesMade() {
        return totemChangesMade;
    }

    public void onlyChangeStaticLevels() {
        int levelModifier = settings.get(Settings.Name.STATICS_LEVEL_MODIFIER_PERCENT);

        List<StaticEncounter> currentStaticPokemon = romHandler.getStaticPokemon();
        for (StaticEncounter se : currentStaticPokemon) {
            if (!se.isEgg()) {
                se.setLevel(applyPercentageLevelModifier(se.getLevel(), levelModifier));
                for (StaticEncounter linkedStatic : se.getLinkedEncounters()) {
                    if (!linkedStatic.isEgg()) {
                        linkedStatic.setLevel(applyPercentageLevelModifier(linkedStatic.getLevel(), levelModifier));
                    }
                }
            }
        }
        romHandler.setStaticPokemon(currentStaticPokemon);
    }

    public void randomizeStaticPokemon() {
        Settings.StaticPokemonMod mod = settings.get(Settings.Name.RANDOMIZE_STATIC_ENCOUNTERS);
        boolean swapLegendaries = mod == Settings.StaticPokemonMod.RANDOM_MATCHING;
        boolean similarStrength = mod == Settings.StaticPokemonMod.SIMILAR_STRENGTH;
        boolean limitMainGameLegendaries = settings.get(Settings.Name.STATICS_LIMIT_MAIN_GAME_LEGENDARIES);
        boolean limit600 = settings.get(Settings.Name.STATICS_FULL_RANDOM_OVER_600_BST);
        boolean allowAltFormes = settings.get(Settings.Name.STATICS_ALLOW_ALT_FORMES);
        boolean banIrregularAltFormes = settings.get(Settings.Name.LIMIT_NO_TEMPORARY_ALT_FORMES);
        boolean swapMegaEvos = settings.get(Settings.Name.STATICS_SWAP_MEGA_EVOLVABLES);
        Settings.AbilitiesMod abilitiesMod = settings.get(Settings.Name.RANDOMIZE_SPECIES_ABILITIES);
        boolean abilitiesAreRandomized = abilitiesMod == Settings.AbilitiesMod.RANDOMIZE;
        int levelModifier = settings.get(Settings.Name.STATICS_LEVEL_MODIFIER_PERCENT);
        boolean correctStaticMusic = settings.get(Settings.Name.STATICS_FIX_MUSIC);

        // Load
        List<StaticEncounter> currentStaticPokemon = romHandler.getStaticPokemon();
        List<StaticEncounter> replacements = new ArrayList<>();

        SpeciesSet banned = new SpeciesSet(romHandler.getBannedForStaticPokemon());
        banned.addAll(rSpecService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rSpecService.getAbilityDependentFormes();
            banned.addAll(abilityDependentFormes);
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }
        boolean reallySwapMegaEvos = romHandler.forceSwapStaticMegaEvos() || swapMegaEvos;

        Map<Integer, Integer> specialMusicStaticChanges = new HashMap<>();
        List<Integer> changeMusicStatics = new ArrayList<>();
        if (correctStaticMusic) {
            changeMusicStatics = romHandler.getSpecialMusicStatics();
        }

        if (swapLegendaries) {
            SpeciesSet legendariesLeft = new SpeciesSet(rSpecService.getLegendaries(allowAltFormes));
            if (allowAltFormes) {
                legendariesLeft = legendariesLeft.filter(pk -> !pk.isEssentiallyCosmetic());
            }
            SpeciesSet nonlegsLeft = new SpeciesSet(rSpecService.getNonLegendaries(allowAltFormes));
            if (allowAltFormes) {
                nonlegsLeft = nonlegsLeft.filter(pk -> !pk.isEssentiallyCosmetic());
            }
            SpeciesSet ultraBeastsLeft = new SpeciesSet(rSpecService.getUltrabeasts(false));
            legendariesLeft.removeAll(ultraBeastsLeft);
            nonlegsLeft.removeAll(ultraBeastsLeft);
            legendariesLeft.removeAll(banned);
            nonlegsLeft.removeAll(banned);
            ultraBeastsLeft.removeAll(banned);

            // Full pools for easier refilling later
            SpeciesSet legendariesPool = new SpeciesSet(legendariesLeft);
            SpeciesSet nonlegsPool = new SpeciesSet(nonlegsLeft);
            SpeciesSet ultraBeastsPool = new SpeciesSet(ultraBeastsLeft);

            for (StaticEncounter old : currentStaticPokemon) {
                StaticEncounter newStatic = cloneStaticEncounter(old);
                Species newPK;
                if (old.getSpecies().isLegendary()) {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rSpecService.getLegendaries(false), legendariesLeft, newStatic);
                    } else {
                        if (old.isRestrictedPool()) {
                            newPK = getRestrictedStaticPokemon(legendariesPool, legendariesLeft, old);
                        } else {
                            newPK = legendariesLeft.getRandomSpecies(random);
                            legendariesLeft.remove(newPK);
                        }
                    }
                    newStatic.getSpeciesHolder().setSpecies(newPK);
                    randomizeCosmeticForme(newStatic);

                    if (legendariesLeft.isEmpty()) {
                        legendariesLeft.addAll(legendariesPool);
                    }
                } else if (rSpecService.getUltrabeasts(false).contains(old.getSpecies())) {
                    if (old.isRestrictedPool()) {
                        newPK = getRestrictedStaticPokemon(ultraBeastsPool, ultraBeastsLeft, old);
                    } else {
                        newPK = ultraBeastsLeft.getRandomSpecies(random);
                        ultraBeastsLeft.remove(newPK);
                    }
                    newStatic.getSpeciesHolder().setSpecies(newPK);
                    randomizeCosmeticForme(newStatic);

                    if (ultraBeastsLeft.isEmpty()) {
                        ultraBeastsLeft.addAll(ultraBeastsPool);
                    }
                } else {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rSpecService.getNonLegendaries(false), nonlegsLeft, newStatic);
                    } else {
                        if (old.isRestrictedPool()) {
                            newPK = getRestrictedStaticPokemon(nonlegsPool, nonlegsLeft, old);
                        } else {
                            newPK = nonlegsLeft.getRandomSpecies(random);
                            nonlegsLeft.remove(newPK);
                        }
                    }
                    newStatic.getSpeciesHolder().setSpecies(newPK);
                    randomizeCosmeticForme(newStatic);

                    if (nonlegsLeft.isEmpty()) {
                        nonlegsLeft.addAll(nonlegsPool);
                    }
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.getSpecies().getNumber())) {
                    specialMusicStaticChanges.put(old.getSpecies().getNumber(), newPK.getNumber());
                }
            }
        } else if (similarStrength) {
            SpeciesSet listInclFormesExclCosmetics = rSpecService.getAll(true)
                    .filter(pk -> !pk.isEssentiallyCosmetic());
            SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                    rSpecService.getAll(false) : listInclFormesExclCosmetics);
            pokemonLeft.removeAll(banned);

            SpeciesSet pokemonPool = new SpeciesSet(pokemonLeft);

            List<Integer> mainGameLegendaries = romHandler.getMainGameLegendaries();
            for (StaticEncounter old : currentStaticPokemon) {
                StaticEncounter newStatic = cloneStaticEncounter(old);
                Species newPK;
                Species oldPK = old.getSpecies();
                if (oldPK.getBST(true) >= 600 && limit600) {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rSpecService.getAll(false), pokemonLeft, newStatic);
                    } else {
                        if (old.isRestrictedPool()) {
                            newPK = getRestrictedStaticPokemon(pokemonPool, pokemonLeft, old);
                        } else {
                            newPK = pokemonLeft.getRandomSpecies(random);
                            pokemonLeft.remove(newPK);
                        }
                    }
                } else {
                    boolean limitBST = limitMainGameLegendaries
                            && mainGameLegendaries.contains(oldPK.getBaseForme().getNumber());
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        SpeciesSet megaEvoPokemonLeft = rSpecService.getMegaEvolutions()
                                .stream()
                                .filter(Species::needsMegaEvolutionItem)
                                .map(Species::getBaseForme)
                                .filter(pokemonLeft::contains)
                                .collect(Collectors.toCollection(SpeciesSet::new));
                        if (megaEvoPokemonLeft.isEmpty()) {
                            megaEvoPokemonLeft = rSpecService.getMegaEvolutions()
                                    .stream()
                                    .filter(Species::needsMegaEvolutionItem)
                                    .map(Species::getBaseForme)
                                    .filter(rSpecService.getAll(false)::contains)
                                    .collect(Collectors.toCollection(SpeciesSet::new));
                        }
                        if(limitBST) {
                            int bstMax = oldPK.getBST(true);
                            SpeciesSet lowerStrengthMEs = megaEvoPokemonLeft.filter(p -> p.getBST(false) <= bstMax);
                            if(!lowerStrengthMEs.isEmpty()) {
                                megaEvoPokemonLeft = lowerStrengthMEs;
                            }
                        }
                        newPK = pickStaticPowerLvlReplacement(
                                megaEvoPokemonLeft,
                                oldPK,
                                true,
                                limitBST);
                        newStatic.setHeldItem(newPK
                                .getAltFormes()
                                .filter(Species::isMegaEvolution)
                                .getRandomSpecies(random)
                                .getMegaEvolutionItem()
                        );
                    } else {
                        if (old.isRestrictedPool()) {
                            SpeciesSet restrictedPool = pokemonLeft
                                    .filter(pk -> old.getRestrictedList().contains(pk));
                            if (restrictedPool.isEmpty()) {
                                restrictedPool = pokemonPool.filter(pk -> old.getRestrictedList().contains(pk));
                            }
                            newPK = pickStaticPowerLvlReplacement(
                                    restrictedPool,
                                    oldPK,
                                    false, // Allow same Pokemon just in case
                                    limitBST);
                        } else {
                            newPK = pickStaticPowerLvlReplacement(
                                    pokemonLeft,
                                    oldPK,
                                    true,
                                    limitBST);
                        }
                    }
                    pokemonLeft.remove(newPK);
                }
                newStatic.getSpeciesHolder().setSpecies(newPK);
                randomizeCosmeticForme(newStatic);

                if (pokemonLeft.isEmpty()) {
                    pokemonLeft.addAll(pokemonPool);
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.getSpecies().getNumber())) {
                    specialMusicStaticChanges.put(old.getSpecies().getNumber(), newPK.getNumber());
                }
            }
        } else { // Completely random
            SpeciesSet listInclFormesExclCosmetics = rSpecService.getAll(true)
                    .filter(pk -> !pk.isEssentiallyCosmetic());
            SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                    rSpecService.getAll(false) : listInclFormesExclCosmetics);
            pokemonLeft.removeAll(banned);

            SpeciesSet pokemonPool = new SpeciesSet(pokemonLeft);

            for (StaticEncounter old : currentStaticPokemon) {
                StaticEncounter newStatic = cloneStaticEncounter(old);
                Species newPK;
                if (reallySwapMegaEvos && old.canMegaEvolve()) {
                    newPK = getMegaEvoPokemon(rSpecService.getAll(false), pokemonLeft, newStatic);
                } else {
                    if (old.isRestrictedPool()) {
                        newPK = getRestrictedStaticPokemon(pokemonPool, pokemonLeft, old);
                    } else {
                        newPK = pokemonLeft.getRandomSpecies(random);
                        pokemonLeft.remove(newPK);
                    }
                }
                newStatic.getSpeciesHolder().setSpecies(newPK);
                randomizeCosmeticForme(newStatic);

                pokemonLeft.remove(newPK);
                if (pokemonLeft.isEmpty()) {
                    pokemonLeft.addAll(pokemonPool);
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.getSpecies().getNumber())) {
                    specialMusicStaticChanges.put(old.getSpecies().getNumber(), newPK.getNumber());
                }
            }
        }

        if (levelModifier != 0) {
            for (StaticEncounter se : replacements) {
                if (!se.isEgg()) {
                    se.setLevel(applyPercentageLevelModifier(se.getLevel(), levelModifier));
                    se.setMaxLevel(applyPercentageLevelModifier(se.getMaxLevel(), levelModifier));
                    for (StaticEncounter linkedStatic : se.getLinkedEncounters()) {
                        if (!linkedStatic.isEgg()) {
                            linkedStatic.setLevel(applyPercentageLevelModifier(linkedStatic.getLevel(), levelModifier));
                            linkedStatic.setMaxLevel(applyPercentageLevelModifier(linkedStatic.getMaxLevel(), levelModifier));
                        }
                    }
                }
            }
        }

        if (!specialMusicStaticChanges.isEmpty()) {
            romHandler.applyCorrectStaticMusic(specialMusicStaticChanges);
        }

        // Save
        romHandler.setStaticPokemon(replacements);
        changesMade = true;
    }

    public void randomizeTotemPokemon() {
        Settings.TotemPokemonMod totemMod = settings.get(Settings.Name.RANDOMIZE_TOTEM_POKEMON);
        Settings.AllyPokemonMod allyMod = settings.get(Settings.Name.TOTEMS_RANDOMIZE_ALLIES);
        Settings.AuraMod auraMod = settings.get(Settings.Name.TOTEMS_RANDOMIZE_AURAS);
        boolean randomizeTotem =
                totemMod == Settings.TotemPokemonMod.RANDOM ||
                totemMod == Settings.TotemPokemonMod.SIMILAR_STRENGTH;
        boolean randomizeAllies =
                allyMod == Settings.AllyPokemonMod.RANDOM ||
                allyMod == Settings.AllyPokemonMod.SIMILAR_STRENGTH;
        boolean randomizeAuras =
                auraMod == Settings.AuraMod.RANDOM ||
                auraMod == Settings.AuraMod.SAME_STRENGTH;
        boolean similarStrengthTotem = totemMod == Settings.TotemPokemonMod.SIMILAR_STRENGTH;
        boolean similarStrengthAllies = allyMod == Settings.AllyPokemonMod.SIMILAR_STRENGTH;
        boolean similarStrengthAuras = auraMod == Settings.AuraMod.SAME_STRENGTH;
        boolean randomizeHeldItems = settings.get(Settings.Name.TOTEMS_RANDOMIZE_HELD_ITEMS);
        int levelModifier = settings.get(Settings.Name.TOTEMS_LEVEL_MODIFIER_PERCENT);
        boolean allowAltFormes = settings.get(Settings.Name.TOTEMS_ALLOW_ALT_FORMES);
        boolean banIrregularAltFormes = settings.get(Settings.Name.LIMIT_NO_TEMPORARY_ALT_FORMES);
        Settings.AbilitiesMod abilitiesMod = settings.get(Settings.Name.RANDOMIZE_SPECIES_ABILITIES);
        boolean abilitiesAreRandomized = abilitiesMod == Settings.AbilitiesMod.RANDOMIZE;

        List<TotemPokemon> currentTotemPokemon = romHandler.getTotemPokemon();
        List<TotemPokemon> replacements = new ArrayList<>();
        SpeciesSet banned = new SpeciesSet(romHandler.getBannedForStaticPokemon());
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rSpecService.getAbilityDependentFormes();
            banned.addAll(abilityDependentFormes);
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }

        SpeciesSet allNonCosmetic = rSpecService.getAll(true).filter(
                pk -> !pk.isEssentiallyCosmetic());
        SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                rSpecService.getAll(false) : allNonCosmetic);
        pokemonLeft.removeAll(banned);

        for (TotemPokemon old : currentTotemPokemon) {
            TotemPokemon newTotem = new TotemPokemon(old);
            if (randomizeTotem) {
                pickRandomTotemOrAlly(newTotem, pokemonLeft, allNonCosmetic, banned,
                        allowAltFormes, similarStrengthTotem);
            }

            if (randomizeAllies) {
                for (StaticEncounter newAlly : newTotem.getAllies().values()) {
                    pickRandomTotemOrAlly(newAlly, pokemonLeft, allNonCosmetic, banned,
                            allowAltFormes, similarStrengthAllies);
                }
            }

            if (randomizeAuras) {
                if (similarStrengthAuras) {
                    newTotem.setAura(Aura.randomAuraSimilarStrength(random, old.getAura()));
                } else {
                    newTotem.setAura(Aura.randomAura(random));
                }
            } else {
                newTotem.setAura(old.getAura());
            }

            if (randomizeHeldItems) {
                if (old.getHeldItem() != null) {
                    List<Item> consumableList = new ArrayList<>(romHandler.getAllConsumableHeldItems());
                    newTotem.setHeldItem(consumableList.get(random.nextInt(consumableList.size())));
                }
            }

            if (levelModifier != 0) {
                newTotem.setLevel(applyPercentageLevelModifier(newTotem.getLevel(), levelModifier));
                for (StaticEncounter ally : newTotem.getAllies().values()) {
                    ally.setLevel(applyPercentageLevelModifier(ally.getLevel(), levelModifier));
                }
            }

            replacements.add(newTotem);
        }

        // Save
        romHandler.setTotemPokemon(replacements);
        totemChangesMade = true;
    }

    /**
     * Picks and sets a random Species for the Totem Pokemon/Ally that is passed in as staticEnc.
     * Assumes that staticEnc is a clone of the original totem/ally.
     */
    private void pickRandomTotemOrAlly(StaticEncounter staticEnc,
                                       SpeciesSet pokemonLeft, SpeciesSet allNonCosmetic,
                                       SpeciesSet banned, boolean allowAltFormes,
                                       boolean similarStrength) {
        // We assume staticEnc is a clone of the original totem/ally;
        // thus the species it holds is the "old" species.
        Species oldPK = staticEnc.getSpecies();
        Species newPK;

        if (similarStrength) {
            newPK = pickStaticPowerLvlReplacement(
                    pokemonLeft,
                    oldPK,
                    true,
                    false);
        } else {
            newPK = pokemonLeft.getRandomSpecies(random);
            pokemonLeft.remove(newPK);
        }
        pokemonLeft.remove(newPK);

        staticEnc.getSpeciesHolder().setSpecies(newPK);
        randomizeCosmeticForme(staticEnc);
        staticEnc.setResetMoves(true);

        if (pokemonLeft.isEmpty()) {
            pokemonLeft.addAll(!allowAltFormes ? rSpecService.getAll(false) : allNonCosmetic);
            pokemonLeft.removeAll(banned);
        }
    }

    private StaticEncounter cloneStaticEncounter(StaticEncounter old) {
        StaticEncounter newStatic = new StaticEncounter(old);
        newStatic.setResetMoves(true);
        for (StaticEncounter linked : newStatic.getLinkedEncounters()) {
            linked.setResetMoves(true);
        }
        return newStatic;
    }

    private Species getRestrictedStaticPokemon(SpeciesSet fullList, SpeciesSet pokemonLeft,
                                               StaticEncounter old) {
        SpeciesSet restrictedPool = pokemonLeft.filter(pk -> old.getRestrictedList().contains(pk));
        if (restrictedPool.isEmpty()) {
            restrictedPool = fullList.filter(pk -> old.getRestrictedList().contains(pk));
        }
        Species newPK = restrictedPool.getRandomSpecies(random);
        pokemonLeft.remove(newPK);
        return newPK;
    }

    private Species getMegaEvoPokemon(SpeciesSet fullList, SpeciesSet pokemonLeft,
                                      StaticEncounter newStatic) {
        SpeciesSet megaEvos = rSpecService.getMegaEvolutions();
        SpeciesSet megaEvoPokemon = megaEvos
                .stream()
                .filter(Species::needsMegaEvolutionItem)
                .map(Species::getBaseForme)
                .collect(Collectors.toCollection(SpeciesSet::new));
        SpeciesSet megaEvoPokemonLeft = new SpeciesSet(megaEvoPokemon).filter(pokemonLeft::contains);
        if (megaEvoPokemonLeft.isEmpty()) {
            megaEvoPokemonLeft = new SpeciesSet(megaEvoPokemon).filter(fullList::contains);
        }

        Species newPK = megaEvoPokemonLeft.getRandomSpecies(random);
        pokemonLeft.remove(newPK);
        newStatic.setHeldItem(newPK
                .getAltFormes()
                .filter(Species::isMegaEvolution)
                .getRandomSpecies(random)
                .getMegaEvolutionItem()
        );
        return newPK;
    }

    private Species pickStaticPowerLvlReplacement(SpeciesSet pokemonPool, Species current,
                                                  boolean banSamePokemon, boolean limitBST) {
        SpeciesSet finalPool = pokemonPool;
        if(limitBST) {
            int maxBST = current.getBST(false);
            finalPool = finalPool.filter(p -> p.getBST(true) <= maxBST);
        }

        return finalPool.getRandomSimilarStrengthSpecies(current, banSamePokemon, random);
    }

    /**
     * If possible, sets the Species of the given StaticEncounter to a random cosmetic forme.<br>
     * Does nothing if StaticEncounter doesn't allow alt formes, or if the Species doesn't have any cosmetic alt formes.
     */
    private void randomizeCosmeticForme(StaticEncounter se) {
        SpeciesHolder sh = se.getSpeciesHolder();
        if (sh.isAltFormeAllowed() && sh.getSpecies().isBaseForme()) {
            Species base = sh.getSpecies().getBaseForme();
            sh.setFormeNumber(base.getRandomCosmeticFormeNumber(random));
        }
    }

}
