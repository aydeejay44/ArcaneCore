package me.aydeejay.arcanecore;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {

    private final ArcaneCore plugin;
    private final HashMap<String, Long> cooldowns = new HashMap<>();

    public CooldownManager(ArcaneCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::clearExpired, 20L * 60, 20L * 60);
    }

    public boolean isOnCooldown(UUID uuid, String ability) {
        String key = uuid + ":" + ability;
        Long until = cooldowns.get(key);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() >= until) {
            cooldowns.remove(key); // evict expired entry
            return false;
        }
        return true;
    }

    public void setCooldown(UUID uuid, String ability, int seconds) {
        String key = uuid + ":" + ability;
        cooldowns.put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public long getTimeLeft(UUID uuid, String ability) {
        return getRemainingMillis(uuid, ability) / 1000;
    }

    public long getRemainingMillis(UUID uuid, String ability) {
        String key = uuid + ":" + ability;
        Long until = cooldowns.get(key);
        if (until == null) {
            return 0;
        }
        long remaining = until - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(key);
            return 0;
        }
        return remaining;
    }

    private void clearExpired() {
        long now = System.currentTimeMillis();
        cooldowns.values().removeIf(until -> until <= now);
    }

    public void showCooldown(Player player, String abilityName, String abilityKey) {
        long remainingMillis = getRemainingMillis(player.getUniqueId(), abilityKey);

        if (remainingMillis <= 0) {
            player.sendActionBar(ChatColor.GREEN + abilityName + " ready!");
            return;
        }

        double seconds = remainingMillis / 1000.0;
        player.sendActionBar(ChatColor.RED + abilityName + " cooldown: " + String.format("%.1f", seconds) + "s");
    }

    public void startActionBarCooldown(Player player, String abilityName, String abilityKey) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                long remainingMillis = getRemainingMillis(player.getUniqueId(), abilityKey);

                if (remainingMillis <= 0) {
                    player.sendActionBar(ChatColor.GREEN + abilityName + " ready!");
                    cancel();
                    return;
                }

                double seconds = remainingMillis / 1000.0;
                player.sendActionBar(ChatColor.RED + abilityName + " cooldown: " + String.format("%.1f", seconds) + "s");
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
