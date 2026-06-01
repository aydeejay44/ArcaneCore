package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
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
                        2,
                        0.25,
                        0.4,
                        0.25,
                        0.01
                );

                player.getWorld().spawnParticle(
                        Particle.SCULK_SOUL,
                        player.getLocation().add(0, 1, 0),
                        1,
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
                plugin.getConfig().getInt("void.flight.cooldown-seconds", 30)
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
                .multiply(plugin.getConfig().getDouble("void.flight.launch-strength", 1.6));

        launch.setY(Math.max(0.6, launch.getY()));

        player.getWorld().spawnParticle(
                Particle.PORTAL,
                player.getLocation(),
                6,
                0.2,
                0.2,
                0.2,
                0.02
        );

        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation(),
                3,
                0.1,
                0.1,
                0.1,
                0
        );

        player.setVelocity(launch);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed((float) plugin.getConfig().getDouble("void.flight.fly-speed", 0.22));

        player.sendMessage(ChatColor.DARK_PURPLE + "Void Flight activated!");

        player.getWorld().spawnParticle(
                Particle.PORTAL,
                player.getLocation(),
                100,
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

        }, plugin.getConfig().getInt("void.flight.duration-seconds", 3) * 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        endFlight(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        endFlight(event.getEntity());
    }

    private void endFlight(Player player) {
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

        final int[] durationTicks = {
                plugin.getConfig().getInt("void.breath.duration-ticks", 60)
        };

        double totalDamage = plugin.getConfig().getDouble("void.breath.total-damage", 12.0);
        double damagePerTick = totalDamage / (durationTicks[0] / 10.0);

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
            Vector rightBase = direction.clone()
                    .crossProduct(new Vector(0, 1, 0))
                    .normalize();

            int range = plugin.getConfig().getInt("void.breath.range", 10);
            double maxWidth = plugin.getConfig().getDouble("void.breath.max-width", 6.0);

            for (int distance = 1; distance <= range; distance++) {

                double width = (distance / (double) range) * maxWidth;

                for (double side = -width; side <= width; side += 0.35) {

                    Location point = start.clone()
                            .add(direction.clone().multiply(distance))
                            .add(rightBase.clone().multiply(side));

                    player.getWorld().spawnParticle(Particle.WITCH, point, 18, 0.08, 0.08, 0.08, 0.02);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 10, 0.05, 0.05, 0.05, 0.04);
                    player.getWorld().spawnParticle(Particle.SMOKE, point, 6, 0.06, 0.06, 0.06, 0.01);

                    for (LivingEntity target : point.getNearbyLivingEntities(1.2)) {

                        if (target.equals(player)) continue;

                        if (target instanceof Player targetPlayer) {
                            if (plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
                                continue;
                            }
                        }

                        if (recentlyHit.contains(target.getUniqueId())) continue;

                        recentlyHit.add(target.getUniqueId());
                        target.damage(damagePerTick, player);

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
            durationTicks[0] -= 10;

            if (durationTicks[0] <= 0) {
                task.cancel();
            }

        }, 0L, 10L);

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "void_breath",
                plugin.getConfig().getInt("void.breath.cooldown-seconds", 60)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Void Breath", "void_breath");
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
            player.sendActionBar(ChatColor.RED + "You unlock this ability at Level 3.");
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
            player.sendActionBar(ChatColor.RED + "You unlock this ability at Level 3.");
            return;
        }

        activateVoidFlight(player);
    }
}
