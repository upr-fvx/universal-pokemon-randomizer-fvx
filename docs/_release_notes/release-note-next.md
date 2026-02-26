<!-- This is a template for release notes. Everything in square brackets should be replaced. -->

[Greeting, short description of the release. Mention if it's a minor or major release, highlight major features.]

<!-- Any category below can be skipped if there are no people in it. -->
<!-- People on GitHub should be referred to using their ID with the @. E.g. @namehere. 
     For redditors, /u/namehere works for brevity. 
     For people from all other forums, their forum username should be used alongside the forum's name. E.g. "Jane Doe from Spriter's Resource". -->
Thanks to 
[Every person who submitted a merged pull request] for your code contributions,
[Every person who submitted a solved issue] for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] Happy Time Boredom and bepis from Spriter's Resource for the CPG sprites, and
[Community members who helped with some feature] for help with [feature]
[Etc.]!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v[VERSION]-[OS].zip`. If you are on Linux or Mac, and don't know if your computer uses x86 or ARM, there are guides on the internet. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the Randomizer by double-clicking the launcher script:

- Windows: Use `launcher.bat`
- Mac: Use `launcher.command`
- Linux: Use `launcher.sh`

# Changelog
## New and Changed Features
<!-- Group features by the tabs by where they appear in the GUI. Namely, use the names of the boxed categories (not necessarily the same as the tab names) -->
<!-- Below are some example features. They are not expansive, because it is annoying to remove a dozen categories that don't have any new/changed features this release. -->
<!-- (Gen [N]) can be used to denote a feature or bugfix only is relevant when randomizing certain Generations, and (GUI) for GUI stuff. -->

### Pokemon Base Stats
- [The description of a new feature here.]

### Static Pokemon
- (Gen 3) [The description of a changed feature here.]

### Graphics
- (Gen 2) New Custom Player Graphics: Dennis the Menace (from Beano) by Happy Time Boredom, Quote (from Cave Story) by bepis.

### Misc. Tweaks
- (Gen 4+5) [The description of a misc. tweak addition or change here.]

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- Upgraded/automatized the Randomizer's build environment. 
  - For end users, this has the following effects: 
    - Fixes for urgent bugs will come faster (since building and packaging a new release is faster).
    - The Randomizer now comes with a mini-version of Java, sufficient to run it. This means Java does not need to be separately installed. On the other hand the mini-Java takes some space, so to not bloat the download folder, it is now divided into one download for each OS/architecture (Windows, Mac x86, Mac ARM, Linux x86, Linux ARM). The Randomizer no longer supports running on 32-bit Windows.
    - (32-bit Windows, or other odd OS/architecture combos, can probably still work *if* you manage scrunch up a compatible Java 25 JRE. Contact us using GitHub Issues, if you want to give it a try.)
  - For developers, this has the following effects:
    - Gradle is now used, for building and testing the Randomizer. [The "Building" page](https://upr-fvx.github.io/universal-pokemon-randomizer-fvx/wikipages/building.html) has been updated to account for these changes.
    - Dependencies are automatically managed.
    - We are no longer stuck with Java 8, now using Java 25.
    - Building and packaging a new release is much faster.
    - The code has been restructured, and the `romio`/`random`/`devtools` division has been pulled to root. `utils` has also been pulled out of `romio`. This division is also enforced by Gradle. [TODO: see this wiki page explaining the code divisions. Or a readme?]
    - It should be possible to further facilitate/automatize tasks, using GitHub Actions hooking into Gradle.

## Bugfixes
- (Gen [N]) Fixed [...]. (Issue #[issue num])
- (GUI) Fixed [...].
