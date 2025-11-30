package com.dabomstew.pkromio.gamedata;

import java.util.*;

import static com.dabomstew.pkromio.gamedata.Effectiveness.*;

/**
 * Keeps track of all relevant {@link Type}s and the {@link Effectiveness}-es between them.
 */
public class TypeTable {

    private static final List<Effectiveness> VALID_EFFECTIVENESSES = Collections.unmodifiableList(Arrays.asList(
            ZERO, HALF, NEUTRAL, DOUBLE
    ));

    // Each TypeTable has its own mapping of Types to indexes in the map.
    // This is both so the actual table only has to be types.length^2, instead of [AllTypes]^2,
    // but also to make this class less dependent on the internal structure of the Type class.
    private final List<Type> types;
    private final Effectiveness[][] effectivenesses;
    private final Map<Type, Integer> typeIndexMap;

    /**
     * Creates a new TypeTable.
     * @param types A list of all relevant {@link Type}s.
     */
    public TypeTable(List<Type> types) {
        if (types.stream().distinct().count() != types.size()) {
            throw new IllegalArgumentException("Types must be unique");
        }
        this.types = Collections.unmodifiableList(types);
        this.effectivenesses = new Effectiveness[types.size()][types.size()];
        this.typeIndexMap = new EnumMap<>(Type.class);
        for (int i = 0; i < types.size(); i++) {
            typeIndexMap.put(types.get(i), i);
        }
        for (Effectiveness[] row : effectivenesses) {
            Arrays.fill(row, NEUTRAL);
        }
    }

    public TypeTable(TypeTable original) {
        this.types = new ArrayList<>(original.types);
        this.typeIndexMap = new HashMap<>(original.typeIndexMap);
        this.effectivenesses = new Effectiveness[types.size()][types.size()];
        for (int row = 0; row < types.size(); row++) {
            effectivenesses[row] = Arrays.copyOf(original.effectivenesses[row], types.size());
        }
    }

    /**
     * Creates a new TypeTable using a pre-made Effectiveness[][] table.
     *
     * @param types A list of all relevant {@link Type}s.
     * @param effectivenesses Must have the dimensions ({@code types.length})x({@code types.length}).<br>
     *                        The first index/rows of the table represents the attacker type, and the second
     *                        index/columns represents the defender type. Each row/column follows the same order
     *                        of the types as given in {@code types}. I.e. {@code effectivenesses[i][j]} represents
     *                        the {@link Effectiveness} when a move of the type at index {@code i} is used against a
     *                        Pokemon of the type at index {@code j}.
     */
    private TypeTable(List<Type> types, Effectiveness[][] effectivenesses) {
        this(types);
        checkDimensions(types, effectivenesses);
        for (int attIndex = 0; attIndex < types.size(); attIndex++) {
            for (int defIndex = 0; defIndex < types.size(); defIndex++) {
                Effectiveness eff = effectivenesses[attIndex][defIndex];
                setEffectiveness(types.get(attIndex), types.get(defIndex), eff);
            }
        }
    }

    private static void checkDimensions(List<Type> types, Effectiveness[][] effectivenesses) {
        if (effectivenesses.length != types.size()) {
            throw new IllegalArgumentException("Wrong dimensions of effectivenesses table, should be " +
                    types.size() + "x" + types.size());
        }
        for (Effectiveness[] row : effectivenesses) {
            if (row.length != types.size()) {
                throw new IllegalArgumentException("Wrong dimensions of effectivenesses table, should be " +
                        types.size() + "x" + types.size());
            }
        }
    }

    /**
     * Returns an unmodifiable {@link List} of the types used by the TypeTable.
     */
    public List<Type> getTypes() {
        return types;
    }

    private void validTypeCheck(Type t) {
        if (!typeIndexMap.containsKey(t)) {
            throw new IllegalArgumentException("Type " + t + " not supported by this TypeTable.");
        }
    }

    private void validEffectivenessCheck(Effectiveness eff) {
        if (!VALID_EFFECTIVENESSES.contains(eff)) {
            throw new IllegalArgumentException("Invalid Effectiveness: " + eff
                    + " (must be ZERO, HALF, NEUTRAL, or DOUBLE)");
        }
    }

    public void setEffectiveness(Type attacker, Type defender, Effectiveness effectiveness) {
        validTypeCheck(attacker);
        validTypeCheck(defender);
        validEffectivenessCheck(effectiveness);
        effectivenesses[typeIndexMap.get(attacker)][typeIndexMap.get(defender)] = effectiveness;
    }

    public Effectiveness getEffectiveness(Type attacker, Type defender) {
        validTypeCheck(attacker);
        validTypeCheck(defender);
        return effectivenesses[typeIndexMap.get(attacker)][typeIndexMap.get(defender)];
    }

