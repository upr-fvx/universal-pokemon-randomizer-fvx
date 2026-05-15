package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.MoveCategory;
import com.uprfvx.romio.gamedata.Type;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Gen3MoveDataWriterTest {

    @Test
    public void writeGen3BattleMoveDataWritesCoreMoveDataAndPreservesUnmanagedBytes() {
        byte[] data = filledMoveData();
        Move move = move(7, 95, Type.WATER, 85, 12, MoveCategory.SPECIAL);

        Gen3RomHandler.writeGen3BattleMoveData(data, 0, move, 85, true);

        assertEquals(7, data[0] & 0xFF);
        assertEquals(95, data[1] & 0xFF);
        assertEquals(0x0B, data[2] & 0xFF);
        assertEquals(85, data[3] & 0xFF);
        assertEquals(12, data[4] & 0xFF);
        assertEquals(1, data[10] & 0xFF);
        assertUnmanagedBytesPreserved(data);
    }

    @Test
    public void writeGen3BattleMoveDataUsesCfruDpeFairyTypeByte() {
        byte[] data = filledMoveData();
        Move move = move(1, 60, Type.FAIRY, 100, 15, MoveCategory.PHYSICAL);

        Gen3RomHandler.writeGen3BattleMoveData(data, 0, move, 100, true);

        assertEquals(0x17, data[2] & 0xFF);
        assertEquals(0, data[10] & 0xFF);
    }

    @Test
    public void writeGen3BattleMoveDataPreservesSplitWhenNotCfruDpe() {
        byte[] data = filledMoveData();
        Move move = move(1, 60, Type.FAIRY, 100, 15, MoveCategory.STATUS);

        Gen3RomHandler.writeGen3BattleMoveData(data, 0, move, 100, false);

        assertEquals(0x55, data[10] & 0xFF);
    }

    private static byte[] filledMoveData() {
        byte[] data = new byte[12];
        Arrays.fill(data, (byte) 0x55);
        return data;
    }

    private static Move move(int effectIndex, int power, Type type, int hitratio, int pp, MoveCategory category) {
        Move move = new Move();
        move.effectIndex = effectIndex;
        move.power = power;
        move.type = type;
        move.hitratio = hitratio;
        move.pp = pp;
        move.category = category;
        return move;
    }

    private static void assertUnmanagedBytesPreserved(byte[] data) {
        byte[] expected = filledMoveData();
        expected[0] = data[0];
        expected[1] = data[1];
        expected[2] = data[2];
        expected[3] = data[3];
        expected[4] = data[4];
        expected[10] = data[10];
        assertArrayEquals(expected, data);
    }
}
