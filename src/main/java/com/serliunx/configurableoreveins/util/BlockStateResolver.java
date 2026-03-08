package com.serliunx.configurableoreveins.util;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 方块状态解析与缓存工具.
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public final class BlockStateResolver {

    private static final Map<String, IBlockState> STATE_CACHE =
            Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Block> BLOCK_CACHE =
            Collections.synchronizedMap(new HashMap<>());

    private BlockStateResolver() {}

    /**
     * 解析方块状态。
     *
     * @param blockName 参数 blockName。
     * @param meta 参数 meta。
     * @return 处理结果。
    */
    @Nullable
    public static IBlockState resolveState(String blockName, int meta) {
        if (blockName == null || blockName.trim().isEmpty()) {
            return null;
        }

        String cacheKey = blockName + "#" + meta;
        if (STATE_CACHE.containsKey(cacheKey)) {
            return STATE_CACHE.get(cacheKey);
        }
        Block block = resolveBlock(blockName);
        if (block == null) {
            return null;
        }

        try {
            IBlockState state = block.getStateFromMeta(meta);
            STATE_CACHE.put(cacheKey, state);
            return state;
        } catch (RuntimeException exception) {
            FMLLog.log.warn(
                    "[{}] Invalid meta {} for block {}, falling back to default state",
                    ConfigurableOreVeinsMod.MOD_ID,
                    meta,
                    blockName);
            IBlockState state = block.getDefaultState();
            STATE_CACHE.put(cacheKey, state);
            return state;
        }
    }

    /**
     * 批量解析方块列表.
     *
     * @param blockNames 方块名称
     * @return 方块列表
    */
    public static List<Block> resolveBlocks(List<String> blockNames) {
        List<Block> resolved = new ArrayList<>();
        if (blockNames == null) {
            return resolved;
        }

        for (String blockName : blockNames) {
            if (blockName == null || blockName.trim().isEmpty()) {
                continue;
            }
            Block block = resolveBlock(blockName);
            if (block != null) {
                resolved.add(block);
            }
        }

        return resolved;
    }

    @Nullable
    private static Block resolveBlock(String blockName) {
        if (BLOCK_CACHE.containsKey(blockName)) {
            return BLOCK_CACHE.get(blockName);
        }
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
        if (block == null) {
            FMLLog.log.warn("[{}] Unknown block id {}", ConfigurableOreVeinsMod.MOD_ID, blockName);
            BLOCK_CACHE.put(blockName, null);
            return null;
        }
        BLOCK_CACHE.put(blockName, block);
        return block;
    }
}
