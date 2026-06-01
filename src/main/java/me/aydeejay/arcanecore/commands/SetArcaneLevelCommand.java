package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetArcaneLevelCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public SetArcaneLevelCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("arcanecore.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setarcanelevel <player> <level>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "That player is not online.");
            return true;
        }

        int level;

        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Level must be a number.");
            return true;
        }

        plugin.getLevelManager().setLevel(target, level);
        int actualLevel = plugin.getLevelManager().getLevel(target);

        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s Arcane level to " + actualLevel + ".");
        target.sendMessage(ChatColor.AQUA + "Your Arcane level is now " + actualLevel + ".");

        return true;
    }
}
