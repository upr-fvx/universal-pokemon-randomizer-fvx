package com.dabomstew.pkromio.constants.enctaggers;

import com.dabomstew.pkromio.constants.Gen4Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Gen4EncounterAreaTagger extends EncounterAreaTagger {

    private static final int[] dpPostGameEncounterAreas = new int[]{
            530, 531, 532, 533, //Resort Area
            492, 493, 494, 495, 496, 497, //Route 224
            498, 499, 500, 501, 502, 503, //Route 225
            542, 543, 544, 545, 546, 547, //Route 226
            504, 505, 506, 507, 508, 509, //Route 227
            510, 511, 512, 513, 514, 515, //Route 228
            516, 517, 518, 519, 520, 521, //Route 229
            548, 549, 550, 551, 552, 553, //Route 230
            178, 179, 180, 181, 182, 183, //Stark Mountain
            184, 185, 186, 187, 188, 189, //Sendoff Spring
            196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
            208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219,
            220, 221, 222, 223, 224, 225, 226, 227, 228, 229, //Turnback Cave
            282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, //Snowpoint Temple
            3, 7, 11, 15, 19, 29, 37, 43, 57, 75, 81, 87, 93, 99, 107,
            153, 159, 169, 177, 309, 345, 351, 357, 363, 377, 383, 389,
            395, 401, 411, 417, 435, 445, 451, 457, 463, 475, 479, 485,
            491, 525, 529, 537, 541, //Super Rod
            21, 23, 25, 31, 33, 39, 45, 47, 49, 51, 53, 59, 61, 63, 65,
            67, 69, 71, 77, 83, 89, 95, 101, 103, 109, 111, 113, 115, 117,
            119, 121, 123, 125, 127, 129, 131, 133, 135, 137, 139, 141,
            143, 145, 147, 149, 155, 161, 163, 165, 171, 173, 295, 297,
            299, 301, 303, 305, 311, 313, 315, 317, 319, 321, 323, 325,
            327, 329, 331, 333, 335, 337, 339, 341, 347, 353, 359, 365,
            367, 369, 371, 373, 379, 385, 391, 397, 403, 405, 407, 413,
            419, 421, 423, 425, 427, 429, 431, 437, 439, 441, 447, 453,
            459, 465, 467, 469, 471, 481, 487, //Swarm/Radar/GBA
            558, //Trophy Garden Rotating Pokemon
            559, //Great Marsh Rotating Pokemon (Post-National Dex)
            154, 156, 157, 158, 160, 162, //Victory Road (back)
            190, 191, 192, 193, 194, 195, 230, 231, 232, 233, 234, 235,
            236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247,
            248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259,
            260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271,
            272, 273, 274, 275, 276, 277, 278, 279, 280, 281 //Unknown areas - likely unused
    };

    private static final int[] platPostGameEncounterAreas = new int[]{
            534, 535, 536, 537, //Resort Area
            496, 497, 498, 499, 500, 501, //Route 224
            502, 503, 504, 505, 506, 507, //Route 225
            546, 547, 548, 549, 550, 551, //Route 226
            508, 509, 510, 511, 512, 513, //Route 227
            514, 515, 516, 517, 518, 519, //Route 228
            520, 521, 522, 523, 524, 525, //Route 229
            552, 553, 554, 555, 556, 557, //Route 230
            182, 183, 184, 185, 186, 187, //Stark Mountain
            200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211,
            212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223,
            224, 225, 226, 227, 228, 229, 230, 231, 232, 233, //Turnback Cave
            286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, //Snowpoint Temple
            3, 7, 11, 15, 19, 29, 37, 43, 57, 75, 81, 87, 93, 99, 105,
            111, 157, 163, 173, 181, 193, 313, 349, 355, 361, 367, 381,
            387, 393, 399, 405, 415, 421, 439, 449, 455, 461, 467, 479,
            483, 489, 495, 529, 533, 541, 545, //Super Rod
            21, 23, 25, 31, 33, 39, 45, 47, 49, 51, 53, 59, 61, 63, 65,
            67, 69, 71, 77, 83, 89, 95, 101, 107, 113, 115, 117, 119, 121,
            123, 125, 127, 129, 131, 133, 135, 137, 139, 141, 143, 145,
            147, 149, 151, 153, 159, 165, 167, 169, 175, 177, 189, 299,
            301, 303, 305, 307, 309, 315, 317, 319, 321, 323, 325, 327,
            329, 331, 333, 335, 337, 339, 341, 343, 345, 351, 357, 363,
            369, 371, 373, 375, 377, 383, 389, 395, 401, 407, 409, 411,
            417, 423, 425, 427, 429, 431, 433, 435, 441, 443, 445, 451,
            457, 463, 469, 471, 473, 475, 485, 491, //Swarm/Radar/GBA
            562, //Trophy Garden Rotating Pokemon
            563, //Great Marsh Rotating Pokemon (Post-National Dex)
            158, 160, 161, 162, 164, 166, //back of Victory Road
            194, 195, 196, 197, 198, 199, 234, 235, 236, 237, 238, 239,
            240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251,
            252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263,
            264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275,
            276, 277, 278, 279, 280, 281, 282, 283, 284, 285 //Unknown areas - probably unused
    };

    private static final int[] hgssPostGameEncounterAreasTOD = new int[]{
            676, 677, 678, 679, 680, 681, 682, 1051, //Pallet Town
            683, 684, 685, 686, 687, 688, 689, 1052, //Viridian City
            1053, //Pewter City
            690, 691, 692, 693, 694, 695, 696, 1054, //Cerulean City
            697, 698, 699, 700, 701, 702, 703, 704, 1055, //Vermilion City
            705, 706, 1056, //Celadon City
            707, 708, 709, 710, 711, 712, 713, 1057, //Fuchsia City
            785, 786, 787, 788, 789, 1016, //Route 1
            790, 791, 792, 793, 794, 971, 972, 973, 974, 975, 1017, 1072, //Route 2
            795, 796, 797, 798, 799, 1018, //Route 3
            800, 801, 802, 803, 804, 805, 806, 807, 808, 809, 810, 1019, //Route 4
            811, 812, 813, 814, 815, 1020, //Route 5
            816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 1021, //Route 6
            827, 828, 829, 830, 831, 1022, //Route 7
            832, 833, 834, 835, 836, 1023, //Route 8
            837, 838, 839, 840, 841, 842, 843, 844, 845, 846, 847, //Route 9
            848, 849, 850, 851, 852, 853, 854, 855, 856, 857, 858, //Route 10
            859, 860, 861, 862, 863, 1024, //Route 11
            654, 655, 656, 657, 658, 659, 660, 1025, //Route 12
            864, 865, 866, 867, 868, 869, 870, 871, 872, 873, 874, 1026, //Route 13
            875, 876, 877, 878, 879, 1027, //Route 14
            880, 881, 882, 883, 884, 1028, //Route 15
            885, 886, 887, 888, 889, 1073, //Route 16
            890, 891, 892, 893, 894, //Route 17
            895, 896, 897, 898, 899, 1029, //Route 18
            661, 662, 663, 664, 665, 666, 667, 668, //Route 19
            669, 670, 671, 672, 673, 674, 675, //Route 20
            900, 901, 902, 903, 904, 905, 906, 907, 908, 909, 910, 1065, //Route 21
            911, 912, 913, 914, 915, 916, 917, 918, 919, 920, 921, 1030, //Route 22
            922, 923, 924, 925, 926, 927, 928, 929, 930, 931, 932, //Route 24
            933, 934, 935, 936, 937, 938, 939, 940, 941, 942, 943, 1031, //Route 25
            714, 715, 716, 717, 718, 719, 720, //Cinnabar Island
            981, 982, 983, 984, 985, 986, 987, 988, 989, 990, 991, 992, 993,
            994, 995, 996, 997, 998, 999, 1000, 1001, 1002, 1003, 1004, 1005,
            1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, //Cerulean Cave
            955, 956, 957, 958, 959, //DIGLETT’s Cave
            769, 770, 771, 772, 773, 774, 775, 776, 777, 778, 779, //Rock Tunnel
            518, 519, 520, 521, 522, 523, 524, 525, 526, 527, 528, 529, 530,
            531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543,
            544, 545, 546, 547, 548, //Seafoam Islands
            976, 977, 978, 979, 980, 1068, //Viridian Forest
            504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516,
            517, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, //Mt. Moon
            748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 758, 1034, //Route 28
            549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561,
            562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574,
            575, 576, 577, 578, 579, 580, 581, 600, 601, 602, 603, 604, 605,
            606, 607, 608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618,
            619, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 631,
            632, 633, 634, 635, 636, 637, 638, 639, 640, 641, 642, 1064, 1074, //Mt. Silver
            593, //Cliff Cave Rock Smash
            3, 15, 26, 37, 44, 65, 77, 108, 119, 130, 146, 157, 168, 179, 190,
            217, 284, 291, 298, 309, 325, 338, 349, 360, 376, 387, 398, 405,
            416, 443, 450, 461, 478, 489, 500, 585, 650, 733, 744, 951, //Super Rod
            4, 6, 11, 16, 18, 27, 29, 38, 40, 45, 47, 52, 57, 66, 68, 78, 80,
            85, 90, 95, 100, 109, 111, 120, 122, 131, 133, 138, 147, 149, 158,
            160, 169, 171, 180, 182, 191, 193, 198, 203, 208, 213, 218, 220,
            225, 230, 235, 240, 245, 250, 255, 260, 265, 270, 275, 280, 285,
            287, 292, 294, 299, 301, 310, 312, 317, 326, 328, 333, 339, 341,
            350, 352, 361, 363, 368, 377, 379, 388, 390, 399, 401, 406, 408,
            417, 419, 424, 429, 434, 439, 444, 446, 451, 453, 462, 464, 469,
            479, 481, 490, 492, 501, 503, 586, 588, 594, 599, 651, 653, 725,
            734, 736, 745, 747, 784, 952, 954, 964, 970, //Swarm
            10, 22, 33, 51, 56, 61, 72, 84, 89, 94, 99, 104, 115, 126, 137,
            142, 153, 164, 175, 186, 197, 202, 207, 212, 224, 229, 234, 239,
            244, 249, 254, 259, 264, 269, 274, 279, 305, 316, 321, 332, 345,
            356, 367, 372, 383, 394, 412, 423, 428, 433, 438, 457, 468, 473,
            485, 496, 592, 598, 646, 724, 729, 740, 783, 947, 963, 968, //Radio
            1076, 1077, 1078, //Post-National Dex Bug-Catching Contests
            643, 644, 645, 647, 648, 649, 652, 1071 //Safari Zone - unused?
    };

    private static final int[] hgssPartialPostGameTOD = new int[]{
            1044, 1059, 1066 //headbutt trees
    };

    private static final int[] hgssPostGameEncounterAreasNoTOD = new int[]{
            532, 533, 534, 535, 536, 537, 538, 829, //Pallet Town
            539, 540, 541, 542, 543, 544, 545, 830, //Viridian City
            831, //Pewter City
            546, 547, 548, 549, 550, 551, 552, 832, //Cerulean City
            553, 554, 555, 556, 557, 558, 559, 560, 833, //Vermilion City
            561, 562, 834, //Celadon City
            563, 564, 565, 566, 567, 568, 569, 835, //Fuchsia City
            623, 624, 625, 794, //Route 1
            626, 627, 628, 759, 760, 761, 795, 850, //Route 2
            629, 630, 631, 796, //Route 3
            632, 633, 634, 635, 636, 637, 638, 639, 640, 797, //Route 4
            641, 642, 643, 798, //Route 5
            644, 645, 646, 647, 648, 649, 650, 651, 652, 799, //Route 6
            653, 654, 655, 800, //Route 7
            656, 657, 658, 801, //Route 8
            659, 660, 661, 662, 663, 664, 665, 666, 667, //Route 9
            668, 669, 670, 671, 672, 673, 674, 675, 676, //Route 10
            677, 678, 679, 802, //Route 11
            510, 511, 512, 513, 514, 515, 516, 803, //Route 12
            680, 681, 682, 683, 684, 685, 686, 687, 688, 804, //Route 13
            689, 690, 691, 805, //Route 14
            692, 693, 694, 806, //Route 15
            695, 696, 697, 851, //Route 16
            698, 699, 700, //Route 17
            701, 702, 703, 807, //Route 18
            517, 518, 519, 520, 521, 522, 523, 524, //Route 19
            525, 526, 527, 528, 529, 530, 531, //Route 20
            704, 705, 706, 707, 708, 709, 710, 711, 712, 843, //Route 21
            713, 714, 715, 716, 717, 718, 719, 720, 721, 808, //Route 22
            722, 723, 724, 725, 726, 727, 728, 729, 730, //Route 24
            731, 732, 733, 734, 735, 736, 737, 738, 739, 809, //Route 25
            570, 571, 572, 573, 574, 575, 576, //Cinnabar Island
            765, 766, 767, 768, 769, 770, 771, 772, 773, 774, 775, 776, 777,
            778, 779, 780, 781, 782, 783, 784, 785, 786, 787, 788, 789, 790,
            791, 792, 793, //Cerulean Cave
            749, 750, 751, //DIGLETT’s Cave
            613, 614, 615, 616, 617, 618, 619, //Rock Tunnel
            406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418,
            419, 420, 421, 422, 423, 424, 425, 426, //Seafoam Islands
            762, 763, 764, 846, //Viridian Forest
            392, 393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404,
            405, 607, 608, 609, 610, 611, 612, //Mt. Moon
            598, 599, 600, 601, 602, 603, 604, 605, 606, 812, //Route 28
            427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439,
            440, 441, 442, 443, 444, 445, 446, 447, 448, 449, 450, 451, 452,
            453, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479,
            480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492,
            493, 494, 495, 496, 497, 498, 499, 500, 842, 852, //Mt. Silver
            463, //Cliff Cave Rock Smash
            3, 13, 22, 31, 38, 53, 63, 84, 93, 102, 114, 123, 132, 141, 150,
            169, 212, 219, 226, 235, 247, 258, 267, 276, 288, 297, 306, 313,
            322, 341, 348, 357, 370, 379, 388, 457, 506, 585, 594, 745, //Super Rod
            4, 6, 9, 14, 16, 23, 25, 32, 34, 39, 41, 44, 47, 54, 56, 64, 66,
            69, 72, 75, 78, 85, 87, 94, 96, 103, 105, 108, 115, 117, 124, 126,
            133, 135, 142, 144, 151, 153, 156, 159, 162, 165, 170, 172, 175,
            178, 181, 184, 187, 190, 193, 196, 199, 202, 205, 208, 213, 215,
            220, 222, 227, 229, 236, 238, 241, 248, 250, 253, 259, 261, 268,
            270, 277, 279, 282, 289, 291, 298, 300, 307, 309, 314, 316, 323,
            325, 328, 331, 334, 337, 342, 344, 349, 351, 358, 360, 363, 371,
            373, 380, 382, 389, 391, 458, 460, 464, 467, 507, 509, 579, 586,
            588, 595, 597, 622, 746, 748, 754, 758, //Swarm
            8, 18, 27, 43, 46, 49, 58, 68, 71, 74, 77, 80, 89, 98, 107, 110,
            119, 128, 137, 146, 155, 158, 161, 164, 174, 177, 180, 183, 186,
            189, 192, 195, 198, 201, 204, 207, 231, 240, 243, 252, 263, 272,
            281, 284, 293, 302, 318, 327, 330, 333, 336, 353, 362, 365, 375,
            384, 462, 466, 502, 578, 581, 590, 621, 741, 753, 756, //Radio
            854, 855, 856, //Post-National Dex Bug-Catching Contests
            501, 503, 504, 505, 508, 849, //Safari Zone - unused?
    };

    private static final int[] hgssPartialPostGameNoTOD = new int[]{
            822, 837, 844 //headbutt trees
    };

    private static final int headbuttPartialPostgameCutoff = 12;

    private static final List<String> locationTagsDP = initLocationTagsDP();

    private static List<String> initLocationTagsDP() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "CANALAVE CITY");
        addCopies(tags, 4, "ETERNA CITY");
        addCopies(tags, 4, "PASTORIA CITY");
        addCopies(tags, 4, "SUNYSHORE CITY");
        addCopies(tags, 4, "POKEMON LEAGUE");
        addCopies(tags, 4, "OREBURGH MINE");
        addCopies(tags, 6, "VALLEY WINDWORKS");
        addCopies(tags, 2, "ETERNA FOREST");
        addCopies(tags, 6, "FUEGO IRONWORKS");
        addCopies(tags, 38, "MT. CORONET");
        addCopies(tags, 32, "GREAT MARSH");
        addCopies(tags, 36, "SOLACEON RUINS");
        addCopies(tags, 20, "VICTORY ROAD");
        addCopies(tags, 6, "RAVAGED PATH");
        addCopies(tags, 8, "OREBURGH GATE");
        addCopies(tags, 6, "STARK MOUNTAIN");
        addCopies(tags, 6, "SENDOFF SPRING");
        addCopies(tags, 6, "UNKNOWN");
        addCopies(tags, 34, "TURNBACK CAVE");
        addCopies(tags, 52, "UNKNOWN");
        addCopies(tags, 12, "SNOWPOINT TEMPLE");
        addCopies(tags, 4, "WAYWARD CAVE");
        addCopies(tags, 6, "MANIAC TUNNEL");
        addCopies(tags, 2, "TROPHY GARDEN");
        addCopies(tags, 16, "IRON ISLAND");
        addCopies(tags, 18, "OLD CHATEAU");
        addCopies(tags, 12, "LAKE VERITY");
        addCopies(tags, 6, "LAKE VALOR");
        addCopies(tags, 6, "LAKE ACUITY");
        addCopies(tags, 2, "VALOR LAKEFRONT");
        addCopies(tags, 2, "ACUITY LAKEFRONT");
        addCopies(tags, 2, "ROUTE 201");
        addCopies(tags, 2, "ROUTE 202");
        addCopies(tags, 6, "ROUTE 203");
        addCopies(tags, 12, "ROUTE 204");
        addCopies(tags, 12, "ROUTE 205");
        addCopies(tags, 2, "ROUTE 206");
        addCopies(tags, 2, "ROUTE 207");
        addCopies(tags, 6, "ROUTE 208");
        addCopies(tags, 16, "ROUTE 209");
        addCopies(tags, 8, "ROUTE 210");
        addCopies(tags, 4, "ROUTE 211");
        addCopies(tags, 12, "ROUTE 212");
        addCopies(tags, 6, "ROUTE 213");
        addCopies(tags, 6, "ROUTE 214");
        addCopies(tags, 2, "ROUTE 215");
        addCopies(tags, 2, "ROUTE 216");
        addCopies(tags, 2, "ROUTE 217");
        addCopies(tags, 6, "ROUTE 218");
        addCopies(tags, 4, "ROUTE 219");
        addCopies(tags, 6, "ROUTE 221");
        addCopies(tags, 6, "ROUTE 222");
        addCopies(tags, 6, "ROUTE 224");
        addCopies(tags, 6, "ROUTE 225");
        addCopies(tags, 6, "ROUTE 227");
        addCopies(tags, 6, "ROUTE 228");
        addCopies(tags, 6, "ROUTE 229");
        addCopies(tags, 4, "TWINLEAF TOWN");
        addCopies(tags, 4, "CELESTIC TOWN");
        addCopies(tags, 4, "RESORT AREA");
        addCopies(tags, 4, "ROUTE 220");
        addCopies(tags, 4, "ROUTE 223");
        addCopies(tags, 6, "ROUTE 226");
        addCopies(tags, 6, "ROUTE 230");
        addCopies(tags, 1, "MT. CORONET");
        addCopies(tags, 3, "HONEY TREE");
        addCopies(tags, 1, "TROPHY GARDEN");
        addCopies(tags, 2, "GREAT MARSH");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> locationTagsPt = initLocationTagsPt();

    private static List<String> initLocationTagsPt() {
        List<String> locationTags = new ArrayList<>();
        locationTags.addAll(locationTagsDP.subList(0, 108));
        locationTags.addAll(Arrays.asList("GREAT MARSH", "GREAT MARSH", "GREAT MARSH", "GREAT MARSH"));
        locationTags.addAll(locationTagsDP.subList(108, 561));
        return Collections.unmodifiableList(locationTags);
    }

    private static final List<String> locationTagsNoTimeHGSS = initLocationTagsNoTimeHGSS();

    private static List<String> initLocationTagsNoTimeHGSS() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 7, "NEW BARK TOWN");
        addCopies(tags, 3, "ROUTE 29");
        addCopies(tags, 7, "CHERRYGROVE CITY");
        addCopies(tags, 9, "ROUTE 30");
        addCopies(tags, 9, "ROUTE 31");
        addCopies(tags, 7, "VIOLET CITY");
        addCopies(tags, 6, "SPROUT TOWER");
        addCopies(tags, 9, "ROUTE 32");
        addCopies(tags, 22, "RUINS OF ALPH");
        addCopies(tags, 27, "UNION CAVE");
        addCopies(tags, 3, "ROUTE 33");
        addCopies(tags, 18, "SLOWPOKE WELL");
        addCopies(tags, 9, "ILEX FOREST");
        addCopies(tags, 9, "ROUTE 34");
        addCopies(tags, 9, "ROUTE 35");
        addCopies(tags, 6, "NATIONAL PARK");
        addCopies(tags, 3, "ROUTE 36");
        addCopies(tags, 3, "ROUTE 37");
        addCopies(tags, 7, "ECRUTEAK CITY");
        addCopies(tags, 6, "BURNED TOWER");
        addCopies(tags, 24, "BELL TOWER");
        addCopies(tags, 3, "ROUTE 38");
        addCopies(tags, 3, "ROUTE 39");
        addCopies(tags, 7, "OLIVINE CITY");
        addCopies(tags, 7, "ROUTE 40");
        addCopies(tags, 7, "ROUTE 41");
        addCopies(tags, 24, "WHIRL ISLANDS");
        addCopies(tags, 8, "CIANWOOD CITY");
        addCopies(tags, 9, "ROUTE 42");
        addCopies(tags, 30, "MT. MORTAR");
        addCopies(tags, 9, "ROUTE 43");
        addCopies(tags, 7, "LAKE OF RAGE");
        addCopies(tags, 9, "ROUTE 44");
        addCopies(tags, 12, "ICE PATH");
        addCopies(tags, 7, "BLACKTHORN CITY");
        addCopies(tags, 7, "DRAGON'S DEN");
        addCopies(tags, 9, "ROUTE 45");
        addCopies(tags, 3, "ROUTE 46");
        addCopies(tags, 19, "DARK CAVE");
        addCopies(tags, 9, "ROUTE 47");
        addCopies(tags, 14, "MT. MOON");
        addCopies(tags, 21, "SEAFOAM ISLANDS");
        addCopies(tags, 27, "MT. SILVER CAVE");
        addCopies(tags, 11, "CLIFF EDGE GATE");
        addCopies(tags, 3, "BELL TOWER");
        addCopies(tags, 9, "MT. SILVER");
        addCopies(tags, 23, "MT. SILVER CAVE");
        addCopies(tags, 9, "SAFARI ZONE");
        addCopies(tags, 7, "ROUTE 12");
        addCopies(tags, 8, "ROUTE 19");
        addCopies(tags, 7, "ROUTE 20");
        addCopies(tags, 7, "PALLET TOWN");
        addCopies(tags, 7, "VIRIDIAN CITY");
        addCopies(tags, 7, "CERULEAN CITY");
        addCopies(tags, 8, "VERMILION CITY");
        addCopies(tags, 2, "CELADON CITY");
        addCopies(tags, 8, "FUCHSIA CITY");
        addCopies(tags, 7, "CINNABAR ISLAND");
        addCopies(tags, 3, "ROUTE 48");
        addCopies(tags, 9, "ROUTE 26");
        addCopies(tags, 9, "ROUTE 27");
        addCopies(tags, 9, "ROUTE 28");
        addCopies(tags, 6, "MT. MOON");
        addCopies(tags, 7, "ROCK TUNNEL");
        addCopies(tags, 3, "VICTORY ROAD");
        addCopies(tags, 3, "ROUTE 1");
        addCopies(tags, 3, "ROUTE 2");
        addCopies(tags, 3, "ROUTE 3");
        addCopies(tags, 9, "ROUTE 4");
        addCopies(tags, 3, "ROUTE 5");
        addCopies(tags, 9, "ROUTE 6");
        addCopies(tags, 3, "ROUTE 7");
        addCopies(tags, 3, "ROUTE 8");
        addCopies(tags, 9, "ROUTE 9");
        addCopies(tags, 9, "ROUTE 10");
        addCopies(tags, 3, "ROUTE 11");
        addCopies(tags, 9, "ROUTE 13");
        addCopies(tags, 3, "ROUTE 14");
        addCopies(tags, 3, "ROUTE 15");
        addCopies(tags, 3, "ROUTE 16");
        addCopies(tags, 3, "ROUTE 17");
        addCopies(tags, 3, "ROUTE 18");
        addCopies(tags, 9, "ROUTE 21");
        addCopies(tags, 9, "ROUTE 22");
        addCopies(tags, 9, "ROUTE 24");
        addCopies(tags, 9, "ROUTE 25");
        addCopies(tags, 9, "TOHJO FALLS");
        addCopies(tags, 3, "DIGLETT'S CAVE");
        addCopies(tags, 7, "VICTORY ROAD");
        addCopies(tags, 3, "ROUTE 2");
        addCopies(tags, 3, "VIRIDIAN FOREST");
        addCopies(tags, 29, "CERULEAN CAVE");
        // headbutt
        tags.addAll(Arrays.asList("ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7",
                "ROUTE 8", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 18", "ROUTE 22",
                "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28", "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32",
                "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37", "ROUTE 38", "ROUTE 39", "ROUTE 42",
                "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46", "PALLET TOWN", "VIRIDIAN CITY", "PEWTER CITY",
                "CERULEAN CITY", "VERMILION CITY", "CELADON CITY", "FUCHSIA CITY", "NEW BARK TOWN", "CHERRYGROVE CITY",
                "VIOLET CITY", "AZALEA TOWN", "ECRUTEAK CITY", "LAKE OF RAGE", "MT. SILVER", "ROUTE 21",
                "NATIONAL PARK", "ILEX FOREST", "VIRIDIAN FOREST", "ROUTE 47", "ROUTE 48", "SAFARI ZONE", "ROUTE 2",
                "ROUTE 16", "MT. SILVER CAVE"));
        addCopies(tags, 4, "BUG CATCHING CONTEST");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> locationTagsUseTimeHGSS = initLocationTagsUseTimeHGSS();

    private static List<String> initLocationTagsUseTimeHGSS() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 7, "NEW BARK TOWN");
        addCopies(tags, 5, "ROUTE 29");
        addCopies(tags, 7, "CHERRYGROVE CITY");
        addCopies(tags, 11, "ROUTE 30");
        addCopies(tags, 11, "ROUTE 31");
        addCopies(tags, 7, "VIOLET CITY");
        addCopies(tags, 10, "SPROUT TOWER");
        addCopies(tags, 11, "ROUTE 32");
        addCopies(tags, 32, "RUINS OF ALPH");
        addCopies(tags, 33, "UNION CAVE");
        addCopies(tags, 5, "ROUTE 33");
        addCopies(tags, 22, "SLOWPOKE WELL");
        addCopies(tags, 11, "ILEX FOREST");
        addCopies(tags, 11, "ROUTE 34");
        addCopies(tags, 11, "ROUTE 35");
        addCopies(tags, 10, "NATIONAL PARK");
        addCopies(tags, 5, "ROUTE 36");
        addCopies(tags, 5, "ROUTE 37");
        addCopies(tags, 7, "ECRUTEAK CITY");
        addCopies(tags, 10, "BURNED TOWER");
        addCopies(tags, 40, "BELL TOWER");
        addCopies(tags, 5, "ROUTE 38");
        addCopies(tags, 5, "ROUTE 39");
        addCopies(tags, 7, "OLIVINE CITY");
        addCopies(tags, 7, "ROUTE 40");
        addCopies(tags, 7, "ROUTE 41");
        addCopies(tags, 32, "WHIRL ISLANDS");
        addCopies(tags, 8, "CIANWOOD CITY");
        addCopies(tags, 11, "ROUTE 42");
        addCopies(tags, 38, "MT. MORTAR");
        addCopies(tags, 11, "ROUTE 43");
        addCopies(tags, 7, "LAKE OF RAGE");
        addCopies(tags, 11, "ROUTE 44");
        addCopies(tags, 20, "ICE PATH");
        addCopies(tags, 7, "BLACKTHORN CITY");
        addCopies(tags, 7, "DRAGON'S DEN");
        addCopies(tags, 11, "ROUTE 45");
        addCopies(tags, 5, "ROUTE 46");
        addCopies(tags, 23, "DARK CAVE");
        addCopies(tags, 11, "ROUTE 47");
        addCopies(tags, 14, "MT. MOON");
        addCopies(tags, 31, "SEAFOAM ISLANDS");
        addCopies(tags, 33, "MT. SILVER CAVE");
        addCopies(tags, 13, "CLIFF EDGE GATE");
        addCopies(tags, 5, "BELL TOWER");
        addCopies(tags, 11, "MT. SILVER");
        addCopies(tags, 31, "MT. SILVER CAVE");
        addCopies(tags, 11, "SAFARI ZONE");
        addCopies(tags, 7, "ROUTE 12");
        addCopies(tags, 8, "ROUTE 19");
        addCopies(tags, 7, "ROUTE 20");
        addCopies(tags, 7, "PALLET TOWN");
        addCopies(tags, 7, "VIRIDIAN CITY");
        addCopies(tags, 7, "CERULEAN CITY");
        addCopies(tags, 8, "VERMILION CITY");
        addCopies(tags, 2, "CELADON CITY");
        addCopies(tags, 8, "FUCHSIA CITY");
        addCopies(tags, 7, "CINNABAR ISLAND");
        addCopies(tags, 5, "ROUTE 48");
        addCopies(tags, 11, "ROUTE 26");
        addCopies(tags, 11, "ROUTE 27");
        addCopies(tags, 11, "ROUTE 28");
        addCopies(tags, 10, "MT. MOON");
        addCopies(tags, 11, "ROCK TUNNEL");
        addCopies(tags, 5, "VICTORY ROAD");
        addCopies(tags, 5, "ROUTE 1");
        addCopies(tags, 5, "ROUTE 2");
        addCopies(tags, 5, "ROUTE 3");
        addCopies(tags, 11, "ROUTE 4");
        addCopies(tags, 5, "ROUTE 5");
        addCopies(tags, 11, "ROUTE 6");
        addCopies(tags, 5, "ROUTE 7");
        addCopies(tags, 5, "ROUTE 8");
        addCopies(tags, 11, "ROUTE 9");
        addCopies(tags, 11, "ROUTE 10");
        addCopies(tags, 5, "ROUTE 11");
        addCopies(tags, 11, "ROUTE 13");
        addCopies(tags, 5, "ROUTE 14");
        addCopies(tags, 5, "ROUTE 15");
        addCopies(tags, 5, "ROUTE 16");
        addCopies(tags, 5, "ROUTE 17");
        addCopies(tags, 5, "ROUTE 18");
        addCopies(tags, 11, "ROUTE 21");
        addCopies(tags, 11, "ROUTE 22");
        addCopies(tags, 11, "ROUTE 24");
        addCopies(tags, 11, "ROUTE 25");
        addCopies(tags, 11, "TOHJO FALLS");
        addCopies(tags, 5, "DIGLETT'S CAVE");
        addCopies(tags, 11, "VICTORY ROAD");
        addCopies(tags, 5, "ROUTE 2");
        addCopies(tags, 5, "VIRIDIAN FOREST");
        addCopies(tags, 35, "CERULEAN CAVE");
        // headbutt
        tags.addAll(Arrays.asList("ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7",
                "ROUTE 8", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 18", "ROUTE 22",
                "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28", "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32",
                "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37", "ROUTE 38", "ROUTE 39", "ROUTE 42",
                "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46", "PALLET TOWN", "VIRIDIAN CITY", "PEWTER CITY",
                "CERULEAN CITY", "VERMILION CITY", "CELADON CITY", "FUCHSIA CITY", "NEW BARK TOWN", "CHERRYGROVE CITY",
                "VIOLET CITY", "AZALEA TOWN", "ECRUTEAK CITY", "LAKE OF RAGE", "MT. SILVER", "ROUTE 21",
                "NATIONAL PARK", "ILEX FOREST", "VIRIDIAN FOREST", "ROUTE 47", "ROUTE 48", "SAFARI ZONE", "ROUTE 2",
                "ROUTE 16", "MT. SILVER CAVE"));
        addCopies(tags, 4, "BUG CATCHING CONTEST");
        return Collections.unmodifiableList(tags);
    }

    private static final TagPackMap Gen4TagPackMap = new Builder()
            .newPack(Gen4Constants.Type_DP, locationTagsDP)
                .postGameAreas(dpPostGameEncounterAreas)
            .newPack(Gen4Constants.Type_Plat, locationTagsPt)
                .postGameAreas(platPostGameEncounterAreas)
            .newPack(Gen4Constants.Type_HGSS, locationTagsNoTimeHGSS, locationTagsUseTimeHGSS)
                .postGameAreas(hgssPostGameEncounterAreasNoTOD, hgssPostGameEncounterAreasTOD)
                .partialPostGameAreas(hgssPartialPostGameNoTOD, hgssPartialPostGameTOD)
                .partialPostGameCutoff(headbuttPartialPostgameCutoff)
            .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen4TagPackMap;
    }

}
