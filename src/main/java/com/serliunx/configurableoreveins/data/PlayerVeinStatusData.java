package com.serliunx.configurableoreveins.data;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/**
 * 玩家独立的矿脉状态存档。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class PlayerVeinStatusData extends WorldSavedData {
    private static final String DATA_NAME = ConfigurableOreVeinsMod.MOD_ID + "_player_vein_status";
    private static final String PLAYERS_TAG = "players";
    private static final String DIMENSIONS_TAG = "dimensions";
    private static final String CHUNK_X_TAG = "chunkX";
    private static final String CHUNK_Z_TAG = "chunkZ";

    private final Map<String, Map<Integer, Set<Long>>> minedByPlayer = new HashMap<String, Map<Integer, Set<Long>>>();

    /** 构造 PlayerVeinStatusData 实例。 */
    public PlayerVeinStatusData() {
        super(DATA_NAME);
    }

    /**
     * 构造 PlayerVeinStatusData 实例。
     *
     * @param name 参数 name。
    */
    public PlayerVeinStatusData(String name) {
        super(name);
    }

    /**
     * 获取玩家矿脉状态存档对象。
     *
     * @param world 参数 world。
     * @return 处理结果。
    */
    public static PlayerVeinStatusData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        PlayerVeinStatusData data =
                (PlayerVeinStatusData) storage.getOrLoadData(PlayerVeinStatusData.class, DATA_NAME);
        if (data == null) {
            data = new PlayerVeinStatusData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    /**
     * 判断玩家是否已将矿脉标记为已挖掘。
     *
     * @param playerId 参数 playerId。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @return 处理结果。
    */
    public boolean isMined(UUID playerId, int dimensionId, int chunkX, int chunkZ) {
        Map<Integer, Set<Long>> dimensions = minedByPlayer.get(playerId.toString());
        if (dimensions == null) {
            return false;
        }
        Set<Long> chunkKeys = dimensions.get(dimensionId);
        return chunkKeys != null && chunkKeys.contains(packChunkKey(chunkX, chunkZ));
    }

    /**
     * 设置玩家的矿脉挖掘标记。
     *
     * @param playerId 参数 playerId。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param mined 参数 mined。
     * @return 处理结果。
    */
    public boolean setMined(UUID playerId, int dimensionId, int chunkX, int chunkZ, boolean mined) {
        String playerKey = playerId.toString();
        Map<Integer, Set<Long>> dimensions = minedByPlayer.get(playerKey);
        if (dimensions == null) {
            if (!mined) {
                return false;
            }
            dimensions = new HashMap<Integer, Set<Long>>();
            minedByPlayer.put(playerKey, dimensions);
        }

        Set<Long> chunkKeys = dimensions.get(dimensionId);
        if (chunkKeys == null) {
            if (!mined) {
                return false;
            }
            chunkKeys = new HashSet<Long>();
            dimensions.put(dimensionId, chunkKeys);
        }

        long chunkKey = packChunkKey(chunkX, chunkZ);
        boolean changed = mined ? chunkKeys.add(chunkKey) : chunkKeys.remove(chunkKey);
        if (!changed) {
            return false;
        }

        if (chunkKeys.isEmpty()) {
            dimensions.remove(dimensionId);
        }
        if (dimensions.isEmpty()) {
            minedByPlayer.remove(playerKey);
        }
        markDirty();
        return true;
    }

    /**
     * 从 NBT 读取玩家矿脉状态。
     *
     * @param nbt 参数 nbt。
    */
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        minedByPlayer.clear();
        if (!nbt.hasKey(PLAYERS_TAG)) {
            return;
        }
        NBTTagCompound playersTag = nbt.getCompoundTag(PLAYERS_TAG);
        for (String playerKey : playersTag.getKeySet()) {
            NBTTagCompound playerTag = playersTag.getCompoundTag(playerKey);
            int[] dimensionIds = playerTag.getIntArray(DIMENSIONS_TAG);
            Map<Integer, Set<Long>> dimensions = new HashMap<Integer, Set<Long>>();
            for (int dimensionId : dimensionIds) {
                NBTTagCompound dimensionTag = playerTag.getCompoundTag(String.valueOf(dimensionId));
                int[] chunkXs = dimensionTag.getIntArray(CHUNK_X_TAG);
                int[] chunkZs = dimensionTag.getIntArray(CHUNK_Z_TAG);
                int count = Math.min(chunkXs.length, chunkZs.length);
                Set<Long> chunkKeys = new HashSet<Long>(count);
                for (int index = 0; index < count; index++) {
                    chunkKeys.add(packChunkKey(chunkXs[index], chunkZs[index]));
                }
                if (!chunkKeys.isEmpty()) {
                    dimensions.put(dimensionId, chunkKeys);
                }
            }
            if (!dimensions.isEmpty()) {
                minedByPlayer.put(playerKey, dimensions);
            }
        }
    }

    /**
     * 将玩家矿脉状态写入 NBT。
     *
     * @param compound 参数 compound。
     * @return 处理结果。
    */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound playersTag = new NBTTagCompound();
        for (Map.Entry<String, Map<Integer, Set<Long>>> playerEntry : minedByPlayer.entrySet()) {
            NBTTagCompound playerTag = new NBTTagCompound();
            int[] dimensionIds = new int[playerEntry.getValue().size()];
            int dimensionIndex = 0;
            for (Map.Entry<Integer, Set<Long>> dimensionEntry : playerEntry.getValue().entrySet()) {
                int dimensionId = dimensionEntry.getKey().intValue();
                dimensionIds[dimensionIndex++] = dimensionId;
                int size = dimensionEntry.getValue().size();
                int[] chunkXs = new int[size];
                int[] chunkZs = new int[size];
                int index = 0;
                for (Long chunkKey : dimensionEntry.getValue()) {
                    chunkXs[index] = unpackChunkX(chunkKey.longValue());
                    chunkZs[index] = unpackChunkZ(chunkKey.longValue());
                    index++;
                }
                NBTTagCompound dimensionTag = new NBTTagCompound();
                dimensionTag.setTag(CHUNK_X_TAG, new NBTTagIntArray(chunkXs));
                dimensionTag.setTag(CHUNK_Z_TAG, new NBTTagIntArray(chunkZs));
                playerTag.setTag(String.valueOf(dimensionId), dimensionTag);
            }
            playerTag.setTag(DIMENSIONS_TAG, new NBTTagIntArray(dimensionIds));
            playersTag.setTag(playerEntry.getKey(), playerTag);
        }
        compound.setTag(PLAYERS_TAG, playersTag);
        return compound;
    }

    /**
     * 打包区块键。
     *
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @return 处理结果。
    */
    private static long packChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    /**
     * 解包区块 X 坐标。
     *
     * @param chunkKey 参数 chunkKey。
     * @return 处理结果。
    */
    private static int unpackChunkX(long chunkKey) {
        return (int) (chunkKey >> 32);
    }

    /**
     * 解包区块 Z 坐标。
     *
     * @param chunkKey 参数 chunkKey。
     * @return 处理结果。
    */
    private static int unpackChunkZ(long chunkKey) {
        return (int) chunkKey;
    }
}
