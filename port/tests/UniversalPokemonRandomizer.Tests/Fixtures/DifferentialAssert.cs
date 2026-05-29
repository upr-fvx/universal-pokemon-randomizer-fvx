namespace UniversalPokemonRandomizer.Tests.Fixtures;

/// <summary>
/// Helpers for byte-exact differential comparison with clear failure messages.
/// </summary>
public static class DifferentialAssert
{
    /// <summary>
    /// Asserts that <paramref name="actual"/> is byte-for-byte identical to
    /// <paramref name="expected"/>.  On failure, throws an
    /// <see cref="Xunit.Sdk.XunitException"/> that identifies the fixture and the
    /// first point of divergence.
    /// </summary>
    /// <param name="fixture">The fixture case being verified (for diagnostics).</param>
    /// <param name="expected">Expected bytes (from the .out file).</param>
    /// <param name="actual">Actual bytes produced by the implementation under test.</param>
    public static void BytesEqual(FixtureCase fixture, byte[] expected, byte[] actual)
    {
        if (expected.Length != actual.Length)
        {
            throw new Xunit.Sdk.XunitException(
                $"Fixture {fixture.Function}/{fixture.Case}: " +
                $"length mismatch — expected {expected.Length} bytes, got {actual.Length} bytes.");
        }

        for (int i = 0; i < expected.Length; i++)
        {
            if (expected[i] != actual[i])
            {
                throw new Xunit.Sdk.XunitException(
                    $"Fixture {fixture.Function}/{fixture.Case}: " +
                    $"first difference at offset {i} — " +
                    $"expected 0x{expected[i]:X2}, got 0x{actual[i]:X2}.");
            }
        }
    }
}
