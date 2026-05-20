package com.uprfvx.romio.services;

import com.uprfvx.romio.constants.Gen6Constants;
import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.ItemMechanicCategory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Source-backed category layer for CFRU/DPE item identities.
 *
 * <p>The numeric CFRU/DPE constants below are from the provided items.h layout. Gen3 handlers store unknown expanded
 * item IDs in UPR-FVX's standard item namespace as {@code ItemIDs.UNIQUE_OFFSET + sourceId}. Those standard IDs can
 * collide with generic later-generation item IDs, so CFRU/DPE source-block checks are paired with decoded item names
 * instead of being treated as globally unique item identities.</p>
 */
public final class CfruDpeItemCategories {

    public static final int ITEM_ULTRANECROZIUM_Z = 0x214;
    public static final int ITEM_VENUSAURITE = 0x215;
    public static final int ITEM_DIANCITE = 0x243;
    public static final int ITEM_NORMALIUM_Z = 0x244;
    public static final int ITEM_TAPUNIUM_Z = 0x265;

    private static final Set<Integer> MEGA_ACCESSORY_IDS = Set.of(
            ItemIDs.megaRing,
            ItemIDs.megaCharm,
            ItemIDs.megaGlove,
            ItemIDs.megaBracelet,
            ItemIDs.megaPendant,
            ItemIDs.megaGlasses,
            ItemIDs.megaAnchor,
            ItemIDs.megaStickpin,
            ItemIDs.megaTiara,
            ItemIDs.megaAnklet,
            ItemIDs.megaCuff,
            ItemIDs.keyStone
    );
    private static final Set<String> MEGA_STONE_NAMES = Set.of(
            "gengarite",
            "gardevoirite",
            "ampharosite",
            "venusaurite",
            "charizarditex",
            "blastoisinite",
            "mewtwonitex",
            "mewtwonitey",
            "blazikenite",
            "medichamite",
            "houndoominite",
            "aggronite",
            "banettite",
            "tyranitarite",
            "scizorite",
            "pinsirite",
            "aerodactylite",
            "lucarionite",
            "abomasite",
            "kangaskhanite",
            "gyaradosite",
            "absolite",
            "charizarditey",
            "alakazite",
            "heracronite",
            "mawilite",
            "manectite",
            "garchompite",
            "latiasite",
            "latiosite",
            "swampertite",
            "sceptilite",
            "sablenite",
            "altarianite",
            "galladite",
            "audinite",
            "metagrossite",
            "sharpedonite",
            "slowbronite",
            "steelixite",
            "pidgeotite",
            "glalitite",
            "diancite",
            "cameruptite",
            "lopunnite",
            "salamencite",
            "beedrillite"
    );
    private static final Set<String> MEGA_ACCESSORY_NAMES = Set.of(
            "megaring",
            "megacharm",
            "megaglove",
            "megabracelet",
            "megapendant",
            "megaglasses",
            "megaanchor",
            "megastickpin",
            "megatiara",
            "megaanklet",
            "megacuff",
            "megabonnet",
            "megaearing",
            "megaearring",
            "keystone"
    );

    private static final Set<Integer> Z_CRYSTAL_IDS = setupZCrystalIds();
    private static final Set<Integer> Z_ACCESSORY_IDS = Set.of(ItemIDs.zRing, ItemIDs.zPowerRing);
    private static final Set<String> Z_CRYSTAL_NAMES = Set.of(
            "normaliumz",
            "fightiniumz",
            "flyiniumz",
            "poisoniumz",
            "groundiumz",
            "rockiumz",
            "buginiumz",
            "ghostiumz",
            "steeliumz",
            "firiumz",
            "wateriumz",
            "grassiumz",
            "electriumz",
            "psychiumz",
            "iciumz",
            "dragoniumz",
            "darkiniumz",
            "fairiumz",
            "aloraichiumz",
            "pikaniumz",
            "pikashuniumz",
            "decidiumz",
            "eeviumz",
            "inciniumz",
            "kommoniumz",
            "lunaliumz",
            "lycaniumz",
            "marshadiumz",
            "mewniumz",
            "mimikiumz",
            "primariumz",
            "snorliumz",
            "solganiumz",
            "tapuniumz",
            "necroziumz",
            "ultranecroziumz"
    );
    private static final Set<String> Z_ACCESSORY_NAMES = Set.of("zring", "zpowerring");

    private static final Set<Integer> DYNAMAX_GIGANTAMAX_IDS = Set.of(
            ItemIDs.wishingStar,
            ItemIDs.dynamaxBand,
            ItemIDs.dynamaxCandy,
            ItemIDs.wishingPiece,
            ItemIDs.wishingChip,
            ItemIDs.maxHoney,
            ItemIDs.maxMushrooms,
            ItemIDs.gigantamix,
            ItemIDs.dyniteOre
    );
    private static final Set<String> DYNAMAX_GIGANTAMAX_NAMES = Set.of(
            "wishingstar",
            "dynamaxband",
            "dynamaxcandy",
            "wishingpiece",
            "wishingchip",
            "maxhoney",
            "maxmushrooms",
            "gigantamix",
            "dyniteore"
    );

