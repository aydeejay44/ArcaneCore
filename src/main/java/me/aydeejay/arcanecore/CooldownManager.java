package me.aydeejay.arcanecore;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {

    private final HashMap<String, Long> cooldowns = new HashMap<>();

    public CooldownManager(ArcaneCore plugin) {
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

    public void clearCooldowns(UUID uuid) {
        String prefix = uuid + ":";
        cooldowns.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public void clearAllCooldowns() {
        cooldowns.clear();
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
        // Intentionally empty. ArcaneStatusBarManager owns all action-bar UI.
    }

    public void startActionBarCooldown(Player player, String abilityName, String abilityKey) {
        // Intentionally empty. The permanent status bar displays cooldowns.
    }
}
