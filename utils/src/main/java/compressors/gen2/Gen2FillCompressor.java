package compressors.gen2;

import java.util.Arrays;

/**
 * Uses the three "fill" commands: "byte fill" / 001, "word fill" / 010, and "zero fill" / 011.
 * (<a href=https://sneslab.net/wiki/LZ3>Documentation for the compression format</a>).
 * <br><br>
 * Based on pokecrystal's
 * <a href=https://github.com/pret/pokecrystal/blob/master/tools/lz/repcomp.c>repcomp.c</a>.
 */
public class Gen2FillCompressor extends Gen2Compressor {

    private enum Command {
        BYTE_FILL(1), WORD_FILL(2), ZERO_FILL(3);

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

    @Override
    public byte[] compress(byte[] uncompressed, byte[] bitFlipped) {
        int size = 0;
        int pos = 0;
        byte[] board = new byte[uncompressed.length * 2];

        while (pos < uncompressed.length) {
            Chunk chunk = findBestRepetition(uncompressed, pos);

            board[size++] = (byte) ((chunk.command.bits << 5) + ((chunk.count - 1) & 0b11111));

            System.arraycopy(chunk.value, 0, board, size, chunk.value.length);
            size += chunk.value.length;
            pos += chunk.count;
        }
        board[size++] = TERMINATOR;

        return Arrays.copyOf(board, size);
    }

    private Chunk findBestRepetition(byte[] data, int pos) {

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
