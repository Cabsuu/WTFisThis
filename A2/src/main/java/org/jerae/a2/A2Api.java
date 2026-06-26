package org.jerae.a2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A2Api {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<&x([0-9a-fA-F]{6}):&[x]?([0-9a-fA-F]{1,6})>(.*?)(?=<&x|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RGB_PATTERN = Pattern.compile("&x([0-9a-fA-F]{6})", Pattern.CASE_INSENSITIVE);

    // Provide a strict MiniMessage instance to prevent abuse (like <click> tags) from user input
    private static final MiniMessage strictMiniMessage = MiniMessage.builder()
            .tags(TagResolver.standard()) // Allows standard tags but we will escape the input first
            .build();

    public static Component processColors(String text, CommandSender sender, String permPrefix) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String processed = text;

        // Handle gradients
        if (sender.hasPermission(permPrefix + ".gradient")) {
            Matcher gradientMatcher = GRADIENT_PATTERN.matcher(processed);
            StringBuffer sb = new StringBuffer();
            while (gradientMatcher.find()) {
                String color1 = gradientMatcher.group(1);
                String color2 = gradientMatcher.group(2);
                if (color2.length() == 1) { // Like &b
                    color2 = getHexFromLegacy(color2.charAt(0));
                } else if (color2.length() != 6) {
                    color2 = "FFFFFF"; // fallback
                }
                String content = gradientMatcher.group(3);

                String replacement = "<gradient:#" + color1 + ":#" + color2 + ">" + content + "</gradient>";
                gradientMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            gradientMatcher.appendTail(sb);
            processed = sb.toString();
        } else {
            // Strip out gradient tags if no permission
            processed = processed.replaceAll("<&x([0-9a-fA-F]{6}):&[x]?([0-9a-fA-F]{1,6})>", "");
        }

        // Handle RGB
        if (sender.hasPermission(permPrefix + ".rgb")) {
            Matcher rgbMatcher = RGB_PATTERN.matcher(processed);
            StringBuffer sb = new StringBuffer();
            while (rgbMatcher.find()) {
                String hex = rgbMatcher.group(1);
                String replacement = "<#" + hex + ">";
                rgbMatcher.appendReplacement(sb, replacement);
            }
            rgbMatcher.appendTail(sb);
            processed = sb.toString();
        } else {
            // Strip out RGB tags if no permission
            processed = processed.replaceAll("&x[0-9a-fA-F]{6}", "");
        }

        // Escape any stray < and > to prevent MiniMessage abuse if they aren't part of our processed tags
        // Actually, a safer way is to only deserialize exactly what we want.
        // For simplicity and fixing the vulnerability, we can escape text first, then process tags.
        // Let's re-write the processing to be safer:

        // 1. Escape all existing MiniMessage tags in the raw text so users can't use <click> etc.
        // A simple way to escape is to replace < with \< and > with \>
        // But we already added our own <gradient> and <#> tags above.
        // It's better to escape first, THEN apply our parsing.

        return legacyProcess(text, sender, permPrefix);
    }

    public static Component processColors(String text, CommandSender sender) {
        return processColors(text, sender, "a2.chat");
    }

    private static Component legacyProcess(String text, CommandSender sender, String permPrefix) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        boolean hasColor = sender.hasPermission(permPrefix + ".color");
        boolean hasFormat = sender.hasPermission(permPrefix + ".format");
        boolean hasObfuscated = sender.hasPermission(permPrefix + ".obfuscated");
        boolean hasRgb = sender.hasPermission(permPrefix + ".rgb");
        boolean hasGradient = sender.hasPermission(permPrefix + ".gradient");

        // Escape existing Minimessage tags completely
        String safeText = text.replace("<", "\\<").replace(">", "\\>");

        String processed = safeText;

        // Re-implement gradient on safe text
        if (hasGradient) {
            // Since we escaped < and >, our pattern matching needs to look for \< and \>
            Pattern safeGradientPattern = Pattern.compile("\\\\<&x([0-9a-fA-F]{6}):&[x]?([0-9a-fA-F]{1,6})\\\\>(.*?)(?=\\\\&x|$)", Pattern.CASE_INSENSITIVE);
            Matcher gradientMatcher = safeGradientPattern.matcher(processed);
            StringBuffer sb = new StringBuffer();
            while (gradientMatcher.find()) {
                String color1 = gradientMatcher.group(1);
                String color2 = gradientMatcher.group(2);
                if (color2.length() == 1) {
                    color2 = getHexFromLegacy(color2.charAt(0));
                } else if (color2.length() != 6) {
                    color2 = "FFFFFF";
                }
                String content = gradientMatcher.group(3);
                String replacement = "<gradient:#" + color1 + ":#" + color2 + ">" + content + "</gradient>";
                gradientMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            gradientMatcher.appendTail(sb);
            processed = sb.toString();
        } else {
             processed = processed.replaceAll("\\\\<&x([0-9a-fA-F]{6}):&[x]?([0-9a-fA-F]{1,6})\\\\>", "");
        }

        if (hasRgb) {
            Matcher rgbMatcher = RGB_PATTERN.matcher(processed);
            StringBuffer sb = new StringBuffer();
            while (rgbMatcher.find()) {
                String hex = rgbMatcher.group(1);
                String replacement = "<#" + hex + ">";
                rgbMatcher.appendReplacement(sb, replacement);
            }
            rgbMatcher.appendTail(sb);
            processed = sb.toString();
        } else {
            processed = processed.replaceAll("&x[0-9a-fA-F]{6}", "");
        }

        // Convert standard legacy ampersand codes to MiniMessage tags
        char[] chars = processed.toCharArray();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);
                if (isColor(code)) {
                    if (hasColor) {
                        sb.append(getMiniMessageColor(code));
                    }
                    i++; // skip code char
                    continue;
                } else if (isFormat(code)) {
                    if (code == 'k') {
                        if (hasObfuscated) {
                            sb.append("<obf>");
                        }
                    } else if (hasFormat) {
                        sb.append(getMiniMessageFormat(code));
                    }
                    i++;
                    continue;
                }
            }
            sb.append(chars[i]);
        }

        return MiniMessage.miniMessage().deserialize(sb.toString());
    }

    // For config files and messages that don't need strict escaping
    public static Component processConfigMessage(String text) {
        if (text == null) return Component.empty();
        String processed = text;

        Matcher rgbMatcher = RGB_PATTERN.matcher(processed);
        StringBuffer sb = new StringBuffer();
        while (rgbMatcher.find()) {
            String hex = rgbMatcher.group(1);
            String replacement = "<#" + hex + ">";
            rgbMatcher.appendReplacement(sb, replacement);
        }
        rgbMatcher.appendTail(sb);
        processed = sb.toString();

        char[] chars = processed.toCharArray();
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);
                if (isColor(code)) {
                    sb2.append(getMiniMessageColor(code));
                    i++;
                    continue;
                } else if (isFormat(code)) {
                    if (code == 'k') {
                        sb2.append("<obf>");
                    } else {
                        sb2.append(getMiniMessageFormat(code));
                    }
                    i++;
                    continue;
                }
            }
            sb2.append(chars[i]);
        }

        return MiniMessage.miniMessage().deserialize(sb2.toString());
    }

    private static boolean isColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    private static boolean isFormat(char c) {
        return (c >= 'k' && c <= 'o') || c == 'r';
    }

    private static String getMiniMessageColor(char c) {
        switch (c) {
            case '0': return "<black>";
            case '1': return "<dark_blue>";
            case '2': return "<dark_green>";
            case '3': return "<dark_aqua>";
            case '4': return "<dark_red>";
            case '5': return "<dark_purple>";
            case '6': return "<gold>";
            case '7': return "<gray>";
            case '8': return "<dark_gray>";
            case '9': return "<blue>";
            case 'a': return "<green>";
            case 'b': return "<aqua>";
            case 'c': return "<red>";
            case 'd': return "<light_purple>";
            case 'e': return "<yellow>";
            case 'f': return "<white>";
            default: return "";
        }
    }

    private static String getMiniMessageFormat(char c) {
        switch (c) {
            case 'k': return "<obf>";
            case 'l': return "<bold>";
            case 'm': return "<st>";
            case 'n': return "<underlined>";
            case 'o': return "<italic>";
            case 'r': return "<reset>";
            default: return "";
        }
    }

    private static String getHexFromLegacy(char c) {
        switch (Character.toLowerCase(c)) {
            case '0': return "000000";
            case '1': return "0000AA";
            case '2': return "00AA00";
            case '3': return "00AAAA";
            case '4': return "AA0000";
            case '5': return "AA00AA";
            case '6': return "FFAA00";
            case '7': return "AAAAAA";
            case '8': return "555555";
            case '9': return "5555FF";
            case 'a': return "55FF55";
            case 'b': return "55FFFF";
            case 'c': return "FF5555";
            case 'd': return "FF55FF";
            case 'e': return "FFFF55";
            case 'f': return "FFFFFF";
            default: return "FFFFFF";
        }
    }
}
