package com.dabomstew.pkrandom.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen1Constants.java - Constants for Red/Green/Blue/Yellow              --*/
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

import java.util.*;

import com.dabomstew.pkrandom.gamedata.*;

public class Gen1Constants {

    public static final int Type_RB = 0;
    public static final int Type_Yellow = 1;

    public static final int baseStatsEntrySize = 0x1C;

	public static final int bsHPOffset = 1, bsAttackOffset = 2, bsDefenseOffset = 3, bsSpeedOffset = 4,
			bsSpecialOffset = 5, bsPrimaryTypeOffset = 6, bsSecondaryTypeOffset = 7, bsCatchRateOffset = 8,
			bsExpYieldOffset = 9, bsFrontImageDimensionsOffset = 10, bsFrontImagePointerOffset = 11,
            bsBackImagePointerOffset = 13, bsLevel1MovesOffset = 15, bsGrowthCurveOffset = 19, bsTMHMCompatOffset = 20;

    public static final int encounterTableEnd = 0xFFFF, encounterTableSize = 10, yellowSuperRodTableSize = 4;

    public static final int trainerClassCount = 47;

    public static final byte trainerDataTerminator = 0x00;

    public static final int champRivalOffsetFromGymLeaderMoves = 0x44;

    public static final int pokemonCount = 151;

    public static final int tmCount = 50, hmCount = 5;

    public static final int[] gymLeaderTMs = new int[] { 34, 11, 24, 21, 6, 46, 38, 27 };

    public static final int[] tclassesCounts = new int[] { 21, 47 };

    public static final List<Integer> singularTrainers = Arrays.asList(28, 32, 33, 34, 35, 36, 37, 38, 39, 43, 45, 46);

    public static final List<Integer> bannedMovesWithXAccBanned = Arrays.asList(
            MoveIDs.sonicBoom, MoveIDs.dragonRage, MoveIDs.spore);

    public static final List<Integer> bannedMovesWithoutXAccBanned = Arrays.asList(
            MoveIDs.sonicBoom, MoveIDs.dragonRage, MoveIDs.spore, MoveIDs.hornDrill, MoveIDs.fissure, MoveIDs.guillotine);

    // ban transform because of Transform assumption glitch
    public static final List<Integer> bannedLevelupMoves = Collections.singletonList(MoveIDs.transform);

