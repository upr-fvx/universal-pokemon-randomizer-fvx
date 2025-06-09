package com.dabomstew.pkromio.romhandlers.romentries;

/**
 * An entry for text in the Gen 3 games, referred to by some map event. The {@link #mapBank}, {@link #mapNumber},
 * and {@link #personNum} values specify which map event. {@link #relativePointerOffsets} is an array of
 * "relative offsets" to a pointer each; the path to take from the start of the map event's script to the pointer to
 * the text.
 * <br><br>
 * Finding these values may be cumbersome, but makes the entries largely copyable between different versions
 * of the same game (pair), since scripts rarely change.
 * <br>
 * The {@link #mapBank}, {@link #mapNumber}, and {@link #personNum} can be found using
 * <a href=https://github.com/huderlem/porymap>Porymap</a>, and so can the scripts.
 * The scripts can then be manually decoded to bytes using
 * <a href=https://web.archive.org/web/20221119033122/http://sphericalice.com/romhacking/documents/script/>this document</a>,
 * which can be counted to get {@link #relativePointerOffsets}.
 */
public class Gen3EventTextEntry {
    private final int id;
    private final int mapBank, mapNumber;
    private final int personNum;
    private final int[] relativePointerOffsets;
    private final String template;
    private int actualPointerOffset = -1;

    public Gen3EventTextEntry(int id, int mapBank, int mapNumber, int personNum, int[] relativePointerOffsets,
                              String template) {
        this.id = id;
        this.mapBank = mapBank;
        this.mapNumber = mapNumber;
        this.personNum = personNum;
        this.relativePointerOffsets = relativePointerOffsets;
        this.template = template;
    }

    public int getID() {
        return id;
    }

    public int getMapBank() {
        return mapBank;
    }

    public int getMapNumber() {
        return mapNumber;
    }

    public int getPersonNum() {
        return personNum;
    }

    public String getTemplate() {
        return template;
    }

    public int[] getRelativePointerOffsets() {
        return relativePointerOffsets;
    }

    public int getActualPointerOffset() {
        if (actualPointerOffset == -1) {
            throw new IllegalStateException("actualPointerOffset has not been set");
        }
        return actualPointerOffset;
    }

    public void setActualPointerOffset(int actualPointerOffset) {
        this.actualPointerOffset = actualPointerOffset;
    }

    @Override
    public String toString() {
        return String.format("EventTextEntry = [id=%d, actualPointerOffset=%x, mapBank=%x, mapNumber=%x, personNum=%x, " +
                        "%s=%s, template=\"%s\"]",
                id, actualPointerOffset, mapBank, mapNumber, personNum,
                relativePointerOffsets.length < 2 ? "relativePointerOffset" : "relativePointerOffsets",
                relativePointerOffsetsToString(),
                template);
    }

    public String relativePointerOffsetsToString() {
        if (relativePointerOffsets == null || relativePointerOffsets.length == 0) {
            return "invalid";
        } else if (relativePointerOffsets.length == 1) {
            return "0x" + Integer.toHexString(relativePointerOffsets[0]);
        } else {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < relativePointerOffsets.length; i++) {
                sb.append("0x").append(Integer.toHexString(relativePointerOffsets[i]));
                if (i != relativePointerOffsets.length - 1) {
                    sb.append(";");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

}