package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.TraderItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TraderCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        player.getInventory().addItem(TraderItem.createItem());

        player.sendMessage(ChatColor.GOLD + "You received a Trader!");

        return true;
    }
}
