New year, new version! Version info below, explanation on how to use at the bottom.

---

This update contains some upgrades to the encounter randomization following refactoring, as well as several new options/features by [foxoftheasterisk](https://github.com/foxoftheasterisk):

Starters:
- new option(s): "Type Restrictions" radio button (foxoftheasterisk)
    - "None"
    - "Fire, Water, Grass"
    - "Any Type Triangle"
    - "Unique"
    - "Single Type" - all starters will share the same type, which can be separately chosen
- new option: "No Dual Types" (foxoftheasterisk)
- new option: "Don't Use Legendaries" (foxoftheasterisk)

Trainers:
- new option: "Keep Type Themed Trainer's Theme" - trainers that only had Pokémon of a certain type, will when randomized keep that type theme (foxoftheasterisk)
- new option: "Use Local Pokemon" - trainers only use Pokémon that can be caught in the wild in main-game areas (foxoftheasterisk)

Encounters:
- "Similar strength", "Catch-em-all Mode", and "Type Themed Areas" are no longer mutually exclusive
- new option: "Location 1-to-1 Mapping" - groups together all encounters in a given "location" (i.e. all floors of a cave), and replaces Pokémon species 1-to-1.
- renamed option: "Type Themed Areas" -> "Random Area/Loc. Themes"
- new option: "Keep Area/Loc. Themes" - Encounter areas/locations that only had Pokémon of a certain type, will when randomized keep that type theme (foxoftheasterisk)
- new option: "Keep Primary Type" - Pokémon will be replaced with ones that have its primary type as one of their types. E.g. Oddish (Grass/Poison) could be replaced by Tangela (pure Grass), Exeggcute (Grass/Psychic), or Paras (Bug/Grass), but not Grimer (Poison).(foxoftheasterisk)

Graphics: 
- fix: some vital files for the palette randomization were missed when building the last version, making that feature defunct. Thanks morgansmnm for pointing that out! 
- new CPGs (Gen 1): Jotaro Kujo (MrHtuber), Noriaki Kakyoin (MrHtuber), Kirby (Retro64)

Known issues: 

- The custom player graphics feature in Gen 2 does not work, presumably due to DLL difficulties. The feature is still accessible in case this bug is dependent on the user's computer and could work for others. It does not work on mine. What's weird about this is that the feature works in my development environment, but NOT once the JAR is built. If anyone has experience with Java's JNA Library, Lunar Compress, or Gen 2's compression algorithm, your advice is highly valued.

- Some weirdness with Emerald's trainer handling has showed up in test cases. Exactly _what_ it is and what errors it may cause is yet unknown, other that it probably having to do Sootopolis Steven. Trainer randomization in that game is done at own risk.

(I had intended to go to `V0.10.0` when the custom player graphics were in all Gens, but there are exciting developments going on in the DS ROM Hacking scene with NDS4j. It ought to be of great help / eliminate the need to reverse engineer various file formats, once the. The Gen IV and V custom player graphics come then, but the features added here are too many and significant to reckon only an increase from `V0.9.4` to `V0.9.5`. Thus `V0.10.0` now.)

---

Download the randomizer below by clicking on `PokeRandoZX-v4_6_0+V0.10.0.zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the randomizer by double clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`
