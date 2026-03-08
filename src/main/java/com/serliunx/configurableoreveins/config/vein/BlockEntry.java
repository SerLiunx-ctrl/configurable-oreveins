package com.serliunx.configurableoreveins.config.vein;

/**
 * 矿物方块输出配置项。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class BlockEntry {

    private String block;
    private int meta = 0;
    private int weight = 1;

    public String getBlock() {
        return block;
    }

    public int getMeta() {
        return meta;
    }

    public int getWeight() {
        return Math.max(0, weight);
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
