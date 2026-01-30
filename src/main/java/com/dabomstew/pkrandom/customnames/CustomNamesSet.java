package com.dabomstew.pkrandom.customnames;

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

import com.dabomstew.pkrandom.SysConstants;
import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.RootPath;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.zip.CRC32;

public class CustomNamesSet {

    private static final int CUSTOM_NAMES_VERSION = 1;
    private static final String DEFAULT_FILE_PATH = "/com/dabomstew/pkrandom/customnames/";

    public static CustomNamesSet readNamesFromFile() throws IOException {
        InputStream is = openFile(SysConstants.customNamesFile);
        CustomNamesSet cns = new CustomNamesSet(is);
        is.close();
        return cns;
    }

    public static CustomNamesSet importOldNames() throws IOException {
        CustomNamesSet cns = new CustomNamesSet();

        // Trainer Names
        if (fileExists(SysConstants.tnamesFile)) {
            Scanner sc = new Scanner(openFile(SysConstants.tnamesFile), "UTF-8");
            while (sc.hasNextLine()) {
                String trainername = sc.nextLine().trim();
                if (trainername.isEmpty()) {
                    continue;
                }
                if (trainername.startsWith("\uFEFF")) {
                    trainername = trainername.substring(1);
                }
                if (trainername.contains("&")) {
                    cns.doublesTrainerNames.add(trainername);
                } else {
                    cns.trainerNames.add(trainername);
                }
            }
            sc.close();
        }

        // Trainer Classes
        if (fileExists(SysConstants.tclassesFile)) {
            Scanner sc = new Scanner(openFile(SysConstants.tclassesFile), "UTF-8");
            while (sc.hasNextLine()) {
                String trainerClassName = sc.nextLine().trim();
                if (trainerClassName.isEmpty()) {
                    continue;
                }
                if (trainerClassName.startsWith("\uFEFF")) {
                    trainerClassName = trainerClassName.substring(1);
                }
                String checkName = trainerClassName.toLowerCase();
                int idx = (checkName.endsWith("couple") || checkName.contains(" and ") || checkName.endsWith("kin")
                        || checkName.endsWith("team") || checkName.contains("&") || (checkName.endsWith("s") && !checkName
                        .endsWith("ss"))) ? 1 : 0;
                if (idx == 1) {
                    cns.doublesTrainerClasses.add(trainerClassName);
                } else {
                    cns.trainerClasses.add(trainerClassName);
                }
            }
            sc.close();
        }

        // Nicknames
        if (fileExists(SysConstants.nnamesFile)) {
            Scanner sc = new Scanner(openFile(SysConstants.nnamesFile), "UTF-8");
            while (sc.hasNextLine()) {
                String nickname = sc.nextLine().trim();
                if (nickname.isEmpty()) {
                    continue;
                }
                if (nickname.startsWith("\uFEFF")) {
                    nickname = nickname.substring(1);
                }
                cns.pokemonNicknames.add(nickname);
            }
            sc.close();
        }

        return cns;
    }

    private static InputStream openFile(String filename) throws IOException {
        File fh = new File(RootPath.path + filename);
        if (fh.exists() && fh.canRead()) {
            return Files.newInputStream(fh.toPath());
        }

        String resourcePath = DEFAULT_FILE_PATH + filename;
        InputStream is = FileFunctions.class.getResourceAsStream(resourcePath);
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

        return FileFunctions.class.getResource(DEFAULT_FILE_PATH + filename) != null;
    }

    // Custom Names use TWO custom check sum methods for whatever reason.
    // It might be possible to replace them with something more standard,
    // but am not looking into that now. -- voliol 2025-04-27

