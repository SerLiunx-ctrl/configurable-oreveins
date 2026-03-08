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
 * 矿脉配置管理命令
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class OreVeinsCommand extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "oreveins";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/oreveins reload";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length != 1 || !"reload".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getUsage(sender));
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
                "Failed to reload ore vein config. See server log for details.");
    }

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
