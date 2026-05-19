package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;

public class TrainerNameRandomizer extends Randomizer {

    private static final Set<Integer> KNOWN_RIVAL_TRAINER_IDS = Set.of(
            0x148, 0x146, 0x147,
            0x14B, 0x149, 0x14A,
            0x14E, 0x14C, 0x14D,
            0x1AC, 0x1AA, 0x1AB,
            0x1AF, 0x1AD, 0x1AE,
            0x1B2, 0x1B0, 0x1B1,
            0x1B5, 0x1B3, 0x1B4,
            0x1B8, 0x1B6, 0x1B7,
            0x2E5, 0x2E3, 0x2E4,
            373, 374, 380, 381, 382, 383, 384, 385);
    private final Map<Integer, Integer> trainerClassIdMapping = new HashMap<>();
    private final Map<Integer, Integer> trainerClassIdAssignmentsByTrainerIndex = new HashMap<>();
    private List<String> originalTrainerClassNames = Collections.emptyList();

    public TrainerNameRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    @SuppressWarnings("unchecked")
    public void randomizeTrainerNames() {
        CustomNamesSet customNames = settings.getCustomNames();

        if (!romHandler.canChangeTrainerText()) {
            return;
        }

        // index 0 = singles, 1 = doubles
        List<String>[] allTrainerNames = new List[]{new ArrayList<String>(), new ArrayList<String>()};
        Map<Integer, List<String>>[] trainerNamesByLength = new Map[]{new TreeMap<Integer, List<String>>(),
                new TreeMap<Integer, List<String>>()};

        List<String> repeatedTrainerNames = Arrays.asList("GRUNT", "EXECUTIVE", "SHADOW", "ADMIN", "GOON", "EMPLOYEE");

        // Read name lists
        for (String trainername : customNames.getTrainerNames()) {
            int len = romHandler.internalStringLength(trainername);
            if (len <= 10) {
                allTrainerNames[0].add(trainername);
                if (trainerNamesByLength[0].containsKey(len)) {
                    trainerNamesByLength[0].get(len).add(trainername);
                } else {
                    List<String> namesOfThisLength = new ArrayList<>();
                    namesOfThisLength.add(trainername);
                    trainerNamesByLength[0].put(len, namesOfThisLength);
                }
            }
        }

        for (String trainername : customNames.getDoublesTrainerNames()) {
            int len = romHandler.internalStringLength(trainername);
            if (len <= 10) {
                allTrainerNames[1].add(trainername);
                if (trainerNamesByLength[1].containsKey(len)) {
                    trainerNamesByLength[1].get(len).add(trainername);
                } else {
                    List<String> namesOfThisLength = new ArrayList<>();
                    namesOfThisLength.add(trainername);
                    trainerNamesByLength[1].put(len, namesOfThisLength);
                }
            }
        }

        // Get the current trainer names data
        List<String> currentTrainerNames = romHandler.getTrainerNames();
        if (currentTrainerNames.isEmpty()) {
            // RBY have no trainer names
            return;
        }
        RomHandler.TrainerNameMode mode = romHandler.trainerNameMode();
        int maxLength = romHandler.maxTrainerNameLength();
        int totalMaxLength = romHandler.maxSumOfTrainerNameLengths();

        boolean success = false;
        int tries = 0;

        // Init the translation map and new list
        Map<String, String> translation = new HashMap<>();
        List<String> newTrainerNames = new ArrayList<>();
        List<Integer> tcNameLengths = romHandler.getTCNameLengthsByTrainer();

        // loop until we successfully pick names that fit
        // should always succeed first attempt except for gen2.
        while (!success && tries < 10000) {
            success = true;
            translation.clear();
            newTrainerNames.clear();
            int totalLength = 0;

            // Start choosing
            int tnIndex = -1;
            for (String trainerName : currentTrainerNames) {
                tnIndex++;
                if (translation.containsKey(trainerName) && !repeatedTrainerNames.contains(trainerName.toUpperCase())) {
                    // use an already picked translation
                    newTrainerNames.add(translation.get(trainerName));
                    totalLength += romHandler.internalStringLength(translation.get(trainerName));
                } else {
                    int idx = trainerName.contains("&") ? 1 : 0;
                    List<String> pickFrom = allTrainerNames[idx];
                    int intStrLen = romHandler.internalStringLength(trainerName);
                    if (mode == RomHandler.TrainerNameMode.SAME_LENGTH) {
                        pickFrom = trainerNamesByLength[idx].get(intStrLen);
                    }
                    String changeTo = trainerName;
                    int ctl = intStrLen;
                    if (pickFrom != null && !pickFrom.isEmpty() && intStrLen > 0) {
                        int innerTries = 0;
                        changeTo = pickFrom.get(random.nextInt(pickFrom.size()));
                        ctl = romHandler.internalStringLength(changeTo);
                        while ((mode == RomHandler.TrainerNameMode.MAX_LENGTH && ctl > maxLength)
                                || (mode == RomHandler.TrainerNameMode.MAX_LENGTH_WITH_CLASS && ctl + tcNameLengths.get(tnIndex) > maxLength)) {
                            innerTries++;
                            if (innerTries == 100) {
                                changeTo = trainerName;
                                ctl = intStrLen;
                                break;
                            }
                            changeTo = pickFrom.get(random.nextInt(pickFrom.size()));
                            ctl = romHandler.internalStringLength(changeTo);
                        }
                    }
                    translation.put(trainerName, changeTo);
                    newTrainerNames.add(changeTo);
                    totalLength += ctl;
                }

                if (totalLength > totalMaxLength) {
                    success = false;
                    tries++;
                    break;
                }
            }
        }

        if (!success) {
            throw new RandomizationException("Could not randomize trainer names in a reasonable amount of attempts."
                    + "\nPlease add some shorter names to your custom trainer names.");
        }

        // Done choosing, save
        romHandler.setTrainerNames(newTrainerNames);
        changesMade = true;
    }

