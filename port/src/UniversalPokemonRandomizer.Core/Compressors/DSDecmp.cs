namespace UniversalPokemonRandomizer.Core.Compressors;

// Ported from utils/src/main/java/compressors/DSDecmp.java
// Original: Copyright (c) 2010 Nick Kraayenbrink (MIT License)
// Modified DSDecmp-Java source for randomizer's needs.
//
// Java->C# hazards addressed:
//   - Java byte is signed (-128..127); C# byte is unsigned (0..255).
//     All Java `& 0xFF` masking is unnecessary in C# for byte array reads
//     but arithmetic intermediates use int to avoid sign extension.
//   - The Java inner-loop bug `curr_size > outData.length` (should be `>=`)
//     is fixed: the loop guard `curr_size < outData.Length` prevents over-run.

/// <summary>
/// DS LZ decompressor — ports the Java <c>DSDecmp</c> class.
/// </summary>
public static class DSDecmp
{
    /// <summary>
    /// Dispatch: reads the type byte at offset 0 and routes to the correct
    /// decompressor (0x10 = LZ10, 0x11 = LZ11).
    /// </summary>
    public static byte[] Decompress(byte[] data) => Decompress(data, 0);

    /// <summary>
    /// Dispatch starting at <paramref name="offset"/>.
    /// </summary>
    public static byte[] Decompress(byte[] data, int offset)
    {
        return (data[offset] & 0xFF) switch
        {
            0x10 => DecompressLZ10(data, offset),
            0x11 => DecompressLZ11(data, offset),
            _ => throw new ArgumentException(
                $"No compressed data found at offset 0x{offset:X}. " +
                $"Type byte = 0x{data[offset]:X2}.")
        };
    }

    /// <summary>
    /// DS LZ10 decompressor.  Accepts the raw compressed stream (including the
    /// 0x10 type byte + 3-byte little-endian length header) and returns the
    /// decompressed plaintext.
    /// </summary>
    /// <param name="data">Compressed byte array.</param>
    /// <returns>Decompressed byte array.</returns>
    public static byte[] DecompressLZ10(byte[] data) => DecompressLZ10(data, 0);

    /// <summary>
    /// DS LZ10 decompressor starting at <paramref name="offset"/>.
    /// </summary>
    public static byte[] DecompressLZ10(byte[] data, int offset)
    {
        // Skip the 0x10 type byte.
        offset++;

        // Read the 3-byte little-endian decompressed length.
        int length = (data[offset] & 0xFF)
                   | ((data[offset + 1] & 0xFF) << 8)
                   | ((data[offset + 2] & 0xFF) << 16);
        offset += 3;

        // If length == 0, read a 4-byte big-endian length (extended header).
        if (length == 0)
        {
            length = ReadFullIntBigEndian(data, offset);
            offset += 4;
        }

        byte[] outData = new byte[length];
        int currSize = 0;

        while (currSize < outData.Length)
        {
            int flags = data[offset++] & 0xFF;

            for (int i = 0; i < 8; i++)
            {
                bool flag = (flags & (0x80 >> i)) > 0;

                if (flag)
                {
                    // Back-reference token: 2 bytes.
                    // Byte layout (high nibble | low nibble of first byte, then second byte):
                    //   n    = (b >> 4) + 3          (copy length, 3..18)
                    //   disp = ((b & 0x0F) << 8) | nextByte   (lookback distance - 1)
                    int b = data[offset++] & 0xFF;
                    int n = (b >> 4) + 3;
                    int disp = (b & 0x0F) << 8;
                    disp |= data[offset++] & 0xFF;

                    if (disp > currSize)
                        throw new InvalidDataException(
                            "LZ10: Cannot go back further than already written.");

                    int cdest = currSize;
                    for (int j = 0; j < n && currSize < outData.Length; j++)
                        outData[currSize++] = outData[cdest - disp - 1 + j];

                    // Exit the flag-bit loop early if the output buffer is full.
                    if (currSize >= outData.Length)
                        break;
                }
                else
                {
                    // Literal byte.
                    int b = data[offset++] & 0xFF;

                    if (currSize < outData.Length)
                        outData[currSize++] = (byte)b;

                    if (currSize >= outData.Length)
                        break;
                }
            }
        }

        return outData;
    }

