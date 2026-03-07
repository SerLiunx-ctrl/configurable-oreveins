package com.serliunx.configurableoreveins.client;

import com.serliunx.configurableoreveins.network.LocatorVeinInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 客户端附近矿脉缓存。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public final class ClientNearbyVeinCache {
    private static int dimensionId = Integer.MIN_VALUE;
    private static int rangeChunks = 0;
    private static long lastUpdateTick = -1L;
    private static List<LocatorVeinInfo> nearbyVeins = Collections.emptyList();

    /** 构造 ClientNearbyVeinCache 实例。 */
    private ClientNearbyVeinCache() {}

    /**
     * 更新客户端附近矿脉缓存。
     *
     * @param newDimensionId 参数 newDimensionId。
     * @param newRangeChunks 参数 newRangeChunks。
     * @param veins 参数 veins。
    */
    public static void update(int newDimensionId, int newRangeChunks, List<LocatorVeinInfo> veins) {
        dimensionId = newDimensionId;
        rangeChunks = Math.max(0, newRangeChunks);
        nearbyVeins = veins == null ? Collections.<LocatorVeinInfo>emptyList() : new ArrayList<LocatorVeinInfo>(veins);
        Minecraft minecraft = Minecraft.getMinecraft();
        lastUpdateTick = minecraft.world == null ? -1L : minecraft.world.getTotalWorldTime();
    }

    /** 清空客户端附近矿脉缓存。 */
    public static void clear() {
        dimensionId = Integer.MIN_VALUE;
        rangeChunks = 0;
        lastUpdateTick = -1L;
        nearbyVeins = Collections.emptyList();
    }

    /**
     * 更新缓存中矿脉的已挖掘状态。
     *
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param veinHash 参数 veinHash。
     * @param mined 参数 mined。
    */
    public static void setMined(int dimensionId, int chunkX, int chunkZ, int veinHash, boolean mined) {
        if (ClientNearbyVeinCache.dimensionId != dimensionId || nearbyVeins.isEmpty()) {
            return;
        }
        List<LocatorVeinInfo> updated = new ArrayList<LocatorVeinInfo>(nearbyVeins.size());
        for (LocatorVeinInfo info : nearbyVeins) {
            if (info.getChunkX() == chunkX && info.getChunkZ() == chunkZ && info.getVeinHash() == veinHash) {
                updated.add(
                        new LocatorVeinInfo(
                                info.getVeinHash(),
                                mined,
                                info.getDimensionId(),
                                info.getChunkX(),
                                info.getChunkZ(),
                                info.getCenterX(),
                                info.getCenterY(),
                                info.getCenterZ(),
                                info.getName(),
                                info.getHighlightColor(),
                                info.getIconBlockId(),
                                info.getIconMeta(),
                                info.getTotalBlocks(),
                                info.getOreStateKeys(),
                                info.getOreCounts()));
                continue;
            }
            updated.add(info);
        }
        nearbyVeins = updated;
    }

    /**
     * 获取附近矿脉范围。
     *
     * @return 处理结果。
    */
    public static int getRangeChunks() {
        return rangeChunks;
    }

    /**
     * 获取缓存最后更新时间。
     *
     * @return 处理结果。
    */
    public static long getLastUpdateTick() {
        return lastUpdateTick;
    }

    /**
     * 获取指定矿脉在缓存中的总数。
     *
     * @param veinHash 参数 veinHash。
     * @return 处理结果。
    */
    public static int getNearbyCount(int veinHash) {
        int count = 0;
        for (LocatorVeinInfo info : getCurrentDimensionVeins()) {
            if (info.getVeinHash() == veinHash && !info.isMined()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取当前维度中最近的矿脉记录。
     *
     * @param veinHash 参数 veinHash。
     * @return 处理结果。
    */
    @Nullable
    public static LocatorVeinInfo getNearestVein(int veinHash) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null) {
            return null;
        }

        LocatorVeinInfo nearest = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (LocatorVeinInfo info : getCurrentDimensionVeins()) {
            if (info.getVeinHash() != veinHash || info.isMined()) {
                continue;
            }
            double dx = info.getCenterX() - player.posX;
            double dy = info.getCenterY() - player.posY;
            double dz = info.getCenterZ() - player.posZ;
            double distanceSq = (dx * dx) + (dy * dy) + (dz * dz);
            if (nearest == null || distanceSq < bestDistanceSq) {
                nearest = info;
                bestDistanceSq = distanceSq;
            }
        }
        return nearest;
    }

    /**
     * 获取当前维度的矿脉快照。
     *
     * @return 处理结果。
    */
    public static List<LocatorVeinInfo> getCurrentDimensionVeins() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.world.provider.getDimension() != dimensionId) {
            return Collections.emptyList();
        }
        return nearbyVeins;
    }
}
