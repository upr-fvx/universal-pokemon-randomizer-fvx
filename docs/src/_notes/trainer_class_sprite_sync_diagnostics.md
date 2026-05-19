# Trainer Class Sprite Sync Diagnostics

Status: diagnosis and design only. No writer change, no ROM execution, no P1 promotion.

## Why this exists

The Trainer/Foe log is not visual proof for Trainer Class Names. The log prints
the loaded `Trainer` model display name, and `Gen3RomHandler` refreshes that
display name after trainer-class text labels are rewritten. That proves the
class-name text table and logger-facing display string changed. It does not
prove that the trainer's visible battle sprite changed.

For Gen 3, the trainer data row stores these fields separately:

- byte `0x01`: `trainerClass`
- byte `0x02`: encounter music plus gender in vanilla, split into bitfields in
  CFRU
- byte `0x03`: `trainerPic`
- byte `0x04`: trainer name

The current `Trainer` model stores `trainerclass`, `name`, `fullDisplayName`,
party data, tags and battle-style state, but it does not store `trainerPic` or
the encounter-music/gender byte as mutable randomizer state. Loading reads
`trainerClass` from the Gen 3 `TrainerData` row. Saving currently writes party
data, trainer name, party size and the forced-double-battle flag. It does not
write `trainerClass`, encounter music/gender, or `trainerPic`.

That is why Trainer Class Names can appear textlabel-only: a class label can be
renamed globally from `BUG CATCHER` to `PKMN RANGER`, and the log can show
`PKMN RANGER Pi`, while the same trainer row still has the old `trainerPic` byte
and therefore still draws the old battle sprite.

## Current feature semantics

`Randomize Trainer Names` uses `TrainerNameRandomizer.randomizeTrainerNames()`.
It reads trainer names through `RomHandler.getTrainerNames()` and writes only the
trainer-name text through `RomHandler.setTrainerNames()`.

`Randomize Trainer Class Names` uses
`TrainerNameRandomizer.randomizeTrainerClassNames()`. Despite the feature name,
it does not assign new class IDs to trainers. It reads the current class-name
table through `RomHandler.getTrainerClassNames()`, builds shuffled translations
from existing class labels, preserves single-vs-double class pools, then writes
the class-name text table through `RomHandler.setTrainerClassNames()`.

For Gen 3, `setTrainerClassNames()` writes fixed-width strings into the resolved
trainer-class-name table and refreshes `Trainer.fullDisplayName` for already
loaded trainers. It does not write the `TrainerData` `trainerClass` byte. It also
does not write the `trainerPic` byte.

The logger snapshots original loaded trainer display names and later prints
`Trainer.fullDisplayName` when trainer-name randomization changed anything. That
output is useful text evidence, not class-ID or sprite evidence.

Existing no-ROM tests cover the important seam:

- the Gen 3 record layout keeps class ID, encounter music/gender, sprite pic and
  name at separate offsets
- trainer-class text remap updates the logger-facing display name without
  changing the row's class ID, encounter music/gender byte, or pic byte
- loading stores the class ID, not the gender bit, for later display-name
  refresh
- CFRU/DPE class-name text uses the runtime pointer when available

## Runtime sprite source

In vanilla pret FireRed, the battle trainer struct has separate fields for
`trainerClass`, `encounterMusic_gender`, and `trainerPic`. The visible opponent
trainer sprite is loaded from `gTrainers[trainerId].trainerPic`; the class name
text is loaded from `gTrainerClassNames[gTrainers[trainerId].trainerClass]`.
Prize money looks up `gTrainerMoneyTable` by `trainerClass`, and several code
paths use `trainerClass` for special logic such as league friendship events and
battle music.

CFRU keeps the same relevant shape: `struct Trainer` has `trainerClass`,
`encounterMusic`, `gender`, and `trainerPic`. The opponent battle controller
loads the visible front sprite from `gTrainers[...].trainerPic`, while battle
strings load the class text from `GetTrainerClassName(gTrainers[...].trainerClass,
text)`. CFRU also adds class-based systems, including class-based encounter BGM,
battle BGM, trainer money, trainer Pokeballs, AI/stat spread hooks, battle
transition logos and frontier helpers.

The CFRU repoints manifest repoints `gTrainers`, `gTrainerClassNames` and
`gTrainerMoneyTable` when `EXPAND_TRAINERS` is enabled. The trainer front pic and
front pic palette tables are present at vanilla BPRE symbols, but are commented
out in the CFRU repoints file. This means there is no confirmed FVX table model
today for "trainer class -> sprite/palette". The sprite is best treated as a
per-trainer `trainerPic` field until sanitized evidence proves a separate
expanded mapping table for the target ROM.

## Data model answers

- There is a per-trainer `trainerClass` / class ID.
- Gen 3 loading reads that class ID from byte `0x01` of each `TrainerData` row
  and stores it in `Trainer.trainerclass`.
- Gen 3 saving currently does not write the class ID back.
- The visible battle sprite is not bound to the class-name text table. It is
  read from the separate per-trainer `trainerPic` byte in the trainer row.
- Class ID affects more than text. It can affect money, encounter/battle music,
  Pokeballs, battle transition visuals, special-case logic and CFRU/DPE hooks.
- No current FVX Gen 3 code path models or writes a separate trainer-class to
  sprite/palette table.
