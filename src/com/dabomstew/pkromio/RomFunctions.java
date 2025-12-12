package com.dabomstew.pkromio;

/*----------------------------------------------------------------------------*/
/*--  RomFunctions.java - contains functions useful throughout the program. --*/
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

import com.dabomstew.pkromio.gamedata.MoveLearnt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RomFunctions {

    /**
     * Get the 4 moves known by a Species at a particular level.
     * 
     * @param pkmn Species index to get moves for.
     * @param movesets Map of Species indices mapped to movesets.
     * @param level Level to get at.
     * @return Array with move indices.
     */
    public static int[] getMovesAtLevel(int pkmn, Map<Integer, List<MoveLearnt>> movesets, int level) {
        return getMovesAtLevel(pkmn, movesets, level, 0);
    }

    public static int[] getMovesAtLevel(int pkmn, Map<Integer, List<MoveLearnt>> movesets, int level, int emptyValue) {
        int[] curMoves = new int[4];

        if (emptyValue != 0) {
            Arrays.fill(curMoves, emptyValue);
        }

        int moveCount = 0;
        List<MoveLearnt> movepool = movesets.get(pkmn);
        for (MoveLearnt ml : movepool) {
            if (ml.level > level) {
                // we're done
                break;
            }

            boolean alreadyKnownMove = false;
            for (int i = 0; i < moveCount; i++) {
                if (curMoves[i] == ml.move) {
                    alreadyKnownMove = true;
                    break;
                }
            }

            if (!alreadyKnownMove) {
                // add this move to the moveset
                if (moveCount == 4) {
                    // shift moves up and add to last slot
                    System.arraycopy(curMoves, 1, curMoves, 0, 3);
                    curMoves[3] = ml.move;
                } else {
                    // add to next available slot
                    curMoves[moveCount++] = ml.move;
                }
            }
        }

        return curMoves;
    }

    public static String camelCase(String original) {
        char[] string = original.toLowerCase().toCharArray();
        boolean docap = true;
        for (int j = 0; j < string.length; j++) {
            char current = string[j];
            if (docap && Character.isLetter(current)) {
                string[j] = Character.toUpperCase(current);
                docap = false;
            } else {
                if (!docap && !Character.isLetter(current) && current != '\'' && current != '’') {
                    docap = true;
                }
            }
        }
        return new String(string);
    }

    @Deprecated
    public static int freeSpaceFinder(byte[] rom, byte freeSpace, int amount, int offset) {
        System.out.print("RomFunctions.freeSpaceFinder() is deprecated, " +
                "use AbstractGBRomHandler.findAndUnfreeSpace() instead.");
        // by default align to 4 bytes to make sure things don't break
        return freeSpaceFinder(rom, freeSpace, amount, offset, true);
    }

    @Deprecated
    public static int freeSpaceFinder(byte[] rom, byte freeSpace, int amount, int offset, boolean longAligned) {
        System.out.print("RomFunctions.freeSpaceFinder() is deprecated, " +
                "use AbstractGBRomHandler.findAndUnfreeSpace() instead.");
        if (!longAligned) {
            // Find 2 more than necessary and return 2 into it,
            // to preserve stuff like FF terminators for strings
            // 161: and FFFF terminators for movesets
            byte[] searchNeedle = new byte[amount + 2];
            for (int i = 0; i < amount + 2; i++) {
                searchNeedle[i] = freeSpace;
            }
            return searchForFirst(rom, offset, searchNeedle) + 2;
        } else {
            // Find 5 more than necessary and return into it as necessary for
            // 4-alignment,
            // to preserve stuff like FF terminators for strings
            // 161: and FFFF terminators for movesets
            byte[] searchNeedle = new byte[amount + 5];
            for (int i = 0; i < amount + 5; i++) {
                searchNeedle[i] = freeSpace;
            }
            return (searchForFirst(rom, offset, searchNeedle) + 5) & ~3;
        }
    }

    public static List<Integer> search(byte[] haystack, byte[] needle) {
        return search(haystack, 0, haystack.length, needle);
    }

    public static List<Integer> search(byte[] haystack, int beginOffset, byte[] needle) {
        return search(haystack, beginOffset, haystack.length, needle);
    }

    public static List<Integer> search(byte[] haystack, int beginOffset, int endOffset, byte[] needle) {
        int currentMatchStart = beginOffset;
        int currentCharacterPosition = 0;

        int needleSize = needle.length;

        int[] toFillTable = buildKMPSearchTable(needle);
        List<Integer> results = new ArrayList<>();

        while ((currentMatchStart + currentCharacterPosition) < endOffset) {

            if (needle[currentCharacterPosition] == (haystack[currentCharacterPosition + currentMatchStart])) {
                currentCharacterPosition = currentCharacterPosition + 1;

                if (currentCharacterPosition == (needleSize)) {
                    results.add(currentMatchStart);
                    currentCharacterPosition = 0;
                    currentMatchStart = currentMatchStart + needleSize;

                }

            } else {
                currentMatchStart = currentMatchStart + currentCharacterPosition
                        - toFillTable[currentCharacterPosition];

                if (toFillTable[currentCharacterPosition] > -1) {
                    currentCharacterPosition = toFillTable[currentCharacterPosition];
                }

                else {
                    currentCharacterPosition = 0;

                }

            }
        }
        return results;
    }

    public static int searchForFirst(byte[] haystack, int beginOffset, byte[] needle) {
        int currentMatchStart = beginOffset;
        int currentCharacterPosition = 0;

        int docSize = haystack.length;
        int needleSize = needle.length;

        int[] toFillTable = buildKMPSearchTable(needle);

        while ((currentMatchStart + currentCharacterPosition) < docSize) {

            if (needle[currentCharacterPosition] == (haystack[currentCharacterPosition + currentMatchStart])) {
                currentCharacterPosition = currentCharacterPosition + 1;

                if (currentCharacterPosition == (needleSize)) {
                    return currentMatchStart;
                }

            } else {
                currentMatchStart = currentMatchStart + currentCharacterPosition
                        - toFillTable[currentCharacterPosition];

                if (toFillTable[currentCharacterPosition] > -1) {
                    currentCharacterPosition = toFillTable[currentCharacterPosition];
                }

                else {
                    currentCharacterPosition = 0;

                }

            }
        }
        return -1;
    }

    private static int[] buildKMPSearchTable(byte[] needle) {
        int[] stable = new int[needle.length];
        int pos = 2;
        int j = 0;
        stable[0] = -1;
        stable[1] = 0;
        while (pos < needle.length) {
            if (needle[pos - 1] == needle[j]) {
                stable[pos] = j + 1;
                pos++;
                j++;
            } else if (j > 0) {
                j = stable[j];
            } else {
                stable[pos] = 0;
                pos++;
            }
        }
        return stable;
    }

    public static String rewriteDescriptionForNewLineSize(String moveDesc, String newline, int lineSize,
            StringSizeDeterminer ssd) {
        // We rewrite the description we're given based on some new chars per
        // line.
        moveDesc = moveDesc.replace("-" + newline, "").replace(newline, " ");
        // Keep spatk/spdef as one word on one line
        moveDesc = moveDesc.replace("Sp. Atk", "Sp__Atk");
        moveDesc = moveDesc.replace("Sp. Def", "Sp__Def");
        moveDesc = moveDesc.replace("SP. ATK", "SP__ATK");
        moveDesc = moveDesc.replace("SP. DEF", "SP__DEF");
        String[] words = moveDesc.split(" ");
        StringBuilder fullDesc = new StringBuilder();
        StringBuilder thisLine = new StringBuilder();
        int currLineWC = 0;
        int currLineCC = 0;
        int linesWritten = 0;
        for (int i = 0; i < words.length; i++) {
            // Reverse the spatk/spdef preservation from above
            words[i] = words[i].replace("SP__", "SP. ");
            words[i] = words[i].replace("Sp__", "Sp. ");
            int reqLength = ssd.lengthFor(words[i]);
            if (currLineWC > 0) {
                reqLength++;
            }
            if (currLineCC + reqLength <= lineSize) {
                // add to current line
                if (currLineWC > 0) {
                    thisLine.append(' ');
                }
                thisLine.append(words[i]);
                currLineWC++;
                currLineCC += reqLength;
            } else {
                // Save current line, if applicable
                if (currLineWC > 0) {
                    if (linesWritten > 0) {
                        fullDesc.append(newline);
                    }
                    fullDesc.append(thisLine);
                    linesWritten++;
                    thisLine = new StringBuilder();
                }
                // Start the new line
                thisLine.append(words[i]);
                currLineWC = 1;
                currLineCC = ssd.lengthFor(words[i]);
            }
        }

        // If the last line has anything add it
        if (currLineWC > 0) {
            if (linesWritten > 0) {
                fullDesc.append(newline);
            }
            fullDesc.append(thisLine);
        }

        return fullDesc.toString();
    }

    public static String formatTextWithReplacements(String text, Map<String, String> replacements, String newline,
            String extraline, String newpara, int maxLineLength, StringSizeDeterminer ssd) {
        // Ends with a paragraph indicator?
        boolean endsWithPara = false;
        if (text.endsWith(newpara)) {
            endsWithPara = true;
            text = text.substring(0, text.length() - newpara.length());
        }
        // Replace current line endings with spaces
        text = text.replace(newline, " ").replace(extraline, " ");
        // Replace words if replacements are set
        // do it in two stages so the rules don't conflict
        if (replacements != null) {
            int index = 0;
            for (Map.Entry<String, String> toReplace : replacements.entrySet()) {
                index++;
                text = text.replace(toReplace.getKey(), "<tmpreplace" + index + ">");
            }
            index = 0;
            for (Map.Entry<String, String> toReplace : replacements.entrySet()) {
                index++;
                text = text.replace("<tmpreplace" + index + ">", toReplace.getValue());
            }
        }
        // Split on paragraphs and deal with each one individually
        String[] oldParagraphs = text.split(newpara.replace("\\", "\\\\"));
        StringBuilder finalResult = new StringBuilder();
        int sentenceNewLineSize = Math.max(10, maxLineLength / 2);
        for (int para = 0; para < oldParagraphs.length; para++) {
            String[] words = oldParagraphs[para].split(" ");
            StringBuilder fullPara = new StringBuilder();
            StringBuilder thisLine = new StringBuilder();
            int currLineWC = 0;
            int currLineCC = 0;
            int linesWritten = 0;
            char currLineLastChar = 0;
            for (String word : words) {
                int reqLength = ssd.lengthFor(word);
                if (currLineWC > 0) {
                    reqLength++;
                }
                if ((currLineCC + reqLength > maxLineLength)
                        || (currLineCC >= sentenceNewLineSize && (currLineLastChar == '.' || currLineLastChar == '?'
                        || currLineLastChar == '!' || currLineLastChar == ','))) {
                    // new line
                    // Save current line, if applicable
                    if (currLineWC > 0) {
                        if (linesWritten > 1) {
                            fullPara.append(extraline);
                        } else if (linesWritten == 1) {
                            fullPara.append(newline);
                        }
                        fullPara.append(thisLine);
                        linesWritten++;
                        thisLine = new StringBuilder();
                    }
                    // Start the new line
                    thisLine.append(word);
                    currLineWC = 1;
                    currLineCC = ssd.lengthFor(word);
                    if (word.length() == 0) {
                        currLineLastChar = 0;
                    } else {
                        currLineLastChar = word.charAt(word.length() - 1);
                    }
                } else {
                    // add to current line
                    if (currLineWC > 0) {
                        thisLine.append(' ');
                    }
                    thisLine.append(word);
                    currLineWC++;
                    currLineCC += reqLength;
                    if (word.length() == 0) {
                        currLineLastChar = 0;
                    } else {
                        currLineLastChar = word.charAt(word.length() - 1);
                    }
                }
            }

            // If the last line has anything add it
            if (currLineWC > 0) {
                if (linesWritten > 1) {
                    fullPara.append(extraline);
                } else if (linesWritten == 1) {
                    fullPara.append(newline);
                }
                fullPara.append(thisLine);
            }
            if (para > 0) {
                finalResult.append(newpara);
            }
            finalResult.append(fullPara);
        }
        if (endsWithPara) {
            finalResult.append(newpara);
        }
        return finalResult.toString();
    }

    public interface StringSizeDeterminer {
        int lengthFor(String encodedText);
    }

    public static class StringLengthSD implements StringSizeDeterminer {

        @Override
        public int lengthFor(String encodedText) {
            return encodedText.length();
        }

    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static void main(String[] args) {
        System.out.println("    abc\tdef  \t\t ghi\r\njkl".replaceAll("\\s",""));
    }

    public static byte[] hexToBytes(String hex) {
        hex = hex.replaceAll("\\s","");
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("hex string is not of even length");
        }
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return bytes;
    }

    /**
     * A debugging tool
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    /**
     * A debugging tool
     */
    public static String bytesToHexNoSeparator(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * A debugging tool. Creates a nice hex block that is easy to read.
     */
    public static String bytesToHexBlock(byte[] bytes, int offset, int length) {
        final int rowLen = 0x10;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length / rowLen; i++) {
            byte[] row = new byte[rowLen];
            System.arraycopy(bytes, offset + i * rowLen, row, 0, rowLen);
            sb.append(bytesToHex(row));
            sb.append("\n");
        }
        if (length % rowLen != 0) {
            byte[] row = new byte[length % rowLen];
            System.arraycopy(bytes, offset + (length / rowLen) * rowLen, row, 0, row.length);
            sb.append(bytesToHex(row));
            sb.append("\n");
        }
        return sb.toString();
    }

}
