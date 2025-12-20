package com.dabomstew.pkrandom.cli;

import com.dabomstew.pkrandom.GameRandomizer;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.customnames.CustomNamesSet;
import com.dabomstew.pkrandom.gui.CPGSelection;
import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.gamedata.PlayerCharacterType;
import com.dabomstew.pkromio.graphics.packs.CustomPlayerGraphics;
import com.dabomstew.pkromio.graphics.packs.GraphicsPack;
import com.dabomstew.pkromio.romhandlers.Abstract3DSRomHandler;
import com.dabomstew.pkromio.romhandlers.AbstractDSRomHandler;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romio.RomOpener;

import java.io.*;
import java.util.*;

public class CliRandomizer {

    // TODO: Why is this class fully static? It gives bad vibes since it's also in Java. Is it really fine like this?

    private final static ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dabomstew/pkrandom/gui/Bundle");

    private final static RomOpener romOpener = new RomOpener();

    private static boolean performDirectRandomization(String sourceRomFilePath, String destinationRomFilePath,
                                                      Settings settings, long seed,
                                                      String cpgName, PlayerCharacterType cpgType,
                                                      boolean saveAsDirectory,
                                                      String updateFilePath, boolean saveLog) {

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

                GameRandomizer randomizer = new GameRandomizer(settings, cpg, romHandler, bundle, saveAsDirectory);
                randomizer.randomize(filename, verboseLog, seed);
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

    private static CustomPlayerGraphics prepareCPG(RomHandler romHandler, String name, PlayerCharacterType type) {
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

    public static int invoke(String[] args) {
        String sourceRomFilePath = null;
        String outputRomFilePath = null;

        Settings settings;
        long seed = -1;
        String settingsFilePath = null;
        String settingsString = null;
        String seedString = null;

        String cpgName = null;
        String cpgTypeString = null;
        PlayerCharacterType cpgType = null;

        boolean saveAsDirectory = false;
        String updateFilePath = null;
        boolean saveLog = false;

        List<String> allowedFlags = Arrays.asList("-i", "-o", "-s", "-S", "-z", "-c", "-d", "-u", "-l", "-h", "--help");
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
                    case "-S":
                        settingsString = args[i + 1];
                        break;
                    case "-z":
                        seedString = args[i + 1];
                        break;
                    case "-c":
                        cpgName = args[i + 1];
                        cpgTypeString = args[i + 2];
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
                    case "-h":
                    case "--help":
                        printUsage();
                        return 0;
                    default:
                        break;
                }
            }
        }

        if (sourceRomFilePath == null || outputRomFilePath == null) {
            return usageError("Missing required argument");
        }

        // check that everything is readable/writable as appropriate
        if (!new File(sourceRomFilePath).exists()) {
            return usageError("Could not read source ROM file");
        }

        // java will return false for a non-existent file, have to check the parent directory
        if (!new File(outputRomFilePath).getAbsoluteFile().getParentFile().canWrite()) {
            return usageError("Destination ROM path not writable");
        }

        if (settingsFilePath == null && settingsString == null) {
            return usageError("Missing settings argument");
        }
        if (settingsFilePath != null) {
            if (settingsString != null) {
                printWarning("Both settings file path (-s) and settings string (-S) were given. " +
                        "The settings file path will be used.");
            }
            if (!new File(settingsFilePath).exists()) {
                return usageError("Could not read settings file");
            }
            try {
                File fh = new File(settingsFilePath);
                FileInputStream fis = new FileInputStream(fh);
                settings = Settings.read(fis);
                settings.setCustomNames(CustomNamesSet.readNamesFromFile());
                fis.close();
            } catch (UnsupportedOperationException | IllegalArgumentException | IOException e) {
                try (PrintWriter pw = new PrintWriter(new StringWriter())) {
                    pw.println("Invalid settings file. Error caught:");
                    e.printStackTrace(pw);
                    return usageError(pw.toString());
                }
            }
        } else {
            try {
                settings = Settings.fromString(settingsString);
            } catch (UnsupportedEncodingException e) {
                try (PrintWriter pw = new PrintWriter(new StringWriter())) {
                    pw.println("Invalid settings string. Error caught:");
                    e.printStackTrace(pw);
                    return usageError(pw.toString());
                }
            }
        }

        if (seedString != null) {
            try {
                seed = Long.parseLong(seedString);
            } catch (NumberFormatException e) {
                return usageError("Invalid seed - could not parse as long");
            }
        }

        if (cpgName != null) {
            try {
                cpgType = PlayerCharacterType.valueOf(cpgTypeString);
            } catch (IllegalArgumentException e) {
                return usageError("Invalid CPG type");
            }
        }

        boolean processResult = CliRandomizer.performDirectRandomization(
                sourceRomFilePath,
                outputRomFilePath,
                settings,
                seed,
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
        System.err.println("ERROR: " + text);
    }

    private static void printWarning(String text) {
        System.err.println("WARNING: " + text);
    }

    private static void printUsage() {
        System.err.println("Usage: java [-Xmx4096M] -jar UPR-FVX.jar cli -i <path to source ROM> -o <path for new ROM>\n" +
                           "       {-s <path to settings file> | -S <settings string> } [options]");
        System.err.println("Optional flags: ");
        System.err.println("-Xmx4096M          : Increase the amount of RAM available to Java. Required for 3DS games.");
        System.err.println("-e <seed>          : Use the given seed.");
        System.err.println("-c <name> <type>   : Use a Custom Player Graphics. \"name\" must match a CPG defined in the\n" +
                           "                     data folder. \"type\" denotes which player character will be replaced,\n" +
                           "                     and must be either PC1 or PC2.");
        System.err.println("-d                 : Save 3DS game as directory (LayeredFS).");
        System.err.println("-u <path to update>: Apply the given 3DS game update before randomization.");
        System.err.println("-l                 : Save a detailed log file.");
        System.err.println("-h --help          : Print usage/help info.");
    }
}
