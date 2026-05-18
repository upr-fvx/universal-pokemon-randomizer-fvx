# Trainer Runtime Source Diagnostics

This note documents the no-ROM and opt-in diagnosis path for CFRU/DPE FireRed
trainer battles that still appear vanilla in-game even when the randomizer log
shows randomized Trainer Pokemon.

The Trainer Pokemon log is not a writer or runtime proof. Trainer randomization
mutates the in-memory `RomHandler.getTrainers()` list, and the log prints that
same list. The ROM writer later serializes trainer parties through
`Gen3RomHandler.saveTrainers()`, and the game runtime may still read a different
scripted trainer ID, copied raw party data, or another CFRU/DPE runtime source.

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

The ROM-reading report remains opt-in through local test configuration. It must
not be run by automated CI, and reports must keep private ROM paths redacted.
For local sanitized evidence, provide only:

- affected battle label, such as "second rival", "Brock", or a route/trainer
  description
- expected/logged trainer ID if visible
- observed in-game trainer ID or party summary if known
- relevant diagnostic lines with ROM paths, hashes, full logs, screenshots, and
  save/emulator state details removed
- whether the `trainerbattle` runtime source points at the same `TrainerData`
  row and party pointer that the randomizer log identified

Do not include ROMs, output ROMs, private paths, hashes, full logs, screenshots,
saves, emulator states, secrets, tokens, or environment files in PRs or issues.
