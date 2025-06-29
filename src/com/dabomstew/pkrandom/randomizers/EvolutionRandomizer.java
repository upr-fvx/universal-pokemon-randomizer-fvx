package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.constants.SpeciesIDs;
import com.dabomstew.pkromio.gamedata.Evolution;
import com.dabomstew.pkromio.gamedata.EvolutionType;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.SpeciesSet;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EvolutionRandomizer extends Randomizer {

    public EvolutionRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeEvolutions() {
        boolean similarStrength = settings.isEvosSimilarStrength();
        boolean sameType = settings.isEvosSameTyping();
        boolean limitToThreeStages = settings.isEvosMaxThreeStages();
        boolean forceChange = settings.isEvosForceChange();
        boolean forceGrowth = settings.isEvosForceGrowth();
        boolean noConvergence = settings.isEvosNoConvergence();

        boolean banIrregularAltFormes = settings.isBanIrregularAltFormes();
        boolean abilitiesAreRandomized = settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE;

        boolean evolveEveryLevel = settings.getEvolutionsMod() == Settings.EvolutionsMod.RANDOM_EVERY_LEVEL;
        randomizeEvolutions(similarStrength, sameType, limitToThreeStages, forceChange, forceGrowth, noConvergence,
                banIrregularAltFormes, abilitiesAreRandomized, evolveEveryLevel);
        changesMade = true;
    }

    private void randomizeEvolutions(boolean similarStrength, boolean sameType, boolean limitToThreeStages,
                                     boolean forceChange, boolean forceGrowth, boolean noConvergence,
                                     boolean banIrregularAltFormes, boolean abilitiesAreRandomized,
                                     boolean evolveEveryLevel) {

        SpeciesSet pokemonPool = rSpecService.getSpecies(false,
                romHandler.altFormesCanHaveDifferentEvolutions(), false);
        SpeciesSet banned = new SpeciesSet(rSpecService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            banned.addAll(rSpecService.getAbilityDependentFormes());
        }
        if (banIrregularAltFormes) {
            banned.addAll(romHandler.getIrregularFormes());
        }

        new InnerRandomizer(pokemonPool, banned, similarStrength, sameType, limitToThreeStages, noConvergence,
                forceChange, forceGrowth, evolveEveryLevel)
                .randomizeEvolutions();
    }

    private class InnerRandomizer {

        private static final int MAX_TRIES = 1000;
        private static final int DEFAULT_STAGE_LIMIT = 10;

        private final boolean similarStrength;
        private final boolean sameType;
        private final int stageLimit;
        private final boolean noConvergence;
        private final boolean forceChange;
        private final boolean forceGrowth;
        private final boolean evolveEveryLevel;

        private final SpeciesSet pokemonPool;
        private final SpeciesSet banned;

        private Map<Species, List<Evolution>> allOriginalEvos;

        public InnerRandomizer(SpeciesSet pokemonPool, SpeciesSet banned,
                               boolean similarStrength, boolean sameType,
                               boolean limitToThreeStages, boolean noConvergence,
                               boolean forceChange, boolean forceGrowth,
                               boolean evolveEveryLevel) {
            this.pokemonPool = pokemonPool;
            this.banned = banned;
            this.similarStrength = similarStrength;
            this.sameType = sameType;
            this.stageLimit = limitToThreeStages ? 3 : DEFAULT_STAGE_LIMIT;
            this.noConvergence = noConvergence;
            this.forceChange = forceChange;
            this.forceGrowth = forceGrowth;
            this.evolveEveryLevel = evolveEveryLevel;
            if (evolveEveryLevel && similarStrength) {
                throw new IllegalArgumentException("Can't use evolveEveryLevel and similarStrength together.");
            }
            if (evolveEveryLevel && limitToThreeStages) {
                throw new IllegalArgumentException("Can't use evolveEveryLevel and limitToThreeStages together.");
            }
            if (evolveEveryLevel && forceGrowth) {
                throw new IllegalArgumentException("Can't use evolveEveryLevel and forceGrowth together.");
            }
        }

        public void randomizeEvolutions() {
            allOriginalEvos = cacheOriginalEvolutions();

            boolean succeeded = false;
            int tries = 0;
            while (!succeeded && tries < MAX_TRIES) {
                succeeded = randomizeEvolutionsInner();
                tries++;
            }
            if (tries == MAX_TRIES) {
                if (settings.isStandardizeEXPCurves()) {
                    throw new RandomizationException("Could not randomize Evolutions in " + MAX_TRIES + " tries.");
                } else {
                    throw new RandomizationException("Could not randomize Evolutions in " + MAX_TRIES + " tries." +
                            " Try using the \"Standardize EXP Curves\" option.");
                }
            }
        }

        private boolean randomizeEvolutionsInner() {
            clearEvolutions();

            // TODO: iterating through this in a random order would be better
            for (Species from : pokemonPool) {
                List<Evolution> originalEvos = getOriginalEvos(from);
                for (Evolution evo : originalEvos) {
                    SpeciesSet possible = findPossibleReplacements(from, evo);
                    if (possible.isEmpty()) {
                        return false;
                    }
                    Species picked = similarStrength ? possible.getRandomSimilarStrengthSpecies(evo.getTo(), random)
                            : possible.getRandomSpecies(random);

                    Evolution newEvo = prepareNewEvolution(from, evo, picked);
                    from.getEvolutionsFrom().add(newEvo);
                    picked.getEvolutionsTo().add(newEvo);
                }
            }
            return true;
        }

        private Map<Species, List<Evolution>> cacheOriginalEvolutions() {
            Map<Species, List<Evolution>> originalEvos = new HashMap<>();
            for (Species pk : pokemonPool) {
                originalEvos.put(pk, new ArrayList<>(pk.getEvolutionsFrom()));
            }
            return originalEvos;
        }

        private void clearEvolutions() {
            for (Species pk : pokemonPool) {
                pk.getEvolutionsFrom().clear();
                pk.getEvolutionsTo().clear();
            }
        }

        private List<Evolution> getOriginalEvos(Species from) {
            if (evolveEveryLevel) {
                // A list containing a single dummy object; ensures we always go through all Pokemon exactly once.
                // "originalEvos" of course becomes a misnomer here, and because it is but a dummy object,
                // it should NEVER be used except for iteration.
                return Collections.singletonList(new Evolution(from, from, EvolutionType.LEVEL, 0));
            } else {
                return allOriginalEvos.get(from);
            }
        }

        private Evolution prepareNewEvolution(Species from, Evolution evo, Species picked) {
            Evolution newEvo;
            if (evolveEveryLevel) {
                newEvo = new Evolution(from, picked, EvolutionType.LEVEL, 1);
            } else {
                newEvo = new Evolution(from, picked, evo.getType(), evo.getExtraInfo());
            }
            if (newEvo.getType() == EvolutionType.LEVEL_FEMALE_ESPURR) {
                newEvo.setType(EvolutionType.LEVEL_FEMALE_ONLY);
            }
            newEvo.setForme(picked.getRandomCosmeticFormeNumber(random));
            return newEvo;
        }

        private SpeciesSet findPossibleReplacements(Species from, Evolution evo) {
            List<Predicate<Species>> filters = new ArrayList<>();
            filters.add(to -> !banned.contains(to));
            filters.add(to -> !to.equals(from));
            filters.add(to -> to.getGrowthCurve().equals(from.getGrowthCurve()));
            filters.add(to -> !isAlreadyChosenAsOtherSplitEvo(from, to));

            if (!evolveEveryLevel) {
                filters.add(to -> !createsCycle(from, to));
                filters.add(to -> !breaksStageLimit(from, to));
            }
            if (noConvergence) {
                filters.add(to -> to.getEvolutionsTo().isEmpty());
            }
            if (forceChange) {
                filters.add(to -> !isAnOriginalEvo(from, to));
            }
            if (forceGrowth) {
                filters.add(to -> to.getBSTForPowerLevels() > from.getBSTForPowerLevels());
            }
            if (sameType) {
                if (from.getNumber() == SpeciesIDs.eevee && !evolveEveryLevel) {
                    filters.add(to -> to.hasSharedType(evo.getTo()));
                } else {
                    filters.add(to -> to.hasSharedType(from));
                }
            }

            Predicate<Species> combinedFilter = to -> {
                for (Predicate<Species> filter : filters) {
                    if (!filter.test(to)) return false;
                }
                return true;
            };
            return pokemonPool.filter(combinedFilter);
        }

        private boolean isAlreadyChosenAsOtherSplitEvo(Species from, Species to) {
            return from.getEvolutionsFrom().stream().map(Evolution::getTo).collect(Collectors.toList()).contains(to);
        }

        /**
         * Check whether adding an evolution from one Pokemon to another will cause
         * an evolution cycle.
         *
         * @param from Pokemon that is evolving
         * @param to   Pokemon to evolve to
         * @return True if there is an evolution cycle, else false
         */
        private boolean createsCycle(Species from, Species to) {
            Evolution tempEvo = new Evolution(from, to, EvolutionType.NONE, 0);
            from.getEvolutionsFrom().add(tempEvo);
            Set<Species> visited = new HashSet<>();
            Set<Species> recStack = new HashSet<>();
            boolean recur = isCyclic(from, visited, recStack);
            from.getEvolutionsFrom().remove(tempEvo);
            return recur;
        }

        private boolean isCyclic(Species pk, Set<Species> visited, Set<Species> recStack) {
            if (!visited.contains(pk)) {
                visited.add(pk);
                recStack.add(pk);
                for (Evolution ev : pk.getEvolutionsFrom()) {
                    if (!visited.contains(ev.getTo()) && isCyclic(ev.getTo(), visited, recStack)) {
                        return true;
                    } else if (recStack.contains(ev.getTo())) {
                        return true;
                    }
                }
            }
            recStack.remove(pk);
            return false;
        }

        private boolean breaksStageLimit(Species from, Species to) {
            int maxFrom = numPreEvolutions(from, stageLimit);
            int maxTo = numEvolutions(to, stageLimit);
            return maxFrom + maxTo + 2 > stageLimit;
        }

        // Return the max depth of pre-evolutions a Pokemon has
        private int numPreEvolutions(Species pk, int maxInterested) {
            return numPreEvolutions(pk, 0, maxInterested);
        }

        private int numPreEvolutions(Species pk, int depth, int maxInterested) {
            if (pk.getEvolutionsTo().isEmpty()) {
                return 0;
            }
            if (depth == maxInterested - 1) {
                return 1;
            }
            int maxPreEvos = 0;
            for (Evolution ev : pk.getEvolutionsTo()) {
                maxPreEvos = Math.max(maxPreEvos, numPreEvolutions(ev.getFrom(), depth + 1, maxInterested) + 1);
            }
            return maxPreEvos;
        }

        private int numEvolutions(Species pk, int maxInterested) {
            return numEvolutions(pk, 0, maxInterested);
        }

        private int numEvolutions(Species pk, int depth, int maxInterested) {
            if (pk.getEvolutionsFrom().isEmpty()) {
                // looks ahead to see if an evo MUST be given to this Pokemon in the future
                return allOriginalEvos.get(pk).isEmpty() ? 0 : 1;
            }
            if (depth == maxInterested - 1) {
                return 1;
            }
            int maxEvos = 0;
            for (Evolution ev : pk.getEvolutionsFrom()) {
                maxEvos = Math.max(maxEvos, numEvolutions(ev.getTo(), depth + 1, maxInterested) + 1);
            }
            return maxEvos;
        }

        private boolean isAnOriginalEvo(Species from, Species to) {
            return allOriginalEvos.get(from).stream().map(Evolution::getTo).collect(Collectors.toList()).contains(to);
        }
    }

}