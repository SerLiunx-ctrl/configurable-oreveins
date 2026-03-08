package com.serliunx.configurableoreveins.config;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.ConfigManager;

/**
 * 通用配置
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
@Config(modid = ConfigurableOreVeinsMod.MOD_ID, name = "General")
public final class GeneralConfig {

    @Name("disableIronOre")
    @LangKey("config.configurableoreveins.disableIronOre")
    @Comment("Disable vanilla iron ore generation.")
    public static boolean disableIronOre = true;

    @Name("disableGoldOre")
    @LangKey("config.configurableoreveins.disableGoldOre")
    @Comment("Disable vanilla gold ore generation.")
    public static boolean disableGoldOre = true;

    @Name("disableRedstoneOre")
    @LangKey("config.configurableoreveins.disableRedstoneOre")
    @Comment("Disable vanilla redstone ore generation.")
    public static boolean disableRedstoneOre = true;

    @Name("disableDiamondOre")
    @LangKey("config.configurableoreveins.disableDiamondOre")
    @Comment("Disable vanilla diamond ore generation.")
    public static boolean disableDiamondOre = true;

    @Name("disableLapisOre")
    @LangKey("config.configurableoreveins.disableLapisOre")
    @Comment("Disable vanilla lapis ore generation.")
    public static boolean disableLapisOre = true;

    @Name("disableEmeraldOre")
    @LangKey("config.configurableoreveins.disableEmeraldOre")
    @Comment("Disable vanilla emerald ore generation.")
    public static boolean disableEmeraldOre = true;

    @Name("disableCoalOre")
    @LangKey("config.configurableoreveins.disableCoalOre")
    @Comment("Disable vanilla coal ore generation.")
    public static boolean disableCoalOre = true;

    @Name("locatorRangeChunks")
    @LangKey("config.configurableoreveins.locatorRangeChunks")
    @Comment("How many chunks around the player the vein locator scans.")
    public static int locatorRangeChunks = 8;

    @Name("locatorMaxResults")
    @LangKey("config.configurableoreveins.locatorMaxResults")
    @Comment("Maximum number of nearby veins the locator reports.")
    public static int locatorMaxResults = 8;

    private GeneralConfig() {}

    /**
     * 配置同步
     */
    public static void sync() {
        ConfigManager.sync(ConfigurableOreVeinsMod.MOD_ID, Config.Type.INSTANCE);
    }
}
