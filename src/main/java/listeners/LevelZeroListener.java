package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LevelZeroListener implements Listener {

    private final ArcaneCore plugin;

    public LevelZeroListener(ArcaneCore plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (plugin.getLevelManager().getLevel(player) <= 0) {

                    if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
                    }
                }

            }

        }, 0L, 40L);
    }

    @EventHandler
    public void onLevelZeroDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (plugin.getLevelManager().getLevel(player) <= 0) {
            event.setDamage(event.getDamage() * 0.65);
        }
    }
}