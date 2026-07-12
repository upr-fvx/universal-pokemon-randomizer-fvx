package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.AbilityIDs;
import com.uprfvx.romio.constants.MoveIDs;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.Generation;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * ROM-driven validation for the custom four-slot Better Movesets redesign.
 * <p>
 * Named to match the {@code *Randomizer*Test} filter so it runs under the {@code testROMs} Gradle task (which
 * sets {@code romsPath} and a 4 GB heap). It does NOT extend {@link RandomizerTest}, so it does not trigger the
 * load-every-ROM {@code @BeforeAll}; instead it loads each ROM in {@code roms/} itself, by content.
 * <pre>{@code  ./gradlew.bat :random:testROMs --tests "*BetterMovesets*" }</pre>
 * Runs Better Movesets on each ROM, prints sample movesets, and asserts the redesign's invariants. Skips itself
 * if no loadable ROM is present.
 */
public class BetterMovesetsRandomizerTest {

    // 3DS retail dumps are multi-GB. Decrypted ones ARE loadable: the NCCH handler reads via RandomAccessFile
    // and only pulls the GARC archives it needs into memory, so the ROM's file size never lands in the heap.
    // Cap generously (above the largest retail cart) so decrypted Gen 6/7 ROMs run; genuinely unloadable files
    // still get skipped gracefully by tryLoad returning null.
    private static final long MAX_ROM_BYTES = 6L * 1024 * 1024 * 1024;
    private static final double UBIQUITOUS_RATE = 0.20;
    private static final int SAMPLE_MOVESETS_PER_ROM = 6;

    private static final Set<Integer> RAIN_ABILITIES = Set.of(
            AbilityIDs.swiftSwim, AbilityIDs.rainDish, AbilityIDs.drySkin, AbilityIDs.hydration,
            AbilityIDs.drizzle, AbilityIDs.primordialSea);
    private static final Set<Integer> SUN_ABILITIES = Set.of(
            AbilityIDs.chlorophyll, AbilityIDs.solarPower, AbilityIDs.leafGuard, AbilityIDs.flowerGift,
            AbilityIDs.harvest, AbilityIDs.drought, AbilityIDs.desolateLand);
    private static final Set<Integer> SAND_ABILITIES = Set.of(
            AbilityIDs.sandVeil, AbilityIDs.sandRush, AbilityIDs.sandForce, AbilityIDs.sandStream);
    private static final Set<Integer> HAIL_ABILITIES = Set.of(
            AbilityIDs.snowCloak, AbilityIDs.iceBody, AbilityIDs.slushRush, AbilityIDs.snowWarning);

    @Test
    public void inspectBetterMovesets() {
        String romsDir = System.getProperty("romsPath");
        assumeTrue(romsDir != null, "romsPath not set");
        File dir = new File(romsDir);
        assumeTrue(dir.isDirectory(), "roms dir missing: " + romsDir);

        File[] files = dir.listFiles();
        assumeTrue(files != null && files.length > 0, "roms dir empty: " + romsDir);
        List<File> candidates = new ArrayList<>(Arrays.asList(files));
        candidates.sort(Comparator.comparingLong(File::length)); // small/fast ROMs first

        int loaded = 0;
        List<String> failures = new ArrayList<>();
        for (File f : candidates) {
            if (!f.isFile() || f.getName().equalsIgnoreCase("readme.txt")) {
                continue;
            }
            if (f.length() > MAX_ROM_BYTES) {
                System.out.printf("SKIP (%.1f GB, too large - likely encrypted 3DS dump): %s%n",
                        f.length() / 1024.0 / 1024 / 1024, f.getName());
                continue;
            }
            RomHandler rh = tryLoad(f.getAbsolutePath());
            if (rh == null) {
                System.out.println("SKIP (not loadable): " + f.getName());
                continue;
            }
            loaded++;
            failures.addAll(checkOneRom(rh, f.getName()));
        }

        assumeTrue(loaded > 0, "No loadable ROM found in " + romsDir);
        assertTrue(failures.isEmpty(),
                "Better Movesets invariant failures (" + failures.size() + "):\n  " + String.join("\n  ", failures));
        System.out.println("\n=== All invariants held across " + loaded + " ROM(s). ===");
    }

