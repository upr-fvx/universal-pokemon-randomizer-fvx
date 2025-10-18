Yet another release! Had planned to make this one when Pokémon Palettes in Gen 4 got support for even Pokémon with formes (so that feature can truly be said to be finished, or at least for DP). However, it turned to be even more cumbersome than expected, so have randomizable Type Effectiveness and a rewrite of the random Evolutions code in the meanwhile.
 
Pokemon:
- rewrote the Evolutions code. It should now work more in line with the rest of the Randomizer, following the set options to the dot, instead of being satisfied with 80% correctness. No more getting the occasional Pokémon which evolves into a different type, when "Same Type" is checked. 
However, this also means it will fail completely in more circumstances, when the options are logically impossible to follow.
- new Evolution option: "Force Growth". All Pokémon will evolve into something with a higher BST.
- new Evolution option: "No Convergence". No two Pokémon will evolve into the same Pokémon. 

Types:
- new tab "Types", with randomization options for the Types themselves. For now this means randomizable Type Effectiveness, with the sub-options "Random", "Random (balanced)", "Keep Type Identities", and "Inverse".
- moved "Update Type Effectiveness" from the misc. tweaks to this tab

Graphics:
- improve "Randomize Pokemon Palettes", by adding support for more Pokémon in Gens 4 and 5. In Diamond/Pearl, this feature should be complete except for Pokémon with formes.

---

Download the randomizer below by clicking on `PokeRandoZX-v4_6_0+V0.11.0.zip`. After downloading, extract the contents of the zip file to a folder on your computer. You can then run the randomizer by double clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`