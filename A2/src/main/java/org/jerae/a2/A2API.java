package org.jerae.a2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A2API {

    private static final MiniMessage MINIMESSAGE = MiniMessage.miniMessage();

    public static Component format(String message, boolean hasColor, boolean hasFormat, boolean hasObfuscated, boolean hasRgb, boolean hasGradient) {
        // Escape '<' to prevent native MiniMessage tag injection
        String processed = message.replace("<", "\\<");

        if (hasGradient) {
            Pattern gradientPattern = Pattern.compile("(?i)\\\\<(&x[0-9a-f]{6}|&[0-9a-f]):(&x[0-9a-f]{6}|&[0-9a-f])>");
            Matcher m = gradientPattern.matcher(processed);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String c1 = resolveColor(m.group(1));
                String c2 = resolveColor(m.group(2));
                m.appendReplacement(sb, "<gradient:" + c1 + ":" + c2 + ">");
            }
            m.appendTail(sb);
            processed = sb.toString();
        }

        if (hasRgb) {
            Pattern rgbPattern = Pattern.compile("(?i)&x([0-9a-f]{6})");
            Matcher m = rgbPattern.matcher(processed);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                m.appendReplacement(sb, "<#" + m.group(1) + ">");
            }
            m.appendTail(sb);
            processed = sb.toString();
        }

        if (hasColor) {
            processed = processed.replaceAll("(?i)&0", "<black>");
            processed = processed.replaceAll("(?i)&1", "<dark_blue>");
            processed = processed.replaceAll("(?i)&2", "<dark_green>");
            processed = processed.replaceAll("(?i)&3", "<dark_aqua>");
            processed = processed.replaceAll("(?i)&4", "<dark_red>");
            processed = processed.replaceAll("(?i)&5", "<dark_purple>");
            processed = processed.replaceAll("(?i)&6", "<gold>");
            processed = processed.replaceAll("(?i)&7", "<gray>");
            processed = processed.replaceAll("(?i)&8", "<dark_gray>");
            processed = processed.replaceAll("(?i)&9", "<blue>");
            processed = processed.replaceAll("(?i)&a", "<green>");
            processed = processed.replaceAll("(?i)&b", "<aqua>");
            processed = processed.replaceAll("(?i)&c", "<red>");
            processed = processed.replaceAll("(?i)&d", "<light_purple>");
            processed = processed.replaceAll("(?i)&e", "<yellow>");
            processed = processed.replaceAll("(?i)&f", "<white>");
        }

        if (hasFormat) {
            processed = processed.replaceAll("(?i)&l", "<bold>");
            processed = processed.replaceAll("(?i)&m", "<strikethrough>");
            processed = processed.replaceAll("(?i)&n", "<underlined>");
            processed = processed.replaceAll("(?i)&o", "<italic>");
            processed = processed.replaceAll("(?i)&r", "<reset>");
        }

        if (hasObfuscated) {
            processed = processed.replaceAll("(?i)&k", "<obf>");
        }

        return MINIMESSAGE.deserialize(processed);
    }

    private static String resolveColor(String token) {
        String lowerToken = token.toLowerCase();
        if (lowerToken.startsWith("&x")) {
            return "#" + lowerToken.substring(2);
        }
        switch (lowerToken.charAt(1)) {
            case '0': return "black";
            case '1': return "dark_blue";
            case '2': return "dark_green";
            case '3': return "dark_aqua";
            case '4': return "dark_red";
            case '5': return "dark_purple";
            case '6': return "gold";
            case '7': return "gray";
            case '8': return "dark_gray";
            case '9': return "blue";
            case 'a': return "green";
            case 'b': return "aqua";
            case 'c': return "red";
            case 'd': return "light_purple";
            case 'e': return "yellow";
            case 'f': return "white";
        }
        return "white";
    }
}
