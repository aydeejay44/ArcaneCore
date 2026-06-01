package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TrustListener implements Listener {

    private final ArcaneCore plugin;

    public TrustListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;

        Entity damager = event.getDamager();

        if (damager instanceof Player player) {
            attacker = player;
        }

        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                attacker = player;
            }
        }

        if (attacker == null) {
            return;
        }

        if (plugin.getTrustManager().shouldBlockDamage(attacker, victim)) {

            event.setCancelled(true);

            attacker.sendActionBar(
                    ChatColor.GREEN + "You cannot damage trusted players."
            );
        }
    }
}
