package com.dabomstew.pkromio.ctr;

/*----------------------------------------------------------------------------*/
/*--  NCCH.java - a base class for dealing with 3DS NCCH ROM images.        --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
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

import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.RootPath;
import com.dabomstew.pkromio.exceptions.EncryptedROMException;
import com.dabomstew.pkromio.exceptions.RomIOException;
import cuecompressors.BLZCoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NCCH {
    private String romFilename;
    private RandomAccessFile baseRom;
    private long ncchStartingOffset;
    private String productCode;
    private String titleId;
    private int version;
    private long exefsOffset, romfsOffset, fileDataOffset;
    private ExefsFileHeader codeFileHeader;
    private SMDH smdh;
    private List<ExefsFileHeader> extraExefsFiles;
    private List<FileMetadata> fileMetadataList;
    private Map<String, RomfsFile> romfsFiles;
    private boolean romOpen;
    private String tmpFolder;
    private boolean writingEnabled;
    private boolean codeCompressed, codeOpen, codeChanged;
    private byte[] codeRamstored;

    // Public so the base game can read it from the game update NCCH
    public long originalCodeCRC, originalRomfsHeaderCRC;

    private static final int media_unit_size = 0x200;
    private static final int header_and_exheader_size = 0xA00;
    private static final int ncsd_magic = 0x4E435344;
    private static final int cia_header_size = 0x2020;
    private static final int ncch_magic = 0x4E434348;
    private static final int ncch_and_ncsd_magic_offset = 0x100;
    private static final int exefs_header_size = 0x200;
    private static final int romfs_header_size = 0x5C;
    private static final int romfs_magic_1 = 0x49564643;
    private static final int romfs_magic_2 = 0x00000100;
    private static final int level3_header_size = 0x28;
    private static final int metadata_unused = 0xFFFFFFFF;

    public NCCH(String filename, String productCode, String titleId) throws IOException {
        this.romFilename = filename;
        this.baseRom = new RandomAccessFile(filename, "r");
        this.ncchStartingOffset = NCCH.getCXIOffsetInFile(filename);
        this.productCode = productCode;
        this.titleId = titleId;
        this.romOpen = true;

        if (this.ncchStartingOffset != -1) {
            this.version = this.readVersionFromFile();
        }

        // TMP folder?
        String rawFilename = new File(filename).getName();
        String dataFolder = "tmp_" + rawFilename.substring(0, rawFilename.lastIndexOf('.'));
        // remove nonsensical chars
        dataFolder = dataFolder.replaceAll("[^A-Za-z0-9_]+", "");
        File tmpFolder = new File(RootPath.path + dataFolder);
        tmpFolder.mkdirs();
        if (tmpFolder.canWrite()) {
            writingEnabled = true;
            this.tmpFolder = RootPath.path + dataFolder + File.separator;
            tmpFolder.deleteOnExit();
        } else {
            writingEnabled = false;
        }

        // The below code handles things "wrong" with regards to encrypted ROMs. We just
        // blindly treat the ROM as decrypted and try to parse all of its data, when we
        // *should* be looking at the header of the ROM to determine if the ROM is encrypted.
        // Unfortunately, many people have poorly-decrypted ROMs that do not properly set
        // the bytes on the NCCH header, so we can't assume that the header is telling the
        // truth. If we read the whole ROM without crashing, then it's probably decrypted.
        try {
            readFileSystem();
        } catch (Exception ex) {
            if (!this.isDecrypted()) {
                throw new EncryptedROMException(ex);
            } else {
                throw ex;
            }
        }
    }

    public void reopenROM() throws IOException {
        if (!this.romOpen) {
            baseRom = new RandomAccessFile(this.romFilename, "r");
            romOpen = true;
        }
    }

    public void closeROM() throws IOException {
        if (this.romOpen && baseRom != null) {
            baseRom.close();
            baseRom = null;
            romOpen = false;
        }
    }

    private void readFileSystem() throws IOException {
        exefsOffset = ncchStartingOffset + FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x1A0) * media_unit_size;
        romfsOffset = ncchStartingOffset + FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x1B0) * media_unit_size;
        baseRom.seek(ncchStartingOffset + 0x20D);
        byte systemControlInfoFlags = baseRom.readByte();
        codeCompressed = (systemControlInfoFlags & 0x01) != 0;
        readExefs();
        readRomfs();
    }

    private void readExefs() throws IOException {
        System.out.println("NCCH: Reading exefs...");
        byte[] exefsHeaderData = new byte[exefs_header_size];
        baseRom.seek(exefsOffset);
        baseRom.readFully(exefsHeaderData);

        ExefsFileHeader[] fileHeaders = new ExefsFileHeader[10];
        for (int i = 0; i < 10; i++) {
            fileHeaders[i] = new ExefsFileHeader(exefsHeaderData, i * 0x10);
        }

        extraExefsFiles = new ArrayList<>();
        for (ExefsFileHeader fileHeader : fileHeaders) {
            if (fileHeader.isValid() && fileHeader.filename.equals(".code")) {
                codeFileHeader = fileHeader;
            } else if (fileHeader.isValid()) {
                extraExefsFiles.add(fileHeader);
            }

            if (fileHeader.isValid() && fileHeader.filename.equals("icon")) {
                byte[] smdhBytes = new byte[fileHeader.size];
                baseRom.seek(exefsOffset + 0x200 + fileHeader.offset);
                baseRom.readFully(smdhBytes);
                smdh = new SMDH(smdhBytes);
            }
        }
        System.out.println("NCCH: Done reading exefs");
    }

    private void readRomfs() throws IOException {
        System.out.println("NCCH: Reading romfs...");
        byte[] romfsHeaderData = new byte[romfs_header_size];
        baseRom.seek(romfsOffset);
        baseRom.readFully(romfsHeaderData);
        originalRomfsHeaderCRC = FileFunctions.getCRC32(romfsHeaderData);
        int magic1 = FileFunctions.readFullIntBigEndian(romfsHeaderData, 0x00);
        int magic2 = FileFunctions.readFullIntBigEndian(romfsHeaderData, 0x04);
        if (magic1 != romfs_magic_1 || magic2 != romfs_magic_2) {
            System.err.println("NCCH: romfs does not contain magic values");
            // Not a valid romfs
            return;
        }
        int masterHashSize = FileFunctions.readFullInt(romfsHeaderData, 0x08);
        int level3HashBlockSize = 1 << FileFunctions.readFullInt(romfsHeaderData, 0x4C);
        long level3Offset = romfsOffset + alignLong(0x60 + masterHashSize, level3HashBlockSize);

        byte[] level3HeaderData = new byte[level3_header_size];
        baseRom.seek(level3Offset);
        baseRom.readFully(level3HeaderData);
        int headerLength = FileFunctions.readFullInt(level3HeaderData, 0x00);
        if (headerLength != level3_header_size) {
            // Not a valid romfs
            System.err.println("NCCH: romfs does not have a proper level 3 header");
            return;
        }
        int directoryMetadataOffset = FileFunctions.readFullInt(level3HeaderData, 0x0C);
        int directoryMetadataLength = FileFunctions.readFullInt(level3HeaderData, 0x10);
        int fileMetadataOffset = FileFunctions.readFullInt(level3HeaderData, 0x1c);
        int fileMetadataLength = FileFunctions.readFullInt(level3HeaderData, 0x20);
        int fileDataOffsetFromHeaderStart = FileFunctions.readFullInt(level3HeaderData, 0x24);
        fileDataOffset = level3Offset + fileDataOffsetFromHeaderStart;

        byte[] directoryMetadataBlock = new byte[directoryMetadataLength];
        baseRom.seek(level3Offset + directoryMetadataOffset);
        baseRom.readFully(directoryMetadataBlock);
        byte[] fileMetadataBlock = new byte[fileMetadataLength];
        baseRom.seek(level3Offset + fileMetadataOffset);
        baseRom.readFully(fileMetadataBlock);
        fileMetadataList = new ArrayList<>();
        romfsFiles = new TreeMap<>();
        visitDirectory(0, "", directoryMetadataBlock, fileMetadataBlock);
        System.out.println("NCCH: Done reading romfs");
    }

    private void visitDirectory(int offset, String rootPath, byte[] directoryMetadataBlock, byte[] fileMetadataBlock) {
        DirectoryMetadata metadata = new DirectoryMetadata(directoryMetadataBlock, offset);
        String currentPath = rootPath;
        if (!metadata.name.equals("")) {
            currentPath = rootPath + metadata.name + "/";
        }

        if (metadata.firstFileOffset != metadata_unused) {
            visitFile(metadata.firstFileOffset, currentPath, fileMetadataBlock);
        }
        if (metadata.firstChildDirectoryOffset != metadata_unused) {
            visitDirectory(metadata.firstChildDirectoryOffset, currentPath, directoryMetadataBlock, fileMetadataBlock);
        }
        if (metadata.siblingDirectoryOffset != metadata_unused) {
            visitDirectory(metadata.siblingDirectoryOffset, rootPath, directoryMetadataBlock, fileMetadataBlock);
        }
    }

    private void visitFile(int offset, String rootPath, byte[] fileMetadataBlock) {
        FileMetadata metadata = new FileMetadata(fileMetadataBlock, offset);
        String currentPath = rootPath + metadata.name;
        //System.out.println("NCCH: Visiting file " + currentPath);
        RomfsFile file = new RomfsFile(this);
        file.offset = fileDataOffset + metadata.fileDataOffset;
        file.size = (int) metadata.fileDataLength;  // no Pokemon game has a file larger than unsigned int max
        file.fullPath = currentPath;
        metadata.file = file;
        fileMetadataList.add(metadata);
        romfsFiles.put(currentPath, file);
        if (metadata.siblingFileOffset != metadata_unused) {
            visitFile(metadata.siblingFileOffset, rootPath, fileMetadataBlock);
        }
    }

    public void saveAsNCCH(String filename, String gameAcronym, long seed) throws IOException, NoSuchAlgorithmException {
        this.reopenROM();

        // Initialize new ROM
        RandomAccessFile fNew = new RandomAccessFile(filename, "rw");

        // Read the header and exheader and write it to the output ROM
        byte[] header = new byte[header_and_exheader_size];
        baseRom.seek(ncchStartingOffset);
        baseRom.readFully(header);
        fNew.write(header);

        // Just in case they were set wrong in the original header, let's correctly set the
        // bytes in the header to indicate the output ROM is decrypted
        byte[] flags = new byte[8];
        baseRom.seek(ncchStartingOffset + 0x188);
        baseRom.readFully(flags);
        flags[3] = 0;
        flags[7] = 4;
        fNew.seek(0x188);
        fNew.write(flags);

        // The logo is small enough (8KB) to just read the whole thing into memory. Write it to the new ROM directly
        // after the header, then update the new ROM's logo offset
        long logoOffset = ncchStartingOffset + FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x198) * media_unit_size;
        long logoLength = FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x19C) * media_unit_size;
        if (logoLength > 0) {
            byte[] logo = new byte[(int) logoLength];
            baseRom.seek(logoOffset);
            baseRom.readFully(logo);
            long newLogoOffset = header_and_exheader_size;
            fNew.seek(newLogoOffset);
            fNew.write(logo);
            fNew.seek(0x198);
            fNew.write((int) newLogoOffset / media_unit_size);
        }

        // The plain region is even smaller (1KB) so repeat the same process
        long plainOffset = ncchStartingOffset + FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x190) * media_unit_size;
        long plainLength = FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x194) * media_unit_size;
        if (plainLength > 0) {
            byte[] plain = new byte[(int) plainLength];
            baseRom.seek(plainOffset);
            baseRom.readFully(plain);
            long newPlainOffset = header_and_exheader_size + logoLength;
            fNew.seek(newPlainOffset);
            fNew.write(plain);
            fNew.seek(0x190);
            fNew.write((int) newPlainOffset / media_unit_size);
        }

        // Update the SMDH so that Citra displays the seed in the title
        smdh.setAllDescriptions(gameAcronym + " randomizer seed: " + seed);
        smdh.setAllPublishers("Universal Pokemon Randomizer FVX");

        // Now, reconstruct the exefs based on our new version of .code and our new SMDH
        long newExefsOffset = header_and_exheader_size + logoLength + plainLength;
        long newExefsLength = rebuildExefs(fNew, newExefsOffset);
        fNew.seek(0x1A0);
        fNew.write((int) newExefsOffset / media_unit_size);
        fNew.seek(0x1A4);
        fNew.write((int) newExefsLength / media_unit_size);

        // Then, reconstruct the romfs
        // TODO: Fix the yet-unsolved alignment issues in rebuildRomfs when you remove this align
        long newRomfsOffset = alignLong(header_and_exheader_size + logoLength + plainLength + newExefsLength, 4096);
        long newRomfsLength = rebuildRomfs(fNew, newRomfsOffset);
        fNew.seek(0x1B0);
        fNew.write((int) newRomfsOffset / media_unit_size);
        fNew.seek(0x1B4);
        fNew.write((int) newRomfsLength / media_unit_size);

        // Lastly, reconstruct the superblock hashes
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int exefsHashRegionSize = FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x1A8) * media_unit_size;
        byte[] exefsDataToHash = new byte[exefsHashRegionSize];
        fNew.seek(newExefsOffset);
        fNew.readFully(exefsDataToHash);
        byte[] exefsSuperblockHash = digest.digest(exefsDataToHash);
        fNew.seek(0x1C0);
        fNew.write(exefsSuperblockHash);
        int romfsHashRegionSize = FileFunctions.readIntFromFile(baseRom, ncchStartingOffset + 0x1B8) * media_unit_size;
        byte[] romfsDataToHash = new byte[romfsHashRegionSize];
        fNew.seek(newRomfsOffset);
        fNew.readFully(romfsDataToHash);
        byte[] romfsSuperblockHash = digest.digest(romfsDataToHash);
        fNew.seek(0x1E0);
        fNew.write(romfsSuperblockHash);

        // While totally optional, let's zero out the NCCH signature so that
        // it's clear this isn't a properly-signed ROM
        byte[] zeroedSignature = new byte[0x100];
        fNew.seek(0x0);
        fNew.write(zeroedSignature);
        fNew.close();
    }

    private long rebuildExefs(RandomAccessFile fNew, long newExefsOffset) throws IOException, NoSuchAlgorithmException {
        System.out.println("NCCH: Rebuilding exefs...");
        byte[] code = getCode();
        if (codeCompressed) {
            code = new BLZCoder(null).BLZ_EncodePub(code, false, true, ".code");
        }

        // Create a new ExefsFileHeader for our updated .code
        ExefsFileHeader newCodeHeader = new ExefsFileHeader();
        newCodeHeader.filename = codeFileHeader.filename;
        newCodeHeader.size = code.length;
        newCodeHeader.offset = 0;

        // For all the file headers, write them to the new ROM and store them in order for hashing later
        ExefsFileHeader[] newHeaders = new ExefsFileHeader[10];
        newHeaders[0] = newCodeHeader;
        fNew.seek(newExefsOffset);
        fNew.write(newCodeHeader.asBytes());
        for (int i = 0; i < extraExefsFiles.size(); i++) {
            ExefsFileHeader header = extraExefsFiles.get(i);
            newHeaders[i + 1] = header;
            fNew.write(header.asBytes());
        }

        // Write the file data, then hash the data and write the hashes in reverse order
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        long endingOffset = 0;
        for (int i = 0; i < newHeaders.length; i++) {
            ExefsFileHeader header = newHeaders[i];
            if (header != null) {
                byte[] data;
                if (header.filename.equals(".code")) {
                    data = code;
                } else if (header.filename.equals("icon")) {
                    data = smdh.getBytes();
                } else {
                    long dataOffset = exefsOffset + 0x200 + header.offset;
                    data = new byte[header.size];
                    baseRom.seek(dataOffset);
                    baseRom.readFully(data);
                }
                fNew.seek(newExefsOffset + 0x200 + header.offset);
                fNew.write(data);
                byte[] hash = digest.digest(data);
                fNew.seek(newExefsOffset + 0x200 - ((i + 1) * 0x20));
                fNew.write(hash);
                endingOffset = newExefsOffset + 0x200 + header.offset + header.size;
            }
        }

        // Pad to media unit size
        fNew.seek(endingOffset);
        long exefsLength = endingOffset - newExefsOffset;
        while (exefsLength % media_unit_size != 0) {
            fNew.writeByte(0);
            exefsLength++;
        }

        System.out.println("NCCH: Done rebuilding exefs");
        return exefsLength;
    }

    private long rebuildRomfs(RandomAccessFile fNew, long newRomfsOffset) throws IOException, NoSuchAlgorithmException {
        System.out.println("NCCH: Rebuilding romfs...");

        // Start by copying the romfs header straight from the original ROM. We'll update the
        // header as we continue to build the romfs
        byte[] romfsHeaderData = new byte[romfs_header_size];
        baseRom.seek(romfsOffset);
        baseRom.readFully(romfsHeaderData);
        fNew.seek(newRomfsOffset);
        fNew.write(romfsHeaderData);

        // Now find the level 3 (file data) offset, since the first thing we need to do is write the
        // updated file data. We're assuming here that the master hash size is smaller than the level 3
        // hash block size, which it almost certainly will because we're not adding large amounts of data
        // to the romfs
        int masterHashSize = FileFunctions.readFullInt(romfsHeaderData, 0x08);
        int level3HashBlockSize = 1 << FileFunctions.readFullInt(romfsHeaderData, 0x4C);
        long level3Offset = romfsOffset + alignLong(0x60 + masterHashSize, level3HashBlockSize);
        long newLevel3Offset = newRomfsOffset + alignLong(0x60 + masterHashSize, level3HashBlockSize);

        // Copy the level 3 header straight from the original ROM. Since we're not adding or
        // removing any files, the File/Directory tables should have the same offsets and lengths
        byte[] level3HeaderData = new byte[level3_header_size];
        baseRom.seek(level3Offset);
        baseRom.readFully(level3HeaderData);
        fNew.seek(newLevel3Offset);
        fNew.write(level3HeaderData);

        // Write out both hash tables and the directory metadata table. Since we're not adding or removing
        // any files/directories, we can just use what's in the base ROM for this.
        int directoryHashTableOffset = FileFunctions.readFullInt(level3HeaderData, 0x04);
        int directoryHashTableLength = FileFunctions.readFullInt(level3HeaderData, 0x08);
        int directoryMetadataTableOffset = FileFunctions.readFullInt(level3HeaderData, 0x0C);
        int directoryMetadataTableLength = FileFunctions.readFullInt(level3HeaderData, 0x10);
        int fileHashTableOffset = FileFunctions.readFullInt(level3HeaderData, 0x14);
        int fileHashTableLength = FileFunctions.readFullInt(level3HeaderData, 0x18);
        byte[] directoryHashTable = new byte[directoryHashTableLength];
        baseRom.seek(level3Offset + directoryHashTableOffset);
        baseRom.readFully(directoryHashTable);
        fNew.seek(newLevel3Offset + directoryHashTableOffset);
        fNew.write(directoryHashTable);
        byte[] directoryMetadataTable = new byte[directoryMetadataTableLength];
        baseRom.seek(level3Offset + directoryMetadataTableOffset);
        baseRom.readFully(directoryMetadataTable);
        fNew.seek(newLevel3Offset + directoryMetadataTableOffset);
        fNew.write(directoryMetadataTable);
        byte[] fileHashTable = new byte[fileHashTableLength];
        baseRom.seek(level3Offset + fileHashTableOffset);
        baseRom.readFully(fileHashTable);
        fNew.seek(newLevel3Offset + fileHashTableOffset);
        fNew.write(fileHashTable);

        // Now reconstruct the file metadata table. It may need to be changed if any file grew or shrunk
        int fileMetadataTableOffset = FileFunctions.readFullInt(level3HeaderData, 0x1C);
        int fileMetadataTableLength = FileFunctions.readFullInt(level3HeaderData, 0x20);
        byte[] newFileMetadataTable = updateFileMetadataTable(fileMetadataTableLength);
        fNew.seek(newLevel3Offset + fileMetadataTableOffset);
        fNew.write(newFileMetadataTable);

        // Using the new file metadata table, output the file data
        int fileDataOffset = FileFunctions.readFullInt(level3HeaderData, 0x24);
        long endOfFileDataOffset = 0;
        for (FileMetadata metadata : fileMetadataList) {
            System.out.println("NCCH: Writing file " + metadata.file.fullPath + " to romfs");
            // Users have sent us bug reports with really bizarre errors here that seem to indicate
            // broken metadata; do this in a try-catch solely so we can log the metadata if we fail
            try {
                byte[] fileData;
                if (metadata.file.fileChanged) {
                    fileData = metadata.file.getOverrideContents();
                } else {
                    fileData = new byte[metadata.file.size];
                    baseRom.seek(metadata.file.offset);
                    baseRom.readFully(fileData);
                }
                long currentDataOffset = newLevel3Offset + fileDataOffset + metadata.fileDataOffset;
                fNew.seek(currentDataOffset);
                fNew.write(fileData);
                endOfFileDataOffset = currentDataOffset + fileData.length;
            } catch (Exception e) {
                String message = String.format("Error when building romfs: File: %s, offset: %s, size: %s",
                        metadata.file.fullPath, metadata.offset, metadata.file.size);
                throw new RomIOException(message, e);
            }
        }

        // Now that level 3 (file data) is done, construct level 2 (hashes of file data)
        // Note that in the ROM, level 1 comes *before* level 2, so we need to calculate
        // level 1 length and offset as well.
        long newLevel3EndingOffset = endOfFileDataOffset;
        long newLevel3HashdataSize = newLevel3EndingOffset - newLevel3Offset;
        long numberOfLevel3HashBlocks = alignLong(newLevel3HashdataSize, level3HashBlockSize) / level3HashBlockSize;
        int level2HashBlockSize = 1 << FileFunctions.readFullInt(romfsHeaderData, 0x34);
        long newLevel2HashdataSize = numberOfLevel3HashBlocks * 0x20;
        long numberOfLevel2HashBlocks = alignLong(newLevel2HashdataSize, level2HashBlockSize) / level2HashBlockSize;
        int level1HashBlockSize = 1 << FileFunctions.readFullInt(romfsHeaderData, 0x1C);
        long newLevel1HashdataSize = numberOfLevel2HashBlocks * 0x20;
        long newLevel1Offset = newLevel3Offset + alignLong(newLevel3HashdataSize, level3HashBlockSize);
        long newLevel2Offset = newLevel1Offset + alignLong(newLevel1HashdataSize, level1HashBlockSize);
        long newFileEndingOffset = alignLong(newLevel2Offset + newLevel2HashdataSize, level2HashBlockSize);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dataToHash = new byte[level3HashBlockSize];
        for (long i = 0; i < numberOfLevel3HashBlocks; i++) {
            fNew.seek(newLevel3Offset + (i * level3HashBlockSize));
            fNew.readFully(dataToHash);
            byte[] hash = digest.digest(dataToHash);
            fNew.seek(newLevel2Offset + (i * 0x20));
            fNew.write(hash);
        }
        while (fNew.getFilePointer() != newFileEndingOffset) {
            fNew.writeByte(0);
        }

        // Now that level 2 (hashes of file data) is done, construct level 1 (hashes of
        // hashes of file data) and the master hash/level 0 (hashes of level 1)
        dataToHash = new byte[level2HashBlockSize];
        for (long i = 0; i < numberOfLevel2HashBlocks; i++) {
            fNew.seek(newLevel2Offset + (i * level2HashBlockSize));
            fNew.readFully(dataToHash);
            byte[] hash = digest.digest(dataToHash);
            fNew.seek(newLevel1Offset + (i * 0x20));
            fNew.write(hash);
        }
        long numberOfLevel1HashBlocks = alignLong(newLevel1HashdataSize, level1HashBlockSize) / level1HashBlockSize;
        dataToHash = new byte[level1HashBlockSize];
        for (long i = 0; i < numberOfLevel1HashBlocks; i++) {
            fNew.seek(newLevel1Offset + (i * level1HashBlockSize));
            fNew.readFully(dataToHash);
            byte[] hash = digest.digest(dataToHash);
            fNew.seek(newRomfsOffset + 0x60 + (i * 0x20));
            fNew.write(hash);
        }

        // Lastly, update the header and return the size of the new romfs
        long level1LogicalOffset = 0;
        long level2LogicalOffset = alignLong(newLevel1HashdataSize, level1HashBlockSize);
        long level3LogicalOffset = alignLong(level2LogicalOffset + newLevel2HashdataSize, level2HashBlockSize);
        FileFunctions.writeFullInt(romfsHeaderData, 0x08, (int) numberOfLevel1HashBlocks * 0x20);
        FileFunctions.writeFullLong(romfsHeaderData, 0x0C, level1LogicalOffset);
        FileFunctions.writeFullLong(romfsHeaderData, 0x14, newLevel1HashdataSize);
        FileFunctions.writeFullLong(romfsHeaderData, 0x24, level2LogicalOffset);
        FileFunctions.writeFullLong(romfsHeaderData, 0x2C, newLevel2HashdataSize);
        FileFunctions.writeFullLong(romfsHeaderData, 0x3C, level3LogicalOffset);
        FileFunctions.writeFullLong(romfsHeaderData, 0x44, newLevel3HashdataSize);
        fNew.seek(newRomfsOffset);
        fNew.write(romfsHeaderData);
        long currentLength = newFileEndingOffset - newRomfsOffset;
        long newRomfsLength = alignLong(currentLength, media_unit_size);
        fNew.seek(newFileEndingOffset);
        while (fNew.getFilePointer() < newRomfsOffset + newRomfsLength) {
            fNew.writeByte(0);
        }

        System.out.println("NCCH: Done rebuilding romfs");
        return newRomfsLength;
    }

    private byte[] updateFileMetadataTable(int fileMetadataTableLength) {
        fileMetadataList.sort((FileMetadata f1, FileMetadata f2) -> (int) (f1.fileDataOffset - f2.fileDataOffset));
        byte[] fileMetadataTable = new byte[fileMetadataTableLength];
        int currentTableOffset = 0;
        long currentFileDataOffset = 0;
        for (FileMetadata metadata : fileMetadataList) {
            metadata.fileDataOffset = currentFileDataOffset;
            if (metadata.file.fileChanged) {
                metadata.fileDataLength = metadata.file.size;
            }
            byte[] metadataBytes = metadata.asBytes();
            System.arraycopy(metadataBytes, 0, fileMetadataTable, currentTableOffset, metadataBytes.length);
            currentTableOffset += metadataBytes.length;
            currentFileDataOffset += metadata.fileDataLength;
        }
        return fileMetadataTable;
    }

    public void saveAsLayeredFS(String outputPath) throws IOException {
        String layeredFSRootPath = outputPath + File.separator + titleId + File.separator;
        File layeredFSRootDir = new File(layeredFSRootPath);
        if (!layeredFSRootDir.exists()) {
            layeredFSRootDir.mkdirs();
        } else {
            purgeDirectory(layeredFSRootDir);
        }
        String romfsRootPath = layeredFSRootPath + "romfs" + File.separator;
        File romfsDir = new File(romfsRootPath);
        if (!romfsDir.exists()) {
            romfsDir.mkdirs();
        }

        if (codeChanged) {
            byte[] code = getCode();
            FileOutputStream fos = new FileOutputStream(new File(layeredFSRootPath + "code.bin"));
            fos.write(code);
            fos.close();
        }

        for (Map.Entry<String, RomfsFile> entry : romfsFiles.entrySet()) {
            RomfsFile file = entry.getValue();
            if (file.fileChanged) {
                writeRomfsFileToLayeredFS(file, romfsRootPath);
            }
        }
    }

    private void purgeDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                purgeDirectory(file);
            }
            file.delete();
        }
    }

    private void writeRomfsFileToLayeredFS(RomfsFile file, String layeredFSRootPath) throws IOException {
        String[] romfsPathComponents = file.fullPath.split("/");
        StringBuffer buffer = new StringBuffer(layeredFSRootPath);
        for (int i = 0; i < romfsPathComponents.length - 1; i++) {
            buffer.append(romfsPathComponents[i]);
            buffer.append(File.separator);
            File currentDir = new File(buffer.toString());
            if (!currentDir.exists()) {
                currentDir.mkdirs();
            }
        }
        buffer.append(romfsPathComponents[romfsPathComponents.length - 1]);
        String romfsFilePath = buffer.toString();
        FileOutputStream fos = new FileOutputStream(new File(romfsFilePath));
        fos.write(file.getOverrideContents());
        fos.close();
    }

    public boolean isDecrypted() throws IOException {
        // This is the way you're *supposed* to tell if a ROM is decrypted. Specifically, this
        // is checking the noCrypto flag on the NCCH bitflags.
        long ncchFlagOffset = ncchStartingOffset + 0x188;
        byte[] ncchFlags = new byte[8];
        baseRom.seek(ncchFlagOffset);
        baseRom.readFully(ncchFlags);
        if ((ncchFlags[7] & 0x4) != 0) {
            return true;
        }

        // However, some poorly-decrypted ROMs don't set this flag. So our heuristic for detecting
        // if they're decrypted is to check whether the battle CRO exists, since all 3DS Pokemon
        // games and updates have this file. If the game is *really* encrypted, then the odds of us
        // successfully extracting this exact name from the metadata tables is like one in a billion.
        return romfsFiles != null && (romfsFiles.containsKey("DllBattle.cro") || romfsFiles.containsKey("Battle.cro"));
    }

    // Retrieves a decompressed version of .code (the game's executable).
    // The first time this is called, it will retrieve it straight from the
    // exefs. Future calls will rely on a cached version to speed things up.
    // If writing is enabled, it will cache the decompressed version to the
    // tmpFolder; otherwise, it will store it in RAM.
    public byte[] getCode() throws IOException {
        if (!codeOpen) {
            codeOpen = true;
            byte[] code = new byte[codeFileHeader.size];

            // File header offsets are from the start of the exefs but *exclude* the
            // size of the exefs header, so we need to add it back ourselves.
            baseRom.seek(exefsOffset + exefs_header_size + codeFileHeader.offset);
            baseRom.readFully(code);
            originalCodeCRC = FileFunctions.getCRC32(code);

            if (codeCompressed) {
                code = new BLZCoder(null).BLZ_DecodePub(code, ".code");
            }

            // Now actually make the copy or w/e
            if (writingEnabled) {
                File arm9file = new File(tmpFolder + ".code");
                FileOutputStream fos = new FileOutputStream(arm9file);
                fos.write(code);
                fos.close();
                arm9file.deleteOnExit();
                this.codeRamstored = null;
                return code;
            } else {
                this.codeRamstored = code;
                byte[] newcopy = new byte[code.length];
                System.arraycopy(code, 0, newcopy, 0, code.length);
                return newcopy;
            }
        } else {
            if (writingEnabled) {
                return FileFunctions.readFileFullyIntoBuffer(tmpFolder + ".code");
            } else {
                byte[] newcopy = new byte[this.codeRamstored.length];
                System.arraycopy(this.codeRamstored, 0, newcopy, 0, this.codeRamstored.length);
                return newcopy;
            }
        }
    }

    public void writeCode(byte[] code) throws IOException {
        if (!codeOpen) {
            getCode();
        }
        codeChanged = true;
        if (writingEnabled) {
            FileOutputStream fos = new FileOutputStream(new File(tmpFolder + ".code"));
            fos.write(code);
            fos.close();
        } else {
            if (this.codeRamstored.length == code.length) {
                // copy new in
                System.arraycopy(code, 0, this.codeRamstored, 0, code.length);
            } else {
                // make new array
                this.codeRamstored = null;
                this.codeRamstored = new byte[code.length];
                System.arraycopy(code, 0, this.codeRamstored, 0, code.length);
            }
        }
    }

    public Set<String> getFileNames() {
        return Collections.unmodifiableSet(romfsFiles.keySet());
    }

    public boolean hasFile(String filename) {
        return romfsFiles.containsKey(filename);
    }

    // returns null if file doesn't exist
    public byte[] getFile(String filename) throws IOException {
        if (romfsFiles.containsKey(filename)) {
            return romfsFiles.get(filename).getContents();
        } else {
            return null;
        }
    }

    public void writeFile(String filename, byte[] data) throws IOException {
        if (romfsFiles.containsKey(filename)) {
            romfsFiles.get(filename).writeOverride(data);
        }
    }

    public void printRomDiagnostics(PrintStream logStream, NCCH gameUpdate) {
        Path p = Paths.get(this.romFilename);
        logStream.println("File name: " + p.getFileName().toString());
        if (gameUpdate == null) {
            logStream.println(".code: " + String.format("%08X", this.originalCodeCRC));
        } else {
            logStream.println(".code: " + String.format("%08X", gameUpdate.originalCodeCRC));
        }
        logStream.println("romfs header: " + String.format("%08X", this.originalRomfsHeaderCRC));
        if (gameUpdate != null) {
            logStream.println("romfs header (game update): " + String.format("%08X", gameUpdate.originalRomfsHeaderCRC));
        }
        List<String> fileList = new ArrayList<>();
        Map<String, String> baseRomfsFileDiagnostics = this.getRomfsFilesDiagnostics();
        Map<String, String> updateRomfsFileDiagnostics = new HashMap<>();
        if (gameUpdate != null) {
            updateRomfsFileDiagnostics = gameUpdate.getRomfsFilesDiagnostics();
        }
        for (Map.Entry<String, String> entry : updateRomfsFileDiagnostics.entrySet()) {
            baseRomfsFileDiagnostics.remove(entry.getKey());
            fileList.add(entry.getValue());
        }
        for (Map.Entry<String, String> entry : baseRomfsFileDiagnostics.entrySet()) {
            fileList.add(entry.getValue());
        }
        Collections.sort(fileList);
        for (String fileLog : fileList) {
            logStream.println(fileLog);
        }
    }

    public Map<String, String> getRomfsFilesDiagnostics() {
        Map<String, String> fileDiagnostics = new HashMap<>();
        for (Map.Entry<String, RomfsFile> entry : romfsFiles.entrySet()) {
            if (entry.getValue().originalCRC != 0) {
                fileDiagnostics.put(entry.getKey(), entry.getKey() + ": " + String.format("%08X", entry.getValue().originalCRC));
            }
        }
        return fileDiagnostics;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public RandomAccessFile getBaseRom() {
        return baseRom;
    }

    public boolean isWritingEnabled() {
        return writingEnabled;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getTitleId() {
        return titleId;
    }

    public int getVersion() {
        return version;
    }

    public static int alignInt(int num, int alignment) {
        int mask = ~(alignment - 1);
        return (num + (alignment - 1)) & mask;
    }

    public static long alignLong(long num, long alignment) {
        long mask = ~(alignment - 1);
        return (num + (alignment - 1)) & mask;
    }

    private int readVersionFromFile() {
        try {
            // Only CIAs can define a version in their TMD. If this is a different ROM type,
            // just exit out early.
            int magic = FileFunctions.readBigEndianIntFromFile(this.baseRom, ncch_and_ncsd_magic_offset);
            if (magic == ncch_magic || magic == ncsd_magic) {
                return 0;
            }

            // For CIAs, we need to read the title metadata (TMD) in order to retrieve the version.
            // The TMD is after the certificate chain and ticket.
            int certChainSize = FileFunctions.readIntFromFile(this.baseRom, 0x08);
            int ticketSize = FileFunctions.readIntFromFile(this.baseRom, 0x0C);
            long certChainOffset = NCCH.alignLong(cia_header_size, 64);
            long ticketOffset = NCCH.alignLong(certChainOffset + certChainSize, 64);
            long tmdOffset = NCCH.alignLong(ticketOffset + ticketSize, 64);

            // At the start of the TMD is a signature whose length varies based on what type of signature it is.
            int signatureType = FileFunctions.readBigEndianIntFromFile(this.baseRom, tmdOffset);
            int signatureSize, paddingSize;
            switch (signatureType) {
                case 0x010003:
                    signatureSize = 0x200;
                    paddingSize = 0x3C;
                    break;
                case 0x010004:
                    signatureSize = 0x100;
                    paddingSize = 0x3C;
                    break;
                case 0x010005:
                    signatureSize = 0x3C;
                    paddingSize = 0x40;
                    break;
                default:
                    signatureSize = -1;
                    paddingSize = -1;
                    break;
            }
            if (signatureSize == -1) {
                // This shouldn't happen in practice, since all used and valid signature types are represented
                // in the above switch. However, if we can't find the right signature type, then it's probably
                // an invalid CIA anyway, so we're unlikely to get good version information out of it.
                return 0;
            }

            // After the signature is the TMD header, which actually contains the version information.
            long tmdHeaderOffset = tmdOffset + 4 + signatureSize + paddingSize;
            return FileFunctions.read2ByteBigEndianIntFromFile(this.baseRom, tmdHeaderOffset + 0x9C);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    // At the bare minimum, a 3DS game consists of what's known as a CXI file, which
    // is just an NCCH that contains executable code. However, 3DS games are packaged
    // in various containers that can hold other NCCH files like the game manual and
    // firmware updates, among other things. This function's determines the location
    // of the CXI regardless of the container.
    public static long getCXIOffsetInFile(String filename) {
        try {
            RandomAccessFile rom = new RandomAccessFile(filename, "r");
            int ciaHeaderSize = FileFunctions.readIntFromFile(rom, 0x00);
            if (ciaHeaderSize == cia_header_size) {
                // This *might* be a CIA; let's do our best effort to try to get
                // a CXI out of this.
                int certChainSize = FileFunctions.readIntFromFile(rom, 0x08);
                int ticketSize = FileFunctions.readIntFromFile(rom, 0x0C);
                int tmdFileSize = FileFunctions.readIntFromFile(rom, 0x10);

                // If this is *really* a CIA, we'll find our CXI at the beginning of the
                // content section, which is after the certificate chain, ticket, and TMD
                long certChainOffset = NCCH.alignLong(ciaHeaderSize, 64);
                long ticketOffset = NCCH.alignLong(certChainOffset + certChainSize, 64);
                long tmdOffset = NCCH.alignLong(ticketOffset + ticketSize, 64);
                long contentOffset = NCCH.alignLong(tmdOffset + tmdFileSize, 64);
                int magic = FileFunctions.readBigEndianIntFromFile(rom, contentOffset + ncch_and_ncsd_magic_offset);
                if (magic == ncch_magic) {
                    // This CIA's content contains a valid CXI!
                    return contentOffset;
                }
            }

            // We don't put the following code in an else-block because there *might*
            // exist a totally-valid CXI or CCI whose first four bytes just so
            // *happen* to be the same as the first four bytes of a CIA file.
            int magic = FileFunctions.readBigEndianIntFromFile(rom, ncch_and_ncsd_magic_offset);
            rom.close();
            if (magic == ncch_magic) {
                // Magic is NCCH, so this just a straight-up NCCH/CXI; there is no container
                // around the game data. Thus, the CXI offset is the beginning of the file.
                return 0;
            } else if (magic == ncsd_magic) {
                // Magic is NCSD, so this is almost certainly a CCI. The CXI is always
                // a fixed distance away from the start.
                return 0x4000;
            } else {
                // This doesn't seem to be a valid 3DS file.
                return -1;
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private class ExefsFileHeader {
        public String filename;
        public int offset;
        public int size;

        public ExefsFileHeader() { }

        public ExefsFileHeader(byte[] exefsHeaderData, int fileHeaderOffset) {
            byte[] filenameBytes = new byte[0x8];
            System.arraycopy(exefsHeaderData, fileHeaderOffset, filenameBytes, 0, 0x8);
            this.filename = new String(filenameBytes, StandardCharsets.UTF_8).trim();
            this.offset = FileFunctions.readFullInt(exefsHeaderData, fileHeaderOffset + 0x08);
            this.size = FileFunctions.readFullInt(exefsHeaderData, fileHeaderOffset + 0x0C);
        }

        public boolean isValid() {
            return this.filename != "" && this.size != 0;
        }

        public byte[] asBytes() {
            byte[] output = new byte[0x10];
            byte[] filenameBytes = this.filename.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(filenameBytes, 0, output, 0, filenameBytes.length);
            FileFunctions.writeFullInt(output, 0x08, this.offset);
            FileFunctions.writeFullInt(output, 0x0C, this.size);
            return output;
        }
    }

    private class DirectoryMetadata {
        public int parentDirectoryOffset;
        public int siblingDirectoryOffset;
        public int firstChildDirectoryOffset;
        public int firstFileOffset;
        public int nextDirectoryInHashBucketOffset;
        public int nameLength;
        public String name;

        public DirectoryMetadata(byte[] directoryMetadataBlock, int offset) {
            parentDirectoryOffset = FileFunctions.readFullInt(directoryMetadataBlock, offset);
            siblingDirectoryOffset = FileFunctions.readFullInt(directoryMetadataBlock, offset + 0x04);
            firstChildDirectoryOffset = FileFunctions.readFullInt(directoryMetadataBlock, offset + 0x08);
            firstFileOffset = FileFunctions.readFullInt(directoryMetadataBlock, offset + 0x0C);
            nextDirectoryInHashBucketOffset = FileFunctions.readFullInt(directoryMetadataBlock, offset + 0x10);
            nameLength = FileFunctions.readFullInt(directoryMetadataBlock, offset + 0x14);
            name = "";
            if (nameLength != metadata_unused) {
                byte[] nameBytes = new byte[nameLength];
                System.arraycopy(directoryMetadataBlock, offset + 0x18, nameBytes, 0, nameLength);
                name = new String(nameBytes, StandardCharsets.UTF_16LE).trim();
            }
        }
    }

    private class FileMetadata {
        public int offset;
        public int parentDirectoryOffset;
        public int siblingFileOffset;
        public long fileDataOffset;
        public long fileDataLength;
        public int nextFileInHashBucketOffset;
        public int nameLength;
        public String name;
        public RomfsFile file; // used only for rebuilding CXI

        public FileMetadata(byte[] fileMetadataBlock, int offset) {
            this.offset = offset;
            parentDirectoryOffset = FileFunctions.readFullInt(fileMetadataBlock, offset);
            siblingFileOffset = FileFunctions.readFullInt(fileMetadataBlock, offset + 0x04);
            fileDataOffset = FileFunctions.readFullLong(fileMetadataBlock, offset + 0x08);
            fileDataLength = FileFunctions.readFullLong(fileMetadataBlock, offset + 0x10);
            nextFileInHashBucketOffset = FileFunctions.readFullInt(fileMetadataBlock, offset + 0x18);
            nameLength = FileFunctions.readFullInt(fileMetadataBlock, offset + 0x1C);
            name = "";
            if (nameLength != metadata_unused) {
                byte[] nameBytes = new byte[nameLength];
                System.arraycopy(fileMetadataBlock, offset + 0x20, nameBytes, 0, nameLength);
                name = new String(nameBytes, StandardCharsets.UTF_16LE).trim();
            }
        }

        public byte[] asBytes() {
            int metadataLength = 0x20;
            if (nameLength != metadata_unused) {
                metadataLength += alignInt(nameLength, 4);
            }
            byte[] output = new byte[metadataLength];
            FileFunctions.writeFullInt(output, 0x00, this.parentDirectoryOffset);
            FileFunctions.writeFullInt(output, 0x04, this.siblingFileOffset);
            FileFunctions.writeFullLong(output, 0x08, this.fileDataOffset);
            FileFunctions.writeFullLong(output, 0x10, this.fileDataLength);
            FileFunctions.writeFullInt(output, 0x18, this.nextFileInHashBucketOffset);
            FileFunctions.writeFullInt(output, 0x1C, this.nameLength);
            if (!name.equals("")) {
                byte[] nameBytes = name.getBytes(StandardCharsets.UTF_16LE);
                System.arraycopy(nameBytes, 0, output, 0x20, nameBytes.length);
            }
            return output;
        }
    }
}
