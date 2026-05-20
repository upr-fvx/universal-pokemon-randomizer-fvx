# Special Form and Item Exclusions for CFRU/DPE

Status: settings/serialization, species-pool filtering, and mechanic item-pool filtering are connected for the intended
Mega, Gigantamax, regional-form, evolutionary-relative, and mirrored item-exclusion semantics for CFRU/DPE Gen9 BPRE.
GUI controls are exposed through the Limit Pokemon dialog. Source-backed coverage now includes known CFRU/DPE Mega
identity ranges, the known GMax identity block, known CFRU/DPE Z-Crystal identities/names, and known Pikachu
irregular-form identities. ROM-facing metadata audits remain follow-up work.

Codex did not run, copy, generate, modify, or inspect ROMs for this note.

## Target Semantics

Mega forms need their own `Include/Allow Mega Forms` setting. The default should be off. When off, Mega forms must not
appear in starter, wild, trainer, static, trade, intro, catching tutorial, or other species replacement pools. This is a
species-pool exclusion, separate from existing "follow Mega Evolution" and "swap Mega Evolution" behavior settings.

Gigantamax forms need their own `Include/Allow Gigantamax Forms` setting. The default should be off. When off, GMax
forms must not appear in species replacement pools.

Regional forms count by their own form generation by default. A Gen1-only limit without a regional override should
exclude Alolan Vulpix. A separate `Allow Regional Forms across Gen Limit` setting should permit regional forms when the
base family is allowed by the active generation limit, so Gen1-only plus that override can allow Alolan Vulpix because
the Vulpix family is Gen1.

Evolutionary relatives remain a separate override. When enabled, cross-generation evolutions and pre-evolutions can be
allowed even when their own generation is outside the direct generation limit. Examples for Gen1-only plus evolutionary
relatives include later family members such as Sylveon, Annihilape, and Magmortar when the evolution graph links them to
an allowed family. This override does not allow regional forms: Galarian Weezing and Alolan Vulpix still require their
own form generation to be enabled or `Allow Regional Forms across Gen Limit` to be enabled. Regional-branch evolutions
such as Mr. Rime should follow the same branch rule: Gen1-only plus evolutionary relatives but without the regional
override should exclude both Galarian Mr. Mime and Mr. Rime; enabling the regional override can allow that branch.

Species exclusions must be mirrored by item exclusions. If Mega forms are off, Mega Stones and other Mega-relevant items
should be excluded from randomized item pools. If GMax is off, Dynamax and GMax-relevant items should be excluded where
those items are modeled. If Z-Moves or Z-Crystals are unsupported or off, Z-Crystals should be excluded from item pools.

## Current Species Metadata

`Species` currently carries:

- `number`
- `speciesSetIdentityNumber`
- `generation`
- `baseForme`
- `formeNumber`
- `formeSuffix`
- `alolanForme`
- cosmetic-form fields
- `evolutionsFrom` and `evolutionsTo`
- `megaEvolutionsFrom` and `megaEvolutionsTo`

That is enough to express generation limits and evolutionary-relative expansion when the loaded species metadata is
complete. It is not enough by itself to classify all special-form categories for CFRU/DPE expanded BPRE.

The existing Gen3 handler currently reports:

- `getSpeciesInclFormes()` as the same list as normal species
- `getAltFormes()` as empty
- `getIrregularFormes()` as empty
- `getMegaEvolutions()` as empty
- `hasFunctionalFormes()` as false

As a result, CFRU/DPE expanded forms, Mega forms, and GMax forms are not currently visible to the generic alt-forme or
Mega metadata paths. The existing per-path "allow alt formes" settings cannot remove expanded CFRU/DPE forms unless the
handler first classifies those forms.

The current generation predicate in `RestrictedSpeciesService` uses:

```text
species.getBaseForme().getGeneration() == allowedGeneration
```

That behavior is intentionally documented as current behavior, but it does not match the target regional-form semantics.
The target default must use the form's own generation, then apply the regional-form override as a separate exception.

## Example Modelability

