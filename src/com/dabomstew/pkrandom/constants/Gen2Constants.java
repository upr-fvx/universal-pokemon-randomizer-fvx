package com.dabomstew.pkrandom.constants;

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

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.*;

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

    public static final List<Integer> requiredFieldTMs = Arrays.asList(4, 20, 22, 26, 28, 34, 35, 39,
            40, 43, 44, 46);

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

    public static final int tmBlockOneIndex = Gen2ItemIDs.tm01, tmBlockOneSize = 4,
            tmBlockTwoIndex = Gen2ItemIDs.tm05, tmBlockTwoSize = 24,
            tmBlockThreeIndex = Gen2ItemIDs.tm29, tmBlockThreeSize = 22;

    public static final int priorityHitEffectIndex = 0x67, protectEffectIndex = 0x6F, endureEffectIndex = 0x74,
            forceSwitchEffectIndex = 0x1C,counterEffectIndex = 0x59, mirrorCoatEffectIndex = 0x90;

    // probably the terminator for all move-lists, like TM/HM compatibility
    public static final byte eggMovesTerminator = (byte) 0xFF;

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

    // i.e. normal pokemarts + TM shops + shops that must be skipped for other reasons
    public static final List<Integer> skipShops = Collections.unmodifiableList(Arrays.asList(
            0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 29, 30, 32)
    );

    public static final List<Integer> mainGameShops = Collections.unmodifiableList(Arrays.asList(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 32, 33
    ));

    public static final int itemCount = 256, itemAttributesEntrySize = 7;

    // Held-while-traded evo items (upgrade etc.) are not considered because players are not expected to trade. Same as in Gen3Constants.
    public static final List<Integer> evolutionItems = Collections.unmodifiableList(Arrays.asList(
            Gen2ItemIDs.sunStone, Gen2ItemIDs.moonStone, Gen2ItemIDs.fireStone, Gen2ItemIDs.thunderstone, Gen2ItemIDs.waterStone,
            Gen2ItemIDs.leafStone
    ));

    public static final List<Integer> xItems = Collections.unmodifiableList(Arrays.asList(
            Gen2ItemIDs.guardSpec, Gen2ItemIDs.direHit, Gen2ItemIDs.xAttack,
            Gen2ItemIDs.xDefend, Gen2ItemIDs.xSpeed, Gen2ItemIDs.xAccuracy, Gen2ItemIDs.xSpecial
    ));

    public static final List<Integer> generalPurposeConsumableItems = Collections.unmodifiableList(Arrays.asList(
            Gen2ItemIDs.psnCureBerry, Gen2ItemIDs.przCureBerry, Gen2ItemIDs.burntBerry, Gen2ItemIDs.iceBerry, Gen2ItemIDs.bitterBerry,
            Gen2ItemIDs.mintBerry, Gen2ItemIDs.miracleBerry, Gen2ItemIDs.mysteryBerry, Gen2ItemIDs.berry, Gen2ItemIDs.goldBerry,
            Gen2ItemIDs.berryJuice
    ));

    public static final List<Integer> consumableHeldItems = setupConsumableHeldItems();

    private static List<Integer> setupConsumableHeldItems() {
        List<Integer> consumableHeldItems = new ArrayList<>(generalPurposeConsumableItems);
        consumableHeldItems.add(Gen2ItemIDs.berserkGene);
        return Collections.unmodifiableList(consumableHeldItems);
    }

    public static final List<Integer> generalPurposeItems = Collections.unmodifiableList(Arrays.asList(
            Gen2ItemIDs.brightPowder, Gen2ItemIDs.quickClaw, Gen2ItemIDs.kingsRock, Gen2ItemIDs.smokeBall
    ));

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        List<Integer> allHeldItems = new ArrayList<>(generalPurposeItems);
        allHeldItems.addAll(Collections.unmodifiableList(Arrays.asList(
                // type-boosting items
                Gen2ItemIDs.blackbelt, Gen2ItemIDs.blackGlasses, Gen2ItemIDs.charcoal, Gen2ItemIDs.dragonScale,
                Gen2ItemIDs.hardStone, Gen2ItemIDs.magnet, Gen2ItemIDs.metalCoat, Gen2ItemIDs.miracleSeed,
                Gen2ItemIDs.mysticWater, Gen2ItemIDs.neverMeltIce, Gen2ItemIDs.pinkBow, Gen2ItemIDs.polkadotBow,
                Gen2ItemIDs.sharpBeak, Gen2ItemIDs.silverPowder, Gen2ItemIDs.softSand, Gen2ItemIDs.spellTag,
                Gen2ItemIDs.twistedSpoon)));
        allHeldItems.addAll(consumableHeldItems);
        return Collections.unmodifiableList(allHeldItems);
    }

    public static final Map<Type, List<Integer>> typeBoostingItems = initializeTypeBoostingItems();

    private static Map<Type, List<Integer>> initializeTypeBoostingItems() {
        Map<Type, List<Integer>> map = new HashMap<>();
        map.put(Type.BUG, Collections.singletonList(Gen2ItemIDs.silverPowder));
        map.put(Type.DARK, Collections.singletonList(Gen2ItemIDs.blackGlasses));
        map.put(Type.DRAGON, Collections.singletonList(Gen2ItemIDs.dragonScale)); // NOT Dragon Fang due to a bug in the game's code
        map.put(Type.ELECTRIC, Collections.singletonList(Gen2ItemIDs.magnet));
        map.put(Type.FIGHTING, Collections.singletonList(Gen2ItemIDs.blackbelt));
        map.put(Type.FIRE, Collections.singletonList(Gen2ItemIDs.charcoal));
        map.put(Type.FLYING, Collections.singletonList(Gen2ItemIDs.sharpBeak));
        map.put(Type.GHOST, Collections.singletonList(Gen2ItemIDs.spellTag));
        map.put(Type.GRASS, Collections.singletonList(Gen2ItemIDs.miracleSeed));
        map.put(Type.GROUND, Collections.singletonList(Gen2ItemIDs.softSand));
        map.put(Type.ICE, Collections.singletonList(Gen2ItemIDs.neverMeltIce));
        map.put(Type.NORMAL, Arrays.asList(Gen2ItemIDs.pinkBow, Gen2ItemIDs.polkadotBow));
        map.put(Type.POISON, Collections.singletonList(Gen2ItemIDs.poisonBarb));
        map.put(Type.PSYCHIC, Collections.singletonList(Gen2ItemIDs.twistedSpoon));
        map.put(Type.ROCK, Collections.singletonList(Gen2ItemIDs.hardStone));
        map.put(Type.STEEL, Collections.singletonList(Gen2ItemIDs.metalCoat));
        map.put(Type.WATER, Collections.singletonList(Gen2ItemIDs.mysticWater));
        map.put(null, Collections.emptyList()); // ??? type
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> speciesBoostingItems = initializeSpeciesBoostingItems();

    private static Map<Integer, List<Integer>> initializeSpeciesBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(SpeciesIDs.pikachu, Collections.singletonList(Gen2ItemIDs.lightBall));
        map.put(SpeciesIDs.chansey, Collections.singletonList(Gen2ItemIDs.luckyPunch));
        map.put(SpeciesIDs.ditto, Collections.singletonList(Gen2ItemIDs.metalPowder));
        map.put(SpeciesIDs.cubone, Collections.singletonList(Gen2ItemIDs.thickClub));
        map.put(SpeciesIDs.marowak, Collections.singletonList(Gen2ItemIDs.thickClub));
        map.put(SpeciesIDs.farfetchd, Collections.singletonList(Gen2ItemIDs.stick));
        return Collections.unmodifiableMap(map);
    }

    public static final List<Integer> regularShopItems = Collections.unmodifiableList(Arrays.asList(
            Gen2ItemIDs.pokeBall, Gen2ItemIDs.greatBall,
            Gen2ItemIDs.ultraBall, Gen2ItemIDs.potion, Gen2ItemIDs.superPotion, Gen2ItemIDs.hyperPotion, Gen2ItemIDs.maxPotion,
            Gen2ItemIDs.antidote, Gen2ItemIDs.burnHeal, Gen2ItemIDs.iceHeal, Gen2ItemIDs.awakening, Gen2ItemIDs.parlyzHeal,
            Gen2ItemIDs.fullHeal, Gen2ItemIDs.fullRestore, Gen2ItemIDs.revive, Gen2ItemIDs.repel, Gen2ItemIDs.superRepel,
            Gen2ItemIDs.maxRepel, Gen2ItemIDs.escapeRope
    ));

    // rare candy, lucky egg, and all the "valuable items"
    public static final List<Integer> opShopItems = Collections.unmodifiableList(Arrays.asList(
            Gen2ItemIDs.rareCandy, Gen2ItemIDs.luckyEgg,
            Gen2ItemIDs.nugget, Gen2ItemIDs.tinyMushroom, Gen2ItemIDs.bigMushroom, Gen2ItemIDs.pearl, Gen2ItemIDs.bigPearl,
            Gen2ItemIDs.stardust, Gen2ItemIDs.stardust, Gen2ItemIDs.brickPiece, Gen2ItemIDs.silverLeaf, Gen2ItemIDs.goldLeaf
    ));

    public static ItemList allowedItems;
    public static ItemList nonBadItems;

    static {
        setupAllowedItems();
    }

    private static void setupAllowedItems() {
        allowedItems = new ItemList(Gen2ItemIDs.hm07); // 250-255 are junk and cancel
        // Assorted key items
        allowedItems.banSingles(Gen2ItemIDs.bicycle, Gen2ItemIDs.coinCase, Gen2ItemIDs.itemfinder, Gen2ItemIDs.oldRod,
                Gen2ItemIDs.goodRod, Gen2ItemIDs.superRod, Gen2ItemIDs.gsBall, Gen2ItemIDs.blueCard, Gen2ItemIDs.basementKey,
                Gen2ItemIDs.pass, Gen2ItemIDs.squirtBottle, Gen2ItemIDs.rainbowWing);
        allowedItems.banRange(Gen2ItemIDs.redScale, 6);
        allowedItems.banRange(Gen2ItemIDs.cardKey, 4);
        // HMs
        allowedItems.banRange(Gen2ItemIDs.hm01, 7);
        // Unused items (Teru-Samas and dummy TMs)
        allowedItems.banSingles(Gen2ItemIDs.terusama6, Gen2ItemIDs.terusama25, Gen2ItemIDs.terusama45,
                Gen2ItemIDs.terusama50, Gen2ItemIDs.terusama56, Gen2ItemIDs.terusama90, Gen2ItemIDs.terusama100,
                Gen2ItemIDs.terusama120, Gen2ItemIDs.terusama135, Gen2ItemIDs.terusama136, Gen2ItemIDs.terusama137,
                Gen2ItemIDs.terusama141, Gen2ItemIDs.terusama142, Gen2ItemIDs.terusama145, Gen2ItemIDs.terusama147,
                Gen2ItemIDs.terusama148, Gen2ItemIDs.terusama149, Gen2ItemIDs.terusama153, Gen2ItemIDs.terusama154,
                Gen2ItemIDs.terusama155, Gen2ItemIDs.terusama162, Gen2ItemIDs.terusama171, Gen2ItemIDs.terusama176,
                Gen2ItemIDs.terusama179, Gen2ItemIDs.terusama190, Gen2ItemIDs.tm04Unused, Gen2ItemIDs.tm28Unused);
        // Real TMs
        allowedItems.tmRange(tmBlockOneIndex, tmBlockOneSize);
        allowedItems.tmRange(tmBlockTwoIndex, tmBlockTwoSize);
        allowedItems.tmRange(tmBlockThreeIndex, tmBlockThreeSize);

        // non-bad items
        // ban specific pokemon hold items, berries, apricorns, mail
        nonBadItems = allowedItems.copy();
        nonBadItems.banSingles(Gen2ItemIDs.luckyPunch, Gen2ItemIDs.metalPowder, Gen2ItemIDs.silverLeaf,
                Gen2ItemIDs.goldLeaf, Gen2ItemIDs.redApricorn, Gen2ItemIDs.bluApricorn, Gen2ItemIDs.whtApricorn,
                Gen2ItemIDs.blkApricorn, Gen2ItemIDs.pnkApricorn, Gen2ItemIDs.stick, Gen2ItemIDs.thickClub,
                Gen2ItemIDs.flowerMail, Gen2ItemIDs.lightBall, Gen2ItemIDs.berry, Gen2ItemIDs.brickPiece);
        nonBadItems.banRange(Gen2ItemIDs.ylwApricorn, 2);
        nonBadItems.banRange(Gen2ItemIDs.normalBox, 2);
        nonBadItems.banRange(Gen2ItemIDs.surfMail, 9);
    }

    public static final String friendshipValueForEvoLocator = "FEDCDA";

    public static String getName(SettingsManager.PlayerCharacterMod playerCharacter) {
        if (playerCharacter == SettingsManager.PlayerCharacterMod.PC1) {
            return "Chris";
        } else if (playerCharacter == SettingsManager.PlayerCharacterMod.PC2){
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
            if (t.trainerclass == classNum - 1) {
                currnum++;
                if (currnum == number) {
                    t.tag = tag;
                    return;
                }
            }
        }
    }

    private static final int[] gsPostGameEncounterAreasTOD = new int[] {
            327, //PALLET TOWN
            328, //VIRIDIAN CITY
            329, //CERULEAN CITY
            330, 334, //VERMILION CITY
            331, //CELADON CITY
            332, //FUCHSIA CITY
            239, 240, 241, //ROUTE 1
            242, 243, 244, //ROUTE 2
            245, 246, 247, //ROUTE 3
            248, 249, 250, 311, //ROUTE 4
            251, 252, 253, //ROUTE 5
            254, 255, 256, 312, //ROUTE 6
            257, 258, 259, //ROUTE 7
            260, 261, 262, //ROUTE 8
            263, 264, 265, 313, //ROUTE 9
            266, 267, 268, 314, //ROUTE 10
            269, 270, 271, //ROUTE 11
            315, //ROUTE 12
            272, 273, 274, 316, //ROUTE 13
            275, 276, 277, //ROUTE 14
            278, 279, 280, //ROUTE 15
            281, 282, 283, //ROUTE 16
            284, 285, 286, //ROUTE 17
            287, 288, 289, //ROUTE 18
            317, //ROUTE 19
            318, //ROUTE 20
            290, 291, 292, 319, //ROUTE 21
            293, 294, 295, 320, //ROUTE 22
            296, 297, 298, 321, //ROUTE 24
            299, 300, 301, 322, //ROUTE 25
            333, //CINNABAR ISLAND
            221, 222, 223, //DIGLETT's CAVE
            227, 228, 229, 230, 231, 232, //ROCK TUNNEL
            224, 225, 226, //MT.MOON
            308, 309, 310, 326, //ROUTE 28
            114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 180, 181, 182, 196, 219, //SILVER CAVE
    };

    private static final int[] gsPartialPostGameTOD = new int[] {
            348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359,
            360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, //Fishing
    };

    private static final int[] partialPostGameCutoffsTOD = new int[] {
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, // main fishing
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 // time-specific
    };

    private static final int[] gsPostGameEncounterAreasNoTOD = new int[] {
            145, //PALLET TOWN
            146, //VIRIDIAN CITY
            147, //CERULEAN CITY
            148, 152, //VERMILION CITY
            149, //CELADON CITY
            150, //FUCHSIA CITY
            105, //ROUTE 1
            106, //ROUTE 2
            107, //ROUTE 3
            108, 129, //ROUTE 4
            109, //ROUTE 5
            110, 130, //ROUTE 6
            111, //ROUTE 7
            112, //ROUTE 8
            113, 131, //ROUTE 9
            114, 132, //ROUTE 10
            115, //ROUTE 11
            133, //ROUTE 12
            116, 134, //ROUTE 13
            117, //ROUTE 14
            118, //ROUTE 15
            119, //ROUTE 16
            120, //ROUTE 17
            121, //ROUTE 18
            135, //ROUTE 19
            136, //ROUTE 20
            122, 137, //ROUTE 21
            123, 138, //ROUTE 22
            124, 139, //ROUTE 24
            125, 140, //ROUTE 25
            151, //CINNABAR ISLAND
            99, //DIGLETT's CAVE
            101, 102, //ROCK TUNNEL
            100, //MT.MOON
            128, 144, //ROUTE 28
            38, 39, 40, 41, 60, 74, 97, //SILVER CAVE
    };

    private static final int[] gsPartialPostGameNoTOD = new int[] {
            158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, //Fishing
    };

    private static final int[] partialPostGameCutoffsNoTOD = new int[] {
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };

    private static final int[] crysPostGameEncounterAreasTOD = new int[] {
            328, //PALLET TOWN
            329, //VIRIDIAN CITY
            330, //CERULEAN CITY
            312, 331, //VERMILION CITY
            332, //CELADON CITY
            333, //FUCHSIA CITY
            239, 240, 241, //ROUTE 1
            242, 243, 244, //ROUTE 2
            245, 246, 247, //ROUTE 3
            248, 249, 250, 313, //ROUTE 4
            251, 252, 253, //ROUTE 5
            254, 255, 256, 314, //ROUTE 6
            257, 258, 259, //ROUTE 7
            260, 261, 262, //ROUTE 8
            263, 264, 265, 315, //ROUTE 9
            266, 267, 268, 316, //ROUTE 10
            269, 270, 271, //ROUTE 11
            317, //ROUTE 12
            272, 273, 274, 318, //ROUTE 13
            275, 276, 277, //ROUTE 14
            278, 279, 280, //ROUTE 15
            281, 282, 283, //ROUTE 16
            284, 285, 286, //ROUTE 17
            287, 288, 289, //ROUTE 18
            319, //ROUTE 19
            320, //ROUTE 20
            290, 291, 292, 321, //ROUTE 21
            293, 294, 295, 322, //ROUTE 22
            296, 297, 298, 323, //ROUTE 24
            299, 300, 301, 324, //ROUTE 25
            334, //CINNABAR ISLAND
            221, 222, 223, //DIGLETT
            227, 228, 229, 230, 231, 232, //ROCK TUNNEL
            224, 225, 226, //MT.MOON
            308, 309, 310, 327, //ROUTE 28
            114, 115, 116, 117, 118, 119, 120, 121, 122, 123,
            124, 125, 180, 181, 182, 196, 220, //SILVER CAVE
    };

    private static final int[] crysPartialPostGameTOD = new int[] {
            341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352,
            353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, //Fishing
    };

    private static final int[] crysPostGameEncounterAreasNoTOD = new int[] {
            146, //PALLET TOWN
            147, //VIRIDIAN CITY
            148, //CERULEAN CITY
            130, 149, //VERMILION CITY
            150, //CELADON CITY
            151, //FUCHSIA CITY
            105, //ROUTE 1
            106, //ROUTE 2
            107, //ROUTE 3
            108, 131, //ROUTE 4
            109, //ROUTE 5
            110, 132, //ROUTE 6
            111, //ROUTE 7
            112, //ROUTE 8
            113, 133, //ROUTE 9
            114, 134, //ROUTE 10
            115, //ROUTE 11
            135, //ROUTE 12
            116, 136, //ROUTE 13
            117, //ROUTE 14
            118, //ROUTE 15
            119, //ROUTE 16
            120, //ROUTE 17
            121, //ROUTE 18
            137, //ROUTE 19
            138, //ROUTE 20
            122, 139, //ROUTE 21
            123, 140, //ROUTE 22
            124, 141, //ROUTE 24
            125, 142, //ROUTE 25
            152, //CINNABAR ISLAND
            99, //DIGLETT
            101, 102, //ROCK TUNNEL
            100, //MT.MOON
            128, 145, //ROUTE 28
            38, 39, 40, 41, 60, 74, 98, //SILVER CAVE
    };

    private static final int[] crysPartialPostGameNoTOD = new int[] {
            155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, //Fishing
    };

    private static final List<String> locationTagsNoTimeGS = Collections.unmodifiableList(Arrays.asList(
            // Johto cave/grass
            "SPROUT TOWER", "SPROUT TOWER",
            "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER",
            "BURNED TOWER", "BURNED TOWER",
            "NATIONAL PARK",
            "RUINS OF ALPH", "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE", "SILVER CAVE", "SILVER CAVE", "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37",
            "ROUTE 38", "ROUTE 39", "ROUTE 42", "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46",
            "SILVER CAVE",
            // Johto surfing
            "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "DRAGON'S DEN",
            "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 34", "ROUTE 35", "ROUTE 40", "ROUTE 41", "ROUTE 42", "ROUTE 43",
            "ROUTE 44", "ROUTE 45",
            "NEW BARK TOWN",
            "CHERRYGROVE CITY",
            "VIOLET CITY",
            "CIANWOOD CITY",
            "OLIVINE CITY",
            "ECRUTEAK CITY",
            "LAKE OF RAGE",
            "BLACKTHORN CITY",
            "SILVER CAVE",
            "OLIVINE CITY",
            // Kanto cave/grass
            "DIGLETT'S CAVE",
            "MT.MOON",
            "ROCK TUNNEL", "ROCK TUNNEL",
            "VICTORY ROAD",
            "TOHJO FALLS",
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28",
            // Kanto surfing
            "ROUTE 4", "ROUTE 6", "ROUTE 9", "ROUTE 10", "ROUTE 12", "ROUTE 13", "ROUTE 19", "ROUTE 20",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27",
            "TOHJO FALLS",
            "ROUTE 28",
            "PALLET TOWN",
            "VIRIDIAN CITY",
            "CERULEAN CITY",
            "VERMILION CITY",
            "CELADON CITY",
            "FUCHSIA CITY",
            "CINNABAR ISLAND",
            "VERMILION CITY",
            // Swarms
            "ROUTE 35",
            "ROUTE 38",
            "DARK CAVE",
            "MT.MORTAR", "MT.MORTAR",
            // Fishing, Headbutt, BCC
            "FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND", "FISHING DRATINI 1",
            "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS", "FISHING DRATINI 2",
            "FISHING WHIRL ISLANDS", "FISHING QWILFISH", "FISHING REMORAID",
            "HEADBUTT FOREST GS", "HEADBUTT FOREST GS", "HEADBUTT CANYON GS", "HEADBUTT CANYON GS", "ROCK SMASH",
            "BUG CATCHING CONTEST"
    ));

    private static final List<String> locationTagsUseTimeGS = initLocationTagsUseTimeGS();

    private static List<String> initLocationTagsUseTimeGS() {
        List<String> locationTags = new ArrayList<>();
        for (int areaNum = 0; areaNum < 61; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeGS.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeGS.subList(61, 99));
        for (int areaNum = 99; areaNum < 129; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeGS.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeGS.subList(129, 153));
        for (int areaNum = 153; areaNum < 157; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeGS.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeGS.subList(157, 170));
        locationTags.addAll(Arrays.asList("FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND",
                "FISHING DRATINI 1", "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS",
                "FISHING DRATINI 2", "FISHING WHIRL ISLANDS", "FISHING QWILFISH"));
        locationTags.addAll(locationTagsNoTimeGS.subList(170, 176));
        return Collections.unmodifiableList(locationTags);
    }

    private static final List<String> locationTagsNoTimeCrystal = Collections.unmodifiableList(Arrays.asList(
            // Johto cave/grass
            "SPROUT TOWER", "SPROUT TOWER",
            "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER", "TIN TOWER",
            "BURNED TOWER", "BURNED TOWER",
            "NATIONAL PARK",
            "RUINS OF ALPH", "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH", "ICE PATH",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE", "SILVER CAVE", "SILVER CAVE", "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37",
            "ROUTE 38", "ROUTE 39", "ROUTE 42", "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46",
            "SILVER CAVE",
            // Johto surfing
            "RUINS OF ALPH",
            "UNION CAVE", "UNION CAVE", "UNION CAVE",
            "SLOWPOKE WELL", "SLOWPOKE WELL",
            "ILEX FOREST",
            "MT.MORTAR", "MT.MORTAR", "MT.MORTAR",
            "WHIRL ISLANDS", "WHIRL ISLANDS", "WHIRL ISLANDS",
            "SILVER CAVE",
            "DARK CAVE", "DARK CAVE",
            "DRAGON'S DEN",
            "OLIVINE CITY",
            "ROUTE 30", "ROUTE 31", "ROUTE 32", "ROUTE 34", "ROUTE 35", "ROUTE 40", "ROUTE 41", "ROUTE 42", "ROUTE 43",
            "ROUTE 44", "ROUTE 45",
            "NEW BARK TOWN",
            "CHERRYGROVE CITY",
            "VIOLET CITY",
            "CIANWOOD CITY",
            "OLIVINE CITY",
            "ECRUTEAK CITY",
            "LAKE OF RAGE",
            "BLACKTHORN CITY",
            "SILVER CAVE",
            // Kanto cave/grass
            "DIGLETT'S CAVE",
            "MT.MOON",
            "ROCK TUNNEL", "ROCK TUNNEL",
            "VICTORY ROAD",
            "TOHJO FALLS",
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28",
            // Kanto surfing
            "TOHJO FALLS",
            "VERMILION CITY",
            "ROUTE 4", "ROUTE 6", "ROUTE 9", "ROUTE 10", "ROUTE 12", "ROUTE 13", "ROUTE 19", "ROUTE 20",
            "ROUTE 21", "ROUTE 22", "ROUTE 24", "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28",
            "PALLET TOWN",
            "VIRIDIAN CITY",
            "CERULEAN CITY",
            "VERMILION CITY",
            "CELADON CITY",
            "FUCHSIA CITY",
            "CINNABAR ISLAND",
            // Swarms
            "DARK CAVE",
            "ROUTE 35",
            // Fishing, Headbutt, BCC
            "FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND", "FISHING DRATINI 1",
            "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS", "FISHING DRATINI 2",
            "FISHING WHIRL ISLANDS", "FISHING QWILFISH", "FISHING REMORAID",
            "HEADBUTT CANYON C", "HEADBUTT CANYON C", "HEADBUTT TOWN", "HEADBUTT TOWN", "HEADBUTT ROUTE",
            "HEADBUTT ROUTE", "HEADBUTT KANTO", "HEADBUTT KANTO", "HEADBUTT LAKE", "HEADBUTT LAKE", "HEADBUTT FOREST C",
            "HEADBUTT FOREST C", "ROCK SMASH",
            "BUG CATCHING CONTEST"
    ));

    private static final List<String> locationTagsUseTimeCrystal = initLocationTagsUseTimeCrystal();

    private static List<String> initLocationTagsUseTimeCrystal() {
        List<String> locationTags = new ArrayList<>();
        for (int areaNum = 0; areaNum < 61; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeCrystal.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeCrystal.subList(61, 99));
        for (int areaNum = 99; areaNum < 129; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeCrystal.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeCrystal.subList(129, 153));
        for (int areaNum = 153; areaNum < 155; areaNum++) {
            for (int i = 0; i < 3; i++) {
                locationTags.add(locationTagsNoTimeCrystal.get(areaNum));
            }
        }
        locationTags.addAll(locationTagsNoTimeCrystal.subList(155, 167));
        locationTags.addAll(Arrays.asList("FISHING SHORE", "FISHING OCEAN", "FISHING LAKE", "FISHING POND",
                "FISHING DRATINI 1", "FISHING QWILFISH", "FISHING REMORAID", "FISHING GYARADOS",
                "FISHING DRATINI 2", "FISHING WHIRL ISLANDS", "FISHING QWILFISH"));
        locationTags.addAll(locationTagsNoTimeCrystal.subList(167, 181));
        return Collections.unmodifiableList(locationTags);
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

    private static void tagEncounterAreas(List<EncounterArea> encounterAreas, List<String> locationTags,
                                          int[] postGameAreas, int[] partialPostGameAreas, int[] partialPostGameCutoffs) {
        if (encounterAreas.size() != locationTags.size()) {
            throw new IllegalArgumentException("Unexpected amount of encounter areas");
        }
        for (int i = 0; i < encounterAreas.size(); i++) {
            encounterAreas.get(i).setLocationTag(locationTags.get(i));
        }
        for (int areaIndex : postGameAreas) {
            encounterAreas.get(areaIndex).setPostGame(true);
        }
        for (int i = 0; i < partialPostGameAreas.length; i++) {
            int areaIndex = partialPostGameAreas[i];
            int cutoff = partialPostGameCutoffs[i];
            encounterAreas.get(areaIndex).setPartiallyPostGameCutoff(cutoff);
        }
    }

    public static void tagEncounterAreas(List<EncounterArea> encounterAreas, boolean useTimeOfDay, boolean isCrystal) {
        List<String> locationTags = isCrystal ?
                (useTimeOfDay ? locationTagsUseTimeCrystal : locationTagsNoTimeCrystal) :
                (useTimeOfDay ? locationTagsUseTimeGS : locationTagsNoTimeGS);
        int[] postGameAreas = isCrystal ?
                (useTimeOfDay ? crysPostGameEncounterAreasTOD : crysPostGameEncounterAreasNoTOD) :
                (useTimeOfDay ? gsPostGameEncounterAreasTOD : gsPostGameEncounterAreasNoTOD);
        int[] partialPostGameAreas = isCrystal ?
                (useTimeOfDay ? crysPartialPostGameTOD : crysPartialPostGameNoTOD) :
                (useTimeOfDay ? gsPartialPostGameTOD : gsPartialPostGameNoTOD);
        int[] partialPostGameCutoffs = useTimeOfDay ? partialPostGameCutoffsTOD : partialPostGameCutoffsNoTOD;
        tagEncounterAreas(encounterAreas, locationTags, postGameAreas, partialPostGameAreas, partialPostGameCutoffs);
    }

    public static final Map<Integer, Integer> balancedItemPrices = Stream.of(new Integer[][]{

            // general held items
            {Gen2ItemIDs.brightPowder, 3000}, // same as in Gen3Constants
            {Gen2ItemIDs.expShare, 6000}, // same as in Gen3Constants
            {Gen2ItemIDs.quickClaw, 4500}, // sane as in Gen3Constants
            {Gen2ItemIDs.kingsRock, 5000}, // same as in Gen3Constants
            {Gen2ItemIDs.amuletCoin, 1500}, // same as in Gen3Constants, could be too low
            {Gen2ItemIDs.smokeBall, 1200}, // vanilla value of 200 felt too low
            {Gen2ItemIDs.everstone, 200}, // same as in Gen3Constants
            {Gen2ItemIDs.focusBand, 3000}, // same as in Gen3Constants
            {Gen2ItemIDs.leftovers, 10000}, // same as in Gen3Constants
            {Gen2ItemIDs.cleanseTag, 1000}, // same as in Gen3Constants
            {Gen2ItemIDs.luckyEgg, 10000}, // same as in Gen3Constants
            {Gen2ItemIDs.scopeLens, 5000}, // same as in Gen3Constants
            {Gen2ItemIDs.berserkGene, 800}, // probably not a very good item

            // type boosting items
            {Gen2ItemIDs.softSand, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.sharpBeak, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.poisonBarb, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.silverPowder, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.mysticWater, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.twistedSpoon, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.blackbelt, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.blackGlasses, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.pinkBow, 2000}, // same as other type-boosting items
            {Gen2ItemIDs.polkadotBow, 2000}, // same as other type-boosting items
            {Gen2ItemIDs.neverMeltIce, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.magnet, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.spellTag, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.miracleSeed, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.hardStone, 2000}, // same as in Gen3Constants
            {Gen2ItemIDs.charcoal, 2000}, // same as other type-boosting items; the vanilla cost is way too high
            {Gen2ItemIDs.metalCoat, 2000}, // same as other type-boosting items
            {Gen2ItemIDs.dragonScale, 2000}, // same as in Gen3Constants

            // specific poke boosting items
            {Gen2ItemIDs.luckyPunch, 1200}, // vanilla value of 10 felt too low
            {Gen2ItemIDs.metalPowder, 1200}, // vanilla value of 10 felt too low
            {Gen2ItemIDs.stick, 1200}, // vanilla value of 200 felt too low
            {Gen2ItemIDs.thickClub, 2300}, // vanilla value of 500 felt too low
            {Gen2ItemIDs.lightBall, 2300}, // vanilla value of 100 felt too low

            // berries
            {Gen2ItemIDs.berry, 50}, // same as Gen3Constants Oran Berry
            {Gen2ItemIDs.goldBerry, 500}, // same as Gen3Constants Sitrus Berry
            {Gen2ItemIDs.psnCureBerry, 100}, // same as Gen3Constants Pecha Berry
            {Gen2ItemIDs.przCureBerry, 200}, // same as Gen3Constants Cheri Berry
            {Gen2ItemIDs.burntBerry, 250}, // same as Gen3Constants Aspear Berry
            {Gen2ItemIDs.iceBerry, 250}, // same as Gen3Constants Rawst Berry
            {Gen2ItemIDs.bitterBerry, 200}, // same as Gen3Constants Persim Berry
            {Gen2ItemIDs.mintBerry, 250}, // same as Gen3Constants Chesto Berry
            {Gen2ItemIDs.miracleBerry, 500}, // same as Gen3Constants Lum Berry
            {Gen2ItemIDs.mysteryBerry, 3000}, // same as Gen3Constants Leppa Berry
            {Gen2ItemIDs.berryJuice, 300}, // same as potion (which also heals 20 HP)

            // poke balls
            {Gen2ItemIDs.masterBall, 3000},
            {Gen2ItemIDs.parkBall, 600}, // same as Great Ball
            // all the Apricorn balls are worth 300, same as in Gen4Constants
            {Gen2ItemIDs.heavyBall, 300},
            {Gen2ItemIDs.levelBall, 300},
            {Gen2ItemIDs.lureBall, 300},
            {Gen2ItemIDs.fastBall, 300},
            {Gen2ItemIDs.friendBall, 300},
            {Gen2ItemIDs.moonBall, 300},
            {Gen2ItemIDs.loveBall, 300},

            // misc
            {Gen2ItemIDs.moonStone, 2100}, // same as other stones
            {Gen2ItemIDs.rareCandy, 10000}, // same as in Gen3Constants
            {Gen2ItemIDs.sacredAsh, 10000}, // same as in Gen3Constants
            {Gen2ItemIDs.dragonFang, 100}, // it does nothing in Gen2 due to a bug
            {Gen2ItemIDs.normalBox, 1000}, // arbitrary. these boxes should be unobtainable, but I'm not sure
            {Gen2ItemIDs.gorgeousBox, 1000}
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


}
