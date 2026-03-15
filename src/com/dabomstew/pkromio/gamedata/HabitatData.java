package com.dabomstew.pkromio.gamedata;

import com.dabomstew.pkromio.FileFunctions;

import java.io.IOException;
import java.util.*;

/**
 * Provides habitat data for all Pokémon species across all generations.
 * <p>
 * Habitat assignments for Gen I-III come from the official FRLG Pokédex data.
 * Assignments for Gen IV+ are curated based on Pokédex lore, typing, design,
 * and consistency with the FRLG classification system.
 * <p>
 * Habitat data is loaded from a config file ({@code habitat_data.ini}) in an INI-like format
 * with section headers like {@code [Grassland]} and species number ranges like {@code 1-3}
 * or individual entries like {@code 25}.
 */
public class HabitatData {

    private static Map<Integer, Habitat> habitatBySpeciesNumber;

    /**
     * Returns the habitat for the given species, or null if unknown.
     * Alt formes inherit the habitat of their base forme.
     */
    public static Habitat getHabitat(Species species) {
        if (species == null) return null;
        ensureLoaded();

        // Alt formes inherit from base forme
        int number = species.getBaseForme() != null
                ? species.getBaseForme().getNumber()
                : species.getNumber();

        return habitatBySpeciesNumber.getOrDefault(number, null);
    }

    /**
     * Returns the habitat for the given national dex number, or null if unknown.
     */
    public static Habitat getHabitat(int speciesNumber) {
        ensureLoaded();
        return habitatBySpeciesNumber.getOrDefault(speciesNumber, null);
    }

    /**
     * Returns true if a habitat assignment exists for the given species.
     */
    public static boolean hasHabitat(Species species) {
        return getHabitat(species) != null;
    }

    /**
     * Returns true if a habitat assignment exists for the given species number.
     */
    public static boolean hasHabitat(int speciesNumber) {
        return getHabitat(speciesNumber) != null;
    }

    private static synchronized void ensureLoaded() {
        if (habitatBySpeciesNumber != null) return;
        habitatBySpeciesNumber = new HashMap<>();
        try {
            Scanner sc = new Scanner(FileFunctions.openConfig("habitat_data.ini"), "UTF-8");
            Habitat currentHabitat = null;

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                // Strip inline comments
                int commentIndex = line.indexOf("//");
                if (commentIndex >= 0) {
                    line = line.substring(0, commentIndex).trim();
                }

                // Skip empty lines
                if (line.isEmpty()) continue;

                // Section header: [HabitatName]
                if (line.startsWith("[") && line.endsWith("]")) {
                    String habitatName = line.substring(1, line.length() - 1).trim();
                    currentHabitat = Habitat.fromDisplayName(habitatName);
                    if (currentHabitat == null) {
                        System.err.println("Warning: Unknown habitat '" + habitatName + "' in habitat_data.ini");
                    }
                    continue;
                }

                if (currentHabitat == null) continue;

                // Parse species entries: comma-separated, each can be a single number or a range (e.g. 10-15)
                String[] entries = line.split(",");
                for (String entry : entries) {
                    entry = entry.trim();
                    if (entry.isEmpty()) continue;

                    if (entry.contains("-")) {
                        // Range: e.g. "1-3"
                        String[] rangeParts = entry.split("-", 2);
                        try {
                            int start = Integer.parseInt(rangeParts[0].trim());
                            int end = Integer.parseInt(rangeParts[1].trim());
                            for (int i = start; i <= end; i++) {
                                habitatBySpeciesNumber.put(i, currentHabitat);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Malformed range '" + entry + "' in habitat_data.ini");
                        }
                    } else {
                        // Single number
                        try {
                            int number = Integer.parseInt(entry);
                            habitatBySpeciesNumber.put(number, currentHabitat);
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Malformed entry '" + entry + "' in habitat_data.ini");
                        }
                    }
                }
            }
            sc.close();
        } catch (IOException e) {
            System.err.println("Warning: Could not load habitat_data.ini. " +
                    "Habitat-based randomization will be unavailable.");
        }
    }

    /**
     * Reloads habitat data from the config file. Useful for testing.
     */
    public static synchronized void reload() {
        habitatBySpeciesNumber = null;
        ensureLoaded();
    }

    /**
     * Returns the number of species with habitat assignments.
     */
    public static int getAssignedCount() {
        ensureLoaded();
        return habitatBySpeciesNumber.size();
    }
}