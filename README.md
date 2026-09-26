# HudUniVerse 1.3.3 — Paper 26.2 / Java 25

HUD integration plugin for the UniVerse Minecraft server.

## Target

- Paper 26.2
- Java 25
- PlaceholderAPI 2.11.6 API
- Works with Towny, AuraSkills and RealisticSeasons through PlaceholderAPI
- TAB remains responsible for the right-side scoreboard

Paper 26.2 is declared in `plugin.yml` with `api-version: '26.2'`.

## What it provides

The plugin registers these PlaceholderAPI placeholders:

- `%huduniverse_town%`
- `%huduniverse_nation%`
- `%huduniverse_location%`
- `%huduniverse_season%`
- `%huduniverse_temperature%`
- `%huduniverse_mana%`
- `%huduniverse_mana_max%`
- `%huduniverse_mana_percent%`
- `%huduniverse_pvp%`
- `%huduniverse_pvp_color%`
- `%huduniverse_world%`
- `%huduniverse_x%`
- `%huduniverse_y%`
- `%huduniverse_z%`
- `%huduniverse_coordinates%`
- `%huduniverse_biome%`

The values are obtained from the existing PlaceholderAPI placeholders supplied by the installed plugins:

Towny:
`%townyadvanced_town%`
`%townyadvanced_nation%`

AuraSkills:
`%auraskills_mana_int%`
`%auraskills_mana_max_int%`

RealisticSeasons:
`%rs_season%`
`%rs_temperature_int_celcius%`

## Installation

1. Install Java 25.
2. Build this project:
   `gradle build`
3. Copy:
   `build/libs/HudUniVerse-1.1.0.jar`
   to your server `plugins` folder.
4. Restart Paper.
5. Confirm:
   `/plugins`
6. Test:
   `/huduniverse status`
   `/huduniverse test`
   `/huduniverse reload`

## Required plugins

- PlaceholderAPI

The plugin soft-depends on:
- Towny
- AuraSkills
- RealisticSeasons
- TAB

No Towny/AuraSkills/RealisticSeasons jar is bundled into this plugin.

## Important: PlaceholderAPI

AuraSkills exposes its placeholders through PlaceholderAPI directly. RealisticSeasons also exposes its placeholders through PlaceholderAPI. Towny exposes its placeholders through PlaceholderAPI.

Check the real values with:

`/papi parse me %townyadvanced_town%`
`/papi parse me %auraskills_mana_int%`
`/papi parse me %auraskills_mana_max_int%`
`/papi parse me %rs_temperature_int_celcius%`
`/papi parse me %rs_season%`

If a placeholder is returned literally instead of a value, fix that plugin/PAPI integration before troubleshooting HudUniVerse.

## TAB scoreboard

TAB can directly use the HudUniVerse placeholders. Example:

```yaml
scoreboard:
  enabled: true
  scoreboards:
    universe:
      title: "&2✦ &aUNIVERSE &2✦"
      lines:
        - "&8━━━━━━━━━━━━━━"
        - "&a⌂ &f%huduniverse_location%"
        - "&7%huduniverse_nation%"
        - ""
        - "&d✦ &fMana: &d%huduniverse_mana%&7/&d%huduniverse_mana_max%"
        - "&b❄ &fTemp: &b%huduniverse_temperature%°C"
        - "&🌿 &fBioma: &a%huduniverse_biome%"
        - "&c⚔ &fPVP: %huduniverse_pvp_color%%huduniverse_pvp%"
        - ""
        - "&8%huduniverse_coordinates%"
        - "&8━━━━━━━━━━━━━━"
```

Do not make HudUniVerse create a Bukkit scoreboard. TAB stays in control of the right side.

## Lower HUD / actionbar

`config.yml` has an optional actionbar. It is disabled by default because AuraSkills can also use the actionbar.

If enabled:

```yaml
actionbar:
  enabled: true
  refresh-ticks: 10
  format: "&b🌡 %huduniverse_temperature%°C &8| &d✦ %huduniverse_mana%/%huduniverse_mana_max% &8| &c⚔ %huduniverse_pvp%"
```

If AuraSkills' own actionbar is enabled, the two plugins may compete for the actionbar. In that case either leave this feature disabled or configure AuraSkills' actionbar appropriately.

## Permanent top location text (1.3.2)

HudUniVerse 1.3.2 keeps the physical Towny territory name permanently visible in the native BossBar text area at the top of the screen.

The plugin does not hide/show the bar when crossing a boundary. It reuses the same BossBar and only changes its title after the Towny debounce, preventing the entry/exit flash.

To make the visual bar itself invisible while keeping the text, install the bundled resource pack:
`resource-pack/HudUniVerse-TopText-26.2.zip`

The pack hides only the white BossBar background/progress sprites used by HudUniVerse. Other BossBar colors remain visible.

## Commands

- `/huduniverse status`
- `/huduniverse test`
- `/huduniverse reload`

Permission:
`huduniverse.admin`

## Notes about exact screen coordinates

Minecraft plugins cannot freely place ordinary text at arbitrary pixel coordinates in the vanilla HUD. The practical vanilla layout is:

- Town/location: Title
- Right side: TAB scoreboard
- Above hotbar: ActionBar
- Other bars: BossBar/title

For a pixel-perfect HUD matching the supplied screenshot, the next step is a client Resource Pack/font-glyph HUD. This plugin is the data/integration layer for that future HUD.


## 1.3.2 — Permanent top location text

- The location HUD is permanent.
- There is no stay/fade timeout.
- Territory changes update the existing BossBar title instead of removing/recreating it.
- The title contains only the territory name; no icon or subtitle.
- The companion 26.2 resource pack makes the BossBar graphics transparent, leaving only the text.

## 1.2.0 — Towny location vs. player town

- `%huduniverse_town%` is the Towny town the player belongs to.
- `%huduniverse_location%` is the Towny town at the player's current physical location.
- `%huduniverse_location_town%` is an alias for the physical location town.
- The location title uses the physical location town, so entering `Nova Hera Settlement` displays `Nova Hera Settlement`.
- The scoreboard should use `%huduniverse_town%` to show the player's own town.
- The central location title uses `%huduniverse_location%` and changes when the player crosses into another Towny town.

Towny is a compile-only dependency; the server still provides the Towny plugin at runtime.


## 1.3.2
The permanent top HUD can now display the physical Towny town and the Nation that owns that town using `{town}` and `{nation}`.


## 1.3.3 — Biome placeholder
- Adds `%huduniverse_biome%`, calculated from the player's current block biome.
- The TAB example includes the biome in the right-side scoreboard.
- Coordinates remain in the scoreboard; no Lunar/client HUD is required for the biome.
