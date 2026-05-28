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
    }

    public boolean isOnCooldown(UUID uuid, String ability) {
        String key = uuid + ":" + ability;
        return cooldowns.containsKey(key) && System.currentTimeMillis() < cooldowns.get(key);
    }

    public void setCooldown(UUID uuid, String ability, int seconds) {
        String key = uuid + ":" + ability;
        cooldowns.put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public long getTimeLeft(UUID uuid, String ability) {
        String key = uuid + ":" + ability;

        if (!cooldowns.containsKey(key)) {
            return 0;
        }

        return Math.max(0, (cooldowns.get(key) - System.currentTimeMillis()) / 1000);
    }

    public long getRemainingMillis(UUID uuid, String ability) {
        String key = uuid + ":" + ability;

        if (!cooldowns.containsKey(key)) {
            return 0;
        }

        return Math.max(0, cooldowns.get(key) - System.currentTimeMillis());
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