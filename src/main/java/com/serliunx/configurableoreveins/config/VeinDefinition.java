package com.serliunx.configurableoreveins.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条矿脉配置定义。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class VeinDefinition {
    private String name = "unnamed";
    private String displayName = "";
    private String highlightColor = "#B2FF8C";
    private String locatorIconBlock = "";
    private int locatorIconMeta = 0;
    private boolean enabled = true;
    private List<Integer> dimensionIds = new ArrayList<Integer>();
    private double chunkChance = 0.12D;
    private int attemptsPerChunk = 1;
    private int minY = 0;
    private int maxY = 64;
    private double density = 1.0D;
    private List<String> replaceableBlocks = new ArrayList<String>();
    private List<String> biomes = new ArrayList<String>();
    private List<String> excludedBiomes = new ArrayList<String>();
    private List<BlockEntry> blocks = new ArrayList<BlockEntry>();
    private VeinShapeConfig shape = new VeinShapeConfig();

    /**
     * 获取命令名称。
     *
     * @return 处理结果。
    */
    public String getName() {
        return name == null ? "unnamed" : name;
    }

    /**
     * 获取 DisplayName。
     *
     * @return 处理结果。
    */
    public String getDisplayName() {
        return displayName == null || displayName.trim().isEmpty() ? getName() : displayName;
    }

    /**
     * 获取 HighlightColor。
     *
     * @return 处理结果。
    */
    public int getHighlightColor() {
        String raw = highlightColor == null ? "" : highlightColor.trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }

        try {
            return Integer.parseInt(raw, 16) & 0xFFFFFF;
        } catch (NumberFormatException exception) {
            return 0xB2FF8C;
        }
    }

    /**
     * 判断 Enabled。
     *
     * @return 处理结果。
    */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取 LocatorIconBlock。
     *
     * @return 处理结果。
    */
    public String getLocatorIconBlock() {
        if (locatorIconBlock != null && !locatorIconBlock.trim().isEmpty()) {
            return locatorIconBlock;
        }
        BlockEntry dominantBlock = getDominantBlock();
        return dominantBlock == null ? "" : dominantBlock.getBlock();
    }

    /**
     * 获取 LocatorIconMeta。
     *
     * @return 处理结果。
    */
    public int getLocatorIconMeta() {
        if (locatorIconBlock != null && !locatorIconBlock.trim().isEmpty()) {
            return locatorIconMeta;
        }
        BlockEntry dominantBlock = getDominantBlock();
        return dominantBlock == null ? 0 : dominantBlock.getMeta();
    }

    /**
     * 获取 DimensionIds。
     *
     * @return 处理结果。
    */
    public List<Integer> getDimensionIds() {
        return dimensionIds == null ? new ArrayList<Integer>() : dimensionIds;
    }

    /**
     * 获取 AttemptsPerChunk。
     *
     * @return 处理结果。
    */
    public int getAttemptsPerChunk() {
        return Math.max(0, attemptsPerChunk);
    }

    /**
     * 获取 ChunkChance。
     *
     * @return 处理结果。
    */
    public double getChunkChance() {
        return Math.max(0.0D, Math.min(1.0D, chunkChance));
    }

    /**
     * 获取 MinY。
     *
     * @return 处理结果。
    */
    public int getMinY() {
        return minY;
    }

    /**
     * 获取 MaxY。
     *
     * @return 处理结果。
    */
    public int getMaxY() {
        return maxY;
    }

    /**
     * 获取 Density。
     *
     * @return 处理结果。
    */
    public double getDensity() {
        return Math.max(0.0D, Math.min(1.0D, density));
    }

    /**
     * 获取 ReplaceableBlocks。
     *
     * @return 处理结果。
    */
    public List<String> getReplaceableBlocks() {
        return replaceableBlocks == null ? new ArrayList<String>() : replaceableBlocks;
    }

    /**
     * 获取 Biomes。
     *
     * @return 处理结果。
    */
    public List<String> getBiomes() {
        return biomes == null ? new ArrayList<String>() : biomes;
    }

    /**
     * 获取 ExcludedBiomes。
     *
     * @return 处理结果。
    */
    public List<String> getExcludedBiomes() {
        return excludedBiomes == null ? new ArrayList<String>() : excludedBiomes;
    }

    /**
     * 获取 Blocks。
     *
     * @return 处理结果。
    */
    public List<BlockEntry> getBlocks() {
        return blocks == null ? new ArrayList<BlockEntry>() : blocks;
    }

    /**
     * 获取 Shape。
     *
     * @return 处理结果。
    */
    public VeinShapeConfig getShape() {
        return shape == null ? new VeinShapeConfig() : shape;
    }

    /**
     * 设置 Name。
     *
     * @param name 参数 name。
    */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置 DisplayName。
     *
     * @param displayName 参数 displayName。
    */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 设置 HighlightColor。
     *
     * @param highlightColor 参数 highlightColor。
    */
    public void setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
    }

    /**
     * 设置 LocatorIconBlock。
     *
     * @param locatorIconBlock 参数 locatorIconBlock。
    */
    public void setLocatorIconBlock(String locatorIconBlock) {
        this.locatorIconBlock = locatorIconBlock;
    }

    /**
     * 设置 LocatorIconMeta。
     *
     * @param locatorIconMeta 参数 locatorIconMeta。
    */
    public void setLocatorIconMeta(int locatorIconMeta) {
        this.locatorIconMeta = locatorIconMeta;
    }

    /**
     * 设置 Enabled。
     *
     * @param enabled 参数 enabled。
    */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置 DimensionIds。
     *
     * @param dimensionIds 参数 dimensionIds。
    */
    public void setDimensionIds(List<Integer> dimensionIds) {
        this.dimensionIds = dimensionIds;
    }

    /**
     * 设置 AttemptsPerChunk。
     *
     * @param attemptsPerChunk 参数 attemptsPerChunk。
    */
    public void setAttemptsPerChunk(int attemptsPerChunk) {
        this.attemptsPerChunk = attemptsPerChunk;
    }

    /**
     * 设置 ChunkChance。
     *
     * @param chunkChance 参数 chunkChance。
    */
    public void setChunkChance(double chunkChance) {
        this.chunkChance = chunkChance;
    }

    /**
     * 设置 MinY。
     *
     * @param minY 参数 minY。
    */
    public void setMinY(int minY) {
        this.minY = minY;
    }

    /**
     * 设置 MaxY。
     *
     * @param maxY 参数 maxY。
    */
    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    /**
     * 设置 Density。
     *
     * @param density 参数 density。
    */
    public void setDensity(double density) {
        this.density = density;
    }

    /**
     * 设置 ReplaceableBlocks。
     *
     * @param replaceableBlocks 参数 replaceableBlocks。
    */
    public void setReplaceableBlocks(List<String> replaceableBlocks) {
        this.replaceableBlocks = replaceableBlocks;
    }

    /**
     * 设置 Biomes。
     *
     * @param biomes 参数 biomes。
    */
    public void setBiomes(List<String> biomes) {
        this.biomes = biomes;
    }

    /**
     * 设置 ExcludedBiomes。
     *
     * @param excludedBiomes 参数 excludedBiomes。
    */
    public void setExcludedBiomes(List<String> excludedBiomes) {
        this.excludedBiomes = excludedBiomes;
    }

    /**
     * 设置 Blocks。
     *
     * @param blocks 参数 blocks。
    */
    public void setBlocks(List<BlockEntry> blocks) {
        this.blocks = blocks;
    }

    /**
     * 设置 Shape。
     *
     * @param shape 参数 shape。
    */
    public void setShape(VeinShapeConfig shape) {
        this.shape = shape;
    }

    /**
     * 判断矿脉是否匹配指定维度。
     *
     * @param dimensionId 参数 dimensionId。
     * @return 处理结果。
    */
    public boolean matchesDimension(int dimensionId) {
        return getDimensionIds().isEmpty() || getDimensionIds().contains(dimensionId);
    }

    /**
     * 判断矿脉是否匹配指定群系。
     *
     * @param biomeName 参数 biomeName。
     * @return 处理结果。
    */
    public boolean matchesBiome(String biomeName) {
        if (biomeName == null || biomeName.isEmpty()) {
            return getBiomes().isEmpty();
        }

        if (getExcludedBiomes().contains(biomeName)) {
            return false;
        }
        return getBiomes().isEmpty() || getBiomes().contains(biomeName);
    }

    /**
     * 获取 DominantBlock。
     *
     * @return 处理结果。
    */
    private BlockEntry getDominantBlock() {
        BlockEntry dominant = null;
        int bestWeight = Integer.MIN_VALUE;
        for (BlockEntry block : getBlocks()) {
            if (block == null) {
                continue;
            }

            if (dominant == null || block.getWeight() > bestWeight) {
                dominant = block;
                bestWeight = block.getWeight();
            }
        }

        return dominant;
    }
}
