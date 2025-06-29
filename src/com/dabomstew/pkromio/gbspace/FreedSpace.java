package com.dabomstew.pkromio.gbspace;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2022.                                 --*/
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

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Represents/handles all the manually freed bytes in a ROM. If bytes are manually freed (using free())
 * we know they should be available for writing at a later time, at which point we can findAndUnfree() them.
 **/
public class FreedSpace {

    private static final String ALREADY_FREED_EXCEPTION_MESSAGE =  "Can't free a space that is already freed. " +
            "This is a safety measure to prevent bad usage of free()." +
            "\n%s overlaps with existing freed chunk %s";

    protected static class FreedChunk {

        public int start, end;

        public FreedChunk(int offset, int end) {
            this.start = offset;
            this.end = end;
        }

        public int getLength() {
            return end - start + 1;
        }

        @Override
        public String toString() {
            return String.format("%x-%x", start, end);
        }

    }

    protected final LinkedList<FreedChunk> freedChunks = new LinkedList<>();

    public void free(int start, int length) {
        // System.out.printf("freeing %d bytes starting from 0x%x.%n", length, start);
        if (length < 1) {
            throw new IllegalArgumentException("length must be at least 1");
        }

        int end = start + length - 1;
        FreedChunk toFree = new FreedChunk(start, end);

        boolean freedChunkAdded = false;
        ListIterator<FreedChunk> iterator = freedChunks.listIterator();
        while (iterator.hasNext() && !freedChunkAdded) {
            FreedChunk neighbor = iterator.next();

            if (toFree.start < neighbor.start) {
                addChunkBeforeOrMergeWithRightNeighbor(toFree, neighbor, iterator);
                freedChunkAdded = true;

            } else if (neighbor.end == toFree.start - 1) {
                mergeChunkWithLeftOrBothNeighbors(toFree, neighbor, iterator);
                freedChunkAdded = true;

            } else if (neighbor.end >= toFree.start) {
                throw new RuntimeException(String.format(ALREADY_FREED_EXCEPTION_MESSAGE, toFree, neighbor));
            }
        }

        if (!freedChunkAdded) {
            freedChunks.add(toFree);
        }
        // System.out.println("after:\t" + this);
    }

    protected final void addChunkBeforeOrMergeWithRightNeighbor(FreedChunk toFree, FreedChunk rightNeighbor,
                                                                ListIterator<FreedChunk> iterator) {
        if (toFree.end >= rightNeighbor.start) {
            throw new RuntimeException(String.format(ALREADY_FREED_EXCEPTION_MESSAGE, toFree, rightNeighbor));
        }

        if (toFree.end == rightNeighbor.start - 1) {
            rightNeighbor.start = toFree.start;
        } else {
            iterator.previous();
            iterator.add(toFree);
        }
    }

    protected final void mergeChunkWithLeftOrBothNeighbors(FreedChunk toFree, FreedChunk leftNeighbor,
                                                           ListIterator<FreedChunk> iterator) {
        if (iterator.hasNext()) {
            FreedChunk rightNeighbor = iterator.next();

            if (toFree.end >= rightNeighbor.start) {
                throw new RuntimeException(String.format(ALREADY_FREED_EXCEPTION_MESSAGE, toFree, rightNeighbor));
            } else if (toFree.end == rightNeighbor.start - 1) {
                leftNeighbor.end = rightNeighbor.end;
                iterator.remove();
            } else {
                leftNeighbor.end = toFree.end;
            }
            iterator.previous();

        } else {
            leftNeighbor.end = toFree.end;
        }
    }

    public int findAndUnfree(int length) {
        // System.out.println("looking for " + length + " bytes");
        FreedChunk found = find(length);
        if (found == null) {
            return -1;
        }
        int offset = found.start;
        unfree(found, length);
        // System.out.println("after:\t" + this);
        return offset;
    }

    protected final FreedChunk find(int length) {
        for (FreedChunk fc : freedChunks) {
            if (fc.getLength() >= length) {
                return fc;
            }
        }
        return null;
    }

    protected final void unfree(FreedChunk toUnfree, int length) {
        // System.out.println("unfreeing " + length + " bytes starting from 0x" + Integer.toHexString(toUnfree.start));
        toUnfree.start += length;
        if (toUnfree.start > toUnfree.end) {
            freedChunks.remove(toUnfree);
        }
    }

    public int getLengthSum() {
        int sum = 0;
        for (FreedChunk fs : freedChunks) {
            sum += fs.getLength();
        }
        return sum;
    }

    @Override
    public String toString() {
		return getLengthSum() + " bytes, " + freedChunks.size() + " chunks, " + freedChunks;
	}

}