CFRU/DPE Gen9 Mega forms are modelable through the DPE/CFRU species identity ranges declared in local source headers:
`SPECIES_VENUSAUR_MEGA` `0x365` through `SPECIES_LATIOS_MEGA` `0x38C`, then `SPECIES_RAYQUAZA_MEGA` `0x38F` through
`SPECIES_DIANCIE_MEGA` `0x396`. `Species.isMegaForm()` now treats those `speciesSetIdentityNumber` ranges as Mega
forms even when the display name and national species number look like the base Pokemon, such as Mega Charizard X/Y
displaying as "Charizard" / `6`. This is intentionally identity-based rather than display-name-based. The adjacent
Primal Groudon/Kyogre identities `0x38D..0x38E` are not classified as Mega by this narrow PR and need a separate
mechanic-design decision if they should be excluded by the Mega-form setting.

CFRU/DPE Gen9 Gigantamax forms are modelable through the DPE/CFRU species identity block declared in local source
headers: `SPECIES_VENUSAUR_GIGA` `0x4EC` through `SPECIES_URSHIFU_RAPID_GIGA` `0x50D`. `Species.isGigantamaxForm()`
now treats that `speciesSetIdentityNumber` range as GMax even when the display name and national species number look
like the base Pokemon, such as GMax Pikachu displaying as "Pikachu" / `25`. This is intentionally identity-based rather
than display-name-based; GMax entries outside that known CFRU/DPE block remain audit-required.

CFRU/DPE Gen9 also declares known Pikachu nonstandard visual identities in one contiguous source-backed block:
`SPECIES_PIKACHU_SURFING` `0x43D` through `SPECIES_PIKACHU_CAP_PARTNER` `0x44B`. These are not Mega, GMax, or regional
forms, but they are not normal default-pool species either. `Species.isIrregularSpecialForm()` now treats that block as
irregular special forms, so a Pokemon that displays as "Pikachu" / `25` but has a Surfing, Flying, Cosplay, Libre,
Rock Star, Pop Star, Belle, Ph.D, or Cap identity is excluded by safe/default special-form options. Normal Pikachu
identity `0x19` remains allowed. GMax Pikachu identity `0x4F0` remains classified by the GMax predicate, not by the
irregular Pikachu block.

CFRU/DPE regional forms are now also source-backed through identity ranges rather than relying only on `baseForme` or
display names:

- Alolan regional forms: `SPECIES_RATTATA_A` `0x3FC` through `SPECIES_MAROWAK_A` `0x40F`.
- Galarian regional forms: `SPECIES_MEOWTH_G` `0x4BC` through `SPECIES_STUNFISK_G` `0x4D1`.
- Hisuian regional forms: `SPECIES_GROWLITHE_H` `0x4D2` through `SPECIES_DECIDUEYE_H` `0x4E2`.
- Paldean regional forms: `SPECIES_TAUROS_P` `0x581` through `SPECIES_WOOPER_P` `0x584`.

This covers cases where the loaded Pokemon displays a base name and national number, such as "Arcanine" / `59`, but
its `speciesSetIdentityNumber` points at `SPECIES_ARCANINE_H` (`0x4D3`). Without the regional override these identities
use their own regional form generation for Gen-limit checks. With `Allow Regional Forms across Gen Limit`, they use a
source-backed base-family generation fallback, so Hisuian Arcanine can be allowed by a Gen1 family limit while Paldean
Wooper still requires a Gen2 family limit.

Regional-branch evolutions are now separately marked when source-backed. This covers Galarian branch evolutions
`SPECIES_OBSTAGOON` `0x482` through `SPECIES_RUNERIGUS` `0x487`, Hisuian branch/dependent evolutions
`SPECIES_WYRDEER` `0x4E3` through `SPECIES_OVERQWIL` `0x4E9`, and Paldean `SPECIES_CLODSIRE` `0x560`. Mr. Rime is
therefore excluded from Gen1-only plus `allowEvolutionaryRelatives=true` when the regional override is off, but can be
allowed when the override is on because its branch maps back to the Gen1 Mr. Mime family.

## Current Settings Surface

Existing related settings are:

- `Settings.limitPokemon`
- `Settings.currentRestrictions`
- `GenRestrictions.allowEvolutionaryRelatives`
- per-path alternate-forme toggles for starters, trainers, wild Pokemon, static Pokemon, evolutions, and totems
- `banIrregularAltFormes`
- Mega follow/swap settings for base stats, types, abilities, trainers, and statics

The prepared settings are:

- `Settings.allowMegaForms`, default off
- `Settings.allowGigantamaxForms`, default off
- `Settings.allowRegionalFormsAcrossGenLimit`, default off
- `Settings.includeMegaItems`, default off
- `Settings.includeZCrystalItems`, default off
- `Settings.includeDynamaxGmaxItems`, default off

