package com.uprfvx.random;

import com.uprfvx.random.exceptions.ReadPresetException;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Coordinates the randomization pipeline, though not the randomization itself (directly),
 * since that is handled by the {@link GameRandomizer} class.<br>
 * This class accounts for features like race mode, batch mode, and loading from a preset file,
 * as well as actually outputting log/preset files.<br><br>
 * It also allows for reading preset files, using {@link #readPresetFile(File)}.
 */
public class RandomizerCoordinator {
    // The ultimate plan is for this class to work as a (state-full)
    // coordinator for everything core to the Randomizer,
    // with the UI classes only taking care of... UI.
    // This should own the RomHandler, the SettingsManager, etc.
    // However, we are not there yet. In the meanwhile,
    // this class contains methods that should be available to
    // both current UIs (the Swing GUI and the CLI),
    // and is essentially stateless.

    public record ReadPresetResults(boolean success, ReadPresetException exception, int versionID,
                                    long seed, boolean raceMode, boolean raceModeLogExists,
                                    SettingsManager settings, String romName) {

        private static ReadPresetResults failure(ReadPresetException.Type exceptionType, Exception cause) {
            return new ReadPresetResults(false, new ReadPresetException(exceptionType, cause), 0,
                    0, false, false, null, null);
        }

        private static ReadPresetResults versionFailure(int versionID) {
            return new ReadPresetResults(false,
                    new ReadPresetException(ReadPresetException.Type.WRONG_VERSION, null), versionID,
                    0, false, false, null, null);
        }

        private static ReadPresetResults success(long seed, boolean raceMode, boolean raceModeLogExists,
                                                 SettingsManager settings, String romName) {
            return new ReadPresetResults(true, null, 0,
                    seed, raceMode, raceModeLogExists, settings, romName);
        }
    }

    public record LogPresetPromptResults(boolean createLog, boolean createPreset) {}

    private final RandomizerUI ui;

    public RandomizerCoordinator(RandomizerUI ui) {
        this.ui = ui;
    }

    public ReadPresetResults readPresetFile(File presetFile) {
        try {
            Long seed = null;
            boolean raceMode = false;
            boolean raceModeLogExists = false;
            List<String> lines = Files.readAllLines(presetFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.startsWith(SysConstants.SEED_IDENTIFIER)) {
                    seed = Long.parseLong(line.substring(SysConstants.SEED_IDENTIFIER.length()));
                }
                if (line.equals(SysConstants.RACE_MODE_IDENTIFIER)) {
                    raceMode = true;
                }
                if (line.equals(SysConstants.RACE_MODE_LOG_IDENTIFIER)) {
                    raceModeLogExists = true;
                }
            }
            if (seed == null) {
                return ReadPresetResults.failure(ReadPresetException.Type.NO_SEED, null);
            }
            if (raceMode) {
                seed -= SysConstants.RACE_MODE_SEED_ADDEND;
            }
            if (raceModeLogExists) {
                seed -= SysConstants.RACE_MODE_SEED_LOG_ADDEND;
            }

            SettingsManager settings = new SettingsManager();
            SettingsManager.FromIniResults results = settings.populateFromIni(
                        Files.readString(presetFile.toPath(), StandardCharsets.UTF_8));
            if (!results.success()) {
                return ReadPresetResults.failure(ReadPresetException.Type.NO_SEED, results.exception());
            }
            if (results.versionID() != Version.LATEST.id) {
                return ReadPresetResults.versionFailure(results.versionID());
            }

            return ReadPresetResults.success(seed, raceMode, raceModeLogExists, settings, results.romName());

        } catch (IOException e) {
            return ReadPresetResults.failure(ReadPresetException.Type.IO_ERROR, e);
        }
    }

    public void randomize(RomHandler romHandler, String baseFileName, boolean saveAsDirectory, boolean unloadGameOnSuccess,
                          SettingsManager settings, long seed,
                          RandomizeMode mode,
                          int batchCount, int batchCountNameOffset, boolean batchSaveLogs, String batchLogSuffix,
                          boolean racePreset, boolean racePresetLogExists) {
        // TODO: account for CPG
        CustomPlayerGraphics cpg = null;

        int batchSize = mode == RandomizeMode.BATCH ? batchCount : 1;

        GameRandomizer.Results results = null;
        ui.displayRandomizationStarted();
        for (int i = 0; i < batchSize; i++) {
            ui.displayRandomizationProgress(i, batchSize);

            String fileName = prepareFileName(baseFileName, romHandler, saveAsDirectory,
                    mode == RandomizeMode.BATCH, i + batchCountNameOffset);
            GameRandomizer randomizer = new GameRandomizer(settings, cpg, romHandler, saveAsDirectory);
            results = randomizer.randomize(fileName, seed);

            if (!results.wasSaveSuccessful()) {
                ui.displayRandomizationFailure(results.getException(), seed, fileName);
                return;
            }

            if (mode == RandomizeMode.BATCH && batchSaveLogs) {
                Exception logException = results.wasLogSuccessful() ? null : results.getLogException();
                saveBatchLog(fileName, batchLogSuffix, i, batchSize,
                        logException, results.getLogBAOS(), seed);
            }
            if (mode == RandomizeMode.BATCH) {
                ui.reinitializeRomHandler(true);
            }
        }
        ui.displayRandomizationEnded();

        if (mode == RandomizeMode.NORMAL || mode == RandomizeMode.CREATE_RACE
                || (mode == RandomizeMode.LOAD_PRESET && !racePreset)) {
            Exception logException = results.wasLogSuccessful() ? null : results.getLogException();
            maybeSaveLogOrPreset(mode, baseFileName, results.getLogBAOS(), logException, settings, seed, racePreset);
        }

        ui.displayRandomizationCoordinationDone(mode, batchCount, batchSaveLogs, racePreset, racePresetLogExists);

        updateUIStateAfterSuccess(unloadGameOnSuccess);
    }

    private void saveBatchLog(String fileName, String batchLogSuffix,
                              int currentBatch, int batchSize,
                              Exception logException, ByteArrayOutputStream logBAOS,
                              long seed) {
        if (logException != null) {
            ui.displayBatchLoggingFailure(logException, seed, currentBatch, batchSize);
        }
        try {
            saveLogFile(fileName, batchLogSuffix, logBAOS.toByteArray());
        } catch (IOException e) {
            ui.displayBatchLogFileSaveFailure(currentBatch, batchSize);
        }
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
            ps.println(SysConstants.RACE_MODE_IDENTIFIER);
            seed += SysConstants.RACE_MODE_SEED_ADDEND;
            if (raceLogCreated) {
                ps.println(SysConstants.RACE_MODE_LOG_IDENTIFIER);
                seed += SysConstants.RACE_MODE_SEED_LOG_ADDEND;
            }
        }
        ps.println(SysConstants.SEED_IDENTIFIER + seed);
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
