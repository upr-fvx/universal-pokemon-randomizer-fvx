# CFRU/DPE Item Sources

Status: source-backed item category constants are now modeled for the known CFRU/DPE Gen9 BPRE `items.h` Mega and
Z-Move blocks, with additional passive categories for Plates, Drives, Memories, Nectars, and selected form-change items.
The category layer is compatible with the existing allowed, bad, non-bad, sensible, consumable, and ban-bad-item pool
filters. No GUI, RNQS, settings-profile, script, gift, or NPC item logic is changed by this note.

Codex did not run, copy, generate, modify, or inspect ROMs for this note.

## Source Model

No complete `items.h` file is checked into this repo. The category layer therefore records the source-backed constants
and blocks provided for CFRU/DPE:

- `ITEM_ULTRANECROZIUM_Z` `0x214`
- `ITEM_VENUSAURITE` `0x215` through `ITEM_DIANCITE` `0x243`
- `ITEM_NORMALIUM_Z` `0x244` through `ITEM_TAPUNIUM_Z` `0x265`

UPR-FVX Gen3 item loading maps unknown expanded internal item IDs into the standard item namespace with
`ItemIDs.UNIQUE_OFFSET + sourceId`. That namespace can collide with generic later-generation `ItemIDs`. For example,
`UNIQUE_OFFSET + ITEM_NORMALIUM_Z` equals a generic later-generation item ID. `CfruDpeItemCategories` therefore treats
the numeric CFRU/DPE source blocks as a source hint and pairs them with decoded item names instead of assuming those
standard IDs are globally unique.

## UPR-FVX Item Model

`Item` stores:

- standard item `id`
- display `name`
- `allowed`
- `bad`
- `tm`
- passive mechanic categories

`Item` does not store descriptions. Text such as "Z-Power", "Z-Move", "Ultra Burst", "Mega Evolution", "Dynamax", or
"Gigantamax" is therefore not available to non-ROM predicates through the current item model.

The shared `RomHandler` item API exposes loaded items, allowed items, non-bad items, evolution/X/Mega helper sets,
regular/OP shop helper sets, and field/shop/pickup/trade item get-set APIs.

## Category Layer

`CfruDpeItemCategories` is the central category layer. It feeds `ItemMechanicPredicates.categoriesFor(...)` and keeps
long source/category lists out of the predicate class.

Modeled categories are:

- Mega Evolution items: known Mega Stones, CFRU/DPE `ITEM_VENUSAURITE..ITEM_DIANCITE`, and Mega accessories.
- Z-Move items: generic Z-Crystals/accessories, `ITEM_ULTRANECROZIUM_Z`, and CFRU/DPE
  `ITEM_NORMALIUM_Z..ITEM_TAPUNIUM_Z`.
- Dynamax/GMax items: Dynamax Candy, Dynamax Band, Wishing Piece, Wishing Star/Chip, Max Honey, Max Mushrooms,
  Gigantamix, and Dynite Ore where modeled.
- Arceus Plates.
- Genesect Drives.
- Silvally Memories.
- Nectars and selected form-change items such as Reveal Glass, Prison Bottle, DNA Splicers, N-Solarizer/N-Lunarizer,
  Rotom Catalog, Reins of Unity, and the Gen4 deity orbs.

Key items are not modeled as a CFRU/DPE category in this PR because no clean source-backed key-item ID range was present
in the repo or in the provided block list. Existing `allowed`/`bad`/ban-list logic remains responsible for keeping key
items out of normal replacement pools.

## Filter Policy

The active settings policy is unchanged:

- `includeMegaItems=false` excludes Mega Stones and Mega accessories from mechanic-filtered replacement pools.
- `includeZCrystalItems=false` excludes Z-Crystals and Z accessories from mechanic-filtered replacement pools.
- `includeDynamaxGmaxItems=false` excludes Dynamax/GMax-related items from mechanic-filtered replacement pools.

