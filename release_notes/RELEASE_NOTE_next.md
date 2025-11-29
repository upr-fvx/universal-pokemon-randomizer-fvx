<!-- This is a template for release notes. Everything in square brackets should be replaced. -->

[Greeting, short description of the release. Mention if it's a minor or major release, highlight major features.]

<!-- Any category below can be skipped if there are no people in it. -->
<!-- People on GitHub should be referred to using their ID with the @. E.g. @namehere. 
     For redditors, /u/namehere works for brevity. 
     For people from all other forums, their forum username should be used alongside the forum's name. E.g. "Jane Doe from Spriter's Resource". -->
Thanks to 
@bergmaen for your code contributions,
@slingshotocelot and @callomello for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] Nachopeñalva and Ploxel for the CPG sprites, and
AZBZ from the 3DS Pokémon Modding Discord server for help with Gen 7 Type Effectiveness randomization
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

### Pokemon Evolutions
- The option "Use Estimated Evolution Levels" is now also usable for "Random"
  evolutions.
  (#111)

### Static Pokemon
- (Gen 3) [The description of a changed feature here.]

### Trainer Pokemon
- Add option "Trainer Evolve their Pokemon" that can be selected if "Random
  Every Level" for Pokemon Evolutions is not selected.
  If selected, any trainer Pokemon will be evolved as far as possible at its
  level. For any trainer Pokemon that does not evolve by level up, its
  estimated evolution level is used. 
  This replaces "Force Middle Stage at Level:".
  (#107)

### Types
- (Gen 7) Added support for Type Effectiveness randomization.

### Graphics
- (Gen 3) New RSE Custom Player Graphics: Cynthia (from Pokémon) by NachoPeñalva.
- (Gen 1) New Custom Player Graphics: Kind Dedede, Meta Knight (both from Kirby), by Ploxel.

### Misc. Tweaks
- (Gen 4+5) [The description of a misc. tweak addition or change here.]

### Misc.
- Add option "No Random Intro Mon". If you want the Pokemon that the professor sends out in the intro (or Ethan's/Lyra's Marill in HGSS) to NOT be randomized, check this box. (#121)

## Bugfixes
- (Gen [N]) Fixed [...]. (Issue #[issue num])
- (GUI) Fixed [...].
- (Gen 4-7) Fixed additional trainer Pokemon of unrandomized trainers always
  being duplicates of original trainer Pokemon. (#116)
