package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  Gen1Species.java - represents an individual Gen 1 Species. Used to    --*/
/*--                 handle things related to stats because of the lack     --*/
/*--                 of the Special split in Gen 1.                         --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
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

import com.dabomstew.pkromio.graphics.palettes.SGBPaletteID;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Gen1Species extends Species {

    public Gen1Species(int number) {
        super(number);
        shuffledStatsOrder = Arrays.asList(0, 1, 2, 3, 4);
        setGeneration(1);
    }

    private int frontImagePointer;
    private int backImagePointer;
	
	private SGBPaletteID paletteID;

	@Override
	public void copyShuffledStatsUpEvolution(Species evolvesFrom) {
		// If stats were already shuffled once, un-shuffle them
		shuffledStatsOrder = Arrays.asList(shuffledStatsOrder.indexOf(0), shuffledStatsOrder.indexOf(1),
				shuffledStatsOrder.indexOf(2), shuffledStatsOrder.indexOf(3), shuffledStatsOrder.indexOf(4));
		applyShuffledOrderToStats();
		shuffledStatsOrder = evolvesFrom.shuffledStatsOrder;
		applyShuffledOrderToStats();
	}

    @Override
    protected void applyShuffledOrderToStats() {
        List<Integer> stats = Arrays.asList(getHp(), getAttack(), getDefense(), getSpecial(), getSpeed());

        // Copy in new stats
        setHp(stats.get(shuffledStatsOrder.get(0)));
        setAttack(stats.get(shuffledStatsOrder.get(1)));
        setDefense(stats.get(shuffledStatsOrder.get(2)));
        setSpecial(stats.get(shuffledStatsOrder.get(3)));
        setSpeed(stats.get(shuffledStatsOrder.get(4)));
    }

	@Override
	public void randomizeStatsWithinBST(Random random) {
		// Minimum 20 HP, 10 everything else
		int bst = getBST() - 60;

		// Make weightings
		double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
		double specW = random.nextDouble(), speW = random.nextDouble();

		double totW = hpW + atkW + defW + specW + speW;

        setHp((int) Math.max(1, Math.round(hpW / totW * bst)) + 20);
        setAttack((int) Math.max(1, Math.round(atkW / totW * bst)) + 10);
        setDefense((int) Math.max(1, Math.round(defW / totW * bst)) + 10);
        setSpecial((int) Math.max(1, Math.round(specW / totW * bst)) + 10);
        setSpeed((int) Math.max(1, Math.round(speW / totW * bst)) + 10);

        // Check for something we can't store
        if (getHp() > 255 || getAttack() > 255 || getDefense() > 255 || getSpecial() > 255 || getSpeed() > 255) {
            // re roll
            randomizeStatsWithinBST(random);
        }
    }

	@Override
	public void copyRandomizedStatsUpEvolution(Species evolvesFrom) {
		double ourBST = getBST();
		double theirBST = evolvesFrom.getBST();

		double bstRatio = ourBST / theirBST;

        setHp((int) Math.min(255, Math.max(1, Math.round(evolvesFrom.getHp() * bstRatio))));
        setAttack((int) Math.min(255, Math.max(1, Math.round(evolvesFrom.getAttack() * bstRatio))));
        setDefense((int) Math.min(255, Math.max(1, Math.round(evolvesFrom.getDefense() * bstRatio))));
        setSpeed((int) Math.min(255, Math.max(1, Math.round(evolvesFrom.getSpeed() * bstRatio))));
        setSpecial((int) Math.min(255, Math.max(1, Math.round(evolvesFrom.getSpecial() * bstRatio))));
    }

	@Override
	public void assignNewStatsForEvolution(Species evolvesFrom, Random random) {
		double ourBST = getBST();
		double theirBST = evolvesFrom.getBST();

		double bstDiff = ourBST - theirBST;

		// Make weightings
		double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
		double specW = random.nextDouble(), speW = random.nextDouble();

		double totW = hpW + atkW + defW + specW + speW;

		double hpDiff = Math.round((hpW / totW) * bstDiff);
		double atkDiff = Math.round((atkW / totW) * bstDiff);
		double defDiff = Math.round((defW / totW) * bstDiff);
		double specDiff = Math.round((specW / totW) * bstDiff);
		double speDiff = Math.round((speW / totW) * bstDiff);

        setHp((int) Math.min(255, Math.max(1, evolvesFrom.getHp() + hpDiff)));
        setAttack((int) Math.min(255, Math.max(1, evolvesFrom.getAttack() + atkDiff)));
        setDefense((int) Math.min(255, Math.max(1, evolvesFrom.getDefense() + defDiff)));
        setSpeed((int) Math.min(255, Math.max(1, evolvesFrom.getSpeed() + speDiff)));
        setSpecial((int) Math.min(255, Math.max(1, evolvesFrom.getSpecial() + specDiff)));
    }

    @Override
    protected int getBST() {
        return getHp() + getAttack() + getDefense() + getSpecial() + getSpeed();
    }

    @Override
    public int getBSTForPowerLevels() {
        return getHp() + getAttack() + getDefense() + getSpecial() + getSpeed();
    }

    @Override
    public double getAttackSpecialAttackRatio() {
        return (double) getAttack() / ((double) getAttack() + (double) getSpecial());
    }

    @Override
    public String toString() {
        return "Species [name=" + getName() + ", number=" + getNumber() + ", primaryType=" + getPrimaryType(false) + ", secondaryType="
                + getSecondaryType(false) + ", hp=" + getHp() + ", attack=" + getAttack() + ", defense=" + getDefense() + ", special=" + getSpecial()
                + ", speed=" + getSpeed() + "]";
    }

    public int getFrontImagePointer() {
        return frontImagePointer;
    }

    public void setFrontImagePointer(int frontImagePointer) {
        this.frontImagePointer = frontImagePointer;
    }

    public int getBackImagePointer() {
        return backImagePointer;
    }

    public void setBackImagePointer(int backImagePointer) {
        this.backImagePointer = backImagePointer;
    }

    public SGBPaletteID getPaletteID() {
        return paletteID;
    }

    public void setPaletteID(SGBPaletteID paletteID) {
        this.paletteID = paletteID;
    }
}
