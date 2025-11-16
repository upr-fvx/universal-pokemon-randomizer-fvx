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

/**
 * Represents an evolution between two {@link Species}.
 */
public class Evolution implements Comparable<Evolution> {
    //TODO: make this unmodifiable after creation
    private Species from;
    private Species to;
    private EvolutionType type;
    private int extraInfo;
    private int estimatedEvoLvl;

    // only relevant for Gen 7
    private int forme;

    public Evolution(Species from, Species to, EvolutionType type, int extra) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.extraInfo = extra;
        // TODO change the following to usesLevelGreaterZero after merge of the Crabrawler bugfix
        if (type.usesLevel()) {
            this.estimatedEvoLvl = extra;
        }
    }

    public Evolution(Evolution original) {
        this.from = original.from;
        this.to = original.to;
        this.type = original.type;
        this.extraInfo = original.extraInfo;
        this.estimatedEvoLvl = original.estimatedEvoLvl;
        this.forme = original.forme;
    }

    public Evolution(Species from, Species to, EvolutionType type, int extra, int estimatedEvoLvl) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.extraInfo = extra;
        this.estimatedEvoLvl = estimatedEvoLvl;
    }

    /**
     * Returns the {@link Species} this Evolution is "from".<br>
     * E.g. for the Evolution "Bulbasaur->Ivysaur" this would return Bulbasaur.
     */
    public Species getFrom() {
        return from;
    }

    public void setFrom(Species from) {
        this.from = from;
    }

    /**
     * Returns the {@link Species} this Evolution is "to".<br>
     * E.g. for the Evolution "Bulbasaur->Ivysaur" this would return Ivysaur.
     */
    public Species getTo() {
        return to;
    }

    public void setTo(Species to) {
        this.to = to;
    }

    public EvolutionType getType() {
        return type;
    }

    public void setType(EvolutionType type) {
        this.type = type;
    }

    public int getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(int extraInfo) {
        this.extraInfo = extraInfo;
    }

    /**
     * Returns the estimated evolution level, which either equals chosenEvo.getExtraInfo() if
     * chosenEvo.getType().usesLevelThreshold() == true, or was estimated from the evolution levels of all original
     * level-up evolutions in the ROM.
     *
     * @return The estimated evolution level of the evolution.
     */
    public int getEstimatedEvoLvl() {
        return estimatedEvoLvl;
    }

    public void setEstimatedEvoLvl(int estimatedEvoLvl) {
        this.estimatedEvoLvl = estimatedEvoLvl;
    }

    public int getForme() {
        return forme;
    }

    public void updateEvolutionMethod(EvolutionType type, int extraInfo) {
        EvolutionType oldType = this.type;
        boolean oldTypeUsesLevelGreaterZero = oldType.usesLevel(); // TODO GreaterZero
        boolean newTypeUsesLevelGreaterZero = type.usesLevel(); // TODO GreaterZero

        this.type = type;
        this.extraInfo = extraInfo;
        // Keep estimatedEvoLevel if both old and new type does not use evo level > 0.
        // Keep estimatedEvoLevel if old type used evo level > 0 but new method does not, i.e., continue to enable
        // level up evolutions if needed, e.g. for trainers evolve their pokemon.
        // Only update estimatedEvoLvl if type changes from to evo that uses level > 0 to continue guaranteeing that
        // extraInfo and estimatedEvoLvl are equal for evos that use level > 0.
        // Apart from that, make sure that in any of the above cases, estimatedEvoLvl and extraInfo are the same for
        // evolutions that use a evo level > 0.
        if (newTypeUsesLevelGreaterZero && (!oldTypeUsesLevelGreaterZero || (this.extraInfo != this.estimatedEvoLvl))) { // TODO do not use bools above if it's only one
            this.estimatedEvoLvl = extraInfo;
        }
    }

    public void updateEvolutionMethod(EvolutionType type, int extraInfo, boolean useEstimatedLevels) {
        this.updateEvolutionMethod(type, (useEstimatedLevels && type.usesLevel())  // TODO ...GreaterZero()
                ? this.estimatedEvoLvl : extraInfo);
    }

    public void setForme(int forme) {
        this.forme = forme;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from.getNumber();
        result = prime * result + to.getNumber();
        result = prime * result + type.ordinal();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Evolution other = (Evolution) obj;
        return type == other.type && extraInfo == other.extraInfo && from.equals(other.from) && to.equals(other.to);
    }

    @Override
    public int compareTo(Evolution o) {
        if (this.from.getNumber() < o.from.getNumber()) {
            return -1;
        } else if (this.from.getNumber() > o.from.getNumber()) {
            return 1;
        } else if (this.to.getNumber() < o.to.getNumber()) {
            return -1;
        } else if (this.to.getNumber() > o.to.getNumber()) {
            return 1;
        } else return Integer.compare(this.type.ordinal(), o.type.ordinal());
    }

    @Override
    public String toString() {
        return forme == 0 ?
                String.format("(%s->%s, %s, extraInfo:%d, estimatedEvoLvl:%d)", from.getFullName(), to.getFullName(),
                        type, extraInfo, estimatedEvoLvl) :
                String.format("(%s->%s, %s, extraInfo:%d, estimatedEvoLvl:%d, forme:%d)", from.getFullName(), to.getFullName(),
                        type, extraInfo, estimatedEvoLvl, forme);
    }
}
