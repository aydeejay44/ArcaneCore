package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.HasteLeggings;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveHasteLeggingsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        player.getInventory().addItem(HasteLeggings.create());
        player.sendMessage(ChatColor.YELLOW + "You received the Haste Leggings.");
        return true;
    }
}