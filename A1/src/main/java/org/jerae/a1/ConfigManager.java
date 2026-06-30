package org.jerae.a1;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
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

        // Handle config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
            String configVersion = config.getString("config-version");
            if (configVersion == null || !configVersion.equals(pluginVersion)) {
                File oldFile = new File(plugin.getDataFolder(), "config.yml.old");
                try {
                    Files.move(configFile.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not backup config.yml");
                }
                plugin.saveResource("config.yml", true);
                FileConfiguration newConfig = YamlConfiguration.loadConfiguration(configFile);

                // Keep the current settings
                for (String key : config.getKeys(true)) {
                    if (!key.equals("config-version")) {
                        newConfig.set(key, config.get(key));
                    }
                }
                config = newConfig;
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save updated config.yml");
                }
            }
        } else {
            plugin.saveResource("config.yml", false);
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        // Handle message.yml
        messagesFile = new File(plugin.getDataFolder(), "message.yml");
        if (messagesFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            String messageVersion = messages.getString("message-version");
            if (messageVersion == null || !messageVersion.equals(pluginVersion)) {
                File oldFile = new File(plugin.getDataFolder(), "message.yml.old");
                try {
                    Files.move(messagesFile.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not backup message.yml");
                }
                plugin.saveResource("message.yml", true);
                FileConfiguration newMessages = YamlConfiguration.loadConfiguration(messagesFile);

                // Keep the current settings
                for (String key : messages.getKeys(true)) {
                    if (!key.equals("message-version")) {
                        newMessages.set(key, messages.get(key));
                    }
                }
                messages = newMessages;
                try {
                    messages.save(messagesFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save updated message.yml");
                }
            }
        } else {
            plugin.saveResource("message.yml", false);
            messages = YamlConfiguration.loadConfiguration(messagesFile);
        }
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
