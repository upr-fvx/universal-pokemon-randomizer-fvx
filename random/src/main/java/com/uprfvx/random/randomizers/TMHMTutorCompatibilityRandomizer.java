package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.MoveLearnt;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.Gen3RomHandler;
import com.uprfvx.romio.romhandlers.RomHandler;

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
        if (moveCompatibilityFlags == null || shouldSkipMoveCompatibilitySpecies(pkmn)) {
            return;
        }
        List<Move> moveData = romHandler.getMoves();
        for (int i = 1; i <= moveIDs.size() && i < moveCompatibilityFlags.length; i++) {
            int move = moveIDs.get(i - 1);
            if (!isValidMove(moveData, move)) {
                continue;
            }
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
        if (prevCompatibilityFlags == null || toCompatibilityFlags == null || shouldSkipMoveCompatibilitySpecies(evFrom)
                || shouldSkipMoveCompatibilitySpecies(evTo)) {
            return;
        }
        List<Move> moveData = romHandler.getMoves();
        for (int i = 1; i <= moveIDs.size() && i < prevCompatibilityFlags.length && i < toCompatibilityFlags.length; i++) {
            if (!prevCompatibilityFlags[i]) {
                // Slight chance to gain TM/HM compatibility for a move if not learned by an earlier evolution step
                // Without prefer same type: 25% chance
                // With prefer same type:    10% chance, 90% chance for a type new to this evolution
                int move = moveIDs.get(i - 1);
                if (!isValidMove(moveData, move)) {
                    continue;
                }
                Move mv = moveData.get(move);
                double probability = 0.25;
                if (preferSameType) {
                    probability = 0.1;
                    Type evToPrimary = evTo.getPrimaryType(false);
                    Type evToSecondary = evTo.getSecondaryType(false);
                    Type evFromPrimary = evFrom.getPrimaryType(false);
                    Type evFromSecondary = evFrom.getSecondaryType(false);
                    if (evToPrimary != null && evToPrimary.equals(mv.type)
                            && !evToPrimary.equals(evFromPrimary) && !evToPrimary.equals(evFromSecondary)
                            || evToSecondary != null && evToSecondary.equals(mv.type)
                            && !evToSecondary.equals(evFromSecondary) && !evToSecondary.equals(evFromPrimary)) {
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
        if (pkmn == null || mv == null) {
            return 0.0;
        }
        double probability = 0.5;
        if (preferSameType) {
            Type primaryType = pkmn.getPrimaryType(false);
            Type secondaryType = pkmn.getSecondaryType(false);
            if (primaryType != null && primaryType.equals(mv.type)
                    || (secondaryType != null && secondaryType.equals(mv.type))) {
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

    private boolean shouldSkipMoveCompatibilitySpecies(Species pkmn) {
        return hasExtendedBpreHackSpeciesPool() && (pkmn == null || pkmn.getPrimaryType(false) == null);
    }

    private boolean isValidMove(List<Move> moveData, int move) {
        return move >= 0 && move < moveData.size() && moveData.get(move) != null;
    }

    private boolean hasExtendedBpreHackSpeciesPool() {
        return romHandler instanceof Gen3RomHandler
                && ((Gen3RomHandler) romHandler).hasExtendedBpreHackSpeciesPool();
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
        int skippedMissingMovesets = 0;
        for (Species pkmn : compat.keySet()) {
            List<MoveLearnt> moveset = getMovesetForSpecies(pkmn, movesets);
            boolean[] pkmnCompat = compat.get(pkmn);
            if (moveset == null || pkmnCompat == null) {
                skippedMissingMovesets++;
                continue;
            }
            for (MoveLearnt ml : moveset) {
                if (tmMoves.contains(ml.move)) {
                    int tmIndex = tmMoves.indexOf(ml.move);
                    if (tmIndex + 1 < pkmnCompat.length) {
                        pkmnCompat[tmIndex + 1] = true;
                    }
                }
            }
        }
        if (skippedMissingMovesets > 0) {
            System.out.println("[CFRU-DPE-TM-SANITY] skippedMissingMovesets=" + skippedMissingMovesets);
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
        int skippedMissingMovesets = 0;
        for (Species pkmn : compat.keySet()) {
            List<MoveLearnt> moveset = getMovesetForSpecies(pkmn, movesets);
            boolean[] pkmnCompat = compat.get(pkmn);
            if (moveset == null || pkmnCompat == null) {
                skippedMissingMovesets++;
                continue;
            }
            for (MoveLearnt ml : moveset) {
                if (mtMoves.contains(ml.move)) {
                    int mtIndex = mtMoves.indexOf(ml.move);
                    if (mtIndex + 1 < pkmnCompat.length) {
                        pkmnCompat[mtIndex + 1] = true;
                    }
                }
            }
        }
        if (skippedMissingMovesets > 0) {
            System.out.println("[CFRU-DPE-TUTOR-SANITY] skippedMissingMovesets=" + skippedMissingMovesets);
        }
        romHandler.setMoveTutorCompatibility(compat);
        tutorChangesMade = true;
    }

    private List<MoveLearnt> getMovesetForSpecies(Species species, Map<Integer, List<MoveLearnt>> movesets) {
        if (species == null || movesets == null) {
            return null;
        }
        int identityNumber = species.getSpeciesSetIdentityNumber();
        if (identityNumber > 0 && movesets.containsKey(identityNumber)) {
            return movesets.get(identityNumber);
        }
        return movesets.get(species.getNumber());
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
