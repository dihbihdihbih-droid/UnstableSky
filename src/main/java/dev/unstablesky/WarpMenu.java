package dev.unstablesky;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static dev.unstablesky.Util.item;

/** Warp GUI: three pages of warp icons with navigation. */
public final class WarpMenu {

    private static final String TITLE = "&8✦ &c&lUNSTABLE SKY WARPS &8✦";
    private static final int[] WARP_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    /** Pages show 28 icons each, starting at (page - 1) * 27 + 1 (kept from the original). */
    private static final int PAGE_OFFSET = 27;
    private static final String TOTAL_TEXT = "80";

    private final WarpManager warps;

    public WarpMenu(WarpManager warps) {
        this.warps = warps;
    }

    public void open(Player p, int page) {
        MenuHolder h = new MenuHolder(MenuHolder.Kind.WARP_MENU, null, page, null);
        Inventory inv = Bukkit.createInventory(h, 54, Util.text(TITLE + " &7(Page " + page + ")"));
        h.setInventory(inv);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, item(Material.RED_STAINED_GLASS_PANE, " "));
            inv.setItem(i + 45, item(Material.RED_STAINED_GLASS_PANE, " "));
        }

        List<String> all = WarpManager.WARPS;
        int start = (page - 1) * PAGE_OFFSET;
        for (int i = 0; i < WARP_SLOTS.length; i++) {
            int index = start + i;
            if (index >= all.size()) break;
            String warp = all.get(index);
            int slot = WARP_SLOTS[i];
            inv.setItem(slot, warpItem(warp));
            h.slotToWarp().put(slot, warp);
        }

        if (page == 1) {
            inv.setItem(48, item(Material.ARROW, "&c&lPrevious Page", "&7You are already on page 1."));
            inv.setItem(50, item(Material.ARROW, "&a&lNext Page", "&7Click to view page 2."));
        } else {
            inv.setItem(48, item(Material.ARROW, "&e&lPrevious Page", "&7Click to go to the previous page."));
            if (page == 2) {
                inv.setItem(50, item(Material.ARROW, "&a&lNext Page", "&7Click to view page 3."));
            } else {
                inv.setItem(50, item(Material.BARRIER, "&c&lNo More Pages", "&7You are on the last page."));
            }
        }
        inv.setItem(49, item(Material.BARRIER, "&c&lClose", "&7Click to close the menu."));
        inv.setItem(4, item(Material.NETHER_STAR, "&c&lWarp Menu", "",
                "&7Page &f" + page + " &7of &f3&7.", "&7Total warps: &f" + TOTAL_TEXT, ""));

        p.openInventory(inv);
    }

    private ItemStack warpItem(String warp) {
        WarpManager.WarpDef def = WarpManager.def(warp);
        String sc = Util.smallcaps(def.display());
        ItemStack stack = new ItemStack(def.material());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(
                    "<!italic><bold><gradient:" + def.from() + ":" + def.to() + ">" + sc + "</gradient></bold>"));
            meta.lore(List.of(
                    Util.text(""),
                    Util.text("&7Teleport to &f" + sc + "&7."),
                    Util.text(""),
                    Util.text("&e&lCLICK TO TELEPORT!")));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public void handleClick(Player p, MenuHolder holder, int slot, ClickType click) {
        String warp = holder.slotToWarp().get(slot);
        if (warp != null) {
            p.closeInventory();
            warps.requestWarp(p, warp);
            return;
        }
        switch (slot) {
            case 48 -> {
                if (holder.page() == 3) {
                    open(p, 2);
                } else if (holder.page() == 2) {
                    open(p, 1);
                }
            }
            case 49 -> p.closeInventory();
            case 50 -> {
                if (holder.page() == 1) {
                    open(p, 2);
                } else if (holder.page() == 2) {
                    open(p, 3);
                }
            }
            default -> { }
        }
    }
}
