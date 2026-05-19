# Intro Mon Visual Source Diagnostics

Status: diagnostic-only. No P1 promotion.

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

`IntroPaletteOffset` is still reported by the diagnostic because ROM entries define it, but the FRLG writer currently
uses `IntroImageOffset + 4` for the palette pointer. A mismatch between these candidates is useful local evidence, not
an automatic fix.

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
- The observed visible intro species in words.

Do not post ROM paths, hashes, full logs, screenshots, saves, emulator states, output ROMs, secrets, tokens, or `.env`
content.

## Follow-up fix options

If local evidence shows the known locations change but the visible intro sprite does not, likely follow-ups are:

- Update the FRLG/CFRU-DPE ROM-entry offsets if a configured offset is stale.
- Add a CFRU/DPE-specific Intro Mon visual source writer if the visible sprite uses a separate literal or table.
- Extend the scanner to detect the newly identified source before changing writer behavior.

No writer change should be promoted from this diagnostic alone; it needs sanitized base/output evidence and an ingame
smoke result.
