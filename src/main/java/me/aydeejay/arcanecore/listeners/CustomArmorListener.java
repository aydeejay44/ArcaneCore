package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.items.HasteLeggings;
import me.aydeejay.arcanecore.items.HeartHelmet;
import me.aydeejay.arcanecore.items.ResistanceChestplate;
import me.aydeejay.arcanecore.items.SpeedBoots;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomArmorListener implements Listener {

    private final ArcaneCore plugin;

    public CustomArmorListener(ArcaneCore plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                ItemStack helmet = player.getInventory().getHelmet();
                ItemStack chestplate = player.getInventory().getChestplate();
                ItemStack leggings = player.getInventory().getLeggings();
                ItemStack boots = player.getInventory().getBoots();

                // HEART HELMET

                if (HeartHelmet.isHeartHelmet(helmet)) {

                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            player.getLocation().add(0, 2.1, 0),
                            8,
                            0.3,
                            0.3,
                            0.3,
                            new Particle.DustOptions(Color.RED, 1.4f)
                    );

                }

                // RESISTANCE CHESTPLATE

                if (ResistanceChestplate.isResistanceChestplate(chestplate)) {

                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.RESISTANCE,
                            60,
                            0,
                            true,
                            false
                    ));

                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            player.getLocation().add(0, 1, 0),
                            12,
                            0.35,
                            0.6,
                            0.35,
                            0.03
                    );
                }

                // HASTE LEGGINGS

                if (HasteLeggings.isHasteLeggings(leggings)) {

                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.HASTE,
                            60,
                            1,
                            true,
                            false
                    ));

                    player.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            player.getLocation().add(0, 0.7, 0),
                            8,
                            0.35,
                            0.4,
                            0.35,
                            0.03
                    );
                }

                // SPEED BOOTS

                if (SpeedBoots.isSpeedBoots(boots)) {

                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED,
                            60,
                            2,
                            true,
                            false
                    ));

                    player.getWorld().spawnParticle(
                            Particle.CLOUD,
                            player.getLocation().add(0, 0.1, 0),
                            10,
                            0.25,
                            0.05,
                            0.25,
                            0.02
                    );

                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            player.getLocation().add(0, 0.1, 0),
                            6,
                            0.2,
                            0.05,
                            0.2,
                            new Particle.DustOptions(Color.AQUA, 1.2f)
                    );
                }

            }

        }, 0L, 20L);
    }
}