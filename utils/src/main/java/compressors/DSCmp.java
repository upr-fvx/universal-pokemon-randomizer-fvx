package compressors;

/* 
 * Modified AlmiaE source for randomizer's needs (e.g. assuming little endian).
 * License is found below. 
 */

/*
 * Copyright (C) 2018 Aurum
 *
 * AlmiaE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AlmiaE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.ByteArrayOutputStream;

public final class DSCmp {

    public static final int LZ10 = 0x10;
    public static final int LZ11 = 0x11;
    public static final int HUFF4 = 0x24;
    public static final int HUFF8 = 0x28;
    public static final int RLE = 0x30;

    // https://github.com/pleonex/tinke/blob/master/Plugins/DSDecmp/DSDecmp/Formats/Nitro/LZ10.cs#L173
    public static byte[] compressLZ10(byte[] decompressed) {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();

        compressed.write(LZ10);

        if (decompressed.length > 0xFFFFFF) {
            compressed.write(0);
            compressed.write(0);
            compressed.write(0);
            compressed.write(getBytes(decompressed.length), 0, 4);
        } else
            compressed.write(getBytes24(decompressed.length), 0, 3);

        int curIn = 0;

        byte[] outBuffer = new byte[17];
        outBuffer[0] = 0;
        int lenBuffer = 1;
        int numBlocks = 0;

        while (curIn < decompressed.length) {
            if (numBlocks == 8) {
                compressed.write(outBuffer, 0, lenBuffer);
                outBuffer[0] = 0;
                lenBuffer = 1;
                numBlocks = 0;
            }

            int[] occInfo = getOccurenceInfo(decompressed, curIn, Math.min(decompressed.length - curIn, 0x12),
                    Math.min(curIn, 0x1000));
            int occLength = occInfo[0];
            int occDisp = occInfo[1];

            if (occLength < 3)
                outBuffer[lenBuffer++] = decompressed[curIn++];
            else {
                outBuffer[0] |= (byte) (1 << (7 - numBlocks));

                outBuffer[lenBuffer] = (byte) (((occLength - 3) & 0xF) << 4);
                outBuffer[lenBuffer++] |= (byte) (((occDisp - 1) >> 8) & 0xF);
                outBuffer[lenBuffer++] = (byte) ((occDisp - 1) & 0xFF);
                curIn += occLength;
            }

            numBlocks++;
        }

        if (numBlocks > 0)
            compressed.write(outBuffer, 0, lenBuffer);

        return compressed.toByteArray();
    }

    // https://github.com/pleonex/tinke/blob/master/Plugins/DSDecmp/DSDecmp/Formats/Nitro/LZ11.cs#L238
    public static byte[] compressLZ11(byte[] decompressed) {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();

        compressed.write(LZ11);

        if (decompressed.length > 0xFFFFFF) {
            compressed.write(0);
            compressed.write(0);
            compressed.write(0);
            compressed.write(getBytes(decompressed.length), 0, 4);
        } else
            compressed.write(getBytes24(decompressed.length), 0, 3);

        int curIn = 0;

        byte[] outBuffer = new byte[33];
        outBuffer[0] = 0;
        int lenBuffer = 1;
        int numBlocks = 0;

        while (curIn < decompressed.length) {
            if (numBlocks == 8) {
                compressed.write(outBuffer, 0, lenBuffer);
                outBuffer[0] = 0;
                lenBuffer = 1;
                numBlocks = 0;
            }

            int[] occInfo = getOccurenceInfo(decompressed, curIn, Math.min(decompressed.length - curIn, 0x10110),
                    Math.min(curIn, 0x1000));
            int occLength = occInfo[0];
            int occDisp = occInfo[1];

            if (occLength < 3)
                outBuffer[lenBuffer++] = decompressed[curIn++];
            else {
                outBuffer[0] |= (byte) (1 << (7 - numBlocks));

                if (occLength > 0x110) {
                    outBuffer[lenBuffer] = 0x10;
                    outBuffer[lenBuffer++] |= (byte) (((occLength - 0x111) >> 12) & 0xF);
                    outBuffer[lenBuffer++] = (byte) (((occLength - 0x111) >> 4) & 0xFF);
                    outBuffer[lenBuffer] = (byte) (((occLength - 0x111) & 0xF) << 4);
                } else if (occLength > 0x10) {
                    outBuffer[lenBuffer] = 0x00;
                    outBuffer[lenBuffer++] |= (byte) (((occLength - 0x111) >> 4) & 0xF);
                    outBuffer[lenBuffer] = (byte) (((occLength - 0x111) & 0xF) << 4);
                } else
                    outBuffer[lenBuffer] = (byte) (((occLength - 1) & 0xF) << 4);

                outBuffer[lenBuffer++] |= (byte) (((occDisp - 1) >> 8) & 0xF);
                outBuffer[lenBuffer++] = (byte) ((occDisp - 1) & 0xFF);
                curIn += occLength;
            }

            numBlocks++;
        }

        if (numBlocks > 0)
            compressed.write(outBuffer, 0, lenBuffer);

        return compressed.toByteArray();
    }

    // https://github.com/pleonex/tinke/blob/master/Plugins/DSDecmp/DSDecmp/Utils/LZUtil.cs#L19
    private static int[] getOccurenceInfo(byte[] decompressed, int current, int pending, int finished) {
        if (pending == 0)
            return new int[] { 0, 0 };

        int disp = 0;
        int length = 0;

        for (int i = 0; i < finished - 1; i++) {
            int pendingOffset = current - finished + i;
            int pendingLength = 0;

            for (int j = 0; j < pending; j++) {
                if (decompressed[pendingOffset + j] != decompressed[current + j])
                    break;
                pendingLength++;
            }

            if (pendingLength > length) {
                length = pendingLength;
                disp = finished - i;

                if (length == pending)
                    break;
            }
        }

        return new int[] { length, disp };
    }

    private static byte[] getBytes(int val) {
        byte[] bs = new byte[Integer.BYTES];

        bs[3] = (byte) ((val >> 24) & 0xFF);
        bs[2] = (byte) ((val >> 16) & 0xFF);
        bs[1] = (byte) ((val >> 8) & 0xFF);
        bs[0] = (byte) (val & 0xFF);

        return bs;
    }

    private static byte[] getBytes24(int val) {
        byte[] bs = new byte[3];

        bs[2] = (byte) ((val >> 16) & 0xFF);
        bs[1] = (byte) ((val >> 8) & 0xFF);
        bs[0] = (byte) (val & 0xFF);

        return bs;
    }
}
