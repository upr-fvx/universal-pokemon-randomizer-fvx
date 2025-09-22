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

    // only relevant for Gen 7
    private int forme;

    public Evolution(Species from, Species to, EvolutionType type, int extra) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.extraInfo = extra;
    }

    public Evolution(Evolution original) {
        this.from = original.from;
        this.to = original.to;
        this.type = original.type;
        this.extraInfo = original.extraInfo;
        this.forme = original.forme;
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
     * Returns the {@link Species} this Evolution is "from".<br>
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

    public int getForme() {
        return forme;
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
                String.format("(%s->%s, %s, extraInfo:%d)", from.getFullName(), to.getFullName(),
                        type, extraInfo) :
                String.format("(%s->%s, %s, extraInfo:%d, forme:%d)", from.getFullName(), to.getFullName(),
                        type, extraInfo, forme);
    }
}
