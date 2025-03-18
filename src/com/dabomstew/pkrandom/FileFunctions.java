package com.dabomstew.pkrandom;

/*----------------------------------------------------------------------------*/
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

import com.dabomstew.pkrandom.exceptions.InvalidROMException;
import com.dabomstew.pkrandom.exceptions.InvalidSupplementFilesException;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

/**
 * Functions relating to file I/O.
 */
public class FileFunctions {

    public static void validateRomFile(File fh) throws InvalidROMException {
        // first, check for common filetypes that aren't ROMs
        // read first 10 bytes of the file to do this
        try {
            FileInputStream fis = new FileInputStream(fh);
            byte[] sig = new byte[10];
            int sigLength = fis.read(sig);
            fis.close();
            if (sigLength < 10) {
                throw new InvalidROMException(InvalidROMException.Type.LENGTH, String.format(
                        "%s appears to be a blank or nearly blank file.", fh.getName()));
            }
            if (sig[0] == 0x50 && sig[1] == 0x4b && sig[2] == 0x03 && sig[3] == 0x04) {
                throw new InvalidROMException(InvalidROMException.Type.ZIP_FILE, String.format(
                        "%s is a ZIP archive, not a ROM.", fh.getName()));
            }
            if (sig[0] == 0x52 && sig[1] == 0x61 && sig[2] == 0x72 && sig[3] == 0x21 && sig[4] == 0x1A
                    && sig[5] == 0x07) {
                throw new InvalidROMException(InvalidROMException.Type.RAR_FILE, String.format(
                        "%s is a RAR archive, not a ROM.", fh.getName()));
            }
            if (sig[0] == 'P' && sig[1] == 'A' && sig[2] == 'T' && sig[3] == 'C' && sig[4] == 'H') {
                throw new InvalidROMException(InvalidROMException.Type.IPS_FILE, String.format(
                        "%s is a IPS patch, not a ROM.", fh.getName()));
            }
        } catch (IOException ex) {
            throw new InvalidROMException(InvalidROMException.Type.UNREADABLE, String.format(
                    "Could not read %s from disk.", fh.getName()));
        }
    }

    public static File fixFilename(File original, String defaultExtension) {
        return fixFilename(original, defaultExtension, new ArrayList<>());
    }