    public static final List<Integer> fieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport);

    public static final int damagePoison20PercentEffect = 2, damageAbsorbEffect = 3, damageBurn10PercentEffect = 4,
            damageFreeze10PercentEffect = 5, damageParalyze10PercentEffect = 6, dreamEaterEffect = 8,
            noDamageAtkPlusOneEffect = 10, noDamageDefPlusOneEffect = 11, noDamageSpecialPlusOneEffect = 13,
            noDamageEvasionPlusOneEffect = 15, noDamageAtkMinusOneEffect = 18, noDamageDefMinusOneEffect = 19,
            noDamageSpeMinusOneEffect = 20, noDamageAccuracyMinusOneEffect = 22, flinch10PercentEffect = 31,
            noDamageSleepEffect = 32, damagePoison40PercentEffect = 33, damageBurn30PercentEffect = 34,
            damageFreeze30PercentEffect = 35, damageParalyze30PercentEffect = 36, flinch30PercentEffect = 37,
            chargeEffect = 39, flyEffect = 43, damageRecoilEffect = 48, noDamageConfusionEffect = 49,
            noDamageAtkPlusTwoEffect = 50, noDamageDefPlusTwoEffect = 51, noDamageSpePlusTwoEffect = 52,
            noDamageSpecialPlusTwoEffect = 53, noDamageDefMinusTwoEffect = 59, noDamagePoisonEffect = 66,
            noDamageParalyzeEffect = 67, damageAtkMinusOneEffect = 68, damageDefMinusOneEffect = 69,
            damageSpeMinusOneEffect = 70, damageSpecialMinusOneEffect = 71, damageConfusionEffect = 76,
            twineedleEffect = 77, hyperBeamEffect = 80;

    // Taken from critical_hit_moves.asm; we could read this from the ROM, but it's easier to hardcode it.
    public static final List<Integer> increasedCritMoves = Arrays.asList(MoveIDs.karateChop, MoveIDs.razorLeaf, MoveIDs.crabhammer, MoveIDs.slash);

    public static final List<Integer> earlyRequiredHMs = Collections.singletonList(MoveIDs.cut);

    public static final int hmsStartIndex = Gen1ItemIDs.hm01, tmsStartIndex = Gen1ItemIDs.tm01;

    public static final List<Integer> requiredFieldTMs = Arrays.asList(3, 4, 8, 10, 12, 14, 16, 19, 20,
            22, 25, 26, 30, 40, 43, 44, 45, 47);

    public static final int towerMapsStartIndex = 0x90, towerMapsEndIndex = 0x94;

    public static final String guaranteedCatchPrefix = "CF7EFE01";

    public static final int playerFrontImageOffset5 = 54; // "5" because it is used to put a value at index 5 of PlayerFrontImagePointers

    public static final int playerFrontBankOffset1 = 4, playerFrontBankOffset2 = 3, playerFrontBankOffset3 = 4,
            playerFrontBankOffset4 = 3, playerFrontBankOffset5 = 4;

    public static final int playerBackImageOffsetRGB0 = 8, playerBackImageOffset1 = 3, playerBackImageOffsetYellow0 = 3;

    public static final int oldManBackImageOffsetRGB = 5, oldManBackImageOffsetYellow = -14, oakBackImageOffset = -7;

    public static final Type[] typeTable = constructTypeTable();

    private static Type[] constructTypeTable() {
        Type[] table = new Type[0x20];
        table[0x00] = Type.NORMAL;
        table[0x01] = Type.FIGHTING;
        table[0x02] = Type.FLYING;
        table[0x03] = Type.POISON;
        table[0x04] = Type.GROUND;
        table[0x05] = Type.ROCK;
        table[0x07] = Type.BUG;
        table[0x08] = Type.GHOST;
        table[0x14] = Type.FIRE;
        table[0x15] = Type.WATER;
        table[0x16] = Type.GRASS;
        table[0x17] = Type.ELECTRIC;
        table[0x18] = Type.PSYCHIC;
        table[0x19] = Type.ICE;
        table[0x1A] = Type.DRAGON;
        return table;
    }

    public static byte typeToByte(Type type) {
        for (int i = 0; i < typeTable.length; i++) {
            if (typeTable[i] == type) {
                return (byte) i;
            }
        }
        return (byte) 0;
    }

    public static final int nonNeutralEffectivenessCount = 82;

    public static int evolutionTypeToIndex(EvolutionType evolutionType) {
        switch (evolutionType) {
            case LEVEL:
                return 1;
            case STONE:
                return 2;
            case TRADE:
                return 3;
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
            default:
                return EvolutionType.NONE;
        }
    }

    public static final Set<Integer> bannedItems = setupBannedItems();

    private static Set<Integer> setupBannedItems() {
        // Assorted key items & junk
        Set<Integer> set = new HashSet<>(Arrays.asList(Gen1ItemIDs.townMap, Gen1ItemIDs.bicycle,
                Gen1ItemIDs.questionMark7, Gen1ItemIDs.safariBall, Gen1ItemIDs.pokedex, Gen1ItemIDs.oldAmber,
                Gen1ItemIDs.cardKey, Gen1ItemIDs.ppUpGlitch, Gen1ItemIDs.coin, Gen1ItemIDs.ssTicket,
                Gen1ItemIDs.goldTeeth));
        addRange(set, Gen1ItemIDs.boulderBadge, 8);
        addRange(set, Gen1ItemIDs.domeFossil, 5);
        addRange(set, Gen1ItemIDs.coinCase, 10);
        // Unused
        addRange(set, Gen1ItemIDs.unused84, 112);
        // HMs
        addRange(set, hmsStartIndex, hmCount);
        // Junk at end
        addRange(set, Gen1ItemIDs.tm50, 5); // 251-255 are junk TMs
        return Collections.unmodifiableSet(set);
    }

    private static void addRange(Set<Integer> set, int start, int length) {
        for (int i = start; i < start + length; i++) {
            set.add(i);
        }
    }

    public static void tagTrainersUniversal(List<Trainer> trs) {
        // Gym Leaders
        tbc(trs, 34, 0, "GYM1-LEADER");
        tbc(trs, 35, 0, "GYM2-LEADER");
        tbc(trs, 36, 0, "GYM3-LEADER");
        tbc(trs, 37, 0, "GYM4-LEADER");
        tbc(trs, 38, 0, "GYM5-LEADER");
        tbc(trs, 40, 0, "GYM6-LEADER");
        tbc(trs, 39, 0, "GYM7-LEADER");
        tbc(trs, 29, 2, "GYM8-LEADER");

        // Other giovanni teams
        tbc(trs, 29, 0, "GIO1");
        tbc(trs, 29, 1, "GIO2");

        // Elite 4
        tbc(trs, 44, 0, "ELITE1");
        tbc(trs, 33, 0, "ELITE2");
        tbc(trs, 46, 0, "ELITE3");
        tbc(trs, 47, 0, "ELITE4");
    }

    public static void tagTrainersRB(List<Trainer> trs) {
        // Gary Battles
        tbc(trs, 25, 0, "RIVAL1-0");
        tbc(trs, 25, 1, "RIVAL1-1");
        tbc(trs, 25, 2, "RIVAL1-2");

        tbc(trs, 25, 3, "RIVAL2-0");
        tbc(trs, 25, 4, "RIVAL2-1");
        tbc(trs, 25, 5, "RIVAL2-2");

        tbc(trs, 25, 6, "RIVAL3-0");
        tbc(trs, 25, 7, "RIVAL3-1");
        tbc(trs, 25, 8, "RIVAL3-2");

        tbc(trs, 42, 0, "RIVAL4-0");
        tbc(trs, 42, 1, "RIVAL4-1");
        tbc(trs, 42, 2, "RIVAL4-2");

        tbc(trs, 42, 3, "RIVAL5-0");
        tbc(trs, 42, 4, "RIVAL5-1");
        tbc(trs, 42, 5, "RIVAL5-2");

        tbc(trs, 42, 6, "RIVAL6-0");
        tbc(trs, 42, 7, "RIVAL6-1");
        tbc(trs, 42, 8, "RIVAL6-2");

        tbc(trs, 42, 9, "RIVAL7-0");
        tbc(trs, 42, 10, "RIVAL7-1");
        tbc(trs, 42, 11, "RIVAL7-2");

        tbc(trs, 43, 0, "RIVAL8-0");
        tbc(trs, 43, 1, "RIVAL8-1");
        tbc(trs, 43, 2, "RIVAL8-2");

        // Gym Trainers
        tbc(trs, 5, 0, "GYM1");

        tbc(trs, 15, 0, "GYM2");
        tbc(trs, 6, 0, "GYM2");

        tbc(trs, 4, 7, "GYM3");
        tbc(trs, 20, 0, "GYM3");
        tbc(trs, 41, 2, "GYM3");

        tbc(trs, 3, 16, "GYM4");
        tbc(trs, 3, 17, "GYM4");
        tbc(trs, 6, 10, "GYM4");
        tbc(trs, 18, 0, "GYM4");
        tbc(trs, 18, 1, "GYM4");
        tbc(trs, 18, 2, "GYM4");
        tbc(trs, 32, 0, "GYM4");

        tbc(trs, 21, 2, "GYM5");
        tbc(trs, 21, 3, "GYM5");
        tbc(trs, 21, 6, "GYM5");
        tbc(trs, 21, 7, "GYM5");
        tbc(trs, 22, 0, "GYM5");
        tbc(trs, 22, 1, "GYM5");

        tbc(trs, 19, 0, "GYM6");
        tbc(trs, 19, 1, "GYM6");
        tbc(trs, 19, 2, "GYM6");
        tbc(trs, 19, 3, "GYM6");
        tbc(trs, 45, 21, "GYM6");
        tbc(trs, 45, 22, "GYM6");
        tbc(trs, 45, 23, "GYM6");

        tbc(trs, 8, 8, "GYM7");
        tbc(trs, 8, 9, "GYM7");
        tbc(trs, 8, 10, "GYM7");
        tbc(trs, 8, 11, "GYM7");
        tbc(trs, 11, 3, "GYM7");
        tbc(trs, 11, 4, "GYM7");
        tbc(trs, 11, 5, "GYM7");

        tbc(trs, 22, 2, "GYM8");
        tbc(trs, 22, 3, "GYM8");
        tbc(trs, 24, 5, "GYM8");
        tbc(trs, 24, 6, "GYM8");
        tbc(trs, 24, 7, "GYM8");
        tbc(trs, 31, 0, "GYM8");
        tbc(trs, 31, 8, "GYM8");
        tbc(trs, 31, 9, "GYM8");
    }

    public static void tagTrainersYellow(List<Trainer> trs) {
        // Rival Battles
        tbc(trs, 25, 0, "IRIVAL");

        tbc(trs, 25, 1, "RIVAL1-0");

        tbc(trs, 25, 2, "RIVAL2-0");

        tbc(trs, 42, 0, "RIVAL3-0");

        tbc(trs, 42, 1, "RIVAL4-0");
        tbc(trs, 42, 2, "RIVAL4-1");
        tbc(trs, 42, 3, "RIVAL4-2");

        tbc(trs, 42, 4, "RIVAL5-0");
        tbc(trs, 42, 5, "RIVAL5-1");
        tbc(trs, 42, 6, "RIVAL5-2");

        tbc(trs, 42, 7, "RIVAL6-0");
        tbc(trs, 42, 8, "RIVAL6-1");
        tbc(trs, 42, 9, "RIVAL6-2");

        tbc(trs, 43, 0, "RIVAL7-0");
        tbc(trs, 43, 1, "RIVAL7-1");
        tbc(trs, 43, 2, "RIVAL7-2");

        // Rocket Jessie & James
        tbc(trs, 30, 41, "THEMED:JESSIE&JAMES");
        tbc(trs, 30, 42, "THEMED:JESSIE&JAMES");
        tbc(trs, 30, 43, "THEMED:JESSIE&JAMES");
        tbc(trs, 30, 44, "THEMED:JESSIE&JAMES");

        // Gym Trainers
        tbc(trs, 5, 0, "GYM1");

        tbc(trs, 6, 0, "GYM2");
        tbc(trs, 15, 0, "GYM2");

        tbc(trs, 4, 7, "GYM3");
        tbc(trs, 20, 0, "GYM3");
        tbc(trs, 41, 2, "GYM3");

        tbc(trs, 3, 16, "GYM4");
        tbc(trs, 3, 17, "GYM4");
        tbc(trs, 6, 10, "GYM4");
        tbc(trs, 18, 0, "GYM4");
        tbc(trs, 18, 1, "GYM4");
        tbc(trs, 18, 2, "GYM4");
        tbc(trs, 32, 0, "GYM4");

        tbc(trs, 21, 2, "GYM5");
        tbc(trs, 21, 3, "GYM5");
        tbc(trs, 21, 6, "GYM5");
        tbc(trs, 21, 7, "GYM5");
        tbc(trs, 22, 0, "GYM5");
        tbc(trs, 22, 1, "GYM5");

        tbc(trs, 19, 0, "GYM6");
        tbc(trs, 19, 1, "GYM6");
        tbc(trs, 19, 2, "GYM6");
        tbc(trs, 19, 3, "GYM6");
        tbc(trs, 45, 21, "GYM6");
        tbc(trs, 45, 22, "GYM6");
        tbc(trs, 45, 23, "GYM6");

        tbc(trs, 8, 8, "GYM7");
        tbc(trs, 8, 9, "GYM7");
        tbc(trs, 8, 10, "GYM7");
        tbc(trs, 8, 11, "GYM7");
        tbc(trs, 11, 3, "GYM7");
        tbc(trs, 11, 4, "GYM7");
        tbc(trs, 11, 5, "GYM7");

        tbc(trs, 22, 2, "GYM8");
        tbc(trs, 22, 3, "GYM8");
        tbc(trs, 24, 5, "GYM8");
        tbc(trs, 24, 6, "GYM8");
        tbc(trs, 24, 7, "GYM8");
        tbc(trs, 31, 0, "GYM8");
        tbc(trs, 31, 8, "GYM8");
        tbc(trs, 31, 9, "GYM8");
    }

    public static final HashMap<String, Type> gymAndEliteThemes = setupGymAndEliteThemes();

    private static HashMap<String, Type> setupGymAndEliteThemes() {
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

    private static final int[] postGameEncounterAreasRBG = new int[] {
            53, 54, 55, 67, //CERULEAN CAVE
    };

    private static final int[] postGameEncounterAreasJapaneseBlue = new int[] {
            54, 55, 56, 68, //CERULEAN CAVE
    };

    private static final int[] postGameEncounterAreasYellow = new int[] {
            59, 60, 61, 94, 95, //CERULEAN CAVE
    };

    // the ones tagged "SUPER ROD N" are super rod encounters shared between several locations
    private static final List<String> locationTagsRBG = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 19/20", "ROUTE 21", "ROUTE 21", "ROUTE 22", "ROUTE 23", "ROUTE 24", "ROUTE 25",
            "VIRIDIAN FOREST", "MT.MOON", "MT.MOON", "MT.MOON", "ROCK TUNNEL", "POWER PLANT", "VICTORY ROAD",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "POKEMON MANSION", "SEAFOAM ISLANDS",
            "VICTORY ROAD", "DIGLETT'S CAVE", "VICTORY ROAD", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "ROCK TUNNEL",
            "OLD ROD", "GOOD ROD", "SUPER ROD 1", "SUPER ROD 2", "SUPER ROD 3", "SUPER ROD 4", "SUPER ROD 5",
            "SUPER ROD 6", "SUPER ROD 7", "SUPER ROD 8", "SUPER ROD 9", "SUPER ROD 10"));

    // for whatever reason Japanese blue loads Route 19/20 as separate encounters,
    // the only difference to locationTagsRBG.
    private static final List<String> locationTagsJapaneseBlue = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 19", "ROUTE 20", "ROUTE 21", "ROUTE 21", "ROUTE 22", "ROUTE 23", "ROUTE 24", "ROUTE 25",
            "VIRIDIAN FOREST", "MT.MOON", "MT.MOON", "MT.MOON", "ROCK TUNNEL", "POWER PLANT", "VICTORY ROAD",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "POKEMON MANSION", "SEAFOAM ISLANDS",
            "VICTORY ROAD", "DIGLETT'S CAVE", "VICTORY ROAD", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "ROCK TUNNEL",
            "OLD ROD", "GOOD ROD", "SUPER ROD 1", "SUPER ROD 2", "SUPER ROD 3", "SUPER ROD 4", "SUPER ROD 5",
            "SUPER ROD 6", "SUPER ROD 7", "SUPER ROD 8", "SUPER ROD 9", "SUPER ROD 10"));

    // yellow has more specific super rod encounters
    private static final List<String> locationTagsYellow = Collections.unmodifiableList(Arrays.asList(
            "ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 6",
            "ROUTE 7", "ROUTE 8", "ROUTE 9",
            "ROUTE 10", "ROUTE 11", "ROUTE 12", "ROUTE 12", "ROUTE 13", "ROUTE 13",
            "ROUTE 14", "ROUTE 15", "ROUTE 16", "ROUTE 17", "ROUTE 18",
            "ROUTE 19", "ROUTE 20", "ROUTE 21", "ROUTE 21", "ROUTE 22", "ROUTE 23", "ROUTE 24", "ROUTE 25",
            "VIRIDIAN FOREST", "MT.MOON", "MT.MOON", "MT.MOON", "ROCK TUNNEL", "POWER PLANT", "VICTORY ROAD",
            "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "POKEMON TOWER", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS", "SEAFOAM ISLANDS",
            "POKEMON MANSION", "SEAFOAM ISLANDS",
            "VICTORY ROAD", "DIGLETT'S CAVE", "VICTORY ROAD", "POKEMON MANSION", "POKEMON MANSION", "POKEMON MANSION",
            "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "CERULEAN CAVE", "CERULEAN CAVE",
            "CERULEAN CAVE", "ROCK TUNNEL",
            "OLD ROD", "GOOD ROD",
            "PALLET TOWN", "VIRIDIAN CITY", "CERULEAN CITY", "VERMILION CITY", "CELADON CITY", "FUCHSIA CITY",
            "CINNABAR ISLAND", "ROUTE 4", "ROUTE 6", "ROUTE 24", "ROUTE 25", "ROUTE 10", "ROUTE 11", "ROUTE 12",
            "ROUTE 13", "ROUTE 17", "ROUTE 18", "ROUTE 19", "ROUTE 20", "ROUTE 21", "ROUTE 22", "ROUTE 23",
            "VERMILION CITY", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SAFARI ZONE", "SEAFOAM ISLANDS",
            "SEAFOAM ISLANDS", "CERULEAN CAVE", "CERULEAN CAVE"));

    /**
     * The order the player is "expected" to traverse locations. Taken from
     * <a href=https://strategywiki.org/wiki/Pok%C3%A9mon_Red_and_Blue/Walkthrough>this walkthrough</a>.
     */
    public static final List<String> locationTagsTraverseOrder = Collections.unmodifiableList(Arrays.asList(
            "PALLET TOWN", "ROUTE 1", "VIRIDIAN CITY", "ROUTE 2", "ROUTE 22", "VIRIDIAN FOREST", "ROUTE 3",
            "MT.MOON", "ROUTE 4", "CERULEAN CITY", "ROUTE 24", "ROUTE 25", "ROUTE 5", "ROUTE 6", "VERMILION CITY",
            "ROUTE 11", "DIGLETT'S CAVE", "ROUTE 9", "ROUTE 10", "ROCK TUNNEL", "ROUTE 8", "ROUTE 7", "CELADON CITY",
            "POKEMON TOWER", "ROUTE 16", "ROUTE 17", "ROUTE 18", "FUCHSIA CITY", "SAFARI ZONE", "ROUTE 12", "ROUTE 13",
            "ROUTE 14", "ROUTE 15", "ROUTE 21", "CINNABAR ISLAND", "POKEMON MANSION", "POWER PLANT", "ROUTE 19",
            "ROUTE 20", "ROUTE 19/20", "SEAFOAM ISLANDS", "ROUTE 23", "VICTORY ROAD", "CERULEAN CAVE",
            "OLD ROD", "GOOD ROD",
            "SUPER ROD 1", // pallet, viridian
            "SUPER ROD 2", // route 22
            "SUPER ROD 3", // route 4, cerulean, cerulean gym, route 24, route 25
            "SUPER ROD 4", // route 6, vermilion, vermilion dock, route 11
            "SUPER ROD 5", // route 10, celadon city
            "SUPER ROD 7", // route 17, route 18, route 12, route 13
            "SUPER ROD 10", // fuchsia city
            "SUPER ROD 6", // safari zone
            "SUPER ROD 8", // route 19, route 20, route 21, seafoam islands
            "SUPER ROD 9" // route 23, cerulean cave
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

    public static void tagEncounterAreasRBG(List<EncounterArea> encounterAreas) {
        tagEncounterAreas(encounterAreas, locationTagsRBG, postGameEncounterAreasRBG);
    }

    public static void tagEncounterAreasJapaneseBlue(List<EncounterArea> encounterAreas) {
        tagEncounterAreas(encounterAreas, locationTagsJapaneseBlue, postGameEncounterAreasJapaneseBlue);
    }

    public static void tagEncounterAreasYellow(List<EncounterArea> encounterAreas) {
        tagEncounterAreas(encounterAreas, locationTagsYellow, postGameEncounterAreasYellow);
    }

}
