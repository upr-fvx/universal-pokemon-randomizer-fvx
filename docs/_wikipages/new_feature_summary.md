---
name: New Feature Summary
---
This page summarizes all new features added in this fork (compared to UPR ZX).

UPR FVX also tweaks a number of features that already exist in ZX, and fixes numerous bugs. 
To keep this page relatively short, those changes have been left out.

# Evolutions

## Random Every Level
"Random Every Level" is now supported in Gen 1 and Gen 2.

## Force Growth
Ensures that random evolutions will always be to a Pokemon with a higher base stat total. (No guarantee on individual stats.)

## No Convergence
Ensures that each Pokemon has only one Pokemon that evolves (directly) into it.

# Starter Pokemon

## "Any basic Pokemon"
Allows you to choose as starters any Pokemon which is not an evolved form, regardless of how many times it evolves (0-2).

## Starter Type Restrictions
A set of type restrictions for starter Pokemon, including:
* None : Any type may be chosen
* Fire, Water, Grass : One pokemon of each of the three classic starter types. The new starters are placed in their original corresponding slots, so any other game features that depend on your starter choice (e.g. the Trio gym) should follow the element of the starter you chose.
* Any type triangle : Chooses any three types that form a triangle such that each is super-effective to the next. (Does not allow "triangles" of all the same type, such as Ghost or Dragon.) Assigns the triangle in order, so if you have "Rival keeps starter" checked in Trainer Pokemon, your rival should correctly have a type that is super-effective against yours.
* Unique : Ensures no starter shares a type with any other starter. Also works if you custom-choose some starters and assign the others randomly (excepting that it does not prevent your custom chosen starters from sharing types with each other.)
* All one type : Ensures every starter chosen shares a type. This type can be chosen randomly, or you may choose manually. Can be used with random pokemon in custom sets.
* No dual types : Can be selected along with any other type restriction, including "None". Ensures that all starters are single-type Pokemon. (This ensures that type triangles are not broken or confused by a second type.) Makes no guarantees about their evolutions.

## Base Stat Total limits
Allows setting a maximum and/or minimum base stat total for starters.

# Trainer Pokemon

## Use Local Pokemon
Ensures that all Pokemon used by trainers are those that can be found in the wild during the main game (pre-Elite Four), or evolutionary relatives of those Pokemon.  
If "Pokemon League has Unique Pokemon" is checked, the unique Pokemon will be _non_-local Pokemon if possible.

## Preserve Type Themes
If a trainer had a type theme in the original game (a type that _all_ their Pokemon share), this setting ensures that they will have that same type theme after randomization.  
If the trainer did not have a type theme, they will have random Pokemon of any types.  
If the trainer has two type themes, they will follow only the theme established by their first Pokemon's primary type—unless it's Normal, in which case they will use the secondary type. (I call this the "Bird Override", since it mainly ensures that Bird Keepers use _Flying_ rather than Normal.)  
If the trainer is a Gym Leader, gym trainer, Elite Four, or themed Champion, they will be forced to their theme/their Gym's theme even if they did not strictly follow it in the original game.  

## Preserve Themes Or Primary
The same as Preserve Type Themes, except if the trainer does not have a type theme, each of their Pokemon will share the primary type of the Pokemon the trainer originally had.  
For example, if the trainer originally had an Oddish (Grass-Poison), after randomization it might be a Tangela (pure Grass), an Exeggcute (Grass-Psychic), or a Paras (Bug-Grass). However, it could not become a Zubat (Poison-Flying) or a Gastly (Ghost-Poison) as Poison is the _secondary_ type.  

## Add Pokémon
"Add Pokémon" was a feature in ZX as well, but FVX makes it possible to add Pokémon to trainers without randomizing the rest of their teams. Added Pokémon are random, but follow their trainer's Type theme (if they have one), and can be restricted using the usual randomization options.

## Battle Style Randomization
"Battle Style Randomization" replaces "Double Battle Mode". Setting all Trainers to be double battles is still possible, but it also allows setting all trainers to triple or rotation battles (in Gen 5+6), and setting each Trainer to use a random battle style of the ones available in the game. It is also possible to randomize the Battle Styles without randomizing the trainer teams. If a trainer has fewer Pokémon that their assigned Battle Style requires (e.g. a trainer with 1 mon turned into a Double Battle), their existing Pokémon are duplicated.

