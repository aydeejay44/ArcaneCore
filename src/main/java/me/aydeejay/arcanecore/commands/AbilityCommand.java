package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import me.aydeejay.arcanecore.items.LevelItem;

public class AbilityCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public AbilityCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (command.getName().equalsIgnoreCase("withdrawlevel")) {
            if (!(sender instanceof Player)) return true;

            int level = plugin.getLevelManager().getLevel(player);

            if (level <= 0) {
                player.sendMessage(ChatColor.RED + "You do not have any levels to withdraw.");
                return true;
            }

            plugin.getLevelManager().setLevel(player, level - 1);

            ItemStack item = LevelItem.createItem();

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

            for (ItemStack leftoverItem : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }

            player.sendMessage(ChatColor.GREEN + "You withdrew 1 level.");
            return true;
        }

        if (!(sender instanceof Player )) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /ability sneakrightclick");
            player.sendMessage(ChatColor.YELLOW + "Usage: /ability swap");
            player.sendMessage(ChatColor.YELLOW + "Usage: /ability slot1-slot9");
            return true;
        }

        String trigger = args[0].toLowerCase();

        if (!trigger.equals("sneakrightclick")
                && !trigger.equals("swap")
                && !trigger.matches("slot[1-9]")) {
            player.sendMessage(ChatColor.RED + "Invalid ability trigger.");
            player.sendMessage(ChatColor.YELLOW + "Use: sneakrightclick, swap, or slot1-slot9");
            return true;
        }

        plugin.getAbilityTriggerManager().setTrigger(player, trigger);

        player.sendMessage(ChatColor.GREEN + "Ability trigger set to: " + ChatColor.AQUA + trigger);

        return true;
    }
}
