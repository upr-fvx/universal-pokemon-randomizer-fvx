package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Species;

@FunctionalInterface
public interface AltFormeAction {
    void applyTo(Species baseForme, Species altForme);
}
