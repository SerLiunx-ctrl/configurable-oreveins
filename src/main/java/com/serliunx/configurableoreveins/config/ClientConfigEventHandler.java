package com.serliunx.configurableoreveins.config;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 配置更改事件监听及处理
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/8
 */
@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientConfigEventHandler {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ClientConfigEventHandler());
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!ConfigurableOreVeinsMod.MOD_ID.equals(event.getModID())) {
            return;
        }
        ConfigManager.sync(ConfigurableOreVeinsMod.MOD_ID, Config.Type.INSTANCE);
    }
}
