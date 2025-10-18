# Changes

---
## General

- Remastered GUI

The graphical user interface has been remade from the ground up (but looks mostly the same). Language/wording in the GUI has been made more consistent, and resizing the window works a bit better. For those who want to compile the program themselves, this now requires IntelliJ; better support for other IDEs may be added in the future.

- Logs

When generating a log after randomization, information about randomizer version as well as seed and configuration string is now printed at the beginning of the log.

- Preset Seeds

Should work again, both when using a preset file (.rndp) or a seed + config string.

- **NEW:** Alternate Formes

Alternate Formes of Pokemon, such as those of Deoxys, Wormadam and Rotom, are now randomized separately from their base forms. Currently only in generation 4 and 5.

---
## Starters, Statics & Trades

### Static Pokemon

- More Static Pokemon Supported

  - In generation 5 games, static Foongus and Amoonguss (fake Pokéballs) are now randomized when randomizing static Pokemon. Note that every static Foongus will become the same Pokemon, and every static Amoonguss will become the same Pokemon.
  - In Black 2/White 2, N's Zorua is now randomized when randomizing static Pokemon.

- Random (similar strength)

Fixed a bug that caused this setting to not work if Trainer Pokemon were not randomized.

---
## Moves & Movesets

### Pokemon Movesets

- **NEW:** No Game-Breaking Moves

The previous "General Option" with the same name, which prevented game-breaking moves from appearing as part of Pokemon movesets, TMs or Move Tutors, has been split up into three separate options instead (one for movesets, one for TMs and one for tutors).

---
## Foe Pokemon

- Cosmetic Forms

In generations 4 and 5, Pokemon with cosmetic forms (such as Unown and Shellos) will get a random form when a trainer has them.

### Trainer Pokemon

- Force Fully Evolved at

This setting can now be used even if Trainer Pokemon are not randomized.

- Percentage Level Modifier

This setting can now be used even if Trainer Pokemon are not randomized.

- **NEW:** Allow Alternate Formes

With this setting enabled, alternate formes of Pokemon can appear as Trainer Pokemon. This means forms that change a Pokemon's stats/typing/ability, such as those of Deoxys, Wormadam and Rotom (due to how it is stored in the game's data, Keldeo's Resolute Forme is also included despite not having any changes to stats/typing/ability). Available for generation 4 and 5.

---
## Wild Pokemon

### Wild Pokemon

- More Wild Pokemon Supported

  - In Diamond/Pearl/Platinum, Pokemon from Honey Trees, rotating Pokemon in the Great Marsh, and Feebas squares are now randomized when randomizing Wild Pokemon. (Due to certain limitations, using the "Percentage Level Modifier" setting will not change the levels of these Pokemon.)
  - In HeartGold/SoulSilver, Pokemon from Headbutt Trees are now randomized when randomizing Wild Pokemon.

- Percentage Level Modifier

  - Now works in generation 3.
  - Now works for all wild Pokemon in HeartGold/SoulSilver.
  - This setting can now be used even if Wild Pokemon are not randomized.

---
## TM/HMs & Tutors

### TMs & HMs

- **NEW:** No Game-Breaking Moves

The previous "General Option" with the same name, which prevented game-breaking moves from appearing as part of Pokemon movesets, TMs or Move Tutors, has been split up into three separate options instead (one for movesets, one for TMs and one for tutors).

### Move Tutors

- **NEW:** No Game-Breaking Moves

The previous "General Option" with the same name, which prevented game-breaking moves from appearing as part of Pokemon movesets, TMs or Move Tutors, has been split up into three separate options instead (one for movesets, one for TMs and one for tutors).

---
## Items

### Non-Main Shop Items

- Guarantee Evolution Items

Now guarantees evolution items in main-game shops, rather than any shop.