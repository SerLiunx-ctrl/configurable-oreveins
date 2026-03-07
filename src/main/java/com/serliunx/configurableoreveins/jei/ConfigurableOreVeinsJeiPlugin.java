package com.serliunx.configurableoreveins.jei;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.config.ModConfigManager;
import com.serliunx.configurableoreveins.config.VeinDefinition;
import com.serliunx.configurableoreveins.item.ModItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**
 * Configurable Ore Veins 的 JEI 插件入口。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
@JEIPlugin
public class ConfigurableOreVeinsJeiPlugin implements IModPlugin {

    /**
     * 注册 JEI 配方类别。
     *
     * @param registry 参数 registry。
    */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new OreVeinJeiCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    /**
     * 注册 JEI 配方和说明。
     *
     * @param registry 参数 registry。
    */
    @Override
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
     *
     * @return 处理结果。
    */
    private List<OreVeinJeiRecipe> buildRecipes() {
        List<OreVeinJeiRecipe> recipes = new ArrayList<OreVeinJeiRecipe>();
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
