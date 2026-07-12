package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.AbilityIDs;
import com.uprfvx.romio.constants.GlobalConstants;
import com.uprfvx.romio.constants.MoveIDs;
import com.uprfvx.romio.gamedata.*;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class TrainerMovesetRandomizer extends Randomizer {

    private Map<Integer, List<MoveLearnt>> allLevelUpMoves;
    private Map<Integer, List<Integer>> allEggMoves;
    private Map<Species, boolean[]> allTMCompat, allTutorCompat;
    private List<Integer> allTMMoves, allTutorMoves;
    
    private final boolean hasAbilities;

    public TrainerMovesetRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
        this.hasAbilities = romHandler.abilitiesPerSpecies() != 0;
    }

    public void randomizeTrainerMovesets() {
        boolean isCyclicEvolutions = settings.getEvolutionsMod() == Settings.EvolutionsMod.RANDOM_EVERY_LEVEL;
        boolean isOnlyMultiBattles = settings.getBattleStyle().isOnlyMultiBattles();
        boolean betterBossMovesets = settings.isBetterBossTrainerMovesets();
        boolean betterImportantMovesets = settings.isBetterImportantTrainerMovesets();
        boolean betterRegularMovesets = settings.isBetterRegularTrainerMovesets();

        List<Trainer> trainers = romHandler.getTrainers().stream()
                .filter(t -> (t.isBoss() && betterBossMovesets) ||
                        (t.isImportant() && betterImportantMovesets) ||
                        (t.isRegular() && betterRegularMovesets))
                .filter(t -> !t.shouldNotGetBuffs())
                .collect(Collectors.toList());

        for (Trainer t : trainers) {

            for (TrainerPokemon tp : t.getPokemon()) {
                tp.setResetMoves(false);

                List<Move> movesAtLevel = getMoveSelectionPoolAtLevel(tp, isCyclicEvolutions);

                Species pk = tp.getSpecies();
                int ability = hasAbilities ? romHandler.getAbilityForTrainerPokemon(tp) : 0;

                // Strip intrinsically-redundant situational status moves (weather / Trick Room) up front, so they
                // cannot slip in via a small movepool or the trim / fallback paths that skip the per-slot gate.
                movesAtLevel.removeIf(mv -> isSituationalStatusRedundant(mv, pk, ability));

                // Some moves are dead weight unless an "enabler" move is also known: Sleep Talk and Snore only act
                // while the user sleeps (Rest), and Spit Up and Swallow consume Stockpile counters. If a dependent's
                // enabler is not even in the pool, strip it before it can claim a slot; the post-pass below then
                // guarantees the dependency even when the enabler is available but goes unpicked.
                stripUnsupportedDependentMoves(movesAtLevel);

                if (movesAtLevel.isEmpty()) {
                    // No custom moves to offer (e.g. a low-level rival starter whose selection pool is empty).
                    // Rather than leave the Pokemon moveless (all-zero slots), let the game assign it its natural
                    // level-up moveset at ROM-write time.
                    tp.setResetMoves(true);
                    continue;
                }

                movesAtLevel = trimMoveList(tp, movesAtLevel, isOnlyMultiBattles);

                if (movesAtLevel.isEmpty()) {
                    // trimMoveList already wrote a small (<=4) moveset directly into tp; nothing more to build.
                    continue;
                }

                int level = tp.getLevel();
                // Boss & Important trainers get the full STAB + coverage + status + wildcard structure;
                // Regular trainers skip the status slot (STAB + coverage + wildcard + wildcard).
                boolean includeStatus = t.isBoss() || t.isImportant();

                List<Move> distinctPool = movesAtLevel.stream().distinct().collect(Collectors.toList());
                List<Move> picked = new ArrayList<>();

                if (distinctPool.size() <= 4) {
                    // Too few candidates to be choosy - just take what is available.
                    picked.addAll(distinctPool);
                } else {
                    // Slot 1: a STAB attacking move, base power scaled to the Pokemon's level.
                    Move stab = pickStabMove(pk, distinctPool, level, picked);
                    if (stab == null) {
                        stab = pickBestDamaging(distinctPool, picked);
                    }
                    if (stab != null) {
                        picked.add(stab);
                    }

                    // Slot 2: a coverage move that hits what the STAB move is walled by.
                    Move coverage = pickCoverageMove(pk, distinctPool, level,
                            stab == null ? null : stab.type, picked);
                    if (coverage == null) {
                        coverage = pickBestDamaging(distinctPool, picked);
                    }
                    if (coverage != null) {
                        picked.add(coverage);
                    }

                    // Slot 3 (bosses/important only): a non-redundant status move.
                    if (includeStatus) {
                        Move status = pickStatusMove(pk, ability, distinctPool, picked);
                        if (status == null) {
                            status = pickBestDamaging(distinctPool, picked);
                        }
                        if (status != null) {
                            picked.add(status);
                        }
                    }

                    // Remaining slots: wildcard picks reusing the existing synergy-weighted logic.
                    fillWildcardMoves(t, tp, pk, ability, distinctPool, picked);
                }

                // Enabler-dependency guarantee across every slot: drop any dependent whose enabler did not make the
                // final set (Snore/Sleep Talk without Rest, Spit Up/Swallow without Stockpile), then backfill with
                // the next-best damaging move.
                enforceEnablerDependencies(picked, distinctPool);

                for (int i = 0; i < 4; i++) {
                    tp.getMoves()[i] = i < picked.size() ? picked.get(i).number : 0;
                }
            }
        }
        changesMade = true;
    }

    // ===== Role-based moveset construction (custom Better Movesets redesign) ==========================
    // Boss/Important: STAB + Coverage + Status + Wildcard. Regular: STAB + Coverage + Wildcard + Wildcard.
    // Any slot that cannot be filled falls back to the next-best damaging move, so every mon gets 4 moves.

    private static final double STAB_LEVEL_POWER_MULTIPLIER = 3.0;
    private static final double COVERAGE_BLIND_SPOT_BONUS = 2.0;
    private static final int TRICK_ROOM_MAX_SPEED = 60;

    // Weather moves are only worth running if the Pokemon benefits from that weather, and are pointless
    // (redundant) if the Pokemon's own ability already sets it.
    private static final Set<Integer> RAIN_BENEFIT_ABILITIES = Set.of(
            AbilityIDs.swiftSwim, AbilityIDs.rainDish, AbilityIDs.drySkin, AbilityIDs.hydration);
    private static final Set<Integer> RAIN_SETTER_ABILITIES = Set.of(
            AbilityIDs.drizzle, AbilityIDs.primordialSea);
    private static final Set<Integer> SUN_BENEFIT_ABILITIES = Set.of(
            AbilityIDs.chlorophyll, AbilityIDs.solarPower, AbilityIDs.leafGuard, AbilityIDs.flowerGift, AbilityIDs.harvest);
    private static final Set<Integer> SUN_SETTER_ABILITIES = Set.of(
            AbilityIDs.drought, AbilityIDs.desolateLand);
    private static final Set<Integer> SAND_BENEFIT_ABILITIES = Set.of(
            AbilityIDs.sandVeil, AbilityIDs.sandRush, AbilityIDs.sandForce);
    private static final Set<Integer> SAND_SETTER_ABILITIES = Set.of(AbilityIDs.sandStream);
    private static final Set<Integer> HAIL_BENEFIT_ABILITIES = Set.of(
            AbilityIDs.snowCloak, AbilityIDs.iceBody, AbilityIDs.slushRush);
    private static final Set<Integer> HAIL_SETTER_ABILITIES = Set.of(AbilityIDs.snowWarning);

    // Moves that accomplish nothing unless an "enabler" move is also known: Snore and Sleep Talk only act while the
    // user sleeps (Rest), and Spit Up and Swallow consume Stockpile counters. Each may be selected only when its
    // enabler is present in the same moveset. Maps dependent move -> its required enabler.
    private static final Map<Integer, Integer> DEPENDENT_MOVE_ENABLERS = Map.of(
            MoveIDs.snore, MoveIDs.rest,
            MoveIDs.sleepTalk, MoveIDs.rest,
            MoveIDs.spitUp, MoveIDs.stockpile,
            MoveIDs.swallow, MoveIDs.stockpile);

    // A move that can fill an attacking slot (STAB or coverage): a real, level-appropriate damaging move.
    private boolean isAttackSlotEligible(Move mv, int level) {
        if (mv == null || mv.category == MoveCategory.STATUS || mv.power <= 1) {
            return false;
        }
        if (mv.power * mv.hitCount > level * STAB_LEVEL_POWER_MULTIPLIER) {
            return false;
        }
        if (GlobalConstants.badStrongMoves.contains(mv.number)) {
            return false;
        }
        return mv.isGoodDamaging(romHandler.getPerfectAccuracy())
                || GlobalConstants.goodWeakMoves.contains(mv.number);
    }

    // Slot 1: a STAB attacking move within the level power band, weighted toward stronger moves.
    private Move pickStabMove(Species pk, List<Move> pool, int level, List<Move> exclude) {
        Type t1 = pk.getPrimaryType(false);
        Type t2 = pk.getSecondaryType(false);
        List<Move> candidates = pool.stream()
                .filter(mv -> !exclude.contains(mv))
                .filter(mv -> mv.type == t1 || (t2 != null && mv.type == t2))
                .filter(mv -> isAttackSlotEligible(mv, level))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            // Fall back to the strongest STAB move even if it breaks the level ceiling.
            candidates = pool.stream()
                    .filter(mv -> !exclude.contains(mv))
                    .filter(mv -> mv.category != MoveCategory.STATUS && mv.power > 1)
                    .filter(mv -> mv.type == t1 || (t2 != null && mv.type == t2))
                    .collect(Collectors.toList());
        }
        return weightedPick(candidates, mv -> mv.power * mv.hitCount);
    }

    // Slot 2: a coverage move hitting a type that resists the STAB move; SE-gated, blind-spot-weighted.
    private Move pickCoverageMove(Species pk, List<Move> pool, int level, Type stabType, List<Move> exclude) {
        if (stabType == null) {
            return null;
        }
        TypeTable tt = romHandler.getTypeTable();
        if (!tt.getTypes().contains(stabType)) {
            return null;
        }
        Set<Type> holes = new HashSet<>(tt.notVeryEffectiveWhenAttacking(stabType));
        holes.addAll(tt.immuneWhenAttacking(stabType));
        Set<Type> blindSpots = computeBlindSpots(pk, tt, holes);

        List<Move> eligible = pool.stream()
                .filter(mv -> !exclude.contains(mv))
                .filter(mv -> mv.type != stabType)
                .filter(mv -> isAttackSlotEligible(mv, level))
                .filter(mv -> tt.getTypes().contains(mv.type))
                .filter(mv -> coversAny(tt, mv.type, holes, false))
                .collect(Collectors.toList());
        if (eligible.isEmpty()) {
            return null;
        }
        // Prefer super-effective coverage; only fall back to merely-neutral coverage if none is SE.
        List<Move> superEffective = eligible.stream()
                .filter(mv -> coversAny(tt, mv.type, holes, true))
                .collect(Collectors.toList());
        List<Move> chosenSet = superEffective.isEmpty() ? eligible : superEffective;
        return weightedPick(chosenSet, mv -> {
            double weight = mv.power * mv.hitCount;
            if (coversAny(tt, mv.type, blindSpots, false)) {
                weight *= COVERAGE_BLIND_SPOT_BONUS;
            }
            return weight;
        });
    }

    // The Pokemon's true offensive blind spots: types resisting BOTH of its types (== STAB holes if mono-type).
    private Set<Type> computeBlindSpots(Species pk, TypeTable tt, Set<Type> stabHoles) {
        Type t1 = pk.getPrimaryType(false);
        Type t2 = pk.getSecondaryType(false);
        if (t2 == null || t1 == t2) {
            return stabHoles;
        }
        Set<Type> h1 = new HashSet<>(tt.notVeryEffectiveWhenAttacking(t1));
        h1.addAll(tt.immuneWhenAttacking(t1));
        Set<Type> h2 = new HashSet<>(tt.notVeryEffectiveWhenAttacking(t2));
        h2.addAll(tt.immuneWhenAttacking(t2));
        h1.retainAll(h2);
        return h1;
    }

    // Does a move of attackType hit at least one of the given defending types for the required strength?
    private boolean coversAny(TypeTable tt, Type attackType, Set<Type> defenders, boolean superEffectiveOnly) {
        for (Type d : defenders) {
            if (!tt.getTypes().contains(d)) {
                continue;
            }
            Effectiveness eff = tt.getEffectiveness(attackType, d);
            if (superEffectiveOnly) {
                if (eff == Effectiveness.DOUBLE) {
                    return true;
                }
            } else if (eff == Effectiveness.NEUTRAL || eff == Effectiveness.DOUBLE) {
                return true;
            }
        }
        return false;
    }

    // Slot 3: a non-redundant good status move, synergy-weighted to suit the Pokemon.
    private Move pickStatusMove(Species pk, int ability, List<Move> pool, List<Move> picked) {
        List<Move> candidates = pool.stream()
                .filter(mv -> !picked.contains(mv))
                .filter(mv -> mv.category == MoveCategory.STATUS)
                .filter(mv -> GlobalConstants.goodStatusMoves.contains(mv.number))
                .filter(mv -> !GlobalConstants.uselessMoves.contains(mv.number))
                .filter(mv -> !isRedundantStatusMove(mv, pk, ability, picked))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return null;
        }
        Set<Integer> synergy = MoveSynergy.getStatMoveSynergy(pk, candidates)
                .stream().map(mv -> mv.number).collect(Collectors.toSet());
        return weightedPick(candidates, mv -> synergy.contains(mv.number) ? 3.0 : 1.0);
    }

    // Global fallback for any unfillable slot: the strongest available damaging move.
    private Move pickBestDamaging(List<Move> pool, List<Move> exclude) {
        List<Move> candidates = pool.stream()
                .filter(mv -> !exclude.contains(mv))
                .filter(mv -> mv.category != MoveCategory.STATUS && mv.power > 1)
                .collect(Collectors.toList());
        return weightedPick(candidates, mv -> mv.power * mv.hitCount);
    }

    // Situational status moves whose payoff can never apply to this Pokemon based on intrinsic traits only
    // (type / ability / speed). Safe to strip from the movepool before any slot logic runs, so they cannot
    // leak in via a small movepool, the trim path, or a damaging fallback.
    private boolean isSituationalStatusRedundant(Move mv, Species pk, int ability) {
        switch (mv.number) {
            case MoveIDs.rainDance:
                if (RAIN_SETTER_ABILITIES.contains(ability)) {
                    return true;
                }
                return !(hasType(pk, Type.WATER) || RAIN_BENEFIT_ABILITIES.contains(ability));
            case MoveIDs.sunnyDay:
                if (SUN_SETTER_ABILITIES.contains(ability)) {
                    return true;
                }
                return !(hasType(pk, Type.FIRE) || SUN_BENEFIT_ABILITIES.contains(ability));
            case MoveIDs.sandstorm:
                if (SAND_SETTER_ABILITIES.contains(ability)) {
                    return true;
                }
                return !(hasType(pk, Type.ROCK) || hasType(pk, Type.GROUND) || hasType(pk, Type.STEEL)
                        || SAND_BENEFIT_ABILITIES.contains(ability));
            case MoveIDs.hail:
                if (HAIL_SETTER_ABILITIES.contains(ability)) {
                    return true;
                }
                return !(hasType(pk, Type.ICE) || HAIL_BENEFIT_ABILITIES.contains(ability));
            case MoveIDs.trickRoom:
                return pk.getSpeed() > TRICK_ROOM_MAX_SPEED;
            default:
                return false;
        }
    }

    // A status move is redundant when its situational payoff cannot apply, or a stat boost it grants is wasted.
    private boolean isRedundantStatusMove(Move mv, Species pk, int ability, List<Move> picked) {
        if (isSituationalStatusRedundant(mv, pk, ability)) {
            return true;
        }

        // Stat-boost gate (inclusive): an Attack-only booster needs >=1 physical move already picked;
        // a Sp.Atk-only booster needs >=1 special move. Boosters raising both are never gated.
        boolean boostsAtk = raisesUserAttack(mv);
        boolean boostsSpAtk = raisesUserSpecialAttack(mv);
        if (boostsAtk && !boostsSpAtk) {
            return !hasCategory(picked, MoveCategory.PHYSICAL);
        }
        if (boostsSpAtk && !boostsAtk) {
            return !hasCategory(picked, MoveCategory.SPECIAL);
        }
        return false;
    }

    // Remaining slots: reuse the existing synergy-weighted pick + anti-synergy removal, but never pick a
    // redundant status move (so e.g. Spinarak never rolls Sunny Day and powers up the Fire moves it fears).
    private void fillWildcardMoves(Trainer t, TrainerPokemon tp, Species pk, int ability,
                                   List<Move> pool, List<Move> picked) {
        if (picked.size() >= 4) {
            return;
        }

        List<Move> working = new ArrayList<>();
        for (Move mv : pool) {
            if (picked.contains(mv)) {
                continue;
            }
            if (mv.category == MoveCategory.STATUS && isRedundantStatusMove(mv, pk, ability, picked)) {
                continue;
            }
            working.add(mv);
        }

        double trainerTypeModifier = t.isImportant() ? 1.5 : (t.isBoss() ? 2 : 1);
        double bonusModifier = trainerTypeModifier * (working.size() / 10.0);
        double stabMoveBias = 0.25 * bonusModifier;
        double hardAbilityMoveBias = 1 * bonusModifier;
        double softAbilityMoveBias = 0.5 * bonusModifier;
        double statBias = 0.5 * bonusModifier;
        double softMoveBias = 0.25 * bonusModifier;
        double hardMoveBias = 1 * bonusModifier;
        double softMoveAntiBias = 0.5;

        // Re-apply the existing pool biases (STAB, ability, stat, atk/spatk ratio) to the wildcard pool.
        List<Move> stab1 = working.stream()
                .filter(mv -> mv.type == pk.getPrimaryType(false) && mv.category != MoveCategory.STATUS)
                .collect(Collectors.toList());
        addCopies(working, stab1, stabMoveBias);
        if (pk.getSecondaryType(false) != null) {
            List<Move> stab2 = working.stream()
                    .filter(mv -> mv.type == pk.getSecondaryType(false) && mv.category != MoveCategory.STATUS)
                    .collect(Collectors.toList());
            addCopies(working, stab2, stabMoveBias);
        }
        if (hasAbilities) {
            working = updateMovesConsideringAbilitySynergies(tp, pk, working, hardAbilityMoveBias, softAbilityMoveBias);
        }
        working = updateMovesConsideringStatSynergies(pk, working, statBias);

        double atkSpatkRatioModifier = 0.75;
        double atkSpatkRatio = getAtkSpatkRatio(tp, pk);
        List<Move> physicalMoves = working.stream()
                .filter(mv -> mv.category == MoveCategory.PHYSICAL).collect(Collectors.toList());
        List<Move> specialMoves = working.stream()
                .filter(mv -> mv.category == MoveCategory.SPECIAL).collect(Collectors.toList());
        if (atkSpatkRatio < 1 && !specialMoves.isEmpty()) {
            atkSpatkRatio = 1 / atkSpatkRatio;
            int additionalMoves = (int) (physicalMoves.size() * atkSpatkRatioModifier * atkSpatkRatio) - specialMoves.size();
            for (int i = 0; i < additionalMoves; i++) {
                working.add(specialMoves.get(random.nextInt(specialMoves.size())));
            }
        } else if (!physicalMoves.isEmpty()) {
            int additionalMoves = (int) (specialMoves.size() * atkSpatkRatioModifier * atkSpatkRatio) - physicalMoves.size();
            for (int i = 0; i < additionalMoves; i++) {
                working.add(physicalMoves.get(random.nextInt(physicalMoves.size())));
            }
        }

        // Seed move-to-move synergy from the already-chosen role moves, so wildcards complement them.
        for (Move rolePick : new ArrayList<>(picked)) {
            working.removeAll(MoveSynergy.getHardMoveAntiSynergy(rolePick, working));
            addCopies(working, MoveSynergy.getMoveSynergy(rolePick, working, romHandler.generationOfPokemon()), hardMoveBias);
            addCopies(working, MoveSynergy.getSoftMoveSynergy(rolePick, working, romHandler.getTypeTable()), softMoveBias);
        }

        while (picked.size() < 4) {
            List<Move> distinct = eligibleWildcards(working, pk, ability, picked);
            int slotsLeft = 4 - picked.size();
            if (distinct.isEmpty()) {
                break;
            }
            if (distinct.size() <= slotsLeft) {
                picked.addAll(distinct);
                break;
            }
            // On the last slot, drop moves that need a partner move we did not pick (e.g. Spit Up).
            if (slotsLeft == 1) {
                for (Move dependent : new ArrayList<>(distinct)) {
                    if (!GlobalConstants.requiresOtherMove.contains(dependent.number)) {
                        continue;
                    }
                    boolean hasRequired = false;
                    for (Move required : MoveSynergy.requiresOtherMove(dependent, working)) {
                        if (picked.contains(required)) {
                            hasRequired = true;
                            break;
                        }
                    }
                    if (!hasRequired) {
                        working.removeAll(Collections.singletonList(dependent));
                    }
                }
                distinct = eligibleWildcards(working, pk, ability, picked);
                if (distinct.isEmpty()) {
                    break;
                }
                if (distinct.size() <= slotsLeft) {
                    picked.addAll(distinct);
                    break;
                }
            }

            Move move = distinct.get(random.nextInt(distinct.size()));
            picked.add(move);
            if (picked.size() >= 4) {
                break;
            }

            working.removeAll(Collections.singletonList(move));
            working.removeAll(MoveSynergy.getHardMoveAntiSynergy(move, working));
            addCopies(working, MoveSynergy.getMoveSynergy(move, working, romHandler.generationOfPokemon()), hardMoveBias);
            addCopies(working, MoveSynergy.getSoftMoveSynergy(move, working, romHandler.getTypeTable()), softMoveBias);

            List<Move> softAnti = MoveSynergy.getSoftMoveAntiSynergy(move, working);
            Collections.shuffle(softAnti, random);
            int softAntiCount = (int) (softMoveAntiBias * softAnti.size());
            for (int j = 0; j < softAntiCount; j++) {
                if (eligibleWildcards(working, pk, ability, picked).size() <= (4 - picked.size())) {
                    break;
                }
                working.remove(softAnti.get(j % softAnti.size()));
            }
        }
    }

    // The distinct, currently-pickable wildcard moves (excludes already-picked and redundant status moves).
    private List<Move> eligibleWildcards(List<Move> working, Species pk, int ability, List<Move> picked) {
        return working.stream()
                .filter(mv -> !picked.contains(mv))
                .filter(mv -> !(mv.category == MoveCategory.STATUS && isRedundantStatusMove(mv, pk, ability, picked)))
                .distinct()
                .collect(Collectors.toList());
    }

    // Adds `factor * toAdd.size()` copies of the given moves into the pool (higher weight in later picks).
    private void addCopies(List<Move> pool, List<Move> toAdd, double factor) {
        if (toAdd.isEmpty() || factor <= 0) {
            return;
        }
        Collections.shuffle(toAdd, random);
        int count = (int) (factor * toAdd.size());
        for (int i = 0; i < count; i++) {
            pool.add(toAdd.get(i % toAdd.size()));
        }
    }

    // Weighted-random selection over a list, probability proportional to weightFn (>=0). Null if empty.
    private Move weightedPick(List<Move> candidates, ToDoubleFunction<Move> weightFn) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        double total = 0;
        for (Move mv : candidates) {
            total += Math.max(0.0, weightFn.applyAsDouble(mv));
        }
        if (total <= 0) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        double r = random.nextDouble() * total;
        for (Move mv : candidates) {
            r -= Math.max(0.0, weightFn.applyAsDouble(mv));
            if (r <= 0) {
                return mv;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private static boolean hasType(Species pk, Type type) {
        return pk.getPrimaryType(false) == type || pk.getSecondaryType(false) == type;
    }

    // Removes enabler-dependent moves whose enabler is absent from the pool, so they can never claim a slot.
    private static void stripUnsupportedDependentMoves(List<Move> pool) {
        Set<Integer> present = new HashSet<>();
        for (Move mv : pool) {
            present.add(mv.number);
        }
        pool.removeIf(mv -> {
            Integer enabler = DEPENDENT_MOVE_ENABLERS.get(mv.number);
            return enabler != null && !present.contains(enabler);
        });
    }

    // Final cross-slot guarantee: drop any dependent whose enabler did not make the chosen set, then backfill to
    // four with the next-best damaging move. The backfill pool excludes every dependent, or pickBestDamaging could
    // re-select the move just removed (Snore and Spit Up are themselves valid damaging moves).
    private void enforceEnablerDependencies(List<Move> picked, List<Move> distinctPool) {
        Set<Integer> pickedNumbers = new HashSet<>();
        for (Move mv : picked) {
            pickedNumbers.add(mv.number);
        }
        boolean removed = picked.removeIf(mv -> {
            Integer enabler = DEPENDENT_MOVE_ENABLERS.get(mv.number);
            return enabler != null && !pickedNumbers.contains(enabler);
        });
        if (!removed) {
            return;
        }
        List<Move> backfillPool = distinctPool.stream()
                .filter(mv -> !DEPENDENT_MOVE_ENABLERS.containsKey(mv.number))
                .collect(Collectors.toList());
        Move fill;
        while (picked.size() < 4 && (fill = pickBestDamaging(backfillPool, picked)) != null) {
            picked.add(fill);
        }
    }

    private static boolean hasCategory(List<Move> moves, MoveCategory category) {
        for (Move mv : moves) {
            if (mv.category == category) {
                return true;
            }
        }
        return false;
    }

    private static boolean raisesOwnStat(Move mv) {
        return mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER
                || mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER;
    }

    private static boolean raisesUserAttack(Move mv) {
        return raisesOwnStat(mv) && (mv.hasSpecificStatChange(StatChangeType.ATTACK, true)
                || mv.hasSpecificStatChange(StatChangeType.ALL, true));
    }

    private static boolean raisesUserSpecialAttack(Move mv) {
        return raisesOwnStat(mv) && (mv.hasSpecificStatChange(StatChangeType.SPECIAL_ATTACK, true)
                || mv.hasSpecificStatChange(StatChangeType.SPECIAL, true)
                || mv.hasSpecificStatChange(StatChangeType.ALL, true));
    }

    private List<Move> updateMovesConsideringAbilitySynergies(TrainerPokemon tp, Species pk, List<Move> movesAtLevel, double hardAbilityMoveBias, double softAbilityMoveBias) {
        // Hard ability/move synergy

        List<Move> abilityMoveSynergyList = MoveSynergy.getHardAbilityMoveSynergy(
                romHandler.getAbilityForTrainerPokemon(tp),
                pk.getPrimaryType(false),
                pk.getSecondaryType(false),
                movesAtLevel,
                romHandler.generationOfPokemon(),
                romHandler.getPerfectAccuracy());
        Collections.shuffle(abilityMoveSynergyList, random);
        for (int i = 0; i < hardAbilityMoveBias * abilityMoveSynergyList.size(); i++) {
            int j = i % abilityMoveSynergyList.size();
            movesAtLevel.add(abilityMoveSynergyList.get(j));
        }

        // Soft ability/move synergy

        List<Move> softAbilityMoveSynergyList = MoveSynergy.getSoftAbilityMoveSynergy(
                romHandler.getAbilityForTrainerPokemon(tp),
                movesAtLevel,
                pk.getPrimaryType(false),
                pk.getSecondaryType(false));

        Collections.shuffle(softAbilityMoveSynergyList, random);
        for (int i = 0; i < softAbilityMoveBias * softAbilityMoveSynergyList.size(); i++) {
            int j = i % softAbilityMoveSynergyList.size();
            movesAtLevel.add(softAbilityMoveSynergyList.get(j));
        }

        // Soft ability/move anti-synergy

        List<Move> softAbilityMoveAntiSynergyList = MoveSynergy.getSoftAbilityMoveAntiSynergy(
                romHandler.getAbilityForTrainerPokemon(tp), movesAtLevel);
        List<Move> withoutSoftAntiSynergy = new ArrayList<>(movesAtLevel);
        for (Move mv : softAbilityMoveAntiSynergyList) {
            withoutSoftAntiSynergy.remove(mv);
        }
        if (!withoutSoftAntiSynergy.isEmpty()) {
            movesAtLevel = withoutSoftAntiSynergy;
        }
        return movesAtLevel;
    }

    private List<Move> updateMovesConsideringStatSynergies(Species pk, List<Move> movesAtLevel, double statBias) {
        // Stat/move synergy

        List<Move> statSynergyList = MoveSynergy.getStatMoveSynergy(pk, movesAtLevel);
        Collections.shuffle(statSynergyList, random);
        for (int i = 0; i < statBias * statSynergyList.size(); i++) {
            int j = i % statSynergyList.size();
            movesAtLevel.add(statSynergyList.get(j));
        }

        // Stat/move anti-synergy

        List<Move> statAntiSynergyList = MoveSynergy.getStatMoveAntiSynergy(pk, movesAtLevel);
        List<Move> withoutStatAntiSynergy = new ArrayList<>(movesAtLevel);
        for (Move mv : statAntiSynergyList) {
            withoutStatAntiSynergy.remove(mv);
        }
        if (!withoutStatAntiSynergy.isEmpty()) {
            movesAtLevel = withoutStatAntiSynergy;
        }
        return movesAtLevel;
    }

    private double getAtkSpatkRatio(TrainerPokemon tp, Species pk) {
        int spatk = romHandler.generationOfPokemon() == 1 ? pk.getSpecial() : pk.getSpatk();
        double atkSpatkRatio = (double) pk.getAttack() / (double) spatk;
        if (hasAbilities) {
            switch (romHandler.getAbilityForTrainerPokemon(tp)) {
                case AbilityIDs.hugePower:
                case AbilityIDs.purePower:
                    atkSpatkRatio *= 2;
                    break;
                case AbilityIDs.hustle:
                case AbilityIDs.gorillaTactics:
                    atkSpatkRatio *= 1.5;
                    break;
                case AbilityIDs.moxie:
                    atkSpatkRatio *= 1.1;
                    break;
                case AbilityIDs.soulHeart:
                    atkSpatkRatio *= 0.9;
                    break;
            }
        }
        return atkSpatkRatio;
    }

    private List<Move> trimMoveList(TrainerPokemon tp, List<Move> movesAtLevel, boolean isMultiBattlesOnly) {
        int movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.getMoves()[i] = movesAtLevel.get(i).number;
                } else {
                    tp.getMoves()[i] = 0;
                }
            }
            return new ArrayList<>();
        }

        movesAtLevel = movesAtLevel
                .stream()
                .filter(mv -> !GlobalConstants.uselessMoves.contains(mv.number) &&
                        (isMultiBattlesOnly || !GlobalConstants.doubleBattleMoves.contains(mv.number)))
                .collect(Collectors.toList());

        movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.getMoves()[i] = movesAtLevel.get(i).number;
                } else {
                    tp.getMoves()[i] = 0;
                }
            }
            return new ArrayList<>();
        }

        List<Move> obsoletedMoves = getObsoleteMoves(movesAtLevel);

        // Remove obsoleted moves

        movesAtLevel.removeAll(obsoletedMoves);

        movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.getMoves()[i] = movesAtLevel.get(i).number;
                } else {
                    tp.getMoves()[i] = 0;
                }
            }
            return new ArrayList<>();
        }

        List<Move> requiresOtherMove = movesAtLevel
                .stream()
                .filter(mv -> GlobalConstants.requiresOtherMove.contains(mv.number)).collect(Collectors.toList());

        for (Move dependentMove : requiresOtherMove) {
            if (MoveSynergy.requiresOtherMove(dependentMove, movesAtLevel).isEmpty()) {
                movesAtLevel.remove(dependentMove);
            }
        }

        movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.getMoves()[i] = movesAtLevel.get(i).number;
                } else {
                    tp.getMoves()[i] = 0;
                }
            }
            return new ArrayList<>();
        }

        // Remove hard ability anti-synergy moves

        if (hasAbilities) {
            List<Move> withoutHardAntiSynergy = new ArrayList<>(movesAtLevel);
            withoutHardAntiSynergy.removeAll(MoveSynergy.getHardAbilityMoveAntiSynergy(
                    romHandler.getAbilityForTrainerPokemon(tp),
                    movesAtLevel));

            if (!withoutHardAntiSynergy.isEmpty()) {
                movesAtLevel = withoutHardAntiSynergy;
            }
        }

        movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.getMoves()[i] = movesAtLevel.get(i).number;
                } else {
                    tp.getMoves()[i] = 0;
                }
            }
            return new ArrayList<>();
        }
        return movesAtLevel;
    }

    private List<Move> getObsoleteMoves(List<Move> movesAtLevel) {
        List<Move> obsoletedMoves = new ArrayList<>();
        for (Move mv : movesAtLevel) {
            if (GlobalConstants.cannotObsoleteMoves.contains(mv.number)) {
                continue;
            }
            if (mv.power > 0) {
                List<Move> obsoleteThis = movesAtLevel
                        .stream()
                        .filter(mv2 -> !GlobalConstants.cannotBeObsoletedMoves.contains(mv2.number) &&
                                mv.type == mv2.type &&
                                ((((mv.statChangeMoveType == mv2.statChangeMoveType &&
                                        mv.statChanges[0].equals(mv2.statChanges[0])) ||
                                        (mv2.statChangeMoveType == StatChangeMoveType.NONE_OR_UNKNOWN &&
                                                mv.hasBeneficialStatChange())) &&
                                        mv.absorbPercent >= mv2.absorbPercent &&
                                        !mv.isChargeMove &&
                                        !mv.isRechargeMove) ||
                                        mv2.power * mv2.hitCount <= 30) &&
                                mv.hitratio >= mv2.hitratio &&
                                mv.category == mv2.category &&
                                mv.priority >= mv2.priority &&
                                mv2.power > 0 &&
                                mv.power * mv.hitCount > mv2.power * mv2.hitCount).collect(Collectors.toList());
//                for (Move obsoleted: obsoleteThis) {
//                    System.out.println(obsoleted.name + " obsoleted by " + mv.name);
//                }
                obsoletedMoves.addAll(obsoleteThis);
            } else if (mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER ||
                    mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_TARGET) {
                List<Move> obsoleteThis = new ArrayList<>();
                List<Move.StatChange> statChanges1 = new ArrayList<>();
                for (Move.StatChange sc : mv.statChanges) {
                    if (sc.type != StatChangeType.NONE) {
                        statChanges1.add(sc);
                    }
                }
                for (Move mv2 : movesAtLevel
                        .stream()
                        .filter(otherMv -> !otherMv.equals(mv) &&
                                otherMv.power <= 0 &&
                                otherMv.statChangeMoveType == mv.statChangeMoveType &&
                                (otherMv.statusType == mv.statusType ||
                                        otherMv.statusType == StatusType.NONE)).collect(Collectors.toList())) {
                    List<Move.StatChange> statChanges2 = new ArrayList<>();
                    for (Move.StatChange sc : mv2.statChanges) {
                        if (sc.type != StatChangeType.NONE) {
                            statChanges2.add(sc);
                        }
                    }
                    if (statChanges2.size() > statChanges1.size()) {
                        continue;
                    }
                    List<Move.StatChange> statChanges1Filtered = statChanges1
                            .stream()
                            .filter(sc -> !statChanges2.contains(sc)).collect(Collectors.toList());
                    statChanges2.removeAll(statChanges1);
                    if (!statChanges1Filtered.isEmpty() && statChanges2.isEmpty()) {
                        if (!GlobalConstants.cannotBeObsoletedMoves.contains(mv2.number)) {
                            obsoleteThis.add(mv2);
                        }
                        continue;
                    }
                    if (statChanges1Filtered.isEmpty() && statChanges2.isEmpty()) {
                        continue;
                    }
                    boolean maybeBetter = false;
                    for (Move.StatChange sc1 : statChanges1Filtered) {
                        boolean canStillBeBetter = false;
                        for (Move.StatChange sc2 : statChanges2) {
                            if (sc1.type == sc2.type) {
                                canStillBeBetter = true;
                                if ((mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER && sc1.stages > sc2.stages) ||
                                        (mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_TARGET && sc1.stages < sc2.stages)) {
                                    maybeBetter = true;
                                } else {
                                    canStillBeBetter = false;
                                }
                            }
                        }
                        if (!canStillBeBetter) {
                            maybeBetter = false;
                            break;
                        }
                    }
                    if (maybeBetter) {
                        if (!GlobalConstants.cannotBeObsoletedMoves.contains(mv2.number)) {
                            obsoleteThis.add(mv2);
                        }
                    }
                }
//                for (Move obsoleted : obsoleteThis) {
//                    System.out.println(obsoleted.name + " obsoleted by " + mv.name);
//                }
                obsoletedMoves.addAll(obsoleteThis);
            }
        }

        return obsoletedMoves.stream().distinct().collect(Collectors.toList());
    }

    private List<Move> getMoveSelectionPoolAtLevel(TrainerPokemon tp, boolean cyclicEvolutions) {

        List<Move> moves = romHandler.getMoves();
        double eggMoveProbability = 0.1;
        double preEvoMoveProbability = 0.5;
        double tmMoveProbability = 0.6;
        double tutorMoveProbability = 0.6;

        if (allLevelUpMoves == null) {
            allLevelUpMoves = romHandler.getMovesLearnt();
        }

        if (allEggMoves == null) {
            allEggMoves = romHandler.getEggMoves();
        }

        if (allTMCompat == null) {
            allTMCompat = romHandler.getTMHMCompatibility();
        }

        if (allTMMoves == null) {
            allTMMoves = romHandler.getTMMoves();
        }

        if (allTutorCompat == null && romHandler.hasMoveTutors()) {
            allTutorCompat = romHandler.getMoveTutorCompatibility();
        }

        if (allTutorMoves == null) {
            allTutorMoves = romHandler.getMoveTutorMoves();
        }

        // Level-up Moves
        List<Move> moveSelectionPoolAtLevel = allLevelUpMoves.get(tp.getSpecies().getNumber())
                .stream()
                .filter(ml -> (ml.level <= tp.getLevel() && ml.level != 0) || (ml.level == 0 && tp.getLevel() >= 30))
                .map(ml -> moves.get(ml.move))
                .distinct()
                .collect(Collectors.toList());

        // Pre-Evo Moves
        if (!cyclicEvolutions) {
            Species preEvo;
            if (romHandler.altFormesCanHaveDifferentEvolutions()) {
                preEvo = tp.getSpecies();
            } else {
                preEvo = tp.getSpecies().getBaseForme();
            }
            while (!preEvo.getEvolutionsTo().isEmpty()) {
                preEvo = preEvo.getEvolutionsTo().get(0).getFrom();
                moveSelectionPoolAtLevel.addAll(allLevelUpMoves.get(preEvo.getNumber())
                        .stream()
                        .filter(ml -> ml.level <= tp.getLevel())
                        .filter(_ -> this.random.nextDouble() < preEvoMoveProbability)
                        .map(ml -> moves.get(ml.move))
                        .distinct().toList());
            }
        }

        // TM Moves
        boolean[] tmCompat = allTMCompat.get(tp.getSpecies());
        for (int tmMove: allTMMoves) {
            if (tmCompat[allTMMoves.indexOf(tmMove) + 1]) {
                Move thisMove = moves.get(tmMove);
                if (thisMove.power > 1 && tp.getLevel() * 3 > thisMove.power * thisMove.hitCount &&
                        this.random.nextDouble() < tmMoveProbability) {
                    moveSelectionPoolAtLevel.add(thisMove);
                } else if ((thisMove.power <= 1 && this.random.nextInt(100) < tp.getLevel()) ||
                        this.random.nextInt(200) < tp.getLevel()) {
                    moveSelectionPoolAtLevel.add(thisMove);
                }
            }
        }

        // Move Tutor Moves
        if (romHandler.hasMoveTutors()) {
            boolean[] tutorCompat = allTutorCompat.get(tp.getSpecies());
            for (int tutorMove: allTutorMoves) {
                if (tutorCompat[allTutorMoves.indexOf(tutorMove) + 1]) {
                    Move thisMove = moves.get(tutorMove);
                    if (thisMove.power > 1 && tp.getLevel() * 3 > thisMove.power * thisMove.hitCount &&
                            this.random.nextDouble() < tutorMoveProbability) {
                        moveSelectionPoolAtLevel.add(thisMove);
                    } else if ((thisMove.power <= 1 && this.random.nextInt(100) < tp.getLevel()) ||
                            this.random.nextInt(200) < tp.getLevel()) {
                        moveSelectionPoolAtLevel.add(thisMove);
                    }
                }
            }
        }

        // Egg Moves
        if (!cyclicEvolutions) {
            Species firstEvo;
            if (romHandler.altFormesCanHaveDifferentEvolutions()) {
                firstEvo = tp.getSpecies();
            } else {
                firstEvo = tp.getSpecies().getBaseForme();
            }
            while (!firstEvo.getEvolutionsTo().isEmpty()) {
                firstEvo = firstEvo.getEvolutionsTo().get(0).getFrom();
            }
            if (allEggMoves.get(firstEvo.getNumber()) != null) {
                moveSelectionPoolAtLevel.addAll(allEggMoves.get(firstEvo.getNumber())
                        .stream()
                        .filter(egm -> this.random.nextDouble() < eggMoveProbability)
                        .map(moves::get).collect(Collectors.toList()));
            }
        }

        return moveSelectionPoolAtLevel.stream().distinct().collect(Collectors.toList());
    }
}
