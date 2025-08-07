Hello everyone! Here comes a new major release for the Randomizer, with new trainer options and QoL features, bug fixes and more!
Add mons to Trainers' teams without randomizing the rest of the team, force them all to be triple battles, and/or add cheap rare candies to all shops... to only mention some features, the full list can be found below.

This release also includes the Universal Pokemon Shop Editor, more on that in its own section at the end. 

Thanks to @bergmaen and @mFireworks for your code contributions; @SteelPH, @JeffreySoriano5, @bergmaen, and @AxelElric8 for reporting Issues; DelyBulacha and MollyChan from Spriter's Resource for the CPG sprites; and many people in the pret and Kingdom of DS Hacking Discord servers for help with the shop rewrite(s) needed to get Cheap Rare Candies to work, and AdAstra from the same servers who helped with Gen 4 forgettable HMs!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.3.0.zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the Randomizer by double clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features

### Trainer Pokémon
- "Add Pokémon" option can now be used without randomizing Trainer Pokémon (PR #45). Added Pokémon are random, but follow their Trainer's Type theme (if they have one), and can be restricted using the usual randomization options.
- New option "Battle Style Randomization"; replaces "Double Battles Only" (PR #53). Setting all Trainers to be double battles is still possible, but it also allows setting all trainers to triple or rotation battles (in Gen 5+6), and setting each Trainer to use a random battle style of the ones available in the game.
- New option "Force Middle Stage" (PR #54). Works similarly to "Force Fully Evolved"; a level is chosen in the GUI, and any Trainer Pokémon at that level or higher will be forcibly evolved, if they have a middle stage to evolve into. **E.g.**, if the level is set to 20, lv24 Bellsprout and lv40 Dratini, will instead become lv24 Weepinbell and lv40 Dragonair. Lv19 Charmander and lv21 Rattata will be untouched.

### Shop Items
- (Gen 1-5) New option "Add Cheap Rare Candies": adds Rare Candies to most shops, purchasable for 10¥ each.
- (GUI) restructured the GUI some. "Balance item prices" can now be used without randomizing special shops.

### Graphics
- (Gen1) New CPGs by DelyBulacha: Frisk and Chara from Undertale.
- (Gen2) New CPGs by MollyChan: Rouge, Ondore, and Hierre from Time Bokan.

### Misc. Tweaks
- (Gen 4) Added support for "Forgettable HMs".
 
## Bugfixes
- (Gen 3+4) Fixed corruption in some text being inserted into the games. 
- (Gen 3) Fixed Randomized Trainer names not being written to ROM (Issue #41). 
- Fixed Clamperl not being able to evolve into Huntail, when "Remove impossible evolutions" was used (Issue #42).
- Fixed Trainer Pokémon with predefined movesets, losing these moves in favor of their level-up moveset, if evolved through "Force fully evolved". (PR #50). 
- (Gen 7) Fixed item-related evolutions using the wrong items, in many cases making evolving impossible (as invalid items were chosen) (Issue #59).
- (Gen 7) Fixed the evolutions of Rockruff and Cosmoem being incorrectly read and written.

# The Universal Pokémon Shop Editor

At the time of writing, there definitely exists better shop editors if you are hacking Gens 1-3, maybe if you are hacking Gens 6+7, and perhaps in certain ways too if you are hacking Gens 4+5.

The Universal Pokémon Shop Editor is just what it sounds like, a shop editor which leverages the same code as the UPR to work on all Gen 1-7 games (barring Korean Gold/Silver and Let's Go! Pikachu/Eevee). It is a bonus included in this Randomizer release, since the research needed to make "Cheap Rare Candies" work, also gave the Randomizer some shop editing capabilities not seen in other Gen 4+5 tools. The UPR can not be used directly to edit shops though, so here is the Universal Pokémon Shop Editor. Though the tool was made for the Gen 4+5 hacking community it is still universal, because why not.

It is not a very refined tool, and there are no plans to develop it further. If you like what it does but see issues with it (e.g. the way it does ARM9 expansion in Gen 4), pick apart the source code to make way for a better tool. That is one of the intended ways to use it. :)

Technical notes for how it works in Gen 4+5, and how to use the tool, can be found inside the download.