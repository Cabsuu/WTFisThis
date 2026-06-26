package org.jerae.a1;

import org.bukkit.plugin.java.JavaPlugin;

public final class A1 extends JavaPlugin {

    private NickDataManager nickDataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("message.yml", false);

        nickDataManager = new NickDataManager(getDataFolder());

        getCommand("a1").setExecutor(new A1Command(this));
        getCommand("nickname").setExecutor(new NickCommand(this));

        getServer().getPluginManager().registerEvents(new A1PlayerListener(this), this);

        getLogger().info("A1 enabled.");
    }

    @Override
    public void onDisable() {
        if (nickDataManager != null) {
            nickDataManager.saveData();
        }
        getLogger().info("A1 disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        nickDataManager.loadData();
    }

    public NickDataManager getNickDataManager() {
        return nickDataManager;
    }
}
