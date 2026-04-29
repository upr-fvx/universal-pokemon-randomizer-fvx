package com.uprfvx.romio.ctr;

/*----------------------------------------------------------------------------*/
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

import filefunctions.IOFunctions;

import java.io.IOException;

/**
 * An entry in the romfs filesystem. The contents of a RomfsFile is essentially a byte array,
 * but to avoid unnecessary copying to RAM, getting its contents is done using a {@link RandomAccessWindowInput} -
 * unless the file has been overwritten in which case it is indeed gotten as a byte array.
 */
public class RomfsFile {

    private final NCCH parent;
    private final long offset;
    private int size;
    private final String fullPath;

    private byte[] data;

    private boolean changed;
    private boolean crcCalculated;
    private long originalCRC;

    public RomfsFile(NCCH parent, long offset, int size, String fullPath) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be non-negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size must be non-negative");
        }
        this.parent = parent;
        this.offset = offset;
        this.size = size;
        this.fullPath = fullPath;
    }

    public RomfsFileInput getContents() throws IOException {
        if (changed) {
            System.out.println("getting ByteArrayFileInput");
            return new ByteArrayFileInput(data);
        } else {
            System.out.println("getting RandomAccessWindowInput");
            return new RandomAccessWindowInput(parent.getBaseRom(), offset, size);
        }
    }

    public void writeOverride(byte[] data) {
        if (!crcCalculated) {
            // must be done before overriding the data,
            // so we get the original CRC
            calculateCRC();
        }
        changed = true;
        size = data.length;
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        System.out.println("Overwrote file " + fullPath);
    }

    /**
     * Returns the data as overridden by {@link #writeOverride(byte[])}.
     * @throws IllegalStateException if the file has not been overridden.
     */
    public byte[] getOverrideContents() {
        if (!changed) {
            throw new IllegalStateException("file has not been overridden");
        }
        return data;
    }

    private void calculateCRC() {
        if (crcCalculated) {
            throw new IllegalStateException("CRC has already been calculated");
        }
        try {
            byte[] originalData = getContents().readFully();
            originalCRC = IOFunctions.getCRC32(originalData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate CRC for file " + fullPath, e);
        }
        crcCalculated = true;
    }

    public long getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public String getFullPath() {
        return fullPath;
    }

    public boolean isChanged() {
        return changed;
    }

    public long getOriginalCRC() {
        if (!crcCalculated) {
            calculateCRC();
        }
        return originalCRC;
    }
}
