package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.gamedata.PlayerCharacterType;

/**
 * Represents a Custom Player Graphics, to be inserted into a game.
 * Contains the {@link GraphicsPack} itself, as well as a {@link PlayerCharacterType}
 * holding the notion of which player character to replace.
 */
public class CustomPlayerGraphics {

    private final GraphicsPack graphicsPack;
    private final PlayerCharacterType typeToReplace;

    public CustomPlayerGraphics(GraphicsPack graphicsPack, PlayerCharacterType typeToReplace) {
        this.graphicsPack = graphicsPack;
        this.typeToReplace = typeToReplace;
    }

    public GraphicsPack getGraphicsPack() {
        return graphicsPack;
    }

    public PlayerCharacterType getTypeToReplace() {
        return typeToReplace;
    }
}
