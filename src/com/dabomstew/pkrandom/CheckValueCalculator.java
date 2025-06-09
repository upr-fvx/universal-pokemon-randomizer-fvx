package com.dabomstew.pkrandom;

import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.romhandlers.RomHandler;

public class CheckValueCalculator {

    private final RomHandler romHandler;
    private final Settings settings;

    private int checkValue;

    public CheckValueCalculator(RomHandler romHandler, Settings settings) {
        this.romHandler = romHandler;
        this.settings = settings;
    }

    public int calculate() {

        checkValue = 0;

        addSpeciesInfo();
        addTrainerInfo();
        addEncounterInfo();
        addTMMoveInfo();
        addMTMoveInfo();
        addStaticEncounterInfo();
        addTotemInfo();

        return checkValue;
    }

    private void addSpeciesInfo() {
        for (Species pkmn : romHandler.getSpecies()) {
            if (pkmn != null) {
                addToCV(pkmn.getHp(), pkmn.getAttack(), pkmn.getDefense(), pkmn.getSpeed(), pkmn.getSpatk(),
                        pkmn.getSpdef(), pkmn.getAbility1(), pkmn.getAbility2(), pkmn.getAbility3());
            }
        }
    }

    private void addTrainerInfo() {
        for (Trainer t : romHandler.getTrainers()) {
            for (TrainerPokemon tpk : t.pokemon) {
                addToCV(tpk.getLevel(), tpk.getSpecies().getNumber());
            }
        }
    }

    private void addEncounterInfo() {
        boolean useTimeBasedEncounters = settings.isUseTimeBasedEncounters() ||
                (!settings.isRandomizeWildPokemon() && settings.isWildLevelsModified());
        for (EncounterArea area : romHandler.getEncounters(useTimeBasedEncounters)) {
            for (Encounter e : area) {
                addToCV(e.getLevel(), e.getSpecies().getNumber());
            }
        }

    }

    private void addTMMoveInfo() {
        for (int tmMove : romHandler.getTMMoves()) {
            addToCV(tmMove);
        }

    }

    private void addMTMoveInfo() {
        for (int mtMove : romHandler.getMoveTutorMoves()) {
            addToCV(mtMove);
        }

    }

    private void addStaticEncounterInfo() {
        for (StaticEncounter se : romHandler.getStaticPokemon()) {
            addToCV(se.getSpecies().getNumber());
        }
    }

    private void addTotemInfo() {
        for (TotemPokemon totem : romHandler.getTotemPokemon()) {
            addToCV(totem.getSpecies().getNumber());
        }
    }

    private void addToCV(int... values) {
        for (int value : values) {
            checkValue = Integer.rotateLeft(checkValue, 3);
            checkValue ^= value;
        }
    }

}
