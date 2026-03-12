package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.constants.MoveIDs;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveCategory;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.graphics.palettes.TypeColor;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MoveDataRandomizer extends Randomizer {

    private boolean nameChangesMade;
    
    public MoveDataRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    /**
     * Returns whether any changes have been made to Move names.
     */
    public boolean isNameChangesMade() {
        return nameChangesMade;
    }

    // Makes sure to not touch move ID 165 (Struggle)
    // There are other exclusions where necessary to stop things glitching.

    public void randomizeMovePowers() {
        List<Move> moves = romHandler.getMoves();
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle && mv.power >= 10) {
                // "Generic" damaging move to randomize power
                if (random.nextInt(3) != 2) {
                    // "Regular" move
                    mv.power = random.nextInt(11) * 5 + 50; // 50 ... 100
                } else {
                    // "Extreme" move
                    mv.power = random.nextInt(27) * 5 + 20; // 20 ... 150
                }
                // Tiny chance for massive power jumps
                for (int i = 0; i < 2; i++) {
                    if (random.nextInt(100) == 0) {
                        mv.power += 50;
                    }
                }

                if (mv.hitCount != 1) {
                    // Divide randomized power by average hit count, round to
                    // nearest 5
                    mv.power = (int) (Math.round(mv.power / mv.hitCount / 5) * 5);
                    if (mv.power == 0) {
                        mv.power = 5;
                    }
                }
            }
        }
        changesMade = true;
    }

    public void randomizeMovePPs() {
        List<Move> moves = romHandler.getMoves();
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                if (random.nextInt(3) != 2) {
                    // "average" PP: 15-25
                    mv.pp = random.nextInt(3) * 5 + 15;
                } else {
                    // "extreme" PP: 5-40
                    mv.pp = random.nextInt(8) * 5 + 5;
                }
            }
        }
        changesMade = true;
    }

    public void randomizeMoveAccuracies() {
        List<Move> moves = romHandler.getMoves();
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle && mv.hitratio >= 5) {
                // "Sane" accuracy randomization
                // Broken into three tiers based on original accuracy
                // Designed to limit the chances of 100% accurate OHKO moves and
                // keep a decent base of 100% accurate regular moves.

                if (mv.hitratio <= 50) {
                    // lowest tier (acc <= 50)
                    // new accuracy = rand(20...50) inclusive
                    // with a 10% chance to increase by 50%
                    mv.hitratio = random.nextInt(7) * 5 + 20;
                    if (random.nextInt(10) == 0) {
                        mv.hitratio = (mv.hitratio * 3 / 2) / 5 * 5;
                    }
                } else if (mv.hitratio < 90) {
                    // middle tier (50 < acc < 90)
                    // count down from 100% to 20% in 5% increments with 20%
                    // chance to "stop" and use the current accuracy at each
                    // increment
                    // gives decent-but-not-100% accuracy most of the time
                    mv.hitratio = 100;
                    while (mv.hitratio > 20) {
                        if (random.nextInt(10) < 2) {
                            break;
                        }
                        mv.hitratio -= 5;
                    }
                } else {
                    // highest tier (90 <= acc <= 100)
                    // count down from 100% to 20% in 5% increments with 40%
                    // chance to "stop" and use the current accuracy at each
                    // increment
                    // gives high accuracy most of the time
                    mv.hitratio = 100;
                    while (mv.hitratio > 20) {
                        if (random.nextInt(10) < 4) {
                            break;
                        }
                        mv.hitratio -= 5;
                    }
                }
            }
        }
        changesMade = true;
    }

    public void randomizeMoveTypes() {
        List<Move> moves = romHandler.getMoves();
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle && mv.type != null) {
                mv.type = romHandler.getTypeService().randomType(random);
            }
        }
        changesMade = true;
    }

    private static final Map<Type, String[]> TYPE_MOVE_NAMES = TypeColor.readTypeNameMapFromFile("data/TypeMoveNames.txt");
    private static final Map<MoveCategory, String[]> CAT_MOVE_NAMES = TypeColor.readCatNameMapFromFile("data/CatMoveNames.txt");
    public static String getRandomMoveName(Type type, Random random, MoveCategory... category) {
        // Get type-based move names
        String[] typeNames = TYPE_MOVE_NAMES.get(type);
    
        // If no type names are available, default to "Attack"
        String typeWord = (typeNames == null || typeNames.length == 0)
                            ? "Attack"
                            : typeNames[random.nextInt(typeNames.length)];

        System.out.println("Type: " + type + ", Chosen Type Name: " + typeWord);

        // Determine the category to use: if none provided, pick one randomly
        MoveCategory chosenCat = (category.length > 0) ? category[0] : switch (random.nextInt(3)) {
            case 0 -> MoveCategory.PHYSICAL;
            case 1 -> MoveCategory.SPECIAL;
            case 2 -> MoveCategory.STATUS;
            default -> MoveCategory.PHYSICAL; // Default case, though technically unreachable
        };
    
        // Get category-based move names
        String[] catNames = CAT_MOVE_NAMES.get(chosenCat);
    
        // If no category names are available, default to a generic term
        String categoryWord = (catNames == null || catNames.length == 0)
                                ? "Strike"
                                : catNames[random.nextInt(catNames.length)];

        System.out.println("Chosen Category: " + chosenCat + ", Chosen Category Name: " + categoryWord);

        // Combine type word and category word to form the move name
        return typeWord + " " + categoryWord;
    }

    public void randomizeMoveNames() {
        List<Move> moves = romHandler.getMoves();
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                String newName = getRandomMoveName(mv.type, random, mv.category);
                mv.oldName = mv.name;
                mv.name = newName;
                mv.newName = newName;
            }
        }
        changesMade = true;
        nameChangesMade = true;
    }

    public void randomizeMoveCategory() {
        if (!romHandler.hasPhysicalSpecialSplit()) {
            return;
        }
        List<Move> moves = romHandler.getMoves();
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle && mv.category != MoveCategory.STATUS) {
                if (random.nextInt(2) == 0) {
                    mv.category = (mv.category == MoveCategory.PHYSICAL) ? MoveCategory.SPECIAL : MoveCategory.PHYSICAL;
                }
            }
        }
        changesMade = true;
    }

}
