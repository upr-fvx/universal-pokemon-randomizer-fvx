# Wild Encounter Output Audit

Status: diagnostic-only helper for Gen3/FRLG/CFRU-DPE wild encounter verification. No writer behavior is changed by this note or by the audit.

## Purpose

Local visual smoke can show a mismatch where the randomizer log says Wild Pokemon were randomized/changed, but early-game encounters still appear vanilla in-game. The opt-in audit compares a private Base ROM and private randomized Output ROM at the modeled Gen3 base `WildPokemon` table level.

The report is intended to answer a narrow question:

- did modeled base wild encounter slots change in the output ROM?
- which map/area/type/slot changed or stayed unchanged?
- what changed percentage is visible in the modeled table?

It is not a runtime proof for CFRU/DPE special wild systems such as day/night headers, swarms, DexNav or other runtime hooks.

## Opt-In Test

Run locally only with private ROM paths:

```sh
./gradlew :romio:test --tests '*Gen3WildEncounterOutputAuditRomTest*' \
  -Duprfvx.wildEncounterAuditBaseRom=<private-base-rom> \
  -Duprfvx.wildEncounterAuditOutputRom=<private-output-rom>
```

Environment variable equivalents are also supported:

- `UPRFVX_WILD_ENCOUNTER_AUDIT_BASE_ROM`
- `UPRFVX_WILD_ENCOUNTER_AUDIT_OUTPUT_ROM`

If either path is omitted, the test is skipped.

## Report Output

The test writes:

```text
build/reports/diagnostics/wild-encounter-output-audit.txt
```

The report redacts ROM paths and includes:

- ROM code/version/type for base and output.
- total encounter slots.
- changed slots.
- unchanged slots.
- changed percentage.
- per-slot area name, map index, location tag, encounter type, slot index, level range, base species, output species and `changedFromBase=yes/no`.

Early-game labels such as Route 1, Route 2 and Viridian Forest are included when the Gen3 map labels/tagger expose them through the modeled base wild encounter areas.

## Interpretation

Use the audit as sanitized local evidence. Share only aggregate counts and selected redacted rows, for example:

- `totalEncounterSlots=...`
- `changedSlots=...`
- `changedPercentage=...`
- selected early-game rows with area/type/slot and base/output species

Do not share ROM paths, hashes, full logs, screenshots, saves, emulator states, secrets or `.env` content.

If the modeled base `WildPokemon` table changed but in-game encounters stay vanilla, the next likely investigation is a runtime-source mismatch: the hack may be sourcing encounters from a CFRU/DPE special/runtime table outside the modeled base wild table.
