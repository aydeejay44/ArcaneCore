package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveBreezeCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public GiveBreezeCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (plugin.getPlayerArcaneManager().hasNormalArcane(player)) {
            player.sendMessage(ChatColor.RED + "You already have an Arcane.");
            return true;
        }

        plugin.getPlayerArcaneManager().replaceOwnedArcane(
                player,
                BreezeArcane.createItem()
        );

        player.sendMessage(ChatColor.AQUA + "You received the Breeze Arcane!");

        return true;
    }
}
