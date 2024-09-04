package com.dabomstew.pkrandom.constants;

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

import com.dabomstew.pkrandom.gamedata.*;

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

    public static final Map<Integer,List<Integer>> speciesToMegaStoneXY = setupSpeciesToMegaStone(Type_XY);
    public static final Map<Integer,List<Integer>> speciesToMegaStoneORAS = setupSpeciesToMegaStone(Type_ORAS);

    public static final Map<Integer,String> formeSuffixes = setupFormeSuffixes();
    public static final Map<Integer,String> dummyFormeSuffixes = setupDummyFormeSuffixes();
    public static final Map<Integer,Map<Integer,String>> formeSuffixesByBaseForme = setupFormeSuffixesByBaseForme();

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
            1, 9, 40, 19, 65, 73, 69, 74, 81, 57, 61, 97, 95, 71, 79, 30, 31, 36, 53, 29, 22, 3, 2, 80, 26);

    private static final List<Integer> requiredFieldTMsORAS = Arrays.asList(
            37, 32, 62, 11, 86, 29, 59, 43, 53, 69, 6, 2, 13, 18, 22, 61, 30, 97, 7, 90, 26, 55, 34, 35, 64, 65, 66,
            74, 79, 80, 81, 84, 89, 91, 93, 95);

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

    public static boolean isMegaStone(int itemIndex) {
        // These values come from https://bulbapedia.bulbagarden.net/wiki/List_of_items_by_index_number_(Generation_VI)
        return (itemIndex >= ItemIDs.gengarite && itemIndex <= ItemIDs.latiosite) ||
                (itemIndex >= ItemIDs.swampertite && itemIndex <= ItemIDs.diancite) ||
                (itemIndex >= ItemIDs.cameruptite && itemIndex <= ItemIDs.beedrillite);
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
            EvolutionType.LEVEL_FEMALE_ONLY, EvolutionType.LEVEL_ELECTRIFIED_AREA, EvolutionType.LEVEL_MOSS_ROCK,
            EvolutionType.LEVEL_ICY_ROCK, EvolutionType.LEVEL_UPSIDE_DOWN, EvolutionType.FAIRY_AFFECTION,
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

    public static String getSaveLoadFormeReversionPrefix(int romType) {
        if (romType == Type_XY) {
            return saveLoadFormeReversionPrefixXY;
        } else {
            return saveLoadFormeReversionPrefixORAS;
        }
    }

    private static Map<Integer,String> setupFormeSuffixes() {
        Map<Integer,String> formeSuffixes = new HashMap<>();
        formeSuffixes.put(SpeciesIDs.Gen6Formes.deoxysA,"-Attack");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.deoxysD,"-Defense");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.deoxysS,"-Speed");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.wormadamS,"-Sandy");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.wormadamT,"-Trash");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.shayminS,"-Sky");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.giratinaO,"-Origin");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.rotomH,"-Heat");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.rotomW,"-Wash");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.rotomFr,"-Frost");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.rotomFa,"-Fan");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.rotomM,"-Mow");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.castformSu,"-Sunny");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.castformR,"-Rainy");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.castformSn,"-Snowy");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.basculinB,"-Blue");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.darmanitanZ,"-Zen");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.meloettaP,"-Pirouette");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.kyuremW,"-White");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.kyuremB,"-Black");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.keldeoCosmetic1,"-R");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.tornadusT,"-Therian");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.thundurusT,"-Therian");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.landorusT,"-Therian");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.gengarMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.meowsticF,"-F");
        // 749 - 757 Furfrou
        formeSuffixes.put(SpeciesIDs.Gen6Formes.gardevoirMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.ampharosMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.venusaurMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.charizardMegaX,"-Mega-X");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.charizardMegaY,"-Mega-Y");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.mewtwoMegaX,"-Mega-X");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.mewtwoMegaY,"-Mega-Y");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.blazikenMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.medichamMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.houndoomMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.aggronMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.banetteMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.tyranitarMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.scizorMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.pinsirMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.aerodactylMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.lucarioMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.abomasnowMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.aegislashB,"-Blade");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.blastoiseMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.kangaskhanMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.gyaradosMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.absolMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.alakazamMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.heracrossMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.mawileMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.manectricMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.garchompMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.latiosMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.latiasMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.pumpkabooCosmetic1,"-M");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.pumpkabooCosmetic2,"-L");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.pumpkabooCosmetic3,"-XL");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.gourgeistCosmetic1,"-M");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.gourgeistCosmetic2,"-L");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.gourgeistCosmetic3,"-XL");
        // 794 - 797 Floette
        formeSuffixes.put(SpeciesIDs.Gen6Formes.floetteE,"-Eternal");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.swampertMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.sceptileMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.sableyeMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.altariaMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.galladeMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.audinoMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.sharpedoMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.slowbroMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.steelixMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.pidgeotMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.glalieMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.diancieMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.metagrossMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.kyogreP,"-Primal");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.groudonP,"-Primal");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.rayquazaMega,"-Mega");
        // 815 - 820 Cosplay Pikachu
        formeSuffixes.put(SpeciesIDs.Gen6Formes.hoopaU,"-Unbound");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.cameruptMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.lopunnyMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.salamenceMega,"-Mega");
        formeSuffixes.put(SpeciesIDs.Gen6Formes.beedrillMega,"-Mega");

        return formeSuffixes;
    }

    private static Map<Integer,Map<Integer,String>> setupFormeSuffixesByBaseForme() {
        Map<Integer,Map<Integer,String>> map = new HashMap<>();

        Map<Integer,String> deoxysMap = new HashMap<>();
        deoxysMap.put(1,"-A");
        deoxysMap.put(2,"-D");
        deoxysMap.put(3,"-S");
        map.put(SpeciesIDs.deoxys, deoxysMap);

        Map<Integer,String> wormadamMap = new HashMap<>();
        wormadamMap.put(1,"-S");
        wormadamMap.put(2,"-T");
        map.put(SpeciesIDs.wormadam, wormadamMap);

        Map<Integer,String> shayminMap = new HashMap<>();
        shayminMap.put(1,"-S");
        map.put(SpeciesIDs.shaymin, shayminMap);

        Map<Integer,String> giratinaMap = new HashMap<>();
        giratinaMap.put(1,"-O");
        map.put(SpeciesIDs.giratina, giratinaMap);

        Map<Integer,String> rotomMap = new HashMap<>();
        rotomMap.put(1,"-H");
        rotomMap.put(2,"-W");
        rotomMap.put(3,"-Fr");
        rotomMap.put(4,"-Fa");
        rotomMap.put(5,"-M");
        map.put(SpeciesIDs.rotom, rotomMap);

        Map<Integer,String> castformMap = new HashMap<>();
        castformMap.put(1,"-F");
        castformMap.put(2,"-W");
        castformMap.put(3,"-I");
        map.put(SpeciesIDs.castform, castformMap);

        Map<Integer,String> basculinMap = new HashMap<>();
        basculinMap.put(1,"-B");
        map.put(SpeciesIDs.basculin, basculinMap);

        Map<Integer,String> darmanitanMap = new HashMap<>();
        darmanitanMap.put(1,"-Z");
        map.put(SpeciesIDs.darmanitan, darmanitanMap);

        Map<Integer,String> meloettaMap = new HashMap<>();
        meloettaMap.put(1,"-P");
        map.put(SpeciesIDs.meloetta, meloettaMap);

        Map<Integer,String> kyuremMap = new HashMap<>();
        kyuremMap.put(1,"-W");
        kyuremMap.put(2,"-B");
        map.put(SpeciesIDs.kyurem, kyuremMap);

        Map<Integer,String> tornadusMap = new HashMap<>();
        tornadusMap.put(1,"-T");
        map.put(SpeciesIDs.tornadus, tornadusMap);

        Map<Integer,String> thundurusMap = new HashMap<>();
        thundurusMap.put(1,"-T");
        map.put(SpeciesIDs.thundurus, thundurusMap);

        Map<Integer,String> landorusMap = new HashMap<>();
        landorusMap.put(1,"-T");
        map.put(SpeciesIDs.landorus, landorusMap);

        Map<Integer,String> meowsticMap = new HashMap<>();
        meowsticMap.put(1,"-F");
        map.put(SpeciesIDs.meowstic, meowsticMap);

        Map<Integer,String> aegislashMap = new HashMap<>();
        aegislashMap.put(1,"-B");
        map.put(SpeciesIDs.aegislash, aegislashMap);

        Map<Integer,String> pumpkabooMap = new HashMap<>();
        pumpkabooMap.put(1,"-M");
        pumpkabooMap.put(2,"-L");
        pumpkabooMap.put(3,"-XL");
        map.put(SpeciesIDs.pumpkaboo, pumpkabooMap);

        Map<Integer,String> gourgeistMap = new HashMap<>();
        gourgeistMap.put(1,"-M");
        gourgeistMap.put(2,"-L");
        gourgeistMap.put(3,"-XL");
        map.put(SpeciesIDs.gourgeist, gourgeistMap);

        Map<Integer,String> floetteMap = new HashMap<>();
        floetteMap.put(5,"-E");
        map.put(SpeciesIDs.floette, floetteMap);

        Map<Integer,String> kyogreMap = new HashMap<>();
        kyogreMap.put(1,"-P");
        map.put(SpeciesIDs.kyogre, kyogreMap);

        Map<Integer,String> groudonMap = new HashMap<>();
        groudonMap.put(1,"-P");
        map.put(SpeciesIDs.groudon, groudonMap);

        Map<Integer,String> rayquazaMap = new HashMap<>();
        rayquazaMap.put(1,"-Mega");
        map.put(SpeciesIDs.rayquaza, rayquazaMap);

        Map<Integer,String> hoopaMap = new HashMap<>();
        hoopaMap.put(1,"-U");
        map.put(SpeciesIDs.hoopa, hoopaMap);

        for (Integer species: speciesToMegaStoneORAS.keySet()) {
            Map<Integer,String> megaMap = new HashMap<>();
            if (species == SpeciesIDs.charizard || species == SpeciesIDs.mewtwo) {
                megaMap.put(1,"-Mega-X");
                megaMap.put(2,"-Mega-Y");
            } else {
                megaMap.put(1,"-Mega");
            }
            map.put(species,megaMap);
        }

        return map;
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
    public static final Set<Integer> regularShopItems = setupRegularShopItems();
    public static final Set<Integer> opShopItems =  setupOPShopItems();

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
        // TMs & HMs - tms cant be held in gen6
        addBetween(set, ItemIDs.tm01, ItemIDs.hm08);
        addBetween(set, ItemIDs.tm93, ItemIDs.tm95);
        addBetween(set, ItemIDs.tm96, ItemIDs.tm100);
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

    public static final List<Integer> uniqueNoSellItems = Arrays.asList(ItemIDs.gengarite, ItemIDs.gardevoirite,
            ItemIDs.ampharosite, ItemIDs.venusaurite, ItemIDs.charizarditeX, ItemIDs.blastoisinite, ItemIDs.mewtwoniteX,
            ItemIDs.mewtwoniteY, ItemIDs.blazikenite, ItemIDs.medichamite, ItemIDs.houndoominite, ItemIDs.aggronite,
            ItemIDs.banettite, ItemIDs.tyranitarite, ItemIDs.scizorite, ItemIDs.pinsirite, ItemIDs.aerodactylite,
            ItemIDs.lucarionite, ItemIDs.abomasite, ItemIDs.kangaskhanite, ItemIDs.gyaradosite, ItemIDs.absolite,
            ItemIDs.charizarditeY, ItemIDs.alakazite, ItemIDs.heracronite, ItemIDs.mawilite, ItemIDs.manectite, ItemIDs.garchompite,
            ItemIDs.latiasite, ItemIDs.latiosite, ItemIDs.swampertite, ItemIDs.sceptilite, ItemIDs.sablenite, ItemIDs.altarianite,
            ItemIDs.galladite, ItemIDs.audinite, ItemIDs.metagrossite, ItemIDs.sharpedonite, ItemIDs.slowbronite,
            ItemIDs.steelixite, ItemIDs.pidgeotite, ItemIDs.glalitite, ItemIDs.diancite, ItemIDs.cameruptite, ItemIDs.lopunnite,
            ItemIDs.salamencite, ItemIDs.beedrillite);

    private static Map<Integer,List<Integer>> setupSpeciesToMegaStone(int romType) {
        Map<Integer,List<Integer>> map = new TreeMap<>();

        map.put(SpeciesIDs.venusaur, Collections.singletonList(ItemIDs.venusaurite));
        map.put(SpeciesIDs.charizard, Arrays.asList(ItemIDs.charizarditeX, ItemIDs.charizarditeY));
        map.put(SpeciesIDs.blastoise, Collections.singletonList(ItemIDs.blastoisinite));
        map.put(SpeciesIDs.alakazam, Collections.singletonList(ItemIDs.alakazite));
        map.put(SpeciesIDs.gengar, Collections.singletonList(ItemIDs.gengarite));
        map.put(SpeciesIDs.kangaskhan, Collections.singletonList(ItemIDs.kangaskhanite));
        map.put(SpeciesIDs.pinsir, Collections.singletonList(ItemIDs.pinsirite));
        map.put(SpeciesIDs.gyarados, Collections.singletonList(ItemIDs.gyaradosite));
        map.put(SpeciesIDs.aerodactyl, Collections.singletonList(ItemIDs.aerodactylite));
        map.put(SpeciesIDs.mewtwo, Arrays.asList(ItemIDs.mewtwoniteX, ItemIDs.mewtwoniteY));
        map.put(SpeciesIDs.ampharos, Collections.singletonList(ItemIDs.ampharosite));
        map.put(SpeciesIDs.scizor, Collections.singletonList(ItemIDs.scizorite));
        map.put(SpeciesIDs.heracross, Collections.singletonList(ItemIDs.heracronite));
        map.put(SpeciesIDs.houndoom, Collections.singletonList(ItemIDs.houndoominite));
        map.put(SpeciesIDs.tyranitar, Collections.singletonList(ItemIDs.tyranitarite));
        map.put(SpeciesIDs.blaziken, Collections.singletonList(ItemIDs.blazikenite));
        map.put(SpeciesIDs.gardevoir, Collections.singletonList(ItemIDs.gardevoirite));
        map.put(SpeciesIDs.mawile, Collections.singletonList(ItemIDs.mawilite));
        map.put(SpeciesIDs.aggron, Collections.singletonList(ItemIDs.aggronite));
        map.put(SpeciesIDs.medicham, Collections.singletonList(ItemIDs.medichamite));
        map.put(SpeciesIDs.manectric, Collections.singletonList(ItemIDs.manectite));
        map.put(SpeciesIDs.banette, Collections.singletonList(ItemIDs.banettite));
        map.put(SpeciesIDs.absol, Collections.singletonList(ItemIDs.absolite));
        map.put(SpeciesIDs.latias, Collections.singletonList(ItemIDs.latiasite));
        map.put(SpeciesIDs.latios, Collections.singletonList(ItemIDs.latiosite));
        map.put(SpeciesIDs.garchomp, Collections.singletonList(ItemIDs.garchompite));
        map.put(SpeciesIDs.lucario, Collections.singletonList(ItemIDs.lucarionite));
        map.put(SpeciesIDs.abomasnow, Collections.singletonList(ItemIDs.abomasite));

        if (romType == Type_ORAS) {
            map.put(SpeciesIDs.beedrill, Collections.singletonList(ItemIDs.beedrillite));
            map.put(SpeciesIDs.pidgeot, Collections.singletonList(ItemIDs.pidgeotite));
            map.put(SpeciesIDs.slowbro, Collections.singletonList(ItemIDs.slowbronite));
            map.put(SpeciesIDs.steelix, Collections.singletonList(ItemIDs.steelixite));
            map.put(SpeciesIDs.sceptile, Collections.singletonList(ItemIDs.sceptilite));
            map.put(SpeciesIDs.swampert, Collections.singletonList(ItemIDs.swampertite));
            map.put(SpeciesIDs.sableye, Collections.singletonList(ItemIDs.sablenite));
            map.put(SpeciesIDs.sharpedo, Collections.singletonList(ItemIDs.sharpedonite));
            map.put(SpeciesIDs.camerupt, Collections.singletonList(ItemIDs.cameruptite));
            map.put(SpeciesIDs.altaria, Collections.singletonList(ItemIDs.altarianite));
            map.put(SpeciesIDs.glalie, Collections.singletonList(ItemIDs.glalitite));
            map.put(SpeciesIDs.salamence, Collections.singletonList(ItemIDs.salamencite));
            map.put(SpeciesIDs.metagross, Collections.singletonList(ItemIDs.metagrossite));
            map.put(SpeciesIDs.lopunny, Collections.singletonList(ItemIDs.lopunnite));
            map.put(SpeciesIDs.gallade, Collections.singletonList(ItemIDs.galladite));
            map.put(SpeciesIDs.audino, Collections.singletonList(ItemIDs.audinite));
            map.put(SpeciesIDs.diancie, Collections.singletonList(ItemIDs.diancite));
        }

        return map;
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
        tagRival(trs, "RIVAL1", 596);
        tagRival(trs, "RIVAL2", 575);
        tagRival(trs, "RIVAL3", 581);
        tagRival(trs, "RIVAL4", 578);
        tagRival(trs, "RIVAL5", 584);
        tagRival(trs, "RIVAL6", 607);
        tagRival(trs, "RIVAL7", 587);
        tagRival(trs, "RIVAL8", 590);
        tagRival(trs, "RIVAL9", 593);
        tagRival(trs, "RIVAL10", 599);

        // Rival - Calem
        tagRival(trs, "RIVAL1", 435);
        tagRival(trs, "RIVAL2", 130);
        tagRival(trs, "RIVAL3", 329);
        tagRival(trs, "RIVAL4", 184);
        tagRival(trs, "RIVAL5", 332);
        tagRival(trs, "RIVAL6", 604);
        tagRival(trs, "RIVAL7", 335);
        tagRival(trs, "RIVAL8", 338);
        tagRival(trs, "RIVAL9", 341);
        tagRival(trs, "RIVAL10", 519);

        // Rival - Shauna
        tagRival(trs, "FRIEND1", 137);
        tagRival(trs, "FRIEND2", 321);
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
        tagRival(trs, "RIVAL1", 1);
        tagRival(trs, "RIVAL2", 289);
        tagRival(trs, "RIVAL3", 674);
        tagRival(trs, "RIVAL4", 292);
        tagRival(trs, "RIVAL5", 527);
        tagRival(trs, "RIVAL6", 699);

        // Rival - May
        tagRival(trs, "RIVAL1", 4);
        tagRival(trs, "RIVAL2", 295);
        tagRival(trs, "RIVAL3", 677);
        tagRival(trs, "RIVAL4", 298);
        tagRival(trs, "RIVAL5", 530);
        tagRival(trs, "RIVAL6", 906);
    }

    private static void tagRival(List<Trainer> allTrainers, String tag, int offset) {
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
    public static final List<String> locationTagsTraverseOrderXY = Collections.unmodifiableList(Arrays.asList(
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
    public static final List<String> locationTagsTraverseOrderORAS = Collections.unmodifiableList(Arrays.asList(
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

    public static final Map<Integer,Integer> balancedItemPrices = Stream.of(new Integer[][] {
            // Skip item index 0. All prices divided by 10
            {ItemIDs.masterBall, 300},
            {ItemIDs.ultraBall, 120},
            {ItemIDs.greatBall, 60},
            {ItemIDs.pokeBall, 20},
            {ItemIDs.safariBall, 50},
            {ItemIDs.netBall, 100},
            {ItemIDs.diveBall, 100},
            {ItemIDs.nestBall, 100},
            {ItemIDs.repeatBall, 100},
            {ItemIDs.timerBall, 100},
            {ItemIDs.luxuryBall, 100},
            {ItemIDs.premierBall, 20},
            {ItemIDs.duskBall, 100},
            {ItemIDs.healBall, 30},
            {ItemIDs.quickBall, 100},
            {ItemIDs.cherishBall, 20},
            {ItemIDs.potion, 30},
            {ItemIDs.antidote, 10},
            {ItemIDs.burnHeal, 25},
            {ItemIDs.iceHeal, 25},
            {ItemIDs.awakening, 25},
            {ItemIDs.paralyzeHeal, 20},
            {ItemIDs.fullRestore, 300},
            {ItemIDs.maxPotion, 250},
            {ItemIDs.hyperPotion, 120},
            {ItemIDs.superPotion, 70},
            {ItemIDs.fullHeal, 60},
            {ItemIDs.revive, 150},
            {ItemIDs.maxRevive, 400},
            {ItemIDs.freshWater, 40},
            {ItemIDs.sodaPop, 60},
            {ItemIDs.lemonade, 70},
            {ItemIDs.moomooMilk, 80},
            {ItemIDs.energyPowder, 40},
            {ItemIDs.energyRoot, 110},
            {ItemIDs.healPowder, 45},
            {ItemIDs.revivalHerb, 280},
            {ItemIDs.ether, 300},
            {ItemIDs.maxEther, 450},
            {ItemIDs.elixir, 1500},
            {ItemIDs.maxElixir, 1800},
            {ItemIDs.lavaCookie, 45},
            {ItemIDs.berryJuice, 10},
            {ItemIDs.sacredAsh, 1000},
            {ItemIDs.hpUp, 980},
            {ItemIDs.protein, 980},
            {ItemIDs.iron, 980},
            {ItemIDs.carbos, 980},
            {ItemIDs.calcium, 980},
            {ItemIDs.rareCandy, 1000},
            {ItemIDs.ppUp, 980},
            {ItemIDs.zinc, 980},
            {ItemIDs.ppMax, 2490},
            {ItemIDs.oldGateau, 45},
            {ItemIDs.guardSpec, 70},
            {ItemIDs.direHit, 65},
            {ItemIDs.xAttack, 50},
            {ItemIDs.xDefense, 55},
            {ItemIDs.xSpeed, 35},
            {ItemIDs.xAccuracy, 95},
            {ItemIDs.xSpAtk, 35},
            {ItemIDs.xSpDef, 35},
            {ItemIDs.pokeDoll, 100},
            {ItemIDs.fluffyTail, 100},
            {ItemIDs.blueFlute, 2},
            {ItemIDs.yellowFlute, 2},
            {ItemIDs.redFlute, 2},
            {ItemIDs.blackFlute, 2},
            {ItemIDs.whiteFlute, 2},
            {ItemIDs.shoalSalt, 2},
            {ItemIDs.shoalShell, 2},
            {ItemIDs.redShard, 40},
            {ItemIDs.blueShard, 40},
            {ItemIDs.yellowShard, 40},
            {ItemIDs.greenShard, 40},
            {ItemIDs.superRepel, 50},
            {ItemIDs.maxRepel, 70},
            {ItemIDs.escapeRope, 55},
            {ItemIDs.repel, 35},
            {ItemIDs.sunStone, 300},
            {ItemIDs.moonStone, 300},
            {ItemIDs.fireStone, 300},
            {ItemIDs.thunderStone, 300},
            {ItemIDs.waterStone, 300},
            {ItemIDs.leafStone, 300},
            {ItemIDs.tinyMushroom, 50},
            {ItemIDs.bigMushroom, 500},
            {ItemIDs.pearl, 140},
            {ItemIDs.bigPearl, 750},
            {ItemIDs.stardust, 200},
            {ItemIDs.starPiece, 980},
            {ItemIDs.nugget, 1000},
            {ItemIDs.heartScale, 500},
            {ItemIDs.honey, 50},
            {ItemIDs.growthMulch, 20},
            {ItemIDs.dampMulch, 20},
            {ItemIDs.stableMulch, 20},
            {ItemIDs.gooeyMulch, 20},
            {ItemIDs.rootFossil, 500},
            {ItemIDs.clawFossil, 500},
            {ItemIDs.helixFossil, 500},
            {ItemIDs.domeFossil, 500},
            {ItemIDs.oldAmber, 800},
            {ItemIDs.armorFossil, 500},
            {ItemIDs.skullFossil, 500},
            {ItemIDs.rareBone, 1000},
            {ItemIDs.shinyStone, 300},
            {ItemIDs.duskStone, 300},
            {ItemIDs.dawnStone, 300},
            {ItemIDs.ovalStone, 300},
            {ItemIDs.oddKeystone, 210},
            {ItemIDs.griseousOrb, 1000},
            {ItemIDs.tea, 0}, // unused in Gen 6
            {ItemIDs.unused114, 0},
            {ItemIDs.autograph, 0},
            {ItemIDs.douseDrive, 100},
            {ItemIDs.shockDrive, 100},
            {ItemIDs.burnDrive, 100},
            {ItemIDs.chillDrive, 100},
            {ItemIDs.unused120, 0},
            {ItemIDs.pokemonBox, 0}, // unused in Gen 6
            {ItemIDs.medicinePocket, 0}, // unused in Gen 6
            {ItemIDs.tmCase, 0}, // unused in Gen 6
            {ItemIDs.candyJar, 0}, // unused in Gen 6
            {ItemIDs.powerUpPocket, 0}, // unused in Gen 6
            {ItemIDs.clothingTrunk, 0}, // unused in Gen 6
            {ItemIDs.catchingPocket, 0}, // unused in Gen 6
            {ItemIDs.battlePocket, 0}, // unused in Gen 6
            {ItemIDs.unused129, 0},
            {ItemIDs.unused130, 0},
            {ItemIDs.unused131, 0},
            {ItemIDs.unused132, 0},
            {ItemIDs.unused133, 0},
            {ItemIDs.sweetHeart, 15},
            {ItemIDs.adamantOrb, 1000},
            {ItemIDs.lustrousOrb, 1000},
            {ItemIDs.mail1, 5},
            {ItemIDs.mail2, 5},
            {ItemIDs.mail3, 5},
            {ItemIDs.mail4, 5},
            {ItemIDs.mail5, 5},
            {ItemIDs.mail6, 5},
            {ItemIDs.mail7, 5},
            {ItemIDs.mail8, 5},
            {ItemIDs.mail9, 5},
            {ItemIDs.mail10, 5},
            {ItemIDs.mail11, 5},
            {ItemIDs.mail12, 5},
            {ItemIDs.cheriBerry, 20},
            {ItemIDs.chestoBerry, 25},
            {ItemIDs.pechaBerry, 10},
            {ItemIDs.rawstBerry, 25},
            {ItemIDs.aspearBerry, 25},
            {ItemIDs.leppaBerry, 300},
            {ItemIDs.oranBerry, 5},
            {ItemIDs.persimBerry, 20},
            {ItemIDs.lumBerry, 50},
            {ItemIDs.sitrusBerry, 50},
            {ItemIDs.figyBerry, 10},
            {ItemIDs.wikiBerry, 10},
            {ItemIDs.magoBerry, 10},
            {ItemIDs.aguavBerry, 10},
            {ItemIDs.iapapaBerry, 10},
            {ItemIDs.razzBerry, 50},
            {ItemIDs.blukBerry, 50},
            {ItemIDs.nanabBerry, 50},
            {ItemIDs.wepearBerry, 50},
            {ItemIDs.pinapBerry, 50},
            {ItemIDs.pomegBerry, 50},
            {ItemIDs.kelpsyBerry, 50},
            {ItemIDs.qualotBerry, 50},
            {ItemIDs.hondewBerry, 50},
            {ItemIDs.grepaBerry, 50},
            {ItemIDs.tamatoBerry, 50},
            {ItemIDs.cornnBerry, 50},
            {ItemIDs.magostBerry, 50},
            {ItemIDs.rabutaBerry, 50},
            {ItemIDs.nomelBerry, 50},
            {ItemIDs.spelonBerry, 50},
            {ItemIDs.pamtreBerry, 50},
            {ItemIDs.watmelBerry, 50},
            {ItemIDs.durinBerry, 50},
            {ItemIDs.belueBerry, 50},
            {ItemIDs.occaBerry, 100},
            {ItemIDs.passhoBerry, 100},
            {ItemIDs.wacanBerry, 100},
            {ItemIDs.rindoBerry, 100},
            {ItemIDs.yacheBerry, 100},
            {ItemIDs.chopleBerry, 100},
            {ItemIDs.kebiaBerry, 100},
            {ItemIDs.shucaBerry, 100},
            {ItemIDs.cobaBerry, 100},
            {ItemIDs.payapaBerry, 100},
            {ItemIDs.tangaBerry, 100},
            {ItemIDs.chartiBerry, 100},
            {ItemIDs.kasibBerry, 100},
            {ItemIDs.habanBerry, 100},
            {ItemIDs.colburBerry, 100},
            {ItemIDs.babiriBerry, 100},
            {ItemIDs.chilanBerry, 100},
            {ItemIDs.liechiBerry, 100},
            {ItemIDs.ganlonBerry, 100},
            {ItemIDs.salacBerry, 100},
            {ItemIDs.petayaBerry, 100},
            {ItemIDs.apicotBerry, 100},
            {ItemIDs.lansatBerry, 100},
            {ItemIDs.starfBerry, 100},
            {ItemIDs.enigmaBerry, 100},
            {ItemIDs.micleBerry, 100},
            {ItemIDs.custapBerry, 100},
            {ItemIDs.jabocaBerry, 100},
            {ItemIDs.rowapBerry, 100},
            {ItemIDs.brightPowder, 300},
            {ItemIDs.whiteHerb, 100},
            {ItemIDs.machoBrace, 300},
            {ItemIDs.expShare, 0},
            {ItemIDs.quickClaw, 450},
            {ItemIDs.sootheBell, 100},
            {ItemIDs.mentalHerb, 100},
            {ItemIDs.choiceBand, 1000},
            {ItemIDs.kingsRock, 500},
            {ItemIDs.silverPowder, 200},
            {ItemIDs.amuletCoin, 1500},
            {ItemIDs.cleanseTag, 100},
            {ItemIDs.soulDew, 20},
            {ItemIDs.deepSeaTooth, 300},
            {ItemIDs.deepSeaScale, 300},
            {ItemIDs.smokeBall, 20},
            {ItemIDs.everstone, 20},
            {ItemIDs.focusBand, 300},
            {ItemIDs.luckyEgg, 1000},
            {ItemIDs.scopeLens, 500},
            {ItemIDs.metalCoat, 300},
            {ItemIDs.leftovers, 1000},
            {ItemIDs.dragonScale, 300},
            {ItemIDs.lightBall, 10},
            {ItemIDs.softSand, 200},
            {ItemIDs.hardStone, 200},
            {ItemIDs.miracleSeed, 200},
            {ItemIDs.blackGlasses, 200},
            {ItemIDs.blackBelt, 200},
            {ItemIDs.magnet, 200},
            {ItemIDs.mysticWater, 200},
            {ItemIDs.sharpBeak, 200},
            {ItemIDs.poisonBarb, 200},
            {ItemIDs.neverMeltIce, 200},
            {ItemIDs.spellTag, 200},
            {ItemIDs.twistedSpoon, 200},
            {ItemIDs.charcoal, 200},
            {ItemIDs.dragonFang, 200},
            {ItemIDs.silkScarf, 200},
            {ItemIDs.upgrade, 300},
            {ItemIDs.shellBell, 600},
            {ItemIDs.seaIncense, 200},
            {ItemIDs.laxIncense, 300},
            {ItemIDs.luckyPunch, 1},
            {ItemIDs.metalPowder, 1},
            {ItemIDs.thickClub, 50},
            {ItemIDs.leek, 20},
            {ItemIDs.redScarf, 10},
            {ItemIDs.blueScarf, 10},
            {ItemIDs.pinkScarf, 10},
            {ItemIDs.greenScarf, 10},
            {ItemIDs.yellowScarf, 10},
            {ItemIDs.wideLens, 150},
            {ItemIDs.muscleBand, 200},
            {ItemIDs.wiseGlasses, 200},
            {ItemIDs.expertBelt, 600},
            {ItemIDs.lightClay, 150},
            {ItemIDs.lifeOrb, 1000},
            {ItemIDs.powerHerb, 100},
            {ItemIDs.toxicOrb, 150},
            {ItemIDs.flameOrb, 150},
            {ItemIDs.quickPowder, 1},
            {ItemIDs.focusSash, 200},
            {ItemIDs.zoomLens, 150},
            {ItemIDs.metronome, 300},
            {ItemIDs.ironBall, 100},
            {ItemIDs.laggingTail, 100},
            {ItemIDs.destinyKnot, 150},
            {ItemIDs.blackSludge, 500},
            {ItemIDs.icyRock, 20},
            {ItemIDs.smoothRock, 20},
            {ItemIDs.heatRock, 20},
            {ItemIDs.dampRock, 20},
            {ItemIDs.gripClaw, 150},
            {ItemIDs.choiceScarf, 1000},
            {ItemIDs.stickyBarb, 150},
            {ItemIDs.powerBracer, 300},
            {ItemIDs.powerBelt, 300},
            {ItemIDs.powerLens, 300},
            {ItemIDs.powerBand, 300},
            {ItemIDs.powerAnklet, 300},
            {ItemIDs.powerWeight, 300},
            {ItemIDs.shedShell, 50},
            {ItemIDs.bigRoot, 150},
            {ItemIDs.choiceSpecs, 1000},
            {ItemIDs.flamePlate, 200},
            {ItemIDs.splashPlate, 200},
            {ItemIDs.zapPlate, 200},
            {ItemIDs.meadowPlate, 200},
            {ItemIDs.iciclePlate, 200},
            {ItemIDs.fistPlate, 200},
            {ItemIDs.toxicPlate, 200},
            {ItemIDs.earthPlate, 200},
            {ItemIDs.skyPlate, 200},
            {ItemIDs.mindPlate, 200},
            {ItemIDs.insectPlate, 200},
            {ItemIDs.stonePlate, 200},
            {ItemIDs.spookyPlate, 200},
            {ItemIDs.dracoPlate, 200},
            {ItemIDs.dreadPlate, 200},
            {ItemIDs.ironPlate, 200},
            {ItemIDs.oddIncense, 200},
            {ItemIDs.rockIncense, 200},
            {ItemIDs.fullIncense, 100},
            {ItemIDs.waveIncense, 200},
            {ItemIDs.roseIncense, 200},
            {ItemIDs.luckIncense, 1500},
            {ItemIDs.pureIncense, 100},
            {ItemIDs.protector, 300},
            {ItemIDs.electirizer, 300},
            {ItemIDs.magmarizer, 300},
            {ItemIDs.dubiousDisc, 300},
            {ItemIDs.reaperCloth, 300},
            {ItemIDs.razorClaw, 500},
            {ItemIDs.razorFang, 500},
            {ItemIDs.tm01, 1000},
            {ItemIDs.tm02, 1000},
            {ItemIDs.tm03, 1000},
            {ItemIDs.tm04, 1000},
            {ItemIDs.tm05, 1000},
            {ItemIDs.tm06, 1000},
            {ItemIDs.tm07, 2000},
            {ItemIDs.tm08, 1000},
            {ItemIDs.tm09, 1000},
            {ItemIDs.tm10, 1000},
            {ItemIDs.tm11, 2000},
            {ItemIDs.tm12, 1000},
            {ItemIDs.tm13, 1000},
            {ItemIDs.tm14, 2000},
            {ItemIDs.tm15, 2000},
            {ItemIDs.tm16, 2000},
            {ItemIDs.tm17, 1000},
            {ItemIDs.tm18, 2000},
            {ItemIDs.tm19, 1000},
            {ItemIDs.tm20, 2000},
            {ItemIDs.tm21, 1000},
            {ItemIDs.tm22, 1000},
            {ItemIDs.tm23, 1000},
            {ItemIDs.tm24, 1000},
            {ItemIDs.tm25, 2000},
            {ItemIDs.tm26, 1000},
            {ItemIDs.tm27, 1000},
            {ItemIDs.tm28, 1000},
            {ItemIDs.tm29, 1000},
            {ItemIDs.tm30, 1000},
            {ItemIDs.tm31, 1000},
            {ItemIDs.tm32, 1000},
            {ItemIDs.tm33, 2000},
            {ItemIDs.tm34, 1000},
            {ItemIDs.tm35, 1000},
            {ItemIDs.tm36, 1000},
            {ItemIDs.tm37, 2000},
            {ItemIDs.tm38, 2000},
            {ItemIDs.tm39, 1000},
            {ItemIDs.tm40, 1000},
            {ItemIDs.tm41, 1000},
            {ItemIDs.tm42, 1000},
            {ItemIDs.tm43, 1000},
            {ItemIDs.tm44, 1000},
            {ItemIDs.tm45, 1000},
            {ItemIDs.tm46, 1000},
            {ItemIDs.tm47, 1000},
            {ItemIDs.tm48, 1000},
            {ItemIDs.tm49, 1000},
            {ItemIDs.tm50, 1000},
            {ItemIDs.tm51, 1000},
            {ItemIDs.tm52, 1000},
            {ItemIDs.tm53, 1000},
            {ItemIDs.tm54, 1000},
            {ItemIDs.tm55, 1000},
            {ItemIDs.tm56, 1000},
            {ItemIDs.tm57, 1000},
            {ItemIDs.tm58, 1000},
            {ItemIDs.tm59, 1000},
            {ItemIDs.tm60, 1000},
            {ItemIDs.tm61, 1000},
            {ItemIDs.tm62, 1000},
            {ItemIDs.tm63, 1000},
            {ItemIDs.tm64, 1000},
            {ItemIDs.tm65, 1000},
            {ItemIDs.tm66, 1000},
            {ItemIDs.tm67, 1000},
            {ItemIDs.tm68, 2000},
            {ItemIDs.tm69, 1000},
            {ItemIDs.tm70, 1000},
            {ItemIDs.tm71, 1000},
            {ItemIDs.tm72, 1000},
            {ItemIDs.tm73, 1000},
            {ItemIDs.tm74, 1000},
            {ItemIDs.tm75, 1000},
            {ItemIDs.tm76, 1000},
            {ItemIDs.tm77, 1000},
            {ItemIDs.tm78, 1000},
            {ItemIDs.tm79, 1000},
            {ItemIDs.tm80, 1000},
            {ItemIDs.tm81, 1000},
            {ItemIDs.tm82, 1000},
            {ItemIDs.tm83, 1000},
            {ItemIDs.tm84, 1000},
            {ItemIDs.tm85, 1000},
            {ItemIDs.tm86, 1000},
            {ItemIDs.tm87, 1000},
            {ItemIDs.tm88, 1000},
            {ItemIDs.tm89, 1000},
            {ItemIDs.tm90, 1000},
            {ItemIDs.tm91, 1000},
            {ItemIDs.tm92, 1000},
            {ItemIDs.hm01, 0},
            {ItemIDs.hm02, 0},
            {ItemIDs.hm03, 0},
            {ItemIDs.hm04, 0},
            {ItemIDs.hm05, 0},
            {ItemIDs.hm06, 0},
            {ItemIDs.hm07, 0}, // unused in Gen 6
            {ItemIDs.hm08, 0}, // unused in Gen 6
            {ItemIDs.explorerKit, 0},
            {ItemIDs.lootSack, 0},
            {ItemIDs.ruleBook, 0},
            {ItemIDs.pokeRadar, 0},
            {ItemIDs.pointCard, 0},
            {ItemIDs.journal, 0},
            {ItemIDs.sealCase, 0},
            {ItemIDs.fashionCase, 0},
            {ItemIDs.sealBag, 0},
            {ItemIDs.palPad, 0},
            {ItemIDs.worksKey, 0},
            {ItemIDs.oldCharm, 0},
            {ItemIDs.galacticKey, 0},
            {ItemIDs.redChain, 0},
            {ItemIDs.townMap, 0},
            {ItemIDs.vsSeeker, 0},
            {ItemIDs.coinCase, 0},
            {ItemIDs.oldRod, 0},
            {ItemIDs.goodRod, 0},
            {ItemIDs.superRod, 0},
            {ItemIDs.sprayduck, 0},
            {ItemIDs.poffinCase, 0},
            {ItemIDs.bike, 0},
            {ItemIDs.suiteKey, 0},
            {ItemIDs.oaksLetter, 0},
            {ItemIDs.lunarWing, 0},
            {ItemIDs.memberCard, 0},
            {ItemIDs.azureFlute, 0},
            {ItemIDs.ssTicketJohto, 0},
            {ItemIDs.contestPass, 0},
            {ItemIDs.magmaStone, 0},
            {ItemIDs.parcelSinnoh, 0},
            {ItemIDs.coupon1, 0},
            {ItemIDs.coupon2, 0},
            {ItemIDs.coupon3, 0},
            {ItemIDs.storageKeySinnoh, 0},
            {ItemIDs.secretPotion, 0},
            {ItemIDs.vsRecorder, 0},
            {ItemIDs.gracidea, 0},
            {ItemIDs.secretKeySinnoh, 0},
            {ItemIDs.apricornBox, 0},
            {ItemIDs.unownReport, 0},
            {ItemIDs.berryPots, 0},
            {ItemIDs.dowsingMachine, 0},
            {ItemIDs.blueCard, 0},
            {ItemIDs.slowpokeTail, 0},
            {ItemIDs.clearBell, 0},
            {ItemIDs.cardKeyJohto, 0},
            {ItemIDs.basementKeyJohto, 0},
            {ItemIDs.squirtBottle, 0},
            {ItemIDs.redScale, 0},
            {ItemIDs.lostItem, 0},
            {ItemIDs.pass, 0},
            {ItemIDs.machinePart, 0},
            {ItemIDs.silverWing, 0},
            {ItemIDs.rainbowWing, 0},
            {ItemIDs.mysteryEgg, 0},
            {ItemIDs.redApricorn, 2},
            {ItemIDs.blueApricorn, 2},
            {ItemIDs.yellowApricorn, 2},
            {ItemIDs.greenApricorn, 2},
            {ItemIDs.pinkApricorn, 2},
            {ItemIDs.whiteApricorn, 2},
            {ItemIDs.blackApricorn, 2},
            {ItemIDs.fastBall, 30},
            {ItemIDs.levelBall, 30},
            {ItemIDs.lureBall, 30},
            {ItemIDs.heavyBall, 30},
            {ItemIDs.loveBall, 30},
            {ItemIDs.friendBall, 30},
            {ItemIDs.moonBall, 30},
            {ItemIDs.sportBall, 30},
            {ItemIDs.parkBall, 0},
            {ItemIDs.photoAlbum, 0},
            {ItemIDs.gbSounds, 0},
            {ItemIDs.tidalBell, 0},
            {ItemIDs.rageCandyBar, 15},
            {ItemIDs.dataCard01, 0},
            {ItemIDs.dataCard02, 0},
            {ItemIDs.dataCard03, 0},
            {ItemIDs.dataCard04, 0},
            {ItemIDs.dataCard05, 0},
            {ItemIDs.dataCard06, 0},
            {ItemIDs.dataCard07, 0},
            {ItemIDs.dataCard08, 0},
            {ItemIDs.dataCard09, 0},
            {ItemIDs.dataCard10, 0},
            {ItemIDs.dataCard11, 0},
            {ItemIDs.dataCard12, 0},
            {ItemIDs.dataCard13, 0},
            {ItemIDs.dataCard14, 0},
            {ItemIDs.dataCard15, 0},
            {ItemIDs.dataCard16, 0},
            {ItemIDs.dataCard17, 0},
            {ItemIDs.dataCard18, 0},
            {ItemIDs.dataCard19, 0},
            {ItemIDs.dataCard20, 0},
            {ItemIDs.dataCard21, 0},
            {ItemIDs.dataCard22, 0},
            {ItemIDs.dataCard23, 0},
            {ItemIDs.dataCard24, 0},
            {ItemIDs.dataCard25, 0},
            {ItemIDs.dataCard26, 0},
            {ItemIDs.dataCard27, 0},
            {ItemIDs.jadeOrb, 0},
            {ItemIDs.lockCapsule, 0},
            {ItemIDs.redOrb, 0},
            {ItemIDs.blueOrb, 0},
            {ItemIDs.enigmaStone, 0},
            {ItemIDs.prismScale, 300},
            {ItemIDs.eviolite, 1000},
            {ItemIDs.floatStone, 100},
            {ItemIDs.rockyHelmet, 600},
            {ItemIDs.airBalloon, 100},
            {ItemIDs.redCard, 100},
            {ItemIDs.ringTarget, 100},
            {ItemIDs.bindingBand, 200},
            {ItemIDs.absorbBulb, 100},
            {ItemIDs.cellBattery, 100},
            {ItemIDs.ejectButton, 100},
            {ItemIDs.fireGem, 100},
            {ItemIDs.waterGem, 100},
            {ItemIDs.electricGem, 100},
            {ItemIDs.grassGem, 100},
            {ItemIDs.iceGem, 100},
            {ItemIDs.fightingGem, 100},
            {ItemIDs.poisonGem, 100},
            {ItemIDs.groundGem, 100},
            {ItemIDs.flyingGem, 100},
            {ItemIDs.psychicGem, 100},
            {ItemIDs.bugGem, 100},
            {ItemIDs.rockGem, 100},
            {ItemIDs.ghostGem, 100},
            {ItemIDs.dragonGem, 100},
            {ItemIDs.darkGem, 100},
            {ItemIDs.steelGem, 100},
            {ItemIDs.normalGem, 100},
            {ItemIDs.healthFeather, 300},
            {ItemIDs.muscleFeather, 300},
            {ItemIDs.resistFeather, 300},
            {ItemIDs.geniusFeather, 300},
            {ItemIDs.cleverFeather, 300},
            {ItemIDs.swiftFeather, 300},
            {ItemIDs.prettyFeather, 20},
            {ItemIDs.coverFossil, 500},
            {ItemIDs.plumeFossil, 500},
            {ItemIDs.libertyPass, 0},
            {ItemIDs.passOrb, 20},
            {ItemIDs.dreamBall, 100},
            {ItemIDs.pokeToy, 100},
            {ItemIDs.propCase, 0},
            {ItemIDs.dragonSkull, 0},
            {ItemIDs.balmMushroom, 1250},
            {ItemIDs.bigNugget, 2000},
            {ItemIDs.pearlString, 1500},
            {ItemIDs.cometShard, 3000},
            {ItemIDs.relicCopper, 0},
            {ItemIDs.relicSilver, 0},
            {ItemIDs.relicGold, 0},
            {ItemIDs.relicVase, 0},
            {ItemIDs.relicBand, 0},
            {ItemIDs.relicStatue, 0},
            {ItemIDs.relicCrown, 0},
            {ItemIDs.casteliacone, 45},
            {ItemIDs.direHit2, 0},
            {ItemIDs.xSpeed2, 0},
            {ItemIDs.xSpAtk2, 0},
            {ItemIDs.xSpDef2, 0},
            {ItemIDs.xDefense2, 0},
            {ItemIDs.xAttack2, 0},
            {ItemIDs.xAccuracy2, 0},
            {ItemIDs.xSpeed3, 0},
            {ItemIDs.xSpAtk3, 0},
            {ItemIDs.xSpDef3, 0},
            {ItemIDs.xDefense3, 0},
            {ItemIDs.xAttack3, 0},
            {ItemIDs.xAccuracy3, 0},
            {ItemIDs.xSpeed6, 0},
            {ItemIDs.xSpAtk6, 0},
            {ItemIDs.xSpDef6, 0},
            {ItemIDs.xDefense6, 0},
            {ItemIDs.xAttack6, 0},
            {ItemIDs.xAccuracy6, 0},
            {ItemIDs.abilityUrge, 0},
            {ItemIDs.itemDrop, 0},
            {ItemIDs.itemUrge, 0},
            {ItemIDs.resetUrge, 0},
            {ItemIDs.direHit3, 0},
            {ItemIDs.lightStone, 0},
            {ItemIDs.darkStone, 0},
            {ItemIDs.tm93, 1000},
            {ItemIDs.tm94, 1000},
            {ItemIDs.tm95, 1000},
            {ItemIDs.xtransceiverMale, 0},
            {ItemIDs.unused622, 0},
            {ItemIDs.gram1, 0},
            {ItemIDs.gram2, 0},
            {ItemIDs.gram3, 0},
            {ItemIDs.xtransceiverFemale, 0},
            {ItemIDs.medalBox, 0},
            {ItemIDs.dNASplicersFuse, 0},
            {ItemIDs.dNASplicersSeparate, 0},
            {ItemIDs.permit, 0},
            {ItemIDs.ovalCharm, 0},
            {ItemIDs.shinyCharm, 0},
            {ItemIDs.plasmaCard, 0},
            {ItemIDs.grubbyHanky, 0},
            {ItemIDs.colressMachine, 0},
            {ItemIDs.droppedItemCurtis, 0},
            {ItemIDs.droppedItemYancy, 0},
            {ItemIDs.revealGlass, 0},
            {ItemIDs.weaknessPolicy, 200},
            {ItemIDs.assaultVest, 600},
            {ItemIDs.holoCasterMale, 0},
            {ItemIDs.profsLetter, 0},
            {ItemIDs.rollerSkates, 0},
            {ItemIDs.pixiePlate, 200},
            {ItemIDs.abilityCapsule, 500},
            {ItemIDs.whippedDream, 300},
            {ItemIDs.sachet, 300},
            {ItemIDs.luminousMoss, 20},
            {ItemIDs.snowball, 20},
            {ItemIDs.safetyGoggles, 300},
            {ItemIDs.pokeFlute, 0},
            {ItemIDs.richMulch, 20},
            {ItemIDs.surpriseMulch, 20},
            {ItemIDs.boostMulch, 20},
            {ItemIDs.amazeMulch, 20},
            {ItemIDs.gengarite, 1000},
            {ItemIDs.gardevoirite, 1000},
            {ItemIDs.ampharosite, 1000},
            {ItemIDs.venusaurite, 1000},
            {ItemIDs.charizarditeX, 1000},
            {ItemIDs.blastoisinite, 1000},
            {ItemIDs.mewtwoniteX, 2000},
            {ItemIDs.mewtwoniteY, 2000},
            {ItemIDs.blazikenite, 1000},
            {ItemIDs.medichamite, 500},
            {ItemIDs.houndoominite, 1000},
            {ItemIDs.aggronite, 1000},
            {ItemIDs.banettite, 500},
            {ItemIDs.tyranitarite, 2000},
            {ItemIDs.scizorite, 1000},
            {ItemIDs.pinsirite, 1000},
            {ItemIDs.aerodactylite, 1000},
            {ItemIDs.lucarionite, 1000},
            {ItemIDs.abomasite, 500},
            {ItemIDs.kangaskhanite, 500},
            {ItemIDs.gyaradosite, 1000},
            {ItemIDs.absolite, 500},
            {ItemIDs.charizarditeY, 1000},
            {ItemIDs.alakazite, 1000},
            {ItemIDs.heracronite, 1000},
            {ItemIDs.mawilite, 300},
            {ItemIDs.manectite, 500},
            {ItemIDs.garchompite, 2000},
            {ItemIDs.latiasite, 2000},
            {ItemIDs.latiosite, 2000},
            {ItemIDs.roseliBerry, 100},
            {ItemIDs.keeBerry, 100},
            {ItemIDs.marangaBerry, 100},
            {ItemIDs.sprinklotad, 0},
            {ItemIDs.tm96, 1000},
            {ItemIDs.tm97, 1000},
            {ItemIDs.tm98, 1000},
            {ItemIDs.tm99, 1000},
            {ItemIDs.tm100, 500},
            {ItemIDs.powerPlantPass, 0},
            {ItemIDs.megaRing, 0},
            {ItemIDs.intriguingStone, 0},
            {ItemIDs.commonStone, 0},
            {ItemIDs.discountCoupon, 2},
            {ItemIDs.elevatorKey, 0},
            {ItemIDs.tmvPass, 0},
            {ItemIDs.honorofKalos, 0},
            {ItemIDs.adventureGuide, 0},
            {ItemIDs.strangeSouvenir, 1},
            {ItemIDs.lensCase, 0},
            {ItemIDs.makeupBag, 0},
            {ItemIDs.travelTrunk, 0},
            {ItemIDs.lumioseGalette, 45},
            {ItemIDs.shalourSable, 45},
            {ItemIDs.jawFossil, 500},
            {ItemIDs.sailFossil, 500},
            {ItemIDs.lookerTicket, 0},
            {ItemIDs.bikeYellow, 0},
            {ItemIDs.holoCasterFemale, 0},
            {ItemIDs.fairyGem, 100},
            {ItemIDs.megaCharm, 0},
            {ItemIDs.megaGlove, 0},
            {ItemIDs.machBike, 0},
            {ItemIDs.acroBike, 0},
            {ItemIDs.wailmerPail, 0},
            {ItemIDs.devonParts, 0},
            {ItemIDs.sootSack, 0},
            {ItemIDs.basementKeyHoenn, 0},
            {ItemIDs.pokeblockKit, 0},
            {ItemIDs.letter, 0},
            {ItemIDs.eonTicket, 0},
            {ItemIDs.scanner, 0},
            {ItemIDs.goGoggles, 0},
            {ItemIDs.meteoriteFirstForm, 0},
            {ItemIDs.keytoRoom1, 0},
            {ItemIDs.keytoRoom2, 0},
            {ItemIDs.keytoRoom4, 0},
            {ItemIDs.keytoRoom6, 0},
            {ItemIDs.storageKeyHoenn, 0},
            {ItemIDs.devonScope, 0},
            {ItemIDs.ssTicketHoenn, 0},
            {ItemIDs.hm07ORAS, 0},
            {ItemIDs.devonScubaGear, 0},
            {ItemIDs.contestCostumeMale, 0},
            {ItemIDs.contestCostumeFemale, 0},
            {ItemIDs.magmaSuit, 0},
            {ItemIDs.aquaSuit, 0},
            {ItemIDs.pairOfTickets, 0},
            {ItemIDs.megaBracelet, 0},
            {ItemIDs.megaPendant, 0},
            {ItemIDs.megaGlasses, 0},
            {ItemIDs.megaAnchor, 0},
            {ItemIDs.megaStickpin, 0},
            {ItemIDs.megaTiara, 0},
            {ItemIDs.megaAnklet, 0},
            {ItemIDs.meteoriteSecondForm, 0},
            {ItemIDs.swampertite, 1000},
            {ItemIDs.sceptilite, 1000},
            {ItemIDs.sablenite, 300},
            {ItemIDs.altarianite, 500},
            {ItemIDs.galladite, 1000},
            {ItemIDs.audinite, 500},
            {ItemIDs.metagrossite, 2000},
            {ItemIDs.sharpedonite, 500},
            {ItemIDs.slowbronite, 500},
            {ItemIDs.steelixite, 1000},
            {ItemIDs.pidgeotite, 500},
            {ItemIDs.glalitite, 500},
            {ItemIDs.diancite, 2000},
            {ItemIDs.prisonBottle, 0},
            {ItemIDs.megaCuff, 0},
            {ItemIDs.cameruptite, 500},
            {ItemIDs.lopunnite, 500},
            {ItemIDs.salamencite, 2000},
            {ItemIDs.beedrillite, 300},
            {ItemIDs.meteoriteThirdForm, 0},
            {ItemIDs.meteoriteFinalForm, 0},
            {ItemIDs.keyStone, 0},
            {ItemIDs.meteoriteShard, 0},
            {ItemIDs.eonFlute, 0},
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
