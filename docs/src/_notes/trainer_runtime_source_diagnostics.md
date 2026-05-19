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

The CFRU/DPE FireRed trainer runtime-party fix originally kept the scope
deliberately small: confirmed valid FRLG trainerbattle runtime sources for Rival
2 (`0x14B`, `0x149`, `0x14A`) and Brock (`0x19E`) were loaded into the trainer
model only when their raw `TrainerData` row was outside `TrainerCount`, had a
valid party pointer, and had a plausible party size.

Strict runtime-source sync generalizes that path without trusting arbitrary
script bytes. During trainer loading, deduped `trainerbattle` runtime-source
rows are loaded into the normal trainer model only when the audit classification
is `VALID_RUNTIME_NOT_LOADED`. The required conditions are:

- the script references a valid `TrainerData` row
- the raw party pointer is valid
- party size is `1..6`
- the full raw party is readable
- all raw party species resolve to loaded species
- the trainer is not already present in the normal loaded trainer model

Rows that pass strict discovery are randomized by the normal Trainer Pokemon
pipeline and are serialized back through the normal Gen3 trainer party
serializer during `saveTrainers()`. Invalid pointers, empty parties, out-of-range
rows, oversized parties, loaded/runtime mismatches and likely false positives are
not auto-synced by this path.

Generic strict runtime-source rows keep the `RUNTIME-SOURCE` tag for diagnostics
and save tracking, but the trainer model classifies that tag as a regular
trainer. That makes the rows eligible for Trainer Pokemon randomization and for
regular-trainer subpaths such as movesets, held items, added Pokemon and type
diversity when those settings are enabled. Known runtime-source tags, such as
Rival 2 and Brock, keep their specific `RIVAL*` / `GYM*-LEADER`
classification.

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
- strict runtime-source sync candidate selection using synthetic rows only

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

The audit modes remain diagnosis/reporting tools. Strict runtime-source sync is
the only automatic path, and it consumes only rows equivalent to
`unloaded-valid-parties` / `VALID_RUNTIME_NOT_LOADED`. It does not sync
`loaded-mismatch` rows and does not attempt to repair invalid rows.

## Post-randomization runtime source audit

The opt-in post-randomization audit compares a private base ROM with a private
randomized output ROM. It is intended for local verification after a normal
randomizer run, so a tester can check script-referenced runtime trainers without
playing every affected battle.

Enable it with system properties:

- `-Duprfvx.trainerRuntimeSourceBaseRom=<private-input-rom>`
- `-Duprfvx.trainerRuntimeSourceRandomizedRom=<private-output-rom>`

or the matching environment variables:

- `UPRFVX_TRAINER_RUNTIME_SOURCE_BASE_ROM=<private-input-rom>`
- `UPRFVX_TRAINER_RUNTIME_SOURCE_RANDOMIZED_ROM=<private-output-rom>`

The report is written to
`build/reports/diagnostics/trainer-runtime-source-post-randomization-audit-report.txt`.
Paths are redacted in the report header.

The compare audit starts from base-ROM `trainerbattle` runtime sources, dedupes
by `trainerId`, and keeps only conservative valid candidates: valid
`TrainerData` entry, valid party pointer, party size `1..6`, fully readable raw
party, in-bounds trainer ID, and plausible raw species. It then compares each
candidate to the randomized output ROM and reports:

- `trainerId`
- `baseRawParty`
- `outputRawParty`
- `loadedOutputParty`
- `outputClassification`
- `changedFromBase`
- `loadedRawPartyComparison`

The summary contains:

- `totalRuntimeSources`
- `validRuntimeTrainerCount`
- `changedFromBaseCount`
- `unchangedFromBaseCount`
- `outputValidRuntimeNotLoadedCount`
- `outputLoadedRuntimeMismatchCount`
- `invalidIgnoredCount`

Rows can emit these warning markers:

- `WARN unchanged valid runtime trainer`
- `WARN loaded/raw mismatch`
- `WARN valid runtime not loaded after strict sync`

An unchanged valid runtime trainer is not automatically a bug, because settings
can preserve some parties or species by chance. It is a focused review cue: the
tester should compare the randomized settings and nearby trainer-log output
before filing a follow-up. A loaded/raw mismatch means the loaded trainer model
and output ROM raw party disagree for that trainer ID. A valid runtime-not-loaded
warning means strict sync did not load a candidate that still looks eligible in
the randomized output and needs separate investigation.

Local ingame smoke is still required before stronger compatibility claims:

- run the private-ROM audit locally and keep paths/hashes/logs redacted
- confirm affected battles no longer show vanilla parties after randomization
- report only sanitized trainer IDs, classification, party summaries and pass/fail
  observations
- keep additional special cases or loaded/runtime mismatches as separate follow-up
  fixes

Strict sync does not promote Trainer Pokemon or any Foe Trainer suboption to P1.

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
