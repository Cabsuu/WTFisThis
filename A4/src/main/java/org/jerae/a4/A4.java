package org.jerae.a4;

import org.bukkit.plugin.java.JavaPlugin;

public class A4 extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("A4 plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("A4 plugin has been disabled!");
    }
}
