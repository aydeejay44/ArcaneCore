package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

public class FrostListener implements Listener {

    private final ArcaneCore plugin;
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public FrostListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (plugin.getArcaneManager().hasFrost(player)) {
                    if (plugin.getLevelManager().getLevel(player) <= 0) continue;
                    int level = plugin.getLevelManager().getLevel(player);

                    String speedPath = level >= 4
                            ? "frost.passive.speed-level.level-4"
                            : "frost.passive.speed-level.level-3";

                    if (ConfigValues.isPotionEnabled(plugin, speedPath, level >= 4 ? 2 : 1)) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.SPEED,
                                ConfigValues.getInt(plugin, "frost.passive.effect-duration-ticks", 60, 1, 6000),
                                ConfigValues.getPotionAmplifier(plugin, speedPath, level >= 4 ? 2 : 1),
                                true,
                                false
                        ));
                    }
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

        if (!FrostArcane.isFrostArcane(item)) return;

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        activateFrost(player);
    }

    public void activateFrost(Player player) {
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "frost")) {
            plugin.getCooldownManager().showCooldown(player, "Frost", "frost");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
            return;
        }

        int level = plugin.getLevelManager().getLevel(player);

        int radius = level >= 4
                ? ConfigValues.getInt(plugin, "frost.radius.level-4", 10, 0, 100)
                : ConfigValues.getInt(plugin, "frost.radius.level-3", 5, 0, 100);

        List<LivingEntity> targets = new ArrayList<>();

        for (LivingEntity target : player.getLocation().getNearbyLivingEntities(radius)) {
            if (target.equals(player)) continue;

            if (target instanceof Player targetPlayer) {
                if (plugin.getTrustManager().shouldBlockDamage(player, targetPlayer)) {
                    continue;
                }
            }

            targets.add(target);
        }

        if (targets.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No valid targets nearby.");
            return;
        }

        int durationSeconds = level >= 4
                ? ConfigValues.getInt(plugin, "frost.freeze-duration.level-4", 3, 1, 600)
                : ConfigValues.getInt(plugin, "frost.freeze-duration.level-3", 3, 1, 600);

        double totalDamage = level >= 4
                ? ConfigValues.getDouble(plugin, "frost.total-damage.level-4", 4.0, 0.0, 1000.0)
                : ConfigValues.getDouble(plugin, "frost.total-damage.level-3", 0.0, 0.0, 1000.0);

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "frost",
                ConfigValues.getInt(plugin, "frost.cooldown-seconds", 75, 0, 86400)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Frost", "frost");

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.4f);

        player.getWorld().spawnParticle(
                Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0),
                ConfigValues.getInt(plugin, "frost.particles.cast-snowflake", 40, 0, 5000),
                0.5,
                0.5,
                0.5,
                0.03
        );

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                playFrostRing(player, radius);
                freezeTargets(player, targets, durationSeconds, totalDamage);
            }
        }, ConfigValues.getLong(plugin, "frost.particles.cast-delay-ticks", 4L, 0L, 200L));
    }

    private void playFrostRing(Player player, int radius) {
        int ringPoints = ConfigValues.getInt(plugin, "frost.particles.ring-points", 48, 1, 720);
        for (int i = 0; i < ringPoints; i++) {
            double angle = (Math.PI * 2 * i) / ringPoints;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location loc = player.getLocation().clone().add(x, 0.15, z);

            player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    loc,
                    ConfigValues.getInt(plugin, "frost.particles.ring-snowflake", 4, 0, 5000),
                    0.05,
                    0.05,
                    0.05,
                    0
            );

            player.getWorld().spawnParticle(
                    Particle.ITEM_SNOWBALL,
                    loc,
                    ConfigValues.getInt(plugin, "frost.particles.ring-snowball", 2, 0, 5000),
                    0.03,
                    0.03,
                    0.03,
                    0
            );
        }

        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
    }

    private void freezeTargets(Player player, List<LivingEntity> targets, int durationSeconds, double totalDamage) {
        double damagePerSecond = totalDamage / durationSeconds;

        for (LivingEntity target : targets) {

            if (target instanceof Player targetPlayer) {
                frozenPlayers.add(targetPlayer.getUniqueId());
                targetPlayer.setFreezeTicks(durationSeconds * 20);
                targetPlayer.setVelocity(new Vector(0, 0, 0));
            }

            if (ConfigValues.isPotionEnabled(plugin, "frost.freeze-effects.slowness-level", 255)) {
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        durationSeconds * 20,
                        ConfigValues.getPotionAmplifier(plugin, "frost.freeze-effects.slowness-level", 255),
                        true,
                        false,
                        false
                ));
            }

            if (ConfigValues.isPotionEnabled(plugin, "frost.freeze-effects.jump-boost-level", 129)) {
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.JUMP_BOOST,
                        durationSeconds * 20,
                        ConfigValues.getPotionAmplifier(plugin, "frost.freeze-effects.jump-boost-level", 129),
                        true,
                        false,
                        false
                ));
            }

            new BukkitRunnable() {
                int ticks = durationSeconds;

                @Override
                public void run() {
                    if (target.isDead() || !target.isValid()) {
                        if (target instanceof Player targetPlayer) {
                            frozenPlayers.remove(targetPlayer.getUniqueId());
                            targetPlayer.setFreezeTicks(0);
                        }
                        cancel();
                        return;
                    }

                    target.setVelocity(new Vector(0, 0, 0));

                    target.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            target.getLocation().add(0, 1, 0),
                            ConfigValues.getInt(plugin, "frost.particles.target-snowflake", 35, 0, 5000),
                            0.5,
                            1,
                            0.5,
                            0.02
                    );

                    target.getWorld().spawnParticle(
                            Particle.ITEM_SNOWBALL,
                            target.getLocation().add(0, 1, 0),
                            ConfigValues.getInt(plugin, "frost.particles.target-snowball", 20, 0, 5000),
                            0.4,
                            0.8,
                            0.4,
                            0.01
                    );

                    if (damagePerSecond > 0) {
                        target.damage(damagePerSecond, player);
                    }

                    ticks--;

                    if (ticks <= 0) {
                        target.setVelocity(new Vector(0, 0, 0));
                        target.removePotionEffect(PotionEffectType.SLOWNESS);
                        target.removePotionEffect(PotionEffectType.JUMP_BOOST);

                        if (target instanceof Player targetPlayer) {
                            frozenPlayers.remove(targetPlayer.getUniqueId());
                            targetPlayer.setFreezeTicks(0);
                            targetPlayer.sendMessage(ChatColor.RED + "You are no longer frozen.");
                        }

                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    public void unfreeze(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        player.setFreezeTicks(0);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    @EventHandler
    public void onFrozenMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!frozenPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);
    }
}
