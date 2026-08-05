package org.jerae.a3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jerae.a2.A2API;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A3API {

    private static final Map<String, BiFunction<Player, String, Double>> cooldownProviders = new HashMap<>();
    private static final Map<java.util.UUID, String> dialogContext = new HashMap<>();

    public static void registerCooldownProvider(String pluginName, BiFunction<Player, String, Double> provider) {
        cooldownProviders.put(pluginName.toLowerCase(), provider);
    }

    public static void setDialogContext(Player player, String dialogId) {
        if (dialogId == null) {
            dialogContext.remove(player.getUniqueId());
        } else {
            dialogContext.put(player.getUniqueId(), dialogId);
        }
    }

    public static String parseToString(Player player, String message) {
        return parseToString(player, message, null);
    }

    public static String parseToString(Player player, String message, Map<String, String> extraPlaceholders) {
        String processed = message;

        if (extraPlaceholders != null) {
            for (Map.Entry<String, String> entry : extraPlaceholders.entrySet()) {
                if (processed.contains(entry.getKey())) {
                    processed = processed.replace(entry.getKey(), entry.getValue());
                }
            }
        }

        if (processed.contains("%player_username%")) {
            processed = processed.replace("%player_username%", player.getName());
        }

        if (processed.contains("%dialog_id%")) {
            String dialogId = dialogContext.getOrDefault(player.getUniqueId(), "none");
            processed = processed.replace("%dialog_id%", dialogId);
        }

        if (processed.contains("%a1_version%")) {
            Plugin a1 = Bukkit.getPluginManager().getPlugin("A1");
            String version = a1 != null ? a1.getPluginMeta().getVersion() : "Unknown";
            processed = processed.replace("%a1_version%", version);
        }

        Pattern otherUsernamePattern = Pattern.compile("(?i)%otherplayer_username_([a-zA-Z0-9_]+)%");
        Matcher otherUsernameMatcher = otherUsernamePattern.matcher(processed);
        StringBuffer otherSb = new StringBuffer();
        while (otherUsernameMatcher.find()) {
            String pName = otherUsernameMatcher.group(1);
            Player target = Bukkit.getPlayerExact(pName);
            otherUsernameMatcher.appendReplacement(otherSb, target != null ? target.getName() : pName);
        }
        otherUsernameMatcher.appendTail(otherSb);
        processed = otherSb.toString();

        Pattern otherDisplaynamePattern = Pattern.compile("(?i)%otherplayer_displayname_([a-zA-Z0-9_]+)%");
        Matcher otherDisplaynameMatcher = otherDisplaynamePattern.matcher(processed);
        StringBuffer otherDisplaySb = new StringBuffer();
        while (otherDisplaynameMatcher.find()) {
            String pName = otherDisplaynameMatcher.group(1);
            Player target = Bukkit.getPlayerExact(pName);
            otherDisplaynameMatcher.appendReplacement(otherDisplaySb, target != null ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(target.displayName()) : pName);
        }
        otherDisplaynameMatcher.appendTail(otherDisplaySb);
        processed = otherDisplaySb.toString();

        Pattern dialogIdPattern = Pattern.compile("(?i)%dialog_id_([a-zA-Z0-9_-]+)%");
        Matcher dialogIdMatcher = dialogIdPattern.matcher(processed);
        StringBuffer dialogSb = new StringBuffer();
        while (dialogIdMatcher.find()) {
            dialogIdMatcher.appendReplacement(dialogSb, dialogIdMatcher.group(1));
        }
        dialogIdMatcher.appendTail(dialogSb);
        processed = dialogSb.toString();

        // Process cooldown placeholders dynamically: %[plugin].cooldown.[command]%
        Pattern cooldownPattern = Pattern.compile("%([a-zA-Z0-9]+)\\.cooldown\\.([a-zA-Z0-9]+)%");
        Matcher matcher = cooldownPattern.matcher(processed);
        StringBuffer sb = new StringBuffer();

        DecimalFormat df = new DecimalFormat("0.##");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));

        while (matcher.find()) {
            String pluginName = matcher.group(1).toLowerCase();
            String command = matcher.group(2).toLowerCase();

            BiFunction<Player, String, Double> provider = cooldownProviders.get(pluginName);
            double remaining = 0.0;
            if (provider != null) {
                remaining = provider.apply(player, command);
            }
            matcher.appendReplacement(sb, df.format(Math.max(0.0, remaining)));
        }
        matcher.appendTail(sb);
        processed = sb.toString();

        return processed;
    }

    public static Component parse(Player player, String message) {
        return parse(player, message, null);
    }

    public static Component parse(Player player, String message, Map<String, String> extraPlaceholders) {
        String processed = parseToString(player, message, extraPlaceholders);

        // Apply colors globally using A2 API. We assume maximum features allowed here as requested.
        Component parsed = A2API.format(processed, true, true, true, true, true);

        // Item placeholders
        if (message.contains("%item_material%")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            Component matName;
            if (item != null && item.getType().isItem() && !item.getType().isAir()) {
                matName = Component.translatable(item.getType().translationKey());
            } else {
                matName = Component.text("Air");
            }
            parsed = parsed.replaceText(TextReplacementConfig.builder()
                .matchLiteral("%item_material%")
                .replacement(matName)
                .build());
        }

        if (message.contains("%item_displayname%")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            Component itemName;
            if (item != null && item.getType().isItem() && !item.getType().isAir()) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    itemName = item.getItemMeta().displayName();
                } else {
                    itemName = Component.translatable(item.getType().translationKey());
                }
            } else {
                itemName = Component.text("Air");
            }
            if (itemName != null) {
                parsed = parsed.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("%item_displayname%")
                    .replacement(itemName)
                    .build());
            }
        }

        if (message.contains("%player_displayname%")) {
             parsed = parsed.replaceText(TextReplacementConfig.builder()
                .matchLiteral("%player_displayname%")
                .replacement(player.displayName())
                .build());
        }

        parsed = parsed.replaceText(TextReplacementConfig.builder()
            .match("(?i)%otherplayer_displayname_([a-zA-Z0-9_]+)%")
            .replacement((matchResult, builder) -> {
                String pName = matchResult.group(1);
                Player target = Bukkit.getPlayerExact(pName);
                if (target != null) {
                    return target.displayName();
                }
                return Component.text(pName);
            })
            .build());

        return parsed;
    }
}
