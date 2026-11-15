package dev.unstablesky;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.unstablesky.Util.send;

/**
 * Warp list, the 3-second /warp countdown (cancelled by moving more than 0.2 blocks),
 * and the /warps blocking. Intercepts /warp in the preprocess event like the Skript did.
 */
public final class WarpManager {

    public record WarpDef(Material material, String display, String from, String to) {}

    /** Ordered warp list (the order defines the GUI pages). */
    public static final List<String> WARPS = new ArrayList<>();
    private static final Map<String, WarpDef> DEFS = new HashMap<>();

    private static void w(String name, Material mat, String display, String c1, String c2) {
        WARPS.add(name);
        DEFS.put(name, new WarpDef(mat, display, c1, c2));
    }

    static {
        // ---- page 1
        w("2v1000", Material.DIAMOND_SWORD, "2v1000", "#5CF2E6", "#1AA7B5");
        w("banditcave", Material.IRON_SWORD, "BanditCave", "#E8E8E8", "#9A9A9A");
        w("batbase", Material.PHANTOM_MEMBRANE, "BatBase", "#D9D9A0", "#7A8F6A");
        w("blackmarket", Material.BARREL, "BlackMarket", "#B5895A", "#7A5632");
        w("capitalcity", Material.GOLD_BLOCK, "CapitalCity", "#FFE55C", "#E0A800");
        w("caveciv", Material.STONE, "CaveCiv", "#B0B0B0", "#6E6E6E");
        w("cindercrest", Material.MAGMA_BLOCK, "CinderCrest", "#FF8A1F", "#B32000");
        w("clownciv", Material.RED_WOOL, "ClownCiv", "#FF4A4A", "#A80000");
        w("darkforest", Material.DARK_OAK_LOG, "DarkForest", "#6B4A2B", "#3A2614");
        w("end", Material.ENDER_EYE, "End", "#2BD9A0", "#0F6B66");
        w("farlands", Material.GRASS_BLOCK, "FarLands", "#7BD944", "#4C8F2A");
        w("flame'sbase", Material.BLAZE_ROD, "FlamesBase", "#FFD23F", "#E8700A");
        w("fortfeather", Material.FEATHER, "FortFeather", "#FFFFFF", "#C9D3DD");
        w("highwater", Material.WATER_BUCKET, "HighWater", "#5BB8FF", "#1E5BD8");
        w("jadenscastle", Material.DIAMOND_BLOCK, "JadensCastle", "#7DFFF2", "#1FB8C4");
        w("mafia", Material.TNT, "Mafia", "#FF5A4A", "#C41E1E");
        w("manetree", Material.OAK_SAPLING, "ManeTree", "#8EDB5A", "#3E8E2E");
        w("merchantarena", Material.EMERALD, "MerchantArena", "#5CFF8A", "#0FA84A");
        w("merchantcity", Material.EMERALD_BLOCK, "MerchantCity", "#3DFF7A", "#0B7A36");
        w("mist", Material.WHITE_STAINED_GLASS, "Mist", "#FFFFFF", "#BFD4E8");
        w("mistrulcave", Material.COBBLESTONE, "MistrulCave", "#A8A8A8", "#6B6B6B");
        w("northern", Material.SNOW_BLOCK, "Northern", "#FFFFFF", "#BEE3F5");
        w("null", Material.OBSIDIAN, "Null", "#8A4FFF", "#2A0A5E");
        w("nullcapital", Material.CRYING_OBSIDIAN, "NullCapital", "#C060FF", "#4A0F9A");
        w("NullEndBase", Material.END_STONE, "NullEndBase", "#F7F3B5", "#D9D27A");
        w("pirateciv", Material.CHEST, "PirateCiv", "#D9A05B", "#8A5A1F");
        w("redstone", Material.REDSTONE_BLOCK, "Redstone", "#FF3B3B", "#8F0000");

        // ---- page 2
        w("spawn", Material.NETHER_STAR, "UnstableSpawn", "#FFFFFF", "#E8E27A");
        w("spokesbase", Material.BEACON, "SpokesBase", "#7DFFF5", "#3E8CFF");
        w("steampunk", Material.COPPER_BLOCK, "Steampunk", "#FFA06A", "#C4532B");
        w("tavern", Material.BARREL, "Tavern", "#B5895A", "#7A5632");
        w("theo_sprison", Material.IRON_BARS, "TheosPrison", "#CFCFCF", "#6F6F6F");
        w("wemmbu_egg", Material.PURPLE_CONCRETE, "WemmbuTree", "#B24DFF", "#6B1FA8");
        w("wemmbuciv", Material.DRAGON_EGG, "WemmbuCiv", "#B060FF", "#2A0A4A");
        w("wheatciv", Material.WHEAT, "WheatCiv", "#FFE27A", "#D4A017");
        w("wifiesbase", Material.REDSTONE_LAMP, "WifiesBase", "#FFD97A", "#B8651B");
        w("zamempire", Material.EMERALD_BLOCK, "ZamEmpire", "#3DFF7A", "#0B7A36");
        w("underworld", Material.MUD, "Underworld", "#7A5A4A", "#3E2B22");
        w("antagg", Material.REDSTONE, "antagg", "#FF4040", "#A00000");
        w("PurgatoryBlackmarket", Material.BLACKSTONE, "PurgatoryBlackmarket", "#5A5060", "#2A2530");
        w("ArachnidCiv", Material.COBWEB, "ArachnidCiv", "#FFFFFF", "#B5B5B5");
        w("Wraith", Material.SOUL_LANTERN, "Wraith", "#6FE8FF", "#2A9CC4");
        w("BatHQ", Material.PHANTOM_MEMBRANE, "BatHQ", "#D9D9A0", "#7A8F6A");
        w("Garden", Material.FLOWERING_AZALEA, "Garden", "#FF9AE0", "#5DBB3C");
        w("GreatSea", Material.SAND, "GreatSea", "#F5E6A8", "#D8C27A");
        w("Amplified", Material.SCULK, "Amplified", "#1FB5C4", "#08323A");
        w("Lintower", Material.QUARTZ_BLOCK, "Lintower", "#FFFFFF", "#E0D8D0");
        w("warpforest", Material.OAK_LEAVES, "WarpForest", "#6FD24A", "#2F7A22");
        w("farlandsportal", Material.END_PORTAL_FRAME, "FarLandsPortal", "#7BE6B0", "#2D6A4F");
        w("farlandstemple", Material.SANDSTONE, "FarLandsTemple", "#F0E0A0", "#C9B170");
        w("PyramidOfAnubis", Material.SANDSTONE, "PyramidAnubis", "#F0E0A0", "#C9B170");
        w("FarlandsPalaceCog", Material.QUARTZ_BLOCK, "FarlandsPalace/Cog", "#FFFFFF", "#E0D8D0");
        w("boosfer", Material.CRAFTING_TABLE, "BoosferFactory", "#C98F52", "#7A4A22");
        w("bridges", Material.OAK_PLANKS, "SpokeLandBridges", "#D4A864", "#9A6E36");
        w("AntaggArena", Material.IRON_SWORD, "AntaggArena", "#E8E8E8", "#9A9A9A");
        w("AntaggCastle", Material.STONE_BRICKS, "AntaggCastle", "#A8A8A8", "#6A6A6A");
        w("WoodenCiv", Material.OAK_LOG, "WoodenCiv", "#A57C3F", "#5E4220");
        w("DionesBase", Material.DIAMOND_BLOCK, "DionesBase", "#7DFFF2", "#1FB8C4");
        w("CanaryCiv", Material.YELLOW_WOOL, "CanaryCiv", "#FFEF4A", "#E0B400");
        w("GodOfWar", Material.NETHERITE_SWORD, "GodOfWar", "#6B5A66", "#2A2228");
        w("Proton", Material.LIGHTNING_ROD, "Proton", "#FFB27A", "#C4602B");
        w("MafiaStash", Material.CHEST, "MafiaStash", "#D9A05B", "#8A5A1F");

        // ---- added warps
        w("anubis", Material.GOLD_INGOT, "Anubis", "#FFE27A", "#C9A227");
        w("BatMeetingArea", Material.LECTERN, "BatMeetingArea", "#D9D9A0", "#7A8F6A");
        w("endfarlands", Material.ENDER_PEARL, "EndFarlands", "#7BE6B0", "#2D6A4F");
        w("LawBase", Material.BOOKSHELF, "LawBase", "#D4A864", "#7A4A22");
        w("mainspawn", Material.COMPASS, "MainSpawn", "#FFFFFF", "#E8E27A");
        w("monarch", Material.GOLDEN_HELMET, "Monarch", "#FFE55C", "#E0A800");
        w("purge", Material.FLINT_AND_STEEL, "Purge", "#FF8A1F", "#B32000");
        w("PyramidAnubis", Material.SANDSTONE, "PyramidAnubis", "#F0E0A0", "#C9B170");
        w("skymore", Material.ELYTRA, "Skymore", "#9AD8FF", "#4A7FD8");
        w("SpokeEndBase", Material.PURPUR_BLOCK, "SpokeEndBase", "#D9A0E8", "#8A4FA8");
        w("TheosTrainingGround", Material.BOW, "TheosTrainingGround", "#CFCFCF", "#6F6F6F");
        w("VoidTown", Material.BLACK_CONCRETE, "VoidTown", "#5A5060", "#1A1520");
        w("warith", Material.WITHER_SKELETON_SKULL, "Warith", "#8A8A9A", "#3A3A4A");
        w("wartsforest", Material.NETHER_WART, "WartsForest", "#E04A4A", "#8A1F1F");
        w("wormhole", Material.CHORUS_FRUIT, "Wormhole", "#B24DFF", "#2A0A5E");
        w("lawgraveyard", Material.BONE, "LawGraveyard", "#E8E8E8", "#8A8A9A");
        w("NullPerimiter", Material.AMETHYST_SHARD, "NullPerimiter", "#C060FF", "#4A0F9A");
        w("WemmbusTrap", Material.TRIPWIRE_HOOK, "WemmbusTrap", "#B24DFF", "#6B1FA8");
    }

