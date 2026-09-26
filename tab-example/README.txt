TAB — SCOREBOARD CORRETO PARA O HUD UNIVERSE 1.3.0

O bloco precisa ficar dentro de `scoreboard:` no `plugins/TAB/config.yml`.
Não coloque `scoreboards:` sozinho no topo do arquivo.

scoreboard:
  enabled: true
  scoreboards:
    universe:
      title: "&2✦ &aUniVerse &2✦"
      lines:
        - "&8━━━━━━━━━━━━"
        - "&a⌂ Cidade:||&f%huduniverse_town%"
        - "&d✦ Mana:||&d%huduniverse_mana%&7/%huduniverse_mana_max%"
        - "&b❄ Temp:||&b%huduniverse_temperature%°C"
        - "&🌿 Bioma:||&a%huduniverse_biome%"
        - "&7☀ Estação:||&f%huduniverse_season%"
        - "&c⚔ PVP:||%huduniverse_pvp_color%%huduniverse_pvp%"
        - ""
        - "&fX:||&7%huduniverse_x%"
        - "&fY:||&7%huduniverse_y%"
        - "&fZ:||&7%huduniverse_z%"
        - "&8━━━━━━━━━━━━"

Depois execute:
  /papi parse me %huduniverse_town%
  /papi parse me %huduniverse_x%
  /tab reload
  /tab scoreboard show universe

O TAB atualiza placeholders dinamicamente; não é necessário um loop do HudUniVerse para o scoreboard.
O `||` é o recurso nativo do TAB para colocar texto no lado direito em clientes/servidores 1.20.3+.

IMPORTANTE: se `/papi parse me %huduniverse_town%` não retornar o nome da cidade, o problema é no PlaceholderAPI/Towny, não no lado direito do TAB.


HudUniVerse 1.3.3:
- The physical Towny location is shown permanently at the top by HudUniVerse.
- `%huduniverse_town%` remains the player's own Towny town and belongs in the TAB scoreboard.
- Install `resource-pack/HudUniVerse-TopText-26.2.zip` on clients to hide the BossBar graphics and keep only the location text.


HudUniVerse 1.3.3: `%huduniverse_biome%` returns the biome at the player's current block for use in TAB.