    public void randomizeTrainerClassNames() {
        trainerClassIdMapping.clear();
        trainerClassIdAssignmentsByTrainerIndex.clear();
        originalTrainerClassNames = Collections.emptyList();
        if (!romHandler.canChangeTrainerText()) {
            return;
        }

        // Get the current trainer names data
        List<String> currentClassNames = romHandler.getTrainerClassNames();
        originalTrainerClassNames = new ArrayList<>(currentClassNames);
        boolean mustBeSameLength = romHandler.fixedTrainerClassNamesLength();

        // Init the translation maps and new list
        List<String> newClassNames = new ArrayList<>();

        int numTrainerClasses = currentClassNames.size();
        List<Integer> doublesClasses = romHandler.getDoublesTrainerClasses();
        Set<Integer> doublesClassIndexes = new HashSet<>(doublesClasses);
        List<Integer> targetClassIds = randomizedTrainerClassIdTargets(currentClassNames, doublesClassIndexes,
                mustBeSameLength);

        // Start choosing
        for (int i = 0; i < numTrainerClasses; i++) {
            newClassNames.add(currentClassNames.get(targetClassIds.get(i)));
            trainerClassIdMapping.put(i, targetClassIds.get(i));
        }
        if (settings.isRandomizeTrainerClassSprites()) {
            assignTrainerClassIdsPerTrainer(currentClassNames, doublesClassIndexes, mustBeSameLength);
        }

        // Done choosing, save
        romHandler.setTrainerClassNames(newClassNames);
        changesMade = true;
    }

    public Map<Integer, Integer> getTrainerClassIdMapping() {
        return Collections.unmodifiableMap(trainerClassIdMapping);
    }

    public Map<Integer, Integer> getTrainerClassIdAssignmentsByTrainerIndex() {
        return Collections.unmodifiableMap(trainerClassIdAssignmentsByTrainerIndex);
    }

    public List<String> getOriginalTrainerClassNames() {
        return Collections.unmodifiableList(originalTrainerClassNames);
    }

