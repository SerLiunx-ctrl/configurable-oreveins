# Configurable Ore Veins

这是一个基于 Minecraft Forge 1.12.2 的矿脉生成模组，支持通过 JSON 配置文件定义矿脉，并提供矿脉定位器 GUI。

## 当前功能

- 从 `config/configurableoreveins/veins.json` 读取矿脉定义
- 按配置在世界生成阶段生成矿脉
- 支持一个矿脉内混合多种矿石方块
- 使用 `weight` 控制矿物方块比例
- 支持 `ELLIPSOID`、`SPHERE`、`BOX`、`WORM` 四种形状
- 支持通过 `irregularity` 控制矿脉边缘扰动
- 支持 `sizeMultiplierMin` 和 `sizeMultiplierMax` 做大小随机
- 支持 `density` 控制填充密度
- 支持维度、群系白名单、群系黑名单筛选
- 支持配置热重载和 `/oreveins reload`
- 硬编码分布规则：每个区块最多一个矿脉锚点，最小间隔 2 个区块
- 提供 `矿脉定位仪器`
- 在存档中保存紧凑的矿脉记录，供定位器查询
- 支持为每个矿脉配置显示名称、高亮颜色、定位图标
- 定位 GUI 支持搜索、排序、滚动、图标显示和悬浮 tooltip
- 未手动选中矿脉时，定位器会自动指向附近最近的已生成矿脉，但不会高亮
- tooltip 所需的矿脉大小和矿石统计会在生成时写入存档，不会实时计算

## 构建

使用 JDK 8：

```powershell
$env:JAVA_HOME='C:\software\jdks\jdk1.8.0_202'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
```

## 配置文件

首次启动时会自动生成：

- `config/configurableoreveins/veins.json`
- `config/configurableoreveins.cfg`

默认行为：

- 默认禁用原版铁矿、金矿、红石、钻石、青金石、绿宝石、煤矿生成
- `configurableoreveins.cfg` 控制定向器扫描范围和最大结果数
- 右键矿脉定位仪器会打开附近矿脉 GUI
- 点击 GUI 条目会锁定目标、更新指南针方向，并启用客户端高亮
- 清除目标后，会恢复为自动指向最近矿脉

## 配置示例

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

## 说明

- `block` 必须是有效的方块注册名，例如 `minecraft:iron_ore`
- `meta` 是方块状态使用的 metadata
- `weight` 控制矿物在矿脉中的占比
- `replaceableBlocks` 控制可被替换的原始方块
- `replaceableBlocks` 可填写 `minecraft:air`，方便把矿脉生在空中做调试
- `displayName` 控制 GUI 名称和高亮名称
- `highlightColor` 控制客户端高亮颜色
- `locatorIconBlock` 和 `locatorIconMeta` 控制 GUI 中显示的矿石图标；未配置时会退回到权重最高的矿物
- `chunkChance` 取值为 `0.0` 到 `1.0`
- `density` 取值为 `0.0` 到 `1.0`
- `biomes` 为空时表示所有群系都允许
- 存档中只保存定位器需要的紧凑数据，不保存整条矿脉的完整方块列表
