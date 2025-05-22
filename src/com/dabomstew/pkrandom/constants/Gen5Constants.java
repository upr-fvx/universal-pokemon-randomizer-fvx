package com.dabomstew.pkrandom.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen5Constants.java - Constants for Black/White/Black 2/White 2        --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.gamedata.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gen5Constants {

    public static final int Type_BW = 0;
    public static final int Type_BW2 = 1;

    public static final int arm9Offset = 0x02004000;

    public static final int pokemonCount = 649, moveCount = 559;
    private static final int bw1FormeCount = 18, bw2FormeCount = 24;
    private static final int bw1formeOffset = 0, bw2formeOffset = 35;

    private static final int bw1NonPokemonBattleSpriteCount = 3;
    private static final int bw2NonPokemonBattleSpriteCount = 36;

    public static final int bsHPOffset = 0, bsAttackOffset = 1, bsDefenseOffset = 2, bsSpeedOffset = 3,
            bsSpAtkOffset = 4, bsSpDefOffset = 5, bsPrimaryTypeOffset = 6, bsSecondaryTypeOffset = 7,
            bsCatchRateOffset = 8, bsCommonHeldItemOffset = 12, bsRareHeldItemOffset = 14,
            bsDarkGrassHeldItemOffset = 16, bsGenderRatioOffset = 18, bsGrowthCurveOffset = 21,
            bsAbility1Offset = 24, bsAbility2Offset = 25, bsAbility3Offset = 26,
            bsFormeOffset = 28, bsFormeSpriteOffset = 30, bsFormeCountOffset = 32,
            bsTMHMCompatOffset = 40, bsMTCompatOffset = 60;

    public static final byte[] bw1NewStarterScript = { 0x24, 0x00, (byte) 0xA7, 0x02, (byte) 0xE7, 0x00, 0x00, 0x00,
            (byte) 0xDE, 0x00, 0x00, 0x00, (byte) 0xF8, 0x01, 0x05, 0x00 };

    public static final String bw1StarterScriptMagic = "2400A702";

    public static final int bw1StarterTextOffset = 18, bw1CherenText1Offset = 26, bw1CherenText2Offset = 53;

    public static final byte[] bw2NewStarterScript = { 0x28, 0x00, (byte) 0xA1, 0x40, 0x04, 0x00, (byte) 0xDE, 0x00,
            0x00, 0x00, (byte) 0xFD, 0x01, 0x05, 0x00 };

    public static final String bw2StarterScriptMagic = "2800A1400400";

    public static final int bw2StarterTextOffset = 37, bw2RivalTextOffset = 60;

    public static final int perSeasonEncounterDataLength = 232;
    private static final int bw1AreaDataEntryLength = 249, bw2AreaDataEntryLength = 345, bw1EncounterAreaCount = 61, bw2EncounterAreaCount = 85;

    public static final int[] encountersOfEachType = { 12, 12, 12, 5, 5, 5, 5 };

    public static final String[] encounterTypeNames = { "Grass/Cave", "Doubles Grass", "Shaking Spots", "Surfing",
            "Surfing Spots", "Fishing", "Fishing Spots" };

    public static final EncounterType[] encounterTypeValues = {EncounterType.WALKING, //Grass/Cave
            EncounterType.WALKING, //Dark grass
            EncounterType.AMBUSH, //Shaking Grass
            EncounterType.SURFING, //Surfing
            EncounterType.AMBUSH, //Surfing Spots
            EncounterType.FISHING, //Fishing
            EncounterType.AMBUSH //Fishing spots
    };

    public static final int[] habitatClassificationOfEachType = { 0, 0, 0, 1, 1, 2, 2 };

    public static final int bw2Route4AreaIndex = 40, bw2VictoryRoadAreaIndex = 76, bw2ReversalMountainAreaIndex = 73;

    public static final int b2Route4EncounterFile = 104, b2VRExclusiveRoom1 = 71, b2VRExclusiveRoom2 = 73,
            b2ReversalMountainStart = 49, b2ReversalMountainEnd = 54;

    public static final int w2Route4EncounterFile = 105, w2VRExclusiveRoom1 = 78, w2VRExclusiveRoom2 = 79,
            w2ReversalMountainStart = 55, w2ReversalMountainEnd = 60;

    public static final List<Integer> bw2HiddenHollowUnovaPokemon = Arrays.asList(SpeciesIDs.watchog, SpeciesIDs.herdier, SpeciesIDs.liepard,
            SpeciesIDs.pansage, SpeciesIDs.pansear, SpeciesIDs.panpour, SpeciesIDs.pidove, SpeciesIDs.zebstrika, SpeciesIDs.boldore,
            SpeciesIDs.woobat, SpeciesIDs.drilbur, SpeciesIDs.audino, SpeciesIDs.gurdurr, SpeciesIDs.tympole, SpeciesIDs.throh,
            SpeciesIDs.sawk, SpeciesIDs.leavanny, SpeciesIDs.scolipede, SpeciesIDs.cottonee, SpeciesIDs.petilil, SpeciesIDs.basculin,
            SpeciesIDs.krookodile, SpeciesIDs.maractus, SpeciesIDs.crustle, SpeciesIDs.scraggy, SpeciesIDs.sigilyph, SpeciesIDs.tirtouga,
            SpeciesIDs.garbodor, SpeciesIDs.minccino, SpeciesIDs.gothorita, SpeciesIDs.duosion, SpeciesIDs.ducklett, SpeciesIDs.vanillish,
            SpeciesIDs.emolga, SpeciesIDs.karrablast, SpeciesIDs.alomomola, SpeciesIDs.galvantula, SpeciesIDs.klinklang, SpeciesIDs.elgyem,
            SpeciesIDs.litwick, SpeciesIDs.axew, SpeciesIDs.cubchoo, SpeciesIDs.shelmet, SpeciesIDs.stunfisk, SpeciesIDs.mienfoo,
            SpeciesIDs.druddigon, SpeciesIDs.golett, SpeciesIDs.pawniard, SpeciesIDs.bouffalant, SpeciesIDs.braviary, SpeciesIDs.mandibuzz,
            SpeciesIDs.heatmor, SpeciesIDs.durant);

    public static final String tmDataPrefix = "87038803";

    public static final int tmCount = 95, hmCount = 6, tmBlockOneCount = 92, tmBlockOneOffset = ItemIDs.tm01,
            tmBlockTwoOffset = ItemIDs.tm93;

    public static final String bw1ItemPalettesPrefix = "E903EA03020003000400050006000700",
            bw2ItemPalettesPrefix = "FD03FE03020003000400050006000700";

    public static final int bw2MoveTutorCount = 60, bw2MoveTutorBytesPerEntry = 12;

    public static final int evolutionMethodCount = 27;

    public static final int highestAbilityIndex = AbilityIDs.teravolt;

    public static final int fossilPokemonFile = 877;
    public static final int fossilPokemonLevelOffset = 0x3F7;

    // It is intentional that "after" is 4 bytes longer that "before".
    // 2 of those last 4 bytes in "before" vary between games, so we don't want to search for
    // them/confirm they're there. Still, they need to be overwritten by "after".
    public static final String hmsForgettableBefore = "08 4A 00 23 59 00 51 18 B8 31 09 88 88 42 01 D1 01 20 70 47 59 1C 09 06 0B 0E 06 2B F2 D3 00 20 70 47 C0 46",
            hmsForgettableAfter = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 70 47 00 00 00 00 00 00";

    public static final Map<Integer,List<Integer>> abilityVariations = setupAbilityVariations();

    private static Map<Integer,List<Integer>> setupAbilityVariations() {
        Map<Integer,List<Integer>> map = new HashMap<>();
        map.put(AbilityIDs.insomnia, Arrays.asList(AbilityIDs.insomnia, AbilityIDs.vitalSpirit));
        map.put(AbilityIDs.clearBody, Arrays.asList(AbilityIDs.clearBody, AbilityIDs.whiteSmoke));
        map.put(AbilityIDs.hugePower, Arrays.asList(AbilityIDs.hugePower, AbilityIDs.purePower));
        map.put(AbilityIDs.battleArmor, Arrays.asList(AbilityIDs.battleArmor, AbilityIDs.shellArmor));
        map.put(AbilityIDs.cloudNine, Arrays.asList(AbilityIDs.cloudNine, AbilityIDs.airLock));
        map.put(AbilityIDs.filter, Arrays.asList(AbilityIDs.filter, AbilityIDs.solidRock));
        map.put(AbilityIDs.roughSkin, Arrays.asList(AbilityIDs.roughSkin, AbilityIDs.ironBarbs));
        map.put(AbilityIDs.moldBreaker, Arrays.asList(AbilityIDs.moldBreaker, AbilityIDs.turboblaze, AbilityIDs.teravolt));

        return map;
    }

    public static final List<Integer> uselessAbilities = Arrays.asList(AbilityIDs.forecast, AbilityIDs.multitype,
            AbilityIDs.flowerGift, AbilityIDs.zenMode);

    public static final int normalItemSetVarCommand = 0x28, hiddenItemSetVarCommand = 0x2A, normalItemVarSet = 0x800C,
            hiddenItemVarSet = 0x8000;

    public static final int scriptListTerminator = 0xFD13;

    public static final MoveCategory[] moveCategoryIndices = { MoveCategory.STATUS, MoveCategory.PHYSICAL,
            MoveCategory.SPECIAL };

    public static byte moveCategoryToByte(MoveCategory cat) {
        switch (cat) {
        case PHYSICAL:
            return 1;
        case SPECIAL:
            return 2;
        case STATUS:
        default:
            return 0;
        }
    }

    public static final int trappingEffect = 106;

    public static final int noDamageStatusQuality = 1, noDamageStatChangeQuality = 2, damageStatusQuality = 4,
            noDamageStatusAndStatChangeQuality = 5, damageTargetDebuffQuality = 6, damageUserBuffQuality = 7,
            damageAbsorbQuality = 8;

    public static final Type[] typeTable = constructTypeTable();

    private static final Map<Integer,Map<Integer,String>> formeSuffixesByBaseForme = setupFormeSuffixesByBaseForme();
    private static final Map<Integer,String> dummyFormeSuffixes = setupDummyFormeSuffixes();

    private static final Map<Integer,Map<Integer,Integer>> absolutePokeNumsByBaseForme = setupAbsolutePokeNumsByBaseForme();
    private static final Map<Integer,Integer> dummyAbsolutePokeNums = setupDummyAbsolutePokeNums();

    public static String getFormeSuffixByBaseForme(int baseForme, int formNum) {
        return formeSuffixesByBaseForme.getOrDefault(baseForme,dummyFormeSuffixes).getOrDefault(formNum,"");
    }

    public static Integer getAbsolutePokeNumByBaseForme(int baseForme, int formNum) {
        return absolutePokeNumsByBaseForme.getOrDefault(baseForme,dummyAbsolutePokeNums).getOrDefault(formNum,baseForme);
    }

    private static final List<Integer> bw1IrregularFormes = Arrays.asList(
            SpeciesIDs.Gen5Formes.castformSu, SpeciesIDs.Gen5Formes.castformR, SpeciesIDs.Gen5Formes.castformSn,
            SpeciesIDs.Gen5Formes.darmanitanZ,
            SpeciesIDs.Gen5Formes.meloettaP
    );

    private static final List<Integer> bw2IrregularFormes = Arrays.asList(
            SpeciesIDs.Gen5Formes.castformSu, SpeciesIDs.Gen5Formes.castformR, SpeciesIDs.Gen5Formes.castformSn,
            SpeciesIDs.Gen5Formes.darmanitanZ,
            SpeciesIDs.Gen5Formes.meloettaP,
            SpeciesIDs.Gen5Formes.kyuremW,
            SpeciesIDs.Gen5Formes.kyuremB
    );

    public static final List<Integer> emptyPlaythroughTrainers = Collections.emptyList();

    public static final List<Integer> bw1MainPlaythroughTrainers = Arrays.asList(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
            40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
            62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 79,
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 93, 94, 95, 96, 97, 98,
            99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118,
            119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 137, 138,
            139, 140, 141, 142, 143, 144, 145, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162,
            163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181,
            182, 183, 184, 186, 187, 188, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203,
            204, 212, 213, 214, 215, 216, 217, 218, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230,
            231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250,
            251, 252, 253, 254, 255, 256, 257, 258, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273,
            274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 290, 291, 292, 293,
            294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312,
            313, 315, 316, 401, 402, 408, 409, 412, 413, 438, 439, 441, 442, 443, 445, 447, 450,
            460, 461, 462, 465, 466, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481,
            484, 485, 488, 489, 490, 501, 502, 503, 504, 505, 506,
            513, 514, 515, 516, 517, 518, 519, 520, 526, 531, 532, 533, 534, 535, 536, 537,
            538, 544, 545, 546, 549, 550, 552, 553, 554, 555, 556, 557, 582, 583, 584, 585, 586,
            587, 600, 601, 602, 603, 604, 605, 606, 607, 610, 611, 612, 613);

    public static final List<Integer> bw2MainPlaythroughTrainers = Arrays.asList(
            4, 5, 6, 133, 134, 135, 136, 137, 138, 139, 147, 148, 149,
            150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160,
            164, 165, 169, 170, 171, 172, 173, 174, 175, 176, 177,
            178, 179, 180, 181, 182, 203, 204, 205, 206, 207, 208, 209, 210, 211,
            212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225,
            226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 237, 238, 239, 240,
            242, 243, 244, 245, 247, 248, 249, 250, 252, 253, 254, 255, 256, 257,
            258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271,
            272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285,
            286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299,
            300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313,
            314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327,
            328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341,
            342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355,
            356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367,
            372, 373, 374, 375, 376, 377, 381, 382, 383,
            384, 385, 386, 387, 388, 389, 390, 391, 392, 426, 427, 428, 429, 430,
            431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 444,
            445, 446, 447, 448, 449, 450, 451, 452, 453, 454, 455, 461, 462, 463,
            464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477,
            478, 479, 480, 481, 482, 483, 484, 485, 486, 497, 498, 499, 500, 501,
            502, 503, 510, 511, 512, 513, 514, 515, 516, 517, 518, 519, 520, 521,
            522, 523, 524, 537, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547,
            548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561,
            562, 563, 564, 565, 566, 567, 568, 569, 570, 580, 581, 583, 584, 585,
            586, 587, 592, 593, 594, 595, 596, 597, 598, 599, 600,
            601, 602, 603, 604, 605, 606, 607, 608, 609, 610, 611, 612, 613, 614,
            615, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 631, 657, 658,
            659, 660, 661, 662, 663, 664, 665, 666, 667, 668, 669, 670, 671, 672,
            673, 679, 680, 681, 682, 683, 690, 691, 692, 703, 704,
            705, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724,
            725, 726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 736, 737, 738,
            745, 746, 747, 748, 749, 750, 751, 752, 754, 755, 756, 763, 764, 765,
            766, 767, 768, 769, 770, 771, 772, 773, 774, 775, 776, 786, 787, 788,
            789, 797, 798, 799, 800, 801, 802, 803, 804, 805, 806,
            807, 808, 809, 810, 811, 812);

    public static final List<Integer> bw2DriftveilTrainerOffsets = Arrays.asList(56, 57, 0, 1, 2, 3, 4, 68, 69, 70,
            71, 72, 73, 74, 75, 76, 77);

    public static final int normalTrainerNameLength = 813, normalTrainerClassLength = 236;

//    public static final Map<Integer, String> bw1ShopIndex = new HashMap<Integer, String>() {1:"Check"};

    public static final List<Integer> bw1MainGameShops = Arrays.asList(
            3, 5, 6, 8, 9, 12, 14, 17, 18, 19, 21, 22
    );

    public static final List<String> bw1ShopNames = Arrays.asList(
            "Primary 0 Badges",
            "Primary 2 Badges",
            "Primary 3 Badges",
            "Primary 5 Badges",
            "Primary 7 Badges",
            "Primary 8 Badges",
            "Accumula Secondary",
            "Striaton Secondary",
            "Nacrene Secondary",
            "Castelia Secondary",
            "Nimbasa Secondary (TMs)",
            "Driftveil Secondary",
            "Mistralton Secondary (TMs)",
            "Icirrus Secondary (TMs)",
            "Opelucid Secondary",
            "League Secondary",
            "Lacunosa Secondary",
            "Undella Secondary",
            "Black City/White Forest Secondary",
            "Nacrene/Shopping Mall 9 X Items",
            "Driftveil Herb Salesman",
            "Driftveil Incense Salesman",
            "Shopping Mall 9 F1",
            "Shopping Mall 9 TMs",
            "Shopping Mall 9 F2",
            "Shopping Mall 9 F3 Left");

    public static final List<Integer> bw2MainGameShops = Arrays.asList(
            9, 11, 14, 15, 16, 18, 20, 21, 22, 23, 25, 26, 27, 28, 29, 30, 31
    );

    public static final List<String> bw2ShopNames = Arrays.asList(
            "Primary 0 Badges",
            "Primary 1 Badges",
            "Primary 3 Badges",
            "Primary 5 Badges",
            "Primary 7 Badges",
            "Primary 8 Badges",
            "Accumula Secondary",
            "Striaton Secondary (TMs)",
            "Nacrene Secondary",
            "Castelia Secondary",
            "Nimbasa Secondary (TMs)",
            "Driftveil Secondary",
            "Mistralton Secondary (TMs)",
            "Icirrus Secondary",
            "Opelucid Secondary",
            "Victory Road Secondary",
            "Pokemon League Secondary",
            "Lacunosa Secondary (TMs)",
            "Undella Secondary",
            "Black City/White Forest Secondary",
            "Nacrene/Shopping Mall 9 X Items",
            "Driftveil Herb Salesman",
            "Driftveil Incense Salesman",
            "Shopping Mall 9 F1",
            "Shopping Mall 9 TMs",
            "Shopping Mall 9 F2",
            "Shopping Mall 9 F3 Left",
            "Aspertia Secondary",
            "Virbank Secondary",
            "Humilau Secondary",
            "Floccesy Secondary",
            "Lentimas Secondary");


    public static final List<Integer> evolutionItems = Arrays.asList(ItemIDs.sunStone, ItemIDs.moonStone, ItemIDs.fireStone,
            ItemIDs.thunderStone, ItemIDs.waterStone, ItemIDs.leafStone, ItemIDs.shinyStone, ItemIDs.duskStone, ItemIDs.dawnStone,
            ItemIDs.ovalStone, ItemIDs.kingsRock, ItemIDs.deepSeaTooth, ItemIDs.deepSeaScale, ItemIDs.metalCoat, ItemIDs.dragonScale,
            ItemIDs.upgrade, ItemIDs.protector, ItemIDs.electirizer, ItemIDs.magmarizer, ItemIDs.dubiousDisc, ItemIDs.reaperCloth,
            ItemIDs.razorClaw, ItemIDs.razorFang, ItemIDs.prismScale);

    public static final List<Integer> bw1RequiredFieldTMs = Arrays.asList(ItemIDs.tm02, ItemIDs.tm03, ItemIDs.tm05,
            ItemIDs.tm06, ItemIDs.tm09, ItemIDs.tm12, ItemIDs.tm13, ItemIDs.tm19, ItemIDs.tm22, ItemIDs.tm24,
            ItemIDs.tm26, ItemIDs.tm29, ItemIDs.tm30, ItemIDs.tm35, ItemIDs.tm36, ItemIDs.tm39, ItemIDs.tm41,
            ItemIDs.tm46, ItemIDs.tm47, ItemIDs.tm50, ItemIDs.tm52, ItemIDs.tm53, ItemIDs.tm55, ItemIDs.tm58,
            ItemIDs.tm61, ItemIDs.tm63, ItemIDs.tm65, ItemIDs.tm66, ItemIDs.tm71, ItemIDs.tm80, ItemIDs.tm81,
            ItemIDs.tm84, ItemIDs.tm85, ItemIDs.tm86, ItemIDs.tm90, ItemIDs.tm91, ItemIDs.tm92, ItemIDs.tm93);

    public static final List<Integer> bw2RequiredFieldTMs = Arrays.asList(ItemIDs.tm01, ItemIDs.tm02, ItemIDs.tm03,
            ItemIDs.tm05, ItemIDs.tm06, ItemIDs.tm12, ItemIDs.tm13, ItemIDs.tm19, ItemIDs.tm22, ItemIDs.tm26,
            ItemIDs.tm28, ItemIDs.tm29, ItemIDs.tm30, ItemIDs.tm36, ItemIDs.tm39, ItemIDs.tm41, ItemIDs.tm46,
            ItemIDs.tm47, ItemIDs.tm50, ItemIDs.tm52, ItemIDs.tm53, ItemIDs.tm56, ItemIDs.tm58, ItemIDs.tm61,
            ItemIDs.tm63, ItemIDs.tm65, ItemIDs.tm66, ItemIDs.tm67, ItemIDs.tm69, ItemIDs.tm71, ItemIDs.tm80,
            ItemIDs.tm81, ItemIDs.tm84, ItemIDs.tm85, ItemIDs.tm86, ItemIDs.tm90, ItemIDs.tm91, ItemIDs.tm92,
            ItemIDs.tm93);

    public static final List<Integer> bw1EarlyRequiredHMMoves = Collections.singletonList(MoveIDs.cut);

    public static final List<Integer> bw2EarlyRequiredHMMoves = Collections.emptyList();

    public static final List<Integer> fieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.waterfall, MoveIDs.sweetScent, MoveIDs.dive);

    public static final String shedinjaSpeciesLocator = "24010000";

    public static final String runningShoesPrefix = "01D0012008BD002008BD63";

    public static final String introGraphicPrefix = "5A0000010000001700000001000000", bw1IntroCryPrefix = "0021009101910291", bw2IntroCryLocator = "3D020000F8B51C1C";

    public static final String typeEffectivenessTableLocator = "0404040404020400";

    public static final String forceChallengeModeLocator = "816A406B0B1C07490022434090000858834201D1";

    public static final String pickupTableLocator = "19005C00DD00";
    public static final int numberOfPickupItems = 29;

    public static final String friendshipValueForEvoLocator = "DC282FD3";

    public static final String perfectOddsBranchLocator = "08DB002801D0012000E0";

    public static final String lowHealthMusicLocator = "00D10127";

    public static final List<Integer> consumableHeldItems = setupAllConsumableItems();

    private static List<Integer> setupAllConsumableItems() {
        List<Integer> list = new ArrayList<>(Gen4Constants.consumableHeldItems);
        list.addAll(Arrays.asList(ItemIDs.airBalloon, ItemIDs.redCard, ItemIDs.absorbBulb, ItemIDs.cellBattery,
                ItemIDs.ejectButton, ItemIDs.fireGem, ItemIDs.waterGem, ItemIDs.electricGem, ItemIDs.grassGem, ItemIDs.iceGem,
                ItemIDs.fightingGem, ItemIDs.poisonGem, ItemIDs.groundGem, ItemIDs.flyingGem, ItemIDs.psychicGem, ItemIDs.bugGem,
                ItemIDs.rockGem, ItemIDs.ghostGem, ItemIDs.dragonGem, ItemIDs.darkGem, ItemIDs.steelGem, ItemIDs.normalGem));
        return Collections.unmodifiableList(list);
    }

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        List<Integer> list = new ArrayList<>(Gen4Constants.allHeldItems);
        list.addAll(Arrays.asList(ItemIDs.airBalloon, ItemIDs.redCard, ItemIDs.absorbBulb, ItemIDs.cellBattery,
                ItemIDs.ejectButton, ItemIDs.fireGem, ItemIDs.waterGem, ItemIDs.electricGem, ItemIDs.grassGem, ItemIDs.iceGem,
                ItemIDs.fightingGem, ItemIDs.poisonGem, ItemIDs.groundGem, ItemIDs.flyingGem, ItemIDs.psychicGem, ItemIDs.bugGem,
                ItemIDs.rockGem, ItemIDs.ghostGem, ItemIDs.dragonGem, ItemIDs.darkGem, ItemIDs.steelGem, ItemIDs.normalGem));
        list.addAll(Arrays.asList(ItemIDs.eviolite, ItemIDs.floatStone, ItemIDs.rockyHelmet, ItemIDs.ringTarget, ItemIDs.bindingBand));
        return Collections.unmodifiableList(list);
    }

    public static final List<Integer> generalPurposeConsumableItems = initializeGeneralPurposeConsumableItems();

    private static List<Integer> initializeGeneralPurposeConsumableItems() {
        List<Integer> list = new ArrayList<>(Gen4Constants.generalPurposeConsumableItems);
        list.addAll(Arrays.asList(ItemIDs.redCard, ItemIDs.absorbBulb, ItemIDs.cellBattery, ItemIDs.ejectButton));
        return Collections.unmodifiableList(list);
    }

    public static final List<Integer> generalPurposeItems = initializeGeneralPurposeItems();

    private static List<Integer> initializeGeneralPurposeItems() {
        List<Integer> list = new ArrayList<>(Gen4Constants.generalPurposeItems);
        list.addAll(Arrays.asList(ItemIDs.floatStone, ItemIDs.rockyHelmet));
        return Collections.unmodifiableList(list);
    }

    public static final Map<Type, Integer> consumableTypeBoostingItems = initializeConsumableTypeBoostingItems();

    private static Map<Type, Integer> initializeConsumableTypeBoostingItems() {
        Map<Type, Integer> map = new HashMap<>();
        map.put(Type.FIRE, ItemIDs.fireGem);
        map.put(Type.WATER, ItemIDs.waterGem);
        map.put(Type.ELECTRIC, ItemIDs.electricGem);
        map.put(Type.GRASS, ItemIDs.grassGem);
        map.put(Type.ICE, ItemIDs.iceGem);
        map.put(Type.FIGHTING, ItemIDs.fightingGem);
        map.put(Type.POISON, ItemIDs.poisonGem);
        map.put(Type.GROUND, ItemIDs.groundGem);
        map.put(Type.FLYING, ItemIDs.flyingGem);
        map.put(Type.PSYCHIC, ItemIDs.psychicGem);
        map.put(Type.BUG, ItemIDs.bugGem);
        map.put(Type.ROCK, ItemIDs.rockGem);
        map.put(Type.GHOST, ItemIDs.ghostGem);
        map.put(Type.DRAGON, ItemIDs.dragonGem);
        map.put(Type.DARK, ItemIDs.darkGem);
        map.put(Type.STEEL, ItemIDs.steelGem);
        map.put(Type.NORMAL, ItemIDs.normalGem);
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> moveBoostingItems = initializeMoveBoostingItems();

    private static Map<Integer, List<Integer>> initializeMoveBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>(Gen4Constants.moveBoostingItems);
        map.put(MoveIDs.trick, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb, ItemIDs.ringTarget));
        map.put(MoveIDs.switcheroo, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb, ItemIDs.ringTarget));

        map.put(MoveIDs.bind, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.clamp, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.fireSpin, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.magmaStorm, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.sandTomb, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.whirlpool, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.wrap, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));

        map.put(MoveIDs.hornLeech, Collections.singletonList(ItemIDs.bigRoot));
        return Collections.unmodifiableMap(map);
    }

    // None of these have new entries in Gen V.
    public static final Map<Integer, List<Integer>> abilityBoostingItems = Gen4Constants.abilityBoostingItems;
    public static final Map<Integer, List<Integer>> speciesBoostingItems = Gen4Constants.speciesBoostingItems;
    public static final Map<Type, List<Integer>> typeBoostingItems = Gen4Constants.typeBoostingItems;
    public static final Map<Type, Integer> weaknessReducingBerries = Gen4Constants.weaknessReducingBerries;

    private static Type[] constructTypeTable() {
        Type[] table = new Type[0x11];
        table[0x00] = Type.NORMAL;
        table[0x01] = Type.FIGHTING;
        table[0x02] = Type.FLYING;
        table[0x03] = Type.POISON;
        table[0x04] = Type.GROUND;
        table[0x05] = Type.ROCK;
        table[0x06] = Type.BUG;
        table[0x07] = Type.GHOST;
        table[0x08] = Type.STEEL;
        table[0x09] = Type.FIRE;
        table[0x0A] = Type.WATER;
        table[0x0B] = Type.GRASS;
        table[0x0C] = Type.ELECTRIC;
        table[0x0D] = Type.PSYCHIC;
        table[0x0E] = Type.ICE;
        table[0x0F] = Type.DRAGON;
        table[0x10] = Type.DARK;
        return table;
    }

    public static byte typeToByte(Type type) {
        if (type == null) {
            return 0x00; // normal?
        }
        switch (type) {
        case NORMAL:
            return 0x00;
        case FIGHTING:
            return 0x01;
        case FLYING:
            return 0x02;
        case POISON:
            return 0x03;
        case GROUND:
            return 0x04;
        case ROCK:
            return 0x05;
        case BUG:
            return 0x06;
        case GHOST:
            return 0x07;
        case FIRE:
            return 0x09;
        case WATER:
            return 0x0A;
        case GRASS:
            return 0x0B;
        case ELECTRIC:
            return 0x0C;
        case PSYCHIC:
            return 0x0D;
        case ICE:
            return 0x0E;
        case DRAGON:
            return 0x0F;
        case STEEL:
            return 0x08;
        case DARK:
            return 0x10;
        default:
            return 0; // normal by default
        }
    }

    private static final EvolutionType[] evolutionTypeTable = new EvolutionType[] {
            EvolutionType.HAPPINESS, EvolutionType.HAPPINESS_DAY, EvolutionType.HAPPINESS_NIGHT, EvolutionType.LEVEL,
            EvolutionType.TRADE, EvolutionType.TRADE_ITEM, EvolutionType.TRADE_SPECIAL, EvolutionType.STONE,
            EvolutionType.LEVEL_ATTACK_HIGHER, EvolutionType.LEVEL_ATK_DEF_SAME, EvolutionType.LEVEL_DEFENSE_HIGHER,
            EvolutionType.LEVEL_LOW_PV, EvolutionType.LEVEL_HIGH_PV, EvolutionType.LEVEL_CREATE_EXTRA,
            EvolutionType.LEVEL_IS_EXTRA, EvolutionType.LEVEL_HIGH_BEAUTY, EvolutionType.STONE_MALE_ONLY,
            EvolutionType.STONE_FEMALE_ONLY, EvolutionType.LEVEL_ITEM_DAY, EvolutionType.LEVEL_ITEM_NIGHT,
            EvolutionType.LEVEL_WITH_MOVE, EvolutionType.LEVEL_WITH_OTHER, EvolutionType.LEVEL_MALE_ONLY,
            EvolutionType.LEVEL_FEMALE_ONLY, EvolutionType.LEVEL_MAGNETIC_FIELD, EvolutionType.LEVEL_MOSS_ROCK,
            EvolutionType.LEVEL_ICE_ROCK
    };

    public static int evolutionTypeToIndex(EvolutionType evolutionType) {
        for (int i = 0; i < evolutionTypeTable.length; i++) {
            if (evolutionType == evolutionTypeTable[i]) {
                return i + 1;
            }
        }
        return -1;
    }

    public static EvolutionType evolutionTypeFromIndex(int index) {
        if (index == -1) {
            return EvolutionType.NONE;
        }
        return evolutionTypeTable[index - 1];
    }

    public static int getMapIndexForLocationEvolution(EvolutionType et, int romType) {
        switch (et) {
            case LEVEL_MAGNETIC_FIELD:
                return romType == Type_BW ? 41 : 20; // Chargestone Cave
            case LEVEL_MOSS_ROCK:
                return romType == Type_BW ? 5 : 8; // Pinwheel Forest
            case LEVEL_ICE_ROCK:
                return romType == Type_BW ? 44 : 23; // Twist Mountain
            default:
                throw new IllegalArgumentException(et + " is not a valid EvolutionType for this game.");
        }
    }

    public static int getAreaDataEntryLength(int romType) {
        if (romType == Type_BW) {
            return bw1AreaDataEntryLength;
        } else if (romType == Type_BW2) {
            return bw2AreaDataEntryLength;
        }
        return 0;
    }

    public static int getEncounterAreaCount(int romType) {
        if (romType == Type_BW) {
            return bw1EncounterAreaCount;
        } else if (romType == Type_BW2) {
            return bw2EncounterAreaCount;
        }
        return 0;
    }

    public static int[] getWildFileToAreaMap(int romType) {
        if (romType == Type_BW) {
            return bw1WildFileToAreaMap;
        } else if (romType == Type_BW2) {
            return bw2WildFileToAreaMap;
        }
        return new int[0];
    }

    public static List<Integer> getMainGameShops(int romType) {
        if (romType == Type_BW) {
            return bw1MainGameShops;
        } else if (romType == Type_BW2) {
            return bw2MainGameShops;
        }
        return new ArrayList<>();
    }

    public static List<Integer> getIrregularFormes(int romType) {
        if (romType == Type_BW) {
            return bw1IrregularFormes;
        } else if (romType == Type_BW2) {
            return bw2IrregularFormes;
        }
        return new ArrayList<>();
    }

    public static int getFormeCount(int romType) {
        if (romType == Type_BW) {
            return bw1FormeCount;
        } else if (romType == Type_BW2) {
            return bw2FormeCount;
        }
        return 0;
    }

    public static int getFormeOffset(int romType) {
        if (romType == Type_BW) {
            return bw1formeOffset;
        } else if (romType == Type_BW2) {
            return bw2formeOffset;
        }
        return 0;
    }

    public static int getNonPokemonBattleSpriteCount(int romType) {
        if (romType == Type_BW) {
            return bw1NonPokemonBattleSpriteCount;
        } else if (romType == Type_BW2) {
            return bw2NonPokemonBattleSpriteCount;
        }
        return 0;
    }

    private static Map<Integer,Map<Integer,String>> setupFormeSuffixesByBaseForme() {
        Map<Integer,Map<Integer,String>> map = new HashMap<>();

        putFormSuffixes(map, SpeciesIDs.castform, "-Sunny", "-Rainy", "-Snowy");
        putFormSuffixes(map, SpeciesIDs.deoxys, "-Attack", "-Defense", "-Speed");

        putFormSuffixes(map, SpeciesIDs.wormadam, "-Sandy", "-Trash");
        putFormSuffixes(map, SpeciesIDs.rotom, "-Heat", "-Wash", "-Frost", "-Fan", "-Mow");
        putFormSuffixes(map, SpeciesIDs.giratina, "-Origin");
        putFormSuffixes(map, SpeciesIDs.shaymin, "-Sky");

        putFormSuffixes(map, SpeciesIDs.basculin, "-Blue");
        putFormSuffixes(map, SpeciesIDs.darmanitan, "-Zen");
        putFormSuffixes(map, SpeciesIDs.tornadus, "-Therian");
        putFormSuffixes(map, SpeciesIDs.thundurus, "-Therian");
        putFormSuffixes(map, SpeciesIDs.landorus, "-Therian");
        putFormSuffixes(map, SpeciesIDs.kyurem, "-White", "-Black");
        putFormSuffixes(map, SpeciesIDs.keldeo, "-Resolute");
        putFormSuffixes(map, SpeciesIDs.meloetta, "-Pirouette");

        return map;
    }

    private static void putFormSuffixes(Map<Integer, Map<Integer, String>> map, int species, String... suffixes) {
        Map<Integer, String> speciesMap = new HashMap<>();
        for (int i = 0; i < suffixes.length; i++) {
            speciesMap.put(i + 1, suffixes[i]);
        }
        map.put(species, speciesMap);
    }

    private static Map<Integer,String> setupDummyFormeSuffixes() {
        Map<Integer,String> m = new HashMap<>();
        m.put(0,"");
        return m;
    }

    private static Map<Integer,Map<Integer,Integer>> setupAbsolutePokeNumsByBaseForme() {

        Map<Integer,Map<Integer,Integer>> map = new HashMap<>();

        Map<Integer,Integer> deoxysMap = new HashMap<>();
        deoxysMap.put(1, SpeciesIDs.Gen5Formes.deoxysA);
        deoxysMap.put(2, SpeciesIDs.Gen5Formes.deoxysD);
        deoxysMap.put(3, SpeciesIDs.Gen5Formes.deoxysS);
        map.put(SpeciesIDs.deoxys, deoxysMap);

        Map<Integer,Integer> wormadamMap = new HashMap<>();
        wormadamMap.put(1, SpeciesIDs.Gen5Formes.wormadamS);
        wormadamMap.put(2, SpeciesIDs.Gen5Formes.wormadamT);
        map.put(SpeciesIDs.wormadam, wormadamMap);

        Map<Integer,Integer> shayminMap = new HashMap<>();
        shayminMap.put(1, SpeciesIDs.Gen5Formes.shayminS);
        map.put(SpeciesIDs.shaymin, shayminMap);

        Map<Integer,Integer> giratinaMap = new HashMap<>();
        giratinaMap.put(1, SpeciesIDs.Gen5Formes.giratinaO);
        map.put(SpeciesIDs.giratina, giratinaMap);

        Map<Integer,Integer> rotomMap = new HashMap<>();
        rotomMap.put(1, SpeciesIDs.Gen5Formes.rotomH);
        rotomMap.put(2, SpeciesIDs.Gen5Formes.rotomW);
        rotomMap.put(3, SpeciesIDs.Gen5Formes.rotomFr);
        rotomMap.put(4, SpeciesIDs.Gen5Formes.rotomFa);
        rotomMap.put(5, SpeciesIDs.Gen5Formes.rotomM);
        map.put(SpeciesIDs.rotom, rotomMap);

        Map<Integer,Integer> castformMap = new HashMap<>();
        castformMap.put(1, SpeciesIDs.Gen5Formes.castformSu);
        castformMap.put(2, SpeciesIDs.Gen5Formes.castformR);
        castformMap.put(3, SpeciesIDs.Gen5Formes.castformSn);
        map.put(SpeciesIDs.castform, castformMap);

        Map<Integer,Integer> basculinMap = new HashMap<>();
        basculinMap.put(1, SpeciesIDs.Gen5Formes.basculinB);
        map.put(SpeciesIDs.basculin, basculinMap);

        Map<Integer,Integer> darmanitanMap = new HashMap<>();
        darmanitanMap.put(1, SpeciesIDs.Gen5Formes.darmanitanZ);
        map.put(SpeciesIDs.darmanitan, darmanitanMap);

        Map<Integer,Integer> meloettaMap = new HashMap<>();
        meloettaMap.put(1, SpeciesIDs.Gen5Formes.meloettaP);
        map.put(SpeciesIDs.meloetta, meloettaMap);

        Map<Integer,Integer> kyuremMap = new HashMap<>();
        kyuremMap.put(1, SpeciesIDs.Gen5Formes.kyuremW);
        kyuremMap.put(2, SpeciesIDs.Gen5Formes.kyuremB);
        map.put(SpeciesIDs.kyurem, kyuremMap);

        Map<Integer,Integer> keldeoMap = new HashMap<>();
        keldeoMap.put(1, SpeciesIDs.Gen5Formes.keldeoCosmetic1);
        map.put(SpeciesIDs.keldeo, keldeoMap);

        Map<Integer,Integer> tornadusMap = new HashMap<>();
        tornadusMap.put(1, SpeciesIDs.Gen5Formes.tornadusT);
        map.put(SpeciesIDs.tornadus, tornadusMap);

        Map<Integer,Integer> thundurusMap = new HashMap<>();
        thundurusMap.put(1, SpeciesIDs.Gen5Formes.thundurusT);
        map.put(SpeciesIDs.thundurus, thundurusMap);

        Map<Integer,Integer> landorusMap = new HashMap<>();
        landorusMap.put(1, SpeciesIDs.Gen5Formes.landorusT);
        map.put(SpeciesIDs.landorus, landorusMap);

        return map;
    }

    private static Map<Integer,Integer> setupDummyAbsolutePokeNums() {
        Map<Integer,Integer> m = new HashMap<>();
        m.put(255,0);
        return m;
    }

    public static String blackBoxLegendaryCheckPrefix1 = "79F6BAEF07B0F0BDC046", blackBoxLegendaryCheckPrefix2 = "DEDB0020C04302B0F8BDC046",
        whiteBoxLegendaryCheckPrefix1 = "00F0FEF8002070BD", whiteBoxLegendaryCheckPrefix2 = "64F62EF970BD0000";

    public static final Set<Integer> bannedItems = setupBannedItems();
    private static final Set<Integer> badItemsBW2 = setupBadItemsBW2();
    private static final Set<Integer> badItemsBW1 = setupBadItemsBW1();
    public static final Set<Integer> regularShopItems = setupRegularShopItems();
    public static final Set<Integer> opShopItems = setupOPShopItems();

    private static Set<Integer> setupBannedItems() {
        Set<Integer> set = new HashSet<>(Arrays.asList(ItemIDs.libertyPass, ItemIDs.propCase, ItemIDs.dragonSkull,
                ItemIDs.lightStone, ItemIDs.darkStone));
        addBetween(set, ItemIDs.explorerKit, ItemIDs.tidalBell);
        addBetween(set, ItemIDs.dataCard01, ItemIDs.enigmaStone);
        addBetween(set, ItemIDs.xtransceiverMale, ItemIDs.revealGlass);
        // Unknown blank items or version exclusives
        addBetween(set, ItemIDs.tea, ItemIDs.autograph);
        addBetween(set, ItemIDs.unused120, ItemIDs.unused133);
        // HMs
        addBetween(set, ItemIDs.hm01, ItemIDs.hm08);
        // Battle Launcher exclusives
        addBetween(set, ItemIDs.direHit2, ItemIDs.direHit3);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItemsBW2() {
        // specific pokemon hold items, berries, apricorns, mail
        Set<Integer> set = new HashSet<>(Arrays.asList(ItemIDs.oddKeystone, ItemIDs.griseousOrb, ItemIDs.adamantOrb,
                ItemIDs.lustrousOrb, ItemIDs.soulDew, ItemIDs.lightBall, ItemIDs.oranBerry, ItemIDs.quickPowder,
                ItemIDs.passOrb));
        addBetween(set, ItemIDs.growthMulch, ItemIDs.gooeyMulch); // mulch
        addBetween(set, ItemIDs.mail1, ItemIDs.mail12); // mails
        addBetween(set, ItemIDs.figyBerry, ItemIDs.belueBerry); // berries without useful battle effects
        addBetween(set, ItemIDs.luckyPunch, ItemIDs.leek); // pokemon specific
        addBetween(set, ItemIDs.redScarf, ItemIDs.yellowScarf); // contest scarves
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItemsBW1() {
        Set<Integer> set = new HashSet<>(badItemsBW2);
        // The shards are bad in BW1; even the maniac only gives you $200 for them, and they serve no other purpose.
        addBetween(set, ItemIDs.redShard, ItemIDs.greenShard);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupRegularShopItems() {
        Set<Integer> set = new HashSet<>();
        addBetween(set, ItemIDs.ultraBall, ItemIDs.pokeBall);
        addBetween(set, ItemIDs.potion, ItemIDs.revive);
        addBetween(set, ItemIDs.superRepel, ItemIDs.repel);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupOPShopItems() {
        Set<Integer> set = new HashSet<>();
        // "Money items" etc
        set.add(ItemIDs.lavaCookie);
        set.add(ItemIDs.berryJuice);
        set.add(ItemIDs.rareCandy);
        set.add(ItemIDs.oldGateau);
        addBetween(set, ItemIDs.blueFlute, ItemIDs.shoalShell);
        addBetween(set, ItemIDs.tinyMushroom, ItemIDs.nugget);
        set.add(ItemIDs.rareBone);
        addBetween(set, ItemIDs.lansatBerry, ItemIDs.rowapBerry);
        set.add(ItemIDs.luckyEgg);
        set.add(ItemIDs.prettyFeather);
        addBetween(set, ItemIDs.balmMushroom, ItemIDs.casteliacone);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Adds the Integers to the set, from start to end, inclusive.
     */
    private static void addBetween(Set<Integer> set, int start, int end) {
        for (int i = start; i <= end; i++) {
            set.add(i);
        }
    }

    public static Set<Integer> getBadItems(int romType) {
        if (romType == Gen5Constants.Type_BW2) {
            return badItemsBW2;
        } else {
            return badItemsBW1;
        }
    }

    public static final Map<Integer, Integer> balancedItemPrices = Stream.of(new Integer[][]{
            {ItemIDs.masterBall, 3000},
            {ItemIDs.ultraBall, 1200},
            {ItemIDs.greatBall, 600},
            {ItemIDs.pokeBall, 200},
            {ItemIDs.safariBall, 500},
            {ItemIDs.netBall, 1000},
            {ItemIDs.diveBall, 1000},
            {ItemIDs.nestBall, 1000},
            {ItemIDs.repeatBall, 1000},
            {ItemIDs.timerBall, 1000},
            {ItemIDs.luxuryBall, 1000},
            {ItemIDs.premierBall, 200},
            {ItemIDs.duskBall, 1000},
            {ItemIDs.healBall, 300},
            {ItemIDs.quickBall, 1000},
            {ItemIDs.cherishBall, 200},
            {ItemIDs.fastBall, 300},
            {ItemIDs.levelBall, 300},
            {ItemIDs.lureBall, 300},
            {ItemIDs.heavyBall, 300},
            {ItemIDs.loveBall, 300},
            {ItemIDs.friendBall, 300},
            {ItemIDs.moonBall, 300},
            {ItemIDs.sportBall, 300},

            {ItemIDs.redApricorn, 20},
            {ItemIDs.blueApricorn, 20},
            {ItemIDs.yellowApricorn, 20},
            {ItemIDs.greenApricorn, 20},
            {ItemIDs.pinkApricorn, 20},
            {ItemIDs.whiteApricorn, 20},
            {ItemIDs.blackApricorn, 20},

            {ItemIDs.potion, 300},
            {ItemIDs.antidote, 100},
            {ItemIDs.burnHeal, 250},
            {ItemIDs.iceHeal, 250},
            {ItemIDs.awakening, 250},
            {ItemIDs.paralyzeHeal, 200},
            {ItemIDs.fullRestore, 3000},
            {ItemIDs.maxPotion, 2500},
            {ItemIDs.hyperPotion, 1200},
            {ItemIDs.superPotion, 700},
            {ItemIDs.fullHeal, 600},
            {ItemIDs.revive, 1500},
            {ItemIDs.maxRevive, 4000},
            {ItemIDs.freshWater, 400},
            {ItemIDs.sodaPop, 600},
            {ItemIDs.lemonade, 700},
            {ItemIDs.moomooMilk, 800},
            {ItemIDs.energyPowder, 400},
            {ItemIDs.energyRoot, 1100},
            {ItemIDs.healPowder, 450},
            {ItemIDs.revivalHerb, 2800},
            {ItemIDs.ether, 3000},
            {ItemIDs.maxEther, 4500},
            {ItemIDs.elixir, 15000},
            {ItemIDs.maxElixir, 18000},
            {ItemIDs.lavaCookie, 450},
            {ItemIDs.berryJuice, 100},
            {ItemIDs.sacredAsh, 10000},
            {ItemIDs.oldGateau, 450},
            {ItemIDs.rageCandyBar, 15000},
            {ItemIDs.casteliacone, 450},

            {ItemIDs.hpUp, 9800},
            {ItemIDs.protein, 9800},
            {ItemIDs.iron, 9800},
            {ItemIDs.carbos, 9800},
            {ItemIDs.calcium, 9800},
            {ItemIDs.rareCandy, 10000},
            {ItemIDs.ppUp, 9800},
            {ItemIDs.zinc, 9800},
            {ItemIDs.ppMax, 24900},
            {ItemIDs.healthFeather, 3000},
            {ItemIDs.muscleFeather, 3000},
            {ItemIDs.resistFeather, 3000},
            {ItemIDs.geniusFeather, 3000},
            {ItemIDs.cleverFeather, 3000},
            {ItemIDs.swiftFeather, 3000},
            {ItemIDs.prettyFeather, 200},

            {ItemIDs.guardSpec, 700},
            {ItemIDs.direHit, 650},
            {ItemIDs.xAttack, 500},
            {ItemIDs.xDefense, 550},
            {ItemIDs.xSpeed, 350},
            {ItemIDs.xAccuracy, 950},
            {ItemIDs.xSpAtk, 350},
            {ItemIDs.xSpDef, 350},

            {ItemIDs.pokeDoll, 1000},
            {ItemIDs.fluffyTail, 1000},
            {ItemIDs.blueFlute, 20},
            {ItemIDs.yellowFlute, 20},
            {ItemIDs.redFlute, 20},
            {ItemIDs.blackFlute, 20},
            {ItemIDs.whiteFlute, 20},
            {ItemIDs.shoalSalt, 20},
            {ItemIDs.shoalShell, 20},
            {ItemIDs.redShard, 400},
            {ItemIDs.blueShard, 400},
            {ItemIDs.yellowShard, 400},
            {ItemIDs.greenShard, 400},

            {ItemIDs.superRepel, 500},
            {ItemIDs.maxRepel, 700},
            {ItemIDs.escapeRope, 550},
            {ItemIDs.repel, 350},

            {ItemIDs.sunStone, 3000},
            {ItemIDs.moonStone, 3000},
            {ItemIDs.fireStone, 3000},
            {ItemIDs.thunderStone, 3000},
            {ItemIDs.waterStone, 3000},
            {ItemIDs.leafStone, 3000},
            {ItemIDs.shinyStone, 3000},
            {ItemIDs.duskStone, 3000},
            {ItemIDs.dawnStone, 3000},
            {ItemIDs.ovalStone, 3000},
            {ItemIDs.prismScale, 3000},

            {ItemIDs.tinyMushroom, 500},
            {ItemIDs.bigMushroom, 5000},
            {ItemIDs.pearl, 1400},
            {ItemIDs.bigPearl, 7500},
            {ItemIDs.stardust, 2000},
            {ItemIDs.starPiece, 9800},
            {ItemIDs.nugget, 10000},
            {ItemIDs.heartScale, 5000},
            {ItemIDs.rareBone, 10000},

            {ItemIDs.honey, 500},
            {ItemIDs.growthMulch, 200},
            {ItemIDs.dampMulch, 200},
            {ItemIDs.stableMulch, 200},
            {ItemIDs.gooeyMulch, 200},

            {ItemIDs.rootFossil, 5000},
            {ItemIDs.clawFossil, 5000},
            {ItemIDs.helixFossil, 5000},
            {ItemIDs.domeFossil, 5000},
            {ItemIDs.oldAmber, 8000},
            {ItemIDs.armorFossil, 5000},
            {ItemIDs.skullFossil, 5000},
            {ItemIDs.coverFossil, 5000},
            {ItemIDs.plumeFossil, 5000},

            {ItemIDs.oddKeystone, 2100},
            {ItemIDs.griseousOrb, 10000},
            {ItemIDs.adamantOrb, 10000},
            {ItemIDs.lustrousOrb, 10000},

            {ItemIDs.mail1, 50},
            {ItemIDs.mail2, 50},
            {ItemIDs.mail3, 50},
            {ItemIDs.mail4, 50},
            {ItemIDs.mail5, 50},
            {ItemIDs.mail6, 50},
            {ItemIDs.mail7, 50},
            {ItemIDs.mail8, 50},
            {ItemIDs.mail9, 50},
            {ItemIDs.mail10, 50},
            {ItemIDs.mail11, 50},
            {ItemIDs.mail12, 50},

            {ItemIDs.chestoBerry, 250},
            {ItemIDs.pechaBerry, 100},
            {ItemIDs.rawstBerry, 250},
            {ItemIDs.aspearBerry, 250},
            {ItemIDs.leppaBerry, 3000},
            {ItemIDs.oranBerry, 50},
            {ItemIDs.lumBerry, 500},
            {ItemIDs.sitrusBerry, 500},
            {ItemIDs.figyBerry, 100},
            {ItemIDs.wikiBerry, 100},
            {ItemIDs.magoBerry, 100},
            {ItemIDs.aguavBerry, 100},
            {ItemIDs.iapapaBerry, 100},
            {ItemIDs.razzBerry, 500},
            {ItemIDs.blukBerry, 500},
            {ItemIDs.nanabBerry, 500},
            {ItemIDs.wepearBerry, 500},
            {ItemIDs.pinapBerry, 500},
            {ItemIDs.pomegBerry, 500},
            {ItemIDs.kelpsyBerry, 500},
            {ItemIDs.qualotBerry, 500},
            {ItemIDs.hondewBerry, 500},
            {ItemIDs.grepaBerry, 500},
            {ItemIDs.tamatoBerry, 500},
            {ItemIDs.cornnBerry, 500},
            {ItemIDs.magostBerry, 500},
            {ItemIDs.rabutaBerry, 500},
            {ItemIDs.nomelBerry, 500},
            {ItemIDs.spelonBerry, 500},
            {ItemIDs.pamtreBerry, 500},
            {ItemIDs.watmelBerry, 500},
            {ItemIDs.durinBerry, 500},
            {ItemIDs.belueBerry, 500},
            {ItemIDs.occaBerry, 1000},
            {ItemIDs.passhoBerry, 1000},
            {ItemIDs.wacanBerry, 1000},
            {ItemIDs.rindoBerry, 1000},
            {ItemIDs.yacheBerry, 1000},
            {ItemIDs.chopleBerry, 1000},
            {ItemIDs.kebiaBerry, 1000},
            {ItemIDs.shucaBerry, 1000},
            {ItemIDs.cobaBerry, 1000},
            {ItemIDs.payapaBerry, 1000},
            {ItemIDs.tangaBerry, 1000},
            {ItemIDs.chartiBerry, 1000},
            {ItemIDs.kasibBerry, 1000},
            {ItemIDs.habanBerry, 1000},
            {ItemIDs.colburBerry, 1000},
            {ItemIDs.babiriBerry, 1000},
            {ItemIDs.chilanBerry, 1000},
            {ItemIDs.liechiBerry, 1000},
            {ItemIDs.ganlonBerry, 1000},
            {ItemIDs.salacBerry, 1000},
            {ItemIDs.petayaBerry, 1000},
            {ItemIDs.apicotBerry, 1000},
            {ItemIDs.lansatBerry, 1000},
            {ItemIDs.starfBerry, 1000},
            {ItemIDs.enigmaBerry, 1000},
            {ItemIDs.micleBerry, 1000},
            {ItemIDs.custapBerry, 1000},
            {ItemIDs.jabocaBerry, 1000},
            {ItemIDs.rowapBerry, 1000},

            {ItemIDs.brightPowder, 3000},
            {ItemIDs.machoBrace, 3000},
            {ItemIDs.expShare, 6000},
            {ItemIDs.quickClaw, 4500},
            {ItemIDs.choiceBand, 10000},
            {ItemIDs.kingsRock, 5000},
            {ItemIDs.silverPowder, 2000},
            {ItemIDs.amuletCoin, 15000},
            {ItemIDs.cleanseTag, 1000},
            {ItemIDs.soulDew, 200},
            {ItemIDs.deepSeaTooth, 3000},
            {ItemIDs.deepSeaScale, 3000},
            {ItemIDs.smokeBall, 200},
            {ItemIDs.everstone, 200},
            {ItemIDs.focusBand, 3000},
            {ItemIDs.luckyEgg, 10000},
            {ItemIDs.scopeLens, 5000},
            {ItemIDs.metalCoat, 3000},
            {ItemIDs.leftovers, 10000},
            {ItemIDs.dragonScale, 3000},
            {ItemIDs.lightBall, 100},
            {ItemIDs.softSand, 2000},
            {ItemIDs.hardStone, 2000},
            {ItemIDs.miracleSeed, 2000},
            {ItemIDs.blackGlasses, 2000},
            {ItemIDs.blackBelt, 2000},
            {ItemIDs.magnet, 2000},
            {ItemIDs.mysticWater, 2000},
            {ItemIDs.sharpBeak, 2000},
            {ItemIDs.poisonBarb, 2000},
            {ItemIDs.neverMeltIce, 2000},
            {ItemIDs.spellTag, 2000},
            {ItemIDs.twistedSpoon, 2000},
            {ItemIDs.charcoal, 2000},
            {ItemIDs.dragonFang, 2000},
            {ItemIDs.silkScarf, 2000},
            {ItemIDs.upgrade, 3000},
            {ItemIDs.shellBell, 6000},
            {ItemIDs.seaIncense, 2000},
            {ItemIDs.laxIncense, 3000},
            {ItemIDs.luckyPunch, 10},
            {ItemIDs.metalPowder, 10},
            {ItemIDs.thickClub, 500},
            {ItemIDs.leek, 200},
            {ItemIDs.redScarf, 100},
            {ItemIDs.blueScarf, 100},
            {ItemIDs.pinkScarf, 100},
            {ItemIDs.greenScarf, 100},
            {ItemIDs.yellowScarf, 100},
            {ItemIDs.wideLens, 1500},
            {ItemIDs.expertBelt, 6000},
            {ItemIDs.lightClay, 1500},
            {ItemIDs.lifeOrb, 10000},
            {ItemIDs.toxicOrb, 1500},
            {ItemIDs.flameOrb, 1500},
            {ItemIDs.quickPowder, 10},
            {ItemIDs.zoomLens, 1500},
            {ItemIDs.metronome, 3000},
            {ItemIDs.ironBall, 1000},
            {ItemIDs.laggingTail, 1000},
            {ItemIDs.destinyKnot, 1500},
            {ItemIDs.blackSludge, 5000},
            {ItemIDs.icyRock, 200},
            {ItemIDs.smoothRock, 200},
            {ItemIDs.heatRock, 200},
            {ItemIDs.dampRock, 200},
            {ItemIDs.gripClaw, 1500},
            {ItemIDs.choiceScarf, 10000},
            {ItemIDs.stickyBarb, 1500},
            {ItemIDs.powerBracer, 3000},
            {ItemIDs.powerBelt, 3000},
            {ItemIDs.powerLens, 3000},
            {ItemIDs.powerBand, 3000},
            {ItemIDs.powerAnklet, 3000},
            {ItemIDs.powerWeight, 3000},
            {ItemIDs.shedShell, 500},
            {ItemIDs.bigRoot, 1500},
            {ItemIDs.choiceSpecs, 10000},
            {ItemIDs.flamePlate, 2000},
            {ItemIDs.splashPlate, 2000},
            {ItemIDs.zapPlate, 2000},
            {ItemIDs.meadowPlate, 2000},
            {ItemIDs.iciclePlate, 2000},
            {ItemIDs.fistPlate, 2000},
            {ItemIDs.toxicPlate, 2000},
            {ItemIDs.earthPlate, 2000},
            {ItemIDs.skyPlate, 2000},
            {ItemIDs.mindPlate, 2000},
            {ItemIDs.insectPlate, 2000},
            {ItemIDs.stonePlate, 2000},
            {ItemIDs.spookyPlate, 2000},
            {ItemIDs.dracoPlate, 2000},
            {ItemIDs.dreadPlate, 2000},
            {ItemIDs.ironPlate, 2000},
            {ItemIDs.oddIncense, 2000},
            {ItemIDs.rockIncense, 2000},
            {ItemIDs.fullIncense, 1000},
            {ItemIDs.waveIncense, 2000},
            {ItemIDs.roseIncense, 2000},
            {ItemIDs.luckIncense, 15000},
            {ItemIDs.pureIncense, 1000},
            {ItemIDs.protector, 3000},
            {ItemIDs.electirizer, 3000},
            {ItemIDs.magmarizer, 3000},
            {ItemIDs.dubiousDisc, 3000},
            {ItemIDs.reaperCloth, 3000},
            {ItemIDs.razorClaw, 5000},
            {ItemIDs.razorFang, 5000},
            {ItemIDs.eviolite, 10000},
            {ItemIDs.floatStone, 1000},
            {ItemIDs.rockyHelmet, 6000},
            {ItemIDs.airBalloon, 1000},
            {ItemIDs.redCard, 1000},
            {ItemIDs.ringTarget, 1000},
            {ItemIDs.absorbBulb, 1000},
            {ItemIDs.cellBattery, 1000},
            {ItemIDs.ejectButton, 1000},
            {ItemIDs.fireGem, 1000},
            {ItemIDs.waterGem, 1000},
            {ItemIDs.electricGem, 1000},
            {ItemIDs.grassGem, 1000},
            {ItemIDs.iceGem, 1000},
            {ItemIDs.fightingGem, 1000},
            {ItemIDs.poisonGem, 1000},
            {ItemIDs.groundGem, 1000},
            {ItemIDs.flyingGem, 1000},
            {ItemIDs.psychicGem, 1000},
            {ItemIDs.bugGem, 1000},
            {ItemIDs.rockGem, 1000},
            {ItemIDs.ghostGem, 1000},
            {ItemIDs.dragonGem, 1000},
            {ItemIDs.darkGem, 1000},
            {ItemIDs.steelGem, 1000},
            {ItemIDs.normalGem, 1000},

            {ItemIDs.passOrb, 200},
            {ItemIDs.dreamBall, 1000},
            {ItemIDs.pokeToy, 1000},

            {ItemIDs.tm01, 10000},
            {ItemIDs.tm02, 10000},
            {ItemIDs.tm03, 10000},
            {ItemIDs.tm04, 10000},
            {ItemIDs.tm05, 10000},
            {ItemIDs.tm06, 10000},
            {ItemIDs.tm07, 20000},
            {ItemIDs.tm08, 10000},
            {ItemIDs.tm09, 10000},
            {ItemIDs.tm10, 10000},
            {ItemIDs.tm11, 20000},
            {ItemIDs.tm12, 10000},
            {ItemIDs.tm13, 10000},
            {ItemIDs.tm14, 20000},
            {ItemIDs.tm15, 20000},
            {ItemIDs.tm16, 20000},
            {ItemIDs.tm17, 10000},
            {ItemIDs.tm18, 20000},
            {ItemIDs.tm19, 10000},
            {ItemIDs.tm20, 20000},
            {ItemIDs.tm21, 10000},
            {ItemIDs.tm22, 10000},
            {ItemIDs.tm23, 10000},
            {ItemIDs.tm24, 10000},
            {ItemIDs.tm25, 20000},
            {ItemIDs.tm26, 10000},
            {ItemIDs.tm27, 10000},
            {ItemIDs.tm28, 10000},
            {ItemIDs.tm29, 10000},
            {ItemIDs.tm30, 10000},
            {ItemIDs.tm31, 10000},
            {ItemIDs.tm32, 10000},
            {ItemIDs.tm33, 20000},
            {ItemIDs.tm34, 10000},
            {ItemIDs.tm35, 10000},
            {ItemIDs.tm36, 10000},
            {ItemIDs.tm37, 20000},
            {ItemIDs.tm38, 20000},
            {ItemIDs.tm39, 10000},
            {ItemIDs.tm40, 10000},
            {ItemIDs.tm41, 10000},
            {ItemIDs.tm42, 10000},
            {ItemIDs.tm43, 10000},
            {ItemIDs.tm44, 10000},
            {ItemIDs.tm45, 10000},
            {ItemIDs.tm46, 10000},
            {ItemIDs.tm47, 10000},
            {ItemIDs.tm48, 10000},
            {ItemIDs.tm49, 10000},
            {ItemIDs.tm50, 10000},
            {ItemIDs.tm51, 10000},
            {ItemIDs.tm52, 10000},
            {ItemIDs.tm53, 10000},
            {ItemIDs.tm54, 10000},
            {ItemIDs.tm55, 10000},
            {ItemIDs.tm56, 10000},
            {ItemIDs.tm57, 10000},
            {ItemIDs.tm58, 10000},
            {ItemIDs.tm59, 10000},
            {ItemIDs.tm60, 10000},
            {ItemIDs.tm61, 10000},
            {ItemIDs.tm62, 10000},
            {ItemIDs.tm63, 10000},
            {ItemIDs.tm64, 10000},
            {ItemIDs.tm65, 10000},
            {ItemIDs.tm66, 10000},
            {ItemIDs.tm67, 10000},
            {ItemIDs.tm68, 20000},
            {ItemIDs.tm69, 10000},
            {ItemIDs.tm70, 10000},
            {ItemIDs.tm71, 10000},
            {ItemIDs.tm72, 10000},
            {ItemIDs.tm73, 10000},
            {ItemIDs.tm74, 10000},
            {ItemIDs.tm75, 10000},
            {ItemIDs.tm76, 10000},
            {ItemIDs.tm77, 10000},
            {ItemIDs.tm78, 10000},
            {ItemIDs.tm79, 10000},
            {ItemIDs.tm80, 10000},
            {ItemIDs.tm81, 10000},
            {ItemIDs.tm82, 10000},
            {ItemIDs.tm83, 10000},
            {ItemIDs.tm84, 10000},
            {ItemIDs.tm85, 10000},
            {ItemIDs.tm86, 10000},
            {ItemIDs.tm87, 10000},
            {ItemIDs.tm88, 10000},
            {ItemIDs.tm89, 10000},
            {ItemIDs.tm90, 10000},
            {ItemIDs.tm91, 10000},
            {ItemIDs.tm92, 10000},
            {ItemIDs.tm93, 10000},
            {ItemIDs.tm94, 10000},
            {ItemIDs.tm95, 10000},
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

    /* @formatter:off */
    @SuppressWarnings("unused")
    private static final int[][] habitatListEntries = {
        { 104, 105 }, // Route 4
        { 124 }, // Route 15
        { 134 }, // Route 21
        { 84, 85, 86 }, // Clay Tunnel
        { 23, 24, 25, 26 }, // Twist Mountain
        { 97 }, // Village Bridge
        { 27, 28, 29, 30 }, // Dragonspiral Tower
        { 81, 82, 83 }, // Relic Passage
        { 106 }, // Route 5*
        { 125 }, // Route 16*
        { 98 }, // Marvelous Bridge
        { 123 }, // Abundant Shrine
        { 132 }, // Undella Town
        { 107 }, // Route 6
        { 43 }, // Undella Bay
        { 102, 103 }, // Wellspring Cave
        { 95 }, // Nature Preserve
        { 127 }, // Route 18
        { 32, 33, 34, 35, 36 }, // Giant Chasm
        { 111 }, // Route 7
        { 31, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80 }, // Victory Road
        { 12, 13, 14, 15, 16, 17, 18, 19 }, // Relic Castle
        { 0 }, // Striation City
        { 128 }, // Route 19
        { 3 }, // Aspertia City
        { 116 }, // Route 8*
        { 44, 45 }, // Floccesy Ranch
        { 61, 62, 63, 64, 65, 66, 67, 68, 69, 70 }, // Strange House
        { 129 }, // Route 20
        { 4 }, // Virbank City
        { 37, 38, 39, 40, 41 }, // Castelia Sewers
        { 118 }, // Route 9
        { 46, 47 }, // Virbank Complex
        { 42 }, // P2 Laboratory
        { 1 }, // Castelia City
        { 8, 9 }, // Pinwheel Forest
        { 5 }, // Humilau City
        { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60 }, // Reversal Mountain
        { 6, 7 }, // Dreamyard
        { 112, 113, 114, 115 }, // Celestial Tower
        { 130 }, // Route 22
        { 10, 11 }, // Desert Resort
        { 119 }, // Route 11
        { 133 }, // Route 17
        { 99 }, // Route 1
        { 131 }, // Route 23
        { 2 }, // Icirrus City*
        { 120 }, // Route 12
        { 100 }, // Route 2
        { 108, 109 }, // Mistralton Cave
        { 121 }, // Route 13
        { 101 }, // Route 3
        { 117 }, // Moor of Icirrus*
        { 96 }, // Driftveil Drawbridge
        { 93, 94 }, // Seaside Cave
        { 126 }, // Lostlorn Forest
        { 122 }, // Route 14
        { 20, 21, 22 }, // Chargestone Cave
    };

    private static final int[] bw1WildFileToAreaMap = {
        2,
        6,
        8,
        18, 18,
        19, 19,
        20, 20,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, // lol
        22,
        23, 23, 23,
        24, 24, 24, 24,
        25, 25, 25, 25,
        26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
        27, 27, 27, 27,
        29,
        36,
        57,
        59,
        60,
        38,
        39,
        40,
        30, 30,
        41,
        42,
        43,
        31, 31, 31,
        44,
        33, 33, 33, 33,
        45,
        34,
        46,
        32, 32, 32,
        47, 47,
        48,
        49,
        50,
        51,
        35,
        52,
        53,
        37,
        55,
        12,
        54,
    };

    private static final int[] bw2WildFileToAreaMap = {
        2,
        4,
        8,
        59,
        61,
        63,
        19, 19,
        20, 20,
        21, 21,
        22, 22, 22, 22, 22, 22, 22, 22,
        24, 24, 24,
        25, 25, 25, 25,
        26, 26, 26, 26,
        76,
        27, 27, 27, 27, 27,
        70, 70, 70, 70, 70,
        29,
        35,
        71, 71,
        72, 72,
        73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73,
        74, 74, 74, 74, 74, 74, 74, 74, 74, 74,
        76, 76, 76, 76, 76, 76, 76, 76, 76, 76,
        77, 77, 77,
        79, 79, 79, 79, 79, 79, 79, 79, 79,
        78, 78,
        -1, // Nature Preserve (not on map)
        55,
        57,
        58,
        37,
        38,
        39,
        30, 30,
        40, 40,
        41,
        42,
        31, 31, 31,
        43,
        32, 32, 32, 32,
        44,
        33,
        45,
        46,
        47,
        48,
        49,
        34,
        50,
        51,
        36,
        53,
        66,
        67,
        69,
        75,
        12,
        52,
        68,
    };

    public static void tagTrainersBW(List<Trainer> trs) {
        // We use different Gym IDs to cheat the system for the 3 n00bs
        // Chili, Cress, and Cilan
        // Cilan can be GYM1, then Chili is GYM9 and Cress GYM10
        // Also their *trainers* are GYM11 lol

        // Gym Trainers
        tag(trs, "GYM11", 0x09, 0x0A);
        tag(trs, "GYM2", 0x56, 0x57, 0x58);
        tag(trs, "GYM3", 0xC4, 0xC6, 0xC7, 0xC8);
        tag(trs, "GYM4", 0x42, 0x43, 0x44, 0x45);
        tag(trs, "GYM5", 0xC9, 0xCA, 0xCB, 0x5F, 0xA8);
        tag(trs, "GYM6", 0x7D, 0x7F, 0x80, 0x46, 0x47);
        tag(trs, "GYM7", 0xD7, 0xD8, 0xD9, 0xD4, 0xD5, 0xD6);
        tag(trs, "GYM8", 0x109, 0x10A, 0x10F, 0x10E, 0x110, 0x10B, 0x113, 0x112);

        // Gym Leaders
        tag(trs, 0x0C, "GYM1-LEADER"); // Cilan
        tag(trs, 0x0B, "GYM9-LEADER"); // Chili
        tag(trs, 0x0D, "GYM10-LEADER"); // Cress
        tag(trs, 0x15, "GYM2-LEADER"); // Lenora
        tag(trs, 0x16, "GYM3-LEADER"); // Burgh
        tag(trs, 0x17, "GYM4-LEADER"); // Elesa
        tag(trs, 0x18, "GYM5-LEADER"); // Clay
        tag(trs, 0x19, "GYM6-LEADER"); // Skyla
        tag(trs, 0x83, "GYM7-LEADER"); // Brycen
        tag(trs, 0x84, "GYM8-LEADER"); // Iris or Drayden
        tag(trs, 0x85, "GYM8-LEADER"); // Iris or Drayden

        // Elite 4
        tag(trs, 0xE4, "ELITE1"); // Shauntal
        tag(trs, 0xE6, "ELITE2"); // Grimsley
        tag(trs, 0xE7, "ELITE3"); // Caitlin
        tag(trs, 0xE5, "ELITE4"); // Marshal

        // Elite 4 R2
        tag(trs, 0x233, "ELITE1"); // Shauntal
        tag(trs, 0x235, "ELITE2"); // Grimsley
        tag(trs, 0x236, "ELITE3"); // Caitlin
        tag(trs, 0x234, "ELITE4"); // Marshal
        tag(trs, 0x197, "CHAMPION"); // Alder

        // Ubers?
        tag(trs, 0x21E, "UBER"); // Game Freak Guy
        tag(trs, 0x237, "UBER"); // Cynthia
        tag(trs, 0xE8, "UBER"); // Ghetsis
        tag(trs, 0x24A, "UBER"); // N-White
        tag(trs, 0x24B, "UBER"); // N-Black

        // Rival - Cheren
        tagRivalBW(trs, "RIVAL1", 0x35);
        tagRivalBW(trs, "RIVAL2", 0x11F);
        tagRivalBW(trs, "RIVAL3", 0x38); // used for 3rd battle AND tag battle
        tagRivalBW(trs, "RIVAL4", 0x193);
        tagRivalBW(trs, "RIVAL5", 0x5A); // 5th battle & 2nd tag battle
        tagRivalBW(trs, "RIVAL6", 0x21B);
        tagRivalBW(trs, "RIVAL7", 0x24C);
        tagRivalBW(trs, "RIVAL8", 0x24F);

        // Rival - Bianca
        tagRivalBW(trs, "FRIEND1", 0x3B);
        tagRivalBW(trs, "FRIEND2", 0x1F2);
        tagRivalBW(trs, "FRIEND3", 0x1FB);
        tagRivalBW(trs, "FRIEND4", 0x1EB);
        tagRivalBW(trs, "FRIEND5", 0x1EE);
        tagRivalBW(trs, "FRIEND6", 0x252);

        // N
        tag(trs, "NOTSTRONG", 64);
        tag(trs, "STRONG", 65, 89, 218);
    }

    public static void tagTrainersBW2(List<Trainer> trs) {
        // Use GYM9/10/11 for the retired Chili/Cress/Cilan.
        // Lenora doesn't have a team, or she'd be 12.
        // Likewise for Brycen

        // Some trainers have TWO teams because of Challenge Mode
        // I believe this is limited to Gym Leaders, E4, Champ...
        // The "Challenge Mode" teams have levels at similar to regular,
        // but have the normal boost applied too.

        // Gym Trainers
        tag(trs, "GYM1", 0xab, 0xac);
        tag(trs, "GYM2", 0xb2, 0xb3);
        tag(trs, "GYM3", 0x2de, 0x2df, 0x2e0, 0x2e1);
        // GYM4: old gym site included to give the city a theme
        tag(trs, "GYM4", 0x26d, 0x94, 0xcf, 0xd0, 0xd1); // 0x94 might be 0x324
        tag(trs, "GYM5", 0x13f, 0x140, 0x141, 0x142, 0x143, 0x144, 0x145);
        tag(trs, "GYM6", 0x95, 0x96, 0x97, 0x98, 0x14c);
        tag(trs, "GYM7", 0x17d, 0x17e, 0x17f, 0x180, 0x181);
        tag(trs, "GYM8", 0x15e, 0x15f, 0x160, 0x161, 0x162, 0x163);

        // Gym Leaders
        // Order: Normal, Challenge Mode
        // All the challenge mode teams are near the end of the ROM
        // which makes things a bit easier.
        tag(trs, "GYM1-LEADER", 0x9c, 0x2fc); // Cheren
        tag(trs, "GYM2-LEADER", 0x9d, 0x2fd); // Roxie
        tag(trs, "GYM3-LEADER", 0x9a, 0x2fe); // Burgh
        tag(trs, "GYM4-LEADER", 0x99, 0x2ff); // Elesa
        tag(trs, "GYM5-LEADER", 0x9e, 0x300); // Clay
        tag(trs, "GYM6-LEADER", 0x9b, 0x301); // Skyla
        tag(trs, "GYM7-LEADER", 0x9f, 0x302); // Drayden
        tag(trs, "GYM8-LEADER", 0xa0, 0x303); // Marlon

        // Elite 4 / Champion
        // Order: Normal, Challenge Mode, Rematch, Rematch Challenge Mode
        tag(trs, "ELITE1", 0x26, 0x304, 0x8f, 0x309);
        tag(trs, "ELITE2", 0x28, 0x305, 0x91, 0x30a);
        tag(trs, "ELITE3", 0x29, 0x307, 0x92, 0x30c);
        tag(trs, "ELITE4", 0x27, 0x306, 0x90, 0x30b);
        tag(trs, "CHAMPION", 0x155, 0x308, 0x218, 0x30d);

        // Rival - Hugh
        tagRivalBW(trs, "RIVAL1", 0xa1); // Start
        tagRivalBW(trs, "RIVAL2", 0xa6); // Floccessy Ranch
        tagRivalBW(trs, "RIVAL3", 0x24c); // Tag Battles in the sewers
        tagRivalBW(trs, "RIVAL4", 0x170); // Tag Battle on the Plasma Frigate
        tagRivalBW(trs, "RIVAL5", 0x17a); // Undella Town 1st visit
        tagRivalBW(trs, "RIVAL6", 0x2bd); // Lacunosa Town Tag Battle
        tagRivalBW(trs, "RIVAL7", 0x31a); // 2nd Plasma Frigate Tag Battle
        tagRivalBW(trs, "RIVAL8", 0x2ac); // Victory Road
        tagRivalBW(trs, "RIVAL9", 0x2b5); // Undella Town Post-E4
        tagRivalBW(trs, "RIVAL10", 0x2b8); // Driftveil Post-Undella-Battle

        // Tag Battle with Opposite Gender Hero
        tagRivalBW(trs, "FRIEND1", 0x168);
        tagRivalBW(trs, "FRIEND1", 0x16b);

        // Tag/PWT Battles with Cheren
        tag(trs, "GYM1", 0x173, 0x278, 0x32E);

        // The Restaurant Brothers
        tag(trs, "GYM9-LEADER", 0x1f0); // Cilan
        tag(trs, "GYM10-LEADER", 0x1ee); // Chili
        tag(trs, "GYM11-LEADER", 0x1ef); // Cress

        // Themed Trainers
        tag(trs, "THEMED:ZINZOLIN-STRONG", 0x2c0, 0x248, 0x15b, 0x1f1);
        tag(trs, "THEMED:COLRESS-STRONG", 0x166, 0x158, 0x32d, 0x32f);
        tag(trs, "THEMED:SHADOW1", 0x247, 0x15c, 0x2af);
        tag(trs, "THEMED:SHADOW2", 0x1f2, 0x2b0);
        tag(trs, "THEMED:SHADOW3", 0x1f3, 0x2b1);

        // Uber-Trainers
        // There are *fourteen* ubers of 17 allowed (incl. the champion)
        // It's a rather stacked game...
        tag(trs, 0x246, "UBER"); // Alder
        tag(trs, 0x1c8, "UBER"); // Cynthia
        tag(trs, 0xca, "UBER"); // Benga/BlackTower
        tag(trs, 0xc9, "UBER"); // Benga/WhiteTreehollow
        tag(trs, 0x5, "UBER"); // N/Zekrom
        tag(trs, 0x6, "UBER"); // N/Reshiram
        tag(trs, 0x30e, "UBER"); // N/Spring
        tag(trs, 0x30f, "UBER"); // N/Summer
        tag(trs, 0x310, "UBER"); // N/Autumn
        tag(trs, 0x311, "UBER"); // N/Winter
        tag(trs, 0x159, "UBER"); // Ghetsis
        tag(trs, 0x8c, "UBER"); // Game Freak Guy
        tag(trs, 0x24f, "UBER"); // Game Freak Leftovers Guy

    }

    private static void tagRivalBW(List<Trainer> allTrainers, String tag, int offset) {
        allTrainers.get(offset - 1).tag = tag + "-0";
        allTrainers.get(offset).tag = tag + "-1";
        allTrainers.get(offset + 1).tag = tag + "-2";

    }

    private static void tag(List<Trainer> allTrainers, int number, String tag) {
        if (allTrainers.size() > (number - 1)) {
            allTrainers.get(number - 1).tag = tag;
        }
    }

    private static void tag(List<Trainer> allTrainers, String tag, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).tag = tag;
            }
        }
    }

    public static final HashMap<String, Type> gymAndEliteThemesBW = setupGymAndEliteThemesBW();

    private static HashMap<String, Type> setupGymAndEliteThemesBW() {
        HashMap<String, Type> themeMap = new HashMap<>();
        //Alder has no theme
        themeMap.put("ELITE1", Type.GHOST); //Shauntal
        themeMap.put("ELITE2", Type.DARK); //Grimsley
        themeMap.put("ELITE3", Type.PSYCHIC); //Caitlin
        themeMap.put("ELITE4", Type.FIGHTING); //Marshal
        themeMap.put("GYM1", Type.GRASS); //Cilan
        themeMap.put("GYM2", Type.NORMAL); //Lenora
        themeMap.put("GYM3", Type.BUG); //Burgh
        themeMap.put("GYM4", Type.ELECTRIC); //Elesa
        themeMap.put("GYM5", Type.GROUND); //Clay
        themeMap.put("GYM6", Type.FLYING); //Skyla
        themeMap.put("GYM7", Type.ICE); //Brycen
        themeMap.put("GYM8", Type.DRAGON); //Iris
        themeMap.put("GYM9", Type.FIRE); //Chili
        themeMap.put("GYM10", Type.WATER); //Cress
        themeMap.put("GYM11", Type.NORMAL); //Trio gym trainers
        return themeMap;
    }

    public static final HashMap<String, Type> gymAndEliteThemesBW2 = setupGymAndEliteThemesBW2();

    private static HashMap<String, Type> setupGymAndEliteThemesBW2() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("CHAMPION", Type.DRAGON); //Iris
        themeMap.put("ELITE1", Type.GHOST); //Shauntal
        themeMap.put("ELITE2", Type.DARK); //Grimsley
        themeMap.put("ELITE3", Type.PSYCHIC); //Caitlin
        themeMap.put("ELITE4", Type.FIGHTING); //Marshal
        themeMap.put("GYM1", Type.NORMAL); //Cheren
        themeMap.put("GYM2", Type.POISON); //Roxie
        themeMap.put("GYM3", Type.BUG); //Burgh
        themeMap.put("GYM4", Type.ELECTRIC); //Elesa
        themeMap.put("GYM5", Type.GROUND); //Clay
        themeMap.put("GYM6", Type.FLYING); //Skyla
        themeMap.put("GYM7", Type.DRAGON); //Drayden
        themeMap.put("GYM8", Type.WATER); //Marlon
        //The trio gym is no longer an official League gym, so I'm not including it.
        //(Besides, their theming works as-is.)
        return themeMap;
    }

    public static void setMultiBattleStatusBW(List<Trainer> trs) {
        // 62 + 63: Multi Battle with Team Plasma Grunts in Wellspring Cave w/ Cheren
        // 401 + 402: Double Battle with Preschooler Sarah and Preschooler Billy
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 62, 63, 401, 402);
    }

    public static void setMultiBattleStatusBW2(List<Trainer> trs, boolean isBlack2) {
        // 342 + 356: Multi Battle with Team Plasma Grunts in Castelia Sewers w/ Hugh
        // 347 + 797: Multi Battle with Team Plasma Zinzolin and Team Plasma Grunt on Plasma Frigate w/ Hugh
        // 374 + 375: Multi Battle with Team Plasma Grunts on Plasma Frigate w/ Cheren
        // 376 + 377: Multi Battle with Team Plasma Grunts on Plasma Frigate w/ Hugh
        // 494 + 495 + 496: Cilan, Chili, and Cress all participate in a Multi Battle
        // 614 + 615: Double Battle with Veteran Claude and Veteran Cecile
        // 643 + 644: Double Battle with Veteran Sinan and Veteran Rosaline
        // 704 + 705: Multi Battle with Team Plasma Zinzolin and Team Plasma Grunt in Lacunosa Town w/ Hugh
        // 798 + 799: Multi Battle with Team Plasma Grunts on Plasma Frigate w/ Hugh
        // 807 + 809: Double Battle with Team Plasma Grunts on Plasma Frigate
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 342, 347, 356, 374, 375, 376, 377, 494,
                495, 496, 614, 615, 643, 644, 704, 705, 797, 798, 799, 807, 809
        );

        // 513/788 + 522: Potential Double Battle with Backpacker Kiyo (513 in B2, 788 in W2) and Hiker Markus
        // 519/786 + 520/787: Potential Double Battle with Ace Trainer Ray (519 in W2, 786 in B2) and Ace Trainer Cora (520 in B2, 787 in W2)
        // 602 + 603: Potential Double Battle with Ace Trainer Webster and Ace Trainer Shanta
        // 790 + 791: Potential Double Battle with Nursery Aide Rosalyn and Preschooler Ike
        // 792 + 793: Potential Double Battle with Youngster Henley and Lass Helia
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 513, 522, 602, 603, 788, 790, 791, 792, 793);

        if (isBlack2) {
            // 789 + 521: Double Battle with Backpacker Kumiko and Hiker Jared
            setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 521, 789);

            // 786 + 520: Potential Double Batlte with Ace Trainer Ray and Ace Trainer Cora
            setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 520, 786);
        } else {
            // 514 + 521: Potential Double Battle with Backpacker Kumiko and Hiker Jared
            setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 514, 521);

            // 519 + 787: Double Battle with Ace Trainer Ray and Ace Trainer Cora
            setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 519, 787);
        }
    }

    private static void setMultiBattleStatus(List<Trainer> allTrainers, Trainer.MultiBattleStatus status, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).multiBattleStatus = status;
            }
        }
    }

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

    private static void addCopies(List<String> list, int n, String s) {
        list.addAll(Collections.nCopies(n, s));
    }

    /**
     * Based on
     * <a href=https://bulbapedia.bulbagarden.net/wiki/Appendix:Black_and_White_walkthrough>this walkthrough</a>.
     */
    private static final List<String> locationTagsTraverseOrderBW = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "STRIATON CITY", "DREAMYARD", "ROUTE 3", "WELLSPRING CAVE",
            "PINWHEEL FOREST", "CASTELIA CITY", "ROUTE 4", "DESERT RESORT", "ROUTE 5", "RELIC CASTLE", "ROUTE 5",
            "DRIFTVEIL DRAWBRIDGE", "DRIFTVEIL CITY", "COLD STORAGE", "ROUTE 6", "CHARGESTONE CAVE", "ROUTE 7",
            "CELESTIAL TOWER", "ROUTE 17", "ROUTE 18", "P2 LABORATORY", "MISTRALTON CAVE", "TWIST MOUNTAIN",
            "ICIRRUS CITY", "DRAGONSPIRAL TOWER", "ROUTE 8", "MOOR OF ICIRRUS", "ROUTE 9", "ROUTE 10", "VICTORY ROAD",
            "CHALLENGER'S CAVE", "ROUTE 11", "VILLAGE BRIDGE", "ROUTE 12", "ROUTE 13", "GIANT CHASM",
            "UNDELLA TOWN", "UNDELLA BAY", "ROUTE 14", "ABUNDANT SHRINE", "ROUTE 15", "MARVELOUS BRIDGE", "ROUTE 16",
            "LOSTLORN FOREST"
    ));

    /**
     * Same order as the in-game Pokédex Habitat List.
     */
    private static final List<String> locationTagsTraverseOrderBW2 = Collections.unmodifiableList(Arrays.asList(
            "ASPERTIA CITY", "ROUTE 19", "ROUTE 20", "FLOCCESY RANCH", "VIRBANK CITY", "VIRBANK COMPLEX",
            "CASTELIA CITY", "CASTELIA SEWERS", "ROUTE 4", "DESERT RESORT", "RELIC CASTLE", "ROUTE 5", "ROUTE 16",
            "LOSTLORN FOREST", "DRIFTVEIL DRAWBRIDGE", "ROUTE 6", "RELIC PASSAGE", "CLAY TUNNEL", "MISTRALTON CAVE",
            "CHARGESTONE CAVE", "ROUTE 7", "CELESTIAL TOWER", "REVERSAL MOUNTAIN", "STRANGE HOUSE", "UNDELLA TOWN",
            "ROUTE 13", "UNDELLA BAY", "ROUTE 14", "ABUNDANT SHRINE", "ROUTE 12", "VILLAGE BRIDGE", "ROUTE 11",
            "ROUTE 9", "SEASIDE CAVE", "ROUTE 21", "ROUTE 15", "MARVELOUS BRIDGE", "HUMILAU CITY", "ROUTE 22",
            "GIANT CHASM", "ROUTE 23", "VICTORY ROAD", "ROUTE 8", "MOOR OF ICIRRUS", "ICIRRUS CITY",
            "DRAGONSPIRAL TOWER", "TWIST MOUNTAIN", "PINWHEEL FOREST", "ROUTE 3", "WELLSPRING CAVE", "STRIATON CITY",
            "DREAMYARD", "ROUTE 2", "ROUTE 1", "ROUTE 17", "ROUTE 18", "P2 LABORATORY",
            "NATURE PRESERVE"
    ));

    public static List<String> getLocationTagsTraverseOrder(int romType) {
        return romType == Type_BW ? locationTagsTraverseOrderBW : locationTagsTraverseOrderBW2;
    }

    private static void tagEncounterAreas(List<EncounterArea> encounterAreas, List<String> locationTags, int[] postGameAreas) {
        if (encounterAreas.size() != locationTags.size()) {
            throw new IllegalArgumentException("Unexpected amount of encounter areas");
        }
        for (int i = 0; i < encounterAreas.size(); i++) {
            encounterAreas.get(i).setLocationTag(locationTags.get(i));
        }
        for (int areaIndex : postGameAreas) {
            encounterAreas.get(areaIndex).setPostGame(true);
        }
    }

    public static void tagEncounterAreas(List<EncounterArea> encounterAreas, int romType, boolean useTimeOfDay) {
        List<String> locationTags;
        int[] postGameAreas;
        switch (romType) {
            case 0:
                locationTags = (useTimeOfDay ? locationTagsUseTimeBW : locationTagsNoTimeBW);
                postGameAreas = (useTimeOfDay ? bwPostGameEncounterAreasTOD : bwPostGameEncounterAreasNoTOD);
                break;
            case 1:
                locationTags = (useTimeOfDay ? locationTagsUseTimeBW2 : locationTagsNoTimeBW2);
                postGameAreas = (useTimeOfDay ? b2w2PostGameEncounterAreasTOD : b2w2PostGameEncounterAreasNoTOD);
                break;
            default:
                throw new IllegalStateException("Unexpected value for romType: " + romType);
        }
        tagEncounterAreas(encounterAreas, locationTags, postGameAreas);
    }

    public static void setForcedRivalStarterPositionsBW(List<Trainer> allTrainers) {
        allTrainers.get(287 - 1).forceStarterPosition = 0;
        allTrainers.get(288 - 1).forceStarterPosition = 0;
        allTrainers.get(289 - 1).forceStarterPosition = 0;
    }

    public static void setForcedRivalStarterPositionsBW2(List<Trainer> allTrainers) {
        //?????
        //Hugh's appearance in Pokemon World Tournament should need assigning, but it doesn't appear.
        //I'm guessing the game just uses his team from the Plasma Frigate and assigns the levels to 25?
        //Weird choice, but whatever.

        //allTrainers.get(? - 1).forceStarterPosition = 0;
        //allTrainers.get(? - 1).forceStarterPosition = 0;
        //allTrainers.get(? - 1).forceStarterPosition = 0;
    }

}