Plates, Drives, Memories, Nectars, and form-change items are categorized but not filtered by new settings in this PR.
They continue to follow the existing allowed, bad, non-bad, sensible, consumable, and ban-bad-item filters. This avoids
silently changing policy for items that may already be treated as bad or sensible by generation-specific constants.

## Mechanic-Filtered Item Pools

The shared helper in `Randomizer` filters item candidates through `ItemMechanicPredicates` after the path has already
constructed its candidate pool. That means the new category layer does not bypass existing pool construction or ban-bad
behavior.

Mechanic filtering applies to replacement pools that draw new items:

| Source | Replacement path | Mechanic-filtered |
| --- | --- | --- |
| Field items | `ItemRandomizer.randomizeNonTMFieldItems`, RANDOM/RANDOM_EVEN | yes |
| Shops | `ItemRandomizer.randomizeShopItems`, RANDOM | yes |
| Pickup | `ItemRandomizer.randomizePickupItems`, RANDOM | yes |
| Trainer held items | `TrainerPokemonRandomizer.randomizeHeldItem` draws | yes |
| Starter held items | `StarterRandomizer.randomizeStarterHeldItems` draws | yes |
| Wild held items | `EncounterHeldItemRandomizer.randomizeWildHeldItems` draws | yes |
| Totem/static held item draws | consumable held item draw paths | yes |
| In-game trade held items | `TradeRandomizer.randomizeIngameTrades` random held item path | yes |

Trainer held sensible item pools handle missing CFRU/DPE metadata defensively. Gen3 type-specific sensible held
subpools are treated as empty when the move type has no vanilla Gen3 boosting item mapping, which covers expanded
types such as Fairy. Missing species metadata and invalid move slots are skipped instead of crashing. If sensible
candidate filtering leaves no eligible item after mechanic exclusions, Trainer Held Item randomization falls back to the
same mechanic-filtered held-item pool used by the non-sensible path, so disabled Mega/Z/Dynamax-GMax items are not
reintroduced by the fallback.

Missing expanded movepools/learnsets are also tolerated. `getMovesAtLevel` returns an empty moveset when a species has
no loaded movepool entry, so Trainer Held Item randomization simply has no move-based sensible candidates for that
Pokemon and reaches the same mechanic-filtered held-item fallback.

## Not Mechanic-Filtered Or Not Fully Modeled

The following caveats remain:

| Source | Current coverage |
| --- | --- |
| Field item SHUFFLE | shuffles existing field items; it does not rebuild from a filtered replacement pool |
| Shop SHUFFLE | shuffles existing shop items; it does not rebuild from a filtered replacement pool |
| Trainer held item preservation | existing Z-Crystals or Mega Stones can be preserved by trainer-specific early returns |
| PC Potion misc tweak | draws from non-bad non-TM items and does not currently use the mechanic filter |
| Generic NPC gifts | no generic Gen3 `giveitem` script gift parser/filter was found |
| Generic script items | no broad script item patcher was found |
| Static one-off item scripts | not covered unless they are modeled as field item balls or hidden item signposts |

Gen3 field item scanning covers item-ball event scripts that match the known simple script shape and hidden signpost
items with signpost types 5 through 7. It does not decode and filter every possible NPC or script-level `giveitem`.

## Recommended Next Fix Strategy

1. Add an opt-in local item-source audit report that the user runs against their local ROM. The report should list item
   id, internal item id if available, item name, mechanic category, source pool, mode, and whether that source is
   mechanic-filtered.
2. Import or generate a complete CFRU/DPE item constants metadata file from source-controlled CFRU/DPE/DPE headers.
   This would replace the partial block mapping and allow key-item/form-change categories to be fully source-backed.
3. Decide shuffle semantics separately. If disabled mechanic items should disappear from field/shop shuffle modes, that
   is a behavior change and should be implemented in a focused PR.
4. Decide whether PC Potion should mirror mechanic item exclusions. If yes, wire the misc tweak through the shared
   mechanic filter in a focused PR.
5. Keep generic script/gift/NPC items diagnostic-only until an explicit source index exists. Do not patch arbitrary
   scripts blindly.