    private void assignTrainerClassIdsPerTrainer(
            List<String> currentClassNames, Set<Integer> doublesClassIndexes, boolean mustBeSameLength) {
        List<Trainer> trainers = romHandler.getTrainers();
        Integer rivalTargetClassId = rivalTargetClassId(trainers, currentClassNames, doublesClassIndexes,
                mustBeSameLength);
        for (Trainer trainer : trainers) {
            int oldClassId = trainer.getTrainerclass();
            if (oldClassId < 0 || oldClassId >= currentClassNames.size()) {
                continue;
            }
            int targetClassId = rivalTargetClassId != null
                    && isRivalClassSpriteSyncTrainer(trainer, currentClassNames)
                    ? rivalTargetClassId
                    : randomTrainerClassIdForTrainer(oldClassId, currentClassNames, doublesClassIndexes,
                    mustBeSameLength);
            trainerClassIdAssignmentsByTrainerIndex.put(trainer.getIndex(), targetClassId);
        }
    }

    private Integer rivalTargetClassId(List<Trainer> trainers, List<String> currentClassNames,
                                       Set<Integer> doublesClassIndexes, boolean mustBeSameLength) {
        List<Integer> rivalClassIds = trainers.stream()
                .filter(trainer -> isRivalClassSpriteSyncTrainer(trainer, currentClassNames))
                .map(Trainer::getTrainerclass)
                .filter(classId -> classId >= 0 && classId < currentClassNames.size())
                .toList();
        if (rivalClassIds.isEmpty()) {
            return null;
        }

        int sourceClassId = rivalClassIds.get(0);
        List<Integer> candidates = trainerClassIdCandidatesForSource(sourceClassId, currentClassNames,
                doublesClassIndexes, mustBeSameLength);
        if (candidates.size() <= 1) {
            return sourceClassId;
        }

        Set<Integer> rivalClassIdSet = new HashSet<>(rivalClassIds);
        Set<String> rivalClassNames = rivalClassIds.stream()
                .map(currentClassNames::get)
                .collect(java.util.stream.Collectors.toSet());
        List<Integer> preferredCandidates = candidates.stream()
                .filter(candidate -> !rivalClassIdSet.contains(candidate))
                .filter(candidate -> !rivalClassNames.contains(currentClassNames.get(candidate)))
                .toList();
        if (preferredCandidates.isEmpty()) {
            preferredCandidates = candidates.stream()
                    .filter(candidate -> !rivalClassIdSet.contains(candidate))
                    .toList();
        }
        if (preferredCandidates.isEmpty()) {
            preferredCandidates = candidates.stream()
                    .filter(candidate -> candidate != sourceClassId)
                    .toList();
        }
        if (preferredCandidates.isEmpty()) {
            return sourceClassId;
        }
        return preferredCandidates.get(random.nextInt(preferredCandidates.size()));
    }

    static boolean isRivalClassSpriteSyncTrainer(Trainer trainer, List<String> trainerClassNames) {
        if (trainer == null) {
            return false;
        }
        if (containsRivalOrFriendToken(trainer.getTag())) {
            return true;
        }
        if (KNOWN_RIVAL_TRAINER_IDS.contains(trainer.getIndex())) {
            return true;
        }
        if (containsRivalOrFriendToken(trainer.getFullDisplayName())) {
            return true;
        }
        int trainerClass = trainer.getTrainerclass();
        return trainerClass >= 0 && trainerClass < trainerClassNames.size()
                && containsRivalOrFriendToken(trainerClassNames.get(trainerClass));
    }

    private static boolean containsRivalOrFriendToken(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ");
        for (String token : normalized.trim().split(" +")) {
            if (token.startsWith("RIVAL") || token.startsWith("FRIEND")) {
                return true;
            }
        }
        return false;
    }

    private int randomTrainerClassIdForTrainer(
            int oldClassId, List<String> currentClassNames, Set<Integer> doublesClassIndexes,
            boolean mustBeSameLength) {
        List<Integer> candidates = trainerClassIdCandidatesForSource(oldClassId, currentClassNames,
                doublesClassIndexes, mustBeSameLength);
        if (candidates.size() <= 1) {
            return oldClassId;
        }

        List<Integer> preferredCandidates = candidates.stream()
                .filter(candidate -> candidate != oldClassId)
                .filter(candidate -> !currentClassNames.get(candidate).equals(currentClassNames.get(oldClassId)))
                .toList();
        if (preferredCandidates.isEmpty()) {
            preferredCandidates = candidates.stream()
                    .filter(candidate -> candidate != oldClassId)
                    .toList();
        }
        return preferredCandidates.get(random.nextInt(preferredCandidates.size()));
    }

