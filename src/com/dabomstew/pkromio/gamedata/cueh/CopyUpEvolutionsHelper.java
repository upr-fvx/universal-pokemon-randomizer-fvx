package com.dabomstew.pkromio.gamedata.cueh;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkromio.gamedata.Evolution;
import com.dabomstew.pkromio.gamedata.Gen1Species;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.SpeciesSet;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * Universal implementation for things that have "copy X up evolutions" support.<br>
 * Assumes no two Species evolve into the same third Species. Note this might not hold true if evolutions are
 * randomized.<br>
 * Another assumption made, is that evolutions and prevolutions of a Species of generic type T are also
 * of T - E.g. that the evolutions and prevolutions of a {@link Gen1Species} are
 * always Gen1Species.
 */
public class CopyUpEvolutionsHelper<T extends Species> {

    private final BasicSpeciesAction<T> nullBasicSpeciesAction = pk -> {
    };
    private final EvolvedSpeciesAction<T> nullEvolvedSpeciesAction = (evFrom, evTo, toMonIsFinalEvo) -> {
    };

    private final Supplier<SpeciesSet> speciesSetSupplier;

    private BasicSpeciesAction<T> noEvoAction;
    private BasicSpeciesAction<T> basicAction;
    private EvolvedSpeciesAction<T> evolvedAction;
    private EvolvedSpeciesAction<T> splitAction;

    public CopyUpEvolutionsHelper(SpeciesSet speciesSet) {
        this.speciesSetSupplier = () -> speciesSet;
    }

    public CopyUpEvolutionsHelper(Supplier<SpeciesSet> speciesSetSupplier) {
        this.speciesSetSupplier = speciesSetSupplier;
    }

    /**
     * Sets the method to run on all {@link Species}, when evolutionSanity == false. (see {@link #apply(boolean, boolean)})
     */
    private void setNoEvoAction(BasicSpeciesAction<T> noEvoAction) {
        this.noEvoAction = noEvoAction == null ? nullBasicSpeciesAction : noEvoAction;
    }

    /**
     * Sets the method to run on basic {@link Species}.
     */
    private void setBasicAction(BasicSpeciesAction<T> basicAction) {
        this.basicAction = basicAction == null ? nullBasicSpeciesAction : basicAction;
    }

    /**
     * Sets the method to run on evolved {@link Species}.
     */
    private void setEvolvedAction(EvolvedSpeciesAction<T> evolvedAction) {
        this.evolvedAction = evolvedAction == null ? nullEvolvedSpeciesAction : evolvedAction;
    }

    /**
     * Sets the method to run on split evos.
     */
    private void setSplitAction(EvolvedSpeciesAction<T> splitAction) {
        this.splitAction = splitAction == null ? nullEvolvedSpeciesAction : splitAction;
    }

    /**
     * Applies the CopyUpEvolutionsHelper, using the {@link SpeciesSet} given by the constructor,
     * boolean options, and a number of circumstantial "actions".
     * Any action argument can be set to null, to have it do nothing.
     *
     * @param evolutionSanity If false, the noEvoAction will be used on all {@link Species}.
     * @param copySplitEvos   If false, split evos are treated as basic Pokemon {@link Species}, and
     *                        will thus use bpAction instead of splitAction.
     * @param bpAction        Method to run on all basic Pokemon {@link Species}.
     * @param epAction        Method to run on all evolved Pokemon {@link Species} that are not
     *                        "split evos" (e.g. Venusaur, Metapod).
     * @param splitAction     Method to run on all evolved Pokemon {@link Species} that are "split
     *                        evos" (e.g. Poliwrath and Politoed, Silcoon and
     *                        Cascoon).
     * @param noEvoAction     Method to run on all {@link Species}, when evolutionSanity ==
     *                        false.
     */
    public void apply(boolean evolutionSanity, boolean copySplitEvos, BasicSpeciesAction<T> bpAction,
                      EvolvedSpeciesAction<T> epAction, EvolvedSpeciesAction<T> splitAction, BasicSpeciesAction<T> noEvoAction) {
        setBasicAction(bpAction);
        setEvolvedAction(epAction);
        setSplitAction(splitAction);
        setNoEvoAction(noEvoAction);

        apply(evolutionSanity, copySplitEvos);
    }

    /**
     * A simplified version of {@link #apply(boolean, boolean, BasicSpeciesAction, EvolvedSpeciesAction, EvolvedSpeciesAction, BasicSpeciesAction)},
     * which supposes split evos are treated the same as other evolved {@link Species},
     * and that the bpAction is used when evolutionSanity == false.
     *
     * @param evolutionSanity If false, the bpAction will be used on all {@link Species}.
     * @param copySplitEvos   If false, split evos (e.g. Poliwrath and Politoed) are
     *                        treated as basic Pokemon {@link Species}, and will thus use bpAction
     *                        instead of epAction.
     * @param bpAction        Method to run on all basic Pokemon {@link Species}.
     * @param epAction        Method to run on all evolved Pokemon {@link Species}.
     */
    public void apply(boolean evolutionSanity, boolean copySplitEvos, BasicSpeciesAction<T> bpAction,
                      EvolvedSpeciesAction<T> epAction) {
        setBasicAction(bpAction);
        setEvolvedAction(epAction);
        setSplitAction(epAction);
        setNoEvoAction(bpAction);

        apply(evolutionSanity, copySplitEvos);
    }