    // Behavior:
    // if file has no extension, add defaultExtension
    // if there are banned extensions & file has a banned extension, replace
    // with defaultExtension
    // else, leave as is
    public static File fixFilename(File original, String defaultExtension, List<String> bannedExtensions) {
        String absolutePath = original.getAbsolutePath();
        for (String bannedExtension: bannedExtensions) {
            if (absolutePath.endsWith("." + bannedExtension)) {
                absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf('.') + 1) + defaultExtension;
                break;
            }
        }
        if (!absolutePath.endsWith("." + defaultExtension)) {
            absolutePath += "." + defaultExtension;
        }
        return new File(absolutePath);
    }

    // RomHandlers implicitly rely on these - call this before creating settings
    // etc.
    public static void testForRequiredConfigs() throws FileNotFoundException {
        String[] required = new String[] { "gameboy_jpn.tbl", "rby_english.tbl", "rby_freger.tbl", "rby_espita.tbl",
                "green_translation.tbl", "gsc_english.tbl", "gsc_freger.tbl", "gsc_espita.tbl", "gba_english.tbl",
                "gba_jpn.tbl", "Generation4.tbl", "Generation5.tbl", "gen1_offsets.ini", "gen2_offsets.ini",
                "gen3_offsets.ini", "gen4_offsets.ini", "gen5_offsets.ini", "gen6_offsets.ini", "gen7_offsets.ini",
                SysConstants.customNamesFile };
        for (String filename : required) {
            if (!configExists(filename)) {
                throw new FileNotFoundException(filename);
            }
        }
    }

    private static List<String> overrideFiles = Arrays.asList(SysConstants.customNamesFile,
            SysConstants.tclassesFile, SysConstants.tnamesFile, SysConstants.nnamesFile);

    public static boolean configExists(String filename) {
        if (overrideFiles.contains(filename)) {
            File fh = new File(SysConstants.ROOT_PATH + filename);
            if (fh.exists() && fh.canRead()) {
                return true;
            }
            fh = new File("./" + filename);
            if (fh.exists() && fh.canRead()) {
                return true;
            }
        }
        return FileFunctions.class.getResource("/com/dabomstew/pkrandom/config/" + filename) != null;
    }

    public static InputStream openConfig(String filename) throws FileNotFoundException {
        if (overrideFiles.contains(filename)) {
            File fh = new File(SysConstants.ROOT_PATH + filename);
            if (fh.exists() && fh.canRead()) {
                return new FileInputStream(fh);
            }
            fh = new File("./" + filename);
            if (fh.exists() && fh.canRead()) {
                return new FileInputStream(fh);
            }
        }

        String resourcePath = "/com/dabomstew/pkrandom/config/" + filename;
        InputStream is = FileFunctions.class.getResourceAsStream(resourcePath);
        if (is == null) {
            // FileNotFoundException is not strictly correct, I think? I believe IOException might be what should
            // really be used, but this should do as a quickfix.
            throw new FileNotFoundException("Could not find resource " + resourcePath);
        }
        return is;

    }

    public static CustomNamesSet getCustomNames() throws IOException {
        InputStream is = openConfig(SysConstants.customNamesFile);
        CustomNamesSet cns = new CustomNamesSet(is);
        is.close();
        return cns;
    }

    public static long readFullLong(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(data, offset, 8);
        buf.rewind();
        return buf.getLong();
    }

    public static int readFullInt(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(data, offset, 4);
        buf.rewind();
        return buf.getInt();
    }

    public static int readFullIntBigEndian(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.allocate(4).put(data, offset, 4);
        buf.rewind();
        return buf.getInt();
    }

    public static int read2ByteIntBigEndian(byte[] data, int index) {
        return (data[index + 1] & 0xFF) | ((data[index] & 0xFF) << 8);
    }

    public static int read2ByteInt(byte[] data, int index) {
        return (data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8);
    }

    public static void write2ByteInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    public static void writeFullInt(byte[] data, int offset, int value) {
        byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
        System.arraycopy(valueBytes, 0, data, offset, 4);
    }

    public static void writeFullIntBigEndian(byte[] data, int offset, int value) {
        byte[] valueBytes = ByteBuffer.allocate(4).putInt(value).array();
        System.arraycopy(valueBytes, 0, data, offset, 4);
    }

    public static void writeFullLong(byte[] data, int offset, long value) {
        byte[] valueBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
        System.arraycopy(valueBytes, 0, data, offset, 8);
    }

    public static byte[] readFileFullyIntoBuffer(String filename) throws IOException {
        File fh = new File(filename);
        if (!fh.exists() || !fh.isFile() || !fh.canRead()) {
            throw new FileNotFoundException(filename);
        }
        long fileSize = fh.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException(filename + " is too long to read in as a byte-array.");
        }
        FileInputStream fis = new FileInputStream(filename);
        byte[] buf = readFullyIntoBuffer(fis, (int) fileSize);
        fis.close();
        return buf;
    }

    public static byte[] readFullyIntoBuffer(InputStream in, int bytes) throws IOException {
        byte[] buf = new byte[bytes];
        readFully(in, buf, 0, bytes);
        return buf;
    }

    private static void readFully(InputStream in, byte[] buf, int offset, int length) throws IOException {
        int offs = 0, read;
        while (offs < length && (read = in.read(buf, offs + offset, length - offs)) != -1) {
            offs += read;
        }
    }

    public static int read2ByteBigEndianIntFromFile(RandomAccessFile file, long offset) throws IOException {
        byte[] buf = new byte[2];
        file.seek(offset);
        file.readFully(buf);
        return read2ByteIntBigEndian(buf, 0);
    }

    public static int readBigEndianIntFromFile(RandomAccessFile file, long offset) throws IOException {
        byte[] buf = new byte[4];
        file.seek(offset);
        file.readFully(buf);
        return readFullIntBigEndian(buf, 0);
    }

    public static int readIntFromFile(RandomAccessFile file, long offset) throws IOException {
        byte[] buf = new byte[4];
        file.seek(offset);
        file.readFully(buf);
        return readFullInt(buf, 0);
    }

    public static void writeBytesToFile(String filename, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(data);
        fos.close();
    }

    public static byte[] getConfigAsBytes(String filename) throws IOException {
        InputStream in = openConfig(filename);
        byte[] buf = readFullyIntoBuffer(in, in.available());
        in.close();
        return buf;
    }

    public static int getFileChecksum(String filename) {
        try {
            return getFileChecksum(openConfig(filename));
        } catch (IOException e) {
            return 0;
        }
    }

    private static int getFileChecksum(InputStream stream) {
        Scanner sc = new Scanner(stream, "UTF-8");
        CRC32 checksum = new CRC32();
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                checksum.update(line.getBytes(StandardCharsets.UTF_8));
            }
        }
        sc.close();
        return (int) checksum.getValue();
    }

    public static void validatePresetSupplementFiles(String config, CustomNamesSet customNames)
            throws InvalidSupplementFilesException {
        byte[] data = Base64.getDecoder().decode(config);

        if (data.length < Settings.LENGTH_OF_SETTINGS_DATA + 9) {
            throw new InvalidSupplementFilesException(InvalidSupplementFilesException.Type.UNKNOWN,
                    "The preset config is too short to be valid");
        }

        // Check the checksum
        ByteBuffer buf = ByteBuffer.allocate(4).put(data, data.length - 8, 4);
        buf.rewind();
        int crc = buf.getInt();

        CRC32 checksum = new CRC32();
        checksum.update(data, 0, data.length - 8);
        if ((int) checksum.getValue() != crc) {
            throw new IllegalArgumentException("Checksum failure.");
        }

        // Check the trainerclass & trainernames & nicknames crc
        if (customNames == null && !FileFunctions.checkOtherCRC(data, 16, 4, SysConstants.customNamesFile, data.length - 4)) {
            throw new InvalidSupplementFilesException(InvalidSupplementFilesException.Type.CUSTOM_NAMES,
                    "Can't use this preset because you have a different set " + "of custom names to the creator.");
        }
    }

    public static boolean checkOtherCRC(byte[] data, int byteIndex, int switchIndex, String filename, int offsetInData) {
        // If the switch at data[byteIndex].switchIndex is on, then check that
        // the CRC at data[offsetInData] ... data[offsetInData+3] matches the
        // CRC of filename.
        // If not, return false.
        // If any other case, return true.
        int switches = data[byteIndex] & 0xFF;
        if (((switches >> switchIndex) & 0x01) == 0x01) {
            // have to check the CRC
            int crc = readFullIntBigEndian(data, offsetInData);

            return getFileChecksum(filename) == crc;
        }
        return true;
    }

    public static long getCRC32(byte[] data) {
        CRC32 checksum = new CRC32();
        checksum.update(data);
        return checksum.getValue();
    }

    private static byte[] getCodeTweakFile(String filename) throws IOException {
        System.out.println(filename);
        InputStream is = FileFunctions.class.getResourceAsStream("/com/dabomstew/pkrandom/patches/" + filename);
        byte[] buf = readFullyIntoBuffer(is, is.available());
        is.close();
        return buf;
    }

    public static byte[] downloadFile(String url) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int count;
        while ((count = in.read(buf, 0, 1024)) != -1) {
            out.write(buf, 0, count);
        }
        in.close();
        return out.toByteArray();
    }

    public static void applyPatch(byte[] rom, String patchName) throws IOException {
        byte[] patch = getCodeTweakFile(patchName + ".ips");

        // check sig
        int patchlen = patch.length;
        if (patchlen < 8 || patch[0] != 'P' || patch[1] != 'A' || patch[2] != 'T' || patch[3] != 'C' || patch[4] != 'H') {
            throw new IOException("not a valid IPS file");
        }

        // records
        int offset = 5;
        while (offset + 2 < patchlen) {
            int writeOffset = readIPSOffset(patch, offset);
            if (writeOffset == 0x454f46) {
                // eof, done
                return;
            }
            offset += 3;
            if (offset + 1 >= patchlen) {
                // error
                throw new IOException("abrupt ending to IPS file, entry cut off before size");
            }
            int size = readIPSSize(patch, offset);
            offset += 2;
            if (size == 0) {
                // RLE
                if (offset + 1 >= patchlen) {
                    // error
                    throw new IOException("abrupt ending to IPS file, entry cut off before RLE size");
                }
                int rleSize = readIPSSize(patch, offset);
                if (writeOffset + rleSize > rom.length) {
                    // error
                    throw new IOException("trying to patch data past the end of the ROM file");
                }
                offset += 2;
                if (offset >= patchlen) {
                    // error
                    throw new IOException("abrupt ending to IPS file, entry cut off before RLE byte");
                }
                byte rleByte = patch[offset++];
                for (int i = writeOffset; i < writeOffset + rleSize; i++) {
                    rom[i] = rleByte;
                }
            } else {
                if (offset + size > patchlen) {
                    // error
                    throw new IOException("abrupt ending to IPS file, entry cut off before end of data block");
                }
                if (writeOffset + size > rom.length) {
                    // error
                    throw new IOException("trying to patch data past the end of the ROM file");
                }
                System.arraycopy(patch, offset, rom, writeOffset, size);
                offset += size;
            }
        }
        throw new IOException("improperly terminated IPS file");
    }

    private static int readIPSOffset(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | (data[offset + 2] & 0xFF);
    }

    private static int readIPSSize(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    public static byte[] convIntArrToByteArr(int[] arg) {
        byte[] out = new byte[arg.length];
        for (int i = 0; i < arg.length; i++) {
            out[i] = (byte) arg[i];
        }
        return out;
    }
}