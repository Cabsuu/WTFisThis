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
                                        String namespace = "minecraft";
                                        String id = parsedLine;
                                        if (parsedLine.contains(":")) {
                                            String[] parts = parsedLine.split(":", 2);
                                            namespace = parts[0];
                                            id = parts[1];
                                        }
                                        final String finalNamespace = namespace;
                                        final String finalId = id;

                                        class PaperDialog implements io.papermc.paper.dialog.Dialog {
                                            private final org.bukkit.NamespacedKey key;
                                            public PaperDialog(org.bukkit.NamespacedKey k) { this.key = k; }
                                            @Override
                                            public org.bukkit.NamespacedKey getKey() { return key; }
                                            @Override
                                            public net.kyori.adventure.key.Key key() { return key; }
                                            @Override
                                            public String toString() { return key.asString(); }
                                        }

                                        comp = comp.clickEvent(ClickEvent.showDialog(new PaperDialog(new org.bukkit.NamespacedKey(finalNamespace, finalId))));
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
}
