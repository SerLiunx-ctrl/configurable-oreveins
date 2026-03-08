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
    private List<Integer> dimensionIds = new ArrayList<>();
    private double chunkChance = 0.12D;
    private int attemptsPerChunk = 1;
    private int minY = 0;
    private int maxY = 64;
    private double density = 1.0D;
    private List<String> replaceableBlocks = new ArrayList<>();
    private List<String> biomes = new ArrayList<>();
    private List<String> excludedBiomes = new ArrayList<>();
    private List<BlockEntry> blocks = new ArrayList<>();
    private VeinShapeConfig shape = new VeinShapeConfig();

    public String getName() {
        return name == null ? "unnamed" : name;
    }

    public String getDisplayName() {
        return displayName == null || displayName.trim().isEmpty() ? getName() : displayName;
    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public String getLocatorIconBlock() {
        if (locatorIconBlock != null && !locatorIconBlock.trim().isEmpty()) {
            return locatorIconBlock;
        }
        BlockEntry dominantBlock = getDominantBlock();
        return dominantBlock == null ? "" : dominantBlock.getBlock();
    }

    public int getLocatorIconMeta() {
        if (locatorIconBlock != null && !locatorIconBlock.trim().isEmpty()) {
            return locatorIconMeta;
        }
        BlockEntry dominantBlock = getDominantBlock();
        return dominantBlock == null ? 0 : dominantBlock.getMeta();
    }

    public List<Integer> getDimensionIds() {
        return dimensionIds == null ? new ArrayList<>() : dimensionIds;
    }

    public int getAttemptsPerChunk() {
        return Math.max(0, attemptsPerChunk);
    }

    public double getChunkChance() {
        return Math.max(0.0D, Math.min(1.0D, chunkChance));
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public double getDensity() {
        return Math.max(0.0D, Math.min(1.0D, density));
    }

    public List<String> getReplaceableBlocks() {
        return replaceableBlocks == null ? new ArrayList<>() : replaceableBlocks;
    }

    public List<String> getBiomes() {
        return biomes == null ? new ArrayList<>() : biomes;
    }

    public List<String> getExcludedBiomes() {
        return excludedBiomes == null ? new ArrayList<>() : excludedBiomes;
    }

    public List<BlockEntry> getBlocks() {
        return blocks == null ? new ArrayList<>() : blocks;
    }

    public VeinShapeConfig getShape() {
        return shape == null ? new VeinShapeConfig() : shape;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void setLocatorIconBlock(String locatorIconBlock) {
        this.locatorIconBlock = locatorIconBlock;
    }

    public void setLocatorIconMeta(int locatorIconMeta) {
        this.locatorIconMeta = locatorIconMeta;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDimensionIds(List<Integer> dimensionIds) {
        this.dimensionIds = dimensionIds;
    }

    public void setAttemptsPerChunk(int attemptsPerChunk) {
        this.attemptsPerChunk = attemptsPerChunk;
    }

    public void setChunkChance(double chunkChance) {
        this.chunkChance = chunkChance;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public void setReplaceableBlocks(List<String> replaceableBlocks) {
        this.replaceableBlocks = replaceableBlocks;
    }

    public void setBiomes(List<String> biomes) {
        this.biomes = biomes;
    }

    public void setExcludedBiomes(List<String> excludedBiomes) {
        this.excludedBiomes = excludedBiomes;
    }

    public void setBlocks(List<BlockEntry> blocks) {
        this.blocks = blocks;
    }

    public void setShape(VeinShapeConfig shape) {
        this.shape = shape;
    }

    public boolean matchesDimension(int dimensionId) {
        return getDimensionIds().isEmpty() || getDimensionIds().contains(dimensionId);
    }

    /**
     * 判断矿脉是否匹配指定群系.
     *
     * @param biomeName 生物群系名称
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
