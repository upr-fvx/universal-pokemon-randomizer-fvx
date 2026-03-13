package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.constants.MoveIDs;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveCategory;
import com.dabomstew.pkromio.gamedata.StatChangeMoveType;
import com.dabomstew.pkromio.gamedata.StatChangeType;
import com.dabomstew.pkromio.gamedata.StatusType;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.graphics.palettes.TypeColor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                        if (random.nextInt(10) < 2) break;
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

    // =========================================================================
    // Move name generation
    // =========================================================================
 
    private static final Set<String> USED_MOVE_NAMES = new HashSet<>();
 
    private static final Map<Type, String[]> TYPE_MOVE_NAMES =
            TypeColor.readTypeNameMapFromFile("data/TypeMoveNames.txt");
    private static final Map<MoveCategory, String[]> CAT_MOVE_NAMES =
            TypeColor.readCatNameMapFromFile("data/CatMoveNames.txt");
    private static final Map<String, String[]> EXTRA_NAME_LISTS =
            readAllNameLists("data/SubCatMoveNames.txt");
 
    private static final int MAX_ATTEMPTS = 50;
 
    /**
     * Moves whose primary effect is HP restoration. These have no distinguishing
     * fields in the Move data structure (healing is encoded in effectIndex),
     * so we identify them by move number.
     */
    public static final Set<Integer> healMoves = Set.of(
            // Self-healing (restore user's HP)
            MoveIDs.recover,        // 105 - Gen 1, restores 50% HP
            MoveIDs.softBoiled,     // 135 - Gen 1, restores 50% HP
            MoveIDs.rest,           // 156 - Gen 1, full heal + sleep
            MoveIDs.milkDrink,      // 208 - Gen 2, restores 50% HP
            MoveIDs.morningSun,     // 234 - Gen 2, weather-dependent heal
            MoveIDs.synthesis,      // 235 - Gen 2, weather-dependent heal
            MoveIDs.moonlight,      // 236 - Gen 2, weather-dependent heal
            MoveIDs.swallow,        // 256 - Gen 3, heals based on Stockpile count
            MoveIDs.wish,           // 273 - Gen 3, delayed heal next turn
            MoveIDs.ingrain,        // 275 - Gen 3, gradual heal + trapped
            MoveIDs.refresh,        // 287 - Gen 3, cures own status
            MoveIDs.slackOff,       // 303 - Gen 3, restores 50% HP
            MoveIDs.roost,          // 355 - Gen 4, restores 50% HP
            MoveIDs.healingWish,    // 361 - Gen 4, faints user, fully heals switch-in
            MoveIDs.aquaRing,       // 392 - Gen 4, gradual heal each turn
            MoveIDs.healOrder,      // 456 - Gen 4, restores 50% HP
            MoveIDs.lunarDance,     // 461 - Gen 4, faints user, fully heals switch-in
 
            // Ally/party healing
            MoveIDs.healBell,       // 215 - Gen 2, cures party status conditions
            MoveIDs.aromatherapy,   // 312 - Gen 3, cures party status conditions
            MoveIDs.healPulse,      // 505 - Gen 5, heals target 50% HP
            MoveIDs.floralHealing,  // 666 - Gen 7, heals target
            MoveIDs.purify,         // 685 - Gen 7, cures target's status + heals
            MoveIDs.shoreUp,        // 659 - Gen 7, restores 50% HP (more in sandstorm)
            MoveIDs.lifeDew,        // 791 - Gen 8, heals user and allies
            MoveIDs.jungleHealing   // 816 - Gen 8, heals party + cures status
    );
 
    /**
     * Maps StatusType enum values to their per-status word list key.
     */
    private static final Map<StatusType, String> STATUS_TYPE_TO_LIST_KEY = Map.of(
            StatusType.POISON, "INFLICT_POISON",
            StatusType.TOXIC_POISON, "INFLICT_POISON",
            StatusType.BURN, "INFLICT_BURN",
            StatusType.FREEZE, "INFLICT_FREEZE",
            StatusType.PARALYZE, "INFLICT_PARALYZE",
            StatusType.SLEEP, "INFLICT_SLEEP",
            StatusType.CONFUSION, "INFLICT_CONFUSION"
    );
 
    private static Map<String, String[]> readAllNameLists(String filename) {
        Map<String, String[]> result = new HashMap<>();
        Pattern bracketPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Pattern wordPattern = Pattern.compile("\\(([^)]+)\\)");
 
        try {
            BufferedReader br;
            InputStream is = MoveDataRandomizer.class.getClassLoader().getResourceAsStream(filename);
            if (is != null) {
                br = new BufferedReader(new InputStreamReader(is));
            } else {
                br = new BufferedReader(new FileReader(filename));
            }
 
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
 
                Matcher keyMatcher = bracketPattern.matcher(line);
                if (!keyMatcher.find()) continue;
 
                String key = keyMatcher.group(1);
                List<String> words = new ArrayList<>();
                Matcher wordMatcher = wordPattern.matcher(line.substring(keyMatcher.end()));
                while (wordMatcher.find()) {
                    words.add(wordMatcher.group(1));
                }
 
                if (!words.isEmpty()) {
                    result.put(key, words.toArray(new String[0]));
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Warning: Could not read extra name lists from " + filename +
                    ": " + e.getMessage());
        }
 
        return result;
    }
 
    public static int getMaxMoveNameLength(int generation) {
        return switch (generation) {
            case 1 -> 10;
            case 2, 3 -> 12;
            case 4, 5 -> 16;
            default -> 24;
        };
    }
 
    private static String[] getExtraList(String key) {
        if (EXTRA_NAME_LISTS == null) return null;
        return EXTRA_NAME_LISTS.get(key);
    }
 
    /**
     * Determines the best "action word" for a move based on its properties.
     *
     * Priority:
     * 1. isPunchMove -> PUNCH words
     * 2. isSoundMove -> SOUND words
     * 3. Drain moves (damaging + absorbPercent > 0) -> DRAIN words
     * 4. STATUS category -> subcategory based on mechanical effect
     * 5. isTrapMove -> STATUS_TRAP words
     * 6. Default -> standard PHYSICAL/SPECIAL/STATUS words
     */
    private static String[] getActionWords(Move mv) {
        if (mv.isPunchMove) {
            String[] words = getExtraList("PUNCH");
            if (words != null && words.length > 0) return words;
        }
 
        if (mv.isSoundMove) {
            String[] words = getExtraList("SOUND");
            if (words != null && words.length > 0) return words;
        }
 
        // Drain moves: damaging moves that heal by dealing damage (Giga Drain, Drain Punch, etc.)
        if (mv.absorbPercent > 0 && mv.power > 0) {
            String[] words = getExtraList("DRAIN");
            if (words != null && words.length > 0) return words;
        }
 
        if (mv.category == MoveCategory.STATUS) {
            String[] words = getStatusSubcategoryWords(mv);
            if (words != null && words.length > 0) return words;
        }
 
        if (mv.isTrapMove) {
            String[] words = getExtraList("STATUS_TRAP");
            if (words != null && words.length > 0) return words;
        }
 
        String[] catNames = CAT_MOVE_NAMES.get(mv.category);
        if (catNames == null || catNames.length == 0) {
            return new String[]{"Strike"};
        }
        return catNames;
    }
 
    /**
     * For STATUS moves, picks the right subcategory word list based on the move's
     * mechanical effect.
     *
     * Classification order:
     * 1. Known healing moves (by move number) -> STATUS_HEAL
     * 2. Per-status-type infliction words (sleep, poison, burn, etc.)
     * 3. Trap moves -> STATUS_TRAP
     * 4. Stat buffs (self-targeting positive) -> STATUS_BUFF
     * 5. Stat debuffs (opponent-targeting) -> STATUS_DEBUFF
     * 6. Field/ally effects -> STATUS_BUFF
     * 7. Fallback -> STATUS_HEAL (for unclassifiable moves like Splash, Teleport)
     */
    private static String[] getStatusSubcategoryWords(Move mv) {
        // Known healing moves identified by move number
        if (healMoves.contains(mv.number)) {
            return getExtraList("STATUS_HEAL");
        }
 
        // Per-status-type infliction words
        if (mv.statusType != StatusType.NONE) {
            String listKey = STATUS_TYPE_TO_LIST_KEY.get(mv.statusType);
            if (listKey != null) {
                String[] words = getExtraList(listKey);
                if (words != null && words.length > 0) return words;
            }
        }
 
        // Trap moves
        if (mv.isTrapMove) {
            return getExtraList("STATUS_TRAP");
        }
 
        // Stat-changing moves
        if (mv.statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            if (mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) {
                boolean hasPositive = false;
                for (Move.StatChange sc : mv.statChanges) {
                    if (sc.type != StatChangeType.NONE && sc.stages > 0) {
                        hasPositive = true;
                        break;
                    }
                }
                return getExtraList(hasPositive ? "STATUS_BUFF" : "STATUS_DEBUFF");
            }
            if (mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_TARGET) {
                return getExtraList("STATUS_DEBUFF");
            }
            if (mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_ALL ||
                    mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_ALLY) {
                return getExtraList("STATUS_BUFF");
            }
        }
 
        // Fallback for unclassifiable STATUS moves (Splash, Teleport, etc.)
        return getExtraList("STATUS_HEAL");
    }
 
    /**
     * Generates a random move name for the given move.
     *
     * Strategy:
     * 1. Pick a type word + context-aware action word
     * 2. Combine as "TypeWord ActionWord"
     * 3. If too long with space, try "TypeWordActionWord"
     * 4. If still too long, re-roll
     */
    public static String getRandomMoveName(Move mv, Type type, Random random, int maxMoveNameLength) {
        String[] typeNames = TYPE_MOVE_NAMES.get(type);
        if (typeNames == null || typeNames.length == 0) {
            typeNames = new String[]{"Attack"};
        }
 
        String[] actionWords = getActionWords(mv);
 
        System.out.println("Type: " + type + ", Category: " + mv.category +
                (mv.isPunchMove ? " [PUNCH]" : "") +
                (mv.isSoundMove ? " [SOUND]" : "") +
                (mv.isTrapMove ? " [TRAP]" : "") +
                (mv.absorbPercent > 0 && mv.power > 0 ? " [DRAIN]" : "") +
                (healMoves.contains(mv.number) ? " [HEAL]" : "") +
                (mv.category == MoveCategory.STATUS ?
                        " [" + mv.statChangeMoveType + "/" + mv.statusType + "]" : ""));
 
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String typeWord = typeNames[random.nextInt(typeNames.length)];
            String actionWord = actionWords[random.nextInt(actionWords.length)];
            String moveName = typeWord + " " + actionWord;
 
            if (moveName.length() > maxMoveNameLength) {
                String compactName = typeWord + actionWord;
                if (compactName.length() <= maxMoveNameLength) {
                    moveName = compactName;
                    System.out.println("Compacted Move Name: '" + typeWord + " " + actionWord +
                            "' -> '" + compactName + "' (" + compactName.length() + " chars)");
                } else {
                    System.out.println("Rejected Move Name: '" + moveName + "' (" + moveName.length() +
                            " chars, too long even compacted: " + compactName.length() + ")");
                    continue;
                }
            }
 
            if (USED_MOVE_NAMES.contains(moveName)) {
                System.out.println("Rejected Move Name: '" + moveName + "' (duplicate)");
                continue;
            }
 
            USED_MOVE_NAMES.add(moveName);
            System.out.println("Chosen Type Name: " + typeWord);
            System.out.println("Chosen Action Name: " + actionWord);
            return moveName;
        }
 
        System.out.println("WARNING: Move name generation failed after " + MAX_ATTEMPTS +
                " attempts. Using fallback.");
        return "Attack Strike";
    }
 
    /**
     * Backwards-compatible overload for callers without a Move object.
     */
    public static String getRandomMoveName(Type type, Random random, int maxMoveNameLength,
                                           MoveCategory... category) {
        Move dummy = new Move();
        dummy.category = (category.length > 0)
                ? category[0]
                : switch (random.nextInt(3)) {
                    case 0 -> MoveCategory.PHYSICAL;
                    case 1 -> MoveCategory.SPECIAL;
                    default -> MoveCategory.STATUS;
                };
        return getRandomMoveName(dummy, type, random, maxMoveNameLength);
    }
 
    public void randomizeMoveNames() {
        List<Move> moves = romHandler.getMoves();
        int maxNameLength = getMaxMoveNameLength(romHandler.generationOfPokemon());
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                String newName = getRandomMoveName(mv, mv.type, random, maxNameLength);
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
                    mv.category = (mv.category == MoveCategory.PHYSICAL) ?
                            MoveCategory.SPECIAL : MoveCategory.PHYSICAL;
                }
            }
        }
        changesMade = true;
    }
 
}