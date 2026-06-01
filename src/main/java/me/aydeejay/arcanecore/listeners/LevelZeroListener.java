package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LevelZeroListener implements Listener {

    private final ArcaneCore plugin;

    public LevelZeroListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    // Max-health for level-0 players is handled by BonusHealthListener (bonus 0
    // => base 20). This listener only softens the damage they deal.
    @EventHandler
    public void onLevelZeroDamage(EntityDamageByEntityEvent event) {

        Player player = null;

        if (event.getDamager() instanceof Player damager) {
            player = damager;
        } else if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            player = shooter;
        }

        if (player == null) {
            return;
        }

        if (plugin.getLevelManager().getLevel(player) <= 0) {
            event.setDamage(event.getDamage() * 0.65);
        }
    }
}
