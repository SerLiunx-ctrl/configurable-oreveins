package com.serliunx.configurableoreveins.network;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 打开矿脉定位界面的客户端消息处理器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class OpenLocatorGuiMessageHandler
        implements IMessageHandler<OpenLocatorGuiMessage, IMessage> {
    /**
     * 执行 onMessage 逻辑。
     *
     * @param message 参数 message。
     * @param ctx 参数 ctx。
     * @return 处理结果。
    */
    @Override
    public IMessage onMessage(final OpenLocatorGuiMessage message, MessageContext ctx) {
        ConfigurableOreVeinsMod.proxy.openLocatorGui(message);
        return null;
    }
}
