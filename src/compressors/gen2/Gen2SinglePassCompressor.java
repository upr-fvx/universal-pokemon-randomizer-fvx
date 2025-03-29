package compressors.gen2;

import java.util.ArrayList;
import java.util.List;

public class Gen2SinglePassCompressor implements Gen2Compressor {

    private static final int SHORT_COMMAND_COUNT = 32;
    private static final int MAX_COMMAND_COUNT = 1024;
    private static final int LOOKBACK_LIMIT = 128;
    private static final int LOOKAHEAD_LIMIT = 3072;

    private enum Command {
        DIRECT_COPY(0), BYTE_FILL(1), WORD_FILL(2), ZERO_FILL(3),
        REPEAT(4), BIT_REVERSE_REPEAT(5), BACKWARDS_REPEAT(6), LONG_LENGTH(7);

        private final int bits;

        Command(int bits) {
            this.bits = bits;
        }
    }

    private static class Chunk {
        private final Command command;
        private int count;
        private final byte[] value;

        public Chunk(Command command, int count, byte[] value) {
            this.command = command;
            this.count = count;
            this.value = value;
        }

        public Chunk(Command command, int count, int value) {
            this.command = command;
            this.count = count;
            this.value = new byte[]{(byte) value};
        }

    }

    /*
   Single-pass compressor: attempts to compress the data in a single pass, selecting the best command at each
                           position within some constraints.
   Methods defined: 72
   Flags values:
     Bit fields (will trigger alternate behavior if set):
     1: prefer repetition commands over copy commands of equal count
     2: don't emit a copy or repetition with a count equal to its size when the previous command is a literal (0) that
        is not at maximum size (32 or 1024)
     4: don't emit long copy commands
     Selector values (pick one from each group and add them to the bit fields):
     - Scan delay: number of bytes that are forced into literal (0) commands after each non-literal command:
       0: 0 bytes
       8: 1 byte
       16: 2 bytes
     - Copy command preference (when the command counts are tied), in order from most to least:
       0: normal (4), reversed (6), flipped (5)
       24: reversed (6), flipped (5), normal (4)
       48: flipped (5), reversed (6), normal (4)
*/

    public enum CopyCommandPref {
        NRF, RFN, FRN
    }

    private final boolean preferFillOverRepeat;
    private final boolean avoidFillOrRepeat;
    private final boolean avoidLongRepeat;
    private final int scanDelay;
    private final CopyCommandPref copyCommandPref;

    public Gen2SinglePassCompressor(boolean preferFillOverRepeat, boolean avoidFillOrRepeat, boolean avoidLongRepeat,
                                    int scanDelay, CopyCommandPref copyCommandPref) {
        if (scanDelay < 0 || scanDelay > 2) {
            throw new IllegalArgumentException("Scan delay must be between 0-2");
        }
        this.preferFillOverRepeat = preferFillOverRepeat;
        this.avoidFillOrRepeat = avoidFillOrRepeat;
        this.avoidLongRepeat = avoidLongRepeat;
        this.scanDelay = scanDelay;
        this.copyCommandPref = copyCommandPref;
    }

    @Override
    public byte[] compress(byte[] uncompressed, byte[] bitFlipped) {

        // init chunks array
        List<Chunk> chunks = new ArrayList<>();
        // current_command = commands[0]
        Chunk current = new Chunk();
        chunks.add(current);
        // init some values
        int pos = 0;
        int previousData = 0;
        int scanDDelay = 0;
        Chunk fill;
        Chunk repeat;
        while (pos < uncompressed.length) {
            fill = findBestFill(uncompressed, pos);
            repeat = findBestRepeat(uncompressed, bitFlipped, pos);
            if (preferFillOverRepeat) {
                current = pick_best_command(2, fill, repeat);
            } else {
                current = pick_best_command(2, repeat, fill);
            }
            Chunk direct = new Chunk(Command.DIRECT_COPY, 1, pos);
            current = pick_best_command(2, direct, current);
            if (avoidFillOrRepeat && (command_size(current) == current.count)
                && previousData && (previousData != SHORT_COMMAND_COUNT) && (previousData != MAX_COMMAND_COUNT)) {
                current = = new Chunk(Command.DIRECT_COPY, 1, pos);
            }
            if (scanDelay != 0) {
                if (scanDDelay >= scanDDelay) {
                    scanDDelay = 0;
                } else if (current.command != Command.DIRECT_COPY) {
                    scanDDelay++;
                    current = new Chunk(Command.DIRECT_COPY, 1, pos);
                }
            }
            if (current.command != Command.DIRECT_COPY) {
                previousData = 0;
            } else {
                previousData += current.count;
            }
            pos += current.count;
            current = new Chunk();
            chunks.add(current);
        }
        optimize(commands, current_command - commands);
        repack(&commands, length);
        return commands;
    }

    private Chunk findBestRepeat(byte[] data, byte[] bitFlipped, int pos) {
        Chunk simple = scanForwards(data, data, pos, Command.REPEAT);
        Chunk flipped = scanForwards(data, bitFlipped, pos, Command.BIT_REVERSE_REPEAT);
        Chunk backwards = scanBackwards(data, pos);
        Chunk chunk;
        switch (copyCommandPref) {
            case NRF:
                chunk = pick_best_command(3, simple, backwards, flipped);
                break;
            case RFN:
                chunk = pick_best_command(3, backwards, flipped, simple);
                break;
            case FRN:
                chunk = pick_best_command(3, flipped, backwards, simple);
        }
        if (avoidLongRepeat && (chunk.count > SHORT_COMMAND_COUNT)) {
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
                return new Chunk(Command.BYTE_FILL, 1, new byte[]{data[pos]});
            } else {
                return new Chunk(Command.ZERO_FILL, 1, new byte[]{});
            }
        }

        byte[] value = {data[pos], data[pos + 1]};
        int limit = data.length - pos;
        limit = Math.min(limit, MAX_CHUNK_LENGTH);
        int repCount = 2;
        while ((repCount < limit) && (data[pos + repCount] == value[repCount & 1])) {
            repCount++;
        }

        if (value[0] != value[1]) {
            if (value[0] == 0 && repCount < 3) {
                return new Chunk(Command.ZERO_FILL, 1, new byte[]{});
            } else {
                return new Chunk(Command.WORD_FILL, repCount, value);
            }
        } else if (value[0] != 0) {
            return new Chunk(Command.BYTE_FILL, repCount, new byte[]{value[0]});
        } else {
            return new Chunk(Command.ZERO_FILL, repCount, new byte[]{});
        }
    }
}
