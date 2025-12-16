package me.bright.BrightsTPA;

import me.bright.BrightsTPA.Commands.CommandsHandler;
import me.bright.BrightsTPA.Commands.HandlerExecutor;
import me.bright.BrightsTPA.Commands.TabComplete;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class BrightsTPA extends JavaPlugin {
    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void onEnable() {
        getLogger().info("Plugin Loaded!");
        reloadAll();
        CommandsHandler handler = new CommandsHandler(new HandlerExecutor(this));
        TabComplete tabComplete = new TabComplete();
        for (String name : getDescription().getCommands().keySet()) {
            PluginCommand cmd = Objects.requireNonNull(getCommand(name),"Commands is missing from plugin.yml");
            cmd.setExecutor(handler);
            cmd.setTabCompleter(tabComplete);
        }
    }
    public void reloadAll() {
        saveDefaultConfig();
        reloadConfig();
        reloadLang();
    }
    public int getRequestTimeout() {
        return getConfig().getInt("request-timeout", 0);
    }
    public int getRequestCooldown() {
        return getConfig().getInt("request-cooldown", 0);
    }
    public int getCommandCooldown() {
        return getConfig().getInt("command-cooldown", 0);
    }
    public int getTpDelay() {
        return getConfig().getInt("tp-delay", 0);
    }
    public boolean getCancelOnMove() {
        return getConfig().getBoolean("cancel-on-move", false);
    }

    public FileConfiguration langConfig;
    public void reloadLang() {
        File langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }
    public String msg(String path, Map<String, String> replacements) {
        String text = langConfig.isList(path) ? String.join("\n", langConfig.getStringList(path)) : langConfig.getString(path, "Unknown message");
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }
}
