package com.uprfvx.romio.gamedata.cueh;

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

import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * Universal implementation for things that have "copy X up evolutions/formes" support.<br>
 * This class allows for running a {@link BasicSpeciesAction} on each basic (non-evolved) {@link Species},
 * and then a {@link EvolvedSpeciesAction} on each Species they evolve into.<br>
 * It also allows for further control, e.g. giving a separate EvolvedSpeciesAction to split evolutions.
 * See {@link Options.Builder} for more details.
 * <br><br>
 * This class also allows for running a {@link AltFormeAction} on each forme. Non-cosmetic formes and
 * cosmetic formes have their own actions.<br>
 * Some Species are both alt formes and have evolution into them (e.g. Meowstic-F, Lycanroc-Midnight, Raticate-Alolan).
 * These are treated as evolved species rather than alt formes, and thus use the EvolvedSpeciesActions rather
 * than the AltFormeActions.
 * <br><br>
 * This class ensures actions are applied in the correct order, so traits altered by the basic action
 * can be copied up to all evolutions and alt formes. <b>E.g.</b> Magby -> Magmar -> Magmortar will always
 * be applied in that order. More complicated "lines" like Rattata's in USUM will too.
 * Rattata -> Raticate; Rattata -> Rattata-Alolan -> Raticate-Alolan -> Raticate-Alolan-Totem.
 * Note here that Raticate-Alolan has no direct connection to Raticate.
 * <br><br>
 * Note that this class does NOT ensure that Species in the same line are applied <i>directly</i>
 * after one another. After applying the action to Magby, any number of species actions may be applied before
 * getting to Magmar. It only ensures Magmar does not come before Magby.
 * <br><br>
 * This class ignores all Species outside the given SpeciesSet. This affects which Species
 * it considers to be e.g. basic or split evolutions. <b>E.g.</b>, if the given SpeciesSet contains only
 * Poliwhirl and Poliwrath, but not Poliwag nor Politoed, Poliwhirl will be considered basic,
 * and Poliwrath will NOT be considered a split evolution.<br>
 * If an alt forme is in the SpeciesSet, but its conceptual base forme is not, the alt forme will be
 * treated like a base forme.
 * <br><br>
 * This class assumes no two Species evolve into the same third Species.
 * Note this might not hold true if evolutions are randomized.
 */
public class CopyUpEvolutionsHelper {

    public static class Options {

        private final boolean evolutionSanity;
        private final boolean copySplitEvos;
        private final boolean treatMegasAsEvos;
        private final BasicSpeciesAction noEvoAction;
        private final EvolvedSpeciesAction evolvedAction;
        private final BasicSpeciesAction basicAction;
        private final EvolvedSpeciesAction splitAction;
        private final AltFormeAction altFormeAction;
        private final AltFormeAction cosmeticAction;

        private Options(boolean evolutionSanity, boolean copySplitEvos, boolean treatMegasAsEvos,
                        BasicSpeciesAction noEvoAction,
                        BasicSpeciesAction basicAction,
                        EvolvedSpeciesAction evolvedAction,
                        EvolvedSpeciesAction splitAction,
                        AltFormeAction altFormeAction,
                        AltFormeAction cosmeticAction) {
            this.evolutionSanity = evolutionSanity;
            this.copySplitEvos = copySplitEvos;
            this.treatMegasAsEvos = treatMegasAsEvos;
            this.noEvoAction = noEvoAction;
            this.basicAction = basicAction;
            this.evolvedAction = evolvedAction;
            this.splitAction = splitAction;
            this.altFormeAction = altFormeAction;
            this.cosmeticAction = cosmeticAction;
        }

        public static class Builder {
            private final BasicSpeciesAction nullBasicSpeciesAction = _ -> {};
            private final EvolvedSpeciesAction nullEvolvedSpeciesAction = (_, _, _) -> {};
            private final AltFormeAction nullAltFormeAction = (_, _) -> {};

            private boolean evolutionSanity = true;
            private boolean copySplitEvos;
            private boolean treatMegasAsEvos;

            private final BasicSpeciesAction basicAction;
            private final EvolvedSpeciesAction evolvedAction;
            private BasicSpeciesAction noEvoAction;
            private EvolvedSpeciesAction splitAction;
            private AltFormeAction altFormeAction;
            private AltFormeAction cosmeticAction;

            public Builder(BasicSpeciesAction basicAction, EvolvedSpeciesAction evolvedAction) {
                this.basicAction = basicAction;
                this.evolvedAction = evolvedAction;
            }

            /**
             * If false, the noEvoAction will be used on all {@link Species} that are base formes.
             * The altFormeAction and cosmeticAction are not affected by this option.<br>
             * Defaults to true.
             */
            public Builder evolutionSanity(boolean evolutionSanity) {
                this.evolutionSanity = evolutionSanity;
                return this;
            }

            /**
             * If false, split evos (e.g. Poliwrath and Politoed) will be treated as basic Pokemon {@link Species},
             * and will thus use basicAction instead of evolvedAction.
             * Defaults to false.
             */
            public Builder copySplitEvos(boolean copySplitEvos) {
                this.copySplitEvos = copySplitEvos;
                return this;
            }

