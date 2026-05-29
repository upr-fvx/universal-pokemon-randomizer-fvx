# UPR-5 — Golden-Vector Fixture Corpus

**Repo:** https://github.com/TimvanderWal504/universal-pokemon-randomizer-fvx  
**Branch:** upr/UPR-5  
**Commit SHA:** (set at commit time)  
**Related issues:** UPR-5 (producer), UPR-8 (consumer — C# port differential tests)  
**Date produced:** 2026-05-29  
**Produced by:** claude-sonnet-4-6 (upr-worker subagent + orchestrator completion)

---

## Directory layout & naming

```
utils/fixtures/
├── README.md              ← this file
├── manifest.tsv           ← machine-readable index of every fixture
├── BLZCoder/
│   ├── <case>.in
│   └── <case>.out
├── DSCmp_LZ10/
├── DSCmp_LZ11/
├── DSDecmp_LZ10/
├── DSDecmp_LZ11/
├── Gen1Decmp/
├── Gen2Cmp_flipBits/
├── Gen2Decmp/
└── IOFunctions/
```

Each sub-directory corresponds to one Java function or codec.  
Within each directory every test case is two files:

| File | Contents |
|---|---|
| `<case>.in` | Raw binary input fed to the function |
| `<case>.out` | Expected raw binary output the function must produce |

File names use lowercase snake_case and contain no spaces.

---

## Manifest schema

`manifest.tsv` is a UTF-8 tab-separated file with a header row:

| Column | Type | Meaning |
|---|---|---|
| `function` | string | Directory name / codec identifier (e.g. `DSDecmp_LZ10`) |
| `case` | string | Case name without extension (e.g. `repetitive_256`) |
| `description` | string | Human-readable description of what the input stresses |
| `in_path` | string | Repo-root-relative POSIX path to the `.in` file |
| `out_path` | string | Repo-root-relative POSIX path to the `.out` file |

**Worked example entry:**

```
function        case             description                              in_path                              out_path
DSDecmp_LZ10    repetitive_256   256 bytes of 0xAA (highly repetitive)   fixtures/DSDecmp_LZ10/repetitive_256.in   fixtures/DSDecmp_LZ10/repetitive_256.out
```

---

## How to consume

1. Read `<case>.in` as raw bytes.
2. Pass those bytes to your port's implementation of the function named by the directory.
3. Compare the returned bytes byte-for-byte against `<case>.out`.
4. The test passes if and only if the two byte arrays are identical (exact length and every byte equal).

**Decompressor fixtures** (DSDecmp_LZ10, DSDecmp_LZ11, Gen1Decmp, Gen2Decmp, BLZCoder):  
`.in` = compressed stream, `.out` = expected decompressed plaintext.

**Compressor fixtures** (DSCmp_LZ10, DSCmp_LZ11, Gen2Cmp_flipBits):  
`.in` = plaintext, `.out` = expected compressed output.

**IOFunctions fixtures** (IOFunctions):  
`.in` = input encoding (big-endian 4-byte int for read functions; the integer value for write functions), `.out` = result encoding. See individual case descriptions in manifest.tsv for exact semantics.

---

## Fixture inventory

| Directory | Cases | What it stresses |
|---|---|---|
| `DSDecmp_LZ10` | 8 | DS LZ10 decompressor — 8-byte, 256-byte, 4096-byte repetitive/random/alternating/sawtooth/mixed inputs; `.in` = LZ10-compressed, `.out` = plain |
| `DSDecmp_LZ11` | 9 | DS LZ11 decompressor — same suite + 65536-byte case to exercise extended length codes (type `0x00`/`0x10` prefixes) |
| `DSCmp_LZ10` | 6 | DS LZ10 compressor — empty, single byte, 256 repetitive/random/sawtooth, 512 mixed |
| `DSCmp_LZ11` | 6 | DS LZ11 compressor — same suite |
| `Gen2Decmp` | 7 | GBC LC_LZ3 decompressor — 1-byte, 32-byte repetitive/alternating/sawtooth, 64 random, 128 mixed; compressed data built with literal-only encoder to avoid Lunar DLL dependency |
| `Gen2Cmp_flipBits` | 5 | `Gen2Cmp.flipBits` pure byte bijection — empty, single, all-zeros, sawtooth, random |
| `Gen1Decmp` | 1 | GB/GBC sprite decompressor — 1×1-tile all-white image; compressed stream built by programmatic bit-writer matching Gen1Cmp's RLE encoding |
| `BLZCoder` | 4 | BLZCoder GARC/LZSS path (`BLZ_DecodePub(..., "GARC")`) — single byte, 128 repetitive, 256 random, 512 mixed; BLZ Bottom-LZ path skipped at runtime (encoder returned null) |
| `IOFunctions` | 7 | `read2ByteInt`, `read2ByteIntBigEndian`, `readFullInt`, `readFullIntBigEndian`, `write2ByteInt`, `writeFullInt`, `writeFullIntBigEndian` — canonical endian conversion cases |

**Total: 53 cases, 106 files** (not counting manifest.tsv and README.md).

---

## Regenerate / extend

### Re-run deterministically

```
# From repo root (Windows)
.\gradlew :utils:test --tests "fixtures.GoldenVectorHarness"
```

The harness uses a fixed seed (`0xDEADBEEFL`) for all PRNG calls.  
Re-running overwrites all fixture files in place and regenerates `manifest.tsv`.  
The output is fully deterministic: the same JDK + source produces byte-identical files.

### Add new cases

1. Open `utils/src/test/java/fixtures/GoldenVectorHarness.java`.
2. Add a `new Case(...)` entry in the appropriate `generate*Fixtures` method.
3. Re-run the harness — the new `.in`/`.out` files and a fresh `manifest.tsv` are produced automatically.
4. Commit the new files.

### Add a new function

1. Add a `generateMyFunctionFixtures(Path root)` method following the existing pattern.
2. Call it from `run()` before `writeManifest(root)`.
3. Re-run the harness.

---

## Known limitations

- **BLZ Bottom-LZ (`BLZ_EncodePub`)**: returned `null` at runtime; BLZ encode→decode round-trip cases were skipped. Only GARC/LZSS cases are present.
- **Gen1Decmp**: only one fixture (1×1 all-white). Larger sprites require `BufferedImage` which is not headless-safe; extending coverage requires a headless-compatible image fixture builder.
- **DSDecmp LZ10/LZ11 known bug**: the Java implementation has an inner-loop over-read bug on inputs whose compressed form doesn't over-fill by a whole back-reference. Fixtures work around this by using `DSCmp` to produce the `.in` files (known-correct compressor) rather than calling the buggy decompressor to produce `.out` files. A correct port must handle all sizes cleanly.
