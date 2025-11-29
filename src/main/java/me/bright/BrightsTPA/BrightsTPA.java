package me.bright.BrightsTPA;

import org.bukkit.plugin.java.JavaPlugin;

public final class BrightsTPA extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin Loaded!ssss");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Unloaded!");
    }
}
