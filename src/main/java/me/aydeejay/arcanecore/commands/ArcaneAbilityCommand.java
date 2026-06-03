package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArcaneAbilityCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public ArcaneAbilityCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            return true;
        }

        if (BreezeArcane.isBreezeArcane(item)) {
            plugin.getBreezeListener().activateBreeze(player);
            return true;
        }

        if (LuckArcane.isLuckArcane(item)) {
            plugin.getLuckListener().activateLuck(player);
            return true;
        }

        if (FrostArcane.isFrostArcane(item)) {
            plugin.getFrostListener().activateFrost(player);
            return true;
        }

        if (EmberArcane.isEmberArcane(item)) {
            plugin.getEmberListener().activateEmber(player);
            return true;
        }

        if (VoidArcane.isVoidArcane(item)) {
            plugin.getVoidListener().activateVoidFlight(player);
            return true;
        }

        player.sendMessage(ChatColor.RED + "Hold an Arcane to use its ability.");
        return true;
    }
}
