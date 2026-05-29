namespace UniversalPokemonRandomizer.Tests.Fixtures;

/// <summary>
/// Reads the golden-vector corpus from <c>utils/fixtures/manifest.tsv</c> and
/// exposes the cases as <see cref="FixtureCase"/> objects.
/// </summary>
public static class FixtureLoader
{
    /// <summary>
    /// Repo-root-relative location of the fixture directory, using the OS path
    /// separator.  Resolved against <see cref="RepoRoot"/> at load time.
    /// </summary>
    private const string FixtureSubdir = "utils/fixtures";

    /// <summary>
    /// Walks up from the test assembly location until it finds the repo root
    /// (identified by the presence of <c>utils/fixtures</c>).
    /// </summary>
    public static string RepoRoot { get; } = FindRepoRoot();

    private static string FindRepoRoot()
    {
        // Start from the directory containing this assembly.
        var dir = new DirectoryInfo(
            Path.GetDirectoryName(typeof(FixtureLoader).Assembly.Location)
            ?? Directory.GetCurrentDirectory());

        while (dir != null)
        {
            var candidate = Path.Combine(dir.FullName, "utils", "fixtures", "manifest.tsv");
            if (File.Exists(candidate))
                return dir.FullName;
            dir = dir.Parent;
        }

        throw new InvalidOperationException(
            "Cannot find repo root containing utils/fixtures/manifest.tsv. " +
            "Make sure the test is run from a working tree that includes the fixture corpus.");
    }

    /// <summary>
    /// Loads all fixture cases from <c>manifest.tsv</c>.
    /// </summary>
    public static IReadOnlyList<FixtureCase> LoadAll()
    {
        var manifestPath = Path.Combine(RepoRoot, FixtureSubdir.Replace('/', Path.DirectorySeparatorChar), "manifest.tsv");

        if (!File.Exists(manifestPath))
            throw new FileNotFoundException($"Fixture manifest not found: {manifestPath}");

        var results = new List<FixtureCase>();

        foreach (var line in File.ReadLines(manifestPath))
        {
            // Skip the header row and blank lines.
            if (string.IsNullOrWhiteSpace(line) || line.StartsWith("function\t", StringComparison.Ordinal))
                continue;

            var columns = line.Split('\t');
            if (columns.Length < 5)
                throw new InvalidDataException($"Malformed manifest row (expected 5 columns): {line}");

            var function    = columns[0].Trim();
            var caseName    = columns[1].Trim();
            var description = columns[2].Trim();
            var inRelPath   = columns[3].Trim().Replace('/', Path.DirectorySeparatorChar);
            var outRelPath  = columns[4].Trim().Replace('/', Path.DirectorySeparatorChar);

            // The manifest paths are relative to utils/ (e.g. "fixtures/DSCmp_LZ10/empty.in").
            var inPath  = Path.Combine(RepoRoot, "utils", inRelPath);
            var outPath = Path.Combine(RepoRoot, "utils", outRelPath);

            results.Add(new FixtureCase(function, caseName, description, inPath, outPath));
        }

        return results;
    }

    /// <summary>
    /// Loads only the fixture cases whose <c>Function</c> column matches
    /// <paramref name="functionName"/> (case-sensitive).
    /// </summary>
    public static IReadOnlyList<FixtureCase> LoadForFunction(string functionName)
        => LoadAll()
            .Where(c => c.Function == functionName)
            .ToList();
}
