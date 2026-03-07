package com.serliunx.configurableoreveins.command;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.config.GeneralConfig;
import com.serliunx.configurableoreveins.config.ModConfigManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * 矿脉配置管理命令。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class OreVeinsCommand extends CommandBase {

    /**
     * 获取命令名称。
     *
     * @return 处理结果。
    */
    @Nonnull
    @Override
    public String getName() {
        return "oreveins";
    }

    /**
     * 获取命令用法说明。
     *
     * @param sender 参数 sender。
     * @return 处理结果。
    */
    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/oreveins reload";
    }

    /**
     * 获取命令所需权限等级。
     *
     * @return 处理结果。
    */
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * 执行命令逻辑。
     *
     * @param server 参数 server。
     * @param sender 参数 sender。
     * @param args 参数 args。
     * @throws CommandException 执行过程中可能抛出的异常。
    */
    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length != 1 || !"reload".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        ModConfigManager configManager = ConfigurableOreVeinsMod.getConfigManager();
        GeneralConfig.sync();
        boolean reloaded = configManager != null && configManager.reloadNow();
        if (reloaded) {
            notifyCommandListener(
                    sender,
                    this,
                    "Reloaded %s and configurableoreveins.cfg",
                    configManager.getConfigFilePath());
            return;
        }

        throw new CommandException(
                "Failed to reload ore vein config. See server log for details.", new Object[0]);
    }

    /**
     * 获取命令补全列表。
     *
     * @param server 参数 server。
     * @param sender 参数 sender。
     * @param args 参数 args。
     * @param targetPos 参数 targetPos。
     * @return 处理结果。
    */
    @Nonnull
    @Override
    public List<String> getTabCompletions(
            @Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Collections.singletonList("reload"));
        }
        return Collections.emptyList();
    }
}
