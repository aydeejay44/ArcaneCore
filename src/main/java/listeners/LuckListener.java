package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class LuckListener implements Listener {

    private final ArcaneCore plugin;

    private final HashMap<UUID, Integer> remainingHits = new HashMap<>();
    private final HashMap<UUID, Integer> remainingNegates = new HashMap<>();

    private final Random random = new Random();

    public LuckListener(ArcaneCore plugin) {
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

        ItemStack item;

        if (event.getHand() == EquipmentSlot.HAND) {
            item = player.getInventory().getItemInMainHand();
        } else {
            item = player.getInventory().getItemInOffHand();
        }

        if (!LuckArcane.isLuckArcane(item)) {
            return;
        }

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendActionBar("§cYou unlock this ability at Level 3.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        activateLuck(player);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!remainingHits.containsKey(uuid)) {
            return;
        }

        int hitsLeft = remainingHits.get(uuid);

        if (hitsLeft <= 0) {
            remainingHits.remove(uuid);
            remainingNegates.remove(uuid);

            player.sendMessage(ChatColor.RED + "Your lucky protection has worn off.");
            return;
        }

        int negatesLeft = remainingNegates.get(uuid);

        if (negatesLeft > 0) {

            int hitsLeftIncludingThis = hitsLeft;

            double negateChance = (double) negatesLeft / hitsLeftIncludingThis;

            if (random.nextDouble() <= negateChance) {

                remainingNegates.put(uuid, negatesLeft - 1);

                event.setCancelled(true);

                player.sendMessage(ChatColor.GREEN + "Lucky save! Damage negated.");

                player.getWorld().spawnParticle(
                        Particle.TOTEM_OF_UNDYING,
                        player.getLocation(),
                        20,
                        0.4,
                        1,
                        0.4,
                        0.05
                );

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        1.0f,
                        1.5f
                );
            }
        }

        hitsLeft--;

        remainingHits.put(uuid, hitsLeft);

        if (hitsLeft <= 0) {
            remainingHits.remove(uuid);
            remainingNegates.remove(uuid);

            player.sendMessage(ChatColor.RED + "Your lucky protection has worn off.");
        }
    }

    public void activateLuck(Player player) {

        int level = plugin.getLevelManager().getLevel(player);

        int negates = 0;

        if (level >= 4) {
            negates = plugin.getConfig().getInt("luck.negates.level-4", 3);
        } else if (level >= 3) {
            negates = plugin.getConfig().getInt("luck.negates.level-3", 2);
        }

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "luck")) {
            plugin.getCooldownManager().showCooldown(player, "Luck", "luck");
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_BASS,
                    0.7f,
                    0.5f
            );
            return;
        }

        remainingHits.put(player.getUniqueId(), plugin.getConfig().getInt("luck.active-hits", 5));
        remainingNegates.put(player.getUniqueId(), negates);

        player.sendMessage(ChatColor.GREEN + "Lucky Protection activated!");

        player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                player.getLocation(),
                40,
                0.5,
                1,
                0.5,
                0.1
        );

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                1.0f,
                1.2f
        );

        plugin.getCooldownManager().setCooldown(
                player.getUniqueId(),
                "luck",
                plugin.getConfig().getInt("luck.cooldown-seconds", 60)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Luck", "luck");
    }

    public void startPassiveTask() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (plugin.getArcaneManager().hasLuck(player)) {
                    if (plugin.getLevelManager().getLevel(player) <= 0) continue;

                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.HERO_OF_THE_VILLAGE,
                            60,
                            9,
                            true,
                            false
                    ));

                }
            }

        }, 0L, 20L);
    }
}