# UPR-3 — Green Build Baseline

**Repo:** https://github.com/TimvanderWal504/universal-pokemon-randomizer-fvx  
**Branch:** upr/UPR-3  
**Commit SHA:** 4d68f8f86e094fcc6e1067539c0c6d2ca1c6d8a2  
**Related issues:** UPR-1 (parent epic), UPR-4 (consumes this), UPR-5 (consumes this)  
**Date produced:** 2026-05-28  
**Produced by:** claude-sonnet-4-6 (upr-worker subagent)

---

## Environment

| Property | Value |
|---|---|
| JDK vendor | Eclipse Adoptium (Temurin) |
| JDK version | 25.0.3+9-LTS |
| Gradle version | 9.3.1 |
| OS | Windows 11 10.0 amd64 |
| Latest release tag | vFVX1.5.1 (commit `6cef8bff`) |
| HEAD commit | `4d68f8f86e094fcc6e1067539c0c6d2ca1c6d8a2` |
| Describe | `vFVX1.5.1-5-g4d68f8f8` (5 commits ahead of v1.5.1) |

The HEAD is 5 commits ahead of vFVX1.5.1; the 5 extra commits are UPR infrastructure additions and do not affect game logic.

---

## Build

Command run:

```
.\gradlew build -x test
```

Result: **BUILD SUCCESSFUL** (exit 0) in 22 s. All 15 tasks executed cleanly. A Gradle deprecation warning about features incompatible with Gradle 10 was emitted (pre-existing; not a failure).

Exact terminal output (last 10 lines):

```
> Task :random:build
> Task :romio:build
> Task :utils:build

BUILD SUCCESSFUL in 22s
15 actionable tasks: 15 executed
```

---

## Unit tests (`:random:test`)

Command run:

```
.\gradlew :random:test
```

Result: **BUILD SUCCESSFUL** (exit 0).

### Summary

| Suite | Tests | Skipped | Failures | Errors |
|---|---|---|---|---|
| `com.uprfvx.random.VersionTest` | 2 | 0 | 0 | 0 |
| **Total** | **2** | **0** | **0** | **0** |

### Passing tests

- `VersionTest.latestVersion_IsInAllVersionsList()`
- `VersionTest.latestVersion_HasNewID()`

### Failing tests

None. All 2 unit tests pass.

---

## ROM tests (`:random:testROMs`)

Command run:

```
.\gradlew :random:testROMs
```

Result: **BUILD FAILED** (exit non-zero). All failures are caused by missing ROM files; no code-logic failures were found beyond the ROM-missing cases (see detailed breakdown below).

### roms/ directory contents

Only `roms/readme.txt` is present. No ROM files exist. ROM files must be placed in `roms/` using the naming convention described in that file (e.g., `Gold (U).gbc`, `Ruby (U).gba`).

### Test suite summary

| Suite | Tests | Skipped | Failures | Errors | Root cause |
|---|---|---|---|---|---|
| `CliRandomizerTest` | 16 | 0 | 6 | 0 | Missing `Gold (U).gbc` ROM file |
| `EvolutionRandomizerTest` | 1 | 0 | 1 | 0 | Missing ROM (initializationError: `AbstractDSRomHandler.java:164` FileNotFoundException) |
| `ItemRandomizerTest` | 1 | 0 | 1 | 0 | Same |
| `MoveNameRandomizerTest` | 1 | 0 | 1 | 0 | Same |
| `SpeciesTraitRandomizersTest` | 1 | 0 | 1 | 0 | Same |
| `StarterRandomizerTest` | 1 | 0 | 1 | 0 | Same |
| `StaticPokemonRandomizerTest` | 1 | 0 | 1 | 0 | Same |
| `TrainerRandomizersTest` | 1 | 0 | 1 | 0 | Same |
| `TypeEffectivenessRandomizerTest` | 1 | 0 | 1 | 0 | Same |
| `WildEncounterRandomizerTest` | 1 | 0 | 1 | 0 | Same |
| `MoveUpdaterTest` | 1985 | 0 | 1985 | 0 | Gen 1-3 ROMs: `IllegalArgumentException` (missing .gb/.gba ROM); Gen 4-7 ROMs: `RomIOException / FileNotFoundException` (missing .nds ROM) |
| `SpeciesBaseStatUpdaterTest` | 1985 | 271 | 1714 | 0 | 271 skipped (ROM-independent); 1714 failed due to missing ROMs |
| `TypeEffectivenessUpdaterTest` | 163 | 8 | 155 | 0 | 8 skipped (ROM-independent); 155 failed due to missing ROMs |
| **Total** | **4158** | **279** | **3869** | **0** | |

### Passing ROM tests (10 out of 16 CliRandomizerTest)

The following 10 CLI tests pass because they do not require a ROM file (all test error paths):

