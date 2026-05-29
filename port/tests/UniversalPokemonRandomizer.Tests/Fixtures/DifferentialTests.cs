using UniversalPokemonRandomizer.Tests.Fixtures;

namespace UniversalPokemonRandomizer.Tests.Fixtures;

// ---------------------------------------------------------------------------
// Delegate type used by the differential harness.
// ---------------------------------------------------------------------------

/// <summary>
/// A codec implementation under test.  Receives the raw input bytes and
/// returns the transformed output bytes.
/// </summary>
public delegate byte[] CodecFunc(byte[] input);

// ---------------------------------------------------------------------------
// Stub implementations used to validate the harness itself.
// ---------------------------------------------------------------------------

/// <summary>
/// Correct stub for DSCmp_LZ10: reads the expected output directly from the
/// .out file and returns it, proving the harness passes a correct implementation.
/// </summary>
internal static class CorrectStub
{
    /// <summary>
    /// Returns the golden output for the fixture whose input was <paramref name="input"/>.
    /// Looks up the corresponding .out file via the fixture loader.
    /// </summary>
    /// <remarks>
    /// This is intentionally a cheat — it reads the answer from disk.  Its only
    /// purpose is to prove that, when the output is correct, the harness reports
    /// a pass.  Real codec implementations will compute the output from the input.
    /// </remarks>
    public static byte[] Transform(byte[] input, FixtureCase fixture)
        => File.ReadAllBytes(fixture.OutPath);
}

/// <summary>
/// Wrong stub for DSCmp_LZ10: echoes the input back unchanged.
/// For any non-trivial codec (.in != .out) this must cause the harness to fail.
/// </summary>
internal static class WrongStub
{
    /// <summary>Returns the input bytes as-is (identity transform).</summary>
    public static byte[] Transform(byte[] input) => input;
}

// ---------------------------------------------------------------------------
// xUnit data sources
// ---------------------------------------------------------------------------

/// <summary>
/// Provides the DSCmp_LZ10 fixture cases as xUnit <c>[MemberData]</c>.
/// </summary>
public static class DSCmp_LZ10Fixtures
{
    // Loaded once per test-run; fixture discovery is cheap.
    private static readonly IReadOnlyList<FixtureCase> Cases =
        FixtureLoader.LoadForFunction("DSCmp_LZ10");

    /// <summary>All DSCmp_LZ10 fixture cases wrapped in object[] for [Theory].</summary>
    public static IEnumerable<object[]> All =>
        Cases.Select(c => new object[] { c });
}

// ---------------------------------------------------------------------------
// Tests: Correct Stub (all must PASS)
// ---------------------------------------------------------------------------

/// <summary>
/// Proves the harness accepts a correct implementation.
/// The CorrectStub reads the .out bytes directly, so it always returns
/// byte-exact output.  All 6 DSCmp_LZ10 fixtures must pass.
/// </summary>
public sealed class DSCmp_LZ10_CorrectStubTests
{
    [Theory]
    [MemberData(nameof(DSCmp_LZ10Fixtures.All), MemberType = typeof(DSCmp_LZ10Fixtures))]
    public void CorrectStub_Passes(FixtureCase fixture)
    {
        var input    = File.ReadAllBytes(fixture.InPath);
        var expected = File.ReadAllBytes(fixture.OutPath);

        // CorrectStub reads the answer directly — must always produce byte-exact output.
        var actual = CorrectStub.Transform(input, fixture);

        DifferentialAssert.BytesEqual(fixture, expected, actual);
    }
}

// ---------------------------------------------------------------------------
// Tests: Wrong Stub (all must FAIL)
// ---------------------------------------------------------------------------

/// <summary>
/// Proves the harness rejects a wrong implementation.
/// The WrongStub returns the input unchanged; for DSCmp_LZ10 every fixture
/// has a non-trivial transform (.in != .out), so all 6 must fail.
/// </summary>
public sealed class DSCmp_LZ10_WrongStubTests
{
    [Theory]
    [MemberData(nameof(DSCmp_LZ10Fixtures.All), MemberType = typeof(DSCmp_LZ10Fixtures))]
    public void WrongStub_Fails(FixtureCase fixture)
    {
        var input    = File.ReadAllBytes(fixture.InPath);
        var expected = File.ReadAllBytes(fixture.OutPath);

        // WrongStub returns input unchanged.  This must differ from expected
        // compressed output, so DifferentialAssert.BytesEqual WILL throw.
        var actual = WrongStub.Transform(input);

        // This call is expected to throw — the test is intentionally failing
        // to demonstrate that the harness catches bad output.
        DifferentialAssert.BytesEqual(fixture, expected, actual);
    }
}
