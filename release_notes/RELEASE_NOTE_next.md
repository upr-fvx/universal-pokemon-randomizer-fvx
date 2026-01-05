Hello again! Here comes UPR FVX v1.4.0, with all sorts of goodies. More precise evolution control has been granted to both your own mons and enemy trainers, 
but also "Better Movesets" in Gen 1 + 2, improvements to the Command Line Interface, etc. etc...

Thanks to 
@Glamurio and @bergmaen for your code contributions,
@CDNievas, @TheFreezingChicken, and @scipio19 for reporting Issues, and
Happy Time Boredom from Spriter's Resource for the CPG sprites!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.4.0.zip`. After downloading, extract the contents of the 
zip file to a folder on your computer. You can then run the Randomizer by double-clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features

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

### Misc.
- (CLI) Added options for the command line interface to use settings strings, seeds, and custom player graphics. (#132)

## Bugfixes
- (GUI) Fixed Graphics tab staying disabled when switching between ROMs. (#133)
- (GUI) Fixed radio buttons in the Totem Pokemon panel being unselected when a ROM has been loaded. (#134)
- (Gen 5) Fixed English text being inserted into non-English versions.
  - The text shown when picking starters from the bag, is now in the proper language in the French/German/Spanish/Italian versions, and is unchanged in Japanese and Korean.
  - Cheren's & Hugh's dialogues about what Pokémon they picked, are now unchanged in all non-English versions.
