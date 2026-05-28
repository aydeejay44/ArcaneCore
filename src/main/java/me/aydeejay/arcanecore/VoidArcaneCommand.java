package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.aydeejay.arcanecore.ArcaneCore;

public class VoidArcaneCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public VoidArcaneCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (plugin.getPlayerArcaneManager().hasVoidArcane(player)) {

            player.sendMessage(
                    ChatColor.RED + "You already own a Void Arcane."
            );

            return true;
        }

        player.getInventory().addItem(VoidArcane.createItem());

        player.sendMessage(
                ChatColor.DARK_PURPLE + "You received the Void Arcane!"
        );

        return true;
    }
}