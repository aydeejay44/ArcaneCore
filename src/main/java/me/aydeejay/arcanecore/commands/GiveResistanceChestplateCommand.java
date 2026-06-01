package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.items.ResistanceChestplate;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveResistanceChestplateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        player.getInventory().addItem(ResistanceChestplate.create());
        player.sendMessage(ChatColor.DARK_PURPLE + "You received the Resistance Chestplate.");
        return true;
    }
}