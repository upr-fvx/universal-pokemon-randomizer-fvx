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
