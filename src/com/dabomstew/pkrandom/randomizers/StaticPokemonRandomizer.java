package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.*;
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
            if (!se.isEgg()) {
                se.setLevel(Math.min(100, (int) Math.round(se.getLevel() * (1 + levelModifier / 100.0))));
                for (StaticEncounter linkedStatic : se.getLinkedEncounters()) {
                    if (!linkedStatic.isEgg()) {
                        linkedStatic.setLevel(Math.min(100, (int) Math.round(linkedStatic.getLevel() * (1 + levelModifier / 100.0))));
                    }
                }
            }
            setPokemonAndFormeForStaticEncounter(se, se.getSpecies());
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
        banned.addAll(rPokeService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            SpeciesSet abilityDependentFormes = rPokeService.getAbilityDependentFormes();
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
            SpeciesSet legendariesLeft = new SpeciesSet(rPokeService.getLegendaries(allowAltFormes));
            if (allowAltFormes) {
                legendariesLeft = legendariesLeft.filter(pk -> !pk.isActuallyCosmetic());
            }
            SpeciesSet nonlegsLeft = new SpeciesSet(rPokeService.getNonLegendaries(allowAltFormes));
            if (allowAltFormes) {
                nonlegsLeft = nonlegsLeft.filter(pk -> !pk.isActuallyCosmetic());
            }
            SpeciesSet ultraBeastsLeft = new SpeciesSet(rPokeService.getUltrabeasts(false));
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
                        newPK = getMegaEvoPokemon(rPokeService.getLegendaries(false), legendariesLeft, newStatic);
                    } else {
                        if (old.isRestrictedPool()) {
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
                } else if (rPokeService.getUltrabeasts(false).contains(old.getSpecies())) {
                    if (old.isRestrictedPool()) {
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
                        newPK = getMegaEvoPokemon(rPokeService.getNonLegendaries(false), nonlegsLeft, newStatic);
                    } else {
                        if (old.isRestrictedPool()) {
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
                if (changeMusicStatics.contains(old.getSpecies().getNumber())) {
                    specialMusicStaticChanges.put(old.getSpecies().getNumber(), newPK.getNumber());
                }
            }
        } else if (similarStrength) {
            SpeciesSet listInclFormesExclCosmetics = rPokeService.getAll(true)
                    .filter(pk -> !pk.isActuallyCosmetic());
            SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                    rPokeService.getAll(false) : listInclFormesExclCosmetics);
            pokemonLeft.removeAll(banned);

            SpeciesSet pokemonPool = new SpeciesSet(pokemonLeft);

            List<Integer> mainGameLegendaries = romHandler.getMainGameLegendaries();
            for (StaticEncounter old : currentStaticPokemon) {
                StaticEncounter newStatic = cloneStaticEncounter(old);
                Species newPK;
                Species oldPK = old.getSpecies();
                if (old.getForme() > 0) {
                    oldPK = romHandler.getAltFormeOfPokemon(oldPK, old.getForme());
                }
                Integer oldBST = oldPK.getBSTForPowerLevels();
                if (oldBST >= 600 && limit600) {
                    if (reallySwapMegaEvos && old.canMegaEvolve()) {
                        newPK = getMegaEvoPokemon(rPokeService.getAll(false), pokemonLeft, newStatic);
                    } else {
                        if (old.isRestrictedPool()) {
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
                        SpeciesSet megaEvoPokemonLeft = rPokeService.getMegaEvolutions()
                                .stream()
                                .filter(MegaEvolution::isNeedsItem)
                                .map(mega -> mega.getFrom())
                                .filter(pokemonLeft::contains)
                                .collect(Collectors.toCollection(SpeciesSet::new));
                        if (megaEvoPokemonLeft.isEmpty()) {
                            megaEvoPokemonLeft = rPokeService.getMegaEvolutions()
                                    .stream()
                                    .filter(MegaEvolution::isNeedsItem)
                                    .map(mega -> mega.getFrom())
                                    .filter(rPokeService.getAll(false)::contains)
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
                        newStatic.setHeldItem(newPK
                                .getMegaEvolutionsFrom()
                                .get(random.nextInt(newPK.getMegaEvolutionsFrom().size()))
                                .getItem());
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
                    setPokemonAndFormeForStaticEncounter(newStatic, newPK);
                }

                if (pokemonLeft.size() == 0) {
                    pokemonLeft.addAll(pokemonPool);
                }
                replacements.add(newStatic);
                if (changeMusicStatics.contains(old.getSpecies().getNumber())) {
                    specialMusicStaticChanges.put(old.getSpecies().getNumber(), newPK.getNumber());
                }
            }
        } else { // Completely random
            SpeciesSet listInclFormesExclCosmetics = rPokeService.getAll(true)
                    .filter(pk -> !pk.isActuallyCosmetic());
            SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                    rPokeService.getAll(false) : listInclFormesExclCosmetics);
            pokemonLeft.removeAll(banned);

            SpeciesSet pokemonPool = new SpeciesSet(pokemonLeft);

            for (StaticEncounter old : currentStaticPokemon) {
                StaticEncounter newStatic = cloneStaticEncounter(old);
                Species newPK;
                if (reallySwapMegaEvos && old.canMegaEvolve()) {
                    newPK = getMegaEvoPokemon(rPokeService.getAll(false), pokemonLeft, newStatic);
                } else {
                    if (old.isRestrictedPool()) {
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
                if (changeMusicStatics.contains(old.getSpecies().getNumber())) {
                    specialMusicStaticChanges.put(old.getSpecies().getNumber(), newPK.getNumber());
                }
            }
        }

        if (levelModifier != 0) {
            for (StaticEncounter se : replacements) {
                if (!se.isEgg()) {
                    se.setLevel(Math.min(100, (int) Math.round(se.getLevel() * (1 + levelModifier / 100.0))));
                    se.setMaxLevel(Math.min(100, (int) Math.round(se.getMaxLevel() * (1 + levelModifier / 100.0))));
                    for (StaticEncounter linkedStatic : se.getLinkedEncounters()) {
                        if (!linkedStatic.isEgg()) {
                            linkedStatic.setLevel(Math.min(100, (int) Math.round(linkedStatic.getLevel() * (1 + levelModifier / 100.0))));
                            linkedStatic.setMaxLevel(Math.min(100, (int) Math.round(linkedStatic.getMaxLevel() * (1 + levelModifier / 100.0))));
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
            SpeciesSet abilityDependentFormes = rPokeService.getAbilityDependentFormes();
            banned.addAll(abilityDependentFormes);
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }

        SpeciesSet listInclFormesExclCosmetics = rPokeService.getAll(true).filter(
                pk -> !pk.isActuallyCosmetic());
        SpeciesSet pokemonLeft = new SpeciesSet(!allowAltFormes ?
                rPokeService.getAll(false) : listInclFormesExclCosmetics);
        pokemonLeft.removeAll(banned);

        for (TotemPokemon old : currentTotemPokemon) {
            TotemPokemon newTotem = new TotemPokemon();
            newTotem.setHeldItem(old.getHeldItem());
            if (randomizeTotem) {
                Species newPK;
                Species oldPK = old.getSpecies();
                if (old.getForme() > 0) {
                    oldPK = romHandler.getAltFormeOfPokemon(oldPK, old.getForme());
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
                newTotem.setSpecies(newPK);
                setFormeForStaticEncounter(newTotem, newPK);
                newTotem.setResetMoves(true);
                newTotem.setLevel(old.getLevel());

                if (levelModifier != 0) {
                    newTotem.setLevel(Math.min(100, (int) Math.round(newTotem.getLevel() * (1 + levelModifier / 100.0))));
                }
                if (pokemonLeft.size() == 0) {
                    pokemonLeft.addAll(!allowAltFormes ? rPokeService.getAll(false) : listInclFormesExclCosmetics);
                    pokemonLeft.removeAll(banned);
                }
            } else {
                newTotem.setSpecies(old.getSpecies());
                newTotem.setLevel(old.getLevel());
                if (levelModifier != 0) {
                    newTotem.setLevel(Math.min(100, (int) Math.round(newTotem.getLevel() * (1 + levelModifier / 100.0))));
                }
                setFormeForStaticEncounter(newTotem, newTotem.getSpecies());
            }

            if (randomizeAllies) {
                for (Integer oldAllyIndex : old.getAllies().keySet()) {
                    StaticEncounter oldAlly = old.getAllies().get(oldAllyIndex);
                    StaticEncounter newAlly = new StaticEncounter();
                    Species newAllyPK;
                    Species oldAllyPK = oldAlly.getSpecies();
                    if (oldAlly.getForme() > 0) {
                        oldAllyPK = romHandler.getAltFormeOfPokemon(oldAllyPK, oldAlly.getForme());
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
                    newAlly.setSpecies(newAllyPK);
                    setFormeForStaticEncounter(newAlly, newAllyPK);
                    newAlly.setResetMoves(true);
                    newAlly.setLevel(oldAlly.getLevel());
                    if (levelModifier != 0) {
                        newAlly.setLevel(Math.min(100, (int) Math.round(newAlly.getLevel() * (1 + levelModifier / 100.0))));
                    }

                    newTotem.getAllies().put(oldAllyIndex, newAlly);
                    if (pokemonLeft.size() == 0) {
                        pokemonLeft.addAll(!allowAltFormes ? rPokeService.getAll(false) : listInclFormesExclCosmetics);
                        pokemonLeft.removeAll(banned);
                    }
                }
            } else {
                newTotem.setAllies(old.getAllies());
                for (StaticEncounter ally : newTotem.getAllies().values()) {
                    if (levelModifier != 0) {
                        ally.setLevel(Math.min(100, (int) Math.round(ally.getLevel() * (1 + levelModifier / 100.0))));
                        setFormeForStaticEncounter(ally, ally.getSpecies());
                    }
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

            replacements.add(newTotem);
        }

        // Save
        romHandler.setTotemPokemon(replacements);
        totemChangesMade = true;
    }

    private StaticEncounter cloneStaticEncounter(StaticEncounter old) {
        StaticEncounter newStatic = new StaticEncounter(old);
        newStatic.setResetMoves(true);
        for (StaticEncounter linked : newStatic.getLinkedEncounters()) {
            linked.setResetMoves(true);
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
        newStatic.setSpecies(newPK);
        newStatic.setForme(newForme);
        for (StaticEncounter linked : newStatic.getLinkedEncounters()) {
            linked.setSpecies(newPK);
            linked.setForme(newForme);
        }
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
        Set<MegaEvolution> megaEvos = rPokeService.getMegaEvolutions();
        SpeciesSet megaEvoPokemon = megaEvos
                .stream()
                .filter(MegaEvolution::isNeedsItem)
                .map(MegaEvolution::getFrom)
                .collect(Collectors.toCollection(SpeciesSet::new));
        SpeciesSet megaEvoPokemonLeft = new SpeciesSet(megaEvoPokemon).filter(pokemonLeft::contains);
        if (megaEvoPokemonLeft.isEmpty()) {
            megaEvoPokemonLeft = new SpeciesSet(megaEvoPokemon).filter(fullList::contains);
        }

        Species newPK = megaEvoPokemonLeft.getRandomSpecies(random);
        pokemonLeft.remove(newPK);
        newStatic.setHeldItem(newPK
                .getMegaEvolutionsFrom()
                .get(random.nextInt(newPK.getMegaEvolutionsFrom().size()))
                .getItem());
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
        newStatic.setForme(0);
        if (pk.getFormeNumber() > 0) {
            newStatic.setForme(pk.getFormeNumber());
            newStatic.setSpecies(pk.getBaseForme());
            checkCosmetics = false;
        }
        if (checkCosmetics && newStatic.getSpecies().getCosmeticForms() > 0) {
            newStatic.setForme(newStatic.getSpecies().getCosmeticFormNumber(random.nextInt(newStatic.getSpecies().getCosmeticForms())));
        } else if (!checkCosmetics && pk.getCosmeticForms() > 0) {
            newStatic.setForme(newStatic.getForme() + pk.getCosmeticFormNumber(random.nextInt(pk.getCosmeticForms())));
        }
    }

}
