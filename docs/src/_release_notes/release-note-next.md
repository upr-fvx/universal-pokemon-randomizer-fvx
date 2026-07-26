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
@AxelElric8 for your code contributions,
[Every person who submitted a solved issue] @kiliwily, @Angeluco, @GeCAF, MClarke93, and @DrSodaCan for reporting Issues,
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
- New Panel: Base Stat Totals (Issue #4)
  - New Option: <b>Random Buff/Nerf</b>. Gives each Pokémon a random buff or nerf to their BST. The extent of these buffs/nerfs are given by a slider, going between 0-50%. <b>E.g.</b> if set to 20%, each Pokémon will randomly get between 80-120% of their original BST.
  - New Option: <b>Shuffle</b>. Shuffles the BSTs of all Pokémon. If there were originally 6 Pokémon with 600 BST, there will be 6 after applying this option, but the Pokémon will be different. <b>E.g.</b> Weedle might receive the BST of Mewtwo, Charizard the BST of Butterfree.
  - New Option: <b>Random</b>. Entirely randomizes the BST of each Pokémon.
  - New Option: <b>Follow Evolutions</b>. Can be used with "Random Buff/Nerf" and "Shuffle". 
   
    If used with "Random Buff/Nerf", every Pokémon in a family will get the same buff/nerf.
    
    If used with "Shuffle", BSTs will be shuffled family-by-family, going by length. (3-stage families will be shuffled with 3-stage families, 2-stage families with 2-stage families, non-evolving mons with non-evolving mons)
  - New Option: <b>Separate Legendaries</b>. Can only be used with "Shuffle". Makes legendaries have their BSTs shuffled separately from all other Pokémon. This prevents extremes like Farfetch'd getting the BST of Mewtwo, or vice versa, while still allowing variety within the categories.
- Moved "Standardize EXP Curves" to its own panel further down in the GUI.
- [The description of a new feature here.] (Issue #[issue num])

### Pokemon Evolutions
- New Option: Adjust Evolution Levels. This option is only available when you randomize evolutions or BSTs. If checked, level-up evolutions will have their levels algorithmically adjusted, to better fit the Pokemon before and after evolution (this reuses [the "estimated evo levels" algorithm](https://upr-fvx.github.io/universal-pokemon-randomizer-fvx/wikipages/estimatedLevels.html)). <b>E.g.</b>, if a Pokemon evolves into Dragonite, it will do so at a high level.

### Static Pokemon
- (Gen 3) [The description of a changed feature here.]

### Misc. Tweaks
- (Gen 4+5) [The description of a misc. tweak addition or change here.]

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- Added Level Caps section to the log file to track boss levels across generations. (Issue #152)
- Made the alt formes of Wormadam, Meowstic, and Lycanroc be treated as split evolutions, when "Follow Evolutions" options are used.
- Improved launcher error reporting. (Issue #237)
- Using Linux and Mac, all console output is now logged in the file `console-output.log`. Using Windows, said file only logs launch errors.

## Bugfixes
- (GUI) Fixed "Random (any basic Pokemon)" starter radio button not getting disabled after randomization. (Issue #230)
- Fixed "Add Held Items to Boss Trainers" instead adding held items to regular trainers. (Issue #232)
- (Gen 3) Fixed randomized TMs corrupting move description text, in FRLG. (Issue #154) 
- (Gen [N]) Fixed [...]. 
- (GUI) Fixed [...].
