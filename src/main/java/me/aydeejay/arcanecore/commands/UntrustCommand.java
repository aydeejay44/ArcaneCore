package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UntrustCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public UntrustCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /untrust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "That player is not online.");
            return true;
        }

        plugin.getTrustManager().untrust(player, target);

        player.sendMessage(ChatColor.RED + "You are no longer trusted with " + target.getName() + ".");
        target.sendMessage(ChatColor.RED + player.getName() + " removed your trust.");

        return true;
    }
}
