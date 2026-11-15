package dev.unstablesky;

import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Song catalog and the three artist menus. Slot layout, sound ids, display names and durations
 * are carried over exactly from the Skript files.
 *
 * playlistDuration mirrors the original playCurrentPlaylistSong() table, which did not list three
 * FlameFrags tracks, so they played with a duration of 0 from the playlist (preserved on purpose).
 */
public final class Songs {

    public record Song(String id, String display, Material disc, double duration, double playlistDuration) {}

    public enum Artist { AZALI, NOMBRA, DOKURO }

    private static final Map<Artist, Map<Integer, Song>> MENUS = new LinkedHashMap<>();
    private static final Map<String, Song> BY_ID = new LinkedHashMap<>();

    private Songs() {}

    private static Song s(Map<Integer, Song> menu, int slot, String id, String display, Material disc,
                          double duration, double playlistDuration) {
        Song song = new Song(id, display, disc, duration, playlistDuration);
        menu.put(slot, song);
        BY_ID.put(id, song);
        return song;
    }

    private static Song same(Map<Integer, Song> menu, int slot, String id, String display, Material disc,
                             double duration) {
        return s(menu, slot, id, display, disc, duration, duration);
    }

    static {
        // ------------------------------------------------------------ AZALI / WEMMBU
        Map<Integer, Song> azali = new LinkedHashMap<>();
        same(azali, 10, "azali_chaos_construct", "Chaos Construct", Material.MUSIC_DISC_CAT, 110.41);
        same(azali, 11, "azali_battle_under_a_broken_sky", "Battle Under A Broken Sky", Material.MUSIC_DISC_CHIRP, 76.63);
        same(azali, 12, "azali_mechanical_god", "Mechanical God", Material.MUSIC_DISC_FAR, 119.95);
        same(azali, 13, "azali_playful_massacre", "Playful Massacre", Material.MUSIC_DISC_MALL, 131.52);
        same(azali, 14, "azali_altered_obsidian", "Altered Obsidian", Material.MUSIC_DISC_MELLOHI, 130.05);
        same(azali, 15, "azali_rival", "Rival", Material.MUSIC_DISC_STAL, 154.69);
        same(azali, 16, "azali_above_the_law", "Above The Law", Material.MUSIC_DISC_STRAD, 146.80);
        same(azali, 19, "azali_playing_with_fire", "Playing With Fire", Material.MUSIC_DISC_CAT, 198.34);
        same(azali, 20, "azali_decisive_battle", "Decisive Battle", Material.MUSIC_DISC_CHIRP, 106.65);
        same(azali, 21, "azali_deadlock", "Deadlock", Material.MUSIC_DISC_FAR, 160.92);
        same(azali, 22, "azali_rival_-vs.jadenman", "Rival - VS JadenMAN", Material.MUSIC_DISC_MALL, 152.42);
        same(azali, 23, "azali_playful_massacre_2v1000", "Playful Massacre 2V1000", Material.MUSIC_DISC_MELLOHI, 285.21);
        same(azali, 24, "azali_eves_alter_ego", "Eve's Alter Ego", Material.MUSIC_DISC_STAL, 60.33);
        same(azali, 25, "azali_voidside_swordfight", "Voidside Swordfight", Material.MUSIC_DISC_STRAD, 136.80);
        same(azali, 28, "azali_divinity_dissonant", "Divinity Dissonant", Material.MUSIC_DISC_CAT, 122.84);
        same(azali, 29, "azali_tiny_sand_rat", "Tiny Sand Rat", Material.MUSIC_DISC_CHIRP, 117.93);
        same(azali, 30, "azali_rematch", "Rematch", Material.MUSIC_DISC_FAR, 73.45);
        same(azali, 31, "azali_rematch_plus_chaos_construct", "Rematch + Chaos Construct", Material.MUSIC_DISC_MALL, 138.57);
        MENUS.put(Artist.AZALI, Collections.unmodifiableMap(azali));

        // ------------------------------------------------------------ NOMBRA
        Map<Integer, Song> nombra = new LinkedHashMap<>();
        same(nombra, 10, "nombra_total_annihilation", "Total Annihilation", Material.MUSIC_DISC_CAT, 192.84);
        same(nombra, 11, "nombra_burn_the_horizon", "Burn The Horizon", Material.MUSIC_DISC_CHIRP, 185.30);
        same(nombra, 12, "nombra_immortal", "Immortal", Material.MUSIC_DISC_FAR, 184.13);
        same(nombra, 13, "nombra_blazeborn", "Blazeborn", Material.MUSIC_DISC_MALL, 190.47);
        same(nombra, 14, "nombra_oblivionfire", "Oblivionfire", Material.MUSIC_DISC_MELLOHI, 188.17);
        same(nombra, 15, "nombra_incinerated_battlefield", "Incinerated Battlefield", Material.MUSIC_DISC_STAL, 186.13);
        same(nombra, 16, "nombra_the_zone", "The Zone", Material.MUSIC_DISC_STRAD, 360.34);
        same(nombra, 19, "nombra_inferno_rising", "Inferno Rising", Material.MUSIC_DISC_CAT, 187.00);
        same(nombra, 20, "nombra_carnage", "Carnage", Material.MUSIC_DISC_CHIRP, 182.72);
        same(nombra, 21, "nombra_flamefrags_2nd_theme_song", "FlameFrags 2nd Theme Song", Material.MUSIC_DISC_FAR, 264.68);
        same(nombra, 22, "nombra_killstreak_anthem", "Killstreak Anthem", Material.MUSIC_DISC_MALL, 200.92);
        same(nombra, 23, "nombra_flamefrags_theme_song", "FlameFrags Theme Song", Material.MUSIC_DISC_MELLOHI, 215.01);
        same(nombra, 24, "nombra_scorched_earth_symphony", "Scorched Earth Symphony", Material.MUSIC_DISC_STAL, 188.63);
        same(nombra, 25, "nombra_ignite_the_sky", "Ignite The Sky", Material.MUSIC_DISC_STRAD, 183.59);
        same(nombra, 28, "nombra_pursuit_protocol", "Pursuit Protocol", Material.MUSIC_DISC_CAT, 199.60);
        s(nombra, 29, "nombra_flamefrags_its_fraggin_time", "FlameFrags - It's Fraggin Time", Material.MUSIC_DISC_CHIRP, 264.68, 0);
        s(nombra, 30, "nombra_flamefrags_burn_for_the_metal", "FlameFrags - Burn For The Metal", Material.MUSIC_DISC_FAR, 264.68, 0);
        s(nombra, 31, "nombra_flamefrags_huntsman", "FlameFrags - Huntsman", Material.MUSIC_DISC_MALL, 264.68, 0);
        MENUS.put(Artist.NOMBRA, Collections.unmodifiableMap(nombra));

        // ------------------------------------------------------------ DOKURO
        Map<Integer, Song> dokuro = new LinkedHashMap<>();
        same(dokuro, 10, "dokuro_threats_of_the_ocean_floor", "Threats Of The Ocean Floor", Material.MUSIC_DISC_CAT, 259.24);
        same(dokuro, 12, "dokuro_universal_collapse", "Universal Collapse", Material.MUSIC_DISC_CHIRP, 256.55);
        MENUS.put(Artist.DOKURO, Collections.unmodifiableMap(dokuro));
    }

    public static Map<Integer, Song> menu(Artist artist) {
        return MENUS.get(artist);
    }

    public static Optional<Song> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }
}
