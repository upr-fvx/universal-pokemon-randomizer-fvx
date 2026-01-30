package com.dabomstew.pkromio;

// TODO: location likely temporary

import java.util.*;

/**
 * Represents a chunk of ARM9 thumb instructions.
 * <br><br>
 * The purpose of this class is to act as a sort of middle ground; to allow more freedom and power than modifying the
 * code manually with byte-level operations, without needing a full disassembler/editor/assembler pipeline.<br>
 * The user can insert and remove instructions as needed, with relative jump/branch instructions correcting themselves
 * accordingly. (though for now, only BL instructions are supported)
 */
public class ARMThumbCode {

    public static final byte LSL_imm_0 = (byte) 0x00, LDRPC_r1 = (byte) 0x49, ADD_i8r4 = (byte) 0x34,
            ADDSP_imm7 = (byte) 0xB0, POP_pc = (byte) 0xBD, BGE = (byte) 0xDA;

    private static final List<String> OP_CODE_NAMES = Collections.unmodifiableList(Arrays.asList(
            "LSL imm", "LSL imm", "LSL imm", "LSL imm", "LSL imm", "LSL imm", "LSL imm", "LSL imm",
            "LSR imm", "LSR imm", "LSR imm", "LSR imm", "LSR imm", "LSR imm", "LSR imm", "LSR imm",
            "ASR imm", "ASR imm", "ASR imm", "ASR imm", "ASR imm", "ASR imm", "ASR imm", "ASR imm",
            "ADD reg", "ADD reg", "SUB reg", "SUB reg", "ADD imm3", "ADD imm3", "SUB imm3", "SUB imm3",
            "MOV i8r0", "MOV i8r1", "MOV i8r2", "MOV i8r3", "MOV i8r4", "MOV i8r5", "MOV i8r6", "MOV i8r7",
            "CMP i8r0", "CMP i8r1", "CMP i8r2", "CMP i8r3", "CMP i8r4", "CMP i8r5", "CMP i8r6", "CMP i8r7",
            "ADD i8r0", "ADD i8r1", "ADD i8r2", "ADD i8r3", "ADD i8r4", "ADD i8r5", "ADD i8r6", "ADD i8r7",
            "SUB i8r0", "SUB i8r1", "SUB i8r2", "SUB i8r3", "SUB i8r4", "SUB i8r5", "SUB i8r6", "SUB i8r7",
            "DP g1", "DP g2", "DP g3", "DP g4", "ADDH", "CMPH", "MOVH", "BX reg",
            "LDRPC r0", "LDRPC r1", "LDRPC r2", "LDRPC r3", "LDRPC r4", "LDRPC r5", "LDRPC r6", "LDRPC r7",
            "STR reg", "STR reg", "STRH reg", "STRH reg", "STRB reg", "STRB reg", "LDRSB reg", "LDRSB reg",
            "LDR reg", "LDR reg", "LDRH reg", "LDRH reg", "LDRB reg", "LDRB reg", "LDRSH reg", "LDRSH reg",
            "STR imm5", "STR imm5", "STR imm5", "STR imm5", "STR imm5", "STR imm5", "STR imm5", "STR imm5",
            "LDR imm5", "LDR imm5", "LDR imm5", "LDR imm5", "LDR imm5", "LDR imm5", "LDR imm5", "LDR imm5",
            "STRB imm5", "STRB imm5", "STRB imm5", "STRB imm5", "STRB imm5", "STRB imm5", "STRB imm5", "STRB imm5",
            "LDRB imm5", "LDRB imm5", "LDRB imm5", "LDRB imm5", "LDRB imm5", "LDRB imm5", "LDRB imm5", "LDRB imm5",
            "STRH imm5", "STRH imm5", "STRH imm5", "STRH imm5", "STRH imm5", "STRH imm5", "STRH imm5", "STRH imm5",
            "LDRH imm5", "LDRH imm5", "LDRH imm5", "LDRH imm5", "LDRH imm5", "LDRH imm5", "LDRH imm5", "LDRH imm5",
            "STRSP r0", "STRSP r1", "STRSP r2", "STRSP r3", "STRSP r4", "STRSP r5", "STRSP r6", "STRSP r7",
            "LDRSP r0", "LDRSP r1", "LDRSP r2", "LDRSP r3", "LDRSP r4", "LDRSP r5", "LDRSP r6", "LDRSP r7",
            "ADDPC r0", "ADDPC r1", "ADDPC r2", "ADDPC r3", "ADDPC r4", "ADDPC r5", "ADDPC r6", "ADDPC r7",
            "ADDSP r0", "ADDSP r1", "ADDSP r2", "ADDSP r3", "ADDSP r4", "ADDSP r5", "ADDSP r6", "ADDSP r7",
            "ADDSP imm7", "!INVALID!", "!INVALID!", "!INVALID!", "PUSH", "PUSH lr", "!INVALID!", "!INVALID!",
            "!INVALID!", "!INVALID!", "!INVALID!", "!INVALID!", "POP", "POP pc", "BKPT", "!INVALID!",
            "STMIA r0", "STMIA r1", "STMIA r2", "STMIA r3", "STMIA r4", "STMIA r5", "STMIA r6", "STMIA r7",
            "LDMIA r0", "LDMIA r1", "LDMIA r2", "LDMIA r3", "LDMIA r4", "LDMIA r5", "LDMIA r6", "LDMIA r7",
            "BEQ", "BNE", "BCS", "BCC", "BMI", "BPL", "BVS", "BVC",
            "BHI", "BLS", "BGE", "BLT", "BGT", "BLE", "!INVALID!", "SWI",
            "B", "B", "B", "B", "B", "B", "B", "B",
            "BLX off", "BLX off", "BLX off", "BLX off", "BLX off", "BLX off", "BLX off", "BLX off",
            "BL setup", "BL setup", "BL setup", "BL setup", "BL setup", "BL setup", "BL setup", "BL setup",
            "BL off", "BL off", "BL off", "BL off", "BL off", "BL off", "BL off", "BL off"
    ));

