---
layout: default
title: About
---
## About the Universal Pokémon Randomizer

The **Universal Pokémon Randomizer** (**UPR**) is a randomizer for Pokémon games, originally released by Dabomstew in 2012. Like other randomizers, it can shuffle around and change the data of the target game to your liking. The Randomizer also contains various quality-of-life features, like turning off trade evolutions or choosing exactly which Pokémon to have as your starter. All randomizations are optional, so you can change the game as little or as much as you like.

### What games can the UPR randomize?

All versions of the UPR can randomize core series games from Generation 1-5, from Red/Green to Black 2/White 2. All versions based on UPR ZX, including the FVX branch linked to by this site, also support randomizing Generation 6 and Generation 7 games.

The UPR does not support ROM hacks, but it does support all official localizations of the games it supports (except Korean Gold/Silver). ROM hacks *may* still work with the UPR in practise, especially if they are old or only make minor changes to the game. However, this is done at own risk.

### What can the UPR randomize in the game?

- Pokémon traits
	- What base stats Pokémon have
	- The types of Pokémon
	- Pokémon abilities
	- What each Pokémon evolves into
- Starters, Statics, and Trades
	- What Pokémon are available as starters. You may also set the starters to any Pokémon in the game, so you can play through the game with your favorite 'mon.
	- "Static" Pokémon, like legendary encounters, gifts, and ones you buy at the Game Corner
	- What Pokémon are available in in-game trades, and what Pokémon are requested
- Moves and movesets
	- The power, accuracy, PP, and type of each move
	- The category (physical or special) of each move (Gen 4+)
	- What moves Pokémon learn by leveling up
	- What egg moves each Pokémon has
- Foe Pokémon
	- What Pokémon each Trainer uses
	- The held items of Trainer Pokémon
	- Trainer names and trainer class names  
	- Totem Pokémon (Gen 7 only)
- Wild Pokémon
	- What Pokémon can be encountered in each area
	- The held items of wild Pokémon
- TM/HMs & Tutors 
	- The power, accuracy, and type of each move
	- What moves are available as TMs
	- What TMs are compatible with which Pokémon
	- What moves are available as Move Tutors
	- What Move Tutors are compatible with which Pokémon
- Items
	- What items can be found in item balls, and as hidden items
	- What items are sold in shops
	- What items can be found by Pickup
- The strengths and weaknesses of types (only in FVX)
- The colors of Pokémon (only in FVX)

### Seeds and logs

Each time the Randomizer is used, it outputs not only the randomized ROM, but also the random "seed" that was used and a string representing the settings. By inputting this same seed and settings, users can ensure the same randomization results every time, enabling consistent experiences and sharing Randomizer results with others. This feature is particularly useful for races and other community playthroughs, where uniformity is key.

The UPR can also log its randomization, so users can look up details on the randomized game. For races, this feature may be turned off to avoid spoilers.

## About the FVX branch

The **Universal Pokémon Randomizer FVX** (**F**ox + **V**oliol + z**X**) is a continuation of the Universal Pokémon Randomizer. It was born of a merge of branches by [foxoftheasterisk](https://github.com/foxoftheasterisk/UPR-ZX-closer-to-vanilla) and [voliol](https://github.com/voliol/universal-pokemon-randomizer), both based on [Ajarmar's UPR ZX](https://github.com/Ajarmar/universal-pokemon-randomizer-zx).

Compared to ZX, FVX adds a number of features; from upgrades to Trainer and wild Pokémon randomization, to Pokémon Palette randomization and Custom Player Graphics. For a full list of new features, see [this wiki page]({{ site.baseurl }}/wikipages/new_feature_summary.html).

True to its ancestry in ZX, it supports all vanilla core series Pokémon games from Generation 1-7 except Let's Go, Pikachu!/Eevee!; in other words, it supports all core series games for the GameBoy, GameBoy Color, GameBoy Advance, Nintendo DS, and Nintendo 3DS.

For developers, FVX also has a considerable amount of refactoring and new features, including separate Randomizer classes for each category of randomization, a SpeciesSet class with many helper functions, and automated tests for most features.

## Further reading

- [History of the Randomizer]({{ site.baseurl}}/about_history.html)
- [Other Pokémon randomizers and UPR branches]({{ site.baseurl}}/about_other_randomizers.html)