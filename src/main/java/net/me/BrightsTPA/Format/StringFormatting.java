package net.me.BrightsTPA.Format;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.me.BrightsTPA.BrightsTPA.LEGACY;

public class StringFormatting {
    public static void send(CommandSender sender, java.lang.String msg, Object... args) {
        msg = java.lang.String.format(msg, args);
        if (sender instanceof Player player) {
            msg = PlaceholderAPI.setPlaceholders(player, msg);
        }
        sender.sendMessage(LEGACY.deserialize(msg));
    }
}
