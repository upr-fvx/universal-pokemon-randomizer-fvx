package com.uprfvx.romio.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen2Constants.java - Constants for Gold/Silver/Crystal                --*/
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

import com.uprfvx.romio.gamedata.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gen2Constants {

    public static final int Type_GS = 0;
    public static final int Type_Crystal = 1;

    public static final int vietCrystalCheckOffset = 0x63;

    public static final byte vietCrystalCheckValue = (byte) 0xF5;

    public static final String vietCrystalROMName = "Pokemon VietCrystal";

    public static final int pokemonCount = 251, moveCount = 251;

    public static final int unownFormeCount = 26;

    public static final int baseStatsEntrySize = 0x20;

    public static final Type[] typeTable = constructTypeTable();

    public static final int bsHPOffset = 1, bsAttackOffset = 2, bsDefenseOffset = 3, bsSpeedOffset = 4,
            bsSpAtkOffset = 5, bsSpDefOffset = 6, bsPrimaryTypeOffset = 7, bsSecondaryTypeOffset = 8,
            bsCatchRateOffset = 9, bsCommonHeldItemOffset = 11, bsRareHeldItemOffset = 12, bsFrontImageDimensionsOffset = 17,
            bsGrowthCurveOffset = 22, bsTMHMCompatOffset = 24, bsMTCompatOffset = 31;

    public static final int fishingAreaCount = 12, pokesPerFishingArea = 11, fishingAreaEntryLength = 3,
            timeSpecificFishingAreaCount = 11, pokesPerTSFishingArea = 4;

    public static final List<Integer> crystalUnusedFishingAreas = Collections.unmodifiableList(Arrays.asList(6, 11));

    public static final String[] fishingAreaNames = new String[]{"Shore", "Ocean", "Lake", "Pond",
            "Dratini 1 (Ice Path, Dragon's Den)", "Qwilfish Swarm (Route 32)", "Remoraid Swarm (Route 44)",
            "Gyarados (Lake of Rage, Fuchsia City)", "Dratini 2 (Route 45)", "Whirl Islands",
            "Qwilfish (Routes 32, 12, 13)", "Remoraid (Route 44)"};

    public static final String[] headbuttAreaNamesGS = new String[]{
            "Headbutt (Routes 34-39, Azalea Town, Ilex Forest, Routes 26-27) (common)",
            "Headbutt (Routes 34-39, Azalea Town, Ilex Forest, Routes 26-27) (rare)",
            "Headbutt (Routes 29-33, 42-46) (common)", "Headbutt (Routes 29-33, 42-46) (rare)",
            "Rock Smash"};

    public static final String[] headbuttAreaNamesCrystal = new String[]{
            "Headbutt (Routes 44, 45, 46) (common)", "Headbutt (Routes 44, 45, 46) (rare)",
            "Headbutt (Azalea Town, Route 42) (common)", "Headbutt (Azalea Town, Route 42) (rare)",
            "Headbutt (Routes 29-31, 34-39) (common)", "Headbutt (Routes 29-31, 34-39) (rare)",
            "Headbutt (Routes 32, 26, 27) (common)", "Headbutt (Routes 32, 26, 27) (rare)",
            "Headbutt (Route 43) (common)", "Headbutt (Route 43) (rare)",
            "Headbutt (Ilex Forest) (common)", "Headbutt (Ilex Forest) (rare)",
            "Rock Smash"};

    public static final int landEncounterSlots = 7, seaEncounterSlots = 3;

    public static final int oddEggPokemonCount = 14;

    public static final int tmCount = 50, hmCount = 7;

    public static final int mtCount = 3;

    /**
     * Taken from older code about the move tutor dialogue option. Assumed to work for other dialogue options as well,
     * but this will have to be tested when implemented.
     */
    public static final byte dialogueOptionInitByte = (byte) 0x80;

    public static final int maxTrainerNameLength = 17;

    public static final byte trainerDataTerminator = (byte) 0xFF;

    public static final int fleeingSetTwoOffset = 0xE, fleeingSetThreeOffset = 0x17;

    public static final int mapGroupCount = 26, mapsInLastGroup = 11;

    public static final int noDamageSleepEffect = 1, damagePoisonEffect = 2, damageAbsorbEffect = 3, damageBurnEffect = 4,
            damageFreezeEffect = 5, damageParalyzeEffect = 6, dreamEaterEffect = 8, noDamageAtkPlusOneEffect = 10,
            noDamageDefPlusOneEffect = 11, noDamageSpAtkPlusOneEffect = 13, noDamageEvasionPlusOneEffect = 16,
            noDamageAtkMinusOneEffect = 18, noDamageDefMinusOneEffect = 19, noDamageSpeMinusOneEffect = 20,
            noDamageAccuracyMinusOneEffect = 23, noDamageEvasionMinusOneEffect = 24, flinchEffect = 31, toxicEffect = 33,
            razorWindEffect = 39, bindingEffect = 42, damageRecoilEffect = 48, noDamageConfusionEffect = 49,
            noDamageAtkPlusTwoEffect = 50, noDamageDefPlusTwoEffect = 51, noDamageSpePlusTwoEffect = 52,
            noDamageSpDefPlusTwoEffect = 54, noDamageAtkMinusTwoEffect = 58, noDamageDefMinusTwoEffect = 59,
            noDamageSpeMinusTwoEffect = 60, noDamageSpDefMinusTwoEffect = 62, noDamagePoisonEffect = 66,
            noDamageParalyzeEffect = 67, damageAtkMinusOneEffect = 68, damageDefMinusOneEffect = 69,
            damageSpeMinusOneEffect = 70, damageSpDefMinusOneEffect = 72, damageAccuracyMinusOneEffect = 73,
            skyAttackEffect = 75, damageConfusionEffect = 76, twineedleEffect = 77, hyperBeamEffect = 80,
            snoreEffect = 92, flailAndReversalEffect = 102, trappingEffect = 106, swaggerEffect = 118,
            damageBurnAndThawUserEffect = 125, damageUserDefPlusOneEffect = 138, damageUserAtkPlusOneEffect = 139,
            damageUserAllPlusOneEffect = 140, skullBashEffect = 145, twisterEffect = 146, futureSightEffect = 148,
            stompEffect = 150, solarbeamEffect = 151, thunderEffect = 152, semiInvulnerableEffect = 155,
            defenseCurlEffect = 156;

    // Taken from critical_hit_moves.asm; we could read this from the ROM, but it's easier to hardcode it.
    public static final List<Integer> increasedCritMoves = Arrays.asList(MoveIDs.karateChop, MoveIDs.razorWind, MoveIDs.razorLeaf,
            MoveIDs.crabhammer, MoveIDs.slash, MoveIDs.aeroblast, MoveIDs.crossChop);

    public static final List<Integer> requiredFieldTMs = Arrays.asList(
            ItemIDs.tm04, ItemIDs.tm20, ItemIDs.tm22, ItemIDs.tm26, ItemIDs.tm28, ItemIDs.tm34, ItemIDs.tm35,
            ItemIDs.tm39, ItemIDs.tm40, ItemIDs.tm43, ItemIDs.tm44, ItemIDs.tm46
    );

    public static final List<Integer> fieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.whirlpool, MoveIDs.waterfall, MoveIDs.rockSmash, MoveIDs.headbutt, MoveIDs.sweetScent);

    public static final List<Integer> earlyRequiredHMMoves = Collections.singletonList(MoveIDs.cut);

    // ban thief because trainers are broken with it (items are not returned).
    // ban transform because of Transform assumption glitch
    public static final List<Integer> bannedLevelupMoves = Arrays.asList(MoveIDs.transform, MoveIDs.thief);

    public static final List<Integer> brokenMoves = Arrays.asList(
            MoveIDs.sonicBoom, MoveIDs.dragonRage, MoveIDs.hornDrill, MoveIDs.fissure, MoveIDs.guillotine);

    public static final List<Integer> illegalVietCrystalMoves = Arrays.asList(
            MoveIDs.protect, MoveIDs.rest, MoveIDs.spikeCannon, MoveIDs.detect);

    public static final int priorityHitEffectIndex = 0x67, protectEffectIndex = 0x6F, endureEffectIndex = 0x74,
            forceSwitchEffectIndex = 0x1C,counterEffectIndex = 0x59, mirrorCoatEffectIndex = 0x90;

    // probably the terminator for all move-lists, like TM/HM compatibility
    public static final byte eggMovesTerminator = (byte) 0xFF;

    public static final byte farTextStart = 0x16;

    public static final byte shopItemsTerminator = (byte) 0xFF;

    public static final List<String> shopNames = Collections.unmodifiableList(Arrays.asList(
            "Cherrygrove Poké Mart (Before Pokédex)",
            "Cherrygrove Poké Mart (After Pokédex)",
            "Violet Poké Mart",
            "Azalea Poké Mart",
            "Cianwood Poké Mart",
            "Goldenrod Department Store 2F Upper",
            "Goldenrod Department Store 2F Lower",
            "Goldenrod Department Store 3F",
            "Goldenrod Department Store 4F",
            "Goldenrod Department Store 5F (neither TM02 nor TM08 unlocked)", // TODO: how does this work?
            "Goldenrod Department Store 5F (TM02 unlocked)",
            "Goldenrod Department Store 5F (TM08 unlocked)",
            "Goldenrod Department Store 5F (TM02 and TM08 unlocked)",
            "Olivine Poké Mart",
            "Ecruteak Poké Mart",
            "Mahogany Souvenir Shop (Before Team Rocket HQ)",
            "Mahogany Souvenir Shop (After Team Rocket HQ)",
            "Blackthorn Poké Mart",
            "Viridian Poké Mart",
            "Pewter Poké Mart",
            "Cerulean Poké Mart",
            "Lavender Poké Mart",
            "Vermilion Poké Mart",
            "Celadon Department Store 2F Left",
            "Celadon Department Store 2F Right",
            "Celadon Department Store 3F",
            "Celadon Department Store 4F",
            "Celadon Department Store 5F Left",
            "Celadon Department Store 5F Right",
            "Fuchsia Poké Mart",
            "Saffron Poké Mart",
            "Mt. Moon Square Shop",
            "Indigo Plateau Poké Mart",
            "Goldenrod Tunnel Herb Shop"
    ));

    public static final int[] specialShops = new int[]{7, 8, 26, 27, 28, 31, 33};

    public static final int[] mainGameShops = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 32, 33
    };

    public static final int itemCount = 256, itemAttributesEntrySize = 7;

    // Held-while-traded evo items (upgrade etc.) are not considered because players are not expected to trade. Same as in Gen3Constants.
    public static final List<Integer> evolutionItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.sunStone, ItemIDs.moonStone, ItemIDs.fireStone, ItemIDs.thunderStone, ItemIDs.waterStone,
            ItemIDs.leafStone
    ));

    public static final List<Integer> generalPurposeConsumableItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.pechaBerry, ItemIDs.cheriBerry, ItemIDs.aspearBerry, ItemIDs.rawstBerry, ItemIDs.persimBerry,
            ItemIDs.chestoBerry, ItemIDs.lumBerry, ItemIDs.leppaBerry, ItemIDs.oranBerry, ItemIDs.sitrusBerry,
            ItemIDs.berryJuice
    ));

    public static final List<Integer> consumableHeldItems = setupConsumableHeldItems();

    private static List<Integer> setupConsumableHeldItems() {
        List<Integer> consumableHeldItems = new ArrayList<>(generalPurposeConsumableItems);
        consumableHeldItems.add(ItemIDs.Gen2.berserkGene);
        return Collections.unmodifiableList(consumableHeldItems);
    }

    public static final List<Integer> generalPurposeItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.brightPowder, ItemIDs.quickClaw, ItemIDs.kingsRock, ItemIDs.smokeBall
    ));

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        List<Integer> allHeldItems = new ArrayList<>(generalPurposeItems);
        allHeldItems.addAll(Collections.unmodifiableList(Arrays.asList(
                // type-boosting items
                ItemIDs.blackBelt, ItemIDs.blackGlasses, ItemIDs.charcoal, ItemIDs.dragonScale,
                ItemIDs.hardStone, ItemIDs.magnet, ItemIDs.metalCoat, ItemIDs.miracleSeed,
                ItemIDs.mysticWater, ItemIDs.neverMeltIce, ItemIDs.Gen2.pinkBow, ItemIDs.Gen2.polkadotBow,
                ItemIDs.sharpBeak, ItemIDs.silverPowder, ItemIDs.softSand, ItemIDs.spellTag,
                ItemIDs.twistedSpoon)));
        allHeldItems.addAll(consumableHeldItems);
        return Collections.unmodifiableList(allHeldItems);
    }

    public static final Map<Type, List<Integer>> typeBoostingItems = initializeTypeBoostingItems();

    private static Map<Type, List<Integer>> initializeTypeBoostingItems() {
        Map<Type, List<Integer>> map = new HashMap<>();
        map.put(Type.BUG, Collections.singletonList(ItemIDs.silverPowder));
        map.put(Type.DARK, Collections.singletonList(ItemIDs.blackGlasses));
        map.put(Type.DRAGON, Collections.singletonList(ItemIDs.dragonScale)); // NOT Dragon Fang due to a bug in the game's code
        map.put(Type.ELECTRIC, Collections.singletonList(ItemIDs.magnet));
        map.put(Type.FIGHTING, Collections.singletonList(ItemIDs.blackBelt));
        map.put(Type.FIRE, Collections.singletonList(ItemIDs.charcoal));
        map.put(Type.FLYING, Collections.singletonList(ItemIDs.sharpBeak));
        map.put(Type.GHOST, Collections.singletonList(ItemIDs.spellTag));
        map.put(Type.GRASS, Collections.singletonList(ItemIDs.miracleSeed));
        map.put(Type.GROUND, Collections.singletonList(ItemIDs.softSand));
        map.put(Type.ICE, Collections.singletonList(ItemIDs.neverMeltIce));
        map.put(Type.NORMAL, Arrays.asList(ItemIDs.Gen2.pinkBow, ItemIDs.Gen2.polkadotBow));
        map.put(Type.POISON, Collections.singletonList(ItemIDs.poisonBarb));
        map.put(Type.PSYCHIC, Collections.singletonList(ItemIDs.twistedSpoon));
        map.put(Type.ROCK, Collections.singletonList(ItemIDs.hardStone));
        map.put(Type.STEEL, Collections.singletonList(ItemIDs.metalCoat));
        map.put(Type.WATER, Collections.singletonList(ItemIDs.mysticWater));
        map.put(null, Collections.emptyList()); // ??? type
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> speciesBoostingItems = initializeSpeciesBoostingItems();

    private static Map<Integer, List<Integer>> initializeSpeciesBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(SpeciesIDs.pikachu, Collections.singletonList(ItemIDs.lightBall));
        map.put(SpeciesIDs.chansey, Collections.singletonList(ItemIDs.luckyPunch));
        map.put(SpeciesIDs.ditto, Collections.singletonList(ItemIDs.metalPowder));
        map.put(SpeciesIDs.cubone, Collections.singletonList(ItemIDs.thickClub));
        map.put(SpeciesIDs.marowak, Collections.singletonList(ItemIDs.thickClub));
        map.put(SpeciesIDs.farfetchd, Collections.singletonList(ItemIDs.leek));
        return Collections.unmodifiableMap(map);
    }

    // rare candy, lucky egg, and all the "valuable items"
    public static final List<Integer> opShopItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.rareCandy, ItemIDs.luckyEgg,
            ItemIDs.nugget, ItemIDs.tinyMushroom, ItemIDs.bigMushroom, ItemIDs.pearl, ItemIDs.bigPearl,
            ItemIDs.stardust, ItemIDs.stardust, ItemIDs.Gen2.brickPiece, ItemIDs.Gen2.silverLeaf, ItemIDs.Gen2.goldLeaf
    ));

    public static final Set<Integer> bannedItems = setupBannedItems();
    public static final Set<Integer> badItems = setupBadItems();
    public static final Set<Integer> tmItems = setupTMItems();
    // In VietCrystal only, these items crash your game if used, glitch out your inventory if carried
    public static final List<Integer> vietCrystalBannedItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.burnHeal, ItemIDs.calcium, ItemIDs.elixir, ItemIDs.twistedSpoon
    ));

    private static Set<Integer> setupBannedItems() {
        Set<Integer> set = new HashSet<>();
        // Most of the Gen 2 unique items are either key items or unused.
        addBetween(set, ItemIDs.Gen2.first, ItemIDs.Gen2.last);
        Arrays.asList(ItemIDs.Gen2.silverLeaf, ItemIDs.Gen2.goldLeaf, ItemIDs.Gen2.brickPiece,
                ItemIDs.Gen2.berserkGene, ItemIDs.Gen2.pinkBow, ItemIDs.Gen2.polkadotBow,
                ItemIDs.Gen2.normalBox, ItemIDs.Gen2.gorgeousBox).forEach(set::remove);
        // HMs
        addBetween(set, ItemIDs.hm01, ItemIDs.hm07);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItems() {
        Set<Integer> set = new HashSet<>(Arrays.asList(ItemIDs.lightBall, ItemIDs.oranBerry,
                ItemIDs.Gen2.silverLeaf, ItemIDs.Gen2.goldLeaf, ItemIDs.Gen2.brickPiece,
                ItemIDs.Gen2.normalBox, ItemIDs.Gen2.gorgeousBox));
        addBetween(set, ItemIDs.luckyPunch, ItemIDs.leek);
        addBetween(set, ItemIDs.redApricorn, ItemIDs.blackApricorn);
        addBetween(set, ItemIDs.mail1, ItemIDs.mail10);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupTMItems() {
        Set<Integer> set = new HashSet<>();
        addBetween(set, ItemIDs.tm01, ItemIDs.tm50);
        return set;
    }

    /**
     * Adds the Integers to the set, from start to end, inclusive.
     */
    private static void addBetween(Set<Integer> set, int start, int end) {
        for (int i = start; i <= end; i++) {
            set.add(i);
        }
    }

    public static final String friendshipValueForEvoLocator = "FEDCDA";

    public static String getName(PlayerCharacterType playerCharacter) {
        if (playerCharacter == PlayerCharacterType.PC1) {
            return "Chris";
        } else if (playerCharacter == PlayerCharacterType.PC2) {
            return "Kris";
        } else {
            throw new IllegalArgumentException("Invalid enum. Gen 2 only has two playable characters, Chris and Kris.");
        }
    }

    // directly after chris's respective images
    public static final int krisTrainerCardImageOffset = 16 * 5 * 7, krisFrontImageOffset = 16 * 7 * 7;

    public static final int krisPalettePointerOffset = -4;

    public static final int krisSpritePaletteOffset = 16;

    public static final int chrisBackBankOffsetGS0 = 16, dudeBackPointerOffset = 10, // battle script GS
                            chrisBackBankOffsetGS1 = 6, // hall of fame script GS
                            chrisBackBankOffsetCrystal0 = -2, // battle script Crystal
                            chrisBackBankOffsetCrystal1 = 3; // hall of fame script Crystal

    public static final int chrisTrainerClassGS = 12, chrisPaletteTrainerClassGS = 0, chrisTrainerClassCrystal = 0,
            falknerTrainerClass = 1;

    public static final int tcPal1Offset = 0xB, tcPal5Offset = 0x2B,
                            tcFalknerPalOffset = 0x7C, tcChuckPalOffset = 0xA8, tcClairePalOffset = 0xD0;

    private static Type[] constructTypeTable() {
        Type[] table = new Type[256];
        table[0x00] = Type.NORMAL;
        table[0x01] = Type.FIGHTING;
        table[0x02] = Type.FLYING;
        table[0x03] = Type.POISON;
        table[0x04] = Type.GROUND;
        table[0x05] = Type.ROCK;
        table[0x07] = Type.BUG;
        table[0x08] = Type.GHOST;
        table[0x09] = Type.STEEL;
        table[0x14] = Type.FIRE;
        table[0x15] = Type.WATER;
        table[0x16] = Type.GRASS;
        table[0x17] = Type.ELECTRIC;
        table[0x18] = Type.PSYCHIC;
        table[0x19] = Type.ICE;
        table[0x1A] = Type.DRAGON;
        table[0x1B] = Type.DARK;
        return table;
    }

    public static byte typeToByte(Type type) {
        if (type == null) {
            return 0x13; // ???-type
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
            return 0x07;
        case GHOST:
            return 0x08;
        case FIRE:
            return 0x14;
        case WATER:
            return 0x15;
        case GRASS:
            return 0x16;
        case ELECTRIC:
            return 0x17;
        case PSYCHIC:
            return 0x18;
        case ICE:
            return 0x19;
        case DRAGON:
            return 0x1A;
        case STEEL:
            return 0x09;
        case DARK:
            return 0x1B;
        default:
            return 0; // normal by default
        }
    }

    public static final int nonNeutralEffectivenessCount = 110;

    public static int evolutionTypeToIndex(EvolutionType evolutionType) {
        switch (evolutionType) {
            case LEVEL:
                return 1;
            case STONE:
                return 2;
            case TRADE:
            case TRADE_ITEM:
                return 3;
            case HAPPINESS:
            case HAPPINESS_DAY:
            case HAPPINESS_NIGHT:
                return 4;
            case LEVEL_ATTACK_HIGHER:
            case LEVEL_DEFENSE_HIGHER:
            case LEVEL_ATK_DEF_SAME:
                return 5;
            default:
                return -1;
        }
    }

    public static EvolutionType evolutionTypeFromIndex(int index) {
        switch (index) {
            case 1:
                return EvolutionType.LEVEL;
            case 2:
                return EvolutionType.STONE;
            case 3:
                return EvolutionType.TRADE;
            case 4:
                return EvolutionType.HAPPINESS;
            case 5:
                return EvolutionType.LEVEL_ATTACK_HIGHER;
            default:
                return EvolutionType.NONE;
        }
    }

    public static void universalTrainerTags(List<Trainer> allTrainers) {
        // Gym Leaders
        tbc(allTrainers, 1, 0, "GYM1-LEADER");
        tbc(allTrainers, 3, 0, "GYM2-LEADER");
        tbc(allTrainers, 2, 0, "GYM3-LEADER");
        tbc(allTrainers, 4, 0, "GYM4-LEADER");
        tbc(allTrainers, 7, 0, "GYM5-LEADER");
        tbc(allTrainers, 6, 0, "GYM6-LEADER");
        tbc(allTrainers, 5, 0, "GYM7-LEADER");
        tbc(allTrainers, 8, 0, "GYM8-LEADER");
        tbc(allTrainers, 17, 0, "GYM9-LEADER");
        tbc(allTrainers, 18, 0, "GYM10-LEADER");
        tbc(allTrainers, 19, 0, "GYM11-LEADER");
        tbc(allTrainers, 21, 0, "GYM12-LEADER");
        tbc(allTrainers, 26, 0, "GYM13-LEADER");
        tbc(allTrainers, 35, 0, "GYM14-LEADER");
        tbc(allTrainers, 46, 0, "GYM15-LEADER");
        tbc(allTrainers, 64, 0, "GYM16-LEADER");

        // Elite 4 & Red
        tbc(allTrainers, 11, 0, "ELITE1");
        tbc(allTrainers, 15, 0, "ELITE2");
        tbc(allTrainers, 13, 0, "ELITE3");
        tbc(allTrainers, 14, 0, "ELITE4");
        tbc(allTrainers, 16, 0, "CHAMPION");
        tbc(allTrainers, 63, 0, "UBER");

        // Silver
        // Order in rom is BAYLEEF, QUILAVA, CROCONAW teams
        // Starters go CYNDA, TOTO, CHIKO
        // So we want 0=CROCONAW/FERALI, 1=BAYLEEF/MEGAN, 2=QUILAVA/TYPHLO
        tbc(allTrainers, 9, 0, "RIVAL1-1");
        tbc(allTrainers, 9, 1, "RIVAL1-2");
        tbc(allTrainers, 9, 2, "RIVAL1-0");

        tbc(allTrainers, 9, 3, "RIVAL2-1");
        tbc(allTrainers, 9, 4, "RIVAL2-2");
        tbc(allTrainers, 9, 5, "RIVAL2-0");

        tbc(allTrainers, 9, 6, "RIVAL3-1");
        tbc(allTrainers, 9, 7, "RIVAL3-2");
        tbc(allTrainers, 9, 8, "RIVAL3-0");

        tbc(allTrainers, 9, 9, "RIVAL4-1");
        tbc(allTrainers, 9, 10, "RIVAL4-2");
        tbc(allTrainers, 9, 11, "RIVAL4-0");

        tbc(allTrainers, 9, 12, "RIVAL5-1");
        tbc(allTrainers, 9, 13, "RIVAL5-2");
        tbc(allTrainers, 9, 14, "RIVAL5-0");

        tbc(allTrainers, 42, 0, "RIVAL6-1");
        tbc(allTrainers, 42, 1, "RIVAL6-2");
        tbc(allTrainers, 42, 2, "RIVAL6-0");

        tbc(allTrainers, 42, 3, "RIVAL7-1");
        tbc(allTrainers, 42, 4, "RIVAL7-2");
        tbc(allTrainers, 42, 5, "RIVAL7-0");

        // Female Rocket Executive (Ariana)
        tbc(allTrainers, 55, 0, "THEMED:ARIANA");
        tbc(allTrainers, 55, 1, "THEMED:ARIANA");

        // others (unlabeled in this game, using HGSS names)
        tbc(allTrainers, 51, 2, "THEMED:PETREL");
        tbc(allTrainers, 51, 3, "THEMED:PETREL");

        tbc(allTrainers, 51, 1, "THEMED:PROTON");
        tbc(allTrainers, 31, 0, "THEMED:PROTON");

        // Sprout Tower
        tbc(allTrainers, 56, 0, "THEMED:SPROUTTOWER");
        tbc(allTrainers, 56, 1, "THEMED:SPROUTTOWER");
        tbc(allTrainers, 56, 2, "THEMED:SPROUTTOWER");
        tbc(allTrainers, 56, 3, "THEMED:SPROUTTOWER");
        tbc(allTrainers, 56, 6, "THEMED:SPROUTTOWER");
        tbc(allTrainers, 56, 7, "THEMED:SPROUTTOWER");
        tbc(allTrainers, 56, 8, "THEMED:SPROUTTOWER");
    }

    public static void goldSilverTags(List<Trainer> allTrainers) {
        tbc(allTrainers, 24, 0, "GYM1");
        tbc(allTrainers, 24, 1, "GYM1");
        tbc(allTrainers, 36, 4, "GYM2");
        tbc(allTrainers, 36, 5, "GYM2");
        tbc(allTrainers, 36, 6, "GYM2");
        tbc(allTrainers, 61, 0, "GYM2");
        tbc(allTrainers, 61, 3, "GYM2");
        tbc(allTrainers, 25, 0, "GYM3");
        tbc(allTrainers, 25, 1, "GYM3");
        tbc(allTrainers, 29, 0, "GYM3");
        tbc(allTrainers, 29, 1, "GYM3");
        tbc(allTrainers, 56, 4, "GYM4");
        tbc(allTrainers, 56, 5, "GYM4");
        tbc(allTrainers, 57, 0, "GYM4");
        tbc(allTrainers, 57, 1, "GYM4");
        tbc(allTrainers, 50, 1, "GYM5");
        tbc(allTrainers, 50, 3, "GYM5");
        tbc(allTrainers, 50, 4, "GYM5");
        tbc(allTrainers, 50, 6, "GYM5");
        tbc(allTrainers, 58, 0, "GYM7");
        tbc(allTrainers, 58, 1, "GYM7");
        tbc(allTrainers, 58, 2, "GYM7");
        tbc(allTrainers, 33, 0, "GYM7");
        tbc(allTrainers, 33, 1, "GYM7");
        tbc(allTrainers, 27, 2, "GYM8");
        tbc(allTrainers, 27, 4, "GYM8");
        tbc(allTrainers, 27, 3, "GYM8");
        tbc(allTrainers, 28, 2, "GYM8");
        tbc(allTrainers, 28, 3, "GYM8");
        tbc(allTrainers, 54, 17, "GYM9");
        tbc(allTrainers, 38, 20, "GYM10");
        tbc(allTrainers, 39, 17, "GYM10");
        tbc(allTrainers, 39, 18, "GYM10");
        tbc(allTrainers, 49, 2, "GYM11");
        tbc(allTrainers, 43, 1, "GYM11");
        tbc(allTrainers, 32, 2, "GYM11");
        tbc(allTrainers, 61, 4, "GYM12");
        tbc(allTrainers, 61, 5, "GYM12");
        tbc(allTrainers, 25, 8, "GYM12");
        tbc(allTrainers, 53, 18, "GYM12");
        tbc(allTrainers, 29, 13, "GYM12");
        tbc(allTrainers, 25, 2, "GYM13");
        tbc(allTrainers, 25, 5, "GYM13");
        tbc(allTrainers, 53, 4, "GYM13");
        tbc(allTrainers, 54, 4, "GYM13");
        tbc(allTrainers, 57, 5, "GYM14");
        tbc(allTrainers, 57, 6, "GYM14");
        tbc(allTrainers, 52, 1, "GYM14");
        tbc(allTrainers, 52, 10, "GYM14");
    }

    public static void crystalTags(List<Trainer> allTrainers) {
        tbc(allTrainers, 24, 0, "GYM1");
        tbc(allTrainers, 24, 1, "GYM1");
        tbc(allTrainers, 36, 4, "GYM2");
        tbc(allTrainers, 36, 5, "GYM2");
        tbc(allTrainers, 36, 6, "GYM2");
        tbc(allTrainers, 61, 0, "GYM2");
        tbc(allTrainers, 61, 3, "GYM2");
        tbc(allTrainers, 25, 0, "GYM3");
        tbc(allTrainers, 25, 1, "GYM3");
        tbc(allTrainers, 29, 0, "GYM3");
        tbc(allTrainers, 29, 1, "GYM3");
        tbc(allTrainers, 56, 4, "GYM4");
        tbc(allTrainers, 56, 5, "GYM4");
        tbc(allTrainers, 57, 0, "GYM4");
        tbc(allTrainers, 57, 1, "GYM4");
        tbc(allTrainers, 50, 1, "GYM5");
        tbc(allTrainers, 50, 3, "GYM5");
        tbc(allTrainers, 50, 4, "GYM5");
        tbc(allTrainers, 50, 6, "GYM5");
        tbc(allTrainers, 58, 0, "GYM7");
        tbc(allTrainers, 58, 1, "GYM7");
        tbc(allTrainers, 58, 2, "GYM7");
        tbc(allTrainers, 33, 0, "GYM7");
        tbc(allTrainers, 33, 1, "GYM7");
        tbc(allTrainers, 27, 2, "GYM8");
        tbc(allTrainers, 27, 4, "GYM8");
        tbc(allTrainers, 27, 3, "GYM8");
        tbc(allTrainers, 28, 2, "GYM8");
        tbc(allTrainers, 28, 3, "GYM8");
        tbc(allTrainers, 54, 17, "GYM9");
        tbc(allTrainers, 38, 20, "GYM10");
        tbc(allTrainers, 39, 17, "GYM10");
        tbc(allTrainers, 39, 18, "GYM10");
        tbc(allTrainers, 49, 2, "GYM11");
        tbc(allTrainers, 43, 1, "GYM11");
        tbc(allTrainers, 32, 2, "GYM11");
        tbc(allTrainers, 61, 4, "GYM12");
        tbc(allTrainers, 61, 5, "GYM12");
        tbc(allTrainers, 25, 8, "GYM12");
        tbc(allTrainers, 53, 18, "GYM12");
        tbc(allTrainers, 29, 13, "GYM12");
        tbc(allTrainers, 25, 2, "GYM13");
        tbc(allTrainers, 25, 5, "GYM13");
        tbc(allTrainers, 53, 4, "GYM13");
        tbc(allTrainers, 54, 4, "GYM13");
        tbc(allTrainers, 57, 5, "GYM14");
        tbc(allTrainers, 57, 6, "GYM14");
        tbc(allTrainers, 52, 1, "GYM14");
        tbc(allTrainers, 52, 10, "GYM14");
    }

    private static void tbc(List<Trainer> allTrainers, int classNum, int number, String tag) {
        int currnum = -1;
        for (Trainer t : allTrainers) {
            // adjusted to not change the above but use 0-indexing properly
            if (t.getTrainerclass() == classNum - 1) {
                currnum++;
                if (currnum == number) {
                    t.setTag(tag);
                    return;
                }
            }
        }
    }

    /**
     * The order the player is "expected" to traverse locations. Based on
     * <a href=https://strategywiki.org/wiki/Pok%C3%A9mon_Gold_and_Silver/Walkthrough>this walkthrough</a>.
     */
    public static final List<String> locationTagsTraverseOrder = Collections.unmodifiableList(Arrays.asList(
            "NEW BARK TOWN", "ROUTE 29", "ROUTE 46", "CHERRYGROVE CITY", "ROUTE 30", "ROUTE 31", "DARK CAVE",
            "VIOLET CITY", "SPROUT TOWER", "ROUTE 32", "RUINS OF ALPH", "UNION CAVE", "ROUTE 33",
            "SLOWPOKE WELL", "ILEX FOREST", "ROUTE 34", "GOLDENROD CITY", "ROUTE 35", "NATIONAL PARK",
            "ROUTE 36", "ROUTE 37", "ECRUTEAK CITY", "BURNED TOWER", "ROUTE 38", "ROUTE 39", "OLIVINE CITY",
            "ROUTE 40", "ROUTE 41", "CIANWOOD CITY", "ROUTE 42", "MT.MORTAR", "ROUTE 43", "LAKE OF RAGE",
            "ROUTE 44", "ICE PATH", "BLACKTHORN CITY", "DRAGON'S DEN", "ROUTE 45", "WHIRL ISLANDS",
            "TIN TOWER", "ROUTE 27", "TOHJO FALLS", "ROUTE 26", "VICTORY ROAD",
            "VERMILION CITY", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROCK TUNNEL", "ROUTE 10", "ROUTE 9",
            "CERULEAN CITY", "ROUTE 24", "ROUTE 25", "ROUTE 5", "CELADON CITY", "ROUTE 16", "ROUTE 17",
            "ROUTE 18", "FUCHSIA CITY", "ROUTE 15", "ROUTE 14", "ROUTE 13", "ROUTE 12", "ROUTE 11",
            "DIGLETT'S CAVE", "ROUTE 2", "ROUTE 3", "MT.MOON", "ROUTE 4", "VIRIDIAN CITY", "ROUTE 1", "PALLET TOWN",
            "ROUTE 21", "CINNABAR ISLAND", "ROUTE 20", "ROUTE 19", "ROUTE 22", "ROUTE 28", "SILVER CAVE",
            "FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND",
            "FISHING QWILFISH", "FISHING REMORAID",
            "FISHING GYARADOS", "FISHING DRATINI 1", "FISHING DRATINI 2", "FISHING WHIRL ISLANDS",
            "HEADBUTT FOREST GS", "HEADBUTT CANYON GS",
            "HEADBUTT ROUTE", "HEADBUTT TOWN", "HEADBUTT KANTO", "HEADBUTT FOREST C", "HEADBUTT LAKE",
            "HEADBUTT CANYON C",
            "ROCK SMASH", "BUG CATCHING CONTEST"
    ));

    public static final Map<Integer, Integer> balancedItemPrices = Stream.of(new Integer[][]{

            // general held items
            {ItemIDs.brightPowder, 3000}, // same as in Gen3Constants
            {ItemIDs.expShare, 6000}, // same as in Gen3Constants
            {ItemIDs.quickClaw, 4500}, // sane as in Gen3Constants
            {ItemIDs.kingsRock, 5000}, // same as in Gen3Constants
            {ItemIDs.amuletCoin, 15000}, // same as in Gen3Constants
            {ItemIDs.smokeBall, 1200}, // vanilla value of 200 felt too low
            {ItemIDs.everstone, 200}, // same as in Gen3Constants
            {ItemIDs.focusBand, 3000}, // same as in Gen3Constants
            {ItemIDs.leftovers, 10000}, // same as in Gen3Constants
            {ItemIDs.cleanseTag, 1000}, // same as in Gen3Constants
            {ItemIDs.luckyEgg, 10000}, // same as in Gen3Constants
            {ItemIDs.scopeLens, 5000}, // same as in Gen3Constants
            {ItemIDs.Gen2.berserkGene, 800}, // probably not a very good item

            // type boosting items
            {ItemIDs.softSand, 2000}, // same as in Gen3Constants
            {ItemIDs.sharpBeak, 2000}, // same as in Gen3Constants
            {ItemIDs.poisonBarb, 2000}, // same as in Gen3Constants
            {ItemIDs.silverPowder, 2000}, // same as in Gen3Constants
            {ItemIDs.mysticWater, 2000}, // same as in Gen3Constants
            {ItemIDs.twistedSpoon, 2000}, // same as in Gen3Constants
            {ItemIDs.blackBelt, 2000}, // same as in Gen3Constants
            {ItemIDs.blackGlasses, 2000}, // same as in Gen3Constants
            {ItemIDs.Gen2.pinkBow, 2000}, // same as other type-boosting items
            {ItemIDs.Gen2.polkadotBow, 2000}, // same as other type-boosting items
            {ItemIDs.neverMeltIce, 2000}, // same as in Gen3Constants
            {ItemIDs.magnet, 2000}, // same as in Gen3Constants
            {ItemIDs.spellTag, 2000}, // same as in Gen3Constants
            {ItemIDs.miracleSeed, 2000}, // same as in Gen3Constants
            {ItemIDs.hardStone, 2000}, // same as in Gen3Constants
            {ItemIDs.charcoal, 2000}, // same as other type-boosting items; the vanilla cost is way too high
            {ItemIDs.metalCoat, 2000}, // same as other type-boosting items
            {ItemIDs.dragonScale, 2000}, // same as in Gen3Constants

            // specific poke boosting items
            {ItemIDs.luckyPunch, 1200}, // vanilla value of 10 felt too low
            {ItemIDs.metalPowder, 1200}, // vanilla value of 10 felt too low
            {ItemIDs.leek, 1200}, // vanilla value of 200 felt too low
            {ItemIDs.thickClub, 2300}, // vanilla value of 500 felt too low
            {ItemIDs.lightBall, 2300}, // vanilla value of 100 felt too low

            // berries
            {ItemIDs.oranBerry, 50}, // same as in Gen3Constants
            {ItemIDs.sitrusBerry, 500}, // same as in Gen3Constants
            {ItemIDs.pechaBerry, 100}, // same as in Gen3Constants
            {ItemIDs.cheriBerry, 200}, // same as in Gen3Constants
            {ItemIDs.aspearBerry, 250}, // same as in Gen3Constants
            {ItemIDs.rawstBerry, 250}, // same as in Gen3Constants
            {ItemIDs.persimBerry, 200}, // same as in Gen3Constants
            {ItemIDs.chestoBerry, 250}, // same as in Gen3Constants
            {ItemIDs.lumBerry, 500}, // same as in Gen3Constants
            {ItemIDs.leppaBerry, 3000}, // same as in Gen3Constants
            {ItemIDs.berryJuice, 300}, // same as potion (which also heals 20 HP)

            // poke balls
            {ItemIDs.masterBall, 3000},
            {ItemIDs.parkBall, 600}, // same as Great Ball
            // all the Apricorn balls are worth 300, same as in Gen4Constants
            {ItemIDs.heavyBall, 300},
            {ItemIDs.levelBall, 300},
            {ItemIDs.lureBall, 300},
            {ItemIDs.fastBall, 300},
            {ItemIDs.friendBall, 300},
            {ItemIDs.moonBall, 300},
            {ItemIDs.loveBall, 300},

            // pp related
            {ItemIDs.ether, 3000}, // same as in Gen3Constants
            {ItemIDs.maxEther, 4500}, // same as in Gen3Constants
            {ItemIDs.elixir, 15000}, // same as in Gen3Constants
            {ItemIDs.maxElixir, 18000}, // same as in Gen3Constants

            // misc
            {ItemIDs.moonStone, 2100}, // same as other stones
            {ItemIDs.rareCandy, 10000}, // same as in Gen3Constants
            {ItemIDs.sacredAsh, 10000}, // same as in Gen3Constants
            {ItemIDs.dragonFang, 100}, // it does nothing in Gen2 due to a bug
            {ItemIDs.Gen2.normalBox, 1000}, // arbitrary. these boxes should be unobtainable, but I'm not sure
            {ItemIDs.Gen2.gorgeousBox, 1000}
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

    public static final HashMap<String, Type> gymAndEliteThemes = setupGymAndEliteThemes();

    private static HashMap<String, Type> setupGymAndEliteThemes() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("CHAMPION", Type.DRAGON); //Lance
        themeMap.put("ELITE1", Type.PSYCHIC); //Will
        themeMap.put("ELITE2", Type.POISON); //Koga
        themeMap.put("ELITE3", Type.FIGHTING); //Bruno
        themeMap.put("ELITE4", Type.DARK); //Karen
        themeMap.put("GYM1", Type.FLYING); //Falkner
        themeMap.put("GYM2", Type.BUG); //Bugsy
        themeMap.put("GYM3", Type.NORMAL); //Whitney
        themeMap.put("GYM4", Type.GHOST); //Morty
        themeMap.put("GYM5", Type.FIGHTING); //Chuck
        themeMap.put("GYM6", Type.STEEL); //Jasmine
        themeMap.put("GYM7", Type.ICE); //Pryce
        themeMap.put("GYM8", Type.DRAGON); //Clair
        themeMap.put("GYM9", Type.ROCK); //Brock
        themeMap.put("GYM10", Type.WATER); //Misty
        themeMap.put("GYM11", Type.ELECTRIC); //Lt. Surge
        themeMap.put("GYM12", Type.GRASS); //Erika
        themeMap.put("GYM13", Type.POISON); //Janine
        themeMap.put("GYM14", Type.PSYCHIC); //Sabrina
        themeMap.put("GYM15", Type.FIRE); //Blaine
        //Blue has no specialty and thus is not included.
        return themeMap;
    }

    private static final Map<Integer, Integer> itemIDToStandardMap = Stream.of(new Integer[][]{
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
            {53, ItemIDs.xSpAtk}, // X Special
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
            {146, ItemIDs.leftovers},
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

    private static final Map<Integer, Integer> itemIDToInternalMap = itemIDToStandardMap.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static int itemIDToStandard(int id) {
        Integer standard = itemIDToStandardMap.get(id);
        if (standard == null) {
            standard = ItemIDs.UNIQUE_OFFSET + id;
        }
        return standard;
    }

    public static int itemIDToInternal(int id) {
        if (id >= ItemIDs.UNIQUE_OFFSET) {
            return id - ItemIDs.UNIQUE_OFFSET;
        }
        return itemIDToInternalMap.get(id);
    }

}
