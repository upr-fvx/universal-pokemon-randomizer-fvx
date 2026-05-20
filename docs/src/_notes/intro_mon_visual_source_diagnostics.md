# Intro Mon Visual Source Diagnostics

Status: diagnostic plus a narrow CFRU/DPE Intro visual pointer-table sync. No P1 promotion.

## Why this exists

The Intro Mon setting and log path can show that the randomizer selected a new species, but that is not visual proof.
The log is emitted after `IntroPokemonRandomizer` successfully calls `RomHandler.setIntroPokemon()`. For Gen 3 FRLG,
that only proves that the known intro locations were accepted and written in memory.

FRLG/CFRU-DPE hacks may route the visible Oak intro sprite through a different literal, pointer table, or runtime asset
source than the vanilla offsets known to the randomizer. If that happens, the settings and log can be correct while the
visible intro sprite still stays vanilla.

## Setting semantics

`No Random Intro Mon` is a negative GUI checkbox. Internally the settings field is positive:

- `randomizeIntroMon=true` means randomize the Intro Mon.
- `randomizeIntroMon=false` means keep the Intro Mon unchanged.
- `MODE-INTRO-RANDOM` sets `randomizeIntroMon=true`.
- `MODE-NO-RANDOM-INTRO` sets `randomizeIntroMon=false`.
- `FVX-GEN-003` also sets `randomizeIntroMon=false`.
- The `.rnqs` serialization stores this as general-options byte 65, bit 0.

`GameRandomizer` only calls `IntroPokemonRandomizer.randomizeIntroPokemon()` when `settings.isRandomizeIntroMon()` is
true.

## Current Gen 3 write targets

For FRLG, `Gen3RomHandler.setIntroPokemon()` writes:

- `IntroCryOffset`: raw internal species byte.
- `IntroOtherOffset`: raw internal species byte.
- `IntroImageOffset`: pointer to `PokemonFrontImages + speciesId * 8`.
- `IntroImageOffset + 4`: pointer to `PokemonNormalPalettes + speciesId * 8`.
- For detected CFRU/DPE Gen9 BPRE, the Nidoran female front-image and normal-palette pointer-table entries are also
  synced to the target species asset pointers. Local evidence showed the visible Oak intro sprite still read those
  Nidoran female normal asset table entries even after the known FRLG intro sources changed.
- For extended CFRU/DPE BPRE species pools, the writer uses the species-set internal identity for Intro Mon writes.
  This prevents named extended-pool species whose Pokédex mapping is `0` from writing raw species `0` to the Intro
  species literals and visual pointer sources.
- CFRU/DPE Gen 7/8/9 Intro Mon candidates with identity values above the FRLG raw-byte limit can be accepted through
  the confirmed visual pointer-table path when both target front-image and normal-palette table entries are in ROM. In
  that path the raw one-byte cry/other literals are left unchanged because they cannot safely encode extended species
  IDs.
- The randomizer exhausts candidates and skips Intro Mon unchanged if no candidate is accepted, instead of crashing,
  looping, or accepting species `0`.

`IntroPaletteOffset` is still reported by the diagnostic because ROM entries define it, but the FRLG writer currently
uses `IntroImageOffset + 4` for the palette pointer. A mismatch between these candidates is useful local evidence, not
an automatic fix.

## CFRU/DPE visual-source search

Local evidence can show that every known FRLG Intro source changed from the base species to the randomized species while
the visible intro sprite still remains the base species. In that case the known write targets are not enough visual
evidence; CFRU/DPE may be using another raw literal or asset pointer for the on-screen sprite.

When both base and output ROM paths are configured, the opt-in report now also searches for base-species visual
candidates and compares the same offset in the output ROM. Candidate types are:

- `raw-u8-species`: one-byte species literal matching the base known Intro species.
- `raw-u16-species`: little-endian two-byte species literal matching the base known Intro species.
- `front-table-entry-pointer`: pointer to the base species entry inside `PokemonFrontImages`.
- `palette-table-entry-pointer`: pointer to the base species entry inside `PokemonNormalPalettes`.
- `front-asset-pointer`: direct pointer to the base species front-image asset read from the front-image table.
- `palette-asset-pointer`: direct pointer to the base species normal-palette asset read from the palette table.

Each line reports candidate type, offset, base value, output value, `changedFromBase=yes/no`, and a plausibility reason.
Reasons currently include known intro offsets, nearby recognized script opcodes, proximity to known intro code/data
offsets, and membership in the front-image or palette pointer tables.

An unchanged candidate is not proof by itself. It is a short list for local inspection: if the visible intro sprite
stays at the base species while the known FRLG sources changed, unchanged front asset, palette asset, table-entry, or
species-literal candidates near plausible code/data regions are the best follow-up targets.

The current CFRU/DPE writer intentionally uses only the confirmed pointer-table source shape. It does not write raw
species literals and does not scan arbitrary candidates during saving.

## Opt-in local report

The diagnostic harness is skipped unless a local ROM path is explicitly configured:

```sh
./gradlew :romio:test --tests '*Gen3IntroMonVisualSourceRomTest*' \
  -Duprfvx.introMonVisualSourceBaseRom='<private-input-rom>'
```

Optional base-vs-output comparison:

```sh
./gradlew :romio:test --tests '*Gen3IntroMonVisualSourceRomTest*' \
  -Duprfvx.introMonVisualSourceBaseRom='<private-input-rom>' \
  -Duprfvx.introMonVisualSourceOutputRom='<private-output-rom>'
```

Environment variable equivalents:

- `UPRFVX_INTRO_MON_VISUAL_SOURCE_BASE_ROM`
- `UPRFVX_INTRO_MON_VISUAL_SOURCE_OUTPUT_ROM`

The report is written under `build/reports/diagnostics/intro-mon-visual-source-report.txt`.

## Sanitized evidence to post

Post only concise, sanitized lines such as:

- Candidate source name.
- Offset as hex.
- Raw species id or decoded species.
- Pointer-derived expected species id.
- Base-vs-output `changedFromBase=yes/no`.
- Candidate-search type, base value, output value, and short plausibility reason.
- The observed visible intro species in words.

Do not post ROM paths, hashes, full logs, screenshots, saves, emulator states, output ROMs, secrets, tokens, or `.env`
content.

## Follow-up fix options

If local evidence shows the known locations change but the visible intro sprite does not, likely follow-ups are:

- Update the FRLG/CFRU-DPE ROM-entry offsets if a configured offset is stale.
- Extend the CFRU/DPE-specific writer if the visible sprite uses a different separate literal or table.
- Extend the scanner to detect newly identified sources before changing writer behavior.

The CFRU/DPE pointer-table sync still needs local ingame smoke after a randomized output ROM is created outside Codex.
Do not promote it to P1 from diagnostics alone.
