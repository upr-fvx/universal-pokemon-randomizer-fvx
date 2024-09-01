package com.dabomstew.pkrandom.romhandlers;

/*----------------------------------------------------------------------------*/
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

import com.dabomstew.pkrandom.*;
import com.dabomstew.pkrandom.constants.*;
import com.dabomstew.pkrandom.exceptions.RomIOException;
import com.dabomstew.pkrandom.graphics.images.GBCImage;
import com.dabomstew.pkrandom.graphics.packs.Gen2PlayerCharacterGraphics;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.graphics.palettes.Color;
import com.dabomstew.pkrandom.graphics.palettes.Gen2SpritePaletteID;
import com.dabomstew.pkrandom.graphics.palettes.Palette;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.romentries.GBCTMTextEntry;
import com.dabomstew.pkrandom.romhandlers.romentries.Gen2RomEntry;
import compressors.Gen2Cmp;
import compressors.Gen2Decmp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link RomHandler} for Gold, Silver, Crystal.
 */
public class Gen2RomHandler extends AbstractGBCRomHandler {

    public static class Factory extends RomHandler.Factory {

        @Override
        public Gen2RomHandler create() {
            return new Gen2RomHandler();
        }

        public boolean isLoadable(String filename) {
            long fileLength = new File(filename).length();
            if (fileLength > 8 * 1024 * 1024) {
                return false;
            }
            byte[] loaded = loadFilePartial(filename, 0x1000);
            // nope
            return loaded.length != 0 && detectRomInner(loaded, (int) fileLength);
        }
    }

    private static List<Gen2RomEntry> roms;

    static {
        loadROMInfo();
    }

    private static void loadROMInfo() {
        try {
            roms = Gen2RomEntry.READER.readEntriesFromFile("gen2_offsets.ini");
        } catch (IOException e) {
            throw new RuntimeException("Could not read Rom Entries.", e);
        }
    }

    // This ROM's data
    private Gen2RomEntry romEntry;
    private Species[] pokes;
    private List<Species> speciesList;
    private List<Trainer> trainers;
    private List<Item> items;
    private Move[] moves;
    private Map<Integer, List<MoveLearnt>> movesets;
    private boolean havePatchedFleeing;
    private List<Integer> itemOffs;
    private String[][] mapNames;
    private String[] landmarkNames;
    private boolean isVietCrystal;

    @Override
    public boolean detectRom(byte[] rom) {
        return detectRomInner(rom, rom.length);
    }

    private static boolean detectRomInner(byte[] rom, int romSize) {
        // size check
        return romSize >= GBConstants.minRomSize && romSize <= GBConstants.maxRomSize && checkRomEntry(rom) != null;
    }

    @Override
    public void midLoadingSetUp() {
        super.midLoadingSetUp();
        havePatchedFleeing = false;
        loadLandmarkNames();
        preprocessMaps();
    }

    @Override
    protected void loadGameData() {
        super.loadGameData();
    }

    @Override
    protected void initRomEntry() {
        romEntry = checkRomEntry(this.rom);
        if (romEntry.getName().equals("Crystal (J)")
                && rom[Gen2Constants.vietCrystalCheckOffset] == Gen2Constants.vietCrystalCheckValue) {
            readTextTable("vietcrystal");
            isVietCrystal = true;
        } else {
            isVietCrystal = false;
        }

        if (romEntry.isCrystal()) {
            addRelativeOffsetToRomEntry("KrisFrontImage", "ChrisFrontImage",
                    Gen2Constants.krisFrontImageOffset);
            addRelativeOffsetToRomEntry("KrisTrainerCardImage", "ChrisTrainerCardImage",
                    Gen2Constants.krisTrainerCardImageOffset);
            addRelativeOffsetToRomEntry("KrisPalettePointer", "ChrisPalettePointer",
                    Gen2Constants.krisPalettePointerOffset);
            addRelativeOffsetToRomEntry("KrisSpritePalette", "ChrisSpritePalette",
                    Gen2Constants.krisSpritePaletteOffset);
        }

        int[] chrisBackPointers = romEntry.getArrayValue("ChrisBackImagePointers");
        if (chrisBackPointers.length == 2) {
            if (romEntry.getArrayValue("ChrisBackImageBankOffsets").length == 0) {
                int offset0 = romEntry.isCrystal() ? Gen2Constants.chrisBackBankOffsetCrystal0 :
                        Gen2Constants.chrisBackBankOffsetGS0;
                int offset1 = romEntry.isCrystal() ? Gen2Constants.chrisBackBankOffsetCrystal1 :
                        Gen2Constants.chrisBackBankOffsetGS1;
                int[] bankOffsets = new int[] {chrisBackPointers[0] + offset0, chrisBackPointers[1] + offset1};
                System.out.println("ChrisBackImageBankOffsets=[0x" + Integer.toHexString(bankOffsets[0]) +
                        ", 0x" + Integer.toHexString(bankOffsets[1]) + "]");
                romEntry.putArrayValue("ChrisBackImageBankOffsets", bankOffsets);
            }

            if (!romEntry.isCrystal() && romEntry.getIntValue("DudeBackImagePointer") == 0) {
                int dudeBackPointer = chrisBackPointers[0] + Gen2Constants.dudeBackPointerOffset;
                romEntry.putIntValue("DudeBackImagePointer", dudeBackPointer);
            }
        }

    }

    @Override
    protected void initTextTables() {
        clearTextTables();
        readTextTable("gameboy_jpn");
        String extraTableFile = romEntry.getExtraTableFile();
        if (extraTableFile != null && !extraTableFile.equalsIgnoreCase("none")) {
            readTextTable(extraTableFile);
        }
    }

    private static Gen2RomEntry checkRomEntry(byte[] rom) {
        int version = rom[GBConstants.versionOffset] & 0xFF;
        int nonjap = rom[GBConstants.jpFlagOffset] & 0xFF;
        // Check for specific CRC first
        int crcInHeader = ((rom[GBConstants.crcOffset] & 0xFF) << 8) | (rom[GBConstants.crcOffset + 1] & 0xFF);
        for (Gen2RomEntry re : roms) {
            if (romCode(rom, re.getRomCode()) && re.getVersion() == version && re.getNonJapanese() == nonjap
                    && re.getCRCInHeader() == crcInHeader) {
                return new Gen2RomEntry(re);
            }
        }
        // Now check for non-specific-CRC entries
        for (Gen2RomEntry re : roms) {
            if (romCode(rom, re.getRomCode()) && re.getVersion() == version && re.getNonJapanese() == nonjap && re.getCRCInHeader() == -1) {
                return new Gen2RomEntry(re);
            }
        }
        // Not found
        return null;
    }

