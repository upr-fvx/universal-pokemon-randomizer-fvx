# Changes - v4.3.0

---
## General

- **NEW SETTING**: No Irregular Alt Formes
    - This new global setting allows you to disable alternate formes that cannot normally exist outside of battle (for example, Mega Evolutions or Zygarde Complete) from appearing in the pool of available Pokemon when the "Allow Alternate Formes" settings are enabled.

- CHANGED: ROM Validity Checking
    - The randomizer will now attempt to verify that a loaded ROM is a clean, official version of the game. If a bad dump or ROM hack is supplied, the randomizer will still attempt to load it, but it will display a warning dialog.
    - As part of this change, only the latest game updates for the 3DS games (Ver 1.5 for X/Y, Ver 1.4 for OR/AS, and Ver 1.2 for the Gen 7 games) are allowed to be loaded into the randomizer. **Unpatched 3DS games are still fully supported**; this change only affects which game updates can be loaded.

- CHANGED: Limit Pokemon Window
    - This window is now simpler, which should make its handling of evolutionary lines that cross multiple generations (e.g., the Magmar line) easier to understand.

- CHANGED: Logs
    - Trainers now have both their original name *and* their randomized names in the logs, assuming that trainer name and/or trainer class name randomization is enabled.
    - When the "Update Base Stats to Generation" setting is enabled, these stat changes are logged even when base stats are not shuffled or randomized.

- FIX: Launcher Scripts
    - The launcher scripts now use a slightly larger Java heap size; this should hopefully help prevent the randomizer from freezing up when loading or saving certain 3DS games.
    - The Windows launcher script now works when the randomizer is located on a network drive.

- FIX: Random Crashes When Outputting 3DS Games as CXI
    - Fixed an issue where the 3DS games, when output as a CXI, can occasionally crash randomly.

- FIX: Professor Intro Pokemon
    - Diamond/Pearl/Platinum: Fixed an issue where certain intro Pokemon could cause the game to crash on real DS hardware.

- FIX: Vietnamese Crystal Stability
    - Multiple fixes were implemented to make randomizing Vietnamese Crystal more stable.
         - Thanks to @pidgezero-one for these fixes!

- FIX: Non-English Black/White Crashing
    - Fixed an issue where certain non-English versions of Black/White would crash the randomizer when loaded.

- FIX: Gen 1/2 Japanese Text Handling
    - Fixed an issue where "ォ" was used instead of "オ".

---
## Pokemon Traits
 
### Pokemon Base Statistics
 
- **NEW SETTING**: Randomize Added Stats on Evolution
    - When this setting is enabled, it changes how "Follow Evolutions"/"Follow Mega Evolutions" works; when a Pokemon evolves, the additional BST it gains will be assigned randomly on top of the pre-evo's stats instead of the evolved Pokemon having the exact same ratio between its stats as the pre-evo.

- CHANGED: Follow Evolutions/Follow Mega Evolutions
    - When using these settings, split Evolutions/Mega Evolutions will now have the additional BST they gain assigned randomly on top of the pre-evo's stats instead of having completely random base stats.

- FIX: Random
    - Gen 1: Fixed an issue where randomizing base stats would cause a Pokemon's base stat total to change.

- FIX: Update Base Stats to Generation
    - Gen 7: Fixed an issue where Turtonator's base stats were accidentally affected by this setting.

### Pokemon Abilities

- CHANGED: Useless Abilities
    - Flower Gift is now considered a useless ability and will thus be always banned when randomizing abilities.

### Pokemon Evolutions

- **NEW SETTING**: Random Every Level
    - This feature forces every single Pokemon (regardless of their evolutionary stage in the original game) to evolve every level by replacing all of their evolutions with a single Level 1 evolution.
         - Enabling this feature will disable all "Follow Evolution" settings in the randomizer.

- FIX: Random
    - Gen 7: Fixed an issue where randomizing evolutions could cause the randomizer to crash under very rare circumstances.

---
## Starters, Statics & Trades

### Starter Pokemon

- FIX: Custom
    - Fixed an issue where alternate forme custom starters would be reverted back to their base forme when loading a settings string or file.

### Static Pokemon

- ADDED SUPPORT: Randomized Static Pokemon
    - The following Static Pokemon are now also randomized:
        - X/Y: The roaming Pokemon as they roam. These were already correctly randomized when they came to rest in the Sea Spirit's Den, but now they appear randomized when roaming as well.
        - Gen 7: The Pokemon created by assembling Zygarde Cells in the lab on Route 16.
            - Note that assembling the cells will result in the same kind of Pokemon regardless of how many cells are used.

- ADDED SUPPORT: Percentage Level Modifier
    - Gen 5: The fake ball Static Pokemon are now affected by this setting.

- FIX: Crystal Odd Egg Movesets
    - Fixed an issue where the Odd Egg Pokemon would still use their original moveset when Static Pokemon were randomized.

### In-Game Trades

- FIX: In-Game Trade Text
    - Fixed multiple issues in X/Y and the Gen 7 games where in-game trade text displayed the wrong requested and given Pokemon.

---
## Foe Pokemon

- FIX: Type Themed
    - Fixed an issue where this setting could crash the randomizer when combined with "Limit Pokemon" and "Swap Mega Evolvables".
        - If there isn't a mega evolvable of every type in the pool (as determined by your "Limit Pokemon" settings), then "Swap Mega Evolvables" will be disabled for type-themed trainers.

---
## Wild Pokemon

- FIX: Randomize Held Items
    - Generations 6 and 7: Fixed an issue where Pokemon could be given an item that is impossible to obtain.

---
## Items

### Special Shops

- ADDED SUPPORT: Gen 3 Special Shops
    - Special Shop randomization is now available in Generation 3 games.

### **NEW FEATURE**: Pickup Items
- This feature allows you to randomize which items are obtainable via the Pickup ability.

---
## Misc. Tweaks

- **NEW SETTING**: Force Challenge Mode
    - This setting allows you to forcibly enable Challenge Mode in Black 2/White 2.
        - Enabling this setting will prevent you from accessing Easy Mode or Normal Mode entirely.

- CHANGED: Give National Dex at Start
    - FireRed/LeafGreen: When this setting is enabled, Oak's Aides will now use how many Pokemon in the National Dex you've caught to determine whether or not to give you an item rather than only caring about how many Pokemon in the Kanto Dex you've caught.

- CHANGED: Update Type Effectiveness
    - This setting is now available for all games from Gens 2-5. When enabled, it removes Steel's Ghost and Dark resistances to match the type chart from Gen 6 onward.

- FIX: Lower Case Pokemon Names
    - FireRed/LeafGreen: Fixed an issue where the certain text involving the starters was not updated when this setting was enabled.