# UPR-4: Test-Coverage Map — `utils` Module

**Repo:** https://github.com/Timvanderwal504/universal-pokemon-randomizer-fvx  
**Branch:** `upr/UPR-4`  
**Base commit:** `4d68f8f86e094fcc6e1067539c0c6d2ca1c6d8a2`  
**Date produced:** 2026-05-29  
**Model:** claude-sonnet-4-6  
**Related UPR issues:** UPR-1 (parent epic), UPR-3 (baseline), UPR-6 (characterization, consumer of this map)

---

## 1. Class Inventory

The `utils` subproject (`utils/src/main/java/`) contains **four packages**:

### Package `compressors`

| Class | Public API (signatures) |
|---|---|
| `Gen1Cmp` | `static byte[] compress(BufferedImage bp1, BufferedImage bp2)`; `Gen1Cmp(BufferedImage bp1, BufferedImage bp2)`; `byte[] compressUsingModeAndOrder(int mode, boolean order)` |
| `Gen1Decmp` | `Gen1Decmp(byte[] input, int baseOffset)`; `void decompress()`; `byte[] getData()`; `int getWidth()`; `int getHeight()`; `int getCompressedLength()` |
| `Gen2Cmp` | `static byte[] compress(byte[] uncompressed)`; `static byte[] flipBits(byte[] data)`; `static byte[] lunarCompress(byte[] uncompressed)` |
| `Gen2Decmp` | `static byte[] decompress(byte[] data, int offset)`; `static int lengthOfCompressed(byte[] data, int offset)` |
| `DSCmp` | `static byte[] compressLZ10(byte[] decompressed)`; `static byte[] compressLZ11(byte[] decompressed)` |
| `DSDecmp` | `static byte[] Decompress(byte[] data)`; `static byte[] Decompress(byte[] data, int offset)` |
| `LunarCompressLibrary` | JNA interface; `int LunarVersion()`; `int LunarDecompress(...)`; `int LunarRecompress(...)` |

**Inner/support classes within `compressors`:**

| Class | Note |
|---|---|
| `Gen1Cmp$BitWriteStream` | private — bit-level output used by Gen1Cmp |
| `Gen1Cmp$BitReadStream` | private — bit-level input used by Gen1Cmp |
| `Gen1Decmp$BitStream` | private — bit-level input used by Gen1Decmp |

### Package `compressors.gen2`

| Class | Public API |
|---|---|
| `Gen2Compressor` | abstract; `abstract byte[] compress(byte[] uncompressed, byte[] bitFlipped)` |
| `Gen2NullCompressor` | `byte[] compress(byte[] uncompressed, byte[] bitFlipped)` — direct-copy only |
| `Gen2FillCompressor` | `byte[] compress(byte[] uncompressed, byte[] bitFlipped)` — fill commands only |
| `Gen2SinglePassCompressor` | `byte[] compress(byte[] uncompressed, byte[] bitFlipped)`; many option combinations via `ALL_OPTIONS` |

### Package `cuecompressors`

| Class | Public API |
|---|---|
| `BLZCoder` | `static void main(String[] args)`; `byte[] BLZ_DecodePub(byte[] data, String reference)`; `byte[] BLZ_EncodePub(byte[] data, boolean arm9, boolean best, String reference)` |

### Package `filefunctions`

| Class | Public API |
|---|---|
| `FileFunctions` | `static byte[] readFileFullyIntoBuffer(String filename)`; `static byte[] readFullyIntoBuffer(InputStream in, int bytes)`; `static int read2ByteBigEndianIntFromFile(RandomAccessFile file, long offset)`; `static int readBigEndianIntFromFile(...)`; `static int readIntFromFile(...)`; `static void writeBytesToFile(String filename, byte[] data)` |
| `FileNameFunctions` | `static File fixFilename(File original, String defaultExtension)`; `static File fixFilename(File original, String defaultExtension, List<String> bannedExtensions)` |
| `IOFunctions` | `static long readFullLong(byte[] data, int offset)`; `static int readFullInt(...)`; `static int readFullIntBigEndian(...)`; `static int read2ByteIntBigEndian(...)`; `static int read2ByteInt(...)`; `static void write2ByteInt(...)`; `static void writeFullInt(...)`; `static void writeFullIntBigEndian(...)`; `static void writeFullLong(...)`; `static long getCRC32(byte[] data)` |
| `PatchFunctions` | `static void applyPatch(byte[] rom, String path)` |

