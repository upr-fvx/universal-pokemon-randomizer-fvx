---
layout: default
title: Acknowledgements
---

The Randomizer is an open source project, but it also exists and has only been made possible by the wider (Pokémon) ROM hacking community. Below is an edited account of Dabomstew's original acknowledgements. Its language has been update to reflect the Randomizer becoming an open source project worked on by a string of people, and expanded to include the communities, people, and tools that have helped out since. The original text can be found [archived here](https://web.archive.org/web/20240110170403/https://pokehacks.dabomstew.com/randomizer/acks.php).

---

Many people have put countless hours of their time into researching the structures contained within Pokemon games over the years. Without the research done by these people, this randomizer would not exist, or would have taken a lot longer to create.

Instead of trying to note where every single piece of information used in the Randomizer's creation was found, the major sources of information that were used will be acknowledged, as well as some of the main contributors of said information.

The creators of the external components used will also be acknowledged. These components have been used appropriately subject to their respective software licenses.

Thanks to the efforts of those acknowledged below, time spent making the program can be mainly focused on just that - putting the pieces together to create the program itself. However, sometimes documentation proved insufficient, and original research had to be done. This research can be seen in the comments of the program's source code where appropriate.

## Sources of information

[The Skeetendo community](https://web.archive.org/web/20220929151130/https://hax.iimarckus.org/topic/8058/) was a forum full of hacking information regarding the GameBoy/GameBoy Color Pokemon games. It was shut down in 2022, but was an invaluable resource in the development of the randomizers for these games.

Similarly, [ProjectPokemon](https://projectpokemon.org/) contains a lot of useful information to aid users in hacking the Nintendo DS Pokemon games. The research done by members such as andibad, Kaphotics & Bond697 helped the Generation 5 portions of the Randomizer get off the ground.

[The Pokecommunity](https://www.pokecommunity.com/) is the largest English-language Pokemon ROM hacking community, and also contains a wealth of useful information, especially information relating to GBA/Generation 3 Pokemon games.

[The disassemblies and decompilations by the pret team](https://pret.github.io/), are hard to overstate the value of. They have left an impact comparable to a large meteor on all parts of the ROM hacking community, and randomizers are not excempt from this. When trying to understand the inner workings of a game, nothing beats being able to read its code. 

[The pret Discord server](https://discord.com/invite/d5dubZ3) is the prime forum for discussing the contents of pret resources, and ROM hacking using them. Considering how disassembly hacking now dominates the Gen 1 and 2 scenes, and Gen 3 hacking is in transition of being mostly decomp, this community plays a vital role in sharing knowledge and providing support for hackers of these games.

[The Kingdom of DS Hacking! Discord server](https://discord.com/invite/m4XcSTB4ga) covers what the pret server lacks, by focusing on the Gen 4/5 games. Much information can be found about said games here, and the research conducted by its members furthers our understanding of how to hack them. Special thanks to AdAstra!

[Island of Lost ROM Hacks](https://discord.gg/NrPDYB2mtW) is a smaller Discord server dedicated to the preservation of ROM hacks, and small talk. Its members have provided insight and advice, a place to bounce off ideas for the Randomizer.

## Tools used

Not all the functionality implemented into the Randomizer was documented anywhere, but some of the functionality did already have tools available. Analysing the output of the tools below was another important part of the implementation of this program.

- A-Starter & A-Trainer by Hackmew
- Attack Editor Advance by Scizz
- Attack Editor GB & Poke Edit GB by thethethe
- Item Image Editor & Pokemon Red/Blue Trainer Editor, by Swampert Tools
- PPRE by ProjectPokemon.org
- YAPE by Silver314
- NPRE by pichu2000 ([here](https://code.google.com/archive/p/nintendo-pokemon-rom-editor/))
- Pokanalysis by Ubitux ([here](https://github.com/ubitux/pokanalysis))
- SDSME by Spiky Eared Pichu ([here](https://projectpokemon.org/home/files/file/2099-spikys-ds-map-editor-sdsme/), or [here](https://github.com/Skareeg/SDSME))
- PokeDSPic by loadingNOW
- BWSE by KazoWAR ([here](https://projectpokemon.org/home/forums/topic/13424-kazos-bw-tools/))
- Tinke by pleonex ([here](https://github.com/pleonex/tinke))
- Hex Maniac Advance by haven1433 ([here](https://github.com/haven1433/HexManiacAdvance))
- Sprite decompression webapp by IsoFrieze ([here](https://rgmechex.com/tech/gen1decompress.html)) 

## Libraries/Programs used in the Randomizer

The NDS games introduced a lot of file formats, for which great programs have already been written that handle them. Compression and decompression algorithms for other Generations are also complicated, and have largely been written elsewhere. Rather than reinvent the wheel, these programs have been included inside the Randomizer, under the appropriate software licenses.

- **Generation 4 text handling** is processed by a Java port of loadingNOW's thenewpoketext.
- **Generation 5 text handling** is processed by a Java port of SCV/ProjectPokemon's pptxt.
- **NDS ROM extraction & creation** is handled using a Java port of code from **ndstool**.
- **arm9.bin decompression & compression** is achieved using a Java port of **CUE's BLZ compressor**.
- **Decompression of LZ10/11-compressed images** is achieved using the DSDecmp library.
- **Decompression of Gen 1/2 Pokemon images** is achieved using a Java port of code from [pokemon-reverse-engineering-tools](https://github.com/pret/pokemon-reverse-engineering-tools)
- **Recompression of LZ10/11-compressed images** is achieved using modified code from [AlmiaE](https://github.com/SunakazeKun/AlmiaE).
- **Recompression of Gen 2 images** is achieved using [FuSoYa's Lunar Compress DLL](https://fusoya.eludevisibility.org/lc/index.html), or a Java port of code from [pokecrystal](https://github.com/pret/pokecrystal) on non-Windows OS:es. 
- **3DS ROM extraction & creation** is handled using a Java port of code from Kaphotics' pk3DS and pkNX, and FireyFly's poketools.
- **Reading/parsing BFLIM images** is achieved using Java port of code from KillzXGaming's Switch Toolbox.  

In addition,

- **Pokémon Palette Randomization in Gen 3-5** is achieved using a port of code from [Artemis251's Emerald Randomizer](http://artemis251.fobby.net/downloads/emerald/).

## Images used for Custom Player Graphics

The Custom Player Graphics requires image files to insert into the game. Some of these are from other Pokémon ROM hacking projects, while others were made as general resources. Full credits for each CPG are included in its files, but for good measure an abridged version is here as well. 

All authors were asked when possible, or clearly stated free use for the image. For some old ROM hacks their creators are no longer contact-able on the internet, and so are used under the assumption normally seen in the ROM hacking community, that old work may be iterated upon as long as proper credits are given. Should your work be included and you do not agree, please notify us and it will be removed from the Randomizer.

- voliol (Various)
- altedgy (Blue)
- MollyChan (Various)
- Cutlerine (Ghost, Wraith)
- MrHtuber (Jotaro, Kakyoin)
- BettyNewby (Leaf)
- Luna (Leaf)
- CFA (Luigi)
- jojobear13 (Violet)
- Pokeli (Red)
- DelyBulacha (Frisk, Chara)
- SharkGuy (Boyfriend)
- bepis (Shantae)

## Direct code contributors

Since Dabomstew opened up the Randomizer to open source development, a number of people have worked directly on the Randomizer's codebase. They are listed here. The names used are taken from git, and they are sorted according to when their first git commit (code contribution) was made. For more information on who did what, browse [the project's source code/version control](https://github.com/upr-fvx/universal-pokemon-randomizer-fvx).

- Dabomstew 
- toddblove 
- sickoe 
- Ajarmar 
- cleartonic 
- Tom Overton 
- Sarah 
- Aaron Freytag 
- Stephen Biston 
- spaceonaut
- Kanto 
- pidgezero_one
- SilverstarStream 
- Aric Morrow 
- Zach Meadows 
- Brandon 
- Loren
- voliol
- Realitaetsverlust 
- Stella Hack 
- Michael Long
- Enrico Bergmann

<!--- "Randomizer" is intentionally capitalized in this subheading. Normally on this website it is only capitalized while referring to the UPR, while the concept or randomizers is not. However, here we are paying respects and thus following the "capitalization means respect" principle. Plus Dabomstew capitalized it this way lol.-->
## The Randomizers that came before 

Credit also goes to those who made randomizer programs before this one was released, for the inspiration!

- The original Emerald randomizer by Artemis251
- Artemis251's Red/Blue randomizer
- pateandrew's randomizer for gen2/3 games
- Another R/B randomizer by Ubuntaur
