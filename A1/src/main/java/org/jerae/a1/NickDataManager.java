package org.jerae.a1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NickDataManager {

    private final File dataFile;
    private final Gson gson;
    private Map<UUID, PlayerData> dataMap;

    public NickDataManager(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.dataFile = new File(dataFolder, "data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataMap = new HashMap<>();
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            saveData();
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>() {}.getType();
            Map<UUID, PlayerData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                this.dataMap = loaded;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(dataMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        return data != null ? data.getNickname() : null;
    }

    public void setNickname(UUID uuid, String nickname) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        data.setNickname(nickname);
        saveData();
    }

    public void removeNickname(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        if (data != null) {
            data.setNickname(null);
            if (data.isEmpty()) {
                dataMap.remove(uuid);
            }
            saveData();
        }
    }

    public static class PlayerData {
        private String nickname;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public boolean isEmpty() {
            return nickname == null;
        }
    }
}
