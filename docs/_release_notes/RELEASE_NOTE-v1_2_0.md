Hello everybody, here comes a new release for the Randomizer! Including bug fixes, a revamp of the log system, and "Random Every Level" enabled in Gen 1+2. 

No forgettable HMs in Gen 4 yet though, as just as the sidetracking was done, a Dwarf Fortress modding update dropped... Will still get to it, but figured it's better to release what has been done already, rather than waiting another week or two.

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.2.0.zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the Randomizer by double clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features

### Pokémon Evolutions
- (Gen1+2) Support for the "Random Every Level" option added (Japanese games excluded, due to memory concerns). Note that in Red/Blue, there is a bug where evolving into Mew will give that Pokémon glitched stats. Evolving once again and healing at a Pokémon Center will fix this. 

### Trainer Pokémon
- (Gen 5) When using "Any Type Triangle" for starter + "Keep Trainers' Type Themes" for trainers in BW, the Striaton City Gym now uses the chosen starter types instead of Fire/Water/Grass.

### Misc.
- The log file output after randomizing a game, has been entirely revamped. New info has been added in places, and it is now more structured. The strings composing the log file have also been separated from the code, to make translations of the Randomizer into other languages easier.
- If/when Evolution randomization fails, the Randomizer now recommends "Standardize EXP curves" in the warning.

## Bugfixes
- Fixed bug where partially post-game areas treated the main-game portion as post-game and vice versa. 
- (Gen 1) The "Remove Time-based Evolutions" checkbox has been hidden, since it had no effect.
- (Gen 2) Reusable TMs can now be used in non-Japanese Gold/Silver (the checkbox was accidentally hidden).
- (Gen 2) "Custom Trainer Graphics" options have been hidden on non-Windows OS:es, since the feature only works on Windows.
- (Gen 4+5) Trying to randomize Pokémon palettes no longer causes the Randomizer to fail.
- (Gen 6+7) The "Update Type Effectiveness" checkbox has been hidden, since it had no effect.