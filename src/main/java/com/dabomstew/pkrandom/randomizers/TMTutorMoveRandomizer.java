package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.constants.GlobalConstants;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.*;

/**
 * A randomizer for which moves are found in TMs and at Move Tutors.
 * For compatibility with Pokemon, see {@link TMHMTutorCompatibilityRandomizer}.
 */
public class TMTutorMoveRandomizer extends Randomizer {

    private boolean tmChangesMade;
    private boolean tutorChangesMade;

    public TMTutorMoveRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    /**
     * Returns whether any changes have been made to TM moves.
     */
    public boolean isTMChangesMade() {
        return tmChangesMade;
    }

    /**
     * Returns whether any changes have been made to Move Tutor moves.
     */
    public boolean isTutorChangesMade() {
        return tutorChangesMade;
    }

    public void randomizeTMMoves() {
        boolean noBroken = settings.isBlockBrokenTMMoves();
        boolean preserveField = settings.isKeepFieldMoveTMs();
        double goodDamagingPercentage = settings.isTmsForceGoodDamaging() ? settings.getTmsGoodDamagingPercent() / 100.0 : 0;

        // Pick some random TM moves.
        int tmCount = romHandler.getTMCount();
        List<Move> allMoves = romHandler.getMoves();
        List<Integer> hms = romHandler.getHMMoves();
        List<Integer> oldTMs = romHandler.getTMMoves();
        @SuppressWarnings("unchecked")
        List<Integer> banned = new ArrayList<Integer>(noBroken ? romHandler.getGameBreakingMoves() : Collections.EMPTY_LIST);
        banned.addAll(romHandler.getMovesBannedFromLevelup());
        banned.addAll(romHandler.getIllegalMoves());
        // field moves?
        List<Integer> fieldMoves = romHandler.getFieldMoves();
        int preservedFieldMoveCount = 0;

        if (preserveField) {
            List<Integer> banExistingField = new ArrayList<>(oldTMs);
            banExistingField.retainAll(fieldMoves);
            preservedFieldMoveCount = banExistingField.size();
            banned.addAll(banExistingField);
        }

        // Determine which moves are pickable
        List<Move> usableMoves = new ArrayList<>(allMoves);
        usableMoves.remove(0); // remove null entry
        Set<Move> unusableMoves = new HashSet<>();
        Set<Move> unusableDamagingMoves = new HashSet<>();

        for (Move mv : usableMoves) {
            if (GlobalConstants.bannedRandomMoves[mv.number] || GlobalConstants.zMoves.contains(mv.number) ||
                    hms.contains(mv.number) || banned.contains(mv.number)) {
                unusableMoves.add(mv);
            } else if (GlobalConstants.bannedForDamagingMove[mv.number] || !mv.isGoodDamaging(romHandler.getPerfectAccuracy())) {
                unusableDamagingMoves.add(mv);
            }
        }

        usableMoves.removeAll(unusableMoves);
        List<Move> usableDamagingMoves = new ArrayList<>(usableMoves);
        usableDamagingMoves.removeAll(unusableDamagingMoves);

        // pick (tmCount - preservedFieldMoveCount) moves
        List<Integer> pickedMoves = new ArrayList<>();

        // Force a certain amount of good damaging moves depending on the percentage
        int goodDamagingLeft = (int)Math.round(goodDamagingPercentage * (tmCount - preservedFieldMoveCount));

        for (int i = 0; i < tmCount - preservedFieldMoveCount; i++) {
            Move chosenMove;
            if (goodDamagingLeft > 0 && !usableDamagingMoves.isEmpty()) {
                chosenMove = usableDamagingMoves.get(random.nextInt(usableDamagingMoves.size()));
            } else {
                chosenMove = usableMoves.get(random.nextInt(usableMoves.size()));
            }
            pickedMoves.add(chosenMove.number);
            usableMoves.remove(chosenMove);
            usableDamagingMoves.remove(chosenMove);
            goodDamagingLeft--;
        }

        // shuffle the picked moves because high goodDamagingPercentage
        // will bias them towards early numbers otherwise

        Collections.shuffle(pickedMoves, random);

        // finally, distribute them as tms
        int pickedMoveIndex = 0;
        List<Integer> newTMs = new ArrayList<>();

        for (int i = 0; i < tmCount; i++) {
            if (preserveField && fieldMoves.contains(oldTMs.get(i))) {
                newTMs.add(oldTMs.get(i));
            } else {
                newTMs.add(pickedMoves.get(pickedMoveIndex++));
            }
        }

        romHandler.setTMMoves(newTMs);
        tmChangesMade = true;
    }

