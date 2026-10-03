package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerClass;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.*;

public class TrainerNameRandomizer extends Randomizer {

    private static final int MAX_TRIES = 10000;

    private static final int MAX_TRAINER_NAME_LEN = 10; // why this? should logically depend on the game
    private static final List<String> REPEATED_TRAINER_NAMES =
            Arrays.asList("GRUNT", "EXECUTIVE", "SHADOW", "ADMIN", "GOON", "EMPLOYEE");

    private record CustomNames(List<String> all, Map<Integer, List<String>> byLength) {}

    public TrainerNameRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeTrainerNames() {

        if (!romHandler.canChangeTrainerText()) {
            return;
        }

        // Setup custom names
        CustomNamesSet customNamesRaw = getCustomNames();
        CustomNames singlesCustomNames = readCustomNameList(customNamesRaw.trainerNames(), MAX_TRAINER_NAME_LEN);
        CustomNames doublesCustomNames = readCustomNameList(customNamesRaw.doublesTrainerNames(), MAX_TRAINER_NAME_LEN);

        // RomHandler-dependent variables
        List<String> currentTrainerNames = getTrainerNames();
        RomHandler.TrainerNameMode mode = romHandler.trainerNameMode();
        int maxLength = romHandler.maxTrainerNameLength();
        int totalMaxLength = romHandler.maxSumOfTrainerNameLengths();
        List<Integer> tcNameLengths = romHandler.getTCNameLengthsByTrainer();

        // Init the translation map and new list
        Map<String, String> translation = new HashMap<>();
        List<String> newTrainerNames = new ArrayList<>();

        boolean success = false;
        int tries = 0;

        // loop until we successfully pick names that fit
        // should always succeed first attempt except for gen2.
        while (!success && tries < MAX_TRIES) {
            success = true;
            translation.clear();
            newTrainerNames.clear();
            int totalLength = 0;

            // Start choosing
            int tnIndex = -1;
            for (String trainerName : currentTrainerNames) {
                tnIndex++;
                if (translation.containsKey(trainerName) &&
                        !REPEATED_TRAINER_NAMES.contains(trainerName.toUpperCase())) {
                    // use an already picked translation
                    newTrainerNames.add(translation.get(trainerName));
                    totalLength += romHandler.internalStringLength(translation.get(trainerName));
                } else {
                    boolean doubles = trainerName.contains("&");
                    CustomNames customNames = doubles ? doublesCustomNames : singlesCustomNames;

                    List<String> pickFrom = customNames.all();
                    int intStrLen = romHandler.internalStringLength(trainerName);
                    if (mode == RomHandler.TrainerNameMode.SAME_LENGTH) {
                        pickFrom = customNames.byLength().get(intStrLen);
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
            throw new RandomizationException("Could not randomize trainer names within " + MAX_TRIES + " tries."
                    + "\nPlease add some shorter names to your custom trainer names.");
        }

        if (detectUpperCaseNames(currentTrainerNames)) {
            newTrainerNames.replaceAll(String::toUpperCase);
        }

        // Done choosing, save
        setTrainerNames(newTrainerNames);
        changesMade = true;
    }

    private List<String> getTrainerNames() {
        List<String> trainerNames = new ArrayList<>();
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getName() == null) continue;
            trainerNames.add(tr.getName());
        }
        for (TrainerClass personalTC : romHandler.getPersonalTrainerClasses()) {
            trainerNames.add(personalTC.getName());
        }
        return trainerNames;
    }

    private void setTrainerNames(List<String> newTrainerNames) {
        int i = 0;
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.getName() == null) continue;
            tr.setName(newTrainerNames.get(i));
            i++;
        }
        for (TrainerClass personalTC : romHandler.getPersonalTrainerClasses()) {
            personalTC.setName(newTrainerNames.get(i));
            i++;
        }
    }

    public void randomizeTrainerClassNames() {
        if (!romHandler.canChangeTrainerText()) {
            return;
        }

        // Setup custom names
        CustomNamesSet customNamesRaw = getCustomNames();
        CustomNames singlesCustomClasses = readCustomNameList(customNamesRaw.trainerClasses(), Integer.MAX_VALUE);
        CustomNames doublesCustomClasses = readCustomNameList(customNamesRaw.doublesTrainerClasses(), Integer.MAX_VALUE);

        // RomHandler-dependent variables
        List<String> currentClassNames = getTrainerClassNames();
        int numTrainerClasses = currentClassNames.size();
        List<Integer> doublesClasses = romHandler.getDoublesTrainerClasses();
        boolean mustBeSameLength = romHandler.fixedTrainerClassNamesLength();
        int maxLength = romHandler.maxTrainerClassNameLength();

        // Init the translation map and new list
        Map<String, String> translation = new HashMap<>();
        List<String> newClassNames = new ArrayList<>();

        // Start choosing
        for (int i = 0; i < numTrainerClasses; i++) {
            String trainerClassName = currentClassNames.get(i);
            if (translation.containsKey(trainerClassName)) {
                // use an already picked translation
                newClassNames.add(translation.get(trainerClassName));
            } else {
                boolean doubles = doublesClasses.contains(i);
                CustomNames customClasses = doubles ? doublesCustomClasses : singlesCustomClasses;

                List<String> pickFrom = customClasses.all();
                int intStrLen = romHandler.internalStringLength(trainerClassName);
                if (mustBeSameLength) {
                    pickFrom = customClasses.byLength().get(intStrLen);
                }
                String changeTo = trainerClassName;
                if (pickFrom != null && !pickFrom.isEmpty()) {
                    changeTo = pickFrom.get(random.nextInt(pickFrom.size()));
                    while (changeTo.length() > maxLength) {
                        changeTo = pickFrom.get(random.nextInt(pickFrom.size()));
                    }
                }
                translation.put(trainerClassName, changeTo);
                newClassNames.add(changeTo);
            }
        }

        if (detectUpperCaseNames(currentClassNames)) {
            newClassNames.replaceAll(String::toUpperCase);
        }

        // Done choosing, save
        setTrainerClassNames(newClassNames);
        changesMade = true;
    }

    private List<String> getTrainerClassNames() {
        List<String> trainerClassNames = new ArrayList<>();
        List<TrainerClass> personal = romHandler.getPersonalTrainerClasses();
        for (TrainerClass tc : romHandler.getTrainerClasses()) {
            if (personal.contains(tc)) continue;
            trainerClassNames.add(tc.getName());
        }

        return trainerClassNames;
    }

    private void setTrainerClassNames(List<String> newClassNames) {
        List<TrainerClass> personal = romHandler.getPersonalTrainerClasses();
        int i = 0;
        for (TrainerClass tc : romHandler.getTrainerClasses()) {
            if (personal.contains(tc)) continue;
            tc.setName(newClassNames.get(i));
            i++;
        }
    }

    private CustomNames readCustomNameList(List<String> customNames, int limit) {
        List<String> allNames = new ArrayList<>();
        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String trainername : customNames) {
            int len = romHandler.internalStringLength(trainername);
            if (len <= limit) {
                allNames.add(trainername);
                byLength.putIfAbsent(len, new ArrayList<>());
                byLength.get(len).add(trainername);
            }
        }
        return new CustomNames(allNames, byLength);
    }

}
