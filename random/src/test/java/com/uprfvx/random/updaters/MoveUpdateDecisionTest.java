package com.uprfvx.random.updaters;

import com.uprfvx.romio.constants.MoveIDs;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.MoveCategory;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MoveUpdateDecisionTest {

    @Test
    public void updateMovesAppliesPowerAccuracyPpAndTypeChangesWithoutRom() {
        List<Move> moves = syntheticMoves();
        MoveUpdater updater = new MoveUpdater(romHandler(moves, 1));

        updater.updateMoves(4);

        assertEquals(Type.FIGHTING, moves.get(MoveIDs.karateChop).type);
        assertEquals(60, moves.get(MoveIDs.wingAttack).power);
        assertEquals(100.0, moves.get(MoveIDs.whirlwind).hitratio);
        assertEquals(15, moves.get(MoveIDs.vineWhip).pp);
        assertFalse(updater.getUpdates().isEmpty());
    }

    private static List<Move> syntheticMoves() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Move move = new Move();
            move.number = i;
            move.internalId = i;
            move.name = "Move " + i;
            move.power = 1;
            move.pp = 1;
            move.hitratio = 1;
            move.type = Type.NORMAL;
            move.category = MoveCategory.PHYSICAL;
            moves.add(move);
        }
        return moves;
    }

    private static RomHandler romHandler(List<Move> moves, int generationOfPokemon) {
        return (RomHandler) Proxy.newProxyInstance(RomHandler.class.getClassLoader(), new Class<?>[]{RomHandler.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMoves" -> moves;
                    case "generationOfPokemon" -> generationOfPokemon;
                    case "getPerfectAccuracy" -> 101;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
