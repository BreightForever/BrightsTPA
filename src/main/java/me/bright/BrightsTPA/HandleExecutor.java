package me.bright.BrightsTPA;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.bright.BrightsTPA.BrightsTPA.LEGACY;

public record HandleExecutor(BrightsTPA plugin) implements me.bright.BrightsTPA.Format.String {

    static HashMap<UUID, UUID> tpaMap = new HashMap<>();
    static HashMap<UUID, UUID> tpahereMap = new HashMap<>();
    static HashMap<UUID, Long> commandCooldownsMap = new HashMap<>();
    static HashMap<UUID, Long> requestCooldownsMap = new HashMap<>();

    @Override
    public void send(CommandSender sender, String msg, Object... args) {
        sender.sendMessage(LEGACY.deserialize(String.format(msg, args)));
    }

    private boolean getCooldown(Player player, long cooldownSeconds, String command) {
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000L;
        long lastUse = commandCooldownsMap.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastUse < cooldownMillis) {
            long remaining = (cooldownMillis - (currentTime - lastUse)) / 1000;
            send(player,"&cYou must wait &6%s &csecond(s) before using &6/%s &cagain.", remaining, command);
            return true;
        }
        commandCooldownsMap.put(player.getUniqueId(),currentTime);
        return false;
    }

    public void handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("brightstpa.reload")) {
            send(sender,"&cYou do not have permission to use this command.");
            return;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            send(sender,"&cUsage: /brightstpa reload");
            return;
        }

        try {
            plugin.reloadConfig();
            plugin.loadSettings();
            send(sender,"&aConfiguration reloaded successfully!");
        }
        catch (Exception e) {
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            send(sender,"&cFailed to reload config! Check console for details.");
        }
    }

    public void handleTpaDenyCommand(CommandSender sender) {
        final Player requestPlayer = (Player) sender;

        if (getCooldown(requestPlayer, plugin.getCommandCooldown(), "tpa")) {
            return;
        }
        
        if (!requestPlayer.hasPermission("brightstpa.deny")) {
            send(requestPlayer,"&cYou do not have permission to use this command!");
            return;
        }

        if (!tpaMap.containsValue(requestPlayer.getUniqueId())) {
            send(requestPlayer,"&6You don't have any pending requests!");
            return;
        }

        for (Map.Entry<UUID, UUID> entry : tpaMap.entrySet()) {
            if (entry.getValue().equals(requestPlayer.getUniqueId())) {
                tpaMap.remove(entry.getKey());

                Player originalSender = Bukkit.getPlayer(entry.getKey());
                if (originalSender != null) {
                    send(originalSender,"&6Your TPA request was denied!");
                }

                send(requestPlayer,"&6Denied TPA request.");
                break;
            }
        }
    }

    public void handleTpaAcceptCommand(CommandSender sender) {
        final Player requestPlayer = (Player) sender;

        if (getCooldown(requestPlayer, plugin.getCommandCooldown(), "tpaccept")) {
            return;
        }

        if (!requestPlayer.hasPermission("brightstpa.accept")) {
            send(requestPlayer, "&cYou do not have permission to use this command!");
            return;
        }

        UUID receivePlayerUUID1 = tpaMap.entrySet().stream().filter(entry -> entry.getValue().equals(requestPlayer.getUniqueId())).findFirst().map(Map.Entry::getKey).orElse(null);
        UUID receivePlayerUUID2 = tpahereMap.entrySet().stream().filter(entry -> entry.getValue().equals(requestPlayer.getUniqueId())).findFirst().map(Map.Entry::getKey).orElse(null);

        if (receivePlayerUUID1 == null && receivePlayerUUID2 == null) {
            send(requestPlayer, "&6You don't have any pending requests!");
        } else if (receivePlayerUUID1 != null) {
            Player receivePlayer1 = Bukkit.getPlayer(receivePlayerUUID1);

            if (receivePlayer1 == null || !receivePlayer1.isOnline()) {
                tpaMap.remove(receivePlayerUUID1);
                send(requestPlayer, "&cThe player %s is no longer online.", receivePlayer1);
            }

            final int delaySeconds = plugin.getTpDelay();
            final long delayTicks = delaySeconds * 20L;

            assert receivePlayer1 != null;
            send(requestPlayer, "&6TPA request accepted! &a%s will teleport to you in &c%s seconds.", receivePlayer1.getName(), delaySeconds);
            send(receivePlayer1, "&6Request accepted! Teleporting in &c%s seconds.", delaySeconds);

            new BukkitRunnable() {
                @Override
                public void run() {

                    if (receivePlayer1.isOnline() && requestPlayer.isOnline()) {
                        receivePlayer1.teleport(requestPlayer.getLocation());
                        tpaMap.remove(receivePlayerUUID1);
                        send(receivePlayer1, "&aTeleported successfully!");
                        send(requestPlayer, "&a%s has arrived.", receivePlayer1.getName());
                    } else {
                        send(receivePlayer1, "&cTeleport cancelled: player went offline.");
                    }
                }
            }.runTaskLater(plugin, delayTicks);
        } else {
            Player receivePlayer2 = Bukkit.getPlayer(receivePlayerUUID2);

            if (receivePlayer2 == null || !receivePlayer2.isOnline()) {
                tpahereMap.remove(receivePlayerUUID2);
                send(requestPlayer, "&cThe player %s is no longer online.", receivePlayer2);
            }

            final int delaySeconds = plugin.getTpDelay();
            final long delayTicks = delaySeconds * 20L;

            assert receivePlayer2 != null;
            send(receivePlayer2, "&6TPA request accepted! &a%s will teleport to you in &c%s seconds.", requestPlayer.getName(), delaySeconds);
            send(requestPlayer, "&6Request accepted! Teleporting in &c%s seconds.", delaySeconds);

            new BukkitRunnable() {
                @Override
                public void run() {

                    if (receivePlayer2.isOnline() && requestPlayer.isOnline()) {
                        requestPlayer.teleport(receivePlayer2.getLocation());
                        tpahereMap.remove(receivePlayerUUID2);
                        send(requestPlayer, "&aTeleported successfully!");
                        send(receivePlayer2, "&a%s has arrived.", requestPlayer.getName());
                    } else {
                        send(requestPlayer, "&cTeleport cancelled: player went offline.");
                    }
                }
            }.runTaskLater(plugin, delayTicks);
        }
    }

    public void handleTpaCommand(CommandSender sender, String[] args) {
        final Player requestPlayer = (Player) sender;
        final Player receivePlayer = Bukkit.getPlayer(args[0]);

        if (getCooldown(requestPlayer, plugin.getCommandCooldown(), "tpa")) {
            return;
        }
        
        if (!requestPlayer.hasPermission("brightstpa.tpa")) {
            send(requestPlayer,"&cYou do not have permission to use this command!");
            return;
        }

        if (args.length != 1) {
            send(requestPlayer,"&cInvalid syntax!");
            return;
        }
        
        if (receivePlayer == null) {
            send(requestPlayer,"&cPlayer is not online!");
            return;
        }

        if (receivePlayer.getUniqueId().equals(requestPlayer.getUniqueId())) {
            send(requestPlayer,"&cYou may not teleport to yourself!");
            return;
        }

        if (tpaMap.containsKey(requestPlayer.getUniqueId())) {
            send(requestPlayer,"&6You already have a pending request!");
            return;
        }

        final int timeoutSecond = plugin.getRequestTimeout();
        final long timeoutTicks = timeoutSecond * 20L;

        tpaMap.put(requestPlayer.getUniqueId(), receivePlayer.getUniqueId());

        send(requestPlayer,"&6Sent TPA request to &c%s",receivePlayer.getName());
        send(receivePlayer,"""
            &c%s &6wants to teleport to you.
            &6Type &c/tpaccept &6to accept.
            &6Type &c/tpdeny &6to deny.
            &6You have %s second(s) to respond.
            """,requestPlayer.getName(),timeoutSecond);

        new BukkitRunnable() {
            @Override
            public void run() {
                UUID receivePlayerUUID = tpaMap.remove(requestPlayer.getUniqueId());

                if (receivePlayerUUID != null) {
                    Player receivePlayer = Bukkit.getPlayer(receivePlayerUUID);

                    String receivePlayerName = (receivePlayer != null && receivePlayer.isOnline()) ? receivePlayer.getName() : "a player";
                    send(requestPlayer, "&cYour TPA request to %s has timed out.", receivePlayerName);

                    if (receivePlayer != null && receivePlayer.isOnline()) {
                        send(receivePlayer, "&c%s's TPA request has timed out.", requestPlayer.getName());
                    }
                }
            }
        }.runTaskLater(plugin, timeoutTicks);
    }

    public void handleTpahereCommand(CommandSender sender, String[] args) {
        final Player requestPlayer = (Player) sender;
        final Player receivePlayer = Bukkit.getPlayer(args[0]);

        if (getCooldown(requestPlayer, plugin.getCommandCooldown(), "tpahere")) {
            return;
        }

        if (!requestPlayer.hasPermission("brightstpa.tpahere")) {
            send(requestPlayer,"&cYou do not have permission to use this command!");
            return;
        }

        if (args.length != 1) {
            send(requestPlayer,"&cInvalid syntax!");
            return;
        }

        if (receivePlayer == null) {
            send(requestPlayer,"&cPlayer is not online!");
            return;
        }

        if (receivePlayer.getUniqueId().equals(requestPlayer.getUniqueId())) {
            send(requestPlayer,"&cYou may not teleport to yourself!");
            return;
        }

        if (tpaMap.containsKey(requestPlayer.getUniqueId())) {
            send(requestPlayer,"&6You already have a pending request!");
            return;
        }

        final int timeoutSecond = plugin.getRequestTimeout();
        final long timeoutTicks = timeoutSecond * 20L;

        tpahereMap.put(requestPlayer.getUniqueId(), receivePlayer.getUniqueId());

        send(requestPlayer,"&6Sent TPAHERE request to &c%s", receivePlayer.getName());
        send(receivePlayer,"""
            &c%s &6wants you to teleport to them.
            &6Type &c/tpaccept &6to accept
            &6Type &c/tpdeny &6to deny.
            &6You have %s second(s) to respond.
            """,requestPlayer.getName(),timeoutSecond);

        new BukkitRunnable() {
            @Override
            public void run() {
                UUID receivePlayerUUID = tpahereMap.remove(requestPlayer.getUniqueId());

                if (receivePlayerUUID != null) {
                    Player receivePlayer = Bukkit.getPlayer(receivePlayerUUID);

                    String receivePlayerName = (receivePlayer != null && receivePlayer.isOnline()) ? receivePlayer.getName() : "a player";
                    send(requestPlayer, "&cYour TPA request to %s has timed out.", receivePlayerName);

                    if (receivePlayer != null && receivePlayer.isOnline()) {
                        send(receivePlayer, "&c%s's TPA request has timed out.", requestPlayer.getName());
                    }
                }
            }
        }.runTaskLater(plugin, timeoutTicks);
    }
}
