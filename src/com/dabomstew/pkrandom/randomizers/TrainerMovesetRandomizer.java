package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.AbilityIDs;
import com.dabomstew.pkrandom.constants.GlobalConstants;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class TrainerMovesetRandomizer extends Randomizer {

    /**
     * Returns whether a TrainerMovesetRandomizer can be used on games of the given generation.
     */
    public static boolean hasSupport(int generation) {
        // For some unknown reason, only Gen 3+ are supported by this class.
        // Might be due to abilities, might be due to ROM space concerns.
        // TODO: give Gen1+2 support, and remove this method
        return generation >= 3;
    }

    private Map<Integer, List<MoveLearnt>> allLevelUpMoves;
    private Map<Integer, List<Integer>> allEggMoves;
    private Map<Species, boolean[]> allTMCompat, allTutorCompat;
    private List<Integer> allTMMoves, allTutorMoves;

    public TrainerMovesetRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeTrainerMovesets() {
        boolean isCyclicEvolutions = settings.getEvolutionsMod() == Settings.EvolutionsMod.RANDOM_EVERY_LEVEL;
        boolean doubleBattleMode = settings.isDoubleBattleMode();

        List<Trainer> trainers = romHandler.getTrainers();

        for (Trainer t : trainers) {
            t.setPokemonHaveCustomMoves(true);
            System.out.println(t);

            for (TrainerPokemon tp : t.pokemon) {
                tp.resetMoves = false;

                List<Move> movesAtLevel = getMoveSelectionPoolAtLevel(tp, isCyclicEvolutions);

                movesAtLevel = trimMoveList(tp, movesAtLevel, doubleBattleMode);

                if (movesAtLevel.isEmpty()) {
                    continue;
                }

                MoveWeights weights = getInitialMoveWeights(t, tp, movesAtLevel);
                if (weights == null) continue;
                List<Move> distinctMoveList;
                int movesLeft;

                // Pick moves

                List<Move> pickedMoves = new ArrayList<>();

                for (int i = 1; i <= 4; i++) {
                    Move move;
                    List<Move> pickFrom;

                    if (i == 1) {
                        pickFrom = weights.movesAtLevel
                                .stream()
                                .filter(mv -> mv.isGoodDamaging(romHandler.getPerfectAccuracy()))
                                .collect(Collectors.toList());
                        if (pickFrom.isEmpty()) {
                            pickFrom = weights.movesAtLevel;
                        }
                    } else {
                        pickFrom = weights.movesAtLevel;
                    }

                    if (i == 4) {
                        List<Move> requiresOtherMove = weights.movesAtLevel
                                .stream()
                                .filter(mv -> GlobalConstants.requiresOtherMove.contains(mv.number))
                                .distinct().collect(Collectors.toList());

                        for (Move dependentMove : requiresOtherMove) {
                            boolean hasRequiredMove = false;
                            for (Move requiredMove : MoveSynergy.requiresOtherMove(dependentMove, weights.movesAtLevel)) {
                                if (pickedMoves.contains(requiredMove)) {
                                    hasRequiredMove = true;
                                    break;
                                }
                            }
                            if (!hasRequiredMove) {
                                weights.movesAtLevel.removeAll(Collections.singletonList(dependentMove));
                            }
                        }
                    }

                    move = pickFrom.get(random.nextInt(pickFrom.size()));
                    pickedMoves.add(move);

                    if (i == 4) {
                        break;
                    }

                    weights.movesAtLevel.removeAll(Collections.singletonList(move));

                    weights.movesAtLevel.removeAll(MoveSynergy.getHardMoveAntiSynergy(move, weights.movesAtLevel));

                    distinctMoveList = weights.movesAtLevel.stream().distinct().collect(Collectors.toList());
                    movesLeft = distinctMoveList.size();

                    if (movesLeft <= (4 - i)) {
                        pickedMoves.addAll(distinctMoveList);
                        break;
                    }

                    List<Move> hardMoveSynergyList = MoveSynergy.getMoveSynergy(
                            move,
                            weights.movesAtLevel,
                            romHandler.generationOfPokemon());
                    Collections.shuffle(hardMoveSynergyList, random);
                    for (int j = 0; j < weights.hardMoveBias * hardMoveSynergyList.size(); j++) {
                        int k = j % hardMoveSynergyList.size();
                        weights.movesAtLevel.add(hardMoveSynergyList.get(k));
                    }

                    List<Move> softMoveSynergyList = MoveSynergy.getSoftMoveSynergy(
                            move,
                            weights.movesAtLevel,
                            romHandler.getTypeTable());
                    Collections.shuffle(softMoveSynergyList, random);
                    for (int j = 0; j < weights.softMoveBias * softMoveSynergyList.size(); j++) {
                        int k = j % softMoveSynergyList.size();
                        weights.movesAtLevel.add(softMoveSynergyList.get(k));
                    }

                    List<Move> softMoveAntiSynergyList = MoveSynergy.getSoftMoveAntiSynergy(move, weights.movesAtLevel);
                    Collections.shuffle(softMoveAntiSynergyList, random);
                    for (int j = 0; j < weights.softMoveAntiBias * softMoveAntiSynergyList.size(); j++) {
                        distinctMoveList = weights.movesAtLevel.stream().distinct().collect(Collectors.toList());
                        if (distinctMoveList.size() <= (4 - i)) {
                            break;
                        }
                        int k = j % softMoveAntiSynergyList.size();
                        weights.movesAtLevel.remove(softMoveAntiSynergyList.get(k));
                    }

                    distinctMoveList = weights.movesAtLevel.stream().distinct().collect(Collectors.toList());
                    movesLeft = distinctMoveList.size();

                    if (movesLeft <= (4 - i)) {
                        pickedMoves.addAll(distinctMoveList);
                        break;
                    }
                }

                System.out.println("PiM=" + pickedMoves.stream().map(m -> m.name).collect(Collectors.toList()));
                System.out.println("MAL=" + movesAtLevel.stream().distinct().map(m -> m.name).collect(Collectors.toList()));
                int movesPicked = pickedMoves.size();

                for (int i = 0; i < 4; i++) {
                    if (i < movesPicked) {
                        tp.moves[i] = pickedMoves.get(i).number;
                    } else {
                        tp.moves[i] = 0;
                    }
                }
            }
        }
        romHandler.setTrainers(trainers);
        changesMade = true;
    }

    private MoveWeights getInitialMoveWeights(Trainer t, TrainerPokemon tp, List<Move> availableMoves) {
        double trainerTypeModifier = 1;
        if (t.isImportant()) {
            trainerTypeModifier = 1.5;
        } else if (t.isBoss()) {
            trainerTypeModifier = 2;
        }
        double movePoolSizeModifier = availableMoves.size() / 10.0;
        double bonusModifier = trainerTypeModifier * movePoolSizeModifier;

        double atkSpatkRatioModifier = 0.75;
        double stabMoveBias = 0.25 * bonusModifier;
        double hardAbilityMoveBias = 1 * bonusModifier;
        double softAbilityMoveBias = 0.5 * bonusModifier;
        double statBias = 0.5 * bonusModifier;
        double softMoveBias = 0.25 * bonusModifier;
        double hardMoveBias = 1 * bonusModifier;
        double softMoveAntiBias = 0.5;

        // Add bias for STAB

        Species pk = romHandler.getAltFormeOfSpecies(tp.species, tp.forme);

        List<Move> stabMoves = new ArrayList<>(availableMoves)
                .stream()
                .filter(mv -> mv.type == pk.getPrimaryType(false) && mv.category != MoveCategory.STATUS)
                .collect(Collectors.toList());
        Collections.shuffle(stabMoves, random);

        for (int i = 0; i < stabMoveBias * stabMoves.size(); i++) {
            int j = i % stabMoves.size();
            availableMoves.add(stabMoves.get(j));
        }

        if (pk.getSecondaryType(false) != null) {
            stabMoves = new ArrayList<>(availableMoves)
                    .stream()
                    .filter(mv -> mv.type == pk.getSecondaryType(false) && mv.category != MoveCategory.STATUS)
                    .collect(Collectors.toList());
            Collections.shuffle(stabMoves, random);

            for (int i = 0; i < stabMoveBias * stabMoves.size(); i++) {
                int j = i % stabMoves.size();
                availableMoves.add(stabMoves.get(j));
            }
        }

        // Hard ability/move synergy

        List<Move> abilityMoveSynergyList = MoveSynergy.getHardAbilityMoveSynergy(
                romHandler.getAbilityForTrainerPokemon(tp),
                pk.getPrimaryType(false),
                pk.getSecondaryType(false),
                availableMoves,
                romHandler.generationOfPokemon(),
                romHandler.getPerfectAccuracy());
        Collections.shuffle(abilityMoveSynergyList, random);
        for (int i = 0; i < hardAbilityMoveBias * abilityMoveSynergyList.size(); i++) {
            int j = i % abilityMoveSynergyList.size();
            availableMoves.add(abilityMoveSynergyList.get(j));
        }

        // Soft ability/move synergy

        List<Move> softAbilityMoveSynergyList = MoveSynergy.getSoftAbilityMoveSynergy(
                romHandler.getAbilityForTrainerPokemon(tp),
                availableMoves,
                pk.getPrimaryType(false),
                pk.getSecondaryType(false));

        Collections.shuffle(softAbilityMoveSynergyList, random);
        for (int i = 0; i < softAbilityMoveBias * softAbilityMoveSynergyList.size(); i++) {
            int j = i % softAbilityMoveSynergyList.size();
            availableMoves.add(softAbilityMoveSynergyList.get(j));
        }

        // Soft ability/move anti-synergy

        List<Move> softAbilityMoveAntiSynergyList = MoveSynergy.getSoftAbilityMoveAntiSynergy(
                romHandler.getAbilityForTrainerPokemon(tp), availableMoves);
        List<Move> withoutSoftAntiSynergy = new ArrayList<>(availableMoves);
        for (Move mv : softAbilityMoveAntiSynergyList) {
            withoutSoftAntiSynergy.remove(mv);
        }
        if (!withoutSoftAntiSynergy.isEmpty()) {
            availableMoves = withoutSoftAntiSynergy;
        }

        List<Move> distinctMoveList = availableMoves.stream().distinct().collect(Collectors.toList());
        int movesLeft = distinctMoveList.size();

        if (movesLeft <= 4) {

            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.moves[i] = distinctMoveList.get(i).number;
                } else {
                    tp.moves[i] = 0;
                }
            }
            return null;
        }

        // Stat/move synergy

        List<Move> statSynergyList = MoveSynergy.getStatMoveSynergy(pk, availableMoves);
        Collections.shuffle(statSynergyList, random);
        for (int i = 0; i < statBias * statSynergyList.size(); i++) {
            int j = i % statSynergyList.size();
            availableMoves.add(statSynergyList.get(j));
        }

        // Stat/move anti-synergy

        List<Move> statAntiSynergyList = MoveSynergy.getStatMoveAntiSynergy(pk, availableMoves);
        List<Move> withoutStatAntiSynergy = new ArrayList<>(availableMoves);
        for (Move mv : statAntiSynergyList) {
            withoutStatAntiSynergy.remove(mv);
        }
        if (!withoutStatAntiSynergy.isEmpty()) {
            availableMoves = withoutStatAntiSynergy;
        }

        distinctMoveList = availableMoves.stream().distinct().collect(Collectors.toList());
        movesLeft = distinctMoveList.size();

        if (movesLeft <= 4) {

            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.moves[i] = distinctMoveList.get(i).number;
                } else {
                    tp.moves[i] = 0;
                }
            }
            return null;
        }

        // Add bias for atk/spatk ratio

        double atkSpatkRatio = (double) pk.getAttack() / (double) pk.getSpatk();
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

        List<Move> physicalMoves = new ArrayList<>(availableMoves)
                .stream()
                .filter(mv -> mv.category == MoveCategory.PHYSICAL).collect(Collectors.toList());
        List<Move> specialMoves = new ArrayList<>(availableMoves)
                .stream()
                .filter(mv -> mv.category == MoveCategory.SPECIAL).collect(Collectors.toList());

        if (atkSpatkRatio < 1 && !specialMoves.isEmpty()) {
            atkSpatkRatio = 1 / atkSpatkRatio;
            double acceptedRatio = atkSpatkRatioModifier * atkSpatkRatio;
            int additionalMoves = (int) (physicalMoves.size() * acceptedRatio) - specialMoves.size();
            for (int i = 0; i < additionalMoves; i++) {
                Move mv = specialMoves.get(random.nextInt(specialMoves.size()));
                availableMoves.add(mv);
            }
        } else if (!physicalMoves.isEmpty()) {
            double acceptedRatio = atkSpatkRatioModifier * atkSpatkRatio;
            int additionalMoves = (int) (specialMoves.size() * acceptedRatio) - physicalMoves.size();
            for (int i = 0; i < additionalMoves; i++) {
                Move mv = physicalMoves.get(random.nextInt(physicalMoves.size()));
                availableMoves.add(mv);
            }
        }
        MoveWeights weights = new MoveWeights(availableMoves, softMoveBias, hardMoveBias, softMoveAntiBias);
        return weights;
    }

    private static class MoveWeights {
        public final List<Move> movesAtLevel;
        public final double softMoveBias;
        public final double hardMoveBias;
        public final double softMoveAntiBias;

        public MoveWeights(List<Move> movesAtLevel, double softMoveBias, double hardMoveBias, double softMoveAntiBias) {
            this.movesAtLevel = movesAtLevel;
            this.softMoveBias = softMoveBias;
            this.hardMoveBias = hardMoveBias;
            this.softMoveAntiBias = softMoveAntiBias;
        }
    }

    private List<Move> trimMoveList(TrainerPokemon tp, List<Move> movesAtLevel, boolean doubleBattleMode) {
        int movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.moves[i] = movesAtLevel.get(i).number;
                } else {
                    tp.moves[i] = 0;
                }
            }
            return new ArrayList<>();
        }

        movesAtLevel = movesAtLevel
                .stream()
                .filter(mv -> !GlobalConstants.uselessMoves.contains(mv.number) &&
                        (doubleBattleMode || !GlobalConstants.doubleBattleMoves.contains(mv.number)))
                .collect(Collectors.toList());

        movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.moves[i] = movesAtLevel.get(i).number;
                } else {
                    tp.moves[i] = 0;
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
                    tp.moves[i] = movesAtLevel.get(i).number;
                } else {
                    tp.moves[i] = 0;
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
                    tp.moves[i] = movesAtLevel.get(i).number;
                } else {
                    tp.moves[i] = 0;
                }
            }
            return new ArrayList<>();
        }

        // Remove hard ability anti-synergy moves

        List<Move> withoutHardAntiSynergy = new ArrayList<>(movesAtLevel);
        withoutHardAntiSynergy.removeAll(MoveSynergy.getHardAbilityMoveAntiSynergy(
                romHandler.getAbilityForTrainerPokemon(tp),
                movesAtLevel));

        if (!withoutHardAntiSynergy.isEmpty()) {
            movesAtLevel = withoutHardAntiSynergy;
        }

        movesLeft = movesAtLevel.size();

        if (movesLeft <= 4) {
            for (int i = 0; i < 4; i++) {
                if (i < movesLeft) {
                    tp.moves[i] = movesAtLevel.get(i).number;
                } else {
                    tp.moves[i] = 0;
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
                                mv.hitRatio >= mv2.hitRatio &&
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

        // This method unexpectedly (to me) includes random elements,
        // to only allow egg/prevo/tm/move tutor moves *sometimes*.
        // This fills two purposes, in that every lvl. 5 Rattata
        // having Thunderbolt access may both lead to a too difficult
        // and too "samey" gameplay experience.
        // However, "better movesets" *is* meant as a challenge option,
        // and though sameyness is discouraged, there are other parts
        // of the code for randomly selecting moves from the move pool.
        // It should not be done here.
        // TODO: mitigate; move this functionality or just remove it.

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
        List<Move> moveSelectionPoolAtLevel = allLevelUpMoves.get(romHandler.getAltFormeOfSpecies(tp.species, tp.forme).getNumber())
                .stream()
                .filter(ml -> (ml.level <= tp.level && ml.level != 0) || (ml.level == 0 && tp.level >= 30))
                .map(ml -> moves.get(ml.move))
                .distinct()
                .collect(Collectors.toList());

        // Pre-Evo Moves
        if (!cyclicEvolutions) {
            Species preEvo;
            if (romHandler.altFormesCanHaveDifferentEvolutions()) {
                preEvo = romHandler.getAltFormeOfSpecies(tp.species, tp.forme);
            } else {
                preEvo = tp.species;
            }
            while (!preEvo.getEvolutionsTo().isEmpty()) {
                preEvo = preEvo.getEvolutionsTo().get(0).getFrom();
                moveSelectionPoolAtLevel.addAll(allLevelUpMoves.get(preEvo.getNumber())
                        .stream()
                        .filter(ml -> ml.level <= tp.level)
                        .filter(ml -> this.random.nextDouble() < preEvoMoveProbability)
                        .map(ml -> moves.get(ml.move))
                        .distinct().collect(Collectors.toList()));
            }
        }

        // TM Moves
        boolean[] tmCompat = allTMCompat.get(romHandler.getAltFormeOfSpecies(tp.species, tp.forme));
        for (int tmMove: allTMMoves) {
            if (tmCompat[allTMMoves.indexOf(tmMove) + 1]) {
                Move thisMove = moves.get(tmMove);
                if (thisMove.power > 1 && tp.level * 3 > thisMove.power * thisMove.hitCount &&
                        this.random.nextDouble() < tmMoveProbability) {
                    moveSelectionPoolAtLevel.add(thisMove);
                } else if ((thisMove.power <= 1 && this.random.nextInt(100) < tp.level) ||
                        this.random.nextInt(200) < tp.level) {
                    moveSelectionPoolAtLevel.add(thisMove);
                }
            }
        }

        // Move Tutor Moves
        if (romHandler.hasMoveTutors()) {
            boolean[] tutorCompat = allTutorCompat.get(romHandler.getAltFormeOfSpecies(tp.species, tp.forme));
            for (int tutorMove: allTutorMoves) {
                if (tutorCompat[allTutorMoves.indexOf(tutorMove) + 1]) {
                    Move thisMove = moves.get(tutorMove);
                    if (thisMove.power > 1 && tp.level * 3 > thisMove.power * thisMove.hitCount &&
                            this.random.nextDouble() < tutorMoveProbability) {
                        moveSelectionPoolAtLevel.add(thisMove);
                    } else if ((thisMove.power <= 1 && this.random.nextInt(100) < tp.level) ||
                            this.random.nextInt(200) < tp.level) {
                        moveSelectionPoolAtLevel.add(thisMove);
                    }
                }
            }
        }

        // Egg Moves
        if (!cyclicEvolutions) {
            Species firstEvo;
            if (romHandler.altFormesCanHaveDifferentEvolutions()) {
                firstEvo = romHandler.getAltFormeOfSpecies(tp.species, tp.forme);
            } else {
                firstEvo = tp.species;
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
