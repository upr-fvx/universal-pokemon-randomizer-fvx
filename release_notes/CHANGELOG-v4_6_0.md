# Changes

---
## General

- FIX: Cosmetic Formes in Gen 7
    - Fixed an issue where certain cosmetic formes (for example, regular size Mimikyu) could not be randomly chosen for wild, trainer, or static Pokemon in the Generation 7 games.

- FIX: Vietnamese Crystal Stability
    - Removed the TwistedSpoon from the item pool, since it can cause the player's inventory to glitch out.
         - Thanks to @pidgezero-one for this fix!

- FIX: Unix Launcher
    - Added a shebang to the Unix launcher
        - Thanks to @Realitaetsverlust for this fix!

- FIX: Loading Settings Created in a Later Generation
    - Fixed an issue where the randomizer would silently fail to load settings files or settings strings if those settings were created in a later generation.

---
## Pokemon Traits

### Pokemon Base Statistics

- FIX: Update Base Stats to Generation
    - Ultra Sun/Ultra Moon: Fixed an issue where enabling this setting would erroneously change Mega Scizor's stats.

- CHANGED: Update Base Stats to Generation
    - This setting can now update base stats to match the Generation 9 games.

### Pokemon Evolutions

- FIX: Nincada's Evolutions
    - Gen 5: Fixed an issue where Nincada'a "Shedinja Evolution" would crash on real hardware.
    - Gen 7: Fixed an issue where Nincada's "Shedinja Evolution" could be an invalid Pokemon.
    - Gen 7: Fixed an issue where Nincada's "Shedinja Evolution" sometimes didn't generate a Pokemon even if all the appropriate conditions were met.

---
## Starters, Statics & Trades

### Starter Pokemon

- ADDED SUPPORT: Randomize Starter Held Items
    - This setting is now supported in Diamond, Pearl, Platinum, and all Generation 6 and 7 games.

---
## Moves & Movesets

### Move Data

- CHANGED: Update Moves to Generation
    - This setting can now update move stats to match the Generation 9 games.

---
## Foe Pokemon

### Trainer Pokemon

- ADDED SUPPORT: More Trainer Pokemon Supported
    - Emerald: Steven's team during the Multi Battle in the Mossdeep Space Center can now be randomized.

- CHANGED: Better Movesets
    - This setting can now also be enabled when Trainer Pokemon are not randomized.

- FIX: Pokemon League Has Unique Pokemon
    - Fixed an issue where enabling this setting with a Japanese Generation 3 game would crash the randomizer.

- FIX: Type Themed (Elite Four/Gyms Only)
    - Fixed an issue where this setting could crash the randomizer when combined with "Limit Pokemon" and "Swap Mega Evolvables".
        - If there isn't a mega evolvable of every type in the pool (as determined by your "Limit Pokemon" settings), then "Swap Mega Evolvables" will be disabled.

- FIX: Additional Pokemon for...
    - Fixed an issue where certain stats on the trainer Pokemon (for example, IVs) were accidentally set to 0 instead of being based on the stats of the trainer's original Pokemon.

---
## Wild Pokemon

### Wild Pokemon

- CHANGED: Allowed Randomized Wild Pokemon
    - Unown is no longer allowed to be a randomized wild Pokemon in the Generation 2 and 4 games.
        - In the Gen 2 games and HG/SS, this is because Unown will never appear in the wild until the player solves at least one of the puzzles within the Ruins of Alph. In D/P/Pt, this is because not all letters are accessible outside of the Solaceon Ruins.

- CHANGED: Catch Em All Mode
    - When this setting is enabled, it will now no longer randomize encounters for Pokemon that are not allowed to be a randomized wild Pokemon.
        - This currently only applies to Unown in Gen 2, FR/LG, and Gen 4. As an example, enabling "Catch Em All Mode" in HG/SS will make it so the Ruins of Alph interior encounters are *not* randomized; they will all remain Unown.

- CHANGED: Set Minimum Catch Rate
    - The slider for this setting now goes up to 5. Setting the slider to 5 will enable guaranteed catches, ensuring that every Pokemon is guaranteed to be caught by any ball, so long as they are catchable in the first place. For more information on this setting, see our [wiki page about it](https://github.com/Ajarmar/universal-pokemon-randomizer-zx/wiki/Wild-Pokemon#minimum-catch-rate-odds).

---
## Misc Tweaks

- **NEW SETTING**: Update Rotom Appliance Typings
    - Platinum/HeartGold/SoulSilver: This setting updates the typings of Rotom's alternate formes to match the typings they have in Generation 5 and onwards.
        - For example, Wash Rotom will change from Electric/Ghost to Electric/Water.

- **NEW SETTING**: Disable Low HP Music
    - Black/White/Black 2/White 2: This setting disables the music that plays when one of the player's Pokemon is at low HP in battle.