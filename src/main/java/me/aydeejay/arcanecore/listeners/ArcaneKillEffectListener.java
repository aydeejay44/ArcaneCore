package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
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
            loc.getWorld().spawnParticle(Particle.CLOUD, loc, ConfigValues.getInt(plugin, "kill-effects.breeze.cloud", 45, 0, 5000), 0.7, 0.7, 0.7, 0.05);
            loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, ConfigValues.getInt(plugin, "kill-effects.breeze.sweep-attack", 8, 0, 5000), 0.6, 0.4, 0.6, 0);
            loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.2f);
        }

        else if (plugin.getArcaneManager().hasFrost(killer)) {
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, ConfigValues.getInt(plugin, "kill-effects.frost.snowflake", 60, 0, 5000), 0.8, 0.8, 0.8, 0.04);
            loc.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, loc, ConfigValues.getInt(plugin, "kill-effects.frost.snowball", 25, 0, 5000), 0.6, 0.6, 0.6, 0.03);
            loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
        }

        else if (plugin.getArcaneManager().hasEmber(killer)) {
            loc.getWorld().spawnParticle(Particle.FLAME, loc, ConfigValues.getInt(plugin, "kill-effects.ember.flame", 55, 0, 5000), 0.8, 0.8, 0.8, 0.05);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, ConfigValues.getInt(plugin, "kill-effects.ember.smoke", 35, 0, 5000), 0.7, 0.7, 0.7, 0.04);
            loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.9f);
        }

        else if (plugin.getArcaneManager().hasLuck(killer)) {
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, ConfigValues.getInt(plugin, "kill-effects.luck.happy-villager", 45, 0, 5000), 0.8, 0.8, 0.8, 0.05);
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
        }

        if (plugin.getArcaneManager().hasVoid(killer)) {
            loc.getWorld().spawnParticle(Particle.PORTAL, loc, ConfigValues.getInt(plugin, "kill-effects.void.portal", 80, 0, 5000), 0.9, 0.9, 0.9, 0.1);
            loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, ConfigValues.getInt(plugin, "kill-effects.void.sculk-soul", 25, 0, 5000), 0.7, 0.7, 0.7, 0.03);
            loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.6f);
        }
    }
}
