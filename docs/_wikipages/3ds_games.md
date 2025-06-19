---
name: Randomizing the 3DS games
---
## Intro

Owing to work done for the Universal Pokemon Randomizer ZX, UPR FVX supports randomizing all the mainline Pokemon games on the 3DS. However, 3DS games are quite different from games on older Nintendo systems; they can come in a variety of different formats, and they can be updated with downloadable patches. This page will explain the various 3DS file formats, how to use game updates, and how to randomize your games for use on both emulator and real hardware.

## Dumping your games from your 3DS

If you have a 3DS running custom firmware (CFW), you can dump your Pokemon games and game updates yourself for use with the randomizer. Depending on whether you own your games physically or digitally, you can follow the following guides from the Citra wiki to dump your games:

* [Dumping Game Cartridges](https://citra-emulator.com/wiki/dumping-game-cartridges/)
* [Dumping Installed Titles](https://citra-emulator.com/wiki/dumping-installed-titles/)
* [Dumping Updates and DLC](https://citra-emulator.com/wiki/dumping-updates-and-dlcs/)

## 3DS game formats

These are the primary formats that you'll see for 3DS games. **So long as your game is decrypted, Universal Pokemon Randomizer FVX can work with all of these!**

* **CCI/3DS** - Short for **C**TR **C**art **I**mage. This is a dump of a game from a cartridge. It very often has the .3ds file extension, since cartridge dumps from the original DS had the .nds extension. The randomizer supports both .3ds and .cci file extensions.
* **CIA** - Short for **C**TR **I**mportable **A**rchive. This is a dump of an installed title, usually purchased from the eShop. Digital games are dumped in this format, and game updates are also in this format.
* **CXI** - Short for **C**TR e**X**ecutable **I**mage. You can consider this the "bare minimum" file format, as it contains only what is necessary to execute a program and nothing more. Both CCIs and CIAs *contain* a CXI for a game, along with other things like the game manual and system updates. None of these extras are strictly necessary for running a game, however.

## Changes to saving a ROM when working with 3DS games

If you click on the "Randomize (Save)" button when you've loaded a 3DS game, you'll see the following dialog:

![Image of 3DS output dialog]({{ site.baseurl }}/assets/images/wikipages/3ds_games/output_dialog.png)

The "CXI" option will output the game as a single CXI file; this file contains all the information necessary to run the game in an emulator. However, this file is *quite* large (it ranges from 1.67 GB for X/Y to 3.45 GB for Ultra Sun/Ultra Moon), so it can take a while to generate and can be difficult to share with others. The alternative is to create a LayeredFS directory; this is a directory that *only* contains the files necessary to randomize the game. You can think of it as a directory full of "patches" to the original game. A LayeredFS directory is significantly smaller than an equivalent CXI, and it can be used to run your randomized game both on an emulator and on a real 3DS with CFW.

## 3DS game updates

When a 3DS game is loaded in the randomizer, the Settings submenu will have new options to let you load a game update into the randomizer or unload a game update if one is currently loaded:

![Image of Settings submenu when a 3DS game is loaded]({{ site.baseurl }}/assets/images/wikipages/3ds_games/settings.png)

The "Load Game Update" option loads a game update (in CIA format) into the randomizer. Loading a game update will allow you to create a randomized version of the game with that update applied. For the 3DS games, these updates are almost exclusively bug fixes, but some bugs are either [game-breaking](https://bulbapedia.bulbagarden.net/wiki/Lumiose_City_save_glitch) or can [negatively affect randomization](https://bulbapedia.bulbagarden.net/wiki/List_of_glitches_in_Generation_VII#Evolution_move_learning_glitch). When you load a game update for the first time, the randomizer will remember the location of that update and automatically load it every time you load its associated game. If you want to stop using a game update, simply select "Unload Game Update".

There are two important things to keep in mind when using game updates with the randomizer:
- When a game update is loaded, the randomizer can *only* output your randomized game as a LayeredFS directory. This is a technical limitation based on how 3DS game updates work.
- **If you create a LayeredFS directory using a particular version of the game, you *MUST* have that *same* version of the game installed in Citra or on your 3DS!** Ignoring this advice can lead to negative consequences, including the randomization simply not working or the game outright crashing upon booting up. To help you determine which version of the game update you need to install, the [ROM Information section]({{ site.baseurl }}/assets/images/wikipages/3ds_games/rom_info.png) in the randomizer will display the game's version if a game update is loaded. For more information about managing game updates in Citra or on your 3DS, please read [Managing game updates](#managing-game-updates).

## Guide for creating a randomized game
1. Open the Universal Pokemon Randomizer FVX
2. Click on the "Open ROM" button and then select your decrypted 3DS game
3. If desired, use Settings -> Load Game Update to load a decrypted game update for this game
4. Adjust the settings to your liking
5. Click on the "Randomize (Save)" button. If you loaded a game update, then you are required to output as a LayeredFS directory, and no choice will be provided to you; head to the sections on running a randomized LayeredFS [in Citra](#guide-for-playing-your-randomized-layeredfs-directory-on-the-citra-emulator) or [on a 3DS](#guide-for-playing-your-randomized-layeredfs-directory-on-a-3ds-running-cfw). Otherwise, you can choose between CXI or LayeredFS output. If you plan on only using the Citra emulator, then outputting as CXI [will make it easier to use within the emulator](#guide-for-playing-your-randomized-cxi-on-the-citra-emulator). Otherwise, choose LayeredFS so you can play your randomized game both [in Citra](#guide-for-playing-your-randomized-layeredfs-directory-on-the-citra-emulator) or [on a 3DS](#guide-for-playing-your-randomized-layeredfs-directory-on-a-3ds-running-cfw).

## Guide for playing your randomized CXI on the Citra emulator

These steps below assume that you have a randomized CXI, created as described in [this section](#guide-for-creating-a-randomized-game).

1. If you loaded a game update in the randomizer, make sure that Citra has the same version of that game update installed. If you did *not* load a game update, then make sure that Citra *also* does not have any game updates for the game installed. The [Managing game updates in Citra](#managing-game-updates-in-citra) section will help you with this.
2. In Citra, select File -> Load File..., then load the CXI you created earlier

## Guide for playing your randomized LayeredFS directory on the Citra emulator

These steps below assume that you have a randomized LayeredFS directory, created as described in [this section](#guide-for-creating-a-randomized-game).

1. If you loaded a game update in the randomizer, make sure that Citra has the same version of that game update installed. If you did *not* load a game update, then make sure that Citra *also* does not have any game updates for the game installed. The [Managing game updates in Citra](#managing-game-updates-in-citra) section will help you with this.
2. In Citra, right-click the game in the game list and select "Open Mods Location"
2. Navigate to where you saved your LayeredFS directory; you should see a folder whose name consists of sixteen hexadecimal digits which vary depending on the game. Copy what's *within* the LayeredFS directory and paste it in the mod folder that you opened in the last step. You're doing it correctly if you're copying over a "romfs" directory and a file called "code.bin"
3. Double-click on the game in the game list to load the now-modified game

## Guide for playing your randomized LayeredFS directory on a 3DS running CFW

These steps below assume you are running [Luma3DS](https://github.com/LumaTeam/Luma3DS) custom firmware and that you have a randomized LayeredFS directory, created as described in [this section](#guide-for-creating-a-randomized-game).

1. Insert your 3DS's SD card into your computer, then navigate to where you saved your LayeredFS directory; you should see a folder whose name consists of sixteen hexadecimal digits which vary depending on the game. Copy this directory and paste it to sdroot:/luma/titles
2. Eject your 3DS's SD card from your computer and insert it back into your 3DS
3. Turn on your 3DS and enter Luma configuration mode (this is usually done by holding Select while pressing Power). Ensure that "Enable game patching" is enabled [like so]({{ site.baseurl }}/assets/images/wikipages/3ds_games/enable_patching.png), then press Start to save and reboot into the regular 3DS Home Menu.
4. If you loaded a game update in the randomizer, make sure that your 3DS has the same version of that game update installed. If you did *not* load a game update, then make sure that your 3DS *also* does not have any game updates for the game installed. The [Managing game updates on a 3DS](#managing-game-updates-on-a-3ds) section will help you with this.
5. Return to the 3DS Home Menu and select your Pokemon game

## Managing game updates

To repeat what was said earlier: **if you create a LayeredFS directory using a particular version of the game, you *MUST* have that *same* version of the game installed in Citra or on your 3DS.** This section will teach you how to manage game updates so that you can make sure your version of the game matches the version you've loaded in the randomizer.

#### Managing game updates in Citra
To install a game update...
- Select File -> Install CIA... and select your game update CIA. Wait a bit for the installation to complete and that's it.

To remove a game update...
- Unfortunately, there is no streamlined way of doing this in Citra. The best way to do this is to right-click the game in the game list and then select "Open Update Data Location" (if you don't see this option, then you don't have updates installed and can move on)
- You should be in a directory that ends with 0004000e/XXXXXXXX/content; make a note of what XXXXXXXX is, then go up two directories so you are now in the 0004000e directory
- Close Citra (if you don't, the next step may fail)
- Delete the directory named after what you took note of before. The update is now removed, so you can reopen Citra.

#### Managing game updates on a 3DS
To install a game update...
- If you need the latest version of the game (v1.5 for X/Y, v1.4 for OR/AS, v1.2 for all Gen 7 games), you can simply download it for free straight from Nintendo. Just make sure your 3DS is connected to the internet and then select the game in the Home Menu. If the game is not fully updated, it will prompt you to install the latest update.
- Otherwise, you can use FBI (requires CFW) to install any arbitrary game update CIA to your 3DS. Navigate to the location of the CIA on your SD card, press A to select it, and then select "Install CIA".

To remove a game update...
- Open the Settings app, then select "Data Management"
- Select the "Nintendo 3DS" tile, then select "Downloadable Content" and find the update data for your desired game. [Here is an example]({{ site.baseurl }}/assets/images/wikipages/3ds_games/update_example.png) to give you an idea
- Press the large "Delete" button