# Special Form and Item Exclusions for CFRU/DPE

Status: settings/serialization, species-pool filtering, and mechanic item-pool filtering are connected for the intended
Mega, Gigantamax, regional-form, evolutionary-relative, and mirrored item-exclusion semantics for CFRU/DPE Gen9 BPRE.
GUI controls and ROM-facing metadata audits remain follow-up work.

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
own form generation to be enabled or `Allow Regional Forms across Gen Limit` to be enabled.

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

Mega Venusaur is modelable only if the CFRU/DPE loader identifies it as either a Mega-form species or a
`MegaEvolution` target linked to Venusaur. Generic Gen6 constants contain Mega suffix and Mega Stone knowledge, but the
Gen3 CFRU/DPE path does not currently expose Mega metadata.

Gigantamax Venusaur is not safely modelable with the current metadata. There is no dedicated GMax flag, and using names
or suffixes alone would be too fragile for CFRU/DPE compatibility. It needs a source-backed marker or table.

Alolan Vulpix is partly represented by the existing `alolanForme` concept, but the model is Alola-specific and not a
general regional-form category. It does not cover Galarian Meowth, Hisuian Growlithe, or Paldean Wooper as a uniform
feature.

Galarian Meowth, Hisuian Growlithe, and Paldean Wooper need a generalized regional-form metadata layer that records the
form species, base family, region/form kind, and own form generation. The eligibility predicate can then decide whether
to use own generation only or the base-family override.

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
disallowed regional forms before the path chooses replacements. Path-specific checks should still reject candidates that
cannot be safely written by that ROM handler.

After `allowEvolutionaryRelatives` expands a generation-limited family, the same special-form exclusions are re-applied.
This preserves true cross-generation evolution support while keeping regional forms separate from evolutionary-relative
overrides.

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

## Blockers

- CFRU/DPE Gen3 does not yet expose expanded alt-forme, Mega, or GMax metadata through the handler.
- `Species` has no generalized regional-form kind; `alolanForme` is not enough for Galarian, Hisuian, and Paldean
  forms.
- GMax has no dedicated metadata marker.
- The current generation predicate uses base-form generation, which conflicts with the desired regional-form default.
- Later-generation item constants are not a CFRU/DPE item compatibility audit, even though they now back the shared
  predicate.
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

- Add GUI controls near generation/species restrictions as a follow-up.
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

- Mega forms excluded by default and included only when allowed.
- GMax forms excluded by default and included only when allowed.
- Alolan Vulpix excluded by Gen1-only without regional override.
- Alolan Vulpix allowed by Gen1-only with regional override when its base family is Gen1.
- Sylveon, Annihilape, and Magmortar allowed only through the evolutionary-relative override when appropriate.
- Mega Stones, Z-Crystals, and Dynamax/GMax items excluded by the shared item predicate when their mechanics are off.
- Field, shop, pickup, and starter held item replacement pools exclude mechanic items by default.
- Mega, Z-Crystal, and Dynamax/GMax items can be selected when their corresponding include setting is enabled.
