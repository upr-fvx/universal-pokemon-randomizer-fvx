 - There are no longer separate "normal" and "internal config" releases.
  * The randomizer now uses the "internal config" framework by default.
  * Customized trainer names / class names / trade nicknames can still be provided by placing appropriately named text files in the directory where the randomizer is kept (instead of inside the config folder).
  * If you customized these names inside the config folder on an earlier version, the randomizer will prompt you to move them to the new location when it is first opened after the update.
 - The annoying/confusing "cannot load gameboy_jap.tbl" screen should now be gone.
  * However, the randomizer still needs to be able to read files from disk and write files to disk to function, so it'll warn you if this isn't possible.
  * You still really need to extract the randomizer from the zip file before running it.
 - There is no longer a seperate jar file to run to disable automatic updates.
  * You can disable automatic updates inside the program itself, using the "Update Settings" button.
  * The randomizer will still have automatic updates ON by default.
  * If you really don't want to open the randomizer even once with automatic updates on, you can create a config.ini text file with autoupdate=false as the only line before opening it.
 - A new feature, "Limit Pokemon" has been added to allow you to limit the Pokemon that appear in the randomized game.
  * You can pick which generations of Pokemon appear, as well as including "associated" Pokemon (evolutions/pre-evolutions) from generations that you don't entirely include.
  * This will only affect things which are actually randomized - if you don't randomize something, the original Pokemon will be kept even if they aren't in your set limits.
  * This also doesn't have any impact on systems which aren't randomizable (yet), such as the various Battle Towers/Frontiers.
  * Pokemon which you don't include in your randomization will be cut off from evolution trees, so you won't be able to obtain them even via breeding or evolution.
  * Keep in mind that certain Pokemon choices using this feature will make some post-game gameplay objectives impossible, such as obtaining the National Dex in FireRed/LeafGreen (requires 60 of the original 151 caught) or Diamond/Pearl/Platinum (requires all the Pokemon in the Sinnoh dex to be at least seen). This feature is best used for just the "core" experience (up to the first round Elite Four)
 - A group of small adjustments to gameplay mechanics, "Code Tweaks", have been added.
  * Right now, these tweaks are focused on Generation 1 games, but more will be added in the future.
  * The current tweaks mainly aim to correct some of the more broken things about the games to make the gameplay more fun/balanced.
  * The "B/W Exp Patch" option has been categorized as a Code Tweak (because it is one), and moved into that window.
  * Code Tweaks in general are experimental, and if your game doesn't work after you apply one, you should try again without them active.
 - The efficiency/speed of DS game randomization has been significantly improved.
  * The time taken to load DS ROMs should be significantly reduced, while save times should stay about the same.
  * Older versions of the randomizer would extract the entire contents of the DS ROMs to a folder each time you opened them, and not clean up that folder once you were done.
  * This version only extracts what it needs to edit, and deletes temporary files/folders when it is closed.
  * You'll still need to delete the temporary folders made by old versions of the randomizer manually.
 - A new option for Generation 1 games has been added which gives every Pokemon 4 starting moves at Level 1.
  * This attempts to balance out Pokemon which only have one or two starting moves and don't learn any moves for ages with those which have more fortunate movepools.
  * This option is only available for Generation 1 games, but is not really required in later games because the advent of breeding necessitated that a large majority of Pokemon learn more moves at low levels anyway.
 - "Standardize EXP Curves" has been added as an option to change up the balancing of the game.
  * If this option is chosen, all non-legendary Pokemon will use the Medium-Fast curve (1,000,000 EXP to Level 100) and all legendaries will use the Slow curve (1,250,000 EXP to Level 100).
  * This makes some Pokemon more usable (namely non-legendaries without great stats that had the Slow curve such as Lapras) while making others a little less overpowered early on (Alakazam having the Medium-Slow curve and lots of levelup moves made him a monster in early game).
  * Legendaries still use the Slow curve to balance them out with regular Pokemon to some degree.
 - The "Update Moves" function has been changed up a bit.
  * The core function "Update Moves" now updates moves to their Generation 6 power/accuracy/PP instead of Generation 5.
  * Because of this, it is now available for Generation 5 games too.
  * For people that don't like the changes Generation 6 introduced, a "Legacy" checkbox is available to update moves to Generation 5 instead.
  * The Generation 5 updates have also been tidied up slightly, they weren't entirely accurate before.
 - The "Remove Trade Evolutions" option has been overhauled and renamed to "Change Impossible Evo[lution]s".
  * This option still removes trade evolutions, but now also addresses other evolutions that are impossible in the game played or made impossible by the randomization.
  * The former category of impossible evolution mainly impacts the remake games. FireRed/LeafGreen are missing a clock for Espeon/Umbreon and contests for Milotic, while HeartGold/SoulSilver are missing contests for Milotic and specific locations for Magnezone/Probopass/Glaceon/Leafeon. These evolutions are changed to either use stones or be achieved via levelup.
  * The latter category impacts Generation 4 games onwards, where certain Pokemon evolve when they learn a certain move. These evolutions are now replaced by pure levelup evolutions if you enable this option and randomize movesets. If you don't randomize movesets, they will be left alone.
  * As always, you can see the specifics of the changed evolutions by looking at the log file created after you save your randomized game.
 - "Randomize Held Items" has been seperated into two options under Starter Pokemon and Wild Pokemon respectively.
  * The Starter Held Items option controls held items for the Starters only, and is available in Generations 2/3.
  * The Wild Held Items option controls held items for wild Pokemon in general as well as some static encounters, and is available in all Generations except Gen 1.
  * Held items on in-game trades continue to be controlled by the same checkbox as before.
 - A few more moves have been added to the banlist which stops them being picked as the required damaging move on each Pokemon when movesets are randomized.
  * These moves include the extraordinarily weak Constrict, the lock-in moves Rollout & Ice Ball, and the fixed-damage moves Sonicboom & Dragon Rage.
  * These moves can still be picked as any other move in the randomized movesets.
 - The "Minimum Catch Rate" option has been reworked a bit.
  * It now makes normal Pokemon slightly easier to catch than before, but legendary Pokemon are a bit harder to catch.
  * The minimum catch rate for a weakened normal Pokemon with this option on should be about 30% in a Poke Ball, whilst legendaries should be at about 15%.
  * This makes it possible to catch most Pokemon early on, whilst still giving at least some special status to legendaries.
 - The Pokemon that Oak shows you in the intro to Pokemon Yellow is now randomized, though the color palette remains yellow, quite amusingly in some cases.
 - Spore has been added as a "broken move" in Generation 1, because of its ability to infinitely lock Pokemon into sleep and 100% accuracy.
  * In Generation 2 onwards, a Pokemon will always get a move off every time it wakes up, making sleep a lot less broken.
 - The "No Early Shedinja" option has been changed to "No Early Wonder Guard".
  * This means that if you randomize abilities and allow Wonder Guard, any Pokemon that get it will no longer appear early on when this checkbox is checked.
 - The inner mechanics of the "Rival carries starter through game?" checkbox under trainer randomization have been changed a little bit.
  * These adjustments should ensure that the starter Pokemon is always last in the Rival's party (though he might not always bring it out last in later games) except when the original games deliberately put it first.
  * This impacts a few rival fights, the majority of which are in the Generation 1 games, Red/Blue/Yellow.
 - The speed of the scrollbar arrows / mouse wheel on the randomization options panel has been significantly increased.
 - The ability of the randomizer to catch errors and make log files for them has been improved slightly.
  * This should help track down the cause of a few annoying bugs, such as the bug which stopped DS games from randomizing fully on certain PCs (if it still exists in this version)
 - Metronome Only Mode now works correctly in Generation 3 games.
 - Fix a small issue with randomizing multiple Generation 2 games in the same randomizer session.
 - Rotom is no longer considered a "legendary Pokemon" by the randomizer (because the games don't really treat it as such)
 - Randomizing static Pokemon in Pokemon Silver no longer causes a softlock when you obtain Shuckie in Cianwood City.