package com.serliunx.configurableoreveins.config;

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

    /**
     * 获取 Block。
     *
     * @return 处理结果。
    */
    public String getBlock() {
        return block;
    }

    /**
     * 获取 Meta。
     *
     * @return 处理结果。
    */
    public int getMeta() {
        return meta;
    }

    /**
     * 获取 Weight。
     *
     * @return 处理结果。
    */
    public int getWeight() {
        return Math.max(0, weight);
    }

    /**
     * 设置 Block。
     *
     * @param block 参数 block。
    */
    public void setBlock(String block) {
        this.block = block;
    }

    /**
     * 设置 Meta。
     *
     * @param meta 参数 meta。
    */
    public void setMeta(int meta) {
        this.meta = meta;
    }

    /**
     * 设置 Weight。
     *
     * @param weight 参数 weight。
    */
    public void setWeight(int weight) {
        this.weight = weight;
    }
}
