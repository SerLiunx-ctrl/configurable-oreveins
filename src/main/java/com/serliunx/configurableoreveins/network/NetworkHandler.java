package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.network.handler.*;
import com.serliunx.configurableoreveins.network.message.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 模组网络消息注册器
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public final class NetworkHandler {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ConfigurableOreVeinsMod.MOD_ID);

    /**
     * 注册时自行递增的消息ID
     */
    private static int nextMessageId = 0;

    private NetworkHandler() {}

    /**
     * 初始化, 注册网络消息及处理器
     */
    public static void init() {
        CHANNEL.registerMessage(
                SetLocatorTargetMessageHandler.class,
                SetLocatorTargetMessage.class,
                nextMessageId++,
                Side.SERVER);
        CHANNEL.registerMessage(
                OpenLocatorGuiMessageHandler.class,
                OpenLocatorGuiMessage.class,
                nextMessageId++,
                Side.CLIENT);
        CHANNEL.registerMessage(
                RequestNearbyVeinsMessageHandler.class,
                RequestNearbyVeinsMessage.class,
                nextMessageId++,
                Side.SERVER);
        CHANNEL.registerMessage(
                SyncNearbyVeinsMessageHandler.class,
                SyncNearbyVeinsMessage.class,
                nextMessageId++,
                Side.CLIENT);
        CHANNEL.registerMessage(
                MarkVeinMinedMessageHandler.class,
                MarkVeinMinedMessage.class,
                nextMessageId++,
                Side.SERVER);
    }
}
