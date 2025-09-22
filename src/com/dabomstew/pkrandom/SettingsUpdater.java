package com.dabomstew.pkrandom;

/*----------------------------------------------------------------------------*/
/*--  SettingsUpdater.java - handles the process of updating a Settings file--*/
/*--                         from an old randomizer version to use the      --*/
/*--                         correct binary format so it can be loaded by   --*/
/*--                         the current version.                           --*/
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

import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.MiscTweak;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.zip.CRC32;

public class SettingsUpdater {

    private byte[] dataBlock;
    private int actualDataLength;

    /**
     * Given a quicksettings config string from an old randomizer version,
     * update it to be compatible with the currently running randomizer version.
     * 
     * @param oldVersion
     *            The Version id used to generate the given string
     * @param configString
     *            The outdated config string
     * @return The updated config string to be applied
     */
    public String update(int oldVersion, String configString) {
        byte[] data = Base64.getDecoder().decode(configString);
        this.dataBlock = new byte[200];
        this.actualDataLength = data.length;
        System.arraycopy(data, 0, this.dataBlock, 0, this.actualDataLength);

        // new field values here are written as bitwise ORs
        // this is slightly slower in execution, but it makes it clearer
        // just what values we actually want to set
        // bit fields 1 2 3 4 5 6 7 8
        // are values 0x01 0x02 0x04 0x08 0x10 0x20 0x40 0x80

        // versions prior to v1.2.0a didn't have quick settings file,
        // they're just included here for completeness' sake

        // versions < v1.0.2a: add abilities set to unchanged
        if (oldVersion < Version.v1_0_2a.id) {
            dataBlock[1] |= 0x10;
        }

        // versions < v1.1.0: add move tutor byte (set both to unchanged)
        if (oldVersion < Version.v1_1_0.id) {
            insertExtraByte(15, (byte) (0x04 | 0x10));
        }

        // v1.1.0-v1.1.1 no change (only added trainer names/classes to preset
        // files, and some checkboxes which it is safe to leave as off)

        // v1.1.1-v1.1.2 no change (another checkbox we leave as off)

        // v1.1.2-v1.2.0a no change (only another checkbox)

        // v1.2.0a-v1.5.0 new features
        if (oldVersion < Version.v1_5_0.id) {
            // trades and field items: both unchanged
            insertExtraByte(16, (byte) (0x40));
            insertExtraByte(17, (byte) (0x04));
            // add a fake checksum for nicknames at the very end of the data,
            // we can leave it at 0
            actualDataLength += 4;
        }

        // v1.5.0-v1.6.0a lots of re-org etc
        if (oldVersion < Version.v1_6_0a.id) {
            // byte 0:
            // copy "update moves" to "update legacy moves"
            // move the other 3 fields after it up one
            int firstByte = dataBlock[0] & 0xFF;
            int updateMoves = firstByte & 0x08;
            int laterFields = firstByte & (0x10 | 0x20 | 0x40);
            dataBlock[0] = (byte) ((firstByte & (0x01 | 0x02 | 0x04 | 0x08)) | (updateMoves << 1) | (laterFields << 1));

            // byte 1:
            // leave as is (don't turn on exp standardization)

            // byte 2:
            // retrieve values of bw exp patch & held items
            // code tweaks keeps the same value as bw exp patch had
            // but turn held items off (it got replaced by pokelimit)
            int hasBWPatch = (dataBlock[2] & 0x08) >> 3;
            int hasHeldItems = (dataBlock[2] & 0x80) >> 7;
            dataBlock[2] &= (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);

            // byte 3:
            // turn on starter held items if held items checkbox was on
            if (hasHeldItems > 0) {
                dataBlock[3] |= 0x10;
            }

            // byte 4-9 are starters
            // byte 10 adds "4 moves" but we leave it off

            // byte 11:
            // pull out value of WP no legendaries
            // replace it with TP no early shedinja
            // also get WP catch rate value
            int wpNoLegendaries = (dataBlock[11] & 0x80) >> 7;
            int tpNoEarlyShedinja = (dataBlock[13] & 0x10) >> 4;
            int wpCatchRate = (dataBlock[13] & 0x08) >> 3;
            dataBlock[11] = (byte) ((dataBlock[11] & (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40)) | (tpNoEarlyShedinja << 7));

            // byte 12 unchanged

            // insert a new byte for "extra" WP stuff
            // include no legendaries & catch rate
            // also include WP held items if overall held items box was on
            // leave similar strength off, there's a bugfix a little later on...
            insertExtraByte(13, (byte) ((wpCatchRate) | (wpNoLegendaries << 1) | (hasHeldItems << 3)));

            // new byte 14 (was 13 in 150):
            // switch off bits 4 and 5 (were for catch rate & no early shedinja)
            dataBlock[14] &= 0x07;

            // the rest of the config bytes are unchanged
            // but we need to add the fields for pokemon limit & code tweaks

            // no pokemon limit
            insertIntField(19, 0);

            // only possible code tweak = bw exp
            insertIntField(23, hasBWPatch);
        }

        // v1.6.0a to v1.6.1: no change
        // the only changes were in implementation, which broke presets, but
        // leaves settings files the same

        // v1.6.1 to v1.6.2:
        // some added fields to tm/move tutors that we can leave blank
        // more crucially: a new general options byte @ offset 3
        // set it to all off by default
        if (oldVersion < Version.v1_6_2.id) {
            insertExtraByte(3, (byte) 0);
        }

        // no significant changes from v1.6.2 to v1.6.3b

        if (oldVersion < Version.v1_7_0b.id) {
            // v1.6.3b to v1.7.0b: add move data/evolution randoms and 2nd TM byte
            insertExtraByte(17, (byte) 0);
            insertExtraByte(21, (byte) 0);
            insertExtraByte(22, (byte) 1);

            // Move some bits from general options to misc tweaks
            int oldTweaks = FileFunctions.readFullIntBigEndian(dataBlock, 27);
            if ((dataBlock[0] & 1) != 0) {
                oldTweaks |= MiscTweak.LOWER_CASE_POKEMON_NAMES.getValue();
            }
            if ((dataBlock[0] & (1 << 1)) != 0) {
                oldTweaks |= MiscTweak.NATIONAL_DEX_AT_START.getValue();
            }
            if ((dataBlock[0] & (1 << 5)) != 0) {
                oldTweaks |= MiscTweak.OLD_UPDATE_TYPE_EFFECTIVENESS.getValue();
            }
            if ((dataBlock[2] & (1 << 5)) != 0) {
                oldTweaks |= MiscTweak.FORCE_CHALLENGE_MODE.getValue();
            }
            FileFunctions.writeFullIntBigEndian(dataBlock, 27, oldTweaks);

            // Now remap the affected bytes
            dataBlock[0] = getRemappedByte(dataBlock[0], new int[] { 2, 3, 4, 6, 7 });
            dataBlock[2] = getRemappedByte(dataBlock[2], new int[] { 0, 1, 2, 4, 6, 7 });
        }

        if (oldVersion < Version.v1_7_1.id) {
            // v1.7.0b to v1.7.1: base stats follow evolutions is now a checkbox
            // so if it's set in the settings file (byte 1 bit 0), turn on the
            // "random" radiobox (byte 1 bit 1)
            if ((dataBlock[1] & 1) != 0) {
                dataBlock[1] |= (1 << 1);
            }

            // shift around stuff to give abilities their own byte.

            // move byte 3 bit 0 to byte 0 bit 5
            // (byte 0 got cleared out by things becoming Tweaks in 170)
            if ((dataBlock[3] & 1) != 0) {
                dataBlock[0] |= (1 << 5);
            }

            // move bits 4-6 from byte 1 to byte 3
            dataBlock[3] = (byte) ((dataBlock[1] & 0x70) >> 4);

            // clean up byte 1 (keep bits 0-3, move bit 7 to 4, clear 5-7)
            dataBlock[1] = (byte) ((dataBlock[1] & 0x0F) | ((dataBlock[1] & 0x80) >> 3));

            // empty byte for fully evolved trainer mon setting
            insertExtraByte(13, (byte) 30);

            // bytes for "good damaging moves" settings
            insertExtraByte(12, (byte) 0);
            insertExtraByte(20, (byte) 0);
            insertExtraByte(22, (byte) 0);
        }
        
        if(oldVersion < Version.v1_7_2.id) {
            // v1.7.1 to v1.7.2: removed separate names files in favor of one unified file
            // so two of the trailing checksums are gone
            actualDataLength -= 8;
            
            // fix wild legendaries
            dataBlock[16] = (byte) (dataBlock[16] ^ (1 << 1));
            
            // add space for the trainer level modifier
            insertExtraByte(35, (byte) 50); // 50 in the settings file = +0% after adjustment
        }

        // TODO: There are seemingly more versions of ZX between Dabomstew's v1.7.2 and ZX v3.0.0,
        //  and some of these (by the release logs https://github.com/Ajarmar/universal-Pokemon-randomizer-zx/releases)
        //  add options. Do really old Settings not get updated properly, due to this?

        if (oldVersion < Version.ZX_3_0_0.id) {
            // wild level modifier
            insertExtraByte(38, (byte) 50);

            // exp curve modifier
            insertExtraByte(39, (byte) 1);
        }

        if (oldVersion < Version.ZX_4_0_0.id) {
            // double battle mode + boss/important extra pokemon
            insertExtraByte(40, (byte) 0);

            // regular extra pokemon + aura mod
            insertExtraByte(41, (byte) 8);

            // Totem/Ally mod + totem items/alt formes
            insertExtraByte(42, (byte) 9);

            // totem level modifier
            insertExtraByte(43, (byte) 50);

            // base stat generation
            insertExtraByte(44, (byte) 0);

            // move generation
            insertExtraByte(45, (byte) 0);
        }

        if (oldVersion < Version.ZX_4_1_0.id) {
            // exp curve
            insertExtraByte(46, (byte) 0);

            // static level modifier
            insertExtraByte(47, (byte) 50);
        }

        if (oldVersion < Version.ZX_4_2_0.id) {
            // This tweak used to be "Randomize Hidden Hollows", which got moved to static Pokemon
            // randomization, so the misc tweak became unused in this version. It eventually *was*
            // used in a future version for something else, but don't get confused by the new name.
            int oldTweaks = FileFunctions.readFullIntBigEndian(dataBlock, 32);
            oldTweaks &= ~MiscTweak.FORCE_CHALLENGE_MODE.getValue();
            FileFunctions.writeFullIntBigEndian(dataBlock, 32, oldTweaks);

            // Trainer Pokemon held items
            insertExtraByte(48, (byte) 0);
        }

        if (oldVersion < Version.ZX_4_3_0.id) {
            // Pickup items
            insertExtraByte(49, (byte) 0);

            // Clear "assoc" state from GenRestrictions as it doesn't exist any longer
            int genRestrictions = FileFunctions.readFullIntBigEndian(dataBlock, 28);
            genRestrictions &= 127;
            FileFunctions.writeFullIntBigEndian(dataBlock, 28, genRestrictions);
        }

        if (oldVersion < Version.ZX_4_5_0.id) {
            // 5-10 custom starters, offset by 1 because of new "Random" option
            int starter1 = FileFunctions.read2ByteInt(dataBlock, 5);
            int starter2 = FileFunctions.read2ByteInt(dataBlock, 7);
            int starter3 = FileFunctions.read2ByteInt(dataBlock, 9);

            starter1 += 1;
            starter2 += 1;
            starter3 += 1;

            FileFunctions.write2ByteInt(dataBlock, 5, starter1);
            FileFunctions.write2ByteInt(dataBlock, 7, starter2);
            FileFunctions.write2ByteInt(dataBlock, 9, starter3);

            // 50 elite four unique pokemon (3 bits)
            insertExtraByte(50, (byte) 0);
        }

        if (oldVersion < Version.ZX_4_6_0.id) {
            // Minimum Catch Rate got moved around to give it more space for Guaranteed Catch.
            // Read the old one, clear it out, then write it to the new location.
            int oldMinimumCatchRate = ((dataBlock[16] & 0x60) >> 5) + 1;
            dataBlock[16] &= ~0x60;
            dataBlock[50] |= (byte) ((oldMinimumCatchRate - 1) << 3);
        }

        if (oldVersion < Version.FVX_0_1_0.id) {
            // The first version of FVX was a merge between two branches with different versions/updaters.
            // Thus, to ensure settings end up the same, they must take the according branching path.
            // Older settings also have to take one of these paths, but which is arbitrary.
            if (isFromCTVVersion(oldVersion)) {
                updateCTV(oldVersion);
            } else {
                updateVBranch(oldVersion);
            }

            // Then there are settings updates which apply regardless of branch:
            // add 3 bytes for starter BST limits
            insertExtraByte(58, (byte) 0);
            insertExtraByte(59, (byte) 0);
            insertExtraByte(60, (byte) 0);
        }

        if (oldVersion < Version.FVX_1_1_0.id) {
            //add byte for trainer type diversity
            insertExtraByte(61, (byte) 0);
        }

        if (oldVersion < Version.FVX_1_3_0.id) {
            // Introduced Battle Style Randomization.
            // Get the old "Double Battle Only" state to initialize the data to.
            byte initialState = 0;
            if (((dataBlock[42] & 1)) == 0x01) { // is set to double battle mode
                initialState = 0x14;
            }
            insertExtraByte(62, initialState);
            // add byte for "force middle evolution"
            insertExtraByte(63, (byte) 0);
            // add byte for shop items, and move "balanceShopPrices" there
            insertExtraByte(64, (byte) ((dataBlock[39] & 0x20) >> 5));
            dataBlock[39] &= ~0x20;
            // Change GenRestrictions format, to have "allow evolutionary relatives" be the lowest bit,
            // instead of the 8th.
            // Also, 0 was previously used to denote "all generations allowed".
            // Make it use -1 (I.e., all bits 1) instead.
            // Finally, make it little endian.
            int restrictions = FileFunctions.readFullIntBigEndian(dataBlock, 30);
            int allowEvoRelatives = (restrictions & 0x80) >> 7;
            restrictions <<= 1;
            restrictions |= allowEvoRelatives;
            if (restrictions == 0) {
                restrictions = -1;
            }
            FileFunctions.writeFullInt(dataBlock, 30, restrictions);
        }

        // fix checksum
        CRC32 checksum = new CRC32();
        checksum.update(dataBlock, 0, actualDataLength - 8);

        // convert crc32 to int bytes
        byte[] crcBuf = ByteBuffer.allocate(4).putInt((int) checksum.getValue()).array();
        System.arraycopy(crcBuf, 0, dataBlock, actualDataLength - 8, 4);

        // have to make a new byte array to convert to base64
        byte[] finalConfigString = new byte[actualDataLength];
        System.arraycopy(dataBlock, 0, finalConfigString, 0, actualDataLength);
        return Base64.getEncoder().encodeToString(finalConfigString);
    }

