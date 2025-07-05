# Changes - v4.5.1

---
## General

- **NEW FEATURE**: Batch Randomization
    - This feature allows you to generate multiple ROMs at once using the same settings.
        - Settings for batch randomization can be found under Settings -> Batch Randomization Settings. These settings will apply every time you click on the "Randomize (Save)" button.
        - Thanks to @sornerol for this addition!

- FIX: Saving to a Location Without Permissions
    - If you attempt to save a ROM to a location where the randomizer doesn't have permission to write files, it will now display an error dialog instead of crashing.

- FIX: Generation 1 Logs
    - Fixed an issue where the logs created for the Generation 1 games would sometimes use Special Attack/Special Defense rather than using Special like they should.

---
## Pokemon Traits

### Pokemon Evolutions

- CHANGED: Make Evolutions Easier
    - This setting now also makes it so that Pokemon that evolve via friendship will evolve at 160 happiness (down from 220), similar to how it works in the Switch Pokemon games.

- FIX: Random Every Level
    - This setting now cannot be used alongside the "Rival Carries Starter" setting. This combination of settings was never truly intended and could cause the randomizer to crash if used alongside the "Pokemon League Has Unique Pokemon" setting.

---
## Foe Pokemon

### Trainer Pokemon

- CHANGED: Better Movesets
    - Made it so Leech Seed/Perish Song and moves that force switches (e.g. Roar) cannot appear on the same Pokemon.

- FIX: Force Fully Evolved at Level
    - Fixed an issue where enabling this setting would cause premade seeds to give different results each time they were used.