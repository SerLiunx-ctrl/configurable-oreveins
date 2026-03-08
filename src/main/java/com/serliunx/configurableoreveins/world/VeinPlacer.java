package com.serliunx.configurableoreveins.world;

import com.serliunx.configurableoreveins.config.vein.BlockEntry;
import com.serliunx.configurableoreveins.config.vein.VeinDefinition;
import com.serliunx.configurableoreveins.config.vein.VeinShapeConfig;
import com.serliunx.configurableoreveins.util.BlockStateResolver;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 矿脉实例放置器.
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

    private final Map<VeinDefinition, PlacementTemplate> templateCache = Collections.synchronizedMap(new IdentityHashMap<>());

    /**
     * 创建运行时矿脉实例.
     *
     * @param vein 矿脉定义信息.
     * @param seed 种子
     * @param origin 原始立体坐标信息
     * @param anchorChunkX 锚定区块X轴
     * @param anchorChunkZ 锚定区块Y轴
     * @return 矿脉实例
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
     * 估算矿脉最大水平影响范围.
     *
     * @param vein 矿脉定义
     * @return 水平范围
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
     * 判断矿脉是否会影响当前区块.
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
            double progress = instance.steps == 1 ? 0.0D : (double) step / (double) (instance.steps - 1);
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
     * 执行 placeBlob 逻辑.
     *
     * @return 已放置的方块数量.
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
     * 选择方块
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
     * 获取放置模板
     */
    @Nullable
    private PlacementTemplate getTemplate(VeinDefinition vein) {
        if (templateCache.containsKey(vein)) {
            return templateCache.get(vein);
        }

        Set<Block> replaceable =
                new HashSet<>(BlockStateResolver.resolveBlocks(vein.getReplaceableBlocks()));
        if (replaceable.isEmpty()) {
            templateCache.put(vein, null);
            return null;
        }

        List<WeightedState> weightedStates = new ArrayList<>();
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
     * 在两个数之间随机
     */
    private double randomBetween(Random random, double min, double max) {
        if (max <= min) {
            return min;
        }
        return min + (random.nextDouble() * (max - min));
    }

    /**
     * 简易噪声算法
     */
    private double sampleNoise(long seed, int x, int y, int z) {
        long positive = positionHash(seed, x, y, z);
        return positive / (double) Long.MAX_VALUE;
    }

    private long positionHash(long seed, int x, int y, int z) {
        long hash = seed;
        hash ^= (long) x * 341873128712L;
        hash ^= (long) y * 132897987541L;
        hash ^= (long) z * 42317861L;
        return positiveHash(hash);
    }

    private long positiveHash(long value) {
        return mix(value) & Long.MAX_VALUE;
    }

    private long positiveMod(long value, int mod) {
        return mod <= 0 ? 0L : (value % mod);
    }

    private long mix(long value) {
        long mixed = value;
        mixed = (mixed ^ (mixed >>> 33)) * 0xff51afd7ed558ccdL;
        mixed = (mixed ^ (mixed >>> 33)) * 0xc4ceb9fe1a85ec53L;
        mixed = mixed ^ (mixed >>> 33);
        return mixed;
    }

    /**
     * 矿脉放置模板缓存对象.
     */
    private static class PlacementTemplate {

        private final Set<Block> replaceable;
        private final List<WeightedState> weightedStates;
        private final int totalWeight;

        private PlacementTemplate(
                Set<Block> replaceable, List<WeightedState> weightedStates, int totalWeight) {
            this.replaceable = replaceable;
            this.weightedStates = weightedStates;
            this.totalWeight = totalWeight;
        }
    }

    /**
     * 矿脉放置统计结果.
     */
    public static class PlacementResult {

        private final int placedBlocks;
        private final List<BlockStat> blockStats;

        private PlacementResult(int placedBlocks, List<BlockStat> blockStats) {
            this.placedBlocks = placedBlocks;
            this.blockStats = blockStats;
        }

        public int getPlacedBlocks() {
            return placedBlocks;
        }

        public List<BlockStat> getBlockStats() {
            return blockStats;
        }
    }

    /**
     * 单种矿物方块的放置统计。
     */
    public static class BlockStat {

        private final int packedState;
        private final int count;

        private BlockStat(int packedState, int count) {
            this.packedState = packedState;
            this.count = count;
        }

        public int getPackedState() {
            return packedState;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * 矿脉放置过程统计累加器。
     */
    private static class PlacementAccumulator {

        private final Map<Integer, Integer> blockCounts = new HashMap<>();
        private int placedBlocks;

        private void record(IBlockState state) {
            int packedState = packState(state);
            placedBlocks++;
            blockCounts.compute(packedState, (k, existing) -> existing == null ? 1 : existing + 1);
        }

        private PlacementResult toResult() {
            List<BlockStat> stats = new ArrayList<>(blockCounts.size());
            for (Map.Entry<Integer, Integer> entry : blockCounts.entrySet()) {
                stats.add(new BlockStat(entry.getKey(), entry.getValue()));
            }

            stats.sort((left, right) -> {
                int byCount = Integer.compare(right.count, left.count);
                if (byCount != 0) {
                    return byCount;
                }
                return Integer.compare(left.packedState, right.packedState);
            });
            return new PlacementResult(placedBlocks, stats);
        }

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
     */
    private static class WeightedState {

        private final IBlockState state;
        private final int cumulativeWeight;

        private WeightedState(IBlockState state, int cumulativeWeight) {
            this.state = state;
            this.cumulativeWeight = cumulativeWeight;
        }
    }

    /**
     * 运行时矿脉实例。
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

        public long getSeed() {
            return seed;
        }

        public BlockPos getOrigin() {
            return origin;
        }

        public VeinDefinition getDefinition() {
            return definition;
        }

        public int getAnchorChunkX() {
            return anchorChunkX;
        }

        public int getAnchorChunkZ() {
            return anchorChunkZ;
        }
    }
}