    private boolean isFromCTVVersion(int oldVersion) {
        // Just checks the version ids.
        // This means V branch versions 0_9_0 to 0_9_3, which have overlapping version ids,
        // cannot have their settings read correctly. However, there was no easy way to
        // differentiate them and these versions had very few downloads, so we simply let this be.
        return oldVersion >= Version.CTV_4_7_0.id && oldVersion <= Version.CTV_4_8_0.id;
    }

    private void updateCTV(int oldVersion) {
        if (oldVersion < Version.CTV_4_7_0.id) {
            //added new enum WildPokemonTypeMod and moved TypeThemed to it,
            //so we need to select None on RestrictionMod if TypeThemed is selected,
            //and select None on TypeMod otherwise
            int typeThemed = dataBlock[15] & 0x08;
            if (typeThemed != 0) {
                dataBlock[15] |= 0x04;
            } else {
                dataBlock[16] |= 0x20;
            }
        }

        if (oldVersion < Version.CTV_4_7_1.id) {
            //add two new bytes, including new enum
            dataBlock[51] = 0x01;
            dataBlock[52] = 0;
            actualDataLength += 2;
        }

        if (oldVersion < Version.CTV_4_7_2.id) {
            //insert two additional wild pokemon bytes and reorganize
            insertExtraByte(17, (byte) 0);
            insertExtraByte(18, (byte) 0);
            byte areaMethod = 0, restriction = 0,
                    types = 0, various = 0;

            areaMethod |= (byte) ((dataBlock[15] & 0x40) >> 6);
            areaMethod |= (byte) ((dataBlock[15] & 0x20) >> 4);
            areaMethod |= (byte) ((dataBlock[15] & 0x02) << 1);
            areaMethod |= (byte) ((dataBlock[15] & 0x10) >> 1);

            restriction |= (byte) ((dataBlock[15] & 0x04) >> 2);
            restriction |= (byte) ((dataBlock[16] & 0x04) >> 1);
            restriction |= (byte) ((dataBlock[15] & 0x01) << 2);

            types |= (byte) ((dataBlock[16] & 0x20) >> 5);
            types |= (byte) ((dataBlock[16] & 0x40) >> 5);
            types |= (byte) ((dataBlock[15] & 0x08) >> 1);

            various |= (byte) ((dataBlock[15] & 0x80) >> 7);
            various |= (byte) ((dataBlock[16] & 0x01) << 1);
            various |= (byte) ((dataBlock[16] & 0x02) << 1);
            various |= (byte) (dataBlock[16] & 0x08);
            various |= (byte) (dataBlock[16] & 0x10);
            various |= (byte) (dataBlock[16] & 0x80 >> 2);

            dataBlock[15] = areaMethod;
            dataBlock[16] = restriction;
            dataBlock[17] = types;
            dataBlock[18] = various;

        }

        // Pokemon palettes
        insertExtraByte(55, (byte) 0x1);

        // type effectiveness
        insertExtraByte(56, (byte) 0x1);
        // move the former Update Type Effectiveness misctweak to a proper setting
        int miscTweaks = FileFunctions.readFullIntBigEndian(dataBlock, 34);
        boolean updateTypeEffectiveness = (MiscTweak.OLD_UPDATE_TYPE_EFFECTIVENESS.getValue() | miscTweaks) != 0;
        if (updateTypeEffectiveness) {
            dataBlock[56] |= 0x40;
        }

        // new evolutions byte
        insertExtraByte(57, (byte) 0);
    }

