package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.client.ClientNearbyVeinCache;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 同步附近矿脉缓存的客户端消息处理器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class SyncNearbyVeinsMessageHandler implements IMessageHandler<SyncNearbyVeinsMessage, IMessage> {
    /**
     * 执行 onMessage 逻辑。
     *
     * @param message 参数 message。
     * @param ctx 参数 ctx。
     * @return 处理结果。
    */
    @Override
    public IMessage onMessage(final SyncNearbyVeinsMessage message, MessageContext ctx) {
        Minecraft.getMinecraft()
                .addScheduledTask(
                        new Runnable() {
                            @Override
                            public void run() {
                                ClientNearbyVeinCache.update(
                                        message.getDimensionId(), message.getRangeChunks(), message.getVeins());
                            }
                        });
        return null;
    }
}
