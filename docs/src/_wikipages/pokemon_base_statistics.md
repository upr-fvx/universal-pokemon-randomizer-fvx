---
name: Pokemon Base Statistics
---
## Updated Base Stats

Following are the Pokemon that received updated base stats in each generation. Note that updating base stats to a certain generation will include all changes that happened in previous generations as well (for example: if you update to generation 7, you will also receive the updates that happened in generation 6).

### Generation 6

* **Butterfree**
    * Special Attack 80 -> 90
* **Beedrill**
    * Attack 80 -> 90
* **Pidgeot**
    * Speed 91 -> 101
* **Pikachu**
    * Defense 30 -> 40
    * Special Defense 40 -> 50
* **Raichu**
    * Speed 100 -> 110
* **Nidoqueen**
    * Attack 82 -> 92
* **Nidoking**
    * Attack 92 -> 102
* **Clefable**
    * Special Attack 85 -> 95
* **Wigglytuff**
    * Special Attack 75 -> 85
* **Vileplume**
    * Special Attack 100 -> 110
* **Poliwrath**
    * Attack 85 -> 95
* **Alakazam**
    * Special Defense 85 -> 95
* **Victreebel**
    * Special Defense 60 -> 70
* **Golem**
    * Attack 110 -> 120
* **Ampharos**
    * Defense 75 -> 85
* **Bellossom**
    * Defense 85 -> 95
* **Azumarill**
    * Special Attack 50 -> 60
* **Jumpluff**
    * Special Defense 85 -> 95
* **Beautifly**
    * Special Attack 90 -> 100
* **Exploud**
    * Special Defense 63 -> 73
* **Staraptor**
    * Special Defense 50 -> 60
* **Roserade**
    * Defense 55 -> 65
* **Stoutland**
    * Attack 100 -> 110
* **Unfezant**
    * Attack 105 -> 115
* **Gigalith**
    * Special Defense 70 -> 80
* **Seismitoad**
    * Attack 85 -> 95
* **Leavanny**
    * Special Defense 70 -> 80
* **Scolipede**
    * Attack 90 -> 100
* **Krookodile**
    * Defense 70 -> 80

### Generation 7

* **Arbok**
    * Attack 85 -> 95
* **Dugtrio**
    * Attack 80 -> 100
* **Mega Alakazam**
    * Special Defense 95 -> 105
* **Farfetch'd**
    * Attack 65 -> 90
* **Dodrio**
    * Speed 100 -> 110
* **Electrode**
    * Speed 140 -> 150
* **Exeggutor**
    * Special Defense 65 -> 75
* **Noctowl**
    * Special Attack 76 -> 86
* **Ariados**
    * Special Defense 60 -> 70
* **Qwilfish**
    * Defense 75 -> 85
* **Magcargo**
    * HP 50 -> 60
    * Special Attack 80 -> 90
* **Corsola**
    * HP 55 -> 65
    * Defense 85 -> 95
    * Special Defense 85 -> 95
* **Mantine**
    * HP 65 -> 85
* **Swellow**
    * Special Attack 50 -> 75
* **Pelipper**
    * Special Attack 85 -> 95
* **Masquerain**
    * Special Attack 80 -> 100
    * Speed 60 -> 80
* **Delcatty**
    * Speed 70 -> 90
* **Volbeat**
    * Defense 55 -> 75
    * Special Defense 75 -> 85
* **Illumise**
    * Defense 55 -> 75
    * Special Defense 75 -> 85
* **Lunatone**
    * HP 70 -> 90
* **Solrock**
    * HP 70 -> 90
* **Chimecho**
    * HP 65 -> 75
    * Defense 70 -> 80
    * Special Defense 80 -> 90
* **Woobat**
    * HP 55 -> 65
* **Crustle**
    * Attack 95 -> 105
* **Beartic**
    * Attack 110 -> 130
* **Cryogonal**
    * HP 70 -> 80
    * Defense 30 -> 50

### Generation 8

* **Aegislash**
    * Defense 150 -> 140
    * Special Defense 150 -> 140
* **Aegislash-B**
    * Attack 150 -> 140
    * Special Attack 150 -> 140

### Generation 9

* **Cresselia**
    * Defense 120 -> 110
    * Special Defense 130 -> 120
* **Zacian**
    * Attack 130-> 120
* **Zamazenta**
    * Attack 130 -> 120

## Similar Strength

The method of choosing "Similar Strength" Pokemon has changed slightly from the ZX branch. When using "Similar Strength" settings for things like evolutions, Trainer Pokemon and Wild Pokemon, replacements are picked in the following way:

1. Start with the initial viable pool of Pokemon. What this pool includes is dependent on the situation; for example, if randomizing evolutions with the "Same Typing" setting, it would be every Pokemon with the same EXP Curve as the base Pokemon (always applies when randomizing evolutions), sharing at least one type with the base Pokemon (applies when using the "Same Typing" setting). As such, the pool can be quite limited to begin with, depending on settings.
2. Determine the minimum number of the "similar-strength" pool. This minimum is the lower of 5, or one-quarter the number of Pokemon in the viable pool (rounded down).
3. From the initial pool, add every Pokemon that is within a 10% BST range from the original Pokemon to the "similar-strength" pool.
4. If the number of candidates is less than the calculated minimum, expand the range by 5 percentage points and add every Pokemon within the new range to the "similar-strength" pool. Repeat this step until enough Pokemon have been found to meet or exceed the minimum.
5. Pick a replacement at random from the "similar-strength" pool.

The end result of this is that replacements _typically_ will be within a 10% BST range of the original Pokemon, but it may vary if the initial pool is too limited. Compared to ZX's method, this method tends towards slightly larger pools at the cost of allowing more variation from the original BST.

---

Following is an example to illustrate the process, including the differences from ZX's method:

We are picking a replacement wild Pokemon for Seedot, which has a BST of 220, in a Generation 3 game. We are using the "Keep Primary Type" setting, so the replacement must be Grass-type.

There are 41 Grass-type Pokemon in Generation 3, so the minimum pool size would remain at the default of 5. However, let's say we're using global 1-to-1 (which only allows each Pokemon to be chosen once) and most of the Pokemon have already been chosen, so there are only 16 left. This then gives us a minimum pool of 4.

We start by looking for Grass-type Pokemon in the range 198-242. This range includes only Seedot and Lotad.
```
Seedot (220)
Lotad (220)
```

However, let's say Lotad has already been chosen. Thus, Seedot is our only option.

Since we don't have four potential replacements yet, the range is expanded by 5 percentage points and is now 187-253. This adds Hoppip (250) to the list of possible Pokemon. 
```
Seedot (220)
Hoppip (250)
```

We still don't have four, so we expand again, finding Sunkern at the bottom of the range (176-264).
```
Sunkern (180)
Seedot (220)
Hoppip (250)
```

If we were using ZX's method, we would stop here. However, in FVX we have no limit to the number of cycles, and we still have not reached four Pokemon, so we expand twice more, to 165-275 (no new Pokemon) and then to 154-286. This adds Paras.
```
Sunkern (180)
Seedot (220)
Hoppip (250)
Paras (285)
```

The replacement is chosen at random from this pool; it ends up being Paras, a result that would not have been possible in ZX.