package com.uprfvx.romio.constants.enctaggers;

import com.uprfvx.romio.constants.Gen6Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Gen6EncounterAreaTagger extends EncounterAreaTagger {

    private static final int[] orasPostGameEncounterAreas = new int[]{
            585, 586, 587, 588, //Sky Pillar
            609, 610, 611, 612, //Battle Resort
            589, 590, 591, 592, 593, 594, 595, 596, 597, 598, 599, 600, 601, 602, 603,
            604, 605, 606, 607, 608, 657, 658, 659, 660, 661, 662, 663, 664, 665, 666,
            667, 668, 669, 670, 671, 672, 673, 674, 675, 676, 677, //Mirage spots
            34, 39, 48, 57, 66, 74, 79, 84, 89, 94, 100, 109, 119, 124, 129, 134, 144,
            153, 158, 168, 210, 214, 227, 232, 237, 242, 247, 252, 257, 262, 267, 272,
            277, 289, 298, 307, 316, 330, 335, 340, 346, 351, 356, 377, 382, 494, 499,
            504, 510, 515, 524, 615, 625, 635, 645, 679, //DexNav Foreign Encounter
            //Technically, neither mirage spots nor Dexnav foreign encounters are post-game.
            //however, they don't really qualify as "local" either, which is the actual use case.
    };

    private static final List<String> xyLocationTags = initXYLocationTags();

    private static List<String> initXYLocationTags() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "COURIWAY TOWN");
        addCopies(tags, 5, "AMBRETTE TOWN");
        addCopies(tags, 5, "CYLLAGE CITY");
        addCopies(tags, 4, "SHALOUR CITY");
        addCopies(tags, 3, "LAVERRE CITY");
        addCopies(tags, 1, "ROUTE 2");
        addCopies(tags, 5, "ROUTE 3");
        addCopies(tags, 2, "ROUTE 4");
        addCopies(tags, 5, "ROUTE 5");
        addCopies(tags, 1, "ROUTE 6");
        addCopies(tags, 6, "ROUTE 7");
        addCopies(tags, 10, "ROUTE 8");
        addCopies(tags, 1, "ROUTE 9");
        addCopies(tags, 5, "ROUTE 10");
        addCopies(tags, 4, "ROUTE 11");
        addCopies(tags, 10, "ROUTE 12");
        addCopies(tags, 2, "ROUTE 13");
        addCopies(tags, 9, "ROUTE 14");
        addCopies(tags, 9, "ROUTE 15");
        addCopies(tags, 9, "ROUTE 16");
        addCopies(tags, 1, "ROUTE 17");
        addCopies(tags, 6, "ROUTE 18");
        addCopies(tags, 10, "ROUTE 19");
        addCopies(tags, 5, "ROUTE 20");
        addCopies(tags, 9, "ROUTE 21");
        addCopies(tags, 6, "ROUTE 22");
        addCopies(tags, 1, "SANTALUNE FOREST");
        addCopies(tags, 3, "PARFUM PALACE");
        addCopies(tags, 2, "GLITTERING CAVE");
        addCopies(tags, 16, "REFLECTION CAVE");
        addCopies(tags, 24, "FROST CAVERN");
        addCopies(tags, 9, "POKEMON VILLAGE");
        addCopies(tags, 30, "VICTORY ROAD");
        addCopies(tags, 4, "CONNECTING CAVE");
        addCopies(tags, 23, "TERMINUS CAVE");
        addCopies(tags, 1, "LOST HOTEL");
        addCopies(tags, 9, "AZURE BAY");
        addCopies(tags, 4, "GLITTERING CAVE");
        addCopies(tags, 16, "REFLECTION CAVE");
        addCopies(tags, 26, "VICTORY ROAD");
        addCopies(tags, 9, "TERMINUS CAVE");
        addCopies(tags, 3, "ROUTE 6");
        addCopies(tags, 4, "ROUTE 18");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> orasLocationTags = initORASLocationTags();

    private static List<String> initORASLocationTags() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "DEWFORD TOWN");
        addCopies(tags, 4, "PACIFIDLOG TOWN");
        addCopies(tags, 4, "PETALBURG CITY");
        addCopies(tags, 4, "SLATEPORT CITY");
        addCopies(tags, 5, "LILYCOVE CITY");
        addCopies(tags, 4, "MOSSDEEP CITY");
        addCopies(tags, 4, "SOOTOPOLIS CITY");
        addCopies(tags, 4, "EVER GRANDE CITY");
        addCopies(tags, 5, "ROUTE 101");
        addCopies(tags, 9, "ROUTE 102");
        addCopies(tags, 9, "ROUTE 103");
        addCopies(tags, 18, "ROUTE 104");
        addCopies(tags, 5, "ROUTE 105");
        addCopies(tags, 5, "ROUTE 106");
        addCopies(tags, 5, "ROUTE 107");
        addCopies(tags, 5, "ROUTE 108");
        addCopies(tags, 5, "ROUTE 109");
        addCopies(tags, 9, "ROUTE 110");
        addCopies(tags, 10, "ROUTE 111");
        addCopies(tags, 10, "ROUTE 112");
        addCopies(tags, 5, "ROUTE 113");
        addCopies(tags, 10, "ROUTE 114");
        addCopies(tags, 9, "ROUTE 115");
        addCopies(tags, 5, "ROUTE 116");
        addCopies(tags, 9, "ROUTE 117");
        addCopies(tags, 10, "ROUTE 118");
        addCopies(tags, 16, "ROUTE 119");
        addCopies(tags, 16, "ROUTE 120");
        addCopies(tags, 6, "ROUTE 121");
        addCopies(tags, 5, "ROUTE 122");
        addCopies(tags, 8, "ROUTE 123");
        addCopies(tags, 5, "ROUTE 124");
        addCopies(tags, 5, "ROUTE 125");
        addCopies(tags, 5, "ROUTE 126");
        addCopies(tags, 5, "ROUTE 127");
        addCopies(tags, 5, "ROUTE 128");
        addCopies(tags, 5, "ROUTE 129");
        addCopies(tags, 5, "ROUTE 130");
        addCopies(tags, 5, "ROUTE 131");
        addCopies(tags, 5, "ROUTE 132");
        addCopies(tags, 5, "ROUTE 133");
        addCopies(tags, 5, "ROUTE 134");
        addCopies(tags, 1, "ROUTE 107");
        addCopies(tags, 1, "ROUTE 124");
        addCopies(tags, 1, "ROUTE 126");
        addCopies(tags, 1, "ROUTE 128");
        addCopies(tags, 1, "ROUTE 129");
        addCopies(tags, 1, "ROUTE 130");
        addCopies(tags, 36, "METEOR FALLS");
        addCopies(tags, 5, "RUSTURF TUNNEL");
        addCopies(tags, 16, "GRANITE CAVE");
        addCopies(tags, 5, "PETALBURG WOODS");
        addCopies(tags, 5, "JAGGED PASS");
        addCopies(tags, 5, "FIERY PATH");
        addCopies(tags, 26, "MT. PYRE");
        addCopies(tags, 4, "TEAM AQUA HIDEOUT");
        addCopies(tags, 55, "SEAFLOOR CAVERN");
        addCopies(tags, 20, "CAVE OF ORIGIN");
        addCopies(tags, 28, "VICTORY ROAD");
        addCopies(tags, 43, "SHOAL CAVE");
        addCopies(tags, 4, "NEW MAUVILLE");
        addCopies(tags, 16, "SEA MAUVILLE");
        addCopies(tags, 4, "SEALED CHAMBER");
        addCopies(tags, 21, "SCORCHED SLAB");
        addCopies(tags, 4, "TEAM MAGMA HIDEOUT");
        addCopies(tags, 4, "SKY PILLAR");
        // mirage island/forest/cave/mountain each appear in multiple places,
        // but e.g. the mirage forests all look alike and have similar wild encounters,
        // so we treat island/forest/cave/mountain as four locations and not more
        addCopies(tags, 9, "MIRAGE FOREST");
        addCopies(tags, 10, "MIRAGE ISLAND");
        addCopies(tags, 1, "MIRAGE MOUNTAIN");
        addCopies(tags, 4, "BATTLE RESORT");
        addCopies(tags, 40, "SAFARI ZONE");
        addCopies(tags, 4, "CAVE OF ORIGIN");
        addCopies(tags, 9, "MIRAGE MOUNTAIN");
        addCopies(tags, 12, "MIRAGE CAVE");
        addCopies(tags, 5, "MT. PYRE");
        addCopies(tags, 4, "SOOTOPOLIS CITY");
        return Collections.unmodifiableList(tags);
    }

    private static final TagPackMap Gen6TagPackMap = new Builder()
            .newPack(Gen6Constants.Type_XY, xyLocationTags) // not even any post-game encounter areas. RIP Pokémon Z.
            .newPack(Gen6Constants.Type_ORAS, orasLocationTags)
                .postGameAreas(orasPostGameEncounterAreas)
            .build();

    @Override
    protected TagPackMap getTagPacks() {
        return Gen6TagPackMap;
    }
}
