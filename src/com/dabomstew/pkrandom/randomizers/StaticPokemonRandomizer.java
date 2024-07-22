package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.game_data.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class StaticPokemonRandomizer extends Randomizer {

    // the totem randomization is here because the code is very similar,
    // but some notion of changes made to statics vs totems was still needed.
    private boolean totemChangesMade;

    public StaticPokemonRandomizer(RomHandler romHandler, Settings settings, Random random) {
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
        int levelModifier = settings.getStaticLevelModifier();

        List<StaticEncounter> currentStaticPokemon = romHandler.getStaticPokemon();
        for (StaticEncounter se : currentStaticPokemon) {
            if (!se.isEgg) {
                se.level = Math.min(100, (int) Math.round(se.level * (1 + levelModifier / 100.0)));
                for (StaticEncounter linkedStatic : se.linkedEncounters) {
                    if (!linkedStatic.isEgg) {
                        linkedStatic.level = Math.min(100, (int) Math.round(linkedStatic.level * (1 + levelModifier / 100.0)));
                    }
                }
            }
            setPokemonAndFormeForStaticEncounter(se, se.pkmn);
        }
        romHandler.setStaticPokemon(currentStaticPokemon);
    }

    public void randomizeStaticPokemon() {
        boolean swapLegendaries = settings.getStaticPokemonMod() == Settings.StaticPokemonMod.RANDOM_MATCHING;
        boolean similarStrength = settings.getStaticPokemonMod() == Settings.StaticPokemonMod.SIMILAR_STRENGTH;
        boolean limitMainGameLegendaries = settings.isLimitMainGameLegendaries();
        boolean limit600 = settings.isLimit600();
        boolean allowAltFormes = settings.isAllowStaticAltFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean swapMegaEvos = settings.isSwapStaticMegaEvos();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;
        int levelModifier = settings.isStaticLevelModified() ? settings.getStaticLevelModifier() : 0;
        boolean correctStaticMusic = settings.isCorrectStaticMusic();

        // Load
        List<StaticEncounter> currentStaticPokemon = romHandler.getStaticPokemon();
        List<StaticEncounter> replacements = new ArrayList<>();

        SpeciesSet banned = romHandler.getBannedForStaticPokemon();
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
                legendariesLeft = legendariesLeft.filter(pk -> !pk.isActuallyCosmetic());
            }
            SpeciesSet nonlegsLeft = new SpeciesSet(rSpecService.getNonLegendaries(allowAltFormes));
            if (allowAltFormes) {
                nonlegsLeft = nonlegsLeft.filter(pk -> !pk.isActuallyCosmetic());
            }
            SpeciesSet ultraBeastsLeft = new SpeciesSet(rSpecService.getUltrabeasts(false));
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
                if (old.pkmn.isLegendary()) {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rSpecService.getLegendaries(false), legendariesLeft, newStatic);
                    } else {
                        if (old.restrictedPool) {
                            newPK = getRestrictedStaticPokemon(legendariesPool, legendariesLeft, old);
                        } else {
                            newPK = legendariesLeft.getRandomSpecies(random);
                            legendariesLeft.remove(newPK);
                        }
                    }

                    setPokemonAndFormeForStaticEncounter(newStatic, newPK);

                    if (legendariesLeft.size() == 0) {
                        legendariesLeft.addAll(legendariesPool);
                    }
                } else if (rSpecService.getUltrabeasts(false).contains(old.pkmn)) {
                    if (old.restrictedPool) {
                        newPK = getRestrictedStaticPokemon(ultraBeastsPool, ultraBeastsLeft, old);
                    } else {
                        newPK = ultraBeastsLeft.getRandomSpecies(random);
                        ultraBeastsLeft.remove(newPK);
                    }

                    setPokemonAndFormeForStaticEncounter(newStatic, newPK);

                    if (ultraBeastsLeft.size() == 0) {
                        ultraBeastsLeft.addAll(ultraBeastsPool);
                    }
                } else {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rSpecService.getNonLegendaries(false), nonlegsLeft, newStatic);
                    } else {
                        if (old.restrictedPool) {
                            newPK = getRestrictedStaticPokemon(nonlegsPool, nonlegsLeft, old);
                        } else {
                            newPK = nonlegsLeft.getRandomSpecies(random);
                            nonlegsLeft.remove(newPK);
                        }
                    }
                    setPokemonAndFormeForStaticEncounter(newStatic, newPK);

                    if (nonlegsLeft.size() == 0) {
                        nonlegsLeft.addAll(nonlegsPool);
                    }
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.pkmn.getNumber())) {
                    specialMusicStaticChanges.put(old.pkmn.getNumber(), newPK.getNumber());
                }
            }
        } else if (similarStrength) {
            SpeciesSet listInclFormesExclCosmetics = rSpecService.getAll(true)
                    .filter(pk -> !pk.isActuallyCosmetic());
            SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                    rSpecService.getAll(false) : listInclFormesExclCosmetics);
            pokemonLeft.removeAll(banned);

            SpeciesSet pokemonPool = new SpeciesSet(pokemonLeft);

            List<Integer> mainGameLegendaries = romHandler.getMainGameLegendaries();
            for (StaticEncounter old : currentStaticPokemon) {
                StaticEncounter newStatic = cloneStaticEncounter(old);
                Species newPK;
                Species oldPK = old.pkmn;
                if (old.forme > 0) {
                    oldPK = romHandler.getAltFormeOfSpecies(oldPK, old.forme);
                }
                Integer oldBST = oldPK.getBSTForPowerLevels();
                if (oldBST >= 600 && limit600) {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rSpecService.getAll(false), pokemonLeft, newStatic);
                    } else {
                        if (old.restrictedPool) {
                            newPK = getRestrictedStaticPokemon(pokemonPool, pokemonLeft, old);
                        } else {
                            newPK = pokemonLeft.getRandomSpecies(random);
                            pokemonLeft.remove(newPK);
                        }
                    }
                    setPokemonAndFormeForStaticEncounter(newStatic, newPK);
                } else {
                    boolean limitBST = oldPK.getBaseForme() == null ?
                            limitMainGameLegendaries && mainGameLegendaries.contains(oldPK.getNumber()) :
                            limitMainGameLegendaries && mainGameLegendaries.contains(oldPK.getBaseForme().getNumber());
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        SpeciesSet megaEvoPokemonLeft = rSpecService.getMegaEvolutions()
                                .stream()
                                .filter(mega -> mega.method == 1)
                                .map(mega -> mega.from)
                                .filter(pokemonLeft::contains)
                                .collect(Collectors.toCollection(SpeciesSet::new));
                        if (megaEvoPokemonLeft.isEmpty()) {
                            megaEvoPokemonLeft = rSpecService.getMegaEvolutions()
                                    .stream()
                                    .filter(mega -> mega.method == 1)
                                    .map(mega -> mega.from)
                                    .filter(rSpecService.getAll(false)::contains)
                                    .collect(Collectors.toCollection(SpeciesSet::new));
                        }
                        if(limitBST) {
                            int bstMax = oldPK.getBSTForPowerLevels();
                            SpeciesSet lowerStrengthMEs = megaEvoPokemonLeft.filter(p -> p.getBSTForPowerLevels() <= bstMax);
                            if(!lowerStrengthMEs.isEmpty()) {
                                megaEvoPokemonLeft = lowerStrengthMEs;
                            }
                        }
                        newPK = pickStaticPowerLvlReplacement(
                                megaEvoPokemonLeft,
                                oldPK,
                                true,
                                limitBST);
                        newStatic.heldItem = newPK
                                .getMegaEvolutionsFrom()
                                .get(random.nextInt(newPK.getMegaEvolutionsFrom().size()))
                                .argument;
                    } else {
                        if (old.restrictedPool) {
                            SpeciesSet restrictedPool = pokemonLeft
                                    .filter(pk -> old.restrictedList.contains(pk));
                            if (restrictedPool.isEmpty()) {
                                restrictedPool = pokemonPool.filter(pk -> old.restrictedList.contains(pk));
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
                    setPokemonAndFormeForStaticEncounter(newStatic, newPK);
                }

                if (pokemonLeft.size() == 0) {
                    pokemonLeft.addAll(pokemonPool);
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.pkmn.getNumber())) {
                    specialMusicStaticChanges.put(old.pkmn.getNumber(), newPK.getNumber());
                }
            }
        } else { // Completely random
            SpeciesSet listInclFormesExclCosmetics = rSpecService.getAll(true)
                    .filter(pk -> !pk.isActuallyCosmetic());
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
                    if (old.restrictedPool) {
                        newPK = getRestrictedStaticPokemon(pokemonPool, pokemonLeft, old);
                    } else {
                        newPK = pokemonLeft.getRandomSpecies(random);
                        pokemonLeft.remove(newPK);
                    }
                }
                pokemonLeft.remove(newPK);
                setPokemonAndFormeForStaticEncounter(newStatic, newPK);
                if (pokemonLeft.size() == 0) {
                    pokemonLeft.addAll(pokemonPool);
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.pkmn.getNumber())) {
                    specialMusicStaticChanges.put(old.pkmn.getNumber(), newPK.getNumber());
                }
            }
        }

        if (levelModifier != 0) {
            for (StaticEncounter se : replacements) {
                if (!se.isEgg) {
                    se.level = Math.min(100, (int) Math.round(se.level * (1 + levelModifier / 100.0)));
                    se.maxLevel = Math.min(100, (int) Math.round(se.maxLevel * (1 + levelModifier / 100.0)));
                    for (StaticEncounter linkedStatic : se.linkedEncounters) {
                        if (!linkedStatic.isEgg) {
                            linkedStatic.level = Math.min(100, (int) Math.round(linkedStatic.level * (1 + levelModifier / 100.0)));
                            linkedStatic.maxLevel = Math.min(100, (int) Math.round(linkedStatic.maxLevel * (1 + levelModifier / 100.0)));
                        }
                    }
                }
            }
        }

        if (specialMusicStaticChanges.size() > 0) {
            romHandler.applyCorrectStaticMusic(specialMusicStaticChanges);
        }

        // Save
        romHandler.setStaticPokemon(replacements);
        changesMade = true;
    }

    public void randomizeTotemPokemon() {
        boolean randomizeTotem =
                settings.getTotemPokemonMod() == Settings.TotemPokemonMod.RANDOM ||
                        settings.getTotemPokemonMod() == Settings.TotemPokemonMod.SIMILAR_STRENGTH;
        boolean randomizeAllies =
                settings.getAllyPokemonMod() == Settings.AllyPokemonMod.RANDOM ||
                        settings.getAllyPokemonMod() == Settings.AllyPokemonMod.SIMILAR_STRENGTH;
        boolean randomizeAuras =
                settings.getAuraMod() == Settings.AuraMod.RANDOM ||
                        settings.getAuraMod() == Settings.AuraMod.SAME_STRENGTH;
        boolean similarStrengthTotem = settings.getTotemPokemonMod() == Settings.TotemPokemonMod.SIMILAR_STRENGTH;
        boolean similarStrengthAllies = settings.getAllyPokemonMod() == Settings.AllyPokemonMod.SIMILAR_STRENGTH;
        boolean similarStrengthAuras = settings.getAuraMod() == Settings.AuraMod.SAME_STRENGTH;
        boolean randomizeHeldItems = settings.isRandomizeTotemHeldItems();
        int levelModifier = settings.isTotemLevelsModified() ? settings.getTotemLevelModifier() : 0;
        boolean allowAltFormes = settings.isAllowTotemAltFormes();
        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;

        List<TotemPokemon> currentTotemPokemon = romHandler.getTotemPokemon();
        List<TotemPokemon> replacements = new ArrayList<>();
        SpeciesSet banned = romHandler.getBannedForStaticPokemon();
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rSpecService.getAbilityDependentFormes();
            banned.addAll(abilityDependentFormes);
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }

        SpeciesSet listInclFormesExclCosmetics = rSpecService.getAll(true).filter(
                pk -> !pk.isActuallyCosmetic());
        SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                rSpecService.getAll(false) : listInclFormesExclCosmetics);
        pokemonLeft.removeAll(banned);

        for (TotemPokemon old : currentTotemPokemon) {
            TotemPokemon newTotem = new TotemPokemon();
            newTotem.heldItem = old.heldItem;
            if (randomizeTotem) {
                Species newPK;
                Species oldPK = old.pkmn;
                if (old.forme > 0) {
                    oldPK = romHandler.getAltFormeOfSpecies(oldPK, old.forme);
                }

                if (similarStrengthTotem) {
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
                newTotem.pkmn = newPK;
                setFormeForStaticEncounter(newTotem, newPK);
                newTotem.resetMoves = true;
                newTotem.level = old.level;

                if (levelModifier != 0) {
                    newTotem.level = Math.min(100, (int) Math.round(newTotem.level * (1 + levelModifier / 100.0)));
                }
                if (pokemonLeft.size() == 0) {
                    pokemonLeft.addAll(!allowAltFormes ? rSpecService.getAll(false) : listInclFormesExclCosmetics);
                    pokemonLeft.removeAll(banned);
                }
            } else {
                newTotem.pkmn = old.pkmn;
                newTotem.level = old.level;
                if (levelModifier != 0) {
                    newTotem.level = Math.min(100, (int) Math.round(newTotem.level * (1 + levelModifier / 100.0)));
                }
                setFormeForStaticEncounter(newTotem, newTotem.pkmn);
            }

            if (randomizeAllies) {
                for (Integer oldAllyIndex : old.allies.keySet()) {
                    StaticEncounter oldAlly = old.allies.get(oldAllyIndex);
                    StaticEncounter newAlly = new StaticEncounter();
                    Species newAllyPK;
                    Species oldAllyPK = oldAlly.pkmn;
                    if (oldAlly.forme > 0) {
                        oldAllyPK = romHandler.getAltFormeOfSpecies(oldAllyPK, oldAlly.forme);
                    }
                    if (similarStrengthAllies) {
                        newAllyPK = pickStaticPowerLvlReplacement(
                                pokemonLeft,
                                oldAllyPK,
                                true,
                                false);
                    } else {
                        newAllyPK = pokemonLeft.getRandomSpecies(random);
                        pokemonLeft.remove(newAllyPK);
                    }

                    pokemonLeft.remove(newAllyPK);
                    newAlly.pkmn = newAllyPK;
                    setFormeForStaticEncounter(newAlly, newAllyPK);
                    newAlly.resetMoves = true;
                    newAlly.level = oldAlly.level;
                    if (levelModifier != 0) {
                        newAlly.level = Math.min(100, (int) Math.round(newAlly.level * (1 + levelModifier / 100.0)));
                    }

                    newTotem.allies.put(oldAllyIndex, newAlly);
                    if (pokemonLeft.size() == 0) {
                        pokemonLeft.addAll(!allowAltFormes ? rSpecService.getAll(false) : listInclFormesExclCosmetics);
                        pokemonLeft.removeAll(banned);
                    }
                }
            } else {
                newTotem.allies = old.allies;
                for (StaticEncounter ally : newTotem.allies.values()) {
                    if (levelModifier != 0) {
                        ally.level = Math.min(100, (int) Math.round(ally.level * (1 + levelModifier / 100.0)));
                        setFormeForStaticEncounter(ally, ally.pkmn);
                    }
                }
            }

            if (randomizeAuras) {
                if (similarStrengthAuras) {
                    newTotem.aura = Aura.randomAuraSimilarStrength(random, old.aura);
                } else {
                    newTotem.aura = Aura.randomAura(random);
                }
            } else {
                newTotem.aura = old.aura;
            }

            if (randomizeHeldItems) {
                if (old.heldItem != 0) {
                    List<Integer> consumableList = romHandler.getAllConsumableHeldItems();
                    newTotem.heldItem = consumableList.get(random.nextInt(consumableList.size()));
                }
            }

            replacements.add(newTotem);
        }

        // Save
        romHandler.setTotemPokemon(replacements);
        totemChangesMade = true;
    }

    private StaticEncounter cloneStaticEncounter(StaticEncounter old) {
        StaticEncounter newStatic = new StaticEncounter(old);
        newStatic.resetMoves = true;
        for (StaticEncounter linked : newStatic.linkedEncounters) {
            linked.resetMoves = true;
        }
        return newStatic;
    }

    private void setPokemonAndFormeForStaticEncounter(StaticEncounter newStatic, Species pk) {
        boolean checkCosmetics = true;
        Species newPK = pk;
        int newForme = 0;
        if (pk.getFormeNumber() > 0) {
            newForme = pk.getFormeNumber();
            newPK = pk.getBaseForme();
            checkCosmetics = false;
        }
        if (checkCosmetics && pk.getCosmeticForms() > 0) {
            newForme = pk.getCosmeticFormNumber(random.nextInt(pk.getCosmeticForms()));
        } else if (!checkCosmetics && pk.getCosmeticForms() > 0) {
            newForme += pk.getCosmeticFormNumber(random.nextInt(pk.getCosmeticForms()));
        }
        newStatic.pkmn = newPK;
        newStatic.forme = newForme;
        for (StaticEncounter linked : newStatic.linkedEncounters) {
            linked.pkmn = newPK;
            linked.forme = newForme;
        }
    }

    private Species getRestrictedStaticPokemon(SpeciesSet fullList, SpeciesSet pokemonLeft,
                                               StaticEncounter old) {
        SpeciesSet restrictedPool = pokemonLeft.filter(pk -> old.restrictedList.contains(pk));
        if (restrictedPool.isEmpty()) {
            restrictedPool = fullList.filter(pk -> old.restrictedList.contains(pk));
        }
        Species newPK = restrictedPool.getRandomSpecies(random);
        pokemonLeft.remove(newPK);
        return newPK;
    }

    private Species getMegaEvoPokemon(SpeciesSet fullList, SpeciesSet pokemonLeft,
                                      StaticEncounter newStatic) {
        Set<MegaEvolution> megaEvos = rSpecService.getMegaEvolutions();
        SpeciesSet megaEvoPokemon = megaEvos
                .stream()
                .filter(mega -> mega.method == 1)
                .map(mega -> mega.from)
                .collect(Collectors.toCollection(SpeciesSet::new));
        SpeciesSet megaEvoPokemonLeft = new SpeciesSet(megaEvoPokemon).filter(pokemonLeft::contains);
        if (megaEvoPokemonLeft.isEmpty()) {
            megaEvoPokemonLeft = new SpeciesSet(megaEvoPokemon).filter(fullList::contains);
        }

        Species newPK = megaEvoPokemonLeft.getRandomSpecies(random);
        pokemonLeft.remove(newPK);
        newStatic.heldItem = newPK
                .getMegaEvolutionsFrom()
                .get(random.nextInt(newPK.getMegaEvolutionsFrom().size()))
                .argument;
        return newPK;
    }

    private Species pickStaticPowerLvlReplacement(SpeciesSet pokemonPool, Species current,
                                                  boolean banSamePokemon, boolean limitBST) {
        SpeciesSet finalPool = pokemonPool;
        if(limitBST) {
            int maxBST = current.getBSTForPowerLevels();
            finalPool = finalPool.filter(p -> p.getBSTForPowerLevels() <= maxBST);
        }

        return finalPool.getRandomSimilarStrengthSpecies(current, banSamePokemon, random);
    }

    private void setFormeForStaticEncounter(StaticEncounter newStatic, Species pk) {
        boolean checkCosmetics = true;
        newStatic.forme = 0;
        if (pk.getFormeNumber() > 0) {
            newStatic.forme = pk.getFormeNumber();
            newStatic.pkmn = pk.getBaseForme();
            checkCosmetics = false;
        }
        if (checkCosmetics && newStatic.pkmn.getCosmeticForms() > 0) {
            newStatic.forme = newStatic.pkmn.getCosmeticFormNumber(random.nextInt(newStatic.pkmn.getCosmeticForms()));
        } else if (!checkCosmetics && pk.getCosmeticForms() > 0) {
            newStatic.forme += pk.getCosmeticFormNumber(random.nextInt(pk.getCosmeticForms()));
        }
    }

}
