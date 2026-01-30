package com.dabomstew.pkromio.romio;

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

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A {@link FileFilter} for the various ROM files.
 */
public class ROMFilter extends FileFilter {

    private static final String[][] EXTENSIONS = new String[][]{
            {"gb", "sgb", "gbc"},
            {"gb", "sgb", "gbc"},
            {"gba"},
            {"nds"},
            {"nds"},
            {"3ds", "cci", "cxi", "cia", "app"},
            {"3ds", "cci", "cxi", "cia", "app"}
    };
    private static final int MAX_GENERATION = 7;

    private final int[] allowedGenerations;

    /**
     * Creates a ROMFilter, allowing ROMs of all Generations to be seen/selected.
     */
    public ROMFilter() {
        this(IntStream.rangeClosed(1, MAX_GENERATION).toArray());
    }

    /**
     * Creates a ROMFilter, allowing only ROMs of select Generations to be seen/selected.
     *
     * @param allowedGenerations The allowed Generations. "1" means Gen 1, etc.
     */
    public ROMFilter(int... allowedGenerations) {
        if (allowedGenerations.length == 0) {
            throw new IllegalArgumentException("allowedGenerations can't be empty");
        }
        for (int gen : allowedGenerations) {
            if (gen < 1 || gen > MAX_GENERATION) {
                throw new IllegalArgumentException(gen + " is not a valid Generation");
            }
        }
        this.allowedGenerations = allowedGenerations;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true; // needed to allow directory navigation
        }
        String filename = f.getName();
        if (!filename.contains(".")) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        for (int gen : allowedGenerations) {
            if (Arrays.asList(EXTENSIONS[gen - 1]).contains(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("Nintendo ");

        String gbPart = "";
        if (isAllowed(1) || isAllowed(2)) {
            if (isAllowed(3)) {
                gbPart = "GB(C/A)";
            } else {
                gbPart = "GB(C)";
            }
        } else if (isAllowed(3)) {
            gbPart = "GBA";
        }
        String dsPart = "";
        if (isAllowed(4) || isAllowed(5)) {
            if (isAllowed(6) || isAllowed(7)) {
                dsPart = "(3)DS";
            } else {
                dsPart = "DS";
            }
        } else if (isAllowed(6) || isAllowed(7)) {
            dsPart = "3DS";
        }

        sb.append(gbPart);
        if (!gbPart.isEmpty() && !dsPart.isEmpty()) {
            sb.append("/");
        }
        sb.append(dsPart);

        sb.append(" ROM File (");
        Set<String> extensions = new LinkedHashSet<>();
        for (int gen : allowedGenerations) {
            extensions.addAll(Arrays.asList(EXTENSIONS[gen - 1]));
        }
        sb.append(extensions.stream()
                .map(s -> "*." + s)
                .collect(Collectors.joining(",")));
        sb.append(")");

        return sb.toString();
    }

    private boolean isAllowed(int gen) {
        for (int allowed : allowedGenerations) {
            if (allowed == gen) {
                return true;
            }
        }
        return false;
    }

}
