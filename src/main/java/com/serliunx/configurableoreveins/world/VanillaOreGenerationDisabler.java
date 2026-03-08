package com.serliunx.configurableoreveins.world;

import com.serliunx.configurableoreveins.config.ModConfiguration;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 原版矿物生成禁用处理
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
@SuppressWarnings("unused")
public final class VanillaOreGenerationDisabler {

    public VanillaOreGenerationDisabler() {}

    /**
     * 执行 onGenerateMinable 逻辑。
     *
     * @param event 参数 event。
    */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGenerateMinable(OreGenEvent.GenerateMinable event) {
        OreGenEvent.GenerateMinable.EventType type = event.getType();
        if (type == OreGenEvent.GenerateMinable.EventType.IRON && ModConfiguration.general.disableIronOre) {
            event.setResult(Result.DENY);
            return;
        }

        if (type == OreGenEvent.GenerateMinable.EventType.GOLD && ModConfiguration.general.disableGoldOre) {
            event.setResult(Result.DENY);
            return;
        }

        if (type == OreGenEvent.GenerateMinable.EventType.REDSTONE && ModConfiguration.general.disableRedstoneOre) {
            event.setResult(Result.DENY);
            return;
        }

        if (type == OreGenEvent.GenerateMinable.EventType.DIAMOND && ModConfiguration.general.disableDiamondOre) {
            event.setResult(Result.DENY);
            return;
        }

        if (type == OreGenEvent.GenerateMinable.EventType.LAPIS && ModConfiguration.general.disableLapisOre) {
            event.setResult(Result.DENY);
            return;
        }

        if (type == OreGenEvent.GenerateMinable.EventType.EMERALD && ModConfiguration.general.disableEmeraldOre) {
            event.setResult(Result.DENY);
            return;
        }

        if (type == OreGenEvent.GenerateMinable.EventType.COAL && ModConfiguration.general.disableCoalOre) {
            event.setResult(Result.DENY);
        }
    }
}
