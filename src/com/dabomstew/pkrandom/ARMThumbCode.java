package com.dabomstew.pkrandom;

// TODO: location likely temporary

import com.dabomstew.pkrandom.constants.Gen4Constants;

import java.util.*;

/**
 * Represents a chunk of ARM9 thumb instructions.<br>
 * The purpose of this class is to act as a sort of middle ground; to allow more freedom and power than modifying the
 * code manually with byte-level operations, without needing a full disassembler/editor/assembler pipeline.<br>
 * The user can insert and remove instructions as needed, with relative jump/branch instructions correcting themselves
 * accordingly. (though for now, only BL instructions are supported)
 */
public class ARMThumbCode {

    private static final List<String> OP_CODE_NAMES = Collections.unmodifiableList(Arrays.asList(
        "LSL imm",    "LSL imm",    "LSL imm",    "LSL imm",    "LSL imm",    "LSL imm",    "LSL imm",    "LSL imm",
        "LSR imm",    "LSR imm",    "LSR imm",    "LSR imm",    "LSR imm",    "LSR imm",    "LSR imm",    "LSR imm",
        "ASR imm",    "ASR imm",    "ASR imm",    "ASR imm",    "ASR imm",    "ASR imm",    "ASR imm",    "ASR imm",
        "ADD reg",    "ADD reg",    "SUB reg",    "SUB reg",    "ADD imm3",   "ADD imm3",   "SUB imm3",   "SUB imm3",
        "MOV i8r0",   "MOV i8r1",   "MOV i8r2",   "MOV i8r3",   "MOV i8r4",   "MOV i8r5",   "MOV i8r6",   "MOV i8r7",
        "CMP i8r0",   "CMP i8r1",   "CMP i8r2",   "CMP i8r3",   "CMP i8r4",   "CMP i8r5",   "CMP i8r6",   "CMP i8r7",
        "ADD i8r0",   "ADD i8r1",   "ADD i8r2",   "ADD i8r3",   "ADD i8r4",   "ADD i8r5",   "ADD i8r6",   "ADD i8r7",
        "SUB i8r0",   "SUB i8r1",   "SUB i8r2",   "SUB i8r3",   "SUB i8r4",   "SUB i8r5",   "SUB i8r6",   "SUB i8r7",
        "DP g1",      "DP g2",      "DP g3",      "DP g4",      "ADDH",       "CMPH",       "MOVH",       "BX reg",
        "LDRPC r0",   "LDRPC r1",   "LDRPC r2",   "LDRPC r3",   "LDRPC r4",   "LDRPC r5",   "LDRPC r6",   "LDRPC r7",
        "STR reg",    "STR reg",    "STRH reg",   "STRH reg",   "STRB reg",   "STRB reg",   "LDRSB reg",  "LDRSB reg",
        "LDR reg",    "LDR reg",    "LDRH reg",   "LDRH reg",   "LDRB reg",   "LDRB reg",   "LDRSH reg",  "LDRSH reg",
        "STR imm5",   "STR imm5",   "STR imm5",   "STR imm5",   "STR imm5",   "STR imm5",   "STR imm5",   "STR imm5",
        "LDR imm5",   "LDR imm5",   "LDR imm5",   "LDR imm5",   "LDR imm5",   "LDR imm5",   "LDR imm5",   "LDR imm5",
        "STRB imm5",  "STRB imm5",  "STRB imm5",  "STRB imm5",  "STRB imm5",  "STRB imm5",  "STRB imm5",  "STRB imm5",
        "LDRB imm5",  "LDRB imm5",  "LDRB imm5",  "LDRB imm5",  "LDRB imm5",  "LDRB imm5",  "LDRB imm5",  "LDRB imm5",
        "STRH imm5",  "STRH imm5",  "STRH imm5",  "STRH imm5",  "STRH imm5",  "STRH imm5",  "STRH imm5",  "STRH imm5",
        "LDRH imm5",  "LDRH imm5",  "LDRH imm5",  "LDRH imm5",  "LDRH imm5",  "LDRH imm5",  "LDRH imm5",  "LDRH imm5",
        "STRSP r0",   "STRSP r1",   "STRSP r2",   "STRSP r3",   "STRSP r4",   "STRSP r5",   "STRSP r6",   "STRSP r7",
        "LDRSP r0",   "LDRSP r1",   "LDRSP r2",   "LDRSP r3",   "LDRSP r4",   "LDRSP r5",   "LDRSP r6",   "LDRSP r7",
        "ADDPC r0",   "ADDPC r1",   "ADDPC r2",   "ADDPC r3",   "ADDPC r4",   "ADDPC r5",   "ADDPC r6",   "ADDPC r7",
        "ADDSP r0",   "ADDSP r1",   "ADDSP r2",   "ADDSP r3",   "ADDSP r4",   "ADDSP r5",   "ADDSP r6",   "ADDSP r7",
        "ADDSP imm7", "!INVALID!",  "!INVALID!",  "!INVALID!",  "PUSH",       "PUSH lr",    "!INVALID!",  "!INVALID!",
        "!INVALID!",  "!INVALID!",  "!INVALID!",  "!INVALID!",  "POP",        "POP pc",     "BKPT",       "!INVALID!",
        "STMIA r0",   "STMIA r1",   "STMIA r2",   "STMIA r3",   "STMIA r4",   "STMIA r5",   "STMIA r6",   "STMIA r7",
        "LDMIA r0",   "LDMIA r1",   "LDMIA r2",   "LDMIA r3",   "LDMIA r4",   "LDMIA r5",   "LDMIA r6",   "LDMIA r7",
        "BEQ",        "BNE",        "BCS",        "BCC",        "BMI",        "BPL",        "BVS",        "BVC",
        "BHI",        "BLS",        "BGE",        "BLT",        "BGT",        "BLE",        "!INVALID!",  "SWI",
        "B",          "B",          "B",          "B",          "B",          "B",          "B",          "B",
        "BLX off",    "BLX off",    "BLX off",    "BLX off",    "BLX off",    "BLX off",    "BLX off",    "BLX off",
        "BL setup",   "BL setup",   "BL setup",   "BL setup",   "BL setup",   "BL setup",   "BL setup",   "BL setup",
        "BL off",     "BL off",     "BL off",     "BL off",     "BL off",     "BL off",     "BL off",     "BL off"
    ));

