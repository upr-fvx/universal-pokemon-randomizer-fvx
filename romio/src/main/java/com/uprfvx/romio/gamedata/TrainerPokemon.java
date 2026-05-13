package com.uprfvx.romio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  TrainerPokemon.java - represents a Pokemon owned by a trainer.        --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import java.util.Arrays;

/**
 * Represents a Pokemon owned by a {@link Trainer}. The {@link Species} and forme info is largely
 * held by a {@link SpeciesHolder}, but can also be gotten through {@link #getSpecies()}.
 */
public class TrainerPokemon {

    private final SpeciesHolder speciesHolder;

    private int level;

    private int[] moves = {0, 0, 0, 0};

    private Item heldItem;

    private boolean hasMegaStone;
    private boolean hasZCrystal;
    //TODO: change these to methods which determine at runtime

    private int abilitySlot;

    private int forcedGenderFlag;
    private byte nature;
    private byte hpEVs;
    private byte atkEVs;
    private byte defEVs;
    private byte spatkEVs;
    private byte spdefEVs;
    private byte speedEVs;
    private int IVs;
    // In gens 3-5, there is a byte or word that corresponds
    // to the IVs a trainer's pokemon has. In X/Y, this byte
    // also encodes some other information, possibly related
    // to EV spread. Because of the unknown part in X/Y,
    // we store the whole "strength byte" so we can
    // write it unchanged when randomizing trainer pokemon.
    private int strength;
    
    private boolean resetMoves = false;

    private boolean isAddedTeamMember = false;

    public TrainerPokemon(Species species, int level) {
        this.speciesHolder = new SpeciesHolder(species);
        this.level = level;
    }

    public TrainerPokemon(TrainerPokemon original) {
        speciesHolder = new SpeciesHolder(original.speciesHolder);

        level = original.level;

        moves = Arrays.copyOf(original.moves, 4);

        forcedGenderFlag = original.forcedGenderFlag;
        nature = original.nature;
        IVs = original.IVs;
        hpEVs = original.hpEVs;
        atkEVs = original.atkEVs;
        defEVs = original.defEVs;
        spatkEVs = original.spatkEVs;
        spdefEVs = original.spdefEVs;
        speedEVs = original.speedEVs;
        strength = original.strength;
        heldItem = original.heldItem;
        abilitySlot = original.abilitySlot;

        hasZCrystal = original.hasZCrystal;
        hasMegaStone = original.hasMegaStone;

        resetMoves = original.resetMoves;

        isAddedTeamMember = original.isAddedTeamMember;
    }

    public SpeciesHolder getSpeciesHolder() {
        return speciesHolder;
    }

    /**
     * Short for {@link #getSpeciesHolder()}.{@link SpeciesHolder#getSpecies() getSpecies()}
     */
    public Species getSpecies() {
        return speciesHolder.getSpecies();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int[] getMoves() {
        return moves;
    }

    public void setMoves(int[] moves) {
        this.moves = moves;
    }

    public Item getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(Item heldItem) {
        this.heldItem = heldItem;
    }

    public boolean hasMegaStone() {
        return hasMegaStone;
    }

    public void setHasMegaStone(boolean hasMegaStone) {
        this.hasMegaStone = hasMegaStone;
    }

    public boolean canMegaEvolve() {
        for (MegaEvolution mega: getSpecies().getMegaEvolutionsFrom()) {
            if (mega.isNeedsItem() && mega.getItem().equals(heldItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasZCrystal() {
        return hasZCrystal;
    }

    public void setHasZCrystal(boolean hasZCrystal) {
        this.hasZCrystal = hasZCrystal;
    }

    public boolean isAddedTeamMember() {
        return isAddedTeamMember;
    }

    public void setIsAddedTeamMember(boolean isAddedTeamMember) {
        this.isAddedTeamMember = isAddedTeamMember;
    }

    public int getAbilitySlot() {
        return abilitySlot;
    }

    public void setAbilitySlot(int abilitySlot) {
        this.abilitySlot = abilitySlot;
    }

    public int getForcedGenderFlag() {
        return forcedGenderFlag;
    }

    public void setForcedGenderFlag(int forcedGenderFlag) {
        this.forcedGenderFlag = forcedGenderFlag;
    }

    public byte getNature() {
        return nature;
    }

    public void setNature(byte nature) {
        this.nature = nature;
    }

    public byte getHpEVs() {
        return hpEVs;
    }

    public void setHpEVs(byte hpEVs) {
        this.hpEVs = hpEVs;
    }

    public byte getAtkEVs() {
        return atkEVs;
    }

    public void setAtkEVs(byte atkEVs) {
        this.atkEVs = atkEVs;
    }

    public byte getDefEVs() {
        return defEVs;
    }

    public void setDefEVs(byte defEVs) {
        this.defEVs = defEVs;
    }

    public byte getSpatkEVs() {
        return spatkEVs;
    }

    public void setSpatkEVs(byte spatkEVs) {
        this.spatkEVs = spatkEVs;
    }

    public byte getSpdefEVs() {
        return spdefEVs;
    }

    public void setSpdefEVs(byte spdefEVs) {
        this.spdefEVs = spdefEVs;
    }

    public byte getSpeedEVs() {
        return speedEVs;
    }

    public void setSpeedEVs(byte speedEVs) {
        this.speedEVs = speedEVs;
    }

    public int getIVs() {
        return IVs;
    }

    public void setIVs(int IVs) {
        this.IVs = IVs;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public boolean isResetMoves() {
        return resetMoves;
    }

    public void setResetMoves(boolean resetMoves) {
        this.resetMoves = resetMoves;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getSpecies().getFullName());
        if (heldItem != null) {
            sb.append("@").append(heldItem.getName());
        }
        sb.append(" Lv").append(level);
        return sb.toString();
    }
}
