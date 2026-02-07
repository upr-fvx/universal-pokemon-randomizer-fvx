package com.uprfvx.pkromio.constants.enctaggers;

import com.uprfvx.pkromio.constants.Gen7Constants;
import com.uprfvx.pkromio.gamedata.EncounterType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Gen7EncounterAreaTagger extends EncounterAreaTagger {

    public static int[] smPostGameEncounterAreasTOD = new int[]{
            664, 665, 666, 667, 668, 669, 670, 671, 672, 673, 674, 675, 676, 677, //Poni Grove
            678, 679, 680, 681, 682, 683, 684, 685, 686, 687, 688, 689, 690, 691,
            692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 704, 705,
            706, 707, 708, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719,
            720, 721, 722, 723, //Poni Plains
            724, 725, //Poni Coast
            740, 741, 742, 743, 744, 745, 746, 747, 748, 749, 750, 751, //Poni Meadow
            726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 736, 737, 738, 739, //Poni Gauntlet
            782, 783, 784, 785, //Resolution Cave
            786, 787, 788, 789, 790, 791, 792, 793, 586, 587, 588, 589, 590, 591,
            592, 593, 594, 595, 572, 573, 550, 551, 552, 553, 466, 467, 380, 381,
            384, 385, 388, 389, 390, 391, 364, 365, 246, 247, 186, 187, 174, 175,
            146, 147, 116, 117, 48, 49, 50, 51 //apparently unused (various areas)
    };

    public static int[] smPostGameEncounterAreasNoTOD = new int[]{
            332, 333, 334, 335, 336, 337, 338, //Poni Grove
            339, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352,
            353, 354, 355, 356, 357, 358, 359, 360, 361, //Poni Plains
            362, //Poni Coast
            370, 371, 372, 373, 374, 375, //Poni Meadow
            363, 364, 365, 366, 367, 368, 369, //Poni Gauntlet
            391, 392, //Resolution Cave
            24, 25, 58, 73, 87, 93, 123, 182, 190, 192, 194, 195, 233, 275, 276,
            286, 293, 294, 295, 296, 297, 393, 394, 395, 396 //apparently unused (various areas)
    };

    public static int[] usumPostGameEncounterAreasTOD = new int[]{
            668, 669, 670, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 681, //Poni Grove
            682, 683, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695,
            696, 697, 698, 699, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709,
            710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723,
            724, 725, 726, 727, //Poni Plains
            728, 729, //Poni Coast
            744, 745, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, //Poni Meadow
            730, 731, 732, 733, 734, 735, 736, 737, 738, 739, 740, 741, 742, 743, //Poni Gauntlet
            786, 787, 788, 789, //Resolution Cave
            50, 51, 52, 53, 62, 63, 64, 65, 124, 125, 156, 157, 184, 185,
            196, 197, 378, 379, 394, 395, 398, 399, 402, 403, 404, 405, 472, 473,
            558, 559, 560, 561, 580, 581, 592, 593, 594, 595, 596, 597, 598, 599,
            602, 603, //apparently unused (various areas)
    };

    public static int[] usumPostGameEncounterAreasNoTOD = new int[]{
            334, 335, 336, 337, 338, 339, 340, //Poni Grove
            341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354,
            355, 356, 357, 358, 359, 360, 361, 362, 363, //Poni Plains
            364, //Poni Coast
            372, 373, 374, 375, 376, 377, //Poni Meadow
            365, 366, 367, 368, 369, 370, 371, //Poni Gauntlet
            393, 394, //Resolution Cave
            25, 26, 31, 32, 62, 78, 92, 98, 189, 197, 199, 201, 202, 236, 279,
            280, 290, 296, 297, 298, 299, 301, //apparently unused (various areas)
    };

    private static final List<String> smLocationTagsTOD = initSMLocationTagsTOD();

    private static List<String> initSMLocationTagsTOD() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 8, "ROUTE 1");
        addCopies(tags, 10, "MELEMELE SEA");
        addCopies(tags, 14, "ROUTE 1");
        addCopies(tags, 16, "MELEMELE SEA");
        addCopies(tags, 4, "UNUSED");
        addCopies(tags, 12, "HAU'OLI CITY");
        addCopies(tags, 2, "ROUTE 3");
        addCopies(tags, 2, "KALA'E BAY");
        addCopies(tags, 4, "ROUTE 3");
        addCopies(tags, 10, "KALA'E BAY");
        addCopies(tags, 8, "ROUTE 3");
        addCopies(tags, 4, "KALA'E BAY");
        addCopies(tags, 18, "ROUTE 2");
        addCopies(tags, 4, "TEN CARAT HILL");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "TEN CARAT HILL"); //Farthest Hollow
        addCopies(tags, 4, "HAU'OLI CEMETERY");
        addCopies(tags, 2, "MELEMELE MEADOW");
        addCopies(tags, 8, "SEAWARD CAVE");
        addCopies(tags, 2, "ROUTE 2"); //Berry Fields
        // counting as Route 2 because it's *one Pokemon*
        addCopies(tags, 4, "VERDANT CAVERN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "HAU'OLI CITY"); //Trainers' School
        // counting as Hau'oli because the Pokemon are a subset
        addCopies(tags, 14, "ROUTE 4");
        addCopies(tags, 4, "PANIOLA TOWN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 10, "ROUTE 5");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "ROUTE 5");
        addCopies(tags, 6, "ROUTE 6");
        addCopies(tags, 18, "ROUTE 7");
        addCopies(tags, 24, "ROUTE 8");
        addCopies(tags, 2, "ROUTE 9");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "HANO BEACH");
        addCopies(tags, 4, "MEMORIAL HILL");
        addCopies(tags, 14, "AKALA OUTSKIRTS");
        addCopies(tags, 4, "DIGLETT'S TUNNEL");
        addCopies(tags, 10, "WELA VOLCANO PARK");
        addCopies(tags, 54, "BROOKLET HILL");
        addCopies(tags, 14, "LUSH JUNGLE");
        addCopies(tags, 8, "PANIOLA TOWN"); //Paniola Ranch
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 6, "MALIE CITY"); //Outer Cape
        addCopies(tags, 8, "ROUTE 10");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 2, "ROUTE 10");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 2, "ROUTE 10");
        addCopies(tags, 4, "UNUSED");
        addCopies(tags, 4, "ROUTE 10");
        addCopies(tags, 2, "ROUTE 12");
        addCopies(tags, 8, "SECLUDED SHORE");
        addCopies(tags, 20, "ROUTE 12");
        addCopies(tags, 6, "ROUTE 13");
        addCopies(tags, 2, "TAPU VILLAGE");
        addCopies(tags, 14, "ROUTE 14");
        addCopies(tags, 8, "ROUTE 15");
        //the first 2 of these are indistinguishable from route 16; I'm only assuming they're in the
        //same order as in USUM
        addCopies(tags, 2, "ROUTE 16");
        addCopies(tags, 2, "ROUTE 15");
        //same with these two
        addCopies(tags, 4, "ROUTE 16");
        //and these four
        //TODO: spading to verify these are the correct locations
        addCopies(tags, 2, "ROUTE 15");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 18, "ROUTE 17");
        addCopies(tags, 12, "ROUTE 11");
        addCopies(tags, 36, "HAINA DESERT");
        addCopies(tags, 8, "ULA'ULA MEADOW");
        addCopies(tags, 8, "MALIE GARDEN");
        //could be considered part of Malie City,
        //but doesn't really matter one way or the other as there's no shared Pokemon.
        addCopies(tags, 4, "UNUSED");
        addCopies(tags, 18, "MALIE GARDEN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "MOUNT HOKULANI");
        addCopies(tags, 4, "BLUSH MOUNTAIN");
        addCopies(tags, 10, "UNUSED");
        addCopies(tags, 8, "MOUNT LANAKILA");
        addCopies(tags, 2, "THRIFTY MEGAMART");
        addCopies(tags, 4, "SEAFOLK VILLAGE");
        addCopies(tags, 38, "PONI WILDS");
        addCopies(tags, 10, "PONI BREAKER COAST");
        addCopies(tags, 6, "ANCIENT PONI PATH");
        addCopies(tags, 12, "PONI GROVE");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 46, "PONI PLAINS");
        addCopies(tags, 2, "PONI COAST");
        addCopies(tags, 14, "PONI GAUNTLET");
        addCopies(tags, 12, "PONI MEADOW");
        addCopies(tags, 30, "VAST PONI CANYON");
        addCopies(tags, 4, "RESOLUTION CAVE");
        addCopies(tags, 8, "UNUSED");
        addCopies(tags, 4, "EXEGGUTOR ISLAND");
        return Collections.unmodifiableList(tags);
    }

    private static final List<EncounterType> smEncounterTypesTOD = initSMEncounterTypesTOD();

    private static List<EncounterType> initSMEncounterTypesTOD() {
        List<EncounterType> tags = new ArrayList<>();
        addCopies(tags, 8, EncounterType.WALKING); //Route 1 / Melemele Sea
        addCopies(tags, 10, EncounterType.FISHING);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.SURFING);
        addCopies(tags, 10, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.UNUSED);
        addCopies(tags, 12, EncounterType.WALKING); //Hau'oli City
        addCopies(tags, 4, EncounterType.WALKING); //Route 3 / Kala'e Bay
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT); //Considering berry piles INTERACT not AMBUSH
        addCopies(tags, 10, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 6, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.WALKING); //Route 2
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.WALKING); //Ten Carat Hill
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.WALKING); //Hau'oli Cemetery
        addCopies(tags, 2, EncounterType.WALKING); //Melemele Meadow
        addCopies(tags, 2, EncounterType.WALKING); //Seaward Cave
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT); //Berry Fields
        addCopies(tags, 2, EncounterType.WALKING); //Verdant Cavern
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING); //Trainers' School
        addCopies(tags, 12, EncounterType.WALKING); //Route 4
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 4, EncounterType.FISHING); //Paniola Town
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 6, EncounterType.AMBUSH); //Route 5
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 6, EncounterType.WALKING); //Route 6
        addCopies(tags, 2, EncounterType.SURFING); //Route 7
        addCopies(tags, 12, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.AMBUSH); //Route 8
        addCopies(tags, 8, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 10, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.FISHING); //Route 9
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH); //Hano Beach
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.WALKING); //Memorial Hill
        addCopies(tags, 2, EncounterType.WALKING); //Akala Outskirts
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH); //Diglett's Tunnel
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 10, EncounterType.WALKING); //Wela Volcano Park
        addCopies(tags, 2, EncounterType.WALKING); //Brooklet Hill
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 24, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.SURFING); //Brooklet Totem's Den
        addCopies(tags, 14, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.WALKING); //Lush Jungle
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 8, EncounterType.WALKING); //Paniola Ranch
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 6, EncounterType.WALKING); //Malie City (Outer Cape)
        addCopies(tags, 2, EncounterType.WALKING); //Route 10
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 2, EncounterType.WALKING); //Route 12 / Secluded Shore
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 20, EncounterType.WALKING);
        addCopies(tags, 6, EncounterType.FISHING); //Route 13
        addCopies(tags, 2, EncounterType.WALKING); //Tapu Village
        addCopies(tags, 12, EncounterType.FISHING); //Route 14
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.WALKING); //Route 15/16
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.WALKING); //Route 17
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 12, EncounterType.WALKING); //Route 11
        addCopies(tags, 8, EncounterType.WALKING); //Haina Desert
        addCopies(tags, 12, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 8, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.WALKING); //ULA'ULA MEADOW
        addCopies(tags, 2, EncounterType.WALKING); //MALIE GARDEN
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.UNUSED);
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING); //MOUNT HOKULANI
        addCopies(tags, 4, EncounterType.WALKING); //BLUSH MOUNTAIN
        addCopies(tags, 10, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING); //MOUNT LANAKILA
        addCopies(tags, 2, EncounterType.WALKING); //THRIFTY MEGAMART
        addCopies(tags, 4, EncounterType.FISHING); //SEAFOLK VILLAGE
        addCopies(tags, 2, EncounterType.WALKING); //PONI WILDS
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 6, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.INTERACT);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH); //PONI BREAKER COAST
        addCopies(tags, 8, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING); //ANCIENT PONI PATH
        addCopies(tags, 12, EncounterType.WALKING); //PONI GROVE
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.WALKING); //PONI PLAINS
        addCopies(tags, 12, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 28, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.AMBUSH); //PONI COAST
        addCopies(tags, 2, EncounterType.WALKING); //PONI GAUNTLET
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.WALKING); //PONI MEADOW
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 16, EncounterType.WALKING); //VAST PONI CANYON
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.AMBUSH);
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.WALKING); //RESOLUTION CAVE
        addCopies(tags, 8, EncounterType.UNUSED); //EXEGGUTOR ISLAND
        addCopies(tags, 4, EncounterType.WALKING);
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> usumLocationTagsTOD = initUSUMLocationTagsTOD();

    private static List<String> initUSUMLocationTagsTOD() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 6, "ROUTE 1");
        addCopies(tags, 10, "MELEMELE SEA");
        addCopies(tags, 4, "ROUTE 1");
        addCopies(tags, 2, "MELEMELE SEA");
        addCopies(tags, 12, "ROUTE 1");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 14, "MELEMELE SEA");
        addCopies(tags, 4, "UNUSED");
        addCopies(tags, 8, "HAU'OLI CITY");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 2, "HAU'OLI CITY");
        addCopies(tags, 2, "ROUTE 3");
        addCopies(tags, 2, "KALA'E BAY");
        addCopies(tags, 4, "ROUTE 3");
        addCopies(tags, 10, "KALA'E BAY");
        addCopies(tags, 8, "ROUTE 3");
        addCopies(tags, 4, "KALA'E BAY");
        addCopies(tags, 18, "ROUTE 2");
        addCopies(tags, 6, "ROUTE 1");
        addCopies(tags, 4, "TEN CARAT HILL");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "TEN CARAT HILL"); //Farthest Hollow
        addCopies(tags, 4, "HAU'OLI CEMETERY");
        addCopies(tags, 2, "MELEMELE MEADOW");
        addCopies(tags, 8, "SEAWARD CAVE");
        addCopies(tags, 2, "ROUTE 2"); //Berry Fields
        // counting as Route 2 because it's *one Pokemon*
        addCopies(tags, 2, "SANDY CAVE");
        addCopies(tags, 4, "VERDANT CAVERN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "HAU'OLI CITY"); //Trainers' School
        // counting as Hau'oli because of high Pokemon overlap
        addCopies(tags, 14, "ROUTE 4");
        addCopies(tags, 4, "PANIOLA TOWN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 10, "ROUTE 5");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "ROUTE 5");
        addCopies(tags, 6, "ROUTE 6");
        addCopies(tags, 18, "ROUTE 7");
        addCopies(tags, 24, "ROUTE 8");
        addCopies(tags, 4, "ROUTE 9");
        addCopies(tags, 8, "HANO BEACH");
        addCopies(tags, 6, "DIVIDING PEAK TUNNEL");
        addCopies(tags, 4, "MEMORIAL HILL");
        addCopies(tags, 14, "AKALA OUTSKIRTS");
        addCopies(tags, 4, "DIGLETT'S TUNNEL");
        addCopies(tags, 10, "WELA VOLCANO PARK");
        addCopies(tags, 52, "BROOKLET HILL");
        addCopies(tags, 14, "LUSH JUNGLE");
        addCopies(tags, 8, "PANIOLA TOWN"); //Paniola Ranch
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 6, "MALIE CITY"); //OUTER COVE
        addCopies(tags, 8, "ROUTE 10");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 2, "ROUTE 10");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 2, "ROUTE 10");
        addCopies(tags, 4, "UNUSED");
        addCopies(tags, 4, "ROUTE 10");
        addCopies(tags, 22, "ROUTE 12");
        addCopies(tags, 6, "ROUTE 13");
        addCopies(tags, 2, "TAPU VILLAGE");
        addCopies(tags, 14, "ROUTE 14");
        addCopies(tags, 8, "ROUTE 15");
        addCopies(tags, 2, "ROUTE 16");
        addCopies(tags, 2, "ROUTE 15");
        addCopies(tags, 4, "ROUTE 16");
        addCopies(tags, 2, "ROUTE 15");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 18, "ROUTE 17");
        addCopies(tags, 12, "ROUTE 11");
        addCopies(tags, 2, "ULA'ULA BEACH");
        addCopies(tags, 36, "HAINA DESERT");
        addCopies(tags, 8, "ULA'ULA MEADOW");
        addCopies(tags, 8, "MALIE GARDEN");
        //could be considered part of Malie City,
        //but doesn't really matter one way or the other as there's no shared Pokemon.
        addCopies(tags, 4, "UNUSED");
        addCopies(tags, 18, "MALIE GARDEN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 8, "MOUNT HOKULANI");
        addCopies(tags, 2, "BLUSH MOUNTAIN");
        addCopies(tags, 8, "UNUSED");
        addCopies(tags, 2, "BLUSH MOUNTAIN");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 14, "MOUNT LANAKILA");
        addCopies(tags, 2, "THRIFTY MEGAMART");
        addCopies(tags, 4, "SEAFOLK VILLAGE");
        addCopies(tags, 22, "PONI WILDS");
        addCopies(tags, 16, "PONI BREAKER COAST");
        addCopies(tags, 6, "ANCIENT PONI PATH");
        addCopies(tags, 12, "PONI GROVE");
        addCopies(tags, 2, "UNUSED");
        addCopies(tags, 46, "PONI PLAINS");
        addCopies(tags, 2, "PONI COAST");
        addCopies(tags, 14, "PONI GAUNTLET");
        addCopies(tags, 12, "PONI MEADOW");
        addCopies(tags, 30, "VAST PONI CANYON");
        addCopies(tags, 4, "RESOLUTION CAVE");
        addCopies(tags, 4, "EXEGGUTOR ISLAND");
        return Collections.unmodifiableList(tags);
    }

    private static final List<EncounterType> usumEncounterTypesTOD = initUSUMEncounterTypesTOD();

    private static List<EncounterType> initUSUMEncounterTypesTOD() {
        List<EncounterType> tags = new ArrayList<>();
        addCopies(tags, 6, EncounterType.WALKING); //Route 1 / Melemele Sea
        addCopies(tags, 10, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 12, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 10, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.UNUSED); //Hau'oli City
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.WALKING); //Route 3 / Kala'e Bay
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT); //Considering berry piles INTERACT not AMBUSH
        addCopies(tags, 10, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 6, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.WALKING); //Route 2
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.WALKING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.WALKING); //Ten Carat Hill
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.WALKING); //Hau'oli Cemetery
        addCopies(tags, 2, EncounterType.WALKING); //Melemele Meadow
        addCopies(tags, 2, EncounterType.WALKING); //Seaward Cave
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT); //Berry Fields
        addCopies(tags, 2, EncounterType.SPECIAL); //SANDY CAVE
        //same tables used for both walking and surfing
        addCopies(tags, 2, EncounterType.WALKING); //Verdant Cavern
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING); //Trainers' School
        addCopies(tags, 12, EncounterType.WALKING); //Route 4
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 4, EncounterType.FISHING); //Paniola Town
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 6, EncounterType.AMBUSH); //Route 5
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 6, EncounterType.WALKING); //Route 6
        addCopies(tags, 2, EncounterType.SURFING); //Route 7
        addCopies(tags, 12, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.AMBUSH); //Route 8
        addCopies(tags, 8, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 10, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.FISHING); //Route 9
        addCopies(tags, 2, EncounterType.AMBUSH); //Hano Beach
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.AMBUSH);
        addCopies(tags, 6, EncounterType.AMBUSH); //DIVIDING PEAK TUNNEL
        addCopies(tags, 4, EncounterType.WALKING); //Memorial Hill
        addCopies(tags, 2, EncounterType.WALKING); //Akala Outskirts
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH); //Diglett's Tunnel
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 10, EncounterType.WALKING); //Wela Volcano Park
        addCopies(tags, 2, EncounterType.WALKING); //Brooklet Hill
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 22, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.SURFING); //Brooklet Totem's Den
        addCopies(tags, 14, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.WALKING); //Lush Jungle
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 8, EncounterType.WALKING); //Paniola Ranch
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 6, EncounterType.WALKING); //Malie City (Outer Cape)
        addCopies(tags, 2, EncounterType.WALKING); //Route 10
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 4, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 22, EncounterType.WALKING); //Route 12
        addCopies(tags, 6, EncounterType.FISHING); //Route 13
        addCopies(tags, 2, EncounterType.WALKING); //Tapu Village
        addCopies(tags, 12, EncounterType.FISHING); //Route 14
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.WALKING); //Route 15/16
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.WALKING); //Route 17
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 12, EncounterType.WALKING); //Route 11
        addCopies(tags, 2, EncounterType.INTERACT); //ULA'ULA BEACH
        addCopies(tags, 8, EncounterType.WALKING); //Haina Desert
        addCopies(tags, 12, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.WALKING);
        addCopies(tags, 8, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.WALKING); //ULA'ULA MEADOW
        addCopies(tags, 2, EncounterType.WALKING); //MALIE GARDEN
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 4, EncounterType.UNUSED);
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 8, EncounterType.WALKING); //MOUNT HOKULANI
        addCopies(tags, 2, EncounterType.WALKING); //BLUSH MOUNTAIN
        addCopies(tags, 8, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.UNUSED); //RUINS OF ABUNDANCE
        addCopies(tags, 14, EncounterType.WALKING); //MOUNT LANAKILA
        addCopies(tags, 2, EncounterType.WALKING); //THRIFTY MEGAMART
        addCopies(tags, 4, EncounterType.FISHING); //SEAFOLK VILLAGE
        addCopies(tags, 2, EncounterType.WALKING); //PONI WILDS
        addCopies(tags, 6, EncounterType.INTERACT);
        addCopies(tags, 14, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.SURFING); //PONI BREAKER COAST
        addCopies(tags, 6, EncounterType.AMBUSH);
        addCopies(tags, 8, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING); //ANCIENT PONI PATH
        addCopies(tags, 12, EncounterType.WALKING); //PONI GROVE
        addCopies(tags, 2, EncounterType.UNUSED);
        addCopies(tags, 2, EncounterType.WALKING); //PONI PLAINS
        addCopies(tags, 12, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.INTERACT);
        addCopies(tags, 28, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.AMBUSH);
        addCopies(tags, 2, EncounterType.AMBUSH); //PONI COAST
        addCopies(tags, 2, EncounterType.WALKING); //PONI GAUNTLET
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 2, EncounterType.WALKING); //PONI MEADOW
        addCopies(tags, 4, EncounterType.FISHING);
        addCopies(tags, 6, EncounterType.WALKING);
        addCopies(tags, 16, EncounterType.WALKING); //VAST PONI CANYON
        addCopies(tags, 2, EncounterType.SURFING);
        addCopies(tags, 4, EncounterType.AMBUSH);
        addCopies(tags, 6, EncounterType.FISHING);
        addCopies(tags, 2, EncounterType.WALKING);
        addCopies(tags, 4, EncounterType.WALKING); //RESOLUTION CAVE
        addCopies(tags, 4, EncounterType.WALKING);
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> smLocationTagsNoTOD = initSMLocationTagsNoTOD();

    private static List<String> initSMLocationTagsNoTOD() {
        //SM are convenient in that EVERY encounter has both day and night mode.
        //So, to convert from ToD to NoToD... just remove half of them
        //(...this would probably be better to do the other way around...
        // but I've already made the ToD version and the whole point of this is to keep from having to
        // convert the whole thing)
        List<String> tags = new ArrayList<>(smLocationTagsTOD);
        Iterator<String> itor = tags.iterator();
        while (itor.hasNext()) {
            itor.next();
            if (!itor.hasNext()) {
                throw new IllegalStateException("Failed building tags - wrong number for ToD!");
            }
            itor.next();
            itor.remove();
        }

        return Collections.unmodifiableList(tags);
    }

    private static final List<String> usumLocationTagsNoTOD = initUSUMLocationTagsNoTOD();

    private static List<String> initUSUMLocationTagsNoTOD() {
        //SM are convenient in that EVERY encounter has both day and night mode.
        //So, to convert from ToD to NoToD... just remove half of them
        //(...this would probably be better to do the other way around...
        // but I've already made the ToD version and the whole point of this is to keep from having to
        // convert the whole thing)
        List<String> tags = new ArrayList<>(usumLocationTagsTOD);
        Iterator<String> itor = tags.iterator();
        while (itor.hasNext()) {
            itor.next();
            if (!itor.hasNext()) {
                throw new IllegalStateException("Failed building tags - wrong number for ToD!");
            }
            itor.next();
            itor.remove();
        }

        return Collections.unmodifiableList(tags);
    }

    private static final List<EncounterType> smEncounterTypesNoTOD = initSMEncounterTypesNoTOD();

    private static List<EncounterType> initSMEncounterTypesNoTOD() {
        //SM are convenient in that EVERY encounter has both day and night mode.
        //So, to convert from ToD to NoToD... just remove half of them
        //(...this would probably be better to do the other way around...
        // but I've already made the ToD version and the whole point of this is to keep from having to
        // convert the whole thing)
        List<EncounterType> tags = new ArrayList<>(smEncounterTypesTOD);
        Iterator<EncounterType> itor = tags.iterator();
        while (itor.hasNext()) {
            itor.next();
            if (!itor.hasNext()) {
                throw new IllegalStateException("Failed building encounter types - wrong number for ToD!");
            }
            itor.next();
            itor.remove();
        }

        return Collections.unmodifiableList(tags);
    }

    private static final List<EncounterType> usumEncounterTypesNoTOD = initUSUMEncounterTypesNoTOD();

    private static List<EncounterType> initUSUMEncounterTypesNoTOD() {
        //SM are convenient in that EVERY encounter has both day and night mode.
        //So, to convert from ToD to NoToD... just remove half of them
        //(...this would probably be better to do the other way around...
        // but I've already made the ToD version and the whole point of this is to keep from having to
        // convert the whole thing)
        List<EncounterType> tags = new ArrayList<>(usumEncounterTypesTOD);
        Iterator<EncounterType> itor = tags.iterator();
        while (itor.hasNext()) {
            itor.next();
            if (!itor.hasNext()) {
                throw new IllegalStateException("Failed building encounter types - wrong number for ToD!");
            }
            itor.next();
            itor.remove();
        }

        return Collections.unmodifiableList(tags);
    }

    private static final TagPackMap Gen7TagPackMap = new Builder()
            .newPack(Gen7Constants.Type_SM, smLocationTagsNoTOD, smLocationTagsTOD)
                .encounterTypes(smEncounterTypesNoTOD, smEncounterTypesTOD)
                .postGameAreas(smPostGameEncounterAreasNoTOD, smPostGameEncounterAreasTOD)
            .newPack(Gen7Constants.Type_USUM, usumLocationTagsNoTOD, usumLocationTagsTOD)
                .encounterTypes(usumEncounterTypesNoTOD, usumEncounterTypesTOD)
                .postGameAreas(usumPostGameEncounterAreasNoTOD, usumPostGameEncounterAreasTOD)
            .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen7TagPackMap;
    }
}
