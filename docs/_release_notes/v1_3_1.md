Hi! UPR FVX 1.3.0 introduced some new bugs, so here's a bug-fix release to remedy that (and some more)!

Thanks to 
@bergmaen and @samualtnorman for your code contributions,
and @SteelPH, @FrostedGeulleisia, @Smurphy2014, and @karthik9313 for reporting Issues!

# How to use

Download the Randomizer below by clicking on `UPR_FVX-v1.3.1.zip`. After downloading, extract the contents of the 
zip file to a folder on your computer. You can then run the Randomizer by double-clicking the appropriate launcher script:

- Windows: Use `launcher_WINDOWS.bat`
- Mac: Use `launcher_MAC.command`
- Other Unix-based systems: Use `launcher_UNIX.sh`

# Changelog
## New and Changed Features

### Trainer Pokemon
- (GUI) Improved spacing of GUI elements.

### Wild Pokemon
- (GUI) Description of "Similar Strength" now mentions "Catch 'Em All" as a confounding setting.

### Misc.
- Improved Linux launcher compatibility, with distros that do not use `/bin/bash` (PR #79).

## Bugfixes
- (Gen 1+3) Fixed randomization failure when using "Randomize PC Potion" Misc. Tweak (Issue #43).
- (Gen 4+5) Fixed TM Shops being counted as Special Shops in all Gen 4 games + Black/White (Issue #73). 
- Fixed "Same Evo Stage" causing randomization failures in certain contexts including "Catch 'Em All" or mapping (Issue #67).
- (Gen 1) Fixed "Rival Carries Starter Through Game" not working, when trainers were randomized (Issue #78).
- (Gen 1) Fixed Yellow Rival's Starter always being Eevee.
- Fixed randomization failure in several games when using "Balance Shop Prices" (Issue #70).
- (Gen 1) Fixed "Balance Shop Prices" setting incorrect prices.
- (Gen 7) Fixed "Rival Carries Starter Through Game" not giving Kukui the final non-chosen starter in his Title Defense battle, in USUM.
