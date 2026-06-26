package org.jerae.a1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Plugin plugin;
    private final File dataFile;
    private final Gson gson;
    private Map<UUID, PlayerData> dataMap;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "nicknames.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataMap = new HashMap<>();
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            save();
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>() {}.getType();
            Map<UUID, PlayerData> loadedData = gson.fromJson(reader, type);
            if (loadedData != null) {
                dataMap = loadedData;
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load nicknames.json");
            e.printStackTrace();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(dataMap, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save nicknames.json");
            e.printStackTrace();
        }
    }

    public void setNickname(UUID uuid, String nickname) {
        dataMap.computeIfAbsent(uuid, k -> new PlayerData()).setNickname(nickname);
        save();
    }

    public String getNickname(UUID uuid) {
        PlayerData pd = dataMap.get(uuid);
        return pd != null ? pd.getNickname() : null;
    }

    public static class PlayerData {
        private String nickname;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
