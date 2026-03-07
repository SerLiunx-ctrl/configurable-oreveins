package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 模组网络消息注册器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(ConfigurableOreVeinsMod.MOD_ID);

    private static int nextMessageId = 0;

    /** 构造 NetworkHandler 实例。 */
    private NetworkHandler() {}

    /** 执行模组初始化逻辑。 */
    public static void init() {
        CHANNEL.registerMessage(
                SetLocatorTargetMessage.Handler.class,
                SetLocatorTargetMessage.class,
                nextMessageId++,
                Side.SERVER);
        CHANNEL.registerMessage(
                OpenLocatorGuiMessageHandler.class,
                OpenLocatorGuiMessage.class,
                nextMessageId++,
                Side.CLIENT);
        CHANNEL.registerMessage(
                RequestNearbyVeinsMessage.Handler.class,
                RequestNearbyVeinsMessage.class,
                nextMessageId++,
                Side.SERVER);
        CHANNEL.registerMessage(
                SyncNearbyVeinsMessageHandler.class,
                SyncNearbyVeinsMessage.class,
                nextMessageId++,
                Side.CLIENT);
        CHANNEL.registerMessage(
                MarkVeinMinedMessage.Handler.class,
                MarkVeinMinedMessage.class,
                nextMessageId++,
                Side.SERVER);
    }
}
