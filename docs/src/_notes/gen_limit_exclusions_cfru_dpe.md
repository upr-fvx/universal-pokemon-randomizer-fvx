# Gen Limit / Exclusions CFRU/DPE Notes

Status: Gen 1-9 limit is supported in the shared settings model and restricted-species predicate. Mega, GMax, and
forme-specific exclusions remain follow-up work. No writer behavior change, ROM execution, or P1 promotion.

Codex did not run, copy, generate, modify, or inspect ROMs for this note.

## Current Settings Surface

UPR-FVX has one global Pokemon pool limiter:

- `Settings.limitPokemon`
- `Settings.currentRestrictions`
- `GenRestrictions.allowEvolutionaryRelatives`
- generation bits stored by `GenRestrictions.toInt()`

The GUI exposes this through `GenerationLimitDialog`. `GenRestrictions` now models Gen 1 through Gen 9. The dialog is
capped by `RomHandler.highestPokemonGeneration()`, which uses loaded Species metadata instead of only
`generationOfPokemon()`. This keeps normal base-ROM caps intact while allowing CFRU/DPE expanded BPRE pools to expose
Gen 8 and Gen 9 when the Species metadata carries those generations.

The settings format serializes `currentRestrictions.toInt()` when `limitPokemon` is enabled. The current bitfield has
Gen 8 and Gen 9 slots after the existing Gen 1-7 bits, so existing Gen 1-7 settings keep their previous bit meanings.
Settings-profile/RNQS overlays now support:

- `MODE-GEN-LIMIT-1-9`
- `MODE-GEN-LIMIT-1-9-NO-RELATIVES`

`MODE-GEN-LIMIT-1-9-NO-MEGAS` remains unsupported because there is no dedicated Mega-specific pool exclusion field.
`MODE-GEN-LIMIT-1-9-NO-GMAX` remains unsupported because there is no dedicated Gigantamax exclusion field.

Related exclusion controls that exist today:

- Formes: global `banIrregularAltFormes`, plus per-path allow-alt-forme settings for starters, trainers, wild,
  statics, totems, and evolutions.
- Mega Evolution: follow/swap settings for traits, trainers, and statics; mega evolution candidates are filtered by
  the restricted species service when their target species is not in the allowed pool.
- Legendaries and Ultra Beasts: modeled by `Species.isLegendary()` and `Species.isUltraBeast()`, with path-specific
  no-legendary or separate legendary/non-legendary pool handling.
- Mythicals are not a separate setting; they are part of the legendary ID list.
- Gigantamax has no dedicated metadata or settings predicate in the inspected code.

## Predicate Semantics

`GameRandomizer.setupSpeciesRestrictions()` calls `RestrictedSpeciesService.setRestrictions()` once. Species-picking
paths are expected to use the restricted service instead of raw ROM handler species lists.

With restrictions enabled, `RestrictedSpeciesService` builds `allInclAltFormes` by checking:

```text
species.getBaseForme().getGeneration() == allowedGeneration
```

This means the predicate is Species-object based, not a direct `species.getNumber()` filter. The Gen 3 CFRU/DPE loader
sets `Species.speciesSetIdentityNumber` to the internal species index for expanded BPRE hacks, but generation assignment
currently derives from normalized species names mapped to `SpeciesIDs`, falling back to `species.getNumber()`. That is
identity-aware for modeled names, but unknown names and placeholder entries can still inherit an unreliable fallback.

When `allowEvolutionaryRelatives` is enabled, the service expands the selected set with `SpeciesSet.addFullFamilies()`.
That can intentionally pull in species outside the directly allowed generations if they are connected by the evolution
graph.

Alt-forme exclusion is a second-stage filter: `getAll(false)` and `getNonLegendaries(false)` remove species returned by
`romHandler.getAltFormes()`. If a CFRU/DPE Gen3 handler reports no alt formes, per-path "allow formes" settings cannot
remove expanded-forme entries from the service pool.

## Pool Coverage

The inspected randomizer paths use the restricted service for their primary species picks:

- Starters: `StarterRandomizer` uses `getAll()` / `getNonLegendaries()` with starter no-legendary and alt-forme flags.
- Wild Pokemon: `WildEncounterRandomizer` uses `getSpecies(noLegendaries, allowAltFormes, false)` and then applies
  wild-specific bans.
- Trainer Pokemon: `TrainerPokemonRandomizer` uses `getSpecies(noLegendaries, includeFormes, false)` and
  `getMegaEvolutions()` for mega-swap handling.