    public void randomizeMoveTutorMoves() {
        boolean noBroken = settings.isBlockBrokenTutorMoves();
        boolean preserveField = settings.isKeepFieldMoveTutors();
        double goodDamagingPercentage = settings.isTutorsForceGoodDamaging() ? settings.getTutorsGoodDamagingPercent() / 100.0 : 0;

        if (!romHandler.hasMoveTutors()) {
            return;
        }

        // Pick some random Move Tutor moves, excluding TMs.
        List<Move> allMoves = romHandler.getMoves();
        List<Integer> tms = romHandler.getTMMoves();
        List<Integer> oldMTs = romHandler.getMoveTutorMoves();
        int mtCount = oldMTs.size();
        List<Integer> hms = romHandler.getHMMoves();
        @SuppressWarnings("unchecked")
        List<Integer> banned = new ArrayList<Integer>(noBroken ? romHandler.getGameBreakingMoves() : Collections.EMPTY_LIST);
        banned.addAll(romHandler.getMovesBannedFromLevelup());
        banned.addAll(romHandler.getIllegalMoves());

        // field moves?
        List<Integer> fieldMoves = romHandler.getFieldMoves();
        int preservedFieldMoveCount = 0;
        if (preserveField) {
            List<Integer> banExistingField = new ArrayList<>(oldMTs);
            banExistingField.retainAll(fieldMoves);
            preservedFieldMoveCount = banExistingField.size();
            banned.addAll(banExistingField);
        }

        // Determine which moves are pickable
        List<Move> usableMoves = new ArrayList<>(allMoves);
        usableMoves.remove(0); // remove null entry
        Set<Move> unusableMoves = new HashSet<>();
        Set<Move> unusableDamagingMoves = new HashSet<>();

        for (Move mv : usableMoves) {
            if (GlobalConstants.bannedRandomMoves[mv.number] || tms.contains(mv.number) || hms.contains(mv.number)
                    || banned.contains(mv.number) || GlobalConstants.zMoves.contains(mv.number)) {
                unusableMoves.add(mv);
            } else if (GlobalConstants.bannedForDamagingMove[mv.number] || !mv.isGoodDamaging(romHandler.getPerfectAccuracy())) {
                unusableDamagingMoves.add(mv);
            }
        }

        usableMoves.removeAll(unusableMoves);
        List<Move> usableDamagingMoves = new ArrayList<>(usableMoves);
        usableDamagingMoves.removeAll(unusableDamagingMoves);

        // pick (tmCount - preservedFieldMoveCount) moves
        List<Integer> pickedMoves = new ArrayList<>();

        // Force a certain amount of good damaging moves depending on the percentage
        int goodDamagingLeft = (int) Math.round(goodDamagingPercentage * (mtCount - preservedFieldMoveCount));

        for (int i = 0; i < mtCount - preservedFieldMoveCount; i++) {
            Move chosenMove;
            if (goodDamagingLeft > 0 && !usableDamagingMoves.isEmpty()) {
                chosenMove = usableDamagingMoves.get(random.nextInt(usableDamagingMoves.size()));
            } else {
                chosenMove = usableMoves.get(random.nextInt(usableMoves.size()));
            }
            pickedMoves.add(chosenMove.number);
            usableMoves.remove(chosenMove);
            usableDamagingMoves.remove(chosenMove);
            goodDamagingLeft--;
        }

        // shuffle the picked moves because high goodDamagingPercentage
        // will bias them towards early numbers otherwise

        Collections.shuffle(pickedMoves, random);

        // finally, distribute them as tutors
        int pickedMoveIndex = 0;
        List<Integer> newMTs = new ArrayList<>();

        for (Integer oldMT : oldMTs) {
            if (preserveField && fieldMoves.contains(oldMT)) {
                newMTs.add(oldMT);
            } else {
                newMTs.add(pickedMoves.get(pickedMoveIndex++));
            }
        }

        romHandler.setMoveTutorMoves(newMTs);
        tutorChangesMade = true;
    }

}
