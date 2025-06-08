package com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkromio.constants.MoveIDs;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveCategory;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class MoveUpdater extends Updater<Move, MoveUpdateType, Object> {

    private final Map<Move, Map<MoveUpdateType, Update<Object>>> moveUpdates = new TreeMap<>();

    // starts with two null-consumers so the indexing can be nicer
    private final List<Consumer<List<Move>>> updates = Arrays.asList(
            l -> {
            }, l -> {
            },
            this::gen2Updates, this::gen3Updates, this::gen4Updates, this::gen5Updates,
            this::gen6Updates, this::gen7Updates, this::gen8Updates, this::gen9Updates
    );

    public MoveUpdater(RomHandler romHandler) {
        super(romHandler);
    }

    public Map<Move, Map<MoveUpdateType, Update<Object>>> getUpdates() {
        return moveUpdates;
    }

    public void updateMoves(int updateToGen) {
        if (updateToGen > updates.size() - 1) {
            throw new IllegalArgumentException("updateToGen too high, can't update to Gen " + updateToGen);
        }
        List<Move> moves = romHandler.getMoves();

        for (int i = 2; i <= updates.size(); i++) {
            if (updateToGen >= i && romHandler.generationOfPokemon() < i) {
                updates.get(i).accept(moves);
            }
        }
    }

    private void gen2Updates(List<Move> moves) {
        updateMoveType(moves, MoveIDs.karateChop, Type.FIGHTING);
        updateMoveType(moves, MoveIDs.gust, Type.FLYING);
        updateMovePower(moves, MoveIDs.wingAttack, 60);
        updateMoveAccuracy(moves, MoveIDs.whirlwind, 100);
        updateMoveType(moves, MoveIDs.sandAttack, Type.GROUND);
        updateMovePower(moves, MoveIDs.doubleEdge, 120);
        updateMoveAccuracy(moves, MoveIDs.blizzard, 70);
        updateMoveAccuracy(moves, MoveIDs.rockThrow, 90);
        updateMoveAccuracy(moves, MoveIDs.hypnosis, 60);
        updateMovePower(moves, MoveIDs.selfDestruct, 200);
        updateMovePower(moves, MoveIDs.explosion, 250);
        updateMovePower(moves, MoveIDs.dig, 60);
        // Bite also becomes dark between Gen 1 and 2, but we can't change moves to non-existing types
    }

    private void gen3Updates(List<Move> moves) {
        updateMoveAccuracy(moves, MoveIDs.razorWind, 100);
        // Low Kick is a good example of how these "updates" don't update any mechanics,
        // just power/accuracy/pp/category/Type
        // Low Kick gets the accuracy boost of Gen 3+, but not the weight-based power.
        updateMoveAccuracy(moves, MoveIDs.lowKick, 100);
    }

    private void gen4Updates(List<Move> moves) {
        updateMovePower(moves, MoveIDs.fly, 90);
        updateMovePP(moves, MoveIDs.vineWhip, 15);
        updateMovePP(moves, MoveIDs.absorb, 25);
        updateMovePP(moves, MoveIDs.megaDrain, 15);
        updateMovePower(moves, MoveIDs.dig, 80);
        updateMovePP(moves, MoveIDs.recover, 10);
        updateMoveAccuracy(moves, MoveIDs.flash, 100);
        updateMovePower(moves, MoveIDs.petalDance, 90);
        updateMoveAccuracy(moves, MoveIDs.disable, 80);
        updateMovePower(moves, MoveIDs.jumpKick, 85);
        updateMovePower(moves, MoveIDs.highJumpKick, 100);

        if (romHandler.generationOfPokemon() >= 2) {
            updateMovePower(moves, MoveIDs.zapCannon, 120);
            updateMovePower(moves, MoveIDs.outrage, 120);
            updateMovePP(moves, MoveIDs.outrage, 10);
            updateMovePP(moves, MoveIDs.gigaDrain, 10);
            updateMovePower(moves, MoveIDs.rockSmash, 40);
        }

        if (romHandler.generationOfPokemon() == 3) {
            updateMovePP(moves, MoveIDs.stockpile, 20);
            updateMovePower(moves, MoveIDs.dive, 80);
            updateMovePower(moves, MoveIDs.leafBlade, 90);
        }
    }

    private void gen5Updates(List<Move> moves) {
        updateMoveAccuracy(moves, MoveIDs.bind, 85);
        updateMovePP(moves, MoveIDs.jumpKick, 10);
        updateMovePower(moves, MoveIDs.jumpKick, 100);
        updateMovePower(moves, MoveIDs.tackle, 50);
        updateMoveAccuracy(moves, MoveIDs.tackle, 100);
        updateMoveAccuracy(moves, MoveIDs.wrap, 90);
        updateMovePP(moves, MoveIDs.thrash, 10);
        updateMovePower(moves, MoveIDs.thrash, 120);
        updateMoveAccuracy(moves, MoveIDs.disable, 100);
        updateMovePP(moves, MoveIDs.petalDance, 10);
        updateMovePower(moves, MoveIDs.petalDance, 120);
        updateMoveAccuracy(moves, MoveIDs.fireSpin, 85);
        updateMovePower(moves, MoveIDs.fireSpin, 35);
        updateMoveAccuracy(moves, MoveIDs.toxic, 90);
        updateMoveAccuracy(moves, MoveIDs.clamp, 85);
        updateMovePP(moves, MoveIDs.clamp, 15);
        updateMovePP(moves, MoveIDs.highJumpKick, 10);
        updateMovePower(moves, MoveIDs.highJumpKick, 130);
        updateMoveAccuracy(moves, MoveIDs.glare, 90);
        updateMoveAccuracy(moves, MoveIDs.poisonGas, 80);
        updateMoveAccuracy(moves, MoveIDs.crabhammer, 90);

        if (romHandler.generationOfPokemon() >= 2) {
            updateMoveType(moves, MoveIDs.curse, Type.GHOST);
            updateMoveAccuracy(moves, MoveIDs.cottonSpore, 100);
            updateMoveAccuracy(moves, MoveIDs.scaryFace, 100);
            updateMoveAccuracy(moves, MoveIDs.boneRush, 90);
            updateMovePower(moves, MoveIDs.gigaDrain, 75);
            updateMovePower(moves, MoveIDs.furyCutter, 20);
            updateMovePP(moves, MoveIDs.futureSight, 10);
            updateMovePower(moves, MoveIDs.futureSight, 100);
            updateMoveAccuracy(moves, MoveIDs.futureSight, 100);
            updateMovePower(moves, MoveIDs.whirlpool, 35);
            updateMoveAccuracy(moves, MoveIDs.whirlpool, 85);
        }

        if (romHandler.generationOfPokemon() >= 3) {
            updateMovePower(moves, MoveIDs.uproar, 90);
            updateMovePower(moves, MoveIDs.sandTomb, 35);
            updateMoveAccuracy(moves, MoveIDs.sandTomb, 85);
            updateMovePower(moves, MoveIDs.bulletSeed, 25);
            updateMovePower(moves, MoveIDs.icicleSpear, 25);
            updateMovePower(moves, MoveIDs.covet, 60);
            updateMoveAccuracy(moves, MoveIDs.rockBlast, 90);
            updateMovePower(moves, MoveIDs.doomDesire, 140);
            updateMoveAccuracy(moves, MoveIDs.doomDesire, 100);
        }

        if (romHandler.generationOfPokemon() == 4) {
            updateMovePower(moves, MoveIDs.feint, 30);
            updateMovePower(moves, MoveIDs.lastResort, 140);
            updateMovePP(moves, MoveIDs.drainPunch, 10);
            updateMovePower(moves, MoveIDs.drainPunch, 75);
            updateMoveAccuracy(moves, MoveIDs.magmaStorm, 75);
        }
    }

    private void gen6Updates(List<Move> moves) {
        updateMovePP(moves, MoveIDs.swordsDance, 20);
        updateMoveAccuracy(moves, MoveIDs.whirlwind, romHandler.getPerfectAccuracy());
        updateMovePP(moves, MoveIDs.vineWhip, 25);
        updateMovePower(moves, MoveIDs.vineWhip, 45);
        updateMovePower(moves, MoveIDs.pinMissile, 25);
        updateMoveAccuracy(moves, MoveIDs.pinMissile, 95);
        updateMovePower(moves, MoveIDs.flamethrower, 90);
        updateMovePower(moves, MoveIDs.hydroPump, 110);
        updateMovePower(moves, MoveIDs.surf, 90);
        updateMovePower(moves, MoveIDs.iceBeam, 90);
        updateMovePower(moves, MoveIDs.blizzard, 110);
        updateMovePP(moves, MoveIDs.growth, 20);
        updateMovePower(moves, MoveIDs.thunderbolt, 90);
        updateMovePower(moves, MoveIDs.thunder, 110);
        updateMovePP(moves, MoveIDs.minimize, 10);
        updateMovePP(moves, MoveIDs.barrier, 20);
        updateMovePower(moves, MoveIDs.lick, 30);
        updateMovePower(moves, MoveIDs.smog, 30);
        updateMovePower(moves, MoveIDs.fireBlast, 110);
        updateMovePP(moves, MoveIDs.skullBash, 10);
        updateMovePower(moves, MoveIDs.skullBash, 130);
        updateMoveAccuracy(moves, MoveIDs.glare, 100);
        updateMoveAccuracy(moves, MoveIDs.poisonGas, 90);
        updateMovePower(moves, MoveIDs.bubble, 40);
        updateMoveAccuracy(moves, MoveIDs.psywave, 100);
        updateMovePP(moves, MoveIDs.acidArmor, 20);
        updateMovePower(moves, MoveIDs.crabhammer, 100);

        if (romHandler.generationOfPokemon() >= 2) {
            updateMovePP(moves, MoveIDs.thief, 25);
            updateMovePower(moves, MoveIDs.thief, 60);
            updateMovePower(moves, MoveIDs.snore, 50);
            updateMovePower(moves, MoveIDs.furyCutter, 40);
            updateMovePower(moves, MoveIDs.futureSight, 120);
        }

        if (romHandler.generationOfPokemon() >= 3) {
            updateMovePower(moves, MoveIDs.heatWave, 95);
            updateMoveAccuracy(moves, MoveIDs.willOWisp, 85);
            updateMovePower(moves, MoveIDs.smellingSalts, 70);
            updateMovePower(moves, MoveIDs.knockOff, 65);
            updateMovePower(moves, MoveIDs.meteorMash, 90);
            updateMoveAccuracy(moves, MoveIDs.meteorMash, 90);
            updateMovePower(moves, MoveIDs.airCutter, 60);
            updateMovePower(moves, MoveIDs.overheat, 130);
            updateMovePP(moves, MoveIDs.rockTomb, 15);
            updateMovePower(moves, MoveIDs.rockTomb, 60);
            updateMoveAccuracy(moves, MoveIDs.rockTomb, 95);
            updateMovePP(moves, MoveIDs.extrasensory, 20);
            updateMovePower(moves, MoveIDs.muddyWater, 90);
            updateMovePP(moves, MoveIDs.covet, 25);
        }

        if (romHandler.generationOfPokemon() >= 4) {
            updateMovePower(moves, MoveIDs.wakeUpSlap, 70);
            updateMovePP(moves, MoveIDs.tailwind, 15);
            updateMovePower(moves, MoveIDs.assurance, 60);
            updateMoveAccuracy(moves, MoveIDs.psychoShift, 100);
            updateMovePower(moves, MoveIDs.auraSphere, 80);
            updateMovePP(moves, MoveIDs.airSlash, 15);
            updateMovePower(moves, MoveIDs.dragonPulse, 85);
            updateMovePower(moves, MoveIDs.powerGem, 80);
            updateMovePower(moves, MoveIDs.energyBall, 90);
            updateMovePower(moves, MoveIDs.dracoMeteor, 130);
            updateMovePower(moves, MoveIDs.leafStorm, 130);
            updateMoveAccuracy(moves, MoveIDs.gunkShot, 80);
            updateMovePower(moves, MoveIDs.chatter, 65);
            updateMovePower(moves, MoveIDs.magmaStorm, 100);
        }

        if (romHandler.generationOfPokemon() == 5) {
            updateMovePower(moves, MoveIDs.synchronoise, 120);
            updateMovePower(moves, MoveIDs.lowSweep, 65);
            updateMovePower(moves, MoveIDs.hex, 65);
            updateMovePower(moves, MoveIDs.incinerate, 60);
            updateMovePower(moves, MoveIDs.waterPledge, 80);
            updateMovePower(moves, MoveIDs.firePledge, 80);
            updateMovePower(moves, MoveIDs.grassPledge, 80);
            updateMovePower(moves, MoveIDs.struggleBug, 50);
            // Frost Breath and Storm Throw 45 Power
            // Crits are 2x in these games, so we need to multiply BP by 3/4
            // Storm Throw was also updated to have a base BP of 60
            updateMovePower(moves, MoveIDs.frostBreath, 45);
            updateMovePower(moves, MoveIDs.stormThrow, 45);
            updateMovePP(moves, MoveIDs.sacredSword, 15);
            updateMovePower(moves, MoveIDs.hurricane, 110);
            updateMovePower(moves, MoveIDs.technoBlast, 120);
        }
    }

    private void gen7Updates(List<Move> moves) {
        updateMovePower(moves, MoveIDs.leechLife, 80);
        updateMovePP(moves, MoveIDs.leechLife, 10);
        updateMovePP(moves, MoveIDs.submission, 20);
        updateMovePower(moves, MoveIDs.tackle, 40);
        updateMoveAccuracy(moves, MoveIDs.thunderWave, 90);

        if (romHandler.generationOfPokemon() >= 2) {
            updateMoveAccuracy(moves, MoveIDs.swagger, 85);
        }

        if (romHandler.generationOfPokemon() >= 3) {
            updateMovePP(moves, MoveIDs.knockOff, 20);
        }

        if (romHandler.generationOfPokemon() >= 4) {
            updateMoveAccuracy(moves, MoveIDs.darkVoid, 50);
            updateMovePower(moves, MoveIDs.suckerPunch, 70);
        }

        if (romHandler.generationOfPokemon() == 6) {
            updateMoveAccuracy(moves, MoveIDs.aromaticMist, romHandler.getPerfectAccuracy());
            updateMovePower(moves, MoveIDs.fellStinger, 50);
            updateMovePower(moves, MoveIDs.flyingPress, 100);
            updateMovePP(moves, MoveIDs.matBlock, 10);
            updateMovePower(moves, MoveIDs.mysticalFire, 75);
            updateMovePower(moves, MoveIDs.parabolicCharge, 65);
            updateMoveAccuracy(moves, MoveIDs.topsyTurvy, romHandler.getPerfectAccuracy());
            updateMoveCategory(moves, MoveIDs.waterShuriken, MoveCategory.SPECIAL);
        }
    }

    private void gen8Updates(List<Move> moves) {
        if (romHandler.generationOfPokemon() >= 2) {
            updateMovePower(moves, MoveIDs.rapidSpin, 50);
        }

        if (romHandler.generationOfPokemon() == 7) {
            updateMovePower(moves, MoveIDs.multiAttack, 120);
        }
    }

    private void gen9Updates(List<Move> moves) {
        updateMovePP(moves, MoveIDs.recover, 5);
        updateMovePP(moves, MoveIDs.softBoiled, 5);
        updateMovePP(moves, MoveIDs.rest, 5);

        if (romHandler.generationOfPokemon() >= 2) {
            updateMovePP(moves, MoveIDs.milkDrink, 5);
        }

        if (romHandler.generationOfPokemon() >= 3) {
            updateMovePower(moves, MoveIDs.lusterPurge, 95);
            updateMovePower(moves, MoveIDs.mistBall, 95);
            updateMovePP(moves, MoveIDs.slackOff, 5);
        }

        if (romHandler.generationOfPokemon() >= 4) {
            updateMovePP(moves, MoveIDs.roost, 5);
        }

        if (romHandler.generationOfPokemon() >= 7) {
            updateMovePP(moves, MoveIDs.shoreUp, 5);
        }

        if (romHandler.generationOfPokemon() >= 8) {
            updateMovePower(moves, MoveIDs.grassyGlide, 55);
            updateMovePower(moves, MoveIDs.wickedBlow, 75);
            updateMovePower(moves, MoveIDs.glacialLance, 120);
        }
    }

    private void updateMovePower(List<Move> moves, int moveNum, int power) {
        Move mv = moves.get(moveNum);
        if (mv.power != power) {
            int before = mv.power;
            mv.power = power;
            addUpdate(mv, before, power, MoveUpdateType.POWER);
        }
    }

    private void updateMovePP(List<Move> moves, int moveNum, int pp) {
        Move mv = moves.get(moveNum);
        if (mv.pp != pp) {
            int before = mv.pp;
            mv.pp = pp;
            addUpdate(mv, before, pp, MoveUpdateType.PP);
        }
    }

    private void updateMoveAccuracy(List<Move> moves, int moveNum, double accuracy) {
        Move mv = moves.get(moveNum);
        if (Math.abs(mv.hitratio - accuracy) >= 1) {
            double before = mv.hitratio;
            mv.hitratio = accuracy;
            addUpdate(mv, before, accuracy, MoveUpdateType.ACCURACY);
        }
    }

    private void updateMoveType(List<Move> moves, int moveNum, Type type) {
        Move mv = moves.get(moveNum);
        if (mv.type != type) {
            Type before = mv.type;
            mv.type = type;
            addUpdate(mv, before, type, MoveUpdateType.TYPE);
        }
    }

    private void updateMoveCategory(List<Move> moves, int moveNum, MoveCategory category) {
        Move mv = moves.get(moveNum);
        if (mv.category != category) {
            MoveCategory before = mv.category;
            mv.category = category;
            addUpdate(mv, before, category, MoveUpdateType.CATEGORY);
        }
    }

    private void addUpdate(Move move, Object before, Object after, MoveUpdateType type) {
        if (!moveUpdates.containsKey(move)) {
            moveUpdates.put(move, new TreeMap<>());
        }
        moveUpdates.get(move).put(type, new Update<>(before, after));
    }

}
