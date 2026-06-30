package org.jerae.a1;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration messages;
    private File messagesFile;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        String pluginVersion = plugin.getPluginMeta().getVersion();

        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = processConfig(configFile, "config.yml", "config-version", pluginVersion);

        messagesFile = new File(plugin.getDataFolder(), "message.yml");
        messages = processConfig(messagesFile, "message.yml", "message-version", pluginVersion);
    }

    private FileConfiguration processConfig(File file, String resourceName, String versionKey, String pluginVersion) {
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
            return YamlConfiguration.loadConfiguration(file);
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);
        String currentVersion = currentConfig.getString(versionKey);

        boolean needsUpdate = currentVersion == null || !currentVersion.equals(pluginVersion);

        InputStream defaultStream = plugin.getResource(resourceName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            for (String key : defaultConfig.getKeys(true)) {
                if (!currentConfig.contains(key)) {
                    needsUpdate = true;
                    break;
                }
            }
        }

        if (needsUpdate) {
            File oldFile = new File(plugin.getDataFolder(), resourceName + ".old");
            try {
                Files.move(file.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not backup " + resourceName);
            }
            plugin.saveResource(resourceName, true);
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(file);

            for (String key : currentConfig.getKeys(true)) {
                if (!key.equals(versionKey) && !(currentConfig.get(key) instanceof ConfigurationSection)) {
                    newConfig.set(key, currentConfig.get(key));
                }
            }
            try {
                newConfig.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save updated " + resourceName);
            }
            return newConfig;
        }

        return currentConfig;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public void reload() {
        loadConfigs();
    }
}
