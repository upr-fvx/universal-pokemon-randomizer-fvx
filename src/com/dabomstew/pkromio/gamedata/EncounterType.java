package com.dabomstew.pkromio.gamedata;

public enum EncounterType {
    WALKING, //e.g. grass, cave, seaweed(unfortunately), Horde encounters, shaking grass
    SURFING, FISHING, //obvious
    INTERACT, //e.g. headbutt trees, Rock Smash
    AMBUSH, //e.g. flying pokemon, shaking trees, shaking grass, rippling water
    SPECIAL, //e.g. Poke Radar, DexNav Foreign encounter, Hoenn/Sinnoh Sound, Feebas tiles, swarms
    //(The most poorly defined, but generally "one of the regular encounter types, but with an extra condition that
    //must be fulfilled")
    UNUSED //obvious
} //SOS encounters are included in the same area as their non-SOS origin, so aren't included as a type
