package com.uprfvx.pkromio.constants.enctaggers;

import com.uprfvx.pkromio.constants.Gen5Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Gen5EncounterAreaTagger extends EncounterAreaTagger {

    //TODO: unify with location tag system
    //(Applies to all gens & lists)
    private static final int[] bwPostGameEncounterAreasTOD = new int[] {
            369, 370, 371, 372, 373, 374, 375, 376, 377, 378, //Challenger's Cave
            409, 410, 411, 412, 413, 414, 415, //Abundant Shrine
            193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, //Giant Chasm
            385, 386, 387, 388, 389, 390, 391, //Route 11
            392, 393, 394, //Route 12
            395, 396, 397, 398, 399, 400, 401, //Route 13
            402, 403, 404, 405, 406, 407, 408, //Route 14
            416, 417, 418, //Route 15
            436, 437, 438, 439, //Undella Town
            234, 235, 236, 237, 238, 239, 240, //Village Bridge
            217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, //Undella Bay
            241, //Marvelous Bridge
            2, 3, 6, 7, 11, 12, 16, 17, 21, 22, 25, 26, 39, 40, 129, 130, 136, 137, 143, 144,
            150, 151, 159, 160, 173, 174, 215, 216, 247, 248, 256, 257, 262, 263, 268, 269, 273,
            274, 283, 284, 290, 291, 297, 298, 304, 305, 331, 332, 336, 337, 341, 342, 345, 346,
            350, 351, 355, 356, 360, 361, 364, 365, 427, 428, 434, 435, 442, 443, //Fishing. Yes, all fishing.
            28, 30, //Dreamyard black grass
            49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, //Relic Castle bottom floors

            //Thankfully, White Forest is a special zone for encounters,
            //so does not create an encounter number mismatch between Black and White
    };

    private static final int[] bwPostGameEncounterAreasNoTOD = new int[] {
            233, 234, 235, 236, 237, 238, 239, 240, 241, 242, //Challenger's Cave
            273, 274, 275, 276, 277, 278, 279, //Abundant Shrine
            127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, //Giant Chasm
            249, 250, 251, 252, 253, 254, 255, //Route 11
            256, 257, 258, //Route 12
            259, 260, 261, 262, 263, 264, 265, //Route 13
            266, 267, 268, 269, 270, 271, 272, //Route 14
            280, 281, 282, //Route 15
            300, 301, 302, 303, //Undella Town
            156, 157, 158, 159, 160, 161, 162, //Village Bridge
            151, 152, 153, 154, //Undella Bay
            163, //Marvelous Bridge
            2, 3, 6, 7, 11, 12, 25, 26, 84, 85, 93, 94, 107, 108, 149, 150, 169, 170, 178, 179, 184,
            185, 190, 191, 195, 196, 205, 206, 223, 224, 228, 229, 291, 292, 298, 299, 306, 307, //Fishing
            14, 16, //Dreamyard black grass
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, //Relic Castle bottom floors
    };

    private static final int[] b2w2PostGameEncounterAreasTOD = new int[] {
            477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493,
            494, 495, //Moor of Icirrus
            317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, //Clay Tunnel
            331, 332, 333, 334, 335, 336, //Underground Ruins
            42, 43, 44, 45, 46, 47, 48, 49, 50, 51, //Pinwheel Forest
            187, 188, 189, 190, 191, 192, //P2 Laboratory
            383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, //Wellspring Cave
            367, 368, 369, 370, 371, 372, 373, //Route 1
            374, 375, //Route 2
            376, 377, 378, 379, 380, 381, 382, //Route 3
            458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474,
            475, 476, //Route 8
            530, 531, 532, //Route 15
            602, 603, 604, 605, //Route 17
            543, 544, 545, 546, 547, 548, 549, //Route 18
            7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, //Icirrus City
            0, 1, 2, 3, //Striaton City
            366, //Marvelous Bridge
            100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116,
            117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133,
            134, 135, 136, 137, 138, //Dragonspiral Tower
            68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88,
            89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, //Twist Mountain
            38, 39, 40, 41, //Dreamyard
            351, 352, 353, 354, 355, 356, 357, //Nature Preserve
            337, 338, //Rock Peak Chamber
            339, 340, //Iceberg Chamber
            341, 342, //Iron Chamber
            28, 29, 32, 33, 36, 37, 148, 149, 160, 161, 165, 166, 170, 171, 175, 176, 180, 181,
            185, 186, 195, 196, 199, 200, 203, 204, 207, 208, 211, 212, 217, 218, 223, 224, 268,
            269, 278, 279, 286, 287, 296, 297, 303, 304, 313, 314, 347, 348, 364, 365, 398, 399,
            403, 404, 413, 414, 420, 421, 427, 428, 434, 435, 504, 505, 514, 515, 521, 522, 528,
            529, 541, 542, 554, 555, 561, 562, 568, 569, 575, 576, 582, 583, 589, 590, 596, 597,
            600, 601, 608, 609, //Fishing
    };

    private static final int[] b2w2PostGameEncounterAreasNoTOD = new int[] {
            355, 356, 357, 358, 359, //Moor of Icirrus
            239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, //Clay Tunnel
            253, 254, 255, 256, 257, 258, //Underground Ruins
            28, 29, 30, 31, 32, 33, 34, 35, 36, 37, //Pinwheel Forest
            121, 122, 123, 124, 125, 126, //P2 Laboratory
            305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, //Wellspring Cave
            289, 290, 291, 292, 293, 294, 295, //Route 1
            296, 297, //Route 2
            298, 299, 300, 301, 302, 303, 304, //Route 3
            350, 351, 352, 353, 354, //Route 8
            394, 395, 396, //Route 15
            445, 446, 447, 448, //Route 17
            407, 408, 409, 410, 411, 412, 413, //Route 18
            7, 8, 9, 10, 11, //Icirrus City
            0, 1, 2, 3, //Striaton City
            288, //Marvelous Bridge
            62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, //Dragonspiral Tower
            54, 55, 56, 57, 58, 59, 60, 61, //Twist Mountain
            24, 25, 26, 27, //Dreamyard
            273, 274, 275, 276, 277, 278, 279, //Nature Preserve
            259, 260, //Rock Peak Chamber
            261, 262, //Iceberg Chamber
            263, 264, //Iron Chamber
            14, 15, 18, 19, 22, 23, 82, 83, 94, 95, 99, 100, 104, 105, 109, 110, 114,
            115, 119, 120, 129, 130, 133, 134, 139, 140, 145, 146, 190, 191, 200, 201,
            208, 209, 218, 219, 225, 226, 235, 236, 269, 270, 286, 287, 320, 321, 325,
            326, 335, 336, 368, 369, 378, 379, 385, 386, 392, 393, 405, 406, 418, 419,
            425, 426, 432, 433, 439, 440, 443, 444, 451, 452, //Fishing
    };

    private static final List<String> locationTagsNoTimeBW = initLocationTagsNoTimeBW();

    private static List<String> initLocationTagsNoTimeBW() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "STRIATON CITY");
        addCopies(tags, 4, "DRIFTVEIL CITY");
        addCopies(tags, 5, "ICIRRUS CITY");
        addCopies(tags, 4, "DREAMYARD");
        addCopies(tags, 10, "PINWHEEL FOREST");
        addCopies(tags, 2, "DESERT RESORT");
        addCopies(tags, 31, "RELIC CASTLE");
        addCopies(tags, 3, "COLD STORAGE");
        addCopies(tags, 6, "CHARGESTONE CAVE");
        addCopies(tags, 8, "TWIST MOUNTAIN");
        addCopies(tags, 11, "DRAGONSPIRAL TOWER");
        addCopies(tags, 39, "VICTORY ROAD");
        addCopies(tags, 18, "GIANT CHASM");
        addCopies(tags, 6, "P2 LABORATORY");
        addCopies(tags, 4, "UNDELLA BAY");
        addCopies(tags, 1, "DRIFTVEIL DRAWBRIDGE");
        addCopies(tags, 7, "VILLAGE BRIDGE");
        addCopies(tags, 1, "MARVELOUS BRIDGE");
        addCopies(tags, 7, "ROUTE 1");
        addCopies(tags, 2, "ROUTE 2");
        addCopies(tags, 7, "ROUTE 3");
        addCopies(tags, 12, "WELLSPRING CAVE");
        addCopies(tags, 5, "ROUTE 4");
        addCopies(tags, 3, "ROUTE 5");
        addCopies(tags, 7, "ROUTE 6");
        addCopies(tags, 6, "MISTRALTON CAVE");
        addCopies(tags, 3, "ROUTE 7");
        addCopies(tags, 4, "CELESTIAL TOWER");
        addCopies(tags, 5, "ROUTE 8");
        addCopies(tags, 5, "MOOR OF ICIRRUS");
        addCopies(tags, 3, "ROUTE 9");
        addCopies(tags, 10, "CHALLENGER'S CAVE");
        addCopies(tags, 6, "ROUTE 10");
        addCopies(tags, 7, "ROUTE 11");
        addCopies(tags, 3, "ROUTE 12");
        addCopies(tags, 7, "ROUTE 13");
        addCopies(tags, 7, "ROUTE 14");
        addCopies(tags, 7, "ABUNDANT SHRINE");
        addCopies(tags, 3, "ROUTE 15");
        addCopies(tags, 3, "ROUTE 16");
        addCopies(tags, 7, "LOSTLORN FOREST");
        addCopies(tags, 7, "ROUTE 18");
        addCopies(tags, 4, "UNDELLA TOWN");
        addCopies(tags, 4, "ROUTE 17");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> locationTagsUseTimeBW = initLocationTagsUseTimeBW();

    private static List<String> initLocationTagsUseTimeBW() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "STRIATON CITY");
        addCopies(tags, 4, "DRIFTVEIL CITY");
        addCopies(tags, 19, "ICIRRUS CITY");
        addCopies(tags, 4, "DREAMYARD");
        addCopies(tags, 10, "PINWHEEL FOREST");
        addCopies(tags, 2, "DESERT RESORT");
        addCopies(tags, 31, "RELIC CASTLE");
        addCopies(tags, 3, "COLD STORAGE");
        addCopies(tags, 6, "CHARGESTONE CAVE");
        addCopies(tags, 32, "TWIST MOUNTAIN");
        addCopies(tags, 39, "DRAGONSPIRAL TOWER");
        addCopies(tags, 39, "VICTORY ROAD");
        addCopies(tags, 18, "GIANT CHASM");
        addCopies(tags, 6, "P2 LABORATORY");
        addCopies(tags, 16, "UNDELLA BAY");
        addCopies(tags, 1, "DRIFTVEIL DRAWBRIDGE");
        addCopies(tags, 7, "VILLAGE BRIDGE");
        addCopies(tags, 1, "MARVELOUS BRIDGE");
        addCopies(tags, 7, "ROUTE 1");
        addCopies(tags, 2, "ROUTE 2");
        addCopies(tags, 7, "ROUTE 3");
        addCopies(tags, 12, "WELLSPRING CAVE");
        addCopies(tags, 5, "ROUTE 4");
        addCopies(tags, 3, "ROUTE 5");
        addCopies(tags, 28, "ROUTE 6");
        addCopies(tags, 6, "MISTRALTON CAVE");
        addCopies(tags, 12, "ROUTE 7");
        addCopies(tags, 4, "CELESTIAL TOWER");
        addCopies(tags, 19, "ROUTE 8");
        addCopies(tags, 19, "MOOR OF ICIRRUS");
        addCopies(tags, 3, "ROUTE 9");
        addCopies(tags, 10, "CHALLENGER'S CAVE");
        addCopies(tags, 6, "ROUTE 10");
        addCopies(tags, 7, "ROUTE 11");
        addCopies(tags, 3, "ROUTE 12");
        addCopies(tags, 7, "ROUTE 13");
        addCopies(tags, 7, "ROUTE 14");
        addCopies(tags, 7, "ABUNDANT SHRINE");
        addCopies(tags, 3, "ROUTE 15");
        addCopies(tags, 3, "ROUTE 16");
        addCopies(tags, 7, "LOSTLORN FOREST");
        addCopies(tags, 7, "ROUTE 18");
        addCopies(tags, 4, "UNDELLA TOWN");
        addCopies(tags, 4, "ROUTE 17");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> locationTagsNoTimeBW2 = initLocationTagsNoTimeBW2();

    private static List<String> initLocationTagsNoTimeBW2() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "STRIATON CITY");
        addCopies(tags, 3, "CASTELIA CITY");
        addCopies(tags, 5, "ICIRRUS CITY");
        addCopies(tags, 4, "ASPERTIA CITY");
        addCopies(tags, 4, "VIRBANK CITY");
        addCopies(tags, 4, "HUMILAU CITY");
        addCopies(tags, 4, "DREAMYARD");
        addCopies(tags, 10, "PINWHEEL FOREST");
        addCopies(tags, 2, "DESERT RESORT");
        addCopies(tags, 8, "RELIC CASTLE");
        addCopies(tags, 6, "CHARGESTONE CAVE");
        addCopies(tags, 8, "TWIST MOUNTAIN");
        addCopies(tags, 11, "DRAGONSPIRAL TOWER");
        addCopies(tags, 2, "VICTORY ROAD");
        addCopies(tags, 21, "GIANT CHASM");
        addCopies(tags, 25, "CASTELIA SEWERS");
        addCopies(tags, 6, "P2 LABORATORY");
        addCopies(tags, 4, "UNDELLA BAY");
        addCopies(tags, 10, "FLOCCESY RANCH");
        addCopies(tags, 9, "VIRBANK COMPLEX");
        addCopies(tags, 27, "REVERSAL MOUNTAIN");
        addCopies(tags, 10, "STRANGE HOUSE");
        addCopies(tags, 42, "VICTORY ROAD");
        addCopies(tags, 10, "RELIC PASSAGE");
        addCopies(tags, 26, "CLAY TUNNEL");
        addCopies(tags, 8, "SEASIDE CAVE");
        addCopies(tags, 7, "NATURE PRESERVE");
        addCopies(tags, 1, "DRIFTVEIL DRAWBRIDGE");
        addCopies(tags, 7, "VILLAGE BRIDGE");
        addCopies(tags, 1, "MARVELOUS BRIDGE");
        addCopies(tags, 7, "ROUTE 1");
        addCopies(tags, 2, "ROUTE 2");
        addCopies(tags, 7, "ROUTE 3");
        addCopies(tags, 12, "WELLSPRING CAVE");
        addCopies(tags, 10, "ROUTE 4");
        addCopies(tags, 3, "ROUTE 5");
        addCopies(tags, 7, "ROUTE 6");
        addCopies(tags, 6, "MISTRALTON CAVE");
        addCopies(tags, 3, "ROUTE 7");
        addCopies(tags, 4, "CELESTIAL TOWER");
        addCopies(tags, 5, "ROUTE 8");
        addCopies(tags, 5, "MOOR OF ICIRRUS");
        addCopies(tags, 3, "ROUTE 9");
        addCopies(tags, 7, "ROUTE 11");
        addCopies(tags, 3, "ROUTE 12");
        addCopies(tags, 7, "ROUTE 13");
        addCopies(tags, 7, "ROUTE 14");
        addCopies(tags, 7, "ABUNDANT SHRINE");
        addCopies(tags, 3, "ROUTE 15");
        addCopies(tags, 3, "ROUTE 16");
        addCopies(tags, 7, "LOSTLORN FOREST");
        addCopies(tags, 7, "ROUTE 18");
        addCopies(tags, 6, "ROUTE 19");
        addCopies(tags, 7, "ROUTE 20");
        addCopies(tags, 7, "ROUTE 22");
        addCopies(tags, 7, "ROUTE 23");
        addCopies(tags, 4, "UNDELLA TOWN");
        addCopies(tags, 4, "ROUTE 17");
        addCopies(tags, 4, "ROUTE 21");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> locationTagsUseTimeBW2 = initLocationTagsUseTimeBW2();

    private static List<String> initLocationTagsUseTimeBW2() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "STRIATON CITY");
        addCopies(tags, 3, "CASTELIA CITY");
        addCopies(tags, 19, "ICIRRUS CITY");
        addCopies(tags, 4, "ASPERTIA CITY");
        addCopies(tags, 4, "VIRBANK CITY");
        addCopies(tags, 4, "HUMILAU CITY");
        addCopies(tags, 4, "DREAMYARD");
        addCopies(tags, 10, "PINWHEEL FOREST");
        addCopies(tags, 2, "DESERT RESORT");
        addCopies(tags, 8, "RELIC CASTLE");
        addCopies(tags, 6, "CHARGESTONE CAVE");
        addCopies(tags, 32, "TWIST MOUNTAIN");
        addCopies(tags, 39, "DRAGONSPIRAL TOWER");
        addCopies(tags, 2, "VICTORY ROAD");
        addCopies(tags, 21, "GIANT CHASM");
        addCopies(tags, 25, "CASTELIA SEWERS");
        addCopies(tags, 6, "P2 LABORATORY");
        addCopies(tags, 16, "UNDELLA BAY");
        addCopies(tags, 10, "FLOCCESY RANCH");
        addCopies(tags, 9, "VIRBANK COMPLEX");
        addCopies(tags, 27, "REVERSAL MOUNTAIN");
        addCopies(tags, 10, "STRANGE HOUSE");
        addCopies(tags, 42, "VICTORY ROAD");
        addCopies(tags, 10, "RELIC PASSAGE");
        addCopies(tags, 26, "CLAY TUNNEL");
        addCopies(tags, 8, "SEASIDE CAVE");
        addCopies(tags, 7, "NATURE PRESERVE");
        addCopies(tags, 1, "DRIFTVEIL DRAWBRIDGE");
        addCopies(tags, 7, "VILLAGE BRIDGE");
        addCopies(tags, 1, "MARVELOUS BRIDGE");
        addCopies(tags, 7, "ROUTE 1");
        addCopies(tags, 2, "ROUTE 2");
        addCopies(tags, 7, "ROUTE 3");
        addCopies(tags, 12, "WELLSPRING CAVE");
        addCopies(tags, 10, "ROUTE 4");
        addCopies(tags, 3, "ROUTE 5");
        addCopies(tags, 28, "ROUTE 6");
        addCopies(tags, 6, "MISTRALTON CAVE");
        addCopies(tags, 12, "ROUTE 7");
        addCopies(tags, 4, "CELESTIAL TOWER");
        addCopies(tags, 19, "ROUTE 8");
        addCopies(tags, 19, "MOOR OF ICIRRUS");
        addCopies(tags, 3, "ROUTE 9");
        addCopies(tags, 7, "ROUTE 11");
        addCopies(tags, 3, "ROUTE 12");
        addCopies(tags, 7, "ROUTE 13");
        addCopies(tags, 7, "ROUTE 14");
        addCopies(tags, 7, "ABUNDANT SHRINE");
        addCopies(tags, 3, "ROUTE 15");
        addCopies(tags, 3, "ROUTE 16");
        addCopies(tags, 7, "LOSTLORN FOREST");
        addCopies(tags, 7, "ROUTE 18");
        addCopies(tags, 6, "ROUTE 19");
        addCopies(tags, 28, "ROUTE 20");
        addCopies(tags, 7, "ROUTE 22");
        addCopies(tags, 7, "ROUTE 23");
        addCopies(tags, 4, "UNDELLA TOWN");
        addCopies(tags, 4, "ROUTE 17");
        addCopies(tags, 4, "ROUTE 21");
        return Collections.unmodifiableList(tags);
    }

    private static final TagPackMap Gen5TagPackMap = new Builder()
            .newPack(Gen5Constants.Type_BW, locationTagsNoTimeBW, locationTagsUseTimeBW)
                .postGameAreas(bwPostGameEncounterAreasNoTOD, bwPostGameEncounterAreasTOD)
            .newPack(Gen5Constants.Type_BW2, locationTagsNoTimeBW2, locationTagsUseTimeBW2)
                .postGameAreas(b2w2PostGameEncounterAreasNoTOD, b2w2PostGameEncounterAreasTOD)
            .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen5TagPackMap;
    }
}
