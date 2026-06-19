package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.items.HasteLeggings;
import me.aydeejay.arcanecore.items.HeartHelmet;
import me.aydeejay.arcanecore.items.ResistanceChestplate;
import me.aydeejay.arcanecore.items.SpeedBoots;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomArmorListener implements Listener {

    private static final ArmorTrim FLOW_TRIM = new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.FLOW);
    private static final int TASK_PERIOD_TICKS = 20;

    private final ArcaneCore plugin;
    private int particleCooldownTicks;

    public CustomArmorListener(ArcaneCore plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            boolean spawnParticles = shouldSpawnParticles();

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                updateExistingArmorTrims(player);

                ItemStack helmet = player.getInventory().getHelmet();
                ItemStack chestplate = player.getInventory().getChestplate();
                ItemStack leggings = player.getInventory().getLeggings();
                ItemStack boots = player.getInventory().getBoots();

                // THE CROWN

                if (spawnParticles && HeartHelmet.isHeartHelmet(helmet)) {

                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            player.getLocation().add(0, 2.1, 0),
                            ConfigValues.getInt(plugin, "custom-items.heart-helmet.particles.dust", 2, 0, 1000),
                            0.3,
                            0.3,
                            0.3,
                            new Particle.DustOptions(
                                    Color.RED,
                                    ConfigValues.getFloat(plugin, "custom-items.heart-helmet.particles.dust-size", 0.8f, 0.1f, 10.0f)
                            )
                    );

                }

                // RESISTANCE CHESTPLATE

                if (ResistanceChestplate.isResistanceChestplate(chestplate)) {

                    if (ConfigValues.isPotionEnabled(plugin, "custom-items.resistance-chestplate.resistance-level", 1)) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.RESISTANCE,
                                ConfigValues.getInt(plugin, "custom-items.resistance-chestplate.effect-duration-ticks", 60, 1, 6000),
                                ConfigValues.getPotionAmplifier(plugin, "custom-items.resistance-chestplate.resistance-level", 1),
                                true,
                                false
                        ));
                    }

                    if (spawnParticles) {
                        player.getWorld().spawnParticle(
                                Particle.PORTAL,
                                player.getLocation().add(0, 1, 0),
                                ConfigValues.getInt(plugin, "custom-items.resistance-chestplate.particles.portal", 3, 0, 1000),
                                0.35,
                                0.6,
                                0.35,
                                0.03
                        );
                    }
                }

                // HASTE LEGGINGS

                if (HasteLeggings.isHasteLeggings(leggings)) {

                    if (ConfigValues.isPotionEnabled(plugin, "custom-items.haste-leggings.haste-level", 2)) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.HASTE,
                                ConfigValues.getInt(plugin, "custom-items.haste-leggings.effect-duration-ticks", 60, 1, 6000),
                                ConfigValues.getPotionAmplifier(plugin, "custom-items.haste-leggings.haste-level", 2),
                                true,
                                false
                        ));
                    }

                    if (spawnParticles) {
                        player.getWorld().spawnParticle(
                                Particle.HAPPY_VILLAGER,
                                player.getLocation().add(0, 0.7, 0),
                                ConfigValues.getInt(plugin, "custom-items.haste-leggings.particles.happy-villager", 2, 0, 1000),
                                0.35,
                                0.4,
                                0.35,
                                0.03
                        );
                    }
                }

                // SPEED BOOTS

                if (SpeedBoots.isSpeedBoots(boots)) {

                    if (ConfigValues.isPotionEnabled(plugin, "custom-items.speed-boots.speed-level", 3)) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.SPEED,
                                ConfigValues.getInt(plugin, "custom-items.speed-boots.effect-duration-ticks", 60, 1, 6000),
                                ConfigValues.getPotionAmplifier(plugin, "custom-items.speed-boots.speed-level", 3),
                                true,
                                false
                        ));
                    }

                    if (spawnParticles) {
                        player.getWorld().spawnParticle(
                                Particle.CLOUD,
                                player.getLocation().add(0, 0.1, 0),
                                ConfigValues.getInt(plugin, "custom-items.speed-boots.particles.cloud", 2, 0, 1000),
                                0.25,
                                0.05,
                                0.25,
                                0.02
                        );

                        player.getWorld().spawnParticle(
                                Particle.DUST,
                                player.getLocation().add(0, 0.1, 0),
                                ConfigValues.getInt(plugin, "custom-items.speed-boots.particles.dust", 1, 0, 1000),
                                0.2,
                                0.05,
                                0.2,
                                new Particle.DustOptions(
                                        Color.AQUA,
                                        ConfigValues.getFloat(plugin, "custom-items.speed-boots.particles.dust-size", 0.8f, 0.1f, 10.0f)
                                )
                        );
                    }
                }

            }

        }, 0L, TASK_PERIOD_TICKS);
    }

    private boolean shouldSpawnParticles() {
        int intervalTicks = ConfigValues.getInt(plugin, "custom-items.particle-interval-ticks", 80, 0, 12000);
        if (intervalTicks <= 0) {
            return false;
        }

        if (particleCooldownTicks > 0) {
            particleCooldownTicks = Math.max(0, particleCooldownTicks - TASK_PERIOD_TICKS);
            return false;
        }

        particleCooldownTicks = Math.max(TASK_PERIOD_TICKS, intervalTicks) - TASK_PERIOD_TICKS;
        return true;
    }

    private void updateExistingArmorTrims(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (!isCustomArmor(item) || !(item.getItemMeta() instanceof ArmorMeta meta)) {
                continue;
            }

            if (HeartHelmet.isHeartHelmet(item)) {
                if (meta.hasTrim()) {
                    meta.setTrim(null);
                    item.setItemMeta(meta);
                }
                continue;
            }

            if (!FLOW_TRIM.equals(meta.getTrim())) {
                meta.setTrim(FLOW_TRIM);
                item.setItemMeta(meta);
            }
        }
    }

    private boolean isCustomArmor(ItemStack item) {
        return HeartHelmet.isHeartHelmet(item)
                || ResistanceChestplate.isResistanceChestplate(item)
                || HasteLeggings.isHasteLeggings(item)
                || SpeedBoots.isSpeedBoots(item);
    }
}
