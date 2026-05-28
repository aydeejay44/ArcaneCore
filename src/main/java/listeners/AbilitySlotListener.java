package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class AbilitySlotListener implements Listener {

    private final ArcaneCore plugin;

    public AbilitySlotListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {

        Player player = event.getPlayer();

        int slotNumber = event.getNewSlot() + 1;

        if (!plugin.getAbilityTriggerManager().isSlot(player, slotNumber)) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.performCommand("__arcane_internal_ability");
        });
    }
}