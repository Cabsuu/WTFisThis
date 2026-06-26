package org.jerae.a3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    public static void registerCooldownProvider(String pluginName, BiFunction<Player, String, Double> provider) {
        cooldownProviders.put(pluginName.toLowerCase(), provider);
    }

    public static Component parse(Player player, String message) {
        String processed = message;

        if (processed.contains("%player_username%")) {
            processed = processed.replace("%player_username%", player.getName());
        }

        if (processed.contains("%a1_version%")) {
            Plugin a1 = Bukkit.getPluginManager().getPlugin("A1");
            String version = a1 != null ? a1.getPluginMeta().getVersion() : "Unknown";
            processed = processed.replace("%a1_version%", version);
        }

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

        // Apply colors globally using A2 API. We assume maximum features allowed here as requested.
        Component parsed = A2API.format(processed, true, true, true, true, true);

        if (message.contains("%player_displayname%")) {
             parsed = parsed.replaceText(TextReplacementConfig.builder()
                .matchLiteral("%player_displayname%")
                .replacement(player.displayName())
                .build());
        }

        return parsed;
    }
}
