package com.uprfvx.random.log;

import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomizationLoggerTest {

    @Test
    void trainerNameForLogFallsBackWhenTrainerIndexExceedsOriginalNames() {
        Trainer trainer = new Trainer();
        trainer.setIndex(595);
        trainer.setFullDisplayName("Bug Catcher Kai");

        assertEquals("Bug Catcher Kai",
                RandomizationLogger.trainerNameForLog(List.of("Youngster Ben"), trainer));
    }

    @Test
    void trainerNameForLogFallsBackToTrainerIndexWhenNoNameExists() {
        Trainer trainer = new Trainer();
        trainer.setIndex(595);

        assertEquals("trainer #595", RandomizationLogger.trainerNameForLog(List.of("Youngster Ben"), trainer));
    }

    @Test
    void formatTrainerPokemonForLogFallsBackForMissingSpecies() {
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setLevel(12);

        assertEquals("unknown species Lv12", RandomizationLogger.formatTrainerPokemonForLog(trainerPokemon));
    }

    @Test
    void speciesNameForLogIncludesSpeciesNumbersWhenNameMetadataIsMissing() {
        Species species = new Species(595);
        species.setSpeciesSetIdentityNumber(900);

        assertEquals("unknown species #595 identity=900", RandomizationLogger.speciesNameForLog(species));
    }

    @Test
    void moveNameForLogFallsBackWhenMoveIndexExceedsMoveList() {
        Move move = new Move();
        move.name = "Tackle";

        assertEquals("Tackle", RandomizationLogger.moveNameForLog(Arrays.asList(null, move), 1));
        assertEquals("unknown move #595", RandomizationLogger.moveNameForLog(Arrays.asList(null, move), 595));
    }
}
