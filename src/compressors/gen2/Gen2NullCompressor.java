package compressors.gen2;

import java.util.Arrays;

public class Gen2NullCompressor implements Gen2Compressor {

    private static final byte TERMINATOR = (byte) 0xFF;
    private static final int MAX_CHUNK_SIZE = 0b11111;
    // https://sneslab.net/wiki/LZ3

    /**
     * Uses "direct copy" / 000 only.
     */
    @Override
    public byte[] compress(byte[] uncompressed, byte[] bitFlipped) {
        int size = 0;
        int left = uncompressed.length;
        byte[] board = new byte[uncompressed.length*2];
        while (left > 0) {
            int chunk = Math.min(left, MAX_CHUNK_SIZE);
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
