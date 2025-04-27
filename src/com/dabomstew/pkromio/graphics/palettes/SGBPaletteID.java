package com.dabomstew.pkromio.graphics.palettes;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer" by Dabomstew                   --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2012.                                 --*/
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

import java.util.Random;

/**
 * The Super Gameboy palette IDs, names taken from
 * pret/pokered/constants/paletteconstants.asm
 */
public enum SGBPaletteID {
    ROUTE, PALLET, VIRIDIAN, PEWTER, CERULEAN, LAVENDER, VERMILION, CELADON, FUCHSIA, CINNABAR, INDIGO, SAFFRON,
    TOWNMAP, LOGO1, LOGO2, ZEROF, MEWMON, BLUEMON, REDMON, CYANMON, PURPLEMON, BROWNMON, GREENMON, PINKMON, YELLOWMON,
    GREYMON, SLOTS1, SLOTS2, SLOTS3, SLOTS4, BLACK, GREENBAR, YELLOWBAR, REDBAR, BADGE, CAVE, GAMEFREAK;

    public static SGBPaletteID getRandomPokemonPaletteID(Random random) {
        SGBPaletteID[] pokemonPaletteIDs = new SGBPaletteID[]{MEWMON, BLUEMON, REDMON, CYANMON, PURPLEMON, BROWNMON,
                GREENMON, PINKMON, YELLOWMON, GREYMON};
        return pokemonPaletteIDs[random.nextInt(pokemonPaletteIDs.length)];
    }
}
