package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveEmberCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public GiveEmberCommand(ArcaneCore plugin) {
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
                EmberArcane.createItem()
        );

        player.sendMessage(ChatColor.RED + "You received the Ember Arcane!");

        return true;
    }
}
