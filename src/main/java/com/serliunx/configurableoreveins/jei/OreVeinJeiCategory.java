package com.serliunx.configurableoreveins.jei;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.item.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI 中的矿脉来源类别.
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

    public OreVeinJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(166, 124);
        this.icon =
                guiHelper.createDrawableIngredient(
                        new ItemStack(
                                ModItems.VEIN_LOCATOR));
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format("jei.configurableoreveins.ore_vein.category");
    }

    @Nonnull
    @Override
    public String getModName() {
        return ConfigurableOreVeinsMod.MOD_NAME;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        for (int index = 0; index < OUTPUT_VISIBLE_SLOTS; index++) {
            int y = OUTPUT_START_Y + (index * OUTPUT_SPACING);
            slotDrawable.draw(minecraft, OUTPUT_START_X, y);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, final OreVeinJeiRecipe recipeWrapper, @Nonnull IIngredients ingredients) {
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
                (slotIndex, input, ingredient, tooltip) -> {
                    if (input) {
                        return;
                    }
                    String extra = recipeWrapper.getOutputTooltip(slotIndex);
                    if (extra != null && !extra.isEmpty()) {
                        tooltip.add(extra);
                    }
                });
    }
}
