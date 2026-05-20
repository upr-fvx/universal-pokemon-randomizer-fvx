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

public final class ItemMechanicPredicates {

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

    private static final Set<Integer> Z_CRYSTAL_IDS = setupZCrystalIds();
    private static final Set<Integer> Z_ACCESSORY_IDS = Set.of(ItemIDs.zRing, ItemIDs.zPowerRing);
    private static final int CFRU_DPE_ULTRANECROZIUM_Z = 0x214;
    private static final int CFRU_DPE_MEGA_STONE_FIRST = 0x215;
    private static final int CFRU_DPE_MEGA_STONE_LAST = 0x243;
    private static final int CFRU_DPE_Z_CRYSTAL_FIRST = 0x244;
    private static final int CFRU_DPE_Z_CRYSTAL_LAST = 0x265;
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

    private static final Set<Integer> DYNAMAX_GIGANTAMAX_IDS = Set.of(
            ItemIDs.wishingStar,
            ItemIDs.dynamaxBand,
            ItemIDs.dynamaxCandy,
            ItemIDs.wishingPiece,
            ItemIDs.wishingChip,
            ItemIDs.maxHoney,
            ItemIDs.maxMushrooms,
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
            "dyniteore"
    );

    private ItemMechanicPredicates() {
    }

    public static boolean isItemAllowed(Item item, ItemMechanicExclusionOptions options) {
        if (item == null) {
            return false;
        }
        ItemMechanicExclusionOptions effectiveOptions = options == null ? ItemMechanicExclusionOptions.defaults() : options;
        Set<ItemMechanicCategory> categories = categoriesFor(item);
        if (!effectiveOptions.isIncludeMegaMechanicItems()
                && (categories.contains(ItemMechanicCategory.MEGA_STONE)
                || categories.contains(ItemMechanicCategory.MEGA_ACCESSORY))) {
            return false;
        }
        if (!effectiveOptions.isIncludeZCrystalItems()
                && (categories.contains(ItemMechanicCategory.Z_CRYSTAL)
                || categories.contains(ItemMechanicCategory.Z_ACCESSORY))) {
            return false;
        }
        return effectiveOptions.isIncludeDynamaxGigantamaxItems()
                || !categories.contains(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
    }

    public static Set<ItemMechanicCategory> categoriesFor(Item item) {
        if (item == null) {
            return Collections.emptySet();
        }
        EnumSet<ItemMechanicCategory> categories = EnumSet.noneOf(ItemMechanicCategory.class);
        categories.addAll(item.getMechanicCategories());
        int id = item.getId();
        String normalizedName = normalizedName(item);
        if (Gen6Constants.megaStones.contains(id)
                || (id >= CFRU_DPE_MEGA_STONE_FIRST && id <= CFRU_DPE_MEGA_STONE_LAST)
                || MEGA_STONE_NAMES.contains(normalizedName)) {
            categories.add(ItemMechanicCategory.MEGA_STONE);
        }
        if (MEGA_ACCESSORY_IDS.contains(id) || MEGA_ACCESSORY_NAMES.contains(normalizedName)) {
            categories.add(ItemMechanicCategory.MEGA_ACCESSORY);
        }
        if (Z_CRYSTAL_IDS.contains(id) || Z_CRYSTAL_NAMES.contains(normalizedName)) {
            categories.add(ItemMechanicCategory.Z_CRYSTAL);
        }
        if (Z_ACCESSORY_IDS.contains(id) || Z_ACCESSORY_NAMES.contains(normalizedName)) {
            categories.add(ItemMechanicCategory.Z_ACCESSORY);
        }
        if (DYNAMAX_GIGANTAMAX_IDS.contains(id) || DYNAMAX_GIGANTAMAX_NAMES.contains(normalizedName)) {
            categories.add(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
        }
        return Collections.unmodifiableSet(categories);
    }

    public static boolean isMegaMechanicItem(Item item) {
        Set<ItemMechanicCategory> categories = categoriesFor(item);
        return categories.contains(ItemMechanicCategory.MEGA_STONE)
                || categories.contains(ItemMechanicCategory.MEGA_ACCESSORY);
    }

    public static boolean isZMechanicItem(Item item) {
        Set<ItemMechanicCategory> categories = categoriesFor(item);
        return categories.contains(ItemMechanicCategory.Z_CRYSTAL)
                || categories.contains(ItemMechanicCategory.Z_ACCESSORY);
    }

    public static boolean isDynamaxGigantamaxItem(Item item) {
        return categoriesFor(item).contains(ItemMechanicCategory.DYNAMAX_GIGANTAMAX);
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
        ids.add(CFRU_DPE_ULTRANECROZIUM_Z);
        addBetween(ids, CFRU_DPE_Z_CRYSTAL_FIRST, CFRU_DPE_Z_CRYSTAL_LAST);
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