    /**
     * @param evolutionSanity If false, the noEvoAction will be used on all {@link Species}.
     * @param copySplitEvos   If false, split evos are treated as basic Pokemon {@link Species}, and will thus use basicAction instead of splitAction.
     */
    @SuppressWarnings("unchecked")
    private void apply(boolean evolutionSanity, boolean copySplitEvos) {

        SpeciesSet allSpecs = speciesSetSupplier.get();

        if (!evolutionSanity) {
            allSpecs.forEach(pk -> noEvoAction.applyTo((T) pk));
            return;
        }

        SpeciesSet basicSpecs = allSpecs.filter(spec -> isBasicSpecies(allSpecs, spec));
        SpeciesSet splitEvos = allSpecs.filter(spec -> isSplitEvo(allSpecs, spec));
        SpeciesSet finalEvos = allSpecs.filter(spec -> isFinalEvo(allSpecs, spec));

        Set<Species> processed = new HashSet<>();

        if (!copySplitEvos) {
            basicSpecs.addAll(splitEvos);
        }

        for (Species pk : basicSpecs) {
            basicAction.applyTo((T) pk);
            processed.add(pk);
        }

        // go "up" evolutions looking for pre-evos to do first
        for (Species pk : allSpecs) {
            if (!processed.contains(pk)) {

                // Non-processed specs at this point must have
                // a linear chain of single evolutions down to
                // a processed spec.
                Stack<Evolution> evStack = new Stack<>();
                Evolution ev = pk.getEvolutionsTo().get(0);
                while (!processed.contains(ev.getFrom())) {
                    evStack.push(ev);
                    ev = ev.getFrom().getEvolutionsTo().get(0);
                }
                evStack.push(ev);

                // Now "ev" is set to an evolution from a Species that has had
                // the base action done on it to one that hasn't.
                // Do the evolution action for everything left on the stack.
                while (!evStack.isEmpty()) {
                    ev = evStack.pop();
                    if (copySplitEvos && splitEvos.contains(ev.getTo())) {
                        splitAction.applyTo((T) ev.getFrom(), (T) ev.getTo(), finalEvos.contains(ev.getTo()));
                    } else {
                        evolvedAction.applyTo((T) ev.getFrom(), (T) ev.getTo(), finalEvos.contains(ev.getTo()));
                    }
                    processed.add(ev.getTo());
                }

            }
        }
    }

    // SpeciesSet has inbuilt filter methods for different evolutionary stages.
    // However, those assume alt forms evolve from the prevos of their base form,
    // and can thus not be used in this class.
    // At some point, a proper form rewrite is due, but until then the methods below will do.
    // TODO: the form rewrite
    // Note that the below methods function in a way that ignores all Species outside of the
    // given SpeciesSet. This should make them usable with smaller SpeciesSet / species restrictions.
    // However, at the time of writing all CopyUpEvolutionsHelpers use RomHandler::getSpeciesSet...
    // TODO: integrate with species restrictions
    // Also, there is some risk/possible bug when one Species evolves into the same other Species
    // in two different ways. Feebas, Meowstic, Pikachu/Exeggute/Cubone (USUM), and Species granted
    // extra Evolutions when removing time-based evos are of notice here.
    // TODO: investigate split evos into the same species

    /**
     * Returns true if spec has no other {@link Species} in allSpecs that evolves into it.
     */
    private boolean isBasicSpecies(SpeciesSet allSpecs, Species spec) {
        for (Evolution evo : spec.getEvolutionsTo()) {
            if (allSpecs.contains(evo.getFrom())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if spec evolves from some other {@link Species} in allSpecs,
     * which in turn evolves into at least 2 {@link Species} in allSpecs.
     */
    private boolean isSplitEvo(SpeciesSet allSpecs, Species spec) {
        // TODO: there was a notion in earlier code, of treating Ninjask only as a non-split evo
        //  (or technically Species which evolved through EvolutionType.LEVEL_CREATE_EXTRA).
        //  Is this something we want to recreate?
        for (Evolution evo : spec.getEvolutionsTo()) {
            if (allSpecs.contains(evo.getFrom())) {
                long evoCount = evo.getFrom().getEvolutionsFrom().stream()
                        .map(Evolution::getTo)
                        .filter(allSpecs::contains)
                        .count();
                if (evoCount > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if spec is not a {@link #isBasicSpecies(SpeciesSet, Species) basic species},
     * and also spec does not evolve into any other {@link Species} in allSpecs.
     */
    private boolean isFinalEvo(SpeciesSet allSpecs, Species spec) {
        if (isBasicSpecies(allSpecs, spec)) {
            return false;
        }
        for (Evolution evo : spec.getEvolutionsFrom()) {
            if (allSpecs.contains(evo.getTo())) {
                return false;
            }
        }
        return true;
    }


}
