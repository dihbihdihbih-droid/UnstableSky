package dev.unstablesky;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Identifies which UnstableSky menu an inventory belongs to. Using a holder instead of matching
 * titles keeps the same visible titles while making click handling exact.
 */
public final class MenuHolder implements InventoryHolder {

    public enum Kind {
        MUSIC_MAIN, MUSIC_SETTINGS, MUSIC_ARTIST, MUSIC_PLAYLIST,
        KIT_MENU, KIT_CONFIRM,
        WARP_MENU
    }

    private final Kind kind;
    private final Songs.Artist artist;
    private final int page;
    private final String kit;
    /** Warp menu: inventory slot -> warp name for the current page. */
    private final Map<Integer, String> slotToWarp = new HashMap<>();
    private Inventory inventory;

    public MenuHolder(Kind kind) {
        this(kind, null, 0, null);
    }

    public MenuHolder(Kind kind, Songs.Artist artist, int page, String kit) {
        this.kind = kind;
        this.artist = artist;
        this.page = page;
        this.kit = kit;
    }

    public Kind kind() { return kind; }
    public Songs.Artist artist() { return artist; }
    public int page() { return page; }
    public String kit() { return kit; }
    public Map<Integer, String> slotToWarp() { return slotToWarp; }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
