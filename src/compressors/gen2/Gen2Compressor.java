package compressors.gen2;

/**
 * A compressor for the compression format used by the Generation 2 Pokémon games.
 * This compression format is commonly known as LC_LZ3 or LZ3, and is documented
 * <a href=https://sneslab.net/wiki/LZ3>here on SNESlab</a>.
 */
public interface Gen2Compressor {

    int MAX_CHUNK_LENGTH = 0b11111;
    byte TERMINATOR = (byte) 0xFF;

    byte[] compress(byte[] uncompressed, byte[] bitFlipped);
}
