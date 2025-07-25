package com.dabomstew.pkromio.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen3Constants.java - Constants for Ruby/Sapphire/FR/LG/Emerald        --*/
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

import com.dabomstew.pkromio.gamedata.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gen3Constants {

    public static final int RomType_Ruby = 0;
    public static final int RomType_Sapp = 1;
    public static final int RomType_Em = 2;
    public static final int RomType_FRLG = 3;

    public static final int size8M = 0x800000, size16M = 0x1000000, size32M = 0x2000000;

    public static final String unofficialEmeraldROMName = "YJencrypted";

    public static final int romNameOffset = 0xA0, romCodeOffset = 0xAC, romVersionOffset = 0xBC,
            headerChecksumOffset = 0xBD;

    public static final int pokemonCount = 386;

    public static final int unownFormeCount = 28, unownBIndex = 413;

    public static final String rsPokemonNamesPointerSuffix = "30B50025084CC8F7";

    // These pointer prefixes aren't used despite being accurate to all vanilla ROMs. Using them to search was slow
    // enough to become annoying when unit testing, compared to having the offsets they dug out in the RomEntry .ini files.
    // They remain here in case you want to fill a new RomEntry (for a ROM hack).
    @SuppressWarnings("unused")
    public static final String wildPokemonPointerPrefix = "0348048009E00000FFFF0000",
            mapBanksPointerPrefix = "80180068890B091808687047",
            frlgMapLabelsPointerPrefix = "AC470000AE470000B0470000",
            rseMapLabelsPointerPrefix = "C078288030BC01BC00470000",
            pokedexOrderPointerPrefix = "0448814208D0481C0004000C05E00000";

    // pointer block 1
    public static final int pokemonFrontImagesPointer = 0x128, pokemonBackImagesPointer = 0x12C,
    		pokemonNormalPalettesPointer = 0x130, pokemonShinyPalettesPointer = 0x134,
    		pokemonIconSpritesPointer = 0x138, pokemonIconPalettesPointer = 0x13C,
    		pokemonNamesPointer = 0x144, moveNamesPointer = 0x148, decorationNamesPointer = 0x14C;

    // pointer block 2
    public static final int pokemonStatsPointer = 0x1BC, abilityNamesPointer = 0x1C0, 
    		abilityDescriptionsPointer = 0x1C4, itemDataPointer = 0x1C8, moveDataPointer = 0x1CC,
    		ballSpritesPointer = 0x1D0, ballPalettesPointer = 0x1D4;    

    private static final String runningShoesCheckPrefixRS = "0440002C1DD08620", runningShoesCheckPrefixFRLG = "02200540002D29D0",
            runningShoesCheckPrefixE = "0640002E1BD08C20";

    public static final byte[] emptyPokemonSig = new byte[] { 0x32, (byte) 0x96, 0x32, (byte) 0x96, (byte) 0x96, 0x32,
            0x00, 0x00, 0x03, 0x01, (byte) 0xAA, 0x0A, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, 0x78, 0x00, 0x00, 0x0F,
            0x0F, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00 };

    public static final int baseStatsEntrySize = 0x1C;

    public static final int bsHPOffset = 0, bsAttackOffset = 1, bsDefenseOffset = 2, bsSpeedOffset = 3,
            bsSpAtkOffset = 4, bsSpDefOffset = 5, bsPrimaryTypeOffset = 6, bsSecondaryTypeOffset = 7,
            bsCatchRateOffset = 8, bsCommonHeldItemOffset = 12, bsRareHeldItemOffset = 14, bsGenderRatioOffset = 16,
            bsGrowthCurveOffset = 19, bsAbility1Offset = 22, bsAbility2Offset = 23;

    public static final byte textTerminator = (byte) 0xFF, textVariable = (byte) 0xFD, textPadding = (byte) 0x00;

    public static final byte freeSpaceByte = (byte) 0xFF;

    public static final int unusedSpaceChunkLength = 0x100, unusedSpaceFrontMargin = 0x10;

    public static final int rseStarter2Offset = 2, rseStarter3Offset = 4, frlgStarter2Offset = 515,
            frlgStarter3Offset = 461, frlgStarterRepeatOffset = 5;

    public static final int frlgStarterItemsOffset = 218;

    public static final int gbaAddRxOpcode = 0x30, gbaUnconditionalJumpOpcode = 0xE0, gbaSetRxOpcode = 0x20,
            gbaCmpRxOpcode = 0x28, gbaNopOpcode = 0x46C0;

    public static final int gbaR0 = 0, gbaR1 = 1, gbaR2 = 2, gbaR3 = 3, gbaR4 = 4, gbaR5 = 5, gbaR6 = 6, gbaR7 = 7;

    public static final Type[] typeTable = constructTypeTable();

    public static final int walkingSlots = 12, surfingSlots = 5, rockSmashSlots = 5, fishingSlots = 10;

    public static final byte[] vanillaMovesLearntTerminator = new byte[] {(byte) 0xFF, (byte) 0xFF},
            jamboMovesLearntTerminator = new byte[] {0x00, 0x00, (byte) 0xFF};

    public static final int tmCount = 50, hmCount = 8;

    public static final List<Integer> hmMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.rockSmash, MoveIDs.waterfall, MoveIDs.dive);

    public static final int tmItemOffset = Gen3ItemIDs.tm01;

    public static final int rseItemDescCharsPerLine = 18, frlgItemDescCharsPerLine = 24;

    public static final int regularTextboxCharsPerLine = 36;

    public static final int pointerSearchRadius = 500;

    public static final int itemDataDescriptionOffset = 0x14;

    public static final String deoxysObeyCode = "CD21490088420FD0";

    public static final int mewObeyOffsetFromDeoxysObey = 0x16;

    public static final String levelEvoKantoDexCheckCode = "972814DD";

    public static final String stoneEvoKantoDexCheckCode = "972808D9";

    public static final int levelEvoKantoDexJumpAmount = 0x14, stoneEvoKantoDexJumpAmount = 0x08;

    public static final String rsPokedexScriptIdentifier = "326629010803";

    public static final int rsNatDexScriptLength = 44;

    public static final String rsNatDexScriptPart1 = "31720167";

    public static final String rsNatDexScriptPart2 = "32662901082B00801102006B02021103016B020211DABE4E020211675A6A02022A008003";

    public static final String frlgPokedexScriptIdentifier = "292908258101";

    public static final int frlgNatDexScriptLength = 10;

    public static final String frlgNatDexScript = "292908258101256F0103";

    public static final String frlgNatDexFlagChecker = "260D809301210D800100";

    public static final String frlgE4FlagChecker = "2B2C0800000000000000";

    public static final String frlgOaksLabKantoDexChecker = "257D011604800000260D80D400";

    public static final String frlgOaksLabFix = "257D011604800100";

    public static final String frlgOakOutsideHouseCheck = "1604800000260D80D4001908800580190980068083000880830109802109803C";

    public static final String frlgOakOutsideHouseFix = "1604800100";

    public static final String frlgOakAideCheckPrefix = "00B5064800880028";

    public static final String ePokedexScriptIdentifier = "3229610825F00129E40816CD40010003";

    public static final int eNatDexScriptLength = 27;

    public static final String eNatDexScriptPart1 = "31720167";

    public static final String eNatDexScriptPart2 = "3229610825F00129E40825F30116CD40010003";

    public static final String friendshipValueForEvoLocator = "DB2900D8";

    public static final String perfectOddsBranchLocator = "FE2E2FD90020";

    public static final int unhackedMaxPokedex = 411, unhackedRealPokedex = 386, hoennPokesStart = 252;

    public static final int evolutionMethodCount = 15;

    public static final int cacophonyIndex = 76, airLockIndex = 77, highestAbilityIndex = 77;

    public static final int emMeteorFallsStevenIndex = 804;

    public static String rseGetName(PlayerCharacterType playerCharacter) {
        if (playerCharacter == PlayerCharacterType.PC1) {
            return "Brendan";
        } else if (playerCharacter == PlayerCharacterType.PC2) {
            return "May";
        } else {
            throw new IllegalArgumentException("Invalid enum. RSE only has two playable characters, Brendan and May.");
        }
    }

    public static String frlgGetName(PlayerCharacterType playerCharacter) {
        if (playerCharacter == PlayerCharacterType.PC1) {
            return "Red";
        } else if (playerCharacter == PlayerCharacterType.PC2) {
            return "Leaf";
        } else {
            throw new IllegalArgumentException("Invalid enum. FRLG only has two playable characters, Red and Leaf.");
        }
    }

    public static final int emBrendanFrontImageIndex = 71, frlgRedFrontImageIndex = 135;

    public static final int rsTrainerFrontPalettesOffset = 0x298, emTrainerFrontPalettesOffset = 0x2E8,
            frlgTrainerFrontPalettesOffset = 0x4A0,
            rsTrainerBackPalettesOffset = 0x18, emTrainerBackPalettesOffset = 0x40,
            frlgTrainerBackPalettesOffset = 0x30;

    public static final int brendanMapIconPaletteOffset = -0x20, mayMapIconImageOffset = 0xA0,
            mayMapIconPaletteOffset = 0x80, redMapIconPalettePointerOffset = 0xC4,
            leafMapIconImagePointerOffset = -0x30, leafMapIconPalettePointerOffset = 0xE0;

    public static final Map<Integer,List<Integer>> abilityVariations = setupAbilityVariations();

    private static Map<Integer,List<Integer>> setupAbilityVariations() {
        Map<Integer,List<Integer>> map = new HashMap<>();
        map.put(AbilityIDs.insomnia, Arrays.asList(AbilityIDs.insomnia, AbilityIDs.vitalSpirit));
        map.put(AbilityIDs.clearBody, Arrays.asList(AbilityIDs.clearBody, AbilityIDs.whiteSmoke));
        map.put(AbilityIDs.hugePower, Arrays.asList(AbilityIDs.hugePower, AbilityIDs.purePower));
        map.put(AbilityIDs.battleArmor, Arrays.asList(AbilityIDs.battleArmor, AbilityIDs.shellArmor));
        map.put(AbilityIDs.cloudNine, Arrays.asList(AbilityIDs.cloudNine, Gen3Constants.airLockIndex));

        return map;
    }

    public static final List<Integer> uselessAbilities = Arrays.asList(AbilityIDs.forecast, Gen3Constants.cacophonyIndex);

    public static final int frlgMapLabelsStart = 0x58;

    public static final int noDamageSleepEffect = 1, damagePoisonEffect = 2, damageAbsorbEffect = 3, damageBurnEffect = 4,
            damageFreezeEffect = 5, damageParalyzeEffect = 6, dreamEaterEffect = 8, noDamageAtkPlusOneEffect = 10,
            noDamageDefPlusOneEffect = 11, noDamageSpAtkPlusOneEffect = 13, noDamageEvasionPlusOneEffect = 16,
            noDamageAtkMinusOneEffect = 18, noDamageDefMinusOneEffect = 19, noDamageSpeMinusOneEffect = 20,
            noDamageAccuracyMinusOneEffect = 23, noDamageEvasionMinusOneEffect = 24, flinchEffect = 31, toxicEffect = 33,
            razorWindEffect = 39, bindingEffect = 42, increasedCritEffect = 43, damageRecoil25PercentEffect = 48,
            noDamageConfusionEffect = 49, noDamageAtkPlusTwoEffect = 50, noDamageDefPlusTwoEffect = 51,
            noDamageSpePlusTwoEffect = 52, noDamageSpAtkPlusTwoEffect = 53, noDamageSpDefPlusTwoEffect = 54,
            noDamageAtkMinusTwoEffect = 58, noDamageDefMinusTwoEffect = 59, noDamageSpeMinusTwoEffect = 60,
            noDamageSpDefMinusTwoEffect = 62, noDamagePoisonEffect = 66, noDamageParalyzeEffect = 67,
            damageAtkMinusOneEffect = 68, damageDefMinusOneEffect = 69, damageSpeMinusOneEffect = 70,
            damageSpAtkMinusOneEffect = 71, damageSpDefMinusOneEffect = 72, damageAccuracyMinusOneEffect = 73,
            skyAttackEffect = 75, damageConfusionEffect = 76, twineedleEffect = 77, rechargeEffect = 80,
            snoreEffect = 92, trappingEffect = 106, minimizeEffect = 108, swaggerEffect = 118,
            damageBurnAndThawUserEffect = 125, damageUserDefPlusOneEffect = 138, damageUserAtkPlusOneEffect = 139,
            damageUserAllPlusOneEffect = 140, skullBashEffect = 145, twisterEffect = 146,
            futureSightAndDoomDesireEffect = 148, flinchWithMinimizeBonusEffect = 150, solarbeamEffect = 151,
            thunderEffect = 152, semiInvulnerableEffect = 155, defenseCurlEffect = 156, fakeOutEffect = 158,
            spitUpEffect = 161, flatterEffect = 166, noDamageBurnEffect = 167, chargeEffect = 174,
            damageUserAtkAndDefMinusOneEffect = 182, damageRecoil33PercentEffect = 198, teeterDanceEffect = 199,
            blazeKickEffect = 200, poisonFangEffect = 202, damageUserSpAtkMinusTwoEffect = 204,
            noDamageAtkAndDefMinusOneEffect = 205, noDamageDefAndSpDefPlusOneEffect = 206,
            noDamageAtkAndDefPlusOneEffect = 208, poisonTailEffect = 209, noDamageSpAtkAndSpDefPlusOneEffect = 211,
            noDamageAtkAndSpePlusOneEffect = 212;

    public static final List<Integer> soundMoves = Arrays.asList(MoveIDs.growl, MoveIDs.roar, MoveIDs.sing, MoveIDs.supersonic,
            MoveIDs.screech, MoveIDs.snore, MoveIDs.uproar, MoveIDs.metalSound, MoveIDs.grassWhistle, MoveIDs.hyperVoice,
            MoveIDs.perishSong, MoveIDs.healBell);

    public static final int tmsStartIndex = Gen3ItemIDs.tm01;

    public static final List<Integer> rsRequiredFieldTMs = Arrays.asList(Gen3ItemIDs.tm01, Gen3ItemIDs.tm02,
            Gen3ItemIDs.tm06, Gen3ItemIDs.tm07, Gen3ItemIDs.tm11, Gen3ItemIDs.tm18, Gen3ItemIDs.tm22, Gen3ItemIDs.tm23,
            Gen3ItemIDs.tm26, Gen3ItemIDs.tm30, Gen3ItemIDs.tm37, Gen3ItemIDs.tm48);

    public static final List<Integer> eRequiredFieldTMs = Arrays.asList(Gen3ItemIDs.tm02, Gen3ItemIDs.tm06,
            Gen3ItemIDs.tm07, Gen3ItemIDs.tm11, Gen3ItemIDs.tm18, Gen3ItemIDs.tm22, Gen3ItemIDs.tm23, Gen3ItemIDs.tm30,
            Gen3ItemIDs.tm37, Gen3ItemIDs.tm48);

    public static final List<Integer> frlgRequiredFieldTMs = Arrays.asList(Gen3ItemIDs.tm01, Gen3ItemIDs.tm02,
            Gen3ItemIDs.tm07, Gen3ItemIDs.tm08, Gen3ItemIDs.tm09, Gen3ItemIDs.tm11, Gen3ItemIDs.tm12, Gen3ItemIDs.tm14,
            Gen3ItemIDs.tm17, Gen3ItemIDs.tm18, Gen3ItemIDs.tm21, Gen3ItemIDs.tm22, Gen3ItemIDs.tm25, Gen3ItemIDs.tm32,
            Gen3ItemIDs.tm36, Gen3ItemIDs.tm37, Gen3ItemIDs.tm40, Gen3ItemIDs.tm41, Gen3ItemIDs.tm44, Gen3ItemIDs.tm46,
            Gen3ItemIDs.tm47, Gen3ItemIDs.tm48, Gen3ItemIDs.tm49, Gen3ItemIDs.tm50);

    public static final List<Integer> rseFieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.waterfall, MoveIDs.rockSmash, MoveIDs.sweetScent, MoveIDs.dive, MoveIDs.secretPower);

    public static final List<Integer> frlgFieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.waterfall, MoveIDs.rockSmash, MoveIDs.sweetScent);

    public static final List<Integer> rseEarlyRequiredHMMoves = Collections.singletonList(MoveIDs.rockSmash);

    public static final List<Integer> frlgEarlyRequiredHMMoves = Collections.singletonList(MoveIDs.cut);

    public static final byte[] shopTerminator = new byte[] {0x00, 0x00, 0x6C, 0x02};

    private static final List<String> rsShopNames = Arrays.asList(
            "Slateport Vitamins",
            "Slateport TMs",
            "Oldale Poké Mart (Before Pokédex)",
            "Oldale Poké Mart (After Pokédex)",
            "Lavaridge Herbs",
            "Lavaridge Poké Mart",
            "Fallarbor Poké Mart",
            "Verdanturf Poké Mart",
            "Petalburg Poké Mart (Before 4 Badges)",
            "Petalburg Poké Mart (After 4 Badges)",
            "Slateport Poké Mart",
            "Mauville Poké Mart",
            "Rustboro Poké Mart (Before Delivering Devon Goods)",
            "Rustboro Poké Mart (After Delivering Devon Goods)",
            "Fortree Poké Mart",
            "Lilycove Department Store 2F Left",
            "Lilycove Department Store 2F Right",
            "Lilycove Department Store 3F Left",
            "Lilycove Department Store 3F Right",
            "Lilycove Department Store 4F Left (TMs)",
            "Lilycove Department Store 4F Right (TMs)",
            "Mossdeep Poké Mart",
            "Sootopolis Poké Mart",
            "Pokémon League Poké Mart"
    );

    private static final List<String> frlgShopNames = Arrays.asList(
            "Trainer Tower Poké Mart",
            "Two Island Market Stall (Initial)",
            "Two Island Market Stall (After Saving Lostelle)",
            "Two Island Market Stall (After Hall of Fame)",
            "Two Island Market Stall (After Ruby/Sapphire Quest)",
            "Viridian Poké Mart",
            "Pewter Poké Mart",
            "Cerulean Poké Mart",
            "Lavender Poké Mart",
            "Vermillion Poké Mart",
            "Celadon Department 2F South",
            "Celadon Department 2F North (TMs)",
            "Celadon Department 4F",
            "Celadon Department 5F South",
            "Celadon Department 5F North",
            "Fuchsia Poké Mart",
            "Cinnabar Poké Mart",
            "Indigo Plateau Poké Mart",
            "Saffron Poké Mart",
            "Seven Island Poké Mart",
            "Three Island Poké Mart",
            "Four Island Poké Mart",
            "Six Island Poké Mart"
    );

    private static final List<String> emShopNames = Arrays.asList(
            "Slateport Vitamins",
            "Slateport TMs",
            "Oldale Poké Mart (Before Pokédex)",
            "Oldale Poké Mart (After Pokédex)",
            "Lavaridge Herbs",
            "Lavaridge Poké Mart",
            "Fallarbor Poké Mart",
            "Verdanturf Poké Mart",
            "Petalburg Poké Mart (Before 4 Badges)",
            "Petalburg Poké Mart (After 4 Badges)",
            "Slateport Poké Mart",
            "Mauville Poké Mart",
            "Rustboro Poké Mart (Before Delivering Devon Goods)",
            "Rustboro Poké Mart (After Delivering Devon Goods)",
            "Fortree Poké Mart",
            "Lilycove Department Store 2F Left",
            "Lilycove Department Store 2F Right",
            "Lilycove Department Store 3F Left",
            "Lilycove Department Store 3F Right",
            "Lilycove Department Store 4F Left (TMs)",
            "Lilycove Department Store 4F Right (TMs)",
            "Mossdeep Poké Mart",
            "Sootopolis Poké Mart",
            "Pokémon League Poké Mart",
            "Battle Frontier Poké Mart",
            "Trainer Hill Poké Mart (Before Hall of Fame)",
            "Trainer Hill Poké Mart (After Hall of Fame)"
    );

    public static List<String> getShopNames(int romType) {
        if (romType == RomType_Ruby || romType == RomType_Sapp) {
            return rsShopNames;
        } else if (romType == RomType_FRLG) {
            return frlgShopNames;
        } else if (romType == RomType_Em) {
            return emShopNames;
        } else {
            throw new IllegalArgumentException("Invalid RomType");
        }
    }

    public static final List<Integer> evolutionItems = Arrays.asList(Gen3ItemIDs.sunStone, Gen3ItemIDs.moonStone,
            Gen3ItemIDs.fireStone, Gen3ItemIDs.thunderstone, Gen3ItemIDs.waterStone, Gen3ItemIDs.leafStone);

    public static final List<Integer> xItems = Arrays.asList(Gen3ItemIDs.guardSpec, Gen3ItemIDs.direHit, Gen3ItemIDs.xAttack,
            Gen3ItemIDs.xDefend, Gen3ItemIDs.xSpeed, Gen3ItemIDs.xAccuracy, Gen3ItemIDs.xSpecial);

    public static final List<Integer> consumableHeldItems = Collections.unmodifiableList(Arrays.asList(
            Gen3ItemIDs.cheriBerry, Gen3ItemIDs.chestoBerry, Gen3ItemIDs.pechaBerry, Gen3ItemIDs.rawstBerry,
            Gen3ItemIDs.aspearBerry, Gen3ItemIDs.leppaBerry, Gen3ItemIDs.oranBerry, Gen3ItemIDs.persimBerry, Gen3ItemIDs.lumBerry,
            Gen3ItemIDs.sitrusBerry, Gen3ItemIDs.figyBerry, Gen3ItemIDs.wikiBerry, Gen3ItemIDs.magoBerry, Gen3ItemIDs.aguavBerry,
            Gen3ItemIDs.iapapaBerry, Gen3ItemIDs.liechiBerry, Gen3ItemIDs.ganlonBerry, Gen3ItemIDs.salacBerry,
            Gen3ItemIDs.petayaBerry, Gen3ItemIDs.apicotBerry, Gen3ItemIDs.lansatBerry, Gen3ItemIDs.starfBerry,
            Gen3ItemIDs.berryJuice, Gen3ItemIDs.whiteHerb, Gen3ItemIDs.mentalHerb));

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        List<Integer> list = new ArrayList<>();
        list.addAll(Arrays.asList(Gen3ItemIDs.brightPowder, Gen3ItemIDs.quickClaw, Gen3ItemIDs.choiceBand,
                Gen3ItemIDs.kingsRock, Gen3ItemIDs.silverPowder, Gen3ItemIDs.focusBand, Gen3ItemIDs.scopeLens,
                Gen3ItemIDs.metalCoat, Gen3ItemIDs.leftovers, Gen3ItemIDs.softSand, Gen3ItemIDs.hardStone,
                Gen3ItemIDs.miracleSeed, Gen3ItemIDs.blackGlasses, Gen3ItemIDs.blackBelt, Gen3ItemIDs.magnet,
                Gen3ItemIDs.mysticWater, Gen3ItemIDs.sharpBeak, Gen3ItemIDs.poisonBarb, Gen3ItemIDs.neverMeltIce,
                Gen3ItemIDs.spellTag, Gen3ItemIDs.twistedSpoon, Gen3ItemIDs.charcoal, Gen3ItemIDs.dragonFang,
                Gen3ItemIDs.silkScarf, Gen3ItemIDs.shellBell, Gen3ItemIDs.seaIncense, Gen3ItemIDs.laxIncense));
        list.addAll(consumableHeldItems);
        return Collections.unmodifiableList(list);
    }

    public static final List<Integer> generalPurposeConsumableItems = Collections.unmodifiableList(Arrays.asList(
            Gen3ItemIDs.cheriBerry, Gen3ItemIDs.chestoBerry, Gen3ItemIDs.pechaBerry, Gen3ItemIDs.rawstBerry,
            Gen3ItemIDs.aspearBerry, Gen3ItemIDs.leppaBerry, Gen3ItemIDs.oranBerry, Gen3ItemIDs.persimBerry, Gen3ItemIDs.lumBerry,
            Gen3ItemIDs.sitrusBerry, Gen3ItemIDs.ganlonBerry, Gen3ItemIDs.salacBerry,
            // An NPC pokemon's nature is generated randomly with IVs during gameplay. Therefore, we do not include
            // the flavor berries because, prior to Gen 7, they aren't worth the risk.
            Gen3ItemIDs.apicotBerry, Gen3ItemIDs.lansatBerry, Gen3ItemIDs.starfBerry, Gen3ItemIDs.berryJuice,
            Gen3ItemIDs.whiteHerb, Gen3ItemIDs.mentalHerb
    ));

    public static final List<Integer> generalPurposeItems = Collections.unmodifiableList(Arrays.asList(
            Gen3ItemIDs.brightPowder, Gen3ItemIDs.quickClaw, Gen3ItemIDs.kingsRock, Gen3ItemIDs.focusBand, Gen3ItemIDs.scopeLens,
            Gen3ItemIDs.leftovers, Gen3ItemIDs.shellBell, Gen3ItemIDs.laxIncense
    ));

    public static final Map<Type, List<Integer>> typeBoostingItems = initializeTypeBoostingItems();

    private static Map<Type, List<Integer>> initializeTypeBoostingItems() {
        Map<Type, List<Integer>> map = new HashMap<>();
        map.put(Type.BUG, Collections.singletonList(Gen3ItemIDs.silverPowder));
        map.put(Type.DARK, Collections.singletonList(Gen3ItemIDs.blackGlasses));
        map.put(Type.DRAGON, Collections.singletonList(Gen3ItemIDs.dragonFang));
        map.put(Type.ELECTRIC, Collections.singletonList(Gen3ItemIDs.magnet));
        map.put(Type.FIGHTING, Collections.singletonList(Gen3ItemIDs.blackBelt));
        map.put(Type.FIRE, Collections.singletonList(Gen3ItemIDs.charcoal));
        map.put(Type.FLYING, Collections.singletonList(Gen3ItemIDs.sharpBeak));
        map.put(Type.GHOST, Collections.singletonList(Gen3ItemIDs.spellTag));
        map.put(Type.GRASS, Collections.singletonList(Gen3ItemIDs.miracleSeed));
        map.put(Type.GROUND, Collections.singletonList(Gen3ItemIDs.softSand));
        map.put(Type.ICE, Collections.singletonList(Gen3ItemIDs.neverMeltIce));
        map.put(Type.NORMAL, Collections.singletonList(Gen3ItemIDs.silkScarf));
        map.put(Type.POISON, Collections.singletonList(Gen3ItemIDs.poisonBarb));
        map.put(Type.PSYCHIC, Collections.singletonList(Gen3ItemIDs.twistedSpoon));
        map.put(Type.ROCK, Collections.singletonList(Gen3ItemIDs.hardStone));
        map.put(Type.STEEL, Collections.singletonList(Gen3ItemIDs.metalCoat));
        map.put(Type.WATER, Arrays.asList(Gen3ItemIDs.mysticWater, Gen3ItemIDs.seaIncense));
        map.put(null, Collections.emptyList()); // ??? type
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> speciesBoostingItems = initializeSpeciesBoostingItems();

    private static Map<Integer, List<Integer>> initializeSpeciesBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(SpeciesIDs.latias, Collections.singletonList(Gen3ItemIDs.soulDew));
        map.put(SpeciesIDs.latios, Collections.singletonList(Gen3ItemIDs.soulDew));
        map.put(SpeciesIDs.clamperl, Arrays.asList(Gen3ItemIDs.deepSeaTooth, Gen3ItemIDs.deepSeaScale));
        map.put(SpeciesIDs.pikachu, Collections.singletonList(Gen3ItemIDs.lightBall));
        map.put(SpeciesIDs.chansey, Collections.singletonList(Gen3ItemIDs.luckyPunch));
        map.put(SpeciesIDs.ditto, Collections.singletonList(Gen3ItemIDs.metalPowder));
        map.put(SpeciesIDs.cubone, Collections.singletonList(Gen3ItemIDs.thickClub));
        map.put(SpeciesIDs.marowak, Collections.singletonList(Gen3ItemIDs.thickClub));
        map.put(SpeciesIDs.farfetchd, Collections.singletonList(Gen3ItemIDs.stick));
        return Collections.unmodifiableMap(map);
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
        table[0x0A] = Type.FIRE;
        table[0x0B] = Type.WATER;
        table[0x0C] = Type.GRASS;
        table[0x0D] = Type.ELECTRIC;
        table[0x0E] = Type.PSYCHIC;
        table[0x0F] = Type.ICE;
        table[0x10] = Type.DRAGON;
        table[0x11] = Type.DARK;
        return table;
    }

    public static byte typeToByte(Type type) {
        if (type == null) {
            return 0x09; // ???-type
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
            return 0x0A;
        case WATER:
            return 0x0B;
        case GRASS:
            return 0x0C;
        case ELECTRIC:
            return 0x0D;
        case PSYCHIC:
            return 0x0E;
        case ICE:
            return 0x0F;
        case DRAGON:
            return 0x10;
        case STEEL:
            return 0x08;
        case DARK:
            return 0x11;
        default:
            return 0; // normal by default
        }
    }

    public static final int nonNeutralEffectivenessCount = 110;

    private static final EvolutionType[] evolutionTypeTable = new EvolutionType[] {
            EvolutionType.HAPPINESS, EvolutionType.HAPPINESS_DAY, EvolutionType.HAPPINESS_NIGHT, EvolutionType.LEVEL,
            EvolutionType.TRADE, EvolutionType.TRADE_ITEM, EvolutionType.STONE, EvolutionType.LEVEL_ATTACK_HIGHER,
            EvolutionType.LEVEL_ATK_DEF_SAME, EvolutionType.LEVEL_DEFENSE_HIGHER, EvolutionType.LEVEL_LOW_PV,
            EvolutionType.LEVEL_HIGH_PV, EvolutionType.LEVEL_CREATE_EXTRA, EvolutionType.LEVEL_IS_EXTRA,
            EvolutionType.LEVEL_HIGH_BEAUTY
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


    public static String getRunningShoesCheckPrefix(int romType) {
        if (romType == Gen3Constants.RomType_Ruby || romType == Gen3Constants.RomType_Sapp) {
            return runningShoesCheckPrefixRS;
        } else if (romType == Gen3Constants.RomType_FRLG) {
            return runningShoesCheckPrefixFRLG;
        } else {
            return runningShoesCheckPrefixE;
        }
    }

    public static final Set<Integer> bannedItems = setupBannedItems();
    private static final Set<Integer> badItemsRSE = setupBadItemsRSE();
    private static final Set<Integer> badItemsFRLG = setupBadItemsFRLG();
    public static final Set<Integer> regularShopItems = setupRegularShopItems();
    public static final Set<Integer> opShopItems = setupOPShopItems();

    private static Set<Integer> setupBannedItems() {
        Set<Integer> set = new HashSet<>();
        set.add(Gen3ItemIDs.oldSeaMap);
        // Key items (+1 unknown item)
        addBetween(set, Gen3ItemIDs.machBike, Gen3ItemIDs.devonScope);
        addBetween(set, Gen3ItemIDs.oaksParcel, Gen3ItemIDs.oldSeaMap);
        // Unknown blank items
        addBetween(set, Gen3ItemIDs.unknown52, Gen3ItemIDs.unknown62);
        addBetween(set, Gen3ItemIDs.unknown87, Gen3ItemIDs.unknown92);
        addBetween(set, Gen3ItemIDs.unknown99, Gen3ItemIDs.unknown102);
        addBetween(set, Gen3ItemIDs.unknown112, Gen3ItemIDs.unknown120);
        addBetween(set, Gen3ItemIDs.unknown176, Gen3ItemIDs.unknown178);
        addBetween(set, Gen3ItemIDs.unknown226, Gen3ItemIDs.unknown253);
        set.addAll(Arrays.asList(Gen3ItemIDs.unknown72, Gen3ItemIDs.unknown82, Gen3ItemIDs.unknown105,
                Gen3ItemIDs.unknown267, Gen3ItemIDs.unknown347, Gen3ItemIDs.unknown348));
        // HMs
        addBetween(set, Gen3ItemIDs.hm01, Gen3ItemIDs.hm08);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItemsRSE() {
        // ban specific pokemon hold items, berries, apricorns, mail
        Set<Integer> set = new HashSet<>(Arrays.asList(Gen3ItemIDs.lightBall, Gen3ItemIDs.oranBerry, Gen3ItemIDs.soulDew));
        addBetween(set, Gen3ItemIDs.orangeMail, Gen3ItemIDs.retroMail); // mail
        addBetween(set, Gen3ItemIDs.figyBerry, Gen3ItemIDs.enigmaBerry); // berries
        addBetween(set, Gen3ItemIDs.luckyPunch, Gen3ItemIDs.stick); // pokemon specific
        addBetween(set, Gen3ItemIDs.redScarf, Gen3ItemIDs.yellowScarf); // contest scarves
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupBadItemsFRLG() {
        Set<Integer> set = new HashSet<>(badItemsRSE);
        // Ban Shoal items and Shards, since they don't do anything
        addBetween(set, Gen3ItemIDs.shoalSalt, Gen3ItemIDs.greenShard);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupRegularShopItems() {
        Set<Integer> set = new HashSet<>();
        addBetween(set, Gen3ItemIDs.ultraBall, Gen3ItemIDs.pokeBall);
        addBetween(set, Gen3ItemIDs.potion, Gen3ItemIDs.revive);
        addBetween(set, Gen3ItemIDs.superRepel, Gen3ItemIDs.repel);
        return Collections.unmodifiableSet(set);
    }

    private static Set<Integer> setupOPShopItems() {
        Set<Integer> set = new HashSet<>();
        set.add(Gen3ItemIDs.rareCandy);
        addBetween(set, Gen3ItemIDs.tinyMushroom, Gen3ItemIDs.bigMushroom);
        addBetween(set, Gen3ItemIDs.pearl, Gen3ItemIDs.nugget);
        set.add(Gen3ItemIDs.luckyEgg);
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
        return romType == Gen3Constants.RomType_FRLG ? badItemsFRLG : badItemsRSE;
    }

    public static void trainerTagsRS(List<Trainer> trs, int romType) {
        // Gym Trainers
        tag(trs, "GYM1", 0x140, 0x141);
        tag(trs, "GYM2", 0x1AA, 0x1A9, 0xB3);
        tag(trs, "GYM3", 0xBF, 0x143, 0xC2, 0x289);
        tag(trs, "GYM4", 0xC9, 0x288, 0xCB, 0x28A, 0xCD);
        tag(trs, "GYM5", 0x47, 0x59, 0x49, 0x5A, 0x48, 0x5B, 0x4A);
        tag(trs, "GYM6", 0x191, 0x28F, 0x28E, 0x194);
        tag(trs, "GYM7", 0xE9, 0xEA, 0xEB, 0xF4, 0xF5, 0xF6);
        tag(trs, "GYM8", 0x82, 0x266, 0x83, 0x12D, 0x81, 0x74, 0x80, 0x265);

        // Gym Leaders
        tag(trs, 0x109, "GYM1-LEADER");
        tag(trs, 0x10A, "GYM2-LEADER");
        tag(trs, 0x10B, "GYM3-LEADER");
        tag(trs, 0x10C, "GYM4-LEADER");
        tag(trs, 0x10D, "GYM5-LEADER");
        tag(trs, 0x10E, "GYM6-LEADER");
        tag(trs, 0x10F, "GYM7-LEADER");
        tag(trs, 0x110, "GYM8-LEADER");
        // Elite 4
        tag(trs, 0x105, "ELITE1");
        tag(trs, 0x106, "ELITE2");
        tag(trs, 0x107, "ELITE3");
        tag(trs, 0x108, "ELITE4");
        tag(trs, 0x14F, "CHAMPION");
        // Brendan
        tag(trs, 0x208, "RIVAL1-2");
        tag(trs, 0x20B, "RIVAL1-0");
        tag(trs, 0x20E, "RIVAL1-1");

        tag(trs, 0x209, "RIVAL2-2");
        tag(trs, 0x20C, "RIVAL2-0");
        tag(trs, 0x20F, "RIVAL2-1");

        tag(trs, 0x20A, "RIVAL3-2");
        tag(trs, 0x20D, "RIVAL3-0");
        tag(trs, 0x210, "RIVAL3-1");

        tag(trs, 0x295, "RIVAL4-2");
        tag(trs, 0x296, "RIVAL4-0");
        tag(trs, 0x297, "RIVAL4-1");

        // May
        tag(trs, 0x211, "RIVAL1-2");
        tag(trs, 0x214, "RIVAL1-0");
        tag(trs, 0x217, "RIVAL1-1");

        tag(trs, 0x212, "RIVAL2-2");
        tag(trs, 0x215, "RIVAL2-0");
        tag(trs, 0x218, "RIVAL2-1");

        tag(trs, 0x213, "RIVAL3-2");
        tag(trs, 0x216, "RIVAL3-0");
        tag(trs, 0x219, "RIVAL3-1");

        tag(trs, 0x298, "RIVAL4-2");
        tag(trs, 0x299, "RIVAL4-0");
        tag(trs, 0x29A, "RIVAL4-1");

        // Wally
        tag(trs, "THEMED:WALLY-STRONG", 0x207, 0x290, 0x291, 0x292, 0x293, 0x294);

        if (romType == RomType_Ruby) {
            tag(trs, "THEMED:MAXIE-LEADER", 0x259, 0x25A);
            tag(trs, "THEMED:COURTNEY-STRONG", 0x257, 0x258);
            tag(trs, "THEMED:TABITHA-STRONG", 0x254, 0x255);
        } else {
            tag(trs, "THEMED:ARCHIE-LEADER", 0x23, 0x22);
            tag(trs, "THEMED:MATT-STRONG", 0x1E, 0x1F);
            tag(trs, "THEMED:SHELLY-STRONG", 0x20, 0x21);
        }

    }

    public static void trainerTagsE(List<Trainer> trs) {
        // Gym Trainers
        tag(trs, "GYM1", 0x140, 0x141, 0x23B);
        tag(trs, "GYM2", 0x1AA, 0x1A9, 0xB3, 0x23C, 0x23D, 0x23E);
        tag(trs, "GYM3", 0xBF, 0x143, 0xC2, 0x289, 0x322);
        tag(trs, "GYM4", 0x288, 0xC9, 0xCB, 0x28A, 0xCA, 0xCC, 0x1F5, 0xCD);
        tag(trs, "GYM5", 0x47, 0x59, 0x49, 0x5A, 0x48, 0x5B, 0x4A);
        tag(trs, "GYM6", 0x192, 0x28F, 0x191, 0x28E, 0x194, 0x323);
        tag(trs, "GYM7", 0xE9, 0xEA, 0xEB, 0xF4, 0xF5, 0xF6, 0x24F, 0x248, 0x247, 0x249, 0x246, 0x23F);
        tag(trs, "GYM8", 0x265, 0x80, 0x1F6, 0x73, 0x81, 0x76, 0x82, 0x12D, 0x83, 0x266);

        // Gym Leaders + Emerald Rematches!
        tag(trs, "GYM1-LEADER", 0x109, 0x302, 0x303, 0x304, 0x305);
        tag(trs, "GYM2-LEADER", 0x10A, 0x306, 0x307, 0x308, 0x309);
        tag(trs, "GYM3-LEADER", 0x10B, 0x30A, 0x30B, 0x30C, 0x30D);
        tag(trs, "GYM4-LEADER", 0x10C, 0x30E, 0x30F, 0x310, 0x311);
        tag(trs, "GYM5-LEADER", 0x10D, 0x312, 0x313, 0x314, 0x315);
        tag(trs, "GYM6-LEADER", 0x10E, 0x316, 0x317, 0x318, 0x319);
        tag(trs, "GYM7-LEADER", 0x10F, 0x31A, 0x31B, 0x31C, 0x31D);
        tag(trs, "GYM8-LEADER", 0x110, 0x31E, 0x31F, 0x320, 0x321);

        // Elite 4
        tag(trs, 0x105, "ELITE1");
        tag(trs, 0x106, "ELITE2");
        tag(trs, 0x107, "ELITE3");
        tag(trs, 0x108, "ELITE4");
        tag(trs, 0x14F, "CHAMPION");

        // Brendan
        tag(trs, 0x208, "RIVAL1-2");
        tag(trs, 0x20B, "RIVAL1-0");
        tag(trs, 0x20E, "RIVAL1-1");

        tag(trs, 0x251, "RIVAL2-2");
        tag(trs, 0x250, "RIVAL2-0");
        tag(trs, 0x257, "RIVAL2-1");

        tag(trs, 0x209, "RIVAL3-2");
        tag(trs, 0x20C, "RIVAL3-0");
        tag(trs, 0x20F, "RIVAL3-1");

        tag(trs, 0x20A, "RIVAL4-2");
        tag(trs, 0x20D, "RIVAL4-0");
        tag(trs, 0x210, "RIVAL4-1");

        tag(trs, 0x295, "RIVAL5-2");
        tag(trs, 0x296, "RIVAL5-0");
        tag(trs, 0x297, "RIVAL5-1");

        // May
        tag(trs, 0x211, "RIVAL1-2");
        tag(trs, 0x214, "RIVAL1-0");
        tag(trs, 0x217, "RIVAL1-1");

        tag(trs, 0x258, "RIVAL2-2");
        tag(trs, 0x300, "RIVAL2-0");
        tag(trs, 0x301, "RIVAL2-1");

        tag(trs, 0x212, "RIVAL3-2");
        tag(trs, 0x215, "RIVAL3-0");
        tag(trs, 0x218, "RIVAL3-1");

        tag(trs, 0x213, "RIVAL4-2");
        tag(trs, 0x216, "RIVAL4-0");
        tag(trs, 0x219, "RIVAL4-1");

        tag(trs, 0x298, "RIVAL5-2");
        tag(trs, 0x299, "RIVAL5-0");
        tag(trs, 0x29A, "RIVAL5-1");

        // Themed
        tag(trs, "THEMED:MAXIE-LEADER", 0x259, 0x25A, 0x2DE);
        tag(trs, "THEMED:TABITHA-STRONG", 0x202, 0x255, 0x2DC);
        tag(trs, "THEMED:ARCHIE-LEADER", 0x22);
        tag(trs, "THEMED:MATT-STRONG", 0x1E);
        tag(trs, "THEMED:SHELLY-STRONG", 0x20, 0x21);
        tag(trs, "THEMED:WALLY-STRONG", 0x207, 0x290, 0x291, 0x292, 0x293, 0x294);

        // Steven
        tag(trs, emMeteorFallsStevenIndex, "UBER");

    }

    public static void trainerTagsFRLG(List<Trainer> trs) {

        // Gym Trainers
        tag(trs, "GYM1", 0x8E);
        tag(trs, "GYM2", 0xEA, 0x96);
        tag(trs, "GYM3", 0xDC, 0x8D, 0x1A7);
        tag(trs, "GYM4", 0x10A, 0x84, 0x109, 0xA0, 0x192, 0x10B, 0x85);
        tag(trs, "GYM5", 0x125, 0x124, 0x120, 0x127, 0x126, 0x121);
        tag(trs, "GYM6", 0x11A, 0x119, 0x1CF, 0x11B, 0x1CE, 0x1D0, 0x118);
        tag(trs, "GYM7", 0xD5, 0xB1, 0xB2, 0xD6, 0xB3, 0xD7, 0xB4);
        tag(trs, "GYM8", 0x129, 0x143, 0x188, 0x190, 0x142, 0x128, 0x191, 0x144);

        // Gym Leaders
        tag(trs, 0x19E, "GYM1-LEADER");
        tag(trs, 0x19F, "GYM2-LEADER");
        tag(trs, 0x1A0, "GYM3-LEADER");
        tag(trs, 0x1A1, "GYM4-LEADER");
        tag(trs, 0x1A2, "GYM5-LEADER");
        tag(trs, 0x1A4, "GYM6-LEADER");
        tag(trs, 0x1A3, "GYM7-LEADER");
        tag(trs, 0x15E, "GYM8-LEADER");

        // Giovanni
        tag(trs, 0x15C, "GIO1-LEADER");
        tag(trs, 0x15D, "GIO2-LEADER");

        // E4 Round 1
        tag(trs, 0x19A, "ELITE1-1");
        tag(trs, 0x19B, "ELITE2-1");
        tag(trs, 0x19C, "ELITE3-1");
        tag(trs, 0x19D, "ELITE4-1");

        // E4 Round 2
        tag(trs, 0x2DF, "ELITE1-2");
        tag(trs, 0x2E0, "ELITE2-2");
        tag(trs, 0x2E1, "ELITE3-2");
        tag(trs, 0x2E2, "ELITE4-2");

        // Rival Battles

        // Initial Rival
        tag(trs, 0x148, "RIVAL1-0");
        tag(trs, 0x146, "RIVAL1-1");
        tag(trs, 0x147, "RIVAL1-2");

        // Route 22 (weak)
        tag(trs, 0x14B, "RIVAL2-0");
        tag(trs, 0x149, "RIVAL2-1");
        tag(trs, 0x14A, "RIVAL2-2");

        // Cerulean
        tag(trs, 0x14E, "RIVAL3-0");
        tag(trs, 0x14C, "RIVAL3-1");
        tag(trs, 0x14D, "RIVAL3-2");

        // SS Anne
        tag(trs, 0x1AC, "RIVAL4-0");
        tag(trs, 0x1AA, "RIVAL4-1");
        tag(trs, 0x1AB, "RIVAL4-2");

        // Pokemon Tower
        tag(trs, 0x1AF, "RIVAL5-0");
        tag(trs, 0x1AD, "RIVAL5-1");
        tag(trs, 0x1AE, "RIVAL5-2");

        // Silph Co
        tag(trs, 0x1B2, "RIVAL6-0");
        tag(trs, 0x1B0, "RIVAL6-1");
        tag(trs, 0x1B1, "RIVAL6-2");

        // Route 22 (strong)
        tag(trs, 0x1B5, "RIVAL7-0");
        tag(trs, 0x1B3, "RIVAL7-1");
        tag(trs, 0x1B4, "RIVAL7-2");

        // E4 Round 1
        tag(trs, 0x1B8, "RIVAL8-0");
        tag(trs, 0x1B6, "RIVAL8-1");
        tag(trs, 0x1B7, "RIVAL8-2");

        // E4 Round 2
        tag(trs, 0x2E5, "RIVAL9-0");
        tag(trs, 0x2E3, "RIVAL9-1");
        tag(trs, 0x2E4, "RIVAL9-2");

    }

    private static void tag(List<Trainer> trainers, int trainerNum, String tag) {
        trainers.get(trainerNum - 1).tag = tag;
    }

    private static void tag(List<Trainer> allTrainers, String tag, int... numbers) {
        for (int num : numbers) {
            allTrainers.get(num - 1).tag = tag;
        }
    }

    public static final HashMap<String, Type> gymAndEliteThemesRS = setupGymAndEliteThemesRS();

    private static HashMap<String, Type> setupGymAndEliteThemesRS() {
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

    public static final HashMap<String, Type> gymAndEliteThemesEm = setupGymAndEliteThemesEm();

    private static HashMap<String, Type> setupGymAndEliteThemesEm() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("CHAMPION", Type.WATER); //Wallace
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
        themeMap.put("GYM8", Type.WATER); //Juan
        return themeMap;
    }

    public static final HashMap<String, Type> gymAndEliteThemesFRLG = setupGymAndEliteThemesFRLG();

    private static HashMap<String, Type> setupGymAndEliteThemesFRLG() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("ELITE1", Type.ICE); //Lorelei
        themeMap.put("ELITE2", Type.FIGHTING); //Bruno
        themeMap.put("ELITE3", Type.GHOST); //Agatha
        themeMap.put("ELITE4", Type.DRAGON); //Lance
        themeMap.put("GYM1", Type.ROCK); //Brock
        themeMap.put("GYM2", Type.WATER); //Misty
        themeMap.put("GYM3", Type.ELECTRIC); //Lt. Surge
        themeMap.put("GYM4", Type.GRASS); //Erika
        themeMap.put("GYM5", Type.POISON); //Koga
        themeMap.put("GYM6", Type.PSYCHIC); //Sabrina
        themeMap.put("GYM7", Type.FIRE); //Blaine
        themeMap.put("GYM8", Type.GROUND); //Giovanni
        return themeMap;
    }

    public static void setMultiBattleStatusEm(List<Trainer> trs) {
        // 25 + 569: Double Battle with Team Aqua Grunts on Mt. Pyre
        // 105 + 237: Double Battle with Hex Maniac Patricia and Psychic Joshua
        // 397 + 508: Double Battle with Dragon Tamer Aaron and Cooltrainer Marley
        // 404 + 654: Double Battle with Bird Keeper Edwardo and Camper Flint
        // 504 + 505: Double Battle with Ninja Boy Jonas and Parasol Lady Kayley
        // 514 + 734: Double Battle with Tabitha and Maxie in Mossdeep Space Center
        // 572 + 573: Double Battle with Sailor Brenden and Battle Girl Lilith
        // 721 + 730: Double Battle with Team Magma Grunts in Team Magma Hideout
        // 848 + 850: Double Battle with Psychic Mariela and Gentleman Everett
        // 855: Steven from the Double Battle in Mossdeep Space Center
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 25, 105, 237, 397, 404, 504, 505, 508, 514,
                569, 572, 573, 654, 721, 730, 734, 848, 850, 855
        );

        // 1 + 124: Potential Double Battle with Hiker Sawyer and Beauty Melissa
        // 3 + 192: Potential Double Battle with Team Aqua Grunts in Team Aqua Hideout
        // 8 + 14: Potential Double Battle with Team Aqua Grunts in Seafloor Cavern
        // 9 + 236 + 247: Potential Double Battle with Pokemon Breeder Gabrielle, Psychic William, and Psychic Kayla
        // 11 + 767: Potential Double Battle with Cooltrainer Marcel and Cooltrainer Cristin
        // 12 + 195: Potential Double Battle with Bird Keeper Alberto and Guitarist Fernando
        // 13 + 106: Potential Double Battle with Collector Ed and Hex Maniac Kindra
        // 15 + 450: Potential Double Battle with Swimmer Declan and Swimmer Grace
        // 18 + 596: Potential Double Battle with Team Aqua Grunts in Weather Institute
        // 28 + 193: Potential Double Battle with Team Aqua Grunts in Team Aqua Hideout
        // 29 + 249: Potential Double Battle with Expert Fredrick and Psychic Jacki
        // 31 + 35 + 145: Potential Double Battles with Black Belt Zander, Hex Maniac Leah, and PokéManiac Mark
        // 33 + 567: Potential Double Battle with Shelly and Team Aqua Grunt in Seafloor Cavern
        // 37 + 715: Potential Double Battle with Aroma Lady Rose and Youngster Deandre
        // 38 + 417: Potential Double Battle with Cooltrainer Felix and Cooltrainer Dianne
        // 57 + 698: Potential Double Battle with Tuber Lola and Tuber Chandler
        // 64 + 491 + 697: Potential Double Battles with Tuber Ricky, Sailor Edmond, and Tuber Hailey
        // 107 + 764: Potential Double Battle with Hex Maniac Tammy and Bug Maniac Cale
        // 108 + 475: Potential Double Battle with Hex Maniac Valerie and Psychic Cedric
        // 115 + 502: Potential Double Battle with Lady Daphne and Pokéfan Annika
        // 118 + 129: Potential Double Battle with Lady Brianna and Beauty Bridget
        // 130 + 301: Potential Double Battle with Beauty Olivia and Pokéfan Bethany
        // 131 + 614: Potential Double Battle with Beauty Tiffany and Lass Crissy
        // 137 + 511: Potential Double Battle with Expert Mollie and Expert Conor
        // 144 + 375: Potential Double Battle with Beauty Thalia and Youngster Demetrius
        // 146 + 579: Potential Double Battle with Team Magma Grunts on Mt. Chimney
        // 160 + 595: Potential Double Battle with Swimmer Roland and Triathlete Isabella
        // 168 + 455: Potential Double Battle with Swimmer Santiago and Swimmer Katie
        // 170 + 460: Potential Double Battle with Swimmer Franklin and Swimmer Debra
        // 171 + 385: Potential Double Battle with Swimmer Kevin and Triathlete Taila
        // 180 + 509: Potential Double Battle with Black Belt Hitoshi and Battle Girl Reyna
        // 182 + 307 + 748 + 749: Potential Double Battles with Black Belt Koichi, Expert Timothy, Triathlete Kyra, and Ninja Boy Jaiden
        // 191 + 649: Potential Double Battle with Guitarist Kirk and Battle Girl Vivian
        // 194 + 802: Potential Double Battle with Guitarist Shawn and Bug Maniac Angelo
        // 201 + 648: Potential Double Battle with Kindler Cole and Cooltrainer Gerald
        // 204 + 501: Potential Double Battle with Kindler Jace and Hiker Eli
        // 217 + 566: Potential Double Battle with Picnicker Autumn and Triathlete Julio
        // 232 + 701: Potential Double Battle with Psychic Edward and Triathlete Alyssa
        // 233 + 246: Potential Double Battle with Psychic Preston and Psychic Maura
        // 234 + 244 + 575 + 582: Potential Double Battles with Psychic Virgil, Psychic Hannah, Hex Maniac Sylvia, and Gentleman Nate
        // 235 + 245: Potential Double Battle with Psychic Blake and Psychic Samantha
        // 248 + 849: Potential Double Battle with Psychic Alexis and Psychic Alvaro
        // 273 + 605: Potential Double Battle with School Kid Jerry and Lass Janice
        // 302 + 699: Potential Double Battle with Pokéfan Isabel and Pokéfan Kaleb
        // 321 + 571: Potential Double Battle with Youngster Tommy and Hiker Marc
        // 324 + 325: Potential Double Battle with Cooltrainer Quincy and Cooltrainer Katelynn
        // 345 + 742: Potential Double Battle with Fisherman Carter and Bird Keeper Elijah
        // 377 + 459: Potential Double Battle with Triathlete Pablo and Swimmer Sienna
        // 383 + 576: Potential Double Battle with Triathlete Isobel and Swimmer Leonardo
        // 400 + 761: Potential Double Battle with Bird Keeper Phil and Parasol Lady Rachel
        // 401 + 655: Potential Double Battle with Bird Keeper Jared and Picnicker Ashley
        // 403 + 506: Potential Double Battle with Bird Keeper Presley and Expert Auron
        // 413 + 507: Potential Double Battle with Bird Keeper Alex and Sailor Kelvin
        // 415 + 759: Potential Double Battle with Ninja Boy Yasu and Guitarist Fabian
        // 416 + 760: Potential Double Battle with Ninja Boy Takashi and Kindler Dayton
        // 418 + 547: Potential Double Battle with Tuber Jani and Ruin Maniac Garrison
        // 420 + 710 + 711: Potential Double Battles with Ninja Boy Lung, Camper Lawrence, and PokéManiac Wyatt
        // 436 + 762: Potential Double Battle with Parasol Lady Angelica and Cooltrainer Leonel
        // 445 + 739: Potential Double Battle with Swimmer Beth and Triathlete Camron
        // 464 + 578: Potential Double Battle with Swimmer Carlee and Swimmer Harrison
        // 494 + 495: Potential Double Battle with Sailor Phillip and Sailor Leonard (S.S. Tidal)
        // 503 + 539: Potential Double Battle with Cooltrainer Jazmyn and Bug Catcher Davis
        // 512 + 700: Potential Double Battle with Collector Edwin and Guitarist Joseph
        // 513 + 752: Potential Double Battle with Collector Hector and Psychic Marlene
        // 540 + 546: Potential Double Battle with Cooltrainer Mitchell and Cooltrainer Halle
        // 577 + 674: Potential Double Battle with Cooltrainer Athena and Bird Keeper Aidan
        // 580 + 676: Potential Double Battle with Swimmer Clarence and Swimmer Tisha
        // 583 + 584 + 585 + 591: Potential Double Battles with Hex Maniac Kathleen, Gentleman Clifford, Psychic Nicholas, and Psychic Macey
        // 594 + 733: Potential Double Battle with Expert Paxton and Cooltrainer Darcy
        // 598 + 758: Potential Double Battle with Cooltrainer Jonathan and Expert Makayla
        // 629 + 712: Potential Double Battle with Hiker Lucas and Picnicker Angelina
        // 631 + 753 + 754: Potential Double Battles with Hiker Clark, Hiker Devan, and Youngster Johnson
        // 653 + 763: Potential Double Battle with Ninja Boy Riley and Battle Girl Callie
        // 694 + 695: Potential Double Battle with Rich Boy Dawson and Lady Sarah
        // 702 + 703: Potential Double Battle with Guitarist Marcos and Black Belt Rhett
        // 704 + 705: Potential Double Battle with Camper Tyron and Aroma Lady Celina
        // 706 + 707: Potential Double Battle with Picnicker Bianca and Kindler Hayden
        // 708 + 709: Potential Double Battle with Picnicker Sophie and Bird Keeper Coby
        // 713 + 714: Potential Double Battle with Fisherman Kai and Picnicker Charlotte
        // 719 + 720: Potential Double Battle with Team Magma Grunts in Team Magma Hideout
        // 727 + 728: Potential Double Battle with Team Magma Grunts in Team Magma Hideout
        // 735 + 736: Potential Double Battle with Swimmer Pete and Swimmer Isabelle
        // 737 + 738: Potential Double Battle with Ruin Maniac Andres and Bird Keeper Josue
        // 740 + 741: Potential Double Battle with Sailor Cory and Cooltrainer Carolina
        // 743 + 744 + 745: Potential Double Battles with Picnicker Celia, Ruin Maniac Bryan, and Camper Branden
        // 746 + 747: Potential Double Battle with Kindler Bryant and Aroma Lady Shayla
        // 750 + 751: Potential Double Battle with Psychic Alix and Battle Girl Helene
        // 755 + 756 + 757: Potential Double Battles with Triathlete Melina, Psychic Brandi, and Battle Girl Aisha
        // 765 + 766: Potential Double Battle with Pokémon Breeder Myles and Pokémon Breeder Pat
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 1, 3, 8, 9, 11, 12, 13, 14, 15, 18, 28,
                29, 31, 33, 35, 37, 38, 57, 64, 106, 107, 108, 115, 118, 124, 129, 130, 131, 137, 144, 145, 146, 160,
                168, 170, 171, 180, 182, 191, 192, 193, 194, 195, 201, 204, 217, 232, 233, 234, 235, 236, 244, 245, 246,
                247, 248, 249, 273, 301, 302, 307, 321, 324, 325, 345, 375, 377, 383, 385, 400, 401, 403, 413, 415, 416,
                417, 418, 420, 436, 445, 450, 455, 459, 460, 464, 475, 491, 494, 495, 501, 502, 503, 506, 507, 509, 511,
                512, 513, 539, 540, 546, 547, 566, 567, 571, 575, 576, 577, 578, 579, 580, 582, 583, 584, 585, 591, 594,
                595, 596, 598, 605, 614, 629, 631, 648, 649, 653, 655, 674, 676, 694, 695, 697, 698, 699, 700, 701, 702,
                703, 704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714, 715, 719, 720, 727, 728, 733, 735, 736, 737,
                738, 739, 740, 741, 742, 743, 744, 745, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 758,
                759, 760, 761, 762, 763, 764, 765, 766, 767, 802, 849
        );
    }

    private static void setMultiBattleStatus(List<Trainer> allTrainers, Trainer.MultiBattleStatus status, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).multiBattleStatus = status;
            }
        }
    }

    private static final int[] rsPostGameEncounterAreas = new int[] {
            81, 82, 83, //SKY PILLAR
            153 //Mirage Island - technically not post-game, but not exactly part of the game either
    };

    private static final int[] emPostGameEncounterAreas = new int[] {
            174, 177, 178, //SKY PILLAR
            199, 200, 201, 202, 203, 204, 205, 206, 207, //ALTERING CAVE
            196, //DESERT UNDERPASS
            95, //Mirage Island - technically not post-game, but hardly "local" since it almost never exists
    };

    private static final int[] frlgPostGameEncounterAreas = new int[] {
            33, 34, 35, 36, 37, 38, 39, 40, 41, 42, //CERULEAN CAVE
            118, //THREE ISLE PORT
            214, 215, //FOUR ISLAND
            82, 83, 84, 85, 86, 87, 88, 89, //ICEFALL CAVE
            216, 217, //FIVE ISLAND
            119, 120, //RESORT GORGEOUS
            91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, //LOST CAVE
            121, 122, //WATER LABYRINTH
            123, 124, 125, //FIVE ISLE MEADOW
            126, 127, 128, //MEMORIAL PILLAR
            133, 134, 135, //WATER PATH
            136, 137, 138, //RUIN VALLEY
            131, 132, //GREEN PATH
            90, //PATTERN BUSH
            129, 130, //OUTCAST ISLAND
            218, 219, 220, 221, 222, 223, 224, 225, 226, //ALTERING CAVE
            144, 145, //TANOBY RUINS
            0, 1, 2, 3, 4, 5, 6, //the Tanoby Chambers
            142, 143, //SEVAULT CANYON
            141, //CANYON ENTRANCE
            139, 140, //TRAINER TOWER
    };

    public static final List<String> locationTagsRS = Collections.unmodifiableList(Arrays.asList(
            "PETALBURG CITY", "PETALBURG CITY",
            "SLATEPORT CITY", "SLATEPORT CITY",
            "LILYCOVE CITY", "LILYCOVE CITY",
            "MOSSDEEP CITY", "MOSSDEEP CITY",
            "SOOTOPOLIS CITY", "SOOTOPOLIS CITY",
            "EVER GRANDE CITY", "EVER GRANDE CITY",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "RUSTURF TUNNEL",
            "GRANITE CAVE", "GRANITE CAVE", "GRANITE CAVE", "GRANITE CAVE", "GRANITE CAVE",
            "PETALBURG WOODS",
            "JAGGED PASS",
            "FIERY PATH",
            "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN",
            "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD",
            "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE",
            "SHOAL CAVE", "SHOAL CAVE",
            "NEW MAUVILLE", "NEW MAUVILLE",
            "ABANDONED SHIP", "ABANDONED SHIP", "ABANDONED SHIP", "ABANDONED SHIP",
            "SKY PILLAR", "SKY PILLAR", "SKY PILLAR",
            "ROUTE 101",
            "ROUTE 102", "ROUTE 102", "ROUTE 102",
            "ROUTE 103", "ROUTE 103", "ROUTE 103",
            "ROUTE 104", "ROUTE 104", "ROUTE 104",
            "ROUTE 105", "ROUTE 105",
            "ROUTE 106", "ROUTE 106",
            "ROUTE 107", "ROUTE 107",
            "ROUTE 108", "ROUTE 108",
            "ROUTE 109", "ROUTE 109",
            "ROUTE 110", "ROUTE 110", "ROUTE 110",
            "ROUTE 111", "ROUTE 111", "ROUTE 111", "ROUTE 111",
            "ROUTE 112",
            "ROUTE 113",
            "ROUTE 114", "ROUTE 114", "ROUTE 114", "ROUTE 114",
            "ROUTE 115", "ROUTE 115", "ROUTE 115",
            "ROUTE 116",
            "ROUTE 117", "ROUTE 117", "ROUTE 117",
            "ROUTE 118", "ROUTE 118", "ROUTE 118",
            "ROUTE 119", "ROUTE 119", "ROUTE 119",
            "ROUTE 120", "ROUTE 120", "ROUTE 120",
            "ROUTE 121", "ROUTE 121", "ROUTE 121",
            "ROUTE 122", "ROUTE 122",
            "ROUTE 123", "ROUTE 123", "ROUTE 123",
            "ROUTE 124", "ROUTE 124",
            "ROUTE 125", "ROUTE 125",
            "ROUTE 126", "ROUTE 126",
            "ROUTE 127", "ROUTE 127",
            "ROUTE 128", "ROUTE 128",
            "ROUTE 129", "ROUTE 129",
            "ROUTE 130", "ROUTE 130", "ROUTE 130",
            "ROUTE 131", "ROUTE 131",
            "ROUTE 132", "ROUTE 132",
            "ROUTE 133", "ROUTE 133",
            "ROUTE 134", "ROUTE 134",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "SAFARI ZONE", "SAFARI ZONE",
            "DEWFORD TOWN", "DEWFORD TOWN",
            "PACIFIDLOG TOWN", "PACIFIDLOG TOWN",
            "UNDERWATER", "UNDERWATER"
    ));

    public static final List<String> locationTagsEm = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 101",
            "ROUTE 102", "ROUTE 102", "ROUTE 102",
            "ROUTE 103", "ROUTE 103", "ROUTE 103",
            "ROUTE 104", "ROUTE 104", "ROUTE 104",
            "ROUTE 105", "ROUTE 105",
            "ROUTE 110", "ROUTE 110", "ROUTE 110",
            "ROUTE 111", "ROUTE 111", "ROUTE 111", "ROUTE 111",
            "ROUTE 112",
            "ROUTE 113",
            "ROUTE 114", "ROUTE 114", "ROUTE 114", "ROUTE 114",
            "ROUTE 116",
            "ROUTE 117", "ROUTE 117", "ROUTE 117",
            "ROUTE 118", "ROUTE 118", "ROUTE 118",
            "ROUTE 124", "ROUTE 124",
            "PETALBURG WOODS",
            "RUSTURF TUNNEL",
            "GRANITE CAVE", "GRANITE CAVE",
            "MT. PYRE",
            "VICTORY ROAD",
            "SAFARI ZONE",
            "UNDERWATER",
            "ABANDONED SHIP", "ABANDONED SHIP",
            "GRANITE CAVE", "GRANITE CAVE",
            "FIERY PATH",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "JAGGED PASS",
            "ROUTE 106", "ROUTE 106",
            "ROUTE 107", "ROUTE 107",
            "ROUTE 108", "ROUTE 108",
            "ROUTE 109", "ROUTE 109",
            "ROUTE 115", "ROUTE 115", "ROUTE 115",
            "NEW MAUVILLE",
            "ROUTE 119", "ROUTE 119", "ROUTE 119",
            "ROUTE 120", "ROUTE 120", "ROUTE 120",
            "ROUTE 121", "ROUTE 121", "ROUTE 121",
            "ROUTE 122", "ROUTE 122",
            "ROUTE 123", "ROUTE 123", "ROUTE 123",
            "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE", "MT. PYRE",
            "GRANITE CAVE",
            "ROUTE 125", "ROUTE 125",
            "ROUTE 126", "ROUTE 126",
            "ROUTE 127", "ROUTE 127",
            "ROUTE 128", "ROUTE 128",
            "ROUTE 129", "ROUTE 129",
            "ROUTE 130", "ROUTE 130", "ROUTE 130",
            "ROUTE 131", "ROUTE 131",
            "ROUTE 132", "ROUTE 132",
            "ROUTE 133", "ROUTE 133",
            "ROUTE 134", "ROUTE 134",
            "ABANDONED SHIP", "ABANDONED SHIP",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN", "SEAFLOOR CAVERN",
            "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN", "CAVE OF ORIGIN",
            "NEW MAUVILLE",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "SAFARI ZONE",
            "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "METEOR FALLS", "METEOR FALLS", "METEOR FALLS",
            "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE", "SHOAL CAVE",
            "SHOAL CAVE",
            "LILYCOVE CITY", "LILYCOVE CITY",
            "DEWFORD TOWN", "DEWFORD TOWN",
            "SLATEPORT CITY", "SLATEPORT CITY",
            "MOSSDEEP CITY", "MOSSDEEP CITY",
            "PACIFIDLOG TOWN", "PACIFIDLOG TOWN",
            "EVER GRANDE CITY", "EVER GRANDE CITY",
            "PETALBURG CITY", "PETALBURG CITY",
            "UNDERWATER",
            "SHOAL CAVE",
            "SKY PILLAR",
            "SOOTOPOLIS CITY", "SOOTOPOLIS CITY",
            "SKY PILLAR", "SKY PILLAR",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT", "MAGMA HIDEOUT",
            "MAGMA HIDEOUT", "MAGMA HIDEOUT",
            "MIRAGE TOWER", "MIRAGE TOWER", "MIRAGE TOWER", "MIRAGE TOWER",
            "DESERT UNDERPASS",
            "ARTISAN CAVE", "ARTISAN CAVE",
            "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE",
            "ALTERING CAVE", "ALTERING CAVE", "ALTERING CAVE",
            "METEOR FALLS"
    ));

    public static final List<String> locationTagsFRLG = Collections.unmodifiableList(Arrays.asList(
            "TANOBY CHAMBERS", "TANOBY CHAMBERS", "TANOBY CHAMBERS", "TANOBY CHAMBERS", "TANOBY CHAMBERS",
            "TANOBY CHAMBERS", "TANOBY CHAMBERS",
            "VIRIDIAN FOREST",
            "MT. MOON", "MT. MOON", "MT. MOON",
            "S.S. ANNE", "S.S. ANNE",
            "DIGLETT'S CAVE",
            "VICTORY ROAD", "VICTORY ROAD", "VICTORY ROAD",
            "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE",
            "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE", "CERULEAN CAVE",
            "ROCK TUNNEL", "ROCK TUNNEL", "ROCK TUNNEL",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER",
            "POWER PLANT",
            "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER",
            "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER", "MT. EMBER",
            "MT. EMBER", "MT. EMBER",
            "BERRY FOREST", "BERRY FOREST", "BERRY FOREST",
            "ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE","ICEFALL CAVE",
            "ICEFALL CAVE",
            "PATTERN BUSH",
            "LOST CAVE", "LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE",
            "LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE","LOST CAVE",
            "KINDLE ROAD","KINDLE ROAD","KINDLE ROAD","KINDLE ROAD",
            "TREASURE BEACH","TREASURE BEACH","TREASURE BEACH",
            "CAPE BRINK","CAPE BRINK","CAPE BRINK",
            "BOND BRIDGE", "BOND BRIDGE", "BOND BRIDGE",
            "THREE ISLE PORT",
            "RESORT GORGEOUS", "RESORT GORGEOUS",
            "WATER LABYRINTH", "WATER LABYRINTH",
            "FIVE ISLE MEADOW","FIVE ISLE MEADOW","FIVE ISLE MEADOW",
            "MEMORIAL PILLAR","MEMORIAL PILLAR","MEMORIAL PILLAR",
            "OUTCAST ISLAND","OUTCAST ISLAND",
            "GREEN PATH", "GREEN PATH",
            "WATER PATH", "WATER PATH", "WATER PATH",
            "RUIN VALLEY","RUIN VALLEY","RUIN VALLEY",
            "TRAINER TOWER", "TRAINER TOWER",
            "CANYON ENTRANCE",
            "SEVAULT CANYON", "SEVAULT CANYON",
            "TANOBY RUINS", "TANOBY RUINS",
            "ROUTE 1",
            "ROUTE 2",
            "ROUTE 3",
            "ROUTE 4", "ROUTE 4", "ROUTE 4",
            "ROUTE 5",
            "ROUTE 6", "ROUTE 6", "ROUTE 6",
            "ROUTE 7",
            "ROUTE 8",
            "ROUTE 9",
            "ROUTE 10", "ROUTE 10", "ROUTE 10",
            "ROUTE 11", "ROUTE 11", "ROUTE 11",
            "ROUTE 12", "ROUTE 12", "ROUTE 12",
            "ROUTE 13", "ROUTE 13", "ROUTE 13",
            "ROUTE 14",
            "ROUTE 15",
            "ROUTE 16",
            "ROUTE 17",
            "ROUTE 18",
            "ROUTE 19", "ROUTE 19",
            "ROUTE 20", "ROUTE 20",
            "ROUTE 21", "ROUTE 21", "ROUTE 21", "ROUTE 21", "ROUTE 21", "ROUTE 21",
            "ROUTE 22", "ROUTE 22", "ROUTE 22",
            "ROUTE 23", "ROUTE 23", "ROUTE 23",
            "ROUTE 24", "ROUTE 24", "ROUTE 24",
            "ROUTE 25", "ROUTE 25", "ROUTE 25",
            "PALLET TOWN", "PALLET TOWN",
            "VIRIDIAN CITY", "VIRIDIAN CITY",
            "CERULEAN CITY", "CERULEAN CITY",
            "VERMILION CITY", "VERMILION CITY",
            "CELADON CITY", "CELADON CITY",
            "FUCHSIA CITY", "FUCHSIA CITY",
            "CINNABAR ISLAND", "CINNABAR ISLAND",
            "ONE ISLAND", "ONE ISLAND",
            "FOUR ISLAND", "FOUR ISLAND",
            "FIVE ISLAND", "FIVE ISLAND",
            "ALTERING CAVE","ALTERING CAVE","ALTERING CAVE","ALTERING CAVE","ALTERING CAVE","ALTERING CAVE",
            "ALTERING CAVE","ALTERING CAVE","ALTERING CAVE"
    ));

    private static final List<String> locationTagsTraverseOrderRSE = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 101", "ROUTE 103",
            "ROUTE 102", "PETALBURG CITY", "ROUTE 104", "PETALBURG WOODS", "ROUTE 116", "RUSTURF TUNNEL",
            "DEWFORD TOWN", "GRANITE CAVE", "ROUTE 109", "SLATEPORT CITY", "ROUTE 110", "ALTERING CAVE",
            "MAUVILLE CITY", "NEW MAUVILLE", "ROUTE 117", "ROUTE 111", "MIRAGE TOWER", "ROUTE 112", "FIERY PATH",
            "ROUTE 113", "ROUTE 114", "METEOR FALLS", "ROUTE 115", "JAGGED PASS", "ROUTE 118", "ROUTE 119", "ROUTE 120",
            "ROUTE 121", "SAFARI ZONE", "LILYCOVE CITY", "ROUTE 122", "MT. PYRE", "ROUTE 123", "MAGMA HIDEOUT",
            "ROUTE 124", "MOSSDEEP CITY", "UNDERWATER", "ROUTE 125", "SHOAL CAVE", "ROUTE 127", "ROUTE 128",
            "SEAFLOOR CAVERN", "ROUTE 126", "SOOTOPOLIS CITY", "CAVE OF ORIGIN", "ROUTE 129", "ROUTE 130", "ROUTE 131",
            "PACIFIDLOG TOWN", "ROUTE 132", "ROUTE 133", "ROUTE 134", "ROUTE 105", "ROUTE 106", "ROUTE 107",
            "ROUTE 108", "ABANDONED SHIP", "EVER GRANDE CITY", "VICTORY ROAD", "SKY PILLAR", "DESERT UNDERPASS",
            "ARTISAN CAVE"
    ));

    private static final List<String> locationTagsTraverseOrderFRLG = Collections.unmodifiableList(Arrays.asList(
            "PALLET TOWN", "ROUTE 1",
            "VIRIDIAN CITY", "ROUTE 22", "ROUTE 2", "VIRIDIAN FOREST", "ROUTE 3", "MT. MOON", "ROUTE 4",
            "CERULEAN CITY", "ROUTE 24", "ROUTE 25", "ROUTE 5", "ROUTE 6", "VERMILION CITY", "S.S. ANNE", "ROUTE 11",
            "DIGLETT'S CAVE", "ROUTE 9", "ROUTE 10", "ROCK TUNNEL", "ROUTE 8", "ROUTE 7", "CELADON CITY",
            "POKEMON TOWER", "ROUTE 16", "ROUTE 17", "ROUTE 18", "FUCHSIA CITY", "SAFARI ZONE", "ROUTE 15", "ROUTE 14",
            "ROUTE 13", "ROUTE 12", "POWER PLANT", "ROUTE 19", "ROUTE 20", "SEAFOAM ISLANDS", "CINNABAR ISLAND",
            "POKEMON MANSION", "ROUTE 21", "ONE ISLAND", "TREASURE BEACH", "THREE ISLE PORT", "BOND BRIDGE",
            "BERRY FOREST", "CAPE BRINK", "KINDLE ROAD", "MT. EMBER", "ROUTE 23", "VICTORY ROAD", "FOUR ISLAND",
            "ICEFALL CAVE", "WATER PATH", "GREEN PATH", "PATTERN BUSH", "RUIN VALLEY", "OUTCAST ISLAND",
            "ALTERING CAVE", "FIVE ISLAND", "FIVE ISLE MEADOW", "MEMORIAL PILLAR", "WATER LABYRINTH", "RESORT GORGEOUS",
            "LOST CAVE", "TRAINER TOWER", "CANYON ENTRANCE", "SEVAULT CANYON", "TANOBY RUINS", "TANOBY CHAMBERS",
            "CERULEAN CAVE"
    ));

    public static List<String> getLocationTagsTraverseOrder(int romType) {
        return romType == RomType_FRLG ? locationTagsTraverseOrderFRLG : locationTagsTraverseOrderRSE;
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
            case 0:
            case 1:
                locationTags = locationTagsRS;
                postGameAreas = rsPostGameEncounterAreas;
                break;
            case 2:
                locationTags = locationTagsEm;
                postGameAreas = emPostGameEncounterAreas;
                break;
            case 3:
                locationTags = locationTagsFRLG;
                postGameAreas = frlgPostGameEncounterAreas;
                break;
            default:
                throw new IllegalStateException("Unexpected value for romType: " + romType);
        }
        tagEncounterAreas(encounterAreas, locationTags, postGameAreas);
    }

    public static final Map<Integer,Integer> balancedItemPrices = Stream.of(new Integer[][] {
            {Gen3ItemIDs.masterBall, 3000},
            {Gen3ItemIDs.safariBall, 500},
            {Gen3ItemIDs.freshWater, 400},
            {Gen3ItemIDs.sodaPop, 600},
            {Gen3ItemIDs.lemonade, 700},
            {Gen3ItemIDs.moomooMilk, 800},
            {Gen3ItemIDs.energyPowder, 400},
            {Gen3ItemIDs.energyRoot, 1100},
            {Gen3ItemIDs.ether, 3000},
            {Gen3ItemIDs.maxEther, 4500},
            {Gen3ItemIDs.elixir, 15000},
            {Gen3ItemIDs.maxElixir, 18000},
            {Gen3ItemIDs.lavaCookie, 450},
            {Gen3ItemIDs.blueFlute, 20},
            {Gen3ItemIDs.yellowFlute, 20},
            {Gen3ItemIDs.redFlute, 20},
            {Gen3ItemIDs.blackFlute, 20},
            {Gen3ItemIDs.whiteFlute, 20},
            {Gen3ItemIDs.sacredAsh, 10000},
            {Gen3ItemIDs.redShard, 400},
            {Gen3ItemIDs.blueShard, 400},
            {Gen3ItemIDs.yellowShard, 400},
            {Gen3ItemIDs.greenShard, 400},
            {Gen3ItemIDs.rareCandy, 10000},
            {Gen3ItemIDs.ppMax, 24900},
            {Gen3ItemIDs.sunStone, 3000},
            {Gen3ItemIDs.moonStone, 3000},
            {Gen3ItemIDs.fireStone, 3000},
            {Gen3ItemIDs.thunderstone, 3000},
            {Gen3ItemIDs.waterStone, 3000},
            {Gen3ItemIDs.leafStone, 3000},
            {Gen3ItemIDs.heartScale, 5000},
            {Gen3ItemIDs.retroMail, 50},

            {Gen3ItemIDs.cheriBerry, 200},
            {Gen3ItemIDs.chestoBerry, 250},
            {Gen3ItemIDs.pechaBerry, 100},
            {Gen3ItemIDs.rawstBerry, 250},
            {Gen3ItemIDs.aspearBerry, 250},
            {Gen3ItemIDs.leppaBerry, 3000},
            {Gen3ItemIDs.oranBerry, 50},
            {Gen3ItemIDs.persimBerry, 200},
            {Gen3ItemIDs.lumBerry, 500},
            {Gen3ItemIDs.sitrusBerry, 500},
            {Gen3ItemIDs.figyBerry, 100},
            {Gen3ItemIDs.wikiBerry, 100},
            {Gen3ItemIDs.magoBerry, 100},
            {Gen3ItemIDs.aguavBerry, 100},
            {Gen3ItemIDs.iapapaBerry, 100},
            {Gen3ItemIDs.razzBerry, 500},
            {Gen3ItemIDs.blukBerry, 500},
            {Gen3ItemIDs.nanabBerry, 500},
            {Gen3ItemIDs.wepearBerry, 500},
            {Gen3ItemIDs.pinapBerry, 500},
            {Gen3ItemIDs.pomegBerry, 500},
            {Gen3ItemIDs.kelpsyBerry, 500},
            {Gen3ItemIDs.qualotBerry, 500},
            {Gen3ItemIDs.hondewBerry, 500},
            {Gen3ItemIDs.grepaBerry, 500},
            {Gen3ItemIDs.tamatoBerry, 500},
            {Gen3ItemIDs.cornnBerry, 500},
            {Gen3ItemIDs.magostBerry, 500},
            {Gen3ItemIDs.rabutaBerry, 500},
            {Gen3ItemIDs.nomelBerry, 500},
            {Gen3ItemIDs.spelonBerry, 500},
            {Gen3ItemIDs.pamtreBerry, 500},
            {Gen3ItemIDs.watmelBerry, 500},
            {Gen3ItemIDs.durinBerry, 500},
            {Gen3ItemIDs.belueBerry, 500},
            {Gen3ItemIDs.liechiBerry, 1000},
            {Gen3ItemIDs.ganlonBerry, 1000},
            {Gen3ItemIDs.salacBerry, 1000},
            {Gen3ItemIDs.petayaBerry, 1000},
            {Gen3ItemIDs.apicotBerry, 1000},
            {Gen3ItemIDs.lansatBerry, 1000},
            {Gen3ItemIDs.starfBerry, 1000},
            {Gen3ItemIDs.enigmaBerry, 1000},
            {Gen3ItemIDs.brightPowder, 3000},

            {Gen3ItemIDs.whiteHerb, 1000},
            {Gen3ItemIDs.expShare, 6000},
            {Gen3ItemIDs.quickClaw, 4500},
            {Gen3ItemIDs.sootheBell, 1000},
            {Gen3ItemIDs.mentalHerb, 1000},
            {Gen3ItemIDs.choiceBand, 10000},
            {Gen3ItemIDs.kingsRock, 5000},
            {Gen3ItemIDs.silverPowder, 2000},
            {Gen3ItemIDs.amuletCoin, 15000},
            {Gen3ItemIDs.cleanseTag, 1000},
            {Gen3ItemIDs.deepSeaTooth, 3000},
            {Gen3ItemIDs.deepSeaScale, 3000},
            {Gen3ItemIDs.focusBand, 3000},
            {Gen3ItemIDs.luckyEgg, 10000},
            {Gen3ItemIDs.scopeLens, 5000},
            {Gen3ItemIDs.metalCoat, 3000},
            {Gen3ItemIDs.leftovers, 10000},
            {Gen3ItemIDs.dragonScale, 3000},
            {Gen3ItemIDs.softSand, 2000},
            {Gen3ItemIDs.hardStone, 2000},
            {Gen3ItemIDs.miracleSeed, 2000},
            {Gen3ItemIDs.blackGlasses, 2000},
            {Gen3ItemIDs.blackBelt, 2000},
            {Gen3ItemIDs.magnet, 2000},
            {Gen3ItemIDs.mysticWater, 2000},
            {Gen3ItemIDs.sharpBeak, 2000},
            {Gen3ItemIDs.poisonBarb, 2000},
            {Gen3ItemIDs.neverMeltIce, 2000},
            {Gen3ItemIDs.spellTag, 2000},
            {Gen3ItemIDs.twistedSpoon, 2000},
            {Gen3ItemIDs.charcoal, 2000},
            {Gen3ItemIDs.dragonFang, 2000},
            {Gen3ItemIDs.silkScarf, 2000},
            {Gen3ItemIDs.upGrade, 3000},
            {Gen3ItemIDs.shellBell, 6000},
            {Gen3ItemIDs.seaIncense, 2000},
            {Gen3ItemIDs.laxIncense, 3000},

            {Gen3ItemIDs.tm04, 1500},
            {Gen3ItemIDs.tm07, 2000},
            {Gen3ItemIDs.tm08, 1500},
            {Gen3ItemIDs.tm09, 2000},
            {Gen3ItemIDs.tm10, 2000},
            {Gen3ItemIDs.tm12, 1500},
            {Gen3ItemIDs.tm16, 2000},
            {Gen3ItemIDs.tm17, 2000},
            {Gen3ItemIDs.tm20, 2000},
            {Gen3ItemIDs.tm29, 3000},
            {Gen3ItemIDs.tm32, 1000},
            {Gen3ItemIDs.tm33, 2000},
            {Gen3ItemIDs.tm36, 3000},
            {Gen3ItemIDs.tm39, 2000},
            {Gen3ItemIDs.tm41, 1500},
            {Gen3ItemIDs.tm43, 2000},
            {Gen3ItemIDs.tm46, 2000},
            {Gen3ItemIDs.tm49, 1500},
            {Gen3ItemIDs.tm50, 5500}
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
}
