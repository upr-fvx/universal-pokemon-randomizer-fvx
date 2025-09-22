package com.dabomstew.pkromio.services;

import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.List;
import java.util.Random;

/**
 * A service for {@link Type}-related stuff.
 */
public class TypeService {

    private final RomHandler romHandler;

    public TypeService(RomHandler romHandler) {
        this.romHandler = romHandler;
    }

    public List<Type> getTypes() {
        return romHandler.getTypeTable().getTypes();
    }

    public boolean typeInGame(Type type) {
        return romHandler.getTypeTable().getTypes().contains(type);
    }

    /**
     * returns a random {@link Type} which is valid in this game
     */
    public Type randomType(Random random) {
        List<Type> allTypes = romHandler.getTypeTable().getTypes();
        return allTypes.get(random.nextInt(allTypes.size()));
    }
}