- CFRU/DPE has expanded/repointed `gTrainers`, `gTrainerClassNames` and
  `gTrainerMoneyTable`; FVX already handles the CFRU/DPE trainer-class-name
  runtime pointer, but not a class/sprite assignment model.

## Design options

### A. Keep textlabel-only behavior

Keep the existing feature as a class-name text-table shuffle. Rename or document
the semantics clearly: "Trainer Class Names" changes displayed class labels and
log display strings, not trainer class IDs or visible sprites.

This is the lowest-risk path and matches current tests. It still needs visible
caveats in user-facing support claims because class label and sprite can
intentionally disagree.

### B. Remap trainer class IDs per trainer

Add a new Gen 3 capability that changes each selected trainer row's
`trainerClass` byte and writes it during `saveTrainers()`. If the target class is
chosen from another existing trainer, class name, money class, class-based music
and CFRU class metadata would follow the new class ID.

This does not automatically make the visible sprite follow unless the feature
also writes `trainerPic`, because Gen 3/CFRU stores pic separately. For real
sprite sync, the remap needs a source-class metadata map such as original
class-ID -> representative trainerPic and possibly encounter music/gender.

This path is powerful but risky. It can alter boss/rival/leader/champion logic,
class-based BGM, transition logos, money, Pokeballs and CFRU trainer-build hooks.

### C. Remap trainer pic/palette data without changing class ID

Leave `trainerClass` unchanged and write the per-trainer `trainerPic` byte, or
remap the underlying trainer front pic/palette table entries.

Per-trainer `trainerPic` is easier to reason about than table mutation because it
changes only selected trainer rows. Table remapping is much riskier: many
trainers can share a pic ID, mugshots and battle transitions may use the same
table, and CFRU character customization hooks can depend on specific pic IDs.

This option can make sprites change while class text and class-based metadata
stay unchanged, so it is sprite-randomization rather than class assignment sync.

### D. Add opt-in Trainer Class Assignment / Sprite Sync

Add a new feature separate from `Randomize Trainer Class Names`. Recommended
shape:

- off by default
- Gen 3 first, behind an explicit capability flag
- exclude rivals, friends, gym leaders, Elite Four, champion, strong/boss and
  runtime-source special rows by default
- operate on regular trainers first
- build a source metadata table from loaded trainers: class ID, representative
  class label, trainerPic, encounter music/gender, double-class status and risk
  tags
- write both `trainerClass` and `trainerPic` only when the mapping is safe and
  all IDs are in range
- refresh `fullDisplayName` after assignment
- add logger diagnostics that show old/new class ID and old/new pic ID; keep this
  separate from Trainer Pokemon party evidence

This is the best future fix strategy if visible class/sprite consistency is the
goal. It avoids changing the established textlabel-only option and gives users a
clear risk boundary.

## Risks and caveats

- Rivals, friends, gym leaders, Elite Four, champion, boss and other special
  trainers use class IDs in tags, scripts, BGM, transition logos, payout and
  game-specific logic. They should be excluded until explicitly tested.
- CFRU/DPE uses trainer class for more gameplay metadata than vanilla FRLG:
  battle/encounter BGM, money, class Pokeballs, EV/stat/friendship hooks and
  trainer-name substitutions.
- `trainerPic` drives battle sprites and can also feed mugshots, transition
  visuals, trainer card or character customization paths. Changing it can affect
  more than the battle intro sprite.
- Class ID remaps can change payout through `gTrainerMoneyTable`.
- Save/reload compatibility should be treated like any other ROM data rewrite:
  old saves may already have defeated-trainer flags, rematches or quest-log text
  tied to original trainer IDs. The class/sprite fields are ROM data, but visible
  consistency still needs fresh local smoke on a new output.
- CFRU/DPE expanded trainers and runtime-source trainer rows mean the writer must
  handle both normal loaded trainer rows and strict runtime-source rows. A class
  assignment feature should not assume vanilla `TrainerCount` covers every
  in-game battle.
- Randomization logs can confirm only the model and serialized byte intentions.
  They cannot prove that the running game used the same row or that a sprite
  decoded correctly.

## Sanitized evidence needed

Do not post ROMs, output ROMs, private paths, hashes, full logs, screenshots,
saves, emulator states, secrets, tokens or `.env` content.

Useful local evidence for a future implementation:

- affected battle label and trainer ID, if known
- original and randomized class label shown in the FVX log
- original and randomized `trainerClass` byte, if a diagnostic reports it
- original and randomized `trainerPic` byte, if a diagnostic reports it
- observed visible trainer sprite label in words
- whether the battle is a rival, gym, Elite Four, champion, runtime-source or
  regular trainer
- selected sanitized diagnostic rows proving that the in-game battle uses the
  same `TrainerData` row that FVX loaded and wrote

## Recommended fix strategy

Keep the existing Trainer Class Names feature textlabel-only and document the
caveat. Do not make it silently rewrite class IDs or sprites.

If sprite consistency is required, implement a new opt-in Gen 3 feature:
`Trainer Class Assignment / Sprite Sync`. Start with regular trainers only,
write both `trainerClass` and per-trainer `trainerPic`, preserve or explicitly
decide encounter music/gender, and log old/new class ID plus pic ID. Add no-ROM
tests for class-ID writeback, pic-byte writeback, display-name refresh and
special-trainer exclusion before any ROM smoke.

Do not use class-name log output as sprite proof. A local ingame smoke with
sanitized evidence is still required before claiming visual support, and this
diagnosis does not promote Trainer Class Names or any Foe Trainer path to P1.