    public Map<Type, Effectiveness> against(Type defenderPrimary, Type defenderSecondary) {
        validTypeCheck(defenderPrimary);
        if (defenderSecondary != null) {
            validTypeCheck(defenderSecondary);
        }
        Map<Type, Effectiveness> results = new HashMap<>();
        for (int i = 0; i < types.size(); i++) {
            Effectiveness eff = effectivenesses[i][typeIndexMap.get(defenderPrimary)];
            if (defenderSecondary != null) {
                eff = eff.combine(effectivenesses[i][typeIndexMap.get(defenderSecondary)]);
            }
            results.put(types.get(i), eff);
        }
        return results;
    }

    /**
     * Returns a {@link List} of all {@link Type}s where the effectiveness lines up for the attacker. A more general
     * method encompassing {@link #immuneWhenAttacking(Type)}, {@link #notVeryEffectiveWhenAttacking(Type)},
     * {@link #superEffectiveWhenAttacking(Type)}.
     */
    public List<Type> whenAttacking(Type attacker, Effectiveness effectiveness) {
        validTypeCheck(attacker);
        validEffectivenessCheck(effectiveness);
        List<Type> results = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            Effectiveness eff = effectivenesses[typeIndexMap.get(attacker)][i];
            if (eff == effectiveness) {
                results.add(types.get(i));
            }
        }
        return results;
    }

    /**
     * Returns a {@link List} of all {@link Type}s where the effectiveness lines up for the defender. A more general
     * method encompassing {@link #immuneWhenDefending(Type)}, {@link #notVeryEffectiveWhenDefending(Type)},
     * {@link #superEffectiveWhenDefending(Type)}.
     */
    public List<Type> whenDefending(Type defender, Effectiveness effectiveness) {
        validTypeCheck(defender);
        validEffectivenessCheck(effectiveness);
        List<Type> results = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            Effectiveness eff = effectivenesses[i][typeIndexMap.get(defender)];
            if (eff == effectiveness) {
                results.add(types.get(i));
            }
        }
        return results;
    }

    /**
     * Returns a {@link List} of all {@link Type}s that are immune to the attacker.<br>
     * E.g. calling this on a vanilla (Gen 2+) type table with attacker==GHOST, would give you (NORMAL).
     */
    public List<Type> immuneWhenAttacking(Type attacker) {
        return whenAttacking(attacker, ZERO);
    }

    /**
     * Returns a {@link List} of all {@link Type}s the attacker is not very effective against.<br>
     * E.g. calling this on a vanilla type table with attacker==FIRE, would give you (WATER, FIRE, ROCK, DRAGON).
     */
    public List<Type> notVeryEffectiveWhenAttacking(Type attacker) {
        return whenAttacking(attacker, HALF);
    }

    /**
     * Returns a {@link List} of all {@link Type}s the attacker is super effective against.<br>
     * E.g. calling this on a vanilla (Gen 2+) type table with attacker==FIRE, would give you (GRASS, BUG, ICE, STEEL).
     */
    public List<Type> superEffectiveWhenAttacking(Type attacker) {
        return whenAttacking(attacker, DOUBLE);
    }

    /**
     * Returns a {@link List} of all {@link Type}s that the defender is immune to.<br>
     * E.g. calling this on a vanilla type table with defender==GHOST, would give you (NORMAL, FIGHTING).
     */
    public List<Type> immuneWhenDefending(Type defender) {
        return whenDefending(defender, ZERO);
    }

    /**
     * Returns a {@link List} of all {@link Type}s that are not very effective against the defender.<br>
     * E.g. calling this on a vanilla (Gen 6+) type table with defender==FIRE, would give you (GRASS, FIRE, ICE, STEEL, FAIRY).
     */
    public List<Type> notVeryEffectiveWhenDefending(Type defender) {
        return whenDefending(defender, HALF);
    }

    /**
     * Returns a {@link List} of all {@link Type}s that are super effective against the defender.<br>
     * E.g. calling this on a vanilla type table with defender==FIRE, would give you (WATER, ROCK, GROUND).
     */
    public List<Type> superEffectiveWhenDefending(Type defender) {
        return whenDefending(defender, DOUBLE);
    }

    /**
     * Returns the number of {@link Effectiveness}es in the table, that are not {@link Effectiveness#NEUTRAL}.
     */
    public int nonNeutralEffectivenessCount() {
        int count = 0;
        for (Effectiveness[] row : effectivenesses) {
            for (Effectiveness eff : row) {
                if (eff != NEUTRAL) count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TypeTable) {
            TypeTable other = (TypeTable) o;
            return equalTypes(other) && equalEffectivenesses(other);
        }
        return false;
    }

    private boolean equalTypes(TypeTable other) {
        return new HashSet<>(other.types).equals(new HashSet<>(types));
    }

    private boolean equalEffectivenesses(TypeTable other) {
        for (Type attacker : types) {
            for (Type defender : types) {
                Effectiveness eff = getEffectiveness(attacker, defender);
                Effectiveness otherEff = other.getEffectiveness(attacker, defender);
                if (eff != otherEff) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(types);
    }

    @Override
    public String toString() {
        return "TypeTable(#types=" + types.size() + " , #nonNeutral=" + nonNeutralEffectivenessCount() + ", types="
                + types + ")";
    }

    private static final String[] DEFAULT_EFFECTIVENESS_SYMBOLS = new String[] {"Imm", "nve", "---", "SE!"};

    /**
     * Returns the TypeTable as a String formatted as... a table.
     * The attacking Types on the left, the defending Types on top.
     */
    public String toBigString() {
        // this method is practical for testing and logging both,
        // but in the latter case we want to be able to customize the "symbols"
        return toBigString(DEFAULT_EFFECTIVENESS_SYMBOLS);
    }

    public String toBigString(String[] effectivenessSymbols) {
        StringBuilder sb = new StringBuilder();
        int maxNameLength = 0;
        for (Type t : getTypes()) {
            maxNameLength = Math.max(maxNameLength, t.name().length());
        }

        for (int i = 0; i < 3; i+= 3) {
            sb.append(new String(new char[maxNameLength]).replace("\0", " "));
            for (Type defender : types) {
                sb.append(defender.name().length() > i ? defender.name().charAt(i) : " ");
                sb.append(defender.name().length() > i + 1 ? defender.name().charAt(i + 1) : " ");
                sb.append(defender.name().length() > i + 2 ? defender.name().charAt(i + 2) : " ");
                sb.append(" ");
            }
            sb.append("\n");
        }

        for (Type attacker : getTypes()) {
            sb.append(attacker.name());
            sb.append(new String(new char[maxNameLength - attacker.name().length()]).replace("\0", " "));
            for (Type defender : getTypes()) {
                sb.append("|");
                Effectiveness eff = getEffectiveness(attacker, defender);
                sb.append(effectivenessSymbols[eff.ordinal()]);
            }
            sb.append("|");
            sb.append("\n");
        }

        return sb.toString();
    }

    public static TypeTable getVanillaGen1Table() {
        return new TypeTable(gen1Types, gen1Table);
    }

    public static TypeTable getVanillaGen2To5Table() {
        return new TypeTable(gen2To5Types, gen2To5Table);
    }

    public static TypeTable getVanillaGen6PlusTable() {
        return new TypeTable(gen6PlusTypes, gen6PlusTable);
    }

    private static final List<Type> gen1Types = Collections.unmodifiableList(Arrays.asList(
            Type.NORMAL, Type.FIGHTING, Type.FLYING, Type.GRASS, Type.WATER, Type.FIRE, Type.ROCK, Type.GROUND,
            Type.PSYCHIC, Type.BUG, Type.DRAGON, Type.ELECTRIC, Type.GHOST, Type.POISON, Type.ICE
    ));
    private static final Effectiveness[][] gen1Table = {
            /*            NORMAL,FIGHTING, FLYING,   GRASS ,   WATER,   FIRE ,   ROCK , GROUND,  PSYCHIC,   BUG  ,  DRAGON,ELECTRIC,   GHOST , POISON,   ICE  */
            /*NORMAL */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL},
            /*FIGHTING*/{ DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL,    ZERO,    HALF,  DOUBLE},
            /*FLYING */ {NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL},
            /*GRASS  */ {NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE,    HALF,  DOUBLE,  DOUBLE, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL,    HALF, NEUTRAL},
            /*WATER  */ {NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL},
            /*FIRE   */ {NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,    HALF,    HALF,    HALF, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE},
            /*ROCK   */ {NEUTRAL,    HALF,  DOUBLE, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE},
            /*GROUND */ {NEUTRAL, NEUTRAL,    ZERO,    HALF, NEUTRAL,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL},
            /*PSYCHIC*/ {NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL},
            /*BUG    */ {NEUTRAL,    HALF,    HALF,  DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,  DOUBLE, NEUTRAL},
            /*DRAGON */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL},
            /*ELECTRIC*/{NEUTRAL, NEUTRAL,  DOUBLE,    HALF,  DOUBLE, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL, NEUTRAL},
            /*GHOST  */ {   ZERO, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL},
            /*POISON */ {NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL},
            /*ICE    */ {NEUTRAL, NEUTRAL,  DOUBLE,  DOUBLE,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF},
    };

    private static final List<Type> gen2To5Types = Collections.unmodifiableList(Arrays.asList(
            Type.NORMAL, Type.FIGHTING, Type.FLYING, Type.GRASS, Type.WATER, Type.FIRE, Type.ROCK, Type.GROUND,
            Type.PSYCHIC, Type.BUG, Type.DRAGON, Type.ELECTRIC, Type.GHOST, Type.POISON, Type.ICE, Type.STEEL, Type.DARK
    ));
    private static final Effectiveness[][] gen2To5Table = {
        /*            NORMAL,FIGHTING, FLYING,   GRASS ,   WATER,   FIRE ,   ROCK , GROUND,  PSYCHIC,   BUG  ,  DRAGON,ELECTRIC,   GHOST , POISON,   ICE  ,  STEEL ,  DARK  */
        /*NORMAL */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL,    HALF, NEUTRAL},
        /*FIGHTING*/{ DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL,    ZERO,    HALF,  DOUBLE,  DOUBLE,  DOUBLE},
        /*FLYING */ {NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL},
        /*GRASS  */ {NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE,    HALF,  DOUBLE,  DOUBLE, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,    HALF, NEUTRAL},
        /*WATER  */ {NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL},
        /*FIRE   */ {NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,    HALF,    HALF,    HALF, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,  DOUBLE, NEUTRAL},
        /*ROCK   */ {NEUTRAL,    HALF,  DOUBLE, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL},
        /*GROUND */ {NEUTRAL, NEUTRAL,    ZERO,    HALF, NEUTRAL,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL},
        /*PSYCHIC*/ {NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF,    ZERO},
        /*BUG    */ {NEUTRAL,    HALF,    HALF,  DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL,    HALF,  DOUBLE},
        /*DRAGON */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL},
        /*ELECTRIC*/{NEUTRAL, NEUTRAL,  DOUBLE,    HALF,  DOUBLE, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL},
        /*GHOST  */ {   ZERO, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF,    HALF},
        /*POISON */ {NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL,    ZERO, NEUTRAL},
        /*ICE    */ {NEUTRAL, NEUTRAL,  DOUBLE,  DOUBLE,    HALF,    HALF, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL},
        /*STEEL  */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL},
        /*DARK   */ {NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF,    HALF},
    };

    private static final List<Type> gen6PlusTypes = Collections.unmodifiableList(Arrays.asList(
            Type.NORMAL, Type.FIGHTING, Type.FLYING, Type.GRASS, Type.WATER, Type.FIRE, Type.ROCK, Type.GROUND,
            Type.PSYCHIC, Type.BUG, Type.DRAGON, Type.ELECTRIC, Type.GHOST, Type.POISON, Type.ICE, Type.STEEL,
            Type.DARK, Type.FAIRY
    ));
    private static final Effectiveness[][] gen6PlusTable = {
            /*            NORMAL,FIGHTING, FLYING,   GRASS ,   WATER,   FIRE ,   ROCK , GROUND,  PSYCHIC,   BUG  ,  DRAGON,ELECTRIC,   GHOST , POISON,   ICE  ,  STEEL ,  DARK  , FAIRY */
            /*NORMAL */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL},
            /*FIGHTING*/{ DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL,    ZERO,    HALF,  DOUBLE,  DOUBLE,  DOUBLE,    HALF},
            /*FLYING */ {NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL},
            /*GRASS  */ {NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE,    HALF,  DOUBLE,  DOUBLE, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,    HALF, NEUTRAL, NEUTRAL},
            /*WATER  */ {NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL},
            /*FIRE   */ {NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,    HALF,    HALF,    HALF, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL},
            /*ROCK   */ {NEUTRAL,    HALF,  DOUBLE, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL, NEUTRAL},
            /*GROUND */ {NEUTRAL, NEUTRAL,    ZERO,    HALF, NEUTRAL,  DOUBLE,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL},
            /*PSYCHIC*/ {NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL,    HALF,    ZERO, NEUTRAL},
            /*BUG    */ {NEUTRAL,    HALF,    HALF,  DOUBLE, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL,    HALF,  DOUBLE,    HALF},
            /*DRAGON */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,    ZERO},
            /*ELECTRIC*/{NEUTRAL, NEUTRAL,  DOUBLE,    HALF,  DOUBLE, NEUTRAL, NEUTRAL,    ZERO, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL},
            /*GHOST  */ {   ZERO, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL},
            /*POISON */ {NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL,    ZERO, NEUTRAL,  DOUBLE},
            /*ICE    */ {NEUTRAL, NEUTRAL,  DOUBLE,  DOUBLE,    HALF,    HALF, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF, NEUTRAL, NEUTRAL},
            /*STEEL  */ {NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL,  DOUBLE,    HALF, NEUTRAL,  DOUBLE},
            /*DARK   */ {NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF,    HALF},
            /*FAIRY  */ {NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL, NEUTRAL,    HALF, NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL,  DOUBLE, NEUTRAL, NEUTRAL,    HALF, NEUTRAL,    HALF,  DOUBLE, NEUTRAL},
    };


}
