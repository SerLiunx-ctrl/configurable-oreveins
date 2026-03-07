package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.data.PlayerVeinStatusData;
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
 * 标记矿脉已挖掘的网络消息。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class MarkVeinMinedMessage implements IMessage {
    private int handOrdinal;
    private boolean mined;
    private int dimensionId;
    private int chunkX;
    private int chunkZ;
    private int centerX;
    private int centerY;
    private int centerZ;

    /** 构造 MarkVeinMinedMessage 实例。 */
    public MarkVeinMinedMessage() {}

    /**
     * 构建标记矿脉消息。
     *
     * @param hand 参数 hand。
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    public static MarkVeinMinedMessage set(EnumHand hand, LocatorVeinInfo vein, boolean mined) {
        MarkVeinMinedMessage message = new MarkVeinMinedMessage();
        message.handOrdinal = hand.ordinal();
        message.mined = mined;
        message.dimensionId = vein.getDimensionId();
        message.chunkX = vein.getChunkX();
        message.chunkZ = vein.getChunkZ();
        message.centerX = vein.getCenterX();
        message.centerY = vein.getCenterY();
        message.centerZ = vein.getCenterZ();
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
        mined = buf.readBoolean();
        dimensionId = buf.readInt();
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        centerX = buf.readInt();
        centerY = buf.readInt();
        centerZ = buf.readInt();
    }

    /**
     * 执行 toBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(handOrdinal);
        buf.writeBoolean(mined);
        buf.writeInt(dimensionId);
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeInt(centerX);
        buf.writeInt(centerY);
        buf.writeInt(centerZ);
    }

    /**
     * 标记矿脉已挖掘的服务端处理器。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    public static class Handler implements IMessageHandler<MarkVeinMinedMessage, IMessage> {
        /**
         * 执行 onMessage 逻辑。
         *
         * @param message 参数 message。
         * @param ctx 参数 ctx。
         * @return 处理结果。
        */
        @Override
        public IMessage onMessage(final MarkVeinMinedMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player
                    .getServerWorld()
                    .addScheduledTask(
                            new Runnable() {
                                @Override
                                public void run() {
                                    PlayerVeinStatusData statusData = PlayerVeinStatusData.get(player.getServerWorld());
                                    statusData.setMined(
                                            player.getUniqueID(),
                                            message.dimensionId,
                                            message.chunkX,
                                            message.chunkZ,
                                            message.mined);

                                    EnumHand hand =
                                            EnumHand.values()[
                                                    Math.max(
                                                            0,
                                                            Math.min(
                                                                    EnumHand.values().length - 1,
                                                                    message.handOrdinal))];
                                    ItemStack stack = player.getHeldItem(hand);
                                    if (stack.getItem() == ModItems.VEIN_LOCATOR) {
                                        if (message.mined) {
                                            VeinLocatorItem.clearTargetIfMatches(
                                                    stack,
                                                    message.dimensionId,
                                                    message.centerX,
                                                    message.centerY,
                                                    message.centerZ);
                                            VeinLocatorItem.clearAutomaticTargetIfMatches(
                                                    stack,
                                                    message.dimensionId,
                                                    message.centerX,
                                                    message.centerY,
                                                    message.centerZ);
                                        }
                                        VeinLocatorItem.refreshAutomaticTarget(stack, player.getServerWorld(), player);
                                        player.inventory.markDirty();
                                    }
                                }
                            });
            return null;
        }
    }
}
