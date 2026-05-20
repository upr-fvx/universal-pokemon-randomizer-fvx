# Misc Tweaks CFRU/DPE Running Diagnostics

Local behavior smoke reported by the workspace:

- Fastest Text: pass.
- Randomize PC Potion: pass.
- Ban Lucky Egg: pass.
- Running Shoes Indoors: previously ineffective on CFRU/DPE Gen9 BPRE.
- Run Without Running Shoes: previously ineffective on CFRU/DPE Gen9 BPRE.

Codex did not run or inspect ROMs for this diagnosis.

## Diagnosis

The existing Gen3 misc tweak code used vanilla FRLG/RSE/E movement patches:

- Running Shoes Indoors wrote `0x00` at `RunIndoorsTweakOffset`.
- Run Without Running Shoes searched the vanilla running-shoes flag-check prefix and patched the branch near `FLAG_SYS_B_DASH`.

Those patches do not target the CFRU/DPE movement path. CFRU routes running through `ShouldPlayerRun` and `IsRunningDisallowed`. Its running gate is split:

- `FLAG_RUNNING_ENABLED` controls whether the player has running enabled.
- `CAN_RUN_IN_BUILDINGS` only removes the indoor map-type restriction.

That means a CFRU build can have indoor running support compiled in and still block running before the shoes/progression flag is set.

## Fix Decision

For BPRE with CFRU/DPE Gen9 detection:

- Run Without Running Shoes now uses a CFRU/DPE-specific signature for `IsRunningDisallowed` and changes the running-enabled flag branch into an unconditional skip, so the player can run from New Game without the item/progression flag.
- Running Shoes Indoors no longer writes the stale vanilla `RunIndoorsTweakOffset`. For the target CFRU/DPE build with `CAN_RUN_IN_BUILDINGS`, the indoor gate is already absent, so the tweak is a safe no-op.

Vanilla FRLG/RSE/E behavior stays on the existing patch paths.

## Caveat

If a future CFRU/DPE BPRE build is compiled without `CAN_RUN_IN_BUILDINGS`, Running Shoes Indoors needs a separate CFRU/DPE indoor-gate signature before it can be patched safely. The current fix intentionally avoids guessing at that alternate compiled shape.
