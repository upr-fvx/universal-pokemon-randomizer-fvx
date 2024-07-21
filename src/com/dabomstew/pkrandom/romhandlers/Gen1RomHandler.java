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
import com.dabomstew.pkrandom.graphics.packs.GBCPlayerCharacterGraphics;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.graphics.palettes.Palette;
import com.dabomstew.pkrandom.graphics.palettes.SGBPaletteID;
import com.dabomstew.pkrandom.game_data.*;
import com.dabomstew.pkrandom.romhandlers.romentries.GBCTMTextEntry;
import com.dabomstew.pkrandom.romhandlers.romentries.Gen1RomEntry;
import compressors.Gen1Cmp;
import compressors.Gen1Decmp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link RomHandler} for Red, Blue, Yellow, Green.
 */
public class Gen1RomHandler extends AbstractGBCRomHandler {

    public static class Factory extends RomHandler.Factory {

        @Override
        public Gen1RomHandler create() {
            return new Gen1RomHandler();
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

    // Important RBY Data Structures

    private int[] pokeNumToRBYTable;
    private int[] pokeRBYToNumTable;
    private int[] moveNumToRomTable;
    private int[] moveRomToNumTable;
    private int pokedexCount;

    private static List<Gen1RomEntry> roms;

    static {
        loadRomEntries();
    }

    private static void loadRomEntries() {
        try {
            roms = Gen1RomEntry.READER.readEntriesFromFile("gen1_offsets.ini");
        } catch (IOException e) {
            throw new RuntimeException("Could not read Rom Entries.", e);
        }
    }

    // This ROM's data
    private Gen1RomEntry romEntry;
    private Species[] pokes;
    private List<Species> speciesList;
    private List<Trainer> trainers;
    private List<Item> items;
    private Move[] moves;
    private Map<Integer, List<MoveLearnt>> movesets;
    private String[] itemNames;
    private String[] mapNames;
    private SubMap[] maps;
    private boolean xAccNerfed;

    @Override
    public boolean detectRom(byte[] rom) {
        return detectRomInner(rom, rom.length);
    }

    public static boolean detectRomInner(byte[] rom, int romSize) {
        // size check
        return romSize >= GBConstants.minRomSize && romSize <= GBConstants.maxRomSize && checkRomEntry(rom) != null;
    }

    @Override
    public void midLoadingSetUp() {
        super.midLoadingSetUp();
        pokeNumToRBYTable = new int[256];
        pokeRBYToNumTable = new int[256];
        moveNumToRomTable = new int[256];
        moveRomToNumTable = new int[256];
        maps = new SubMap[256];
        xAccNerfed = false;
        preloadMaps();
        loadMapNames();
    }

    @Override
    protected void initRomEntry() {
        romEntry = checkRomEntry(this.rom);

        addPlayerFrontImagePointersToRomEntry();
        addPlayerBackImageBankOffsetsToRomEntry();
        addOldManBackImagePointerToRomEntry();
        if (romEntry.isYellow()) {
            addOakBackImagePointerToRomEntry();
        }
    }

    private void addPlayerFrontImagePointersToRomEntry() {
        int[] oldPointers = romEntry.getArrayValue("PlayerFrontImagePointers");
        int[] oldBankOffsets = romEntry.getArrayValue("PlayerFrontImageBankOffsets");
        if (oldPointers.length != 5 || oldBankOffsets.length != 1) {
            return;
        }

        int[] newPointers = new int[6];
        System.arraycopy(oldPointers, 0, newPointers, 0, 5);
        newPointers[5] = oldPointers[1] + Gen1Constants.playerFrontImageOffset5;
        romEntry.putArrayValue("PlayerFrontImagePointers", newPointers);

        int[] newBankOffsets = new int[]{oldBankOffsets[0],
                newPointers[1] + Gen1Constants.playerFrontBankOffset1,
                newPointers[2] + Gen1Constants.playerFrontBankOffset2,
                newPointers[3] + Gen1Constants.playerFrontBankOffset3,
                newPointers[4] + Gen1Constants.playerFrontBankOffset4,
                newPointers[5] + Gen1Constants.playerFrontBankOffset5};
        romEntry.putArrayValue("PlayerFrontImageBankOffsets", newBankOffsets);
    }

    private void addPlayerBackImageBankOffsetsToRomEntry() {
        int[] pointers = romEntry.getArrayValue("PlayerBackImagePointers");
        if (pointers.length != 2) {
            return;
        }
        int backOffset0 = romEntry.isYellow() ? Gen1Constants.playerBackImageOffsetYellow0 :
                Gen1Constants.playerBackImageOffsetRGB0;
        int[] bankOffsets = new int[]{pointers[0] + backOffset0,
                pointers[1] + Gen1Constants.playerBackImageOffset1};
        romEntry.putArrayValue("PlayerBackImageBankOffsets", bankOffsets);
    }

    private void addOldManBackImagePointerToRomEntry() {
        int[] playerBackImagePointers = romEntry.getArrayValue("PlayerBackImagePointers");
        if (playerBackImagePointers.length != 0 && romEntry.getIntValue("OldManBackImagePointer") == 0) {
            int oldManOffset = romEntry.isYellow() ? Gen1Constants.oldManBackImageOffsetYellow :
                    Gen1Constants.oldManBackImageOffsetRGB;
            romEntry.putIntValue("OldManBackImagePointer", playerBackImagePointers[0] + oldManOffset);
        }
    }

    private void addOakBackImagePointerToRomEntry() {
        int[] playerBackImagePointers = romEntry.getArrayValue("PlayerBackImagePointers");
        if (playerBackImagePointers.length != 0 && romEntry.getIntValue("OakBackImagePointer") == 0) {
            romEntry.putIntValue("OakBackImagePointer", playerBackImagePointers[0] + Gen1Constants.oakBackImageOffset);
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

    private static Gen1RomEntry checkRomEntry(byte[] rom) {
        int version = rom[GBConstants.versionOffset] & 0xFF;
        int nonjap = rom[GBConstants.jpFlagOffset] & 0xFF;
        // Check for specific CRC first
        int crcInHeader = ((rom[GBConstants.crcOffset] & 0xFF) << 8) | (rom[GBConstants.crcOffset + 1] & 0xFF);
        for (Gen1RomEntry re : roms) {
            if (romSig(rom, re.getRomCode()) && re.getVersion() == version && re.getNonJapanese() == nonjap
                    && re.getCRCInHeader() == crcInHeader) {
                return new Gen1RomEntry(re);
            }
        }
        // Now check for non-specific-CRC entries
        for (Gen1RomEntry re : roms) {
            if (romSig(rom, re.getRomCode()) && re.getVersion() == version && re.getNonJapanese() == nonjap && re.getCRCInHeader() == -1) {
                return new Gen1RomEntry(re);
            }
        }
        // Not found
        return null;
    }

    private String[] readMoveNames() {
        int moveCount = romEntry.getIntValue("MoveCount");
        int offset = romEntry.getIntValue("MoveNamesOffset");
        String[] moveNames = new String[moveCount + 1];
        for (int i = 1; i <= moveCount; i++) {
            moveNames[i] = readVariableLengthString(offset, false);
            offset += lengthOfStringAt(offset, false);
        }
        return moveNames;
    }

    @Override
    public void loadMoves() {
        String[] moveNames = readMoveNames();
        int moveCount = romEntry.getIntValue("MoveCount");
        int movesOffset = romEntry.getIntValue("MoveDataOffset");
        // check real move count
        int trueMoveCount = 0;
        for (int i = 1; i <= moveCount; i++) {
            // temp hack for Brown
            if (rom[movesOffset + (i - 1) * 6] != 0 && !moveNames[i].equals("Nothing")) {
                trueMoveCount++;
            }
        }
        moves = new Move[trueMoveCount + 1];
        int trueMoveIndex = 0;

        for (int i = 1; i <= moveCount; i++) {
            int anim = rom[movesOffset + (i - 1) * 6] & 0xFF;
            // another temp hack for brown
            if (anim > 0 && !moveNames[i].equals("Nothing")) {
                trueMoveIndex++;
                moveNumToRomTable[trueMoveIndex] = i;
                moveRomToNumTable[i] = trueMoveIndex;
                moves[trueMoveIndex] = new Move();
                moves[trueMoveIndex].name = moveNames[i];
                moves[trueMoveIndex].internalId = i;
                moves[trueMoveIndex].number = trueMoveIndex;
                moves[trueMoveIndex].effectIndex = rom[movesOffset + (i - 1) * 6 + 1] & 0xFF;
                moves[trueMoveIndex].hitratio = ((rom[movesOffset + (i - 1) * 6 + 4] & 0xFF)) / 255.0 * 100;
                moves[trueMoveIndex].power = rom[movesOffset + (i - 1) * 6 + 2] & 0xFF;
                moves[trueMoveIndex].pp = rom[movesOffset + (i - 1) * 6 + 5] & 0xFF;
                moves[trueMoveIndex].type = Gen1Constants.typeTable[rom[movesOffset + (i - 1) * 6 + 3] & 0xFF];
                moves[trueMoveIndex].category = GBConstants.physicalTypes.contains(moves[trueMoveIndex].type) ? MoveCategory.PHYSICAL : MoveCategory.SPECIAL;
                if (moves[trueMoveIndex].power == 0 && !GlobalConstants.noPowerNonStatusMoves.contains(trueMoveIndex)) {
                    moves[trueMoveIndex].category = MoveCategory.STATUS;
                }

                if (moves[trueMoveIndex].name.equals("Swift")) {
                    perfectAccuracy = (int)moves[trueMoveIndex].hitratio;
                }

                if (GlobalConstants.normalMultihitMoves.contains(i)) {
                    moves[trueMoveIndex].hitCount = 3;
                } else if (GlobalConstants.doubleHitMoves.contains(i)) {
                    moves[trueMoveIndex].hitCount = 2;
                }

                loadStatChangesFromEffect(moves[trueMoveIndex]);
                loadStatusFromEffect(moves[trueMoveIndex]);
                loadMiscMoveInfoFromEffect(moves[trueMoveIndex]);
            }
        }
    }

    private void loadStatChangesFromEffect(Move move) {
        switch (move.effectIndex) {
            case Gen1Constants.noDamageAtkPlusOneEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 1;
                break;
            case Gen1Constants.noDamageDefPlusOneEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 1;
                break;
            case Gen1Constants.noDamageSpecialPlusOneEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL;
                move.statChanges[0].stages = 1;
                break;
            case Gen1Constants.noDamageEvasionPlusOneEffect:
                move.statChanges[0].type = StatChangeType.EVASION;
                move.statChanges[0].stages = 1;
                break;
            case Gen1Constants.noDamageAtkMinusOneEffect:
            case Gen1Constants.damageAtkMinusOneEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = -1;
                break;
            case Gen1Constants.noDamageDefMinusOneEffect:
            case Gen1Constants.damageDefMinusOneEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = -1;
                break;
            case Gen1Constants.noDamageSpeMinusOneEffect:
            case Gen1Constants.damageSpeMinusOneEffect:
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = -1;
                break;
            case Gen1Constants.noDamageAccuracyMinusOneEffect:
                move.statChanges[0].type = StatChangeType.ACCURACY;
                move.statChanges[0].stages = -1;
                break;
            case Gen1Constants.noDamageAtkPlusTwoEffect:
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 2;
                break;
            case Gen1Constants.noDamageDefPlusTwoEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 2;
                break;
            case Gen1Constants.noDamageSpePlusTwoEffect:
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = 2;
                break;
            case Gen1Constants.noDamageSpecialPlusTwoEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL;
                move.statChanges[0].stages = 2;
                break;
            case Gen1Constants.noDamageDefMinusTwoEffect:
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = -2;
                break;
            case Gen1Constants.damageSpecialMinusOneEffect:
                move.statChanges[0].type = StatChangeType.SPECIAL;
                move.statChanges[0].stages = -1;
                break;
            default:
                // Move does not have a stat-changing effect
                return;
        }

        switch (move.effectIndex) {
            case Gen1Constants.noDamageAtkPlusOneEffect:
            case Gen1Constants.noDamageDefPlusOneEffect:
            case Gen1Constants.noDamageSpecialPlusOneEffect:
            case Gen1Constants.noDamageEvasionPlusOneEffect:
            case Gen1Constants.noDamageAtkMinusOneEffect:
            case Gen1Constants.noDamageDefMinusOneEffect:
            case Gen1Constants.noDamageSpeMinusOneEffect:
            case Gen1Constants.noDamageAccuracyMinusOneEffect:
            case Gen1Constants.noDamageAtkPlusTwoEffect:
            case Gen1Constants.noDamageDefPlusTwoEffect:
            case Gen1Constants.noDamageSpePlusTwoEffect:
            case Gen1Constants.noDamageSpecialPlusTwoEffect:
            case Gen1Constants.noDamageDefMinusTwoEffect:
                if (move.statChanges[0].stages < 0) {
                    move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_TARGET;
                } else {
                    move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_USER;
                }
                break;

            case Gen1Constants.damageAtkMinusOneEffect:
            case Gen1Constants.damageDefMinusOneEffect:
            case Gen1Constants.damageSpeMinusOneEffect:
            case Gen1Constants.damageSpecialMinusOneEffect:
                move.statChangeMoveType = StatChangeMoveType.DAMAGE_TARGET;
                break;
        }

        if (move.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET) {
            for (int i = 0; i < move.statChanges.length; i++) {
                if (move.statChanges[i].type != StatChangeType.NONE) {
                    move.statChanges[i].percentChance = 85 / 256.0;
                }
            }
        }
    }

    private void loadStatusFromEffect(Move move) {
        switch (move.effectIndex) {
            case Gen1Constants.noDamageSleepEffect:
            case Gen1Constants.noDamageConfusionEffect:
            case Gen1Constants.noDamagePoisonEffect:
            case Gen1Constants.noDamageParalyzeEffect:
                move.statusMoveType = StatusMoveType.NO_DAMAGE;
                break;

            case Gen1Constants.damagePoison20PercentEffect:
            case Gen1Constants.damageBurn10PercentEffect:
            case Gen1Constants.damageFreeze10PercentEffect:
            case Gen1Constants.damageParalyze10PercentEffect:
            case Gen1Constants.damagePoison40PercentEffect:
            case Gen1Constants.damageBurn30PercentEffect:
            case Gen1Constants.damageFreeze30PercentEffect:
            case Gen1Constants.damageParalyze30PercentEffect:
            case Gen1Constants.damageConfusionEffect:
            case Gen1Constants.twineedleEffect:
                move.statusMoveType = StatusMoveType.DAMAGE;
                break;

            default:
                // Move does not have a status effect
                return;
        }

        switch (move.effectIndex) {
            case Gen1Constants.noDamageSleepEffect:
                move.statusType = StatusType.SLEEP;
                break;
            case Gen1Constants.damagePoison20PercentEffect:
            case Gen1Constants.damagePoison40PercentEffect:
            case Gen1Constants.noDamagePoisonEffect:
            case Gen1Constants.twineedleEffect: {
                move.statusType = StatusType.POISON;
                if (move.number == MoveIDs.toxic) {
                    move.statusType = StatusType.TOXIC_POISON;
                }
                break;
            }
            case Gen1Constants.damageBurn10PercentEffect:
            case Gen1Constants.damageBurn30PercentEffect:
                move.statusType = StatusType.BURN;
                break;
            case Gen1Constants.damageFreeze10PercentEffect:
            case Gen1Constants.damageFreeze30PercentEffect:
                move.statusType = StatusType.FREEZE;
                break;
            case Gen1Constants.damageParalyze10PercentEffect:
            case Gen1Constants.damageParalyze30PercentEffect:
            case Gen1Constants.noDamageParalyzeEffect:
                move.statusType = StatusType.PARALYZE;
                break;
            case Gen1Constants.noDamageConfusionEffect:
            case Gen1Constants.damageConfusionEffect:
                move.statusType = StatusType.CONFUSION;
                break;
        }

        if (move.statusMoveType == StatusMoveType.DAMAGE) {
            switch (move.effectIndex) {
                case Gen1Constants.damageBurn10PercentEffect:
                case Gen1Constants.damageFreeze10PercentEffect:
                case Gen1Constants.damageParalyze10PercentEffect:
                case Gen1Constants.damageConfusionEffect:
                    move.statusPercentChance = 10.0;
                    break;
                case Gen1Constants.damagePoison20PercentEffect:
                case Gen1Constants.twineedleEffect:
                    move.statusPercentChance = 20.0;
                    break;
                case Gen1Constants.damageBurn30PercentEffect:
                case Gen1Constants.damageFreeze30PercentEffect:
                case Gen1Constants.damageParalyze30PercentEffect:
                    move.statusPercentChance = 30.0;
                    break;
                case Gen1Constants.damagePoison40PercentEffect:
                    move.statusPercentChance = 40.0;
                    break;
            }
        }
    }

    private void loadMiscMoveInfoFromEffect(Move move) {
        switch (move.effectIndex) {
            case Gen1Constants.flinch10PercentEffect:
                move.flinchPercentChance = 10.0;
                break;
            case Gen1Constants.flinch30PercentEffect:
                move.flinchPercentChance = 30.0;
                break;
            case Gen1Constants.damageAbsorbEffect:
            case Gen1Constants.dreamEaterEffect:
                move.absorbPercent = 50;
                break;
            case Gen1Constants.damageRecoilEffect:
                move.recoilPercent = 25;
                break;
            case Gen1Constants.chargeEffect:
            case Gen1Constants.flyEffect:
                move.isChargeMove = true;
                break;
            case Gen1Constants.hyperBeamEffect:
                move.isRechargeMove = true;
                break;
        }

        if (Gen1Constants.increasedCritMoves.contains(move.number)) {
            move.criticalChance = CriticalChance.INCREASED;
        }
    }

    @Override
    public void saveMoves() {
        int movesOffset = romEntry.getIntValue("MoveDataOffset");
        for (Move m : moves) {
            if (m != null) {
                int i = m.internalId;
                int hitratio = (int) Math.round(m.hitratio * 2.55);
                hitratio = Math.max(0, hitratio);
                hitratio = Math.min(255, hitratio);
                writeBytes(movesOffset + (i - 1) * 6 + 1, new byte[]{
                        (byte) m.effectIndex, (byte) m.power,
                        Gen1Constants.typeToByte(m.type), (byte) hitratio, (byte) m.pp
                });
            }
        }
    }

    public List<Move> getMoves() {
        return Arrays.asList(moves);
    }

    private void loadPokedexOrder() {
        int pkmnCount = romEntry.getIntValue("InternalPokemonCount");
        int orderOffset = romEntry.getIntValue("PokedexOrder");
        pokedexCount = 0;
        for (int i = 1; i <= pkmnCount; i++) {
            int pokedexNum = rom[orderOffset + i - 1] & 0xFF;
            pokeRBYToNumTable[i] = pokedexNum;
            if (pokedexNum != 0 && pokeNumToRBYTable[pokedexNum] == 0) {
                pokeNumToRBYTable[pokedexNum] = i;
            }
            pokedexCount = Math.max(pokedexCount, pokedexNum);
        }
    }

    @Override
    public void loadPokemonStats() {
        loadPokedexOrder();

        pokes = new Gen1Species[pokedexCount + 1];
        // Fetch our names
        String[] pokeNames = readPokemonNames();
        // Get base stats
        int pokeStatsOffset = romEntry.getIntValue("PokemonStatsOffset");
        for (int i = 1; i <= pokedexCount; i++) {
            pokes[i] = new Gen1Species(i);
            if (i != SpeciesIDs.mew || romEntry.isYellow()) {
                loadBasicPokeStats((Gen1Species) pokes[i], pokeStatsOffset + (i - 1) * Gen1Constants.baseStatsEntrySize);
            }
            pokes[i].setName(pokeNames[pokeNumToRBYTable[i]]);
        }

        // Mew override for R/B
        if (!romEntry.isYellow()) {
            loadBasicPokeStats((Gen1Species) pokes[SpeciesIDs.mew], romEntry.getIntValue("MewStatsOffset"));
        }

        this.speciesList = Arrays.asList(pokes);
    }

    @Override
    public void savePokemonStats() {
        // Write pokemon names
        int offs = romEntry.getIntValue("PokemonNamesOffset");
        int nameLength = romEntry.getIntValue("PokemonNamesLength");
        for (int i = 1; i <= pokedexCount; i++) {
            int rbynum = pokeNumToRBYTable[i];
            int stringOffset = offs + (rbynum - 1) * nameLength;
            writeFixedLengthString(pokes[i].getName(), stringOffset, nameLength);
        }
        // Write pokemon stats
        int pokeStatsOffset = romEntry.getIntValue("PokemonStatsOffset");
        for (int i = 1; i <= pokedexCount; i++) {
            if (i == SpeciesIDs.mew) {
                continue;
            }
            saveBasicPokeStats(pokes[i], pokeStatsOffset + (i - 1) * Gen1Constants.baseStatsEntrySize);
        }
        // Write MEW
        int mewOffset = romEntry.isYellow() ? pokeStatsOffset + (SpeciesIDs.mew - 1)
                * Gen1Constants.baseStatsEntrySize : romEntry.getIntValue("MewStatsOffset");
        saveBasicPokeStats(pokes[SpeciesIDs.mew], mewOffset);

        // Write evolutions and movesets
        saveEvosAndMovesLearnt();
    }

    private void loadBasicPokeStats(Gen1Species pkmn, int offset) {
        pkmn.setHp(rom[offset + Gen1Constants.bsHPOffset] & 0xFF);
        pkmn.setAttack(rom[offset + Gen1Constants.bsAttackOffset] & 0xFF);
        pkmn.setDefense(rom[offset + Gen1Constants.bsDefenseOffset] & 0xFF);
        pkmn.setSpeed(rom[offset + Gen1Constants.bsSpeedOffset] & 0xFF);
        pkmn.setSpecial(rom[offset + Gen1Constants.bsSpecialOffset] & 0xFF);
        // Type
        pkmn.setPrimaryType(Gen1Constants.typeTable[rom[offset + Gen1Constants.bsPrimaryTypeOffset] & 0xFF]);
        pkmn.setSecondaryType(Gen1Constants.typeTable[rom[offset + Gen1Constants.bsSecondaryTypeOffset] & 0xFF]);
        // Only one type?
        if (pkmn.getSecondaryType(false) == pkmn.getPrimaryType(false)) {
            pkmn.setSecondaryType(null);
        }

        pkmn.setCatchRate(rom[offset + Gen1Constants.bsCatchRateOffset] & 0xFF);
        pkmn.setExpYield(rom[offset + Gen1Constants.bsExpYieldOffset] & 0xFF);
        pkmn.setGrowthCurve(ExpCurve.fromByte(rom[offset + Gen1Constants.bsGrowthCurveOffset]));
        pkmn.setFrontImageDimensions(rom[offset + Gen1Constants.bsFrontImageDimensionsOffset] & 0xFF);
        pkmn.setFrontImagePointer(readWord(offset + Gen1Constants.bsFrontImagePointerOffset));
        pkmn.setBackImagePointer(readWord(offset + Gen1Constants.bsBackImagePointerOffset));
        
        pkmn.setGuaranteedHeldItem(-1);
        pkmn.setCommonHeldItem(-1);
        pkmn.setRareHeldItem(-1);
        pkmn.setDarkGrassHeldItem(-1);
    }

    private void saveBasicPokeStats(Species pkmn, int offset) {
        writeByte(offset + Gen1Constants.bsHPOffset, (byte) pkmn.getHp());
        writeByte(offset + Gen1Constants.bsAttackOffset, (byte) pkmn.getAttack());
        writeByte(offset + Gen1Constants.bsDefenseOffset, (byte) pkmn.getDefense());
        writeByte(offset + Gen1Constants.bsSpeedOffset, (byte) pkmn.getSpeed());
        writeByte(offset + Gen1Constants.bsSpecialOffset, (byte) pkmn.getSpecial());
        writeByte(offset + Gen1Constants.bsPrimaryTypeOffset, Gen1Constants.typeToByte(pkmn.getPrimaryType(false)));
        byte secondaryTypeByte = pkmn.getSecondaryType(false) == null ? rom[offset + Gen1Constants.bsPrimaryTypeOffset]
                : Gen1Constants.typeToByte(pkmn.getSecondaryType(false));
        writeByte(offset + Gen1Constants.bsSecondaryTypeOffset, secondaryTypeByte);
        writeByte(offset + Gen1Constants.bsCatchRateOffset, (byte) pkmn.getCatchRate());
        writeByte(offset + Gen1Constants.bsGrowthCurveOffset, pkmn.getGrowthCurve().toByte());
        writeByte(offset + Gen1Constants.bsExpYieldOffset, (byte) pkmn.getExpYield());
    }

    private String[] readPokemonNames() {
        int offs = romEntry.getIntValue("PokemonNamesOffset");
        int nameLength = romEntry.getIntValue("PokemonNamesLength");
        int pkmnCount = romEntry.getIntValue("InternalPokemonCount");
        String[] names = new String[pkmnCount + 1];
        for (int i = 1; i <= pkmnCount; i++) {
            names[i] = readFixedLengthString(offs + (i - 1) * nameLength, nameLength);
        }
        return names;
    }

    @Override
    public List<Species> getStarters() {
        // Get the starters
        List<Species> starters = new ArrayList<>();
        starters.add(pokes[pokeRBYToNumTable[rom[romEntry.getArrayValue("StarterOffsets1")[0]] & 0xFF]]);
        starters.add(pokes[pokeRBYToNumTable[rom[romEntry.getArrayValue("StarterOffsets2")[0]] & 0xFF]]);
        if (!romEntry.isYellow()) {
            starters.add(pokes[pokeRBYToNumTable[rom[romEntry.getArrayValue("StarterOffsets3")[0]] & 0xFF]]);
        }
        return starters;
    }

    @Override
    public boolean setStarters(List<Species> newStarters) {
        // Amount?
        int starterAmount = isYellow() ? 2 : 3;

        // Basic checks
        if (newStarters.size() != starterAmount) {
            throw new IllegalArgumentException("Unexpected amount of new starters. Should be " + starterAmount +
                    ", was " + newStarters.size());
        }

        // Patch starter bytes
        for (int i = 0; i < starterAmount; i++) {
            byte starter = (byte) pokeNumToRBYTable[newStarters.get(i).getNumber()];
            int[] offsets = romEntry.getArrayValue("StarterOffsets" + (i + 1));
            for (int offset : offsets) {
                writeByte(offset, starter);
            }
        }

        // Special stuff for non-Yellow only

        if (!romEntry.isYellow()) {

            // Starter text
            if (romEntry.getIntValue("CanChangeStarterText") > 0) {
                int[] starterTextOffsets = romEntry.getArrayValue("StarterTextOffsets");
                for (int i = 0; i < 3 && i < starterTextOffsets.length; i++) {
                    writeVariableLengthString(String.format("So! You want\\n%s?\\e", newStarters.get(i).getName()),
                            starterTextOffsets[i], true);
                }
            }

            // Patch starter pokedex routine?
            // Can only do in 1M roms because of size concerns
            if (romEntry.getIntValue("PatchPokedex") > 0) {

                // Starter pokedex required RAM values
                // RAM offset => value
                // Allows for multiple starters in the same RAM byte
                Map<Integer, Integer> onValues = new TreeMap<>();
                for (int i = 0; i < 3; i++) {
                    int pkDexNum = newStarters.get(i).getNumber();
                    int ramOffset = (pkDexNum - 1) / 8 + romEntry.getIntValue("PokedexRamOffset");
                    int bitShift = (pkDexNum - 1) % 8;
                    int writeValue = 1 << bitShift;
                    if (onValues.containsKey(ramOffset)) {
                        onValues.put(ramOffset, onValues.get(ramOffset) | writeValue);
                    } else {
                        onValues.put(ramOffset, writeValue);
                    }
                }

                // Starter pokedex offset/pointer calculations

                int pkDexOnOffset = romEntry.getIntValue("StarterPokedexOnOffset");
                int pkDexOffOffset = romEntry.getIntValue("StarterPokedexOffOffset");

                int sizeForOnRoutine = 5 * onValues.size() + 3;
                int writeOnRoutineTo = romEntry.getIntValue("StarterPokedexBranchOffset");
                int writeOffRoutineTo = writeOnRoutineTo + sizeForOnRoutine;

                // Starter pokedex
                // Branch to our new routine(s)

                // Turn bytes on
                rom[pkDexOnOffset] = GBConstants.gbZ80Jump;
                writePointer(pkDexOnOffset + 1, writeOnRoutineTo);
                rom[pkDexOnOffset + 3] = GBConstants.gbZ80Nop;
                rom[pkDexOnOffset + 4] = GBConstants.gbZ80Nop;

                // Turn bytes off
                rom[pkDexOffOffset] = GBConstants.gbZ80Jump;
                writePointer(pkDexOffOffset + 1, writeOffRoutineTo);
                rom[pkDexOffOffset + 3] = GBConstants.gbZ80Nop;

                // Put together the two scripts
                rom[writeOffRoutineTo] = GBConstants.gbZ80XorA;
                int turnOnOffset = writeOnRoutineTo;
                int turnOffOffset = writeOffRoutineTo + 1;
                for (int ramOffset : onValues.keySet()) {
                    int onValue = onValues.get(ramOffset);
                    // Turn on code
                    rom[turnOnOffset++] = GBConstants.gbZ80LdA;
                    rom[turnOnOffset++] = (byte) onValue;
                    // Turn on code for ram writing
                    rom[turnOnOffset++] = GBConstants.gbZ80LdAToFar;
                    rom[turnOnOffset++] = (byte) (ramOffset % 0x100);
                    rom[turnOnOffset++] = (byte) (ramOffset / 0x100);
                    // Turn off code for ram writing
                    rom[turnOffOffset++] = GBConstants.gbZ80LdAToFar;
                    rom[turnOffOffset++] = (byte) (ramOffset % 0x100);
                    rom[turnOffOffset++] = (byte) (ramOffset / 0x100);
                }
                // Jump back
                rom[turnOnOffset++] = GBConstants.gbZ80Jump;
                writePointer(turnOnOffset, pkDexOnOffset + 5);

                rom[turnOffOffset++] = GBConstants.gbZ80Jump;
                writePointer(turnOffOffset, pkDexOffOffset + 4);
            }

        }

        // If we're changing the player's starter for Yellow, then the player can't get the
        // Bulbasaur gift unless they randomly stumble into a Pikachu somewhere else. This is
        // because you need a certain amount of Pikachu happiness to acquire this gift, and
        // happiness only accumulates if you have a Pikachu. Instead, just patch out this check.
        if (romEntry.getIntValue("PikachuHappinessCheckOffset") != 0 && newStarters.get(0).getNumber() != SpeciesIDs.pikachu) {
            int offset = romEntry.getIntValue("PikachuHappinessCheckOffset");

            // The code looks like this:
            // ld a, [wPikachuHappiness]
            // cp 147
            // jr c, .asm_1cfb3    <- this is where "offset" is
            // Write two nops to patch out the jump
            writeBytes(offset, new byte[]{GBConstants.gbZ80Nop, GBConstants.gbZ80Nop});
        }

        return true;
    }

    /*
    @Override
    public boolean hasStarterTypeTriangleSupport() {
        return !isYellow();
    }
     */

    @Override
    public boolean hasStarterAltFormes() {
        return false;
    }

    @Override
    public int starterCount() {
        return isYellow() ? 2 : 3;
    }

    @Override
    public boolean supportsStarterHeldItems() {
        // No held items in Gen 1
        return false;
    }

    @Override
    public List<Integer> getStarterHeldItems() {
        // do nothing
        return new ArrayList<>();
    }

    @Override
    public void setStarterHeldItems(List<Integer> items) {
        // do nothing
    }

    @Override
    public Set<Item> getEvolutionItems() {
        return null; // TODO: implement
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        List<EncounterArea> encounterAreas = new ArrayList<>();

        readNormalEncounters(encounterAreas);
        readFishingEncounters(encounterAreas);

        tagEncounterAreas(encounterAreas);

        return encounterAreas;
    }

    private void tagEncounterAreas(List<EncounterArea> encounterAreas) {
        if (romEntry.isYellow()) {
            Gen1Constants.tagEncounterAreasYellow(encounterAreas);
        } else if (romEntry.getName().equals("Blue (J)")) { // kind of ugly to refer to a specific ROM name
            Gen1Constants.tagEncounterAreasJapaneseBlue(encounterAreas);
        } else {
            Gen1Constants.tagEncounterAreasRBG(encounterAreas);
        }
    }

    private Species getGhostMarowakPoke() {
        return canChangeStaticPokemon() ? pokes[pokeRBYToNumTable[rom[romEntry.getGhostMarowakOffsets()[0]] & 0xFF]] :
                pokes[SpeciesIDs.marowak];
    }

    private void readNormalEncounters(List<EncounterArea> encounterAreas) {
        List<Integer> usedOffsets = new ArrayList<>();
        int tableOffset = romEntry.getIntValue("WildPokemonTableOffset");
        int mapID = -1;
        Species ghostMarowak = getGhostMarowakPoke();

        while (readWord(tableOffset) != Gen1Constants.encounterTableEnd) {
            mapID++;
            int offset = readPointer(tableOffset);
            int rootOffset = offset;
            if (!usedOffsets.contains(offset)) {
                usedOffsets.add(offset);
                // grass and water are exactly the same
                for (int a = 0; a < 2; a++) {
                    int rate = rom[offset++] & 0xFF;
                    if (rate > 0) {
                        // there is data here
                        EncounterArea thisArea = new EncounterArea();
                        thisArea.setRate(rate);
                        thisArea.setMapIndex(rootOffset);
                        thisArea.setDisplayName((a == 1 ? "Surfing" : "Grass/Cave") + " on " + mapNames[mapID]);
                        thisArea.setEncounterType(a == 1 ? EncounterType.SURFING : EncounterType.WALKING);
                        if (mapID >= Gen1Constants.towerMapsStartIndex && mapID <= Gen1Constants.towerMapsEndIndex) {
                            thisArea.banPokemon(ghostMarowak);
                        }
                        for (int slot = 0; slot < Gen1Constants.encounterTableSize; slot++) {
                            Encounter enc = new Encounter();
                            enc.setLevel(rom[offset] & 0xFF);
                            enc.setSpecies(pokes[pokeRBYToNumTable[rom[offset + 1] & 0xFF]]);
                            thisArea.add(enc);
                            offset += 2;
                        }
                        encounterAreas.add(thisArea);
                    }
                }
            } else {
                for (EncounterArea area : encounterAreas) {
                    if (area.getMapIndex() == offset) {
                        area.setDisplayName(area.getDisplayName() + ", " + mapNames[mapID]);
                    }
                }
            }
            tableOffset += 2;
        }
    }

    private void readFishingEncounters(List<EncounterArea> encounterAreas) {
        readOldRodEncounters(encounterAreas);
        readGoodRodEncounters(encounterAreas);
        readSuperRodEncounters(encounterAreas);
    }

    public void readOldRodEncounters(List<EncounterArea> encounterAreas) {
        int oldRodOffset = romEntry.getIntValue("OldRodOffset");
        EncounterArea area = new EncounterArea();
        area.setDisplayName("Old Rod Fishing");
        area.setEncounterType(EncounterType.FISHING);
        Encounter oldRodEnc = new Encounter();
        oldRodEnc.setLevel(rom[oldRodOffset + 2] & 0xFF);
        oldRodEnc.setSpecies(pokes[pokeRBYToNumTable[rom[oldRodOffset + 1] & 0xFF]]);
        area.add(oldRodEnc);
        area.banPokemon(getGhostMarowakPoke());

        encounterAreas.add(area);
    }

    public void readGoodRodEncounters(List<EncounterArea> encounterAreas) {
        int goodRodOffset = romEntry.getIntValue("GoodRodOffset");
        EncounterArea area = new EncounterArea();
        area.setDisplayName("Good Rod Fishing");
        area.setEncounterType(EncounterType.FISHING);
        for (int slot = 0; slot < 2; slot++) {
            Encounter enc = new Encounter();
            enc.setLevel(rom[goodRodOffset + slot * 2] & 0xFF);
            enc.setSpecies(pokes[pokeRBYToNumTable[rom[goodRodOffset + slot * 2 + 1] & 0xFF]]);
            area.add(enc);
        }
        area.banPokemon(getGhostMarowakPoke());

        encounterAreas.add(area);
    }

    public void readSuperRodEncounters(List<EncounterArea> encounterAreas) {
        Species ghostMarowak = getGhostMarowakPoke();

        if (romEntry.isYellow()) {
            int superRodOffset = romEntry.getIntValue("SuperRodTableOffset");
            while ((rom[superRodOffset] & 0xFF) != 0xFF) {
                int map = rom[superRodOffset++] & 0xFF;
                EncounterArea area = new EncounterArea();
                area.setDisplayName("Super Rod Fishing on " + mapNames[map]);
                area.setEncounterType(EncounterType.FISHING);
                for (int encN = 0; encN < Gen1Constants.yellowSuperRodTableSize; encN++) {
                    Encounter enc = new Encounter();
                    enc.setLevel(rom[superRodOffset + 1] & 0xFF);
                    enc.setSpecies(pokes[pokeRBYToNumTable[rom[superRodOffset] & 0xFF]]);
                    area.add(enc);
                    superRodOffset += 2;
                }
                area.banPokemon(ghostMarowak);
                encounterAreas.add(area);
            }
        } else {
            // red/blue
            int superRodOffset = romEntry.getIntValue("SuperRodTableOffset");
            List<Integer> usedSROffsets = new ArrayList<>();
            while ((rom[superRodOffset] & 0xFF) != 0xFF) {
                int map = rom[superRodOffset++] & 0xFF;
                int areaOffset = readPointer(superRodOffset);
                superRodOffset += 2;
                if (!usedSROffsets.contains(areaOffset)) {
                    usedSROffsets.add(areaOffset);
                    EncounterArea area = new EncounterArea();
                    area.setDisplayName("Super Rod Fishing on " + mapNames[map]);
                    area.setEncounterType(EncounterType.FISHING);
                    area.setMapIndex(areaOffset);
                    int pokesInArea = rom[areaOffset++] & 0xFF;
                    for (int encN = 0; encN < pokesInArea; encN++) {
                        Encounter enc = new Encounter();
                        enc.setLevel(rom[areaOffset] & 0xFF);
                        enc.setSpecies(pokes[pokeRBYToNumTable[rom[areaOffset + 1] & 0xFF]]);
                        area.add(enc);
                        areaOffset += 2;
                    }
                    area.banPokemon(ghostMarowak);
                    encounterAreas.add(area);
                } else {
                    for (EncounterArea area : encounterAreas) {
                        if (area.getMapIndex() == areaOffset) {
                            area.setDisplayName(area.getDisplayName() + ", " + mapNames[map]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        return getEncounters(useTimeOfDay).stream()
                .sorted(Comparator.comparingInt(a -> Gen1Constants.locationTagsTraverseOrder.indexOf(a.getLocationTag())))
                .collect(Collectors.toList());
    }

    @Override
    public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounterAreas) {
        Iterator<EncounterArea> areaIterator = encounterAreas.iterator();

        writeNormalEncounters(areaIterator);
        writeFishingEncounters(areaIterator);
    }

    private void writeNormalEncounters(Iterator<EncounterArea> areaIterator) {
        List<Integer> usedOffsets = new ArrayList<>();
        int tableOffset = romEntry.getIntValue("WildPokemonTableOffset");

        while (readWord(tableOffset) != Gen1Constants.encounterTableEnd) {
            int offset = readPointer(tableOffset);
            if (!usedOffsets.contains(offset)) {
                usedOffsets.add(offset);
                // grass and water are exactly the same
                for (int a = 0; a < 2; a++) {
                    int rate = rom[offset++] & 0xFF;
                    if (rate > 0) {
                        // there is data here
                        EncounterArea thisArea = areaIterator.next();
                        for (int slot = 0; slot < Gen1Constants.encounterTableSize; slot++) {
                            Encounter enc = thisArea.get(slot);
                            writeBytes(offset, new byte[]{
                                    (byte) enc.getLevel(),
                                    (byte) pokeNumToRBYTable[enc.getSpecies().getNumber()]
                            });
                            offset += 2;
                        }
                    }
                }
            }
            tableOffset += 2;
        }
    }

    private void writeFishingEncounters(Iterator<EncounterArea> areaIterator) {
        writeOldRodEncounters(areaIterator);
        writeGoodRodEncounters(areaIterator);
        writeSuperRodEncounters(areaIterator);
    }

    private void writeOldRodEncounters(Iterator<EncounterArea> areaIterator) {
        int oldRodOffset = romEntry.getIntValue("OldRodOffset");
        EncounterArea area = areaIterator.next();
        Encounter oldRodEnc = area.get(0);
        writeBytes(oldRodOffset + 1, new byte[]{
                (byte) pokeNumToRBYTable[oldRodEnc.getSpecies().getNumber()],
                (byte) oldRodEnc.getLevel()
        });
    }

    private void writeGoodRodEncounters(Iterator<EncounterArea> areaIterator) {
        int goodRodOffset = romEntry.getIntValue("GoodRodOffset");
        EncounterArea area = areaIterator.next();
        for (int grSlot = 0; grSlot < 2; grSlot++) {
            Encounter enc = area.get(grSlot);
            writeBytes(goodRodOffset + grSlot * 2, new byte[]{
                    (byte) enc.getLevel(),
                    (byte) pokeNumToRBYTable[enc.getSpecies().getNumber()]
            });
        }
    }

    private void writeSuperRodEncounters(Iterator<EncounterArea> areaIterator) {
        int superRodOffset = romEntry.getIntValue("SuperRodTableOffset");
        if (romEntry.isYellow()) {
            while ((rom[superRodOffset] & 0xFF) != 0xFF) {
                superRodOffset++;
                EncounterArea area = areaIterator.next();
                for (int encN = 0; encN < Gen1Constants.yellowSuperRodTableSize; encN++) {
                    Encounter enc = area.get(encN);
                    writeBytes(superRodOffset, new byte[]{
                            (byte) pokeNumToRBYTable[enc.getSpecies().getNumber()],
                            (byte) enc.getLevel()
                    });
                    superRodOffset += 2;
                }
            }
        } else {
            // red/blue
            List<Integer> usedSROffsets = new ArrayList<>();
            while ((rom[superRodOffset] & 0xFF) != 0xFF) {
                superRodOffset++;
                int areaOffset = readPointer(superRodOffset);
                superRodOffset += 2;
                if (!usedSROffsets.contains(areaOffset)) {
                    usedSROffsets.add(areaOffset);
                    int pokesInArea = rom[areaOffset++] & 0xFF;
                    EncounterArea area = areaIterator.next();
                    for (int encN = 0; encN < pokesInArea; encN++) {
                        Encounter enc = area.get(encN);
                        writeBytes(areaOffset, new byte[]{
                                (byte) enc.getLevel(),
                                (byte) pokeNumToRBYTable[enc.getSpecies().getNumber()]});
                        areaOffset += 2;
                    }
                }
            }
        }
    }

    @Override
    public boolean hasEncounterLocations() {
        return true;
    }

    @Override
    public boolean hasWildAltFormes() {
        return false;
    }

    @Override
    public List<Species> getPokemon() {
        return speciesList;
    }

    @Override
    public List<Species> getPokemonInclFormes() {
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
    public Species getAltFormeOfPokemon(Species pk, int forme) {
        return pk;
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
    public List<Trainer> getTrainers() {
        if (trainers == null) {
            throw new IllegalStateException("Trainers have not been loaded.");
        }
        return trainers;
    }

    @Override
    // This is very similar to the implementation in Gen2RomHandler. As trainers is a private field though,
    // the two should only be reconciled during some bigger refactoring, where other private fields (e.g. pokemonList)
    // are considered.
    public void loadTrainers() {
        int trainerClassTableOffset = romEntry.getIntValue("TrainerDataTableOffset");
        int trainerClassAmount = Gen1Constants.trainerClassCount;
        int[] trainersPerClass = romEntry.getArrayValue("TrainerDataClassCounts");
        if (trainersPerClass.length != trainerClassAmount) {
            throw new RuntimeException("Conflicting count of trainer classes.");
        }
        List<String> tcnames = getTrainerClassesForText();

        trainers = new ArrayList<>();

        int index = 0;
        for (int trainerClass = 0; trainerClass < trainerClassAmount; trainerClass++) {

            int offset = readPointer(trainerClassTableOffset + trainerClass * 2);

            for (int trainerNum = 0; trainerNum < trainersPerClass[trainerClass]; trainerNum++) {
                index++;
                Trainer tr = readTrainer(offset);
                tr.index = index;
                tr.trainerclass = trainerClass;
                tr.fullDisplayName = tcnames.get(trainerClass);
                trainers.add(tr);

                offset += trainerToBytes(tr).length;
            }
        }

        tagTrainers();
    }

    private void tagTrainers() {
        Gen1Constants.tagTrainersUniversal(trainers);
        if (romEntry.isYellow()) {
            Gen1Constants.tagTrainersYellow(trainers);
        } else {
            Gen1Constants.tagTrainersRB(trainers);
        }
    }

    private Trainer readTrainer(int offset) {
        Trainer tr = new Trainer();
        tr.offset = offset;
        int dataType = rom[offset] & 0xFF;
        if (dataType == 0xFF) {
            // "Special" trainer
            tr.poketype = 1;
            offset++;
            while (rom[offset] != 0x0) {
                TrainerPokemon tp = new TrainerPokemon();
                tp.setLevel(rom[offset] & 0xFF);
                tp.setSpecies(pokes[pokeRBYToNumTable[rom[offset + 1] & 0xFF]]);
                tr.pokemon.add(tp);
                offset += 2;
            }
        } else {
            tr.poketype = 0;
            offset++;
            while (rom[offset] != 0x0) {
                TrainerPokemon tp = new TrainerPokemon();
                tp.setLevel(dataType);
                tp.setSpecies(pokes[pokeRBYToNumTable[rom[offset] & 0xFF]]);
                tr.pokemon.add(tp);
                offset++;
            }
        }
        return tr;
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
            if (tr.tag != null &&
                    (tr.tag.contains("ELITE") ||
                    (!isYellow() && tr.tag.contains("RIVAL8")) || (isYellow() && tr.tag.contains("RIVAL7")))) {
                eliteFourIndices.add(i + 1);
            }
        }

        return eliteFourIndices;
    }

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        return Gen1Constants.gymAndEliteThemes;
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
        int trainerClassAmount = Gen1Constants.trainerClassCount;
        int[] trainersPerClass = romEntry.getArrayValue("TrainerDataClassCounts");

        Iterator<Trainer> trainerIterator = trainers.iterator();
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
            new GBCDataRewriter<byte[]>().rewriteData(pointerOffset, trainersOfClassBytes, b -> b, oldDataOffset ->
                    lengthOfTrainerClassAt(oldDataOffset, trainersPerThisClass));
        }

        // Custom Moves AI Table
        // Zero it out entirely.
        writeByte(romEntry.getIntValue("ExtraTrainerMovesTableOffset"), (byte) 0xFF);

        // Champion Rival overrides in Red/Blue
        if (!isYellow()) {
            // hacky relative offset (very likely to work but maybe not always)
            int champRivalJump = romEntry.getIntValue("GymLeaderMovesTableOffset")
                    - Gen1Constants.champRivalOffsetFromGymLeaderMoves;
            // nop out this jump
            writeBytes(champRivalJump, new byte[] {GBConstants.gbZ80Nop, GBConstants.gbZ80Nop});
        }
    }

    private byte[] trainerToBytes(Trainer trainer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (trainer.poketype == 0) {
            // Regular trainer
            int fixedLevel = trainer.pokemon.get(0).getLevel();
            baos.write(fixedLevel);
            for (TrainerPokemon tp : trainer.pokemon) {
                baos.write((byte) pokeNumToRBYTable[tp.getSpecies().getNumber()]);
            }
        } else {
            // Special trainer
            baos.write(0xFF);
            for (TrainerPokemon tp : trainer.pokemon) {
                baos.write(tp.getLevel());
                baos.write((byte) pokeNumToRBYTable[tp.getSpecies().getNumber()]);
            }
        }
        baos.write(Gen1Constants.trainerDataTerminator);

        return baos.toByteArray();
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
    public boolean hasRivalFinalBattle() {
        return true;
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
        return false;
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        return false;
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        return false;
    }

    @Override
    public boolean isYellow() {
        return romEntry.isYellow();
    }

    @Override
    public boolean hasMultiplePlayerCharacters() {
        return false;
    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        return Gen1Constants.bannedLevelupMoves;
    }

    @Override
    public TypeTable getTypeTable() {
        return readTypeTable();
    }

    private TypeTable readTypeTable() {
        TypeTable typeTable = new TypeTable(Type.getAllTypes(1));
        int currentOffset = romEntry.getIntValue("TypeEffectivenessOffset");
        int attackingType = rom[currentOffset];
        while (attackingType != GBConstants.typeTableTerminator) {
            int defendingType = rom[currentOffset + 1];
            int effectivenessInternal = rom[currentOffset + 2];
            Type attacking = Gen1Constants.typeTable[attackingType];
            Type defending = Gen1Constants.typeTable[defendingType];
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
                    break;
            }
            if (effectiveness != null) {
                typeTable.setEffectiveness(attacking, defending, effectiveness);
            }
            currentOffset += 3;
            attackingType = rom[currentOffset];
        }
        return typeTable;
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        writeTypeTable(typeTable);
    }

    private void writeTypeTable(TypeTable typeTable) {
        if (typeTable.nonNeutralEffectivenessCount() > Gen1Constants.nonNeutralEffectivenessCount) {
            throw new IllegalArgumentException("Too many non-neutral Effectiveness-es. Was "
                    + typeTable.nonNeutralEffectivenessCount() + ", has to be at most " +
                    Gen1Constants.nonNeutralEffectivenessCount);
        }
        int currentOffset = romEntry.getIntValue("TypeEffectivenessOffset");
        for (Type attacker : typeTable.getTypes()) {
            for (Type defender : typeTable.getTypes()) {
                Effectiveness eff = typeTable.getEffectiveness(attacker, defender);
                if (eff != Effectiveness.NEUTRAL) {
                    byte effectivenessInternal;
                    switch (eff) {
                        case DOUBLE:
                            effectivenessInternal = 20;
                            break;
                        case HALF:
                            effectivenessInternal = 5;
                            break;
                        case ZERO:
                            effectivenessInternal = 0;
                            break;
                        default:
                            effectivenessInternal = 0;
                            break;
                    }
                    writeBytes(currentOffset, new byte[]{Gen1Constants.typeToByte(attacker),
                            Gen1Constants.typeToByte(defender), effectivenessInternal});
                    currentOffset += 3;
                }
            }
        }
        rom[currentOffset] = GBConstants.typeTableTerminator;
    }

    @Override
    protected void loadMovesLearnt() {
        Map<Integer, List<MoveLearnt>> movesets = new TreeMap<>();
        int pointersOffset = romEntry.getIntValue("PokemonMovesetsTableOffset");
        int pokeStatsOffset = romEntry.getIntValue("PokemonStatsOffset");
        int pkmnCount = romEntry.getIntValue("InternalPokemonCount");
        for (int i = 1; i <= pkmnCount; i++) {
            int pointer = readPointer(pointersOffset + (i - 1) * 2);
            if (pokeRBYToNumTable[i] != 0) {
                Species pk = pokes[pokeRBYToNumTable[i]];
                int statsOffset;
                if (pokeRBYToNumTable[i] == SpeciesIDs.mew && !romEntry.isYellow()) {
                    // Mewww
                    statsOffset = romEntry.getIntValue("MewStatsOffset");
                } else {
                    statsOffset = (pokeRBYToNumTable[i] - 1) * 0x1C + pokeStatsOffset;
                }
                List<MoveLearnt> ourMoves = new ArrayList<>();
                for (int delta = Gen1Constants.bsLevel1MovesOffset; delta < Gen1Constants.bsLevel1MovesOffset + 4; delta++) {
                    if (rom[statsOffset + delta] != 0x00) {
                        MoveLearnt learnt = new MoveLearnt();
                        learnt.level = 1;
                        learnt.move = moveRomToNumTable[rom[statsOffset + delta] & 0xFF];
                        ourMoves.add(learnt);
                    }
                }
                // Skip over evolution data
                while (rom[pointer] != 0) {
                    if (rom[pointer] == 1) {
                        pointer += 3;
                    } else if (rom[pointer] == 2) {
                        pointer += 4;
                    } else if (rom[pointer] == 3) {
                        pointer += 3;
                    }
                }
                pointer++;
                while (rom[pointer] != 0) {
                    MoveLearnt learnt = new MoveLearnt();
                    learnt.level = rom[pointer] & 0xFF;
                    learnt.move = moveRomToNumTable[rom[pointer + 1] & 0xFF];
                    ourMoves.add(learnt);
                    pointer += 2;
                }
                movesets.put(pk.getNumber(), ourMoves);
            }
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
    public Map<Integer, List<Integer>> getEggMoves() {
        // Gen 1 does not have egg moves
        return new TreeMap<>();
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
        // Gen 1 does not have egg moves
    }

    public static class StaticPokemon {
        protected int[] speciesOffsets;
        protected int[] levelOffsets;

        public StaticPokemon(int[] speciesOffsets, int[] levelOffsets) {
            this.speciesOffsets = speciesOffsets;
            this.levelOffsets = levelOffsets;
        }

        public int[] getSpeciesOffsets() {
            return speciesOffsets;
        }

        public Species getPokemon(Gen1RomHandler rh) {
            return rh.pokes[rh.pokeRBYToNumTable[rh.rom[speciesOffsets[0]] & 0xFF]];
        }

        public void setPokemon(Gen1RomHandler rh, Species pkmn) {
            for (int offset : speciesOffsets) {
                rh.writeByte(offset, (byte) rh.pokeNumToRBYTable[pkmn.getNumber()]);
            }
        }

        public int getLevel(byte[] rom, int i) {
            if (levelOffsets.length <= i) {
                return 1;
            }
            return rom[levelOffsets[i]];
        }

        public void setLevel(byte[] rom, int level, int i) {
            rom[levelOffsets[i]] = (byte) level;
        }

    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        List<StaticEncounter> statics = new ArrayList<>();
        if (romEntry.getIntValue("StaticPokemonSupport") > 0) {
            for (StaticPokemon sp : romEntry.getStaticPokemon()) {
                StaticEncounter se = new StaticEncounter();
                se.setSpecies(sp.getPokemon(this));
                se.setLevel(sp.getLevel(rom, 0));
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
        for (int i = 0; i < romEntry.getStaticPokemon().size(); i++) {
            StaticEncounter se = staticPokemon.get(i);
            StaticPokemon sp = romEntry.getStaticPokemon().get(i);
            sp.setPokemon(this, se.getSpecies());
            sp.setLevel(rom, se.getLevel(), 0);
        }

        return true;
    }

    @Override
    public boolean hasStaticAltFormes() {
        return false;
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

    @Override
    public List<Integer> getTMMoves() {
        List<Integer> tms = new ArrayList<>();
        int offset = romEntry.getIntValue("TMMovesOffset");
        for (int i = 1; i <= Gen1Constants.tmCount; i++) {
            tms.add(moveRomToNumTable[rom[offset + (i - 1)] & 0xFF]);
        }
        return tms;
    }

    @Override
    public List<Integer> getHMMoves() {
        List<Integer> hms = new ArrayList<>();
        int offset = romEntry.getIntValue("TMMovesOffset");
        for (int i = 1; i <= Gen1Constants.hmCount; i++) {
            hms.add(moveRomToNumTable[rom[offset + Gen1Constants.tmCount + (i - 1)] & 0xFF]);
        }
        return hms;
    }

    @Override
    public void setTMMoves(List<Integer> moveIndexes) {
        int offset = romEntry.getIntValue("TMMovesOffset");
        for (int i = 1; i <= Gen1Constants.tmCount; i++) {
            writeByte(offset + (i - 1), (byte) moveNumToRomTable[moveIndexes.get(i - 1)]);
        }

        // Gym Leader TM Moves (RB only)
        if (!romEntry.isYellow()) {
            int[] tms = Gen1Constants.gymLeaderTMs;
            int glMovesOffset = romEntry.getIntValue("GymLeaderMovesTableOffset");
            for (int i = 0; i < tms.length; i++) {
                // Set the special move used by gym (i+1) to
                // the move we just wrote to TM tms[i]
                writeByte(glMovesOffset + i * 2, (byte) moveNumToRomTable[moveIndexes.get(tms[i] - 1)]);
            }
        }

        // TM Text
        String[] moveNames = readMoveNames();
        for (GBCTMTextEntry tte : romEntry.getTMTexts()) {
            String moveName = moveNames[moveNumToRomTable[moveIndexes.get(tte.getNumber() - 1)]];
            String text = tte.getTemplate().replace("%m", moveName);
            writeVariableLengthString(text, tte.getOffset(), true);
        }
    }

    @Override
    public int getTMCount() {
        return Gen1Constants.tmCount;
    }

    @Override
    public int getHMCount() {
        return Gen1Constants.hmCount;
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        Map<Species, boolean[]> compat = new TreeMap<>();
        int pokeStatsOffset = romEntry.getIntValue("PokemonStatsOffset");
        for (int i = 1; i <= pokedexCount; i++) {
            int baseStatsOffset = (romEntry.isYellow() || i != SpeciesIDs.mew) ? (pokeStatsOffset + (i - 1)
                    * Gen1Constants.baseStatsEntrySize) : romEntry.getIntValue("MewStatsOffset");
            Species pkmn = pokes[i];
            boolean[] flags = new boolean[Gen1Constants.tmCount + Gen1Constants.hmCount + 1];
            for (int j = 0; j < 7; j++) {
                readByteIntoFlags(flags, j * 8 + 1, baseStatsOffset + Gen1Constants.bsTMHMCompatOffset + j);
            }
            compat.put(pkmn, flags);
        }
        return compat;
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
        int pokeStatsOffset = romEntry.getIntValue("PokemonStatsOffset");
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int baseStatsOffset = (romEntry.isYellow() || pkmn.getNumber() != SpeciesIDs.mew) ? (pokeStatsOffset + (pkmn.getNumber() - 1)
                    * Gen1Constants.baseStatsEntrySize)
                    : romEntry.getIntValue("MewStatsOffset");
            for (int j = 0; j < 7; j++) {
                writeByte(baseStatsOffset + Gen1Constants.bsTMHMCompatOffset + j,
                        getByteFromFlags(flags, j * 8 + 1));
            }
        }
    }

    @Override
    public boolean hasMoveTutors() {
        return false;
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        return new ArrayList<>();
    }

    @Override
    public void setMoveTutorMoves(List<Integer> moves) {
        // Do nothing
    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        return new TreeMap<>();
    }

    @Override
    public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {
        // Do nothing
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
        if (found.size() == 0) {
            return -1; // not found
        } else if (found.size() > 1) {
            return -2; // not unique
        } else {
            return found.get(0);
        }
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

        int pkmnCount = romEntry.getIntValue("InternalPokemonCount");
        for (int i = 1; i <= pkmnCount; i++) {
            int pointer = readPointer(pointersOffset + (i - 1) * 2);
            if (pokeRBYToNumTable[i] != 0) {
                int thisPoke = pokeRBYToNumTable[i];
                Species pkmn = pokes[thisPoke];
                while (rom[pointer] != 0) {
                    int method = rom[pointer];
                    EvolutionType type = Gen1Constants.evolutionTypeFromIndex(method);
                    int otherPoke = pokeRBYToNumTable[rom[pointer + 2 + (type == EvolutionType.STONE ? 1 : 0)] & 0xFF];
                    int extraInfo = rom[pointer + 1] & 0xFF;
                    Evolution evo = new Evolution(pkmn, pokes[otherPoke], type, extraInfo);
                    if (!pkmn.getEvolutionsFrom().contains(evo)) {
                        pkmn.getEvolutionsFrom().add(evo);
                        if (pokes[otherPoke] != null) {
                            pokes[otherPoke].getEvolutionsTo().add(evo);
                        }
                    }
                    pointer += (type == EvolutionType.STONE ? 4 : 3);
                }
            }
        }
    }

    @Override
    public void removeImpossibleEvolutions(Settings settings) {
        // Gen 1: only regular trade evos
        // change them all to evolve at level 37
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                for (Evolution evo : pkmn.getEvolutionsFrom()) {
                    if (evo.getType() == EvolutionType.TRADE) {
                        // change
                        evo.setType(EvolutionType.LEVEL);
                        evo.setExtraInfo(37);
                        addEvoUpdateLevel(impossibleEvolutionUpdates,evo);
                    }
                }
            }
        }
    }

    @Override
    public void makeEvolutionsEasier(Settings settings) {
        // No such thing
    }

    @Override
    public void removeTimeBasedEvolutions() {
        // No such thing
    }

    @Override
    public boolean hasShopSupport() {
        return false;
    }

    @Override
    public Map<Integer, Shop> getShopItems() {
        return null; // Not implemented
    }

    @Override
    public void setShopItems(Map<Integer, Shop> shopItems) {
        // Not implemented
    }

    @Override
    public void setBalancedShopPrices() {
        // Not implemented
    }

    /**
     * Similar to {@link #getTrainerClassNames()}, but has the following differences:
     * <ul>
     * <li>This only reads the actual trainer class name list, while {@code getTrainerClassNames()} also reads the copy
     * "only used for trainers' defeat speeches" (according to pokered). The names in the copy are shortened in the
     * Japanese but redundant in all (?) other versions.</li>
     * <li>This doesn't filter out the "individual" class names of bosses (e.g. MISTY, BROCK, LANCE).</li>
     * </ul>
     */
    private List<String> getTrainerClassesForText() {
        int[] offsets = romEntry.getArrayValue("TrainerClassNamesOffsets");
        List<String> tcNames = new ArrayList<>();
        int offset = offsets[offsets.length - 1];
        for (int j = 0; j < Gen1Constants.tclassesCounts[1]; j++) {
            String name = readVariableLengthString(offset, false);
            offset += lengthOfStringAt(offset, false);
            tcNames.add(name);
        }
        return tcNames;
    }

    @Override
    public boolean canChangeTrainerText() {
        return romEntry.getIntValue("CanChangeTrainerText") > 0;
    }

    @Override
    public List<Integer> getDoublesTrainerClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getTrainerNames() {
        int[] offsets = romEntry.getArrayValue("TrainerClassNamesOffsets");
        List<String> trainerNames = new ArrayList<>();
        int offset = offsets[offsets.length - 1];
        for (int j = 0; j < Gen1Constants.tclassesCounts[1]; j++) {
            String name = readVariableLengthString(offset, false);
            offset += lengthOfStringAt(offset, false);
            if (Gen1Constants.singularTrainers.contains(j)) {
                trainerNames.add(name);
            }
        }
        return trainerNames;
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {
        if (romEntry.getIntValue("CanChangeTrainerText") > 0) {
            int[] offsets = romEntry.getArrayValue("TrainerClassNamesOffsets");
            Iterator<String> trainerNamesI = trainerNames.iterator();
            int offset = offsets[offsets.length - 1];
            for (int j = 0; j < Gen1Constants.tclassesCounts[1]; j++) {
                int oldLength = lengthOfStringAt(offset, false);
                if (Gen1Constants.singularTrainers.contains(j)) {
                    String newName = trainerNamesI.next();
                    writeFixedLengthString(newName, offset, oldLength);
                }
                offset += oldLength;
            }
        }
    }

    @Override
    public TrainerNameMode trainerNameMode() {
        return TrainerNameMode.SAME_LENGTH;
    }

    @Override
    public List<Integer> getTCNameLengthsByTrainer() {
        // not needed
        return new ArrayList<>();
    }

    @Override
    public List<String> getTrainerClassNames() {
        int[] offsets = romEntry.getArrayValue("TrainerClassNamesOffsets");
        List<String> trainerClassNames = new ArrayList<>();
        if (offsets.length == 2) {
            for (int i = 0; i < offsets.length; i++) {
                int offset = offsets[i];
                for (int j = 0; j < Gen1Constants.tclassesCounts[i]; j++) {
                    String name = readVariableLengthString(offset, false);
                    offset += lengthOfStringAt(offset, false);
                    if (i == 0 || !Gen1Constants.singularTrainers.contains(j)) {
                        trainerClassNames.add(name);
                    }
                }
            }
        } else {
            int offset = offsets[0];
            for (int j = 0; j < Gen1Constants.tclassesCounts[1]; j++) {
                String name = readVariableLengthString(offset, false);
                offset += lengthOfStringAt(offset, false);
                if (!Gen1Constants.singularTrainers.contains(j)) {
                    trainerClassNames.add(name);
                }
            }
        }
        return trainerClassNames;
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {
        if (romEntry.getIntValue("CanChangeTrainerText") > 0) {
            int[] offsets = romEntry.getArrayValue("TrainerClassNamesOffsets");
            Iterator<String> tcNamesIter = trainerClassNames.iterator();
            if (offsets.length == 2) {
                for (int i = 0; i < offsets.length; i++) {
                    int offset = offsets[i];
                    for (int j = 0; j < Gen1Constants.tclassesCounts[i]; j++) {
                        int oldLength = lengthOfStringAt(offset, false);
                        if (i == 0 || !Gen1Constants.singularTrainers.contains(j)) {
                            String newName = tcNamesIter.next();
                            writeFixedLengthString(newName, offset, oldLength);
                        }
                        offset += oldLength;
                    }
                }
            } else {
                int offset = offsets[0];
                for (int j = 0; j < Gen1Constants.tclassesCounts[1]; j++) {
                    int oldLength = lengthOfStringAt(offset, false);
                    if (!Gen1Constants.singularTrainers.contains(j)) {
                        String newName = tcNamesIter.next();
                        writeFixedLengthString(newName, offset, oldLength);
                    }
                    offset += oldLength;
                }
            }
        }

    }

    @Override
    public boolean fixedTrainerClassNamesLength() {
        return true;
    }

    @Override
    public String getDefaultExtension() {
        return "gbc";
    }

    @Override
    public int abilitiesPerPokemon() {
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

        if (romEntry.getTweakFile("BWXPTweak") != null) {
            available |= MiscTweak.BW_EXP_PATCH.getValue();
        }
        if (romEntry.getTweakFile("XAccNerfTweak") != null) {
            available |= MiscTweak.NERF_X_ACCURACY.getValue();
        }
        if (romEntry.getTweakFile("CritRateTweak") != null) {
            available |= MiscTweak.FIX_CRIT_RATE.getValue();
        }
        if (romEntry.getIntValue("TextDelayFunctionOffset") != 0) {
            available |= MiscTweak.FASTEST_TEXT.getValue();
        }
        if (romEntry.getIntValue("PCPotionOffset") != 0) {
            available |= MiscTweak.RANDOMIZE_PC_POTION.getValue();
        }
        if (romEntry.getIntValue("PikachuEvoJumpOffset") != 0) {
            available |= MiscTweak.ALLOW_PIKACHU_EVOLUTION.getValue();
        }
        if (romEntry.getIntValue("CatchingTutorialMonOffset") != 0) {
            available |= MiscTweak.RANDOMIZE_CATCHING_TUTORIAL.getValue();
        }
        if (romEntry.getIntValue("TMMovesReusableFunctionOffset") != 0) {
            available |= MiscTweak.REUSABLE_TMS.getValue();
        }
        return available;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        if (tweak == MiscTweak.BW_EXP_PATCH) {
            applyBWEXPPatch();
        } else if (tweak == MiscTweak.NERF_X_ACCURACY) {
            applyXAccNerfPatch();
        } else if (tweak == MiscTweak.FIX_CRIT_RATE) {
            applyCritRatePatch();
        } else if (tweak == MiscTweak.FASTEST_TEXT) {
            applyFastestTextPatch();
        } else if (tweak == MiscTweak.ALLOW_PIKACHU_EVOLUTION) {
            applyPikachuEvoPatch();
        } else if (tweak == MiscTweak.LOWER_CASE_POKEMON_NAMES) {
            applyCamelCaseNames();
        } else if (tweak == MiscTweak.REUSABLE_TMS) {
            applyReusableTMsPatch();
        }
    }

    private void applyBWEXPPatch() {
        genericIPSPatch("BWXPTweak");
    }

    private void applyXAccNerfPatch() {
        xAccNerfed = genericIPSPatch("XAccNerfTweak");
    }

    private void applyCritRatePatch() {
        genericIPSPatch("CritRateTweak");
    }

    private void applyFastestTextPatch() {
        if (romEntry.getIntValue("TextDelayFunctionOffset") != 0) {
            writeByte(romEntry.getIntValue("TextDelayFunctionOffset"), GBConstants.gbZ80Ret);
        }
    }

    private void applyPikachuEvoPatch() {
        if (romEntry.getIntValue("PikachuEvoJumpOffset") != 0) {
            writeByte(romEntry.getIntValue("PikachuEvoJumpOffset"), GBConstants.gbZ80JumpRelative);
        }
    }

    private void applyReusableTMsPatch() {
        // Changes a conditional "Ret C" to an unconditional "Ret",
        // and thus skips the consumption/removal of the TM.
        int offset = romEntry.getIntValue("TMMovesReusableFunctionOffset");
        if (offset == 0) {
            return;
        }
        if (rom[offset] != GBConstants.gbZ80RetC) {
            throw new RuntimeException("Unexpected byte found for the ROM's TM teaching function, " +
                    "likely ROM entry value \"TMMovesReusableFunctionOffset\" is faulty.");
        }
        writeByte(offset, GBConstants.gbZ80Ret);
    }

    @Override
    public void enableGuaranteedPokemonCatching() {
        int offset = find(rom, Gen1Constants.guaranteedCatchPrefix);
        if (offset > 0) {
            offset += Gen1Constants.guaranteedCatchPrefix.length() / 2; // because it was a prefix

            // The game ensures that the Master Ball always catches a Pokemon by running the following code:
            // ; Get the item ID.
            //  ld hl, wcf91
            //  ld a, [hl]
            //
            // ; The Master Ball always succeeds.
            //  cp MASTER_BALL
            //  jp z, .captured
            // By making the jump here unconditional, we can ensure that catching always succeeds no
            // matter the ball type. We check that the original condition is present just for safety.
            if (rom[offset] == (byte)0xCA) {
                writeByte(offset, (byte) 0xC3);
            }
        }
    }

    private boolean genericIPSPatch(String ctName) {
        String patchName = romEntry.getTweakFile(ctName);
        if (patchName == null) {
            return false;
        }

        try {
            FileFunctions.applyPatch(rom, patchName);
            return true;
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        if (romEntry.getIntValue("CatchingTutorialMonOffset") != 0) {
            writeByte(romEntry.getIntValue("CatchingTutorialMonOffset"),
                    (byte) pokeNumToRBYTable[opponent.getNumber()]);
        }
        return true;
    }

    @Override
    public void setPCPotionItem(int itemID) {
        if (romEntry.getIntValue("PCPotionOffset") != 0) {
            if (!getAllowedItems().getItemSet().contains(itemID)) {
                String itemName = itemID >= items.size() ? "unknown item, ID=" + itemID : items.get(itemID).getName();
                throw new IllegalArgumentException("item not allowed for PC Potion: " + itemName);
            }
            writeByte(romEntry.getIntValue("PCPotionOffset"),
                    (byte) itemID);
        }
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        // Sonicboom & drage & OHKO moves
        // 160 add spore
        // also remove OHKO if xacc nerfed
        if (xAccNerfed) {
            return Gen1Constants.bannedMovesWithXAccBanned;
        } else {
            return Gen1Constants.bannedMovesWithoutXAccBanned;
        }
    }

    @Override
    public List<Integer> getFieldMoves() {
        // cut, fly, surf, strength, flash,
        // dig, teleport (NOT softboiled)
        return Gen1Constants.fieldMoves;
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        // just cut
        return Gen1Constants.earlyRequiredHMs;
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        // 160 add yellow intro random // TODO: is this a strange todo telling random intro pokes don't work in yellow?
        int introPokemon = pokeNumToRBYTable[pk.getNumber()];
        writeByte(romEntry.getIntValue("IntroPokemonOffset"), (byte) introPokemon);
        writeByte(romEntry.getIntValue("IntroCryOffset"), (byte) introPokemon);
        return true;
    }

    @Override
    public ItemList getAllowedItems() {
        return Gen1Constants.allowedItems;
    }

    @Override
    public ItemList getNonBadItems() {
        // Gen 1 has no bad items Kappa
        return Gen1Constants.allowedItems;
    }

    @Override
    public Set<Item> getUniqueNoSellItems() {
        return new HashSet<>();
    }

    @Override
    public Set<Item> getRegularShopItems() {
        return null; // Not implemented
    }

    @Override
    public Set<Item> getOPShopItems() {
        return null; // Not implemented
    }

    @Override
    public void loadItems() {
        String[] names = readItemNames();
        items = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            items.add(new Item(i, names[i]));
        }
    }

    public String[] readItemNames() {
        String[] itemNames = new String[256];
        itemNames[0] = "glitch";
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
        // hms override
        for (int index = Gen1Constants.hmsStartIndex; index < Gen1Constants.tmsStartIndex; index++) {
            itemNames[index] = String.format("HM%02d", index - Gen1Constants.hmsStartIndex + 1);
        }
        // tms override
        for (int index = Gen1Constants.tmsStartIndex; index < 0x100; index++) {
            itemNames[index] = String.format("TM%02d", index - Gen1Constants.tmsStartIndex + 1);
        }
        return itemNames;
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    private static class SubMap {
        private int id;
        private int addr;
        private int bank;
        private MapHeader header;
        private Connection[] cons;
        private int n_cons;
        private int obj_addr;
        private List<Integer> itemOffsets;
    }

    private static class MapHeader {
        private int tileset_id; // u8
        private int map_h, map_w; // u8
        private int map_ptr, text_ptr, script_ptr; // u16
        private int connect_byte; // u8
        // 10 bytes
    }

    private static class Connection {
        private int index; // u8
        private int connected_map; // u16
        private int current_map; // u16
        private int bigness; // u8
        private int map_width; // u8
        private int y_align; // u8
        private int x_align; // u8
        private int window; // u16
        // 11 bytes
    }

    private void preloadMaps() {
        int mapBanks = romEntry.getIntValue("MapBanks");
        int mapAddresses = romEntry.getIntValue("MapAddresses");

        preloadMap(mapBanks, mapAddresses, 0);
    }

    private void preloadMap(int mapBanks, int mapAddresses, int mapID) {

        if (maps[mapID] != null || mapID == 0xED || mapID == 0xFF) {
            return;
        }

        SubMap map = new SubMap();
        maps[mapID] = map;

        map.id = mapID;
        map.addr = readPointer(mapAddresses + mapID * 2, rom[mapBanks + mapID] & 0xFF);
        map.bank = bankOf(map.addr);

        map.header = new MapHeader();
        map.header.tileset_id = rom[map.addr] & 0xFF;
        map.header.map_h = rom[map.addr + 1] & 0xFF;
        map.header.map_w = rom[map.addr + 2] & 0xFF;
        map.header.map_ptr = readPointer(map.addr + 3, map.bank);
        map.header.text_ptr = readPointer(map.addr + 5, map.bank);
        map.header.script_ptr = readPointer(map.addr + 7, map.bank);
        map.header.connect_byte = rom[map.addr + 9] & 0xFF;

        int cb = map.header.connect_byte;
        map.n_cons = ((cb & 8) >> 3) + ((cb & 4) >> 2) + ((cb & 2) >> 1) + (cb & 1);

        int cons_offset = map.addr + 10;

        map.cons = new Connection[map.n_cons];
        for (int i = 0; i < map.n_cons; i++) {
            int tcon_offs = cons_offset + i * 11;
            Connection con = new Connection();
            con.index = rom[tcon_offs] & 0xFF;
            con.connected_map = readWord(tcon_offs + 1);
            con.current_map = readWord(tcon_offs + 3);
            con.bigness = rom[tcon_offs + 5] & 0xFF;
            con.map_width = rom[tcon_offs + 6] & 0xFF;
            con.y_align = rom[tcon_offs + 7] & 0xFF;
            con.x_align = rom[tcon_offs + 8] & 0xFF;
            con.window = readWord(tcon_offs + 9);
            map.cons[i] = con;
            preloadMap(mapBanks, mapAddresses, con.index);
        }
        map.obj_addr = readPointer(cons_offset + map.n_cons * 11, map.bank);

        // Read objects
        // +0 is the border tile (ignore)
        // +1 is warp count

        int n_warps = rom[map.obj_addr + 1] & 0xFF;
        int offs = map.obj_addr + 2;
        for (int i = 0; i < n_warps; i++) {
            // track this warp
            int to_map = rom[offs + 3] & 0xFF;
            preloadMap(mapBanks, mapAddresses, to_map);
            offs += 4;
        }

        // Now we're pointing to sign count
        int n_signs = rom[offs++] & 0xFF;
        offs += n_signs * 3;

        // Finally, entities, which contain the items
        map.itemOffsets = new ArrayList<>();
        int n_entities = rom[offs++] & 0xFF;
        for (int i = 0; i < n_entities; i++) {
            // Read text ID
            int tid = rom[offs + 5] & 0xFF;
            if ((tid & (1 << 6)) > 0) {
                // trainer
                offs += 8;
            } else if ((tid & (1 << 7)) > 0 && (rom[offs + 6] != 0x00)) {
                // item
                map.itemOffsets.add(offs + 6);
                offs += 7;
            } else {
                // generic
                offs += 6;
            }
        }
    }

    private void loadMapNames() {
        mapNames = new String[256];
        int mapNameTableOffset = romEntry.getIntValue("MapNameTableOffset");
        // external names
        List<Integer> usedExternal = new ArrayList<>();
        for (int i = 0; i < 0x25; i++) {
            int externalOffset = readPointer(mapNameTableOffset + 1);
            usedExternal.add(externalOffset);
            mapNames[i] = readVariableLengthString(externalOffset, false);
            mapNameTableOffset += 3;
        }

        // internal names
        int lastMaxMap = 0x25;
        Map<Integer, Integer> previousMapCounts = new HashMap<>();
        while ((rom[mapNameTableOffset] & 0xFF) != 0xFF) {
            int maxMap = rom[mapNameTableOffset] & 0xFF;
            int nameOffset = readPointer(mapNameTableOffset + 2);
            String actualName = readVariableLengthString(nameOffset, false).trim();
            if (usedExternal.contains(nameOffset)) {
                for (int i = lastMaxMap; i < maxMap; i++) {
                    if (maps[i] != null) {
                        mapNames[i] = actualName + " (Building)";
                    }
                }
            } else {
                int mapCount = 0;
                if (previousMapCounts.containsKey(nameOffset)) {
                    mapCount = previousMapCounts.get(nameOffset);
                }
                for (int i = lastMaxMap; i < maxMap; i++) {
                    if (maps[i] != null) {
                        mapCount++;
                        mapNames[i] = actualName + " (" + mapCount + ")";
                    }
                }
                previousMapCounts.put(nameOffset, mapCount);
            }
            lastMaxMap = maxMap;
            mapNameTableOffset += 4;
        }
    }

    private List<Integer> getItemOffsets() {

        List<Integer> itemOffs = new ArrayList<>();

        for (SubMap map : maps) {
            if (map != null) {
                itemOffs.addAll(map.itemOffsets);
            }
        }

        int hiRoutine = romEntry.getIntValue("HiddenItemRoutine");
        int spclTable = romEntry.getIntValue("SpecialMapPointerTable");

        if (!isYellow()) {

            int lOffs = romEntry.getIntValue("SpecialMapList");
            int idx = 0;

            while ((rom[lOffs] & 0xFF) != 0xFF) {

                int spclOffset = readPointer(spclTable + idx);

                while ((rom[spclOffset] & 0xFF) != 0xFF) {
                    if (readPointer(spclOffset + 4, rom[spclOffset + 3] & 0xFF) == hiRoutine) {
                        itemOffs.add(spclOffset + 2);
                    }
                    spclOffset += 6;
                }
                lOffs++;
                idx += 2;
            }
        } else {

            int lOffs = spclTable;

            while ((rom[lOffs] & 0xFF) != 0xFF) {

                int spclOffset = readPointer(lOffs + 1);

                while ((rom[spclOffset] & 0xFF) != 0xFF) {
                    if (readPointer(readWord(spclOffset + 4), rom[spclOffset + 3] & 0xFF) == hiRoutine) {
                        itemOffs.add(spclOffset + 2);
                    }
                    spclOffset += 6;
                }
                lOffs += 3;
            }
        }

        return itemOffs;
    }

    @Override
    public List<Integer> getRequiredFieldTMs() {
        return Gen1Constants.requiredFieldTMs;
    }

    @Override
    public List<Integer> getCurrentFieldTMs() {
        List<Integer> itemOffsets = getItemOffsets();
        List<Integer> fieldTMs = new ArrayList<>();

        for (int offset : itemOffsets) {
            int itemHere = rom[offset] & 0xFF;
            if (Gen1Constants.allowedItems.isTM(itemHere)) {
                fieldTMs.add(itemHere - Gen1Constants.tmsStartIndex + 1); // TM
                                                                          // offset
            }
        }
        return fieldTMs;
    }

    @Override
    public void setFieldTMs(List<Integer> fieldTMs) {
        List<Integer> itemOffsets = getItemOffsets();
        Iterator<Integer> iterTMs = fieldTMs.iterator();

        for (int offset : itemOffsets) {
            int itemHere = rom[offset] & 0xFF;
            if (Gen1Constants.allowedItems.isTM(itemHere)) {
                // Replace this with a TM from the list
                writeByte(offset, (byte) (iterTMs.next() + Gen1Constants.tmsStartIndex - 1));
            }
        }
    }

    @Override
    public List<Item> getRegularFieldItems() {
        List<Integer> itemOffsets = getItemOffsets();
        List<Item> allItems = getItems();
        List<Item> fieldItems = new ArrayList<>();

        for (int offset : itemOffsets) {
            int itemHere = rom[offset] & 0xFF;
            if (Gen1Constants.allowedItems.isAllowed(itemHere) && !(Gen1Constants.allowedItems.isTM(itemHere))) {
                fieldItems.add(allItems.get(itemHere));
            }
        }
        return fieldItems;
    }

    @Override
    public void setRegularFieldItems(List<Item> items) {
        List<Integer> itemOffsets = getItemOffsets();
        Iterator<Item> iterItems = items.iterator();

        for (int offset : itemOffsets) {
            int itemHere = rom[offset] & 0xFF;
            if (Gen1Constants.allowedItems.isAllowed(itemHere) && !(Gen1Constants.allowedItems.isTM(itemHere))) {
                // Replace it
                writeByte(offset, (byte) (iterItems.next().getId()));
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
        int[] unused = romEntry.getArrayValue("TradesUnused");
        int unusedOffset = 0;
        int entryLength = nicknameLength + 3;

        for (int entry = 0; entry < tableSize; entry++) {
            if (unusedOffset < unused.length && unused[unusedOffset] == entry) {
                unusedOffset++;
                continue;
            }
            IngameTrade trade = new IngameTrade();
            int entryOffset = tableOffset + entry * entryLength;
            trade.requestedSpecies = pokes[pokeRBYToNumTable[rom[entryOffset] & 0xFF]];
            trade.givenSpecies = pokes[pokeRBYToNumTable[rom[entryOffset + 1] & 0xFF]];
            trade.nickname = readString(entryOffset + 3, nicknameLength, false);
            trades.add(trade);
        }

        return trades;
    }

    @Override
    public void setIngameTrades(List<IngameTrade> trades) {

        // info
        int tableOffset = romEntry.getIntValue("TradeTableOffset");
        int tableSize = romEntry.getIntValue("TradeTableSize");
        int nicknameLength = romEntry.getIntValue("TradeNameLength");
        int[] unused = romEntry.getArrayValue("TradesUnused");
        int unusedOffset = 0;
        int entryLength = nicknameLength + 3;
        int tradeOffset = 0;

        for (int entry = 0; entry < tableSize; entry++) {
            if (unusedOffset < unused.length && unused[unusedOffset] == entry) {
                unusedOffset++;
                continue;
            }
            IngameTrade trade = trades.get(tradeOffset++);
            int entryOffset = tableOffset + entry * entryLength;
            writeBytes(entryOffset, new byte[]{
                    (byte) pokeNumToRBYTable[trade.requestedSpecies.getNumber()],
                    (byte) pokeNumToRBYTable[trade.givenSpecies.getNumber()]});
            if (romEntry.getIntValue("CanChangeTrainerText") > 0) {
                writeFixedLengthString(trade.nickname, entryOffset + 3, nicknameLength);
            }
        }
    }

    @Override
    public boolean hasDVs() {
        return true;
    }

    @Override
    public int generationOfPokemon() {
        return 1;
    }

    @Override
    public void removeEvosForPokemonPool() {
        // gen1 doesn't have this functionality anyway
    }

    @Override
    public boolean supportsFourStartingMoves() {
        return true;
    }

    private void saveEvosAndMovesLearnt() {
        saveLevel1Moves();

        int pointerTableOffset = romEntry.getIntValue("PokemonMovesetsTableOffset");

        int pokemonCount = romEntry.getIntValue("InternalPokemonCount");
        for (int i = 1; i <= pokemonCount; i++) {
            Species pk = pokes[pokeRBYToNumTable[i]];
            int pointerOffset = pointerTableOffset + (i - 1) * 2;
            new GBCDataRewriter<Species>().rewriteData(pointerOffset, pk, this::pokemonToEvosAndMovesLearntBytes,
                    oldDataOffset -> lengthOfDataWithTerminatorsAt(oldDataOffset,
                            GBConstants.evosAndMovesTerminator, 2));
        }
    }

    private void saveLevel1Moves() {
        int pokeStatsOffset = romEntry.getIntValue("PokemonStatsOffset");
        for (Species pk : speciesList) {
            if (pk == null) continue;
            int statsOffset;
            if (pk.getNumber() == SpeciesIDs.mew && !romEntry.isYellow()) {
                statsOffset = romEntry.getIntValue("MewStatsOffset");
            } else {
                statsOffset = (pk.getNumber() - 1) * Gen1Constants.baseStatsEntrySize + pokeStatsOffset;
            }
            List<MoveLearnt> moveset = movesets.get(pk.getNumber());
            byte[] level1MoveBytes = movesetToLevel1MoveBytes(moveset);
            writeBytes(statsOffset + Gen1Constants.bsLevel1MovesOffset, level1MoveBytes);
        }
    }

    private byte[] movesetToLevel1MoveBytes(List<MoveLearnt> moveset) {
        byte[] level1MoveBytes = new byte[4];
        for (int i = 0; i < Math.min(4, moveset.size()); i++) {
            MoveLearnt ml = moveset.get(i);
            if (ml.level == 1) {
                level1MoveBytes[i] = (byte) moveNumToRomTable[ml.move];
            }
        }
        return level1MoveBytes;
    }

    private byte[] pokemonToEvosAndMovesLearntBytes(Species pk) {
        if (pk == null) {
            return new byte[] {0x00, 0x00};
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Evolution evo : pk.getEvolutionsFrom()) {
            byte[] evoBytes = evolutionToBytes(evo);
            baos.write(evoBytes, 0, evoBytes.length);
        }
        baos.write(GBConstants.evosAndMovesTerminator);
        List<MoveLearnt> moveset = movesets.get(pk.getNumber());
        for (int i = 0; i < moveset.size(); i++) {
            MoveLearnt ml = moveset.get(i);
            if (i <= 4 && ml.level == 1) continue;
            byte[] mlBytes = moveLearntToBytes(ml);
            baos.write(mlBytes, 0, mlBytes.length);
        }
        baos.write(GBConstants.evosAndMovesTerminator);
        return baos.toByteArray();
    }

    private byte[] evolutionToBytes(Evolution evo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(Gen1Constants.evolutionTypeToIndex(evo.getType()));
        byte[] extraInfoBytes = evoTypeExtraInfoToBytes(evo);
        baos.write(extraInfoBytes, 0, extraInfoBytes.length);
        baos.write(pokeNumToRBYTable[evo.getTo().getNumber()]);
        return baos.toByteArray();
    }

    private byte[] evoTypeExtraInfoToBytes(Evolution evo) {
        switch (evo.getType()) {
            case LEVEL:
                return new byte[]{(byte) evo.getExtraInfo()};
            case STONE:
                return new byte[]{(byte) evo.getExtraInfo(), 0x01}; // minimum level
            case TRADE:
                return new byte[]{(byte) 0x01}; // minimum level
            default:
                throw new IllegalStateException("EvolutionType " + evo.getType() + " is not supported " +
                        "by Gen 1 games.");
        }
    }

    private byte[] moveLearntToBytes(MoveLearnt ml) {
        return new byte[] {(byte) ml.level, (byte) moveNumToRomTable[ml.move]};
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        // The JP games probably can't get support due to extreme space issues.
        // They would need a more powerful space handling system (than DataRewriter) or even ROM expansion.
        return romEntry.isNonJapanese();
    }

    @Override
    public void setCustomPlayerGraphics(GraphicsPack unchecked, Settings.PlayerCharacterMod toReplace) {
        if (toReplace != Settings.PlayerCharacterMod.PC1) {
            throw new IllegalArgumentException("Invalid toReplace. Only one player character in Gen 1.");
        }
        if (!(unchecked instanceof GBCPlayerCharacterGraphics)) {
            throw new IllegalArgumentException("Invalid playerGraphics");
        }
        GBCPlayerCharacterGraphics playerGraphics = (GBCPlayerCharacterGraphics) unchecked;

        if (playerGraphics.hasFrontImage()) {
            rewritePlayerFrontImage(playerGraphics.getFrontImage());
        }
        if (playerGraphics.hasBackImage()) {
            rewritePlayerBackImage(playerGraphics.getBackImage());
        }
        if (playerGraphics.hasWalkSprite()) {
            int walkOffset = romEntry.getIntValue("PlayerWalkSprite");
            writeImage(walkOffset, playerGraphics.getWalkSprite());
        }
        if (playerGraphics.hasBikeSprite()) {
            int bikeOffset = romEntry.getIntValue("PlayerBikeSprite");
            writeImage(bikeOffset, playerGraphics.getBikeSprite());
        }
        if (playerGraphics.hasFishSprite()) {
            int fishOffset = romEntry.getIntValue("PlayerFishSprite");
            writeImage(fishOffset, playerGraphics.getFishSprite());
        }
    }

    private int calculateFrontSpriteBank(Gen1Species pk) {
        int idx = pokeNumToRBYTable[pk.getNumber()];
        int fsBank;
        // define (by index number) the bank that a pokemon's image is in
        // using pokered code
        if (pk.getNumber() == SpeciesIDs.mew && !romEntry.isYellow()) {
            fsBank = 1;
        } else if (idx < 0x1F) {
            fsBank = 0x9;
        } else if (idx < 0x4A) {
            fsBank = 0xA;
        } else if (idx < 0x74 || idx == 0x74 && pk.getFrontImagePointer() > 0x7000) {
            fsBank = 0xB;
        } else if (idx < 0x99 || idx == 0x99 && pk.getFrontImagePointer() > 0x7000) {
            fsBank = 0xC;
        } else {
            fsBank = 0xD;
        }
        return fsBank;
    }

    @Override
    public void loadPokemonPalettes() {
		int palIndex = romEntry.getIntValue("MonPaletteIndicesOffset");
		for (Species pk : getPokemonSet()) {
            // they are in Pokédex order
			Gen1Species gen1pk = (Gen1Species) pk;
			gen1pk.setPaletteID((SGBPaletteID.values()[rom[palIndex + gen1pk.getNumber()]]));
		}
	}

	@Override
	public void savePokemonPalettes() {
		int palIndex = romEntry.getIntValue("MonPaletteIndicesOffset");
		for (Species pk : getPokemonSet()) {
            // they are in Pokédex order
			Gen1Species gen1pk = (Gen1Species) pk;
			writeByte(palIndex + gen1pk.getNumber(), (byte) gen1pk.getPaletteID().ordinal());
		}
	}
    
    private Palette read4ColorPalette(int offset) {
        byte[] paletteBytes = new byte[8];
        System.arraycopy(rom, offset, paletteBytes, 0, 8);
        return new Palette(paletteBytes);
    }

    public void rewritePlayerFrontImage(GBCImage frontImage) {
        int[] pointerOffsets = romEntry.getArrayValue("PlayerFrontImagePointers");
        int primaryPointerOffset = pointerOffsets[0];
        int[] secondaryPointerOffsets = Arrays.copyOfRange(pointerOffsets, 1, pointerOffsets.length);
        int[] bankOffsets = romEntry.getArrayValue("PlayerFrontImageBankOffsets");
        DataRewriter<GBCImage> dataRewriter = new IndirectBankDataRewriter<>(bankOffsets);

        dataRewriter.rewriteData(primaryPointerOffset, frontImage, secondaryPointerOffsets,
                Gen1Cmp::compress, this::lengthOfCompressedDataAt);
    }

    private void rewritePlayerBackImage(GBCImage backImage) {
        int[] pointerOffsets = romEntry.getArrayValue("PlayerBackImagePointers");
        int primaryPointerOffset = pointerOffsets[0];
        int[] secondaryPointerOffsets = Arrays.copyOfRange(pointerOffsets, 1, pointerOffsets.length);
        int[] bankOffsets = romEntry.getArrayValue("PlayerBackImageBankOffsets");
        DataRewriter<GBCImage> dataRewriter = new IndirectBankDataRewriter<>(bankOffsets);

        if (romEntry.isYellow()) {
            dataRewriter.rewriteData(primaryPointerOffset, backImage, secondaryPointerOffsets,
                    this::playerPlusOtherBackImagesToBytes, this::lengthOfPlayerAndOtherBackImagesAt);
            repointOakBackImage(primaryPointerOffset);
        } else {
            dataRewriter.rewriteData(primaryPointerOffset, backImage, secondaryPointerOffsets,
                    this::playerPlusOldManBackImagesToBytes, this::lengthOfPlayerAndOldManBackImagesAt);
        }
        repointOldManBackImage(primaryPointerOffset);
    }

    private byte[] playerPlusOtherBackImagesToBytes(GBCImage playerBack) {
        byte[] playerPlusOldManBackData = playerPlusOldManBackImagesToBytes(playerBack);
        byte[] oakBackData = readCompressedOakBackData();

        byte[] allData = new byte[playerPlusOldManBackData.length + oakBackData.length];
        System.arraycopy(playerPlusOldManBackData, 0, allData, 0, playerPlusOldManBackData.length);
        System.arraycopy(oakBackData, 0, allData, playerPlusOldManBackData.length, oakBackData.length);
        return allData;
    }

    private byte[] playerPlusOldManBackImagesToBytes(GBCImage playerBack) {
        byte[] playerBackData = Gen1Cmp.compress(playerBack);
        byte[] oldManBackData = readCompressedOldManBackData();

        byte[] bothData = new byte[playerBackData.length + oldManBackData.length];
        System.arraycopy(playerBackData, 0, bothData, 0, playerBackData.length);
        System.arraycopy(oldManBackData, 0, bothData, playerBackData.length, oldManBackData.length);
        return bothData;
    }

    private byte[] readCompressedOldManBackData() {
        int[] bankOffsets = romEntry.getArrayValue("PlayerBackImageBankOffsets");
        int bank = rom[bankOffsets[0]];
        int oldManBackOffset = readPointer(romEntry.getIntValue("OldManBackImagePointer"), bank);
        int oldManBackLength = lengthOfCompressedDataAt(oldManBackOffset);
        return Arrays.copyOfRange(rom, oldManBackOffset, oldManBackOffset + oldManBackLength);
    }

    private byte[] readCompressedOakBackData() {
        int[] bankOffsets = romEntry.getArrayValue("PlayerBackImageBankOffsets");
        int bank = rom[bankOffsets[0]];
        int oakBackOffset = readPointer(romEntry.getIntValue("OakBackImagePointer"), bank);
        int oakBackLength = lengthOfCompressedDataAt(oakBackOffset);
        return Arrays.copyOfRange(rom, oakBackOffset, oakBackOffset + oakBackLength);
    }

    /**
     * The length in bytes of the player's compressed back image, followed by the catching tutorial old man's,
     * and then prof. Oak's shown when catching the starter.<br>
     * Assumes they actually follow another in ROM, with no gaps.
     */
    private int lengthOfPlayerAndOtherBackImagesAt(int offset) {
        int length = lengthOfPlayerAndOldManBackImagesAt(offset);
        length += lengthOfCompressedDataAt(offset + length);
        return length;
    }

    /**
     * The length in bytes of the player's compressed back image, followed by the catching tutorial old man's.<br>
     * Assumes they actually follow another in ROM, with no gaps.
     */
    private int lengthOfPlayerAndOldManBackImagesAt(int offset) {
        int length = lengthOfCompressedDataAt(offset);
        length += lengthOfCompressedDataAt(offset + length);
        return length;
    }

    private void repointOakBackImage(int playerBackPointerOffset) {
        int[] bankOffsets = romEntry.getArrayValue("PlayerBackImageBankOffsets");
        int bank = rom[bankOffsets[0]];

        int newOffset = readPointer(playerBackPointerOffset, bank);
        int newOldManOffset = newOffset + lengthOfCompressedDataAt(newOffset);
        int newOakOffset = newOldManOffset + lengthOfCompressedDataAt(newOldManOffset);
        int oakBackPointerOffset = romEntry.getIntValue("OakBackImagePointer");
        writePointer(oakBackPointerOffset, newOakOffset);
    }

    private void repointOldManBackImage(int playerBackPointerOffset) {
        int[] bankOffsets = romEntry.getArrayValue("PlayerBackImageBankOffsets");
        int bank = rom[bankOffsets[0]];

        int newOffset = readPointer(playerBackPointerOffset, bank);
        int newOldManOffset = newOffset + lengthOfCompressedDataAt(newOffset);
        int oldManBackPointerOffset = romEntry.getIntValue("OldManBackImagePointer");
        writePointer(oldManBackPointerOffset, newOldManOffset);
    }

    private int lengthOfCompressedDataAt(int offset) {
        if (offset == 0) {
            throw new IllegalArgumentException("Invalid offset. Compressed data cannot be at offset 0.");
        }
        Gen1Decmp decmp = new Gen1Decmp(rom, offset);
        decmp.decompress();
        return decmp.getCompressedLength();
    }

    @Override
    public Gen1PokemonImageGetter createPokemonImageGetter(Species pk) {
        return new Gen1PokemonImageGetter(pk);
    }

    public class Gen1PokemonImageGetter extends GBPokemonImageGetter {
        private final Gen1Species pk;

        public Gen1PokemonImageGetter(Species pk) {
            super(pk);
            if (!(pk instanceof Gen1Species)) {
                throw new IllegalArgumentException("Argument \"pk\" is not a Gen1Pokemon");
            }
            this.pk = (Gen1Species) pk;
        }

        @Override
        public Gen1PokemonImageGetter setShiny(boolean shiny) {
            throw new UnsupportedOperationException("No shinies in Generation 1");
        }

        @Override
        public Gen1PokemonImageGetter setGraphicalForme(int forme) {
            throw new UnsupportedOperationException("No graphical formes in Generation 1");
        }

        @Override
        public BufferedImage get() {
            int width = back ? 4 : pk.getFrontImageDimensions() & 0x0F;
            int height = back ? 4 : (pk.getFrontImageDimensions() >> 4) & 0x0F;

            int bank = calculateFrontSpriteBank(pk);
            int imageOffset = calculateOffset(back ? pk.getBackImagePointer() : pk.getFrontImagePointer(), bank);
            byte[] data = readImageData(imageOffset);
            Palette palette = getVisiblePokemonPalette(pk);

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

        private byte[] readImageData(int imageOffset) {
            Gen1Decmp image = new Gen1Decmp(rom, imageOffset);
            image.decompress();
            return image.getData();
        }

        private Palette getVisiblePokemonPalette(Gen1Species pk) {
            Palette palette;
            if (romEntry.getIntValue("MonPaletteIndicesOffset") > 0 && romEntry.getIntValue("SGBPalettesOffset") > 0) {
                int palIndex = pk.getPaletteID().ordinal();
                int palOffset = romEntry.getIntValue("SGBPalettesOffset") + palIndex * 8;
                if (romEntry.isYellow() && romEntry.isNonJapanese()) {
                    // Non-japanese Yellow can use GBC palettes instead.
                    // Stored directly after regular SGB palettes.
                    palOffset += 320;
                }
                palette = read4ColorPalette(palOffset);
            } else {
                palette = GBCImage.DEFAULT_PALETTE;
            }
            return palette;
        }
        @Override
        public BufferedImage getFull() {
            setIncludePalette(true);

            BufferedImage frontNormal = get();
            BufferedImage backNormal = setBack(true).get();

            return GFXFunctions.stitchToGrid(new BufferedImage[][] { { frontNormal, backNormal } });
        }
    }

    @Override
    public Gen1RomEntry getRomEntry() {
        return romEntry;
    }

}
