package compressors.gen2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Attempts to compress the data in a single pass, selecting the best command at each
 * position within some constraints.<br>
 * (<a href=https://sneslab.net/wiki/LZ3>Documentation for the compression format</a>).
 * <br><br>
 * Essentially a Java translation of pokecrystal's
 * <a href=https://github.com/pret/pokecrystal/blob/master/tools/lz/spcomp.c>spcomp.c</a>.
 */
public class Gen2SinglePassCompressor extends Gen2Compressor {

    public enum CopyCommandPref {
        NRF, // Normal (100), Reversed (110), Flipped (101)
        RFN, // Reversed (110), Flipped (101), Normal (100)
        FRN  // Flipped (101), Reversed (110), Normal (100)
    }

    public static List<Gen2SinglePassCompressor> ALL_OPTIONS = initAllOptions();

    private static List<Gen2SinglePassCompressor> initAllOptions() {
        List<Gen2SinglePassCompressor> l = new ArrayList<>(2 * 2 * 2 * 3 * 3);
        for (int pfor = 0; pfor < 2; pfor++) {
            for (int afor = 0; afor < 2; afor++) {
                for (int alr = 0; alr < 2; alr++) {
                    for (int maxScanDelay = 0; maxScanDelay < 3; maxScanDelay++) {
                        for (CopyCommandPref pref : CopyCommandPref.values()) {
                            l.add(new Gen2SinglePassCompressor(
                                    pfor == 1,
                                    afor == 1,
                                    alr == 1,
                                    maxScanDelay, pref
                            ));
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(l);
    }

    private final boolean preferFillOverRepeat;
    private final boolean avoidFillOrRepeat;
    private final boolean avoidLongRepeat;
    private final int maxScanDelay;
    private final CopyCommandPref copyCommandPref;

    /**
     * @param preferFillOverRepeat If true: prefer fill chunks over repeat chunks of equal count.
     * @param avoidFillOrRepeat If true: don't emit a fill or repeat with a count equal to its size when the previous
     *                          chunks is a Direct Copy (000) that is not at maximum size (32 or 1024).
     * @param avoidLongRepeat If true: don't emit long copy chunks.
     * @param maxScanDelay Number of bytes that are forced into Direct Copy (000) chunks after each
     *                     non-Direct Copy chunk.
     * @param copyCommandPref Copy command preference (when the command counts are tied), in order from most to least.
     */
    public Gen2SinglePassCompressor(boolean preferFillOverRepeat, boolean avoidFillOrRepeat, boolean avoidLongRepeat,
                                    int maxScanDelay, CopyCommandPref copyCommandPref) {
        if (maxScanDelay < 0 || maxScanDelay > 2) {
            throw new IllegalArgumentException("Max scan delay must be between 0-2");
        }
        this.preferFillOverRepeat = preferFillOverRepeat;
        this.avoidFillOrRepeat = avoidFillOrRepeat;
        this.avoidLongRepeat = avoidLongRepeat;
        this.maxScanDelay = maxScanDelay;
        this.copyCommandPref = copyCommandPref;
    }

    @Override
    public String toString() {
        return "SPC{" + preferFillOverRepeat +
                ", " + avoidFillOrRepeat +
                ", " + avoidLongRepeat +
                ", " + maxScanDelay +
                ", " + copyCommandPref +
                '}';
    }

    @Override
    public byte[] compress(byte[] uncompressed, byte[] bitFlipped) {

        Chunk[] chunks = new Chunk[uncompressed.length];
        int curr = 0;

        int pos = 0;
        int previousData = 0;
        int scanDelay = 0;

        while (pos < uncompressed.length) {
            Chunk fill = findBestFill(uncompressed, pos);
            Chunk repeat = findBestRepeat(uncompressed, bitFlipped, pos);
            if (preferFillOverRepeat) {
                chunks[curr] = pickBestChunk(fill, repeat);
            } else {
                chunks[curr] = pickBestChunk(repeat, fill);
            }

            Chunk direct = new Chunk(Command.DIRECT_COPY, 1, pos);
            chunks[curr] = pickBestChunk(direct, chunks[curr]);
            if (avoidFillOrRepeat && (chunks[curr].size() == chunks[curr].count)
                    && (previousData != 0) && (previousData != SHORT_COMMAND_COUNT)
                    && (previousData != MAX_COMMAND_COUNT)) {
                chunks[curr] = new Chunk(Command.DIRECT_COPY, 1, pos);
            }

            if (maxScanDelay != 0) {
                if (scanDelay >= maxScanDelay) {
                    scanDelay = 0;
                } else if (chunks[curr].command != Command.DIRECT_COPY) {
                    scanDelay++;
                    chunks[curr] = new Chunk(Command.DIRECT_COPY, 1, pos);
                }
            }

            if (chunks[curr].command != Command.DIRECT_COPY) {
                previousData = 0;
            } else {
                previousData += chunks[curr].count;
            }
            pos += chunks[curr].count;

            curr++;
        }
//        optimize(commands, current_command - commands);
//        repack(&commands, length);
//        return commands;
        List<Chunk> chunksList = Arrays.asList(Arrays.copyOf(chunks, curr)); // cut off the nulls
        chunksList = mergeDirectCopy(chunksList);
        return chunksToBytes(chunksList, uncompressed);
    }

    private Chunk findBestRepeat(byte[] data, byte[] bitFlipped, int pos) {
        Chunk simple = scanForwards(data, data, pos, Command.REPEAT);
        Chunk flipped = scanForwards(data, bitFlipped, pos, Command.BIT_REVERSE_REPEAT);
        Chunk backwards = scanBackwards(data, pos);
        Chunk chunk;
        switch (copyCommandPref) {
            case NRF:
                chunk = pickBestChunk(simple, backwards, flipped);
                break;
            case RFN:
                chunk = pickBestChunk(backwards, flipped, simple);
                break;
            case FRN:
                chunk = pickBestChunk(flipped, backwards, simple);
                break;
            default:
                throw new RuntimeException("Should be unreachable...");
        }
        if (avoidLongRepeat && chunk != null && (chunk.count > SHORT_COMMAND_COUNT)) {
            chunk.count = SHORT_COMMAND_COUNT;
        }
        return chunk;
    }

    private Chunk scanForwards(byte[] target, byte[] source, int realPos, Command command) {
        int limit = source.length - realPos;
        int bestMatch = 0;
        int bestLength = 0;
        for (int pos = 0; pos < realPos; pos++) {
            if (source[pos] != target[realPos]) continue;

            int currentLength = 0;
            while ((currentLength < limit) && (source[pos + currentLength] == target[realPos + currentLength])) {
                currentLength++;
            }
            currentLength = Math.min(currentLength, MAX_COMMAND_COUNT);
            if (currentLength > bestLength) {
                bestMatch = pos;
                bestLength = currentLength;
            }
        }
        if (bestLength == 0) {
            return null;
        }
        int offset = bestMatch;
        if (offset + LOOKBACK_LIMIT >= realPos) {
            offset -= realPos;
        }
        return new Chunk(command, bestLength, offset);
    }

    private Chunk scanBackwards (byte[] data, int realPos) {
        int limit = data.length - realPos;
        limit = Math.min(realPos, limit);

        int bestMatch = 0;
        int bestLength = 0;
        for (int pos = 0; pos < realPos; pos++) {
            if (data[pos] != data[realPos]) continue;

            int currentLength = 0;
            while ((currentLength <= pos) && (currentLength < limit) &&
                    (data[pos - currentLength] == data[realPos + currentLength])) {
                currentLength++;
            }
            currentLength = Math.min(currentLength, MAX_COMMAND_COUNT);
            if (currentLength > bestLength) {
                bestMatch = pos;
                bestLength = currentLength;
            }
        }
        if (bestLength == 0) {
            return null;
        }
        int offset = bestMatch;
        if (offset + LOOKBACK_LIMIT >= realPos) {
            offset -= realPos;
        }
        return new Chunk(Command.BACKWARDS_REPEAT, bestLength, offset);
    }

    private Chunk findBestFill(byte[] data, int pos) {

        if (pos + 1 >= data.length) {
            if (data[pos] != 0) {
                return new Chunk(Command.BYTE_FILL, 1, data[pos]);
            } else {
                return new Chunk(Command.ZERO_FILL, 1, 0);
            }
        }

        byte[] value = {data[pos], data[pos + 1]};
        int limit = data.length - pos;
        limit = Math.min(limit, MAX_COMMAND_COUNT);
        int repCount = 2;
        while ((repCount < limit) && (data[pos + repCount] == value[repCount & 1])) {
            repCount++;
        }

        if (value[0] != value[1]) {
            if (value[0] == 0 && repCount < 3) {
                return new Chunk(Command.ZERO_FILL, 1, 0);
            } else {
                return new Chunk(Command.WORD_FILL, repCount, ((value[0] & 0xFF) << 8) + (value[1] & 0xFF));
            }
        } else if (value[0] != 0) {
            return new Chunk(Command.BYTE_FILL, repCount, value[0]);
        } else {
            return new Chunk(Command.ZERO_FILL, repCount, 0);
        }
    }

}
