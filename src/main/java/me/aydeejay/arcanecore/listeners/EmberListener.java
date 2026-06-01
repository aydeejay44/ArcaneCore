package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
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

                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE,
                        60,
                        0,
                        true,
                        false
                ));
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
            player.sendActionBar(ChatColor.RED + "You unlock this ability at Level 3.");
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
                plugin.getConfig().getInt("ember.cooldown-seconds", 120)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Ember", "ember");

        int level = plugin.getLevelManager().getLevel(player);

        double range = plugin.getConfig().getDouble("ember.beam-range", 15);
        double hitRadius = plugin.getConfig().getDouble("ember.hit-radius", 1.3);

        int fireTicks = level >= 4
                ? plugin.getConfig().getInt("ember.fire-ticks.level-4", 120)
                : plugin.getConfig().getInt("ember.fire-ticks.level-3", 80);

        double damage = level >= 4
                ? plugin.getConfig().getDouble("ember.damage.level-4", 10.0)
                : plugin.getConfig().getDouble("ember.damage.level-3", 8.0);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f);

        player.getWorld().spawnParticle(
                Particle.FLAME,
                player.getLocation().add(0, 1, 0),
                45,
                0.45,
                0.5,
                0.45,
                0.03
        );

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                shootBeam(player, range, hitRadius, fireTicks, damage);
            }
        }, 6L);
    }

    private void shootBeam(Player player, double range, double hitRadius, int fireTicks, double damage) {
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        Set<LivingEntity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            double distance = 0;

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

                for (int i = 0; i < 4; i++) {
                    distance += 0.35;

                    if (distance >= range) {
                        cancel();
                        return;
                    }

                    Location point = start.clone().add(direction.clone().multiply(distance));

                    if (point.getBlock().getType().isSolid()) {
                        cancel();
                        return;
                    }

                    point.getWorld().spawnParticle(
                            Particle.FLAME,
                            point,
                            18,
                            0.12,
                            0.12,
                            0.12,
                            0.02
                    );

                    point.getWorld().spawnParticle(
                            Particle.LAVA,
                            point,
                            3,
                            0.06,
                            0.06,
                            0.06,
                            0
                    );

                    point.getWorld().spawnParticle(
                            Particle.SMOKE,
                            point,
                            8,
                            0.1,
                            0.1,
                            0.1,
                            0.01
                    );

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

                        target.setFireTicks(fireTicks);
                        target.damage(damage, player);

                        target.getWorld().spawnParticle(
                                Particle.FLAME,
                                target.getLocation().add(0, 1, 0),
                                35,
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
}
