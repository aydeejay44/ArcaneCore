package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.items.LevelItem;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class LevelItemListener implements Listener {

    private final ArcaneCore plugin;

    public LevelItemListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!LevelItem.isLevelItem(player.getInventory().getItemInMainHand())) {
            return;
        }

        event.setCancelled(true);

        if (plugin.getLevelManager().getLevel(player) >= 4) {
            player.sendMessage(ChatColor.RED + "You are already at the max Arcane Level.");
            return;
        }

        player.getInventory().getItemInMainHand().setAmount(
                player.getInventory().getItemInMainHand().getAmount() - 1
        );

        plugin.getLevelManager().addLevels(player, 1);

        player.sendMessage(ChatColor.AQUA + "You consumed an Arcane Level!");
        player.sendMessage(ChatColor.GRAY + "New Arcane Level: " +
                ChatColor.AQUA + plugin.getLevelManager().getLevel(player));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }
}