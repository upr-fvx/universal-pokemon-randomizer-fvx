# Universal Pokemon Randomizer FVX — C# Port

This directory contains the C# port skeleton created by story **UPR-7**.
No Java logic has been translated yet; real porting begins in UPR-8.

## Quick start

```bash
# Restore packages (uses nuget.org only)
dotnet restore --configfile NuGet.config

# Build + run tests
dotnet test --no-restore
```

Expected output:

```
Passed!  - Failed: 0, Passed: 3, Skipped: 0, Total: 3
```

## Layout

```
port/
  UniversalPokemonRandomizer.sln          solution file
  NuGet.config                            restricts packages to nuget.org
  .gitignore                              excludes bin/ obj/ etc.
  src/
    UniversalPokemonRandomizer.Core/      main class library (ported code goes here)
  tests/
    UniversalPokemonRandomizer.Tests/     xUnit test project
```

## Decision record

See `docs/upr/UPR-7-adr-target-language.md` for the full Architecture
Decision Record covering language choice, repo strategy, and toolchain.

## Toolchain

- .NET SDK 10.0.103
- Target framework: net10.0
- Test framework: xUnit 2.9.3
