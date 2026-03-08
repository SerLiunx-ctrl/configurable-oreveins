package com.serliunx.configurableoreveins.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 请求同步附近矿脉的网络消息, 没有消息体; 标记类消息.
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class RequestNearbyVeinsMessage implements IMessage {

    public RequestNearbyVeinsMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        // 没额外操作.
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // 没额外操作.
    }
}