    private List<String> checkOneRom(RomHandler romHandler, String romName) {
        romHandler.getRestrictedSpeciesService().setRestrictions(new GenRestrictions());
        int gen = romHandler.generationOfPokemon();
        System.out.println("\n================ " + romName + " (gen " + gen + ") ================");

        Settings s = new Settings();
        s.setBetterBossTrainerMovesets(true);
        s.setBetterImportantTrainerMovesets(true);
        s.setBetterRegularTrainerMovesets(true);
        new TrainerMovesetRandomizer(romHandler, s, new Random(20260712L)).randomizeTrainerMovesets();

        List<Move> allMoves = romHandler.getMoves();
        List<String> violations = new ArrayList<>();
        Map<Integer, Integer> moveCounts = new HashMap<>();
        int tpCount = 0;
        int printed = 0;
        int resetCount = 0;   // empty-pool mons handed back to the game for their natural level-up moveset
        int naturalCount = 0; // moveless mons in trainers with no custom moves (e.g. first-rival) -> game fills them

        for (Trainer tr : romHandler.getTrainers()) {
            boolean printThis = printed < SAMPLE_MOVESETS_PER_ROM;
            for (TrainerPokemon tp : tr.getPokemon()) {
                tpCount++;
                Species pk = tp.getSpecies();
                int ability = safeAbility(romHandler, tp);
                Set<Integer> movesThisMon = new HashSet<>();
                int nonZero = 0;
                StringBuilder line = new StringBuilder("  [" + tierOf(tr) + "] L" + tp.getLevel() + " "
                        + pk.getName() + " (" + typeStr(pk) + "): ");

                for (int moveID : tp.getMoves()) {
                    if (moveID == 0) {
                        continue;
                    }
                    nonZero++;
                    moveCounts.merge(moveID, 1, Integer::sum);
                    String moveName = allMoves.get(moveID).name;
                    line.append(moveName).append(' ');
                    if (!movesThisMon.add(moveID)) {
                        violations.add(romName + ": duplicate move " + moveName + " on " + pk.getName());
                    }
                    String weather = weatherRedundancy(moveID, pk, ability);
                    if (weather != null) {
                        violations.add(romName + ": " + weather);
                    }
                    if (moveID == MoveIDs.trickRoom && pk.getSpeed() > 60) {
                        violations.add(romName + ": Trick Room on fast " + pk.getName() + " (spe " + pk.getSpeed() + ")");
                    }
                }
                // Sleep Talk and Snore are useless without Rest, so they may only appear alongside it.
                if (!movesThisMon.contains(MoveIDs.rest)) {
                    if (movesThisMon.contains(MoveIDs.sleepTalk)) {
                        violations.add(romName + ": Sleep Talk without Rest on " + pk.getName());
                    }
                    if (movesThisMon.contains(MoveIDs.snore)) {
                        violations.add(romName + ": Snore without Rest on " + pk.getName());
                    }
                }
                // Spit Up and Swallow consume Stockpile counters, so they may only appear alongside Stockpile.
                if (!movesThisMon.contains(MoveIDs.stockpile)) {
                    if (movesThisMon.contains(MoveIDs.spitUp)) {
                        violations.add(romName + ": Spit Up without Stockpile on " + pk.getName());
                    }
                    if (movesThisMon.contains(MoveIDs.swallow)) {
                        violations.add(romName + ": Swallow without Stockpile on " + pk.getName());
                    }
                }
                if (nonZero == 0) {
                    // A mon with empty move slots is only genuinely moveless in-game when its trainer writes
                    // custom moves yet this mon is neither reset nor given any. When the trainer has no custom
                    // moves at all - e.g. the intentionally-excluded first-rival battle - the game supplies the
                    // natural level-up moveset, so that is expected, not a regression.
                    if (tp.isResetMoves()) {
                        resetCount++;
                    } else if (tr.pokemonHaveCustomMoves()) {
                        violations.add(romName + ": moveless " + pk.getName() + " in a custom-move trainer (no moves, no reset)");
                    } else {
                        naturalCount++;
                    }
                }
                if (printThis) {
                    System.out.println(line);
                }
            }
            if (printThis && !tr.getPokemon().isEmpty()) {
                printed++;
            }
        }

        int finalTp = tpCount;
        for (Map.Entry<Integer, Integer> e : moveCounts.entrySet()) {
            double rate = (double) e.getValue() / (double) finalTp;
            if (rate >= UBIQUITOUS_RATE) {
                violations.add(String.format("%s: '%s' is ubiquitous (%.1f%% of mons)",
                        romName, allMoves.get(e.getKey()).name, rate * 100));
            }
        }
        System.out.printf("  -> %d trainer Pokemon, %d distinct moves, %d violation(s)%s%s%n",
                tpCount, moveCounts.size(), violations.size(),
                resetCount > 0 ? " (" + resetCount + " empty-pool mon(s) reset to natural moveset)" : "",
                naturalCount > 0 ? " (" + naturalCount + " excluded/no-custom-move mon(s) -> natural moveset)" : "");
        return violations;
    }

