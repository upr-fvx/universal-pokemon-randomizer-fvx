# Changes - v4.2.0

---
## General

- **IMPORTANT:** New Launcher
    - The launcher is no longer distributed as a jar file, but instead as a script. Use the launcher by double clicking the script meant for your operating system:
        - Windows: Use `launcher_WINDOWS.bat`
        - Mac: Use `launcher_MAC.command`
        - Other Unix-based systems: Use `launcher_UNIX.sh`

- **NEW FEATURE:** CLI Randomizer
    - For advanced users. Allows for creating randomized games from the terminal instead of using the GUI. See [this wiki page](https://github.com/Ajarmar/universal-pokemon-randomizer-zx/wiki/CLI-Randomizer) for more information.
        - Thanks to @sjb9774 for this addition!

- ADDED SUPPORT: Professor Intro Pokemon
    - The Pokemon that the Professor sends out in the game's intro is now also randomized in the following games:
        - Diamond/Pearl/Platinum
        - Black/White/Black 2/White 2
        - X/Y
    - In Heart Gold/Soul Silver, Ethan/Lyra's Marill is randomized instead.

- CHANGED: Logs
    - Trainers now have their original names in the log, even if their names are randomized.
    - If an evolution is changed both by "Change Impossible Evolutions" and "Make Evolutions Easier", this is made more clear in the log.

- CHANGED: Legendary Pokemon
    - For settings that concern Legendary Pokemon, Rotom is no longer considered Legendary. Type: Null and Silvally are now considered Legendary.

- FIX: Output File Name Issue
    - Fixed an issue where specifying a file name for the output ROM that also occurred in the file path would cause an error (for example, trying to save an output ROM named "asdf" in the file path "D:\asdf\randomizer")

- FIX: Unclickable GUI Elements
    - Fixed a GUI resizing issue that could occur depending on the user's screen resolution, which made some GUI elements unclickable.

- FIX: Pokemon White (French)
    - Fixed an issue where randomizing Pokemon White (French) produced a game that crashed immediately.

---
## Pokemon Traits

### Pokemon Base Statistics

- FIX: Follow Mega Evolutions

    - Fixed an issue with this setting where it would not work in conjunction with "Shuffle" of base stats.

### Pokemon Abilities

- CHANGED: Ban Negative Abilities
    - This setting no longer bans abilities that are completely useless; these abilities are instead always banned when randomizing abilities. These abilities are:
        - Cacophony (Generation 3 only), Forecast, Multitype, Zen Mode, Stance Change, Shields Down, Schooling, Disguise, Battle Bond, Power Construct, RKS System

- CHANGED: Ban Bad Abilities
    - This setting no longer bans good double battle abilities if double battle mode is on. These abilities are:
        - Friend Guard, Healer, Telepathy, Symbiosis, Battery

### Pokemon Evolutions

- FIX: Change Impossible Evolutions
    - In Generation 3 games, Clamperl's Gorebyss evolution will now correctly require a Water Stone instead of being unavailable.

---
## Starters, Statics & Trades

### Starter Pokemon

- FIX: Starter Graphics
    - The sprite displayed when picking a starter is now fixed in the following games:
        - Diamond/Pearl/Platinum
        - Black/White/Black 2/White 2 (sprite shadow fixed)

- FIX: Starter Cries
    - The cry that plays when picking a starter is now fixed in the following games:
        - Heart Gold/Soul Silver
        - Black/White/Black 2/White 2

### Static Pokemon

- **NEW SETTING:** Fix Music
    - Fixes the music for Static Pokemon that are supposed to have special music (typically legendaries), so that the correct music plays even when they are randomized. 
        - Note that in Generation 4/5, the special music will also play if you find the randomized Pokemon in the wild. 
        - Available in Generation 3-5 (except Ruby/Sapphire, where this already works natively) - only for English versions, except for Heart Gold/Soul Silver where it is available for all languages.

- ADDED SUPPORT: Randomized Static Pokemon
    - The following Static Pokemon are now also randomized:
        - Ruby/Sapphire: The Poochyena battled after picking a starter is now randomized. (English version only)
        - Ruby/Sapphire: The roaming Latios/Latias is now randomized.
        - Fire Red/Leaf Green: The Ghost Marowak is now randomized. (English version only)
        - Fire Red/Leaf Green: The roaming Raikou/Entei/Suicune is now randomized. (English version only)
        - Emerald: The Zigzagoon battled after picking a starter is now randomized. (English version only)
        - Emerald: The roaming Latios/Latias is now randomized.
        - Diamond/Pearl: The Starly battled after picking a starter is now randomized.
        - Diamond/Pearl: The roaming Mesprit and Cresselia are now randomized. (English version only)
        - Platinum: The roaming Mesprit, Cresselia and Articuno/Zapdos/Moltres are now randomized. (English version only)
        - Heart Gold/Soul Silver: The roaming Raikou, Entei, and Latios/Latias are now randomized. (English version only)

- ADDED SUPPORT: Percentage Level Modifier
    - This setting is now also available for Generation 1-2 games.

- CHANGED: Crystal Ho-Oh Requirements
    - In order to get access to Ho-Oh in Crystal, if Static Pokemon are randomized, Eusine will check that you have caught the Pokemon that Raikou/Entei/Suicune were randomized into, rather than checking for Raikou/Entei/Suicune themselves.

- FIX: Giratina (Platinum)
    - Giratina in Platinum can now be randomized into anything, instead of just a limited subset of Pokemon.

- FIX: Bulbasaur (Yellow)
    - The static Bulbasaur in Yellow can now be received even if starters are randomized.

---
## Moves & Movesets

### Move Data

- FIX: Update Moves to Generation
    - Fixed an issue where using this setting in Generation 4-7 games incorrectly would set Dig's base power to 60.
    - Fixed the following moves not being updated in Generation 3 games when updating to Generation 4+:
        - Stockpile, Dive, Leaf Blade

### Pokemon Movesets

- CHANGED: Random (preferring same type)
    - This setting now attempts to do some power balancing when picking moves of the same type as the Pokemon; types that have very many non-damaging/low-damage moves gain a slightly boosted chance of getting higher-damage moves, and vice versa for types that have very many high-damage moves.

- CHANGED: Banned Levelup Moves
    - Transform is no longer banned from appearing as a levelup move in Generation 3 and up.

- CHANGED: Good Damaging Moves
    - The following moves are no longer considered "Good Damaging Moves":
        - Spit Up, Synchronoise, Foul Play, Shell Trap
    - When picking "Good Damaging Moves", the randomizer also considers the Pokemon's Attack/Special Attack distribution (a Pokemon with higher Attack will have a higher probability of getting a physical move, and vice versa).
    - The percentage specified by "Guarantee Good Damaging Moves" is now actually the guaranteed percentage of "Good Damaging Moves", rather than just being a probability.

- FIX: Good Damaging Moves
    - Fixed an issue where perfect accuracy moves would not be considered "Good Damaging Moves" in Generation 1-4.

---
## Foe Pokemon

### Trainer Pokemon

- **NEW SETTING:** Add Held Items to...
    - These settings allow for adding held items to Trainer Pokemon. Thanks to @spaceonaut for these additions!
    - Boss Trainers
        - Gives held items to "Boss" Trainers (gym leaders, team leaders, elite four, champion, etc.)
    - Important Trainers
        - Gives held items to "Important" Trainers (rivals, important story battles).
    - Regular Trainers
        - Gives held items to all other Trainers that don't fit into either of the previous categories.
    - Consumable Only
        - This setting restricts the given held items to only be items that are consumed on use, such as berries, Focus Sash, Mental Herb, etc.
    - Sensible Items
        - This setting uses some logic to place the items, for example only giving Choice Band to Pokemon that have physical moves or only giving weakness-reducing berries to Pokemon that are weak to that berry's specific type.
    - Highest Level Only
        - Only gives a held item to the Trainer's highest level Pokemon.

- ADDED SUPPORT: Double Battle Mode
    - This setting is now also available in Generation 3 games, for all Trainers.
    - In Generation 4/5 games, this setting now applies to all Trainers instead of only important/boss Trainers.

- ADDED SUPPORT: Additional Pokemon for...
    - This setting is now also available in Generation 3 games.

- CHANGED: Random (even distribution)
    - When this setting is used in conjunction with "Similar Strength" for Trainers, it will try a bit harder to actually pick Pokemon with similar strength (at the expense of Pokemon being slightly less evenly distributed).

- CHANGED: Randomize Trainer Names
    - When this setting is used, Trainers with the following Trainer names will now be given different names instead of all having the same name: Grunt, Executive, Shadow, Admin, Goon, Employee. (Was previously just Grunt and Executive.)
        - Thanks to @wwtepicfail for this change!

- FIX: Minior Forme Issue
    - Fixed an issue where Trainers could be given an illegal forme of Minior, crashing the game.

- FIX: Type Themed
    - This setting now works in Generation 1 games again.

---
## Items

### Field Items

- ADDED SUPPORT: Mega Stone Sparkles
    - In Omega Ruby/Alpha Sapphire, the Mega Stone sparkles that appear on the ground after a certain point in the game are now randomized when field items are randomized.

- FIX: Bad Items
    - Fixed an issue where using "Ban Bad Items" settings in Omega Ruby/Alpha Sapphire also would exclude all new items that were introduced in those games.

### Special Shops

- CHANGED: Balance Shop Prices
    - If this setting is NOT used, Mega Stones will be limited to one occurrence among field items. (This is because Mega Stones cannot be sold if this setting is not used, making duplicates largely useless.)

- FIX: Randomized Special Shop Infinite Saving Loop
    - Fixed an issue where using the "Random" setting for Special Shops on non-English versions of Generation 4 games would cause an infinite saving loop in the randomizer.

---
## Misc Tweaks

- **NEW SETTING:** Faster HP and EXP Bars
    - Generation 4 only. Doubles the speed of HP and EXP bars, which makes them the same speed as bars in Generation 3.

- ADDED SUPPORT: Fastest Text
    - This setting is now also available for Diamond/Pearl. (English version only)

- CHANGED: Give National Dex at Start
    - When this setting is used in Omega Ruby/Alpha Sapphire, allows the Pokédex to be opened without having to catch a Hoenn Dex Pokemon first.
    - When this setting is used in Diamond/Pearl/Platinum, you now need to speak to Rowan a second time after receiving the Pokédex to get the National Dex. This is to make it so that you also can get the PokéRadar.

- FIX: Randomize Catching Tutorial
    - Fixed an issue where this setting could softlock the game in Heart Gold/Soul Silver.

- REMOVED: Randomize Hidden Hollow Pokemon
    - This setting has been removed; randomization of Hidden Grotto Pokemon in Black 2/White 2 is now incorporated into randomization of Static Pokemon.
