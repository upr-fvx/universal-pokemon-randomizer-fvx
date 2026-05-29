# Phase 2 Work Breakdown — `utils` Module Porting

**Repo:** https://github.com/TimvanderWal504/universal-pokemon-randomizer-fvx
**Branch:** `upr/UPR-12`
**Base commit SHA:** `88d92e38`
**Date produced:** 2026-05-29
**Produced by:** claude-sonnet-4-6 (upr-worker subagent)

**Related UPR issues:**
- [UPR-4] Coverage map — source of class inventory and coverage percentages
- [UPR-5] Fixture corpus — source of golden-vector coverage data
- [UPR-10] Java→C# porting notes — semantic hazards and Phase 2 checklist

---

## Summary

Phase 2 ports all remaining `utils` classes to C#. DSDecmp was already ported in Phase 1 (UPR-9) and is included here for completeness only. The module has **23 Java classes** (including inner classes); the effective porting target is **22 classes** (DSDecmp already done). The fixture corpus covers 9 function directories; 13 classes have no fixture coverage at all and need fixtures extended before or during porting.

---

## 1. Class Inventory (Ordered — Leaf-First)

Classes are ordered so that every class appears after all its `utils`-internal dependencies. Classes at the same tier have no ordering constraint relative to each other.

### Tier 0 — Pure leaves (no utils dependencies)

#### 1. `IOFunctions`
- **Source:** `utils/src/main/java/filefunctions/IOFunctions.java`
- **Public methods:**
  - `static long readFullLong(byte[] data, int offset)`
  - `static int readFullInt(byte[] data, int offset)`
  - `static int readFullIntBigEndian(byte[] data, int offset)`
  - `static int read2ByteIntBigEndian(byte[] data, int offset)`
  - `static int read2ByteInt(byte[] data, int offset)`
  - `static void write2ByteInt(byte[] data, int offset, int value)`
  - `static void writeFullInt(byte[] data, int offset, int value)`
  - `static void writeFullIntBigEndian(byte[] data, int offset, int value)`
  - `static void writeFullLong(byte[] data, int offset, long value)`
  - `static long getCRC32(byte[] data)`
- **Utils dependencies:** none
- **Notes:** Byte-order conversion helpers plus CRC32. Used by DSDecmp.

#### 2. `FileFunctions`
- **Source:** `utils/src/main/java/filefunctions/FileFunctions.java`
- **Public methods:**
  - `static byte[] readFileFullyIntoBuffer(String filename)`
  - `static byte[] readFullyIntoBuffer(InputStream in, int bytes)`
  - `static int read2ByteBigEndianIntFromFile(RandomAccessFile file, long offset)`
  - `static int readBigEndianIntFromFile(RandomAccessFile file, long offset)`
  - `static int readIntFromFile(RandomAccessFile file, long offset)`
  - `static void writeBytesToFile(String filename, byte[] data)`
- **Utils dependencies:** none
- **Notes:** Wraps file I/O. Used by PatchFunctions and BLZCoder. The `readFullyIntoBuffer` static import in PatchFunctions means FileFunctions must be ported before PatchFunctions.

#### 3. `FileNameFunctions`
- **Source:** `utils/src/main/java/filefunctions/FileNameFunctions.java`
- **Public methods:**
  - `static File fixFilename(File original, String defaultExtension)`
  - `static File fixFilename(File original, String defaultExtension, List<String> bannedExtensions)`
- **Utils dependencies:** none
- **Notes:** Standalone filename-extension normaliser. No other utils class depends on it.

#### 4. `DSCmp`
- **Source:** `utils/src/main/java/compressors/DSCmp.java`
- **Public methods:**
  - `static byte[] compressLZ10(byte[] decompressed)`
  - `static byte[] compressLZ11(byte[] decompressed)`
- **Utils dependencies:** none
- **Notes:** Uses only `java.io.ByteArrayOutputStream`. DSDecmp depends on IOFunctions, not DSCmp.

#### 5. `Gen1Cmp`
- **Source:** `utils/src/main/java/compressors/Gen1Cmp.java`
- **Public methods:**
  - `static byte[] compress(BufferedImage bitplane1Image, BufferedImage bitplane2Image)`
  - `Gen1Cmp(BufferedImage bitplane1Image, BufferedImage bitplane2Image)`
  - `byte[] compressUsingModeAndOrder(int mode, boolean order)`
