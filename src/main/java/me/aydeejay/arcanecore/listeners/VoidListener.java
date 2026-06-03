package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoidListener implements Listener {

    private final ArcaneCore plugin;
    private final Map<UUID, FlightState> activeFlights = new HashMap<>();

    private record FlightState(boolean allowFlight, boolean flying, float flySpeed) {
    }

    public VoidListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (!plugin.getArcaneManager().hasVoid(player)) continue;
                if (plugin.getLevelManager().getLevel(player) <= 0) continue;

                player.getWorld().spawnParticle(
                        Particle.PORTAL,
                        player.getLocation().add(0, 1, 0),
                        ConfigValues.getInt(plugin, "void.particles.passive-portal", 2, 0, 5000),
                        0.25,
                        0.4,
                        0.25,
                        0.01
                );

                player.getWorld().spawnParticle(
                        Particle.SCULK_SOUL,
                        player.getLocation().add(0, 1, 0),
                        ConfigValues.getInt(plugin, "void.particles.passive-sculk-soul", 1, 0, 5000),
                        0.2,
                        0.3,
                        0.2,
                        0
                );
            }
        }, 0L, 20L);
    }

    public void activateVoidFlight(Player player) {

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "void_flight")) {
            plugin.getCooldownManager().showCooldown(player, "Void Flight", "void_flight");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
            return;
        }

        // Apply the cooldown up front so logging off mid-flight cannot skip it.
        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "void_flight",
                ConfigValues.getInt(plugin, "void.flight.cooldown-seconds", 30, 0, 86400)
        );
        plugin.getCooldownManager().startActionBarCooldown(player, "Void Flight", "void_flight");

        activeFlights.put(player.getUniqueId(), new FlightState(
                player.getAllowFlight(),
                player.isFlying(),
                player.getFlySpeed()
        ));

        Vector launch = player.getLocation()
                .getDirection()
                .normalize()
                .multiply(ConfigValues.getDouble(plugin, "void.flight.launch-strength", 1.6, 0.0, 20.0));

        launch.setY(Math.max(ConfigValues.getDouble(plugin, "void.flight.min-y-velocity", 0.6, 0.0, 10.0), launch.getY()));

        player.getWorld().spawnParticle(
                Particle.PORTAL,
                player.getLocation(),
                ConfigValues.getInt(plugin, "void.particles.flight-launch-portal", 6, 0, 5000),
                0.2,
                0.2,
                0.2,
                0.02
        );

        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation(),
                ConfigValues.getInt(plugin, "void.particles.flight-launch-smoke", 3, 0, 5000),
                0.1,
                0.1,
                0.1,
                0
        );

        player.setVelocity(launch);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(ConfigValues.getFloat(plugin, "void.flight.fly-speed", 0.22f, -1.0f, 1.0f));

        player.sendMessage(ChatColor.DARK_PURPLE + "Void Flight activated!");

        player.getWorld().spawnParticle(
                Particle.PORTAL,
                player.getLocation(),
                ConfigValues.getInt(plugin, "void.particles.flight-burst-portal", 100, 0, 5000),
                0.7,
                1,
                0.7,
                0.35
        );

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1f, 0.7f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            FlightState state = activeFlights.remove(player.getUniqueId());
            if (!player.isOnline() || state == null) return;

            restoreFlightState(player, state);

            player.sendMessage(ChatColor.GRAY + "Void Flight ended.");

        }, ConfigValues.getInt(plugin, "void.flight.duration-seconds", 3, 1, 600) * 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        endFlight(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        endFlight(event.getEntity());
    }

    public void endFlight(Player player) {
        FlightState state = activeFlights.remove(player.getUniqueId());
        if (state != null) {
            restoreFlightState(player, state);
        }
    }

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            FlightState state = activeFlights.remove(player.getUniqueId());
            if (state != null) {
                restoreFlightState(player, state);
            }
        }
        activeFlights.clear();
    }

    private void restoreFlightState(Player player, FlightState state) {
        player.setFlying(false);

        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            player.setAllowFlight(true);
            player.setFlying(state.flying());
        } else {
            player.setAllowFlight(state.allowFlight());
            if (state.allowFlight() && state.flying()) {
                player.setFlying(true);
            }
        }

        player.setFlySpeed(state.flySpeed());
        player.setFallDistance(0);
    }

    public void activateVoidBreath(Player player) {

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "void_breath")) {
            plugin.getCooldownManager().showCooldown(player, "Void Breath", "void_breath");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
            return;
        }

        int durationTicks = ConfigValues.getInt(plugin, "void.breath.duration-ticks", 60, 1, 12000);
        int pulseIntervalTicks = ConfigValues.getInt(plugin, "void.breath.pulse-interval-ticks", 10, 1, 12000);
        double totalDamage = ConfigValues.getDouble(plugin, "void.breath.total-damage", 12.0, 0.0, 1000.0);
        int damagePulses = Math.max(1, (int) Math.ceil(durationTicks / (double) pulseIntervalTicks));
        double damagePerPulse = totalDamage / damagePulses;

        final int[] elapsedTicks = {0};
        HashSet<UUID> recentlyHit = new HashSet<>();

        player.sendMessage(ChatColor.DARK_PURPLE + "Void Breath unleashed!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 0.8f);

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }

            Location start = player.getEyeLocation();
            Vector direction = start.getDirection().normalize();
            Vector rightBase = direction.clone().crossProduct(new Vector(0, 1, 0));
            if (rightBase.lengthSquared() == 0) {
                rightBase = new Vector(1, 0, 0);
            } else {
                rightBase.normalize();
            }

            int range = ConfigValues.getInt(plugin, "void.breath.range", 10, 1, 100);
            double maxWidth = ConfigValues.getDouble(plugin, "void.breath.max-width", 6.0, 0.0, 100.0);
            double pointSpacing = ConfigValues.getDouble(plugin, "void.breath.point-spacing", 0.35, 0.05, 5.0);
            double hitRadius = ConfigValues.getDouble(plugin, "void.breath.hit-radius", 1.2, 0.0, 20.0);

            for (int distance = 1; distance <= range; distance++) {

                double width = (distance / (double) range) * maxWidth;

                for (double side = -width; side <= width; side += pointSpacing) {

                    Location point = start.clone()
                            .add(direction.clone().multiply(distance))
                            .add(rightBase.clone().multiply(side));

                    player.getWorld().spawnParticle(Particle.WITCH, point, ConfigValues.getInt(plugin, "void.particles.breath-witch", 18, 0, 5000), 0.08, 0.08, 0.08, 0.02);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, ConfigValues.getInt(plugin, "void.particles.breath-reverse-portal", 10, 0, 5000), 0.05, 0.05, 0.05, 0.04);
                    player.getWorld().spawnParticle(Particle.SMOKE, point, ConfigValues.getInt(plugin, "void.particles.breath-smoke", 6, 0, 5000), 0.06, 0.06, 0.06, 0.01);

                    for (LivingEntity target : point.getNearbyLivingEntities(hitRadius)) {

                        if (target.equals(player)) continue;

                        if (target instanceof Player targetPlayer) {
                            if (plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
                                continue;
                            }
                        }

                        if (recentlyHit.contains(target.getUniqueId())) continue;

                        recentlyHit.add(target.getUniqueId());
                        if (!dealTrueDamage(target, player, damagePerPulse)) {
                            continue;
                        }

                        applyVoidBreathKnockback(target, direction);

                        target.getWorld().playSound(
                                target.getLocation(),
                                Sound.ENTITY_ENDER_DRAGON_HURT,
                                0.6f,
                                1.4f
                        );
                    }
                }
            }

            recentlyHit.clear();
            elapsedTicks[0] += pulseIntervalTicks;

            if (elapsedTicks[0] >= durationTicks) {
                task.cancel();
            }

        }, 0L, pulseIntervalTicks);

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "void_breath",
                ConfigValues.getInt(plugin, "void.breath.cooldown-seconds", 60, 0, 86400)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Void Breath", "void_breath");
    }

    private void applyVoidBreathKnockback(LivingEntity target, Vector direction) {
        Vector knockback = direction.clone();
        knockback.setY(0);

        if (knockback.lengthSquared() == 0) {
            return;
        }

        knockback.normalize().multiply(ConfigValues.getDouble(plugin, "void.breath.knockback.horizontal", 0.25, 0.0, 10.0));
        knockback.setY(ConfigValues.getDouble(plugin, "void.breath.knockback.vertical", 0.06, 0.0, 10.0));
        target.setVelocity(target.getVelocity().add(knockback));
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
    public void onVoidFallDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        if (plugin.getArcaneManager().hasVoid(player)
                && plugin.getLevelManager().getLevel(player) > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVoidBreathUse(PlayerInteractEvent event) {

        if (event.getHand() == null) return;

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!VoidArcane.isVoidArcane(item)) return;

        event.setCancelled(true);

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            return;
        }

        activateVoidBreath(player);
    }

    @EventHandler
    public void onVoidFlightUse(PlayerInteractEvent event) {

        if (event.getHand() == null) return;

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!VoidArcane.isVoidArcane(item)) return;

        event.setCancelled(true);

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            return;
        }

        activateVoidFlight(player);
    }
}
