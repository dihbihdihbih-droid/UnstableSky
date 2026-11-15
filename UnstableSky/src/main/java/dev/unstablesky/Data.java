package dev.unstablesky;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Persistent storage replacing the Skript variables that survived restarts:
 * music preferences and playlists, kit cooldowns, ClearLag timer/broadcast toggles and the AFK location.
 */
public final class Data {

    private final UnstableSkyPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public Data(UnstableSkyPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    private void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void save() {
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", ex);
        }
    }

    // ---------------------------------------------------------------- music

    public double getMusicVolume(UUID id) {
        return yaml.getDouble("music." + id + ".volume", 1.0);
    }

    public void setMusicVolume(UUID id, double volume) {
        yaml.set("music." + id + ".volume", volume);
    }

    public boolean getLoopSetting(UUID id) {
        return yaml.getBoolean("music." + id + ".loop", true);
    }

    public void setLoopSetting(UUID id, boolean value) {
        yaml.set("music." + id + ".loop", value);
    }

    public boolean getPlaylistRepeat(UUID id) {
        return yaml.getBoolean("music." + id + ".repeat", false);
    }

    public void setPlaylistRepeat(UUID id, boolean value) {
        yaml.set("music." + id + ".repeat", value);
    }

    /** Returns the five playlist slots (index 0-4). Empty slots are null. */
    public String[] getPlaylistRaw(UUID id) {
        String[] slots = new String[5];
        ConfigurationSection section = yaml.getConfigurationSection("music." + id + ".playlist");
        if (section == null) {
            return slots;
        }
        for (int i = 0; i < 5; i++) {
            slots[i] = section.getString(String.valueOf(i + 1));
        }
        return slots;
    }

    public void setPlaylistRaw(UUID id, String[] slots) {
        yaml.set("music." + id + ".playlist", null);
        for (int i = 0; i < 5; i++) {
            if (slots[i] != null) {
                yaml.set("music." + id + ".playlist." + (i + 1), slots[i]);
            }
        }
    }

    // ---------------------------------------------------------------- kits

    /** Absolute expiry (unix seconds) of a kit cooldown, or 0 if none is stored. */
    public long getKitExpiry(UUID id, String kit) {
        return yaml.getLong("kitcooldown." + id + "." + kit, 0L);
    }

    public void setKitExpiry(UUID id, String kit, long expiry) {
        yaml.set("kitcooldown." + id + "." + kit, expiry);
    }

    public void clearKitExpiry(UUID id, String kit) {
        yaml.set("kitcooldown." + id + "." + kit, null);
    }

    // ---------------------------------------------------------------- clearlag

    public int getClearlagTime(int fallback) {
        return yaml.getInt("clearlag.time", fallback);
    }

    public void setClearlagTime(int seconds) {
        yaml.set("clearlag.time", seconds);
    }

    /** Null means "never set" (broadcasts enabled by default). */
    public Boolean getBroadcastSetting(UUID id) {
        String path = "clearlag.broadcasts." + id;
        return yaml.contains(path) ? yaml.getBoolean(path) : null;
    }

    public void setBroadcastSetting(UUID id, boolean value) {
        yaml.set("clearlag.broadcasts." + id, value);
    }

    // ---------------------------------------------------------------- afk

    public void setAfkLocation(Location loc) {
        yaml.set("afk.location.world", loc.getWorld().getName());
        yaml.set("afk.location.x", loc.getX());
        yaml.set("afk.location.y", loc.getY());
        yaml.set("afk.location.z", loc.getZ());
        yaml.set("afk.location.yaw", (double) loc.getYaw());
        yaml.set("afk.location.pitch", (double) loc.getPitch());
    }

    public Location getAfkLocation() {
        String worldName = yaml.getString("afk.location.world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world,
                yaml.getDouble("afk.location.x"),
                yaml.getDouble("afk.location.y"),
                yaml.getDouble("afk.location.z"),
                (float) yaml.getDouble("afk.location.yaw"),
                (float) yaml.getDouble("afk.location.pitch"));
    }
}