    @Override
    public void loadPokemonStats() {
        pokes = new Species[Gen2Constants.pokemonCount + 1];
        // Fetch our names
        String[] pokeNames = readPokemonNames();
        int offs = romEntry.getIntValue("PokemonStatsOffset");
        // Get base stats
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            pokes[i] = new Species(i);
            loadBasicPokeStats(pokes[i], offs + (i - 1) * Gen2Constants.baseStatsEntrySize);
            pokes[i].setName(pokeNames[i]);
            pokes[i].setGeneration(pokes[i].getNumber() >= SpeciesIDs.chikorita ? 2 : 1);
        }
        this.speciesList = Arrays.asList(pokes);
    }

    @Override
    public void savePokemonStats() {
        // Write pokemon names
        int offs = romEntry.getIntValue("PokemonNamesOffset");
        int len = romEntry.getIntValue("PokemonNamesLength");
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            int stringOffset = offs + (i - 1) * len;
            writeFixedLengthString(pokes[i].getName(), stringOffset, len);
        }
        // Write pokemon stats
        int offs2 = romEntry.getIntValue("PokemonStatsOffset");
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            saveBasicPokeStats(pokes[i], offs2 + (i - 1) * Gen2Constants.baseStatsEntrySize);
        }
        // Write evolutions and movesets
        saveEvosAndMovesLearnt();
    }

    private String[] readMoveNames() {
        int offset = romEntry.getIntValue("MoveNamesOffset");
        String[] moveNames = new String[Gen2Constants.moveCount + 1];
        for (int i = 1; i <= Gen2Constants.moveCount; i++) {
            moveNames[i] = readVariableLengthString(offset, false);
            offset += lengthOfStringAt(offset, false);
        }
        return moveNames;
    }

    @Override
    public void loadMoves() {
        moves = new Move[Gen2Constants.moveCount + 1];
        String[] moveNames = readMoveNames();
        int offs = romEntry.getIntValue("MoveDataOffset");
        for (int i = 1; i <= Gen2Constants.moveCount; i++) {
            moves[i] = new Move();
            moves[i].name = moveNames[i];
            moves[i].number = i;
            moves[i].internalId = i;
            moves[i].effectIndex = rom[offs + (i - 1) * 7 + 1] & 0xFF;
            moves[i].hitratio = ((rom[offs + (i - 1) * 7 + 4] & 0xFF)) / 255.0 * 100;
            moves[i].power = rom[offs + (i - 1) * 7 + 2] & 0xFF;
            moves[i].pp = rom[offs + (i - 1) * 7 + 5] & 0xFF;
            moves[i].type = Gen2Constants.typeTable[rom[offs + (i - 1) * 7 + 3]];
            moves[i].category = GBConstants.physicalTypes.contains(moves[i].type) ? MoveCategory.PHYSICAL : MoveCategory.SPECIAL;
            if (moves[i].power == 0 && !GlobalConstants.noPowerNonStatusMoves.contains(i)) {
                moves[i].category = MoveCategory.STATUS;
            }

            if (i == MoveIDs.swift) {
                perfectAccuracy = (int) moves[i].hitratio;
            }

            if (GlobalConstants.normalMultihitMoves.contains(i)) {
                moves[i].hitCount = 3;
            } else if (GlobalConstants.doubleHitMoves.contains(i)) {
                moves[i].hitCount = 2;
            } else if (i == MoveIDs.tripleKick) {
                moves[i].hitCount = 2.71; // this assumes the first hit lands
            }

            // Values taken from effect_priorities.asm from the Gen 2 disassemblies.
            if (moves[i].effectIndex == Gen2Constants.priorityHitEffectIndex) {
                moves[i].priority = 2;
            } else if (moves[i].effectIndex == Gen2Constants.protectEffectIndex ||
                    moves[i].effectIndex == Gen2Constants.endureEffectIndex) {
                moves[i].priority = 3;
            } else if (moves[i].effectIndex == Gen2Constants.forceSwitchEffectIndex ||
                    moves[i].effectIndex == Gen2Constants.counterEffectIndex ||
                    moves[i].effectIndex == Gen2Constants.mirrorCoatEffectIndex) {
                moves[i].priority = 0;
            } else {
                moves[i].priority = 1;
            }

            double secondaryEffectChance = ((rom[offs + (i - 1) * 7 + 6] & 0xFF)) / 255.0 * 100;
            loadStatChangesFromEffect(moves[i], secondaryEffectChance);
            loadStatusFromEffect(moves[i], secondaryEffectChance);
            loadMiscMoveInfoFromEffect(moves[i], secondaryEffectChance);
        }
    }

    private void loadStatChangesFromEffect(Move move, double secondaryEffectChance) {
        switch (move.effectIndex) {
            case Gen2Constants.noDamageAtkPlusOneEffect:
            case Gen2Constants.damageUserAtkPlusOneEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 1;
                break;
            case Gen2Constants.noDamageDefPlusOneEffect:
            case Gen2Constants.damageUserDefPlusOneEffect:
            case Gen2Constants.defenseCurlEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 1;
                break;
            case Gen2Constants.noDamageSpAtkPlusOneEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
                move.statChanges[0].stages = 1;
                break;
            case Gen2Constants.noDamageEvasionPlusOneEffect:
                move.statChanges[0].type = StatChangeType.EVASION;
                move.statChanges[0].stages = 1;
                break;
            case Gen2Constants.noDamageAtkMinusOneEffect:
            case Gen2Constants.damageAtkMinusOneEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = -1;
                break;
            case Gen2Constants.noDamageDefMinusOneEffect:
            case Gen2Constants.damageDefMinusOneEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = -1;
                break;
            case Gen2Constants.noDamageSpeMinusOneEffect:
            case Gen2Constants.damageSpeMinusOneEffect:
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = -1;
                break;
            case Gen2Constants.noDamageAccuracyMinusOneEffect:
            case Gen2Constants.damageAccuracyMinusOneEffect:
                move.statChanges[0].type = StatChangeType.ACCURACY;
                move.statChanges[0].stages = -1;
                break;
            case Gen2Constants.noDamageEvasionMinusOneEffect:
                move.statChanges[0].type = StatChangeType.EVASION;
                move.statChanges[0].stages = -1;
                break;
            case Gen2Constants.noDamageAtkPlusTwoEffect:
            case Gen2Constants.swaggerEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 2;
                break;
            case Gen2Constants.noDamageDefPlusTwoEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 2;
                break;
            case Gen2Constants.noDamageSpePlusTwoEffect:
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = 2;
                break;
            case Gen2Constants.noDamageSpDefPlusTwoEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = 2;
                break;
            case Gen2Constants.noDamageAtkMinusTwoEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = -2;
                break;
            case Gen2Constants.noDamageDefMinusTwoEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = -2;
                break;
            case Gen2Constants.noDamageSpeMinusTwoEffect:
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = -2;
                break;
            case Gen2Constants.noDamageSpDefMinusTwoEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = -2;
                break;
            case Gen2Constants.damageSpDefMinusOneEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = -1;
                break;
            case Gen2Constants.damageUserAllPlusOneEffect:
                move.statChanges[0].type = StatChangeType.ALL;
                move.statChanges[0].stages = 1;
                break;
            default:
                // Move does not have a stat-changing effect
                return;
        }

        switch (move.effectIndex) {
            case Gen2Constants.noDamageAtkPlusOneEffect:
            case Gen2Constants.noDamageDefPlusOneEffect:
            case Gen2Constants.noDamageSpAtkPlusOneEffect:
            case Gen2Constants.noDamageEvasionPlusOneEffect:
            case Gen2Constants.noDamageAtkMinusOneEffect:
            case Gen2Constants.noDamageDefMinusOneEffect:
            case Gen2Constants.noDamageSpeMinusOneEffect:
            case Gen2Constants.noDamageAccuracyMinusOneEffect:
            case Gen2Constants.noDamageEvasionMinusOneEffect:
            case Gen2Constants.noDamageAtkPlusTwoEffect:
            case Gen2Constants.noDamageDefPlusTwoEffect:
            case Gen2Constants.noDamageSpePlusTwoEffect:
            case Gen2Constants.noDamageSpDefPlusTwoEffect:
            case Gen2Constants.noDamageAtkMinusTwoEffect:
            case Gen2Constants.noDamageDefMinusTwoEffect:
            case Gen2Constants.noDamageSpeMinusTwoEffect:
            case Gen2Constants.noDamageSpDefMinusTwoEffect:
            case Gen2Constants.swaggerEffect:
            case Gen2Constants.defenseCurlEffect:
                if (move.statChanges[0].stages < 0 || move.effectIndex == Gen2Constants.swaggerEffect) {
                    move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_TARGET;
                } else {
                    move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_USER;
                }
                break;

            case Gen2Constants.damageAtkMinusOneEffect:
            case Gen2Constants.damageDefMinusOneEffect:
            case Gen2Constants.damageSpeMinusOneEffect:
            case Gen2Constants.damageSpDefMinusOneEffect:
            case Gen2Constants.damageAccuracyMinusOneEffect:
                move.statChangeMoveType = StatChangeMoveType.DAMAGE_TARGET;
                break;

            case Gen2Constants.damageUserDefPlusOneEffect:
            case Gen2Constants.damageUserAtkPlusOneEffect:
            case Gen2Constants.damageUserAllPlusOneEffect:
                move.statChangeMoveType = StatChangeMoveType.DAMAGE_USER;
                break;
        }

        if (move.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET || move.statChangeMoveType == StatChangeMoveType.DAMAGE_USER) {
            for (int i = 0; i < move.statChanges.length; i++) {
                if (move.statChanges[i].type != StatChangeType.NONE) {
                    move.statChanges[i].percentChance = secondaryEffectChance;
                    if (move.statChanges[i].percentChance == 0.0) {
                        move.statChanges[i].percentChance = 100.0;
                    }
                }
            }
        }
    }

    private void loadStatusFromEffect(Move move, double secondaryEffectChance) {
        switch (move.effectIndex) {
            case Gen2Constants.noDamageSleepEffect:
            case Gen2Constants.toxicEffect:
            case Gen2Constants.noDamageConfusionEffect:
            case Gen2Constants.noDamagePoisonEffect:
            case Gen2Constants.noDamageParalyzeEffect:
            case Gen2Constants.swaggerEffect:
                move.statusMoveType = StatusMoveType.NO_DAMAGE;
                break;

            case Gen2Constants.damagePoisonEffect:
            case Gen2Constants.damageBurnEffect:
            case Gen2Constants.damageFreezeEffect:
            case Gen2Constants.damageParalyzeEffect:
            case Gen2Constants.damageConfusionEffect:
            case Gen2Constants.twineedleEffect:
            case Gen2Constants.damageBurnAndThawUserEffect:
            case Gen2Constants.thunderEffect:
                move.statusMoveType = StatusMoveType.DAMAGE;
                break;

            default:
                // Move does not have a status effect
                return;
        }

        switch (move.effectIndex) {
            case Gen2Constants.noDamageSleepEffect:
                move.statusType = StatusType.SLEEP;
                break;
            case Gen2Constants.damagePoisonEffect:
            case Gen2Constants.noDamagePoisonEffect:
            case Gen2Constants.twineedleEffect:
                move.statusType = StatusType.POISON;
                break;
            case Gen2Constants.damageBurnEffect:
            case Gen2Constants.damageBurnAndThawUserEffect:
                move.statusType = StatusType.BURN;
                break;
            case Gen2Constants.damageFreezeEffect:
                move.statusType = StatusType.FREEZE;
                break;
            case Gen2Constants.damageParalyzeEffect:
            case Gen2Constants.noDamageParalyzeEffect:
            case Gen2Constants.thunderEffect:
                move.statusType = StatusType.PARALYZE;
                break;
            case Gen2Constants.toxicEffect:
                move.statusType = StatusType.TOXIC_POISON;
                break;
            case Gen2Constants.noDamageConfusionEffect:
            case Gen2Constants.damageConfusionEffect:
            case Gen2Constants.swaggerEffect:
                move.statusType = StatusType.CONFUSION;
                break;
        }

        if (move.statusMoveType == StatusMoveType.DAMAGE) {
            move.statusPercentChance = secondaryEffectChance;
            if (move.statusPercentChance == 0.0) {
                move.statusPercentChance = 100.0;
            }
        }
    }

    private void loadMiscMoveInfoFromEffect(Move move, double secondaryEffectChance) {
        switch (move.effectIndex) {
            case Gen2Constants.flinchEffect:
            case Gen2Constants.snoreEffect:
            case Gen2Constants.twisterEffect:
            case Gen2Constants.stompEffect:
                move.flinchPercentChance = secondaryEffectChance;
                break;
            case Gen2Constants.damageAbsorbEffect:
            case Gen2Constants.dreamEaterEffect:
                move.absorbPercent = 50;
                break;
            case Gen2Constants.damageRecoilEffect:
                move.recoilPercent = 25;
                break;
            case Gen2Constants.flailAndReversalEffect:
            case Gen2Constants.futureSightEffect:
                move.criticalChance = CriticalChance.NONE;
                break;
            case Gen2Constants.bindingEffect:
            case Gen2Constants.trappingEffect:
                move.isTrapMove = true;
                break;
            case Gen2Constants.razorWindEffect:
            case Gen2Constants.skyAttackEffect:
            case Gen2Constants.skullBashEffect:
            case Gen2Constants.solarbeamEffect:
            case Gen2Constants.semiInvulnerableEffect:
                move.isChargeMove = true;
                break;
            case Gen2Constants.hyperBeamEffect:
                move.isRechargeMove = true;
                break;
        }

        if (Gen2Constants.increasedCritMoves.contains(move.number)) {
            move.criticalChance = CriticalChance.INCREASED;
        }
    }

    @Override
    public void saveMoves() {
        int offs = romEntry.getIntValue("MoveDataOffset");
        for (int i = 1; i <= 251; i++) {
            int hitratio = (int) Math.round(moves[i].hitratio * 2.55);
            if (hitratio < 0) {
                hitratio = 0;
            }
            if (hitratio > 255) {
                hitratio = 255;
            }
            writeBytes(offs + (i - 1) * 7 + 1, new byte[]{(byte) moves[i].effectIndex, (byte) moves[i].power,
                    Gen2Constants.typeToByte(moves[i].type), (byte) hitratio, (byte) moves[i].pp});
        }
    }

    public List<Move> getMoves() {
        return Arrays.asList(moves);
    }

    private void loadBasicPokeStats(Species pkmn, int offset) {
        pkmn.setHp(rom[offset + Gen2Constants.bsHPOffset] & 0xFF);
        pkmn.setAttack(rom[offset + Gen2Constants.bsAttackOffset] & 0xFF);
        pkmn.setDefense(rom[offset + Gen2Constants.bsDefenseOffset] & 0xFF);
        pkmn.setSpeed(rom[offset + Gen2Constants.bsSpeedOffset] & 0xFF);
        pkmn.setSpatk(rom[offset + Gen2Constants.bsSpAtkOffset] & 0xFF);
        pkmn.setSpdef(rom[offset + Gen2Constants.bsSpDefOffset] & 0xFF);
        // Type
        pkmn.setPrimaryType(Gen2Constants.typeTable[rom[offset + Gen2Constants.bsPrimaryTypeOffset] & 0xFF]);
        pkmn.setSecondaryType(Gen2Constants.typeTable[rom[offset + Gen2Constants.bsSecondaryTypeOffset] & 0xFF]);
        // Only one type?
        if (pkmn.getSecondaryType(false) == pkmn.getPrimaryType(false)) {
            pkmn.setSecondaryType(null);
        }
        pkmn.setCatchRate(rom[offset + Gen2Constants.bsCatchRateOffset] & 0xFF);
        pkmn.setCommonHeldItem(items.get(rom[offset + Gen2Constants.bsCommonHeldItemOffset] & 0xFF));
        pkmn.setRareHeldItem(items.get(rom[offset + Gen2Constants.bsRareHeldItemOffset] & 0xFF));
        pkmn.setGrowthCurve(ExpCurve.fromByte(rom[offset + Gen2Constants.bsGrowthCurveOffset]));
        pkmn.setFrontImageDimensions(rom[offset + Gen2Constants.bsFrontImageDimensionsOffset] & 0xFF);

    }

    private void saveBasicPokeStats(Species pkmn, int offset) {
        writeByte(offset + Gen2Constants.bsHPOffset, (byte) pkmn.getHp());
        writeByte(offset + Gen2Constants.bsAttackOffset, (byte) pkmn.getAttack());
        writeByte(offset + Gen2Constants.bsDefenseOffset, (byte) pkmn.getDefense());
        writeByte(offset + Gen2Constants.bsSpeedOffset, (byte) pkmn.getSpeed());
        writeByte(offset + Gen2Constants.bsSpAtkOffset, (byte) pkmn.getSpatk());
        writeByte(offset + Gen2Constants.bsSpDefOffset, (byte) pkmn.getDefense());
        writeByte(offset + Gen2Constants.bsPrimaryTypeOffset, Gen2Constants.typeToByte(pkmn.getPrimaryType(false)));
        byte secondaryTypeByte = pkmn.getSecondaryType(false) == null ? rom[offset + Gen2Constants.bsPrimaryTypeOffset]
                : Gen2Constants.typeToByte(pkmn.getSecondaryType(false));
        writeByte(offset + Gen2Constants.bsSecondaryTypeOffset, secondaryTypeByte);
        writeByte(offset + Gen2Constants.bsCatchRateOffset, (byte) pkmn.getCatchRate());

        writeByte(offset + Gen2Constants.bsCommonHeldItemOffset, (byte) pkmn.getCommonHeldItem());
        writeByte(offset + Gen2Constants.bsRareHeldItemOffset, (byte) pkmn.getRareHeldItem());
        writeByte(offset + Gen2Constants.bsGrowthCurveOffset, pkmn.getGrowthCurve().toByte());
    }

    private String[] readPokemonNames() {
        int offs = romEntry.getIntValue("PokemonNamesOffset");
        int len = romEntry.getIntValue("PokemonNamesLength");
        String[] names = new String[Gen2Constants.pokemonCount + 1];
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            names[i] = readFixedLengthString(offs + (i - 1) * len, len);
        }
        return names;
    }

    @Override
    public List<Species> getStarters() {
        // Get the starters
        List<Species> starters = new ArrayList<>();
        starters.add(pokes[rom[romEntry.getArrayValue("StarterOffsets1")[0]] & 0xFF]);
        starters.add(pokes[rom[romEntry.getArrayValue("StarterOffsets2")[0]] & 0xFF]);
        starters.add(pokes[rom[romEntry.getArrayValue("StarterOffsets3")[0]] & 0xFF]);
        return starters;
    }

    @Override
    public boolean setStarters(List<Species> newStarters) {
        if (newStarters.size() != 3) {
            return false;
        }

        // Actually write

        for (int i = 0; i < 3; i++) {
            byte starter = (byte) newStarters.get(i).getNumber();
            int[] offsets = romEntry.getArrayValue("StarterOffsets" + (i + 1));
            for (int offset : offsets) {
                writeByte(offset, starter);
            }
        }

        // Attempt to replace text
        if (romEntry.getIntValue("CanChangeStarterText") > 0) {
            int[] starterTextOffsets = romEntry.getArrayValue("StarterTextOffsets");
            for (int i = 0; i < 3 && i < starterTextOffsets.length; i++) {
                writeVariableLengthString(String.format("%s?\\e", newStarters.get(i).getName()), starterTextOffsets[i], true);
            }
        }
        return true;
    }

    @Override
    public boolean hasStarterAltFormes() {
        return false;
    }

    @Override
    public int starterCount() {
        return 3;
    }

    @Override
    public boolean hasMultiplePlayerCharacters() {
        return romEntry.isCrystal();
    }

    @Override
    public boolean supportsStarterHeldItems() {
        return true;
    }

    @Override
    public List<Item> getStarterHeldItems() {
        List<Item> sHeldItems = new ArrayList<>();
        int[] shiOffsets = romEntry.getArrayValue("StarterHeldItems");
        for (int offset : shiOffsets) {
            sHeldItems.add(items.get(rom[offset] & 0xFF));
        }
        return sHeldItems;
    }

    @Override
    public void setStarterHeldItems(List<Item> items) {
        int[] shiOffsets = romEntry.getArrayValue("StarterHeldItems");
        if (items.size() != shiOffsets.length) {
            return;
        }
        Iterator<Item> sHeldItems = items.iterator();
        for (int offset : shiOffsets) {
            writeByte(offset, (byte) (sHeldItems.next().getId() & 0xFF));
        }
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        List<EncounterArea> encounterAreas = new ArrayList<>();

        readNormalEncounters(encounterAreas, useTimeOfDay);
        readFishingEncounters(encounterAreas, useTimeOfDay);
        readHeadbuttEncounters(encounterAreas);
        readBugCatchingContestEncounters(encounterAreas);

        Gen2Constants.tagEncounterAreas(encounterAreas, useTimeOfDay, romEntry.isCrystal());

        return encounterAreas;
    }

    private void readNormalEncounters(List<EncounterArea> encounterAreas, boolean useTimeOfDay) {
        int offset = romEntry.getIntValue("WildPokemonOffset");
        offset = readLandEncounters(offset, encounterAreas, useTimeOfDay); // Johto
        offset = readSeaEncounters(offset, encounterAreas); // Johto
        offset = readLandEncounters(offset, encounterAreas, useTimeOfDay); // Kanto
        offset = readSeaEncounters(offset, encounterAreas); // Kanto
        offset = readLandEncounters(offset, encounterAreas, useTimeOfDay); // Specials
        readSeaEncounters(offset, encounterAreas); // Specials
    }

    private int readLandEncounters(int offset, List<EncounterArea> areas, boolean useTimeOfDay) {
        String[] todNames = new String[]{"Morning", "Day", "Night"};
        while ((rom[offset] & 0xFF) != 0xFF) {
            int mapBank = rom[offset] & 0xFF;
            int mapNumber = rom[offset + 1] & 0xFF;
            String mapName = mapNames[mapBank][mapNumber];
            if (useTimeOfDay) {
                for (int i = 0; i < 3; i++) {
                    EncounterArea area = new EncounterArea();
                    area.setRate(rom[offset + 2 + i] & 0xFF);
                    area.setDisplayName(mapName + " Grass/Cave (" + todNames[i] + ")");
                    area.setEncounterType(EncounterType.WALKING);
                    for (int j = 0; j < Gen2Constants.landEncounterSlots; j++) {
                        Encounter enc = new Encounter();
                        enc.setLevel(rom[offset + 5 + (i * Gen2Constants.landEncounterSlots * 2) + (j * 2)] & 0xFF);
                        enc.setMaxLevel(0);
                        enc.setSpecies(pokes[rom[offset + 5 + (i * Gen2Constants.landEncounterSlots * 2) + (j * 2) + 1] & 0xFF]);
                        area.add(enc);
                    }
                    areas.add(area);
                }
            } else {
                // Use Day only
                EncounterArea area = new EncounterArea();
                area.setRate(rom[offset + 3] & 0xFF);
                area.setDisplayName(mapName + " Grass/Cave");
                area.setEncounterType(EncounterType.WALKING);
                for (int j = 0; j < Gen2Constants.landEncounterSlots; j++) {
                    Encounter enc = new Encounter();
                    enc.setLevel(rom[offset + 5 + Gen2Constants.landEncounterSlots * 2 + (j * 2)] & 0xFF);
                    enc.setMaxLevel(0);
                    enc.setSpecies(pokes[rom[offset + 5 + Gen2Constants.landEncounterSlots * 2 + (j * 2) + 1] & 0xFF]);
                    area.add(enc);
                }
                areas.add(area);
            }
            offset += 5 + 6 * Gen2Constants.landEncounterSlots;
        }
        return offset + 1;
    }

    private int readSeaEncounters(int offset, List<EncounterArea> areas) {
        while ((rom[offset] & 0xFF) != 0xFF) {
            int mapBank = rom[offset] & 0xFF;
            int mapNumber = rom[offset + 1] & 0xFF;
            String mapName = mapNames[mapBank][mapNumber];
            EncounterArea area = new EncounterArea();
            area.setRate(rom[offset + 2] & 0xFF);
            area.setDisplayName(mapName + " Surfing");
            area.setEncounterType(EncounterType.SURFING);
            for (int j = 0; j < Gen2Constants.seaEncounterSlots; j++) {
                Encounter enc = new Encounter();
                enc.setLevel(rom[offset + 3 + (j * 2)] & 0xFF);
                enc.setMaxLevel(0);
                enc.setSpecies(pokes[rom[offset + 3 + (j * 2) + 1] & 0xFF]);
                area.add(enc);
            }
            areas.add(area);
            offset += 3 + Gen2Constants.seaEncounterSlots * 2;
        }
        return offset + 1;
    }

    private void readFishingEncounters(List<EncounterArea> encounterAreas, boolean useTimeOfDay) {
        int offset = romEntry.getIntValue("FishingWildsOffset");
        int rootOffset = offset;
        for (int k = 0; k < Gen2Constants.fishingAreaCount; k++) {
            EncounterArea area = new EncounterArea();
            area.setDisplayName("Fishing " + Gen2Constants.fishingAreaNames[k]);
            area.setEncounterType(romEntry.isCrystal() && Gen2Constants.crystalUnusedFishingAreas.contains(k)
                    ? EncounterType.UNUSED : EncounterType.FISHING);
            for (int i = 0; i < Gen2Constants.pokesPerFishingArea; i++) {
                offset++;
                int pokeNum = rom[offset++] & 0xFF;
                int level = rom[offset++] & 0xFF;
                if (pokeNum == 0) {
                    if (!useTimeOfDay) {
                        // read the encounter they put here for DAY
                        int specialOffset = rootOffset + Gen2Constants.fishingAreaEntryLength
                                * Gen2Constants.pokesPerFishingArea * Gen2Constants.fishingAreaCount + level * 4 + 2;
                        Encounter enc = new Encounter();
                        enc.setSpecies(pokes[rom[specialOffset] & 0xFF]);
                        enc.setLevel(rom[specialOffset + 1] & 0xFF);
                        area.add(enc);
                    }
                    // else will be handled by code below
                } else {
                    Encounter enc = new Encounter();
                    enc.setSpecies(pokes[pokeNum]);
                    enc.setLevel(level);
                    area.add(enc);
                }
            }
            encounterAreas.add(area);
        }
        if (useTimeOfDay) {
            for (int k = 0; k < Gen2Constants.timeSpecificFishingAreaCount; k++) {
                EncounterArea area = new EncounterArea();
                area.setDisplayName("Time-Specific Fishing " + (k + 1));
                area.setEncounterType(EncounterType.FISHING);
                for (int i = 0; i < Gen2Constants.pokesPerTSFishingArea; i++) {
                    int pokeNum = rom[offset++] & 0xFF;
                    int level = rom[offset++] & 0xFF;
                    Encounter enc = new Encounter();
                    enc.setSpecies(pokes[pokeNum]);
                    enc.setLevel(level);
                    area.add(enc);
                }
                encounterAreas.add(area);
            }
        }
    }

    private void readHeadbuttEncounters(List<EncounterArea> encounterAreas) {
        int offset = romEntry.getIntValue("HeadbuttWildsOffset");
        int limit = romEntry.getIntValue("HeadbuttTableSize");
        String[] names = romEntry.isCrystal() ? Gen2Constants.headbuttAreaNamesCrystal : Gen2Constants.headbuttAreaNamesGS;
        for (int i = 0; i < limit; i++) {
            EncounterArea area = new EncounterArea();
            area.setDisplayName(names[i]);
            area.setEncounterType(EncounterType.INTERACT);
            while ((rom[offset] & 0xFF) != 0xFF) {
                offset++;
                int pokeNum = rom[offset++] & 0xFF;
                int level = rom[offset++] & 0xFF;
                Encounter enc = new Encounter();
                enc.setSpecies(pokes[pokeNum]);
                enc.setLevel(level);
                area.add(enc);
            }
            offset++;
            encounterAreas.add(area);
        }
    }

    private void readBugCatchingContestEncounters(List<EncounterArea> encounterAreas) {
        int offset = romEntry.getIntValue("BCCWildsOffset");
        EncounterArea area = new EncounterArea();
        area.setDisplayName("Bug Catching Contest");
        area.setEncounterType(EncounterType.WALKING);
        while ((rom[offset] & 0xFF) != 0xFF) {
            offset++;
            Encounter enc = new Encounter();
            enc.setSpecies(pokes[rom[offset++] & 0xFF]);
            enc.setLevel(rom[offset++] & 0xFF);
            enc.setMaxLevel(rom[offset++] & 0xFF);
            area.add(enc);
        }
        // Unown is banned for Bug Catching Contest (5/8/2016)
        area.banSpecies(pokes[SpeciesIDs.unown]);
        encounterAreas.add(area);
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        return getEncounters(useTimeOfDay).stream()
                .sorted(Comparator.comparingInt(a -> Gen2Constants.locationTagsTraverseOrder.indexOf(a.getLocationTag())))
                .collect(Collectors.toList());
    }

    @Override
    public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounters) {
        if (!havePatchedFleeing) {
            patchFleeing();
        }

        Iterator<EncounterArea> areaIterator = encounters.iterator();

        writeNormalEncounters(areaIterator, useTimeOfDay);
        writeFishingEncounters(areaIterator, useTimeOfDay);
        writeHeadbuttEncounters(areaIterator);
        writeBugCatchingContestEncounters(areaIterator);

    }

    private void writeNormalEncounters(Iterator<EncounterArea> areaIterator, boolean useTimeOfDay) {
        int offset = romEntry.getIntValue("WildPokemonOffset");
        offset = writeLandEncounters(offset, areaIterator, useTimeOfDay); // Johto
        offset = writeSeaEncounters(offset, areaIterator); // Johto
        offset = writeLandEncounters(offset, areaIterator, useTimeOfDay); // Kanto
        offset = writeSeaEncounters(offset, areaIterator); // Kanto
        offset = writeLandEncounters(offset, areaIterator, useTimeOfDay); // Specials
        writeSeaEncounters(offset, areaIterator); // Specials
    }

    private void writeFishingEncounters(Iterator<EncounterArea> areaIterator, boolean useTimeOfDay) {
        int offset;
        // Fishing Data
        offset = romEntry.getIntValue("FishingWildsOffset");
        for (int k = 0; k < Gen2Constants.fishingAreaCount; k++) {
            EncounterArea area = areaIterator.next();
            Iterator<Encounter> encounterIterator = area.iterator();
            for (int i = 0; i < Gen2Constants.pokesPerFishingArea; i++) {
                offset++;
                if (rom[offset] == 0) {
                    if (!useTimeOfDay) {
                        // overwrite with a static encounter
                        Encounter enc = encounterIterator.next();
                        rom[offset++] = (byte) enc.getSpecies().getNumber();
                        rom[offset++] = (byte) enc.getLevel();
                    } else {
                        // else handle below
                        offset += 2;
                    }
                } else {
                    Encounter enc = encounterIterator.next();
                    rom[offset++] = (byte) enc.getSpecies().getNumber();
                    rom[offset++] = (byte) enc.getLevel();
                }
            }
        }
        if (useTimeOfDay) {
            for (int k = 0; k < Gen2Constants.timeSpecificFishingAreaCount; k++) {
                EncounterArea area = areaIterator.next();
                Iterator<Encounter> encounterIterator = area.iterator();
                for (int i = 0; i < Gen2Constants.pokesPerTSFishingArea; i++) {
                    Encounter enc = encounterIterator.next();
                    rom[offset++] = (byte) enc.getSpecies().getNumber();
                    rom[offset++] = (byte) enc.getLevel();
                }
            }
        }
    }

    private void writeHeadbuttEncounters(Iterator<EncounterArea> areaIterator) {
        int offset;
        // Headbutt Data
        offset = romEntry.getIntValue("HeadbuttWildsOffset");
        int limit = romEntry.getIntValue("HeadbuttTableSize");
        for (int i = 0; i < limit; i++) {
            EncounterArea area = areaIterator.next();
            Iterator<Encounter> encounterIterator = area.iterator();
            while ((rom[offset] & 0xFF) != 0xFF) {
                Encounter enc = encounterIterator.next();
                offset++;
                rom[offset++] = (byte) enc.getSpecies().getNumber();
                rom[offset++] = (byte) enc.getLevel();
            }
            offset++;
        }
    }

    private void writeBugCatchingContestEncounters(Iterator<EncounterArea> areaIterator) {
        int offset;
        // Bug Catching Contest Data
        offset = romEntry.getIntValue("BCCWildsOffset");
        EncounterArea bugCatchingContestArea = areaIterator.next();
        Iterator<Encounter> bccEncounterIterator = bugCatchingContestArea.iterator();
        while ((rom[offset] & 0xFF) != 0xFF) {
            offset++;
            Encounter enc = bccEncounterIterator.next();
            rom[offset++] = (byte) enc.getSpecies().getNumber();
            rom[offset++] = (byte) enc.getLevel();
            rom[offset++] = (byte) enc.getMaxLevel();
        }
    }

    private int writeLandEncounters(int offset, Iterator<EncounterArea> areaIterator, boolean useTimeOfDay) {
        while ((rom[offset] & 0xFF) != 0xFF) {
            if (useTimeOfDay) {
                for (int i = 0; i < 3; i++) {
                    EncounterArea area = areaIterator.next();
                    Iterator<Encounter> encounterIterator = area.iterator();
                    for (int j = 0; j < Gen2Constants.landEncounterSlots; j++) {
                        Encounter enc = encounterIterator.next();
                        rom[offset + 5 + (i * Gen2Constants.landEncounterSlots * 2) + (j * 2)] = (byte) enc.getLevel();
                        rom[offset + 5 + (i * Gen2Constants.landEncounterSlots * 2) + (j * 2) + 1] = (byte) enc.getSpecies().getNumber();
                    }
                }
            } else {
                // Write the set to all 3 equally
                EncounterArea area = areaIterator.next();
                for (int i = 0; i < 3; i++) {
                    Iterator<Encounter> encounterIterator = area.iterator();
                    for (int j = 0; j < Gen2Constants.landEncounterSlots; j++) {
                        Encounter enc = encounterIterator.next();
                        rom[offset + 5 + (i * Gen2Constants.landEncounterSlots * 2) + (j * 2)] = (byte) enc.getLevel();
                        rom[offset + 5 + (i * Gen2Constants.landEncounterSlots * 2) + (j * 2) + 1] = (byte) enc.getSpecies().getNumber();
                    }
                }
            }
            offset += 5 + 6 * Gen2Constants.landEncounterSlots;
        }
        return offset + 1;
    }

    private int writeSeaEncounters(int offset, Iterator<EncounterArea> areaIterator) {
        while ((rom[offset] & 0xFF) != 0xFF) {
            EncounterArea area = areaIterator.next();
            Iterator<Encounter> encounterIterator = area.iterator();
            for (int j = 0; j < Gen2Constants.seaEncounterSlots; j++) {
                Encounter enc = encounterIterator.next();
                rom[offset + 3 + (j * 2)] = (byte) enc.getLevel();
                rom[offset + 3 + (j * 2) + 1] = (byte) enc.getSpecies().getNumber();
            }
            offset += 3 + Gen2Constants.seaEncounterSlots * 2;
        }
        return offset + 1;
    }

    @Override
    public List<Trainer> getTrainers() {
        if (trainers == null) {
            throw new IllegalStateException("Trainers have not been loaded.");
        }
        return trainers;
    }

    @Override
    // This is very similar to the implementation in Gen1RomHandler. As trainers is a private field though,
    // the two should only be reconciled during some bigger refactoring, where other private fields (e.g. pokemonList)
    // are considered.
    public void loadTrainers() {
        int trainerClassTableOffset = romEntry.getIntValue("TrainerDataTableOffset");
        int trainerClassAmount = romEntry.getIntValue("TrainerClassAmount");
        int[] trainersPerClass = romEntry.getArrayValue("TrainerDataClassCounts");
        List<String> tcnames = this.getTrainerClassNames();

        trainers = new ArrayList<>();

        int index = 0;
        for (int trainerClass = 0; trainerClass < trainerClassAmount; trainerClass++) {

            int offset = readPointer(trainerClassTableOffset + trainerClass * 2);

            for (int trainerNum = 0; trainerNum < trainersPerClass[trainerClass]; trainerNum++) {
                index++;
                Trainer tr = readTrainer(offset);
                tr.index = index;
                tr.trainerclass = trainerClass;
                tr.fullDisplayName = tcnames.get(trainerClass) + " " + tr.name;
                trainers.add(tr);

                offset += trainerToBytes(tr).length;
            }
        }

        tagTrainers();
    }

    private Trainer readTrainer(int offset) {
        Trainer tr = new Trainer();
        tr.offset = offset;
        tr.name = readVariableLengthString(offset, false);
        offset += lengthOfStringAt(offset, false);
        int dataType = rom[offset] & 0xFF;
        tr.poketype = dataType;
        offset++;
        while ((rom[offset] & 0xFF) != 0xFF) {
            //System.out.println(tr);
            TrainerPokemon tp = new TrainerPokemon();
            tp.setLevel(rom[offset] & 0xFF);
            tp.setSpecies(pokes[rom[offset + 1] & 0xFF]);
            offset += 2;
            if ((dataType & 2) == 2) {
                tp.setHeldItem(items.get(rom[offset] & 0xFF));
                offset++;
            }
            if ((dataType & 1) == 1) {
                for (int move = 0; move < 4; move++) {
                    tp.getMoves()[move] = rom[offset + move] & 0xFF;
                }
                offset += 4;
            }
            tr.pokemon.add(tp);
        }
        return tr;
    }

    private void tagTrainers() {
        Gen2Constants.universalTrainerTags(trainers);
        if (romEntry.isCrystal()) {
            Gen2Constants.crystalTags(trainers);
        } else {
            Gen2Constants.goldSilverTags(trainers);
        }
    }

    @Override
    public List<Integer> getMainPlaythroughTrainers() {
        return new ArrayList<>(); // Not implemented
    }

    @Override
    public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
        List<Integer> eliteFourIndices = new ArrayList<>();
        List<Trainer> allTrainers = getTrainers();
        for (int i = 0; i < allTrainers.size(); i++) {
            Trainer tr = allTrainers.get(i);
            if (tr.tag != null && ((tr.tag.contains("ELITE") || tr.tag.contains("CHAMPION")))) {
                eliteFourIndices.add(i + 1);
            }

        }
        return eliteFourIndices;
    }

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        return Gen2Constants.gymAndEliteThemes;
    }

    @Override
    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }

    @Override
    public void saveTrainers() {
        if (trainers == null) {
            throw new IllegalStateException("Trainers are not loaded");
        }

        int trainerClassTableOffset = romEntry.getIntValue("TrainerDataTableOffset");
        int trainerClassAmount = romEntry.getIntValue("TrainerClassAmount");
        int[] trainersPerClass = romEntry.getArrayValue("TrainerDataClassCounts");

        Iterator<Trainer> trainerIterator = getTrainers().iterator();
        for (int trainerClassNum = 0; trainerClassNum < trainerClassAmount; trainerClassNum++) {
            if (trainersPerClass[trainerClassNum] == 0) continue;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            for (int trainerNum = 0; trainerNum < trainersPerClass[trainerClassNum]; trainerNum++) {
                Trainer tr = trainerIterator.next();
                if (tr.trainerclass != trainerClassNum) {
                    System.err.println("Trainer mismatch: " + tr.name);
                }
                byte[] trainerBytes = trainerToBytes(tr);
                baos.write(trainerBytes, 0, trainerBytes.length);
            }

            byte[] trainersOfClassBytes = baos.toByteArray();
            int pointerOffset = trainerClassTableOffset + trainerClassNum * 2;
            int trainersPerThisClass = trainersPerClass[trainerClassNum];
            new SameBankDataRewriter<byte[]>().rewriteData(pointerOffset, trainersOfClassBytes, b -> b,
                    oldDataOffset -> lengthOfTrainerClassAt(oldDataOffset, trainersPerThisClass));
        }
    }

    private byte[] trainerToBytes(Trainer trainer) {
        // sometimes it's practical to use a baos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] nameBytes = trainerNameToBytes(trainer);
        baos.write(nameBytes, 0, nameBytes.length);
        baos.write(trainer.poketype);
        for (TrainerPokemon tp : trainer.pokemon) {
            byte[] tpBytes = trainerPokemonToBytes(tp, trainer);
            baos.write(tpBytes, 0, tpBytes.length);
        }
        baos.write(Gen2Constants.trainerDataTerminator);

        return baos.toByteArray();
    }

    private byte[] trainerNameToBytes(Trainer trainer) {
        int trainerNameLength = internalStringLength(trainer.name) + 1;
        byte[] trainerNameBytes = new byte[trainerNameLength];
        writeFixedLengthString(trainerNameBytes, trainer.name, 0, trainerNameLength);
        return trainerNameBytes;
    }

    private byte[] trainerPokemonToBytes(TrainerPokemon tp, Trainer trainer) {
        byte[] data = new byte[trainerPokemonDataLength(trainer)];
        int offset = 0;
        data[offset] = (byte) tp.getLevel();
        data[offset + 1] = (byte) tp.getSpecies().getNumber();
        offset += 2;
        if (trainer.pokemonHaveItems()) {
            byte itemId = tp.getHeldItem() == null ? 0 : (byte) tp.getHeldItem().getId();
            data[offset] = itemId;
            offset++;
        }
        if (trainer.pokemonHaveCustomMoves()) {
            if (tp.isResetMoves()) {
                resetTrainerPokemonMoves(tp);
            }
            data[offset] = (byte) tp.getMoves()[0];
            data[offset + 1] = (byte) tp.getMoves()[1];
            data[offset + 2] = (byte) tp.getMoves()[2];
            data[offset + 3] = (byte) tp.getMoves()[3];
        }
        return data;
    }

    private int trainerPokemonDataLength(Trainer trainer) {
        return 2 + (trainer.pokemonHaveItems() ? 1 : 0) + (trainer.pokemonHaveCustomMoves() ? 4 : 0);
    }

    private void resetTrainerPokemonMoves(TrainerPokemon tp) {
        // made quickly while refactoring trainer writing, might be applicable in more/better places
        // (including other gens)
        // TODO: look at the above
        tp.setMoves(RomFunctions.getMovesAtLevel(tp.getSpecies().getNumber(), this.getMovesLearnt(), tp.getLevel()));
    }

    private int lengthOfTrainerClassAt(int offset, int numberOfTrainers) {
        int sum = 0;
        for (int i = 0; i < numberOfTrainers; i++) {
            Trainer trainer = readTrainer(offset);
            int trainerLength = trainerToBytes(trainer).length;
            sum += trainerLength;
            offset += trainerLength;
        }
        return sum;
    }

    @Override
    public boolean canAddPokemonToBossTrainers() {
        // because there isn't enough space in the bank with trainer data; the Japanese ROMs are smaller
        return romEntry.isNonJapanese();
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        // because there isn't enough space in the bank with trainer data; the Japanese ROMs are smaller
        return romEntry.isNonJapanese();
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        // because there isn't enough space in the bank with trainer data
        return false;
    }

    @Override
    public boolean canAddHeldItemsToBossTrainers() {
        return romEntry.isNonJapanese();
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        return romEntry.isNonJapanese();
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        // there should be enough space in the trainer data bank for the international Crystal versions,
        // but the randomizer needs better space allocation methods to use it
        return romEntry.isNonJapanese() && !romEntry.isCrystal();
    }

    @Override
    public Set<Item> getAllConsumableHeldItems() {
        return itemIdsToSet(Gen2Constants.consumableHeldItems);
    }

    @Override
    public Set<Item> getAllHeldItems() {
        return itemIdsToSet(Gen2Constants.allHeldItems);
    }

    @Override
    public List<Item> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        List<Integer> ids = new ArrayList<>(Gen2Constants.generalPurposeConsumableItems);

        if (!consumableOnly) {
            ids.addAll(Gen2Constants.generalPurposeItems);

            for (int moveIdx : pokeMoves) {
                Move move = moves.get(moveIdx);
                if (move == null) {
                    continue;
                }
                ids.addAll(Gen2Constants.typeBoostingItems.get(move.type));
            }

            List<Integer> speciesItems = Gen2Constants.speciesBoostingItems.get(tp.getSpecies().getNumber());
            if (speciesItems != null) {
                for (int i = 0; i < 6; i++) {  // Increase the likelihood of using species specific items.
                    ids.addAll(speciesItems);
                }
            }
        }
        return ids.stream().map(items::get).collect(Collectors.toList());
    }

    @Override
    public List<Species> getSpecies() {
        return speciesList;
    }

    @Override
    public List<Species> getSpeciesInclFormes() {
        return speciesList;
    }

    @Override
    public SpeciesSet getAltFormes() {
        return new SpeciesSet();
    }

    @Override
    public List<MegaEvolution> getMegaEvolutions() {
        return new ArrayList<>();
    }

    @Override
    public Species getAltFormeOfSpecies(Species base, int forme) {
        return base;
    }

    @Override
    public SpeciesSet getIrregularFormes() {
        return new SpeciesSet();
    }

    @Override
    public boolean hasFunctionalFormes() {
        return false;
    }

    @Override
    protected void loadMovesLearnt() {
        Map<Integer, List<MoveLearnt>> movesets = new TreeMap<>();
        int pointersOffset = romEntry.getIntValue("PokemonMovesetsTableOffset");
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            int pointer = readPointer(pointersOffset + (i - 1) * 2);
            Species pkmn = pokes[i];
            // Skip over evolution data
            while (rom[pointer] != 0) {
                if (rom[pointer] == 5) {
                    pointer += 4;
                } else {
                    pointer += 3;
                }
            }
            List<MoveLearnt> ourMoves = new ArrayList<>();
            pointer++;
            while (rom[pointer] != 0) {
                MoveLearnt learnt = new MoveLearnt();
                learnt.level = rom[pointer] & 0xFF;
                learnt.move = rom[pointer + 1] & 0xFF;
                ourMoves.add(learnt);
                pointer += 2;
            }
            movesets.put(pkmn.getNumber(), ourMoves);
        }
        setMovesLearnt(movesets);
    }

    @Override
    public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
        return movesets;
    }

    @Override
    public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets) {
        this.movesets = movesets;
    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        return Gen2Constants.bannedLevelupMoves;
    }

    @Override
    public Map<Integer, List<Integer>> getEggMoves() {
        Map<Integer, List<Integer>> eggMoves = new TreeMap<>();
        int tableOffset = romEntry.getIntValue("EggMovesTableOffset");
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            int pointerOffset = tableOffset + (i - 1) * 2;
            int eggMoveOffset = readPointer(pointerOffset);
            List<Integer> moves = new ArrayList<>();
            int val = rom[eggMoveOffset] & 0xFF;
            while (val != 0xFF) {
                moves.add(val);
                eggMoveOffset++;
                val = rom[eggMoveOffset] & 0xFF;
            }
            if (!moves.isEmpty()) {
                eggMoves.put(i, moves);
            }
        }
        return eggMoves;
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
        int tableOffset = romEntry.getIntValue("EggMovesTableOffset");
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            if (eggMoves.containsKey(i)) {
                int pointerOffset = tableOffset + (i - 1) * 2;
                new SameBankDataRewriter<List<Integer>>().rewriteData(pointerOffset, eggMoves.get(i), this::eggMovesToBytes,
                        oldDataOffset -> lengthOfDataWithTerminatorAt(oldDataOffset, Gen2Constants.eggMovesTerminator));
            }
        }
    }

    private byte[] eggMovesToBytes(List<Integer> eggMoves) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        eggMoves.forEach(baos::write);
        baos.write(Gen2Constants.eggMovesTerminator);
        return baos.toByteArray();
    }

    public static class StaticPokemon {
        protected int[] speciesOffsets;
        protected int[] levelOffsets;

        public StaticPokemon(int[] speciesOffsets, int[] levelOffsets) {
            this.speciesOffsets = speciesOffsets;
            this.levelOffsets = levelOffsets;
        }

        public Species getPokemon(Gen2RomHandler rh) {
            return rh.pokes[rh.rom[speciesOffsets[0]] & 0xFF];
        }

        public void setPokemon(Gen2RomHandler rh, Species pkmn) {
            for (int offset : speciesOffsets) {
                rh.writeByte(offset, (byte) pkmn.getNumber());
            }
        }

        public int getLevel(byte[] rom, int i) {
            if (levelOffsets.length <= i) {
                return 1;
            }
            return rom[levelOffsets[i]];
        }

        public void setLevel(byte[] rom, int level, int i) {
            if (levelOffsets.length > i) { // Might not have a level entry e.g., it's an egg
                rom[levelOffsets[i]] = (byte) level;
            }
        }
    }

    public static class StaticPokemonGameCorner extends StaticPokemon {
        public StaticPokemonGameCorner(int[] speciesOffsets, int[] levelOffsets) {
            super(speciesOffsets, levelOffsets);
        }

        @Override
        public void setPokemon(Gen2RomHandler rh, Species pkmn) {
            // Last offset is a pointer to the name
            int offsetSize = speciesOffsets.length;
            for (int i = 0; i < offsetSize - 1; i++) {
                rh.rom[speciesOffsets[i]] = (byte) pkmn.getNumber();
            }
            rh.writePaddedPokemonName(pkmn.getName(), rh.romEntry.getIntValue("GameCornerPokemonNameLength"),
                    speciesOffsets[offsetSize - 1]);
        }
    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        List<StaticEncounter> statics = new ArrayList<>();
        int[] staticEggOffsets = romEntry.getArrayValue("StaticEggPokemonOffsets");
        if (romEntry.getIntValue("StaticPokemonSupport") > 0) {
            for (int i = 0; i < romEntry.getStaticPokemon().size(); i++) {
                int currentOffset = i;
                StaticPokemon sp = romEntry.getStaticPokemon().get(i);
                StaticEncounter se = new StaticEncounter();
                se.setSpecies(sp.getPokemon(this));
                se.setLevel(sp.getLevel(rom, 0));
                se.setEgg(Arrays.stream(staticEggOffsets).anyMatch(x -> x == currentOffset));
                statics.add(se);
            }
        }
        if (romEntry.getIntValue("StaticPokemonOddEggOffset") > 0) {
            int oeOffset = romEntry.getIntValue("StaticPokemonOddEggOffset");
            int oeSize = romEntry.getIntValue("StaticPokemonOddEggDataSize");
            for (int i = 0; i < Gen2Constants.oddEggPokemonCount; i++) {
                StaticEncounter se = new StaticEncounter();
                se.setSpecies(pokes[rom[oeOffset + i * oeSize] & 0xFF]);
                se.setEgg(true);
                statics.add(se);
            }
        }
        return statics;
    }

    @Override
    public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
        if (romEntry.getIntValue("StaticPokemonSupport") == 0) {
            return false;
        }
        if (!havePatchedFleeing) {
            patchFleeing();
        }

        int desiredSize = romEntry.getStaticPokemon().size();
        if (romEntry.getIntValue("StaticPokemonOddEggOffset") > 0) {
            desiredSize += Gen2Constants.oddEggPokemonCount;
        }

        if (staticPokemon.size() != desiredSize) {
            return false;
        }

        Iterator<StaticEncounter> statics = staticPokemon.iterator();
        for (int i = 0; i < romEntry.getStaticPokemon().size(); i++) {
            StaticEncounter currentStatic = statics.next();
            StaticPokemon sp = romEntry.getStaticPokemon().get(i);
            sp.setPokemon(this, currentStatic.getSpecies());
            sp.setLevel(rom, currentStatic.getLevel(), 0);
        }

        if (romEntry.getIntValue("StaticPokemonOddEggOffset") > 0) {
            int oeOffset = romEntry.getIntValue("StaticPokemonOddEggOffset");
            int oeSize = romEntry.getIntValue("StaticPokemonOddEggDataSize");
            for (int i = 0; i < Gen2Constants.oddEggPokemonCount; i++) {
                int oddEggPokemonNumber = statics.next().getSpecies().getNumber();
                writeByte(oeOffset + i * oeSize, (byte) oddEggPokemonNumber);
                setMovesForOddEggPokemon(oddEggPokemonNumber, oeOffset + i * oeSize);
            }
        }

        return true;
    }

    // This method depends on movesets being randomized before static Pokemon. This is currently true,
    // but may not *always* be true, so take care.
    private void setMovesForOddEggPokemon(int oddEggPokemonNumber, int oddEggPokemonOffset) {
        // Determine the level 5 moveset, minus Dizzy Punch
        Map<Integer, List<MoveLearnt>> movesets = this.getMovesLearnt();
        List<Move> moves = this.getMoves();
        List<MoveLearnt> moveset = movesets.get(oddEggPokemonNumber);
        Queue<Integer> level5Moveset = new LinkedList<>();
        int currentMoveIndex = 0;
        while (moveset.size() > currentMoveIndex && moveset.get(currentMoveIndex).level <= 5) {
            if (level5Moveset.size() == 4) {
                level5Moveset.remove();
            }
            level5Moveset.add(moveset.get(currentMoveIndex).move);
            currentMoveIndex++;
        }

        // Now add Dizzy Punch and write the moveset and PP
        if (level5Moveset.size() == 4) {
            level5Moveset.remove();
        }
        level5Moveset.add(MoveIDs.dizzyPunch);
        for (int i = 0; i < 4; i++) {
            int move = 0;
            int pp = 0;
            if (!level5Moveset.isEmpty()) {
                move = level5Moveset.remove();
                pp = moves.get(move).pp; // This assumes the ordering of moves matches the internal order
            }
            writeByte(oddEggPokemonOffset + 2 + i, (byte) move);
            writeByte(oddEggPokemonOffset + 23 + i, (byte) pp);
        }
    }

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        // Ban Unown because they don't show up unless you complete a puzzle in the Ruins of Alph.
        return new SpeciesSet(Collections.singletonList(pokes[SpeciesIDs.unown]));
    }

    @Override
    public boolean hasStaticAltFormes() {
        return false;
    }

    @Override
    public SpeciesSet getBannedForStaticPokemon() {
        return new SpeciesSet(Collections.singletonList(pokes[SpeciesIDs.unown]));
    }

    @Override
    public boolean hasMainGameLegendaries() {
        return false;
    }

    @Override
    public List<Integer> getMainGameLegendaries() {
        return new ArrayList<>();
    }

    @Override
    public List<Integer> getSpecialMusicStatics() {
        return new ArrayList<>();
    }

    @Override
    public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {

    }

    @Override
    public boolean hasStaticMusicFix() {
        return false;
    }

    @Override
    public List<TotemPokemon> getTotemPokemon() {
        return new ArrayList<>();
    }

    @Override
    public void setTotemPokemon(List<TotemPokemon> totemPokemon) {

    }

    private void writePaddedPokemonName(String name, int length, int offset) {
        String paddedName = String.format("%-" + length + "s", name);
        byte[] rawData = translateString(paddedName);
        System.arraycopy(rawData, 0, rom, offset, length);
    }

    @Override
    public List<Integer> getTMMoves() {
        List<Integer> tms = new ArrayList<>();
        int offset = romEntry.getIntValue("TMMovesOffset");
        for (int i = 1; i <= Gen2Constants.tmCount; i++) {
            tms.add(rom[offset + (i - 1)] & 0xFF);
        }
        return tms;
    }

    @Override
    public List<Integer> getHMMoves() {
        List<Integer> hms = new ArrayList<>();
        int offset = romEntry.getIntValue("TMMovesOffset");
        for (int i = 1; i <= Gen2Constants.hmCount; i++) {
            hms.add(rom[offset + Gen2Constants.tmCount + (i - 1)] & 0xFF);
        }
        return hms;
    }

    @Override
    public void setTMMoves(List<Integer> moveIndexes) {
        int offset = romEntry.getIntValue("TMMovesOffset");
        for (int i = 1; i <= Gen2Constants.tmCount; i++) {
            rom[offset + (i - 1)] = moveIndexes.get(i - 1).byteValue();
        }

        // TM Text
        String[] moveNames = readMoveNames();
        for (GBCTMTextEntry tte : romEntry.getTMTexts()) {
            String moveName = moveNames[moveIndexes.get(tte.getNumber() - 1)];
            String text = tte.getTemplate().replace("%m", moveName);
            writeVariableLengthString(text, tte.getOffset(), true);
        }
    }

    @Override
    public int getTMCount() {
        return Gen2Constants.tmCount;
    }

    @Override
    public int getHMCount() {
        return Gen2Constants.hmCount;
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        Map<Species, boolean[]> compat = new TreeMap<>();
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            int baseStatsOffset = romEntry.getIntValue("PokemonStatsOffset") + (i - 1) * Gen2Constants.baseStatsEntrySize;
            Species pkmn = pokes[i];
            boolean[] flags = new boolean[Gen2Constants.tmCount + Gen2Constants.hmCount + 1];
            for (int j = 0; j < 8; j++) {
                readByteIntoFlags(flags, j * 8 + 1, baseStatsOffset + Gen2Constants.bsTMHMCompatOffset + j);
            }
            compat.put(pkmn, flags);
        }
        return compat;
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int baseStatsOffset = romEntry.getIntValue("PokemonStatsOffset") + (pkmn.getNumber() - 1)
                    * Gen2Constants.baseStatsEntrySize;
            for (int j = 0; j < 8; j++) {
                if (!romEntry.isCrystal() || j != 7) {
                    writeByte(baseStatsOffset + Gen2Constants.bsTMHMCompatOffset + j,
                            getByteFromFlags(flags, j * 8 + 1));
                } else {
                    // Move tutor data
                    // bits 1,2,3 of byte 7
                    int changedByte = getByteFromFlags(flags, j * 8 + 1) & 0xFF;
                    int currentByte = rom[baseStatsOffset + Gen2Constants.bsTMHMCompatOffset + j];
                    changedByte |= ((currentByte >> 1) & 0x01) << 1;
                    changedByte |= ((currentByte >> 2) & 0x01) << 2;
                    changedByte |= ((currentByte >> 3) & 0x01) << 3;
                    writeByte(baseStatsOffset + 0x18 + j, (byte) changedByte);
                }
            }
        }
    }

    @Override
    public boolean hasMoveTutors() {
        return romEntry.isCrystal();
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        if (romEntry.isCrystal()) {
            List<Integer> mtMoves = new ArrayList<>();
            for (int offset : romEntry.getArrayValue("MoveTutorMoves")) {
                mtMoves.add(rom[offset] & 0xFF);
            }
            return mtMoves;
        }
        return new ArrayList<>();
    }

    @Override
    public void setMoveTutorMoves(List<Integer> moves) {
        if (!romEntry.isCrystal()) {
            return;
        }
        if (moves.size() != Gen2Constants.mtCount) {
            throw new IllegalArgumentException("Wrong number of move tutor moves. Should be " +
                    Gen2Constants.mtCount + ", is " + moves.size() + ".");
        }
        int menuPointerOffset = romEntry.getIntValue("MoveTutorMenuOffset");
        if (menuPointerOffset <= 0) {
            throw new IllegalStateException("ROM does not support move tutor randomization.");
        }

        Iterator<Integer> mvList = moves.iterator();
        for (int offset : romEntry.getArrayValue("MoveTutorMoves")) {
            writeByte(offset, mvList.next().byteValue());
        }

        new SameBankDataRewriter<List<Integer>>().rewriteData(menuPointerOffset, moves,
                this::moveTutorMovesToDialogueOptionBytes, this::lengthOfDialogueOptionAt);
    }

    private byte[] moveTutorMovesToDialogueOptionBytes(List<Integer> moves) {
        String[] moveNames = readMoveNames();
        String[] options = new String[]{moveNames[moves.get(0)], moveNames[moves.get(1)], moveNames[moves.get(2)],
                romEntry.getStringValue("CancelString")};
        return dialogueOptionToBytes(options);
    }

    private byte[] dialogueOptionToBytes(String[] options) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(Gen2Constants.dialogueOptionInitByte);
        baos.write(options.length);
        for (String option : options) {
            byte[] bytes = translateString(option);
            baos.write(bytes, 0, bytes.length);
            baos.write(GBConstants.stringTerminator);
        }
        return baos.toByteArray();
    }

    private int lengthOfDialogueOptionAt(int offset) {
        if (rom[offset] != Gen2Constants.dialogueOptionInitByte) {
            throw new IllegalArgumentException("There is either no dialogue option at " + offset +
                    ", or it is in a format not supported by the randomizer.");
        }
        int numberOfOptions = rom[offset + 1];
        int length = 2;
        length += lengthOfDataWithTerminatorsAt(offset + length, GBConstants.stringTerminator,
                numberOfOptions);
        return length;
    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        if (!romEntry.isCrystal()) {
            return new TreeMap<>();
        }
        Map<Species, boolean[]> compat = new TreeMap<>();
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            int baseStatsOffset = romEntry.getIntValue("PokemonStatsOffset") + (i - 1) * Gen2Constants.baseStatsEntrySize;
            Species pkmn = pokes[i];
            boolean[] flags = new boolean[4];
            int mtByte = rom[baseStatsOffset + Gen2Constants.bsMTCompatOffset] & 0xFF;
            for (int j = 1; j <= 3; j++) {
                flags[j] = ((mtByte >> j) & 0x01) > 0;
            }
            compat.put(pkmn, flags);
        }
        return compat;
    }

    @Override
    public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {
        if (!romEntry.isCrystal()) {
            return;
        }
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int baseStatsOffset = romEntry.getIntValue("PokemonStatsOffset") + (pkmn.getNumber() - 1)
                    * Gen2Constants.baseStatsEntrySize;
            int origMtByte = rom[baseStatsOffset + Gen2Constants.bsMTCompatOffset] & 0xFF;
            int mtByte = origMtByte & 0x01;
            for (int j = 1; j <= 3; j++) {
                mtByte |= flags[j] ? (1 << j) : 0;
            }
            writeByte(baseStatsOffset + Gen2Constants.bsMTCompatOffset, (byte) mtByte);
        }
    }

    @Override
    public String getROMName() {
        if (isVietCrystal) {
            return Gen2Constants.vietCrystalROMName;
        }
        return "Pokemon " + romEntry.getName();
    }

    private static int find(byte[] haystack, String hexString) {
        if (hexString.length() % 2 != 0) {
            return -3; // error
        }
        byte[] searchFor = new byte[hexString.length() / 2];
        for (int i = 0; i < searchFor.length; i++) {
            searchFor[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        List<Integer> found = RomFunctions.search(haystack, searchFor);
        if (found.isEmpty()) {
            return -1; // not found
        } else if (found.size() > 1) {
            return -2; // not unique
        } else {
            return found.get(0);
        }
    }

    @Override
    public boolean hasEncounterLocations() {
        return true;
    }

    @Override
    public boolean hasTimeBasedEncounters() {
        return true; // All GSC do
    }

    @Override
    public boolean hasWildAltFormes() {
        return false;
    }

    @Override
    public void loadEvolutions() {
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                pkmn.getEvolutionsFrom().clear();
                pkmn.getEvolutionsTo().clear();
            }
        }

        int pointersOffset = romEntry.getIntValue("PokemonMovesetsTableOffset");
        for (int i = 1; i <= Gen2Constants.pokemonCount; i++) {
            int pointer = readPointer(pointersOffset + (i - 1) * 2);
            Species pkmn = pokes[i];
            while (rom[pointer] != 0) {
                int method = rom[pointer] & 0xFF;
                int otherPoke = rom[pointer + 2 + (method == 5 ? 1 : 0)] & 0xFF;
                EvolutionType type = Gen2Constants.evolutionTypeFromIndex(method);
                int extraInfo = 0;
                if (type == EvolutionType.TRADE) {
                    int itemNeeded = rom[pointer + 1] & 0xFF;
                    if (itemNeeded != 0xFF) {
                        type = EvolutionType.TRADE_ITEM;
                        extraInfo = itemNeeded;
                    }
                } else if (type == EvolutionType.LEVEL_ATTACK_HIGHER) {
                    int tyrogueCond = rom[pointer + 2] & 0xFF;
                    if (tyrogueCond == 2) {
                        type = EvolutionType.LEVEL_DEFENSE_HIGHER;
                    } else if (tyrogueCond == 3) {
                        type = EvolutionType.LEVEL_ATK_DEF_SAME;
                    }
                    extraInfo = rom[pointer + 1] & 0xFF;
                } else if (type == EvolutionType.HAPPINESS) {
                    int happCond = rom[pointer + 1] & 0xFF;
                    if (happCond == 2) {
                        type = EvolutionType.HAPPINESS_DAY;
                    } else if (happCond == 3) {
                        type = EvolutionType.HAPPINESS_NIGHT;
                    }
                } else {
                    extraInfo = rom[pointer + 1] & 0xFF;
                }
                Evolution evo = new Evolution(pokes[i], pokes[otherPoke], type, extraInfo);
                if (!pkmn.getEvolutionsFrom().contains(evo)) {
                    pkmn.getEvolutionsFrom().add(evo);
                    pokes[otherPoke].getEvolutionsTo().add(evo);
                }
                pointer += (method == 5 ? 4 : 3);
            }
        }
    }

    @Override
    public void removeImpossibleEvolutions(Settings settings) {
        // no move evos, so no need to check for those
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                for (Evolution evol : pkmn.getEvolutionsFrom()) {
                    if (evol.getType() == EvolutionType.TRADE || evol.getType() == EvolutionType.TRADE_ITEM) {
                        // change
                        if (evol.getFrom().getNumber() == SpeciesIDs.slowpoke) {
                            // Slowpoke: Make water stone => Slowking
                            evol.setType(EvolutionType.STONE);
                            evol.setExtraInfo(Gen2ItemIDs.waterStone);
                            addEvoUpdateStone(impossibleEvolutionUpdates, evol, items.get(Gen2ItemIDs.waterStone));
                        } else if (evol.getFrom().getNumber() == SpeciesIDs.seadra) {
                            // Seadra: level 40
                            evol.setType(EvolutionType.LEVEL);
                            evol.setExtraInfo(40); // level
                            addEvoUpdateLevel(impossibleEvolutionUpdates, evol);
                        } else if (evol.getFrom().getNumber() == SpeciesIDs.poliwhirl || evol.getType() == EvolutionType.TRADE) {
                            // Poliwhirl or any of the original 4 trade evos
                            // Level 37
                            evol.setType(EvolutionType.LEVEL);
                            evol.setExtraInfo(37); // level
                            addEvoUpdateLevel(impossibleEvolutionUpdates, evol);
                        } else {
                            // A new trade evo of a single stage Pokemon
                            // level 30
                            evol.setType(EvolutionType.LEVEL);
                            evol.setExtraInfo(30); // level
                            addEvoUpdateLevel(impossibleEvolutionUpdates, evol);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void makeEvolutionsEasier(Settings settings) {
        // Reduce the amount of happiness required to evolve.
        int offset = find(rom, Gen2Constants.friendshipValueForEvoLocator);
        if (offset > 0) {
            // The thing we're looking at is actually one byte before what we
            // want to change; this makes it work in both G/S and Crystal.
            offset++;

            // Amount of required happiness for all happiness evolutions.
            if (rom[offset] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
                writeByte(offset, (byte) GlobalConstants.easierHappinessToEvolve);
            }
        }
    }

    @Override
    public void removeTimeBasedEvolutions() {
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                for (Evolution evol : pkmn.getEvolutionsFrom()) {
                    // In Gen 2, only Eevee has a time-based evolution.
                    if (evol.getType() == EvolutionType.HAPPINESS_DAY) {
                        // Eevee: Make sun stone => Espeon
                        evol.setType(EvolutionType.STONE);
                        evol.setExtraInfo(Gen2ItemIDs.sunStone);
                        addEvoUpdateStone(timeBasedEvolutionUpdates, evol, items.get(Gen2ItemIDs.sunStone));
                    } else if (evol.getType() == EvolutionType.HAPPINESS_NIGHT) {
                        // Eevee: Make moon stone => Umbreon
                        evol.setType(EvolutionType.STONE);
                        evol.setExtraInfo(Gen2ItemIDs.moonStone);
                        addEvoUpdateStone(timeBasedEvolutionUpdates, evol, items.get(Gen2ItemIDs.moonStone));
                    }
                }
            }
        }

    }

    @Override
    public boolean hasShopSupport() {
        return true;
    }

    @Override
    public Map<Integer, Shop> getShopItems() {
        List<Shop> shops = readShops();

        Map<Integer, Shop> shopMap = new HashMap<>();
        for (int i = 0; i < shops.size(); i++) {
            if (!Gen2Constants.skipShops.contains(i)) {
                shopMap.put(i, shops.get(i));
            }
        }
        return shopMap;
    }

    private List<Shop> readShops() {
        List<Shop> shops = new ArrayList<>();

        int tableOffset = romEntry.getIntValue("ShopItemOffset");
        int shopAmount = romEntry.getIntValue("ShopAmount");
        int shopNum = 0;
        while (shopNum < shopAmount) {
            int shopOffset = readPointer(tableOffset + shopNum * 2, bankOf(tableOffset));
            Shop shop = readShop(shopOffset);
            shop.setName(Gen2Constants.shopNames.get(shopNum));
            shop.setMainGame(Gen2Constants.mainGameShops.contains(shopNum));
            shops.add(shop);
            shopNum++;
        }
        return shops;
    }

    private Shop readShop(int offset) {
        Shop shop = new Shop();
        shop.setItems(new ArrayList<>());
        int itemAmount = rom[offset++];
        for (int itemNum = 0; itemNum < itemAmount; itemNum++) {
            shop.getItems().add(items.get((int) rom[offset++] & 0xFF));
        }
        if (rom[offset] != Gen2Constants.shopItemsTerminator) {
            throw new RomIOException("Invalid shop data");
        }
        return shop;
    }

    @Override
    public void setShopItems(Map<Integer, Shop> shopItems) {
        int tableOffset = romEntry.getIntValue("ShopItemOffset");

        for (Map.Entry<Integer, Shop> entry : shopItems.entrySet()) {
            int shopNum = entry.getKey();
            Shop shop = entry.getValue();
            new SameBankDataRewriter<Shop>().rewriteData(tableOffset + shopNum * 2, shop, this::shopToBytes,
                    this::lengthOfShopAt);
        }
    }

    private byte[] shopToBytes(Shop shop) {
        byte[] data = new byte[shop.getItems().size() + 2];
        data[0] = (byte) shop.getItems().size();
        for (int i = 0; i < shop.getItems().size(); i++) {
            data[i + 1] = (byte) (shop.getItems().get(i).getId() & 0xFF);
        }
        data[data.length - 1] = (byte) 0xFF;
        return data;
    }

    private int lengthOfShopAt(int offset) {
        return shopToBytes(readShop(offset)).length;
    }

    public List<Integer> getShopPrices() {
        int itemAttributesOffset = romEntry.getIntValue("ItemAttributesOffset");
        int entrySize = Gen2Constants.itemAttributesEntrySize;
        int itemCount = Gen2Constants.itemCount;
        List<Integer> prices = new ArrayList<>(itemCount);
        prices.add(0);
        for (int i = 1; i < itemCount; i++) {
            int offset = itemAttributesOffset + (i - 1) * entrySize;
            prices.add(readWord(offset));
        }
        return prices;
    }

    @Override
    public void setBalancedShopPrices() {
        List<Integer> prices = getShopPrices();
        for (Map.Entry<Integer, Integer> entry : Gen2Constants.balancedItemPrices.entrySet()) {
            prices.set(entry.getKey(), entry.getValue());
        }
        setShopPrices(prices);
    }

    public void setShopPrices(List<Integer> prices) {
        int itemDataOffset = romEntry.getIntValue("ItemAttributesOffset");
        int entrySize = Gen2Constants.itemAttributesEntrySize;
        int itemCount = Gen2Constants.itemCount;
        if (prices.size() != itemCount) {
            throw new IllegalArgumentException("");
        }
        for (int i = 1; i < itemCount; i++) {
            int balancedPrice = prices.get(i);
            int offset = itemDataOffset + (i - 1) * entrySize;
            writeWord(offset, balancedPrice);
        }
    }

    @Override
    public boolean canChangeTrainerText() {
        return romEntry.getIntValue("CanChangeTrainerText") > 0;
    }

    @Override
    public TrainerNameMode trainerNameMode() {
        return TrainerNameMode.MAX_LENGTH_WITH_CLASS;
    }

    @Override
    public int maxTrainerNameLength() {
        // line size minus one for space
        return Gen2Constants.maxTrainerNameLength;
    }

    @Override
    public int maxSumOfTrainerNameLengths() {
        return romEntry.getIntValue("MaxSumOfTrainerNameLengths");
    }

    @Override
    public List<Integer> getTCNameLengthsByTrainer() {
        int traineramount = romEntry.getIntValue("TrainerClassAmount");
        int[] trainerclasslimits = romEntry.getArrayValue("TrainerDataClassCounts");
        List<String> tcNames = this.getTrainerClassNames();
        List<Integer> tcLengthsByT = new ArrayList<>();

        for (int i = 0; i < traineramount; i++) {
            int len = internalStringLength(tcNames.get(i));
            for (int k = 0; k < trainerclasslimits[i]; k++) {
                tcLengthsByT.add(len);
            }
        }

        return tcLengthsByT;
    }

    @Override
    public List<String> getTrainerClassNames() {
        int amount = romEntry.getIntValue("TrainerClassAmount");
        int offset = romEntry.getIntValue("TrainerClassNamesOffset");
        List<String> trainerClassNames = new ArrayList<>();
        for (int j = 0; j < amount; j++) {
            String name = readVariableLengthString(offset, false);
            offset += lengthOfStringAt(offset, false);
            trainerClassNames.add(name);
        }
        return trainerClassNames;
    }

    @Override
    public Set<Item> getEvolutionItems() {
        return itemIdsToSet(Gen2Constants.evolutionItems);
    }

    @Override
    public Set<Item> getXItems() {
        return itemIdsToSet(Gen2Constants.xItems);
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {
        if (romEntry.getIntValue("CanChangeTrainerText") != 0) {
            int amount = romEntry.getIntValue("TrainerClassAmount");
            int offset = romEntry.getIntValue("TrainerClassNamesOffset");
            Iterator<String> trainerClassNamesI = trainerClassNames.iterator();
            for (int j = 0; j < amount; j++) {
                int len = lengthOfStringAt(offset, false);
                String newName = trainerClassNamesI.next();
                writeFixedLengthString(newName, offset, len);
                offset += len;
            }
        }
    }

    @Override
    public boolean fixedTrainerClassNamesLength() {
        return true;
    }

    @Override
    public List<Integer> getDoublesTrainerClasses() {
        int[] doublesClasses = romEntry.getArrayValue("DoublesTrainerClasses");
        List<Integer> doubles = new ArrayList<>();
        for (int tClass : doublesClasses) {
            doubles.add(tClass);
        }
        return doubles;
    }

    @Override
    public String getDefaultExtension() {
        return "gbc";
    }

    @Override
    public int abilitiesPerSpecies() {
        return 0;
    }

    @Override
    public int highestAbilityIndex() {
        return 0;
    }

    @Override
    public Map<Integer, List<Integer>> getAbilityVariations() {
        return new HashMap<>();
    }

    @Override
    public boolean hasMegaEvolutions() {
        return false;
    }

    @Override
    public int internalStringLength(String string) {
        return translateString(string).length;
    }

    @Override
    public int miscTweaksAvailable() {
        int available = MiscTweak.LOWER_CASE_POKEMON_NAMES.getValue();
        if (romEntry.hasTweakFile("BWXPTweak")) {
            available |= MiscTweak.BW_EXP_PATCH.getValue();
        }
        if (romEntry.getIntValue("TextDelayFunctionOffset") != 0) {
            available |= MiscTweak.FASTEST_TEXT.getValue();
        }
        if (romEntry.getArrayValue("CatchingTutorialOffsets").length != 0) {
            available |= MiscTweak.RANDOMIZE_CATCHING_TUTORIAL.getValue();
        }
        available |= MiscTweak.BAN_LUCKY_EGG.getValue();
        if (romEntry.getIntValue("TMMovesReusableFunctionOffset") != 0
                && romEntry.getIntValue("TMMovesReusableFunctionJumpLength") != 0) {
            available |= MiscTweak.REUSABLE_TMS.getValue();
        }
        return available;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        if (tweak == MiscTweak.BW_EXP_PATCH) {
            applyBWEXPPatch();
        } else if (tweak == MiscTweak.FASTEST_TEXT) {
            applyFastestTextPatch();
        } else if (tweak == MiscTweak.LOWER_CASE_POKEMON_NAMES) {
            applyCamelCaseNames();
        } else if (tweak == MiscTweak.BAN_LUCKY_EGG) {
            items.get(Gen2ItemIDs.luckyEgg).setAllowed(false);
        } else if (tweak == MiscTweak.REUSABLE_TMS) {
            applyReusableTMsPatch();
        }
    }

    private void applyBWEXPPatch() {
        if (!romEntry.hasTweakFile("BWXPTweak")) {
            return;
        }
        String patchName = romEntry.getTweakFile("BWXPTweak");
        try {
            FileFunctions.applyPatch(rom, patchName);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void applyFastestTextPatch() {
        if (romEntry.getIntValue("TextDelayFunctionOffset") != 0) {
            writeByte(romEntry.getIntValue("TextDelayFunctionOffset"), GBConstants.gbZ80Ret);
        }
    }

    private void applyReusableTMsPatch() {
        // Overwrites the call to the code that consumes the TM, with a jump to the next part.
        // Since the TM is not consumed, it becomes infinitely reusable.
        int offset = romEntry.getIntValue("TMMovesReusableFunctionOffset");
        int jumpLength = romEntry.getIntValue("TMMovesReusableFunctionJumpLength");
        if (offset == 0 || jumpLength == 0) {
            return;
        }
        if (rom[offset] != GBConstants.gbZ80Call
                || rom[offset + GBConstants.gbZ80CallSize] != GBConstants.gbZ80JumpRelative) {
            throw new RuntimeException("Unexpected bytes found for the ROM's TM teaching function, " +
                    "likely ROM entry value \"TMMovesReusableFunctionOffset\" is faulty.");
        }
        writeByte(offset++, GBConstants.gbZ80Jump);
        writeByte(offset, (byte) jumpLength);
        tmsReusable = true;
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        if (romEntry.getArrayValue("CatchingTutorialOffsets").length != 0) {
            // Unown is banned
            if (opponent.getNumber() == SpeciesIDs.unown) {
                return false;
            }

            int[] offsets = romEntry.getArrayValue("CatchingTutorialOffsets");
            for (int offset : offsets) {
                writeByte(offset, (byte) opponent.getNumber());
            }
        }
        return true;
    }

    @Override
    public TypeTable getTypeTable() {
        return readTypeTable();
    }

    private TypeTable readTypeTable() {
        TypeTable typeTable = new TypeTable(Type.getAllTypes(2));
        int currentOffset = romEntry.getIntValue("TypeEffectivenessOffset");
        int attackingType = rom[currentOffset];
        while (attackingType != GBConstants.typeTableTerminator) {
            if (rom[currentOffset] == GBConstants.typeTableForesightTerminator) {
                currentOffset++;
            } else {
                int defendingType = rom[currentOffset + 1];
                int effectivenessInternal = rom[currentOffset + 2];
                Type attacking = Gen2Constants.typeTable[attackingType];
                Type defending = Gen2Constants.typeTable[defendingType];
                Effectiveness effectiveness;
                switch (effectivenessInternal) {
                    case 20:
                        effectiveness = Effectiveness.DOUBLE;
                        break;
                    case 10:
                        effectiveness = Effectiveness.NEUTRAL;
                        break;
                    case 5:
                        effectiveness = Effectiveness.HALF;
                        break;
                    case 0:
                        effectiveness = Effectiveness.ZERO;
                        break;
                    default:
                        effectiveness = null;
                }
                if (effectiveness != null) {
                    typeTable.setEffectiveness(attacking, defending, effectiveness);
                }
                currentOffset += 3;
            }
            attackingType = rom[currentOffset];
        }
        return typeTable;
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        writeTypeTable(typeTable);
    }

    private void writeTypeTable(TypeTable typeTable) {
        if (typeTable.nonNeutralEffectivenessCount() > Gen2Constants.nonNeutralEffectivenessCount) {
            throw new IllegalArgumentException("Too many non-neutral Effectiveness-es. Was "
                    + typeTable.nonNeutralEffectivenessCount() + ", has to be at most " +
                    Gen2Constants.nonNeutralEffectivenessCount);
        }

        int tableOffset = romEntry.getIntValue("TypeEffectivenessOffset");

        ByteArrayOutputStream mainPart = new ByteArrayOutputStream();
        ByteArrayOutputStream ghostImmunities = new ByteArrayOutputStream();

        prepareTypeTableParts(typeTable, mainPart, ghostImmunities);
        writeTypeTableParts(tableOffset, mainPart, ghostImmunities);
    }

    private void prepareTypeTableParts(TypeTable typeTable, ByteArrayOutputStream mainPart, ByteArrayOutputStream ghostImmunities) {
        for (Type attacker : typeTable.getTypes()) {
            for (Type defender : typeTable.getTypes()) {
                Effectiveness eff = typeTable.getEffectiveness(attacker, defender);
                if (eff != Effectiveness.NEUTRAL) {
                    ByteArrayOutputStream part = (defender == Type.GHOST && eff == Effectiveness.ZERO)
                            ? ghostImmunities : mainPart;
                    byte effectivenessInternal;
                    switch (eff) {
                        case DOUBLE : effectivenessInternal= 20;
                            break;
                        case HALF:
                            effectivenessInternal = 5;
                            break;
                        default:
                            effectivenessInternal = 0;
                            break;
                    }
                    part.write(Gen2Constants.typeToByte(attacker));
                    part.write(Gen2Constants.typeToByte(defender));
                    part.write(effectivenessInternal);
                }
            }
        }
    }

    private void writeTypeTableParts(int tableOffset, ByteArrayOutputStream mainPart, ByteArrayOutputStream ghostImmunities) {
        writeBytes(tableOffset, mainPart.toByteArray());
        tableOffset += mainPart.size();
        rom[tableOffset++] = GBConstants.typeTableForesightTerminator;
        writeBytes(tableOffset, ghostImmunities.toByteArray());
        tableOffset += ghostImmunities.size();
        rom[tableOffset] = GBConstants.typeTableTerminator;
    }

    @Override
    public void enableGuaranteedPokemonCatching() {
        String prefix = romEntry.getStringValue("GuaranteedCatchPrefix");
        int offset = find(rom, prefix);
        if (offset > 0) {
            offset += prefix.length() / 2; // because it was a prefix

            // The game guarantees that the catching tutorial always succeeds in catching by running
            // the following code:
            // ld a, [wBattleType]
            // cp BATTLETYPE_TUTORIAL
            // jp z, .catch_without_fail
            // By making the jump here unconditional, we can ensure that catching always succeeds no
            // matter the battle type. We check that the original condition is present just for safety.
            if (rom[offset] == (byte) 0xCA) {
                writeByte(offset, (byte) 0xC3);
            }
        }
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        if (pk.getNumber() == SpeciesIDs.unown) {
            return false;
        }
        writeByte(romEntry.getIntValue("IntroSpriteOffset"), (byte) pk.getNumber());
        writeByte(romEntry.getIntValue("IntroCryOffset"), (byte) pk.getNumber());
        return true;
    }

    @Override
    public Set<Item> getNonBadItems() {
        Set<Item> nonBad = new HashSet<>(getAllowedItems());
        nonBad.removeIf(item -> Gen2Constants.badItems.contains(item.getId()));
        // VietCrystal: exclude Burn Heal, Calcium, TwistedSpoon, and Elixir
        // crashes your game if used, glitches out your inventory if carried
        if (isVietCrystal) {
            Set<Item> vietBad = Stream.of(Gen2ItemIDs.burnHeal, Gen2ItemIDs.calcium, Gen2ItemIDs.elixer,
                    Gen2ItemIDs.twistedSpoon).map(items::get).collect(Collectors.toSet());
            nonBad.removeAll(vietBad);
        }
        return nonBad;
    }

    @Override
    public Set<Item> getUniqueNoSellItems() {
        return new HashSet<>();
    }

    @Override
    public Set<Item> getRegularShopItems() {
        return itemIdsToSet(Gen2Constants.regularShopItems);
    }

    @Override
    public Set<Item> getOPShopItems() {
        return itemIdsToSet(Gen2Constants.opShopItems);
    }

    @Override
    public void loadItems() {
        String[] names = readItemNames();
        items = new ArrayList<>(names.length);
        items.add(null);
        for (int i = 1; i < names.length; i++) {
            items.add(new Item(i, names[i]));
        }

        Gen2Constants.bannedItems.forEach(id -> items.get(id).setAllowed(false));
        for (int i = Gen2Constants.tmsStartIndex; i < Gen2Constants.tmsStartIndex + Gen2Constants.tmCount; i++) {
            items.get(i).setTM(true);
        }
    }

    private String[] readItemNames() {
        String[] itemNames = new String[256];
        // trying to emulate pretty much what the game does here
        // normal items
        int origOffset = romEntry.getIntValue("ItemNamesOffset");
        int itemNameOffset = origOffset;
        for (int index = 1; index <= 0x100; index++) {
            if (itemNameOffset / GBConstants.bankSize > origOffset / GBConstants.bankSize) {
                // the game would continue making its merry way into VRAM here,
                // but we don't have VRAM to simulate.
                // just give up.
                break;
            }
            int startOfText = itemNameOffset;
            while ((rom[itemNameOffset] & 0xFF) != GBConstants.stringTerminator) {
                itemNameOffset++;
            }
            itemNameOffset++;
            itemNames[index % 256] = readFixedLengthString(startOfText, 20);
        }
        return itemNames;
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    private void patchFleeing() {
        havePatchedFleeing = true;
        int offset = romEntry.getIntValue("FleeingDataOffset");
        writeByte(offset, (byte) 0xFF);
        writeByte(offset + Gen2Constants.fleeingSetTwoOffset, (byte) 0xFF);
        writeByte(offset + Gen2Constants.fleeingSetThreeOffset, (byte) 0xFF);
    }

    private void loadLandmarkNames() {

        int lmOffset = romEntry.getIntValue("LandmarkTableOffset");
        int lmCount = romEntry.getIntValue("LandmarkCount");

        landmarkNames = new String[lmCount];

        for (int i = 0; i < lmCount; i++) {
            int lmNameOffset = readPointer(lmOffset + i * 4 + 2);
            landmarkNames[i] = readVariableLengthString(lmNameOffset, false).replace("\\x1F", " ");
        }

    }

    private void preprocessMaps() {
        itemOffs = new ArrayList<>();

        int mhOffset = romEntry.getIntValue("MapHeaders");
        int mapGroupCount = Gen2Constants.mapGroupCount;
        int mapsInLastGroup = Gen2Constants.mapsInLastGroup;
        int mhBank = bankOf(mhOffset);
        mapNames = new String[mapGroupCount + 1][100];

        int[] groupOffsets = new int[mapGroupCount];
        for (int i = 0; i < mapGroupCount; i++) {
            groupOffsets[i] = readPointer(mhOffset + i * 2);
        }

        // Read maps
        for (int mg = 0; mg < mapGroupCount; mg++) {
            int offset = groupOffsets[mg];
            int maxOffset = (mg == mapGroupCount - 1) ? (mhBank + 1) * GBConstants.bankSize : groupOffsets[mg + 1];
            int map = 0;
            int maxMap = (mg == mapGroupCount - 1) ? mapsInLastGroup : Integer.MAX_VALUE;
            while (offset < maxOffset && map < maxMap) {
                processMapAt(offset, mg + 1, map + 1);
                offset += 9;
                map++;
            }
        }
    }

    private void processMapAt(int offset, int mapBank, int mapNumber) {

        // second map header
        int smhBank = rom[offset] & 0xFF;
        int smhOffset = readPointer(offset + 3, smhBank);

        // map name
        int mapLandmark = rom[offset + 5] & 0xFF;
        mapNames[mapBank][mapNumber] = landmarkNames[mapLandmark];

        // event header
        // event header is in same bank as script header
        int ehBank = rom[smhOffset + 6] & 0xFF;
        int ehOffset = readPointer(smhOffset + 9, ehBank);

        // skip over filler
        ehOffset += 2;

        // warps
        int warpCount = rom[ehOffset++] & 0xFF;
        // warps are skipped
        ehOffset += warpCount * 5;

        // xy triggers
        int triggerCount = rom[ehOffset++] & 0xFF;
        // xy triggers are skipped
        ehOffset += triggerCount * 8;

        // signposts
        int signpostCount = rom[ehOffset++] & 0xFF;
        // we do care about these
        for (int sp = 0; sp < signpostCount; sp++) {
            // type=7 are hidden items
            int spType = rom[ehOffset + sp * 5 + 2] & 0xFF;
            if (spType == 7) {
                // get event pointer
                int spOffset = readPointer(ehOffset + sp * 5 + 3, ehBank);
                // item is at spOffset+2 (first two bytes are the flag id)
                itemOffs.add(spOffset + 2);
            }
        }
        // now skip past them
        ehOffset += signpostCount * 5;

        // visible objects/people
        int peopleCount = rom[ehOffset++] & 0xFF;
        // we also care about these
        for (int p = 0; p < peopleCount; p++) {
            // color_function & 1 = 1 if itemball
            int pColorFunction = rom[ehOffset + p * 13 + 7];
            if ((pColorFunction & 1) == 1) {
                // get event pointer
                int pOffset = readPointer(ehOffset + p * 13 + 9, ehBank);
                // item is at the pOffset for non-hidden items
                itemOffs.add(pOffset);
            }
        }

    }

    @Override
    public List<Integer> getRequiredFieldTMs() {
        return Gen2Constants.requiredFieldTMs;
    }

    @Override
    public List<Integer> getCurrentFieldTMs() {
        List<Integer> fieldTMs = new ArrayList<>();

        for (int offset : itemOffs) {
            int itemId = rom[offset] & 0xFF;
            if (items.get(itemId).isTM()) {
                int thisTM;
                if (itemId >= Gen2Constants.tmBlockOneIndex
                        && itemId < Gen2Constants.tmBlockOneIndex + Gen2Constants.tmBlockOneSize) {
                    thisTM = itemId - Gen2Constants.tmBlockOneIndex + 1;
                } else if (itemId >= Gen2Constants.tmBlockTwoIndex
                        && itemId < Gen2Constants.tmBlockTwoIndex + Gen2Constants.tmBlockTwoSize) {
                    thisTM = itemId - Gen2Constants.tmBlockTwoIndex + 1 + Gen2Constants.tmBlockOneSize; // TM
                    // block
                    // 2
                    // offset
                } else {
                    thisTM = itemId - Gen2Constants.tmBlockThreeIndex + 1 + Gen2Constants.tmBlockOneSize
                            + Gen2Constants.tmBlockTwoSize; // TM block 3 offset
                }
                // hack for the bug catching contest repeat TM28
                if (!fieldTMs.contains(thisTM)) {
                    fieldTMs.add(thisTM);
                }
            }
        }
        return fieldTMs;
    }

    @Override
    public void setFieldTMs(List<Integer> fieldTMs) {
        Iterator<Integer> iterTMs = fieldTMs.iterator();
        int[] givenTMs = new int[256];

        for (int offset : itemOffs) {
            int itemId = rom[offset] & 0xFF;
            if (items.get(itemId).isTM()) {
                // Cache replaced TMs to duplicate bug catching contest TM
                if (givenTMs[itemId] != 0) {
                    writeByte(offset, (byte) givenTMs[itemId]);
                } else {
                    // Replace this with a TM from the list
                    int tm = iterTMs.next();
                    if (tm >= 1 && tm <= Gen2Constants.tmBlockOneSize) {
                        tm += Gen2Constants.tmBlockOneIndex - 1;
                    } else if (tm >= Gen2Constants.tmBlockOneSize + 1
                            && tm <= Gen2Constants.tmBlockOneSize + Gen2Constants.tmBlockTwoSize) {
                        tm += Gen2Constants.tmBlockTwoIndex - 1 - Gen2Constants.tmBlockOneSize;
                    } else {
                        tm += Gen2Constants.tmBlockThreeIndex - 1 - Gen2Constants.tmBlockOneSize
                                - Gen2Constants.tmBlockTwoSize;
                    }
                    givenTMs[itemId] = tm;
                    writeByte(offset, (byte) tm);
                }
            }
        }
    }

    @Override
    public List<Item> getRegularFieldItems() {
        List<Item> fieldItems = new ArrayList<>();

        for (int offset : itemOffs) {
            Item item = items.get(rom[offset] & 0xFF);
            if (item.isAllowed() && !item.isTM()) {
                fieldItems.add(item);
            }
        }
        return fieldItems;
    }

    @Override
    public void setRegularFieldItems(List<Item> items) {
        Iterator<Item> iterItems = items.iterator();

        for (int offset : itemOffs) {
            Item current = items.get(rom[offset] & 0xFF);
            if (current.isAllowed() && !current.isTM()) {
                // Replace it
                writeByte(offset, (byte) iterItems.next().getId());
            }
        }

    }

    @Override
    public List<IngameTrade> getIngameTrades() {
        List<IngameTrade> trades = new ArrayList<>();

        // info
        int tableOffset = romEntry.getIntValue("TradeTableOffset");
        int tableSize = romEntry.getIntValue("TradeTableSize");
        int nicknameLength = romEntry.getIntValue("TradeNameLength");
        int otLength = romEntry.getIntValue("TradeOTLength");
        int[] unused = romEntry.getArrayValue("TradesUnused");
        int unusedOffset = 0;
        int entryLength = nicknameLength + otLength + 9;
        if (entryLength % 2 != 0) {
            entryLength++;
        }

        for (int entry = 0; entry < tableSize; entry++) {
            if (unusedOffset < unused.length && unused[unusedOffset] == entry) {
                unusedOffset++;
                continue;
            }
            IngameTrade trade = new IngameTrade();
            int entryOffset = tableOffset + entry * entryLength;
            trade.requestedSpecies = pokes[rom[entryOffset + 1] & 0xFF];
            trade.givenSpecies = pokes[rom[entryOffset + 2] & 0xFF];
            trade.nickname = readString(entryOffset + 3, nicknameLength, false);
            int atkdef = rom[entryOffset + 3 + nicknameLength] & 0xFF;
            int spdspc = rom[entryOffset + 4 + nicknameLength] & 0xFF;
            trade.ivs = new int[]{(atkdef >> 4) & 0xF, atkdef & 0xF, (spdspc >> 4) & 0xF, spdspc & 0xF};
            trade.item = rom[entryOffset + 5 + nicknameLength] & 0xFF;
            trade.otId = readWord(entryOffset + 6 + nicknameLength);
            trade.otName = readString(entryOffset + 8 + nicknameLength, otLength, false);
            trades.add(trade);
        }

        return trades;

    }

    @Override
    public void setIngameTrades(List<IngameTrade> trades) {
        // info
        int tableOffset = romEntry.getIntValue("TradeTableOffset");
        int tableSize = romEntry.getIntValue("TradeTableSize");
        int entryLength = getIngameTradeEntryLength();
        int[] unused = romEntry.getArrayValue("TradesUnused");
        int unusedIndex = 0;
        int tradeIndex = 0;

        for (int entry = 0; entry < tableSize; entry++) {
            if (unusedIndex < unused.length && unused[unusedIndex] == entry) {
                unusedIndex++;
            } else {
                IngameTrade trade = trades.get(tradeIndex);
                int entryOffset = tableOffset + entry * entryLength;
                writeBytes(entryOffset, ingameTradeToBytes(trade));
                tradeIndex++;
            }
        }
    }

    private byte[] ingameTradeToBytes(IngameTrade trade) {
        int nicknameLength = romEntry.getIntValue("TradeNameLength");
        int otLength = romEntry.getIntValue("TradeOTLength");

        byte[] data = new byte[getIngameTradeEntryLength()];
        data[0] = (byte) trade.requestedSpecies.getNumber();
        data[1] = (byte) trade.givenSpecies.getNumber();
        if (romEntry.getIntValue("CanChangeTrainerText") > 0) {
            writeFixedLengthString(data, trade.nickname, 2, nicknameLength);
        }
        data[2 + nicknameLength] = (byte) (trade.ivs[0] << 4 | trade.ivs[1]);
        data[3 + nicknameLength] = (byte) (trade.ivs[2] << 4 | trade.ivs[3]);
        data[4 + nicknameLength] = (byte) trade.item;
        writeWord(data, 5 + nicknameLength, trade.otId);
        if (romEntry.getIntValue("CanChangeTrainerText") > 0) {
            writeFixedLengthString(data, trade.otName, 7 + nicknameLength, otLength);
        }
        // remove gender req
        data[7 + nicknameLength + otLength] = 0;
        return data;
    }

    private int getIngameTradeEntryLength() {
        int entryLength = romEntry.getIntValue("TradeNameLength") + romEntry.getIntValue("TradeOTLength") + 9;
        if (entryLength % 2 != 0) {
            entryLength++;
        }
        return entryLength;
    }

    @Override
    public boolean hasDVs() {
        return true;
    }

    @Override
    public int generationOfPokemon() {
        return 2;
    }

    @Override
    public void removeEvosForPokemonPool() {
        SpeciesSet pokemonIncluded = rPokeService.getAll(false);
        Set<Evolution> keepEvos = new HashSet<>();
        for (Species pk : pokes) {
            if (pk != null) {
                keepEvos.clear();
                for (Evolution evol : pk.getEvolutionsFrom()) {
                    if (pokemonIncluded.contains(evol.getFrom()) && pokemonIncluded.contains(evol.getTo())) {
                        keepEvos.add(evol);
                    } else {
                        evol.getTo().getEvolutionsTo().remove(evol);
                    }
                }
                pk.getEvolutionsFrom().retainAll(keepEvos);
            }
        }
    }

    private void saveEvosAndMovesLearnt() {
        int pointerTableOffset = romEntry.getIntValue("PokemonMovesetsTableOffset");

        for (Species pk : speciesList) {
            if (pk == null) continue;
            int pokeNum = pk.getNumber();
            int pointerOffset = pointerTableOffset + (pokeNum - 1) * 2;
            new SameBankDataRewriter<Species>().rewriteData(pointerOffset, pk, this::pokemonToEvosAndMovesLearntBytes,
                    oldDataOffset -> lengthOfDataWithTerminatorsAt(oldDataOffset,
                            GBConstants.evosAndMovesTerminator, 2));
        }
    }

    private byte[] pokemonToEvosAndMovesLearntBytes(Species pk) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Evolution evo : pk.getEvolutionsFrom()) {
            byte[] evoBytes = evolutionToBytes(evo);
            baos.write(evoBytes, 0, evoBytes.length);
        }
        baos.write(GBConstants.evosAndMovesTerminator);
        for (MoveLearnt ml : movesets.get(pk.getNumber())) {
            byte[] mlBytes = moveLearntToBytes(ml);
            baos.write(mlBytes, 0, mlBytes.length);
        }
        baos.write(GBConstants.evosAndMovesTerminator);
        return baos.toByteArray();
    }

    private byte[] evolutionToBytes(Evolution evo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(Gen2Constants.evolutionTypeToIndex(evo.getType()));
        byte[] extraInfoBytes = evoTypeExtraInfoToBytes(evo);
        baos.write(extraInfoBytes, 0, extraInfoBytes.length);
        baos.write(evo.getTo().getNumber());
        return baos.toByteArray();
    }

    private byte[] evoTypeExtraInfoToBytes(Evolution evo) {
         switch (evo.getType()) {
             case LEVEL: case STONE: case TRADE_ITEM:
                 return new byte[]{(byte) evo.getExtraInfo()};
             case TRADE:
                 return new byte[]{(byte) 0xFF};
             case HAPPINESS:
                 return new byte[]{(byte) 0x01};
             case HAPPINESS_DAY:
                 return new byte[]{(byte) 0x02};
             case HAPPINESS_NIGHT:
                 return new byte[]{(byte) 0x03};
             case LEVEL_ATTACK_HIGHER:
                 return new byte[]{(byte) evo.getExtraInfo(), (byte) 0x01};
             case LEVEL_DEFENSE_HIGHER:
                 return new byte[]{(byte) evo.getExtraInfo(), (byte) 0x02};
             case LEVEL_ATK_DEF_SAME:
                 return new byte[]{(byte) evo.getExtraInfo(), (byte) 0x03};
             default:
                 throw new IllegalStateException("EvolutionType " + evo.getType() + " is not supported " +
                         "by Gen 2 games.");
         }
    }

    private byte[] moveLearntToBytes(MoveLearnt ml) {
        return new byte[] {(byte) ml.level, (byte) ml.move};
    }

    @Override
    public boolean supportsFourStartingMoves() {
        return (romEntry.getIntValue("SupportsFourStartingMoves") > 0);
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        // add OHKO moves for gen2 because x acc is still broken
        return Gen2Constants.brokenMoves;
    }

    @Override
    public List<Integer> getIllegalMoves() {
        // 3 moves that crash the game when used by self or opponent
        if (isVietCrystal) {
            return Gen2Constants.illegalVietCrystalMoves;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Integer> getFieldMoves() {
        // cut, fly, surf, strength, flash,
        // dig, teleport, whirlpool, waterfall,
        // rock smash, headbutt, sweet scent
        // not softboiled or milk drink
        return Gen2Constants.fieldMoves;
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        // just cut
        return Gen2Constants.earlyRequiredHMMoves;
    }

    @Override
    public void loadPokemonPalettes() {
        int palOffset = romEntry.getIntValue("PokemonPalettes") + 8;
        for (Species pk : getSpeciesSet()) {
            int num = pk.getNumber() - 1;

            int normalPaletteOffset = palOffset + num * 8;
            pk.setNormalPalette(read2ColorPalette(normalPaletteOffset));

            int shinyPaletteOffset = palOffset + num * 8 + 4;
            pk.setShinyPalette(read2ColorPalette(shinyPaletteOffset));
        }
    }

    private Palette readTrainerPalette(int trainerClass) {
        if (trainerClass < 0) {
            throw new IllegalArgumentException("Invalid trainerClass; can't be negative");
        }
        int lastTrainerClass = romEntry.getIntValue("TrainerClassAmount") - 1;
        if (trainerClass > lastTrainerClass) {
            throw new IllegalArgumentException("Invalid trainerClass; can't exceed " + lastTrainerClass);
        }
        int offset = romEntry.getIntValue("TrainerPalettes") + trainerClass * 4;
        return read2ColorPalette(offset);
    }

    private Palette read2ColorPalette(int offset) {
        byte[] paletteBytes = new byte[]{rom[offset], rom[offset + 1], rom[offset + 2], rom[offset + 3]};
        return new Palette(paletteBytes);
    }

    @Override
    public void savePokemonPalettes() {
        int palOffset = romEntry.getIntValue("PokemonPalettes") + 8;
        for (Species pk : getSpeciesSet()) {
            int num = pk.getNumber() - 1;

            int normalPaletteOffset = palOffset + num * 8;
            writePalette(normalPaletteOffset, pk.getNormalPalette());

            int shinyPaletteOffset = palOffset + num * 8 + 4;
            writePalette(shinyPaletteOffset, pk.getShinyPalette());
        }
    }

    private void writeTrainerPalette(int trainerClass, Palette palette) {
        if (palette.size() != 2) {
            throw new IllegalArgumentException("Invalid Palette, must have exactly 2 colors.");
        }
        int[] pointerOffsets = romEntry.getArrayValue("TrainerPalettesPointers");
        int tableOffset = pointerOffsets.length == 0 ?
                romEntry.getIntValue("TrainerPalettes") : readPointer(pointerOffsets[0]);
        int offset = tableOffset + trainerClass * 4;
        writePalette(offset, palette);
    }

    private void writePalette(int offset, Palette palette) {
        writeBytes(offset, palette.toBytes());
    }

    private int getPokemonImagePointerOffset(Species pk, boolean back) {
        // Each Pokemon has a front and back pic with a bank and a pointer (3*2=6)
        // There is no zero-entry.
        int pointerOffset;
        if (pk.getNumber() == SpeciesIDs.unown) {
            int unownLetter = new Random().nextInt(Gen2Constants.unownFormeCount);
            pointerOffset = romEntry.getIntValue("UnownImages") + unownLetter * 6;
        } else {
            pointerOffset = romEntry.getIntValue("PokemonImages") + (pk.getNumber() - 1) * 6;
        }
        if (back) {
            pointerOffset += 3;
        }
        return pointerOffset;
    }

    private void rewriteTrainerImage(int trainerClass, GBCImage image) {
        if (trainerClass < 1) {
            throw new IllegalArgumentException("Invalid trainerClass; can't be less than 1");
        }
        int lastTrainerClass = romEntry.getIntValue("TrainerClassAmount") - 1;
        if (trainerClass > lastTrainerClass) {
            throw new IllegalArgumentException("Invalid trainerClass; can't exceed " + lastTrainerClass);
        }
        int pointerOffset = romEntry.getIntValue("TrainerImages") + (trainerClass - 1) * 3;
        rewritePokemonOrTrainerImage(pointerOffset, image);
    }

    private void rewritePokemonOrTrainerImage(int pointerOffset, GBCImage image) {
        byte[] uncompressed = image.toBytes();

        GBCDataRewriter<byte[]> dataRewriter = new GBCDataRewriter<>();
        dataRewriter.setPointerReader(this::readPokemonOrTrainerImagePointer);
        dataRewriter.setPointerWriter(this::writePokemonOrTrainerImagePointer);
        dataRewriter.rewriteData(pointerOffset, uncompressed, Gen2Cmp::compress, this::lengthOfCompressedDataAt);
    }

    private int lengthOfCompressedDataAt(int offset) {
        if (offset == 0) {
            throw new IllegalArgumentException("Invalid offset. Compressed data cannot be at offset 0.");
        }
        return Gen2Decmp.lengthOfCompressed(rom, offset);
    }

    private int readPokemonOrTrainerImagePointer(int pointerOffset) {
        int bank = (rom[pointerOffset] & 0xFF);
        if (romEntry.isCrystal()) {
            // Crystal pic banks are offset by x36 for whatever reason.
            bank += 0x36;
        } else {
            // Hey, G/S are dumb too! Arbitrarily redirected bank numbers.
            if (bank == 0x13) {
                bank = 0x1F;
            } else if (bank == 0x14) {
                bank = 0x20;
            } else if (bank == 0x1F) {
                bank = 0x2E;
            }
        }
        return readPointer(pointerOffset + 1, bank);
    }

    private void writePokemonOrTrainerImagePointer(int offset, int pointer) {
        int bank = bankOf(pointer);
        if (romEntry.isCrystal()) {
            // Crystal pic banks are offset by x36 for whatever reason.
            bank -= 0x36;
        } else {
            // Hey, G/S are dumb too! Arbitrarily redirected bank numbers.
            if (bank == 0x1F) {
                bank = 0x13;
            } else if (bank == 0x20) {
                bank = 0x14;
            } else if (bank == 0x2E) {
                bank = 0x1F;
            }
        }
        writeByte(offset, (byte) bank);
        writePointer(offset + 1, pointer);
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        return true;
    }

    @Override
    public void setCustomPlayerGraphics(GraphicsPack unchecked, Settings.PlayerCharacterMod toReplace) {
        if (!(unchecked instanceof Gen2PlayerCharacterGraphics)) {
            throw new IllegalArgumentException("Invalid playerGraphics");
        }
        Gen2PlayerCharacterGraphics playerGraphics = (Gen2PlayerCharacterGraphics) unchecked;

        if (playerGraphics.hasFrontImage()) {
            rewritePlayerFrontImage(playerGraphics.getFrontImage(), toReplace);
            rewritePlayerTrainerCardImage(playerGraphics.getTrainerCardImage(), toReplace);
        }

        if (playerGraphics.hasBackImage()) {
            if (toReplace == Settings.PlayerCharacterMod.PC1) {
                rewriteChrisBackImage(playerGraphics.getBackImage());
            } else {
                rewriteKrisBackImage(playerGraphics.getBackImage());
            }
        }

        if (playerGraphics.hasImagePalette()) {
            rewritePlayerImagePalette(playerGraphics.getImagePalette(), toReplace);
        }

        if (playerGraphics.hasWalkSprite()) {
            int walkOffset = romEntry.getIntValue(Gen2Constants.getName(toReplace) + "WalkSprite");
            writeImage(walkOffset, playerGraphics.getWalkSprite());
        }
        if (playerGraphics.hasBikeSprite()) {
            int bikeOffset = romEntry.getIntValue(Gen2Constants.getName(toReplace) + "BikeSprite");
            writeImage(bikeOffset, playerGraphics.getBikeSprite());
        }
        if (playerGraphics.hasFishSprite()) {
            int fishOffset = romEntry.getIntValue(Gen2Constants.getName(toReplace) + "FishSprite");
            writeImage(fishOffset, playerGraphics.getFishSprite());
        }

        if (playerGraphics.hasSpritePaletteID()) {
            rewritePlayerSpritePalette(playerGraphics.getSpritePaletteID(), toReplace);
        }
    }

    private void rewritePlayerFrontImage(GBCImage frontImage, Settings.PlayerCharacterMod toReplace) {
        if (romEntry.isCrystal()) {
            int frontOffset = romEntry.getIntValue(Gen2Constants.getName(toReplace) + "FrontImage");
            writeImage(frontOffset, frontImage);
        } else {
            rewriteTrainerImage(Gen2Constants.chrisTrainerClassGS, frontImage);
        }
    }

    private void rewritePlayerTrainerCardImage(GBCImage trainerCardImage, Settings.PlayerCharacterMod toReplace) {
        int trainerCardOffset = romEntry.getIntValue(Gen2Constants.getName(toReplace) + "TrainerCardImage");
        // the trainer card image has different column modes in GS / Crystal, for whatever reason
        writeImage(trainerCardOffset, new GBCImage.Builder(trainerCardImage).columnMode(romEntry.isCrystal()).build());
    }

    private void rewritePlayerImagePalette(Palette palette, Settings.PlayerCharacterMod toReplace) {
        if (toReplace == Settings.PlayerCharacterMod.PC1) {
            rewriteChrisImagePalette(palette);
        } else if (toReplace == Settings.PlayerCharacterMod.PC2) {
            rewriteKrisImagePalette(palette);
        } else {
            throw new IllegalArgumentException("Unexpected value for toReplace: " + toReplace);
        }
    }

    private void rewriteChrisImagePalette(Palette palette) {
        if (romEntry.isCrystal()) {
            writeTrainerPalette(Gen2Constants.chrisTrainerClassCrystal, palette);
        } else {
            // GS Chris uses mostly the palette at index 0, like in Crystal.
            // This one is identical to Cal's palette, but in some places like the Oak Speech Cal's actual palette
            // is used instead. Thus both of them need to be overwritten.
            writeTrainerPalette(Gen2Constants.chrisTrainerClassGS, palette);
            writeTrainerPalette(Gen2Constants.chrisPaletteTrainerClassGS, palette);
        }
    }

    private void rewriteKrisImagePalette(Palette palette) {
        // Due to the games routines for drawing images, the players (and Oak) are assigned trainer classes.
        // Kris doesn't have a unique palette/class though, but shares one with Falkner.
        // This means if we want to give Kris a unique palette, we have to do extra operations.
        int classAmount = romEntry.getIntValue("TrainerClassAmount");
        resizeTrainerPalettes(classAmount + 1);

        writeTrainerPalette(classAmount, palette);
        writeChrisTrainerClass(classAmount);
        repointDirectPlayerPalettePointers(classAmount);
        fixTrainerCardPalettes(classAmount);
    }

    private void resizeTrainerPalettes(int newSize) {
        byte[] data = readPlayerPalettesBeforeMove();

        int[] pointerOffsets = romEntry.getArrayValue("TrainerPalettesPointers");
        int primaryPointerOffset = pointerOffsets[0];
        int[] secondaryPointerOffsets = Arrays.copyOfRange(pointerOffsets, 1, pointerOffsets.length);
        // uses a DataRewriter since it has checks to see if the pointers are valid,
        // even though it just moves/resizes the same chunk of data
        DataRewriter<byte[]> dataRewriter = new SpecificBankDataRewriter<>(romEntry.getIntValue("TrainerPalettesBank"));
        dataRewriter.rewriteData(primaryPointerOffset, data, secondaryPointerOffsets,
                data1 -> resizeTrainerPalettes(data1, newSize), odo -> data.length);
    }

    private byte[] readPlayerPalettesBeforeMove() {
        int classAmount = romEntry.getIntValue("TrainerClassAmount");
        int pointerOffset = romEntry.getArrayValue("TrainerPalettesPointers")[0];
        int offset = readPointer(pointerOffset);
        byte[] data = new byte[2 * 2 * classAmount];
        System.arraycopy(rom, offset, data, 0, data.length);
        return data;
    }

    private byte[] resizeTrainerPalettes(byte[] before, int newSize) {
        byte[] after = new byte[2 * 2 * newSize];
        System.arraycopy(before, 0, after, 0, Math.min(before.length, after.length));
        return after;
    }

    private void writeChrisTrainerClass(int krisTrainerClass) {
        int[] classOffsets = romEntry.getArrayValue("KrisTrainerClassOffsets");
        int origClassValue = rom[classOffsets[0]] & 0xFF;
        rom[classOffsets[0]] = (byte) (krisTrainerClass & 0xFF);
        for (int i = 1; i < classOffsets.length; i++) {
            int classValue = rom[classOffsets[i]];
            if (classValue != origClassValue) {
                throw new RuntimeException("Value mismatch; expected " + origClassValue + ", was " + classValue);
            }
            rom[classOffsets[i]] = (byte) (krisTrainerClass & 0xFF);
        }
    }

    private void repointDirectPlayerPalettePointers(int krisTrainerClass) {
        // One snippet of code also has direct pointers to the Chris and Kris/Falkner palettes.
        // Since we resized/moved the TrainerPalettes table we need to
        int trainerPalsOffset = readPointer(romEntry.getArrayValue("TrainerPalettesPointers")[0]);

        int chrisPointerOffset = romEntry.getIntValue("ChrisPalettePointer");
        writePointer(chrisPointerOffset, trainerPalsOffset);
        int krisPointerOffset = romEntry.getIntValue("KrisPalettePointer");
        writePointer(krisPointerOffset, trainerPalsOffset + 4 * krisTrainerClass);
    }

    private void fixTrainerCardPalettes(int krisTrainerClass) {
        int startOffset = romEntry.getIntValue("TrainerCardPalettesRoutine");
        if (startOffset != 0) {
            // palette #1: Falkner -> Kris
            // palette #5: Chuck -> Falkner
            rom[startOffset + Gen2Constants.tcPal1Offset] = (byte) (krisTrainerClass & 0xFF);
            rom[startOffset + Gen2Constants.tcPal5Offset] = (byte) (Gen2Constants.falknerTrainerClass & 0xFF);
            // Falkner icon: pal #1 -> #5
            // Chuck icon: pal #5 -> #7 (i.e. makes him share Pryce's pal)
            // Claire icon: pal #1 -> #5
            rom[startOffset + Gen2Constants.tcFalknerPalOffset] = (byte) 5;
            rom[startOffset + Gen2Constants.tcChuckPalOffset] = (byte) 7;
            rom[startOffset + Gen2Constants.tcClairePalOffset] = (byte) 5;
        } else {
            System.out.println("No value for TrainerCardPalettesRoutine in ROM entry.");
        }
    }

    public void rewriteKrisBackImage(GBCImage krisBack) {
        // not compressed
        int krisBackOffset = romEntry.getIntValue("KrisBackImage");
        writeImage(krisBackOffset, krisBack);
    }

    public void rewriteChrisBackImage(GBCImage chrisBack) {
        int[] pointerOffsets = romEntry.getArrayValue("ChrisBackImagePointers");
        int primaryPointerOffset = pointerOffsets[0];
        int[] secondaryPointerOffsets = Arrays.copyOfRange(pointerOffsets, 1, pointerOffsets.length);
        int[] bankOffsets = romEntry.getArrayValue("ChrisBackImageBankOffsets");
        DataRewriter<GBCImage> dataRewriter = new IndirectBankDataRewriter<>(bankOffsets);

        if (romEntry.isCrystal()) {
            dataRewriter.rewriteData(primaryPointerOffset, chrisBack, secondaryPointerOffsets,
                    image -> Gen2Cmp.compress(image.toBytes()), this::lengthOfCompressedDataAt);
        } else {
            // much more in GS since it has to make sure the catching tutorial dude's backpic ends up in the same bank
            dataRewriter.rewriteData(primaryPointerOffset, chrisBack, secondaryPointerOffsets,
                    this::chrisPlusDudeBackImagesToBytes, this::lengthOfChrisAndDudeBackImagesAt);
            repointDudeBackImage(primaryPointerOffset);
        }
    }

    private byte[] chrisPlusDudeBackImagesToBytes(GBCImage chrisBack) {
        byte[] chrisBackData = Gen2Cmp.compress(chrisBack.toBytes());
        byte[] dudeBackData = readDudeCompressedBackData();

        byte[] bothData = new byte[chrisBackData.length + dudeBackData.length];
        System.arraycopy(chrisBackData, 0, bothData, 0, chrisBackData.length);
        System.arraycopy(dudeBackData, 0, bothData, chrisBackData.length, dudeBackData.length);
        return bothData;
    }

    private byte[] readDudeCompressedBackData() {
        int[] bankOffsets = romEntry.getArrayValue("ChrisBackImageBankOffsets");
        int bank = rom[bankOffsets[0]];
        int dudeBackOffset = readPointer(romEntry.getIntValue("DudeBackImagePointer"), bank);
        int dudeBackLength = Gen2Decmp.lengthOfCompressed(rom, dudeBackOffset);
        return Arrays.copyOfRange(rom, dudeBackOffset, dudeBackOffset + dudeBackLength);
    }

    /**
     * The length in bytes of Chris' compressed back image, followed by the catching tutorial dude's.<br>
     * Assumes they actually follow another in ROM, with no gaps.
     */
    private int lengthOfChrisAndDudeBackImagesAt(int offset) {
        int length = lengthOfCompressedDataAt(offset);
        length += lengthOfCompressedDataAt(offset + length);
        return length;
    }

    private void repointDudeBackImage(int chrisBackPointerOffset) {
        int[] bankOffsets = romEntry.getArrayValue("ChrisBackImageBankOffsets");
        int bank = rom[bankOffsets[0]];

        int newOffset = readPointer(chrisBackPointerOffset, bank);
        int newDudeOffset = newOffset + lengthOfCompressedDataAt(newOffset);
        int dudeBackPointerOffset = romEntry.getIntValue("DudeBackImagePointer");
        writePointer(dudeBackPointerOffset, newDudeOffset);
    }

    private void rewritePlayerSpritePalette(Gen2SpritePaletteID spritePaletteID,
                                            Settings.PlayerCharacterMod toReplace) {
        int offset = romEntry.getIntValue(Gen2Constants.getName(toReplace) + "SpritePalette");
        byte value = (byte) ((spritePaletteID.ordinal() | 0b1000) << 4);
        System.out.println(spritePaletteID);
        System.out.println("SpritePalette Offset: 0x" + Integer.toHexString(offset));
        System.out.println("SpritePalette Value: 0x" + Integer.toHexString(Byte.toUnsignedInt(value)));
        writeByte(offset, value);
    }

    @Override
    public Gen2PokemonImageGetter createPokemonImageGetter(Species pk) {
        return new Gen2PokemonImageGetter(pk);
    }

    public class Gen2PokemonImageGetter extends GBPokemonImageGetter {

        public Gen2PokemonImageGetter(Species pk) {
            super(pk);
        }

        @Override
        public int getGraphicalFormeAmount() {
            return pk.getNumber() == SpeciesIDs.unown ? Gen2Constants.unownFormeCount : 1;
        }

        @Override
        public BufferedImage get() {

            int pointerOffset = getPokemonImagePointerOffset(pk, back);

            int width = back ? 6 : pk.getFrontImageDimensions() & 0x0F;
            int height = back ? 6 : (pk.getFrontImageDimensions() >> 4) & 0x0F;

            byte[] data;
            try {
                data = readPokemonOrTrainerImageData(pointerOffset, width, height);
            } catch (Exception e) {
                return null;
            }

            // White and black are always in the palettes at positions 0 and 3,
            // so only the middle colors are stored and need to be read.
            Palette palette = shiny ? pk.getShinyPalette() : pk.getNormalPalette();
            palette = new Palette(new Color[] {Color.WHITE, palette.get(0), palette.get(1), Color.BLACK});

            BufferedImage bim = new GBCImage.Builder(width, height, palette, data).columnMode(true).build();

            if (transparentBackground) {
                bim = GFXFunctions.pseudoTransparent(bim, palette.get(0).toARGB());
            }
            if (includePalette) {
                for (int j = 0; j < palette.size(); j++) {
                    bim.setRGB(j, 0, palette.get(j).toARGB());
                }
            }

            return bim;
        }

        @Override
        public BufferedImage getFull() {
            if (pk.getNumber() == SpeciesIDs.unown) {
                setIncludePalette(true);

                BufferedImage[] normal = new BufferedImage[Gen2Constants.unownFormeCount*2];
                BufferedImage[] shiny = new BufferedImage[Gen2Constants.unownFormeCount*2];
                for (int i = 0; i < Gen2Constants.unownFormeCount; i++) {
                    setGraphicalForme(i);

                    normal[i*2] = get();
                    normal[i*2 + 1] = setBack(true).get();
                    shiny[i*2 + 1] = setShiny(true).get();
                    shiny[i*2] = setBack(false).get();
                    setShiny(false);
                }
                return GFXFunctions.stitchToGrid(new BufferedImage[][] { normal, shiny });

            } else {
                return super.getFull();
            }
        }
    }

    private byte[] readPokemonOrTrainerImageData(int pointerOffset, int imageWidth, int imageHeight) {
        int imageOffset = readPokemonOrTrainerImagePointer(pointerOffset);
        byte[] data = Gen2Decmp.decompress(rom, imageOffset);
        return Arrays.copyOf(data, imageWidth * imageHeight * 16);
    }

    @Override
    public Gen2RomEntry getRomEntry() {
        return romEntry;
    }

    @Override
    public void writeCheckValueToROM(int value) {
        if (romEntry.getIntValue("CheckValueOffset") > 0) {
            int cvOffset = romEntry.getIntValue("CheckValueOffset");
            byte[] cvBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                rom[cvOffset + i] = (byte) ((value >> (3 - i) * 8) & 0xFF);
            }
            writeBytes(cvOffset, cvBytes);
        }
    }


}
