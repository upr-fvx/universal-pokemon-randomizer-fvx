package com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkrandom.game_data.Effectiveness;
import com.dabomstew.pkrandom.game_data.Type;
import com.dabomstew.pkrandom.game_data.TypeTable;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

public class TypeEffectivenessUpdater extends Updater {

    // for now has no method to return anything loggable, separate from the final TypeTable

    public TypeEffectivenessUpdater(RomHandler romHandler) {
        super(romHandler);
    }

    /**
     * Updates the Type effectiveness to how it is in (vanilla) Gen 6.
     */
    public void updateTypeEffectiveness() {
        TypeTable typeTable = romHandler.getTypeTable();

        if (romHandler.generationOfPokemon() == 1) {
            typeTable.setEffectiveness(Type.POISON, Type.BUG, Effectiveness.NEUTRAL);
            typeTable.setEffectiveness(Type.BUG, Type.POISON, Effectiveness.HALF);
            typeTable.setEffectiveness(Type.GHOST, Type.PSYCHIC, Effectiveness.DOUBLE);
            typeTable.setEffectiveness(Type.ICE, Type.FIRE, Effectiveness.HALF);
        } else {
            typeTable.setEffectiveness(Type.GHOST, Type.STEEL, Effectiveness.NEUTRAL);
            typeTable.setEffectiveness(Type.DARK, Type.STEEL, Effectiveness.NEUTRAL);
        }
        romHandler.setTypeTable(typeTable);

        updated = true;
    }
}
