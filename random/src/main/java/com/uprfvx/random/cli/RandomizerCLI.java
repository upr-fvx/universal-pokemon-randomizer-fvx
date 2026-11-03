package com.uprfvx.random.cli;

import com.uprfvx.random.RandomizeMode;
import com.uprfvx.random.RandomizerCoordinator;
import com.uprfvx.random.RandomizerUI;
import com.uprfvx.random.Version;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.random.gui.CPGSelection;
import com.uprfvx.random.random.SeedPicker;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.exceptions.CannotWriteToLocationException;
import com.uprfvx.romio.gamedata.PlayerCharacterType;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.graphics.packs.GraphicsPack;
import com.uprfvx.romio.romhandlers.Abstract3DSRomHandler;
import com.uprfvx.romio.romhandlers.AbstractDSRomHandler;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.romio.RomOpener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A class with functions for randomizing from the command line.<br>
 * This implements {@link RandomizerUI} but does currently not support batch mode or race mode.
 */
public class RandomizerCLI implements RandomizerUI {

    private final static ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/uprfvx/random/gui/Bundle");

    private final static RomOpener romOpener = new RomOpener();

    private boolean saveLog;

    private boolean performDirectRandomization(String sourceRomFilePath, String destinationRomFilePath,
                                               RandomizeMode mode,
                                               SettingsManager settings, Long seed,
                                               boolean racePreset, boolean racePresetLogExists,
                                               String cpgName, PlayerCharacterType cpgType,
                                               boolean saveAsDirectory,
                                               String updateFilePath, boolean saveLog) {
        this.saveLog = saveLog;

        try {
            File romFile = new File(sourceRomFilePath);

            RomOpener.Results results = romOpener.openRomFile(romFile);
            if (!results.wasOpeningSuccessful()) {
                printError("Could not load " + romFile.getAbsolutePath() + "; " + results.getFailType());
                return false;
            }

            RomHandler romHandler = results.getRomHandler();
            CustomPlayerGraphics cpg = prepareCPG(romHandler, cpgName, cpgType);

            if (updateFilePath != null && (romHandler.generationOfPokemon() == 6 || romHandler.generationOfPokemon() == 7)) {
                romHandler.loadGameUpdate(updateFilePath);
                if (!saveAsDirectory) {
                    printWarning("Forcing save as directory since a game update was supplied.");
                }
                saveAsDirectory = true;
            }
            if (saveAsDirectory && romHandler.generationOfPokemon() != 6 && romHandler.generationOfPokemon() != 7) {
                saveAsDirectory = false;
                printWarning("Saving as directory does not make sense for non-3DS games, ignoring \"-d\" flag...");
            }

            RandomizerCLI.displaySettingsWarnings(settings, romHandler);

            File fh = new File(destinationRomFilePath);
            if (!saveAsDirectory) {
                if (romHandler instanceof AbstractDSRomHandler || romHandler instanceof Abstract3DSRomHandler) {
                    String currentFN = romHandler.loadedFilename();
                    if (currentFN.equals(fh.getAbsolutePath())) {
                        printError(bundle.getString("GUI.saveROM.cantOverwriteDSDialog.message"));
                        return false;
                    }
                }
            }

            String filename = fh.getAbsolutePath();

            if (mode != RandomizeMode.LOAD_PRESET) {
                seed = SeedPicker.pickSeed();
            }

            RandomizerCoordinator rc = new RandomizerCoordinator(this);
            rc.randomize(romHandler, filename, saveAsDirectory, false, settings, seed, mode,
                    0, 0, false, null, // <- irrelevant args
                    racePreset, racePresetLogExists);
            // this is the only successful exit, everything else will return false at the end of the function
            return true;

        } catch (Exception e) {
            printError(Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    @Override
    public void displayRandomizationStarted() {
        System.out.println("Randomization started...");
    }

    @Override
    public void displayRandomizationProgress(int currentBatch, int batchSize) {
        if (batchSize != 1) {
            System.out.printf("Randomizing game %d/%d...%n", currentBatch + 1, batchSize);
        }
    }

    @Override
    public void displayRandomizationEnded() {
        System.out.println("Randomization ended successfully.");
    }

    @Override
    public RandomizerCoordinator.LogPresetPromptResults promptSaveLogOrPresetFiles(boolean createRaceMode) {
        return new RandomizerCoordinator.LogPresetPromptResults(saveLog, false);
    }

    @Override
    public void displayRandomizationCoordinationDone(RandomizeMode mode, int batchNumber, boolean batchSaveLogs, boolean racePreset, boolean racePresetLogExists) {
        System.out.println("Done!");
    }

    @Override
    public void displayRandomizationFailure(Exception e, long seed, String fileName) {
        if (e instanceof RandomizationException) {
            printError("Randomization failed.\n" + Arrays.toString(e.getStackTrace()));
        } else if (e instanceof CannotWriteToLocationException) {
            printError("Could not write randomized file to " + fileName + ".");
        } else {
            printError("Randomization failed; likely an IO issue.\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void displayBatchLoggingFailure(Exception logE, long seed, int currentBatch, int batchSize) {
        printWarning(String.format("Logging partially failed for game %d/%d.\n%s",
                currentBatch + 1, batchSize, logE.getMessage()));
    }

    @Override
    public void displayBatchLogFileSaveFailure(int currentBatch, int batchSize) {
        printError(String.format("Could not save log file for game %d/%d.", currentBatch + 1, batchSize));
    }

    @Override
    public void displayLoggingFailure(Exception logE, long seed) {
        printWarning("Logging partially failed.\n" + Arrays.toString(logE.getStackTrace()));
    }

    @Override
    public void displayLogFileSaveFailure() {
        printError("Could not save log file.");
    }

    @Override
    public void displayPresetFileSaveFailure() {
        printError("Could not save preset file.");
    }

    @Override
    public void unloadRomHandler() {
        // not relevant since the CLI is a one-off action
    }

    @Override
    public void initialState() {
        // not relevant since the CLI is a one-off action
    }

    @Override
    public void reinitializeRomHandler(boolean batchMode) {
        // this needs to work if batch mode is supported, but otherwise not
    }

    private static void displaySettingsWarnings(SettingsManager settings, RomHandler romHandler) {
        // TODO: what kind of warnings do we want to show, with the new settings framework?
//        SettingsManager.TweakForROMFeedback feedback = settings.tweakForRom(romHandler);
//        Settings.StartersMod startersMod = settings.get(Settings.Name.RANDOMIZE_STARTERS);
//        if (feedback.isChangedStarter() && startersMod == Settings.StartersMod.CUSTOM) {
//            printWarning(bundle.getString("GUI.saveROM.starterUnavailableDialog.message"));
//        }
//        if (settings.isUpdatedFromOldVersion()) {
//            printWarning(bundle.getString("GUI.loadSettings.settingsFileOlderDialog.message"));
//        }
    }

    private static CustomPlayerGraphics prepareCPG(RomHandler romHandler, String name, PlayerCharacterType type) {
        if (name == null) {
            return null;
        }
        GraphicsPack pack = null;
        List<GraphicsPack> packs = CPGSelection.getAllCPGPacks(romHandler);
        for (GraphicsPack gp : packs) {
            if (gp.getName().equals(name)) {
                pack = gp;
                break;
            }
        }
        if (pack == null) {
            printWarning("Could not find CPG with the name \"" + name + "\". No CPG will be used.");
            return null;
        }

        if (!romHandler.hasMultiplePlayerCharacters() && type != PlayerCharacterType.PC1) {
            printWarning("PCG type \"" + type + "\" is not valid for the given ROM. Using "
                    + PlayerCharacterType.PC1 + " instead.");
            type = PlayerCharacterType.PC1;
        }
        return new CustomPlayerGraphics(pack, type);
    }

    public int invoke(String[] args) {
        String sourceRomFilePath = null;
        String outputRomFilePath = null;

        RandomizeMode mode;
        SettingsManager settings;
        Long seed = null;
        boolean racePreset = false;
        boolean racePresetLogExists = false;

        String presetFilePath = null;
        String settingsFilePath = null;

        String cpgName = null;
        String cpgTypeString = null;
        PlayerCharacterType cpgType = null;

        boolean saveAsDirectory = false;
        String updateFilePath = null;
        boolean saveLog = false;

        List<String> allowedFlags = Arrays.asList("-i", "-o", "-p", "-s", "-c", "-d", "-u", "-l", "-h", "--help");
        for (int i = 0; i < args.length; i++) {
            if (allowedFlags.contains(args[i])) {
                switch (args[i]) {
                    case "-i":
                        sourceRomFilePath = args[i + 1];
                        break;
                    case "-o":
                        outputRomFilePath = args[i + 1];
                        break;
                    case "-p":
                        presetFilePath = args[i + 1];
                        break;
                    case "-s":
                        settingsFilePath = args[i + 1];
                        break;
                    case "-c":
                        cpgName = args[i + 1];
                        cpgTypeString = args[i + 2];
                        break;
                    case "-d":
                        saveAsDirectory = true;
                        break;
                    case "-u":
                        updateFilePath = args[i + 1];
                        break;
                    case "-l":
                        saveLog = true;
                        break;
                    case "-h":
                    case "--help":
                        printUsage();
                        return 0;
                    default:
                        break;
                }
            }
        }

        if (sourceRomFilePath == null) {
            return usageError("Missing required argument: -i (source ROM path)");
        }

        if (outputRomFilePath == null) {
            return usageError("Missing required argument: -o (path for output ROM)");
        }

        // check that everything is readable/writable as appropriate
        if (!new File(sourceRomFilePath).exists()) {
            return usageError("Could not read source ROM file");
        }

        // java will return false for a non-existent file, have to check the parent directory
        if (!new File(outputRomFilePath).getAbsoluteFile().getParentFile().canWrite()) {
            return usageError("Destination ROM path not writable");
        }

        if (presetFilePath == null && settingsFilePath == null) {
            return usageError("Missing preset/settings file argument");
        }
        if (presetFilePath != null) {
            if (settingsFilePath != null) {
                printWarning("Both preset file path (-p) and settings file path (-s) were given. " +
                        "The preset file will be used.");
            }
            File rh = new File(presetFilePath);
            RandomizerCoordinator rc = new RandomizerCoordinator(this);
            RandomizerCoordinator.ReadPresetResults results = rc.readPresetFile(rh);
            if (results.success()) {
                mode = RandomizeMode.LOAD_PRESET;
                seed = results.seed();
                racePreset = results.raceMode();
                racePresetLogExists = results.raceModeLogExists();
                settings = results.settings();
            } else {
                switch (results.exception().getType()) {
                    case IO_ERROR -> usageError("Could not read preset file.");
                    case NO_SEED -> usageError("Preset file contained no seed.");
                    case INVALID_SETTINGS -> usageError("Preset file contained invalid settings.");
                    case WRONG_VERSION -> {
                        if (results.versionID() < Version.LATEST.id) {
                            usageError(String.format(
                                    "Preset file is for an old Randomizer version. Expected: %d, was: %d",
                                    Version.LATEST.id, results.versionID()));
                        } else {
                            usageError(String.format(
                                    "Preset file is for a newer Randomizer version. Expected: %d, was: %d",
                                    Version.LATEST.id, results.versionID()));
                        }
                    }
                }
                return 1;
            }
        } else {
            String settingsText;
            try {
                settingsText = Files.readString(Path.of(settingsFilePath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return usageError("Could not read settings file.\n" + Arrays.toString(e.getStackTrace()));
            }
            settings = new SettingsManager();
            SettingsManager.FromIniResults results = settings.populateFromIni(settingsText);
            if (!results.success()) {
                return usageError("Invalid settings file.\n"
                        + Arrays.toString(results.exception().getStackTrace()));
            }
            mode = RandomizeMode.NORMAL;
        }

        if (cpgName != null) {
            try {
                cpgType = PlayerCharacterType.valueOf(cpgTypeString);
            } catch (IllegalArgumentException e) {
                return usageError("Invalid CPG type");
            }
        }

        boolean processResult = performDirectRandomization(
                sourceRomFilePath,
                outputRomFilePath,
                mode,
                settings, seed,
                racePreset, racePresetLogExists,
                cpgName, cpgType,
                saveAsDirectory,
                updateFilePath,
                saveLog
        );
        if (!processResult) {
            return usageError("Randomization failed");
        }
        return 0;
    }

    private static int usageError(String text) {
        printError(text);
        printUsage();
        return 1;
    }

    private static void printError(String text) {
        System.out.println("ERROR: " + text);
    }

    private static void printWarning(String text) {
        System.out.println("WARNING: " + text);
    }

    private static void printUsage() {
        System.out.println("Usage: java [-Xmx4096M] -jar UPR-FVX.jar cli -i <path to source ROM> -o <path for output ROM>\n" +
                           "       {-p <path to preset file> | -s <path to settings file>} [options]");
        System.out.println("Optional flags: ");
        System.out.println("-Xmx4096M          : Increase the amount of RAM available to Java. Required for 3DS games.");
        System.out.println("-c <name> <type>   : Use a Custom Player Graphics. \"name\" must match a CPG defined in the\n" +
                           "                     data folder. \"type\" denotes which player character will be replaced,\n" +
                           "                     and must be either PC1 or PC2.");
        System.out.println("-d                 : Save 3DS game as directory (LayeredFS).");
        System.out.println("-u <path to update>: Apply the given 3DS game update before randomization.");
        System.out.println("-l                 : Generate a detailed log file.");
        System.out.println("-h --help          : Print usage/help info.");
        System.out.println("Note: if using a settings file instead of a preset file, a random seed will be used.");
    }
}
