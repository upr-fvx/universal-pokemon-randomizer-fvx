# Trainer Class Sprite Sync Diagnostics

Status: Gen 3 opt-in implementation for regular trainers only. No ROM execution. No P1 promotion.

## Separation from Trainer Class Names

`Randomize Trainer Class Names` remains textlabel-only. It shuffles/writes the
trainer-class-name text table and refreshes logger display names. It does not
prove that the visible battle sprite changed.

Gen 3 trainer rows keep these fields separate:

- byte `0x01`: `trainerClass`
- byte `0x02`: encounter music plus gender in vanilla, split in CFRU
- byte `0x03`: `trainerPic`
- byte `0x04`: trainer name

The visible opponent battle sprite is driven by per-trainer `trainerPic`, while
the displayed class string is driven by `trainerClass` plus the class-name text
table. A class-name log entry alone is therefore not sprite evidence.

## New opt-in feature

`Trainer Class Assignment / Sprite Sync` is implemented as a separate opt-in
path, currently exposed through the settings-profile overlay:

- `MODE-TRAINER-CLASS-SPRITE-SYNC`

The setting is serialized as `Settings.randomizeTrainerClassSprites`. It is off
by default and disabled by `Settings.tweakForRom()` when a ROM handler does not
support Gen 3 trainer class/sprite row serialization.

The implementation intentionally does not change the existing class-name text
feature. If both options are enabled, class-name text remapping and class/sprite
assignment remain separate operations.

## Scope

The first implementation is conservative:

- Gen 3 only
- regular trainers only
- excludes runtime-source rows
- excludes rival/friend, gym leader, Elite Four, champion, strong/boss and other
  tagged special trainers through the existing `Trainer` classification helpers
- uses only class/sprite pairs already observed on eligible regular trainers
- writes `trainerClass` and `trainerPic` only when the new opt-in feature made
  assignments

The feature does not mutate trainer names or trainer-class text strings.

## Writer behavior

Gen 3 loading now stores `trainerPic` on the `Trainer` model alongside
`trainerclass`.

Gen 3 saving still preserves old behavior by default. `saveTrainers()` writes the
`trainerClass` and `trainerPic` row bytes only after the new randomizer enables
the Gen 3 class/sprite sync write flag. With the feature off, existing
TrainerData class and pic bytes are not rewritten.

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
- visible trainer sprite label in words
- whether the battle is regular, rival, gym, Elite Four, champion, boss or
  runtime-source
- confirmation that regular trainers changed while excluded special trainers did
  not

This feature remains below P1 until local ingame smoke confirms visible sprite
consistency on a private output ROM produced outside Codex.
