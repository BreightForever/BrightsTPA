package net.me.BrightsTPA.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, Command command, @NonNull String alias, String @NonNull [] args) {
        if (command.getName().equalsIgnoreCase("brightstpa")) {
            if (args.length == 1) {
                return List.of("reload", "version");
            }
        }
        return null;
    }
}
