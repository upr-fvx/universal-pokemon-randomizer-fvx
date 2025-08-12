package com.dabomstew.pkromio.gamedata;

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

import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Trainer as seen in battle; contains a list of their {@link TrainerPokemon},
 * their name, trainerclass, and other details.<br>
 * Most Trainer objects represent opponents, but some represent your ally in tag battles.<br>
 * Multiple Trainer objects may be used to represent a single in-game character
 * (such as the Rival, or Youngster Joey), in case they have multiple battles to their name.
 * <br><br>
 * The Trainer class uses a tagging system. The following tags are used:
 * <ul>
 *     <li><b><code>GYM[n]</code></b><br>
 *     Used by gym trainers. <code>[n]</code> is an integer denoting which
 *     specific gym this trainer belongs to.<br>
 *     E.g. "<code>GYM1</code>" is used by Bird Keeper Abe in Crystal.
 *     </li>
 *     <li><b><code>THEMED:[s]</code></b><br>
 *     Used by non-gym trainers who should share a type theme. <code>[s]</code> is
 *     an arbitrary string to group these trainers.<br>
 *     E.g. "<code>THEMED:SPROUTTOWER</code>" is used by Sage Neal in Gold.
 *     </li>
 *     <li><b><code>STRONG</code></b><br>
 *     Used by evil team admins and other important "mini-boss" trainers.
 *     Should typically be used with the <code>THEMED:[s]</code> tag.<br>
 *     E.g. "<code>THEMED:WALLY-STRONG</code>" is used by Wally in Alpha Sapphire.
 *     </li>
 *     <li><b><code>LEADER</code></b><br>
 *     Used by gym leaders and evil team leaders, following the
 *     <code>GYM[n]</code> and <code>THEMED:[s]</code> tags, respectively.<br>
 *     E.g. "<code>GYM2-LEADER</code>" is used by Brawly in Emerald,
 *     "<code>THEMED:LYSANDRE-LEADER</code>" is used by Lysandre in X.
 *     </li>
 *     <li><b><code>ELITE[n]</code></b><br>
 *     Used by Elite 4 members. <code>[n]</code> is an integer denoting which
 *     member it is.<br>
 *     E.g. "<code>ELITE3</code>" is used by Agatha in Yellow.
 *     </li>
 *     <li><b><code>CHAMPION</code></b><br>
 *     Used by the Champion.<br>
 *     E.g. "<code>CHAMPION</code>" is used by Iris in Black 2.
 *     </li>
 *     <li><b><code>UBER</code></b><br>
 *     Used by extra-strong trainers, the post-game super-bosses.<br>
 *     E.g. "<code>CHAMPION</code>" is used by Red in HeartGold.
 *     </li>
 *     <li><b><code>NOTSTRONG</code></b><br>
 *     Used to denote this trainer should not get any buffs (e.g. added Pokémon, held items).
 *     E.g. "<code>NOTSTRONG</code>" is used by Successor Korrina in Y.
 *     </li>
 *     <li><b><code>RIVAL[x]-[y]</code></b><br>
 *     Used by trainers that carry the starter <i>strong</i> against the player's chosen starter.
 *     <code>[x]</code> and <code>[y]</code> are both integers.<br>
 *     <code>[x]</code> denotes which battle this is. <code>[x]</code>==1 is for
 *     the first battle, where the trainer only has 1 Pokémon: the corresponding starter.
 *     <code>[x]</code>==1 also denotes that the trainer should not get any buffs
 *     (like <code>NOTSTRONG</code>).<br>
 *     <code>[y]</code> denotes the "variant": which starter the player chose.
 *     This corresponds to the indices in {@link RomHandler#getStarters()}.<br>
 *     E.g. "<code>CHAMPION</code>" is used by Red in HeartGold.
 *     </li>
 *     <li><b><code>FRIEND[x]-[y]</code></b><br>
 *     Like <code>RIVAL[x]-[y]</code> for trainers that carry the starter
 *     <i>weak</i> against the player's chosen starter.
 *     </li>
 * </ul>
 */
public class Trainer implements Comparable<Trainer> {
    // TODO: the tagging of Trainers is inconsistent between games. Some sub-issues:
    //  - Gen 1+FRLG uses the GIO tag for earlier Giovanni battles, seemingly identical to GYM8 in function.
    //  - Gen 1+2 never uses the -STRONG tag.
    //  - HGSS tags the (presumably) kimono girls as KIMONO[n], but this is never used.
    //  - BW1 uses UBER for the final N + Ghetsis battles.
    //  - XY does not use THEMED:[s]-STRONG for team admins and non-RIVAL/FRIEND-applicable rivals (Tierno & Trevor).
    //  - Gen 7 uses ELITE[n] for the kahunas.

    public int offset;
    public int index;
    public List<TrainerPokemon> pokemon = new ArrayList<>();
    public String tag;
    // This value has some flags about the trainer's pokemon (e.g. if they have items or custom moves)
    public int poketype;
    public String name; // TODO: make trainer name randomization use Trainer.name in all gens, really strange it doesn't
    public int trainerclass;
    public String fullDisplayName;
    public MultiBattleStatus multiBattleStatus = MultiBattleStatus.NEVER;
    public boolean forcedDoubleBattle; // for doubleBattleMode
    public int forceStarterPosition = -1;
    // Certain trainers (e.g., trainers in the PWT in BW2) require unique held items for all of their Pokemon to prevent a game crash.
    public boolean requiresUniqueHeldItems;
    public BattleStyle currBattleStyle = new BattleStyle(); // Defaults to "Unchanged", but need this per-trainer for the Random style option.

    public Trainer() { }

    public Trainer(Trainer original) {
        this.offset = original.offset;
        this.index = original.index;
        this.pokemon = new ArrayList<>();
        for(TrainerPokemon originalTP : original.pokemon) {
            TrainerPokemon copiedTP = new TrainerPokemon(originalTP);
            this.pokemon.add(copiedTP);
        }
        this.tag = original.tag;
        this.poketype = original.poketype;
        this.name = original.name;
        this.trainerclass = original.trainerclass;
        this.fullDisplayName = original.fullDisplayName;
        this.multiBattleStatus = original.multiBattleStatus;
        this.forcedDoubleBattle = original.forcedDoubleBattle;
        this.forceStarterPosition = original.forceStarterPosition;
        this.requiresUniqueHeldItems = original.requiresUniqueHeldItems;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (fullDisplayName != null) {
            sb.append(fullDisplayName).append(" ");
        } else if (name != null) {
            sb.append(name).append(" ");
        }
        if (trainerclass != 0) {
            sb.append("(").append(trainerclass).append(") - ");
        }
        if (currBattleStyle.isBattleStyleChanged()) {
            sb.append("(").append(currBattleStyle.getStyle().toString()).append(") - ");
        }
        if (offset > 0) {
            sb.append(String.format("%x", offset));
        }
        sb.append(" => ");
        boolean first = true;
        for (TrainerPokemon p : pokemon) {
            if (!first) {
                sb.append(',');
            }
            sb.append(p.getSpecies().getName()).append(" Lv").append(p.getLevel());
            first = false;
        }
        sb.append(']');
        if (tag != null) {
            sb.append(" (").append(tag).append(")");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Trainer other = (Trainer) obj;
        return index == other.index;
    }

    @Override
    public int compareTo(Trainer o) {
        return index - o.index;
    }

    public boolean isBoss() {
        return tag != null && (tag.startsWith("ELITE") || tag.startsWith("CHAMPION")
                || tag.startsWith("UBER") || tag.endsWith("LEADER"));
    }

    public boolean isImportant() {
        return tag != null && (tag.startsWith("RIVAL") || tag.startsWith("FRIEND") || tag.endsWith("STRONG"));
    }

    public boolean shouldNotGetBuffs() {
        return tag != null && (tag.startsWith("RIVAL1-") || tag.startsWith("FRIEND1-") || tag.endsWith("NOTSTRONG"));
    }

    public void setPokemonHaveItems(boolean haveItems) {
        if (haveItems) {
            this.poketype |= 2;
        } else {
            // https://stackoverflow.com/a/1073328
            this.poketype = poketype & ~2;
        }
    }

    public boolean pokemonHaveItems() {
        // This flag seems consistent for all gens
        return (this.poketype & 2) == 2;
    }

    public void setPokemonHaveCustomMoves(boolean haveCustomMoves) {
        if (haveCustomMoves) {
            this.poketype |= 1;
        } else {
            this.poketype = poketype & ~1;
        }
    }

    public boolean pokemonHaveCustomMoves() {
        // This flag seems consistent for all gens
        return (this.poketype & 1) == 1;
    }

    public boolean pokemonHaveUniqueHeldItems() {
        List<Item> heldItemsForThisTrainer = new ArrayList<>();
        for (TrainerPokemon poke : this.pokemon) {
            if (poke.getHeldItem() != null) {
                if (heldItemsForThisTrainer.contains(poke.getHeldItem())) {
                    return false;
                } else {
                    heldItemsForThisTrainer.add(poke.getHeldItem());
                }
            }
        }
        return true;
    }

    public enum MultiBattleStatus {
        NEVER, POTENTIAL, ALWAYS
    }
}
