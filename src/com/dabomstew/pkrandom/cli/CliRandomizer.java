package com.dabomstew.pkrandom.cli;

import com.dabomstew.pkrandom.GameRandomizer;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.customnames.CustomNamesSet;
import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.romhandlers.Abstract3DSRomHandler;
import com.dabomstew.pkromio.romhandlers.AbstractDSRomHandler;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romio.RomOpener;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CliRandomizer {

    // TODO: Why is this class fully static? It gives bad vibes since it's also in Java. Is it really fine like this?

    private final static ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dabomstew/pkrandom/gui/Bundle");

    private final static RomOpener romOpener = new RomOpener();

    private static boolean performDirectRandomization(String settingsFilePath, String sourceRomFilePath,
                                                      String destinationRomFilePath, boolean saveAsDirectory,
                                                      String updateFilePath, boolean saveLog) {
        Settings settings;
        try {
            File fh = new File(settingsFilePath);
            FileInputStream fis = new FileInputStream(fh);
            settings = Settings.read(fis);
            // taken from com.dabomstew.pkrandom.newgui.RandomizerGUI.saveROM, set distinctly from all other settings
            settings.setCustomNames(CustomNamesSet.readNamesFromFile());
            fis.close();
        } catch (UnsupportedOperationException | IllegalArgumentException | IOException ex) {
            ex.printStackTrace();
            return false;
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream log;
        try {
            log = new PrintStream(baos, false, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log = new PrintStream(baos);
        }

        final PrintStream verboseLog = log;

        try {
            File romFile = new File(sourceRomFilePath);

            RomOpener.Results results = romOpener.openRomFile(romFile);
            if (results.wasOpeningSuccessful()) {
                RomHandler romHandler = results.getRomHandler();

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

                CliRandomizer.displaySettingsWarnings(settings, romHandler);

                File fh = new File(destinationRomFilePath);
                if (!saveAsDirectory) {
                    List<String> extensions = new ArrayList<>(Arrays.asList("sgb", "gbc", "gba", "nds", "cxi"));
                    extensions.remove(romHandler.getDefaultExtension());

                    fh = FileFunctions.fixFilename(fh, romHandler.getDefaultExtension(), extensions);
                    if (romHandler instanceof AbstractDSRomHandler || romHandler instanceof Abstract3DSRomHandler) {
                        String currentFN = romHandler.loadedFilename();
                        if (currentFN.equals(fh.getAbsolutePath())) {
                            printError(bundle.getString("GUI.cantOverwriteDS"));
                            return false;
                        }
                    }
                }

                String filename = fh.getAbsolutePath();

                GameRandomizer randomizer = new GameRandomizer(settings, null, romHandler, bundle, saveAsDirectory);
                randomizer.randomize(filename, verboseLog);
                verboseLog.close();
                byte[] out = baos.toByteArray();
                if (saveLog) {
                    try {
                        FileOutputStream fos = new FileOutputStream(filename + ".log");
                        fos.write(0xEF);
                        fos.write(0xBB);
                        fos.write(0xBF);
                        fos.write(out);
                        fos.close();
                    } catch (IOException e) {
                        printWarning("Could not write log.");
                    }
                }
                System.out.println("Randomized successfully!");
                // this is the only successful exit, everything else will return false at the end of the function
                return true;

            } else {
                printError("Could not load " + romFile.getAbsolutePath() + "; " + results.getFailType());
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void displaySettingsWarnings(Settings settings, RomHandler romHandler) {
        Settings.TweakForROMFeedback feedback = settings.tweakForRom(romHandler);
        if (feedback.isChangedStarter() && settings.getStartersMod() == Settings.StartersMod.CUSTOM) {
            printWarning(bundle.getString("GUI.starterUnavailable"));
        }
        if (settings.isUpdatedFromOldVersion()) {
            printWarning(bundle.getString("GUI.settingsFileOlder"));
        }
    }

    public static int invoke(String[] args) {
        String settingsFilePath = null;
        String sourceRomFilePath = null;
        String outputRomFilePath = null;
        boolean saveAsDirectory = false;
        String updateFilePath = null;
        boolean saveLog = false;

        List<String> allowedFlags = Arrays.asList("-i", "-o", "-s", "-d", "-u", "-l", "--help");
        for (int i = 0; i < args.length; i++) {
            if (allowedFlags.contains(args[i])) {
                switch(args[i]) {
                    case "-i":
                        sourceRomFilePath = args[i + 1];
                        break;
                    case "-o":
                        outputRomFilePath = args[i + 1];
                        break;
                    case "-s":
                        settingsFilePath = args[i + 1];
                        break;
                    case "-d":
                        saveAsDirectory = true;
                        break;
                    case "-u":
                        updateFilePath = args[i+1];
                        break;
                    case "-l":
                        saveLog = true;
                        break;
                    case "--help":
                        printUsage();
                        return 0;
                    default:
                        break;
                }
            }
        }

        if (settingsFilePath == null || sourceRomFilePath == null || outputRomFilePath == null) {
            printError("Missing required argument");
            CliRandomizer.printUsage();
            return 1;

        }

        // now we know we have the right number of args...
        if (!new File(settingsFilePath).exists()) {
            printError("Could not read settings file");
            CliRandomizer.printUsage();
            return 1;
        }

        // check that everything is readable/writable as appropriate
        if (!new File(sourceRomFilePath).exists()) {
            printError("Could not read source ROM file");
            CliRandomizer.printUsage();
            return 1;
        }

        // java will return false for a non-existent file, have to check the parent directory
        if (!new File(outputRomFilePath).getAbsoluteFile().getParentFile().canWrite()) {
            printError("Destination ROM path not writable");
            CliRandomizer.printUsage();
            return 1;
        }

        boolean processResult = CliRandomizer.performDirectRandomization(
                settingsFilePath,
                sourceRomFilePath,
                outputRomFilePath,
                saveAsDirectory,
                updateFilePath,
                saveLog
        );
        if (!processResult) {
            printError("Randomization failed");
            CliRandomizer.printUsage();
            return 1;
        }
        return 0;
    }

    private static void printError(String text) {
        System.err.println("ERROR: " + text);
    }

    private static void printWarning(String text) {
        System.err.println("WARNING: " + text);
    }

    private static void printUsage() {
        System.err.println("Usage: java [-Xmx4096M] -jar PokeRandoFVX.jar cli -s <path to settings file> " +
                "-i <path to source ROM> -o <path for new ROM> [-d][-u <path to 3DS game update>][-l]");
        System.err.println("-d: Save 3DS game as directory (LayeredFS)");
    }
}
