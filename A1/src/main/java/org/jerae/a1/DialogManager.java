package org.jerae.a1;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.dialog.DialogLike;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
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
    private Map<String, JsonObject> dialogs = new HashMap<>();
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
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root != null) {
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        dialogs.put(entry.getKey(), entry.getValue().getAsJsonObject());
                    } else if (entry.getValue().isJsonPrimitive()) {
                        // Migrate old string format in memory
                        String text = entry.getValue().getAsString();
                        JsonObject migrated = new JsonObject();
                        migrated.addProperty("type", "minecraft:notice");
                        migrated.addProperty("title", "Dialog");
                        JsonArray bodyArray = new JsonArray();
                        JsonObject bodyObj = new JsonObject();
                        bodyObj.addProperty("type", "minecraft:plain_message");
                        bodyObj.addProperty("contents", text);
                        bodyArray.add(bodyObj);
                        migrated.add("body", bodyArray);
                        dialogs.put(entry.getKey(), migrated);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load dialogs.json", e);
        }
    }

    public DialogLike getParsedDialog(Player player, String dialogId) {
        JsonObject dialogObj = dialogs.get(dialogId);
        if (dialogObj == null) {
            return null;
        }

        String titleStr = "Dialog";
        if (dialogObj.has("title") && !dialogObj.get("title").isJsonNull()) {
            titleStr = dialogObj.get("title").getAsString();
        }

        String bodyStr = "";
        if (dialogObj.has("body") && dialogObj.get("body").isJsonArray()) {
            JsonArray bodyArray = dialogObj.getAsJsonArray("body");
            if (bodyArray.size() > 0) {
                JsonObject firstBody = bodyArray.get(0).getAsJsonObject();
                if (firstBody.has("contents") && !firstBody.get("contents").isJsonNull()) {
                    bodyStr = firstBody.get("contents").getAsString();
                }
            }
        } else if (dialogObj.has("body") && dialogObj.get("body").isJsonObject()) {
            JsonObject bodyObj = dialogObj.getAsJsonObject("body");
            if (bodyObj.has("contents") && !bodyObj.get("contents").isJsonNull()) {
                bodyStr = bodyObj.get("contents").getAsString();
            }
        }

        String parsedTitlePlaceholder = A3API.parseToString(player, titleStr);
        Component finalTitle = A2API.format(parsedTitlePlaceholder, true, true, true, true, true);

        String parsedBodyPlaceholder = A3API.parseToString(player, bodyStr);
        Component finalBody = A2API.format(parsedBodyPlaceholder, true, true, true, true, true);

        return Dialog.create(factory -> {
            factory.empty().base(DialogBase.builder(finalTitle)
                        .body(java.util.List.of(DialogBody.plainMessage(finalBody)))
                        .build())
                   .type(DialogType.notice(io.papermc.paper.registry.data.dialog.ActionButton.builder(net.kyori.adventure.text.Component.text("OK")).build()));
        });
    }
}
