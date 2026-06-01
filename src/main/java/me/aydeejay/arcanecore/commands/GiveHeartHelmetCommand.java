package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.HeartHelmet;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveHeartHelmetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        player.getInventory().addItem(HeartHelmet.create());
        player.sendMessage(ChatColor.RED + "You received the Heart Helmet.");
        return true;
    }
}