package com.dabomstew.pkrandom.gamedata.cueh;

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

import com.dabomstew.pkrandom.gamedata.Evolution;
import com.dabomstew.pkrandom.gamedata.Gen1Species;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.gamedata.SpeciesSet;

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

    private final BasicSpeciesAction<T> nullBasicSpeciesAction = pk -> {};
    private final EvolvedSpeciesAction<T> nullEvolvedSpeciesAction = (evFrom, evTo, toMonIsFinalEvo) -> {};

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
     * @param copySplitEvos If false, split evos are treated as basic Pokemon {@link Species}, and will thus use basicAction instead of splitAction.
     */
    @SuppressWarnings("unchecked")
    private void apply(boolean evolutionSanity, boolean copySplitEvos) {

        SpeciesSet allSpecs = speciesSetSupplier.get();

        if (!evolutionSanity) {
            allSpecs.forEach(pk -> noEvoAction.applyTo((T) pk));
            return;
        }

        SpeciesSet basicSpecs = allSpecs.filterFirstEvolutionAvailable(false, false);
        SpeciesSet splitEvos = allSpecs.filterSplitEvolutions(false);
        SpeciesSet finalEvos = allSpecs.filterFinalEvos(false);

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

}