    private static final Set<Integer> ARCEUS_PLATE_IDS = Set.of(
            ItemIDs.flamePlate,
            ItemIDs.splashPlate,
            ItemIDs.zapPlate,
            ItemIDs.meadowPlate,
            ItemIDs.iciclePlate,
            ItemIDs.fistPlate,
            ItemIDs.toxicPlate,
            ItemIDs.earthPlate,
            ItemIDs.skyPlate,
            ItemIDs.mindPlate,
            ItemIDs.insectPlate,
            ItemIDs.stonePlate,
            ItemIDs.spookyPlate,
            ItemIDs.dracoPlate,
            ItemIDs.dreadPlate,
            ItemIDs.ironPlate,
            ItemIDs.pixiePlate
    );
    private static final Set<String> ARCEUS_PLATE_NAMES = Set.of(
            "flameplate",
            "splashplate",
            "zapplate",
            "meadowplate",
            "icicleplate",
            "fistplate",
            "toxicplate",
            "earthplate",
            "skyplate",
            "mindplate",
            "insectplate",
            "stoneplate",
            "spookyplate",
            "dracoplate",
            "dreadplate",
            "ironplate",
            "pixieplate"
    );

    private static final Set<Integer> GENESECT_DRIVE_IDS = Set.of(
            ItemIDs.douseDrive,
            ItemIDs.shockDrive,
            ItemIDs.burnDrive,
            ItemIDs.chillDrive
    );
    private static final Set<String> GENESECT_DRIVE_NAMES = Set.of(
            "dousedrive",
            "shockdrive",
            "burndrive",
            "chilldrive"
    );

    private static final Set<Integer> SILVALLY_MEMORY_IDS = setupSilvallyMemoryIds();
    private static final Set<String> SILVALLY_MEMORY_NAMES = Set.of(
            "fightingmemory",
            "flyingmemory",
            "poisonmemory",
            "groundmemory",
            "rockmemory",
            "bugmemory",
            "ghostmemory",
            "steelmemory",
            "firememory",
            "watermemory",
            "grassmemory",
            "electricmemory",
            "psychicmemory",
            "icememory",
            "dragonmemory",
            "darkmemory",
            "fairymemory"
    );

    private static final Set<Integer> NECTAR_FORM_CHANGE_IDS = Set.of(
            ItemIDs.redNectar,
            ItemIDs.yellowNectar,
            ItemIDs.pinkNectar,
            ItemIDs.purpleNectar,
            ItemIDs.dNASplicersFuse,
            ItemIDs.dNASplicersSeparate,
            ItemIDs.revealGlass,
            ItemIDs.prisonBottle,
            ItemIDs.zygardeCube,
            ItemIDs.nSolarizerFuse,
            ItemIDs.nLunarizerFuse,
            ItemIDs.nSolarizerSeparate,
            ItemIDs.nLunarizerSeparate,
            ItemIDs.rotomCatalog,
            ItemIDs.reinsofUnityFuse,
            ItemIDs.reinsofUnitySeparate,
            ItemIDs.reinsofUnity,
            ItemIDs.griseousOrb,
            ItemIDs.adamantOrb,
            ItemIDs.lustrousOrb
    );
    private static final Set<String> NECTAR_FORM_CHANGE_NAMES = Set.of(
            "rednectar",
            "yellownectar",
            "pinknectar",
            "purplenectar",
            "dnasplicers",
            "dnasplicersfuse",
            "dnasplicersseparate",
            "revealglass",
            "prisonbottle",
            "zygardecube",
            "nsolarizer",
            "nlunarizer",
            "nsolarizerfuse",
            "nlunarizerfuse",
            "nsolarizerseparate",
            "nlunarizerseparate",
            "rotomcatalog",
            "reinsofunity",
            "reinsofunityfuse",
            "reinsofunityseparate",
            "griseousorb",
            "adamantorb",
            "lustrousorb"
    );

    private CfruDpeItemCategories() {
    }

