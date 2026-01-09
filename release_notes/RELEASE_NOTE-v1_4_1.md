This is a minor release, adding experimental support to pokecrystal-speedchoice v8. And also you can play as Professor Birch in Ruby/Sapphire/Emerald, thanks to sprites by NachoPeñalva.

Thanks to 
Dabomstew and Choatix and others who have worked on the pokecrystal-speedchoice ecosystem, and
NachoPeñalva from DeviantArt for the CPG sprites!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.4.1.zip`. After downloading, extract the contents of the 
zip file to a folder on your computer. You can then run the Randomizer by double-clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features

### Graphics
- (Gen 3) New RSE Custom Player Graphics: Professor Birch (from Pokémon) by NachoPeñalva.

### Misc.
- Added experimental support for pokecrystal-speedchoice v8. This support entails the ROM passing our unit tests and basic in-game testing, but it has not been extensively playtested, and all features of ZX-plus are not covered. See [this TODO page](https://github.com/upr-fvx/universal-pokemon-randomizer-fvx/blob/vFVX1.4.1/TODO_crystal-speedchoice.md) for what is left to be done, at the time of this release.

  Due to diverging too long ago, this is not a merge of [Choatix' ZX-plus branch for pokecrystal-speedchoice](https://github.com/choatix/zxplus). The ROM Entry from that branch has been borrowed, and extended for to work with FVX-only features. In addition, the list of commits has been looked at, to get a grasp for what is needed to support speedchoice. Code other than the ROM Entry has not been directly referenced, though.