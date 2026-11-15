package dev.unstablesky;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.Map;

import static dev.unstablesky.Util.item;
import static dev.unstablesky.Util.send;

/** Builds and handles the music GUIs: main, settings, artist catalogs and playlist. */
public final class MusicMenu {

    private static final String MSG = "&c&lMUSIC &8» &f";

    private final MusicManager music;

    public MusicMenu(MusicManager music) {
        this.music = music;
    }

    // ------------------------------------------------------------ open helpers

    private Inventory create(Player p, MenuHolder holder, int rows, String title) {
        Inventory inv = Bukkit.createInventory(holder, rows * 9, Util.text(title));
        holder.setInventory(inv);
        return inv;
    }

    // ------------------------------------------------------------ main

    public void openMain(Player p) {
        MenuHolder h = new MenuHolder(MenuHolder.Kind.MUSIC_MAIN);
        Inventory inv = create(p, h, 6, "&c&lUNSTABLE MUSIC");
        inv.setItem(4, item(Material.JUKEBOX, "&c&lAZALI", "&7Wemmbu Music", "", "&eClick to view songs"));
        inv.setItem(22, item(Material.JUKEBOX, "&6&lNOMBRA", "&7Nombra Music", "", "&eClick to view songs"));
        inv.setItem(40, item(Material.JUKEBOX, "&9&lDOKURO", "&7Dokuro Music", "", "&eClick to view songs"));
        inv.setItem(48, item(Material.MUSIC_DISC_STRAD, "&d&lPLAYLIST",
                "&7Your personal 5-song playlist", "", "&eClick to open playlist"));
        inv.setItem(50, item(Material.COMPARATOR, "&e&lSETTINGS",
                "&7Volume, loop and playback settings", "", "&eClick to open settings"));
        inv.setItem(49, item(Material.BARRIER, "&c&lSTOP MUSIC", "&7Stop all music"));
        p.openInventory(inv);
    }

    // ------------------------------------------------------------ settings

    public void openSettings(Player p) {
        MenuHolder h = new MenuHolder(MenuHolder.Kind.MUSIC_SETTINGS);
        Inventory inv = create(p, h, 6, "&e&lMUSIC SETTINGS");
        String pct = String.valueOf(music.getVolumePercent(p.getUniqueId()));
        boolean loop = music.getLoopSetting(p.getUniqueId());
        boolean repeat = music.getPlaylistRepeat(p.getUniqueId());

        inv.setItem(20, item(Material.RED_DYE, "&c&lVOLUME -",
                "&7Decrease volume by 5 percent", "", "&fCurrent: &e" + pct + "% percent", "", "&eClick to decrease"));
        inv.setItem(22, item(Material.NOTE_BLOCK, "&e&lVOLUME &8» &f" + pct + "% percent",
                "&7Left click &8» &c-5 percent", "&7Right click &8» &a+5 percent", "",
                "&fCurrent Volume: &e" + pct + "% percent"));
        inv.setItem(24, item(Material.LIME_DYE, "&a&lVOLUME +",
                "&7Increase volume by 5 percent", "", "&fCurrent: &e" + pct + "% percent", "", "&eClick to increase"));
        inv.setItem(29, item(Material.REPEATER, "&e&lSONG LOOP",
                "&7Current: " + (loop ? "&aENABLED" : "&cDISABLED"), "", "&eClick to toggle"));
        inv.setItem(31, item(Material.HOPPER, "&d&lPLAYLIST REPEAT",
                "&7Current: " + (repeat ? "&aENABLED" : "&cDISABLED"), "", "&eClick to toggle"));
        inv.setItem(33, item(Material.NOTE_BLOCK, "&b&lNOW PLAYING",
                "&f" + music.getNowPlayingName(p.getUniqueId())));
        inv.setItem(40, item(Material.BARRIER, "&c&lSTOP MUSIC",
                "&7Stop the current music", "", "&cClick to stop"));
        inv.setItem(45, item(Material.ARROW, "&e&l<- BACK"));
        p.openInventory(inv);
    }

    // ------------------------------------------------------------ artist catalogs

    public void openArtist(Player p, Songs.Artist artist) {
        MenuHolder h = new MenuHolder(MenuHolder.Kind.MUSIC_ARTIST, artist, 0, null);
        String title = switch (artist) {
            case AZALI -> "&c&lAZALI &8| &fWEMMBU";
            case NOMBRA -> "&6&lNOMBRA MUSIC";
            case DOKURO -> "&9&lDOKURO MUSIC";
        };
        Inventory inv = create(p, h, 6, title);
        for (Map.Entry<Integer, Songs.Song> e : Songs.menu(artist).entrySet()) {
            Songs.Song song = e.getValue();
            inv.setItem(e.getKey(), item(song.disc(), "&f" + song.display(),
                    "&aLeft click to play", "&eRight click to add to 5-song playlist"));
        }
        inv.setItem(45, item(Material.ARROW, "&e&l<- BACK"));
        inv.setItem(47, item(Material.COMPARATOR, "&e&lSETTINGS", "&7Open music settings"));
        inv.setItem(49, item(Material.BARRIER, "&4&lSTOP MUSIC"));
        p.openInventory(inv);
    }

    // ------------------------------------------------------------ playlist