    /** Visual definition for a warp; unknown warps use paper, like the original fallback. */
    public static WarpDef def(String warp) {
        WarpDef d = DEFS.get(warp);
        return d != null ? d : new WarpDef(Material.PAPER, warp, "#FFFFFF", "#C8C8C8");
    }

    // ------------------------------------------------------------ state

    private static final double MOVE_TOLERANCE = 0.2;

    private static final class Countdown {
        final Location start;
        final String warp;
        Countdown(Location start, String warp) { this.start = start; this.warp = warp; }
    }

    private final UnstableSkyPlugin plugin;
    private final Map<UUID, Countdown> countdowns = new HashMap<>();
    private final Set<UUID> bypass = new HashSet<>();

    public WarpManager(UnstableSkyPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isTeleporting(UUID id) {
        return countdowns.containsKey(id);
    }

    /** Shared entry point for /warp <name> and for clicks in the warp GUI. */
    public void requestWarp(Player p, String warp) {
        if (isTeleporting(p.getUniqueId())) {
            send(p, "&c&lWARP &8» &7You already have a teleport in progress.");
            return;
        }
        startCountdown(p, warp);
    }

    private void startCountdown(Player p, String warp) {
        countdowns.put(p.getUniqueId(), new Countdown(p.getLocation(), warp));
        send(p, "");
        send(p, "&c&lWARP &8» &7Teleporting to &f" + warp + " &7in &c3&7...");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> step(p.getUniqueId(), 2), 20L);
    }

