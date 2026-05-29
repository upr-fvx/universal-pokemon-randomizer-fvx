namespace UniversalPokemonRandomizer.Tests.Fixtures;

/// <summary>
/// Represents a single golden-vector test case loaded from the fixture corpus.
/// </summary>
/// <param name="Function">Directory name / codec identifier (e.g. "DSCmp_LZ10").</param>
/// <param name="Case">Case name without extension (e.g. "repetitive_256").</param>
/// <param name="Description">Human-readable description of what the input stresses.</param>
/// <param name="InPath">Absolute path to the .in file.</param>
/// <param name="OutPath">Absolute path to the .out file.</param>
public sealed record FixtureCase(
    string Function,
    string Case,
    string Description,
    string InPath,
    string OutPath)
{
    /// <inheritdoc />
    public override string ToString() => $"{Function}/{Case}";
}
