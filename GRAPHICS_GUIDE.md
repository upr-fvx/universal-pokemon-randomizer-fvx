This is a technical guide/overview of the framework for images and palettes included in
the Universal Pokémon Randomizer. 

Please update this as you make changes to the framework, or remove it from your branch 
so that it doesn't contain outdated information. Prioritize self-documenting code (good code structure, 
well-named methods, classes and fields) and JavaDocs over this file.

This should be a guide about the *framework* of the UPR, not the structures for graphics in the ROMs. 
For guides on the latter, see e.g. these ones:
https://voliol.neocities.org/articles/genIIpalettes.html
https://voliol.neocities.org/articles/genIIIpalettes.html 

# Overview

The framework consists of the graphics package, mostly for palettes as of yet,
as well as the older GFXFunctions class, and compressor/decompressor classes.

The graphics package classes have basic JavaDocs, the others do not.

Images can be read to some extent in all generations, and written in I-III. 

Palettes can be read and written in generations I-V.

# Images

The word "image" should be used generally, rather than "sprite". "Sprite" has a meaning within 
the consoles the ROMs are for, as an image that is not a background tile. Many of these methods 
work on background tiles as well, making "sprite" a misnomer.

Images are used by the Randomizer GUI when a ROM is loaded, to display a "mascot" Pokémon, 
and there are methods for dumping all Pokémon images for bugtesting purposes.

Gen I uses Gen1Decmp to decompress images, and Gen1Cmp to compress them. 
Only some images are compressed: the large ones for Pokémon and trainers.

Gen II uses Gen2Decmp to decompress images, and Gen2Cmp to compress them. 
Only some images are compressed: the large ones for Pokémon and trainers.

Gen I and II uses the GBCImage class to represent their images.

Gen III uses DSDecmp to decompress images. (when needed, e.g. Pokémon icons are uncompressed in the ROM)
As there is a DScmp class, these can be compressed and written to ROM. No methods exist specifically for this,
but Gen3RomHandler.rewriteCompressedData can be used when the pointer offset(s) for the image are known.
 
Gen IV and V have file systems, with Pokémon images in the "PokémonGraphics" NARC-file. It should be opened 
beforehand for faster mass operations. Then DSDecmp is used for decompression. The DSCmp class 
could be used for compression, but the methods for inserting into the NARCs don't exist.

Gen VI and VII have file systems, with Pokémon icon images in the "PokémonGraphics" GARC-file. 
More investigation on how these work/the UPR works with them, is needed.
Gen VII uses the BFLIM class for "reading/parsing BFLIM images".

GFXFunctions also has a method, readTiledImageData, that reads a BufferedImage, and converts it to Gen III-V 
image data which can then be written. This method was originally written for the Palette framework, 
to fix the weird Blastoise sprite in FRLG (when palettes are randomized). But as I realized the 
surrounding framework (i.e. all missing writing above) would be substantial the method is unused for now.
This altered Blastoise image file can be found in graphics/resources as "pokeImageFRLG_09f.png", 
and can be used as an example for how images should be (i.e. 4bpp .png files, irfanview is the recommended image editor).

# Palettes

Palettes are read/written by the generation/platform-specific RomHandlers (they can be read/written in all generations), 
and then other palette operations are done in the PaletteHandlers.

The PaletteHandlers for Gen I and II are simple, and their implementations are made for the UPR.

The PaletteHandler for Gen III to V is shared between those generations, and is based on/a port of the 
Palette Randomization system in Artemis251's Emerald Randomizer.
Each game uses a .txt file from graphics/resources for the Pokémon palettes, 
and similar files could be used for other palettes. 
The syntax for these files is explained in the JavaDoc for PalettePartDescription. 

Gen VI and VII do not have a PaletteHandler assigned, though they could probably use the GenIIItoV one.

The Palette class has a function for reading from a BufferedImage with an indexed palette. 