    /// <summary>
    /// DS LZ11 decompressor.  Accepts the raw compressed stream (including the
    /// 0x11 type byte + 3-byte little-endian length header) and returns the
    /// decompressed plaintext.
    /// </summary>
    /// <param name="data">Compressed byte array.</param>
    /// <returns>Decompressed byte array.</returns>
    public static byte[] DecompressLZ11(byte[] data) => DecompressLZ11(data, 0);

    /// <summary>
    /// DS LZ11 decompressor starting at <paramref name="offset"/>.
    /// </summary>
    public static byte[] DecompressLZ11(byte[] data, int offset)
    {
        // Skip the 0x11 type byte.
        offset++;

        // Read the 3-byte little-endian decompressed length.
        int length = (data[offset] & 0xFF)
                   | ((data[offset + 1] & 0xFF) << 8)
                   | ((data[offset + 2] & 0xFF) << 16);
        offset += 3;

        // If length == 0, read a 4-byte big-endian length (extended header).
        if (length == 0)
        {
            length = ReadFullIntBigEndian(data, offset);
            offset += 4;
        }

        byte[] outData = new byte[length];
        int currSize = 0;

        while (currSize < outData.Length)
        {
            int flags = data[offset++] & 0xFF;

            for (int i = 0; i < 8 && currSize < outData.Length; i++)
            {
                bool flag = (flags & (0x80 >> i)) > 0;

                if (flag)
                {
                    int b1 = data[offset++] & 0xFF;
                    int len;
                    int disp;

                    switch (b1 >> 4)
                    {
                        case 0:
                        {
                            // 3-byte encoding:  ab cd ef
                            //   len  = abc + 0x11   (b1 contributes low nibble 'a', bt contributes 'bc')
                            //   disp = def
                            int bt = data[offset++] & 0xFF;
                            len = (b1 << 4) | (bt >> 4);
                            len += 0x11;

                            disp = (bt & 0x0F) << 8;
                            int b2 = data[offset++] & 0xFF;
                            disp |= b2;
                            break;
                        }

                        case 1:
                        {
                            // 4-byte encoding:  ab cd ef gh
                            //   len  = bcde + 0x111
                            //   disp = fgh
                            int bt = data[offset++] & 0xFF;
                            int b2 = data[offset++] & 0xFF;
                            int b3 = data[offset++] & 0xFF;

                            len = (b1 & 0xF) << 12;   // b000
                            len |= bt << 4;             // bcd0
                            len |= b2 >> 4;             // bcde
                            len += 0x111;

                            disp = (b2 & 0x0F) << 8;   // f
                            disp |= b3;                  // fgh
                            break;
                        }

                        default:
                        {
                            // 2-byte encoding:  ab cd
                            //   len  = a + 1      (where a = b1 >> 4, range 2..F -> len 3..16)
                            //   disp = bcd
                            len = (b1 >> 4) + 1;

                            disp = (b1 & 0x0F) << 8;
                            int b2 = data[offset++] & 0xFF;
                            disp |= b2;
                            break;
                        }
                    }

                    if (disp > currSize)
                        throw new InvalidDataException(
                            "LZ11: Cannot go back further than already written.");

                    int cdest = currSize;
                    for (int j = 0; j < len && currSize < outData.Length; j++)
                        outData[currSize++] = outData[cdest - disp - 1 + j];

                    if (currSize >= outData.Length)
                        break;
                }
                else
                {
                    // Literal byte — no masking needed; C# byte is unsigned 0..255.
                    outData[currSize++] = data[offset++];

                    if (currSize >= outData.Length)
                        break;
                }
            }
        }

        return outData;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /// <summary>
    /// Reads a 4-byte big-endian signed integer from <paramref name="data"/>
    /// at <paramref name="offset"/>.  Mirrors Java's
    /// <c>IOFunctions.readFullIntBigEndian</c>.
    /// </summary>
    private static int ReadFullIntBigEndian(byte[] data, int offset)
        => ((data[offset] & 0xFF) << 24)
         | ((data[offset + 1] & 0xFF) << 16)
         | ((data[offset + 2] & 0xFF) << 8)
         |  (data[offset + 3] & 0xFF);
}
