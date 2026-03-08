package com.serliunx.configurableoreveins.network.handler;

import com.serliunx.configurableoreveins.data.PlayerVeinStatusData;
import com.serliunx.configurableoreveins.item.ModItems;
import com.serliunx.configurableoreveins.item.VeinLocatorItem;
import com.serliunx.configurableoreveins.network.message.MarkVeinMinedMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 消息处理器：服务器处理客户端发来的矿脉标记消息
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @since 2026/3/8
 */
public final class MarkVeinMinedMessageHandler implements IMessageHandler<MarkVeinMinedMessage, IMessage> {

    @Override
    public IMessage onMessage(MarkVeinMinedMessage message, MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld()
                .addScheduledTask(
                        () -> {
                            PlayerVeinStatusData statusData = PlayerVeinStatusData.get(player.getServerWorld());
                            statusData.setMined(
                                    player.getUniqueID(),
                                    message.getDimensionId(),
                                    message.getChunkX(),
                                    message.getChunkZ(),
                                    message.isMined());

                            EnumHand hand = EnumHand.values()[Math.max(0, Math.min(EnumHand.values().length - 1,
                                    message.getHandOrdinal()))];
                            ItemStack stack = player.getHeldItem(hand);
                            if (stack.getItem() == ModItems.VEIN_LOCATOR) {
                                if (message.isMined()) {
                                    VeinLocatorItem.clearTargetIfMatches(
                                            stack,
                                            message.getDimensionId(),
                                            message.getChunkX(),
                                            message.getCenterY(),
                                            message.getCenterZ());
                                    VeinLocatorItem.clearAutomaticTargetIfMatches(
                                            stack,
                                            message.getDimensionId(),
                                            message.getChunkX(),
                                            message.getCenterY(),
                                            message.getCenterZ());
                                }
                                VeinLocatorItem.refreshAutomaticTarget(stack, player.getServerWorld(), player);
                                player.inventory.markDirty();
                            }
                        });
        return null;
    }
}
