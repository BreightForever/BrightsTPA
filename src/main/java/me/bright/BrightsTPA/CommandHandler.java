package me.bright.BrightsTPA;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.bright.BrightsTPA.Format.String.send;

public record CommandHandler(HandleExecutor plugin) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        switch (command.getName().toLowerCase()) {

            case "tpa" -> {
                if (!(sender instanceof Player)) {
                    send(sender, "&cOnly players may use this command!");
                    return true;
                }
                plugin.handleTpaCommand((Player) sender, args);
                return true;
            }

            case "tpahere" -> {
                if (!(sender instanceof Player)) {
                    send(sender, "&cOnly players may use this command!");
                    return true;
                }
                plugin.handleTpahereCommand((Player) sender, args);
                return true;
            }

            case "tpaccept", "tpyes" -> {
                if (!(sender instanceof Player)) {
                    send(sender, "&cOnly players may use this command!");
                    return true;
                }
                plugin.handleTpaAcceptCommand((Player) sender, args);
                return true;
            }

            case "tpdeny", "tpno" -> {
                if (!(sender instanceof Player)) {
                    send(sender, "&cOnly players may use this command!");
                    return true;
                }
                plugin.handleTpaDenyCommand((Player) sender, args);
                return true;
            }

            case "brightstpa" -> {
                plugin.handleReloadCommand((Player) sender);
                return true;
            }

            case "tpacancel" -> {
                if (!(sender instanceof Player)) {
                    send(sender, "&cOnly players may use this command!");
                    return true;
                }
                plugin.handleTpaCancelCommand((Player) sender, args);
                return true;
            }

        }
        return false;
    }
}