    /** Step 2 and 3 announce the countdown; step 4 performs the teleport. */
    private void step(UUID id, int n) {
        Countdown c = countdowns.get(id);
        if (c == null) return;
        Player p = plugin.getServer().getPlayer(id);
        if (p == null) {
            countdowns.remove(id);
            return;
        }
        if (moved(p, c)) {
            cancelMoved(p, id);
            return;
        }

        if (n == 2) {
            send(p, "&c&lWARP &8» &7Teleporting to &f" + c.warp + " &7in &c2&7...");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> step(id, 3), 20L);
        } else if (n == 3) {
            send(p, "&c&lWARP &8» &7Teleporting to &f" + c.warp + " &7in &c1&7...");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.4f);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> step(id, 4), 20L);
        } else {
            countdowns.remove(id);
            bypass.add(id);
            try {
                p.performCommand("warp " + c.warp);
            } finally {
                bypass.remove(id);
            }
        }
    }

    private boolean moved(Player p, Countdown c) {
        return p.getLocation().distance(c.start) > MOVE_TOLERANCE;
    }

    private void cancelMoved(Player p, UUID id) {
        countdowns.remove(id);
        send(p, "&c&lWARP &8» &7Teleport cancelled because you moved.");
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    // ------------------------------------------------------------ listeners

    /** Handles /warp, the blocked warps commands. Returns nothing; cancels the event when needed. */
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().startsWith("/") ? event.getMessage().substring(1) : event.getMessage();
        String[] parts = raw.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return;
        String label = parts[0].toLowerCase();
        Player p = event.getPlayer();

        if (label.equals("warp")) {
            if (bypass.contains(p.getUniqueId())) return;
            if (parts.length < 2) return;
            event.setCancelled(true);
            requestWarp(p, parts[1]);
            return;
        }

        if (label.equals("warps") || label.equals("essentials:warps") || label.equals("essentials:warp")) {
            event.setCancelled(true);
            send(p, "&cYou cannot use /" + label + ".");
        }
    }

    public void onMove(PlayerMoveEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Countdown c = countdowns.get(id);
        if (c == null) return;
        if (moved(event.getPlayer(), c)) {
            cancelMoved(event.getPlayer(), id);
        }
    }

    public void forgetPlayer(UUID id) {
        countdowns.remove(id);
        bypass.remove(id);
    }
}
