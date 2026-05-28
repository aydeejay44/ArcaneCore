package me.aydeejay.arcanecore;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TrustCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public TrustCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // ACCEPT
        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {

            Player requester = Bukkit.getPlayer(args[1]);

            if (requester == null) {
                player.sendMessage(ChatColor.RED + "That player is no longer online.");
                return true;
            }

            if (!plugin.getTrustManager().hasRequest(player, requester)) {
                player.sendMessage(ChatColor.RED + "You do not have a trust request from that player.");
                return true;
            }

            plugin.getTrustManager().trust(player, requester);
            plugin.getTrustManager().removeRequest(player);

            player.sendMessage(ChatColor.GREEN + requester.getName() + " is now trusted.");
            requester.sendMessage(ChatColor.GREEN + player.getName() + " is now trusted.");

            return true;
        }

        // DECLINE
        if (args.length == 2 && args[0].equalsIgnoreCase("decline")) {

            Player requester = Bukkit.getPlayer(args[1]);

            if (requester != null &&
                    plugin.getTrustManager().hasRequest(player, requester)) {

                plugin.getTrustManager().removeRequest(player);

                requester.sendMessage(
                        ChatColor.RED + player.getName() + " declined your trust request."
                );

                player.sendMessage(
                        ChatColor.RED + "You declined " + requester.getName() + "'s trust request."
                );
            }

            return true;
        }

        // NORMAL /trust player
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /trust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "That player is not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot trust yourself.");
            return true;
        }

        if (plugin.getTrustManager().trusts(player, target)) {
            player.sendMessage(ChatColor.RED + "You are already trusted with that player.");
            return true;
        }

        plugin.getTrustManager().sendRequest(player, target);

        player.sendMessage(
                ChatColor.GREEN + "Trust request sent to " + target.getName() + "."
        );

        target.sendMessage(
                ChatColor.YELLOW + player.getName() + " wants to trust you."
        );

        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Accept]");
        accept.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/trust accept " + player.getName()
                )
        );

        TextComponent space = new TextComponent(" ");

        TextComponent decline = new TextComponent(ChatColor.RED + "[Decline]");
        decline.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/trust decline " + player.getName()
                )
        );

        target.spigot().sendMessage(accept, space, decline);

        return true;
    }
}