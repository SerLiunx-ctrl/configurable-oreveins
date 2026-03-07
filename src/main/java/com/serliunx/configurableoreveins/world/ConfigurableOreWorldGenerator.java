package com.serliunx.configurableoreveins.world;

import com.serliunx.configurableoreveins.config.ModConfigManager;
import com.serliunx.configurableoreveins.config.VeinDefinition;
import com.serliunx.configurableoreveins.data.VeinWorldData;
import com.serliunx.configurableoreveins.data.VeinWorldData.StatRecord;
import com.serliunx.configurableoreveins.util.BlockStateResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * 矿脉世界生成器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class ConfigurableOreWorldGenerator implements IWorldGenerator {
    private static final int MIN_VEIN_SPACING_CHUNKS = 2;
    private static final long CHUNK_SELECTION_SALT = 0x4F56474C4F4E474CL;
    private static final long VEIN_SELECTION_SALT = 0x5645494E53454C31L;
    private static final long VEIN_CHANCE_SALT = 0x5645494E43484E43L;
    private static final long POSITION_SALT = 0x504F534954494F4EL;
    private static final long INSTANCE_SALT = 0x494E5354414E4345L;

    private final ModConfigManager configManager;
    private final VeinPlacer veinPlacer = new VeinPlacer();

    /**
     * 构造 ConfigurableOreWorldGenerator 实例。
     *
     * @param configManager 参数 configManager。
    */
    public ConfigurableOreWorldGenerator(ModConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 执行矿脉世界生成逻辑。
     *
     * @param random 参数 random。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param world 参数 world。
     * @param chunkGenerator 参数 chunkGenerator。
     * @param chunkProvider 参数 chunkProvider。
    */
    @Override
    public void generate(
            Random random,
            int chunkX,
            int chunkZ,
            World world,
            IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        configManager.reloadIfNeeded();
        int dimensionId = world.provider.getDimension();
        long worldSeed = world.getSeed();
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        int chunkMinX = chunkOrigin.getX();
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkOrigin.getZ();
        int chunkMaxZ = chunkMinZ + 15;
        Chunk currentChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int searchRadiusChunks = getSearchRadiusChunks();

        VeinPlacer.VeinInstance bestInstance = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (int anchorChunkX = chunkX - searchRadiusChunks;
                anchorChunkX <= chunkX + searchRadiusChunks;
                anchorChunkX++) {
            for (int anchorChunkZ = chunkZ - searchRadiusChunks;
                    anchorChunkZ <= chunkZ + searchRadiusChunks;
                    anchorChunkZ++) {
                VeinPlacer.VeinInstance instance =
                        createVeinInstance(world, worldSeed, dimensionId, anchorChunkX, anchorChunkZ);
                if (instance == null
                        || !veinPlacer.canAffectChunk(instance, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ)) {
                    continue;
                }
                double distanceSq = veinPlacer.distanceToChunkCenterSq(instance, chunkMinX, chunkMinZ);
                if (bestInstance == null
                        || distanceSq < bestDistanceSq
                        || (distanceSq == bestDistanceSq && instance.getSeed() < bestInstance.getSeed())) {
                    bestInstance = instance;
                    bestDistanceSq = distanceSq;
                }
            }
        }

        if (bestInstance != null) {
            VeinPlacer.PlacementResult placementResult =
                    veinPlacer.place(
                            world, currentChunk, bestInstance, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);
            if (placementResult.getPlacedBlocks() > 0) {
                BlockPos origin = bestInstance.getOrigin();
                int[] iconState = resolveLocatorIcon(bestInstance.getDefinition());
                VeinWorldData.get(world)
                        .recordGeneratedVein(
                                dimensionId,
                                bestInstance.getAnchorChunkX(),
                                bestInstance.getAnchorChunkZ(),
                                origin.getX(),
                                origin.getY(),
                                origin.getZ(),
                                bestInstance.getDefinition().getName().hashCode(),
                                iconState[0],
                                iconState[1],
                                placementResult.getPlacedBlocks(),
                                toStatRecords(placementResult.getBlockStats()));
            }
        }
    }

    /**
     * 执行 toStatRecords 逻辑。
     *
     * @param stats 参数 stats。
     * @return 处理结果。
    */
    private List<StatRecord> toStatRecords(List<VeinPlacer.BlockStat> stats) {
        List<StatRecord> result = new ArrayList<StatRecord>(stats.size());
        for (VeinPlacer.BlockStat stat : stats) {
            if (stat != null && stat.getCount() > 0) {
                result.add(new StatRecord(stat.getPackedState(), stat.getCount()));
            }
        }
        return result;
    }

    /**
     * 解析 LocatorIcon。
     *
     * @param definition 参数 definition。
     * @return 处理结果。
    */
    private int[] resolveLocatorIcon(VeinDefinition definition) {
        String blockName = definition.getLocatorIconBlock();
        int meta = definition.getLocatorIconMeta();
        IBlockState state = BlockStateResolver.resolveState(blockName, meta);
        if (state == null) {
            return new int[] {0, 0};
        }

        return new int[] {Block.getIdFromBlock(state.getBlock()), meta};
    }

    /**
     * 获取 SearchRadiusChunks。
     *
     * @return 处理结果。
    */
    private int getSearchRadiusChunks() {
        int maxReachBlocks = 0;
        for (VeinDefinition vein : configManager.getVeins()) {
            if (!vein.isEnabled()) {
                continue;
            }
            maxReachBlocks = Math.max(maxReachBlocks, veinPlacer.estimateMaxHorizontalReachBlocks(vein));
        }
        return Math.max(0, (int) Math.ceil(maxReachBlocks / 16.0D));
    }

    /**
     * 创建 VeinInstance。
     *
     * @param world 参数 world。
     * @param worldSeed 参数 worldSeed。
     * @param dimensionId 参数 dimensionId。
     * @param anchorChunkX 参数 anchorChunkX。
     * @param anchorChunkZ 参数 anchorChunkZ。
     * @return 处理结果。
    */
    private VeinPlacer.VeinInstance createVeinInstance(
            World world, long worldSeed, int dimensionId, int anchorChunkX, int anchorChunkZ) {
        if (!isVeinChunk(worldSeed, dimensionId, anchorChunkX, anchorChunkZ)) {
            return null;
        }

        int chunkMinX = anchorChunkX * 16;
        int chunkMinZ = anchorChunkZ * 16;
        Random positionRandom =
                createChunkRandom(worldSeed, dimensionId, anchorChunkX, anchorChunkZ, POSITION_SALT);
        int x = chunkMinX + positionRandom.nextInt(16);
        int z = chunkMinZ + positionRandom.nextInt(16);
        BlockPos biomePos = new BlockPos(x, 64, z);
        ResourceLocation biomeName = world.getBiomeProvider().getBiome(biomePos).getRegistryName();
        String biomeId = biomeName == null ? null : biomeName.toString();
        VeinDefinition selectedVein =
                selectVein(worldSeed, dimensionId, anchorChunkX, anchorChunkZ, biomeId);
        if (selectedVein == null) {
            return null;
        }
        int minY = Math.max(0, selectedVein.getMinY());
        int maxY = Math.min(world.getActualHeight() - 1, selectedVein.getMaxY());
        if (maxY < minY) {
            return null;
        }
        int y = minY + positionRandom.nextInt((maxY - minY) + 1);
        BlockPos origin = new BlockPos(x, y, z);
        long seed =
                mix(
                        worldSeed
                                ^ (((long) dimensionId) * 0x9E3779B97F4A7C15L)
                                ^ (((long) anchorChunkX) * 341873128712L)
                                ^ (((long) anchorChunkZ) * 132897987541L)
                                ^ INSTANCE_SALT);
        return veinPlacer.createInstance(selectedVein, seed, origin, anchorChunkX, anchorChunkZ);
    }

    /**
     * 选择 Vein。
     *
     * @param worldSeed 参数 worldSeed。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param biomeId 参数 biomeId。
     * @return 处理结果。
    */
    private VeinDefinition selectVein(
            long worldSeed, int dimensionId, int chunkX, int chunkZ, String biomeId) {
        List<VeinDefinition> eligible = new ArrayList<VeinDefinition>();
        for (VeinDefinition vein : configManager.getVeins()) {
            if (!vein.isEnabled() || !vein.matchesDimension(dimensionId) || !vein.matchesBiome(biomeId)) {
                continue;
            }

            if (!passesChunkChance(worldSeed, dimensionId, chunkX, chunkZ, vein)) {
                continue;
            }
            eligible.add(vein);
        }

        if (eligible.isEmpty()) {
            return null;
        }
        Random selectionRandom =
                createChunkRandom(worldSeed, dimensionId, chunkX, chunkZ, VEIN_SELECTION_SALT);
        return eligible.get(selectionRandom.nextInt(eligible.size()));
    }

    /**
     * 执行 passesChunkChance 逻辑。
     *
     * @param worldSeed 参数 worldSeed。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    private boolean passesChunkChance(
            long worldSeed, int dimensionId, int chunkX, int chunkZ, VeinDefinition vein) {
        double chance = vein.getChunkChance();
        if (chance <= 0.0D) {
            return false;
        }

        if (chance >= 1.0D) {
            return true;
        }
        long veinHash = vein.getName().hashCode();
        long mixed =
                mix(
                        worldSeed
                                ^ (((long) dimensionId) * 0x9E3779B97F4A7C15L)
                                ^ (((long) chunkX) * 341873128712L)
                                ^ (((long) chunkZ) * 132897987541L)
                                ^ (veinHash * 0xBF58476D1CE4E5B9L)
                                ^ VEIN_CHANCE_SALT);
        double roll = (mixed & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
        return roll < chance;
    }

    /**
     * 判断 VeinChunk。
     *
     * @param worldSeed 参数 worldSeed。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @return 处理结果。
    */
    private boolean isVeinChunk(long worldSeed, int dimensionId, int chunkX, int chunkZ) {
        long centerScore = chunkScore(worldSeed, dimensionId, chunkX, chunkZ);
        for (int offsetX = -MIN_VEIN_SPACING_CHUNKS; offsetX <= MIN_VEIN_SPACING_CHUNKS; offsetX++) {
            for (int offsetZ = -MIN_VEIN_SPACING_CHUNKS; offsetZ <= MIN_VEIN_SPACING_CHUNKS; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) {
                    continue;
                }

                int otherChunkX = chunkX + offsetX;
                int otherChunkZ = chunkZ + offsetZ;
                long otherScore = chunkScore(worldSeed, dimensionId, otherChunkX, otherChunkZ);
                if (otherScore > centerScore) {
                    return false;
                }

                if (otherScore == centerScore
                        && compareChunkPosition(otherChunkX, otherChunkZ, chunkX, chunkZ) < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 比较 ChunkPosition。
     *
     * @param leftX 参数 leftX。
     * @param leftZ 参数 leftZ。
     * @param rightX 参数 rightX。
     * @param rightZ 参数 rightZ。
     * @return 处理结果。
    */
    private int compareChunkPosition(int leftX, int leftZ, int rightX, int rightZ) {
        if (leftX != rightX) {
            return Integer.compare(leftX, rightX);
        }
        return Integer.compare(leftZ, rightZ);
    }

    /**
     * 执行 chunkScore 逻辑。
     *
     * @param worldSeed 参数 worldSeed。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @return 处理结果。
    */
    private long chunkScore(long worldSeed, int dimensionId, int chunkX, int chunkZ) {
        long mixed =
                mix(
                        worldSeed
                                ^ (((long) dimensionId) * 0x9E3779B97F4A7C15L)
                                ^ (((long) chunkX) * 341873128712L)
                                ^ (((long) chunkZ) * 132897987541L)
                                ^ CHUNK_SELECTION_SALT);
        return mixed & Long.MAX_VALUE;
    }

    /**
     * 创建 ChunkRandom。
     *
     * @param worldSeed 参数 worldSeed。
     * @param dimensionId 参数 dimensionId。
     * @param chunkX 参数 chunkX。
     * @param chunkZ 参数 chunkZ。
     * @param salt 参数 salt。
     * @return 处理结果。
    */
    private Random createChunkRandom(
            long worldSeed, int dimensionId, int chunkX, int chunkZ, long salt) {
        return new Random(
                mix(
                        worldSeed
                                ^ (((long) dimensionId) * 0x9E3779B97F4A7C15L)
                                ^ (((long) chunkX) * 341873128712L)
                                ^ (((long) chunkZ) * 132897987541L)
                                ^ salt));
    }

    /**
     * 执行 mix 逻辑。
     *
     * @param value 参数 value。
     * @return 处理结果。
    */
    private long mix(long value) {
        long mixed = value;
        mixed = (mixed ^ (mixed >>> 33)) * 0xff51afd7ed558ccdL;
        mixed = (mixed ^ (mixed >>> 33)) * 0xc4ceb9fe1a85ec53L;
        mixed = mixed ^ (mixed >>> 33);
        return mixed;
    }
}
