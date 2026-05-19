package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrainerClassSpriteSyncRandomizer extends Randomizer {

    public static class Assignment {
        private final int oldTrainerClass;
        private final int oldTrainerPic;
        private final int newTrainerClass;
        private final int newTrainerPic;

        Assignment(int oldTrainerClass, int oldTrainerPic, int newTrainerClass, int newTrainerPic) {
            this.oldTrainerClass = oldTrainerClass;
            this.oldTrainerPic = oldTrainerPic;
            this.newTrainerClass = newTrainerClass;
            this.newTrainerPic = newTrainerPic;
        }

        public int getOldTrainerClass() {
            return oldTrainerClass;
        }

        public int getOldTrainerPic() {
            return oldTrainerPic;
        }

        public int getNewTrainerClass() {
            return newTrainerClass;
        }

        public int getNewTrainerPic() {
            return newTrainerPic;
        }
    }

    private record ClassSpritePair(int trainerClass, int trainerPic) {
    }

    private final Map<Integer, Assignment> assignmentsByTrainerIndex = new LinkedHashMap<>();

    public TrainerClassSpriteSyncRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeTrainerClassSprites() {
        assignmentsByTrainerIndex.clear();
        if (!romHandler.supportsTrainerClassSpriteSync()) {
            return;
        }

        List<Trainer> eligibleTrainers = eligibleRegularTrainers(romHandler.getTrainers());
        List<ClassSpritePair> sourcePairs = safeSourcePairs(eligibleTrainers);
        if (eligibleTrainers.isEmpty() || sourcePairs.size() < 2) {
            return;
        }

        List<String> trainerClassNames = romHandler.getTrainerClassNames();
        for (Trainer trainer : eligibleTrainers) {
            ClassSpritePair replacement = pickReplacement(trainer, sourcePairs);
            if (replacement == null) {
                continue;
            }
            int oldTrainerClass = trainer.getTrainerclass();
            int oldTrainerPic = trainer.getTrainerPic();
            trainer.setTrainerclass(replacement.trainerClass());
            trainer.setTrainerPic(replacement.trainerPic());
            refreshFullDisplayName(trainer, trainerClassNames);
            assignmentsByTrainerIndex.put(trainer.getIndex(), new Assignment(
                    oldTrainerClass, oldTrainerPic, replacement.trainerClass(), replacement.trainerPic()));
        }

        if (!assignmentsByTrainerIndex.isEmpty()) {
            romHandler.setTrainerClassSpriteSyncEnabled(true);
            changesMade = true;
        }
    }

    public Map<Integer, Assignment> getAssignmentsByTrainerIndex() {
        return Collections.unmodifiableMap(assignmentsByTrainerIndex);
    }

    private List<Trainer> eligibleRegularTrainers(List<Trainer> trainers) {
        List<Trainer> eligible = new ArrayList<>();
        for (Trainer trainer : trainers) {
            if (isEligibleRegularTrainer(trainer)) {
                eligible.add(trainer);
            }
        }
        return eligible;
    }

    static boolean isEligibleRegularTrainer(Trainer trainer) {
        return trainer != null
                && trainer.isRegular()
                && !trainer.isRuntimeSource()
                && trainer.getTrainerclass() > 0
                && trainer.getTrainerPic() >= 0;
    }

    private List<ClassSpritePair> safeSourcePairs(List<Trainer> trainers) {
        List<ClassSpritePair> pairs = new ArrayList<>();
        for (Trainer trainer : trainers) {
            ClassSpritePair pair = new ClassSpritePair(trainer.getTrainerclass(), trainer.getTrainerPic());
            if (!pairs.contains(pair)) {
                pairs.add(pair);
            }
        }
        return pairs;
    }

    private ClassSpritePair pickReplacement(Trainer trainer, List<ClassSpritePair> sourcePairs) {
        List<ClassSpritePair> candidates = new ArrayList<>(sourcePairs);
        candidates.remove(new ClassSpritePair(trainer.getTrainerclass(), trainer.getTrainerPic()));
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private void refreshFullDisplayName(Trainer trainer, List<String> trainerClassNames) {
        int trainerClass = trainer.getTrainerclass();
        if (trainerClass >= 0 && trainerClass < trainerClassNames.size() && trainer.getName() != null) {
            trainer.setFullDisplayName(trainerClassNames.get(trainerClass) + " " + trainer.getName());
        }
    }
}
