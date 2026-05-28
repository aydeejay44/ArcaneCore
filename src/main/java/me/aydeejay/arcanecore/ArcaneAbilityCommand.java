package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.*;
import me.aydeejay.arcanecore.listeners.*;
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
            player.sendActionBar("§cYou unlock this ability at Level 3.");
            return true;
        }

        if (BreezeArcane.isBreezeArcane(item)) {
            new BreezeListener(plugin).activateBreeze(player);
            return true;
        }

        if (LuckArcane.isLuckArcane(item)) {
            new LuckListener(plugin).activateLuck(player);
            return true;
        }

        if (FrostArcane.isFrostArcane(item)) {
            new FrostListener(plugin).activateFrost(player);
            return true;
        }

        if (EmberArcane.isEmberArcane(item)) {
            new EmberListener(plugin).activateEmber(player);
            return true;
        }

        if (VoidArcane.isVoidArcane(item)) {
            new VoidListener(plugin).activateVoidFlight(player);
            return true;
        }

        player.sendActionBar("§cHold an Arcane to use its ability.");
        return true;
    }
}