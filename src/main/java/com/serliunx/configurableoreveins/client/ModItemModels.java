package com.serliunx.configurableoreveins.client;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 客户端物品模型注册工具。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
@Mod.EventBusSubscriber(modid = ConfigurableOreVeinsMod.MOD_ID, value = Side.CLIENT)
public final class ModItemModels {

    /** 构造 ModItemModels 实例。 */
    private ModItemModels() {}

    /**
     * 注册模组物品模型。
     *
     * @param event 参数 event。
    */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                ModItems.VEIN_LOCATOR,
                0,
                new ModelResourceLocation(ModItems.VEIN_LOCATOR.getRegistryName(), "inventory"));
    }
}