- **Inner classes:** `Gen1Cmp$BitWriteStream` (private), `Gen1Cmp$BitReadStream` (private)
- **Utils dependencies:** none
- **Notes:** Requires `System.Drawing.Bitmap` (C# equivalent of `BufferedImage`) as input. No utils imports; purely JDK-dependent.

#### 6. `Gen1Decmp`
- **Source:** `utils/src/main/java/compressors/Gen1Decmp.java`
- **Public methods:**
  - `Gen1Decmp(byte[] input, int baseOffset)`
  - `void decompress()`
  - `byte[] getData()`
  - `int getWidth()`
  - `int getHeight()`
  - `int getCompressedLength()`
- **Inner class:** `Gen1Decmp$BitStream` (private)
- **Utils dependencies:** none
- **Notes:** Stateful decompressor object. Produces raw pixel data (not a `BufferedImage`).

#### 7. `Gen2Decmp`
- **Source:** `utils/src/main/java/compressors/Gen2Decmp.java`
- **Public methods:**
  - `static byte[] decompress(byte[] data, int offset)`
  - `static int lengthOfCompressed(byte[] data, int offset)`
- **Utils dependencies:** none
- **Notes:** Pure-Java LC_LZ3 decompressor. No imports outside JDK.

#### 8. `LunarCompressLibrary`
- **Source:** `utils/src/main/java/compressors/LunarCompressLibrary.java`
- **Public methods (interface):**
  - `int LunarVersion()`
  - `int LunarDecompress(byte[] destination, int addressToStart, int maxDataSize, int format, int format2, int[] lastROMPosition)`
  - `int LunarRecompress(byte[] source, byte[] destination, int dataSize, int maxDataSize, int format, int format2)`
- **Utils dependencies:** none
- **Notes:** JNA wrapper interface. In C# this becomes a P/Invoke declaration or a wrapper class around `DllImport`. The DLL (`Lunar_Compress_1.90_x64`) is Windows-only. Used by Gen2Cmp.

#### 9. `Gen2Compressor`
- **Source:** `utils/src/main/java/compressors/gen2/Gen2Compressor.java`
- **Public methods:**
  - `abstract byte[] compress(byte[] uncompressed, byte[] bitFlipped)`
- **Utils dependencies:** none
- **Notes:** Abstract base class for the Gen2 compressor strategy hierarchy. Also contains protected helper methods used by subclasses (`protected byte[] initOutBuffer(int len)`, etc.).

#### 10. `Gen2NullCompressor`
- **Source:** `utils/src/main/java/compressors/gen2/Gen2NullCompressor.java`
- **Public methods:**
  - `byte[] compress(byte[] uncompressed, byte[] bitFlipped)`
- **Utils dependencies:** none (extends Gen2Compressor which is in the same package)
- **Notes:** Direct-copy-only compressor. Extends Gen2Compressor.

#### 11. `Gen2FillCompressor`
- **Source:** `utils/src/main/java/compressors/gen2/Gen2FillCompressor.java`
- **Public methods:**
  - `byte[] compress(byte[] uncompressed, byte[] bitFlipped)`
- **Utils dependencies:** none (extends Gen2Compressor)
- **Notes:** Fill-command-only compressor. Extends Gen2Compressor.

#### 12. `Gen2SinglePassCompressor`
- **Source:** `utils/src/main/java/compressors/gen2/Gen2SinglePassCompressor.java`
- **Public methods:**
  - `byte[] compress(byte[] uncompressed, byte[] bitFlipped)`
  - `public static final List<Gen2SinglePassCompressor> ALL_OPTIONS` (all option combinations, instantiated at class init)
- **Utils dependencies:** none (extends Gen2Compressor)
- **Notes:** Main Gen2 strategy. Many option combinations; `ALL_OPTIONS` is a static list of all permutations used by Gen2Cmp.

#### 13. `UnicodeParser`
- **Source:** `utils/src/main/java/text/UnicodeParser.java`
- **Public API:**
  - `public static String[] tb` (decode table, 65536 entries)
  - `public static Map<String, Integer> d` (encode map)
  - Loaded via static initializer from resource `/text/Generation4.tbl`
- **Utils dependencies:** none
- **Notes:** Data-only class; no methods, just public fields populated in the static initializer. Used by PPTxtHandler, PokeTextData, and TextToPoke (via field references, no explicit import in Java).

#### 14. `N3DSTextCodes`
- **Source:** `utils/src/main/java/text/N3DSTextCodes.java`
- **Public methods:**
  - `static Map<Integer, String> getTextVariableCodes(int romType)`
  - `static int getVariableCode(String name, int romType)`
- **Utils dependencies:** none
- **Notes:** Lookup tables for 3DS text variable codes. No external dependencies.

#### 15. `RandomPointSelector`
- **Source:** `utils/src/main/java/randompoint/RandomPointSelector.java`
- **Public methods:**
  - `RandomPointSelector(Random random, int dimensions, double[] lower, double[] upper, Function<double[], Double> weight)`
  - `double[] getRandomPoint()`
  - `double getRelativeMeasureUnderCurve()`
  - `Random getRandom()`
  - `void setRandom(Random random)`
- **Utils dependencies:** none
- **Notes:** Monte Carlo weighted point sampler. Fully standalone.

### Tier 1 — One level of utils dependencies

#### 16. `DSDecmp` *(Done — Phase 1, UPR-9)*
- **Source:** `utils/src/main/java/compressors/DSDecmp.java`
- **Public methods:**
  - `static byte[] Decompress(byte[] data)`
  - `static byte[] Decompress(byte[] data, int offset)`
- **Utils dependencies:** `IOFunctions`
- **Notes:** Already ported in Phase 1 (UPR-9). Listed for completeness.

#### 17. `PatchFunctions`
- **Source:** `utils/src/main/java/filefunctions/PatchFunctions.java`
- **Public methods:**
  - `static void applyPatch(byte[] rom, String path)`
- **Utils dependencies:** `FileFunctions` (static import of `readFullyIntoBuffer`)
- **Notes:** IPS patch applicator. Requires FileFunctions to be ported first.

#### 18. `BLZCoder`
- **Source:** `utils/src/main/java/cuecompressors/BLZCoder.java`
- **Public methods:**
  - `static void main(String[] args)`
  - `byte[] BLZ_DecodePub(byte[] data, String reference)`
  - `byte[] BLZ_EncodePub(byte[] data, boolean arm9, boolean best, String reference)`
- **Utils dependencies:** `FileFunctions`
- **Notes:** Bottom-LZ coder (GBA/DS/3DS). Largest class at 429 lines. Contains `System.exit(0)` calls on bad headers (EXIT macro pattern from CUE's C source) — these need special handling in C#. GARC/LZSS path only has fixtures; BLZ Bottom-LZ encode path returned null at runtime during UPR-5.

#### 19. `Gen2Cmp`
- **Source:** `utils/src/main/java/compressors/Gen2Cmp.java`
- **Public methods:**
  - `static byte[] compress(byte[] uncompressed)`
  - `static byte[] flipBits(byte[] data)`
  - `static byte[] lunarCompress(byte[] uncompressed)`
  - `public static final List<Gen2Compressor> COMPRESSORS`
- **Utils dependencies:** `Gen2Compressor`, `Gen2NullCompressor`, `Gen2FillCompressor`, `Gen2SinglePassCompressor`, `LunarCompressLibrary`
- **Notes:** Facade that dispatches to Lunar DLL on Windows or pure-Java Gen2Compressor implementations otherwise. The `flipBits` method is pure and independently testable.

#### 20. `PPTxtHandler`
- **Source:** `utils/src/main/java/text/PPTxtHandler.java`
- **Public methods:**
  - `static List<String> readTexts(byte[] ds)`
  - `static byte[] saveEntry(byte[] originalData, List<String> text)`
- **Utils dependencies:** `UnicodeParser` (uses `UnicodeParser.class.getResourceAsStream(...)` in static initializer — classloader call, not a data dependency, but the class must be on the classpath)
- **Notes:** Gen 5 text codec. Loads its own table (`Generation5.tbl`) via UnicodeParser's classloader; does not actually call any UnicodeParser methods.

#### 21. `PokeTextData`
- **Source:** `utils/src/main/java/text/PokeTextData.java`
- **Public methods:**
  - `PokeTextData(byte[] data)`
  - `byte[] get()`
  - `void decrypt()`
  - `void encrypt()`
  - `void SetKey(int key)`
  - `int GetKey()`
- **Utils dependencies:** `UnicodeParser` (reads `UnicodeParser.tb[]` in decode path)
- **Notes:** Gen 4 text decode/encrypt. The `tb` field lookup makes this depend on UnicodeParser being initialised.

#### 22. `TextToPoke`
- **Source:** `utils/src/main/java/text/TextToPoke.java`
- **Public methods:**
  - `static byte[] MakeFile(List<String> textarr, boolean compressed)`
- **Utils dependencies:** `UnicodeParser` (reads `UnicodeParser.d` encode map)
- **Notes:** Gen 4 text encoder. No explicit Java import because both classes are in the same package — but a runtime dependency on UnicodeParser.d exists.

### Tier 2 — Two levels of utils dependencies

#### 23. `N3DSTextHandler`
- **Source:** `utils/src/main/java/text/N3DSTextHandler.java`
- **Public methods:**
  - `static List<String> readTexts(byte[] ds, boolean remapChars, int romType)`
  - `static byte[] saveEntry(byte[] originalData, List<String> values, int romType)`
- **Utils dependencies:** `N3DSTextCodes`
- **Notes:** 3DS text codec (231 lines). Uses `N3DSTextCodes.getVariableCode()` and `N3DSTextCodes.getTextVariableCodes()`. No JNA or image dependency; pure byte processing.

---

## 2. Dependency Graph

```
Tier 0 (pure leaves)
════════════════════════════════════════════════════════════
IOFunctions           FileFunctions    FileNameFunctions
     │                     │
     │              ┌──────┴──────┐
     │              │             │
DSCmp    DSDecmp*  BLZCoder   PatchFunctions     (Tier 1)
(leaf)  ══╗(done)
          ╚══ IOFunctions

Gen2Compressor (abstract)
    ├── Gen2NullCompressor
    ├── Gen2FillCompressor
    └── Gen2SinglePassCompressor
                 └──────────────────┐
LunarCompressLibrary (JNA interface)│
                 └──────────────────┤
                              Gen2Cmp (Tier 1)

Gen1Cmp    Gen1Decmp    Gen2Decmp   (leaves — no utils deps)

UnicodeParser (leaf)
    ├── PPTxtHandler  (Tier 1 — classloader ref)
    ├── PokeTextData  (Tier 1 — reads tb[])
    └── TextToPoke    (Tier 1 — reads d{})

N3DSTextCodes (leaf)
    └── N3DSTextHandler (Tier 1)

RandomPointSelector  (leaf, no dependents)
```

*DSDecmp marked as Done (Phase 1).

**Blocking order for porting stories:**
1. Port all Tier-0 leaves first (any order within tier).
2. Port Tier-1 classes only after their Tier-0 dependencies are done.
3. `N3DSTextHandler` (Tier 1 via N3DSTextCodes) can be worked in parallel with the UnicodeParser cluster.

---

## 3. Fixture Coverage Check

The corpus directories present in `utils/fixtures/` are:

| Directory | Cases | Covers |
|---|---|---|
| `IOFunctions/` | 7 | 7 of 10 public IOFunctions methods |
| `DSCmp_LZ10/` | 6 | `DSCmp.compressLZ10` — empty, single, repetitive, random, sawtooth, mixed |
| `DSCmp_LZ11/` | 6 | `DSCmp.compressLZ11` — same suite |
| `DSDecmp_LZ10/` | 8 | `DSDecmp.Decompress` LZ10 path |
| `DSDecmp_LZ11/` | 9 | `DSDecmp.Decompress` LZ11 path |
| `Gen1Decmp/` | 1 | `Gen1Decmp.decompress` — 1×1 all-white only |
| `Gen2Cmp_flipBits/` | 5 | `Gen2Cmp.flipBits` — empty, single, all-zeros, sawtooth, random |
| `Gen2Decmp/` | 7 | `Gen2Decmp.decompress` — single, repetitive, alternating, sawtooth, random, mixed |
| `BLZCoder/` | 4 | `BLZCoder.BLZ_DecodePub(..., "GARC")` only |

### Per-class fixture status

| # | Class | Fixture Status | Notes |
|---|---|---|---|
| 1 | `IOFunctions` | **Sufficient** | 7 cases covering all integer read/write methods; `readFullLong`, `writeFullLong`, and `getCRC32` have no fixtures but are lower-priority helpers |
| 2 | `FileFunctions` | **Needs extending** | No `FileFunctions/` directory in corpus; file I/O methods (read file, write file, read from stream) need at least happy-path cases plus error cases |
| 3 | `FileNameFunctions` | **Needs extending** | No `FileNameFunctions/` directory; `fixFilename` with default extension, with banned extensions, no-op cases needed |
| 4 | `DSCmp` | **Sufficient** | 6 cases each for LZ10 and LZ11: empty, single byte, repetitive, random, sawtooth, 512 mixed |
| 5 | `Gen1Cmp` | **Needs extending** | No `Gen1Cmp/` directory; only 1 Gen1Decmp case exists; compress round-trip requires programmatic `BufferedImage` construction; test infrastructure work needed |
| 6 | `Gen1Decmp` | **Needs extending** | Only 1 fixture (1×1 all-white); missing: multi-tile sprite, repetitive, random, corrupt-header, 1-byte edge case |
| 7 | `Gen2Decmp` | **Sufficient** | 7 cases: single byte, repetitive×2, alternating, sawtooth, random, mixed — covers main decode paths |
| 8 | `LunarCompressLibrary` | **N/A** | JNA/P/Invoke interface; not golden-vector testable; tested indirectly via Gen2Cmp on Windows |
| 9 | `Gen2Compressor` | **N/A** | Abstract base; tested via subclasses |
| 10 | `Gen2NullCompressor` | **Needs extending** | No `Gen2NullCompressor/` directory; needs compress cases: empty, single, short random |
| 11 | `Gen2FillCompressor` | **Needs extending** | No `Gen2FillCompressor/` directory; needs compress cases: repetitive, fill runs, sawtooth |
| 12 | `Gen2SinglePassCompressor` | **Needs extending** | No `Gen2SinglePassCompressor/` directory; large class with many option combinations; needs at least one case per major option |
| 13 | `Gen2Cmp` | **Partially sufficient** | `Gen2Cmp_flipBits/` covers `flipBits` (5 cases); `compress` and `lunarCompress` have no fixtures; `compress` is OS-branching — pure-Java path untestable on Windows without fixture workaround |
| 14 | `BLZCoder` | **Needs extending** | 4 GARC/LZSS decode cases exist; `BLZ_EncodePub` has no fixtures (returned null at runtime during UPR-5); `BLZ_DecodePub` standard Bottom-LZ path also not covered |
| 15 | `PatchFunctions` | **Needs extending** | No `PatchFunctions/` directory; needs: valid IPS patch apply, bad-sig error, RLE record, truncated file |
| 16 | `UnicodeParser` | **Needs extending** | No `UnicodeParser/` fixture dir; existing Java test only checks resource loads; needs encode/decode round-trip cases |
| 17 | `PPTxtHandler` | **Needs extending** | No `PPTxtHandler/` directory; needs readTexts/saveEntry round-trip cases with Gen5 .msg data |
| 18 | `PokeTextData` | **Needs extending** | No `PokeTextData/` directory; needs encrypted/decrypted byte arrays for round-trip |
| 19 | `TextToPoke` | **Needs extending** | No `TextToPoke/` directory; needs Gen4 text encode cases |
| 20 | `N3DSTextCodes` | **Needs extending** | No `N3DSTextCodes/` directory; lookup table results for known romType/variable combinations |
| 21 | `N3DSTextHandler` | **Needs extending** | No `N3DSTextHandler/` directory; needs readTexts/saveEntry round-trip cases with 3DS .msg data |
| 22 | `RandomPointSelector` | **Needs extending** | No `RandomPointSelector/` directory; needs deterministic-seed cases for getRandomPoint |
| 23 | `DSDecmp` *(Done — Phase 1)* | **Sufficient** | 8 LZ10 + 9 LZ11 cases in corpus |

**Summary:**
- **Sufficient:** IOFunctions, DSCmp (LZ10+LZ11), Gen2Decmp, DSDecmp (done)
- **Partially sufficient:** Gen2Cmp (flipBits only), BLZCoder (GARC decode only), Gen1Decmp (1 case only)
- **N/A:** LunarCompressLibrary, Gen2Compressor
- **Needs extending:** FileFunctions, FileNameFunctions, Gen1Cmp, Gen2NullCompressor, Gen2FillCompressor, Gen2SinglePassCompressor, PatchFunctions, UnicodeParser, PPTxtHandler, PokeTextData, TextToPoke, N3DSTextCodes, N3DSTextHandler, RandomPointSelector

---

## 4. Recommended Split into Per-Class Porting Stories

Each porting story follows the UPR 2.4 pattern: extend fixtures if needed, port the class, write differential tests.

Classes that are trivially small (< 15 lines of real logic) or pure data are grouped.

| Story | Classes | Rationale |
|---|---|---|
| **2.4-A** | `IOFunctions` | Leaf; fixtures sufficient; simple byte-order helpers; good "first port" to validate test harness |
| **2.4-B** | `FileFunctions` + `FileNameFunctions` | Both are file-I/O leaves; short (34 + 11 lines); naturally grouped; need fixture extension |
| **2.4-C** | `PatchFunctions` | Depends on FileFunctions; IPS patch logic is self-contained; needs fixtures |
| **2.4-D** | `DSCmp` | Leaf compressor; fixtures sufficient; clean port with no deps |
| **2.4-E** | `DSDecmp` *(already done — UPR-9, skip)* | — |
| **2.4-F** | `Gen1Decmp` | Leaf; fixtures need extending (1 → 5+ cases); stateful decompressor object |
| **2.4-G** | `Gen1Cmp` | Depends on Gen1Decmp (round-trip tests); hardest port due to BufferedImage; needs fixture infra design |
| **2.4-H** | `Gen2Decmp` | Leaf; fixtures sufficient; static decompressor |
| **2.4-I** | `Gen2Compressor` + `Gen2NullCompressor` + `Gen2FillCompressor` | Abstract base + two simple concrete implementations; natural unit; needs fixture extension |
| **2.4-J** | `Gen2SinglePassCompressor` | Largest Gen2 strategy; complex option combinations; warrants its own story |
| **2.4-K** | `LunarCompressLibrary` + `Gen2Cmp` | JNA wrapper + facade depend on each other at runtime; Windows-only path; port together |
| **2.4-L** | `BLZCoder` | Largest class (429 lines); depends on FileFunctions; BLZ Bottom-LZ encode path not yet fixtured |
| **2.4-M** | `UnicodeParser` | Leaf data class; needs encode/decode fixtures; prerequisite for PPTxtHandler, PokeTextData, TextToPoke |
| **2.4-N** | `PPTxtHandler` | Gen 5 text codec; depends on UnicodeParser classloader; needs Gen5 round-trip fixtures |
| **2.4-O** | `PokeTextData` + `TextToPoke` | Gen 4 decode + encode pair; both depend on UnicodeParser; natural round-trip grouping |
| **2.4-P** | `N3DSTextCodes` | Leaf lookup table; short; prerequisite for N3DSTextHandler |
| **2.4-Q** | `N3DSTextHandler` | 3DS text codec (231 lines); depends on N3DSTextCodes; needs .msg test data |
| **2.4-R** | `RandomPointSelector` | Standalone; pure numerical; needs deterministic-seed fixtures |

**Estimated story count: 17 stories** (2.4-A through 2.4-R, skipping DSDecmp which is done).

**Blocking constraints for scheduling:**
- 2.4-C requires 2.4-B (FileFunctions) to be complete.
- 2.4-G requires 2.4-F (Gen1Decmp) for round-trip tests.
- 2.4-I, 2.4-J, 2.4-K require 2.4-H is not strictly required — but logically Gen2Decmp validates Gen2Cmp output; recommend ordering 2.4-H before 2.4-K.
- 2.4-K requires 2.4-I and 2.4-J.
- 2.4-L requires 2.4-B.
- 2.4-N requires 2.4-M.
- 2.4-O requires 2.4-M.
- 2.4-Q requires 2.4-P.

All Tier-0 stories (2.4-A, 2.4-B, 2.4-D, 2.4-F, 2.4-H, 2.4-M, 2.4-P, 2.4-R) can be started in parallel.

---

## Resume Point

This document was produced on branch `upr/UPR-12`, base commit `88d92e38`. An agent resuming work on Phase 2 should:

1. Read `docs/upr/UPR-7-adr-target-language.md` to confirm the target language (C#) and skeleton structure.
2. Read `docs/upr/UPR-10-porting-notes.md` for the list of Java→C# semantic hazards (signed bytes, `System.exit`, JNA→P/Invoke, OS branching).
3. Use this document as the direct input for creating per-class porting stories (2.4-A through 2.4-R).
4. For each story, extend `utils/fixtures/<ClassName>/` via `utils/src/test/java/fixtures/GoldenVectorHarness.java` before writing the port.
5. Priority order for stories with no blocking constraints: start with 2.4-A (IOFunctions) and 2.4-D (DSCmp) as the simplest, highest-fixture-coverage classes.
