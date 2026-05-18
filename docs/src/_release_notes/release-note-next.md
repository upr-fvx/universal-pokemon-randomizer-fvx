<!-- This is a template for release notes. --> 
<!-- If you are a contributor editing this file as part of a PR, most of the below should be left untouched. -->
<!-- If you are finalizing a release, everything in square brackets should be replaced. -->

[Greeting, short description of the release. Mention if it's a minor or major release, highlight major features.]

<!-- When editing as part of a PR, credit yourself and people in the other categories as appropriate.-->
<!-- When finalizing, any category below can be skipped/removed if there are no people in it. -->
<!-- People on GitHub should be referred to using their ID with the @. E.g. @namehere. 
     For redditors, /u/namehere works for brevity. 
     For people from all other forums, their forum username should be used alongside the forum's name. E.g. "Jane Doe from Spriter's Resource". -->
Thanks to 
[Every person who submitted a merged pull request] for your code contributions,
[Every person who submitted a solved issue] for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] for the CPG sprites, and
[Community members who helped with some feature] for help with [feature]
[Etc.]!

# How to use

<!-- This [VERSION] and [OS] are the exception, to not replace while finalizing. [VERSION] is automatically replaced by a build script, and [OS] should remain for the end users. -->
Download the Randomizer below by clicking on `UPR_FVX-[VERSION]-[OS].zip`. If you are on Linux or Mac, and don't know if your computer uses x86 or ARM, there are guides on the internet. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the Randomizer by double-clicking the launcher script:

- Windows: Use `launcher.bat`
- Linux: Use `launcher.sh`
- Mac: Use `launcher.command`

# Changelog
## New and Changed Features
<!-- When editing as part of a PR, add your feature/bugfix below. Use (Issue #[issue num]) to denote the associated issue. -->
<!-- Group features by where they appear in the GUI tabs. Namely, use the names of the boxed categories (not necessarily the same as the tab names). This means e.g. a "Pokemon Evolutions" feature would go between "Pokemon Base Stats" and "Static Pokemon". -->
<!-- Below are some example features. They are not expansive, because it is annoying to remove a dozen categories that don't have any new/changed features this release. -->
<!-- (Gen [N]) can be used to denote a feature or bugfix only is relevant when randomizing certain Generations, and (GUI) for GUI stuff. -->

### Pokemon Base Stats
- [The description of a new feature here.] (Issue #[issue num])

### Static Pokemon
- If a Static Pokémon slot allows for alt formes, and a Pokémon with cosmetic formes (e.g. Burmy, Flabébé, Unown) is put in that slot, that Pokémon will have a random cosmetic forme instead of always having the forme that corresponds with id=0.

### In-Game Trades
- (Gen 7) If a Pokémon with cosmetic formes (e.g. Burmy, Flabébé, Unown) is randomly chosen as the given Pokémon of a trade, that Pokémon will have a random cosmetic forme instead of always having the forme that corresponds with id=0. 

### Misc. Tweaks
- (Gen 4+5) [The description of a misc. tweak addition or change here.]

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- (Log) Made the "Pokemon Base Statistics / Types / Abilities" list include alt forms immediately after their base forms, instead of at the very end.
- (Log+GUI) Gave form suffixes to the base forms of Pokemon with non-obvious base forms. **E.g.** "Wormadam" -> "Wormadam-Plant". This applies to: Wormadam, Basculin, Pumpkaboo, Gourgeist, Oricorio, and Lycanroc. 
- [The description of some miscellaneous feature (new or changed) here.]

## Bugfixes
- (Gen 7) Fixed non-Ultra Beasts static encounters occasionally being randomized into Ultra Beasts when "Swap Legendaries & Swap Standards" is used.
