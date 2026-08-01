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
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;

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
            if (bodyObj.has("description")) {
                if (bodyObj.get("description").isJsonObject()) {
                    JsonObject descObj = bodyObj.getAsJsonObject("description");
                    Component descContents = Component.empty();
                    if (descObj.has("contents")) {
                        descContents = A3API.parse(player, descObj.get("contents").getAsString());
                    }
                    if (descObj.has("width") && descObj.get("width").isJsonPrimitive()) {
                        descBody = DialogBody.plainMessage(descContents, descObj.get("width").getAsInt());
                    } else {
                        descBody = DialogBody.plainMessage(descContents);
                    }
                } else if (bodyObj.get("description").isJsonPrimitive()) {
                    descBody = DialogBody.plainMessage(A3API.parse(player, bodyObj.get("description").getAsString()));
                }
            } else if (bodyObj.has("contents")) {
                if (bodyObj.get("contents").isJsonObject()) {
                    JsonObject contentsObj = bodyObj.getAsJsonObject("contents");
                    Component contentsCmp = Component.empty();
                    if (contentsObj.has("contents")) contentsCmp = A3API.parse(player, contentsObj.get("contents").getAsString());
                    else if (contentsObj.has("text")) contentsCmp = A3API.parse(player, contentsObj.get("text").getAsString());

                    if (contentsObj.has("width") && contentsObj.get("width").isJsonPrimitive()) {
                        descBody = DialogBody.plainMessage(contentsCmp, contentsObj.get("width").getAsInt());
                    } else {
                        descBody = DialogBody.plainMessage(contentsCmp);
                    }
                } else if (bodyObj.get("contents").isJsonPrimitive()) {
                    descBody = DialogBody.plainMessage(A3API.parse(player, bodyObj.get("contents").getAsString()));
                }
            }

            ItemDialogBody.Builder itemBuilder = DialogBody.item(itemStack).description(descBody);
            if (bodyObj.has("width") && bodyObj.get("width").isJsonPrimitive()) {
                itemBuilder = itemBuilder.width(bodyObj.get("width").getAsInt());
            }
            if (bodyObj.has("height") && bodyObj.get("height").isJsonPrimitive()) {
                itemBuilder = itemBuilder.height(bodyObj.get("height").getAsInt());
            }
            if (bodyObj.has("show_decorations") && bodyObj.get("show_decorations").isJsonPrimitive()) {
                itemBuilder = itemBuilder.showDecorations(bodyObj.get("show_decorations").getAsBoolean());
            }
            if (bodyObj.has("show_tooltip") && bodyObj.get("show_tooltip").isJsonPrimitive()) {
                itemBuilder = itemBuilder.showTooltip(bodyObj.get("show_tooltip").getAsBoolean());
            }

            return itemBuilder.build();
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
                    } else if (element.isJsonPrimitive()) {
                        bodies.add(DialogBody.plainMessage(A3API.parse(player, element.getAsString())));
                    }
                }
            } else if (bodyElement.isJsonObject()) {
                JsonObject bodyObj = bodyElement.getAsJsonObject();
                bodies.add(parseDialogBody(player, bodyObj));
            } else if (bodyElement.isJsonPrimitive()) {
                bodies.add(DialogBody.plainMessage(A3API.parse(player, bodyElement.getAsString())));
            }
        }

        String type = dialogConfig.has("type") ? dialogConfig.get("type").getAsString().toLowerCase() : "notice";

        DialogType dialogType;
        if (type.equals("confirmation")) {
            ActionButton.Builder yesBuilder = ActionButton.builder(A3API.parse(player, "Yes"));
            if (dialogConfig.has("yes") && dialogConfig.get("yes").isJsonObject()) {
                JsonObject yesObj = dialogConfig.getAsJsonObject("yes");
                if (yesObj.has("label")) yesBuilder = ActionButton.builder(A3API.parse(player, yesObj.get("label").getAsString()));
                if (yesObj.has("width") && yesObj.get("width").isJsonPrimitive()) yesBuilder.width(yesObj.get("width").getAsInt());
                if (yesObj.has("tooltip") && yesObj.get("tooltip").isJsonPrimitive()) yesBuilder.tooltip(A3API.parse(player, yesObj.get("tooltip").getAsString()));
            }

            ActionButton.Builder noBuilder = ActionButton.builder(A3API.parse(player, "No"));
            if (dialogConfig.has("no") && dialogConfig.get("no").isJsonObject()) {
                JsonObject noObj = dialogConfig.getAsJsonObject("no");
                if (noObj.has("label")) noBuilder = ActionButton.builder(A3API.parse(player, noObj.get("label").getAsString()));
                if (noObj.has("width") && noObj.get("width").isJsonPrimitive()) noBuilder.width(noObj.get("width").getAsInt());
                if (noObj.has("tooltip") && noObj.get("tooltip").isJsonPrimitive()) noBuilder.tooltip(A3API.parse(player, noObj.get("tooltip").getAsString()));
            }

            dialogType = DialogType.confirmation(yesBuilder.build(), noBuilder.build());
        } else if (type.equals("multi_action") || type.equals("multiaction")) {
            List<ActionButton> actions = new ArrayList<>();
            if (dialogConfig.has("actions") && dialogConfig.get("actions").isJsonArray()) {
                for (JsonElement element : dialogConfig.getAsJsonArray("actions")) {
                    if (element.isJsonObject()) {
                        JsonObject actionObj = element.getAsJsonObject();
                        String label = actionObj.has("label") ? actionObj.get("label").getAsString() : "Action";
                        ActionButton.Builder actBuilder = ActionButton.builder(A3API.parse(player, label));
                        if (actionObj.has("width") && actionObj.get("width").isJsonPrimitive()) actBuilder.width(actionObj.get("width").getAsInt());
                        if (actionObj.has("tooltip") && actionObj.get("tooltip").isJsonPrimitive()) actBuilder.tooltip(A3API.parse(player, actionObj.get("tooltip").getAsString()));
                        actions.add(actBuilder.build());
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
            ActionButton.Builder btnBuilder = ActionButton.builder(A3API.parse(player, "OK"));
            if (dialogConfig.has("button") && dialogConfig.get("button").isJsonObject()) {
                JsonObject btnObj = dialogConfig.getAsJsonObject("button");
                if (btnObj.has("label")) btnBuilder = ActionButton.builder(A3API.parse(player, btnObj.get("label").getAsString()));
                if (btnObj.has("width") && btnObj.get("width").isJsonPrimitive()) btnBuilder.width(btnObj.get("width").getAsInt());
                if (btnObj.has("tooltip") && btnObj.get("tooltip").isJsonPrimitive()) btnBuilder.tooltip(A3API.parse(player, btnObj.get("tooltip").getAsString()));
            }
            dialogType = DialogType.notice(btnBuilder.build());
        }

        DialogBase.Builder baseBuilder = DialogBase.builder(titleComponent).body(bodies);

        if (dialogConfig.has("external_title")) {
            baseBuilder.externalTitle(A3API.parse(player, dialogConfig.get("external_title").getAsString()));
        }

        if (dialogConfig.has("can_close_with_escape")) {
            baseBuilder.canCloseWithEscape(dialogConfig.get("can_close_with_escape").getAsBoolean());
        }

        if (dialogConfig.has("pause")) {
            baseBuilder.pause(dialogConfig.get("pause").getAsBoolean());
        }

        if (dialogConfig.has("after_action")) {
            String afterAction = dialogConfig.get("after_action").getAsString().toLowerCase();
            if (afterAction.equals("close")) {
                baseBuilder.afterAction(DialogBase.DialogAfterAction.CLOSE);
            } else if (afterAction.equals("none")) {
                baseBuilder.afterAction(DialogBase.DialogAfterAction.NONE);
            } else if (afterAction.equals("wait_for_response")) {
                baseBuilder.afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE);
            }
        }

        List<DialogInput> inputs = new ArrayList<>();
        if (dialogConfig.has("inputs") && dialogConfig.get("inputs").isJsonArray()) {
            for (JsonElement element : dialogConfig.getAsJsonArray("inputs")) {
                if (element.isJsonObject()) {
                    JsonObject inputObj = element.getAsJsonObject();
                    String inputKey = inputObj.has("key") ? inputObj.get("key").getAsString() : "key";
                    Component inputLabel = inputObj.has("label") ? A3API.parse(player, inputObj.get("label").getAsString()) : Component.empty();
                    String inputType = inputObj.has("type") ? inputObj.get("type").getAsString() : "text";

                    if (inputType.equals("bool")) {
                        var boolBuilder = DialogInput.bool(inputKey, inputLabel);
                        if (inputObj.has("initial") && inputObj.get("initial").isJsonPrimitive()) boolBuilder.initial(inputObj.get("initial").getAsBoolean());
                        if (inputObj.has("on_true") && inputObj.get("on_true").isJsonPrimitive()) boolBuilder.onTrue(inputObj.get("on_true").getAsString());
                        if (inputObj.has("on_false") && inputObj.get("on_false").isJsonPrimitive()) boolBuilder.onFalse(inputObj.get("on_false").getAsString());
                        inputs.add(boolBuilder.build());
                    } else if (inputType.equals("number_range")) {
                        float start = inputObj.has("start") ? inputObj.get("start").getAsFloat() : 0f;
                        float end = inputObj.has("end") ? inputObj.get("end").getAsFloat() : 100f;
                        var numBuilder = DialogInput.numberRange(inputKey, inputLabel, start, end);
                        if (inputObj.has("step") && inputObj.get("step").isJsonPrimitive()) numBuilder.step(inputObj.get("step").getAsFloat());
                        if (inputObj.has("width") && inputObj.get("width").isJsonPrimitive()) numBuilder.width(inputObj.get("width").getAsInt());
                        if (inputObj.has("initial") && inputObj.get("initial").isJsonPrimitive()) numBuilder.initial(inputObj.get("initial").getAsFloat());
                        if (inputObj.has("label_format") && inputObj.get("label_format").isJsonPrimitive()) numBuilder.labelFormat(inputObj.get("label_format").getAsString());
                        inputs.add(numBuilder.build());
                    } else if (inputType.equals("single_option")) {
                        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
                        if (inputObj.has("entries") && inputObj.get("entries").isJsonArray()) {
                            for (JsonElement entryElem : inputObj.getAsJsonArray("entries")) {
                                if (entryElem.isJsonObject()) {
                                    JsonObject entryObj = entryElem.getAsJsonObject();
                                    String val = entryObj.has("value") ? entryObj.get("value").getAsString() : "";
                                    Component lbl = entryObj.has("label") ? A3API.parse(player, entryObj.get("label").getAsString()) : Component.text(val);
                                    boolean initial = entryObj.has("initial") && entryObj.get("initial").getAsBoolean();
                                    entries.add(SingleOptionDialogInput.OptionEntry.create(val, lbl, initial));
                                }
                            }
                        }
                        var singleOptBuilder = DialogInput.singleOption(inputKey, inputLabel, entries);
                        if (inputObj.has("width") && inputObj.get("width").isJsonPrimitive()) singleOptBuilder.width(inputObj.get("width").getAsInt());
                        if (inputObj.has("label_visible") && inputObj.get("label_visible").isJsonPrimitive()) singleOptBuilder.labelVisible(inputObj.get("label_visible").getAsBoolean());
                        inputs.add(singleOptBuilder.build());
                    } else if (inputType.equals("text")) {
                        var textBuilder = DialogInput.text(inputKey, inputLabel);
                        if (inputObj.has("width") && inputObj.get("width").isJsonPrimitive()) textBuilder.width(inputObj.get("width").getAsInt());
                        if (inputObj.has("max_length") && inputObj.get("max_length").isJsonPrimitive()) textBuilder.maxLength(inputObj.get("max_length").getAsInt());
                        if (inputObj.has("initial") && inputObj.get("initial").isJsonPrimitive()) textBuilder.initial(inputObj.get("initial").getAsString());
                        if (inputObj.has("label_visible") && inputObj.get("label_visible").isJsonPrimitive()) textBuilder.labelVisible(inputObj.get("label_visible").getAsBoolean());
                        inputs.add(textBuilder.build());
                    }
                }
            }
        }
        if (!inputs.isEmpty()) {
            baseBuilder.inputs(inputs);
        }

        return Dialog.create(builder -> builder.empty()
            .base(baseBuilder.build())
            .type(dialogType)
        );
    }
}
