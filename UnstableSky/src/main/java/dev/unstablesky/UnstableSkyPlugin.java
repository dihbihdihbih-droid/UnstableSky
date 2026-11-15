package dev.unstablesky;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static dev.unstablesky.Util.send;

/**
 * UnstableSky: standalone replacement for the Skript scripts (music, kits, warp GUI,
 * KeyAll, ClearLag). Commands, permissions, titles, messages and layouts are unchanged.
 */
public final class UnstableSkyPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private static final long AUTOSAVE_TICKS = 20L * 60 * 5;

    private Data data;
    private MusicManager music;
    private MusicMenu musicMenu;
    private KitManager kits;
    private WarpManager warps;
    private WarpMenu warpMenu;
    private KeyAllManager keyAll;
    private ClearLagManager clearLag;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        data = new Data(this);
        music = new MusicManager(this, data);
        musicMenu = new MusicMenu(music);
        kits = new KitManager(data);
        warps = new WarpManager(this);
        warpMenu = new WarpMenu(warps);
        keyAll = new KeyAllManager(this, data);
        clearLag = new ClearLagManager(this, data);

        for (String name : new String[]{"music", "kits", "kit", "warpgui", "warps2", "warps3",
                "clearlag", "unstableskybroadcast", "afkset", "afk", "keyall"}) {
            var cmd = getCommand(name);
            if (cmd != null) cmd.setExecutor(this);
        }

        getServer().getPluginManager().registerEvents(this, this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            keyAll.onJoin(p);
        }

        // One scheduler task drives every once-per-second engine, same as the Skript "every 1 second" blocks.
        getServer().getScheduler().runTaskTimer(this, this::secondTick, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, data::save, AUTOSAVE_TICKS, AUTOSAVE_TICKS);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new KeyAllPlaceholders(this, keyAll).register();
        }

        getLogger().info("UnstableSky enabled.");
    }

    @Override
    public void onDisable() {
        data.save();
        getLogger().info("UnstableSky disabled.");
    }

    private void secondTick() {
        music.tick();
        keyAll.tick();
        clearLag.tick();
        for (Player p : getServer().getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder mh
                    && mh.kind() == MenuHolder.Kind.KIT_MENU) {
                kits.refreshMenu(p, p.getOpenInventory().getTopInventory());
            }
        }
    }

    // ------------------------------------------------------------ commands

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("keyall")) {
            if (args.length < 1) {
                send(sender, "&cUsage: /keyall <1-600>");
                return true;
            }
            int seconds;
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                send(sender, "&cUsage: /keyall <1-600>");
                return true;
            }
            if (!keyAll.setAll(seconds)) {
                send(sender, "&cChoose a number from 1 to 600.");
                return true;
            }
            send(sender, "&aEveryone's KeyAll timer was set to &e" + seconds + " seconds&a.");
            return true;
        }

        if (!(sender instanceof Player p)) {
            send(sender, "&cOnly players can use this command.");
            return true;
        }

        switch (name) {
            case "music" -> musicMenu.openMain(p);
            case "kits", "kit" -> kits.openMenu(p);
            case "warpgui" -> warpMenu.open(p, 1);
            case "warps2" -> warpMenu.open(p, 2);
            case "warps3" -> warpMenu.open(p, 3);
            case "clearlag" -> clearLag.command(p, args);
            case "unstableskybroadcast" -> clearLag.toggleBroadcast(p);
            case "afkset" -> keyAll.setAfkLocation(p);
            case "afk" -> keyAll.teleportToAfk(p);
            default -> { }
        }
        return true;
    }

    // ------------------------------------------------------------ listeners

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        warps.onCommand(event);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        keyAll.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        music.forgetPlayer(p.getUniqueId());
        kits.forgetPlayer(p.getUniqueId());
        warps.forgetPlayer(p.getUniqueId());
        keyAll.onQuit(p);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        warps.onMove(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);

        // Only clicks on the menu itself are handled; clicks in the player's own inventory are ignored.
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        if (!(event.getWhoClicked() instanceof Player p)) return;
        int slot = event.getRawSlot();

        switch (holder.kind()) {
            case MUSIC_MAIN, MUSIC_SETTINGS, MUSIC_ARTIST, MUSIC_PLAYLIST ->
                    musicMenu.handleClick(p, holder, slot, event.getClick());
            case KIT_MENU -> kits.handleMenuClick(p, slot, event.getClick());
            case KIT_CONFIRM -> kits.handleConfirmClick(p, slot);
            case WARP_MENU -> warpMenu.handleClick(p, holder, slot, event.getClick());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
}
