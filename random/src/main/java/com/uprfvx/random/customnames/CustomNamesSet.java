package com.uprfvx.random.customnames;

/*----------------------------------------------------------------------------*/
/*--  CustomNamesSet.java - handles functionality related to custom names.  --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.uprfvx.random.SysConstants;
import com.uprfvx.romio.RootPath;
import filefunctions.FileFunctions;
import filefunctions.IOFunctions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CustomNamesSet {

    // TODO: standardize CustomNamesSet to work like the /data resources

    private static final String FOLDER_PATH = "data/trainer_and_mon_names";
    private static final String TRAINER_NAMES_PATH = FOLDER_PATH + "/TrainerNames.txt";
    private static final String TRAINER_CLASSES_PATH = FOLDER_PATH + "/TrainerClasses.txt";
    private static final String DOUBLES_TRAINER_NAMES_PATH = FOLDER_PATH + "/DoublesTrainerNames.txt";
    private static final String DOUBLES_TRAINER_CLASSES_PATH = FOLDER_PATH + "/DoublesTrainerNames.txt";
    private static final String POKEMON_NICKNAMES_PATH = FOLDER_PATH + "/PokemonNicknames.txt";

    // TODO: rename these wacky ones
    private static final int CUSTOM_NAMES_VERSION = 1;
    private static final String DEFAULT_FILE_PATH = "/com/uprfvx/random/customnames/";

    public static CustomNamesSet readNamesFromFile() throws IOException {
        // TODO: do some input checking, don't just read lines
        List<String> trainerNames = Files.readAllLines(Path.of(TRAINER_NAMES_PATH), StandardCharsets.UTF_8);
        List<String> trainerClasses = Files.readAllLines(Path.of(TRAINER_CLASSES_PATH), StandardCharsets.UTF_8);
        List<String> doublesTrainerNames = Files.readAllLines(Path.of(DOUBLES_TRAINER_NAMES_PATH), StandardCharsets.UTF_8);
        List<String> doublesTrainerClasses = Files.readAllLines(Path.of(DOUBLES_TRAINER_CLASSES_PATH), StandardCharsets.UTF_8);
        List<String> pokemonNicknames = Files.readAllLines(Path.of(POKEMON_NICKNAMES_PATH), StandardCharsets.UTF_8);
        return new CustomNamesSet(trainerNames, trainerClasses,
                doublesTrainerNames, doublesTrainerClasses,
                pokemonNicknames);
    }

    public static void writeNamesToFile(CustomNamesSet customNamesSet) throws IOException {
        Files.write(Path.of(TRAINER_NAMES_PATH), customNamesSet.trainerNames, StandardCharsets.UTF_8);
        Files.write(Path.of(TRAINER_CLASSES_PATH), customNamesSet.trainerClasses, StandardCharsets.UTF_8);
        Files.write(Path.of(DOUBLES_TRAINER_NAMES_PATH), customNamesSet.doublesTrainerNames, StandardCharsets.UTF_8);
        Files.write(Path.of(DOUBLES_TRAINER_CLASSES_PATH), customNamesSet.doublesTrainerClasses, StandardCharsets.UTF_8);
        Files.write(Path.of(POKEMON_NICKNAMES_PATH), customNamesSet.pokemonNicknames, StandardCharsets.UTF_8);
    }

    public static CustomNamesSet importOldNames() throws IOException {
        if (fileExists(SysConstants.tnamesFile)) {
            return readRNCNFile(SysConstants.tnamesFile);
        } else if (fileExists(SysConstants.tnamesFile) ||
                fileExists(SysConstants.tclassesFile) || fileExists(SysConstants.nnamesFile)) {
            return readPreRNCNTextFiles(SysConstants.tnamesFile, SysConstants.tclassesFile, SysConstants.nnamesFile);
        } else {
            throw new IOException("Neither RNCN file nor pre-RNCN text name files could be found.");
        }
    }

    // public for testing
    public static CustomNamesSet readRNCNFile(String path) throws IOException {
        InputStream data = openFile(path);
        if (data.read() != CUSTOM_NAMES_VERSION) {
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
        SinglesAndDoublesNames trainerNames = importOldTrainerNames(trainerPath);
        SinglesAndDoublesNames trainerClasses = importOldTrainerClasses(classesPath);
        List<String> pokemonNicknames = importOldNicknames(nicknamesPath);

        return new CustomNamesSet(trainerNames.singles, trainerClasses.singles,
                trainerNames.doubles, trainerClasses.doubles,
                pokemonNicknames);
    }

    private record SinglesAndDoublesNames(List<String> singles, List<String> doubles) { }

    private static SinglesAndDoublesNames importOldTrainerNames(String path) throws IOException {
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

    private static SinglesAndDoublesNames importOldTrainerClasses(String path) throws IOException {
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

    private static List<String> importOldNicknames(String path) throws IOException {
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

        String resourcePath = DEFAULT_FILE_PATH + filename;
        InputStream is = CustomNamesSet.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new FileNotFoundException("Could not find resource " + resourcePath);
        }
        return is;
    }

    private static boolean fileExists(String filename) {
        File fh = new File(RootPath.path + filename);
        if (fh.exists() && fh.canRead()) {
            return true;
        }

        return CustomNamesSet.class.getResource(DEFAULT_FILE_PATH + filename) != null;
    }

    private final List<String> trainerNames;
    private final List<String> trainerClasses;
    private final List<String> doublesTrainerNames;
    private final List<String> doublesTrainerClasses;
    private final List<String> pokemonNicknames;

    public CustomNamesSet(List<String> trainerNames, List<String> trainerClasses,
                          List<String> doublesTrainerNames, List<String> doublesTrainerClasses,
                          List<String> pokemonNicknames) {
        this.trainerNames = trainerNames;
        this.trainerClasses = trainerClasses;
        this.doublesTrainerNames = doublesTrainerNames;
        this.doublesTrainerClasses = doublesTrainerClasses;
        this.pokemonNicknames = pokemonNicknames;
    }

    public List<String> getTrainerNames() {
        return Collections.unmodifiableList(trainerNames);
    }

    public List<String> getTrainerClasses() {
        return Collections.unmodifiableList(trainerClasses);
    }

    public List<String> getDoublesTrainerNames() {
        return Collections.unmodifiableList(doublesTrainerNames);
    }

    public List<String> getDoublesTrainerClasses() {
        return Collections.unmodifiableList(doublesTrainerClasses);
    }

    public List<String> getPokemonNicknames() {
        return Collections.unmodifiableList(pokemonNicknames);
    }

}
