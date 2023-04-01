package me.scyphers.customcraft.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public abstract class ConfigFile {

    protected final Plugin plugin;
    private final File file;
    private final String filePath;

    public ConfigFile(Plugin plugin, String filePath) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), filePath);
        this.filePath = filePath;
    }

    public void load() throws Exception {
        if (!this.getFile().exists()) {
            this.getFile().getParentFile().mkdirs();
            this.getPlugin().saveResource(this.getFilePath(), false);
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.getFile());
        this.load(configuration);
    }

    public abstract void load(YamlConfiguration file) throws Exception;

    // Default save operation is to do nothing
    public void save() throws Exception {}

    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the {@link File} for this data file
     * @return the file
     */
    public @NotNull File getFile() {
        return file;
    }

    public @NotNull String getFilePath() {
        return filePath;
    }

}
