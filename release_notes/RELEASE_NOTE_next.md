<!-- This is a template for release notes. Everything in square brackets should be replaced. -->

[Greeting, short description of the release. Mention if it's a minor or major release, highlight major features.]

<!-- Any category below can be skipped if there are no people in it. -->
<!-- People on GitHub should be referred to using their ID with the @. E.g. @namehere. 
     For redditors, /u/namehere works for brevity. 
     For people from all other forums, their forum username should be used alongside the forum's name. E.g. "Jane Doe from Spriter's Resource". -->
Thanks to 
[Every person who submitted a merged pull request] @Glamurio and @bergmaen for your code contributions,
[Every person who submitted a solved issue] @CDNievas, @TheFreezingChicken, and @scipio19 for reporting Issues,
[Any person on e.g. Reddit who reported solved bugs or suggested implemented features] for [whatever they did],
[Every person who made a new CPG] Happy Time Boredom from Spriter's Resource for the CPG sprites, and
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

### Pokemon Evolutions
- Add a slider that is enabled when 'Make Evolutions Easier' is selected.
  It can be used to select at which level every Pokemon shall be evolved to 
  its final stage. Three-stage evolutions will reach their middle stage by no 
  later than 75% of the selected level. Furthermore, it is now possible to apply
  the other evolution improvements of 'Make Evolutions Easier' without lowering 
  evolution levels by choosing the maximum value of the slider. 
  (#123)

### Static Pokemon
- (Gen 2) Allowed Unown to appear as a random static Pokémon.

### Trainer Pokemon
- (Gen 1 + 2) Enabled "Better Movesets". (#84)

### Wild Pokemon
- (Gen 2) Allowed Unown to appear as a random wild Pokémon.

### Trainer Pokemon
- Add option 'Do Not Use Prematurely Evolved Pokemon' that can be selected if 
  Trainer Pokemon are randomized or additional Pokemon are added.
  This option guarantees that each random/added Pokemon is at a legal evolution
  stage at its level. This was previously included in the 'Trainers Evolve 
  Their Pokemon' checkbox and is now removed in said setting.
  (#125)

### Trainer Pokemon
- Remove 'Force Fully Evolved at Level:' checkbox and replace its level select
  slider with a slider to select the percentage to scale trainer Pokemon
  evolution levels and estimated evolution levels by. The scaled evolution
  levels affect the options 'Trainers Evolve their Pokemon' as well as 'Do Not
  Use Prematurely Evolved Pokemon'. 
  If 'Trainers Evolve their Pokemon' is selected, the Randomizer displays the
  highest evolution level for Trainer Pokemon, i.e., the level every trainer
  Pokemon will be fully evolved at, based on the percentage chosen with the new
  slider and other relevant randomization options. 
  With this, the previous functionality of 'Force Fully Evolved at Level:' to
  have fully evolved trainer Pokemon before their actual evolution level is now
  supported for 'Trainers Evolve their Pokemon' as well. Therefore, all
  functionality of 'Force Fully Evolved at Level:' is now replicated and
  improved upon by 'Trainers Evolve their Pokemon'. (#136)

### Graphics
- (Gen 5) More Pokémon species can have their palettes randomized (all from Bulbasaur-Girafarig). (#91)
- (Gen 2) New Custom Player Graphics: Duck (from Duck Life) by Happy Time Boredom.

### Misc. Tweaks
- (Gen 4+5) [The description of a misc. tweak addition or change here.]

<!-- Features that don't fit in any of the GUI tabs go in "Misc.". Not to be confused with "Misc. Tweaks". -->
### Misc.
- (CLI) Added options for the command line interface to use settings strings, seeds, and custom player graphics. (#132)
- [The description of some miscellaneous feature (new or changed) here.]

## Bugfixes
- (GUI) Fixed Graphics tab staying disabled when switching between ROMs. (#133)
- (GUI) Fixed radio buttons in the Totem Pokemon panel being unselected when a ROM has been loaded. (#134)
- (Gen 5) Fixed English text being inserted into non-English versions.
  - The text shown when picking starters from the bag, is now in the proper language in the French/German/Spanish/Italian versions, and is unchanged in Japanese and Korean.
  - Cheren's & Hugh's dialogues about what Pokémon they picked, is now unchanged in all non-English versions.
