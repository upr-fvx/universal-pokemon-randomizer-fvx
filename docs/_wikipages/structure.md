---
name: Code Structure of the Universal Pokémon Randomizer FVX
---

This page is for aspiring developers, and others who are curious about the source code of the Universal Pokémon Randomizer FVX.

The Randomizer is divided into five modules.

`docs`: The Randomizer's website, also holding the release notes. 

`devtools`: Devtools, primarily for generating resources for romio/random. 

`random`: The randomization logic, and also the GUI.

`romio`: The interface to the game files (ROMs).

`utils`: Various utilities such as compressors, most of them ports of external code.