# Misc Tweaks CFRU/DPE Notes

Status: CFRU/DPE BPRE has narrow fixes for Running Shoes misc tweaks and Randomize Catching Tutorial. No P1 promotion.

Codex did not run or inspect ROMs for these diagnostics.

## Running Shoes Diagnostics

Local behavior smoke reported by the workspace:

- Fastest Text: pass.
- Randomize PC Potion: pass.
- Ban Lucky Egg: pass.
- Running Shoes Indoors: previously ineffective on CFRU/DPE Gen9 BPRE.
- Run Without Running Shoes: previously ineffective on CFRU/DPE Gen9 BPRE.

The existing Gen3 misc tweak code used vanilla FRLG/RSE/E movement patches:

- Running Shoes Indoors wrote `0x00` at `RunIndoorsTweakOffset`.
- Run Without Running Shoes searched the vanilla running-shoes flag-check prefix and patched the branch near `FLAG_SYS_B_DASH`.

Those patches do not target the CFRU/DPE movement path. CFRU routes running through `ShouldPlayerRun` and
`IsRunningDisallowed`. Its running gate is split:

- `FLAG_RUNNING_ENABLED` controls whether the player has running enabled.
- `CAN_RUN_IN_BUILDINGS` only removes the indoor map-type restriction.

For BPRE with CFRU/DPE Gen9 detection:

- Run Without Running Shoes uses a CFRU/DPE-specific signature for `IsRunningDisallowed` and changes the running-enabled
  flag branch into an unconditional skip, so the player can run from New Game without the item/progression flag.
- Running Shoes Indoors no longer writes the stale vanilla `RunIndoorsTweakOffset`. For the target CFRU/DPE build with
  `CAN_RUN_IN_BUILDINGS`, the indoor gate is already absent, so the tweak is a safe no-op.

Vanilla FRLG/RSE/E behavior stays on the existing patch paths.

If a future CFRU/DPE BPRE build is compiled without `CAN_RUN_IN_BUILDINGS`, Running Shoes Indoors needs a separate
CFRU/DPE indoor-gate signature before it can be patched safely. The current fix intentionally avoids guessing at that
alternate compiled shape.

## Catching Tutorial Species

Local behavior smoke reported that Randomize Catching Tutorial produced a question-mark sprite with name `????????`.

The Gen 3 Catching Tutorial writer updates the opponent and player tutorial species literals from the ROM entry offsets:

- `CatchingTutorialOpponentMonOffset`
- `CatchingTutorialPlayerMonOffset`

Vanilla FRLG expects the normal `pokedexToInternal[species.getNumber()]` mapping. CFRU/DPE Gen9 BPRE can expose valid
runtime species through `speciesSetIdentityNumber` instead; this matches the mapping already used for CFRU/DPE wild,
trainer, static, trade, and intro species writes.

The failed local smoke is consistent with writing species `0` or another wrong raw ID from the Pokédex mapping path:
the battle tries to decode a species that is not a valid modeled runtime species and displays the placeholder name and
sprite.

For extended BPRE hacks, Randomize Catching Tutorial resolves the tutorial species through the internal species identity.
Species `0`, null species, or values that cannot be encoded by the existing tutorial literal shape are rejected before
any bytes are written.

The FRLG opponent literal remains limited to one byte. CFRU/DPE species with an internal identity above `255` are skipped
for that opponent slot instead of being truncated into an invalid low byte. The player literal keeps the existing Gen 3
two-instruction shape and accepts identities up to `510`.

Vanilla FRLG/RSE/E behavior remains on the existing Pokédex-to-internal mapping path.
