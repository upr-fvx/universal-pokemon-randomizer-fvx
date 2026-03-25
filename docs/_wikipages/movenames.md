---
name: Move Names
---
## Randomize Move Names

This is a purely cosmetic setting that replaces every move's display name with a randomly generated one. It does not affect gameplay in any way. Move power, accuracy, type, and all other mechanical properties are unchanged.

Because this setting is cosmetic, it uses a separate random number generator so that enabling or disabling it does not alter the results of any other randomization for players who share the same seed.

### How names are generated

Each move name is composed of two parts: a **type word** and an **action word**.

The **type word** is chosen based on the move's elemental type. For example, a Fire-type move might get a word like "Blaze", "Ember", or "Inferno", while a Water-type move might get "Torrent", "Splash", or "Cascade". These words are defined in the `data/TypeMoveNames.txt` file.

The **action word** is chosen based on the move's mechanical properties, in the following priority order:

1. **Punch moves** get punching-themed words (Punch, Jab, Uppercut, etc.)
2. **Sound moves** get sound-themed words (Cry, Roar, Shriek, etc.)
3. **Drain moves** (damaging moves that heal the user) get draining-themed words (Drain, Leech, Siphon, etc.)
4. **Status moves** get subcategory-specific words depending on their effect:
   - Healing moves get words like Heal, Mend, Cure
   - Status-inflicting moves get words matching the status (e.g. Poison, Burn, Freeze, Stun, Lullaby)
   - Trapping moves get words like Snare, Bind, Trap
   - Stat-boosting moves get words like Boost, Empower, Fortify
   - Stat-lowering moves get words like Weaken, Sap, Hinder
5. **Trap moves** (non-status) get trapping-themed words
6. **All other moves** fall back to their category's generic word list (Physical, Special, or Status)

These action words are defined in the `data/CatMoveNames.txt` and `data/SubCatMoveNames.txt` files.

### Additional details

- **Struggle is never renamed.** It is a special internal move and its name is preserved.
- **ALL CAPS detection:** If the ROM stores move names in all capital letters (as some older games do), randomized names will also be converted to all caps for consistency.
- **Name length limits:** Generated names are guaranteed to fit within the ROM's maximum move name length. If a name is too long, the space between the two words is removed to create a compact form. If even that is too long, a different combination is tried.
- **Uniqueness:** No two moves will receive the same name within a single randomization.

### Customizing the word lists

The word list files in the `data/` folder can be edited by the user. The format uses square brackets for keys and parentheses for words:

```
[FIRE] (Flame) (Ember) (Blaze) (Scorch)
```

You can add or remove words, or even add entries for new types, as long as each type has at least one word. See the comments at the top of each file for more details.