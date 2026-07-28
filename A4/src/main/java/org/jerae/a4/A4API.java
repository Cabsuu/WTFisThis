package org.jerae.a4;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jerae.a3.A3API;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.bukkit.Material;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;

public class A4API {

    public static Component parseTextholders(Player player, Component inputComponent, boolean isChat, ConfigurationSection section, Logger logger) {
        if (section == null) return inputComponent;

        Component result = inputComponent;

        for (String key : section.getKeys(false)) {
            ConfigurationSection holder = section.getConfigurationSection(key);
            if (holder == null) continue;

            if (isChat && !isTrue(holder, "use-in-chat")) continue;

            if (!player.hasPermission("a4.textholder." + key)) continue;

            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("[" + key + "]")
                    .replacement(builder -> {
                        String text = holder.getString("text", "");
                        Component comp = A3API.parse(player, text);

                        if (isTrue(holder, "click-event")) {
                            String type = holder.getString("click-type", "");
                            String line = holder.getString("click-line", "");

                            if (!type.isEmpty() && !line.isEmpty()) {
                                String parsedLine = A3API.parseToString(player, line);
                                switch (type.toLowerCase()) {
                                    case "run_command":
                                        comp = comp.clickEvent(ClickEvent.runCommand(parsedLine));
                                        break;
                                    case "copy_to_clipboard":
                                        comp = comp.clickEvent(ClickEvent.copyToClipboard(parsedLine));
                                        break;
                                    case "suggest_command":
                                        comp = comp.clickEvent(ClickEvent.suggestCommand(parsedLine));
                                        break;
                                    case "open_url":
                                        comp = comp.clickEvent(ClickEvent.openUrl(parsedLine));
                                        break;
                                    case "show_dialog":
                                        comp = comp.clickEvent(ClickEvent.runCommand("/a1dialog " + parsedLine));
                                        break;
                                }
                            }
                        }

                        if (isTrue(holder, "hover-event")) {
                            String hoverType = holder.getString("hover-type", "");
                            String hoverLine = holder.getString("hover-line", "");

                            // fallback for old properties format in case they used `type` and `line` under hover-event section
                            // but in YAML they might just be root level keys
                            if (hoverType.isEmpty()) hoverType = holder.getString("hover_type", ""); // attempt with underscore

                            if (!hoverType.isEmpty() && !hoverLine.isEmpty()) {
                                if (hoverType.equalsIgnoreCase("show_text")) {
                                    comp = comp.hoverEvent(HoverEvent.showText(A3API.parse(player, hoverLine)));
                                } else if (hoverType.equalsIgnoreCase("show_item")) {
                                    if (hoverLine.equalsIgnoreCase("mainhand")) {
                                        ItemStack item = player.getInventory().getItemInMainHand();
                                        if (item != null && !item.getType().isAir()) {
                                            comp = comp.hoverEvent(item.asHoverEvent());
                                        }
                                    } else if (hoverLine.equalsIgnoreCase("offhand")) {
                                        ItemStack item = player.getInventory().getItemInOffHand();
                                        if (item != null && !item.getType().isAir()) {
                                            comp = comp.hoverEvent(item.asHoverEvent());
                                        }
                                    } else if (hoverLine.startsWith("{") && hoverLine.endsWith("}")) {
                                        try {
                                            String id = "";
                                            int count = 1;

                                            Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
                                            Matcher idMatcher = idPattern.matcher(hoverLine);
                                            if (idMatcher.find()) {
                                                id = idMatcher.group(1);
                                            }

                                            Pattern countPattern = Pattern.compile("\"count\"\\s*:\\s*\"([^\"]+)\"");
                                            Matcher countMatcher = countPattern.matcher(hoverLine);
                                            if (countMatcher.find()) {
                                                count = Integer.parseInt(countMatcher.group(1));
                                            } else {
                                                Pattern countNumPattern = Pattern.compile("\"count\"\\s*:\\s*(\\d+)");
                                                Matcher countNumMatcher = countNumPattern.matcher(hoverLine);
                                                if (countNumMatcher.find()) {
                                                    count = Integer.parseInt(countNumMatcher.group(1));
                                                }
                                            }

                                            if (!id.isEmpty()) {
                                                NamespacedKey nkey = NamespacedKey.fromString(id);
                                                if (nkey != null) {
                                                    HoverEvent.ShowItem showItem = HoverEvent.ShowItem.showItem(net.kyori.adventure.key.Key.key(nkey.namespace(), nkey.value()), count);
                                                    comp = comp.hoverEvent(HoverEvent.showItem(showItem));
                                                }
                                            }
                                        } catch (Exception e) {
                                            if (logger != null) {
                                                logger.warning("Invalid JSON for show_item in textholder " + key);
                                            }
                                        }
                                    } else {
                                        // Plain item name like "iron_sword"
                                        NamespacedKey nkey = NamespacedKey.fromString(hoverLine);
                                        if (nkey == null && !hoverLine.contains(":")) {
                                            nkey = NamespacedKey.minecraft(hoverLine);
                                        }
                                        if (nkey != null) {
                                            HoverEvent.ShowItem showItem = HoverEvent.ShowItem.showItem(net.kyori.adventure.key.Key.key(nkey.namespace(), nkey.value()), 1);
                                            comp = comp.hoverEvent(HoverEvent.showItem(showItem));
                                        }
                                    }
                                }
                            }
                        }

                        return comp;
                    })
                    .build());
        }

