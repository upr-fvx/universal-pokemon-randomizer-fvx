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
@bergmaen and @glamurio for your code contributions,
[Every person who submitted a solved issue]@MClarke93 and @Shiigu for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] Happy Time Boredom and bepis from Spriter's Resource for the CPG sprites, and
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
- (Gen 1-7) Moved the option 'Do Not Use Prematurely Evolved Pokemon' from the
  Trainer Pokemon tab to the General Options. If selected, it now also affects
  randomized Wild Pokemon if neither 'Same Evolution Stage' nor 'Keep
  Relations' is selected for Wild Pokemon Evolution Restrictions. (Issue #146)

### Static Pokemon
- (Gen 3) [The description of a changed feature here.]

### Move Data
- (Gen 3-7) Added the ability to randomize move names. Due to technical limitations,
this feature is only availabe from Generation 3 and upwards. (Issue #176)

### Trainer Pokemon
- (Gen 1-7) Introduce new option 'Try to Avoid Duplicates'. If this is checked,
  any randomly chosen Pokemon for a given trainer will be different from the
  other Pokemon of the trainer. However, if other rules restrict the set of
  available Pokemon too much, duplicates are possible and weaker or stronger
  Pokemon might be chosen even if 'Try to Use Pokemon with Similar Strength' is
  selected. (Issue #5, #162)
- (Gen 1-7) The first rival and/or friend battles no longer have their
  Pokemon's level increased if a value greater than 0 is chosen for 'Percentage
  Level Modifier:'. (Pull request #164)
- (Gen 1-7) Even for 'Unchanged' Trainer Pokemon, if an 'Additional Pokemon
  for...' option is selected, enable respective 'Force Diverse Types for...'
  option as well. Selecting it guarantees that for any trainers that are not
  type themed the added Pokemon have diverse types from the original Pokemon of
  the trainer. (Issue #150)

### Graphics
- (Gen 2) New Custom Player Graphics: Dennis the Menace (from Beano) by Happy Time Boredom, Quote (from Cave Story) by bepis.

### Misc. Tweaks
- (All games except ORAS) New Misc. Tweak: "Fast Egg Hatching". Makes all eggs hatch in as few egg cycles as possible. Generally, this means all eggs will hatch in <256 steps.

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- [The description of some miscellaneous feature (new or changed) here.]
- The percentage level modifier sliders for Static Pokemon, Trainer Pokemon
  Evolution Levels, Trainer Pokemon, Totem Pokemon, and Wild Pokemon can now be
  used to select any integer value between -100% and 155%. Furthermore, thanks
  to an added spinner, it is now easier to recognize which value is chosen and
  to fine-tune the selection. Note that the lowest possible level is 1 and the
  highest possible level is 100. 
  Furthermore, the percentage modifier for good damaging moves for randomized
  move sets, TMs, and move tutors can now be used to select any integer value 
  between 0% and 100%. (Issue #170)
- Upgraded/automatized the Randomizer's build environment. 
  - For end users, this has the following effects: 
    - Fixes for urgent bugs will come faster (since building and packaging a new release is faster).
    - The Randomizer now comes with a mini-version of Java, sufficient to run it. This means Java does not need to be separately installed. On the other hand the mini-Java takes some space, so to not bloat the download folder, it is now divided into one download for each OS/architecture (Windows, Mac x86, Mac ARM, Linux x86, Linux ARM). The Randomizer no longer supports running on 32-bit Windows.
    - (32-bit Windows, or other odd OS/architecture combos, can probably still work *if* you manage scrunch up a compatible Java 25 JRE.)
  - For developers, this has the following effects:
    - Gradle is now used, for building and testing the Randomizer. [The "Building" page](https://upr-fvx.github.io/universal-pokemon-randomizer-fvx/wikipages/building.html) has been updated to account for these changes.
    - Dependencies are automatically managed.
    - We are no longer stuck with Java 8, now using Java 25.
    - Building and packaging a new release is much faster.
    - The code has been restructured, and the `romio`/`random`/`devtools` division has been pulled to root. `utils` has also been pulled out of `romio`. This division is also enforced by Gradle. See [this wiki page](https://upr-fvx.github.io/universal-pokemon-randomizer-fvx/wikipages/structure.html) explaining the code divisions.
    - It should be possible to further facilitate/automatize tasks, using GitHub Actions hooking into Gradle.

## Bugfixes
- (Gen 4-7) Fixed Pokemon with formes showing up prematurely evolved despite
  'Do Not Use Prematurely Evolved Pokemon' being selected. (Issue #142)
- Fixed Special Attack not getting randomized when "Randomize Added Stats on Evolution" is used (Issue #178)
