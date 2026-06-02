package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BreezeListener implements Listener {

    private final ArcaneCore plugin;
    private final java.util.HashSet<java.util.UUID> dashingPlayers = new java.util.HashSet<>();
    private final java.util.HashMap<java.util.UUID, java.util.HashSet<java.util.UUID>> hitPlayers = new java.util.HashMap<>();

    public BreezeListener(ArcaneCore plugin) {
        this.plugin = plugin;
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

        if (event.getHand() != EquipmentSlot.HAND) {
            return; // ignore the off-hand pass so the ability fires once
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!BreezeArcane.isBreezeArcane(item)) {
            return;
        }

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendActionBar(ChatColor.RED + "You unlock this ability at Level 3.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        activateBreeze(player);
    }

    public void activateBreeze(Player player) {

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "breeze")) {
            plugin.getCooldownManager().showCooldown(player, "Breeze", "breeze");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
            return;
        }

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "breeze",
                plugin.getConfig().getInt("breeze.cooldown-seconds", 25)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Breeze", "breeze");

        int level = plugin.getLevelManager().getLevel(player);

        double distance = 0.0;

        if (level >= 4) {
            distance = plugin.getConfig().getDouble("breeze.dash-distance.level-4", 8.0);
        } else if (level >= 3) {
            distance = plugin.getConfig().getDouble("breeze.dash-distance.level-3", 5.0);
        }

        Vector direction = player.getLocation()
                .getDirection()
                .normalize();

        Vector velocity = direction.multiply(distance / 4.0);

        velocity.setY(Math.max(0.25, velocity.getY()));

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_BREEZE_WIND_BURST,
                1.0f,
                1.2f
        );

        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation(),
                35,
                0.5,
                0.2,
                0.5,
                0.03
        );

        player.getWorld().spawnParticle(
                Particle.GUST,
                player.getLocation(),
                20,
                0.4,
                0.2,
                0.4,
                0.01
        );

        player.setVelocity(velocity);

        plugin.getServer().getScheduler().runTaskTimer(plugin, new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (ticks >= 10 || player.isOnGround()) {

                    player.getWorld().spawnParticle(
                            Particle.CLOUD,
                            player.getLocation(),
                            20,
                            0.3,
                            0.1,
                            0.3,
                            0.02
                    );

                    player.getWorld().playSound(
                            player.getLocation(),
                            Sound.ENTITY_BREEZE_LAND,
                            0.8f,
                            1.2f
                    );

                    cancel();
                    return;
                }

                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation(),
                        6,
                        0.15,
                        0.15,
                        0.15,
                        0.01
                );

                player.getWorld().spawnParticle(
                        Particle.GUST,
                        player.getLocation(),
                        3,
                        0.1,
                        0.1,
                        0.1,
                        0
                );

                ticks++;
            }

        }, 0L, 1L);

        java.util.UUID dasherId = player.getUniqueId();

        dashingPlayers.add(dasherId);
        hitPlayers.put(dasherId, new java.util.HashSet<>());

        final org.bukkit.Location[] lastLocation = {player.getLocation().clone()};

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {

            if (!dashingPlayers.contains(dasherId)) {
                task.cancel();
                return;
            }

            if (!player.isOnline()) {
                dashingPlayers.remove(dasherId);
                hitPlayers.remove(dasherId);
                task.cancel();
                return;
            }

            org.bukkit.Location currentLocation = player.getLocation().clone();
            org.bukkit.Location previousLocation = lastLocation[0];

            double hitRadius = plugin.getConfig().getDouble("breeze.hit-radius", 2.8);
            double damage = plugin.getConfig().getDouble("breeze.dash-damage", 4.0);

            Vector travel = currentLocation.toVector().subtract(previousLocation.toVector());
            int steps = Math.max(1, (int) Math.ceil(travel.length() * 2));

            for (int step = 0; step <= steps; step++) {

                double t = (double) step / steps;

                org.bukkit.Location checkPoint = previousLocation.clone().add(
                        travel.clone().multiply(t)
                );

                Vector positionOffset = checkPoint.toVector().subtract(currentLocation.toVector());
                BoundingBox dashHitBox = player.getBoundingBox()
                        .shift(positionOffset)
                        .expand(hitRadius);

                for (LivingEntity target : player.getWorld().getLivingEntities()) {

                    if (target.equals(player)) {
                        continue;
                    }

                    if (target.getWorld() != player.getWorld()) {
                        continue;
                    }

                    if (!dashHitBox.overlaps(target.getBoundingBox())) {
                        continue;
                    }

                    if (target instanceof Player targetPlayer) {
                        if (plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
                            continue;
                        }
                    }

                    java.util.HashSet<java.util.UUID> alreadyHit = hitPlayers.get(dasherId);

                    if (alreadyHit == null || alreadyHit.contains(target.getUniqueId())) {
                        continue;
                    }

                    alreadyHit.add(target.getUniqueId());

                    // The dash only attempts one hit per target. Clear normal
                    // hurt immunity so that attempt is not silently discarded.
                    target.setNoDamageTicks(0);
                    target.damage(damage, player); // routed through the damage pipeline (armor, i-frames, events)

                    Vector knockback = target.getLocation().toVector()
                            .subtract(player.getLocation().toVector())
                            .normalize()
                            .multiply(0.8);

                    knockback.setY(0.25);

                    target.setVelocity(knockback);

                    target.getWorld().spawnParticle(
                            Particle.CLOUD,
                            target.getLocation(),
                            25,
                            0.4,
                            0.8,
                            0.4,
                            0.08
                    );

                    player.playSound(
                            player.getLocation(),
                            Sound.ENTITY_BREEZE_IDLE_GROUND,
                            1f,
                            1.2f
                    );

                    player.playSound(
                            player.getLocation(),
                            Sound.ENTITY_WIND_CHARGE_THROW,
                            1f,
                            1f
                    );

                    target.getWorld().playSound(
                            target.getLocation(),
                            Sound.ENTITY_BREEZE_WIND_BURST,
                            1.0f,
                            1.1f
                    );
                }
            }

            lastLocation[0] = currentLocation;

        }, 0L, 1L);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            dashingPlayers.remove(dasherId);
            hitPlayers.remove(dasherId);
        }, plugin.getConfig().getLong("breeze.damage-window-ticks", 30L));

        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation(),
                35,
                0.4,
                0.4,
                0.4,
                0.05
        );

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_BREEZE_JUMP,
                1.0f,
                1.2f
        );
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (plugin.getArcaneManager().hasBreeze(player)
                && plugin.getLevelManager().getLevel(player) > 0) {
            event.setCancelled(true);
        }
    }
}
