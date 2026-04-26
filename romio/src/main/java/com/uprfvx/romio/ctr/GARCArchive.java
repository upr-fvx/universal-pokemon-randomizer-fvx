package com.uprfvx.romio.ctr;

/*----------------------------------------------------------------------------*/
/*--  GARCArchive.java - class for packing/unpacking GARC archives          --*/
/*--                                                                        --*/
/*--  Code based on "pk3DS", copyright (C) Kaphotics                        --*/
/*--                                                                        --*/
/*--  Ported to Java by UPR-ZX Team under the terms of the GPL:             --*/
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

import cuecompressors.BLZCoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GARCArchive {

    private static final int FRAME_COUNT = 4;
    private static final int VER_4 = 0x0400;
    private static final int VER_6 = 0x0600;
    private static final int GARC_HEADER_SIZE_4 = 0x1C;
    private static final int GARC_HEADER_SIZE_6 = 0x24;
    private static final String GARC_MAGIC = "GARC";
    private static final String FATO_MAGIC = "FATO";
    private static final String FATB_MAGIC = "FATB";
    private static final String FIMB_MAGIC = "FIMB";

    private int version;
    private boolean skipDecompression = true;

    public List<Map<Integer, byte[]>> files;

    private final Map<Integer, Boolean> isCompressed = new TreeMap<>();
    private List<Boolean> compressThese = null;

    private GARCFrame garc;
    private FATOFrame fato;
    private FATBFrame fatb;
    private FIMBFrame fimb;

    public GARCArchive(RandomAccessFileWindow fileWindow, boolean skipDecompression) throws IOException {
        this.skipDecompression = skipDecompression;
        readFrames(fileWindow);
        files = fimb.files;
    }

    public GARCArchive(RandomAccessFileWindow fileWindow, List<Boolean> compressedThese) throws IOException {
        this.compressThese = compressedThese;
        readFrames(fileWindow);
        files = fimb.files;
    }

    private void readFrames(RandomAccessFileWindow fw) throws IOException {
        if (fw.size() <= 0) {
            throw new IOException("Invalid GARC file: Empty");
        }

        // GARC
        String magic = fw.readMagic();
        if (!magic.equals(GARC_MAGIC)) {
            throw new IOException("Invalid GARC file: incorrect GARC magic. Expected " + GARC_MAGIC + ", got " + magic);
        }
        garc = new GARCFrame();
        garc.headerSize = fw.readInt();
        garc.endianness = fw.readShort();
        garc.version = fw.readShort();
        int frameCount = fw.readInt();
        if (frameCount != FRAME_COUNT) {
            throw new IOException("Invalid GARC file: invalid frameCount. Expected " + FRAME_COUNT + ", got " + frameCount);
        }
        garc.dataOffset = fw.readInt();
        garc.fileSize = fw.readInt();
        if (garc.version == VER_4) {
            garc.contentLargestUnpadded = fw.readInt();
            garc.contentPadToNearest = 4;
            version = 4;
        } else if (garc.version == VER_6) {
            garc.contentLargestPadded = fw.readInt();
            garc.contentLargestUnpadded = fw.readInt();
            garc.contentPadToNearest = fw.readInt();
            version = 6;
        } else {
            throw new IOException("Invalid GARC file: invalid version. Expected " + VER_4 + " or " + VER_6 + ", got " + garc.version);
        }

        // FATO
        fato = new FATOFrame();
        magic = fw.readMagic();
        if (!magic.equals(FATO_MAGIC)) {
            throw new IOException("Invalid GARC file: incorrect FATO magic. Expected " + FATO_MAGIC + ", got " + magic);
        }
        fato.headerSize = fw.readInt();
        fato.entryCount = fw.readShort();
        fato.padding = fw.readShort();
        fato.entries = new int[fato.entryCount];
        for (int i = 0; i < fato.entryCount; i++) {
            fato.entries[i] = fw.readInt();
        }

        // FATB
        fatb = new FATBFrame();
        magic = fw.readMagic();
        if (!magic.equals(FATB_MAGIC)) {
            throw new IOException("Invalid GARC file: incorrect FATB magic. Expected " + FATB_MAGIC + ", got " + magic);
        }
        fatb.headerSize = fw.readInt();
        fatb.fileCount = fw.readInt();
        fatb.entries = new FATBEntry[fatb.fileCount];
        for (int i = 0; i < fatb.fileCount; i++) {
            fatb.entries[i] = new FATBEntry();
            fatb.entries[i].vector = fw.readInt();
            fatb.entries[i].subEntries = new TreeMap<>();
            int bitVector = fatb.entries[i].vector;
            int counter = 0;
            for (int b = 0; b < 32; b++) {
                boolean exists = (bitVector & 1) == 1;
                bitVector >>>= 1;
                if (!exists) continue;
                FATBSubEntry subEntry = new FATBSubEntry();
                subEntry.start = fw.readInt();
                subEntry.end = fw.readInt();
                subEntry.length = fw.readInt();
                fatb.entries[i].subEntries.put(b,subEntry);
                counter++;
            }
            fatb.entries[i].isFolder = counter > 1;
        }

        // FIMB
        fimb = new FIMBFrame();
        magic = fw.readMagic();
        if (!magic.equals(FIMB_MAGIC)) {
            throw new IOException("Invalid GARC file: incorrect FIMB magic. Expected " + FIMB_MAGIC + ", got " + magic);
        }
        fimb.headerSize = fw.readInt();
        fimb.dataSize = fw.readInt();
        fimb.files = new ArrayList<>();
        for (int i = 0; i < fatb.fileCount; i++) {
            FATBEntry entry = fatb.entries[i];
            Map<Integer,byte[]> files = new TreeMap<>();
            for (int k: entry.subEntries.keySet()) {
                FATBSubEntry subEntry = entry.subEntries.get(k);
                fw.seek(garc.dataOffset + subEntry.start);
                byte compressByte = fw.readByteInPlace();

                // And this is another problem. We don't want to copy all files in the GARC,
                // when we don't know that all of them will ever be read
                // (before writing, everything needs to be read before we write).
                // In general, reading late is always good, because then test cases can
                // skip unnecessary reading.
                byte[] file = new byte[subEntry.length];

                boolean compressed = compressThese == null ?
                        compressByte == 0x11 && !skipDecompression :
                        compressByte == 0x11 && compressThese.get(i);
                fw.read(file);
                if (compressed) {
                    try {
                        files.put(k,new BLZCoder(null).BLZ_DecodePub(file,"GARC"));
                        isCompressed.put(i,true);
                    } catch (Exception e) {
                        throw new IOException("Invalid GARC file.", e);
                    }
                } else {
                    files.put(k,file);
                    isCompressed.put(i,false);
                }
            }
            fimb.files.add(files);
        }
    }

    public void updateFiles(List<Map<Integer,byte[]>> files) {
        fimb.files = files;
    }

    public byte[] getBytes() throws IOException {
        int garcHeaderSize = garc.version == VER_4 ? GARC_HEADER_SIZE_4 : GARC_HEADER_SIZE_6;
        ByteBuffer garcBuf = ByteBuffer.allocate(garcHeaderSize);
        garcBuf.order(ByteOrder.LITTLE_ENDIAN);
        putMagic(garcBuf, GARC_MAGIC);
        garcBuf.putInt(garcHeaderSize);
        garcBuf.putShort((short)0xFEFF);
        garcBuf.putShort(version == 4 ? (short)VER_4 : (short)VER_6);
        garcBuf.putInt(4);

        ByteBuffer fatoBuf = ByteBuffer.allocate(fato.headerSize);
        fatoBuf.order(ByteOrder.LITTLE_ENDIAN);
        putMagic(fatoBuf, FATO_MAGIC);
        fatoBuf.putInt(fato.headerSize);
        fatoBuf.putShort((short)fato.entryCount);
        fatoBuf.putShort((short)fato.padding);

        ByteBuffer fatbBuf = ByteBuffer.allocate(fatb.headerSize);
        fatbBuf.order(ByteOrder.LITTLE_ENDIAN);
        putMagic(fatbBuf, FATB_MAGIC);
        fatbBuf.putInt(fatb.headerSize);
        fatbBuf.putInt(fatb.fileCount);

        ByteBuffer fimbHeaderBuf = ByteBuffer.allocate(fimb.headerSize);
        fimbHeaderBuf.order(ByteOrder.LITTLE_ENDIAN);
        putMagic(fimbHeaderBuf, FIMB_MAGIC);
        fimbHeaderBuf.putInt(fimb.headerSize);

        ByteArrayOutputStream fimbPayloadStream = new ByteArrayOutputStream(); // Unknown size, can't use ByteBuffer

        int fimbOffset = 0;
        int largestSize = 0;
        int largestPadded = 0;
        for (int i = 0; i < fimb.files.size(); i++) {
            Map<Integer,byte[]> directory = fimb.files.get(i);
            int bitVector = 0;
            int totalLength = 0;
            for (int k: directory.keySet()) {
                bitVector |= (1 << k);
                byte[] file = directory.get(k);
                if (isCompressed.get(i)) {
                    file = new BLZCoder(null).BLZ_EncodePub(file,false,false,"GARC");
                }
                fimbPayloadStream.write(file);
                totalLength += file.length;
            }

            int paddingRequired = totalLength % garc.contentPadToNearest;
            if (paddingRequired != 0) {
                paddingRequired = garc.contentPadToNearest - paddingRequired;
            }

            if (totalLength > largestSize) {
                largestSize = totalLength;
            }
            if (totalLength + paddingRequired > largestPadded) {
                largestPadded = totalLength + paddingRequired;
            }

            for (int j = 0; j < paddingRequired; j++) {
                fimbPayloadStream.write(fato.padding & 0xFF);
            }

            fatoBuf.putInt(fatbBuf.position() - 12);

            fatbBuf.putInt(bitVector);
            fatbBuf.putInt(fimbOffset);
            fimbOffset = fimbPayloadStream.size();
            fatbBuf.putInt(fimbOffset);
            fatbBuf.putInt(totalLength);
        }

        int dataOffset = garcHeaderSize + fatoBuf.position() + fatbBuf.position() + fimb.headerSize;
        garcBuf.putInt(dataOffset);
        garcBuf.putInt(dataOffset + fimbOffset);
        if (garc.version == VER_4) {
            garcBuf.putInt(largestSize);
        } else if (garc.version == VER_6) {
            garcBuf.putInt(largestPadded);
            garcBuf.putInt(largestSize);
            garcBuf.putInt(garc.contentPadToNearest);
        }
        fimbHeaderBuf.putInt(fimbPayloadStream.size());

        garcBuf.flip();
        fatoBuf.flip();
        fatbBuf.flip();
        fimbHeaderBuf.flip();

        byte[] fullArray = new byte[garcBuf.limit() + fatoBuf.limit() + fatbBuf.limit() + fimbHeaderBuf.limit() + fimbPayloadStream.size()];
        System.arraycopy(garcBuf.array(),
                0,
                fullArray,
                0,
                garcBuf.limit());
        System.arraycopy(fatoBuf.array(),
                0,
                fullArray,
                garcBuf.limit(),
                fatoBuf.limit());
        System.arraycopy(fatbBuf.array(),
                0,
                fullArray,
                garcBuf.limit()+fatoBuf.limit(),
                fatbBuf.limit());
        System.arraycopy(fimbHeaderBuf.array(),
                0,
                fullArray,
                garcBuf.limit()+fatoBuf.limit()+fatbBuf.limit(),
                fimbHeaderBuf.limit());
//        garcBuf.get(fullArray);
//        fatoBuf.get(fullArray,garcBuf.limit(),fatoBuf.limit());
//        fatbBuf.get(fullArray,garcBuf.limit()+fatoBuf.limit(),fatbBuf.limit());
//        fimbHeaderBuf.get(fullArray,garcBuf.limit()+fatoBuf.limit()+fatbBuf.limit(),fimbHeaderBuf.limit());
        System.arraycopy(fimbPayloadStream.toByteArray(),
                0,
                fullArray,
                garcBuf.limit()+fatoBuf.limit()+fatbBuf.limit()+fimbHeaderBuf.limit(),
                fimbPayloadStream.size());
        return fullArray;
    }

    private void putMagic(ByteBuffer buf, String magic) {
        buf.put(new StringBuffer(magic).reverse().toString().getBytes());
    }

    public byte[] getFile(int index) {
        return fimb.files.get(index).get(0);
    }

    public byte[] getFile(int index, int subIndex) {
        return fimb.files.get(index).get(subIndex);
    }

    public void setFile(int index, byte[] data) {
        fimb.files.get(index).put(0,data);
    }

    public Map<Integer,byte[]> getDirectory(int index) {
        return fimb.files.get(index);
    }

    private static class GARCFrame {
        int headerSize;
        int endianness;
        int version;
        int dataOffset;
        int fileSize;

        int contentLargestPadded;
        int contentLargestUnpadded;
        int contentPadToNearest;
    }

    private static class FATOFrame {
        int headerSize;
        int entryCount;
        int padding;

        int[] entries;
    }

    private static class FATBFrame {
        int headerSize;
        int fileCount;
        FATBEntry[] entries;
    }

    private static class FATBEntry {
        int vector;
        boolean isFolder;
        Map<Integer,FATBSubEntry> subEntries;
    }

    private static class FATBSubEntry {
        boolean exists;
        int start;
        int end;
        int length;
        int padding;
    }

    private static class FIMBFrame {
        int headerSize;
        int dataSize;
        List<Map<Integer,byte[]>> files;
    }
}
