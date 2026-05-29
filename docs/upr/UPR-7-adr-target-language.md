# ADR: UPR-7 — Target Language and Port Skeleton

| Field            | Value                                                                 |
|------------------|-----------------------------------------------------------------------|
| Status           | Accepted                                                              |
| Date             | 2026-05-29                                                            |
| Author/Model     | claude-sonnet-4-6 (Claude Code agent, UPR worker)                    |
| Repo             | https://github.com/TimvanderWal504/universal-pokemon-randomizer-fvx  |
| Branch           | upr/UPR-7                                                             |
| Base commit SHA  | e0420cec (HEAD on upr/UPR-7; includes skeleton + ADR)                |
| Related issues   | UPR-2 (parent epic), UPR-7 (this story), UPR-8 (first consumer)      |
| Jira epic        | UPR-2 — Phase 1: Walking skeleton — first verified port               |

---

## Context

The Universal Pokemon Randomizer FVX is a Java Swing desktop application.
The goal of the AI Modernisation project (epic UPR-2) is to produce a
maintainable port that can eventually run in-browser.  Before any code can be
translated, a target language must be chosen and an empty project must exist to
receive the translated code.

Decision criteria, in priority order:

1. **Long-term maintenance** — the owner's preferred language for ongoing work.
2. **In-browser / Blazor WASM** — the ability to ship a browser-playable engine
   without a separate runtime.
3. **Ecosystem fit** — how well the language's idioms match Java's OOP style,
   and how many relevant libraries already exist (binary I/O, ROM handling,
   UI frameworks).
4. **Tooling** — quality of IDE support, test frameworks, CI integration, and
   AI-assisted translation tools.

---

## Options Considered

### Option A — C# (.NET 10)

| Criterion         | Score | Notes                                                                                       |
|-------------------|-------|---------------------------------------------------------------------------------------------|
| Long-term maint.  | 5/5   | Owner preference; statically-typed, class-based OOP closest to Java.                       |
| In-browser (WASM) | 5/5   | Blazor WASM ships the entire .NET runtime to the browser — first-class, production-ready.  |
| Ecosystem fit     | 5/5   | Value types, interfaces, generics, streams — near-identical mental model to Java.           |
| Tooling           | 5/5   | `dotnet` CLI, xUnit/NUnit, Rider/VS Code/VS, GitHub Actions dotnet action.                  |

