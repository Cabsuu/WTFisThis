package org.jerae.a1;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jerae.a2.A2API;
import org.jerae.a3.A3API;

public final class A1 extends JavaPlugin implements Listener {

    private ConfigManager configManager;
    private DataManager dataManager;
    private AfkManager afkManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        afkManager = new AfkManager(this);

        A1API.init(this);
        A3API.registerCooldownProvider("a1", A1API::getCooldown);

        Commands commands = new Commands(this);
        getCommand("nickname").setExecutor(commands);
        getCommand("a1").setExecutor(commands);
        getCommand("afk").setExecutor(commands);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String nickname = dataManager.getNickname(event.getPlayer().getUniqueId());
        if (nickname != null) {
            boolean hasColor = event.getPlayer().hasPermission("a1.nick.color");
            boolean hasFormat = event.getPlayer().hasPermission("a1.nick.format");
            boolean hasObfuscated = event.getPlayer().hasPermission("a1.nick.obfuscated");
            boolean hasRgb = event.getPlayer().hasPermission("a1.nick.rgb");
            boolean hasGradient = event.getPlayer().hasPermission("a1.nick.gradient");

            event.getPlayer().displayName(A2API.format(nickname, hasColor, hasFormat, hasObfuscated, hasRgb, hasGradient));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkManager.cleanup(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock() || event.hasChangedOrientation()) {
            if (afkManager.isAfk(event.getPlayer())) {
                afkManager.setAfk(event.getPlayer(), false);
                MessageUtil.sendMessage(this, event.getPlayer(), "afk-disabled");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (afkManager.isAfk(event.getPlayer())) {
            afkManager.setAfk(event.getPlayer(), false);
            MessageUtil.sendMessage(this, event.getPlayer(), "afk-disabled");
        }
    }
}
