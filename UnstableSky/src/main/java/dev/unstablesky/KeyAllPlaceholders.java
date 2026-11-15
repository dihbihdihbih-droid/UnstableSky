package dev.unstablesky;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * PlaceholderAPI expansion for the "sky" prefix. Only registered when PlaceholderAPI is installed,
 * so the plugin still loads without it.
 *
 *   %sky_keytimer%  -> the player's KeyAll countdown as m:ss
 */
public final class KeyAllPlaceholders extends PlaceholderExpansion {

    private final UnstableSkyPlugin plugin;
    private final KeyAllManager keyAll;

    public KeyAllPlaceholders(UnstableSkyPlugin plugin, KeyAllManager keyAll) {
        this.plugin = plugin;
        this.keyAll = keyAll;
    }

    @Override
    public String getIdentifier() {
        return "sky";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", List.of("UnstableSky"));
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null || !player.isOnline()) {
            return "";
        }
        if (identifier.equalsIgnoreCase("keytimer")) {
            return keyAll.displayFor(player.getUniqueId());
        }
        return null;
    }
}
