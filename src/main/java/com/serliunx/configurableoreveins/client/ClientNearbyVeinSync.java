package com.serliunx.configurableoreveins.client;

import com.serliunx.configurableoreveins.network.NetworkHandler;
import com.serliunx.configurableoreveins.network.message.RequestNearbyVeinsMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * 客户端附近矿脉同步器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class ClientNearbyVeinSync {

    private static final int REQUEST_INTERVAL_TICKS = 100;

    private long lastRequestTick = Long.MIN_VALUE;
    private int lastDimensionId = Integer.MIN_VALUE;

    /**
     * 在客户端 Tick 阶段请求同步附近矿脉。
     *
     * @param event 参数 event。
    */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Loader.isModLoaded("jei")) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null) {
            ClientNearbyVeinCache.clear();
            lastRequestTick = Long.MIN_VALUE;
            lastDimensionId = Integer.MIN_VALUE;
            return;
        }

        int dimensionId = minecraft.world.provider.getDimension();
        long worldTime = minecraft.world.getTotalWorldTime();
        if (dimensionId != lastDimensionId) {
            ClientNearbyVeinCache.clear();
            lastRequestTick = Long.MIN_VALUE;
            lastDimensionId = dimensionId;
        }
        if ((worldTime - lastRequestTick) < REQUEST_INTERVAL_TICKS) {
            return;
        }

        lastRequestTick = worldTime;
        NetworkHandler.CHANNEL.sendToServer(new RequestNearbyVeinsMessage());
    }
}