    private static class Instruction {
        byte lower;
        byte higher;

        Instruction(byte lower, byte higher) {
            this.lower = lower;
            this.higher = higher;
        }

        boolean isBLSetup() {
            return 0xF0 <= (higher & 0xFF) && (higher & 0xFF) <= 0xF7;
        }

        boolean isBLOffset() {
            return 0xF8 <= (higher & 0xFF);
        }

        @Override
        public String toString() {
            return toBitString(lower) + " " + toBitString(higher) + " | "
                    + toHexString(lower) + " " + toHexString(higher) + " | "
                    + OP_CODE_NAMES.get(higher & 0xFF);
        }

        private String toBitString(byte b) {
            // non-standard method to keep the right amount of zeroes
            return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
        }

        private String toHexString(byte b) {
            // non-standard method to have e.g. 0x0F not come out as just "F".
            return Integer.toHexString((b & 0xFF) + 0x100).substring(1);
        }
    }

    private final List<Instruction> instructions;

    public ARMThumbCode(byte[] bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("bytes can't be empty");
        }
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("bytes.length must be even");
        }

        instructions = new LinkedList<>();
        for (int i = 0; i < bytes.length; i += 2) {
            instructions.add(new Instruction(bytes[i], bytes[i + 1]));
        }
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[instructions.size() * 2];
        for (int i = 0; i < instructions.size(); i++) {
            bytes[i * 2] = instructions.get(i).lower;
            bytes[i * 2 + 1] = instructions.get(i).higher;
        }
        return bytes;
    }

    /**
     * Inserts a number of instructions.<br>
     * All BL instructions past the insertion point, get updated accordingly. (Other branch instructions <i>could</i>
     * also be supported, they simply aren't implemented. Add them as needed.)
     *
     * @param offset    Where to insert the instructions. Since thumb instructions are 16-bit, has to be even.
     * @param instBytes The bytes to insert. Since thumb instructions are 16-bit, has to be an even amount.
     */
    public void insertInstructions(int offset, byte... instBytes) {
        if (offset % 2 != 0)
            throw new IllegalArgumentException("offset must be even");
        if (offset < 0)
            throw new IllegalArgumentException("offset must be positive");
        if (offset > instructions.size() * 2)
            throw new IllegalArgumentException("offset too high");
        if (instBytes.length % 2 != 0)
            throw new IllegalArgumentException("instBytes must have even length.");

        List<Instruction> toAdd = new LinkedList<>();
        for (int i = 0; i < instBytes.length; i += 2) {
            toAdd.add(new Instruction(instBytes[i], instBytes[i + 1]));
        }
        instructions.addAll(offset / 2, toAdd);

        shiftBranchInstructions(offset + instBytes.length, instBytes.length / 2);
    }

    /**
     * Removes a number of instructions.<br>
     * All BL instruction past the removal point, get updated accordingly. (Other branch instructions <i>could</i>
     * also be supported, they simply aren't implemented. Add them as needed.)
     *
     * @param offset Where to start removing the instructions. Since thumb instructions are 16-bit, has to be even.
     * @param length The number of <i>instructions</i> to remove. Since thumb instructions are 16-bit,
     *               twice as many bytes are removed.
     */
    public void removeInstructions(int offset, int length) {
        if (offset % 2 != 0)
            throw new IllegalArgumentException("offset must be even");
        if (offset < 0)
            throw new IllegalArgumentException("offset must be positive");
        if (instructions.size() < (offset / 2) + length)
            throw new IllegalArgumentException("offset and length imply removing instructions beyond the code's end");

        Iterator<Instruction> iter = instructions.listIterator(offset / 2);
        for (int i = 0; i < length; i++) {
            iter.next();
            iter.remove();
        }

        shiftBranchInstructions(offset, -length);
    }

    /**
     * Sets the instruction at the given offset, overwriting what was previously there.
     *
     * @param offset Where to set the instruction. Since thumb instructions are 16-bit, has to be even.
     * @param lower  The lower byte to set.
     * @param higher The higher byte to set.
     */
    public void setInstruction(int offset, byte lower, byte higher) {
        if (offset % 2 != 0)
            throw new IllegalArgumentException("offset must be even");
        if (offset < 0)
            throw new IllegalArgumentException("offset must be positive");
        if (offset > instructions.size() * 2)
            throw new IllegalArgumentException("offset too high");

        Instruction inst = instructions.get(offset / 2);
        inst.lower = lower;
        inst.higher = higher;
    }

    /**
     * Updates relative branch instructions, to account for them having been shifted up/down in the code.
     * For now, <b>only BL instructions are supported</b>.
     *
     * @param startOffset Where to start updating the instructions.
     * @param shift       The amount of bytes to add (or subtract if the value is negative).
     */
    private void shiftBranchInstructions(int startOffset, int shift) {
        ListIterator<Instruction> iter = instructions.listIterator(startOffset / 2);
        int currOffset = startOffset;

        while (iter.hasNext()) {
            Instruction inst = iter.next();
            currOffset += 2;

            // BL instruction.
            if (inst.isBLSetup()) {
                Instruction inst2 = iter.next();
                iter.previous();
                if (inst2.isBLOffset()) {

                    int disp = getBLDisplacement(inst, inst2);
                    disp += shift;
                    if (!blDestinationIncludedInShift(startOffset, currOffset, disp))
                        setBLDisplacement(inst, inst2, disp);

                } else {
                    throw new RuntimeException("BL setup not followed by BL offset");
                }
            }
        }
    }

    private boolean blDestinationIncludedInShift(int startOffset, int blInstOffset, int displacement) {
        // Often, a BL instruction will refer to somewhere far away, way outside the code snippet
        // that is being represented by this ARMThumbCode object.
        // However, it might too refer to somewhere close.
        // If a BL instruction refers to a point in the code, which is being shifted around due to
        // the same insertion or removal of instruction, then the BL displacement value should not change.
        // That is what this method checks for.
        return (blInstOffset + displacement >= startOffset)
                && ((blInstOffset + displacement) / 2 <= instructions.size());
    }

    private int getBLDisplacement(Instruction blSetup, Instruction blOffset) {
        int disp = ((blSetup.higher & 0b11) << 19) + ((blSetup.lower & 0b11111111) << 11)
                + ((blOffset.higher & 0b111) << 8) + (blOffset.lower & 0b11111111);
        if ((blSetup.higher & 0b100) > 0)
            disp *= -1;
        return disp;
    }

    private void setBLDisplacement(Instruction blSetup, Instruction blOffset, int disp) {
        int sign = disp > 0 ? 0 : 1;
        if (sign == 1) // negative
            disp *= -1;
        if (disp >= 2 << 22) {
            throw new IllegalArgumentException("number too large to be represented by 21 bits + sign");
        }
        blSetup.higher = (byte) (0xF0 + (sign << 2) + ((disp >>> 19) & 0b11));
        blSetup.lower = (byte) ((disp >>> 11) & 0b11111111);
        blOffset.higher = (byte) (0xF8 + ((disp >>> 8) & 0b111));
        blOffset.lower = (byte) (disp & 0b11111111);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ARMThumbCode: [\n");
        for (Instruction inst : instructions) {
            sb.append(inst.toString());
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

}
