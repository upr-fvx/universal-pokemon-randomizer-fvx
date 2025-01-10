package com.dabomstew.pkrandom.constants;

/*----------------------------------------------------------------------------*/
/*--  Gen4Constants.java - Constants for DPPt and HGSS                      --*/
/*--                                                                        --*/
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.dabomstew.pkrandom.gamedata.*;

public class Gen4Constants {

    public static final int Type_DP = 0;
    public static final int Type_Plat = 1;
    public static final int Type_HGSS = 2;

    public static final int arm9Offset = 0x02000000;

    public static final int pokemonCount = 493, moveCount = 467;
    private static final int dpFormeCount = 5, platHgSsFormeCount = 12;
    public static final int formeOffset = 2;

    public static final int bsHPOffset = 0, bsAttackOffset = 1, bsDefenseOffset = 2, bsSpeedOffset = 3,
            bsSpAtkOffset = 4, bsSpDefOffset = 5, bsPrimaryTypeOffset = 6, bsSecondaryTypeOffset = 7,
            bsCatchRateOffset = 8, bsCommonHeldItemOffset = 12, bsRareHeldItemOffset = 14, bsGenderRatioOffset = 16,
            bsGrowthCurveOffset = 19, bsAbility1Offset = 22, bsAbility2Offset = 23, bsTMHMCompatOffset = 28;

    public static final String starterCriesPrefix = "0004000C10BD0000000000000000000000E000000000000000E0000000000200";

    public static final byte[] hgssStarterCodeSuffix = { 0x03, 0x03, 0x1A, 0x12, 0x1, 0x23, 0x0, 0x0 };

    public static final int[] hgssFilesWithRivalScript = { 7, 23, 96, 110, 819, 850, 866 };

    public static final byte[] hgssRivalScriptMagic = { (byte) 0xCE, 0x00, 0x0C, (byte) 0x80, 0x11, 0x00, 0x0C,
            (byte) 0x80, (byte) 152, 0, 0x1C, 0x00, 0x05 };

    public static final int[] ptFilesWithRivalScript = { 31, 36, 112, 123, 186, 427, 429, 1096 };

    public static final int[] dpFilesWithRivalScript = { 34, 90, 118, 180, 195, 394 };

    public static final byte[] dpptRivalScriptMagic = { (byte) 0xDE, 0x00, 0x0C, (byte) 0x80, 0x11, 0x00, 0x0C,
            (byte) 0x80, (byte) 0x83, 0x01, 0x1C, 0x00, 0x01 };

    public static final byte[] dpptTagBattleScriptMagic1 = { (byte) 0xDE, 0x00, 0x0C, (byte) 0x80, 0x28, 0x00, 0x04,
            (byte) 0x80 };

    public static final byte[] dpptTagBattleScriptMagic2 = { 0x11, 0x00, 0x0C, (byte) 0x80, (byte) 0x86, 0x01, 0x1C,
            0x00, 0x01 };

    public static final int[] ptFilesWithTagScript = { 2, 136, 201, 236 };

    public static final int[] dpFilesWithTagScript = { 2, 131, 230 };

    public static final int dpStarterStringIndex = 19, ptStarterStringIndex = 36;

    public static final int fossilCount = 7;

    public static final String dpptTMDataPrefix = "D100D200D300D400", hgssTMDataPrefix = "1E003200";

    public static final int tmCount = 92, hmCount = 8;

    public static final int tmItemOffset = ItemIDs.tm01;

    public static final byte tmsReusableByteBefore = (byte) 0xD1, tmsReusableByteAfter = (byte) 0xE0;

    private static final int dpptTextCharsPerLine = 38, hgssTextCharsPerLine = 36;

    public static final String dpItemPalettesPrefix = "8D018E01210132018D018F0122013301",
            pthgssItemPalettesPrefix = "8D018E01210133018D018F0122013401";

    public static final int ptSpearPillarPortalScriptFile = 237;

    public static final int evolutionMethodCount = 26;

    public static final int highestAbilityIndex = AbilityIDs.badDreams;

    public static final int mysteryEggCommandLength = 216; // the improved version, technically

    public static final int mysteryEggImprovementIdentifierOffset = 30;

    public static final byte mysteryEggImprovementIdentifier = (byte) 0x57;

    public static final List<Integer> consumableHeldItems = Arrays.asList(
            ItemIDs.cheriBerry, ItemIDs.chestoBerry, ItemIDs.pechaBerry, ItemIDs.rawstBerry, ItemIDs.aspearBerry,
            ItemIDs.leppaBerry, ItemIDs.oranBerry, ItemIDs.persimBerry, ItemIDs.lumBerry, ItemIDs.sitrusBerry, ItemIDs.figyBerry,
            ItemIDs.wikiBerry, ItemIDs.magoBerry, ItemIDs.aguavBerry, ItemIDs.iapapaBerry, ItemIDs.occaBerry, ItemIDs.passhoBerry,
            ItemIDs.wacanBerry, ItemIDs.rindoBerry, ItemIDs.yacheBerry, ItemIDs.chopleBerry, ItemIDs.kebiaBerry, ItemIDs.shucaBerry,
            ItemIDs.cobaBerry, ItemIDs.payapaBerry, ItemIDs.tangaBerry, ItemIDs.chartiBerry, ItemIDs.kasibBerry, ItemIDs.habanBerry,
            ItemIDs.colburBerry, ItemIDs.babiriBerry, ItemIDs.chilanBerry, ItemIDs.liechiBerry, ItemIDs.ganlonBerry,
            ItemIDs.salacBerry, ItemIDs.petayaBerry, ItemIDs.apicotBerry, ItemIDs.lansatBerry, ItemIDs.starfBerry,
            ItemIDs.enigmaBerry, ItemIDs.micleBerry, ItemIDs.custapBerry, ItemIDs.jabocaBerry, ItemIDs.rowapBerry,
            ItemIDs.berryJuice, ItemIDs.whiteHerb, ItemIDs.mentalHerb, ItemIDs.powerHerb, ItemIDs.focusSash);

    public static final List<Integer> allHeldItems = setupAllHeldItems();

    private static List<Integer> setupAllHeldItems() {
        List<Integer> list = new ArrayList<>();
        list.addAll(Arrays.asList(ItemIDs.brightPowder, ItemIDs.quickClaw, ItemIDs.choiceBand, ItemIDs.kingsRock,
                ItemIDs.silverPowder, ItemIDs.focusBand, ItemIDs.scopeLens, ItemIDs.metalCoat, ItemIDs.leftovers, ItemIDs.softSand,
                ItemIDs.hardStone, ItemIDs.miracleSeed, ItemIDs.blackGlasses, ItemIDs.blackBelt, ItemIDs.magnet,
                ItemIDs.mysticWater, ItemIDs.sharpBeak, ItemIDs.poisonBarb, ItemIDs.neverMeltIce, ItemIDs.spellTag,
                ItemIDs.twistedSpoon, ItemIDs.charcoal, ItemIDs.dragonFang, ItemIDs.silkScarf, ItemIDs.shellBell,
                ItemIDs.seaIncense, ItemIDs.laxIncense, ItemIDs.wideLens, ItemIDs.muscleBand, ItemIDs.wiseGlasses,
                ItemIDs.expertBelt, ItemIDs.lightClay, ItemIDs.lifeOrb, ItemIDs.toxicOrb, ItemIDs.flameOrb, ItemIDs.zoomLens,
                ItemIDs.metronome, ItemIDs.ironBall, ItemIDs.laggingTail, ItemIDs.destinyKnot, ItemIDs.blackSludge, ItemIDs.icyRock,
                ItemIDs.smoothRock, ItemIDs.heatRock, ItemIDs.dampRock, ItemIDs.gripClaw, ItemIDs.choiceScarf, ItemIDs.stickyBarb,
                ItemIDs.shedShell, ItemIDs.bigRoot, ItemIDs.choiceSpecs, ItemIDs.flamePlate, ItemIDs.splashPlate, ItemIDs.zapPlate,
                ItemIDs.meadowPlate, ItemIDs.iciclePlate, ItemIDs.fistPlate, ItemIDs.toxicPlate, ItemIDs.earthPlate,
                ItemIDs.skyPlate, ItemIDs.mindPlate, ItemIDs.insectPlate, ItemIDs.stonePlate, ItemIDs.spookyPlate,
                ItemIDs.dracoPlate, ItemIDs.dreadPlate, ItemIDs.ironPlate, ItemIDs.oddIncense, ItemIDs.rockIncense,
                ItemIDs.fullIncense, ItemIDs.waveIncense, ItemIDs.roseIncense, ItemIDs.razorClaw, ItemIDs.razorFang));
        list.addAll(consumableHeldItems);
        return list;
    }

