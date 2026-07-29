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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration messages;
    private File messagesFile;
    private FileConfiguration textholders;
    private File textholdersFile;
    private JsonObject dialogs;
    private File dialogsFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
        // Reverting the regression: message.yml doesn't need to be fully set to null, memory says users define custom sections but for messages typically we keep the versionKey to trigger missing key checks if implemented correctly, but wait, memory says "Do not use version keys (like message-version or textholder-version) for configuration files where users define custom sections (e.g., message.yml, textholder.yml).". The review complained that message.yml should not have its versionKey removed. I will revert it to "message-version".
        messages = processConfig(messagesFile, "message.yml", "message-version", pluginVersion);

        if (plugin.getServer().getPluginManager().getPlugin("A4") != null) {
            textholdersFile = new File(plugin.getDataFolder(), "textholder.yml");
            textholders = processConfig(textholdersFile, "textholder.yml", null, null);
        }

        File oldTextholder = new File(plugin.getDataFolder(), "textholder.yml.old");
        if (oldTextholder.exists()) {
            oldTextholder.delete();
        }

        dialogsFile = new File(plugin.getDataFolder(), "dialogs.json");
        loadDialogs();
    }

    private FileConfiguration processConfig(File file, String resourceName, String versionKey, String pluginVersion) {
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
            return YamlConfiguration.loadConfiguration(file);
        }

        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);

        boolean needsUpdate = false;
        if (versionKey != null) {
            String currentVersion = currentConfig.getString(versionKey);
            needsUpdate = currentVersion == null || !currentVersion.equals(pluginVersion);
        }

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
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(file);
            newConfig.options().copyHeader(true);

            for (String key : currentConfig.getKeys(true)) {
                if ((versionKey == null || !key.equals(versionKey)) && !(currentConfig.get(key) instanceof ConfigurationSection)) {
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

    public FileConfiguration getTextholders() {
        return textholders;
    }

    public void loadDialogs() {
        if (!dialogsFile.exists()) {
            plugin.saveResource("dialogs.json", false);
        }

        try (FileReader reader = new FileReader(dialogsFile)) {
            dialogs = JsonParser.parseReader(reader).getAsJsonObject();

            boolean updated = false;
            // Iterate over entries to perform graceful migration if necessary (as per memory)
            for (String key : dialogs.keySet()) {
                if (dialogs.get(key).isJsonPrimitive()) {
                    JsonObject newObj = new JsonObject();
                    newObj.addProperty("type", "notice");
                    newObj.addProperty("title", "Dialog");
                    com.google.gson.JsonArray bodyArray = new com.google.gson.JsonArray();
                    JsonObject bodyObj = new JsonObject();
                    bodyObj.addProperty("contents", dialogs.get(key).getAsString());
                    bodyArray.add(bodyObj);
                    newObj.add("body", bodyArray);

                    dialogs.add(key, newObj);
                    updated = true;
                }
            }

            if (updated) {
                try (FileWriter writer = new FileWriter(dialogsFile)) {
                    gson.toJson(dialogs, writer);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load dialogs.json: " + e.getMessage());
            dialogs = new JsonObject();
        }
    }

    public JsonObject getDialogs() {
        return dialogs;
    }

    public void reload() {
        loadConfigs();
    }
}
