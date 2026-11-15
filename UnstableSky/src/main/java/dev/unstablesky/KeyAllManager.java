package dev.unstablesky;

import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.unstablesky.Util.send;

/**
 * KeyAll timer: per-player countdown by rank (with a separate AFK-world interval),
 * the key give via the crates console command, /keyall, and the AFK world teleport.
 */
public final class KeyAllManager {

    private static final String AFK_WORLD = "afk";
    private static final double AFK_TOLERANCE = 0.1;

    private final UnstableSkyPlugin plugin;
    private final Data data;

    private final Map<UUID, Integer> timers = new HashMap<>();
    private final Map<UUID, Boolean> inAfk = new HashMap<>();
    private final Map<UUID, String> display = new HashMap<>();
    private final Set<UUID> afkTeleporting = new HashSet<>();

    public KeyAllManager(UnstableSkyPlugin plugin, Data data) {
        this.plugin = plugin;
        this.data = data;
    }

    // ------------------------------------------------------------ intervals

    private int cfg(String key) {
        return plugin.getConfig().getInt("keyall." + key);
    }

    /** Interval outside the AFK world, by rank (checked in the original order). */
    private int intervalOutside(Player p) {
        if (p.hasPermission("group.ascendant")) return cfg("ascendant-interval");
        if (p.hasPermission("group.prestige")) return cfg("prestige-interval");
        if (p.hasPermission("group.overlord")) return cfg("overlord-interval");
        if (p.hasPermission("group.skysmper++")) return cfg("skysmperplusplus-interval");
        if (p.hasPermission("group.skysmper+")) return cfg("skysmperplus-interval");
        if (p.hasPermission("group.media")) return cfg("media-interval");
        if (p.hasPermission("group.vip")) return cfg("vip-interval");
        return cfg("normal-interval");
    }

    /** Interval inside the AFK world, by rank. Ranks without an AFK value use the base AFK interval. */
    private int intervalAfk(Player p) {
        if (p.hasPermission("group.ascendant")) return cfg("ascendant-afk-interval");
        if (p.hasPermission("group.prestige")) return cfg("prestige-afk-interval");
        if (p.hasPermission("group.overlord")) return cfg("overlord-afk-interval");
        if (p.hasPermission("group.media")) return cfg("media-afk-interval");
        return cfg("afk-interval");
    }

    private static boolean inAfkWorld(Player p) {
        return p.getWorld().getName().equals(AFK_WORLD);
    }

    public void onJoin(Player p) {
        timers.put(p.getUniqueId(), intervalOutside(p));
        inAfk.put(p.getUniqueId(), false);
    }

    public void onQuit(Player p) {
        afkTeleporting.remove(p.getUniqueId());
    }

    // ------------------------------------------------------------ engine

    /** Runs once per second for all online players. */
    public void tick() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            if (!timers.containsKey(id)) {
                timers.put(id, intervalOutside(p));
            }

            boolean afkWorld = inAfkWorld(p);
            if (afkWorld) {
                if (!Boolean.TRUE.equals(inAfk.get(id))) {
                    inAfk.put(id, true);
                    timers.put(id, intervalAfk(p));
                }
            } else if (Boolean.TRUE.equals(inAfk.get(id))) {
                inAfk.put(id, false);
                timers.put(id, intervalOutside(p));
            }

            int remaining = timers.get(id) - 1;
            timers.put(id, remaining);

            if (remaining <= 0) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        "crates key give " + p.getName() + " keyall");
                send(p, "&aYou received a &eKeyAll Key&a!");
                timers.put(id, afkWorld ? intervalAfk(p) : intervalOutside(p));
            }

            display.put(id, Util.mmss(timers.get(id)));
        }
    }

    /** Value used by the PlaceholderAPI %sky_keytimer% placeholder. */
    public String displayFor(UUID id) {
        return display.getOrDefault(id, "");
    }

    // ------------------------------------------------------------ /keyall

    /** Sets every online player's timer. Returns false when the value is out of range. */
    public boolean setAll(int seconds) {
        if (seconds < 1 || seconds > 600) return false;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            timers.put(p.getUniqueId(), seconds);
        }
        return true;
    }

    // ------------------------------------------------------------ /afkset and /afk

    public void setAfkLocation(Player p) {
        data.setAfkLocation(p.getLocation());
        data.save();
        send(p, "&aAFK location set.");
    }

    public void teleportToAfk(Player p) {
        Location afk = data.getAfkLocation();
        if (afk == null) {
            send(p, "&cThe AFK location has not been set.");
            return;
        }
        UUID id = p.getUniqueId();
        if (afkTeleporting.contains(id)) return;

        afkTeleporting.add(id);
        Location start = p.getLocation();
        showTitle(p, "&c&l3", "&7Teleporting to AFK...");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> afkStep(id, start, 2), 20L);
    }

    /** n = 2: check + "2"; n = 3: check + "1"; n = 4: check + teleport. */
    private void afkStep(UUID id, Location start, int n) {
        Player p = plugin.getServer().getPlayer(id);
        if (p == null) {
            afkTeleporting.remove(id);
            return;
        }
        if (p.getLocation().distance(start) > AFK_TOLERANCE) {
            afkTeleporting.remove(id);
            send(p, "&cAFK teleport cancelled because you moved.");
            return;
        }

        if (n == 2) {
            showTitle(p, "&e&l2", "&7Don't move!");
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> afkStep(id, start, 3), 20L);
        } else if (n == 3) {
            showTitle(p, "&a&l1", "&7Get ready...");
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> afkStep(id, start, 4), 20L);
        } else {
            Location afk = data.getAfkLocation();
            if (afk != null) {
                p.teleport(afk);
            }
            p.performCommand("essentials:afk");
            showTitle(p, "&a&lTELEPORTED", "&7You are now in the AFK world.");
            afkTeleporting.remove(id);
        }
    }

    private static void showTitle(Player p, String title, String subtitle) {
        p.showTitle(Title.title(Util.text(title), Util.text(subtitle)));
    }
}
