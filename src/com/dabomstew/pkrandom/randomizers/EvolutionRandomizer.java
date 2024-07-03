package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.Species;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkrandom.pokemon.Evolution;
import com.dabomstew.pkrandom.pokemon.EvolutionType;
import com.dabomstew.pkrandom.pokemon.Pokemon;
import com.dabomstew.pkrandom.pokemon.PokemonSet;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

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
        rPokeService.setRestrictions(settings);

        PokemonSet pokemonPool = rPokeService.getPokemon(false,
                romHandler.altFormesCanHaveDifferentEvolutions(), false);
        PokemonSet banned = new PokemonSet(rPokeService.getBannedFormesForPlayerPokemon());
        if (!abilitiesAreRandomized) {
            banned.addAll(rPokeService.getAbilityDependentFormes());
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

        private final PokemonSet pokemonPool;
        private final PokemonSet banned;

        private Map<Pokemon, List<Evolution>> allOriginalEvos;

        public InnerRandomizer(PokemonSet pokemonPool, PokemonSet banned,
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
                throw new RandomizationException("Could not randomize Evolutions in " + MAX_TRIES + " tries.");
            }
        }

        private boolean randomizeEvolutionsInner() {
            clearEvolutions();

            // TODO: iterating through this in a random order would be better
            for (Pokemon from : pokemonPool) {
                List<Evolution> originalEvos = getOriginalEvos(from);
                for (Evolution evo : originalEvos) {
                    PokemonSet possible = findPossibleReplacements(from, evo);
                    if (possible.isEmpty()) {
                        return false;
                    }
                    Pokemon picked = similarStrength ? possible.getRandomSimilarStrengthPokemon(evo.getTo(), random)
                            : possible.getRandomPokemon(random);

                    Evolution newEvo = prepareNewEvolution(from, evo, picked);
                    from.getEvolutionsFrom().add(newEvo);
                    picked.getEvolutionsTo().add(newEvo);
                }
            }
            return true;
        }

        private Map<Pokemon, List<Evolution>> cacheOriginalEvolutions() {
            Map<Pokemon, List<Evolution>> originalEvos = new HashMap<>();
            for (Pokemon pk : pokemonPool) {
                originalEvos.put(pk, new ArrayList<>(pk.getEvolutionsFrom()));
            }
            return originalEvos;
        }

        private void clearEvolutions() {
            for (Pokemon pk : pokemonPool) {
                pk.getEvolutionsFrom().clear();
                pk.getEvolutionsTo().clear();
            }
        }

        private List<Evolution> getOriginalEvos(Pokemon from) {
            if (evolveEveryLevel) {
                // A list containing a single dummy object; ensures we always go through all Pokemon exactly once.
                // "originalEvos" of course becomes a misnomer here, and because it is but a dummy object,
                // it should NEVER be used except for iteration.
                return Collections.singletonList(new Evolution(from, from, EvolutionType.LEVEL, 0));
            } else {
                return allOriginalEvos.get(from);
            }
        }

        private Evolution prepareNewEvolution(Pokemon from, Evolution evo, Pokemon picked) {
            Evolution newEvo;
            if (evolveEveryLevel) {
                newEvo = new Evolution(from, picked, EvolutionType.LEVEL, 1);
                newEvo.setLevel(1);
            } else {
                newEvo = new Evolution(from, picked, evo.getType(), evo.getExtraInfo());
            }
            if (newEvo.getType() == EvolutionType.LEVEL_FEMALE_ESPURR) {
                newEvo.setType(EvolutionType.LEVEL_FEMALE_ONLY);
            }
            pickCosmeticForme(picked, newEvo);
            return newEvo;
        }

        private void pickCosmeticForme(Pokemon picked, Evolution newEvo) {
            boolean checkCosmetics = true;
            if (picked.getFormeNumber() > 0) {
                newEvo.setForme(picked.getFormeNumber());
                checkCosmetics = false;
            }
            if (checkCosmetics && newEvo.getTo().getCosmeticForms() > 0) {
                newEvo.setForme(newEvo.getTo().getCosmeticFormNumber(random.nextInt(newEvo.getTo().getCosmeticForms())));
            } else if (!checkCosmetics && picked.getCosmeticForms() > 0) {
                newEvo.setForme(newEvo.getForme() + picked.getCosmeticFormNumber(random.nextInt(picked.getCosmeticForms())));
            }
        }

        private PokemonSet findPossibleReplacements(Pokemon from, Evolution evo) {
            List<Predicate<Pokemon>> filters = new ArrayList<>();
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
                if (from.getNumber() == Species.eevee && !evolveEveryLevel) {
                    filters.add(to -> to.hasSharedType(evo.getTo()));
                } else {
                    filters.add(to -> to.hasSharedType(from));
                }
            }

            Predicate<Pokemon> combinedFilter = to -> {
                for (Predicate<Pokemon> filter : filters) {
                    if (!filter.test(to)) return false;
                }
                return true;
            };
            return pokemonPool.filter(combinedFilter);
        }

        private boolean isAlreadyChosenAsOtherSplitEvo(Pokemon from, Pokemon to) {
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
        private boolean createsCycle(Pokemon from, Pokemon to) {
            Evolution tempEvo = new Evolution(from, to, EvolutionType.NONE, 0);
            from.getEvolutionsFrom().add(tempEvo);
            Set<Pokemon> visited = new HashSet<>();
            Set<Pokemon> recStack = new HashSet<>();
            boolean recur = isCyclic(from, visited, recStack);
            from.getEvolutionsFrom().remove(tempEvo);
            return recur;
        }

        private boolean isCyclic(Pokemon pk, Set<Pokemon> visited, Set<Pokemon> recStack) {
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

        private boolean breaksStageLimit(Pokemon from, Pokemon to) {
            int maxFrom = numPreEvolutions(from, stageLimit);
            int maxTo = numEvolutions(to, stageLimit);
            return maxFrom + maxTo + 2 > stageLimit;
        }

        // Return the max depth of pre-evolutions a Pokemon has
        private int numPreEvolutions(Pokemon pk, int maxInterested) {
            return numPreEvolutions(pk, 0, maxInterested);
        }

        private int numPreEvolutions(Pokemon pk, int depth, int maxInterested) {
            if (pk.getEvolutionsTo().size() == 0) {
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

        private int numEvolutions(Pokemon pk, int maxInterested) {
            return numEvolutions(pk, 0, maxInterested);
        }

        private int numEvolutions(Pokemon pk, int depth, int maxInterested) {
            if (pk.getEvolutionsFrom().size() == 0) {
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

        private boolean isAnOriginalEvo(Pokemon from, Pokemon to) {
            boolean isAnOriginalEvo = allOriginalEvos.get(from).stream().map(Evolution::getTo).collect(Collectors.toList()).contains(to);
            // Hard-coded Cosmoem case, since the other-version evolution doesn't actually
            // exist within the game's data, but we don't want Cosmoem to evolve into Lunala in Sun, still.
            if (from.getNumber() == Species.cosmoem) {
                isAnOriginalEvo |= to.getNumber() == Species.solgaleo || to.getNumber() == Species.lunala;
            }
            return isAnOriginalEvo;
        }
    }

}