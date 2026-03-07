# Configurable Ore Veins

Minecraft Forge 1.12.2 mod that generates ore veins from a JSON config file.

## Current features

- Reads vein definitions from `config/configurableoreveins/veins.json`
- Generates configurable veins during chunk generation
- Supports multiple output blocks in one vein
- Uses `weight` as the block ratio within a vein
- Supports `ELLIPSOID`, `SPHERE`, `BOX`, and `WORM` shapes
- Supports irregular vein outlines via `irregularity`
- Supports size randomization via `sizeMultiplierMin` and `sizeMultiplierMax`
- Supports density control via `density`
- Supports dimension, biome whitelist, and biome blacklist filtering
- Supports hot reload by file change detection and `/oreveins reload`
- Hardcoded generation policy: at most 1 vein per chunk, minimum spacing 2 chunks
- Adds a `Vein Locator` item to open a nearby-vein GUI
- Stores compact vein anchor records in the world save for locator queries
- Supports per-vein locator display name and highlight color from `veins.json`
- Supports per-vein locator icon configuration from `veins.json`
- Locator GUI entries show hover tooltips with saved vein size and ore statistics

## Build

Use JDK 8.

```powershell
$env:JAVA_HOME='C:\software\jdks\jdk1.8.0_202'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
```

## Config format

The mod writes a default config on first launch:

`config/configurableoreveins/veins.json`

General switches are written to:

`config/configurableoreveins.cfg`

Default behavior:

- Vanilla iron, gold, redstone, diamond, lapis, emerald, and coal generation are disabled by default
- The vein locator scan range and result count are configured in `configurableoreveins.cfg`
- Right-clicking the vein locator opens a vanilla-style GUI list of nearby generated veins
- Clicking a vein in the GUI sets the locator target, updates the compass needle, and enables a client-only highlight at the vein center
- If no vein is manually selected, the locator automatically points to the nearest generated vein within locator range without enabling highlight
- Locator tooltip ore statistics are saved when the vein is generated and rendered with block icons in the GUI

Example:

```json
{
  "veins": [
    {
      "name": "mixed_overworld_vein",
      "displayName": "Mixed Overworld Vein",
      "highlightColor": "#B2FF8C",
      "locatorIconBlock": "minecraft:iron_ore",
      "locatorIconMeta": 0,
      "enabled": true,
      "chunkChance": 0.12,
      "dimensionIds": [0],
      "biomes": [],
      "excludedBiomes": [
        "minecraft:deep_ocean"
      ],
      "minY": 8,
      "maxY": 48,
      "density": 0.82,
      "replaceableBlocks": [
        "minecraft:stone"
      ],
      "blocks": [
        {
          "block": "minecraft:iron_ore",
          "meta": 0,
          "weight": 70
        },
        {
          "block": "minecraft:gold_ore",
          "meta": 0,
          "weight": 20
        },
        {
          "block": "minecraft:redstone_ore",
          "meta": 0,
          "weight": 10
        }
      ],
      "shape": {
        "type": "WORM",
        "radiusX": 5,
        "radiusY": 3,
        "radiusZ": 4,
        "sizeMultiplierMin": 0.9,
        "sizeMultiplierMax": 1.35,
        "irregularity": 0.45,
        "steps": 10,
        "stepLength": 1.6
      }
    }
  ]
}
```

## Notes

- `block` must be a valid block registry id, such as `minecraft:iron_ore`
- `meta` is the metadata value for the target block state
- `weight` controls the ratio between blocks inside the vein
- `replaceableBlocks` controls which existing blocks can be replaced
- `replaceableBlocks` can include `minecraft:air` if you want to generate veins floating in the air for debugging
- `displayName` controls the locator GUI label and world highlight text
- `highlightColor` is a hex RGB color like `#B2FF8C` used by the client-only locator highlight
- `locatorIconBlock` and `locatorIconMeta` control the block icon shown in the locator GUI; if omitted, the highest-weight block is used
- `dimensionIds` uses standard dimension ids, for example overworld `0`, nether `-1`, end `1`
- `chunkChance` is the chance that a vein definition is allowed to occupy a candidate anchor chunk
- `density` is a `0.0` to `1.0` fill rate inside the selected vein volume
- `biomes` is a whitelist of biome registry ids, empty means all biomes
- `excludedBiomes` blocks specific biome registry ids
- `sizeMultiplierMin` and `sizeMultiplierMax` randomize the final vein size per spawn attempt
- `irregularity` distorts the vein boundary and makes the shape less uniform
- `steps` and `stepLength` are mainly used by `WORM` veins
- Chunk distribution is now hardcoded in code: one vein max per chunk, with a minimum spacing of 2 chunks between generated veins
- Change the config file while the server is running and the mod will reload it automatically within a few seconds, or run `/oreveins reload`
- `configurableoreveins.cfg` contains the global vanilla ore disable switches for iron, gold, redstone, diamond, lapis, emerald, and coal
- Generated vein anchors are stored compactly in the save using chunk key + packed center position + vein hash, not full block lists
- Saved vein records also store the locator icon block id and meta, so the GUI does not need to recalculate it every time
- Saved vein records also store total placed blocks and top ore counts for tooltip display, so the GUI does not need to recalculate statistics
