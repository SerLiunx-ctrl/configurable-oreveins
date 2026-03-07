package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.config.GeneralConfig;
import com.serliunx.configurableoreveins.item.VeinLocatorItem;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 请求同步附近矿脉的网络消息。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class RequestNearbyVeinsMessage implements IMessage {
    /** 构造 RequestNearbyVeinsMessage 实例。 */
    public RequestNearbyVeinsMessage() {}

    /**
     * 执行 fromBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void fromBytes(ByteBuf buf) {}

    /**
     * 执行 toBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void toBytes(ByteBuf buf) {}

    /**
     * 请求附近矿脉的服务端处理器。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class Handler implements IMessageHandler<RequestNearbyVeinsMessage, IMessage> {
        /**
         * 执行 onMessage 逻辑。
         *
         * @param message 参数 message。
         * @param ctx 参数 ctx。
         * @return 处理结果。
        */
        @Override
        public IMessage onMessage(RequestNearbyVeinsMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player
                    .getServerWorld()
                    .addScheduledTask(
                            new Runnable() {
                                @Override
                                public void run() {
                                    int rangeChunks = Math.max(1, GeneralConfig.locatorRangeChunks);
                                    int maxResults = Math.max(1, GeneralConfig.locatorMaxResults);
                                    List<LocatorVeinInfo> nearbyVeins =
                                            VeinLocatorItem.createNearbyVeinInfos(
                                                    player.getServerWorld(), player, rangeChunks, maxResults);
                                    NetworkHandler.CHANNEL.sendTo(
                                            new SyncNearbyVeinsMessage(
                                                    player.dimension, rangeChunks, nearbyVeins),
                                            player);
                                }
                            });
            return null;
        }
    }
}
