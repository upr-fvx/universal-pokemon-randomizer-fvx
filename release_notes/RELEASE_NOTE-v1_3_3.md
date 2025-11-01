Hello everybody! This release adds more Custom Player Graphics to choose from, and fixes a bug related to adding your own CPG. 
There's also a new option for Pokemon Evolutions, a first view of a more robust system for when a mon ought to evolve, 
which should make it into other parts of the Randomizer going forwards. :)

Thanks to 
[Every person who submitted a merged pull request] for your code contributions, and
[Every person who made a new CPG] FourLeafSunny and NachoPeñalva for the CPG sprites!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.3.3.zip`. After downloading, extract the contents of the 
zip file to a folder on your computer. You can then run the Randomizer by double-clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features

### Pokemon Evolutions
- Add option "Use Estimated Evolution Level" that can be selected if evolutions are not randomized and "Change impossible evolutions" is
  selected. If selected, evolution levels are estimated from all level-up evolutions of the loaded ROM instead
  of using a preset evolution level in the "Change impossible evolutions" option.

### Graphics
- (Gen 2) New Custom Player Graphics: Silver (from Pokémon), Susie (from Deltarune) by FourLeafSunny.
- (Gen 3) New FRLG Custom Player Graphics: Kris (from Pokémon) by FourLeafSunny, Wally (from Pokémon) by NachoPeñalva.

## Bugfixes
- (Gen 3) Fixed overworld sprites of Custom Player Graphics sometimes getting an invalid/glitchy palette.
