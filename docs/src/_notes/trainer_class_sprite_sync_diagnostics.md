# Trainer Class Sprite Sync Diagnostics

Status: Gen 3 opt-in chaotic visual consistency implementation for class
label/class ID/pic consistency. No ROM execution. No P1 promotion.

## Separation from Trainer Class Names

Without `MODE-TRAINER-CLASS-SPRITE-SYNC`, `Randomize Trainer Class Names`
remains legacy textlabel-only behavior: it shuffles/writes the trainer-class-name
text table and refreshes logger display names. It does not prove that the visible
battle sprite changed.

Gen 3 trainer rows keep these fields separate:

- byte `0x01`: `trainerClass`
- byte `0x02`: encounter music plus gender in vanilla, split in CFRU
- byte `0x03`: `trainerPic`
- byte `0x04`: trainer name

The visible opponent battle sprite is driven by per-trainer `trainerPic`, while
the displayed class string is driven by `trainerClass` plus the class-name text
table. A class-name log entry alone is therefore not sprite evidence.

## New opt-in feature

`Trainer Class Assignment / Sprite Sync` is implemented as a separate opt-in path
that follows the Trainer Class Names randomization, currently exposed through the
settings-profile overlay:

- `MODE-TRAINER-CLASS-SPRITE-SYNC`

The setting is serialized as `Settings.randomizeTrainerClassSprites`. It is off
by default and disabled by `Settings.tweakForRom()` when a ROM handler does not
support Gen 3 trainer class/sprite row serialization.

`Randomize Trainer Names` remains separate and changes only trainer personal
names. It does not provide class ID or sprite mappings.

When both `Randomize Trainer Class Names` and `MODE-TRAINER-CLASS-SPRITE-SYNC`
are enabled, the class-name randomizer records its old class ID -> target class
ID mapping. Sprite Sync then assigns each trainer to the mapped target class ID
and uses an observed `trainerPic` from that target class. The class-name text
table is restored to the original class labels so the displayed class, class ID
and visible pic describe the same target class.

The class-name mapping is class-ID based and avoids identity mappings when an
alternative target class exists in the same allowed pool. If a pool has only one
valid class, identity is allowed because no different target exists.

This is a chaotic opt-in visual consistency mode. It is not a lore/plausibility
mode, not a regular-only stable mode and not a promise that a trainer remains in
the same narrative role.

## Scope

The first implementation is narrow but intentionally chaotic:

- Gen 3 only
- off by default
- requires Trainer Class Names randomization to provide a class ID mapping
- follows that mapping instead of choosing an independent random class/pic pair
- does not add target-class filtering beyond requiring a modeled target class
  and an observed valid pic
- allows regular trainers to become Rival, Player, Gym Leader, Elite Four,
  champion, boss or other special-looking classes when the class-name randomizer
  maps them there
- treats those special-looking target classes as expected chaos behavior with
  Sprite Sync enabled
- uses only `trainerPic` values observed on trainers with the target class ID
- skips only target classes that have no observed valid pic ID
- writes `trainerClass` and `trainerPic` only when the new opt-in feature made
  assignments

The feature does not mutate trainer personal names. Its goal is class
label/class ID/trainerPic consistency, not lore or encounter plausibility.

## Writer behavior

Gen 3 loading now stores `trainerPic` on the `Trainer` model alongside
`trainerclass`.

Gen 3 saving still preserves old behavior by default. `saveTrainers()` writes the
`trainerClass` and `trainerPic` row bytes only after the new randomizer enables
the Gen 3 class/sprite sync write flag. With the feature off, existing
TrainerData class and pic bytes are not rewritten.

For FRLG runtime-source safety, the Sprite Sync save path also revisits
script-referenced trainer IDs when the sync flag is active. Loaded rows are
rewritten by trainer ID for class/pic bytes, and strict unloaded runtime-source
rows continue through the full TrainerData row writer. This is intended to avoid
a log/model-only sync when the battle script reads a runtime TrainerData row.

The latest local smoke still keeps this feature caveated: the log/model showed a
regular trainer becoming an Elite Four class with a class/sprite sync marker, but
the visible ingame sprite remained the old class. That result means class
label/class ID/pic consistency is not yet ingame-proven for the final output
path.

## Logging

Trainer/Foe logging now distinguishes class/sprite assignment from class-name
text remapping. When the opt-in feature changes a trainer, the Trainer Pokemon
section includes a marker like:

```text
[class/sprite sync: class 1=>2, pic 7=>8]
```

This marker is byte-intent/model evidence. It is not a substitute for local
ingame visual smoke.

## Local smoke requirement

Do not post ROMs, output ROMs, private paths, hashes, full logs, screenshots,
saves, emulator states, secrets, tokens or `.env` content.

Useful sanitized local evidence:

- affected battle label and trainer ID, if known
- old/new class ID and old/new pic ID from the log marker
- targeted runtime-source diagnostic row for the same trainer ID, including
  loaded trainer class/pic, raw TrainerData class/pic, script runtime row
  class/pic and loaded-vs-raw comparison
- visible trainer sprite label in words
- whether the battle is regular, rival, player, gym, Elite Four, champion, boss
  or runtime-source
- confirmation that the displayed class label and visible trainer sprite match
  the logged target class

This feature remains below P1 until local ingame smoke confirms visible sprite
consistency on a private output ROM produced outside Codex.
