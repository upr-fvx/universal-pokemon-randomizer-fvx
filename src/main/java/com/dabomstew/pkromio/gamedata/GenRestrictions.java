package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
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

import com.dabomstew.pkromio.services.RestrictedSpeciesService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of Generations whose {@link Species} are "allowed".
 * To be used with {@link RestrictedSpeciesService}.
 */
public class GenRestrictions {

    public static final int MAX_GENERATION = 7;
    private final boolean[] gensAllowed = new boolean[MAX_GENERATION];

    private boolean allowEvolutionaryRelatives;

    /**
     * Creates a GenRestrictions that allows everything.
     */
    public GenRestrictions() {
        allowEvolutionaryRelatives = true;
        Arrays.fill(gensAllowed, true);
    }

    public GenRestrictions(int state) {
        allowEvolutionaryRelatives = (state & 1) > 0;
        for (int gen = 1; gen <= MAX_GENERATION; gen++) {
            setGenAllowed(gen, (state & (1 << gen)) > 0);
        }
    }

    public boolean nothingSelected() {
        for (boolean genAllowed : gensAllowed) {
            if (genAllowed) {
                return false;
            }
        }
        return true;
    }

    public int toInt() {
        return makeIntSelected(
                allowEvolutionaryRelatives,
                isGenAllowed(1), isGenAllowed(2), isGenAllowed(3), isGenAllowed(4),
                isGenAllowed(5), isGenAllowed(6), isGenAllowed(7)
        );
    }

    private int makeIntSelected(boolean... switches) {
        if (switches.length > 32) {
            // No can do
            return 0;
        }
        int initial = 0;
        int state = 1;
        for (boolean b : switches) {
            initial |= b ? state : 0;
            state *= 2;
        }
        return initial;
    }

    public boolean isGenAllowed(int gen) {
        return gensAllowed[gen - 1];
    }

    public void setGenAllowed(int gen, boolean allowed) {
        gensAllowed[gen - 1] = allowed;
    }

    public void limitToGen(int gen) {
        for (int i = MAX_GENERATION; i > gen; i--) {
            setGenAllowed(i, false);
        }
    }

    public boolean isAllowEvolutionaryRelatives() {
        return allowEvolutionaryRelatives;
    }

    public void setAllowEvolutionaryRelatives(boolean allowEvolutionaryRelatives) {
        this.allowEvolutionaryRelatives = allowEvolutionaryRelatives;
    }

    // TODO: this should *not* be here
    public boolean allowTrainerSwapMegaEvolvables(boolean isXY, boolean isTypeThemedTrainers) {
        if (isTypeThemedTrainers) {
            return megaEvolutionsOfEveryTypeAreInPool(isXY);
        } else {
            return megaEvolutionsAreInPool(isXY);
        }
    }

    public boolean megaEvolutionsOfEveryTypeAreInPool(boolean isXY) {
        Set<Type> typePool = new HashSet<>();
        if (isGenAllowed(1)) {
            typePool.addAll(Arrays.asList(Type.GRASS, Type.POISON, Type.FIRE, Type.FLYING, Type.WATER, Type.PSYCHIC,
                    Type.GHOST, Type.NORMAL, Type.BUG, Type.ROCK));
        }
        if (isGenAllowed(2)) {
            typePool.addAll(Arrays.asList(Type.ELECTRIC, Type.BUG, Type.STEEL, Type.FIGHTING, Type.DARK,
                    Type.FIRE, Type.ROCK));
            if (!isXY) {
                typePool.add(Type.GROUND);
            }
        }
        if (isGenAllowed(3)) {
            typePool.addAll(Arrays.asList(Type.FIRE, Type.FIGHTING, Type.PSYCHIC, Type.FAIRY, Type.STEEL, Type.ROCK,
                    Type.ELECTRIC, Type.GHOST, Type.DARK, Type.DRAGON));
            if (!isXY) {
                typePool.addAll(Arrays.asList(Type.GRASS, Type.WATER, Type.GROUND, Type.FLYING, Type.ICE));
            }
        }
        if (isGenAllowed(4)) {
            typePool.addAll(Arrays.asList(Type.DRAGON, Type.GROUND, Type.FIGHTING, Type.STEEL, Type.GRASS, Type.ICE));
            if (!isXY) {
                typePool.addAll(Arrays.asList(Type.NORMAL, Type.PSYCHIC));
            }
        }
        if (isGenAllowed(5) && !isXY) {
            typePool.add(Type.NORMAL);
        }
        if (isGenAllowed(6) && !isXY) {
            typePool.addAll(Arrays.asList(Type.ROCK, Type.FAIRY));
        }
        return typePool.size() == 18;
    }

    public boolean megaEvolutionsAreInPool(boolean isXY) {
        if (isXY) {
            return isGenAllowed(1) || isGenAllowed(2) || isGenAllowed(3) || isGenAllowed(4);
        } else {
            return isGenAllowed(1) || isGenAllowed(2) || isGenAllowed(3) || isGenAllowed(4)
                    || isGenAllowed(5) || isGenAllowed(6);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GenRestrictions[");
        sb.append("gens allowed:");
        if (nothingSelected()) {
            sb.append("none ");
        }
        for (int gen = 1; gen <= MAX_GENERATION; gen++) {
            if (isGenAllowed(gen)) {
                sb.append(gen).append(" ");
            }
        }
        sb.append("allow evolutionary relatives=");
        sb.append(allowEvolutionaryRelatives);
        sb.append("]");
        return sb.toString();
    }
}
