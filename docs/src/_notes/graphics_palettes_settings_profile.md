# Graphics/Palettes Settings Profile

`settings-profile` supports the Pokemon palette settings as normal serialized `.rnqs`
fields. It does not support Custom Player Graphics assets or character replacement
through `.rnqs`; those remain CLI/GUI asset inputs.

Supported Graphics/Palettes feature overlays:

- `FVX-GFX-001`: enables Pokemon Palette Randomization.
- `FVX-GFX-002`: enables Pokemon Palette Randomization and Follow Types.
- `FVX-GFX-003`: enables Pokemon Palette Randomization and Follow Evolutions.
- `FVX-GFX-004`: enables Pokemon Palette Randomization and Shiny From Normal.

The dependent overlays intentionally enable the parent palette randomization mode. A
single-feature settings profile for `FVX-GFX-002`, `FVX-GFX-003`, or `FVX-GFX-004`
would otherwise serialize only a checkbox while leaving palette randomization
`UNCHANGED`, producing a valid but ineffective profile.

Profile aliases:

- `09_graphics_palettes`
- `risk_graphics_palettes_visual`

Both aliases enable `FVX-GFX-001` through `FVX-GFX-004`. These profiles prepare an
isolated Graphics/Palettes smoke input only. They do not prove ingame visual palette
changes, do not cover Custom Player Graphics, and do not promote P1 status.

## Expanded Species Palette Descriptions

Gen 3-5 Pokemon palette randomization uses bundled `pokePalettes*` part-description
resources to decide which palette slots can be recolored together. CFRU/DPE expanded
species or formes can outnumber those bundled description rows.

When a species has no matching part description, the randomizer now falls back to a
blank description for that species instead of indexing past the description list.
That preserves vanilla/in-range behavior and prevents expanded-species crashes, but
the unmatched species/form may be skipped by the recolor pass until a specific
description row is added.

Local visual smoke is still required for any stronger Graphics/Palettes support
claim. This remains below P1 promotion.

## GUI Random Activation

The GUI `Graphics -> Pokemon Palettes -> Random` selection maps to
`Settings.PokemonPalettesMod.RANDOM`. `GameRandomizer` activates Gen 3-5 palette
randomization from that setting.

The Gen 3-5 palette randomizer now marks `changesMade` after successful population
of at least one species palette candidate. Without that marker the overview log can
report `Pokemon Palettes: Unchanged` even though the randomization path ran.

## CFRU/DPE Output Writes

The CFRU/DPE Gen9 BPRE palette writer now persists changed modeled Pokemon palettes
by writing a fresh compressed palette copy and repointing the affected normal or
shiny palette table entry. This avoids overwriting shared compressed palette data
while still allowing in-range species such as Charmander, Squirtle, Caterpie and
Pikachu to produce changed Base-vs-Output palette digests.

Expanded species or formes without bundled part descriptions may still remain
unchanged or be skipped/defaulted. Local visual smoke remains required because the
modeled palette table changing does not by itself prove CFRU/DPE runtime visuals
use that table for every context.

## Palette Output Audit

`Gen3PaletteOutputAuditRomTest` is an opt-in local diagnostic for cases where the
log says Pokemon palettes changed but ingame CFRU/DPE visuals still look vanilla.
It compares modeled Gen3 Pokemon palette table data between a private Base ROM and
a private Output ROM without changing writer behavior.

Run locally only with private ROM paths:

```sh
./gradlew :romio:test --tests '*Gen3PaletteOutputAuditRomTest*' \
  -Duprfvx.paletteAuditBaseRom=<private-base-rom> \
  -Duprfvx.paletteAuditOutputRom=<private-output-rom> \
  -Duprfvx.paletteAuditSpeciesIds=4,7,10,25,242
```

Environment variable equivalents are also supported:

- `UPRFVX_PALETTE_AUDIT_BASE_ROM`
- `UPRFVX_PALETTE_AUDIT_OUTPUT_ROM`
- `UPRFVX_PALETTE_AUDIT_SPECIES_IDS`

If either ROM path is omitted, the test is skipped and no ROM is read. If species
IDs are omitted, all modeled species are sampled.

The report is written to `build/reports/diagnostics/pokemon-palette-output-audit.txt`
with redacted ROM paths, ROM code/version/type, sampled counts, normal/shiny changed
counts, palette table pointers, palette byte digests and `changedFromBase` markers.
For CFRU/DPE expanded species, the report includes both `speciesId` and
`speciesIdentityNumber` so local evidence can distinguish base species from expanded
identity rows.

Use this only as sanitized local evidence. Do not share ROM paths, hashes, full
reports, screenshots, saves, emulator states, secrets or `.env` content. If modeled
palette table digests change but ingame visuals stay vanilla, the next likely
investigation is a CFRU/DPE runtime visual source outside the modeled Gen3 Pokemon
palette tables.
