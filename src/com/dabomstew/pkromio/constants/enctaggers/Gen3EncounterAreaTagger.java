package com.dabomstew.pkromio.constants.enctaggers;

import com.dabomstew.pkromio.constants.Gen2Constants;
import com.dabomstew.pkromio.constants.Gen3Constants;
import com.sun.org.apache.bcel.internal.generic.LoadClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Gen3EncounterAreaTagger extends EncounterAreaTagger {

    private static final int[] rsPostGameEncounterAreas = new int[] {
            81, 82, 83, //SKY PILLAR
            153 //Mirage Island - technically not post-game, but not exactly part of the game either
    };

    private static final int[] emPostGameEncounterAreas = new int[] {
            174, 177, 178, //SKY PILLAR
            199, 200, 201, 202, 203, 204, 205, 206, 207, //ALTERING CAVE
            196, //DESERT UNDERPASS
            95, //Mirage Island - technically not post-game, but hardly "local" since it almost never exists
    };

    private static final int[] frlgPostGameEncounterAreas = new int[] {
            33, 34, 35, 36, 37, 38, 39, 40, 41, 42, //CERULEAN CAVE
            118, //THREE ISLE PORT
            214, 215, //FOUR ISLAND
            82, 83, 84, 85, 86, 87, 88, 89, //ICEFALL CAVE
            216, 217, //FIVE ISLAND
            119, 120, //RESORT GORGEOUS
            91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, //LOST CAVE
            121, 122, //WATER LABYRINTH
            123, 124, 125, //FIVE ISLE MEADOW
            126, 127, 128, //MEMORIAL PILLAR
            133, 134, 135, //WATER PATH
            136, 137, 138, //RUIN VALLEY
            131, 132, //GREEN PATH
            90, //PATTERN BUSH
            129, 130, //OUTCAST ISLAND
            218, 219, 220, 221, 222, 223, 224, 225, 226, //ALTERING CAVE
            144, 145, //TANOBY RUINS
            0, 1, 2, 3, 4, 5, 6, //the Tanoby Chambers
            142, 143, //SEVAULT CANYON
            141, //CANYON ENTRANCE
            139, 140, //TRAINER TOWER
    };

    public static final List<String> locationTagsRS = Collections.unmodifiableList(Arrays.asList(
            "PETALBURG CITY", "PETALBURG CITY",
            "SLATEPORT CITY", "SLATEPORT CITY",
            "LILYCOVE CITY", "LILYCOVE CITY",
            "MOSSDEEP CITY", "MOSSDEEP CITY",
            "SOOTOPOLIS CITY", "SOOTOPOLIS CITY",
            "EVER GRANDE CITY", "EVER GRANDE CITY",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "RUSTURF TUNNEL",
            "GRANITE CAVE", "GRANITE CAVE", "GRANITE CAVE", "GRANITE CAVE", "GRANITE CAVE",
            "PETALBURG WOODS",
            "JAGGED PASS",
            "FIERY PATH",
            "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN",
            "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD",
            "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE",
            "SHOAL CAVE", "SHOAL CAVE",
            "NEW MAUVILLE", "NEW MAUVILLE",
            "ABANDONED SHIP", "ABANDONED SHIP", "ABANDONED SHIP", "ABANDONED SHIP",
            "SKY PILLAR", "SKY PILLAR", "SKY PILLAR",
            "ROUTE 101",
            "ROUTE 102", "ROUTE 102", "ROUTE 102",
            "ROUTE 103", "ROUTE 103", "ROUTE 103",
            "ROUTE 104", "ROUTE 104", "ROUTE 104",
            "ROUTE 105", "ROUTE 105",
            "ROUTE 106", "ROUTE 106",
            "ROUTE 107", "ROUTE 107",
            "ROUTE 108", "ROUTE 108",
            "ROUTE 109", "ROUTE 109",
            "ROUTE 110", "ROUTE 110", "ROUTE 110",
            "ROUTE 111", "ROUTE 111", "ROUTE 111", "ROUTE 111",
            "ROUTE 112",
            "ROUTE 113",
            "ROUTE 114", "ROUTE 114", "ROUTE 114", "ROUTE 114",
            "ROUTE 115", "ROUTE 115", "ROUTE 115",
            "ROUTE 116",
            "ROUTE 117", "ROUTE 117", "ROUTE 117",
            "ROUTE 118", "ROUTE 118", "ROUTE 118",
            "ROUTE 119", "ROUTE 119", "ROUTE 119",
            "ROUTE 120", "ROUTE 120", "ROUTE 120",
            "ROUTE 121", "ROUTE 121", "ROUTE 121",
            "ROUTE 122", "ROUTE 122",
            "ROUTE 123", "ROUTE 123", "ROUTE 123",
            "ROUTE 124", "ROUTE 124",
            "ROUTE 125", "ROUTE 125",
            "ROUTE 126", "ROUTE 126",
            "ROUTE 127", "ROUTE 127",
            "ROUTE 128", "ROUTE 128",
            "ROUTE 129", "ROUTE 129",
            "ROUTE 130", "ROUTE 130", "ROUTE 130",
            "ROUTE 131", "ROUTE 131",
            "ROUTE 132", "ROUTE 132",
            "ROUTE 133", "ROUTE 133",
            "ROUTE 134", "ROUTE 134",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "SAFARI ZONE", "SAFARI ZONE",
            "DEWFORD TOWN", "DEWFORD TOWN",
            "PACIFIDLOG TOWN", "PACIFIDLOG TOWN",
            "UNDERWATER", "UNDERWATER"
    ));

    public static final List<String> locationTagsEm = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 101",
            "ROUTE 102", "ROUTE 102", "ROUTE 102",
            "ROUTE 103", "ROUTE 103", "ROUTE 103",
            "ROUTE 104", "ROUTE 104", "ROUTE 104",
            "ROUTE 105", "ROUTE 105",
            "ROUTE 110", "ROUTE 110", "ROUTE 110",
            "ROUTE 111", "ROUTE 111", "ROUTE 111", "ROUTE 111",
            "ROUTE 112",
            "ROUTE 113",
            "ROUTE 114", "ROUTE 114", "ROUTE 114", "ROUTE 114",
            "ROUTE 116",
            "ROUTE 117", "ROUTE 117", "ROUTE 117",
            "ROUTE 118", "ROUTE 118", "ROUTE 118",
            "ROUTE 124", "ROUTE 124",
            "PETALBURG WOODS",
            "RUSTURF TUNNEL",
            "GRANITE CAVE", "GRANITE CAVE",
            "MT. PYRE",
            "VICTORY ROAD",
            "SAFARI ZONE",
            "UNDERWATER",
            "ABANDONED SHIP", "ABANDONED SHIP",
            "GRANITE CAVE", "GRANITE CAVE",
            "FIERY PATH",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "JAGGED PASS",
            "ROUTE 106", "ROUTE 106",
            "ROUTE 107", "ROUTE 107",
            "ROUTE 108", "ROUTE 108",
            "ROUTE 109", "ROUTE 109",
            "ROUTE 115", "ROUTE 115", "ROUTE 115",
            "NEW MAUVILLE",
            "ROUTE 119", "ROUTE 119", "ROUTE 119",
            "ROUTE 120", "ROUTE 120", "ROUTE 120",
            "ROUTE 121", "ROUTE 121", "ROUTE 121",
            "ROUTE 122", "ROUTE 122",
            "ROUTE 123", "ROUTE 123", "ROUTE 123",
            "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE",
            "GRANITE CAVE",
            "ROUTE 125", "ROUTE 125",
            "ROUTE 126", "ROUTE 126",
            "ROUTE 127", "ROUTE 127",
            "ROUTE 128", "ROUTE 128",
            "ROUTE 129", "ROUTE 129",
            "ROUTE 130", "ROUTE 130", "ROUTE 130",
            "ROUTE 131", "ROUTE 131",
            "ROUTE 132", "ROUTE 132",
            "ROUTE 133", "ROUTE 133",
            "ROUTE 134", "ROUTE 134",
            "ABANDONED SHIP", "ABANDONED SHIP",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN",
            "NEW MAUVILLE",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "SAFARI ZONE",
            "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE",
            "SHOAL CAVE",
            "LILYCOVE CITY", "LILYCOVE CITY",
            "DEWFORD TOWN", "DEWFORD TOWN",
            "SLATEPORT CITY", "SLATEPORT CITY",
            "MOSSDEEP CITY", "MOSSDEEP CITY",
            "PACIFIDLOG TOWN", "PACIFIDLOG TOWN",
            "EVER GRANDE CITY", "EVER GRANDE CITY",
            "PETALBURG CITY", "PETALBURG CITY",
            "UNDERWATER",
            "SHOAL CAVE",
            "SKY PILLAR",
            "SOOTOPOLIS CITY", "SOOTOPOLIS CITY",
            "SKY PILLAR", "SKY PILLAR",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT",
            "MAGMA HIDEOUT", "MAGMA HIDEOUT",
            "MIRAGE TOWER", "MIRAGE TOWER", "MIRAGE TOWER", "MIRAGE TOWER",
            "DESERT UNDERPASS",
            "ARTISAN CAVE", "ARTISAN CAVE",
            "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE",
            "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE",
            "METEOR FALLS"
    ));

    public static final List<String> locationTagsFRLG = Collections.unmodifiableList(Arrays.asList(
            "TANOBY CHAMBERS", "TANOBY CHAMBERS", "TANOBY CHAMBERS", "TANOBY CHAMBERS", "TANOBY CHAMBERS",
            "TANOBY CHAMBERS", "TANOBY CHAMBERS",
            "VIRIDIAN FOREST",
            "MT. MOON", "MT. MOON", "MT. MOON",
            "S.S. ANNE", "S.S. ANNE",
            "DIGLETT'S CAVE",
            "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD",
            "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE",
            "ROCK TUNNEL", "ROCK TUNNEL", "ROCK TUNNEL",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER",
            "POWER PLANT",
            "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER",
            "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER",
            "MT. EMBER", "MT. EMBER",
            "BERRY FOREST", "BERRY FOREST", "BERRY FOREST",
            "ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE",
            "ICEFALL CAVE",
            "PATTERN BUSH",
            "LOST CAVE", "LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE",
            "LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE",
            "KINDLE ROAD","KINDLE ROAD","KINDLE ROAD","KINDLE ROAD",
            "TREASURE BEACH","TREASURE BEACH","TREASURE BEACH",
            "CAPE BRINK","CAPE BRINK","CAPE BRINK",
            "BOND BRIDGE", "BOND BRIDGE", "BOND BRIDGE",
            "THREE ISLE PORT",
            "RESORT GORGEOUS", "RESORT GORGEOUS",
            "WATER LABYRINTH", "WATER LABYRINTH",
            "FIVE ISLE MEADOW","FIVE ISLE MEADOW","FIVE ISLE MEADOW",
            "MEMORIAL PILLAR","MEMORIAL PILLAR","MEMORIAL PILLAR",
            "OUTCAST ISLAND","OUTCAST ISLAND",
            "GREEN PATH", "GREEN PATH",
            "WATER PATH", "WATER PATH", "WATER PATH",
            "RUIN VALLEY","RUIN VALLEY","RUIN VALLEY",
            "TRAINER TOWER", "TRAINER TOWER",
            "CANYON ENTRANCE",
            "SEVAULT CANYON", "SEVAULT CANYON",
            "TANOBY RUINS", "TANOBY RUINS",
            "ROUTE 1",
            "ROUTE 2",
            "ROUTE 3",
            "ROUTE 4", "ROUTE 4", "ROUTE 4",
            "ROUTE 5",
            "ROUTE 6", "ROUTE 6", "ROUTE 6",
            "ROUTE 7",
            "ROUTE 8",
            "ROUTE 9",
            "ROUTE 10", "ROUTE 10", "ROUTE 10",
            "ROUTE 11", "ROUTE 11", "ROUTE 11",
            "ROUTE 12", "ROUTE 12", "ROUTE 12",
            "ROUTE 13", "ROUTE 13", "ROUTE 13",
            "ROUTE 14",
            "ROUTE 15",
            "ROUTE 16",
            "ROUTE 17",
            "ROUTE 18",
            "ROUTE 19", "ROUTE 19",
            "ROUTE 20", "ROUTE 20",
            "ROUTE 21", "ROUTE 21", "ROUTE 21", "ROUTE 21", "ROUTE 21", "ROUTE 21",
            "ROUTE 22", "ROUTE 22", "ROUTE 22",
            "ROUTE 23", "ROUTE 23", "ROUTE 23",
            "ROUTE 24", "ROUTE 24", "ROUTE 24",
            "ROUTE 25", "ROUTE 25", "ROUTE 25",
            "PALLET TOWN", "PALLET TOWN",
            "VIRIDIAN CITY", "VIRIDIAN CITY",
            "CERULEAN CITY", "CERULEAN CITY",
            "VERMILION CITY", "VERMILION CITY",
            "CELADON CITY", "CELADON CITY",
            "FUCHSIA CITY", "FUCHSIA CITY",
            "CINNABAR ISLAND", "CINNABAR ISLAND",
            "ONE ISLAND", "ONE ISLAND",
            "FOUR ISLAND", "FOUR ISLAND",
            "FIVE ISLAND", "FIVE ISLAND",
            "ALTERING CAVE","ALTERING CAVE","ALTERING CAVE","ALTERING CAVE","ALTERING CAVE","ALTERING CAVE",
            "ALTERING CAVE","ALTERING CAVE","ALTERING CAVE"
    ));

    private static final TagPackMap Gen3TagPackMap = new Builder()
            .newPack(Gen3Constants.RomType_Ruby, locationTagsRS)
                .postGameAreas(rsPostGameEncounterAreas)
            .newPack(Gen3Constants.RomType_Sapp, locationTagsRS)
                .postGameAreas(rsPostGameEncounterAreas)
            .newPack(Gen3Constants.RomType_Em, locationTagsEm)
                .postGameAreas(emPostGameEncounterAreas)
            .newPack(Gen3Constants.RomType_FRLG, locationTagsFRLG)
                .postGameAreas(frlgPostGameEncounterAreas)
            .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen3TagPackMap;
    }
}
