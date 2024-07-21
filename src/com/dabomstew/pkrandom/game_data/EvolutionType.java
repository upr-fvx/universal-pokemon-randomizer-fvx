package com.dabomstew.pkrandom.game_data;

/*----------------------------------------------------------------------------*/
/*--  EvolutionType.java - describes what process is necessary for an       --*/
/*--                       evolution to occur                               --*/
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

public enum EvolutionType {

    LEVEL, STONE, TRADE, TRADE_ITEM, HAPPINESS, HAPPINESS_DAY, HAPPINESS_NIGHT, LEVEL_ATTACK_HIGHER,
    LEVEL_DEFENSE_HIGHER, LEVEL_ATK_DEF_SAME, LEVEL_LOW_PV, LEVEL_HIGH_PV, LEVEL_CREATE_EXTRA, LEVEL_IS_EXTRA,
    LEVEL_HIGH_BEAUTY, STONE_MALE_ONLY, STONE_FEMALE_ONLY, LEVEL_ITEM_DAY, LEVEL_ITEM_NIGHT, LEVEL_WITH_MOVE,
    LEVEL_WITH_OTHER, LEVEL_MALE_ONLY, LEVEL_FEMALE_ONLY, LEVEL_ELECTRIFIED_AREA, LEVEL_MOSS_ROCK, LEVEL_ICY_ROCK,
    TRADE_SPECIAL, FAIRY_AFFECTION, LEVEL_WITH_DARK, LEVEL_UPSIDE_DOWN, LEVEL_RAIN, LEVEL_DAY, LEVEL_NIGHT,
    LEVEL_FEMALE_ESPURR, LEVEL_GAME, LEVEL_DAY_GAME, LEVEL_NIGHT_GAME, LEVEL_SNOWY, LEVEL_DUSK, LEVEL_NIGHT_ULTRA,
    STONE_ULTRA, NONE;

    public boolean usesLevel() {
        return (this == LEVEL) || (this == LEVEL_ATTACK_HIGHER) || (this == LEVEL_DEFENSE_HIGHER)
                || (this == LEVEL_ATK_DEF_SAME) || (this == LEVEL_LOW_PV) || (this == LEVEL_HIGH_PV)
                || (this == LEVEL_CREATE_EXTRA) || (this == LEVEL_IS_EXTRA) || (this == LEVEL_MALE_ONLY)
                || (this == LEVEL_FEMALE_ONLY) || (this == LEVEL_WITH_DARK)|| (this == LEVEL_UPSIDE_DOWN)
                || (this == LEVEL_RAIN) || (this == LEVEL_DAY)|| (this == LEVEL_NIGHT)|| (this == LEVEL_FEMALE_ESPURR)
                || (this == LEVEL_GAME) || (this == LEVEL_DAY_GAME) || (this == LEVEL_NIGHT_GAME)
                || (this == LEVEL_SNOWY) || (this == LEVEL_DUSK) || (this == LEVEL_NIGHT_ULTRA);
    }

    public boolean skipSplitEvo() {
        return (this == LEVEL_HIGH_BEAUTY) || (this == LEVEL_NIGHT_ULTRA) || (this == STONE_ULTRA);
    }
}
