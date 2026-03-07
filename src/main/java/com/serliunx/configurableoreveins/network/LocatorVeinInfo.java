package com.serliunx.configurableoreveins.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * 服务端发送到客户端的矿脉信息载体。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class LocatorVeinInfo {
    private final int veinHash;
    private final boolean mined;
    private final int dimensionId;
    private final int chunkX;
    private final int chunkZ;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final String name;
    private final int highlightColor;
    private final int iconBlockId;
    private final int iconMeta;
    private final int totalBlocks;
    private final int[] oreStateKeys;
    private final int[] oreCounts;

    /**
     * 构造 LocatorVeinInfo 实例。
     *
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param centerX 参数 centerX。
     * @param centerY 参数 centerY。
     * @param centerZ 参数 centerZ。
     * @param name 参数 name。
     * @param highlightColor 参数 highlightColor。
     * @param iconBlockId 参数 iconBlockId。
     * @param iconMeta 参数 iconMeta。
    */
    public LocatorVeinInfo(
            int veinHash,
            boolean mined,
            int dimensionId,
            int chunkX,
            int chunkZ,
            int centerX,
            int centerY,
            int centerZ,
            String name,
            int highlightColor,
            int iconBlockId,
            int iconMeta) {
        this(
                veinHash,
                mined,
                dimensionId,
                chunkX,
                chunkZ,
                centerX,
                centerY,
                centerZ,
                name,
                highlightColor,
                iconBlockId,
                iconMeta,
                0,
                new int[0],
                new int[0]);
    }

    /**
     * 构造 LocatorVeinInfo 实例。
     *
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param centerX 参数 centerX。
     * @param centerY 参数 centerY。
     * @param centerZ 参数 centerZ。
     * @param name 参数 name。
     * @param highlightColor 参数 highlightColor。
     * @param iconBlockId 参数 iconBlockId。
     * @param iconMeta 参数 iconMeta。
     * @param totalBlocks 参数 totalBlocks。
     * @param oreStateKeys 参数 oreStateKeys。
     * @param oreCounts 参数 oreCounts。
    */
    public LocatorVeinInfo(
            int veinHash,
            boolean mined,
            int dimensionId,
            int chunkX,
            int chunkZ,
            int centerX,
            int centerY,
            int centerZ,
            String name,
            int highlightColor,
            int iconBlockId,
            int iconMeta,
            int totalBlocks,
            int[] oreStateKeys,
            int[] oreCounts) {
        this.veinHash = veinHash;
        this.mined = mined;
        this.dimensionId = dimensionId;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.name = name;
        this.highlightColor = highlightColor;
        this.iconBlockId = iconBlockId;
        this.iconMeta = iconMeta;
        this.totalBlocks = Math.max(0, totalBlocks);
        this.oreStateKeys = oreStateKeys == null ? new int[0] : oreStateKeys;
        this.oreCounts = oreCounts == null ? new int[0] : oreCounts;
    }

    /**
     * 从网络字节流读取矿脉信息。
     *
     * @param buf 参数 buf。
     * @return 处理结果。
    */
    public static LocatorVeinInfo fromBytes(ByteBuf buf) {
        return new LocatorVeinInfo(
                buf.readInt(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                ByteBufUtils.readUTF8String(buf),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                readIntArray(buf),
                readIntArray(buf));
    }

    /**
     * 将矿脉信息写入网络字节流。
     *
     * @param buf 参数 buf。
    */
    public void toBytes(ByteBuf buf) {
        buf.writeInt(veinHash);
        buf.writeBoolean(mined);
        buf.writeInt(dimensionId);
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeInt(centerX);
        buf.writeInt(centerY);
        buf.writeInt(centerZ);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(highlightColor);
        buf.writeInt(iconBlockId);
        buf.writeInt(iconMeta);
        buf.writeInt(totalBlocks);
        writeIntArray(buf, oreStateKeys);
        writeIntArray(buf, oreCounts);
    }

    /**
     * 读取整型数组。
     *
     * @param buf 参数 buf。
     * @return 处理结果。
    */
    private static int[] readIntArray(ByteBuf buf) {
        int length = buf.readInt();
        int[] values = new int[Math.max(0, length)];
        for (int index = 0; index < values.length; index++) {
            values[index] = buf.readInt();
        }
        return values;
    }

    /**
     * 写入整型数组。
     *
     * @param buf 参数 buf。
     * @param values 参数 values。
    */
    private static void writeIntArray(ByteBuf buf, int[] values) {
        int[] safe = values == null ? new int[0] : values;
        buf.writeInt(safe.length);
        for (int value : safe) {
            buf.writeInt(value);
        }
    }

    /**
     * 获取 VeinHash。
     *
     * @return 处理结果。
    */
    public int getVeinHash() {
        return veinHash;
    }

    /**
     * 判断矿脉是否已被当前玩家标记为已挖掘。
     *
     * @return 处理结果。
    */
    public boolean isMined() {
        return mined;
    }

    /**
     * 获取 DimensionId。
     *
     * @return 处理结果。
    */
    public int getDimensionId() {
        return dimensionId;
    }

    /**
     * 获取 ChunkX。
     *
     * @return 处理结果。
    */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * 获取 ChunkZ。
     *
     * @return 处理结果。
    */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * 获取 CenterX。
     *
     * @return 处理结果。
    */
    public int getCenterX() {
        return centerX;
    }

    /**
     * 获取 CenterY。
     *
     * @return 处理结果。
    */
    public int getCenterY() {
        return centerY;
    }

    /**
     * 获取 CenterZ。
     *
     * @return 处理结果。
    */
    public int getCenterZ() {
        return centerZ;
    }

    /**
     * 获取命令名称。
     *
     * @return 处理结果。
    */
    public String getName() {
        return name;
    }

    /**
     * 获取 HighlightColor。
     *
     * @return 处理结果。
    */
    public int getHighlightColor() {
        return highlightColor;
    }

    /**
     * 获取 IconBlockId。
     *
     * @return 处理结果。
    */
    public int getIconBlockId() {
        return iconBlockId;
    }

    /**
     * 获取 IconMeta。
     *
     * @return 处理结果。
    */
    public int getIconMeta() {
        return iconMeta;
    }

    /**
     * 获取 TotalBlocks。
     *
     * @return 处理结果。
    */
    public int getTotalBlocks() {
        return totalBlocks;
    }

    /**
     * 获取 OreStateKeys。
     *
     * @return 处理结果。
    */
    public int[] getOreStateKeys() {
        return oreStateKeys;
    }

    /**
     * 获取 OreCounts。
     *
     * @return 处理结果。
    */
    public int[] getOreCounts() {
        return oreCounts;
    }
}
