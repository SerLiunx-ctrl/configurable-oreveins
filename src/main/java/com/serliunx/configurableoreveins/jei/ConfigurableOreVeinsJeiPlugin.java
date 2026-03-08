package com.serliunx.configurableoreveins.jei;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.config.ModConfigManager;
import com.serliunx.configurableoreveins.config.VeinDefinition;
import com.serliunx.configurableoreveins.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI适配相关功能适配
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
@JEIPlugin
@SuppressWarnings("unused")
public class ConfigurableOreVeinsJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new OreVeinJeiCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    @SuppressWarnings("all")
    public void register(IModRegistry registry) {
        registry.addRecipes(buildRecipes(), OreVeinJeiCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.VEIN_LOCATOR), OreVeinJeiCategory.UID);
        registry.addIngredientInfo(
                new ItemStack(ModItems.VEIN_LOCATOR),
                ItemStack.class,
                I18n.format("jei.configurableoreveins.ore_vein.locator_info"));
    }

    /**
     * 构建 JEI 矿脉配方列表。
     */
    private List<OreVeinJeiRecipe> buildRecipes() {
        List<OreVeinJeiRecipe> recipes = new ArrayList<>();
        ModConfigManager configManager = ConfigurableOreVeinsMod.getConfigManager();
        if (configManager == null) {
            return recipes;
        }
        for (VeinDefinition vein : configManager.getVeins()) {
            if (vein == null || !vein.isEnabled() || vein.getBlocks().isEmpty()) {
                continue;
            }
            recipes.add(new OreVeinJeiRecipe(vein));
        }
        return recipes;
    }
}