    // Returns a violation description if this weather move is redundant on the Pokemon, else null.
    private String weatherRedundancy(int moveID, Species pk, int ability) {
        boolean ok = switch (moveID) {
            case MoveIDs.rainDance -> hasType(pk, Type.WATER) || RAIN_ABILITIES.contains(ability);
            case MoveIDs.sunnyDay -> hasType(pk, Type.FIRE) || SUN_ABILITIES.contains(ability);
            case MoveIDs.sandstorm -> hasType(pk, Type.ROCK) || hasType(pk, Type.GROUND)
                    || hasType(pk, Type.STEEL) || SAND_ABILITIES.contains(ability);
            case MoveIDs.hail -> hasType(pk, Type.ICE) || HAIL_ABILITIES.contains(ability);
            default -> true;
        };
        if (ok) {
            return null;
        }
        String move = switch (moveID) {
            case MoveIDs.rainDance -> "Rain Dance";
            case MoveIDs.sunnyDay -> "Sunny Day";
            case MoveIDs.sandstorm -> "Sandstorm";
            default -> "Hail";
        };
        return "redundant " + move + " on " + pk.getName() + " (" + typeStr(pk) + "), ability " + ability;
    }

    private static int safeAbility(RomHandler romHandler, TrainerPokemon tp) {
        try {
            return romHandler.abilitiesPerSpecies() != 0 ? romHandler.getAbilityForTrainerPokemon(tp) : 0;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private static boolean hasType(Species pk, Type type) {
        return pk.getPrimaryType(false) == type || pk.getSecondaryType(false) == type;
    }

    private static String typeStr(Species pk) {
        Type t2 = pk.getSecondaryType(false);
        return pk.getPrimaryType(false) + (t2 == null ? "" : "/" + t2);
    }

    private static String tierOf(Trainer tr) {
        if (tr.isBoss()) {
            return "BOSS";
        }
        if (tr.isImportant()) {
            return "IMP ";
        }
        return "REG ";
    }

    private RomHandler tryLoad(String path) {
        for (Generation gen : new HashSet<>(Generation.GAME_TO_GENERATION.values())) {
            try {
                RomHandler.Factory factory = gen.createFactory();
                if (factory.isLoadable(path)) {
                    RomHandler rh = factory.create();
                    rh.loadRom(path);
                    return rh;
                }
            } catch (Throwable e) {
                System.out.println("  [gen " + gen.getNumber() + " load error for " + new File(path).getName()
                        + "]: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        return null;
    }
}
