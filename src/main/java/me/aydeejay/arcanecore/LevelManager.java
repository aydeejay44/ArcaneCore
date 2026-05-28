package me.aydeejay.arcanecore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.UUID;

public class LevelManager {

    private final ArcaneCore plugin;
    private final HashMap<UUID, Integer> levels = new HashMap<>();

    public LevelManager(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public int getLevel(Player player) {
        return levels.getOrDefault(player.getUniqueId(), 2);
    }

    public void setLevel(Player player, int level) {
        int maxLevel = plugin.getConfig().getInt("levels.max-level", 4);
        int cappedLevel = Math.max(0, Math.min(maxLevel, level));
        levels.put(player.getUniqueId(), cappedLevel);
    }

    public void addLevels(Player player, int amount) {
        int maxLevel = plugin.getConfig().getInt("levels.max-level", 4);

        if (getLevel(player) >= maxLevel && amount > 0) {
            return;
        }

        setLevel(player, getLevel(player) + amount);
    }

    public boolean hasAbility(Player player) {
        int unlockLevel = plugin.getConfig().getInt("levels.ability-unlock-level", 3);
        return getLevel(player) >= unlockLevel;
    }

    public boolean canUseArcaneAbility(Player player) {
        return getLevel(player) >= 3;
    }

    public void loadLevels() {

        levels.clear();

        if (!plugin.getConfig().contains("levels")) {
            return;
        }

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("levels");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {

            try {
                UUID uuid = UUID.fromString(key);

                levels.put(
                        uuid,
                        section.getInt(key)
                );

            } catch (IllegalArgumentException ignored) {
                // Ignore non-UUID config keys
            }
        }
    }

    public void saveLevels() {
        for (UUID uuid : levels.keySet()) {
            plugin.getConfig().set("levels." + uuid.toString(), levels.get(uuid));
        }

        plugin.saveConfig();
    }

    public void fixLevelZeroPlayersToTwo() {
        for (UUID uuid : levels.keySet()) {
            if (levels.get(uuid) <= 0) {
                levels.put(uuid, 2);
            }
        }

        saveLevels();
    }
}