    private void updateVBranch(int oldVersion) {
        if (oldVersion < Version.Vb_0_9_0.id) {
            // Pokemon palettes
            insertExtraByte(51, (byte) 0x1);
        }

        if (oldVersion < Version.Vb_0_10_0.id) {
            //added new enum WildPokemonTypeMod and moved TypeThemed to it,
            //so we need to select None on RestrictionMod if TypeThemed is selected,
            //and select None on TypeMod otherwise
            int typeThemed = dataBlock[15] & 0x08;
            if (typeThemed != 0) {
                dataBlock[15] |= 0x08;
            } else {
                dataBlock[16] |= 0x20;
            }
            // we also need to zero out LocationMapping
            dataBlock[15] &= ~0x4;

            // starter type mod / starter no legendaries / starter no dual type checkbox
            insertExtraByte(52, (byte) 0x1);
            // starter single-type type choice
            insertExtraByte(53, (byte) 0);
            // new wild pokes byte
            insertExtraByte(54, (byte) 0);
        }

        if (oldVersion < Version.Vb_0_11_0.id) {
            // type effectiveness
            insertExtraByte(55, (byte) 0x1);
            // move the former Update Type Effectiveness misctweak to a proper setting
            int miscTweaks = FileFunctions.readFullIntBigEndian(dataBlock, 32);
            boolean updateTypeEffectiveness = (MiscTweak.OLD_UPDATE_TYPE_EFFECTIVENESS.getValue() | miscTweaks) != 0;
            if (updateTypeEffectiveness) {
                dataBlock[55] |= 0x40;
            }

            // new evolutions byte
            insertExtraByte(56, (byte) 0);
        }

        // insert two additional wild pokemon bytes and reorganize
        insertExtraByte(17, (byte) 0);
        insertExtraByte(18, (byte) 0);
        removeByte(54); // the old "wild pokemon 3", far away from the rest
        dataBlock[15] = (byte) makeByteSelected(
                restoreState(dataBlock[15], 6), // WildPokemonMod.UNCHANGED
                restoreState(dataBlock[15], 5), // WildPokemonMod.RANDOM
                restoreState(dataBlock[15], 1), // WildPokemonMod.AREA_MAPPING
                restoreState(dataBlock[15], 4), // WildPokemonMod.GLOBAL_MAPPING
                false, // WildPokemonMod.FAMILY_MAPPING (not present in older settings)
                restoreState(dataBlock[15], 2), // WildPokemonMod.LOCATION_MAPPING
                false, false // unused
        );
        dataBlock[16] = (byte) makeByteSelected(
                false, // unused
                restoreState(dataBlock[16], 2), // similarStrengthEncounters
                restoreState(dataBlock[15], 0), // catchEmAllEncounters
                false, false, false, false, false // unused
        );
        dataBlock[17] = (byte) makeByteSelected(
                restoreState(dataBlock[16], 5), // WildPokemonTypeMod.NONE
                restoreState(dataBlock[16], 6), // WildPokemonTypeMod.KEEP_PRIMARY
                restoreState(dataBlock[15], 3), // WildPokemonTypeMod.THEMED_AREAS
                restoreState(dataBlock[54], 0), // keepWildTypeThemes
                false, false, false, false // unused
        );
        dataBlock[18] = (byte) makeByteSelected(
                restoreState(dataBlock[15], 7), // useTimeBasedEncounters
                restoreState(dataBlock[16], 0), // useMinimumCatchRate
                restoreState(dataBlock[16], 1), // blockWildLegendaries
                restoreState(dataBlock[16], 3), // randomizeWildPokemonHeldItems
                restoreState(dataBlock[16], 4), // banBadRandomWildPokemonHeldItems
                restoreState(dataBlock[16], 7), // balanceShakingGrass
                false, false // unused
        );

        // move palette randomization
        moveByte(53, 55);
    }

