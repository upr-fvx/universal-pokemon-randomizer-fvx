package com.dabomstew.pkromio.gbspace;

import java.util.ListIterator;

/**
 * A {@link FreedSpace} with bank functionality.<br>
 * Assumes there is data which must be in certain banks, and data which can be placed anywhere. It would be a problem
 * if space freed for bank-specific data could be used up by bank-unspecific data, so there is a simple "reservation"
 * system.<br>
 * {@link #findAndUnfree(int)} can only find freed space in banks that are NOT reserved.<br>
 * {@link #findAndUnfreeInBank(int, int)} on the other hand lets you specify a bank, assuming you only use it for
 * bank-specific data.
 *
 */
public class BankDividedFreedSpace extends FreedSpace {

    private final int bankSize;
    private final int numberOfBanks;
    private final boolean[] reservedBanks;

    public BankDividedFreedSpace(int bankSize, int numberOfBanks, int[] reservedBanks) {
        if (bankSize <= 0) {
            throw new IllegalArgumentException("bankSize must be positive.");
        }
        this.bankSize = bankSize;
        this.numberOfBanks = numberOfBanks;
        this.reservedBanks = new boolean[numberOfBanks];
        for (int i : reservedBanks) {
            this.reservedBanks[i] = true;
        }
    }

    @Override
    public void free(int start, int length) {
        //System.out.printf("freeing %d bytes starting from 0x%x.%n", length, start);
        if (freedChunkWouldCrossBankBoundary(start, length)) {
            throw new RuntimeException("Can't free a space spanning over multiple banks. This is a safety measure " +
                    "to prevent bad usage of free().");
        }
        super.free(start, length);
        splitFreedChunksAtBankBoundaries();
        //System.out.println("after:\t" + this);
    }

    private boolean freedChunkWouldCrossBankBoundary(int start, int length) {
        int startBank = start / bankSize;
        int endBank = (start + length - 1) / bankSize;
        return startBank != endBank;
    }

    private void splitFreedChunksAtBankBoundaries() {
        int bank = 0;

        ListIterator<FreedChunk> iterator = freedChunks.listIterator();
        while (iterator.hasNext()) {
            FreedChunk fs = iterator.next();
            while (freedChunkStartBank(fs) > bank) {
                bank++;
            }

            int startOfNextBank = (bank + 1) * bankSize;
            if (fs.end >= startOfNextBank) {
                FreedChunk splitOff = new FreedChunk(startOfNextBank, fs.end);
                fs.end = startOfNextBank - 1;
                iterator.add(splitOff);
                iterator.previous();
            }
        }
    }

    private int freedChunkStartBank(FreedChunk fc) {
        return fc.start / bankSize;
    }

    /**
     * Only finds/unfrees in banks that are NOT reserved.
     */
    @Override
    public int findAndUnfree(int length) {
        for (int i = 0; i < numberOfBanks; i++) {
            if (!isBankReserved(i)) {
                int offset = findAndUnfreeInBank(length, i);
                if (offset != -1) {
                    return offset;
                }
            }
        }
        return -1;
    }

    private boolean isBankReserved(int bank) {
        return reservedBanks[bank];
    }

    public int findAndUnfreeInBank(int length, int bank) {
        // System.out.println("looking for " + length + " bytes in bank " + bank);
        if (length < 1) {
            throw new IllegalArgumentException("length must be at least 1");
        }
        FreedChunk found = findInBank(length, bank);
        if (found == null) {
            return -1;
        }
        int offset = found.start;
        unfree(found, length);
        //System.out.println("after:\t" + this);
        return offset;
    }

    private FreedChunk findInBank(int length, int bank) {
        for (FreedChunk fc : freedChunks) {
            if (fc.getLength() >= length && freedChunkStartBank(fc) == bank) {
                return fc;
            }
        }
        return null;
    }

    public String toString() {
        return super.toString() + "(bank size: " + bankSize + " bytes)";
    }

}
