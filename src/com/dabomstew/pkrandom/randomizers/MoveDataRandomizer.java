package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.constants.MoveIDs;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveCategory;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.graphics.palettes.TypeColor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

    private static final Set<String> USED_MOVE_NAMES = new HashSet<>();
    private static final Map<Type, String[]> TYPE_MOVE_NAMES = TypeColor.readTypeNameMapFromFile("data/TypeMoveNames.txt");
    private static final Map<MoveCategory, String[]> CAT_MOVE_NAMES = TypeColor.readCatNameMapFromFile("data/CatMoveNames.txt");
    private static final int MAX_MOVE_NAME_LENGTH = 12;
    private static final int MAX_ATTEMPTS = 50;
    
    public static String getRandomMoveName(Type type, Random random, MoveCategory... category) {

        // If no type names are available, default to "Attack"
        String[] typeNames = TYPE_MOVE_NAMES.get(type);
        if (typeNames == null || typeNames.length == 0) {
            typeNames = new String[]{"Attack"};
        }
    
        MoveCategory chosenCat = (category.length > 0)
                ? category[0]
                : switch (random.nextInt(3)) {
                    case 0 -> MoveCategory.PHYSICAL;
                    case 1 -> MoveCategory.SPECIAL;
                    default -> MoveCategory.STATUS;
                };
    
        // If no category names are available, default to "Strike"
        String[] catNames = CAT_MOVE_NAMES.get(chosenCat);
        if (catNames == null || catNames.length == 0) {
            catNames = new String[]{"Strike"};
        }
    
        System.out.println("Type: " + type + ", Category: " + chosenCat);
    
        // Try multiple times to get a short enough name
        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            String typeWord = typeNames[random.nextInt(typeNames.length)];
            String categoryWord = catNames[random.nextInt(catNames.length)];
            String moveName = typeWord + " " + categoryWord;
        
            if (moveName.length() > MAX_MOVE_NAME_LENGTH) {
                System.out.println("Rejected Move Name: '" + moveName + "' (" + moveName.length() + " chars, too long)");
                continue;
            }
        
            if (USED_MOVE_NAMES.contains(moveName)) {
                System.out.println("Rejected Move Name: '" + moveName + "' (duplicate)");
                continue;
            }
        
            USED_MOVE_NAMES.add(moveName);
        
            System.out.println("Chosen Type Name: " + typeWord);
            System.out.println("Chosen Category Name: " + categoryWord);
            return moveName;
        }
    
        // Absolute fallback
        System.out.println("WARNING: Move name generation failed after " + MAX_ATTEMPTS + " attempts. Using fallback.");
        return "Attack Strike";
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
