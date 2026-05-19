# Trainer Runtime Source Diagnostics

This note documents the no-ROM and opt-in diagnosis path for CFRU/DPE FireRed
trainer battles that still appear vanilla in-game even when the randomizer log
shows randomized Trainer Pokemon.

The Trainer Pokemon log is not a writer or runtime proof. Trainer randomization
mutates the in-memory `RomHandler.getTrainers()` list, and the log prints that
same list. The ROM writer later serializes trainer parties through
`Gen3RomHandler.saveTrainers()`, and the game runtime may still read a different
scripted trainer ID, copied raw party data, or another CFRU/DPE runtime source.
Clean log output therefore proves only that the loaded trainer model was
randomized. It does not prove that every in-game `trainerbattle` command reads
that same row at runtime.

The CFRU/DPE FireRed trainer runtime-party fix keeps the scope deliberately
small: confirmed valid FRLG trainerbattle runtime sources for Rival 2
(`0x14B`, `0x149`, `0x14A`) and Brock (`0x19E`) are loaded into the trainer
model only when their raw `TrainerData` row is outside `TrainerCount`, has a
valid party pointer, and has a plausible party size. The same rows are then
serialized back during trainer saving, so a randomized trainer log entry is
backed by the raw trainerbattle runtime source instead of only by the loaded
model.

Current hypotheses:

- A script uses a different `trainerbattle` trainer ID than the vanilla FRLG
  tag expected for the affected battle.
- The runtime uses a separate or copied raw party source instead of the loaded
  `TrainerData` row that was logged.
- The writer updates a valid `TrainerData` source, but not the source actually
  used by the affected battle at runtime.

The committed no-ROM coverage uses synthetic byte arrays only:

- valid `trainerbattle` ID to `TrainerData` row mapping
- out-of-range trainer ID producing a safe diagnostic entry
- party pointer and first raw species extraction from synthetic party bytes
- global audit classification and trainer-ID dedupe using synthetic runtime
  source rows only

The ROM-reading report remains opt-in through local test configuration. It must
not be run by automated CI, and reports must keep private ROM paths redacted.

## Global runtime source audit

The existing opt-in ROM report can also emit a deduped global audit over all
byte-scanned FRLG `trainerbattle` commands. Enable it with either:

- `-Duprfvx.trainerRuntimeSourceAudit=all`
- `-Duprfvx.trainerRuntimeSourceAudit=unloaded-valid-parties`
- `-Duprfvx.trainerRuntimeSourceAudit=loaded-mismatch`
- `-Duprfvx.trainerRuntimeSourceAudit=invalid`

or the matching environment variable:

- `UPRFVX_TRAINER_RUNTIME_SOURCE_AUDIT=all`
- `UPRFVX_TRAINER_RUNTIME_SOURCE_AUDIT=unloaded-valid-parties`
- `UPRFVX_TRAINER_RUNTIME_SOURCE_AUDIT=loaded-mismatch`
- `UPRFVX_TRAINER_RUNTIME_SOURCE_AUDIT=invalid`

The audit emits one row per unique `trainerId`, with all observed
`scriptOffsets` and `battleTypes` grouped into that row. Each row contains:

- `trainerId`
- `scriptOffsets`
- `battleTypes`
- `trainerOffset`
- `trainerEntryValid`
- `partyFlags`
- `partySize`
- `partyPointer`
- `partyPointerValid`
- `firstRawSpeciesId`
- `firstDecodedSpecies`
- `loadedParty`
- `rawParty`
- `classification`

Classifications are:

- `VALID_RUNTIME_NOT_LOADED`
- `LOADED_AND_RUNTIME_MATCH`
- `LOADED_AND_RUNTIME_MISMATCH`
- `INVALID_POINTER`
- `EMPTY_PARTY`
- `OUT_OF_RANGE`
- `FALSE_POSITIVE_LIKELY`

The focused `unloaded-valid-parties` mode reports only rows where the
`TrainerData` entry is valid, the party pointer is valid, party size is `1..6`,
the raw party can be read, the first raw species resolves to a loaded species,
and the loaded trainer model reports `loadedParty=<not loaded>`.

This audit is diagnosis only. It does not add any automatic runtime-source sync,
does not expand `saveTrainers()`, and does not change normal randomizer
behavior. Any broader auto-sync/write coverage must stay in a separate follow-up
PR after sanitized local evidence proves the target rows are real runtime
sources.

For local sanitized evidence, provide only:

- affected battle label, such as "second rival", "Brock", or a route/trainer
  description
- expected/logged trainer ID if visible
- observed in-game trainer ID or party summary if known
- relevant diagnostic lines with ROM paths, hashes, full logs, screenshots, and
  save/emulator state details removed
- whether the `trainerbattle` runtime source points at the same `TrainerData`
  row and party pointer that the randomizer log identified
- selected global audit rows from `unloaded-valid-parties`, `loaded-mismatch`,
  or `invalid` mode when they are directly relevant to a suspected in-game
  vanilla party

Do not include ROMs, output ROMs, private paths, hashes, full logs, screenshots,
saves, emulator states, secrets, tokens, or environment files in PRs or issues.
