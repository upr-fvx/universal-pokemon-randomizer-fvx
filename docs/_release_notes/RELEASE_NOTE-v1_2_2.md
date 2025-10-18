Here comes a new Randomizer release with a number of bug fixes, and then some. If you plan on randomizing a Gen 2 game, it is highly recommended you download this new release, since it fixes a major bug related to Pokémon Base Stats.

Thanks to @XModxGodX, @TendrilChicken, @Lunaislazier, and @tom-overton for reporting Issues, /u/pixelvistas on Reddit for noting the odd randomization order, MollyChan on the Spriter's Resource for the CPG sprites, and Dead by pfero and Sylvie in the pret Discord server for help with the compression algorithm!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.2.2.zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the Randomizer by double clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features
### Pokémon Evolutions

- Evolutions are now randomized before Pokémon Types. This way, Types will follow the new evolutions when "Follow Evolutions" is selected.

### Graphics

- (Gen 2) Custom Player Graphics can now be set on non-Windows OS:es. This was done by writing a new LZ_LC3 compressor - a translation of the one used by the [pokecrystal](https://github.com/pret/pokecrystal) disassembly - instead of relying on the Lunar Compress DLL by FuSoYa. (Issue #23)
- (Gen 2) New CPGs by MollyChan: Doronjo, Tonzura, and Boyacky from Yatterman. These are the characters who inspired the Team Rocket Trio!

## Bugfixes
 
- (Gen 2) Fixed bug where Pokémon had their base defense stats mirrored onto their base special defense. (Issue #37) 
- (Gen 2+3) Fixed Eevee's evolutions into Umbreon/Espeon using the wrong items when "Remove Time-Based Evolutions" was selected. (Issue #32) 
- (Gen 4) Fixed valid Platinum ROMs being marked as "unofficial". (Issue #33) 
- (Gen 5) Fixed bug where Black/White 2 would softlock during catching tutorial. (Issue #36)  
- (GUI) Fixed incorrect tooltips for Pokémon Palettes options
