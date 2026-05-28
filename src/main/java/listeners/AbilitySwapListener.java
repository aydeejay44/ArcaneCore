package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class AbilitySwapListener implements Listener {

    private final ArcaneCore plugin;

    public AbilitySwapListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {

        Player player = event.getPlayer();

        if (!plugin.getAbilityTriggerManager().isSwap(player)) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ItemStack arcaneItem = plugin.getPlayerArcaneManager().isNormalArcane(mainHand)
                ? mainHand
                : offHand;

        if (!plugin.getPlayerArcaneManager().isNormalArcane(arcaneItem)) return;

        event.setCancelled(true);

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendActionBar("§cYou unlock this ability at Level 3.");
            return;
        }

        if (BreezeArcane.isBreezeArcane(arcaneItem)) {
            player.sendActionBar("§bBreeze swap trigger detected.");
            return;
        }

        if (LuckArcane.isLuckArcane(arcaneItem)) {
            player.sendActionBar("§aLuck swap trigger detected.");
            return;
        }

        if (FrostArcane.isFrostArcane(arcaneItem)) {
            player.sendActionBar("§bFrost swap trigger detected.");
            return;
        }

        if (EmberArcane.isEmberArcane(arcaneItem)) {
            player.sendActionBar("§6Ember swap trigger detected.");
        }
    }
}