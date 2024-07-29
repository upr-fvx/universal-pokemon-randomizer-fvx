package com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkrandom.constants.SpeciesIDs;
import com.dabomstew.pkrandom.game_data.Species;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PokemonBaseStatUpdater extends Updater {

    // starts with two null-consumers so the indexing can be nicer,
    // and then four more since Gens 2-5 didn't change the base stats of any existing Pokemon
    private final List<Consumer<List<Species>>> updates = Arrays.asList(
            l -> {}, l -> {},
            l -> {}, l -> {}, l -> {}, l -> {},
            this::gen6Updates, this::gen7Updates, this::gen8Updates, this::gen9Updates
    );

    public PokemonBaseStatUpdater(RomHandler romHandler) {
        super(romHandler);
    }

    public void updatePokemonStats(int updateToGen) {
        if (updateToGen > updates.size() - 1) {
            throw new IllegalArgumentException("updateToGen too high, can't update to Gen " + updateToGen);
        }
        List<Species> pokes = romHandler.getSpeciesInclFormes();

        for (int i = 2; i <= updates.size(); i++) {
            if (updateToGen >= i && romHandler.generationOfPokemon() < i) {
                updates.get(i).accept(pokes);
            }
        }

        updated = true;
    }

    private void gen6Updates(List<Species> pokes) {
        if (romHandler.generationOfPokemon() == 1) {
            // These are the Gen 1 Pokemon that otherwise get their SpAtk updated
            updateSpecial(pokes, SpeciesIDs.butterfree, 90);
            updateSpecial(pokes, SpeciesIDs.clefable, 95);
            updateSpecial(pokes, SpeciesIDs.vileplume, 110);
        }

        updateSpAtk(pokes, SpeciesIDs.butterfree, 90);
        updateAtk(pokes, SpeciesIDs.beedrill, 90);
        updateSpeed(pokes, SpeciesIDs.pidgeot, 101);
        updateSpeed(pokes, SpeciesIDs.raichu, 92);
        updateAtk(pokes, SpeciesIDs.nidoqueen, 92);
        updateAtk(pokes, SpeciesIDs.nidoking, 102);
        updateSpAtk(pokes, SpeciesIDs.clefable, 95);
        updateSpAtk(pokes, SpeciesIDs.vileplume, 110);
        updateAtk(pokes, SpeciesIDs.poliwrath, 95);
        updateSpDef(pokes, SpeciesIDs.alakazam, 95);
        updateSpDef(pokes, SpeciesIDs.victreebel, 70);
        updateAtk(pokes, SpeciesIDs.golem, 120);

        if (romHandler.generationOfPokemon() >= 2) {
            updateDef(pokes, SpeciesIDs.ampharos, 85);
            updateDef(pokes, SpeciesIDs.bellossom, 95);
            updateSpAtk(pokes, SpeciesIDs.azumarill, 60);
            updateSpDef(pokes, SpeciesIDs.jumpluff, 95);
        }
        if (romHandler.generationOfPokemon() >= 3) {
            updateSpAtk(pokes, SpeciesIDs.beautifly, 100);
            updateSpDef(pokes, SpeciesIDs.exploud, 73);
        }
        if (romHandler.generationOfPokemon() >= 4) {
            updateSpDef(pokes, SpeciesIDs.staraptor, 60);
            updateDef(pokes, SpeciesIDs.roserade, 65);
        }
        if (romHandler.generationOfPokemon() >= 5) {
            updateAtk(pokes, SpeciesIDs.stoutland, 110);
            updateAtk(pokes, SpeciesIDs.unfezant, 115);
            updateSpDef(pokes, SpeciesIDs.gigalith, 80);
            updateAtk(pokes, SpeciesIDs.seismitoad, 95);
            updateSpDef(pokes, SpeciesIDs.leavanny, 80);
            updateAtk(pokes, SpeciesIDs.scolipede, 100);
            updateDef(pokes, SpeciesIDs.krookodile, 80);
        }
    }

    private void gen7Updates(List<Species> pokes) {
        updateAtk(pokes, SpeciesIDs.arbok, 95);
        updateAtk(pokes, SpeciesIDs.dugtrio, 100);
        updateAtk(pokes, SpeciesIDs.farfetchd, 90);
        updateSpeed(pokes, SpeciesIDs.dodrio, 110);
        updateSpeed(pokes, SpeciesIDs.electrode, 150);
        updateSpDef(pokes, SpeciesIDs.exeggutor, 75);

        if (romHandler.generationOfPokemon() >= 2) {
            updateSpAtk(pokes, SpeciesIDs.noctowl, 86);
            updateSpDef(pokes, SpeciesIDs.ariados, 70);
            updateDef(pokes, SpeciesIDs.qwilfish, 85);
            updateHP(pokes, SpeciesIDs.magcargo, 60);
            updateSpAtk(pokes, SpeciesIDs.magcargo, 90);
            updateHP(pokes, SpeciesIDs.corsola, 65);
            updateDef(pokes, SpeciesIDs.corsola, 95);
            updateSpDef(pokes, SpeciesIDs.corsola, 95);
            updateHP(pokes, SpeciesIDs.mantine, 85);
        }
        if (romHandler.generationOfPokemon() >= 3) {
            updateSpAtk(pokes, SpeciesIDs.swellow, 75);
            updateSpAtk(pokes, SpeciesIDs.pelipper, 95);
            updateSpAtk(pokes, SpeciesIDs.masquerain, 100);
            updateSpeed(pokes, SpeciesIDs.masquerain, 80);
            updateSpeed(pokes, SpeciesIDs.delcatty, 90);
            updateDef(pokes, SpeciesIDs.volbeat, 75);
            updateSpDef(pokes, SpeciesIDs.volbeat, 85);
            updateDef(pokes, SpeciesIDs.illumise, 75);
            updateSpDef(pokes, SpeciesIDs.illumise, 85);
            updateHP(pokes, SpeciesIDs.lunatone, 90);
            updateHP(pokes, SpeciesIDs.solrock, 90);
            updateHP(pokes, SpeciesIDs.chimecho, 75);
            updateDef(pokes, SpeciesIDs.chimecho, 80);
            updateSpDef(pokes, SpeciesIDs.chimecho, 90);
        }
        if (romHandler.generationOfPokemon() >= 5) {
            updateHP(pokes, SpeciesIDs.woobat, 65);
            updateAtk(pokes, SpeciesIDs.crustle, 105);
            updateAtk(pokes, SpeciesIDs.beartic, 130);
            updateHP(pokes, SpeciesIDs.cryogonal, 80);
            updateDef(pokes, SpeciesIDs.cryogonal, 50);
        }
        if (romHandler.generationOfPokemon() == 6) {
            updateSpDef(pokes, SpeciesIDs.Gen6Formes.alakazamMega, 105);
            updateAtk(pokes, SpeciesIDs.Gen6Formes.aegislashB, 140);
            updateSpAtk(pokes, SpeciesIDs.Gen6Formes.aegislashB, 140);
        }
    }

    private void gen8Updates(List<Species> pokes) {
        if (romHandler.generationOfPokemon() >= 6) {
            updateDef(pokes, SpeciesIDs.aegislash, 140);
            updateSpDef(pokes, SpeciesIDs.aegislash, 140);
            int aegislashBlade;
            if (romHandler.generationOfPokemon() == 6) {
                aegislashBlade = SpeciesIDs.Gen6Formes.aegislashB;
            } else { // Gen 7
                aegislashBlade = romHandler.isUSUM() ? SpeciesIDs.USUMFormes.aegislashB : SpeciesIDs.SMFormes.aegislashB;
            }
            updateAtk(pokes, aegislashBlade, 140);
            updateSpAtk(pokes, aegislashBlade, 140);
        }
    }

    private void gen9Updates(List<Species> pokes) {
        if (romHandler.generationOfPokemon() >= 4) {
            updateDef(pokes, SpeciesIDs.cresselia, 110);
            updateSpDef(pokes, SpeciesIDs.cresselia, 120);
        }
        if (romHandler.generationOfPokemon() >= 8) {
            updateAtk(pokes, SpeciesIDs.zacian, 120);
            updateAtk(pokes, SpeciesIDs.zamazenta, 120);
        }
    }

    private void updateHP(List<Species> pokes, int species, int value) {
        pokes.get(species).setHp(value);
    }

    private void updateAtk(List<Species> pokes, int species, int value) {
        pokes.get(species).setAttack(value);
    }

    private void updateDef(List<Species> pokes, int species, int value) {
        pokes.get(species).setDefense(value);
    }

    private void updateSpAtk(List<Species> pokes, int species, int value) {
        // just gets ignored in Gen 1 games
        if (romHandler.generationOfPokemon() != 1) {
            pokes.get(species).setSpatk(value);
        }
    }

    private void updateSpDef(List<Species> pokes, int species, int value) {
        // just gets ignored in Gen 1 games
        if (romHandler.generationOfPokemon() != 1) {
            pokes.get(species).setSpdef(value);
        }
    }

    private void updateSpeed(List<Species> pokes, int species, int value) {
        pokes.get(species).setSpeed(value);
    }

    private void updateSpecial(List<Species> pokes, int species, int value) {
        // just gets ignored in non-Gen 1 games
        if (romHandler.generationOfPokemon() == 1) {
            pokes.get(species).setSpecial(value);
        }
    }

}
