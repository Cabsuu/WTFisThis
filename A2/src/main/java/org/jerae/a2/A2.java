package org.jerae.a2;

import org.bukkit.plugin.java.JavaPlugin;

public final class A2 extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new A2ChatListener(), this);
        getLogger().info("A2 Chat Listener enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
