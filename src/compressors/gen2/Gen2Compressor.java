package compressors.gen2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A compressor for the compression format used by the Generation 2 Pokémon games.
 * This compression format is commonly known as LC_LZ3 or LZ3, and is documented
 * <a href=https://sneslab.net/wiki/LZ3>here on SNESlab</a>.
 */
public abstract class Gen2Compressor {

    protected static final int SHORT_COMMAND_COUNT = 32;
    protected static final int MAX_COMMAND_COUNT = 1024;
    protected static final int LOOKBACK_LIMIT = 128;
    private static final int MAX_FILE_SIZE = 32768;

    protected int MAX_CHUNK_LENGTH = 0b11111;
    protected byte TERMINATOR = (byte) 0xFF;

    protected enum Command {
        // "Long Length" functionality baked into the logic, instead of being an option here
        DIRECT_COPY(0), BYTE_FILL(1), WORD_FILL(2), ZERO_FILL(3),
        REPEAT(4), BIT_REVERSE_REPEAT(5), BACKWARDS_REPEAT(6);

        private final int bits;

        Command(int bits) {
            this.bits = bits;
        }
    }

    protected static class Chunk {
        protected final Command command;
        protected int count;
        protected final int value;

        public Chunk(Command command, int count, int value) {
            this.command = command;
            this.count = count;
            this.value = value;
        }

        public int size() {
            int headerSize = count > SHORT_COMMAND_COUNT ? 2 : 1;
            if (command.bits >= 4) { // the Repeat commands
                return headerSize + 1 + (value >= 0 ? 1 : 0);
            }
            int[] commandSizes = new int[]{count, 1, 2, 0};
            return headerSize + commandSizes[command.bits];
        }

        @Override
        public String toString() {
            return "[" + command + ", count=" + count + ", value=" + value + "]";
        }
    }

    public abstract byte[] compress(byte[] uncompressed, byte[] bitFlipped);

    protected Chunk pickBestChunk(Chunk... chunks) {
        if (Arrays.equals(Arrays.stream(chunks).distinct().toArray(), new Chunk[]{null})) {
            return null;
        }
        return Arrays.stream(chunks).max((a, b) ->
                {
                    if (a == null) return -1;
                    if (b == null) return 1;
                    int aSavings = a.count - a.size();
                    int bSavings = b.count - b.size();
                    return Integer.compare(aSavings, bSavings);
                }
        ).orElseThrow(RuntimeException::new);
    }

    protected List<Chunk> mergeDirectCopy(List<Chunk> chunks) {
        List<Chunk> merged = new ArrayList<>();
        int sum = 0;
        for (Chunk chunk : chunks) {
            if (chunk.command == Command.DIRECT_COPY) {
                sum += chunk.count;
            } else {
                if (sum != 0) {
                    merged.add(new Chunk(Command.DIRECT_COPY, sum, 0));
                    sum = 0;
                }
                merged.add(chunk);
            }
        }
        if (sum != 0) {
            merged.add(new Chunk(Command.DIRECT_COPY, sum, 0));
        }
        return merged;
    }

    protected byte[] chunksToBytes(List<Chunk> chunks, byte[] uncompressed) {
        byte[] board = new byte[uncompressed.length * 2];
        int size = 0;
        int pos = 0;
        for (Chunk chunk : chunks) {
            if (chunk.count < SHORT_COMMAND_COUNT) { // short header
                board[size++] = (byte) ((chunk.command.bits << 5) + ((chunk.count - 1) & 0b11111));
            } else { // long header (i.e. command 111 / "Long length")
                board[size++] = (byte) (0b11100000 + (chunk.command.bits << 2) + (chunk.count >>> 8));
                board[size++] = (byte) ((chunk.count - 1) & 0xFF);
            }
            switch (chunk.command) {
                case DIRECT_COPY:
                    for (int i = 0; i < chunk.count; i++) {
                        board[size++] = uncompressed[pos + i];
                    }
                    break;
                case BYTE_FILL:
                    board[size++] = (byte) chunk.value;
                    break;
                case WORD_FILL:
                    board[size++] = (byte) (chunk.value >>> 8);
                    board[size++] = (byte) (chunk.value);
                    break;
                case ZERO_FILL:
                    break;
                default:
                    if ((chunk.value < -LOOKBACK_LIMIT) || (chunk.value >= MAX_FILE_SIZE)) {
                        throw new IllegalStateException("invalid command");
                    }
                    if (chunk.value < 0) {
                        board[size++] = (byte) (chunk.value ^ 127);
                    } else {
                        board[size++] = (byte) (chunk.value >>> 8);
                        board[size++] = (byte) (chunk.value);
                    }
            }
            pos += chunk.count;
        }
        board[size++] = TERMINATOR;

        return Arrays.copyOf(board, size);
    }
}
