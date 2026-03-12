---
name: CLI Randomizer
---
The CLI randomizer can be used by starting the randomizer from the command line with the program argument `cli`.

```
Usage: java [-Xmx4096M] -jar UPR-FVX.jar cli -i <path to source ROM> -o <path for output ROM>
       {-s <path to settings file> | -S <settings string> } [options]
Optional flags: 
-Xmx4096M          : Increase the amount of RAM available to Java. Required for 3DS games.
-z <seed>          : Use the given seed.
-c <name> <type>   : Use a Custom Player Graphics. \"name\" must match a CPG defined in the
                     data folder. \"type\" denotes which player character will be replaced,
                     and must be either PC1 or PC2.
-d                 : Save 3DS game as directory (LayeredFS).
-u <path to update>: Apply the given 3DS game update before randomization.
-l                 : Generate a detailed log file.
-h --help          : Print usage/help info.
```