### Package `text`

| Class | Public API |
|---|---|
| `UnicodeParser` | Static initializer loads Gen4 table; `String[] tb`; `Map<String, Integer> d` |
| `PPTxtHandler` | `static List<String> readTexts(byte[] ds)`; `static byte[] saveEntry(byte[] originalData, List<String> text)` |
| `N3DSTextHandler` | `static List<String> readTexts(byte[] ds, boolean remapChars, int romType)`; `static byte[] saveEntry(byte[] originalData, List<String> values, int romType)` |
| `N3DSTextCodes` | `static Map<Integer,String> getTextVariableCodes(int romType)`; `static int getVariableCode(String name, int romType)` |
| `PokeTextData` | `PokeTextData(byte[] data)`; `byte[] get()`; `void decrypt()`; `void encrypt()`; `void SetKey(int key)`; `int GetKey()` |
| `TextToPoke` | `static byte[] MakeFile(List<String> textarr, boolean compressed)` |

### Package `randompoint`

| Class | Public API |
|---|---|
| `RandomPointSelector` | `RandomPointSelector(Random, int dimensions, double[] lower, double[] upper, Function<double[], Double> weight)`; `double[] getRandomPoint()`; `double getRelativeMeasureUnderCurve()`; `Random getRandom()`; `void setRandom(Random)` |

---

## 2. Coverage Table

JaCoCo was run via `.\gradlew :utils:test :utils:jacocoTestReport` using JaCoCo 0.8.13 (required for JDK 25 / class file major version 69). Two tests exist in the utils test suite, 1 of which intentionally fails (Gen1CmpTest.dummyTest — always fails by design to mark commented-out tests). The UnicodeParserTest.canReadTableResource test passes.

All line/branch percentages are from `utils/build/reports/jacoco/test/jacocoTestReport.xml`. "0%" means the class was never entered at all during the test run.

### Compression classes

| Class | Classification | Line % | Branch % | Tests exercising it |
|---|---|---|---|---|
| `compressors.Gen1Cmp` | **UNTESTED** | 0% | 0% | None (Gen1CmpTest fully commented out) |
| `compressors.Gen1Cmp$BitWriteStream` | **UNTESTED** | 0% | 0% | None |
| `compressors.Gen1Cmp$BitReadStream` | **UNTESTED** | 0% | 0% | None |
| `compressors.Gen1Decmp` | **UNTESTED** | 0% | 0% | None (Gen1CmpTest fully commented out) |
| `compressors.Gen1Decmp$BitStream` | **UNTESTED** | 0% | 0% | None |
| `compressors.Gen2Cmp` | **UNTESTED** | 0% | 0% | None (Gen2CmpTest fully commented out) |
| `compressors.Gen2Decmp` | **UNTESTED** | 0% | 0% | None |
| `compressors.DSCmp` | **UNTESTED** | 0% | 0% | None |
| `compressors.DSDecmp` | **UNTESTED** | 0% | 0% | None |
| `compressors.LunarCompressLibrary` | **UNTESTED** | 0% | N/A | None |
| `compressors.gen2.Gen2Compressor` | **UNTESTED** | 0% | 0% | None |
| `compressors.gen2.Gen2NullCompressor` | **UNTESTED** | 0% | 0% | None |
| `compressors.gen2.Gen2FillCompressor` | **UNTESTED** | 0% | 0% | None |
| `compressors.gen2.Gen2SinglePassCompressor` | **UNTESTED** | 0% | 0% | None |