    public void openPlaylist(Player p) {
        MenuHolder h = new MenuHolder(MenuHolder.Kind.MUSIC_PLAYLIST);
        Inventory inv = create(p, h, 6, "&d&lUNSTABLE PLAYLIST");
        String[] names = music.playlistNames(p.getUniqueId());
        String[] sounds = music.playlistSounds(p.getUniqueId());

        for (int i = 0; i < 5; i++) {
            if (sounds[i] != null) {
                inv.setItem(i, item(Material.MUSIC_DISC_STRAD, "&f" + (i + 1) + ". &d" + names[i],
                        "&7Right click to remove", "&7Playlist slot &f" + (i + 1) + "&7/5"));
            } else {
                inv.setItem(i, item(Material.GRAY_STAINED_GLASS_PANE, "&7" + (i + 1) + ". &8EMPTY"));
            }
        }

        if (music.isPlaylistActive(p.getUniqueId())) {
            inv.setItem(22, item(Material.LIME_CONCRETE, "&a&lPLAYLIST PLAYING",
                    "&7Currently playing slot &f" + music.currentPlaylistSlot(p.getUniqueId()) + "&7/5"));
        } else {
            inv.setItem(22, item(Material.JUKEBOX, "&e&lPLAYLIST READY",
                    "&7Songs: &f" + music.playlistCount(p.getUniqueId()) + "&7/5"));
        }
        inv.setItem(40, item(Material.LIME_WOOL, "&a&lPLAY PLAYLIST", "&7Start your 5-song playlist"));
        inv.setItem(42, item(Material.LAVA_BUCKET, "&c&lCLEAR PLAYLIST", "&7Remove all songs"));
        inv.setItem(44, item(Material.BARRIER, "&4&lSTOP MUSIC"));
        inv.setItem(45, item(Material.ARROW, "&e&l<- BACK"));
        inv.setItem(47, item(Material.COMPARATOR, "&e&lSETTINGS", "&7Open music settings"));
        p.openInventory(inv);
    }

    // ------------------------------------------------------------ clicks

    public void handleClick(Player p, MenuHolder holder, int slot, ClickType click) {
        switch (holder.kind()) {
            case MUSIC_MAIN -> handleMain(p, slot);
            case MUSIC_SETTINGS -> handleSettings(p, slot, click);
            case MUSIC_ARTIST -> handleArtist(p, holder.artist(), slot, click);
            case MUSIC_PLAYLIST -> handlePlaylist(p, slot, click);
            default -> { }
        }
    }

    private void handleMain(Player p, int slot) {
        switch (slot) {
            case 4 -> openArtist(p, Songs.Artist.AZALI);
            case 22 -> openArtist(p, Songs.Artist.NOMBRA);
            case 40 -> openArtist(p, Songs.Artist.DOKURO);
            case 48 -> openPlaylist(p);
            case 50 -> openSettings(p);
            case 49 -> {
                music.stop(p);
                send(p, MSG + "Music stopped!");
            }
            default -> { }
        }
    }

    private void handleSettings(Player p, int slot, ClickType click) {
        switch (slot) {
            case 45 -> openMain(p);
            case 20 -> {
                music.adjustVolume(p, -MusicManager.VOLUME_STEP);
                send(p, MSG + "Volume set to &c" + music.getVolumePercent(p.getUniqueId()) + "% percent");
                openSettings(p);
            }
            case 24 -> {
                music.adjustVolume(p, MusicManager.VOLUME_STEP);
                send(p, MSG + "Volume set to &a" + music.getVolumePercent(p.getUniqueId()) + "% percent");
                openSettings(p);
            }
            case 22 -> {
                if (click.isLeftClick()) {
                    music.adjustVolume(p, -MusicManager.VOLUME_STEP);
                    send(p, MSG + "Volume set to &c" + music.getVolumePercent(p.getUniqueId()) + "% percent");
                } else if (click.isRightClick()) {
                    music.adjustVolume(p, MusicManager.VOLUME_STEP);
                    send(p, MSG + "Volume set to &a" + music.getVolumePercent(p.getUniqueId()) + "% percent");
                }
                openSettings(p);
            }
            case 29 -> {
                music.toggleLoopSetting(p);
                openSettings(p);
            }
            case 31 -> {
                music.togglePlaylistRepeat(p);
                openSettings(p);
            }
            case 40 -> {
                music.stop(p);
                send(p, MSG + "Music stopped!");
                openSettings(p);
            }
            default -> { }
        }
    }

    private void handleArtist(Player p, Songs.Artist artist, int slot, ClickType click) {
        switch (slot) {
            case 45 -> openMain(p);
            case 47 -> openSettings(p);
            case 49 -> {
                music.stop(p);
                send(p, MSG + "Music stopped!");
            }
            default -> {
                Songs.Song song = Songs.menu(artist).get(slot);
                if (song == null) return;
                if (click.isRightClick()) {
                    music.togglePlaylist(p, song.id(), song.display());
                    openArtist(p, artist);
                } else {
                    music.playSong(p, song, false);
                }
            }
        }
    }

    private void handlePlaylist(Player p, int slot, ClickType click) {
        switch (slot) {
            case 40 -> {
                music.startPlaylist(p);
                openPlaylist(p);
            }
            case 42 -> {
                music.clearPlaylist(p);
                openPlaylist(p);
            }
            case 44 -> {
                music.stop(p);
                send(p, MSG + "Music stopped!");
                openPlaylist(p);
            }
            case 45 -> openMain(p);
            case 47 -> openSettings(p);
            case 0, 1, 2, 3, 4 -> {
                if (click.isRightClick() && music.playlistSounds(p.getUniqueId())[slot] != null) {
                    music.removeFromPlaylistSlot(p, slot);
                    openPlaylist(p);
                }
            }
            default -> { }
        }
    }
}
