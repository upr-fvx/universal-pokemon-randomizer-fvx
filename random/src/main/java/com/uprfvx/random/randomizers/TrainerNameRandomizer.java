package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;

public class TrainerNameRandomizer extends Randomizer {

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
        if (!romHandler.canChangeTrainerText()) {
            return;
        }

        // Get the current trainer names data
        List<String> currentClassNames = romHandler.getTrainerClassNames();
        boolean mustBeSameLength = romHandler.fixedTrainerClassNamesLength();

        // Init the translation maps and new list
        List<String> newClassNames = new ArrayList<>();

        int numTrainerClasses = currentClassNames.size();
        List<Integer> doublesClasses = romHandler.getDoublesTrainerClasses();
        Set<Integer> doublesClassIndexes = new HashSet<>(doublesClasses);
        List<String>[] existingTrainerClasses = existingTrainerClassesByMode(currentClassNames, doublesClassIndexes);
        Map<Integer, List<String>>[] existingTrainerClassesByLength =
                existingTrainerClassesByLength(existingTrainerClasses);
        Map<String, String>[] translations = existingTrainerClassTranslations(
                existingTrainerClasses, existingTrainerClassesByLength, mustBeSameLength);

        // Start choosing
        for (int i = 0; i < numTrainerClasses; i++) {
            String trainerClassName = currentClassNames.get(i);
            int idx = doublesClassIndexes.contains(i) ? 1 : 0;
            newClassNames.add(translations[idx].getOrDefault(trainerClassName, trainerClassName));
        }

        // Done choosing, save
        romHandler.setTrainerClassNames(newClassNames);
        changesMade = true;
    }

    @SuppressWarnings("unchecked")
    private List<String>[] existingTrainerClassesByMode(List<String> currentClassNames, Set<Integer> doublesClasses) {
        List<String>[] existingTrainerClasses = new List[]{new ArrayList<String>(), new ArrayList<String>()};
        for (int i = 0; i < currentClassNames.size(); i++) {
            int idx = doublesClasses.contains(i) ? 1 : 0;
            String trainerClassName = currentClassNames.get(i);
            if (!existingTrainerClasses[idx].contains(trainerClassName)) {
                existingTrainerClasses[idx].add(trainerClassName);
            }
        }
        return existingTrainerClasses;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, List<String>>[] existingTrainerClassesByLength(List<String>[] existingTrainerClasses) {
        Map<Integer, List<String>>[] existingTrainerClassesByLength = new Map[]{new HashMap<Integer, List<String>>(),
                new HashMap<Integer, List<String>>()};
        for (int i = 0; i < existingTrainerClasses.length; i++) {
            for (String trainerClassName : existingTrainerClasses[i]) {
                int len = romHandler.internalStringLength(trainerClassName);
                existingTrainerClassesByLength[i].computeIfAbsent(len, ignored -> new ArrayList<>())
                        .add(trainerClassName);
            }
        }
        return existingTrainerClassesByLength;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String>[] existingTrainerClassTranslations(List<String>[] existingTrainerClasses,
                                                                   Map<Integer, List<String>>[]
                                                                           existingTrainerClassesByLength,
                                                                   boolean mustBeSameLength) {
        Map<String, String>[] translations = new Map[]{new HashMap<String, String>(), new HashMap<String, String>()};
        for (int i = 0; i < translations.length; i++) {
            if (mustBeSameLength) {
                for (List<String> sameLengthClasses : existingTrainerClassesByLength[i].values()) {
                    translations[i].putAll(shuffledTranslations(sameLengthClasses));
                }
            } else {
                translations[i].putAll(shuffledTranslations(existingTrainerClasses[i]));
            }
        }
        return translations;
    }

    private Map<String, String> shuffledTranslations(List<String> classNames) {
        Map<String, String> translations = new HashMap<>();
        if (classNames.isEmpty()) {
            return translations;
        }

        List<String> shuffledClassNames = new ArrayList<>(classNames);
        if (shuffledClassNames.size() > 1) {
            int tries = 0;
            do {
                Collections.shuffle(shuffledClassNames, random);
                tries++;
            } while (hasIdentityMapping(classNames, shuffledClassNames) && tries < 100);
            if (hasIdentityMapping(classNames, shuffledClassNames)) {
                Collections.rotate(shuffledClassNames, 1);
            }
        }

        for (int i = 0; i < classNames.size(); i++) {
            translations.put(classNames.get(i), shuffledClassNames.get(i));
        }
        return translations;
    }

    private boolean hasIdentityMapping(List<String> originals, List<String> replacements) {
        for (int i = 0; i < originals.size(); i++) {
            if (originals.get(i).equals(replacements.get(i))) {
                return true;
            }
        }
        return false;
    }
}
