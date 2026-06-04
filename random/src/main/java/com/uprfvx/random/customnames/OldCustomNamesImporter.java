package com.uprfvx.random.customnames;

import com.uprfvx.romio.RootPath;
import filefunctions.FileFunctions;
import filefunctions.IOFunctions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Allows for importing/reading old file formats for {@link CustomNamesSet}.
 * <br><br>
 * RNCN was a format .<br>
 * Before RNCN the custom names resided in text files, but without the names for double battle being separated.
 * Presumably, these text files were also backed up by defaults in the JAR, but these no longer exist so we don't
 * account for them.
 */
public class OldCustomNamesImporter {

    private static final String RNCN_FILENAME = "customnames.rncn";
    private static final int RNCN_VERSION = 1;

    private static final String PRE_RNCN_TRAINERS_FILENAME = "trainernames.txt";
    private static final String PRE_RNCN_CLASSES_FILENAME = "trainerclasses.txt";
    private static final String PRE_RNCN_NICKNAMES_FILENAME = "nicknames.txt";

    /**
     * Returns whether the file system has files for old custom names to import.
     */
    public static boolean hasOldNamesToImport() {
        return Stream.of(RNCN_FILENAME, PRE_RNCN_TRAINERS_FILENAME, PRE_RNCN_CLASSES_FILENAME, PRE_RNCN_NICKNAMES_FILENAME)
                .anyMatch(OldCustomNamesImporter::fileExists);
    }

    public static CustomNamesSet importOldNames() throws IOException {
        if (fileExists(RNCN_FILENAME)) {
            return readRNCNFile(RNCN_FILENAME);
        } else if (fileExists(PRE_RNCN_TRAINERS_FILENAME) || fileExists(PRE_RNCN_CLASSES_FILENAME) ||
                fileExists(PRE_RNCN_NICKNAMES_FILENAME)) {
            return readPreRNCNTextFiles(PRE_RNCN_TRAINERS_FILENAME, PRE_RNCN_CLASSES_FILENAME,
                    PRE_RNCN_NICKNAMES_FILENAME);
        } else {
            throw new IOException("Neither RNCN file nor pre-RNCN text name files could be found.");
        }
    }

    // public for testing
    public static CustomNamesSet readRNCNFile(String path) throws IOException {
        InputStream data = openFile(path);
        if (data.read() != RNCN_VERSION) {
            throw new IOException("Invalid custom names file provided.");
        }
        return new CustomNamesSet(readRNCNBlock(data), readRNCNBlock(data),
                readRNCNBlock(data), readRNCNBlock(data),
                readRNCNBlock(data));
    }

    private static List<String> readRNCNBlock(InputStream in) throws IOException {
        // Read the size of the block to come.
        byte[] szData = FileFunctions.readFullyIntoBuffer(in, 4);
        int size = IOFunctions.readFullIntBigEndian(szData, 0);
        if (in.available() < size) {
            throw new IOException("Invalid size specified.");
        }

        // Read the block and translate it into a list of names.
        byte[] namesData = FileFunctions.readFullyIntoBuffer(in, size);
        List<String> names = new ArrayList<>();
        Scanner sc = new Scanner(new ByteArrayInputStream(namesData), StandardCharsets.UTF_8);
        while (sc.hasNextLine()) {
            String name = sc.nextLine().trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        sc.close();

        return names;
    }

    // public for testing
    public static CustomNamesSet readPreRNCNTextFiles(String trainerPath, String classesPath, String nicknamesPath)
            throws IOException {
        SinglesAndDoublesNames trainerNames = readPreRNCNTrainerNames(trainerPath);
        SinglesAndDoublesNames trainerClasses = readPreRNCNTrainerClasses(classesPath);
        List<String> pokemonNicknames = readPreRNCNNicknames(nicknamesPath);

        return new CustomNamesSet(trainerNames.singles, trainerClasses.singles,
                trainerNames.doubles, trainerClasses.doubles,
                pokemonNicknames);
    }

    private record SinglesAndDoublesNames(List<String> singles, List<String> doubles) { }

    private static SinglesAndDoublesNames readPreRNCNTrainerNames(String path) throws IOException {
        SinglesAndDoublesNames trainerNames = new SinglesAndDoublesNames(new ArrayList<>(), new ArrayList<>());
        if (fileExists(path)) {
            Scanner sc = new Scanner(openFile(path), StandardCharsets.UTF_8);
            while (sc.hasNextLine()) {
                String trainername = sc.nextLine().trim();
                if (trainername.isEmpty()) {
                    continue;
                }
                if (trainername.startsWith("\uFEFF")) {
                    trainername = trainername.substring(1);
                }
                if (trainername.contains("&")) {
                    trainerNames.doubles.add(trainername);
                } else {
                    trainerNames.singles.add(trainername);
                }
            }
            sc.close();
        }
        return trainerNames;
    }

    private static SinglesAndDoublesNames readPreRNCNTrainerClasses(String path) throws IOException {
        SinglesAndDoublesNames trainerClasses = new SinglesAndDoublesNames(new ArrayList<>(), new ArrayList<>());
        if (fileExists(path)) {
            Scanner sc = new Scanner(openFile(path), StandardCharsets.UTF_8);
            while (sc.hasNextLine()) {
                String trainerClassName = sc.nextLine().trim();
                if (trainerClassName.isEmpty()) {
                    continue;
                }
                if (trainerClassName.startsWith("\uFEFF")) {
                    trainerClassName = trainerClassName.substring(1);
                }
                String checkName = trainerClassName.toLowerCase();
                boolean isDoubles = (checkName.endsWith("couple") || checkName.contains(" and ") || checkName.endsWith("kin")
                        || checkName.endsWith("team") || checkName.contains("&") || (checkName.endsWith("s") && !checkName
                        .endsWith("ss")));
                if (isDoubles) {
                    trainerClasses.doubles.add(trainerClassName);
                } else {
                    trainerClasses.singles.add(trainerClassName);
                }
            }
            sc.close();
        }
        return trainerClasses;
    }

    private static List<String> readPreRNCNNicknames(String path) throws IOException {
        List<String> pokemonNicknames = new ArrayList<>();
        if (fileExists(path)) {
            Scanner sc = new Scanner(openFile(path), StandardCharsets.UTF_8);
            while (sc.hasNextLine()) {
                String nickname = sc.nextLine().trim();
                if (nickname.isEmpty()) {
                    continue;
                }
                if (nickname.startsWith("\uFEFF")) {
                    nickname = nickname.substring(1);
                }
                pokemonNicknames.add(nickname);
            }
            sc.close();
        }
        return pokemonNicknames;
    }

    private static InputStream openFile(String filename) throws IOException {
        File fh = new File(RootPath.path + filename);
        if (fh.exists() && fh.canRead()) {
            return Files.newInputStream(fh.toPath());
        }
        throw new IOException("Could not find or read file: " + fh.getAbsolutePath());
    }

    private static boolean fileExists(String filename) {
        File fh = new File(RootPath.path + filename);
        return fh.exists() && fh.canRead();
    }
}
