# Changes

---
## General

- Changes to the Launcher

The randomizer will now not allow the user to randomize 3DS games if it was not started via the launcher, to prevent users from encountering "loading forever" errors. A pop-up message will alert the user if this is the case. The launcher will also give a pop-up error message if it fails to start (which usually happens due to an incompatible Java version). [A new wiki page describes the randomizer's Java requirements.](https://github.com/Ajarmar/universal-pokemon-randomizer-zx/wiki/About-Java)

- Setting: Limit Pokemon

Fixed an issue that would cause the randomizer to crash if Gen 3 Pokemon were excluded.
Can now be used with "Allow Alternate Formes" for Trainer Pokemon/Wild Pokemon.

---
## Pokemon Traits

### Pokemon Base Statistics

- Setting: Update Base Stats

Gen 2: Fixed an issue that would cause this setting to crash the randomizer.

### Pokemon Abilities

- Setting: Unchanged

When Pokemon Abilities are unchanged, prevents alternate formes of Pokemon that change depending on their abilities from appearing even if "Allow Alternate Formes" settings are used. (Affects Castform, Darmanitan, Aegislash, Wishiwashi)

---
## Starters, Statics & Trades

### Starter Pokemon

- Setting: Custom

Gen 7: Now allows alternate formes to be selected (previously only available in Gen 6).

- Setting: Randomize Starter Held Items

Now works for the German version of Pokemon Emerald.

### Static Pokemon

- Setting: Swap Legendaries & Swap Standards

Gen 7: Now also swaps Ultra Beast encounters with other Ultra Beasts.

- Setting: Swap Mega Evolvables

No longer allows Rayquaza to appear, due to its special Mega Evolution circumstances.

- Legendary Box/Party Swapping

B/W/X/Y/OR/AS: The legendary Pokemon that allow you to swap out a party Pokemon for the captured legendary can now be swapped even when they are randomized. Affects Reshiram/Zekrom (B/W), Xerneas/Yveltal (X/Y), Rayquaza (OR/AS).

---
## Moves & Movesets

### Move Data

- Setting: Update Moves

When saving a settings file, the choice for this setting is saved/loaded properly instead of always matching the "Update Base Stats" setting.

---
## Trainer Pokemon

### Trainers Pokemon

- Setting: Double Battle Mode

Gen 4: Special Trainer intros and music now work correctly, instead of using the default Trainer intro and music.

- Setting: Swap Mega Evolvables

No longer allows Rayquaza to appear, due to its special Mega Evolution circumstances.

- Important Trainers

HG/SS: The Kimono Girls are now considered important trainers, for the purpose of the "Double Battle Mode" and "Additional Pokemon" settings.

- Lusamine 2's Boosted Pokemon

S/M: In Lusamine's second fight, her Pokemon now properly start with a boosted stat even if they are randomized. The boosted stat will be their highest non-HP stat. Note that if the "Additional Pokemon" setting is used, only the first five of her Pokemon will be boosted.

---
## Wild Pokemon

### Wild Pokemon

- Setting: Set Minimum Catch Rate

Now also modifies catch rates for alternate formes of Pokemon.

- Setting: Percentage Level Modifier

G/S/C: Now also modifies levels for regular Grass/Sea encounters.
D/P/Pt: Now also modifies levels for Honey Tree encounters and Feebas tile encounters.

- Setting: Global 1-to-1 Mapping

Fixed an issue that would cause the randomizer to crash when this setting was used without "Allow Alternate Formes".

- Setting: Allow Alternate Formes

Gen 7: Now also allows Minior's Core Form to appear in the wild.

- More Wild Pokemon Randomized

    - D/P/Pt: Mr. Backlot's Pokemon are now randomized.
    - HG/SS: Bug Catching Contest Pokemon are now randomized.

---
## TM/HMs & Tutors

### TMs & HMs

- Setting: Keep Field Move TMs

OR/AS: Secret Power is now considered a field move.

---
## Misc Tweaks

- Setting: Don't Revert Temporary Alt Formes

OR/AS: Now also doesn't revert Primal Groudon/Kyogre when they are caught.