The RNQS representation stores these flags in previously unused bits in the existing settings byte 64. Old settings
therefore keep reading with safe/off defaults and do not need a data-length migration.

Settings-profile/RNQS currently supports:

- `MODE-GEN-LIMIT-1-9`
- `MODE-GEN-LIMIT-1-9-NO-RELATIVES`
- `MODE-GEN-LIMIT-1-9-NO-MEGAS`
- `MODE-GEN-LIMIT-1-9-NO-GMAX`
- `MODE-INCLUDE-MEGAS`
- `MODE-INCLUDE-GMAX`
- `MODE-ALLOW-REGIONAL-FORMS`
- `MODE-INCLUDE-MEGA-ITEMS`
- `MODE-INCLUDE-Z-CRYSTALS`
- `MODE-INCLUDE-DYNAMAX-GMAX-ITEMS`

The species flags are connected to `RestrictedSpeciesService`. The item flags are connected to the shared randomizer item
pool helper and therefore apply to replacement pools that draw through the randomizer classes.

## Current Item Metadata

`Item` currently carries:

- `id`
- `name`
- `allowed`
- `bad`
- `tm`
- mechanic categories

The mechanic category layer and `ItemMechanicPredicates` identify Mega Stones/accessories, Z-Crystals/accessories, and
Dynamax/GMax-related items. The predicate is intentionally separate from existing `allowed` and `bad` filters.

The Z-Crystal predicate now includes the CFRU/DPE Gen9 item identities for `ITEM_ULTRANECROZIUM_Z` / Necrozium Z
(`0x214`) and the contiguous CFRU/DPE Z-Crystal block `0x244..0x265`. The source block includes the type crystals and
signature crystals through `ITEM_SNORLIUM_Z` (`0x263`) and `ITEM_TAPUNIUM_Z` (`0x265`). This covers local evidence items
whose descriptions mention Necrozma Ultra Burst or Z-Power / Z-Moves when they enter mechanic-filtered replacement
pools; they were not covered by the previous Gen7 bag/held split item ID ranges.

`Item` stores IDs and display names, but not descriptions. Because the in-ROM text such as "Z-Power", "Z-Move",
"Ultra Burst", "Mega Evolution", "Dynamax", or "Gigantamax" is not present in the item model, description-pattern
classification cannot be used safely by the non-ROM predicate today. The predicate therefore combines existing
constant/range checks with normalized known item names for CFRU/DPE aliases and synthetic audit coverage. The source
also declares the CFRU/DPE Mega Stone block `ITEM_VENUSAURITE` `0x215` through `ITEM_DIANCITE` `0x243`, which is now
classified as Mega-related even though those IDs differ from the generic Gen6 `ItemIDs` constants. This catches modeled
names such as `Snorlium Z`, `Necrozium Z`, `Pikanium Z`, `Pikashunium Z`, `Eevium Z`, `Mewnium Z`, the other known
signature Z-Crystals, source-backed CFRU/DPE Mega Stones, Mega accessories, and modeled Dynamax/GMax items without
broadly matching move names such as Mega Drain.

Mega Stone detection also uses normalized known Mega Stone names when the item ID is not canonical or not in the
currently documented CFRU/DPE range. This covers local evidence examples such as `Pidgeotite` and `Cameruptite`, plus
other known Mega Stones like `Charizardite X`, `Charizardite Y`, `Mewtwonite X`, `Mewtwonite Y`, `Diancite`, and
`Beedrillite`. The predicate remains explicit-name based rather than a broad `*ite` suffix match, so unrelated held
items such as `Eviolite` are not treated as Mega Stones.

Known partial item metadata exists outside `Item`:

- Gen6 constants expose Mega Stone ID sets.
- Gen7 constants expose Z-Crystal mappings and banned/bad item ranges that include Z-Crystals and Mega Stones.
- `ItemIDs` contains Mega Stone, Z-Crystal, Dynamax, and GMax-related constants such as Dynamax Band, Dynamax Candy,
  Wishing Piece, Max Honey, Max Mushrooms, and Dynite Ore.

Those constants are used by the shared predicate, but they are still not a CFRU/DPE-specific compatibility audit.
Future metadata work should verify that expanded Gen3 item IDs match the intended mechanic categories.

## Species Pool Touch Points

