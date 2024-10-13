---
name: Custom Player Graphics
---

The FVX branch has support for custom player graphics in the Gen 1-3 games. Basically, this means that you can take a pack of images, and replace the ones in the game with them.
 
This feature was inspired in part by a similar feature in [The ALttP Randomizer](https://alttpr.com/en) and in part by people on Reddit and Discords wanting to easily change the player character, and only it. The UPR already had some customizer features like setting specific starters, and disabling trade evolutions, so it seemed a natural fit.

<details><summary>Showcase gifs:</summary>
[in-game footage of custom player graphics in Gen 1](showcase_gen1.gif)
[in-game footage of custom player graphics in Gen 2](showcase_gen2.gif)
</details>

The UPR comes pre-packed with some custom player graphics to try out, but you can also make your own quite easily. 
Below are links to pages with generation-specific information, followed by a guide on how to do it in general.

[TODO: navbox]

## File structure
To add a custom player graphics, create a new folder within `data/players`. This folder should contain your image files (and palette files, if you need those), as well as a text file called `info.ini`.

[the players/ folder](nav1.png)
[inside the players/ folder](nav2.png)
[inside a subfolder of players/, showing image files and info.ini](nav3.png)

The UPR accepts images in many formats, but ".png" and ".bmp" are strongly recommended. Don't use ".jpg" files. If you know what "indexing" images is, you generally do not need to worry about that. Unindexed images work just fine, as long as they don't have too many colors. This means you can use pretty much any image editing tool to create the images, like MS Paint or [Paint.net](https://www.getpaint.net/) (the exception where indexing is needed, is Gen III overworld sprites).

Palettes should be ".pal" files in JASC-PAL format. Palette files are only used in special cases, such as for the reflection palette in Gen III games. Normally the palettes are taken from the image files.

`info.ini` contains some info about the custom player graphics, such as who made it, and also tells the UPR how to use the images. The "info.ini" file can contain one or more custom player graphics entries. Each of these entries has a name in square brackets, followed by a number of tags. Each tag has a key, followed by "=", and a value. For example, if you have "RomType=Gen1", that means the custom player graphics are meant for inserting into a Gen I game. 

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

## Example:

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

