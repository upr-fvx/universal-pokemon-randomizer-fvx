package compressors.gen2;

@FunctionalInterface
public interface Gen2Compressor {
    byte[] compress(byte[] uncompressed, byte[] bitFlipped);
}
