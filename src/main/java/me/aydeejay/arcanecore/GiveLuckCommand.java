package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveLuckCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public GiveLuckCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (plugin.getPlayerArcaneManager().hasNormalArcane(player)) {
            player.sendMessage(ChatColor.RED + "You already have an Arcane.");
            return true;
        }

        plugin.getPlayerArcaneManager().replaceOwnedArcane(
                player,
                LuckArcane.createItem()
        );

        player.sendMessage(ChatColor.GREEN + "You received the Luck Arcane!");
        return true;
    }
}