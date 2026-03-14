package com.dabomstew.pkromio.gamedata;

import java.util.*;

/**
 * Maps encounter areas to habitat categories based on their encounter type,
 * location tag, and display name.
 * <p>
 * The mapping uses a layered approach:
 * <ol>
 *   <li>Encounter type gives a baseline (e.g., FISHING → Sea, SURFING → Sea/Water's-edge)</li>
 *   <li>Location tag keywords refine the mapping (e.g., "CAVE" → Cave, "FOREST" → Forest)</li>
 *   <li>Display name keywords act as a final fallback</li>
 * </ol>
 * <p>
 * An encounter area may map to multiple habitats, allowing the replacement pool
 * to draw from all matching habitat groups.
 */
public class EncounterAreaHabitatMapper {

    // Keywords that indicate specific habitats when found in location tags or display names
    private static final Map<String, Set<Habitat>> LOCATION_KEYWORD_MAP = new LinkedHashMap<>();

    static {
        // Cave indicators
        addKeywords(EnumSet.of(Habitat.CAVE),
                "CAVE", "CAVERN", "TUNNEL", "GROTTO", "HOLLOW", "WELL", "RUINS",
                "CERULEAN CAVE", "DARK CAVE", "ICE PATH", "ROCK TUNNEL",
                "DIGLETT", "WAYWARD CAVE", "IRON ISLAND",
                "CHARGESTONE", "MISTRALTON CAVE", "WELLSPRING CAVE",
                "RELIC PASSAGE", "CLAY TUNNEL", "SEASIDE CAVE",
                "REFLECTION CAVE", "CONNECTING CAVE", "TERMINUS CAVE", "GLITTERING CAVE",
                "GRANITE CAVE", "METEOR FALLS", "SHOAL CAVE",
                "SEALED CHAMBER", "SEAFLOOR CAVERN",
                "VERDANT CAVERN", "SEAWARD CAVE", "DIGLETT'S TUNNEL",
                "RESOLUTION CAVE", "TEN CARAT HILL");

        // Mountain indicators
        addKeywords(EnumSet.of(Habitat.MOUNTAIN),
                "MT.", "MT ", "MOUNT", "MOUNTAIN", "VOLCANO", "SUMMIT", "PEAK", "TOWER",
                "VICTORY ROAD", "STARK MOUNTAIN", "REVERSAL MOUNTAIN",
                "MT. EMBER", "MT. MOON", "MT. MORTAR", "MT. SILVER",
                "MT. CORONET", "MT. PYRE", "MT. CHIMNEY",
                "JAGGED PASS", "FIERY PATH",
                "SPEAR PILLAR", "DRAGONSPIRAL TOWER", "CELESTIAL TOWER",
                "SKY PILLAR", "BELL TOWER", "TIN TOWER", "BURNED TOWER",
                "SPROUT TOWER", "POKEMON TOWER",
                "VAST PONI CANYON", "MOUNT LANAKILA");

        // Forest indicators
        addKeywords(EnumSet.of(Habitat.FOREST),
                "FOREST", "WOODS", "JUNGLE", "GROVE",
                "VIRIDIAN FOREST", "ILEX FOREST", "PETALBURG WOODS",
                "ETERNA FOREST", "PINWHEEL FOREST", "LOSTLORN FOREST",
                "SANTALUNE FOREST", "BERRY FOREST",
                "LUSH JUNGLE", "SHADE JUNGLE",
                "PATTERN BUSH");

        // Sea / ocean water indicators
        addKeywords(EnumSet.of(Habitat.SEA),
                "OCEAN", "WHIRL ISLANDS",
                "SEAFOAM", "UNDERWATER",
                "UNDELLA BAY", "AZURE BAY");

        // Water's-edge indicators
        addKeywords(EnumSet.of(Habitat.WATERS_EDGE),
                "LAKE", "POND", "MARSH", "SWAMP", "BOG", "RIVER", "STREAM", "FALLS",
                "LAKEFRONT", "LAKESIDE", "WATERFALL",
                "LAKE OF RAGE", "LAKE ACUITY", "LAKE VERITY", "LAKE VALOR",
                "MOOR OF ICIRRUS", "ABUNDANT SHRINE",
                "GREAT MARSH", "PASTORIA CITY",
                "BROOKLET HILL");

        // Rough terrain indicators
        addKeywords(EnumSet.of(Habitat.ROUGH_TERRAIN),
                "DESERT", "BADLANDS", "WASTELAND", "CANYON", "QUARRY",
                "DESERT RESORT", "RELIC CASTLE",
                "HAINA DESERT",
                "SCORCHED SLAB");

        // Urban indicators
        addKeywords(EnumSet.of(Habitat.URBAN),
                "CITY", "TOWN", "BUILDING", "MANSION", "FACTORY", "PLANT", "WAREHOUSE",
                "POWER PLANT", "POKEMON MANSION", "LOST HOTEL",
                "VIRBANK COMPLEX", "COLD STORAGE", "DREAMYARD",
                "POKEMON VILLAGE", "TRAINER SCHOOL",
                "P2 LABORATORY", "AETHER",
                "CASTELIA CITY", "MAUVILLE CITY");

        // Grassland indicators (used as fallback for generic routes)
        addKeywords(EnumSet.of(Habitat.GRASSLAND),
                "SAFARI ZONE", "NATIONAL PARK",
                "RANCH", "FARM", "MEADOW", "FIELD", "PLAIN", "PRAIRIE",
                "FLOCCESY RANCH", "PANIOLA RANCH",
                "MELEMELE MEADOW");

        // Rare (mostly for special encounter types)
        addKeywords(EnumSet.of(Habitat.RARE),
                "MIRAGE");
    }

