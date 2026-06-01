package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.StrengthAxe;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveStrengthAxeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        player.getInventory().addItem(StrengthAxe.create());

        player.sendMessage(ChatColor.RED + "You received the Strength Axe.");

        return true;
    }
}