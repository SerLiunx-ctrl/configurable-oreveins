package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.item.ModItems;
import com.serliunx.configurableoreveins.item.VeinLocatorItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 切换定位目标的网络消息。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class SetLocatorTargetMessage implements IMessage {
    private int handOrdinal;
    private boolean clear;
    private int dimensionId;
    private int x;
    private int y;
    private int z;
    private String name;
    private int highlightColor;

    /** 构造 SetLocatorTargetMessage 实例。 */
    public SetLocatorTargetMessage() {}

    /**
     * 执行 clear 逻辑。
     *
     * @param hand 参数 hand。
     * @return 处理结果。
    */
    public static SetLocatorTargetMessage clear(EnumHand hand) {
        SetLocatorTargetMessage message = new SetLocatorTargetMessage();
        message.handOrdinal = hand.ordinal();
        message.clear = true;
        message.name = "";
        return message;
    }

    /**
     * 执行 set 逻辑。
     *
     * @param hand 参数 hand。
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    public static SetLocatorTargetMessage set(EnumHand hand, LocatorVeinInfo vein) {
        SetLocatorTargetMessage message = new SetLocatorTargetMessage();
        message.handOrdinal = hand.ordinal();
        message.clear = false;
        message.dimensionId = vein.getDimensionId();
        message.x = vein.getCenterX();
        message.y = vein.getCenterY();
        message.z = vein.getCenterZ();
        message.name = vein.getName();
        message.highlightColor = vein.getHighlightColor();
        return message;
    }

    /**
     * 执行 fromBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void fromBytes(ByteBuf buf) {
        handOrdinal = buf.readInt();
        clear = buf.readBoolean();
        dimensionId = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        name = net.minecraftforge.fml.common.network.ByteBufUtils.readUTF8String(buf);
        highlightColor = buf.readInt();
    }

    /**
     * 执行 toBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(handOrdinal);
        buf.writeBoolean(clear);
        buf.writeInt(dimensionId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        net.minecraftforge.fml.common.network.ByteBufUtils.writeUTF8String(
                buf, name == null ? "" : name);
        buf.writeInt(highlightColor);
    }

    /**
     * 网络消息服务端处理器。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class Handler implements IMessageHandler<SetLocatorTargetMessage, IMessage> {
        /**
         * 执行 onMessage 逻辑。
         *
         * @param message 参数 message。
         * @param ctx 参数 ctx。
         * @return 处理结果。
        */
        @Override
        public IMessage onMessage(final SetLocatorTargetMessage message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player
                    .getServerWorld()
                    .addScheduledTask(
                            new Runnable() {
                                @Override
                                public void run() {
                                    EnumHand hand =
                                            EnumHand.values()[
                                                    Math.max(0, Math.min(EnumHand.values().length - 1, message.handOrdinal))];
                                    ItemStack stack = player.getHeldItem(hand);
                                    if (stack.getItem() != ModItems.VEIN_LOCATOR) {
                                        return;
                                    }

                                    if (message.clear) {
                                        VeinLocatorItem.clearTarget(stack);
                                        VeinLocatorItem.refreshAutomaticTarget(stack, player.getServerWorld(), player);
                                    } else {
                                        VeinLocatorItem.setTarget(
                                                stack,
                                                message.dimensionId,
                                                message.x,
                                                message.y,
                                                message.z,
                                                message.name,
                                                message.highlightColor);
                                    }
                                    player.inventory.markDirty();
                                }
                            });
            return null;
        }
    }
}