    public static final List<Integer> generalPurposeConsumableItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.cheriBerry, ItemIDs.chestoBerry, ItemIDs.pechaBerry, ItemIDs.rawstBerry, ItemIDs.aspearBerry, ItemIDs.leppaBerry,
            ItemIDs.oranBerry, ItemIDs.persimBerry, ItemIDs.lumBerry, ItemIDs.sitrusBerry, ItemIDs.ganlonBerry, ItemIDs.salacBerry,
            // An NPC pokemon's nature is generated randomly with IVs during gameplay. Therefore, we do not include
            // the flavor berries because, prior to Gen 7, they aren't worth the risk.
            ItemIDs.apicotBerry, ItemIDs.lansatBerry, ItemIDs.starfBerry, ItemIDs.enigmaBerry, ItemIDs.micleBerry, ItemIDs.custapBerry,
            ItemIDs.jabocaBerry, ItemIDs.rowapBerry, ItemIDs.berryJuice, ItemIDs.whiteHerb, ItemIDs.mentalHerb, ItemIDs.focusSash));

    public static final List<Integer> generalPurposeItems = Collections.unmodifiableList(Arrays.asList(
            ItemIDs.brightPowder, ItemIDs.quickClaw, ItemIDs.kingsRock, ItemIDs.focusBand, ItemIDs.scopeLens, ItemIDs.leftovers,
            ItemIDs.shellBell, ItemIDs.laxIncense, ItemIDs.wideLens, ItemIDs.expertBelt, ItemIDs.lifeOrb, ItemIDs.zoomLens,
            ItemIDs.destinyKnot, ItemIDs.shedShell, ItemIDs.razorClaw, ItemIDs.razorFang));

    public static final Map<Type, List<Integer>> typeBoostingItems = initializeTypeBoostingItems();

    private static Map<Type, List<Integer>> initializeTypeBoostingItems() {
        Map<Type, List<Integer>> map = new HashMap<>();
        map.put(Type.BUG, Arrays.asList(ItemIDs.silverPowder, ItemIDs.insectPlate));
        map.put(Type.DARK, Arrays.asList(ItemIDs.blackGlasses, ItemIDs.dreadPlate));
        map.put(Type.DRAGON, Arrays.asList(ItemIDs.dragonFang, ItemIDs.dracoPlate));
        map.put(Type.ELECTRIC, Arrays.asList(ItemIDs.magnet, ItemIDs.zapPlate));
        map.put(Type.FIGHTING, Arrays.asList(ItemIDs.blackBelt, ItemIDs.fistPlate));
        map.put(Type.FIRE, Arrays.asList(ItemIDs.charcoal, ItemIDs.flamePlate));
        map.put(Type.FLYING, Arrays.asList(ItemIDs.sharpBeak, ItemIDs.skyPlate));
        map.put(Type.GHOST, Arrays.asList(ItemIDs.spellTag, ItemIDs.spookyPlate));
        map.put(Type.GRASS, Arrays.asList(ItemIDs.miracleSeed, ItemIDs.meadowPlate, ItemIDs.roseIncense));
        map.put(Type.GROUND, Arrays.asList(ItemIDs.softSand, ItemIDs.earthPlate));
        map.put(Type.ICE, Arrays.asList(ItemIDs.neverMeltIce, ItemIDs.iciclePlate));
        map.put(Type.NORMAL, Collections.singletonList(ItemIDs.silkScarf));
        map.put(Type.POISON, Arrays.asList(ItemIDs.poisonBarb, ItemIDs.toxicPlate));
        map.put(Type.PSYCHIC, Arrays.asList(ItemIDs.twistedSpoon, ItemIDs.mindPlate, ItemIDs.oddIncense));
        map.put(Type.ROCK, Arrays.asList(ItemIDs.hardStone, ItemIDs.stonePlate, ItemIDs.rockIncense));
        map.put(Type.STEEL, Arrays.asList(ItemIDs.metalCoat, ItemIDs.ironPlate));
        map.put(Type.WATER, Arrays.asList(ItemIDs.mysticWater, ItemIDs.seaIncense, ItemIDs.splashPlate, ItemIDs.waveIncense));
        map.put(null, Collections.emptyList()); // ??? type
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> moveBoostingItems = initializeMoveBoostingItems();

    private static Map<Integer, List<Integer>> initializeMoveBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(MoveIDs.bounce, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.dig, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.dive, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.fly, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.razorWind, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.skullBash, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.skyAttack, Collections.singletonList(ItemIDs.powerHerb));
        map.put(MoveIDs.solarBeam, Collections.singletonList(ItemIDs.powerHerb));

        map.put(MoveIDs.fling, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb, ItemIDs.ironBall));

        map.put(MoveIDs.trick, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb, ItemIDs.fullIncense, ItemIDs.laggingTail));
        map.put(MoveIDs.switcheroo, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb, ItemIDs.fullIncense, ItemIDs.laggingTail));

        map.put(MoveIDs.trickRoom, Collections.singletonList(ItemIDs.ironBall));

        map.put(MoveIDs.facade, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb));

        map.put(MoveIDs.psychoShift, Arrays.asList(ItemIDs.toxicOrb, ItemIDs.flameOrb));

        map.put(MoveIDs.lightScreen, Collections.singletonList(ItemIDs.lightClay));
        map.put(MoveIDs.reflect, Collections.singletonList(ItemIDs.lightClay));

        map.put(MoveIDs.hail, Collections.singletonList(ItemIDs.icyRock));

        map.put(MoveIDs.sandstorm, Collections.singletonList(ItemIDs.smoothRock));

        map.put(MoveIDs.sunnyDay, Collections.singletonList(ItemIDs.heatRock));

        map.put(MoveIDs.rainDance, Collections.singletonList(ItemIDs.dampRock));

        map.put(MoveIDs.bind, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.clamp, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.fireSpin, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.magmaStorm, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.outrage, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.sandTomb, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.uproar, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.whirlpool, Collections.singletonList(ItemIDs.gripClaw));
        map.put(MoveIDs.wrap, Collections.singletonList(ItemIDs.gripClaw));

        map.put(MoveIDs.absorb, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.aquaRing, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.drainPunch, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.dreamEater, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.gigaDrain, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.ingrain, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.leechLife, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.leechSeed, Collections.singletonList(ItemIDs.bigRoot));
        map.put(MoveIDs.megaDrain, Collections.singletonList(ItemIDs.bigRoot));

        return Collections.unmodifiableMap(map);
    }

    public static final Map<Type, Integer> weaknessReducingBerries = initializeWeaknessReducingBerries();

    private static Map<Type, Integer> initializeWeaknessReducingBerries() {
        Map<Type, Integer> map = new HashMap<>();
        map.put(Type.FIRE, ItemIDs.occaBerry);
        map.put(Type.WATER, ItemIDs.passhoBerry);
        map.put(Type.ELECTRIC, ItemIDs.wacanBerry);
        map.put(Type.GRASS, ItemIDs.rindoBerry);
        map.put(Type.ICE, ItemIDs.yacheBerry);
        map.put(Type.FIGHTING, ItemIDs.chopleBerry);
        map.put(Type.POISON, ItemIDs.kebiaBerry);
        map.put(Type.GROUND, ItemIDs.shucaBerry);
        map.put(Type.FLYING, ItemIDs.cobaBerry);
        map.put(Type.PSYCHIC, ItemIDs.payapaBerry);
        map.put(Type.BUG, ItemIDs.tangaBerry);
        map.put(Type.ROCK, ItemIDs.chartiBerry);
        map.put(Type.GHOST, ItemIDs.kasibBerry);
        map.put(Type.DRAGON, ItemIDs.habanBerry);
        map.put(Type.DARK, ItemIDs.colburBerry);
        map.put(Type.STEEL, ItemIDs.babiriBerry);
        map.put(Type.NORMAL, ItemIDs.chilanBerry); //With randomized type effectiveness, this can come up!
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> speciesBoostingItems = initializeSpeciesBoostingItems();

    private static Map<Integer, List<Integer>> initializeSpeciesBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(SpeciesIDs.dialga, Collections.singletonList(ItemIDs.adamantOrb));
        map.put(SpeciesIDs.palkia, Collections.singletonList(ItemIDs.lustrousOrb));
        map.put(SpeciesIDs.latias, Collections.singletonList(ItemIDs.soulDew));
        map.put(SpeciesIDs.latios, Collections.singletonList(ItemIDs.soulDew));
        map.put(SpeciesIDs.clamperl, Arrays.asList(ItemIDs.deepSeaTooth, ItemIDs.deepSeaScale));
        map.put(SpeciesIDs.pikachu, Collections.singletonList(ItemIDs.lightBall));
        map.put(SpeciesIDs.chansey, Collections.singletonList(ItemIDs.luckyPunch));
        map.put(SpeciesIDs.ditto, Arrays.asList(ItemIDs.metalPowder, ItemIDs.quickPowder));
        map.put(SpeciesIDs.cubone, Collections.singletonList(ItemIDs.thickClub));
        map.put(SpeciesIDs.marowak, Collections.singletonList(ItemIDs.thickClub));
        map.put(SpeciesIDs.farfetchd, Collections.singletonList(ItemIDs.leek));
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, List<Integer>> abilityBoostingItems = initializeAbilityBoostingItems();

    private static Map<Integer, List<Integer>> initializeAbilityBoostingItems() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(AbilityIDs.guts, Arrays.asList(ItemIDs.flameOrb, ItemIDs.toxicOrb));
        map.put(AbilityIDs.magicGuard, Arrays.asList(ItemIDs.stickyBarb, ItemIDs.lifeOrb));
        return Collections.unmodifiableMap(map);
    }

    public static final Map<Integer,List<Integer>> abilityVariations = setupAbilityVariations();

    private static Map<Integer,List<Integer>> setupAbilityVariations() {
        Map<Integer,List<Integer>> map = new HashMap<>();
        map.put(AbilityIDs.insomnia, Arrays.asList(AbilityIDs.insomnia, AbilityIDs.vitalSpirit));
        map.put(AbilityIDs.clearBody, Arrays.asList(AbilityIDs.clearBody, AbilityIDs.whiteSmoke));
        map.put(AbilityIDs.hugePower, Arrays.asList(AbilityIDs.hugePower, AbilityIDs.purePower));
        map.put(AbilityIDs.battleArmor, Arrays.asList(AbilityIDs.battleArmor, AbilityIDs.shellArmor));
        map.put(AbilityIDs.cloudNine, Arrays.asList(AbilityIDs.cloudNine, AbilityIDs.airLock));
        map.put(AbilityIDs.filter, Arrays.asList(AbilityIDs.filter, AbilityIDs.solidRock));

        return map;
    }

    // Note: Flower Gift is NOT useless in this generation; it is in this list solely for consistency with future generations.
    public static final List<Integer> uselessAbilities = Arrays.asList(AbilityIDs.forecast, AbilityIDs.multitype, AbilityIDs.flowerGift);

    public static final int dpptSetVarScript = 0x28, hgssSetVarScript = 0x29;

    public static final int scriptListTerminator = 0xFD13;

    public static final int itemScriptVariable = 0x8008;

    private static final List<String> dpShopNames = Arrays.asList(
            "Sunyshore Secondary",
            "Jubilife Secondary",
            "Floaroma Secondary",
            "Oreburgh Secondary",
            "Eterna Secondary",
            "Eterna Herbs",
            "Snowpoint Secondary",
            "Solaceon Secondary",
            "Pastoria Secondary",
            "Celestic Secondary",
            "Hearthome Secondary",
            "Canalave Secondary",
            "Veilstone Department Store Secret Base Decorations 1",
            "Veilstone Department Store Secret Base Decorations 2",
            "Veilstone Department Store Vitamins",
            "Veilstone Department Store TMs 1",
            "Sunyshore Market Seals 1",
            "Sunyshore Market Seals 2",
            "Sunyshore Market Seals 3",
            "Sunyshore Market Seals 4",
            "Veilstone Department Store TMs 2",
            "Sunyshore Market Seals 5",
            "Sunyshore Market Seals 6",
            "Sunyshore Market Seals 7",
            "Pokemon League Secondary",
            "Veilstone Department Store X Items",
            "Veilstone Department Store Healing",
            "Veilstone Department Store Balls Etc.",
            "Progressive Shops"
    );

    private static final List<String> ptShopNames = Arrays.asList(
            "Jubilife Secondary",
            "Sunyshore Secondary",
            "Floaroma Secondary",
            "Oreburgh Secondary",
            "Eterna Herbs",
            "Canalave Secondary",
            "Pastoria Secondary",
            "Celestic Secondary",
            "Snowpoint Secondary",
            "Solaceon Secondary",
            "Eterna Secondary",
            "Hearthome Secondary",
            "Veilstone Department Store B1 Berries",
            "Veilstone Department Store Secret Base Decorations 1",
            "Veilstone Department Store Vitamins",
            "Veilstone Department Store Secret Base Decorations 2",
            "Veilstone Department Store TMs 1",
            "Sunyshore Market Seals 1",
            "Sunyshore Market Seals 2",
            "Sunyshore Market Seals 3",
            "Sunyshore Market Seals 4",
            "Veilstone Department Store TMs 2",
            "Sunyshore Market Seals 5",
            "Sunyshore Market Seals 6",
            "Sunyshore Market Seals 7",
            "Pokemon League Secondary",
            "Veilstone Department Store X Items",
            "Veilstone Department Store Healing",
            "Veilstone Department Store Balls Etc.",
            "Progressive Shops"
    );

    private static final List<String> hgssShopNames = Arrays.asList(
            "Cherrygrove Secondary",
            "Cerulean Secondary",
            "Ecruteak Secondary",
            "Celadon Department Store Mail",
            "Saffron Secondary",
            "Violet Secondary",
            "Blackthorn Secondary",
            "Olivine Secondary",
            "Fuchsia Secondary",
            "Lavender Secondary",
            "Pewter Secondary",
            "Viridian Secondary",
            "Azalea Secondary",
            "Mahogany Before Hideout",
            "Safari Zone Gate Southwest",
            "Goldenrod Herb Shop",
            "Cianwood Pharmacy",
            "Veilstone Department Store Secret Base Decorations 1",
            "Veilstone Department Store Secret Base Decorations 2",
            "Goldenrod Department Store Vitamins",
            "Celadon Department Store Vitamins",
            "Mt. Moon Square",
            "Sunyshore Market Seals 1",
            "Sunyshore Market Seals 2",
            "Sunyshore Market Seals 3",
            "Sunyshore Market Seals 4",
            "Sunyshore Market Seals 5",
            "Sunyshore Market Seals 6",
            "Unused Secondary",
            "Sunyshore Market Seals 7",
            "Pokeathlon Dome Data Card Shop 25-27",
            "Goldenrod Department Store X Items",
            "Celadon Department Store X Items",
            "Mahogany After Hideout",
            "Goldenrod Department Store Healing",
            "Celadon Department Store Healing",
            "Goldenrod Department Store Balls Etc.",
            "Goldenrod TMs",
            "Celadon Department Store Balls Etc.",
            "Celadon TMs",
            "Pokeathlon Dome Athlete Shop Sunday (Pre-National Dex)",
            "Pokeathlon Dome Data Card Shop 19-24",
            "Pokeathlon Dome Data Card Shop 1-6",
            "Pokeathlon Dome Athlete Shop Monday (Pre-National Dex)",
            "Pokeathlon Dome Athlete Shop Tuesday (Pre-National Dex)",
            "Pokeathlon Dome Data Card Shop 7-12",
            "Pokeathlon Dome Athlete Shop Wednesday (Pre-National Dex)",
            "Pokeathlon Dome Athlete Shop Thursday (Pre-National Dex)",
            "Pokeathlon Dome Athlete Shop Friday (Pre-National Dex)",
            "Pokeathlon Dome Athlete Shop Saturday (Pre-National Dex)",
            "Pokeathlon Dome Data Card Shop 13-18",
            "Pokeathlon Dome Athlete Shop Sunday (Post-National Dex)",
            "Pokeathlon Dome Athlete Shop Monday (Post-National Dex)",
            "Pokeathlon Dome Athlete Shop Tuesday (Post-National Dex)",
            "Pokeathlon Dome Athlete Shop Wednesday (Post-National Dex)",
            "Pokeathlon Dome Athlete Shop Thursday (Post-National Dex)",
            "Pokeathlon Dome Athlete Shop Friday (Post-National Dex)",
            "Pokeathlon Dome Athlete Shop Saturday (Post-National Dex)",
            "Progressive Shops"
    );

    public static List<String> getShopNames(int romType) {
        if (romType == Type_DP) {
            return dpShopNames;
        } else if (romType == Type_Plat) {
            return ptShopNames;
        } else if (romType == Type_HGSS) {
            return hgssShopNames;
        }
        return null;
    }

    public static final List<Integer> evolutionItems = Arrays.asList(ItemIDs.sunStone, ItemIDs.moonStone, ItemIDs.fireStone,
            ItemIDs.thunderStone, ItemIDs.waterStone, ItemIDs.leafStone, ItemIDs.shinyStone, ItemIDs.duskStone, ItemIDs.dawnStone,
            ItemIDs.ovalStone, ItemIDs.kingsRock, ItemIDs.deepSeaTooth, ItemIDs.deepSeaScale, ItemIDs.metalCoat, ItemIDs.dragonScale,
            ItemIDs.upgrade, ItemIDs.protector, ItemIDs.electirizer, ItemIDs.magmarizer, ItemIDs.dubiousDisc, ItemIDs.reaperCloth,
            ItemIDs.razorClaw, ItemIDs.razorFang);

    public static final Map<Integer,FormeInfo> formeMappings = setupFormeMappings();
    public static final Map<Integer,Integer> cosmeticForms = setupCosmeticForms();

    private static final Map<Integer,Map<Integer,String>> formeSuffixesByBaseForme = setupFormeSuffixesByBaseForme();
    private static final Map<Integer,String> dummyFormeSuffixes = setupDummyFormeSuffixes();

    private static final Map<Integer,Map<Integer,Integer>> absolutePokeNumsByBaseForme = setupAbsolutePokeNumsByBaseForme();
    private static final Map<Integer,Integer> dummyAbsolutePokeNums = setupDummyAbsolutePokeNums();

    public static String getFormeSuffixByBaseForme(int baseForme, int formNum) {
        return formeSuffixesByBaseForme.getOrDefault(baseForme,dummyFormeSuffixes).getOrDefault(formNum,"");
    }

    public static Integer getAbsolutePokeNumByBaseForme(int baseForme, int formNum) {
        return absolutePokeNumsByBaseForme.getOrDefault(baseForme,dummyAbsolutePokeNums).getOrDefault(formNum,baseForme);
    }

    public static final String lyraEthanMarillSpritePrefix = "274E0604C301274E0704E101274E0804";

    private final static int bulbasaurOverworldSpriteID = 297;

    /**
     * Returns the first overworld sprite ID for a given Species.
     * Some Species have more than one sprite ID, either due to gender
     * differences or formes, but this gives the first one only.
     */
    public static int getOverworldSpriteIDOfSpecies(int species) {
        int spriteID = bulbasaurOverworldSpriteID;
        for (int i = 1; i < species; i++) {
            spriteID += speciesToOverworldSpriteAmount.getOrDefault(i, 1);
        }
        return spriteID;
    }

    private final static Map<Integer, Integer> speciesToOverworldSpriteAmount = initSpeciesToOverworldSpriteAmount();

    private static Map<Integer, Integer> initSpeciesToOverworldSpriteAmount() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(SpeciesIDs.venusaur, 2);
        map.put(SpeciesIDs.pikachu, 2);
        map.put(SpeciesIDs.meganium, 2);
        map.put(SpeciesIDs.pichu, 2);
        map.put(SpeciesIDs.unown, 28);
        map.put(SpeciesIDs.wobbuffet, 2);
        map.put(SpeciesIDs.steelix, 2);
        map.put(SpeciesIDs.heracross, 2);
        map.put(SpeciesIDs.deoxys, 4);
        map.put(SpeciesIDs.burmy, 3);
        map.put(SpeciesIDs.wormadam, 3);
        map.put(SpeciesIDs.combee, 2);
        map.put(SpeciesIDs.shellos, 2);
        map.put(SpeciesIDs.gastrodon, 2);
        map.put(SpeciesIDs.gible, 2);
        map.put(SpeciesIDs.gabite, 2);
        map.put(SpeciesIDs.garchomp, 2);
        map.put(SpeciesIDs.hippopotas, 2);
        map.put(SpeciesIDs.hippowdon, 2);
        map.put(SpeciesIDs.rotom, 6);
        map.put(SpeciesIDs.giratina, 2);
        map.put(SpeciesIDs.arceus, 18);
        return map;
    }

    // Technically the property of being big or not is one of the sprites and not species,
    // but it just happens that all species with SOME overworld sprite that's big have all
    // their overworld sprites be big.
    public static final List<Integer> hgssBigOverworldPokemon = Collections.unmodifiableList(Arrays.asList(
            SpeciesIDs.steelix, SpeciesIDs.lugia, SpeciesIDs.hoOh, SpeciesIDs.wailord, SpeciesIDs.kyogre, SpeciesIDs.groudon,
            SpeciesIDs.rayquaza, SpeciesIDs.dialga, SpeciesIDs.palkia, SpeciesIDs.regigigas, SpeciesIDs.giratina, SpeciesIDs.arceus
    ));

    private static final Map<Integer, int[][]> otherPokemonGraphicsImagesDP = initOtherPokemonGraphicsImagesDP();
    private static final Map<Integer, int[][]> otherPokemonGraphicsImagesPt = initOtherPokemonGraphicsImagesPt();
    private static final Map<Integer, int[][]> otherPokemonGraphicsImagesHGSS = initOtherPokemonGraphicsImagesHGSS();

    private static Map<Integer, int[][]> initOtherPokemonGraphicsImagesDP() {
        Map<Integer, int[][]> map = new HashMap<>();
        map.put(SpeciesIDs.unown, new int[][]{
                // alphabetical order, !, ?
                {9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63},
                {8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62}});
        map.put(SpeciesIDs.castform, new int[][]{{68, 69, 70, 71}, {64, 65, 66, 67}}); // normal, sunny, rainy, snowy
        map.put(SpeciesIDs.deoxys, new int[][]{{1, 3, 5, 7}, {0, 2, 4, 6}}); // normal, attack, defense, speed
        map.put(SpeciesIDs.burmy, new int[][]{{73, 75, 77}, {72, 74, 76}}); // plant, sandy, trash
        map.put(SpeciesIDs.wormadam, new int[][]{{79, 81, 83}, {78, 80, 82}}); // plant, sandy, trash
        map.put(SpeciesIDs.cherrim, new int[][]{{94, 95}, {92, 93}}); // normal, sunny
        map.put(SpeciesIDs.shellos, new int[][]{{86, 87}, {84, 85}}); // west, east
        map.put(SpeciesIDs.gastrodon, new int[][]{{90, 91}, {88, 89}}); // west, east
        map.put(SpeciesIDs.arceus, new int[][]{
                // same order as types internally, see typeToByte() (though ??? type is also included)
                {97, 99, 101, 103, 105, 107, 109, 111, 113, 115, 117, 119, 121, 123, 125, 127, 129, 131},
                {96, 98, 100, 102, 104, 106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 130}});
        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, int[][]> initOtherPokemonGraphicsImagesPt() {
        Map<Integer, int[][]> map = new HashMap<>(otherPokemonGraphicsImagesDP);
        map.put(SpeciesIDs.rotom, new int[][]{
                // normal, heat, wash, frost, fan, mow
                {139, 141, 143, 145, 147, 149}, {138, 140, 142, 144, 146, 148}});
        map.put(SpeciesIDs.giratina, new int[][]{{151, 153}, {150, 152}}); // normal, origin
        map.put(SpeciesIDs.shaymin, new int[][]{{135, 137}, {134, 136}}); // normal, sky
        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, int[][]> initOtherPokemonGraphicsImagesHGSS() {
        Map<Integer, int[][]> map = new HashMap<>(otherPokemonGraphicsImagesPt);
        map.put(SpeciesIDs.pichu, new int[][]{{155, 157}, {154, 156}}); // normal, spiky-eared
        return Collections.unmodifiableMap(map);
    }

    /**
     * Maps {@link SpeciesIDs} IDs to the file indices of images found in the OtherPokemonGraphics NARC.<br>
     * [0] lists the front images, [1] the back images.
     */
    public static Map<Integer, int[][]> getOtherPokemonGraphicsImages(int romType) {
        if (romType == Type_DP) {
            return otherPokemonGraphicsImagesDP;
        } else if (romType == Type_Plat) {
            return otherPokemonGraphicsImagesPt;
        } else {
            return otherPokemonGraphicsImagesHGSS;
        }
    }

    public static final Map<Integer, int[][]> otherPokemonGraphicsPalettesDP = initOtherPokemonGraphicsPalettesDP();
    public static final Map<Integer, int[][]> otherPokemonGraphicsPalettesPt = initOtherPokemonGraphicsPalettesPt();
    public static final Map<Integer, int[][]> otherPokemonGraphicsPalettesHGSS = initOtherPokemonGraphicsPalettesHGSS();

    private static Map<Integer, int[][]> initOtherPokemonGraphicsPalettesDP() {
        Map<Integer, int[][]> map = new HashMap<>();
        map.put(SpeciesIDs.unown, new int[][]{
                // alphabetical order, !, ?
                {136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136},
                {137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137, 137}});
        map.put(SpeciesIDs.castform, new int[][]{{138, 139, 140, 141}, {142, 143, 144, 145}}); // normal, sunny, rainy, snowy
        map.put(SpeciesIDs.deoxys, new int[][]{{134, 134, 134, 134}, {135, 135, 135, 135}});
        map.put(SpeciesIDs.burmy, new int[][]{{146, 148, 150}, {147, 149, 151}}); // plant, sandy, trash
        map.put(SpeciesIDs.wormadam, new int[][]{{152, 154, 156}, {153, 155, 157}}); // plant, sandy, trash
        map.put(SpeciesIDs.cherrim, new int[][]{{166, 167}, {168, 169}}); // normal, sunny
        map.put(SpeciesIDs.shellos, new int[][]{{158, 160}, {159, 161}}); // west, east
        map.put(SpeciesIDs.gastrodon, new int[][]{{162, 164}, {163, 165}}); // west, east
        map.put(SpeciesIDs.arceus, new int[][]{
                // same order as types internally, see typeToByte() (though this also includes a ???-type)
                {170, 172, 174, 176, 178, 180, 182, 184, 186, 188, 190, 192, 194, 196, 198, 200, 202, 204},
                {171, 173, 175, 177, 179, 181, 183, 185, 187, 189, 191, 193, 195, 197, 199, 201, 203, 205}});
        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, int[][]> initOtherPokemonGraphicsPalettesPt() {
        int shift = calculateShift(otherPokemonGraphicsImagesDP, otherPokemonGraphicsImagesPt);
        Map<Integer, int[][]> map = new HashMap<>(shiftAllValues(otherPokemonGraphicsPalettesDP, shift));

        map.put(SpeciesIDs.rotom, new int[][] {
                // normal, heat, wash, frost, fan, mow
                {232, 234, 236, 238, 240, 242}, {233, 235, 237, 239, 241, 243}});
        map.put(SpeciesIDs.giratina, new int[][]{{244, 246}, {245, 247}});
        map.put(SpeciesIDs.shaymin, new int[][]{{228, 230}, {229, 231}}); // normal, sky
        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, int[][]> initOtherPokemonGraphicsPalettesHGSS() {
        int shift = calculateShift(otherPokemonGraphicsImagesPt, otherPokemonGraphicsImagesHGSS);
        Map<Integer, int[][]> map = new HashMap<>(shiftAllValues(otherPokemonGraphicsPalettesPt, shift));

        map.put(SpeciesIDs.pichu, new int[][]{{252, 254}, {253, 255}}); // normal, spiky-eared
        return Collections.unmodifiableMap(map);
    }

    private static int calculateShift(Map<Integer, int[][]> before, Map<Integer, int[][]> after) {
        return countValues(after) - countValues(before);
    }

    private static int countValues(Map<Integer, int[][]> before) {
        int count = 0;
        for (int[][] arr : before.values()) {
            for (int[] row : arr) {
                count += row.length;
            }
        }
        return count;
    }

    /**
     * Returns a copy of the {@link Map}, where all the values in its int array have been shifted/increased by 'shift'.
     */
    private static Map<Integer, int[][]> shiftAllValues(Map<Integer, int[][]> original, int shift) {
        // The palettes in OtherPokemonGraphics are in the same order between games,
        // but their file indices get shifted up when images for new forms (Rotom/Giratina/Shaymin in Pt, Pichu in HGSS)
        // are added to OtherPokemonGraphics. Here, we shift up all values in our _description_ of palettes in
        // OtherPokemonGraphics, so that they match the shifted indices in the actual games.
        Map<Integer, int[][]> shifted = new HashMap<>(original.size());

        for (Map.Entry<Integer, int[][]> entry : original.entrySet()) {
            int[][] arr = entry.getValue();
            int[][] arrCopy = new int[arr.length][];
            for (int i = 0; i < arr.length; i++) {
                int[] rowCopy = new int[arr[i].length];
                for (int j = 0; j < arr[i].length; j++) {
                    rowCopy[j] = arr[i][j] + shift;
                }
                arrCopy[i] = rowCopy;
            }
            shifted.put(entry.getKey(), arrCopy);
        }

        return shifted;
    }

    /**
     * Maps {@link SpeciesIDs} IDs to the file indices of palettes found in the OtherPokemonGraphics NARC.<br>
     * [0] lists the normal palettes, [1] the shiny palettes.
     */
    public static Map<Integer, int[][]> getOtherPokemonGraphicsPalettes(int romType) {
        if (romType == Type_DP) {
            return otherPokemonGraphicsPalettesDP;
        } else if (romType == Type_Plat) {
            return otherPokemonGraphicsPalettesPt;
        } else {
            return otherPokemonGraphicsPalettesHGSS;
        }
    }

    // The original slot each of the 20 "alternate" slots is mapped to
    // swarmx2, dayx2, nightx2, pokeradarx4, GBAx10
    // NOTE: in the game data there are 6 fillers between pokeradar and GBA

    public static final int[] dpptAlternateSlots = new int[] { 0, 1, 2, 3, 2, 3, 4, 5, 10, 11, 8, 9, 8, 9, 8, 9, 8, 9,
            8, 9 };

    public static final String[] dpptWaterSlotSetNames = new String[] { "Surfing", "Filler", "Old Rod", "Good Rod",
            "Super Rod" };

    public static final String[] hgssTimeOfDayNames = new String[] { "Morning", "Day", "Night" };

    public static final String[] hgssNonWalkingAreaNames = new String[] { "", "Surfing", "Rock Smash", "Old Rod",
            "Good Rod", "Super Rod" };
    public static final EncounterType[] hgssNonWalkingAreaTypes = new EncounterType[]{EncounterType.UNUSED,
            EncounterType.SURFING, EncounterType.INTERACT, EncounterType.FISHING, EncounterType.FISHING,
            EncounterType.FISHING};
    public static final int hgssGoodRodReplacementIndex = 3, hgssSuperRodReplacementIndex = 1;

    public static final double stabMultiplier = 1.5;

    public static final MoveCategory[] moveCategoryIndices = { MoveCategory.PHYSICAL, MoveCategory.SPECIAL,
            MoveCategory.STATUS };

    public static byte moveCategoryToByte(MoveCategory cat) {
        switch (cat) {
        case PHYSICAL:
            return 0;
        case SPECIAL:
            return 1;
        case STATUS:
        default:
            return 2;
        }
    }

    public static final int noDamageSleepEffect = 1, damagePoisonEffect = 2, damageAbsorbEffect = 3, damageBurnEffect = 4,
            damageFreezeEffect = 5, damageParalyzeEffect = 6, dreamEaterEffect = 8, noDamageAtkPlusOneEffect = 10,
            noDamageDefPlusOneEffect = 11, noDamageSpAtkPlusOneEffect = 13, noDamageEvasionPlusOneEffect = 16,
            noDamageAtkMinusOneEffect = 18, noDamageDefMinusOneEffect = 19, noDamageSpeMinusOneEffect = 20,
            noDamageAccuracyMinusOneEffect = 23, noDamageEvasionMinusOneEffect = 24, flinchEffect = 31, toxicEffect = 33,
            razorWindEffect = 39, bindingEffect = 42, increasedCritEffect = 43, damageRecoil25PercentEffect = 48,
            noDamageConfusionEffect = 49, noDamageAtkPlusTwoEffect = 50, noDamageDefPlusTwoEffect = 51,
            noDamageSpePlusTwoEffect = 52, noDamageSpAtkPlusTwoEffect = 53, noDamageSpDefPlusTwoEffect = 54,
            noDamageAtkMinusTwoEffect = 58, noDamageDefMinusTwoEffect = 59, noDamageSpeMinusTwoEffect = 60,
            noDamageSpDefMinusTwoEffect = 62, noDamagePoisonEffect = 66, noDamageParalyzeEffect = 67,
            damageAtkMinusOneEffect = 68, damageDefMinusOneEffect = 69, damageSpeMinusOneEffect = 70,
            damageSpAtkMinusOneEffect = 71, damageSpDefMinusOneEffect = 72, damageAccuracyMinusOneEffect = 73,
            skyAttackEffect = 75, damageConfusionEffect = 76, twineedleEffect = 77, rechargeEffect = 80, snoreEffect = 92,
            trappingEffect = 106, minimizeEffect = 108, swaggerEffect = 118, damageBurnAndThawUserEffect = 125,
            damageUserDefPlusOneEffect = 138, damageUserAtkPlusOneEffect = 139, damageUserAllPlusOneEffect = 140,
            skullBashEffect = 145, twisterEffect = 146, futureSightAndDoomDesireEffect = 148, stompEffect = 150,
            solarbeamEffect = 151, thunderEffect = 152, flyEffect = 155, defenseCurlEffect = 156,
            fakeOutEffect = 158, flatterEffect = 166, noDamageBurnEffect = 167, chargeEffect = 174,
            damageUserAtkAndDefMinusOneEffect = 182, damageRecoil33PercentEffect = 198, teeterDanceEffect = 199,
            blazeKickEffect = 200, poisonFangEffect = 202, damageUserSpAtkMinusTwoEffect = 204,
            noDamageAtkAndDefMinusOneEffect = 205, noDamageDefAndSpDefPlusOneEffect = 206,
            noDamageAtkAndDefPlusOneEffect = 208, damagePoisonWithIncreasedCritEffect = 209,
            noDamageSpAtkAndSpDefPlusOneEffect = 211, noDamageAtkAndSpePlusOneEffect = 212,
            damageUserSpeMinusOneEffect = 218, damageUserDefAndSpDefMinusOneEffect = 229, flareBlitzEffect = 253,
            diveEffect = 255, digEffect = 256, blizzardEffect = 260, voltTackleEffect = 262, bounceEffect = 263,
            noDamageSpAtkMinusTwoEffect = 265, chatterEffect = 267, damageRecoil50PercentEffect = 269,
            damageSpDefMinusTwoEffect = 271, shadowForceEffect = 272, fireFangEffect = 273, iceFangEffect = 274,
            thunderFangEffect = 275, damageUserSpAtkPlusOneEffect = 276;

    public static final List<Integer> soundMoves = Arrays.asList(MoveIDs.growl, MoveIDs.roar, MoveIDs.sing, MoveIDs.supersonic,
            MoveIDs.screech, MoveIDs.snore, MoveIDs.uproar, MoveIDs.metalSound, MoveIDs.grassWhistle, MoveIDs.hyperVoice,
            MoveIDs.bugBuzz, MoveIDs.chatter, MoveIDs.perishSong, MoveIDs.healBell);

    public static final List<Integer> punchMoves = Arrays.asList(MoveIDs.icePunch, MoveIDs.firePunch, MoveIDs.thunderPunch,
            MoveIDs.machPunch, MoveIDs.focusPunch, MoveIDs.dizzyPunch, MoveIDs.dynamicPunch, MoveIDs.hammerArm, MoveIDs.megaPunch,
            MoveIDs.cometPunch, MoveIDs.meteorMash, MoveIDs.shadowPunch, MoveIDs.drainPunch, MoveIDs.bulletPunch, MoveIDs.skyUppercut);

    public static final List<Integer> dpRequiredFieldTMs = Arrays.asList(2, 3, 5, 9, 12, 19, 23, 28,
            34, 39, 41, 43, 46, 47, 49, 50, 62, 69, 79, 80, 82, 84, 85, 87);

    public static final List<Integer> ptRequiredFieldTMs = Arrays.asList(2, 3, 5, 7, 9, 11, 12, 18, 19,
            23, 28, 34, 37, 39, 41, 43, 46, 47, 49, 50, 62, 69, 79, 80, 82, 84, 85, 87);

    public static final List<Integer> dpptFieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.waterfall, MoveIDs.rockSmash, MoveIDs.sweetScent, MoveIDs.defog, MoveIDs.rockClimb);

    public static final List<Integer> hgssFieldMoves = Arrays.asList(
            MoveIDs.cut, MoveIDs.fly, MoveIDs.surf, MoveIDs.strength, MoveIDs.flash, MoveIDs.dig, MoveIDs.teleport,
            MoveIDs.whirlpool, MoveIDs.waterfall, MoveIDs.rockSmash, MoveIDs.headbutt, MoveIDs.sweetScent, MoveIDs.rockClimb);

    public static final List<Integer> dpptEarlyRequiredHMMoves = Arrays.asList(MoveIDs.rockSmash, MoveIDs.cut);

    public static final List<Integer> hgssEarlyRequiredHMMoves = Collections.singletonList(MoveIDs.cut);

    public static ItemList allowedItems, nonBadItems;
    public static List<Integer> regularShopItems, opShopItems;

    public static final String shedinjaSpeciesLocator = "492080000090281C0521";

    public static final int ilexForestScriptFile = 92, ilexForestStringsFile = 115;
    public static final List<Integer> headbuttTutorScriptOffsets = Arrays.asList(0xF55, 0xFC5, 0x100A, 0x104C);

    private static final String doubleBattleFixPrefixDP = "022912D90221214201", doubleBattleFixPrefixPt = "022919D90221214205",
            doubleBattleFixPrefixHGSS = "2C2815D00221214201";

    public static final String feebasLevelPrefixDPPt = "019813B0F0BD", honeyTreeLevelPrefixDPPt = "F0BDF0B589B0051C0C1C";

    private static final String runningShoesCheckPrefixDPPt = "281C0C24", runningShoesCheckPrefixHGSS = "301C0C24";

    public static final String distortionWorldGroundCheckPrefix = "23D849187944C988090409148F44";

    public static final List<String> dpptIntroPrefixes = Arrays.asList("381CF8BDC046", "08B0F8BD");

    public static final String hpBarSpeedPrefix = "0CD106200090", expBarSpeedPrefix = "011C00D101212E6C", bothBarsSpeedPrefix = "70BD90421DDA";

    public static final String dpptEggMoveTablePrefix = "40016601";

    public static final String typeEffectivenessTableLocator = "000505000805";

    private static final int trophyGardenGrassEncounterIndexDP = 304, trophyGardenGrassEncounterIndexPt = 308;
    private static final List<Integer> marshGrassEncounterIndicesDP = Arrays.asList(76, 82, 88, 94, 100, 102),
            marshGrassEncounterIndicesPt = Arrays.asList(76, 82, 88, 94, 100, 106);

    public static final String pickupTableLocator = "110012001A000300", rarePickupTableLocator = "19005C00DD00";
    public static final int numberOfCommonPickupItems = 18, numberOfRarePickupItems = 11;

    public static final String friendshipValueForEvoLocator = "DC286AD3";

    public static final String perfectOddsBranchLocator = "FF2901D30425";

    public static final int[] dpptOverworldDexMaps = new int[] {
            1,  2,  3,  4,  5, -1, -1,  6, -1,  7, // 0-9 (cities, pkmn league, wind/ironworks)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 10-19 (all mt coronet)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 20-29 (mt coronet, great marsh, solaceon ruins)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 30-39 (all solaceon ruins)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 40-49 (solaceon ruins/v.road)
            -1, -1, -1, -1, -1, -1,  8, -1, -1, -1, // 50-59 (v.road, stark mountain outer then inner, sendoff spring)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 60-69 (unknown, turnback cave)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 70-79 (all turnback cave)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 80-89 (all unknown)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 90-99 (all unknown)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 100-109 (unknown, snowpoint temple)
            -1, -1, -1, -1, -1, -1, -1, -1,  9, -1, // 110-119 (various dungeons, iron island outer/inner)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 120-129 (rest of iron island inner, old chateau)
            -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, // 130-139 (old chateau, inner lakes, lakefronts)
            12, 13, 14, 15, 16, 17, 18, 19, 20, 21, // 140-149 (first few routes)
            22, -1, -1, -1, -1, -1, 23, 24, 25, 26, // 150-159 (route 209 + lost tower, more routes)
            27, 28, 29, 30, 31, 32, 33, 34, 35, 36, // 160-169 (routes; 220 is skipped until later)
            37, 38, 39, 40, 41, 42, 43, 44, 45, 46, // 170-179 (last few land routes, towns, resort area, first sea route)
            47, 48, 49,                             // 180-182 (other sea routes)
    };

    public static final int[] dpptDungeonDexMaps = new int[] {
            -1, -1, -1, -1, -1,  1,  1, -1,  2, -1, // 0-9 (cities, pkmn league, wind/ironworks, mine/forest)
            3,  3,  3,  3,  3,  3,  3,  3,  3,  3, // 10-19 (all mt coronet)
            3,  3,  3,  4,  4,  4,  4,  4,  4,  5, // 20-29 (mt coronet, great marsh, solaceon ruins)
            5,  5,  5,  5,  5,  5,  5,  5,  5,  5, // 30-39 (all solaceon ruins)
            5,  5,  5,  5,  5,  5,  5,  6,  6,  6, // 40-49 (solaceon ruins/v.road)
            6,  6,  6,  7,  8,  8, -1,  9,  9, 10, // 50-59 (v.road, stark mountain outer then inner, sendoff spring)
            -1, -1, -1, 10, 10, 10, 10, 10, 10, 10, // 60-69 (unknown, turnback cave)
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10, // 70-79 (all turnback cave)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 80-89 (all unknown)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 90-99 (all unknown)
            -1, -1, -1, -1, -1, -1, 11, 11, 11, 11, // 100-109 (unknown, snowpoint temple)
            11, 11, 12, 12, 13, 13, 13, 14, -1, 15, // 110-119 (various dungeons, iron island outer/inner)
            15, 15, 15, 15, 15, 16, 16, 16, 16, 16, // 120-129 (rest of iron island inner, old chateau)
            16, 16, 16, 16, 17, 17, 18, 19, -1, -1, // 130-139 (old chateau, inner lakes, lakefronts)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 140-149 (first few routes)
            -1, 20, 20, 20, 20, 20, -1, -1, -1, -1, // 150-159 (route 209 + lost tower, more routes)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 160-169 (routes; 220 is skipped until later)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 170-179 (last few land routes, towns, resort area, first sea route)
            -1, -1, -1,                             // 180-182 (other sea routes)
    };

    public static final int[] hgssOverworldDexMaps = new int[] {
            1,  2,  3,  4,  5,  6, -1, -1,  7, -1, // 0-9 (first few cities/routes, sprout tower + alph)
            -1, -1, -1, -1, -1, -1, -1,  8, -1, -1, // 10-19 (more alph, union cave, r33, slowpoke)
            -1,  9, 10, -1, -1, 11, 12, 13, -1, -1, // 20-29 (ilex, routes, natpark, routes, burned)
            -1, -1, -1, -1, -1, -1, -1, -1, 14, 15, // 30-39 (bell tower, routes)
            16, 17, 18, -1, -1, -1, -1, -1, -1, -1, // 40-49 (olivine, routes, whirl islands, missing slots)
            -1, 19, 20, -1, -1, -1, -1, 21, 22, 23, // 50-59 (missing, cianwood, routes, mortar)
            -1, -1, -1, -1, -1, 24, -1, 25, 26, -1, // 60-69 (ice path, missing, blackthorn, dragons, routes, dark)
            -1, 27, -1, -1, -1, -1, -1, -1, -1, -1, // 70-79 (dark, route 47, moon, seafoam, silver cave)
            -1, -1, -1, -1, -1, 28, -1, -1, -1, -1, // 80-89 (more silver cave, cliff stuff, random bell tower)
            -1, -1, 29, 30, 31, 32, 33, 34, 35, 36, // 90-99 (missing, saf zone, kanto routes/cities)
            37, 38, 39, 40, 41, 42, -1, -1, -1, -1, // 100-109 (more cities, some routes, more moon, RT)
            -1, 43, 44, 45, 46, 47, 48, 49, 50, 51, // 110-119 (vroad, routes 1-9)
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // 120-129 (routes 10-21)
            62, 63, -1, -1, -1, -1, 64, -1, -1, -1, // 130-139 (last 2 routes, tohjo, DC, VR, route 2 north, VF, CC)
            -1, -1,                                 // 140-141 (cerulean cave)
    };

    public static final int[] hgssDungeonDexMaps = new int[] {
            -1, -1, -1, -1, -1, -1,  1,  1, -1,  2, // 0-9 (first few cities/routes, sprout tower + alph)
            2,  2,  2,  2,  3,  3,  3, -1,  4,  4, // 10-19 (more alph, union cave, r33, slowpoke)
            5, -1, -1,  6,  -1, -1, -1, -1,  7,  7, // 20-29 (ilex, routes, natpark, routes, burned)
            8,  8,  8,  8,  8,  8,  8,  8, -1, -1, // 30-39 (bell tower, routes)
            -1, -1, -1,  9,  9, -1,  9, -1,  9, -1, // 40-49 (olivine, routes, whirl islands, missing slots)
            -1, -1, -1, 10, 10, 10, 10, -1, -1, -1, // 50-59 (missing, cianwood, routes, mortar)
            11, 11, 11, 11, -1, -1, 12, -1, -1, 13, // 60-69 (ice path, missing, blackthorn, dragons, routes, dark)
            13, -1, 14, 14, 15, 15, 15, 15, 15, 16, // 70-79 (dark, route 47, moon, seafoam, silver cave)
            16, 16, 17, 18,  8, -1, 16, 16, 16, 16, // 80-89 (more silver cave, cliff stuff, random bell tower)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 90-99 (missing, saf zone, kanto routes/cities)
            -1, -1, -1, -1, -1, -1, 14, 14, 20, 20, // 100-109 (more cities, some routes, more moon, RT)
            21, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 110-119 (vroad, routes 1-9)
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 120-129 (routes 10-21)
            -1, -1, 22, 23, 21, 21, -1, 24, -1, 25, // 130-139 (last 2 routes, tohjo, DC, VR, route 2 north, VF, CC)
            25, 25,                                 // 140-141 (cerulean cave)
    };

    public static final int[] hgssHeadbuttOverworldDexMaps = new int[] {
            43, 44, 45, 46, 47, 48, 49, 50, 53, 29, // Routes 1-12, skipping 9 and 10
            54, 55, 56, 59, 61, 63, 40, 41, 42,  2, // Routes 13-15, Route 18, Route 22, Routes 25-29
             4,  5,  7,  8,  9, 10, 11, 12, 14, 15, // Routes 30-39
            20, 21, 23, 25, 26, 32, 33, 65, 34, 35, // Routes 42-46, first five Kanto cities
            36, 37,  1,  3,  6, 66, 13, 22, 28, 60, // Remaining Kanto cities, Johto cities, Lake of Rage, Mt Silver, Route 21
            -1, -1, -1, 27, 39, 67, 64, 57, -1,     // National Park, Ilex/Viridian Forest, Routes 47-48, Safari Zone Gate, Routes 2 (north) and 16, Mt Silver Cave
    };

    public static final int[] hgssHeadbuttDungeonDexMaps = new int[] {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // Routes 1-12, skipping 9 and 10
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // Routes 13-15, Route 18, Route 22, Routes 25-29
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // Routes 30-39
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // Routes 42-46, first five Kanto cities
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // Remaining Kanto cities, Johto cities, Lake of Rage, Mt Silver, Route 21
             6,  5, 24, -1, -1, -1, -1, -1, 16,     // National Park, Ilex/Viridian Forest, Routes 47-48, Safari Zone Gate, Routes 2 (north) and 16, Mt Silver Cave
    };

    public static final int pokedexAreaDataSize = 495;
    public static final int dpptMtCoronetDexIndex = 3, dpptGreatMarshDexIndex = 4, dpptTrophyGardenDexIndex = 14, dpptFloaromaMeadowDexIndex = 21;
    public static final List<Integer> dpptOverworldHoneyTreeDexIndicies = Arrays.asList(6, 7, 17, 18, 19, 20, 21, 22, 23, 24, 26, 27, 28, 29, 30, 31, 34, 36, 37, 50);
    public static final List<Integer> partnerTrainerIndices = Arrays.asList(608, 609, 610, 611, 612);

    static {
        setupAllowedItems();
    }

    private static void setupAllowedItems() {
        allowedItems = new ItemList(ItemIDs.enigmaStone);
        // Key items + version exclusives
        allowedItems.banRange(ItemIDs.explorerKit, 109);
        // Unknown blank items or version exclusives
        allowedItems.banRange(ItemIDs.griseousOrb, 23);
        // HMs
        allowedItems.banRange(ItemIDs.hm01, 8);
        // TMs
        allowedItems.tmRange(ItemIDs.tm01, 92);

        // non-bad items
        // ban specific pokemon hold items, berries, apricorns, mail
        nonBadItems = allowedItems.copy();

        nonBadItems.banSingles(ItemIDs.oddKeystone, ItemIDs.griseousOrb, ItemIDs.soulDew, ItemIDs.lightBall,
                ItemIDs.oranBerry, ItemIDs.quickPowder);
        nonBadItems.banRange(ItemIDs.shoalSalt,2);
        nonBadItems.banRange(ItemIDs.growthMulch, 4); // mulch
        nonBadItems.banRange(ItemIDs.adamantOrb, 2); // orbs
        nonBadItems.banRange(ItemIDs.mail1, 12); // mails
        nonBadItems.banRange(ItemIDs.figyBerry, 25); // berries without useful battle effects
        nonBadItems.banRange(ItemIDs.luckyPunch, 4); // pokemon specific
        nonBadItems.banRange(ItemIDs.redScarf, 5); // contest scarves

        regularShopItems = new ArrayList<>();

        regularShopItems.addAll(IntStream.rangeClosed(ItemIDs.ultraBall, ItemIDs.pokeBall).boxed().collect(Collectors.toList()));
        regularShopItems.addAll(IntStream.rangeClosed(ItemIDs.potion, ItemIDs.revive).boxed().collect(Collectors.toList()));
        regularShopItems.addAll(IntStream.rangeClosed(ItemIDs.superRepel, ItemIDs.repel).boxed().collect(Collectors.toList()));

        opShopItems = new ArrayList<>();

        // "Money items" etc
        opShopItems.add(ItemIDs.rareCandy);
        opShopItems.addAll(IntStream.rangeClosed(ItemIDs.tinyMushroom, ItemIDs.nugget).boxed().collect(Collectors.toList()));
        opShopItems.add(ItemIDs.rareBone);
        opShopItems.add(ItemIDs.luckyEgg);
    }

    public static String getDoubleBattleFixPrefix(int romType) {
        if (romType == Gen4Constants.Type_DP) {
            return doubleBattleFixPrefixDP;
        } else if (romType == Gen4Constants.Type_Plat) {
            return doubleBattleFixPrefixPt;
        } else {
            return doubleBattleFixPrefixHGSS;
        }
    }

    public static String getRunWithoutRunningShoesPrefix(int romType) {
        if (romType == Gen4Constants.Type_DP || romType == Gen4Constants.Type_Plat) {
            return runningShoesCheckPrefixDPPt;
        } else {
            return runningShoesCheckPrefixHGSS;
        }
    }

    public static int getTrophyGardenGrassEncounterIndex(int romType) {
        if (romType == Gen4Constants.Type_DP) {
            return trophyGardenGrassEncounterIndexDP;
        } else {
            return trophyGardenGrassEncounterIndexPt;
        }
    }

    public static List<Integer> getMarshGrassEncounterIndices(int romType) {
        if (romType == Gen4Constants.Type_DP) {
            return marshGrassEncounterIndicesDP;
        } else {
            return marshGrassEncounterIndicesPt;
        }
    }

    public static int getTextCharsPerLine(int romType) {
        if (romType == Gen4Constants.Type_HGSS) {
            return hgssTextCharsPerLine;
        } else {
            return dpptTextCharsPerLine;
        }
    }

    public static final Map<Integer,Integer> balancedItemPrices = Stream.of(new Integer[][] {
            // Skip item index 0. All prices divided by 10
            {ItemIDs.masterBall, 300},
            {ItemIDs.ultraBall, 120},
            {ItemIDs.greatBall, 60},
            {ItemIDs.pokeBall, 20},
            {ItemIDs.safariBall, 50},
            {ItemIDs.netBall, 100},
            {ItemIDs.diveBall, 100},
            {ItemIDs.nestBall, 100},
            {ItemIDs.repeatBall, 100},
            {ItemIDs.timerBall, 100},
            {ItemIDs.luxuryBall, 100},
            {ItemIDs.premierBall, 20},
            {ItemIDs.duskBall, 100},
            {ItemIDs.healBall, 30},
            {ItemIDs.quickBall, 100},
            {ItemIDs.cherishBall, 20},
            {ItemIDs.potion, 30},
            {ItemIDs.antidote, 10},
            {ItemIDs.burnHeal, 25},
            {ItemIDs.iceHeal, 25},
            {ItemIDs.awakening, 25},
            {ItemIDs.paralyzeHeal, 20},
            {ItemIDs.fullRestore, 300},
            {ItemIDs.maxPotion, 250},
            {ItemIDs.hyperPotion, 120},
            {ItemIDs.superPotion, 70},
            {ItemIDs.fullHeal, 60},
            {ItemIDs.revive, 150},
            {ItemIDs.maxRevive, 400},
            {ItemIDs.freshWater, 40},
            {ItemIDs.sodaPop, 60},
            {ItemIDs.lemonade, 70},
            {ItemIDs.moomooMilk, 80},
            {ItemIDs.energyPowder, 40},
            {ItemIDs.energyRoot, 110},
            {ItemIDs.healPowder, 45},
            {ItemIDs.revivalHerb, 280},
            {ItemIDs.ether, 300},
            {ItemIDs.maxEther, 450},
            {ItemIDs.elixir, 1500},
            {ItemIDs.maxElixir, 1800},
            {ItemIDs.lavaCookie, 45},
            {ItemIDs.berryJuice, 10},
            {ItemIDs.sacredAsh, 1000},
            {ItemIDs.hpUp, 980},
            {ItemIDs.protein, 980},
            {ItemIDs.iron, 980},
            {ItemIDs.carbos, 980},
            {ItemIDs.calcium, 980},
            {ItemIDs.rareCandy, 1000},
            {ItemIDs.ppUp, 980},
            {ItemIDs.zinc, 980},
            {ItemIDs.ppMax, 2490},
            {ItemIDs.oldGateau, 45},
            {ItemIDs.guardSpec, 70},
            {ItemIDs.direHit, 65},
            {ItemIDs.xAttack, 50},
            {ItemIDs.xDefense, 55},
            {ItemIDs.xSpeed, 35},
            {ItemIDs.xAccuracy, 95},
            {ItemIDs.xSpAtk, 35},
            {ItemIDs.xSpDef, 35},
            {ItemIDs.pokeDoll, 100},
            {ItemIDs.fluffyTail, 100},
            {ItemIDs.blueFlute, 2},
            {ItemIDs.yellowFlute, 2},
            {ItemIDs.redFlute, 2},
            {ItemIDs.blackFlute, 2},
            {ItemIDs.whiteFlute, 2},
            {ItemIDs.shoalSalt, 2},
            {ItemIDs.shoalShell, 2},
            {ItemIDs.redShard, 40},
            {ItemIDs.blueShard, 40},
            {ItemIDs.yellowShard, 40},
            {ItemIDs.greenShard, 40},
            {ItemIDs.superRepel, 50},
            {ItemIDs.maxRepel, 70},
            {ItemIDs.escapeRope, 55},
            {ItemIDs.repel, 35},
            {ItemIDs.sunStone, 300},
            {ItemIDs.moonStone, 300},
            {ItemIDs.fireStone, 300},
            {ItemIDs.thunderStone, 300},
            {ItemIDs.waterStone, 300},
            {ItemIDs.leafStone, 300},
            {ItemIDs.tinyMushroom, 50},
            {ItemIDs.bigMushroom, 500},
            {ItemIDs.pearl, 140},
            {ItemIDs.bigPearl, 750},
            {ItemIDs.stardust, 200},
            {ItemIDs.starPiece, 980},
            {ItemIDs.nugget, 1000},
            {ItemIDs.heartScale, 500},
            {ItemIDs.honey, 50},
            {ItemIDs.growthMulch, 20},
            {ItemIDs.dampMulch, 20},
            {ItemIDs.stableMulch, 20},
            {ItemIDs.gooeyMulch, 20},
            {ItemIDs.rootFossil, 500},
            {ItemIDs.clawFossil, 500},
            {ItemIDs.helixFossil, 500},
            {ItemIDs.domeFossil, 500},
            {ItemIDs.oldAmber, 800},
            {ItemIDs.armorFossil, 500},
            {ItemIDs.skullFossil, 500},
            {ItemIDs.rareBone, 1000},
            {ItemIDs.shinyStone, 300},
            {ItemIDs.duskStone, 300},
            {ItemIDs.dawnStone, 300},
            {ItemIDs.ovalStone, 300},
            {ItemIDs.oddKeystone, 210},
            {ItemIDs.griseousOrb, 1000},
            {ItemIDs.tea, 0}, // unused in Gen 4
            {ItemIDs.unused114, 0},
            {ItemIDs.autograph, 0}, // unused in Gen 4
            {ItemIDs.douseDrive, 0}, // unused in Gen 4
            {ItemIDs.shockDrive, 0}, // unused in Gen 4
            {ItemIDs.burnDrive, 0}, // unused in Gen 4
            {ItemIDs.chillDrive, 0}, // unused in Gen 4
            {ItemIDs.unused120, 0}, // unused in Gen 4
            {ItemIDs.pokemonBox, 0}, // unused in Gen 4
            {ItemIDs.medicinePocket, 0}, // unused in Gen 4
            {ItemIDs.tmCase, 0}, // unused in Gen 4
            {ItemIDs.candyJar, 0}, // unused in Gen 4
            {ItemIDs.powerUpPocket, 0}, // unused in Gen 4
            {ItemIDs.clothingTrunk, 0}, // unused in Gen 4
            {ItemIDs.catchingPocket, 0}, // unused in Gen 4
            {ItemIDs.battlePocket, 0}, // unused in Gen 4
            {ItemIDs.unused129, 0},
            {ItemIDs.unused130, 0},
            {ItemIDs.unused131, 0},
            {ItemIDs.unused132, 0},
            {ItemIDs.unused133, 0},
            {ItemIDs.sweetHeart, 0}, // unused in Gen 4
            {ItemIDs.adamantOrb, 1000},
            {ItemIDs.lustrousOrb, 1000},
            {ItemIDs.mail1, 5},
            {ItemIDs.mail2, 5},
            {ItemIDs.mail3, 5},
            {ItemIDs.mail4, 5},
            {ItemIDs.mail5, 5},
            {ItemIDs.mail6, 5},
            {ItemIDs.mail7, 5},
            {ItemIDs.mail8, 5},
            {ItemIDs.mail9, 5},
            {ItemIDs.mail10, 5},
            {ItemIDs.mail11, 5},
            {ItemIDs.mail12, 5},
            {ItemIDs.cheriBerry, 20},
            {ItemIDs.chestoBerry, 25},
            {ItemIDs.pechaBerry, 10},
            {ItemIDs.rawstBerry, 25},
            {ItemIDs.aspearBerry, 25},
            {ItemIDs.leppaBerry, 300},
            {ItemIDs.oranBerry, 5},
            {ItemIDs.persimBerry, 20},
            {ItemIDs.lumBerry, 50},
            {ItemIDs.sitrusBerry, 50},
            {ItemIDs.figyBerry, 10},
            {ItemIDs.wikiBerry, 10},
            {ItemIDs.magoBerry, 10},
            {ItemIDs.aguavBerry, 10},
            {ItemIDs.iapapaBerry, 10},
            {ItemIDs.razzBerry, 50},
            {ItemIDs.blukBerry, 50},
            {ItemIDs.nanabBerry, 50},
            {ItemIDs.wepearBerry, 50},
            {ItemIDs.pinapBerry, 50},
            {ItemIDs.pomegBerry, 50},
            {ItemIDs.kelpsyBerry, 50},
            {ItemIDs.qualotBerry, 50},
            {ItemIDs.hondewBerry, 50},
            {ItemIDs.grepaBerry, 50},
            {ItemIDs.tamatoBerry, 50},
            {ItemIDs.cornnBerry, 50},
            {ItemIDs.magostBerry, 50},
            {ItemIDs.rabutaBerry, 50},
            {ItemIDs.nomelBerry, 50},
            {ItemIDs.spelonBerry, 50},
            {ItemIDs.pamtreBerry, 50},
            {ItemIDs.watmelBerry, 50},
            {ItemIDs.durinBerry, 50},
            {ItemIDs.belueBerry, 50},
            {ItemIDs.occaBerry, 100},
            {ItemIDs.passhoBerry, 100},
            {ItemIDs.wacanBerry, 100},
            {ItemIDs.rindoBerry, 100},
            {ItemIDs.yacheBerry, 100},
            {ItemIDs.chopleBerry, 100},
            {ItemIDs.kebiaBerry, 100},
            {ItemIDs.shucaBerry, 100},
            {ItemIDs.cobaBerry, 100},
            {ItemIDs.payapaBerry, 100},
            {ItemIDs.tangaBerry, 100},
            {ItemIDs.chartiBerry, 100},
            {ItemIDs.kasibBerry, 100},
            {ItemIDs.habanBerry, 100},
            {ItemIDs.colburBerry, 100},
            {ItemIDs.babiriBerry, 100},
            {ItemIDs.chilanBerry, 100},
            {ItemIDs.liechiBerry, 100},
            {ItemIDs.ganlonBerry, 100},
            {ItemIDs.salacBerry, 100},
            {ItemIDs.petayaBerry, 100},
            {ItemIDs.apicotBerry, 100},
            {ItemIDs.lansatBerry, 100},
            {ItemIDs.starfBerry, 100},
            {ItemIDs.enigmaBerry, 100},
            {ItemIDs.micleBerry, 100},
            {ItemIDs.custapBerry, 100},
            {ItemIDs.jabocaBerry, 100},
            {ItemIDs.rowapBerry, 100},
            {ItemIDs.brightPowder, 300},
            {ItemIDs.whiteHerb, 100},
            {ItemIDs.machoBrace, 300},
            {ItemIDs.expShare, 600},
            {ItemIDs.quickClaw, 450},
            {ItemIDs.sootheBell, 100},
            {ItemIDs.mentalHerb, 100},
            {ItemIDs.choiceBand, 1000},
            {ItemIDs.kingsRock, 500},
            {ItemIDs.silverPowder, 200},
            {ItemIDs.amuletCoin, 1500},
            {ItemIDs.cleanseTag, 100},
            {ItemIDs.soulDew, 20},
            {ItemIDs.deepSeaTooth, 300},
            {ItemIDs.deepSeaScale, 300},
            {ItemIDs.smokeBall, 20},
            {ItemIDs.everstone, 20},
            {ItemIDs.focusBand, 300},
            {ItemIDs.luckyEgg, 1000},
            {ItemIDs.scopeLens, 500},
            {ItemIDs.metalCoat, 300},
            {ItemIDs.leftovers, 1000},
            {ItemIDs.dragonScale, 300},
            {ItemIDs.lightBall, 10},
            {ItemIDs.softSand, 200},
            {ItemIDs.hardStone, 200},
            {ItemIDs.miracleSeed, 200},
            {ItemIDs.blackGlasses, 200},
            {ItemIDs.blackBelt, 200},
            {ItemIDs.magnet, 200},
            {ItemIDs.mysticWater, 200},
            {ItemIDs.sharpBeak, 200},
            {ItemIDs.poisonBarb, 200},
            {ItemIDs.neverMeltIce, 200},
            {ItemIDs.spellTag, 200},
            {ItemIDs.twistedSpoon, 200},
            {ItemIDs.charcoal, 200},
            {ItemIDs.dragonFang, 200},
            {ItemIDs.silkScarf, 200},
            {ItemIDs.upgrade, 300},
            {ItemIDs.shellBell, 600},
            {ItemIDs.seaIncense, 200},
            {ItemIDs.laxIncense, 300},
            {ItemIDs.luckyPunch, 1},
            {ItemIDs.metalPowder, 1},
            {ItemIDs.thickClub, 50},
            {ItemIDs.leek, 20},
            {ItemIDs.redScarf, 10},
            {ItemIDs.blueScarf, 10},
            {ItemIDs.pinkScarf, 10},
            {ItemIDs.greenScarf, 10},
            {ItemIDs.yellowScarf, 10},
            {ItemIDs.wideLens, 150},
            {ItemIDs.muscleBand, 200},
            {ItemIDs.wiseGlasses, 200},
            {ItemIDs.expertBelt, 600},
            {ItemIDs.lightClay, 150},
            {ItemIDs.lifeOrb, 1000},
            {ItemIDs.powerHerb, 100},
            {ItemIDs.toxicOrb, 150},
            {ItemIDs.flameOrb, 150},
            {ItemIDs.quickPowder, 1},
            {ItemIDs.focusSash, 200},
            {ItemIDs.zoomLens, 150},
            {ItemIDs.metronome, 300},
            {ItemIDs.ironBall, 100},
            {ItemIDs.laggingTail, 100},
            {ItemIDs.destinyKnot, 150},
            {ItemIDs.blackSludge, 500},
            {ItemIDs.icyRock, 20},
            {ItemIDs.smoothRock, 20},
            {ItemIDs.heatRock, 20},
            {ItemIDs.dampRock, 20},
            {ItemIDs.gripClaw, 150},
            {ItemIDs.choiceScarf, 1000},
            {ItemIDs.stickyBarb, 150},
            {ItemIDs.powerBracer, 300},
            {ItemIDs.powerBelt, 300},
            {ItemIDs.powerLens, 300},
            {ItemIDs.powerBand, 300},
            {ItemIDs.powerAnklet, 300},
            {ItemIDs.powerWeight, 300},
            {ItemIDs.shedShell, 50},
            {ItemIDs.bigRoot, 150},
            {ItemIDs.choiceSpecs, 1000},
            {ItemIDs.flamePlate, 200},
            {ItemIDs.splashPlate, 200},
            {ItemIDs.zapPlate, 200},
            {ItemIDs.meadowPlate, 200},
            {ItemIDs.iciclePlate, 200},
            {ItemIDs.fistPlate, 200},
            {ItemIDs.toxicPlate, 200},
            {ItemIDs.earthPlate, 200},
            {ItemIDs.skyPlate, 200},
            {ItemIDs.mindPlate, 200},
            {ItemIDs.insectPlate, 200},
            {ItemIDs.stonePlate, 200},
            {ItemIDs.spookyPlate, 200},
            {ItemIDs.dracoPlate, 200},
            {ItemIDs.dreadPlate, 200},
            {ItemIDs.ironPlate, 200},
            {ItemIDs.oddIncense, 200},
            {ItemIDs.rockIncense, 200},
            {ItemIDs.fullIncense, 100},
            {ItemIDs.waveIncense, 200},
            {ItemIDs.roseIncense, 200},
            {ItemIDs.luckIncense, 1500},
            {ItemIDs.pureIncense, 100},
            {ItemIDs.protector, 300},
            {ItemIDs.electirizer, 300},
            {ItemIDs.magmarizer, 300},
            {ItemIDs.dubiousDisc, 300},
            {ItemIDs.reaperCloth, 300},
            {ItemIDs.razorClaw, 500},
            {ItemIDs.razorFang, 500},
            {ItemIDs.tm01, 300},
            {ItemIDs.tm02, 300},
            {ItemIDs.tm03, 300},
            {ItemIDs.tm04, 150},
            {ItemIDs.tm05, 100},
            {ItemIDs.tm06, 300},
            {ItemIDs.tm07, 200},
            {ItemIDs.tm08, 150},
            {ItemIDs.tm09, 200},
            {ItemIDs.tm10, 200},
            {ItemIDs.tm11, 200},
            {ItemIDs.tm12, 150},
            {ItemIDs.tm13, 300},
            {ItemIDs.tm14, 550},
            {ItemIDs.tm15, 750},
            {ItemIDs.tm16, 200},
            {ItemIDs.tm17, 200},
            {ItemIDs.tm18, 200},
            {ItemIDs.tm19, 300},
            {ItemIDs.tm20, 200},
            {ItemIDs.tm21, 100},
            {ItemIDs.tm22, 300},
            {ItemIDs.tm23, 300},
            {ItemIDs.tm24, 300},
            {ItemIDs.tm25, 550},
            {ItemIDs.tm26, 300},
            {ItemIDs.tm27, 100},
            {ItemIDs.tm28, 200},
            {ItemIDs.tm29, 300},
            {ItemIDs.tm30, 300},
            {ItemIDs.tm31, 300},
            {ItemIDs.tm32, 100},
            {ItemIDs.tm33, 200},
            {ItemIDs.tm34, 300},
            {ItemIDs.tm35, 300},
            {ItemIDs.tm36, 300},
            {ItemIDs.tm37, 200},
            {ItemIDs.tm38, 550},
            {ItemIDs.tm39, 200},
            {ItemIDs.tm40, 300},
            {ItemIDs.tm41, 150},
            {ItemIDs.tm42, 300},
            {ItemIDs.tm43, 200},
            {ItemIDs.tm44, 300},
            {ItemIDs.tm45, 300},
            {ItemIDs.tm46, 200},
            {ItemIDs.tm47, 300},
            {ItemIDs.tm48, 300},
            {ItemIDs.tm49, 150},
            {ItemIDs.tm50, 550},
            {ItemIDs.tm51, 200},
            {ItemIDs.tm52, 550},
            {ItemIDs.tm53, 300},
            {ItemIDs.tm54, 200},
            {ItemIDs.tm55, 300},
            {ItemIDs.tm56, 200},
            {ItemIDs.tm57, 300},
            {ItemIDs.tm58, 200},
            {ItemIDs.tm59, 300},
            {ItemIDs.tm60, 300},
            {ItemIDs.tm61, 200},
            {ItemIDs.tm62, 300},
            {ItemIDs.tm63, 200},
            {ItemIDs.tm64, 750},
            {ItemIDs.tm65, 300},
            {ItemIDs.tm66, 300},
            {ItemIDs.tm67, 100},
            {ItemIDs.tm68, 750},
            {ItemIDs.tm69, 150},
            {ItemIDs.tm70, 100},
            {ItemIDs.tm71, 300},
            {ItemIDs.tm72, 300},
            {ItemIDs.tm73, 200},
            {ItemIDs.tm74, 300},
            {ItemIDs.tm75, 150},
            {ItemIDs.tm76, 200},
            {ItemIDs.tm77, 150},
            {ItemIDs.tm78, 150},
            {ItemIDs.tm79, 300},
            {ItemIDs.tm80, 300},
            {ItemIDs.tm81, 300},
            {ItemIDs.tm82, 100},
            {ItemIDs.tm83, 200},
            {ItemIDs.tm84, 300},
            {ItemIDs.tm85, 300},
            {ItemIDs.tm86, 300},
            {ItemIDs.tm87, 150},
            {ItemIDs.tm88, 300},
            {ItemIDs.tm89, 300},
            {ItemIDs.tm90, 200},
            {ItemIDs.tm91, 300},
            {ItemIDs.tm92, 550},
            {ItemIDs.hm01, 0},
            {ItemIDs.hm02, 0},
            {ItemIDs.hm03, 0},
            {ItemIDs.hm04, 0},
            {ItemIDs.hm05, 0},
            {ItemIDs.hm06, 0},
            {ItemIDs.hm07, 0},
            {ItemIDs.hm08, 0},
            {ItemIDs.explorerKit, 0},
            {ItemIDs.lootSack, 0},
            {ItemIDs.ruleBook, 0},
            {ItemIDs.pokeRadar, 0},
            {ItemIDs.pointCard, 0},
            {ItemIDs.journal, 0},
            {ItemIDs.sealCase, 0},
            {ItemIDs.fashionCase, 0},
            {ItemIDs.sealBag, 0},
            {ItemIDs.palPad, 0},
            {ItemIDs.worksKey, 0},
            {ItemIDs.oldCharm, 0},
            {ItemIDs.galacticKey, 0},
            {ItemIDs.redChain, 0},
            {ItemIDs.townMap, 0},
            {ItemIDs.vsSeeker, 0},
            {ItemIDs.coinCase, 0},
            {ItemIDs.oldRod, 0},
            {ItemIDs.goodRod, 0},
            {ItemIDs.superRod, 0},
            {ItemIDs.sprayduck, 0},
            {ItemIDs.poffinCase, 0},
            {ItemIDs.bike, 0},
            {ItemIDs.suiteKey, 0},
            {ItemIDs.oaksLetter, 0},
            {ItemIDs.lunarWing, 0},
            {ItemIDs.memberCard, 0},
            {ItemIDs.azureFlute, 0},
            {ItemIDs.ssTicketJohto, 0},
            {ItemIDs.contestPass, 0},
            {ItemIDs.magmaStone, 0},
            {ItemIDs.parcelSinnoh, 0},
            {ItemIDs.coupon1, 0},
            {ItemIDs.coupon2, 0},
            {ItemIDs.coupon3, 0},
            {ItemIDs.storageKeySinnoh, 0},
            {ItemIDs.secretPotion, 0},
            {ItemIDs.vsRecorder, 0},
            {ItemIDs.gracidea, 0},
            {ItemIDs.secretKeySinnoh, 0},
            {ItemIDs.apricornBox, 0},
            {ItemIDs.unownReport, 0},
            {ItemIDs.berryPots, 0},
            {ItemIDs.dowsingMachine, 0},
            {ItemIDs.blueCard, 0},
            {ItemIDs.slowpokeTail, 0},
            {ItemIDs.clearBell, 0},
            {ItemIDs.cardKeyJohto, 0},
            {ItemIDs.basementKeyJohto, 0},
            {ItemIDs.squirtBottle, 0},
            {ItemIDs.redScale, 0},
            {ItemIDs.lostItem, 0},
            {ItemIDs.pass, 0},
            {ItemIDs.machinePart, 0},
            {ItemIDs.silverWing, 0},
            {ItemIDs.rainbowWing, 0},
            {ItemIDs.mysteryEgg, 0},
            {ItemIDs.redApricorn, 2},
            {ItemIDs.blueApricorn, 2},
            {ItemIDs.yellowApricorn, 2},
            {ItemIDs.greenApricorn, 2},
            {ItemIDs.pinkApricorn, 2},
            {ItemIDs.whiteApricorn, 2},
            {ItemIDs.blackApricorn, 2},
            {ItemIDs.fastBall, 30},
            {ItemIDs.levelBall, 30},
            {ItemIDs.lureBall, 30},
            {ItemIDs.heavyBall, 30},
            {ItemIDs.loveBall, 30},
            {ItemIDs.friendBall, 30},
            {ItemIDs.moonBall, 30},
            {ItemIDs.sportBall, 30},
            {ItemIDs.parkBall, 0},
            {ItemIDs.photoAlbum, 0},
            {ItemIDs.gbSounds, 0},
            {ItemIDs.tidalBell, 0},
            {ItemIDs.rageCandyBar, 0},
            {ItemIDs.dataCard01, 0},
            {ItemIDs.dataCard02, 0},
            {ItemIDs.dataCard03, 0},
            {ItemIDs.dataCard04, 0},
            {ItemIDs.dataCard05, 0},
            {ItemIDs.dataCard06, 0},
            {ItemIDs.dataCard07, 0},
            {ItemIDs.dataCard08, 0},
            {ItemIDs.dataCard09, 0},
            {ItemIDs.dataCard10, 0},
            {ItemIDs.dataCard11, 0},
            {ItemIDs.dataCard12, 0},
            {ItemIDs.dataCard13, 0},
            {ItemIDs.dataCard14, 0},
            {ItemIDs.dataCard15, 0},
            {ItemIDs.dataCard16, 0},
            {ItemIDs.dataCard17, 0},
            {ItemIDs.dataCard18, 0},
            {ItemIDs.dataCard19, 0},
            {ItemIDs.dataCard20, 0},
            {ItemIDs.dataCard21, 0},
            {ItemIDs.dataCard22, 0},
            {ItemIDs.dataCard23, 0},
            {ItemIDs.dataCard24, 0},
            {ItemIDs.dataCard25, 0},
            {ItemIDs.dataCard26, 0},
            {ItemIDs.dataCard27, 0},
            {ItemIDs.jadeOrb, 0},
            {ItemIDs.lockCapsule, 0},
            {ItemIDs.redOrb, 0},
            {ItemIDs.blueOrb, 0},
            {ItemIDs.enigmaStone, 0},
    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

    public static final Type[] typeTable = constructTypeTable();

    private static Type[] constructTypeTable() {
        Type[] table = new Type[256];
        table[0x00] = Type.NORMAL;
        table[0x01] = Type.FIGHTING;
        table[0x02] = Type.FLYING;
        table[0x03] = Type.POISON;
        table[0x04] = Type.GROUND;
        table[0x05] = Type.ROCK;
        table[0x06] = Type.BUG;
        table[0x07] = Type.GHOST;
        table[0x08] = Type.STEEL;
        table[0x0A] = Type.FIRE;
        table[0x0B] = Type.WATER;
        table[0x0C] = Type.GRASS;
        table[0x0D] = Type.ELECTRIC;
        table[0x0E] = Type.PSYCHIC;
        table[0x0F] = Type.ICE;
        table[0x10] = Type.DRAGON;
        table[0x11] = Type.DARK;
        return table;
    }

    public static byte typeToByte(Type type) {
        if (type == null) {
            return 0x09; // ???-type
        }
        switch (type) {
        case NORMAL:
            return 0x00;
        case FIGHTING:
            return 0x01;
        case FLYING:
            return 0x02;
        case POISON:
            return 0x03;
        case GROUND:
            return 0x04;
        case ROCK:
            return 0x05;
        case BUG:
            return 0x06;
        case GHOST:
            return 0x07;
        case FIRE:
            return 0x0A;
        case WATER:
            return 0x0B;
        case GRASS:
            return 0x0C;
        case ELECTRIC:
            return 0x0D;
        case PSYCHIC:
            return 0x0E;
        case ICE:
            return 0x0F;
        case DRAGON:
            return 0x10;
        case STEEL:
            return 0x08;
        case DARK:
            return 0x11;
        default:
            return 0; // normal by default
        }
    }

    public static final byte typeTableTerminator = (byte) 0xFF, typeTableForesightTerminator = (byte) 0xFE;

    public static final int nonNeutralEffectivenessCount = 110;

    private static final EvolutionType[] evolutionTypeTable = new EvolutionType[] {
            EvolutionType.HAPPINESS, EvolutionType.HAPPINESS_DAY, EvolutionType.HAPPINESS_NIGHT, EvolutionType.LEVEL,
            EvolutionType.TRADE, EvolutionType.TRADE_ITEM, EvolutionType.STONE, EvolutionType.LEVEL_ATTACK_HIGHER,
            EvolutionType.LEVEL_ATK_DEF_SAME, EvolutionType.LEVEL_DEFENSE_HIGHER, EvolutionType.LEVEL_LOW_PV,
            EvolutionType.LEVEL_HIGH_PV, EvolutionType.LEVEL_CREATE_EXTRA, EvolutionType.LEVEL_IS_EXTRA,
            EvolutionType.LEVEL_HIGH_BEAUTY, EvolutionType.STONE_MALE_ONLY, EvolutionType.STONE_FEMALE_ONLY,
            EvolutionType.LEVEL_ITEM_DAY, EvolutionType.LEVEL_ITEM_NIGHT, EvolutionType.LEVEL_WITH_MOVE,
            EvolutionType.LEVEL_WITH_OTHER, EvolutionType.LEVEL_MALE_ONLY, EvolutionType.LEVEL_FEMALE_ONLY,
            EvolutionType.LEVEL_ELECTRIFIED_AREA, EvolutionType.LEVEL_MOSS_ROCK, EvolutionType.LEVEL_ICY_ROCK
    };

    public static int evolutionTypeToIndex(EvolutionType evolutionType) {
        for (int i = 0; i < evolutionTypeTable.length; i++) {
            if (evolutionType == evolutionTypeTable[i]) {
                return i + 1;
            }
        }
        return -1;
    }

    public static EvolutionType evolutionTypeFromIndex(int index) {
        if (index == -1) {
            return EvolutionType.NONE;
        }
        return evolutionTypeTable[index - 1];
    }

    public static int getFormeCount(int romType) {
        if (romType == Type_DP) {
            return dpFormeCount;
        } else if (romType == Type_Plat || romType == Type_HGSS) {
            return platHgSsFormeCount;
        }
        return 0;
    }

    private static Map<Integer,FormeInfo> setupFormeMappings() {
        Map<Integer,FormeInfo> formeMappings = new TreeMap<>();

        putFormeMappings(formeMappings, SpeciesIDs.deoxys,
                SpeciesIDs.Gen4Formes.deoxysA, SpeciesIDs.Gen4Formes.deoxysD, SpeciesIDs.Gen4Formes.deoxysS);
        putFormeMappings(formeMappings, SpeciesIDs.wormadam,
                SpeciesIDs.Gen4Formes.wormadamS, SpeciesIDs.Gen4Formes.wormadamT);
        putFormeMappings(formeMappings, SpeciesIDs.giratina,
                SpeciesIDs.Gen4Formes.giratinaO);
        putFormeMappings(formeMappings, SpeciesIDs.shaymin,
                SpeciesIDs.Gen4Formes.shayminS);
        putFormeMappings(formeMappings, SpeciesIDs.rotom,
                SpeciesIDs.Gen4Formes.rotomH, SpeciesIDs.Gen4Formes.rotomW, SpeciesIDs.Gen4Formes.rotomFr,
                SpeciesIDs.Gen4Formes.rotomFa, SpeciesIDs.Gen4Formes.rotomM);

        return formeMappings;
    }

    private static void putFormeMappings(Map<Integer, FormeInfo> formeMappings, int baseFormeID, int... altFormeIDs) {
        for (int i = 0; i < altFormeIDs.length; i++) {
            formeMappings.put(altFormeIDs[i] + formeOffset, new FormeInfo(baseFormeID,i + 1));
        }
    }

    private static Map<Integer,Integer> setupCosmeticForms() {
        Map<Integer,Integer> cosmeticForms = new TreeMap<>();

        cosmeticForms.put(SpeciesIDs.unown, 28);
        cosmeticForms.put(SpeciesIDs.burmy, 3);
        cosmeticForms.put(SpeciesIDs.shellos, 2);
        cosmeticForms.put(SpeciesIDs.gastrodon, 2);
        return cosmeticForms;
    }

    private static Map<Integer,Map<Integer,String>> setupFormeSuffixesByBaseForme() {
        Map<Integer,Map<Integer,String>> map = new HashMap<>();

        putFormSuffixes(map, SpeciesIDs.deoxys, "-Attack", "-Defense", "-Speed");

        putFormSuffixes(map, SpeciesIDs.wormadam, "-Sandy", "-Trash");
        putFormSuffixes(map, SpeciesIDs.rotom, "-Heat", "-Wash", "-Frost", "-Fan", "-Mow");
        putFormSuffixes(map, SpeciesIDs.giratina, "-Origin");
        putFormSuffixes(map, SpeciesIDs.shaymin, "-Sky");

        return map;
    }

    private static void putFormSuffixes(Map<Integer, Map<Integer, String>> map, int species, String... suffixes) {
        Map<Integer, String> speciesMap = new HashMap<>();
        for (int i = 0; i < suffixes.length; i++) {
            speciesMap.put(i + 1, suffixes[i]);
        }
        map.put(species, speciesMap);
    }

    private static Map<Integer,String> setupDummyFormeSuffixes() {
        Map<Integer,String> m = new HashMap<>();
        m.put(0,"");
        return m;
    }

    private static Map<Integer,Map<Integer,Integer>> setupAbsolutePokeNumsByBaseForme() {
        Map<Integer,Map<Integer,Integer>> map = new HashMap<>();

        Map<Integer,Integer> deoxysMap = new HashMap<>();
        deoxysMap.put(1, SpeciesIDs.Gen4Formes.deoxysA);
        deoxysMap.put(2, SpeciesIDs.Gen4Formes.deoxysD);
        deoxysMap.put(3, SpeciesIDs.Gen4Formes.deoxysS);
        map.put(SpeciesIDs.deoxys, deoxysMap);

        Map<Integer,Integer> wormadamMap = new HashMap<>();
        wormadamMap.put(1, SpeciesIDs.Gen4Formes.wormadamS);
        wormadamMap.put(2, SpeciesIDs.Gen4Formes.wormadamT);
        map.put(SpeciesIDs.wormadam, wormadamMap);

        Map<Integer,Integer> giratinaMap = new HashMap<>();
        giratinaMap.put(1, SpeciesIDs.Gen4Formes.giratinaO);
        map.put(SpeciesIDs.giratina, giratinaMap);

        Map<Integer,Integer> shayminMap = new HashMap<>();
        shayminMap.put(1, SpeciesIDs.Gen4Formes.shayminS);
        map.put(SpeciesIDs.shaymin, shayminMap);

        Map<Integer,Integer> rotomMap = new HashMap<>();
        rotomMap.put(1, SpeciesIDs.Gen4Formes.rotomH);
        rotomMap.put(2, SpeciesIDs.Gen4Formes.rotomW);
        rotomMap.put(3, SpeciesIDs.Gen4Formes.rotomFr);
        rotomMap.put(4, SpeciesIDs.Gen4Formes.rotomFa);
        rotomMap.put(5, SpeciesIDs.Gen4Formes.rotomM);
        map.put(SpeciesIDs.rotom, rotomMap);

        return map;
    }

    private static Map<Integer,Integer> setupDummyAbsolutePokeNums() {
        Map<Integer,Integer> m = new HashMap<>();
        m.put(255,0);
        return m;
    }

    public static void tagTrainersDP(List<Trainer> trs) {
        // Gym Trainers
        tag(trs, "GYM1", 0xf4, 0xf5);
        tag(trs, "GYM2", 0x144, 0x103, 0x104, 0x15C);
        tag(trs, "GYM3", 0x135, 0x136, 0x137, 0x138);
        tag(trs, "GYM4", 0x1f1, 0x1f2, 0x191, 0x153, 0x125, 0x1E3);
        tag(trs, "GYM5", 0x165, 0x145, 0x10a, 0x14a, 0x154, 0x157, 0x118, 0x11c);
        tag(trs, "GYM6", 0x13a, 0x100, 0x101, 0x117, 0x16f, 0xe8, 0x11b);
        tag(trs, "GYM7", 0x10c, 0x10d, 0x10e, 0x10f, 0x33b, 0x33c);
        tag(trs, "GYM8", 0x158, 0x155, 0x12d, 0x12e, 0x12f, 0x11d, 0x119);

        // Gym Leaders
        tag(trs, 0xf6, "GYM1-LEADER");
        tag(trs, 0x13b, "GYM2-LEADER");
        tag(trs, 0x13d, "GYM3-LEADER"); // Maylene
        tag(trs, 0x13c, "GYM4-LEADER"); // Wake
        tag(trs, 0x13e, "GYM5-LEADER"); // Fantina
        tag(trs, 0xfa, "GYM6-LEADER"); // Byron
        tag(trs, 0x13f, "GYM7-LEADER"); // Candice
        tag(trs, 0x140, "GYM8-LEADER"); // Volkner

        // Elite 4
        tag(trs, 0x105, "ELITE1");
        tag(trs, 0x106, "ELITE2");
        tag(trs, 0x107, "ELITE3");
        tag(trs, 0x108, "ELITE4");
        tag(trs, 0x10b, "CHAMPION");

        // Rival battles (8)
        tagRivalConsecutive(trs, "RIVAL1", 0xf8);
        tagRivalConsecutive(trs, "RIVAL2", 0x1d7);
        tagRivalConsecutive(trs, "RIVAL3", 0x1da);
        tagRivalConsecutive(trs, "RIVAL4", 0x1dd);
        // Tag battle is not following ze usual format
        tag(trs, 0x26b, "RIVAL5-0");
        tag(trs, 0x26c, "RIVAL5-1");
        tag(trs, 0x25f, "RIVAL5-2");
        // Back to normal
        tagRivalConsecutive(trs, "RIVAL6", 0x1e0);
        tagRivalConsecutive(trs, "RIVAL7", 0x346);
        tagRivalConsecutive(trs, "RIVAL8", 0x349);

        // Themed
        tag(trs, "THEMED:CYRUS-LEADER", 0x193, 0x194);
        tag(trs, "THEMED:MARS-STRONG", 0x127, 0x195, 0x210);
        tag(trs, "THEMED:JUPITER-STRONG", 0x196, 0x197);
        tag(trs, "THEMED:SATURN-STRONG", 0x198, 0x199);

        // Lucas & Dawn tag battles
        tagFriendConsecutive(trs, "FRIEND1", 0x265);
        tagFriendConsecutive(trs, "FRIEND1", 0x268);
        tagFriendConsecutive2(trs, "FRIEND2", 0x26D);
        tagFriendConsecutive2(trs, "FRIEND2", 0x270);

    }

    public static void tagTrainersPt(List<Trainer> trs) {
        // Gym Trainers
        tag(trs, "GYM1", 0xf4, 0xf5);
        tag(trs, "GYM2", 0x144, 0x103, 0x104, 0x15C);
        tag(trs, "GYM3", 0x165, 0x145, 0x154, 0x157, 0x118, 0x11c);
        tag(trs, "GYM4", 0x135, 0x136, 0x137, 0x138);
        tag(trs, "GYM5", 0x1f1, 0x1f2, 0x191, 0x153, 0x125, 0x1E3);
        tag(trs, "GYM6", 0x13a, 0x100, 0x101, 0x117, 0x16f, 0xe8, 0x11b);
        tag(trs, "GYM7", 0x10c, 0x10d, 0x10e, 0x10f, 0x33b, 0x33c);
        tag(trs, "GYM8", 0x158, 0x155, 0x12d, 0x12e, 0x12f, 0x11d, 0x119, 0x14b);

        // Gym Leaders
        tag(trs, 0xf6, "GYM1-LEADER");
        tag(trs, 0x13b, "GYM2-LEADER");
        tag(trs, 0x13e, "GYM3-LEADER"); // Fantina
        tag(trs, 0x13d, "GYM4-LEADER"); // Maylene
        tag(trs, 0x13c, "GYM5-LEADER"); // Wake
        tag(trs, 0xfa, "GYM6-LEADER"); // Byron
        tag(trs, 0x13f, "GYM7-LEADER"); // Candice
        tag(trs, 0x140, "GYM8-LEADER"); // Volkner

        // Elite 4
        tag(trs, 0x105, "ELITE1");
        tag(trs, 0x106, "ELITE2");
        tag(trs, 0x107, "ELITE3");
        tag(trs, 0x108, "ELITE4");
        tag(trs, 0x10b, "CHAMPION");

        // Rival battles (10)
        tagRivalConsecutive(trs, "RIVAL1", 0x353);
        tagRivalConsecutive(trs, "RIVAL2", 0xf8);
        tagRivalConsecutive(trs, "RIVAL3", 0x1d7);
        tagRivalConsecutive(trs, "RIVAL4", 0x1da);
        tagRivalConsecutive(trs, "RIVAL5", 0x1dd);
        // Tag battle is not following ze usual format
        tag(trs, 0x26b, "RIVAL6-0");
        tag(trs, 0x26c, "RIVAL6-1");
        tag(trs, 0x25f, "RIVAL6-2");
        // Back to normal
        tagRivalConsecutive(trs, "RIVAL7", 0x1e0);
        tagRivalConsecutive(trs, "RIVAL8", 0x346);
        tagRivalConsecutive(trs, "RIVAL9", 0x349);
        tagRivalConsecutive(trs, "RIVAL10", 0x368);

        // Battleground Gym Leaders
        tag(trs, 0x35A, "GYM1");
        tag(trs, 0x359, "GYM2");
        tag(trs, 0x35C, "GYM3");
        tag(trs, 0x356, "GYM4");
        tag(trs, 0x35B, "GYM5");
        tag(trs, 0x358, "GYM6");
        tag(trs, 0x355, "GYM7");
        tag(trs, 0x357, "GYM8");

        // Match vs Volkner and Flint in Battle Frontier
        tag(trs, 0x399, "GYM8");
        tag(trs, 0x39A, "ELITE3");

        // E4 rematch
        tag(trs, 0x362, "ELITE1");
        tag(trs, 0x363, "ELITE2");
        tag(trs, 0x364, "ELITE3");
        tag(trs, 0x365, "ELITE4");
        tag(trs, 0x366, "CHAMPION");

        // Themed
        tag(trs, "THEMED:CYRUS-LEADER", 0x391, 0x193, 0x194);
        tag(trs, "THEMED:MARS-STRONG", 0x127, 0x195, 0x210, 0x39e);
        tag(trs, "THEMED:JUPITER-STRONG", 0x196, 0x197, 0x39f);
        tag(trs, "THEMED:SATURN-STRONG", 0x198, 0x199);

        // Lucas & Dawn tag battles
        tagFriendConsecutive(trs, "FRIEND1", 0x265);
        tagFriendConsecutive(trs, "FRIEND1", 0x268);
        tagFriendConsecutive2(trs, "FRIEND2", 0x26D);
        tagFriendConsecutive2(trs, "FRIEND2", 0x270);

    }

    public static void tagTrainersHGSS(List<Trainer> trs) {
        // Gym Trainers
        tag(trs, "GYM1", 0x32, 0x1D);
        tag(trs, "GYM2", 0x43, 0x44, 0x45, 0x0a);
        tag(trs, "GYM3", 0x05, 0x46, 0x47, 0x16);
        tag(trs, "GYM4", 0x1ed, 0x1ee, 0x59, 0x2e);
        tag(trs, "GYM5", 0x9c, 0x9d, 0x9f, 0xfb);
        tag(trs, "GYM7", 0x1e0, 0x1e1, 0x1e2, 0x1e3, 0x1e4);
        tag(trs, "GYM8", 0x6e, 0x6f, 0x70, 0x75, 0x77);

        tag(trs, "GYM9", 0x134, 0x2ad);
        tag(trs, "GYM10", 0x2a4, 0x2a5, 0x2a6, 0x129, 0x12a);
        tag(trs, "GYM11", 0x18c, 0xe8, 0x151);
        tag(trs, "GYM12", 0x150, 0x146, 0x164, 0x15a);
        tag(trs, "GYM13", 0x53, 0x54, 0xb7, 0x88);
        tag(trs, "GYM14", 0x170, 0x171, 0xe6, 0x19f);
        tag(trs, "GYM15", 0x2b1, 0x2b2, 0x2b3, 0x2b4, 0x2b5, 0x2b6);
        tag(trs, "GYM16", 0x2a9, 0x2aa, 0x2ab, 0x2ac);

        // Gym Leaders
        tag(trs, 0x14, "GYM1-LEADER");
        tag(trs, 0x15, "GYM2-LEADER");
        tag(trs, 0x1e, "GYM3-LEADER");
        tag(trs, 0x1f, "GYM4-LEADER");
        tag(trs, 0x22, "GYM5-LEADER");
        tag(trs, 0x21, "GYM6-LEADER");
        tag(trs, 0x20, "GYM7-LEADER");
        tag(trs, 0x23, "GYM8-LEADER");

        tag(trs, 0xFD, "GYM9-LEADER");
        tag(trs, 0xFE, "GYM10-LEADER");
        tag(trs, 0xFF, "GYM11-LEADER");
        tag(trs, 0x100, "GYM12-LEADER");
        tag(trs, 0x101, "GYM13-LEADER");
        tag(trs, 0x102, "GYM14-LEADER");
        tag(trs, 0x103, "GYM15-LEADER");
        tag(trs, 0x105, "GYM16-LEADER");

        // Elite 4
        tag(trs, 0xf5, "ELITE1");
        tag(trs, 0xf7, "ELITE2");
        tag(trs, 0x1a2, "ELITE3");
        tag(trs, 0xf6, "ELITE4");
        tag(trs, 0xf4, "CHAMPION");

        // Red
        tag(trs, 0x104, "UBER");

        // Gym Rematches
        tag(trs, 0x2c8, "GYM1-LEADER");
        tag(trs, 0x2c9, "GYM2-LEADER");
        tag(trs, 0x2ca, "GYM3-LEADER");
        tag(trs, 0x2cb, "GYM4-LEADER");
        tag(trs, 0x2ce, "GYM5-LEADER");
        tag(trs, 0x2cd, "GYM6-LEADER");
        tag(trs, 0x2cc, "GYM7-LEADER");
        tag(trs, 0x2cf, "GYM8-LEADER");

        tag(trs, 0x2d0, "GYM9-LEADER");
        tag(trs, 0x2d1, "GYM10-LEADER");
        tag(trs, 0x2d2, "GYM11-LEADER");
        tag(trs, 0x2d3, "GYM12-LEADER");
        tag(trs, 0x2d4, "GYM13-LEADER");
        tag(trs, 0x2d5, "GYM14-LEADER");
        tag(trs, 0x2d6, "GYM15-LEADER");
        tag(trs, 0x2d7, "GYM16-LEADER");

        // Elite 4 Rematch
        tag(trs, 0x2be, "ELITE1");
        tag(trs, 0x2bf, "ELITE2");
        tag(trs, 0x2c0, "ELITE3");
        tag(trs, 0x2c1, "ELITE4");
        tag(trs, 0x2bd, "CHAMPION");

        // Rival Battles
        tagRivalConsecutive(trs, "RIVAL1", 0x1F0);

        tag(trs, 0x10a, "RIVAL2-0");
        tag(trs, 0x10d, "RIVAL2-1");
        tag(trs, 0x1, "RIVAL2-2");

        tag(trs, 0x10B, "RIVAL3-0");
        tag(trs, 0x10E, "RIVAL3-1");
        tag(trs, 0x107, "RIVAL3-2");

        tag(trs, 0x121, "RIVAL4-0");
        tag(trs, 0x10f, "RIVAL4-1");
        tag(trs, 0x120, "RIVAL4-2");

        tag(trs, 0x10C, "RIVAL5-0");
        tag(trs, 0x110, "RIVAL5-1");
        tag(trs, 0x108, "RIVAL5-2");

        tagRivalConsecutive(trs, "RIVAL6", 0x11e);
        tagRivalConsecutive(trs, "RIVAL7", 0x2e0); // dragons den tag battle
        tagRivalConsecutive(trs, "RIVAL8", 0x1EA);

        // Clair & Lance match in Dragons Den
        tag(trs, 0x2DE, "GYM8");
        tag(trs, 0x2DD, "CHAMPION");

        tag(trs, 0xa0, "KIMONO1-STRONG");
        tag(trs, 0xa1, "KIMONO2-STRONG");
        tag(trs, 0xa2, "KIMONO3-STRONG");
        tag(trs, 0xa3, "KIMONO4-STRONG");
        tag(trs, 0xa4, "KIMONO5-STRONG");

        // Themed
        tag(trs, "THEMED:ARIANA-STRONG", 0x1df, 0x1de);
        tag(trs, "THEMED:PETREL-STRONG", 0x1e8, 0x1e7);
        tag(trs, "THEMED:PROTON-STRONG", 0x1e6, 0x2c2);
        tag(trs, "THEMED:SPROUTTOWER", 0x2b, 0x33, 0x34, 0x35, 0x36, 0x37, 0x122);

        tag(trs,"LEADER",485); // Archer
    }

    private static void tag(List<Trainer> allTrainers, int number, String tag) {
        allTrainers.get(number - 1).tag = tag;
    }

    private static void tag(List<Trainer> allTrainers, String tag, int... numbers) {
        for (int num : numbers) {
            allTrainers.get(num - 1).tag = tag;
        }
    }

    private static void tagRivalConsecutive(List<Trainer> allTrainers, String tag, int offsetFire) {
        allTrainers.get(offsetFire - 1).tag = tag + "-0";
        allTrainers.get(offsetFire).tag = tag + "-1";
        allTrainers.get(offsetFire - 2).tag = tag + "-2";

    }

    private static void tagFriendConsecutive(List<Trainer> allTrainers, String tag, int offsetGrass) {
        allTrainers.get(offsetGrass - 1).tag = tag + "-1";
        allTrainers.get(offsetGrass).tag = tag + "-2";
        allTrainers.get(offsetGrass + 1).tag = tag + "-0";

    }

    private static void tagFriendConsecutive2(List<Trainer> allTrainers, String tag, int offsetWater) {
        allTrainers.get(offsetWater - 1).tag = tag + "-0";
        allTrainers.get(offsetWater).tag = tag + "-1";
        allTrainers.get(offsetWater + 1).tag = tag + "-2";

    }

    public static final HashMap<String, Type> gymAndEliteThemesDP = setupGymAndEliteThemesDP();

    private static HashMap<String, Type> setupGymAndEliteThemesDP() {
        HashMap<String, Type> themeMap = new HashMap<>();
        //no theme for Cynthia
        themeMap.put("ELITE1", Type.BUG); //Aaron
        themeMap.put("ELITE2", Type.GROUND); //Bertha
        themeMap.put("ELITE3", Type.FIRE); //Flint
        themeMap.put("ELITE4", Type.PSYCHIC); //Lucian
        themeMap.put("GYM1", Type.ROCK); //Roark
        themeMap.put("GYM2", Type.GRASS); //Gardenia
        themeMap.put("GYM3", Type.FIGHTING); //Maylene
        themeMap.put("GYM4", Type.WATER); //Wake
        themeMap.put("GYM5", Type.GHOST); //Fantina
        themeMap.put("GYM6", Type.STEEL); //Byron
        themeMap.put("GYM7", Type.ICE); //Candice
        themeMap.put("GYM8", Type.ELECTRIC); //Volkner
        return themeMap;
    }

    public static final HashMap<String, Type> gymAndEliteThemesPt = setupGymAndEliteThemesPt();

    private static HashMap<String, Type> setupGymAndEliteThemesPt() {
        HashMap<String, Type> themeMap = new HashMap<>();
        //no theme for Cynthia
        themeMap.put("ELITE1", Type.BUG); //Aaron
        themeMap.put("ELITE2", Type.GROUND); //Bertha
        themeMap.put("ELITE3", Type.FIRE); //Flint
        themeMap.put("ELITE4", Type.PSYCHIC); //Lucian
        themeMap.put("GYM1", Type.ROCK); //Roark
        themeMap.put("GYM2", Type.GRASS); //Gardenia
        themeMap.put("GYM3", Type.GHOST); //Fantina
        themeMap.put("GYM4", Type.FIGHTING); // Maylene
        themeMap.put("GYM5", Type.WATER); //Wake
        themeMap.put("GYM6", Type.STEEL); //Byron
        themeMap.put("GYM7", Type.ICE); //Candice
        themeMap.put("GYM8", Type.ELECTRIC); //Volkner
        return themeMap;
    }

    public static final HashMap<String, Type> gymAndEliteThemesHGSS = setupGymAndEliteThemesHGSS();

    private static HashMap<String, Type> setupGymAndEliteThemesHGSS() {
        HashMap<String, Type> themeMap = new HashMap<>();
        themeMap.put("CHAMPION", Type.DRAGON); //Lance
        themeMap.put("ELITE1", Type.PSYCHIC); //Will
        themeMap.put("ELITE2", Type.POISON); //Koga
        themeMap.put("ELITE3", Type.FIGHTING); //Bruno
        themeMap.put("ELITE4", Type.DARK); //Karen
        themeMap.put("GYM1", Type.FLYING); //Falkner
        themeMap.put("GYM2", Type.BUG); //Bugsy
        themeMap.put("GYM3", Type.NORMAL); //Whitney
        themeMap.put("GYM4", Type.GHOST); //Morty
        themeMap.put("GYM5", Type.FIGHTING); //Chuck
        themeMap.put("GYM6", Type.STEEL); //Jasmine
        themeMap.put("GYM7", Type.ICE); //Pryce
        themeMap.put("GYM8", Type.DRAGON); //Clair
        themeMap.put("GYM9", Type.ROCK); //Brock
        themeMap.put("GYM10", Type.WATER); //Misty
        themeMap.put("GYM11", Type.ELECTRIC); //Lt. Surge
        themeMap.put("GYM12", Type.GRASS); //Erika
        themeMap.put("GYM13", Type.POISON); //Janine
        themeMap.put("GYM14", Type.PSYCHIC); //Sabrina
        themeMap.put("GYM15", Type.FIRE); //Blaine
        //Blue has no specialty and thus is not included.
        return themeMap;
    }

    public static void setMultiBattleStatusDP(List<Trainer> trs) {
        // 407 + 528: Commander Mars and Commander Jupiter Multi Battle on Spear Pillar
        // 414 + 415: Galactic Grunts in Jubilife City
        // 419 + 426: Galactic Grunts in Lake Verity
        // 420 + 427: Galactic Grunts in Lake Verity
        // 521 + 527: Galactic Grunts on Spear Pillar
        // 561 + 590: Double Battle with Dragon Tamer Drake and Black Belt Jarrett
        // 835 + 836: Galactic Grunts in Iron Island
        // 848 + 849: Galactic Grunts in Veilstone City
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 414, 415, 419, 420, 426, 427, 521, 527,
                528, 561, 590, 835, 836, 848, 849
        );

        // 34 + 35: Potential Double Battle with Camper Anthony and Picnicker Lauren
        // 82 + 83: Potential Double Battle with Rich Boy Jason and Lady Melissa
        // 84 + 85: Potential Double Battle with Gentleman Jeremy and Socialite Reina
        // 95 + 96: Potential Double Battle with PKMN Ranger Jeffrey and PKMN Ranger Allison
        // 104 + 106: Potential Double Battle with Swimmer Evan and Swimmer Mary
        // 160 + 494: Potential Double Battle with Swimmer Erik and Swimmer Claire
        // 186 + 191: Potential Double Battle with Swimmer Colton and Swimmer Paige
        // 201 + 204: Potential Double Battle with Bug Catcher Jack and Lass Briana
        // 202 + 203: Potential Double Battle with Bug Catcher Phillip and Bug Catcher Donald
        // 205 + 206: Potential Double Battle with Psychic Elijah and Psychic Lindsey
        // 278 + 287: Potential Double Battle with Ace Trainer Maya and Ace Trainer Dennis
        // 337 + 359: Potential Double Battle with Sailor Marc and Tuber Conner
        // 358 + 360: Potential Double Battle with Tuber Trenton and Tuber Mariel
        // 372 + 445: Potential Double Battle with Battle Girl Tyler and Black Belt Kendal
        // 373 + 386: Potential Double Battle with Bird Keeper Autumn and Dragon Tamer Joe
        // 379 + 459: Potential Double Battle with Camper Diego and Picnicker Ana
        // 383 + 443: Potential Double Battle with Collector Terry and Ruin Maniac Gerald
        // 388 + 392: Potential Double Battle with Ace Trainer Jonah and Ace Trainer Brenda
        // 389 + 393: Potential Double Battle with Ace Trainer Micah and Ace Trainer Brandi
        // 390 + 394: Potential Double Battle with Ace Trainer Arthur and Ace Trainer Clarice
        // 395 + 398: Potential Double Battle with Psychic Kody and Psychic Rachael
        // 396 + 399: Potential Double Battle with Psychic Landon and Psychic Desiree
        // 397 + 400: Potential Double Battle with Psychic Deandre and Psychic Kendra
        // 446 + 499: Potential Double Battle with Black Belt Eddie and Veteran Terrell
        // 447 + 500: Potential Double Battle with Black Belt Willie and Veteran Brenden
        // 450 + 496: Potential Double Battle with Lass Cassidy and Youngster Wayne
        // 452 + 453: Potential Double Battle with Hiker Damon and Hiker Maurice
        // 454 + 455: Potential Double Battle with Hiker Reginald and Hiker Lorenzo
        // 505 + 506: Potential Double Battle with Worker Brendon and Worker Quentin
        // 555 + 560: Potential Double Battle with Bird Keeper Geneva and Dragon Tamer Stanley
        // 556 + 589: Potential Double Battle with Bird Keeper Krystal and Black Belt Ray
        // 562 + 606: Potential Double Battle with Dragon Tamer Kenny and Veteran Harlan
        // 566 + 575: Potential Double Battle with Ace Trainer Felix and Ace Trainer Dana
        // 569 + 579: Potential Double Battle with Ace Trainer Keenan and Ace Trainer Kassandra
        // 570 + 580: Potential Double Battle with Ace Trainer Stefan and Ace Trainer Jasmin
        // 571 + 581: Potential Double Battle with Ace Trainer Skylar and Ace Trainer Natasha
        // 572 + 582: Potential Double Battle with Ace Trainer Abel and Ace Trainer Monique
        // 584 + 586: Potential Double Battle with Psychic Sterling and Psychic Chelsey
        // 591 + 596: Potential Double Battle with PKMN Ranger Kyler and PKMN Ranger Krista
        // 594 + 554/585: Potential Double Battle with PKMN Ranger Ashlee and either Bird Keeper Audrey or Psychic Daisy
        // 599 + 602: Potential Double Battle with Swimmer Sam and Swimmer Sophia
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 34, 35, 82, 83, 84, 85, 95, 96, 104,
                106, 160, 186, 191, 201, 202, 203, 204, 205, 206, 278, 287, 337, 358, 359, 360, 372, 373, 379, 383, 386,
                388, 389, 390, 392, 393, 394, 395, 396, 397, 398, 399, 400, 443, 445, 446, 447, 450, 452, 453, 454, 455,
                459, 494, 496, 499, 500, 505, 506, 554, 555, 556, 560, 562, 566, 569, 570, 571, 572, 575, 579, 580, 581,
                582, 584, 585, 586, 589, 591, 594, 596, 599, 602, 606
        );
    }

    public static void setMultiBattleStatusPt(List<Trainer> trs) {
        // In addition to every single trainer listed in setCouldBeMultiBattleDP...
        // 921 + 922: Elite Four Flint and Leader Volkner Multi Battle in the Fight Area
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 414, 415, 419, 420, 426, 427, 521, 527,
                528, 561, 590, 835, 836, 848, 849, 921, 922
        );
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 34, 35, 82, 83, 84, 85, 95, 96, 104,
                106, 160, 186, 191, 201, 202, 203, 204, 205, 206, 278, 287, 337, 358, 359, 360, 372, 373, 379, 383, 386,
                388, 389, 390, 392, 393, 394, 395, 396, 397, 398, 399, 400, 443, 445, 446, 447, 450, 452, 453, 454, 455,
                459, 494, 496, 499, 500, 505, 506, 554, 555, 556, 560, 562, 566, 569, 570, 571, 572, 575, 579, 580, 581,
                582, 584, 585, 586, 589, 591, 594, 596, 599, 602, 606
        );
    }

    public static void setMultiBattleStatusHGSS(List<Trainer> trs) {
        // 120 + 417: Double Battle with Ace Trainer Irene and Ace Trainer Jenn
        // 354 + 355: Double Battle with Lass Laura and Lass Shannon
        // 479 + 499: Multi Battle with Executive Ariana and Team Rocket Grunt in Team Rocket HQ
        // 679 + 680: Double Battle with Beauty Callie and Beauty Kassandra
        // 733 + 734: Multi Battle with Champion Lance and Leader Clair in the Dragon's Den
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.ALWAYS, 120, 354, 355, 417, 479, 499, 679, 680, 733, 734);

        // 147 + 151: Potential Double Battle with Camper Ted and Picnicker Erin
        // 423: Potential Double Battle with Pokéfan Jeremy. His potential teammate (Pokéfan Georgia) has more than
        // three Pokemon in the vanilla game, so we leave her be.
        // 564 + 567: Potential Double Battle with Teacher Clarice and School Kid Torin
        // 575 + 576: Potential Double Battle with Biker Dan and Biker Theron
        // 577 + 579: Potential Double Battle with Biker Markey and Biker Teddy
        setMultiBattleStatus(trs, Trainer.MultiBattleStatus.POTENTIAL, 147, 151, 423, 564, 567, 575, 576, 577, 579);
    }

    private static void setMultiBattleStatus(List<Trainer> allTrainers, Trainer.MultiBattleStatus status, int... numbers) {
        for (int num : numbers) {
            if (allTrainers.size() > (num - 1)) {
                allTrainers.get(num - 1).multiBattleStatus = status;
            }
        }
    }

    private static final int[] dpPostGameEncounterAreas = new int[] {
            530, 531, 532, 533, //Resort Area
            492, 493, 494, 495, 496, 497, //Route 224
            498, 499, 500, 501, 502, 503, //Route 225
            542, 543, 544, 545, 546, 547, //Route 226
            504, 505, 506, 507, 508, 509, //Route 227
            510, 511, 512, 513, 514, 515, //Route 228
            516, 517, 518, 519, 520, 521, //Route 229
            548, 549, 550, 551, 552, 553, //Route 230
            178, 179, 180, 181, 182, 183, //Stark Mountain
            184, 185, 186, 187, 188, 189, //Sendoff Spring
            196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
            208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219,
            220, 221, 222, 223, 224, 225, 226, 227, 228, 229, //Turnback Cave
            282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, //Snowpoint Temple
            3, 7, 11, 15, 19, 29, 37, 43, 57, 75, 81, 87, 93, 99, 107,
            153, 159, 169, 177, 309, 345, 351, 357, 363, 377, 383, 389,
            395, 401, 411, 417, 435, 445, 451, 457, 463, 475, 479, 485,
            491, 525, 529, 537, 541, //Super Rod
            21, 23, 25, 31, 33, 39, 45, 47, 49, 51, 53, 59, 61, 63, 65,
            67, 69, 71, 77, 83, 89, 95, 101, 103, 109, 111, 113, 115, 117,
            119, 121, 123, 125, 127, 129, 131, 133, 135, 137, 139, 141,
            143, 145, 147, 149, 155, 161, 163, 165, 171, 173, 295, 297,
            299, 301, 303, 305, 311, 313, 315, 317, 319, 321, 323, 325,
            327, 329, 331, 333, 335, 337, 339, 341, 347, 353, 359, 365,
            367, 369, 371, 373, 379, 385, 391, 397, 403, 405, 407, 413,
            419, 421, 423, 425, 427, 429, 431, 437, 439, 441, 447, 453,
            459, 465, 467, 469, 471, 481, 487, //Swarm/Radar/GBA
            558, //Trophy Garden Rotating Pokemon
            559, //Great Marsh Rotating Pokemon (Post-National Dex)
            154, 156, 157, 158, 160, 162, //Victory Road (back)
            190, 191, 192, 193, 194, 195, 230, 231, 232, 233, 234, 235,
            236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247,
            248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259,
            260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271,
            272, 273, 274, 275, 276, 277, 278, 279, 280, 281 //Unknown areas - likely unused
    };

    private static final int[] platPostGameEncounterAreas = new int[] {
            534, 535, 536, 537, //Resort Area
            496, 497, 498, 499, 500, 501, //Route 224
            502, 503, 504, 505, 506, 507, //Route 225
            546, 547, 548, 549, 550, 551, //Route 226
            508, 509, 510, 511, 512, 513, //Route 227
            514, 515, 516, 517, 518, 519, //Route 228
            520, 521, 522, 523, 524, 525, //Route 229
            552, 553, 554, 555, 556, 557, //Route 230
            182, 183, 184, 185, 186, 187, //Stark Mountain
            200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211,
            212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223,
            224, 225, 226, 227, 228, 229, 230, 231, 232, 233, //Turnback Cave
            286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, //Snowpoint Temple
            3, 7, 11, 15, 19, 29, 37, 43, 57, 75, 81, 87, 93, 99, 105,
            111, 157, 163, 173, 181, 193, 313, 349, 355, 361, 367, 381,
            387, 393, 399, 405, 415, 421, 439, 449, 455, 461, 467, 479,
            483, 489, 495, 529, 533, 541, 545, //Super Rod
            21, 23, 25, 31, 33, 39, 45, 47, 49, 51, 53, 59, 61, 63, 65,
            67, 69, 71, 77, 83, 89, 95, 101, 107, 113, 115, 117, 119, 121,
            123, 125, 127, 129, 131, 133, 135, 137, 139, 141, 143, 145,
            147, 149, 151, 153, 159, 165, 167, 169, 175, 177, 189, 299,
            301, 303, 305, 307, 309, 315, 317, 319, 321, 323, 325, 327,
            329, 331, 333, 335, 337, 339, 341, 343, 345, 351, 357, 363,
            369, 371, 373, 375, 377, 383, 389, 395, 401, 407, 409, 411,
            417, 423, 425, 427, 429, 431, 433, 435, 441, 443, 445, 451,
            457, 463, 469, 471, 473, 475, 485, 491, //Swarm/Radar/GBA
            562, //Trophy Garden Rotating Pokemon
            563, //Great Marsh Rotating Pokemon (Post-National Dex)
            158, 160, 161, 162, 164, 166, //back of Victory Road
            194, 195, 196, 197, 198, 199, 234, 235, 236, 237, 238, 239,
            240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251,
            252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263,
            264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275,
            276, 277, 278, 279, 280, 281, 282, 283, 284, 285 //Unknown areas - probably unused
    };

    private static final int[] hgssPostGameEncounterAreasTOD = new int[] {
            676, 677, 678, 679, 680, 681, 682, 1051, //Pallet Town
            683, 684, 685, 686, 687, 688, 689, 1052, //Viridian City
            1053, //Pewter City
            690, 691, 692, 693, 694, 695, 696, 1054, //Cerulean City
            697, 698, 699, 700, 701, 702, 703, 704, 1055, //Vermilion City
            705, 706, 1056, //Celadon City
            707, 708, 709, 710, 711, 712, 713, 1057, //Fuchsia City
            785, 786, 787, 788, 789, 1016, //Route 1
            790, 791, 792, 793, 794, 971, 972, 973, 974, 975, 1017, 1072, //Route 2
            795, 796, 797, 798, 799, 1018, //Route 3
            800, 801, 802, 803, 804, 805, 806, 807, 808, 809, 810, 1019, //Route 4
            811, 812, 813, 814, 815, 1020, //Route 5
            816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 1021, //Route 6
            827, 828, 829, 830, 831, 1022, //Route 7
            832, 833, 834, 835, 836, 1023, //Route 8
            837, 838, 839, 840, 841, 842, 843, 844, 845, 846, 847, //Route 9
            848, 849, 850, 851, 852, 853, 854, 855, 856, 857, 858, //Route 10
            859, 860, 861, 862, 863, 1024, //Route 11
            654, 655, 656, 657, 658, 659, 660, 1025, //Route 12
            864, 865, 866, 867, 868, 869, 870, 871, 872, 873, 874, 1026, //Route 13
            875, 876, 877, 878, 879, 1027, //Route 14
            880, 881, 882, 883, 884, 1028, //Route 15
            885, 886, 887, 888, 889, 1073, //Route 16
            890, 891, 892, 893, 894, //Route 17
            895, 896, 897, 898, 899, 1029, //Route 18
            661, 662, 663, 664, 665, 666, 667, 668, //Route 19
            669, 670, 671, 672, 673, 674, 675, //Route 20
            900, 901, 902, 903, 904, 905, 906, 907, 908, 909, 910, 1065, //Route 21
            911, 912, 913, 914, 915, 916, 917, 918, 919, 920, 921, 1030, //Route 22
            922, 923, 924, 925, 926, 927, 928, 929, 930, 931, 932, //Route 24
            933, 934, 935, 936, 937, 938, 939, 940, 941, 942, 943, 1031, //Route 25
            714, 715, 716, 717, 718, 719, 720, //Cinnabar Island
            981, 982, 983, 984, 985, 986, 987, 988, 989, 990, 991, 992, 993,
            994, 995, 996, 997, 998, 999, 1000, 1001, 1002, 1003, 1004, 1005,
            1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, //Cerulean Cave
            955, 956, 957, 958, 959, //DIGLETT’s Cave
            769, 770, 771, 772, 773, 774, 775, 776, 777, 778, 779, //Rock Tunnel
            518, 519, 520, 521, 522, 523, 524, 525, 526, 527, 528, 529, 530,
            531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543,
            544, 545, 546, 547, 548, //Seafoam Islands
            976, 977, 978, 979, 980, 1068, //Viridian Forest
            504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516,
            517, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, //Mt. Moon
            748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 758, 1034, //Route 28
            549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561,
            562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574,
            575, 576, 577, 578, 579, 580, 581, 600, 601, 602, 603, 604, 605,
            606, 607, 608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618,
            619, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 631,
            632, 633, 634, 635, 636, 637, 638, 639, 640, 641, 642, 1064, 1074, //Mt. Silver
            593, //Cliff Cave Rock Smash
            3, 15, 26, 37, 44, 65, 77, 108, 119, 130, 146, 157, 168, 179, 190,
            217, 284, 291, 298, 309, 325, 338, 349, 360, 376, 387, 398, 405,
            416, 443, 450, 461, 478, 489, 500, 585, 650, 733, 744, 951, //Super Rod
            4, 6, 11, 16, 18, 27, 29, 38, 40, 45, 47, 52, 57, 66, 68, 78, 80,
            85, 90, 95, 100, 109, 111, 120, 122, 131, 133, 138, 147, 149, 158,
            160, 169, 171, 180, 182, 191, 193, 198, 203, 208, 213, 218, 220,
            225, 230, 235, 240, 245, 250, 255, 260, 265, 270, 275, 280, 285,
            287, 292, 294, 299, 301, 310, 312, 317, 326, 328, 333, 339, 341,
            350, 352, 361, 363, 368, 377, 379, 388, 390, 399, 401, 406, 408,
            417, 419, 424, 429, 434, 439, 444, 446, 451, 453, 462, 464, 469,
            479, 481, 490, 492, 501, 503, 586, 588, 594, 599, 651, 653, 725,
            734, 736, 745, 747, 784, 952, 954, 964, 970, //Swarm
            10, 22, 33, 51, 56, 61, 72, 84, 89, 94, 99, 104, 115, 126, 137,
            142, 153, 164, 175, 186, 197, 202, 207, 212, 224, 229, 234, 239,
            244, 249, 254, 259, 264, 269, 274, 279, 305, 316, 321, 332, 345,
            356, 367, 372, 383, 394, 412, 423, 428, 433, 438, 457, 468, 473,
            485, 496, 592, 598, 646, 724, 729, 740, 783, 947, 963, 968, //Radio
            1076, 1077, 1078, //Post-National Dex Bug-Catching Contests
            643, 644, 645, 647, 648, 649, 652, 1071 //Safari Zone - unused?
    };

    private static final int[] hgssPartialPostGameTOD = new int[] {
            1044, 1059, 1066 //headbutt trees
    };

    private static final int[] hgssPostGameEncounterAreasNoTOD = new int[] {
            532, 533, 534, 535, 536, 537, 538, 829, //Pallet Town
            539, 540, 541, 542, 543, 544, 545, 830, //Viridian City
            831, //Pewter City
            546, 547, 548, 549, 550, 551, 552, 832, //Cerulean City
            553, 554, 555, 556, 557, 558, 559, 560, 833, //Vermilion City
            561, 562, 834, //Celadon City
            563, 564, 565, 566, 567, 568, 569, 835, //Fuchsia City
            623, 624, 625, 794, //Route 1
            626, 627, 628, 759, 760, 761, 795, 850, //Route 2
            629, 630, 631, 796, //Route 3
            632, 633, 634, 635, 636, 637, 638, 639, 640, 797, //Route 4
            641, 642, 643, 798, //Route 5
            644, 645, 646, 647, 648, 649, 650, 651, 652, 799, //Route 6
            653, 654, 655, 800, //Route 7
            656, 657, 658, 801, //Route 8
            659, 660, 661, 662, 663, 664, 665, 666, 667, //Route 9
            668, 669, 670, 671, 672, 673, 674, 675, 676, //Route 10
            677, 678, 679, 802, //Route 11
            510, 511, 512, 513, 514, 515, 516, 803, //Route 12
            680, 681, 682, 683, 684, 685, 686, 687, 688, 804, //Route 13
            689, 690, 691, 805, //Route 14
            692, 693, 694, 806, //Route 15
            695, 696, 697, 851, //Route 16
            698, 699, 700, //Route 17
            701, 702, 703, 807, //Route 18
            517, 518, 519, 520, 521, 522, 523, 524, //Route 19
            525, 526, 527, 528, 529, 530, 531, //Route 20
            704, 705, 706, 707, 708, 709, 710, 711, 712, 843, //Route 21
            713, 714, 715, 716, 717, 718, 719, 720, 721, 808, //Route 22
            722, 723, 724, 725, 726, 727, 728, 729, 730, //Route 24
            731, 732, 733, 734, 735, 736, 737, 738, 739, 809, //Route 25
            570, 571, 572, 573, 574, 575, 576, //Cinnabar Island
            765, 766, 767, 768, 769, 770, 771, 772, 773, 774, 775, 776, 777,
            778, 779, 780, 781, 782, 783, 784, 785, 786, 787, 788, 789, 790,
            791, 792, 793, //Cerulean Cave
            749, 750, 751, //DIGLETT’s Cave
            613, 614, 615, 616, 617, 618, 619, //Rock Tunnel
            406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418,
            419, 420, 421, 422, 423, 424, 425, 426, //Seafoam Islands
            762, 763, 764, 846, //Viridian Forest
            392, 393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404,
            405, 607, 608, 609, 610, 611, 612, //Mt. Moon
            598, 599, 600, 601, 602, 603, 604, 605, 606, 812, //Route 28
            427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439,
            440, 441, 442, 443, 444, 445, 446, 447, 448, 449, 450, 451, 452,
            453, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479,
            480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492,
            493, 494, 495, 496, 497, 498, 499, 500, 842, 852, //Mt. Silver
            463, //Cliff Cave Rock Smash
            3, 13, 22, 31, 38, 53, 63, 84, 93, 102, 114, 123, 132, 141, 150,
            169, 212, 219, 226, 235, 247, 258, 267, 276, 288, 297, 306, 313,
            322, 341, 348, 357, 370, 379, 388, 457, 506, 585, 594, 745, //Super Rod
            4, 6, 9, 14, 16, 23, 25, 32, 34, 39, 41, 44, 47, 54, 56, 64, 66,
            69, 72, 75, 78, 85, 87, 94, 96, 103, 105, 108, 115, 117, 124, 126,
            133, 135, 142, 144, 151, 153, 156, 159, 162, 165, 170, 172, 175,
            178, 181, 184, 187, 190, 193, 196, 199, 202, 205, 208, 213, 215,
            220, 222, 227, 229, 236, 238, 241, 248, 250, 253, 259, 261, 268,
            270, 277, 279, 282, 289, 291, 298, 300, 307, 309, 314, 316, 323,
            325, 328, 331, 334, 337, 342, 344, 349, 351, 358, 360, 363, 371,
            373, 380, 382, 389, 391, 458, 460, 464, 467, 507, 509, 579, 586,
            588, 595, 597, 622, 746, 748, 754, 758, //Swarm
            8, 18, 27, 43, 46, 49, 58, 68, 71, 74, 77, 80, 89, 98, 107, 110,
            119, 128, 137, 146, 155, 158, 161, 164, 174, 177, 180, 183, 186,
            189, 192, 195, 198, 201, 204, 207, 231, 240, 243, 252, 263, 272,
            281, 284, 293, 302, 318, 327, 330, 333, 336, 353, 362, 365, 375,
            384, 462, 466, 502, 578, 581, 590, 621, 741, 753, 756, //Radio
            854, 855, 856, //Post-National Dex Bug-Catching Contests
            501, 503, 504, 505, 508, 849, //Safari Zone - unused?
    };

    private static final int[] hgssPartialPostGameNoTOD = new int[] {
            822, 837, 844 //headbutt trees
    };

    private static final int headbuttPartialPostgameCutoff = 12;

    private static final List<String> locationTagsDP = initLocationTagsDP();

    private static List<String> initLocationTagsDP() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 4, "CANALAVE CITY");
        addCopies(tags, 4, "ETERNA CITY");
        addCopies(tags, 4, "PASTORIA CITY");
        addCopies(tags, 4, "SUNYSHORE CITY");
        addCopies(tags, 4, "POKEMON LEAGUE");
        addCopies(tags, 4, "OREBURGH MINE");
        addCopies(tags, 6, "VALLEY WINDWORKS");
        addCopies(tags, 2, "ETERNA FOREST");
        addCopies(tags, 6, "FUEGO IRONWORKS");
        addCopies(tags, 38, "MT. CORONET");
        addCopies(tags, 32, "GREAT MARSH");
        addCopies(tags, 36, "SOLACEON RUINS");
        addCopies(tags, 20, "VICTORY ROAD");
        addCopies(tags, 6, "RAVAGED PATH");
        addCopies(tags, 8, "OREBURGH GATE");
        addCopies(tags, 6, "STARK MOUNTAIN");
        addCopies(tags, 6, "SENDOFF SPRING");
        addCopies(tags, 6, "UNKNOWN");
        addCopies(tags, 34, "TURNBACK CAVE");
        addCopies(tags, 52, "UNKNOWN");
        addCopies(tags, 12, "SNOWPOINT TEMPLE");
        addCopies(tags, 4, "WAYWARD CAVE");
        addCopies(tags, 6, "MANIAC TUNNEL");
        addCopies(tags, 2, "TROPHY GARDEN");
        addCopies(tags, 16, "IRON ISLAND");
        addCopies(tags, 18, "OLD CHATEAU");
        addCopies(tags, 12, "LAKE VERITY");
        addCopies(tags, 6, "LAKE VALOR");
        addCopies(tags, 6, "LAKE ACUITY");
        addCopies(tags, 2, "VALOR LAKEFRONT");
        addCopies(tags, 2, "ACUITY LAKEFRONT");
        addCopies(tags, 2, "ROUTE 201");
        addCopies(tags, 2, "ROUTE 202");
        addCopies(tags, 6, "ROUTE 203");
        addCopies(tags, 12, "ROUTE 204");
        addCopies(tags, 12, "ROUTE 205");
        addCopies(tags, 2, "ROUTE 206");
        addCopies(tags, 2, "ROUTE 207");
        addCopies(tags, 6, "ROUTE 208");
        addCopies(tags, 16, "ROUTE 209");
        addCopies(tags, 8, "ROUTE 210");
        addCopies(tags, 4, "ROUTE 211");
        addCopies(tags, 12, "ROUTE 212");
        addCopies(tags, 6, "ROUTE 213");
        addCopies(tags, 6, "ROUTE 214");
        addCopies(tags, 2, "ROUTE 215");
        addCopies(tags, 2, "ROUTE 216");
        addCopies(tags, 2, "ROUTE 217");
        addCopies(tags, 6, "ROUTE 218");
        addCopies(tags, 4, "ROUTE 219");
        addCopies(tags, 6, "ROUTE 221");
        addCopies(tags, 6, "ROUTE 222");
        addCopies(tags, 6, "ROUTE 224");
        addCopies(tags, 6, "ROUTE 225");
        addCopies(tags, 6, "ROUTE 227");
        addCopies(tags, 6, "ROUTE 228");
        addCopies(tags, 6, "ROUTE 229");
        addCopies(tags, 4, "TWINLEAF TOWN");
        addCopies(tags, 4, "CELESTIC TOWN");
        addCopies(tags, 4, "RESORT AREA");
        addCopies(tags, 4, "ROUTE 220");
        addCopies(tags, 4, "ROUTE 223");
        addCopies(tags, 6, "ROUTE 226");
        addCopies(tags, 6, "ROUTE 230");
        addCopies(tags, 1, "MT. CORONET");
        addCopies(tags, 3, "HONEY TREE");
        addCopies(tags, 1, "TROPHY GARDEN");
        addCopies(tags, 2, "GREAT MARSH");
        return Collections.unmodifiableList(tags);
    }

    public static final int trophyGardenMapIndex = 117;
    public static final int nationalParkMapIndex = 23;
    public static final int nationalParkBadMapIndex = 24; //National Park is incorrectly considered two maps
    //This lets us merge them
    //(Some other areas that are debatably the same map are not merged)
    public static final int mtCoronetFeebasLakeMapIndex = 10;
    //(It's not the best that we're hardcoding these, but we've already got plenty of hardcoded stuff that probably
    //shouldn't be)

    private static final List<String> locationTagsPt = initLocationTagsPt();

    private static List<String> initLocationTagsPt() {
        List<String> locationTags = new ArrayList<>();
        locationTags.addAll(locationTagsDP.subList(0, 108));
        locationTags.addAll(Arrays.asList("GREAT MARSH", "GREAT MARSH", "GREAT MARSH", "GREAT MARSH"));
        locationTags.addAll(locationTagsDP.subList(108, 561));
        return Collections.unmodifiableList(locationTags);
    }

    private static final List<String> locationTagsNoTimeHGSS = initLocationTagsNoTimeHGSS();

    private static List<String> initLocationTagsNoTimeHGSS() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 7, "NEW BARK TOWN");
        addCopies(tags, 3, "ROUTE 29");
        addCopies(tags, 7, "CHERRYGROVE CITY");
        addCopies(tags, 9, "ROUTE 30");
        addCopies(tags, 9, "ROUTE 31");
        addCopies(tags, 7, "VIOLET CITY");
        addCopies(tags, 6, "SPROUT TOWER");
        addCopies(tags, 9, "ROUTE 32");
        addCopies(tags, 22, "RUINS OF ALPH");
        addCopies(tags, 27, "UNION CAVE");
        addCopies(tags, 3, "ROUTE 33");
        addCopies(tags, 18, "SLOWPOKE WELL");
        addCopies(tags, 9, "ILEX FOREST");
        addCopies(tags, 9, "ROUTE 34");
        addCopies(tags, 9, "ROUTE 35");
        addCopies(tags, 6, "NATIONAL PARK");
        addCopies(tags, 3, "ROUTE 36");
        addCopies(tags, 3, "ROUTE 37");
        addCopies(tags, 7, "ECRUTEAK CITY");
        addCopies(tags, 6, "BURNED TOWER");
        addCopies(tags, 24, "BELL TOWER");
        addCopies(tags, 3, "ROUTE 38");
        addCopies(tags, 3, "ROUTE 39");
        addCopies(tags, 7, "OLIVINE CITY");
        addCopies(tags, 7, "ROUTE 40");
        addCopies(tags, 7, "ROUTE 41");
        addCopies(tags, 24, "WHIRL ISLANDS");
        addCopies(tags, 8, "CIANWOOD CITY");
        addCopies(tags, 9, "ROUTE 42");
        addCopies(tags, 30, "MT. MORTAR");
        addCopies(tags, 9, "ROUTE 43");
        addCopies(tags, 7, "LAKE OF RAGE");
        addCopies(tags, 9, "ROUTE 44");
        addCopies(tags, 12, "ICE PATH");
        addCopies(tags, 7, "BLACKTHORN CITY");
        addCopies(tags, 7, "DRAGON'S DEN");
        addCopies(tags, 9, "ROUTE 45");
        addCopies(tags, 3, "ROUTE 46");
        addCopies(tags, 19, "DARK CAVE");
        addCopies(tags, 9, "ROUTE 47");
        addCopies(tags, 14, "MT. MOON");
        addCopies(tags, 21, "SEAFOAM ISLANDS");
        addCopies(tags, 27, "MT. SILVER CAVE");
        addCopies(tags, 11, "CLIFF EDGE GATE");
        addCopies(tags, 3, "BELL TOWER");
        addCopies(tags, 9, "MT. SILVER");
        addCopies(tags, 23, "MT. SILVER CAVE");
        addCopies(tags, 9, "SAFARI ZONE");
        addCopies(tags, 7, "ROUTE 12");
        addCopies(tags, 8, "ROUTE 19");
        addCopies(tags, 7, "ROUTE 20");
        addCopies(tags, 7, "PALLET TOWN");
        addCopies(tags, 7, "VIRIDIAN CITY");
        addCopies(tags, 7, "CERULEAN CITY");
        addCopies(tags, 8, "VERMILION CITY");
        addCopies(tags, 2, "CELADON CITY");
        addCopies(tags, 8, "FUCHSIA CITY");
        addCopies(tags, 7, "CINNABAR ISLAND");
        addCopies(tags, 3, "ROUTE 48");
        addCopies(tags, 9, "ROUTE 26");
        addCopies(tags, 9, "ROUTE 27");
        addCopies(tags, 9, "ROUTE 28");
        addCopies(tags, 6, "MT. MOON");
        addCopies(tags, 7, "ROCK TUNNEL");
        addCopies(tags, 3, "VICTORY ROAD");
        addCopies(tags, 3, "ROUTE 1");
        addCopies(tags, 3, "ROUTE 2");
        addCopies(tags, 3, "ROUTE 3");
        addCopies(tags, 9, "ROUTE 4");
        addCopies(tags, 3, "ROUTE 5");
        addCopies(tags, 9, "ROUTE 6");
        addCopies(tags, 3, "ROUTE 7");
        addCopies(tags, 3, "ROUTE 8");
        addCopies(tags, 9, "ROUTE 9");
        addCopies(tags, 9, "ROUTE 10");
        addCopies(tags, 3, "ROUTE 11");
        addCopies(tags, 9, "ROUTE 13");
        addCopies(tags, 3, "ROUTE 14");
        addCopies(tags, 3, "ROUTE 15");
        addCopies(tags, 3, "ROUTE 16");
        addCopies(tags, 3, "ROUTE 17");
        addCopies(tags, 3, "ROUTE 18");
        addCopies(tags, 9, "ROUTE 21");
        addCopies(tags, 9, "ROUTE 22");
        addCopies(tags, 9, "ROUTE 24");
        addCopies(tags, 9, "ROUTE 25");
        addCopies(tags, 9, "TOHJO FALLS");
        addCopies(tags, 3, "DIGLETT'S CAVE");
        addCopies(tags, 7, "VICTORY ROAD");
        addCopies(tags, 3, "ROUTE 2");
        addCopies(tags, 3, "VIRIDIAN FOREST");
        addCopies(tags, 29, "CERULEAN CAVE");
        // headbutt
        tags.addAll(Arrays.asList("ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7",
                "ROUTE 8", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 18", "ROUTE 22",
                "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28", "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32",
                "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37", "ROUTE 38", "ROUTE 39", "ROUTE 42",
                "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46", "PALLET TOWN", "VIRIDIAN CITY", "PEWTER CITY",
                "CERULEAN CITY", "VERMILION CITY", "CELADON CITY", "FUCHSIA CITY", "NEW BARK TOWN", "CHERRYGROVE CITY",
                "VIOLET CITY", "AZALEA TOWN", "ECRUTEAK CITY", "LAKE OF RAGE", "MT. SILVER", "ROUTE 21",
                "NATIONAL PARK", "ILEX FOREST", "VIRIDIAN FOREST", "ROUTE 47", "ROUTE 48", "SAFARI ZONE", "ROUTE 2",
                "ROUTE 16", "MT. SILVER CAVE"));
        addCopies(tags, 4, "BUG CATCHING CONTEST");
        return Collections.unmodifiableList(tags);
    }

    private static final List<String> locationTagsUseTimeHGSS = initLocationTagsUseTimeHGSS();

    private static List<String> initLocationTagsUseTimeHGSS() {
        List<String> tags = new ArrayList<>();
        addCopies(tags, 7, "NEW BARK TOWN");
        addCopies(tags, 5, "ROUTE 29");
        addCopies(tags, 7, "CHERRYGROVE CITY");
        addCopies(tags, 11, "ROUTE 30");
        addCopies(tags, 11, "ROUTE 31");
        addCopies(tags, 7, "VIOLET CITY");
        addCopies(tags, 10, "SPROUT TOWER");
        addCopies(tags, 11, "ROUTE 32");
        addCopies(tags, 32, "RUINS OF ALPH");
        addCopies(tags, 33, "UNION CAVE");
        addCopies(tags, 5, "ROUTE 33");
        addCopies(tags, 22, "SLOWPOKE WELL");
        addCopies(tags, 11, "ILEX FOREST");
        addCopies(tags, 11, "ROUTE 34");
        addCopies(tags, 11, "ROUTE 35");
        addCopies(tags, 10, "NATIONAL PARK");
        addCopies(tags, 5, "ROUTE 36");
        addCopies(tags, 5, "ROUTE 37");
        addCopies(tags, 7, "ECRUTEAK CITY");
        addCopies(tags, 10, "BURNED TOWER");
        addCopies(tags, 40, "BELL TOWER");
        addCopies(tags, 5, "ROUTE 38");
        addCopies(tags, 5, "ROUTE 39");
        addCopies(tags, 7, "OLIVINE CITY");
        addCopies(tags, 7, "ROUTE 40");
        addCopies(tags, 7, "ROUTE 41");
        addCopies(tags, 32, "WHIRL ISLANDS");
        addCopies(tags, 8, "CIANWOOD CITY");
        addCopies(tags, 11, "ROUTE 42");
        addCopies(tags, 38, "MT. MORTAR");
        addCopies(tags, 11, "ROUTE 43");
        addCopies(tags, 7, "LAKE OF RAGE");
        addCopies(tags, 11, "ROUTE 44");
        addCopies(tags, 20, "ICE PATH");
        addCopies(tags, 7, "BLACKTHORN CITY");
        addCopies(tags, 7, "DRAGON'S DEN");
        addCopies(tags, 11, "ROUTE 45");
        addCopies(tags, 5, "ROUTE 46");
        addCopies(tags, 23, "DARK CAVE");
        addCopies(tags, 11, "ROUTE 47");
        addCopies(tags, 14, "MT. MOON");
        addCopies(tags, 31, "SEAFOAM ISLANDS");
        addCopies(tags, 33, "MT. SILVER CAVE");
        addCopies(tags, 13, "CLIFF EDGE GATE");
        addCopies(tags, 5, "BELL TOWER");
        addCopies(tags, 11, "MT. SILVER");
        addCopies(tags, 31, "MT. SILVER CAVE");
        addCopies(tags, 11, "SAFARI ZONE");
        addCopies(tags, 7, "ROUTE 12");
        addCopies(tags, 8, "ROUTE 19");
        addCopies(tags, 7, "ROUTE 20");
        addCopies(tags, 7, "PALLET TOWN");
        addCopies(tags, 7, "VIRIDIAN CITY");
        addCopies(tags, 7, "CERULEAN CITY");
        addCopies(tags, 8, "VERMILION CITY");
        addCopies(tags, 2, "CELADON CITY");
        addCopies(tags, 8, "FUCHSIA CITY");
        addCopies(tags, 7, "CINNABAR ISLAND");
        addCopies(tags, 5, "ROUTE 48");
        addCopies(tags, 11, "ROUTE 26");
        addCopies(tags, 11, "ROUTE 27");
        addCopies(tags, 11, "ROUTE 28");
        addCopies(tags, 10, "MT. MOON");
        addCopies(tags, 11, "ROCK TUNNEL");
        addCopies(tags, 5, "VICTORY ROAD");
        addCopies(tags, 5, "ROUTE 1");
        addCopies(tags, 5, "ROUTE 2");
        addCopies(tags, 5, "ROUTE 3");
        addCopies(tags, 11, "ROUTE 4");
        addCopies(tags, 5, "ROUTE 5");
        addCopies(tags, 11, "ROUTE 6");
        addCopies(tags, 5, "ROUTE 7");
        addCopies(tags, 5, "ROUTE 8");
        addCopies(tags, 11, "ROUTE 9");
        addCopies(tags, 11, "ROUTE 10");
        addCopies(tags, 5, "ROUTE 11");
        addCopies(tags, 11, "ROUTE 13");
        addCopies(tags, 5, "ROUTE 14");
        addCopies(tags, 5, "ROUTE 15");
        addCopies(tags, 5, "ROUTE 16");
        addCopies(tags, 5, "ROUTE 17");
        addCopies(tags, 5, "ROUTE 18");
        addCopies(tags, 11, "ROUTE 21");
        addCopies(tags, 11, "ROUTE 22");
        addCopies(tags, 11, "ROUTE 24");
        addCopies(tags, 11, "ROUTE 25");
        addCopies(tags, 11, "TOHJO FALLS");
        addCopies(tags, 5, "DIGLETT'S CAVE");
        addCopies(tags, 11, "VICTORY ROAD");
        addCopies(tags, 5, "ROUTE 2");
        addCopies(tags, 5, "VIRIDIAN FOREST");
        addCopies(tags, 35, "CERULEAN CAVE");
        // headbutt
        tags.addAll(Arrays.asList("ROUTE 1", "ROUTE 2", "ROUTE 3", "ROUTE 4", "ROUTE 5", "ROUTE 6", "ROUTE 7",
                "ROUTE 8", "ROUTE 11", "ROUTE 12", "ROUTE 13", "ROUTE 14", "ROUTE 15", "ROUTE 18", "ROUTE 22",
                "ROUTE 25", "ROUTE 26", "ROUTE 27", "ROUTE 28", "ROUTE 29", "ROUTE 30", "ROUTE 31", "ROUTE 32",
                "ROUTE 33", "ROUTE 34", "ROUTE 35", "ROUTE 36", "ROUTE 37", "ROUTE 38", "ROUTE 39", "ROUTE 42",
                "ROUTE 43", "ROUTE 44", "ROUTE 45", "ROUTE 46", "PALLET TOWN", "VIRIDIAN CITY", "PEWTER CITY",
                "CERULEAN CITY", "VERMILION CITY", "CELADON CITY", "FUCHSIA CITY", "NEW BARK TOWN", "CHERRYGROVE CITY",
                "VIOLET CITY", "AZALEA TOWN", "ECRUTEAK CITY", "LAKE OF RAGE", "MT. SILVER", "ROUTE 21",
                "NATIONAL PARK", "ILEX FOREST", "VIRIDIAN FOREST", "ROUTE 47", "ROUTE 48", "SAFARI ZONE", "ROUTE 2",
                "ROUTE 16", "MT. SILVER CAVE"));
        addCopies(tags, 4, "BUG CATCHING CONTEST");
        return Collections.unmodifiableList(tags);
    }

    private static void addCopies(List<String> list, int n, String s) {
        list.addAll(Collections.nCopies(n, s));
    }

    /**
     * Based on <a href=https://strategywiki.org/wiki/Pok%C3%A9mon_Diamond_and_Pearl/Walkthrough>this walkthrough</a>.
     */
    public static final List<String> locationTagsTraverseOrderDPPt = Collections.unmodifiableList(Arrays.asList(
            "TWINLEAF TOWN", "ROUTE 201", "LAKE VERITY", "ROUTE 202", "ROUTE 203", "OREBURGH GATE",
            "OREBURGH MINE", "ROUTE 204", "RAVAGED PATH", "VALLEY WINDWORKS", "ROUTE 205", "ETERNA FOREST",
            "OLD CHATEAU", "ETERNA CITY", "ROUTE 206", "WAYWARD CAVE", "ROUTE 207", "ROUTE 208", "ROUTE 209",
            "SOLACEON RUINS", "ROUTE 210", "ROUTE 215", "ROUTE 214", "MANIAC TUNNEL", "VALOR LAKEFRONT", "ROUTE 213",
            "PASTORIA CITY", "GREAT MARSH", "ROUTE 212", "TROPHY GARDEN", "CELESTIC TOWN", "FUEGO IRONWORKS",
            "ROUTE 219", "ROUTE 220", "ROUTE 221", "ROUTE 218", "CANALAVE CITY", "IRON ISLAND", "LAKE VALOR",
            "ROUTE 211", "MT. CORONET", "ROUTE 216", "ROUTE 217", "ACUITY LAKEFRONT", "LAKE ACUITY", "ROUTE 222",
            "SUNYSHORE CITY", "ROUTE 223", "VICTORY ROAD", "POKEMON LEAGUE", "ROUTE 224", "ROUTE 230", "ROUTE 229",
            "RESORT AREA", "ROUTE 228", "ROUTE 226", "ROUTE 227", "ROUTE 225", "STARK MOUNTAIN", "SNOWPOINT TEMPLE",
            "SENDOFF SPRING", "TURNBACK CAVE", "HONEY TREE", "UNKNOWN"
    ));

    /**
     * Based on <a href=https://strategywiki.org/wiki/Pok%C3%A9mon_Gold_and_Silver/Walkthrough>this walkthrough</a>,
     * with Gen IV-only locations added.
     */
    public static final List<String> locationTagsTraverseOrderHGSS = Collections.unmodifiableList(Arrays.asList(
            "NEW BARK TOWN", "ROUTE 29", "ROUTE 46", "CHERRYGROVE CITY", "ROUTE 30", "ROUTE 31", "DARK CAVE",
            "VIOLET CITY", "SPROUT TOWER", "ROUTE 32", "RUINS OF ALPH", "UNION CAVE", "ROUTE 33", "AZALEA TOWN",
            "SLOWPOKE WELL", "ILEX FOREST", "ROUTE 34", "GOLDENROD CITY", "ROUTE 35", "NATIONAL PARK",
            "ROUTE 36", "ROUTE 37", "ECRUTEAK CITY", "BURNED TOWER", "ROUTE 38", "ROUTE 39", "OLIVINE CITY",
            "ROUTE 40", "ROUTE 41", "CIANWOOD CITY", "CLIFF EDGE GATE", "ROUTE 47", "CLIFF CAVE", "ROUTE 48",
            "SAFARI ZONE", "ROUTE 42", "MT. MORTAR", "ROUTE 43", "LAKE OF RAGE",
            "ROUTE 44", "ICE PATH", "BLACKTHORN CITY", "DRAGON'S DEN", "ROUTE 45", "WHIRL ISLANDS",
            "BELL TOWER", "ROUTE 27", "TOHJO FALLS", "ROUTE 26", "VICTORY ROAD",
            "VERMILION CITY", "ROUTE 6", "ROUTE 7", "ROUTE 8", "ROCK TUNNEL", "ROUTE 10", "ROUTE 9",
            "CERULEAN CITY", "ROUTE 24", "ROUTE 25", "ROUTE 5", "CELADON CITY", "ROUTE 16", "ROUTE 17",
            "ROUTE 18", "FUCHSIA CITY", "ROUTE 15", "ROUTE 14", "ROUTE 13", "ROUTE 12", "ROUTE 11",
            "DIGLETT'S CAVE", "ROUTE 2", "VIRIDIAN FOREST", "ROUTE 3", "PEWTER CITY", "MT. MOON", "ROUTE 4",
            "VIRIDIAN CITY", "ROUTE 1", "PALLET TOWN",
            "ROUTE 21", "CINNABAR ISLAND", "ROUTE 20", "SEAFOAM ISLANDS", "ROUTE 19", "ROUTE 22", "ROUTE 28",
            "MT. SILVER", "MT. SILVER CAVE", "CERULEAN CAVE", "BUG CATCHING CONTEST"
    ));

    private static void tagEncounterAreas(List<EncounterArea> encounterAreas, List<String> locationTags,
                                          int[] postGameAreas, int[] partialPostGameAreas) {
        if (encounterAreas.size() != locationTags.size()) {
            throw new IllegalArgumentException("Unexpected amount of encounter areas");
        }
        for (int i = 0; i < encounterAreas.size(); i++) {
            encounterAreas.get(i).setLocationTag(locationTags.get(i));
        }
        for (int areaIndex : postGameAreas) {
            encounterAreas.get(areaIndex).setPostGame(true);
        }
        for (int areaIndex : partialPostGameAreas) {
            encounterAreas.get(areaIndex).setPartiallyPostGameCutoff(headbuttPartialPostgameCutoff);
        }
    }

    public static void tagEncounterAreas(List<EncounterArea> encounterAreas, int romType, boolean useTimeOfDay) {
        List<String> locationTags;
        int[] postGameAreas;
        int[] partialPostGameAreas;
        switch (romType) {
            case 0:
                locationTags = locationTagsDP;
                postGameAreas = dpPostGameEncounterAreas;
                partialPostGameAreas = new int[0];
                break;
            case 1:
                locationTags = locationTagsPt;
                postGameAreas = platPostGameEncounterAreas;
                partialPostGameAreas = new int[0];
                break;
            case 2:
                locationTags = (useTimeOfDay ? locationTagsUseTimeHGSS : locationTagsNoTimeHGSS);
                postGameAreas = (useTimeOfDay ? hgssPostGameEncounterAreasTOD : hgssPostGameEncounterAreasNoTOD);
                partialPostGameAreas = (useTimeOfDay ? hgssPartialPostGameTOD : hgssPartialPostGameNoTOD);
                break;
            default:
                throw new IllegalStateException("Unexpected value for romType: " + romType);
        }
        tagEncounterAreas(encounterAreas, locationTags, postGameAreas, partialPostGameAreas);
    }

}
