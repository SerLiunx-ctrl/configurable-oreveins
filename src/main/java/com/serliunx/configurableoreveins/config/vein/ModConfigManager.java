package com.serliunx.configurableoreveins.config.vein;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import net.minecraftforge.fml.common.FMLLog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * 矿脉配置加载与热重载管理器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class ModConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long RELOAD_CHECK_INTERVAL_MS = 2000L;

    private final File configDirectory;
    private final File veinsFile;
    private ModConfigData configData = new ModConfigData();
    private long lastLoadedTimestamp = -1L;
    private long lastReloadCheckTimestamp = 0L;

    public ModConfigManager(File configDirectory) {
        this.configDirectory = configDirectory;
        this.veinsFile = new File(configDirectory, "veins.json");
    }

    /**
     * 加载矿脉配置
     */
    public synchronized boolean load() {
        ensureDefaultConfig();

        try {
            String json = new String(Files.readAllBytes(veinsFile.toPath()), StandardCharsets.UTF_8);
            ModConfigData loaded = GSON.fromJson(json, ModConfigData.class);
            configData = loaded == null ? new ModConfigData() : loaded;
            lastLoadedTimestamp = getFileLastModified();
            lastReloadCheckTimestamp = System.currentTimeMillis();
            FMLLog.log.info(
                    "[{}] Loaded {} vein definitions from {}",
                    ConfigurableOreVeinsMod.MOD_ID,
                    getVeins().size(),
                    veinsFile.getAbsolutePath());
            return true;
        } catch (IOException | JsonSyntaxException exception) {
            FMLLog.log.error(
                    "[{}] Failed to load vein config from {}",
                    ConfigurableOreVeinsMod.MOD_ID,
                    veinsFile.getAbsolutePath(),
                    exception);
            if (configData == null) {
                configData = new ModConfigData();
            }
            return false;
        }
    }

    /**
     * 获取当前矿脉配置列表.
     */
    public List<VeinDefinition> getVeins() {
        return Collections.unmodifiableList(configData.getVeins());
    }

    /**
     * 在需要时重载矿脉配置
     */
    public synchronized boolean reloadIfNeeded() {
        long now = System.currentTimeMillis();
        if ((now - lastReloadCheckTimestamp) < RELOAD_CHECK_INTERVAL_MS) {
            return false;
        }

        lastReloadCheckTimestamp = now;
        long currentTimestamp = getFileLastModified();
        if (currentTimestamp <= 0L || currentTimestamp == lastLoadedTimestamp) {
            return false;
        }
        return load();
    }

    /**
     * 立即重载矿脉配置
     */
    public synchronized boolean reloadNow() {
        return load();
    }

    /**
     * 获取配置文件路径
     */
    public String getConfigFilePath() {
        return veinsFile.getAbsolutePath();
    }

    private void ensureDefaultConfig() {
        if (configDirectory.exists() || configDirectory.mkdirs()) {
            if (!veinsFile.exists()) {
                writeDefaultConfig();
            }
            return;
        }
        FMLLog.log.error(
                "[{}] Failed to create config directory {}",
                ConfigurableOreVeinsMod.MOD_ID,
                configDirectory.getAbsolutePath());
    }

    private void writeDefaultConfig() {
        ModConfigData defaults = DefaultConfigs.create();

        try (Writer writer =
                new OutputStreamWriter(Files.newOutputStream(veinsFile.toPath()), StandardCharsets.UTF_8)) {
            GSON.toJson(defaults, writer);
        } catch (IOException exception) {
            FMLLog.log.error(
                    "[{}] Failed to write default vein config to {}",
                    ConfigurableOreVeinsMod.MOD_ID,
                    veinsFile.getAbsolutePath(),
                    exception);
        }
    }

    /**
     * 获取 FileLastModified。
     *
     * @return 处理结果。
    */
    private long getFileLastModified() {
        return veinsFile.exists() ? veinsFile.lastModified() : -1L;
    }
}
