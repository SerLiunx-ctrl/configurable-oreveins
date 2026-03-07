package com.serliunx.configurableoreveins.world;

import com.serliunx.configurableoreveins.config.BlockEntry;
import com.serliunx.configurableoreveins.config.VeinDefinition;
import com.serliunx.configurableoreveins.config.VeinShapeConfig;
import com.serliunx.configurableoreveins.util.BlockStateResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * 矿脉实例放置器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class VeinPlacer {

    private static final long SIZE_SALT = 0x53495A4553414C54L;
    private static final long PATH_SALT = 0x5041544853414C54L;
    private static final long DENSITY_SALT = 0x44454E5349545951L;
    private static final long BLOCK_SALT = 0x424C4F434B53454CL;
    private static final long NOISE_SALT = 0x4E4F49534553414CL;

    private final Map<VeinDefinition, PlacementTemplate> templateCache =
            Collections.synchronizedMap(new IdentityHashMap<VeinDefinition, PlacementTemplate>());

    /**
     * 创建运行时矿脉实例。
     *
     * @param vein 参数 vein。
     * @param seed 参数 seed。
     * @param origin 参数 origin。
     * @param anchorChunkX 参数 anchorChunkX。
     * @param anchorChunkZ 参数 anchorChunkZ。
     * @return 处理结果。
    */
    @Nullable
    public VeinInstance createInstance(
            VeinDefinition vein, long seed, BlockPos origin, int anchorChunkX, int anchorChunkZ) {
        PlacementTemplate template = getTemplate(vein);
        if (template == null) {
            return null;
        }
        VeinShapeConfig shape = vein.getShape();
        VeinShapeType shapeType = VeinShapeType.fromConfig(shape.getType());
        Random sizeRandom = new Random(mix(seed ^ SIZE_SALT));
        double sizeMultiplier =
                randomBetween(sizeRandom, shape.getSizeMultiplierMin(), shape.getSizeMultiplierMax());
        int radiusX = Math.max(1, (int) Math.round(shape.getRadiusX() * sizeMultiplier));
        int radiusY = Math.max(1, (int) Math.round(shape.getRadiusY() * sizeMultiplier));
        int radiusZ = Math.max(1, (int) Math.round(shape.getRadiusZ() * sizeMultiplier));
        int horizontalReach = Math.max(radiusX, radiusZ);
        if (shapeType == VeinShapeType.WORM) {
            horizontalReach += (int) Math.ceil(shape.getSteps() * shape.getStepLength());
        }

        return new VeinInstance(
                vein,
                template,
                seed,
                origin,
                anchorChunkX,
                anchorChunkZ,
                shapeType,
                radiusX,
                radiusY,
                radiusZ,
                horizontalReach,
                vein.getDensity(),
                shape.getIrregularity(),
                shape.getSteps(),
                shape.getStepLength());
    }

    /**
     * 估算矿脉最大水平影响范围。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    public int estimateMaxHorizontalReachBlocks(VeinDefinition vein) {
        VeinShapeConfig shape = vein.getShape();
        int radius =
                (int)
                        Math.ceil(
                                Math.max(shape.getRadiusX(), shape.getRadiusZ()) * shape.getSizeMultiplierMax());
        if (VeinShapeType.fromConfig(shape.getType()) == VeinShapeType.WORM) {
            radius += (int) Math.ceil(shape.getSteps() * shape.getStepLength());
        }
        return Math.max(1, radius);
    }

    /**
     * 判断矿脉是否会影响当前区块。
     *
     * @param instance 参数 instance。
     * @param chunkMinX 参数 chunkMinX。
     * @param chunkMaxX 参数 chunkMaxX。
     * @param chunkMinZ 参数 chunkMinZ。
     * @param chunkMaxZ 参数 chunkMaxZ。
     * @return 处理结果。
    */
    public boolean canAffectChunk(
            VeinInstance instance, int chunkMinX, int chunkMaxX, int chunkMinZ, int chunkMaxZ) {
        int minX = instance.origin.getX() - instance.horizontalReach;
        int maxX = instance.origin.getX() + instance.horizontalReach;
        int minZ = instance.origin.getZ() - instance.horizontalReach;
        int maxZ = instance.origin.getZ() + instance.horizontalReach;
        return maxX >= chunkMinX && minX <= chunkMaxX && maxZ >= chunkMinZ && minZ <= chunkMaxZ;
    }

    /**
     * 计算矿脉到区块中心的平方距离。
     *
     * @param instance 参数 instance。
     * @param chunkMinX 参数 chunkMinX。
     * @param chunkMinZ 参数 chunkMinZ。
     * @return 处理结果。
    */
    public double distanceToChunkCenterSq(VeinInstance instance, int chunkMinX, int chunkMinZ) {
        double chunkCenterX = chunkMinX + 7.5D;
        double chunkCenterZ = chunkMinZ + 7.5D;
        double dx = (instance.origin.getX() + 0.5D) - chunkCenterX;
        double dz = (instance.origin.getZ() + 0.5D) - chunkCenterZ;
        return (dx * dx) + (dz * dz);
    }

    /**
     * 在当前区块放置矿脉。
     *
     * @param world 参数 world。
     * @param chunk 参数 chunk。
     * @param instance 参数 instance。
     * @param chunkMinX 参数 chunkMinX。
     * @param chunkMaxX 参数 chunkMaxX。
     * @param chunkMinZ 参数 chunkMinZ。
     * @param chunkMaxZ 参数 chunkMaxZ。
     * @return 处理结果。
    */
    public PlacementResult place(
            World world,
            Chunk chunk,
            VeinInstance instance,
            int chunkMinX,
            int chunkMaxX,
            int chunkMinZ,
            int chunkMaxZ) {
        PlacementAccumulator accumulator = new PlacementAccumulator();
        if (instance.shapeType == VeinShapeType.WORM) {
            placeWorm(world, chunk, instance, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, accumulator);
        } else {
            placeBlob(
                    world,
                    chunk,
                    instance,
                    instance.origin,
                    instance.shapeType,
                    instance.radiusX,
                    instance.radiusY,
                    instance.radiusZ,
                    instance.irregularity,
                    mix(instance.seed ^ NOISE_SALT),
                    chunkMinX,
                    chunkMaxX,
                    chunkMinZ,
                    chunkMaxZ,
                    accumulator);
        }
        return accumulator.toResult();
    }

    /**
     * 执行 placeWorm 逻辑。
     *
     * @param world 参数 world。
     * @param chunk 参数 chunk。
     * @param instance 参数 instance。
     * @param chunkMinX 参数 chunkMinX。
     * @param chunkMaxX 参数 chunkMaxX。
     * @param chunkMinZ 参数 chunkMinZ。
     * @param chunkMaxZ 参数 chunkMaxZ。
     * @param accumulator 参数 accumulator。
    */
    private void placeWorm(
            World world,
            Chunk chunk,
            VeinInstance instance,
            int chunkMinX,
            int chunkMaxX,
            int chunkMinZ,
            int chunkMaxZ,
            PlacementAccumulator accumulator) {
        Random pathRandom = new Random(mix(instance.seed ^ PATH_SALT));
        double currentX = instance.origin.getX() + 0.5D;
        double currentY = instance.origin.getY() + 0.5D;
        double currentZ = instance.origin.getZ() + 0.5D;
        double directionX = pathRandom.nextDouble() - 0.5D;
        double directionY = (pathRandom.nextDouble() - 0.5D) * 0.4D;
        double directionZ = pathRandom.nextDouble() - 0.5D;

        for (int step = 0; step < instance.steps; step++) {
            double progress = instance.steps <= 1 ? 0.0D : (double) step / (double) (instance.steps - 1);
            double lobeScale = 0.65D + (Math.sin(progress * Math.PI) * 0.45D);
            double wobbleScale = 1.0D + ((pathRandom.nextDouble() - 0.5D) * instance.irregularity);
            int localRadiusX = Math.max(1, (int) Math.round(instance.radiusX * lobeScale * wobbleScale));
            int localRadiusY = Math.max(1, (int) Math.round(instance.radiusY * lobeScale));
            int localRadiusZ = Math.max(1, (int) Math.round(instance.radiusZ * lobeScale * wobbleScale));

            placeBlob(
                    world,
                    chunk,
                    instance,
                    new BlockPos(currentX, currentY, currentZ),
                    VeinShapeType.ELLIPSOID,
                    localRadiusX,
                    localRadiusY,
                    localRadiusZ,
                    Math.min(1.0D, instance.irregularity + 0.15D),
                    mix(instance.seed ^ NOISE_SALT ^ step),
                    chunkMinX,
                    chunkMaxX,
                    chunkMinZ,
                    chunkMaxZ,
                    accumulator);
            directionX += (pathRandom.nextDouble() - 0.5D) * (0.35D + instance.irregularity);
            directionY += (pathRandom.nextDouble() - 0.5D) * (0.18D + (instance.irregularity * 0.5D));
            directionZ += (pathRandom.nextDouble() - 0.5D) * (0.35D + instance.irregularity);
            double length =
                    Math.sqrt(
                            (directionX * directionX) + (directionY * directionY) + (directionZ * directionZ));
            if (length > 0.0D) {
                directionX /= length;
                directionY /= length;
                directionZ /= length;
            }

            currentX += directionX * instance.stepLength;
            currentY += directionY * instance.stepLength;
            currentZ += directionZ * instance.stepLength;
        }
    }

    /**
     * 执行 placeBlob 逻辑。
     *
     * @param world 参数 world。
     * @param chunk 参数 chunk。
     * @param instance 参数 instance。
     * @param center 参数 center。
     * @param shapeType 参数 shapeType。
     * @param radiusX 参数 radiusX。
     * @param radiusY 参数 radiusY。
     * @param radiusZ 参数 radiusZ。
     * @param irregularity 参数 irregularity。
     * @param noiseSeed 参数 noiseSeed。
     * @param chunkMinX 参数 chunkMinX。
     * @param chunkMaxX 参数 chunkMaxX。
     * @param chunkMinZ 参数 chunkMinZ。
     * @param chunkMaxZ 参数 chunkMaxZ。
     * @param accumulator 参数 accumulator。
     * @return 处理结果。
    */
    private int placeBlob(
            World world,
            Chunk chunk,
            VeinInstance instance,
            BlockPos center,
            VeinShapeType shapeType,
            int radiusX,
            int radiusY,
            int radiusZ,
            double irregularity,
            long noiseSeed,
            int chunkMinX,
            int chunkMaxX,
            int chunkMinZ,
            int chunkMaxZ,
            PlacementAccumulator accumulator) {
        int minOffsetX = Math.max(-radiusX, chunkMinX - center.getX());
        int maxOffsetX = Math.min(radiusX, chunkMaxX - center.getX());
        int minOffsetZ = Math.max(-radiusZ, chunkMinZ - center.getZ());
        int maxOffsetZ = Math.min(radiusZ, chunkMaxZ - center.getZ());
        int placedBlocks = 0;

        for (int x = minOffsetX; x <= maxOffsetX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = minOffsetZ; z <= maxOffsetZ; z++) {
                    int worldX = center.getX() + x;
                    int worldY = center.getY() + y;
                    int worldZ = center.getZ() + z;
                    if (worldY < 0 || worldY >= world.getActualHeight()) {
                        continue;
                    }
                    double normalizedDistance =
                            shapeType.normalizedDistance(x, y, z, radiusX, radiusY, radiusZ);
                    double boundaryModifier =
                            (sampleNoise(noiseSeed, worldX, worldY, worldZ) - 0.5D) * 2.0D * irregularity;
                    if (normalizedDistance > (1.0D + boundaryModifier)) {
                        continue;
                    }

                    placedBlocks +=
                            tryPlaceBlock(
                                    world, chunk, instance, new BlockPos(worldX, worldY, worldZ), accumulator);
                }
            }
        }

        return placedBlocks;
    }

    /**
     * 执行 tryPlaceBlock 逻辑。
     *
     * @param world 参数 world。
     * @param chunk 参数 chunk。
     * @param instance 参数 instance。
     * @param targetPos 参数 targetPos。
     * @param accumulator 参数 accumulator。
     * @return 处理结果。
    */
    private int tryPlaceBlock(
            World world,
            Chunk chunk,
            VeinInstance instance,
            BlockPos targetPos,
            PlacementAccumulator accumulator) {
        if (sampleNoise(
                        instance.seed ^ DENSITY_SALT, targetPos.getX(), targetPos.getY(), targetPos.getZ())
                > instance.density) {
            return 0;
        }
        Block currentBlock = chunk.getBlockState(targetPos).getBlock();
        if (!instance.template.replaceable.contains(currentBlock)) {
            return 0;
        }
        IBlockState resultState = chooseBlockState(instance, targetPos);
        if (resultState != null) {
            IBlockState previousState = chunk.setBlockState(targetPos, resultState);
            if (previousState != resultState) {
                accumulator.record(resultState);
                return 1;
            }
            return 0;
        }

        return 0;
    }

    /**
     * 执行 chooseBlockState 逻辑。
     *
     * @param instance 参数 instance。
     * @param pos 参数 pos。
     * @return 处理结果。
    */
    @Nullable
    private IBlockState chooseBlockState(VeinInstance instance, BlockPos pos) {
        if (instance.template.weightedStates.isEmpty()) {
            return null;
        }

        int totalWeight = instance.template.totalWeight;
        int selected =
                (int)
                        (positiveMod(
                                positionHash(instance.seed ^ BLOCK_SALT, pos.getX(), pos.getY(), pos.getZ()),
                                totalWeight));
        for (WeightedState weightedState : instance.template.weightedStates) {
            if (selected < weightedState.cumulativeWeight) {
                return weightedState.state;
            }
        }
        return instance.template.weightedStates.get(instance.template.weightedStates.size() - 1).state;
    }

    /**
     * 获取 Template。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    @Nullable
    private PlacementTemplate getTemplate(VeinDefinition vein) {
        if (templateCache.containsKey(vein)) {
            return templateCache.get(vein);
        }

        Set<Block> replaceable =
                new HashSet<Block>(BlockStateResolver.resolveBlocks(vein.getReplaceableBlocks()));
        if (replaceable.isEmpty()) {
            templateCache.put(vein, null);
            return null;
        }

        List<WeightedState> weightedStates = new ArrayList<WeightedState>();
        int totalWeight = 0;
        for (BlockEntry entry : vein.getBlocks()) {
            if (entry == null || entry.getWeight() <= 0) {
                continue;
            }
            IBlockState state = BlockStateResolver.resolveState(entry.getBlock(), entry.getMeta());
            if (state == null) {
                continue;
            }
            totalWeight += entry.getWeight();
            weightedStates.add(new WeightedState(state, totalWeight));
        }

        if (weightedStates.isEmpty()) {
            templateCache.put(vein, null);
            return null;
        }

        PlacementTemplate template = new PlacementTemplate(replaceable, weightedStates, totalWeight);
        templateCache.put(vein, template);
        return template;
    }

    /**
     * 执行 randomBetween 逻辑。
     *
     * @param random 参数 random。
     * @param min 参数 min。
     * @param max 参数 max。
     * @return 处理结果。
    */
    private double randomBetween(Random random, double min, double max) {
        if (max <= min) {
            return min;
        }
        return min + (random.nextDouble() * (max - min));
    }

    /**
     * 执行 sampleNoise 逻辑。
     *
     * @param seed 参数 seed。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @return 处理结果。
    */
    private double sampleNoise(long seed, int x, int y, int z) {
        long positive = positionHash(seed, x, y, z);
        return positive / (double) Long.MAX_VALUE;
    }

    /**
     * 执行 positionHash 逻辑。
     *
     * @param seed 参数 seed。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @return 处理结果。
    */
    private long positionHash(long seed, int x, int y, int z) {
        long hash = seed;
        hash ^= (long) x * 341873128712L;
        hash ^= (long) y * 132897987541L;
        hash ^= (long) z * 42317861L;
        return positiveHash(hash);
    }

    /**
     * 执行 positiveHash 逻辑。
     *
     * @param value 参数 value。
     * @return 处理结果。
    */
    private long positiveHash(long value) {
        return mix(value) & Long.MAX_VALUE;
    }

    /**
     * 执行 positiveMod 逻辑。
     *
     * @param value 参数 value。
     * @param mod 参数 mod。
     * @return 处理结果。
    */
    private long positiveMod(long value, int mod) {
        return mod <= 0 ? 0L : (value % mod);
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

    /**
     * 矿脉放置模板缓存对象。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private static class PlacementTemplate {
        private final Set<Block> replaceable;
        private final List<WeightedState> weightedStates;
        private final int totalWeight;

        /**
         * 构造 PlacementTemplate 实例。
         *
         * @param replaceable 参数 replaceable。
         * @param weightedStates 参数 weightedStates。
         * @param totalWeight 参数 totalWeight。
        */
        private PlacementTemplate(
                Set<Block> replaceable, List<WeightedState> weightedStates, int totalWeight) {
            this.replaceable = replaceable;
            this.weightedStates = weightedStates;
            this.totalWeight = totalWeight;
        }
    }

    /**
     * 矿脉放置统计结果。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class PlacementResult {
        private final int placedBlocks;
        private final List<BlockStat> blockStats;

        /**
         * 构造 PlacementResult 实例。
         *
         * @param placedBlocks 参数 placedBlocks。
         * @param blockStats 参数 blockStats。
        */
        private PlacementResult(int placedBlocks, List<BlockStat> blockStats) {
            this.placedBlocks = placedBlocks;
            this.blockStats = blockStats;
        }

        /**
         * 获取 PlacedBlocks。
         *
         * @return 处理结果。
        */
        public int getPlacedBlocks() {
            return placedBlocks;
        }

        /**
         * 获取 BlockStats。
         *
         * @return 处理结果。
        */
        public List<BlockStat> getBlockStats() {
            return blockStats;
        }
    }

    /**
     * 单种矿物方块的放置统计。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class BlockStat {
        private final int packedState;
        private final int count;

        /**
         * 构造 BlockStat 实例。
         *
         * @param packedState 参数 packedState。
         * @param count 参数 count。
        */
        private BlockStat(int packedState, int count) {
            this.packedState = packedState;
            this.count = count;
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
    }

    /**
     * 矿脉放置过程统计累加器。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private static class PlacementAccumulator {
        private final Map<Integer, Integer> blockCounts = new HashMap<Integer, Integer>();
        private int placedBlocks;

        /**
         * 执行 record 逻辑。
         *
         * @param state 参数 state。
        */
        private void record(IBlockState state) {
            int packedState = packState(state);
            placedBlocks++;
            Integer existing = blockCounts.get(packedState);
            blockCounts.put(packedState, existing == null ? 1 : existing + 1);
        }

        /**
         * 执行 toResult 逻辑。
         *
         * @return 处理结果。
        */
        private PlacementResult toResult() {
            List<BlockStat> stats = new ArrayList<BlockStat>(blockCounts.size());
            for (Map.Entry<Integer, Integer> entry : blockCounts.entrySet()) {
                stats.add(new BlockStat(entry.getKey(), entry.getValue()));
            }

            Collections.sort(
                    stats,
                    new Comparator<BlockStat>() {
                        @Override
                        public int compare(BlockStat left, BlockStat right) {
                            int byCount = Integer.compare(right.count, left.count);
                            if (byCount != 0) {
                                return byCount;
                            }
                            return Integer.compare(left.packedState, right.packedState);
                        }
                    });
            return new PlacementResult(placedBlocks, stats);
        }

        /**
         * 打包 State。
         *
         * @param state 参数 state。
         * @return 处理结果。
        */
        private int packState(IBlockState state) {
            Block block = state.getBlock();
            int meta;
            try {
                meta = block.getMetaFromState(state);
            } catch (RuntimeException exception) {
                meta = 0;
            }

            return (Block.getIdFromBlock(block) << 4) | (meta & 15);
        }
    }

    /**
     * 带累计权重的输出状态条目。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private static class WeightedState {
        private final IBlockState state;
        private final int cumulativeWeight;

        /**
         * 构造 WeightedState 实例。
         *
         * @param state 参数 state。
         * @param cumulativeWeight 参数 cumulativeWeight。
        */
        private WeightedState(IBlockState state, int cumulativeWeight) {
            this.state = state;
            this.cumulativeWeight = cumulativeWeight;
        }
    }

    /**
     * 运行时矿脉实例。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class VeinInstance {
        private final VeinDefinition definition;
        private final PlacementTemplate template;
        private final long seed;
        private final BlockPos origin;
        private final int anchorChunkX;
        private final int anchorChunkZ;
        private final VeinShapeType shapeType;
        private final int radiusX;
        private final int radiusY;
        private final int radiusZ;
        private final int horizontalReach;
        private final double density;
        private final double irregularity;
        private final int steps;
        private final double stepLength;

        /**
         * 构造 VeinInstance 实例。
         *
         * @param definition 参数 definition。
         * @param template 参数 template。
         * @param seed 参数 seed。
         * @param origin 参数 origin。
         * @param anchorChunkX 参数 anchorChunkX。
         * @param anchorChunkZ 参数 anchorChunkZ。
         * @param shapeType 参数 shapeType。
         * @param radiusX 参数 radiusX。
         * @param radiusY 参数 radiusY。
         * @param radiusZ 参数 radiusZ。
         * @param horizontalReach 参数 horizontalReach。
         * @param density 参数 density。
         * @param irregularity 参数 irregularity。
         * @param steps 参数 steps。
         * @param stepLength 参数 stepLength。
        */
        private VeinInstance(
                VeinDefinition definition,
                PlacementTemplate template,
                long seed,
                BlockPos origin,
                int anchorChunkX,
                int anchorChunkZ,
                VeinShapeType shapeType,
                int radiusX,
                int radiusY,
                int radiusZ,
                int horizontalReach,
                double density,
                double irregularity,
                int steps,
                double stepLength) {
            this.definition = definition;
            this.template = template;
            this.seed = seed;
            this.origin = origin;
            this.anchorChunkX = anchorChunkX;
            this.anchorChunkZ = anchorChunkZ;
            this.shapeType = shapeType;
            this.radiusX = radiusX;
            this.radiusY = radiusY;
            this.radiusZ = radiusZ;
            this.horizontalReach = horizontalReach;
            this.density = density;
            this.irregularity = irregularity;
            this.steps = steps;
            this.stepLength = stepLength;
        }

        /**
         * 获取 Seed。
         *
         * @return 处理结果。
        */
        public long getSeed() {
            return seed;
        }

        /**
         * 获取 Origin。
         *
         * @return 处理结果。
        */
        public BlockPos getOrigin() {
            return origin;
        }

        /**
         * 获取 Definition。
         *
         * @return 处理结果。
        */
        public VeinDefinition getDefinition() {
            return definition;
        }

        /**
         * 获取 AnchorChunkX。
         *
         * @return 处理结果。
        */
        public int getAnchorChunkX() {
            return anchorChunkX;
        }

        /**
         * 获取 AnchorChunkZ。
         *
         * @return 处理结果。
        */
        public int getAnchorChunkZ() {
            return anchorChunkZ;
        }
    }
}