### File-function classes

| Class | Classification | Line % | Branch % | Tests exercising it |
|---|---|---|---|---|
| `filefunctions.FileFunctions` | **UNTESTED** | 0% | 0% | None |
| `filefunctions.FileNameFunctions` | **UNTESTED** | 0% | 0% | None |
| `filefunctions.IOFunctions` | **UNTESTED** | 0% | N/A | None |
| `filefunctions.PatchFunctions` | **UNTESTED** | 0% | 0% | None |

### Text-handler classes

| Class | Classification | Line % | Branch % | Tests exercising it |
|---|---|---|---|---|
| `text.UnicodeParser` | **THIN** | 83.3% (15/18) | 62.5% | `UnicodeParserTest.canReadTableResource` |
| `text.PPTxtHandler` | **UNTESTED** | 0% | 0% | None |
| `text.N3DSTextHandler` | **UNTESTED** | 0% | 0% | None |
| `text.N3DSTextCodes` | **UNTESTED** | 0% | 0% | None |
| `text.PokeTextData` | **UNTESTED** | 0% | 0% | None |
| `text.TextToPoke` | **UNTESTED** | 0% | 0% | None |

### Other utility classes

| Class | Classification | Line % | Branch % | Tests exercising it |
|---|---|---|---|---|
| `randompoint.RandomPointSelector` | **UNTESTED** | 0% | 0% | None |
| `cuecompressors.BLZCoder` | **UNTESTED** | 0% | 0% | None |

**Classification key:**
- **WELL** = line coverage >= 80% AND branch coverage >= 70%
- **THIN** = some coverage but below well thresholds, or coverage of only a trivial happy path
- **UNTESTED** = 0% line coverage

---

## 3. Gap List

The following classes are the direct input to UPR-6 (0.4 Characterization tests). Every class except `UnicodeParser` has zero test protection.

### Priority 1 — Phase 1 compression targets (must characterize before any refactor)

1. `compressors.Gen1Cmp` — Gen 1 image compressor; 0% covered; 119 lines, 76 branches unprotected
2. `compressors.Gen1Decmp` — Gen 1 image decompressor; 0% covered; 145 lines, 58 branches unprotected
3. `compressors.Gen2Cmp` — Gen 2 compressor facade (selects best algorithm, uses Lunar DLL on Windows); 0% covered
4. `compressors.Gen2Decmp` — Gen 2 decompressor; 0% covered; 71 lines, 28 branches
5. `compressors.gen2.Gen2Compressor` — abstract base for Gen2 compressor strategies; 0% covered
6. `compressors.gen2.Gen2NullCompressor` — direct-copy-only compressor; 0% covered
7. `compressors.gen2.Gen2FillCompressor` — fill-based compressor; 0% covered
8. `compressors.gen2.Gen2SinglePassCompressor` — main single-pass strategy (many option combinations); 0% covered
9. `compressors.DSCmp` — DS LZ10/LZ11 compressor; 0% covered; 108 lines, 36 branches
10. `compressors.DSDecmp` — DS LZ10/LZ11 decompressor; 0% covered; 88 lines, 48 branches
11. `cuecompressors.BLZCoder` — BLZ (bottom-LZ) coder for GBA/DS/3DS; 0% covered; 429 lines, largest class in module

### Priority 2 — Text/encoding utilities

12. `text.UnicodeParser` — THIN; the one existing test only checks that the resource file can be loaded; no I/O round-trip, no encoding/decoding logic exercised; 3 uncovered lines, 1 uncovered branch
13. `text.PPTxtHandler` — Gen 5 text codec; 0% covered
14. `text.N3DSTextHandler` — 3DS text codec; 0% covered; 231 lines
15. `text.N3DSTextCodes` — 3DS text variable code tables; 0% covered; 166 lines
16. `text.PokeTextData` — Gen 4 text decode/encrypt; 0% covered; 105 lines
17. `text.TextToPoke` — Gen 4 text encode; 0% covered; 119 lines

