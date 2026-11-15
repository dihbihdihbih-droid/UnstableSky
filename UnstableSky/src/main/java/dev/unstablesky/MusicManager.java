package dev.unstablesky;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.unstablesky.Util.send;

/**
 * Playback state, volume, playlist (5 slots) and the 1-second engine that advances
 * songs and loops. Behaviour mirrors the original Skript "UNSTABLE MUSIC" script.
 */
public final class MusicManager {

    private static final String PREFIX = "&c&lMUSIC &8» &f";
    public static final double MIN_VOLUME = 0.05;
    public static final double MAX_VOLUME = 3.0;
    public static final double VOLUME_STEP = 0.05;

    /** Transient playback state (not persisted, same as a restart stopping the music). */
    private static final class Play {
        boolean playing;
        boolean loop;
        boolean playlistActive;
        int slot = 1;               // 1..5
        String sound;
        String name;
        double duration;
        long endMillis;             // when the current track is considered finished
    }

    private final UnstableSkyPlugin plugin;
    private final Data data;
    private final Map<UUID, Play> plays = new HashMap<>();
    /** Playlist slots (index 0-4); null is an empty slot. */
    private final Map<UUID, String[]> playlists = new HashMap<>();
    private final Map<UUID, String[]> playlistNames = new HashMap<>();

    public MusicManager(UnstableSkyPlugin plugin, Data data) {
        this.plugin = plugin;
        this.data = data;
    }

    // ------------------------------------------------------------ preferences

    public double getVolume(UUID id) {
        return data.getMusicVolume(id);
    }

    public int getVolumePercent(UUID id) {
        return (int) Util.percent(getVolume(id));
    }

    public void adjustVolume(Player p, double delta) {
        double v = getVolume(p.getUniqueId()) + delta;
        if (v < MIN_VOLUME) v = MIN_VOLUME;
        if (v > MAX_VOLUME) v = MAX_VOLUME;
        data.setMusicVolume(p.getUniqueId(), v);
        data.save();
        restartCurrentWithVolume(p, v);
    }

    public boolean getLoopSetting(UUID id) {
        return data.getLoopSetting(id);
    }

    public void toggleLoopSetting(Player p) {
        boolean now = !data.getLoopSetting(p.getUniqueId());
        data.setLoopSetting(p.getUniqueId(), now);
        if (!now) {
            Play play = plays.get(p.getUniqueId());
            if (play != null) play.loop = false;
        }
        data.save();
        send(p, PREFIX + "Song loop " + (now ? "&aenabled" : "&cdisabled") + "&f.");
    }

    public boolean getPlaylistRepeat(UUID id) {
        return data.getPlaylistRepeat(id);
    }

    public void togglePlaylistRepeat(Player p) {
        boolean now = !data.getPlaylistRepeat(p.getUniqueId());
        data.setPlaylistRepeat(p.getUniqueId(), now);
        data.save();
        send(p, PREFIX + "Playlist repeat " + (now ? "&aenabled" : "&cdisabled") + "&f.");
    }

    public String getNowPlayingName(UUID id) {
        Play play = plays.get(id);
        return (play != null && play.name != null) ? play.name : "&7Nothing";
    }

    public boolean isPlaying(UUID id) {
        Play play = plays.get(id);
        return play != null && play.playing;
    }

    // ------------------------------------------------------------ playlist storage

    /** Loads a player's playlist from storage on first access ("sound;name" per slot). */
    private void ensureLoaded(UUID id) {
        if (playlists.containsKey(id)) return;
        String[] sounds = new String[5];
        String[] names = new String[5];
        String[] raw = data.getPlaylistRaw(id);
        for (int i = 0; i < 5; i++) {
            if (raw[i] != null) {
                int sep = raw[i].indexOf(';');
                sounds[i] = sep >= 0 ? raw[i].substring(0, sep) : raw[i];
                names[i] = sep >= 0 ? raw[i].substring(sep + 1) : raw[i];
            }
        }
        playlists.put(id, sounds);
        playlistNames.put(id, names);
    }