            /**
             * If true, mega evolutions will be treated as evolutions rather than alt formes.
             * They will use evolvedAction rather than altFormeAction, and "split" mega evolutions
             * (Charizard X/Y and Mewtwo X/Y) will use splitAction if applicable
             * (see {@link #splitAction(EvolvedSpeciesAction)}).<br>
             * Defaults to false.
             */
            public Builder treatMegasAsEvos(boolean treatMegasAsEvos) {
                this.treatMegasAsEvos = treatMegasAsEvos;
                return this;
            }

            /**
             * Sets the action to run on all {@link Species}, when evolutionSanity == false.
             * See {@link #evolutionSanity}.<br>
             * Defaults to equaling the basicAction.
             */
            public Builder noEvoAction(BasicSpeciesAction noEvoAction) {
                this.noEvoAction = noEvoAction;
                return this;
            }

            /**
             * Sets the action to run on all evolved Pokemon {@link Species} that are "split evos"
             * (e.g. Poliwrath and Politoed, Silcoon and Cascoon). See also {@link #copySplitEvos(boolean)}.<br>
             * Defaults to equaling the evolvedAction.
             */
            public Builder splitAction(EvolvedSpeciesAction splitAction) {
                this.splitAction = splitAction;
                return this;
            }

            /**
             * Sets the action to run on non-cosmetic alt formes.
             */
            public Builder altFormeAction(AltFormeAction altFormeAction) {
                this.altFormeAction = altFormeAction;
                return this;
            }

            /**
             * Sets the action to run on essentially cosmetic alt formes ({@link Species} explains what that is).
             */
            public Builder cosmeticAction(AltFormeAction cosmeticAction) {
                this.cosmeticAction = cosmeticAction;
                return this;
            }

            public Options build() {
                if (noEvoAction == null) noEvoAction = basicAction;
                if (splitAction == null) splitAction = evolvedAction;
                return new Options(
                        evolutionSanity, copySplitEvos, treatMegasAsEvos,
                        noEvoAction == null ? nullBasicSpeciesAction : noEvoAction,
                        basicAction == null ? nullBasicSpeciesAction : basicAction,
                        evolvedAction == null ? nullEvolvedSpeciesAction : evolvedAction,
                        splitAction == null ? nullEvolvedSpeciesAction : splitAction,
                        altFormeAction == null ? nullAltFormeAction : altFormeAction,
                        cosmeticAction == null ? nullAltFormeAction : cosmeticAction
                );
            }
        }
    }

    private final Supplier<SpeciesSet> speciesSetSupplier;

    public CopyUpEvolutionsHelper(SpeciesSet speciesSet) {
        this.speciesSetSupplier = () -> speciesSet;
    }

    public CopyUpEvolutionsHelper(Supplier<SpeciesSet> speciesSetSupplier) {
        this.speciesSetSupplier = speciesSetSupplier;
    }

    private record CopyUpRelation(Species from, Species to) {}

    /**
     * Creates/finds the correct CopyUpRelation using the <code>to</code> {@link Species}.<br>
     * Evolution takes priority; Raticate-Alolan should copy up from Rattata-Alolan instead of Raticate-Base.
     * The exception to this rule is essentially cosmetic formes, which always copy from their (conceptual) base forme.
     */
    private CopyUpRelation findRelationInto(Species to) {
        SpeciesSet allSpecs = speciesSetSupplier.get();

        List<Evolution> validEvolutionsTo = to.getEvolutionsTo().stream()
                .filter(evo -> allSpecs.contains(evo.getFrom())).toList();
        if (!validEvolutionsTo.isEmpty() && !to.isEssentiallyCosmetic()) {
            return new CopyUpRelation(to.getEvolutionsTo().getFirst().getFrom(), to);

        } else if (!to.isBaseForme() && allSpecs.contains(to.getConceptualBaseForme())) {
            return new CopyUpRelation(to.getConceptualBaseForme(), to);

        } else {
            throw new IllegalStateException("Argument to is neither an evolved Pokémon nor an alt forme!! " +
                    "(counting only Species in allSpecs)");
        }
    }