### Priority 3 — File and I/O utilities

18. `filefunctions.FileFunctions` — file read/write; 0% covered; 34 lines
19. `filefunctions.FileNameFunctions` — filename extension fixing; 0% covered; 11 lines
20. `filefunctions.IOFunctions` — byte-array read/write helpers; 0% covered; 31 lines
21. `filefunctions.PatchFunctions` — IPS patch application; 0% covered; 44 lines

### Priority 4 — Probabilistic utility

22. `randompoint.RandomPointSelector` — Monte Carlo point selection; 0% covered; 41 lines

---

## 4. Compression Deep-Dive

The compression utilities are the Phase 1 refactoring target. All have 0% test coverage.

### 4.1 Gen 1 Image Compression (`Gen1Cmp` / `Gen1Decmp`)

#### Purpose
Implements the Pokémon Gen 1 sprite compression algorithm (RLE + delta encoding + XOR, operating on two bitplanes). Gen1Cmp tries all 6 mode/order combinations and returns the shortest result.

#### Signatures
```java
// Compressor
public static byte[] compress(BufferedImage bitplane1Image, BufferedImage bitplane2Image)
public Gen1Cmp(BufferedImage bitplane1Image, BufferedImage bitplane2Image)
public byte[] compressUsingModeAndOrder(int mode, boolean order)  // mode 0-2, order false/true

// Decompressor
public Gen1Decmp(byte[] input, int baseOffset)
public void decompress()
public byte[] getData()
public int getWidth()
public int getHeight()
public int getCompressedLength()
```

#### Edge-case behavior (from source analysis — none observed, 0% coverage)

- **Mismatched bitplane dimensions**: `Gen1Cmp` constructor throws `IllegalArgumentException` with a message listing both sizes.
- **Image too large (>15 tiles in either dimension)**: `Gen1Cmp` constructor throws `IllegalArgumentException`.
- **Invalid mode argument**: `compressUsingModeAndOrder` throws `IllegalArgumentException` for mode outside 0-2.
- **Empty input / 0-byte ROM offset**: The decompressor reads dimensions from the first byte; a zero-length input would cause `ArrayIndexOutOfBoundsException` in `BitStream.next()` when `offset >= data.length` returns -1, which is then used as a bit value — corrupt but no explicit guard.
- **1-byte input**: Same as above; `sizex = 0`, `sizey = 0`, resulting in `size = 0`; `fillram` allocates a 0-element array; behaviour is undefined/silent corruption.
- **Repetitive data (all-zero bitplane)**: Compressor delta-encodes to all-zero, then RLE encodes efficiently; decompressor read_rle_chunk loop handles this.
- **Incompressible data**: Compressor falls back to data packets; the "shortest of 6 modes" selection minimizes output regardless.

**Key structural observation:** `Gen1CmpTest` and `Gen2CmpTest` are entirely commented out because they depend on `GBCImage`, which lives in the `romio` module (outside `utils`). The `TODO` in both files says to rewrite without that dependency. This is the primary gap — the round-trip compress/decompress tests existed conceptually but cannot compile in the current module structure.

### 4.2 Gen 2 Image Compression (`Gen2Cmp` / `Gen2Decmp`)

#### Purpose
Implements the Pokémon Gen 2 sprite compression algorithm (LC_LZ3 / LZ3). On Windows, `Gen2Cmp.compress()` delegates to the Lunar Compress DLL (much faster). On other platforms it tries all registered `Gen2Compressor` instances and picks the shortest output. `Gen2Decmp` is a pure-Java decompressor.

#### Signatures
```java
// Compressor facade
public static byte[] compress(byte[] uncompressed)       // OS-aware: uses DLL on Windows
public static byte[] lunarCompress(byte[] uncompressed)  // always calls Lunar DLL
public static byte[] flipBits(byte[] data)               // bit-reversal table lookup

// Decompressor (static factory)
public static byte[] decompress(byte[] data, int offset)
public static int lengthOfCompressed(byte[] data, int offset)
```

