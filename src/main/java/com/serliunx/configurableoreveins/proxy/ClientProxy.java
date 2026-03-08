package com.serliunx.configurableoreveins.proxy;

import com.serliunx.configurableoreveins.client.ClientNearbyVeinCache;
import com.serliunx.configurableoreveins.client.ClientNearbyVeinSync;
import com.serliunx.configurableoreveins.client.ClientVeinHighlightRenderer;
import com.serliunx.configurableoreveins.client.gui.VeinLocatorGui;
import com.serliunx.configurableoreveins.network.message.OpenLocatorGuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

/**
 * 客户端代理实现.
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void initClientSystems() {
        MinecraftForge.EVENT_BUS.register(new ClientVeinHighlightRenderer());
        MinecraftForge.EVENT_BUS.register(new ClientNearbyVeinSync());
    }

    /**
     * 打开矿脉定位界面。
     *
     * @param message 参数 message。
    */
    @Override
    public void openLocatorGui(final OpenLocatorGuiMessage message) {
        Minecraft.getMinecraft()
                .addScheduledTask(
                        () -> {
                            int dimensionId =
                                    Minecraft.getMinecraft().world == null
                                            ? Integer.MIN_VALUE
                                            : Minecraft.getMinecraft().world.provider.getDimension();
                            ClientNearbyVeinCache.update(
                                    dimensionId, message.getRangeChunks(), message.getVeins());
                            Minecraft.getMinecraft()
                                    .displayGuiScreen(
                                            new VeinLocatorGui(
                                                    message.getHandOrdinal(),
                                                    message.getRangeChunks(),
                                                    message.getVeins()));
                        });
    }
}
