package dev.unstablesky;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static dev.unstablesky.Util.item;
import static dev.unstablesky.Util.send;

/**
 * Kit menu: cooldowns (persisted as absolute expiry times), the kit grid, the confirmation
 * screen, and the 1-second live refresh of the cooldown lore. Mirrors the Skript kit script.
 */
public final class KitManager {

    /** Kit key -> cooldown seconds. Unknown kits have no cooldown. */
    private static final Map<String, Integer> COOLDOWNS = Map.of(
            "starter", 86400,
            "vip", 21600,
            "media", 3600,
            "unstablesky+", 10800,
            "unstablesky++", 3600,
            "overlord", 3600,
            "prestige", 5400,
            "invisknight", 3600);

    /** Grid layout: slot -> kit key. */
    private static final Map<Integer, String> SLOTS = new LinkedHashMap<>();
    static {
        SLOTS.put(10, "starter");
        SLOTS.put(11, "vip");
        SLOTS.put(12, "media");
        SLOTS.put(14, "unstablesky+");
        SLOTS.put(15, "unstablesky++");
        SLOTS.put(16, "overlord");
        SLOTS.put(20, "prestige");
        SLOTS.put(24, "invisknight");
    }

    /** Display names shown when the menu opens. */
    private static final Map<String, String> NAMES = Map.of(
            "starter", "ѕᴛᴀʀᴛᴇʀ ᴋɪᴛ",
            "vip", "ᴠɪᴘ ᴋɪᴛ",
            "media", "ᴍᴇᴅɪᴀ ᴋɪᴛ",
            "unstablesky+", "ѕᴋʏ-ѕᴍᴘ+ ᴋɪᴛ",
            "unstablesky++", "ѕᴋʏ-ѕᴍᴘ++ ᴋɪᴛ",
            "overlord", "ᴏᴠᴇʀʟᴏʀᴅ ᴋɪᴛ",
            "prestige", "ᴘʀᴇѕᴛɪɢᴇ ᴋɪᴛ",
            "invisknight", "ɪɴᴠɪѕᴋɴɪɢʜᴛ ᴋɪᴛ");

    /**
     * Names written by the 1-second refresh. The original script used different smallcaps
     * spellings for two kits here, so the refresh overwrites them; kept as-is for parity.
     */
    private static final Map<String, String> REFRESH_NAMES = new HashMap<>(NAMES);
    static {
        REFRESH_NAMES.put("unstablesky+", "ᴜɴsᴛᴀʙʟᴇsᴋʏ+ ᴋɪᴛ");
        REFRESH_NAMES.put("unstablesky++", "ᴜɴsᴛᴀʙʟᴇsᴋʏ++ ᴋɪᴛ");
    }

    private static final Map<String, Material> MATERIALS = Map.of(
            "starter", Material.IRON_SWORD,
            "vip", Material.GOLD_INGOT,
            "media", Material.AMETHYST_SHARD,
            "unstablesky+", Material.DIAMOND,
            "unstablesky++", Material.DIAMOND_BLOCK,
            "overlord", Material.NETHERITE_INGOT,
            "prestige", Material.NETHER_STAR,
            "invisknight", Material.ENDER_PEARL);

    private final Data data;
    private final Map<UUID, String> pendingConfirm = new HashMap<>();

    public KitManager(Data data) {
        this.data = data;
    }

    // ------------------------------------------------------------ cooldowns

    /** Remaining seconds for a kit, or 0 when ready. Clears expired entries. */
    private long remaining(UUID id, String kit) {
        long expiry = data.getKitExpiry(id, kit);
        if (expiry == 0L) return 0L;
        long left = expiry - Util.nowSeconds();
        if (left <= 0) {
            data.clearKitExpiry(id, kit);
            data.save();
            return 0L;
        }
        return left;
    }

    private static String hms(long remaining) {
        long h = remaining / 3600;
        long m = (remaining - h * 3600) / 60;
        long s = remaining - h * 3600 - m * 60;
        return h + "h " + m + "m " + s + "s";
    }

