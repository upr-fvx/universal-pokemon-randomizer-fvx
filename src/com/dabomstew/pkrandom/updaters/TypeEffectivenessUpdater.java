package com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkromio.gamedata.Effectiveness;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.gamedata.TypeTable;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Map;
import java.util.TreeMap;

public class TypeEffectivenessUpdater extends Updater<Type, Type, Effectiveness> {

    public static final int UPDATE_TO_GEN = 6;

    private final Map<Type, Map<Type, Update<Effectiveness>>> typeEffUpdates = new TreeMap<>();

    public TypeEffectivenessUpdater(RomHandler romHandler) {
        super(romHandler);
    }

    @Override
    public Map<Type, Map<Type, Update<Effectiveness>>> getUpdates() {
        return typeEffUpdates;
    }

    /**
     * Updates the Type effectiveness to how it is in (vanilla) Gen 6.
     */
    public void updateTypeEffectiveness() {
        TypeTable typeTable = romHandler.getTypeTable();

        if (romHandler.generationOfPokemon() == 1) {
            updateEffectiveness(typeTable, Type.POISON, Type.BUG, Effectiveness.NEUTRAL);
            updateEffectiveness(typeTable, Type.BUG, Type.POISON, Effectiveness.HALF);
            updateEffectiveness(typeTable, Type.GHOST, Type.PSYCHIC, Effectiveness.DOUBLE);
            updateEffectiveness(typeTable, Type.ICE, Type.FIRE, Effectiveness.HALF);
        } else {
            updateEffectiveness(typeTable, Type.GHOST, Type.STEEL, Effectiveness.NEUTRAL);
            updateEffectiveness(typeTable, Type.DARK, Type.STEEL, Effectiveness.NEUTRAL);
        }

        romHandler.setTypeTable(typeTable);
    }

    private void updateEffectiveness(TypeTable tt, Type attacker, Type defender, Effectiveness eff) {
        Effectiveness before = tt.getEffectiveness(attacker, defender);
        tt.setEffectiveness(attacker, defender, eff);
        addUpdate(attacker, before, eff, defender);
    }

    private void addUpdate(Type attacker, Effectiveness before, Effectiveness after, Type defender) {
        if (!typeEffUpdates.containsKey(attacker)) {
            typeEffUpdates.put(attacker, new TreeMap<>());
        }
        typeEffUpdates.get(attacker).put(defender, new Update<>(before, after));
    }
}
