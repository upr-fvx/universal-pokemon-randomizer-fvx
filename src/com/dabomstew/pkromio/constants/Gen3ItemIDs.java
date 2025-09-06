package com.dabomstew.pkromio.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen3ItemIDs.java - defines an index number constant for every item in --*/
/*--                   the Generation 3 games.                              --*/
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
public class Gen3ItemIDs {

    public static final Map<Integer, Integer> internalToStandard = Stream.of(new Integer[][]{
            {0, ItemIDs.none},
            {1, ItemIDs.masterBall},
            {2, ItemIDs.ultraBall},
            {3, ItemIDs.greatBall},
            {4, ItemIDs.pokeBall},
            {5, ItemIDs.safariBall},
            {6, ItemIDs.netBall},
            {7, ItemIDs.diveBall},
            {8, ItemIDs.nestBall},
            {9, ItemIDs.repeatBall},
            {10, ItemIDs.timerBall},
            {11, ItemIDs.luxuryBall},
            {12, ItemIDs.premierBall},
            {13, ItemIDs.potion},
            {14, ItemIDs.antidote},
            {15, ItemIDs.burnHeal},
            {16, ItemIDs.iceHeal},
            {17, ItemIDs.awakening},
            {18, ItemIDs.paralyzeHeal},
            {19, ItemIDs.fullRestore},
            {20, ItemIDs.maxPotion},
            {21, ItemIDs.hyperPotion},
            {22, ItemIDs.superPotion},
            {23, ItemIDs.fullHeal},
            {24, ItemIDs.revive},
            {25, ItemIDs.maxRevive},
            {26, ItemIDs.freshWater},
            {27, ItemIDs.sodaPop},
            {28, ItemIDs.lemonade},
            {29, ItemIDs.moomooMilk},
            {30, ItemIDs.energyPowder},
            {31, ItemIDs.energyRoot},
            {32, ItemIDs.healPowder},
            {33, ItemIDs.revivalHerb},
            {34, ItemIDs.ether},
            {35, ItemIDs.maxEther},
            {36, ItemIDs.elixir},
            {37, ItemIDs.maxElixir},
            {38, ItemIDs.lavaCookie},
            {39, ItemIDs.blueFlute},
            {40, ItemIDs.yellowFlute},
            {41, ItemIDs.redFlute},
            {42, ItemIDs.blackFlute},
            {43, ItemIDs.whiteFlute},
            {44, ItemIDs.berryJuice},
            {45, ItemIDs.sacredAsh},
            {46, ItemIDs.shoalSalt},
            {47, ItemIDs.shoalShell},
            {48, ItemIDs.redShard},
            {49, ItemIDs.blueShard},
            {50, ItemIDs.yellowShard},
            {51, ItemIDs.greenShard},
            {63, ItemIDs.hpUp},
            {64, ItemIDs.protein},
            {65, ItemIDs.iron},
            {66, ItemIDs.carbos},
            {67, ItemIDs.calcium},
            {68, ItemIDs.rareCandy},
            {69, ItemIDs.ppUp},
            {70, ItemIDs.zinc},
            {71, ItemIDs.ppMax},
            {73, ItemIDs.guardSpec},
            {74, ItemIDs.direHit},
            {75, ItemIDs.xAttack},
            {76, ItemIDs.xDefense},
            {77, ItemIDs.xSpeed},
            {78, ItemIDs.xAccuracy},
            {79, ItemIDs.xSpAtk}, // X Special
            {80, ItemIDs.pokeDoll},
            {81, ItemIDs.fluffyTail},
            {83, ItemIDs.superRepel},
            {84, ItemIDs.maxRepel},
            {85, ItemIDs.escapeRope},
            {86, ItemIDs.repel},
            {93, ItemIDs.sunStone},
            {94, ItemIDs.moonStone},
            {95, ItemIDs.fireStone},
            {96, ItemIDs.thunderStone},
            {97, ItemIDs.waterStone},
            {98, ItemIDs.leafStone},
            {103, ItemIDs.tinyMushroom},
            {104, ItemIDs.bigMushroom},
            {106, ItemIDs.pearl},
            {107, ItemIDs.bigPearl},
            {108, ItemIDs.stardust},
            {109, ItemIDs.starPiece},
            {110, ItemIDs.nugget},
            {111, ItemIDs.heartScale},
            {121, ItemIDs.mail1}, // Orange Mail
            {122, ItemIDs.mail2}, // Harbor Mail
            {123, ItemIDs.mail3}, // Glitter Mail
            {124, ItemIDs.mail4}, // Mech Mail
            {125, ItemIDs.mail5}, // Wood Mail
            {126, ItemIDs.mail6}, // Wave Mail
            {127, ItemIDs.mail7}, // Bead Mail
            {128, ItemIDs.mail8}, // Shadow Mail
            {129, ItemIDs.mail9}, // Tropic Mail
            {130, ItemIDs.mail10}, // Dream Mail
            {131, ItemIDs.mail11}, // Fab Mail
            {132, ItemIDs.mail12}, // Retro Mail
            {133, ItemIDs.cheriBerry},
            {134, ItemIDs.chestoBerry},
            {135, ItemIDs.pechaBerry},
            {136, ItemIDs.rawstBerry},
            {137, ItemIDs.aspearBerry},
            {138, ItemIDs.leppaBerry},
            {139, ItemIDs.oranBerry},
            {140, ItemIDs.persimBerry},
            {141, ItemIDs.lumBerry},
            {142, ItemIDs.sitrusBerry},
            {143, ItemIDs.figyBerry},
            {144, ItemIDs.wikiBerry},
            {145, ItemIDs.magoBerry},
            {146, ItemIDs.aguavBerry},
            {147, ItemIDs.iapapaBerry},
            {148, ItemIDs.razzBerry},
            {149, ItemIDs.blukBerry},
            {150, ItemIDs.nanabBerry},
            {151, ItemIDs.wepearBerry},
            {152, ItemIDs.pinapBerry},
            {153, ItemIDs.pomegBerry},
            {154, ItemIDs.kelpsyBerry},
            {155, ItemIDs.qualotBerry},
            {156, ItemIDs.hondewBerry},
            {157, ItemIDs.grepaBerry},
            {158, ItemIDs.tamatoBerry},
            {159, ItemIDs.cornnBerry},
            {160, ItemIDs.magostBerry},
            {161, ItemIDs.rabutaBerry},
            {162, ItemIDs.nomelBerry},
            {163, ItemIDs.spelonBerry},
            {164, ItemIDs.pamtreBerry},
            {165, ItemIDs.watmelBerry},
            {166, ItemIDs.durinBerry},
            {167, ItemIDs.belueBerry},
            {168, ItemIDs.liechiBerry},
            {169, ItemIDs.ganlonBerry},
            {170, ItemIDs.salacBerry},
            {171, ItemIDs.petayaBerry},
            {172, ItemIDs.apicotBerry},
            {173, ItemIDs.lansatBerry},
            {174, ItemIDs.starfBerry},
            {175, ItemIDs.enigmaBerry},
            {179, ItemIDs.brightPowder},
            {180, ItemIDs.whiteHerb},
            {181, ItemIDs.machoBrace},
            {182, ItemIDs.expShare},
            {183, ItemIDs.quickClaw},
            {184, ItemIDs.sootheBell},
            {185, ItemIDs.mentalHerb},
            {186, ItemIDs.choiceBand},
            {187, ItemIDs.kingsRock},
            {188, ItemIDs.silverPowder},
            {189, ItemIDs.amuletCoin},
            {190, ItemIDs.cleanseTag},
            {191, ItemIDs.soulDew},
            {192, ItemIDs.deepSeaTooth},
            {193, ItemIDs.deepSeaScale},
            {194, ItemIDs.smokeBall},
            {195, ItemIDs.everstone},
            {196, ItemIDs.focusBand},
            {197, ItemIDs.luckyEgg},
            {198, ItemIDs.scopeLens},
            {199, ItemIDs.metalCoat},
            {200, ItemIDs.leftovers},
            {201, ItemIDs.dragonScale},
            {202, ItemIDs.lightBall},
            {203, ItemIDs.softSand},
            {204, ItemIDs.hardStone},
            {205, ItemIDs.miracleSeed},
            {206, ItemIDs.blackGlasses},
            {207, ItemIDs.blackBelt},
            {208, ItemIDs.magnet},
            {209, ItemIDs.mysticWater},
            {210, ItemIDs.sharpBeak},
            {211, ItemIDs.poisonBarb},
            {212, ItemIDs.neverMeltIce},
            {213, ItemIDs.spellTag},
            {214, ItemIDs.twistedSpoon},
            {215, ItemIDs.charcoal},
            {216, ItemIDs.dragonFang},
            {217, ItemIDs.silkScarf},
            {218, ItemIDs.upgrade},
            {219, ItemIDs.shellBell},
            {220, ItemIDs.seaIncense},
            {221, ItemIDs.laxIncense},
            {222, ItemIDs.luckyPunch},
            {223, ItemIDs.metalPowder},
            {224, ItemIDs.thickClub},
            {225, ItemIDs.leek}, // Stick
            {254, ItemIDs.redScarf},
            {255, ItemIDs.blueScarf},
            {256, ItemIDs.pinkScarf},
            {257, ItemIDs.greenScarf},
            {258, ItemIDs.yellowScarf},
            {289, ItemIDs.tm01},
            {290, ItemIDs.tm02},
            {291, ItemIDs.tm03},
            {292, ItemIDs.tm04},
            {293, ItemIDs.tm05},
            {294, ItemIDs.tm06},
            {295, ItemIDs.tm07},
            {296, ItemIDs.tm08},
            {297, ItemIDs.tm09},
            {298, ItemIDs.tm10},
            {299, ItemIDs.tm11},
            {300, ItemIDs.tm12},
            {301, ItemIDs.tm13},
            {302, ItemIDs.tm14},
            {303, ItemIDs.tm15},
            {304, ItemIDs.tm16},
            {305, ItemIDs.tm17},
            {306, ItemIDs.tm18},
            {307, ItemIDs.tm19},
            {308, ItemIDs.tm20},
            {309, ItemIDs.tm21},
            {310, ItemIDs.tm22},
            {311, ItemIDs.tm23},
            {312, ItemIDs.tm24},
            {313, ItemIDs.tm25},
            {314, ItemIDs.tm26},
            {315, ItemIDs.tm27},
            {316, ItemIDs.tm28},
            {317, ItemIDs.tm29},
            {318, ItemIDs.tm30},
            {319, ItemIDs.tm31},
            {320, ItemIDs.tm32},
            {321, ItemIDs.tm33},
            {322, ItemIDs.tm34},
            {323, ItemIDs.tm35},
            {324, ItemIDs.tm36},
            {325, ItemIDs.tm37},
            {326, ItemIDs.tm38},
            {327, ItemIDs.tm39},
            {328, ItemIDs.tm40},
            {329, ItemIDs.tm41},
            {330, ItemIDs.tm42},
            {331, ItemIDs.tm43},
            {332, ItemIDs.tm44},
            {333, ItemIDs.tm45},
            {334, ItemIDs.tm46},
            {335, ItemIDs.tm47},
            {336, ItemIDs.tm48},
            {337, ItemIDs.tm49},
            {338, ItemIDs.tm50},
            {339, ItemIDs.hm01},
            {340, ItemIDs.hm02},
            {341, ItemIDs.hm03},
            {342, ItemIDs.hm04},
            {343, ItemIDs.hm05},
            {344, ItemIDs.hm06},
            {345, ItemIDs.hm07},
            {346, ItemIDs.hm08},
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

    public static final Map<Integer, Integer> standardToInternal = internalToStandard.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // https://bulbapedia.bulbagarden.net/wiki/List_of_items_by_index_number_(Generation_III)
    public static final int nothing = 0;
    public static final int masterBall = 1;
    public static final int ultraBall = 2;
    public static final int greatBall = 3;
    public static final int pokeBall = 4;
    public static final int safariBall = 5;
    public static final int netBall = 6;
    public static final int diveBall = 7;
    public static final int nestBall = 8;
    public static final int repeatBall = 9;
    public static final int timerBall = 10;
    public static final int luxuryBall = 11;
    public static final int premierBall = 12;
    public static final int potion = 13;
    public static final int antidote = 14;
    public static final int burnHeal = 15;
    public static final int iceHeal = 16;
    public static final int awakening = 17;
    public static final int parlyzHeal = 18;
    public static final int fullRestore = 19;
    public static final int maxPotion = 20;
    public static final int hyperPotion = 21;
    public static final int superPotion = 22;
    public static final int fullHeal = 23;
    public static final int revive = 24;
    public static final int maxRevive = 25;
    public static final int freshWater = 26;
    public static final int sodaPop = 27;
    public static final int lemonade = 28;
    public static final int moomooMilk = 29;
    public static final int energyPowder = 30;
    public static final int energyRoot = 31;
    public static final int healPowder = 32;
    public static final int revivalHerb = 33;
    public static final int ether = 34;
    public static final int maxEther = 35;
    public static final int elixir = 36;
    public static final int maxElixir = 37;
    public static final int lavaCookie = 38;
    public static final int blueFlute = 39;
    public static final int yellowFlute = 40;
    public static final int redFlute = 41;
    public static final int blackFlute = 42;
    public static final int whiteFlute = 43;
    public static final int berryJuice = 44;
    public static final int sacredAsh = 45;
    public static final int shoalSalt = 46;
    public static final int shoalShell = 47;
    public static final int redShard = 48;
    public static final int blueShard = 49;
    public static final int yellowShard = 50;
    public static final int greenShard = 51;
    public static final int unknown52 = 52;
    public static final int unknown53 = 53;
    public static final int unknown54 = 54;
    public static final int unknown55 = 55;
    public static final int unknown56 = 56;
    public static final int unknown57 = 57;
    public static final int unknown58 = 58;
    public static final int unknown59 = 59;
    public static final int unknown60 = 60;
    public static final int unknown61 = 61;
    public static final int unknown62 = 62;
    public static final int hpUp = 63;
    public static final int protein = 64;
    public static final int iron = 65;
    public static final int carbos = 66;
    public static final int calcium = 67;
    public static final int rareCandy = 68;
    public static final int ppUp = 69;
    public static final int zinc = 70;
    public static final int ppMax = 71;
    public static final int unknown72 = 72;
    public static final int guardSpec = 73;
    public static final int direHit = 74;
    public static final int xAttack = 75;
    public static final int xDefend = 76;
    public static final int xSpeed = 77;
    public static final int xAccuracy = 78;
    public static final int xSpecial = 79;
    public static final int pokeDoll = 80;
    public static final int fluffyTail = 81;
    public static final int unknown82 = 82;
    public static final int superRepel = 83;
    public static final int maxRepel = 84;
    public static final int escapeRope = 85;
    public static final int repel = 86;
    public static final int unknown87 = 87;
    public static final int unknown88 = 88;
    public static final int unknown89 = 89;
    public static final int unknown90 = 90;
    public static final int unknown91 = 91;
    public static final int unknown92 = 92;
    public static final int sunStone = 93;
    public static final int moonStone = 94;
    public static final int fireStone = 95;
    public static final int thunderstone = 96;
    public static final int waterStone = 97;
    public static final int leafStone = 98;
    public static final int unknown99 = 99;
    public static final int unknown100 = 100;
    public static final int unknown101 = 101;
    public static final int unknown102 = 102;
    public static final int tinyMushroom = 103;
    public static final int bigMushroom = 104;
    public static final int unknown105 = 105;
    public static final int pearl = 106;
    public static final int bigPearl = 107;
    public static final int stardust = 108;
    public static final int starPiece = 109;
    public static final int nugget = 110;
    public static final int heartScale = 111;
    public static final int unknown112 = 112;
    public static final int unknown113 = 113;
    public static final int unknown114 = 114;
    public static final int unknown115 = 115;
    public static final int unknown116 = 116;
    public static final int unknown117 = 117;
    public static final int unknown118 = 118;
    public static final int unknown119 = 119;
    public static final int unknown120 = 120;
    public static final int orangeMail = 121;
    public static final int harborMail = 122;
    public static final int glitterMail = 123;
    public static final int mechMail = 124;
    public static final int woodMail = 125;
    public static final int waveMail = 126;
    public static final int beadMail = 127;
    public static final int shadowMail = 128;
    public static final int tropicMail = 129;
    public static final int dreamMail = 130;
    public static final int fabMail = 131;
    public static final int retroMail = 132;
    public static final int cheriBerry = 133;
    public static final int chestoBerry = 134;
    public static final int pechaBerry = 135;
    public static final int rawstBerry = 136;
    public static final int aspearBerry = 137;
    public static final int leppaBerry = 138;
    public static final int oranBerry = 139;
    public static final int persimBerry = 140;
    public static final int lumBerry = 141;
    public static final int sitrusBerry = 142;
    public static final int figyBerry = 143;
    public static final int wikiBerry = 144;
    public static final int magoBerry = 145;
    public static final int aguavBerry = 146;
    public static final int iapapaBerry = 147;
    public static final int razzBerry = 148;
    public static final int blukBerry = 149;
    public static final int nanabBerry = 150;
    public static final int wepearBerry = 151;
    public static final int pinapBerry = 152;
    public static final int pomegBerry = 153;
    public static final int kelpsyBerry = 154;
    public static final int qualotBerry = 155;
    public static final int hondewBerry = 156;
    public static final int grepaBerry = 157;
    public static final int tamatoBerry = 158;
    public static final int cornnBerry = 159;
    public static final int magostBerry = 160;
    public static final int rabutaBerry = 161;
    public static final int nomelBerry = 162;
    public static final int spelonBerry = 163;
    public static final int pamtreBerry = 164;
    public static final int watmelBerry = 165;
    public static final int durinBerry = 166;
    public static final int belueBerry = 167;
    public static final int liechiBerry = 168;
    public static final int ganlonBerry = 169;
    public static final int salacBerry = 170;
    public static final int petayaBerry = 171;
    public static final int apicotBerry = 172;
    public static final int lansatBerry = 173;
    public static final int starfBerry = 174;
    public static final int enigmaBerry = 175;
    public static final int unknown176 = 176;
    public static final int unknown177 = 177;
    public static final int unknown178 = 178;
    public static final int brightPowder = 179;
    public static final int whiteHerb = 180;
    public static final int machoBrace = 181;
    public static final int expShare = 182;
    public static final int quickClaw = 183;
    public static final int sootheBell = 184;
    public static final int mentalHerb = 185;
    public static final int choiceBand = 186;
    public static final int kingsRock = 187;
    public static final int silverPowder = 188;
    public static final int amuletCoin = 189;
    public static final int cleanseTag = 190;
    public static final int soulDew = 191;
    public static final int deepSeaTooth = 192;
    public static final int deepSeaScale = 193;
    public static final int smokeBall = 194;
    public static final int everstone = 195;
    public static final int focusBand = 196;
    public static final int luckyEgg = 197;
    public static final int scopeLens = 198;
    public static final int metalCoat = 199;
    public static final int leftovers = 200;
    public static final int dragonScale = 201;
    public static final int lightBall = 202;
    public static final int softSand = 203;
    public static final int hardStone = 204;
    public static final int miracleSeed = 205;
    public static final int blackGlasses = 206;
    public static final int blackBelt = 207;
    public static final int magnet = 208;
    public static final int mysticWater = 209;
    public static final int sharpBeak = 210;
    public static final int poisonBarb = 211;
    public static final int neverMeltIce = 212;
    public static final int spellTag = 213;
    public static final int twistedSpoon = 214;
    public static final int charcoal = 215;
    public static final int dragonFang = 216;
    public static final int silkScarf = 217;
    public static final int upGrade = 218;
    public static final int shellBell = 219;
    public static final int seaIncense = 220;
    public static final int laxIncense = 221;
    public static final int luckyPunch = 222;
    public static final int metalPowder = 223;
    public static final int thickClub = 224;
    public static final int stick = 225;
    public static final int unknown226 = 226;
    public static final int unknown227 = 227;
    public static final int unknown228 = 228;
    public static final int unknown229 = 229;
    public static final int unknown230 = 230;
    public static final int unknown231 = 231;
    public static final int unknown232 = 232;
    public static final int unknown233 = 233;
    public static final int unknown234 = 234;
    public static final int unknown235 = 235;
    public static final int unknown236 = 236;
    public static final int unknown237 = 237;
    public static final int unknown238 = 238;
    public static final int unknown239 = 239;
    public static final int unknown240 = 240;
    public static final int unknown241 = 241;
    public static final int unknown242 = 242;
    public static final int unknown243 = 243;
    public static final int unknown244 = 244;
    public static final int unknown245 = 245;
    public static final int unknown246 = 246;
    public static final int unknown247 = 247;
    public static final int unknown248 = 248;
    public static final int unknown249 = 249;
    public static final int unknown250 = 250;
    public static final int unknown251 = 251;
    public static final int unknown252 = 252;
    public static final int unknown253 = 253;
    public static final int redScarf = 254;
    public static final int blueScarf = 255;
    public static final int pinkScarf = 256;
    public static final int greenScarf = 257;
    public static final int yellowScarf = 258;
    public static final int machBike = 259;
    public static final int coinCase = 260;
    public static final int itemfinder = 261;
    public static final int oldRod = 262;
    public static final int goodRod = 263;
    public static final int superRod = 264;
    public static final int ssTicket = 265;
    public static final int contestPass = 266;
    public static final int unknown267 = 267;
    public static final int wailmerPail = 268;
    public static final int devonGoods = 269;
    public static final int sootSack = 270;
    public static final int basementKey = 271;
    public static final int acroBike = 272;
    public static final int pokeblockCase = 273;
    public static final int letter = 274;
    public static final int eonTicket = 275;
    public static final int redOrb = 276;
    public static final int blueOrb = 277;
    public static final int scanner = 278;
    public static final int goGoggles = 279;
    public static final int meteorite = 280;
    public static final int rm1Key = 281;
    public static final int rm2Key = 282;
    public static final int rm4Key = 283;
    public static final int rm6Key = 284;
    public static final int storageKey = 285;
    public static final int rootFossil = 286;
    public static final int clawFossil = 287;
    public static final int devonScope = 288;
    public static final int tm01 = 289;
    public static final int tm02 = 290;
    public static final int tm03 = 291;
    public static final int tm04 = 292;
    public static final int tm05 = 293;
    public static final int tm06 = 294;
    public static final int tm07 = 295;
    public static final int tm08 = 296;
    public static final int tm09 = 297;
    public static final int tm10 = 298;
    public static final int tm11 = 299;
    public static final int tm12 = 300;
    public static final int tm13 = 301;
    public static final int tm14 = 302;
    public static final int tm15 = 303;
    public static final int tm16 = 304;
    public static final int tm17 = 305;
    public static final int tm18 = 306;
    public static final int tm19 = 307;
    public static final int tm20 = 308;
    public static final int tm21 = 309;
    public static final int tm22 = 310;
    public static final int tm23 = 311;
    public static final int tm24 = 312;
    public static final int tm25 = 313;
    public static final int tm26 = 314;
    public static final int tm27 = 315;
    public static final int tm28 = 316;
    public static final int tm29 = 317;
    public static final int tm30 = 318;
    public static final int tm31 = 319;
    public static final int tm32 = 320;
    public static final int tm33 = 321;
    public static final int tm34 = 322;
    public static final int tm35 = 323;
    public static final int tm36 = 324;
    public static final int tm37 = 325;
    public static final int tm38 = 326;
    public static final int tm39 = 327;
    public static final int tm40 = 328;
    public static final int tm41 = 329;
    public static final int tm42 = 330;
    public static final int tm43 = 331;
    public static final int tm44 = 332;
    public static final int tm45 = 333;
    public static final int tm46 = 334;
    public static final int tm47 = 335;
    public static final int tm48 = 336;
    public static final int tm49 = 337;
    public static final int tm50 = 338;
    public static final int hm01 = 339;
    public static final int hm02 = 340;
    public static final int hm03 = 341;
    public static final int hm04 = 342;
    public static final int hm05 = 343;
    public static final int hm06 = 344;
    public static final int hm07 = 345;
    public static final int hm08 = 346;
    public static final int unknown347 = 347;
    public static final int unknown348 = 348;

    /* Exclusive to FRLG and Emerald */
    public static final int oaksParcel = 349;
    public static final int pokeFlute = 350;
    public static final int secretKey = 351;
    public static final int bikeVoucher = 352;
    public static final int goldTeeth = 353;
    public static final int oldAmber = 354;
    public static final int cardKey = 355;
    public static final int liftKey = 356;
    public static final int helixFossil = 357;
    public static final int domeFossil = 358;
    public static final int silphScope = 359;
    public static final int bicycle = 360;
    public static final int townMap = 361;
    public static final int vsSeeker = 362;
    public static final int fameChecker = 363;
    public static final int tmCase = 364;
    public static final int berryPouch = 365;
    public static final int teachyTV = 366;
    public static final int triPass = 367;
    public static final int rainbowPass = 368;
    public static final int tea = 369;
    public static final int mysticTicket = 370;
    public static final int auroraTicket = 371;
    public static final int powderJar = 372;
    public static final int ruby = 373;
    public static final int sapphire = 374;

    /* Exclusive to Emerald */
    public static final int magmaEmblem = 375;
    public static final int oldSeaMap = 376;
}
