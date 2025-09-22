# Changes - v4.4.0

---
## General

- FIX: 3DS Game Updates
    - Fixed an issue where 3DS game updates with an invalid header but valid contents were not recognized by the randomizer.

- FIX: Using the Randomizer with Certain Characters in the File Path
    - Fixed an issue where the randomizer wouldn't work properly if the "+" character was located anywhere in the file path.

---
## Pokemon Traits

### Pokemon Types

- **NEW SETTING**: Force Dual Types
    - When this setting is enabled, all Pokemon will have two types when Pokemon types are randomized.
        - Thanks to @Corruption-Echoes for this addition!

---
## Moves & Movesets
 
### Move Data

- FIX: Update Moves to Generation
    - Gen 5: Fixed an issue where Storm Throw's power was incorrectly set to 60 instead of 45 when moves were updated to Generation 6 or higher.

---
## Foe Pokemon

### Trainer Pokemon

- FIX: Randomize Trainer Names
    - Gen 2: Fixed an issue where custom trainer names with "9" in them would cause glitchy behavior. The "9" character is now silently stripped from names.

---
## Misc Tweaks

- FIX: Force Challenge Mode
    - Fixed an issue where this setting did not function properly.
