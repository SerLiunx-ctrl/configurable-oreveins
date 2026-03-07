package com.serliunx.configurableoreveins.jei;

import com.serliunx.configurableoreveins.item.ModItems;
import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import java.util.List;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**
 * JEI 中的矿脉来源类别。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class OreVeinJeiCategory implements IRecipeCategory<OreVeinJeiRecipe> {
    public static final String UID = ConfigurableOreVeinsMod.MOD_ID + ".ore_veins";

    private static final int OUTPUT_START_X = 6;
    private static final int OUTPUT_START_Y = 6;
    private static final int OUTPUT_SPACING = 18;
    private static final int OUTPUT_VISIBLE_SLOTS = 5;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slotDrawable;

    /**
     * 构造 OreVeinJeiCategory 实例。
     *
     * @param guiHelper 参数 guiHelper。
    */
    public OreVeinJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(166, 124);
        this.icon =
                guiHelper.createDrawableIngredient(
                        new ItemStack(
                                ModItems.VEIN_LOCATOR));
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    /**
     * 获取类别唯一标识。
     *
     * @return 处理结果。
    */
    @Override
    public String getUid() {
        return UID;
    }

    /**
     * 获取类别标题。
     *
     * @return 处理结果。
    */
    @Override
    public String getTitle() {
        return I18n.format("jei.configurableoreveins.ore_vein.category");
    }

    /**
     * 获取模组名称。
     *
     * @return 处理结果。
    */
    @Override
    public String getModName() {
        return ConfigurableOreVeinsMod.MOD_NAME;
    }

    /**
     * 获取类别背景。
     *
     * @return 处理结果。
    */
    @Override
    public IDrawable getBackground() {
        return background;
    }

    /**
     * 获取类别图标。
     *
     * @return 处理结果。
    */
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    /**
     * 绘制固定装饰元素。
     *
     * @param minecraft 参数 minecraft。
    */
    @Override
    public void drawExtras(Minecraft minecraft) {
        for (int index = 0; index < OUTPUT_VISIBLE_SLOTS; index++) {
            int x = OUTPUT_START_X;
            int y = OUTPUT_START_Y + (index * OUTPUT_SPACING);
            slotDrawable.draw(minecraft, x, y);
        }
    }

    /**
     * 绑定类别配方布局。
     *
     * @param recipeLayout 参数 recipeLayout。
     * @param recipeWrapper 参数 recipeWrapper。
     * @param ingredients 参数 ingredients。
    */
    @Override
    public void setRecipe(IRecipeLayout recipeLayout, final OreVeinJeiRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        List<ItemStack> outputs = recipeWrapper.getOutputs();
        for (int index = 0; index < outputs.size(); index++) {
            itemStacks.init(
                    index,
                    false,
                    OUTPUT_START_X,
                    OUTPUT_START_Y + (index * OUTPUT_SPACING));
        }
        itemStacks.set(ingredients);
        itemStacks.addTooltipCallback(
                new ITooltipCallback<ItemStack>() {
                    @Override
                    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
                        if (input) {
                            return;
                        }
                        String extra = recipeWrapper.getOutputTooltip(slotIndex);
                        if (extra != null && !extra.isEmpty()) {
                            tooltip.add(extra);
                        }
                    }
                });
    }
}
