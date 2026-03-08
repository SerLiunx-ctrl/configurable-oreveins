package com.serliunx.configurableoreveins.network.handler;

import com.serliunx.configurableoreveins.item.ModItems;
import com.serliunx.configurableoreveins.item.VeinLocatorItem;
import com.serliunx.configurableoreveins.network.message.SetLocatorTargetMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 消息处理器：切换定位目标; 执行高亮等操作
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @since 2026/3/8
 */
public final class SetLocatorTargetMessageHandler implements IMessageHandler<SetLocatorTargetMessage, IMessage> {

    @Override
    public IMessage onMessage(SetLocatorTargetMessage message, MessageContext ctx) {

        final EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld()
                .addScheduledTask(
                        () -> {
                            EnumHand hand =
                                    EnumHand.values()[
                                            Math.max(0, Math.min(EnumHand.values().length - 1, message.getHandOrdinal()))];
                            ItemStack stack = player.getHeldItem(hand);
                            if (stack.getItem() != ModItems.VEIN_LOCATOR) {
                                return;
                            }

                            if (message.isClear()) {
                                VeinLocatorItem.clearTarget(stack);
                                VeinLocatorItem.refreshAutomaticTarget(stack, player.getServerWorld(), player);
                            } else {
                                VeinLocatorItem.setTarget(
                                        stack,
                                        message.getDimensionId(),
                                        message.getX(),
                                        message.getY(),
                                        message.getZ(),
                                        message.getName(),
                                        message.getHighlightColor());
                            }
                            player.inventory.markDirty();
                        });
        return null;
    }
}