#### Edge-case behavior (from source analysis)

- **Empty array (`compress(new byte[0])`)**: On Windows calls `lunarCompress`; DLL behavior for 0-length is unspecified by source. On non-Windows, all compressors allocate `uncompressed.length * 2 = 0` bytes, write only the 0xFF terminator — `Gen2NullCompressor` would produce a 1-byte output `{0xFF}`.
- **1-byte input**: `Gen2NullCompressor` would write a 1-byte header `{0x00}` (count=1), then the byte, then `{0xFF}` terminator — 3 bytes total.
- **Repetitive data (e.g. all-zero)**: `Gen2FillCompressor` and `Gen2SinglePassCompressor` would exploit ZERO_FILL/BYTE_FILL heavily, producing very short output.
- **Incompressible data**: `Gen2NullCompressor` always produces ~1.03× expansion (direct copy + header bytes + terminator). This is the fallback.
- **`flipBits` correctness**: Uses a pre-computed 256-entry lookup table. Every byte `b` maps to its bit-reversed value. The table is an `int[]` to avoid Java signed-byte issues.
- **Decompressor end-of-stream**: `Gen2Decmp.decompress()` exits when it encounters `0xFF` (LZ_END). The output buffer starts at 0x1000 bytes and doubles as needed via `resizeOutput()`.
- **OS branch in `Gen2Cmp.compress`**: `System.getProperty("os.name").startsWith("Windows")` determines DLL usage. The pure-Java path is never tested on the current Windows CI environment.

### 4.3 DS Compression (`DSCmp` / `DSDecmp`)

#### Purpose
LZ10 and LZ11 compression/decompression for Nintendo DS games. Used for Gen 4/5 ROMs. Modified from DSDecmp-Java and Tinke.

#### Signatures
```java
// Compressor
public static byte[] compressLZ10(byte[] decompressed)
public static byte[] compressLZ11(byte[] decompressed)

// Decompressor
public static byte[] Decompress(byte[] data)
public static byte[] Decompress(byte[] data, int offset)
```

#### Edge-case behavior (from source analysis)

- **Empty array**: `compressLZ10(new byte[0])` / `compressLZ11(new byte[0])` — the while loop body (`while (curIn < decompressed.length)`) never executes; the result is just the 4-byte header `{0x10, 0, 0, 0}` or `{0x11, 0, 0, 0}`.
- **Large input (> 0xFFFFFF bytes)**: Both compressors write an 8-byte header (`0x10/0x11` + three zero bytes + 4-byte little-endian length). The decompressor detects this pattern (length field == 0) and reads the next 4 bytes via `IOFunctions.readFullIntBigEndian`. Note: the comment says big-endian but the compressor writes little-endian — potential big-endian/little-endian mismatch bug.
- **Incompressible data**: Falls back to literal byte output per the occurrence search returning length < 3.
- **Corrupt header (`data[offset] != 0x10` and `!= 0x11`)**: `DSDecmp.Decompress` throws `IllegalArgumentException`.

### 4.4 BLZ Coder (`BLZCoder`)

#### Purpose
Bottom-LZ coding for Nintendo GBA/DS/3DS overlay files. Ported from CUE's `blz.c` and Kaphotics' pk3DS. Provides both decode and encode with a separate LZSS path for GARC files (3DS).

#### Signatures
```java
public byte[] BLZ_DecodePub(byte[] data, String reference)  // reference "GARC" uses LZSS path
public byte[] BLZ_EncodePub(byte[] data, boolean arm9, boolean best, String reference)
```

#### Edge-case behavior (from source analysis)

