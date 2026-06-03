package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

        if (event.getHand() != EquipmentSlot.HAND) {
            return; // fire once, on the main-hand pass
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LuckArcane.isLuckArcane(item)) {
            return;
        }

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
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
                        ConfigValues.getInt(plugin, "luck.particles.lucky-save", 20, 0, 5000),
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
            negates = ConfigValues.getInt(plugin, "luck.negates.level-4", 3, 0, 1000);
        } else if (level >= 3) {
            negates = ConfigValues.getInt(plugin, "luck.negates.level-3", 2, 0, 1000);
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

        remainingHits.put(player.getUniqueId(), ConfigValues.getInt(plugin, "luck.active-hits", 5, 1, 1000));
        remainingNegates.put(player.getUniqueId(), negates);

        player.sendMessage(ChatColor.GREEN + "Lucky Protection activated!");

        player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                player.getLocation(),
                ConfigValues.getInt(plugin, "luck.particles.activate", 40, 0, 5000),
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
                ConfigValues.getInt(plugin, "luck.cooldown-seconds", 60, 0, 86400)
        );

        plugin.getCooldownManager().startActionBarCooldown(player, "Luck", "luck");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearProtection(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        clearProtection(event.getEntity());
    }

    private void clearProtection(Player player) {
        remainingHits.remove(player.getUniqueId());
        remainingNegates.remove(player.getUniqueId());
    }

    public void startPassiveTask() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (plugin.getArcaneManager().hasLuck(player)) {
                    if (plugin.getLevelManager().getLevel(player) <= 0) continue;

                    if (ConfigValues.isPotionEnabled(plugin, "luck.passive.hero-level", 10)) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.HERO_OF_THE_VILLAGE,
                                ConfigValues.getInt(plugin, "luck.passive.effect-duration-ticks", 60, 1, 6000),
                                ConfigValues.getPotionAmplifier(plugin, "luck.passive.hero-level", 10),
                                true,
                                false
                        ));
                    }

                }
            }

        }, 0L, 20L);
    }
}
