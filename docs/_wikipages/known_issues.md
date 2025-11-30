---
name: Known Issues
---
Here is a list of known issues with the randomizer, listed by game. If an issue appears on this page, that doesn't mean it will be ignored forever. It simply means it is an issue with the currently-released version of the randomizer. 

- [All Games](#all-games)
- [Red/Blue/Green](#redbluegreen)
- [Gold/Silver](#goldsilver)
- [Ruby/Sapphire](#rubysapphire)
- [FireRed/LeafGreen](#fireredleafgreen)
- [All Gen 4 Games](#all-gen-4-games)
- [HeartGold/SoulSilver](#heartgoldsoulsilver)
- [All Gen 5 Games](#all-gen-5-games)
- [Black 2/White 2](#black-2white-2)
- [All Gen 6 Games](#all-gen-6-games)
- [X/Y](#xy)
- [All Gen 7 Games](#all-gen-7-games)
- [Sun/Moon](#sunmoon) 

## All Games

* When using the "Random" setting under Pokemon Base Statistics, evolving a Pokemon into something with a lower base HP than it currently has can result in HP underflow, which can cause a variety of issues depending on the game. To prevent this underflow entirely, enable the "Follow Evolutions" setting for Pokemon Base Statistics. To mitigate these underflows without enabling "Follow Evolutions", make sure that a Pokemon is at high health before evolving them. If an underflow *does* occur, healing at a Pokemon Center will fix it in every game.

## Red/Blue/Green

* Evolving a Pokemon into Mew, will give it glitchy stats. If you are using "Random Evolutions Every Level", evolving the Pokemon again and healing at a Pokemon center will fix all issues. Otherwise, the Mew will continue having glitchy stats, but this should be mostly harmless.

## Gold/Silver

* With unchanged wild Pokemon, the "Use Local Pokemon" trainer option will fail with most type theming because there are no local dark types. (Note this does _not_ apply to Crystal version.)

## Ruby/Sapphire

* When using the "Balance Shop Item Prices" setting, prices in the shop item menu will appear slightly glitchy for items that cost more than 9,999 pokedollars; the rightmost digit will be overlapping with the menu's edge. This is purely a visual glitch, and does not affect how much the item actually costs.

## FireRed/LeafGreen

* When using the "Balance Shop Item Prices" setting, prices in the shop item menu will appear incorrect for items that cost more than 9,999 pokedollars; for example, an item that costs 10,000 pokedollars may appear instead as "?000". This is purely a visual glitch, and does not affect how much the item actually costs. Pressing A to select the item will show the correct price.

## All Gen 4 Games

* When using the "Balance Shop Item Prices" setting, prices in the shop item menu will appear incorrect for items that cost more than 9,999 pokedollars; for example, an item that costs 10,000 pokedollars may appear instead as "?000". This is purely a visual glitch, and does not affect how much the item actually costs. Pressing A to select the item will show the correct price.
* Once a roaming Pokemon is spawned, any wild Pokemon of the same species will be treated, in some aspects, as that same roaming Pokemon. This has a variety of consequences, including:
	* If you catch a wild Pokemon of the same species, the roaming Pokemon will also be considered as "caught" and will stop roaming.
	* If you knock out a wild Pokemon of the same species, the roaming Pokemon will also be considered as "knocked out" and will stop roaming. In Platinum and HG/SS, you can get the Pokemon to roam again by beating the Elite Four.
	* If you encounter a wild Pokemon of the same species and then run away, the current HP of the roaming Pokemon will be set to be the same as the current HP of the wild Pokemon.
	* If you inflict a non-volatile status ailment (e.g., Paralysis) on a wild Pokemon of the same species and then run away, the roaming Pokemon will have that same status ailment inflicted upon it.

## HeartGold/SoulSilver

* With unchanged wild Pokemon, the "Use Local Pokemon" trainer option will fail with most type theming because there are no local dark types. 

## All Gen 5 Games

* When using "Allow Alternate Formes" for Trainer Pokemon, alternate formes of Pokemon will have their base forme's ability. Other traits (base stats, typing, moves) are correct.
* Once a roaming Pokemon is spawned, any wild Pokemon of the same species will be treated, in some aspects, as that same roaming Pokemon. This has a variety of consequences, including:
	* If you catch a wild Pokemon of the same species, the roaming Pokemon will also be considered as "caught" and will stop roaming.
	* If you knock out a wild Pokemon of the same species, the roaming Pokemon will also be considered as "knocked out" and will stop roaming. You can get the Pokemon to roam again by beating the Elite Four.
	* If you encounter a wild Pokemon of the same species and then run away, the current HP of the roaming Pokemon will be set to be the same as the current HP of the wild Pokemon.
	* If you inflict a non-volatile status ailment (e.g., Paralysis) on a wild Pokemon of the same species and then run away, the roaming Pokemon will have that same status ailment inflicted upon it.
* When using the "Fastest Text" setting, some issues will occur.
	* Some text boxes are not displayed properly (for example, the first few text boxes when a Pokemon is learning a new move, and the text boxes that appear when you're riding the Ferris Wheel in Nimbasa).
	* In B/W only, at the beginning of Trainer battles, the text will be very fast instead of being instant. Under some circumstances, the text will stop being instant and instead just be very fast in other situations as well (for example, this can happen when the player's Pokemon is KO'd). This will fix itself after entering a new Trainer battle, making the text instant again.

## Black 2/White 2

* When randomizing wild Pokemon, the Pokedex's Habitat mode will not be updated to reflect the new wild Pokemon spawns.

## All Gen 6 Games

* When using the "Double Battle Mode" setting, trainers who are fought as single battles in the original game will say incorrect text when spoken to after a battle. Additionally, these trainers will also say incorrect text if a battle is initiated by speaking with them; they will only produce the correct text if they "spot" the player.
* When evolutions are randomized, it is possible, under very rare circumstances, for a Pokemon other than Nincada to create an "extra" Pokemon in your party if you have an empty party spot and a Poke Ball in your inventory. Specifically, this can happen if Nincada's "Ninjask evolution" is a Pokemon that was also used for another evolution. For example, if Sandile evolves into Dugtrio, and Nincada evolves into Dugtrio and Anorith, then evolving Sandile under the right conditions will add an Anorith to your party.

## X/Y

* The roaming Pokemon that appears in the wild after defeating the Elite Four will always appear in its base forme; if "Allow Alternate Formes" is selected while randomizing Static Pokemon, this can result in the Pokemon appearing as the "wrong" forme when roaming. When the roaming Pokemon comes to rest in the Sea Spirit's Den, it will appear in its "correct" forme.

## All Gen 7 Games

* When the "Rival Carries Starter Through Game" and "Random Shiny Trainer Pokemon" settings are used simultaneously, the rival's starter can occasionally become shiny when it wasn't shiny before or vice versa.
* When evolutions are randomized, it is possible, under very rare circumstances, for a Pokemon other than Nincada to create an "extra" Pokemon in your party if you have an empty party spot and a Poke Ball in your inventory. Specifically, this can happen if Nincada's "Ninjask evolution" is a Pokemon that was also used for another evolution, or a Pokemon whose alternate forme was used for another evolution. For example, if Sandile evolves into Alolan Dugtrio, and Nincada evolves into Dugtrio and Anorith, then evolving Sandile under the right conditions will add an Anorith to your party.
* Using the evolution options "Random Every Level" and "No Convergence" together always causes the randomization to fail.

## Sun/Moon

* Lusamine's Pokemon have auras which boost their stats during the battle against her in Ultra Space. However, these auras only work on her first five Pokemon. If the Additional Pokemon setting gives her a sixth Pokemon, its aura will be purely visual and will not boost any of its stats.