    // TODO: temp copy from Settings; reconcile these to be in one place
    private static int makeByteSelected(boolean... bools) {
        if (bools.length > 8) {
            throw new IllegalArgumentException("Can't set more than 8 bits in a byte!");
        }

        int initial = 0;
        int state = 1;
        for (boolean b : bools) {
            initial |= b ? state : 0;
            state *= 2;
        }
        return initial;
    }

    // TODO: temp copy from Settings; reconcile these to be in one place
    private static boolean restoreState(byte b, int index) {
        if (index >= 8) {
            throw new IllegalArgumentException("Can't read more than 8 bits from a byte!");
        }

        int value = b & 0xFF;
        return ((value >> index) & 0x01) == 0x01;
    }


    private static byte getRemappedByte(byte old, int[] oldIndexes) {
        int newValue = 0;
        int oldValue = old & 0xFF;
        for (int i = 0; i < oldIndexes.length; i++) {
            if ((oldValue & (1 << oldIndexes[i])) != 0) {
                newValue |= (1 << i);
            }
        }
        return (byte) newValue;
    }

    /**
     * Insert a 4-byte int field in the data block at the given position. Shift
     * everything else up. Do nothing if there's no room left (should never
     * happen)
     * 
     * @param position
     *            The offset to add the field
     * @param value
     *            The value to give to the field
     */
    private void insertIntField(int position, int value) {
        if (actualDataLength + 4 > dataBlock.length) {
            // can't do
            return;
        }
        for (int j = actualDataLength; j > position + 3; j--) {
            dataBlock[j] = dataBlock[j - 4];
        }
        byte[] valueBuf = ByteBuffer.allocate(4).putInt(value).array();
        System.arraycopy(valueBuf, 0, dataBlock, position, 4);
        actualDataLength += 4;
    }