## Force Middle Stage
Works similar to "Force Fully Evolved"; a level is chosen in the GUI, and any Trainer Pokémon at that level or higher will be forcibly evolved, if they have a middle stage to evolve into. **E.g.**, if the level is set to 20, lv24 Bellsprout and lv40 Dratini, will instead become lv24 Weepinbell and lv40 Dragonair. Lv19 Charmander and lv21 Rattata will be untouched.

# Wild Pokemon

* Catch-Em-All and Similar Strength have been moved to checkboxes, allowing them to be used at the same time.

## Location 1-to-1

Replaces each Pokemon in a conceptual "location" with exactly one other Pokemon.
A location is, more or less, a named area. It may span multiple maps.
When using this mode, random themes and keep themes are run on the whole location.

## Global Family-to-Family

Replaces each Pokemon, everywhere it is found, with exactly one other Pokemon; also replaces each of its evolutions and pre-evolutions with the evolutions and pre-evolutions of the replacement Pokemon.

## Preserve Primary Types

This setting ensures that every wild Pokemon will be replaced by a Pokemon (or several Pokemon) that shares its primary type.
For example, if the encounter originally was an Oddish (Grass-Poison), after randomization it might be a Tangela (pure Grass), an Exeggcute (Grass-Psychic), or a Paras (Bug-Grass). However, it could not become a Zubat (Poison-Flying) or a Gastly (Ghost-Poison) as Poison is the _secondary_ type.
This setting usually keeps most type theming, including "soft" type theming like forests having both Bug and Grass types.  However, sometimes it has odd results, such as flying ambush encounters becoming Normal types.
This option can be used along with global 1-to-1 and family-to-family.

## Preserve Type Themes

If an area had a type theme in the original game (a type that _all_ Pokemon found in the area share), this option ensures that type theme will be present after randomizing.  
For the purposes of this setting, an "Area" is a specific set of encounters that can all be found by performing the same action.  
For example, on Route 32 in Johto, the tall grass, Headbutt trees, fishing, and surfing are all _different_ areas. (Depending which game, fishing might also be separated into different areas by rod.)  
This option therefore generally keeps water areas full of Water Pokemon, and doesn't have any effect in most other areas, with rare exceptions.  
This option is a checkbox, and overrides other type restrictions.
This option can be used along with global 1-to-1 and family-to-family.

# Items
Added support for Gen 1 and Gen 2 Shops.

## Add Cheap Rare Candies
Adds Rare Candies for 10¥ to most shops. Can be used without randomizing shop items.

# Graphics
## Pokemon Palette Randomization

Allows randomizing the color schemes for Pokemon.
The new color schemes may be logically chosen based on types or evolutions, or completely random.
Support varies by generation: 
- Gens 1-3 have full support.
- Gen 4 has partial support, all base form Pokémon have randomizable palettes, but alt forms do not.
- Gen 5 has partial support, not all base form Pokémon have randomizable palettes.
- Gens 6 & 7 do not support this feature. 

## Custom Player Graphics

In Gen 1-3 games, allows replacing the player's sprites with new sprite sets.
Several preset possibilities are included.
Not supported for Gen 4 and up.

# Types
* Moved "Update type effectiveness" to the new Types tab.

## Type Effectiveness randomization
Allows changing the effectiveness of types against each other in various ways:
### Random
Shuffles all weaknesses, resistances, and immunities to random type pairs. The end result has the same amount of total weaknesses, resistances, and immunities as the base game, but different amounts per type.
### Random (Balanced)
Same as the above, but puts limits on how many of each can be associated with each type offensively or defensively; the limit is the highest number of these any type in the vanilla game has.
### Keep Type Identities
Each type will have the same number each of weaknesses, resistances, and immunities as in the base game, but these will be to different types.
### Inverse
Every weakness will become a resistance and every resistance or immunity will become a weakness. Optionally, some of the weaknesses may become immunities instead of resistances.

# Misc. Tweaks

## Reusable TMs
For generations earlier than 5, changes TMs so they don't disappear after use.

## Forgettable HMs
For generations 1-5, lets HMs be forgotten like any other move, and not just at the Move Forgetter.

# Misc.

## Improved Logging
The log file output after randomizing a game, has been entirely revamped. New info has been added in places, and it is now more structured. The strings composing the log file have also been separated from the code, to make translations of the Randomizer into other languages easier.