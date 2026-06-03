package me.aydeejay.arcanecore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

public class LevelManager {

    private static final int DEFAULT_LEVEL = 2;

    private final ArcaneCore plugin;
    private final HashMap<UUID, Integer> levels = new HashMap<>();
    private final File file;

    public LevelManager(ArcaneCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "levels.yml");
    }

    public int getLevel(Player player) {
        return levels.getOrDefault(player.getUniqueId(), DEFAULT_LEVEL);
    }

    public void setLevel(Player player, int level) {
        int maxLevel = ConfigValues.getInt(plugin, "levels.max-level", 4, 0, 100);
        int cappedLevel = Math.max(0, Math.min(maxLevel, level));
        levels.put(player.getUniqueId(), cappedLevel);
        save();
    }

    public void addLevels(Player player, int amount) {
        int maxLevel = ConfigValues.getInt(plugin, "levels.max-level", 4, 0, 100);

        if (getLevel(player) >= maxLevel && amount > 0) {
            return;
        }

        setLevel(player, getLevel(player) + amount);
    }

    // Persist the default for first-seen players so the starting level is not
    // silently re-derived (and lost) every session.
    public void ensureLevel(Player player) {
        if (!levels.containsKey(player.getUniqueId())) {
            levels.put(player.getUniqueId(), DEFAULT_LEVEL);
            save();
        }
    }

    public boolean hasAbility(Player player) {
        int unlockLevel = ConfigValues.getInt(plugin, "levels.ability-unlock-level", 3, 0, 100);
        return getLevel(player) >= unlockLevel;
    }

    public boolean canUseArcaneAbility(Player player) {
        return hasAbility(player);
    }

    public void load() {
        levels.clear();

        if (file.exists()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);

            for (String key : data.getKeys(false)) {
                try {
                    levels.put(UUID.fromString(key), data.getInt(key));
                } catch (IllegalArgumentException ignored) {
                    // Skip non-UUID keys.
                }
            }
        }

        migrateLegacyLevels();
    }

    private void migrateLegacyLevels() {
        if (plugin.getConfig().getBoolean("data-migrations.levels-from-config", false)) {
            return;
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("levels");
        if (section == null) {
            return;
        }

        boolean migrated = false;
        for (String key : section.getKeys(false)) {
            try {
                levels.put(UUID.fromString(key), section.getInt(key));
                migrated = true;
            } catch (IllegalArgumentException ignored) {
                // Keep normal level settings in config.yml.
            }
        }

        if (migrated && saveToFile()) {
            plugin.getConfig().set("data-migrations.levels-from-config", true);
            plugin.saveConfig();
        }
    }

    public void save() {
        saveToFile();
    }

    private boolean saveToFile() {
        FileConfiguration data = new YamlConfiguration();

        for (UUID uuid : levels.keySet()) {
            data.set(uuid.toString(), levels.get(uuid));
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            if (file.exists()) {
                Files.copy(
                        file.toPath(),
                        new File(plugin.getDataFolder(), "levels.yml.bak").toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
            data.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save levels.yml: " + e.getMessage());
            return false;
        }
    }
}
