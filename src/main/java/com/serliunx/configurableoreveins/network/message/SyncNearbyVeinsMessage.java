package com.serliunx.configurableoreveins.network.message;

import com.serliunx.configurableoreveins.vein.LocatorVeinInfo;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 同步附近矿脉缓存的网络消息。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class SyncNearbyVeinsMessage implements IMessage {

    private int dimensionId;
    private int rangeChunks;
    private List<LocatorVeinInfo> veins = new ArrayList<LocatorVeinInfo>();

    public SyncNearbyVeinsMessage() {}

    /**
     * 构造 SyncNearbyVeinsMessage 实例。
     *
     * @param dimensionId 参数 dimensionId。
     * @param rangeChunks 参数 rangeChunks。
     * @param veins 参数 veins。
    */
    public SyncNearbyVeinsMessage(int dimensionId, int rangeChunks, List<LocatorVeinInfo> veins) {
        this.dimensionId = dimensionId;
        this.rangeChunks = rangeChunks;
        this.veins = veins == null ? new ArrayList<LocatorVeinInfo>() : veins;
    }

    /**
     * 获取 DimensionId。
     *
     * @return 处理结果。
    */
    public int getDimensionId() {
        return dimensionId;
    }

    /**
     * 获取 RangeChunks。
     *
     * @return 处理结果。
    */
    public int getRangeChunks() {
        return rangeChunks;
    }

    /**
     * 获取 Veins。
     *
     * @return 处理结果。
    */
    public List<LocatorVeinInfo> getVeins() {
        return veins;
    }

    /**
     * 执行 fromBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void fromBytes(ByteBuf buf) {
        dimensionId = buf.readInt();
        rangeChunks = buf.readInt();
        int count = buf.readInt();
        veins = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            veins.add(LocatorVeinInfo.fromBytes(buf));
        }
    }

    /**
     * 执行 toBytes 逻辑。
     *
     * @param buf 参数 buf。
    */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimensionId);
        buf.writeInt(rangeChunks);
        buf.writeInt(veins.size());
        for (LocatorVeinInfo vein : veins) {
            vein.toBytes(buf);
        }
    }
}
