# UnstableSky

Standalone Paper plugin that replaces the five Skript scripts (music, kitmenu, warpgui,
keyalltimer, clearlag). No Skript or SkBee required.

## Requirements

- Paper 1.21.11
- Java 21
- Maven 3.9+ to build
- Runtime plugins the original scripts relied on: Essentials (`/warp`, `/essentials:afk`),
  CratesPlus (`crates key give ... keyall`), and optionally PlaceholderAPI (`%sky_keytimer%`).
  Custom music discs need the same resource pack as before.

## Build

    mvn clean package

The jar is written to `target/UnstableSky.jar`. Drop it into `plugins/` and remove the five `.sk` files.

## Commands (unchanged)

| Command | Permission | Notes |
|---|---|---|
| `/music` | none | Music GUI |
| `/kits`, `/kit` | none | Kit menu |
| `/warpgui`, `/warps2`, `/warps3` | none | Warp GUI pages |
| `/warp <name>` | none | Intercepted: 3-second countdown, cancelled by moving. No args goes to Essentials |
| `/warps`, `/essentials:warps`, `/essentials:warp` | none | Blocked with the original message |
| `/clearlag [time set <1s-600s>|<1m-10m>]` | `unstable.clearlag` | |
| `/unstableskybroadcast` | none | Personal ClearLag broadcast toggle |
| `/afkset` | op | Saves current location as AFK spawn |
| `/afk` | none | 3-second title countdown, then teleport and `/essentials:afk` |
| `/keyall <1-600>` | op | Sets every online player's KeyAll timer |

## Persistence

`plugins/UnstableSky/data.yml` stores what the Skript variables stored across restarts:
music volume, song-loop and playlist-repeat settings, the 5-slot playlists, kit cooldowns
(absolute expiry times), ClearLag timer, ClearLag broadcast toggles, and the AFK location.

Playback state (what is currently playing) is not persisted. KeyAll timers reset on join,
as in the original script.

## Configuration

`config.yml` holds the KeyAll intervals (same keys as the Skript `options:` block) and the
ClearLag default time.

## Known behaviour carried over on purpose

- Three FlameFrags tracks (`nombra_flamefrags_its_fraggin_time`, `..._burn_for_the_metal`,
  `..._huntsman`) have a duration of 0 when played from a playlist. The original playlist
  duration table omitted them.
- The 1-second live refresh of the kit menu overwrites two kit names with different
  smallcaps spellings (`ᴜɴsᴛᴀʙʟᴇsᴋʏ+`, `ᴜɴsᴛᴀʙʟᴇsᴋʏ++`) from the ones shown on open.
- Warp GUI pages show 28 icons starting at index `(page-1)*27+1`, and the header says
  "Total warps: 80" as in the original text.
- Clicking a warp in the GUI runs the same 3-second countdown as typing `/warp`.

## Assumptions to check

- ClearLag clearing: the original used `killall mobs|drops|xp <world>`. This plugin removes
  every non-player living entity, item entity, and XP orb in the five worlds
  (`unstablespawn`, `unstablespawn_nether`, `unstablespawn_the_end`, `Spawn`, `nethervoid`).
  If your killall plugin excluded anything (named mobs, tamed animals), adjust
  `ClearLagManager.clearEntities()`.
