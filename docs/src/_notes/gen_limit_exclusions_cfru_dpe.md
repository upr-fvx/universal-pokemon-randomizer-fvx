# Gen Limit / Exclusions CFRU/DPE Notes

Status: Gen 1-9 limit is supported in the shared settings model and restricted-species predicate. CFRU/DPE expanded
BPRE generation assignment now falls back to `speciesSetIdentityNumber` when the ROM name cannot be mapped to
`SpeciesIDs`. Mega, GMax, regional-form, and mirrored item exclusions remain follow-up work; the intended semantics are
documented in `special_form_item_exclusions_cfru_dpe.md`. No writer behavior change, ROM execution, or P1 promotion.

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

`MODE-GEN-LIMIT-1-9-NO-MEGAS` and `MODE-GEN-LIMIT-1-9-NO-GMAX` are settings-serializable, with safe/off defaults for
the corresponding include flags. They do not yet filter species pools because Mega/GMax metadata classification and pool
predicate wiring remain follow-up work.

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

With restrictions enabled, `RestrictedSpeciesService` builds `allInclAltFormes` through the shared special-form
eligibility predicate. The direct generation check now uses:

```text
SpecialFormPredicates.effectiveGenerationForDirectLimit(species, options) == allowedGeneration
```

This means the predicate is Species-object based, not a direct `species.getNumber()` filter. By default, forms use their
own generation. Regional forms can use the base-family generation only when the regional override is enabled. The Gen 3
CFRU/DPE loader sets `Species.speciesSetIdentityNumber` to the internal species index for expanded BPRE hacks, but
generation assignment derives from normalized species names mapped to `SpeciesIDs`. If that name lookup fails, expanded
BPRE generation assignment now falls back to `speciesSetIdentityNumber` before `species.getNumber()`. This is important
for DPE/CFRU names that are shortened, accented, or spelled differently from FVX constants.

When `allowEvolutionaryRelatives` is enabled, the service expands the selected set with `SpeciesSet.addFullFamilies()`.
That can intentionally pull in species outside the directly allowed generations if they are connected by the evolution
graph. After that expansion, special-form restrictions are applied again: disabled Mega/GMax forms remain excluded, and
regional forms are still allowed only by their own generation or by `allowRegionalFormsAcrossGenLimit`. In other words,
`allowEvolutionaryRelatives` can allow true cross-generation family members such as Electivire, Magmortar, or Sylveon,
but it does not by itself allow Galarian Weezing or Alolan Vulpix in a Gen1-only pool.

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
  cannot be encoded. CFRU/DPE expanded BPRE can accept identity-valid Gen 7/8/9 candidates through the confirmed visual
  pointer-table path when target front-image and normal-palette assets are valid. If the restricted pool has no writable
  visual candidates, Intro Mon is skipped unchanged as a safety fallback instead of crashing.
- Catching Tutorial: `MiscTweakRandomizer` uses `randomSpecies()`, so it draws from `getAll(false)`.
- Evolution targets: `EvolutionRandomizer` uses `getSpecies(false, altFormesCanHaveDifferentEvolutions(), false)`.

These paths share the same generation, evolutionary-relative, Mega, GMax, and regional-form predicate. Their generic
alt-forme toggle behavior still depends on whether the handler classifies CFRU/DPE expanded forms in `getAltFormes()` /
`getIrregularFormes()`.

## Gen1-only Local Failure

A local Gen1-only smoke still produced non-Gen1 trainer and wild replacements, including Stonjorner, Squawkbily,
Centskorch, Polchgeist, RoarinMoon, Enamorus, Flabebe, Baculegion, and BruteBonet. The inspected Trainer and Wild
randomizers both start from `RestrictedSpeciesService` pools, so the failure was not a direct global-unrestricted pool
fallback.

The root cause is generation metadata assignment for expanded Gen3 BPRE species. The loader first tries to map
normalized ROM species names to `SpeciesIDs`. Several CFRU/DPE display/internal names do not match FVX constant names:

- shortened names, such as `RoarinMoon`
- misspellings or alternate internal spellings, such as `Stonjorner`, `Squawkbily`, `Centskorch`, `Baculegion`,
  and `BruteBonet`
- accented names, such as Flabebe with accented e characters
- FVX constant spelling mismatches, such as `SpeciesIDs.enamorous`