**Pros**
- Direct Java-to-C# structural mapping (classes, interfaces, enums, generics).
- `BinaryReader`/`BinaryWriter` cover the ROM I/O layer naturally.
- Blazor WASM enables the in-browser goal without a separate JS engine.
- .NET 10 is the current LTS candidate; long support horizon.
- Strong AI-assisted translation tooling (LLMs trained heavily on both Java and C#).

**Cons**
- Runtime download (~10 MB) for first Blazor WASM load (mitigatable with ahead-of-time compile).
- No existing "randomizer in C#" library to leverage.

---

### Option B — TypeScript (Node.js / browser)

| Criterion         | Score | Notes                                                                                       |
|-------------------|-------|---------------------------------------------------------------------------------------------|
| Long-term maint.  | 3/5   | Viable, but owner preference is C#; JS ecosystem churn is a maintenance risk.              |
| In-browser (WASM) | 3/5   | Runs natively in browser, but lacks Blazor WASM's structured app model.                    |
| Ecosystem fit     | 2/5   | Structural typing vs. Java's nominal typing; no value types; verbosity around binary I/O.  |
| Tooling           | 4/5   | Vitest/Jest, npm/pnpm, excellent VS Code support.                                           |

**Pros**
- Runs in any browser without a plugin.
- Large npm ecosystem.

**Cons**
- Binary file I/O is awkward (Buffer/DataView vs Java's DataInputStream).
- Dynamic-by-default semantics make Java's static dispatch patterns harder to replicate faithfully.
- Owner has no long-term preference for TypeScript maintenance.
- Structural typing can obscure bugs that Java/C# nominal typing would catch.

---

### Option C — Kotlin/JS or Kotlin Multiplatform

Evaluated briefly; ruled out because the owner has no Kotlin experience and the
toolchain (Gradle + Kotlin/JS) would duplicate the existing Java build complexity
rather than escape it.

---

## Decision

**Chosen target: C# (.NET 10)**

C# wins on every criterion.  It is the owner's long-term maintenance preference,
it provides a clear path to a Blazor WASM in-browser engine, and its class-based
OOP model is the closest structural match to the Java source code — minimising
the semantic gap that AI-assisted translation must bridge.

The dotnet CLI is already installed (version `10.0.103`) on the development
machine, making the skeleton immediately reproducible.

---

## Consequences

**Enabled by this decision:**
- Blazor WASM in-browser play: the entire randomizer engine can eventually be
  compiled to WASM and served from a static web host.
- Near-1:1 Java class → C# class translation strategy for UPR-8 onward.
- xUnit-based test suite that mirrors the existing Java JUnit tests in structure.

**Foreclosed by this decision:**
- A native Node.js / TypeScript CLI tool (TypeScript was the only realistic
  alternative for that use-case).
- Use of Java-specific libraries (Rhizome ROM parsers, etc.) without a Java
  interop layer — all ROM I/O must be re-implemented in C#.

---

## Repo Strategy

**Decision: single-repo subdirectory (`port/` at repo root)**

The port lives in `port/` within the existing Java repository rather than in a
separate repository.

Rationale:
- A single clone is sufficient for an agent (or human) to work on both the
  original Java source (reference) and the port simultaneously.
- Cross-referencing Java source and C# port is the primary workflow during
  translation; proximity removes friction.
- No GitHub organisation membership or separate CI pipeline is needed.
- The port is explicitly a parallel build — it is not wired into the running
  Java app — so there is no risk of build pollution between `build.gradle.kts`
  and `port/UniversalPokemonRandomizer.sln`.
- A monorepo can always be split later; the inverse (merging two repos) is
  harder.

The Java repo `.gitignore` entries are kept as-is; a `.gitignore` inside
`port/` covers .NET-specific artefacts (`bin/`, `obj/`).

---

## Toolchain

| Component        | Choice                           | Version (exact) |
|------------------|----------------------------------|-----------------|
| SDK / Runtime    | .NET SDK                         | 10.0.103        |
| Target framework | `net10.0`                        | —               |
| Build tool       | `dotnet` CLI (`MSBuild` backend) | 18.0.1 (bundled)|
| Solution file    | `port/UniversalPokemonRandomizer.sln` | —          |
| Core project     | Class library (`Microsoft.NET.Sdk`) | `port/src/UniversalPokemonRandomizer.Core/` |
| Test project     | xUnit (`Microsoft.NET.Sdk`)      | `port/tests/UniversalPokemonRandomizer.Tests/` |
| Test framework   | xUnit                            | 2.9.3           |
| Test runner      | xunit.runner.visualstudio        | 3.1.4           |
| Test SDK         | Microsoft.NET.Test.Sdk           | 17.14.1         |
| Coverage         | coverlet.collector               | 6.0.4           |

### Reproducing the skeleton

```bash
# Restore (nuget.org only — bypasses private Azure DevOps feed if present)
dotnet restore port/ --configfile port/NuGet.config

# Build + test
dotnet test port/ --no-restore
```

Expected output (as verified on 2026-05-29):

```
Passed!  - Failed: 0, Passed: 3, Skipped: 0, Total: 3, Duration: ~370 ms
```

### NuGet source override

`port/NuGet.config` clears all sources and re-adds only `https://api.nuget.org/v3/index.json`.
This isolates the port from any machine-level private feeds (e.g. an Azure
DevOps feed that requires credentials the CI agent may not have).

---

## Resume Point

An agent picking up work after this ADR should:

1. Check out branch `upr/UPR-7` (or the branch created for UPR-8).
2. Run `dotnet restore port/ --configfile port/NuGet.config && dotnet test port/ --no-restore` to confirm the skeleton is green (3 tests pass).
3. The next story is **UPR-8**: translate the first real Java class into `port/src/UniversalPokemonRandomizer.Core/` and add a corresponding xUnit test.
4. All new C# source goes under `port/src/UniversalPokemonRandomizer.Core/`; all new tests go under `port/tests/UniversalPokemonRandomizer.Tests/`.
5. Do not modify the Java source tree; use it as read-only reference material.
