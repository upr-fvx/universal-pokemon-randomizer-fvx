<!-- This is a template for release notes. Everything in square brackets should be replaced. -->

[Greeting, short description of the release. Mention if it's a minor or major release, highlight major features.]

<!-- Any category below can be skipped if there are no people in it. -->
<!-- People on GitHub should be referred to using their ID with the @. E.g. @namehere. 
     For redditors, /u/namehere works for brevity. 
     For people from all other forums, their forum username should be used alongside the forum's name. E.g. "Jane Doe from Spriter's Resource". -->
Thanks to 
@bergmaen for your code contributions,
[Every person who submitted a solved issue] @LunaisLazier and @Gabbyxo97 for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] FourLeafSunny from Spriters' Resource for the CPG sprites, and
[Community members who helped with some feature] for help with [feature]
[Etc.]!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v[VERSION].zip`. After downloading, extract the contents of the 
zip file to a folder on your computer. You can then run the Randomizer by double-clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features
<!-- Group features by the tabs by where they appear in the GUI. Namely, use the names of the boxed categories (not necessarily the same as the tab names) -->
<!-- Below are some example features. They are not expansive, because it is annoying to remove a dozen categories that don't have any new/changed features this release. -->
<!-- (Gen [N]) can be used to denote a feature or bugfix only is relevant when randomizing certain Generations, and (GUI) for GUI stuff. -->

### Pokemon Base Stats
- [The description of a new feature here.]

### Static Pokemon
- (Gen 3) [The description of a changed feature here.]

### Trainer Pokemon
- Split the "Better Movesets" option into three options, for boss trainers, important trainers, and regular trainers. In Gen 2, only boss trainers and important trainers may be given better movesets, due to memory constraints. (#157)
- (Gen 1) Disabled "Better Movesets", since the feature did not function at all in this Gen. 
- "Better Movesets" now excludes the first rival/friend battle, and other "not strong" trainers. This is consistent with those trainers not being given other boosts, like held items and added Pokemon.

### Wild Pokemon
- (GUI) Renamed "Keep Area/Zone Theme" to more consistent "Keep Set/Zone Theme".

### Graphics
- (Gen 3) New FRLG Custom Player Graphics: Ethan (from Pokémon) by FourLeafSunny.

### Misc. Tweaks
- (Gen 2) Added support for "Reusable TMs" and "Forgettable HMs" to pokecrystal-speedchoice v8.

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- [The description of some miscellaneous feature (new or changed) here.]
- (Gen 2) Restored check value writing for pokecrystal-speedchoice.
- (GUI) Added theme selection. This includes dark mode (!) and also the ability to change between Windows/Metal/FlatLaf themes. The Windows theme is still only available on Windows computers. (#151)

## Bugfixes
- (Red/Blue) Fixed rivals original starters not being randomized if starter
  Pokemon are randomized, trainer Pokemon are unchanged, additional Pokemon are
  added to important trainers, and rival is supposed to keep starter throughout
  the game. (#145)
- (Yellow) Fixed Settings from other games not being loadable in yellow. (#145)
- (GUI) Fixed Settings String for the selected percentage for trainer Pokemon
 evolution levels not being loaded correctly. (#144)
- (Gen 2) Fixed random starter held items randomization failure. (#155)
- (GUI) Fixed "No-Game-Breaking Moves" not being saved properly to settings files. (#153)
