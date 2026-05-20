package com.uprfvx.romio.gamedata;

/**
 * Holds a {@link Species} and possibly a forme number.<br>
 * This class is required since all formes found in-game are not represented
 * as Species objects. See the Species' class explanation on "true cosmetic formes".
 * However, objects of several classes such as {@link TrainerPokemon}, {@link StaticEncounter},
 * and {@link Encounter} wish to hold Species that might sometimes be "Flabébé-Blue".
 * <br><br>
 * Oftentimes alt formes are not allowed at all for a TrainerPokemon/StaticEncounter/etc...
 * {@link #setAltFormeAllowed()} and {@link #isAltFormeAllowed()} exist for when they do.
 * Note that this class defaults to alt formes NOT being allowed.
 */
public class SpeciesHolder {
    private Species baseSpecies;
    private int formeNumber = 0;
    private boolean altFormeAllowed;

    /**
     * Creates a SpeciesHolder for the given baseSpecies.
     * @param baseSpecies a species which is a base forme.
     * @throws NullPointerException if baseSpecies is null.
     * @throws IllegalArgumentException if baseSpecies is not a base forme.
     */
    public SpeciesHolder(Species baseSpecies) {
        if (!baseSpecies.isBaseForme()) {
            throw  new IllegalArgumentException(baseSpecies.getNumberAndFullName() + "is not a base forme.");
        }
        this.baseSpecies = baseSpecies;
    }

    public SpeciesHolder(SpeciesHolder original) {
        this.baseSpecies = original.baseSpecies;
        this.formeNumber = original.formeNumber;
        this.altFormeAllowed = original.altFormeAllowed;
    }

    public Species getSpecies() {
        return baseSpecies.getForme(formeNumber);
    }

    /**
     * Sets the species and formeNumber of this Encounter to correspond to the given Species.
     * @throws IllegalStateException if species is an alt forme, and alt formes are not allowed for this Encounter
     */
    public void setSpecies(Species species) {
        if (!species.isBaseForme() && !altFormeAllowed) {
            throw new IllegalArgumentException("Species " + species.getNumberAndFullName() +
                    " could not be set. Alt formes are not allowed.");
        }
        this.baseSpecies = species.getBaseForme();
        this.formeNumber = species.getFormeNumber();
    }

    public boolean isAltFormeAllowed() {
        return altFormeAllowed;
    }

    public void setAltFormeAllowed() {
        this.altFormeAllowed = true;
    }

    /**
     * Gets the forme number.
     * @throws IllegalStateException if alt formes are not allowed.
     */
    public int getFormeNumber() {
        if (!altFormeAllowed) {
            throw new IllegalArgumentException("Alt formes are not allowed.");
        }
        return formeNumber;
    }

    /**
     * Sets the forme number.
     * @param formeNumber The forme number to set.
     * @throws IllegalStateException if alt formes are not allowed.
     * @throws IllegalArgumentException if formeNumber is not a valid forme for {@link #baseSpecies}.
     */
    public void setFormeNumber(int formeNumber) {
        if (!altFormeAllowed) {
            throw new IllegalArgumentException("Alt formes are not allowed.");
        }
        if (!baseSpecies.isValidFormeNumber(formeNumber)) {
            throw new IllegalArgumentException("formeNumber=" + formeNumber + " is not valid for "
                    + baseSpecies.getNumberAndFullName());
        }
        this.formeNumber = formeNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpeciesHolder other) {
            return formeNumber == other.formeNumber && altFormeAllowed == other.altFormeAllowed
                    && baseSpecies.equals(other.baseSpecies);
        }
        return false;
    }
}
