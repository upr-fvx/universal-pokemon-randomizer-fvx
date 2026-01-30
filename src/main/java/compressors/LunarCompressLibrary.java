package compressors;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Wraps the Lunar Compress .dll, by FuSoYa.
 * <a href="https://fusoya.eludevisibility.org/lc/index.html">Source for Lunar Compress here</a>.
 * <br>
 * Documentation for these methods is taken from the documentation of the .dll (version 1.90),
 * with modifications to make them read "in Java".
 * The source documentation has not been included, but can be found in downloads of Lunar Compress (see link above).
 */
public interface LunarCompressLibrary extends Library {
    String LUNAR_COMPRESS_PATH = "Lunar_Compress_1.90_x64";

    int LC_LZ3_FORMAT = 2;
    int LC_LZ3_FORMAT2 = 0;

    LunarCompressLibrary INSTANCE = Native.load(LUNAR_COMPRESS_PATH, LunarCompressLibrary.class);

    /**
    Returns the current version of the DLL as an integer.
    For example, version 1.30 of the DLL would return "130" (decimal).
     **/
    int LunarVersion();

    int LunarDecompress(byte[] destination, int addressToStart, int maxDataSize, int format, int format2,
                        int[] lastROMPosition);

    /**
     * Compress data from a byte array and place it into another array.
     * <br>
     * Returns the size of the compressed data.  A value of zero indicates
     * failure.
     * <br>
     * The Source and Destination variables can point to the same array.
     * <br>
     * If the size of the compressed data is greater than MaxDataSize, the data
     * will be truncated to fit into the array.  Note however that the size value
     * returned by the function will not be the truncated size.
     * <br>
     * In general, a max limit of 0x10000 bytes is supported for the uncompressed
     * data, which is the size of a HiROM SNES bank.  A few formats may have lower
     * limits depending on their design.
     * <br>
     * If Destination=NULL and/or MaxDataSize=0, no data will be copied to the
     * array but the function will still compress the data so it can return the
     * size of it.
     * @param source        source byte array of data to compress.
     * @param destination   destination byte array for compressed data.
     * @param dataSize      size of the data to compress in bytes.
     * @param maxDataSize   maximum number of bytes to copy into dest.
     * @param format        compression format (see LunarDecompress() table)
     * @param format2       must be zero unless otherwise stated.
     **/
    int LunarRecompress(byte[] source, byte[] destination, int dataSize, int maxDataSize, int format, int format2);
}
