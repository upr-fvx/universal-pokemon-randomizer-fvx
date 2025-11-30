package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  BattleStyle.java - Wraps up information regarding a trainer's style   --*/
/*--                  of battle. Held by Settings for the UI options, and   --*/
/*--                  by each trainer for their individual config.          --*/
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

public class BattleStyle {
    public enum Modification {
        UNCHANGED, RANDOM, SINGLE_STYLE
    }

    private Modification selectedMod;

    public enum Style {
        SINGLE_BATTLE, DOUBLE_BATTLE, TRIPLE_BATTLE, ROTATION_BATTLE
    }

    private Style selectedStyle;

    public BattleStyle() {
        this.selectedMod = Modification.UNCHANGED;
        this.selectedStyle = Style.SINGLE_BATTLE;
    }

    public BattleStyle(Modification mod, Style style) {
        this.selectedMod = mod;
        this.selectedStyle = style;
    }

    public Modification getModification() {
        return this.selectedMod;
    }

    public void setModification(Modification mod) {
        this.selectedMod = mod;
    }

    public Style getStyle() {
        return this.selectedStyle;
    }

    public void setStyle(Style style) {
        this.selectedStyle = style;
    }

    public boolean isBattleStyleChanged() {
        return this.selectedMod != BattleStyle.Modification.UNCHANGED;
    }

    public boolean isOnlyMultiBattles() {
        if (this.selectedMod != Modification.SINGLE_STYLE)
            return false;
        return this.selectedStyle != Style.SINGLE_BATTLE;
    }

    public int getRequiredPokemonCount() {
        // Return how many pokemon a trainer should have for their selected style.
        // If unchanged, return -1 to signify no change.
        if (!isBattleStyleChanged())
            return -1;

        switch (this.selectedStyle) {
            case DOUBLE_BATTLE:
                return 2;
            case TRIPLE_BATTLE:
            case ROTATION_BATTLE:
                return 3;
        }
        return 1;
    }
}