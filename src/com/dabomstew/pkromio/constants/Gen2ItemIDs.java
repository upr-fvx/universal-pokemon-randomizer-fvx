package com.dabomstew.pkromio.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen2ItemIDs.java - defines an index number constant for every item in --*/
/*--                   the Generation 2 games.                              --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Gen2ItemIDs {

    public static final Map<Integer, Integer> internalToStandard = Stream.of(new Integer[][]{
            {0, ItemIDs.none},
            {1, ItemIDs.masterBall},
            {2, ItemIDs.ultraBall},
            {3, ItemIDs.brightPowder},
            {4, ItemIDs.greatBall},
            {5, ItemIDs.pokeBall},
            {8, ItemIDs.moonStone},
            {9, ItemIDs.antidote},
            {10, ItemIDs.burnHeal},
            {11, ItemIDs.iceHeal},
            {12, ItemIDs.awakening},
            {13, ItemIDs.paralyzeHeal},
            {14, ItemIDs.fullRestore},
            {15, ItemIDs.maxPotion},
            {16, ItemIDs.hyperPotion},
            {17, ItemIDs.superPotion},
            {18, ItemIDs.potion},
            {19, ItemIDs.escapeRope},
            {20, ItemIDs.repel},
            {21, ItemIDs.maxElixir},
            {22, ItemIDs.fireStone},
            {23, ItemIDs.thunderStone},
            {24, ItemIDs.waterStone},
            {26, ItemIDs.hpUp},
            {27, ItemIDs.protein},
            {28, ItemIDs.iron},
            {29, ItemIDs.carbos},
            {30, ItemIDs.luckyPunch},
            {31, ItemIDs.calcium},
            {32, ItemIDs.rareCandy},
            {33, ItemIDs.xAccuracy},
            {34, ItemIDs.leafStone},
            {35, ItemIDs.metalPowder},
            {36, ItemIDs.nugget},
            {37, ItemIDs.pokeDoll},
            {38, ItemIDs.fullHeal},
            {39, ItemIDs.revive},
            {40, ItemIDs.maxRevive},
            {41, ItemIDs.guardSpec},
            {42, ItemIDs.superRepel},
            {43, ItemIDs.maxRepel},
            {44, ItemIDs.direHit},
            {46, ItemIDs.freshWater},
            {47, ItemIDs.sodaPop},
            {48, ItemIDs.lemonade},
            {49, ItemIDs.xAttack},
            {51, ItemIDs.xDefense},
            {52, ItemIDs.xSpeed},
            {57, ItemIDs.expShare},
            {62, ItemIDs.ppUp},
            {63, ItemIDs.ether},
            {64, ItemIDs.maxEther},
            {65, ItemIDs.elixir},
            {72, ItemIDs.moomooMilk},
            {73, ItemIDs.quickClaw},
            {74, ItemIDs.pechaBerry}, // PSNCureBerry
            {76, ItemIDs.softSand},
            {77, ItemIDs.sharpBeak},
            {78, ItemIDs.cheriBerry}, // PRZCureBerry
            {79, ItemIDs.aspearBerry}, // Burnt Berry
            {80, ItemIDs.rawstBerry}, // Ice Berry
            {81, ItemIDs.poisonBarb},
            {82, ItemIDs.kingsRock},
            {83, ItemIDs.persimBerry}, // Bitter Berry
            {84, ItemIDs.chestoBerry}, // Mint Berry
            {85, ItemIDs.redApricorn},
            {86, ItemIDs.tinyMushroom},
            {87, ItemIDs.bigMushroom},
            {88, ItemIDs.silverPowder},
            {89, ItemIDs.blueApricorn},
            {91, ItemIDs.amuletCoin},
            {92, ItemIDs.yellowApricorn},
            {93, ItemIDs.greenApricorn},
            {94, ItemIDs.cleanseTag},
            {95, ItemIDs.mysticWater},
            {96, ItemIDs.twistedSpoon},
            {97, ItemIDs.whiteApricorn},
            {98, ItemIDs.blackBelt},
            {99, ItemIDs.blackApricorn},
            {101, ItemIDs.pinkApricorn},
            {102, ItemIDs.blackGlasses},
            {103, ItemIDs.slowpokeTail},
            {105, ItemIDs.leek}, // Stick
            {106, ItemIDs.smokeBall},
            {107, ItemIDs.neverMeltIce},
            {108, ItemIDs.magnet},
            {109, ItemIDs.lumBerry}, // Miracle Berry
            {110, ItemIDs.pearl},
            {111, ItemIDs.bigPearl},
            {112, ItemIDs.everstone},
            {113, ItemIDs.spellTag},
            {114, ItemIDs.rageCandyBar},
            {117, ItemIDs.miracleSeed},
            {118, ItemIDs.thickClub},
            {119, ItemIDs.focusBand},
            {121, ItemIDs.energyPowder},
            {122, ItemIDs.energyRoot},
            {123, ItemIDs.healPowder},
            {124, ItemIDs.revivalHerb},
            {125, ItemIDs.hardStone},
            {126, ItemIDs.luckyEgg},
            {131, ItemIDs.stardust},
            {132, ItemIDs.starPiece},
            {138, ItemIDs.charcoal},
            {139, ItemIDs.berryJuice},
            {140, ItemIDs.scopeLens},
            {143, ItemIDs.metalCoat},
            {144, ItemIDs.dragonFang},
            {150, ItemIDs.leppaBerry}, // MysteryBerry
            {151, ItemIDs.dragonScale},
            {156, ItemIDs.sacredAsh},
            {157, ItemIDs.heavyBall},
            {158, ItemIDs.mail1}, // Flower Mail
            {159, ItemIDs.levelBall},
            {160, ItemIDs.lureBall},
            {161, ItemIDs.fastBall},
            {163, ItemIDs.lightBall},
            {164, ItemIDs.friendBall},
            {165, ItemIDs.moonBall},
            {166, ItemIDs.loveBall},
            {169, ItemIDs.sunStone},
            {172, ItemIDs.upgrade},
            {173, ItemIDs.oranBerry}, // Berry
            {174, ItemIDs.sitrusBerry}, // Gold Berry
            {177, ItemIDs.parkBall},
            {181, ItemIDs.mail2}, // Surf Mail
            {182, ItemIDs.mail3}, // Liteblue Mail
            {183, ItemIDs.mail4}, // Portrait Mail
            {184, ItemIDs.mail5}, // Lovely Mail
            {185, ItemIDs.mail6}, // Eon Mail
            {186, ItemIDs.mail7}, // Morph Mail
            {187, ItemIDs.mail8}, // Bluesky Mail
            {188, ItemIDs.mail9}, // Music Mail
            {189, ItemIDs.mail10}, // Mirage Mail
            {191, ItemIDs.tm01},
            {192, ItemIDs.tm02},
            {193, ItemIDs.tm03},
            {194, ItemIDs.tm04},
            {196, ItemIDs.tm05},
            {197, ItemIDs.tm06},
            {198, ItemIDs.tm07},
            {199, ItemIDs.tm08},
            {200, ItemIDs.tm09},
            {201, ItemIDs.tm10},
            {202, ItemIDs.tm11},
            {203, ItemIDs.tm12},
            {204, ItemIDs.tm13},
            {205, ItemIDs.tm14},
            {206, ItemIDs.tm15},
            {207, ItemIDs.tm16},
            {208, ItemIDs.tm17},
            {209, ItemIDs.tm18},
            {210, ItemIDs.tm19},
            {211, ItemIDs.tm20},
            {212, ItemIDs.tm21},
            {213, ItemIDs.tm22},
            {214, ItemIDs.tm23},
            {215, ItemIDs.tm24},
            {216, ItemIDs.tm25},
            {217, ItemIDs.tm26},
            {218, ItemIDs.tm27},
            {219, ItemIDs.tm28},
            {221, ItemIDs.tm29},
            {222, ItemIDs.tm30},
            {223, ItemIDs.tm31},
            {224, ItemIDs.tm32},
            {225, ItemIDs.tm33},
            {226, ItemIDs.tm34},
            {227, ItemIDs.tm35},
            {228, ItemIDs.tm36},
            {229, ItemIDs.tm37},
            {230, ItemIDs.tm38},
            {231, ItemIDs.tm39},
            {232, ItemIDs.tm40},
            {233, ItemIDs.tm41},
            {234, ItemIDs.tm42},
            {235, ItemIDs.tm43},
            {236, ItemIDs.tm44},
            {237, ItemIDs.tm45},
            {238, ItemIDs.tm46},
            {239, ItemIDs.tm47},
            {240, ItemIDs.tm48},
            {241, ItemIDs.tm49},
            {242, ItemIDs.tm50},
            {243, ItemIDs.hm01},
            {244, ItemIDs.hm02},
            {245, ItemIDs.hm03},
            {246, ItemIDs.hm04},
            {247, ItemIDs.hm05},
            {248, ItemIDs.hm06},
            {249, ItemIDs.hm07},
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

    public static final Map<Integer, Integer> standardToInternal = internalToStandard.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // https://bulbapedia.bulbagarden.net/wiki/List_of_items_by_index_number_(Generation_II)
    public static final int nothing = 0;
    public static final int masterBall = 1;
    public static final int ultraBall = 2;
    public static final int brightPowder = 3;
    public static final int greatBall = 4;
    public static final int pokeBall = 5;
    public static final int terusama6 = 6;
    public static final int bicycle = 7;
    public static final int moonStone = 8;
    public static final int antidote = 9;
    public static final int burnHeal = 10;
    public static final int iceHeal = 11;
    public static final int awakening = 12;
    public static final int parlyzHeal = 13;
    public static final int fullRestore = 14;
    public static final int maxPotion = 15;
    public static final int hyperPotion = 16;
    public static final int superPotion = 17;
    public static final int potion = 18;
    public static final int escapeRope = 19;
    public static final int repel = 20;
    public static final int maxElixer = 21;
    public static final int fireStone = 22;
    public static final int thunderstone = 23;
    public static final int waterStone = 24;
    public static final int terusama25 = 25;
    public static final int hpUp = 26;
    public static final int protein = 27;
    public static final int iron = 28;
    public static final int carbos = 29;
    public static final int luckyPunch = 30;
    public static final int calcium = 31;
    public static final int rareCandy = 32;
    public static final int xAccuracy = 33;
    public static final int leafStone = 34;
    public static final int metalPowder = 35;
    public static final int nugget = 36;
    public static final int pokeDoll = 37;
    public static final int fullHeal = 38;
    public static final int revive = 39;
    public static final int maxRevive = 40;
    public static final int guardSpec = 41;
    public static final int superRepel = 42;
    public static final int maxRepel = 43;
    public static final int direHit = 44;
    public static final int terusama45 = 45;
    public static final int freshWater = 46;
    public static final int sodaPop = 47;
    public static final int lemonade = 48;
    public static final int xAttack = 49;
    public static final int terusama50 = 50;
    public static final int xDefend = 51;
    public static final int xSpeed = 52;
    public static final int xSpecial = 53;
    public static final int coinCase = 54;
    public static final int itemfinder = 55;
    public static final int terusama56 = 56;
    public static final int expShare = 57;
    public static final int oldRod = 58;
    public static final int goodRod = 59;
    public static final int silverLeaf = 60;
    public static final int superRod = 61;
    public static final int ppUp = 62;
    public static final int ether = 63;
    public static final int maxEther = 64;
    public static final int elixer = 65;
    public static final int redScale = 66;
    public static final int secretPotion = 67;
    public static final int ssTicket = 68;
    public static final int mysteryEgg = 69;
    public static final int clearBell = 70; // exclusive to Crystal
    public static final int silverWing = 71;
    public static final int moomooMilk = 72;
    public static final int quickClaw = 73;
    public static final int psnCureBerry = 74;
    public static final int goldLeaf = 75;
    public static final int softSand = 76;
    public static final int sharpBeak = 77;
    public static final int przCureBerry = 78;
    public static final int burntBerry = 79;
    public static final int iceBerry = 80;
    public static final int poisonBarb = 81;
    public static final int kingsRock = 82;
    public static final int bitterBerry = 83;
    public static final int mintBerry = 84;
    public static final int redApricorn = 85;
    public static final int tinyMushroom = 86;
    public static final int bigMushroom = 87;
    public static final int silverPowder = 88;
    public static final int bluApricorn = 89;
    public static final int terusama90 = 90;
    public static final int amuletCoin = 91;
    public static final int ylwApricorn = 92;
    public static final int grnApricorn = 93;
    public static final int cleanseTag = 94;
    public static final int mysticWater = 95;
    public static final int twistedSpoon = 96;
    public static final int whtApricorn = 97;
    public static final int blackbelt = 98;
    public static final int blkApricorn = 99;
    public static final int terusama100 = 100;
    public static final int pnkApricorn = 101;
    public static final int blackGlasses = 102;
    public static final int slowpokeTail = 103;
    public static final int pinkBow = 104;
    public static final int stick = 105;
    public static final int smokeBall = 106;
    public static final int neverMeltIce = 107;
    public static final int magnet = 108;
    public static final int miracleBerry = 109;
    public static final int pearl = 110;
    public static final int bigPearl = 111;
    public static final int everstone = 112;
    public static final int spellTag = 113;
    public static final int rageCandyBar = 114;
    public static final int gsBall = 115; // exclusive to Crystal
    public static final int blueCard = 116; // exclusive to Crystal
    public static final int miracleSeed = 117;
    public static final int thickClub = 118;
    public static final int focusBand = 119;
    public static final int terusama120 = 120;
    public static final int energyPowder = 121;
    public static final int energyRoot = 122;
    public static final int healPowder = 123;
    public static final int revivalHerb = 124;
    public static final int hardStone = 125;
    public static final int luckyEgg = 126;
    public static final int cardKey = 127;
    public static final int machinePart = 128;
    public static final int eggTicket = 129; // exclusive to Crystal
    public static final int lostItem = 130;
    public static final int stardust = 131;
    public static final int starPiece = 132;
    public static final int basementKey = 133;
    public static final int pass = 134;
    public static final int terusama135 = 135;
    public static final int terusama136 = 136;
    public static final int terusama137 = 137;
    public static final int charcoal = 138;
    public static final int berryJuice = 139;
    public static final int scopeLens = 140;
    public static final int terusama141 = 141;
    public static final int terusama142 = 142;
    public static final int metalCoat = 143;
    public static final int dragonFang = 144;
    public static final int terusama145 = 145;
    public static final int leftovers = 146;
    public static final int terusama147 = 147;
    public static final int terusama148 = 148;
    public static final int terusama149 = 149;
    public static final int mysteryBerry = 150;
    public static final int dragonScale = 151;
    public static final int berserkGene = 152;
    public static final int terusama153 = 153;
    public static final int terusama154 = 154;
    public static final int terusama155 = 155;
    public static final int sacredAsh = 156;
    public static final int heavyBall = 157;
    public static final int flowerMail = 158;
    public static final int levelBall = 159;
    public static final int lureBall = 160;
    public static final int fastBall = 161;
    public static final int terusama162 = 162;
    public static final int lightBall = 163;
    public static final int friendBall = 164;
    public static final int moonBall = 165;
    public static final int loveBall = 166;
    public static final int normalBox = 167;
    public static final int gorgeousBox = 168;
    public static final int sunStone = 169;
    public static final int polkadotBow = 170;
    public static final int terusama171 = 171;
    public static final int upGrade = 172;
    public static final int berry = 173;
    public static final int goldBerry = 174;
    public static final int squirtBottle = 175;
    public static final int terusama176 = 176;
    public static final int parkBall = 177;
    public static final int rainbowWing = 178;
    public static final int terusama179 = 179;
    public static final int brickPiece = 180;
    public static final int surfMail = 181;
    public static final int litebluemail = 182;
    public static final int portraitmail = 183;
    public static final int lovelyMail = 184;
    public static final int eonMail = 185;
    public static final int morphMail = 186;
    public static final int blueskyMail = 187;
    public static final int musicMail = 188;
    public static final int mirageMail = 189;
    public static final int terusama190 = 190;
    public static final int tm01 = 191;
    public static final int tm02 = 192;
    public static final int tm03 = 193;
    public static final int tm04 = 194;
    public static final int tm04Unused = 195;
    public static final int tm05 = 196;
    public static final int tm06 = 197;
    public static final int tm07 = 198;
    public static final int tm08 = 199;
    public static final int tm09 = 200;
    public static final int tm10 = 201;
    public static final int tm11 = 202;
    public static final int tm12 = 203;
    public static final int tm13 = 204;
    public static final int tm14 = 205;
    public static final int tm15 = 206;
    public static final int tm16 = 207;
    public static final int tm17 = 208;
    public static final int tm18 = 209;
    public static final int tm19 = 210;
    public static final int tm20 = 211;
    public static final int tm21 = 212;
    public static final int tm22 = 213;
    public static final int tm23 = 214;
    public static final int tm24 = 215;
    public static final int tm25 = 216;
    public static final int tm26 = 217;
    public static final int tm27 = 218;
    public static final int tm28 = 219;
    public static final int tm28Unused = 220;
    public static final int tm29 = 221;
    public static final int tm30 = 222;
    public static final int tm31 = 223;
    public static final int tm32 = 224;
    public static final int tm33 = 225;
    public static final int tm34 = 226;
    public static final int tm35 = 227;
    public static final int tm36 = 228;
    public static final int tm37 = 229;
    public static final int tm38 = 230;
    public static final int tm39 = 231;
    public static final int tm40 = 232;
    public static final int tm41 = 233;
    public static final int tm42 = 234;
    public static final int tm43 = 235;
    public static final int tm44 = 236;
    public static final int tm45 = 237;
    public static final int tm46 = 238;
    public static final int tm47 = 239;
    public static final int tm48 = 240;
    public static final int tm49 = 241;
    public static final int tm50 = 242;
    public static final int hm01 = 243;
    public static final int hm02 = 244;
    public static final int hm03 = 245;
    public static final int hm04 = 246;
    public static final int hm05 = 247;
    public static final int hm06 = 248;
    public static final int hm07 = 249;
    public static final int hm08 = 250;
    public static final int hm09 = 251;
    public static final int hm10 = 252;
    public static final int hm11 = 253;
    public static final int hm12 = 254;
    public static final int cancel = 255;
}
