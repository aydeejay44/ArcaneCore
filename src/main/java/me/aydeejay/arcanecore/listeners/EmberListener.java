package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import org.bukkit.Color;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class EmberListener implements Listener {

    private final ArcaneCore plugin;

    public EmberListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!plugin.getArcaneManager().hasEmber(player)) continue;
                if (plugin.getLevelManager().getLevel(player) <= 0) continue;

                if (ConfigValues.isPotionEnabled(plugin, "ember.passive.fire-resistance-level", 1)) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.FIRE_RESISTANCE,
                            ConfigValues.getInt(plugin, "ember.passive.effect-duration-ticks", 60, 1, 6000),
                            ConfigValues.getPotionAmplifier(plugin, "ember.passive.fire-resistance-level", 1),
                            true,
                            false
                    ));
                }
            }
        }, 0L, 20L);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() == null) return;

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!plugin.getAbilityTriggerManager().isSneakRightClick(player)) return;
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // fire once, on the main-hand pass

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!EmberArcane.isEmberArcane(item)) return;

        event.setCancelled(true);

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            return;
        }

        activateEmber(player);
    }

    public void activateEmber(Player player) {
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "ember")) {
            plugin.getCooldownManager().showCooldown(player, "Ember", "ember");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
            return;
        }

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "ember",
                ConfigValues.getInt(plugin, "ember.cooldown-seconds", 120, 0, 86400)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Ember", "ember");

        int level = plugin.getLevelManager().getLevel(player);

        double range = ConfigValues.getDouble(plugin, "ember.beam-range", 15.0, 0.0, 100.0);
        double hitRadius = ConfigValues.getDouble(plugin, "ember.hit-radius", 1.3, 0.0, 20.0);

        int fireTicks = level >= 4
                ? ConfigValues.getInt(plugin, "ember.fire-ticks.level-4", 120, 0, 12000)
                : ConfigValues.getInt(plugin, "ember.fire-ticks.level-3", 80, 0, 12000);

        double damage = level >= 4
                ? ConfigValues.getDouble(plugin, "ember.damage.level-4", 10.0, 0.0, 1000.0)
                : ConfigValues.getDouble(plugin, "ember.damage.level-3", 8.0, 0.0, 1000.0);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f);

        player.getWorld().spawnParticle(
                Particle.FLAME,
                player.getLocation().add(0, 1, 0),
                ConfigValues.getInt(plugin, "ember.particles.cast-flame", 45, 0, 5000),
                0.45,
                0.5,
                0.45,
                0.03
        );

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                shootBeam(player, range, hitRadius, fireTicks, damage);
            }
        }, ConfigValues.getLong(plugin, "ember.cast-delay-ticks", 6L, 0L, 200L));
    }

    private void shootBeam(Player player, double range, double hitRadius, int fireTicks, double damage) {
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        Set<LivingEntity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            double distance = 0;
            final double beamStep = ConfigValues.getDouble(plugin, "ember.beam-step", 0.35, 0.05, 5.0);
            final int stepsPerTick = ConfigValues.getInt(plugin, "ember.beam-steps-per-tick", 4, 1, 100);

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (distance >= range) {
                    cancel();
                    return;
                }

                for (int i = 0; i < stepsPerTick; i++) {
                    distance += beamStep;

                    if (distance >= range) {
                        cancel();
                        return;
                    }

                    Location point = start.clone().add(direction.clone().multiply(distance));

                    if (point.getBlock().getType().isSolid()) {
                        cancel();
                        return;
                    }

                    spawnEmberBeamParticles(point);

                    for (Entity entity : point.getWorld().getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                        if (!(entity instanceof LivingEntity target)) continue;
                        if (target.equals(player)) continue;
                        if (hitEntities.contains(target)) continue;

                        if (target instanceof Player targetPlayer) {
                            if (plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
                                continue;
                            }
                        }

                        hitEntities.add(target);

                        if (!dealTrueDamage(target, player, damage)) {
                            continue;
                        }

                        target.setFireTicks(fireTicks);
                        applyEmberKnockback(target, direction);

                        target.getWorld().spawnParticle(
                                Particle.FLAME,
                                target.getLocation().add(0, 1, 0),
                                ConfigValues.getInt(plugin, "ember.particles.hit-flame", 35, 0, 5000),
                                0.5,
                                0.8,
                                0.5,
                                0.04
                        );

                        target.getWorld().playSound(
                                target.getLocation(),
                                Sound.ENTITY_BLAZE_HURT,
                                0.8f,
                                1.2f
                        );
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnEmberBeamParticles(Location point) {
        point.getWorld().spawnParticle(Particle.FLAME, point, ConfigValues.getInt(plugin, "ember.particles.beam.flame", 16, 0, 5000), 0.16, 0.16, 0.16, 0.02);
        point.getWorld().spawnParticle(Particle.SMALL_FLAME, point, ConfigValues.getInt(plugin, "ember.particles.beam.small-flame", 10, 0, 5000), 0.14, 0.14, 0.14, 0.02);
        point.getWorld().spawnParticle(Particle.SMOKE, point, ConfigValues.getInt(plugin, "ember.particles.beam.smoke", 8, 0, 5000), 0.12, 0.12, 0.12, 0.015);
        point.getWorld().spawnParticle(Particle.ASH, point, ConfigValues.getInt(plugin, "ember.particles.beam.ash", 5, 0, 5000), 0.18, 0.18, 0.18, 0.01);
        point.getWorld().spawnParticle(Particle.LAVA, point, ConfigValues.getInt(plugin, "ember.particles.beam.lava", 1, 0, 5000), 0.05, 0.05, 0.05, 0);
        point.getWorld().spawnParticle(
                Particle.DUST,
                point,
                ConfigValues.getInt(plugin, "ember.particles.beam.dust", 8, 0, 5000),
                0.10,
                0.10,
                0.10,
                0,
                new Particle.DustOptions(
                        Color.fromRGB(255, 90, 20),
                        ConfigValues.getFloat(plugin, "ember.particles.beam.dust-size", 1.2f, 0.1f, 10.0f)
                )
        );
    }

    private void applyEmberKnockback(LivingEntity target, Vector direction) {
        if (!(target instanceof Player)) {
            return;
        }

        Vector knockback = direction.clone();
        knockback.setY(0);

        if (knockback.lengthSquared() == 0) {
            return;
        }

        knockback.normalize().multiply(ConfigValues.getDouble(plugin, "ember.knockback.horizontal", 0.4, 0.0, 10.0));
        knockback.setY(ConfigValues.getDouble(plugin, "ember.knockback.vertical", 0.12, 0.0, 10.0));
        target.setVelocity(target.getVelocity().add(knockback));
    }

    @SuppressWarnings("removal")
    private boolean dealTrueDamage(LivingEntity target, Player source, double damage) {
        if (damage <= 0 || target.isDead()) {
            return false;
        }

        // Let cancellation-based protections react, then apply health damage directly to bypass armor.
        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                source,
                target,
                EntityDamageEvent.DamageCause.MAGIC,
                damage
        );
        plugin.getServer().getPluginManager().callEvent(damageEvent);

        if (damageEvent.isCancelled()) {
            return false;
        }

        double finalDamage = Math.max(0.0, damageEvent.getFinalDamage());
        if (finalDamage <= 0) {
            return false;
        }

        target.setNoDamageTicks(0);
        target.setKiller(source);
        target.setLastDamage(finalDamage);
        target.setLastDamageCause(damageEvent);
        target.setHealth(Math.max(0.0, target.getHealth() - finalDamage));
        return true;
    }
}
