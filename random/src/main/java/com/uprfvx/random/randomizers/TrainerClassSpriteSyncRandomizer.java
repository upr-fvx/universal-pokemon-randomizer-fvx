package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.Collections;
import java.util.HashMap;
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
        private final String group;

        Assignment(int oldTrainerClass, int oldTrainerPic, int newTrainerClass, int newTrainerPic, String group) {
            this.oldTrainerClass = oldTrainerClass;
            this.oldTrainerPic = oldTrainerPic;
            this.newTrainerClass = newTrainerClass;
            this.newTrainerPic = newTrainerPic;
            this.group = group;
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

        public String getGroup() {
            return group;
        }
    }

    private final Map<Integer, Assignment> assignmentsByTrainerIndex = new LinkedHashMap<>();
    private final TrainerNameRandomizer trainerNameRandomizer;

    public TrainerClassSpriteSyncRandomizer(RomHandler romHandler, Settings settings, Random random) {
        this(romHandler, settings, random, null);
    }

    public TrainerClassSpriteSyncRandomizer(
            RomHandler romHandler, Settings settings, Random random, TrainerNameRandomizer trainerNameRandomizer) {
        super(romHandler, settings, random);
        this.trainerNameRandomizer = trainerNameRandomizer;
    }

    public void randomizeTrainerClassSprites() {
        assignmentsByTrainerIndex.clear();
        if (!romHandler.supportsTrainerClassSpriteSync()) {
            return;
        }

        List<Trainer> trainers = romHandler.getTrainers();
        Map<Integer, Integer> classIdMapping = trainerClassIdMapping();
        Map<Integer, Integer> classIdAssignmentsByTrainerIndex = trainerClassIdAssignmentsByTrainerIndex();
        Map<Integer, Integer> trainerPicByClassId = trainerPicByClassId(trainers);
        List<String> originalTrainerClassNames = originalTrainerClassNames();
        if ((classIdMapping.isEmpty() && classIdAssignmentsByTrainerIndex.isEmpty())
                || trainerPicByClassId.isEmpty() || originalTrainerClassNames.isEmpty()) {
            return;
        }

        for (Trainer trainer : trainers) {
            if (!hasValidClassPic(trainer)) {
                continue;
            }
            int oldTrainerClass = trainer.getTrainerclass();
            int oldTrainerPic = trainer.getTrainerPic();
            Integer newTrainerClass = classIdAssignmentsByTrainerIndex.getOrDefault(trainer.getIndex(),
                    classIdMapping.get(oldTrainerClass));
            if (newTrainerClass == null || newTrainerClass < 0
                    || newTrainerClass >= originalTrainerClassNames.size()) {
                continue;
            }
            Integer newTrainerPic = trainerPicByClassId.get(newTrainerClass);
            if (newTrainerPic == null || newTrainerPic < 0) {
                continue;
            }
            if (oldTrainerClass == newTrainerClass && oldTrainerPic == newTrainerPic) {
                continue;
            }
            trainer.setTrainerclass(newTrainerClass);
            trainer.setTrainerPic(newTrainerPic);
            refreshFullDisplayName(trainer, originalTrainerClassNames);
            assignmentsByTrainerIndex.put(trainer.getIndex(), new Assignment(
                    oldTrainerClass, oldTrainerPic, newTrainerClass, newTrainerPic, assignmentGroup(trainer)));
        }

        if (!assignmentsByTrainerIndex.isEmpty()) {
            romHandler.setTrainerClassNames(originalTrainerClassNames);
            romHandler.setTrainerClassSpriteSyncEnabled(true);
            changesMade = true;
        }
    }

    public Map<Integer, Assignment> getAssignmentsByTrainerIndex() {
        return Collections.unmodifiableMap(assignmentsByTrainerIndex);
    }

    private Map<Integer, Integer> trainerClassIdMapping() {
        if (trainerNameRandomizer == null) {
            return Collections.emptyMap();
        }
        return trainerNameRandomizer.getTrainerClassIdMapping();
    }

    private Map<Integer, Integer> trainerClassIdAssignmentsByTrainerIndex() {
        if (trainerNameRandomizer == null) {
            return Collections.emptyMap();
        }
        return trainerNameRandomizer.getTrainerClassIdAssignmentsByTrainerIndex();
    }

    private List<String> originalTrainerClassNames() {
        if (trainerNameRandomizer == null) {
            return Collections.emptyList();
        }
        return trainerNameRandomizer.getOriginalTrainerClassNames();
    }

    private Map<Integer, Integer> trainerPicByClassId(List<Trainer> trainers) {
        Map<Integer, Integer> trainerPicByClassId = new HashMap<>();
        for (Trainer trainer : trainers) {
            if (hasValidClassPic(trainer)) {
                trainerPicByClassId.putIfAbsent(trainer.getTrainerclass(), trainer.getTrainerPic());
            }
        }
        return trainerPicByClassId;
    }

    private boolean hasValidClassPic(Trainer trainer) {
        return trainer != null && trainer.getTrainerclass() >= 0 && trainer.getTrainerPic() >= 0;
    }

    private String assignmentGroup(Trainer trainer) {
        String tag = trainer.getTag();
        if (tag != null && (tag.startsWith("RIVAL") || tag.startsWith("FRIEND"))) {
            return "rival";
        }
        return null;
    }

    private void refreshFullDisplayName(Trainer trainer, List<String> trainerClassNames) {
        int trainerClass = trainer.getTrainerclass();
        if (trainerClass >= 0 && trainerClass < trainerClassNames.size() && trainer.getName() != null) {
            trainer.setFullDisplayName(trainerClassNames.get(trainerClass) + " " + trainer.getName());
        }
    }
}
