package me.aydeejay.arcanecore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityTriggerManager {

    private final ArcaneCore plugin;
    private final File file;
    private final HashMap<UUID, String> triggers = new HashMap<>();

    public AbilityTriggerManager(ArcaneCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ability-triggers.yml");
        load();
    }

    public String getTrigger(Player player) {
        return triggers.getOrDefault(player.getUniqueId(), "sneakrightclick");
    }

    public void setTrigger(Player player, String trigger) {
        triggers.put(player.getUniqueId(), trigger.toLowerCase());
        save();
    }

    public boolean isSneakRightClick(Player player) {
        return getTrigger(player).equalsIgnoreCase("sneakrightclick");
    }

    public boolean isSwap(Player player) {
        return getTrigger(player).equalsIgnoreCase("swap");
    }

    public boolean isSlot(Player player, int slot) {
        return getTrigger(player).equalsIgnoreCase("slot" + slot);
    }

    public void load() {
        triggers.clear();

        if (!file.exists()) {
            return;
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            try {
                triggers.put(UUID.fromString(key), data.getString(key, "sneakrightclick"));
            } catch (IllegalArgumentException ignored) {
                // Skip non-UUID keys.
            }
        }
    }

    public void save() {
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : triggers.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save ability-triggers.yml: " + e.getMessage());
        }
    }
}
