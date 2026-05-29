using UniversalPokemonRandomizer.Core;

namespace UniversalPokemonRandomizer.Tests;

/// <summary>
/// Trivial smoke tests that verify the skeleton builds, the test runner
/// is wired up, and the Core assembly is reachable from the test project.
/// These are intentionally minimal — real logic arrives in UPR-8.
/// </summary>
public class SkeletonInfoTests
{
    [Fact]
    public void ProjectName_IsNotEmpty()
    {
        Assert.False(string.IsNullOrWhiteSpace(SkeletonInfo.ProjectName));
    }

    [Fact]
    public void CreatedByStory_IsUPR7()
    {
        Assert.Equal("UPR-7", SkeletonInfo.CreatedByStory);
    }

    [Fact]
    public void TrivialArithmetic_Passes()
    {
        // Sanity: xUnit runner is alive.
        Assert.Equal(4, 2 + 2);
    }
}
