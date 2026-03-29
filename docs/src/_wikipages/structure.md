---
name: Code Structure of the Universal Pokémon Randomizer FVX
---

This page is for aspiring developers, and others who are curious about the source code of the Universal Pokémon Randomizer FVX.

The Randomizer is divided into five modules. There are also some assorted folders like `gradle` and `.github`, but most editing is done inside these modules:

1. `docs`: The Randomizer's website, also holding the release notes. 

2. `devtools`: Developer tools, primarily for generating resources for `romio`/`random`. 

3. `random`: The randomization logic, and also the GUI.

4. `romio`: The interface to the game files (ROMs).

5. `utils`: Various utilities such as compressors, most of them ports of external code.

When working on the Randomizer, you will most likely work inside `random` and/or `romio`, and document your changes inside `docs`. 

Each module depends on the modules *below* it in the above list, and can thus access their code, but not vice versa. This is enforced by the build system. **E.g.**, `Item` (from `romio`) can be used inside `ItemRandomizer` (from `random`), but the reverse would not be possible.