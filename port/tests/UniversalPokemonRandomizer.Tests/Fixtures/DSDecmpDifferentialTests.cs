using UniversalPokemonRandomizer.Core.Compressors;

namespace UniversalPokemonRandomizer.Tests.Fixtures;

// ---------------------------------------------------------------------------
// xUnit data sources
// ---------------------------------------------------------------------------

/// <summary>
/// Provides the DSDecmp_LZ10 fixture cases as xUnit <c>[MemberData]</c>.
/// </summary>
public static class DSDecmp_LZ10Fixtures
{
    private static readonly IReadOnlyList<FixtureCase> Cases =
        FixtureLoader.LoadForFunction("DSDecmp_LZ10");

    /// <summary>All DSDecmp_LZ10 fixture cases wrapped in object[] for [Theory].</summary>
    public static IEnumerable<object[]> All =>
        Cases.Select(c => new object[] { c });
}

/// <summary>
/// Provides the DSDecmp_LZ11 fixture cases as xUnit <c>[MemberData]</c>.
/// </summary>
public static class DSDecmp_LZ11Fixtures
{
    private static readonly IReadOnlyList<FixtureCase> Cases =
        FixtureLoader.LoadForFunction("DSDecmp_LZ11");

    /// <summary>All DSDecmp_LZ11 fixture cases wrapped in object[] for [Theory].</summary>
    public static IEnumerable<object[]> All =>
        Cases.Select(c => new object[] { c });
}

// ---------------------------------------------------------------------------
// Tests: DSDecmp LZ10
// ---------------------------------------------------------------------------

/// <summary>
/// Differential tests for <see cref="DSDecmp.DecompressLZ10"/>.
/// Each of the 8 golden-vector fixtures is decompressed and the output must
/// be byte-for-byte identical to the corresponding .out file.
/// </summary>
public sealed class DSDecmp_LZ10_DifferentialTests
{
    [Theory]
    [MemberData(nameof(DSDecmp_LZ10Fixtures.All), MemberType = typeof(DSDecmp_LZ10Fixtures))]
    public void DecompressLZ10_MatchesGoldenVector(FixtureCase fixture)
    {
        var input    = File.ReadAllBytes(fixture.InPath);
        var expected = File.ReadAllBytes(fixture.OutPath);

        var actual = DSDecmp.DecompressLZ10(input);

        DifferentialAssert.BytesEqual(fixture, expected, actual);
    }
}

// ---------------------------------------------------------------------------
// Tests: DSDecmp LZ11
// ---------------------------------------------------------------------------

/// <summary>
/// Differential tests for <see cref="DSDecmp.DecompressLZ11"/>.
/// Each of the 9 golden-vector fixtures is decompressed and the output must
/// be byte-for-byte identical to the corresponding .out file.
/// </summary>
public sealed class DSDecmp_LZ11_DifferentialTests
{
    [Theory]
    [MemberData(nameof(DSDecmp_LZ11Fixtures.All), MemberType = typeof(DSDecmp_LZ11Fixtures))]
    public void DecompressLZ11_MatchesGoldenVector(FixtureCase fixture)
    {
        var input    = File.ReadAllBytes(fixture.InPath);
        var expected = File.ReadAllBytes(fixture.OutPath);

        var actual = DSDecmp.DecompressLZ11(input);

        DifferentialAssert.BytesEqual(fixture, expected, actual);
    }
}
