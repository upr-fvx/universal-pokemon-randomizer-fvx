---
name: Custom Player Graphics
---

The FVX branch has support for custom player graphics in the Gen 1-3 games. Basically, this means that you can take a pack of images, and replace the ones in the game with them. The UPR comes pre-packed with some custom player graphics to try out, but you can also make your own quite easily. 
 
This feature was inspired in part by a similar feature in [The ALttP Randomizer](https://alttpr.com/en), and in part by people on Reddit and Discords wanting to easily change the player character, and only it. The UPR already had some customizer features like setting specific starters, and disabling trade evolutions, so it seemed a natural fit.

Below is a general guide for how CPGs work, followed by sections for specific games/Generations.

* [In all games](#in-all-games)
* [In Generation 1](#in-generation-1)
* [In Generation 2](#in-generation-2)
* [In Generation 3 (general)](#in-generation-3-general)
* [In Ruby/Sapphire/Emerald](#in-rubysapphireemerald)
* [In FireRed/LeafGreen](#in-fireredleafgreen)

## In all games

### File structure
To add a custom player graphics, create a new folder within `data/players/`. This folder should contain your image files (and palette files, if you need those), as well as a text file called `info.ini`.

![the randomizer's root folder, with data/ highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/nav1.png)

![the data/ folder, with players/ highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/nav2.png)

![the players/ folder, with snorlax/ highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/nav3.png)

![the snorlax/ folder, showing image files and info.ini]({{ site.baseurl }}/assets/images/wikipages/cpg/nav4.png)

The UPR accepts images in many formats, but ".png" and ".bmp" are strongly recommended. Don't use ".jpg" files. If you know what "indexing" images is, you generally do not need to worry about that. Unindexed images work just fine, as long as they don't have too many colors. This means you can use pretty much any image editing tool to create the images, like MS Paint or [Paint.net](https://www.getpaint.net/) (the exception where indexing is needed, is Gen 3 overworld sprites).

Palettes should be ".pal" files in JASC-PAL format. Palette files are only used in special cases, such as for the reflection palette in Gen 3 games. Normally the palettes are taken from the image files.

`info.ini` contains some info about the custom player graphics, such as who made it, and also tells the UPR how to use the images. The "info.ini" file can contain one or more custom player graphics entries. Each of these entries has a name in square brackets, followed by a number of tags. Each tag has a key, followed by "=", and a value. For example, if you have "RomType=Gen1", that means the custom player graphics are meant for inserting into a Gen 1 game. 

Below is a table of common tags, what values they accept, and what they are used for. Depending on the RomType/what game the custom player graphics are meant for, there are also additional tags, like "UnderwaterSprite" or "FishSpriteMode". The generation-specific pages explain what these tags are.

<table><tr> <th>Key</th> <th>Accepted values</th> <th>Usage</th> </tr>
<tr><td>RomType</td>
	<td>Gen1, Gen2, RSE, FRLG</td> 
	<td>What game the custom player graphics are meant for. Gen1 is for any Generation I game, Gen2 for any Generation II game, RSE for Ruby/Sapphire/Emerald, and FRLG for FireRed/LeafGreen.<br><br>The kinds of images needed depends on the RomType. Read more on the generation-specific pages.</td></tr>
<tr><td>Description</td>
	<td>Any string</td> 
	<td>An arbitrary description of the character the custom player graphics are based on.</td></tr>
<tr><td>From</td>
	<td>Any string</td> 
	<td>Where is the character from? If you are basing your custom player graphics on existing images (like a video game), where are those from, specifically?</td></tr>
<tr><td>Creator</td>
	<td>Any string</td> 
	<td>Who made the the original images? If you are basing your custom player graphics on existing images, who made those? If you made the images from scratch, this is you. Can be multiple people.</td></tr>
<tr><td>Adapter</td>
	<td>Any string</td> 
	<td>Who adapted the images, into a format usable by the UPR/target game. Who made those extra images which were missing? This is (presumably) you, but can also be multiple people in case you didn't make all the extra images.<br><br> 
	If you're also the creator, or the creator is Game Freak because it's the vanilla graphics for the same game (Red for Gen1, May for RSE),you don't need to include this.</td></tr>
<tr><td>FrontImage</td>
	<td>Image file name</td> 
	<td>The file name of the front image.</td></tr>
<tr><td>BackImage</td>
	<td>Image file name</td> 
	<td>The file name of the back image.</td></tr>
<tr><td>WalkSprite</td>
	<td>Image file name</td> 
	<td>The file name of the walk sprite image.</td></tr>
<tr><td>BikeSprite</td>
	<td>Image file name</td> 
	<td>The file name of the bike sprite image.</td></tr>
<tr><td>FishSprite</td>
	<td>Image file name</td> 
	<td>The file name of the fishing sprite image.</td></tr>
</table>

### Example:

Below is an example of the contents of a info.ini file. Double slashes "//" can be used for comments.

```
[Snorlax]
RomType=Gen1
Description=The Pokémon known for blocking roads.
From=Pokémon - Generation I
Creator=Gamefreak
Adapter=Voliol
Category=POKEMON
FrontImage=gb_front.png
BackImage=gb_back.png
WalkSprite=gb_walk.png
BikeSprite=gb_bike.png
FishSpriteMode=combined
FishSprite=gb_fish.png

[Snorlax]
RomType=Gen2 // Note this is different from "RomType=Gen1" above. 
Description=The Pokémon known for blocking roads.
From=Pokémon - Generation I
Creator=Gamefreak
Adapter=Voliol
Category=POKEMON
FrontImage=gbc_front.png
BackImage=gbc_back.png
WalkSprite=gb_walk.png
BikeSprite=gb_bike.png
FishSpriteMode=combined
FishSprite=gb_fish.png
```

---

## In Generation 1

[Showcase gif]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/showcase.gif)

### General info

Due to the Gen 1 games being for the original GameBoy, all graphics are monochrome, with 4 shades. The UPR can detect the shades used in your images, so as long as your image contains no more than four colors, it will be fine. You do not have to index the palette.

For overworld sprites, the lightest color in the source image is used for "transparent", instead of "white".

The example below shows that both source images (colored and monochrome) give the same result when inserted into the game.

![a colored source image]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_walk_colored.png) or ![a monochrome source image]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_walk.png) &rarr; ![a screenshot of the resulting sprite in-game]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_screenshot.png)

### Graphic specifications

The player has a front image, a back image, and sprites for walking, cycling, and fishing in the overworld.

The front image is 56x56 pixels, and the back image is 32x32 pixels. The bottom 4 rows of pixels of the back image should be white. Examples of valid front and back images below:

![front image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_front.png)
![front image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_front.png)
![front image #3]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_front.png)
![front image #4]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_front.png)
![back image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_back.png)
![back image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_back.png)
![back image #3]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_back.png)
![back image #4]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_back.png)

The walk sprite and bike sprite have identical specifications. They are both 16x96 pixels, split into 6 frames of 16x16 pixels each. Examples of valid walk and bike sprites below:

![walk sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_walk.png)
![walk sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_walk.png)
![walk sprite #3]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_walk.png)
![walk sprite #4]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_walk.png)
![bike sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_bike.png)
![bike sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_bike.png)
![bike sprite #3]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_bike.png)
![bike sprite #4]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_bike.png)

Like the walk and bike sprites, the fishing sprite has frames for the directions up/down/side. However, they only have 1 frame for each direction, 
and this frame only covers the bottom half of what is seen on screen. The top half is taken from the walk sprite frame facing the appropriate direction.

![walk sprite with the part seen in screenshot highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_walk_highlight.png) + 
![fishing sprite with the part seen in screenshot highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_fish_highlight.png) &rarr;
![screenshot of the player fishing ]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/fishing_screenshot.png)

The UPR can read fishing sprites in two "modes". With the "separate" mode, each frame will be read from a separate 8x16 pixel image. 
With the "combined" mode, all frames will be read from a single 16x24 pixel image. This is controlled Examples of valid fishing sprites below:

![fishing sprite separate #1-front]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_fish_front.png)
![fishing sprite separate #1-back]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_fish_back.png)
![fishing sprite separate #1-side]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_fish_side.png),
![fishing sprite separate #2-front]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_fish_front.png)
![fishing sprite separate #2-back]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_fish_back.png)
![fishing sprite separate #2-side]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_fish_side.png),
![fishing sprite separate #3-front]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_fish_front.png)
![fishing sprite separate #3-back]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_fish_back.png)
![fishing sprite separate #3-side]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_fish_side.png),
![fishing sprite separate #4-front]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_fish_front.png)
![fishing sprite separate #4-back]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_fish_back.png)
![fishing sprite separate #4-side]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_fish_side.png),
![fishing sprite combined #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/red_fish.png)
![fishing sprite combined #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/blackmage_fish.png)
![fishing sprite combined #3]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/snorlax_fish.png)
![fishing sprite combined #4]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/mario_fish.png)

### List of tags

<table><tr> <th>Key</th> <th>Accepted values</th> <th>Usage</th> </tr>
<tr><td>FishSpriteMode</td>
	<td><b>Separate</b> or <b>Combined</b></td> 
	<td>Controls whether the fishing sprite will be read from 3 separate images (one for each frame), or a single 
	image.</td></tr>
<tr><td>FishFrontSprite</td>
	<td>Image file name</td> 
	<td>The file name of the south-facing fishing sprite frame image. Used only if FishSpriteMode is "Separate".</td></tr>
<tr><td>FishBackSprite</td>
	<td>Image file name</td> 
	<td>The file name of the north-facing fishing sprite frame image. Used only if FishSpriteMode is "Separate".</td></tr>
<tr><td>FishSideSprite</td>
	<td>Image file name</td> 
	<td>The file name of the east/west-facing fishing sprite frame image. Used only if FishSpriteMode is "Separate".</td></tr>
</table>

### Other/unsupported graphics

On the title screen, there is an image of Red holding a Poké Ball. The UPR has no support for changing it. There is no support for changing the fishing rod.

![a screenshot of the Pokémon Blue title screen]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/title_screen_screenshot.png)
![the fishing rod sprite]({{ site.baseurl }}/assets/images/wikipages/cpg/gen1/fishing_rod.png)

---

## In Generation 2

[Showcase gif]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/showcase.gif)

### General info

Perhaps because the Gen 2 games were originally set up to be playable on the original GameBoy, as well as the GameBoy color, all graphics have 4 colors: "white", "black", and two others. The UPR can detect the shades used in your images, so as long as your image contains no more than four colors, it will be fine. You do not have to index the palette.

For overworld sprites, the lightest color in the source image is used for "transparent", instead of "white".

The example below shows that both source images (colored and monochrome) give the same result when inserted into the game.

![a colored source image]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_walk.png) or ![a monochrome source image]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_walk_monochrome.png) &rarr;
![a screenshot of the resulting sprite in-game]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_screenshot.png)

What colors are actually used for an image/sprite depends on the palette loaded for it. The front and back images will use the palette of the given front image. The overworld sprites will use one of 8 predefined palettes, chosen in `ini.txt`.

### Graphic specifications

The player has a front image, a back image, a trainer card image, and sprites for walking, cycling, and fishing in the overworld.

The front image is 56x56 pixels, and the back image is 48x48 pixels. The trainer card image is 40x56 pixels, and normally just a cropped down version of the front image, with a triangle in the corner in Gold/Silver. The UPR knows how to crop down a front image to create a trainer card image, so you don't have to include the latter if you don't want to.

Examples of valid front, back, and trainer card images below. The last trainer card image includes the aforementioned "triangle in the corner":

![front image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_front.png)
![front image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_front.png)
![back image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_back.png)
![back image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_back.png)
![trainer card image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_trainer_card.png)
![trainer card image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_trainer_card.png)

The walk sprite and bike sprite have identical specifications. They are both 16x96 pixels, split into 6 frames of 16x16 pixels each. 
Examples of valid walk and bike sprites below:

![walk sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_walk.png)
![walk sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_walk.png)
![bike sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_bike.png)
![bike sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_bike.png)

Like the walk and bike sprites, the fishing sprite has frames for the directions up/down/side. However, they only have 1 frame for each direction, 
and this frame only covers the bottom half of what is seen on screen. The top half is taken from the walk sprite frame facing the appropriate direction.

![walk sprite with the part seen in screenshot highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_walk_highlight.png) + 
![fishing sprite with the part seen in screenshot highlighted]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_fish_highlight.png) &rarr;
![screenshot of the player fishing ]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/fishing_screenshot.png)

The UPR can read fishing sprites in two "modes". With the "separate" mode, each frame will be read from a separate 8x16 pixel image. 
With the "combined" mode, all frames will be read from a single 16x24 pixel image. Examples of valid fishing sprites below:

![fishing sprite separate #1-front]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_fish_front.png)
![fishing sprite separate #1-back]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_fish_back.png)
![fishing sprite separate #1-side]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_fish_side.png),
![fishing sprite separate #2-front]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_fish_front.png)
![fishing sprite separate #2-back]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_fish_back.png)
![fishing sprite separate #2-side]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_fish_side.png),
![fishing sprite combined #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/kris_fish.png)
![fishing sprite combined #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/other_fish.png)

### List of tags

<table><tr> <th>Key</th> <th>Accepted values</th> <th>Usage</th> </tr>
<tr><td>SpritePalette</td>
	<td>RED, BLUE, GREEN, BROWN, PINK, EMOTE, TREE, ROCK</td> 
	<td>Decides what palette will be used for the overworld sprites.
	<br><br>
	RED, BLUE, GREEN, and BROWN are used for NPCs in the Vanilla games, while PINK is an unused palette very similar to RED. EMOTE, TREE, and ROCK are used by emotes, cuttable trees, and breakable rocks respectively.</td></tr>
<tr><td>FishSpriteMode</td>
	<td><b>Separate</b> or <b>Combined</b></td> 
	<td>Controls whether the fishing sprite will be read from 3 separate images (one for each frame), or a single 
	image.</td></tr>
<tr><td>FishFrontSprite</td>
	<td>Image file name</td> 
	<td>The file name of the south-facing fishing sprite frame image. Used only if FishSpriteMode is "Separate".</td></tr>
<tr><td>FishBackSprite</td>
	<td>Image file name</td> 
	<td>The file name of the north-facing fishing sprite frame image. Used only if FishSpriteMode is "Separate".</td></tr>
<tr><td>FishSideSprite</td>
	<td>Image file name</td> 
	<td>The file name of the east/west-facing fishing sprite frame image. Used only if FishSpriteMode is "Separate".</td></tr>
</table>

### Other/unsupported graphics

There is no support for changing the fishing rod. 

![the fishing rod sprite]({{ site.baseurl }}/assets/images/wikipages/cpg/gen2/fishing_rod.png)

---

## In Generation 3 (general)

### General info

All the customizable player graphics use indexed palettes of size 16. Since 1 color is always used for transparency, this in effect means each image can have up to <b>15</b> colors. 

The transparent color is picked differently depending on whether your source image is indexed or not. If it is, the color at index 0 will be picked. If it isn't, then the color of pixel in the top right corner will be picked. Generally, this means you don't have to worry about indexing the source image correctly, since the UPR takes care of transparency. However, the overworld sprites mostly use the same palette, so you want to keep track of the palette indexing when working on them.

### Graphic specifications

The player has a front image, a back image, a map icon image, and various sprites (walking, cycling, fishing etc.) used in the overworld.
<br><br>
The front image is 64x64 pixels, and the back image is 64x256 pixels in RSE and 64x320 pixels in FRLG, divided into 4 respectively 5 64x64 pixel frames. Examples of valid front and back images below. The first back image is valid for RSE, and the second for FRLG:

![front image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_front.png)
![front image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_front.png)
![back image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_back.png)
![back image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_back.png)

The map icon image is 16x16 pixels. Examples of valid map icon images below.

![trainer map icon image #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_icon.png)
![trainer map icon image #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_icon.png)

The walk sprite is 144x32 pixels, split into 9 frames of 16x32 pixels each. 
Examples of valid walk sprites below:

![walk sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_walk.png)
![walk sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_walk.png)

The run sprite too is 144x32 pixels, split into 9 frames of 16x32 pixels each. However, depending on which "mode" it is read in, these frames will be assumed to be in different orders. The "RSE" mode assumes the same order as the frames in the walk sprite, while the "FRLG" mode assumes each "direction" is grouped together. Examples of valid run sprites below. The first run sprite is valid for "RSE" mode, and the second for "FRLG" mode:

![run sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_run.png)
![run sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_run.png)

The bike sprite is 288x32 pixels, split into 9 frames of 32x32 pixels each. In RSE, this sprite is used for the Mach Bike. Examples of valid bike sprites below:

![bike sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_bike.png)
![bike sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_bike.png)

The fishing sprite is 384x32 pixels, split into 12 frames of 32x32 pixels each. Examples of valid fishing sprites below:

![fishing sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_fish.png)
![fishing sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_fish.png)

The size of the sit sprite (used primarily when surfing) varies between games. In RSE it is 96x32 pixels, split into 3 frames of 32x32 pixels each. In FRLG it is 48x32 pixels, split into 3 frames of 16x32 pixels eachs. Examples of valid sit sprites below. The first sit sprite is valid for RSE, and the second for FRLG: 

![sit sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_sit.png)
![sit sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_sit.png)

The size of the surf blob sprite varies between games. In RSE it is 96x32 pixels, split into 3 frames of 32x32 pixels each. In FRLG it is 192x32pixels, split into 6 frames of 32x32 pixels each. Note that the surf blob sprite shares its palette with most other overworld sprites. The first surf blob sprite is valid for RSE, and the second for FRLG: 

![surf blob sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_surfblob.png)
![surf blob sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_surfblob.png)

The size of the bird sprite (which appears when Fly is used) varies between games. In RSE it is 32x32 pixels, with a single frame. In FRLG it is 64x192 pixels, split into 3 frames of 64x64 pixels each. Note that the bird sprite shares its palette with most other overworld sprites. The first bird sprite is valid for RSE, and the second for FRLG:

![bird sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/may_bird.png)
![bird sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/gen3/red_bird.png)

<!-- TODO: how does the indexing work? can you use a non-indexed walk image as long as the same 16 or less colors are used?? -->
All overworld sprites mentioned above share the same normal palette, and the same reflection palette. These are 16-colors palettes, of which the first color is "transparent". By default, the normal palette is derived from the walking image, and the reflection palette is a copy of the normal palette. However, they can also be assigned custom palettes through ".pal" files. Below is an example of a valid .pal file:

```
JASC-PAL
0100
16
148 230 230
255 238 213
255 213 213
238 180 180
172 156 164
164 164 180
106 123 148
106 123 148
123 131 131
238 238 255
164 246 156
123 205 123
255 156 156
230 123 139
255 255 255
106 115 106
```

### List of tags

| Key                     | Accepted values     | Usage                                      |
|-------------------------|---------------------|--------------------------------------------|
| MapIcon                 | Image file name     | The file name of the map icon image.       |
| RunSpriteMode           | **RSE** or **FRLG** | The mode for reading the run sprite image. |
| RunSprite               | Image file name     | The file name of the run sprite image.     |
| SpriteNormalPalette     | Palette file name   | The file name of the normal palette.       |
| SpriteReflectionPalette | Palette file name   | The file name of the reflection palette.   |

---

## In Ruby/Sapphire/Emerald

[TODO: showcase gif]

### Graphic specifications

The sit/jump sprite (used when jumping into water/onto the fly bird) is 92x32 pixels, split into 3 frames of 32x32 pixels each. Examples of valid sit/jump sprites below:

![sit/jump sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_sitjump.png)
![sit/jump sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_sitjump.png)

The acro bike sprite is 864x32 pixels, split into 27 frames of 32x32 pixels each. Examples of valid sit/jump sprites below:

![acro bike sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_acrobike.png)
![acro bike sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_acrobike.png)

The underwater sprite is 96x32 pixels, split into 3 frames of 32x32 pixels each. Note that the underwater sprite uses a different palette from all other overworld sprites. Examples of valid underwater sprites below:

![underwater sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_underwater.png)
![underwater sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_underwater.png)

The watering can sprite is 192x32 pixels, split into 6 frames of 32x32 pixels each. Examples of valid underwater sprites below:

![watering can sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_wateringcan.png)
![watering can sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_wateringcan.png)

The decorate sprite is 16x32 pixels, with a single frame. Examples of valid decorate sprites below:

![decorate sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_decorate.png)
![decorate sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_decorate.png)

And finally, the field move sprite is 160x32 pixels, split into 5 frames of 32x32 pixels each. Examples of valid decorate sprites below:

![field move sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_fieldmove.png)
![field move sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_fieldmove.png)

### List of tags

| Key               | Accepted values | Usage                                           |
|-------------------|-----------------|-------------------------------------------------|
| SitJumpSprite     | Image file name | The file name of the sit/jump sprite image.     |
| AcroBikeSprite    | Image file name | The file name of the acro bike sprite image.    |
| Underwater        | Image file name | The file name of the underwater sprite image.   |
| WateringCanSprite | Image file name | The file name of the watering can sprite image. |
| DecorateSprite    | Image file name | The file name of the decorate sprite image.     |
| FieldMoveSprite   | Image file name | The file name of the field move sprite image.   |

### Other/unsupported graphics

During the intro movie, the player is shown riding a bike. In Emerald, this is also shown during the credits. This bike is split into two images, for the player and for the bike. The UPR does not support changing either.

![intro brendan (ruby/sapphire)]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_introbike_rs.png)
![intro may (ruby/sapphire)]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_introbike_rs.png)
![intro brendan (emerald)]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/brendan_introbike_e.png) 
![intro may (emerald)]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/may_introbike_e.png)
![the bike itself (ruby/sapphire)]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/introbike_rs.png)
![the bike itself (emerald)]({{ site.baseurl }}/assets/images/wikipages/cpg/rse/introbike_e.png)

---

## In FireRed/LeafGreen

[TODO: showcase gif]

### Graphic specifications

Ihe item sprite (used when using a field move or the VS Seeker) is 144x32 pixels, split into 9 frames of 16x32 pixels each. Examples of valid decorate sprites below:

![field move sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/frlg/leaf_item.png)
![field move sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/frlg/red_item.png)

Ihe item/bike sprite (used when using the VS Seeker on a bike) is 192x32 pixels, split into 6 frames of 32x32 pixels each. Examples of valid decorate sprites below:

![field move sprite #1]({{ site.baseurl }}/assets/images/wikipages/cpg/frlg/leaf_itembike.png)
![field move sprite #2]({{ site.baseurl }}/assets/images/wikipages/cpg/frlg/red_itembike.png)

### List of tags

| Key            | Accepted values | Usage                                        |
|----------------|-----------------|----------------------------------------------|
| ItemSprite     | Image file name | The file name of the item sprite image.      |
| ItemBikeSprite | Item file name  | The file name of the item/bike sprite image. |

### Other/unsupported graphics

The large images shown during the player select are not supported. These use a palette of 32 colors, unlike all supported images.

![the player select red image]({{ site.baseurl }}/assets/images/wikipages/cpg/frlg/red_playerselect.png)
![the player select leaf image]({{ site.baseurl }}/assets/images/wikipages/cpg/frlg/leaf_playerselect.png)
