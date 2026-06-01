package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ArcaneKillEffectListener implements Listener {

    private final ArcaneCore plugin;

    public ArcaneKillEffectListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        Location loc = victim.getLocation().add(0, 1, 0);

        if (plugin.getArcaneManager().hasBreeze(killer)) {
            loc.getWorld().spawnParticle(Particle.CLOUD, loc, 45, 0.7, 0.7, 0.7, 0.05);
            loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 8, 0.6, 0.4, 0.6, 0);
            loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.2f);
        }

        else if (plugin.getArcaneManager().hasFrost(killer)) {
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 60, 0.8, 0.8, 0.8, 0.04);
            loc.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, loc, 25, 0.6, 0.6, 0.6, 0.03);
            loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
        }

        else if (plugin.getArcaneManager().hasEmber(killer)) {
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 55, 0.8, 0.8, 0.8, 0.05);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, 35, 0.7, 0.7, 0.7, 0.04);
            loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.9f);
        }

        else if (plugin.getArcaneManager().hasLuck(killer)) {
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 45, 0.8, 0.8, 0.8, 0.05);
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
        }

        if (plugin.getArcaneManager().hasVoid(killer)) {
            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 80, 0.9, 0.9, 0.9, 0.1);
            loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 25, 0.7, 0.7, 0.7, 0.03);
            loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.6f);
        }
    }
}
