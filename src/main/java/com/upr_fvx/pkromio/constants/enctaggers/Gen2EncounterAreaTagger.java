package com.upr_fvx.pkromio.constants.enctaggers;

import com.upr_fvx.pkromio.constants.Gen2Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Gen2EncounterAreaTagger extends EncounterAreaTagger {

    private static final int[] gsPostGameEncounterAreasTOD = new int[] {
            327, //PALLET TOWN
            328, //VIRIDIAN CITY
            329, //CERULEAN CITY
            330, 334, //VERMILION CITY
            331, //CELADON CITY
            332, //FUCHSIA CITY
            239, 240, 241, //ROUTE 1
            242, 243, 244, //ROUTE 2
            245, 246, 247, //ROUTE 3
            248, 249, 250, 311, //ROUTE 4
            251, 252, 253, //ROUTE 5
            254, 255, 256, 312, //ROUTE 6
            257, 258, 259, //ROUTE 7
            260, 261, 262, //ROUTE 8
            263, 264, 265, 313, //ROUTE 9
            266, 267, 268, 314, //ROUTE 10
            269, 270, 271, //ROUTE 11
            315, //ROUTE 12
            272, 273, 274, 316, //ROUTE 13
            275, 276, 277, //ROUTE 14
            278, 279, 280, //ROUTE 15
            281, 282, 283, //ROUTE 16
            284, 285, 286, //ROUTE 17
            287, 288, 289, //ROUTE 18
            317, //ROUTE 19
            318, //ROUTE 20
            290, 291, 292, 319, //ROUTE 21
            293, 294, 295, 320, //ROUTE 22
            296, 297, 298, 321, //ROUTE 24
            299, 300, 301, 322, //ROUTE 25
            333, //CINNABAR ISLAND
            221, 222, 223, //DIGLETT's CAVE
            227, 228, 229, 230, 231, 232, //ROCK TUNNEL
            224, 225, 226, //MT.MOON
            308, 309, 310, 326, //ROUTE 28
            114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 180, 181, 182, 196, 219, //SILVER CAVE
    };

    private static final int[] gsPartialPostGameTOD = new int[] {
            348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359,
            360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, //Fishing
    };

    private static final int[] partialPostGameCutoffsTOD = new int[] {
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, // main fishing
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 // time-specific
    };

    private static final int[] gsPostGameEncounterAreasNoTOD = new int[] {
            145, //PALLET TOWN
            146, //VIRIDIAN CITY
            147, //CERULEAN CITY
            148, 152, //VERMILION CITY
            149, //CELADON CITY
            150, //FUCHSIA CITY
            105, //ROUTE 1
            106, //ROUTE 2
            107, //ROUTE 3
            108, 129, //ROUTE 4
            109, //ROUTE 5
            110, 130, //ROUTE 6
            111, //ROUTE 7
            112, //ROUTE 8
            113, 131, //ROUTE 9
            114, 132, //ROUTE 10
            115, //ROUTE 11
            133, //ROUTE 12
            116, 134, //ROUTE 13
            117, //ROUTE 14
            118, //ROUTE 15
            119, //ROUTE 16
            120, //ROUTE 17
            121, //ROUTE 18
            135, //ROUTE 19
            136, //ROUTE 20
            122, 137, //ROUTE 21
            123, 138, //ROUTE 22
            124, 139, //ROUTE 24
            125, 140, //ROUTE 25
            151, //CINNABAR ISLAND
            99, //DIGLETT's CAVE
            101, 102, //ROCK TUNNEL
            100, //MT.MOON
            128, 144, //ROUTE 28
            38, 39, 40, 41, 60, 74, 97, //SILVER CAVE
    };

    private static final int[] gsPartialPostGameNoTOD = new int[] {
            158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, //Fishing
    };

    private static final int[] partialPostGameCutoffsNoTOD = new int[] {
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };

    private static final int[] crysPostGameEncounterAreasTOD = new int[] {
            328, //PALLET TOWN
            329, //VIRIDIAN CITY
            330, //CERULEAN CITY
            312, 331, //VERMILION CITY
            332, //CELADON CITY
            333, //FUCHSIA CITY
            239, 240, 241, //ROUTE 1
            242, 243, 244, //ROUTE 2
            245, 246, 247, //ROUTE 3
            248, 249, 250, 313, //ROUTE 4
            251, 252, 253, //ROUTE 5
            254, 255, 256, 314, //ROUTE 6
            257, 258, 259, //ROUTE 7
            260, 261, 262, //ROUTE 8
            263, 264, 265, 315, //ROUTE 9
            266, 267, 268, 316, //ROUTE 10
            269, 270, 271, //ROUTE 11
            317, //ROUTE 12
            272, 273, 274, 318, //ROUTE 13
            275, 276, 277, //ROUTE 14
            278, 279, 280, //ROUTE 15
            281, 282, 283, //ROUTE 16
            284, 285, 286, //ROUTE 17
            287, 288, 289, //ROUTE 18
            319, //ROUTE 19
            320, //ROUTE 20
            290, 291, 292, 321, //ROUTE 21
            293, 294, 295, 322, //ROUTE 22
            296, 297, 298, 323, //ROUTE 24
            299, 300, 301, 324, //ROUTE 25
            334, //CINNABAR ISLAND
            221, 222, 223, //DIGLETT
            227, 228, 229, 230, 231, 232, //ROCK TUNNEL
            224, 225, 226, //MT.MOON
            308, 309, 310, 327, //ROUTE 28
            114, 115, 116, 117, 118, 119, 120, 121, 122, 123,
            124, 125, 180, 181, 182, 196, 220, //SILVER CAVE
    };

    private static final int[] crysPartialPostGameTOD = new int[] {
            341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352,
            353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, //Fishing
    };

    private static final int[] crysPostGameEncounterAreasNoTOD = new int[] {
            146, //PALLET TOWN
            147, //VIRIDIAN CITY
            148, //CERULEAN CITY
            130, 149, //VERMILION CITY
            150, //CELADON CITY
            151, //FUCHSIA CITY
            105, //ROUTE 1
            106, //ROUTE 2
            107, //ROUTE 3
            108, 131, //ROUTE 4
            109, //ROUTE 5
            110, 132, //ROUTE 6
            111, //ROUTE 7
            112, //ROUTE 8
            113, 133, //ROUTE 9
            114, 134, //ROUTE 10
            115, //ROUTE 11
            135, //ROUTE 12
            116, 136, //ROUTE 13
            117, //ROUTE 14
            118, //ROUTE 15
            119, //ROUTE 16
            120, //ROUTE 17
            121, //ROUTE 18
            137, //ROUTE 19
            138, //ROUTE 20
            122, 139, //ROUTE 21
            123, 140, //ROUTE 22
            124, 141, //ROUTE 24
            125, 142, //ROUTE 25
            152, //CINNABAR ISLAND
            99, //DIGLETT
            101, 102, //ROCK TUNNEL
            100, //MT.MOON
            128, 145, //ROUTE 28
            38, 39, 40, 41, 60, 74, 98, //SILVER CAVE
    };

    private static final int[] crysPartialPostGameNoTOD = new int[] {
            155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, //Fishing
    };

    private static final List<String> locationTagsNoTimeGS = Collections.unmodifiableList(Arrays.asList(
            // Johto cave/grass
            "SPROUT TOWER", "SPROUT TOWER",
            "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER",
            "BURNED TOWER", "BURNED TOWER",
            "NATIONAL PARK",
            "RUINS OF ALPH", "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE", "SILVER CAVE", "SILVER CAVE", "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37",
            "ROUTE 38", "ROUTE 39", "ROUTE 42", "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46",
            "SILVER CAVE",
            // Johto surfing
            "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "DRAGON'S DEN",
            "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 34", "ROUTE 35", "ROUTE 40", "ROUTE 41", "ROUTE 42", "ROUTE 43",
            "ROUTE 44", "ROUTE 45",
            "NEW BARK TOWN",
            "CHERRYGROVE CITY",
            "VIOLET CITY",
            "CIANWOOD CITY",
            "OLIVINE CITY",
            "ECRUTEAK CITY",
            "LAKE OF RAGE",
            "BLACKTHORN CITY",
            "SILVER CAVE",
            "OLIVINE CITY",
            // Kanto cave/grass
            "DIGLETT'S CAVE",
            "MT.MOON",
            "ROCK TUNNEL", "ROCK TUNNEL",
            "VICTORY ROAD",
            "TOHJO FALLS",
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28",
            // Kanto surfing
            "ROUTE 4", "ROUTE 6", "ROUTE 9", "ROUTE 10", "ROUTE 12", "ROUTE 13", "ROUTE 19", "ROUTE 20",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27",
            "TOHJO FALLS",
            "ROUTE 28",
            "PALLET TOWN",
            "VIRIDIAN CITY",
            "CERULEAN CITY",
            "VERMILION CITY",
            "CELADON CITY",
            "FUCHSIA CITY",
            "CINNABAR ISLAND",
            "VERMILION CITY",
            // Swarms
            "ROUTE 35",
            "ROUTE 38",
            "DARK CAVE",
            "MT.MORTAR", "MT.MORTAR",
            // Fishing, Headbutt, BCC
            "FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND", "FISHING DRATINI 1",
            "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS", "FISHING DRATINI 2",
            "FISHING WHIRL ISLANDS", "FISHING QWILFISH", "FISHING REMORAID",
            "HEADBUTT FOREST GS", "HEADBUTT FOREST GS", "HEADBUTT CANYON GS", "HEADBUTT CANYON GS", "ROCK SMASH",
            "BUG CATCHING CONTEST"
    ));

    private static final List<String> locationTagsUseTimeGS = initLocationTagsUseTimeGS();

    private static List<String> initLocationTagsUseTimeGS() {
        List<String> locationTags = new ArrayList<>();
        for (int areaNum = 0; areaNum < 61; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeGS.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeGS.subList(61, 99));
        for (int areaNum = 99; areaNum < 129; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeGS.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeGS.subList(129, 153));
        for (int areaNum = 153; areaNum < 157; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeGS.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeGS.subList(157, 170));
        locationTags.addAll(Arrays.asList("FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND",
                "FISHING DRATINI 1", "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS",
                "FISHING DRATINI 2", "FISHING WHIRL ISLANDS", "FISHING QWILFISH"));
        locationTags.addAll(locationTagsNoTimeGS.subList(170, 176));
        return Collections.unmodifiableList(locationTags);
    }

    private static final List<String> locationTagsNoTimeCrystal = Collections.unmodifiableList(Arrays.asList(
            // Johto cave/grass
            "SPROUT TOWER", "SPROUT TOWER",
            "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER",
            "BURNED TOWER", "BURNED TOWER",
            "NATIONAL PARK",
            "RUINS OF ALPH", "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE", "SILVER CAVE", "SILVER CAVE", "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37",
            "ROUTE 38", "ROUTE 39", "ROUTE 42", "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46",
            "SILVER CAVE",
            // Johto surfing
            "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "DRAGON'S DEN",
            "OLIVINE CITY",
            "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 34", "ROUTE 35", "ROUTE 40", "ROUTE 41", "ROUTE 42", "ROUTE 43",
            "ROUTE 44", "ROUTE 45",
            "NEW BARK TOWN",
            "CHERRYGROVE CITY",
            "VIOLET CITY",
            "CIANWOOD CITY",
            "OLIVINE CITY",
            "ECRUTEAK CITY",
            "LAKE OF RAGE",
            "BLACKTHORN CITY",
            "SILVER CAVE",
            // Kanto cave/grass
            "DIGLETT'S CAVE",
            "MT.MOON",
            "ROCK TUNNEL", "ROCK TUNNEL",
            "VICTORY ROAD",
            "TOHJO FALLS",
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28",
            // Kanto surfing
            "TOHJO FALLS",
            "VERMILION CITY",
            "ROUTE 4", "ROUTE 6", "ROUTE 9", "ROUTE 10", "ROUTE 12", "ROUTE 13", "ROUTE 19", "ROUTE 20",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28",
            "PALLET TOWN",
            "VIRIDIAN CITY",
            "CERULEAN CITY",
            "VERMILION CITY",
            "CELADON CITY",
            "FUCHSIA CITY",
            "CINNABAR ISLAND",
            // Swarms
            "DARK CAVE",
            "ROUTE 35",
            // Fishing, Headbutt, BCC
            "FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND", "FISHING DRATINI 1",
            "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS", "FISHING DRATINI 2",
            "FISHING WHIRL ISLANDS", "FISHING QWILFISH", "FISHING REMORAID",
            "HEADBUTT CANYON C", "HEADBUTT CANYON C", "HEADBUTT TOWN", "HEADBUTT TOWN", "HEADBUTT ROUTE",
            "HEADBUTT ROUTE", "HEADBUTT KANTO", "HEADBUTT KANTO", "HEADBUTT LAKE", "HEADBUTT LAKE", "HEADBUTT FOREST C",
            "HEADBUTT FOREST C", "ROCK SMASH",
            "BUG CATCHING CONTEST"
    ));

    private static final List<String> locationTagsUseTimeCrystal = initLocationTagsUseTimeCrystal();

    private static List<String> initLocationTagsUseTimeCrystal() {
        List<String> locationTags = new ArrayList<>();
        for (int areaNum = 0; areaNum < 61; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeCrystal.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeCrystal.subList(61, 99));
        for (int areaNum = 99; areaNum < 129; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeCrystal.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeCrystal.subList(129, 153));
        for (int areaNum = 153; areaNum < 155; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeCrystal.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeCrystal.subList(155, 167));
        locationTags.addAll(Arrays.asList("FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND",
                "FISHING DRATINI 1", "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS",
                "FISHING DRATINI 2", "FISHING WHIRL ISLANDS", "FISHING QWILFISH"));
        locationTags.addAll(locationTagsNoTimeCrystal.subList(167, 181));
        return Collections.unmodifiableList(locationTags);
    }

    private static final TagPackMap Gen2TagPackMap = new Builder()
                    .newPack(Gen2Constants.Type_GS, locationTagsNoTimeGS, locationTagsUseTimeGS)
                        .postGameAreas(gsPostGameEncounterAreasNoTOD, gsPostGameEncounterAreasTOD)
                        .partialPostGameAreas(gsPartialPostGameNoTOD, gsPartialPostGameTOD)
                        .partialPostGameCutoffs(partialPostGameCutoffsNoTOD, partialPostGameCutoffsTOD)
                    .newPack(Gen2Constants.Type_Crystal, locationTagsNoTimeCrystal, locationTagsUseTimeCrystal)
                        .postGameAreas(crysPostGameEncounterAreasNoTOD, crysPostGameEncounterAreasTOD)
                        .partialPostGameAreas(crysPartialPostGameNoTOD, crysPartialPostGameTOD)
                        .partialPostGameCutoffs(partialPostGameCutoffsNoTOD, partialPostGameCutoffsTOD)
                    .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen2TagPackMap;
    }
}
