package com.serliunx.configurableoreveins.config;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

/**
 * 客户端相关配置
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @since 2026/3/8
 */
@Config(modid = ConfigurableOreVeinsMod.MOD_ID,
        name = "Client",
        category = "client"
)
public class ClientConfig {

    @Config.Name("renderHighlightName")
    @Config.LangKey("config.configurableoreveins.disableIronOre")
    @Config.Comment("Should render highlight vein name.")
    public static boolean renderHighlightName = true;

    private ClientConfig() {}

    /**
     * 配置同步
     */
    public static void sync() {
        ConfigManager.sync(ConfigurableOreVeinsMod.MOD_ID, Config.Type.INSTANCE);
    }
}
