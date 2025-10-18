# Changes

## General

* Auto-update has been disabled to prevent potential new releases of the original randomizer from overwriting this version of the randomizer.

* GUI has been updated with a link to this release page instead of the original website.

## Pokemon Traits

### Pokemon Abilities

* Gen 5 abilities are now in the randomization pool for gen 5 games.
* Imposter has been added as a negative ability.

### Pokemon Evolutions

* The randomizer will try to avoid having several Pokemon evolve into the same Pokemon. (In some cases, this will be unavoidable if there are no more Pokemon to pick from.)
* Evolutions are now randomized before base stats and abilities. Enabling "Follow Evolutions" on these options will make the randomized base stat spread/ability follow the randomized evolutions rather than the original evolutions, so you will keep your base stat spread and ability when you evolve. Exceptions: If more than one Pokemon evolve into the same Pokemon, the base stat spread/ability will carry from only one of them; if a Pokemon can evolve into more than one Pokemon, it will carry its base stat spread/ability to none of them.
* The "Same Typing" option now makes Eevee evolve into Pokemon with types matching those of the Eeveelutions, instead of just evolving into several Normal-type Pokemon.

## Starters, Statics & Trades

### Static Pokemon

* Giratina can now be randomized in Platinum, but only from a limited pool of Pokemon (because most Pokemon will softlock the game).

## Moves & Movesets

### Random (preferring same type)

* This option has been rebalanced:

> Monotype: **60% primary, 20% normal, 20% random** changed to **40% primary, 60% random**
> Monotype Normal: **75% normal, 25% random** changed to **same as Monotype**
> Dual-type: **50% primary, 30% secondary, 5% normal, 15% random** changed to **20% primary, 20% secondary, 60% random**
> Dual-type Normal: **30% normal, 55% other, 15% random** changed to **10% normal, 30% other, 60% random**

### NEW: Guaranteed Level 1 Moves

* Changed to a slider where you can choose to guarantee 2-4 moves on level 1 (the option previously always set it to 4).

## Misc Tweaks

### Fastest Text
* New fast text IPS patches for Black/White to prevent the game from softlocking/crashing if you mash at the beginning of trainer battles. At the beginning of trainer battles, text is displayed very quickly instead of instantly. Under certain circumstances it will continue to just display text very quickly instead of instantly; this fixes itself the next time you enter a trainer battle.

### NEW: No Free Lucky Egg
* In B/W/B2/W2, you normally get a Lucky Egg for free from Professor Juniper. With this setting on, she will instead give you a random type of Mulch."
}