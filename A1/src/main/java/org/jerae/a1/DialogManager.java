package org.jerae.a1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jerae.a2.A2API;
import org.jerae.a3.A3API;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DialogManager {

    private final Plugin plugin;
    private Map<String, String> dialogs = new HashMap<>();
    private final Gson gson = new Gson();

    public DialogManager(Plugin plugin) {
        this.plugin = plugin;
        loadDialogs();
    }

    public void loadDialogs() {
        dialogs.clear();
        File file = new File(plugin.getDataFolder(), "dialogs.json");

        if (!file.exists()) {
            plugin.saveResource("dialogs.json", false);
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                dialogs.putAll(loaded);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load dialogs.json", e);
        }
    }

    public Component getParsedDialog(Player player, String dialogId) {
        String rawText = dialogs.get(dialogId);
        if (rawText == null) {
            return null;
        }

        String parsedPlaceholder = A3API.parseToString(player, rawText);

        return A2API.format(parsedPlaceholder, true, true, true, true, true);
    }
}
