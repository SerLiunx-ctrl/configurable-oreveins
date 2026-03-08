package com.serliunx.configurableoreveins;

import com.serliunx.configurableoreveins.command.OreVeinsCommand;
import com.serliunx.configurableoreveins.config.ClientConfig;
import com.serliunx.configurableoreveins.config.GeneralConfig;
import com.serliunx.configurableoreveins.config.ModConfigManager;
import com.serliunx.configurableoreveins.network.NetworkHandler;
import com.serliunx.configurableoreveins.proxy.CommonProxy;
import com.serliunx.configurableoreveins.world.ConfigurableOreWorldGenerator;
import com.serliunx.configurableoreveins.world.VanillaOreGenerationDisabler;
import java.io.File;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 主类
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
@Mod(
        modid = ConfigurableOreVeinsMod.MOD_ID,
        name = ConfigurableOreVeinsMod.MOD_NAME,
        version = ConfigurableOreVeinsMod.VERSION,
        acceptableRemoteVersions = "*")
public class ConfigurableOreVeinsMod {
    public static final String MOD_ID = "configurableoreveins";
    public static final String MOD_NAME = "Configurable Ore Veins";
    public static final String VERSION = "0.0.1";

    @SidedProxy(
            clientSide = "com.serliunx.configurableoreveins.proxy.ClientProxy",
            serverSide = "com.serliunx.configurableoreveins.proxy.CommonProxy")
    public static CommonProxy proxy;

    private static ModConfigManager configManager;

    /**
     * 执行模组预初始化逻辑。
     *
     * @param event 参数 event。
    */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GeneralConfig.sync();
        ClientConfig.sync();
        NetworkHandler.init();
        File configDirectory = new File(event.getModConfigurationDirectory(), MOD_ID);
        configManager = new ModConfigManager(configDirectory);
        configManager.load();
    }

    /**
     * 执行模组初始化逻辑。
     *
     * @param event 参数 event。
    */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        IWorldGenerator generator = new ConfigurableOreWorldGenerator(configManager);
        GameRegistry.registerWorldGenerator(generator, 0);
        MinecraftForge.ORE_GEN_BUS.register(new VanillaOreGenerationDisabler());
        proxy.initClientSystems();
    }

    /**
     * 执行服务端启动阶段逻辑。
     *
     * @param event 参数 event。
    */
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new OreVeinsCommand());
    }

    /**
     * 获取当前矿脉配置管理器。
     *
     * @return 处理结果。
    */
    public static ModConfigManager getConfigManager() {
        return configManager;
    }
}