- **Not-coded file (inc_len < 1)**: `BLZ_Decode` prints a WARNING and returns the data as-is (dec_len = pak_len, no encoded section).
- **Bad header**: Multiple explicit checks with `EXIT(...)` calls (`pak_len < 8`, `hdr_len` out of range 8-11, `pak_len <= hdr_len`). `EXIT` calls `System.exit(0)` — side-effectful even in library use.
- **ARM9 mode**: Subtracts 0x4000 from the end-of-raw pointer before inversion, encoding only the last portion of the file.
- **GARC/LZSS path**: Uses a ByteBuffer-based implementation with LZ11-style variable-length commands.

---

## 5. JaCoCo Report

### Report location
Committed to: `docs/upr/jacoco-utils/index.html`  
Full path in repo: `docs/upr/jacoco-utils/`

### How to regenerate
```powershell
.\gradlew :utils:test :utils:jacocoTestReport
```
HTML output: `utils/build/reports/jacoco/test/html/index.html`

### Test run summary
```
> Task :utils:test
Gen1CmpTest > dummyTest() FAILED
    org.opentest4j.AssertionFailedError at Gen1CmpTest.java:28
2 tests completed, 1 failed
BUILD SUCCESSFUL
```

The failing test (`Gen1CmpTest.dummyTest`) is an intentional placeholder — it always asserts `"foobar".equals("foobaz")` which is always false. The test file states: "Does nothing, is just here so gradle doesn't complain about these test files being 'empty'." The root build sets `ignoreFailures = true` so the build still succeeds.

The passing test (`UnicodeParserTest.canReadTableResource`) touches `UnicodeParser` static initializer (reads `Generation4.tbl` from resources) and asserts `UnicodeParser.tb != null`, producing 83.3% line and 62.5% branch coverage of that one class.

### Coverage summary (from XML report)

| Package | Classes | Lines covered | Lines total | Line % | Notes |
|---|---|---|---|---|---|
| `compressors` | 10 | 0 | 551 | 0% | All commented-out tests |
| `compressors.gen2` | 9 | 0 | 250 | 0% | All commented-out tests |
| `cuecompressors` | 4 | 0 | 441 | 0% | No test class |
| `filefunctions` | 4 | 0 | 120 | 0% | No test class |
| `randompoint` | 2 | 0 | 46 | 0% | No test class |
| `text` | 10 | 15 | 872 | 1.7% | Only UnicodeParser exercised |

---

## Resume Point

**Story UPR-4 is complete.** All deliverables have been produced:

1. `docs/upr/UPR-4-coverage-map.md` — this file, committed on branch `upr/UPR-4`.
2. `docs/upr/jacoco-utils/` — JaCoCo HTML report committed. Entry point: `docs/upr/jacoco-utils/index.html`.
3. `utils/build.gradle.kts` — JaCoCo plugin (`jacoco`, toolVersion 0.8.13) added and committed.

**Key findings for UPR-6:**

- The entire `utils` module (34 classes, ~2280 executable lines) is effectively untested.
- The only passing test (`UnicodeParserTest`) only verifies that a resource file loads — not that the class works correctly.
- Both `Gen1CmpTest` and `Gen2CmpTest` have all real tests commented out with a `TODO` to rewrite without the `GBCImage` dependency from `romio`.
- **Phase 1 target** (`Gen1Cmp`, `Gen1Decmp`, `Gen2Cmp`, `Gen2Decmp`) has 0% coverage across ~400 lines and ~180 branches.
- To write real characterization tests for Gen1/Gen2 compression without `GBCImage`, test data must be raw byte arrays (bitplane data), not PNG images. The `Gen2Decmp`/`Gen2Cmp` path is more amenable as it works on raw byte slices. The `Gen1Cmp` path requires `BufferedImage` inputs — tests would need to construct minimal `BufferedImage` objects programmatically.
- `Gen2Cmp.compress()` takes a Windows-only code path (Lunar DLL) on the current environment; the pure-Java fallback (which exercises all the `Gen2Compressor` implementations) is never reached in tests.
