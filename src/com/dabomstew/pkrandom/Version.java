package com.dabomstew.pkrandom;

/*----------------------------------------------------------------------------*/
/*--  Version.java - contains information about the randomizer's versions   --*/
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
import java.util.Collections;
import java.util.List;

public class Version {
    // TODO: come up with some more clever way to deal with versions; this one still falls flat with the
    //  forking-and-then-merging that is bound to happen with an open-source project like this.

    // LATEST_VERSION is a bit superfluous, but refactoring VERSION/VERSION_STRING felt like a project for later
    // id should increment by 1 for new version (note the current system has an upper limit of 999)

    // If creating a new fork, should "jump" the version number by some number of hundreds.
    // (This is not ideal, but it's better than sharing increments)
    public static final Version LATEST_VERSION = new Version(413, "1.3.1", "FVX");
    public static final int VERSION = LATEST_VERSION.id;
    public static final String VERSION_STRING = LATEST_VERSION.name;

    public final int id;
    public final String name;
    public final String branchName;

    private Version(int id, String name) {
        this(id, name, "");
    }

    private Version(int id, String name, String branchName) {
        this.id = id;
        this.name = name;
        this.branchName = branchName;
    }

    public static final Version v1_0_1a = new Version(100, "1.0.1a");
    public static final Version v1_0_2a = new Version(102, "1.0.2a");
    public static final Version v1_1_0 = new Version(110, "1.1.0");
    public static final Version v1_1_1 = new Version(111, "1.1.1");
    public static final Version v1_1_2 = new Version(112, "1.1.2");
    public static final Version v1_2_0a = new Version(120, "1.2.0a");
    public static final Version v1_5_0 = new Version(150, "1.5.0");
    public static final Version v1_6_0a = new Version(160, "1.6.0a");
    public static final Version v1_6_1 = new Version(161, "1.6.1");
    public static final Version v1_6_2 = new Version(162, "1.6.2");
    public static final Version v1_6_3b = new Version(163, "1.6.3b");
    public static final Version v1_7_0b = new Version(170, "1.7.0b");
    public static final Version v1_7_1 = new Version(171, "1.7.1");
    public static final Version v1_7_2 = new Version(172, "1.7.2");

    public static final Version ZX_3_0_0 = new Version(300, "3.0.0", "ZX");
    public static final Version ZX_3_1_0 = new Version(310, "3.1.0", "ZX");
    public static final Version ZX_4_0_0 = new Version(311, "4.0.0", "ZX");
    public static final Version ZX_4_0_1 = new Version(312, "4.0.1", "ZX");
    public static final Version ZX_4_0_2 = new Version(313, "4.0.2", "ZX");
    public static final Version ZX_4_1_0 = new Version(314, "4.1.0", "ZX");
    public static final Version ZX_4_2_0 = new Version(315, "4.2.0", "ZX");
    public static final Version ZX_4_2_1 = new Version(316, "4.2.1", "ZX");
    public static final Version ZX_4_3_0 = new Version(317, "4.3.0", "ZX");
    public static final Version ZX_4_4_0 = new Version(318, "4.4.0", "ZX");
    public static final Version ZX_4_5_0 = new Version(319, "4.5.0", "ZX");
    public static final Version ZX_4_5_1 = new Version(320, "4.5.1", "ZX");
    public static final Version ZX_4_6_0 = new Version(321, "4.6.0", "ZX");
    // TODO: might there be collisions with ZX v4.6.1, CTV 4.7.0, and V Branch 0.9.0?
    public static final Version ZX_4_6_1 = new Version(322, "4.6.1", "ZX");

    // Due to UPR FVX's origin as a merge of two branches, these share IDs with V branch Versions, and vice versa.
    public static final Version CTV_4_7_0 = new Version(322, "4.7.0", "closer-to-vanilla");
    public static final Version CTV_4_7_1 = new Version(323, "4.7.1", "closer-to-vanilla");
    public static final Version CTV_4_7_2 = new Version(324, "4.7.2", "closer-to-vanilla");
    public static final Version CTV_4_8_0 = new Version(325, "4.8.0", "closer-to-vanilla");