    public static Set<ItemMechanicCategory> categoriesFor(Item item) {
        if (item == null) {
            return Collections.emptySet();
        }
        EnumSet<ItemMechanicCategory> categories = EnumSet.noneOf(ItemMechanicCategory.class);
        if (isMegaStone(item)) {
            categories.add(ItemMechanicCategory.MEGA_STONE);
        }
        if (isMegaAccessory(item)) {
            categories.add(ItemMechanicCategory.MEGA_ACCESSORY);
        }
        if (isZCrystal(item)) {
            categories.add(ItemMechanicCategory.Z_CRYSTAL);
        }
        if (isZAccessory(item)) {
            categories.add(ItemMechanicCategory.Z_ACCESSORY);
        }
        if (isDynamaxGmaxItem(item)) {
            categories.add(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
        }
        if (isArceusPlate(item)) {
            categories.add(ItemMechanicCategory.ARCEUS_PLATE);
        }
        if (isGenesectDrive(item)) {
            categories.add(ItemMechanicCategory.GENESECT_DRIVE);
        }
        if (isSilvallyMemory(item)) {
            categories.add(ItemMechanicCategory.SILVALLY_MEMORY);
        }
        if (isNectarOrFormChangeItem(item)) {
            categories.add(ItemMechanicCategory.NECTAR_FORM_CHANGE);
        }
        return Collections.unmodifiableSet(categories);
    }

    public static boolean isMegaEvolutionItem(Item item) {
        return isMegaStone(item) || isMegaAccessory(item);
    }

    public static boolean isZMoveItem(Item item) {
        return isZCrystal(item) || isZAccessory(item);
    }

    public static boolean isDynamaxGmaxItem(Item item) {
        if (item == null) {
            return false;
        }
        return DYNAMAX_GIGANTAMAX_IDS.contains(item.getId())
                || DYNAMAX_GIGANTAMAX_NAMES.contains(normalizedName(item));
    }

    public static boolean isArceusPlate(Item item) {
        if (item == null) {
            return false;
        }
        return ARCEUS_PLATE_IDS.contains(item.getId()) || ARCEUS_PLATE_NAMES.contains(normalizedName(item));
    }

    public static boolean isGenesectDrive(Item item) {
        if (item == null) {
            return false;
        }
        return GENESECT_DRIVE_IDS.contains(item.getId()) || GENESECT_DRIVE_NAMES.contains(normalizedName(item));
    }

    public static boolean isSilvallyMemory(Item item) {
        if (item == null) {
            return false;
        }
        return SILVALLY_MEMORY_IDS.contains(item.getId()) || SILVALLY_MEMORY_NAMES.contains(normalizedName(item));
    }

    public static boolean isNectarOrFormChangeItem(Item item) {
        if (item == null) {
            return false;
        }
        return NECTAR_FORM_CHANGE_IDS.contains(item.getId())
                || NECTAR_FORM_CHANGE_NAMES.contains(normalizedName(item));
    }

    public static int standardIdForSourceId(int cfruDpeSourceItemId) {
        return ItemIDs.UNIQUE_OFFSET + cfruDpeSourceItemId;
    }

    private static boolean isMegaStone(Item item) {
        String normalizedName = normalizedName(item);
        return Gen6Constants.megaStones.contains(item.getId())
                || MEGA_STONE_NAMES.contains(normalizedName)
                || (isSourceBlockStandardId(item.getId(), ITEM_VENUSAURITE, ITEM_DIANCITE)
                && MEGA_STONE_NAMES.contains(normalizedName));
    }

    private static boolean isMegaAccessory(Item item) {
        return MEGA_ACCESSORY_IDS.contains(item.getId()) || MEGA_ACCESSORY_NAMES.contains(normalizedName(item));
    }

    private static boolean isZCrystal(Item item) {
        String normalizedName = normalizedName(item);
        return Z_CRYSTAL_IDS.contains(item.getId())
                || Z_CRYSTAL_NAMES.contains(normalizedName)
                || ((item.getId() == standardIdForSourceId(ITEM_ULTRANECROZIUM_Z)
                || isSourceBlockStandardId(item.getId(), ITEM_NORMALIUM_Z, ITEM_TAPUNIUM_Z))
                && Z_CRYSTAL_NAMES.contains(normalizedName));
    }

    private static boolean isZAccessory(Item item) {
        return Z_ACCESSORY_IDS.contains(item.getId()) || Z_ACCESSORY_NAMES.contains(normalizedName(item));
    }

    private static boolean isSourceBlockStandardId(int standardItemId, int firstSourceItemId, int lastSourceItemId) {
        int firstStandardId = standardIdForSourceId(firstSourceItemId);
        int lastStandardId = standardIdForSourceId(lastSourceItemId);
        return standardItemId >= firstStandardId && standardItemId <= lastStandardId;
    }

    private static Set<Integer> setupZCrystalIds() {
        Set<Integer> ids = new HashSet<>();
        addBetween(ids, ItemIDs.normaliumZHeld, ItemIDs.pikaniumZHeld);
        addBetween(ids, ItemIDs.decidiumZHeld, ItemIDs.mewniumZHeld);
        addBetween(ids, ItemIDs.normaliumZBag, ItemIDs.pikashuniumZBag);
        addBetween(ids, ItemIDs.solganiumZBag, ItemIDs.kommoniumZBag);
        addBetween(ids, ItemIDs.mimikiumZHeld, ItemIDs.ultranecroziumZHeld);
        ids.add(ItemIDs.pikashuniumZHeld);
        ids.add(ItemIDs.ilimaNormaliumZ);
        return Collections.unmodifiableSet(ids);
    }

    private static Set<Integer> setupSilvallyMemoryIds() {
        Set<Integer> ids = new HashSet<>();
        addBetween(ids, ItemIDs.fightingMemory, ItemIDs.fairyMemory);
        return Collections.unmodifiableSet(ids);
    }

    private static void addBetween(Set<Integer> set, int start, int end) {
        for (int i = start; i <= end; i++) {
            set.add(i);
        }
    }

    private static String normalizedName(Item item) {
        String name = item.getName();
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
