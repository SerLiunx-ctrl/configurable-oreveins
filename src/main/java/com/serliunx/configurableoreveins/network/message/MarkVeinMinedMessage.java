package com.serliunx.configurableoreveins.network.message;

import com.serliunx.configurableoreveins.vein.LocatorVeinInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

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

    public MarkVeinMinedMessage() {}

    /**
     * 构建标记矿脉消息。
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

    public int getHandOrdinal() {
        return handOrdinal;
    }

    public boolean isMined() {
        return mined;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }
}