    public static int getFileChecksum() {
        try {
            Scanner sc = new Scanner(openFile(SysConstants.customNamesFile), "UTF-8");
            CRC32 checksum = new CRC32();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty()) {
                    checksum.update(line.getBytes(StandardCharsets.UTF_8));
                }
            }
            sc.close();
            return (int) checksum.getValue();
        } catch (IOException e) {
            return 0;
        }
    }

    public static boolean checkOtherCRC(byte[] data, int byteIndex, int switchIndex, int offsetInData) {
        // If the switch at data[byteIndex].switchIndex is on, then check that
        // the CRC at data[offsetInData] ... data[offsetInData+3] matches the
        // CRC of filename.
        // If not, return false.
        // If any other case, return true.
        int switches = data[byteIndex] & 0xFF;
        if (((switches >> switchIndex) & 0x01) == 0x01) {
            // have to check the CRC
            int crc = FileFunctions.readFullIntBigEndian(data, offsetInData);

            return getFileChecksum() == crc;
        }
        return true;
    }

    private final List<String> trainerNames;
    private final List<String> trainerClasses;
    private final List<String> doublesTrainerNames;
    private final List<String> doublesTrainerClasses;
    private final List<String> pokemonNicknames;

    // Standard constructor: read binary data from an input stream.
    public CustomNamesSet(InputStream data) throws IOException {

        if (data.read() != CUSTOM_NAMES_VERSION) {
            throw new IOException("Invalid custom names file provided.");
        }

        trainerNames = readNamesBlock(data);
        trainerClasses = readNamesBlock(data);
        doublesTrainerNames = readNamesBlock(data);
        doublesTrainerClasses = readNamesBlock(data);
        pokemonNicknames = readNamesBlock(data);
    }

    // Alternate constructor: blank all lists
    // Used for importing old names and on the editor dialog.
    public CustomNamesSet() {
        trainerNames = new ArrayList<>();
        trainerClasses = new ArrayList<>();
        doublesTrainerNames = new ArrayList<>();
        doublesTrainerClasses = new ArrayList<>();
        pokemonNicknames = new ArrayList<>();
    }

    private List<String> readNamesBlock(InputStream in) throws IOException {
        // Read the size of the block to come.
        byte[] szData = FileFunctions.readFullyIntoBuffer(in, 4);
        int size = FileFunctions.readFullIntBigEndian(szData, 0);
        if (in.available() < size) {
            throw new IOException("Invalid size specified.");
        }

        // Read the block and translate it into a list of names.
        byte[] namesData = FileFunctions.readFullyIntoBuffer(in, size);
        List<String> names = new ArrayList<>();
        Scanner sc = new Scanner(new ByteArrayInputStream(namesData),"UTF-8");
        while (sc.hasNextLine()) {
            String name = sc.nextLine().trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        sc.close();

        return names;
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        baos.write(CUSTOM_NAMES_VERSION);

        writeNamesBlock(baos, trainerNames);
        writeNamesBlock(baos, trainerClasses);
        writeNamesBlock(baos, doublesTrainerNames);
        writeNamesBlock(baos, doublesTrainerClasses);
        writeNamesBlock(baos, pokemonNicknames);

        return baos.toByteArray();
    }

    private void writeNamesBlock(OutputStream out, List<String> names) throws IOException {
        StringBuilder outNames = new StringBuilder();
        boolean first = true;
        for (String name : names) {
            if (!first) {
                outNames.append(System.lineSeparator());
            }
            first = false;
            outNames.append(name);
        }
        byte[] namesData = outNames.toString().getBytes(StandardCharsets.UTF_8);
        byte[] szData = new byte[4];
        FileFunctions.writeFullIntBigEndian(szData, 0, namesData.length);
        out.write(szData);
        out.write(namesData);
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
    
    public void setTrainerNames(List<String> names) {
        trainerNames.clear();
        trainerNames.addAll(names);
    }
    
    public void setTrainerClasses(List<String> names) {
        trainerClasses.clear();
        trainerClasses.addAll(names);
    }
    
    public void setDoublesTrainerNames(List<String> names) {
        doublesTrainerNames.clear();
        doublesTrainerNames.addAll(names);
    }
    
    public void setDoublesTrainerClasses(List<String> names) {
        doublesTrainerClasses.clear();
        doublesTrainerClasses.addAll(names);
    }
    
    public void setPokemonNicknames(List<String> names) {
        pokemonNicknames.clear();
        pokemonNicknames.addAll(names);
    }

}