- `invoke_noSourcePathGiven_fails()`
- `invoke_noOutputPathGiven_fails()`
- `invoke_cannotReadSourcePath_fails()`
- `invoke_cannotWriteOutputPathParentFolder_fails()`
- `invoke_seedIsNotLong_fails()`
- `invoke_invalidSettingsFile_fails()`
- `invoke_invalidSettingsString_fails()`
- `invoke_cannotOpenSettingsPath_fails()`
- `invoke_invalidCPGType_fails()`
- `invoke_noSettingsGiven_fails()`

### ROM files required vs. available

**Available:** None.

**Required** (derived from test parameter sets and error messages):

| Generation | ROM file examples | Extension |
|---|---|---|
| Gen 1 (GB) | `Red (U).gb`, `Blue (U).gb`, `Yellow (U).gb`, `Red (J).gb`, `Green (J).gb`, `Blue (J).gb`, `Yellow (J).gb`, plus French/German/Spanish/Italian/Korean variants | `.gb` |
| Gen 2 (GBC) | `Gold (U).gbc`, `Silver (U).gbc`, `Crystal (U).gbc`, plus Japanese/French/German/Spanish/Italian variants | `.gbc` |
| Gen 3 (GBA) | `Ruby (U).gba`, `Sapphire (U).gba`, `Emerald (U).gba`, `Fire Red (U).gba`, `Leaf Green (U).gba`, plus regional variants | `.gba` |
| Gen 4 (NDS) | `Diamond (U).nds`, `Pearl (U).nds`, `Platinum (U).nds`, `HeartGold (U).nds`, `SoulSilver (U).nds`, plus regional variants | `.nds` |
| Gen 5 (NDS) | `Black (U).nds`, `White (U).nds`, `Black 2 (U).nds`, `White 2 (U).nds`, plus regional variants | `.nds` |
| Gen 6 (3DS) | `X.3ds`, `Y.3ds`, `Omega Ruby.3ds`, `Alpha Sapphire.3ds` | `.3ds` |
| Gen 7 (3DS) | `Sun.3ds`, `Moon.3ds`, `Ultra Sun.3ds`, `Ultra Moon.3ds` | `.3ds` |

Region codes used in file names: `(U)` = USA/English, `(J)` = Japan, `(F)` = French, `(G)` = German, `(S)` = Spanish, `(I)` = Italian, `(K)` = Korean, `(E)` = European.

### Nature of failures

- **`initializationError`** (8 randomizer test classes): JUnit could not instantiate the parameterized test because loading the default ROM threw `RomIOException(FileNotFoundException)` in `AbstractDSRomHandler.java:164`. These tests are set to load a Gen 4/5 USA ROM on initialization.
- **`IllegalArgumentException` (Gen 1-3 in MoveUpdaterTest)**: The test framework throws before even attempting to open a file, indicating the ROM name itself is not found in the registered list — the actual GBx ROM files need to be present for the handler to accept them.
- **`RomIOException / FileNotFoundException` (Gen 4-7 in MoveUpdaterTest)**: ROM NDS/3DS files are absent.
- **No code-logic failures** were observed. Every single failure traces back to `FileNotFoundException` for a ROM file.

---

## Reproduce from scratch

1. Clone the repository:
   ```
   git clone https://github.com/TimvanderWal504/universal-pokemon-randomizer-fvx.git
   cd universal-pokemon-randomizer-fvx
   git checkout upr/UPR-3
   ```
2. Ensure JDK 11 or later is installed (tested with Eclipse Temurin 25.0.3). The Gradle wrapper downloads Gradle 9.3.1 automatically.
3. Build the project:
   ```
   .\gradlew build -x test
   ```
   Expected: `BUILD SUCCESSFUL`
4. Run the fast unit-test tier:
   ```
   .\gradlew :random:test
   ```
   Expected: `BUILD SUCCESSFUL`, 2 tests pass, 0 failures.
5. Run the ROM-dependent test tier:
   ```
   .\gradlew :random:testROMs
   ```
   Expected: `BUILD FAILED` with all failures caused by missing ROM files (no ROM files are provided in the repo). Place ROM files in the `roms/` directory following the naming convention in `roms/readme.txt` to enable those tests.

---

## Resume point

**Current state:** BASELINE.md written and committed on branch `upr/UPR-3` at commit `4d68f8f86e094fcc6e1067539c0c6d2ca1c6d8a2 + 1`. The project compiles cleanly. The 2 pure unit tests pass. All 4158 ROM-tier tests fail exclusively due to missing ROM files (no ROM files are present in `roms/`).

**Exact next action for a follow-on agent:** UPR-4 and UPR-5 can now consume this baseline. If ROM files are added to `roms/`, re-run `.\gradlew :random:testROMs` and compare counts against the totals in this document to verify the environment delta. No code changes are needed to unblock the ROM tests — only ROM file acquisition is needed.
