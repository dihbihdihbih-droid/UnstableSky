package dev.unstablesky;

import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.UUID;

import static dev.unstablesky.Util.send;

/**
 * ClearLag countdown, announcements, entity clearing and the personal broadcast toggle.
 * The clear replaces the original "killall mobs/drops/xp <world>" console commands.
 */
public final class ClearLagManager {

    private static final String PREFIX = "&6UNSTABLESKY: &f";
    private static final String USAGE = "&cUsage: /clearlag or /clearlag time set <1s-600s> or <1m-10m>";
    private static final String RANGE_ERROR = "&cUNSTABLESKY: Time must be between 1 second and 10 minutes!";
    /** Worlds cleared by the original script. Missing worlds are skipped. */
    private static final List<String> CLEAR_WORLDS = List.of(
            "unstablespawn", "unstablespawn_nether", "unstablespawn_the_end", "Spawn", "nethervoid");

    private final UnstableSkyPlugin plugin;
    private final Data data;
    private final int defaultTime;
    private int time;

    public ClearLagManager(UnstableSkyPlugin plugin, Data data) {
        this.plugin = plugin;
        this.data = data;
        this.defaultTime = plugin.getConfig().getInt("clearlag.default-time", 600);
        this.time = data.getClearlagTime(defaultTime);
    }

    // ------------------------------------------------------------ broadcast

    /** Sends to every player who has not turned broadcasts off (enabled by default). */
    public void broadcast(String message) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            Boolean setting = data.getBroadcastSetting(p.getUniqueId());
            if (setting == null || setting) {
                send(p, message);
            }
        }
    }

    /** /unstableskybroadcast: a never-set player is switched to disabled on first use. */
    public void toggleBroadcast(Player p) {
        UUID id = p.getUniqueId();
        Boolean setting = data.getBroadcastSetting(id);
        if (setting == null || setting) {
            data.setBroadcastSetting(id, false);
            data.save();
            send(p, "&cUNSTABLESKY: ClearLag broadcasts disabled.");
        } else {
            data.setBroadcastSetting(id, true);
            data.save();
            send(p, "&aUNSTABLESKY: ClearLag broadcasts enabled.");
        }
    }

    // ------------------------------------------------------------ /clearlag

    public void command(Player p, String[] args) {
        if (args.length == 0) {
            send(p, "&6UNSTABLESKY: &fClearlag in &6" + time + " seconds&f.");
            return;
        }
        if (!args[0].equalsIgnoreCase("time")) {
            send(p, USAGE);
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("set")) {
            send(p, USAGE);
            return;
        }
        if (args.length < 3) {
            send(p, "&cUsage: /clearlag time set <1s-600s> or <1m-10m>");
            return;
        }

        String raw = args[2];
        String lower = raw.toLowerCase();
        Integer seconds = null;
        if (lower.contains("s")) {
            seconds = parse(lower.replace("s", ""));
        } else if (lower.contains("m")) {
            Integer minutes = parse(lower.replace("m", ""));
            if (minutes != null) seconds = minutes * 60;
        } else {
            send(p, "&cUsage: /clearlag time set <1s-600s> or <1m-10m>");
            return;
        }

        if (seconds == null) {
            send(p, "&cInvalid time!");
            return;
        }
        if (seconds < 1 || seconds > 600) {
            send(p, RANGE_ERROR);
            return;
        }

        time = seconds;
        data.setClearlagTime(time);
        data.save();
        broadcast(PREFIX + "Clearlag timer set to &6" + raw + "&f!");
        send(p, "&aClearlag timer set to " + raw + ".");
    }

    private static Integer parse(String s) {
        try {
            return (int) Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // ------------------------------------------------------------ timer

    /** Runs once per second. */
    public void tick() {
        time--;

        switch (time) {
            case 60 -> broadcast(PREFIX + "Clearlag in 1 Minute!");
            case 30 -> broadcast(PREFIX + "Clearlag in 30 Seconds!");
            case 15 -> broadcast(PREFIX + "Clearlag in 15 Seconds!");
            case 5 -> broadcast(PREFIX + "Clearlag in 5 Seconds!");
            case 4 -> broadcast(PREFIX + "Clearlag in 4 Seconds!");
            case 3 -> broadcast(PREFIX + "Clearlag in 3 Seconds!");
            case 2 -> broadcast(PREFIX + "Clearlag in 2 Seconds!");
            case 1 -> broadcast(PREFIX + "Clearlag in 1 Second!");
            default -> { }
        }

        if (time <= 0) {
            clearEntities();
            broadcast(PREFIX + "Entities, Drops and Mobs have been Cleared!");
            time = defaultTime;
            broadcast(PREFIX + "Clearlag in 10 Minutes!");
        }
        data.setClearlagTime(time);
    }

    private void clearEntities() {
        for (String name : CLEAR_WORLDS) {
            World world = plugin.getServer().getWorld(name);
            if (world == null) continue;
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;
                if (entity instanceof Item || entity instanceof ExperienceOrb || entity instanceof LivingEntity) {
                    entity.remove();
                }
            }
        }
    }
}
