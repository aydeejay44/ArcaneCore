package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class EmberListener implements Listener {

    private final ArcaneCore plugin;

    public EmberListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {

                // Passive works even at level 0
                if (plugin.getArcaneManager().hasEmber(player)) {
                    if (plugin.getLevelManager().getLevel(player) <= 0) continue;

                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.FIRE_RESISTANCE,
                            60,
                            0,
                            true,
                            false
                    ));

                }
            }
        }, 0L, 20L);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getHand() == null) {
            return;
        }

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!plugin.getAbilityTriggerManager().isSneakRightClick(player)) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        ItemStack item;

        if (event.getHand() == EquipmentSlot.HAND) {
            item = player.getInventory().getItemInMainHand();
        } else {
            item = player.getInventory().getItemInOffHand();
        }

        if (!EmberArcane.isEmberArcane(item)) {
            return;
        }

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendActionBar("§cYou unlock this ability at Level 3.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        activateEmber(player);
    }

    public void activateEmber(Player player) {

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "ember")) {
            plugin.getCooldownManager().showCooldown(player, "Ember", "ember");
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_BASS,
                    0.7f,
                    0.5f
            );
            return;
        }

        int level = plugin.getLevelManager().getLevel(player);

        double damage;
        int fireTicks;

        if (level >= 4) {
            damage = plugin.getConfig().getDouble("ember.damage.level-4", 8.0);
            fireTicks = plugin.getConfig().getInt("ember.fire-ticks.level-4", 60);
        } else {
            damage = plugin.getConfig().getDouble("ember.damage.level-3", 6.0);
            fireTicks = plugin.getConfig().getInt("ember.fire-ticks.level-3", 40);
        }

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ITEM_FIRECHARGE_USE,
                1.0f,
                0.8f
        );

        player.getWorld().spawnParticle(
                Particle.FLAME,
                player.getLocation().add(0, 1, 0),
                35,
                0.4,
                0.5,
                0.4,
                0.02
        );

        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                15,
                0.3,
                0.3,
                0.3,
                0.01
        );

        HashSet<UUID> hitEntities = new HashSet<>();

        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {

            int beamRange = plugin.getConfig().getInt("ember.beam-range", 15);

            for (int i = 1; i <= beamRange; i++) {

                Location point = start.clone().add(direction.clone().multiply(i));

                player.getWorld().spawnParticle(
                        Particle.FLAME,
                        point,
                        18,
                        0.18,
                        0.18,
                        0.18,
                        0.02
                );

                player.getWorld().spawnParticle(
                        Particle.LAVA,
                        point,
                        2,
                        0.08,
                        0.08,
                        0.08,
                        0
                );

                player.getWorld().spawnParticle(
                        Particle.SMOKE,
                        point,
                        6,
                        0.12,
                        0.12,
                        0.12,
                        0.01
                );

                double hitRadius = plugin.getConfig().getDouble("ember.hit-radius", 1.3);

                for (LivingEntity target : point.getNearbyLivingEntities(hitRadius)) {

                    if (target.equals(player)) {
                        continue;
                    }

                    if (hitEntities.contains(target.getUniqueId())) {
                        continue;
                    }

                    if (target instanceof Player targetPlayer) {
                        if (plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
                            continue;
                        }
                    }

                    hitEntities.add(target.getUniqueId());

                    target.setNoDamageTicks(0);

                    double health = target.getHealth();

                    if (health <= damage) {
                        target.setHealth(0.0);
                    } else {
                        target.setHealth(Math.max(0, health - damage));
                    }

                    Vector knockback = direction.clone()
                            .normalize()
                            .multiply(0.7);

                    knockback.setY(0.18);

                    target.setVelocity(knockback);

                    target.setVisualFire(true);

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (!target.isDead()) {
                            target.setVisualFire(false);
                        }
                    }, fireTicks);

                    target.getWorld().spawnParticle(
                            Particle.LAVA,
                            target.getLocation().add(0, 1, 0),
                            12,
                            0.4,
                            0.6,
                            0.4,
                            0.01
                    );

                    target.getWorld().playSound(
                            target.getLocation(),
                            Sound.ITEM_FIRECHARGE_USE,
                            1.0f,
                            0.8f
                    );
                }
            }

            task.cancel();

        }, 6L, 1L);

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "ember",
                plugin.getConfig().getInt("ember.cooldown-seconds", 120)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Ember", "ember");
    }
}