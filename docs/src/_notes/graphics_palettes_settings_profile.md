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
