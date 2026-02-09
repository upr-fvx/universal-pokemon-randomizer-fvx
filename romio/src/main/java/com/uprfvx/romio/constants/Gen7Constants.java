package com.uprfvx.romio.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen7Constants.java - Constants for Sun/Moon/Ultra Sun/Ultra Moon      --*/
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

import com.uprfvx.romio.gamedata.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gen7Constants {

    public static final int Type_SM = N3DSConstants.Type_SM;
    public static final int Type_USUM = N3DSConstants.Type_USUM;

    private static final int pokemonCountSM = 802, pokemonCountUSUM = 807;
    private static final int formeCountSM = 158, formeCountUSUM = 168;
    private static final int moveCountSM = 719, moveCountUSUM = 728;
    private static final int highestAbilityIndexSM = AbilityIDs.prismArmor, highestAbilityIndexUSUM = AbilityIDs.neuroforce;

    public static final int bsHPOffset = 0, bsAttackOffset = 1, bsDefenseOffset = 2, bsSpeedOffset = 3,
            bsSpAtkOffset = 4, bsSpDefOffset = 5, bsPrimaryTypeOffset = 6, bsSecondaryTypeOffset = 7,
            bsCatchRateOffset = 8, bsCommonHeldItemOffset = 12, bsRareHeldItemOffset = 14, bsDarkGrassHeldItemOffset = 16,
            bsGenderOffset = 18, bsGrowthCurveOffset = 21, bsAbility1Offset = 24, bsAbility2Offset = 25,
            bsAbility3Offset = 26, bsCallRateOffset = 27, bsFormeOffset = 28, bsFormeSpriteOffset = 30,
            bsFormeCountOffset = 32, bsTMHMCompatOffset = 40, bsSpecialMTCompatOffset = 56, bsMTCompatOffset = 60;

    public static final int bsSize = 0x54;

    public static final int evolutionMethodCount = 42;

    private static final List<Integer> speciesWithAlolanForms = Arrays.asList(
            SpeciesIDs.rattata, SpeciesIDs.raticate, SpeciesIDs.raichu, SpeciesIDs.sandshrew, SpeciesIDs.sandslash, SpeciesIDs.vulpix,
            SpeciesIDs.ninetales, SpeciesIDs.diglett, SpeciesIDs.dugtrio, SpeciesIDs.meowth, SpeciesIDs.persian, SpeciesIDs.geodude,
            SpeciesIDs.graveler, SpeciesIDs.golem, SpeciesIDs.grimer, SpeciesIDs.muk, SpeciesIDs.exeggutor, SpeciesIDs.marowak
    );

    private static final List<Integer> speciesWithTotemForms = Arrays.asList(
            // Raticate-Alolan and Marowak-Alolan being here depends on the USUM formes that share their SM species IDs,
            // (Oricorio-Pom-Pom and Geodude-Alolan) not having cosmetic alt formes of their own.
            // Wishiwashi is also excluded, since it seems to simply not have a totem forme.
            SpeciesIDs.gumshoos, SpeciesIDs.vikavolt, SpeciesIDs.lurantis, SpeciesIDs.salazzle, SpeciesIDs.kommoO,
            SpeciesIDs.araquanid, SpeciesIDs.togedemaru, SpeciesIDs.ribombee, SpeciesIDs.SMFormes.raticateA,
            SpeciesIDs.USUMFormes.raticateA, SpeciesIDs.SMFormes.marowakA, SpeciesIDs.USUMFormes.marowakA
    );

    private static final Map<Integer,String> dummyFormeSuffixes = setupDummyFormeSuffixes();
    private static final Map<Integer,Map<Integer,String>> formeSuffixesByBaseForme = setupFormeSuffixesByBaseForme();

    public static String getFormeSuffixByBaseForme(int baseForme, int formNum) {
        return formeSuffixesByBaseForme.getOrDefault(baseForme,dummyFormeSuffixes).getOrDefault(formNum,"");
    }

    public static List<Integer> getIrregularFormes(int romType) {
        if (romType == Type_SM) {
            return irregularFormesSM;
        } else if (romType == Type_USUM) {
            return irregularFormesUSUM;
        }
        return irregularFormesSM;
    }

    public static final List<Integer> irregularFormesSM = Arrays.asList(
            SpeciesIDs.SMFormes.castformSu, SpeciesIDs.SMFormes.castformR, SpeciesIDs.SMFormes.castformSn,
            SpeciesIDs.SMFormes.darmanitanZ,
            SpeciesIDs.SMFormes.meloettaP,
            SpeciesIDs.SMFormes.kyuremW,
            SpeciesIDs.SMFormes.kyuremB,
            SpeciesIDs.SMFormes.gengarMega,
            SpeciesIDs.SMFormes.gardevoirMega,
            SpeciesIDs.SMFormes.ampharosMega,
            SpeciesIDs.SMFormes.venusaurMega,
            SpeciesIDs.SMFormes.charizardMegaX, SpeciesIDs.SMFormes.charizardMegaY,
            SpeciesIDs.SMFormes.mewtwoMegaX, SpeciesIDs.SMFormes.mewtwoMegaY,
            SpeciesIDs.SMFormes.blazikenMega,
            SpeciesIDs.SMFormes.medichamMega,
            SpeciesIDs.SMFormes.houndoomMega,
            SpeciesIDs.SMFormes.aggronMega,
            SpeciesIDs.SMFormes.banetteMega,
            SpeciesIDs.SMFormes.tyranitarMega,
            SpeciesIDs.SMFormes.scizorMega,
            SpeciesIDs.SMFormes.pinsirMega,
            SpeciesIDs.SMFormes.aerodactylMega,
            SpeciesIDs.SMFormes.lucarioMega,
            SpeciesIDs.SMFormes.abomasnowMega,
            SpeciesIDs.SMFormes.aegislashB,
            SpeciesIDs.SMFormes.blastoiseMega,
            SpeciesIDs.SMFormes.kangaskhanMega,
            SpeciesIDs.SMFormes.gyaradosMega,
            SpeciesIDs.SMFormes.absolMega,
            SpeciesIDs.SMFormes.alakazamMega,
            SpeciesIDs.SMFormes.heracrossMega,
            SpeciesIDs.SMFormes.mawileMega,
            SpeciesIDs.SMFormes.manectricMega,
            SpeciesIDs.SMFormes.garchompMega,
            SpeciesIDs.SMFormes.latiosMega,
            SpeciesIDs.SMFormes.latiasMega,
            SpeciesIDs.SMFormes.swampertMega,
            SpeciesIDs.SMFormes.sceptileMega,
            SpeciesIDs.SMFormes.sableyeMega,
            SpeciesIDs.SMFormes.altariaMega,
            SpeciesIDs.SMFormes.galladeMega,
            SpeciesIDs.SMFormes.audinoMega,
            SpeciesIDs.SMFormes.sharpedoMega,
            SpeciesIDs.SMFormes.slowbroMega,
            SpeciesIDs.SMFormes.steelixMega,
            SpeciesIDs.SMFormes.pidgeotMega,
            SpeciesIDs.SMFormes.glalieMega,
            SpeciesIDs.SMFormes.diancieMega,
            SpeciesIDs.SMFormes.metagrossMega,
            SpeciesIDs.SMFormes.kyogreP,
            SpeciesIDs.SMFormes.groudonP,
            SpeciesIDs.SMFormes.rayquazaMega,
            SpeciesIDs.SMFormes.cameruptMega,
            SpeciesIDs.SMFormes.lopunnyMega,
            SpeciesIDs.SMFormes.salamenceMega,
            SpeciesIDs.SMFormes.beedrillMega,
            SpeciesIDs.SMFormes.wishiwashiS,
            SpeciesIDs.SMFormes.greninjaA,
            SpeciesIDs.SMFormes.zygardeC,
            SpeciesIDs.SMFormes.miniorC
    );

    public static final List<Integer> irregularFormesUSUM = Arrays.asList(
            SpeciesIDs.USUMFormes.castformSu, SpeciesIDs.USUMFormes.castformR, SpeciesIDs.USUMFormes.castformSn,
            SpeciesIDs.USUMFormes.darmanitanZ,
            SpeciesIDs.USUMFormes.meloettaP,
            SpeciesIDs.USUMFormes.kyuremW,
            SpeciesIDs.USUMFormes.kyuremB,
            SpeciesIDs.USUMFormes.gengarMega,
            SpeciesIDs.USUMFormes.gardevoirMega,
            SpeciesIDs.USUMFormes.ampharosMega,
            SpeciesIDs.USUMFormes.venusaurMega,
            SpeciesIDs.USUMFormes.charizardMegaX, SpeciesIDs.USUMFormes.charizardMegaY,
            SpeciesIDs.USUMFormes.mewtwoMegaX, SpeciesIDs.USUMFormes.mewtwoMegaY,
            SpeciesIDs.USUMFormes.blazikenMega,
            SpeciesIDs.USUMFormes.medichamMega,
            SpeciesIDs.USUMFormes.houndoomMega,
            SpeciesIDs.USUMFormes.aggronMega,
            SpeciesIDs.USUMFormes.banetteMega,
            SpeciesIDs.USUMFormes.tyranitarMega,
            SpeciesIDs.USUMFormes.scizorMega,
            SpeciesIDs.USUMFormes.pinsirMega,
            SpeciesIDs.USUMFormes.aerodactylMega,
            SpeciesIDs.USUMFormes.lucarioMega,
            SpeciesIDs.USUMFormes.abomasnowMega,
            SpeciesIDs.USUMFormes.aegislashB,
            SpeciesIDs.USUMFormes.blastoiseMega,
            SpeciesIDs.USUMFormes.kangaskhanMega,
            SpeciesIDs.USUMFormes.gyaradosMega,
            SpeciesIDs.USUMFormes.absolMega,
            SpeciesIDs.USUMFormes.alakazamMega,
            SpeciesIDs.USUMFormes.heracrossMega,
            SpeciesIDs.USUMFormes.mawileMega,
            SpeciesIDs.USUMFormes.manectricMega,
            SpeciesIDs.USUMFormes.garchompMega,
            SpeciesIDs.USUMFormes.latiosMega,
            SpeciesIDs.USUMFormes.latiasMega,
            SpeciesIDs.USUMFormes.swampertMega,
            SpeciesIDs.USUMFormes.sceptileMega,
            SpeciesIDs.USUMFormes.sableyeMega,
            SpeciesIDs.USUMFormes.altariaMega,
            SpeciesIDs.USUMFormes.galladeMega,
            SpeciesIDs.USUMFormes.audinoMega,
            SpeciesIDs.USUMFormes.sharpedoMega,
            SpeciesIDs.USUMFormes.slowbroMega,
            SpeciesIDs.USUMFormes.steelixMega,
            SpeciesIDs.USUMFormes.pidgeotMega,
            SpeciesIDs.USUMFormes.glalieMega,
            SpeciesIDs.USUMFormes.diancieMega,
            SpeciesIDs.USUMFormes.metagrossMega,
            SpeciesIDs.USUMFormes.kyogreP,
            SpeciesIDs.USUMFormes.groudonP,
            SpeciesIDs.USUMFormes.rayquazaMega,
            SpeciesIDs.USUMFormes.cameruptMega,
            SpeciesIDs.USUMFormes.lopunnyMega,
            SpeciesIDs.USUMFormes.salamenceMega,
            SpeciesIDs.USUMFormes.beedrillMega,
            SpeciesIDs.USUMFormes.wishiwashiS,
            SpeciesIDs.USUMFormes.greninjaA,
            SpeciesIDs.USUMFormes.zygardeC,
            SpeciesIDs.USUMFormes.miniorC,
            SpeciesIDs.USUMFormes.necrozmaDM,
            SpeciesIDs.USUMFormes.necrozmaDW,
            SpeciesIDs.USUMFormes.necrozmaU
    );

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

    public static final int route1EncAreaIndex = 0, route1PikipekEncIndex = 8, route1PikipekStaticIndex = 0;

    public static final int noDamageTargetTrappingEffect = 106, noDamageFieldTrappingEffect = 354,
            damageAdjacentFoesTrappingEffect = 373, damageTargetTrappingEffect = 384;

    public static final int noDamageStatusQuality = 1, noDamageStatChangeQuality = 2, damageStatusQuality = 4,
            noDamageStatusAndStatChangeQuality = 5, damageTargetDebuffQuality = 6, damageUserBuffQuality = 7,
            damageAbsorbQuality = 8;

    public static List<Integer> bannedMoves = Arrays.asList(MoveIDs.darkVoid, MoveIDs.hyperspaceFury);

    public static final Type[] typeTable = constructTypeTable();

    private static final String tmDataPrefixSM = "034003410342034303",  tmDataPrefixUSUM = "03BC03BD03BE03BF03";
    public static final int tmCount = 100, tmBlockOneCount = 92, tmBlockTwoCount = 3, tmBlockThreeCount = 5,
            tmBlockOneOffset = ItemIDs.tm01, tmBlockTwoOffset = ItemIDs.tm93, tmBlockThreeOffset = ItemIDs.tm96;
    public static final String itemPalettesPrefix = "070000000000000000010100";

    public static final int shopItemsOffsetSM = 0x50A8;
    public static final int shopItemsOffsetUSUM = 0x50BC;

    public static final int tutorsOffset = 0x54DE;
    public static final String tutorsPrefix = "5F6F6E5F6F6666FF";
    public static final int tutorMoveCount = 67;

    public static final String[] fastestTextPrefixes = new String[]{"1080BDE80E000500F0412DE9", "34019FE50060A0E3"};

    private static final List<Integer> mainGameShopsSM = Arrays.asList(
            8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 23
    );

    private static final List<Integer> mainGameShopsUSUM = Arrays.asList(
            8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 23, 24, 25, 26, 27
    );

    public static final List<Integer> evolutionItems = Arrays.asList(ItemIDs.sunStone, ItemIDs.moonStone, ItemIDs.fireStone,
            ItemIDs.thunderStone, ItemIDs.waterStone, ItemIDs.leafStone, ItemIDs.shinyStone, ItemIDs.duskStone, ItemIDs.dawnStone,
            ItemIDs.ovalStone, ItemIDs.kingsRock, ItemIDs.deepSeaTooth, ItemIDs.deepSeaScale, ItemIDs.metalCoat, ItemIDs.dragonScale,
            ItemIDs.upgrade, ItemIDs.protector, ItemIDs.electirizer, ItemIDs.magmarizer, ItemIDs.dubiousDisc, ItemIDs.reaperCloth,
            ItemIDs.razorClaw, ItemIDs.razorFang, ItemIDs.prismScale, ItemIDs.whippedDream, ItemIDs.sachet, ItemIDs.iceStone);

    private static final List<Boolean> relevantEncounterFilesSM = setupRelevantEncounterFiles(Type_SM);
    private static final List<Boolean> relevantEncounterFilesUSUM = setupRelevantEncounterFiles(Type_USUM);

    public static final Map<Type, Integer> heldZCrystalsByType = initHeldZCrystalsByType();

    private static Map<Type, Integer> initHeldZCrystalsByType() {
        Map<Type, Integer> map = new EnumMap<>(Type.class);
        map.put(Type.NORMAL, ItemIDs.normaliumZHeld);
        map.put(Type.FIGHTING, ItemIDs.fightiniumZHeld);
        map.put(Type.FLYING, ItemIDs.flyiniumZHeld);
        map.put(Type.POISON, ItemIDs.poisoniumZHeld);
        map.put(Type.GROUND, ItemIDs.groundiumZHeld);
        map.put(Type.ROCK, ItemIDs.rockiumZHeld);
        map.put(Type.BUG, ItemIDs.buginiumZHeld);
        map.put(Type.GHOST, ItemIDs.ghostiumZHeld);
        map.put(Type.STEEL, ItemIDs.steeliumZHeld);
        map.put(Type.FIRE, ItemIDs.firiumZHeld);
        map.put(Type.WATER, ItemIDs.wateriumZHeld);
        map.put(Type.GRASS, ItemIDs.grassiumZHeld);
        map.put(Type.ELECTRIC, ItemIDs.electriumZHeld);
        map.put(Type.PSYCHIC, ItemIDs.psychiumZHeld);
        map.put(Type.ICE, ItemIDs.iciumZHeld);
        map.put(Type.DRAGON, ItemIDs.dragoniumZHeld);
        map.put(Type.DARK, ItemIDs.darkiniumZHeld);
        map.put(Type.FAIRY, ItemIDs.fairiumZHeld);
        return Collections.unmodifiableMap(map);
    }


//    Arrays.asList(
//            Items.normaliumZHeld, // Normal
//            Items.fightiniumZHeld, // Fighting
//            Items.flyiniumZHeld, // Flying
//            Items.poisoniumZHeld, // Poison
//            Items.groundiumZHeld, // Ground
//            Items.rockiumZHeld, // Rock
//            Items.buginiumZHeld, // Bug
//            Items.ghostiumZHeld, // Ghost
//            Items.steeliumZHeld, // Steel
//            Items.firiumZHeld, // Fire
//            Items.wateriumZHeld, // Water
//            Items.grassiumZHeld, // Grass
//            Items.electriumZHeld, // Electric
//            Items.psychiumZHeld, // Psychic
//            Items.iciumZHeld, // Ice
//            Items.dragoniumZHeld, // Dragon
//            Items.darkiniumZHeld, // Dark
//            Items.fairiumZHeld  // Fairy
//    );

    public static final Map<Integer,List<Integer>> abilityVariations = setupAbilityVariations();

    private static Map<Integer,List<Integer>> setupAbilityVariations() {
        Map<Integer,List<Integer>> map = new HashMap<>();
        map.put(AbilityIDs.insomnia, Arrays.asList(AbilityIDs.insomnia, AbilityIDs.vitalSpirit));
        map.put(AbilityIDs.clearBody, Arrays.asList(AbilityIDs.clearBody, AbilityIDs.whiteSmoke, AbilityIDs.fullMetalBody));
        map.put(AbilityIDs.hugePower, Arrays.asList(AbilityIDs.hugePower, AbilityIDs.purePower));
        map.put(AbilityIDs.battleArmor, Arrays.asList(AbilityIDs.battleArmor, AbilityIDs.shellArmor));
        map.put(AbilityIDs.cloudNine, Arrays.asList(AbilityIDs.cloudNine, AbilityIDs.airLock));
        map.put(AbilityIDs.filter, Arrays.asList(AbilityIDs.filter, AbilityIDs.solidRock, AbilityIDs.prismArmor));
        map.put(AbilityIDs.roughSkin, Arrays.asList(AbilityIDs.roughSkin, AbilityIDs.ironBarbs));
        map.put(AbilityIDs.moldBreaker, Arrays.asList(AbilityIDs.moldBreaker, AbilityIDs.turboblaze, AbilityIDs.teravolt));
        map.put(AbilityIDs.wimpOut, Arrays.asList(AbilityIDs.wimpOut, AbilityIDs.emergencyExit));
        map.put(AbilityIDs.queenlyMajesty, Arrays.asList(AbilityIDs.queenlyMajesty, AbilityIDs.dazzling));
        map.put(AbilityIDs.gooey, Arrays.asList(AbilityIDs.gooey, AbilityIDs.tanglingHair));
        map.put(AbilityIDs.receiver, Arrays.asList(AbilityIDs.receiver, AbilityIDs.powerOfAlchemy));
        map.put(AbilityIDs.multiscale, Arrays.asList(AbilityIDs.multiscale, AbilityIDs.shadowShield));

        return map;
    }

    public static final List<Integer> uselessAbilities = Arrays.asList(AbilityIDs.forecast, AbilityIDs.multitype,
            AbilityIDs.flowerGift, AbilityIDs.zenMode, AbilityIDs.stanceChange, AbilityIDs.shieldsDown, AbilityIDs.schooling,
            AbilityIDs.disguise, AbilityIDs.battleBond, AbilityIDs.powerConstruct, AbilityIDs.rksSystem);

    private static final String saveLoadFormeReversionPrefixSM = "00EB040094E50C1094E5F70E80E2", saveLoadFormeReversionPrefixUSUM = "00EB040094E50C1094E5030B80E2EE0F80E2";
    public static final String afterBattleFormeReversionPrefix = "0055E10B00001A0010A0E30700A0E1";

    public static final String ninjaskSpeciesPrefix = "11FF2FE11CD08DE2F080BDE8", shedinjaPrefix = "A0E194FDFFEB0040A0E1";

    public static final String beastLusaminePokemonBoostsPrefix = "1D14FFFF";
    public static final int beastLusamineTrainerIndex = 157;

    public static final String miniorWildEncounterPatchPrefix = "032C42E2062052E2";

    public static final int zygardeAssemblyScriptFile = 45;
    public static final String zygardeAssemblyFormePrefix = "BC21CDE1B801CDE1", zygardeAssemblySpeciesPrefix = "FBEB4CD08DE20400A0E1F08FBDE8";

    public static final String friendshipValueForEvoLocator = "DC0050E3F700002A";

    public static final String perfectOddsBranchLocator = "050000BA000050E3";

    public static int getPokemonCount(int romType) {
        if (romType == Type_SM) {
            return pokemonCountSM;
        } else if (romType == Type_USUM) {
            return pokemonCountUSUM;
        }
        return pokemonCountSM;
    }

    public static final List<Integer> consumableHeldItems = setupAllConsumableItems();

    private static List<Integer> setupAllConsumableItems() {
        List<Integer> list = new ArrayList<>(Gen6Constants.consumableHeldItems);
        list.addAll(Arrays.asList(ItemIDs.adrenalineOrb, ItemIDs.electricSeed, ItemIDs.psychicSeed, ItemIDs.mistySeed, ItemIDs.grassySeed));
        return list;
    }

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        // We intentionally do not include Z Crystals in this list. Adding Z-Crystals to random trainers should
        // probably require its own setting if desired.
        List<Integer> list = new ArrayList<>(Gen6Constants.allHeldItems);
        list.addAll(Arrays.asList(ItemIDs.adrenalineOrb, ItemIDs.electricSeed, ItemIDs.psychicSeed, ItemIDs.mistySeed, ItemIDs.grassySeed));
        list.addAll(Arrays.asList(ItemIDs.terrainExtender, ItemIDs.protectivePads));
        return list;
    }

    public static final List<Integer> generalPurposeConsumableItems = initializeGeneralPurposeConsumableItems();

    private static List<Integer> initializeGeneralPurposeConsumableItems() {
        List<Integer> list = new ArrayList<>(Gen6Constants.generalPurposeConsumableItems);
        // These berries are worth the risk of causing confusion because they heal for half max HP.
        list.addAll(Arrays.asList(ItemIDs.figyBerry, ItemIDs.wikiBerry, ItemIDs.magoBerry,
                ItemIDs.aguavBerry, ItemIDs.iapapaBerry, ItemIDs.adrenalineOrb));
        return Collections.unmodifiableList(list);
    }

    public static final List<Integer> generalPurposeItems = initializeGeneralPurposeItems();

    private static List<Integer> initializeGeneralPurposeItems() {
        List<Integer> list = new ArrayList<>(Gen6Constants.generalPurposeItems);
        list.add(ItemIDs.protectivePads);
        return Collections.unmodifiableList(list);
    }

    public static final Map<Integer, List<Integer>> moveBoostingItems = initializeMoveBoostingItems();

    private static Map<Integer, List<Integer>> initializeMoveBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>(Gen6Constants.moveBoostingItems);
        map.put(MoveIDs.electricTerrain, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(MoveIDs.grassyTerrain, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(MoveIDs.mistyTerrain, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(MoveIDs.psychicTerrain, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(MoveIDs.strengthSap, Collections.singletonList(ItemIDs.bigRoot));
        return Collections.unmodifiableMap(map);
    }
    public static final Map<Integer, List<Integer>> abilityBoostingItems = initializeAbilityBoostingItems();

    private static Map<Integer, List<Integer>> initializeAbilityBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>(Gen6Constants.abilityBoostingItems);
        map.put(AbilityIDs.electricSurge, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(AbilityIDs.grassySurge, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(AbilityIDs.mistySurge, Collections.singletonList(ItemIDs.terrainExtender));
        map.put(AbilityIDs.psychicSurge, Collections.singletonList(ItemIDs.terrainExtender));
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, Integer> consumableAbilityBoostingItems = initializeConsumableAbilityBoostingItems();

    private static Map<Integer, Integer> initializeConsumableAbilityBoostingItems() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(AbilityIDs.electricSurge, ItemIDs.electricSeed);
        map.put(AbilityIDs.grassySurge, ItemIDs.grassySeed);
        map.put(AbilityIDs.mistySurge, ItemIDs.mistySeed);
        map.put(AbilityIDs.psychicSurge, ItemIDs.psychicSeed);
        return Collections.unmodifiableMap(map);
    }

    // None of these have new entries in Gen VII.
    public static final Map<Type, Integer> consumableTypeBoostingItems = Gen6Constants.consumableTypeBoostingItems;
    public static final Map<Integer, List<Integer>> speciesBoostingItems = Gen6Constants.speciesBoostingItems;
    public static final Map<Type, List<Integer>> typeBoostingItems = Gen6Constants.typeBoostingItems;
    public static final Map<Type, Integer> weaknessReducingBerries = Gen6Constants.weaknessReducingBerries;

    public static List<String> getShopNames(int romType) {
        List<String> shopNames = new ArrayList<>();
        shopNames.add("Primary 0 Trials");
        shopNames.add("Primary 1 Trials");
        shopNames.add("Primary 2 Trials");
        shopNames.add("Primary 3 Trials");
        shopNames.add("Primary 4 Trials");
        shopNames.add("Primary 5 Trials");
        shopNames.add("Primary 6 Trials");
        shopNames.add("Primary 7 Trials");
        shopNames.add("Konikoni City Incenses");
        shopNames.add("Konikoni City Herbs");
        shopNames.add("Hau'oli City Secondary");
        shopNames.add("Route 2 Secondary");
        shopNames.add("Heahea City Secondary (TMs)");
        shopNames.add("Royal Avenue Secondary (TMs)");
        shopNames.add("Route 8 Secondary");
        shopNames.add("Paniola Town Secondary");
        shopNames.add("Malie City Secondary (TMs)");
        shopNames.add("Mount Hokulani Secondary");
        shopNames.add("Seafolk Village Secondary (TMs)");
        shopNames.add("Konikoni City TMs");
        shopNames.add("Konikoni City Stones");
        shopNames.add("Thrifty Megamart, Center-Left");
        shopNames.add("Thrifty Megamart, Center-Right");
        shopNames.add("Thrifty Megamart, Right");
        if (romType == Type_USUM) {
            shopNames.add("Route 5 Secondary");
            shopNames.add("Konikoni City Secondary");
            shopNames.add("Tapu Village Secondary");
            shopNames.add("Mount Lanakila Secondary");
        }
        return shopNames;
    }

    public static List<Integer> getMainGameShops(int romType) {
        if (romType == Type_SM) {
            return mainGameShopsSM;
        } else {
            return mainGameShopsUSUM;
        }
    }

    public static int getShopItemsOffset(int romType) {
        if (romType == Type_SM) {
            return shopItemsOffsetSM;
        } else {
            return shopItemsOffsetUSUM;
        }
    }

    public static int getFormeCount(int romType) {
        if (romType == Type_SM) {
            return formeCountSM;
        } else {
            return formeCountUSUM;
        }
    }

    public static int getMoveCount(int romType) {
        if (romType == Type_SM) {
            return moveCountSM;
        } else if (romType == Type_USUM) {
            return moveCountUSUM;
        }
        return moveCountSM;
    }

    public static String getTmDataPrefix(int romType) {
        if (romType == Type_SM) {
            return tmDataPrefixSM;
        } else if (romType == Type_USUM) {
            return tmDataPrefixUSUM;
        }
        return tmDataPrefixSM;
    }

    public static int getHighestAbilityIndex(int romType) {
        if (romType == Type_SM) {
            return highestAbilityIndexSM;
        } else if (romType == Type_USUM) {
            return highestAbilityIndexUSUM;
        }
        return highestAbilityIndexSM;
    }

    public static List<Boolean> getRelevantEncounterFiles(int romType) {
        if (romType == Type_SM) {
            return relevantEncounterFilesSM;
        } else {
            return relevantEncounterFilesUSUM;
        }
    }

    public static String getSaveLoadFormeReversionPrefix(int romType) {
        if (romType == Type_SM) {
            return saveLoadFormeReversionPrefixSM;
        } else {
            return saveLoadFormeReversionPrefixUSUM;
        }
    }

    private static Map<Integer,Map<Integer,String>> setupFormeSuffixesByBaseForme() {
        Map<Integer,Map<Integer,String>> map = new HashMap<>();

        putFormSuffixes(map, SpeciesIDs.pikachu,
                // the last one is Partner Cap because it is only in USUM, but unsure which is which of the others
                "-InACap", "-InACap", "-InACap", "-InACap", "-InACap", "-InACap", "-PartnerCap");

        putFormSuffixes(map, SpeciesIDs.castform, "-Sunny", "-Rainy", "-Snowy");
        putFormSuffixes(map, SpeciesIDs.kyogre, "-Primal");
        putFormSuffixes(map, SpeciesIDs.groudon, "-Primal");
        putFormSuffixes(map, SpeciesIDs.deoxys, "-Attack", "-Defense", "-Speed");

        putFormSuffixes(map, SpeciesIDs.wormadam, "-Sandy", "-Trash");
        putFormSuffixes(map, SpeciesIDs.shaymin, "-Sky");
        putFormSuffixes(map, SpeciesIDs.giratina, "-Origin");
        putFormSuffixes(map, SpeciesIDs.rotom, "-Heat", "-Wash", "-Frost", "-Fan", "-Mow");

        putFormSuffixes(map, SpeciesIDs.basculin, "-Blue");
        putFormSuffixes(map, SpeciesIDs.darmanitan, "-Zen");
        putFormSuffixes(map, SpeciesIDs.tornadus, "-Therian");
        putFormSuffixes(map, SpeciesIDs.thundurus, "-Therian");
        putFormSuffixes(map, SpeciesIDs.landorus, "-Therian");
        putFormSuffixes(map, SpeciesIDs.kyurem, "-White", "-Black");
        putFormSuffixes(map, SpeciesIDs.keldeo, "-Resolute");
        putFormSuffixes(map, SpeciesIDs.meloetta, "-Pirouette");

        putFormSuffixes(map, SpeciesIDs.greninja, "-BattleBond", "-Ash");
        putFormSuffixes(map, SpeciesIDs.meowstic, "-F");
        putFormSuffixes(map, SpeciesIDs.aegislash, "-Blade");
        putFormSuffixes(map, SpeciesIDs.pumpkaboo, "-M", "-L", "-XL");
        putFormSuffixes(map, SpeciesIDs.gourgeist, "-M", "-L", "-XL");
        putFormSuffixes(map, SpeciesIDs.floette, "", "", "", "", "-Eternal"); // first 4 are just colors
        putFormSuffixes(map, SpeciesIDs.zygarde,
                // using '%' causes issues with Java's string formatting, so avoid it/use 'p' instead
                "-10p", "-10p-PowerConstruct", "-PowerConstruct", "-Complete");
        putFormSuffixes(map, SpeciesIDs.hoopa, "-Unbound");

        putFormSuffixes(map, SpeciesIDs.oricorio, "-Pom-Pom", "-Pa'u", "-Sensu");
        putFormSuffixes(map, SpeciesIDs.rockruff, "-OwnTempo");
        putFormSuffixes(map, SpeciesIDs.lycanroc, "-Midnight", "-Dusk");
        putFormSuffixes(map, SpeciesIDs.wishiwashi, "-School");
        putFormSuffixes(map, SpeciesIDs.minior, "", "", "", "", "", "", "-Core"); // first 6 are just colors
        putFormSuffixes(map, SpeciesIDs.necrozma, "-DuskMane", "-DawnWings", "-Ultra");
        putFormSuffixes(map, SpeciesIDs.magearna, "-OGColors");

        for (Integer speciesID : Gen6Constants.speciesWithMegaEvos) {
            if (speciesID == SpeciesIDs.charizard || speciesID == SpeciesIDs.mewtwo) {
                putFormSuffixes(map, speciesID, "-Mega-X", "-Mega-Y");
            } else {
                putFormSuffixes(map, speciesID, "-Mega");
            }
        }

        for (int species : speciesWithAlolanForms) {
            putFormSuffixes(map, species, "-Alolan");
        }

        for (int species : speciesWithTotemForms) {
            putFormSuffixes(map, species, "-Totem");
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

    private static final List<Integer> actuallyCosmeticFormsSM = Arrays.asList(
            SpeciesIDs.SMFormes.cherrimCosmetic1,
            SpeciesIDs.SMFormes.shellosCosmetic1,
            SpeciesIDs.SMFormes.gastrodonCosmetic1,
            SpeciesIDs.SMFormes.keldeoCosmetic1,
            SpeciesIDs.SMFormes.furfrouCosmetic1, SpeciesIDs.SMFormes.furfrouCosmetic2,
            SpeciesIDs.SMFormes.furfrouCosmetic3, SpeciesIDs.SMFormes.furfrouCosmetic4,
            SpeciesIDs.SMFormes.furfrouCosmetic5, SpeciesIDs.SMFormes.furfrouCosmetic6,
            SpeciesIDs.SMFormes.furfrouCosmetic7, SpeciesIDs.SMFormes.furfrouCosmetic8,
            SpeciesIDs.SMFormes.furfrouCosmetic9,
            SpeciesIDs.SMFormes.pumpkabooCosmetic1, SpeciesIDs.SMFormes.pumpkabooCosmetic2,
            SpeciesIDs.SMFormes.pumpkabooCosmetic3,
            SpeciesIDs.SMFormes.gourgeistCosmetic1, SpeciesIDs.SMFormes.gourgeistCosmetic2,
            SpeciesIDs.SMFormes.gourgeistCosmetic3,
            SpeciesIDs.SMFormes.floetteCosmetic1, SpeciesIDs.SMFormes.floetteCosmetic2,
            SpeciesIDs.SMFormes.floetteCosmetic3, SpeciesIDs.SMFormes.floetteCosmetic4,
            SpeciesIDs.SMFormes.raticateACosmetic1,
            SpeciesIDs.SMFormes.mimikyuCosmetic1, SpeciesIDs.SMFormes.mimikyuCosmetic2, SpeciesIDs.SMFormes.mimikyuCosmetic3,
            SpeciesIDs.SMFormes.gumshoosCosmetic1,
            SpeciesIDs.SMFormes.vikavoltCosmetic1,
            SpeciesIDs.SMFormes.lurantisCosmetic1,
            SpeciesIDs.SMFormes.salazzleCosmetic1,
            SpeciesIDs.SMFormes.kommoOCosmetic1,
            SpeciesIDs.SMFormes.greninjaCosmetic1,
            SpeciesIDs.SMFormes.zygarde10Cosmetic1, SpeciesIDs.SMFormes.zygardeCosmetic1,
            SpeciesIDs.SMFormes.miniorCosmetic1, SpeciesIDs.SMFormes.miniorCosmetic2, SpeciesIDs.SMFormes.miniorCosmetic3,
            SpeciesIDs.SMFormes.miniorCosmetic4, SpeciesIDs.SMFormes.miniorCosmetic5, SpeciesIDs.SMFormes.miniorCosmetic6,
            SpeciesIDs.SMFormes.miniorCCosmetic1, SpeciesIDs.SMFormes.miniorCCosmetic2, SpeciesIDs.SMFormes.miniorCCosmetic3,
            SpeciesIDs.SMFormes.miniorCCosmetic4, SpeciesIDs.SMFormes.miniorCCosmetic5, SpeciesIDs.SMFormes.miniorCCosmetic6,
            SpeciesIDs.SMFormes.magearnaCosmetic1,
            SpeciesIDs.SMFormes.pikachuCosmetic1, SpeciesIDs.SMFormes.pikachuCosmetic2, SpeciesIDs.SMFormes.pikachuCosmetic3,
            SpeciesIDs.SMFormes.pikachuCosmetic4, SpeciesIDs.SMFormes.pikachuCosmetic5, SpeciesIDs.SMFormes.pikachuCosmetic6 // Pikachu With Funny Hats
    );

    private static final List<Integer> actuallyCosmeticFormsUSUM = Arrays.asList(
            SpeciesIDs.USUMFormes.cherrimCosmetic1,
            SpeciesIDs.USUMFormes.shellosCosmetic1,
            SpeciesIDs.USUMFormes.gastrodonCosmetic1,
            SpeciesIDs.USUMFormes.keldeoCosmetic1,
            SpeciesIDs.USUMFormes.furfrouCosmetic1, SpeciesIDs.USUMFormes.furfrouCosmetic2,
            SpeciesIDs.USUMFormes.furfrouCosmetic3, SpeciesIDs.USUMFormes.furfrouCosmetic4,
            SpeciesIDs.USUMFormes.furfrouCosmetic5, SpeciesIDs.USUMFormes.furfrouCosmetic6,
            SpeciesIDs.USUMFormes.furfrouCosmetic7, SpeciesIDs.USUMFormes.furfrouCosmetic8,
            SpeciesIDs.USUMFormes.furfrouCosmetic9,
            SpeciesIDs.USUMFormes.pumpkabooCosmetic1, SpeciesIDs.USUMFormes.pumpkabooCosmetic2,
            SpeciesIDs.USUMFormes.pumpkabooCosmetic3,
            SpeciesIDs.USUMFormes.gourgeistCosmetic1, SpeciesIDs.USUMFormes.gourgeistCosmetic2,
            SpeciesIDs.USUMFormes.gourgeistCosmetic3,
            SpeciesIDs.USUMFormes.floetteCosmetic1, SpeciesIDs.USUMFormes.floetteCosmetic2,
            SpeciesIDs.USUMFormes.floetteCosmetic3, SpeciesIDs.USUMFormes.floetteCosmetic4,
            SpeciesIDs.USUMFormes.raticateACosmetic1,
            SpeciesIDs.USUMFormes.marowakACosmetic1,
            SpeciesIDs.USUMFormes.mimikyuCosmetic1, SpeciesIDs.USUMFormes.mimikyuCosmetic2, SpeciesIDs.USUMFormes.mimikyuCosmetic3,
            SpeciesIDs.USUMFormes.gumshoosCosmetic1,
            SpeciesIDs.USUMFormes.vikavoltCosmetic1,
            SpeciesIDs.USUMFormes.lurantisCosmetic1,
            SpeciesIDs.USUMFormes.salazzleCosmetic1,
            SpeciesIDs.USUMFormes.kommoOCosmetic1,
            SpeciesIDs.USUMFormes.araquanidCosmetic1,
            SpeciesIDs.USUMFormes.togedemaruCosmetic1,
            SpeciesIDs.USUMFormes.ribombeeCosmetic1,
            SpeciesIDs.USUMFormes.greninjaCosmetic1,
            SpeciesIDs.USUMFormes.zygarde10Cosmetic1, SpeciesIDs.USUMFormes.zygardeCosmetic1,
            SpeciesIDs.USUMFormes.miniorCosmetic1, SpeciesIDs.USUMFormes.miniorCosmetic2, SpeciesIDs.USUMFormes.miniorCosmetic3,
            SpeciesIDs.USUMFormes.miniorCosmetic4, SpeciesIDs.USUMFormes.miniorCosmetic5, SpeciesIDs.USUMFormes.miniorCosmetic6,
            SpeciesIDs.USUMFormes.miniorCCosmetic1, SpeciesIDs.USUMFormes.miniorCCosmetic2, SpeciesIDs.USUMFormes.miniorCCosmetic3,
            SpeciesIDs.USUMFormes.miniorCCosmetic4, SpeciesIDs.USUMFormes.miniorCCosmetic5, SpeciesIDs.USUMFormes.miniorCCosmetic6,
            SpeciesIDs.USUMFormes.magearnaCosmetic1,
            SpeciesIDs.USUMFormes.pikachuCosmetic1, SpeciesIDs.USUMFormes.pikachuCosmetic2, SpeciesIDs.USUMFormes.pikachuCosmetic3,
            SpeciesIDs.USUMFormes.pikachuCosmetic4, SpeciesIDs.USUMFormes.pikachuCosmetic5, SpeciesIDs.USUMFormes.pikachuCosmetic6,
            SpeciesIDs.USUMFormes.pikachuCosmetic7, // Pikachu With Funny Hats
            SpeciesIDs.USUMFormes.rockruffCosmetic1
    );

    public static List<Integer> getActuallyCosmeticForms(int romType) {
        if (romType == Type_SM) {
            return actuallyCosmeticFormsSM;
        } else {
            return actuallyCosmeticFormsUSUM;
        }
    }

    private static final List<Integer> ignoreFormsSM = Arrays.asList(
            SpeciesIDs.SMFormes.cherrimCosmetic1,
            SpeciesIDs.SMFormes.greninjaCosmetic1,
            SpeciesIDs.SMFormes.zygarde10Cosmetic1,
            SpeciesIDs.SMFormes.zygardeCosmetic1,
            SpeciesIDs.SMFormes.miniorCosmetic1,
            SpeciesIDs.SMFormes.miniorCosmetic2,
            SpeciesIDs.SMFormes.miniorCosmetic3,
            SpeciesIDs.SMFormes.miniorCosmetic4,
            SpeciesIDs.SMFormes.miniorCosmetic5,
            SpeciesIDs.SMFormes.miniorCosmetic6,
            SpeciesIDs.SMFormes.mimikyuCosmetic1,
            SpeciesIDs.SMFormes.mimikyuCosmetic3
    );

    private static final List<Integer> ignoreFormsUSUM = Arrays.asList(
            SpeciesIDs.USUMFormes.cherrimCosmetic1,
            SpeciesIDs.USUMFormes.greninjaCosmetic1,
            SpeciesIDs.USUMFormes.zygarde10Cosmetic1,
            SpeciesIDs.USUMFormes.zygardeCosmetic1,
            SpeciesIDs.USUMFormes.miniorCosmetic1,
            SpeciesIDs.USUMFormes.miniorCosmetic2,
            SpeciesIDs.USUMFormes.miniorCosmetic3,
            SpeciesIDs.USUMFormes.miniorCosmetic4,
            SpeciesIDs.USUMFormes.miniorCosmetic5,
            SpeciesIDs.USUMFormes.miniorCosmetic6,
            SpeciesIDs.USUMFormes.mimikyuCosmetic1,
            SpeciesIDs.USUMFormes.mimikyuCosmetic3,
            SpeciesIDs.USUMFormes.rockruffCosmetic1
    );

    public static List<Integer> getIgnoreForms(int romType) {
        if (romType == Type_SM) {
            return ignoreFormsSM;
        } else {
            return ignoreFormsUSUM;
        }
    }

    private static final Map<Integer,Integer> altFormesWithCosmeticFormsSM = setupAltFormesWithCosmeticForms(Type_SM);
    private static final Map<Integer,Integer> altFormesWithCosmeticFormsUSUM = setupAltFormesWithCosmeticForms(Type_USUM);

    public static Map<Integer,Integer> getAltFormesWithCosmeticForms(int romType) {
        if (romType == Type_SM) {
            return altFormesWithCosmeticFormsSM;
        } else {
            return altFormesWithCosmeticFormsUSUM;
        }
    }

    private static Map<Integer,Integer> setupAltFormesWithCosmeticForms(int romType) {
        Map<Integer,Integer> map = new HashMap<>();
        if (romType == Type_SM) {
            map.put(SpeciesIDs.SMFormes.raticateA,1); // 1 form (Totem)
            map.put(SpeciesIDs.SMFormes.zygarde10,1); // 1 form (Power Construct)
            map.put(SpeciesIDs.SMFormes.miniorC,6); // 6 forms (colors)
        } else {
            map.put(SpeciesIDs.USUMFormes.raticateA,1); // 1 form (Totem)
            map.put(SpeciesIDs.USUMFormes.marowakA,1); // 1 form (Totem)
            map.put(SpeciesIDs.USUMFormes.zygarde10,1); // 1 form (Power Construct)
            map.put(SpeciesIDs.USUMFormes.miniorC,6); // 6 forms (colors)
        }

        return map;
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

    public static byte getSunnyEvolutionExtraInfo(int romType) {
        return (byte) (romType == Type_SM ? 0x1E : 0x20);
    }

    public static byte getMoonyEvolutionExtraInfo(int romType) {
        return (byte) (romType == Type_SM ? 0x1F : 0x21);
    }

    public static final int evolutionMethodLevelGame = 0x24,
            evolutionMethodLevelGameDay = 0x25, evolutionMethodLevelGameNight = 0x26;

    public static final List<Integer> gameSpecificEvolutionMethods = Collections.unmodifiableList(Arrays.asList(
            evolutionMethodLevelGame, evolutionMethodLevelGameDay, evolutionMethodLevelGameNight
    ));

    private static final EvolutionType[] evolutionTypeTable = new EvolutionType[] {
            EvolutionType.HAPPINESS, EvolutionType.HAPPINESS_DAY, EvolutionType.HAPPINESS_NIGHT, EvolutionType.LEVEL,
            EvolutionType.TRADE, EvolutionType.TRADE_ITEM, EvolutionType.TRADE_SPECIAL, EvolutionType.STONE,
            EvolutionType.LEVEL_ATTACK_HIGHER, EvolutionType.LEVEL_ATK_DEF_SAME, EvolutionType.LEVEL_DEFENSE_HIGHER,
            EvolutionType.LEVEL_LOW_PV, EvolutionType.LEVEL_HIGH_PV, EvolutionType.LEVEL_CREATE_EXTRA,
            EvolutionType.LEVEL_IS_EXTRA, EvolutionType.HIGH_BEAUTY, EvolutionType.STONE_MALE_ONLY,
            EvolutionType.STONE_FEMALE_ONLY, EvolutionType.ITEM_DAY, EvolutionType.ITEM_NIGHT,
            EvolutionType.WITH_MOVE, EvolutionType.WITH_OTHER, EvolutionType.LEVEL_MALE_ONLY,
            EvolutionType.LEVEL_FEMALE_ONLY, EvolutionType.MAGNETIC_FIELD, EvolutionType.MOSS_ROCK,
            EvolutionType.ICE_ROCK, EvolutionType.LEVEL_UPSIDE_DOWN, EvolutionType.FAIRY_AFFECTION,
            EvolutionType.LEVEL_WITH_DARK, EvolutionType.LEVEL_RAIN, EvolutionType.LEVEL_DAY, EvolutionType.LEVEL_NIGHT,
            EvolutionType.LEVEL_FEMALE_ESPURR, EvolutionType.NONE, EvolutionType.NONE,
            EvolutionType.NONE, EvolutionType.NONE, EvolutionType.SNOWY,
            EvolutionType.LEVEL_DUSK, EvolutionType.LEVEL_ULTRA, EvolutionType.STONE_ULTRA
    };

    public static int evolutionTypeToIndex(EvolutionType et) {
        for (int i = 0; i < evolutionTypeTable.length; i++) {
            if (et == evolutionTypeTable[i]) {
                return i + 1;
            }
        }
        if (et == EvolutionType.LEVEL_GAME_THIS || et == EvolutionType.LEVEL_GAME_OTHER) {
            return evolutionMethodLevelGame;
        } else if (et == EvolutionType.LEVEL_GAME_THIS_DAY || et == EvolutionType.LEVEL_GAME_OTHER_DAY) {
            return evolutionMethodLevelGameDay;
        } else if (et == EvolutionType.LEVEL_GAME_THIS_NIGHT || et == EvolutionType.LEVEL_GAME_OTHER_NIGHT) {
            return evolutionMethodLevelGameNight;
        }
        return -1;
    }

    public static EvolutionType evolutionTypeFromIndex(int index) {
        if (index == -1) {
            return EvolutionType.NONE;
        }
        return evolutionTypeTable[index - 1];
    }

    public static List<Integer> getAreaIndicesForLocationEvolution(EvolutionType et, int romType) {
        switch (et) {
            case MAGNETIC_FIELD:
                // {Vast Poni Canyon} : {Blush Mountain, Vast Poni Canyon}
                return romType == Type_SM ? Collections.singletonList(198) : Arrays.asList(126, 198);
            case MOSS_ROCK:
                return Collections.singletonList(74); // {Lush Jungle}
            case ICE_ROCK:
            case SNOWY:
                return Collections.singletonList(138); // {Mount Lanakila}
            default:
                throw new IllegalArgumentException(et + " is not a valid EvolutionType for this game.");
        }
    }

    private static List<Boolean> setupRelevantEncounterFiles(int romType) {
        int fileCount = romType == Type_SM ? 2761 : 3696;
        List<Boolean> list = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            if (((i - 9) % 11 == 0) || (i % 11 == 0)) {
                list.add(true);
            } else {
                list.add(false);
            }
        }

        return list;
    }


    public static Map<Integer, List<Integer>> getHardcodedTradeTextOffsets(int romType) {
        Map<Integer, List<Integer>> hardcodedTradeTextOffsets = new HashMap<>();
        if (romType == Gen7Constants.Type_USUM) {
            // For some reason, the Route 2 trade is hardcoded in USUM but not in SM
            hardcodedTradeTextOffsets.put(0, Arrays.asList(20, 21, 22));
        }
        hardcodedTradeTextOffsets.put(1, Arrays.asList(26, 28, 30));
        hardcodedTradeTextOffsets.put(2, Arrays.asList(32, 33, 34, 36));
        hardcodedTradeTextOffsets.put(3, Arrays.asList(38, 39, 40, 42));
        hardcodedTradeTextOffsets.put(4, Arrays.asList(44, 45, 46, 48));
        hardcodedTradeTextOffsets.put(5, Arrays.asList(50, 51, 52, 54));
        hardcodedTradeTextOffsets.put(6, Arrays.asList(56, 57, 58, 60));
        return hardcodedTradeTextOffsets;
    }

    private static final Set<Integer> bannedItemsSM = setupBannedItemsSM();
    private static final Set<Integer> bannedItemsUSUM = setupBannedItemsUSUM();
    public static final Set<Integer> badItems = setupBadItems();
    private static final Set<Integer> regularShopItemsSM = setupRegularShopItemsSM();
    private static final Set<Integer> regularShopItemsUSUM = setupRegularShopItemsUSUM();
    public static final Set<Integer> opShopItems = setupOPShopItems();
    public static final Set<Integer> heldZCrystals = setupHeldZCrystals();

    private static Set<Integer> setupBannedItemsSM() {
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
        addBetween(set, ItemIDs.machBike, ItemIDs.meteoriteSecondForm);
        addBetween(set, ItemIDs.prisonBottle, ItemIDs.megaCuff);
        addBetween(set, ItemIDs.meteoriteThirdForm, ItemIDs.eonFlute);

        // Z-Crystals
        addBetween(set, ItemIDs.normaliumZHeld, ItemIDs.pikaniumZHeld);
        addBetween(set, ItemIDs.decidiumZHeld, ItemIDs.pikashuniumZBag);

        // Key Items (Gen 7)
        set.addAll(Arrays.asList(ItemIDs.zRing, ItemIDs.sparklingStone, ItemIDs.zygardeCube, ItemIDs.ridePager,
                ItemIDs.sunFlute, ItemIDs.moonFlute, ItemIDs.enigmaticCard));
        addBetween(set, ItemIDs.forageBag, ItemIDs.professorsMask);

        // Unused
        set.addAll(Arrays.asList(ItemIDs.unused848, ItemIDs.unused859));
        addBetween(set, ItemIDs.unused837, ItemIDs.unused840);
        addBetween(set, ItemIDs.silverRazzBerry, ItemIDs.liftKey);
        addBetween(set, ItemIDs.stretchySpring, ItemIDs.pewterCrunchies);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBannedItemsUSUM() {
        Set<Integer> set = new HashSet<>(bannedItemsSM);
        // Z-Crystals
        addBetween(set, ItemIDs.solganiumZBag, ItemIDs.kommoniumZBag);
        // Key Items
        addBetween(set, ItemIDs.zPowerRing, ItemIDs.leftPokeBall);
        // ROTO LOTO
        addBetween(set, ItemIDs.rotoHatch, ItemIDs.rotoCatch);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItems() {
        Set<Integer> set = new HashSet<>(Arrays.asList(ItemIDs.oddKeystone, ItemIDs.griseousOrb, ItemIDs.adamantOrb,
                ItemIDs.lustrousOrb, ItemIDs.soulDew, ItemIDs.lightBall, ItemIDs.oranBerry, ItemIDs.quickPowder,
                ItemIDs.passOrb, ItemIDs.discountCoupon, ItemIDs.strangeSouvenir, ItemIDs.festivalTicket));
        addBetween(set, ItemIDs.growthMulch, ItemIDs.gooeyMulch); // mulch
        addBetween(set, ItemIDs.mail1, ItemIDs.mail12); // mails
        addBetween(set, ItemIDs.figyBerry, ItemIDs.belueBerry); // berries without useful battle effects
        addBetween(set, ItemIDs.luckyPunch, ItemIDs.leek); // pokemon specific
        addBetween(set, ItemIDs.redScarf, ItemIDs.yellowScarf); // contest scarves
        addBetween(set, ItemIDs.relicCopper, ItemIDs.relicCrown); // relic items
        addBetween(set, ItemIDs.richMulch, ItemIDs.amazeMulch); // more mulch
        addBetween(set, ItemIDs.gengarite, ItemIDs.latiosite); // Mega Stones, part 1
        addBetween(set, ItemIDs.swampertite, ItemIDs.beedrillite); // Mega Stones, part 2
        addBetween(set, ItemIDs.cameruptite, ItemIDs.beedrillite); // Mega Stones, part 3
        addBetween(set, ItemIDs.fightingMemory, ItemIDs.fairyMemory); // Memories
        set.addAll(Arrays.asList(ItemIDs.shoalSalt, ItemIDs.shoalShell)); // Shoal items; have no purpose and sell for $10.
        addBetween(set, ItemIDs.blueFlute, ItemIDs.whiteFlute); // Flutes; have no purpose and sell for $10.
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupRegularShopItemsSM() {
        Set<Integer> set = new HashSet<>(GlobalConstants.regularShopItems);
        set.add(ItemIDs.honey);
        set.add(ItemIDs.adrenalineOrb);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupRegularShopItemsUSUM() {
        Set<Integer> set = new HashSet<>(regularShopItemsSM);
        set.add(ItemIDs.pokeToy);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupOPShopItems() {
        Set<Integer> set = new HashSet<>();
        // "Money items" etc
        set.add(ItemIDs.lavaCookie);
        set.add(ItemIDs.berryJuice);
        set.add(ItemIDs.rareCandy);
        set.add(ItemIDs.oldGateau);
        addBetween(set, ItemIDs.tinyMushroom, ItemIDs.nugget);
        set.add(ItemIDs.rareBone);
        addBetween(set, ItemIDs.lansatBerry, ItemIDs.rowapBerry);
        set.add(ItemIDs.prettyFeather);
        addBetween(set, ItemIDs.balmMushroom, ItemIDs.casteliacone);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupHeldZCrystals() {
        Set<Integer> set = new HashSet<>();
        addBetween(set, ItemIDs.normaliumZHeld, ItemIDs.pikaniumZHeld);
        addBetween(set, ItemIDs.decidiumZHeld, ItemIDs.mewniumZHeld);
        set.add(ItemIDs.pikashuniumZHeld);
        addBetween(set, ItemIDs.mimikiumZHeld, ItemIDs.ultranecroziumZHeld); // USUM only
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
        if (romType == Type_SM) {
            return bannedItemsSM;
        } else {
            return bannedItemsUSUM;
        }
    }

    public static Set<Integer> getRegularShopItems(int romType) {
        if (romType == Type_SM) {
            return regularShopItemsSM;
        } else {
            return regularShopItemsUSUM;
        }
    }

    private static final List<Integer> requiredFieldTMsSM = Arrays.asList(
            ItemIDs.tm02, ItemIDs.tm03, ItemIDs.tm05, ItemIDs.tm06, ItemIDs.tm13, ItemIDs.tm24, ItemIDs.tm26,
            ItemIDs.tm28, ItemIDs.tm30, ItemIDs.tm31, ItemIDs.tm35, ItemIDs.tm36, ItemIDs.tm39, ItemIDs.tm41,
            ItemIDs.tm46, ItemIDs.tm49, ItemIDs.tm50, ItemIDs.tm53, ItemIDs.tm55, ItemIDs.tm57, ItemIDs.tm59,
            ItemIDs.tm61, ItemIDs.tm62, ItemIDs.tm64, ItemIDs.tm72, ItemIDs.tm73, ItemIDs.tm74, ItemIDs.tm79,
            ItemIDs.tm80, ItemIDs.tm81, ItemIDs.tm83, ItemIDs.tm84, ItemIDs.tm85, ItemIDs.tm86, ItemIDs.tm88,
            ItemIDs.tm91, ItemIDs.tm93, ItemIDs.tm97, ItemIDs.tm99, ItemIDs.tm100
    );

    private static final List<Integer> requiredFieldTMsUSUM = Arrays.asList(
            ItemIDs.tm02, ItemIDs.tm03, ItemIDs.tm05, ItemIDs.tm06, ItemIDs.tm13, ItemIDs.tm23, ItemIDs.tm24,
            ItemIDs.tm26, ItemIDs.tm28, ItemIDs.tm30, ItemIDs.tm31, ItemIDs.tm32, ItemIDs.tm35, ItemIDs.tm36,
            ItemIDs.tm39, ItemIDs.tm41, ItemIDs.tm46, ItemIDs.tm49, ItemIDs.tm50, ItemIDs.tm53, ItemIDs.tm55,
            ItemIDs.tm57, ItemIDs.tm59, ItemIDs.tm61, ItemIDs.tm62, ItemIDs.tm64, ItemIDs.tm72, ItemIDs.tm73,
            ItemIDs.tm74, ItemIDs.tm79, ItemIDs.tm80, ItemIDs.tm81, ItemIDs.tm83, ItemIDs.tm84, ItemIDs.tm85,
            ItemIDs.tm86, ItemIDs.tm88, ItemIDs.tm91, ItemIDs.tm93, ItemIDs.tm97, ItemIDs.tm99, ItemIDs.tm100
    );

    public static void main(String[] args) {
        System.out.println(requiredFieldTMsSM.stream().distinct().sorted().collect(Collectors.toList()));
        System.out.println(requiredFieldTMsUSUM.stream().distinct().sorted().collect(Collectors.toList()));
    }

    public static List<Integer> getRequiredFieldTMs(int romType) {
        if (romType == Type_SM) {
            return requiredFieldTMsSM.stream().distinct().collect(Collectors.toList());
        } else {
            return requiredFieldTMsUSUM;
        }
    }

    public static void tagTrainersSM(List<Trainer> trs) {

        tag(trs,"ELITE1", 23, 152, 349); // Hala
        tag(trs,"ELITE2",90, 153, 351); // Olivia
        tag(trs,"ELITE3", 154, 403); // Nanu
        tag(trs,"ELITE4", 155, 359); // Hapu
        tag(trs,"ELITE5", 149, 350); // Acerola
        tag(trs,"ELITE6", 156, 352); // Kahili

        tag(trs,"RIVAL2-0", 129);
        tag(trs,"RIVAL2-1", 413);
        tag(trs,"RIVAL2-2", 414);
        tagRival(trs,"RIVAL3",477);

        tagRival(trs,"FRIEND1", 6);
        tagRival(trs,"FRIEND2", 9);
        tagRival(trs,"FRIEND3", 12);
        tagRival(trs,"FRIEND4", 76);
        tagRival(trs,"FRIEND5", 82);
        tagRival(trs,"FRIEND6", 438);
        tagRival(trs,"FRIEND7", 217);
        tagRival(trs,"FRIEND8", 220);
        tagRival(trs,"FRIEND9", 447);
        tagRival(trs,"FRIEND10", 450);
        tagRival(trs,"FRIEND11", 482);
        tagRival(trs,"FRIEND12", 356);

        tag(trs,"THEMED:GLADION-STRONG", 79, 185, 239, 240, 415, 416, 417, 418, 419, 441);
        tag(trs,"THEMED:ILIMA-STRONG", 52, 215, 216, 396);
        tag(trs,"THEMED:LANA-STRONG", 144);
        tag(trs,"THEMED:KIAWE-STRONG", 398);
        tag(trs,"THEMED:MALLOW-STRONG", 146);
        tag(trs,"THEMED:SOPHOCLES-STRONG", 405);
        tag(trs,"THEMED:MOLAYNE-STRONG", 167, 481);
        tag(trs,"THEMED:MINA-STRONG", 435, 467);
        tag(trs,"THEMED:PLUMERIA-STRONG", 89, 238, 401);
        tag(trs,"THEMED:SINA-STRONG", 75);
        tag(trs,"THEMED:DEXIO-STRONG", 74, 412);
        tag(trs,"THEMED:FABA-STRONG",132, 241, 360, 410);
        tag(trs,"THEMED:GUZMA-LEADER", 138, 235, 236, 400);
        tag(trs,"THEMED:LUSAMINE-LEADER", 131, 158);
    }

    public static void tagTrainersUSUM(List<Trainer> trs) {

        tag(trs,"ELITE1", 23, 650); // Hala
        tag(trs,"ELITE2", 90, 153, 351); // Olivia
        tag(trs,"ELITE3", 154, 508); // Nanu
        tag(trs,"ELITE4", 359, 497); // Hapu
        tag(trs,"ELITE5", 489, 490); // Big Mo
        tag(trs,"ELITE6", 149, 350); // Acerola
        tag(trs,"ELITE7", 156, 352); // Kahili

        tagRival(trs,"RIVAL2", 477); // Kukui

        // Hau
        tagRival(trs,"FRIEND1", 491);
        tagRival(trs,"FRIEND2", 9);
        tagRival(trs,"FRIEND3", 12);
        tagRival(trs,"FRIEND4", 76);
        tagRival(trs,"FRIEND5", 82);
        tagRival(trs,"FRIEND6", 438);
        tagRival(trs,"FRIEND7", 217);
        tagRival(trs,"FRIEND8", 220);
        tagRival(trs,"FRIEND9", 447);
        tagRival(trs,"FRIEND10", 450);
        tagRival(trs,"FRIEND11", 494);
        tagRival(trs,"FRIEND12", 356);

        tag(trs,"THEMED:GLADION-STRONG", 79, 185, 239, 240, 415, 416, 417, 418, 419, 441);
        tag(trs,"THEMED:ILIMA-STRONG", 52, 215, 216, 396, 502);
        tag(trs,"THEMED:LANA-STRONG", 144, 503);
        tag(trs,"THEMED:KIAWE-STRONG", 398, 504);
        tag(trs,"THEMED:MALLOW-STRONG", 146, 505);
        tag(trs,"THEMED:SOPHOCLES-STRONG", 405, 506);
        tag(trs,"THEMED:MINA-STRONG", 507);
        tag(trs,"THEMED:PLUMERIA-STRONG", 89, 238, 401);
        tag(trs,"THEMED:SINA-STRONG", 75);
        tag(trs,"THEMED:DEXIO-STRONG", 74, 412, 623);
        tag(trs,"THEMED:FABA-STRONG", 132, 241, 410, 561);
        tag(trs,"THEMED:SOLIERA-STRONG", 498, 499, 648, 651);
        tag(trs,"THEMED:DULSE-STRONG", 500, 501, 649, 652);
        tag(trs,"THEMED:GUZMA-LEADER", 138, 235, 236, 558, 647);
        tag(trs,"THEMED:LUSAMINE-LEADER", 131, 644);

        tag(trs,"UBER", 541, 542, 543, 580, 572, 573, 559, 560, 562, 645); // RR Episode
    }

    private static void tagRival(List<Trainer> allTrainers, String tag, int offset) {
        allTrainers.get(offset - 1).setTag(tag + "-0");
        allTrainers.get(offset).setTag(tag + "-1");
        allTrainers.get(offset + 1).setTag(tag + "-2");

    }

    private static void tag(List<Trainer> allTrainers, String tag, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).setTag(tag);
            }
        }
    }

    public static final HashMap<String, Type> gymAndEliteThemesSM = setupGymAndEliteThemesSM();

    private static HashMap<String, Type> setupGymAndEliteThemesSM() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("ELITE1", Type.FIGHTING); //Hala
        themeMap.put("ELITE2", Type.ROCK); //Olivia
        themeMap.put("ELITE3", Type.DARK); //Nanu
        themeMap.put("ELITE4", Type.GROUND); //Hapu
        themeMap.put("ELITE5", Type.GHOST); //Acerola
        themeMap.put("ELITE6", Type.FLYING); //Kahili
        return themeMap;
    }

    public static final HashMap<String, Type> gymAndEliteThemesUSUM = setupGymAndEliteThemesUSUM();

    private static HashMap<String, Type> setupGymAndEliteThemesUSUM() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("ELITE1", Type.FIGHTING); //Hala
        themeMap.put("ELITE2", Type.ROCK); //Olivia
        themeMap.put("ELITE3", Type.DARK); //Nanu
        themeMap.put("ELITE4", Type.GROUND); //Hapu
        themeMap.put("ELITE5", Type.STEEL); //Molayne
        themeMap.put("ELITE6", Type.GHOST); //Acerola
        themeMap.put("ELITE7", Type.FLYING); //Kahili
        return themeMap;
    }

    public static void setMultiBattleStatusSM(List<Trainer> trs) {
        // All Double Battles in Gen 7 are internally treated as a Multi Battle
        // 92 + 93: Rising Star Duo Justin and Lauren
        // 97 + 98: Twins Isa and Nico
        // 134 + 136: Aether Foundation Employees in Secret Lab B w/ Hau
        // 141 + 227: Team Skull Grunts on Route 17
        // 241 + 442: Faba and Aether Foundation Employee w/ Hau
        // 262 + 265: Ace Duo Aimee and Kent
        // 270 + 299: Swimmers Jake and Yumi
        // 278 + 280: Honeymooners Noriko and Devin
        // 303 + 307: Veteran Duo Tsunekazu and Nobuko
        // 315 + 316: Team Skull Grunts in Po Town
        // 331 + 332: Karate Family Guy and Samuel
        // 371 + 372: Twins Harper and Sarah
        // 373 + 374: Swimmer Girls Ashlyn and Kylie
        // 375 + 376: Golf Buddies Tara and Tina
        // 421 + 422: Athletic Siblings Alyssa and Sho
        // 425 + 426: Punk Pair Lane and Yoko
        // 429 + 430: Punk Pair Troy and Marie
        // 443 + 444: Team Skull Grunts in Diglett's Tunnel w/ Hau
        // 453 + 454: Aether Foundation Employees w/ Hau
        // 455 + 456: Aether Foundation Employees w/ Gladion
        setMultiBattleStatus(trs, 92, 93, 97, 98, 134, 136, 141, 227, 241, 262, 265, 270, 278, 280, 299, 303,
                307, 315, 316, 331, 332, 371, 372, 373, 374, 375, 376, 421, 422, 425, 426, 429, 430, 442, 443, 444, 453,
                454, 455, 456
        );
    }

    public static void setMultiBattleStatusUSUM(List<Trainer> trs) {
        // All Double Battles in Gen 7 are internally treated as a Multi Battle
        // 92 + 93: Rising Star Duo Justin and Lauren
        // 97 + 98: Twins Isa and Nico
        // 134 + 136: Aether Foundation Employees in Secret Lab B w/ Hau
        // 141 + 227: Team Skull Grunts on Route 17
        // 178 + 511: Capoeira Couple Cara and Douglas
        // 241 + 442: Faba and Aether Foundation Employee w/ Hau
        // 262 + 265: Ace Duo Aimee and Kent
        // 270 + 299: Swimmers Jake and Yumi
        // 278 + 280: Honeymooners Noriko and Devin
        // 303 + 307: Veteran Duo Tsunekazu and Nobuko
        // 315 + 316: Team Skull Grunts in Po Town
        // 331 + 332: Karate Family Guy and Samuel
        // 371 + 372: Twins Harper and Sarah
        // 373 + 374: Swimmer Girls Ashlyn and Kylie
        // 375 + 376: Golf Buddies Tara and Tina
        // 421 + 422: Athletic Siblings Alyssa and Sho
        // 425 + 426: Punk Pair Lane and Yoko
        // 429 + 430: Punk Pair Troy and Marie
        // 443 + 444: Team Skull Grunts in Diglett's Tunnel w/ Hau
        // 453 + 454: Aether Foundation Employees w/ Hau
        // 455 + 456: Aether Foundation Employees w/ Gladion
        // 514 + 521: Tourist Couple Yuriko and Landon
        // 515 + 534: Tourist Couple Steve and Reika
        // 529 + 530: Dancing Family Jen and Fumiko
        // 554 + 561: Aether Foundation Employee and Faba w/ Lillie
        // 557 + 578: GAME FREAK Iwao and Morimoto
        // 586 + 595: Team Rainbow Rocket Grunts w/ Guzma
        // 613 + 626: Master & Apprentice Kaimana and Breon
        // 617 + 618: Sparring Partners Allon and Eimar
        // 619 + 620: Sparring Partners Craig and Jason
        setMultiBattleStatus(trs, 92, 93, 97, 98, 134, 136, 141, 178, 227, 241, 262, 265, 270, 278, 280, 299,
                303, 307, 315, 316, 331, 332, 371, 372, 373, 374, 375, 376, 421, 422, 425, 426, 429, 430, 442, 443, 444,
                453, 454, 455, 456, 511, 514, 515, 521, 529, 530, 534, 544, 557, 561, 578, 586, 595, 613, 617, 618, 619,
                620, 626
        );
    }

    private static void setMultiBattleStatus(List<Trainer> allTrainers, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).setMultiBattleStatus(Trainer.MultiBattleStatus.ALWAYS);
            }
        }
    }

    public static void setForcedRivalStarterPositionsUSUM(List<Trainer> allTrainers) {

        // Hau 3
        allTrainers.get(11).setForceStarterPosition(0);
        allTrainers.get(12).setForceStarterPosition(0);
        allTrainers.get(13).setForceStarterPosition(0);

        // Hau 6
        allTrainers.get(216).setForceStarterPosition(0);
        allTrainers.get(217).setForceStarterPosition(0);
        allTrainers.get(218).setForceStarterPosition(0);

        // Kukui
        allTrainers.get(476).setForceStarterPosition(5);
        allTrainers.get(477).setForceStarterPosition(5);
        allTrainers.get(478).setForceStarterPosition(5);
    }

    /**
     * Based on
     * <a href=https://bulbapedia.bulbagarden.net/wiki/Walkthrough:Pok%C3%A9mon_Sun_and_Moon>this walkthrough</a>.
     */
    private static final List<String> locationTagsTraverseOrderSM = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "HAU'OLI CITY", "ROUTE 2", "HAU'OLI CEMETERY", "VERDANT CAVERN", "ROUTE 3", "MELEMELE MEADOW",
            "SEAWARD CAVE", "KALA'E BAY", "TEN CARAT HILL", "ROUTE 4", "PANIOLA TOWN", "ROUTE 5", "BROOKLET HILL",
            "ROUTE 6",  "ROUTE 7", "WELA VOLCANO PARK", "ROUTE 8", "LUSH JUNGLE", "DIGLETT'S TUNNEL", "ROUTE 9",
            "MEMORIAL HILL", "AKALA OUTSKIRTS", "HANO BEACH", "MALIE GARDEN", "MALIE CITY", "ROUTE 10",
            "MOUNT HOKULANI", "ROUTE 11", "ROUTE 12", "BLUSH MOUNTAIN", "SECLUDED SHORE", "ROUTE 13", "TAPU VILLAGE",
            "ROUTE 15", "ROUTE 14", "THRIFTY MEGAMART", "ROUTE 16", "ULA'ULA MEADOW", "ROUTE 17", "SEAFOLK VILLAGE",
            "PONI WILDS", "ANCIENT PONI PATH", "PONI BREAKER COAST", "EXEGGUTOR ISLAND", "VAST PONI CANYON",
            "MOUNT LANAKILA", "HAINA DESERT", "PONI GROVE", "PONI PLAINS", "PONI MEADOW", "RESOLUTION CAVE",
            "MELEMELE SEA", "PONI COAST", "PONI GAUNTLET", "UNUSED"
    ));

    /**
     * Based on
     * <a href=https://bulbapedia.bulbagarden.net/wiki/Walkthrough:Pok%C3%A9mon_Sun_and_Moon>this walkthrough</a>,
     * but adding "SANDY CAVE" and "DIVIDING PEAK TUNNEL" in appropriate spots.
     */
    private static final List<String> locationTagsTraverseOrderUSUM = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "HAU'OLI CITY", "ROUTE 2", "SANDY CAVE", "HAU'OLI CEMETERY", "VERDANT CAVERN", "ROUTE 3",
            "MELEMELE MEADOW", "SEAWARD CAVE", "KALA'E BAY", "TEN CARAT HILL", "ROUTE 4", "PANIOLA TOWN", "ROUTE 5",
            "BROOKLET HILL", "ROUTE 6",  "ROUTE 7", "WELA VOLCANO PARK", "DIVIDING PEAK TUNNEL", "ROUTE 8",
            "LUSH JUNGLE", "DIGLETT'S TUNNEL", "ROUTE 9", "MEMORIAL HILL", "AKALA OUTSKIRTS", "HANO BEACH",
            "MALIE GARDEN", "MALIE CITY", "ROUTE 10", "MOUNT HOKULANI", "ROUTE 11", "ROUTE 12", "BLUSH MOUNTAIN",
            "ULA'ULA BEACH", "ROUTE 13", "TAPU VILLAGE", "ROUTE 15", "ROUTE 14", "THRIFTY MEGAMART", "ROUTE 16",
            "ULA'ULA MEADOW", "ROUTE 17", "SEAFOLK VILLAGE", "PONI WILDS", "ANCIENT PONI PATH", "PONI BREAKER COAST",
            "EXEGGUTOR ISLAND", "VAST PONI CANYON", "MOUNT LANAKILA", "HAINA DESERT", "PONI GROVE", "PONI PLAINS",
            "PONI MEADOW", "RESOLUTION CAVE", "MELEMELE SEA", "PONI COAST", "PONI GAUNTLET", "UNUSED"
    ));

    public static List<String> getLocationTagsTraverseOrder(int romType) {
        return romType == Type_SM ? locationTagsTraverseOrderSM : locationTagsTraverseOrderUSUM;
    }

    public static final Map<Integer, Integer> balancedItemPrices = Stream.of(new Integer[][]{
            {ItemIDs.masterBall, 3000},
            {ItemIDs.safariBall, 500},
            {ItemIDs.premierBall, 200},
            {ItemIDs.cherishBall, 200},
            {ItemIDs.ether, 3000},
            {ItemIDs.maxEther, 4500},
            {ItemIDs.elixir, 15000},
            {ItemIDs.maxElixir, 18000},
            {ItemIDs.sacredAsh, 5000},
            {ItemIDs.ppMax, 25000},
            {ItemIDs.heartScale, 5000},
            {ItemIDs.griseousOrb, 10000},
            {ItemIDs.douseDrive, 1000},
            {ItemIDs.shockDrive, 1000},
            {ItemIDs.burnDrive, 1000},
            {ItemIDs.chillDrive, 1000},
            {ItemIDs.sweetHeart, 150},
            {ItemIDs.adamantOrb, 10000},
            {ItemIDs.lustrousOrb, 10000},
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
            {ItemIDs.quickClaw, 4500},
            {ItemIDs.sootheBell, 1000},
            {ItemIDs.mentalHerb, 1000},
            {ItemIDs.choiceBand, 10000},
            {ItemIDs.silverPowder, 2000},
            {ItemIDs.amuletCoin, 15000},
            {ItemIDs.cleanseTag, 1000},
            {ItemIDs.soulDew, 200},
            {ItemIDs.deepSeaTooth, 3000},
            {ItemIDs.deepSeaScale, 3000},
            {ItemIDs.focusBand, 3000},
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
            {ItemIDs.icyRock, 200},
            {ItemIDs.smoothRock, 200},
            {ItemIDs.heatRock, 200},
            {ItemIDs.dampRock, 200},
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
            {ItemIDs.fullIncense, 1000},
            {ItemIDs.luckIncense, 15000},
            {ItemIDs.pureIncense, 1000},
            {ItemIDs.protector, 3000},
            {ItemIDs.electirizer, 3000},
            {ItemIDs.magmarizer, 3000},
            {ItemIDs.dubiousDisc, 3000},
            {ItemIDs.reaperCloth, 3000},
            {ItemIDs.tm07, 20000},
            {ItemIDs.tm11, 20000},
            {ItemIDs.tm14, 20000},
            {ItemIDs.tm15, 20000},
            {ItemIDs.tm18, 20000},
            {ItemIDs.tm25, 20000},
            {ItemIDs.tm28, 20000},
            {ItemIDs.tm37, 20000},
            {ItemIDs.tm38, 20000},
            {ItemIDs.tm50, 20000},
            {ItemIDs.tm52, 20000},
            {ItemIDs.tm59, 20000},
            {ItemIDs.tm68, 20000},
            {ItemIDs.tm70, 20000},
            {ItemIDs.tm71, 20000},
            {ItemIDs.fastBall, 300},
            {ItemIDs.levelBall, 300},
            {ItemIDs.lureBall, 300},
            {ItemIDs.heavyBall, 300},
            {ItemIDs.loveBall, 300},
            {ItemIDs.friendBall, 300},
            {ItemIDs.moonBall, 300},
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
            {ItemIDs.dreamBall, 1000},
            {ItemIDs.relicGold, 0},
            {ItemIDs.tm93, 20000},
            {ItemIDs.tm94, 20000},
            {ItemIDs.weaknessPolicy, 2000},
            {ItemIDs.assaultVest, 6000},
            {ItemIDs.pixiePlate, 2000},
            {ItemIDs.abilityCapsule, 5000},
            {ItemIDs.whippedDream, 3000},
            {ItemIDs.sachet, 3000},
            {ItemIDs.luminousMoss, 200},
            {ItemIDs.snowball, 200},
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
            {ItemIDs.latiosite, 20000},
            {ItemIDs.latiasite, 20000},
            {ItemIDs.roseliBerry, 1000},
            {ItemIDs.keeBerry, 1000},
            {ItemIDs.marangaBerry, 1000},
            {ItemIDs.tm98, 20000},
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
            {ItemIDs.beastBall, 300},
            {ItemIDs.protectivePads, 3000},
            {ItemIDs.electricSeed, 1000},
            {ItemIDs.psychicSeed, 1000},
            {ItemIDs.mistySeed, 1000},
            {ItemIDs.grassySeed, 1000},
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
}
