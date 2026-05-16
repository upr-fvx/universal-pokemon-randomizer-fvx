package com.uprfvx.romio.gamedata;

public enum CfruDpeRandomPoolAssetIssue {
    NULL_SPECIES("null species"),
    NO_USABLE_LEARNSET("no usable learnset"),
    INVALID_FRONT_BATTLE_SPRITE_POINTER("invalid/missing front battle sprite pointer"),
    INVALID_NORMAL_PALETTE_POINTER("invalid/missing normal palette pointer");

    private final String label;

    CfruDpeRandomPoolAssetIssue(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
