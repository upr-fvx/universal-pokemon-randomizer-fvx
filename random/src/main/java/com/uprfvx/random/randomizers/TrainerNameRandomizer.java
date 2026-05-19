package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;

public class TrainerNameRandomizer extends Randomizer {

    private final Map<Integer, Integer> trainerClassIdMapping = new HashMap<>();
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

        // Done choosing, save
        romHandler.setTrainerClassNames(newClassNames);
        changesMade = true;
    }

    public Map<Integer, Integer> getTrainerClassIdMapping() {
        return Collections.unmodifiableMap(trainerClassIdMapping);
    }

    public List<String> getOriginalTrainerClassNames() {
        return Collections.unmodifiableList(originalTrainerClassNames);
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
