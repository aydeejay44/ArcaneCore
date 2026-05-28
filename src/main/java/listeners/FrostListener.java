package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
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
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED,
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
        if (event.getHand() == null) return;

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!plugin.getAbilityTriggerManager().isSneakRightClick(player)) return;
        if (!player.isSneaking()) return;

        ItemStack item = event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (!FrostArcane.isFrostArcane(item)) return;

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendActionBar(ChatColor.RED + "You unlock this ability at Level 3.");
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
                ? plugin.getConfig().getInt("frost.radius.level-4", 10)
                : plugin.getConfig().getInt("frost.radius.level-3", 5);

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
            player.sendActionBar(ChatColor.RED + "No valid targets nearby.");
            return;
        }

        int durationSeconds = level >= 4
                ? plugin.getConfig().getInt("frost.freeze-duration.level-4", 3)
                : plugin.getConfig().getInt("frost.freeze-duration.level-3", 3);

        double totalDamage = level >= 4
                ? plugin.getConfig().getDouble("frost.total-damage.level-4", 0.0)
                : plugin.getConfig().getDouble("frost.total-damage.level-3", 0.0);

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "frost",
                plugin.getConfig().getInt("frost.cooldown-seconds", 75)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Frost", "frost");

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.4f);

        player.getWorld().spawnParticle(
                Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0),
                40,
                0.5,
                0.5,
                0.5,
                0.03
        );

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            playFrostRing(player, radius);
            freezeTargets(player, targets, durationSeconds, totalDamage);
        }, 4L);
    }

    private void playFrostRing(Player player, int radius) {
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 24) {
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location loc = player.getLocation().clone().add(x, 0.15, z);

            player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    loc,
                    4,
                    0.05,
                    0.05,
                    0.05,
                    0
            );

            player.getWorld().spawnParticle(
                    Particle.ITEM_SNOWBALL,
                    loc,
                    2,
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

            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    durationSeconds * 20,
                    254,
                    true,
                    false,
                    false
            ));

            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP_BOOST,
                    durationSeconds * 20,
                    128,
                    true,
                    false,
                    false
            ));

            new BukkitRunnable() {
                int ticks = durationSeconds;

                @Override
                public void run() {
                    if (target.isDead()) {
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
                            35,
                            0.5,
                            1,
                            0.5,
                            0.02
                    );

                    target.getWorld().spawnParticle(
                            Particle.ITEM_SNOWBALL,
                            target.getLocation().add(0, 1, 0),
                            20,
                            0.4,
                            0.8,
                            0.4,
                            0.01
                    );

                    if (damagePerSecond > 0) {
                        target.setNoDamageTicks(0);
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

    @EventHandler
    public void onFrozenMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!frozenPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);
    }
}