    private List<Integer> trainerClassIdCandidatesForSource(
            int oldClassId, List<String> currentClassNames, Set<Integer> doublesClassIndexes,
            boolean mustBeSameLength) {
        List<Integer> candidates = new ArrayList<>();
        int oldMode = doublesClassIndexes.contains(oldClassId) ? 1 : 0;
        int oldLength = romHandler.internalStringLength(currentClassNames.get(oldClassId));
        for (int classId = 0; classId < currentClassNames.size(); classId++) {
            int mode = doublesClassIndexes.contains(classId) ? 1 : 0;
            if (mode != oldMode) {
                continue;
            }
            if (mustBeSameLength && romHandler.internalStringLength(currentClassNames.get(classId)) != oldLength) {
                continue;
            }
            candidates.add(classId);
        }
        return candidates;
    }

    private List<Integer> randomizedTrainerClassIdTargets(
            List<String> currentClassNames, Set<Integer> doublesClassIndexes, boolean mustBeSameLength) {
        List<Integer> targetClassIds = new ArrayList<>();
        for (int i = 0; i < currentClassNames.size(); i++) {
            targetClassIds.add(i);
        }
        for (List<Integer> bucket : trainerClassIdBuckets(currentClassNames, doublesClassIndexes, mustBeSameLength)) {
            List<Integer> shuffledTargets = shuffledTrainerClassIds(bucket, currentClassNames);
            for (int i = 0; i < bucket.size(); i++) {
                targetClassIds.set(bucket.get(i), shuffledTargets.get(i));
            }
        }
        return targetClassIds;
    }

    private List<List<Integer>> trainerClassIdBuckets(
            List<String> currentClassNames, Set<Integer> doublesClassIndexes, boolean mustBeSameLength) {
        Map<String, List<Integer>> bucketsByKey = new LinkedHashMap<>();
        for (int classId = 0; classId < currentClassNames.size(); classId++) {
            int mode = doublesClassIndexes.contains(classId) ? 1 : 0;
            String key;
            if (mustBeSameLength) {
                key = mode + ":" + romHandler.internalStringLength(currentClassNames.get(classId));
            } else {
                key = String.valueOf(mode);
            }
            bucketsByKey.computeIfAbsent(key, ignored -> new ArrayList<>()).add(classId);
        }
        return new ArrayList<>(bucketsByKey.values());
    }

    private List<Integer> shuffledTrainerClassIds(List<Integer> classIds, List<String> currentClassNames) {
        List<Integer> shuffledClassIds = new ArrayList<>(classIds);
        if (shuffledClassIds.size() <= 1) {
            return shuffledClassIds;
        }

        int tries = 0;
        do {
            Collections.shuffle(shuffledClassIds, random);
            tries++;
        } while ((hasIdentityClassIdMapping(classIds, shuffledClassIds)
                || hasAvoidableClassNameIdentityMapping(classIds, shuffledClassIds, currentClassNames))
                && tries < 100);
        if (hasIdentityClassIdMapping(classIds, shuffledClassIds)) {
            Collections.rotate(shuffledClassIds, 1);
        }
        return shuffledClassIds;
    }

    private boolean hasIdentityClassIdMapping(List<Integer> originals, List<Integer> replacements) {
        for (int i = 0; i < originals.size(); i++) {
            if (originals.get(i).equals(replacements.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAvoidableClassNameIdentityMapping(
            List<Integer> originals, List<Integer> replacements, List<String> currentClassNames) {
        for (int i = 0; i < originals.size(); i++) {
            int sourceClassId = originals.get(i);
            int targetClassId = replacements.get(i);
            String sourceName = currentClassNames.get(sourceClassId);
            String targetName = currentClassNames.get(targetClassId);
            if (sourceName.equals(targetName) && hasDifferentClassNameCandidate(sourceName, originals,
                    currentClassNames)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDifferentClassNameCandidate(String sourceName, List<Integer> classIds,
                                                   List<String> currentClassNames) {
        for (int classId : classIds) {
            if (!sourceName.equals(currentClassNames.get(classId))) {
                return true;
            }
        }
        return false;
    }
}