        return result;
    }

    private static boolean isTrue(ConfigurationSection section, String path) {
        if (!section.contains(path)) return false;
        if (section.isBoolean(path)) {
            return section.getBoolean(path);
        }
        String val = section.getString(path);
        return val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("on"));
    }

    public static void showDialog(Player player, String dialogId, JsonObject dialogConfig, JsonObject fullConfig) {
        if (dialogConfig == null) return;
        Set<String> visited = new HashSet<>();
        Dialog dialog = buildDialog(player, dialogId, dialogConfig, fullConfig, visited);
        if (dialog != null) {
            player.showDialog(dialog);
        }
    }

    private static DialogBody parseDialogBody(Player player, JsonObject bodyObj) {
        String bodyType = bodyObj.has("type") ? bodyObj.get("type").getAsString() : "plain_message";

        if (bodyType.equals("item")) {
            String itemId = "stone";
            if (bodyObj.has("item") && bodyObj.get("item").isJsonObject()) {
                JsonObject itemObj = bodyObj.getAsJsonObject("item");
                if (itemObj.has("id")) {
                    itemId = itemObj.get("id").getAsString();
                }
            }
            Material mat = Material.matchMaterial(itemId);
            if (mat == null) mat = Material.STONE;
            ItemStack itemStack = new ItemStack(mat);

            PlainMessageDialogBody descBody = null;
            if (bodyObj.has("description") && bodyObj.get("description").isJsonObject()) {
                JsonObject descObj = bodyObj.getAsJsonObject("description");
                if (descObj.has("contents")) {
                    descBody = DialogBody.plainMessage(A3API.parse(player, descObj.get("contents").getAsString()));
                }
            }

            return DialogBody.item(itemStack).description(descBody).build();
        } else {
            String bodyText = bodyObj.has("contents") ? bodyObj.get("contents").getAsString() : "";
            return DialogBody.plainMessage(A3API.parse(player, bodyText));
        }
    }

    public static Dialog buildDialog(Player player, String dialogId, JsonObject dialogConfig, JsonObject fullConfig, Set<String> visited) {
        if (dialogConfig == null) return null;
        if (visited.contains(dialogId)) {
            if (player.getServer().getLogger() != null) {
                player.getServer().getLogger().warning("Cyclic reference detected in dialogs.json for dialog: " + dialogId);
            }
            return null;
        }
        visited.add(dialogId);

        String titleStr = dialogConfig.has("title") ? dialogConfig.get("title").getAsString() : "";
        Component titleComponent = A3API.parse(player, titleStr);

        List<DialogBody> bodies = new ArrayList<>();
        if (dialogConfig.has("body")) {
            JsonElement bodyElement = dialogConfig.get("body");
            if (bodyElement.isJsonArray()) {
                for (JsonElement element : bodyElement.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        JsonObject bodyObj = element.getAsJsonObject();
                        bodies.add(parseDialogBody(player, bodyObj));
                    }
                }
            } else if (bodyElement.isJsonObject()) {
                JsonObject bodyObj = bodyElement.getAsJsonObject();
                bodies.add(parseDialogBody(player, bodyObj));
            }
        }

        String type = dialogConfig.has("type") ? dialogConfig.get("type").getAsString().toLowerCase() : "notice";

        DialogType dialogType;
        if (type.equals("confirmation")) {
            String yesLabel = "Yes";
            if (dialogConfig.has("yes") && dialogConfig.get("yes").isJsonObject() && dialogConfig.getAsJsonObject("yes").has("label")) {
                yesLabel = dialogConfig.getAsJsonObject("yes").get("label").getAsString();
            }
            String noLabel = "No";
            if (dialogConfig.has("no") && dialogConfig.get("no").isJsonObject() && dialogConfig.getAsJsonObject("no").has("label")) {
                noLabel = dialogConfig.getAsJsonObject("no").get("label").getAsString();
            }

            ActionButton yesButton = ActionButton.builder(A3API.parse(player, yesLabel)).build();
            ActionButton noButton = ActionButton.builder(A3API.parse(player, noLabel)).build();
            dialogType = DialogType.confirmation(yesButton, noButton);
        } else if (type.equals("multi_action") || type.equals("multiaction")) {
            List<ActionButton> actions = new ArrayList<>();
            if (dialogConfig.has("actions") && dialogConfig.get("actions").isJsonArray()) {
                for (JsonElement element : dialogConfig.getAsJsonArray("actions")) {
                    if (element.isJsonObject()) {
                        JsonObject actionObj = element.getAsJsonObject();
                        String label = actionObj.has("label") ? actionObj.get("label").getAsString() : "Action";
                        actions.add(ActionButton.builder(A3API.parse(player, label)).build());
                    }
                }
            }
            dialogType = DialogType.multiAction(actions).build();
        } else if (type.equals("dialog_list") || type.equals("dialoglist")) {
            List<Dialog> builtDialogs = new ArrayList<>();
            if (dialogConfig.has("dialogs") && dialogConfig.get("dialogs").isJsonArray() && fullConfig != null) {
                for (JsonElement element : dialogConfig.getAsJsonArray("dialogs")) {
                    if (element.isJsonPrimitive()) {
                        String dialogName = element.getAsString();
                        if (fullConfig.has(dialogName) && fullConfig.get(dialogName).isJsonObject()) {
                            Dialog d = buildDialog(player, dialogName, fullConfig.getAsJsonObject(dialogName), fullConfig, new HashSet<>(visited));
                            if (d != null) {
                                builtDialogs.add(d);
                            }
                        }
                    }
                }
            }
            RegistrySet<Dialog> registrySet = RegistrySet.valueSet(RegistryKey.DIALOG, builtDialogs);
            dialogType = DialogType.dialogList(registrySet).build();
        } else {
            String btnLabel = "OK";
            if (dialogConfig.has("button") && dialogConfig.get("button").isJsonObject() && dialogConfig.getAsJsonObject("button").has("label")) {
                btnLabel = dialogConfig.getAsJsonObject("button").get("label").getAsString();
            }
            dialogType = DialogType.notice(ActionButton.builder(A3API.parse(player, btnLabel)).build());
        }

        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(titleComponent)
                .body(bodies)
                .build())
            .type(dialogType)
        );
    }
}