- Static Pokemon: `StaticPokemonRandomizer` uses restricted all/legendary/non-legendary/ultra-beast pools and
  `getMegaEvolutions()` for mega-swap handling.
- In-game trades: `TradeRandomizer` uses `randomSpecies()`, which draws from `getAll(false)`.
- Intro Mon: `IntroPokemonRandomizer` uses `getAll(true)` and relies on the ROM handler writer to reject species that
  cannot be encoded.
- Catching Tutorial: `MiscTweakRandomizer` uses `randomSpecies()`, so it draws from `getAll(false)`.
- Evolution targets: `EvolutionRandomizer` uses `getSpecies(false, altFormesCanHaveDifferentEvolutions(), false)`.

These paths share the same generation and evolutionary-relative predicate. Their forme behavior depends on whether the
handler classifies CFRU/DPE expanded forms in `getAltFormes()` / `getIrregularFormes()`.

## CFRU/DPE Metadata Findings

Modeled metadata locations:

- Generation: `Species.generation`, assigned during `Gen3RomHandler.loadSpeciesStats()`.
- CFRU/DPE internal identity: `Species.speciesSetIdentityNumber`, set from the internal species slot for expanded BPRE.
- Base forme: `Species.baseForme`.
- Forme number and suffix: `Species.formeNumber`, `Species.formeSuffix`.
- Evolution family: `Species.evolutionsFrom` / `Species.evolutionsTo`, consumed by `SpeciesSet.addFullFamilies()`.
- Mega Evolution: `MegaEvolution` plus `Species.megaEvolutionsFrom` / `Species.megaEvolutionsTo`.
- Legendary and Ultra Beast: hard-coded Species ID lists through `Species.isLegendary()` / `Species.isUltraBeast()`.

Known gaps:

- Gen 3 `getSpeciesInclFormes()` currently returns `speciesList`, `getAltFormes()` returns an empty set, and
  `getMegaEvolutions()` returns an empty list. For CFRU/DPE BPRE this means expanded formes, Megas, and GMax entries
  are not independently classified by the Gen3 handler today.
- Unknown or placeholder species names can fall back to `species.getNumber()` for generation assignment. That is not
  guaranteed to match CFRU/DPE internal species identity.
- No dedicated GMax metadata was found, so GMax cannot be reliably excluded as a distinct category.

## Non-ROM Tests Added

`RestrictedSpeciesServiceGenLimitExclusionsTest` documents current predicate behavior with synthetic Species objects:

- unrestricted service and current `GenRestrictions` include synthetic Gen 8 and Gen 9 species.
- Gen 8 and Gen 9 species are excluded when only earlier generations are allowed.
- unknown-generation species are excluded by generation restrictions.
- forme inclusion uses the base forme generation, while `getAll(false)` still filters handler-reported alt formes.
- evolutionary-relative expansion can include a Gen 9 relative when only Gen 1 is directly allowed.
- mega evolution entries are retained only when their target species remains in the allowed pool.

These tests do not read ROMs and intentionally do not change production behavior.

`SettingsProfileGeneratorTest` also round-trips `MODE-GEN-LIMIT-1-9` and
`MODE-GEN-LIMIT-1-9-NO-RELATIVES` through RNQS serialization without a ROM.

## Recommended Fix Strategy

1. Add a CFRU/DPE metadata classifier before changing Mega/GMax/Forme behavior. It should report sampled species count,
   allowed/excluded counts by reason, unknown-generation count, mega/form/GMax counts where known, and examples.
2. Make Gen3 CFRU/DPE generation assignment prefer a valid internal identity/speciesSet identity mapping over
   `species.getNumber()` fallback when names are unknown.
3. Add explicit CFRU/DPE forme and Mega metadata if those entries are present as expanded species. Do not rely on names
   or suffixes alone for GMax; add a source-backed marker first.
4. Route Mega/GMax/Forme exclusions through the same eligibility predicate once their metadata and settings exist, then
   keep writer-specific identity mapping
   checks at the ROM handler boundary.

## Later Local Smokes

Local user-run smokes should cover:

- Gen 1-only and Gen 9-only pool selections across starters, wild, trainers, statics, trades, intro mon, and catching
  tutorial.
- evolutionary relatives off and on, including families crossing generation boundaries.
- Mega/GMax/Forme exclusions once metadata exists.
- placeholder and unknown-name entries remain absent from generated pools unless explicitly proven valid.
