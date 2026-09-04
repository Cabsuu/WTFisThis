package org.jerae.a5;

import org.bukkit.plugin.java.JavaPlugin;

public class A5 extends JavaPlugin {

    private TpaManager tpaManager;

    @Override
    public void onEnable() {
        getLogger().info("A5 plugin has been enabled!");
        tpaManager = new TpaManager(this);

        TeleportCommands commands = new TeleportCommands(this);
        getCommand("tpa").setExecutor(commands);
        getCommand("tpahere").setExecutor(commands);
        getCommand("tpyes").setExecutor(commands);
        getCommand("tpno").setExecutor(commands);
        getCommand("tpaall").setExecutor(commands);
    }

    @Override
    public void onDisable() {
        getLogger().info("A5 plugin has been disabled!");
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }
}