    private String cooldownText(UUID id, String kit) {
        long left = remaining(id, kit);
        if (left <= 0) return "&a&lREADY!";
        return "&c&lCOOLDOWN: &f" + hms(left);
    }

    // ------------------------------------------------------------ menu

    public void openMenu(Player p) {
        MenuHolder h = new MenuHolder(MenuHolder.Kind.KIT_MENU);
        Inventory inv = Bukkit.createInventory(h, 27, Util.text("KITS"));
        h.setInventory(inv);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        inv.setItem(4, item(Material.NETHER_STAR, "ᴋɪᴛѕ", "&7Choose a kit below."));
        for (Map.Entry<Integer, String> e : SLOTS.entrySet()) {
            String kit = e.getValue();
            inv.setItem(e.getKey(), item(MATERIALS.get(kit), NAMES.get(kit),
                    "&7Left-click &8-> &fClaim kit", "&7Right-click &8-> &fEdit kit", "", cooldownText(p.getUniqueId(), kit)));
        }
        p.openInventory(inv);
    }

    /** Refreshes only the lore (and the refresh-name spelling) while the kit menu is open. */
    public void refreshMenu(Player p, Inventory inv) {
        for (Map.Entry<Integer, String> e : SLOTS.entrySet()) {
            String kit = e.getValue();
            inv.setItem(e.getKey(), item(MATERIALS.get(kit), REFRESH_NAMES.get(kit),
                    "&7Left-click &8-> &fClaim kit", "&7Right-click &8-> &fEdit kit", "", cooldownText(p.getUniqueId(), kit)));
        }
    }

    private void openConfirm(Player p, String kit) {
        pendingConfirm.put(p.getUniqueId(), kit);
        MenuHolder h = new MenuHolder(MenuHolder.Kind.KIT_CONFIRM, null, 0, kit);
        Inventory inv = Bukkit.createInventory(h, 27, Util.text("ᴄᴏɴꜰɪʀᴍ ᴋɪᴛ"));
        h.setInventory(inv);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        inv.setItem(13, item(Material.PAPER, "&c&lBE CAREFUL!",
                "&cUsing this kit will replace your current inventory!", "", "&7Kit: &f" + kit));
        inv.setItem(11, item(Material.LIME_STAINED_GLASS_PANE, "&a&lYES", "&7Click to claim this kit."));
        inv.setItem(15, item(Material.RED_STAINED_GLASS_PANE, "&c&lNO", "&7Click to cancel."));
        p.openInventory(inv);
    }

    // ------------------------------------------------------------ clicks

    public void handleMenuClick(Player p, int slot, ClickType click) {
        String kit = SLOTS.get(slot);
        if (kit == null) return;
        if (click.isRightClick()) {
            p.closeInventory();
            p.performCommand("catkits edit " + kit);
        } else if (click.isLeftClick()) {
            openConfirm(p, kit);
        }
    }

    public void handleConfirmClick(Player p, int slot) {
        if (slot == 15) {
            pendingConfirm.remove(p.getUniqueId());
            p.closeInventory();
            return;
        }
        if (slot != 11) return;

        UUID id = p.getUniqueId();
        String kit = pendingConfirm.get(id);
        if (kit == null) {
            p.closeInventory();
            return;
        }

        long left = remaining(id, kit);
        if (left > 0) {
            send(p, "&c&lKIT ON COOLDOWN!");
            send(p, "&7You can use this kit again in &f" + hms(left) + "&7.");
            p.closeInventory();
            pendingConfirm.remove(id);
            return;
        }

        Integer cooldown = COOLDOWNS.getOrDefault(kit, 0);
        if (cooldown > 0) {
            data.setKitExpiry(id, kit, Util.nowSeconds() + cooldown);
            data.save();
        }

        pendingConfirm.remove(id);
        p.closeInventory();
        p.performCommand("catkits " + kit);
    }

    public void forgetPlayer(UUID id) {
        pendingConfirm.remove(id);
    }
}
