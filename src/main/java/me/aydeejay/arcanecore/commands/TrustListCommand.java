package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrustListCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public TrustListCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (plugin.getTrustManager().getTrusted(player).isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You do not trust anyone.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Trusted Players:");

        for (UUID uuid : plugin.getTrustManager().getTrusted(player)) {
            OfflinePlayer trusted = Bukkit.getOfflinePlayer(uuid);
            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + trusted.getName());
        }

        return true;
    }
}
