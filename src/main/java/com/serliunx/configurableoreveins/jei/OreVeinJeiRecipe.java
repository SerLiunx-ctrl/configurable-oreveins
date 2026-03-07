package com.serliunx.configurableoreveins.jei;

import com.serliunx.configurableoreveins.client.ClientNearbyVeinCache;
import com.serliunx.configurableoreveins.config.BlockEntry;
import com.serliunx.configurableoreveins.config.VeinDefinition;
import com.serliunx.configurableoreveins.network.LocatorVeinInfo;
import com.serliunx.configurableoreveins.util.BlockStateResolver;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

/**
 * JEI 中的矿脉来源配方包装器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class OreVeinJeiRecipe implements IRecipeWrapper {

    private static final int MAX_OUTPUT_SLOTS = 5;
    private static final int MAX_STATS_LINES = 3;
    private static final int TEXT_AREA_X = 30;
    private static final int TEXT_AREA_WIDTH = 130;

    private final int veinHash;
    private final String displayName;
    private final int minY;
    private final int maxY;
    private final String dimensionText;
    private final List<ItemStack> outputs;
    private final List<String> outputTooltips;
    private final int hiddenOutputCount;

    /**
     * 构造 OreVeinJeiRecipe 实例。
     *
     * @param definition 参数 definition。
    */
    public OreVeinJeiRecipe(VeinDefinition definition) {
        this.veinHash = definition.getName().hashCode();
        this.displayName = definition.getDisplayName();
        this.minY = definition.getMinY();
        this.maxY = definition.getMaxY();
        this.dimensionText = buildDimensionText(definition);

        LinkedHashMap<Integer, Integer> weightByState = new LinkedHashMap<Integer, Integer>();
        for (BlockEntry blockEntry : definition.getBlocks()) {
            if (blockEntry == null) {
                continue;
            }
            ItemStack stack = createStack(blockEntry.getBlock(), blockEntry.getMeta());
            if (stack.isEmpty()) {
                continue;
            }
            int packedState = packState(stack);
            int currentWeight = weightByState.containsKey(packedState) ? weightByState.get(packedState) : 0;
            weightByState.put(packedState, currentWeight + blockEntry.getWeight());
        }

        int totalWeight = 0;
        for (Integer weight : weightByState.values()) {
            totalWeight += Math.max(0, weight.intValue());
        }
        List<ItemStack> allOutputs = new ArrayList<ItemStack>(weightByState.size());
        List<String> allTooltips = new ArrayList<String>(weightByState.size());
        for (Map.Entry<Integer, Integer> entry : weightByState.entrySet()) {
            ItemStack stack = unpackState(entry.getKey());
            if (stack.isEmpty()) {
                continue;
            }
            allOutputs.add(stack);
            allTooltips.add(buildOutputTooltip(stack, entry.getValue().intValue(), totalWeight));
        }

        this.hiddenOutputCount = Math.max(0, allOutputs.size() - MAX_OUTPUT_SLOTS);
        this.outputs = allOutputs.size() <= MAX_OUTPUT_SLOTS
                ? allOutputs
                : new ArrayList<ItemStack>(allOutputs.subList(0, MAX_OUTPUT_SLOTS));
        this.outputTooltips = allTooltips.size() <= MAX_OUTPUT_SLOTS
                ? allTooltips
                : new ArrayList<String>(allTooltips.subList(0, MAX_OUTPUT_SLOTS));
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
     * 获取 DisplayName。
     *
     * @return 处理结果。
    */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取输出物列表。
     *
     * @return 处理结果。
    */
    public List<ItemStack> getOutputs() {
        return outputs;
    }

    /**
     * 获取输出物 tooltip。
     *
     * @param slotIndex 参数 slotIndex。
     * @return 处理结果。
    */
    @Nullable
    public String getOutputTooltip(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= outputTooltips.size()) {
            return null;
        }
        return outputTooltips.get(slotIndex);
    }

    /**
     * 获取隐藏输出数量。
     *
     * @return 处理结果。
    */
    public int getHiddenOutputCount() {
        return hiddenOutputCount;
    }

    /**
     * 将配方输出写入 JEI 原料容器。
     *
     * @param ingredients 参数 ingredients。
    */
    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutputs(ItemStack.class, outputs);
    }

    /**
     * 绘制配方额外信息。
     *
     * @param minecraft 参数 minecraft。
     * @param recipeWidth 参数 recipeWidth。
     * @param recipeHeight 参数 recipeHeight。
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
    */
    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int textX = TEXT_AREA_X;
        int y = 6;
        y = drawWrappedText(minecraft, displayName, textX, y, 0x202020);
        y += 2;
        y = drawWrappedText(
                minecraft,
                I18n.format("jei.configurableoreveins.ore_vein.dimensions", dimensionText),
                textX,
                y,
                0x4A4A4A);
        y = drawWrappedText(
                minecraft,
                I18n.format("jei.configurableoreveins.ore_vein.height", minY, maxY),
                textX,
                y,
                0x4A4A4A);

        LocatorVeinInfo nearest = ClientNearbyVeinCache.getNearestVein(veinHash);
        if (nearest != null) {
            y = drawWrappedText(
                    minecraft,
                    I18n.format(
                            "jei.configurableoreveins.ore_vein.location",
                            nearest.getCenterX(),
                            nearest.getCenterY(),
                            nearest.getCenterZ()),
                    textX,
                    y,
                    0x2E7D32);
            y = drawWrappedText(
                    minecraft,
                    I18n.format(
                            "jei.configurableoreveins.ore_vein.nearby_count",
                            ClientNearbyVeinCache.getNearbyCount(veinHash),
                            ClientNearbyVeinCache.getRangeChunks()),
                    textX,
                    y,
                    0x4A4A4A);
            y = drawWrappedText(
                    minecraft, I18n.format("jei.configurableoreveins.ore_vein.stats"), textX, y, 0x303030);
            drawStats(minecraft, nearest, textX, y);
        } else {
            y = drawWrappedText(
                    minecraft,
                    I18n.format(
                            "jei.configurableoreveins.ore_vein.location_unavailable",
                            ClientNearbyVeinCache.getRangeChunks()),
                    textX,
                    y,
                    0x4A4A4A);
            drawWrappedText(
                    minecraft,
                    I18n.format("jei.configurableoreveins.ore_vein.stats_unavailable"),
                    textX,
                    y,
                    0x4A4A4A);
        }

        if (hiddenOutputCount > 0) {
            drawWrappedText(
                    minecraft,
                    I18n.format("jei.configurableoreveins.ore_vein.more_outputs", hiddenOutputCount),
                    6,
                    100,
                    0x4A4A4A);
        }
    }

    /**
     * 绘制最近矿脉的矿石统计。
     *
     * @param minecraft 参数 minecraft。
     * @param nearest 参数 nearest。
     * @param textX 参数 textX。
     * @param startY 参数 startY。
    */
    private void drawStats(Minecraft minecraft, LocatorVeinInfo nearest, int textX, int startY) {
        int[] oreStates = nearest.getOreStateKeys();
        int[] oreCounts = nearest.getOreCounts();
        int lines = Math.min(Math.min(oreStates.length, oreCounts.length), MAX_STATS_LINES);
        int y = startY;
        for (int index = 0; index < lines; index++) {
            ItemStack stack = unpackState(oreStates[index]);
            String oreName =
                    stack.isEmpty()
                            ? I18n.format("jei.configurableoreveins.ore_vein.unknown_ore")
                            : stack.getDisplayName();
            y =
                    drawWrappedText(
                            minecraft,
                            TextFormatting.GRAY + "- " + oreName + " x" + oreCounts[index],
                            textX,
                            y,
                            0x4A4A4A);
        }
    }

    /**
     * 绘制自动换行文本并返回下一行起始 Y 坐标。
     *
     * @param minecraft 参数 minecraft。
     * @param text 参数 text。
     * @param textX 参数 textX。
     * @param startY 参数 startY。
     * @param color 参数 color。
     * @return 处理结果。
    */
    private int drawWrappedText(Minecraft minecraft, String text, int textX, int startY, int color) {
        List<String> wrapped = minecraft.fontRenderer.listFormattedStringToWidth(text, TEXT_AREA_WIDTH);
        int y = startY;
        for (String line : wrapped) {
            minecraft.fontRenderer.drawString(line, textX, y, color, false);
            y += 10;
        }
        return y;
    }

    /**
     * 构建维度摘要文本。
     *
     * @param definition 参数 definition。
     * @return 处理结果。
    */
    private static String buildDimensionText(VeinDefinition definition) {
        if (definition.getDimensionIds().isEmpty()) {
            return I18n.format("jei.configurableoreveins.ore_vein.all_dimensions");
        }
        List<String> parts = new ArrayList<String>(definition.getDimensionIds().size());
        for (Integer dimensionId : definition.getDimensionIds()) {
            parts.add(String.valueOf(dimensionId));
        }
        return String.join(", ", parts);
    }

    /**
     * 构建输出矿物的 tooltip 文本。
     *
     * @param stack 参数 stack。
     * @param weight 参数 weight。
     * @param totalWeight 参数 totalWeight。
     * @return 处理结果。
    */
    private static String buildOutputTooltip(ItemStack stack, int weight, int totalWeight) {
        double percent = totalWeight <= 0 ? 0.0D : (weight * 100.0D) / totalWeight;
        return I18n.format(
                "jei.configurableoreveins.ore_vein.output_weight",
                stack.getDisplayName(),
                String.format(Locale.ROOT, "%.1f", percent),
                weight);
    }

    /**
     * 根据配置方块创建物品堆。
     *
     * @param blockName 参数 blockName。
     * @param meta 参数 meta。
     * @return 处理结果。
    */
    private static ItemStack createStack(String blockName, int meta) {
        net.minecraft.block.state.IBlockState state = BlockStateResolver.resolveState(blockName, meta);
        if (state == null) {
            return ItemStack.EMPTY;
        }
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, 1, meta);
    }

    /**
     * 打包物品状态键。
     *
     * @param stack 参数 stack。
     * @return 处理结果。
    */
    private static int packState(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == null) {
            return 0;
        }
        return (Block.getIdFromBlock(block) << 4) | (stack.getMetadata() & 15);
    }

    /**
     * 解包物品状态键。
     *
     * @param packedState 参数 packedState。
     * @return 处理结果。
    */
    private static ItemStack unpackState(int packedState) {
        if (packedState <= 0) {
            return ItemStack.EMPTY;
        }
        int blockId = packedState >>> 4;
        int meta = packedState & 15;
        Block block = Block.getBlockById(blockId);
        if (block == null) {
            return ItemStack.EMPTY;
        }
        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, 1, meta);
    }
}
