<!-- This is a template for release notes. Everything in square brackets should be replaced. -->

[Greeting, short description of the release. Mention if it's a minor or major release, highlight major features.]

<!-- Any category below can be skipped if there are no people in it. -->
<!-- People on GitHub should be referred to using their ID with the @. E.g. @namehere. 
     For redditors, /u/namehere works for brevity. 
     For people from all other forums, their forum username should be used alongside the forum's name. E.g. "Jane Doe from Spriter's Resource". -->
Thanks to 
[Every person who submitted a merged pull request] @bergmaen and @samualtnorman for your code contributions,
[Every person who submitted a solved issue] for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] for the CPG sprites, and 
[Community members who helped with some feature] for help with [feature]
[Etc.]!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v[VERSION].zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the Randomizer by double clicking the appropriate launcher script:

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
- (GUI) Improved spacing of GUI elements.

### Wild Pokemon
- (GUI) Description of "Similar Strength" now mentions "Catch 'Em All" as a confounding setting.

### Misc. Tweaks
- (Gen 4+5) [The description of a misc. tweak addition or change here.]

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- Improved Linux launcher compatibility, with distros that do not use `/bin/bash` (PR #79).

## Bugfixes
- (Gen 1+3) Fixed randomization failure when using "Randomize PC Potion" Misc. Tweak (Issue #43).
- (Gen 4+5) Fixed TM Shops being counted as Special Shops in all Gen 4 games + Black/White (Issue #73). 
- Fixed "Same Evo Stage" causing randomization failures in certain contexts including "Catch 'Em All" or mapping (Issue #67).