The shared species eligibility predicate is applied centrally in `RestrictedSpeciesService`, then preserved by
path-specific writer and safety checks.

Species-picking paths that use `RestrictedSpeciesService` now receive the common predicate:

- Starters: `StarterRandomizer` uses restricted all/non-legendary pools.
- Wild Pokemon: `WildEncounterRandomizer` uses `getSpecies(...)` and `randomSpecies(...)` in replacement paths.
- Trainer Pokemon: `TrainerPokemonRandomizer` uses `getSpecies(...)` and Mega-swap helper pools.
- Static Pokemon: `StaticPokemonRandomizer` uses restricted all, legendary, non-legendary, ultra-beast, and Mega helper
  pools.
- In-game trades: `TradeRandomizer` uses `randomSpecies(...)` for given and requested Pokemon.
- Intro Mon: `IntroPokemonRandomizer` uses `getAll(true)` and then relies on handler-level writability checks.
- Catching Tutorial and other miscellaneous species picks should continue to draw through the restricted service.

The common predicate should reject null species, invalid identity species, disabled Mega forms, disabled GMax forms, and
disallowed regional forms before the path chooses replacements. Safe/default special-form options also reject known
irregular special forms such as CFRU/DPE Pikachu costume/cap identities unless the caller explicitly uses the internal
`allowAllSpecialForms()` option. There is no GUI/RNQS irregular-form include setting in this PR. Path-specific checks
should still reject candidates that cannot be safely written by that ROM handler.

After `allowEvolutionaryRelatives` expands a generation-limited family, the same special-form exclusions are re-applied.
This preserves true cross-generation evolution support while keeping regional forms separate from evolutionary-relative
overrides.

This re-application now uses explicit source-backed regional-branch metadata for known CFRU/DPE branch evolutions, so
Mr. Rime-style evolutions cannot bypass the regional override through flattened family expansion. Unknown branch forms
outside those identity ranges still need a source-backed audit before automatic classification.

## Item Pool Touch Points

The shared randomizer helper filters item candidates through `ItemMechanicPredicates` before placement. It is separate
from `bad` item filtering because "disabled mechanic" is a different reason from "bad random item".

Item-picking paths connected in this slice are:

- Field items: non-TM field item randomization builds from `getAllowedItems()` or `getNonBadItems()`.
- Shops: randomized shop pools build from `getAllowedItems()` or `getNonBadItems()`, then apply regular/OP shop bans.
- Pickup: pickup randomization builds from `getAllowedItems()` or `getNonBadItems()`.
- Trainer held items: random held items use sensible held items, consumable held items, or all held items; Z-Crystal and
  Mega Stone preservation currently has separate trainer logic.
- Starter held items: starter held item randomization builds from `getAllowedItems()` or `getNonBadItems()`.
- Wild encounter held items: encounter held item randomization builds from `getAllowedItems()` or `getNonBadItems()`.
- Static or totem held items: static Pokemon logic can draw from consumable held item pools.
- Trade held items: trade randomization builds possible held items from `getAllowedItems()`.

With default settings, Mega-related items, Z-related items, and Dynamax/GMax-related items are excluded from these
replacement pools. Enabling the matching include setting lets that mechanic's items participate in the same existing
allowed/non-bad/sensible/consumable pool logic.

Necrozium Z / Ultranecrozium Z is covered when it enters one of the shared mechanic-filtered replacement pools above.
Snorlium Z and the other source-backed CFRU/DPE signature Z-Crystals are covered by the same predicate when they enter
one of those pools. Pidgeotite, Cameruptite, and other known Mega Stone names are also covered when they enter those
mechanic-filtered replacement pools, even if their modeled IDs do not match the generic `ItemIDs` or known CFRU/DPE
source block.

If a local item comes from a static script, gift, or NPC path that UPR-FVX does not randomize through those pools, this
PR does not blindly patch scripts; that source remains a ROM-backed local audit item.

## Blockers

- CFRU/DPE Gen3 does not yet expose expanded alt-forme or Mega metadata through the handler.
- Mega classification currently covers the known CFRU/DPE `SPECIES_*_MEGA` identity ranges `0x365..0x38C` and
  `0x38F..0x396`; custom Mega encodings outside those ranges still need source-backed audit.