    private static class Instruction {
        byte lower;
        byte higher;
        Instruction (byte lower, byte higher) {
            this.lower = lower;
            this.higher = higher;
        }
    }

    public static void main(String[] args) {
        ARMThumbCode a = new ARMThumbCode(Gen4Constants.mysteryEggCommandImprovement);
        System.out.println(a);
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
        return new byte[0]; // TODO:
    }

    /**
     * Inserts a number of instructions.<br>
     * All BL instructions past the insertion point, get updated accordingly. (Other branch instructions <i>could</i>
     * also be supported, they simply aren't implemented. Add them as needed.)
     * @param offset Where to insert the instructions. Since thumb instructions are 16-bit, has to be even.
     * @param instBytes The bytes to insert. Since thumb instructions are 16-bit, has to be an even amount.
     */
    public void insertInstructions(int offset, byte... instBytes) {
        if (offset % 2 != 0) {
            throw new IllegalArgumentException("offset must be even");
        }
        if (instBytes.length % 2 != 0) {
            throw new IllegalArgumentException("instBytes must have even length.");
        }

        // TODO
    }

    /**
     * Removes a number of instructions.<br>
     * All BL instruction past the removal point, get updated accordingly. (Other branch instructions <i>could</i>
     *  also be supported, they simply aren't implemented. Add them as needed.)
     * @param offset Where to start removing the instructions. Since thumb instructions are 16-bit, has to be even.
     * @param length The number of <i>instructions</i> to remove. Since thumb instructions are 16-bit,
     *               twice as many bytes are removed.
     */
    public void removeInstructions(int offset, int length) {
        if (offset % 2 != 0) {
            throw new IllegalArgumentException("offset must be even");
        }

        // TODO
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ARMThumbCode: [\n");
        for (Instruction inst : instructions) {
            sb.append(toBitString(inst.lower)).append(" ").append(toBitString(inst.higher));
            sb.append(" | ");
            sb.append(toHexString(inst.lower)).append(" ").append(toHexString(inst.higher));
            sb.append(" | ");
            sb.append(OP_CODE_NAMES.get(inst.higher & 0xFF));
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toBitString(byte b) {
        // non-standard method to keep the right amount of zeroes
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }

    private String toHexString(byte b){
        // non-standard method to have e.g. 0x0F not come out as just "F".
        return Integer.toHexString((b & 0xFF) + 0x100).substring(1);
    }

}