    private static void addKeywords(Set<Habitat> habitats, String... keywords) {
        for (String keyword : keywords) {
            LOCATION_KEYWORD_MAP.put(keyword.toUpperCase(), habitats);
        }
    }

    /**
     * Determines the habitats that an encounter area corresponds to.
     * Returns a set of one or more habitats. Never returns an empty set -
     * defaults to GRASSLAND if no specific habitat can be determined.
     *
     * @param area The encounter area to classify.
     * @return A non-empty set of habitats appropriate for this area.
     */
    public static Set<Habitat> getHabitatsForArea(EncounterArea area) {
        Set<Habitat> result = EnumSet.noneOf(Habitat.class);

        // Step 1: Check encounter type
        addHabitatsFromEncounterType(area.getEncounterType(), result);

        // Step 2: Check location tag
        String locationTag = area.getLocationTag();
        if (locationTag != null) {
            addHabitatsFromKeywords(locationTag.toUpperCase(), result);
        }

        // Step 3: Check display name as fallback
        String displayName = area.getDisplayName();
        if (displayName != null && result.isEmpty()) {
            addHabitatsFromKeywords(displayName.toUpperCase(), result);
        }

        // Step 4: Default to GRASSLAND for generic routes with no specific indicators
        if (result.isEmpty()) {
            // Routes with walking encounters that didn't match anything specific
            // are most likely grassland areas
            if (area.getEncounterType() == EncounterType.WALKING) {
                result.add(Habitat.GRASSLAND);
            } else {
                // For any other unmatched type, use a broad default
                result.add(Habitat.GRASSLAND);
            }
        }

        return result;
    }

    /**
     * Gets the single "best" habitat for an area when only one is needed.
     * Prefers more specific habitats over generic ones.
     */
    public static Habitat getPrimaryHabitat(EncounterArea area) {
        Set<Habitat> habitats = getHabitatsForArea(area);

        // Prefer non-grassland if we have multiple
        if (habitats.size() > 1 && habitats.contains(Habitat.GRASSLAND)) {
            habitats.remove(Habitat.GRASSLAND);
        }
        // Prefer non-rare if we have multiple
        if (habitats.size() > 1 && habitats.contains(Habitat.RARE)) {
            habitats.remove(Habitat.RARE);
        }

        return habitats.iterator().next();
    }

    private static void addHabitatsFromEncounterType(EncounterType type, Set<Habitat> result) {
        if (type == null) return;
        switch (type) {
            case SURFING:
                // Surfing could be sea or water's-edge; we'll let location keywords disambiguate.
                // If no keywords match, default below will handle it.
                result.add(Habitat.SEA);
                result.add(Habitat.WATERS_EDGE);
                break;
            case FISHING:
                result.add(Habitat.SEA);
                result.add(Habitat.WATERS_EDGE);
                break;
            // WALKING, INTERACT, AMBUSH, SPECIAL - rely on location keywords
            default:
                break;
        }
    }

    private static void addHabitatsFromKeywords(String text, Set<Habitat> result) {
        for (Map.Entry<String, Set<Habitat>> entry : LOCATION_KEYWORD_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
    }
}