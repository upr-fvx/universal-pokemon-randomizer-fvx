package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.newnds.NARCArchive;
import com.dabomstew.pkromio.romhandlers.AbstractDSRomHandler;

import java.util.Arrays;

public class DSStaticPokemon {

    protected InFileEntry[] speciesEntries;
    protected InFileEntry[] formeEntries;
    protected InFileEntry[] levelEntries;

    public DSStaticPokemon(InFileEntry[] speciesEntries, InFileEntry[] formeEntries, InFileEntry[] levelEntries) {
        this.speciesEntries = speciesEntries;
        this.formeEntries = formeEntries;
        this.levelEntries = levelEntries;
    }

    public Species getPokemon(AbstractDSRomHandler parent, NARCArchive scriptNARC) {
        // TODO: made AbstractDSRomHandler.readWord/writeWord public for this to work. Kind of ugly.
        return parent.getSpecies().get(parent.readWord(scriptNARC.files.get(speciesEntries[0].getFile()),
                speciesEntries[0].getOffset()));
    }

    public void setPokemon(AbstractDSRomHandler parent, NARCArchive scriptNARC, Species pk) {
        int value = pk.getBaseNumber();
        for (InFileEntry speciesEntry : speciesEntries) {
            byte[] file = scriptNARC.files.get(speciesEntry.getFile());
            parent.writeWord(file, speciesEntry.getOffset(), value);
        }
    }

    public int getForme(NARCArchive scriptNARC) {
        if (formeEntries.length == 0) {
            return 0;
        }
        byte[] file = scriptNARC.files.get(formeEntries[0].getFile());
        return file[formeEntries[0].getOffset()];
    }

    public void setForme(NARCArchive scriptNARC, int forme) {
        for (InFileEntry formeEntry : formeEntries) {
            byte[] file = scriptNARC.files.get(formeEntry.getFile());
            file[formeEntry.getOffset()] = (byte) forme;
        }
    }

    public int getLevelCount() {
        return levelEntries.length;
    }

    public int getLevel(NARCArchive scriptOrMapNARC, int i) {
        if (levelEntries.length <= i) {
            return 1;
        }
        byte[] file = scriptOrMapNARC.files.get(levelEntries[i].getFile());
        return file[levelEntries[i].getOffset()];
    }

    public void setLevel(NARCArchive scriptOrMapNARC, int level, int i) {
        if (levelEntries.length > i) { // Might not have a level entry e.g., it's an egg
            byte[] file = scriptOrMapNARC.files.get(levelEntries[i].getFile());
            file[levelEntries[i].getOffset()] = (byte) level;
        }
    }

    @Override
    public String toString() {
        return "DSStaticPokemon{" +
                "speciesEntries=" + Arrays.toString(speciesEntries) +
                ", formeEntries=" + Arrays.toString(formeEntries) +
                ", levelEntries=" + Arrays.toString(levelEntries) +
                '}';
    }
}