    /**
     * Applies the CopyUpEvolutionsHelper, using the supplied Options object.
     */
    public void apply(Options options) {

        SpeciesSet allSpecs = speciesSetSupplier.get();

        SpeciesSet basicSpecs, allEvos, splitEvos, finalEvos;

        if (options.evolutionSanity) {
            basicSpecs = allSpecs.filter(spec -> isBasicSpecies(allSpecs, spec, options))
                    .filter(spec -> isBaseForme(allSpecs, spec));
            allEvos = allSpecs.filter(spec -> !isBasicSpecies(allSpecs, spec, options));
            splitEvos = allSpecs.filter(spec -> isSplitEvo(allSpecs, spec, options));
            finalEvos = allSpecs.filter(spec -> isFinalEvo(allSpecs, spec, options));

        } else {
            basicSpecs = allSpecs.filter(spec -> isBaseForme(allSpecs, spec));
            allEvos = new SpeciesSet();
            splitEvos = new SpeciesSet();
            finalEvos = new SpeciesSet();
        }

        Set<Species> processed = new HashSet<>();

        if (!options.copySplitEvos) {
            basicSpecs.addAll(splitEvos);
        }

        for (Species pk : basicSpecs) {
            if (options.evolutionSanity) {
                options.basicAction.applyTo(pk);
            } else {
                options.noEvoAction.applyTo(pk);
            }
            processed.add(pk);
        }

        // process the rest / all Species that did not get the basic action
        for (Species pk : allSpecs) {
            if (!processed.contains(pk)) {

                // Non-processed specs at this point must have
                // a linear chain down to a processed spec.
                Stack<CopyUpRelation> relStack = new Stack<>();
                CopyUpRelation rel = findRelationInto(pk);
                while (!processed.contains(rel.from)) {
                    relStack.push(rel);
                    rel = findRelationInto(rel.from);
                }
                relStack.push(rel);

                // Now "ev" is set to an evolution from/forme of a Species that has had
                // the basic action done on it to one that hasn't.
                // Do the evolved/forme action for everything left on the stack.
                while (!relStack.isEmpty()) {
                    rel = relStack.pop();
                    if (rel.to.isEssentiallyCosmetic()) {
                        options.cosmeticAction.applyTo(rel.from, rel.to);
                    } else if (options.copySplitEvos && splitEvos.contains(rel.to)) {
                        options.splitAction.applyTo(rel.from, rel.to, finalEvos.contains(rel.to));
                    } else if (allEvos.contains(rel.to)) {
                        options.evolvedAction.applyTo(rel.from, rel.to, finalEvos.contains(rel.to));
                    } else if (!rel.to.isBaseForme()) {
                        // if it gets here it must be an alt forme
                        options.altFormeAction.applyTo(rel.from, rel.to);
                    } else {
                        throw new IllegalStateException(rel + " was not eligible for any copy-up actions...");
                    }
                    processed.add(rel.to);
                }

            }
        }
    }

    // SpeciesSet has inbuilt filter methods for different evolutionary stages.
    // This class uses its own methods, for several reasons:
    //
    // - The SpeciesSet methods assume alt forms evolve from the prevos of their base form;
    //   and this class requires more precise control.
    //
    // - The below methods ignore all Species outside of the given SpeciesSet.
    //   This should make them usable with smaller SpeciesSet / species restrictions,
    //   though at the time of writing this is not actually used.
    //   TODO: integrate with species restrictions (?)
    //
    // - The below methods allow treating Mega Evolutions "as evolutions", which is only relevant here.

    /**
     * Returns true if spec has no other {@link Species} in allSpecs that evolves into it.
     */
    private boolean isBasicSpecies(SpeciesSet allSpecs, Species spec, Options options) {
        for (Evolution evo : spec.getEvolutionsTo()) {
            if (allSpecs.contains(evo.getFrom())) {
                return false;
            }
        }
        if (options.treatMegasAsEvos && spec.isMegaEvolution()) {
            if (allSpecs.contains(spec.getConceptualBaseForme())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if spec evolves from some other {@link Species} in allSpecs,
     * which in turn evolves into at least 2 {@link Species} in allSpecs.
     */
    private boolean isSplitEvo(SpeciesSet allSpecs, Species spec, Options options) {
        // TODO: there was a notion in earlier code, of treating Ninjask only as a non-split evo
        //  (or technically Species which evolved through EvolutionType.LEVEL_CREATE_EXTRA).
        //  Is this something we want to recreate?
        for (Evolution evo : spec.getEvolutionsTo()) {
            if (allSpecs.contains(evo.getFrom())) {
                long evoCount = evo.getFrom().getEvolutionsFrom().stream()
                        .map(Evolution::getTo)
                        .filter(allSpecs::contains)
                        .distinct()
                        .count();
                if (evoCount > 1) {
                    return true;
                }
            }
        }
        if (options.treatMegasAsEvos && spec.isMegaEvolution()) {
            if (allSpecs.contains(spec.getConceptualBaseForme())) {
                int megaCount = spec.getConceptualBaseForme()
                        .getAltFormes()
                        .filter(Species::isMegaEvolution)
                        .size();
                if (megaCount > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if spec is not a {@link #isBasicSpecies(SpeciesSet, Species, Options) basic species},
     * and also spec does not evolve into any other {@link Species} in allSpecs.
     */
    private boolean isFinalEvo(SpeciesSet allSpecs, Species spec, Options options) {
        if (isBasicSpecies(allSpecs, spec, options)) {
            return false;
        }
        for (Evolution evo : spec.getEvolutionsFrom()) {
            if (allSpecs.contains(evo.getTo())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if spec is a base forme, or its conceptual base forme is not in allSpecs.
     */
    private boolean isBaseForme(SpeciesSet allSpecs, Species spec) {
        return spec.isBaseForme() || !allSpecs.contains(spec.getConceptualBaseForme());
    }

}
