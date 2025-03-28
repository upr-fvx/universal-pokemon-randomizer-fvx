package compressors.gen2;

import java.util.ArrayList;
import java.util.List;

public class Gen2SinglePassCompressor implements Gen2Compressor {

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
        private final int count;
        private final byte[] value;

        public Chunk(Command command, int count, byte[] value) {
            this.command = command;
            this.count = count;
            this.value = value;
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
            Chunk direct = {.command = 0, .count = 1, .value = position};
            current = pick_best_command(2, direct, current);
            if (avoidFillOrRepeat && (command_size(current) == current.count)
                && previousData && (previousData != SHORT_COMMAND_COUNT) && (previousData != MAX_COMMAND_COUNT)) {
                current = {.command = 0, .count = 1, .value = position} ;
            }
            if (scanDelay != 0) {
                if (scanDDelay >= scanDDelay) {
                    scanDDelay = 0;
                } else if (current.command != Command.DIRECT_COPY) {
                    scanDDelay++;
                    current = {.command = 0, .count = 1, .value = position};
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
        Chunk simple = {.command = 7};
        Chunk flipped = simple;
        Chunk backwards = simple;
        int count;
        int offset;
        if ((count = scan_forwards(data + position, length - position, data, pos, &offset))){
            simple = {.command = 4, .count = count, .value = offset} ;
        }
        if ((count = scan_forwards(data + position, length - position, bitFlipped, pos, &offset))){
            flipped = {.command = 5, .count = count, .value = offset};
        }
        if ((count = scan_backwards(data, length - position, pos, &offset))){
            backwards = {.command = 6, .count = count, .value = offset };
        }
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

    unsigned short scan_forwards (const unsigned char * target, unsigned short limit, const unsigned char * source, unsigned short real_position, short * offset) {
        unsigned short best_match, best_length = 0;
        unsigned short current_length;
        unsigned short position;
        for (position = 0; position < real_position; position ++) {
            if (source[position] != *target) continue;
            for (current_length = 0; (current_length < limit) && (source[position + current_length] == target[current_length]); current_length ++);
            if (current_length > MAX_COMMAND_COUNT) {
                current_length = MAX_COMMAND_COUNT;
            }
            if (current_length < best_length) continue;
            best_match = position;
            best_length = current_length;
        }
        if (!best_length) {
            return 0;
        }
        if ((best_match + LOOKBACK_LIMIT) >= real_position) {
            *offset = best_match - real_position;
        } else {
            *offset = best_match;
        }
        return best_length;
    }

    unsigned short scan_backwards (const unsigned char * data, unsigned short limit, unsigned short real_position, short * offset) {
        if (real_position < limit) limit = real_position;
        unsigned short best_match, best_length = 0;
        unsigned short current_length;
        unsigned short position;
        for (position = 0; position < real_position; position ++) {
            if (data[position] != data[real_position]) continue;
            for (current_length = 0; (current_length <= position) && (current_length < limit) &&
                    (data[position - current_length] == data[real_position + current_length]); current_length ++);
            if (current_length > MAX_COMMAND_COUNT) {
                current_length = MAX_COMMAND_COUNT;
            }
            if (current_length < best_length) continue;
            best_match = position;
            best_length = current_length;
        }
        if (!best_length) return 0;
        if ((best_match + LOOKBACK_LIMIT) >= real_position) {
            *offset = best_match - real_position;
        } else {
            *offset = best_match;
        }
        return best_length;
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