    private String[] slots(UUID id) {
        ensureLoaded(id);
        return playlists.get(id);
    }

    private String[] names(UUID id) {
        ensureLoaded(id);
        return playlistNames.get(id);
    }

    public String[] playlistSounds(UUID id) {
        return slots(id).clone();
    }

    public String[] playlistNames(UUID id) {
        return names(id).clone();
    }

    public int playlistCount(UUID id) {
        int count = 0;
        for (String s : slots(id)) if (s != null) count++;
        return count;
    }

    private void persistPlaylist(UUID id) {
        String[] sounds = slots(id);
        String[] names = names(id);
        String[] raw = new String[5];
        for (int i = 0; i < 5; i++) {
            raw[i] = sounds[i] == null ? null : sounds[i] + ";" + names[i];
        }
        data.setPlaylistRaw(id, raw);
        data.save();
    }

    /** Add the song to the first empty slot, or remove it if it is already in the playlist. */
    public void togglePlaylist(Player p, String soundId, String display) {
        UUID id = p.getUniqueId();
        String[] sounds = slots(id);
        String[] names = names(id);

        for (int i = 0; i < 5; i++) {
            if (soundId.equals(sounds[i])) {
                // Shift the remaining entries left.
                for (int j = i; j < 4; j++) {
                    sounds[j] = sounds[j + 1];
                    names[j] = names[j + 1];
                }
                sounds[4] = null;
                names[4] = null;
                persistPlaylist(id);
                send(p, PREFIX + "Removed &c" + display + " &ffrom your playlist.");
                return;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (sounds[i] == null) {
                sounds[i] = soundId;
                names[i] = display;
                persistPlaylist(id);
                send(p, PREFIX + "Added &c" + display + " &fto playlist!");
                send(p, "&7Playlist: &f" + playlistCount(id) + "&7/5");
                return;
            }
        }

        send(p, "&c&lMUSIC &8» &cYour playlist is already full! &7(5/5)");
    }

    /** Remove the song at the given slot (0-4) and shift the rest up. */
    public void removeFromPlaylistSlot(Player p, int index) {
        UUID id = p.getUniqueId();
        String[] sounds = slots(id);
        String[] names = names(id);
        String removed = names[index];
        for (int j = index; j < 4; j++) {
            sounds[j] = sounds[j + 1];
            names[j] = names[j + 1];
        }
        sounds[4] = null;
        names[4] = null;
        persistPlaylist(id);
        send(p, PREFIX + "Removed &c" + removed + " &ffrom your playlist.");
    }

    public void clearPlaylist(Player p) {
        UUID id = p.getUniqueId();
        playlists.put(id, new String[5]);
        playlistNames.put(id, new String[5]);
        persistPlaylist(id);
        send(p, PREFIX + "Playlist cleared!");
    }

    // ------------------------------------------------------------ playback

    public void stop(Player p) {
        p.stopSound(SoundCategory.MASTER);
        Play play = plays.computeIfAbsent(p.getUniqueId(), k -> new Play());
        play.playing = false;
        play.loop = false;
        play.playlistActive = false;
        play.sound = null;
        play.name = null;
        play.duration = 0;
        play.endMillis = 0;
    }

    /** Plays a catalog song by id (menu click). Respects the song-loop setting. */
    public void playSong(Player p, Songs.Song song, boolean fromPlaylistPath) {
        UUID id = p.getUniqueId();
        p.stopSound(SoundCategory.MASTER);
        double duration = fromPlaylistPath ? song.playlistDuration() : song.duration();
        boolean loop = data.getLoopSetting(id);
        startTrack(p, song.id(), song.display(), duration, loop);
        send(p, PREFIX + "Now playing: &c" + song.display() + " &7(Volume: " + getVolumePercent(id) + " percent)");
    }

    private void startTrack(Player p, String sound, String display, double duration, boolean loop) {
        Play play = plays.computeIfAbsent(p.getUniqueId(), k -> new Play());
        play.playing = true;
        play.loop = loop;
        play.sound = sound;
        play.name = display;
        play.duration = duration;
        playRaw(p, sound, getVolume(p.getUniqueId()));
        play.endMillis = System.currentTimeMillis() + (long) (duration * 1000);
    }

    /** Raw playsound with the original volume > 1 trick (sound emitted 5 blocks behind the player). */
    private void playRaw(Player p, String sound, double volume) {
        Location loc = p.getLocation();
        if (volume > 1.0) {
            loc = loc.clone().add(0, 0, -5);
        }
        p.playSound(loc, "minecraft:" + sound, SoundCategory.MASTER, (float) volume, 1f);
    }

    private void restartCurrentWithVolume(Player p, double volume) {
        Play play = plays.get(p.getUniqueId());
        if (play == null || !play.playing || play.sound == null) return;
        p.stopSound(SoundCategory.MASTER);
        playRaw(p, play.sound, volume);
    }

    /** Starts the playlist from slot 1 (PLAY PLAYLIST button). */
    public void startPlaylist(Player p) {
        UUID id = p.getUniqueId();
        if (playlistCount(id) == 0) {
            send(p, "&c&lMUSIC &8» &cYour playlist is empty!");
            return;
        }
        stop(p);
        Play play = plays.computeIfAbsent(id, k -> new Play());
        play.playlistActive = true;
        play.slot = 1;
        playCurrentPlaylistSong(p);
    }

    private void playCurrentPlaylistSong(Player p) {
        UUID id = p.getUniqueId();
        Play play = plays.computeIfAbsent(id, k -> new Play());
        if (!play.playlistActive) return;

        int idx = play.slot - 1;
        String sound = slots(id)[idx];
        String display = names(id)[idx];
        if (sound == null) {
            // Fall back to the first filled slot.
            for (int i = 0; i < 5; i++) {
                if (slots(id)[i] != null) {
                    play.slot = i + 1;
                    idx = i;
                    sound = slots(id)[i];
                    display = names(id)[i];
                    break;
                }
            }
            if (sound == null) return;
        }

        double duration = Songs.byId(sound).map(Songs.Song::playlistDuration).orElse(0.0);
        p.stopSound(SoundCategory.MASTER);
        startTrack(p, sound, display, duration, false);
        send(p, PREFIX + "Playlist &7[" + play.slot + "/5] &fNow playing: &c" + display
                + " &7(Volume: " + getVolumePercent(id) + " percent)");
    }

    // ------------------------------------------------------------ engine

    /** Called once per second from the plugin scheduler. */
    public void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Play> entry : plays.entrySet()) {
            Play play = entry.getValue();
            if (!play.playing) continue;
            Player p = plugin.getServer().getPlayer(entry.getKey());
            if (p == null) continue;
            if (play.endMillis != 0 && now < play.endMillis) continue;

            if (play.playlistActive) {
                advancePlaylist(p, play);
            } else if (play.loop && play.sound != null) {
                playRaw(p, play.sound, getVolume(entry.getKey()));
                play.endMillis = now + (long) (play.duration * 1000);
            }
        }
    }

    /** Looks at the next 5 slots (wrapping, current slot last), exactly like the original loop. */
    private void advancePlaylist(Player p, Play play) {
        UUID id = p.getUniqueId();
        String[] sounds = slots(id);
        int current = play.slot;
        for (int k = 1; k <= 5; k++) {
            int next = ((current - 1 + k) % 5) + 1;
            if (sounds[next - 1] != null) {
                play.slot = next;
                playCurrentPlaylistSong(p);
                return;
            }
        }
        if (data.getPlaylistRepeat(id)) {
            play.slot = 1;
            playCurrentPlaylistSong(p);
        } else {
            stop(p);
            send(p, PREFIX + "Playlist finished.");
        }
    }

    public boolean isPlaylistActive(UUID id) {
        Play play = plays.get(id);
        return play != null && play.playlistActive;
    }

    public int currentPlaylistSlot(UUID id) {
        Play play = plays.get(id);
        return play == null ? 1 : play.slot;
    }

    public void forgetPlayer(UUID id) {
        plays.remove(id);
    }

    public void saveAll() {
        data.save();
    }
}
