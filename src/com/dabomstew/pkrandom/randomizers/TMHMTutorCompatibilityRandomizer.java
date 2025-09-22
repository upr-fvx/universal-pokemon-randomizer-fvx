package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveLearnt;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TMHMTutorCompatibilityRandomizer extends Randomizer {

    private boolean tmhmChangesMade;
    private boolean tutorChangesMade;

    public TMHMTutorCompatibilityRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    /**
     * Returns whether TM/HM compatibility has been changed.
     */
    public boolean isTMHMChangesMade() {
        return tmhmChangesMade;
    }

    /**
     * Returns whether Move Tutor compatibility has been changed.
     */
    public boolean isTutorChangesMade() {
        return tutorChangesMade;
    }

    public void randomizeTMHMCompatibility() {
        boolean preferSameType = settings.getTmsHmsCompatibilityMod() == Settings.TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE;
        boolean followEvolutions = settings.isTmsFollowEvolutions();

        // Get current compatibility
        // increase HM chances if required early on
        List<Integer> requiredEarlyOn = romHandler.getEarlyRequiredHMMoves();
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        List<Integer> tmHMs = new ArrayList<>(romHandler.getTMMoves());
        tmHMs.addAll(romHandler.getHMMoves());

        if (followEvolutions) {
            copyUpEvolutionsHelper.apply(true, false,
                    pk -> randomizePokemonMoveCompatibility(pk, compat.get(pk), tmHMs, requiredEarlyOn, preferSameType),
                    (evFrom, evTo, toMonIsFinalEvo) -> copyPokemonMoveCompatibilityUpEvolutions(evFrom, evTo,
                            compat.get(evFrom), compat.get(evTo), tmHMs, preferSameType));
        } else {
            for (Map.Entry<Species, boolean[]> compatEntry : compat.entrySet()) {
                randomizePokemonMoveCompatibility(compatEntry.getKey(), compatEntry.getValue(), tmHMs, requiredEarlyOn,
                        preferSameType);
            }
        }

        // Set the new compatibility
        romHandler.setTMHMCompatibility(compat);
        tmhmChangesMade = true;
    }

    private void randomizePokemonMoveCompatibility(Species pkmn, boolean[] moveCompatibilityFlags,
                                                   List<Integer> moveIDs, List<Integer> prioritizedMoves,
                                                   boolean preferSameType) {
        List<Move> moveData = romHandler.getMoves();
        for (int i = 1; i <= moveIDs.size(); i++) {
            int move = moveIDs.get(i - 1);
            Move mv = moveData.get(move);
            double probability = getMoveCompatibilityProbability(
                    pkmn,
                    mv,
                    prioritizedMoves.contains(move),
                    preferSameType
            );
            moveCompatibilityFlags[i] = (this.random.nextDouble() < probability);
        }
    }

    private void copyPokemonMoveCompatibilityUpEvolutions(Species evFrom, Species evTo, boolean[] prevCompatibilityFlags,
                                                          boolean[] toCompatibilityFlags, List<Integer> moveIDs,
                                                          boolean preferSameType) {
        List<Move> moveData = romHandler.getMoves();
        for (int i = 1; i <= moveIDs.size(); i++) {
            if (!prevCompatibilityFlags[i]) {
                // Slight chance to gain TM/HM compatibility for a move if not learned by an earlier evolution step
                // Without prefer same type: 25% chance
                // With prefer same type:    10% chance, 90% chance for a type new to this evolution
                int move = moveIDs.get(i - 1);
                Move mv = moveData.get(move);
                double probability = 0.25;
                if (preferSameType) {
                    probability = 0.1;
                    if (evTo.getPrimaryType(false).equals(mv.type)
                            && !evTo.getPrimaryType(false).equals(evFrom.getPrimaryType(false)) && !evTo.getPrimaryType(false).equals(evFrom.getSecondaryType(false))
                            || evTo.getSecondaryType(false) != null && evTo.getSecondaryType(false).equals(mv.type)
                            && !evTo.getSecondaryType(false).equals(evFrom.getSecondaryType(false)) && !evTo.getSecondaryType(false).equals(evFrom.getPrimaryType(false))) {
                        probability = 0.9;
                    }
                }
                toCompatibilityFlags[i] = (this.random.nextDouble() < probability);
            }
            else {
                toCompatibilityFlags[i] = prevCompatibilityFlags[i];
            }
        }
    }

    private double getMoveCompatibilityProbability(Species pkmn, Move mv, boolean requiredEarlyOn,
                                                   boolean preferSameType) {
        double probability = 0.5;
        if (preferSameType) {
            if (pkmn.getPrimaryType(false).equals(mv.type)
                    || (pkmn.getSecondaryType(false) != null && pkmn.getSecondaryType(false).equals(mv.type))) {
                probability = 0.9;
            } else if (mv.type != null && mv.type.equals(Type.NORMAL)) {
                probability = 0.5;
            } else {
                probability = 0.25;
            }
        }
        if (requiredEarlyOn) {
            probability = Math.min(1.0, probability * 1.8);
        }
        return probability;
    }

    public void fullTMHMCompatibility() {
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        for (Map.Entry<Species, boolean[]> compatEntry : compat.entrySet()) {
            boolean[] flags = compatEntry.getValue();
            for (int i = 1; i < flags.length; i++) {
                flags[i] = true;
            }
        }
        romHandler.setTMHMCompatibility(compat);
    }

    /**
     * if a pokemon learns a move in its moveset and there is a TM of that move, make sure that TM can be learned.
     */
    public void ensureTMCompatSanity() {
        //
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        Map<Integer, List<MoveLearnt>> movesets = romHandler.getMovesLearnt();
        List<Integer> tmMoves = romHandler.getTMMoves();
        for (Species pkmn : compat.keySet()) {
            List<MoveLearnt> moveset = movesets.get(pkmn.getNumber());
            boolean[] pkmnCompat = compat.get(pkmn);
            for (MoveLearnt ml : moveset) {
                if (tmMoves.contains(ml.move)) {
                    int tmIndex = tmMoves.indexOf(ml.move);
                    pkmnCompat[tmIndex + 1] = true;
                }
            }
        }
        romHandler.setTMHMCompatibility(compat);
        tmhmChangesMade = true;
    }

    public void ensureTMEvolutionSanity() {
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        // Don't do anything with the base, just copy upwards to ensure later evolutions
        // retain learn compatibility
        copyUpEvolutionsHelper.apply(true, true, pk -> {},
                (evFrom, evTo, toMonIsFinalEvo) -> {
                    boolean[] fromCompat = compat.get(evFrom);
                    boolean[] toCompat = compat.get(evTo);
                    for (int i = 1; i < toCompat.length; i++) {
                        toCompat[i] |= fromCompat[i];
                    }
                });
        romHandler.setTMHMCompatibility(compat);
        tmhmChangesMade = true;
    }

    public void fullHMCompatibility() {
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        int tmCount = romHandler.getTMCount();
        for (boolean[] flags : compat.values()) {
            for (int i = tmCount + 1; i < flags.length; i++) {
                flags[i] = true;
            }
        }

        // Set the new compatibility
        romHandler.setTMHMCompatibility(compat);
        tmhmChangesMade = true;
    }

    public void copyTMCompatibilityToCosmeticFormes() {
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();

        for (Map.Entry<Species, boolean[]> compatEntry : compat.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            if (pkmn.isActuallyCosmetic()) {
                boolean[] baseFlags = compat.get(pkmn.getBaseForme());
                for (int i = 1; i < flags.length; i++) {
                    flags[i] = baseFlags[i];
                }
            }
        }

        romHandler.setTMHMCompatibility(compat);
        tmhmChangesMade = true;
    }

    public void randomizeMoveTutorCompatibility() {
        boolean preferSameType = settings.getMoveTutorsCompatibilityMod() == Settings.MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE;
        boolean followEvolutions = settings.isTutorFollowEvolutions();

        if (!romHandler.hasMoveTutors()) {
            return;
        }
        // Get current compatibility
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        List<Integer> mts = romHandler.getMoveTutorMoves();

        // Empty list
        List<Integer> priorityTutors = new ArrayList<>();

        if (followEvolutions) {
            copyUpEvolutionsHelper.apply(true, true,
                    pk -> randomizePokemonMoveCompatibility(pk, compat.get(pk), mts, priorityTutors, preferSameType),
                    (evFrom, evTo, toMonIsFinalEvo) -> copyPokemonMoveCompatibilityUpEvolutions(evFrom, evTo,
                            compat.get(evFrom), compat.get(evTo), mts, preferSameType));
        }
        else {
            for (Map.Entry<Species, boolean[]> compatEntry : compat.entrySet()) {
                randomizePokemonMoveCompatibility(compatEntry.getKey(), compatEntry.getValue(), mts, priorityTutors, preferSameType);
            }
        }

        // Set the new compatibility
        romHandler.setMoveTutorCompatibility(compat);
        tutorChangesMade = true;
    }

    public void fullMoveTutorCompatibility() {
        if (!romHandler.hasMoveTutors()) {
            return;
        }
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        for (Map.Entry<Species, boolean[]> compatEntry : compat.entrySet()) {
            boolean[] flags = compatEntry.getValue();
            for (int i = 1; i < flags.length; i++) {
                flags[i] = true;
            }
        }
        romHandler.setMoveTutorCompatibility(compat);
        tutorChangesMade = true;
    }

    public void ensureMoveTutorCompatSanity() {
        if (!romHandler.hasMoveTutors()) {
            return;
        }
        // if a pokemon learns a move in its moveset
        // and there is a tutor of that move, make sure
        // that tutor can be learned.
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        Map<Integer, List<MoveLearnt>> movesets = romHandler.getMovesLearnt();
        List<Integer> mtMoves = romHandler.getMoveTutorMoves();
        for (Species pkmn : compat.keySet()) {
            List<MoveLearnt> moveset = movesets.get(pkmn.getNumber());
            boolean[] pkmnCompat = compat.get(pkmn);
            for (MoveLearnt ml : moveset) {
                if (mtMoves.contains(ml.move)) {
                    int mtIndex = mtMoves.indexOf(ml.move);
                    pkmnCompat[mtIndex + 1] = true;
                }
            }
        }
        romHandler.setMoveTutorCompatibility(compat);
        tutorChangesMade = true;
    }

    public void ensureMoveTutorEvolutionSanity() {
        if (!romHandler.hasMoveTutors()) {
            return;
        }
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        // Don't do anything with the base, just copy upwards to ensure later evolutions retain learn compatibility
        copyUpEvolutionsHelper.apply(true, true, pk -> {},
                (evFrom, evTo, toMonIsFinalEvo) -> {
                    boolean[] fromCompat = compat.get(evFrom);
                    boolean[] toCompat = compat.get(evTo);
                    for (int i = 1; i < toCompat.length; i++) {
                        toCompat[i] |= fromCompat[i];
                    }
                });
        romHandler.setMoveTutorCompatibility(compat);
        tutorChangesMade = true;
    }

    public void copyMoveTutorCompatibilityToCosmeticFormes() {
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();

        for (Map.Entry<Species, boolean[]> compatEntry : compat.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            if (pkmn.isActuallyCosmetic()) {
                boolean[] baseFlags = compat.get(pkmn.getBaseForme());
                for (int i = 1; i < flags.length; i++) {
                    flags[i] = baseFlags[i];
                }
            }
        }

        romHandler.setMoveTutorCompatibility(compat);
        tutorChangesMade = true;
    }
}