    /**
     * Insert a byte-field in the data block at the given position. Shift
     * everything else up. Do nothing if there's no room left (should never
     * happen)
     * 
     * @param position
     *            The offset to add the field
     * @param value
     *            The value to give to the field
     */
    private void insertExtraByte(int position, byte value) {
        if (actualDataLength == dataBlock.length) {
            // can't do
            return;
        }
        for (int j = actualDataLength; j > position; j--) {
            dataBlock[j] = dataBlock[j - 1];
        }
        dataBlock[position] = value;
        actualDataLength++;
    }

    /**
     * Remove a byte-field in the data block at the given position. Shift
     * everything else down.
     * @param position The offset of the field to remove
     */
    private void removeByte(int position) {
        for (int j = position; j < actualDataLength - 1; j++) {
            dataBlock[j] = dataBlock[j + 1];
        }
        dataBlock[actualDataLength - 1] = (byte) 0x00;
        actualDataLength--;
    }

    /**
     * Removes a byte-field in the data block, and then re-inserts it at
     * another position. Bytes after it are shifted down when it is removed,
     * and shifted up when it is re-inserted.
     * @param positionBefore The offset of the field before it is moved
     * @param positionAfter The offset of the field after it is moved
     */
    private void moveByte(int positionBefore, int positionAfter) {
        byte value = dataBlock[positionBefore];
        removeByte(positionBefore);
        insertExtraByte(positionAfter, value);
    }

}
