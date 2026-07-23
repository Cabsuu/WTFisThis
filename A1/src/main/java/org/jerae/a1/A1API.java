package org.jerae.a1;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.dialog.DialogLike;

public class A1API {

    private static A1 plugin;

    public static void init(A1 a1Plugin) {
        plugin = a1Plugin;
    }

    public static double getCooldown(Player player, String command) {
        if (plugin == null || plugin.getAfkManager() == null) return 0.0;
        return plugin.getAfkManager().getRemainingCooldown(player, command);
    }

    public static DialogLike getDialog(Player player, String dialogId) {
        if (plugin == null || plugin.getDialogManager() == null) return null;
        return plugin.getDialogManager().getParsedDialog(player, dialogId);
    }
}
