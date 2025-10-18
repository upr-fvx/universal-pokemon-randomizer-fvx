 - "Field items" - items found in item balls or hidden items - can now be randomized.
  * "Shuffle" keeps the set of items found on the overworld the same, just shuffles their locations.
  * "Randomize" puts a new random sensible item in each item spot (sensible items exclude things like key items, HMs and glitch items)
  * Item balls containing key items or HMs are not randomized to make sure the game is still able to be completed.
  * TMs are kept in the same balls, but the number of the TM you receive from each ball is shuffled/randomized appropriately.
  * Each TM will still be available in the game at least once.
 - In-game trades can now be randomized.
  * You can pick to only randomize the Pokemon you receive, or also randomize the Pokemon you have to trade in exchange.
  * Where available, you can also randomize the nickname, OT, IVs and held item of each trade.
  * Randomization of nicknames uses a new list of names called "nicknames.txt" stored in the same way as trainer names/classes. Randomization of OT names uses the pre-existing trainer names file.
 - Added Metronome Only Mode as a choice for Pokemon movesets by popular request.
  * When this is enabled, all Pokemon will only learn Metronome and its PP will be boosted to 40.
  * Randomization of TMs and Move Tutor moves will be disabled, and all of them will be changed to Metronome.
  * HM moves & compatibility will be left alone, but all of them will be set to 0PP to prevent their use in battle.
 - "Similar Strength" is now an available option for randomizing wild Pokemon. It uses the same method as its equivalent for trainer Pokemon.
  * This option is not available at the same time as either Catch-Em-All mode or Type Themes because these can limit the available Pokemon pool too much for it to function adequately.
  * More specific options to limit/ban evolved Pokemon may be added in the future, but this should have a similar effect for the time being.
 - Excluding HM moves from levelup movesets/TMs/move tutors is now done per game instead of excluding all moves that have ever been an HM.
  * For some examples, you can now get Flash TMs in Generation 4/5 and Waterfall TMs in Generation 1.
 - Pokemon available by fishing are now included in the wild Pokemon randomization in Generation 1.
 - Shuffling stats in Generation 1 now preserves the Pokemon's base stat total a little better (allowing for the fact that Special is twice as valuable as anything else)
 - Move names, ability names & item names are now read directly from the ROM. The practical application of this is that log files will contain the names from the language of the ROM you randomize instead of having a lot of English. (The sentence structure and formatting is still hardcoded to be English for the time being.)
 - The log file produced for each randomized ROM has been improved slightly.
  * The name of each trainer is now included. This makes it easier to tell what Pokemon a specific trainer will have (though the game will often contain multiple entries for each trainer, such as rematch teams)
  * Each Wild Pokemon set will have a description of what it is actually used for in Generation 1-3 games. This will be included for the DS games in a future release.
  * The log file also now includes the details of randomized in-game trades if you choose to randomize them, but field items are not included yet.
 - When a ROM fails to load or save, the randomizer will now attempt to save a log file with more details about the problem. Please include this log file in any bug reports if possible.
 - The text tables for the Generation 1/2 games have been slightly improved to include the accented characters available in the European releases of these games.
 - Started working on some basics to eventually allow for randomization of more complex ROM hacks that move data around. Nothing concrete yet.
 - The "Minimum Catch Rate" checkbox will actually function as intended now. In earlier randomizer versions it did nothing.
 - When trainers are randomized, the Champion Rival battles in Red/Green/Blue will no longer always have Sky Attack on the lead Pokemon nor a set type move on the last Pokemon.
 - Starter Pokemon should now be randomized correctly in foreign Gold/Silver/Crystal games.
 - Corrupt text data in Gold/Silver/Crystal is now handled a little more gracefully.
 - The "Remove Trade Evolutions" feature will finally work correctly for Generation 3/4/5 games and change pure (no held item) trade evolutions to pure level evolutions instead of also requiring high happiness.
 - When abilities are randomized in Generation 3 games, Pokemon with 2 abilities evolving into Pokemon with only 1 ability will no longer have a chance to lose their ability altogether.
 - Japanese Ruby/Sapphire can now be randomized.
 - Generation 4/5 ROM compatibility improved slightly.
 - Fix a bug in Generation 4 games where moves would sometimes gain weird effects.
 - Fix a bug in Generation 5 games where perfect-accuracy moves such as Swift were being accidentally changed to just 100% accuracy.
 - The Area Data function of the Black/White 2 Pokedex will now correctly exclude Pokemon placed in version exclusive areas.