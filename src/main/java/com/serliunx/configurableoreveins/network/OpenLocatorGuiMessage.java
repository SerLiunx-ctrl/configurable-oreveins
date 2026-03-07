package com.serliunx.configurableoreveins.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 打开矿脉定位界面的网络消息。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class OpenLocatorGuiMessage implements IMessage {
    private int handOrdinal;
    private int rangeChunks;
    private List<LocatorVeinInfo> veins = new ArrayList<LocatorVeinInfo>();

    /** 构造 OpenLocatorGuiMessage 实例。 */
    public OpenLocatorGuiMessage() {}

    /**
     * 构造 OpenLocatorGuiMessage 实例。
     *
     * @param handOrdinal 参数 handOrdinal。
     * @param rangeChunks 参数 rangeChunks。
     * @param veins 参数 veins。
    */
    public OpenLocatorGuiMessage(int handOrdinal, int rangeChunks, List<LocatorVeinInfo> veins) {
        this.handOrdinal = handOrdinal;
        this.rangeChunks = rangeChunks;
        this.veins = veins;
    }

    /**
     * 获取 HandOrdinal。
     *
     * @return 处理结果。
    */
    public int getHandOrdinal() {
        return handOrdinal;
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
     * 获取当前矿脉配置列表。
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
        handOrdinal = buf.readInt();
        rangeChunks = buf.readInt();
        int count = buf.readInt();
        veins = new ArrayList<LocatorVeinInfo>(count);
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
        buf.writeInt(handOrdinal);
        buf.writeInt(rangeChunks);
        buf.writeInt(veins.size());
        for (LocatorVeinInfo vein : veins) {
            vein.toBytes(buf);
        }
    }
}
