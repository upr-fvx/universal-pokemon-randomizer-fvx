package com.uprfvx.random;

import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Coordinates the randomization pipeline, accounting for features like
 * loading from a preset file, batch mode, and actually outputting log/preset files.
 */
public class RandomizationCoordinator {

    public enum RandomizeMode {
        NORMAL, CREATE_RACE, BATCH, LOAD_PRESET
    }

    public record LogPresetPromptResults(boolean createLog, boolean createPreset) {}

    private final RandomizerUI ui;

    public RandomizationCoordinator(RandomizerUI ui) {
        this.ui = ui;
    }

    public void randomize(RomHandler romHandler, String fileName, boolean saveAsDirectory, boolean unloadGameOnSuccess,
                          SettingsManager settings, long seed,
                          RandomizeMode mode,
                          int batchNumber, int batchNumberNameOffset, boolean batchSaveLogs, String batchLogSuffix,
                          boolean racePreset, boolean racePresetLogExists) {
        // TODO: link up to preset loading (and CLI?)
        // TODO: account for every place things can go wrong

        CustomPlayerGraphics cpg = null;

        int batchSize = mode == RandomizeMode.BATCH ? batchNumber : 1;

        GameRandomizer.Results results = null;
        ui.displayRandomizationStarted();
        for (int i = 0; i < batchSize; i++) {
            ui.displayRandomizationProgress(i, batchSize);

            fileName = prepareFileName(fileName, romHandler, saveAsDirectory,
                    mode == RandomizeMode.BATCH, batchNumber + batchNumberNameOffset);
            GameRandomizer randomizer = new GameRandomizer(settings, cpg, romHandler, saveAsDirectory);
            results = randomizer.randomize(fileName, seed);

            if (mode == RandomizeMode.BATCH && batchSaveLogs) {
                try {
                    saveLogFile(fileName, batchLogSuffix, results.getLogBAOS().toByteArray());
                } catch (IOException e) {
                    // TODO: what to do here
                }
            }
            if (mode == RandomizeMode.BATCH) {
                ui.reinitializeRomHandler(true);
            }
        }
        ui.displayRandomizationEnded();

        if (mode == RandomizeMode.NORMAL || mode == RandomizeMode.CREATE_RACE
                || (mode == RandomizeMode.LOAD_PRESET && !racePreset)) {
            Exception logException = results.wasLogSuccessful() ? null : results.getLogException();
            maybeSaveLogOrPreset(mode, fileName, results.getLogBAOS(), logException, settings, seed, racePreset);
        }

        ui.displayRandomizationCoordinationDone(mode, batchNumber, batchSaveLogs, racePreset, racePresetLogExists);

        updateUIStateAfterSuccess(unloadGameOnSuccess);
    }

    private String prepareFileName(String originalFN, RomHandler romHandler, boolean saveAsDirectory,
                                   boolean batchMode, int batchSuffix) {
        String fileName = originalFN.split("\\.")[0];
        if (batchMode) {
            fileName += batchSuffix;
        }
        if (saveAsDirectory) {
            boolean _ = new File(fileName).mkdirs();
        } else {
            fileName += "." + romHandler.getDefaultExtension();
        }
        return fileName;
    }

    private void maybeSaveLogOrPreset(RandomizeMode mode, String fileName,
                                      ByteArrayOutputStream logBAOS, Exception logException,
                                      SettingsManager settings, long seed, boolean racePreset) {
        // TODO: account for creating a log while mode == LOAD_PRESET
        LogPresetPromptResults results = ui.promptSaveLogOrPresetFiles(mode == RandomizeMode.CREATE_RACE);
        if (results.createLog) {
            if (logException != null) {
                ui.displayLoggingFailure(logException, seed);
            }
            try {
                saveLogFile(fileName, "log", logBAOS.toByteArray());
            } catch (IOException e) {
                ui.displayLogFileSaveFailure();
            }
        }
        if (results.createPreset) {
            try {
                savePresetFile(fileName, settings, seed, racePreset, results.createLog);
            } catch (IOException e) {
                ui.displayPresetFileSaveFailure();
            }
        }
    }

    private void saveLogFile(String filename, String fileEnding, byte[] out) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename + "." + fileEnding);
        fos.write(0xEF);
        fos.write(0xBB);
        fos.write(0xBF);
        fos.write(out);
        fos.close();
    }

    private void savePresetFile(String filename, SettingsManager settings, long seed,
                                boolean raceMode, boolean raceLogCreated)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8);
        ps.println("[ Universal Pokémon Randomizer FVX - Randomized Game Preset ]");
        if (raceMode) {
            ps.println("Race mode!");
            seed += SysConstants.RACE_MODE_SEED_ADDEND;
            if (raceLogCreated) {
                ps.println("Race log created.");
                seed += SysConstants.RACE_MODE_SEED_LOG_ADDEND;
            }
        }
        ps.println("Random seed: " + seed);
        ps.println(settings.toString());

        try (FileOutputStream fos = new FileOutputStream(filename + ".preset")) {
            fos.write(baos.toByteArray());
        }
    }

    private void updateUIStateAfterSuccess(boolean unloadGame) {
        // Ideally this class should hold the RomHandler, and be responsible for
        // loading and updating it.
        // The UI holding it is a holdover from before this class was separated.
        // Since we need to access these methods from here, and they are in the UI class,
        // they become a little over-exposed. It's smelly.
        if (unloadGame) {
            ui.unloadRomHandler();
            ui.initialState();
        } else {
            ui.reinitializeRomHandler(false);
        }
    }
}
