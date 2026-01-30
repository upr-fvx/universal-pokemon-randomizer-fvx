package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.gamedata.Effectiveness;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.gamedata.TypeTable;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.*;

public class TypeEffectivenessRandomizer extends Randomizer {

    public TypeEffectivenessRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    private static final Effectiveness[] TO_BALANCE_FOR = new Effectiveness[]
            {Effectiveness.ZERO, Effectiveness.HALF, Effectiveness.DOUBLE};
    private static final int MAX_PLACEMENT_TRIES = 10000;

    boolean balanced;

    private TypeTable oldTable;
    private TypeTable typeTable;
    private final int[] effCounts = new int[Effectiveness.values().length];
    private Map<Effectiveness, Integer> maxWhenAttacking;
    private Map<Effectiveness, Integer> maxWhenDefending;

    private int placementTries;

    public void randomizeTypeEffectiveness(boolean balanced) {
        this.balanced = balanced;
        oldTable = romHandler.getTypeTable();
        typeTable = new TypeTable(oldTable.getTypes());

        fillEffCounts();
        if (balanced) {
            initMaxForBalanced();
        }

        placementTries = 0;
        placeEffectiveness(Effectiveness.ZERO);
        placeEffectiveness(Effectiveness.HALF);
        placeEffectiveness(Effectiveness.DOUBLE);

        romHandler.setTypeTable(typeTable);
        changesMade = true;
    }

    private void fillEffCounts() {
        Arrays.fill(effCounts, 0);
        for (Type attacker : oldTable.getTypes()) {
            for (Type defender : oldTable.getTypes()) {
                Effectiveness eff = oldTable.getEffectiveness(attacker, defender);
                effCounts[eff.ordinal()]++;
            }
        }
    }

    private void initMaxForBalanced() {
        maxWhenAttacking = new EnumMap<>(Effectiveness.class);
        maxWhenDefending = new EnumMap<>(Effectiveness.class);
        for (Effectiveness eff : TO_BALANCE_FOR) {
            maxWhenAttacking.put(eff, 0);
            maxWhenDefending.put(eff, 0);
            for (Type t : oldTable.getTypes()) {
                maxWhenAttacking.put(eff, Math.max(maxWhenAttacking.get(eff), oldTable.whenAttacking(t, eff).size()));
                maxWhenDefending.put(eff, Math.max(maxWhenDefending.get(eff), oldTable.whenDefending(t, eff).size()));
            }
        }
    }

    private void placeEffectiveness(Effectiveness eff) {
        while (effCounts[eff.ordinal()] > 0 && placementTries <= MAX_PLACEMENT_TRIES) {
            Type attacker = typeService.randomType(random);
            Type defender = typeService.randomType(random);
            if (isValidPlacement(attacker, defender, eff)) {
                typeTable.setEffectiveness(attacker, defender, eff);
                effCounts[eff.ordinal()]--;
            }
            placementTries++;
        }
        if (placementTries > MAX_PLACEMENT_TRIES) {
            throw new RandomizationException("Couldn't randomize Type Effectiveness");
        }
    }

    private boolean isValidPlacement(Type attacker, Type defender, Effectiveness eff) {
        if (typeTable.getEffectiveness(attacker, defender) != Effectiveness.NEUTRAL)
            return false;
        if (balanced) {
            if (typeTable.whenAttacking(attacker, eff).size() == maxWhenAttacking.get(eff))
                return false;
            if (typeTable.whenDefending(defender, eff).size() == maxWhenDefending.get(eff))
                return false;
        }
        return true;
    }

    // Due to how the algorithm below works the final TypeTable will be weighted unevenly towards TypeTables
    // that are more similar to the original one. If this constant is large enough though, that unevenness
    // will be negligible. It's basically a sort of random walk, where we are more likely to be near the starting
    // point the fewer steps we take. The tradeoff for better randomness being the algorithm taking more time.
    private static final int TYPES_KEEP_IDENTITIES_SWAPS = 10000;

    public void randomizeTypeEffectivenessKeepIdentities() {
        TypeTable typeTable = new TypeTable(romHandler.getTypeTable());

        int swapsDone = 0;
        while (swapsDone < TYPES_KEEP_IDENTITIES_SWAPS) {
            Type colA = typeService.randomType(random);
            Type colB = typeService.randomType(random);

            int chunkSize = random.nextInt(typeTable.getTypes().size());
            Set<Type> chunk = new HashSet<>(chunkSize);
            while (chunk.size() < chunkSize) {
                chunk.add(typeService.randomType(random));
            }

            if (typeTableChunkCanBeSwapped(typeTable, colA, colB, chunk)) {
                swapTypeTableChunk(typeTable, colA, colB, chunk);
                swapsDone++;
            }
        }

        romHandler.setTypeTable(typeTable);
        changesMade = true;
    }

    private boolean typeTableChunkCanBeSwapped(TypeTable typeTable, Type colA, Type colB, Set<Type> chunk) {
        int[] effCountsA = new int[Effectiveness.values().length];
        int[] effCountsB = new int[Effectiveness.values().length];
        for (Type t : chunk) {
            effCountsA[typeTable.getEffectiveness(t, colA).ordinal()]++;
            effCountsB[typeTable.getEffectiveness(t, colB).ordinal()]++;
        }
        return Arrays.equals(effCountsA, effCountsB);
    }

    private void swapTypeTableChunk(TypeTable typeTable, Type colA, Type colB, Set<Type> chunk) {
        for (Type t : chunk) {
            Effectiveness storage = typeTable.getEffectiveness(t, colA);
            typeTable.setEffectiveness(t, colA, typeTable.getEffectiveness(t, colB));
            typeTable.setEffectiveness(t, colB, storage);
        }
    }

    public void invertTypeEffectiveness(boolean randomImmunities) {
        TypeTable typeTable = romHandler.getTypeTable();
        int immCount = 0;
        LinkedList<Type[]> sePairs = new LinkedList<>();
        for (Type attacker : typeTable.getTypes()) {
            for (Type defender : typeTable.getTypes()) {

                Effectiveness eff = typeTable.getEffectiveness(attacker, defender);
                if (eff == Effectiveness.ZERO) {
                    typeTable.setEffectiveness(attacker, defender, Effectiveness.DOUBLE);
                    immCount++;
                } else if (eff == Effectiveness.HALF) {
                    typeTable.setEffectiveness(attacker, defender, Effectiveness.DOUBLE);
                } else if (eff == Effectiveness.DOUBLE) {
                    typeTable.setEffectiveness(attacker, defender, Effectiveness.HALF);
                    sePairs.add(new Type[]{attacker, defender});
                }
            }
        }
        if (randomImmunities) {
            if (sePairs.size() < immCount) {
                throw new RuntimeException("Too few super-effectives to turn into immunities...");
            }
            for (int i = 0; i < immCount; i++) {
                Type[] sePair = sePairs.remove(random.nextInt(sePairs.size()));
                typeTable.setEffectiveness(sePair[0], sePair[1], Effectiveness.ZERO);
            }
        }

        romHandler.setTypeTable(typeTable);
        changesMade = true;
    }
}