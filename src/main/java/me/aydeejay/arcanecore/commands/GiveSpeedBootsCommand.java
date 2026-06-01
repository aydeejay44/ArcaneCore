package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.SpeedBoots;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveSpeedBootsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        player.getInventory().addItem(SpeedBoots.create());
        player.sendMessage(ChatColor.AQUA + "You received the Speed Boots.");
        return true;
    }
}