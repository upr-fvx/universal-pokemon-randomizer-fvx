package compressors.gen2;

import java.util.Arrays;

/**
 * Uses "direct copy" / 000 only.<br>
 * (<a href=https://sneslab.net/wiki/LZ3>Documentation for the compression format</a>).
 */
public class Gen2NullCompressor extends Gen2Compressor {

    // https://sneslab.net/wiki/LZ3

    @Override
    public byte[] compress(byte[] uncompressed, byte[] bitFlipped) {
        int size = 0;
        int left = uncompressed.length;
        byte[] board = new byte[uncompressed.length*2];
        while (left > 0) {
            int chunk = Math.min(left, MAX_CHUNK_LENGTH);
            board[size] = (byte) ((chunk - 1) & 0b11111);
            size++;
            System.arraycopy(uncompressed, uncompressed.length - left, board, size, chunk);
            size += chunk;
            left -= chunk;
        }
        board[size++] = TERMINATOR;

        return Arrays.copyOf(board, size);
    }
}
