package com.serliunx.configurableoreveins.config;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

/**
 * 全局配置信息
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/8
 */
@Config(modid = ConfigurableOreVeinsMod.MOD_ID, category = "")
public class ModConfiguration {

    @Config.LangKey("config.configurableoreveins.client-settings")
    public static ClientConfig client = new ClientConfig();

    @Config.LangKey("config.configurableoreveins.general-settings")
    public static GeneralConfig general = new GeneralConfig();

    /**
     * 配置同步
     */
    public static void sync() {
        ConfigManager.sync(ConfigurableOreVeinsMod.MOD_ID, Config.Type.INSTANCE);
    }

    /**
     * 客户端相关配置
     */
    public static class ClientConfig {

        @Config.Name("renderHighlightName")
        @Config.LangKey("config.configurableoreveins.renderHighlightName")
        @Config.Comment("Should render highlight vein name.")
        public boolean renderHighlightName = true;

        private ClientConfig() {}
    }

    /**
     * 通用配置
     */
    public static final class GeneralConfig {

        @Config.Name("disableIronOre")
        @Config.LangKey("config.configurableoreveins.disableIronOre")
        @Config.Comment("Disable vanilla iron ore generation.")
        public boolean disableIronOre = true;

        @Config.Name("disableGoldOre")
        @Config.LangKey("config.configurableoreveins.disableGoldOre")
        @Config.Comment("Disable vanilla gold ore generation.")
        public boolean disableGoldOre = true;

        @Config.Name("disableRedstoneOre")
        @Config.LangKey("config.configurableoreveins.disableRedstoneOre")
        @Config.Comment("Disable vanilla redstone ore generation.")
        public boolean disableRedstoneOre = true;

        @Config.Name("disableDiamondOre")
        @Config.LangKey("config.configurableoreveins.disableDiamondOre")
        @Config.Comment("Disable vanilla diamond ore generation.")
        public boolean disableDiamondOre = true;

        @Config.Name("disableLapisOre")
        @Config.LangKey("config.configurableoreveins.disableLapisOre")
        @Config.Comment("Disable vanilla lapis ore generation.")
        public boolean disableLapisOre = true;

        @Config.Name("disableEmeraldOre")
        @Config.LangKey("config.configurableoreveins.disableEmeraldOre")
        @Config.Comment("Disable vanilla emerald ore generation.")
        public boolean disableEmeraldOre = true;

        @Config.Name("disableCoalOre")
        @Config.LangKey("config.configurableoreveins.disableCoalOre")
        @Config.Comment("Disable vanilla coal ore generation.")
        public boolean disableCoalOre = true;

        @Config.Name("locatorRangeChunks")
        @Config.LangKey("config.configurableoreveins.locatorRangeChunks")
        @Config.Comment("How many chunks around the player the vein locator scans.")
        public int locatorRangeChunks = 8;

        @Config.Name("locatorMaxResults")
        @Config.LangKey("config.configurableoreveins.locatorMaxResults")
        @Config.Comment("Maximum number of nearby veins the locator reports.")
        public int locatorMaxResults = 64;

        private GeneralConfig() {}
    }
}
