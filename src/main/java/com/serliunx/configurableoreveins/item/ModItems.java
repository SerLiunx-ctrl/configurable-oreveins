package com.serliunx.configurableoreveins.item;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 模组物品注册入口。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
@Mod.EventBusSubscriber(modid = ConfigurableOreVeinsMod.MOD_ID)
public final class ModItems {

    public static final Item VEIN_LOCATOR = createVeinLocator();

    /** 构造 ModItems 实例。 */
    private ModItems() {}

    /**
     * 创建矿脉定位仪器物品实例。
     *
     * @return 处理结果。
     */
    private static Item createVeinLocator() {
        VeinLocatorItem item = new VeinLocatorItem();
        item.setRegistryName(ConfigurableOreVeinsMod.MOD_ID, "vein_locator");
        item.setUnlocalizedName(ConfigurableOreVeinsMod.MOD_ID + ".vein_locator");
        item.setCreativeTab(CreativeTabs.TOOLS);
        item.setMaxStackSize(1);
        return item;
    }

    /**
     * 注册模组物品。
     *
     * @param event 参数 event。
    */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(VEIN_LOCATOR);
    }
}
