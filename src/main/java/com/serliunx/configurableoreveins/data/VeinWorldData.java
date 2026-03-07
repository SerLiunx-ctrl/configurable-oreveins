package com.serliunx.configurableoreveins.data;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/**
 * 矿脉世界存档数据。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class VeinWorldData extends WorldSavedData {
    private static final String DATA_NAME = ConfigurableOreVeinsMod.MOD_ID + "_veins";
    private static final String DIMENSIONS_TAG = "dimensions";
    private static final String DIMENSION_ID_TAG = "id";
    private static final String CHUNK_X_TAG = "chunkX";
    private static final String CHUNK_Z_TAG = "chunkZ";
    private static final String META_TAG = "meta";
    private static final String VEIN_HASH_TAG = "veinHash";
    private static final String ICON_STATE_TAG = "iconState";
    private static final String TOTAL_BLOCKS_TAG = "totalBlocks";
    private static final String ORE_STATE_KEYS_TAG = "oreStateKeys";
    private static final String ORE_COUNTS_TAG = "oreCounts";
    private static final int MAX_STORED_STATS = 4;

    private final Map<Integer, Map<Long, VeinRecord>> recordsByDimension =
            new HashMap<Integer, Map<Long, VeinRecord>>();

    /** 构造 VeinWorldData 实例。 */
    public VeinWorldData() {
        super(DATA_NAME);
    }

    /**
     * 构造 VeinWorldData 实例。
     *
     * @param name 参数 name。
    */
    public VeinWorldData(String name) {
        super(name);
    }

    /**
     * 获取世界矿脉存档对象。
     *
     * @param world 参数 world。
     * @return 处理结果。
    */
    public static VeinWorldData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        VeinWorldData data = (VeinWorldData) storage.getOrLoadData(VeinWorldData.class, DATA_NAME);
        if (data == null) {
            data = new VeinWorldData();
            storage.setData(DATA_NAME, data);
        }

        return data;
    }

    /**
     * 记录已生成矿脉的存档信息。
     *
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param centerX 参数 centerX。
     * @param centerY 参数 centerY。
     * @param centerZ 参数 centerZ。
     * @param veinHash 参数 veinHash。
     * @param iconBlockId 参数 iconBlockId。
     * @param iconMeta 参数 iconMeta。
     * @param totalBlocks 参数 totalBlocks。
     * @param stats 参数 stats。
    */
    public void recordGeneratedVein(
            int dimensionId,
            int chunkX,
            int chunkZ,
            int centerX,
            int centerY,
            int centerZ,
            int veinHash,
            int iconBlockId,
            int iconMeta,
            int totalBlocks,
            List<StatRecord> stats) {
        Map<Long, VeinRecord> dimensionRecords = getDimensionRecords(dimensionId);
        long chunkKey = packChunkKey(chunkX, chunkZ);
        VeinRecord existing = dimensionRecords.get(chunkKey);
        int localX = centerX - (chunkX << 4);
        int localZ = centerZ - (chunkZ << 4);
        VeinRecord record =
                new VeinRecord(
                        chunkX,
                        chunkZ,
                        localX,
                        centerY,
                        localZ,
                        veinHash,
                        iconBlockId,
                        iconMeta,
                        totalBlocks,
                        normalizeStats(stats));
        if (!record.equals(existing)) {
            dimensionRecords.put(chunkKey, record);
            markDirty();
        }
    }

    /**
     * 查找附近已记录矿脉。
     *
     * @param dimensionId 参数 dimensionId。
     * @param centerChunkX 参数 centerChunkX。
     * @param centerChunkZ 参数 centerChunkZ。
     * @param rangeChunks 参数 rangeChunks。
     * @return 处理结果。
    */
    public List<VeinRecord> findNearby(
            int dimensionId, int centerChunkX, int centerChunkZ, int rangeChunks) {
        Map<Long, VeinRecord> dimensionRecords = recordsByDimension.get(dimensionId);
        if (dimensionRecords == null || dimensionRecords.isEmpty()) {
            return Collections.emptyList();
        }

        List<VeinRecord> results = new ArrayList<VeinRecord>();
        for (int chunkX = centerChunkX - rangeChunks; chunkX <= centerChunkX + rangeChunks; chunkX++) {
            for (int chunkZ = centerChunkZ - rangeChunks;
                    chunkZ <= centerChunkZ + rangeChunks;
                    chunkZ++) {
                VeinRecord record = dimensionRecords.get(packChunkKey(chunkX, chunkZ));
                if (record != null) {
                    results.add(record);
                }
            }
        }

        return results;
    }

    /**
     * 查找最近的已记录矿脉。
     *
     * @param dimensionId 参数 dimensionId。
     * @param centerChunkX 参数 centerChunkX。
     * @param centerChunkZ 参数 centerChunkZ。
     * @param rangeChunks 参数 rangeChunks。
     * @param playerX 参数 playerX。
     * @param playerZ 参数 playerZ。
     * @return 处理结果。
    */
    @Nullable
    public VeinRecord findNearest(
            int dimensionId,
            int centerChunkX,
            int centerChunkZ,
            int rangeChunks,
            double playerX,
            double playerZ) {
        List<VeinRecord> candidates = findNearby(dimensionId, centerChunkX, centerChunkZ, rangeChunks);
        VeinRecord nearest = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (VeinRecord record : candidates) {
            double dx = record.getCenterX() - playerX;
            double dz = record.getCenterZ() - playerZ;
            double distanceSq = (dx * dx) + (dz * dz);
            if (nearest == null || distanceSq < bestDistanceSq) {
                nearest = record;
                bestDistanceSq = distanceSq;
            }
        }

        return nearest;
    }

    /**
     * 从 NBT 读取矿脉存档数据。
     *
     * @param nbt 参数 nbt。
    */
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        recordsByDimension.clear();
        int[] dimensionIds = nbt.getIntArray(DIMENSIONS_TAG);
        for (int dimensionId : dimensionIds) {
            NBTTagCompound dimensionTag = nbt.getCompoundTag(String.valueOf(dimensionId));
            int[] chunkXs = dimensionTag.getIntArray(CHUNK_X_TAG);
            int[] chunkZs = dimensionTag.getIntArray(CHUNK_Z_TAG);
            int[] metas = dimensionTag.getIntArray(META_TAG);
            int[] veinHashes = dimensionTag.getIntArray(VEIN_HASH_TAG);
            int[] iconStates = dimensionTag.getIntArray(ICON_STATE_TAG);
            int[] totalBlocks = dimensionTag.getIntArray(TOTAL_BLOCKS_TAG);
            int[] oreStateKeys = dimensionTag.getIntArray(ORE_STATE_KEYS_TAG);
            int[] oreCounts = dimensionTag.getIntArray(ORE_COUNTS_TAG);
            int count =
                    Math.min(
                            Math.min(chunkXs.length, chunkZs.length), Math.min(metas.length, veinHashes.length));
            Map<Long, VeinRecord> dimensionRecords = getDimensionRecords(dimensionId);
            for (int index = 0; index < count; index++) {
                int iconState = index < iconStates.length ? iconStates[index] : 0;
                int placed = index < totalBlocks.length ? totalBlocks[index] : 0;
                VeinRecord record =
                        VeinRecord.fromPacked(
                                chunkXs[index],
                                chunkZs[index],
                                metas[index],
                                veinHashes[index],
                                iconState,
                                placed,
                                extractStats(oreStateKeys, oreCounts, index));
                dimensionRecords.put(packChunkKey(record.getChunkX(), record.getChunkZ()), record);
            }
        }
    }

    /**
     * 将矿脉存档数据写入 NBT。
     *
     * @param compound 参数 compound。
     * @return 处理结果。
    */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        int[] dimensionIds = new int[recordsByDimension.size()];
        int dimensionIndex = 0;
        for (Map.Entry<Integer, Map<Long, VeinRecord>> entry : recordsByDimension.entrySet()) {
            int dimensionId = entry.getKey();
            dimensionIds[dimensionIndex++] = dimensionId;
            Collection<VeinRecord> values = entry.getValue().values();
            int size = values.size();
            int[] chunkXs = new int[size];
            int[] chunkZs = new int[size];
            int[] metas = new int[size];
            int[] veinHashes = new int[size];
            int[] iconStates = new int[size];
            int[] totalBlocks = new int[size];
            int[] oreStateKeys = new int[size * MAX_STORED_STATS];
            int[] oreCounts = new int[size * MAX_STORED_STATS];
            int recordIndex = 0;
            for (VeinRecord record : values) {
                chunkXs[recordIndex] = record.getChunkX();
                chunkZs[recordIndex] = record.getChunkZ();
                metas[recordIndex] = record.packMeta();
                veinHashes[recordIndex] = record.getVeinHash();
                iconStates[recordIndex] = record.packIconState();
                totalBlocks[recordIndex] = record.getTotalBlocks();
                writeStats(oreStateKeys, oreCounts, recordIndex, record.getStats());
                recordIndex++;
            }

            NBTTagCompound dimensionTag = new NBTTagCompound();
            dimensionTag.setTag(CHUNK_X_TAG, new NBTTagIntArray(chunkXs));
            dimensionTag.setTag(CHUNK_Z_TAG, new NBTTagIntArray(chunkZs));
            dimensionTag.setTag(META_TAG, new NBTTagIntArray(metas));
            dimensionTag.setTag(VEIN_HASH_TAG, new NBTTagIntArray(veinHashes));
            dimensionTag.setTag(ICON_STATE_TAG, new NBTTagIntArray(iconStates));
            dimensionTag.setTag(TOTAL_BLOCKS_TAG, new NBTTagIntArray(totalBlocks));
            dimensionTag.setTag(ORE_STATE_KEYS_TAG, new NBTTagIntArray(oreStateKeys));
            dimensionTag.setTag(ORE_COUNTS_TAG, new NBTTagIntArray(oreCounts));
            dimensionTag.setInteger(DIMENSION_ID_TAG, dimensionId);
            compound.setTag(String.valueOf(dimensionId), dimensionTag);
        }

        compound.setTag(DIMENSIONS_TAG, new NBTTagIntArray(dimensionIds));
        return compound;
    }

    /**
     * 获取 DimensionRecords。
     *
     * @param dimensionId 参数 dimensionId。
     * @return 处理结果。
    */
    private Map<Long, VeinRecord> getDimensionRecords(int dimensionId) {
        Map<Long, VeinRecord> dimensionRecords = recordsByDimension.get(dimensionId);
        if (dimensionRecords == null) {
            dimensionRecords = new HashMap<Long, VeinRecord>();
            recordsByDimension.put(dimensionId, dimensionRecords);
        }

        return dimensionRecords;
    }

    /**
     * 打包 ChunkKey。
     *
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @return 处理结果。
    */
    private static long packChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    /**
     * 执行 normalizeStats 逻辑。
     *
     * @param stats 参数 stats。
     * @return 处理结果。
    */
    private static List<StatRecord> normalizeStats(@Nullable List<StatRecord> stats) {
        List<StatRecord> normalized = new ArrayList<StatRecord>(MAX_STORED_STATS);
        if (stats == null) {
            return normalized;
        }
        int limit = Math.min(MAX_STORED_STATS, stats.size());
        for (int index = 0; index < limit; index++) {
            StatRecord stat = stats.get(index);
            if (stat != null && stat.getCount() > 0) {
                normalized.add(stat);
            }
        }

        return normalized;
    }

    /**
     * 执行 extractStats 逻辑。
     *
     * @param oreStateKeys 参数 oreStateKeys。
     * @param oreCounts 参数 oreCounts。
     * @param recordIndex 参数 recordIndex。
     * @return 处理结果。
    */
    private static List<StatRecord> extractStats(
            int[] oreStateKeys, int[] oreCounts, int recordIndex) {
        List<StatRecord> stats = new ArrayList<StatRecord>(MAX_STORED_STATS);
        int base = recordIndex * MAX_STORED_STATS;
        for (int offset = 0; offset < MAX_STORED_STATS; offset++) {
            int index = base + offset;
            if (index >= oreStateKeys.length || index >= oreCounts.length) {
                break;
            }

            int packedState = oreStateKeys[index];
            int count = oreCounts[index];
            if (packedState > 0 && count > 0) {
                stats.add(new StatRecord(packedState, count));
            }
        }

        return stats;
    }

    /**
     * 写入 Stats。
     *
     * @param oreStateKeys 参数 oreStateKeys。
     * @param oreCounts 参数 oreCounts。
     * @param recordIndex 参数 recordIndex。
     * @param stats 参数 stats。
    */
    private static void writeStats(
            int[] oreStateKeys, int[] oreCounts, int recordIndex, List<StatRecord> stats) {
        int base = recordIndex * MAX_STORED_STATS;
        int limit = Math.min(MAX_STORED_STATS, stats.size());
        for (int offset = 0; offset < limit; offset++) {
            StatRecord stat = stats.get(offset);
            oreStateKeys[base + offset] = stat.getPackedState();
            oreCounts[base + offset] = stat.getCount();
        }
    }

    /**
     * 单条矿脉的紧凑存档记录。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class VeinRecord {
        private final int chunkX;
        private final int chunkZ;
        private final int localX;
        private final int centerY;
        private final int localZ;
        private final int veinHash;
        private final int iconBlockId;
        private final int iconMeta;
        private final int totalBlocks;
        private final List<StatRecord> stats;

        /**
         * 构造 VeinRecord 实例。
         *
         * @param chunkX 参数 chunkX。
         * @param chunkZ 参数 chunkZ。
         * @param localX 参数 localX。
         * @param centerY 参数 centerY。
         * @param localZ 参数 localZ。
         * @param veinHash 参数 veinHash。
         * @param iconBlockId 参数 iconBlockId。
         * @param iconMeta 参数 iconMeta。
         * @param totalBlocks 参数 totalBlocks。
         * @param stats 参数 stats。
        */
        public VeinRecord(
                int chunkX,
                int chunkZ,
                int localX,
                int centerY,
                int localZ,
                int veinHash,
                int iconBlockId,
                int iconMeta,
                int totalBlocks,
                List<StatRecord> stats) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.localX = clamp(localX, 0, 15);
            this.centerY = clamp(centerY, 0, 255);
            this.localZ = clamp(localZ, 0, 15);
            this.veinHash = veinHash;
            this.iconBlockId = Math.max(0, iconBlockId);
            this.iconMeta = clamp(iconMeta, 0, 15);
            this.totalBlocks = Math.max(0, totalBlocks);
            this.stats = normalizeStats(stats);
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
            return (chunkX << 4) + localX;
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
            return (chunkZ << 4) + localZ;
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
         * 获取 Stats。
         *
         * @return 处理结果。
        */
        public List<StatRecord> getStats() {
            return stats;
        }

        /**
         * 打包 Meta。
         *
         * @return 处理结果。
        */
        public int packMeta() {
            return ((localX & 15) << 12) | ((localZ & 15) << 8) | (centerY & 255);
        }

        /**
         * 打包 IconState。
         *
         * @return 处理结果。
        */
        public int packIconState() {
            return (iconBlockId << 4) | (iconMeta & 15);
        }

        /**
         * 执行 fromPacked 逻辑。
         *
         * @param chunkX 参数 chunkX。
         * @param chunkZ 参数 chunkZ。
         * @param packedMeta 参数 packedMeta。
         * @param veinHash 参数 veinHash。
         * @param packedIconState 参数 packedIconState。
         * @param totalBlocks 参数 totalBlocks。
         * @param stats 参数 stats。
         * @return 处理结果。
        */
        public static VeinRecord fromPacked(
                int chunkX,
                int chunkZ,
                int packedMeta,
                int veinHash,
                int packedIconState,
                int totalBlocks,
                List<StatRecord> stats) {
            int localX = (packedMeta >> 12) & 15;
            int localZ = (packedMeta >> 8) & 15;
            int centerY = packedMeta & 255;
            int iconBlockId = packedIconState >>> 4;
            int iconMeta = packedIconState & 15;
            return new VeinRecord(
                    chunkX,
                    chunkZ,
                    localX,
                    centerY,
                    localZ,
                    veinHash,
                    iconBlockId,
                    iconMeta,
                    totalBlocks,
                    stats);
        }

        /**
         * 执行 equals 逻辑。
         *
         * @param other 参数 other。
         * @return 处理结果。
        */
        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof VeinRecord)) {
                return false;
            }
            VeinRecord record = (VeinRecord) other;
            return chunkX == record.chunkX
                    && chunkZ == record.chunkZ
                    && localX == record.localX
                    && centerY == record.centerY
                    && localZ == record.localZ
                    && veinHash == record.veinHash
                    && iconBlockId == record.iconBlockId
                    && iconMeta == record.iconMeta
                    && totalBlocks == record.totalBlocks
                    && stats.equals(record.stats);
        }

        /**
         * 判断是否包含 hCode。
         *
         * @return 处理结果。
        */
        @Override
        public int hashCode() {
            int result = chunkX;
            result = 31 * result + chunkZ;
            result = 31 * result + localX;
            result = 31 * result + centerY;
            result = 31 * result + localZ;
            result = 31 * result + veinHash;
            result = 31 * result + iconBlockId;
            result = 31 * result + iconMeta;
            result = 31 * result + totalBlocks;
            result = 31 * result + stats.hashCode();
            return result;
        }

        /**
         * 执行 clamp 逻辑。
         *
         * @param value 参数 value。
         * @param min 参数 min。
         * @param max 参数 max。
         * @return 处理结果。
        */
        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    /**
     * tooltip 使用的矿物统计记录。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class StatRecord {
        private final int packedState;
        private final int count;

        /**
         * 构造 StatRecord 实例。
         *
         * @param packedState 参数 packedState。
         * @param count 参数 count。
        */
        public StatRecord(int packedState, int count) {
            this.packedState = Math.max(0, packedState);
            this.count = Math.max(0, count);
        }

        /**
         * 获取 PackedState。
         *
         * @return 处理结果。
        */
        public int getPackedState() {
            return packedState;
        }

        /**
         * 获取 Count。
         *
         * @return 处理结果。
        */
        public int getCount() {
            return count;
        }

        /**
         * 执行 equals 逻辑。
         *
         * @param other 参数 other。
         * @return 处理结果。
        */
        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof StatRecord)) {
                return false;
            }
            StatRecord stat = (StatRecord) other;
            return packedState == stat.packedState && count == stat.count;
        }

        /**
         * 判断是否包含 hCode。
         *
         * @return 处理结果。
        */
        @Override
        public int hashCode() {
            int result = packedState;
            result = 31 * result + count;
            return result;
        }
    }
}