Before the fix, those misses fell back to `species.getNumber()`. In an expanded BPRE pool, that number is not the most
reliable identity for CFRU/DPE species and can misclassify later-generation species as Gen1-eligible. The fixed path
falls back to `speciesSetIdentityNumber`, matching the internal identity used by other CFRU/DPE writer paths.

Non-ROM test coverage uses a low simulated `speciesNumber` to prove the old fallback would be unsafe while the
identity fallback yields the expected generations:

| Observed local name | Canonical FVX SpeciesIDs field | Expected identity/species number | Expected generation |
| --- | --- | ---: | ---: |
| `Stonjorner` | `stonjourner` | 874 | 8 |
| `Squawkbily` | `squawkabilly` | 931 | 9 |
| `Centskorch` | `centiskorch` | 851 | 8 |
| `Polchgeist` | `poltchageist` | 1012 | 9 |
| `RoarinMoon` | `roaringMoon` | 1005 | 9 |
| `Enamorus` | `enamorous` | 905 | 8 |
| `Flabebe` | `flabebe` | 669 | 6 |
| `Baculegion` | `basculegion` | 902 | 8 |
| `BruteBonet` | `bruteBonnet` | 986 | 9 |

Codex did not inspect the private ROM, so the actual local runtime `speciesNumber` values are not asserted here. The
fix removes dependence on those values for the name-miss path by using `speciesSetIdentityNumber`.

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
  are not independently classified by the Gen3 handler today. The known CFRU/DPE Gen9 `SPECIES_*_GIGA` identity block
  `0x4EC..0x50D` is handled directly by `Species.isGigantamaxForm()`.
- Unknown or placeholder species names with invalid or noncanonical identities still need explicit audit coverage.
- GMax encodings outside the known CFRU/DPE identity block still need explicit audit coverage.

## Non-ROM Tests Added

`RestrictedSpeciesServiceGenLimitExclusionsTest` documents current predicate behavior with synthetic Species objects:

- unrestricted service and current `GenRestrictions` include synthetic Gen 8 and Gen 9 species.
- Gen 8 and Gen 9 species are excluded when only earlier generations are allowed.
- unknown-generation species are excluded by generation restrictions.
- forme inclusion uses the form generation by default, while `getAll(false)` still filters handler-reported alt formes.
- Mega and GMax forms are excluded by default and included only through their settings.
- regional forms can use base-family generation only when the regional override is enabled.
- evolutionary-relative expansion can include a Gen 9 relative when only Gen 1 is directly allowed.
- mega evolution entries are retained only when their target species remains in the allowed pool.

These tests do not read ROMs. The production behavior change is limited to CFRU/DPE expanded BPRE generation fallback
for species names that cannot be mapped directly.

`SettingsProfileGeneratorTest` also round-trips `MODE-GEN-LIMIT-1-9` and
`MODE-GEN-LIMIT-1-9-NO-RELATIVES` through RNQS serialization without a ROM, plus a Gen1-only/no-relatives settings
round trip.

`Gen3CfruDpeSpeciesGenerationTest` documents the CFRU/DPE name-miss fallback for the locally reported problem species
without loading a ROM.

## Recommended Fix Strategy

1. Add a CFRU/DPE metadata classifier before changing Mega/GMax/Forme behavior. It should report sampled species count,
   allowed/excluded counts by reason, unknown-generation count, mega/form/GMax counts where known, and examples.
2. Add explicit CFRU/DPE forme and Mega metadata if those entries are present as expanded species. GMax now has a
   source-backed CFRU/DPE identity marker for the known `SPECIES_*_GIGA` block; do not extend it by name or suffix alone.
3. Route Mega/GMax/Forme exclusions through the same eligibility predicate once their metadata and settings exist, then
   keep writer-specific identity mapping
   checks at the ROM handler boundary.

## Later Local Smokes

Local user-run smokes should cover:

- Gen 1-only and Gen 9-only pool selections across starters, wild, trainers, statics, trades, intro mon, and catching
  tutorial.
- evolutionary relatives off and on, including families crossing generation boundaries.
- Mega/GMax/Forme exclusions once metadata exists.
- placeholder and unknown-name entries remain absent from generated pools unless explicitly proven valid.
