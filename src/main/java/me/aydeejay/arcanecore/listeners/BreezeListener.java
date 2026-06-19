package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BreezeListener implements Listener {

    private final ArcaneCore plugin;
    private final Set<UUID> dashingPlayers = new HashSet<>();
    private final Map<UUID, Set<UUID>> hitEntities = new HashMap<>();
    private BukkitTask windMovementTask;

    public BreezeListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void startPassiveTask() {
        if (windMovementTask != null) {
            return;
        }

        int periodTicks = ConfigValues.getInt(plugin, "breeze.wind-movement.task-period-ticks", 2, 1, 20);
        windMovementTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                applyWindMovement(player);
            }
        }, 0L, periodTicks);
    }

    public void shutdown() {
        if (windMovementTask != null) {
            windMovementTask.cancel();
            windMovementTask = null;
        }

        dashingPlayers.clear();
        hitEntities.clear();
    }

    private void applyWindMovement(Player player) {
        if (!plugin.getArcaneManager().hasBreeze(player)) {
            return;
        }

        if (plugin.getLevelManager().getLevel(player) <= 0) {
            return;
        }

        if (player.isGliding()) {
            boostHorizontalMovement(
                    player,
                    "breeze.wind-movement.glide-horizontal-multiplier",
                    1.012,
                    "breeze.wind-movement.glide-max-horizontal-speed",
                    1.85
            );
            return;
        }

        if (isSwimming(player)) {
            boostHorizontalMovement(
                    player,
                    "breeze.wind-movement.swim-horizontal-multiplier",
                    1.035,
                    "breeze.wind-movement.swim-max-horizontal-speed",
                    0.36
            );
            return;
        }

        if (isClimbing(player)) {
            boostClimbingMovement(player);
        }
    }

    private void boostHorizontalMovement(
            Player player,
            String multiplierPath,
            double multiplierFallback,
            String maxSpeedPath,
            double maxSpeedFallback
    ) {
        Vector velocity = player.getVelocity();
        Vector horizontal = new Vector(velocity.getX(), 0, velocity.getZ());
        double currentSpeed = horizontal.length();

        if (currentSpeed < 0.03) {
            return;
        }

        double multiplier = ConfigValues.getDouble(plugin, multiplierPath, multiplierFallback, 1.0, 2.0);
        double maxSpeed = ConfigValues.getDouble(plugin, maxSpeedPath, maxSpeedFallback, 0.0, 10.0);

        if (multiplier <= 1.0 || currentSpeed >= maxSpeed) {
            return;
        }

        double boostedSpeed = Math.min(maxSpeed, currentSpeed * multiplier);
        horizontal.normalize().multiply(boostedSpeed);
        player.setVelocity(new Vector(horizontal.getX(), velocity.getY(), horizontal.getZ()));
    }

    private void boostClimbingMovement(Player player) {
        if (player.isSneaking()) {
            return;
        }

        Vector velocity = player.getVelocity();
        if (velocity.getY() <= 0.02) {
            return;
        }

        double boost = ConfigValues.getDouble(plugin, "breeze.wind-movement.climb-vertical-boost", 0.03, 0.0, 1.0);
        double maxUpVelocity = ConfigValues.getDouble(plugin, "breeze.wind-movement.climb-max-up-velocity", 0.22, 0.0, 2.0);

        velocity.setY(Math.min(maxUpVelocity, velocity.getY() + boost));
        player.setVelocity(velocity);
    }

    private boolean isSwimming(Player player) {
        return player.isSwimming()
                || player.getLocation().getBlock().isLiquid()
                || player.getEyeLocation().getBlock().isLiquid();
    }

    private boolean isClimbing(Player player) {
        return isClimbable(player.getLocation().getBlock().getType())
                || isClimbable(player.getEyeLocation().getBlock().getType());
    }

    private boolean isClimbable(Material material) {
        return material == Material.LADDER
                || material == Material.VINE
                || material == Material.SCAFFOLDING
                || material == Material.WEEPING_VINES
                || material == Material.WEEPING_VINES_PLANT
                || material == Material.TWISTING_VINES
                || material == Material.TWISTING_VINES_PLANT
                || material == Material.CAVE_VINES
                || material == Material.CAVE_VINES_PLANT;
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
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        activateBreeze(player);
    }

    public void activateBreeze(Player player) {

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            return;
        }

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "breeze")) {
            plugin.getCooldownManager().showCooldown(player, "Breeze", "breeze");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
            return;
        }

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "breeze",
                ConfigValues.getInt(plugin, "breeze.cooldown-seconds", 25, 0, 86400)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Breeze", "breeze");

        int level = plugin.getLevelManager().getLevel(player);

        double distance = 0.0;

        if (level >= 4) {
            distance = ConfigValues.getDouble(plugin, "breeze.dash-distance.level-4", 15.0, 0.0, 100.0);
        } else if (level >= 3) {
            distance = ConfigValues.getDouble(plugin, "breeze.dash-distance.level-3", 10.5, 0.0, 100.0);
        }

        Vector direction = player.getLocation()
                .getDirection()
                .normalize();

        double velocityDivisor = ConfigValues.getDouble(plugin, "breeze.velocity-divisor", 6.5, 0.1, 100.0);
        Vector velocity = direction.multiply(distance / velocityDivisor);

        velocity.setY(Math.max(ConfigValues.getDouble(plugin, "breeze.min-y-velocity", 0.25, 0.0, 10.0), velocity.getY()));

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_BREEZE_WIND_BURST,
                1.0f,
                1.2f
        );

        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation(),
                ConfigValues.getInt(plugin, "breeze.particles.launch-cloud", 35, 0, 5000),
                0.5,
                0.2,
                0.5,
                0.03
        );

        player.getWorld().spawnParticle(
                Particle.GUST,
                player.getLocation(),
                ConfigValues.getInt(plugin, "breeze.particles.launch-gust", 20, 0, 5000),
                0.4,
                0.2,
                0.4,
                0.01
        );

        player.setVelocity(velocity);

        UUID dasherId = player.getUniqueId();
        dashingPlayers.add(dasherId);
        hitEntities.put(dasherId, new HashSet<>());

        plugin.getServer().getScheduler().runTaskTimer(plugin, new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                boolean trailExpired = ticks >= ConfigValues.getInt(plugin, "breeze.trail-duration-ticks", 10, 1, 200);
                boolean landed = ticks > 2 && player.isOnGround();

                if (trailExpired || landed) {

                    player.getWorld().spawnParticle(
                            Particle.CLOUD,
                            player.getLocation(),
                            ConfigValues.getInt(plugin, "breeze.particles.landing-cloud", 20, 0, 5000),
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

                    if (landed) {
                        endDash(dasherId);
                    }
                    cancel();
                    return;
                }

                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation(),
                        ConfigValues.getInt(plugin, "breeze.particles.trail-cloud", 6, 0, 5000),
                        0.15,
                        0.15,
                        0.15,
                        0.01
                );

                player.getWorld().spawnParticle(
                        Particle.GUST,
                        player.getLocation(),
                        ConfigValues.getInt(plugin, "breeze.particles.trail-gust", 3, 0, 5000),
                        0.1,
                        0.1,
                        0.1,
                        0
                );

                ticks++;
            }

        }, 0L, 1L);

        startDashHitDetection(player, dasherId);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            endDash(dasherId);
        }, ConfigValues.getLong(plugin, "breeze.damage-window-ticks", 18L, 1L, 12000L));

        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation(),
                ConfigValues.getInt(plugin, "breeze.particles.finish-cloud", 35, 0, 5000),
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

    private void startDashHitDetection(Player player, UUID dasherId) {
        final Location[] lastLocation = {player.getLocation().clone()};
        final int[] ticks = {0};

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!dashingPlayers.contains(dasherId)) {
                task.cancel();
                return;
            }

            if (!player.isOnline()) {
                endDash(dasherId);
                task.cancel();
                return;
            }

            if (ticks[0] > 2 && player.isOnGround()) {
                endDash(dasherId);
                task.cancel();
                return;
            }

            Location currentLocation = player.getLocation().clone();
            Location previousLocation = lastLocation[0];
            Vector travel = currentLocation.toVector().subtract(previousLocation.toVector());

            double hitRadius = ConfigValues.getDouble(plugin, "breeze.hit-radius", 1.25, 0.0, 20.0);
            double hitStepSpacing = ConfigValues.getDouble(plugin, "breeze.hit-step-spacing", 0.35, 0.05, 5.0);
            double damage = ConfigValues.getDouble(plugin, "breeze.dash-damage", 4.0, 0.0, 1000.0);
            int steps = Math.max(1, (int) Math.ceil(travel.length() / hitStepSpacing));

            BoundingBox currentHitBox = player.getBoundingBox();

            for (int step = 0; step <= steps; step++) {
                double t = (double) step / steps;
                Location checkPoint = previousLocation.clone().add(travel.clone().multiply(t));
                Vector positionOffset = checkPoint.toVector().subtract(currentLocation.toVector());
                BoundingBox dashHitBox = currentHitBox
                        .clone()
                        .shift(positionOffset)
                        .expand(hitRadius);

                for (Entity entity : player.getWorld().getNearbyEntities(dashHitBox)) {
                    if (entity instanceof LivingEntity target
                            && dashHitBox.overlaps(target.getBoundingBox())) {
                        tryHitBreezeTarget(player, target, dasherId, damage);
                    }
                }
            }

            lastLocation[0] = currentLocation;
            ticks[0]++;
        }, 0L, 1L);
    }

    private void tryHitBreezeTarget(Player player, LivingEntity target, UUID dasherId, double damage) {
        if (target.equals(player) || target.isDead() || !target.isValid()) {
            return;
        }

        if (target instanceof Player targetPlayer
                && plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
            return;
        }

        Set<UUID> alreadyHit = hitEntities.get(dasherId);
        if (alreadyHit == null || !alreadyHit.add(target.getUniqueId())) {
            return;
        }

        if (!dealTrueDamage(target, player, damage)) {
            return;
        }

        applyBreezeKnockback(player, target);
        playBreezeHitEffects(player, target);
    }

    private void applyBreezeKnockback(Player player, LivingEntity target) {
        Vector knockback = target.getLocation().toVector()
                .subtract(player.getLocation().toVector());
        knockback.setY(0);

        if (knockback.lengthSquared() == 0) {
            knockback = player.getLocation().getDirection();
            knockback.setY(0);
        }

        if (knockback.lengthSquared() == 0) {
            return;
        }

        knockback.normalize().multiply(ConfigValues.getDouble(plugin, "breeze.knockback.horizontal", 0.8, 0.0, 10.0));
        knockback.setY(ConfigValues.getDouble(plugin, "breeze.knockback.vertical", 0.25, 0.0, 10.0));
        target.setVelocity(knockback);
    }

    private void playBreezeHitEffects(Player player, LivingEntity target) {
        target.getWorld().spawnParticle(
                Particle.CLOUD,
                target.getLocation(),
                ConfigValues.getInt(plugin, "breeze.particles.hit-cloud", 25, 0, 5000),
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

    private void endDash(UUID dasherId) {
        dashingPlayers.remove(dasherId);
        hitEntities.remove(dasherId);
    }

    @SuppressWarnings("removal")
    private boolean dealTrueDamage(LivingEntity target, Player source, double damage) {
        if (damage <= 0 || target.isDead()) {
            return false;
        }

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
