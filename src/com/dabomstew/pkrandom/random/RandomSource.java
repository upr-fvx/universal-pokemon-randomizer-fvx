package com.dabomstew.pkrandom.random;

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

import java.util.Random;

/**
 * Holds a non-cosmetic and a cosmetic {@link Random}, and keeps track of their usage.
 * This allows both Randoms to use a single seed without cosmetic RNG calls affecting non-cosmetic ones.
 * Seeing how many times RNG has been called is interesting when logging.
 */
public class RandomSource {

    private final RandomWithCounter nonCosmetic = new RandomWithCounter();
    private final RandomWithCounter cosmetic = new RandomWithCounter();
    private long seed;

    public void seed(long seed) {
        nonCosmetic.setSeed(seed);
        cosmetic.setSeed(seed);
        this.seed = seed;
    }

    public Random getNonCosmetic() {
        return nonCosmetic;
    }

    public Random getCosmetic() {
        return cosmetic;
    }

    public long getSeed() {
        return seed;
    }

    public int callsSinceSeed() {
        return nonCosmetic.calls + cosmetic.calls;
    }

    public int callsSinceSeedNonCosmetic() {
        return nonCosmetic.calls;
    }

    public int callsSinceSeedCosmetic() {
        return cosmetic.calls;
    }

    private static class RandomWithCounter extends Random {

        private int calls = 0;

        @Override
        public synchronized void setSeed(long seed) {
            super.setSeed(seed);
            calls = 0;
        }

        @Override
        public void nextBytes(byte[] bytes) {
            calls++;
            super.nextBytes(bytes);
        }

        @Override
        public int nextInt() {
            calls++;
            return super.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            calls++;
            return super.nextInt(bound);
        }

        @Override
        public long nextLong() {
            calls++;
            return super.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            calls++;
            return super.nextBoolean();
        }

        @Override
        public float nextFloat() {
            calls++;
            return super.nextFloat();
        }

        @Override
        public double nextDouble() {
            calls++;
            return super.nextDouble();
        }

        @Override
        public synchronized double nextGaussian() {
            calls++;
            return super.nextGaussian();
        }

    }

}
