package dev.unstablesky;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Shared helpers: legacy-ampersand text, item builders, small caps, formatting. */
public final class Util {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private static final String SMALLCAPS_FROM = "abcdefghijklmnopqrstuvwxyz";
    private static final String SMALLCAPS_TO = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";

    private Util() {}

    public static Component text(String legacy) {
        return LEGACY.deserialize(legacy);
    }

    public static void send(CommandSender sender, String legacy) {
        sender.sendMessage(text(legacy));
    }

    public static ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(text(name));
            }
            if (lore.length > 0) {
                List<Component> lines = new ArrayList<>(lore.length);
                for (String line : lore) {
                    lines.add(text(line));
                }
                meta.lore(lines);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** Lowercases the input and swaps a-z for the small-caps glyphs (other characters unchanged). */
    public static String smallcaps(String input) {
        String lower = input.toLowerCase();
        StringBuilder out = new StringBuilder(lower.length());
        for (char c : lower.toCharArray()) {
            int idx = SMALLCAPS_FROM.indexOf(c);
            out.append(idx >= 0 ? SMALLCAPS_TO.charAt(idx) : c);
        }
        return out.toString();
    }

    /** Volume as a whole percentage, same rounding as the original Skript (round(v * 100)). */
    public static long percent(double volume) {
        return Math.round(volume * 100);
    }

    /** Formats seconds as m:ss with zero-padded seconds, matching the KeyAll display. */
    public static String mmss(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds - minutes * 60;
        return minutes + (seconds < 10 ? ":0" : ":") + seconds;
    }

    public static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
