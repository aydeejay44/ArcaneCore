package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.LevelItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        player.getInventory().addItem(LevelItem.createItem());

        player.sendMessage(ChatColor.AQUA + "You received a Level Item!");

        return true;
    }
}
