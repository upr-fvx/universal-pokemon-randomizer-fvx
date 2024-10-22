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

## History of the Randomizer
December 12, 2012, Dabomstew released the first version of the Universal Pokemon Randomizer. It was not the first randomizer of its kind, Artemis251's Emerald randomizer among others (see [Acknowledgements]({{ site.baseurl }}/acks.html)) had already broken the ground of randomizing Pokémon games. What made the UPR stand out was its goal of being able to randomize all then-released Pokémon games, from Gen 1 to Gen 5. Thus, the *Universal* Pokémon Randomizer.

May 15, 2014, in conjunction with the release of UPR 1.6.1, the Randomizer became open source, [hosted on GitHub](https://github.com/Dabomstew/universal-pokemon-randomizer). 

October 21, 2016, Dabomstew stopped working on the project, passing the torch to the community to continue its development and maintenance. This lead to the birth of a multitute of Randomizer branches. Since this website is primarily for the FVX branch, the below history will focus on its lineage. However, this lineage is not the only one. Some notable other branches are mentioned in the section below.

May 8, 2019, the first version of the Universal Pokémon Randomizer ZX was released. The UPR ZX is a significant branch that emerged from the original UPR, developed by Ajarmar with significant contributions from darkeye and cleartonic. It added a slew of new features and rebalancing, with the most notable perhaps being support for the 3DS games (Gen 6 & 7). Despite its popularity, the development of the ZX branch eventually ceased. The last update was released February 12, 2023, marking the end of an era.

August 6, 2022, voliol released the first version of a nameless branch, later "UPR ZX V branch". This branch had some focus on graphical features, and also contained considerable refactoring. Initially, the branch was based off brentspector's branch, but later "moved" to ZX.

Dec 1, 2023, foxoftheasterisk began work on a branch focused on adding features to control the randomization, called "Closer To Vanilla". Although it was originally intended to implement features to be added to the ZX branch rather than have its own releases, she eventually released a single version on May 17, 2024.

July 11, 2024, collaboration between foxoftheasterisk and voliol led to a release of their branches merged (and then some): the Universal Pokémon Randomizer FVX.

## Other Pokémon randomizers and UPR branches

As mentioned above, the UPR is not the only tool for randomizing Pokémon games, and the FVX branch is not its only branch. 

Below, a small selection of other randomizers and branches are listed. They have been chosen for being notable and filling niches that the UPR or the FVX branch don't. However, there are more! See [The BIG List of Video Game Randomizers](https://randomizers.debigare.com/) for more non-UPR randomizers, and browse through forks on GitHub to find unmentioned branches. 

### Randomizers

- **[The Crystal Randofuser](https://github.com/xCrystal/crystal-randofuser)** (by xCrystal) is a randomizer for Pokémon Crystal, where Pokémon lines are fused when randomized. You no longer have Abra-Kadabra-Alakazam, Chikorita-Bayleef-Meganium - you have Chikabra-Baydabra-Megakazam! Or the like. 
- **[Sanqui's Online Randomizer](https://sanqui.net/randomizer/)** (by Sanqui) is not only an online randomizer for Pokémon Red, it adds new Pokémon species to the game, all the way up to Gen 6. 
- **[GBAXG](https://kittypboxx.github.io/GBAXG/)** (by KittyPBoxx) is a cross-game map randomizer of Pokémon FireRed, Emerald, and the Crystal Dust ROM hack. This does not mean it works on any of those given games - it works on all of them, *at once*. Travel between three regions seamlessly, and keep your team and items as you look for Gym Leaders and the Elite 4.
- **[Archipelago](https://archipelago.gg/)** is a so-called "multi-game" randomizer. Not only does it support many many games from disparate franchises, but it lets you combine them in a sense. Or if you don't feel up for that, the games can be randomized by their own. At the time of writing, Archipelago officially supports Pokémon [Red/Blue](https://archipelago.gg/games/Pokemon%20Red%20and%20Blue/info/en) and [Emerald](https://archipelago.gg/games/Pokemon%20Emerald/info/en), but more are likely to be added in the future. 

### Branches

- **[The Gaia Fork](https://github.com/sphericalice/universal-pokemon-randomizer)** (by Spherical Ice) branches off from the original UPR, and adds support to the popular Gaia ROM hack.
- **[brentspector's branch](https://github.com/brentspector/universal-pokemon-randomizer)** (by brentspector) branches off from the original UPR, and adds a number of features and tweaks, such as support for FireRed Randomizable 809. 
- **[Ironhidelvan's branch](https://github.com/IronhideIvan/universal-pokemon-randomizer-zx)** (by Ironhidelvan) branches off from ZX, and adds options to improve trades and trainer teams, among others.
- **Speedchoice branches** (by [Dabomstew](https://github.com/Dabomstew/UPR-Speedchoice), and [choatix](https://github.com/choatix/zxplus)) are ones that support Speedchoice ROM hacks. Since there are multiple branches with multiple releases each, each compatible with only some ROMs, finding the right Randomizer can be tricky. To solve this, Speedchoice communities tend to pack the relevant Randomizer with the download of the Speedchoice ROM itself.  
