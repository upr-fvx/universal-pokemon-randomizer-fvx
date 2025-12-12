# Changes - v4.5.0

---
## General

- CHANGED: ROM Validity Checking
    - Changed the text that is displayed when an unofficial ROM is loaded into the randomizer to be more clear.

- CHANGED: Bad Item Lists
    - Updated Generation 3/4 bad item lists to be consistent with bad item lists in Generation 5-7.
    - Added more useless items (e.g., Shards and Shoal Salt/Shell in games where they have no use other than being sold cheaply) to the bad item lists.
    - For more information, see the [Bad Items](https://github.com/Ajarmar/universal-pokemon-randomizer-zx/wiki/Items#bad-items) page on the wiki.

- FIX: CLI Randomizer
    - Fixed an issue where the Generation 7 games required an update file to be supplied to use the CLI randomizer.
 
---
## Pokemon Traits

### Pokemon Base Statistics

- FIX: Shuffle/Random
    - FR/LG and Emerald: Fixed an issue where Deoxys' stats would not be changed when either of these options were selected.

### Pokemon Abilities

- **NEW SETTING**: Ensure Two Abilities
	- This setting forces all Pokemon to have two abilities (not counting hidden abilities), instead of some Pokemon having only one.
		- Thanks to @TricksterGuy for this addition!

---
## Starters, Statics & Trades

### Starter Pokemon
- CHANGED: Custom
    - This setting now has a "Random" option at the top of the list that can be used if you don't want to specify all of the starters.
        - Thanks to @Zach-Meadows for this change!

### Static Pokemon

- ADDED SUPPORT: Randomized Static Pokemon
    - X/Y: The Pokemon hiding in trash cans in the Lost Hotel and in Pokemon Village are now randomized.

---
## Moves & Movesets

### Pokemon Movesets

- CHANGED: Random (preferring same type)/Random (completely)
    - These settings now also randomize a Pokemon's egg moves.

- FIX: Evolution Moves for All Pokemon
    - Fixed an issue where this setting could be activated in games that do not support it.

---
## Foe Pokemon

### Trainer Pokemon

- **NEW SETTING**: Type Themed (Elite Four/Gyms Only)
    - This setting makes it so that the Elite Four and Gym Trainers will stick to a particular type, similar to the existing Type Themed setting, but all other trainers will *not* have an assigned type and are free to use whatever Pokemon they want.
        - To make room for this setting, the radio buttons that control trainer randomization have been replaced with a single combo box.

- **NEW SETTING**: Better Movesets
    - This setting attempts to give Trainer Pokemon better movesets by selecting moves that synergize with their ability, typing, stats, other moves, etc. These moves are selected from all possible moves a Pokemon can learn, including TM moves, tutor moves, egg moves, and pre-evolution moves.
	- When this setting is enabled, each Trainer Pokemon's moveset will be shown in the log.
    - This setting is available in Generations 3-7.

- **NEW SETTING**: Pokemon League Has Unique Pokemon
    - This setting gives the Elite Four and Champion (including B/W N and Ghetsis) unique Pokemon that cannot appear on any other Trainer.
        - The number of unique Pokemon per Trainer can be controlled by the spinner next to the setting (the maximum value is 2).
        - The highest-level Pokemon are the ones that will be unique.
        - This setting does not apply to postgame rematch battles.
	- This setting is available in Generations 3-7.

- CHANGED: Randomize Trainer Names/Randomize Trainer Class Names
    - B2/W2: The trainer names and classes for the Driftveil Tournament are now randomized by these settings.

- FIX: Add Held Items to...
    - B2/W2: Fixed an issue where, under extremely rare circumstances, enabling this setting could cause a crash in the Driftveil Tournament.

- FIX: Force Fully Evolved
    - Fixed an issue where, when this setting was enabled at the same type as Type Themed Trainers, Pokemon could be selected for Trainers in such a way that they would be forcibly evolved into the incorrect type.

- FIX: Type Themed
	- HG/SS: Fixed an issue where two trainers in the Ecruteak Gym would not follow the gym's type theme.

---
## Misc Tweaks

- **NEW SETTING**: Fast Distortion World
    - Platinum: This setting makes it so the portal on Spear Pillar's summit warps you directly to the Cyrus fight at the end of the Distortion World.

- FIX: Give National Dex at Start
    - B2/W2: Fixed an issue where turning on this setting would allow you to run away from Bianca before obtaining your starter.
        - Thanks to @SilverstarStream for this fix!

- FIX: Lower Case Pokemon Names
    - Fixed an issue where "FARFETCH'D" would be changed to "Farfetch'D". It is now correctly set to "Farfetch'd".
