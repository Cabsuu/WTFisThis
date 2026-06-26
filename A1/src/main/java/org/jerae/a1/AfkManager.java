package org.jerae.a1;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AfkManager {

    private final Set<UUID> afkPlayers = new HashSet<>();
    private final Map<UUID, Map<String, Long>> commandCooldowns = new HashMap<>();
    private final A1 plugin;

    public AfkManager(A1 plugin) {
        this.plugin = plugin;
    }

    public boolean isAfk(Player player) {
        return afkPlayers.contains(player.getUniqueId());
    }

    public void setAfk(Player player, boolean isAfk) {
        if (isAfk) {
            afkPlayers.add(player.getUniqueId());
        } else {
            afkPlayers.remove(player.getUniqueId());
        }
    }

    public void setCooldown(Player player, String command) {
        int cooldownSeconds = plugin.getConfigManager().getConfig().getInt(command + "-cooldown", 3);
        commandCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(command.toLowerCase(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    public double getRemainingCooldown(Player player, String command) {
        Map<String, Long> cooldowns = commandCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return 0.0;

        Long expiry = cooldowns.get(command.toLowerCase());
        if (expiry == null) return 0.0;

        long remainingMillis = expiry - System.currentTimeMillis();
        return remainingMillis > 0 ? remainingMillis / 1000.0 : 0.0;
    }

    public void cleanup(Player player) {
        afkPlayers.remove(player.getUniqueId());
        commandCooldowns.remove(player.getUniqueId());
    }
}
