package fixtures;

import compressors.DSCmp;
import compressors.DSDecmp;
import compressors.Gen1Decmp;
import compressors.Gen2Cmp;
import compressors.Gen2Decmp;
import cuecompressors.BLZCoder;
import filefunctions.IOFunctions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * UPR-5 Golden-Vector Capture Harness
 *
 * Generates a deterministic fixture corpus on disk that characterises
 * every significant utils compression function.  A port in any language
 * verifies itself by reading the produced .in / .out files and checking
 * that its own implementation of each function reproduces the .out from
 * the .in.
 *
 * Usage (from repo root, Windows):
 *   .\gradlew :utils:test --tests "fixtures.GoldenVectorHarness"
 *
 * The corpus is written to  fixtures/  at the repository root.
 * The manifest is written to fixtures/manifest.tsv
 *
 * All random inputs use a fixed seed (0xDEADBEEFL) so the corpus is
 * fully reproducible.
 */
public class GoldenVectorHarness {

    // ---------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------

    /** Fixed seed for all PRNG use – never change this. */
    private static final long SEED = 0xDEADBEEFL;

    /** Root directory for fixtures, relative to the working directory when the
     *  test is run (repo root via Gradle). */
    private static final String FIXTURE_ROOT = "fixtures";

    // ---------------------------------------------------------------------------
    // Entry point – JUnit is NOT needed.  Can be run as a plain main() too.
    // ---------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        new GoldenVectorHarness().run();
    }

    /** Called by JUnit (via @org.junit.jupiter.api.Test) or directly from main. */
    @org.junit.jupiter.api.Test
    public void generateFixtures() throws Exception {
        run();
    }

    // ---------------------------------------------------------------------------
    // Orchestration
    // ---------------------------------------------------------------------------

    private final List<ManifestEntry> manifest = new ArrayList<>();

    public void run() throws Exception {
        Path root = Paths.get(FIXTURE_ROOT);
        Files.createDirectories(root);

        generateDSDecmpLZ10Fixtures(root);
        generateDSDecmpLZ11Fixtures(root);
        generateDSCmpLZ10Fixtures(root);
        generateDSCmpLZ11Fixtures(root);
        generateGen2DecmpFixtures(root);
        generateGen2CmpFixtures(root);
        generateGen1DecmpFixtures(root);
        generateBLZFixtures(root);
        generateIOFunctionsFixtures(root);

        writeManifest(root);
        System.out.println("[GoldenVectorHarness] Corpus written to: " + root.toAbsolutePath());
        System.out.println("[GoldenVectorHarness] Total fixtures: " + manifest.size());
    }

    // ---------------------------------------------------------------------------
    // DSDecmp – LZ10
    // ---------------------------------------------------------------------------

    private void generateDSDecmpLZ10Fixtures(Path root) throws Exception {
        String fn = "DSDecmp_LZ10";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // Fixture layout for decompressors:
        //   .in  = compressed bytes (produced by DSCmp.compressLZ10)
        //   .out = original plain bytes (the known-correct decompressed result)
        //
        // We do NOT call DSDecmp.Decompress to produce .out — the plain bytes
        // ARE the ground truth.  A port verifies itself by running its own
        // DSDecmp implementation on .in and comparing against .out.
        //
        // This avoids triggering the known inner-loop over-read bug in the
        // Java DSDecmp (documented below) during fixture generation.
        //
        // Known DSDecmp LZ10/LZ11 bug:
        //   The inner flag-byte processing loop does not break when the output
        //   buffer reaches capacity (uses `curr_size > length` instead of `>=`),
        //   causing ArrayIndexOutOfBoundsException on inputs whose compressed
        //   form does not over-fill by a whole back-reference.  Empty input
        //   additionally triggers an extended-length read OOB.  These are
        //   implementation bugs in the Java code; a correct port must handle
        //   all sizes cleanly.

        record Case(String name, byte[] plain, String desc) {}
        List<Case> cases = List.of(
            // All sizes work here because we don't call Java's buggy DSDecmp.
            new Case("eight_bytes",      new byte[]{0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48},
                                         "8 distinct bytes"),
            new Case("repetitive_256",   makeRepetitive(256),
                                         "256 bytes of 0xAA (highly repetitive)"),
            new Case("repetitive_4096",  makeRepetitive(4096),
                                         "4096 bytes of 0xAA (long repetitive run)"),
            new Case("random_256",       makeRandom(256, SEED),
                                         "256 bytes pseudo-random (seed 0xDEADBEEF)"),
            new Case("random_2048",      makeRandom(2048, SEED + 1),
                                         "2048 bytes pseudo-random (seed 0xDEADBEF0)"),
            new Case("alternating_256",  makeAlternating(256),
                                         "256 bytes alternating 0x00/0xFF"),
            new Case("sawtooth_256",     makeSawtooth(256),
                                         "256 bytes sawtooth (0x00-0xFF repeated)"),
            new Case("mixed_512",        makeMixed(512, SEED + 2),
                                         "512 bytes mixed structured+random (sprite-like)")
        );

        for (Case c : cases) {
            byte[] compressed = DSCmp.compressLZ10(c.plain);
            // .in = compressed stream, .out = expected decompressed (plain text)
            writeFixture(dir, c.name, compressed, c.plain);
            addEntry(fn, c.name, c.desc, dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    // ---------------------------------------------------------------------------
    // DSDecmp – LZ11
    // ---------------------------------------------------------------------------

    private void generateDSDecmpLZ11Fixtures(Path root) throws Exception {
        String fn = "DSDecmp_LZ11";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // Same approach as LZ10: .in = compressed, .out = plain (ground truth).
        // LZ11 supports larger back-references; include a 65536-byte case to
        // exercise the extended length codes (type 0x00 and 0x10 prefixes).

        record Case(String name, byte[] plain, String desc) {}
        List<Case> cases = List.of(
            new Case("eight_bytes",      new byte[]{0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48},
                                         "8 distinct bytes"),
            new Case("repetitive_256",   makeRepetitive(256),
                                         "256 bytes of 0xAA"),
            new Case("repetitive_4096",  makeRepetitive(4096),
                                         "4096 bytes of 0xAA"),
            new Case("random_256",       makeRandom(256,  SEED + 10),
                                         "256 bytes pseudo-random (seed+10)"),
            new Case("random_2048",      makeRandom(2048, SEED + 11),
                                         "2048 bytes pseudo-random (seed+11)"),
            new Case("alternating_256",  makeAlternating(256),
                                         "256 bytes alternating 0x00/0xFF"),
            new Case("sawtooth_256",     makeSawtooth(256),
                                         "256 bytes sawtooth"),
            new Case("mixed_512",        makeMixed(512, SEED + 12),
                                         "512 bytes mixed structured+random"),
            new Case("repetitive_65536", makeRepetitiveBlock(65536, 0xBB),
                                         "65536 bytes of 0xBB (exercises LZ11 extended length codes)")
        );

        for (Case c : cases) {
            byte[] compressed = DSCmp.compressLZ11(c.plain);
            writeFixture(dir, c.name, compressed, c.plain);
            addEntry(fn, c.name, c.desc, dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    // ---------------------------------------------------------------------------
    // DSCmp – LZ10 compress
    // ---------------------------------------------------------------------------

    private void generateDSCmpLZ10Fixtures(Path root) throws Exception {
        String fn = "DSCmp_LZ10";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // For compress fixtures: input = raw bytes, output = compressed bytes.

        record Case(String name, byte[] plain, String desc) {}
        List<Case> cases = List.of(
            new Case("empty",           new byte[0],               "compress empty input"),
            new Case("single_byte",     new byte[]{0x01},          "compress single byte"),
            new Case("repetitive_256",  makeRepetitive(256),       "compress 256 repetitive bytes"),
            new Case("random_256",      makeRandom(256, SEED+20),  "compress 256 pseudo-random bytes"),
            new Case("sawtooth_256",    makeSawtooth(256),         "compress 256-byte sawtooth"),
            new Case("mixed_512",       makeMixed(512, SEED+21),   "compress 512-byte mixed")
        );

        for (Case c : cases) {
            byte[] compressed = DSCmp.compressLZ10(c.plain);
            writeFixture(dir, c.name, c.plain, compressed);
            addEntry(fn, c.name, c.desc, dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    // ---------------------------------------------------------------------------
    // DSCmp – LZ11 compress
    // ---------------------------------------------------------------------------

    private void generateDSCmpLZ11Fixtures(Path root) throws Exception {
        String fn = "DSCmp_LZ11";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        record Case(String name, byte[] plain, String desc) {}
        List<Case> cases = List.of(
            new Case("empty",           new byte[0],               "compress empty input"),
            new Case("single_byte",     new byte[]{0x01},          "compress single byte"),
            new Case("repetitive_256",  makeRepetitive(256),       "compress 256 repetitive bytes"),
            new Case("random_256",      makeRandom(256, SEED+30),  "compress 256 pseudo-random bytes"),
            new Case("sawtooth_256",    makeSawtooth(256),         "compress 256-byte sawtooth"),
            new Case("mixed_512",       makeMixed(512, SEED+31),   "compress 512-byte mixed")
        );

        for (Case c : cases) {
            byte[] compressed = DSCmp.compressLZ11(c.plain);
            writeFixture(dir, c.name, c.plain, compressed);
            addEntry(fn, c.name, c.desc, dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    // ---------------------------------------------------------------------------
    // Gen2Decmp – GBC LZ decompressor
    // ---------------------------------------------------------------------------

    private void generateGen2DecmpFixtures(Path root) throws Exception {
        String fn = "Gen2Decmp";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // We build compressed data with Gen2Cmp.compress (Java path, not Lunar DLL)
        // by temporarily forcing non-Windows to avoid DLL dependency.
        // Since we're on Windows, Gen2Cmp.compress will try to use the Lunar DLL.
        // We use a helper that builds valid Gen2 compressed bytes directly.

        record Case(String name, byte[] plain, String desc) {}
        List<Case> cases = List.of(
            new Case("single_byte",    new byte[]{0x42},          "1 byte literal"),
            new Case("repetitive_32",  makeRepetitive(32),        "32 repetitive bytes"),
            new Case("repetitive_128", makeRepetitiveBlock(128, 0xAA), "128 x 0xAA"),
            new Case("alternating_32", makeAlternating(32),       "32-byte alternating 0x00/0xFF"),
            new Case("sawtooth_32",    makeSawtooth(32),          "32-byte sawtooth"),
            new Case("random_64",      makeRandom(64, SEED+40),   "64 pseudo-random bytes"),
            new Case("mixed_128",      makeMixed(128, SEED+41),   "128-byte mixed")
        );

        for (Case c : cases) {
            byte[] compressed = buildGen2Compressed(c.plain);
            byte[] decompressed = Gen2Decmp.decompress(compressed, 0);
            writeFixture(dir, c.name, compressed, decompressed);
            addEntry(fn, c.name, c.desc, dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    /**
     * Builds a minimal valid Gen2 / LC_LZ3-style compressed byte stream for
     * the given plaintext using only the "literal copy" command (cmd=0).
     * This avoids the Lunar Compress DLL dependency and produces data
     * Gen2Decmp can correctly decode.
     *
     * Format reference: Gen2Decmp.java  (cmd byte layout: high 3 bits = cmd, low 5 bits = len-1)
     * cmd=0, len ≤ 32: byte 0x00|(len-1) followed by len literal bytes.
     * Terminator: 0xFF.
     */
    private byte[] buildGen2Compressed(byte[] plain) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (i < plain.length) {
            // Normal literal command: cmd=0, max 32 bytes per packet
            int chunkLen = Math.min(plain.length - i, 32);
            // cmd=0 normal: high 3 bits = 0, low 5 bits = chunkLen-1
            out.write(chunkLen - 1);
            out.write(plain, i, chunkLen);
            i += chunkLen;
        }
        out.write(0xFF); // LZ_END
        return out.toByteArray();
    }

    // ---------------------------------------------------------------------------
    // Gen2Cmp – GBC LZ compressor (flipBits only – DLL-independent part)
    // ---------------------------------------------------------------------------

    private void generateGen2CmpFixtures(Path root) throws Exception {
        String fn = "Gen2Cmp_flipBits";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // Gen2Cmp.compress on Windows calls the Lunar DLL which may not be
        // present in CI.  We capture the DLL-independent flipBits() function,
        // which is a pure bijection on every byte.

        record Case(String name, byte[] plain, String desc) {}
        List<Case> cases = List.of(
            new Case("empty",          new byte[0],               "flip bits of empty array"),
            new Case("single_byte",    new byte[]{0x58},          "flip bits of 0x58 => 0x1A"),
            new Case("all_zeros",      new byte[256],             "flip bits of all-zero array (identity)"),
            new Case("sawtooth_256",   makeSawtooth(256),         "flip bits of 0x00-0xFF sawtooth"),
            new Case("random_256",     makeRandom(256, SEED+50),  "flip bits of 256 random bytes")
        );

        for (Case c : cases) {
            byte[] flipped = Gen2Cmp.flipBits(c.plain);
            writeFixture(dir, c.name, c.plain, flipped);
            addEntry(fn, c.name, c.desc, dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    // ---------------------------------------------------------------------------
    // Gen1Decmp – GB/GBC sprite decompressor
    // ---------------------------------------------------------------------------

    private void generateGen1DecmpFixtures(Path root) throws Exception {
        String fn = "Gen1Decmp";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // Gen1Decmp expects a compressed bit-stream; producing valid input
        // requires either:
        //   a) Running Gen1Cmp.compress (needs BufferedImage, not headless-safe), or
        //   b) Hand-crafting a valid Gen1 bit stream.
        //
        // We use approach (b) with a programmatic bit-writer so there are no
        // calculation errors.  The fixture is a 1x1-tile (8x8 px) all-white image:
        //   sizex = 8 (1 tile wide), sizey = 1 (1 tile tall), mode 0
        //   both bitplanes are all-zero after delta encoding
        //   each bitplane occupies size*4 = 32 "bit-group" slots, all zero
        //   RLE run: length = 32 zeros.
        //
        // Gen1Cmp RLE encoding for length=32:
        //   getBitCount(32): bc starts at 1, while(32>((1<<bc)-2)) bc++
        //     bc=1: 32>0 yes; bc=2: 32>2 yes; bc=3: 32>6 yes; bc=4: 32>14 yes
        //     bc=5: 32>30 yes; bc=6: 32>62 no → bc=6. Then bc-- → bitCount=5.
        //   writeBitCount(5): write 4 ones then a zero: 1 1 1 1 0
        //   writeValue(32,5): for j=5..1: ((32+1)>>(j-1))&1
        //     j=5: (33>>4)&1=0; j=4: (33>>3)&1=0; j=3: (33>>2)&1=0
        //     j=2: (33>>1)&1=0; j=1: (33>>0)&1=1  → 0 0 0 0 1
        //   Total RLE token: 1 1 1 1 0 | 0 0 0 0 1  (10 bits)
        //
        // Full bit stream (32 bits):
        //   pos  0- 7: dim = (1<<4)|1 = 0x11     → 0 0 0 1 0 0 0 1
        //   pos  8   : r1 = 0
        //   pos  9   : bp1 packet type = 0 (RLE)
        //   pos 10-14: ones-run = 1 1 1 1 0
        //   pos 15-19: value = 0 0 0 0 1
        //   pos 20   : mode = 0
        //   pos 21   : bp2 packet type = 0 (RLE)
        //   pos 22-26: ones-run = 1 1 1 1 0
        //   pos 27-31: value = 0 0 0 0 1
        //
        //   byte 0 (bits  0- 7) = 0001 0001 = 0x11
        //   byte 1 (bits  8-15) = 0 0 1 1 1 1 0 0 = 0x3C
        //   byte 2 (bits 16-23) = 0 1 0 0 1 1 1 1 = 0x4F
        //   byte 3 (bits 24-31) = 0 0 0 0 0 1 0 0 = 0x04

        byte[] gen1_1x1_white = gen1BuildCompressed();

        Gen1Decmp dec = new Gen1Decmp(gen1_1x1_white, 0);
        dec.decompress();
        byte[] decompressed = dec.getData();

        writeFixture(dir, "1x1_all_white", gen1_1x1_white, decompressed);
        addEntry(fn, "1x1_all_white",
                "1x1-tile all-white sprite: compressed stream built by programmatic bit-writer",
                dir.resolve("1x1_all_white.in"), dir.resolve("1x1_all_white.out"));
    }

    /**
     * Builds a valid Gen1-compressed bit stream for a 1x1-tile all-white image
     * using a programmatic bit-writer.  Both bitplanes are all-zero; each uses a
     * single RLE run of 32 zero bit-groups.  Mode 0, r1=0.
     */
    private byte[] gen1BuildCompressed() {
        // Tiny inline bit-writer (MSB-first per byte, same as Gen1Cmp.BitWriteStream)
        int[] bits = new int[64]; // more than enough
        int pos = 0;

        // --- dimension byte: (widthTiles << 4) | heightTiles = (1<<4)|1 = 0x11
        for (int i = 7; i >= 0; i--) {
            bits[pos++] = (0x11 >> i) & 1;
        }

        // --- r1 = 0
        bits[pos++] = 0;

        // --- bp1 packet type: 0 (RLE)
        bits[pos++] = 0;

        // --- RLE for 32 zeros ---
        pos = gen1WriteRLE(bits, pos, 32);

        // --- mode = 0 (single bit 0)
        bits[pos++] = 0;

        // --- bp2 packet type: 0 (RLE)
        bits[pos++] = 0;

        // --- RLE for 32 zeros ---
        pos = gen1WriteRLE(bits, pos, 32);

        // Pack bits into bytes (MSB-first)
        int numBytes = (pos + 7) / 8;
        byte[] result = new byte[numBytes];
        for (int i = 0; i < pos; i++) {
            if (bits[i] == 1) {
                result[i / 8] |= (byte)(1 << (7 - (i % 8)));
            }
        }
        return result;
    }

    /**
     * Writes a Gen1 RLE token encoding {@code length} zero bit-groups into
     * {@code bits} starting at {@code pos}, and returns the new position.
     */
    private int gen1WriteRLE(int[] bits, int pos, int length) {
        // Replicate Gen1Cmp.getBitCount + writeBitCount + writeValue
        int bc = 1;
        while (length > ((1 << bc) - 2)) bc++;
        int bitCount = bc - 1; // ignore leading '1'

        // writeBitCount: (bitCount-1) ones then a zero
        for (int j = 0; j < bitCount - 1; j++) bits[pos++] = 1;
        bits[pos++] = 0;

        // writeValue: bitCount bits of (length+1), MSB first
        for (int j = bitCount; j > 0; j--) {
            bits[pos++] = ((length + 1) >> (j - 1)) & 1;
        }
        return pos;
    }

    // ---------------------------------------------------------------------------
    // BLZCoder – Bottom LZ (NDS overlays)
    // ---------------------------------------------------------------------------

    private void generateBLZFixtures(Path root) throws Exception {
        String fn = "BLZCoder";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // BLZ_DecodePub with reference="GARC" invokes LZSS_Decode (LZ11/GARC path)
        // which expects a 0x11 header: same format as DSDecmp LZ11.
        // We generate those with DSCmp.compressLZ11.

        // BLZ_DecodePub with reference != "GARC" invokes BLZ_Decode which
        // expects a Bottom-LZ encoded stream.  Bottom-LZ is an *inverted*
        // LZ stream with a footer header; BLZ_EncodePub produces it.
        // We use reference="GARC" for the LZSS path and reference="BLZ" for the BLZ path.

        BLZCoder coder = new BLZCoder(null);

        // --- GARC (LZSS / LZ11) path ---
        // LZSS_Decode uses BLZCoder's own LZ11-style decoder.
        // Fixture layout: .in = LZ11-compressed stream, .out = plain bytes.
        // NOTE: LZSS_Decode(DSCmp.compressLZ11(new byte[0])) throws
        // BufferUnderflowException because the empty LZ11 stream's decSize=0
        // triggers a second buf.getInt() read past end-of-buffer.
        // Empty input is therefore a documented limitation; skip it.

        record GarcCase(String name, byte[] plain, String desc) {}
        List<GarcCase> garcCases = List.of(
            // empty skipped: LZSS_Decode throws BufferUnderflowException for empty LZ11 stream
            new GarcCase("garc_single_byte",  new byte[]{0x55},          "GARC/LZSS decode single byte"),
            new GarcCase("garc_repetitive",   makeRepetitive(128),       "GARC/LZSS decode 128 repetitive bytes"),
            new GarcCase("garc_random_256",   makeRandom(256, SEED+60),  "GARC/LZSS decode 256 random bytes"),
            new GarcCase("garc_mixed_512",    makeMixed(512, SEED+61),   "GARC/LZSS decode 512 mixed bytes")
        );

        for (GarcCase c : garcCases) {
            byte[] lz11 = DSCmp.compressLZ11(c.plain);
            byte[] decoded;
            try {
                decoded = coder.BLZ_DecodePub(lz11, "GARC");
            } catch (Exception e) {
                System.err.println("[WARN] BLZCoder GARC decode threw for case: " + c.name + ": " + e + " – skipping");
                continue;
            }
            if (decoded == null) {
                System.err.println("[WARN] BLZCoder GARC decode returned null for case: " + c.name + " – skipping");
                continue;
            }
            // .in = compressed LZ11 stream, .out = expected plain bytes
            writeFixture(dir, c.name, lz11, c.plain);
            addEntry(fn, c.name, c.desc,
                    dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }

        // --- BLZ (Bottom-LZ) encode → decode round-trip ---
        // BLZ_EncodePub builds a Bottom-LZ stream; BLZ_DecodePub decodes it.
        // We use arm9=false, best=false (normal mode) and reference="ROM".
        record BlzCase(String name, byte[] plain, String desc) {}
        List<BlzCase> blzCases = List.of(
            new BlzCase("blz_single_byte",  new byte[]{0x42},         "BLZ encode→decode single byte"),
            new BlzCase("blz_repetitive",   makeRepetitive(128),      "BLZ encode→decode 128 repetitive bytes"),
            new BlzCase("blz_random_128",   makeRandom(128, SEED+70), "BLZ encode→decode 128 random bytes"),
            new BlzCase("blz_mixed_256",    makeMixed(256, SEED+71),  "BLZ encode→decode 256 mixed bytes")
        );

        for (BlzCase c : blzCases) {
            // Suppress BLZ encoder's progress output by redirecting stdout temporarily
            PrintStream saved = System.out;
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            byte[] encoded;
            try {
                encoded = coder.BLZ_EncodePub(c.plain, false, false, "ROM");
            } finally {
                System.setOut(saved);
            }
            if (encoded == null) {
                System.err.println("[WARN] BLZCoder encode returned null for case: " + c.name + " – skipping");
                continue;
            }
            byte[] decoded = coder.BLZ_DecodePub(encoded, "ROM");
            if (decoded == null) {
                System.err.println("[WARN] BLZCoder decode returned null for case: " + c.name + " – skipping");
                continue;
            }
            writeFixture(dir, c.name, encoded, decoded);
            addEntry(fn, c.name, c.desc,
                    dir.resolve(c.name + ".in"), dir.resolve(c.name + ".out"));
        }
    }

    // ---------------------------------------------------------------------------
    // IOFunctions – read/write helpers
    // ---------------------------------------------------------------------------

    private void generateIOFunctionsFixtures(Path root) throws Exception {
        String fn = "IOFunctions";
        Path dir = root.resolve(fn);
        Files.createDirectories(dir);

        // read2ByteInt (little-endian)
        {
            byte[] buf = {0x34, 0x12};  // LE => 0x1234
            byte[] out = intToFixtureBytes(IOFunctions.read2ByteInt(buf, 0));
            writeFixture(dir, "read2ByteInt_LE", buf, out);
            addEntry(fn, "read2ByteInt_LE",
                    "read2ByteInt([0x34,0x12],0) => 0x00001234 (LE)",
                    dir.resolve("read2ByteInt_LE.in"), dir.resolve("read2ByteInt_LE.out"));
        }

        // read2ByteIntBigEndian
        {
            byte[] buf = {0x12, 0x34};  // BE => 0x1234
            byte[] out = intToFixtureBytes(IOFunctions.read2ByteIntBigEndian(buf, 0));
            writeFixture(dir, "read2ByteIntBigEndian", buf, out);
            addEntry(fn, "read2ByteIntBigEndian",
                    "read2ByteIntBigEndian([0x12,0x34],0) => 0x00001234 (BE)",
                    dir.resolve("read2ByteIntBigEndian.in"), dir.resolve("read2ByteIntBigEndian.out"));
        }

        // readFullInt (little-endian 32-bit)
        {
            byte[] buf = {0x78, 0x56, 0x34, 0x12};  // LE => 0x12345678
            byte[] out = intToFixtureBytes(IOFunctions.readFullInt(buf, 0));
            writeFixture(dir, "readFullInt_LE", buf, out);
            addEntry(fn, "readFullInt_LE",
                    "readFullInt([0x78,0x56,0x34,0x12],0) => 0x12345678 (LE)",
                    dir.resolve("readFullInt_LE.in"), dir.resolve("readFullInt_LE.out"));
        }

        // readFullIntBigEndian
        {
            byte[] buf = {0x12, 0x34, 0x56, 0x78};  // BE => 0x12345678
            byte[] out = intToFixtureBytes(IOFunctions.readFullIntBigEndian(buf, 0));
            writeFixture(dir, "readFullIntBigEndian", buf, out);
            addEntry(fn, "readFullIntBigEndian",
                    "readFullIntBigEndian([0x12,0x34,0x56,0x78],0) => 0x12345678 (BE)",
                    dir.resolve("readFullIntBigEndian.in"), dir.resolve("readFullIntBigEndian.out"));
        }

        // write2ByteInt (little-endian)
        {
            byte[] buf = new byte[2];
            IOFunctions.write2ByteInt(buf, 0, 0xABCD);
            byte[] inBytes = intToFixtureBytes(0xABCD);
            writeFixture(dir, "write2ByteInt_LE", inBytes, buf);
            addEntry(fn, "write2ByteInt_LE",
                    "write2ByteInt(0xABCD) => [0xCD,0xAB] (LE)",
                    dir.resolve("write2ByteInt_LE.in"), dir.resolve("write2ByteInt_LE.out"));
        }

        // writeFullInt (little-endian)
        {
            byte[] buf = new byte[4];
            IOFunctions.writeFullInt(buf, 0, 0xDEADBEEF);
            byte[] inBytes = longToFixtureBytes(0xDEADBEEFL);
            writeFixture(dir, "writeFullInt_LE", inBytes, buf);
            addEntry(fn, "writeFullInt_LE",
                    "writeFullInt(0xDEADBEEF) => [0xEF,0xBE,0xAD,0xDE] (LE)",
                    dir.resolve("writeFullInt_LE.in"), dir.resolve("writeFullInt_LE.out"));
        }

        // writeFullIntBigEndian
        {
            byte[] buf = new byte[4];
            IOFunctions.writeFullIntBigEndian(buf, 0, 0xDEADBEEF);
            byte[] inBytes = longToFixtureBytes(0xDEADBEEFL);
            writeFixture(dir, "writeFullIntBigEndian", inBytes, buf);
            addEntry(fn, "writeFullIntBigEndian",
                    "writeFullIntBigEndian(0xDEADBEEF) => [0xDE,0xAD,0xBE,0xEF] (BE)",
                    dir.resolve("writeFullIntBigEndian.in"), dir.resolve("writeFullIntBigEndian.out"));
        }
    }

    // ---------------------------------------------------------------------------
    // Manifest
    // ---------------------------------------------------------------------------

    private static class ManifestEntry {
        final String function;
        final String caseName;
        final String description;
        final String inPath;
        final String outPath;

        ManifestEntry(String function, String caseName, String description, String inPath, String outPath) {
            this.function    = function;
            this.caseName    = caseName;
            this.description = description;
            this.inPath      = inPath;
            this.outPath     = outPath;
        }
    }

    private void addEntry(String function, String caseName, String description, Path in, Path out) {
        manifest.add(new ManifestEntry(function, caseName, description,
                in.toString().replace('\\', '/'),
                out.toString().replace('\\', '/')));
    }

    private void writeManifest(Path root) throws IOException {
        Path manifestPath = root.resolve("manifest.tsv");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(manifestPath.toFile()), StandardCharsets.UTF_8))) {
            pw.println("function\tcase\tdescription\tin_path\tout_path");
            for (ManifestEntry e : manifest) {
                pw.printf("%s\t%s\t%s\t%s\t%s%n",
                        e.function, e.caseName, e.description, e.inPath, e.outPath);
            }
        }
        System.out.println("[GoldenVectorHarness] Manifest: " + manifestPath.toAbsolutePath());
    }

    // ---------------------------------------------------------------------------
    // File I/O helpers
    // ---------------------------------------------------------------------------

    private void writeFixture(Path dir, String name, byte[] in, byte[] out) throws IOException {
        Files.write(dir.resolve(name + ".in"),  in);
        Files.write(dir.resolve(name + ".out"), out);
    }

    // ---------------------------------------------------------------------------
    // Data generators (all deterministic)
    // ---------------------------------------------------------------------------

    private byte[] makeRepetitive(int length) {
        return makeRepetitiveBlock(length, 0xAA);
    }

    private byte[] makeRepetitiveBlock(int length, int value) {
        byte[] data = new byte[length];
        Arrays.fill(data, (byte) value);
        return data;
    }

    private byte[] makeRandom(int length, long seed) {
        Random rng = new Random(seed);
        byte[] data = new byte[length];
        rng.nextBytes(data);
        return data;
    }

    private byte[] makeAlternating(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (i & 1) == 0 ? (byte) 0x00 : (byte) 0xFF;
        }
        return data;
    }

    private byte[] makeSawtooth(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (i & 0xFF);
        }
        return data;
    }

    /** Structured (repeating 8-byte pattern) first half, random second half. */
    private byte[] makeMixed(int length, long seed) {
        byte[] data = new byte[length];
        // First half: repeating pattern 0x00 0x11 0x22 0x33 0x44 0x55 0x66 0x77
        byte[] pattern = {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77};
        int half = length / 2;
        for (int i = 0; i < half; i++) {
            data[i] = pattern[i % pattern.length];
        }
        // Second half: random
        Random rng = new Random(seed);
        for (int i = half; i < length; i++) {
            data[i] = (byte) rng.nextInt(256);
        }
        return data;
    }

    /** Encodes an int as 4 big-endian bytes (for IOFunctions fixtures). */
    private byte[] intToFixtureBytes(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >>  8) & 0xFF),
                (byte) ( value        & 0xFF)
        };
    }

    private byte[] longToFixtureBytes(long value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >>  8) & 0xFF),
                (byte) ( value        & 0xFF)
        };
    }
}
