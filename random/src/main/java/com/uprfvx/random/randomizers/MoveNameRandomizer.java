package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.MoveIDs;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.MoveCategory;
import com.uprfvx.romio.gamedata.StatChangeMoveType;
import com.uprfvx.romio.gamedata.StatChangeType;
import com.uprfvx.romio.gamedata.StatusType;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Randomizer for move names. This is a purely cosmetic feature, it does not affect gameplay.
 * <p>
 * Compare with {@link TrainerNameRandomizer} and the {@link PaletteRandomizer}
 * subclasses, which follow the same pattern.
 */
public class MoveNameRandomizer extends Randomizer {

    private static final int MAX_ATTEMPTS = 50;

    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    private static final Map<Type, String[]> TYPE_MOVE_NAMES =
        MoveNameRandomizer.readTypeNameMapFromFile("data/TypeMoveNames.txt");
    private static final Map<MoveCategory, String[]> CAT_MOVE_NAMES =
        MoveNameRandomizer.readCatNameMapFromFile("data/CatMoveNames.txt");
    private static final Map<String, String[]> EXTRA_NAME_LISTS =
        MoveNameRandomizer.readNameListFile("data/SubCatMoveNames.txt");

    private final Set<String> usedMoveNames = new HashSet<>();

    /**
     * Reads a name-list file and returns a map of key -> word array.
     * Keys are matched to {@link Type} enum values.
     */
    public static Map<Type, String[]> readTypeNameMapFromFile(String filename) {
        Map<String, String[]> raw = readNameListFile(filename);
        Map<Type, String[]> result = new EnumMap<>(Type.class);
        for (Map.Entry<String, String[]> entry : raw.entrySet()) {
            try {
                Type type = Type.valueOf(entry.getKey());
                result.put(type, entry.getValue());
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Unknown type '" + entry.getKey()
                        + "' in " + filename);
            }
        }
        return result;
    }

    /**
     * Reads a name-list file and returns a map of key -> word array.
     * Keys are matched to {@link MoveCategory} enum values.
     */
    public static Map<MoveCategory, String[]> readCatNameMapFromFile(String filename) {
        Map<String, String[]> raw = readNameListFile(filename);
        Map<MoveCategory, String[]> result = new EnumMap<>(MoveCategory.class);
        for (Map.Entry<String, String[]> entry : raw.entrySet()) {
            try {
                MoveCategory cat = MoveCategory.valueOf(entry.getKey());
                result.put(cat, entry.getValue());
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Unknown category '" + entry.getKey()
                        + "' in " + filename);
            }
        }
        return result;
    }

    /**
     * Reads a name-list file and returns a raw string-keyed map.
     */
    public static Map<String, String[]> readNameListFile(String filename) {
        Map<String, String[]> result = new HashMap<>();

        try {
            BufferedReader br;
            InputStream is = MoveNameRandomizer.class.getClassLoader()
                    .getResourceAsStream(filename);
            if (is != null) {
                br = new BufferedReader(new InputStreamReader(is));
            } else {
                br = new BufferedReader(new FileReader(filename));
            }

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                Matcher keyMatcher = BRACKET_PATTERN.matcher(line);
                if (!keyMatcher.find()) continue;

                String key = keyMatcher.group(1);
                List<String> words = new ArrayList<>();
                Matcher wordMatcher = WORD_PATTERN.matcher(
                        line.substring(keyMatcher.end()));
                while (wordMatcher.find()) {
                    words.add(wordMatcher.group(1));
                }

                if (!words.isEmpty()) {
                    result.put(key, words.toArray(new String[0]));
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Warning: Could not read name lists from "
                    + filename + ": " + e.getMessage());
        }

        return result;
    }

    public MoveNameRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeMoveNames() {
        usedMoveNames.clear();
        List<Move> moves = romHandler.getMoves();
        int maxNameLength = romHandler.getMaxMoveNameLength();
        boolean useUpperCase = detectUpperCaseNames(moves);
        for (Move mv : moves) {
            if (mv != null && mv.internalId != MoveIDs.struggle) {
                String name = getRandomMoveName(mv, mv.type, maxNameLength);
                mv.name = useUpperCase ? name.toUpperCase() : name;
            }
        }
        changesMade = true;
    }

    /**
     * Detects whether the ROM stores move names in ALL CAPS by sampling
     * existing move names. If a majority of alphabetic characters are
     * uppercase, we assume the ROM uses ALL CAPS.
     */
    private static boolean detectUpperCaseNames(List<Move> moves) {
        int upper = 0, lower = 0;
        for (Move mv : moves) {
            if (mv == null || mv.name == null) continue;
            for (char c : mv.name.toCharArray()) {
                if (Character.isUpperCase(c)) upper++;
                else if (Character.isLowerCase(c)) lower++;
            }
        }
        // If there are essentially no lowercase letters, it's ALL CAPS
        return lower == 0 || (upper > 0 && lower * 10 < upper);
    }

    // Move all the name generation logic here:
    // - getRandomMoveName(Move, Type, int)
    // - getActionWords(Move)
    // - getStatusSubcategoryWords(Move)
    // - getExtraList(String)
    // - healMoves set
    // - STATUS_TYPE_TO_LIST_KEY map

    private String getRandomMoveName(Move mv, Type type, int maxMoveNameLength) {
        String[] typeNames = TYPE_MOVE_NAMES.get(type);
        if (typeNames == null || typeNames.length == 0) {
            typeNames = new String[]{"Attack"};
        }

        String[] actionWords = getActionWords(mv);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String typeWord = typeNames[random.nextInt(typeNames.length)];
            String actionWord = actionWords[random.nextInt(actionWords.length)];
            String moveName = typeWord + " " + actionWord;

            if (moveName.length() > maxMoveNameLength) {
                String compactName = typeWord + actionWord;
                if (compactName.length() <= maxMoveNameLength) {
                    moveName = compactName;
                } else {
                    continue;
                }
            }

            if (usedMoveNames.contains(moveName)) {
                continue;
            }

            usedMoveNames.add(moveName);
            return moveName;
        }

        return "Attack Strike";
    }

    /**
     * Determines the best "action word" for a move based on its properties.
     *
     * Priority:
     * 1. isPunchMove
     * 2. isSoundMove
     * 3. Drain moves (damaging + absorbPercent > 0)
     * 4. STATUS category
     * 5. isTrapMove
     * 6. Default
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
     * Moves whose primary effect is HP restoration. These have no distinguishing
     * fields in the Move data structure (healing is encoded in effectIndex),
     * so we identify them by move number.
     */
    public static final Set<Integer> healMoves = Set.of(
        // Self-healing (restore user's HP)
        MoveIDs.recover, MoveIDs.softBoiled, MoveIDs.rest, MoveIDs.milkDrink, MoveIDs.morningSun, MoveIDs.synthesis,
        MoveIDs.moonlight, MoveIDs.swallow, MoveIDs.wish, MoveIDs.ingrain, MoveIDs.refresh, MoveIDs.slackOff,
        MoveIDs.roost, MoveIDs.healingWish, MoveIDs.aquaRing, MoveIDs.healOrder, MoveIDs.lunarDance, MoveIDs.healBell,
        MoveIDs.aromatherapy, MoveIDs.healPulse, MoveIDs.floralHealing, MoveIDs.purify, MoveIDs.shoreUp,
        MoveIDs.lifeDew, MoveIDs.jungleHealing
    );

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
     * 7. Fallback (for unclassifiable moves like Splash, Teleport)
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
 
        // Fallback: return null for unclassifiable STATUS moves (Splash, Teleport, etc.)
        // so getActionWords falls through to the generic [STATUS] word list in CatMoveNames.txt.
        return null;
    }
 
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

    private static String[] getExtraList(String key) {
        if (EXTRA_NAME_LISTS == null) return null;
        return EXTRA_NAME_LISTS.get(key);
    }

    public boolean isChangesMade() {
        return changesMade;
    }
}