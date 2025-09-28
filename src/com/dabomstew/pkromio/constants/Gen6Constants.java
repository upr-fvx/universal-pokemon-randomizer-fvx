package com.dabomstew.pkromio.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen6Constants.java - Constants for X/Y/Omega Ruby/Alpha Sapphire      --*/
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

import com.dabomstew.pkromio.gamedata.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gen6Constants {

    public static final int Type_XY = N3DSConstants.Type_XY;
    public static final int Type_ORAS = N3DSConstants.Type_ORAS;

    public static final int pokemonCount = 721;
    private static final int xyFormeCount = 77, orasFormeCount = 104;
    private static final int orasformeMovesetOffset = 35;

    public static final List<Integer> actuallyCosmeticForms = Arrays.asList(
            SpeciesIDs.Gen6Formes.cherrimCosmetic1,
            SpeciesIDs.Gen6Formes.keldeoCosmetic1,
            SpeciesIDs.Gen6Formes.furfrouCosmetic1, SpeciesIDs.Gen6Formes.furfrouCosmetic2,
            SpeciesIDs.Gen6Formes.furfrouCosmetic3, SpeciesIDs.Gen6Formes.furfrouCosmetic4,
            SpeciesIDs.Gen6Formes.furfrouCosmetic5, SpeciesIDs.Gen6Formes.furfrouCosmetic6,
            SpeciesIDs.Gen6Formes.furfrouCosmetic7, SpeciesIDs.Gen6Formes.furfrouCosmetic8,
            SpeciesIDs.Gen6Formes.furfrouCosmetic9,
            SpeciesIDs.Gen6Formes.pumpkabooCosmetic1, SpeciesIDs.Gen6Formes.pumpkabooCosmetic2,
            SpeciesIDs.Gen6Formes.pumpkabooCosmetic3,
            SpeciesIDs.Gen6Formes.gourgeistCosmetic1, SpeciesIDs.Gen6Formes.gourgeistCosmetic2,
            SpeciesIDs.Gen6Formes.gourgeistCosmetic3,
            SpeciesIDs.Gen6Formes.floetteCosmetic1, SpeciesIDs.Gen6Formes.floetteCosmetic2,
            SpeciesIDs.Gen6Formes.floetteCosmetic3, SpeciesIDs.Gen6Formes.floetteCosmetic4,
            SpeciesIDs.Gen6Formes.pikachuCosmetic1, SpeciesIDs.Gen6Formes.pikachuCosmetic2,
            SpeciesIDs.Gen6Formes.pikachuCosmetic3, SpeciesIDs.Gen6Formes.pikachuCosmetic4,
            SpeciesIDs.Gen6Formes.pikachuCosmetic5, SpeciesIDs.Gen6Formes.pikachuCosmetic6 // Cosplay Pikachu
    );

    public static final String criesTablePrefixXY = "60000A006B000A0082000A003D010A00";

    public static final String introPokemonModelOffsetXY = "01000400020002000200000003000000";
    public static final String introInitialCryOffset1XY = "3AFEFFEB000055E31400D40507005001";
    public static final String introInitialCryOffset2XY = "0800A0E110FEFFEB000057E31550C405";
    public static final String introInitialCryOffset3XY = "0020E0E30310A0E1E4FDFFEB0000A0E3";
    public static final String introRepeatedCryOffsetXY = "1080BDE800002041000000008D001000";

    public static final List<Integer> speciesWithMegaEvos = Collections.unmodifiableList(Arrays.asList(
            SpeciesIDs.venusaur, SpeciesIDs.charizard, SpeciesIDs.blastoise, SpeciesIDs.alakazam, SpeciesIDs.gengar,
            SpeciesIDs.kangaskhan, SpeciesIDs.pinsir, SpeciesIDs.gyarados, SpeciesIDs.aerodactyl, SpeciesIDs.mewtwo,
            SpeciesIDs.ampharos, SpeciesIDs.scizor, SpeciesIDs.heracross, SpeciesIDs.houndoom, SpeciesIDs.tyranitar,
            SpeciesIDs.blaziken, SpeciesIDs.gardevoir, SpeciesIDs.mawile, SpeciesIDs.aggron, SpeciesIDs.medicham,
            SpeciesIDs.manectric, SpeciesIDs.banette, SpeciesIDs.absol, SpeciesIDs.latias, SpeciesIDs.latios,
            SpeciesIDs.garchomp, SpeciesIDs.lucario, SpeciesIDs.abomasnow,
            // ORAS onlies:
            SpeciesIDs.beedrill, SpeciesIDs.pidgeot, SpeciesIDs.slowbro, SpeciesIDs.steelix, SpeciesIDs.sceptile,
            SpeciesIDs.swampert, SpeciesIDs.sableye, SpeciesIDs.sharpedo, SpeciesIDs.camerupt, SpeciesIDs.altaria,
            SpeciesIDs.glalie, SpeciesIDs.salamence, SpeciesIDs.metagross, SpeciesIDs.rayquaza, SpeciesIDs.lopunny,
            SpeciesIDs.gallade, SpeciesIDs.audino, SpeciesIDs.diancie
    ));

    private static final Map<Integer,String> dummyFormeSuffixes = setupDummyFormeSuffixes();
    private static final Map<Integer,Map<Integer,String>> formeSuffixesByBaseForme = setupFormeSuffixesByBaseForme();

    public static String getFormeSuffixByBaseForme(int baseForme, int formNum) {
        return formeSuffixesByBaseForme.getOrDefault(baseForme,dummyFormeSuffixes).getOrDefault(formNum,"");
    }

    private static final List<Integer> xyIrregularFormes = Arrays.asList(
            SpeciesIDs.Gen6Formes.castformSu, SpeciesIDs.Gen6Formes.castformR, SpeciesIDs.Gen6Formes.castformSn,
            SpeciesIDs.Gen6Formes.darmanitanZ,
            SpeciesIDs.Gen6Formes.meloettaP,
            SpeciesIDs.Gen6Formes.kyuremW,
            SpeciesIDs.Gen6Formes.kyuremB,
            SpeciesIDs.Gen6Formes.gengarMega,
            SpeciesIDs.Gen6Formes.gardevoirMega,
            SpeciesIDs.Gen6Formes.ampharosMega,
            SpeciesIDs.Gen6Formes.venusaurMega,
            SpeciesIDs.Gen6Formes.charizardMegaX, SpeciesIDs.Gen6Formes.charizardMegaY,
            SpeciesIDs.Gen6Formes.mewtwoMegaX, SpeciesIDs.Gen6Formes.mewtwoMegaY,
            SpeciesIDs.Gen6Formes.blazikenMega,
            SpeciesIDs.Gen6Formes.medichamMega,
            SpeciesIDs.Gen6Formes.houndoomMega,
            SpeciesIDs.Gen6Formes.aggronMega,
            SpeciesIDs.Gen6Formes.banetteMega,
            SpeciesIDs.Gen6Formes.tyranitarMega,
            SpeciesIDs.Gen6Formes.scizorMega,
            SpeciesIDs.Gen6Formes.pinsirMega,
            SpeciesIDs.Gen6Formes.aerodactylMega,
            SpeciesIDs.Gen6Formes.lucarioMega,
            SpeciesIDs.Gen6Formes.abomasnowMega,
            SpeciesIDs.Gen6Formes.aegislashB,
            SpeciesIDs.Gen6Formes.blastoiseMega,
            SpeciesIDs.Gen6Formes.kangaskhanMega,
            SpeciesIDs.Gen6Formes.gyaradosMega,
            SpeciesIDs.Gen6Formes.absolMega,
            SpeciesIDs.Gen6Formes.alakazamMega,
            SpeciesIDs.Gen6Formes.heracrossMega,
            SpeciesIDs.Gen6Formes.mawileMega,
            SpeciesIDs.Gen6Formes.manectricMega,
            SpeciesIDs.Gen6Formes.garchompMega,
            SpeciesIDs.Gen6Formes.latiosMega,
            SpeciesIDs.Gen6Formes.latiasMega
    );

    private static final List<Integer> orasIrregularFormes = Arrays.asList(
            SpeciesIDs.Gen6Formes.castformSu, SpeciesIDs.Gen6Formes.castformR, SpeciesIDs.Gen6Formes.castformSn,
            SpeciesIDs.Gen6Formes.darmanitanZ,
            SpeciesIDs.Gen6Formes.meloettaP,
            SpeciesIDs.Gen6Formes.kyuremW,
            SpeciesIDs.Gen6Formes.kyuremB,
            SpeciesIDs.Gen6Formes.gengarMega,
            SpeciesIDs.Gen6Formes.gardevoirMega,
            SpeciesIDs.Gen6Formes.ampharosMega,
            SpeciesIDs.Gen6Formes.venusaurMega,
            SpeciesIDs.Gen6Formes.charizardMegaX, SpeciesIDs.Gen6Formes.charizardMegaY,
            SpeciesIDs.Gen6Formes.mewtwoMegaX, SpeciesIDs.Gen6Formes.mewtwoMegaY,
            SpeciesIDs.Gen6Formes.blazikenMega,
            SpeciesIDs.Gen6Formes.medichamMega,
            SpeciesIDs.Gen6Formes.houndoomMega,
            SpeciesIDs.Gen6Formes.aggronMega,
            SpeciesIDs.Gen6Formes.banetteMega,
            SpeciesIDs.Gen6Formes.tyranitarMega,
            SpeciesIDs.Gen6Formes.scizorMega,
            SpeciesIDs.Gen6Formes.pinsirMega,
            SpeciesIDs.Gen6Formes.aerodactylMega,
            SpeciesIDs.Gen6Formes.lucarioMega,
            SpeciesIDs.Gen6Formes.abomasnowMega,
            SpeciesIDs.Gen6Formes.aegislashB,
            SpeciesIDs.Gen6Formes.blastoiseMega,
            SpeciesIDs.Gen6Formes.kangaskhanMega,
            SpeciesIDs.Gen6Formes.gyaradosMega,
            SpeciesIDs.Gen6Formes.absolMega,
            SpeciesIDs.Gen6Formes.alakazamMega,
            SpeciesIDs.Gen6Formes.heracrossMega,
            SpeciesIDs.Gen6Formes.mawileMega,
            SpeciesIDs.Gen6Formes.manectricMega,
            SpeciesIDs.Gen6Formes.garchompMega,
            SpeciesIDs.Gen6Formes.latiosMega,
            SpeciesIDs.Gen6Formes.latiasMega,
            SpeciesIDs.Gen6Formes.swampertMega,
            SpeciesIDs.Gen6Formes.sceptileMega,
            SpeciesIDs.Gen6Formes.sableyeMega,
            SpeciesIDs.Gen6Formes.altariaMega,
            SpeciesIDs.Gen6Formes.galladeMega,
            SpeciesIDs.Gen6Formes.audinoMega,
            SpeciesIDs.Gen6Formes.sharpedoMega,
            SpeciesIDs.Gen6Formes.slowbroMega,
            SpeciesIDs.Gen6Formes.steelixMega,
            SpeciesIDs.Gen6Formes.pidgeotMega,
            SpeciesIDs.Gen6Formes.glalieMega,
            SpeciesIDs.Gen6Formes.diancieMega,
            SpeciesIDs.Gen6Formes.metagrossMega,
            SpeciesIDs.Gen6Formes.kyogreP,
            SpeciesIDs.Gen6Formes.groudonP,
            SpeciesIDs.Gen6Formes.rayquazaMega,
            SpeciesIDs.Gen6Formes.cameruptMega,
            SpeciesIDs.Gen6Formes.lopunnyMega,
            SpeciesIDs.Gen6Formes.salamenceMega,
            SpeciesIDs.Gen6Formes.beedrillMega
    );

    private static final int moveCountXY = 617, moveCountORAS = 621;
    private static final int highestAbilityIndexXY = AbilityIDs.auraBreak, highestAbilityIndexORAS = AbilityIDs.deltaStream;

    public static final List<Integer> uselessAbilities = Arrays.asList(AbilityIDs.forecast, AbilityIDs.multitype,
            AbilityIDs.flowerGift, AbilityIDs.zenMode, AbilityIDs.stanceChange);

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

    public static final int noDamageTargetTrappingEffect = 106, noDamageFieldTrappingEffect = 354,
            damageAdjacentFoesTrappingEffect = 373;

    public static final int noDamageStatusQuality = 1, noDamageStatChangeQuality = 2, damageStatusQuality = 4,
            noDamageStatusAndStatChangeQuality = 5, damageTargetDebuffQuality = 6, damageUserBuffQuality = 7,
            damageAbsorbQuality = 8;

    public static List<Integer> bannedMoves = Collections.singletonList(MoveIDs.hyperspaceFury);

    public static final Type[] typeTable = constructTypeTable();

    // Copied from pk3DS. "Dark Grass Held Item" should probably be renamed
    public static final int bsHPOffset = 0, bsAttackOffset = 1, bsDefenseOffset = 2, bsSpeedOffset = 3,
            bsSpAtkOffset = 4, bsSpDefOffset = 5, bsPrimaryTypeOffset = 6, bsSecondaryTypeOffset = 7,
            bsCatchRateOffset = 8, bsCommonHeldItemOffset = 12, bsRareHeldItemOffset = 14, bsDarkGrassHeldItemOffset = 16,
            bsGenderOffset = 18, bsGrowthCurveOffset = 21, bsAbility1Offset = 24, bsAbility2Offset = 25,
            bsAbility3Offset = 26, bsFormeOffset = 28, bsFormeSpriteOffset = 30, bsFormeCountOffset = 32,
            bsTMHMCompatOffset = 40, bsSpecialMTCompatOffset = 56, bsMTCompatOffset = 64;

    private static final int bsSizeXY = 0x40;
    private static final int bsSizeORAS = 0x50;

    public static final int evolutionMethodCount = 34;

    public static final int staticPokemonSize = 0xC;
    private static final int staticPokemonCountXY = 0xD;
    private static final int staticPokemonCountORAS = 0x3B;

    private static final int giftPokemonSizeXY = 0x18;
    private static final int giftPokemonSizeORAS = 0x24;
    private static final int giftPokemonCountXY = 0x13;
    private static final int giftPokemonCountORAS = 0x25;

    public static final String tmDataPrefix = "D400AE02AF02B002";
    public static final int tmCount = 100, tmBlockOneCount = 92, tmBlockTwoCount = 3, tmBlockThreeCount = 5,
            tmBlockOneOffset = ItemIDs.tm01, tmBlockTwoOffset = ItemIDs.tm93, tmBlockThreeOffset = ItemIDs.tm96, hmBlockOneCount = 5,
            rockSmashOffsetORAS = 10, diveOffsetORAS = 28;
    private static final int tmBlockTwoStartingOffsetXY = 97, tmBlockTwoStartingOffsetORAS = 98,
            hmCountXY = 5, hmCountORAS = 7;
    public static final int hiddenItemCountORAS = 170;
    public static final String hiddenItemsPrefixORAS = "A100A200A300A400A5001400010053004A0084000900";
    public static final String itemPalettesPrefix = "6F7461746500FF920A063F";
    private static final String shopItemsLocatorXY = "0400110004000300", shopItemsLocatorORAS = "04001100120004000300";

    public static final int tutorMoveCount = 60;
    public static final String tutorsLocator = "C2015701A20012024401BA01";
    public static final String tutorsShopPrefix = "8A02000030000000";

    public static final int[] tutorSize = new int[]{15, 17, 16, 15};

    private static final String ingameTradesPrefixXY = "BA0A02015E000100BC0A150069000100";
    private static final String ingameTradesPrefixORAS = "810B7A0097000A00000047006B000A00";

    public static final int ingameTradeSize = 0x24;

    public static final String friendshipValueForEvoLocator = "DC0050E3BC00002A";

    public static final String perfectOddsBranchLocator = "050000BA000050E3";

    public static final String[] fastestTextPrefixes = new String[]{"1080BDE80000A0E31080BDE8F0412DE9", "485080E59C4040E24C50C0E5EC009FE5"};

    private static final List<Integer> mainGameShopsXY = Arrays.asList(
            10,11,12,13,16,17,20,21,24,25
    );

    private static final List<Integer> mainGameShopsORAS = Arrays.asList(
            10, 11, 13, 14, 16, 17, 18, 19, 20, 21
    );

    private static final List<String> shopNamesXY = Arrays.asList(
            "Primary 0 Badges",
            "Primary 1 Badges",
            "Primary 2 Badges",
            "Primary 3 Badges",
            "Primary 4 Badges",
            "Primary 5 Badges",
            "Primary 6 Badges",
            "Primary 7 Badges",
            "Primary 8 Badges",
            "Unused",
            "Lumiose Herboriste",
            "Lumiose Poké Ball Boutique",
            "Lumiose Stone Emporium",
            "Coumarine Incenses",
            "Aquacorde Poké Ball",
            "Aquacorde Potion",
            "Lumiose North Secondary",
            "Cyllage Secondary",
            "Shalour Secondary (TMs)",
            "Lumiose South Secondary (TMs)",
            "Laverre Secondary",
            "Snowbelle Secondary",
            "Kiloude Secondary (TMs)",
            "Anistar Secondary (TMs)",
            "Santalune Secondary",
            "Coumarine Secondary");

    private static final List<String> shopNamesORAS = Arrays.asList(
            "Primary 0 Badges (After Pokédex)",
            "Primary 1 Badges",
            "Primary 2 Badges",
            "Primary 3 Badges",
            "Primary 4 Badges",
            "Primary 5 Badges",
            "Primary 6 Badges",
            "Primary 7 Badges",
            "Primary 8 Badges",
            "Primary 0 Badges (Before Pokédex)",
            "Slateport Incenses",
            "Slateport Vitamins",
            "Slateport TMs",
            "Rustboro Secondary",
            "Slateport Secondary",
            "Mauville Secondary (TMs)",
            "Verdanturf Secondary",
            "Fallarbor Secondary",
            "Lavaridge Herbs",
            "Lilycove Dept. Store 2F Left",
            "Lilycove Dept. Store 3F Left",
            "Lilycove Dept. Store 3F Right",
            "Lilycove Dept. Store 4F Left (TMs)",
            "Lilycove Dept. Store 4F Right (TMs)");

    public static final List<Integer> evolutionItems = Arrays.asList(ItemIDs.sunStone, ItemIDs.moonStone, ItemIDs.fireStone,
            ItemIDs.thunderStone, ItemIDs.waterStone, ItemIDs.leafStone, ItemIDs.shinyStone, ItemIDs.duskStone, ItemIDs.dawnStone,
            ItemIDs.ovalStone, ItemIDs.kingsRock, ItemIDs.deepSeaTooth, ItemIDs.deepSeaScale, ItemIDs.metalCoat, ItemIDs.dragonScale,
            ItemIDs.upgrade, ItemIDs.protector, ItemIDs.electirizer, ItemIDs.magmarizer, ItemIDs.dubiousDisc, ItemIDs.reaperCloth,
            ItemIDs.razorClaw, ItemIDs.razorFang, ItemIDs.prismScale, ItemIDs.whippedDream, ItemIDs.sachet);

    private static final List<Integer> requiredFieldTMsXY = Arrays.asList(
            ItemIDs.tm01, ItemIDs.tm09, ItemIDs.tm40, ItemIDs.tm19, ItemIDs.tm65, ItemIDs.tm73, ItemIDs.tm69,
            ItemIDs.tm74, ItemIDs.tm81, ItemIDs.tm57, ItemIDs.tm61, ItemIDs.tm97, ItemIDs.tm95, ItemIDs.tm71,
            ItemIDs.tm79, ItemIDs.tm30, ItemIDs.tm31, ItemIDs.tm36, ItemIDs.tm53, ItemIDs.tm29, ItemIDs.tm22,
            ItemIDs.tm03, ItemIDs.tm02, ItemIDs.tm80, ItemIDs.tm26);

    private static final List<Integer> requiredFieldTMsORAS = Arrays.asList(
            ItemIDs.tm37, ItemIDs.tm32, ItemIDs.tm62, ItemIDs.tm11, ItemIDs.tm86, ItemIDs.tm29, ItemIDs.tm59,
            ItemIDs.tm43, ItemIDs.tm53, ItemIDs.tm69, ItemIDs.tm06, ItemIDs.tm02, ItemIDs.tm13, ItemIDs.tm18,
            ItemIDs.tm22, ItemIDs.tm61, ItemIDs.tm30, ItemIDs.tm97, ItemIDs.tm07, ItemIDs.tm90, ItemIDs.tm26,
            ItemIDs.tm55, ItemIDs.tm34, ItemIDs.tm35, ItemIDs.tm64, ItemIDs.tm65, ItemIDs.tm66, ItemIDs.tm74,
            ItemIDs.tm79, ItemIDs.tm80, ItemIDs.tm81, ItemIDs.tm84, ItemIDs.tm89, ItemIDs.tm91, ItemIDs.tm93,
            ItemIDs.tm95);

    public static final List<Integer> fieldMovesXY = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.waterfall, MoveIDs.sweetScent, MoveIDs.rockSmash);
    public static final List<Integer> fieldMovesORAS = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.waterfall, MoveIDs.sweetScent, MoveIDs.rockSmash, MoveIDs.secretPower, MoveIDs.dive);

    public static final int fallingEncounterOffset = 0xF4270, fallingEncounterCount = 55, fieldEncounterSize = 0x3C,
                            rustlingBushEncounterOffset = 0xF40CC, rustlingBushEncounterCount = 7;
    public static final Map<Integer, String> fallingEncounterNameMap = constructFallingEncounterNameMap();
    public static final Map<Integer, String> rustlingBushEncounterNameMap = constructRustlingBushEncounterNameMap();
    public static final int perPokemonAreaDataLengthXY = 0xE8, perPokemonAreaDataLengthORAS = 0x2A0;

    private static final String saveLoadFormeReversionPrefixXY = "09EB000094E5141094E54A0B80E2", saveLoadFormeReversionPrefixORAS = "09EB000094E5141094E5120A80E2";
    public static final String afterBattleFormeReversionPrefix = "E4FFFFEA0000000000000000";
    public static final String ninjaskSpeciesPrefix = "241094E5B810D1E1", shedinjaSpeciesPrefix = "C2FFFFEB0040A0E10020A0E3";
    public static final String boxLegendaryFunctionPrefixXY = "14D08DE20900A0E1";
    public static final int boxLegendaryEncounterFileXY = 341, boxLegendaryLocalScriptOffsetXY = 0x6E0;
    public static final int[] boxLegendaryCodeOffsetsXY = new int[]{ 144, 300, 584 };
    public static final int seaSpiritsDenEncounterFileXY = 351, seaSpiritsDenLocalScriptOffsetXY = 0x1C0;
    public static final int[] seaSpiritsDenScriptOffsetsXY = new int[]{ 0x500, 0x508, 0x510 };
    public static final String rayquazaFunctionPrefixORAS = "0900A0E1F08FBDE8";
    public static final int[] rayquazaScriptOffsetsORAS = new int[]{ 3334, 14734 }, rayquazaCodeOffsetsORAS = new int[]{ 136, 292, 576 };
    public static final String nationalDexFunctionLocator = "080094E5010000E21080BDE8170F122F", xyGetDexFlagFunctionLocator = "000055E30100A0030A00000A",
            orasGetHoennDexCaughtFunctionPrefix = "170F122F1CC15800";
    public static final int megastoneTableStartingOffsetORAS = 0xABA, megastoneTableEntrySizeORAS = 0x20, megastoneTableLengthORAS = 27;

    public static final String pickupTableLocator = "110012001A00";
    public static final int numberOfPickupItems = 29;

    public static final String xyRoamerFreeSpacePostfix = "540095E50220A0E30810A0E1", xyRoamerSpeciesLocator = "9040A0030400000A",
            xyRoamerLevelPrefix = "B020DDE13F3BC1E3";

    public static final String xyTrashEncountersTablePrefix = "4028100000";
    public static final int xyTrashEncounterDataLength = 16, xyTrashCanEncounterCount = 24,
            pokemonVillageGarbadorOffset = 0, pokemonVillageGarbadorCount = 6, pokemonVillageBanetteOffset = 6,
            pokemonVillageBanetteCount = 6, lostHotelGarbadorOffset = 12, lostHotelGarbadorCount = 3,
            lostHotelTrubbishOffset = 15, lostHotelTrubbishCount = 3, lostHotelRotomOffset = 18, lostHotelRotomCount = 6;


    public static List<Integer> xyHardcodedTradeOffsets = Arrays.asList(1, 8);
    public static List<Integer> xyHardcodedTradeTexts = Arrays.asList(129, 349);

    public static final List<Integer> consumableHeldItems = setupAllConsumableItems();

    private static List<Integer> setupAllConsumableItems() {
        List<Integer> list = new ArrayList<>(Gen5Constants.consumableHeldItems);
        list.addAll(Arrays.asList(ItemIDs.weaknessPolicy, ItemIDs.luminousMoss, ItemIDs.snowball, ItemIDs.roseliBerry,
                ItemIDs.keeBerry, ItemIDs.marangaBerry, ItemIDs.fairyGem));
        return list;
    }

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        List<Integer> list = new ArrayList<>(Gen5Constants.allHeldItems);
        list.addAll(Arrays.asList(ItemIDs.weaknessPolicy, ItemIDs.snowball, ItemIDs.roseliBerry, ItemIDs.keeBerry,
                ItemIDs.marangaBerry, ItemIDs.fairyGem));
        list.addAll(Arrays.asList(ItemIDs.assaultVest, ItemIDs.pixiePlate, ItemIDs.safetyGoggles));
        return list;
    }

    public static final List<Integer> generalPurposeConsumableItems = initializeGeneralPurposeConsumableItems();

    private static List<Integer> initializeGeneralPurposeConsumableItems() {
        List<Integer> list = new ArrayList<>(Gen5Constants.generalPurposeConsumableItems);
        list.addAll(Arrays.asList(ItemIDs.weaknessPolicy, ItemIDs.luminousMoss, ItemIDs.snowball, ItemIDs.keeBerry, ItemIDs.marangaBerry));
        return Collections.unmodifiableList(list);
    }

    public static final List<Integer> generalPurposeItems = initializeGeneralPurposeItems();

    private static List<Integer> initializeGeneralPurposeItems() {
        List<Integer> list = new ArrayList<>(Gen5Constants.generalPurposeItems);
        list.add(ItemIDs.safetyGoggles);
        return Collections.unmodifiableList(list);
    }

    public static final Map<Type, Integer> weaknessReducingBerries = initializeWeaknessReducingBerries();

    private static Map<Type, Integer> initializeWeaknessReducingBerries() {
        Map<Type, Integer> map = new HashMap<>(Gen5Constants.weaknessReducingBerries);
        map.put(Type.FAIRY, ItemIDs.roseliBerry);
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Type, Integer> consumableTypeBoostingItems = initializeConsumableTypeBoostingItems();

    private static Map<Type, Integer> initializeConsumableTypeBoostingItems() {
        Map<Type, Integer> map = new HashMap<>(Gen5Constants.consumableTypeBoostingItems);
        map.put(Type.FAIRY, ItemIDs.fairyGem);
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Type, List<Integer>> typeBoostingItems = initializeTypeBoostingItems();

    private static Map<Type, List<Integer>> initializeTypeBoostingItems() {
        Map<Type, List<Integer>> map = new HashMap<>(Gen5Constants.typeBoostingItems);
        map.put(Type.FAIRY, Collections.singletonList(ItemIDs.pixiePlate));
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> moveBoostingItems = initializeMoveBoostingItems();

    private static Map<Integer, List<Integer>> initializeMoveBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>(Gen5Constants.moveBoostingItems);
        map.put(MoveIDs.drainingKiss, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.infestation, Arrays.asList(ItemIDs.gripClaw, ItemIDs.bindingBand));
        map.put(MoveIDs.oblivionWing, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.parabolicCharge, Collections.singletonList(ItemIDs.bigRoot));
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> abilityBoostingItems = initializeAbilityBoostingItems();

    private static Map<Integer, List<Integer>> initializeAbilityBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>(Gen5Constants.abilityBoostingItems);
        // Weather from abilities changed in Gen VI, so these items become relevant.
        map.put(AbilityIDs.drizzle, Collections.singletonList(ItemIDs.dampRock));
        map.put(AbilityIDs.drought, Collections.singletonList(ItemIDs.heatRock));
        map.put(AbilityIDs.sandStream, Collections.singletonList(ItemIDs.smoothRock));
        map.put(AbilityIDs.snowWarning, Collections.singletonList(ItemIDs.icyRock));
        return Collections.unmodifiableMap(map);
    }

    // No new species boosting items in Gen VI
    public static final Map<Integer, List<Integer>> speciesBoostingItems = Gen5Constants.speciesBoostingItems;

    public static String getIngameTradesPrefix(int romType) {
        if (romType == Type_XY) {
            return ingameTradesPrefixXY;
        } else {
            return ingameTradesPrefixORAS;
        }
    }

    public static List<Integer> getRequiredFieldTMs(int romType) {
        if (romType == Type_XY) {
            return requiredFieldTMsXY;
        } else {
            return requiredFieldTMsORAS;
        }
    }

    public static List<Integer> getMainGameShops(int romType) {
        if (romType == Type_XY) {
            return mainGameShopsXY;
        } else {
            return mainGameShopsORAS;
        }
    }

    public static List<String> getShopNames(int romType) {
        if (romType == Type_XY) {
            return shopNamesXY;
        } else {
            return shopNamesORAS;
        }
    }

    public static int getBsSize(int romType) {
        if (romType == Type_XY) {
            return bsSizeXY;
        } else {
            return bsSizeORAS;
        }
    }

    public static List<Integer> getIrregularFormes(int romType) {
        if (romType == Type_XY) {
            return xyIrregularFormes;
        } else if (romType == Type_ORAS) {
            return orasIrregularFormes;
        }
        return new ArrayList<>();
    }

    public static int getFormeCount(int romType) {
        if (romType == Type_XY) {
            return xyFormeCount;
        } else if (romType == Type_ORAS) {
            return orasFormeCount;
        }
        return 0;
    }

    public static int getFormeMovesetOffset(int romType) {
        if (romType == Type_XY) {
            return orasformeMovesetOffset;
        } else if (romType == Type_ORAS) {
            return orasformeMovesetOffset;
        }
        return 0;
    }

    public static int getMoveCount(int romType) {
        if (romType == Type_XY) {
            return moveCountXY;
        } else if (romType == Type_ORAS) {
            return moveCountORAS;
        }
        return moveCountXY;
    }

    public static int getTMBlockTwoStartingOffset(int romType) {
        if (romType == Type_XY) {
            return tmBlockTwoStartingOffsetXY;
        } else if (romType == Type_ORAS) {
            return tmBlockTwoStartingOffsetORAS;
        }
        return tmBlockTwoStartingOffsetXY;
    }

    public static int getHMCount(int romType) {
        if (romType == Type_XY) {
            return hmCountXY;
        } else if (romType == Type_ORAS) {
            return hmCountORAS;
        }
        return hmCountXY;
    }

    public static int getHighestAbilityIndex(int romType) {
        if (romType == Type_XY) {
            return highestAbilityIndexXY;
        } else if (romType == Type_ORAS) {
            return highestAbilityIndexORAS;
        }
        return highestAbilityIndexXY;
    }

    public static int getStaticPokemonCount(int romType) {
        if (romType == Type_XY) {
            return staticPokemonCountXY;
        } else if (romType == Type_ORAS) {
            return staticPokemonCountORAS;
        }
        return staticPokemonCountXY;
    }

    public static int getGiftPokemonCount(int romType) {
        if (romType == Type_XY) {
            return giftPokemonCountXY;
        } else if (romType == Type_ORAS) {
            return giftPokemonCountORAS;
        }
        return giftPokemonCountXY;
    }

    public static int getGiftPokemonSize(int romType) {
        if (romType == Type_XY) {
            return giftPokemonSizeXY;
        } else if (romType == Type_ORAS) {
            return giftPokemonSizeORAS;
        }
        return giftPokemonSizeXY;
    }

    public static String getShopItemsLocator(int romType) {
        if (romType == Type_XY) {
            return shopItemsLocatorXY;
        } else if (romType == Type_ORAS) {
            return shopItemsLocatorORAS;
        }
        return shopItemsLocatorXY;
    }

    private static Type[] constructTypeTable() {
        Type[] table = new Type[256];
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
        table[0x11] = Type.FAIRY;
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
            case FAIRY:
                return 0x11;
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
            EvolutionType.LEVEL_ICE_ROCK, EvolutionType.LEVEL_UPSIDE_DOWN, EvolutionType.FAIRY_AFFECTION,
            EvolutionType.LEVEL_WITH_DARK, EvolutionType.LEVEL_RAIN, EvolutionType.LEVEL_DAY, EvolutionType.LEVEL_NIGHT,
            EvolutionType.LEVEL_FEMALE_ESPURR,
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
                return romType == Type_XY ? 272 : 139; // Route 13 : New Mauville
            case LEVEL_MOSS_ROCK:
                return romType == Type_XY ? 282 : 82; // Route 20 : Petalburg Woods
            case LEVEL_ICE_ROCK:
                return romType == Type_XY ? 313 : 128; // Frost Cavern : Shoal Cave
            default:
                throw new IllegalArgumentException(et + " is not a valid EvolutionType for this game.");
        }
    }

    public static String getSaveLoadFormeReversionPrefix(int romType) {
        if (romType == Type_XY) {
            return saveLoadFormeReversionPrefixXY;
        } else {
            return saveLoadFormeReversionPrefixORAS;
        }
    }

    private static Map<Integer,Map<Integer,String>> setupFormeSuffixesByBaseForme() {
        Map<Integer,Map<Integer,String>> map = new HashMap<>();

        putFormSuffixes(map, SpeciesIDs.pikachu, "-Cosplay", "-Cosplay", "-Cosplay", "-Cosplay", "-Cosplay", "-Cosplay");

        putFormSuffixes(map, SpeciesIDs.castform, "-Sunny", "-Rainy", "-Snowy");
        putFormSuffixes(map, SpeciesIDs.kyogre, "-Primal");
        putFormSuffixes(map, SpeciesIDs.groudon, "-Primal");
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

        putFormSuffixes(map, SpeciesIDs.meowstic, "-F");
        putFormSuffixes(map, SpeciesIDs.aegislash, "-Blade");
        putFormSuffixes(map, SpeciesIDs.pumpkaboo, "-M", "-L", "-XL");
        putFormSuffixes(map, SpeciesIDs.gourgeist, "-M", "-L", "-XL");
        putFormSuffixes(map, SpeciesIDs.floette, "", "", "", "", "-Eternal"); // first 4 are just colors
        putFormSuffixes(map, SpeciesIDs.hoopa, "-Unbound");

        for (Integer speciesID : Gen6Constants.speciesWithMegaEvos) {
            if (speciesID == SpeciesIDs.charizard || speciesID == SpeciesIDs.mewtwo) {
                putFormSuffixes(map, speciesID, "-Mega-X", "-Mega-Y");
            } else {
                putFormSuffixes(map, speciesID, "-Mega");
            }
        }

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

    private static final Set<Integer> bannedItemsXY = setupBannedItemsXY();
    private static final Set<Integer> bannedItemsORAS = setupBannedItemsORAS();
    private static final Set<Integer> badItemsORAS = setupBadItemsORAS();
    private static final Set<Integer> badItemsXY = setupBadItemsXY();
    public static final Set<Integer> opShopItems =  setupOPShopItems();
    public static final Set<Integer> megaStones = setupMegaStones();

    private static Set<Integer> setupBannedItemsXY() {
        Set<Integer> set = new HashSet<>();
        // Key items + version exclusives
        addBetween(set, ItemIDs.explorerKit, ItemIDs.tidalBell);
        addBetween(set, ItemIDs.dataCard01, ItemIDs.enigmaStone);
        addBetween(set, ItemIDs.xtransceiverMale, ItemIDs.revealGlass);
        set.addAll(Arrays.asList(ItemIDs.expShare, ItemIDs.libertyPass, ItemIDs.propCase, ItemIDs.dragonSkull,
                ItemIDs.lightStone, ItemIDs.darkStone));
        // Unknown blank items or version exclusives
        addBetween(set, ItemIDs.tea, ItemIDs.autograph);
        addBetween(set, ItemIDs.unused120, ItemIDs.unused133);
        // HMs
        addBetween(set, ItemIDs.hm01, ItemIDs.hm08);
        // Battle Launcher exclusives
        addBetween(set, ItemIDs.direHit2, ItemIDs.direHit3);
        // Key items (Gen 6)
        addBetween(set, ItemIDs.holoCasterMale, ItemIDs.rollerSkates);
        addBetween(set, ItemIDs.powerPlantPass, ItemIDs.commonStone);
        addBetween(set, ItemIDs.elevatorKey, ItemIDs.adventureGuide);
        addBetween(set, ItemIDs.lensCase, ItemIDs.travelTrunk);
        addBetween(set, ItemIDs.lookerTicket, ItemIDs.holoCasterFemale);
        set.addAll(Arrays.asList(ItemIDs.pokeFlute, ItemIDs.sprinklotad, ItemIDs.megaCharm, ItemIDs.megaGlove));
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBannedItemsORAS() {
        Set<Integer> set = new HashSet<>(bannedItemsXY);
        // Key items and an HM
        addBetween(set, ItemIDs.machBike, ItemIDs.meteoriteSecondForm);
        addBetween(set, ItemIDs.prisonBottle, ItemIDs.megaCuff);
        addBetween(set, ItemIDs.meteoriteThirdForm, ItemIDs.eonFlute);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItemsORAS() {
        Set<Integer> set = new HashSet<>(Arrays.asList(ItemIDs.oddKeystone, ItemIDs.griseousOrb, ItemIDs.adamantOrb,
                ItemIDs.lustrousOrb, ItemIDs.soulDew, ItemIDs.lightBall, ItemIDs.oranBerry, ItemIDs.quickPowder,
                ItemIDs.passOrb, ItemIDs.discountCoupon, ItemIDs.strangeSouvenir));
        addBetween(set, ItemIDs.growthMulch, ItemIDs.gooeyMulch); // mulch
        addBetween(set, ItemIDs.mail1, ItemIDs.mail12); // mails
        addBetween(set, ItemIDs.figyBerry, ItemIDs.belueBerry); // berries without useful battle effects
        addBetween(set, ItemIDs.luckyPunch, ItemIDs.leek); // pokemon specific
        addBetween(set, ItemIDs.redScarf, ItemIDs.yellowScarf); // contest scarves
        addBetween(set, ItemIDs.relicCopper, ItemIDs.relicCrown); // relic items
        addBetween(set, ItemIDs.richMulch, ItemIDs.amazeMulch); // more mulch
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItemsXY() {
        Set<Integer> set = new HashSet<>(badItemsORAS);
        addBetween(set,ItemIDs.shoalSalt, ItemIDs.greenShard); // Shoal items and Shards; they serve no purpose in XY
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

    private static Set<Integer> setupMegaStones() {
        Set<Integer> set = new HashSet<>();
        addBetween(set, ItemIDs.gengarite, ItemIDs.latiosite);
        addBetween(set, ItemIDs.swampertite, ItemIDs.diancite);
        addBetween(set, ItemIDs.cameruptite, ItemIDs.beedrillite);
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

    public static Set<Integer> getBannedItems(int romType) {
        if (romType == Gen6Constants.Type_XY) {
            return bannedItemsXY;
        } else {
            return bannedItemsORAS;
        }
    }

    public static Set<Integer> getBadItems(int romType) {
        if (romType == Gen6Constants.Type_XY) {
            return badItemsXY;
        } else {
            return badItemsORAS;
        }
    }

    public static void tagTrainersXY(List<Trainer> trs) {

        // Gym Trainers
        tag(trs,"GYM1", 39, 40, 48);
        tag(trs,"GYM2",64, 63, 106, 105);
        tag(trs,"GYM3",83, 84, 146, 147);
        tag(trs,"GYM4", 121, 122, 123, 124);
        tag(trs,"GYM5", 461, 462, 463, 464, 465, 466, 467, 468, 469, 28, 29, 30);
        tag(trs,"GYM6", 245, 250, 248, 243);
        tag(trs,"GYM7", 170, 171, 172, 365, 366);
        tag(trs,"GYM8", 168, 169, 31, 32);

        // Gym Leaders
        tag(trs,"GYM1-LEADER", 6);
        tag(trs,"GYM2-LEADER",76);
        tag(trs,"GYM3-LEADER",21);
        tag(trs,"GYM4-LEADER", 22);
        tag(trs,"GYM5-LEADER", 23);
        tag(trs,"GYM6-LEADER", 24);
        tag(trs,"GYM7-LEADER", 25);
        tag(trs,"GYM8-LEADER", 26);

        tag(trs, 188, "NOTSTRONG"); // Successor Korrina

        // Elite 4
        tag(trs, 269, "ELITE1"); // Malva
        tag(trs, 271, "ELITE2"); // Siebold
        tag(trs, 187, "ELITE3"); // Wikstrom
        tag(trs, 270, "ELITE4"); // Drasna
        tag(trs, 276, "CHAMPION"); // Diantha

        tag(trs,"THEMED:LYSANDRE-LEADER", 303, 525, 526);
        tag(trs,"STRONG", 174, 175, 304, 344, 345, 346, 347, 348, 349, 350, 351, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479); // Team Flare Admins lol
        tag(trs,"STRONG", 324, 325, 438, 439, 573); // Tierno and Trevor
        tag(trs,"STRONG", 327, 328); // Sycamore

        // Rival - Serena
        tagRivalXY(trs, "RIVAL2", 596);
        tagRivalXY(trs, "RIVAL3", 575);
        tagRivalXY(trs, "RIVAL4", 581);
        tagRivalXY(trs, "RIVAL5", 578);
        tagRivalXY(trs, "RIVAL6", 584);
        tagRivalXY(trs, "RIVAL7", 607);
        tagRivalXY(trs, "RIVAL8", 587);
        tagRivalXY(trs, "RIVAL9", 590);
        tagRivalXY(trs, "RIVAL10", 593);
        tagRivalXY(trs, "RIVAL11", 599);

        // Rival - Calem
        tagRivalXY(trs, "RIVAL2", 435);
        tagRivalXY(trs, "RIVAL3", 130);
        tagRivalXY(trs, "RIVAL4", 329);
        tagRivalXY(trs, "RIVAL5", 184);
        tagRivalXY(trs, "RIVAL6", 332);
        tagRivalXY(trs, "RIVAL7", 604);
        tagRivalXY(trs, "RIVAL8", 335);
        tagRivalXY(trs, "RIVAL9", 338);
        tagRivalXY(trs, "RIVAL10", 341);
        tagRivalXY(trs, "RIVAL11", 519);

        // Rival - Shauna
        tagRivalXY(trs, "FRIEND1", 137);
        tagRivalXY(trs, "FRIEND2", 321);
    }

    public static void tagTrainersORAS(List<Trainer> trs) {

        // Gym Trainers & Leaders
        tag(trs,"GYM1",562, 22, 667);
        tag(trs,"GYM2",60, 56, 59);
        tag(trs,"GYM3",34, 568, 614, 35);
        tag(trs,"GYM4",81, 824, 83, 615, 823, 613, 85);
        tag(trs,"GYM5",63, 64, 65, 66, 67, 68, 69);
        tag(trs,"GYM6",115, 517, 516, 118, 730);
        tag(trs,"GYM7",157, 158, 159, 226, 320, 225);
        tag(trs,"GYM8",647, 342, 594, 646, 338, 339, 340, 341); // Includes Wallace in Delta Episode

        // Gym Leaders
        tag(trs,"GYM1-LEADER", 561);
        tag(trs,"GYM2-LEADER",563);
        tag(trs,"GYM3-LEADER",567);
        tag(trs,"GYM4-LEADER", 569);
        tag(trs,"GYM5-LEADER", 570);
        tag(trs,"GYM6-LEADER", 571);
        tag(trs,"GYM7-LEADER", 552);
        tag(trs,"GYM8-LEADER", 572, 943);

        // Elite 4
        tag(trs, "ELITE1", 553, 909); // Sidney
        tag(trs, "ELITE2", 554, 910); // Phoebe
        tag(trs, "ELITE3", 555, 911); // Glacia
        tag(trs, "ELITE4", 556, 912); // Drake
        tag(trs, "CHAMPION", 557, 913, 680, 942); // Steven (includes other appearances)

        tag(trs,"THEMED:MAXIE-LEADER", 235, 236, 271);
        tag(trs,"THEMED:ARCHIE-LEADER",178, 231, 266);
        tag(trs,"THEMED:MATT-STRONG",683, 684, 685, 686, 687);
        tag(trs,"THEMED:SHELLY-STRONG",688,689,690);
        tag(trs,"THEMED:TABITHA-STRONG",691,692,693);
        tag(trs,"THEMED:COURTNEY-STRONG",694,695,696,697,698);
        tag(trs, "THEMED:WALLY-STRONG", 518, 583, 944, 946);

        // Rival - Brendan
        tagRivalORAS(trs, "RIVAL1", 1);
        tagRivalORAS(trs, "RIVAL2", 289);
        tagRivalORAS(trs, "RIVAL3", 674);
        tagRivalORAS(trs, "RIVAL4", 292);
        tagRivalORAS(trs, "RIVAL5", 527);
        tagRivalORAS(trs, "RIVAL6", 699);

        // Rival - May
        tagRivalORAS(trs, "RIVAL1", 4);
        tagRivalORAS(trs, "RIVAL2", 295);
        tagRivalORAS(trs, "RIVAL3", 677);
        tagRivalORAS(trs, "RIVAL4", 298);
        tagRivalORAS(trs, "RIVAL5", 530);
        tagRivalORAS(trs, "RIVAL6", 906);
    }

    private static void tagRivalXY(List<Trainer> allTrainers, String tag, int offset) {
        allTrainers.get(offset - 1).tag = tag + "-0";
        allTrainers.get(offset).tag = tag + "-1";
        allTrainers.get(offset + 1).tag = tag + "-2";
    }

    private static void tagRivalORAS(List<Trainer> allTrainers, String tag, int offset) {
        allTrainers.get(offset - 1).tag = tag + "-2";
        allTrainers.get(offset).tag = tag + "-0";
        allTrainers.get(offset + 1).tag = tag + "-1";
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

    public static final HashMap<String, Type> gymAndEliteThemesXY = setupGymAndEliteThemesXY();

    private static HashMap<String, Type> setupGymAndEliteThemesXY() {
        HashMap<String, Type> themeMap = new HashMap<>();
        //Diantha has no theme
        themeMap.put("ELITE1", Type.FIRE); //Malva
        themeMap.put("ELITE2", Type.WATER); //Siebold
        themeMap.put("ELITE3", Type.STEEL); //Wikstrom
        themeMap.put("ELITE4", Type.DRAGON); //Drasna
        themeMap.put("GYM1", Type.BUG); //Viola
        themeMap.put("GYM2", Type.ROCK); //Grant
        themeMap.put("GYM3", Type.FIGHTING); //Korrina
        themeMap.put("GYM4", Type.GRASS); //Ramos
        themeMap.put("GYM5", Type.ELECTRIC); //Clemont
        themeMap.put("GYM6", Type.FAIRY); //Valerie
        themeMap.put("GYM7", Type.PSYCHIC); //Olympia
        themeMap.put("GYM8", Type.ICE); //Wulfric
        return themeMap;

    }

    public static final HashMap<String, Type> gymAndEliteThemesORAS = setupGymAndEliteThemesORAS();

    private static HashMap<String, Type> setupGymAndEliteThemesORAS() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("CHAMPION", Type.STEEL); //Steven
        themeMap.put("ELITE1", Type.DARK); //Sidney
        themeMap.put("ELITE2", Type.GHOST); //Phoebe
        themeMap.put("ELITE3", Type.ICE); //Glacia
        themeMap.put("ELITE4", Type.DRAGON); //Drake
        themeMap.put("GYM1", Type.ROCK); //Roxanne
        themeMap.put("GYM2", Type.FIGHTING); //Brawly
        themeMap.put("GYM3", Type.ELECTRIC); //Wattson
        themeMap.put("GYM4", Type.FIRE); //Flannery
        themeMap.put("GYM5", Type.NORMAL); //Norman
        themeMap.put("GYM6", Type.FLYING); //Winona
        themeMap.put("GYM7", Type.PSYCHIC); //Tate & Liza
        themeMap.put("GYM8", Type.WATER); //Wallace
        return themeMap;
    }

    public static void setMultiBattleStatusXY(List<Trainer> trs) {
        // 108 + 111: Team Flare Grunts in Glittering Cave
        // 348 + 350: Team Flare Celosia and Bryony fight in Poké Ball Factory
        // 438 + 439: Tierno and Trevor fight on Route 7
        // 470 + 611, 472 + 610, 476 + 612: Team Flare Admin and Grunt fights in Team Flare Secret HQ
        setMultiBattleStatus(trs, 108, 111, 348, 350, 438, 439, 470, 472, 476, 610, 611, 612);
    }

    public static void setMultiBattleStatusORAS(List<Trainer> trs) {
        // 683 + 904: Aqua Admin Matt and Team Aqua Grunt fight on the Southern Island
        // 687 + 905: Aqua Admin Matt and Team Aqua Grunt fight at the Mossdeep Space Center
        // 688 + 903: Aqua Admin Shelly and Team Aqua Grunt fight in Meteor Falls
        // 691 + 902: Magma Admin Tabitha and Team Magma Grunt fight in Meteor Falls
        // 694 + 900: Magma Admin Courtney and Team Magma Grunt fight on the Southern Island
        // 698 + 901: Magma Admin Courtney and Team Magma Grunt fight at the Mossdeep Space Center
        setMultiBattleStatus(trs, 683, 687, 688, 691, 694, 698, 900, 901, 902, 903, 904, 905);
    }

    private static void setMultiBattleStatus(List<Trainer> allTrainers, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).multiBattleStatus = Trainer.MultiBattleStatus.ALWAYS;
            }
        }
    }

    private static Map<Integer, String> constructFallingEncounterNameMap() {
        Map<Integer, String> map = new TreeMap<>();
        map.put(0, "Glittering Cave Ceiling Encounter");
        map.put(4, "Reflection Cave Ceiling Encounter");
        map.put(20, "Victory Road Outside 2 Sky Encounter");
        map.put(24, "Victory Road Inside 2 Ceiling Encounter");
        map.put(28, "Victory Road Outside 3 Sky Encounter");
        map.put(32, "Victory Road Inside 3 Ceiling Encounter");
        map.put(36, "Victory Road Outside 4 Sky Encounter");
        map.put(46, "Terminus Cave Ceiling Encounter");
        return map;
    }

    private static Map<Integer, String> constructRustlingBushEncounterNameMap() {
        Map<Integer, String> map = new TreeMap<>();
        map.put(0, "Route 6 Rustling Bush Encounter");
        map.put(3, "Route 18 Rustling Bush Encounter");
        return map;
    }

    private static final int[] xyPostGameEncounterAreas = new int[0]; //there are none

    private static final int[] orasPostGameEncounterAreas = new int[] {
            585, 586, 587, 588, //Sky Pillar
            609, 610, 611, 612, //Battle Resort
            589, 590, 591, 592, 593, 594, 595, 596, 597, 598, 599, 600, 601, 602, 603,
            604, 605, 606, 607, 608, 657, 658, 659, 660, 661, 662, 663, 664, 665, 666,
            667, 668, 669, 670, 671, 672, 673, 674, 675, 676, 677, //Mirage spots
            34, 39, 48, 57, 66, 74, 79, 84, 89, 94, 100, 109, 119, 124, 129, 134, 144,
            153, 158, 168, 210, 214, 227, 232, 237, 242, 247, 252, 257, 262, 267, 272,
            277, 289, 298, 307, 316, 330, 335, 340, 346, 351, 356, 377, 382, 494, 499,
            504, 510, 515, 524, 615, 625, 635, 645, 679, //DexNav Foreign Encounter
            //Technically, neither mirage spots nor Dexnav foreign encounters are post-game.
            //however, they don't really qualify as "local" either, which is the actual use case.
    };

    private static final List<String> xyLocationTags = initXYLocationTags();

    private static List<String> initXYLocationTags() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "COURIWAY TOWN");
        addCopies(tags, 5, "AMBRETTE TOWN");
        addCopies(tags, 5, "CYLLAGE CITY");
        addCopies(tags, 4, "SHALOUR CITY");
        addCopies(tags, 3, "LAVERRE CITY");
        addCopies(tags, 1, "ROUTE 2");
        addCopies(tags, 5, "ROUTE 3");
        addCopies(tags, 2, "ROUTE 4");
        addCopies(tags, 5, "ROUTE 5");
        addCopies(tags, 1, "ROUTE 6");
        addCopies(tags, 6, "ROUTE 7");
        addCopies(tags, 10, "ROUTE 8");
        addCopies(tags, 1, "ROUTE 9");
        addCopies(tags, 5, "ROUTE 10");
        addCopies(tags, 4, "ROUTE 11");
        addCopies(tags, 10, "ROUTE 12");
        addCopies(tags, 2, "ROUTE 13");
        addCopies(tags, 9, "ROUTE 14");
        addCopies(tags, 9, "ROUTE 15");
        addCopies(tags, 9, "ROUTE 16");
        addCopies(tags, 1, "ROUTE 17");
        addCopies(tags, 6, "ROUTE 18");
        addCopies(tags, 10, "ROUTE 19");
        addCopies(tags, 5, "ROUTE 20");
        addCopies(tags, 9, "ROUTE 21");
        addCopies(tags, 6, "ROUTE 22");
        addCopies(tags, 1, "SANTALUNE FOREST");
        addCopies(tags, 3, "PARFUM PALACE");
        addCopies(tags, 2, "GLITTERING CAVE");
        addCopies(tags, 16, "REFLECTION CAVE");
        addCopies(tags, 24, "FROST CAVERN");
        addCopies(tags, 9, "POKEMON VILLAGE");
        addCopies(tags, 30, "VICTORY ROAD");
        addCopies(tags, 4, "CONNECTING CAVE");
        addCopies(tags, 23, "TERMINUS CAVE");
        addCopies(tags, 1, "LOST HOTEL");
        addCopies(tags, 9, "AZURE BAY");
        addCopies(tags, 4, "GLITTERING CAVE");
        addCopies(tags, 16, "REFLECTION CAVE");
        addCopies(tags, 26, "VICTORY ROAD");
        addCopies(tags, 9, "TERMINUS CAVE");
        addCopies(tags, 3, "ROUTE 6");
        addCopies(tags, 4, "ROUTE 18");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> orasLocationTags = initORASLocationTags();

    private static List<String> initORASLocationTags() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "DEWFORD TOWN");
        addCopies(tags, 4, "PACIFIDLOG TOWN");
        addCopies(tags, 4, "PETALBURG CITY");
        addCopies(tags, 4, "SLATEPORT CITY");
        addCopies(tags, 5, "LILYCOVE CITY");
        addCopies(tags, 4, "MOSSDEEP CITY");
        addCopies(tags, 4, "SOOTOPOLIS CITY");
        addCopies(tags, 4, "EVER GRANDE CITY");
        addCopies(tags, 5, "ROUTE 101");
        addCopies(tags, 9, "ROUTE 102");
        addCopies(tags, 9, "ROUTE 103");
        addCopies(tags, 18, "ROUTE 104");
        addCopies(tags, 5, "ROUTE 105");
        addCopies(tags, 5, "ROUTE 106");
        addCopies(tags, 5, "ROUTE 107");
        addCopies(tags, 5, "ROUTE 108");
        addCopies(tags, 5, "ROUTE 109");
        addCopies(tags, 9, "ROUTE 110");
        addCopies(tags, 10, "ROUTE 111");
        addCopies(tags, 10, "ROUTE 112");
        addCopies(tags, 5, "ROUTE 113");
        addCopies(tags, 10, "ROUTE 114");
        addCopies(tags, 9, "ROUTE 115");
        addCopies(tags, 5, "ROUTE 116");
        addCopies(tags, 9, "ROUTE 117");
        addCopies(tags, 10, "ROUTE 118");
        addCopies(tags, 16, "ROUTE 119");
        addCopies(tags, 16, "ROUTE 120");
        addCopies(tags, 6, "ROUTE 121");
        addCopies(tags, 5, "ROUTE 122");
        addCopies(tags, 8, "ROUTE 123");
        addCopies(tags, 5, "ROUTE 124");
        addCopies(tags, 5, "ROUTE 125");
        addCopies(tags, 5, "ROUTE 126");
        addCopies(tags, 5, "ROUTE 127");
        addCopies(tags, 5, "ROUTE 128");
        addCopies(tags, 5, "ROUTE 129");
        addCopies(tags, 5, "ROUTE 130");
        addCopies(tags, 5, "ROUTE 131");
        addCopies(tags, 5, "ROUTE 132");
        addCopies(tags, 5, "ROUTE 133");
        addCopies(tags, 5, "ROUTE 134");
        addCopies(tags, 1, "ROUTE 107");
        addCopies(tags, 1, "ROUTE 124");
        addCopies(tags, 1, "ROUTE 126");
        addCopies(tags, 1, "ROUTE 128");
        addCopies(tags, 1, "ROUTE 129");
        addCopies(tags, 1, "ROUTE 130");
        addCopies(tags, 36, "METEOR FALLS");
        addCopies(tags, 5, "RUSTURF TUNNEL");
        addCopies(tags, 16, "GRANITE CAVE");
        addCopies(tags, 5, "PETALBURG WOODS");
        addCopies(tags, 5, "JAGGED PASS");
        addCopies(tags, 5, "FIERY PATH");
        addCopies(tags, 26, "MT. PYRE");
        addCopies(tags, 4, "TEAM AQUA HIDEOUT");
        addCopies(tags, 55, "SEAFLOOR CAVERN");
        addCopies(tags, 20, "CAVE OF ORIGIN");
        addCopies(tags, 28, "VICTORY ROAD");
        addCopies(tags, 43, "SHOAL CAVE");
        addCopies(tags, 4, "NEW MAUVILLE");
        addCopies(tags, 16, "SEA MAUVILLE");
        addCopies(tags, 4, "SEALED CHAMBER");
        addCopies(tags, 21, "SCORCHED SLAB");
        addCopies(tags, 4, "TEAM MAGMA HIDEOUT");
        addCopies(tags, 4, "SKY PILLAR");
        // mirage island/forest/cave/mountain each appear in multiple places,
        // but e.g. the mirage forests all look alike and have similar wild encounters,
        // so we treat island/forest/cave/mountain as four locations and not more
        addCopies(tags, 9, "MIRAGE FOREST");
        addCopies(tags, 10, "MIRAGE ISLAND");
        addCopies(tags, 1, "MIRAGE MOUNTAIN");
        addCopies(tags, 4, "BATTLE RESORT");
        addCopies(tags, 40, "SAFARI ZONE");
        addCopies(tags, 4, "CAVE OF ORIGIN");
        addCopies(tags, 9, "MIRAGE MOUNTAIN");
        addCopies(tags, 12, "MIRAGE CAVE");
        addCopies(tags, 5, "MT. PYRE");
        addCopies(tags, 4, "SOOTOPOLIS CITY");
        return Collections.unmodifiableList(tags);
    }

    private static void addCopies(List<String> list, int n, String s) {
        list.addAll(Collections.nCopies(n, s));
    }

    /**
     * Based on
     * <a href=https://bulbapedia.bulbagarden.net/wiki/Appendix:X_and_Y_walkthrough>this walkthrough</a>.
     */
    private static final List<String> locationTagsTraverseOrderXY = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 2", "SANTALUNE FOREST", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "PARFUM PALACE", "ROUTE 7",
            "CONNECTING CAVE", "ROUTE 8", "AMBRETTE TOWN", "ROUTE 9", "GLITTERING CAVE", "CYLLAGE CITY", "ROUTE 10",
            "ROUTE 11", "REFLECTION CAVE", "SHALOUR CITY", "ROUTE 12", "AZURE BAY", "ROUTE 13", "ROUTE 14",
            "LAVERRE CITY", "ROUTE 15", "LOST HOTEL", "ROUTE 16", "FROST CAVERN", "ROUTE 17", "ROUTE 18",
            "COURIWAY TOWN", "ROUTE 19", "ROUTE 20", "POKEMON VILLAGE", "ROUTE 21", "ROUTE 22", "VICTORY ROAD",
            "TERMINUS CAVE"
    ));

    /**
     * Based on
     * <a href=https://bulbapedia.bulbagarden.net/wiki/Appendix:Omega_Ruby_and_Alpha_Sapphire_walkthrough>this walkthrough</a>,
     * with ROUTE 105 & 108 moved down since you can't explore them at all until later.
     */
    private static final List<String> locationTagsTraverseOrderORAS = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 101", "ROUTE 103", "ROUTE 102", "PETALBURG CITY", "ROUTE 104", "PETALBURG WOODS", "ROUTE 116",
            "RUSTURF TUNNEL", "DEWFORD TOWN", "ROUTE 106", "ROUTE 107", "GRANITE CAVE", "ROUTE 109",
            "SLATEPORT CITY", "ROUTE 110", "ROUTE 117", "ROUTE 111", "ROUTE 112", "FIERY PATH", "ROUTE 113",
            "ROUTE 114", "METEOR FALLS", "ROUTE 115", "JAGGED PASS", "ROUTE 118", "ROUTE 119", "ROUTE 120", "ROUTE 121",
            "SAFARI ZONE", "ROUTE 122", "MT. PYRE", "ROUTE 123", "LILYCOVE CITY", "TEAM AQUA HIDEOUT",
            "TEAM MAGMA HIDEOUT", "ROUTE 124", "MOSSDEEP CITY", "ROUTE 125", "SHOAL CAVE", "ROUTE 127", "ROUTE 128",
            "SEAFLOOR CAVERN", "ROUTE 126", "SOOTOPOLIS CITY", "CAVE OF ORIGIN", "NEW MAUVILLE", "ROUTE 129",
            "ROUTE 130", "ROUTE 131", "PACIFIDLOG TOWN", "ROUTE 132", "ROUTE 133", "ROUTE 134", "SEALED CHAMBER",
            "ROUTE 105", "ROUTE 108", "SEA MAUVILLE", "SCORCHED SLAB", "EVER GRANDE CITY", "MIRAGE ISLAND",
            "MIRAGE FOREST", "MIRAGE CAVE", "MIRAGE MOUNTAIN", "VICTORY ROAD", "SKY PILLAR", "BATTLE RESORT"
    ));

    public static List<String> getLocationTagsTraverseOrder(int romType) {
        return romType == Type_XY ? locationTagsTraverseOrderXY : locationTagsTraverseOrderORAS;
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

    public static void tagEncounterAreas(List<EncounterArea> encounterAreas, int romType) {
        List<String> locationTags;
        int[] postGameAreas;
        switch (romType) {
            case Type_XY:
                locationTags = xyLocationTags;
                postGameAreas = xyPostGameEncounterAreas;
                break;
            case Type_ORAS:
                locationTags = orasLocationTags;
                postGameAreas = orasPostGameEncounterAreas;
                break;
            default:
                throw new IllegalStateException("Unexpected value for romType: " + romType);
        }
        tagEncounterAreas(encounterAreas, locationTags, postGameAreas);
    }

    public static final Map<Integer, Integer> balancedItemPrices = Stream.of(new Integer[][]{
            {ItemIDs.masterBall, 3000},
            {ItemIDs.safariBall, 500},
            {ItemIDs.freshWater, 400},
            {ItemIDs.sodaPop, 600},
            {ItemIDs.lemonade, 700},
            {ItemIDs.moomooMilk, 800},
            {ItemIDs.energyPowder, 400},
            {ItemIDs.energyRoot, 1100},
            {ItemIDs.ether, 3000},
            {ItemIDs.maxEther, 4500},
            {ItemIDs.elixir, 15000},
            {ItemIDs.maxElixir, 18000},
            {ItemIDs.lavaCookie, 450},
            {ItemIDs.sacredAsh, 10000},
            {ItemIDs.rareCandy, 10000},
            {ItemIDs.ppMax, 24900},
            {ItemIDs.oldGateau, 450},
            {ItemIDs.redShard, 400},
            {ItemIDs.blueShard, 400},
            {ItemIDs.yellowShard, 400},
            {ItemIDs.greenShard, 400},
            {ItemIDs.sunStone, 3000},
            {ItemIDs.moonStone, 3000},
            {ItemIDs.fireStone, 3000},
            {ItemIDs.thunderStone, 3000},
            {ItemIDs.waterStone, 3000},
            {ItemIDs.leafStone, 3000},
            {ItemIDs.heartScale, 5000},
            {ItemIDs.honey, 500},
            {ItemIDs.rootFossil, 5000},
            {ItemIDs.clawFossil, 5000},
            {ItemIDs.helixFossil, 5000},
            {ItemIDs.domeFossil, 5000},
            {ItemIDs.oldAmber, 8000},
            {ItemIDs.armorFossil, 5000},
            {ItemIDs.skullFossil, 5000},
            {ItemIDs.shinyStone, 3000},
            {ItemIDs.duskStone, 3000},
            {ItemIDs.dawnStone, 3000},
            {ItemIDs.ovalStone, 3000},
            {ItemIDs.sweetHeart, 150},
            {ItemIDs.cheriBerry, 200},
            {ItemIDs.chestoBerry, 250},
            {ItemIDs.pechaBerry, 100},
            {ItemIDs.rawstBerry, 250},
            {ItemIDs.aspearBerry, 250},
            {ItemIDs.leppaBerry, 3000},
            {ItemIDs.oranBerry, 50},
            {ItemIDs.persimBerry, 200},
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
            {ItemIDs.whiteHerb, 1000},
            {ItemIDs.expShare, 0},
            {ItemIDs.quickClaw, 4500},
            {ItemIDs.sootheBell, 1000},
            {ItemIDs.mentalHerb, 1000},
            {ItemIDs.choiceBand, 10000},
            {ItemIDs.kingsRock, 5000},
            {ItemIDs.silverPowder, 2000},
            {ItemIDs.amuletCoin, 15000},
            {ItemIDs.cleanseTag, 1000},
            {ItemIDs.deepSeaTooth, 3000},
            {ItemIDs.deepSeaScale, 3000},
            {ItemIDs.focusBand, 3000},
            {ItemIDs.luckyEgg, 10000},
            {ItemIDs.scopeLens, 5000},
            {ItemIDs.metalCoat, 3000},
            {ItemIDs.leftovers, 10000},
            {ItemIDs.dragonScale, 3000},
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
            {ItemIDs.wideLens, 1500},
            {ItemIDs.muscleBand, 2000},
            {ItemIDs.wiseGlasses, 2000},
            {ItemIDs.expertBelt, 6000},
            {ItemIDs.lightClay, 1500},
            {ItemIDs.lifeOrb, 10000},
            {ItemIDs.powerHerb, 1000},
            {ItemIDs.toxicOrb, 1500},
            {ItemIDs.flameOrb, 1500},
            {ItemIDs.focusSash, 2000},
            {ItemIDs.zoomLens, 1500},
            {ItemIDs.metronome, 3000},
            {ItemIDs.ironBall, 1000},
            {ItemIDs.laggingTail, 1000},
            {ItemIDs.destinyKnot, 1500},
            {ItemIDs.blackSludge, 5000},
            {ItemIDs.gripClaw, 1500},
            {ItemIDs.choiceScarf, 10000},
            {ItemIDs.stickyBarb, 1500},
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
            {ItemIDs.tm01, 10000},
            {ItemIDs.tm07, 20000},
            {ItemIDs.tm11, 20000},
            {ItemIDs.tm14, 20000},
            {ItemIDs.tm15, 20000},
            {ItemIDs.tm16, 20000},
            {ItemIDs.tm18, 20000},
            {ItemIDs.tm20, 20000},
            {ItemIDs.tm25, 20000},
            {ItemIDs.tm33, 20000},
            {ItemIDs.tm37, 20000},
            {ItemIDs.tm38, 20000},
            {ItemIDs.tm50, 10000},
            {ItemIDs.tm52, 10000},
            {ItemIDs.tm68, 20000},
            {ItemIDs.tm70, 10000},
            {ItemIDs.tm71, 10000},
            {ItemIDs.tm73, 10000},
            {ItemIDs.tm76, 10000},
            {ItemIDs.rageCandyBar, 150},
            {ItemIDs.prismScale, 3000},
            {ItemIDs.eviolite, 10000},
            {ItemIDs.floatStone, 1000},
            {ItemIDs.rockyHelmet, 6000},
            {ItemIDs.airBalloon, 1000},
            {ItemIDs.redCard, 1000},
            {ItemIDs.ringTarget, 1000},
            {ItemIDs.bindingBand, 2000},
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
            {ItemIDs.coverFossil, 5000},
            {ItemIDs.plumeFossil, 5000},
            {ItemIDs.dreamBall, 1000},
            {ItemIDs.relicGold, 0},
            {ItemIDs.casteliacone, 450},
            {ItemIDs.tm93, 10000},
            {ItemIDs.weaknessPolicy, 2000},
            {ItemIDs.assaultVest, 6000},
            {ItemIDs.pixiePlate, 2000},
            {ItemIDs.abilityCapsule, 5000},
            {ItemIDs.whippedDream, 3000},
            {ItemIDs.sachet, 3000},
            {ItemIDs.safetyGoggles, 3000},
            {ItemIDs.gengarite, 10000},
            {ItemIDs.gardevoirite, 10000},
            {ItemIDs.ampharosite, 10000},
            {ItemIDs.venusaurite, 10000},
            {ItemIDs.charizarditeX, 10000},
            {ItemIDs.blastoisinite, 10000},
            {ItemIDs.mewtwoniteX, 20000},
            {ItemIDs.mewtwoniteY, 20000},
            {ItemIDs.blazikenite, 10000},
            {ItemIDs.medichamite, 5000},
            {ItemIDs.houndoominite, 10000},
            {ItemIDs.aggronite, 10000},
            {ItemIDs.banettite, 5000},
            {ItemIDs.tyranitarite, 20000},
            {ItemIDs.scizorite, 10000},
            {ItemIDs.pinsirite, 10000},
            {ItemIDs.aerodactylite, 10000},
            {ItemIDs.lucarionite, 10000},
            {ItemIDs.abomasite, 5000},
            {ItemIDs.kangaskhanite, 5000},
            {ItemIDs.gyaradosite, 10000},
            {ItemIDs.absolite, 5000},
            {ItemIDs.charizarditeY, 10000},
            {ItemIDs.alakazite, 10000},
            {ItemIDs.heracronite, 10000},
            {ItemIDs.mawilite, 3000},
            {ItemIDs.manectite, 5000},
            {ItemIDs.garchompite, 20000},
            {ItemIDs.latiasite, 20000},
            {ItemIDs.latiosite, 20000},
            {ItemIDs.roseliBerry, 1000},
            {ItemIDs.keeBerry, 1000},
            {ItemIDs.marangaBerry, 1000},
            {ItemIDs.lumioseGalette, 450},
            {ItemIDs.shalourSable, 450},
            {ItemIDs.jawFossil, 5000},
            {ItemIDs.sailFossil, 5000},
            {ItemIDs.fairyGem, 1000},
            {ItemIDs.swampertite, 10000},
            {ItemIDs.sceptilite, 10000},
            {ItemIDs.sablenite, 3000},
            {ItemIDs.altarianite, 5000},
            {ItemIDs.galladite, 10000},
            {ItemIDs.audinite, 5000},
            {ItemIDs.metagrossite, 20000},
            {ItemIDs.sharpedonite, 5000},
            {ItemIDs.slowbronite, 5000},
            {ItemIDs.steelixite, 10000},
            {ItemIDs.pidgeotite, 5000},
            {ItemIDs.glalitite, 5000},
            {ItemIDs.diancite, 20000},
            {ItemIDs.cameruptite, 5000},
            {ItemIDs.lopunnite, 5000},
            {ItemIDs.salamencite, 20000},
            {ItemIDs.beedrillite, 3000},
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

    public static final int[] xyMapNumToPokedexIndex = {
            7,  // Couriway Town
            8,  // Ambrette Town
            13, // Cyllage City
            14, // Shalour City
            16, // Laverre City
            22, // Route 2
            23, // Route 3
            24, // Route 4
            25, // Route 5
            26, // Route 6
            27, // Route 7
            28, // Route 8
            29, // Route 9
            30, // Route 10
            31, // Route 11
            32, // Route 12
            33, // Route 13
            34, // Route 14
            35, // Route 15
            36, // Route 16
            37, // Route 17
            38, // Route 18
            39, // Route 19
            40, // Route 20
            41, // Route 21
            42, // Route 22
            44, // Santalune Forest
            45, // Parfum Palace
            46, 46, // Glittering Cave
            47, 47, 47, 47, // Reflection Cave
            49, 49, 49, 49, 49, // Frost Cavern
            50, // Pokemon Village
            51, 51, 51, 51, 51, // Victory Road
            52, // Connecting Cave
            54, 54, 54, 54, 54, // Terminus Cave
            55, // Lost Hotel
            43, // Azure Bay
            46, 46, 46, 46, // Glittering Cave (ceiling)
            47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, // Reflection Cave (ceiling)
            51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, // Victory Road (ceiling and sky)
            54, 54, 54, 54, 54, 54, 54, 54, 54, // Terminus Cave (ceiling)
            26, 26, 26, // Route 6 (rustling bush)
            38, 38, 38, 38 // Route 18 (rustling bush)
    };

    public static final int[] orasMapNumToPokedexIndex = {
            2,  // Dewford Town
            6,  // Pacifidlog Town
            7,  // Petalburg City
            8,  // Slateport City
            12, // Lilycove City
            13, // Mossdeep City
            14, // Sootopolis City
            15, // Ever Grande City
            17, // Route 101
            18, // Route 102
            19, // Route 103
            20, // Route 104 (North Section)
            21, // Route 104 (South Section)
            22, // Route 105
            23, // Route 106
            24, // Route 107
            26, // Route 108
            27, // Route 109
            28, // Route 110
            30, // Route 111 (Desert)
            32, // Route 111 (South Section)
            33, // Route 112 (North Section)
            34, // Route 112 (South Section)
            35, // Route 113
            36, // Route 114
            37, // Route 115
            38, // Route 116
            39, // Route 117
            40, // Route 118
            41, 41, // Route 119
            43, 43, // Route 120
            45, // Route 121
            46, // Route 122
            47, // Route 123
            48, // Route 124
            50, // Route 125
            51, // Route 126
            53, // Route 127
            55, // Route 128
            57, // Route 129
            59, // Route 130
            61, // Route 131
            62, // Route 132
            63, // Route 133
            64, // Route 134
            25, // Route 107 (Underwater)
            49, // Route 124 (Underwater)
            52, // Route 126 (Underwater)
            56, // Route 128 (Underwater)
            58, // Route 129 (Underwater)
            60, // Route 130 (Underwater)
            69, 69, 69, 69, // Meteor Falls
            73, // Rusturf Tunnel
            74, 74, 74, // Granite Cave
            78, // Petalburg Woods
            80, // Jagged Pass
            81, // Fiery Path
            82, 82, 82, 82, 82, 82, // Mt. Pyre
            -1, // Team Aqua Hideout
            88, 88, 88, 88, 88, 88, 88, 88, 88, 88, 88, // Seafloor Cavern
            102, 102, 102, 102, 102, // Cave of Origin
            114, 114, 114, 114, // Victory Road
            119, 119, 119, 119, 119, 119, 119, // Shoal Cave
            130, // New Mauville
            136, 136, 136, 136, // Sea Mauville
            -1, // Sealed Chamber
            -1, -1, -1, -1, // Scorched Slab
            -1, // Team Magma Hideout
            150, // Sky Pillar
            -1, -1, -1, -1, -1, -1, -1, -1, // Mirage Forest
            -1, -1, -1, -1, -1, -1, -1, -1, // Mirage Island
            -1, // Mirage Mountain
            159, // Battle Resort
            65, 65, 65, 65, // Safari Zone
            102, // Cave of Origin
            -1, -1, -1, -1, -1, -1, -1, // Mirage Mountain
            -1, -1, -1, -1, -1, -1, -1, -1, // Mirage Cave
            -1, // Mt. Pyre (unused)
            -1  // Sootopolis City (unused)
    };
}
