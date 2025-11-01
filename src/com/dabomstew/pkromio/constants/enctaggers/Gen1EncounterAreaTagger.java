package com.dabomstew.pkromio.constants.enctaggers;

import com.dabomstew.pkromio.constants.Gen1Constants;
import com.dabomstew.pkromio.constants.Gen2Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Gen1EncounterAreaTagger extends EncounterAreaTagger {

    private static final int[] postGameEncounterAreasRBG = new int[]{
            53, 54, 55, 67, //CERULEAN CAVE
    };

    private static final int[] postGameEncounterAreasJapaneseBlue = new int[]{
            54, 55, 56, 68, //CERULEAN CAVE
    };

    private static final int[] postGameEncounterAreasYellow = new int[]{
            59, 60, 61, 94, 95, //CERULEAN CAVE
    };

    // the ones tagged "SUPER ROD N" are super rod encounters shared between several locations
    private static final List<String> locationTagsRBG = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 19/20", "ROUTE 21", "ROUTE 21", "ROUTE 22", "ROUTE 23", "ROUTE 24", "ROUTE 25",
            "VIRIDIAN FOREST", "MT.MOON", "MT.MOON", "MT.MOON", "ROCK TUNNEL", "POWER PLANT", "VICTORY ROAD",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "POKEMON MANSION", "SEAFOAM ISLANDS",
            "VICTORY ROAD", "DIGLETT'S CAVE", "VICTORY ROAD", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "ROCK TUNNEL",
            "OLD ROD", "GOOD ROD", "SUPER ROD 1", "SUPER ROD 2", "SUPER ROD 3", "SUPER ROD 4", "SUPER ROD 5",
            "SUPER ROD 6", "SUPER ROD 7", "SUPER ROD 8", "SUPER ROD 9", "SUPER ROD 10"));

    // for whatever reason Japanese blue loads Route 19/20 as separate encounters,
    // the only difference to locationTagsRBG.
    private static final List<String> locationTagsJapaneseBlue = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 19", "ROUTE 20", "ROUTE 21", "ROUTE 21", "ROUTE 22", "ROUTE 23", "ROUTE 24", "ROUTE 25",
            "VIRIDIAN FOREST", "MT.MOON", "MT.MOON", "MT.MOON", "ROCK TUNNEL", "POWER PLANT", "VICTORY ROAD",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "POKEMON MANSION", "SEAFOAM ISLANDS",
            "VICTORY ROAD", "DIGLETT'S CAVE", "VICTORY ROAD", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "ROCK TUNNEL",
            "OLD ROD", "GOOD ROD", "SUPER ROD 1", "SUPER ROD 2", "SUPER ROD 3", "SUPER ROD 4", "SUPER ROD 5",
            "SUPER ROD 6", "SUPER ROD 7", "SUPER ROD 8", "SUPER ROD 9", "SUPER ROD 10"));

    // yellow has more specific super rod encounters
    private static final List<String> locationTagsYellow = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 6",
            "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 12", "ROUTE 12", "ROUTE 13", "ROUTE 13",
            "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 19", "ROUTE 20", "ROUTE 21", "ROUTE 21", "ROUTE 22", "ROUTE 23", "ROUTE 24", "ROUTE 25",
            "VIRIDIAN FOREST", "MT.MOON", "MT.MOON", "MT.MOON", "ROCK TUNNEL", "POWER PLANT", "VICTORY ROAD",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS",
            "POKEMON MANSION", "SEAFOAM ISLANDS",
            "VICTORY ROAD", "DIGLETT'S CAVE", "VICTORY ROAD", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "ROCK TUNNEL",
            "OLD ROD", "GOOD ROD",
            "PALLET TOWN", "VIRIDIAN CITY", "CERULEAN CITY", "VERMILION CITY", "CELADON CITY", "FUCHSIA CITY",
            "CINNABAR ISLAND", "ROUTE 4", "ROUTE 6", "ROUTE 24", "ROUTE 25", "ROUTE 10", "ROUTE 11", "ROUTE 12",
            "ROUTE 13", "ROUTE 17", "ROUTE 18", "ROUTE 19", "ROUTE 20", "ROUTE 21", "ROUTE 22", "ROUTE 23",
            "VERMILION CITY", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "CERULEAN CAVE", "CERULEAN CAVE"));

    // A hacky solution, since I don't feel like exploring what other code changes
    // would be needed to make Blue (J) have its own romtype.
    public static final int JapaneseBlueEncounterType = 2;

    private static final TagPackMap Gen1TagPackMap = new Builder()
            .newPack(Gen1Constants.Type_RB, locationTagsRBG)
                .postGameAreas(postGameEncounterAreasRBG)
            .newPack(Gen1Constants.Type_Yellow, locationTagsYellow)
                .postGameAreas(postGameEncounterAreasYellow)
            .newPack(JapaneseBlueEncounterType, locationTagsJapaneseBlue)
                .postGameAreas(postGameEncounterAreasJapaneseBlue)
            .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen1TagPackMap;
    }
}
