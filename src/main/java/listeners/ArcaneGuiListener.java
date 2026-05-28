package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ArcaneGuiListener implements Listener {

    private final ArcaneCore plugin;

    public ArcaneGuiListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Arcane Status")) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        switch (event.getSlot()) {

            case 10 -> {
                player.sendMessage(ChatColor.AQUA + "Breeze Ability:");
                player.sendMessage(ChatColor.GRAY + "Fast wind movement ability.");
                player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_IDLE_GROUND, 1f, 1f);
            }

            case 12 -> {
                player.sendMessage(ChatColor.AQUA + "Frost Ability:");
                player.sendMessage(ChatColor.GRAY + "Freezes enemies temporarily.");
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            }

            case 14 -> {
                player.sendMessage(ChatColor.RED + "Ember Ability:");
                player.sendMessage(ChatColor.GRAY + "Shoots a fire beam.");
                player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1f, 1f);
            }

            case 16 -> {
                player.sendMessage(ChatColor.GREEN + "Luck Ability:");
                player.sendMessage(ChatColor.GRAY + "Random chance-based effects.");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }

            case 22 -> {
                player.sendMessage(ChatColor.GOLD + "Cooldowns refresh automatically.");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }
}