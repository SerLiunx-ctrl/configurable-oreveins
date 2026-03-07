package com.serliunx.configurableoreveins.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认矿脉配置构造器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public final class DefaultConfigs {

    /** 构造 DefaultConfigs 实例。 */
    private DefaultConfigs() {}

    /**
     * 创建默认配置对象。
     *
     * @return 处理结果。
    */
    public static ModConfigData create() {
        ModConfigData data = new ModConfigData();
        List<VeinDefinition> veins = new ArrayList<>();

        VeinDefinition overworldVein = new VeinDefinition();
        overworldVein.setName("mixed_overworld_vein");
        overworldVein.setDisplayName("Mixed Overworld Vein");
        overworldVein.setHighlightColor("#B2FF8C");
        overworldVein.setLocatorIconBlock("minecraft:iron_ore");
        overworldVein.setLocatorIconMeta(0);
        overworldVein.setEnabled(true);
        overworldVein.setDimensionIds(Collections.singletonList(0));
        overworldVein.setMinY(8);
        overworldVein.setMaxY(48);
        overworldVein.setDensity(0.82D);
        overworldVein.setReplaceableBlocks(Collections.singletonList("minecraft:stone"));
        overworldVein.setBiomes(new ArrayList<>());
        overworldVein.setExcludedBiomes(new ArrayList<>());

        VeinShapeConfig shape = new VeinShapeConfig();
        shape.setType("WORM");
        shape.setRadiusX(5);
        shape.setRadiusY(3);
        shape.setRadiusZ(4);
        shape.setSizeMultiplierMin(0.9D);
        shape.setSizeMultiplierMax(1.35D);
        shape.setIrregularity(0.45D);
        shape.setSteps(10);
        shape.setStepLength(1.6D);
        overworldVein.setShape(shape);

        List<BlockEntry> blocks = new ArrayList<>();
        blocks.add(createBlock("minecraft:iron_ore", 0, 70));
        blocks.add(createBlock("minecraft:gold_ore", 0, 20));
        blocks.add(createBlock("minecraft:redstone_ore", 0, 10));
        overworldVein.setBlocks(blocks);
        veins.add(overworldVein);
        data.setVeins(veins);
        return data;
    }

    /**
     * 创建 Block。
     *
     * @param block 参数 block。
     * @param meta 参数 meta。
     * @param weight 参数 weight。
     * @return 处理结果。
    */
    private static BlockEntry createBlock(String block, int meta, int weight) {
        BlockEntry entry = new BlockEntry();
        entry.setBlock(block);
        entry.setMeta(meta);
        entry.setWeight(weight);
        return entry;
    }
}
