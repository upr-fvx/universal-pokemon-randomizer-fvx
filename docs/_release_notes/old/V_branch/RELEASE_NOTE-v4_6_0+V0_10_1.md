Some things were broken in the last version. Here's fixing that.

Trainers:
- fixed "Pokemon League Has Unique Pokemon" not working
- "Pokemon League Has Unique Pokemon" can now be used on Gen 1 and Gen 2 games

Encounters:
- fixed "Similar Strength" checkbox not being connected to the underlying logic. I.e. it did not work at all. Thanks to ScienceTynan for pointing this out!

Known issues (same as in V0.10.0): 

- The custom player graphics feature in Gen 2 does not work, presumably due to DLL difficulties. The feature is still accessible in case this bug is dependent on the user's computer and could work for others. It does not work on mine. What's weird about this is that the feature works in my development environment, but NOT once the JAR is built. If anyone has experience with Java's JNA Library, Lunar Compress, or Gen 2's compression algorithm, your advice is highly valued.

- Some weirdness with Emerald's trainer handling has showed up in test cases. Exactly _what_ it is and what errors it may cause is yet unknown, other that it probably having to do Sootopolis Steven. Trainer randomization in that game is done at own risk.

---

Download the randomizer below by clicking on `PokeRandoZX-v4_6_0+V0.10.1.zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the randomizer by double clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`
