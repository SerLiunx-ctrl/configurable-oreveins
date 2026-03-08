package com.serliunx.configurableoreveins.network.handler;

import com.serliunx.configurableoreveins.config.ModConfiguration;
import com.serliunx.configurableoreveins.item.VeinLocatorItem;
import com.serliunx.configurableoreveins.network.NetworkHandler;
import com.serliunx.configurableoreveins.network.message.RequestNearbyVeinsMessage;
import com.serliunx.configurableoreveins.network.message.SyncNearbyVeinsMessage;
import com.serliunx.configurableoreveins.vein.LocatorVeinInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

/**
 * 消息处理器：请求同步附近矿脉的网络消息; 用于处理定位器实时指向和JEI同步
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @since 2026/3/8
 * @see com.serliunx.configurableoreveins.client.ClientNearbyVeinSync
 */
public final class RequestNearbyVeinsMessageHandler implements IMessageHandler<RequestNearbyVeinsMessage, IMessage> {

    @Override
    public IMessage onMessage(RequestNearbyVeinsMessage message, MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        player
                .getServerWorld()
                .addScheduledTask(
                        () -> {
                            int rangeChunks = Math.max(1, ModConfiguration.general.locatorRangeChunks);
                            int maxResults = Math.max(1, ModConfiguration.general.locatorMaxResults);
                            List<LocatorVeinInfo> nearbyVeins =
                                    VeinLocatorItem.createNearbyVeinInfos(
                                            player.getServerWorld(), player, rangeChunks, maxResults);
                            NetworkHandler.CHANNEL.sendTo(
                                    new SyncNearbyVeinsMessage(
                                            player.dimension, rangeChunks, nearbyVeins),
                                    player);
                        });
        return null;
    }
}
