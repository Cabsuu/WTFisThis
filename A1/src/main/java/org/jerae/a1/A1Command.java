package org.jerae.a1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jerae.a2.A2Api;
import org.jerae.a3.A3Api;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class A1Command implements CommandExecutor {

    private final A1 plugin;

    public A1Command(A1 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("version")) {
            String msg = getMessageConfig().getString("version-message", "&aA1 Plugin Version: %a1_version%");
            Player p = sender instanceof Player ? (Player) sender : null;
            msg = A3Api.setPlaceholders(p, msg);
            sender.sendMessage(A2Api.processConfigMessage(msg));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("a1.reload")) {
                String noPerm = getMessageConfig().getString("no-permission", "&cYou don't have permission to use this command.");
                sender.sendMessage(A2Api.processConfigMessage(noPerm));
                return true;
            }
            plugin.reloadPlugin();
            String msg = getMessageConfig().getString("reload-message", "&aA1 Plugin files reloaded.");
            sender.sendMessage(A2Api.processConfigMessage(msg));
            return true;
        }

        return false;
    }

    private FileConfiguration getMessageConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "message.yml");
        if (messagesFile.exists()) {
            return YamlConfiguration.loadConfiguration(messagesFile);
        }
        return plugin.getConfig(); // fallback
    }
}