    public static final Version Vb_0_9_0 = new Version(322, "4.6.0 + V0.9.0", "V branch");
    public static final Version Vb_0_9_1 = new Version(323, "4.6.0 + V0.9.1", "V branch");
    public static final Version Vb_0_9_2 = new Version(324, "4.6.0 + V0.9.2", "V branch");
    public static final Version Vb_0_9_3 = new Version(325, "4.6.0 + V0.9.3", "V branch");
    public static final Version Vb_0_10_0 = new Version(326, "4.6.0 + V0.10.0", "V branch");
    public static final Version Vb_0_10_1 = new Version(327, "4.6.0 + V0.10.1", "V branch");
    public static final Version Vb_0_10_2 = new Version(328, "4.6.0 + V0.10.2", "V branch");
    public static final Version Vb_0_10_3 = new Version(329, "4.6.0 + V0.10.3", "V branch");
    public static final Version Vb_0_11_0 = new Version(330, "4.6.0 + V0.11.0", "V branch");
    public static final Version Vb_0_12_0 = new Version(331, "4.6.0 + V0.12.0", "V branch");
    public static final Version Vb_0_12_0a = new Version(332, "4.6.0 + V0.12.0a", "V branch");

    public static final Version FVX_0_1_0 = new Version(400, "0.1.0", "FVX");
    public static final Version FVX_0_1_1 = new Version(401, "0.1.1", "FVX");
    public static final Version FVX_1_0_0 = new Version(402, "1.0.0", "FVX");
    public static final Version FVX_1_0_1 = new Version(403, "1.0.1", "FVX");
    public static final Version FVX_1_0_2 = new Version(404, "1.0.2", "FVX");
    public static final Version FVX_1_0_3 = new Version(405, "1.0.3", "FVX");
    public static final Version FVX_1_1_0 = new Version(406, "1.1.0", "FVX");
    // forgot to create version for 1.1.1, so it doesn't get an id
    public static final Version FVX_1_1_2 = new Version(407, "1.1.2", "FVX");
    public static final Version FVX_1_1_3 = new Version(408, "1.1.3", "FVX");
    public static final Version FVX_1_2_0 = new Version(409, "1.2.0", "FVX");
    public static final Version FVX_1_2_1 = new Version(410, "1.2.1", "FVX");
    public static final Version FVX_1_2_2 = new Version(411, "1.2.2", "FVX");
    public static final Version FVX_1_3_0 = new Version(412, "1.3.0", "FVX");
    public static final Version FVX_1_3_1 = LATEST_VERSION;

    // add versions to the bottom as you create them

    public static final List<Version> ALL_VERSIONS = Collections.unmodifiableList(Arrays.asList(
            v1_0_1a, v1_0_2a, v1_1_0, v1_1_1, v1_1_2, v1_2_0a, v1_5_0, v1_6_0a, v1_6_1, v1_6_2, v1_6_3b, v1_7_0b,
            v1_7_1, v1_7_2,
            ZX_3_0_0, ZX_3_1_0, ZX_4_0_0, ZX_4_0_1, ZX_4_0_2, ZX_4_1_0, ZX_4_2_0, ZX_4_2_1, ZX_4_3_0, ZX_4_4_0,
            ZX_4_5_0, ZX_4_5_1, ZX_4_6_0, ZX_4_6_1, // keep this gap so new ZX versions can easily be added
            CTV_4_7_0, CTV_4_7_1, CTV_4_7_2, CTV_4_8_0, Vb_0_9_0, Vb_0_9_1, Vb_0_9_2, Vb_0_9_3, Vb_0_10_0, Vb_0_10_1,
            Vb_0_10_2, Vb_0_10_3, Vb_0_11_0, Vb_0_12_0, Vb_0_12_0a, FVX_0_1_0, FVX_0_1_1, FVX_1_0_0, FVX_1_0_1,
            FVX_1_0_2, FVX_1_0_3, FVX_1_1_0, FVX_1_1_2, FVX_1_1_3, FVX_1_2_0, FVX_1_2_1, FVX_1_2_2, FVX_1_3_0,
            FVX_1_3_1
    ));

    public static boolean isReleaseVersionNewer(String releaseVersion) {
        if (VERSION_STRING.contains("dev")) {
            return false;
        }
        // Chop off leading "v" from release version
        try {
            String releaseVersionTrimmed = releaseVersion.substring(1);
            String[] thisVersionPieces = VERSION_STRING.split("\\.");
            String[] releaseVersionPieces = releaseVersionTrimmed.split("\\.");
            int smallestLength = Math.min(thisVersionPieces.length, releaseVersionPieces.length);
            for (int i = 0; i < smallestLength; i++) {
                int thisVersionPiece = Integer.parseInt(thisVersionPieces[i]);
                int releaseVersionPiece = Integer.parseInt(releaseVersionPieces[i]);
                if (thisVersionPiece < releaseVersionPiece) {
                    return true;
                } else if (thisVersionPiece > releaseVersionPiece) {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            // Really not a big deal if we fail at this, probably because we can't connect to Github.
            return false;
        }
    }
}