- CFRU/DPE regional-form and regional-branch detection is source-backed for the documented Alola, Galar, Hisui, Paldea,
  Galarian-branch, Hisuian-branch, and Clodsire identities. Other future/custom regional encodings still need audit.
- GMax classification currently covers the known CFRU/DPE `SPECIES_*_GIGA` identity block `0x4EC..0x50D`; other GMax
  encodings would need a separate audit.
- Irregular special-form classification currently covers the known CFRU/DPE Pikachu variant identity block
  `0x43D..0x44B`; other costume, totem, battle-only, or event forms need source-backed identity blocks before they
  should be filtered automatically.
- Regional-form generation handling uses source-backed form/base-family fallbacks when `baseForme` metadata is absent.
- `Item` has no description field, so description-pattern audit output remains a design recommendation rather than a
  production predicate.
- Later-generation item constants, normalized known names, and the known CFRU/DPE Mega/Z identity ranges are still not
  a complete CFRU/DPE item compatibility audit, even though they now back the shared predicate.
- Local ROM-backed audits are still needed, but they must be user-run outside Codex.

## Recommended Implementation Slices

A) Metadata and predicate layer:

- Add source-backed special-form metadata for CFRU/DPE identities.
- Add explicit categories for Mega, Gigantamax, and regional forms.
- Add a species eligibility options object covering include Mega forms, include GMax forms, allow regional forms across
  generation limit, and allow evolutionary relatives.
- Add item categories for Mega Stones, Z-Crystals, Dynamax/GMax items, and other mechanic-specific exclusions.
- Add non-ROM synthetic tests for the predicate layer.

B) Species pool filtering:

- The final species eligibility decision is connected in `RestrictedSpeciesService`.
- The form's own generation is used by default.
- Evolutionary-relative expansion and regional-form override are explicit exceptions.
- Keep handler-specific writability checks for Intro Mon, trades, statics, and tutorial species.

C) Item pool filtering:

- The shared item eligibility helper is connected in the randomizer base class.
- Field, shop, pickup, trainer held, starter held, wild held, totem held, and trade held item replacement pools use the
  predicate.
- "Bad item" and "disabled mechanic item" exclusion reasons remain separate in code paths.

D) GUI, RNQS, and settings-profile:

- GUI controls live in the Limit Pokemon dialog near generation/species restrictions.
- Backward-compatible settings serialization for the new booleans is prepared.
- Settings-profile overlays for the new flags are prepared.
- Keep defaults off for Mega forms, GMax forms, regional-form cross-generation override, and mechanic item inclusion.

E) Local smoke and audit:

- Produce user-run diagnostics with counts by exclusion reason.
- Smoke Gen1-only with and without regional override.
- Smoke Mega/GMax disabled pools across starters, wild, trainers, statics, trades, intro, and tutorial.
- Smoke item pools for absence of Mega Stones, Z-Crystals, and Dynamax/GMax items when their mechanics are disabled.
- Audit expanded CFRU/DPE item IDs against the synthetic mechanic categories.

## Non-ROM Test Plan

Current synthetic tests cover:

- Mega forms excluded by default and included only when allowed, including CFRU/DPE identity examples such as Mega
  Charizard X/Y.
- GMax forms excluded by default and included only when allowed, including CFRU/DPE identity examples such as GMax
  Pikachu, Charizard, Venusaur, Blastoise, Meowth, and Eevee.
- Alolan Vulpix excluded by Gen1-only without regional override.
- Alolan Vulpix allowed by Gen1-only with regional override when its base family is Gen1.
- Source-backed regional identities such as Hisuian Arcanine, Alolan Raticate, Galarian Weezing, Alolan Vulpix,
  Galarian Mr. Mime, and Paldean Wooper are classified without relying on display names.
- Mr. Rime is covered as a source-backed regional-branch evolution: evolutionary relatives alone do not allow it when
  the regional override is off, while the regional override allows it through the Gen1 Mr. Mime family.
- Sylveon, Annihilape, and Magmortar allowed only through the evolutionary-relative override when appropriate.
- Mega Stones, Z-Crystals, and Dynamax/GMax items excluded by the shared item predicate when their mechanics are off.
- Pidgeotite and Cameruptite are recognized as Mega-related by normalized name even when their IDs are not canonical.
- Field, shop, pickup, trainer held, and starter held item replacement pools exclude mechanic items by default.
- Mega, Z-Crystal, and Dynamax/GMax items can be selected when their corresponding include setting is enabled.
